package org.eclipse.jdt.internal.core.builder.impl;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.core.*;

/**
 * These indictments are issued when there is a T1 change to a
 * type, including change of visibility, change of gender, abstractness, etc.
 */
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
	return new StringBuffer("TypeIndictment(").append(fName).append(")").toString(); //$NON-NLS-1$ //$NON-NLS-2$
}
}
