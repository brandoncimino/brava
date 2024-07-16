package brava.core.tuples;

import brava.core.functional.TriFunction;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * A {@link Tuple} with 3 elements.
 *
 * @param a   the first element
 * @param b   the second element
 * @param c   the third element
 * @param <A> the {@link #a()} type
 * @param <B> the {@link #b()} type
 * @param <C> the {@link #c()} type
 */
public record Tuple3<A, B, C>(A a, B b, C c) implements Tuple<Tuple3<A, B, C>> {
    @Contract(pure = true)
    @Override
    public Object get(int index) {
        return switch (index) {
            case 0 -> a;
            case 1 -> b;
            case 2 -> c;
            default -> throw new IndexOutOfBoundsException(index);
        };
    }

    @Contract(pure = true)
    @Override
    public int size() {
        return 3;
    }

    /**
     * Adds a new element to the end of this tuple, making it a {@link Tuple4}.
     *
     * @param d   the new {@link Tuple4#d()}
     * @param <D> the type of {@link Tuple4#d()}
     * @return a new {@link Tuple4}
     */
    @Contract("_ -> new")
    public <D> @NotNull Tuple4<A, B, C, D> append(D d) {
        return new Tuple4<>(a, b, c, d);
    }

    /**
     * Transforms each of my elements individually.
     *
     * @param aFunction the function that transforms {@link #a()}
     * @param bFunction the function that transforms {@link #b()}
     * @param cFunction the function that transforms {@link #c()}
     * @return a new {@link Tuple3}
     * @param <A2> the new {@link #a()} type
     * @param <B2> the new {@link #b()} type
     * @param <C2> the new {@link #c()} type
     */
    @Contract("_, _, _ -> new")
    public <A2, B2, C2> @NotNull Tuple3<A2, B2, C2> map(
          @NotNull Function<? super A, ? extends A2> aFunction,
          @NotNull Function<? super B, ? extends B2> bFunction,
          @NotNull Function<? super C, ? extends C2> cFunction
    ) {
        return new Tuple3<>(
              aFunction.apply(a),
              bFunction.apply(b),
              cFunction.apply(c)
        );
    }

    /**
     * Combines my elements into a single {@link OUT}.
     *
     * @param function the {@link TriFunction} that combines my elements
     * @param <OUT>    the function output type
     * @return the resulting {@link OUT}
     */
    public <OUT> OUT reduce(@NotNull TriFunction<A, B, C, OUT> function) {
        return function.apply(this);
    }

    @Contract(pure = true)
    @Override
    public @NotNull Tuple3<A, B, C> getSelf() {
        return this;
    }
}
