package libs;

public interface MyFunction<T,R> {

    R apply(T t);

    default <V> MyFunction<V, R> compose(MyFunction<? super V, ? extends T> before) {
        return (V v) -> apply(before.apply(v));
    }
}