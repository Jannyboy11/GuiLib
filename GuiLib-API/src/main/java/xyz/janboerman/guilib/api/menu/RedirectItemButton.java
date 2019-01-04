package xyz.janboerman.guilib.api.menu;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * A button that redirects to a different menu and has an icon.
 * @param <MH> the type of your MenuHolder
 */
public class RedirectItemButton<MH extends MenuHolder<?>> extends ItemButton<MH> implements RedirectButton<MH> {

    private final BiFunction<MH, InventoryClickEvent, ? extends Inventory> redirect;

    /**
     * Creates the button with the given icon and redirect.
     * @param icon the icon
     * @param redirect the redirect
     */
    public RedirectItemButton(ItemStack icon, Supplier<? extends Inventory> redirect) {
        this(icon, (menuHolder, event) -> redirect.get());
        Objects.requireNonNull(redirect, "Redirect cannot be null");
    }

    /**
     * Creates the button with the given icon and redirect function.
     * @param icon the icon
     * @param redirect the redirect function
     */
    public RedirectItemButton(ItemStack icon, BiFunction<MH, InventoryClickEvent, ? extends Inventory> redirect) {
        super(icon);
        this.redirect = Objects.requireNonNull(redirect, "Redirect cannot be null");
    }

    /**
     * Evaluates the redirect.
     * @param menuHolder the MenuHolder
     * @param event the InventoryClickEvent
     * @return the Inventory the player is redirected towards.
     */
    @Override
    public final Inventory to(MH menuHolder, InventoryClickEvent event) {
        return redirect.apply(menuHolder, event);
    }

    /**
     * Redirects the player to the inventory supplied by {@link #to(MenuHolder, InventoryClickEvent)}.
     * <p>
     * Subclasses that override this method should always call {@code super.onClick(menuHolder, event);}.
     * @param menuHolder the MenuHolder
     * @param event the InventoryClickEvent
     */
    @Override
    public void onClick(MH menuHolder, InventoryClickEvent event) {
        RedirectButton.super.onClick(menuHolder, event);
    }
    
}
