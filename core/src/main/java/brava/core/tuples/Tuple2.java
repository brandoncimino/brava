package brava.core.tuples;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A {@link Tuple} with 2 elements.
 *
 * @param a   the first element
 * @param b   the second element
 * @param <A> the type of {@link #a()}
 * @param <B> the type of {@link #b()}
 */
public record Tuple2<A, B>(A a, B b) implements Tuple<Tuple2<A, B>> {
    @Contract(pure = true)
    @Override
    public Object get(int index) {
        return switch (index) {
            case 0 -> a;
            case 1 -> b;
            default -> throw new IndexOutOfBoundsException(index);
        };
    }

    @Contract(pure = true)
    @Override
    public int size() {
        return 2;
    }

    /**
     * Adds a new element to the end of this tuple, making it a {@link Tuple3}.
     *
     * @param c the new {@link Tuple3#c()}
     * @param <C> the type of {@link Tuple3#c()}
     * @return a new {@link Tuple3}
     */
    @Contract("_ -> new")
    public <C> @NotNull Tuple3<A, B, C> append(C c) {
        return Tuple.of(a, b, c);
    }

    /**
     * Combines my elements into a single {@link OUT}.
     *
     * @param function the {@link BiFunction} that produces {@link OUT}
     * @param <OUT>    the function output type
     * @return the resulting {@link OUT}
     */
    public <OUT> OUT reduce(@NotNull BiFunction<A, B, OUT> function) {
        return function.apply(a, b);
    }

    /**
     * Applies transformation functions to each of my elements individually.
     *
     * @param aFunction the transformation applied to {@link #a()}
     * @param bFunction the transformation applied to {@link #b()}
     * @param <A2>      the new {@link #a()} type
     * @param <B2>      the new {@link #b()} type
     * @return a new {@link Tuple2}
     */
    @Contract("_, _ -> new")
    public <A2, B2> @NotNull Tuple2<A2, B2> map(
          @NotNull Function<? super A, ? extends A2> aFunction,
          @NotNull Function<? super B, ? extends B2> bFunction
    ) {
        return new Tuple2<>(
              aFunction.apply(a),
              bFunction.apply(b)
        );
    }

    @Contract(pure = true)
    @Override
    public @NotNull Tuple2<A, B> getSelf() {
        return this;
    }

    // Tuple2-only methods

    /**
     * Applies a transformation function to {@link #a()}.
     *
     * @param aFunction the transformation applied to {@link #a()}
     * @param <A2>      the new {@link #a()} type
     * @return a new {@link Tuple2}
     * @apiNote This is syntactic sugar for a simplified version of {@link #map(Function, Function)}.
     * @see #map(Function, Function)
     * @see #mapB(Function)
     */
    @Contract("_ -> new")
    public <A2> @NotNull Tuple2<A2, B> mapA(@NotNull Function<A, A2> aFunction) {
        return new Tuple2<>(
              aFunction.apply(a),
              b
        );
    }

    /**
     * Applies a transformation function to {@link #b()}.
     *
     * @param bFunction the transformation applied to {@link #b()}
     * @param <B2>      the new {@link #b()} type
     * @return a new {@link Tuple2}
     * @apiNote This is syntactic sugar for a simplified version of {@link #map(Function, Function)}.
     * @see #map(Function, Function)
     * @see #mapA(Function)
     */
    @Contract("_ -> new")
    public <B2> @NotNull Tuple2<A, B2> mapB(@NotNull Function<B, B2> bFunction) {
        return new Tuple2<>(
              a,
              bFunction.apply(b)
        );
    }
    
    //endregion
}
