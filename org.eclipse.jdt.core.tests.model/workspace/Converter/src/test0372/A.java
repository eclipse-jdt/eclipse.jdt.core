package test0372;

/* Regression test for bug 23048 */

public class A {
	void foo(int i) {
		if (i == 6) {} else ;
	}
}