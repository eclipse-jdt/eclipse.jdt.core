/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.formatter;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import junit.framework.AssertionFailedError;
import junit.framework.ComparisonFailure;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.core.tests.model.ModelTestsUtil;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.ScannerHelper;
import org.eclipse.jdt.internal.core.util.CodeSnippetParsingUtil;
import org.eclipse.jdt.internal.core.util.SimpleDocument;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatter;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatterOptions;
import org.eclipse.text.edits.TextEdit;

/**
 * Comment formatter test suite for massive tests at a given location.
 * <p>
 * This test suite has only one generic test. The tests are dynamically defined by
 * getting all compilation units located at the running workspace or at the
 * directory specified using the "dir" system property and create
 * one test per unit.
 * </p><p>
 * The test consists in first format the compilation unit using the new comments
 * formatter (i.e. since bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=102780
 * has been fixed) and second eventually compare it with the output that the
 * previous comments formatter would have done.
 * </p><p>
 * So, if no comparison is done, the test only insure that the new formatter does
 * not blow up while formatting the files found at the given location and that the
 * second formatting gives the same output than the first one.
 * </p><p>
 * TODO See how fix the remaining failing tests when comparing the
 * formatting of JUnit 3.8.2 files:
 * <ul>
 * 	<li>0 error</li>
 * 	<li>0 failure</li>
 * 	<li>0 file has different line leading spaces than old formatter</li>
 * 	<li>23 files have spaces differences with old formatter</li>
 *		</ul>
 * </p><p>
 * TODO Fix failures while running on workspaces without comparing.
 * 
 * It is not possible to continue to compare the entire files after 2 formatting
 * as the code formatter cannot handle properly following snippet:
 * <pre>
 * public class X02 {
 * 	int field; // This is a long comment that should be split in multiple line comments in case the line comment formatting is enabled
 * }
 * </pre>
 * Which is formatted as:
 * <pre>
 * public class X02 {
 * 	int field; // This is a long comment that should be split in multiple line
 * 				// comments in case the line comment formatting is enabled
 * }
 * </pre>
 * But got a different output if formatted again:
 * <pre>
 * public class X02 {
 * 	int field; // This is a long comment that should be split in multiple line
 * 	// comments in case the line comment formatting is enabled
 * }
 * </pre>
 * 
 * So, we're now obliged to ignore some whitespaces using the system property
 *  <code>ignoreWhitespaces</code> while running a launch config on this
 * test suite on big workspaces as full source perfs 3.0 or ganymede.
 * 
 * Here are the results when setting the system property to
 * <code>linesLeading</code> (e.g. ignore white spaces at the beginning of the
 * lines, including the star inside javadoc or block comments):
 * <ul>
 * 	<li>3.0 performance workspace (9951 units):<ul>
 * 		<li>0 error</li>
 * 		<li>0 failures</li>
 * 		<li>8 failures due to old formatter</li>
 * 		<li>722 files have different lines leading spaces</li>
 * 		<li>9 files have different spaces</li>
 *		</ul></li>
 *		<li>ganymede M5 workspace (25819 units):<ul>
 * 		<li>0 error</li>
 * 		<li>17 failures due to different output while reformatting!</li>
 * 		<li>15 failures due to old formatter</li>
 * 		<li>1368 files have different line leading spaces when reformatting!</li>
 * 		<li>14 files have different spaces when reformatting!</li>
 *		</ul></li>
 *		<li>ganymede M6a workspace (26336 units):<ul>
 * 		<li>0 error</li>
 * 		<li>16 failures due to different output while reformatting!</li>
 * 		<li>17 failures due to old formatter</li>
 * 		<li>1469 files have different line leading spaces when reformatting!</li>
 * 		<li>14 files have different spaces when reformatting!</li>
 *		</ul></li>
 * </ul>
 */
public class FormatterCommentsMassiveTests extends FormatterRegressionTests {

