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
package org.eclipse.jdt.internal.core.builder;

import org.eclipse.core.resources.IContainer;
import org.eclipse.jdt.core.compiler.CharOperation;

class ClasspathMultiDirectory extends ClasspathDirectory {

IContainer sourceFolder;
char[][] exclusionPatterns; // used by builders when walking source folders
boolean hasIndependentOutputFolder; // if output folder is not equal to any of the source folders

ClasspathMultiDirectory(IContainer sourceFolder, IContainer binaryFolder, char[][] exclusionPatterns) {
	super(binaryFolder, true);

	this.sourceFolder = sourceFolder;
	this.exclusionPatterns = exclusionPatterns;
	this.hasIndependentOutputFolder = false;

	// handle the case when a state rebuilds a source folder
	if (this.exclusionPatterns != null && this.exclusionPatterns.length == 0)
		this.exclusionPatterns = null;
}

public boolean equals(Object o) {
	if (this == o) return true;
	if (!(o instanceof ClasspathMultiDirectory)) return false;

	ClasspathMultiDirectory md = (ClasspathMultiDirectory) o;
	return sourceFolder.equals(md.sourceFolder) && binaryFolder.equals(md.binaryFolder)
		&& CharOperation.equals(exclusionPatterns, md.exclusionPatterns);
} 

public String toString() {
	return "Source classpath directory " + sourceFolder.getFullPath().toString() + //$NON-NLS-1$
		" with binary directory " + binaryFolder.getFullPath().toString(); //$NON-NLS-1$
}
}
