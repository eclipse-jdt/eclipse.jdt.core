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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.internal.core.index.impl.IndexInput;
import org.eclipse.jdt.internal.core.search.IndexQueryRequestor;

public class OrPattern extends SearchPattern {

protected SearchPattern[] patterns;

public OrPattern(SearchPattern leftPattern, SearchPattern rightPattern) {
	super(OR_PATTERN, Math.max(leftPattern.matchRule, rightPattern.matchRule));
	this.mustResolve = leftPattern.mustResolve || rightPattern.mustResolve;

	SearchPattern[] leftPatterns = leftPattern instanceof OrPattern ? ((OrPattern) leftPattern).patterns : null;
	SearchPattern[] rightPatterns = rightPattern instanceof OrPattern ? ((OrPattern) rightPattern).patterns : null;
	int leftSize = leftPatterns == null ? 1 : leftPatterns.length;
	int rightSize = rightPatterns == null ? 1 : rightPatterns.length;
	this.patterns = new SearchPattern[leftSize + rightSize];

	if (leftPatterns == null)
		this.patterns[0] = leftPattern;
	else
		System.arraycopy(leftPatterns, 0, this.patterns, 0, leftSize);
	if (rightPatterns == null)
		this.patterns[leftSize] = rightPattern;
	else
		System.arraycopy(rightPatterns, 0, this.patterns, leftSize, rightSize);
}

public void decodeIndexKey(char[] key) {
	// not used for OrPattern
}

public char[] encodeIndexKey() {
	// not used for OrPattern
	return null;
}

/**
 * Query a given index for matching entries. 
 *
 */
public void findIndexMatches(IndexInput input, IndexQueryRequestor requestor, SearchParticipant participant, IJavaSearchScope scope, IProgressMonitor progressMonitor) throws IOException {
	// per construction, OR pattern can only be used with a PathCollector (which already gather results using a set)
	for (int i = 0, length = this.patterns.length; i < length; i++)
		this.patterns[i].findIndexMatches(input, requestor, participant, scope, progressMonitor);
}

public SearchPattern getIndexRecord() {
	// not used for OrPattern
	return null;
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.core.search.pattern.InternalSearchPattern#isMatchingIndexEntry()
 */
public boolean isMatchingIndexRecord() {
	return false;
}

/**
 * see SearchPattern.isPolymorphicSearch
 */
public boolean isPolymorphicSearch() {
	for (int i = 0, length = this.patterns.length; i < length; i++)
		if (this.patterns[i].isPolymorphicSearch()) return true;
	return false;
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.core.search.pattern.InternalSearchPattern#getMatchCategories()
 */
public char[][] getMatchCategories() {
	return CharOperation.NO_CHAR_CHAR;
}

public String toString() {
	StringBuffer buffer = new StringBuffer();
	buffer.append(this.patterns[0].toString());
	for (int i = 1, length = this.patterns.length; i < length; i++) {
		buffer.append("\n| "); //$NON-NLS-1$
		buffer.append(this.patterns[i].toString());
	}
	return buffer.toString();
}
}
