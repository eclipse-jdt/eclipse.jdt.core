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

import org.eclipse.jdt.core.*;

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
public TypeNameMatch(IType type, int modifiers) {
	// TODO (frederic) Disable null check as it currently breaks JDT/UI => put back ASAP
	// (see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=160652 for follow-up on this issue)
	//Assert.isNotNull(type, "Type cannot be null for a name match!"); //$NON-NLS-1$
	this.type = type;
	this.modifiers = modifiers;
}

/**
 * Returns whether the stored type is equals to given object or not.
 */
public boolean equals(Object obj) {
	if (obj == null) return false;
	return this.type.equals(obj);
}

/**
 * Returns the fully qualified name of stored type
 * (e.g. package name + '.' enclosing type names + '.' simple name)
 * 
 * @see #getType()
 * @see IType#getFullyQualifiedName(char)
 * @return Fully qualified type name of the type
 */
public String getFullyQualifiedName() {
	return this.type.getFullyQualifiedName('.');
}

/**
 * Returns the stored modifiers of the type.
 * This is a handle-only method.
 * 
 * @return the type modifiers
 */
public int getModifiers() {
	return this.modifiers;
}

/**
 * Returns the package fragment root of the stored type.
 * Package fragment root cannot be null and <strong>does</strong> exist.
 * 
 * @see #getType()
 * @see IJavaElement#getAncestor(int)
 * @return the existing java model package fragment root (ie. cannot be <code>null</code>
 * 	and will return <code>true</code> to <code>exists()</code> message).
 */
public IPackageFragmentRoot getPackageFragmentRoot() {
	return (IPackageFragmentRoot) this.type.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
}

/**
 * Returns the package name of the stored type.
 * 
 * @see #getType()
 * @see IType#getPackageFragment()
 * @return the package name
 */
public String getPackageName() {
	return this.type.getPackageFragment().getElementName();
}

/**
 * Returns the name of the stored type.
 * 
 * @see #getType()
 * @see IJavaElement#getElementName()
 * @return the type name
 */
public String getSimpleTypeName() {
	return this.type.getElementName();
}

/**
 * Returns the stored java model type. As this match was built while searching
 * for all types in index files, the stored type cannot be null and does exist.
 * This is a handle-only method.
 * 
 * @see IType
 * @return the existing java model type (ie. cannot be <code>null</code>
 * 	and will return <code>true</code> to <code>exists()</code> message).
 */
public IType getType() {
	return this.type;
}

/**
 * Name of the type container (e.g. enclosing type names + '.' + simple name).
 * 
 * @see #getType()
 * @see IMember#getDeclaringType()
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

/**
 * Returns the qualified name of type
 * (e.g. enclosing type names + '.' simple name).
 * 
 * @see #getType()
 * @see IType#getTypeQualifiedName(char)
 * @return Fully qualified type name of the type
 */
public String getTypeQualifiedName() {
	return this.type.getTypeQualifiedName('.');
}

/**
 * Returns stored type hashCode.
 */
public int hashCode() {
	return this.type.hashCode();
}

/**
 * Returns stored type string.
 */
public String toString() {
	return this.type.toString();
}
}
