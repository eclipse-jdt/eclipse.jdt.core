package javadoc.testBug68017;
public class TestInvalid {
	/**@return*/
	public int foo1() {return 0; }
	/**@return        */
	public int foo2() {return 0; }
	/**@return****/
	public int foo3() {return 0; }
	/**
	 *	@return
	 */
	public int foo4() {return 0; }
}
