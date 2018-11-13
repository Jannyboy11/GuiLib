package xyz.janboerman.guilib.util;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * A supplier that caches it's supplied value when it is supplied for the first time.
 * @param <R> the result type
 */
public class CachedSupplier<R> implements Supplier<R> {

    private static final Object NULL = new Object();

    private Supplier<? extends R> internal;
    private Object result = NULL;

    /**
     * Create the cached supplier.
     * @param supplier the proxy that will supply value that will be cached
     */
    public CachedSupplier(Supplier<? extends R> supplier) {
        this.internal = Objects.requireNonNull(supplier, "Supplier cannot be null");
    }

    /**
     * Gets the result from the cache, or from the delegate supplier if no value was cached.
     * @return the supplied value
     */
    @Override
    public R get() {
        if (result == NULL) { //use or own NULL instead of the java null because the supplier may supply null.
            result = internal.get();
            internal = null; //discard the underlying supplier since it is no longer needed and can be garbage collected.
        }
        return (R) result;
    }

}
