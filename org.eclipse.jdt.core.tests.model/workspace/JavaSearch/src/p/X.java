package p;
public class X implements I {
	public static boolean DEBUG = false;
	public I field;
	public class Inner {
		public String foo() {
			return "foo";
		}
	}
	public X() {
		X.DEBUG = true;
	}
	public X(int i) {
	}
	public static void bar() {
	}
	public void foo(int i, String s, X x) {
		Inner inner = new Inner();
	}
}
