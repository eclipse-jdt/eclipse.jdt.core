package s5;
/* Test case for bug 61017 Refactoring - test case that results in uncompilable source*/
public class A {
	public B b; // <- rename this
	public void method() {
		B.b.a.b = null;
	}
}
class B {
	public static B b;
	public A a;
}