	private static final String LINE_SEPARATOR = org.eclipse.jdt.internal.compiler.util.Util.LINE_SEPARATOR;
	final File file;
	final String path;
	List failures = new ArrayList();
	List expectedFailures = new ArrayList();
	List leadingWhitespacesFailures = new ArrayList();
	List whitespacesFailures= new ArrayList();
	boolean hasSpaceFailure;
	private final static boolean DEBUG_TESTS = "true".equals(System.getProperty("debugTests"));
	private final static String DIR = System.getProperty("dir"); //$NON-NLS-1$
	private final static boolean COMPARE = DefaultCodeFormatterConstants.TRUE.equals(System.getProperty("compare")); //$NON-NLS-1$
	private final static int IGNORE_SPACES;
	private final static int ALL_SPACES = 1;	// ignore all spaces
	private final static int LINES_LEADING_SPACES = 2;	// ignore all spaces at the beginning of all lines
	private final static int ALL_COMMENTS_SPACES = 3;	// ignore all spaces inside all comments
	private final static int ALL_COMMENTS_LINES_LEADING_SPACES = 4;	// ignore all spaces at the beginning of all comments lines
	static {
		String ignoreSpaces = System.getProperty("ignoreSpaces"); //$NON-NLS-1$
		int filterValue;
		if ("all".equals(ignoreSpaces)) {
			filterValue = ALL_SPACES;
		} else if ("linesLeading".equals(ignoreSpaces)) {
			filterValue = LINES_LEADING_SPACES;
		} else if ("comments".equals(ignoreSpaces)) {
			filterValue = ALL_COMMENTS_SPACES;
		} else if ("commentsLinesLeading".equals(ignoreSpaces)) {
			filterValue = ALL_COMMENTS_LINES_LEADING_SPACES;
		} else {
			filterValue = 0; // no filter
		}
		IGNORE_SPACES = filterValue;
	}
	private final static int FORMAT_REPEAT  = Integer.parseInt(System.getProperty("repeat", "2")); //$NON-NLS-1$
	private static final int MAX_FAILURES = Integer.parseInt(System.getProperty("maxFailures", "100")); // Max failures using string comparison
	private static boolean ASSERT_EQUALS_STRINGS = MAX_FAILURES > 0;
	private final static String[] EXPECTED_FAILURES = DIR.indexOf("ganymede") < 0
		? new String[] {
			"org\\eclipse\\jdt\\internal\\compiler\\ast\\QualifiedNameReference.java",
			"org\\eclipse\\jdt\\internal\\eval\\CodeSnippetSingleNameReference.java",
			"org\\eclipse\\jdt\\internal\\core\\DeltaProcessor.java",
			"org\\eclipse\\jdt\\internal\\core\\JavaProject.java",
			"org\\eclipse\\jdt\\internal\\core\\search\\indexing\\IndexManager.java",
			"org\\eclipse\\team\\internal\\ccvs\\ui\\AnnotateView.java",
			"org\\eclipse\\team\\internal\\ccvs\\ui\\HistoryView.java",
			"org\\eclipse\\team\\internal\\ccvs\\ui\\wizards\\UpdateWizard.java",
		}
		:	new String[] {
			"org\\eclipse\\jdt\\core\\JavaCore.java",
			"org\\eclipse\\jdt\\internal\\codeassist\\CompletionEngine.java",
			"org\\eclipse\\jdt\\internal\\codeassist\\SelectionEngine.java",
			"org\\eclipse\\jdt\\internal\\compiler\\ast\\QualifiedNameReference.java",
			"org\\eclipse\\jdt\\internal\\compiler\\ast\\SingleNameReference.java",
			"org\\eclipse\\jdt\\internal\\eval\\CodeSnippetSingleNameReference.java",
			"org\\eclipse\\jdt\\internal\\compiler\\batch\\Main.java",
			"org\\eclipse\\jdt\\internal\\compiler\\lookup\\ParameterizedMethodBinding.java",
			"org\\eclipse\\jdt\\internal\\core\\CompilationUnit.java",
			"org\\eclipse\\jdt\\internal\\core\\ExternalJavaProject.java",
			"org\\eclipse\\jdt\\internal\\core\\hierarchy\\HierarchyResolver.java",
			"org\\eclipse\\jdt\\internal\\core\\hierarchy\\TypeHierarchy.java",
			"org\\eclipse\\jdt\\internal\\core\\search\\indexing\\IndexAllProject.java",
			"org\\eclipse\\jdt\\internal\\core\\search\\JavaSearchScope.java",
			"org\\eclipse\\jdt\\internal\\eval\\EvaluationContext.java",
			"org\\eclipse\\jdt\\internal\\ui\\text\\javadoc\\JavadocContentAccess2.java",
			"org\\eclipse\\team\\internal\\ccvs\\ui\\mappings\\WorkspaceSubscriberContext.java",
	};
	static {
		// Sort expected failures to allow binary search
		Arrays.sort(EXPECTED_FAILURES);
	}
	
public static Test suite() {
	TestSuite suite = new Suite(FormatterCommentsMassiveTests.class.getName());
	try {
		File testDir = ModelTestsUtil.getWorkspaceRoot().getLocation().toFile();
		if (DIR != null) {
			File dir = new File(DIR);
			if (dir.exists()) testDir = dir;
		}
		FileFilter filter = new FileFilter() {
			public boolean accept(File pathname) {
	            return pathname.isDirectory() || pathname.getPath().endsWith(".java");
            }
		};
		long start = System.currentTimeMillis();
		System.out.print("Get all Java files located in "+testDir+"...");
		File[] allFiles = ModelTestsUtil.getAllFiles(testDir, filter);
		int length = allFiles.length;
		System.out.println(length+" found in " + (System.currentTimeMillis() - start) + "ms");
		for (int i=0; i<length; i++) {
			suite.addTest(new FormatterCommentsMassiveTests(allFiles[i]));
		}
//		ASSERT_EQUALS_STRINGS = length < 15000; 
    } catch (Exception e) {
    	// skip
    }
	return suite;
}

public FormatterCommentsMassiveTests(File file) {
	super("testCompare");
	this.file = file;
	this.path = file.getPath().toString().substring(DIR.length()+1);
}

/* (non-Javadoc)
 * @see junit.framework.TestCase#getName()
 */
public String getName() {
	return super.getName()+" - " + this.path;
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.core.tests.formatter.FormatterRegressionTests#setUpSuite()
 */
public void setUp() throws Exception {
	super.setUp();
	this.hasSpaceFailure = false;
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.core.tests.formatter.FormatterRegressionTests#setUpSuite()
 */
public void setUpSuite() throws Exception {
	// skip standard model suite set up
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.core.tests.formatter.FormatterRegressionTests#tearDown()
 */
public void tearDown() throws Exception {
	// skip standard model tear down
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.core.tests.formatter.FormatterRegressionTests#tearDownSuite()
 */
public void tearDownSuite() throws Exception {
	// skip standard model suite tear down
	int sFailures = this.failures.size();
	int seFailures = this.expectedFailures.size();
	int swFailures = this.whitespacesFailures.size();
	int slwFailures = this.leadingWhitespacesFailures.size();
	String failuresType = COMPARE ? "than old formatter" : "when reformatting";
	System.out.println();
	if (sFailures > 0) {
		System.out.println(sFailures+" files has still different output while reformatting!");
	}
	if (seFailures > 0) {
		System.out.println(seFailures+" files has still different output while reformatting due to old formatter bugs!");
	}
	if (slwFailures == 0) {
		System.out.println("No file has different line leading spaces "+failuresType+" :-)");
	} else {
		System.out.println(slwFailures+" files have different line leading spaces "+failuresType+"!");
	}
	if (swFailures > 0) {
		System.out.println(swFailures+" files have different spaces "+failuresType+"!");
	}
	System.out.println();
	if (sFailures > 0) {
		System.out.println("List of files with different output "+failuresType+":");
		for (int i=0; i<sFailures; i++) {
			System.out.println("	- "+this.failures.get(i));
		}
	}
	if (seFailures > 0) {
		System.out.println("List of files with different output "+failuresType+" (due to old formatter bugs):");
		for (int i=0; i<seFailures; i++) {
			System.out.println("	- "+this.expectedFailures.get(i));
		}
	}
	if (slwFailures > 0) {
		System.out.println("List of files with different line leading spaces "+failuresType+":");
		for (int i=0; i<slwFailures; i++) {
			System.out.println("	- "+this.leadingWhitespacesFailures.get(i));
		}
	}
	if (swFailures > 0) {
		System.out.println("List of files with different spaces "+failuresType+":");
		for (int i=0; i<swFailures; i++) {
			System.out.println("	- "+this.whitespacesFailures.get(i));
		}
	}
}

/*
 * Asserts that the given actual source (usually coming from a file content) is equal to the expected one.
 * Note that 'expected' is assumed to have the '\n' line separator. 
 * The line separators in 'actual' are converted to '\n' before the comparison.
 */
protected void assertSourceEquals(String message, String expected, String actual) {
	if (actual == null) {
		assertEquals(message, expected, null);
		return;
	}
	actual = Util.convertToIndependantLineDelimiter(actual);
	try {
		if (ASSERT_EQUALS_STRINGS) {
			assertEquals(message, expected, actual);
		} else {
			assertTrue(message, actual.equals(expected));
		}
	}
	catch (ComparisonFailure cf) {
		if (COMPARE) {
			String trimmedExpected;
			String trimmedActual;
			switch (IGNORE_SPACES) {
				case ALL_SPACES:
					trimmedExpected = ModelTestsUtil.removeWhiteSpace(expected);
					trimmedActual= ModelTestsUtil.removeWhiteSpace(actual);
					if (trimmedExpected.equals(trimmedActual)) {
						this.whitespacesFailures.add(this.path);
						return;
					}
					break;
				case LINES_LEADING_SPACES:
					trimmedExpected = ModelTestsUtil.trimLinesLeadingWhitespaces(expected);
					trimmedActual= ModelTestsUtil.trimLinesLeadingWhitespaces(actual);
					if (trimmedExpected.equals(trimmedActual)) {
						this.leadingWhitespacesFailures.add(this.path);
						return;
					}
					trimmedExpected = ModelTestsUtil.removeWhiteSpace(expected);
					trimmedActual= ModelTestsUtil.removeWhiteSpace(actual);
					if (trimmedExpected.equals(trimmedActual)) {
						this.whitespacesFailures.add(this.path);
						return;
					}
					break;
				default:
					trimmedExpected = filterFormattingInComments(expected);
					trimmedActual= filterFormattingInComments(actual);
					if (trimmedExpected.equals(trimmedActual)) {
						this.whitespacesFailures.add(this.path);
						return;
					}
					break;
			}
			if (DEBUG_TESTS && ASSERT_EQUALS_STRINGS) {
				assertEquals(message, trimmedExpected, trimmedActual);
			}
		}
		this.failures.add(this.path);
		ASSERT_EQUALS_STRINGS = this.failures.size() < MAX_FAILURES;
		throw cf;
	}
	catch (AssertionFailedError afe) {
		this.failures.add(this.path);
		throw afe;
	}
}

DefaultCodeFormatter codeFormatter() {
	DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getEclipseDefaultSettings();
	DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
	return codeFormatter;
}

void compareFormattedSource() throws IOException, Exception {
	DefaultCodeFormatter codeFormatter = codeFormatter();
	String source = new String(org.eclipse.jdt.internal.compiler.util.Util.getFileCharContent(this.file, null));
	try {
		String actualResult = runFormatter(codeFormatter, source, CodeFormatter.K_COMPILATION_UNIT | CodeFormatter.F_INCLUDE_COMMENTS, 0, 0, source.length(), null);
		if (!this.hasSpaceFailure && COMPARE) {
			String expectedResult = expectedFormattedSource(source);
			assertLineEquals(actualResult, source, expectedResult, false);
		}
	}
	catch (Exception e) {
		System.err.println(e.getMessage()+" occurred in "+getName());
		throw e;
	}
}

private String expectedFormattedSource(String source) {
	boolean enableNewCommentFormatter = DefaultCodeFormatter.ENABLE_NEW_COMMENTS_FORMAT;
	try {
		DefaultCodeFormatter.ENABLE_NEW_COMMENTS_FORMAT = false;
		DefaultCodeFormatter codeFormatter = codeFormatter();
		Scanner scanner = new Scanner(true, true, false/*nls*/, ClassFileConstants.JDK1_4/*sourceLevel*/, null/*taskTags*/, null/*taskPriorities*/, true/*taskCaseSensitive*/);
		CodeSnippetParsingUtil codeSnippetParsingUtil = new CodeSnippetParsingUtil();
		CompilationUnitDeclaration compilationUnitDeclaration = codeSnippetParsingUtil.parseCompilationUnit(source.toCharArray(), getDefaultCompilerOptions(), true);
		final TypeDeclaration[] types = compilationUnitDeclaration.types;
		int headerEndPosition = types == null ? compilationUnitDeclaration.sourceEnd : types[0].declarationSourceStart;
		scanner.setSource(source.toCharArray());
		scanner.lineEnds = codeSnippetParsingUtil.recordedParsingInformation.lineEnds;
		int[][] commentsPositions = compilationUnitDeclaration.comments;
		int length = commentsPositions == null ? 0 : commentsPositions.length;
		String[] formattedComments = new String[length];
		for (int i=0; i<length; i++) {
			int[] positions = commentsPositions[i];
			int commentKind = CodeFormatter.K_JAVA_DOC;
			int commentStart = positions [0];
			int commentEnd = positions [1];
			if (commentEnd < 0) { // line or block comments have negative end position
				commentEnd = -commentEnd;
				if (commentStart > 0) { // block comments have positive start position
					commentKind = CodeFormatter.K_MULTI_LINE_COMMENT;
				} else {
					commentStart = -commentStart;
					commentKind = CodeFormatter.K_SINGLE_LINE_COMMENT;
				}
			}
			if (commentStart >= headerEndPosition) {
				int indentationLevel = getIndentationLevel(scanner, commentStart);
				formattedComments[i] = runFormatter(codeFormatter, source.substring(commentStart, commentEnd), commentKind, indentationLevel, 0, commentEnd - commentStart, LINE_SEPARATOR);
			}
		}
		SimpleDocument document = new SimpleDocument(source);
		for (int i=length-1; i>=0; i--) {
			if (formattedComments[i] != null) {
				int[] positions = commentsPositions[i];
				int commentStart = positions [0];
				int commentEnd = positions [1];
				if (commentEnd < 0) { // line or block comments have negative end position
					commentEnd = -commentEnd;
					if (commentStart < 0) { // line comments have negative start position
						commentStart = -commentStart;
					}
				}
				document.replace(commentStart, commentEnd - commentStart, formattedComments[i]);
			}
		}
		String newSource = document.get();
		String oldResult = runFormatter(codeFormatter, newSource, CodeFormatter.K_COMPILATION_UNIT, 0, 0, newSource.length(), null);
		return oldResult == null ? newSource : oldResult;
	}
	finally {
		DefaultCodeFormatter.ENABLE_NEW_COMMENTS_FORMAT = enableNewCommentFormatter;
	}
}
private String filterFormattingInComments(String input) {
	StringTokenizer tokenizer = new StringTokenizer(input, "\r\n\f");
	StringBuffer buffer = new StringBuffer();
	boolean skipToken = false;
	String line =  null;
	lineLoop: while (tokenizer.hasMoreTokens()) {
		if (!skipToken) {
			line = tokenizer.nextToken();
		}
		skipToken = false;
		int length = line.length();
		int lineStart = 0;
		if (length > 0) {
			// Trim leading whitespaces
			if (IGNORE_SPACES > 0) {
				while (lineStart < length && ScannerHelper.isWhitespace(line.charAt(lineStart))) {
					lineStart++;
				}
			}
			// Search if a comment starts
			int commentKind = 0;
			int idx = line.indexOf('/', lineStart);
			if (idx >= 0 && (idx+1) < length) {
				idx++;
				char ch = line.charAt(idx++);
				switch (ch) {
					case '/':
						commentKind = 1; // line comment
						break;
					case '*':
						commentKind = 2; // block comment
						if (idx < length && line.charAt(idx) == '*') {
							commentKind = 3; // javadoc comment
							idx++;
						}
						break;
				}
				if (commentKind != 0) {
					// Enter a comment
					switch (IGNORE_SPACES) {
						case ALL_COMMENTS_SPACES:
							switch (commentKind) {
								case 1:
									int start = idx;
									buffer.append(line.substring(0, start).trim());
									while (true) {
										if (start < length) {
											while (start < length && ScannerHelper.isWhitespace(line.charAt(start))) {
												start++;
											}
											buffer.append(ModelTestsUtil.removeWhiteSpace(line.substring(start)));
										}
										line = tokenizer.nextToken();
										length = line.length();
										start = 0;
										while (start < length && ScannerHelper.isWhitespace(line.charAt(start))) {
											start++;
										}
										if (start > length+1 || line.charAt(start) != '/' || line.charAt(start+1) != '/') {
											buffer.append('\n');
											skipToken = true;
											// only gate to break the loop
											continue lineLoop;
										}
										start += 2;
									}
								case 2:
								case 3:
									buffer.append(line.substring(0, idx).trim());
									int endComment = line.indexOf("*/");
									if (endComment > 0) {
										buffer.append(ModelTestsUtil.removeWhiteSpace(line.substring(0, endComment + 2)));
										line = line.substring(endComment+2);
										skipToken = true;
										continue lineLoop;
									}
									while (endComment < 0) {
										buffer.append(ModelTestsUtil.removeWhiteSpace(line));
										line = tokenizer.nextToken();
										endComment = line.indexOf("*/");
									}
									buffer.append(ModelTestsUtil.removeWhiteSpace(line.substring(0, endComment + 2)));
									buffer.append('\n');
									continue;
							}
							break;
						case ALL_COMMENTS_LINES_LEADING_SPACES:
							switch (commentKind) {
								case 1:
									int start = idx;
									buffer.append(line.substring(0, start).trim());
									while (true) {
										if (start < length) {
											while (start < length && ScannerHelper.isWhitespace(line.charAt(start))) {
												start++;
											}
											if (start < length) {
												buffer.append(line.substring(start));
											}
										}
										line = tokenizer.nextToken();
										length = line.length();
										start = 0;
										while (start < length && ScannerHelper.isWhitespace(line.charAt(start))) {
											start++;
										}
										if (start < length && (line.charAt(start) != '/' || line.charAt(start+1) != '/')) {
											buffer.append('\n');
											skipToken = true;
											// only gate to break the loop
											continue lineLoop;
										}
										buffer.append(' ');
										start += 2; // skip next line starting comment
									}
								case 3:
								case 2:
									start = idx;
									int endComment = line.indexOf("*/");
									if (endComment > 0) {
										buffer.append(line.substring(0, endComment + 2));
										line = line.substring(endComment+2);
										skipToken = true;
										continue lineLoop;
									}
									buffer.append(line.substring(0, start).trim());
									while (endComment < 0) {
										if (start < length) {
											while (start < length && ScannerHelper.isWhitespace(line.charAt(start))) {
												start++;
											}
											if (start < length && ch == '*') {
												start++;
												while (start < length && ScannerHelper.isWhitespace(line.charAt(start))) {
													start++;
												}
											}
											if (start < length) {
												buffer.append(line.substring(start));
											}
										}
										line = tokenizer.nextToken();
										length = line.length();
										endComment = line.indexOf("*/");
										start = 0;
										buffer.append(' ');
									}
									buffer.append(line.substring(0, endComment + 2));
									buffer.append('\n');
									continue;
							}
					}
				}
			}
		}
		if (length > 0 && lineStart > 0 && lineStart < length) {
			buffer.append(line.substring(lineStart).trim());
		} else {
			buffer.append(line);
		}
		buffer.append('\n');
	}
    return buffer.toString();
}

private int getIndentationLevel(Scanner scanner, int position) {
	int indentationLevel = 0;
	int numberOfIndentations = 0;
	int indentationSize;
	try {
		indentationSize = Integer.parseInt(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE);
	} catch (NumberFormatException nfe) {
		indentationSize = 4;
	}
	int lineNumber = scanner.getLineNumber(position);
	int lineStart = scanner.getLineStart(lineNumber);
	scanner.resetTo(lineStart, position-1);
	while (!scanner.atEnd()) {
		int ch = scanner.getNextChar();
		switch (ch) {
			case '\n':
				indentationLevel = 0;
				numberOfIndentations = 0;
				break;
			case '\t':
				numberOfIndentations++;
				indentationLevel = numberOfIndentations * indentationSize;
				break;
			default:
				indentationLevel++;
				if ((indentationLevel%indentationSize) == 0) {
					numberOfIndentations++;
				}
				break;
		}
	}
	if ((indentationLevel%indentationSize) != 0) {
		numberOfIndentations++;
		indentationLevel = numberOfIndentations * indentationSize;
	}
	return numberOfIndentations;
}

private Map getDefaultCompilerOptions() {
	Map optionsMap = new HashMap(30);
	optionsMap.put(CompilerOptions.OPTION_LocalVariableAttribute, CompilerOptions.DO_NOT_GENERATE); 
	optionsMap.put(CompilerOptions.OPTION_LineNumberAttribute, CompilerOptions.DO_NOT_GENERATE);
	optionsMap.put(CompilerOptions.OPTION_SourceFileAttribute, CompilerOptions.DO_NOT_GENERATE);
	optionsMap.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);
	optionsMap.put(CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.DISABLED); 
	optionsMap.put(CompilerOptions.OPTION_ReportMethodWithConstructorName, CompilerOptions.IGNORE); 
	optionsMap.put(CompilerOptions.OPTION_ReportOverridingPackageDefaultMethod, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportOverridingMethodWithoutSuperInvocation, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportDeprecationInDeprecatedCode, CompilerOptions.DISABLED); 
	optionsMap.put(CompilerOptions.OPTION_ReportDeprecationWhenOverridingDeprecatedMethod, CompilerOptions.DISABLED); 
	optionsMap.put(CompilerOptions.OPTION_ReportHiddenCatchBlock, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportUnusedParameter, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportUnusedImport, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportNoEffectAssignment, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportNoImplicitStringConversion, CompilerOptions.IGNORE); 
	optionsMap.put(CompilerOptions.OPTION_ReportNonStaticAccessToStatic, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportIndirectStaticAccess, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportIncompatibleNonInheritedInterfaceMethod, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportLocalVariableHiding, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportFieldHiding, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportPossibleAccidentalBooleanAssignment, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportEmptyStatement, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportAssertIdentifier, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportEnumIdentifier, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportUndocumentedEmptyBlock, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportInvalidJavadoc, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsVisibility, CompilerOptions.PUBLIC);
	optionsMap.put(CompilerOptions.OPTION_ReportInvalidJavadocTags, CompilerOptions.DISABLED);
	optionsMap.put(CompilerOptions.OPTION_ReportMissingJavadocTagDescription, CompilerOptions.RETURN_TAG);
	optionsMap.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsDeprecatedRef, CompilerOptions.DISABLED);
	optionsMap.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsNotVisibleRef, CompilerOptions.DISABLED);
	optionsMap.put(CompilerOptions.OPTION_ReportMissingJavadocTags, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportMissingJavadocTagsVisibility, CompilerOptions.PUBLIC);
	optionsMap.put(CompilerOptions.OPTION_ReportMissingJavadocTagsOverriding, CompilerOptions.DISABLED);
	optionsMap.put(CompilerOptions.OPTION_ReportMissingJavadocComments, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportMissingJavadocCommentsVisibility, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportMissingJavadocCommentsOverriding, CompilerOptions.DISABLED);
	optionsMap.put(CompilerOptions.OPTION_ReportFinallyBlockNotCompletingNormally, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownException, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionWhenOverriding, CompilerOptions.DISABLED); 
	optionsMap.put(CompilerOptions.OPTION_ReportUnqualifiedFieldAccess, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_4);
	optionsMap.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_2); 
	optionsMap.put(CompilerOptions.OPTION_TaskTags, ""); //$NON-NLS-1$
	optionsMap.put(CompilerOptions.OPTION_TaskPriorities, ""); //$NON-NLS-1$
	optionsMap.put(CompilerOptions.OPTION_TaskCaseSensitive, CompilerOptions.DISABLED);
	optionsMap.put(CompilerOptions.OPTION_ReportUnusedParameterWhenImplementingAbstract, CompilerOptions.DISABLED); 
	optionsMap.put(CompilerOptions.OPTION_ReportUnusedParameterWhenOverridingConcrete, CompilerOptions.DISABLED); 
	optionsMap.put(CompilerOptions.OPTION_ReportSpecialParameterHidingField, CompilerOptions.DISABLED); 
	optionsMap.put(CompilerOptions.OPTION_MaxProblemPerUnit, String.valueOf(100));
	optionsMap.put(CompilerOptions.OPTION_InlineJsr, CompilerOptions.DISABLED); 
	optionsMap.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);
	return optionsMap;
}

