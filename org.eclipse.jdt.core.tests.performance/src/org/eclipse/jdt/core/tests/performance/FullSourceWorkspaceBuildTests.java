/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
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

	// Options
	private static final String ALL_OPTIONS = "-warn:" +
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
	
	// Source paths
	final static String[] JDT_CORE_SRC_PATHS = {
		"batch",
		"codeassist",
		"compiler",
		"dom",
		"eval",
		"formatter",
		"model",
		"search"
	};
	
	/**
	 * @param name
	 */
	public FullSourceWorkspaceBuildTests(String name) {
		super(name);
	}

	static {
//		TESTS_PREFIX = "testCompile";
//		TESTS_NAMES = new String[] { "testFullBuildProjectNoWarning" };
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
	long[] parseFile(String fileName, int kind, int iterations) throws InvalidInputException, IOException {

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
	void parseParserFile(final int kind) throws InvalidInputException, IOException, CoreException {

		// Get workspace path
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IWorkspaceRoot workspaceRoot = workspace.getRoot();
		final String workspacePath = workspaceRoot.getLocation().toFile().getCanonicalPath();
		
		// Run test
		for (int i=0; i<MEASURES_COUNT; i++) {
			IWorkspaceRunnable compilation = new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					try {
						parseFile(workspacePath+PARSER_WORKING_COPY.getPath(), kind, ITERATIONS_COUNT*6);
					} catch (InvalidInputException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};
			if (workspace.isTreeLocked()) {
				compilation.run(null/*no progress available*/);
			} else {
				workspace.run(
					compilation,
					null/*don't take any lock*/,
					IWorkspace.AVOID_UPDATE,
					null/*no progress available here*/);
			}
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
	void scanFile(String fileName, int kind) throws InvalidInputException, IOException {

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

		// Measures
		long size = 0;
		for (int i = 0; i < MEASURES_COUNT; i++) {
			runGc();
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
	 * Test performance for Scanner on one file.
	 * Scan is executed many times ({@link #SCAN_REPEAT}) to have significant time for execution.
	 * This test is repeated several times ({@link #ITERATIONS_COUNT}) to average time measuring.
	 *  
	 * @throws InvalidInputException
	 * @throws IOException
	 */
	public void testScanner() throws InvalidInputException, IOException, CoreException {
		// Do no longer print result in performance fingerprint
		tagAsSummary("Compile>Scan>Parser>Default", false); // do NOT put in fingerprint

		// Get workspace path
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IWorkspaceRoot workspaceRoot = workspace.getRoot();
		final String workspacePath = workspaceRoot.getLocation().toFile().getCanonicalPath();

		// Run test
		IWorkspaceRunnable compilation = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				try {
					scanFile(workspacePath+PARSER_WORKING_COPY.getPath(), 1/*scan tokens+get identifiers*/);
				} catch (InvalidInputException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		if (workspace.isTreeLocked()) {
			compilation.run(null/*no progress available*/);
		} else {
			workspace.run(
				compilation,
				null/*don't take any lock*/,
				IWorkspace.AVOID_UPDATE,
				null/*no progress available here*/);
		}
	}

	/**
	 * Test performance for Parser on one file.
	 * Parse is executed many times ({@link #ITERATIONS_COUNT}) to have significant time for execution.
	 * This test is repeated several times ({@link #MEASURES_COUNT}) to average time measuring.
	 *  
	 * @throws InvalidInputException
	 * @throws IOException
	 */
	public void testParser() throws InvalidInputException, IOException, CoreException {
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
	public void _testSourceParser() throws InvalidInputException, IOException, CoreException {
		tagAsSummary("Compile>SrcParse>Parser>Default", true); // put in fingerprint
		parseParserFile(1); // SourceElementParser kind
	}

	/**
	 * Full build with JavaCore default options.
	 * 
	 * WARNING:
	 * 	This test must be and _ever_ stay at first position as it build the entire workspace.
	 * 	It also cannot be removed as it's a Global fingerprint!
	 * 	Move it would have great consequence on all other tests results...
	 * 
	 * @throws CoreException
	 * @throws IOException
	 */
	public void testFullBuildDefault() throws CoreException, IOException {
		tagAsGlobalSummary("Compile>Build>Full>Wksp>Default warnings", true); // put in global fingerprint
		build(null, warningOptions(0/*default warnings*/), false);
	}

	/**
	 * JDT/Core project full build with no warning.
	 * 
	 * @throws CoreException
	 * @throws IOException
	 * @since 3.2 M6
	 */
	public void testFullBuildProjectNoWarning() throws CoreException, IOException {
		tagAsSummary("Compile>Build>Full>Project>No warning", true); // put in fingerprint
		build(JDT_CORE_PROJECT, warningOptions(-1/*no warning*/), true);
	}

	/**
	 * JDT/Core project full build with JavaCore default options.
	 * 
	 * @throws CoreException
	 * @throws IOException
	 * @since 3.2 M6
	 */
	public void testFullBuildProjectDefault() throws CoreException, IOException {
		tagAsSummary("Compile>Build>Full>Project>Default warnings", true); // put in fingerprint
		build(JDT_CORE_PROJECT, warningOptions(0/*default warnings*/), false);
	}

	/**
	 * JDT/Core project full build with all warnings.
	 * 
	 * @throws CoreException
	 * @throws IOException
	 * @since 3.2 M6
	 */
	public void testFullBuildProjectAllWarnings() throws CoreException, IOException {
		tagAsSummary("Compile>Build>Full>Project>All warnings", true); // put in fingerprint
		build(JDT_CORE_PROJECT, warningOptions(1/*all warnings*/), false);
	}

	/**
	 * Batch compiler build with no warning
	 * 
	 * Not calling tagAsSummary means that this test is currently evaluated
	 * before put it in builds performance results.
	 * 
	 * @throws IOException
	 * TODO (frederic) remove for 3.2 RC1
	 */
	public void testBatchCompilerNoWarning() throws IOException, CoreException {
		tagAsSummary("Compile>Batch>Compiler>No warning", false); // do NOT put in fingerprint
		compile(JavaCore.PLUGIN_ID, "-nowarn", true/*log errors*/, null);
	}

	/**
	 * Batch compiler build with default warnings
	 * 
	 * Not calling tagAsSummary means that this test is currently evaluated
	 * before put it in builds performance results.
	 * 
	 * @throws IOException
	 * TODO (frederic) remove for 3.2 RC1
	 */
	public void _testBatchCompilerDefault() throws IOException, CoreException {
		tagAsSummary("Compile>Batch>Compiler>Default warnings", false); // do NOT put in fingerprint
		compile(JavaCore.PLUGIN_ID, "", true/*log errors*/, null);
	}

	/**
	 * Batch compiler build with default javadoc warnings
	 * 
	 * Not calling tagAsSummary means that this test is currently evaluated
	 * before put it in builds performance results.
	 * 
	 * @throws IOException
	 * TODO (frederic) remove for 3.2 RC1
	 */
	public void _testBatchCompilerJavadoc() throws IOException, CoreException {
		tagAsSummary("Compile>Batch>Compiler>Javadoc warnings", false); // do NOT put in fingerprint
		compile(JavaCore.PLUGIN_ID, "-warn:javadoc", true/*log errors*/, null);
	}

	/**
	 * Batch compiler build with invalid javadoc warnings
	 * 
	 * Not calling tagAsSummary means that this test is currently evaluated
	 * before put it in builds performance results.
	 * 
	 * @throws IOException
	 * TODO (frederic) remove for 3.2 RC1
	 */
	public void _testBatchCompilerAllJavadoc() throws IOException, CoreException {
		tagAsSummary("Compile>Batch>Compiler>All Javadoc warnings", false); // do NOT put in fingerprint
		compile(JavaCore.PLUGIN_ID, "-warn:allJavadoc", true/*log errors*/, null);
	}

	/**
	 * Batch compiler build with all warnings
	 * 
	 * Not calling tagAsSummary means that this test is currently evaluated
	 * before put it in builds performance results.
	 * 
	 * @throws IOException
	 * TODO (frederic) remove for 3.2 RC1
	 */
	public void _testBatchCompilerAllWarnings() throws IOException, CoreException {
		tagAsSummary("Compile>Batch>Compiler>All warnings", false); // do NOT put in fingerprint
		compile(JavaCore.PLUGIN_ID, ALL_OPTIONS, true/*log errors*/, null);
	}

	/**
	 * Compile JDT/Core project with default warnings
	 * 
	 * @throws IOException
	 * @since 3.2 M6
	 */
	public void testCompileJDTCoreProjectNoWarning() throws IOException, CoreException {
		tagAsSummary("Compile>Project>JDT/Core>No warning", true); // put in fingerprint
		compile(JavaCore.PLUGIN_ID, "-nowarn", false/*no log*/, JDT_CORE_SRC_PATHS);
	}

	/**
	 * Compile JDT/Core project with default warnings
	 * 
	 * @throws IOException
	 * @since 3.2 M6
	 */
	public void testCompileJDTCoreProjectDefault() throws IOException, CoreException {
		tagAsSummary("Compile>Project>JDT/Core>Default warnings", true); // put in fingerprint
		compile(JavaCore.PLUGIN_ID, "", false/*no log*/, JDT_CORE_SRC_PATHS);
	}

	/**
	 * Compile JDT/Core project with default javadoc warnings
	 * 
	 * @throws IOException
	 * @since 3.2 M6
	 */
	public void testCompileJDTCoreProjectJavadoc() throws IOException, CoreException {
		tagAsSummary("Compile>Project>JDT/Core>Javadoc warnings", true); // put in fingerprint
		compile(JavaCore.PLUGIN_ID, "-warn:javadoc", false/*no log*/, JDT_CORE_SRC_PATHS);
	}

	/**
	 * Compile JDT/Core project with all warnings
	 * 
	 * @throws IOException
	 * @since 3.2 M6
	 */
	public void testCompileJDTCoreProjectAllWarnings() throws IOException, CoreException {
		tagAsSummary("Compile>Project>JDT/Core>All warnings", true); // put in fingerprint
		compile(JavaCore.PLUGIN_ID, ALL_OPTIONS, false/*no log*/, JDT_CORE_SRC_PATHS);
	}

	/**
	 * Compile JDT/Core project with default warnings
	 * 
	 * @throws IOException
	 * @since 3.2 M6
	 */
	public void testCompileSWTProjectDefault() throws IOException, CoreException {
		tagAsSummary("Compile>Project>SWT>Default warnings", true); // put in fingerprint
		String[] sourcePaths = {
				"Eclipse SWT/win32",
				"Eclipse SWT/common",
				"Eclipse SWT/common_j2se",
				"Eclipse SWT PI/win32",
				"Eclipse SWT PI/common_j2se",
				"Eclipse SWT OLE Win32/win32",
				"Eclipse SWT Accessibility/win32",
				"Eclipse SWT Accessibility/common",
				"Eclipse SWT AWT/win32",
				"Eclipse SWT AWT/common",
				"Eclipse SWT Drag and Drop/win32",
				"Eclipse SWT Drag and Drop/common",
				"Eclipse SWT Printing/win32",
				"Eclipse SWT Printing/common",
				"Eclipse SWT Program/win32",
				"Eclipse SWT Program/common",
				"Eclipse SWT Custom Widgets/common",
				"Eclipse SWT Browser/common",
				"Eclipse SWT Browser/win32",
		};
		compile("org.eclipse.swt", "", false/*no log*/, sourcePaths);
	}
}
