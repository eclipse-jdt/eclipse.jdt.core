package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.builder.*;

public class MethodImpl extends AbstractMemberHandle implements IMethod {
	public MethodImpl(ClassOrInterfaceHandleImpl owner, String signature) {
		fOwner = owner;
		fSignature = signature;
	}
	MethodImpl(ClassOrInterfaceHandleImpl owner, String name, IType[] paramTypes) {
		fOwner = owner;
		fSignature = computeSignature(name, paramTypes);
	}
	/**
	 * Returns an array of Type objects that represent the types of
	 *	the checked exceptions thrown by the method
	 *	represented by this Method object.
	 *	Unchecked exceptions are not included in the result, even if
	 *	they are declared in the source.
	 *	Returns an array of length 0 if the method throws no checked 
	 *	exceptions.
	 *	The resulting Types are in no particular order.
	 */
	public IType[] getExceptionTypes() {
		return nonStateSpecific(((IMethod)inCurrentState()).getExceptionTypes());
	}
	/**
	 * Returns the simple name of the member represented by this object.
	 *	If this Member represents a constructor, this returns 
	 *	the simple name of its declaring class.
	 *	This is a handle-only method.
	 */
	public String getName() {
		String sig = getSignature();
		return sig.substring(0, sig.indexOf('('));
	}
	/**
	 * Returns an array of Type objects that represent the formal
	 *	parameter types, in declaration order, of the method
	 *	represented by this Method object.
	 *	Returns an array of length 0 if the underlying method takes 
	 *	no parameters.  This is a handle-only method.
	 */
	public IType[] getParameterTypes() {
		return getInternalDC().parameterTypesFromSignature(getSignature());
	}
	/**
	 * Returns a Type object that represents the formal return type
	 *	of the method represented by this Method object.
	 */
	public IType getReturnType() {
		IType wrapped = ((IMethod)inCurrentState()).getReturnType();
		return (IType) wrapped.nonStateSpecific();
	}
	/**
	 * Returns a string representing the constructor's parameters in the
	 * unqualified source signature format.
	 */
	protected String getSourceParameters() {
		String sourceSig = "(";

		/* append parameter types to signature */
		IType[] parms = getParameterTypes();
		for (int i = 0; i < parms.length; i++) {
			sourceSig += parms[i].getSimpleName().replace('L', 'Q');
		}
		sourceSig += ")";
		return sourceSig;
	}
/**
 * Returns a state specific version of this handle in the given state.
 */
public IHandle inState(IState s) throws org.eclipse.jdt.internal.core.builder.StateSpecificException {
	
	return new MethodImplSWH((StateImpl) s, this);
}
	/**
	 * Returns a constant indicating what kind of handle this is.
	 */
	public int kind() {
		return K_JAVA_METHOD;
	}
/**
 * toString method comment.
 */
public String toString() {
	StringBuffer sb = new StringBuffer(getDeclaringClass().getName());
	sb.append('.').append(getName()).append('(');
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
