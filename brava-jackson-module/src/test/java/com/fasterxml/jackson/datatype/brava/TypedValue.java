package com.fasterxml.jackson.datatype.brava;

import brava.core.collections.Combinatorial;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.impl.UnsupportedTypeDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.ImmutableList;

import java.io.UncheckedIOException;
import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * Groups together a {@link #value} and a {@link TypeReference} for it.
 */
public record TypedValue<T>(@JsonValue T value, TypeReference<T> typeReference, String description) {
    public static final TypedValue<UUID>                             uuid                  = of(UUID.randomUUID(), new TypeReference<>() { });
    public static final TypedValue<List<UUID>>                       list_uuid             = of(List.of(UUID.randomUUID(), UUID.randomUUID()), new TypeReference<>() { });
    public static final TypedValue<Integer>                          integer               = of(1234567890, new TypeReference<>() { });
    public static final TypedValue<List<Integer>>                    list_integer          = of(List.of(1, 2, 3), new TypeReference<>() { });
    public static final TypedValue<BigInteger>                       bigInteger            = of(
        BigInteger.valueOf(Long.MAX_VALUE /*📎 To be unambiguous, this needs to have a value out-of-bounds for a normal `int`*/), new TypeReference<>() { });
    public static final TypedValue<Double>                           double_wrapper        = of(4.5, new TypeReference<Double>() { });
    public static final TypedValue<String>                           string                = of("yolo", new TypeReference<String>() { });
    public static final TypedValue<Map<String, Integer>>             map_string_integer    = of(Map.of("swag", 1), new TypeReference<Map<String, Integer>>() { });
    public static final TypedValue<List<String>>                     list_string           = of(List.of("list", "of", "strings"), new TypeReference<>() { });
    public static final TypedValue<Iterable<? extends CharSequence>> iterable_charSequence = of(List.of("iterable", "of", "? extends CharSequence"), new TypeReference<>() { });

    public static <T> TypedValue<T> of(T value, TypeReference<T> typeReference, String description) {
        return new TypedValue<>(value, typeReference, description);
    }

    public static <T> TypedValue<T> of(T value, TypeReference<T> typeReference) {
        return new TypedValue<>(value, typeReference, "");
    }

    public <C> TypedValue<C> collected(Collector<T, ?, C> collector, TypeReference<C> collectedType) {
        return collected(this, collector, collectedType);
    }

    public static <T, C> TypedValue<C> collected(
        TypedValue<T> value,
        Collector<T, ?, C> collector,
        TypeReference<C> collectedType
    ) {
        var collectedValue = Stream.of(value.value)
            .collect(collector);

        return of(collectedValue, collectedType, "%s of %s".formatted(getShortTypeName(collectedType), value.description));
    }

    //region Properties & functions

    public static JavaType getJavaType(TypeReference<?> typeReference) {
        return TypeFactory.defaultInstance().constructType(typeReference);
    }

    public JavaType javaType() {
        return getJavaType(typeReference);
    }

    public static String getShortTypeName(TypeReference<?> typeReference) {
        var possiblyWithEnclosingClass = typeReference.getType()
            .getTypeName()
            .replace(getJavaType(typeReference).getRawClass().getPackageName() + ".", "");

        var dollarSignIndex = possiblyWithEnclosingClass.indexOf('$');

        if (dollarSignIndex != -1) {
            return possiblyWithEnclosingClass.substring(dollarSignIndex + 1);
        }

        return possiblyWithEnclosingClass;
    }

    public String shortTypeName() {
        return getShortTypeName(typeReference);
    }

    @Override
    public String toString() {
        var str = String.format("[%s]%s", shortTypeName(), value);
        if (description != null) {
            str += description;
        }
        return str;
    }

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder()
        .configure(SerializationFeature.INDENT_OUTPUT, true)
        .build();

    public String toJson() {
        try {
            return JSON_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    //endregion

    /**
     * @return a list of {@link TypedValue}s that, for all of their {@link Combinatorial#orderedPairs(Iterable)}, we expect the {@link EitherModule} to successfully distinguish betwixt
     */
    public static ImmutableList<TypedValue<?>> unambiguousTypes() {
        return ImmutableList.of(
            uuid, ExampleTypes.HasUUID.typedValue, list_uuid, integer, list_integer, ExampleTypes.HasInt.typedValue, bigInteger, ExampleTypes.HasValue.typedValue_integer,
            ExampleTypes.HasValue.typedValue_uuid
        );
    }

    /**
     * @return a list of {@link TypedValue}s that can be both serialized and deserialized by {@link JsonMapper} <b><i>by default</i></b> <i>(i.e. without any {@link com.fasterxml.jackson.core.util.JacksonFeature}s or {@link com.fasterxml.jackson.databind.Module}s)</i>
     */
    public static ImmutableList<TypedValue<?>> supportedTypes() {
        return Stream.concat(unambiguousTypes().stream(), Stream.of(double_wrapper, string, map_string_integer)).collect(ImmutableList.toImmutableList());
    }

    /**
     * @return a list of {@link TypedValue}s that, <i><b>by default</b></i>, cannot be serialized or deserialized by {@link JsonMapper} <i>(specifically, doing so will throw an {@link InvalidDefinitionException})</i>
     */
    public static ImmutableList<TypedValue<?>> unsupportedTypes() {
        return ImmutableList.of(
            of(
                ImmutableList.of(),
                new TypeReference<>() { },
                "Jackson can't handle non-standard collection types by default"
            ),
            of(
                Instant.now(),
                new TypeReference<>() { },
                String.format("`Instant` technically HAS a deserializer, %s, but that deserializer always throws an `%s` at the last second", UnsupportedTypeDeserializer.class.getSimpleName(),
                    InvalidDefinitionException.class.getSimpleName()
                )
            ),
            ExampleTypes.IgnoredConstructor.typedValue,
            ExampleTypes.OnlyMultiArgConstructor.typedValue,
            of(Function.identity(), new TypeReference<>() { }, "Jackson can't handle interfaces without you telling it a concrete implementation to use")
        );
    }
}
