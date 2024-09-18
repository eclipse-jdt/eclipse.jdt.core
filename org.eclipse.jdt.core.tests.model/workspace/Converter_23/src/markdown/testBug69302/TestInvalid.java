package javadoc.testBug69302;
public class TestInvalid {
	/**@see Unknown Unknown reference <a href="http://www.eclipse.org">text</a>*/
	void foo1() {}
	/**@see Unknown Unknown reference "Valid string reference"*/
	void foo2() {}
}
