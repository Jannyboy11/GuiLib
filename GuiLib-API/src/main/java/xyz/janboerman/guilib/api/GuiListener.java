package xyz.janboerman.guilib.api;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.WeakHashMap;
import java.util.function.Consumer;

/**
 * The listener that listens when GUIs are opened, clicked and closed. There should only ever be one instance registered.
 * <br> That means that, when you shade and relocate GuiLib into your plugin, you have to register it yourself in your onEnable.
 * <pre>
 *     <code>
 *     {@literal @}Override
 *     public void onEnable() {
 *         getServer().getPluginManager().registerEvents(GuiListener.getInstance(), this);
 *
 *         // more initialization stuff...
 *     }
 *     </code>
 * </pre>
 * If instead you decide to use GuiLib as a runtime dependency and put the jar in your plugins folder, GuiLib registers this listener itself.
 */
public class GuiListener implements Listener {

    private static final GuiListener INSTANCE = new GuiListener();

    //Does not contain inventories whose holders are GuiInventoryHolders. See CraftInventoryCreator.
    private final WeakHashMap<Object/*NMS Inventory*/, WeakReference<GuiInventoryHolder<?>>> guiInventories = new WeakHashMap<>();

    private GuiListener() {}

    /**
     * Gets the GuiListener.
     * @return the Gui listener singleton instance
     */
    public static GuiListener getInstance() {
        return INSTANCE;
    }

    // ===== registering stuff =====

    /**
     * Registers an inventory gui.
     *
     * @param holder the gui holder
     * @param inventory the inventory that holds the gui item stacks
     * @return true if the gui was registered successfully, otherwise false
     */
    public boolean registerGui(GuiInventoryHolder<?> holder, Inventory inventory) {
        if (holder == inventory.getHolder()) return true; //yes, reference equality

        return guiInventories.putIfAbsent(getBaseInventory(inventory), new WeakReference<>(holder)) == null;
    }

    /**
     * Substitute for {@link Inventory#getHolder()} for gui inventories.
     * @param inventory the inventory
     * @return the holder - or null if no holder was registered with the inventory.
     */
    public GuiInventoryHolder<?> getHolder(Inventory inventory) {
        // If the inventory's owner is a tile entity, don't call getHolder() in order to prevent snapshotting of the inventory during the BlockState calculation.
        // See: https://www.spigotmc.org/threads/why-items-with-lots-of-metadata-actually-cause-lag-an-inventoryholder-psa.607711/
        if (inventory.getLocation() != null) return null;

        InventoryHolder holder = inventory.getHolder();
        if (holder instanceof GuiInventoryHolder) return (GuiInventoryHolder<?>) holder;

        WeakReference<GuiInventoryHolder<?>> reference = guiInventories.get(getBaseInventory(inventory));
        if (reference == null) return null;

        return reference.get(); //can still be null
    }

    /**
     * Checks whether the Inventory is registered with the gui inventory holder.
     *
     * @param inventory the inventory that is maybe registered with the holder
     * @param holder the holder to check
     * @return whether the holder and inventory are registered
     */
    public boolean isGuiRegistered(GuiInventoryHolder<?> holder, Inventory inventory) {
        return getHolder(inventory) == holder; //yes, reference equality!
    }

    /**
     * Checks whether there is a gui registered with this inventory.
     *
     * @param inventory the inventory that is maybe registered
     * @return true if there is a {@link GuiInventoryHolder} registered for the given inventory
     */
    public boolean isGuiRegistered(Inventory inventory) {
       return getHolder(inventory) != null;
    }

    // We cannot ever make an unregisterGui method because to do that we would need to unset the GuiInventoryHolder from the Inventory.
    // So until such a method is added to bukkit's api, this is impossible to do without nms/reflection hacks.

    // ===== event stuff =====

    private void onGuiInventoryEvent(InventoryEvent event, Consumer<GuiInventoryHolder> action) {
        GuiInventoryHolder<?> guiHolder = getHolder(event.getInventory());

        if (guiHolder != null && guiHolder.getPlugin().isEnabled()) {
            action.accept(guiHolder);
        }
    }

    /**
     * Delegates the InventoryOpenEvent to the {@link GuiInventoryHolder} if the top inventory is held by a Gui and the event is not cancelled.
     * @param event the InventoryOpenEvent
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        onGuiInventoryEvent(event, gui -> gui.onOpen(event));
    }

    /**
     * Delegates the InventoryClickEvent to the {@link GuiInventoryHolder} if the top inventory is held by a Gui and the event is not cancelled.
     * InventoryClickEvents are cancelled before they are passed to the Gui.
     * @param event the InventoryClickEvent
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        onGuiInventoryEvent(event, gui -> {
            event.setCancelled(true);
            gui.onClick(event);
        });
    }

    /**
     * Delegates the InventoryDragEvent to the {@link GuiInventoryHolder} if the top inventory is held by a Gui and the event is not cancelled.
     * InventoryDragEvents are cancelled before they are passed to the Gui.
     * @param event the InventoryDragEvent
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        onGuiInventoryEvent(event, gui -> {
            event.setCancelled(true);
            gui.onDrag(event);
        });
    }

    /**
     * Delegates the InventoryCloseEvent to the {@link GuiInventoryHolder} if the top inventory is held by a Gui.
     * @param event InventoryCloseEvent
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClose(InventoryCloseEvent event) {
        onGuiInventoryEvent(event, gui -> gui.onClose(event));
    }

    // ===== nms stuff =====

    private static final Class<?> CRAFT_INVENTORY;
    private static final Method GET_INVENTORY;
    static {
        Class<?> craftInventoryClass = null;
        Method getInventoryMethod = null;
        Class<?> serverClass = Bukkit.getServer().getClass();
        if ("CraftServer".equals(serverClass.getSimpleName())) {
            String serverPackage = serverClass.getPackageName();
            String className = serverPackage + ".inventory.CraftInventory";
            try {
                craftInventoryClass = Class.forName(className);
                getInventoryMethod = craftInventoryClass.getMethod("getInventory");
            } catch (ClassNotFoundException | NoSuchMethodException ignored) {
            }
        }
        CRAFT_INVENTORY = craftInventoryClass;
        GET_INVENTORY = getInventoryMethod;
    }

    /**
     * If the inventory is a CraftInventory, get the NMS inventory. If not, just return the bukkit Inventory.
     * @param inventory the bukkit inventory
     * @return the authoritative inventory
     */
    //This works around a bug in CraftBukkit where CraftInventory instances cannot be used as WeakHashMap keys because CraftInventory instances are created on-demand.
    //They are not stored in the nms inventory, hence to obtain an object that persists the inventory, we need the NMS inventory: IInventory(mc-dev) Container(moj-map).
    private static Object getBaseInventory(Inventory inventory) {
        if (CRAFT_INVENTORY != null && GET_INVENTORY != null && CRAFT_INVENTORY.isInstance(inventory)) {
            try {
                return GET_INVENTORY.invoke(inventory);
            } catch (InvocationTargetException | IllegalAccessException ignored) {
            }
        }
        return inventory;
    }

}
