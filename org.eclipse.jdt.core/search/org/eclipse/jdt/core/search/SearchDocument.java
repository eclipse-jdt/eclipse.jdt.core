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
package org.eclipse.jdt.core.search;

/**
 * TODO (jerome) spec
 * @since 3.0
 */
public class SearchDocument {
	protected String documentPath;
	protected SearchParticipant participant;

	public SearchDocument(String documentPath, SearchParticipant participant) {
		this.documentPath = documentPath;
		this.participant = participant;
	}

	/**
	 * Contents may be different from actual resource at corresponding document path,
	 * in case of preprocessing.
	 */
	public byte[] getByteContents() {
		return null;
	}

	/**
	 * Contents may be different from actual resource at corresponding document path,
	 * in case of preprocessing.
	 */
	public char[] getCharContents() {
		return null;
	}

	/**
	 * Returns the encoding for this document
	 */
	public String getEncoding() {
		return null;
	}

	/**
	 * Returns the participant that created this document
	 */
	public SearchParticipant getParticipant() {
		return this.participant;
	}
	
	/**
	 * Path to the original document to publicly mention in index or search results.
	 */	
	public String getPath() {
		return this.documentPath;
	}
}
