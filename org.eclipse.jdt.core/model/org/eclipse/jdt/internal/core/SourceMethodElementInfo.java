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
package org.eclipse.jdt.internal.core;

import java.util.ArrayList;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.compiler.env.ISourceMethod;

/** 
 * Element info for IMethod elements. 
 */
public class SourceMethodElementInfo extends MemberElementInfo implements ISourceMethod {

	/**
	 * For a source method (that is, a method contained in a compilation unit)
	 * this is a collection of the names of the parameters for this method,
	 * in the order the parameters are delcared. For a binary method (that is, 
	 * a method declared in a binary type), these names are invented as
	 * "arg"i where i starts at 1. This is an empty array if this method
	 * has no parameters.
	 */
	protected char[][] argumentNames;

	/**
	 * Collection of type names for the arguments in this
	 * method, in the order they are declared. This is an empty
	 * array for a method with no arguments. A name is a simple
	 * name or a qualified, dot separated name.
	 * For example, Hashtable or java.util.Hashtable.
	 */
	protected char[][] argumentTypeNames;

	/**
	 * Return type name for this method. The return type of
	 * constructors is equivalent to void.
	 */
	protected char[] returnType;

	/**
	 * A collection of type names of the exceptions this
	 * method throws, or an empty collection if this method
	 * does not declare to throw any exceptions. A name is a simple
	 * name or a qualified, dot separated name.
	 * For example, Hashtable or java.util.Hashtable.
	 */
	protected char[][] exceptionTypes;

	/**
	 * Constructor flag.
	 */
	protected boolean isConstructor= false;
public char[][] getArgumentNames() {
	return this.argumentNames;
}
public char[][] getArgumentTypeNames() {
	return this.argumentTypeNames;
}
public char[][] getExceptionTypeNames() {
	return this.exceptionTypes;
}
public char[] getReturnTypeName() {
	return this.returnType;
}
public char[] getSelector() {
	return this.name;
}
protected String getSignature() {

	String[] paramSignatures = new String[this.argumentTypeNames.length];
	for (int i = 0; i < this.argumentTypeNames.length; ++i) {
		paramSignatures[i] = Signature.createTypeSignature(this.argumentTypeNames[i], false);
	}
	return Signature.createMethodSignature(paramSignatures, Signature.createTypeSignature(this.returnType, false));
}
public char[][][] getTypeParameterBounds() {
	ArrayList typeParameterBounds = new ArrayList();
	for (int i = 0, length = this.children.length; i < length; i++) {
		IJavaElement child = this.children[i];
		if (child instanceof TypeParameter) {
			try {
				TypeParameterElementInfo info = (TypeParameterElementInfo) ((JavaElement)child).getElementInfo();
				typeParameterBounds.add(info.bounds);
			} catch (JavaModelException e) {
				// child does not exist: ignore
			}
		}
	}
	int size = typeParameterBounds.size();
	char[][][] result = new char[size][][];
	typeParameterBounds.toArray(result);
	return result;
}
public char[][] getTypeParameterNames() {
	ArrayList typeParameterNames = new ArrayList();
	for (int i = 0, length = this.children.length; i < length; i++) {
		IJavaElement child = this.children[i];
		if (child instanceof TypeParameter) {
			typeParameterNames.add(child.getElementName().toCharArray());
		}
	}
	int size = typeParameterNames.size();
	char[][] result = new char[size][];
	typeParameterNames.toArray(result);
	return result;
}
public boolean isConstructor() {
	return this.isConstructor;
}
protected void setArgumentNames(char[][] names) {
	this.argumentNames = names;
}
protected void setArgumentTypeNames(char[][] types) {
	this.argumentTypeNames = types;
}
protected void setConstructor(boolean isConstructor) {
	this.isConstructor = isConstructor;
}
protected void setExceptionTypeNames(char[][] types) {
	this.exceptionTypes = types;
}
protected void setReturnType(char[] type) {
	this.returnType = type;
}
}
