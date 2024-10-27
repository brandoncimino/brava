package com.fasterxml.jackson.datatype.brava;

import brava.core.Either;
import brava.core.Which;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assumptions;

import java.util.UUID;

/**
 * Contains data used to test {@link Either}s.
 */
public final class EitherTestData {
    //region Test data holders

    public record EitherInfo<A, B>(TypedValue<A> a, TypedValue<B> b) {
        public static <A, B> EitherInfo<A, B> of(TypedValue<A> a, TypedValue<B> b) {
            return new EitherInfo<>(a, b);
        }

        public JavaType eitherType() {
            return constructEitherType(a.javaType(), b.javaType());
        }

        public static JavaType constructEitherType(JavaType aType, JavaType bType) {
            return TypeFactory.defaultInstance().constructParametricType(Either.class, aType, bType);
        }

        public TypedValue<?> get(Which which) {
            return which == Which.A ? a : b;
        }

        public Either<A, B> createEither(Which which) {
            return which == Which.A ? Either.ofA(a.value()) : Either.ofB(b.value());
        }

        public String toString() {
            return String.format("EitherInfo<%s, %s>", a.shortTypeName(), b.shortTypeName());
        }
    }

    //endregion

    //region Example types

    //region Deducible by properties

    /**
     * This type's {@link JsonSubTypes} specifically <b>do not</b> work with {@link JsonTypeInfo.Id#DEDUCTION}
     * because their immediate properties have the same names.
     * <p/>
     * To properly differentiate betwixt them, we'd need to recur into deducing their individual properties, which {@link Which#hasMoreMatchingProperties(DeserializationConfig, JsonNode, JavaType, JavaType)} <b>does do</b>.
     */
    @SuppressWarnings("JavadocReference")
    @JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
    @JsonSubTypes({ @JsonSubTypes.Type(SuccessResponse.class), @JsonSubTypes.Type(ErrorResponse.class) })
    public static abstract class Response {
        public abstract Object getDetails();
    }

    @AllArgsConstructor
    @NoArgsConstructor(force = true)
    @Getter
    @ToString
    public static class SuccessResponse extends Response {
        private final SuccessDetails details;

        public static final TypedValue<SuccessResponse> TYPED_VALUE = TypedValue.of(new SuccessResponse(new SuccessDetails("yolo", UUID.randomUUID())), new TypeReference<>() { });

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

        public static final TypedValue<ErrorResponse> TYPED_VALUE = TypedValue.of(new ErrorResponse(new ErrorDetails("badness", "BAD-1")), new TypeReference<>() { });
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
                    "Usually, when you deserialize to a type without a proper '%s' like %s, you get an %s. However, if the JSON you are deserializing is an array, you instead get a %s 🤷‍♀️ This breaks the scenario here, so we're skipping it.",
                    JsonCreator.class.getSimpleName(),
                    type,
                    InvalidDefinitionException.class.getSimpleName(),
                    MismatchedInputException.class.getSimpleName()
                );
                Assumptions.abort(message);
            });


        var isNotSupported = result.tryGetB().map(InvalidDefinitionException.class::isInstance).orElse(false);
        var isSupported    = !isNotSupported;


        Preconditions.checkArgument(isSupported == shouldBeSupported, "The type %s should %s deserializable (result: %s, isSupported: %s, shouldBeSupported: %s)", type,
            shouldBeSupported ? "be" : "NOT be", result, isSupported, shouldBeSupported
        );
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
