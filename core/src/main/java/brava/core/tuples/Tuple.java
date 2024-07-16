package brava.core.tuples;

import brava.core.collections.CollectionBase;
import brava.core.functional.TriFunction;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.NoSuchElementException;
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
public interface Tuple<SELF extends Tuple<SELF>> extends CollectionBase<Object> {
    //region Factories

    /**
     * @return the singleton {@link Tuple0#instance()}
     */
    @Contract(pure = true)
    static @NotNull Tuple0 of() {
        return Tuple0.instance();
    }

    /**
     * @see Tuple1#Tuple1(A)
     */
    @Contract("_ -> new")
    static <A> @NotNull Tuple1<A> of(A a) {
        return new Tuple1<>(a);
    }

    /**
     * @see Tuple2#Tuple2(A, B)
     */
    @Contract("_, _ -> new")
    static <A, B> @NotNull Tuple2<A, B> of(A a, B b) {
        return new Tuple2<>(a, b);
    }

    /**
     * @see Tuple3#Tuple3(A, B, C)
     */
    @Contract("_, _, _ -> new")
    static <A, B, C> @NotNull Tuple3<A, B, C> of(A a, B b, C c) {
        return new Tuple3<>(a, b, c);
    }

    /**
     * @see Tuple4#Tuple4(A, B, C, D)
     */
    @Contract("_, _, _, _ -> new")
    static <A, B, C, D> @NotNull Tuple4<A, B, C, D> of(A a, B b, C c, D d) {
        return new Tuple4<>(a, b, c, d);
    }

    /**
     * @see Tuple5#Tuple5(A, B, C, D, E)
     */
    @Contract("_, _, _, _, _ -> new")
    static <A, B, C, D, E> @NotNull Tuple5<A, B, C, D, E> of(A a, B b, C c, D d, E e) {
        return new Tuple5<>(a, b, c, d, e);
    }

    /**
     * @see Tuple6#Tuple6(A, B, C, D, E, F)
     */
    @Contract("_, _, _, _, _, _ -> new")
    static <A, B, C, D, E, F> @NotNull Tuple6<A, B, C, D, E, F> of(A a, B b, C c, D d, E e, F f) {
        return new Tuple6<>(a, b, c, d, e, f);
    }
    //endregion

    /**
     * @return me, as my true {@link SELF}.
     */
    @Contract(pure = true)
    @NotNull
    SELF getSelf();

    /**
     * @param index the element index
     * @return the corresponding element
     */
    @Contract(pure = true)
    Object get(int index);

    @Override
    default @NotNull Iterator<Object> iterator() {
        return new Iterator<>() {
            private int position = 0;

            @Override
            public boolean hasNext() {
                return position < size();
            }

            @Override
            public Object next() {
                // ⚠ It's stupid, but we can't rely on the `IndexOutOfBoundsException` thrown by `get()` because, 
                // technically, `Iterator`'s contract requires a `NoSuchElementException` 🤷‍♀️
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }

                return get(position++);
            }
        };
    }

    /**
     * Combines my elements into a single {@link OUT}.
     *
     * @param function a {@link Function} that turns {@link SELF} ⇒ {@link OUT}
     * @param <OUT>    the function output type
     * @return the resulting {@link OUT}
     */
    default <OUT> OUT reduce(@Nonnull Function<SELF, OUT> function) {
        return function.apply(getSelf());
    }
}
