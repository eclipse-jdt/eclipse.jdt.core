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
 * A search match represents the result of a search query.
 * 
 * @see SearchEngine#search(SearchPattern, SearchParticipant[], IJavaSearchScope, SearchRequestor, org.eclipse.core.runtime.IProgressMonitor)
 * @since 3.0
 */
public class SearchMatch {
	
	/**
	 * The search result corresponds exactly to the search pattern.
	 */
	public static final int A_ACCURATE = 0;

	/**
	 * The search result is potentially a match for the search pattern,
	 * but a problem prevented the search engine from being more accurate
	 * (typically because of the classpath was not correctly set).
	 */
	public static final int A_INACCURATE = 1;
	
	private Object element;
	private int length;
	private int offset;

	private int accuracy;
	private SearchParticipant participant;	
	private IResource resource;

	/**
	 * Creates a new search match.
	 * 
	 * @param element the element that encloses or corresponds to the match
	 * @param offset the offset the match starts at
	 * @param length the length of the match
	 */
	public SearchMatch(Object element, int offset, 	int length) {
		this.element = element;
		this.offset = offset;
		this.length = length;
	}
	
	/**
	 * Creates a new search match.
	 * 
	 * @param element the element that encloses or corresponds to the match
	 * @param accuracy one of A_ACCURATE or A_INACCURATE
	 * @param sourceStart the start position of the match, -1 if it is unknown
	 * @param sourceEnd the end position of the match, -1 if it is unknown;
	 * 	the ending offset is exclusive, meaning that the actual range of characters 
	 * 	covered is <code>[start, end]</code>
	 * @param participant the search participant that created the match
	 * @param resource the resource of the element
	 */
	public SearchMatch(
			IJavaElement element,
			int accuracy,
			int sourceStart,  
			int sourceEnd,
			SearchParticipant participant, 
			IResource resource) {
		this(element, sourceStart, sourceEnd-sourceStart);
		this.accuracy = accuracy;
		this.participant = participant;
		this.resource = resource;
	}

	/**
	 * Returns the accuracy of this search match.
	 * 
	 * @return one of A_ACCURATE or A_INACCURATE
	 */
	public int getAccuracy() {
		return this.accuracy;
	}

	/**
	 * Returns the element of this match.
	 * 
	 * @return the element of the match
	 */
	public Object getElement() {
		return this.element;
	}

	/**
	 * Returns the length of this match.
	 * 
	 * @return the length of this match.
	 */
	public int getLength() {
		return this.length;
	}
	
	/**
	 * Returns the offset of this match.
	 * 
	 * @return the offset of this match.
	 */
	public int getOffset() {
		return this.offset;
	}
	
	/**
	 * Returns the participant which issued this match
	 * 
	 * @return the participant which issued this match
	 */
	public SearchParticipant getParticipant() {
		return this.participant;
	}
	
	/**
	 * Returns the resource containing the match or <code>null</code> if it has no resource.
	 * 
	 * @return the resource of the match or <code>null</code> if it has no resource
	 */
	public IResource getResource() {
		return this.resource;
	}
	
	/**
	 * Returns whether this Java search match is inside a doc comment.
	 * 
	 * @return whether this Java search match is inside a doc comment
	 */
	public boolean insideDocComment() {
		// default is outside a doc comment
		return false;
	}

	/**
	 * Sets the length of this match.
	 * 
	 * @param length the new length of this match
	 */
	public void setLength(int length) {
		this.length = length;
	}
	
	/**
	 * Sets the offset of this match
	 * 
	 * @param offset the new offset of this match
	 */
	public void setOffset(int offset) {
		this.offset = offset;
	}
}
