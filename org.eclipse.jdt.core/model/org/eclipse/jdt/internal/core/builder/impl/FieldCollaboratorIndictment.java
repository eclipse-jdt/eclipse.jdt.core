package org.eclipse.jdt.internal.core.builder.impl;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.core.*;

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
	return new StringBuffer("FieldIndictment("/*nonNLS*/).append(fName).append(")"/*nonNLS*/).toString();
}
}
