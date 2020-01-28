package com.example;

import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import xyz.janboerman.guilib.api.menu.ClaimButton;
import xyz.janboerman.guilib.api.menu.MenuHolder;
import xyz.janboerman.guilib.api.menu.PageMenu;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

//more advanced usage - implements a custom page menu
public class ClaimItemsMenu extends PageMenu<ExamplePlugin> {

    /** rewards list */
    private List<ItemStack> rewards;
    /** list indices */
    private int rewardStartIndex /*inclusive*/, rewardEndIndex /*exclusive*/;

    /**
     * Creates the ClaimItemsMenu
     * @param plugin the plugin
     * @param pageSize the size of the embedded page (9 - 45)
     * @param rewards a mutable list of reward items
     */
    public ClaimItemsMenu(ExamplePlugin plugin, int pageSize, List<ItemStack> rewards) {
        this(plugin, pageSize, rewards, 0, Math.min(rewards.size(), pageSize));
    }

    /**
     * Creates the ClaimItemsMenu
     * @param plugin the plugin
     * @param pageSize the size of the embedded page (9 - 45)
     * @param rewards a mutable list of reward items
     * @param rewardStartIndex the lowerbound of the sublist we are displaying (inclusive)
     * @param rewardEndIndex the upperbound of the sublist we are displaying (exclusive)
     */
    private ClaimItemsMenu(ExamplePlugin plugin, int pageSize, List<ItemStack> rewards, int rewardStartIndex, int rewardEndIndex) {
        super(plugin.getGuiListener(), plugin, new MenuHolder<>(plugin, pageSize), "Claim your items", null, null);
        this.rewards = rewards;
        this.rewardStartIndex = rewardStartIndex;
        this.rewardEndIndex = rewardEndIndex;
    }

    @Override
    public MenuHolder<ExamplePlugin> getPage() {
        //we know the GuiInventoryHolder of the page is always a MenuHolder since we always create it ourselves
        return (MenuHolder<ExamplePlugin>) super.getPage();
    }

    //shifts all buttons in the page after the buttons that was transferred
    //actually creates new buttons
    private void shiftButtons(int slotIndex) {
        var page = getPage();

        int listIndex = rewardStartIndex + slotIndex;
        rewards.remove(listIndex);

        while (slotIndex < page.getInventory().getSize()) {
            if (listIndex < rewards.size()) {
                ItemStack reward = rewards.get(listIndex);
                page.setButton(slotIndex, new ShiftingClaimButton(reward));
            } else {
                page.unsetButton(slotIndex);
            }

            slotIndex++;
            listIndex++;
        }

        resetButtons(); //removes the next-page button if there are no items after the current page
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        //setup rewards
        for (int slot = 0; slot < getPageSize() && rewardStartIndex + slot < rewardEndIndex; slot++) {
            getPage().setButton(slot, new ShiftingClaimButton(rewards.get(rewardStartIndex + slot)));
        }

        //required for the page to even work
        super.onOpen(event);
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        getPage().clearButtons(); //help gc

        //required
        super.onClose(event);
    }

    @Override
    public Optional<Supplier<ClaimItemsMenu>> getNextPageMenu() {
        //there is a next page if the current range upper bound is smaller than the end of the list
        if (rewardEndIndex < rewards.size()) {
            return Optional.of(() -> new ClaimItemsMenu(getPlugin(), getPageSize(), rewards, rewardEndIndex, Math.min(rewards.size(), rewardEndIndex + getPageSize())));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Supplier<ClaimItemsMenu>> getPreviousPageMenu() {
        //there is a previous page if we didn't start at 0
        if (rewardStartIndex > 0) {
            return Optional.of(() -> new ClaimItemsMenu(getPlugin(), getPageSize(), rewards, Math.max(0, rewardStartIndex - getPageSize()), Math.min(rewardStartIndex, rewards.size())));
        } else {
            return Optional.empty();
        }
    }

    public class ShiftingClaimButton extends ClaimButton<MenuHolder<ExamplePlugin>> {
        public ShiftingClaimButton(ItemStack reward) {
            super(reward, (page, event, itemStack) -> ClaimItemsMenu.this.shiftButtons(event.getSlot()));
        }
    }
}