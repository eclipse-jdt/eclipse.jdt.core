package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.builder.*;

public class ConstructorImpl extends AbstractMemberHandle implements IConstructor {
	ConstructorImpl (ClassOrInterfaceHandleImpl owner, IType[] paramTypes) {
		fOwner = owner;
		fSignature = computeSignature("", paramTypes);
	}
	public ConstructorImpl(ClassOrInterfaceHandleImpl owner, String signature) {
		fOwner = owner;
		fSignature = signature;
	}
	/**
	 * Returns an array of Type objects that represent the types of
	 *	the checked exceptions thrown by the underlying constructor
	 *	represented by this Constructor object.	
	 *	Unchecked exceptions are not included in the result, even if
	 *	they are declared in the source.
	 *	Returns an array of length 0 if the constructor throws no checked 
	 *	exceptions.
	 *	The resulting Types are in no particular order.
	 */
	public IType[] getExceptionTypes() {
		return nonStateSpecific(((IConstructor)inCurrentState()).getExceptionTypes());
	}
	/**
	 * Returns the simple name of the member represented by this object.
	 *	If this Member represents a constructor, this returns 
	 *	the simple name of its declaring class.
	 *	This is a handle-only method.
	 */
	public String getName() {
		return ((ClassOrInterfaceHandleImpl)getDeclaringClass()).sourceNameFromHandle();
	}
	/**
	 * Returns an array of Type objects that represent the formal
	 *	parameter types, in declaration order, of the constructor
	 *	represented by this Constructor object.	
	 *	Returns an array of length 0 if the underlying constructor 
	 *	takes no parameters.
	 *	This is a handle-only method.
	 */
	public IType[] getParameterTypes() {
		return getInternalDC().parameterTypesFromSignature(getSignature());
	}
/**
 * Returns a state specific version of this handle in the given state.
 */
public IHandle inState(IState s) throws org.eclipse.jdt.internal.core.builder.StateSpecificException {
	
	return new ConstructorImplSWH((StateImpl) s, this);
}
	/**
	  * Returns a constant indicating what kind of handle this is.
	  */
	public int kind() {
		return IHandle.K_JAVA_CONSTRUCTOR;
	}
/**
 * toString method comment.
 */
public String toString() {
	StringBuffer sb = new StringBuffer(getDeclaringClass().getName());
	sb.append('(');
	IType[] paramTypes = getParameterTypes();
	for (int i = 0; i < paramTypes.length; ++i) {
		if (i != 0) {
			sb.append(',');
		}
		sb.append(paramTypes[i].getName());
	}
	sb.append(')');
	return sb.toString();
}
}
