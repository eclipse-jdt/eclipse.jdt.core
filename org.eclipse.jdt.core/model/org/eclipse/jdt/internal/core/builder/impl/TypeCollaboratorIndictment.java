package org.eclipse.jdt.internal.core.builder.impl;

class TypeCollaboratorIndictment extends Indictment {
/**
 * Creates a new TypeCollaboratorIndictment.
 */
protected TypeCollaboratorIndictment(char[] name) {
	super(name);
}
	/**
	 * Returns what kind of indictment this is
	 */
	public int getKind() {
		return K_TYPE;
	}
/**
 * Returns a string representation of this class.  For debugging purposes
 * only (NON-NLS).
 */
public String toString() {
	// don't use + with char[]
	return new StringBuffer("TypeIndictment("/*nonNLS*/).append(fName).append(")"/*nonNLS*/).toString();
}
}
