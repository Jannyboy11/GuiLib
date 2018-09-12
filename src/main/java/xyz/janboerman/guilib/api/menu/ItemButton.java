package xyz.janboerman.guilib.api.menu;

import org.bukkit.inventory.ItemStack;

/**
 * A button with an icon.
 *
 * @param <MH> the type of the menu
 */
public class ItemButton<MH extends MenuHolder<?>> implements MenuButton<MH> {

    /**
     * The representation of this button.
     */
    protected ItemStack stack;

    /**
     * Creates an ItemButton without an icon.
     */
    protected ItemButton() {
    }

    /**
     * Creates the ItemButton with the given ItemStack.
     * The button uses a clone of the ItemStack.
     * @param stack the icon
     */
    public ItemButton(ItemStack stack) {
        this.stack = stack == null ? null : stack.clone();
    }

    /**
     * Gets the icon.
     * @return a clone of the ItemStack that was provided in the constructor, or null if there is no icon.
     */
    @Override
    public final ItemStack getIcon() {
        return stack == null ? null : stack.clone();
    }
    
}
