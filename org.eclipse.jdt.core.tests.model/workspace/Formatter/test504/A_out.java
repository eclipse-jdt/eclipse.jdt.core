interface Convertible<T> {
	T convert();
}

public class ReprChange<A extends Convertible<B>, B extends Convertible<A>> {
}