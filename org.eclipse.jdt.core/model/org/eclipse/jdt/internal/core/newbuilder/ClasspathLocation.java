package org.eclipse.jdt.internal.core.newbuilder;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;

abstract class ClasspathLocation {

static ClasspathLocation forSourceFolder(String sourceFolderPathname, String outputFolderPathname) {
	return new ClasspathMultiDirectory(sourceFolderPathname, outputFolderPathname);
}

static ClasspathLocation forRequiredProject(String outputFolderPathname) {
	return new ClasspathDirectory(outputFolderPathname);
}

static ClasspathLocation forLibrary(String libraryPathname) {
	return new ClasspathJar(libraryPathname);
}

abstract void clear();
abstract NameEnvironmentAnswer findClass(char[] className, char[][] packageName);
abstract boolean isPackage(char[][] compoundName, char[] packageName);

void reset() {
}
}