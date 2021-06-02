package xyz.janboerman.guilib.api.util;

/**
 * Specialization of {@link java.util.function.BiConsumer} where the first accepted value is an int.
 * @param <T> the type of the second accepted value
 */
@FunctionalInterface
public interface IntBiConsumer<T> {

    /**
     * Consumes an int and another value.
     * @param integer the int
     * @param value the other value
     */
    public void accept(int integer, T value);

}
