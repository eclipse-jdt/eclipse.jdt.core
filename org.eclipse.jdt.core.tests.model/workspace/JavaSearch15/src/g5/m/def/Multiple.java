/*
 * package g5.m.def is the package to define (def) generic (g5) methods (m)
 * 
 * We have 5 typical methods:
 * 	- standard: not generic, parameters is generic type parameter
 * 	- generic: multiple method type parameters
 * 	- return generic type with multiple type arguments
 * 	- method parameter which is several parameterized types
 * 	- mix of all previous ones
 */
package g5.m.def;
public class Multiple<T1, T2, T3> {
	public void standard(T1 t1, T2 t2, T3 t3) {
	}
	public <U1, U2, U3> T1 generic(U1 u1, U2 u2, U3 u3) {
		return null;
	}
	public Multiple<T1, T2, T3> returnParamType() {
		return new Multiple<T1, T2, T3>();
	}
	public void paramTypesArgs(Single<T1> st1, Single<T2> st2, Single<T3> st3, Multiple<T1, T2, T3> gmt) {}
	public <U1, U2 extends Exception, U3 extends RuntimeException> Multiple<T1, T2, T3> complete(U1 u1, U2 u2, U3 u3, Multiple<T1, T2, T3> g) {
		if (u1 == null || u2==null || u3==null) {
			if (g == null) {
				return null;
			}
			return g;
		} else {
			return new Multiple<T1, T2, T3>().returnParamType();
		}
	}
}
