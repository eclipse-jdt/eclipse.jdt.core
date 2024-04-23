package package1;

/**
 * Javadoc for MyEnum
 */
public enum MyEnum {
	/**
	 * Javadoc for ONE
	 */
	ONE("one"),
	/**
	 * Javadoc for TWO
	 */
	TWO("two");

	/**
	 * Javadoc for publicField
	 */
	private String publicField;

	MyEnum(String arg) {
		publicField = arg;
	}

	/**
	 * Javadoc for getPublicField
	 *
	 * @return anything
	 */
	public String getPublicField() {
		return publicField;
	}
}
