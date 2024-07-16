package brava.core.tuples;

public interface AppendableTuple<SELF extends Tuple<SELF> & AppendableTuple<SELF, ONE_UP>, ONE_UP extends Tuple<ONE_UP>> extends Tuple<SELF> {
    <T> ONE_UP append(T value);
}
