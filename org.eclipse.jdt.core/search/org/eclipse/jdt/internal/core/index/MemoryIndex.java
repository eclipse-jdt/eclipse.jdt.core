/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.index;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.core.util.DeduplicationUtil;

public class MemoryIndex {

public int NUM_CHANGES = 100; // number of separate document changes... used to decide when to merge

final Map<String, Map<String, Set<String>>> docsToReferences; // document paths -> Map<>(category names -> set of words)
String lastDocumentName;
Map<String, Set<String>> lastReferenceTable;

MemoryIndex() {
	this.docsToReferences = new HashMap<>();
}
void addDocumentNames(String substring, Set<String> results) {
	// assumed the disk index already skipped over documents which have been added/changed/deleted
	if (substring == null) { // add all new/changed documents
		for (Entry<String, Map<String, Set<String>>> e:this.docsToReferences.entrySet()) {
			if (e.getValue() != null) {
				results.add(e.getKey());
			}
		}
	} else {
		for (Entry<String, Map<String, Set<String>>> e:this.docsToReferences.entrySet())
			if (e.getValue() != null && (e.getKey()).startsWith(substring, 0))
				results.add(e.getKey());
	}
}
void addIndexEntry(String category, String key, String documentName) {
	Map<String, Set<String>> referenceTable;
	if (documentName.equals(this.lastDocumentName))
		referenceTable = this.lastReferenceTable;
	else {
		// assumed a document was removed before its reindexed
		referenceTable =  this.docsToReferences.computeIfAbsent(documentName, k-> new HashMap<>());
		this.lastDocumentName = documentName;
		this.lastReferenceTable = referenceTable;
	}

	Set<String> existingWords = referenceTable.computeIfAbsent(category, k -> new HashSet<>());
	String deduplicatedKey = DeduplicationUtil.intern(key); // XXX performance hotspot
	// XXX: Deduplication is also implicitly done again when saving to disk (but not in memory)
	// see org.eclipse.jdt.internal.core.index.DiskIndex.writeCategoryTable(String, Map<String, Object>, OutputStream)
	// and org.eclipse.jdt.internal.core.index.DiskIndex.copyQueryResults(Map<String, Set<String>>, int)
	existingWords.add(deduplicatedKey);
}
Map<String, EntryResult> addQueryResults(List<String> categories, String key, int matchRule, Map<String, EntryResult> results) {
	// assumed the disk index already skipped over documents which have been added/changed/deleted
	// results maps a word -> EntryResult
	if (matchRule == (SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE) && key != null) {
		nextPath : for (Entry<String, Map<String, Set<String>>> e: this.docsToReferences.entrySet()) {
			Map<String, Set<String>> categoryToWords = e.getValue();
			if (categoryToWords != null) {
				for (String category : categories) {
					Set<String> wordSet = categoryToWords.get(category);
					if (wordSet != null && wordSet.contains(key)) {
						if (results == null)
							results = new HashMap<>(13);
						EntryResult result = results.get(key);
						if (result == null)
							results.put(key, result = new EntryResult(key, null));
						result.addDocumentName(e.getKey());
						continue nextPath;
					}
				}
			}
		}
	} else {
		for (Entry<String, Map<String, Set<String>>> e: this.docsToReferences.entrySet()) {
			Map<String, Set<String>> categoryToWords = e.getValue();
			if (categoryToWords != null) {
				for (String category : categories) {
					Set<String> wordSet = categoryToWords.get(category);
					if (wordSet != null) {
						for (String word : wordSet) {
							if (word != null && Index.isMatch(key == null ? null : key.toCharArray(),
									word.toCharArray(), matchRule)) {
								if (results == null)
									results = new HashMap<>(13);
								EntryResult result = results.get(word);
								if (result == null)
									results.put(word, result = new EntryResult(word, null));
								result.addDocumentName(e.getKey());
							}
						}
					}
				}
			}
		}
	}
	return results;
}
boolean hasChanged() {
	return this.docsToReferences.size() > 0;
}
void remove(String documentName) {
	if (documentName.equals(this.lastDocumentName)) {
		this.lastDocumentName = null;
		this.lastReferenceTable = null;
	}
	this.docsToReferences.put(documentName, null);
}
boolean shouldMerge() {
	return this.docsToReferences.size() >= this.NUM_CHANGES;
}
}
