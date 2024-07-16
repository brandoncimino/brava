package brava.core.tuples;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class Tuple2Tests {
    @Test
    void tuple2_map() {
        var mapper = TupleTestData.stringMapper;
        var original = Tuple.of("yolo", "swag");
        Tuple2<Integer, Number> expected = Tuple.of(
              mapper.apply(original.a()),
              mapper.apply(original.b())
        );

        Tuple2<Integer, Number> actual = original.map(
              TupleTestData.stringMapper,
              TupleTestData.stringMapper
        );

        Assertions.assertThat(actual)
              .isEqualTo(expected);
    }
}
