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
package org.eclipse.jdt.internal.core.index;

import java.io.*;

import org.eclipse.jdt.core.search.*;

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
	 * Returns the document name for the given number.
	 */
	protected abstract String getDocumentName(int number);
	/**
	 * Returns the index file on the disk.
	 */
	public abstract File getIndexFile();
	/**
	 * Ansers true if has some changes to save.
	 */
	public abstract boolean hasChanged();
	/**
	 * Returns the entries containing the given key in a group of categories.
	 * The matchRule dictates whether its an exact, prefix or pattern match, as well as
	 * case sensitive or insensitive.
	 */
	public abstract EntryResult[] query(char[][] categories, char[] key, int matchRule);
	/**
	 * Returns the document names that contain the given substring, if null returns all of them.
	 */
	public abstract String[] queryDocumentNames(String substring) throws IOException;
	/**
	 * Removes the corresponding document from the index.
	 */
	public abstract void remove(String documentName);
	/**
	 * Saves the index on the disk.
	 */
	public abstract void save() throws IOException;
}
