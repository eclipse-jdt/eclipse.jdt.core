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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.core.util.CharArrayBuffer;

import java.io.*;

public class SourceFile implements ICompilationUnit {

IFile resource;
ClasspathMultiDirectory sourceLocation;
String initialTypeName;
String encoding;

public SourceFile(IFile resource, ClasspathMultiDirectory sourceLocation, String encoding) {
	this.resource = resource;
	this.sourceLocation = sourceLocation;
	this.initialTypeName = extractTypeName();
	this.encoding = encoding;
}

public boolean equals(Object o) {
	if (this == o) return true;
	if (!(o instanceof SourceFile)) return false;

	SourceFile f = (SourceFile) o;
	return sourceLocation == f.sourceLocation && resource.getFullPath().equals(f.resource.getFullPath());
} 

String extractTypeName() {
	// answer a String with the qualified type name for the source file in the form: 'p1/p2/A'
	IPath fullPath = resource.getFullPath();
	int resourceSegmentCount = fullPath.segmentCount();
	int sourceFolderSegmentCount = sourceLocation.sourceFolder.getFullPath().segmentCount();
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
	// otherwise retrieve it
	InputStreamReader reader = null;
	try {
		reader =
			this.encoding == null
				? new InputStreamReader(resource.getContents())
				: new InputStreamReader(resource.getContents(), this.encoding);
		CharArrayBuffer result = new CharArrayBuffer();
		try {
			int count;
			char[] buffer = new char[4096];
			while ((count = reader.read(buffer, 0, buffer.length)) > -1)
				result.append(buffer, 0, count);
		} finally {
			reader.close();
		}
		return result.getContents();
	} catch (CoreException e) {
		throw new AbortCompilation(true, new MissingSourceFileException(resource.getFullPath().toString()));
	} catch (IOException e) {
		if (reader != null) {
			try { reader.close(); } catch(IOException ioe) {}
		}
		throw new AbortCompilation(true, new MissingSourceFileException(resource.getFullPath().toString()));
	}
}

public char[] getFileName() {
	return resource.getFullPath().toString().toCharArray(); // do not know what you want to return here
}

public char[] getMainTypeName() {
	char[] typeName = initialTypeName.toCharArray();
	int lastIndex = CharOperation.lastIndexOf('/', typeName);
	return CharOperation.subarray(typeName, lastIndex + 1, -1);
}

public char[][] getPackageName() {
	char[] typeName = initialTypeName.toCharArray();
	int lastIndex = CharOperation.lastIndexOf('/', typeName);
	return CharOperation.splitOn('/', typeName, 0, lastIndex);
}

String typeLocator() {
	return resource.getProjectRelativePath().toString();
}

public String toString() {
	return "SourceFile[" //$NON-NLS-1$
		+ resource.getFullPath() + "]";  //$NON-NLS-1$
}
}