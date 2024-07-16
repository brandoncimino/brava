package brava.core.tuples;

import brava.core.functional.PentaFunction;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public record Tuple5<A, B, C, D, E>(A a, B b, C c, D d, E e) implements Tuple<Tuple5<A, B, C, D, E>> {
    @SuppressWarnings("DuplicatedCode")
    @Override
    public Object get(int index) {
        return switch (index) {
            case 0 -> a;
            case 1 -> b;
            case 2 -> c;
            case 3 -> d;
            case 4 -> e;
            default -> throw new IndexOutOfBoundsException(index);
        };
    }

    @Override
    public int size() {
        return 5;
    }

    @Override
    public @NotNull Tuple5<A, B, C, D, E> getSelf() {
        return this;
    }

    public <A2, B2, C2, D2, E2> Tuple5<A2, B2, C2, D2, E2> map(
          Function<? super A, ? extends A2> aFunction,
          Function<? super B, ? extends B2> bFunction,
          Function<? super C, ? extends C2> cFunction,
          Function<? super D, ? extends D2> dFunction,
          Function<? super E, ? extends E2> eFunction
    ) {
        return new Tuple5<>(
              aFunction.apply(a),
              bFunction.apply(b),
              cFunction.apply(c),
              dFunction.apply(d),
              eFunction.apply(e)
        );
    }

    public <OUT> OUT reduce(PentaFunction<A, B, C, D, E, OUT> function) {
        return function.apply(a, b, c, d, e);
    }
}
