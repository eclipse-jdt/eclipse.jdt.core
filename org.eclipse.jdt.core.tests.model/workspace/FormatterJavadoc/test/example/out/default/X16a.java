package test.prefs.example;

public class X16a {
	/**
	 * Returns true if the check in for this thread failed, in which case the
	 * check out and other end of operation code should not run.
	 * <p>
	 * The failure flag is reset immediately after calling this method.
	 * Subsequent calls to this method will indicate no failure (unless a new
	 * failure has occurred).
	 * 
	 * @return <code>true</code> if the checkIn failed, and <code>false</code>
	 *         otherwise.
	 */
	boolean foo() {
		return false;
	}
}
