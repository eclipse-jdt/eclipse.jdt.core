/*
 * package g1.t.s.ref is the package to define types (t) which contain
 * references (ref) to generic types (g1) which have only one single (s) type parameter
 * 
 * This type is a non-generic type which contains a generic member.
 */
package g3.t.def;
public class NGS {
	public class Generic<T> {
		public T t;
	}
}
