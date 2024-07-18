package brava.core;

import com.google.common.reflect.TypeToken;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.NoSuchElementException;
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

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "yolo")
    void componentSet(String componentValue) {
        var builder   = RecordBuilder.ofType(TypeToken.of(Vinyl.class));
        var component = Vinyl.ARTIST;
        builder.set(component, componentValue);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(builder.hasComponentValue(component))
                .as("hasComponentValue(%s)", component)
                .isEqualTo(true);

            softly.assertThat(builder.get(component))
                .as("get(%s)", component)
                .isEqualTo(componentValue);
        });
    }
    
    void componentNotSet() {
        var builder = RecordBuilder.ofType(TypeToken.of(Vinyl.class));

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(builder.hasComponentValue(Vinyl.ARTIST))
                .as("hasComponentValue(%s)", Vinyl.ARTIST)
                .isFalse();

            softly.assertThatCode(() -> builder.get(Vinyl.ARTIST))
                .as("get(%s)", Vinyl.ARTIST)
                .isInstanceOf(NoSuchElementException.class);
        });
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
            .isInstanceOf(NoSuchElementException.class);
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
