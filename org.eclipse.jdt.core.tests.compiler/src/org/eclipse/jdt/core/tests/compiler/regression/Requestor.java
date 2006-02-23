/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblem;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

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
	StringBuffer buffer = new StringBuffer(100);
	hasErrors |= compilationResult.hasErrors();
	if (compilationResult.hasProblems() || compilationResult.hasTasks()) {
		CategorizedProblem[] problems = compilationResult.getAllProblems();
		int count = problems.length;
		int problemCount = 0;
		char[] unitSource = compilationResult.compilationUnit.getContents();
		for (int i = 0; i < count; i++) { 
			DefaultProblem problem = (DefaultProblem) problems[i];
			if (problem != null) {
				if (problemCount == 0)
					buffer.append("----------\n");
				problemCount++;
				buffer.append(problemCount + (problem.isError() ? ". ERROR" : ". WARNING"));
				buffer.append(" in " + new String(problem.getOriginatingFileName()).replace('/', '\\'));
				try {
					buffer.append(problem.errorReportSource(unitSource));
					buffer.append("\n");
					if (showCategory) {
						String category = problem.getInternalCategoryMessage();
						if (category != null) {
							buffer.append("[@cat:").append(category).append("] ");
						}
					}
					if (showWarningToken) {
						long irritant = ProblemReporter.getIrritant(problem.getID());
						if (irritant != 0) {
							String warningToken = CompilerOptions.warningTokenFromIrritant(irritant);
							if (warningToken != null) {
								buffer.append("[@sup:").append(warningToken).append("] ");
							}
						}
					}
					buffer.append(problem.getMessage());
					buffer.append("\n");
				} catch (Exception e) {
				}
				buffer.append("----------\n");
			}
		}
		problemLog += buffer.toString();
	}
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
					ClassFile.writeToDisk(true, outputPath, relativeName, classFile.getBytes());
				} catch(IOException e) {
				}
			}
		}
	}
}
}
