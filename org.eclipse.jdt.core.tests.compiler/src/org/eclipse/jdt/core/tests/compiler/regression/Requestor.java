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
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import junit.framework.Assert;

import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IProblemFactory;

public class Requestor extends Assert implements ICompilerRequestor {
	public boolean hasErrors = false;
	public IProblemFactory problemFactory;
	public String outputPath;
	private boolean generateOutput;
	public Hashtable expectedProblems = new Hashtable();
	public String problemLog = "";
	public ICompilerRequestor clientRequestor;
	public boolean showCategory = false;
	public boolean showWarningToken = false;
	
public Requestor(IProblemFactory problemFactory, String outputPath, boolean generateOutput, ICompilerRequestor clientRequestor, boolean showCategory, boolean showWarningToken) {
	this.problemFactory = problemFactory;
	this.outputPath = outputPath;
	this.generateOutput = generateOutput;
	this.clientRequestor = clientRequestor;
	this.showCategory = showCategory;
	this.showWarningToken = showWarningToken;
}
public void acceptResult(CompilationResult compilationResult) {
	this.hasErrors |= compilationResult.hasErrors();
	this.problemLog += Util.getProblemLog(compilationResult, this.showCategory, this.showWarningToken);
	outputClassFiles(compilationResult);
	if (this.clientRequestor != null) {
		this.clientRequestor.acceptResult(compilationResult);
	}
}
protected void outputClassFiles(CompilationResult unitResult) {
	if ((unitResult != null) && (!unitResult.hasErrors() || generateOutput)) {
		ClassFile[]classFiles = unitResult.getClassFiles();
		if (outputPath != null) {
			for (int i = 0, fileCount = classFiles.length; i < fileCount; i++) {
				// retrieve the key and the corresponding classfile
				ClassFile classFile = classFiles[i];
				String relativeName = 
					new String(classFile.fileName()).replace('/', File.separatorChar) + ".class";
				try {
					ClassFile.writeToDisk(true, outputPath, relativeName, classFile);
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
}
