package org.eclipse.jdt.internal.core.util;

public abstract class ReferenceInfoAdapter {
	/**
	 * Does nothing.
	 */
	public void acceptConstructorReference(
		char[] typeName,
		int argCount,
		int sourcePosition) {
	}

	/**
	 * Does nothing.
	 */
	public void acceptFieldReference(char[] fieldName, int sourcePosition) {
	}

	/**
	 * Does nothing.
	 */
	public void acceptMethodReference(
		char[] methodName,
		int argCount,
		int sourcePosition) {
	}

	/**
	 * Does nothing.
	 */
	public void acceptTypeReference(
		char[][] typeName,
		int sourceStart,
		int sourceEnd) {
	}

	/**
	 * Does nothing.
	 */
	public void acceptTypeReference(char[] typeName, int sourcePosition) {
	}

	/**
	 * Does nothing.
	 */
	public void acceptUnknownReference(
		char[][] name,
		int sourceStart,
		int sourceEnd) {
	}

	/**
	 * Does nothing.
	 */
	public void acceptUnknownReference(char[] name, int sourcePosition) {
	}

}
