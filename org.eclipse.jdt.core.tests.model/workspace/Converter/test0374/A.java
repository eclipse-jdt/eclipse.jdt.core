package test0374;

/* Regression test for bug 23118 */

public class A {
	void foo() {
		for (int i = 0; i < 10; i++) {
			continue;
		}
	}
}