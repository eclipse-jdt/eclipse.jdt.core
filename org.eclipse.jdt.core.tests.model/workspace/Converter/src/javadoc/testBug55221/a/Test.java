package javadoc.testBug55221.a;
public class Test {
	public int bar() {
		int x=0;
		if (true) {
			x=1;
		} else {
			x=2;
		}
		return x;
	}
	/**
	 * This comment should not be attached to previous method body!
	 * @return int
	 */

	public int foo() { return 1; }
}