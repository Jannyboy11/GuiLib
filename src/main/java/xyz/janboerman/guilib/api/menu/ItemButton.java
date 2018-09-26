package xyz.janboerman.guilib.api.menu;

import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * A button with an icon.
 *
 * @param <MH> the type of the menu
 */
public class ItemButton<MH extends MenuHolder<?>> implements MenuButton<MH> {

    /**
     * The representation of this button.
     * Buttons that wish to update their inventories should use {@link #setIcon(ItemStack)} intead.
     */
    protected ItemStack stack;

    private final WeakHashMap<MH, Set<Integer>> inventoriesContainingMe = new WeakHashMap<>();

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

    /**
     * Set the icon stack. Menus that contain this button will have their inventories update accordingly.
     * @param icon the icon
     */
    public final void setIcon(ItemStack icon) {
        stack = icon == null ? null : icon.clone();
        inventoriesContainingMe.forEach((menuHolder, slots) -> slots.forEach(slot -> menuHolder.getInventory().setItem(slot, stack)));
    }

    /**
     * Called when this button is added to the menu.
     * The ItemButton maintains a cache of menus that it is contained in.
     * When the icon is updated though {@link #setIcon(ItemStack)}, it updates the item stack in those inventories.
     *
     * @param menuHolder the menu
     * @param slot the position in the menu
     * @return whether the button can be added to the menu
     */
    @Override
    public final boolean onAdd(MH menuHolder, int slot) {
        return inventoriesContainingMe.computeIfAbsent(menuHolder, mh -> new HashSet<>()).add(slot);
    }

    /**
     * Removes the menu from the cache of menus that it is contained in.
     *
     * @param menuHolder the menu from which this button is removed
     * @param slot the position in the menu
     * @return whether the button can be removed from the menu
     */
    @Override
    public final boolean onRemove(MH menuHolder, int slot) {
        Set<Integer> slots = inventoriesContainingMe.get(menuHolder);
        if (slots != null) {
            boolean result = slots.remove(slot);
            if (slots.isEmpty()) inventoriesContainingMe.remove(menuHolder);
            return result;
        }
        return true;
    }
}
