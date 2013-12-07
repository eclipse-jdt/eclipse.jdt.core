package test.prefs.example;

public class X16b {
	/**
	 * Asserts that an argument is legal. If the given boolean is not
	 * <code>true</code>, an <code>IllegalArgumentException</code> is thrown.
	 * The given message is included in that exception, to aid debugging.
	 *
	 * @param expression
	 *        the outcode of the check
	 * @param message
	 *        the message to include in the exception
	 * @return <code>true</code> if the check passes (does not return if the
	 *         check fails)
	 * @exception IllegalArgumentException
	 *            if the legality test failed
	 */
	boolean foo(int expression, String message) {
		return false;
	}

}
