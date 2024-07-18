package brava.core;

import brava.core.exceptions.UncheckedReflectionException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Utilities for working with {@link java.lang.reflect}.
 */
public final class Reflection {
    /**
     * A {@link Supplier} that declares a {@link ReflectiveOperationException}.
     * @param <T>
     */
    public interface ReflectionSupplier<T> extends Supplier<T> {
        T getUnchecked() throws ReflectiveOperationException;

        @ApiStatus.NonExtendable
        default T get() {
            try {
                return getUnchecked();
            } catch (ReflectiveOperationException e) {
                throw new UncheckedReflectionException(e);
            }
        }
    }

    /**
     * Invokes some code, wrapping any {@link ReflectiveOperationException}s inside of {@link UncheckedReflectionException}s.
     *
     * @param supplier the code you want to run
     * @return the result of the code
     */
    public static <OUT> OUT invoke(@NotNull ReflectionSupplier<OUT> supplier) {
        return supplier.get();
    }
}
