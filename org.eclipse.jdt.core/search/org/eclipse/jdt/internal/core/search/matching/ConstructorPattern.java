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

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.*;

public class ConstructorPattern extends SearchPattern {

private static ThreadLocal indexRecord = new ThreadLocal() {
	protected Object initialValue() {
		return new ConstructorPattern(false, false, null,null, null, null, R_EXACT_MATCH | R_CASE_SENSITIVE);
	}
};

protected boolean findDeclarations;
protected boolean findReferences;

public char[] declaringQualification;
public char[] declaringSimpleName;

public char[][] parameterQualifications;
public char[][] parameterSimpleNames;
public int parameterCount;

public static char[] createIndexKey(char[] typeName, int argCount) {
	ConstructorPattern record = getConstructorRecord();
	record.declaringSimpleName = typeName;
	record.parameterCount = argCount;
	return record.encodeIndexKey();
}
public static ConstructorPattern getConstructorRecord() {
	return (ConstructorPattern)indexRecord.get();
}
public ConstructorPattern(
	boolean findDeclarations,
	boolean findReferences,
	char[] declaringSimpleName,
	char[] declaringQualification,
	char[][] parameterQualifications,
	char[][] parameterSimpleNames,
	int matchRule) {

	super(CONSTRUCTOR_PATTERN, matchRule);

	this.findDeclarations = findDeclarations;
	this.findReferences = findReferences;

	boolean isCaseSensitive = isCaseSensitive();
	this.declaringQualification = isCaseSensitive ? declaringQualification : CharOperation.toLowerCase(declaringQualification);
	this.declaringSimpleName = isCaseSensitive ? declaringSimpleName : CharOperation.toLowerCase(declaringSimpleName);
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

	this.mustResolve = mustResolve();
}
public void decodeIndexKey(char[] key) {
	int size = key.length;
	int lastSeparatorIndex = CharOperation.lastIndexOf(SEPARATOR, key);	

	this.parameterCount = Integer.parseInt(new String(key, lastSeparatorIndex + 1, size - lastSeparatorIndex - 1));
	this.declaringSimpleName = CharOperation.subarray(key, 0, lastSeparatorIndex);
}
/**
 * Constructor declaration entries are encoded as 'constructorDecl/' TypeName '/' Arity:
 * e.g. 'constructorDecl/X/0'
 *
 * Constructor reference entries are encoded as 'constructorRef/' TypeName '/' Arity:
 * e.g. 'constructorRef/X/0'
 */
public char[] encodeIndexKey() {
	// will have a common pattern in the new story
	if (isCaseSensitive() && this.declaringSimpleName != null) {
		switch(matchMode()) {
			case EXACT_MATCH :
				int arity = this.parameterCount;
				if (arity >= 0) {
					char[] countChars = arity < 10 ? COUNTS[arity] : ("/" + String.valueOf(arity)).toCharArray(); //$NON-NLS-1$
					return CharOperation.concat(this.declaringSimpleName, countChars);
				}
			case PREFIX_MATCH :
				return this.declaringSimpleName;
			case PATTERN_MATCH :
				int starPos = CharOperation.indexOf('*', this.declaringSimpleName);
				switch(starPos) {
					case -1 :
						return this.declaringSimpleName;
					default : 
						char[] result = new char[starPos];
						System.arraycopy(this.declaringSimpleName, 0, result, 0, starPos);
						return result;
					case 0 : // fall through
				}
		}
	}
	return CharOperation.NO_CHAR; // find them all
}
public SearchPattern getIndexRecord() {
	return getConstructorRecord();
}
public char[][] getMatchCategories() {
	if (this.findReferences)
		if (this.findDeclarations) 
			return new char[][] {CONSTRUCTOR_REF, CONSTRUCTOR_DECL};
		else
			return new char[][] {CONSTRUCTOR_REF};
	else
		if (this.findDeclarations)
			return new char[][] {CONSTRUCTOR_DECL};
		else
			return CharOperation.NO_CHAR_CHAR;
}
public boolean isMatchingIndexRecord() {
	ConstructorPattern record = getConstructorRecord();
	if (this.parameterCount != -1 && this.parameterCount != record.parameterCount) return false;

	return matchesName(this.declaringSimpleName, record.declaringSimpleName);
}
protected boolean mustResolve() {
	if (this.declaringQualification != null) return true;

	// parameter types
	if (this.parameterSimpleNames != null)
		for (int i = 0, max = this.parameterSimpleNames.length; i < max; i++)
			if (this.parameterQualifications[i] != null) return true;
	return this.findReferences; // need to check resolved default constructors and explicit constructor calls
}
public String toString() {
	StringBuffer buffer = new StringBuffer(20);
	if (this.findDeclarations) {
		buffer.append(this.findReferences
			? "ConstructorCombinedPattern: " //$NON-NLS-1$
			: "ConstructorDeclarationPattern: "); //$NON-NLS-1$
	} else {
		buffer.append("ConstructorReferencePattern: "); //$NON-NLS-1$
	}
	if (declaringQualification != null)
		buffer.append(declaringQualification).append('.');
	if (declaringSimpleName != null) 
		buffer.append(declaringSimpleName);
	else if (declaringQualification != null)
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