package javadoc.testBug65174;
public class Test {
	/**
	 * Comment with no error: {@link
	 * Object valid} because it's not on first line
	 */
	void foo_ok() {}
	/** Comment previously with error: {@link
	 * Object valid} because tag is on comment very first line
	 */
	void foo_ko() {}
	/**
	 * Comment with no error: {@link		
	 * Object valid} because it's not on first line
	 */
	void fooA_ok() {}
	/** Comment previously with error: {@link		
	 * Object valid} because tag is on comment very first line
	 */
	void fooA_ko() {}
	/**
	 * Comment with no error: {@link java.lang.
	 * Object valid} because it's not on first line
	 */
	void fooB_ok() {}
	/** Comment previously with error: {@link java.lang.
	 * Object valid} because tag is on comment very first line
	 */
	void fooB_ko() {}
	/**
	 * Comment with no error: {@link Object
	 * valid} because it's not on first line
	 */
	void fooC_ok() {}
	/** Comment previously with error: {@link Object
	 * valid} because tag is on comment very first line
	 */
	void fooC_ko() {}
}
