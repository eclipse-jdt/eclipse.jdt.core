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

import java.io.IOException;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.core.index.*;
import org.eclipse.jdt.internal.core.search.indexing.IIndexConstants;

public class MethodPattern extends JavaSearchPattern implements IIndexConstants {

protected boolean findDeclarations;
protected boolean findReferences;

public char[] selector;

public char[] declaringQualification;
public char[] declaringSimpleName;

public char[] returnQualification;
public char[] returnSimpleName;

public char[][] parameterQualifications;
public char[][] parameterSimpleNames;
public int parameterCount;

// extra reference info
protected IType declaringType;

protected static char[][] REF_CATEGORIES = { METHOD_REF };
protected static char[][] REF_AND_DECL_CATEGORIES = { METHOD_REF, METHOD_DECL };
protected static char[][] DECL_CATEGORIES = { METHOD_DECL };

/**
 * Method entries are encoded as selector '/' Arity:
 * e.g. 'foo/0'
 */
public static char[] createIndexKey(char[] selector, int argCount) {
	char[] countChars = argCount < 10
		? COUNTS[argCount]
		: ("/" + String.valueOf(argCount)).toCharArray(); //$NON-NLS-1$
	return CharOperation.concat(selector, countChars);
}

public MethodPattern(
	boolean findDeclarations,
	boolean findReferences,
	char[] selector, 
	char[] declaringQualification,
	char[] declaringSimpleName,	
	char[] returnQualification, 
	char[] returnSimpleName,
	char[][] parameterQualifications, 
	char[][] parameterSimpleNames,
	IType declaringType,
	int matchRule) {

	this(matchRule);

	this.findDeclarations = findDeclarations;
	this.findReferences = findReferences;

	this.selector = isCaseSensitive() ? selector : CharOperation.toLowerCase(selector);
	this.declaringQualification = isCaseSensitive() ? declaringQualification : CharOperation.toLowerCase(declaringQualification);
	this.declaringSimpleName = isCaseSensitive() ? declaringSimpleName : CharOperation.toLowerCase(declaringSimpleName);
	this.returnQualification = isCaseSensitive() ? returnQualification : CharOperation.toLowerCase(returnQualification);
	this.returnSimpleName = isCaseSensitive() ? returnSimpleName : CharOperation.toLowerCase(returnSimpleName);
	if (parameterSimpleNames != null) {
		this.parameterCount = parameterSimpleNames.length;
		this.parameterQualifications = new char[this.parameterCount][];
		this.parameterSimpleNames = new char[this.parameterCount][];
		for (int i = 0; i < this.parameterCount; i++) {
			this.parameterQualifications[i] = isCaseSensitive() ? parameterQualifications[i] : CharOperation.toLowerCase(parameterQualifications[i]);
			this.parameterSimpleNames[i] = isCaseSensitive() ? parameterSimpleNames[i] : CharOperation.toLowerCase(parameterSimpleNames[i]);
		}
	} else {
		this.parameterCount = -1;
	}

	this.declaringType = declaringType;
	((InternalSearchPattern)this).mustResolve = mustResolve();
}
MethodPattern(int matchRule) {
	super(METHOD_PATTERN, matchRule);
}
public void decodeIndexKey(char[] key) {
	int size = key.length;
	int lastSeparatorIndex = CharOperation.lastIndexOf(SEPARATOR, key);	

	this.parameterCount = Integer.parseInt(new String(key, lastSeparatorIndex + 1, size - lastSeparatorIndex - 1));
	this.selector = CharOperation.subarray(key, 0, lastSeparatorIndex);
}
public SearchPattern getBlankPattern() {
	return new MethodPattern(R_EXACT_MATCH | R_CASE_SENSITIVE);
}
public char[][] getIndexCategories() {
	if (this.findReferences)
		return this.findDeclarations ? REF_AND_DECL_CATEGORIES : REF_CATEGORIES;
	if (this.findDeclarations)
		return DECL_CATEGORIES;
	return CharOperation.NO_CHAR_CHAR;
}
boolean isPolymorphicSearch() {
	return this.findReferences;
}
public boolean matchesDecodedKey(SearchPattern decodedPattern) {
	MethodPattern pattern = (MethodPattern) decodedPattern;

	return (this.parameterCount == pattern.parameterCount || this.parameterCount == -1)
		&& matchesName(this.selector, pattern.selector);
}
/**
 * Returns whether a method declaration or message send must be resolved to 
 * find out if this method pattern matches it.
 */
protected boolean mustResolve() {
	// declaring type
	// If declaring type is specified - even with simple name - always resolves
	if (declaringSimpleName != null || declaringQualification != null) return true;

	// return type
	// If return type is specified - even with simple name - always resolves
	if (returnSimpleName != null || returnQualification != null) return true;

	// parameter types
	if (parameterSimpleNames != null)
		for (int i = 0, max = parameterSimpleNames.length; i < max; i++)
			if (parameterQualifications[i] != null) return true;
	return false;
}
EntryResult[] queryIn(Index index) throws IOException {
	char[] key = this.selector; // can be null
	int matchRule = getMatchRule();

	switch(getMatchMode()) {
		case R_EXACT_MATCH :
			if (this.selector != null && this.parameterCount >= 0)
				key = createIndexKey(this.selector, this.parameterCount);
			else // do a prefix query with the selector
				matchRule = matchRule - R_EXACT_MATCH + R_PREFIX_MATCH;
			break;
		case R_PREFIX_MATCH :
			// do a prefix query with the selector
			break;
		case R_PATTERN_MATCH :
			if (this.parameterCount >= 0)
				key = createIndexKey(this.selector == null ? ONE_STAR : this.selector, this.parameterCount);
			else if (this.selector != null && this.selector[this.selector.length - 1] != '*')
				key = CharOperation.concat(this.selector, ONE_STAR, SEPARATOR);
			// else do a pattern query with just the selector
			break;
	}

	return index.query(getIndexCategories(), key, matchRule); // match rule is irrelevant when the key is null
}
public String toString() {
	StringBuffer buffer = new StringBuffer(20);
	if (this.findDeclarations) {
		buffer.append(this.findReferences
			? "MethodCombinedPattern: " //$NON-NLS-1$
			: "MethodDeclarationPattern: "); //$NON-NLS-1$
	} else {
		buffer.append("MethodReferencePattern: "); //$NON-NLS-1$
	}
	if (declaringQualification != null)
		buffer.append(declaringQualification).append('.');
	if (declaringSimpleName != null) 
		buffer.append(declaringSimpleName).append('.');
	else if (declaringQualification != null)
		buffer.append("*."); //$NON-NLS-1$

	if (selector != null)
		buffer.append(selector);
	else
		buffer.append("*"); //$NON-NLS-1$
	buffer.append('(');
	if (parameterSimpleNames == null) {
		buffer.append("..."); //$NON-NLS-1$
	} else {
		for (int i = 0, max = parameterSimpleNames.length; i < max; i++) {
			if (i > 0) buffer.append(", "); //$NON-NLS-1$
			if (parameterQualifications[i] != null) buffer.append(parameterQualifications[i]).append('.');
			if (parameterSimpleNames[i] == null) buffer.append('*'); else buffer.append(parameterSimpleNames[i]);
		}
	}
	buffer.append(')');
	if (returnQualification != null) 
		buffer.append(" --> ").append(returnQualification).append('.'); //$NON-NLS-1$
	else if (returnSimpleName != null)
		buffer.append(" --> "); //$NON-NLS-1$
	if (returnSimpleName != null) 
		buffer.append(returnSimpleName);
	else if (returnQualification != null)
		buffer.append("*"); //$NON-NLS-1$
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
