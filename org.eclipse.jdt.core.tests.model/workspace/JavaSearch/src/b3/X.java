package b3;
/* Test case for bug 9041 search: cannot create a sub-cu scope */
public class X {
	Object field = new X();
	Object foo() {
		return new X();
	}
	class Y {
		Object field2 = new X();
		Object foo2() {
			return new X();
		}
	}
}