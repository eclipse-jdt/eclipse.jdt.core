/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.performance;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import junit.framework.Test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.test.performance.Dimension;

/**
 * Class to test compiler performance.
 * This includes tests on build, batch compiler, Scanner and Parser.
 */
public class CompleteFullSourceWorkspaceBuildTests extends FullSourceWorkspaceTests {
	
	// Tests counters
	private static int TESTS_COUNT = 0;

	// Log files
	private static PrintStream[] LOG_STREAMS = new PrintStream[4];

	
	/**
	 * @param name
	 */
	public CompleteFullSourceWorkspaceBuildTests(String name) {
		super(name);
	}

	static {
//		TESTS_PREFIX = "testPerfBatch";
//		TESTS_NAMES = new String[] { "testPerfParserFiles" };
	}

	public static Test suite() {
		Test suite = buildSuite(testClass());
		TESTS_COUNT = suite.countTestCases();
		createPrintStream(testClass().getName(), LOG_STREAMS, TESTS_COUNT, null);
		return suite;
	}

	private static Class testClass() {
		return CompleteFullSourceWorkspaceBuildTests.class;
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {

		// End of execution => one test less
		TESTS_COUNT--;

		// Log perf result
		if (LOG_DIR != null) {
			logPerfResult(LOG_STREAMS, TESTS_COUNT);
		}
		
		// Call super at the end as it close print streams
		super.tearDown();
	}

	/**
	 * Full build with no warning.
	 * 
	 * Not calling tagAsSummary means that this test is under test before put it in builds
	 * performance results.
	 * 
	 * @throws CoreException
	 * @throws IOException
	 */
	public void testPerfFullBuildNoWarning() throws CoreException, IOException {
		//tagAsSummary("Compile>Build>Clean>Src:full wksp>Warn>None", Dimension.CPU_TIME, false); // do NOT put in fingerprint
		int warnings = startBuild(warningOptions(false/*no warning*/));
		assertEquals("Expected no warning", 0, warnings);
	}

	/**
	 * Full build with all warnings.
	 * 
	 * Not calling tagAsSummary means that this test is under test before put it in builds
	 * performance results.
	 * 
	 * @throws CoreException
	 * @throws IOException
	 * 
	 */
	public void testPerfFullBuildAllWarnings() throws CoreException, IOException {
		tagAsSummary("Compile>Build>Clean>Src:full wksp>Options:All", Dimension.CPU_TIME, false/*put in fingerprint*/);
		int warnings = startBuild(warningOptions(true/*all warnings*/));
		if (DEBUG && warnings > 0) {
			System.out.println(warnings+" warnings were diagnosed");
		}
	}

	/**
	 * Batch compiler build with no warning
	 * 
	 * Not calling tagAsSummary means that this test is under test before put it in builds
	 * performance results.
	 * 
	 * @throws IOException
	 */
	public void testPerfBatchCompilerNoWarning() throws IOException {
		tagAsSummary("Compile>Batch>Src:compiler>Options:None", Dimension.CPU_TIME, false); // do NOT put in fingerprint

		File logsFile = buildUsingBatchCompiler("-nowarn");

		// Should not get any error
		assertTrue("No log file", logsFile.exists());
		if (logsFile.length() != 0) {
			char[] errors = Util.getFileCharContent(logsFile, null);
			int length = Math.min(errors.length, 1000);
			assertTrue("Should have NO warning!\nAlthoug, got following ones:\n"+(new String(errors, 0, length)), false);
		}
	}

	/**
	 * Batch compiler build with default warnings
	 * 
	 * Not calling tagAsSummary means that this test is under test before put it in builds
	 * performance results.
	 * 
	 * @throws IOException
	 */
	public void testPerfBatchCompiler() throws IOException {
		tagAsSummary("Compile>Batch>Src:compiler>Options:Default", Dimension.CPU_TIME, false); // do NOT put in fingerprint

		File logsFile = buildUsingBatchCompiler("");

		// Should get some warnings
		assertTrue("No log file", logsFile.exists());
		assertFalse("Should get some warnings", logsFile.length()==0);
	}

	/**
	 * Batch compiler build with invalid javadoc warnings
	 * 
	 * Not calling tagAsSummary means that this test is under test before put it in builds
	 * performance results.
	 * 
	 * @throws IOException
	 */
	public void testPerfBatchCompilerInvalidJavadoc() throws IOException {
		tagAsSummary("Compile>Batch>Src:compiler>Options:Javadoc", Dimension.CPU_TIME, false); // do NOT put in fingerprint

		File logsFile = buildUsingBatchCompiler("-warn:javadoc");

		// Should get some warnings
		assertTrue("No log file", logsFile.exists());
		assertFalse("Should get some warnings", logsFile.length()==0);
	}

	/**
	 * Batch compiler build with invalid javadoc warnings
	 * 
	 * Not calling tagAsSummary means that this test is under test before put it in builds
	 * performance results.
	 * 
	 * @throws IOException
	 */
	public void testPerfBatchCompilerAllJavadoc() throws IOException {
		tagAsSummary("Compile>Batch>Src:compiler>Options:AllJavadoc", Dimension.CPU_TIME, false); // do NOT put in fingerprint

		File logsFile = buildUsingBatchCompiler("-warn:allJavadoc");

		// Should get some warnings
		assertTrue("No log file", logsFile.exists());
		assertFalse("Should get some warnings", logsFile.length()==0);
	}

	/**
	 * Batch compiler build with all warnings
	 * 
	 * Not calling tagAsSummary means that this test is under test before put it in builds
	 * performance results.
	 * 
	 * @throws IOException
	 */
	public void testPerfBatchCompilerAllWarning() throws IOException {
		tagAsSummary("Compile>Batch>Src:compiler>Options:All", Dimension.CPU_TIME, false); // do NOT put in fingerprint

		String allOptions = "-warn:" +
			"allDeprecation," +
			"allJavadoc," +
			"assertIdentifier," +
			"charConcat," +
			"conditionAssign," +
			"constructorName," +
			"deprecation," +
			"emptyBlock," +
			"fieldHiding," +
			"finally," +
			"indirectStatic," +
			"intfNonInherited," +
			"localHiding," +
			"maskedCatchBlock," +
			"nls," +
			"noEffectAssign," +
			"pkgDefaultMethod," +
			"semicolon," +
			"unqualifiedField," +
			"unusedArgument," +
			"unusedImport," +
			"unusedLocal," +
			"unusedPrivate," +
			"unusedThrown," +
			"unnecessaryElse," +
			"uselessTypeCheck," +
			"specialParamHiding," +
			"staticReceiver," +
			"syntheticAccess," +
			"tasks(TODO|FIX|XXX)," +
			"typeHiding,";
		File logsFile = buildUsingBatchCompiler(allOptions);

		// Should get some warnings
		assertTrue("No log file", logsFile.exists());
		assertFalse("Should get some warnings", logsFile.length()==0);
	}
}
