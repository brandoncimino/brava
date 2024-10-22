package brava.core.functional;

import com.google.common.reflect.TypeToken;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Contains interfaces for doing strongly-typed <i>(with the aid of {@link TypeToken})</i> object conversions.
 *
 * @see FromSource
 * @see ToSource
 * @see BothWays
 */
public final class GenericConverter {
    private GenericConverter() {
        throw new UnsupportedOperationException("🩸🚪");
    }

    /**
     * Converts {@link SOURCE} objects into whatever your heart desires.
     *
     * @param <SOURCE> The known input type <i>(for example, a JSON parser might use {@link String}, {@link CharSequence}, or {@link java.io.InputStream}).</i>
     * @implSpec Even though this is a {@link FunctionalInterface}, you can't use a lambda expression for it due to the limitations of Java generics.
     * Instead, you have to use an <a href="https://docs.oracle.com/javase/tutorial/java/javaOO/anonymousclasses.html">anonymous class</a>:
     * <pre>{@code
     * var jsonMapper = JsonMapper.builder().build();
     *
     * FromSource<String> jacksonParser = new FromSource<String>() {
     *     @Override
     *     public <T> T fromSource(String s, TypeToken<T> toType) {
     *         return Failable.get(() -> jsonMapper.readValue(s, jsonMapper.constructType(toType)));
     *     }
     * };
     * }</pre>
     * However, you <i>can</i> use a <a href="https://docs.oracle.com/javase/tutorial/java/javaOO/methodreferences.html">method reference</a>:
     * <pre>{@code
     * var gson = new Gson();
     * FromSource<String> gsonParser = gson::fromJson;
     * }</pre>
     * @see ToSource
     * @see BothWays
     */
    @FunctionalInterface
    public interface FromSource<SOURCE> {
        /**
         * Converts {@link SOURCE} → {@link T}.
         *
         * @param source what you got
         * @param toType what you want
         * @return an equivalent {@link T} instance
         */
        <T> T fromSource(SOURCE source, @NotNull TypeToken<T> toType);
    }

    /**
     * Converts arbitrary stuff into {@link SOURCE}.
     *
     * @param <SOURCE> The known output type <i>(for example, a JSON writer might use {@link String}, {@link CharSequence}, or {@link java.io.InputStream})</i>.
     * @see FromSource
     * @see BothWays
     */
    @FunctionalInterface
    public interface ToSource<SOURCE> {
        /**
         * Converts an arbitrary {@link Object} → {@link SOURCE}.
         *
         * @param input something
         * @return an equivalent {@link SOURCE}
         */
        SOURCE toSource(Object input);
    }

    /**
     * Combines a {@link ToSource} and {@link FromSource}.
     *
     * @param <SOURCE> The "middleman" type that we can turn into anything else <i>(for example, a JSON parser might use {@link String}, {@link CharSequence}, or {@link java.io.InputStream})</i>.
     * @see ToSource
     * @see FromSource
     */
    public interface BothWays<SOURCE> extends FromSource<SOURCE>, ToSource<SOURCE> {
        /**
         * Converts {@link IN} → {@link SOURCE} → {@link OUT}.
         *
         * @param input      what you got
         * @param outputType what you want
         * @return what you get
         * @implSpec This method should <b>always</b> do a full round-trip, even if the {@link IN} and {@link OUT} types are the same.
         * This should avoid unexpected behaviors.
         */
        @Contract("_, _ -> new")
        @ApiStatus.NonExtendable
        default <IN, OUT> OUT convert(IN input, @NotNull TypeToken<OUT> outputType) {
            var source = toSource(input);
            return fromSource(source, outputType);
        }
    }
}
