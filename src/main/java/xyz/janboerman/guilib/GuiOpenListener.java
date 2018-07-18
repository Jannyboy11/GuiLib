package xyz.janboerman.guilib;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.plugin.Plugin;
import xyz.janboerman.guilib.api.GuiInventoryHolder;

import java.util.Objects;

/**
 * The listener that listens when Guis are opened. There should only ever be one instance registered.
 * <br> That means that, when you shade GuiLib into your plugin, you have to register it yourself in your onEnable.
 * <pre>
 *     <code>
 *     {@literal @}Override
 *     public void onEnable() {
 *         getServer().getPluginManager().registerEvents(new GuiOpenListener(this), this);
 *
 *         // more initialization stuff...
 *     }
 *     </code>
 * </pre>
 * If instead you decide to use GuiLib as a runtime dependency and put the jar in your plugins folder, GuiLib registers this listener itself.
 */
public class GuiOpenListener implements Listener {

    private final Plugin plugin;

    /**
     * Creates the GuiOpenListener
     * @param plugin your plugin
     */
    public GuiOpenListener(Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "Plugin cannot be null");
    }

    /**
     * Listens to InventoryOpenEvents and delegates the event to the {@link GuiInventoryHolder} holding the inventory opened inventory, if any.
     * @param event the InventoryOpenEvent
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getInventory().getHolder() instanceof GuiInventoryHolder) {
            GuiInventoryHolder guiHolder = (GuiInventoryHolder) event.getInventory().getHolder();
            plugin.getServer().getPluginManager().registerEvents(guiHolder.guiListener, plugin);
            guiHolder.onOpen(event);
        }
    }

}
