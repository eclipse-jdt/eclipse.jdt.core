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
package org.eclipse.jdt.internal.core.search.pattern;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.index.IIndex;
import org.eclipse.jdt.internal.core.index.impl.*;
import org.eclipse.jdt.internal.core.search.*;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * TODO add spec
 */
public abstract class InternalSearchPattern {

	public final int kind;
	public final int matchRule;
	
	/* focus element (used for reference patterns*/
	public IJavaElement focus;

	public InternalSearchPattern(int patternKind, int matchRule) {
		this.kind = patternKind;
		this.matchRule = matchRule;
	}
	
	/*
	 * @see SearchPattern
	 */
	public abstract void decodeIndexKey(char[] key);

	/*
	 * @see SearchPattern
	 */
	public abstract char[] encodeIndexKey();

	protected char[] encodeIndexKey(char[] key) {
		// TODO (kent) with new index, need to encode key for case insensitive queries too
		// also want to pass along the entire pattern
		if (isCaseSensitive() && key != null) {
			switch(matchMode()) {
				case SearchPattern.R_EXACT_MATCH :
				case  SearchPattern.R_PREFIX_MATCH :
					return key;
				case  SearchPattern.R_PATTERN_MATCH :
					int starPos = CharOperation.indexOf('*', key);
					switch(starPos) {
						case -1 :
							return key;
						default : 
							char[] result = new char[starPos];
							System.arraycopy(key, 0, result, 0, starPos);
							return result;
						case 0 : // fall through
					}
					break;
				case  SearchPattern.R_REGEXP_MATCH:
					// TODO (jerome) implement
					return key;
			}
		}
		return CharOperation.NO_CHAR; // find them all
	}

	/**
	 * Query a given index for matching entries. 
	 */
	public void findIndexMatches(IIndex index, IndexQueryRequestor requestor, SearchParticipant participant, IJavaSearchScope scope, IProgressMonitor progressMonitor) throws IOException {
	
		if (progressMonitor != null && progressMonitor.isCanceled()) throw new OperationCanceledException();
		IndexInput input = new BlocksIndexInput(index.getIndexFile());
		try {
			input.open();
			findIndexMatches(input, requestor, participant, scope, progressMonitor);
		} finally {
			input.close();
		}
	}
	
	/**
	 * Query a given index for matching entries. 
	 *
	 */
	public void findIndexMatches(IndexInput input, IndexQueryRequestor requestor, SearchParticipant participant, IJavaSearchScope scope, IProgressMonitor progressMonitor) throws IOException {
	
		char[][] categories = getMatchCategories();
		char[] queryKey = encodeIndexKey();
		for (int iCategory = 0, categoriesLength = categories.length; iCategory < categoriesLength; iCategory++) {
			if (progressMonitor != null && progressMonitor.isCanceled()) throw new OperationCanceledException();

			char[] category = categories[iCategory];
			findIndexMatches(input, requestor, participant, scope, progressMonitor, queryKey, category);
		}
	}
	
	protected void findIndexMatches(IndexInput input, IndexQueryRequestor requestor, SearchParticipant participant, IJavaSearchScope scope, IProgressMonitor progressMonitor, char[] queryKey, char[] category) throws IOException {
		/* narrow down a set of entries using prefix criteria */
		// TODO per construction the queryKey will always be the most specific prefix. This should evolve to be the search pattern directly, using proper match rule
		// ideally the index query API should be defined to avoid the need for concatenating the category to the key
		char[] pattern = CharOperation.concat(category, queryKey);
		EntryResult[] entries = input.queryEntries(pattern, SearchPattern.R_PREFIX_MATCH);
		if (entries == null) return;

		/* only select entries which actually match the entire search pattern */
		for (int iMatch = 0, matchesLength = entries.length; iMatch < matchesLength; iMatch++) {
			if (progressMonitor != null && progressMonitor.isCanceled()) throw new OperationCanceledException();

			/* retrieve and decode entry */	
			EntryResult entry = entries[iMatch];
			char[] word = entry.getWord();
			char[] indexKey = CharOperation.subarray(word, category.length, word.length);
			SearchPattern indexRecord = getIndexRecord();
			indexRecord.decodeIndexKey(indexKey);
			if (isMatchingIndexRecord()) {
				int[] references = entry.getFileReferences();
				for (int iReference = 0, refererencesLength = references.length; iReference < refererencesLength; iReference++) {
					String documentPath = IndexedFile.convertPath( input.getIndexedFile(references[iReference]).getPath());
					if (scope.encloses(documentPath)) {
						if (!requestor.acceptIndexMatch(documentPath, indexRecord, participant)) 
							throw new OperationCanceledException();
					}
				}
			}
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
		if (monitor != null) {
			monitor.beginTask(Util.bind("engine.searching"), 100); //$NON-NLS-1$
		}

		if (SearchEngine.VERBOSE) {
			System.out.println("Searching for " + this + " in " + scope); //$NON-NLS-1$//$NON-NLS-2$
		}
	
		IndexManager indexManager = JavaModelManager.getJavaModelManager().getIndexManager();
		try {
			requestor.beginReporting();
			
			for (int iParticipant = 0, length = participants == null ? 0 : participants.length; iParticipant < length; iParticipant++) {
				
				if (monitor != null && monitor.isCanceled()) throw new OperationCanceledException();
	
				SearchParticipant participant = participants[iParticipant];
				try {
					participant.beginSearching();
					requestor.enterParticipant(participant);
		
					// find index matches			
					PathCollector pathCollector = new PathCollector();
					indexManager.performConcurrentJob(
						new PatternSearchJob(
							(SearchPattern)this, 
							participant,
							scope, 
							pathCollector),
						IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
						monitor);
					if (monitor != null && monitor.isCanceled()) throw new OperationCanceledException();
		
					// locate index matches if any (note that all search matches could have been issued during index querying)
					String[] indexMatchPaths = pathCollector.getPaths();
					pathCollector = null; // release
					int indexMatchLength = indexMatchPaths == null ? 0 : indexMatchPaths.length;
					SearchDocument[] indexMatches = new SearchDocument[indexMatchLength];
					for (int iMatch = 0;iMatch < indexMatchLength; iMatch++) {
						String documentPath = indexMatchPaths[iMatch];
						indexMatches[iMatch] = participant.getDocument(documentPath);
					}
					participant.locateMatches(indexMatches, (SearchPattern)this, scope, requestor, monitor);
				} finally {		
					requestor.exitParticipant(participant);
					participant.doneSearching();
				}

				if (monitor != null && monitor.isCanceled()) throw new OperationCanceledException();
			}
		} finally {
			requestor.endReporting();
			if (monitor != null) monitor.done();
		}
	}			

	public abstract SearchPattern getIndexRecord();
	
	public abstract char[][] getMatchCategories();

	public boolean isCaseSensitive() {
		return (this.matchRule & SearchPattern.R_CASE_SENSITIVE) != 0;
	}
	
	public abstract boolean isMatchingIndexRecord();

	/*
	 * Returns whether this pattern is a polymorphic search pattern.
	 */
	public boolean isPolymorphicSearch() {
		return false;
	}

	/*
	 * One of R_EXACT_MATCH, R_PATTERN_MATCH, R_PREFIX_MATCH or R_REGEDP_MATCH
	 */
	public int matchMode() {
		return this.matchRule & ~SearchPattern.R_CASE_SENSITIVE;
	}

}
