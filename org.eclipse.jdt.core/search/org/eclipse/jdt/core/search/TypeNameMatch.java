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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;

/**
 * A match collected while searching for all type names
 * using {@link SearchEngine#searchAllTypeNames(
 *		char[] packageName, 
 *		int packageMatchRule, 
 *		char[] typeName,
 *		int typeMatchRule, 
 *		int searchFor, 
 *		IJavaSearchScope scope, 
 *		TypeNameMatchRequestor nameMatchRequestor,
 *		int waitingPolicy,
 * 	org.eclipse.core.runtime.IProgressMonitor monitor)}
 * method
 * <p>
 * User can get type from this match using {@link #resolvedType()} method.
 * </p><p>
 * This class is not intended to be instantiated or subclassed by clients.
 * </p>
 * @see TypeNameMatchRequestor
 * 
 * @since 3.3
 */
public class TypeNameMatch {

//private IType type;
private int modifiers;
private char[] packageName;
private char[] simpleTypeName;
private char[][] enclosingTypeNames;
private String path;
//private boolean initialized;

/**
 * Creates a new type name match.
 */
public TypeNameMatch(int modifiers, char[] packageName, char[] typeName, char[][] enclosingTypeNames, String path) {
	this.modifiers = modifiers;
	this.packageName = packageName;
	this.simpleTypeName = typeName;
	this.enclosingTypeNames = enclosingTypeNames;
	this.path = path;
}

/**
 * Returns the enclosing type names (if any) of the type.
 * 
 * @return the enclosing type names (if any) of the type
 */
public final char[][] getEnclosingTypeNames() {
	return enclosingTypeNames;
}

/**
 * Fully qualified name of type (e.g. package name + '.' enclosing type names + '.' simple name)
 * 
 * @return Fully qualified type name of the type
 */
public final char[] getFullyQualifiedName() {
	return CharOperation.concat(this.packageName, getTypeContainerName(), '.');
}

/**
 * Returns the modifiers of the type.
 * 
 * @return the type modifiers
 */
public final int getModifiers() {
	return modifiers;
}

/*
 * Specific package fragment root while resolving type
 */
protected IPackageFragmentRoot getPackageFragmentRoot() {
	return null;
}

/**
 * Returns the package name of the type.
 * 
 * @return the package name
 */
public final char[] getPackageName() {
	return packageName;
}

/**
 * Returns the full path of the resource.
 * This path may include the jar file path following by '|' separator
 * if the type is a binary included in a jar.
 * 
 * @return the full path of the resource
 */
public final String getPath() {
	return path;
}

/*
 * Project used to resolve type
 */
protected IProject getProject() {
	return ResourcesPlugin.getWorkspace().getRoot().getProject(new Path(this.path).segment(0));
}

/**
 * Returns the name of the type.
 * 
 * @return the type name
 */
public final char[] getSimpleTypeName() {
	return simpleTypeName;
}

/**
 * Returns the java model type corresponding to fully qualified type name
 * (based on package, enclosing types and simple name).
 * 
 * @return the java model type
 * @throws JavaModelException happens when type stored information are not valid
 */
public IType resolvedType() throws JavaModelException {
	IJavaProject javaProject = JavaCore.create(getProject());
	if (javaProject == null) return null; // cannot initialize without a project
	return javaProject.findType(new String(packageName), new String(getTypeContainerName()), getPackageFragmentRoot(), getWorkingCopies(), null);
}

/**
 * Name of the type container (e.g. enclosing type names + '.' + simple name)
 * 
 * @return Name of the type container
 */
public final char[] getTypeContainerName() {
	return this.enclosingTypeNames == null ? this.simpleTypeName : CharOperation.concatWith(this.enclosingTypeNames, this.simpleTypeName, '.');
}

/*
 * Working copies to look in while resolving type
 */
protected ICompilationUnit[] getWorkingCopies() {
	return null;
}

/* (non-Javadoc)
 * @see java.lang.Object#toString()
 */
public String toString() {
	return new String(getFullyQualifiedName());
}
}
