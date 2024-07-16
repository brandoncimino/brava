package brava.core.tuples;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A {@link Tuple} with 1 element.
 *
 * @param a   the first (and only) element of the tuple
 * @param <A> the type of {@link #a()}
 */
public record Tuple1<A>(A a) implements Tuple<Tuple1<A>> {
    @Contract(pure = true)
    @Override
    public Object get(int index) {
        return switch (index) {
            case 0 -> a;
            default -> throw new IndexOutOfBoundsException(index);
        };
    }

    @Contract(pure = true)
    @Override
    public int size() {
        return 1;
    }

    @Contract(pure = true)
    @Override
    public @NotNull Stream<Object> stream() {
        // 📎 Single-item streams have a special optimization.
        return Stream.of(a);
    }

    /**
     * Adds a new element to this tuple, creating a {@link Tuple2}.
     *
     * @param b   the new {@link Tuple2#b()}
     * @param <B> the type of {@link Tuple2#b()}
     * @return a new {@link Tuple2}
     */
    @Contract("_ -> new")
    public <B> @NotNull Tuple2<A, B> append(B b) {
        return Tuple.of(a, b);
    }

    @Contract(pure = true)
    @Override
    public @NotNull Tuple1<A> getSelf() {
        return this;
    }

    /**
     * Creates a new tuple by transforming my element.
     *
     * @param aFunction the function that transforms {@link #a()}
     * @param <A2>      the new {@link #a()} element type
     * @return a new {@link Tuple1}
     */
    @Contract("_ -> new")
    public <A2> @NotNull Tuple1<A2> map(@NotNull Function<? super A, ? extends A2> aFunction) {
        return Tuple.of(aFunction.apply(a));
    }
}
