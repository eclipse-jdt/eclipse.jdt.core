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
 * A Java search match that represents a local variable reference.
 * The element is the inner-most enclosing member that references this local variable.
 * <p>
 * This class is intended to be instantiated and subclassed by clients.
 * </p>
 * 
 * @since 3.0
 */
public class LocalVariableReferenceMatch extends SearchMatch {

	private boolean insideDocComment;
	private boolean isReadAccess;
	private boolean isWriteAccess;

	/**
	 * Creates a new local variable reference match.
	 * 
	 * @param enclosingElement the inner-most enclosing member that references this local variable
	 * @param accuracy one of A_ACCURATE or A_INACCURATE
	 * @param offset the offset the match starts at, or -1 if unknown
	 * @param length the length of the match, or -1 if unknown
	 * @param isReadAccess whether the match represents a read access
	 * @param isWriteAccess whethre the match represents a write access
	 * @param insideDocComment whether the match is inside a doc comment
	 * @param participant the search participant that created the match
	 * @param resource the resource of the element
	 */
	public LocalVariableReferenceMatch(IJavaElement enclosingElement, int accuracy, int offset, int length, boolean isReadAccess, boolean isWriteAccess, boolean insideDocComment, SearchParticipant participant, IResource resource) {
		super(enclosingElement, accuracy, offset, length, participant, resource);
		this.isReadAccess = isReadAccess;
		this.isWriteAccess = isWriteAccess;
		this.insideDocComment = insideDocComment;
	}

	/**
	 * @see org.eclipse.jdt.core.search.SearchMatch#isInsideDocComment()
	 */
	public final boolean isInsideDocComment() {
		return this.insideDocComment;
	}

	/**
	 * Returns whether the local variable reference is a read access to the variable.
	 * Note that a local variable reference can be read and written at once in case of compound assignments (e.g. i += 0;)
	 * 
	 * @return whether the local variable reference is a read access to the variable.
	 */
	public final boolean isReadAccess() {
		return this.isReadAccess;
	}

	/**
	 * Returns whether the local variable reference is a write access to the variable.
	 * Note that a local variable reference can be read and written at once in case of compound assignments (e.g. i += 0;)
	 * 
	 * @return whether the local variable reference is a write access to the variable.
	 */
	public final boolean isWriteAccess() {
		return this.isWriteAccess;
	}
}
