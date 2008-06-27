/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.util.ArrayList;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;

/**
 * @see IJavaElementRequestor
 */

public class JavaElementRequestor implements IJavaElementRequestor {
	/**
	 * True if this requestor no longer wants to receive
	 * results from its <code>IRequestorNameLookup</code>.
	 */
	protected boolean canceled= false;
	
	/**
	 * A collection of the resulting fields, or <code>null</code>
	 * if no field results have been received.
	 */
	protected ArrayList fields= null;

	/**
	 * A collection of the resulting initializers, or <code>null</code>
	 * if no initializer results have been received.
	 */
	protected ArrayList initializers= null;

	/**
	 * A collection of the resulting member types, or <code>null</code>
	 * if no member type results have been received.
	 */
	protected ArrayList memberTypes= null;

	/**
	 * A collection of the resulting methods, or <code>null</code>
	 * if no method results have been received.
	 */
	protected ArrayList methods= null;

	/**
	 * A collection of the resulting package fragments, or <code>null</code>
	 * if no package fragment results have been received.
	 */
	protected ArrayList packageFragments= null;

	/**
	 * A collection of the resulting types, or <code>null</code>
	 * if no type results have been received.
	 */
	protected ArrayList types= null;

	/**
	 * Empty arrays used for efficiency
	 */
	protected static IField[] EMPTY_FIELD_ARRAY= new IField[0];
	protected static IInitializer[] EMPTY_INITIALIZER_ARRAY= new IInitializer[0];
	protected static IType[] EMPTY_TYPE_ARRAY= new IType[0];
	protected static IPackageFragment[] EMPTY_PACKAGE_FRAGMENT_ARRAY= new IPackageFragment[0];
	protected static IMethod[] EMPTY_METHOD_ARRAY= new IMethod[0];
/**
 * @see IJavaElementRequestor
 */
public void acceptField(IField field) {
	if (fields == null) {
		fields= new ArrayList();
	}
	fields.add(field);
}
/**
 * @see IJavaElementRequestor
 */
public void acceptInitializer(IInitializer initializer) {
	if (initializers == null) {
		initializers= new ArrayList();
	}
	initializers.add(initializer);
}
/**
 * @see IJavaElementRequestor
 */
public void acceptMemberType(IType type) {
	if (memberTypes == null) {
		memberTypes= new ArrayList();
	}
	memberTypes.add(type);
}
/**
 * @see IJavaElementRequestor
 */
public void acceptMethod(IMethod method) {
	if (methods == null) {
		methods = new ArrayList();
	}
	methods.add(method);
}
/**
 * @see IJavaElementRequestor
 */
public void acceptPackageFragment(IPackageFragment packageFragment) {
	if (packageFragments== null) {
		packageFragments= new ArrayList();
	}
	packageFragments.add(packageFragment);
}
/**
 * @see IJavaElementRequestor
 */
public void acceptType(IType type) {
	if (types == null) {
		types= new ArrayList();
	}
	types.add(type);
}
/**
 * @see IJavaElementRequestor
 */
public IField[] getFields() {
	if (fields == null) {
		return EMPTY_FIELD_ARRAY;
	}
	int size = fields.size();
	IField[] results = new IField[size];
	fields.toArray(results);
	return results;
}
/**
 * @see IJavaElementRequestor
 */
public IInitializer[] getInitializers() {
	if (initializers == null) {
		return EMPTY_INITIALIZER_ARRAY;
	}
	int size = initializers.size();
	IInitializer[] results = new IInitializer[size];
	initializers.toArray(results);
	return results;
}
/**
 * @see IJavaElementRequestor
 */
public IType[] getMemberTypes() {
	if (memberTypes == null) {
		return EMPTY_TYPE_ARRAY;
	}
	int size = memberTypes.size();
	IType[] results = new IType[size];
	memberTypes.toArray(results);
	return results;
}
/**
 * @see IJavaElementRequestor
 */
public IMethod[] getMethods() {
	if (methods == null) {
		return EMPTY_METHOD_ARRAY;
	}
	int size = methods.size();
	IMethod[] results = new IMethod[size];
	methods.toArray(results);
	return results;
}
/**
 * @see IJavaElementRequestor
 */
public IPackageFragment[] getPackageFragments() {
	if (packageFragments== null) {
		return EMPTY_PACKAGE_FRAGMENT_ARRAY;
	}
	int size = packageFragments.size();
	IPackageFragment[] results = new IPackageFragment[size];
	packageFragments.toArray(results);
	return results;
}
/**
 * @see IJavaElementRequestor
 */
public IType[] getTypes() {
	if (types== null) {
		return EMPTY_TYPE_ARRAY;
	}
	int size = types.size();
	IType[] results = new IType[size];
	types.toArray(results);
	return results;
}
/**
 * @see IJavaElementRequestor
 */
public boolean isCanceled() {
	return canceled;
}
/**
 * Reset the state of this requestor.
 */
public void reset() {
	canceled = false;
	fields = null;
	initializers = null;
	memberTypes = null;
	methods = null;
	packageFragments = null;
	types = null;
}
/**
 * Sets the #isCanceled state of this requestor to true or false.
 */
public void setCanceled(boolean b) {
	canceled= b;
}
}
