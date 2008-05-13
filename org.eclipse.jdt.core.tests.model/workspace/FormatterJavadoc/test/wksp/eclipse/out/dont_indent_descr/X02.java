package test.wksp.eclipse;

interface X02 {
	/**
	 * Sets the charset for this file. Passing a value of <code>null</code> will
	 * remove the charset setting for this resource.
	 * <p>
	 * This method changes resources; these changes will be reported in a
	 * subsequent resource change event, including an indication that this
	 * file's encoding has changed.
	 * </p>
	 * <p>
	 * This method is long-running; progress and cancellation are provided by
	 * the given progress monitor.
	 * </p>
	 * 
	 * @param newCharset
	 *        a charset name, or <code>null</code>
	 * @param monitor
	 *        a progress monitor, or <code>null</code> if progress reporting is
	 *        not desired
	 * @exception OperationCanceledException
	 *            if the operation is canceled. Cancelation can occur even if no
	 *            progress monitor is provided.
	 * @exception CoreException
	 *            if this method fails. Reasons include:
	 *            <ul>
	 *            <li>This resource does not exist.</li>
	 *            <li>An error happened while persisting this setting.</li>
	 *            <li>Resource changes are disallowed during certain types of
	 *            resource change event notification. See
	 *            {@link IResourceChangeEvent} for more details.</li>
	 *            </ul>
	 * @see #getCharset()
	 * @since 3.0
	 */
	public void setCharset(String newCharset, Object monitor) throws Exception;

}
