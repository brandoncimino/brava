package brava.core.tuples;

import brava.core.functional.QuadFunction;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public record Tuple4<A, B, C, D>(A a, B b, C c, D d) implements Tuple<Tuple4<A, B, C, D>> {

    @Contract(pure = true)
    @Override
    public Object get(int index) {
        return switch (index) {
            case 0 -> a;
            case 1 -> b;
            case 2 -> c;
            case 3 -> d;
            default -> throw new IndexOutOfBoundsException(index);
        };
    }

    @Contract(pure = true)
    @Override
    public int size() {
        return 4;
    }

    /**
     * Adds a new element to the end of this tuple, making it a {@link Tuple4}.
     *
     * @param e   the new {@link Tuple5#e()}
     * @param <E> the type of {@link Tuple5#e()}
     * @return a new {@link Tuple5}
     */
    @Contract("_ -> new")
    public <E> @NotNull Tuple5<A, B, C, D, E> append(E e) {
        return new Tuple5<>(a, b, c, d, e);
    }

    /**
     * Transforms each of my elements individually.
     *
     * @param aFunction the function that transforms {@link #a()}
     * @param bFunction the function that transforms {@link #b()}
     * @param cFunction the function that transforms {@link #c()}
     * @param dFunction the function that transforms {@link #d()}
     * @param <A2>      the new {@link #a()} type
     * @param <B2>      the new {@link #b()} type
     * @param <C2>      the new {@link #c()} type
     * @param <D2>      the new {@link #d()} type
     * @return a new {@link Tuple4}
     */
    @Contract("_, _, _, _ -> new")
    public <A2, B2, C2, D2> @NotNull Tuple4<A2, B2, C2, D2> map(
          @NotNull Function<? super A, ? extends A2> aFunction,
          @NotNull Function<? super B, ? extends B2> bFunction,
          @NotNull Function<? super C, ? extends C2> cFunction,
          @NotNull Function<? super D, ? extends D2> dFunction
    ) {
        return new Tuple4<>(
              aFunction.apply(a),
              bFunction.apply(b),
              cFunction.apply(c),
              dFunction.apply(d)
        );
    }

    /**
     * Combines my elements into a single {@link OUT}.
     *
     * @param function the {@link QuadFunction} that combines my elements
     * @param <OUT>    the function output type
     * @return the resulting {@link OUT}
     */
    public <OUT> OUT reduce(@NotNull QuadFunction<A, B, C, D, OUT> function) {
        return function.apply(this);
    }

    @Override
    public @NotNull Tuple4<A, B, C, D> getSelf() {
        return this;
    }
}
