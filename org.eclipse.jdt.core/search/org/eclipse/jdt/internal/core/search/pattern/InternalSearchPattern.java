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
package org.eclipse.jdt.internal.core.search.pattern;

import java.io.IOException;

import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.index.*;
import org.eclipse.jdt.internal.core.search.*;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * TODO add spec
 */
public abstract class InternalSearchPattern {

public boolean mustResolve = true;

protected void acceptMatch(String documentName, SearchPattern pattern, IndexQueryRequestor requestor, SearchParticipant participant, IJavaSearchScope scope) {
	String documentPath = Index.convertPath(documentName);
	if (scope.encloses(documentPath))
		if (!requestor.acceptIndexMatch(documentPath, pattern, participant)) 
			throw new OperationCanceledException();
}
protected SearchPattern currentPattern() {
	return (SearchPattern) this;
}
/**
 * Query a given index for matching entries. Assumes the sender has opened the index and will close when finished.
 */
public void findIndexMatches(Index index, IndexQueryRequestor requestor, SearchParticipant participant, IJavaSearchScope scope, IProgressMonitor monitor) throws IOException {
	if (monitor != null && monitor.isCanceled()) throw new OperationCanceledException();
	try {
		index.startQuery();
		SearchPattern pattern = currentPattern();
		EntryResult[] entries = pattern.queryIn(index);
		if (entries == null) return;
	
		SearchPattern decodedResult = pattern.getBlankPattern();
		for (int i = 0, l = entries.length; i < l; i++) {
			if (monitor != null && monitor.isCanceled()) throw new OperationCanceledException();
	
			EntryResult entry = entries[i];
			decodedResult.decodeIndexKey(entry.getWord());
			if (pattern.matchesDecodedKey(decodedResult)) {
				String[] names = entry.getDocumentNames(index);
				for (int j = 0, n = names.length; j < n; j++)
					acceptMatch(names[j], decodedResult, requestor, participant, scope);
			}
		}
	} finally {
		index.stopQuery();
	}
}
/**
 * Searches for matches to a given query. Search queries can be created using helper
 * methods (from a String pattern or a Java element) and encapsulate the description of what is
 * being searched (for example, search method declarations in a case sensitive way).
 *
 * @param scope the search result has to be limited to the given scope
 * @param resultCollector a callback object to which each match is reported
 */
public void findMatches(SearchParticipant[] participants, IJavaSearchScope scope, SearchRequestor requestor, IProgressMonitor monitor) throws CoreException {
	if (monitor != null && monitor.isCanceled()) throw new OperationCanceledException();

	/* initialize progress monitor */
	if (monitor != null)
		monitor.beginTask(Util.bind("engine.searching"), 100); //$NON-NLS-1$
	if (SearchEngine.VERBOSE)
		System.out.println("Searching for " + this + " in " + scope); //$NON-NLS-1$//$NON-NLS-2$

	IndexManager indexManager = JavaModelManager.getJavaModelManager().getIndexManager();
	try {
		requestor.beginReporting();
		for (int i = 0, l = participants == null ? 0 : participants.length; i < l; i++) {
			if (monitor != null && monitor.isCanceled()) throw new OperationCanceledException();

			SearchParticipant participant = participants[i];
			try {
				participant.beginSearching();
				requestor.enterParticipant(participant);
				PathCollector pathCollector = new PathCollector();
				indexManager.performConcurrentJob(
					new PatternSearchJob((SearchPattern) this, participant, scope, pathCollector),
					IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
					monitor);
				if (monitor != null && monitor.isCanceled()) throw new OperationCanceledException();

				// locate index matches if any (note that all search matches could have been issued during index querying)
				String[] indexMatchPaths = pathCollector.getPaths();
				pathCollector = null; // release
				int indexMatchLength = indexMatchPaths == null ? 0 : indexMatchPaths.length;
				SearchDocument[] indexMatches = new SearchDocument[indexMatchLength];
				for (int j = 0; j < indexMatchLength; j++)
					indexMatches[j] = participant.getDocument(indexMatchPaths[j]);
				participant.locateMatches(indexMatches, (SearchPattern) this, scope, requestor, monitor);
			} finally {		
				requestor.exitParticipant(participant);
				participant.doneSearching();
			}
		}
	} finally {
		requestor.endReporting();
		if (monitor != null)
			monitor.done();
	}
}
public boolean isPolymorphicSearch() {
	return false;
}
public EntryResult[] queryIn(Index index) throws IOException {
	SearchPattern pattern = (SearchPattern) this;
	return index.query(pattern.getMatchCategories(), pattern.getIndexKey(), pattern.getMatchRule());
}
}
