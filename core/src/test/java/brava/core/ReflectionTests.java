package brava.core;

import brava.core.exceptions.UncheckedReflectionException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class ReflectionTests {
    public static Stream<? extends ReflectiveOperationException> reflectionExceptions() {
        return Stream.of(
            new ReflectiveOperationException(),
            new ClassNotFoundException(),
            new IllegalAccessException(),
            new InstantiationException()
        );
    }

    public static Stream<? extends RuntimeException> runtimeExceptions() {
        return Stream.of(
            new NullPointerException(),
            new ArithmeticException(),
            new UncheckedReflectionException(new ClassNotFoundException())
        );
    }

    @ParameterizedTest
    @MethodSource("reflectionExceptions")
    void givenReflectiveOperation_whenInvokeThrows_thenExceptionIsWrapped(ReflectiveOperationException exception) {
        Assertions.assertThatCode(() ->
                Reflection.invoke(() -> {
                    throw exception;
                })
            )
            .isInstanceOf(UncheckedReflectionException.class)
            .hasCause(exception);
    }

    @ParameterizedTest
    @MethodSource("runtimeExceptions")
    void givenRuntimeException_whenInvokeThrows_thenExceptionIsUntouched(RuntimeException exception) {
        Assertions.assertThatCode(() ->
                Reflection.invoke(() -> {
                    throw exception;
                })
            )
            .isSameAs(exception);
    }
}
