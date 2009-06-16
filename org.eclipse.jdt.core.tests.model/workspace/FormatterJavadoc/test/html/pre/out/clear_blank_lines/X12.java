package test.html.pre;

public class X12 {

	/**
	 * "Comma" two existing nfms x,y -> x,y
	 * <p>
	 * Re-uses x so that x.stop is first transformed to y.start and then x.stop
	 * is reset to y.stop. This is as efficient as possible.
	 * 
	 * <pre>
	 * x start      former x stop   x stop
	 *     |               |           |
	 * +---------+  +----------+  +--------+
	 * | x start |  | y start  |->| y stop |
	 * +---------+  +----------+  +--------+
	 * </pre>
	 * 
	 * Frees y, returns x modified.
	 */
	void foo() {
	}
}
