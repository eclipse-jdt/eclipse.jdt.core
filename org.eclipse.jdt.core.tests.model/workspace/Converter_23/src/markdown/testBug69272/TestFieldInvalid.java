package javadoc.testBug69272;
public class TestFieldInvalid {
	int field;
	/**@see #field* */
	public void foo1() {}
	/**@see #field*** ***/
	public void foo2() {}
	/**@see #field***
	 */
	public void foo3() {}
}
