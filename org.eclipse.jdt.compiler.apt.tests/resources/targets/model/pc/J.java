package targets.model.pc;

public class J {
	int fieldInt; // same-named fields in F, G, and H do NOT hide this; unrelated enclosing classes
	
	int fieldInt() { return 0; } // does not hide, and is not hidden by, any of the fields named fieldInt in F, G, H, or this.
	
	public class FChild {} // does not hide, and is not hidden by, same class in F or H
	
	public class F {} // does not hide, and is not hidden by, outer class F
	
	public static void staticMethod() {} // does not hide, and is not hidden by, F.staticMethod()
}
