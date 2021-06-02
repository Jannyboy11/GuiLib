package xyz.janboerman.guilib.api.mask.patterns;

import xyz.janboerman.guilib.api.mask.Pattern;
import xyz.janboerman.guilib.api.mask.patterns.CheckerboardPattern.Tile;

import java.util.Objects;

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
    private final Tile startWith, other;

    public CheckerboardPattern(int size, Tile startWith) {
        if (size < 0) throw new IllegalArgumentException("negative size: " + size);
        Objects.requireNonNull(startWith, "startWith cannot be null");

        this.size = size;
        this.startWith = startWith;
        this.other = startWith.other();
    }

    @Override
    public Tile getSymbol(int index) {
        if (index < 0 || index >= size) return null;
        else return index % 2 == 0 ? startWith : other;
    }

    public CheckerboardPattern inverse() {
        return new CheckerboardPattern(size, other);
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
