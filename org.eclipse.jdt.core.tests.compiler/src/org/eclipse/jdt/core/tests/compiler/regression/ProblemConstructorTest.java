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

import junit.framework.Test;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.IProblemFactory;

public class ProblemConstructorTest extends AbstractRegressionTest {

public ProblemConstructorTest(String name) {
	super(name);
}
public static Test suite() {
	return setupSuite(testClass());
}
public static Class testClass() {
	return ProblemConstructorTest.class;
}

protected Requestor getRequestor(IProblemFactory problemFactory) {
	return new Requestor(problemFactory, OUTPUT_DIR.endsWith(File.separator) ? OUTPUT_DIR : OUTPUT_DIR + File.separator, false) {
		public void acceptResult(CompilationResult cr) {
			super.acceptResult(cr);
			outputClassFiles(cr);
		}

		protected void outputClassFiles(CompilationResult unitResult) {
		
			if (unitResult != null) {
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
	};
}
public void test001() {
	this.runNegativeTest(
		new String[] {
			"prs/Test1.java",
			"package prs;	\n" +
			"import java.io.IOException;	\n" +
			"public class Test1 {	\n" +
			"String s = 3;	\n" +
			"Test1() throws IOException {	\n" +
			"}	\n" +
			"}"
		},
		new ExpectedProblem[] {
			new ExpectedProblem("prs/Test1.java", IProblem.TypeMismatch, new String[] {"int", "String"})
		}
		);

	this.runNegativeTest(
		new String[] {
			"prs/Test2.java",
			"package prs;	\n" +
			"import java.io.IOException;	\n" +
			"public class Test2 {	\n" +
			"public void foo() {	\n" +
			"try {	\n" +
			"Test1 t = new Test1();	\n" +
			"System.out.println();	\n" +
			"} catch(IOException e)	\n" +
			"{	\n" +
			"e.printStackTrace();	\n" +
			"}	\n" +
			"}	\n" +
			"}"
		},
		new ExpectedProblem[] {},
		null,
		false);
}


}
