package test.prefs.example;

public interface X17c {
	/**
	 * <p>
	 * The resulting resolved classpath is accurate for the given point in time.
	 * If the project's raw classpath is later modified, or if classpath
	 * variables are changed, the resolved classpath can become out of date.
	 * Because of this, hanging on resolved classpath is not recommended.
	 * </p>
	 * 
	 * @exception Exception
	 *                in one of the corresponding situation:
	 *                <ul>
	 *                <li>this element does not exist</li>
	 *                <li>an exception occurs while accessing its corresponding
	 *                resource</li>
	 *                <li>a classpath variable or classpath container was not
	 *                resolvable and <code>ignoreUnresolvedEntry</code> is
	 *                <code>false</code>.</li>
	 *                </ul>
	 * @see String
	 */
	String getResolvedClasspath(boolean ignoreUnresolvedEntry) throws Exception;
}
