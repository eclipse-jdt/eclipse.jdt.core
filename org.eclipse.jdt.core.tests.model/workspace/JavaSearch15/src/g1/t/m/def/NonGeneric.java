/*
 * package g1.t.m.def is the package to define (def) generic (g1) types (t) (top level or
 * members) with multiple (m) type parameters
 * 
 * This type is a non-generic type which contains a generic member.
 */
package g1.t.m.def;

public class NonGeneric {
	public class GenericMember<T1, T2 extends Exception, T3 extends RuntimeException> {
		public T1 t1;
		public T2 t2;
		public T3 t3;
	}
}
