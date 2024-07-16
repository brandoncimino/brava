package brava.core.exceptions;

/**
 * Thrown by a branch of code that we didn't think was actually possible.
 */
public final class UnreachableException extends RuntimeException {
    public UnreachableException() {
        super("This code was thought to be unreachable! How did you get here?!");
    }

    public UnreachableException(String message) {
        super(message);
    }

    public UnreachableException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnreachableException(Throwable cause) {
        super(cause);
    }
}
