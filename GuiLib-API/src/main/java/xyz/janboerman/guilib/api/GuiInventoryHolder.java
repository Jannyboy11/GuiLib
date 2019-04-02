package xyz.janboerman.guilib.api;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

/**
 * An InventoryHolder for GUIs.
 * <p>
 * This class is meant to be extended by your own plugin.
 * Just override the {@link #onClick(InventoryClickEvent)}, {@link #onOpen(InventoryOpenEvent)} or
 * {@link #onClose(InventoryCloseEvent)} methods. The InventoryClickEvent is set to be cancelled by default,
 * however you can un-cancel them in your subclass just fine using {@code event.setCancelled(false)}.
 * <p>
 * If you just need a menu with buttons, then {@link xyz.janboerman.guilib.api.menu.MenuHolder} is
 * a suitable candidate for your needs.
 *
 * @param <P> your Plugin type
 * @see xyz.janboerman.guilib.api.menu.MenuHolder
 */
public abstract class GuiInventoryHolder<P extends Plugin> implements InventoryHolder {
    
    private final Inventory inventory;
    private final P plugin;
    protected final GuiListener guiListener;

    /**
     * Constructs a new GuiInventoryHolder for your plugin with the given inventory type and title.
     * @param plugin your plugin
     * @param type the inventory type
     * @param title the title
     * {@link #onClick(InventoryClickEvent)} and {@link #onClose(InventoryCloseEvent)} methods
     *
     * @apiNote if you're shading GuiLib, be sure to register GuiListener's single instance for events.
     */
    public GuiInventoryHolder(P plugin, InventoryType type, String title) {
        this(GuiListener.getInstance(), plugin, type, title);
    }

    /**
     * Constructs a new GuiInventoryHolder for your plugin with the given inventory type and title.
     * @param plugin your plugin
     * @param type the inventory type
     * @param title the title
     * @param guiListener the listener that invokes the {@link #onOpen(InventoryOpenEvent)},
     * {@link #onClick(InventoryClickEvent)} and {@link #onClose(InventoryCloseEvent)} methods
     */
    public GuiInventoryHolder(GuiListener guiListener, P plugin, InventoryType type, String title) {
        this.guiListener = guiListener;
        this.plugin = plugin;
        this.inventory = plugin.getServer().createInventory(this, type, title); //implicit null check

        guiListener.registerGui(this, inventory); //implicit null check
    }

    /**
     * Constructs a new GuiInventoryHolder for your plugin with the given size and title.
     * @param plugin your plugin
     * @param title the title
     * @param size the chest size (should be a multiple of 9 and ranging from 9 to 54)
     * {@link #onClick(InventoryClickEvent)} and {@link #onClose(InventoryCloseEvent)} methods
     *
     * @apiNote if you're shading GuiLib, be sure to register GuiListener's single instance for events.
     */
    public GuiInventoryHolder(P plugin, int size, String title) {
        this(GuiListener.getInstance(), plugin, size, title);
    }

    /**
     * Constructs a new chest-GuiInventoryHolder for your plugin with the given size and title.
     * @param plugin your plugin
     * @param size the chest size (should be a multiple of 9 and ranging from 9 to 54)
     * @param title the title
     * @param guiListener the listener that invokes the {@link #onOpen(InventoryOpenEvent)},
     * {@link #onClick(InventoryClickEvent)} and {@link #onClose(InventoryCloseEvent)} methods
     */
    public GuiInventoryHolder(GuiListener guiListener, P plugin, int size, String title) {
        this.guiListener = guiListener;
        this.plugin = plugin;
        this.inventory = plugin.getServer().createInventory(this, size, title); //implicit null check

        guiListener.registerGui(this, inventory); //implicit null check
    }

    /**
     * Constructs a new GuiInventoryHolder for your plugin with the given inventory type.
     * @param plugin your plugin
     * @param type the inventory type
     * {@link #onClick(InventoryClickEvent)} and {@link #onClose(InventoryCloseEvent)} methods
     *
     * @apiNote if you're shading GuiLib, be sure to register GuiListener's single instance for events.
     */
    public GuiInventoryHolder(P plugin, InventoryType type) {
        this(GuiListener.getInstance(), plugin, type);
    }

