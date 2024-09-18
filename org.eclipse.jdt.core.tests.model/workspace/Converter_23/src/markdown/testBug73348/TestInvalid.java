package javadoc.testBug73348;

public class TestInvalid {
	/**
	 *	@return
	 * @see Object
	 */
	public int foo1(int x) {return 0; }
	/**
	 *	@return    
	 * @see Object
	 */
	public int foo2(int x) {return 0; }
}
