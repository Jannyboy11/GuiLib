package xyz.janboerman.guilib.api.menu;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * A button that resets buttons in the menu in which it is clicked.
 * @param <MH> the menu holder type
 */
public class ResetButton<MH extends MenuHolder<?>> extends ItemButton<MH> {

    private Supplier<IntStream> slots;
    private IntFunction<? extends MenuButton<? super MH>> mapper;

    /**
     * Creates the reset button without slot providers and a slot-to-button mapping.
     * Subclasses that use this method need to call either {@link #setMapping(Supplier, IntFunction)} )} or any of its overloads,
     * or it needs to override {@link #getResetSlots()} and {@link #getButtonFor(int)}.
     *
     * @param icon the button's icon
     */
    protected ResetButton(ItemStack icon) {
        super(icon);
    }

    /**
     * Creates the reset button.
     *
     * @param icon the icon
     * @param newContents the slot-to-button mapping
     */
    public ResetButton(ItemStack icon, Map<Integer, ? extends MenuButton<? super MH>> newContents) {
        super(icon);
        setMapping(newContents);
    }

    /**
     * Creates the reset button.
     *
     * @param icon the icon
     * @param newContents the slot-to-button mapping
     */
    public ResetButton(ItemStack icon, MenuButton<? super MH>[] newContents) {
        super(icon);
        setMapping(newContents);
    }

    /**
     * Creates the reset button.
     *
     * @param icon the icon
     * @param newContents the slot-to-button mapping
     */
    public ResetButton(ItemStack icon, List<? extends MenuButton<? super MH>> newContents) {
        super(icon);
        setMapping(newContents);
    }

    /**
     * Creates the reset button.
     *
     * @param icon the icon
     * @param slots the slots for which this button will perform a reset
     * @param mapper the mapping function that calculates which button needs to be reset at which slot
     */
    public ResetButton(ItemStack icon, Supplier<IntStream> slots, IntFunction<? extends MenuButton<? super MH>> mapper) {
        super(icon);
        setMapping(slots, mapper);
    }

    /**
     * Set the mapping.
     * @param newContents the slot-to-button mapping
     */
    public void setMapping(Map<Integer, ? extends MenuButton<? super MH>> newContents) {
        Objects.requireNonNull(newContents, "NewContents cannot be null");
        this.slots = () -> newContents.keySet().stream().mapToInt(Integer::intValue);
        this.mapper = newContents::get;
    }

    /**
     * Set the mapping.
     * @param newContents the slot-to-button mapping
     */
    public void setMapping(MenuButton<? super MH>[] newContents) {
        Objects.requireNonNull(newContents, "NewContents cannot be null");
        this.slots = () -> IntStream.range(0, newContents.length);
        this.mapper = i -> newContents[i];
    }

    /**
     * Set the mapping.
     * @param newContents the slot-to-button mapping
     */
    public void setMapping(List<? extends MenuButton<? super MH>> newContents) {
        Objects.requireNonNull(newContents, "NewContents cannot be null");
        this.slots = () -> IntStream.range(0, newContents.size());
        this.mapper = newContents::get;
    }

    /**
     * Set the mapping.
     * @param slots the slots for which to reset buttons
     * @param mapper the slot-to-button mapping
     */
    public void setMapping(Supplier<IntStream> slots, IntFunction<? extends MenuButton<? super MH>> mapper) {
        this.slots = Objects.requireNonNull(slots, "Slots supplier cannot be null");
        this.mapper = Objects.requireNonNull(mapper, "Slot-to-button mapper cannot be null");
    }

    /**
     * Get the slots for which new buttons need to be calculated.
     * @return the stream of slots
     */
    protected IntStream getResetSlots() {
        return Objects.requireNonNull(slots.get(), "The supplier that supplies reset slots cannot return null. Just use an empty stream already.");
    }

    /**
     * Get the button that needs to be set at the given slot.
     * @param slot the slot
     * @return the new button, or null if there shouldn't be a button at the slot
     */
    protected MenuButton<? super MH> getButtonFor(int slot) {
        return mapper.apply(slot);
    }

    /**
     * Resets the buttons in the menu.
     *
     * @param menuHolder the menu holder
     * @param event the InventoryClickEvent
     */
    @Override
    public void onClick(MH menuHolder, InventoryClickEvent event) {
        getResetSlots().forEach(slot -> menuHolder.setButton(slot, getButtonFor(slot)));
    }

}
