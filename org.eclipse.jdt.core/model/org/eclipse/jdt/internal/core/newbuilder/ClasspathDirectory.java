package org.eclipse.jdt.internal.core.newbuilder;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jdt.internal.core.util.LookupTable;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;

import java.io.*;
import java.util.*;

class ClasspathDirectory extends ClasspathLocation {

String binaryPath; // includes .class files for a single directory
LookupTable missingPackages;
LookupTable directoryCache;

ClasspathDirectory(String binaryPath) {
	this.binaryPath = binaryPath;
	if (!binaryPath.endsWith("/")) //$NON-NLS-1$
		this.binaryPath += "/"; //$NON-NLS-1$

	this.missingPackages = new LookupTable(11);
	this.directoryCache = new LookupTable(11);
}

void clear() {
	this.missingPackages = null;
	this.directoryCache = null;
}

String[] directoryList(String pathPrefix, char[][] compoundName, char[] packageName) {
	String partialPath = NameEnvironment.assembleName(packageName, compoundName, '/');
	if (missingPackages.containsKey(partialPath)) return null;

	String fullPath = pathPrefix + partialPath;
	String[] dirList = (String[]) directoryCache.get(fullPath);
	if (dirList != null) return dirList;

	File dir = new File(fullPath);
	if (dir != null && dir.isDirectory()) {
		boolean matchesName = packageName == null;
		if (!matchesName) {
			int index = packageName.length;
			while (--index >= 0 && !Character.isUpperCase(packageName[index])) {}
			matchesName = index < 0 || exists(pathPrefix, new String(packageName), compoundName);
		}
		if (matchesName) {
			if ((dirList = dir.list()) == null)
				dirList = new String[0];
			directoryCache.put(fullPath, dirList);
			return dirList;
		}
	}
	missingPackages.put(partialPath, partialPath); // value is not used
	return null;
}

public boolean equals(Object o) {
	if (this == o) return true;
	if (!(o instanceof ClasspathDirectory)) return false;

	return binaryPath.equals(((ClasspathDirectory) o).binaryPath);
}

boolean exists(String pathPrefix, String filename, char[][] packageName) {
	String[] dirList = directoryList(pathPrefix, packageName, null);
	if (dirList != null)
		for (int i = dirList.length; --i >= 0;)
			if (filename.equals(dirList[i]))
				return true;
	return false;
}

NameEnvironmentAnswer findClass(char[] className, char[][] packageName) {
	String binaryFilename = new String(className) + ".class"; //$NON-NLS-1$
	if (exists(binaryPath, binaryFilename, packageName)) {
		try {
			return new NameEnvironmentAnswer(
				ClassFileReader.read(binaryPath + NameEnvironment.assembleName(binaryFilename, packageName, '/')));
		} catch (Exception e) {
		}
	}
	return null;
}

boolean isPackage(char[][] compoundName, char[] packageName) {
	return directoryList(binaryPath, compoundName, packageName) != null;
}

void reset() {
	this.missingPackages = new LookupTable(11);
	this.directoryCache = new LookupTable(11);
}

public String toString() {
	return "ClasspathDirectory " + binaryPath; //$NON-NLS-1$
}
}