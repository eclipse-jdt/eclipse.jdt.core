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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.core.util.SimpleLookupTable;

import java.io.*;
import java.util.*;
import java.util.zip.*;

public class ClasspathJar extends ClasspathLocation {

String zipFilename; // keep for equals
IFile resource;
ZipFile zipFile;
boolean closeZipFileAtEnd;
SimpleLookupTable packageCache;

ClasspathJar(String zipFilename) {
	this.zipFilename = zipFilename;
	this.zipFile = null;
	this.packageCache = null;
}

ClasspathJar(IFile resource) {
	this.resource = resource;
	IPath location = resource.getLocation();
	this.zipFilename = location != null ? location.toString() : ""; //$NON-NLS-1$
	this.zipFile = null;
	this.packageCache = null;
}

public ClasspathJar(ZipFile zipFile) {
	this.zipFilename = zipFile.getName();
	this.zipFile = zipFile;
	this.closeZipFileAtEnd = false;
	this.packageCache = null;
}

public void cleanup() {
	if (zipFile != null && this.closeZipFileAtEnd) {
		try { zipFile.close(); } catch(IOException e) {}
		this.zipFile = null;
	}
	this.packageCache = null;
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
	} catch (Exception e) {} // treat as if class file is missing
	return null;
}

public IPath getProjectRelativePath() {
	if (resource == null) return null;
	return	resource.getProjectRelativePath();
}

public boolean isPackage(String qualifiedPackageName) {
	if (packageCache != null)
		return packageCache.containsKey(qualifiedPackageName);

	this.packageCache = new SimpleLookupTable(41);
	packageCache.put("", ""); //$NON-NLS-1$ //$NON-NLS-2$
	try {
		if (this.zipFile == null) {
			this.zipFile = new ZipFile(zipFilename);
			this.closeZipFileAtEnd = true;
		}

		nextEntry : for (Enumeration e = zipFile.entries(); e.hasMoreElements(); ) {
			String fileName = ((ZipEntry) e.nextElement()).getName();
	
			// add the package name & all of its parent packages
			int last = fileName.lastIndexOf('/');
			while (last > 0) {
				// extract the package name
				String packageName = fileName.substring(0, last);
				if (packageCache.containsKey(packageName))
					continue nextEntry;
				packageCache.put(packageName, packageName);
				last = packageName.lastIndexOf('/');
			}
		}
		return packageCache.containsKey(qualifiedPackageName);
	} catch(Exception e) {}
	return false;
}

public String toString() {
	return "Classpath jar file " + zipFilename; //$NON-NLS-1$
}
}
