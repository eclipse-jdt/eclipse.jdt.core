/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.core.builder;

import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;

abstract class ClasspathLocation {

static ClasspathLocation forSourceFolder(String sourceFolderPathname, String outputFolderPathname) {
	return new ClasspathMultiDirectory(sourceFolderPathname, outputFolderPathname);
}

static ClasspathLocation forBinaryFolder(String binaryFolderPathname) {
	return new ClasspathDirectory(binaryFolderPathname);
}

static ClasspathLocation forLibrary(String libraryPathname) {
	return new ClasspathJar(libraryPathname);
}

abstract NameEnvironmentAnswer findClass(String binaryFileName, String qualifiedPackageName, String qualifiedBinaryFileName);
abstract boolean isPackage(String qualifiedPackageName);

// free anything which is not required when the state is saved
void cleanup() {
}
// reset any internal caches before another compile loop starts
void reset() {
}
}