package javadoc.testBug69302;
public class TestValid1 {
	/**
	 *	@see Object <a href="http://www.eclipse.org">Eclipse</a>
	 */
	void foo1() {}
	/**
	 *	@see Object "Valid string reference"
	 */
	void foo2() {}

}
