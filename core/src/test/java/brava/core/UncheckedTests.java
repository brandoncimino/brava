package brava.core;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

class UncheckedTests {
    public static Stream<Throwable> exceptions() {
        return Stream.of(
            new NullPointerException(),
            new IOException(),
            new InstantiationError(),
            new IllegalAccessException(),
            new AssertionError(),
            new ArithmeticException()
        );
    }

    @ParameterizedTest
    @MethodSource("exceptions")
    void givenUncheckedSupplier_whenGetThrows_exceptionIsUnmodified(Throwable exception) {
        var supplier = Unchecked.supplier(() -> {
            throw exception;
        });

        Assertions.assertThatCode(supplier::get)
            .isSameAs(exception);
    }

    @ParameterizedTest
    @MethodSource("exceptions")
    void givenUncheckedFunction_whenApplyThrows_exceptionIsUnmodified(Throwable exception) {
        var function = Unchecked.function(it -> {
            throw exception;
        });

        Assertions.assertThatCode(() -> function.apply("yolo"))
            .isSameAs(exception);
    }
}
