package javadoc.testBug69272;
public class TestFieldValid {
	int field;
	/**@see #field*/
	public void foo1() {}
	/**@see #field
	*/
	public void foo2() {}
	/**@see #field    */
	public void foo3() {}
	/**@see #field****/
	public void foo4() {}
	/**@see #field		********/
	public void foo5() {}
}
