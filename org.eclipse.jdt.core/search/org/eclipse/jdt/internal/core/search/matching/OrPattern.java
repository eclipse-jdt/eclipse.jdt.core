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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.compiler.ast.AstNode;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.core.index.impl.IndexInput;
import org.eclipse.jdt.internal.core.search.IIndexSearchRequestor;
import org.eclipse.jdt.internal.core.search.IInfoConstants;

public class OrPattern extends SearchPattern {

protected SearchPattern[] patterns;
protected SearchPattern bestMatch;

public OrPattern(SearchPattern leftPattern, SearchPattern rightPattern) {
	super(
		Math.max(leftPattern.matchMode, rightPattern.matchMode),
		false); // not used

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
/**
 * see SearchPattern.findMatches
 */
public void findIndexMatches(IndexInput input, IIndexSearchRequestor requestor, int detailLevel, IProgressMonitor progressMonitor, IJavaSearchScope scope) throws IOException {
	IIndexSearchRequestor orCombiner = 
		detailLevel == IInfoConstants.NameInfo
			? (IIndexSearchRequestor) new OrNameCombiner(requestor)
			: (IIndexSearchRequestor) new OrPathCombiner(requestor);

	for (int i = 0, length = this.patterns.length; i < length; i++)
		this.patterns[i].findIndexMatches(input, orCombiner, detailLevel, progressMonitor, scope);
}
/**
 * see SearchPattern.initializePolymorphicSearch
 */
public void initializePolymorphicSearch(MatchLocator locator, IProgressMonitor progressMonitor) {
	for (int i = 0, length = this.patterns.length; i < length; i++)
		this.patterns[i].initializePolymorphicSearch(locator, progressMonitor);
}
/**
 * see SearchPattern.isPolymorphicSearch
 */
public boolean isPolymorphicSearch() {
	for (int i = 0, length = this.patterns.length; i < length; i++)
		if (this.patterns[i].isPolymorphicSearch()) return true;
	return false;
}
/**
 * @see SearchPattern#matchContainer()
 */
protected int matchContainer() {
	int result = 0;
	for (int i = 0, length = this.patterns.length; i < length; i++)
		result |= this.patterns[i].matchContainer();
	return result;
}
/**
 * @see SearchPattern#matchesBinary
 */
public boolean matchesBinary(Object binaryInfo, Object enclosingBinaryInfo) {
	for (int i = 0, length = this.patterns.length; i < length; i++)
		if (this.patterns[i].matchesBinary(binaryInfo, enclosingBinaryInfo)) return true;
	return false;
}
/**
 * @see SearchPattern#matchIndexEntry
 */
protected boolean matchIndexEntry() {
	for (int i = 0, length = this.patterns.length; i < length; i++)
		if (this.patterns[i].matchIndexEntry()) return true;
	return false;
}
/**
 * @see SearchPattern#matchLevel(AstNode, boolean)
 */
public int matchLevel(AstNode node, boolean resolve) {
	int level = IMPOSSIBLE_MATCH;
	for (int i = 0, length = this.patterns.length; i < length; i++) {
		int newLevel = this.patterns[i].matchLevel(node, resolve);
		if (newLevel > level) {
			if (newLevel == ACCURATE_MATCH) return ACCURATE_MATCH;
			level = newLevel; // want to answer the stronger match
		}
	}
	return level;
}
/**
 * @see SearchPattern#matchLevel(Binding)
 */
public int matchLevel(Binding binding) {
	this.bestMatch = null;
	int level = IMPOSSIBLE_MATCH;
	for (int i = 0, length = this.patterns.length; i < length; i++) {
		int newLevel = this.patterns[i].matchLevel(binding);
		if (newLevel > level) {
			this.bestMatch = this.patterns[i]; // cache the best match
			if (newLevel == ACCURATE_MATCH) return ACCURATE_MATCH;
			level = newLevel; // want to answer the stronger match
		}
	}
	return level;
}
/**
 * @see SearchPattern#matchReportReference
 */
protected void matchReportReference(AstNode reference, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	if (this.bestMatch != null) {
		this.bestMatch.matchReportReference(reference, element, accuracy, locator);
		return;
	}

	SearchPattern closestPattern = null;
	int level = IMPOSSIBLE_MATCH;
	for (int i = 0, length = this.patterns.length; i < length; i++) {
		int newLevel = this.patterns[i].matchLevel(reference, true);
		if (newLevel > level) {
			if (newLevel == ACCURATE_MATCH) {
				this.patterns[i].matchReportReference(reference, element, accuracy, locator);
				return;
			}
			level = newLevel;
			closestPattern = this.patterns[i];
		}
	}
	if (closestPattern != null)
		closestPattern.matchReportReference(reference, element, accuracy, locator);
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
