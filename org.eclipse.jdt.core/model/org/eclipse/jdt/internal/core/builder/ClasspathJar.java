package org.eclipse.jdt.internal.core.builder;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;

import java.io.*;
import java.util.*;
import java.util.zip.*;

class ClasspathJar extends ClasspathLocation {

String zipFilename; // keep for equals
ZipFile zipFile;
SimpleLookupTable directoryCache;	

ClasspathJar(String zipFilename) {
	this.zipFilename = zipFilename;
	this.zipFile = null;
	this.directoryCache = null;
}

void buildDirectoryStructure() {
	this.directoryCache = new SimpleLookupTable(101);

	try {
		this.zipFile = new ZipFile(zipFilename);
	} catch(IOException e) {
		return;
	}
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

void cleanup() {
	if (zipFile != null) {
		try { zipFile.close(); } catch(IOException e) {}
	}
	this.zipFile = null;
	this.directoryCache = null;
}

public boolean equals(Object o) {
	if (this == o) return true;
	if (!(o instanceof ClasspathJar)) return false;

	return zipFilename.equals(((ClasspathJar) o).zipFilename);
} 

NameEnvironmentAnswer findClass(char[] className, char[][] packageName) {
	if (directoryCache == null) buildDirectoryStructure();
	try {
		String binaryFilename =
			NameEnvironment.assembleName(new String(className) + ".class", packageName, '/'); //$NON-NLS-1$
		if (zipFile.getEntry(binaryFilename) == null) return null;

		return new NameEnvironmentAnswer(ClassFileReader.read(zipFile, binaryFilename));
	} catch (Exception e) {
		return null; // treat as if class file is missing
	}
}

boolean isPackage(char[][] compoundName, char[] packageName) {
	if (directoryCache == null) buildDirectoryStructure();
	return
		directoryCache.get(
			NameEnvironment.assembleName(packageName, compoundName, '/'))
				!= null;
}

public String toString() {
	return "Classpath jar file " + zipFilename; //$NON-NLS-1$
}
}