package xyz.janboerman.guilib.api.menu;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

/**
 * This is a generalisation of {@link PermissionButton}.
 * Represents a button that delegates to another button when the predicate is satisfied.
 * @param <MH> the MenuHolder type
 */
public class PredicateButton<MH extends MenuHolder<?>> implements MenuButton<MH> {

    protected MenuButton<MH> delegate;
    private final BiPredicate<MH, InventoryClickEvent> predicate;
    private final BiConsumer<MH, InventoryClickEvent> predicateFailedCallback;

    /**
     * Creates the PredicateButton.
     * @param delegate the button that this button delegates to
     * @param predicate the predicate that needs to be satisfied in order for the delegate to be called
     */
    public PredicateButton(MenuButton<MH> delegate, BiPredicate<MH, InventoryClickEvent> predicate) {
        this(delegate, predicate, null);
    }

    /**
     * Creates the PredicateButton.
     * @param delegate the button that this button delegates to
     * @param predicate the predicate that needs to be satisfied in order for the delegate to be called
     * @param predicateFailedCallback the callback that is invoked when the predicate is not satisfied
     */
    public PredicateButton(MenuButton<MH> delegate, BiPredicate<MH, InventoryClickEvent> predicate, BiConsumer<MH, InventoryClickEvent> predicateFailedCallback) {
        this.delegate = Objects.requireNonNull(delegate, "Delegate button cannot be null");
        this.predicate = Objects.requireNonNull(predicate, "Predicate cannot be null");
        this.predicateFailedCallback = predicateFailedCallback;
    }

    /**
     * Called by the {@link MenuHolder} - tests whether the predicate is satisfied and calls {@link MenuButton#onClick(MenuHolder, InventoryClickEvent)} on the delegate button.
     * @param menuHolder the MenuHolder
     * @param event the InventoryClickEvent
     */
    @Override
    public void onClick(MH menuHolder, InventoryClickEvent event) {
        if (getPredicate().test(menuHolder, event)) {
            getDelegate().onClick(menuHolder, event);
        } else {
           getPredicateFailedCallback().ifPresent(callback -> callback.accept(menuHolder, event));
        }
    }

    /**
     * Get the button that this button delegates to when the predicate is satisfied.
     * Implementations that override this method must never return null.
     * @return the delegate button
     */
    protected MenuButton<MH> getDelegate() {
        return delegate;
    }

    /**
     * Get the predicate for this button.
     * @return the predicate
     */
    protected BiPredicate<MH, InventoryClickEvent> getPredicate() {
        return predicate;
    }

    /**
     * Optionally get the callback that is executed when the button is clicked and the predicate is not satisfied.
     * @return the Optional containing the callback if present. If there is no callback the empty Optional is returned.
     */
    protected Optional<BiConsumer<MH, InventoryClickEvent>> getPredicateFailedCallback() {
        return Optional.ofNullable(predicateFailedCallback);
    }

    /**
     * Gets the icon for this button.
     * @return the icon of the delegate button
     */
    @Override
    public ItemStack getIcon() {
        return delegate.getIcon();
    }
}
