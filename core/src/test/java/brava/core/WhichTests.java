package brava.core;

import brava.core.collections.Combinatorial;
import brava.core.tuples.Tuple2;
import org.assertj.core.api.SoftAssertions;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

class WhichTests {
    public static Stream<Tuple2<Integer, Integer>> comparisons() {
        var numbers = List.of(Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 88);
        return Combinatorial.cartesianProduct(
              numbers,
              numbers
        );
    }

    @ParameterizedTest
    @MethodSource("comparisons")
    void comparisonTest(Tuple2<Integer, Integer> numbers) {
        final Optional<Which> expectedBigger;
        final Optional<Which> expectedSmaller;

        if (numbers.a() > numbers.b()) {
            expectedBigger = Optional.of(Which.A);
            expectedSmaller = Optional.of(Which.B);
        } else if (numbers.a() < numbers.b()) {
            expectedBigger = Optional.of(Which.B);
            expectedSmaller = Optional.of(Which.A);
        } else {
            expectedBigger = Optional.empty();
            expectedSmaller = Optional.empty();
        }

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(Which.isBigger(numbers.a(), numbers.b()))
                  .isEqualTo(expectedBigger);

            softly.assertThat(Which.isSmaller(numbers.a(), numbers.b()))
                  .isEqualTo(expectedSmaller);
        });
    }

    @CsvSource(value = {
          "true,  true,  null",
          "false, false, null",
          "true,  false, A",
          "false, true,  B"
    }, nullValues = "null")
    @ParameterizedTest
    void whichIsExclusiveTest(boolean a, boolean b, @Nullable Which nullableExpectation) {
        var expected = Optional.ofNullable(nullableExpectation);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(Which.isExclusivelyTrue(a, b))
                  .as("isExclusivelyTrue(%s, %s)", a, b)
                  .isEqualTo(expected);

            softly.assertThat(Which.isExclusivelyEqual(a, b, true))
                  .as("isExclusivelyEqual(%s, %s, %s)", a, b, true)
                  .isEqualTo(expected);

            softly.assertThat(Which.isExclusivelyEqual(a, b, false))
                  .as("isExclusivelyEqual(%s, %s, %s)", a, b, false)
                  .isEqualTo(expected.map(Which::other));

            var aStr = String.valueOf(a);
            var bStr = String.valueOf(b);
            var trueStr = String.valueOf(true);

            softly.assertThat(Which.isExclusivelyEqual(aStr, bStr, trueStr))
                  .as("isExclusivelyEqual(%s, %s, %s)", aStr.getClass().getSimpleName(), bStr.getClass().getSimpleName(), trueStr.getClass().getSimpleName())
                  .isEqualTo(expected);

            softly.assertThat(Which.isExclusivelyTrue(aStr, bStr, Boolean::parseBoolean))
                  .as("isExclusivelyTrue(%s, %s, Boolean::parseBoolean)", aStr.getClass().getSimpleName(), bStr.getClass().getSimpleName())
                  .isEqualTo(expected);
        });
    }
}
