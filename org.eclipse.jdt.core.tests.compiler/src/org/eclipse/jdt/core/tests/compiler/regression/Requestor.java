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
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import junit.framework.Assert;
import org.eclipse.jdt.core.compiler.IProblem;
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
public Requestor(IProblemFactory problemFactory, String outputPath, boolean generateOutput) {
	this.problemFactory = problemFactory;
	this.outputPath = outputPath;
	this.generateOutput = generateOutput;
}
	public void acceptResult(CompilationResult cr) {
		if (cr.hasProblems() || cr.hasTasks()) {
			if (cr.hasErrors()) {
				this.hasErrors = true;
			}
			
			IProblem[] actualProblems = cr.getAllProblems();
			int actualProblemsCount = actualProblems == null ? 0 : actualProblems.length;
			ExpectedProblem[] problems = (ExpectedProblem[])this.expectedProblems.get(new String(cr.getFileName()));
			int expectedProblemsCount = problems == null ? 0 : problems.length;

			if (actualProblemsCount > 0 || expectedProblemsCount > 0) {
				boolean areMessagesDifferent = false;
				String message = new String(cr.getFileName()) + " does not have the correct errors.\nFound :\n";
				for (int i = 0; i < actualProblemsCount; i++) {
					message += "\t" + actualProblems[i].getMessage() + "\n";
					/* START PRINT */
/*					
					System.out.print(actualProblems[i].getID()+":");
					String[] pbArgs =  actualProblems[i].getArguments();
					for (int j = 0; j < pbArgs.length; j++){
						if (j > 0) System.out.print(", ");
						System.out.print(pbArgs[j]);
					}
					System.out.println();
*/					
					/* END PRINT */
					if (i < expectedProblemsCount 
						&& !actualProblems[i].getMessage().equals(problemFactory.getLocalizedMessage(problems[i].id, problems[i].arguments))) {
						areMessagesDifferent = true;
					System.out.print(actualProblems[i].getID()+":");
					String[] pbArgs =  actualProblems[i].getArguments();
					for (int j = 0; j < pbArgs.length; j++){
						if (j > 0) System.out.print(", ");
						System.out.print(pbArgs[j]);
					}
					System.out.println();
					}
				}
				if (expectedProblemsCount == 0) {
					message += "Expecting no problems.";
				} else {
					message += "Expecting :\n";
					for (int i = 0; i < expectedProblemsCount; i++) {
						ExpectedProblem expectedProblem = problems[i];
						message += "\t" + problemFactory.getLocalizedMessage(expectedProblem.id, expectedProblem.arguments) + "\n";
					}
				}
				assertTrue(message, (actualProblemsCount == expectedProblemsCount) && !areMessagesDifferent);
			}
		}
		outputClassFiles(cr);
	}
public void expectedProblems(ExpectedProblem[] problems) {
	for (int i = 0; i < problems.length; i++){
		ExpectedProblem problem = problems[i];
		String fileName = problem.fileName;
		if (File.separator.equals("/")) {
			if (fileName.indexOf("\\") != -1) {
				fileName = fileName.replace('\\', File.separatorChar);
			}
		} else {
			// the file separator is \
			if (fileName.indexOf('/') != -1) {
				fileName = fileName.replace('/', File.separatorChar);
			}
		}
		
		ExpectedProblem[] existingProblems = (ExpectedProblem[])this.expectedProblems.get(fileName);
		if (existingProblems == null) {
			this.expectedProblems.put(fileName, new ExpectedProblem[] {problem});
		} else {
			int length = existingProblems.length;
			ExpectedProblem[] newProblems = new ExpectedProblem[length + 1];
			System.arraycopy(existingProblems, 0, newProblems, 0, length);
			newProblems[length] = problem;
			this.expectedProblems.put(fileName, newProblems);
		}
	}
}
protected void outputClassFiles(CompilationResult unitResult) {

	if ((unitResult != null) && (!unitResult.hasErrors() || generateOutput)) {
		Enumeration classFiles = unitResult.compiledTypes.elements();
		if (outputPath != null) {
			while (classFiles.hasMoreElements()) {
				// retrieve the key and the corresponding classfile
				ClassFile classFile = (ClassFile) classFiles.nextElement();
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
