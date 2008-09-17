/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.matching;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.search.*;

/**
 * A method reference match that stores the local element
 */
public class InternalFieldReferenceMatch extends FieldReferenceMatch {

	IJavaElement localElement;

public InternalFieldReferenceMatch(IJavaElement enclosingElement, int accuracy, int offset, int length, boolean isReadAccess, boolean isWriteAccess, boolean insideDocComment, SearchParticipant participant, IResource resource) {
	super(enclosingElement, accuracy, offset, length, isReadAccess, isWriteAccess, insideDocComment, participant, resource);
}

/**
 * Return the stored local element.
 *
 * @see org.eclipse.jdt.core.search.ReferenceMatch#getLocalElement()
 */
protected IJavaElement localElement() {
	return this.localElement;
}

/**
 * Store the local element in the match.
 *
 * @param element The local element to be stored
 */
public void localElement(IJavaElement element) {
	this.localElement = element;
}
}
