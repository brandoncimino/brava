package brava;

import brava.exceptions.UncheckedReflectionException;
import com.google.common.base.Equivalence;
import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.NoSuchElementException;

public final class RecordBuilder<R extends @NotNull Record> {
    private final TypeToken<R> recordType;
    private final RecordComponent[] recordComponents;
    private final HashMap<Equivalence.Wrapper<RecordComponent>, Object> components;

    public static <R extends @NotNull Record> RecordBuilder<R> ofType(TypeToken<R> recordType) {
        return new RecordBuilder<>(recordType);
    }

    public static <R extends @NotNull Record> RecordBuilder<R> ofType(Class<R> recordType) {
        return new RecordBuilder<>(TypeToken.of(recordType));
    }

    private RecordBuilder(TypeToken<R> recordType) {
        this(recordType, new HashMap<>());
    }

    private RecordBuilder(TypeToken<R> recordType, HashMap<Equivalence.Wrapper<RecordComponent>, Object> components) {
        this.recordType = recordType;
        this.components = components;
        this.recordComponents = recordType.getRawType().getRecordComponents();
    }

    public Object get(RecordComponent component) {
        Preconditions.checkArgument(recordType.isSupertypeOf(component.getDeclaringRecord()));

        var wrapped = Records.recordComponentEquivalence.wrap(component);

        if (components.containsKey(wrapped)) {
            return components.get(wrapped);
        } else {
            throw new NoSuchElementException("The key %s wasn't found in %s!".formatted(component, components));
        }
    }

    public Object set(RecordComponent component, Object value) {
        Preconditions.checkArgument(recordType.isSupertypeOf(component.getDeclaringRecord()));

        var wrapped = Records.recordComponentEquivalence.wrap(component);
        return components.put(wrapped, value);
    }

    public static <R extends @NotNull Record> RecordBuilder<R> from(R rec) {
        @SuppressWarnings("unchecked")
        var recordClass = (Class<R>) rec.getClass();
        var recordToken = TypeToken.of(recordClass);
        var builder = new RecordBuilder<>(recordToken);

        var components = recordClass.getRecordComponents();
        for (var comp : components) {
            builder.set(comp, Records.getComponentValue(rec, comp));
        }

        return builder;
    }

    @Contract(pure = true)
    public R build() {
        if (this.components.size() != recordComponents.length) {
            throw new IllegalStateException("Expected %s record component values, but only found %s: %s".formatted(recordComponents.length, this.components.size(), this.components));
        }

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
