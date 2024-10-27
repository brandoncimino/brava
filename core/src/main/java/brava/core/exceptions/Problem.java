package brava.core.exceptions;

import brava.core.Lazy;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Streams;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents something that went wrong, and how bad it was.
 *
 * @param severity how bad the problem is
 * @param message  describes the problem
 * @param cause    the {@link Throwable} that caused the problem <i>(if there was one)</i>
 */
public record Problem(
    @NotNull Severity severity, @NotNull Lazy<@NotNull String> message, @NotNull Optional<Throwable> cause
) {
    /**
     * Decides what to do with the problem.
     */
    public enum Severity {
        /**
         * You should be ashamed, but we're going to let it slide. <i>This time</i>.
         */
        WARNING,
        /**
         * You are in <i>{@link BigProblemException big trouble}</i>.
         */
        ERROR;

        @Contract(pure = true)
        public @NotNull String icon() {
            return switch (this) {
                case WARNING -> "⚠";
                case ERROR -> "💣";
            };
        }
    }

    /**
     * Thrown when we encounter a {@link Problem} with a {@link Severity#ERROR}.
     */
    public static final class BigProblemException extends RuntimeException {
        public BigProblemException(@NotNull Problem problem) {
            super(problem.toString(), problem.cause.orElse(null));
        }

        public BigProblemException(@NotNull Collection<Problem> problems) {
            super(formatProblemList(problems));
        }

        private static @NotNull String formatProblemSummary(@NotNull Collection<Problem> problems) {
            Preconditions.checkArgument(!problems.isEmpty(), "I can't report 0 problems!");

            var groupedBySeverity = problems.stream()
                .collect(Collectors.groupingBy(Problem::severity));

            return groupedBySeverity.entrySet()
                .stream()
                .map(it -> "%s %s".formatted(it.getValue().size(), it.getKey().icon()))
                .collect(
                    Collectors.joining(
                        " / ",
                        "Found %s problems (".formatted(problems.size()),
                        ")"
                    )
                );
        }

        private static String formatProblemList(@NotNull Collection<Problem> problems) {
            Preconditions.checkArgument(!problems.isEmpty(), "I can't report 0 problems!");

            var maxDigits = (problems.size() + "").length();
            return Streams.mapWithIndex(
                    problems.stream(),
                    (from, index) -> "  [%s]".formatted(
                        Strings.padStart(index + "", maxDigits, ' ')
                    )
                )
                .collect(Collectors.joining(
                    "\n",
                    formatProblemSummary(problems) + "\n",
                    "\n"
                ));
        }
    }

    @Override
    public String toString() {
        return Stream.of(
                severity.icon(),
                message.get(),
                cause.map("(caused by: %s)"::formatted)
                    .orElse("")
            )
            .filter(it -> it.isBlank() == false)
            .collect(Collectors.joining(" "));
    }

    /**
     * Based on my {@link #severity}, either:
     * <ul>
     *     <li>{@link Severity#WARNING WARNING} → invokes {@code logger}</li>
     *     <li>{@link Severity#ERROR ERROR} → throws a {@link BigProblemException}</li>
     * </ul>
     *
     * @param logger what to do if my {@link #severity} is {@link Severity#WARNING}
     * @throws BigProblemException if my {@link #severity} is {@link Severity#ERROR}
     */
    public void logOrThrow(@NotNull BiConsumer<String, Optional<Throwable>> logger) {
        switch (severity) {
            case WARNING -> logger.accept(this.toString(), this.cause);
            case ERROR -> throw new BigProblemException(this);
        }
    }

    /**
     * Similar to {@link #logOrThrow(BiConsumer)}, but shorthanded for the common use case of a standard {@link java.lang.System.Logger}.
     * Based on my {@link #severity}, either:
     * <table>
     *     <tr>
     *         <td>{@link Severity#WARNING}</td>
     *         <td>logs to {@code logger} at {@link java.lang.System.Logger.Level#WARNING}</td>
     *     </tr>
     *     <tr>
     *         <td>{@link Severity#ERROR}</td>
     *         <td>throws a {@link BigProblemException}</td>
     *     </tr>
     * </table>
     *
     * @param logger the {@link System.Logger} for {@link java.lang.System.Logger.Level#WARNING} if my {@link #severity} is {@link Severity#WARNING}
     * @throws BigProblemException if my {@link #severity} is {@link Severity#ERROR}
     * @see #logOrThrow(BiConsumer)
     * @see #logOrThrow(Severity, System.Logger, Supplier, Optional)
     */
    public void logOrThrow(@NotNull System.Logger logger) {
        logOrThrow((s, t) -> logger.log(System.Logger.Level.WARNING, s, t));
    }

    /**
     * {@code static} shorthand for {@link #logOrThrow(BiConsumer)}.
     *
     * @param severity the {@link #severity} of the {@link Problem}
     * @param logger   what to do if {@code severity} is {@link Severity#WARNING}
     * @param message  explains what went wrong
     * @param cause    the {@link Throwable} that is to blame <i>(if there is one)</i>
     * @throws BigProblemException if {@code severity} is {@link Severity#ERROR}
     * @see #logOrThrow(BiConsumer)
     */
    public static void logOrThrow(
        Severity severity,
        BiConsumer<String, Optional<Throwable>> logger,
        Supplier<String> message,
        Optional<Throwable> cause
    ) {
        new Problem(
            severity,
            Lazy.of(message::get),
            cause
        )
            .logOrThrow(logger);
    }

    /**
     * {@code static} shorthand for {@link #logOrThrow(java.lang.System.Logger)}.
     *
     * @param severity the {@link #severity} of the {@link Problem}
     * @param logger   what to do if {@code severity} is {@link Severity#WARNING}
     * @param message  explains what went wrong
     * @param cause    the {@link Throwable} that is to blame <i>(if there is one)</i>
     * @throws BigProblemException if {@code severity} is {@link Severity#ERROR}
     * @see #logOrThrow(java.lang.System.Logger)
     */
    public static void logOrThrow(
        Severity severity,
        System.Logger logger,
        Supplier<String> message,
        Optional<Throwable> cause
    ) {
        new Problem(
            severity,
            Lazy.of(message::get),
            cause
        ).logOrThrow(logger);
    }
}
