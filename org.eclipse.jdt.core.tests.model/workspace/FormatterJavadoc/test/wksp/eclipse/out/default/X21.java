package test.wksp.eclipse;

public class X21 {

	/**
	 * Returns a search pattern based on a given string pattern. The string
	 * patterns support '*' wild-cards. The remaining parameters are used to
	 * narrow down the type of expected results.
	 * 
	 * <br>
	 * Examples:
	 * <ul>
	 * <li>search for case insensitive references to <code>Object</code>:
	 * <code>createSearchPattern("Object", TYPE, REFERENCES, false);</code></li>
	 * <li>search for case sensitive references to exact <code>Object()</code>
	 * constructor:
	 * <code>createSearchPattern("java.lang.Object()", CONSTRUCTOR, REFERENCES, true);</code>
	 * </li>
	 * <li>search for implementers of <code>java.lang.Runnable</code>:
	 * <code>createSearchPattern("java.lang.Runnable", TYPE, IMPLEMENTORS, true);</code>
	 * </li>
	 * </ul>
	 * 
	 * @param stringPattern
	 *            the given pattern
	 * @param searchFor
	 *            determines the nature of the searched elements
	 *            <ul>
	 *            <li><code>IJavaSearchConstants.CLASS</code>: only look for
	 *            classes</li>
	 *            <li><code>IJavaSearchConstants.INTERFACE</code>: only look for
	 *            interfaces</li>
	 *            <li><code>IJavaSearchConstants.TYPE</code>: look for both
	 *            classes and interfaces</li>
	 *            <li><code>IJavaSearchConstants.FIELD</code>: look for fields</li>
	 *            <li><code>IJavaSearchConstants.METHOD</code>: look for methods
	 *            </li>
	 *            <li><code>IJavaSearchConstants.CONSTRUCTOR</code>: look for
	 *            constructors</li>
	 *            <li><code>IJavaSearchConstants.PACKAGE</code>: look for
	 *            packages</li>
	 *            </ul>
	 * @param limitTo
	 *            determines the nature of the expected matches
	 *            <ul>
	 *            <li><code>IJavaSearchConstants.DECLARATIONS</code>: will
	 *            search declarations matching with the corresponding element.
	 *            In case the element is a method, declarations of matching
	 *            methods in subtypes will also be found, allowing to find
	 *            declarations of abstract methods, etc.</li>
	 * 
	 *            <li><code>IJavaSearchConstants.REFERENCES</code>: will search
	 *            references to the given element.</li>
	 * 
	 *            <li><code>IJavaSearchConstants.ALL_OCCURRENCES</code>: will
	 *            search for either declarations or references as specified
	 *            above.</li>
	 * 
	 *            <li><code>IJavaSearchConstants.IMPLEMENTORS</code>: for
	 *            interface, will find all types which implements a given
	 *            interface.</li>
	 *            </ul>
	 * 
	 * @param isCaseSensitive
	 *            indicates whether the search is case sensitive or not.
	 * @return a search pattern on the given string pattern, or
	 *         <code>null</code> if the string pattern is ill-formed.
	 * @deprecated Use
	 *             {@link SearchPattern#createPattern(String, int, int, int)}
	 *             instead.
	 */
	void createSearchPattern() {
	}
}
