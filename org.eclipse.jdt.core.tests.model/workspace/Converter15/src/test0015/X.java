package test0015;

interface Convertible<T> {
	T convert();
}

public class X<A extends Convertible<Convertible<? extends Object>> & java.io.Serializable> {
}