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
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

import junit.framework.*;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.batch.Main;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.test.performance.Dimension;

/**
 */
public class FullSourceWorkspaceBuildTests extends FullSourceWorkspaceTests {
	
	// Loops repeat
	private final static int REPEAT = 10;
	private final static int SCAN_REPEAT = 800; // 800 is default
	private final static long FILE_SIZE_THRESHOLD = 100000L; 	//100,000 characters
	private static int PARSE_REPEAT = 100; // xxx is default
	private static int TIME_THRESHOLD = 150;
	
	// Tests counter
	private static int TESTS_COUNT = 0;

	// Log files
	private static PrintStream[] LOG_STREAMS = new PrintStream[4];

	
	/**
	 * @param name
	 */
	public FullSourceWorkspaceBuildTests(String name) {
		super(name);
	}

	static {
//		TESTS_PREFIX = "testPerfScanner";
//		TESTS_NAMES = new String[] { "testPerfParserFiles" };
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
		TESTS_COUNT--;

		// Log perf result
		if (LOG_DIR != null) {
			logPerfResult(LOG_STREAMS, TESTS_COUNT);
		}

		super.tearDown();
	}

	// No javadoc support
	// This test is now redundant with following one. Just execute it as warm up...
	public void testPerfFullBuildNoComments() throws CoreException, IOException {
		tagAsSummary("Full source workspace build without comment support", Dimension.CPU_TIME);
		Hashtable options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.DISABLED);
		options.put(JavaCore.COMPILER_PB_INVALID_JAVADOC, JavaCore.IGNORE);
		options.put(JavaCore.COMPILER_PB_MISSING_JAVADOC_TAGS, JavaCore.IGNORE);
		options.put(JavaCore.COMPILER_PB_MISSING_JAVADOC_COMMENTS, JavaCore.IGNORE);
		startBuild(options);
	}

	// Set doc comment support
	public void testPerfFullBuild() throws CoreException, IOException {
		tagAsGlobalSummary("Full source workspace build", Dimension.CPU_TIME);
		startBuild(JavaCore.getDefaultOptions());
	}

	// Set invalid javadoc tags
	public void _testPerfWithInvalidJavadocTags() throws CoreException, IOException {
		tagAsSummary("Full build + Invalid Javadoc Tags", Dimension.CPU_TIME);
		Hashtable options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
		options.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
		options.put(JavaCore.COMPILER_PB_INVALID_JAVADOC, JavaCore.WARNING);
		options.put(JavaCore.COMPILER_PB_INVALID_JAVADOC_TAGS_VISIBILITY, JavaCore.PRIVATE);
		options.put(JavaCore.COMPILER_PB_INVALID_JAVADOC_TAGS, JavaCore.ENABLED);
		options.put(JavaCore.COMPILER_PB_MISSING_JAVADOC_TAGS, JavaCore.IGNORE);
		options.put(JavaCore.COMPILER_PB_MISSING_JAVADOC_COMMENTS, JavaCore.IGNORE);
		startBuild(options);
	}

	// Set missing javadoc tags
	public void _testPerfWithMissingJavadocTags() throws CoreException, IOException {
		tagAsSummary("Full build + Invalid & Missing Tags", Dimension.CPU_TIME);
		Hashtable options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
		options.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
		options.put(JavaCore.COMPILER_PB_INVALID_JAVADOC, JavaCore.WARNING);
		options.put(JavaCore.COMPILER_PB_INVALID_JAVADOC_TAGS_VISIBILITY, JavaCore.PRIVATE);
		options.put(JavaCore.COMPILER_PB_INVALID_JAVADOC_TAGS, JavaCore.ENABLED);
		options.put(JavaCore.COMPILER_PB_MISSING_JAVADOC_TAGS, JavaCore.WARNING);
		options.put(JavaCore.COMPILER_PB_MISSING_JAVADOC_TAGS_VISIBILITY, JavaCore.PRIVATE);
		options.put(JavaCore.COMPILER_PB_MISSING_JAVADOC_TAGS_OVERRIDING, JavaCore.ENABLED);
		options.put(JavaCore.COMPILER_PB_MISSING_JAVADOC_COMMENTS, JavaCore.IGNORE);
		startBuild(options);
	}

	// Set missing javadoc comments
	public void _testPerfWithMissingJavadocComments() throws CoreException, IOException {
		tagAsSummary("Full build + Full Javadoc", Dimension.CPU_TIME);
		Hashtable options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
		options.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
		options.put(JavaCore.COMPILER_PB_INVALID_JAVADOC, JavaCore.WARNING);
		options.put(JavaCore.COMPILER_PB_INVALID_JAVADOC_TAGS_VISIBILITY, JavaCore.PRIVATE);
		options.put(JavaCore.COMPILER_PB_INVALID_JAVADOC_TAGS, JavaCore.ENABLED);
		options.put(JavaCore.COMPILER_PB_MISSING_JAVADOC_TAGS, JavaCore.WARNING);
		options.put(JavaCore.COMPILER_PB_MISSING_JAVADOC_TAGS_VISIBILITY, JavaCore.PRIVATE);
		options.put(JavaCore.COMPILER_PB_MISSING_JAVADOC_TAGS_OVERRIDING, JavaCore.ENABLED);
		options.put(JavaCore.COMPILER_PB_MISSING_JAVADOC_COMMENTS, JavaCore.WARNING);
		options.put(JavaCore.COMPILER_PB_MISSING_JAVADOC_COMMENTS_VISIBILITY, JavaCore.PRIVATE);
		options.put(JavaCore.COMPILER_PB_MISSING_JAVADOC_COMMENTS_OVERRIDING, JavaCore.ENABLED);
		startBuild(options);
	}

	public void testPerfBuildCompilerUsingBatchCompiler() throws IOException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IWorkspaceRoot workspaceRoot = workspace.getRoot();
		final String targetWorkspacePath = workspaceRoot.getProject(JavaCore.PLUGIN_ID)
			.getLocation()
			.toFile()
			.getCanonicalPath();
		final String sources = targetWorkspacePath + File.separator + "compiler"; //$NON-NLS-1$
		final String bins = targetWorkspacePath + File.separator + "bin"; //$NON-NLS-1$
		final String logs = targetWorkspacePath + File.separator + "log.txt"; //$NON-NLS-1$

		// Note this test is not a finger print test, so we don't want to use
		// tagAsGlobalSummary(...)
		tagAsSummary("Build jdt-core/compiler using batch compiler", Dimension.CPU_TIME);

		// Compile 10 times
		Main.compile(sources + " -1.4 -g -preserveAllLocals -enableJavadoc -nowarn -d " + bins + " -log " + logs); //$NON-NLS-1$ //$NON-NLS-2$
		for (int i = 0; i < REPEAT; i++) {
			startMeasuring();
			Main.compile(sources + " -1.4 -g -preserveAllLocals -enableJavadoc -nowarn -d " + bins + " -log " + logs); //$NON-NLS-1$ //$NON-NLS-2$
			stopMeasuring();
			cleanupDirectory(new File(bins));
		}
		commitMeasurements();
		assertPerformance();

		File logsFile = new File(logs);
		assertTrue("No log file", logsFile.exists());
		assertEquals("Has errors", 0, logsFile.length());
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
		for (int i = 0; i < REPEAT; i++) {
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

	/**
	 * Test performance for Scanner on one file.
	 * Scan is executed many times ({@link #SCAN_REPEAT}) to have significant time for execution.
	 * This test is repeated several times ({@link #REPEAT}) to average time measuring.
	 *  
	 * @throws InvalidInputException
	 * @throws IOException
	 */
	public void testPerfScannerOneFile() throws InvalidInputException, IOException {

		// Note this test is not a finger print test, so we don't want to use tagAsGlobalSummary(...)
		tagAsSummary("Scanner", Dimension.CPU_TIME);

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


	/*
	 * Scan full source workspace files which are larger than a given limit of characters.
	 * Two kind of scan is currently possible:
	 * 	- 0: only scan all tokens
	 * 	- 1: scan all tokens and get each identifier
	 */
	private void scanFiles(final long limit, int kind) throws InvalidInputException, IOException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		String wkspPath = workspace.getRoot()
			.getLocation()
			.toFile()
			.getCanonicalPath();

		// Get files
		File wkspFile = new File(wkspPath);
		File[] wkspFiles = getAllFiles(wkspFile, new FileFilter() {
			public boolean accept(File pathname) {
				String name = pathname.getName();
				String path = pathname.getAbsolutePath().toLowerCase();
				long length = pathname.length();
				return !name.startsWith(".") && !name.equalsIgnoreCase("cvs") && ((pathname.isDirectory() || length >= limit && path.endsWith(".java")));
			}
		});
		if (DEBUG) {
			System.out.println("Number of files over "+limit+" chars in "+wkspPath+": " + wkspFiles.length);
		}

		// loop for time measuring
		Scanner scanner = new Scanner(true, true, true, ClassFileConstants.JDK1_4, null, null, false);
		int tokenCount = 0;
		long timeMax = 0;
		long timeMin = Integer.MAX_VALUE;
		String fileMin = null;
		String fileMax = null;
		int fileCount = 0;
		long size = 0;
		for (int i = 0, max = wkspFiles.length; i < max; i++) {

			// Get source out of time measuring
			char[] source = Util.getFileCharContent(wkspFiles[i], null);
			int sourceLength = source.length;
			fileCount++;
			scanner.setSource(source);
			long start = 0;
			if (DEBUG) {
				start = System.currentTimeMillis();
				System.out.println("	- file "+wkspFiles[i].getName()+":");
			}
			
			// Repeat scan to have a meaningful times to measure
			int repeat = (int) limit/200;
			if (DEBUG)
				System.out.println("	  repeat="+repeat);
			startMeasuring();
			for (int l = 0; l < repeat; l++) {
				scanner.resetTo(0, sourceLength);
				tokenize: while (true) {
					int token = scanner.getNextToken();
					switch (kind) {
						case 0: // first case: only read tokens
							switch (token) {
								case TerminalTokens.TokenNameEOF:
									break tokenize;
							}
							break;
						case 1: // second case: read tokens + create ids
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
			
			// Warn if measure time is not enough while debugging
			if (DEBUG) {
				long time = System.currentTimeMillis() - start;
				if (time < TIME_THRESHOLD) {
					System.err.println("	  length="+sourceLength+", time="+time);
				} else {
					System.out.println("	  length="+sourceLength+", time="+time);
				}
				if (time<timeMin) {
					timeMin = time;
					fileMin = wkspFiles[i].toString();
				}
				if (time>timeMax) {
					timeMax = time;
					fileMax = wkspFiles[i].toString();
				}
			}
		}
		if (DEBUG) {
			System.out.println("There was "+fileCount+" over "+limit+" characters:");
			System.out.println("	Time min="+timeMin+" for "+fileMin);
			System.out.println("	Time max="+timeMax+" for "+fileMax);
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

	/**
	 * Test performance for Scanner on several files.
	 * Scan is executed many times depending on size of file (times = size / 200) to have significant time for execution.
	 * This test is repeated once for each file over {@link #FILE_SIZE_THRESHOLD} found in workspace to average time measuring.
	 *  
	 * @throws InvalidInputException
	 * @throws IOException
	 */
	public void testPerfScannerFiles() throws InvalidInputException, IOException {

		// Note this test is not a finger print test, so we don't want to use tagAsGlobalSummary(...)
		tagAsSummary("Scan Files", Dimension.CPU_TIME);

		// Run test
//		scanFiles(100000L, 0);
		scanFiles(100000L, 1);
	}

	/*
	 * Parse several times a file giving its name.
	 */
	private long[] parseFile(String fileName, int repeat) throws InvalidInputException, IOException {

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
        Parser parser = new Parser(problemReporter, true);

		// warm-up
		for (int i = 0; i < 2; i++) {
			ICompilationUnit unit = new CompilationUnit(content, file.getName(), null);
			CompilationResult unitResult = new CompilationResult(unit, 0, 1, options.maxProblemsPerUnit);				
			CompilationUnitDeclaration unitDeclaration = parser.dietParse(unit, unitResult);
			parser.getMethodBodies(unitDeclaration);
		}

		// loop for time measuring
		long parsedLines = 0;
		long parsedCharacters = 0;
		long start = 0;
		startMeasuring();
		for (int i = 0; i < REPEAT; i++) {
			if (DEBUG) {
				start = System.currentTimeMillis();
			}
			// Measure time for parse
			for (int j = 0; j < repeat; j++) {
				ICompilationUnit unit = new CompilationUnit(content, file.getName(), null);
				CompilationResult unitResult = new CompilationResult(unit, 0, 1, options.maxProblemsPerUnit);				
				CompilationUnitDeclaration unitDeclaration = parser.dietParse(unit, unitResult);
				parser.getMethodBodies(unitDeclaration);
				parsedCharacters += content.length;
				parsedLines += unitResult.lineSeparatorPositions.length;
            }

			// Warn if measure time is not enough while debugging
			if (DEBUG) {
				long time = System.currentTimeMillis() - start;
				if (time < TIME_THRESHOLD) {
		            System.err.print(parsedLines + " lines/"+ parsedCharacters + " characters parsed");
				} else {
		            System.out.print(parsedLines + " lines/"+ parsedCharacters + " characters parsed");
				}
			}
		}
		stopMeasuring();

		// Return stats
		return new long[] { parsedCharacters, parsedLines };
	}


	/**
	 * Test performance for Parser on one file.
	 * Parse is executed many times ({@link #PARSE_REPEAT}) to have significant time for execution.
	 * This test is repeated several times ({@link #REPEAT}) to average time measuring.
	 *  
	 * @throws InvalidInputException
	 * @throws IOException
	 */
	public void testPerfParserOneFile() throws InvalidInputException, IOException {

		// Note this test is not a finger print test, so we don't want to use tagAsGlobalSummary(...)
		tagAsSummary("Parser", Dimension.CPU_TIME);

		// Get workspace path
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IWorkspaceRoot workspaceRoot = workspace.getRoot();
		final String targetWorkspacePath = workspaceRoot.getProject(JavaCore.PLUGIN_ID)
			.getLocation()
			.toFile()
			.getCanonicalPath();
		
		// Run test
		parseFile(targetWorkspacePath+"/compiler/org/eclipse/jdt/internal/compiler/parser/Parser.java", PARSE_REPEAT);

		// dump measure
		commitMeasurements();
		assertPerformance();
	}

	/*
	 * Parse full source workspace files which are larger than a given limit of characters.
	 */
	private void parseFiles(final long limit) throws InvalidInputException, IOException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		String wkspPath = workspace.getRoot()
			.getLocation()
			.toFile()
			.getCanonicalPath();

		// Get files
		File wkspFile = new File(wkspPath);
		File[] wkspFiles = getAllFiles(wkspFile, new FileFilter() {
			public boolean accept(File pathname) {
				String name = pathname.getName();
				String path = pathname.getAbsolutePath().toLowerCase();
				long length = pathname.length();
				return !name.startsWith(".") && !name.equalsIgnoreCase("cvs") && ((pathname.isDirectory() || length >= limit && path.endsWith(".java")));
			}
		});
		if (DEBUG) {
			System.out.println("Number of files over "+limit+" chars in "+wkspPath+": " + wkspFiles.length);
		}

		// loop for time measuring
		long parsedCharacters = 0;
		long parsedLines = 0;
		for (int i = 0, max = wkspFiles.length; i < max; i++) {
			long[] stats = parseFile(wkspFiles[i].getCanonicalPath(), PARSE_REPEAT/10);
			parsedCharacters += stats[0];
			parsedLines += stats[1];
		}

		// Debug
		if (DEBUG) {
            System.out.print("Finally, "+ parsedLines + " lines/"+ parsedCharacters + " characters were parsed");
		}
	}

	/**
	 * Test performance for Parser on several files.
	 * Parse is executed many times ({@link #PARSE_REPEAT}/10) to have significant time for execution.
	 * This test is repeated once for each file over {@link #FILE_SIZE_THRESHOLD} characters
	 * found in workspace to average time measuring.
	 *  
	 * @throws InvalidInputException
	 * @throws IOException
	 */
	public void testPerfParserFiles() throws InvalidInputException, IOException {

		// Note this test is not a finger print test, so we don't want to use tagAsGlobalSummary(...)
		tagAsSummary("Parse Files", Dimension.CPU_TIME);

		// Run test
		parseFiles(100000L);

		// dump measure
		commitMeasurements();
		assertPerformance();
	}
}
