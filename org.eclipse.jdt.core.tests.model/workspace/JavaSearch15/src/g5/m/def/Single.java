/*
 * package g5.m.def is the package to define (def) generic (g5) methods (m)
 * 
 * We have 5 typical methods:
 * 	- standard: not generic, parameters is generic type parameter
 * 	- generic: one single method type parameter
 * 	- return generic type
 * 	- method parameter which is a parameterized type
 * 	- mix of all previous ones
 */
package g5.m.def;
public class Single<T> {
	public void standard(T t) {
	}
	public <U> T generic(U u) {
		return null;
	}
	public Single<T> returnParamType() {
		return new Single<T>();
	}
	public void paramTypesArgs(Single<T> gst) {}
	public <U> Single<T> complete(U u, Single<T> g) {
		if (u == null) {
			if (g == null) {
				return null;
			}
			return g;
		} else {
			return new Single<T>().returnParamType();
		}
	}
}
