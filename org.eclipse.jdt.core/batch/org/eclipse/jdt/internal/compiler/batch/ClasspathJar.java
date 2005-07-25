/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.batch;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;

public class ClasspathJar extends ClasspathLocation {
	
private File file;
private ZipFile zipFile;
private boolean closeZipFileAtEnd;
private Hashtable packageCache;

public ClasspathJar(File file) throws IOException {
	this(file, true, null);
}
public ClasspathJar(File file, boolean closeZipFileAtEnd, AccessRuleSet accessRuleSet) {
	super(accessRuleSet);
	this.file = file;
	this.closeZipFileAtEnd = closeZipFileAtEnd;
}

public NameEnvironmentAnswer findClass(char[] typeName, String qualifiedPackageName, String qualifiedBinaryFileName) {
	if (!isPackage(qualifiedPackageName)) 
		return null; // most common case

	try {
		ClassFileReader reader = ClassFileReader.read(this.zipFile, qualifiedBinaryFileName);
		if (reader != null) return new NameEnvironmentAnswer(reader, 
				fetchAccessRestriction(qualifiedBinaryFileName));
	} catch (Exception e) {
		// treat as if class file is missing
	}
	return null;
}
public void initialize() throws IOException {
	this.zipFile = new ZipFile(this.file);
}
public boolean isPackage(String qualifiedPackageName) {
	if (this.packageCache != null)
		return this.packageCache.containsKey(qualifiedPackageName);

	this.packageCache = new Hashtable(41);
	this.packageCache.put("", ""); //$NON-NLS-1$ //$NON-NLS-2$

	nextEntry : for (Enumeration e = this.zipFile.entries(); e.hasMoreElements(); ) {
		String fileName = ((ZipEntry) e.nextElement()).getName();

		// add the package name & all of its parent packages
		int last = fileName.lastIndexOf('/');
		while (last > 0) {
			// extract the package name
			String packageName = fileName.substring(0, last);
			if (this.packageCache.containsKey(packageName))
				continue nextEntry;
			this.packageCache.put(packageName, packageName);
			last = packageName.lastIndexOf('/');
		}
	}
	return this.packageCache.containsKey(qualifiedPackageName);
}
public void reset() {
	if (this.zipFile != null && this.closeZipFileAtEnd) {
		try { 
			this.zipFile.close();
		} catch(IOException e) {
			// ignore
		}
		this.zipFile = null;
	}
	this.packageCache = null;
}
public String toString() {
	return "Classpath for jar file " + this.zipFile.getName(); //$NON-NLS-1$
}
public String normalizedPath(){
	String rawName = this.zipFile.getName();
	return rawName.substring(0, rawName.lastIndexOf('.'));
}
}
