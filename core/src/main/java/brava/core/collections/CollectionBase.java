package brava.core.collections;

import com.google.common.collect.Iterables;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

public interface CollectionBase<T> extends Collection<T> {
    @Override
    default boolean isEmpty() {
        return size() == 0;
    }

    @Override
    default boolean contains(Object o) {
        return this.stream().anyMatch(it -> Objects.equals(it, o));
    }

    @SuppressWarnings("NullableProblems")
    @NotNull
    @Override
    default Object[] toArray() {
        return Iterables.toArray(this, Object.class);
    }

    @SuppressWarnings("NullableProblems")
    @NotNull
    @Override
    default <T1> T1[] toArray(@NotNull T1[] a) {
        return CollectionHelpers.toArray(this, a);
    }

    @Override
    default boolean add(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean containsAll(@NotNull Collection<?> c) {
        return c.stream()
              .allMatch(this::contains);
    }

    @Override
    default boolean addAll(@NotNull Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean removeAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean retainAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    default void clear() {
        throw new UnsupportedOperationException();
    }
}
