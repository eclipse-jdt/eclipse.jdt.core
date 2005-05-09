package javadoc.testBug69302;
public class TestValid2 {
	/**
	 *	@see Unknown <a href="http://www.eclipse.org">Unknown</a>
	 */
	void foo1() {}
	/**
	 *	@see Unknown "Valid Unknown reference"
	 */
	void foo2() {}
}
