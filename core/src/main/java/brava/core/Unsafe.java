package brava.core;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

/**
 * Wierd Tricks Sun Corporation HATES.
 */
public final class Unsafe {
    private Unsafe() {
        throw new UnsupportedOperationException("🚪🩸");
    }

    /**
     * Suppresses the {@code "unchecked"} cast warning that {@code (T) object} would normally produce.
     *
     * <h1>Example</h1>
     * Vanilla way:
     * <pre>{@code
     * @SuppresWarning("unchecked")
     * var casted = (T) object;
     * return casted;
     * }</pre>
     * Using {@link #cast(Object)}:
     * <pre>{@code
     * return Unsafe.cast(object);
     * }</pre>
     *
     * @param object the original object
     * @param <T>    the desired type
     * @return the object, cast to {@link T}
     */
    @Contract(pure = true, value = "null -> null; !null -> !null")
    public static <T> @Nullable T cast(@Nullable Object object) {
        @SuppressWarnings("unchecked")
        var casted = (T) object;
        return casted;
    }
}