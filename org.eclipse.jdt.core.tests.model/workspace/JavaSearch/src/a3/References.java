package a3;
import a3.b.B;
public class References {
	public void foo() {
		X x1 = new Z(); // single type references
		a3.b.A a = null; // qualified type reference
		a3.b.A.B.C inner = null; // qualified type references with inner type
		Object o = a3.Y.field; // binary reference + qualified name reference
		X x2 = (B)x1; // single name reference
	}
}