package bug73336b;
public class X73336b<T, U> {
	<V> void foo(V v) {}
	class XX<T> {
		void foo() {}
	}
}
