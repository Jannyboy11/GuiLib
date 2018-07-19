package xyz.janboerman.guilib.api.menu;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;

/**
 * Generalization of {@link ToggleButton}. This button cycles through a fixed array of states.
 * @param <T> the state type
 * @param <MH> the menu holder type
 */
public class CycleButton<T, MH extends MenuHolder<?>> extends TwoWayIteratingButton<T, MH> {

    private final T[] items;
    private int cursor;

    /**
     * Creates the cycle button.
     * @param items the items this button cycles through
     * @param startIndex at which index to start cycling
     * @param copyArray whether the button should use a copy of the array
     */
    protected CycleButton(ItemStack icon, T[] items, int startIndex, boolean copyArray) {
        super(icon);
        if (items == null) throw new NullPointerException("Items array cannot be null");
        if (items.length == 0) throw new IllegalArgumentException("Items array must contain at least one element");

        this.items = copyArray ? Arrays.copyOf(items, items.length) : items;
        setCursor(startIndex);
    }

    /**
     * Creates the cycle button.
     * @param items the items this button cycles through
     */
    public CycleButton(ItemStack icon, T... items) {
        this(icon, items, 0);
    }

    /**
     * Creates the cycle button.
     * @param items the items this button cycles through
     * @param startIndex at which index to start cycling
     */
    public CycleButton(ItemStack icon, T[] items, int startIndex) {
        this(icon, items, startIndex, true);
    }

    /**
     * Creates the cycle button.
     * @param items the items this button cycles through
     */
    public CycleButton(ItemStack icon, Collection<? extends T> items) {
        this(icon, items, 0);
    }

    /**
     * Creates the cycle button.
     * @param items the items this button cycles through
     * @param startIndex at which index to start cycling
     */
    public CycleButton(ItemStack icon, Collection<? extends T> items, int startIndex) {
        this(icon, (T[]) items.toArray(), startIndex, false);
    }

    /**
     * Creates a cycle button that cycles through all enumeration values.
     * @param enumClass the enumeration
     * @param <T> the type of the state in this button
     * @param <MH> the MenuHolder type
     * @return a new cycle button
     */
    public static <T extends Enum<?>, MH extends MenuHolder<?>> CycleButton<T, MH> fromEnum(ItemStack icon, Class<? extends T> enumClass) {
        return new CycleButton<>(icon, enumClass.getEnumConstants(), 0, false);
    }

    /**
     * Creates a cycle button that cycles through all enumeration values.
     * @param enumClass the enumeration
     * @param startValue the enum value at which to start cycling
     * @param <T> the type of the state in this button
     * @param <MH> the MenuHolder type
     * @return a new cycle button
     */
    public static <T extends Enum<?>, MH extends MenuHolder<?>> CycleButton<T, MH> fromEnum(ItemStack icon, Class<? extends T> enumClass, T startValue) {
        return new CycleButton<>(icon, enumClass.getEnumConstants(), startValue.ordinal(), false);
    }

    /**
     * reates a cycle button that cycles through the given enumeration values.
     * @param items the items this button cycles through
     * @param <T> the type of the state in this button
     * @param <MH> the MenuHolder type
     * @return a new cycle button
     */
    //completely redundant since users could just use the collection constructor, but this allows for easier refactoring :-)
    public static <T extends Enum<?>, MH extends MenuHolder<?>> CycleButton<T, MH> fromEnum(ItemStack icon, EnumSet<? extends T> items) {
        return new CycleButton<>(icon, items);
    }

    /**
     * reates a cycle button that cycles through the given enumeration values.
     * @param items the items this button cycles through
     * @param startValue the enum value at which to start cycling
     * @param <T> the type of the state in this button
     * @param <MH> the MenuHolder type
     * @return a new cycle button
     */
    public static <T extends Enum<?>, MH extends MenuHolder<?>> CycleButton<T, MH> fromEnum(ItemStack icon, EnumSet<? extends T> items, T startValue) {
        return new CycleButton<>(icon, items, startValue.ordinal());
    }

    /**
     * Moves the cursor to the next state.
     */
    protected void incrementCursor() {
        setCursor(getCursor() + 1);
    }

    /**
     * Moves the cursor to the previous state.
     */
    protected void decrementCursor() {
        setCursor(getCursor() - 1);
    }

    /**
     * Sets the cursor position.
     * @param cursor the new cursor
     */
    protected void setCursor(int cursor) {
        cursor = cursor % items.length;
        if (cursor < 0) cursor += items.length;
        this.cursor = cursor;
    }

    /**
     * Gets the current cursor position.
     * @return the cursor
     */
    protected int getCursor() {
        return cursor;
    }

    /**
     * Gets the current state of the button.
     * @return the state
     */
    @Override
    public T getCurrentState() {
        return items[getCursor()];
    }

    /**
     * Updates the current state to the previous state.
     * The CycleButton implementation delegates to {@link #incrementCursor()}.
     * @param menuHolder the menu holder
     * @param event the click event that caused the state update
     */
    @Override
    public void updateStateForwards(MH menuHolder, InventoryClickEvent event) {
        incrementCursor();
    }

    /**
     * Updates the current state to the next state.
     * The CycleButton implementation delegates to {@link #decrementCursor()}
     * @param menuHolder the menu holder
     * @param event the click event that caused the state update
     */
    @Override
    public void updateStateBackwards(MH menuHolder, InventoryClickEvent event) {
        decrementCursor();
    }

}
