package javadoc.testBug69275;
public class TestA {
	/**@see <a href="http://www.eclipse.org">text</a>* */
	void foo1() {}
	/**@see <a href="http://www.eclipse.org">text</a>     *** **/
	void foo2() {}
	/**@see <a href="http://www.eclipse.org">text</a>***
	 */
	void foo3() {}
}
