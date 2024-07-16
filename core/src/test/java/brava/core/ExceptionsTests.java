package brava.core;

import org.junit.jupiter.api.Test;

public class ExceptionsTests {
    public static class SuperNullException extends NullPointerException {
    }

    @Test
    void catching2() {
        var runtime = new RuntimeException();
    }
}