    /**
     * Constructs a new GuiInventoryHolder for your plugin with the given inventory type.
     * @param plugin your plugin
     * @param type the inventory type
     * @param guiListener the listener that invokes the {@link #onOpen(InventoryOpenEvent)},
     * {@link #onClick(InventoryClickEvent)} and {@link #onClose(InventoryCloseEvent)} methods
     */
    public GuiInventoryHolder(GuiListener guiListener, P plugin, InventoryType type) {
        this.guiListener = guiListener;
        this.plugin = plugin;
        this.inventory = plugin.getServer().createInventory(this, type); //implicit null check

        guiListener.registerGui(this, inventory); //implicit null check
    }

    /**
     * Constructs a new chest-GuiInventoryHolder for your plugin with the given size.
     * @param plugin your plugin
     * @param size the chest size (should be a multiple of 9 and ranging from 9 to 54)
     * {@link #onClick(InventoryClickEvent)} and {@link #onClose(InventoryCloseEvent)} methods
     *
     * @apiNote if you're shading GuiLib, be sure to register GuiListener's single instance for events.
     */
    public GuiInventoryHolder(P plugin, int size) {
        this(GuiListener.getInstance(), plugin, size);
    }

    /**
     * Constructs a new chest-GuiInventoryHolder for your plugin with the given size.
     * @param plugin your plugin
     * @param size the chest size (should be a multiple of 9 and ranging from 9 to 54)
     * @param guiListener the listener that invokes the {@link #onOpen(InventoryOpenEvent)},
     * {@link #onClick(InventoryClickEvent)} and {@link #onClose(InventoryCloseEvent)} methods
     */
    public GuiInventoryHolder(GuiListener guiListener, P plugin, int size) {
        this.guiListener = guiListener;
        this.plugin = plugin;
        this.inventory = plugin.getServer().createInventory(this, size); //implicit null check

        guiListener.registerGui(this, inventory); //implicit null check
    }

    /**
     * Constructs a GuiInventoryHolder for your plugin using the given inventory.
     * This is especially useful when you are using OBC or NMS classes in your plugin and your inventory cannot be created
     * by {@link org.bukkit.Server#createInventory(InventoryHolder, InventoryType, String)} or any of its overloads.
     * One reason you might want to do this is to implement custom shift-click behaviour in your own Container implementation.
     * <p>
     * The InventoryHolder of the inventory given as the argument should be this new GuiInventoryHolder.
     * A code example:
     * <pre><code>
     * public class MyNMSInventory extends InventorySubContainer implements ITileEntityContainer {
     *
     *     private final MyBukkitWrapper bukkitInventory;
     *
     *     public MyNMSInventory(MyPlugin plugin, String title) {
     *         super(title, true, 6*9);
     *         this.bukkitInventory = new MyBukkitWrapper(this);
     *         this.bukkitOwner = new MyGuiInventoryHolder(plugin, this);
     *     }
     *
     *     {@literal @}Override
     *     public Container createContainer(PlayerInventory playerInventory, EntityHuman human) {
     *         EntityPlayer player = (EntityPlayer) human;
     *         return new MyContainer(player.nextContainerCounter(), player.getBukkitEntity(), bukkitInventory);
     *     }
     *
     *     {@literal @}Override
     *     public String getContainerName() {
     *          return "minecraft:container";
     *     }
     * }
     * </code></pre>
     *
     * @param plugin your plugin
     * @param inventory the custom inventory
     * {@link #onClick(InventoryClickEvent)} and {@link #onClose(InventoryCloseEvent)} methods
     *
     * @apiNote if you're shading GuiLib, be sure to register GuiListener's single instance for events.
     */
    public GuiInventoryHolder(P plugin, Inventory inventory) {
        this(GuiListener.getInstance(), plugin, inventory);
    }

