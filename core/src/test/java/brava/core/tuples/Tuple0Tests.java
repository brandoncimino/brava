package brava.core.tuples;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class Tuple0Tests {
    @Test
    void tuple0_of() {
        var a = Tuple.of();
        var b = Tuple.of();
        Assertions.assertThat(a)
              .isSameAs(b)
              .isSameAs(Tuple0.instance());
    }

    @Test
    void tuple0_intern() {
        var a = Tuple.of();
        @SuppressWarnings("deprecation") 
        var b = new Tuple0();

        Assertions.assertThat(a)
              .isNotSameAs(b)
              .isSameAs(b.intern());
    }
}
