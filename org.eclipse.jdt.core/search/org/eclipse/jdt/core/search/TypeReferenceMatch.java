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

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;

/**
 * A Java search match that represents a type reference.
 * The element is the inner-most enclosing member that references this type.
 * <p>
 * This class is intended to be instantiated and subclassed by clients.
 * </p>
 * 
 * @since 3.0
 */
public class TypeReferenceMatch extends SearchMatch {

	private boolean insideDocComment;

	/**
	 * Creates a new type reference match.
	 * 
	 * @param enclosingElement the inner-most enclosing member that references this type
	 * @param accuracy one of A_ACCURATE or A_INACCURATE
	 * @param offset the offset the match starts at, or -1 if unknown
	 * @param length the length of the match, or -1 if unknown
	 * @param insideDocComment whether the match is inside a doc comment
	 * @param participant the search participant that created the match
	 * @param resource the resource of the element
	 */
	public TypeReferenceMatch(IJavaElement enclosingElement, int accuracy,	int offset, int length, boolean insideDocComment, SearchParticipant participant, IResource resource) {
		super(enclosingElement, accuracy, offset, length, participant, resource);
		this.insideDocComment = insideDocComment;
	}
	
	/**
	 * @see org.eclipse.jdt.core.search.SearchMatch#isInsideDocComment()
	 */
	public final boolean isInsideDocComment() {
		return this.insideDocComment;
	}
}
