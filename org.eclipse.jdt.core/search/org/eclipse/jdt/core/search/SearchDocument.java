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
public abstract class SearchDocument {
	
	/**
	 * Contents may be different from actual resource at corresponding document path,
	 * in case of preprocessing.
	 */
	public abstract byte[] getByteContents();

	/**
	 * Contents may be different from actual resource at corresponding document path,
	 * in case of preprocessing.
	 */
	public abstract char[] getCharContents();

	/**
	 * Returns the encoding for this document
	 */
	public abstract String getEncoding();
	
	/**
	 * Returns the participant that created this document
	 */
	public abstract SearchParticipant getParticipant();
	
	/**
	 * Path to the original document to publicly mention in index or search results.
	 */	
	public abstract String getPath();
}
