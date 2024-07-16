package brava.core.functional;

import brava.core.tuples.Tuple6;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * A {@link Function} with 6 parameters. Analogous to {@link java.util.function.BiFunction}.
 *
 * @param <A>   the first parameter type
 * @param <B>   the second parameter type
 * @param <C>   the third parameter type
 * @param <D>   the fourth parameter type
 * @param <E>   the fifth parameter type
 * @param <F>   the sixth parameter type
 * @param <OUT> the result type
 */
@FunctionalInterface
public interface HexaFunction<A, B, C, D, E, F, OUT> extends Function<Tuple6<A, B, C, D, E, F>, OUT> {
    /**
     * Applies the function to separate arguments.
     *
     * @return the resulting {@link OUT} value
     */
    OUT apply(A a, B b, C c, D d, E e, F f);

    @ApiStatus.NonExtendable
    default OUT apply(@NotNull Tuple6<A, B, C, D, E, F> args) {
        return apply(args.a(), args.b(), args.c(), args.d(), args.e(), args.f());
    }
}
