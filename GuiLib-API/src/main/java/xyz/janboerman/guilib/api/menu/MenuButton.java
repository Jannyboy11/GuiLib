package xyz.janboerman.guilib.api.menu;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * A button that can sit in a {@link MenuHolder}.
 * @param <MH> the specific MenuHolder type
 */
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

    /**
     * Called when the button is added to the menu.
     *
     * @param menuHolder the menu
     * @param slot the position in the menu
     * @return whether the button could be added, true by default
     */
    public default boolean onAdd(MH menuHolder, int slot) {
        return true;
    }

    /**
     * Called when the button is removed from the menu.
     *
     * @param menuHolder the menu
     * @param slot the position in the menu
     * @return whether the button could be removed, true by default
     */
    public default boolean onRemove(MH menuHolder, int slot) {
        return true;
    }
    
}
