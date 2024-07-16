package brava.core.tuples;

import brava.core.functional.TriFunction;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public record Tuple3<A, B, C>(A a, B b, C c) implements Tuple {
    @Override
    public Object get(int index) {
        return switch (index) {
            case 0 -> a;
            case 1 -> b;
            case 2 -> c;
            default -> throw new IndexOutOfBoundsException(index);
        };
    }

    @Override
    public int size() {
        return 3;
    }

    public <D> Tuple4<A, B, C, D> append(D d) {
        return new Tuple4<>(a, b, c, d);
    }

    public <OUT> OUT reduce(TriFunction<A, B, C, OUT> function) {
        return function.apply(this);
    }

    public <A2, B2, C2> Tuple3<A2, B2, C2> map(
          Function<? super A, ? extends A2> aFunction,
          Function<? super B, ? extends B2> bFunction,
          Function<? super C, ? extends C2> cFunction
    ) {
        return new Tuple3<>(
              aFunction.apply(a),
              bFunction.apply(b),
              cFunction.apply(c)
        );
    }

    @Override
    public @NotNull Tuple3<A, B, C> getSelf() {
        return this;
    }
}
