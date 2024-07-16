package brava.core.exceptions;

import javax.annotation.Nonnull;

/**
 * Utilities for working with {@link Throwable}s.
 */
public final class Exceptions {
    private Exceptions() {
        throw new UnsupportedOperationException("🚪🩸");
    }

    /**
     * If the {@code exception} is one of the given types, return it; otherwise, {@code throw} it.
     *
     * @param exception the {@link Throwable} under scrutiny
     * @param catching  a type that we <b><i>don't</i></b> want to {@code throw}
     * @return the {@code exception}, cast to {@link E2}
     */
    public static <E extends Throwable, E2 extends E> @Nonnull E2 throwUnless(
          @Nonnull E exception,
          @Nonnull Class<E2> catching
    ) throws E {
        if (catching.isInstance(exception)) {
            return catching.cast(exception);
        } else {
            throw exception;
        }
    }

    /**
     * If the {@code exception} is one of the given types, return it; otherwise, {@code throw} it.
     *
     * @param exception    the {@link Throwable} under scrutiny
     * @param catching     a type that we <b><i>don't</i></b> want to {@code throw}
     * @param alsoCatching alternatives to {@code catching}
     * @return the {@code exception}, cast to {@link E2}
     */
    @SafeVarargs
    public static <E extends Throwable, E2 extends E> @Nonnull E2 throwUnless(
          @Nonnull E exception,
          @Nonnull Class<? extends E2> catching,
          @Nonnull Class<? extends E2>... alsoCatching
    ) throws E {
        if (catching.isInstance(exception)) {
            return catching.cast(exception);
        }

        for (Class<? extends E2> e : alsoCatching) {
            if (e.isInstance(exception)) {
                return catching.cast(e);
            }
        }

        throw exception;
    }
}
