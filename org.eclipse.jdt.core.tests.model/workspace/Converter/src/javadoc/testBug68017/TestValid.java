package javadoc.testBug68017;
public class TestValid {
	/**@return integer*/
	public int foo1() {return 0; }
	/**
	 *	@return #
	 */
	public int foo2() {return 0; }
}
