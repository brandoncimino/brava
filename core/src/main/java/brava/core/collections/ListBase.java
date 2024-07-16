package brava.core.collections;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A skeletal implementation of {@link List} using {@code default} interface methods.
 *
 * @param <T> the element type
 */
public interface ListBase<T> extends CollectionBase<T>, List<T> {
    @Override
    default boolean addAll(int index, @NotNull Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    default T set(int index, T element) {
        throw new UnsupportedOperationException();
    }

    @Override
    default void add(int index, T element) {
        throw new UnsupportedOperationException();
    }

    @Override
    default T remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    default int indexOf(Object o) {
        for (int i = 0; i < size(); i++) {
            if (Objects.equals(get(i), o)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    default int lastIndexOf(Object o) {
        for (int i = size() - 1; i >= 0; i--) {
            if (Objects.equals(get(i), o)) {
                return i;
            }
        }
        return -1;
    }

    @NotNull
    @Override
    @ApiStatus.NonExtendable
    default ListIterator<T> listIterator() {
        return listIterator(0);
    }

    @NotNull
    @Override
    default ListIterator<T> listIterator(int index) {
        return new CollectionHelpers.ImmutableListIterator<>(this, index);
    }

    @NotNull
    @Override
    @ApiStatus.NonExtendable
    default Iterator<T> iterator() {
        return listIterator();
    }

    @NotNull
    @Override
    default List<T> subList(int fromIndex, int toIndex) {
        Preconditions.checkPositionIndex(fromIndex, this.size(), "fromIndex");
        Preconditions.checkPositionIndex(toIndex, this.size(), "toIndex");
        return new CollectionHelpers.SubList<>(this, fromIndex, toIndex - fromIndex);
    }

    @Override
    default boolean isEmpty() {
        return CollectionBase.super.isEmpty();
    }

    @Override
    default boolean contains(Object o) {
        return CollectionBase.super.contains(o);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    @NotNull
    default Object[] toArray() {
        return CollectionBase.super.toArray();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    default <T1> @NotNull T1[] toArray(@NotNull T1[] a) {
        return CollectionBase.super.toArray(a);
    }

    @Override
    default boolean add(T t) {
        return CollectionBase.super.add(t);
    }

    @Override
    default boolean remove(Object o) {
        return CollectionBase.super.remove(o);
    }

    @Override
    default boolean containsAll(@NotNull Collection<?> c) {
        return CollectionBase.super.containsAll(c);
    }

    @Override
    default boolean addAll(@NotNull Collection<? extends T> c) {
        return CollectionBase.super.addAll(c);
    }

    @Override
    default boolean removeAll(@NotNull Collection<?> c) {
        return CollectionBase.super.removeAll(c);
    }

    @Override
    default boolean retainAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    default void clear() {
        CollectionBase.super.clear();
    }
}
