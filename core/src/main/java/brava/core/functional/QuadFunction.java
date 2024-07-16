package brava.core.functional;

import brava.core.tuples.Tuple4;

import java.util.function.Function;

/**
 * A function with 4 parameters.
 * Analogous to {@link java.util.function.BiFunction}.
 *
 * @param <A>   the first parameter type
 * @param <B>   the second parameter type
 * @param <C>   the third parameter type
 * @param <D>   the fourth parameter type
 * @param <OUT> the result type
 * @implNote According to <a href="https://en.wikipedia.org/wiki/Numeral_prefix">Wikipedia's article on numerical prefixes</a>,
 * the prefix for 4 that corresponds to {@code bi-} should be {@code tetra-}, but that doesn't sound nearly as cool.
 * <p/>
 * Actually, it might still be more accurate to use {@code quadra-}...but I like {@code quad-}, so deal with it.
 *
 * @see java.util.function.BiFunction
 * @see TriFunction
 * @see PentaFunction
 * @see HexaFunction
 */
@FunctionalInterface
public interface QuadFunction<A, B, C, D, OUT> extends Function<Tuple4<A, B, C, D>, OUT> {
    OUT apply(A a, B b, C c, D d);

    default OUT apply(Tuple4<A, B, C, D> args) {
        return apply(args.a(), args.b(), args.c(), args.d());
    }
}
