package brava.core.tuples;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

/**
 * A {@link Tuple0} with 0 elements.
 * <p>
 * Also useful as an alternative indicator for "nothing" when {@link Void} can't be used.
 */
public record Tuple0() implements Tuple<Tuple0> {
    @NotNull
    @SuppressWarnings("java:S1874")
    private static final Tuple0 INSTANCE = new Tuple0();

    /**
     * @return The singleton instance of {@link Tuple0}.
     */
    @Contract(pure = true)
    public static @NotNull Tuple0 instance() {
        return INSTANCE;
    }

    /**
     * @deprecated {@link Tuple0} is intended to be a <a href="https://en.wikipedia.org/wiki/Singleton_pattern">singleton</a>, so you should <b>never</b> instantiate it.
     * Instead, use {@link #instance()} or {@link Tuple#of()}.
     * <p>
     * However, this method must be {@code public} in order to satisfy the {@link Record} requirements,
     * and might be called by libraries such as Jackson.
     * <p>
     * You can use the {@link #intern()} method to ensure that you are using the singleton instance.
     */
    @Contract(pure = true)
    @ApiStatus.Internal
    @SuppressWarnings({"java:S6207", "java:S1133", "DeprecatedIsStillUsed", "DefaultAnnotationParam"})
    @Deprecated(forRemoval = false)
    public Tuple0 {
        // This method should not be called directly.
        // However, I'm allowing it because:
        //  1. It's a part of the contract for `record` types.
        //  2. Instances might get constructed via reflection, such as by Jackson. 
    }

    /**
     * Ideally, you would always use the singleton {@link #instance()}.
     * However, if for some reason you have a new, unique instance, or if you aren't sure,
     * you can call this method to "replace" it with the singleton.
     *
     * @return the singleton {@link #instance()}
     * @apiNote The name is intended to be reminiscent of {@link String#intern()}.
     */
    @Contract(pure = true)
    public @NotNull Tuple0 intern() {
        return INSTANCE;
    }

    @Contract(pure = true)
    @Override
    public Object get(int index) {
        throw new IndexOutOfBoundsException(index);
    }

    @Contract(pure = true)
    @Override
    public int size() {
        return 0;
    }

    @Contract(pure = true)
    @Override
    public @NotNull Stream<Object> stream() {
        return Stream.empty();
    }

    @Contract(pure = true)
    @Override
    public @NotNull Tuple0 getSelf() {
        return this;
    }

    /**
     * @implSpec This class is intended to be a <a href="https://en.wikipedia.org/wiki/Singleton_pattern">singleton</a>, so it should always be equal to any other {@link Tuple0}.
     * However, we can't rely solely on {@code ==}, because {@link Record}s demand the {@link Tuple0#Tuple0()} constructor be public, so it's possible that somebody make a mistake and create another instance.
     */
    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Tuple0;
    }

    /**
     * @implSpec This class is intended to be a <a href="https://en.wikipedia.org/wiki/Singleton_pattern">singleton</a>, so it should always return the same hash code.
     */
    @Contract(pure = true)
    @Override
    public int hashCode() {
        return 1;
    }
}
