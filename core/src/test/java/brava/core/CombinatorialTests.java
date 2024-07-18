package brava.core;

import brava.core.tuples.*;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class CombinatorialTests {
    private static final TypeToken<Tuple2<Character, Character>>                                             tuple2 = new TypeToken<>() { };
    private static final TypeToken<Tuple3<Character, Character, Character>>                                  tuple3 = new TypeToken<>() { };
    private static final TypeToken<Tuple4<Character, Character, Character, Character>>                       tuple4 = new TypeToken<>() { };
    private static final TypeToken<Tuple5<Character, Character, Character, Character, Character>>            tuple5 = new TypeToken<>() { };
    private static final TypeToken<Tuple6<Character, Character, Character, Character, Character, Character>> tuple6 = new TypeToken<>() { };

    private static int sizeOf(TypeToken<? extends Record> type) {
        return type.getRawType().getRecordComponents().length;
    }

    public record CartesianScenario<T extends Record>(
        TypeToken<T> tupleType,
        List<OptionSet> components
    ) {
        public CartesianScenario {
            var componentCount = tupleType.getRawType().getRecordComponents().length;
            Preconditions.checkArgument(
                componentCount == components.size(), "componentCount %s != elements.size() %s", componentCount,
                components.size()
            );
        }

        public List<Character> optionsFor(int componentIndex) {
            return components.get(componentIndex).list;
        }

        public List<T> expectedResults() {
            var elementsAsLists = components.stream()
                .map(comp ->
                    // `Lists.cartesianProduct()` disallows `null`, so we have to wrap everything inside of `Optional<>`s
                    comp.list
                        .stream()
                        .map(Optional::ofNullable)
                        .toList()
                )
                .toList();
            return Lists.cartesianProduct(elementsAsLists)
                .stream()
                .map(combo ->
                    // We need to unwrap the `Optional<>`s here back into nullable values
                    combo.stream()
                        .map(it -> it.orElse(null))
                        .toList()
                )
                .map(it -> Records.construct(tupleType, it))
                .toList();
        }

        /**
         * Creates a {@link CartesianScenario} with an {@link OptionSet} for each of {@link T}'s components.
         * <p>
         * If a component's index matches {@code emptyComponentIndex}, it's {@link OptionSet} will be empty;
         * otherwise, it will be generated with {@link OptionSet#createOptions(int, OptionSet.IncludeNull)}.
         *
         * @param tupleType
         * @param emptyComponentIndex
         * @param <T>
         * @return
         */
        private static <T extends Record> CartesianScenario<T> createOneScenario(
            TypeToken<T> tupleType,
            int emptyComponentIndex,
            OptionSet.IncludeNull includeNull
        ) {
            var components = IntStream.range(0, sizeOf(tupleType))
                .mapToObj(componentIndex ->
                    componentIndex == emptyComponentIndex
                    ? new OptionSet(List.of())
                    : OptionSet.createOptions(componentIndex + 1, includeNull)
                )
                .toList();

            return new CartesianScenario<>(tupleType, components);
        }

        public static <TUPLE extends Record> Stream<CartesianScenario<TUPLE>> createEmptyListScenarios(
            TypeToken<TUPLE> tupleType
        ) {
            return IntStream.range(0, sizeOf(tupleType))
                .mapToObj(emptyListIndex ->
                    createOneScenario(
                        tupleType,
                        emptyListIndex,
                        OptionSet.IncludeNull.NO
                    )
                );
        }

        private static <TUPLE extends Record> Stream<CartesianScenario<TUPLE>> createIncludeNullScenarios(TypeToken<TUPLE> tupleType) {
            return EnumSet.allOf(OptionSet.IncludeNull.class)
                .stream()
                .map(includeNull -> createOneScenario(tupleType, -1, includeNull));
        }

        public static <TUPLE extends Record> Stream<CartesianScenario<TUPLE>> createAllScenarios(TypeToken<TUPLE> tupleType) {
            return Stream.concat(
                createIncludeNullScenarios(tupleType),
                createEmptyListScenarios(tupleType)
            );
        }

        public int numberOfCombos() {
            return components.stream()
                .map(OptionSet::list)
                .mapToInt(List::size)
                .reduce((a, b) -> a * b)
                .orElseThrow(/* We should always have AT LEAST 1 component */);
        }

        @Override
        public String toString() {
            var hasNulls = components.stream()
                .map(OptionSet::list)
                .flatMap(Collection::stream)
                .anyMatch(Objects::isNull);

            var nullMessage = hasNulls ? "w/null" : "sans-null";

            return "%s°, %s combos, %s ⇐ %s".formatted(
                components.size(),
                numberOfCombos(),
                nullMessage,
                components.stream()
                    .map(OptionSet::list)
                    .map(List::size)
                    .map(Objects::toString)
                    .collect(Collectors.joining(", ", "[", "]"))
            );
        }

        /**
         * Represents the possible options for one of a cartesian product's axes.
         */
        public record OptionSet(List<Character> list) {
            private static final char min = 'a';
            private static final char max = 'z';

            public enum IncludeNull {
                YES,
                NO
            }

            /**
             * Creates a {@link #list()} that:
             * <ul>
             *     <li>Has a size of {@code thisComponentIndex}.</li>
             *     <li>Has all unique elements, from {@value #min} to {@value #max} (inclusive).</li>
             *     <li>If {@code includeNull} is {@code true}, replaces {@value #min} with {@code null}.</li>
             * </ul>
             *
             * <h1>Example</h1>
             * <pre>{@code
             * createOptions(3, IncludeNull.NO);  // => ['a',  'b', 'c']
             * createOptions(3, IncludeNull.YES); // => [null, 'b', 'c']
             * }</pre>
             *
             * @param numberOfOptions the size of the {@link #list()}
             * @return the new {@link OptionSet}
             */
            public static OptionSet createOptions(int numberOfOptions, IncludeNull includeNull) {
                int rangeMax = max + 1;
                Preconditions.checkArgument(numberOfOptions < rangeMax - min);
                var list = IntStream.range(min, rangeMax)
                    .limit(numberOfOptions)
                    .mapToObj(it -> (char) it)
                    .map(it -> includeNull == IncludeNull.YES && it == min ? null : it)
                    .toList()/* 📎 .toList(), unlike Collectors.toUnmodifiableList(), DOES allow null values */;

                return new OptionSet(list);
            }
        }
    }

    public static Stream<CartesianScenario<Tuple2<Character, Character>>> fold2_scenarios() {
        return CartesianScenario.createAllScenarios(tuple2);
    }

    //region Cartesian Products

    @ParameterizedTest
    @MethodSource("fold2_scenarios")
    void fold2_product(CartesianScenario<Tuple2<Character, Character>> data) {
        var actual = Combinatorial.cartesianProduct(
            data.optionsFor(0),
            data.optionsFor(1)
        );

        Assertions.assertThat(actual)
            .containsExactlyElementsOf(data.expectedResults());
    }

    public static Stream<CartesianScenario<Tuple3<Character, Character, Character>>> fold3_scenarios() {
        return CartesianScenario.createAllScenarios(tuple3);
    }

    @ParameterizedTest
    @MethodSource("fold3_scenarios")
    void fold3_product(CartesianScenario<Tuple3<Character, Character, Character>> data) {
        var actual = Combinatorial.cartesianProduct(
            data.optionsFor(0),
            data.optionsFor(1),
            data.optionsFor(2)
        );

        Assertions.assertThat(actual)
            .containsExactlyElementsOf(data.expectedResults());
    }

    public static Stream<CartesianScenario<Tuple4<Character, Character, Character, Character>>> fold4_scenarios() {
        return CartesianScenario.createAllScenarios(tuple4);
    }

    @ParameterizedTest
    @MethodSource("fold4_scenarios")
    void fold4_product(CartesianScenario<Tuple4<Character, Character, Character, Character>> data) {
        var actual = Combinatorial.cartesianProduct(
            data.optionsFor(0),
            data.optionsFor(1),
            data.optionsFor(2),
            data.optionsFor(3)
        );

        Assertions.assertThat(actual)
            .containsExactlyElementsOf(data.expectedResults());
    }

    public static Stream<CartesianScenario<Tuple5<Character, Character, Character, Character, Character>>> fold5_scenarios() {
        return CartesianScenario.createAllScenarios(tuple5);
    }

    @ParameterizedTest
    @MethodSource("fold5_scenarios")
    void fold5_product(CartesianScenario<Tuple5<Character, Character, Character, Character, Character>> data) {
        var actual = Combinatorial.cartesianProduct(
            data.optionsFor(0),
            data.optionsFor(1),
            data.optionsFor(2),
            data.optionsFor(3),
            data.optionsFor(4)
        );

        Assertions.assertThat(actual)
            .containsExactlyElementsOf(data.expectedResults());
    }

    public static Stream<CartesianScenario<Tuple6<Character, Character, Character, Character, Character, Character>>> fold6_scenarios() {
        return CartesianScenario.createAllScenarios(tuple6);
    }

    @ParameterizedTest
    @MethodSource("fold6_scenarios")
    void fold6_product(CartesianScenario<Tuple6<Character, Character, Character, Character, Character, Character>> data) {
        var actual = Combinatorial.cartesianProduct(
            data.optionsFor(0),
            data.optionsFor(1),
            data.optionsFor(2),
            data.optionsFor(3),
            data.optionsFor(4),
            data.optionsFor(5)
        );

        Assertions.assertThat(actual)
            .containsExactlyElementsOf(data.expectedResults());
    }

    //endregion

    //region Pairs
    public record ExpectedPairs<T>(
        List<T> source,
        List<Tuple2<T, T>> orderedPairs,
        List<Tuple2<T, T>> unorderedPairs
    ) {
        public static <T> ExpectedPairs<T> of(
            List<T> source,
            List<Tuple2<T, T>> orderedPairs,
            List<Tuple2<T, T>> unorderedPairs
        ) {
            return new ExpectedPairs<>(source, orderedPairs, unorderedPairs);
        }
    }

    public static Stream<ExpectedPairs<?>> provideExpectedPairs() {
        return Stream.of(
            ExpectedPairs.of(
                List.of(),
                List.of(),
                List.of()
            ),
            ExpectedPairs.of(
                List.of(1),
                List.of(),
                List.of()
            ),
            ExpectedPairs.of(
                List.of(1, 2),
                List.of(
                    Tuple.of(1, 2),
                    Tuple.of(2, 1)
                ),
                List.of(
                    Tuple.of(1, 2)
                )
            ),
            ExpectedPairs.of(
                List.of(1, 2, 3),
                List.of(
                    Tuple.of(1, 2),
                    Tuple.of(1, 3),
                    Tuple.of(2, 1),
                    Tuple.of(2, 3),
                    Tuple.of(3, 1),
                    Tuple.of(3, 2)
                ),
                List.of(
                    Tuple.of(1, 2),
                    Tuple.of(1, 3),
                    Tuple.of(2, 3)
                )
            ),
            ExpectedPairs.of(
                List.of(1, 1),
                List.of(
                    Tuple.of(1, 1),
                    Tuple.of(1, 1)
                ),
                List.of(
                    Tuple.of(1, 1)
                )
            ),
            ExpectedPairs.of(
                Arrays.asList("a", null),
                List.of(
                    Tuple.of("a", null),
                    Tuple.of(null, "a")
                ),
                List.of(
                    Tuple.of("a", null)
                )
            )
        );
    }

    @MethodSource("provideExpectedPairs")
    @ParameterizedTest
    <T> void unorderedPairs(ExpectedPairs<T> expectedPairs) {
        var actualPairs = Combinatorial.unorderedPairs(expectedPairs.source)
            .toList();

        Assertions.assertThat(actualPairs)
            .isEqualTo(expectedPairs.unorderedPairs);
    }

    @MethodSource("provideExpectedPairs")
    @ParameterizedTest
    <T> void orderedPairs(ExpectedPairs<T> expectedPairs) {
        var actualPairs = Combinatorial.orderedPairs(expectedPairs.source)
            .toList();

        Assertions.assertThat(actualPairs)
            .isEqualTo(expectedPairs.orderedPairs);
    }
    //endregion
}
