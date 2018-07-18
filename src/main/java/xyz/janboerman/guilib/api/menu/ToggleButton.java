package xyz.janboerman.guilib.api.menu;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

/**
 * A button that can be toggled.
 * The button can be in the enabled or disabled state.
 *
 * @param <MH> the menu holder type
 * @see #beforeToggle(MenuHolder, InventoryClickEvent)
 * @see #afterToggle(MenuHolder, InventoryClickEvent)
 */
public class ToggleButton<MH extends MenuHolder<?>> extends ItemButton<MH> {

    /** boolean that is set to false when the button is clicked. Becomes true again one tick after the button is clicked.*/
    private boolean canToggle = true;
    /** The toggle state. True = On, False = Off*/
    private boolean enabled;

    /**
     * Creates the toggle button with the given icon. The button is toggled off by default.
     * @param item the icon
     */
    public ToggleButton(ItemStack item) {
        this(item, false);
    }

    /**
     * Creates the toggle button with the given icon and toggle-state.
     * @param item the icon
     * @param enabled whether the icon is enabled from the start
     */
    public ToggleButton(ItemStack item, boolean enabled) {
        super(item);
        this.enabled = enabled;
    }

    /**
     * Toggles this button. Subclasses can add extra side-effects before and after toggling by overriding
     * {@link #beforeToggle(MenuHolder, InventoryClickEvent)} and {@link #afterToggle(MenuHolder, InventoryClickEvent)}.
     * @param holder the MenuHolder
     * @param event the InventoryClickEvent
     */
    @Override
    public final void onClick(MH holder, InventoryClickEvent event) {
        toggle(holder, event);
        event.setCurrentItem(this.stack = updateIcon(holder, event));
    }
    
    private void toggle(MH holder, InventoryClickEvent event) {
        if (!canToggle) return;
        if (!beforeToggle(holder, event)) return;
        this.enabled = !isEnabled();
        afterToggle(holder, event);
        canToggle = false;
        Plugin plugin = holder.getPlugin();
        plugin.getServer().getScheduler().runTask(plugin, () -> canToggle = true);
    }

    /**
     * Check whether the button can be toggled.
     * The default implementation always return true.
     * @param menuHolder the inventory holder for the menu
     * @param event the InventoryClickEvent that caused the button to toggle
     * @return true
     */
    public boolean beforeToggle(MH menuHolder, InventoryClickEvent event) {
        return true;
    }

    /**
     * Run a side-effect after the button is toggled.
     * The default implementation does nothing.
     * @param menuHolder the inventory holder for the menu
     * @param event the InventoryClickEvent that caused the button to toggle
     */
    public void afterToggle(MH menuHolder, InventoryClickEvent event) {
    }

    /**
     * Tests whether this button is toggled on or off.
     * @return true if the button is toggled on, otherwise false
     */
    public final boolean isEnabled() {
        return enabled;
    }

    /**
     * Determines what the icon should look like.
     * Implementations can override this method.
     * @param menuHolder the inventory holder for the menu
     * @param event the InventoryClickEvent that caused the button to toggle
     * @return the updated icon.
     */
    protected ItemStack updateIcon(MH menuHolder, InventoryClickEvent event) {
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
