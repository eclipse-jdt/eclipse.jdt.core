package org.eclipse.jdt.core.internal.tools.unicode;

public abstract class Environment {
	/**
	 * Returns <code>true</code> if the given category is a valid one for the current environment, <code>false</code> otherwise.
	 * @param value the given category value
	 * @return <code>true</code> if the given category is a valid one for the current environment, <code>false</code> otherwise.
	 */
	public abstract boolean hasCategory(String value);
	
	/**
	 * Returns the resource file name for the given environment.
	 * @return the resource file name for the given environment.
	 */
	public abstract String getResourceFileName();
}
