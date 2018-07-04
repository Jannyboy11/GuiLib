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
 * When the button is enabled, the icon is enchanted.
 */
public class ToggleButton extends ItemButton {

    private boolean canToggle = true; 
    private boolean enabled;

    /**
     * Creates the toggle button with the given icon.
     * @param item the icon
     */
    public ToggleButton(ItemStack item) {
        this(item, false);
    }

    /**
     * Creates the toggle button with the given icon and enabled-state.
     * @param item the icon
     * @param enabled whether the icon is enabled from the start
     */
    public ToggleButton(ItemStack item, boolean enabled) {
        super(item);
        this.enabled = enabled;
        
        updateIcon();
    }

    /**
     * Toggles this button. Subclasses can add extra side-effects before and after toggling by overriding
     * {@link #beforeToggle()} and {@link #afterToggle()}.
     * @param holder the MenuHolder
     * @param event the InventoryClickEvent
     */
    @Override
    public final void onClick(MenuHolder holder, InventoryClickEvent event) {
        toggle(holder.getPlugin());
        holder.getInventory().setItem(event.getSlot(), this.stack);
    }
    
    private void toggle(Plugin plugin) {
        if (!canToggle) return;
        if (!beforeToggle()) return;
        this.enabled = !isEnabled();
        updateIcon();
        afterToggle();
        canToggle = false;
        plugin.getServer().getScheduler().runTask(plugin, () -> canToggle = true);
    }

    /**
     * Check whether the button can be toggled.
     * The default implementation always return true
     * @return true
     */
    public boolean beforeToggle() {
        return true;
    }

    /**
     * Run a side-effect after the button is toggled.
     * The default implementation does nothing.
     */
    public void afterToggle() {
    }
    
    public final boolean isEnabled() {
        return enabled;
    }

    private void updateIcon() {
        this.stack = isEnabled() ? enable(getIcon()) : disable(getIcon());
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
