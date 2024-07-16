package com.fasterxml.jackson.datatype.brava;


import brava.either.Either;
import brava.either.Which;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.MoreCollectors;
import com.google.common.collect.Streams;
import com.google.common.primitives.Primitives;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * A Jackson {@link com.fasterxml.jackson.databind.Module} that handles the {@link Either} type.
 *
 * <h1>Serialization</h1>
 * This module does not modify the serialization of {@link Either}s, which delegates to the {@link com.fasterxml.jackson.annotation.JsonValue} of
 * {@link Either#getValue()}.
 *
 * <h1>Deserialization</h1>
 * To decide whether we prefer to create an {@link Either#ofA(Object)} or {@link Either#ofB(Object)}:
 * <ol>
 *     <li>({@link WhichHelpers#isAnnotatedAsPreference(BeanProperty) 🔗}) If you've annotated the property with {@link Preference}, we'll prefer that.</li>
 *     <li>({@link WhichHelpers#isPrimitive(JavaType, JavaType) 🔗}) If one of the types {@link Class#isPrimitive()} or {@link Primitives#isWrapperType(Class)}, we'll prefer that.</li>
 *     <li>({@link WhichHelpers#isMoreSpecific(JavaType, JavaType) 🔗}) If one of the types is a <i>{@link Class#isAssignableFrom(Class) strict subtype}</i> of the other, we'll prefer that.</li>
 *     <li>({@link WhichHelpers#hasMoreMatchingProperties(DeserializationConfig, JsonNode, JavaType, JavaType) 🔗}) If one of the types has more properties matching the {@link JsonNode#fieldNames()}, we'll prefer that.</i></li>
 * </ol>
 * If we run out of ideas, we'll throw a {@link MismatchedInputException}.
 */
@ParametersAreNonnullByDefault
public final class EitherModule extends SimpleModule {
    public EitherModule() {
        super(EitherModule.class.getSimpleName());
        addDeserializer(Either.class, new EitherDeserializer());
    }

    private static final class EitherDeserializer extends JsonDeserializer<Either<?, ?>> implements ContextualDeserializer {
        /**
         * The {@link Either} type that we derived {@link #aType} and {@link #bType} from.
         */
        @NotNull
        private final JavaType eitherType;
        /**
         * The type of {@link Either#getA()} from {@link #eitherType}, which may be {@link TypeFactory#unknownType()}.
         */
        @NotNull
        private final JavaType aType;
        /**
         * The type of {@link Either#getB()} from {@link #eitherType}, which maybe {@link TypeFactory#unknownType()}.
         */
        private final JavaType bType;
        /**
         * The property, if any, that contains the {@link Either} value.
         * This lets Jackson utilize annotations on the property itself, like {@link com.fasterxml.jackson.annotation.JsonValue}.
         */
        @Nullable
        private final BeanProperty property;

        //region Constructors

        private EitherDeserializer() {
            this(
                  TypeFactory.defaultInstance().constructType(Either.class),
                  TypeFactory.unknownType(),
                  TypeFactory.unknownType(),
                  null
            );
        }

        private EitherDeserializer(JavaType eitherType, JavaType aType, JavaType bType, @Nullable BeanProperty property) {
            this.eitherType = eitherType;
            this.aType = aType;
            this.bType = bType;
            this.property = property;
        }

        //endregion

        /**
         * @implNote <ol>
         * <li>If you're trying to parse into a type that's "unparseable", you've made a mistake.</li>
         * <li>If <b><i>either</i></b> {@link #aType} or {@link #bType} is "unparseable", then the {@link #eitherType} should be "unparseable".</li>
         * </ol>
         * <p/>
         * The problem is, how do we define if a type is "parseable"? It seems like we'd be able to use
         * {@link DeserializationContext#findRootValueDeserializer(JavaType)}: that method promises to find an explicitly register deserializer if
         * possible, and if not, try to construct a dynamic one such as {@link com.fasterxml.jackson.databind.deser.BeanDeserializer}.
         * <p/>
         * However, there are many types, such as {@link java.time.Instant}, which <i>do</i> have an explicit deserializer - e.g.
         * {@link com.fasterxml.jackson.databind.deser.impl.UnsupportedTypeDeserializer} - that won't let us know it isn't supported until the very
         * last second, when we call their {@link JsonDeserializer#deserialize(JsonParser, DeserializationContext)} method.
         * <p/>
         * Plus, we might have a type that <i>is</i> parsable, but contains a non-deserializable component - such as a {@link List} that
         * contains {@link java.time.Instant}s.
         * <p/>
         * Because of this, in order to know if {@link #aType} and {@link #bType} are really truly supported, is actually supported or not, we have to
         * attempt to fully deserialize into them.
         * <p/>
         * The end result is that we have to deserialize everything twice 🤷‍♀️
         */
        @Override
        public Either<?, ?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            // ⚠ `JsonParser` is stateful!!!
            // Anything that says `read` in it will _(maybe)_ advance the `JsonParser` - this means that you can't deserialize the same content 
            // twice (which is specifically what we want to do)
            // To combat this, we first use `readValueAsTree()`, which _should_:
            //  1. Advance the `JsonParser` past the current object
            //  2. Create a `JsonNode`, which _seems_ to be immutable 🤞
            // ⚠⚠ You _must_ specify the returned type of `p.readValueAsTree()` explicitly as `JsonNode`, because the method's return value is arbitrarily typed
            JsonNode jsonNode = p.readValueAsTree();


            var a = readValue(jsonNode, ctxt, aType);
            var b = readValue(jsonNode, ctxt, bType);

            var eitherWasInvalid = Stream.of(a, b)
                  .map(InAndOut::outputOurException)
                  .flatMap(Either::streamB)
                  .anyMatch(InvalidDefinitionException.class::isInstance);

            if (eitherWasInvalid) {
                throw rejectInvalidDefinition(ctxt, eitherType, a, b);
            }

            var bothWereMismatched = Stream.of(a, b)
                  .map(InAndOut::outputOurException)
                  .map(Either::tryGetB)
                  .allMatch(it -> it.map(MismatchedInputException.class::isInstance).orElse(false));

            if (bothWereMismatched) {
                throw rejectBothMismatchedInput(ctxt, eitherType, a, b);
            }

            // If only one succeeded, return that
            if (a.isSuccess() ^ b.isSuccess()) {
                return a.isSuccess()
                      ? Either.ofA(a.get())
                      : Either.ofB(b.get());
            }

            // If both failed (but not with exception types that we've already handled), combine those exceptions into a single one
            if (a.isFailure() && b.isFailure()) {
                throw new IllegalArgumentException(formatErrorMessage(
                      "Attempted deserialization to both of the %s's types failed with unexpected exception types!",
                      eitherType,
                      a,
                      b
                ));
            }

            return Optional.empty() // this is just here so that the code aligns better and is easier to read
                  .or(() -> WhichHelpers.isAnnotatedAsPreference(property))
                  .or(() -> WhichHelpers.isPrimitive(aType, bType))
                  .or(() -> WhichHelpers.isMoreSpecific(aType, bType))
                  .or(() -> WhichHelpers.hasMoreMatchingProperties(ctxt.getConfig(), jsonNode, aType, bType))
                  .map(which ->
                        which == Which.A
                              ? Either.ofA(a.get())
                              : Either.ofB(b.get()))
                  .orElseThrow(() -> rejectAmbiguousTypes(ctxt, eitherType, jsonNode, a, b));
        }

        private record InAndOut<IN, OUT>(IN input,
                                         Either<@org.jetbrains.annotations.NotNull OUT, @org.jetbrains.annotations.NotNull Throwable> outputOurException) {
            public static <IN, OUT> InAndOut<IN, OUT> success(IN input, OUT output) {
                return new InAndOut<>(input, Either.ofA(output));
            }

            public static <IN, OUT> InAndOut<IN, OUT> failure(IN input, Throwable exception) {
                return new InAndOut<>(input, Either.ofB(exception));
            }

            public boolean isSuccess() {
                return outputOurException.hasA();
            }

            public boolean isFailure() {
                return outputOurException.hasB();
            }

            public OUT get() {
                return outputOurException.getA();
            }
        }

        /**
         * Same as {@link DeserializationContext#readTreeAsValue(JsonNode, Class)}, except that a {@link MismatchedInputException} will be thrown if
         * the result is parsed to {@code null}.
         * <p/>
         * This will usually happen because the JSON input was {@link com.fasterxml.jackson.core.JsonToken#VALUE_NULL}, but can also happen when
         * certain types are parsed - for example, <i>any</i> input, when parsed to {@link Void}, will return {@code null}.
         *
         * @implSpec This <i>must</i> operate on {@link JsonNode}s, not {@link JsonParser}s, because {@link JsonParser} is <i><b>a gross stateful
         * stream thing</b></i> while {@link JsonNode} is <i><b>slightly</b></i> less evil <i>(and can be re-used)</i>.
         */
        @NotNull
        private <T> InAndOut<JavaType, T> readValue(JsonNode jsonNode, DeserializationContext ctxt, JavaType asType) {
            try {
                T value = ctxt.readTreeAsValue(jsonNode, asType);
                if (value == null) {
                    return ctxt.reportInputMismatch(
                          asType,
                          "Deserialization produced a null value, which is not allowed!"
                    );
                }
                return InAndOut.success(asType, value);
            } catch (IOException e) {
                return InAndOut.failure(asType, e);
            }
        }

        private static String formatErrorMessage(String mainMessage, JavaType eitherType, InAndOut<JavaType, ?> a, InAndOut<JavaType, ?> b) {
            assert eitherType.getRawClass() == Either.class;
            assert !mainMessage.isBlank();

            return """
                  Unable to deserialize to %s: %s
                    🅰 %s
                    🅱 %s
                  """.formatted(
                  eitherType,
                  mainMessage,
                  a,
                  b
            );
        }

        private static InvalidDefinitionException rejectInvalidDefinition(
              DeserializationContext ctxt,
              JavaType eitherType,
              InAndOut<JavaType, ?> a,
              InAndOut<JavaType, ?> b
        ) {
            var msg = formatErrorMessage(
                  "Type 🅰 and/or 🅱 isn't supported by Jackson!",
                  eitherType,
                  a,
                  b
            );

            return InvalidDefinitionException.from(
                  ctxt.getParser(),
                  msg,
                  eitherType
            );
        }

        @NotNull
        private static MismatchedInputException rejectBothMismatchedInput(
              DeserializationContext context,
              JavaType eitherType,
              InAndOut<JavaType, ?> a,
              InAndOut<JavaType, ?> b
        ) {
            var msg = formatErrorMessage(
                  "Deserialization failed for both 🅰 and 🅱!",
                  eitherType,
                  a,
                  b
            );

            return MismatchedInputException.from(
                  context.getParser(),
                  eitherType,
                  msg
            );
        }

        @NotNull
        private static MismatchedInputException rejectAmbiguousTypes(DeserializationContext context, JavaType eitherType, JsonNode jsonNode, InAndOut<JavaType, ?> a, InAndOut<JavaType, ?> b) {
            var msg = formatErrorMessage(
                  "Both 🅰 and 🅱 succeeded, and we couldn't pick between them!"
                        + "\n----"
                        + "\n" + jsonNode
                        + "\n----",
                  eitherType,
                  a,
                  b
            );

            return MismatchedInputException.from(
                  context.getParser(),
                  eitherType,
                  msg
            );
        }

        @Override
        public JsonDeserializer<?> createContextual(@NotNull DeserializationContext ctxt, @Nullable BeanProperty property) {
            var resolvedType = resolveContextualEitherType(ctxt, property);
            var a = resolvedType.containedType(0);
            var b = resolvedType.containedType(1);

            // 📎 While it seems logical to check here if we actually support both `aType` and `bType`, we can't actually do that:
            //  `DeserializationContext.findRootValueDeserializer()` seems like it would work, but there are some deserializers,
            //  like the built-in ones for modern date types like `Instant`, that wait until the last second to _always_ throw
            //  an `InvalidDefinitionException`.
            return new EitherDeserializer(resolvedType, a, b, property);
        }

        private static Optional<JavaType> getTypeFromProperty(@Nullable BeanProperty property) {
            return Optional.ofNullable(property)
                  .map(BeanProperty::getType)
                  .filter(it -> it.isTypeOrSubTypeOf(Either.class));
        }

        private static Optional<JavaType> getTypeFromContext(@NotNull DeserializationContext ctxt) {
            return Optional.of(ctxt)
                  .map(DeserializationContext::getContextualType)
                  .filter(it -> it.isTypeOrSubTypeOf(Either.class));
        }

        private static JavaType resolveContextualEitherType(@NotNull DeserializationContext ctxt, @Nullable BeanProperty property) {
            return getTypeFromProperty(property)
                  .or(() -> getTypeFromContext(ctxt))
                  .orElseThrow(() -> new IllegalArgumentException("Unable to figure out the correct type from the context and/or property!"));
        }

    }

    //region Deciding between 🅰 and 🅱

    /**
     * If a {@link JsonNode} can be properly deserialized into both {@link Either#getA()} and {@link Either#getB()},
     * then this annotation will tell us which value to choose.
     */
    @Target({ElementType.FIELD, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Preference {
        Which value();
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
     * @return {@code true} if the {@code nameOrAlias} matches the property's {@link BeanPropertyDefinition#hasName(PropertyName) name} or one of its {@link com.fasterxml.jackson.databind.AnnotationIntrospector#findPropertyAliases(Annotated) aliases}
     */
    @Contract(pure = true)
    private static boolean hasNameOrAlias(
          @NotNull DeserializationConfig config,
          @NotNull BeanPropertyDefinition property,
          @NotNull String nameOrAlias
    ) {
        var pn = PropertyName.construct(nameOrAlias);
        return property.hasName(pn) || Stream.ofNullable(config.getAnnotationIntrospector().findPropertyAliases(property.getPrimaryMember()))
              .flatMap(Collection::stream)
              .anyMatch(pn::equals);
    }

    //endregion
}
