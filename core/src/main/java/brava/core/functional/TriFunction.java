package brava.core.functional;

import brava.core.tuples.Tuple3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * A {@link Function} with 3 parameters. Analogous to {@link java.util.function.BiFunction}.
 *
 * @param <A>   the first parameter type
 * @param <B>   the second parameter type
 * @param <C>   the third parameter type
 * @param <OUT> the result type
 */
@FunctionalInterface
public interface TriFunction<A, B, C, OUT> extends Function<Tuple3<A, B, C>, OUT> {
    OUT apply(A a, B b, C c);

    @ApiStatus.NonExtendable
    default OUT apply(@NotNull Tuple3<A, B, C> args) {
        return apply(args.a(), args.b(), args.c());
    }
}
