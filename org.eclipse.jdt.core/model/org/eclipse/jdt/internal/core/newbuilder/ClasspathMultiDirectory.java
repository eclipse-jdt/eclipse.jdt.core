package org.eclipse.jdt.internal.core.newbuilder;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;
import java.util.*;

import org.eclipse.jdt.internal.core.util.*;
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.classfmt.*;

import org.eclipse.jdt.internal.compiler.util.CharOperation;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObject;

class ClasspathMultiDirectory extends ClasspathDirectory {

String sourcePath; // includes .class files. The primary path is the source path
HashtableOfObject additionalTypeNames; // from the last state

ClasspathMultiDirectory(String sourcePath, String binaryPath, State state) {
	super(binaryPath);

	this.sourcePath = sourcePath;
	if (!sourcePath.endsWith("/")) //$NON-NLS-1$
		this.sourcePath += "/"; //$NON-NLS-1$

	additionalTypeNames = state == null ? null : state.additionalTypeNames;
}

public boolean equals(Object o) {
	if (this == o) return true;
	if (!(o instanceof ClasspathMultiDirectory)) return false;

	ClasspathMultiDirectory md = (ClasspathMultiDirectory) o;
	return binaryPath.equals(md.binaryPath)
		&& sourcePath.equals(md.sourcePath);
}

NameEnvironmentAnswer findClass(char[] className, char[][] packageName) {
	String filename = new String(className);
	String sourceFilename = filename + ".java"; //$NON-NLS-1$
	String binaryFilename = filename + ".class"; //$NON-NLS-1$

	if (!exists(binaryPath, binaryFilename, packageName)) {
		if (!exists(sourcePath, sourceFilename, packageName)) return null;
		String fullName = sourcePath + NameEnvironment.assembleName(sourceFilename, packageName, '/');
		return new NameEnvironmentAnswer(new CompilationUnit(null, fullName));
	}

	if (!exists(sourcePath, sourceFilename, packageName)) {
		// ask builder for the matching source filename for this type
		sourceFilename = findSourceFilenameFor(className, packageName);
		if (sourceFilename == null) {
			// return class file if a matching source filename doesn't exist... EXTRA class file case
			try {
				return new NameEnvironmentAnswer(
					ClassFileReader.read(binaryPath + NameEnvironment.assembleName(binaryFilename, packageName, '/')));
			} catch (Exception e) {
				return null;
			}
		}
		if (exists(sourcePath, sourceFilename, packageName)) {
			// return the source file which contains this secondary type
			String fullName = sourcePath + NameEnvironment.assembleName(sourceFilename, packageName, '/');
			return new NameEnvironmentAnswer(new CompilationUnit(null, fullName));
		}
		return null; // return null if a source filename is known but its not in this classpath entry
	}

	String fullSourceName = sourcePath + NameEnvironment.assembleName(sourceFilename, packageName, '/');
	String fullBinaryName = binaryPath + NameEnvironment.assembleName(binaryFilename, packageName, '/');
	long sourceModified = new File(fullSourceName).lastModified();
	long binaryModified = new File(fullBinaryName).lastModified();
	if (binaryModified > sourceModified) {
		try {
			return new NameEnvironmentAnswer(
				ClassFileReader.read(fullBinaryName));
		} catch (Exception e) {
		}
	}
	return new NameEnvironmentAnswer(new CompilationUnit(null, fullSourceName));
}

String findSourceFilenameFor(char[] className, char[][] packageName) {
	if (additionalTypeNames == null) return null;

	char[] secondaryTypeName = CharOperation.concatWith(className, packageName, '/');
	// keyed by filename "p1/p2/X", value is an array of additional type names "p1/p2/Y"
	Object[] valueTable = additionalTypeNames.valueTable;
	for (int i = 0, l = valueTable.length; i < l; i++) {
		char[][] typeNames = (char[][]) valueTable[i];
		if (typeNames != null) {
			for (int j = 0, k = typeNames.length; j < k; j++) {
				if (CharOperation.equals(secondaryTypeName, typeNames[j])) {
					char[] mainTypeName = additionalTypeNames.keyTable[i];
					int index = CharOperation.lastIndexOf('/', mainTypeName);
					if (index > 0)
						mainTypeName = CharOperation.subarray(mainTypeName, index + 1, mainTypeName.length);
					return new String(mainTypeName) + ".java"; //$NON-NLS-1$
				}
			}
		}
	}
	return null;
}

public String toString() {
	return "ClasspathMultiDirectory " + sourcePath; //$NON-NLS-1$
}
}