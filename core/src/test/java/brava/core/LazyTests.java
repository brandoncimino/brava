package brava.core;

import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Stream;

class LazyTests {
    @Test
    void givenSupplierThrowingException_whenGet_thenThrownExceptionIsNotWrapped() {
        var exception = new Exception();
        var lazy = Lazy.of(() -> {
            throw exception;
        });

        Assertions.assertThatThrownBy(lazy::get)
            .isSameAs(exception);
    }

    @Test
    void givenParallelCalls_whenGet_thenSupplierInvokedExactlyOnce() {
        var counter = new AtomicLong();

        // The result of the supplier will always be the number of times it's been invoked.
        var lazy = Lazy.of(counter::incrementAndGet);

        var tracker    = new ConcurrencyTracker();
        int iterations = 100;
        tracker.runInParallel(iterations, i ->
            Assertions.assertThat(lazy.get())
                .isEqualTo(1)
        );

        System.out.println("tracker = " + tracker);

        // Make sure that things actually did happen concurrently
        Assertions.assertThat(tracker)
            .as(tracker.toString())
            .satisfies(
                it -> Assertions.assertThat(it.maxUsers())
                    .as("maxUsers")
                    .isGreaterThan(1),
                it -> Assertions.assertThat(it.totalUsers())
                    .as("totalUsers")
                    .isEqualTo(iterations)
            );
    }

    @Test
    void givenSupplierReturningNull_whenGet_thenExceptionIsThrown() {
        @SuppressWarnings("DataFlowIssue")
        var lazy = Lazy.of(() -> null);
        Assertions.assertThatCode(lazy::get)
            .isInstanceOf(NullPointerException.class);
    }

    @SuppressWarnings({ "ConstantConditions", "ResultOfMethodCallIgnored" })
    @Test
    void givenNullValue_whenLazyOf_thenExceptionIsThrown() {
        Integer nullInt = null;
        Assertions.assertThatCode(() -> Lazy.of(nullInt))
            .isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void givenNullableValue_whenGet_thenOptionalOfValueIsReturned(@Nullable String value) {
        var lazy = Lazy.ofNullable(() -> value);
        Assertions.assertThat(lazy.get())
            .isEqualTo(Optional.ofNullable(value));
    }

    @NullAndEmptySource
    @ParameterizedTest
    void givenNullableValue_whenLazyOfNullable_thenOptionalOfValueIsStored(@Nullable String value) {
        var lazy = Lazy.ofNullable(value);
        Assertions.assertThat(lazy.get())
            .isEqualTo(Optional.ofNullable(value));
    }

    //region Hardcore garbage-collection stuff
    public record CapturingSupplier(Object capturedObject) implements Unchecked.Supplier<String> {
        @Override
        public String getChecked() throws Throwable {
            return capturedObject.toString();
        }
    }

    record WeakStuff(Lazy<String> lazyWithCapturedObject, WeakReference<Object> weakReferenceToCapturedObject) {
        public static WeakStuff createWeakStuff(Function<Object, Lazy<String>> lazyMaker) {
            var objectToBeCaptured            = new Object();
            var weakReferenceToCapturedObject = new WeakReference<>(objectToBeCaptured);
            var lazy                          = lazyMaker.apply(objectToBeCaptured);
            return new WeakStuff(lazy, weakReferenceToCapturedObject);
        }
    }

    @SuppressWarnings("Convert2Lambda")
    public static Stream<Function<Object, Lazy<String>>> lazyMakers() {
        return Stream.of(
            obj -> Lazy.of(obj::toString),
            obj -> Lazy.of(() -> String.valueOf(obj)),
            obj -> Lazy.of(new Unchecked.Supplier<String>() {
                @Override
                public String getChecked() throws Throwable {
                    return String.valueOf(obj);
                }
            }),
            obj -> Lazy.of(new CapturingSupplier(obj))
        );
    }

    @ParameterizedTest
    @MethodSource("lazyMakers")
    void givenLazyCapturingReference_whenLazyGot_thenReferenceIsDereferenced(Function<Object, Lazy<String>> lazyMaker) {
        var weakStuff = WeakStuff.createWeakStuff(lazyMaker);

        Assertions.assertThat(weakStuff.weakReferenceToCapturedObject.refersTo(null))
            .as("Before we do anything, the references captured by the `Lazy` should exist")
            .isFalse();

        System.gc();
        Assertions.assertThat(weakStuff.weakReferenceToCapturedObject.refersTo(null))
            .as("Because we haven't called `Lazy.get()` yet, the references captured by it should NOT have been garbage collected")
            .isFalse();

        var got = weakStuff.lazyWithCapturedObject.get();
        Assertions.assertThat(got)
            .isNotNull();

        System.gc();
        Assertions.assertThat(weakStuff.weakReferenceToCapturedObject.refersTo(null))
            .as("After calling `Lazy.get()`, the references captured by the lazy SHOULD have been garbage collected")
            .isTrue();
    }
    //endregion

}
