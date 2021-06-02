package xyz.janboerman.guilib.api.animate;

import org.bukkit.inventory.ItemStack;
import xyz.janboerman.guilib.api.mask.Mask;
import xyz.janboerman.guilib.api.mask.Pattern;
import xyz.janboerman.guilib.api.menu.MenuButton;
import xyz.janboerman.guilib.api.util.IntBiConsumer;
import xyz.janboerman.guilib.api.util.IntGenerator;
import xyz.janboerman.guilib.api.util.Option;

import java.util.Objects;

/**
 * Represents a frame in an animation.
 * @param <Symbol> the symbol type
 * @param <Item> the container element type
 */
public class Frame<Symbol, Item> {

    private final Pattern<Symbol> pattern;
    private final Mask<Symbol, Item> mask;
    private final IntGenerator activeSlots;

    /**
     * Construct a frame
     * @param pattern the pattern used for this frame
     * @param mask the mask that will be applied using the pattern
     * @param activeSlots the slots on which this frame will apply.
     */
    public Frame(Pattern<Symbol> pattern, Mask<Symbol, Item> mask, IntGenerator activeSlots) {
        this.pattern = Objects.requireNonNull(pattern, "pattern cannot be null");
        this.mask = Objects.requireNonNull(mask, "mask cannot be null");
        this.activeSlots = Objects.requireNonNull(activeSlots, "activeSlots cannot be null");
    }

    /**
     * Applies the frame to a container.
     * @param container the container. Typically this is {@link xyz.janboerman.guilib.api.menu.MenuHolder#setButton(int, MenuButton)} or {@link org.bukkit.inventory.Inventory#setItem(int, ItemStack)};
     */
    public void apply(IntBiConsumer<? super Item> container) {
        activeSlots.forEachRemaining((int i) -> {
            Symbol symbol = pattern.getSymbol(i);
            Option<Item> item = mask.getItem(symbol);
            if (item.isPresent()) container.accept(i, item.get());
        });
        activeSlots.reset();
    }

    /**
     * Copy the frame, but use a new pattern.
     * @param pattern the new pattern
     * @return a new Frame
     */
    public Frame withNewPattern(Pattern<Symbol> pattern) {
        return new Frame(pattern, mask, activeSlots);
    }

    /**
     * Copy the frame, but use a new mask.
     * @param mask the new mask
     * @return a new Frame
     */
    public Frame withNewMask(Mask<Symbol, Item> mask) {
        return new Frame(pattern, mask, activeSlots);
    }

    /**
     * Copy the frame, but use new active slots
     * @param activeSlots the slots at which the new frame will be applied
     * @return a new frame
     */
    public Frame withNewActiveSlots(IntGenerator activeSlots) {
        return new Frame(pattern, mask, activeSlots);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Frame)) return false;

        Frame that = (Frame) o;
        return Objects.equals(this.pattern, that.pattern)
                && Objects.equals(this.mask, that.mask)
                && Objects.equals(this.activeSlots, activeSlots);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pattern, mask, activeSlots);
    }

    @Override
    public String toString() {
        return "Frame(pattern=" + pattern + ",mask=" + mask + ",activeSlots=" + activeSlots + ")";
    }

}
