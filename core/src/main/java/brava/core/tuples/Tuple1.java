package brava.core.tuples;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.stream.Stream;

public record Tuple1<A>(A a) implements Tuple<Tuple1<A>> {
    @Override
    public Object get(int index) {
        return switch (index) {
            case 0 -> a;
            default -> throw new IndexOutOfBoundsException(index);
        };
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public Stream<Object> stream() {
        // 📎 Single-item streams have a special optimization.
        return Stream.of(a);
    }

    public <B> Tuple2<A, B> append(B b) {
        return Tuple.of(a, b);
    }

    @Override
    public @NotNull Tuple1<A> getSelf() {
        return this;
    }

    public <A2> Tuple1<A2> map(Function<? super A, ? extends A2> aFunction) {
        return Tuple.of(aFunction.apply(a));
    }
}
