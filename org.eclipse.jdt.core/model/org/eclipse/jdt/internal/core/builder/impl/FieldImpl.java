package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.builder.*;

public class FieldImpl extends AbstractMemberHandle implements IField{
	/**
	 * Creates a new non state specific field implementation
	 */
	FieldImpl(ClassOrInterfaceHandleImpl owner, String name) {
		fOwner = owner;
		fSignature = name;
	}
/**
 * getName method comment.
 */
public String getName() {
	return fSignature;
}
	/**
	 * Returns a Type object that identifies the declared type for
	 *	the field represented by this Field object.
	 */
	public IType getType() {
		return (IType)((IField)inCurrentState()).getType().nonStateSpecific();
	}
/**
 * Returns a state specific version of this handle in the given state.
 */
public IHandle inState(IState s) throws org.eclipse.jdt.internal.core.builder.StateSpecificException {
	
	return new FieldImplSWH((StateImpl) s, this);
}
	/**
	 * Returns a constant indicating what kind of handle this is.
	 */
	public int kind() {
		return IHandle.K_JAVA_FIELD;
	}
/**
 * toString method comment.
 */
public String toString() {
	return getDeclaringClass().getName() + "." + getName(); //$NON-NLS-1$
}
}
