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

public class TypeReferencePattern extends AndPattern {

protected char[] qualification;
protected char[] simpleName;

protected char[] decodedSimpleName;
protected char[] currentTag;

/* Optimization: case where simpleName == null */
protected char[][] segments;
protected int currentSegment;
protected char[] decodedSegment;

protected static char[][] TAGS = { TYPE_REF, SUPER_REF, REF, CONSTRUCTOR_REF };
protected static char[][] REF_TAGS = { REF };

public static char[] createReference(char[] typeName) {
	return CharOperation.concat(TYPE_REF, typeName);
}


public TypeReferencePattern(char[] qualification, char[] simpleName, int matchMode, boolean isCaseSensitive) {
	super(TYPE_REF_PATTERN, matchMode, isCaseSensitive);

	this.qualification = isCaseSensitive ? qualification : CharOperation.toLowerCase(qualification);
	this.simpleName = isCaseSensitive ? simpleName : CharOperation.toLowerCase(simpleName);

	if (simpleName == null)
		this.segments = this.qualification == null ? ONE_STAR_CHAR : CharOperation.splitOn('.', this.qualification);
	
	this.mustResolve = true; // always resolve (in case of a simple name reference being a potential match)
}
protected void acceptPath(IIndexSearchRequestor requestor, String path) {
	requestor.acceptTypeReference(path, decodedSimpleName);
}
/**
 * Either decode ref/name, typeRef/name or superRef/superName/name
 */ 
protected void decodeIndexEntry(IEntryResult entryResult) {
	char[] word = entryResult.getWord();
	int tagLength = currentTag.length;
	int nameLength = CharOperation.indexOf(SEPARATOR, word, tagLength);
	if (this.simpleName == null)
		// Optimization, eg. type reference is 'org.eclipse.jdt.core.*'
		this.decodedSegment = CharOperation.subarray(word, tagLength, nameLength);
	else
		this.decodedSimpleName = CharOperation.subarray(word, tagLength, nameLength);
}
public void findIndexMatches(IndexInput input, IIndexSearchRequestor requestor, int detailLevel, IProgressMonitor progressMonitor, IJavaSearchScope scope) throws IOException {

	if (progressMonitor != null && progressMonitor.isCanceled()) throw new OperationCanceledException();

	char[][] possibleTags = this.simpleName == null ? REF_TAGS : TAGS;
	for (int i = 0, max = possibleTags.length; i < max; i++) {
		currentTag = possibleTags[i];
		super.findIndexMatches(input, requestor, detailLevel, progressMonitor, scope);
	}
}
/**
 * @see AndPattern#hasNextQuery
 */
protected boolean hasNextQuery() {
	if (this.simpleName != null) return false;

	// Optimization, eg. type reference is 'org.eclipse.jdt.core.*'
	// if package has at least 4 segments, don't look at the first 2 since they are mostly
	// redundant (eg. in 'org.eclipse.jdt.core.*' 'org.eclipse' is used all the time)
	return --this.currentSegment >= (this.segments.length >= 4 ? 2 : 0);
}
/**
 * Type reference entries are encoded as 'ref/typeName' or 'current tag/typeName'
 */
protected char[] indexEntryPrefix() {
	if (this.simpleName == null) // Optimization, eg. type reference is 'org.eclipse.jdt.core.*'
		return indexEntryPrefix(REF, this.segments[this.currentSegment]);

	return indexEntryPrefix(this.currentTag, this.simpleName);
}
/**
 * @see SearchPattern#matchIndexEntry
 */
protected boolean matchIndexEntry() {
	if (simpleName == null) {
		// Optimization, eg. type reference is 'org.eclipse.jdt.core.*'
		switch(matchMode) {
			case EXACT_MATCH :
				return CharOperation.equals(this.segments[this.currentSegment], this.decodedSegment, isCaseSensitive);
			case PREFIX_MATCH :
				return CharOperation.prefixEquals(this.segments[this.currentSegment], this.decodedSegment, isCaseSensitive);
			case PATTERN_MATCH :
				return CharOperation.match(this.segments[this.currentSegment], this.decodedSegment, isCaseSensitive);
		}
	} else {
		switch(matchMode) {
			case EXACT_MATCH :
				return CharOperation.equals(simpleName, decodedSimpleName, isCaseSensitive);
			case PREFIX_MATCH :
				return CharOperation.prefixEquals(simpleName, decodedSimpleName, isCaseSensitive);
			case PATTERN_MATCH :
				return CharOperation.match(simpleName, decodedSimpleName, isCaseSensitive);
		}
	}
	return true;
}
/**
 * @see AndPattern#resetQuery
 */
protected void resetQuery() {
	/* walk the segments from end to start as it will find less potential references using 'lang' than 'java' */
	if (this.simpleName == null)
		this.currentSegment = this.segments.length - 1;
}
public String toString() {
	StringBuffer buffer = new StringBuffer(20);
	buffer.append("TypeReferencePattern: pkg<"); //$NON-NLS-1$
	if (qualification != null) buffer.append(qualification);
	buffer.append(">, type<"); //$NON-NLS-1$
	if (simpleName != null) buffer.append(simpleName);
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
