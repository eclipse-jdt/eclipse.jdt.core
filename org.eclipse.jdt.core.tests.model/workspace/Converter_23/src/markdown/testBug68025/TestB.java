package javadoc.testBug68025;
public class TestB {
	public int field;
	public void foo() {}
	/**
	 *	@see #field#invalid
	 *	@see #foo#invalid
	 */
	public void foo1() {}
	/**@see Y#field# invalid*/
	public void foo2() {}
	/**@see Y#foo#	invalid*/
	public void foo3() {}
	/**@see Y#foo()#
	 *valid*/
	public void foo4() {}
}
