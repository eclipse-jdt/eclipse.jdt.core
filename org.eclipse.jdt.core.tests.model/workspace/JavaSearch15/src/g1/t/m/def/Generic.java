/*
 * package g1.t.m.def is the package to define (def) generic (g1) types (t) (top level or
 * members) with multiple (m) type parameters
 *
 * Generic type which also contains both generic and non-generic member types.
 */
package g1.t.m.def;
public class Generic<T1, T2 extends Exception, T3 extends RuntimeException> {
	public T1 t1;
	public T2 t2;
	public T3 t3;
	public class Member {
		public Object m;
	}
	public class MemberGeneric<U1, U2 extends Exception, U3 extends Throwable> {
		public U1 u1;
		public U2 u2;
		public U3 u3;
	}
}
