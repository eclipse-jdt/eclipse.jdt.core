/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.core.util.Util;

public class SourceFile implements ICompilationUnit {

IFile resource;
ClasspathMultiDirectory sourceLocation;
String initialTypeName;
boolean updateClassFile;

public SourceFile(IFile resource, ClasspathMultiDirectory sourceLocation) {
	this.resource = resource;
	this.sourceLocation = sourceLocation;
	this.initialTypeName = extractTypeName();
	this.updateClassFile = false;
}

public SourceFile(IFile resource, ClasspathMultiDirectory sourceLocation, boolean updateClassFile) {
	this(resource, sourceLocation);

	this.updateClassFile = updateClassFile;
}

public boolean equals(Object o) {
	if (this == o) return true;
	if (!(o instanceof SourceFile)) return false;

	SourceFile f = (SourceFile) o;
	return this.sourceLocation == f.sourceLocation && this.resource.getFullPath().equals(f.resource.getFullPath());
} 

String extractTypeName() {
	// answer a String with the qualified type name for the source file in the form: 'p1/p2/A'
	IPath fullPath = this.resource.getFullPath();
	int resourceSegmentCount = fullPath.segmentCount();
	int sourceFolderSegmentCount = this.sourceLocation.sourceFolder.getFullPath().segmentCount();
	int charCount = (resourceSegmentCount - sourceFolderSegmentCount - 1) - 5; // length of ".java"
	for (int i = sourceFolderSegmentCount; i < resourceSegmentCount; i++)
		charCount += fullPath.segment(i).length();

	char[] result = new char[charCount];
	int offset = 0;
	resourceSegmentCount--; // deal with the last segment separately
	for (int i = sourceFolderSegmentCount; i < resourceSegmentCount; i++) {
		String segment = fullPath.segment(i);
		int size = segment.length();
		segment.getChars(0, size, result, offset);
		offset += size;
		result[offset++] = '/';
	}
	String segment = fullPath.segment(resourceSegmentCount);
	int size = segment.length() - 5; // length of ".java"
	segment.getChars(0, size, result, offset);
	return new String(result);
}

public char[] getContents() {

	try {	
		return Util.getResourceContentsAsCharArray(this.resource);
	} catch (CoreException e) {
		throw new AbortCompilation(true, new MissingSourceFileException(this.resource.getFullPath().toString()));
	}
}

public char[] getFileName() {
	return this.resource.getFullPath().toString().toCharArray(); // do not know what you want to return here
}

public char[] getMainTypeName() {
	char[] typeName = this.initialTypeName.toCharArray();
	int lastIndex = CharOperation.lastIndexOf('/', typeName);
	return CharOperation.subarray(typeName, lastIndex + 1, -1);
}

public char[][] getPackageName() {
	char[] typeName = this.initialTypeName.toCharArray();
	int lastIndex = CharOperation.lastIndexOf('/', typeName);
	return CharOperation.splitOn('/', typeName, 0, lastIndex);
}

String typeLocator() {
	return this.resource.getProjectRelativePath().toString();
}

public String toString() {
	return "SourceFile[" //$NON-NLS-1$
		+ this.resource.getFullPath() + "]";  //$NON-NLS-1$
}
}
