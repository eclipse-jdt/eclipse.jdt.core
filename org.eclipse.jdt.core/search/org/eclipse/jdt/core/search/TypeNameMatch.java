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
 * This class may be overridden by clients.
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
	this.type = type;
	this.modifiers = modifiers;
}

/**
 * Returns whether the stored type is equals to given object or not.
 */
public boolean equals(Object obj) {
	if (obj == null) return false;
	return getType().equals(obj);
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
	return getType().getFullyQualifiedName('.');
}

/**
 * Returns the modifiers of the matched type.
 * <p>
 * This is a handle-only method as neither Java Model nor classpath
 * initialization is done while calling this method.
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
	return (IPackageFragmentRoot) getType().getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
}

/**
 * Returns the package name of the stored type.
 * 
 * @see #getType()
 * @see IType#getPackageFragment()
 * @return the package name
 */
public String getPackageName() {
	return getType().getPackageFragment().getElementName();
}

/**
 * Returns the name of the stored type.
 * 
 * @see #getType()
 * @see IJavaElement#getElementName()
 * @return the type name
 */
public String getSimpleTypeName() {
	return getType().getElementName();
}

/**
 * Returns an non-null java model type handle. This handle may
 * exist or not.
 * <p>
 * This is a handle-only method as neither Java Model nor classpath
 * initializations are done while calling this method.
 * 
 * @see IType
 * @return the non-null handle on matched java model type.
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
	IType outerType = getType().getDeclaringType();
	if (outerType != null) {
		return outerType.getFullyQualifiedName('.');
	} else {
		return getType().getPackageFragment().getElementName();
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
	return getType().getTypeQualifiedName('.');
}

/* (non-Javadoc)
 * Returns the hash code of the matched type.
 * @see java.lang.Object#hashCode()
 */
public int hashCode() {
	return getType().hashCode();
}

/**
 * Set modifiers which corresponds to the matched type.
 * 
 * @param modifiers the modifiers of the matched type.
 */
public void setModifiers(int modifiers) {
	this.modifiers = modifiers;
}

/**
 * Set matched type.
 * 
 * @param type the matched type.
 */
public void setType(IType type) {
	this.type = type;
}


/* (non-Javadoc)
 * Returns the string of the matched type.
 * @see java.lang.Object#toString()
 */
public String toString() {
	return getType().toString();
}
}
