import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class A<
		T extends A<T, S>,
		S extends List<?>> {

	T t;
	S s;
	A<T, S> a;

	Object field = a.<T, S>getMap(t, s);

	public A() {
		<T, S>this(null, null);
	}

	public <
			M,
			N> A(M param1, N param2) {

	}

	public <
			M,
			N> Map<M, N> getMap(M key, N value) {
		return null;
	}

}

class B extends A<B, List<String>> {

	public <
			M,
			N> B(M param1, N param2) {
		<M, N>super(param1, param2);
	}

	public <
			M,
			N> Map<M, N> getMap(M key, N value) {
		B b = new <M, N>B(key, value);
		AMethod method = b::<String, String>getMap;
		AMethod method2 = super::<String, String>getMap;
		AMethod method3 = AMap<M>::<String, String>new;
		AMethod method4 = AMap<M>::<String, String>createAMap;
		return super.<M, N>getMap(key, value);
	}
}

interface AMethod {
	Map<String, String> getMap(String key, String value);
}

class AMap<
		T> extends HashMap<String, String> {
	public <
			M,
			N> AMap(String param1, String param2) {

	}

	public static <
			M,
			N> AMap<M> createAMap(String param1, String param2) {
		return new AMap<>(param1, param2);
	}
}