package brava.core;

import brava.core.exceptions.UncheckedReflectionException;
import com.google.common.collect.Iterables;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Utilities for working with {@link java.lang.reflect}.
 */
public final class Reflection {
    /**
     * Invokes a {@link Constructor}, wrapping {@link ReflectiveOperationException}s in {@link UncheckedReflectionException}s.
     *
     * @param constructor a {@link Constructor}
     * @param args        the arguments to the constructor
     * @param <T>         the constructed type
     * @return a new {@link T}
     * @throws UncheckedReflectionException if an {@link InvocationTargetException}, {@link InstantiationException}, or {@link IllegalAccessException} occurs
     */
    @Contract("_,_ -> new")
    public static <T> @NotNull T invokeConstructor(@NotNull Constructor<T> constructor, @Nullable Object @NotNull ... args) {
        try {
            return constructor.newInstance(args);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new UncheckedReflectionException(e);
        }
    }

    /**
     * Invokes a {@link Constructor}, wrapping {@link ReflectiveOperationException}s in {@link UncheckedReflectionException}s.
     *
     * @param constructor a {@link Constructor}
     * @param args        the arguments to the constructor
     * @param <T>         the constructed type
     * @return a new {@link T}
     * @throws UncheckedReflectionException if an {@link InvocationTargetException}, {@link InstantiationException}, or {@link IllegalAccessException} occurs
     */
    public static <T> T invokeConstructor(@NotNull Constructor<T> constructor, @NotNull Iterable<? extends @Nullable Object> args) {
        return invokeConstructor(constructor, Iterables.toArray(args, Object.class));
    }

    /**
     * Invokes a {@link Method}, wrapping {@link ReflectiveOperationException}s in {@link UncheckedReflectionException}s.
     *
     * @param method a {@link Method}
     * @param args   the arguments to the method
     * @return the result of the method
     * @throws UncheckedReflectionException if an {@link InvocationTargetException}  or {@link IllegalAccessException} occurs
     */
    public static @Nullable Object invokeMethod(@NotNull Method method, @Nullable Object instance, @Nullable Object @NotNull ... args) {
        try {
            return method.invoke(instance, args);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new UncheckedReflectionException(e);
        }
    }

    /**
     * Invokes a {@link Method}, wrapping {@link ReflectiveOperationException}s in {@link UncheckedReflectionException}s.
     *
     * @param method a {@link Method}
     * @param args   the arguments to the method
     * @return the result of the method
     * @throws UncheckedReflectionException if an {@link InvocationTargetException}  or {@link IllegalAccessException} occurs
     */
    public static Object invokeMethod(@NotNull Method method, @Nullable Object instance, @NotNull Iterable<? extends @Nullable Object> args) {
        return invokeMethod(method, instance, Iterables.toArray(args, Object.class));
    }
}
