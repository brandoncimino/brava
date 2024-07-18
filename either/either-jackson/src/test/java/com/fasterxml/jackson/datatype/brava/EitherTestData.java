package com.fasterxml.jackson.datatype.brava;

import brava.core.collections.Combinatorial;
import brava.either.Either;
import brava.either.Which;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.impl.UnsupportedTypeDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assumptions;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Contains data used to test {@link Either}s.
 */
public final class EitherTestData {
    //region Test data holders

    /**
     * Groups together a {@link #value} and a {@link TypeReference} for it.
     */
//    @AllArgsConstructor(staticName = "of")
//    @RequiredArgsConstructor(staticName = "of")
    public record TypedValue<T>(@JsonValue T value, TypeReference<T> typeReference, String description) {
        public static <T> TypedValue<T> of(T value, TypeReference<T> typeReference, String description) {
            return new TypedValue<>(value, typeReference, description);
        }

        public static <T> TypedValue<T> of(T value, TypeReference<T> typeReference) {
            return new TypedValue<>(value, typeReference, "");
        }

        public JavaType javaType() {
            return TypeFactory.defaultInstance().constructType(typeReference);
        }

        public String shortTypeName() {
            return typeReference.getType()
                  .getTypeName()
                  .replace(javaType().getRawClass().getPackageName() + ".", "")
                  .replace(EitherTestData.class.getSimpleName() + "$", "");
        }

        @Override
        public String toString() {
            var str = String.format("[%s]%s", shortTypeName(), value);
            if (description != null) {
                str += description;
            }
            return str;
        }

        /**
         * @return a list of {@link TypedValue}s that, for all of their {@link Combinatorial#orderedPairs(Iterable)}, we expect the {@link EitherModule} to successfully distinguish
         */
        public static ImmutableList<TypedValue<?>> unambiguousTypes() {
            return ImmutableList.of(
                  of(UUID.randomUUID(), new TypeReference<>() {
                  }),
                  of(new HasUUID(UUID.randomUUID()), new TypeReference<>() {
                  }),
                  of(List.of(UUID.randomUUID(), UUID.randomUUID()), new TypeReference<>() {
                  }),
                  of(1234567890, new TypeReference<>() {
                  }),
                  of(List.of(1, 2, 3), new TypeReference<>() {
                  }),
                  of(new HasInt(100), new TypeReference<>() {
                  }),
                  of(BigInteger.valueOf(Long.MAX_VALUE /*📎 To be unambiguous, this needs to have a value out-of-bounds for a normal `int`*/), new TypeReference<>() {
                  }),
                  of(new HasValue<>(99), new TypeReference<>() {
                  }),
                  of(new HasValue<>(UUID.randomUUID()), new TypeReference<>() {
                  })
            );
        }

        /**
         * @return a list of {@link TypedValue}s that can be both serialized and deserialized by {@link JsonMapper} <b><i>by default</i></b> <i>(i.e. without any {@link com.fasterxml.jackson.core.util.JacksonFeature}s or {@link com.fasterxml.jackson.databind.Module}s)</i>
         */
        public static ImmutableList<TypedValue<?>> supportedTypes() {
            return Stream.concat(
                  unambiguousTypes().stream(),
                  Stream.of(
                        of(777, new TypeReference<Integer>() {
                        }),
                        of(4.5, new TypeReference<Double>() {
                        }),
                        of("yolo", new TypeReference<String>() {
                        }),
                        of(Map.of("swag", 1), new TypeReference<Map<String, Integer>>() {
                        })
                  )
            ).collect(ImmutableList.toImmutableList());
        }

        /**
         * @return a list of {@link TypedValue}s that, <i><b>by default</b></i>, cannot be serialized or deserialized by {@link JsonMapper} <i>(specifically, doing so will throw an {@link InvalidDefinitionException})</i>
         */
        public static ImmutableList<TypedValue<?>> unsupportedTypes() {
            return ImmutableList.of(
                  of(ImmutableList.of(), new TypeReference<>() {
                  }, "Jackson can't handle non-standard collection types by default"),
                  of(Instant.now(), new TypeReference<>() {
                  }, String.format(
                        "`Instant` technically HAS a deserializer, %s, but that deserializer always throws an `%s` at the last second",
                        UnsupportedTypeDeserializer.class.getSimpleName(),
                        InvalidDefinitionException.class.getSimpleName()
                  )),
                  of(new IgnoredConstructor(), new TypeReference<>() {
                  }, "This type doesn't have an explicit deserializer, and Jackson can't create one dynamically because its only \"Creator\" is `@JsonIgnore`d"),
                  of(new OnlyMultiArgConstructor(1, 2, 3), new TypeReference<>() {
                  }, "This type's only constructor has multiple arguments, which Jackson can't use as a \"Creator\""),
                  of(Function.identity(), new TypeReference<>() {
                  }, "Jackson can't handle interfaces without you telling it a concrete implementation to use")
            );
        }
    }

    @AllArgsConstructor(staticName = "of")
    public static final class EitherInfo<A, B> {
        public final TypedValue<A> a;
        public final TypedValue<B> b;

        public JavaType eitherType() {
            return TypeFactory.defaultInstance()
                  .constructParametricType(
                        Either.class,
                        a.javaType(),
                        b.javaType()
                  );
        }

        public TypedValue<?> get(Which which) {
            return which == Which.A ? a : b;
        }

        public Either<A, B> createEither(Which which) {
            return which == Which.A ? Either.ofA(a.value) : Either.ofB(b.value);
        }

