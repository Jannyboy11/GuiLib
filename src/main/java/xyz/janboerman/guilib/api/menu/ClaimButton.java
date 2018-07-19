package xyz.janboerman.guilib.api.menu;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a button that - when clicked transfers the item stack to the player's inventory.
 */
public class ClaimButton extends ItemButton<MenuHolder<?>> {

    /**
     * Creates the ClaimButton.
     * @param item the item that can be claimed by the player
     */
    public ClaimButton(ItemStack item) {
        super(item);
    }

    /**
     * Tries to move the item to the player's inventory. If the item is transferred successfully the button is removed from the menu.
     * @param menuHolder the menu that contains this button
     * @param event the InventoryClickEvent
     */
    @Override
    public void onClick(MenuHolder<?> menuHolder, InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();
        HumanEntity player = event.getWhoClicked();

        boolean success = player.getInventory().addItem(clickedItem).isEmpty();
        if (success) {
            event.setCurrentItem(null);
            menuHolder.unsetButton(event.getSlot());
        }
    }

}
