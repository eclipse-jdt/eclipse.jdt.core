package javadoc.testBug69272;
public class TestClassInvalid {
	/**@see Object* */
	public void foo1() {}
	/**@see Object*** ***/
	public void foo2() {}
	/**@see Object***
	 */
	public void foo3() {}
}
