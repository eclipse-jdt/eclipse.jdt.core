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
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.internal.core.index.impl.EntryResult;
import org.eclipse.jdt.internal.core.index.impl.IndexInput;
import org.eclipse.jdt.internal.core.index.impl.IndexedFile;
import org.eclipse.jdt.internal.core.search.IndexQueryRequestor;

/**
 * Query the index multiple times and do an 'and' on the results.
 */
public abstract class AndPattern extends SearchPattern { // TODO should rename IntersectingPattern, and make AndPattern a true subclass
	
public AndPattern(int patternKind, int matchRule) {
	super(patternKind, matchRule);
}

/**
 * Query a given index for matching entries. 
 */
protected void findIndexMatches(IndexInput input, IndexQueryRequestor requestor, SearchParticipant participant, IJavaSearchScope scope, IProgressMonitor progressMonitor, char[] queryKey, char[] category) throws IOException {

	if (progressMonitor != null && progressMonitor.isCanceled()) throw new OperationCanceledException();
	
	/* narrow down a set of entries using prefix criteria */
	long[] possibleRefs = null;
	int maxRefs = -1;
	this.resetQuery();
	SearchPattern indexRecord = null;
	do {
		queryKey = encodeIndexKey();
		char[] pattern = CharOperation.concat(category, queryKey);
		EntryResult[] entries = input.queryEntries(pattern, SearchPattern.R_PREFIX_MATCH);
		if (entries == null) break;

		int numFiles = input.getNumFiles();
		long[] references = null;
		int referencesLength = -1;
		for (int i = 0, max = entries.length; i < max; i++) {
			if (progressMonitor != null && progressMonitor.isCanceled()) throw new OperationCanceledException();

			/* retrieve and decode entry */	
			EntryResult entry = entries[i];
			char[] word = entry.getWord();
			char[] indexKey = CharOperation.subarray(word, category.length, word.length);
			indexRecord = getIndexRecord();
			indexRecord.decodeIndexKey(indexKey);
			if (isMatchingIndexRecord()) {
				/* accumulate references in an array of bits : 1 if the reference is present, 0 otherwise */
				int[] fileReferences = entry.getFileReferences();
				for (int j = 0, refLength = fileReferences.length; j < refLength; j++) {
					int fileReference = fileReferences[j];
					int vectorIndex = fileReference / 64; // a long has 64 bits
					if (references == null) {
						referencesLength = (numFiles / 64) + 1;
						references = new long[referencesLength];
					}
					long mask = 1L << (fileReference % 64);
					references[vectorIndex] |= mask;
				}
			}
		}
		
		/* only select entries which actually match the entire search pattern */
		if (references == null) return;
		if (possibleRefs == null) {
			/* first query : these are the possible references */
			possibleRefs = references;
			maxRefs = numFiles;
		} else {
			/* eliminate possible references that don't match the current references */
			int possibleLength = possibleRefs.length;
			for (int i = 0, length = references.length; i < length; i++) {
				if (i < possibleLength)
					possibleRefs[i] &= references[i];
				else
					possibleRefs[i] = 0;
			}
			// check to see that there are still possible references after the merge
			while (--possibleLength >= 0 && possibleRefs[possibleLength] == 0);
			if (possibleLength == -1) return;
		}
	} while (this.hasNextQuery());

	/* report possible references that remain */
	if (possibleRefs != null) {
		int[] refs = new int[maxRefs];
		int refsLength = 0;
		for (int reference = 1; reference <= maxRefs; reference++) {
			int vectorIndex = reference / 64; // a long has 64 bits
			if ((possibleRefs[vectorIndex] & (1L << (reference % 64))) != 0)
				refs[refsLength++] = reference;
		}
		System.arraycopy(refs, 0, refs = new int[refsLength], 0, refsLength);
		for (int i = 0; i < refsLength; i++) { // TODO (jerome) merge with previous loop
			int reference = refs[i];
			if (reference != -1) { // if the reference has not been eliminated
				IndexedFile file = input.getIndexedFile(reference);
				if (file != null) {
					String documentPath = IndexedFile.convertPath(file.getPath());
					if (scope.encloses(documentPath)) {
						if (!requestor.acceptIndexMatch(documentPath, indexRecord, participant)) 
							throw new OperationCanceledException();
					}
				}
			}
		}
	}
}
/**
 * Returns whether another query must be done.
 */
protected abstract boolean hasNextQuery();
/**
 * Resets the query and prepares this pattern to be queried.
 */
protected abstract void resetQuery();
}
