package xyz.janboerman.guilib.api.mask.patterns;

import xyz.janboerman.guilib.api.mask.Pattern;
import xyz.janboerman.guilib.api.mask.patterns.CheckerboardPattern.Tile;

import java.util.Objects;

/**
 * A checkerboard pattern alternates between black and white tiles.
 * This implementation is very naive because it uses the fact that all inventory grids in minecraft have an odd number of columns (width).
 */
public class CheckerboardPattern implements Pattern<Tile> {

    public enum Tile {
        BLACK,
        WHITE;

        public boolean isBlack() {
            return this == BLACK;
        }

        public boolean isWhite() {
            return this == WHITE;
        }

        private Tile other() {
            switch (this) {
                case BLACK: return WHITE;
                case WHITE: return BLACK;
                default: assert false; return null;
            }
        }
    }

    private final int size;
    private final Tile startWith;
    private CheckerboardPattern inverse;

    private CheckerboardPattern(int size, Tile startWith, CheckerboardPattern inverse) {
        this.size = size;
        this.startWith = startWith;
        this.inverse = inverse;
    }

    /**
     * Construct a CheckerboardPattern.
     * @param size the size of the inventory grid
     * @param startWith the colour of the first tile.
     */
    public CheckerboardPattern(int size, Tile startWith) {
        if (size < 0) throw new IllegalArgumentException("negative size: " + size);
        Objects.requireNonNull(startWith, "startWith cannot be null");

        this.size = size;
        this.startWith = startWith;
    }

    /**
     * Get the tile at a given index.
     * @param index the slot's index
     * @return the tile at the slot, or null if the index is below 0 or larger than the grid size
     */
    @Override
    public Tile getSymbol(int index) {
        if (index < 0 || index >= size) return null;
        else return index % 2 == 0 ? startWith : startWith.other();
    }

    /**
     * Get a checkerboard pattern that is the inverse of this pattern (all white and black tiles are swapped).
     * @return the inverse pattern
     */
    public CheckerboardPattern inverse() {
        if (inverse == null) {
            inverse = new CheckerboardPattern(size, startWith.other(), this);
        }

        return inverse;
    }

    @Override
    public int hashCode() {
        return Objects.hash(size, startWith);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof CheckerboardPattern)) return false;

        CheckerboardPattern that = (CheckerboardPattern) o;
        return this.size == that.size && this.startWith == that.startWith;
    }

    @Override
    public String toString() {
        return "CheckerboardPattern(size=" + size + ",startWith=" + startWith + ")";
    }
}
