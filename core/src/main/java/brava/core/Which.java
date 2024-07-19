package brava.core;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Species one of two options.
 */
public enum Which {
    A, B;

    /**
     * @return that {@link Which} I am not
     */
    @Contract(pure = true)
    public @NotNull Which other() {
        return switch (this) {
            case A -> B;
            case B -> A;
        };
    }

    /**
     * @param a my choice if I am {@link #A}
     * @param b my choice if I am {@link #B}
     * @return my choice of {@link T}
     */
    @Contract(pure = true)
    public <T> T pickFrom(T a, T b) {
        return switch (this) {
            case A -> a;
            case B -> b;
        };
    }

    /**
     * Determines which option is <a href="https://en.wikipedia.org/wiki/Exclusive_or">exclusively {@code true}</a>.
     *
     * @param a the first option
     * @param b the second option
     * @return <ul>
     *     <li>If one of the options is {@code true} and the other is {@code false}, return that {@link Which} is true.</li>
     *     <li>If both are {@code true} <i>or</i> both are {@code false}, return {@link Optional#empty()}.</li>
     * </ul>
     */
    @Contract(pure = true)
    public static Optional<Which> isExclusivelyTrue(boolean a, boolean b) {
        if (a == b) {
            return Optional.empty();
        }

        return a ? Optional.of(A) : Optional.of(B);
    }

    /**
     * Determines which option is <a href="https://en.wikipedia.org/wiki/Exclusive_or">exclusively satisfies</a> a {@link Predicate}.
     *
     * @param a the first option
     * @param b the second option
     * @param predicate the code used to {@link Predicate#test(T)} the options
     * @return
     * <ul>
     *     <li>If one of the options returns {@code true} and the other returns {@code false}, return that {@link Which} is true.</li>
     *     <li>If both return {@code true} <i>or</i> both return {@code false}, return {@link Optional#empty()}.</li>
     * </ul>
     */
    public static <T> Optional<Which> isExclusivelyTrue(T a, T b, @NotNull Predicate<? super T> predicate) {
        return isExclusivelyTrue(predicate.test(a), predicate.test(b));
    }

    /**
     * Determines which option is <a href="https://en.wikipedia.org/wiki/Exclusive_or">exclusively</a> equal to {@code expected}.
     *
     * @param a the first option
     * @param b the second option
     * @return
     * <ul>
     *     <li>If one of the options is equal and the other isn't, return that {@link Which} is equal.</li>
     *     <li>If both are equal <i>or</i> neither are equal, return {@link Optional#empty()}.</li>
     * </ul>
     */
    public static <T> Optional<Which> isExclusivelyEqual(T a, T b, T expected) {
        return isExclusivelyTrue(Objects.equals(a, expected), Objects.equals(b, expected));
    }

    /**
     * Determines which option is <a href="https://en.wikipedia.org/wiki/Inequality_(mathematics)">strictly greater</a> than the other. 
     *
     * @param a the first option
     * @param b the second option
     * @return <ul>
     *     <li>If one is strictly greater than the other, returns that</li>
     *     <li>If they are equal, return {@link Optional#empty()}</li>
     * </ul>
     */
    @Contract(pure = true)
    public static <T extends Comparable<T>> Optional<Which> isBigger(@NotNull T a, @NotNull T b) {
        var comparison = a.compareTo(b);
        if (comparison > 0) {
            return Optional.of(A);
        } else if (comparison < 0) {
            return Optional.of(B);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Determines which option is <a href="https://en.wikipedia.org/wiki/Inequality_(mathematics)">strictly lesser</a> than the other. 
     *
     * @param a the first option
     * @param b the second option
     * @return <ul>
     *     <li>If one is strictly lesser than the other, returns that</li>
     *     <li>If they are equal, return {@link Optional#empty()}</li>
     * </ul>
     */
    @Contract(pure = true)
    public static <T extends Comparable<T>> Optional<Which> isSmaller(@NotNull T a, @NotNull T b) {
        var comparison = a.compareTo(b);
        if (comparison < 0) {
            return Optional.of(A);
        } else if (comparison > 0) {
            return Optional.of(B);
        } else {
            return Optional.empty();
        }
    }
}
