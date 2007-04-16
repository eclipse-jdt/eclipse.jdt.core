/*******************************************************************************
 * Copyright (c) 2006, 2007 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.jdt.compiler.apt.tests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test the ability to execute annotation processors in batch mode, including
 * tests of file generation, processor environment, etc.
 * <p>
 * This suite is not meant to exhaustively test typesystem functionality.
 * @since 3.3
 */
public class BatchDispatchTests extends TestCase {

	private static final String[] ONE_ARG_OPTIONS = {
		"-s",
		"-processor",
		"-processorpath"
	};
	private static final String[] ZERO_ARG_OPTIONS = {
		"-proc:none",
		"-proc:only",
		"-XprintProcessorInfo",
		"-XprintRounds"
	};


	public static TestSuite suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(BatchDispatchTests.class);
		return suite;
	}

	public BatchDispatchTests(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		BatchTestUtils.init();
	}

	/**
	 * Verify that Eclipse compiler properly supports apt-related command line options
	 */
	public void testCheckOptions() {
		for (String option : ONE_ARG_OPTIONS) {
			assertEquals(option + " requires 1 argument", 1, BatchTestUtils.getEclipseCompiler().isSupportedOption(option));
		}
		for (String option : ZERO_ARG_OPTIONS) {
			assertEquals(option + " requires no argument", 0, BatchTestUtils.getEclipseCompiler().isSupportedOption(option));
		}
	}

	/**
	 * Veriy that processor sees correct environment options
	 * (sanity check with system compiler)
	 * @throws IOException 
	 */
	public void testProcessorArgumentsWithSystemCompiler() throws IOException {
		// System compiler
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTestProcessorArguments(compiler);
	}

	/**
	 * Veriy that processor sees correct environment options
	 * when called from Eclipse compiler
	 * @throws IOException 
	 */
	public void testProcessorArgumentsWithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestProcessorArguments(compiler);
	}

	/**
	 * Read annotation values and generate a class using system compiler (javac)
	 * This is a sanity check to verify that the processors, sample code, and
	 * compiler options are correct.
	 * @throws IOException 
	 */
	public void testCompilerOneClassWithSystemCompiler() throws IOException {
		// System compiler
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTestGenerateClass(compiler);
	}

	/**
	 * Read annotation values and generate a class using Eclipse compiler
	 * @throws IOException 
	 */
	public void testCompilerOneClassWithEclipseCompiler() throws IOException {
		// Eclipse compiler
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestGenerateClass(compiler);
	}

	/**
	 * Verify that if a type has two annotations, both processors are run.
	 * @throws IOException 
	 */
	public void testTwoAnnotations() throws IOException {
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "targets", "dispatch");
		File inputFile = BatchTestUtils.copyResource("targets/dispatch/TwoAnnotations.java", targetFolder);
		assertNotNull("No input file", inputFile);

		List<String> options = new ArrayList<String>();
		// See corresponding list in CheckArgsProc processor.
		// Processor will throw IllegalStateException if it detects a mismatch.
		options.add("-Afoo=bar");
		options.add("-Anovalue");
		options.add("-Abar2=");
		BatchTestUtils.compileOneClass(BatchTestUtils.getEclipseCompiler(), options, inputFile);

		// check that the src and class files were generated
 		File genSrcFile = TestUtils.concatPath(BatchTestUtils.getGenFolderName(), "gen", "TwoAnnotationsGen.java");
 		assertTrue("generated src file does not exist", genSrcFile.exists());

 		File classFile = TestUtils.concatPath(BatchTestUtils.getBinFolderName(), "targets", "dispatch", "TwoAnnotations.class");
 		assertTrue("ordinary src file was not compiled", classFile.exists());

 		File genClassFile = TestUtils.concatPath(BatchTestUtils.getBinFolderName(), "gen", "TwoAnnotationsGen.class");
 		assertTrue("generated src file was not compiled", genClassFile.exists());
	}

	// Called with system compiler and Eclipse compiler
	private void internalTestGenerateClass(JavaCompiler compiler) throws IOException {
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "targets", "dispatch");
		File inputFile = BatchTestUtils.copyResource("targets/dispatch/HasGenClass.java", targetFolder);
		assertNotNull("No input file", inputFile);

		List<String> options = new ArrayList<String>();
		BatchTestUtils.compileOneClass(compiler, options, inputFile);

		// check that the gen-src and class files were generated
 		File genSrcFile = TestUtils.concatPath(BatchTestUtils.getGenFolderName(), "gen", "HgcGen.java");
 		assertTrue("generated src file does not exist", genSrcFile.exists());

 		File classFile = TestUtils.concatPath(BatchTestUtils.getBinFolderName(), "targets", "dispatch", "HasGenClass.class");
 		assertTrue("ordinary src file was not compiled", classFile.exists());

 		File genClassFile = TestUtils.concatPath(BatchTestUtils.getBinFolderName(), "gen", "HgcGen.class");
 		assertTrue("generated src file was not compiled", genClassFile.exists());
	}

	// Called with system compiler and Eclipse compiler
	private void internalTestProcessorArguments(JavaCompiler compiler) throws IOException {
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "targets", "dispatch");
		File inputFile = BatchTestUtils.copyResource("targets/dispatch/HasCheckArgs.java", targetFolder);
		assertNotNull("No input file", inputFile);

		List<String> options = new ArrayList<String>();
		// See corresponding list in CheckArgsProc processor.
		// Processor will throw IllegalStateException if it detects a mismatch.
		options.add("-Afoo=bar");
		options.add("-Anovalue");
		options.add("-Abar2=");
		BatchTestUtils.compileOneClass(compiler, options, inputFile);
	}

	@Override
	protected void tearDown() throws Exception {
		BatchTestUtils.tearDown();
	}
}
