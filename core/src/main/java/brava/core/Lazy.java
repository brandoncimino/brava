package brava.core;


import brava.core.tuples.Tuple;
import brava.core.tuples.Tuple0;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Contains a {@link T} that won't be computed until {@link #get()} <i>(or {@link #getChecked()})</i> is called.
 *
 * @apiNote <ul>
 * <li>This class is thread-safe, meaning that even if multiple threads call {@link #get()} <i>(or {@link #getChecked()})</i> at the same time, the value will only be computed
 * once.</li>
 * <li>Calling {@link #get()} will {@link Unchecked#rethrow(Throwable)} exceptions without wrapping them.
 * If you want to preserve checked exceptions for some reason, you can call {@link #getChecked()} instead.</li>
 * </ul>
 * @implSpec This class is guaranteed to only ever invoke the {@link Unchecked.Supplier} that generates the {@link T} <i>at most once</i>. This means
 * that:
 * <ul>
 *     <li>The supplier invocation must be
 *     <a href="https://docs.oracle.com/javase/tutorial/essential/concurrency/locksync.html">synchronized</a>.</li>
 *     <li>If the supplier throws an exception, then that exception should be re-thrown whenever {@link #get()} <i>(or {@link #getChecked()})</i> is called in the
 *     future.</li>
 *     <li>Once {@link #get()} <i>(or {@link #getChecked()})</i> has been called, no references to the {@link Unchecked.Supplier} should remain. This ensures that any references captured by the {@link Unchecked.Supplier} have been freed.</li>
 * </ul>
 */
public final class Lazy<T> implements Unchecked.Supplier<@NotNull T> {
    private static final String NULL_VALUE_MESSAGE = "A Lazy instance cannot contain a null value! Consider using Lazy.ofNullable() instead.";

    /**
     * Describes what's going on inside of a {@link Lazy}
     */
    public enum State {
        /**
         * I'm brand-new, and haven't tried to generate my value yet.
         */
        FRESH,
        /**
         * I've successfully generated my value.
         */
        DONE,
        /**
         * I tried to generate my value, but threw an exception.
         */
        FAILED
    }

    /**
     * Stores something, based on my {@link #state}:
     * <ul>
     *     <li>{@link State#FRESH} ⇒ the {@link Supplier} that generates my {@link T}</li>
     *     <li>{@link State#DONE} ⇒ my {@link T}</li>
     *     <li>{@link State#FAILED} ⇒ the {@link Throwable} thrown by my supplier</li>
     * </ul>
     *
     * @implNote While there are theoretical benefits to having an object with fewer fields, the primary purpose of re-using {@link #myObject}
     * is to ensure that we forget about the {@link Supplier} once we've used it.
     * <p>
     * This is important because, without something like <a href="https://learn.microsoft.com/en-us/dotnet/csharp/language-reference/proposals/csharp-9.0/static-anonymous-functions">C#'s {@code static} lambda modifier</a>, we can't prevent the {@link Supplier} from containing references
     * to the outside world.
     * <p>
     * By forgetting about the {@link Supplier}, we let the garbage collector clean it up, which it turn lets the garbage collector clean up whatever
     * the {@link Supplier} was referring to.
     */
    @Nullable
    private                   Object myObject;
    private volatile @NotNull State  state = State.FRESH;

    //region Factories

    /**
     * Creates a new {@link Lazy}.
     *
     * @param supplier the code that generates a <b><i>non-null</i></b> {@link T} value
     * @param <T>      the type of my value
     * @return a new {@link Lazy}
     * @apiNote It is assumed that the provided {@code supplier} will <b><i>never</i></b> return {@code null}. If it can, use the
     * {@link #ofNullable(Unchecked.Supplier)} factory instead.
     */
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static <T> Lazy<T> of(@NotNull Unchecked.Supplier<@NotNull T> supplier) {
        return new Lazy<>(supplier);
    }

    /**
     * Creates a new {@link Lazy} that safely wraps {@code null} values in an {@link Optional}.
     *
     * @param supplier the code that generates a <i>(possibly {@code null})</i> {@link T} value
     * @param <T>      the nullable result type
     * @return a new {@link Lazy} that returns an {@link Optional} of {@link T}
     * @apiNote If the provided {@code supplier} can never return {@code null}, use {@link #of(Unchecked.Supplier)} instead.
     */
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static <T> Lazy<Optional<T>> ofNullable(@NotNull Unchecked.Supplier<? extends T> supplier) {
        return of(() -> Optional.ofNullable(supplier.get()));
    }

    /**
     * Creates a new {@link Lazy} with a {@link T} that's been pre-initialized.
     *
     * @param value the pre-initialized instance of {@link T}
     * @param <T>   the type of my value
     * @return a new {@link Lazy} that is already {@link State#DONE}
     * @apiNote Use this method if you already have the {@link T} that you want to avoid creating an unnecessary {@link Unchecked.Supplier}.
     * <p>
     * If {@link T} might be {@code null}, use {@link #ofNullable(T)} instead.
     */
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static <T> Lazy<T> of(@NotNull T value) {
        return new Lazy<>(Objects.requireNonNull(value, NULL_VALUE_MESSAGE));
    }

    /**
     * Creates a new {@link Lazy} with a {@link T} that's been pre-initialized <b>and might be {@code null}</b>.
     *
     * @param value the pre-initialized instance of {@link T}, or {@code null}
     * @param <T>   the type of nullable value
     * @return a new {@link Lazy} that is already {@link State#DONE} with an {@link Optional} value
     */
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static <T> Lazy<Optional<T>> ofNullable(@Nullable T value) {
        return of(Optional.ofNullable(value));
    }

    /**
     * Creates a new {@link Lazy} that is already {@link State#FAILED}.
     *
     * @param exception the {@link Throwable} that will be thrown whenever {@link #get()} is called
     * @param <T>       the type of my value
     * @return a new {@link Lazy} that is already {@link State#FAILED}
     */
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static <T> Lazy<T> failure(@NotNull Throwable exception) {
        return new Lazy<>(exception);
    }

    /**
     * Creates a new {@link Lazy} <i>without</i> a return value.
     *
     * @param code some {@link Runnable} code that doesn't return anything
     * @return a {@link Lazy} with a placeholder return value
     * @implNote We return {@link Tuple0} instead of {@link Void} to maintain the constraint that {@link #myObject} is never {@code null}.
     * <p>
     * The use of {@link Tuple0} as a "unit type" is given some validity by
     * <a href="https://en.wikipedia.org/wiki/Unit_type#:~:text=One%20may%20also%20regard%20the%20unit%20type%20as%20the%20type%20of%200%2Dtuples">Wikipedia</a>.
     * <p/>
     * If we ever decide that we don't like {@link Tuple0} because of its dependency on {@link groovy}, then alternatives include:
     * <ol>
     *     <li><h3>{@link Optional}<{@link Void}></h3>
     *     Since {@link Void} can only ever bee {@code null}, this type only has one value,
     *     {@link Optional#empty()}. This is also consistent with {@link Lazy#ofNullable(Object)}.
     *     Mechanically, this is sound, but it's super weird looking.
     *     </li>
     *
     *     <li><h3>Use {@link Void} and allow it to be {@code null}</h3>
     *     For anybody consuming a {@link Lazy}, this should be fine - if you try to treat a {@link Void} as anything other than {@code null},
     *     you've made a mistake, and you deserve the {@link NullPointerException}.
     *     However, it makes the internal logic of {@link Lazy} super janky, because we now have to treat {@link Void}
     *     type parameters in a special way.
     *     It also wouldn't play nice with other null-hostile libraries.</li>
     *     <li><h3>Use {@link Class}<{@link Void}></h3>
     *     This will <i>(<a href="https://stackoverflow.com/questions/63336082/is-a-java-class-instance-singleton">theoretically</a>)</i>
     *     always refer to the same singleton instance, {@link Void#TYPE}.
     *     It's just about as weird as the {@link Optional} version, though.
     *     <b>UPDATE July 19, 2024:</b> Actually, it's probably even weirder.gvb
     *     </li>
     *     <li><h3>Create our own <a href="https://en.wikipedia.org/wiki/Unit_type">"unit" type</a></h3>
     *     This is the last resort.</li>
     * </ol>
     */
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static Lazy<Tuple0> action(@NotNull Unchecked.Runnable code) {
        return of(() -> {
            code.run();
            return Tuple.of();
        });
    }

    //endregion

    //region Constructors

    @Contract(pure = true)
    private Lazy(@NotNull Unchecked.Supplier<T> supplier) {
        this.myObject = supplier;
        this.state    = State.FRESH;
    }

    @Contract(pure = true)
    private Lazy(@NotNull T value) {
        this.myObject = value;
        this.state    = State.DONE;
    }

    @Contract(pure = true)
    private Lazy(@NotNull Throwable exception) {
        this.myObject = exception;
        this.state    = State.FAILED;
    }

    //endregion

    //region `currentValue` getters

    private Supplier<T> getSupplier() {
        assert state == State.FRESH;
        return Unchecked.cast(myObject);
    }

    private Throwable getException() {
        assert state == State.FAILED;
        return Unchecked.cast(myObject);
    }

    private T getValue() {
        assert state == State.DONE;
        return Unchecked.cast(myObject);
    }

    //endregion

    /**
     * Generates my {@link T} if I haven't yet, then returns it.
     * If I am:
     * <ul>
     *     <li>{@link State#FRESH}, invoke my supplier to generate my {@link T}.</li>
     *     <li>{@link State#DONE}, return my {@link T}.</li>
     *     <li>{@link State#FAILED}, re-{@code throw} the original exception.</li>
     * </ul>
     *
     * @return my {@link T} value
     * @throws NullPointerException if my supplier returned a {@code null} value
     */
    @Override
    public @NotNull T getChecked() throws Throwable {
        if (state == State.FRESH) {
            synchronized (this) {
                if (state == State.FRESH) {
                    try {
                        myObject = getSupplier().get();
                        state    = State.DONE;
                    } catch (Throwable e) {
                        myObject = e;
                        state    = State.FAILED;
                    }
                }
            }
        }

        if (state == State.FAILED) {
            throw getException();
        }

        return Objects.requireNonNull(getValue(), NULL_VALUE_MESSAGE);
    }
}
