package javadoc.testBug69272;
public class TestClassValid {
	/**@see Object*/
	public void foo1() {}
	/**@see Object
	*/
	public void foo2() {}
	/**@see Object    */
	public void foo3() {}
	/**@see Object****/
	public void foo4() {}
	/**@see Object		****/
	public void foo5() {}
}
