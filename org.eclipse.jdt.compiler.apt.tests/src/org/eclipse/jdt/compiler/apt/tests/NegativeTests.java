/*******************************************************************************
 * Copyright (c) 2007, 2012 BEA Systems, Inc. and others
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.lang.model.SourceVersion;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import junit.framework.TestCase;

/**
 * Test cases for annotation processing behavior when code contains semantic errors
 */
public class NegativeTests extends TestCase {
	static class TestDiagnosticListener implements DiagnosticListener<JavaFileObject> {
		public static final int NONE = 0;
		public static final int ERROR = 1;
		public static final int INFO = 2;
		public static final int WARNING = 4;

		public int errorCounter;
		private final PrintWriter writer;

		public TestDiagnosticListener(PrintWriter writer) {
			this.writer = writer;
		}

		@Override
		public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
			switch(diagnostic.getKind()) {
				case ERROR :
					this.writer.print(diagnostic.getMessage(null));
					this.errorCounter++;
					break;
				default:
					break;
			}
		}
	}

	// See corresponding usages in the NegativeModelProc class
	private static final String NEGATIVEMODELPROCNAME = "org.eclipse.jdt.compiler.apt.tests.processors.negative.NegativeModelProc";
	private static final String INHERITED_PROCNAME ="org.eclipse.jdt.compiler.apt.tests.processors.inherited.ArgsConstructorProcessor";
	private static final String IGNOREJAVACBUGS = "ignoreJavacBugs";

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		BatchTestUtils.init();
	}

	/**
	 * Validate the testNegativeModel test against the javac compiler.
	 * All test routines are executed.
	 */
	public void testNegativeModelWithSystemCompiler() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		Set<SourceVersion> sourceVersions = compiler.getSourceVersions();
		if (sourceVersions.size() > 4) {
			// test fail on JDK7
			return;
		}
		internalTestNegativeModel(compiler, 0, Collections.singletonList("-A" + IGNOREJAVACBUGS));
	}

	/**
	 * Inspect model of resources/targets.negative.pa.Negative1,
	 * using the Eclipse compiler.
	 */
	public void testNegativeModel1WithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestNegativeModel(compiler, 1, null);
	}

	/**
	 * Inspect model of resources/targets.negative.pa.Negative2,
	 * using the Eclipse compiler.
	 */
	public void testNegativeModel2WithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestNegativeModel(compiler, 2, null);
	}

	/**
	 * Inspect model of resources/targets.negative.pa.Negative3,
	 * using the Eclipse compiler.
	 */
	public void testNegativeModel3WithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestNegativeModel(compiler, 3, null);
	}

	/**
	 * Inspect model of resources/targets.negative.pa.Negative4,
	 * using the Eclipse compiler.
	 */
	public void testNegativeModel4WithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestNegativeModel(compiler, 4, null);
	}

	/**
	 * Inspect model of resources/targets.negative.pa.Negative5,
	 * using the Eclipse compiler.
	 */
	public void testNegativeModel5WithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestNegativeModel(compiler, 5, null);
	}

	/**
	 * Inspect model of resources/targets.negative.pa.Negative6,
	 * using the Eclipse compiler.
	 */
	public void testNegativeModel6WithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestNegativeModel(compiler, 6, null);
	}

	/**
	 * Inspect model of resources/targets.negative.pa.Negative7,
	 * using the Eclipse compiler.
	 */
	public void testNegativeModel7WithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestNegativeModel(compiler, 7, null);
	}

	/**
	 * Inspect model of resources/targets.negative.pa.Negative8,
	 * using the Eclipse compiler.
	 */
	public void testNegativeModel8WithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestNegativeModel(compiler, 8, null);
	}

	/**
	 * Inspect model of resources/targets.negative.pa.Negative9,
	 * using the Eclipse compiler.
	 */
	public void testNegativeModel9WithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestNegativeModel(compiler, 9, null);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=328575
	 */
	public void _testNegativeModel10WithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		System.clearProperty(NEGATIVEMODELPROCNAME);
		System.clearProperty(INHERITED_PROCNAME);
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "targets", "inherited");
		BatchTestUtils.copyResources("targets/inherited", targetFolder);

		// Invoke processing by compiling the targets.model resources
		ByteArrayOutputStream errBuffer = new ByteArrayOutputStream();
		PrintWriter printWriter = new PrintWriter(errBuffer);
		TestDiagnosticListener diagnosticListener = new TestDiagnosticListener(printWriter);
		boolean success = BatchTestUtils.compileTreeWithErrors(compiler, new ArrayList<String>(), targetFolder, diagnosticListener);

		assertTrue("Compilation should have failed due to expected errors, but it didn't", !success);
		assertEquals("Two errors should be reported", 2, diagnosticListener.errorCounter);
		printWriter.flush();
		printWriter.close();
		String expectedErrors =
				"Class targets.inherited.TestGenericChild lacks a public constructor with args: java.awt.Point" +
				"Class targets.inherited.TestNormalChild lacks a public constructor with args: java.awt.Point";

		assertEquals("Wrong output", expectedErrors, String.valueOf(errBuffer));
		String property = System.getProperty(INHERITED_PROCNAME);
		assertNotNull("No property - probably processing did not take place", property);
		assertEquals("succeeded", property);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=328575
	 */
	public void testNegativeModel10WithSystemCompiler() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		System.clearProperty(NEGATIVEMODELPROCNAME);
		System.clearProperty(INHERITED_PROCNAME);
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "targets", "inherited");
		BatchTestUtils.copyResources("targets/inherited", targetFolder);

		// Invoke processing by compiling the targets.model resources
		ByteArrayOutputStream errBuffer = new ByteArrayOutputStream();
		PrintWriter printWriter = new PrintWriter(errBuffer);
		TestDiagnosticListener diagnosticListener = new TestDiagnosticListener(printWriter);
		boolean success = BatchTestUtils.compileTreeWithErrors(compiler, new ArrayList<String>(), targetFolder, diagnosticListener);

		assertTrue("Compilation should have failed due to expected errors, but it didn't", !success);
		assertEquals("Two errors should be reported", 2, diagnosticListener.errorCounter);
		printWriter.flush();
		printWriter.close();

		String property = System.getProperty(INHERITED_PROCNAME);
		assertNotNull("No property - probably processing did not take place", property);
		assertEquals("succeeded", property);
	}

	/**
	 * Attempt to report errors on various elements.
	 */
	private void internalTestNegativeModel(JavaCompiler compiler, int test, Collection<String> extraOptions) throws IOException {
		System.clearProperty(NEGATIVEMODELPROCNAME);
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "targets", "negative");
		BatchTestUtils.copyResources("targets/negative", targetFolder);

		// Turn on the NegativeModelProc - without this, it will just return without doing anything
		List<String> options = new ArrayList<String>();
		options.add("-A" + NEGATIVEMODELPROCNAME + "=" + test);
		if (null != extraOptions)
			options.addAll(extraOptions);

		// Invoke processing by compiling the targets.model resources
		boolean success = BatchTestUtils.compileTreeWithErrors(compiler, options, targetFolder, null);

		assertTrue("Compilation should have failed due to expected errors, but it didn't", !success);
		// If it succeeded, the processor will have set this property to "succeeded";
		// if not, it will set it to an error value.
		String property = System.getProperty(NEGATIVEMODELPROCNAME);
		assertNotNull("No property - probably processing did not take place", property);
		assertEquals("succeeded", property);

		// TODO: check "errors" against expected values to ensure that the problems were correctly reported
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		System.clearProperty(NEGATIVEMODELPROCNAME);
		super.tearDown();
	}


}
