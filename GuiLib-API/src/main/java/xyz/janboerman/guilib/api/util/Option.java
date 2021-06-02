package xyz.janboerman.guilib.api.util;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Like {@link java.util.Optional}, but allows null values.
 */
public interface Option<T> {

    /**
     * Test whether this Option contains a value.
     * @return true if this Option contains a value, otherwise false.
     */
    public boolean isPresent();

    /**
     * Get the value from this Option.
     * @return the value
     * @throws NoSuchElementException if this option contains no value.
     */
    public T get() throws NoSuchElementException;

    /**
     * Construct an Option with a value. null is allowed.
     * @param value the value
     * @param <T> the type of the value
     * @return a new Option
     */
    public static <T> Option<T> some(T value) {
        return new Some(value);
    }

    /**
     * Get an Option without a value.
     * @param <T> unused
     * @return an empty Option
     */
    public static <T> Option<T> none() {
        return None.INSTANCE;
    }

}

class Some<T> implements Option<T> {

    private final T value;

    Some(T value) {
        this.value = value;
    }

    @Override
    public boolean isPresent() {
        return true;
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof Some)) return false;

        Some that = (Some) obj;
        return Objects.equals(this.value, that.value);
    }

    @Override
    public String toString() {
        return "Some(" + value + ")";
    }
}

class None implements Option {

    static final None INSTANCE = new None();

    private None() {
    }

    @Override
    public boolean isPresent() {
        return false;
    }

    @Override
    public Object get() throws NoSuchElementException {
        throw new NoSuchElementException("None");
    }

    @Override
    public String toString() {
        return "None";
    }
}