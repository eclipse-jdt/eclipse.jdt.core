package s4;
public class X {
	int x;
	int foo() {
		return this.x;
	}
	/**
	 * @see X#x
	 * @see X#foo()
	 * @see X
	 */
	void bar() {
	}
	
	void fred() {
		new X().foo();
	}
}