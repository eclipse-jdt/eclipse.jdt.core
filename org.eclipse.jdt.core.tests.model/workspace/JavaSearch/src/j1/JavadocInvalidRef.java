package j1;

/**
 * Class with invalid references
 */
public class JavadocInvalidRef {
	/**
	 * @see #javadocSearchedVar
	 * @see Javadocsearched#JavadocSearched()
	 * @see j1.JavadocSearched#Javadocsearched(String)
	 * @see JavadocSearched#JavadocSearched(int)
	 * @see Javadocsearched#javadocSearchedVar
	 * @see JavadocSearched#javadocsearchedvar
	 * @see Javadocsearched#javadocSearchedMethod()
	 * @see JavadocSearched#javadocsearchedmethod()
	 * @see JavadocSearched#javadocSearchedMethod(int)
	 */
	void invalid() {}
	
}
