package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.builder.IType;

/**
 * An AbstractMethodCollaboratorIndictment is issued whenever an interface or abstract
 * class adds an abstract method.  All direct subtypes of the originating 
 * interface or abstract class must be found guilty and recompiled, 
 * regardless of evidence.
 */
class AbstractMethodCollaboratorIndictment extends Indictment {

	protected IType fType;
	
/**
 * Creates a new AbstractMethodCollaboratorIndictment for the given type.
 */
protected AbstractMethodCollaboratorIndictment(IType type) {
	super(type.getName().toCharArray());
	fType = type;
}
	/**
	 * Returns what kind of indictment this is
	 */
	public int getKind() {
		return K_ABSTRACT_METHOD;
	}
	/**
	 * Returns the type handle.
	 */
	public IType getType() {
		return fType;
	}
/**
 * Returns a string representation of this class.  For debugging purposes
 * only (NON-NLS).
 */
public String toString() {
	// don't use + with char[]
	return new StringBuffer("AbstractMethodCollaboratorIndictment(").append(fName).append(")").toString(); //$NON-NLS-1$ //$NON-NLS-2$
}
}
