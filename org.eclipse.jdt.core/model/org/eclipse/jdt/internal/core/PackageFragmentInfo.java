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
package org.eclipse.jdt.internal.core;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Element info for PackageFragments.
 */
class PackageFragmentInfo extends OpenableElementInfo {

	/**
	 * A array with all the non-java resources contained by this PackageFragment
	 */
	protected Object[] fNonJavaResources;

/**
 * Create and initialize a new instance of the receiver
 */
public PackageFragmentInfo() {
	fNonJavaResources = null;
}
/**
 */
boolean containsJavaResources() {
	return fChildren.length != 0;
}
/**
 * Returns an array of non-java resources contained in the receiver.
 */
Object[] getNonJavaResources(IResource underlyingResource, PackageFragmentRoot rootHandle) {
	Object[] nonJavaResources = fNonJavaResources;
	if (nonJavaResources == null) {
		try {
			nonJavaResources = 
				PackageFragmentRootInfo.computeFolderNonJavaResources(
					(JavaProject)rootHandle.getJavaProject(), 
					(IContainer)underlyingResource, 
					rootHandle.fullExclusionPatternChars());
		} catch (JavaModelException e) {
		}
		fNonJavaResources = nonJavaResources;
	}
	return nonJavaResources;
}
/**
 * Set the fNonJavaResources to res value
 */
synchronized void setNonJavaResources(Object[] resources) {
	fNonJavaResources = resources;
}
}
