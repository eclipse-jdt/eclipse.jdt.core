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
import org.eclipse.jdt.internal.core.index.IEntryResult;
import org.eclipse.jdt.internal.core.search.IIndexSearchRequestor;

public class PackageReferencePattern extends AndPattern {

protected char[] pkgName;

protected char[][] segments;
protected int currentSegment;
protected char[] decodedSegment;
	
public PackageReferencePattern(char[] pkgName, int matchMode, boolean isCaseSensitive) {
	super(PKG_REF_PATTERN, matchMode, isCaseSensitive);

	this.pkgName = isCaseSensitive ? pkgName : CharOperation.toLowerCase(pkgName);
	this.segments = CharOperation.splitOn('.', this.pkgName);
	this.mustResolve = this.pkgName != null;
}
protected void acceptPath(IIndexSearchRequestor requestor, String path) {
	requestor.acceptPackageReference(path, this.pkgName);
}
/**
 * ref/name (where name is the last segment of the package name)
 */ 
protected void decodeIndexEntry(IEntryResult entryResult) {
	this.decodedSegment = CharOperation.subarray(entryResult.getWord(), REF.length, -1);
}
/**
 * @see AndPattern#hasNextQuery()
 */
protected boolean hasNextQuery() {
	// if package has at least 4 segments, don't look at the first 2 since they are mostly
	// redundant (eg. in 'org.eclipse.jdt.core.*' 'org.eclipse' is used all the time)
	return --this.currentSegment >= (this.segments.length >= 4 ? 2 : 0);
}
/**
 * Package reference entries are encoded as 'ref/packageName'
 */
protected char[] indexEntryPrefix() {
	return indexEntryPrefix(REF, this.segments[this.currentSegment]);
}
/**
 * @see SearchPattern#matchIndexEntry()
 */
protected boolean matchIndexEntry() {
	switch(matchMode) {
		case EXACT_MATCH :
			return CharOperation.equals(this.segments[this.currentSegment], this.decodedSegment, isCaseSensitive);
		case PREFIX_MATCH :
			return CharOperation.prefixEquals(this.segments[this.currentSegment], this.decodedSegment, isCaseSensitive);
		case PATTERN_MATCH :
			return CharOperation.match(this.segments[this.currentSegment], this.decodedSegment, isCaseSensitive);
	}
	return false;
}
/**
 * @see AndPattern#resetQuery()
 */
protected void resetQuery() {
	/* walk the segments from end to start as it will find less potential references using 'lang' than 'java' */
	this.currentSegment = this.segments.length - 1;
}
public String toString() {
	StringBuffer buffer = new StringBuffer(20);
	buffer.append("PackageReferencePattern: <"); //$NON-NLS-1$
	if (this.pkgName != null) buffer.append(this.pkgName);
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
