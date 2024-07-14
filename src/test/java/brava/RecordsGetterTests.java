package brava;

import com.google.common.collect.MoreCollectors;
import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.stream.Stream;

@SuppressWarnings("ResultOfMethodCallIgnored")
class RecordsGetterTests {
    public record RecordGetterInfo<R extends @NotNull Record>(Class<R> recordType, Records.GetterMethod<R, ?> getter,
                                                              String expectedName) {

    }

    //region Example types used in test data
    public record Wrapper<T>(T value, Class<T> valueType) {

    }

    public static final class VinylArtistGetter implements Records.GetterMethod<@NotNull Vinyl, String> {
        @Override
        public String apply(Vinyl vinyl) {
            return vinyl.artist();
        }
    }

    public static final class WrapperValueGetter<T> implements Records.GetterMethod<@NotNull Wrapper<T>, T> {
        @Override
        public T apply(Wrapper<T> wrapper) {
            return wrapper.value;
        }
    }
    //endregion

    //region Test data

    public static Stream<Records.GetterMethod<?, ?>> lambdaExpressions() {
        return Stream.of(
              (Vinyl it) -> it.title(),
              (Vinyl it) -> it.title(),
              (Wrapper<Vinyl> it) -> it.value(),
              (Wrapper<?> it) -> it.valueType()
        );
    }

    @SuppressWarnings({"Convert2Lambda", "Anonymous2MethodRef"})
    public static Stream<Records.GetterMethod<?, ?>> anonymousClasses() {
        return Stream.of(
              new Records.GetterMethod<@NotNull Vinyl, String>() {
                  @Override
                  public String apply(Vinyl vinyl) {
                      return vinyl.artist();
                  }
              },
              new Records.GetterMethod<@NotNull Wrapper<Vinyl>, Vinyl>() {
                  @Override
                  public Vinyl apply(Wrapper<Vinyl> vinylWrapper) {
                      return vinylWrapper.value;
                  }
              }
        );
    }
    
    public static Stream<Records.GetterMethod<?, ?>> lambdaImplementations() {
        return Stream.of(
              new VinylArtistGetter(),
              new WrapperValueGetter<>()
        );
    }

    public static Stream<RecordGetterInfo<?>> methodReferenceGetters() {
        return Stream.of(
              new RecordGetterInfo<>(Vinyl.class, Vinyl::artist, "artist"),
              new RecordGetterInfo<>(Vinyl.class, Vinyl::title, "title"),
              new RecordGetterInfo<>(Wrapper.class, Wrapper::value, "value"),
              new RecordGetterInfo<>(Wrapper.class, Wrapper::valueType, "valueType")
        );
    }

    //endregion

    @Test
    void areSameComponentTest() {
        var comp_1 = Vinyl.class.getRecordComponents()[0];
        var comp_2 = Vinyl.class.getRecordComponents()[0];

        Assertions.assertThat(comp_1)
              .isNotSameAs(comp_2);

        Assertions.assertThat(Records.areSameComponent(comp_1, comp_2))
              .isTrue();
    }

    record AnnoyingNames(String yolo, String YOLO) {
        public static final RecordComponent comp_yolo = AnnoyingNames.class.getRecordComponents()[0];
        public static final RecordComponent comp_YOLO = AnnoyingNames.class.getRecordComponents()[1];
    }

    @Test
    void getComponentByNameTest() {
        var actual = Records.getComponentByName(AnnoyingNames.class, "yolo");
        Assertions.assertThat(Records.areSameComponent(actual, AnnoyingNames.comp_yolo))
              .isTrue();

        Assertions.assertThat(Records.areSameComponent(actual, AnnoyingNames.comp_YOLO))
              .isFalse();
    }

    @MethodSource({
          "lambdaExpressions",
          "anonymousClasses",
          "lambdaImplementations"
    })
    @ParameterizedTest
    void givenLambdaExpression_whenCompCreated_thenExceptionIsThrown(Records.GetterMethod<?, ?> getter) {
        Assertions.assertThatCode(() -> Records.getComponentByGetter(getter))
              .isNotNull();
    }

    @MethodSource("methodReferenceGetters")
    @ParameterizedTest
    void givenMethodReference_whenCompCreated_thenCompIsCreated(RecordGetterInfo<?> recordGetterInfo) {
        var comp = Records.getComponentByGetter(recordGetterInfo.getter);

        var expectedComponent = Arrays.stream(recordGetterInfo.recordType().getRecordComponents())
              .filter(it -> it.getName().equals(recordGetterInfo.expectedName))
              .collect(MoreCollectors.onlyElement());

        Assertions.assertThat(comp.isSameComponentAs(expectedComponent))
              .isTrue();
    }
}