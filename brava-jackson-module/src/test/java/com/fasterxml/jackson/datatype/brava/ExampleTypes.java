package com.fasterxml.jackson.datatype.brava;

import brava.core.Either;
import brava.core.Which;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.datatype.brava.EitherModule.Preference;
import lombok.*;

import java.util.UUID;

/**
 * Types that do weird stuff.
 */
public class ExampleTypes {
    @AllArgsConstructor
    @NoArgsConstructor(force = true)
    @Getter
    @EqualsAndHashCode
    @ToString
    public static class HasEither<A, B> {
        private final Either<A, B> value;

        public record HasEither_Record<A, B>(Either<A, B> value) { }
    }

    @AllArgsConstructor
    @NoArgsConstructor(force = true)
    @Getter
    @EqualsAndHashCode
    @ToString
    public static class HasInt {
        private final Integer intValue;

        public static final TypedValue<HasInt> typedValue = TypedValue.of(new HasInt(100), new TypeReference<>() { });

        public record HasInt_Record(Integer intValue) {
            public static final TypedValue<HasInt_Record> typedValue = TypedValue.of(new HasInt_Record(101), new TypeReference<>() { });
        }
    }

    public record AsInt(@JsonValue int intValue) {
        public static final TypedValue<AsInt> typedValue = TypedValue.of(new AsInt(37), new TypeReference<AsInt>() { });
    }

    @AllArgsConstructor
    @NoArgsConstructor(force = true)
    @Getter
    @EqualsAndHashCode
    @ToString
    public static class HasValue<T> {
        private final T value;

        public static final TypedValue<HasValue<Integer>> typedValue_integer = TypedValue.of(new HasValue<>(99), new TypeReference<>() { });
        public static final TypedValue<HasValue<UUID>>    typedValue_uuid    = TypedValue.of(new HasValue<>(UUID.randomUUID()), new TypeReference<>() { });

        public record HasValue_Record<T>(T value) {
            public static final TypedValue<HasValue_Record<Integer>> typedValue_integer = TypedValue.of(new HasValue_Record<>(100), new TypeReference<>() { });

            public static final TypedValue<HasValue_Record<UUID>> typedValue_uuid = TypedValue.of(new HasValue_Record<>(UUID.randomUUID()), new TypeReference<>() { });
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor(force = true)
    @Getter
    @EqualsAndHashCode
    @ToString
    public static class HasUUID {
        private final UUID uuidValue;

        public static final TypedValue<HasUUID> typedValue = TypedValue.of(new HasUUID(UUID.randomUUID()), new TypeReference<>() { });

        public record HasUUID_Record(UUID uuidValue) {
            public static final TypedValue<HasUUID_Record> typedValue = TypedValue.of(new HasUUID_Record(UUID.randomUUID()), new TypeReference<>() { });
        }
    }

    @ToString
    public static class IgnoredConstructor {
        @JsonIgnore // Jackson is able to find private constructors, so we have to explicitly tell it to skip this
        private IgnoredConstructor() {
        }

        public static final TypedValue<IgnoredConstructor> typedValue = TypedValue.of(
            new IgnoredConstructor(),
            new TypeReference<>() { },
            "This type doesn't have an explicit deserializer, and Jackson can't create one dynamically because its only \"Creator\" is `@JsonIgnore`d"
        );
    }

    @SuppressWarnings("ClassCanBeRecord" /* Jackson has special handling for `record`s that allows it to use the multi-argument constructor */)
    @Getter(/* `get{x}()` methods are necessary because, by default, Jackson will throw an exception if you try to deserialize to a type that has no properties */)
    @ToString
    public static class OnlyMultiArgConstructor {
        private final int a;
        private final int b;
        private final int c;

        // Jackson can't use constructors by default unless they have 0 or 1 arguments
        @SuppressWarnings("unused")
        public OnlyMultiArgConstructor(int a, int b, int c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        public static final TypedValue<OnlyMultiArgConstructor> typedValue = TypedValue.of(
            new ExampleTypes.OnlyMultiArgConstructor(1, 2, 3),
            new TypeReference<>() { },
            "This type's only constructor has multiple arguments, which Jackson can't use as a \"Creator\""
        );
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor(force = true)
    @EqualsAndHashCode
    @ToString
    public static final class HasAnnotatedPreference<A, B> {
        @Preference(Which.A)
        private final Either<A, B> prefersA;

        @Preference(Which.B)
        private final Either<A, B> prefersB;

        public record HasAnnotatedPreference_Record<A, B>(
            @Preference(Which.A)
            Either<A, B> prefersA,

            @Preference(Which.B)
            Either<A, B> prefersB
        ) { }
    }

    //region Inheritance

    @AllArgsConstructor
    @NoArgsConstructor(force = true)
    @Getter
    @ToString
    @EqualsAndHashCode
    public static class Grandparent {
        private final String nickname;

        public static final TypedValue<Grandparent> typedValue = TypedValue.of(new Grandparent("granny"), new TypeReference<>() { });
    }

    @NoArgsConstructor(force = true)
    @Getter
    @ToString
    @EqualsAndHashCode(callSuper = true)
    public static class Parent extends Grandparent {
        public Parent(String nickname) {
            super(nickname);
        }

        public static final TypedValue<Parent> typedValue = TypedValue.of(new Parent("a parent"), new TypeReference<>() { });
    }

    @NoArgsConstructor(force = true)
    @Getter
    @ToString
    @EqualsAndHashCode(callSuper = true)
    public static class Child extends Parent {
        public Child(String nickname) {
            super(nickname);
        }

        public static final TypedValue<Child> typedValue = TypedValue.of(new Child("cool kid"), new TypeReference<>() { });
    }

    @NoArgsConstructor(force = true)
    @Getter
    @ToString
    @EqualsAndHashCode(callSuper = true)
    public static class Stepchild extends Parent {
        public Stepchild(String nickname) {
            super(nickname);
        }

        public static final TypedValue<Stepchild> typedValue = TypedValue.of(new Stepchild("redhead"), new TypeReference<>() { });
    }

    //endregion

    //region Deducible by properties

    public record FirstNameOnly(String firstName) {
        public static final TypedValue<FirstNameOnly> typedValue = TypedValue.of(new FirstNameOnly("Darktholomew"), new TypeReference<>() { });
    }

    public record FirstAndLastName(String firstName, String lastName) {
        public static final TypedValue<FirstAndLastName> typedValue = TypedValue.of(new FirstAndLastName("Jimbo", "Nice"), new TypeReference<>() { });
    }

    //endregion
}
