package xyz.janboerman.guilib.api.menu;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Represents a button that - when clicked transfers the item stack to the player's inventory.
 */
public class ClaimButton<MH extends MenuHolder<?>> extends ItemButton<MH> {

    private final PlayerInventoryFullCallback<MH> inventoryFullCallback;
    private final SuccessFulTransferCallback<MH> successFulTransferCallback;
    private final boolean copy;

    /**
     * Creates the ClaimButton.
     * @param item the item that can be claimed by the player
     */
    public ClaimButton(ItemStack item) {
        this(item, false, null, null);
    }

    /**
     * Creates the ClaimButton.
     * @param item the item that can be claimed by the player
     * @param copy whether a copy of the item should remain in the top inventory
     */
    public ClaimButton(ItemStack item, boolean copy) {
        this(item, copy, null, null);
    }

    /**
     * Creates the ClaimButton.
     * @param item the item that can be claimed by the player
     * @param inventoryFullCallback the callback that is invoked when the item couldn't be moved into the player's inventory - can be null
     */
    public ClaimButton(ItemStack item, PlayerInventoryFullCallback<MH> inventoryFullCallback) {
        this(item, false, inventoryFullCallback, null);
    }

    /**
     * Creates the ClaimButton.
     * @param copy whether a copy of the item should remain in the top inventory
     * @param item the item that can be claimed by the player
     * @param inventoryFullCallback the callback that is invoked when the item couldn't be moved into the player's inventory - can be null
     */
    public ClaimButton(ItemStack item, boolean copy, PlayerInventoryFullCallback<MH> inventoryFullCallback) {
        this(item, copy, inventoryFullCallback, null);
    }

    /**
     * Creates the ClaimButton.
     * @param item the item that can be claimed by the player
     * @param successFulTransferCallback the callback that is invoked after the items was transferred successfully - can be null
     */
    public ClaimButton(ItemStack item, SuccessFulTransferCallback<MH> successFulTransferCallback) {
        this(item, false, null, successFulTransferCallback);
    }

    /**
     * Creates the ClaimButton.
     * @param item the item that can be claimed by the player
     * @param copy whether a copy of the item should remain in the top inventory
     * @param successFulTransferCallback the callback that is invoked after the items was transferred successfully - can be null
     */
    public ClaimButton(ItemStack item, boolean copy, SuccessFulTransferCallback<MH> successFulTransferCallback) {
        this(item, copy, null, successFulTransferCallback);
    }

    /**
     * Creates the ClaimButton.
     * @param item the item that can be claimed by the player
     * @param inventoryFullCallback the callback that is invoked when the item couldn't be moved into the player's inventory - can be null
     * @param successFulTransferCallback the callback that is invoked after the items was transferred successfully - can be null
     */
    public ClaimButton(ItemStack item, PlayerInventoryFullCallback<MH> inventoryFullCallback, SuccessFulTransferCallback<MH> successFulTransferCallback) {
        this(item, false, inventoryFullCallback, successFulTransferCallback);
    }

    /**
     * Creates the ClaimButton.
     * @param item the item that can be claimed by the player
     * @param copy whether a copy of the item should remain in the top inventory
     * @param inventoryFullCallback the callback that is invoked when the item couldn't be moved into the player's inventory - can be null
     * @param successFulTransferCallback the callback that is invoked after the items was transferred successfully - can be null
     */
    public ClaimButton(ItemStack item, boolean copy, PlayerInventoryFullCallback<MH> inventoryFullCallback, SuccessFulTransferCallback<MH> successFulTransferCallback) {
        super(item);
        this.copy = copy;
        this.inventoryFullCallback = inventoryFullCallback;
        this.successFulTransferCallback = successFulTransferCallback;
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

        boolean success = clickedItem == null || player.getInventory().addItem(clickedItem).isEmpty();
        if (success) {
            if (!copy) {
                event.setCurrentItem(null);
                menuHolder.unsetButton(event.getSlot());
            }
            getSuccessFulTransferCallback().ifPresent((Consumer<SuccessFulTransferCallback<MH>>) callback -> callback.afterTransfer(menuHolder, event, clickedItem));
        } else {
            getInventoryFullCallback().ifPresent((Consumer<PlayerInventoryFullCallback<MH>>) callback -> callback.onPlayerInventoryFull(menuHolder, event));
        }
    }

    /**
     * Get the callback that is invoked when the item cannot be moved into the player's inventory.
     * @return the Optional containing callback, or the empty Optional when no callback is present
     */
    public Optional<? extends PlayerInventoryFullCallback<MH>> getInventoryFullCallback() {
        return Optional.ofNullable(inventoryFullCallback);
    }

    /**
     * Get the callback that is invoked after the item is successfully transferred to the player's inventory.
     * @return the Optional containing callback, or the empty Optional when no callback is present
     */
    public Optional<? extends SuccessFulTransferCallback<MH>> getSuccessFulTransferCallback() {
        return Optional.ofNullable(successFulTransferCallback);
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

    /**
     * A callback that can be used to run an additional action after the ItemStack was transferred successfully.
     * @param <MH> the menu holder type
     */
    @FunctionalInterface
    public static interface SuccessFulTransferCallback<MH extends MenuHolder<?>> {

        /**
         * Functional method that is executed after the button is clicked and the ItemStack is transferred to the player's inventory.
         * @param menuHolder the menu holder
         * @param event the inventory click event
         * @param reward the reward that was transferred to the player's inventory
         */
        public void afterTransfer(MH menuHolder, InventoryClickEvent event, ItemStack reward);

    }

}
