package r;
/* Test case for PR 1GKAQJS: ITPJCORE:WIN2000 - search: incorrect results for nested types */
public class A {
	class X {
		X(X X) {
			new X(null);
		}
	}
	A() {
	}
	A(A A) {
	}
	A m() {
		new X(null);
		return (A) new A();
	}
};
class B {
	A.X ax = new A().new X(null);
}