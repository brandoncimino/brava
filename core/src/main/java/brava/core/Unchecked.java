package brava.core;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Weird tricks Oracle HATES, including:
 * <ul>
 *     <li>Bypassing annoying checked {@link Exception}s</li>
 *     <li>Avoiding the need for {@link SuppressWarnings} on generic casts</li>
 * </ul>
 *
 * <h1>Disclaimer</h1>
 * Use this class at your own risk.
 *
 * @implNote Largely based on <a href="https://commons.apache.org/proper/commons-lang/apidocs/org/apache/commons/lang3/function/Failable.html">Apache Commons's {@code Failable}</a>.
 * <p>
 * The {@link FunctionalInterface}s in this type have several advantages over those in Apache Commons, like <a href="https://commons.apache.org/proper/commons-lang/apidocs/org/apache/commons/lang3/function/FailableFunction.html">FailableFunction</a>:
 * <ul>
 *     <li>They contain {@code default} implementations for the base {@link java.util.function} interfaces, for easier interoperability</li>
 *     <li>They have simpler type signatures by throwing {@link Throwable} instead of a parameterized type</li>
 * </ul>
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
     * return Unchecked.cast(object);
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

    //region Supplier

    /**
     * A {@link java.util.function.Supplier} that can {@link #rethrow(Throwable)} checked {@link Exception}s.
     *
     * @param <T> the output type
     * @implNote You generally shouldn't use {@link Unchecked} functional interfaces as input types, because they are kinda sneaky.
     * Instead, use vanilla {@link java.util.function} types, and let callers use "wrapper" methods like {@link Unchecked#supplier(Supplier)} if they wish.
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

        /**
         * Invokes {@link #getChecked()}, catching any thrown {@link Exception}s.
         *
         * @return {@link Either}:
         * <ul>
         *     <li>🅰 The {@link T} result of {@link #getChecked()}</li>
         *     <li>🅱 The caught exception</li>
         * </ul>
         * @see Either#resultOf(Supplier)
         */
        default Either<T, Throwable> tryGet() {
            return Either.resultOf(this);
        }
    }

    /**
     * Creates a special {@link java.util.function.Supplier} from a lambda expression that is allowed to throw checked {@link Exception}s.
     *
     * <h1>Example</h1>
     * Using vanilla Java:
     * <pre>{@code
     * Stream.generate(() -> {
     *     try {
     *         return Files.readString(Path.of("stuff.txt"));
     *     } catch (IOException e){
     *         throw new RuntimeException(e);
     *     }
     * });
     * }</pre>
     * Using {@link #supplier(Supplier)}:
     * <pre>{@code
     * Stream.generate(
     *     Unchecked.supplier(() -> Files.readString(Path.of("stuff.txt")))
     * );
     * }</pre>
     *
     * @param supplier code that returns a value and <i>might</i> throw any type of {@link Throwable}
     * @param <T>      the output type
     * @return a new {@link Supplier}
     * @see Supplier
     * @see #get(Supplier)
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
     * @see #supplier(Supplier)
     */
    public static <T> T get(@NotNull Supplier<T> supplier) {
        return supplier.get();
    }

    //endregion

    //region Function

    /**
     * A {@link java.util.function.Function} that can {@link #rethrow(Throwable)} checked {@link Exception}s.
     *
     * @param <IN>  the input type
     * @param <OUT> the output type
     * @implNote You generally shouldn't use {@link Unchecked} functional interfaces as input types, because they are kinda sneaky.
     * Instead, use vanilla {@link java.util.function} types, and let callers use "wrapper" methods like {@link Unchecked#supplier(Supplier)} if they wish.
     */
    @ApiStatus.NonExtendable
    @FunctionalInterface
    public interface Function<IN, OUT> extends java.util.function.Function<IN, OUT> {
        /**
         * Invokes this function as-is, without messing with its exceptions.
         *
         * @param input the input to the function
         * @return the output of the function
         * @throws Throwable anything that the code throws, unaltered
         */
        OUT applyChecked(IN input) throws Throwable;

        /**
         * @apiNote any checked {@link Exception}s will be {@link #rethrow(Throwable)}n <i><b>without being wrapped</b></i>.
         * If you wish to maintain the checked nature of the exception, use {@link #applyChecked(IN)} instead.
         */
        @ApiStatus.NonExtendable
        default OUT apply(IN t) {
            try {
                return applyChecked(t);
            } catch (Throwable e) {
                return rethrow(e);
            }
        }
    }

    /**
     * Creates a special {@link java.util.function.Function} from a lambda expression that is allowed to throw checked {@link Exception}s.
     *
     * <h1>Example</h1>
     * Using vanilla Java:
     * <pre>{@code
     * Stream.of(Path.of("stuff.txt"))
     *     .map(it -> {
     *         try {
     *             return Files.readString(it);
     *         } catch (IOException e) {
     *             throw new RuntimeException(e);
     *         }
     *     });
     * }</pre>
     * Using {@link Function}:
     * <pre>{@code
     * Stream.of(Path.of("stuff.txt"))
     *     .map(Unchecked.function(it -> Files.readString(it)));
     * }</pre>
     *
     * @param function
     * @param <IN>
     * @param <OUT>
     * @return
     */
    @Contract(value = "_ -> param1", pure = true)
    public static <IN, OUT> Unchecked.Function<IN, OUT> function(@NotNull Function<IN, OUT> function) {
        return function;
    }

    /**
     * Sends {@code input} to a {@link Function} and gets the result.
     * <p>
     * Any checked exceptions are {@link #rethrow(Throwable)}n <i><b>without being wrapped</b></i>.
     *
     * <h1>Example</h1>
     * Using vanilla Java:
     * <pre>{@code
     * var file = Path.of("stuff.txt");
     *
     * final String content;
     * try {
     *     content = Files.readString(file);
     * } catch (IOException e) {
     *     throw new RuntimeException(e);
     * }
     * }</pre>
     * Using {@link #apply(IN, Function)}:
     * <pre>{@code
     * var file = Path.of("stuff.txt");
     *
     * var content = Unchecked.apply(file, Files::readString);
     * }</pre>
     *
     * @param input    the function's input
     * @param function the code that <i>might</i> throw any kind of {@link Throwable}
     * @param <IN>     the input type
     * @param <OUT>    the output type
     * @return the resulting {@link OUT}
     * @apiNote Inspired by <a href="https://commons.apache.org/proper/commons-lang/apidocs/org/apache/commons/lang3/function/Failable.html#apply-org.apache.commons.lang3.function.FailableFunction-T-">Apache Commons's {@code Failable.apply()}</a>.
     * <p>
     * However, the {@code input} precedes the {@code function} because:
     * <ol>
     *     <li>Passing the {@code input} first informs the {@link IN} type parameter, improving code completion in the {@code function} lambda</li>
     *     <li>If you want the {@code function} to come first, you can always call {@link #function(Function)}.{@link Function#apply(IN) apply(IN)}.</li>
     * </ol>
     */
    public static <IN, OUT> OUT apply(IN input, @NotNull Function<IN, OUT> function) {
        return function.apply(input);
    }

    //endregion

    //region Runnable

    /**
     * An equivalent to {@link java.lang.Runnable} that is allowed to throw checked exceptions.
     */
    @FunctionalInterface
    public interface Runnable extends java.lang.Runnable {
        /**
         * Executes my code without messing with checked exceptions.
         */
        void runChecked() throws Throwable;

        /**
         * Executes my code.
         * <p>
         * Any checked exceptions that occur will be {@link #rethrow(Throwable)}n <i><b>without being wrapped</b></i>.
         */
        default void run() {
            try {
                runChecked();
            } catch (Throwable e) {
                rethrow(e);
            }
        }
    }

    /**
     * @param runnable some code that doesn't return a value and might throw a checked exception
     * @return a new {@link Runnable}
     */
    @Contract(value = "_ -> param1", pure = true)
    public static @NotNull Runnable runnable(@NotNull Runnable runnable) {
        return runnable;
    }

    /**
     * Executes some code.
     * <p>
     * Any checked exceptions will be {@link #rethrow(Throwable)}n <i><b>without being wrapped</b></i>.
     *
     * @param runnable some code that doesn't return a value and might throw a checked exception
     */
    public static void run(@NotNull Runnable runnable) {
        runnable.run();
    }

    //endregion
}
