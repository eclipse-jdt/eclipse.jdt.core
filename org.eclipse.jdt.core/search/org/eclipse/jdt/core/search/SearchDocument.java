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
 * <p>
 * This class is intended to be subclassed by clients.
 * </p>
 * 
 * @since 3.0
 */
public abstract class SearchDocument {
	private String documentPath;
	private SearchParticipant participant;
	
	/**
	 * Creates a new search document. The given document path is a string that uniquely identifies the document.
	 * Most of the time it is a workspace-relative path, but it can also be a file system path, or a path inside a zip file.
	 * 
	 * @param documentPath the path to the document,
	 * or <code>null</code> if none
	 * @param participant the participant that creates the search document
	 */
	protected SearchDocument(String documentPath, SearchParticipant participant) {
		this.documentPath = documentPath;
		this.participant = participant;
	}

	/**
	 * Returns the contents of this document.
	 * Contents may be different from actual resource at corresponding document path,
	 * in case of preprocessing.
	 * <p>
	 * This method must be implemented in subclasses.
	 * </p>
	 * 
	 * @return the contents of this document,
	 * or <code>null</code> if none
	 */
	public abstract byte[] getByteContents();

	/**
	 * Returns the contents of this document.
	 * Contents may be different from actual resource at corresponding document
	 * path due to preprocessing.
	 * <p>
	 * This method must be implemented in subclasses.
	 * </p>
	 * 
	 * @return the contents of this document,
	 * or <code>null</code> if none
	 */
	public abstract char[] getCharContents();

	/**
	 * Returns the encoding for this document.
	 * <p>
	 * This method must be implemented in subclasses.
	 * </p>
	 * 
	 * @return the encoding for this document,
	 * or <code>null</code> if none
	 */
	public abstract String getEncoding();

	/**
	 * Returns the participant that created this document.
	 * 
	 * @return the participant that created this document
	 */
	public final SearchParticipant getParticipant() {
		return this.participant;
	}
	
	/**
	 * Returns the path to the original document to publicly mention in index
	 * or search results. This path is a string that uniquely identifies the document.
	 * Most of the time it is a workspace-relative path, but it can also be a file system path, 
	 * or a path inside a zip file.
	 * 
	 * @return the path to the document
	 */	
	public final String getPath() {
		return this.documentPath;
	}
}
