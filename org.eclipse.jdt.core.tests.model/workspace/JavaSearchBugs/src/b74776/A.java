package b74776;
public class A {
	/**
	 * @deprecated Use {@link #foo(IRegion)} instead
	 * @param r
	 */
	void foo(Region r) {
		foo((IRegion)r);
	}
	void foo(IRegion r) {
	}
}
