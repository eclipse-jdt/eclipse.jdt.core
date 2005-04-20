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
package org.eclipse.jdt.core.tests.performance;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import junit.framework.Test;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.SourceElementParser;
import org.eclipse.jdt.internal.compiler.SourceElementRequestorAdapter;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.util.Util;

/**
 */
public class FullSourceWorkspaceBuildTests extends FullSourceWorkspaceTests {
	
	// Tests counters
	private static int TESTS_COUNT = 0;
	private final static int ITERATIONS_COUNT = 10;
	private final static int SCAN_REPEAT = 800; // 800 is default

	// Tests thresholds
	private final static int TIME_THRESHOLD = 150;
	
	// Log files
	private static PrintStream[] LOG_STREAMS = new PrintStream[LOG_TYPES.length];

	
	/**
	 * @param name
	 */
	public FullSourceWorkspaceBuildTests(String name) {
		super(name);
	}

	static {
//		TESTS_PREFIX = "testPerfBatch";
		TESTS_NAMES = new String[] { "testFullBuildNoWarning" };
	}

	public static Test suite() {
		Test suite = buildSuite(testClass());
		TESTS_COUNT = suite.countTestCases();
		createPrintStream(testClass().getName(), LOG_STREAMS, TESTS_COUNT, null);
		return suite;
	}

