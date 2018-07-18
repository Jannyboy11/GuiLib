package xyz.janboerman.guilib.api.menu;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public interface MenuButton<MH extends MenuHolder<?>> {

    /**
     * Callback that is called when this button is clicked.
     * <p>
     * The default implementation does nothing.
     *
     * @param holder the MenuHolder
     * @param event the InventoryClickEvent
     */
    public default void onClick(MH holder, InventoryClickEvent event) {
    }

    /**
     * The icon of the button.
     * <p>
     * The default implementation returns null.
     *
     * @return the icon
     */
    public default ItemStack getIcon() {
        return null;
    }
    
}
