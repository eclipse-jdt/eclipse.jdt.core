package javadoc.testBug73348; 
public class TestValid {
	/**
	 *	@return 
	 * int
	 */
	public int foo1() {return 0; }
	/**
	 *	@return 
	 * int
	 * @see Object
	 */
	public int foo2() {return 0; }
}
