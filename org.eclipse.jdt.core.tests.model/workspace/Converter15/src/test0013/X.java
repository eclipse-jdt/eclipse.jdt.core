package test0013;

interface Convertible<T> {
	T convert();
}

public class X<A extends Convertible<B>, B extends Convertible<A>> {
}