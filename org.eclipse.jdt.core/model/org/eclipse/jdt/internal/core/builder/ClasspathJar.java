/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tal Lev-Ami - added package cache for zip files
 *******************************************************************************/
package org.eclipse.jdt.internal.core.builder;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.core.util.SimpleLookupTable;
import org.eclipse.jdt.internal.core.util.SimpleSet;

import java.io.*;
import java.util.*;
import java.util.zip.*;

public class ClasspathJar extends ClasspathLocation {

static class PackageCacheEntry {
	long lastModified;
	long fileSize;
	SimpleSet packageSet;
	
	PackageCacheEntry(long lastModified, long fileSize, SimpleSet packageSet) {
		this.lastModified = lastModified;
		this.fileSize = fileSize;
		this.packageSet = packageSet;
	}
}

static SimpleLookupTable PackageCache = new SimpleLookupTable();

/**
 * Calculate and cache the package list available in the zipFile.
 * @param zipFile The zip file to use
 * @return A SimpleSet with the all the package names in the zipFile.
 */
static SimpleSet findPackageSet(ZipFile zipFile) {
	String zipFileName = zipFile.getName();
	File zipFileObject = new File(zipFileName);
	long lastModified = zipFileObject.lastModified();
	long fileSize = zipFileObject.length();
	PackageCacheEntry cacheEntry = (PackageCacheEntry) PackageCache.get(zipFileName);
	if (cacheEntry != null && cacheEntry.lastModified == lastModified && cacheEntry.fileSize == fileSize)
		return cacheEntry.packageSet;

	SimpleSet packageSet = new SimpleSet(41);
	packageSet.add(""); //$NON-NLS-1$
	nextEntry : for (Enumeration e = zipFile.entries(); e.hasMoreElements(); ) {
		String fileName = ((ZipEntry) e.nextElement()).getName();

		// add the package name & all of its parent packages
		int last = fileName.lastIndexOf('/');
		while (last > 0) {
			// extract the package name
			String packageName = fileName.substring(0, last);
			if (packageSet.includes(packageName))
				continue nextEntry;
			packageSet.add(packageName);
			last = packageName.lastIndexOf('/');
		}
	}

	PackageCache.put(zipFileName, new PackageCacheEntry(lastModified, fileSize, packageSet));
	return packageSet;
}


String zipFilename; // keep for equals
IFile resource;
ZipFile zipFile;
boolean closeZipFileAtEnd;
SimpleSet knownPackageNames;

ClasspathJar(String zipFilename) {
	this.zipFilename = zipFilename;
	this.zipFile = null;
	this.knownPackageNames = null;
}

ClasspathJar(IFile resource) {
	this.resource = resource;
	IPath location = resource.getLocation();
	this.zipFilename = location != null ? location.toString() : ""; //$NON-NLS-1$
	this.zipFile = null;
	this.knownPackageNames = null;
}

public ClasspathJar(ZipFile zipFile) {
	this.zipFilename = zipFile.getName();
	this.zipFile = zipFile;
	this.closeZipFileAtEnd = false;
	this.knownPackageNames = null;
}

public void cleanup() {
	if (zipFile != null && this.closeZipFileAtEnd) {
		try {
			zipFile.close();
		} catch(IOException e) { // ignore it
		}
		this.zipFile = null;
	}
	this.knownPackageNames = null;
}

public boolean equals(Object o) {
	if (this == o) return true;
	if (!(o instanceof ClasspathJar)) return false;

	return zipFilename.equals(((ClasspathJar) o).zipFilename);
} 

public NameEnvironmentAnswer findClass(String binaryFileName, String qualifiedPackageName, String qualifiedBinaryFileName) {
	if (!isPackage(qualifiedPackageName)) return null; // most common case

	try {
		ClassFileReader reader = ClassFileReader.read(zipFile, qualifiedBinaryFileName);
		if (reader != null) return new NameEnvironmentAnswer(reader);
	} catch (Exception e) { // treat as if class file is missing
	}
	return null;
}

public IPath getProjectRelativePath() {
	if (resource == null) return null;
	return	resource.getProjectRelativePath();
}

public boolean isPackage(String qualifiedPackageName) {
	if (knownPackageNames != null)
		return knownPackageNames.includes(qualifiedPackageName);

	try {
		if (this.zipFile == null) {
			this.zipFile = new ZipFile(zipFilename);
			this.closeZipFileAtEnd = true;
		}
		this.knownPackageNames = findPackageSet(zipFile);
	} catch(Exception e) {
		this.knownPackageNames = new SimpleSet(); // assume for this build the zipFile is empty
	}
	return knownPackageNames.includes(qualifiedPackageName);
}

public String toString() {
	return "Classpath jar file " + zipFilename; //$NON-NLS-1$
}
}
