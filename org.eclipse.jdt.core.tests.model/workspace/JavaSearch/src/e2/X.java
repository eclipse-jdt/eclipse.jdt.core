package e2;
/* Test case for bug 31748 [search] search for reference is broken 2.1 M5 */
public class X {
	protected void foo() {
	}
	void bar() {
		foo();
	}
}