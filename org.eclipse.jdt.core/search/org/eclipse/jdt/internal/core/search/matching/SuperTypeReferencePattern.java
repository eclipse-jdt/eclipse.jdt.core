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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.core.search.indexing.IIndexConstants;
import org.eclipse.jdt.internal.core.index.impl.*;
import org.eclipse.jdt.internal.core.search.*;

public class SuperTypeReferencePattern extends SearchPattern {

private static ThreadLocal indexRecord = new ThreadLocal() {
	protected Object initialValue() {
		return new SuperTypeReferencePattern(null, null, false, R_EXACT_MATCH | R_CASE_SENSITIVE);
	}
};

public char[] superQualification;
public char[] superSimpleName;
public char superClassOrInterface;

public char[] pkgName;
public char[] simpleName;
public char[] enclosingTypeName;
public char classOrInterface;
public int modifiers;

protected boolean checkOnlySuperinterfaces; // used for IMPLEMENTORS

/**
 * A map from IndexInputs to IEntryResult[]
 */
public HashMap entryResults;

private static final EntryResult[] NO_ENTRY_RESULT = new EntryResult[0];

public static char[] createIndexKey(
	int modifiers,
	char[] packageName,
	char[] typeName,
	char[][] enclosingTypeNames,
	char classOrInterface,
	char[] superTypeName,
	char superClassOrInterface) {

	SuperTypeReferencePattern record = getSuperTypeReferenceRecord();
	record.modifiers = modifiers;
	record.pkgName = packageName;
	record.classOrInterface = classOrInterface;
	record.superClassOrInterface = superClassOrInterface;
	if (superTypeName == null)
		superTypeName = OBJECT;
	record.enclosingTypeName = CharOperation.concatWith(enclosingTypeNames, '$');
	record.simpleName = CharOperation.lastSegment(typeName, '.');
	record.superSimpleName = CharOperation.lastSegment(superTypeName, '.');
	record.superQualification = null;
	if (record.superSimpleName != superTypeName) {
		int length = superTypeName.length - record.superSimpleName.length - 1;
		record.superQualification = new char[length];
		System.arraycopy(superTypeName, 0, record.superQualification, 0, length);
	}

	// if the supertype name contains a $, then split it into: source name and append the $ prefix to the qualification
	//	e.g. p.A$B ---> p.A$ + B
	char[] superTypeSourceName = CharOperation.lastSegment(record.superSimpleName, '$');
	if (superTypeSourceName != record.superSimpleName) {
		int start = record.superQualification == null ? 0 : record.superQualification.length + 1;
		int prefixLength = record.superSimpleName.length - superTypeSourceName.length;
		char[] mangledQualification = new char[start + prefixLength];
		if (record.superQualification != null) {
			System.arraycopy(record.superQualification, 0, mangledQualification, 0, start-1);
			mangledQualification[start-1] = '.';
		}
		System.arraycopy(record.superSimpleName, 0, mangledQualification, start, prefixLength);
		record.superQualification = mangledQualification;
		record.superSimpleName = superTypeSourceName;
	} 
	
	return record.encodeIndexKey();
}
public static SuperTypeReferencePattern getSuperTypeReferenceRecord() {
	return (SuperTypeReferencePattern)indexRecord.get();
}
public SuperTypeReferencePattern(char[] superQualification, char[] superSimpleName, int matchRule) {
	this(superQualification, superSimpleName, false, matchRule);
}
public SuperTypeReferencePattern(
	char[] superQualification,
	char[] superSimpleName,
	boolean checkOnlySuperinterfaces,
	int matchRule) {

	super(SUPER_REF_PATTERN, matchRule);

	boolean isCaseSensitive = isCaseSensitive();
	this.superQualification = isCaseSensitive ? superQualification : CharOperation.toLowerCase(superQualification);
	this.superSimpleName = isCaseSensitive ? superSimpleName : CharOperation.toLowerCase(superSimpleName);
	this.mustResolve = superQualification != null;
	this.checkOnlySuperinterfaces = checkOnlySuperinterfaces; // ie. skip the superclass
}
/*
 * superSimpleName / superQualification / superClassOrInterface /  simpleName / enclosingTypeName / pkgName / classOrInterface modifiers
 */
public void decodeIndexKey(char[] key) {
	int slash = -1;
	this.superSimpleName = CharOperation.subarray(key, slash + 1, slash = CharOperation.indexOf(SEPARATOR, key, slash + 1));
	int oldSlash = slash;
	slash = CharOperation.indexOf(SEPARATOR, key, slash + 1);
	this.superQualification = (slash == oldSlash + 1)
		? null // could not have been known at index time
		: CharOperation.subarray(key, oldSlash + 1, slash);
	this.superClassOrInterface = key[slash + 1];
	slash += 2;
	this.simpleName = CharOperation.subarray(key, slash + 1, slash = CharOperation.indexOf(SEPARATOR, key, slash + 1));
	oldSlash = slash;
	slash = CharOperation.indexOf(SEPARATOR, key, slash + 1);
	if (slash == oldSlash + 1) { // could not have been known at index time
		this.enclosingTypeName = null;
	} else {
		this.enclosingTypeName = (slash == oldSlash + 2 && key[oldSlash + 1] == ONE_ZERO[0])
			? ONE_ZERO
			: CharOperation.subarray(key, oldSlash + 1, slash);
	}
	oldSlash = slash;
	slash = CharOperation.indexOf(SEPARATOR, key, slash + 1);
	this.pkgName = (slash == oldSlash + 1)
		? null // could not have been known at index time
		: CharOperation.subarray(key, oldSlash + 1, slash);
	this.classOrInterface = key[slash + 1];
	this.modifiers = key[slash + 2]; // implicit cast to int type
}
/*
 * superSimpleName / superQualification / superClassOrInterface /  simpleName / enclosingTypeName / pkgName / classOrInterface modifiers
 */
public char[] encodeIndexKey() {
	int superSimpleNameLength = this.superSimpleName == null ? 0 : this.superSimpleName.length;
	int superQualificationLength = this.superQualification == null ? 0 : this.superQualification.length;
	int simpleNameLength = this.simpleName == null ? 0 : this.simpleName.length;
	int enclosingTypeNameLength = this.enclosingTypeName == null ? 0 : this.enclosingTypeName.length;
	int pkgNameLength = this.pkgName == null ? 0 : this.pkgName.length;

	int length = superSimpleNameLength + superQualificationLength + simpleNameLength
		+ enclosingTypeNameLength + pkgNameLength + 9;
	char[] result = new char[length];
	int pos = 0;
	if (superSimpleNameLength > 0) {
		System.arraycopy(this.superSimpleName, 0, result, pos, superSimpleNameLength);
		pos += superSimpleNameLength;
	}
	result[pos++] = SEPARATOR;
	if (this.superClassOrInterface != 0) { // 0 when querying index
		if (superQualificationLength > 0) {
			System.arraycopy(this.superQualification, 0, result, pos, superQualificationLength);
			pos += superQualificationLength;
		}
		result[pos++] = SEPARATOR;
		result[pos++] = this.superClassOrInterface;
		result[pos++] = SEPARATOR;
		if (simpleNameLength > 0) {
			System.arraycopy(this.simpleName, 0, result, pos, simpleNameLength);
			pos += simpleNameLength;
		}
		result[pos++] = SEPARATOR;
		if (enclosingTypeNameLength > 0) {
			System.arraycopy(this.enclosingTypeName, 0, result, pos, enclosingTypeNameLength);
			pos += enclosingTypeNameLength;
		}
		result[pos++] = SEPARATOR;
		if (pkgNameLength > 0) {
			System.arraycopy(this.pkgName, 0, result, pos, pkgNameLength);
			pos += pkgNameLength;
		}
		result[pos++] = SEPARATOR;
		result[pos++] = this.classOrInterface;
		result[pos++] = (char) this.modifiers;
	}
	if (pos != length) {
		System.arraycopy(result, 0, result = new char[pos], 0, pos);
	}
	return result;
}
/**
 * Query a given index for matching entries. 
 */
public void findIndexMatches(IndexInput input, IndexQueryRequestor requestor, SearchParticipant participant, IJavaSearchScope scope, IProgressMonitor progressMonitor) throws IOException {
	if (this.entryResults == null) {
		// non-optimized case
		super.findIndexMatches(input, requestor, participant, scope, progressMonitor);	
		return;
	}

	if (progressMonitor != null && progressMonitor.isCanceled()) throw new OperationCanceledException();

	/* narrow down a set of entries using prefix criteria */
	EntryResult[] entries = (EntryResult[]) this.entryResults.get(input);
	if (entries == null) {
		entries = input.queryEntriesPrefixedBy(SUPER_REF);
		if (entries == null)
			entries = NO_ENTRY_RESULT;
		this.entryResults.put(input, entries);
	}
	if (entries == NO_ENTRY_RESULT) return;

	/* only select entries which actually match the entire search pattern */
	int slash = SUPER_REF.length;
	char[] name = this.superSimpleName;
	int length = name == null ? 0 : name.length;
	nextEntry: for (int i = 0, max = entries.length; i < max; i++) {
		/* check that the entry is a super ref to the super simple name */
		EntryResult entry = entries[i];
		if (name != null) {
			char[] word = entry.getWord();
			if (slash + length >= word.length) continue;
			
			// ensure it is the end of the ref (a simple name is not a prefix of ref)
			if (word[length + slash] != '/') continue; 
			
			// compare ref to simple name
			for (int j = 0; j < length; j++)
				if (word[j + slash] != name[j]) continue nextEntry;
		}

		/* retrieve and decode entry */	
		char[] word = entry.getWord();
		char[] indexKey = CharOperation.subarray(word, SUPER_REF.length, word.length);
		SearchPattern record = getIndexRecord();
		record.decodeIndexKey(indexKey);

		int[] references = entry.getFileReferences();
		for (int iReference = 0, refererencesLength = references.length; iReference < refererencesLength; iReference++) {
			String documentPath = IndexedFile.convertPath( input.getIndexedFile(references[iReference]).getPath());
			if (scope.encloses(documentPath)) {
				if (!requestor.acceptIndexMatch(documentPath, record, participant)) 
					throw new OperationCanceledException();
			}
		}
	}
}
public SearchPattern getIndexRecord() {
	return getSuperTypeReferenceRecord();
}
public char[][] getMatchCategories() {
	return new char[][] {SUPER_REF};
}
public boolean isMatchingIndexRecord() {
	SuperTypeReferencePattern record = getSuperTypeReferenceRecord();
	if (this.checkOnlySuperinterfaces)
		if (record.superClassOrInterface != IIndexConstants.INTERFACE_SUFFIX) return false;

	return matchesName(this.superSimpleName, record.superSimpleName);
}
public String toString(){
	StringBuffer buffer = new StringBuffer(20);
	buffer.append(
		this.checkOnlySuperinterfaces
			? "SuperInterfaceReferencePattern: <" //$NON-NLS-1$
			: "SuperTypeReferencePattern: <"); //$NON-NLS-1$
	if (superSimpleName != null) 
		buffer.append(superSimpleName);
	else
		buffer.append("*"); //$NON-NLS-1$
	buffer.append(">, "); //$NON-NLS-1$
	switch(matchMode()){
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
	if (isCaseSensitive())
		buffer.append("case sensitive"); //$NON-NLS-1$
	else
		buffer.append("case insensitive"); //$NON-NLS-1$
	return buffer.toString();
}
}
