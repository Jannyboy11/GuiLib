package xyz.janboerman.guilib.api.mask.patterns;

import xyz.janboerman.guilib.api.mask.Pattern;
import xyz.janboerman.guilib.api.mask.patterns.BorderPattern.Border;

import java.util.Objects;

public class BorderPattern implements Pattern<Border> {

    public enum Border {
        OUTER,
        INNER;

        public boolean isOuter() {
            return this == OUTER;
        }

        public boolean isInner() {
            return this == INNER;
        }
    }

    private final int width, height;

    public BorderPattern(int width, int height) {
        if (width < 0) throw new IllegalArgumentException("Negative width: " + width);
        if (height < 0) throw new IllegalArgumentException("Negative height: "+ height);

        this.width = width;
        this.height = height;
    }

    @Override
    public Border getSymbol(int index) {
        if (index < 0) return null;
        int size = width * height;

        if (index >= size) return null;

        //top border
        if (index < width) return Border.OUTER;
        //bottom border
        if (index > size - width) return Border.OUTER;
        //left border
        if (index % width == 0) return Border.OUTER;
        //right border
        if (index % width == width - 1) return Border.OUTER;

        return Border.INNER;
    };

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof BorderPattern)) return false;

        BorderPattern that = (BorderPattern) o;
        return this.width == that.width && this.height == that.height;
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height);
    }

    @Override
    public String toString() {
        return "BorderPattern(width=" + width + ",height=" + height + ")";
    }
}
