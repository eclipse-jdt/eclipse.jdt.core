package test0362;
/* Regression test for bug 21916 */
public class Test {
	public void foo(int i) {
		for (int i=0, j=0, k=0; i<10 ; i++, j++, k++) {}
	}

}