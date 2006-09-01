/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
import java.util.Hashtable;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.parser.ScannerHelper;

public class ClasspathDirectory extends ClasspathLocation {

private char[] normalizedPath;
private String path;
private Hashtable directoryCache;
private String[] missingPackageHolder = new String[1];
private int mode; // ability to only consider one kind of files (source vs. binaries), by default use both
private String encoding; // only useful if referenced in the source path

ClasspathDirectory(File directory, String encoding, int mode, 
		AccessRuleSet accessRuleSet, String destinationPath) {
	super(accessRuleSet, destinationPath);
	this.mode = mode;
	this.path = directory.getAbsolutePath();
	if (!this.path.endsWith(File.separator))
		this.path += File.separator;
	this.directoryCache = new Hashtable(11);
	this.encoding = encoding;
}
String[] directoryList(String qualifiedPackageName) {
	String[] dirList = (String[]) this.directoryCache.get(qualifiedPackageName);
	if (dirList == this.missingPackageHolder) return null; // package exists in another classpath directory or jar
	if (dirList != null) return dirList;

	File dir = new File(this.path + qualifiedPackageName);
	notFound : if (dir != null && dir.isDirectory()) {
		// must protect against a case insensitive File call
		// walk the qualifiedPackageName backwards looking for an uppercase character before the '/'
		int index = qualifiedPackageName.length();
		int last = qualifiedPackageName.lastIndexOf(File.separatorChar);
		while (--index > last && !ScannerHelper.isUpperCase(qualifiedPackageName.charAt(index))){/*empty*/}
		if (index > last) {
			if (last == -1) {
				if (!doesFileExist(qualifiedPackageName, ""))  //$NON-NLS-1$ 
					break notFound;
			} else {
				String packageName = qualifiedPackageName.substring(last + 1);
				String parentPackage = qualifiedPackageName.substring(0, last);
				if (!doesFileExist(packageName, parentPackage))
					break notFound;
			}
		}
		if ((dirList = dir.list()) == null)
			dirList = new String[0];
		this.directoryCache.put(qualifiedPackageName, dirList);
		return dirList;
	}
	this.directoryCache.put(qualifiedPackageName, this.missingPackageHolder);
	return null;
}
boolean doesFileExist(String fileName, String qualifiedPackageName) {
	String[] dirList = directoryList(qualifiedPackageName);
	if (dirList == null) return false; // most common case

	for (int i = dirList.length; --i >= 0;)
		if (fileName.equals(dirList[i]))
			return true;
	return false;
}
public NameEnvironmentAnswer findClass(char[] typeName, String qualifiedPackageName, String qualifiedBinaryFileName) {
	if (!isPackage(qualifiedPackageName)) return null; // most common case

	String fileName = new String(typeName);
	boolean binaryExists = ((this.mode & BINARY) != 0) && doesFileExist(fileName + SUFFIX_STRING_class, qualifiedPackageName);
	boolean sourceExists = ((this.mode & SOURCE) != 0) && doesFileExist(fileName + SUFFIX_STRING_java, qualifiedPackageName);
	if (sourceExists) {
		String fullSourcePath = this.path + qualifiedBinaryFileName.substring(0, qualifiedBinaryFileName.length() - 6)  + SUFFIX_STRING_java;
		if (!binaryExists)
			return new NameEnvironmentAnswer(new CompilationUnit(null,
					fullSourcePath, this.encoding, destinationPath),
					fetchAccessRestriction(qualifiedBinaryFileName));
		String fullBinaryPath = this.path + qualifiedBinaryFileName;
		long binaryModified = new File(fullBinaryPath).lastModified();
		long sourceModified = new File(fullSourcePath).lastModified();
		if (sourceModified > binaryModified)
			return new NameEnvironmentAnswer(new CompilationUnit(null,
					fullSourcePath, this.encoding, destinationPath),
					fetchAccessRestriction(qualifiedBinaryFileName));
	}
	if (binaryExists) {
		try {
			ClassFileReader reader = ClassFileReader.read(this.path + qualifiedBinaryFileName);
			if (reader != null)
				return new NameEnvironmentAnswer(
						reader,
						fetchAccessRestriction(qualifiedBinaryFileName));
		} catch (Exception e) {
			// treat as if file is missing
		}
	}
	return null;
}
public void initialize() throws IOException {
	// nothing to do
}
public boolean isPackage(String qualifiedPackageName) {
	return directoryList(qualifiedPackageName) != null;
}
public void reset() {
	this.directoryCache = new Hashtable(11);
}
public String toString() {
	return "ClasspathDirectory " + this.path; //$NON-NLS-1$
}
public char[] normalizedPath() {
	if (this.normalizedPath == null) {
		this.normalizedPath = this.path.toCharArray();
		if (File.separatorChar == '\\') {
			CharOperation.replace(this.normalizedPath, '\\', '/');
		}
	}
	return this.normalizedPath;
}
public String getPath() {
	return this.path;
}
}
