package org.eclipse.jdt.internal.core.builder.impl;

class FieldCollaboratorIndictment extends Indictment {
/**
 * Creates a new FieldCollaboratorIndictment.
 */
protected FieldCollaboratorIndictment(char[] name) {
	super(name);
}
	/**
	 * Returns what kind of indictment this is
	 */
	public int getKind() {
		return K_FIELD;
	}
/**
 * Returns a string representation of this class.  For debugging purposes
 * only (NON-NLS).
 */
public String toString() {
	// don't use + with char[]
	return new StringBuffer("FieldIndictment(").append(fName).append(")").toString();
}
}
