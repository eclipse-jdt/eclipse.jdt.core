package org.eclipse.jdt.internal.core.index;

public interface IIndexer {
	/**
	 * Returns the file types the <code>IIndexer</code> handles.
	 */

	String[] getFileTypes();
	/**
	 * Indexes the given document, adding the document name and the word references 
	 * to this document to the given <code>IIndex</code>.The caller should use 
	 * <code>shouldIndex()</code> first to determine whether this indexer handles 
	 * the given type of file, and only call this method if so. 
	 */

	void index(IDocument document, IIndexerOutput output) throws java.io.IOException;
	/**
	 * Sets the document types the <code>IIndexer</code> handles.
	 */

	public void setFileTypes(String[] fileTypes);
	/**
	 * Returns whether the <code>IIndexer</code> can index the given document or not.
	 */

	public boolean shouldIndex(IDocument document);
}
