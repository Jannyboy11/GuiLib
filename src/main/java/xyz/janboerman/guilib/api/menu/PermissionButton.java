package xyz.janboerman.guilib.api.menu;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A button that only works when the player who clicked the button has a permission
 * @param <MH> the menu type
 */
public class PermissionButton<MH extends MenuHolder<?>> extends PredicateButton<MH> {

    private final String permission;
    private final Consumer<? super HumanEntity> noPermissionCallback;

    /**
     * Creates the permission button.
     * @param permission the permission that is required to use this button
     * @param proxy the proxy to which the click is delegated when the player has the permission
     */
    public PermissionButton(String permission, MenuButton<MH> proxy) {
        this(permission, proxy, null);
    }

    /**
     * Creates the permission button.
     * @param permission the permission that is required to use this button
     * @param proxy the proxy to which the click is delegated when the player has the permission
     * @param noPermissionCallback the callback that is executed when the player clicks the button but doesn't have the permission
     */
    public PermissionButton(String permission, MenuButton<MH> proxy, Consumer<? super HumanEntity> noPermissionCallback) {
        super(proxy, (menuHolder, event) -> event.getWhoClicked().hasPermission(permission));
        this.permission = Objects.requireNonNull(permission, "Permission cannot be null");
        this.noPermissionCallback = noPermissionCallback;
    }

    /**
     * Gets the permission for this button.
     * @return the permission string
     */
    public String getPermission() {
        return permission;
    }

    /**
     * Optionally get the callback that is executed when the button is clicked and the inventory clicker doesn't have the permission.
     * @return the Optional containing the callback if present. If there is no callback the empty Optional is returned.
     */
    @Override
    protected Optional<BiConsumer<MH, InventoryClickEvent>> getPredicateFailedCallback() {
        return getNoPermissionCallback().map(consumer -> (menuHolder, event) -> consumer.accept(event.getWhoClicked()));
    }

    /**
     * Get the no-permission callback, if present.
     * @return the Optional containing the callback if it is present in this button, otherwise the empty Optional
     */
    public Optional<Consumer<? super HumanEntity>> getNoPermissionCallback() {
        return Optional.ofNullable(noPermissionCallback);
    }

}
