package test0366;

/* Regression test for bug 23048 */

public class A {
	void theMethod() {
		for (int i = 0; i < 5; ++i);
	}
}