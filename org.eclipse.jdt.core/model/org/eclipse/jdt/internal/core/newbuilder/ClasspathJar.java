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
import java.util.zip.*;

class ClasspathJar extends ClasspathLocation {

String zipFilename; // keep for equals
ZipFile zipFile;
LookupTable directoryCache;	

ClasspathJar(String zipFilename) {
	try {
		this.zipFilename = zipFilename;
		this.zipFile = new ZipFile(new File(zipFilename));
		buildDirectoryStructure();
	} catch(IOException e) {
		directoryCache = new LookupTable();
	}
}

void buildDirectoryStructure() {
	directoryCache = new LookupTable(101);
	for (Enumeration e = zipFile.entries(); e.hasMoreElements(); ) {
		String fileName = ((ZipEntry) e.nextElement()).getName();

		// extract the package name
		int last = fileName.lastIndexOf('/');
		if (last > 0 && directoryCache.get(fileName.substring(0, last)) == null) {
			// add the package name & all of its parent packages
			for (int i = 0; i <= last; i++) {
				i = fileName.indexOf('/', i);
				String packageName = fileName.substring(0, i);
				if (directoryCache.get(packageName) == null)
					directoryCache.put(packageName, packageName);
			}
		}
	}
}

void clear() {
	this.zipFile = null;
	this.directoryCache = null;
}

public boolean equals(Object o) {
	if (this == o) return true;
	if (!(o instanceof ClasspathJar)) return false;

	return zipFilename.equals(((ClasspathJar) o).zipFilename);
}

boolean isPackage(char[][] compoundName, char[] packageName) {
	return 
		directoryCache.get(
			NameEnvironment.assembleName(packageName, compoundName, '/'))
				!= null;
}

NameEnvironmentAnswer findClass(char[] className, char[][] packageName) {
	try {
		String binaryFilename =
			NameEnvironment.assembleName(new String(className) + ".class", packageName, '/'); //$NON-NLS-1$
		if (zipFile.getEntry(binaryFilename) == null) return null;

		return new NameEnvironmentAnswer(
			ClassFileReader.read(zipFile, binaryFilename));
	} catch (Exception e) {
		return null; // treat as if class file is missing
	}
}

public String toString() {
	return "Classpath for jar file " + zipFile; //$NON-NLS-1$
}
}