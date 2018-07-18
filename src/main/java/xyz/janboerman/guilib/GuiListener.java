package xyz.janboerman.guilib;

import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.Plugin;
import xyz.janboerman.guilib.api.GuiInventoryHolder;

import java.util.Objects;

/**
 * Listener dat listens to InventoryClickEvents and InventoryCloseEvents for inventories held by an {@link GuiInventoryHolder}.
 * Library users shouldn't create instances of this class as instances are created in {@link GuiInventoryHolder}'s constructors.
 */
public class GuiListener<P extends Plugin> implements Listener {
    
    private final GuiInventoryHolder<P> guiInventoryHolder;

    /**
     * Creates the GuiListener.
     * @param guiInventoryHolder the Gui that is holding the inventory.
     */
    public GuiListener(GuiInventoryHolder<P> guiInventoryHolder) {
        this.guiInventoryHolder = Objects.requireNonNull(guiInventoryHolder);
    }

    /**
     * Delegates the InventoryClickEvent to the Gui if the top inventory is held by the Gui.
     * @param event the InventoryClickEvent
     */
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getView().getTopInventory() == null) return;
        if (event.getView().getTopInventory().getHolder() != guiInventoryHolder) return;
         
        event.setCancelled(true);
        guiInventoryHolder.onClick(event);
    }

    /**
     * Delegates the InventoryCloseEvent to the Gui if the top inventory is held by the Gui.
     * @param event InventoryCloseEvent
     */
    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getView().getTopInventory() == null) return;
        if (event.getView().getTopInventory().getHolder() != guiInventoryHolder) return;
        
        guiInventoryHolder.onClose(event);
        HandlerList.unregisterAll(this);
    }

}
