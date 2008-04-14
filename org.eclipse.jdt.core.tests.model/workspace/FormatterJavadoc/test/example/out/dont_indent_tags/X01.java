/**
 * An example for comment formatting. This example is meant to illustrate the various possibilities offered by <i>Eclipse</i> in order to format comments.
 */
package test.prefs.example;

/**
 * This is the comment for the example interface.
 */
interface Example {

	/**
	 * 
	 * These possibilities include:
	 * <ul>
	 * <li>Formatting of header comments.</li>
	 * <li>Formatting of Javadoc tags</li>
	 * </ul>
	 */
	int bar();

	/**
	 * The following is some sample code which illustrates source formatting
	 * within javadoc comments:
	 * 
	 * <pre>
	 * public class Example {
	 * 	final int a = 1;
	 * 	final boolean b = true;
	 * }
	 * </pre>
	 * 
	 * Descriptions of parameters and return values are best appended at end of
	 * the javadoc comment.
	 * 
	 * @param a
	 * The first parameter. For an optimum result, this should be an odd number
	 * between 0 and 100.
	 * @param b
	 * The second parameter.
	 * @return The result of the foo operation, usually within 0 and 1000.
	 */
	int foo(int a, int b);
}
