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
package org.eclipse.jdt.internal.core.search.matching;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.core.search.PackageReferenceMatch;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.internal.core.JavaElement;

public class JavaSearchMatch extends SearchMatch {
	
	public IResource resource;
	public IJavaElement element;
	
	public JavaSearchMatch(
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
	public static JavaSearchMatch newDeclarationMatch(
			IJavaElement element,
			int accuracy,
			int sourceStart,  
			int sourceEnd,
			MatchLocator locator) {
		SearchParticipant participant = locator.getParticipant(); 
		IResource resource = locator.currentPossibleMatch.resource;
		return newDeclarationMatch(element, accuracy, sourceStart, sourceEnd, participant, resource);
	}
	public static JavaSearchMatch newDeclarationMatch(
			IJavaElement element,
			int accuracy,
			int sourceStart,  
			int sourceEnd,
			SearchParticipant participant, 
			IResource resource) {
		switch (element.getElementType()) {
			case IJavaElement.PACKAGE_FRAGMENT:
				return new PackageDeclarationMatch(element, accuracy, sourceStart, sourceEnd, participant, resource);
			case IJavaElement.TYPE:
				return new TypeDeclarationMatch(element, accuracy, sourceStart, sourceEnd, participant, resource);
			case IJavaElement.FIELD:
				return new FieldDeclarationMatch(element, accuracy, sourceStart, sourceEnd, participant, resource);
			case IJavaElement.METHOD:
				return new MethodDeclarationMatch(element, accuracy, sourceStart, sourceEnd, participant, resource);
			case IJavaElement.LOCAL_VARIABLE:
				return new LocalVariableDeclarationMatch(element, accuracy, sourceStart, sourceEnd, participant, resource);
			default:
				return new JavaSearchMatch(element, accuracy, sourceStart, sourceEnd, participant, resource);
		}
	}
	public static JavaSearchMatch newReferenceMatch(
			int referenceType,
			IJavaElement enclosingElement,
			int accuracy,
			int sourceStart,  
			int sourceEnd,
			MatchLocator locator) {
		SearchParticipant participant = locator.getParticipant(); 
		IResource resource = locator.currentPossibleMatch.resource;
		switch (referenceType) {
			case IJavaElement.PACKAGE_FRAGMENT:
				return new PackageReferenceMatch(enclosingElement, accuracy, sourceStart, sourceEnd, participant, resource);
			case IJavaElement.TYPE:
				return new TypeReferenceMatch(enclosingElement, accuracy, sourceStart, sourceEnd, participant, resource);
			case IJavaElement.FIELD:
				return new FieldReferenceMatch(enclosingElement, accuracy, sourceStart, sourceEnd, participant, resource);
			case IJavaElement.METHOD:
				return new MethodReferenceMatch(enclosingElement, accuracy, sourceStart, sourceEnd, participant, resource);
			case IJavaElement.LOCAL_VARIABLE:
				return new LocalVariableReferenceMatch(enclosingElement, accuracy, sourceStart, sourceEnd, participant, resource);
			default:
				return new JavaSearchMatch(enclosingElement, accuracy, sourceStart, sourceEnd, participant, resource);
		}
	}
}
