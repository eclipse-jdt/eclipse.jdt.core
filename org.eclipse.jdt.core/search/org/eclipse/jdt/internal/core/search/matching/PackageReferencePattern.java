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

public class PackageReferencePattern extends AndPattern implements IIndexConstants {

protected char[] pkgName;

protected char[][] segments;
protected int currentSegment;

protected static char[][] CATEGORIES = { REF };

public PackageReferencePattern(char[] pkgName, int matchRule) {
	this(matchRule);

	if (pkgName == null || pkgName.length == 0) {
		this.pkgName = null;
		this.segments = new char[][] {CharOperation.NO_CHAR};
		((InternalSearchPattern)this).mustResolve = false;
	} else {
		this.pkgName = isCaseSensitive() ? pkgName : CharOperation.toLowerCase(pkgName);
		this.segments = CharOperation.splitOn('.', this.pkgName);
		((InternalSearchPattern)this).mustResolve = true;
	}
}
PackageReferencePattern(int matchRule) {
	super(PKG_REF_PATTERN, matchRule);
}
public void decodeIndexKey(char[] key) {
	// Package reference keys are encoded as 'name' (where 'name' is the last segment of the package name)
	this.pkgName = key;
}
public SearchPattern getBlankPattern() {
	return new PackageReferencePattern(R_EXACT_MATCH | R_CASE_SENSITIVE);
}
public char[] getIndexKey() {
	// Package reference keys are encoded as 'name' (where 'name' is the last segment of the package name)
	if (this.currentSegment >= 0) 
		return encodeIndexKey(this.segments[this.currentSegment], getMatchMode());
	return null;
}
public char[][] getMatchCategories() {
	return CATEGORIES;
}
protected boolean hasNextQuery() {
	// if package has at least 4 segments, don't look at the first 2 since they are mostly
	// redundant (eg. in 'org.eclipse.jdt.core.*' 'org.eclipse' is used all the time)
	return --this.currentSegment >= (this.segments.length >= 4 ? 2 : 0);
}
public boolean matchesDecodedKey(SearchPattern decodedPattern) {
	return true; // index key is not encoded so query results all match
}
protected void resetQuery() {
	/* walk the segments from end to start as it will find less potential references using 'lang' than 'java' */
	this.currentSegment = this.segments.length - 1;
}
public String toString() {
	StringBuffer buffer = new StringBuffer(20);
	buffer.append("PackageReferencePattern: <"); //$NON-NLS-1$
	if (this.pkgName != null) 
		buffer.append(this.pkgName);
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
		case R_REGEXP_MATCH :
			buffer.append("regexp match, "); //$NON-NLS-1$
	}
	if (isCaseSensitive())
		buffer.append("case sensitive"); //$NON-NLS-1$
	else
		buffer.append("case insensitive"); //$NON-NLS-1$
	return buffer.toString();
}
}
