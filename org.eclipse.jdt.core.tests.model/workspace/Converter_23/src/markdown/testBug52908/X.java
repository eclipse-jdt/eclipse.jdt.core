package javadoc.testBug52908;
public class X {
	/**
	 * Text element starting with a
	 * { caused troubles in its position
	 * if the bug is not fixed
	 * @see Object
	 */
	Object x1;
	/**
	 * Text element ending with a }
	 * caused troubles in its position
	 * if the bug is not fixed
	 * @see Object
	 */
	Object x2;
	/**
	 * Text element starting with a
	 * } caused troubles in its position
	 * if the bug is not fixed
	 * @see Object
	 */
	Object x3;
	/**
	 * Text element ending with a {
	 * caused troubles in its position
	 * if the bug is not fixed
	 * @see Object
	 */
	Object x4;
	/**
	 * Text element starting with
	 * { and ending with }
	 * caused troubles in its position
	 * if the bug is not fixed
	 * @see Object
	 */
	Object x6;
	/**
	 * Text element starting with
	 * } and ending with {
	 * caused troubles in its position
	 * if the bug is not fixed
	 * @see Object
	 */
	Object x7;
}
