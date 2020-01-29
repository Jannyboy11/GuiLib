package xyz.janboerman.guilib.api.menu;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

/**
 * A button that composes two buttons sequentially.
 * @param <P> the Plugin for the buttons
 * @param <MH1> the menu holder type of the first button
 * @param <MH2> the menu holder type of the second button
 * @param <MHR> the super type of both {@code MH1} and {@code MH2}
 */
public class SequenceButton<P extends Plugin, MHR extends MenuHolder<P>, MH1 extends MHR, MH2 extends MHR>
        implements MenuButton<MHR> {

    private final MenuButton<MH1> first;
    private final MenuButton<MH2> second;

    public SequenceButton(MenuButton<MH1> first, MenuButton<MH2> second) {
        this.first = Objects.requireNonNull(first, "First button cannot be null");
        this.second = Objects.requireNonNull(second, "Second button cannot be null");
    }

    /**
     * Get the first button in the sequence.
     * @return the first button
     */
    protected MenuButton<MH1> getFirst() {
        return first;
    }

    /**
     * Get the second button in the sequence.
     * @return the second button
     */
    protected MenuButton<MH2> getSecond() {
        return second;
    }

    /**
     * Delegates the click event to the first button, and then to the second button.
     * The menu holder {@link MHR} is down-casted to both {@link MH1} and {@link MH2}.
     *
     * @param holder the MenuHolder
     * @param event the InventoryClickEvent
     */
    @Override
    public void onClick(MHR holder, InventoryClickEvent event) {
        getFirst().onClick((MH1) holder, event);
        getSecond().onClick((MH2) holder, event);
    }

    /**
     * Get the icon stack.
     * @return the icon of the first button
     */
    @Override
    public ItemStack getIcon() {
        return getFirst().getIcon();
    }

    /**
     * Delegates the onAdd callback to the first and second button - in that order.
     * @param holder the holder to which this button is added
     * @param slot the position in the menu
     * @return whether both buttons' onAdd callbacks returned true
     * @see MenuHolder#setButton(int, MenuButton) 
     */
    @Override
    public boolean onAdd(MHR holder, int slot) {
        return getFirst().onAdd((MH1) holder, slot)
                & getSecond().onAdd((MH2) holder, slot);
    }

    /**
     * Delegates the onRemove callback to the second and first button - in that order.
     * @param holder the holder from which this button is removed
     * @param slot the position in the menu
     * @return whether both buttons' onRemove callbacks returned true
     * @see MenuHolder#unsetButton(int)
     */
    @Override
    public boolean onRemove(MHR holder, int slot) {
        return getSecond().onRemove((MH2) holder, slot)
                & getFirst().onRemove((MH1) holder, slot);
    }

}
