package xyz.janboerman.guilib;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import xyz.janboerman.guilib.api.GuiInventoryHolder;

/**
 * The listener that listens when GUIs are opened, clicked and closed. There should only ever be one instance registered.
 * <br> That means that, when you shade GuiLib into your plugin, you have to register it yourself in your onEnable.
 * <pre>
 *     <code>
 *     {@literal @}Override
 *     public void onEnable() {
 *         getServer().getPluginManager().registerEvents(new GuiListener(), this);
 *
 *         // more initialization stuff...
 *     }
 *     </code>
 * </pre>
 * If instead you decide to use GuiLib as a runtime dependency and put the jar in your plugins folder, GuiLib registers this listener itself.
 */
public class GuiListener implements Listener {

    /**
     * Listens to InventoryOpenEvents and delegates the event to the {@link GuiInventoryHolder} holding the inventory opened inventory, if any.
     * @param event the InventoryOpenEvent
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getView().getTopInventory() == null) return;
        if (event.getView().getTopInventory().getHolder() instanceof GuiInventoryHolder) {
            GuiInventoryHolder guiHolder = (GuiInventoryHolder) event.getView().getTopInventory().getHolder();
            if (guiHolder.getPlugin().isEnabled()) {
                guiHolder.onOpen(event);
            }
        }
    }

    /**
     * Delegates the InventoryClickEvent to the {@link GuiInventoryHolder} if the top inventory is held by a Gui.
     * @param event the InventoryClickEvent
     */
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getView().getTopInventory() == null) return;
        if (!(event.getView().getTopInventory().getHolder() instanceof GuiInventoryHolder)) return;

        GuiInventoryHolder guiHolder = (GuiInventoryHolder) event.getView().getTopInventory().getHolder();
        event.setCancelled(true);
        if (guiHolder.getPlugin().isEnabled()) {
            guiHolder.onClick(event);
        }
    }

    /**
     * Delegates the InventoryCloseEvent to the {@link GuiInventoryHolder} if the top inventory is held by a Gui.
     * @param event InventoryCloseEvent
     */
    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getView().getTopInventory() == null) return;
        if (!(event.getView().getTopInventory().getHolder() instanceof GuiInventoryHolder)) return;

        GuiInventoryHolder guiHolder = (GuiInventoryHolder) event.getView().getTopInventory().getHolder();
        if (guiHolder.getPlugin().isEnabled()) {
            guiHolder.onClose(event);
        }
    }

}
