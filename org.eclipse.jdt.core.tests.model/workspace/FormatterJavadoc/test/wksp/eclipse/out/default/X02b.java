package test.wksp.eclipse;

interface X02b {
	/**
	 * @exception CoreException
	 *                if this method fails. Reasons include:
	 *                <ul>
	 *                <li>This resource does not exist.</li>
	 *                <li>An error happened while persisting this setting.</li>
	 *                <li>Resource changes are disallowed during certain types
	 *                of resource change event notification. See
	 *                {@link IResourceChangeEvent} for more details.</li>
	 *                </ul>
	 */
	public void setCharset(String newCharset, Object monitor) throws Exception;

}
