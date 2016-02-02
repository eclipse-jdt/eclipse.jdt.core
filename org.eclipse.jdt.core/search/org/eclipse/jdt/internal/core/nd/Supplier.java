package org.eclipse.jdt.internal.core.nd;

/**
 * TODO: Remove me once we can use the Java 8 interface
 */
public interface Supplier<T> {
	/**
	 * Returns the result.
	 */
	T get();
}