/*
 * package g5.m.def is the package to define (def) generic (g5) methods (m)
 * 
 * We have 4 typical constructors:
 * 	- standard: not generic, parameters is generic type parameter
 * 	- generic: one single method type parameter
 * 	- method parameter which is a parameterized type
 * 	- mix of all previous ones
 */
package g5.c.def;
public class Single<T> {
	public Single(T t) {}
	public <U> Single(T t, U u) {}
	public Single(Single<T> gst) {}
	public <U> Single(U u, Single<T> gst) {}
}
