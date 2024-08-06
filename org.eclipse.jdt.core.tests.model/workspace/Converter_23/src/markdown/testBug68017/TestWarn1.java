package javadoc.testBug68017;
public class TestWarn1 { 
	/**
	 *	@return* */
	public int foo1() {return 0; }
	/**@return** **/
	public int foo2() {return 0; }
}
