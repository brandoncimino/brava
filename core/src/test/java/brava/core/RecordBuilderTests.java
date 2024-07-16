package brava.core;

import com.google.common.reflect.TypeToken;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class RecordBuilderTests {

    public static Stream<Vinyl> provideRecords() {
        return Stream.of(
              new Vinyl("yolo", "swag", 1999),
              new Vinyl(null, "swag", 1999),
              new Vinyl(null, null, 1999)
        );
    }

    @MethodSource("provideRecords")
    @ParameterizedTest
    void givenCompleteBuilder_whenBuild_thenRecordIsCreated(Vinyl vinyl) {
        var builder = RecordBuilder.ofType(TypeToken.of(Vinyl.class));
        var recordMap = vinyl.toMap();
        recordMap.forEach(builder::set);
        var built = builder.build();

        Assertions.assertThat(built)
              .isEqualTo(vinyl);
    }

    @MethodSource("provideRecords")
    @ParameterizedTest
    void givenRecord_whenBuilderFromRecord_thenBuilderIsPopulated(Vinyl vinyl) {
        var builder = RecordBuilder.from(vinyl);
        Assertions.assertThat(vinyl.toMap())
              .allSatisfy((k, v) -> Assertions.assertThat(builder.get(k)).isEqualTo(v));
    }

    @Test
    void givenNullKey_whenSetInBuilder_thenExceptionIsThrown() {
        var builder = RecordBuilder.ofType(Vinyl.class);
        //noinspection DataFlowIssue
        Assertions.assertThatThrownBy(() -> builder.set(null, "yolo"))
              .isInstanceOf(NullPointerException.class);
    }

    @Test
    void givenIncompleteBuilder_whenBuild_thenExceptionIsThrown() {
        var builder = RecordBuilder.ofType(TypeToken.of(Vinyl.class));
        Assertions.assertThatThrownBy(builder::build)
              .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void givenIncorrectComponentType_whenBuild_thenExceptionIsThrown() {
        var builder = RecordBuilder.ofType(TypeToken.of(Vinyl.class));
        var vinyl = new Vinyl("yolo", "swag", 1999);
        vinyl.toMap().forEach(builder::set);
        builder.set(Vinyl.ARTIST, 99);
        Assertions.assertThatThrownBy(builder::build)
              .isInstanceOf(IllegalArgumentException.class);
    }
}
