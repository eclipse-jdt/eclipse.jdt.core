package org.eclipse.jdt.internal.core.builder.impl;

public class TypeHierarchyIndictment extends Indictment {
	/**
	 * Creates a new TypeHierarchyIndictment.
	 */
	protected TypeHierarchyIndictment(char[] name) {
		super(name);
	}

	/**
	 * Returns what kind of indictment this is
	 */
	public int getKind() {
		return K_HIERARCHY;
	}

	/**
	 * Returns a string representation of this class.  For debugging purposes
	 * only (NON-NLS).
	 */
	public String toString() {
		// don't use + with char[]
		return new StringBuffer("TypeHierarchyIndictment(")
			.append(fName)
			.append(")")
			.toString();
	}

}
