package p8;
/* Regression test case for bug 44506 Type hierarchy is missing anonymous type*/
public class X {
	X(String s) {
	}
	protected void foo() {
		new X("") {
		};
	}
}