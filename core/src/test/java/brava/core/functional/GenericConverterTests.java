package brava.core.functional;

import brava.core.Unchecked;
import com.google.common.reflect.TypeToken;
import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.LongAdder;

class GenericConverterTests {
    private static <T> T parseNumber(String string, TypeToken<T> numberType) {
        var rawType = numberType.unwrap().getRawType();

        final Object parsed;
        if (rawType.equals(int.class)) {
            parsed = Integer.parseInt(string);
        } else if (rawType.equals(long.class)) {
            parsed = Long.parseLong(string);
        } else if (rawType.equals(double.class)) {
            parsed = Double.parseDouble(string);
        } else if (rawType.equals(boolean.class)) {
            parsed = Boolean.parseBoolean(string);
        } else if (rawType.equals(String.class)) {
            parsed = string;
        } else if (rawType.equals(float.class)) {
            parsed = Float.parseFloat(string);
        } else {
            throw new IllegalArgumentException();
        }

        return Unchecked.cast(parsed);
    }

    @Test
    void bothWaysTest() {
        var fromCounter = new LongAdder();
        var toCounter   = new LongAdder();

        GenericConverter.BothWays<String> stringParser = new GenericConverter.BothWays<String>() {
            @Override
            public <T> T fromSource(String s, @NotNull TypeToken<T> toType) {
                fromCounter.increment();
                return parseNumber(s, toType);
            }

            @Override
            public String toSource(Object input) {
                toCounter.increment();
                return String.valueOf(input);
            }
        };

        int original  = 3;
        var converted = stringParser.convert(original, TypeToken.of(float.class));
        Assertions.assertThat(converted)
            .isInstanceOf(Float.class)
            .isEqualTo(original);

        var string         = "yolo";
        var stringToString = stringParser.convert(string, TypeToken.of(String.class));
        Assertions.assertThat(stringToString)
            .isEqualTo(string);

        Assertions.assertThat(fromCounter)
            .as("fromCounter")
            .hasValue(2);

        Assertions.assertThat(toCounter)
            .as("toCounter")
            .hasValue(2);
    }
}