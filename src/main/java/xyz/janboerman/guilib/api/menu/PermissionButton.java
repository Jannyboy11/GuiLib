package xyz.janboerman.guilib.api.menu;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * A button that only works when the player who clicked the button has a permission
 * @param <MH> the menu type
 */
public class PermissionButton<MH extends MenuHolder<?>> implements MenuButton<MH> {

    private final MenuButton<MH> proxy;
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
        this.proxy = Objects.requireNonNull(proxy, "Proxy cannot be null");
        this.permission = Objects.requireNonNull(permission, "Permission cannot be null");
        this.noPermissionCallback = noPermissionCallback;
    }

    /**
     * Called by the {@link MenuHolder} - tests whether the player has the permission and calls {@link MenuButton#onClick(MenuHolder, InventoryClickEvent)} on the proxy.
     * @param holder the MenuHolder
     * @param event the InventoryClickEvent
     */
    @Override
    public void onClick(MH holder, InventoryClickEvent event) {
        HumanEntity whoClicked = event.getWhoClicked();
        if (whoClicked.hasPermission(getPermission())) {
            proxy.onClick(holder, event);
        } else if (noPermissionCallback != null) {
            noPermissionCallback.accept(whoClicked);
        }
    }

    /**
     * Gets the icon for this button.
     * @return the icon of the proxy
     */
    @Override
    public ItemStack getIcon() {
        return proxy.getIcon();
    }

    /**
     * Gets the permission for this button.
     * @return the permission string
     */
    public String getPermission() {
        return permission;
    }

    /**
     * Get the no-permission callback, if present.
     * @return the Optional containing the callback if it is present in this button, otherwise the empty Optional
     */
    public Optional<Consumer<? super HumanEntity>> getNoPermissionCallback() {
        return Optional.ofNullable(noPermissionCallback);
    }

}
