package brava.core;

import brava.core.exceptions.UncheckedReflectionException;
import brava.core.exceptions.UnreachableException;
import com.google.common.collect.MoreCollectors;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.function.Function;

@ParametersAreNonnullByDefault
final class RecordGetterHelpers {
    private static RecordComponent getRecordComponent(SerializedLambda serializedGetter) {
        var componentName = getNameFromMethodReference(serializedGetter);
        var recordClass = getRecordClass(serializedGetter);
        var components = recordClass.getRecordComponents();

        return Arrays.stream(components)
              .filter(it -> it.getName().equals(componentName))
              .collect(MoreCollectors.onlyElement());
    }

    @Contract(pure = true)
    private static @NotNull Class<? extends Record> getRecordClass(SerializedLambda serializedLambda) {
        var implClassName = serializedLambda.getImplClass().replace("/", ".");
        final Class<?> loaded;
        try {
            loaded = Thread.currentThread().getContextClassLoader().loadClass(implClassName);
        } catch (ClassNotFoundException e) {
            throw new UncheckedReflectionException(e);
        }

        if (!loaded.isRecord()) {
            throw new IllegalArgumentException("The type %s must be a %s!".formatted(loaded, Record.class));
        }

        @SuppressWarnings("unchecked" /* We just checked this by using `.isRecord()` */)
        var cast = (Class<? extends Record>) loaded;
        return cast;
    }

    /**
     * Converts a {@link Serializable} {@link FunctionalInterface} into a {@link SerializedLambda}.
     *
     * @param lambda a lambda expression for a {@link FunctionalInterface} that implements {@link Serializable}.
     * @return a new {@link SerializedLambda}
     * @throws UncheckedReflectionException if something goes wrong when we try to hack into the mainframe
     */
    @SuppressWarnings("java:S3011")
    @Contract(pure = true)
    private static SerializedLambda toSerializedLambda(Serializable lambda) {
        final Object replaced;
        try {
            var writeReplace = lambda.getClass().getDeclaredMethod("writeReplace");
            writeReplace.setAccessible(true);
            replaced = writeReplace.invoke(lambda);
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            throw new UncheckedReflectionException(e);
        }

        if (replaced instanceof SerializedLambda serializedLambda) {
            return serializedLambda;
        } else {
            throw new UnreachableException("%s should be a serializable lambda type!".formatted(lambda.getClass()));
        }
    }

    @Contract(pure = true)
    public static <GETTER extends Function<? extends Record, ?> & Serializable> RecordComponent getRecordComponent(GETTER getterMethodReference) {
        var serializedGetter = toSerializedLambda(getterMethodReference);
        return getRecordComponent(serializedGetter);
    }

    private static String getNameFromMethodReference(SerializedLambda methodReference) {
        var name = methodReference.getImplMethodName();

        if (name.contains("$")) {
            throw new IllegalArgumentException("The `implMethodName` of the lambda `%s` was `%s`, which contains `$`, implying that the lambda isn't a method reference!".formatted(methodReference, name));
        }

        return name;
    }
}
