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
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.core.index.IEntryResult;
import org.eclipse.jdt.internal.core.index.impl.IndexInput;
import org.eclipse.jdt.internal.core.search.IIndexSearchRequestor;

public class FieldPattern extends SearchPattern {

protected boolean findDeclarations;
protected boolean findReferences;
protected boolean readAccess;
protected boolean writeAccess;

protected char[] name;
	
// declaring type
protected char[] declaringQualification;
protected char[] declaringSimpleName;

// type
protected char[] typeQualification;
protected char[] typeSimpleName;

protected char[] decodedName;
protected char[] currentTag;

protected static char[][] REF_TAGS = { FIELD_REF, REF };
protected static char[][] REF_AND_DECL_TAGS = { FIELD_REF, REF, FIELD_DECL };
protected static char[][] DECL_TAGS = { FIELD_DECL };

public static char[] createDeclaration(char[] fieldName) {
	return CharOperation.concat(FIELD_DECL, fieldName);
}
public static char[] createReference(char[] fieldName) {
	return CharOperation.concat(FIELD_REF, fieldName);
}


public FieldPattern(
	boolean findDeclarations,
	boolean readAccess,
	boolean writeAccess,
	char[] name, 
	int matchMode, 
	boolean isCaseSensitive,
	char[] declaringQualification,
	char[] declaringSimpleName,	
	char[] typeQualification, 
	char[] typeSimpleName) {

	super(FIELD_PATTERN, matchMode, isCaseSensitive);

	this.findDeclarations = findDeclarations; // set to find declarations & all occurences
	this.readAccess = readAccess; // set to find any reference, read only references & all occurences
	this.writeAccess = writeAccess; // set to find any reference, write only references & all occurences
	this.findReferences = readAccess || writeAccess;

	this.name = isCaseSensitive ? name : CharOperation.toLowerCase(name);
	this.declaringQualification = isCaseSensitive ? declaringQualification : CharOperation.toLowerCase(declaringQualification);
	this.declaringSimpleName = isCaseSensitive ? declaringSimpleName : CharOperation.toLowerCase(declaringSimpleName);
	this.typeQualification = isCaseSensitive ? typeQualification : CharOperation.toLowerCase(typeQualification);
	this.typeSimpleName = isCaseSensitive ? typeSimpleName : CharOperation.toLowerCase(typeSimpleName);

	this.mustResolve = mustResolve();
}
protected void acceptPath(IIndexSearchRequestor requestor, String path) {
	if (this.currentTag ==  FIELD_DECL && this.findDeclarations)
		requestor.acceptFieldDeclaration(path, this.decodedName);
	else
		requestor.acceptFieldReference(path, this.decodedName);
}
protected void decodeIndexEntry(IEntryResult entryResult) {
	this.decodedName = CharOperation.subarray(entryResult.getWord(), this.currentTag.length, -1);
}
public void findIndexMatches(IndexInput input, IIndexSearchRequestor requestor, int detailLevel, IProgressMonitor progressMonitor, IJavaSearchScope scope) throws IOException {
	if (progressMonitor != null && progressMonitor.isCanceled()) throw new OperationCanceledException();

	// in the new story this will be a single call with a mask
	char[][] possibleTags =
		this.findReferences
			? (this.findDeclarations || this.writeAccess ? REF_AND_DECL_TAGS : REF_TAGS)
			: DECL_TAGS;
	for (int i = 0, max = possibleTags.length; i < max; i++) {
		this.currentTag = possibleTags[i];
		super.findIndexMatches(input, requestor, detailLevel, progressMonitor, scope);
	}
}
/**
 * Field declaration entries are encoded as 'fieldDecl/fieldName'
 *
 * Field reference entries are encoded as 'fieldRef/fieldName'
 */
protected char[] indexEntryPrefix() {
	// will have a common pattern in the new story
	return indexEntryPrefix(this.currentTag, this.name);
}
/**
 * @see SearchPattern#matchIndexEntry
 */
protected boolean matchIndexEntry() {
	if (this.name != null) {
		switch(this.matchMode) {
			case EXACT_MATCH :
				return CharOperation.equals(this.name, this.decodedName, this.isCaseSensitive);
			case PREFIX_MATCH :
				return CharOperation.prefixEquals(this.name, this.decodedName, this.isCaseSensitive);
			case PATTERN_MATCH :
				return CharOperation.match(this.name, this.decodedName, this.isCaseSensitive);
		}
	}
	return true;
}
/**
 * Returns whether a method declaration or message send will need to be resolved to 
 * find out if this method pattern matches it.
 */
protected boolean mustResolve() {
	// would like to change this so that we only do it if generic references are found
	if (this.findReferences) return true; // always resolve (in case of a simple name reference being a potential match)

	// declaring type
	if (this.declaringSimpleName != null || this.declaringQualification != null) return true;

	// return type
	if (this.typeSimpleName != null || this.typeQualification != null) return true;

	return false;
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
	buffer.append(isCaseSensitive ? "case sensitive" : "case insensitive"); //$NON-NLS-1$ //$NON-NLS-2$
	return buffer.toString();
}
}
