package brava.core;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BiTruthTests {
    @ParameterizedTest
    @CsvSource(
        {
            "true, true, BOTH",
            "true, false, A",
            "false, true, B",
            "false, false, NEITHER"
        }
    )
    void of(boolean a, boolean b, BiTruth expected) {
        Assertions.assertThat(BiTruth.of(a, b))
            .isEqualTo(expected);
    }
}
