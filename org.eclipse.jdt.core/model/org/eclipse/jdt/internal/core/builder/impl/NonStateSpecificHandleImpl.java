package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.Assert;
import org.eclipse.jdt.internal.core.builder.*;

public abstract class NonStateSpecificHandleImpl implements IHandle {
public boolean equals(Object obj) {
	Assert.isTrue(false, "TBD");
	return false;
}
	public IDevelopmentContext getDevelopmentContext() {
		return getInternalDC();
	}
	abstract JavaDevelopmentContextImpl getInternalDC();
public IState getState() {
	throw new org.eclipse.jdt.internal.core.builder.StateSpecificException();
}
	/**
	 * Returns a consistent hash code for this object
	 */
	public abstract int hashCode();
	/**
	 * Returns a state-specific version of this handle in the current state
	 */
	protected IHandle inCurrentState() {
		return inState(getDevelopmentContext().getCurrentState());
	}
	/**
	 * Returns a state-specific version of this handle in the given state
	 */
	public abstract IHandle inState(IState s) throws org.eclipse.jdt.internal.core.builder.StateSpecificException;
public boolean isFictional() {
	return inCurrentState().isFictional();
}
public boolean isPresent() {
	return inCurrentState().isPresent();
}
public boolean isStateSpecific() {
	return false;
}
public abstract int kind();
public IHandle nonStateSpecific() {
	throw new org.eclipse.jdt.internal.core.builder.StateSpecificException();
}
/**
 * Converts an array of state-specific constructors to non-state-specific constructors.
 */
static IConstructor[] nonStateSpecific(IConstructor[] stateSpecific) {
	int len = stateSpecific.length;
	if (len == 0) return stateSpecific;
	IConstructor[] result = new IConstructor[len]; 
	for (int i = 0; i < len; ++i) {
		result[i] = (IConstructor) stateSpecific[i].nonStateSpecific();
	} 
	return result;
}
/**
 * Converts an array of state-specific fields to non-state-specific fields.
 */
static IField[] nonStateSpecific(IField[] stateSpecific) {
	int len = stateSpecific.length;
	if (len == 0) return stateSpecific;
	IField[] result = new IField[len]; 
	for (int i = 0; i < len; ++i) {
		result[i] = (IField) stateSpecific[i].nonStateSpecific();
	} 
	return result;
}
/**
 * Converts an array of state-specific methods to non-state-specific methods.
 */
static IMethod[] nonStateSpecific(IMethod[] stateSpecific) {
	int len = stateSpecific.length;
	if (len == 0) return stateSpecific;
	IMethod[] result = new IMethod[len]; 
	for (int i = 0; i < len; ++i) {
		result[i] = (IMethod) stateSpecific[i].nonStateSpecific();
	} 
	return result;
}
/**
 * Converts an array of state-specific packages to non-state-specific packages.
 */
static IPackage[] nonStateSpecific(IPackage[] stateSpecific) {
	int len = stateSpecific.length;
	if (len == 0) return stateSpecific;
	IPackage[] result = new IPackage[len]; 
	for (int i = 0; i < len; ++i) {
		result[i] = (IPackage) stateSpecific[i].nonStateSpecific();
	} 
	return result;
}
/**
 * Converts an array of state-specific types to non-state-specific types.
 */
static IType[] nonStateSpecific(IType[] stateSpecific) {
	int len = stateSpecific.length;
	if (len == 0) return stateSpecific;
	IType[] result = new IType[len]; 
	for (int i = 0; i < len; ++i) {
		result[i] = (IType) stateSpecific[i].nonStateSpecific();
	} 
	return result;
}
public String toString() {
	Assert.isTrue(false, "TBD");
	return null;
}
}
