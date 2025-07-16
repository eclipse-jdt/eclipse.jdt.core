package org.eclipse.jdt.core;

/**
 * Classpath container implementations which are initialized asynchronously should implement this interface.
 * @since 3.43
 */
public interface IAsyncClasspathContainer extends IClasspathContainer {

	/**
	 * If container does extra work on creation and does not immediately return full set of classpath entries via
	 * {@link #getClasspathEntries()} this method should return {@code false}.
	 *
	 * @return {@code true}, if container is initialized
	 */
	boolean isInitialized();

	/**
	 * Can be used to trigger container initialization if the {@link #isInitialized()} reports {@code false}. Can be
	 * called by multiple threads, after this method call is finished, the container must be in initialized state and
	 * {@link #isInitialized()} must return {@code true}.
	 */
	void initialize();

}
