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

/**
 * A search document encapsulates a content to be either indexed or searched in.
 * A search particpant creates a search document.
 * 
 * @since 3.0
 */
public class SearchDocument {
	protected String documentPath;
	protected SearchParticipant participant;
	
	/**
	 * Internal field: do not use
	 */
	public org.eclipse.jdt.internal.core.index.Index index;

	/**
	 * Creates a new search document.
	 * 
	 * @param documentPath the path to the document on disk or <code>null</code> if not provided
	 * @param participant the participant that creates the search document
	 */
	public SearchDocument(String documentPath, SearchParticipant participant) {
		this.documentPath = documentPath;
		this.participant = participant;
	}

	/**
	 * Returns the contents of this document.
	 * Contents may be different from actual resource at corresponding document path,
	 * in case of preprocessing.
	 * 
	 * @return the contents of this document.
	 */
	public byte[] getByteContents() {
		return null;
	}

	/**
	 * Returns the contents of this document.
	 * Contents may be different from actual resource at corresponding document path,
	 * in case of preprocessing.
	 * 
	 * @return the contents of this document.
	 */
	public char[] getCharContents() {
		return null;
	}

	/**
	 * Returns the encoding for this document
	 * 
	 * @return the encoding for this document
	 */
	public String getEncoding() {
		return null;
	}

	/**
	 * Returns the participant that created this document
	 * 
	 * @return the participant that created this document
	 */
	public SearchParticipant getParticipant() {
		return this.participant;
	}
	
	/**
	 * Returns the path to the original document to publicly mention in index or search results.
	 * 
	 * @return the path to the document
	 */	
	public String getPath() {
		return this.documentPath;
	}
}
