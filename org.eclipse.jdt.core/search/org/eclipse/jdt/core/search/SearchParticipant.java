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
package org.eclipse.jdt.core.search;

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.internal.core.JavaModelManager;

/**
 * A search participant describes a particular extension to a generic search mechanism, permitting 
 * combined search actions which will involve all required participants.
 * 
 * A search scope defines which participants are involved. 
 * 
 * A search participant is responsible for holding index files, and selecting the appropriate ones to feed to
 * index queries. It also can map a document path to an actual document (note that documents could live outside
 * the workspace or not exist yet, and thus aren't just resources).
 * 
 * @since 3.0
 */
public abstract class SearchParticipant {

	/**
	 * An empty list of search participants.
	 */
	public static final SearchParticipant[] NO_PARTICIPANT = {};

	/**
	 * Adds the given index entry (category and key) coming from the given document to the index.
	 * This method must be called from indexDocument(SearchDocument document, IPath indexPath).
	 * 
	 * @param category the category of the index entry
	 * @param key the key of the index entry
	 * @param document the document that is being indexed
	 */
	public static void addIndexEntry(char[] category, char[] key, SearchDocument document) {
		if (document.index != null)
			document.index.addIndexEntry(category, key, document);
	}
	/**
	 * Removes all index entries from the index for the given document.
	 * This method must be called from indexDocument(SearchDocument document, IPath indexPath).
	 * 
	 * @param document the document that is being indexed
	 */
	public static void removeAllIndexEntries(SearchDocument document) {
		if (document.index != null)
			document.index.remove(document.getPath());
	}

	/**
	 * Intermediate notification sent when a given participant is getting involved.
	 */
	public abstract void beginSearching();

	/**
	 * Intermediate notification sent when a given participant is finished to be involved.
	 */
	public abstract void doneSearching();

	/**
	 * Returns a displayable name of this search participant. e.g. "Java".
	 * 
	 * @return the displayable name of this search participant
	 */
	public abstract String getDescription();

	/**
	 * Returns a search document for the given file.
	 * 
	 * @param file the given file
	 * @return a search document
	 */
	public abstract SearchDocument getDocument(IFile file);

	/**
	 * Returns a search document for the given path.
	 * 
	 * @param documentPath the path of the document.
	 * @return a search document
	 */
	public abstract SearchDocument getDocument(String documentPath);

	/**
	 * Indexes the given document in the given index. A search participant asked to index a document should
	 * parse it and call addIndexEntry(char[], char[], SearchDocument) as many times as needed to add index
	 * entries to the index.
	 * If delegating to another participant, it should use the original index location (and not the delegatee's one)
	 * 
	 * @param document the document to index
	 * @param indexPath the path in the file system to the index
	 */
	public abstract void indexDocument(SearchDocument document, IPath indexPath);

	/**
	 * Locates the matches in the given documents using the given search pattern and seacrh scope
	 * and reports them using the search requestor. This is called by the search engine once it got documents 
	 * that match the given pattern in the given search scope.
	 * <p>
	 * Note: It permits combined match locators (e.g. jsp match locator can preprocess jsp unit contents and feed them
	 * to Java match locator asking for virtual matches by contributing document implementations which do the conversion).
	 * It is assumed that virtual matches are rearranged by requestor for adapting line/source positions before submitting
	 * final results so the provided searchRequestor should intercept virtual matches and do appropriate conversions.
	 * </p>
	 * 
	 * @param documents the documents to locate matches in
	 * @param pattern the search pattern to use when locating matches
	 * @param scope the scope to limit the search to
	 * @param requestor the requestor to report matches to
	 * @param monitor the progress monitor to report progress to or null if no progress should be reported
	 * @throws CoreException if the requestor had problem accepting one of the matches
	 */
	public abstract void locateMatches(SearchDocument[] documents, SearchPattern pattern, IJavaSearchScope scope, SearchRequestor requestor, IProgressMonitor monitor) throws CoreException;

	/**
	 * Schedules the indexing of the given document.
	 * Once the document is ready to be indexed, indexDocument(document, indexPath) is called.
	 * 
	 * @param document the document to index
	 * @param indexPath the path on the file system of the index
	 */
	public void scheduleDocumentIndexing(SearchDocument document, IPath indexPath) {
		JavaModelManager.getJavaModelManager().getIndexManager().scheduleDocumentIndexing(document, indexPath, this);
	}

	/**
	 * Returns the collection of index paths to consider when performing a given search query in a given scope.
	 * This is called by the search engine before locating matches.
	 * 
	 * @param query the search pattern to consider
	 * @param scope the given search scope
	 * @return the collection of index paths to consider
	 */
	public abstract IPath[] selectIndexes(SearchPattern query, IJavaSearchScope scope);
}
