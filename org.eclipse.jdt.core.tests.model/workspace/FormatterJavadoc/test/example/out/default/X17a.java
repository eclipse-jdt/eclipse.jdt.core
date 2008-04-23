package test.prefs.example;

public class X17a {
	/**
	 * Returns <code>false</code> since this <code>IStructureCreator</code>
	 * cannot rewrite the diff tree in order to fold certain combinations of
	 * additons and deletions.
	 * <p>
	 * Note: this method is for internal use only. Clients should not call this
	 * method.
	 * 
	 * @return <code>false</code>
	 */
	public boolean foo() {
		return false;
	}
}
