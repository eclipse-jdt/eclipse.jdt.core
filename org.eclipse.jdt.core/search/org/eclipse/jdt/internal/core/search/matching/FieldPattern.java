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
package org.eclipse.jdt.internal.core.search.matching;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.core.search.indexing.IIndexConstants;

public class FieldPattern extends VariablePattern implements IIndexConstants {

// declaring type
protected char[] declaringQualification;
protected char[] declaringSimpleName;

// type
protected char[] typeQualification;
protected char[] typeSimpleName;
	
// Additional information for generics search
CompilationUnitScope unitScope;
protected boolean declaration;	// show whether the search is based on a declaration or an instance
protected char[][] typeNames;	// type arguments names storage
protected TypeBinding[] typeBindings;	// cache for type arguments bindings
protected int[] wildcards;	// show wildcard kind for each type arguments

protected static char[][] REF_CATEGORIES = { REF };
protected static char[][] REF_AND_DECL_CATEGORIES = { REF, FIELD_DECL };
protected static char[][] DECL_CATEGORIES = { FIELD_DECL };

public static char[] createIndexKey(char[] fieldName) {
	return fieldName;
}

public FieldPattern(
	boolean findDeclarations,
	boolean readAccess,
	boolean writeAccess,
	char[] name, 
	char[] declaringQualification,
	char[] declaringSimpleName,	
	char[] typeQualification, 
	char[] typeSimpleName,
	int matchRule) {

	super(FIELD_PATTERN, findDeclarations, readAccess, writeAccess, name, matchRule);

	this.declaringQualification = isCaseSensitive() ? declaringQualification : CharOperation.toLowerCase(declaringQualification);
	this.declaringSimpleName = isCaseSensitive() ? declaringSimpleName : CharOperation.toLowerCase(declaringSimpleName);
	this.typeQualification = isCaseSensitive() ? typeQualification : CharOperation.toLowerCase(typeQualification);
	this.typeSimpleName = isCaseSensitive() ? typeSimpleName : CharOperation.toLowerCase(typeSimpleName);

	((InternalSearchPattern)this).mustResolve = mustResolve();
}
/*
 * Instanciate a field pattern with additional information for generics search
 */
public FieldPattern(
	boolean findDeclarations,
	boolean readAccess,
	boolean writeAccess,
	char[] name, 
	char[] declaringQualification,
	char[] declaringSimpleName,	
	char[] typeQualification, 
	char[] typeSimpleName,
	char[][] typeNames,
	int[] wildcards,
	int matchRule) {

	this(findDeclarations, readAccess, writeAccess, name, declaringQualification, declaringSimpleName, typeQualification, typeSimpleName, matchRule);

	if (typeNames != null) {
		this.typeNames= typeNames;
		this.typeBindings = new TypeBinding[typeNames.length];
	}
	this.wildcards = wildcards;
}
public void decodeIndexKey(char[] key) {
	this.name = key;
}
public SearchPattern getBlankPattern() {
	return new FieldPattern(false, false, false, null, null, null, null, null, R_EXACT_MATCH | R_CASE_SENSITIVE);
}
public char[] getIndexKey() {
	return this.name;
}
public char[][] getIndexCategories() {
	if (this.findReferences)
		return this.findDeclarations || this.writeAccess ? REF_AND_DECL_CATEGORIES : REF_CATEGORIES;
	if (this.findDeclarations)
		return DECL_CATEGORIES;
	return CharOperation.NO_CHAR_CHAR;
}
/*
 * Get binding of type argument from a class unit scope and its index position.
 * Cache is lazy initialized and if no binding is found, then store a problem binding
 * to avoid making research twice...
 */
protected TypeBinding getTypeNameBinding(int index) {
	if (this.typeNames == null || index <0 || index > this.typeNames.length) return null;
	TypeBinding typeBinding = this.typeBindings[index];
	if (typeBinding == null) {
		typeBinding = this.unitScope.getType(this.typeNames[index]);
		this.typeBindings[index] = typeBinding;
	} 
	if (!typeBinding.isValidBinding()) {
		typeBinding = null;
	}
	return typeBinding;
}
public boolean matchesDecodedKey(SearchPattern decodedPattern) {
	return true; // index key is not encoded so query results all match
}
protected boolean mustResolve() {
	if (this.declaringSimpleName != null || this.declaringQualification != null) return true;
	if (this.typeSimpleName != null || this.typeQualification != null) return true;

	return super.mustResolve();
}
protected void setUnitScope(CompilationUnitScope unitScope) {
	if (unitScope != this.unitScope) {
		this.unitScope = unitScope;
		// reset bindings
		if (typeNames != null)
			this.typeBindings = new TypeBinding[typeNames.length];
	}
}
public String toString() {
	StringBuffer buffer = new StringBuffer(20);
	if (this.findDeclarations) {
		buffer.append(this.findReferences
			? "FieldCombinedPattern: " //$NON-NLS-1$
			: "FieldDeclarationPattern: "); //$NON-NLS-1$
	} else {
		buffer.append("FieldReferencePattern: "); //$NON-NLS-1$
	}
	if (declaringQualification != null) buffer.append(declaringQualification).append('.');
	if (declaringSimpleName != null) 
		buffer.append(declaringSimpleName).append('.');
	else if (declaringQualification != null) buffer.append("*."); //$NON-NLS-1$
	if (name == null) {
		buffer.append("*"); //$NON-NLS-1$
	} else {
		buffer.append(name);
	}
	if (typeQualification != null) 
		buffer.append(" --> ").append(typeQualification).append('.'); //$NON-NLS-1$
	else if (typeSimpleName != null) buffer.append(" --> "); //$NON-NLS-1$
	if (typeSimpleName != null) 
		buffer.append(typeSimpleName);
	else if (typeQualification != null) buffer.append("*"); //$NON-NLS-1$
	buffer.append(", "); //$NON-NLS-1$
	switch(getMatchMode()) {
		case R_EXACT_MATCH : 
			buffer.append("exact match, "); //$NON-NLS-1$
			break;
		case R_PREFIX_MATCH :
			buffer.append("prefix match, "); //$NON-NLS-1$
			break;
		case R_PATTERN_MATCH :
			buffer.append("pattern match, "); //$NON-NLS-1$
			break;
	}
	buffer.append(isCaseSensitive() ? "case sensitive" : "case insensitive"); //$NON-NLS-1$ //$NON-NLS-2$
	return buffer.toString();
}
}
