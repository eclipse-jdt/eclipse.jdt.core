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
package org.eclipse.jdt.internal.core.search.matching;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.core.Util;
import org.eclipse.jdt.internal.core.builder.ClasspathLocation;
import org.eclipse.jdt.internal.core.util.SimpleLookupTable;

public class ClasspathSourceDirectory extends ClasspathLocation {

	IContainer sourceFolder;
	String sourceLocation; 
	String encoding;
	SimpleLookupTable directoryCache;
	String[] missingPackageHolder = new String[1];

ClasspathSourceDirectory(IContainer sourceFolder, String encoding) {
	this.sourceFolder = sourceFolder;
	IPath location = sourceFolder.getLocation();
	this.sourceLocation = location != null ? location.addTrailingSeparator().toString() : ""; //$NON-NLS-1$
	this.encoding = encoding;	
	this.directoryCache = new SimpleLookupTable(5);
}

public void cleanup() {
	this.directoryCache = null;
}

String[] directoryList(String qualifiedPackageName) {
	String[] dirList = (String[]) directoryCache.get(qualifiedPackageName);
	if (dirList == missingPackageHolder) return null; // package exists in another classpath directory or jar
	if (dirList != null) return dirList;

	try {
		IResource container = sourceFolder.findMember(qualifiedPackageName); // this is a case-sensitive check
		if (container instanceof IContainer) {
			IResource[] members = ((IContainer) container).members();
			dirList = new String[members.length];
			int index = 0;
			for (int i = 0, l = members.length; i < l; i++) {
				IResource m = members[i];
				String name;
				if (m.getType() == IResource.FILE && Util.isJavaFileName(name = m.getName()))
					dirList[index++] = name;
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

boolean doesFileExist(String fileName, String qualifiedPackageName) {
	String[] dirList = directoryList(qualifiedPackageName);
	if (dirList == null) return false; // most common case

	for (int i = dirList.length; --i >= 0;)
		if (fileName.equals(dirList[i]))
			return true;
	return false;
}

public boolean equals(Object o) {
	if (this == o) return true;
	if (!(o instanceof ClasspathSourceDirectory)) return false;

	return sourceFolder.equals(((ClasspathSourceDirectory) o).sourceFolder);
} 

public NameEnvironmentAnswer findClass(String sourceFileName, String qualifiedPackageName, String qualifiedSourceFileName) {
	if (!doesFileExist(sourceFileName, qualifiedPackageName)) return null; // most common case

	String fullSourcePath = this.sourceLocation + qualifiedSourceFileName;
	return new NameEnvironmentAnswer(new CompilationUnit(null, fullSourcePath, this.encoding));
}

public IPath getProjectRelativePath() {
	return sourceFolder.getProjectRelativePath();
}

public boolean isPackage(String qualifiedPackageName) {
	return directoryList(qualifiedPackageName) != null;
}

public void reset() {
	this.directoryCache = new SimpleLookupTable(5);
}

public String toString() {
	return "Source classpath directory " + sourceFolder.getFullPath().toString(); //$NON-NLS-1$
}
}
