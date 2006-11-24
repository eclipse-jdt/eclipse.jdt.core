/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.compiler.tool.tests;

import java.util.ServiceLoader;

import javax.tools.JavaCompiler;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * If adding new tests, always add them explicitely in the method suite().
 */
import org.eclipse.jdt.compiler.tool.EclipseCompiler;

public class CompilerToolTests extends TestCase {
	public CompilerToolTests(String name) {
		super(name);
	}
	public static TestSuite suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(new CompilerToolTests("testInitializeJavaCompiler"));
		suite.addTest(new CompilerToolTests("testCheckPresence"));
		suite.addTest(new CompilerToolTests("testCheckOptions"));
		suite.addTest(new CompilerToolTests("testCleanUp"));
		return suite;
	}

	private static JavaCompiler Compiler;
	private static String[] ONE_ARG_OPTIONS = {
		"-cp",
		"-classpath",
		"-bootclasspath",
		"-sourcepath",
		"-extdirs",
		"-endorseddirs",
		"-d",
		"-encoding",
		"-source",
		"-target",
		"-maxProblems",
		"-log",
		"-repeat"
	};
	private static String[] ZERO_ARG_OPTIONS = {
		"-1.3",
		"-1.4",
		"-1.5",
		"-1.6",
		"-6",
		"-6.0",
		"-5",
		"-5.0",
		"-deprecation",
		"-nowarn",
		"-warn:none",
		"-?:warn",
		"-g",
		"-g:lines",
		"-g:source",
		"-g:vars",
		"-g:lines,vars",
		"-g:none",
		"-preserveAllLocals",
		"-X",
		"-O",
		"-proceedOnError",
		"-verbose",
		"-referenceInfo",
		"-progress",
		"-time",
		"-noExit",
		"-inlineJSR",
		"-enableJavadoc",
		"-Xemacs",
		"-?",
		"-help",
		"-v",
		"-version",
		"-showversion"
	};

	/*
	 * Initialize the compiler for all the tests
	 */
	public void testInitializeJavaCompiler() {
		ServiceLoader<JavaCompiler> javaCompilerLoader = ServiceLoader.load(JavaCompiler.class);
		int compilerCounter = 0;
		for (JavaCompiler javaCompiler : javaCompilerLoader) {
			compilerCounter++;
			if (javaCompiler instanceof EclipseCompiler) {
				Compiler = javaCompiler;
			}
	     }
		assertEquals("Only one compiler available", 1, compilerCounter);
	}
	
	public void testCheckPresence() {
		// test that the service provided by org.eclipse.jdt.compiler.tool is there
		assertNotNull("No compiler found", Compiler);
	}
	
	public void testCheckOptions() {
		for (String option : ONE_ARG_OPTIONS) {
			assertEquals(option + " requires 1 argument", 1, Compiler.isSupportedOption(option));
		}
		for (String option : ZERO_ARG_OPTIONS) {
			assertEquals(option + " requires no argument", 0, Compiler.isSupportedOption(option));
		}
		assertEquals("-Jignore requires no argument", 0, Compiler.isSupportedOption("-Jignore"));
		assertEquals("-Xignore requires no argument", 0, Compiler.isSupportedOption("-Xignore"));
	}

	/*
	 * Clean up the compiler
	 */
	public void testCleanUp() {
		Compiler = null;
	}
}