	private static Class testClass() {
		return FullSourceWorkspaceBuildTests.class;
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

	/*
	 * Scan a file giving its name.
	 * Two kind of scan is currently possible:
	 * 	- 0: only scan all tokens
	 * 	- 1: scan all tokens and get each identifier
	 */
	private void scanFile(String fileName, int kind) throws InvalidInputException, IOException {

		// Test for scanner
		long tokenCount = 0;
		char[] content = Util.getFileCharContent(new File(fileName),
			null);
		Scanner scanner = new Scanner();
		scanner.setSource(content);

		// warm-up
		for (int i = 0; i < 2; i++) {
			scanner.resetTo(0, content.length);
			tokenize: while (true) {
				int token = scanner.getNextToken();
				switch (kind) {
					case 0: // first case: only read tokens
						switch (token) {
							case TerminalTokens.TokenNameEOF:
								break tokenize;
						}
						break;
					case 1: // second case: read tokens + get ids
						switch (token) {
							case TerminalTokens.TokenNameEOF:
								break tokenize;
							case TerminalTokens.TokenNameIdentifier:
								scanner.getCurrentIdentifierSource();
								break;
						}
						break;
				}
			}
		}

		// loop for time measuring
		long size = 0;
		for (int i = 0; i < MEASURES_COUNT; i++) {
			startMeasuring();
			for (int j = 0; j < SCAN_REPEAT; j++) {
				scanner.resetTo(0, content.length);
				tokenize: while (true) {
					int token = scanner.getNextToken();
					switch (kind) {
						case 0: // first case: only read tokens
							switch (token) {
								case TerminalTokens.TokenNameEOF:
									break tokenize;
							}
							break;
						case 1: // second case: read tokens + get ids
							switch (token) {
								case TerminalTokens.TokenNameEOF:
									break tokenize;
								case TerminalTokens.TokenNameIdentifier:
									char[] c = scanner.getCurrentIdentifierSource();
									size += c.length;
									break;
							}
							break;
					}
					tokenCount++;
				}
			}
			stopMeasuring();
		}

		// dump measure
		commitMeasurements();
		assertPerformance();

		// Debug
		if (DEBUG) {
			switch (kind) {
				case 0:
					System.out.println(tokenCount + " tokens read.");
					break;
				case 1:
					System.out.print(tokenCount + " tokens were read ("+size+" characters)");
					break;
			}
		}
	}

	/*
	 * Parse several times a file giving its name.
	 */
	private long[] parseSourceFile(String fileName, int iterations) throws InvalidInputException, IOException {

		// Test for parser
		File file = new File(fileName);
		char[] content = Util.getFileCharContent(file, null);
		CompilerOptions options = new CompilerOptions();
		options.sourceLevel = CompilerOptions.JDK1_4;
		options.targetJDK = CompilerOptions.JDK1_4;
		
		// Create parser
        SourceElementParser parser = new SourceElementParser(new SourceElementRequestorAdapter(), new DefaultProblemFactory(), options);

		// Warm-up
		for (int i = 0; i < 2; i++) {
			ICompilationUnit unit = new CompilationUnit(content, file.getName(), null);
			parser.parseCompilationUnit(unit, false);
		}

		// Clean memory
		runGc();

		// Measures
		long start = 0;
		startMeasuring();
		for (int i = 0; i < iterations; i++) {
			ICompilationUnit unit = new CompilationUnit(content, file.getName(), null);
			parser.parseCompilationUnit(unit, false);
		}
		stopMeasuring();

		// Return stats
		return null;
	}

	/**
	 * Full build with no warning.
	 * 
	 * Not calling tagAsSummary means that this test is currently evaluated
	 * before put it in builds performance results.
	 * 
	 * @throws CoreException
	 * @throws IOException
	 */
	public void testFullBuildNoWarning() throws CoreException, IOException {
		tagAsSummary("Compile>Build>Clean>Full>No warning", false); // do NOT put in fingerprint
		startBuild(warningOptions(-1/*no warning*/), false);
	}

	/**
	 * Full build with JavaCore default options.
	 * 
	 * @throws CoreException
	 * @throws IOException
	 */
	public void testFullBuildDefault() throws CoreException, IOException {
		tagAsGlobalSummary("Compile>Build>Clean>Full>Default warnings", true); // put in fingerprint
		startBuild(warningOptions(0/*default warning*/), false);
	}

	/**
	 * Full build with all warnings.
	 * 
	 * Not calling tagAsSummary means that this test is currently evaluated
	 * before put it in builds performance results.
	 * 
	 * @throws CoreException
	 * @throws IOException
	 * 
	 */
	public void testFullBuildAllWarnings() throws CoreException, IOException {
		tagAsSummary("Compile>Build>Clean>Full>All warnings", false); // do NOT put in fingerprint
		startBuild(warningOptions(1/*all warnings*/), false);
	}

	/**
	 * Batch compiler build with no warning
	 * 
	 * Not calling tagAsSummary means that this test is currently evaluated
	 * before put it in builds performance results.
	 * 
	 * @throws IOException
	 */
	public void testBatchCompilerNoWarning() throws IOException {
		tagAsSummary("Compile>Batch>Compiler>No warning", true); // put in fingerprint
		buildUsingBatchCompiler("-nowarn");
	}

	/**
	 * Batch compiler build with default warnings
	 * 
	 * Not calling tagAsSummary means that this test is currently evaluated
	 * before put it in builds performance results.
	 * 
	 * @throws IOException
	 */
	public void testBatchCompilerDefault() throws IOException {
		tagAsSummary("Compile>Batch>Compiler>Default warnings", false); // do NOT put in fingerprint

		buildUsingBatchCompiler("");
	}

	/**
	 * Batch compiler build with all warnings
	 * 
	 * Not calling tagAsSummary means that this test is currently evaluated
	 * before put it in builds performance results.
	 * 
	 * @throws IOException
	 */
	public void testBatchCompilerAllWarnings() throws IOException {
		tagAsSummary("Compile>Batch>Compiler>All warnings", false); // do NOT put in fingerprint

		String allOptions = "-warn:" +
			"allDeprecation," +
			"assertIdentifier," +
			"constructorName," +
			"deprecation," +
			"interfaceNonInherited," +
			"maskedCatchBlock," +
			"nls," +
			"noEffectAssign," +
			"packageDefaultMethod," +
			"unusedArgument," +
			"unusedImport," +
			"unusedLocal," +
			"unusedPrivate," +
			"staticReceiver," +
			"syntheticAccess," +
			"tasks(TODO|FIX|XXX)";
		buildUsingBatchCompiler(allOptions);
	}

	/**
	 * Test performance for Scanner on one file.
	 * Scan is executed many times ({@link #SCAN_REPEAT}) to have significant time for execution.
	 * This test is repeated several times ({@link #ITERATIONS_COUNT}) to average time measuring.
	 *  
	 * @throws InvalidInputException
	 * @throws IOException
	 */
	public void testScanner() throws InvalidInputException, IOException {
		// Do no longer print result in performance fingerprint
		tagAsSummary("Compile>Scan>Parser>Default", true); // put in fingerprint

		// Get workspace path
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IWorkspaceRoot workspaceRoot = workspace.getRoot();
		final String targetWorkspacePath = workspaceRoot.getProject(JavaCore.PLUGIN_ID)
			.getLocation()
			.toFile()
			.getCanonicalPath();
		
		// Run test
//		scanFile(targetWorkspacePath+"/compiler/org/eclipse/jdt/internal/compiler/parser/Parser.java", 0/*only scan tokens*/);
		scanFile(targetWorkspacePath+"/compiler/org/eclipse/jdt/internal/compiler/parser/Parser.java", 1/*scan tokens+get identifiers*/);
	}

	/**
	 * Test performance for SourceElementParser on one file.
	 * Parse is executed many times ({@link #ITERATIONS_COUNT}) to have significant time for execution.
	 * This test is repeated several times ({@link #MEASURES_COUNT}) to average time measuring.
	 *  
	 * @throws InvalidInputException
	 * @throws IOException
	 */
	public void testSourceParser() throws InvalidInputException, IOException {
		tagAsSummary("Compile>SrcParse>Parser>Default", true); // put in fingerprint

		// Get workspace path
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IWorkspaceRoot workspaceRoot = workspace.getRoot();
		final String targetWorkspacePath = workspaceRoot.getProject(JavaCore.PLUGIN_ID)
			.getLocation()
			.toFile()
			.getCanonicalPath();
		
		// Run test
		for (int i=0; i<MEASURES_COUNT; i++) {
			parseSourceFile(targetWorkspacePath+"/compiler/org/eclipse/jdt/internal/compiler/parser/Parser.java", ITERATIONS_COUNT*6);
		}

		// dump measure
		commitMeasurements();
		assertPerformance();
	}
}
