package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.builder.IType;

class MethodCollaboratorIndictment extends Indictment {
	protected IType fOwner;
	protected int fParmCount;
/**
 * Creates a new MethodCollaboratorIndictment.
 */
protected MethodCollaboratorIndictment(IType owner, char[] name, int parmCount) {
	super(getMethodIndictmentKey(name, parmCount));
	fOwner = owner;
	fParmCount = parmCount;
}
	/**
	 * Returns true if indictments are equal, false otherwise
	 */
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null) return false;
		if (!this.getClass().equals(o.getClass())) return false;

		MethodCollaboratorIndictment f = (MethodCollaboratorIndictment)o;
		return (this.fName.equals(f.fName)) && (this.fParmCount == f.fParmCount);
	}
	/**
	 * Returns what kind of indictment this is
	 */
	public int getKind() {
		return K_METHOD;
	}
	/**
	 * Returns the owning type of the method.
	 */
	public IType getOwner() {
		return fOwner;
	}
	/**
	 * Returns the number of parameters to the method.
	 */
	public int getParmCount() {
		return fParmCount;
	}
	/**
	 * Returns a hashcode for the indictment
	 */
	public int hashCode() {
		return super.hashCode() + fParmCount;
	}
	/**
	 * Returns a string representation of this class.  For debugging purposes
	 * only (NON-NLS).
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer("MethodIndictment("/*nonNLS*/);
		buf.append(fName);
		buf.append('/');
		buf.append(fParmCount);
		buf.append(')');
		return buf.toString();
	}
}
