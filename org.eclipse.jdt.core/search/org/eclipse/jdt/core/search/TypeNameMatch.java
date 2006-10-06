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
package org.eclipse.jdt.core.search;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

/**
 * A match collected while searching for all type names using
 * {@link SearchEngine#searchAllTypeNames( char[] packageName, int
 * packageMatchRule, char[] typeName, int typeMatchRule, int searchFor,
 * IJavaSearchScope scope, TypeNameMatchRequestor nameMatchRequestor, int
 * waitingPolicy, org.eclipse.core.runtime.IProgressMonitor monitor)} method
 * <p>
 * User can get type from this match using {@link #getType()} method.
 * </p>
 * <p>
 * This class is not intended to be instantiated or subclassed by clients.
 * </p>
 * 
 * @see TypeNameMatchRequestor
 * 
 * @since 3.3
 */
public class TypeNameMatch {

private IType type;

private int modifiers = -1; // store modifiers to avoid java model population

/**
 * Creates a new type name match.
 */
public TypeNameMatch(IType type) {
	this.type = type;
}

public TypeNameMatch(IType type, int modifiers) {
	this(type);
	this.modifiers = modifiers;
}

/**
 * Returns the java model type corresponding to fully qualified type name (based
 * on package, enclosing types and simple name).
 * 
 * @return the java model type
 * @throws JavaModelException
 *             happens when type stored information are not valid
 */
public IType getType() throws JavaModelException {
	return this.type;
}

/*
 * (non-Javadoc)
 * 
 * @see java.lang.Object#toString()
 */
public String toString() {
	return this.type.toString();
}

public IPackageFragmentRoot getPackageFragmentRoot() {
	return (IPackageFragmentRoot) this.type.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
}

/**
 * Fully qualified name of type (e.g. package name + '.' enclosing type names +
 * '.' simple name)
 * 
 * @return Fully qualified type name of the type
 */
public String getFullyQualifiedName() {
	return this.type.getFullyQualifiedName('.');
}

/**
 * Fully qualified name of type (e.g. package name + '.' enclosing type names +
 * '.' simple name)
 * 
 * @return Fully qualified type name of the type
 */
public String getTypeQualifiedName() {
	return this.type.getTypeQualifiedName('.');
}

/**
 * Returns the modifiers of the type.
 * 
 * @return the type modifiers
 */
public int getModifiers() {
	return this.modifiers;
}

/**
 * Returns the package name of the type.
 * 
 * @return the package name
 */
public String getPackageName() {
	return this.type.getPackageFragment().getElementName();
}

/**
 * Returns the name of the type.
 * 
 * @return the type name
 */
public String getSimpleTypeName() {
	return this.type.getElementName();
}

/**
 * Name of the type container (e.g. enclosing type names + '.' + simple name)
 * 
 * @return Name of the type container
 */
public String getTypeContainerName() {
	IType outerType = this.type.getDeclaringType();
	if (outerType != null) {
		return outerType.getFullyQualifiedName('.');
	} else {
		return this.type.getPackageFragment().getElementName();
	}
}
}
