package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;

import org.eclipse.jdt.core.*;

import java.util.Vector;

/**
 * @see IJavaElementRequestor
 */

public class JavaElementRequestor implements IJavaElementRequestor {
	/**
	 * True if this requestor no longer wants to receive
	 * results from its <code>IRequestorNameLookup</code>.
	 */
	protected boolean fCanceled= false;
	
	/**
	 * A collection of the resulting fields, or <code>null</code>
	 * if no field results have been received.
	 */
	protected Vector fFields= null;

	/**
	 * A collection of the resulting initializers, or <code>null</code>
	 * if no initializer results have been received.
	 */
	protected Vector fInitializers= null;

	/**
	 * A collection of the resulting member types, or <code>null</code>
	 * if no member type results have been received.
	 */
	protected Vector fMemberTypes= null;

	/**
	 * A collection of the resulting methods, or <code>null</code>
	 * if no method results have been received.
	 */
	protected Vector fMethods= null;

	/**
	 * A collection of the resulting package fragments, or <code>null</code>
	 * if no package fragment results have been received.
	 */
	protected Vector fPackageFragments= null;

	/**
	 * A collection of the resulting types, or <code>null</code>
	 * if no type results have been received.
	 */
	protected Vector fTypes= null;

	/**
	 * Empty arrays used for efficiency
	 */
	protected static IField[] fgEmptyFieldArray= new IField[0];
	protected static IInitializer[] fgEmptyInitializerArray= new IInitializer[0];
	protected static IType[] fgEmptyTypeArray= new IType[0];
	protected static IPackageFragment[] fgEmptyPackageFragmentArray= new IPackageFragment[0];
	protected static IMethod[] fgEmptyMethodArray= new IMethod[0];
/**
 * @see IJavaElementRequestor
 */
public void acceptField(IField field) {
	if (fFields == null) {
		fFields= new Vector();
	}
	fFields.addElement(field);
}
/**
 * @see IJavaElementRequestor
 */
public void acceptInitializer(IInitializer initializer) {
	if (fInitializers == null) {
		fInitializers= new Vector();
	}
	fInitializers.addElement(initializer);
}
/**
 * @see IJavaElementRequestor
 */
public void acceptMemberType(IType type) {
	if (fMemberTypes == null) {
		fMemberTypes= new Vector();
	}
	fMemberTypes.addElement(type);
}
/**
 * @see IJavaElementRequestor
 */
public void acceptMethod(IMethod method) {
	if (fMethods == null) {
		fMethods = new Vector();
	}
	fMethods.addElement(method);
}
/**
 * @see IJavaElementRequestor
 */
public void acceptPackageFragment(IPackageFragment packageFragment) {
	if (fPackageFragments== null) {
		fPackageFragments= new Vector();
	}
	fPackageFragments.addElement(packageFragment);
}
/**
 * @see IJavaElementRequestor
 */
public void acceptType(IType type) {
	if (fTypes == null) {
		fTypes= new Vector();
	}
	fTypes.addElement(type);
}
/**
 * @see IJavaElementRequestor
 */
public IField[] getFields() {
	if (fFields == null) {
		return fgEmptyFieldArray;
	}
	int size = fFields.size();
	IField[] results = new IField[size];
	fFields.copyInto(results);
	return results;
}
/**
 * @see IJavaElementRequestor
 */
public IInitializer[] getInitializers() {
	if (fInitializers == null) {
		return fgEmptyInitializerArray;
	}
	int size = fInitializers.size();
	IInitializer[] results = new IInitializer[size];
	fInitializers.copyInto(results);
	return results;
}
/**
 * @see IJavaElementRequestor
 */
public IType[] getMemberTypes() {
	if (fMemberTypes == null) {
		return fgEmptyTypeArray;
	}
	int size = fMemberTypes.size();
	IType[] results = new IType[size];
	fMemberTypes.copyInto(results);
	return results;
}
/**
 * @see IJavaElementRequestor
 */
public IMethod[] getMethods() {
	if (fMethods == null) {
		return fgEmptyMethodArray;
	}
	int size = fMethods.size();
	IMethod[] results = new IMethod[size];
	fMethods.copyInto(results);
	return results;
}
/**
 * @see IJavaElementRequestor
 */
public IPackageFragment[] getPackageFragments() {
	if (fPackageFragments== null) {
		return fgEmptyPackageFragmentArray;
	}
	int size = fPackageFragments.size();
	IPackageFragment[] results = new IPackageFragment[size];
	fPackageFragments.copyInto(results);
	return results;
}
/**
 * @see IJavaElementRequestor
 */
public IType[] getTypes() {
	if (fTypes== null) {
		return fgEmptyTypeArray;
	}
	int size = fTypes.size();
	IType[] results = new IType[size];
	fTypes.copyInto(results);
	return results;
}
/**
 * @see IJavaElementRequestor
 */
public boolean isCanceled() {
	return fCanceled;
}
/**
 * Reset the state of this requestor.
 */
public void reset() {
	fCanceled = false;
	fFields = null;
	fInitializers = null;
	fMemberTypes = null;
	fMethods = null;
	fPackageFragments = null;
	fTypes = null;
}
/**
 * Sets the #isCanceled state of this requestor to true or false.
 */
public void setCanceled(boolean b) {
	fCanceled= b;
}
}
