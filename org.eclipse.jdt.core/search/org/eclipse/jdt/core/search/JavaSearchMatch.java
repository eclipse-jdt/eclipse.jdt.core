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
import org.eclipse.jdt.internal.core.JavaElement;

public class JavaSearchMatch extends SearchMatch {
	
	private IResource resource;
	private IJavaElement element;
	
	/*package*/ JavaSearchMatch(
			IJavaElement element,
			int accuracy,
			int sourceStart,  
			int sourceEnd,
			SearchParticipant participant, 
			IResource resource) {
		super(
			element.getElementName(), 
			element.getPath().toString(),  // document path
			accuracy, 
			participant, 
			sourceStart, 
			sourceEnd, 
			-1, // line number
			((JavaElement)element).toStringWithAncestors());
		this.resource = resource;
		this.element = element;
	}
	
	public IResource getResource() {
		return this.resource;
	}
	
	public IJavaElement getJavaElement() {
		return this.element;
	}
	
	/**
	 * Returns whether this Java search match is inside a doc comment.
	 */
	public boolean insideDocComment() {
		// default is outside a doc comment
		return false;
	}
}
