package test.tags.see;

public class X03 {

	class Y03 {
		class Z03 {
		}
	}

	/**
	 * @see X03 Simple type reference (there's no desciption before this
	 * section!)
	 * @see X03.Y03.Z03 Qualified field reference (extended description to have
	 * it on two lines after the formatting...)
	 * @see test.tags.see.X03.Y03 Fully qualified field reference
	 */
	public void test() {
	}
}
