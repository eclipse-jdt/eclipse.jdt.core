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
package org.eclipse.jdt.internal.compiler.batch;

import java.io.File;
import java.io.IOException;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
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
	if (this.contents != null)
		return this.contents;   // answer the cached source

	// otherwise retrieve it
	try {
		return Util.getFileCharContent(new File(new String(this.fileName)), this.encoding);
	} catch (IOException e) {
		// assume no content then
	}
	return CharOperation.NO_CHAR;
}
/**
 * @see org.eclipse.jdt.internal.compiler.env.IDependent#getFileName()
 */
public char[] getFileName() {
	return this.fileName;
}
public char[] getMainTypeName() {
	return this.mainTypeName;
}
public char[][] getPackageName() {
	return null;
}
public String toString() {
	return "CompilationUnit[" + new String(this.fileName) + "]";  //$NON-NLS-2$ //$NON-NLS-1$
}
}
