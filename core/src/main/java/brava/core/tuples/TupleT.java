package brava.core.tuples;

import brava.core.collections.ListBase;
import org.jetbrains.annotations.ApiStatus;

/**
 * <a href="https://en.wikipedia.org/wiki/Tuple">Tuple</a> types with psychotic type signatures.
 */
@ApiStatus.NonExtendable
@ApiStatus.Experimental
@SuppressWarnings("DuplicatedCode")
public interface TupleT<SELF extends Record & TupleT<SELF, T>, T> extends ListBase<T> {
    //region Factories
    Empty EMPTY = new Empty();

    static Empty of() {
        return EMPTY;
    }

    static <A extends T, T> Solo<A, T> of(A a) {
        return new Solo<>(a);
    }

    static <A extends T, B extends T, T> Duo<A, B, T> of(A a, B b) {
        return new Duo<>(a, b);
    }

    static <A extends T, B extends T, C extends T, T> Trio<A, B, C, T> of(A a, B b, C c) {
        return new Trio<>(a, b, c);
    }

    static <A extends T, B extends T, C extends T, D extends T, T> Quartet<A, B, C, D, T> of(A a, B b, C c, D d) {
        return new Quartet<>(a, b, c, d);
    }

    static <A extends T, B extends T, C extends T, D extends T, E extends T, T> Quintet<A, B, C, D, E, T> of(A a, B b, C c, D d, E e) {
        return new Quintet<>(a, b, c, d, e);
    }

    static <A extends T, B extends T, C extends T, D extends T, E extends T, F extends T, T> Sextet<A, B, C, D, E, F, T> of(A a, B b, C c, D d, E e, F f) {
        return new Sextet<>(a, b, c, d, e, f);
    }


    //endregion

    record Empty() implements TupleT<Empty, Void> {
        @Override
        public Void get(int index) {
            throw new IndexOutOfBoundsException(index);
        }

        @Override
        public int size() {
            return 0;
        }
    }

    record Solo<A extends T, T>(A a) implements TupleT<Solo<A, T>, T> {
        @Override
        public int size() {
            return 1;
        }

        @Override
        public T get(int index) {
            if (index != 0) {
                throw new IndexOutOfBoundsException(index);
            }

            return a;
        }
    }

    record Duo<A extends _T, B extends _T, _T>(A a, B b) implements TupleT<Duo<A, B, _T>, _T> {
        @Override
        public _T get(int index) {
            return switch (index) {
                case 0 -> a;
                case 1 -> b;
                default -> throw new IndexOutOfBoundsException(index);
            };
        }

        @Override
        public int size() {
            return 2;
        }
    }

    record Trio<A extends _T, B extends _T, C extends _T, _T>(A a, B b, C c) implements TupleT<Trio<A, B, C, _T>, _T> {
        @Override
        public _T get(int index) {
            return switch (index) {
                case 0 -> a;
                case 1 -> b;
                case 2 -> c;
                default -> throw new IndexOutOfBoundsException(index);
            };
        }

        @Override
        public int size() {
            return 3;
        }
    }

    record Quartet<A extends _T, B extends _T, C extends _T, D extends _T, _T>(A a, B b, C c,
                                                                               D d) implements TupleT<Quartet<A, B, C, D, _T>, _T> {
        @Override
        public _T get(int index) {
            return switch (index) {
                case 0 -> a;
                case 1 -> b;
                case 2 -> c;
                case 3 -> d;
                default -> throw new IndexOutOfBoundsException(index);
            };
        }

        @Override
        public int size() {
            return 4;
        }
    }

    record Quintet<A extends _T, B extends _T, C extends _T, D extends _T, E extends _T, _T>(
          A a, B b, C c, D d, E e) implements TupleT<Quintet<A, B, C, D, E, _T>, _T> {
        @Override
        public _T get(int index) {
            return switch (index) {
                case 0 -> a;
                case 1 -> b;
                case 2 -> c;
                case 3 -> d;
                case 4 -> e;
                default -> throw new IndexOutOfBoundsException(index);
            };
        }

        @Override
        public int size() {
            return 5;
        }
    }

    record Sextet<A extends _T, B extends _T, C extends _T, D extends _T, E extends _T, F extends _T, _T>(
          A a, B b, C c, D d, E e, F f) implements TupleT<Sextet<A, B, C, D, E, F, _T>, _T> {
        @Override
        public _T get(int index) {
            return switch (index) {
                case 0 -> a;
                case 1 -> b;
                case 2 -> c;
                case 3 -> d;
                case 4 -> e;
                case 5 -> f;
                default -> throw new IndexOutOfBoundsException(index);
            };
        }

        @Override
        public int size() {
            return 6;
        }
    }


    record Septet<A extends _T, B extends _T, C extends _T, D extends _T, E extends _T, F extends _T, G extends _T, _T>(
          A a, B b, C c, D d, E e, F f,
          G g) implements TupleT<Septet<A, B, C, D, E, F, G, _T>, _T> {
        @Override
        public _T get(int index) {
            return switch (index) {
                case 0 -> a;
                case 1 -> b;
                case 2 -> c;
                case 3 -> d;
                case 4 -> e;
                case 5 -> f;
                case 6 -> g;
                default -> throw new IndexOutOfBoundsException(index);
            };
        }

        @Override
        public int size() {
            return 7;
        }
    }

    record Octet<A extends _T, B extends _T, C extends _T, D extends _T, E extends _T, F extends _T, G extends _T, H extends _T, _T>(
          A a, B b, C c, D d, E e, F f, G g,
          H h) implements TupleT<Octet<A, B, C, D, E, F, G, H, _T>, _T> {
        @Override
        public _T get(int index) {
            return switch (index) {
                case 0 -> a;
                case 1 -> b;
                case 2 -> c;
                case 3 -> d;
                case 4 -> e;
                case 5 -> f;
                case 6 -> g;
                case 7 -> h;
                default -> throw new IndexOutOfBoundsException(index);
            };
        }

        @Override
        public int size() {
            return 8;
        }
    }

    record Ennead<A extends _T, B extends _T, C extends _T, D extends _T, E extends _T, F extends _T, G extends _T, H extends _T, I extends _T, _T>(
          A a, B b, C c, D d, E e, F f, G g, H h,
          I i) implements TupleT<Ennead<A, B, C, D, E, F, G, H, I, _T>, _T> {
        @Override
        public _T get(int index) {
            return switch (index) {
                case 0 -> a;
                case 1 -> b;
                case 2 -> c;
                case 3 -> d;
                case 4 -> e;
                case 5 -> f;
                case 6 -> g;
                case 7 -> h;
                case 8 -> i;
                default -> throw new IndexOutOfBoundsException(index);
            };
        }

        @Override
        public int size() {
            return 9;
        }
    }

    record Decade<A extends _T, B extends _T, C extends _T, D extends _T, E extends _T, F extends _T, G extends _T, H extends _T, I extends _T, J extends _T, _T>(
          A a, B b, C c, D d, E e, F f, G g, H h, I i,
          J j) implements TupleT<Decade<A, B, C, D, E, F, G, H, I, J, _T>, _T> {
        @Override
        public _T get(int index) {
            return switch (index) {
                case 0 -> a;
                case 1 -> b;
                case 2 -> c;
                case 3 -> d;
                case 4 -> e;
                case 5 -> f;
                case 6 -> g;
                case 7 -> h;
                case 8 -> i;
                case 9 -> j;
                default -> throw new IndexOutOfBoundsException(index);
            };
        }

        @Override
        public int size() {
            return 10;
        }
    }
}
