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

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.core.index.IEntryResult;
import org.eclipse.jdt.internal.core.index.impl.IndexInput;
import org.eclipse.jdt.internal.core.search.IIndexSearchRequestor;

public class ConstructorPattern extends SearchPattern {

protected boolean findDeclarations;
protected boolean findReferences;

protected char[] declaringQualification;
protected char[] declaringSimpleName;

protected char[][] parameterQualifications;
protected char[][] parameterSimpleNames;

protected char[] decodedTypeName;
protected int decodedParameterCount;

// extra reference info
protected IType declaringType;

protected char[] currentTag;

public static char[] createDeclaration(char[] typeName, int argCount) {
	char[] countChars = argCount < 10 ? COUNTS[argCount] : ("/" + String.valueOf(argCount)).toCharArray(); //$NON-NLS-1$
	return CharOperation.concat(CONSTRUCTOR_DECL, typeName, countChars);
}
public static char[] createReference(char[] typeName, int argCount) {
	char[] countChars = argCount < 10 ? COUNTS[argCount] : ("/" + String.valueOf(argCount)).toCharArray(); //$NON-NLS-1$
	return CharOperation.concat(CONSTRUCTOR_REF, typeName, countChars);
}


public ConstructorPattern(
	boolean findDeclarations,
	boolean findReferences,
	char[] declaringSimpleName,
	int matchMode,
	boolean isCaseSensitive,
	char[] declaringQualification,
	char[][] parameterQualifications,
	char[][] parameterSimpleNames,
	IType declaringType) {

	super(CONSTRUCTOR_PATTERN, matchMode, isCaseSensitive);

	this.findDeclarations = findDeclarations;
	this.findReferences = findReferences;

	this.declaringQualification = isCaseSensitive ? declaringQualification : CharOperation.toLowerCase(declaringQualification);
	this.declaringSimpleName = isCaseSensitive ? declaringSimpleName : CharOperation.toLowerCase(declaringSimpleName);
	if (parameterSimpleNames != null) {
		this.parameterQualifications = new char[parameterSimpleNames.length][];
		this.parameterSimpleNames = new char[parameterSimpleNames.length][];
		for (int i = 0, max = parameterSimpleNames.length; i < max; i++) {
			this.parameterQualifications[i] = isCaseSensitive ? parameterQualifications[i] : CharOperation.toLowerCase(parameterQualifications[i]);
			this.parameterSimpleNames[i] = isCaseSensitive ? parameterSimpleNames[i] : CharOperation.toLowerCase(parameterSimpleNames[i]);
		}
	}

	this.declaringType = declaringType;
	this.mustResolve = mustResolve();
}
protected void acceptPath(IIndexSearchRequestor requestor, String path) {
	if (this.currentTag ==  CONSTRUCTOR_REF)
		requestor.acceptConstructorReference(path, this.decodedTypeName, this.decodedParameterCount);
	else
		requestor.acceptConstructorDeclaration(path, this.decodedTypeName, this.decodedParameterCount);
}
protected void decodeIndexEntry(IEntryResult entryResult){
	char[] word = entryResult.getWord();
	int size = word.length;
	int lastSeparatorIndex = CharOperation.lastIndexOf(SEPARATOR, word);	

	this.decodedParameterCount = Integer.parseInt(new String(word, lastSeparatorIndex + 1, size - lastSeparatorIndex - 1));
	this.decodedTypeName = CharOperation.subarray(word, currentTag.length, lastSeparatorIndex);
}
public void findIndexMatches(IndexInput input, IIndexSearchRequestor requestor, int detailLevel, IProgressMonitor progressMonitor, IJavaSearchScope scope) throws IOException {
	if (progressMonitor != null && progressMonitor.isCanceled()) throw new OperationCanceledException();

	// in the new story this will be a single call with a mask
	if (this.findReferences) {
		this.currentTag = CONSTRUCTOR_REF;
		super.findIndexMatches(input, requestor, detailLevel, progressMonitor, scope);
	}
	if (this.findDeclarations) {
		this.currentTag = CONSTRUCTOR_DECL;
		super.findIndexMatches(input, requestor, detailLevel, progressMonitor, scope);
	}
}
/**
 * Constructor declaration entries are encoded as 'constructorDecl/' TypeName '/' Arity:
 * e.g. 'constructorDecl/X/0'
 *
 * Constructor reference entries are encoded as 'constructorRef/' TypeName '/' Arity:
 * e.g. 'constructorRef/X/0'
 */
protected char[] indexEntryPrefix() {
	// will have a common pattern in the new story
	if (this.isCaseSensitive && this.declaringSimpleName != null) {
		switch(this.matchMode) {
			case EXACT_MATCH :
				int arity = this.parameterSimpleNames == null ? -1 : this.parameterSimpleNames.length;
				if (arity >= 0) {
					char[] countChars = arity < 10 ? COUNTS[arity] : ("/" + String.valueOf(arity)).toCharArray(); //$NON-NLS-1$
					return CharOperation.concat(this.currentTag, this.declaringSimpleName, countChars);
				}
			case PREFIX_MATCH :
				return CharOperation.concat(this.currentTag, this.declaringSimpleName);
			case PATTERN_MATCH :
				int starPos = CharOperation.indexOf('*', this.declaringSimpleName);
				switch(starPos) {
					case -1 :
						return CharOperation.concat(this.currentTag, this.declaringSimpleName);
					default : 
						int length = this.currentTag.length;
						char[] result = new char[length + starPos];
						System.arraycopy(this.currentTag, 0, result, 0, length);
						System.arraycopy(this.declaringSimpleName, 0, result, length, starPos);
						return result;
					case 0 : // fall through
				}
		}
	}
	return this.currentTag; // find them all
}
/**
 * @see SearchPattern#matchIndexEntry
 */
protected boolean matchIndexEntry() {
	if (parameterSimpleNames != null && parameterSimpleNames.length != decodedParameterCount) return false;

	if (declaringSimpleName != null) {
		switch(matchMode) {
			case EXACT_MATCH :
				return CharOperation.equals(declaringSimpleName, decodedTypeName, isCaseSensitive);
			case PREFIX_MATCH :
				return CharOperation.prefixEquals(declaringSimpleName, decodedTypeName, isCaseSensitive);
			case PATTERN_MATCH :
				return CharOperation.match(declaringSimpleName, decodedTypeName, isCaseSensitive);
		}
	}
	return true;
}
protected boolean mustResolve() {
	if (this.declaringQualification != null) return true;

	// parameter types
	if (this.parameterSimpleNames != null)
		for (int i = 0, max = this.parameterSimpleNames.length; i < max; i++)
			if (this.parameterQualifications[i] != null) return true;
	return false;
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
	switch(matchMode) {
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
	buffer.append(isCaseSensitive ? "case sensitive" : "case insensitive"); //$NON-NLS-1$ //$NON-NLS-2$
	return buffer.toString();
}
}