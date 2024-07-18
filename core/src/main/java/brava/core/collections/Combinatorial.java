package brava.core.collections;

import brava.core.tuples.*;
import com.google.common.collect.Streams;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;


/**
 * Methods for producing combinations of values.
 */
@ParametersAreNonnullByDefault
public final class Combinatorial {
    private Combinatorial() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a 2-fold <a href="https://en.wikipedia.org/wiki/Cartesianp1roduct">Cartesian product</a>.
     * <p/>
     * The combinations will be in <a href="https://en.wikipedia.org/wiki/Lexicographico1rder#Cartesianp1roducts">lexicographic order</a>, e.g.:
     * <pre>{@code
     * Combinatorial.cartesianProduct(
     *  List.of('a', 'b'),
     *  List.of(1, 2, 3)
     * )
     *  .forEach(System.out::println);
     *
     * // (a, 1)
     * // (a, 2)
     * // (a, 3)
     * // (b, 1)
     * // (b, 2)
     * // (b, 3)
     * }</pre>
     *
     * @param a   all possible {@link Tuple2#a()} values
     * @param b   all possible {@link Tuple2#b()} values
     * @param <A> the type of {@link Tuple2#a()}
     * @param <B> the type of {@link Tuple2#b()}
     * @return a {@link Stream} containing every unique combination as a {@link Tuple2}
     * @apiNote 📎 You can use {@link java.util.EnumSet#allOf(Class)} to get an {@link Iterable} out of an {@link Enum}'s values.
     */
    @Nonnull
    @Contract(pure = true)
    public static <A, B> Stream<Tuple2<A, B>> cartesianProduct(
          Iterable<A> a,
          Iterable<B> b
    ) {
        return Streams.stream(a)
              .flatMap(a1 -> Streams.stream(b)
                    .map(b1 -> Tuple.of(a1, b1))
              );
    }

    /**
     * Returns a 3-fold <a href="https://en.wikipedia.org/wiki/Cartesian_product">Cartesian product</a>.
     * <p/>
     * The combinations will be in <a href="https://en.wikipedia.org/wiki/Lexicographic_order#Cartesian_products">lexicographic order</a>, e.g.:
     * <pre>{@code
     * Combinatorial.cartesianProduct(
     *  List.of('a', 'b'),
     *  List.of(1, 2, 3)
     * )
     *  .forEach(System.out::println);
     *
     * // (a, 1)
     * // (a, 2)
     * // (a, 3)
     * // (b, 1)
     * // (b, 2)
     * // (b, 3)
     * }</pre>
     *
     * @param a   all possible {@link Tuple3#a()} values
     * @param b   all possible {@link Tuple3#b()} values
     * @param c   all possible {@link Tuple3#c()} values
     * @param <A> the type of {@link Tuple3#a()}
     * @param <B> the type of {@link Tuple3#b()}
     * @param <C> the type of {@link Tuple3#c()}
     * @return a {@link Stream} containing every unique combination as a {@link Tuple3}
     * @apiNote 📎 You can use {@link java.util.EnumSet#allOf(Class)} to get an {@link Iterable} out of an {@link Enum}'s values.
     */
    @Nonnull
    @Contract(pure = true)
    public static <A, B, C> Stream<Tuple3<A, B, C>> cartesianProduct(
          Iterable<A> a,
          Iterable<B> b,
          Iterable<C> c
    ) {
        return Streams.stream(a)
              .flatMap(a1 -> Streams.stream(b)
                    .flatMap(b1 -> Streams.stream(c)
                          .map(c1 -> Tuple.of(a1, b1, c1))
                    )
              );
    }

    /**
     * Returns a 4-fold <a href="https://en.wikipedia.org/wiki/Cartesian_product">Cartesian product</a>.
     * <p/>
     * The combinations will be in <a href="https://en.wikipedia.org/wiki/Lexicographic_order#Cartesian_products">lexicographic order</a>, e.g.:
     * <pre>{@code
     * Combinatorial.cartesianProduct(
     *  List.of('a', 'b'),
     *  List.of(1, 2, 3)
     * )
     *  .forEach(System.out::println);
     *
     * // (a, 1)
     * // (a, 2)
     * // (a, 3)
     * // (b, 1)
     * // (b, 2)
     * // (b, 3)
     * }</pre>
     *
     * @param a   all possible {@link Tuple4#a()} values
     * @param b   all possible {@link Tuple4#b()} values
     * @param c   all possible {@link Tuple4#c()} values
     * @param d   all possible {@link Tuple4#d()} values
     * @param <A> the type of {@link Tuple4#a()}
     * @param <B> the type of {@link Tuple4#b()}
     * @param <C> the type of {@link Tuple4#c()}
     * @param <D> the type of {@link Tuple4#d()}
     * @return a {@link Stream} containing every unique combination as a {@link Tuple4}
     * @apiNote 📎 You can use {@link java.util.EnumSet#allOf(Class)} to get an {@link Iterable} out of an {@link Enum}'s values.
     */
    @Nonnull
    @Contract(pure = true)
    public static <A, B, C, D> Stream<Tuple4<A, B, C, D>> cartesianProduct(
          Iterable<A> a,
          Iterable<B> b,
          Iterable<C> c,
          Iterable<D> d
    ) {
        return Streams.stream(a)
              .flatMap(a1 -> Streams.stream(b)
                    .flatMap(b1 -> Streams.stream(c)
                          .flatMap(c1 -> Streams.stream(d)
                                .map(d1 -> Tuple.of(a1, b1, c1, d1))
                          )
                    )
              );
    }

    /**
     * Returns a 5-fold <a href="https://en.wikipedia.org/wiki/Cartesian_product">Cartesian product</a>.
     * <p/>
     * The combinations will be in <a href="https://en.wikipedia.org/wiki/Lexicographic_order#Cartesian_products">lexicographic order</a>, e.g.:
     * <pre>{@code
     * Combinatorial.cartesianProduct(
     *  List.of('a', 'b'),
     *  List.of(1, 2, 3)
     * )
     *  .forEach(System.out::println);
     *
     * // (a, 1)
     * // (a, 2)
     * // (a, 3)
     * // (b, 1)
     * // (b, 2)
     * // (b, 3)
     * }</pre>
     *
     * @param a   all possible {@link Tuple5#a()} values
     * @param b   all possible {@link Tuple5#b()} values
     * @param c   all possible {@link Tuple5#c()} values
     * @param d   all possible {@link Tuple5#d()} values
     * @param e   all possible {@link Tuple5#e()} values
     * @param <A> the type of {@link Tuple5#a()}
     * @param <B> the type of {@link Tuple5#b()}
     * @param <C> the type of {@link Tuple5#c()}
     * @param <D> the type of {@link Tuple5#d()}
     * @param <E> the type of {@link Tuple5#e()}
     * @return a {@link Stream} containing every unique combination as a {@link Tuple5}
     * @apiNote 📎 You can use {@link java.util.EnumSet#allOf(Class)} to get an {@link Iterable} out of an {@link Enum}'s values.
     */
    @Nonnull
    @Contract(pure = true)
    public static <A, B, C, D, E> Stream<Tuple5<A, B, C, D, E>> cartesianProduct(
          Iterable<A> a,
          Iterable<B> b,
          Iterable<C> c,
          Iterable<D> d,
          Iterable<E> e
    ) {
        return Streams.stream(a)
              .flatMap(a1 -> Streams.stream(b)
                    .flatMap(b1 -> Streams.stream(c)
                          .flatMap(c1 -> Streams.stream(d)
                                .flatMap(d1 -> Streams.stream(e)
                                      .map(e1 -> Tuple.of(a1, b1, c1, d1, e1)
                                      )
                                )
                          )
                    )
              );
    }

    /**
     * Returns a 6-fold <a href="https://en.wikipedia.org/wiki/Cartesianp1roduct">Cartesian product</a>.
     * <p/>
     * The combinations will be in <a href="https://en.wikipedia.org/wiki/Lexicographico1rder#Cartesianp1roducts">lexicographic order</a>, e.g.:
     * <pre>{@code
     * Combinatorial.cartesianProduct(
     *  List.of('a', 'b'),
     *  List.of(1, 2, 3)
     * )
     *  .forEach(System.out::println);
     *
     * // (a, 1)
     * // (a, 2)
     * // (a, 3)
     * // (b, 1)
     * // (b, 2)
     * // (b, 3)
     * }</pre>
     *
     * @param a   all possible {@link Tuple6#a()} values
     * @param b   all possible {@link Tuple6#b()} values
     * @param c   all possible {@link Tuple6#c()} values
     * @param d   all possible {@link Tuple6#d()} values
     * @param e   all possible {@link Tuple6#e()} values
     * @param f   all possible {@link Tuple6#f()} values
     * @param <A> the type of {@link Tuple6#a()}
     * @param <B> the type of {@link Tuple6#b()}
     * @param <C> the type of {@link Tuple6#c()}
     * @param <D> the type of {@link Tuple6#d()}
     * @param <E> the type of {@link Tuple6#e()}
     * @param <F> the type of {@link Tuple6#f()}
     * @return a {@link Stream} containing every unique combination as a {@link Tuple6}
     * @apiNote 📎 You can use {@link java.util.EnumSet#allOf(Class)} to get an {@link Iterable} out of an {@link Enum}'s values.
     */
    @Nonnull
    @Contract(pure = true)
    public static <A, B, C, D, E, F> Stream<Tuple6<A, B, C, D, E, F>> cartesianProduct(
          Iterable<A> a,
          Iterable<B> b,
          Iterable<C> c,
          Iterable<D> d,
          Iterable<E> e,
          Iterable<F> f
    ) {
        return Streams.stream(a)
              .flatMap(a1 -> Streams.stream(b)
                    .flatMap(b1 -> Streams.stream(c)
                          .flatMap(c1 -> Streams.stream(d)
                                .flatMap(d1 -> Streams.stream(e)
                                      .flatMap(e1 -> Streams.stream(f)
                                            .map(f1 -> Tuple.of(a1, b1, c1, d1, e1, f1)
                                            )
                                      )
                                )
                          )
                    )
              );
    }

    /**
     * Returns all of the possible <i><b>ordered</b></i> pairs of {@link T} elements from the {@code source}. For example:
     *
     * <pre>{@code
     * Combinatorial.orderedPairs(List.of(1,2,3))
     *   .forEach(System.out::println);
     *
     * // (1, 2)
     * // (1, 3)
     * // (2, 1)
     * // (2, 3)
     * // (3, 1)
     * // (3, 2)
     *
     * }</pre>
     *
     * @param source every possible {@link T} element
     * @param <T>    the element type
     * @return a {@link Stream} containing every unique, <i><b>ordered</b></i> {@link Tuple2} of different elements
     * @see #unorderedPairs(Iterable)
     */
    public static <T> Stream<@NonNull Tuple2<T, T>> orderedPairs(Iterable<T> source) {
        return Streams.mapWithIndex(
                    Streams.stream(source),
                    (a, aIndex) -> Streams.mapWithIndex(
                          Streams.stream(source),
                          (b, bIndex) -> aIndex == bIndex ? null : Tuple.of(a, b)
                    )
              )
              .flatMap(Function.identity())
              .filter(Objects::nonNull);
    }

    /**
     * Returns all of the possible <i><b>un-ordered</b></i> pairs of distinct {@link T} elements from the source. For example:
     * <pre>{@code
     * Combinatorial.unorderedPairs(List.of(1, 2, 3))
     *   .forEach(System.out::println);
     *
     * // (1, 2)
     * // (1, 3)
     * // (2, 3)
     *
     * }</pre>
     *
     * @param source every possible {@link T} element
     * @param <T>
     * @return a {@link Stream} containing every unique, <b><i>un-ordered</i></b> {@link Tuple2} of different elements
     * @see #orderedPairs(Iterable)
     */
    public static <T> Stream<Tuple2<T, T>> unorderedPairs(Iterable<T> source) {
        return Streams.mapWithIndex(
                    Streams.stream(source),
                    (a, aIndex) -> Streams.mapWithIndex(
                          Streams.stream(source).skip(aIndex + 1),
                          (b, bIndex) -> Tuple.of(a, b)
                    )
              )
              .flatMap(Function.identity());
    }
}
