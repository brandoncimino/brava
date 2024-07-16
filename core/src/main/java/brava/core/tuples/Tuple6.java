package brava.core.tuples;

import brava.core.functional.HexaFunction;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * A {@link Tuple} with 6 elements.
 *
 * @param a   the first element
 * @param b   the second element
 * @param c   the third element
 * @param d   the fourth element
 * @param e   the fifth element
 * @param f   the sixth element
 * @param <A> the {@link #a()} type
 * @param <B> the {@link #b()} type
 * @param <C> the {@link #c()} type
 * @param <D> the {@link #d()} type
 * @param <E> the {@link #e()} type
 * @param <F> the {@link #f()} type
 */
public record Tuple6<A, B, C, D, E, F>(A a, B b, C c, D d, E e, F f) implements Tuple<Tuple6<A, B, C, D, E, F>> {
    @Contract(pure = true)
    @SuppressWarnings("DuplicatedCode")
    @Override
    public Object get(int index) {
        return switch (index) {
            case 0 -> a;
            case 1 -> b;
            case 2 -> c;
            case 3 -> d;
            case 4 -> e;
            case 5 -> f;
            default -> throw new IndexOutOfBoundsException(index);
        };
    }

    @Contract(pure = true)
    @Override
    public int size() {
        return 6;
    }

    @Contract(pure = true)
    @Override
    public @NotNull Tuple6<A, B, C, D, E, F> getSelf() {
        return this;
    }

    /**
     * Transforms each of my elements individually.
     *
     * @param aFunction the function that transforms {@link #a()}
     * @param bFunction the function that transforms {@link #b()}
     * @param cFunction the function that transforms {@link #c()}
     * @param dFunction the function that transforms {@link #d()}
     * @param eFunction the function that transforms {@link #e()}
     * @param fFunction the function that transforms {@link #f()}
     * @param <A2>      the new {@link #a()} type
     * @param <B2>      the new {@link #b()} type
     * @param <C2>      the new {@link #c()} type
     * @param <D2>      the new {@link #d()} type
     * @param <E2>      the new {@link #e()} type
     * @param <F2>      the new {@link #f()} type
     * @return a new {@link Tuple6}
     */
    @Contract("_, _, _, _, _, _ -> new")
    public <A2, B2, C2, D2, E2, F2> @NotNull Tuple6<A2, B2, C2, D2, E2, F2> map(
          @NotNull Function<? super A, ? extends A2> aFunction,
          @NotNull Function<? super B, ? extends B2> bFunction,
          @NotNull Function<? super C, ? extends C2> cFunction,
          @NotNull Function<? super D, ? extends D2> dFunction,
          @NotNull Function<? super E, ? extends E2> eFunction,
          @NotNull Function<? super F, ? extends F2> fFunction
    ) {
        return new Tuple6<>(
              aFunction.apply(a),
              bFunction.apply(b),
              cFunction.apply(c),
              dFunction.apply(d),
              eFunction.apply(e),
              fFunction.apply(f)
        );
    }

    /**
     * Combines each of my elements into a single {@link OUT}.
     *
     * @param function the {@link HexaFunction} that combines my elements
     * @param <OUT>    the function output type
     * @return the resulting {@link OUT}
     */
    public <OUT> OUT reduce(@NotNull HexaFunction<A, B, C, D, E, F, OUT> function) {
        return function.apply(a, b, c, d, e, f);
    }
}
