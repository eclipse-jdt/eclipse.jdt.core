package org.eclipse.jdt.internal.core.builder.impl;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.core.*;

/**
 * Type hierarchy indictments are issued whenever there is any
 * change to the supertype hierarchy graph for a given type.
 */
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
	return new StringBuffer("TypeHierarchyIndictment("/*nonNLS*/).append(fName).append(")"/*nonNLS*/).toString();
}
}
