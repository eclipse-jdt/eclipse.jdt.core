/*
 * package g1.t.m.def is the package to define (def) generic (g1) types (t) (top level or
 * members) with multiple (m) type parameters
 * 
 * This type is a non-generic type which contains a generic member.
 */
package g3.t.def;

public class NGM {
	public class Generic<T1, T2, T3> {
		public T1 t1;
		public T2 t2;
		public T3 t3;
	}
}
