/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.core.builder;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;

class ClasspathDirectory extends ClasspathLocation {

IContainer binaryFolder; // includes .class files for a single directory
boolean isOutputFolder;
String binaryLocation;
SimpleLookupTable directoryCache;
String[] missingPackageHolder = new String[1];

ClasspathDirectory(IContainer binaryFolder, boolean isOutputFolder) {
	this.binaryFolder = binaryFolder;
	this.isOutputFolder = isOutputFolder;
	this.binaryLocation = binaryFolder.getLocation().addTrailingSeparator().toString();

	this.directoryCache = new SimpleLookupTable(5);
}

void cleanup() {
	this.directoryCache = null;
}

String[] directoryList(String qualifiedPackageName) {
	String[] dirList = (String[]) directoryCache.get(qualifiedPackageName);
	if (dirList == missingPackageHolder) return null; // package exists in another classpath directory or jar
	if (dirList != null) return dirList;

	try {
		IResource folder = binaryFolder.findMember(qualifiedPackageName); // this is a case-sensitive check
		if (folder != null && folder.getType() == IResource.FOLDER) {
			IResource[] members = ((IFolder) folder).members();
			dirList = new String[members.length];
			int index = 0;
			for (int i = 0, l = members.length; i < l; i++) {
				IResource m = members[i];
				if (m.getType() == IResource.FILE && JavaBuilder.CLASS_EXTENSION.equalsIgnoreCase(m.getFileExtension()))
					// add exclusion pattern check here if we want to hide .class files
					dirList[index++] = m.getName();
			}
			if (index < dirList.length)
				System.arraycopy(dirList, 0, dirList = new String[index], 0, index);
			directoryCache.put(qualifiedPackageName, dirList);
			return dirList;
		}
	} catch(CoreException ignored) {
	}
	directoryCache.put(qualifiedPackageName, missingPackageHolder);
	return null;
}

boolean doesFileExist(String fileName, String qualifiedPackageName, String qualifiedFullName) {
	String[] dirList = directoryList(qualifiedPackageName);
	if (dirList == null) return false; // most common case

	for (int i = dirList.length; --i >= 0;)
		if (fileName.equals(dirList[i]))
			return true;
	return false;
}

public boolean equals(Object o) {
	if (this == o) return true;
	if (!(o instanceof ClasspathDirectory)) return false;

	return binaryFolder.equals(((ClasspathDirectory) o).binaryFolder);
} 

NameEnvironmentAnswer findClass(String binaryFileName, String qualifiedPackageName, String qualifiedBinaryFileName) {
	if (!doesFileExist(binaryFileName, qualifiedPackageName, qualifiedBinaryFileName)) return null; // most common case

	try {
		ClassFileReader reader = ClassFileReader.read(binaryLocation + qualifiedBinaryFileName);
		if (reader != null) return new NameEnvironmentAnswer(reader);
	} catch (Exception e) {} // treat as if class file is missing
	return null;
}

IPath getRelativePath() {
	return binaryFolder.getProjectRelativePath();
}

boolean isOutputFolder() {
	return isOutputFolder;
}

boolean isPackage(String qualifiedPackageName) {
	return directoryList(qualifiedPackageName) != null;
}

void reset() {
	this.directoryCache = new SimpleLookupTable(5);
}

public String toString() {
	return "Binary classpath directory " + binaryFolder.getFullPath().toString(); //$NON-NLS-1$
}
}