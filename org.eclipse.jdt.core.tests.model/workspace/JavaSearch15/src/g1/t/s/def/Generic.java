/*
 * package g1.t.s.ref is the package to define types (t) which contain
 * references (ref) to generic types (g1) which have only one single (s) type parameter
 * 
 * Generic type which also contains both generic and non-generic member types.
 */
package g1.t.s.def;
public class Generic<T> {
	public T t;
	public class Member {
		public Object m;
	}
	public class MemberGeneric<V> {
		public V v;
	}
}
