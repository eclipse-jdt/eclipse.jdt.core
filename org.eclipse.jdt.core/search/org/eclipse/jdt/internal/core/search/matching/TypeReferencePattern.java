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

import java.io.IOException;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.core.index.*;

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
	return encodeIndexKey(typeName, R_EXACT_MATCH);
}

public TypeReferencePattern(char[] qualification, char[] simpleName, int matchRule) {
	this(matchRule);

	this.qualification = this.isCaseSensitive ? qualification : CharOperation.toLowerCase(qualification);
	this.simpleName = this.isCaseSensitive ? simpleName : CharOperation.toLowerCase(simpleName);

	if (simpleName == null)
		this.segments = this.qualification == null ? ONE_STAR_CHAR : CharOperation.splitOn('.', this.qualification);
	else
		this.segments = null;
	
	this.mustResolve = true; // always resolve (in case of a simple name reference being a potential match)
}
TypeReferencePattern(int matchRule) {
	super(TYPE_REF_PATTERN, matchRule);
}
public void decodeIndexKey(char[] key) {
	int nameLength = CharOperation.indexOf(SEPARATOR, key);
	if (nameLength != -1)
		key = CharOperation.subarray(key, 0, nameLength);

	this.simpleName = key; // decode into the simple name, see matchesDecodedPattern()
}
public SearchPattern getBlankPattern() {
	return new TypeReferencePattern(R_EXACT_MATCH | R_CASE_SENSITIVE);
}
public char[] getIndexKey() {
	if (this.simpleName != null)
		return encodeIndexKey(this.simpleName, this.matchMode);

	// Optimization, eg. type reference is 'org.eclipse.jdt.core.*'
	if (this.currentSegment >= 0) 
		return encodeIndexKey(this.segments[this.currentSegment], this.matchMode);
	return null;
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
public boolean matchesDecodedKey(SearchPattern decodedPattern) {
	return matchesName(
		this.simpleName != null ? this.simpleName : this.segments[this.currentSegment],
		((TypeReferencePattern) decodedPattern).simpleName);
}
public EntryResult[] queryIn(Index index) throws IOException {
	int matchRule = getMatchRule();
	if (this.simpleName != null && this.matchMode == R_EXACT_MATCH)
		matchRule = matchRule - R_EXACT_MATCH + R_PREFIX_MATCH; // must do a prefix match in SUPER_REF & CONSTRUCTOR_REF

	return index.query(getMatchCategories(), getIndexKey(), matchRule);
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
	if (this.isCaseSensitive)
		buffer.append("case sensitive"); //$NON-NLS-1$
	else
		buffer.append("case insensitive"); //$NON-NLS-1$
	return buffer.toString();
}
}
