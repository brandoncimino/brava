package brava.core;

import brava.core.exceptions.UnreachableException;
import com.google.common.base.Equivalence;
import com.google.common.collect.Iterables;
import com.google.common.reflect.TypeToken;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.function.Function;

/**
 * Hacker tools for working with {@link Record}s.
 *
 * @implNote The first time you call {@link Class#getRecordComponents()} appears to be quite slow (up to a second), but it becomes almost instant after that 🤷‍♀️
 */
public final class Records {
    //region Constructor stuff

    /**
     * @param recordType a {@link Record} type
     * @param <R>        the type of {@link Record}
     * @return the <a href="https://docs.oracle.com/javase/specs/jls/se21/html/jls-8.html#jls-8.10.4">canonical constructor</a> for {@link R}
     */
    @Contract(pure = true)
    public static <R extends Record> @NotNull Constructor<R> getCanonicalConstructor(@NotNull TypeToken<R> recordType) {
        @SuppressWarnings("unchecked" /* Records are always `final`, so this is a safe cast. */)
        var rType = (Class<R>) recordType.getRawType();
        return getCanonicalConstructor(rType);
    }

    /**
     * @param recordType a {@link Record} type
     * @return the <a href="https://docs.oracle.com/javase/specs/jls/se21/html/jls-8.html#jls-8.10.4">canonical constructor</a> for {@link R}
     * @param <R> the type of {@link Record}
     */
    @Contract(pure = true)
    public static <R extends Record> @NotNull Constructor<R> getCanonicalConstructor(@NotNull Class<R> recordType) {
        var componentTypes = Arrays.stream(recordType.getRecordComponents())
              .map(RecordComponent::getType)
              .toArray(Class<?>[]::new);

        try {
            return recordType.getConstructor(componentTypes);
        } catch (NoSuchMethodException e) {
            throw new UnreachableException(e);
        }
    }

    /**
     * Instantiates {@link R} using its <a href="https://docs.oracle.com/javase/specs/jls/se21/html/jls-8.html#jls-8.10.4">canonical constructor</a>.
     *
     * @param recordType      the type of {@link Record}
     * @param componentValues the arguments to the canonical {@link Constructor#newInstance(Object...)}
     * @param <R>             the {@link Record} type
     * @return a new {@link R} instance
     */
    @Contract(pure = true, value = "_, _ -> new")
    public static <R extends Record> @NotNull R construct(
        @NotNull TypeToken<R> recordType,
        @NotNull Iterable<?> componentValues
    ) {
        var constructor = getCanonicalConstructor(recordType);
        var args        = Iterables.toArray(componentValues, Object.class);
        return Reflection.invoke(() -> constructor.newInstance(args));
    }

    //endregion

    /**
     * @param rec an instance of {@link R}
     * @param component one of {@link R}'s {@link RecordComponent}s
     * @return the result of the {@link RecordComponent#getAccessor()}
     * @param <R> the {@link Record} type
     * @throws IllegalArgumentException if the {@code component} isn't a member of {@link R}
     */
    @Contract(pure = true)
    public static <R extends Record> @Nullable Object getComponentValue(@NotNull R rec, @NotNull RecordComponent component) {
        if (!component.getDeclaringRecord().isInstance(rec)) {
            throw new IllegalArgumentException("The given %s `%s` is a part of %s, not %s!".formatted(RecordComponent.class.getSimpleName(), component.getName(), component.getDeclaringRecord(), rec.getClass()));
        }

        try {
            return component.getAccessor().invoke(rec);
        } catch (InvocationTargetException e) {
            throw new UnreachableException("Somehow, the %s `%s`'s accessor threw an exception - how is that possible?!", e);
        } catch (IllegalAccessException e) {
            throw new UnreachableException("Somehow, the %s `%s` didn't have a public accessor - how is that possible?!".formatted(RecordComponent.class.getSimpleName(), component), e);
        }
    }

    /**
     * Captures a {@link GetterMethod} from a <a href="https://docs.oracle.com/javase/tutorial/java/javaOO/methodreferences.html">method reference</a> to a {@link RecordComponent#getAccessor()}.
     * From there, you can essentially use it as a type-safe wrapper around {@link RecordComponent}.
     *
     * <h1>Example</h1>
     * <pre>{@code
     * record Vinyl(String artist, String title) { }
     *
     * Record.Getter<Vinyl, String> artistGetter = Records.getterMethod(Vinyl::artist);
     *
     * var fat = new Vinyl("\"Weird Al\" Yankovic", "Fat");
     *
     * String fatArtist = artistGetter.getValueFrom(fat); // => "Weird Al" Yankovic
     * }</pre>
     *
     * @param getterMethodReference a <a href="https://docs.oracle.com/javase/tutorial/java/javaOO/methodreferences.html">method reference</a> to a {@link RecordComponent#getAccessor()}
     * @param <R>                   the {@link Record} type
     * @param <T>                   the {@link RecordComponent#getType()}
     * @return a {@link GetterMethod}
     * @throws IllegalArgumentException if the given {@link GetterMethod} isn't a method reference
     */
    @Contract(pure = true)
    public static <R extends @NotNull Record, T> @NotNull Comp<R, T> getComponent(
        @NotNull GetterMethod<R, T> getterMethodReference
    ) {
        return new Comp<>(getterMethodReference);
    }

