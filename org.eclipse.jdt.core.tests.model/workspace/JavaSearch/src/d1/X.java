package d1;
/* Test case for bug 24934 Move top level doesn't optimize the imports[refactoring] */
import d2.Y;
import d2.Z;
public class X {
	public class Inner {
		Y y;
	}
	Z z;
}
