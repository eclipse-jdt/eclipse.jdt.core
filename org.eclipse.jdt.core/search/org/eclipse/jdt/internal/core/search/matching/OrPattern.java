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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.core.index.Index;
import org.eclipse.jdt.internal.core.search.IndexQueryRequestor;

public class OrPattern extends SearchPattern {

protected SearchPattern[] patterns;

public OrPattern(SearchPattern leftPattern, SearchPattern rightPattern) {
	super(OR_PATTERN, Math.max(leftPattern.getMatchRule(), rightPattern.getMatchRule()));
	((InternalSearchPattern)this).mustResolve = ((InternalSearchPattern) leftPattern).mustResolve || ((InternalSearchPattern) rightPattern).mustResolve;

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
void findIndexMatches(Index index, IndexQueryRequestor requestor, SearchParticipant participant, IJavaSearchScope scope, IProgressMonitor progressMonitor) throws IOException {
	// per construction, OR pattern can only be used with a PathCollector (which already gather results using a set)
	try {
		index.startQuery();
		for (int i = 0, length = this.patterns.length; i < length; i++)
			((InternalSearchPattern)this.patterns[i]).findIndexMatches(index, requestor, participant, scope, progressMonitor);
	} finally {
		index.stopQuery();
	}
}
boolean isPolymorphicSearch() {
	for (int i = 0, length = this.patterns.length; i < length; i++)
		if (((InternalSearchPattern) this.patterns[i]).isPolymorphicSearch()) return true;
	return false;
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
