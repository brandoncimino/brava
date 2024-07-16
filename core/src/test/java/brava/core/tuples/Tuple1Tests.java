package brava.core.tuples;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

class Tuple1Tests {
    static final Function<CharSequence, Integer> stringMapper = CharSequence::length;

    @Test
    void tuple1_of() {
        var a = new Tuple1<>("yolo");
        var b = Tuple.of("yolo");
        Assertions.assertThat(a)
              .isEqualTo(b);
    }

    @Test
    void tuple1_append() {
        var original = Tuple.of("yolo");
        var additionalValue = 99;
        var expected = Tuple.of(original.a(), additionalValue);

        Assertions.assertThat(original.append(additionalValue))
              .isEqualTo(expected);
    }

    @Test
    void tuple1_map() {
        var original = Tuple.of("yolo");
        Tuple1<Number> expected = Tuple.of(original.a().length());
        Tuple1<Number> actual = original.map(TupleTestData.stringMapper);

        Assertions.assertThat(actual)
              .isEqualTo(expected);
    }
}
