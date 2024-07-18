package brava.core;

import brava.core.exceptions.UncheckedReflectionException;
import com.google.common.base.Equivalence;
import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.NoSuchElementException;

/**
 * Constructs a {@link Record} by {@link #set}ting individual {@link RecordComponent} values.
 *
 * @param <R> a {@link Record} type
 */
public final class RecordBuilder<R extends @NotNull Record> {
    private final TypeToken<R> recordType;
    private final RecordComponent[] recordComponents;
    private final HashMap<Equivalence.Wrapper<RecordComponent>, Object> components;

    //region Constructors & factories

    private RecordBuilder(TypeToken<R> recordType) {
        this(recordType, new HashMap<>());
    }

    private RecordBuilder(TypeToken<R> recordType, HashMap<Equivalence.Wrapper<RecordComponent>, Object> components) {
        this.recordType = recordType;
        this.components = components;
        this.recordComponents = recordType.getRawType().getRecordComponents();
    }

    @Contract(pure = true, value = "_ -> new")
    public static <R extends @NotNull Record> @NotNull RecordBuilder<R> ofType(@NotNull TypeToken<R> recordType) {
        return new RecordBuilder<>(recordType);
    }


    @Contract(pure = true, value = "_ -> new")
    public static <R extends @NotNull Record> @NotNull RecordBuilder<R> ofType(@NotNull Class<R> recordType) {
        return new RecordBuilder<>(TypeToken.of(recordType));
    }

    /**
     * @param original the original {@link R}
     * @return a new {@link RecordBuilder} populated with {@code original}'s component values
     * @see Records#builder(Record)
     */
    @Contract(pure = true, value = "_ -> new")
    public static <R extends @NotNull Record> @NotNull RecordBuilder<R> from(@NotNull R original) {
        @SuppressWarnings("unchecked")
        var recordClass = (Class<R>) original.getClass();
        var recordToken = TypeToken.of(recordClass);
        var builder = new RecordBuilder<>(recordToken);

        var components = recordClass.getRecordComponents();
        for (var comp : components) {
            builder.set(comp, Records.getComponentValue(original, comp));
        }

        return builder;
    }

    //endregion

    /**
     * @param component a {@link Class#getRecordComponents()} of {@link R}
     * @return the corresponding value for the component
     * @throws IllegalArgumentException if the given {@link RecordComponent#getDeclaringRecord()} isn't {@link R}
     * @throws NoSuchElementException   if the given {@link RecordComponent} hasn't been {@link #set(RecordComponent, Object)}
     */
    public @Nullable Object get(@NotNull RecordComponent component) {
        Preconditions.checkArgument(recordType.isSupertypeOf(component.getDeclaringRecord()));

        var wrapped = Records.recordComponentEquivalence.wrap(component);

        if (components.containsKey(wrapped)) {
            return components.get(wrapped);
        } else {
            throw new NoSuchElementException("The key %s wasn't found in %s!".formatted(component, components));
        }
    }

    /**
     * Determines the value that the given {@link RecordComponent} will have in the built {@link R}.
     *
     * @param component a {@link Class#getRecordComponents()} of {@link R}
     * @param value     the desired value for the built {@link R} instance
     * @return the previously {@link #set(RecordComponent, Object)} value, if there was one
     */
    public @Nullable Object set(@NotNull RecordComponent component, @Nullable Object value) {
        Preconditions.checkArgument(recordType.isSupertypeOf(component.getDeclaringRecord()));

        var wrapped = Records.recordComponentEquivalence.wrap(component);
        return components.put(wrapped, value);
    }

    /**
     * Similar to {@link #set(RecordComponent, Object)}, but allows for method chaining and takes in a {@link brava.core.Records.GetterMethod}.
     *
     * @param getter
     * @param value
     * @param <T>
     * @return
     */
    @Contract("_, _ -> this")
    public <T> @NotNull RecordBuilder<R> with(@NotNull Records.GetterMethod<R, T> getter, T value) {
        var component = Records.getComponent(getter);
        this.set(component.getRecordComponent(), value);
        return this;
    }

    /**
     * @param component a {@link Class#getRecordComponents()} of {@link R}
     * @return {@code true} if the component has already been {@link #set(RecordComponent, Object)}
     */
    @Contract(pure = true)
    public boolean hasComponentValue(@NotNull RecordComponent component) {
        Preconditions.checkArgument(recordType.isSupertypeOf(component.getDeclaringRecord()));
        return components.containsKey(Records.recordComponentEquivalence.wrap(component));
    }

    /**
     * @return a new instance of {@link R}
     * @throws IllegalArgumentException if you haven't provided the correct values for the {@link Records#getCanonicalConstructor(Class)}
     */
    @Contract(pure = true)
    public R build() {
        var canonicalConstructor = Records.getCanonicalConstructor(recordType);
        var constructorArgs = Arrays.stream(recordComponents)
              .map(this::get)
              .toArray();

        try {
            return canonicalConstructor.newInstance(constructorArgs);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new UncheckedReflectionException(e);
        }
    }
}
