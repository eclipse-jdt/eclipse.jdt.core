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

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;

public abstract class ClasspathLocation {

static ClasspathLocation forSourceFolder(IContainer sourceFolder, IContainer outputFolder, char[][] exclusionPatterns) {
	return new ClasspathMultiDirectory(sourceFolder, outputFolder, exclusionPatterns);
}

public static ClasspathLocation forBinaryFolder(IContainer binaryFolder, boolean isOutputFolder) {
	return new ClasspathDirectory(binaryFolder, isOutputFolder);
}

static ClasspathLocation forLibrary(String libraryPathname) {
	return new ClasspathJar(libraryPathname);
}

static ClasspathLocation forLibrary(IFile library) {
	return new ClasspathJar(library);
}

public abstract NameEnvironmentAnswer findClass(String binaryFileName, String qualifiedPackageName, String qualifiedBinaryFileName);

public abstract IPath getProjectRelativePath();

public boolean isOutputFolder() {
	return false;
}

public abstract boolean isPackage(String qualifiedPackageName);

// free anything which is not required when the state is saved
public void cleanup() {
}
// reset any internal caches before another compile loop starts
public void reset() {
}
}
