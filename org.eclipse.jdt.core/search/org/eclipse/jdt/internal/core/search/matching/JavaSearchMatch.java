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
package org.eclipse.jdt.internal.core.search.matching;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;

public class JavaSearchMatch extends SearchMatch {
	
	public IResource resource;
	public IJavaElement element;
	
	public JavaSearchMatch(
			IResource resource,
			IJavaElement element,
			String documentPath,
			int accuracy,  
			SearchParticipant participant,
			int sourceStart, 
			int sourceEnd, 
			int sourceLineNumber) {
		super(
			element.getElementName(), 
			documentPath, 
			accuracy, 
			participant, 
			sourceStart, 
			sourceEnd, 
			sourceLineNumber, 
			element.toString());
		this.resource = resource;
		this.element = element;
	}
}
