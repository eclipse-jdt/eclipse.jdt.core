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

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.core.search.indexing.IIndexConstants;

public class TypeReferencePattern extends AndPattern implements IIndexConstants {

protected char[] qualification;
protected char[] simpleName;

protected char[] currentCategory;

/* Optimization: case where simpleName == null */
protected char[][] segments;
protected int currentSegment;

protected static char[][] CATEGORIES = { REF };

public TypeReferencePattern(char[] qualification, char[] simpleName, int matchRule) {
	this(matchRule);

	this.qualification = isCaseSensitive() ? qualification : CharOperation.toLowerCase(qualification);
	this.simpleName = isCaseSensitive() ? simpleName : CharOperation.toLowerCase(simpleName);

	if (simpleName == null)
		this.segments = this.qualification == null ? ONE_STAR_CHAR : CharOperation.splitOn('.', this.qualification);
	else
		this.segments = null;

	((InternalSearchPattern)this).mustResolve = true; // always resolve (in case of a simple name reference being a potential match)
}
TypeReferencePattern(int matchRule) {
	super(TYPE_REF_PATTERN, matchRule);
}
public void decodeIndexKey(char[] key) {
	this.simpleName = key;
}
public SearchPattern getBlankPattern() {
	return new TypeReferencePattern(R_EXACT_MATCH | R_CASE_SENSITIVE);
}
public char[] getIndexKey() {
	if (this.simpleName != null)
		return this.simpleName;

	// Optimization, eg. type reference is 'org.eclipse.jdt.core.*'
	if (this.currentSegment >= 0) 
		return this.segments[this.currentSegment];
	return null;
}
public char[][] getMatchCategories() {
	return CATEGORIES;
}
protected boolean hasNextQuery() {
	if (this.segments == null) return false;

	// Optimization, eg. type reference is 'org.eclipse.jdt.core.*'
	// if package has at least 4 segments, don't look at the first 2 since they are mostly
	// redundant (eg. in 'org.eclipse.jdt.core.*' 'org.eclipse' is used all the time)
	return --this.currentSegment >= (this.segments.length >= 4 ? 2 : 0);
}
public boolean matchesDecodedKey(SearchPattern decodedPattern) {
	return true; // index key is not encoded so query results all match
}
protected void resetQuery() {
	/* walk the segments from end to start as it will find less potential references using 'lang' than 'java' */
	if (this.segments != null)
		this.currentSegment = this.segments.length - 1;
}
public String toString() {
	StringBuffer buffer = new StringBuffer(20);
	buffer.append("TypeReferencePattern: qualification<"); //$NON-NLS-1$
	if (qualification != null) 
		buffer.append(qualification);
	else
		buffer.append("*"); //$NON-NLS-1$
	buffer.append(">, type<"); //$NON-NLS-1$
	if (simpleName != null) 
		buffer.append(simpleName);
	else
		buffer.append("*"); //$NON-NLS-1$
	buffer.append(">, "); //$NON-NLS-1$
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
	if (isCaseSensitive())
		buffer.append("case sensitive"); //$NON-NLS-1$
	else
		buffer.append("case insensitive"); //$NON-NLS-1$
	return buffer.toString();
}
}
