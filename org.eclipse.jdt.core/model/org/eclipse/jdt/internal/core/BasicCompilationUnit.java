package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.util.Util;
import java.io.*;

/**
 * A basic implementation of <code>ICompilationUnit</code>
 * for use in the <code>SourceMapper</code>.
 * @see ICompilationUnit
 */
public class BasicCompilationUnit implements ICompilationUnit {
	protected char[] contents;
	protected char[] fileName;
	protected char[] mainTypeName;
	protected String encoding;
	
public BasicCompilationUnit(char[] contents, String fileName, String encoding) {
	this.contents = contents;
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
		return Util.getFileCharContent(new File(new String(fileName)), this.encoding);
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
public String toString(){
	return "CompilationUnit: "+new String(fileName); //$NON-NLS-1$
}
}
