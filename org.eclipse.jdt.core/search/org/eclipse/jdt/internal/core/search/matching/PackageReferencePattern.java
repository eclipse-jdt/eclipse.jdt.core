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
import org.eclipse.jdt.core.search.*;

public class PackageReferencePattern extends AndPattern {

private static ThreadLocal indexRecord = new ThreadLocal() {
	protected Object initialValue() {
		return new PackageReferencePattern("*".toCharArray(), R_EXACT_MATCH | R_CASE_SENSITIVE); //$NON-NLS-1$;
	}
};

protected char[] pkgName;

protected char[][] segments;
protected int currentSegment;

public static PackageReferencePattern getPackageReferenceRecord() {
	return (PackageReferencePattern)indexRecord.get();
}
	
public PackageReferencePattern(char[] pkgName, int matchRule) {
	super(PKG_REF_PATTERN, matchRule);

	if (pkgName == null || pkgName.length == 0) {
		this.pkgName = null;
		this.segments = new char[][] {CharOperation.NO_CHAR};
		this.mustResolve = false;
	} else {
		this.pkgName = isCaseSensitive() ? pkgName : CharOperation.toLowerCase(pkgName);
		this.segments = CharOperation.splitOn('.', this.pkgName);
		this.mustResolve = true;
	}
}
public void decodeIndexKey(char[] key) {
	// Package reference keys are encoded as 'name' (where 'name' is the last segment of the package name)
	this.segments[0] = key;
}
public char[] encodeIndexKey() {
	if (this.currentSegment < 0) return null;
	// Package reference keys are encoded as 'name' (where 'name' is the last segment of the package name)
	return encodeIndexKey(this.segments[this.currentSegment]);
}
public SearchPattern getIndexRecord() {
	return getPackageReferenceRecord();
}
public char[][] getMatchCategories() {
	return new char[][] {REF};
}
/*
 * @see AndPattern#hasNextQuery()
 */
protected boolean hasNextQuery() {
	// if package has at least 4 segments, don't look at the first 2 since they are mostly
	// redundant (eg. in 'org.eclipse.jdt.core.*' 'org.eclipse' is used all the time)
	return --this.currentSegment >= (this.segments.length >= 4 ? 2 : 0);
}
/*
 * @see SearchPattern#isMatchingIndexRecord()
 */
public boolean isMatchingIndexRecord() {
	return matchesName(this.segments[this.currentSegment], ((PackageReferencePattern)getIndexRecord()).segments[0]);
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
	if (this.pkgName != null) 
		buffer.append(this.pkgName);
	else
		buffer.append("*"); //$NON-NLS-1$
	buffer.append(">, "); //$NON-NLS-1$
	switch(matchMode()){
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
