package org.eclipse.jdt.internal.core.builder;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

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

abstract NameEnvironmentAnswer findClass(char[] className, char[][] packageName);
abstract boolean isPackage(char[][] compoundName, char[] packageName);

// free anything which is not required when the state is saved
void cleanup() {
}
// reset any internal caches before another compile loop starts
void reset() {
}
}