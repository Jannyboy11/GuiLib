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
        super(plugin.getGuiListener(), plugin, new ItemPage(plugin, pageSize, rewards, rewardStartIndex, rewardEndIndex), "Claim your items", null, null);
        getPage().enclosingMenu = this;
    }

    @Override
    public ItemPage getPage() {
        //we know the GuiInventoryHolder of the page is always a MenuHolder since we always create it ourselves
        return (ItemPage) super.getPage();
    }

    @Override
    protected boolean needsRedirects() {
        return false;
    }

    private static class ItemPage extends MenuHolder<ExamplePlugin> {
        private final int rewardStartIndex, rewardEndIndex;
        private final List<ItemStack> rewards;
        private ClaimItemsMenu enclosingMenu;

        private ItemPage(ExamplePlugin plugin, int pageSize, List<ItemStack> rewards, int rewardStartIndex, int rewardEndIndex) {
            super(plugin, pageSize);
            this.rewardStartIndex = rewardStartIndex;
            this.rewardEndIndex = rewardEndIndex;
            this.rewards = rewards;
        }

        @Override
        public void onOpen(InventoryOpenEvent event) {
            //setup rewards
            for (int slot = 0; slot < getInventory().getSize() && rewardStartIndex + slot < rewardEndIndex; slot++) {
                setButton(slot, new ShiftingClaimButton(rewards.get(rewardStartIndex + slot)));
            }
        }

        @Override
        public void onClose(InventoryCloseEvent event) {
            //help gc
            clearButtons();
        }

        //shifts all buttons in the page after the buttons that was transferred
        //actually creates new buttons
        private void shiftButtons(int slotIndex) {

            int listIndex = rewardStartIndex + slotIndex;
            rewards.remove(listIndex);

            while (slotIndex < getInventory().getSize()) {
                if (listIndex < rewards.size()) {
                    ItemStack reward = rewards.get(listIndex);
                    setButton(slotIndex, new ShiftingClaimButton(reward));
                } else {
                    unsetButton(slotIndex);
                }

                slotIndex++;
                listIndex++;
            }

            enclosingMenu.getHostingPage().resetButtons(); //removes the next-page button if there are no items after the current page
        }
    }

    private static class ShiftingClaimButton extends ClaimButton<ItemPage> {
        public ShiftingClaimButton(ItemStack reward) {
            super(reward, (page, event, itemStack) -> page.shiftButtons(event.getSlot()));
        }
    }

    @Override
    public Optional<Supplier<ClaimItemsMenu>> getNextPageMenu() {
        //there is a next page if the current range upper bound is smaller than the end of the list
        ItemPage itemPage = getPage();
        if (itemPage.rewardEndIndex < itemPage.rewards.size()) {
            return Optional.of(() -> new ClaimItemsMenu(
                    getPlugin(),
                    getPageSize(),
                    itemPage.rewards,
                    itemPage.rewardEndIndex,
                    Math.min(itemPage.rewards.size(), itemPage.rewardEndIndex + getPageSize())));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Supplier<ClaimItemsMenu>> getPreviousPageMenu() {
        //there is a previous page if we didn't start at 0
        ItemPage itemPage = getPage();
        if (itemPage.rewardStartIndex > 0) {
            return Optional.of(() -> new ClaimItemsMenu(
                    getPlugin(),
                    getPageSize(),
                    itemPage.rewards,
                    Math.max(0, itemPage.rewardStartIndex - getPageSize()), Math.min(itemPage.rewardStartIndex, itemPage.rewards.size())));
        } else {
            return Optional.empty();
        }
    }

}