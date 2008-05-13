package test.prefs.example;

public interface X17b {
	/**
	 * <p>
	 * If the classpath entry denotes a container, it will be resolved and
	 * return the roots corresponding to the set of container entries (empty if
	 * not resolvable).
	 * 
	 * @param entry
	 *        the given entry
	 * @return the existing package fragment roots identified by the given entry
	 */
	Object findPackageFragmentRoots(String entry);
}
