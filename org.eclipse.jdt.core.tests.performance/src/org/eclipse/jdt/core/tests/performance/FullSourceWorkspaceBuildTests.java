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

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.SourceElementParser;
import org.eclipse.jdt.internal.compiler.SourceElementRequestorAdapter;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.util.Util;

/**
 * Class to test compiler performance.
 * This includes tests on build, batch compiler, Scanner and Parser.
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
//		TESTS_NAMES = new String[] { "testBatchCompilerAllWarnings" };
	}

	public static Test suite() {
		Test suite = buildSuite(testClass());
		TESTS_COUNT = suite.countTestCases();
		createPrintStream(testClass(), LOG_STREAMS, TESTS_COUNT, null);
		return suite;
	}

	private static Class testClass() {
		return FullSourceWorkspaceBuildTests.class;
	}

	/*
	 * Parse several times a file giving its name.
	 */
	private long[] parseFile(String fileName, int kind, int iterations) throws InvalidInputException, IOException {

		// Test for parser
		File file = new File(fileName);
		char[] content = Util.getFileCharContent(file, null);
		CompilerOptions options = new CompilerOptions();
		options.sourceLevel = ClassFileConstants.JDK1_4;
		options.targetJDK = ClassFileConstants.JDK1_4;
		ProblemReporter problemReporter = 
				new ProblemReporter(
					DefaultErrorHandlingPolicies.exitAfterAllProblems(), 
					options, 
					new DefaultProblemFactory());
		
		// Create parser
        Parser parser = null;
		switch (kind) {
		case 1: // SourceElementParser
				parser = new SourceElementParser(new SourceElementRequestorAdapter(), problemReporter.problemFactory, options, true, true);
				break;
			default:
				parser = new Parser(problemReporter, true);
				break;
		}

		// Warm up
		for (int i = 0; i < 2; i++) {
			ICompilationUnit unit = new CompilationUnit(content, file.getName(), null);
			CompilationResult unitResult = new CompilationResult(unit, 0, 1, options.maxProblemsPerUnit);				
			CompilationUnitDeclaration unitDeclaration = parser.dietParse(unit, unitResult);
			parser.getMethodBodies(unitDeclaration);
		}

		// Clean memory
		runGc();

		// Measures
		long parsedLines = 0;
		long parsedCharacters = 0;
		long start = 0;
		if (DEBUG) {
			start = System.currentTimeMillis();
		}
		startMeasuring();
		for (int i = 0; i < iterations; i++) {
			ICompilationUnit unit = new CompilationUnit(content, file.getName(), null);
			CompilationResult unitResult = new CompilationResult(unit, 0, 1, options.maxProblemsPerUnit);				
			CompilationUnitDeclaration unitDeclaration = parser.dietParse(unit, unitResult);
			parser.getMethodBodies(unitDeclaration);
			parsedCharacters += content.length;
			parsedLines += unitResult.getLineSeparatorPositions().length;
		}
		stopMeasuring();

		// Warn if measure time is not enough while debugging
		if (DEBUG) {
			long time = System.currentTimeMillis() - start;
			if (time < TIME_THRESHOLD) {
	            System.err.println(parsedLines + " lines/"+ parsedCharacters + " characters parsed");
			} else {
	            System.out.println(parsedLines + " lines/"+ parsedCharacters + " characters parsed");
			}
		}

		// Return stats
		return new long[] { parsedCharacters, parsedLines };
	}

	/*
	 * Test performance for a parser on one file.
	 * Parse is executed many times ({@link #ITERATIONS_COUNT}) to have significant time for execution.
	 * This test is repeated several times ({@link #MEASURES_COUNT}) to average time measuring.
	 *  
	 * @throws InvalidInputException
	 * @throws IOException
	 */
	void parseParserFile(int kind) throws InvalidInputException, IOException {

		// Get workspace path
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IWorkspaceRoot workspaceRoot = workspace.getRoot();
		final String targetWorkspacePath = workspaceRoot.getProject(JavaCore.PLUGIN_ID)
			.getLocation()
			.toFile()
			.getCanonicalPath();
		
		// Run test
		for (int i=0; i<MEASURES_COUNT; i++) {
			parseFile(targetWorkspacePath+"/compiler/org/eclipse/jdt/internal/compiler/parser/Parser.java", kind, ITERATIONS_COUNT*6);
		}

		// dump measure
		commitMeasurements();
		assertPerformance();
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

		// Warm up
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

		// Clean memory
		runGc();

		// Measures
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

		// Commit
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
		startBuild(warningOptions(0/*default warnings*/), false);
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
	 * Batch compiler build with default javadoc warnings
	 * 
	 * Not calling tagAsSummary means that this test is currently evaluated
	 * before put it in builds performance results.
	 * 
	 * @throws IOException
	 */
	public void testBatchCompilerJavadoc() throws IOException {
		tagAsSummary("Compile>Batch>Compiler>Javadoc warnings", false); // do NOT put in fingerprint
		buildUsingBatchCompiler("-warn:javadoc");
	}

	/**
	 * Batch compiler build with invalid javadoc warnings
	 * 
	 * Not calling tagAsSummary means that this test is currently evaluated
	 * before put it in builds performance results.
	 * 
	 * @throws IOException
	 */
	// TODO (frederic) put back after having understood why this test result can have variation over 20%
	public void _testBatchCompilerAllJavadoc() throws IOException {
		tagAsSummary("Compile>Batch>Compiler>All Javadoc warnings", false); // do NOT put in fingerprint
		buildUsingBatchCompiler("-warn:allJavadoc");
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
	 * Test performance for Parser on one file.
	 * Parse is executed many times ({@link #ITERATIONS_COUNT}) to have significant time for execution.
	 * This test is repeated several times ({@link #MEASURES_COUNT}) to average time measuring.
	 *  
	 * @throws InvalidInputException
	 * @throws IOException
	 */
	public void testParser() throws InvalidInputException, IOException {
		tagAsSummary("Compile>Parse>Parser>Default", true); // put in fingerprint
		parseParserFile(0); // Parser kind
	}

	/**
	 * Test performance for SourceElementParser on one file.
	 * Parse is executed many times ({@link #ITERATIONS_COUNT}) to have significant time for execution.
	 * This test is repeated several times ({@link #MEASURES_COUNT}) to average time measuring.
	 * 
	 * Note: This test has been temporarily removed as there's unexplicable difference between
	 * HEAD and 3.0 versions for CPU Time results (10% faster) and Elapsed process (25% slower)...
	 * TODO (frederic) Put back when platform-releng will have stabilized performance results process.
	 *  
	 * @throws InvalidInputException
	 * @throws IOException
	 */
	public void _testSourceParser() throws InvalidInputException, IOException {
		tagAsSummary("Compile>SrcParse>Parser>Default", true); // put in fingerprint
		parseParserFile(1); // SourceElementParser kind
	}
}
