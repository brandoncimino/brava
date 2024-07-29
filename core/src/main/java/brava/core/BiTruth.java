package brava.core;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * The "binary truth" represents the possible combinations of two {@link Boolean} values.
 * <p>
 * These correspond to the cells of a basic <a href="https://en.wikipedia.org/wiki/Truth_table">truth table</a>:
 * <table>
 *     <tr>
 *         <td></td>
 *         <td>🅰 ✅</td>
 *         <td>🅰 ❌</td>
 *     </tr>
 *     <tr>
 *         <td>🅱 ✅</td>
 *         <td>{@link #BOTH}</td>
 *         <td>{@link #B}</td>
 *     </tr>
 *     <tr>
 *         <td>🅱 ❌</td>
 *         <td>{@link #A}</td>
 *         <td>{@link #NEITHER}</td>
 *     </tr>
 * </table>
 */
public enum BiTruth {
    A,
    B,
    BOTH,
    NEITHER;

    /**
     * Evaluates the {@link BiTruth} of {@code a} and {@code b}.
     *
     * @param a the first condition
     * @param b the second condition
     * @return the {@link BiTruth}
     */
    @Contract(pure = true)
    public static @NotNull BiTruth of(boolean a, boolean b) {
        if (a) {
            return b ? BOTH : A;
        } else if (b) {
            return B;
        } else {
            return NEITHER;
        }
    }

    /**
     * Evaluates the {@link BiTruth} of {@code a} and {@code b} when they have a {@link Predicate} applied to them.
     *
     * @param a         the first condition
     * @param b         the second condition
     * @param predicate the predicate applied to {@code a} and {@code b}
     * @return the {@link BiTruth}
     */
    public static <T> @NotNull BiTruth of(T a, T b, @NotNull Predicate<? super T> predicate) {
        return of(predicate.test(a), predicate.test(b));
    }
}
