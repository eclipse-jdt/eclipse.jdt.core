package org.eclipse.jdt.internal.compiler.batch;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;

import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.util.Util;

public class CompilationUnit implements ICompilationUnit {
	public char[] contents;
	public char[] fileName;
	public char[] mainTypeName;
	String encoding;
	
public CompilationUnit(char[] contents, String fileName, String encoding) {
	this.contents = contents;
	if (File.separator.equals("/")) { //$NON-NLS-1$
		if (fileName.indexOf("\\") != -1) { //$NON-NLS-1$
			fileName = fileName.replace('\\', File.separatorChar);
		}
	} else {
		// the file separator is \
		if (fileName.indexOf('/') != -1) {
			fileName = fileName.replace('/', File.separatorChar);
		}
	}
	this.fileName = fileName.toCharArray();

	int start = fileName.lastIndexOf("/") + 1; //$NON-NLS-1$
	if (start == 0 || start < fileName.lastIndexOf("\\")) //$NON-NLS-1$
		start = fileName.lastIndexOf("\\") + 1; //$NON-NLS-1$

	int end = fileName.lastIndexOf("."); //$NON-NLS-1$
	if (end == -1)
		end = fileName.length();

	this.mainTypeName = fileName.substring(start, end).toCharArray();
	this.encoding = encoding;
}
public char[] getContents() {
	if (contents != null)
		return contents;   // answer the cached source

	// otherwise retrieve it
	try {
		return Util.getFileCharContent(new File(new String(fileName)), encoding);
	} catch (IOException e) {
	}
	return new char[0];
}
public char[] getFileName() {
	return fileName;
}
public char[] getMainTypeName() {
	return mainTypeName;
}
public char[][] getPackageName() {
	return null;
}
public String toString() {
	return "CompilationUnit[" + new String(fileName) + "]";  //$NON-NLS-2$ //$NON-NLS-1$
}
}
