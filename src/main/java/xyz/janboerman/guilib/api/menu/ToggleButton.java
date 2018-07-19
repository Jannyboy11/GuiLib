package xyz.janboerman.guilib.api.menu;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * A button that can be toggled.
 * The button can be in the enabled or disabled state.
 *
 * @param <MH> the menu holder type
 * @see #beforeToggle(MenuHolder, InventoryClickEvent)
 * @see #afterToggle(MenuHolder, InventoryClickEvent)
 * @see CycleButton
 */
public class ToggleButton<MH extends MenuHolder<?>> extends CycleButton<Boolean, MH> {

    /**
     * Creates the toggle button with the given icon. The button is toggled off by default.
     * @param icon the icon
     */
    public ToggleButton(ItemStack icon) {
        this(icon, false);
    }

    /**
     * Creates the toggle button with the given icon and toggle-state.
     * @param icon the icon
     * @param enabled whether the icon is enabled from the start
     */
    public ToggleButton(ItemStack icon, boolean enabled) {
        super(icon, new Boolean[]{false, true}, enabled ? 1 : 0, false);
    }

    public final boolean isEnabled() {
        return getCurrentState();
    }

    /**
     * Determines what the icon should look like.
     * Implementations can override this method.
     * @param menuHolder the inventory holder for the menu
     * @param event the InventoryClickEvent that caused the button to toggle
     * @return the updated icon.
     */
    @Override
    public ItemStack updateIcon(MH menuHolder, InventoryClickEvent event) {
        return isEnabled() ? enable(getIcon()) : disable(getIcon());
    }

    private static ItemStack enable(ItemStack stack) {
        if (stack == null) return null;
        
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return stack;

        stack.addUnsafeEnchantment(Enchantment.DURABILITY, 3);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        stack.setItemMeta(meta);
        
        return stack;
    }
    
    private static ItemStack disable(ItemStack stack) {
        if (stack == null) return null;
        
        stack.getEnchantments().forEach((ench, level) -> stack.removeEnchantment(ench));
        
        return stack;
    }

}