private boolean isExpectedFailure() {
	int length = EXPECTED_FAILURES.length;
	for (int i=0; i<length; i++) {
		if (this.path.endsWith(EXPECTED_FAILURES[i])) {
			this.expectedFailures.add(this.path);
			return true;
		}
	}
	return false;
}

String runFormatter(CodeFormatter codeFormatter, String source, int kind, int indentationLevel, int offset, int length, String lineSeparator) {
	TextEdit edit = codeFormatter.format(kind, source, offset, length, indentationLevel, lineSeparator);//$NON-NLS-1$
	if (edit == null) return null;
	String result = org.eclipse.jdt.internal.core.util.Util.editedString(source, edit);

	int count = 1;
	if (!COMPARE && length == source.length()) {
		while (count++ < FORMAT_REPEAT) {
			edit = codeFormatter.format(kind, result, 0, result.length(), indentationLevel, lineSeparator);//$NON-NLS-1$
			if (edit == null) return null;
			String newResult = org.eclipse.jdt.internal.core.util.Util.editedString(result, edit);
			if (!result.equals(newResult)) {
				switch (IGNORE_SPACES) {
					case ALL_SPACES:
						String trimmedExpected = ModelTestsUtil.removeWhiteSpace(result);
						String trimmedActual= ModelTestsUtil.removeWhiteSpace(newResult);
						if (trimmedExpected.equals(trimmedActual)) {
							this.whitespacesFailures.add(this.path);
							this.hasSpaceFailure = true;
							return result;
						}
						break;
					case LINES_LEADING_SPACES:
						trimmedExpected = ModelTestsUtil.trimLinesLeadingWhitespaces(result);
						trimmedActual= ModelTestsUtil.trimLinesLeadingWhitespaces(newResult);
						if (trimmedExpected.equals(trimmedActual)) {
							this.leadingWhitespacesFailures.add(this.path);
							this.hasSpaceFailure = true;
							return result;
						}
						if (ModelTestsUtil.removeWhiteSpace(result).equals(ModelTestsUtil.removeWhiteSpace(newResult))) {
							this.whitespacesFailures.add(this.path);
							this.hasSpaceFailure = true;
							return result;
						}
						break;
					default:
						trimmedExpected = filterFormattingInComments(result);
						trimmedActual= filterFormattingInComments(newResult);
						if (trimmedExpected.equals(trimmedActual)) {
							this.whitespacesFailures.add(this.path);
							this.hasSpaceFailure = true;
							return result;
						}
						break;
				}
				if (!isExpectedFailure()) {
					assertSourceEquals("2nd formatting is different from first one!", Util.convertToIndependantLineDelimiter(result), Util.convertToIndependantLineDelimiter(newResult));
				}
			}
		}
	}
	return result;
}

public void testCompare() throws IOException, Exception {
	compareFormattedSource();
}
}