        public String toString() {
            return String.format("EitherInfo<%s, %s>", a.shortTypeName(), b.shortTypeName());
        }
    }

    //endregion

    //region Example types

    @AllArgsConstructor
    @NoArgsConstructor(force = true)
    @Getter
    @EqualsAndHashCode
    @ToString
    public static class HasEither<A, B> {
        private final Either<A, B> value;
    }


    @AllArgsConstructor
    @NoArgsConstructor(force = true)
    @Getter
    @EqualsAndHashCode
    @ToString
    public static class HasInt {
        private final Integer intValue;
    }


    @AllArgsConstructor
    @NoArgsConstructor(force = true)
    @Getter
    @EqualsAndHashCode
    @ToString
    public static class HasValue<T> {
        private final T value;
    }

    @AllArgsConstructor
    @NoArgsConstructor(force = true)
    @Getter
    @EqualsAndHashCode
    @ToString
    public static class HasUUID {
        private final UUID uuidValue;
    }


    public static class IgnoredConstructor {
        @JsonIgnore // Jackson is able to find private constructors, so we have to explicitly tell it to skip this
        private IgnoredConstructor() {
        }
    }


    @Getter
    // `get{x}()` methods are necessary because, by default, Jackson will throw an exception if you try to deserialize to a type that has no properties
    public static class OnlyMultiArgConstructor {
        private final int a;
        private final int b;
        private final int c;

        // Jackson can't use constructors by default unless they have 0 or 1 arguments
        @SuppressWarnings("unused")
        public OnlyMultiArgConstructor(int a, int b, int c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(force = true)
    @EqualsAndHashCode
    @ToString
    public static final class HasAnnotatedPreference<A, B> {
        @EitherModule.Preference(Which.A)
        private final Either<A, B> prefersA;

        @EitherModule.Preference(Which.B)
        private final Either<A, B> prefersB;
    }

    //region Deducible by properties

    /**
     * This type's {@link JsonSubTypes} specifically <b>do not</b> work with {@link JsonTypeInfo.Id#DEDUCTION}
     * because their immediate properties have the same names.
     * <p/>
     * To properly differentiate betwixt them, we'd need to recur into deducing their individual properties, which {@link Which#hasMoreMatchingProperties(DeserializationConfig, JsonNode, JavaType, JavaType)} <b>does do</b>.
     */
    @SuppressWarnings("JavadocReference")
    @JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
    @JsonSubTypes({@JsonSubTypes.Type(SuccessResponse.class), @JsonSubTypes.Type(ErrorResponse.class)})
    public static abstract class Response {
        public abstract Object getDetails();
    }

    @AllArgsConstructor
    @NoArgsConstructor(force = true)
    @Getter
    @ToString
    public static class SuccessResponse extends Response {
        private final SuccessDetails details;

        public static final TypedValue<SuccessResponse> TYPED_VALUE = TypedValue.of(
              new SuccessResponse(new SuccessDetails("yolo", UUID.randomUUID())),
              new TypeReference<>() {
              }
        );

    }

    @AllArgsConstructor
    @NoArgsConstructor(force = true)
    @Getter
    @ToString
    public static class SuccessDetails {
        private final String message;
        private final UUID id;
    }

    @AllArgsConstructor
    @NoArgsConstructor(force = true)
    @Getter
    @ToString
    public static class ErrorResponse extends Response {
        private final ErrorDetails details;

        public static final TypedValue<ErrorResponse> TYPED_VALUE = TypedValue.of(
              new ErrorResponse(new ErrorDetails("badness", "BAD-1")),
              new TypeReference<>() {
              }
        );
    }

    @AllArgsConstructor
    @NoArgsConstructor(force = true)
    @Getter
    @ToString
    public static class ErrorDetails {
        private final String message;
        private final String code;
    }

    //endregion

    //endregion

    //region Validating test data

    public static void requireTypeSupport(boolean shouldBeSupported, JsonMapper mapper, String json, JavaType type) {
        var result = Either.resultOf(() -> mapper.readValue(json, type));

        result.tryGetB()
              .filter(MismatchedInputException.class::isInstance)
              .ifPresent(e -> {
                  var message = String.format(
                        "Usually, when you deserialize to a type without a proper 'Creator' like %s, you get an %s. However, if the JSON you are deserializing is an array, you instead get a %s 🤷‍♀️ This breaks the scenario here, so we're skipping it.",
                        type,
                        InvalidDefinitionException.class.getSimpleName(),
                        MismatchedInputException.class.getSimpleName()
                  );
                  Assumptions.abort(message);
              });


        var isNotSupported = result.tryGetB()
              .map(InvalidDefinitionException.class::isInstance)
              .orElse(false);
        var isSupported = !isNotSupported;


        Preconditions.checkArgument(isSupported == shouldBeSupported, "The type %s should %s deserializable (result: %s, isSupported: %s, shouldBeSupported: %s)", type, shouldBeSupported ? "be" : "NOT be", result, isSupported, shouldBeSupported);
    }

    @NotNull
    public static JavaType requireSameParent(JavaType a, JavaType b) {
        var aParent = a.getSuperClass();
        var bParent = b.getSuperClass();
        Preconditions.checkArgument(aParent.getRawClass() != Object.class);
        Preconditions.checkArgument(bParent.getRawClass() != Object.class);
        Preconditions.checkArgument(aParent.equals(bParent), "The types must have the same parent (🅰 %s : %s, 🅱 %s : %s)", a, aParent, b, bParent);
        return aParent;
    }

    //endregion
}
