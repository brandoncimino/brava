package com.fasterxml.jackson.datatype.brava;

import brava.core.Either;
import brava.core.Which;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.MoreCollectors;
import com.google.common.collect.Streams;
import com.google.common.primitives.Primitives;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class EitherModuleHelpers {
    private static final System.Logger log = System.getLogger(EitherModule.class.getSimpleName());

    /**
     * @see EitherModule.Preference
     */
    public static Optional<Which> isAnnotatedAsPreference(@Nullable BeanProperty property) {
        return Optional.ofNullable(property)
            .map(it -> it.getAnnotation(EitherModule.Preference.class))
            .map(EitherModule.Preference::value);
    }

    /**
     * @implNote Similar to {@link TypeFactory#moreSpecificType(JavaType, JavaType)}, but when the two are unrelated, returns {@link Optional#empty()} instead of defaulting to {@code a}.
     */
    public static Optional<Which> isMoreSpecific(JavaType a, JavaType b) {
        return Which.isExclusivelyTrue(
            a.isTypeOrSubTypeOf(b.getRawClass()),
            b.isTypeOrSubTypeOf(a.getRawClass())
        );
    }

    /**
     * Jackson can construct objects from "simple" types like {@link Integer} and {@link String}
     * if a "{@link com.fasterxml.jackson.annotation.JsonCreator} - a constructor or factory method - is present with a single parameter of that "simple" type.
     * <p/>
     * For example, take the following class:
     * <pre>{@code
     * public final class HasInt {
     *     private final int value;
     *     public int getValue(){ return value; }
     *
     *     public HasInt(int value){
     *         this.value = value;
     *     }
     * }
     * }</pre>
     * A "normal" JSON for that type would look like this:
     * <pre>{@code
     * { "value": 99 }
     * }</pre>
     * However, it could <i>also</i> be deserialized from just:
     * <pre>{@code
     * 99
     * }</pre>
     * <p>
     * If you were deserializing {@code "99"} to {@code Either<Integer, HasInt>}, we wouldn't know which to type to use, because they'd both succeed.
     * This method causes us to prefer {@link Integer}, since it's probably what you wanted, and since round-trips would then provide the same input.
     */
    public static Optional<Which> isPrimitive(JavaType a, JavaType b) {
        return Which.isExclusivelyTrue(
            Primitives.unwrap(a.getRawClass()).isPrimitive(),
            Primitives.unwrap(b.getRawClass()).isPrimitive()
        );
    }


    public static Optional<Which> hasMoreMatchingProperties(
        DeserializationConfig config,
        JsonNode jsonNode,
        JavaType a,
        JavaType b
    ) {
        if (a.equals(b)) {
            return Optional.empty();
        }

        var aProperties = getMatchingProperties(config, a, jsonNode.fieldNames());
        var bProperties = getMatchingProperties(config, b, jsonNode.fieldNames());

        if (aProperties.size() != bProperties.size()) {
            return Which.isBigger(aProperties.size(), bProperties.size());
        }

        if (aProperties.keySet().equals(bProperties.keySet())) {
            log.log(
                System.Logger.Level.TRACE,
                "🅰 {} and 🅱 {} matched the same set of JSON fields, so we'll see which type each field prefers: {}", a,
                b, aProperties.keySet()
            );
            var fieldPreferences = aProperties.keySet().stream()
                .collect(Collectors.groupingBy(propName -> {
                    var aProp    = aProperties.get(propName).getPrimaryType();
                    var bProp    = bProperties.get(propName).getPrimaryType();
                    var propNode = jsonNode.get(propName);
                    return hasMoreMatchingProperties(config, propNode, aProp, bProp);
                }));

            var aPreferred = fieldPreferences.getOrDefault(Optional.of(Which.A), List.of());
            var bPreferred = fieldPreferences.getOrDefault(Optional.of(Which.B), List.of());
            log.log(
                System.Logger.Level.TRACE,
                """
                    Fields have chosen their preferences:
                      🅰 [{}] {}
                      🅱 [{}] {}
                    """,
                aPreferred.size(),
                aPreferred,
                bPreferred.size(),
                bPreferred
            );
            return Which.isBigger(aPreferred.size(), bPreferred.size());
        }

        log.log(System.Logger.Level.TRACE, "We were unable to find a preference between 🅰 {} and 🅱 {}", a, b);
        return Optional.empty();
    }

    @SuppressWarnings("java:S1452")
    public static Either<?, ?> createEither(Which whichHasValue, Object value) {
        return switch (whichHasValue) {
            case A -> Either.ofA(value);
            case B -> Either.ofB(value);
        };
    }

    /**
     * Calls {@link #findProperty(DeserializationConfig, JavaType, String)} for each of the given {@link JsonNode#fieldNames()},
     * and returns the ones that were found.
     *
     * @param config         the {@link DeserializationContext} being used
     * @param type           the {@link JavaType} in question
     * @param jsonFieldNames the {@link JsonNode#fieldNames()} we are looking for
     * @return a {@link Map} of {@link JsonNode#fieldNames()} → matching {@link BeanPropertyDefinition}s
     */
    @NotNull
    static Map<String, BeanPropertyDefinition> getMatchingProperties(
        DeserializationConfig config,
        JavaType type,
        Iterator<String> jsonFieldNames
    ) {
        assert !type.isPrimitive();

        return Streams.stream(jsonFieldNames)
            .flatMap(fieldName ->
                findProperty(config, type, fieldName)
                    .map(p -> Map.entry(fieldName, p))
                    .stream()
            )
            .collect(Collectors.toUnmodifiableMap(
                Map.Entry::getKey,
                Map.Entry::getValue
            ));
    }

    /**
     * @param config        the {@link DeserializationContext} being used
     * @param type          the type that might have the property
     * @param jsonFieldName the name of the field <b><i>in the JSON string</i></b>, which might match the property {@link #hasNameOrAlias(DeserializationConfig, BeanPropertyDefinition, String) name or alias}
     * @return the matching {@link BeanPropertyDefinition}, if any
     * @throws IllegalArgumentException if <b>two or more</b> properties matched the given {@code jsonFieldName}
     */
    @Contract(pure = true)
    private static Optional<BeanPropertyDefinition> findProperty(
        DeserializationConfig config,
        JavaType type,
        String jsonFieldName
    ) {
        return config.introspect(type)
            .findProperties()
            .stream()
            .filter(it -> hasNameOrAlias(config, it, jsonFieldName))
            .collect(MoreCollectors.toOptional());
    }

    /**
     * @param config      the {@link DeserializationConfig} being used <i>(📎 you can retrieve this from {@link DeserializationContext#getConfig()} or {@link ObjectMapper#getDeserializationConfig()})</i>
     * @param property    the Jackson {@link BeanPropertyDefinition} of the property in question
     * @param nameOrAlias the string you're looking for
     * @return {@code true} if the {@code nameOrAlias} matches the property's {@link BeanPropertyDefinition#hasName(PropertyName) name} or one of its {@link AnnotationIntrospector#findPropertyAliases(Annotated) aliases}
     */
    @Contract(pure = true)
    private static boolean hasNameOrAlias(
        @NotNull DeserializationConfig config,
        @NotNull BeanPropertyDefinition property,
        @NotNull String nameOrAlias
    ) {
        var pn = PropertyName.construct(nameOrAlias);
        return property.hasName(pn) || Stream.ofNullable(
                config.getAnnotationIntrospector().findPropertyAliases(property.getPrimaryMember()))
            .flatMap(Collection::stream)
            .anyMatch(pn::equals);
    }
}
