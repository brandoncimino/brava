package brava.core.collections;

import brava.core.BiTruth;
import brava.core.exceptions.UnreachableException;
import brava.core.functional.TriFunction;
import brava.core.tuples.Tuple;
import brava.core.tuples.Tuple2;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Methods for "updating" {@link ImmutableCollection}s.
 */
public final class Immutably {
    public static <T> ImmutableList<T> add(Iterable<T> list, T element) {
        if (list instanceof Collection<T> collection && collection.isEmpty()) {
            return ImmutableList.of(element);
        }

        return Stream.concat(
            Streams.stream(list),
            Stream.of(element)
        ).collect(ImmutableList.toImmutableList());
    }

    public static <T> ImmutableList<T> insert(List<T> list, T element, int index) {
        Preconditions.checkPositionIndex(index, list.size());

        if (list.isEmpty()) {
            assert index == 0;
            return ImmutableList.of(element);
        }

        var erator = list.iterator();
        var result = IntStream.range(0, list.size() + 1)
            .mapToObj(i -> i == index ? element : erator.next())
            .collect(ImmutableList.toImmutableList());

        assert erator.hasNext() == false;
        return result;
    }

    @Contract(pure = true, value = "_,_,_->new")
    public static <K, V> @NotNull ImmutableMap<K, V> put(
        @NotNull Map<@NotNull K, @NotNull V> map,
        @NotNull K key,
        @NotNull V value
    ) {
        if (map.isEmpty()) {
            return ImmutableMap.of(key, value);
        }

        return ImmutableMap.<K, V>builder()
            .putAll(map)
            .put(key, value)
            .buildKeepingLast();
    }

    @SuppressWarnings("java:S2583")
    public static <K, V> Tuple2<ImmutableMap<K, V>, V> remove(
        @NotNull Map<@NotNull K, @NotNull V> map,
        @NotNull K key
    ) {
        var oldValue = map.get(key);
        if (oldValue == null) {
            throw new IllegalArgumentException(
                "Can't remove the key %s because is isn't present in %s!".formatted(key, map));
        }

        var newMap = map.entrySet()
            .stream()
            .filter(it -> it.getKey().equals(key) == false)
            .collect(ImmutableMap.toImmutableMap(
                Map.Entry::getKey,
                Map.Entry::getValue
            ));

        return Tuple.of(newMap, oldValue);
    }

    public static <K, V> ImmutableMap<K, V> merge(
        Map<K, V> a,
        Map<K, V> b,
        TriFunction<? super K, ? super V, ? super V, ? extends V> arbiter
    ) {
        if (a.isEmpty()) {
            return ImmutableMap.copyOf(b);
        }

        if (b.isEmpty()) {
            return ImmutableMap.copyOf(a);
        }

        return Stream.concat(
                a.keySet().stream(),
                b.keySet().stream()
            )
            .distinct()
            .map(k -> {
                var value = switch (BiTruth.of(a, b, it -> it.containsKey(k))) {
                    case A -> a.get(k);
                    case B -> b.get(k);
                    case BOTH -> arbiter.apply(k, a.get(k), b.get(k));
                    case NEITHER -> throw new UnreachableException("How did %s get in here?!".formatted(k));
                };

                return Map.entry(k, value);
            })
            .collect(ImmutableMap.toImmutableMap(
                Map.Entry::getKey,
                Map.Entry::getValue
            ));
    }
}
