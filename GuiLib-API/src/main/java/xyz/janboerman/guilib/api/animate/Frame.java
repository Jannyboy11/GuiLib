package xyz.janboerman.guilib.api.animate;

import xyz.janboerman.guilib.api.mask.Mask;
import xyz.janboerman.guilib.api.mask.Pattern;
import xyz.janboerman.guilib.api.util.IntBiConsumer;
import xyz.janboerman.guilib.api.util.IntGenerator;
import xyz.janboerman.guilib.api.util.Option;

import java.util.Objects;

public class Frame<Symbol, Item> {

    private final Pattern<Symbol> pattern;
    private final Mask<Symbol, Item> mask;
    private final IntGenerator activeSlots;

    public Frame(Pattern<Symbol> pattern, Mask<Symbol, Item> mask, IntGenerator activeSlots) {
        this.pattern = Objects.requireNonNull(pattern, "pattern cannot be null");
        this.mask = Objects.requireNonNull(mask, "mask cannot be null");
        this.activeSlots = Objects.requireNonNull(activeSlots, "activeSlots cannot be null");
    }

    public void apply(IntBiConsumer<? super Item> updater) {
        activeSlots.forEachRemaining((int i) -> {
            Symbol symbol = pattern.getSymbol(i);
            Option<Item> item = mask.getItem(symbol);
            if (item.isPresent()) updater.accept(i, item.get());
        });
        activeSlots.reset();
    }

    public Frame withNewPattern(Pattern<Symbol> pattern) {
        return new Frame(pattern, mask, activeSlots);
    }

    public Frame withNewMask(Mask<Symbol, Item> mask) {
        return new Frame(pattern, mask, activeSlots);
    }

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
