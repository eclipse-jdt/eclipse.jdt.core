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

import java.io.*;
import java.util.HashMap;

import org.eclipse.jdt.internal.core.index.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.core.search.indexing.IIndexConstants;
import org.eclipse.jdt.internal.core.index.impl.IndexInput;
import org.eclipse.jdt.internal.core.search.*;

public class SuperTypeReferencePattern extends SearchPattern {

public char[] superQualification;
public char[] superSimpleName;

protected char[] decodedSuperQualification;
protected char[] decodedSuperSimpleName;
protected char decodedSuperClassOrInterface;
protected char[] decodedQualification;
protected char[] decodedSimpleName;
protected char[] decodedEnclosingTypeName;
protected char decodedClassOrInterface;
protected int decodedModifiers;

protected boolean checkOnlySuperinterfaces; // used for IMPLEMENTORS

/**
 * A map from IndexInputs to IEntryResult[]
 */
public HashMap entryResults;

private static final IEntryResult[] NO_ENTRY_RESULT = new IEntryResult[0];

public static char[] createReference(
	int modifiers,
	char[] packageName,
	char[] typeName,
	char[][] enclosingTypeNames,
	char classOrInterface,
	char[] superTypeName,
	char superClassOrInterface) {

	if (superTypeName == null)
		superTypeName = OBJECT;
	char[] enclosingTypeName = CharOperation.concatWith(enclosingTypeNames, '$');
	char[] typeSimpleName = CharOperation.lastSegment(typeName, '.');
	char[] superTypeSimpleName = CharOperation.lastSegment(superTypeName, '.');
	char[] superQualification = null;
	if (superTypeSimpleName != superTypeName) {
		int length = superTypeName.length - superTypeSimpleName.length - 1;
		superQualification = new char[length];
		System.arraycopy(superTypeName, 0, superQualification, 0, length);
	}

	// if the supertype name contains a $, then split it into: source name and append the $ prefix to the qualification
	//	e.g. p.A$B ---> p.A$ + B
	char[] superTypeSourceName = CharOperation.lastSegment(superTypeSimpleName, '$');
	if (superTypeSourceName != superTypeSimpleName) {
		int start = superQualification == null ? 0 : superQualification.length + 1;
		int prefixLength = superTypeSimpleName.length - superTypeSourceName.length;
		char[] mangledQualification = new char[start + prefixLength];
		if (superQualification != null) {
			System.arraycopy(superQualification, 0, mangledQualification, 0, start-1);
			mangledQualification[start-1] = '.';
		}
		System.arraycopy(superTypeSimpleName, 0, mangledQualification, start, prefixLength);
		superQualification = mangledQualification;
		superTypeSimpleName = superTypeSourceName;
	} 

	int superTypeSimpleNameLength = superTypeSimpleName == null ? 0 : superTypeSimpleName.length;
	int superQualificationLength = superQualification == null ? 0 : superQualification.length;
	int typeSimpleNameLength = typeSimpleName == null ? 0 : typeSimpleName.length;
	int enclosingTypeNameLength = enclosingTypeName == null ? 0 : enclosingTypeName.length;
	int packageNameLength = packageName == null ? 0 : packageName.length;
	int pos = SUPER_REF.length;

	// SUPER_REF superTypeSimpleName / superQualification / superClassOrInterface /  typeSimpleName / enclosingTypeName / packageName / classOrInterface modifiers
	char[] result = new char[pos + superTypeSimpleNameLength + superQualificationLength + typeSimpleNameLength
		+ enclosingTypeNameLength + packageNameLength + 9];
	System.arraycopy(SUPER_REF, 0, result, 0, pos);
	if (superTypeSimpleNameLength > 0) {
		System.arraycopy(superTypeSimpleName, 0, result, pos, superTypeSimpleNameLength);
		pos += superTypeSimpleNameLength;
	}
	result[pos++] = SEPARATOR;
	if (superQualificationLength > 0) {
		System.arraycopy(superQualification, 0, result, pos, superQualificationLength);
		pos += superQualificationLength;
	}
	result[pos++] = SEPARATOR;
	result[pos++] = superClassOrInterface;
	result[pos++] = SEPARATOR;
	if (typeSimpleNameLength > 0) {
		System.arraycopy(typeSimpleName, 0, result, pos, typeSimpleNameLength);
		pos += typeSimpleNameLength;
	}
	result[pos++] = SEPARATOR;
	if (enclosingTypeNameLength > 0) {
		System.arraycopy(enclosingTypeName, 0, result, pos, enclosingTypeNameLength);
		pos += enclosingTypeNameLength;
	}
	result[pos++] = SEPARATOR;
	if (packageNameLength > 0) {
		System.arraycopy(packageName, 0, result, pos, packageNameLength);
		pos += packageNameLength;
	}
	result[pos++] = SEPARATOR;
	result[pos++] = classOrInterface;
	result[pos] = (char) modifiers;
	return result;
}


public SuperTypeReferencePattern(char[] superQualification, char[] superSimpleName, int matchMode, boolean isCaseSensitive) {
	this(superQualification, superSimpleName, matchMode, isCaseSensitive, false);
}
public SuperTypeReferencePattern(
	char[] superQualification,
	char[] superSimpleName,
	int matchMode,
	boolean isCaseSensitive,
	boolean checkOnlySuperinterfaces) {

	super(SUPER_REF_PATTERN, matchMode, isCaseSensitive);

	this.superQualification = isCaseSensitive ? superQualification : CharOperation.toLowerCase(superQualification);
	this.superSimpleName = isCaseSensitive ? superSimpleName : CharOperation.toLowerCase(superSimpleName);
	this.mustResolve = superQualification != null;
	this.checkOnlySuperinterfaces = checkOnlySuperinterfaces; // ie. skip the superclass
}
protected void acceptPath(IIndexSearchRequestor requestor, String path) {
	requestor.acceptSuperTypeReference(path, decodedQualification, decodedSimpleName, decodedEnclosingTypeName, decodedClassOrInterface, decodedSuperQualification, decodedSuperSimpleName, decodedSuperClassOrInterface, decodedModifiers);
}
/*
 * "superRef/Object/java.lang/X/p" represents "class p.X extends java.lang.Object"
 * "superRef/Exception//X/p" represents "class p.X extends Exception"
 */
protected void decodeIndexEntry(IEntryResult entryResult){
	char[] word = entryResult.getWord();
	int slash = SUPER_REF.length - 1;
	this.decodedSuperSimpleName = CharOperation.subarray(word, slash + 1, slash = CharOperation.indexOf(SEPARATOR, word, slash + 1));
	int oldSlash = slash;
	slash = CharOperation.indexOf(SEPARATOR, word, slash + 1);
	this.decodedSuperQualification = (slash == oldSlash + 1)
		? null // could not have been known at index time
		: CharOperation.subarray(word, oldSlash + 1, slash);
	this.decodedSuperClassOrInterface = word[slash + 1];
	slash += 2;
	this.decodedSimpleName = CharOperation.subarray(word, slash + 1, slash = CharOperation.indexOf(SEPARATOR, word, slash + 1));
	oldSlash = slash;
	slash = CharOperation.indexOf(SEPARATOR, word, slash + 1);
	if (slash == oldSlash + 1) { // could not have been known at index time
		this.decodedEnclosingTypeName = null;
	} else {
		this.decodedEnclosingTypeName = (slash == oldSlash + 2 && word[oldSlash + 1] == ONE_ZERO[0])
			? ONE_ZERO
			: CharOperation.subarray(word, oldSlash + 1, slash);
	}
	oldSlash = slash;
	slash = CharOperation.indexOf(SEPARATOR, word, slash + 1);
	this.decodedQualification = (slash == oldSlash + 1)
		? null // could not have been known at index time
		: CharOperation.subarray(word, oldSlash + 1, slash);
	this.decodedClassOrInterface = word[slash + 1];
	this.decodedModifiers = word[slash + 2]; // implicit cast to int type
}
/**
 * Query a given index for matching entries. 
 */
public void findIndexMatches(IndexInput input, IIndexSearchRequestor requestor, int detailLevel, IProgressMonitor progressMonitor, IJavaSearchScope scope) throws IOException {
	if (this.entryResults == null) {
		// non-optimized case
		super.findIndexMatches(input, requestor, detailLevel, progressMonitor, scope);	
		return;
	}

	if (progressMonitor != null && progressMonitor.isCanceled()) throw new OperationCanceledException();

	/* narrow down a set of entries using prefix criteria */
	IEntryResult[] entries = (IEntryResult[]) this.entryResults.get(input);
	if (entries == null) {
		entries = input.queryEntriesPrefixedBy(SUPER_REF);
		if (entries == null)
			entries = NO_ENTRY_RESULT;
		this.entryResults.put(input, entries);
	}
	if (entries == NO_ENTRY_RESULT) return;

	/* only select entries which actually match the entire search pattern */
	int slash = SUPER_REF.length;
	char[] simpleName = this.superSimpleName;
	int length = simpleName == null ? 0 : simpleName.length;
	nextEntry: for (int i = 0, max = entries.length; i < max; i++) {
		/* check that the entry is a super ref to the super simple name */
		IEntryResult entry = entries[i];
		if (simpleName != null) {
			char[] word = entry.getWord();
			if (slash + length >= word.length) continue;
			
			// ensure it is the end of the ref (a simple name is not a prefix of ref)
			if (word[length + slash] != '/') continue; 
			
			// compare ref to simple name
			for (int j = 0; j < length; j++)
				if (word[j + slash] != simpleName[j]) continue nextEntry;
		}

		/* retrieve and decode entry */	
		decodeIndexEntry(entry);
		feedIndexRequestor(requestor, detailLevel, entry.getFileReferences(), input, scope);
	}
}
/**
 * Package reference entries are encoded as 'superRef/typeName'
 */
protected char[] indexEntryPrefix() {
	return indexEntryPrefix(SUPER_REF, this.superSimpleName);
}
/**
 * @see SearchPattern#matchIndexEntry
 */
protected boolean matchIndexEntry() {
	if (this.checkOnlySuperinterfaces)
		if (this.decodedSuperClassOrInterface != IIndexConstants.INTERFACE_SUFFIX) return false;

	if (this.superSimpleName != null) {
		switch(matchMode) {
			case EXACT_MATCH :
				return CharOperation.equals(this.superSimpleName, this.decodedSuperSimpleName, this.isCaseSensitive);
			case PREFIX_MATCH :
				return CharOperation.prefixEquals(this.superSimpleName, this.decodedSuperSimpleName, this.isCaseSensitive);
			case PATTERN_MATCH :
				return CharOperation.match(this.superSimpleName, this.decodedSuperSimpleName, this.isCaseSensitive);
		}
	}
	return true;
}
public String toString(){
	StringBuffer buffer = new StringBuffer(20);
	buffer.append(
		this.checkOnlySuperinterfaces
			? "SuperInterfaceReferencePattern: <" //$NON-NLS-1$
			: "SuperTypeReferencePattern: <"); //$NON-NLS-1$
	if (superSimpleName != null) buffer.append(superSimpleName);
	buffer.append(">, "); //$NON-NLS-1$
	switch(matchMode){
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
	if (isCaseSensitive)
		buffer.append("case sensitive"); //$NON-NLS-1$
	else
		buffer.append("case insensitive"); //$NON-NLS-1$
	return buffer.toString();
}
}
