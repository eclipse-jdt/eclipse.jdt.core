/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.hierarchy;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.compiler.env.IGenericType;

/**
 * 
 * Partial implementation of an IGenericType used to
 * answer hierarchies.
 */
public class HierarchyType implements IGenericType {

	public IType typeHandle;
	public boolean isClass;
	public char[] name;
	public int modifiers;
	public char[] superclassName;
	public char[][] superInterfaceNames;
	
public HierarchyType(
	IType typeHandle, 
	boolean isClass, 
	char[] name, 
	int modifiers, 
	char[] superclassName,
	char[][] superInterfaceNames) {
		
	this.typeHandle = typeHandle;
	this.isClass = isClass;
	this.name = name;
	this.modifiers = modifiers;
	this.superclassName = superclassName;
	this.superInterfaceNames = superInterfaceNames;
}
/**
 * Answer the file name which defines the type.
 *
 * The path part (optional) must be separated from the actual
 * file proper name by a java.io.File.separator.
 *
 * The proper file name includes the suffix extension (e.g. ".java")
 *
 * e.g. "c:/com/ibm/compiler/java/api/Compiler.java" 
 */
public char[] getFileName() {
	return this.typeHandle.getCompilationUnit().getElementName().toCharArray();
}
/**
 * Answer an int whose bits are set according the access constants
 * defined by the VM spec.
 */
public int getModifiers() {
	return this.modifiers;
}
/**
 * Answer whether the receiver contains the resolved binary form
 * or the unresolved source form of the type.
 */
public boolean isBinaryType() {
	return false;
}
/**
 * isClass method comment.
 */
public boolean isClass() {
	return this.isClass;
}
/**
 * isInterface method comment.
 */
public boolean isInterface() {
	return !isClass;
}
}
