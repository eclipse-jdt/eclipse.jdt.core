package test.tags.see;

public class X02 {
	int foo;

	/**
	 * Test references to fields
	 * 
	 * @see #foo Implicit field reference
	 * @see Y02#bar Qualified field reference
	 * @see Y02.Z02#local Fully qualified field reference (extended description
	 *      to have it on two lines after the formatting...)
	 */
	public void test() {
	}
}

class Y02 {
	int bar;

	class Z02 {
		int local;
	}
}
