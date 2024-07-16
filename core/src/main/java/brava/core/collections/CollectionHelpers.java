package brava.core.collections;

import brava.core.Unchecked;
import com.google.common.collect.ObjectArrays;
import com.google.errorprone.annotations.DoNotCall;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CollectionHelpers {
    /**
     * An implementation for {@link java.util.Collection#toArray(Object[])}.
     */
    public static <T, T2> T2[] toArray(Collection<T> stuff, T2[] a) {
        // Is this efficient? Who cares? This method is stupid.
        var destination = prepDestinationArray(a, stuff.size());

        int i = 0;
        for (var it : stuff) {
            destination[i] = Unchecked.cast(it);
            i++;
        }

        return destination;
    }

    private static <T> T[] prepDestinationArray(T[] existingArray, int desiredSize) {
        if (desiredSize <= existingArray.length) {
            if (desiredSize < existingArray.length) {
                existingArray[desiredSize] = null;
            }

            return existingArray;
        }

        return ObjectArrays.newArray(existingArray, desiredSize);
    }

    static final class SubList<T> implements ListBase<T> {
        private final List<T> source;
        private final int offset;
        private final int length;

        public SubList(List<T> source, int offset, int length) {
            this.source = source;
            this.offset = offset;
            this.length = length;
        }

        @Override
        public int size() {
            return length;
        }

        @Override
        public @NotNull Iterator<T> iterator() {
            return source.stream()
                  .skip(offset)
                  .limit(length)
                  .iterator();
        }

        @Override
        public T get(int index) {
            return source.get(offset + index);
        }
    }

    static final class ImmutableListIterator<T> implements ListIterator<T> {
        private final List<T> source;
        private int position;
        private final int size;

        public ImmutableListIterator(ListBase<T> source, int position) {
            this.source = source;
            this.position = position;
            this.size = source.size();
        }

        @Override
        public boolean hasNext() {
            return position < size;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return source.get(position++);
        }

        @Override
        public boolean hasPrevious() {
            return position > 0;
        }

        @Override
        public T previous() {
            if (!hasPrevious()) {
                throw new NoSuchElementException();
            }
            return source.get(--position);
        }

        @Contract(pure = true)
        @DoNotCall
        @Override
        public int nextIndex() {
            return position;
        }

        @Contract(pure = true)
        @DoNotCall
        @Override
        public int previousIndex() {
            return position - 1;
        }

        @Contract(value = " -> fail", pure = true)
        @DoNotCall
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Contract(value = "_ -> fail", pure = true)
        @DoNotCall
        @Override
        public void set(T t) {
            throw new UnsupportedOperationException();
        }

        @Contract(value = "_ -> fail", pure = true)
        @DoNotCall
        @Override
        public void add(T t) {
            throw new UnsupportedOperationException();
        }
    }
}
