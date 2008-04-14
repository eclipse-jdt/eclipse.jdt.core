package test.tags.see;

public class X01 {
	/**
	 * First constructor with only one string parameter
	 * @see #X01(String, String)
	 */
	public X01(String first) {
	}

	/**
	 * Second constructor with two string parameters
	 * @see X01#X01(String)
	 */
	public X01(String first, String second) {
		this(first);
	}
}
