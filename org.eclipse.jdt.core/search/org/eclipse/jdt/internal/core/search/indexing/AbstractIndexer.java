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
package org.eclipse.jdt.internal.core.search.indexing;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.SearchDocument;
import org.eclipse.jdt.internal.core.search.matching.*;

public abstract class AbstractIndexer implements IIndexConstants {

	SearchDocument document;

	public AbstractIndexer(SearchDocument document) {
		this.document = document;
	}
	public void addClassDeclaration(int modifiers, char[] packageName,char[] name,  char[][] enclosingTypeNames, char[] superclass, char[][] superinterfaces) {
		addIndexEntry(TYPE_DECL, TypeDeclarationPattern.createIndexKey(name, packageName, enclosingTypeNames, CLASS_SUFFIX));

		if (superclass != null)
			addTypeReference(superclass);
		addIndexEntry(
			SUPER_REF, 
			SuperTypeReferencePattern.createIndexKey(
				modifiers, packageName, name, enclosingTypeNames, CLASS_SUFFIX, superclass, CLASS_SUFFIX));
		if (superinterfaces != null) {
			for (int i = 0, max = superinterfaces.length; i < max; i++) {
				addTypeReference(superinterfaces[i]);
				addIndexEntry(
					SUPER_REF,
					SuperTypeReferencePattern.createIndexKey(
						modifiers, packageName, name, enclosingTypeNames, CLASS_SUFFIX, superinterfaces[i], INTERFACE_SUFFIX));
			}
		}
	}
	public void addConstructorDeclaration(char[] typeName, char[][] parameterTypes, char[][] exceptionTypes) {
		int argCount = parameterTypes == null ? 0 : parameterTypes.length;
		addIndexEntry(CONSTRUCTOR_DECL, ConstructorPattern.createIndexKey(CharOperation.lastSegment(typeName,'.'), argCount));
	
		for (int i = 0; i < argCount; i++)
			addTypeReference(parameterTypes[i]);
		if (exceptionTypes != null)
			for (int i = 0, max = exceptionTypes.length; i < max; i++)
				addTypeReference(exceptionTypes[i]);
	}
	public void addConstructorReference(char[] typeName, int argCount) {
		char[] simpleTypeName = CharOperation.lastSegment(typeName,'.');
		addTypeReference(simpleTypeName);
		addIndexEntry(CONSTRUCTOR_REF, ConstructorPattern.createIndexKey(simpleTypeName, argCount));
	}
	public void addFieldDeclaration(char[] typeName, char[] fieldName) {
		addIndexEntry(FIELD_DECL, FieldPattern.createIndexKey(fieldName));
		addTypeReference(typeName);
	}
	public void addFieldReference(char[] fieldName) {
		addNameReference(fieldName);
	}
	protected void addIndexEntry(char[] category, char[] key) {
		this.document.addIndexEntry(category, key);
	}
	public void addInterfaceDeclaration(int modifiers, char[] packageName, char[] name, char[][] enclosingTypeNames, char[][] superinterfaces) {
		addIndexEntry(TYPE_DECL, TypeDeclarationPattern.createIndexKey(name, packageName, enclosingTypeNames, INTERFACE_SUFFIX));

		if (superinterfaces != null) {
			for (int i = 0, max = superinterfaces.length; i < max; i++) {
				addTypeReference(superinterfaces[i]);
				addIndexEntry(
					SUPER_REF,
					SuperTypeReferencePattern.createIndexKey(
						modifiers, packageName, name, enclosingTypeNames, INTERFACE_SUFFIX, superinterfaces[i], INTERFACE_SUFFIX));
			}
		}
	}
	public void addMethodDeclaration(char[] methodName, char[][] parameterTypes, char[] returnType, char[][] exceptionTypes) {
		int argCount = parameterTypes == null ? 0 : parameterTypes.length;
		addIndexEntry(METHOD_DECL, MethodPattern.createIndexKey(methodName, argCount));
	
		for (int i = 0; i < argCount; i++)
			addTypeReference(parameterTypes[i]);
		if (exceptionTypes != null)
			for (int i = 0, max = exceptionTypes.length; i < max; i++)
				addTypeReference(exceptionTypes[i]);
		if (returnType != null)
			addTypeReference(returnType);
	}
	public void addMethodReference(char[] methodName, int argCount) {
		addIndexEntry(METHOD_REF, MethodPattern.createIndexKey(methodName, argCount));
	}
	public void addNameReference(char[] name) {
		addIndexEntry(REF, name);
	}
	public void addTypeReference(char[] typeName) {
		addNameReference(CharOperation.lastSegment(typeName, '.'));
	}
	public abstract void indexDocument();
}