    /**
     * Constructs a GuiInventoryHolder for your plugin using the given inventory.
     * This is especially useful when you are using OBC or NMS classes in your plugin and your inventory cannot be created
     * by {@link org.bukkit.Server#createInventory(InventoryHolder, InventoryType, String)} or any of its overloads.
     * One reason you might want to do this is to implement custom shift-click behaviour in your own Container implementation.
     * <p>
     * The InventoryHolder of the inventory given as the argument should be this new GuiInventoryHolder.
     * A code example:
     * <pre><code>
     * public class MyNMSInventory extends InventorySubContainer implements ITileEntityContainer {
     *
     *     private final MyBukkitWrapper bukkitInventory;
     *
     *     public MyNMSInventory(MyPlugin plugin, String title) {
     *         super(title, true, 6*9);
     *         this.bukkitInventory = new MyBukkitWrapper(this);
     *         this.bukkitOwner = new MyGuiInventoryHolder(plugin, this);
     *     }
     *
     *     {@literal @}Override
     *     public Container createContainer(PlayerInventory playerInventory, EntityHuman human) {
     *         EntityPlayer player = (EntityPlayer) human;
     *         return new MyContainer(player.nextContainerCounter(), player.getBukkitEntity(), bukkitInventory);
     *     }
     *
     *     {@literal @}Override
     *     public String getContainerName() {
     *          return "minecraft:container";
     *     }
     * }
     * </code></pre>
     *
     * @param plugin your plugin
     * @param inventory the custom inventory
     * @param guiListener the listener that invokes the {@link #onOpen(InventoryOpenEvent)},
     * {@link #onClick(InventoryClickEvent)} and {@link #onClose(InventoryCloseEvent)} methods
     */
    public GuiInventoryHolder(GuiListener guiListener, P plugin, Inventory inventory) {
        this.guiListener = guiListener;
        this.plugin = Objects.requireNonNull(plugin, "Plugin cannot be null");
        this.inventory = Objects.requireNonNull(inventory, "Inventory cannot be null");

        guiListener.registerGui(this, inventory); //implicit null check
    }

    /**
     * Get the inventory associated with this gui holder.
     * Subclasses that override this method should always return
     * {@code super.getInventory();} with a cast to a custom inventory type.
     * @return the inventory
     */
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Gets the plugin associated with this gui holder.
     * @return the plugin
     */
    public final P getPlugin() {
        return plugin;
    }

    /**
     * Called when the corresponding InventoryView closes.
     * <p>
     * This method is intended to be overridden by your subclass.
     *
     * @param event the inventory close event
     */
    public void onClose(InventoryCloseEvent event) {
    }

    /**
     * Called when the corresponding InventoryView is clicked.
     * This method makes no guarantees about which inventory was clicked,
     * or whether an inventory was clicked at all.
     * <p>
     * By default, the event is cancelled.
     * <p>
     * This method is intended to be overridden by your subclass.
     * @param event the inventory click event
     */
    public void onClick(InventoryClickEvent event) {
    }

    /**
     * Called when the corresponding InventoryView opens.
     * <p>
     * This method is intended to be overridden by your subclass.
     * @param event the inventory open event.
     */
    public void onOpen(InventoryOpenEvent event) {
    }

    /**
     * Called when items are dragged in the corresponding InventoryView.
     * This method makes no guarantees about which inventory items were dragged into.
     * @param event
     */
    public void onDrag(InventoryDragEvent event) {
    }

    /**
     * Get the inventory that was clicked in the event.
     * @param event the InventoryClickEvent
     * @return the inventory that was clicked, or null if the player clicked outside the inventory
     */
    protected static Inventory getClickedInventory(InventoryClickEvent event) {
        //Adopted from the spigot-api patches
        //See https://hub.spigotmc.org/stash/projects/SPIGOT/repos/spigot/browse/Bukkit-Patches/0010-InventoryClickEvent-getClickedInventory.patch
        int slot = event.getRawSlot();
        if (slot < 0) {
            return null;
        } else {
            InventoryView view = event.getView();
            Inventory topInventory = view.getTopInventory();
            //apparently it is possible that the top inventory is null.
            //does this happen when a player opens his/her own inventory?
            if (topInventory != null && slot < topInventory.getSize()) {
                return topInventory;
            } else {
                return view.getBottomInventory();
            }
        }
    }
    
}
