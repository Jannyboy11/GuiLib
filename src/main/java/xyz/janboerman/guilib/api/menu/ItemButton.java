package xyz.janboerman.guilib.api.menu;

import org.bukkit.inventory.ItemStack;

/**
 * A Button with an icon.
 *
 * @param <MH> the type of the menu
 */
public class ItemButton<MH extends MenuHolder<?>> implements MenuButton<MH> {

    /**
     * The representation of this button.
     */
    protected ItemStack stack;

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
     * @return a clone of the ItemStack given in the constructor
     */
    @Override
    public final ItemStack getIcon() {
        return stack == null ? null : stack.clone();
    }
    
}
