package p;
public class A {
	public static boolean DEBUG = false;
	public X x;
	public A(X x) {
		this.x = x;
	}
	void foo() {
		A.DEBUG = true;
	}
	public void foo(int i, String s, X x) {
		x.new Inner().foo();
	}
	public static void main(String[] args) {
	}
}
