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
package org.eclipse.jdt.internal.core.search.matching;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.*;

public class MethodPattern extends SearchPattern {

private static ThreadLocal indexRecord = new ThreadLocal() {
	protected Object initialValue() {
		return new MethodPattern(false, false, null, null, null, null, null, null, null, null, R_EXACT_MATCH | R_CASE_SENSITIVE);
	}
};

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

public static char[] createIndexKey(char[] selector, int argCount) {
	MethodPattern record = getMethodRecord();
	record.selector = selector;
	record.parameterCount = argCount;
	return record.encodeIndexKey();
}
public static MethodPattern getMethodRecord() {
	return (MethodPattern)indexRecord.get();
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

	super(METHOD_PATTERN, matchRule);

	this.findDeclarations = findDeclarations;
	this.findReferences = findReferences;

	boolean isCaseSensitive = isCaseSensitive();
	this.selector = isCaseSensitive ? selector : CharOperation.toLowerCase(selector);
	this.declaringQualification = isCaseSensitive ? declaringQualification : CharOperation.toLowerCase(declaringQualification);
	this.declaringSimpleName = isCaseSensitive ? declaringSimpleName : CharOperation.toLowerCase(declaringSimpleName);
	this.returnQualification = isCaseSensitive ? returnQualification : CharOperation.toLowerCase(returnQualification);
	this.returnSimpleName = isCaseSensitive ? returnSimpleName : CharOperation.toLowerCase(returnSimpleName);
	if (parameterSimpleNames != null) {
		this.parameterCount = parameterSimpleNames.length;
		this.parameterQualifications = new char[this.parameterCount][];
		this.parameterSimpleNames = new char[this.parameterCount][];
		for (int i = 0; i < this.parameterCount; i++) {
			this.parameterQualifications[i] = isCaseSensitive ? parameterQualifications[i] : CharOperation.toLowerCase(parameterQualifications[i]);
			this.parameterSimpleNames[i] = isCaseSensitive ? parameterSimpleNames[i] : CharOperation.toLowerCase(parameterSimpleNames[i]);
		}
	} else {
		this.parameterCount = -1;
	}

	this.declaringType = declaringType;
	this.mustResolve = mustResolve();
}
public void decodeIndexKey(char[] key) {
	int size = key.length;
	int lastSeparatorIndex = CharOperation.lastIndexOf(SEPARATOR, key);	

	this.parameterCount = Integer.parseInt(new String(key, lastSeparatorIndex + 1, size - lastSeparatorIndex - 1));
	this.selector = CharOperation.subarray(key, 0, lastSeparatorIndex);
}
/**
 * Method declaration entries are encoded as 'methodDecl/' selector '/' Arity
 * e.g. 'methodDecl/X/0'
 *
 * Method reference entries are encoded as 'methodRef/' selector '/' Arity
 * e.g. 'methodRef/X/0'
 */
public char[] encodeIndexKey() {
	// will have a common pattern in the new story
	if (isCaseSensitive() && this.selector != null) {
		switch(matchMode()) {
			case EXACT_MATCH :
				int arity = this.parameterCount;
				if (arity >= 0) {
					char[] countChars = arity < 10 ? COUNTS[arity] : ("/" + String.valueOf(arity)).toCharArray(); //$NON-NLS-1$
					return CharOperation.concat(this.selector, countChars);
				}
			case PREFIX_MATCH :
				return this.selector;
			case PATTERN_MATCH :
				int starPos = CharOperation.indexOf('*', this.selector);
				switch(starPos) {
					case -1 :
						return this.selector;
					default : 
						char[] result = new char[starPos];
						System.arraycopy(this.selector, 0, result, 0, starPos);
						return result;
					case 0 : // fall through
				}
		}
	}
	return CharOperation.NO_CHAR; // find them all
}
public SearchPattern getIndexRecord() {
	return getMethodRecord();
}
public char[][] getMatchCategories() {
	if (this.findReferences)
		if (this.findDeclarations) 
			return new char[][] {METHOD_REF, METHOD_DECL};
		else
			return new char[][] {METHOD_REF};
	else
		if (this.findDeclarations)
			return new char[][] {METHOD_DECL};
		else
			return CharOperation.NO_CHAR_CHAR;
}
public boolean isPolymorphicSearch() {
	return this.findReferences;
}
public boolean isMatchingIndexRecord() {
	MethodPattern record = getMethodRecord();
	if (this.parameterCount != -1 && this.parameterCount != record.parameterCount) return false;

	return matchesName(this.selector, record.selector);
}
/**
 * Returns whether a method declaration or message send must be resolved to 
 * find out if this method pattern matches it.
 */
protected boolean mustResolve() {
	// declaring type 
	// If declaring type is specified - even with simple name - always resolves 
	// (see MethodPattern.matchLevel)
	if (declaringSimpleName != null || declaringQualification != null) return true;

	// return type
	// If return type is specified - even with simple name - always resolves 
	// (see MethodPattern.matchLevel)
	if (returnSimpleName != null || returnQualification != null) return true;

	// parameter types
	if (parameterSimpleNames != null)
		for (int i = 0, max = parameterSimpleNames.length; i < max; i++)
			if (parameterQualifications[i] != null) return true;
	return false;
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
	switch(matchMode()) {
		case EXACT_MATCH : 
			buffer.append("exact match, "); //$NON-NLS-1$
			break;
		case PREFIX_MATCH :
			buffer.append("prefix match, "); //$NON-NLS-1$
			break;
		case PATTERN_MATCH :
			buffer.append("pattern match, "); //$NON-NLS-1$
			break;
	}
	buffer.append(isCaseSensitive() ? "case sensitive" : "case insensitive"); //$NON-NLS-1$ //$NON-NLS-2$
	return buffer.toString();
}
}
