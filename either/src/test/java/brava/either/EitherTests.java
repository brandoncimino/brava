package brava.either;

import brava.core.Combinatorial;
import com.google.common.base.Equivalence;
import com.google.common.base.Preconditions;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.WithAssertions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

class EitherTests implements WithAssertions {
    //region Tests

    @Test
    void either_rejectsBoth() {
        //noinspection DataFlowIssue
        assertThatThrownBy(() -> Either.of(1, "x"))
              .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void either_rejectsNeither() {
        //noinspection DataFlowIssue
        assertThatThrownBy(() -> Either.of(null, null))
              .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void either_acceptsA() {
        var a = UUID.randomUUID();
        var either = Either.of(a, null);
        EitherAssertions.validate(either, a, Which.A);
    }

    @Test
    void either_acceptsB() {
        var b = UUID.randomUUID();
        var either = Either.of(null, b);
        EitherAssertions.validate(either, b, Which.B);
    }

    @Test
    void either_ofA() {
        var a = UUID.randomUUID();
        var either = Either.ofA(a);
        EitherAssertions.validate(either, a, Which.A);
    }

    @Test
    void either_ofB() {
        var b = UUID.randomUUID();
        var either = Either.ofB(b);
        EitherAssertions.validate(either, b, Which.B);
    }

    @Test
    void either_resultOf_returnsResult() {
        var a = UUID.randomUUID();
        var either = Either.resultOf(() -> a);
        EitherAssertions.validate(either, a, Which.A);
    }

    @Test
    void either_resultOf_catchesErrors() {
        var exc = new AssertionError();
        var either = Either.resultOf(() -> {
            throw exc;
        });
        EitherAssertions.validate(either, exc, Which.B);
    }

    @Test
    void either_resultOf_givenNullCallable_doesNotCatch() {
        //noinspection DataFlowIssue
        assertThatCode(() -> Either.resultOf(null))
              .isInstanceOf(NullPointerException.class);
    }

    @Test
    void either_resultOf_givenNullResult_doesNotCatch() {
        //noinspection DataFlowIssue
        assertThatCode(() -> Either.resultOf(() -> null))
              .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void givenEither_whenHandle_thenCorrectFunctionIsInvoked() {
        var hasA = Either.ofA(1);
        var handledA = hasA.handle(
              a -> true,
              b -> Assertions.fail("Should not have been invoked!")
        );
        Assertions.assertThat(handledA).isTrue();

        var hasB = Either.ofB(2);
        var handledB = hasB.handle(
              a -> Assertions.fail("Should not have been invoked!"),
              b -> true
        );
        Assertions.assertThat(handledB).isTrue();
    }

    @Test
    void givenEither_whenMap_thenCorrectFunctionIsInvoked() {
        var hasA = Either.ofA(1);
        var mappedA = hasA.map(
              a -> true,
              b -> Assertions.fail("Should not have been invoked!")
        );

        EitherAssertions.validate(mappedA, true, Which.A);

        var hasB = Either.ofB(2);
        var mappedB = hasB.map(
              a -> Assertions.fail("Should not have been invoked!"),
              b -> true
        );

        EitherAssertions.validate(mappedB, true, Which.B);
    }

    @SuppressWarnings("EqualsBetweenInconvertibleTypes")
    // We'd get a similar warning if we compared `Optional.<String>empty().equals(Optional.<Integer>empty()`, which is valid and returns `true` - 
    // though not intuitive, it's a limitation of type erasure
    @Test
    void givenEithersWithSameValueButDifferentTypes_whenEquals_thenTrue() {
        Either<@NotNull String, @NotNull Integer> a = Either.ofA("a");
        Either<@NotNull CharSequence, @NotNull UUID> b = Either.ofA("a");

        var valueEquality = Objects.equals(a.getValue(), b.getValue());
        var eitherEquality = a.equals(b);
        Assertions.assertThat(eitherEquality)
              .isTrue()
              .as("Either.equals() matches Object.equals() when values are in the same position")
              .isEqualTo(valueEquality);
    }

    @Test
    void givenEithersWithDifferentValues_whenEquals_thenFalse() {
        var a = Either.ofA(1);
        var b = Either.ofA(2);

        Assertions.assertThat(a.equals(b)).isFalse();
    }

    @Test
    void givenEither_whenHashCode_thenUnderlyingValueHashCodeIsReturned() {
        var uuid = UUID.randomUUID();
        var either = Either.ofA(uuid);

        Assertions.assertThat(either.hashCode())
              .isEqualTo(uuid.hashCode());
    }

    @Test
    void givenEithersWithEqualValueInDifferentSlots_whenEquals_thenFalse() {
        var uuid = UUID.randomUUID();
        Either<@NotNull UUID, @NotNull UUID> first = Either.ofA(uuid);
        Either<@NotNull UUID, @NotNull UUID> second = Either.ofB(uuid);

        Assertions.assertThat(first.equals(second))
              .isFalse();
    }

    record EquivalenceData<T>(
          T firstValue,
          T secondValue,
          T notEqualValue,
          Equivalence<? super T> equivalence
    ) {
        static <T> EquivalenceData<T> of(
              T firstValue,
              T equalValue,
              T notEqualValue,
              Equivalence<? super T> equivalence
        ) {
            Preconditions.checkArgument(equivalence.equivalent(firstValue, equalValue), "%s == %s", firstValue, equalValue);
            Preconditions.checkArgument(!equivalence.equivalent(firstValue, notEqualValue), "%s != %s", firstValue, notEqualValue);
            return new EquivalenceData<>(firstValue, equalValue, notEqualValue, equivalence);
        }
    }

    record EquivalenceScenario<T>(EquivalenceData<T> data, Which whichHasValue, boolean expectedEquivalence) {
    }

    static Stream<EquivalenceScenario<?>> provideEquivalenceScenarios() {
        var datae = List.of(
              EquivalenceData.of(
                    "yolo",
                    "swag",
                    "",
                    Equivalence.equals().onResultOf(String::isEmpty)
              )
        );

        return Combinatorial.cartesianProduct(
              datae,
              EnumSet.allOf(Which.class),
              List.of(true, false)
        ).map(it -> new EquivalenceScenario<>(it.a(), it.b(), it.c()));
    }

    private static <T> Equivalence<T> throwingEquivalence() {
        return Equivalence.identity()
              .onResultOf(it -> Assertions.fail("This Equivalence comparer should not have been invoked!"));
    }

    //    @Test(dataProvider = "provideEquivalenceData")
    @ParameterizedTest
    @MethodSource("provideEquivalenceScenarios")
    <T> void givenEithersWithSameSlot_whenEqualsWithEquivalence_thenEquivalenceIsUsed(
          EquivalenceScenario<T> scenario
    ) {
        var secondValue = scenario.expectedEquivalence ? scenario.data.secondValue : scenario.data.notEqualValue;
        var first = EitherAssertions.createEither(scenario.whichHasValue, scenario.data.firstValue);
        var second = EitherAssertions.createEither(scenario.whichHasValue, secondValue);

        Equivalence<? super T> aEquiv = scenario.whichHasValue == Which.A ? scenario.data.equivalence : throwingEquivalence();
        Equivalence<? super T> bEquiv = scenario.whichHasValue == Which.B ? scenario.data.equivalence : throwingEquivalence();

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(
                        first.equals(
                              second,
                              aEquiv,
                              bEquiv
                        )
                  )
                  .as("via this.equals(other, Equivalence, Equivalence)")
                  .isEqualTo(scenario.expectedEquivalence);

            softly.assertThat(
                        Either.areEqual(
                              first,
                              second,
                              aEquiv,
                              bEquiv
                        )
                  )
                  .as("via Either.areEqual(first, second, Equivalence, Equivalence)")
                  .isEqualTo(scenario.expectedEquivalence);
        });

        Assertions.assertThat(
                    first.equals(
                          second,
                          scenario.whichHasValue == Which.A ? scenario.data.equivalence : throwingEquivalence(),
                          scenario.whichHasValue == Which.B ? scenario.data.equivalence : throwingEquivalence()
                    )
              )
              .isEqualTo(scenario.expectedEquivalence);
    }

    @Test
    void givenEithersWithDifferentSlots_whenEqualsWithEquivalence_thenEquivalenceIsNotInvoked() {
        Either<@NotNull Integer, @NotNull Integer> first = Either.ofA(1);
        Either<@NotNull Integer, @NotNull Integer> second = Either.ofB(1);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(
                        first.equals(
                              second,
                              throwingEquivalence(),
                              throwingEquivalence()
                        )
                  )
                  .as("via this.equals(other, Equivalence, Equivalence)")
                  .isFalse();

            softly.assertThat(
                        Either.areEqual(
                              first,
                              second,
                              throwingEquivalence(),
                              throwingEquivalence()
                        )
                  )
                  .as("via Either.areEqual(left, right, Equivalence, Equivalence)")
                  .isFalse();
        });
    }

    //endregion
}
