package j1;

/**
 * Class with valid references
 */
public class JavadocHiddenRef {
	/**
	 * References are hidden in strings
	 * @see "JavadocSearched"
	 * @see "JavadocSearched#JavadocSearched()"
	 * @see "j1.JavadocSearched#JavadocSearched(String)"
	 * @see "JavadocSearched#javadocSearchedVar"
	 * @see "JavadocSearched#javadocSearchedMethod()"
	 * @see "JavadocSearched#javadocSearchedMethod(String)"
	 */
	void hidden() {}
	
}
