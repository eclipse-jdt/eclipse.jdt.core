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

import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.core.index.IEntryResult;
import org.eclipse.jdt.internal.core.index.impl.IndexInput;
import org.eclipse.jdt.internal.core.search.IIndexSearchRequestor;

public class MethodPattern extends SearchPattern {

protected boolean findDeclarations;
protected boolean findReferences;

protected char[] selector;

protected char[] declaringQualification;
protected char[] declaringSimpleName;

protected char[] returnQualification;
protected char[] returnSimpleName;

protected char[][] parameterQualifications;
protected char[][] parameterSimpleNames;

protected char[] decodedSelector;
protected int decodedParameterCount;

// extra reference info
public char[][][] allSuperDeclaringTypeNames;
protected IType declaringType;

protected char[] currentTag;

public static char[] createDeclaration(char[] selector, int argCount) {
	char[] countChars = argCount < 10 ? COUNTS[argCount] : ("/" + String.valueOf(argCount)).toCharArray(); //$NON-NLS-1$
	return CharOperation.concat(METHOD_DECL, selector, countChars);
}
public static char[] createReference(char[] selector, int argCount) {
	char[] countChars = argCount < 10 ? COUNTS[argCount] : ("/" + String.valueOf(argCount)).toCharArray(); //$NON-NLS-1$
	return CharOperation.concat(METHOD_REF, selector, countChars);
}


public MethodPattern(
	boolean findDeclarations,
	boolean findReferences,
	char[] selector, 
	int matchMode, 
	boolean isCaseSensitive,
	char[] declaringQualification,
	char[] declaringSimpleName,	
	char[] returnQualification, 
	char[] returnSimpleName,
	char[][] parameterQualifications, 
	char[][] parameterSimpleNames,
	IType declaringType) {

	super(METHOD_PATTERN, matchMode, isCaseSensitive);

	this.findDeclarations = findDeclarations;
	this.findReferences = findReferences;

	this.selector = isCaseSensitive ? selector : CharOperation.toLowerCase(selector);
	this.declaringQualification = isCaseSensitive ? declaringQualification : CharOperation.toLowerCase(declaringQualification);
	this.declaringSimpleName = isCaseSensitive ? declaringSimpleName : CharOperation.toLowerCase(declaringSimpleName);
	this.returnQualification = isCaseSensitive ? returnQualification : CharOperation.toLowerCase(returnQualification);
	this.returnSimpleName = isCaseSensitive ? returnSimpleName : CharOperation.toLowerCase(returnSimpleName);
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
	if (this.currentTag ==  METHOD_REF)
		requestor.acceptMethodReference(path, this.decodedSelector, this.decodedParameterCount);
	else
		requestor.acceptMethodDeclaration(path, this.decodedSelector, this.decodedParameterCount);
}
protected void decodeIndexEntry(IEntryResult entryResult){
	char[] word = entryResult.getWord();
	int size = word.length;
	int lastSeparatorIndex = CharOperation.lastIndexOf(SEPARATOR, word);	

	this.decodedParameterCount = Integer.parseInt(new String(word, lastSeparatorIndex + 1, size - lastSeparatorIndex - 1));
	this.decodedSelector = CharOperation.subarray(word, this.currentTag.length, lastSeparatorIndex);
}
public void findIndexMatches(IndexInput input, IIndexSearchRequestor requestor, int detailLevel, IProgressMonitor progressMonitor, IJavaSearchScope scope) throws IOException {
	if (progressMonitor != null && progressMonitor.isCanceled()) throw new OperationCanceledException();

	// in the new story this will be a single call with a mask
	if (this.findReferences) {
		this.currentTag = METHOD_REF;
		super.findIndexMatches(input, requestor, detailLevel, progressMonitor, scope);
	}
	if (this.findDeclarations) {
		this.currentTag = METHOD_DECL;
		super.findIndexMatches(input, requestor, detailLevel, progressMonitor, scope);
	}
}
/**
 * Method declaration entries are encoded as 'methodDecl/' selector '/' Arity
 * e.g. 'methodDecl/X/0'
 *
 * Method reference entries are encoded as 'methodRef/' selector '/' Arity
 * e.g. 'methodRef/X/0'
 */
protected char[] indexEntryPrefix() {
	// will have a common pattern in the new story
	if (this.isCaseSensitive && this.selector != null) {
		switch(this.matchMode) {
			case EXACT_MATCH :
				int arity = parameterSimpleNames == null ? -1 : parameterSimpleNames.length;
				if (arity >= 0) {
					char[] countChars = arity < 10 ? COUNTS[arity] : ("/" + String.valueOf(arity)).toCharArray(); //$NON-NLS-1$
					return CharOperation.concat(this.currentTag, this.selector, countChars);
				}
			case PREFIX_MATCH :
				return CharOperation.concat(this.currentTag, this.selector);
			case PATTERN_MATCH :
				int starPos = CharOperation.indexOf('*', this.selector);
				switch(starPos) {
					case -1 :
						return CharOperation.concat(this.currentTag, this.selector);
					default : 
						int length = this.currentTag.length;
						char[] result = new char[length + starPos];
						System.arraycopy(this.currentTag, 0, result, 0, length);
						System.arraycopy(this.selector, 0, result, length, starPos);
						return result;
					case 0 : // fall through
				}
		}
	}
	return this.currentTag; // find them all
}
public void initializePolymorphicSearch(MatchLocator locator, IProgressMonitor progressMonitor) {
	try {
		this.allSuperDeclaringTypeNames = 
			new SuperTypeNamesCollector(
				this, 
				this.declaringSimpleName,
				this.declaringQualification,
				locator,
				this.declaringType, 
				progressMonitor).collect();
	} catch (JavaModelException e) {
		// inaccurate matches will be found
	}
}
public boolean isPolymorphicSearch() {
	return this.findReferences;
}
/**
 * @see SearchPattern#matchIndexEntry
 */
protected boolean matchIndexEntry() {
	if (parameterSimpleNames != null && parameterSimpleNames.length != decodedParameterCount) return false;

	if (selector != null) {
		switch(matchMode) {
			case EXACT_MATCH :
				return CharOperation.equals(selector, decodedSelector, isCaseSensitive);
			case PREFIX_MATCH :
				return CharOperation.prefixEquals(selector, decodedSelector, isCaseSensitive);
			case PATTERN_MATCH :
				return CharOperation.match(selector, decodedSelector, isCaseSensitive);
		}
	}
	return true;
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
