package brava.either;


import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Species one of two options.
 */
public enum Which {
    A, B;

    public Which other() {
        return switch (this) {
            case A -> B;
            case B -> A;
        };
    }

    public <T> T pickFrom(T a, T b) {
        return switch (this) {
            case A -> a;
            case B -> b;
        };
    }

    public static Optional<Which> isExclusivelyTrue(boolean a, boolean b) {
        if (a == b) {
            return Optional.empty();
        }

        return a ? Optional.of(A) : Optional.of(B);
    }

    public static <T> Optional<Which> isExclusivelyTrue(T a, T b, Predicate<? super T> predicate) {
        return isExclusivelyTrue(predicate.test(a), predicate.test(b));
    }

    public static <T> Optional<Which> isExclusivelyEqual(T a, T b, T expected) {
        return isExclusivelyTrue(Objects.equals(a, expected), Objects.equals(b, expected));
    }

    public static <T extends Comparable<T>> Optional<Which> isBigger(T a, T b) {
        var comparison = a.compareTo(b);
        if (comparison > 0) {
            return Optional.of(A);
        } else if (comparison < 0) {
            return Optional.of(B);
        } else {
            return Optional.empty();
        }
    }

    public static <T extends Comparable<T>> Optional<Which> isSmaller(T a, T b) {
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
