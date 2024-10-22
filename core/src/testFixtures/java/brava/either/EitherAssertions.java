package brava.either;

import brava.core.Either;
import brava.core.Which;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.ThrowableAssert;

import javax.annotation.Nonnull;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class EitherAssertions {
    //region Method references
    public static Function<Either<?, ?>, ?> getX(Which which) {
        return switch (which) {
            case A -> Either::getA;
            case B -> Either::getB;
        };
    }

    public static Function<Either<?, ?>, Optional<?>> tryGetX(Which which) {
        return switch (which) {
            case A -> Either::tryGetA;
            case B -> Either::tryGetB;
        };
    }

    public static Function<Either<?, ?>, Stream<?>> streamX(Which which) {
        return switch (which) {
            case A -> Either::streamA;
            case B -> Either::streamB;
        };
    }
    //endregion

    /**
     * @implNote AssertJ likes to break when using {@link SoftAssertions} /
     * {@link org.assertj.core.api.ObjectAssert#satisfies(Consumer[])}. The way the assertions here are grouped is a compromise between:
     * <ul>
     *     <li>wrapping every single call in {@link ThrowableAssert#doesNotThrowAnyException()}, or</li>
     *     <li>getting empty {@link NullPointerException}s when tests fail</li>
     * </ul>
     */
    public static void validate(@Nonnull Either<?, ?> actual, @Nonnull Object expected, @Nonnull Which which) {
        Assertions.assertThat(actual)
            .as(String.valueOf(actual))
            .satisfies(
                wrapWithAsserts(it -> assert_hasX(actual, which)),
                wrapWithAsserts(it -> assert_getX(actual, expected, which)),
                wrapWithAsserts(it -> assert_tryGetX(actual, expected, which)),
                wrapWithAsserts(it -> assert_streamX(actual, expected, which)),
                wrapWithAsserts(it -> assert_getValue(actual, expected)),
                wrapWithAsserts(it -> assert_equality(actual, expected, which)),
                wrapWithAsserts(it -> Assertions.assertThat(it.hasWhich()).as("hasWhich()").isEqualTo(which)),
                wrapWithAsserts(it -> assert_toWhatIHave(actual)),
                wrapWithAsserts(it -> assert_toWhatIHaveNot(actual))
            );
    }

    public static <T> Either<T, T> createEither(Which whichHasValue, T value) {
        return switch (whichHasValue) {
            case A -> Either.ofA(value);
            case B -> Either.ofB(value);
        };
    }

    private static void assert_getValue(Either<?, ?> actual, Object expected) {
        Assertions.assertThat(actual)
            .as("getValue")
            .extracting(Either::getValue)
            .isEqualTo(expected);
    }

    private static void assert_tryGetX(Either<?, ?> actual, Object expected, Which hasWhich) {
        Assertions.assertThat(actual)
            .as("tryGet" + hasWhich)
            .extracting(
                tryGetX(hasWhich),
                InstanceOfAssertFactories.OPTIONAL
            )
            .get()
            .isEqualTo(expected);

        Assertions.assertThat(actual)
            .as("tryGet" + hasWhich.other())
            .extracting(
                tryGetX(hasWhich.other()),
                InstanceOfAssertFactories.OPTIONAL
            )
            .isEmpty();
    }

    private static void assert_hasX(Either<?, ?> actual, Which hasWhich) {
        Assertions.assertThat(actual.hasA())
            .as("has" + hasWhich)
            .isEqualTo(hasWhich == Which.A);

        Assertions.assertThat(actual.hasB())
            .as("has" + hasWhich)
            .isEqualTo(hasWhich == Which.B);
    }

    private static <A, B> void assert_streamX(Either<A, B> actual, Object expected, Which hasWhich) {
        Assertions.assertThat(actual)
            .as("stream" + hasWhich)
            .extracting(streamX(hasWhich), InstanceOfAssertFactories.STREAM)
            .singleElement()
            .isEqualTo(expected);

        Assertions.assertThat(actual)
            .as("stream" + hasWhich.other())
            .extracting(streamX(hasWhich.other()), InstanceOfAssertFactories.STREAM)
            .isEmpty();
    }

    private static <A, B> void assert_getX(Either<A, B> actual, Object expected, Which hasWhich) {
        Assertions.assertThat(actual)
            .as("get" + hasWhich)
            .extracting(getX(hasWhich))
            .isEqualTo(expected);

        var otherGetter = getX(hasWhich.other());
        Assertions.assertThatCode(() -> otherGetter.apply(actual))
            .as("get" + hasWhich.other())
            .isInstanceOf(NoSuchElementException.class);
    }

    private static void assert_equality(Either<?, ?> actual, Object expected, Which hasWhich) {
        var equivalent = switch (hasWhich) {
            case A -> Either.ofA(expected);
            case B -> Either.ofB(expected);
        };

        Assertions.assertThat(actual)
            .isEqualTo(equivalent);
    }

    private static <A, B> void assert_toWhatIHave(Either<A, B> actual) {
        var toWhatIHave = switch (actual.hasWhich()) {
            case A -> actual.toA(b -> Assertions.fail("Should not have been invoked!"));
            case B -> actual.toB(a -> Assertions.fail("Should not have been invoked!"));
        };

        Assertions.assertThat(toWhatIHave)
            .isSameAs(actual.getValue());
    }

    private static void assert_toWhatIHaveNot(Either<?, ?> actual) {
        var counter = new LongAdder();
        var got = switch (actual.hasWhich()) {
            case A -> actual.toB(a -> {
                counter.increment();
                return null;
            });
            case B -> actual.toA(b -> {
                counter.increment();
                return null;
            });
        };

        Assertions.assertThat(got).isNull();
        Assertions.assertThat(counter)
            .as("invocation count of mapper function").hasValue(1);
    }

    /**
     * Wraps any non-{@link AssertionError}s thrown by the given code in {@link AssertionError}s so that they don't break
     * {@link SoftAssertions} or {@link org.assertj.core.api.ObjectAssert#satisfies(Consumer[])}.
     *
     * @implNote Using {@link Assertions#assertThatCode(ThrowableAssert.ThrowingCallable)}.{@link ThrowableAssert#doesNotThrowAnyException()} achieves
     * a similar effect, but extremely verbose, and will nest ALL exceptions - including {@link AssertionError}s - inside of other exceptions.
     */
    private static <I> Consumer<I> wrapWithAsserts(Consumer<I> original) {
        return it -> {
            try {
                original.accept(it);
            } catch (AssertionError e) {
                throw e;
            } catch (Throwable e) {
                // This seems silly, but provides us with a MUCH more useful error message than `Assertions.fail()` does, which completely hides 
                // the actual exception
                Assertions.assertThatCode(() -> {
                    throw e;
                }).doesNotThrowAnyException();
            }
        };
    }
}
