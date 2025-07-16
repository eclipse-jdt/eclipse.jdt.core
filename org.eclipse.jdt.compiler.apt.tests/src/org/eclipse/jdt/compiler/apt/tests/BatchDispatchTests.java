/*******************************************************************************
 * Copyright (c) 2006, 2011 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *    IBM Corporation - fix for 342936
 *******************************************************************************/
package org.eclipse.jdt.compiler.apt.tests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.tools.Diagnostic;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.eclipse.jdt.compiler.apt.tests.BatchTestUtils.DiagnosticReport;

/**
 * Test the ability to execute annotation processors in batch mode, including
 * tests of file generation, processor environment, etc.
 * <p>
 * This suite is not meant to exhaustively test typesystem functionality.
 * @since 3.3
 */
public class BatchDispatchTests extends TestCase {

	// Processor class names; see corresponding usage in the processor classes.
	private static final String INHERITEDANNOPROC = "org.eclipse.jdt.compiler.apt.tests.processors.inherited.InheritedAnnoProc";

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
	 */
	public void testProcessorArgumentsWithSystemCompiler() throws IOException {
		// System compiler
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTestProcessorArguments(compiler);
	}

	/**
	 * Veriy that processor sees correct environment options
	 * when called from Eclipse compiler
	 */
	public void _testProcessorArgumentsWithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestProcessorArguments(compiler);
	}

	/**
	 * Read annotation values and generate a class using system compiler (javac)
	 * This is a sanity check to verify that the processors, sample code, and
	 * compiler options are correct.
	 */
	public void testCompilerOneClassWithSystemCompiler() throws IOException {
		// System compiler
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTestGenerateClass(compiler);
	}

	/**
	 * Read annotation values and generate a class using Eclipse compiler
	 */
	public void testCompilerOneClassWithEclipseCompiler() throws IOException {
		// Eclipse compiler
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestGenerateClass(compiler);
	}

	public void testWarningsWithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestWarnings(compiler, 1, "-warn:+unused");
	}

	public void testNoWarningsWithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestWarnings(compiler, 0, "-warn:+unused", "-nowarn");
	}

	public void testUnusedWarningWithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestWarnings(compiler, 1, "-nowarn", "-warn:+unused");
	}

	public void testNoWarningsInFolderWithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestWarnings(compiler, 0, "-warn:+unused", "-nowarn:[" + BatchTestUtils.getGenFolderName() + ']');
	}

	public void testNoWarningsInFolderWithEclipseCompiler2() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestWarnings(compiler, 0, "-nowarn:[" + BatchTestUtils.getGenFolderName() + ']', "-warn:+unused");
	}

	private void internalTestWarnings(JavaCompiler compiler, int numberOfWarnings, String... extraOptions) throws IOException {
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "targets", "dispatch");
		File inputFile = BatchTestUtils.copyResource("targets/dispatch/WarnGenClass.java", targetFolder);
		assertNotNull("No input file", inputFile);

		List<String> options = new ArrayList<>();
		if (extraOptions != null) {
			options.addAll(Arrays.asList(extraOptions));
		}
		DiagnosticReport<JavaFileObject> diagnostics = BatchTestUtils.compileOneClass(compiler, options, inputFile);

		// check that the gen-src and class files were generated
 		File genSrcFile = TestUtils.concatPath(BatchTestUtils.getGenFolderName(), "gen", "WarnGen.java");
 		assertTrue("generated src file does not exist", genSrcFile.exists());

 		File classFile = TestUtils.concatPath(BatchTestUtils.getBinFolderName(), "targets", "dispatch", "WarnGenClass.class");
 		assertTrue("ordinary src file was not compiled", classFile.exists());

 		File genClassFile = TestUtils.concatPath(BatchTestUtils.getBinFolderName(), "gen", "WarnGen.class");
 		assertTrue("generated src file was not compiled", genClassFile.exists());

 		assertEquals("Wrong number of warnings", numberOfWarnings,
 				diagnostics.get(Diagnostic.Kind.WARNING, Diagnostic.Kind.MANDATORY_WARNING).size());
	}

	/**
	 * Validate the inherited annotations test against the javac compiler.
	 */
	public void testInheritedAnnosWithSystemCompiler() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTestInheritance(compiler, INHERITEDANNOPROC);
	}

	/**
	 * Test dispatch of annotation processor on inherited annotations.
	 */
	public void testInheritedAnnosWithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestInheritance(compiler, INHERITEDANNOPROC);
	}

	/**
	 * Verify that if a type has two annotations, both processors are run.
	 */
	public void _testTwoAnnotations() throws IOException {
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "targets", "dispatch");
		File inputFile = BatchTestUtils.copyResource("targets/dispatch/TwoAnnotations.java", targetFolder);
		assertNotNull("No input file", inputFile);

		List<String> options = new ArrayList<>();
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

		List<String> options = new ArrayList<>();
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

		List<String> options = new ArrayList<>();
		// See corresponding list in CheckArgsProc processor.
		// Processor will throw IllegalStateException if it detects a mismatch.
		options.add("-Afoo=bar");
		options.add("-Anovalue");
		options.add("-Abar2=");
		BatchTestUtils.compileOneClass(compiler, options, inputFile);
	}

	/**
	 * Test functionality by running a particular processor against the types in
	 * resources/targets.  The processor must support "*" (the set of all annotations)
	 * and must report its errors or success via the methods in BaseProcessor.
	 */
	private void internalTestInheritance(JavaCompiler compiler, String processorClass) throws IOException {
		System.clearProperty(processorClass);
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "targets/dispatch", "inheritedanno");
		BatchTestUtils.copyResources("targets/dispatch/inheritedanno", targetFolder);

		List<String> options = new ArrayList<>();
		options.add("-A" + processorClass);
		BatchTestUtils.compileTree(compiler, options, targetFolder);

		// If it succeeded, the processor will have set this property to "succeeded";
		// if not, it will set it to an error value.
		assertEquals("succeeded", System.getProperty(processorClass));
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=209961
	 */
	public void test209961() throws IOException {
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "targets", "dispatch");
		File inputFile = BatchTestUtils.copyResource("targets/dispatch/X.java", targetFolder);
		assertNotNull("No input file", inputFile);
		File classpathEntry =TestUtils.concatPath(
			new File(BatchTestUtils.getPluginDirectoryPath(), BatchTestUtils.getResourceFolderName()).getAbsolutePath(),
			"targets",
			"dispatch");

		List<String> options = new ArrayList<>();
		// See corresponding list in CheckArgsProc processor.
		// Processor will throw IllegalStateException if it detects a mismatch.
		options.add("-classpath");
		options.add(classpathEntry.getAbsolutePath());
		options.add("-verbose");

		BatchTestUtils.compileOneClass(BatchTestUtils.getEclipseCompiler(), options, inputFile);
	}

	@Override
	protected void tearDown() throws Exception {
		BatchTestUtils.tearDown();
	}
}
