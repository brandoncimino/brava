package com.fasterxml.jackson.datatype.brava;


import brava.core.Either;
import brava.core.Unchecked;
import brava.core.Which;
import brava.core.collections.Combinatorial;
import brava.core.tuples.Tuple2;
import brava.either.EitherAssertions;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.cfg.MapperBuilder;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.brava.EitherTestData.EitherInfo;
import com.fasterxml.jackson.datatype.brava.ExampleTypes.HasEither;
import com.google.common.base.Preconditions;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.api.SoftAssertions;
import org.jetbrains.annotations.Contract;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public final class EitherModuleTests {
    public record EitherScenario<A, B>(EitherInfo<? extends A, ? extends B> info, Which which) {
        public static <A, B> EitherScenario<? super A, ? super B> of(Tuple2<? extends EitherInfo<? extends A, ? extends B>, Which> scenario) {
            return new EitherScenario<>(scenario.a(), scenario.b());
        }
    }

    @Contract("-> new")
    private static JsonMapper createMapper() {
        return JsonMapper.builder()
            .addModule(new EitherModule())
            .configure(JsonReadFeature.ALLOW_MISSING_VALUES, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .build();
    }

    public static Stream<EitherScenario<?, ?>> provideSupportedTypes() {
        var eitherInfos = Combinatorial.orderedPairs(TypedValue.supportedTypes())
            .map(it -> EitherInfo.of(it.a(), it.b()))
            .toList();

        return Combinatorial.cartesianProduct(
                eitherInfos,
                EnumSet.allOf(Which.class)
            )
            .map(EitherScenario::of);
    }

    @ParameterizedTest
    @MethodSource("provideSupportedTypes")
    void givenEither_whenSerialized_thenSerializedAsValue(EitherScenario<?, ?> scenario) throws Exception {
        var mapper = createMapper();
        var value = scenario.info.get(scenario.which).value();
        var either = scenario.which == Which.A ? Either.ofA(value) : Either.ofB(value);

        var valueJson = mapper.writeValueAsString(value);
        var eitherJson = mapper.writeValueAsString(either);

        Assertions.assertThat(eitherJson)
            .as("the %s json is the same as the underlying %s json", scenario.info.eitherType(), scenario.info.get(scenario.which).javaType())
            .isEqualTo(valueJson);
    }

    @Test
    void givenJsonOfNeither_whenDeserializedToEither_thenExceptionIsThrown() throws Exception {
        var mapper = createMapper();
        var uuid = UUID.randomUUID();
        var json = mapper.writeValueAsString(uuid);

        Assertions.assertThatCode(() -> mapper.readValue(
            json,
            new TypeReference<Either<Integer, Float>>() {
            }
        )).isInstanceOf(MismatchedInputException.class);
    }

    @Test
    void givenAmbiguousJson_whenPropertyHasAnnotatedPreference_thenAnnotatedPreferenceIsUsed() throws Exception {
        @SuppressWarnings("Convert2Diamond" /* Just for safety, 'cus we're dealing with lots of type nonsense here */)
        var value = new ExampleTypes.HasAnnotatedPreference<BigInteger, BigDecimal>(
            Either.ofA(BigInteger.TEN),
            Either.ofB(BigDecimal.TEN)
        );

        var mapper = createMapper();
        var json = mapper.writeValueAsString(value);

        var fromJson = mapper.readValue(json, new TypeReference<ExampleTypes.HasAnnotatedPreference<BigInteger, BigDecimal>>() {
        });

        Assertions.assertThat(fromJson)
            .isEqualTo(value);
    }

    @Test
    void givenAmbiguousJson_whenPropertyHasAnnotatedPreference_thenAnnotatedPreferenceIsUsed_RECORD() throws Exception {
        @SuppressWarnings("Convert2Diamond" /* Just for safety, 'cus we're dealing with lots of type nonsense here */)
        var value = new ExampleTypes.HasAnnotatedPreference.HasAnnotatedPreference_Record<BigInteger, BigDecimal>(
            Either.ofA(BigInteger.TEN),
            Either.ofB(BigDecimal.TEN)
        );

        var mapper = createMapper();
        var json   = mapper.writeValueAsString(value);

        var fromJson = mapper.readValue(json, new TypeReference<ExampleTypes.HasAnnotatedPreference.HasAnnotatedPreference_Record<BigInteger, BigDecimal>>() {
        });

        Assertions.assertThat(fromJson)
            .isEqualTo(value);
    }

    public static Stream<EitherInfo<?, ?>> provideUnambiguousTypes_noWhich() {
        return Combinatorial.orderedPairs(TypedValue.unambiguousTypes())
            .map(it -> EitherInfo.of(it.a(), it.b()));
    }

    @ParameterizedTest
    @MethodSource("provideUnambiguousTypes_noWhich")
    void givenJsonListOfUnambiguousTypes_whenDeserializedToListOfEither_thenEithersAreConstructed(EitherInfo<?, ?> eitherInfo) throws Exception {
        var mapper = createMapper();
        var list = List.of(eitherInfo.a().value(), eitherInfo.b().value());
        var json = createMapper().writeValueAsString(list);

        var listType = mapper.getTypeFactory().constructCollectionLikeType(List.class, eitherInfo.eitherType());
        List<Either<?, ?>> fromJson = mapper.readValue(json, listType);

        Assertions.assertThat(fromJson)
            .containsExactly(Either.ofA(eitherInfo.a().value()), Either.ofB(eitherInfo.b().value()));

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(fromJson)
                .first()
                .isEqualTo(Either.ofA(eitherInfo.a().value()));

            softly.assertThat(fromJson)
                .last()
                .isEqualTo(Either.ofB(eitherInfo.b().value()));
        });
    }

    //region 

    public static Stream<Arguments> provideUnsupportedTypes() {
        return Combinatorial.cartesianProduct(
                TypedValue.unsupportedTypes(),
                TypedValue.unambiguousTypes(),
                EnumSet.allOf(Which.class)
            )
            .map(it -> Arguments.of(it.a(), it.b(), it.c()));
    }

    @ParameterizedTest
    @MethodSource("provideUnsupportedTypes")
    void givenMissingTypeIsUnsupported_whenDeserializedToEither_thenDeserializationFails(
        TypedValue<?> unsupportedType,
        TypedValue<?> supportedType,
        Which whichIsSupported
    ) throws Exception {
        var mapper = createMapper();
        var value = supportedType.value();
        var json  = mapper.writeValueAsString(value);

        // Parameter validation

        EitherTestData.requireTypeSupport(true, mapper, json, supportedType.javaType());
        EitherTestData.requireTypeSupport(false, mapper, json, unsupportedType.javaType());

        //endregion

        var aType = whichIsSupported == Which.A ? supportedType : unsupportedType;
        var bType = whichIsSupported == Which.B ? supportedType : unsupportedType;
        var eitherType = EitherInfo.of(aType, bType).eitherType();

        var result = Either.resultOf(() -> mapper.readValue(json, eitherType));
        Assertions.assertThat(result)
            .as("Json %s -> Either<%s, %s>", json, aType.javaType().getRawClass().getSimpleName(), bType.javaType().getRawClass().getSimpleName())
            .extracting(Either::tryGetB, InstanceOfAssertFactories.OPTIONAL)
            .containsInstanceOf(InvalidDefinitionException.class);
    }

    //endregion

    //region

    public static Stream<EitherScenario<?, ?>> provideUnambiguousTypes() {
        var eitherInfos = Combinatorial.orderedPairs(TypedValue.unambiguousTypes())
            .map(it -> EitherInfo.of(it.a(), it.b()))
            .toList();

        return Combinatorial.cartesianProduct(eitherInfos, EnumSet.allOf(Which.class))
            .map(EitherScenario::of);
    }

    @ParameterizedTest
    @MethodSource("provideUnambiguousTypes")
    void givenUnambiguousJson_whenDeserializedToEither_thenCorrectEitherIsCreated(EitherScenario<?, ?> scenario) throws Exception {
        var mapper = createMapper();
        var value = scenario.info.get(scenario.which).value();
        var json  = mapper.writeValueAsString(value);

        Either<?, ?> actual = mapper.readValue(json, scenario.info.eitherType());
        Assertions.assertThat(actual)
            .isEqualTo(scenario.info.createEither(scenario.which));
    }

    private static void assertAmbiguousJsonToEither(
        TypedValue<?> preferredType,
        TypedValue<?> otherType
    ) {
        var mapper = createMapper();
        var json   = Unchecked.get(() -> mapper.writeValueAsString(preferredType.value()));

        var toPreferred = Either.resultOf(() -> mapper.readValue(json, preferredType.javaType()));
        var toOther     = Either.resultOf(() -> mapper.readValue(json, otherType.javaType()));

        Preconditions.checkArgument(
            toPreferred.hasA() && toOther.hasA(),
            """
                The JSON must be deserializable to BOTH types!
                  🅰 %s
                  🅱 %s
                  JSON
                  ----
                  %s
                  ----
                """,
            toPreferred,
            toOther,
            json
        );

        var expectedValue = toPreferred.getA();

        Either<?, ?> aPreferred = Unchecked.get(() -> mapper.readValue(json, EitherInfo.of(preferredType, otherType).eitherType()));
        Either<?, ?> bPreferred = Unchecked.get(() -> mapper.readValue(json, EitherInfo.of(otherType, preferredType).eitherType()));

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(aPreferred)
                .as("%s is preferred over %s (while in 🅰️ position)", preferredType, otherType)
                .satisfies(it -> EitherAssertions.validate(
                    it,
                    expectedValue,
                    Which.A
                ));

            softly.assertThat(bPreferred)
                .as("%s is preferred over %s (while in 🅱️ position)", preferredType, otherType)
                .satisfies(it -> EitherAssertions.validate(
                    it,
                    expectedValue,
                    Which.B
                ));
        });
    }

    @Test
    void givenAmbiguousJson_preferPrimitiveType() {
        assertAmbiguousJsonToEither(
            TypedValue.integer,
            ExampleTypes.AsInt.typedValue
        );
    }

    @Test
    void givenAmbiguousJson_preferStrictSubtype() {
        assertAmbiguousJsonToEither(
            ExampleTypes.Child.typedValue,
            ExampleTypes.Parent.typedValue
        );
    }

    @Test
    void givenAmbiguousJson_preferTypeWithMoreMatchingProperties() {
        assertAmbiguousJsonToEither(
            ExampleTypes.FirstAndLastName.typedValue,
            ExampleTypes.FirstNameOnly.typedValue
        );
    }

    //endregion


    /**
     * Jackson deserializers treat values of object properties differently from other generic types - see
     * {@link com.fasterxml.jackson.databind.deser.ContextualDeserializer#createContextual(DeserializationContext, BeanProperty)}
     */
    @ParameterizedTest
    @EnumSource
    void givenEitherInProperty_whenDeserialized_thenEitherIsConstructed(Which which) throws Exception {
        var mapper = createMapper();
        var uuid = UUID.randomUUID();

        var hasEitherType = which == Which.A
                            ? new TypeReference<HasEither<UUID, Integer>>() {
        }
                            : new TypeReference<HasEither<Integer, UUID>>() {
        };

        var             either    = EitherModuleHelpers.createEither(which, uuid);
        var             hasEither = new HasEither<>(either);
        var             json      = mapper.writeValueAsString(hasEither);
        HasEither<?, ?> fromJson  = mapper.readValue(json, hasEitherType);
        Assertions.assertThat(fromJson)
            .hasSameClassAs(hasEither)
            .isEqualTo(hasEither)
            .extracting(ExampleTypes.HasEither::getValue)
            .satisfies(it -> EitherAssertions.validate(it, uuid, which));
    }

    @Test
    void givenModuleInMagicalResourceFile_whenFindModules_thenModuleIsFound() {
        // Make sure the file actually exists
        var magicalResourceFilePath = "META-INF/services/com.fasterxml.jackson.databind.Module";
        Thread.currentThread().getContextClassLoader().resources(magicalResourceFilePath)
            .findAny()
            .orElseThrow();

        var modules = MapperBuilder.findModules();
        Assertions.assertThat(modules)
            .satisfiesOnlyOnce(it -> Assertions.assertThat(it).isInstanceOf(EitherModule.class));
    }

    //region deduceByProperties

    public static Stream<EitherScenario<?, ?>> provideDeducibleByProperties() {
        var types = List.of(EitherTestData.SuccessResponse.TYPED_VALUE, EitherTestData.ErrorResponse.TYPED_VALUE);

        var eitherInfos = Combinatorial.orderedPairs(types)
            .map(it -> EitherInfo.of(it.a(), it.b()))
            .toList();

        return Combinatorial.cartesianProduct(
                eitherInfos,
                EnumSet.allOf(Which.class)
            )
            .map(EitherScenario::of);
    }

    @ParameterizedTest
    @MethodSource("provideDeducibleByProperties")
    void deduceByPropertiesIsRecursiveTest(EitherScenario<?, ?> scenario) throws Exception {
        var mapper = JsonMapper.builder()
            .configure(JsonReadFeature.ALLOW_MISSING_VALUES, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .build();

        var value = scenario.info.get(scenario.which).value();
        var json  = mapper.writeValueAsString(value);
        var parentType = EitherTestData.requireSameParent(scenario.info.a().javaType(), scenario.info.b().javaType());

        Assertions.assertThatCode(() -> mapper.readValue(json, parentType))
            .as(
                """
                    Jackson should NOT be able to deduce betwixt the 2 children of %s:
                      🅰 %s
                      🅱 %s
                    """,
                parentType,
                scenario.info.a().javaType(),
                scenario.info.b().javaType()
            )
            .isInstanceOf(InvalidDefinitionException.class);

        var jsonNode = mapper.readTree(json);

        var deduced = EitherModuleHelpers.hasMoreMatchingProperties(
            mapper.getDeserializationConfig(),
            jsonNode,
            scenario.info.a().javaType(),
            scenario.info.b().javaType()
        );

        Assertions.assertThat(deduced)
            .contains(scenario.which == Which.A ? Which.A : Which.B);
    }

    //endregion
}
