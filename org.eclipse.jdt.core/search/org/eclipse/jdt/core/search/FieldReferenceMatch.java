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
 * TODO add spec
 * @since 3.0
 */
public class FieldReferenceMatch extends JavaSearchMatch {

	private boolean insideDocComment;
	private boolean isReadAccess;
	private boolean isWriteAccess;

	public FieldReferenceMatch(IJavaElement element, int accuracy, int sourceStart, int sourceEnd, boolean isReadAccess, boolean isWriteAccess, boolean insideDocComment, SearchParticipant participant, IResource resource) {
		super(element, accuracy, sourceStart, sourceEnd, participant, resource);
		this.isReadAccess = isReadAccess;
		this.isWriteAccess = isWriteAccess;
		this.insideDocComment = insideDocComment;
	}
	
	public boolean insideDocComment() {
		return this.insideDocComment;
	}

	// important point is that a match can be read & write at once in case of compound assignments:  e.g. i += 0;
	public boolean isReadAccess() {
		return this.isReadAccess;
	}

	public boolean isWriteAccess() {
		return this.isWriteAccess;
	}
	
}
