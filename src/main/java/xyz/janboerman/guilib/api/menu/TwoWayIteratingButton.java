package xyz.janboerman.guilib.api.menu;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ListIterator;
import java.util.function.UnaryOperator;

/**
 * Button that can update it's internal state to a previous state (on right click), or next state (on left click).
 * @param <T> the state's type
 * @param <MH> the menu holder's type
 */
public class TwoWayIteratingButton<T, MH extends MenuHolder<?>> extends IteratingButton<T, MH> {

    private UnaryOperator<T> backwardsFunction;

    /**
     * Creates the TwoWayIteratingButton with just the icon.
     * Using this constructor requires the subclass to initialize the fields {@link #backwardsFunction}, {@link #stateUpdater} and {@link #currentState} on construction.
     * Alternatively they must override {@link #updateStateForwards(MenuHolder, InventoryClickEvent)}, {@link #updateStateBackwards(MenuHolder, InventoryClickEvent)} and {@link #getCurrentState()}.
     * @param icon the icon of this button
     */
    protected TwoWayIteratingButton(ItemStack icon) {
        super(icon);
    }

    /**
     * Creates the TwoWayIteratingButton.
     * @param icon the icon of this button
     * @param initialState the initial current state
     * @param forwardsFunction the function that provides next states
     * @param backwardsFunction the function that provides previous states.
     */
    public TwoWayIteratingButton(ItemStack icon, T initialState, UnaryOperator<T> forwardsFunction, UnaryOperator<T> backwardsFunction) {
        super(icon, initialState, forwardsFunction);
        this.backwardsFunction = backwardsFunction;
    }

    /**
     * Creates a new TwoWayIteratingButton from a list iterator.
     * @param icon the icon
     * @param listIterator the iterator providing the states
     * @param <T> the state type
     * @param <MH> the menu holder type
     * @throws IllegalArgumentException if the listIterator has no element.
     * @return a new TwoWayIteratingButton
     */
    public static <T, MH extends MenuHolder<?>> TwoWayIteratingButton<T, MH> fromListIterator(ItemStack icon, ListIterator<T> listIterator) {
        if (!listIterator.hasNext()) throw new IllegalArgumentException("ListIterator must have at least one element");
        //now that the class is an anonymous class it can't be extended. is that a problem? It might be. I don't want to force the adapter pattern on my users.
        return new TwoWayIteratingButton<>(icon) {
            {
                setCurrentState(listIterator.next());
            }

            @Override
            public void updateStateForwards(MH menuHolder, InventoryClickEvent event) {
                setCurrentState(listIterator.next());
            }

            @Override
            public void updateStateBackwards(MH menuHolder, InventoryClickEvent event) {
                setCurrentState(listIterator.previous());
            }

            @Override
            public boolean beforeToggle(MH menuHolder, InventoryClickEvent event) {
                if (event.isLeftClick()) return listIterator.hasNext();
                else if(event.isRightClick()) return listIterator.hasPrevious();
                else return false;
            }
        };
    }

    /**
     * Updates the current state. If the click is a left-click the it updates to the next state.
     * If it is a right-click it updates to the previous state.
     * @param menuHolder the MenuHolder
     * @param event the event that caused the state update
     */
    @Override
    protected void updateCurrentState(MH menuHolder, InventoryClickEvent event) {
        if (event.isLeftClick()) {
            updateStateForwards(menuHolder, event);
        } else if (event.isRightClick()) {
            updateStateBackwards(menuHolder, event);
        }
    }

    /**
     * Updates the internal state to the next state.
     * @param menuHolder the menu holder
     * @param event the click event that caused the state update
     */
    protected void updateStateForwards(MH menuHolder, InventoryClickEvent event) {
        super.updateCurrentState(menuHolder, event);
    }

    /**
     * Updates the internal state to the previous state.
     * @param menuHolder the menu holder
     * @param event the click event that caused the state update
     */
    protected void updateStateBackwards(MH menuHolder, InventoryClickEvent event) {
        setCurrentState(backwardsFunction.apply(getCurrentState()));
    }
}
