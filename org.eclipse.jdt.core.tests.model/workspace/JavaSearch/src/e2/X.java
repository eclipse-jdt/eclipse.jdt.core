package e2;
/* Test case for bug 38568 Search for method declarations fooled by array types */
public class X {
	public void foo() {
	}
	public void foo(String arg1, String arg2) {
	}
	public void foo(String arg1, String[] arg2) {
	}
	public void foo(String arg1, Object arg2) {
	}
}
