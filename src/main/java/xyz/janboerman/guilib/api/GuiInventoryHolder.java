package xyz.janboerman.guilib.api;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;
import xyz.janboerman.guilib.GuiListener;

/**
 * An InventoryHolder for GUIs.
 * <p>
 * This class is meant to be extended by your own plugin.
 * Just override the {@link #onClick(InventoryClickEvent)}, {@link #onOpen(InventoryOpenEvent)} or
 * {@link #onClose(InventoryCloseEvent)} methods. The events are cancelled by default,
 * however you can un-cancel them in your subclass just fine.
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
    /** @deprecated INTERNAL USE ONLY! */
    @Deprecated
    public final GuiListener<P> guiListener;

    /**
     * Constructs a new GuiInventoryHolder for your plugin with the given inventory type and title.
     * @param plugin your plugin
     * @param type the inventory type
     * @param title the title
     */
    public GuiInventoryHolder(P plugin, InventoryType type, String title) {
        this.plugin = plugin;
        this.guiListener = new GuiListener<>(this);
        this.inventory = plugin.getServer().createInventory(this, type, title); //implicit null check
        
        plugin.getServer().getPluginManager().registerEvents(guiListener, plugin);
    }

    /**
     * Contructs a new chest-GuiInventoryHolder for your plugin with the given size and title.
     * @param plugin your plugin
     * @param size the chest size (should be a multiple of 9 and ranging from 9 to 54)
     * @param title the title
     */
    public GuiInventoryHolder(P plugin, int size, String title) {
        this.plugin = plugin;
        this.guiListener = new GuiListener<>(this);
        this.inventory = plugin.getServer().createInventory(this, size, title); //implicit null check
        
        plugin.getServer().getPluginManager().registerEvents(guiListener, plugin);
    }

    /**
     * Constructs a new GuiInventoryHolder for your plugin with the given inventory type.
     * @param plugin your plugin
     * @param type the inventory type
     */
    public GuiInventoryHolder(P plugin, InventoryType type) {
        this.plugin = plugin;
        this.guiListener = new GuiListener<>(this);
        this.inventory = plugin.getServer().createInventory(this, type);

        plugin.getServer().getPluginManager().registerEvents(guiListener, plugin);
    }

    /**
     * Contructs a new chest-GuiInventoryHolder for your plugin with the given size.
     * @param plugin your plugin
     * @param size the chest size (should be a multiple of 9 and ranging from 9 to 54)
     */
    public GuiInventoryHolder(P plugin, int size) {
        this.plugin = plugin;
        this.guiListener = new GuiListener<>(this);
        this.inventory = plugin.getServer().createInventory(this, size); //implicit null check

        plugin.getServer().getPluginManager().registerEvents(guiListener, plugin);
    }

    /**
     * Contructs a GuiInventoryHolder for your plugin using the given inventory.
     * This is especially usefull when you are using OBC or NMS classes in your plugin and your inventory cannot be created
     * by {@link org.bukkit.Server#createInventory(InventoryHolder, InventoryType, String)} or any of its overloads.
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
     * @throws IllegalArgumentException if the holder if the inventory is not this new GuiInventoryHolder.
     */
    public GuiInventoryHolder(P plugin, Inventory inventory) {
        this.plugin = plugin;
        this.inventory = inventory;
        this.guiListener = new GuiListener<>(this);

        if(getInventory().getHolder() != this) {
            throw new IllegalArgumentException("InventoryHolder returned by inventory.getHolder() should be this new InventoryHolder.");
        }
    }

    /**
     * Get the inventory associated with this gui holder.
     * @return the inventory
     */
    @Override
    public final Inventory getInventory() {
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
    
}
