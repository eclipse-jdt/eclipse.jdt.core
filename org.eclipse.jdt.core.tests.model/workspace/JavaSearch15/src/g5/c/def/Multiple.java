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
package g5.c.def;
public class Multiple<T1, T2, T3> {
	public Multiple(T1 t1, T2 t2, T3 t3) {}
	public <U1, U2, U3> Multiple(Multiple<T1, T2, T3> gst, U1 u1, U2 u2, U3 u3) {}
	public Multiple(Multiple<T1, T2, T3> gst) {}
	public <U1, U2, U3> Multiple(U1 u1, U2 u2, U3 u3, Multiple<T1, T2, T3> gst) {}
}
