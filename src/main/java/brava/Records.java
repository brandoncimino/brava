package brava;

import brava.exceptions.UnreachableException;
import com.google.common.base.Equivalence;
import com.google.common.collect.MoreCollectors;
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
 */
public final class Records {
    @Contract(pure = true)
    public static <R extends Record> @NotNull Constructor<R> getCanonicalConstructor(@NotNull TypeToken<R> recordType) {
        @SuppressWarnings("unchecked" /* Records are always `final`, so this is a safe cast. */)
        var rType = (Class<R>) recordType.getRawType();
        return getCanonicalConstructor(rType);
    }

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

    @Nullable
    @Contract(pure = true)
    public static <R extends Record> Object getComponentValue(@NotNull R rec, @NotNull RecordComponent component) {
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
    public static <R extends @NotNull Record, T> Comp<R, T> getComponentByGetter(GetterMethod<R, T> getterMethodReference) {
        return new Comp<>(getterMethodReference);
    }

    @Contract(pure = true)
    @NotNull
    public static RecordComponent getComponentByName(Class<? extends Record> recordType, String componentName) {
        return Arrays.stream(recordType.getRecordComponents())
              .filter(it -> it.getName().equals(componentName))
              .collect(MoreCollectors.onlyElement());
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
}
