package javadoc.testBug69302;
public class TestValid2 {
	/**
	 *	@see Unknown <a href="http://www.eclipse.org">Eclipse</a>
	 */
	void foo1() {}
	/**
	 *	@see Unknown "Valid string reference"
	 */
	void foo2() {}
}
