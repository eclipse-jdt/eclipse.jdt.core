/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.indexing;

import java.io.IOException;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.core.index.*;
import org.eclipse.jdt.internal.core.search.matching.*;

public abstract class AbstractIndexer implements IIndexer, IIndexConstants {

IIndexerOutput output;

public AbstractIndexer() {
	super();
}
public void addClassDeclaration(int modifiers, char[] packageName,char[] name,  char[][] enclosingTypeNames, char[] superclass, char[][] superinterfaces) {
	this.output.addRef(TypeDeclarationPattern.createClassDeclaration(packageName, enclosingTypeNames, name));

	this.output.addRef(
		SuperTypeReferencePattern.createReference(
			modifiers, packageName, name, enclosingTypeNames, CLASS_SUFFIX, superclass, CLASS_SUFFIX));
	if (superinterfaces != null)
		for (int i = 0, max = superinterfaces.length; i < max; i++)
			this.output.addRef(
				SuperTypeReferencePattern.createReference(
					modifiers, packageName, name, enclosingTypeNames, CLASS_SUFFIX, superinterfaces[i], INTERFACE_SUFFIX));
}
public void addConstructorDeclaration(char[] typeName, char[][] parameterTypes, char[][] exceptionTypes) {
	int argCount = parameterTypes == null ? 0 : parameterTypes.length;
	this.output.addRef(ConstructorPattern.createDeclaration(CharOperation.lastSegment(typeName,'.'), argCount));

	for (int i = 0; i < argCount; i++)
		addTypeReference(parameterTypes[i]);
	if (exceptionTypes != null)
		for (int i = 0, max = exceptionTypes.length; i < max; i++)
			addTypeReference(exceptionTypes[i]);
}
public void addConstructorReference(char[] typeName, int argCount) {
	this.output.addRef(ConstructorPattern.createReference(CharOperation.lastSegment(typeName,'.'), argCount));
}
public void addFieldDeclaration(char[] typeName, char[] fieldName) {
	this.output.addRef(FieldPattern.createDeclaration(fieldName));
	addTypeReference(typeName);
}
public void addFieldReference(char[] fieldName) {
	this.output.addRef(FieldPattern.createReference(fieldName));
}
public void addInterfaceDeclaration(int modifiers, char[] packageName, char[] name, char[][] enclosingTypeNames, char[][] superinterfaces) {
	this.output.addRef(TypeDeclarationPattern.createInterfaceDeclaration(packageName, enclosingTypeNames, name));

	if (superinterfaces != null)
		for (int i = 0, max = superinterfaces.length; i < max; i++)
			this.output.addRef(
				SuperTypeReferencePattern.createReference(
					modifiers, packageName, name, enclosingTypeNames, INTERFACE_SUFFIX, superinterfaces[i], INTERFACE_SUFFIX));
}
public void addMethodDeclaration(char[] methodName, char[][] parameterTypes, char[] returnType, char[][] exceptionTypes) {
	int argCount = parameterTypes == null ? 0 : parameterTypes.length;
	this.output.addRef(MethodPattern.createDeclaration(methodName, argCount));

	for (int i = 0; i < argCount; i++)
		addTypeReference(parameterTypes[i]);
	if (exceptionTypes != null)
		for (int i = 0, max = exceptionTypes.length; i < max; i++)
			addTypeReference(exceptionTypes[i]);
	if (returnType != null)
		addTypeReference(returnType);
}
public void addMethodReference(char[] methodName, int argCount) {
	this.output.addRef(MethodPattern.createReference(methodName, argCount));
}
public void addNameReference(char[] name) {
	this.output.addRef(CharOperation.concat(REF, name));
}
public void addTypeReference(char[] typeName) {
	this.output.addRef(TypeReferencePattern.createReference(CharOperation.lastSegment(typeName, '.')));
}
/**
 * Returns the file types the <code>IIndexer</code> handles.
 */
public abstract String[] getFileTypes();
/**
 * @see IIndexer#index(IDocument document, IIndexerOutput output)
 */
public void index(IDocument document, IIndexerOutput indexerOutput) throws IOException {
	this.output = indexerOutput;
	if (shouldIndex(document))
		indexFile(document);
}
protected abstract void indexFile(IDocument document) throws IOException;
/**
 * @see IIndexer#shouldIndex(IDocument document)
 */
public boolean shouldIndex(IDocument document) {
	String type = document.getType();
	String[] supportedTypes = this.getFileTypes();
	for (int i = 0, l = supportedTypes.length; i < l; i++)
		if (supportedTypes[i].equals(type)) return true;
	return false;
}
}
