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
import org.eclipse.jdt.core.search.SearchPattern;

public class TypeReferencePattern extends AndPattern {

protected char[] qualification;
protected char[] simpleName;

protected char[] currentCategory;

/* Optimization: case where simpleName == null */
protected char[][] segments;
protected int currentSegment;

protected static char[][] CATEGORIES = { TYPE_REF, SUPER_REF, REF, CONSTRUCTOR_REF };
protected static char[][] REF_CATEGORIES = { REF };

public static char[] createIndexKey(char[] typeName) {
	return typeName != null ? typeName : CharOperation.NO_CHAR;
}

public TypeReferencePattern(char[] qualification, char[] simpleName, int matchRule) {
	super(TYPE_REF_PATTERN, matchRule);

	this.qualification = this.isCaseSensitive ? qualification : CharOperation.toLowerCase(qualification);
	this.simpleName = this.isCaseSensitive ? simpleName : CharOperation.toLowerCase(simpleName);

	if (simpleName == null)
		this.segments = this.qualification == null ? ONE_STAR_CHAR : CharOperation.splitOn('.', this.qualification);
	
	this.mustResolve = true; // always resolve (in case of a simple name reference being a potential match)
}
public void decodeIndexKey(char[] key) {
	int nameLength = CharOperation.indexOf(SEPARATOR, key);
	if (nameLength != -1)
		key = CharOperation.subarray(key, 0, nameLength);
	
	this.simpleName = key;

	// Optimization, eg. type reference is 'org.eclipse.jdt.core.*'
	this.segments[0] = key;
}
public char[] encodeIndexKey() {
	if (this.simpleName != null)
		return encodeIndexKey(this.simpleName);

	// Optimization, eg. type reference is 'org.eclipse.jdt.core.*'
	if (this.currentSegment >= 0) 
		return encodeIndexKey(this.segments[this.currentSegment]);
	return null;
}
public SearchPattern getBlankPattern() {
	return new TypeReferencePattern(null, null, R_EXACT_MATCH | R_CASE_SENSITIVE);
}
public char[][] getMatchCategories() {
	return this.simpleName == null ? REF_CATEGORIES : CATEGORIES;
}
protected boolean hasNextQuery() {
	if (this.simpleName != null) return false;

	// Optimization, eg. type reference is 'org.eclipse.jdt.core.*'
	// if package has at least 4 segments, don't look at the first 2 since they are mostly
	// redundant (eg. in 'org.eclipse.jdt.core.*' 'org.eclipse' is used all the time)
	return --this.currentSegment >= (this.segments.length >= 4 ? 2 : 0);
}
public boolean matchesDecodedPattern(SearchPattern decodedPattern) {
	if (this.simpleName != null)
		return matchesName(this.simpleName, ((TypeReferencePattern) decodedPattern).simpleName);

	// Optimization, eg. type reference is 'org.eclipse.jdt.core.*'
	return matchesName(this.segments[this.currentSegment], ((TypeReferencePattern) decodedPattern).segments[0]);
}
protected void resetQuery() {
	/* walk the segments from end to start as it will find less potential references using 'lang' than 'java' */
	if (this.simpleName == null)
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
	switch(this.matchMode) {
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
	if (this.isCaseSensitive)
		buffer.append("case sensitive"); //$NON-NLS-1$
	else
		buffer.append("case insensitive"); //$NON-NLS-1$
	return buffer.toString();
}
}
