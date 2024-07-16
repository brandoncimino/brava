package brava.core.tuples;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

class TupleMapTests {
    private static final String input = "yolo";
    /**
     * <h1>IMPORTANT</h1>
     * <ul>
     *     <li>The function input type should be a PARENT of {@link #input}.</li>
     *     <li>The function output type should be a CHILD of {@link #output}.</li>
     * </ul>
     */
    private static final Function<CharSequence, Integer> mapper = CharSequence::length;
    private static final Number output = mapper.apply(input);

    @Test
    void tuple1_map() {
        var original = Tuple.of(input);
        var expected = Tuple.of(output);
        Tuple1<Number> actual = original.map(mapper);

        Assertions.assertThat(actual)
              .isEqualTo(expected);
    }

    @Test
    void tuple2_map() {
        var original = Tuple.of(input, input);
        var expected = Tuple.of(output, output);
        Tuple2<Number, Number> actual = original.map(mapper, mapper);

        Assertions.assertThat(actual)
              .isEqualTo(expected);
    }

    @Test
    void tuple3_map() {
        var original = Tuple.of(input, input, input);
        var expected = Tuple.of(output, output, output);
        Tuple3<Number, Number, Number> actual = original.map(mapper, mapper, mapper);

        Assertions.assertThat(actual)
              .isEqualTo(expected);
    }

    @Test
    void tuple4_map() {
        var original = Tuple.of(input, input, input, input);
        var expected = Tuple.of(output, output, output, output);
        Tuple4<Number, Number, Number, Number> actual = original.map(mapper, mapper, mapper, mapper);

        Assertions.assertThat(actual)
              .isEqualTo(expected);
    }

    @Test
    void tuple5_map() {
        var original = Tuple.of(input, input, input, input, input);
        var expected = Tuple.of(output, output, output, output, output);
        Tuple5<Number, Number, Number, Number, Number> actual = original.map(mapper, mapper, mapper, mapper, mapper);

        Assertions.assertThat(actual)
              .isEqualTo(expected);
    }

    @Test
    void tuple6_map() {
        var original = Tuple.of(input, input, input, input, input, input);
        var expected = Tuple.of(output, output, output, output, output, output);
        Tuple6<Number, Number, Number, Number, Number, Number> actual = original.map(mapper, mapper, mapper, mapper, mapper, mapper);

        Assertions.assertThat(actual)
              .isEqualTo(expected);
    }
}
