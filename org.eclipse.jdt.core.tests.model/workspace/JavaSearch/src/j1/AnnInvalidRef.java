package j1;

/**
 * Class with invalid references
 */
public class AnnInvalidRef {
	/**
	 * @see #annSearchedVar
	 * @see Annsearched#annSearchedVar
	 * @see AnnSearched#annsearchedvar
	 * @see Annsearched#annSearchedMethod()
	 * @see AnnSearched#annsearchedmethod()
	 * @see AnnSearched#annSearchedMethod(int)
	 */
	void invalid() {}
	
}
