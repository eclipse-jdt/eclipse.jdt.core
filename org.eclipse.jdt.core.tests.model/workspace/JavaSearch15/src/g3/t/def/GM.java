/*
 * package g1.t.m.def is the package to define (def) generic (g1) types (t) (top level or
 * members) with multiple (m) type parameters
 *
 * Generic type which also contains both generic and non-generic member types.
 */
package g3.t.def;
public class GM<T1, T2, T3> {
	public T1 t1;
	public T2 t2;
	public T3 t3;
	public class Member {
		public Object m;
	}
	public class Generic<U1, U2, U3> {
		public U1 u1;
		public U2 u2;
		public U3 u3;
	}
}
