/*
 * package g4.m.def is the package to define (def) generic (g4) methods (t)
 * 
 * Generic type which also contains both generic and non-generic member types.
 */
package g5.m.def;
public class GS<T> {
	public void standard(T t) {
	}
	public <U> T generic(U u) {
		return null;
	}
	public GS<T> returnParamType() {
		return new GS<T>();
	}
	public void paramTypesArgs(GS<T> gst) {}
	public <U extends Exception> GS<T> full(U u, GS<T> g) {
		if (u == null) {
			if (g == null) {
				return null;
			}
			return g;
		} else {
			return new GS<T>().returnParamType();
		}
	}
}
