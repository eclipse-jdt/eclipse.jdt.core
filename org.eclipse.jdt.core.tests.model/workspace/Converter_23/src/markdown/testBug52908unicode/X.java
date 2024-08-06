package javadoc.testBug52908unicode;
public class X {
	/**
	 * Text element starting with a
	 * \u007b caused troubles in its position
	 * if the bug is not fixed
	 * @see Object
	 */
	Object x1;
	/**
	 * Text element ending with a \u007d
	 * caused troubles in its position
	 * if the bug is not fixed
	 * @see Object
	 */
	Object x2;
	/**
	 * Text element starting with a
	 * \u007d caused troubles in its position
	 * if the bug is not fixed
	 * @see Object
	 */
	Object x3;
	/**
	 * Text element ending with a \u007b
	 * caused troubles in its position
	 * if the bug is not fixed
	 * @see Object
	 */
	Object x4;
	/**
	 * Text element starting with
	 * \u007b and ending with \u007d
	 * caused troubles in its position
	 * if the bug is not fixed
	 * @see Object
	 */
	Object x6;
	/**
	 * Text element starting with
	 * \u007d and ending with \u007b
	 * caused troubles in its position
	 * if the bug is not fixed
	 * @see Object
	 */
	Object x7;
}
