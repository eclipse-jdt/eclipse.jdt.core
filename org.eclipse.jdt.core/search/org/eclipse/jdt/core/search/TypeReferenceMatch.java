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
import org.eclipse.jdt.internal.core.search.matching.JavaSearchMatch;

public class TypeReferenceMatch extends JavaSearchMatch {

	public TypeReferenceMatch(IJavaElement enclosingElement, int accuracy,	int sourceStart, int sourceEnd, SearchParticipant participant, IResource resource) {
		super(enclosingElement, accuracy, sourceStart, sourceEnd, participant, resource);
	}
}
