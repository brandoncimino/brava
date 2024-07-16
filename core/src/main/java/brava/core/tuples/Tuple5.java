package brava.core.tuples;

import brava.core.functional.PentaFunction;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.function.Function;

/**
 * A {@link Tuple} with 5 elements.
 *
 * @param a   the first element
 * @param b   the second element
 * @param c   the third element
 * @param d   the fourth element
 * @param e   the fifth element
 * @param <A> the {@link #a()} type
 * @param <B> the {@link #b()} type
 * @param <C> the {@link #c()} type
 * @param <D> the {@link #d()} type
 * @param <E> the {@link #e()} type
 */
public record Tuple5<A, B, C, D, E>(A a, B b, C c, D d, E e) implements Tuple<Tuple5<A, B, C, D, E>> {
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
            default -> throw new IndexOutOfBoundsException(index);
        };
    }

    @Contract(pure = true)
    @Override
    public int size() {
        return 5;
    }

    @Contract(pure = true)
    @Override
    public @NotNull Tuple5<A, B, C, D, E> getSelf() {
        return this;
    }

    /**
     * Adds a new element to the end of this tuple, making it a {@link Tuple6}.
     *
     * @param f   the new {@link Tuple6#f()}
     * @param <F> the type of {@link Tuple6#f()}
     * @return a new {@link Tuple6}
     */
    @Contract("_ -> new")
    public <F> @Nonnull Tuple6<A, B, C, D, E, F> append(F f) {
        return new Tuple6<>(a, b, c, d, e, f);
    }

    /**
     * Transforms each of my elements individually.
     *
     * @param aFunction the function that transforms {@link #a()}
     * @param bFunction the function that transforms {@link #b()}
     * @param cFunction the function that transforms {@link #c()}
     * @param dFunction the function that transforms {@link #d()}
     * @param eFunction the function that transforms {@link #e()}
     * @param <A2>      the new {@link #a()} type
     * @param <B2>      the new {@link #b()} type
     * @param <C2>      the new {@link #c()} type
     * @param <D2>      the new {@link #d()} type
     * @param <E2>      the new {@link #e()} type
     * @return a new {@link Tuple5}
     */
    @Contract("_, _, _, _, _ -> new")
    public <A2, B2, C2, D2, E2> @NotNull Tuple5<A2, B2, C2, D2, E2> map(
          @NotNull Function<? super A, ? extends A2> aFunction,
          @NotNull Function<? super B, ? extends B2> bFunction,
          @NotNull Function<? super C, ? extends C2> cFunction,
          @NotNull Function<? super D, ? extends D2> dFunction,
          @NotNull Function<? super E, ? extends E2> eFunction
    ) {
        return new Tuple5<>(
              aFunction.apply(a),
              bFunction.apply(b),
              cFunction.apply(c),
              dFunction.apply(d),
              eFunction.apply(e)
        );
    }

    /**
     * Combines my elements into a single {@link OUT}.
     *
     * @param function the {@link PentaFunction} that combines my elements
     * @param <OUT>    the function output type
     * @return the resulting {@link OUT}
     */
    public <OUT> OUT reduce(@NotNull PentaFunction<A, B, C, D, E, OUT> function) {
        return function.apply(a, b, c, d, e);
    }
}
