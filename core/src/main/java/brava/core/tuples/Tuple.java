package brava.core.tuples;

import brava.core.collections.ListBase;
import brava.core.functional.TriFunction;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * The base interface and utilities for working with <a href="https://en.wikipedia.org/wiki/Tuple">tuple</a>s.
 *
 * <h1>Common operations between {@link Tuple}s</h1>
 * <ul>
 *     <li>{@link Tuple3#append(Object)}: Creates a tuple of the next size up.</li>
 *     <li>{@link Tuple3#map(Function, Function, Function)}: Performs separate functions on each of the tuple's elements.</li>
 *     <li>{@link Tuple3#reduce(TriFunction)}: Combines element into a single result.</li>
 * </ul>
 */
public interface Tuple<SELF extends Tuple<SELF>> extends ListBase<Object> {
    @Contract(pure = true)
    @NotNull
    SELF getSelf();

    @Contract(pure = true)
    static Tuple0 of() {
        return Tuple0.INSTANCE;
    }

    @Contract("_ -> new")
    static <A> @NotNull Tuple1<A> of(A a) {
        return new Tuple1<>(a);
    }

    @Contract("_, _ -> new")
    static <A, B> @NotNull Tuple2<A, B> of(A a, B b) {
        return new Tuple2<>(a, b);
    }

    @Contract("_, _, _ -> new")
    static <A, B, C> @NotNull Tuple3<A, B, C> of(A a, B b, C c) {
        return new Tuple3<>(a, b, c);
    }

    @Contract("_, _, _, _ -> new")
    static <A, B, C, D> @NotNull Tuple4<A, B, C, D> of(A a, B b, C c, D d) {
        return new Tuple4<>(a, b, c, d);
    }

    @Contract("_, _, _, _, _ -> new")
    static <A, B, C, D, E> @NotNull Tuple5<A, B, C, D, E> of(A a, B b, C c, D d, E e) {
        return new Tuple5<>(a, b, c, d, e);
    }

    @Contract("_, _, _, _, _, _ -> new")
    static <A, B, C, D, E, F> @NotNull Tuple6<A, B, C, D, E, F> of(A a, B b, C c, D d, E e, F f) {
        return new Tuple6<>(a, b, c, d, e, f);
    }

    default <OUT> OUT reduce(Function<SELF, OUT> function) {
        return function.apply(getSelf());
    }
}
