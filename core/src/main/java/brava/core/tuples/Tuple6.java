package brava.core.tuples;

import brava.core.functional.HexaFunction;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public record Tuple6<A, B, C, D, E, F>(A a, B b, C c, D d, E e, F f) implements Tuple<Tuple6<A, B, C, D, E, F>> {
    @SuppressWarnings("DuplicatedCode")
    @Override
    public Object get(int index) {
        return switch (index) {
            case 0 -> a;
            case 1 -> b;
            case 2 -> c;
            case 3 -> d;
            case 4 -> e;
            case 5 -> f;
            default -> throw new IndexOutOfBoundsException(index);
        };
    }

    @Override
    public int size() {
        return 6;
    }

    @Override
    public @NotNull Tuple6<A, B, C, D, E, F> getSelf() {
        return this;
    }

    public <A2, B2, C2, D2, E2, F2> Tuple6<A2, B2, C2, D2, E2, F2> map(
          Function<? super A, ? extends A2> aFunction,
          Function<? super B, ? extends B2> bFunction,
          Function<? super C, ? extends C2> cFunction,
          Function<? super D, ? extends D2> dFunction,
          Function<? super E, ? extends E2> eFunction,
          Function<? super F, ? extends F2> fFunction
    ) {
        return new Tuple6<>(
              aFunction.apply(a),
              bFunction.apply(b),
              cFunction.apply(c),
              dFunction.apply(d),
              eFunction.apply(e),
              fFunction.apply(f)
        );
    }

    public <OUT> OUT reduce(HexaFunction<A, B, C, D, E, F, OUT> function) {
        return function.apply(a, b, c, d, e, f);
    }
}
