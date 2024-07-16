package brava.core.tuples;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;
import java.util.function.Function;

public record Tuple2<A, B>(A a, B b) implements Tuple<Tuple2<A, B>> {
    @Contract(pure = true)
    @Override
    public Object get(int index) {
        return switch (index) {
            case 0 -> a;
            case 1 -> b;
            default -> throw new IndexOutOfBoundsException(index);
        };
    }

    @Override
    public int size() {
        return 2;
    }

    /**
     * Adds a new element to the end of this tuple, making it a
     *
     * @param c
     * @param <C>
     * @return
     */
    public <C> Tuple3<A, B, C> append(C c) {
        return Tuple.of(a, b, c);
    }

    public <T> T reduce(BiFunction<A, B, T> function) {
        return function.apply(a, b);
    }

    public <A2, B2> Tuple2<A2, B2> map(
          Function<? super A, ? extends A2> aFunction,
          Function<? super B, ? extends B2> bFunction
    ) {
        return new Tuple2<>(
              aFunction.apply(a),
              bFunction.apply(b)
        );
    }

    @Override
    public @NotNull Tuple2<A, B> getSelf() {
        return this;
    }

    // Tuple2-only methods
    public <A2> Tuple2<A2, B> mapA(Function<A, A2> aFunction) {
        return new Tuple2<>(
              aFunction.apply(a),
              b
        );
    }

    public <B2> Tuple2<A, B2> mapB(Function<B, B2> bFunction) {
        return new Tuple2<>(
              a,
              bFunction.apply(b)
        );
    }
    //endregion
}
