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
package org.eclipse.jdt.internal.core.index.impl;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.internal.core.index.IDocument;
import org.eclipse.jdt.internal.core.index.IQueryResult;

/**
 * An indexedFile associates a number to a document path, and document properties. 
 * It is what we add into an index, and the result of a query.
 */

public class IndexedFile implements IQueryResult {
	protected String path;
	protected int fileNumber;

	public IndexedFile(String path, int fileNum) {
		if (fileNum < 1)
			throw new IllegalArgumentException();
		this.fileNumber= fileNum;
		this.path= path;
	}
	public IndexedFile(IDocument document, int fileNum) {
		if (fileNum < 1)
			throw new IllegalArgumentException();
		this.path= document.getName();
		this.fileNumber= fileNum;
	}
	/**
	 * Returns the path represented by pathString converted back to a path relative to the local file system.
	 *
	 * @param pathString the path to convert:
	 * <ul>
	 *		<li>an absolute IPath (relative to the workspace root) if the path represents a resource in the 
	 *			workspace
	 *		<li>a relative IPath (relative to the workspace root) followed by JAR_FILE_ENTRY_SEPARATOR
	 *			followed by an absolute path (relative to the jar) if the path represents a .class file in
	 *			an internal jar
	 *		<li>an absolute path (relative to the file system) followed by JAR_FILE_ENTRY_SEPARATOR
	 *			followed by an absolute path (relative to the jar) if the path represents a .class file in
	 *			an external jar
	 * </ul>
	 * @return the converted path:
	 * <ul>
	 *		<li>the original pathString if the path represents a resource in the workspace
	 *		<li>an absolute path (relative to the file system) followed by JAR_FILE_ENTRY_SEPARATOR
	 *			followed by an absolute path (relative to the jar) if the path represents a .class file in
	 *			an external or internal jar
	 * </ul>
	 */
	public static String convertPath(String pathString) {
		int index = pathString.indexOf(JarFileEntryDocument.JAR_FILE_ENTRY_SEPARATOR);
		if (index == -1)
			return pathString;
			
		Path jarPath = new Path(pathString.substring(0, index));
		if (!jarPath.isAbsolute()) {
			return jarPath.makeAbsolute().toString() + pathString.substring(index, pathString.length());
		} else {
			return jarPath.toOSString() + pathString.substring(index, pathString.length());
		}
	}
	/**
	 * Returns the size of the indexedFile.
	 */
	public int footprint() {
		//object+ 2 slots + size of the string (header + 4 slots + char[])
		return 8 + (2 * 4) + (8 + (4 * 4) + 8 + path.length() * 2);
	}
	/**
	 * Returns the file number.
	 */
	public int getFileNumber() {
		return fileNumber;
	}
	/**
	 * Returns the path.
	 */
	public String getPath() {
		return path;
	}
	/**
	 * Sets the file number.
	 */
	public void setFileNumber(int fileNumber) {
		this.fileNumber= fileNumber;
	}
	public String toString() {
		return "IndexedFile(" + fileNumber + ": " + path + ")"; //$NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-3$
	}
}
