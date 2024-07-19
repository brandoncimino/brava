package brava.core;

import org.jetbrains.annotations.Contract;

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

    @Contract(pure = true)
    public static BiTruth of(boolean a, boolean b) {
        if (a) {
            return b ? BOTH : A;
        } else if (b) {
            return B;
        } else {
            return NEITHER;
        }
    }
}
