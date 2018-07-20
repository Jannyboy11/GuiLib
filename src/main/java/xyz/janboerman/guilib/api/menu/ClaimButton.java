package xyz.janboerman.guilib.api.menu;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiConsumer;

/**
 * Represents a button that - when clicked transfers the item stack to the player's inventory.
 */
public class ClaimButton<MH extends MenuHolder<?>> extends ItemButton<MH> {

    protected final PlayerInventoryFullCallback<MH> inventoryFullCallback;

    /**
     * Creates the ClaimButton.
     * @param item the item that can be claimed by the player
     */
    public ClaimButton(ItemStack item) {
        this(item, null);
    }

    /**
     * Creates the ClaimButton.
     * @param item the item that can be claimed by the player
     * @param inventoryFullCallback the callback that is invoked when the item couldn't be moved into the player's inventory
     */
    public ClaimButton(ItemStack item, PlayerInventoryFullCallback<MH> inventoryFullCallback) {
        super(item);
        this.inventoryFullCallback = inventoryFullCallback;
    }

    /**
     * Tries to move the item to the player's inventory. If the item is transferred successfully the button is removed from the menu.
     * @param menuHolder the menu that contains this button
     * @param event the click event
     */
    @Override
    public void onClick(MH menuHolder, InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();
        HumanEntity player = event.getWhoClicked();

        boolean success = player.getInventory().addItem(clickedItem).isEmpty();
        if (success) {
            event.setCurrentItem(null);
            menuHolder.unsetButton(event.getSlot());
        } else {
            var inventoryFullCallback = getInventoryFullCallback();
            if (inventoryFullCallback != null) {
                inventoryFullCallback.onPlayerInventoryFull(menuHolder, event);
            }
        }
    }

    /**
     * Get the callback that is invoked when the item cannot be moved into the player's inventory.
     * @return the callback
     */
    public PlayerInventoryFullCallback<MH> getInventoryFullCallback() {
        return inventoryFullCallback;
    }

    /**
     * A callback that can be used to take action when the player's inventory cannot receive the item of the {@linkplain ClaimButton}.
     * @param <MH> the menu holder type
     */
    @FunctionalInterface
    public static interface PlayerInventoryFullCallback<MH extends MenuHolder<?>> extends BiConsumer<MH, InventoryClickEvent> {

        /**
         * Functional method that is executed when the the button is clicked but the item cannot move to the player's inventory because it's full.
         * @param menuHolder the menu holder
         * @param event the click event
         */
        public void onPlayerInventoryFull(MH menuHolder, InventoryClickEvent event);

        /**
         * Convenience method to make {@linkplain PlayerInventoryFullCallback} work in places where a {@linkplain BiConsumer} is required.
         * The default implementation delegates to {@link #onPlayerInventoryFull(MenuHolder, InventoryClickEvent)}.
         * @param menuHolder the menu holder
         * @param event the inventory click event
         */
        public default void accept(MH menuHolder, InventoryClickEvent event) {
            onPlayerInventoryFull(menuHolder, event);
        }
    }

}
