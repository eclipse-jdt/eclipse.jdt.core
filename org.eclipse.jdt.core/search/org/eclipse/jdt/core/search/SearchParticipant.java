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
import org.eclipse.jdt.internal.core.index.Index;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;

/**
 * A search participant describes a particular extension to a generic search
 * mechanism, permitting combined search actions which will involve all required
 * participants.
 * <p>
 * A search participant is responsible for holding index files, and selecting
 * the appropriate ones to feed to index queries. It also can map a document
 * path to an actual document (note that documents could live outside the
 * workspace or not exist yet, and thus aren't just resources).
 * </p>
 * 
 * @since 3.0
 */
//	 TODO (jerome) - needs subclass contract; describe expected sequence of requests that particpant will see
// TODO (jerome) - need to explain how indexing (which is largely hidden) and searching work together
public abstract class SearchParticipant {

	/**
	 * An empty list of search participants.
	 */
	//	 TODO (jerome) - delete - APIs should not provide conveniences like this unless there is a compeling reason to do so
	public static final SearchParticipant[] NO_PARTICIPANT = {};
	
	/**
	 * Adds the given index entry (category and key) coming from the given
	 * document to the index. This method must be called from
	 * {@link #indexDocument(SearchDocument document, IPath indexPath).
	 * 
	 * @param category the category of the index entry
	 * @param key the key of the index entry
	 * @param document the document that is being indexed
	 */
	public static void addIndexEntry(char[] category, char[] key, SearchDocument document) {
		//	 TODO (jerome) - check for category != null
		//	 TODO (jerome) - check for key != null
		IndexManager indexManager = JavaModelManager.getJavaModelManager().getIndexManager();
		Index index = (Index) indexManager.documentIndexes.get(document);
		if (index != null)
			index.addIndexEntry(category, key, document);
	}
	/**
	 * Removes all index entries from the index for the given document.
	 * This method must be called from 
	 * {@link #indexDocument(SearchDocument document, IPath indexPath).
	 * 
	 * @param document the document that is being indexed
	 */
	public static void removeAllIndexEntries(SearchDocument document) {
		IndexManager indexManager = JavaModelManager.getJavaModelManager().getIndexManager();
		Index index = (Index) indexManager.documentIndexes.get(document);
		if (index != null)
			index.remove(document.getPath());
	}

	/**
	 * Creates a new search participant.
	 */
	protected SearchParticipant() {
		// do nothing
	}
	
	/**
	 * Notification that this participant's help is needed in a search.
	 */
	//	 TODO (jerome) - needs subclass contract
	//	 TODO (jerome) - method should be non-abstract no-op so that simple clients are not forced to implement
	public abstract void beginSearching();

	/**
	 * Notification that this participant's help is no longer needed.
	 */
	//	 TODO (jerome) - needs subclass contract
	//	 TODO (jerome) - method should be non-abstract no-op so that simple clients are not forced to implement
	public abstract void doneSearching();

	/**
	 * Returns a displayable name of this search participant.
	 * <p>
	 * Subclasses must implement.
	 * </p>
	 * 
	 * @return the displayable name of this search participant
	 */
	//	 TODO (jerome) - how important is having a nice description? method should be non-abstract no-op so that simple clients are not forced to implement
	public abstract String getDescription();

	/**
	 * Returns a search document for the given file.
	 * 
	 * @param file the given file
	 * @return a search document
	 */
	//	 TODO (jerome) - does this create a new document each call? if yes, the method should be renamed createDocument to make that clear
	//	 TODO (jerome) - needs subclass contract; explain that implementor typically returns instance of own subclass of SearchDocument
	//	 TODO (jerome) - explain relationship to other getDocument method, and how it relates to SearchDocument constructor which expects a String path
	public abstract SearchDocument getDocument(IFile file);

	/**
	 * Returns a search document for the given path.
	 * 
	 * @param documentPath the path of the document.
	 * @return a search document
	 */
	//	 TODO (jerome) - does this create a new document each call? if yes, the method should be renamed createDocument to make that clear
	//	 TODO (jerome) - needs subclass contract; explain that implementor typically returns instance of own subclass of SearchDocument
	//	 TODO (jerome) - spec should clarify whether documentPath is a workspace-relative path or a general file system path?
	public abstract SearchDocument getDocument(String documentPath);

	/**
	 * Indexes the given document in the given index. A search participant
	 * asked to index a document should parse it and call 
	 * {@link #addIndexEntry(char[], char[], SearchDocument)} as many times as
	 * needed to add index entries to the index. If delegating to another
	 * participant, it should use the original index location (and not the
	 * delegatee's one).
	 * 
	 * @param document the document to index
	 * @param indexPath the path in the file system to the index
	 */
	//	 TODO (jerome) - it seems odd that strings are used for document paths and IPaths are used for index paths
	//	 TODO (jerome) - spec should clarify whether index paths are relative or absolute, workspace or file system
	public abstract void indexDocument(SearchDocument document, IPath indexPath);

	/**
	 * Locates the matches in the given documents using the given search pattern
	 * and search scope, and reports them to the givenn search requestor. This
	 * method is called by the search engine once it has search documents
	 * matching the given pattern in the given search scope.
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
	 * @param monitor the progress monitor to report progress to,
	 * or <code>null</code> if no progress should be reported
	 * @throws CoreException if the requestor had problem accepting one of the matches
	 */
	//	 TODO (jerome) - I could not understand the point the "Note:..." was making
	//	 TODO (jerome) - needs subclass contract
	//	 TODO (jerome) - verify that throwing CoreException is the right behavior; throwing any exception means that flow of results to requestor will not be seen through to completion
	public abstract void locateMatches(SearchDocument[] documents, SearchPattern pattern, IJavaSearchScope scope, SearchRequestor requestor, IProgressMonitor monitor) throws CoreException;

	/**
	 * Schedules the indexing of the given document.
	 * Once the document is ready to be indexed, 
	 * {@link #indexDocument(SearchDocument, IPath) indexDocument(document, indexPath)}
	 * will be called.
	 * 
	 * @param document the document to index
	 * @param indexPath the path on the file system of the index
	 */
	//	 TODO (jerome) - method should be final
	//	 TODO (jerome) - since effects are not immediate, spec should say what thread indexDocument will be called on
	//	 TODO (jerome) - spec should clarify whether index paths are relative or absolute, workspace or file system
	public void scheduleDocumentIndexing(SearchDocument document, IPath indexPath) {
		JavaModelManager.getJavaModelManager().getIndexManager().scheduleDocumentIndexing(document, indexPath, this);
	}

	/**
	 * Returns the collection of index paths to consider when performing the
	 * given search query in the given scope. The search engine calls this
	 * method before locating matches.
	 * 
	 * @param query the search pattern to consider
	 * @param scope the given search scope
	 * @return the collection of index paths to consider
	 */
	//	 TODO (jerome) - needs subclass contract; clients should not call
	//	 TODO (jerome) - spec should clarify whether index paths are relative or absolute, workspace or file system
	public abstract IPath[] selectIndexes(SearchPattern query, IJavaSearchScope scope);
}
