package brava.core.functional;

import brava.core.tuples.Tuple5;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * A {@link Function} with 5 parameters. Analogous to {@link java.util.function.BiFunction}.
 *
 * @param <A>   the first parameter type
 * @param <B>   the second parameter type
 * @param <C>   the third parameter type
 * @param <D>   the fourth parameter type
 * @param <E>   the fifth parameter type
 * @param <OUT> the result type
 *
 * @see java.util.function.BiFunction
 * @see TriFunction
 * @see QuadFunction
 * @see HexaFunction
 */
public interface PentaFunction<A, B, C, D, E, OUT> extends Function<Tuple5<A, B, C, D, E>, OUT> {
    OUT apply(A a, B b, C c, D d, E e);

    @ApiStatus.NonExtendable
    default OUT apply(@NotNull Tuple5<A, B, C, D, E> tuple) {
        return apply(tuple.a(), tuple.b(), tuple.c(), tuple.d(), tuple.e());
    }
}
