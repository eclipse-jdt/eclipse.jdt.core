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
package org.eclipse.jdt.internal.core;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.util.Util;

/**
 * A basic implementation of <code>ICompilationUnit</code>
 * for use in the <code>SourceMapper</code>.
 * @see ICompilationUnit
 */
public class BasicCompilationUnit implements ICompilationUnit {
	protected char[] contents;
	protected char[] fileName;
	protected char[][] packageName;
	protected char[] mainTypeName;
	protected String encoding;

public BasicCompilationUnit(char[] contents, char[][] packageName, String fileName) {
	this.contents = contents;
	this.fileName = fileName.toCharArray();
	this.packageName = packageName;

	int start = fileName.lastIndexOf("/") + 1; //$NON-NLS-1$
	if (start == 0 || start < fileName.lastIndexOf("\\")) //$NON-NLS-1$
		start = fileName.lastIndexOf("\\") + 1; //$NON-NLS-1$

	int end = fileName.lastIndexOf("."); //$NON-NLS-1$
	if (end == -1)
		end = fileName.length();

	this.mainTypeName = fileName.substring(start, end).toCharArray();
	this.encoding = null;
}

public BasicCompilationUnit(char[] contents, char[][] packageName, String fileName, String encoding) {
	this(contents, packageName, fileName);
	this.encoding = encoding;
}

public BasicCompilationUnit(char[] contents, char[][] packageName, String fileName, IJavaElement javaElement) {
	this(contents, packageName, fileName);
	initEncoding(javaElement);
}

/*
 * Initialize compilation unit encoding.
 * If we have a project, then get file name corresponding IFile and retrieve its encoding using
 * new API for encoding.
 * In case of a class file, then go through project in order to let the possibility to retrieve
 * a corresponding source file resource.
 * If we have a compilation unit, then get encoding from its resource directly...
 */
private void initEncoding(IJavaElement javaElement) {
	if (javaElement != null) {
		try {
			IJavaProject javaProject = javaElement.getJavaProject();
			switch (javaElement.getElementType()) {
				case IJavaElement.COMPILATION_UNIT:
					IFile file = (IFile) javaElement.getResource();
					if (file != null) {
						this.encoding = file.getCharset();
						break;
					}
					// if no file, then get project encoding
				default:
					IProject project = (IProject) javaProject.getResource();
					if (project != null) {
						this.encoding = project.getDefaultCharset();
					}
					break;
			}
		} catch (CoreException e1) {
			this.encoding = null;
		}
	} else  {
		this.encoding = null;
	}
}

public char[] getContents() {
	if (this.contents != null)
		return this.contents;   // answer the cached source

	// otherwise retrieve it
	try {
		return Util.getFileCharContent(new File(new String(this.fileName)), this.encoding);
	} catch (IOException e) {
		// could not read file: returns an empty array
	}
	return CharOperation.NO_CHAR;
}
public char[] getFileName() {
	return this.fileName;
}
public char[] getMainTypeName() {
	return this.mainTypeName;
}
public char[][] getPackageName() {
	return this.packageName;
}
public String toString(){
	return "CompilationUnit: "+new String(this.fileName); //$NON-NLS-1$
}
}
