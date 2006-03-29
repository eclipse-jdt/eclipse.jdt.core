/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

	private IJavaElement localElement;
	private IJavaElement[] otherElements;

	/**
	 * Creates a new type reference match.
	 * 
	 * @param enclosingElement the inner-most enclosing member that references this type
	 * @param accuracy one of {@link #A_ACCURATE} or {@link #A_INACCURATE}
	 * @param offset the offset the match starts at, or -1 if unknown
	 * @param length the length of the match, or -1 if unknown
	 * @param insideDocComment <code>true</code> if this search match is inside a doc
	 * comment, and <code>false</code> otherwise
	 * @param participant the search participant that created the match
	 * @param resource the resource of the element
	 */
	public TypeReferenceMatch(IJavaElement enclosingElement, int accuracy,	int offset, int length, boolean insideDocComment, SearchParticipant participant, IResource resource) {
		super(enclosingElement, accuracy, offset, length, participant, resource);
		setInsideDocComment(insideDocComment);
	}

	/**
	 * Returns the local element of this search match.
	 * This may be a local variable which declaring type is the referenced one
	 * or a type parameter which extends it.
	 * 
	 * @return the element of the search match, or <code>null</code> if none or there's
	 * 	no more specific local element than the element itself ({@link SearchMatch#getElement()}).
	 * @since 3.2
	 */
	public final IJavaElement getLocalElement() {
		return this.localElement;
	}

	/**
	 * Returns other enclosing elements of this search match.
	 *
	 * If {@link #getLocalElement()} is not <code>null</code>, these may be other
	 * local elements such as additional local variables of a multiple local
	 * variables declaration. Otherwise, these may be other elements such as
	 * additional fields of a multiple fields declaration.
	 * 
	 * @return the other elements of the search match, or <code>null</code> if none
	 * @since 3.2
	 */
	public final IJavaElement[] getOtherElements() {
		return this.otherElements;
	}

	/**
	 * Sets the local element of this search match.
	 * 
	 * @param localElement A more specific local element that corresponds to the match,
	 * 	or <code>null</code> if none
	 * @since 3.2
	 */
	public final void setLocalElement(IJavaElement localElement) {
		this.localElement = localElement;
	}

	/**
	 * Sets the other elements of this search match.
	 * 
	 * @param otherElements the other elements of the match,
	 * 	or <code>null</code> if none
	 * @since 3.2
	 */
	public final void setOtherElements(IJavaElement[] otherElements) {
		this.otherElements = otherElements;
	}
}
