package xyz.janboerman.guilib.api.menu;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import xyz.janboerman.guilib.api.ItemBuilder;

import java.util.function.Supplier;

/**
 * A button that redirects back to the inventory supplied by the supplier given in the constructor.
 * The material of the icon is wooden door.
 */
public class BackButton extends RedirectItemButton<MenuHolder<?>> {

    private static final ItemStack BACK_BUTTON = new ItemBuilder(Material.IRON_DOOR).name("Back").build();

    /**
     * Creates a BackButton that redirects to the inventory supplied by the supplier
     * @param to the inventory supplier
     */
    public BackButton(Supplier<? extends Inventory> to) {
        super(BACK_BUTTON, to);
    }

    /**
     * Creates a BackButton that redirects to the inventory supplied by the supplier.
     * @param name the display name of the icon
     * @param to the inventory supplier
     */
    public BackButton(String name, Supplier<? extends Inventory> to) {
        super(new ItemBuilder(Material.OAK_DOOR).name(name).build(), to);
    }

}
