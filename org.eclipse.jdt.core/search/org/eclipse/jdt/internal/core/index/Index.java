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
package org.eclipse.jdt.internal.core.index;

import java.io.*;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.core.index.impl.EntryResult;

/**
 * An IIndex is the interface used to generate an index file, and to make queries on
 * this index.
 */

public abstract class Index {
	/**
	 * Associates the category and key with the document.
	 */
	public abstract void addIndexEntry(char[] category, char[] key, SearchDocument document);
	/**
	 * Returns the index file on the disk.
	 */
	public abstract File getIndexFile();
	/**
	 * Returns the path corresponding to a given document number
	 */
	public abstract String getPath(int documentNumber) throws IOException;
	/**
	 * Ansers true if has some changes to save.
	 */
	public abstract boolean hasChanged();
	/**
	 * Adds the given document to the index.
	 */
	public abstract void indexDocument(SearchDocument document, SearchParticipant searchParticipant, IPath indexPath) throws IOException;
	/**
	 * Returns the paths of the documents containing the given word - query a group of categories.
	 */
//	EntryResult[] query(char[][] categories, char[] key, int matchRule);
	/**
	 * Returns the paths of the documents containing the given word.
	 */
	public abstract String[] query(String word) throws IOException;
	/**
	 * Returns all entries for a given word.
	 */
	public abstract EntryResult[] queryEntries(char[] pattern) throws IOException;
	/**
	 * Returns the paths of the documents whose names contain the given word.
	 */
	public abstract String[] queryInDocumentNames(String word) throws IOException;
	/**
	 * Returns the paths of the documents containing the given word prefix.
	 */
	public abstract String[] queryPrefix(char[] prefix) throws IOException;
	/**
	 * Removes the corresponding document from the index.
	 */
	public abstract void remove(String documentName);
	/**
	 * Saves the index on the disk.
	 */
	public abstract void save() throws IOException;
}
