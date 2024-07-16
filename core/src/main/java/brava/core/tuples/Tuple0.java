package brava.core.tuples;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public record Tuple0() implements Tuple<Tuple0> {
    @NotNull
    public static final Tuple0 INSTANCE = new Tuple0();

    @Contract(pure = true)
    @ApiStatus.Internal
    @SuppressWarnings("java:S6207")
    public Tuple0 {
        // This method should not be called directly.
        // However, I'm allowing it because:
        //  1. It's a part of the contract for `record` types.
        //  2. Instances might get constructed via reflection, such as by Jackson. 
    }

    /**
     * Ideally, you would always use the singleton {@link #INSTANCE}.
     * However, if for some reason you have a new, unique instance, or if you aren't sure,
     * you can call this method to "replace" it with the singleton.
     *
     * @return the singleton {@link #INSTANCE}
     * @apiNote The name is intended to be reminiscent of {@link String#intern()}.
     */
    @Contract(pure = true)
    public @NotNull Tuple0 intern() {
        return INSTANCE;
    }

    @Override
    public Object get(int index) {
        throw new IndexOutOfBoundsException(index);
    }

    @Override
    public int size() {
        return 0;
    }

    @Contract(pure = true)
    @Override
    public @NotNull Stream<Object> stream() {
        return Stream.empty();
    }

    @Override
    public @NotNull Tuple0 getSelf() {
        return this;
    }
}
