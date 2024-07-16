package brava.core;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Weird tricks Oracle HATES, including:
 * <p>
 * Primarily for:
 * <ul>
 *     <li>Bypassing annoying checked {@link Exception}s</li>
 *     <li>Avoiding the need for {@link SuppressWarnings} on generic casts</li>
 * </ul>
 *
 * @implNote Largely based on <a href="https://commons.apache.org/proper/commons-lang/apidocs/org/apache/commons/lang3/function/Failable.html">Apache Commons's {@code Failable}</a>.
 */
public final class Unchecked {
    /**
     * Lets you bypass the silly checked exception rule.
     *
     * @param exception the {@link Exception} you want to throw
     * @return nothing, but this way you can use this in {@code return} statements
     */
    @Contract(value = "_ -> fail", pure = true)
    public static <T> T rethrow(@NotNull Throwable exception) {
        return typeErasure(exception);
    }

    /**
     * Claim a Throwable is another Exception type using type erasure. This
     * hides a checked exception from the java compiler, allowing a checked
     * exception to be thrown without having the exception in the method's throw
     * clause.
     *
     * @implNote Taken from Apache Commons.
     */
    @SuppressWarnings("unchecked")
    private static <R, T extends Throwable> R typeErasure(final Throwable throwable) throws T {
        throw (T) throwable;
    }

    /**
     * Suppresses the {@code "unchecked"} cast warning that {@code (T) object} would normally produce.
     *
     * <h1>Example</h1>
     * Vanilla way:
     * <pre>{@code
     * @SuppresWarning("unchecked")
     * var casted = (T) object;
     * return casted;
     * }</pre>
     * Using {@link #cast(Object)}:
     * <pre>{@code
     * return Unsafe.cast(object);
     * }</pre>
     *
     * @param object the original object
     * @param <T>    the desired type
     * @return the object, cast to {@link T}
     */
    @Contract(pure = true, value = "null -> null; !null -> !null")
    public static <T> @Nullable T cast(@Nullable Object object) {
        @SuppressWarnings("unchecked")
        var casted = (T) object;
        return casted;
    }

    /**
     * A {@link Supplier} that can {@link #rethrow(Throwable)} checked {@link Exception}s.
     *
     * @param <T> the output type
     */
    @FunctionalInterface
    @ApiStatus.NonExtendable
    public interface Supplier<T> extends java.util.function.Supplier<T> {
        /**
         * Gets my value, without doing anything sneaky to checked {@link Exception}s.
         * <p>
         * To sneakily bypass checked exceptions instead, use {@link #get()}.
         *
         * @return the resulting {@link T} value
         * @throws Throwable whatever my code throws, untouched
         */
        T getChecked() throws Throwable;

        /**
         * @apiNote any checked {@link Exception}s will be {@link #rethrow(Throwable)}n <i><b>without being wrapped</b></i>.
         * If you wish to maintain the checked nature of the exception, use {@link #getChecked()} instead.
         */
        @Override
        default T get() {
            try {
                return getChecked();
            } catch (Throwable e) {
                return rethrow(e);
            }
        }
    }

    /**
     * Creates a {@link java.util.function.Supplier} from a lambda expression that is allowed to throw checked {@link Exception}s.
     *
     * <h1>Example</h1>
     * Using vanilla Java:
     * <pre>{@code
     * Stream.generate(() -> {
     *     try {
     *         return Files.readString("stuff.txt");
     *     } catch (IOException e){
     *         throw new RuntimeException(e);
     *     }
     * });
     * }</pre>
     * Using {@link #supplier(Supplier)}:
     * <pre>{@code
     * Stream.generate(
     *     Unchecked.supplier(() -> Files.readString("stuff.txt"))
     * );
     * }</pre>
     *
     * @param supplier code that returns a value and <i>might</i> throw any type of {@link Throwable}
     * @param <T>      the output type
     * @return a new {@link java.util.function.Supplier}
     */
    @Contract(value = "_ -> param1", pure = true)
    public static <T> @NotNull Supplier<T> supplier(@NotNull Supplier<T> supplier) {
        return Objects.requireNonNull(supplier, "supplier");
    }

    /**
     * Executes some code that:
     * <ul>
     *     <li>{@code return}s a value</li>
     *     <li><i>might</i> throw a checked {@link Exception}</li>
     * </ul>
     * <p>
     * Any checked exception is <b>{@link #rethrow(Throwable)}n <b><i>without being wrapped.</i></b>
     *
     * <h1>Example</h1>
     * Using vanilla Java:
     * <pre>{@code
     * final String content;
     * try {
     *     content = Files.readString("stuff.txt");
     * } catch (IOException e) {
     *     throw new RuntimeException(e);
     * }
     * }</pre>
     * Using {@link #get(Supplier)}:
     * <pre>{@code
     * var content = Unchecked.get(() -> Files.readString("stuff.txt"));
     * }</pre>
     *
     * @param supplier code that generates a value
     * @param <T>      the output type
     * @return the result of {@code supplier}
     * @implNote Inspired by <a href="https://commons.apache.org/proper/commons-lang/apidocs/org/apache/commons/lang3/function/Failable.html#get-org.apache.commons.lang3.function.FailableSupplier-">Apache Commons's {@code Failable.get()}</a>.
     */
    public static <T> T get(@NotNull Supplier<T> supplier) {

        return supplier.get();
    }
}
