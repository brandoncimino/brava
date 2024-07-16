package brava.core.tuples;

import brava.core.functional.QuadFunction;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public record Tuple4<A, B, C, D>(A a, B b, C c, D d) implements Tuple {

    @Override
    public Object get(int index) {
        return switch (index) {
            case 0 -> a;
            case 1 -> b;
            case 2 -> c;
            case 3 -> d;
            default -> throw new IndexOutOfBoundsException(index);
        };
    }

    @Override
    public int size() {
        return 4;
    }

    public <E> Tuple5<A, B, C, D, E> append(E e) {
        return new Tuple5<>(a, b, c, d, e);
    }

    public <A2, B2, C2, D2> Tuple4<A2, B2, C2, D2> map(
          Function<? super A, ? extends A2> aFunction,
          Function<? super B, ? extends B2> bFunction,
          Function<? super C, ? extends C2> cFunction,
          Function<? super D, ? extends D2> dFunction
    ) {
        return new Tuple4<>(
              aFunction.apply(a),
              bFunction.apply(b),
              cFunction.apply(c),
              dFunction.apply(d)
        );
    }

    public <OUT> OUT reduce(QuadFunction<A, B, C, D, OUT> function) {
        return function.apply(this);
    }

    @Override
    public @NotNull Tuple4<A, B, C, D> getSelf() {
        return this;
    }
}