    /**
     * Determines if the given {@link RecordComponent} instances actually refer to the same {@link RecordComponent#getAccessor()}.
     *
     * <h1>Example</h1>
     * <pre>{@code
     * record Box(Object value) { }
     *
     * var a = Box.class.getRecordComponents()[0];
     * var b = Box.class.getRecordComponents()[0];
     *
     * a.equals(b);             // => ❌ false
     * areSameComponent(a, b);  // => ✅ true
     * }</pre>
     *
     * @param a the first {@link RecordComponent}
     * @param b the second {@link RecordComponent}
     * @return {@code true} if they have the same {@link RecordComponent#getAccessor()}
     */
    @Contract(pure = true, value = "null, null -> true; null, !null -> false; !null, null -> false")
    public static boolean areSameComponent(@Nullable RecordComponent a, @Nullable RecordComponent b) {
        return recordComponentEquivalence.equivalent(a, b);
    }

    /**
     * Represents a {@link RecordComponent} captured via {@link GetterMethod}.
     *
     * @param <R> the {@link Record} type
     * @param <T> the {@link RecordComponent#getType()}
     */
    public static final class Comp<R extends @NotNull Record, T> implements Function<@NotNull R, T> {
        /**
         * A {@link Function} that invokes the {@link RecordComponent#getAccessor()}.
         */
        private final @NotNull Function<R, T> getterMethod;
        /**
         * The underlying {@link RecordComponent}.
         */
        private final @NotNull RecordComponent recordComponent;

        private Comp(@NotNull GetterMethod<R, T> getterMethod) {
            this.getterMethod = getterMethod;
            this.recordComponent = RecordGetterHelpers.getRecordComponent(getterMethod);
        }

        @Override
        public T apply(@NotNull R r) {
            return getterMethod.apply(r);
        }

        @Contract(pure = true)
        public @NotNull RecordComponent getRecordComponent() {
            return recordComponent;
        }

        /**
         * @param obj another {@link Comp}
         * @return {@code true} if we {@link #areSameComponent(RecordComponent, RecordComponent)}
         */
        @Contract(pure = true, value = "null -> false")
        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj instanceof Comp<?, ?> other) {
                return recordComponentEquivalence.equivalent(getRecordComponent(), other.getRecordComponent());
            }

            return false;
        }

        /**
         * @param other another {@link RecordComponent}
         * @return {@code true} if we {@link #areSameComponent(RecordComponent, RecordComponent)}
         */
        @Contract(pure = true, value = "null -> false")
        public boolean isSameComponentAs(@Nullable RecordComponent other) {
            return Records.areSameComponent(getRecordComponent(), other);
        }

        @Override
        public int hashCode() {
            return recordComponentEquivalence.hash(getRecordComponent());
        }
    }

    static final Equivalence<RecordComponent> recordComponentEquivalence = Equivalence.equals().onResultOf(RecordComponent::getAccessor);

    /**
     * Used to capture <a href="https://docs.oracle.com/javase/tutorial/java/javaOO/methodreferences.html">method reference</a>s to {@link RecordComponent#getAccessor()}s.
     *
     * @param <R> the {@link Record} type
     * @param <T> the {@link RecordComponent#getType()}
     */
    @FunctionalInterface
    @ApiStatus.NonExtendable
    public interface GetterMethod<R extends @NotNull Record, T> extends Function<R, T>, Serializable {

    }

    //region Mutations

    /**
     * @param original the original {@link R}
     * @return a new {@link RecordBuilder} populated with {@code original}'s component values.
     * @see RecordBuilder#from(Record)
     */
    @Contract(value = "_ -> new", pure = true)
    public static <R extends @NotNull Record> @NotNull RecordBuilder<R> builder(@NotNull R original) {
        return RecordBuilder.from(original);
    }

    /**
     * Creates a new instance of {@link R} with one of its {@link RecordComponent}s modified.
     *
     * @param original the original {@link R}
     * @param getter   a {@link GetterMethod} reference to one of {@link R}'s {@link RecordComponent#getAccessor()}s
     * @param value    the new value for the {@link RecordComponent}
     * @return a new instance of {@link R}
     * @apiNote If you need to modify multiple components, you should instead call {@link #builder(Record)} and then chain multiple {@link RecordBuilder#with(GetterMethod, Object)} calls together.
     */
    public static <R extends @NotNull Record, T> @NotNull R with(
        @NotNull R original,
        @NotNull GetterMethod<R, T> getter,
        T value
    ) {
        return RecordBuilder.from(original)
            .with(getter, value)
            .build();
    }

    //endregion
}
