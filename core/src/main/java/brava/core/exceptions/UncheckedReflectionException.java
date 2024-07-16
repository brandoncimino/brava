package brava.core.exceptions;

/**
 * Wraps checked {@link ReflectiveOperationException}, similarly to {@link java.io.UncheckedIOException}. 
 */
public final class UncheckedReflectionException extends RuntimeException {
    public UncheckedReflectionException(String message, ReflectiveOperationException cause) {
        super(message, cause);
    }
    public UncheckedReflectionException(ReflectiveOperationException cause) {
        super(cause);
    }
}
