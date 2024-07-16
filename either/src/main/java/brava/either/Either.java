package brava.either;

import brava.core.Unchecked;
import brava.core.exceptions.Exceptions;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Equivalence;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Represents a <a href="https://en.wikipedia.org/wiki/Tagged_union">tagged union</a>, which is an instance of either {@link A} <i><b>OR</b></i> {@link B}.
 *
 * @param <A> one possibility
 * @param <B> an alternate universe
 * @apiNote This class is particularly useful for representing things that might have failed, where you want to return either a proper result or a raised {@link Exception}.
 * You can see this pattern used in Java itself at {@link java.util.concurrent.CompletableFuture#handle(BiFunction)}.
 */
@Immutable
public final class Either<A, B> {
    /**
     * Either the {@link A} or the {@link B} value.
     */
    @NotNull
    @JsonValue
    private final Object value;
    private final boolean hasA;

    private Either(@CheckForNull Object value, boolean hasA) {
        if (value == null) {
            throw new IllegalArgumentException(String.format("You can't construct an %s from a null value!", getClass().getSimpleName()));
        }

        this.value = value;
        this.hasA = hasA;
    }

    /**
     * @return {@code true} if I contain a {@link #value} of {@link A}
     */
    @Contract(pure = true)
    public boolean hasA() {
        return hasA;
    }

    /**
     * @return {@code true} if I contain a {@link #value} of {@link B}
     */
    @Contract(pure = true)
    public boolean hasB() {
        return !hasA;
    }

    /**
     * @return {@link Which#A} if my {@link #value} is {@link A}; otherwise, {@link Which#B}
     */
    @Contract(pure = true)
    public @NotNull Which hasWhich() {
        return hasA ? Which.A : Which.B;
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Contract(pure = true)
    private A unsafeA() {
        return (A) value;
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Contract(pure = true)
    private B unsafeB() {
        return (B) value;
    }

    /**
     * @return my {@link A} value
     * @throws IllegalStateException I actually {@link #hasB()}
     * @see #tryGetA()
     */
    @Contract(pure = true)
    @NotNull
    public A getA() {
        if (hasB()) {
            throw new IllegalStateException(String.format("Can't get 🅰 because this %s contains 🅱 (%s)!", getClass().getSimpleName(), value));
        }

        return unsafeA();
    }

    /**
     * @return my {@link B} value
     * @throws IllegalStateException I actually {@link #hasA()}
     * @see #tryGetB()
     */
    @NotNull
    @Contract(pure = true)
    public B getB() {
        if (hasA()) {
            throw new IllegalStateException(String.format("Can't get 🅱 because this %s contains 🅰 (%s)!", getClass().getSimpleName(), value));
        }

        return unsafeB();
    }

    /**
     * @return my {@link A} value, <i><b>if</b></i> I {@link #hasA()}
     * @see #getA()
     */
    @Contract(pure = true)
    @NotNull
    public Optional<A> tryGetA() {
        if (hasA()) {
            return Optional.of(unsafeA());
        } else {
            return Optional.empty();
        }
    }

    /**
     * @return my {@link B} value, <i><b>if</b></i> I {@link #hasB()}
     * @see #getB()
     */
    @Contract(pure = true)
    @NotNull
    public Optional<B> tryGetB() {
        if (hasB()) {
            return Optional.of(unsafeB());
        } else {
            return Optional.empty();
        }
    }

    /**
     * @return a {@link Stream#of(A)}, <i>if</i> I {@link #hasA()}
     * @apiNote This method is provided to make {@link Stream#flatMap}ping easier:
     * <pre>{@code
     *     public static int getTotalWheels(List<Either<Car, Boat>> vehicles){
     *         return vehicles.stream()
     *             .flatMap(Either::streamA)
     *             .mapToInt(Car::getWheelCount)
     *             .sum();
     *     }
     * }</pre>
     * @see #tryGetA()
     */
    @NotNull
    @Contract(pure = true)
    public Stream<@NotNull A> streamA() {
        if (hasA()) {
            return Stream.of(unsafeA());
        } else {
            return Stream.empty();
        }
    }

    @NotNull
    @Contract(pure = true)
    public Stream<@NotNull B> streamB() {
        if (hasB()) {
            return Stream.of(unsafeB());
        } else {
            return Stream.empty();
        }
    }

    /**
     * @return my {@link #value}, which can be either an {@link A} or {@link B}
     */
    @NotNull
    public Object getValue() {
        return value;
    }

//    /**
//     * @return <ul>
//     * <li>if I {@link #hasA()} ⇒ ({@link #getA()}, null)</li>
//     * <li>if I {@link #hasB()} ⇒ (null, {@link #getB()})</li>
//     * </ul>
//     */
//    public Tuple2<A, B> getBoth() {
//        if (hasA) {
//            return Tuple.tuple(unsafeA(), null);
//        } else {
//            return Tuple.tuple(null, unsafeB());
//        }
//    }

    /**
     * Creates a new {@link Either} from <i>exactly one</i> non-null value.
     *
     * @param a the possible {@link A} value
     * @param b the possible {@link B} value
     * @return a new {@link Either}
     * @throws IllegalArgumentException <ul>
     *                                  <li>{@link A} and {@link B} are <b><i>both</i></b> null</li>
     *                                  <li>{@link A} and {@link B} are <b><i>neither</i></b> null</li>
     *                                  </ul>
     */
    @NotNull
    @Contract(value = "null, null -> fail; !null, !null -> fail", pure = true)
    public static <A, B> Either<@NotNull A, @NotNull B> of(@Nullable A a, @Nullable B b) {
        var isAMissing = a == null;
        var isBMissing = b == null;

        if (isAMissing == isBMissing) {
            throw new IllegalArgumentException(String.format("You must provide EITHER 🅰 OR 🅱!%n🅰 %s%n🅱 %s", a, b));
        }

        return isBMissing ? ofA(a) : ofB(b);
    }

    /**
     * @param a the {@link A} value
     * @return a new {@link Either} that {@link #hasA()}
     */
    @NotNull
    @Contract(pure = true)
    public static <A, B> Either<@NotNull A, @NotNull B> ofA(@NotNull A a) {
        return new Either<>(a, true);
    }

    /**
     * @param b the {@link B} value
     * @return a new {@link Either} that {@link #hasB()}
     */
    @NotNull
    @Contract(pure = true)
    public static <A, B> Either<@NotNull A, @NotNull B> ofB(@NotNull B b) {
        return new Either<>(b, false);
    }

    /**
     * Attempts to invoke a {@link Callable}, catching and returning the given exception types.
     * <i><b>Any</b></i> other exception is {@link Unchecked#rethrow(Throwable)}n.
     *
     * @param supplier     some code that produces {@link T} and might throw an {@link E}
     * @param catching     an exception type that we want to catch and return
     * @param alsoCatching additional exception types that we can catch
     * @return a new {@link Either} containing the resulting {@link T} OR the thrown {@link E}
     */
    @SafeVarargs
    @NotNull
    public static <T, E extends Throwable> Either<@NotNull T, @NotNull E> resultOf(
          @NotNull Unchecked.Supplier<@NotNull T> supplier,
          @NotNull Class<? extends E> catching,
          @NotNull Class<? extends E>... alsoCatching
    ) {
        Objects.requireNonNull(supplier);

        T result;
        E exc;
        try {
            // We don't want to return from inside the try/catch because we want to make sure we ONLY catch exceptions caused by `supplier.call()`.
            // For example, if `supplier.call()` returns `null`, we want that error to be propagated.
            result = supplier.get();
            exc = null;
        } catch (Throwable e) {
            result = null;
            try {
                exc = Unchecked.get(() -> Exceptions.throwUnless(e, catching, alsoCatching));
            } catch (Throwable e2) {
                return Unchecked.rethrow(e2);
            }
        }

        return Either.of(result, exc);
    }

    /**
     * Attempts to invoke a {@link Callable}, returning either the result or the thrown {@link Throwable}.
     *
     * @param supplier some code that might throw a {@link Throwable}
     * @return a new {@link Either} containing the resulting {@link T} OR the thrown {@link Throwable}
     * @throws IllegalArgumentException if the {@code supplier} returns null
     * @implSpec Only exceptions raised <i>inside</i> of {@link Callable#call()} should be caught.
     */
    public static <T> @NotNull Either<@NotNull T, @NotNull Throwable> resultOf(@NotNull Unchecked.Supplier<@NotNull T> supplier) {
        return resultOf(supplier, Throwable.class);
    }

    @NotNull
    @Override
    @Contract(pure = true)
    public String toString() {
        if (hasA()) {
            return "🅰 " + value;
        } else {
            return "🅱 " + value;
        }
    }

    /**
     * Produces a value of {@link T} from my {@link #value}, whether I {@link #hasA()} or {@link #hasB()}.
     *
     * @param ifA if I {@link #hasA()}, transform it with this
     * @param ifB if I {@link #hasB()}, transform it with this
     * @param <T> the new output type
     * @return the resulting {@link T} value
     * @see #map(Function, Function)
     */
    public <T> T handle(@NotNull Function<@NotNull A, T> ifA, @NotNull Function<@NotNull B, T> ifB) {
        if (hasA) {
            return ifA.apply(unsafeA());
        } else {
            return ifB.apply(unsafeB());
        }
    }

    /**
     * Transforms my {@link #value} into {@link Either}<{@link A2}, {@link B2}> depending on whether I {@link #hasA()} or {@link #hasB()}.
     *
     * @param ifA  if I {@link #hasA()}, transform it with this
     * @param ifB  if I {@link #hasB()}, transform it with this
     * @param <A2> the new output type <i>if</i> I {@link #hasA()}
     * @param <B2> the new output type <i>if</i> I {@link #hasB()}
     * @return {@link Either}<{@link A2}, {@link B2}>
     * @see #handle(Function, Function)
     */
    public <A2, B2> Either<@NotNull A2, @NotNull B2> map(@NotNull Function<@NotNull A, @NotNull A2> ifA, @NotNull Function<@NotNull B, @NotNull B2> ifB) {
        if (hasA) {
            return Either.ofA(ifA.apply(unsafeA()));
        } else {
            return Either.ofB(ifB.apply(unsafeB()));
        }
    }

    /**
     * Determines the equality of {@code this} with another {@link Either}.
     * <p/>
     * The other {@code obj} must:
     * <ul>
     *     <li>Be an instance of {@link Either}</li>
     *     <li>Have their value in the same "position" <i>(i.e. {@link #hasA()})</i></li>
     *     <li>Have an equal {@link #getValue()}</li>
     * </ul>
     * <hr/>
     * <h2>Examples</h2>
     * <pre>{@code
     * // The "position" (🅰 or 🅱) of the value matters:
     * Either<String, String> hasA = Either.ofA("x");
     * Either<String, String> hasB = Either.ofB("x");
     * hasA.equals(hasB); // => false
     *
     * // The type of the missing value is irrelevant:
     * Either<String, Integer> stringOrInt  = Either.ofA("a");
     * Either<String, UUID> stringOrUUID    = Either.ofA("a");
     * stringOrInt.equals(stringOrUUID); // => true
     * }</pre>
     *
     * @apiNote The default {@link Object#equals(Object)} method of my {@link #value} is used for comparisons.
     * <br/>
     * If you require more control, you can use {@link #areEqual(Either, Either, Equivalence, Equivalence)},
     * which also provides stronger type safety.
     * @see #equals(Either, Equivalence, Equivalence)
     * @see #areEqual(Either, Either, Equivalence, Equivalence)
     */
    @Contract(pure = true, value = "null -> false")
    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof Either<?, ?> other) {
            return (hasA == other.hasA) && value.equals(other.value);
        }

        return false;
    }

    /**
     * @param first  one thing
     * @param second another thing
     * @param ifA    compares {@link A}s if they both {@link #hasA()}
     * @param ifB    compares {@link B}s if they both {@link #hasB()}
     * @return {@code true} if both {@link Either}s:
     * <ul>
     *     <li>Have values in the same "position" <i>(i.e. {@link #hasA()})</i></li>
     *     <li>Have values that are {@link Equivalence#equivalent(Object, Object)}</li>
     * </ul>
     * @see #equals(Object)
     * @see #equals(Either, Equivalence, Equivalence)
     */
    @Contract(pure = true, value = "null, !null, _, _ -> false; !null, null, _, _ -> false; null, null, _, _ -> true")
    public static <A, B> boolean areEqual(
          @Nullable Either<? extends @NotNull A, ? extends @NotNull B> first,
          @Nullable Either<? extends @NotNull A, ? extends @NotNull B> second,
          @NotNull Equivalence<? super A> ifA,
          @NotNull Equivalence<? super B> ifB
    ) {
        if (first == second) {
            return true;
        }

        if (first == null || second == null) {
            return false;
        }

        if (first.hasA) {
            return second.hasA && ifA.equivalent(first.unsafeA(), second.unsafeA());
        } else {
            return !second.hasA && ifB.equivalent(first.unsafeB(), second.unsafeB());
        }
    }

    /**
     * @param other another thing
     * @param ifA   compares {@link A}s if we both {@link #hasA()}
     * @param ifB   compares {@link B}s if we both {@link #hasB()}
     * @return {@code true} if:
     * <ul>
     *     <li>We both have a value in the same "position" <i>(i.e. {@link #hasA()})</li>
     *     <li>Those values are {@link Equivalence#equivalent(A, A)}</li>
     * </ul>
     * @apiNote Due to the limitations of bounded generic types, {@code other}'s types must extend {@code this}'s.
     * If you require a more flexible type signature, you can use {@link #areEqual(Either, Either, Equivalence, Equivalence)} instead.
     * @see #equals(Object)
     * @see #areEqual(Either, Either, Equivalence, Equivalence)
     */
    @Contract(pure = true, value = "null, _, _ -> false")
    public boolean equals(
          @Nullable Either<? extends A, ? extends B> other,
          @NotNull Equivalence<? super A> ifA,
          @NotNull Equivalence<? super B> ifB
    ) {
        if (other == this) {
            return true;
        }

        if (other == null) {
            return false;
        }

        if (hasA) {
            return other.hasA && ifA.equivalent(unsafeA(), other.unsafeA());
        } else {
            return !other.hasA && ifB.equivalent(unsafeB(), other.unsafeB());
        }
    }

    /**
     * @return the underlying {@link #getValue()}'s {@link Object#hashCode()}
     * @implSpec This logic is consistent with {@link Optional#hashCode()} and {@link #equals(Object)}.
     */
    @Override
    @Contract(pure = true)
    public int hashCode() {
        return value.hashCode();
    }
}
