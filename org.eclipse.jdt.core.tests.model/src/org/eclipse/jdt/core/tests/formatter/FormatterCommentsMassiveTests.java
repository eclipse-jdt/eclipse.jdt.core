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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import junit.framework.AssertionFailedError;
import junit.framework.ComparisonFailure;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.core.tests.model.ModelTestsUtil;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
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
 * 	<li>Eclipse 3.0 performance workspace (9951 units):<ul>
 * 		<li>0 error</li>
 * 		<li>0 failures</li>
 * 		<li>8 failures due to old formatter</li>
 * 		<li>723 files have different lines leading spaces</li>
 * 		<li>9 files have different spaces</li>
 *		</ul></li>
 * 	<li>Eclipse 3.4 workspace (16592 units):<ul>
 * 		<li>0 error</li>
 * 		<li>11 failures</li>
 * 		<li>17 failures due to old formatter</li>
 * 		<li>1244 files have different lines leading spaces</li>
 * 		<li>11 files have different spaces</li>
 *		</ul></li>
 *		<li>ganymede M5 workspace (25819 units):<ul>
 * 		<li>0 error</li>
 * 		<li>12 failures due to different output while reformatting!</li>
 * 		<li>15 failures due to old formatter</li>
 * 		<li>1371 files have different line leading spaces when reformatting!</li>
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
	final IPath path;
	List failures = new ArrayList();
	List expectedFailures = new ArrayList();
	List leadingWhitespacesFailures = new ArrayList();
	List whitespacesFailures= new ArrayList();
	boolean hasSpaceFailure;
	private int changedHeaderFooter;
	private int changedPreTags;
	private int changedCodeTags;
	private final static boolean DEBUG_TESTS = "true".equals(System.getProperty("debugTests"));
	private final static String DIR = System.getProperty("dir"); //$NON-NLS-1$
	private final static String COMPARE = System.getProperty("compare"); //$NON-NLS-1$
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
	private final static IPath[] EXPECTED_FAILURES = DIR.indexOf("v34") < 0
		? new IPath[] {
			new Path("org/eclipse/jdt/internal/compiler/ast/QualifiedNameReference.java"),
			new Path("org/eclipse/jdt/internal/eval/CodeSnippetSingleNameReference.java"),
			new Path("org/eclipse/jdt/internal/core/DeltaProcessor.java"),
			new Path("org/eclipse/jdt/internal/core/JavaProject.java"),
			new Path("org/eclipse/jdt/internal/core/search/indexing/IndexManager.java"),
			new Path("org/eclipse/team/internal/ccvs/ui/AnnotateView.java"),
			new Path("org/eclipse/team/internal/ccvs/ui/HistoryView.java"),
			new Path("org/eclipse/team/internal/ccvs/ui/wizards/UpdateWizard.java"),
		}
		:	new IPath[] {
			new Path("org/eclipse/equinox/internal/p2/director/NewDependencyExpander.java"),
			new Path("org/eclipse/jdt/core/JavaCore.java"),
			new Path("org/eclipse/jdt/internal/codeassist/CompletionEngine.java"),
			new Path("org/eclipse/jdt/internal/codeassist/SelectionEngine.java"),
			new Path("org/eclipse/jdt/internal/compiler/ast/Expression.java"),
			new Path("org/eclipse/jdt/internal/compiler/ast/QualifiedNameReference.java"),
			new Path("org/eclipse/jdt/internal/compiler/ast/SingleNameReference.java"),
			new Path("org/eclipse/jdt/internal/eval/CodeSnippetSingleNameReference.java"),
			new Path("org/eclipse/jdt/internal/compiler/lookup/WildcardBinding.java"),
			new Path("org/eclipse/jdt/internal/compiler/batch/Main.java"),
			new Path("org/eclipse/jdt/internal/compiler/lookup/ParameterizedMethodBinding.java"),
			new Path("org/eclipse/jdt/internal/core/CompilationUnit.java"),
			new Path("org/eclipse/jdt/internal/core/ExternalJavaProject.java"),
			new Path("org/eclipse/jdt/internal/core/hierarchy/HierarchyResolver.java"),
			new Path("org/eclipse/jdt/internal/core/hierarchy/TypeHierarchy.java"),
			new Path("org/eclipse/jdt/internal/core/search/indexing/IndexAllProject.java"),
			new Path("org/eclipse/jdt/internal/core/search/JavaSearchScope.java"),
			new Path("org/eclipse/jdt/internal/eval/EvaluationContext.java"),
			new Path("org/eclipse/jdt/internal/ui/text/javadoc/JavadocContentAccess2.java"),
			new Path("org/eclipse/team/internal/ccvs/ui/mappings/WorkspaceSubscriberContext.java"),
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
		SimpleDateFormat format = new SimpleDateFormat();
		Date now = new Date(start);
		System.out.println("Date of test: "+format.format(now));
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
	this.path = new Path(file.getPath().substring(DIR.length()+1));
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
	String failuresType = COMPARE != null ? "than old formatter" : "when reformatting";
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
	if (this.changedHeaderFooter >0) {
		System.out.println(this.changedHeaderFooter+" differences in header/footer have been found");
	}
	if (this.changedPreTags >0) {
		System.out.println(this.changedPreTags+" differences in <pre> tags (blank lines) have been found");
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
		if ("true".equals(COMPARE)) {
			String trimmedExpected = expected;
			String trimmedActual = actual;
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

private String cleanAllKnownDifferences(String comment) {
	int kind = comment.charAt(1) == '/' ? 1 : comment.charAt(2) == '*' ? 3 : 2;
	String cleanedComment = comment;
	switch (kind) {
		case 1: // line comment
			cleanedComment = cleanBlankLinesAfterLineComment(comment);
			break;
		case 3: // javadoc comment
			cleanedComment = cleanHeaderAndFooter(comment);
			String newComment = cleanPreTags(cleanedComment);
			if (cleanedComment == newComment) {
				cleanedComment = cleanCodeTags(cleanedComment);
			} else {
				cleanedComment = newComment;
			}
			break;
	}
	return cleanedComment;
}
private String cleanHeaderAndFooter(String comment) {
	int start = 1; // skip starting '/'
	int length = comment.length();
	int end = length - 1; // skip ending '/'
	while (comment.charAt(start) == '*') {
		// remove all contiguous '*' in header
		start++;
	}
	while (comment.charAt(--end) == '*') {
		// remove all contiguous '*' in header
	}
	if (start > 3 || end < (length - 2)) {
		this.changedHeaderFooter++;
		return comment.substring(start, end);
	}
	return comment;
}

private String cleanBlankLinesAfterLineComment(String comment) {
	int length = comment.length();
	if (comment.charAt(length-1) == '\n') {
		length--;
		if (comment.charAt(length-1) == '\r') {
			length--;
		}
		return comment.substring(0, length);
	}
	return comment;
}

private String cleanCodeTags(String comment) {
	if (comment.indexOf("<code>") < 0) return comment;
	StringTokenizer tokenizer = new StringTokenizer(comment, "\r\n\f");
	StringBuffer buffer = new StringBuffer();
	while (tokenizer.hasMoreTokens()) {
		String line = tokenizer.nextToken();
		if (line.indexOf("<pre>") >= 0) {
			while (line.indexOf("</pre>") < 0) {
				line = tokenizer.nextToken();
			}
		} else {
			buffer.append(line);
			buffer.append("\n");
		}
	}
	this.changedCodeTags++;
	return buffer.toString();
}

private String cleanPreTags(String comment) {
	if (comment.indexOf("<pre>") < 0) return comment;
	StringTokenizer tokenizer = new StringTokenizer(comment, "\r\n\f");
	StringBuffer buffer = new StringBuffer();
	StringBuffer emptyLines = new StringBuffer();
	String previousLine = null;
	while (tokenizer.hasMoreTokens()) {
		String line = tokenizer.nextToken();
		if (line.trim() == "*") {
			if (previousLine == null || previousLine.indexOf("<pre>") < 0) {
				buffer.append(line);
				buffer.append("\n");
				continue;
			} else {
				emptyLines.append(line);
				emptyLines.append("\n");
			}
		} else if (line.indexOf("<code>") >= 0) {
			while (line.indexOf("</code>") < 0) {
				line = tokenizer.nextToken();
			}
		} else if (emptyLines.length() != 0) {
			if (line.indexOf("</pre>") < 0) {
				buffer.append(emptyLines);
				emptyLines.setLength(0);
			}
			buffer.append(line);
			buffer.append("\n");
		} else {
			buffer.append(line);
			buffer.append("\n");
		}
		previousLine = line;
	}
	this.changedPreTags++;
	return buffer.toString();
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
		if ("comments".equals(COMPARE)) {
			String[] oldFormattedComments = formattedComments(source, true);
			String[] newFormattedComments = formattedComments(source, false);
			int length = oldFormattedComments == null ? 0 : oldFormattedComments.length;
			this.abortOnFailure = false;
			assertEquals("Unexpected number of comments!", length, newFormattedComments == null ? 0 : newFormattedComments.length);
			for (int i=0; i<length; i++) {
				String oldComment = oldFormattedComments[i];
				String newComment = newFormattedComments[i];
				if (oldComment == null) {
					assertNull("Unexpected non-null new comment", newComment);
				} else {
					String expected = cleanAllKnownDifferences(oldComment);
					String actual = cleanAllKnownDifferences(newComment);
					if (!expected.equals(actual)) {
						String actualResult = runFormatter(codeFormatter, source, CodeFormatter.K_COMPILATION_UNIT | CodeFormatter.F_INCLUDE_COMMENTS, 0, 0, source.length(), null);
						String expectedResult = expectedFormattedSource(source);
						assertEquals("Unexpected difference with formatted comment "+(i+1), Util.convertToIndependantLineDelimiter(expectedResult), Util.convertToIndependantLineDelimiter(actualResult));
					}
				}
			}
		} else {
			String actualResult = runFormatter(codeFormatter, source, CodeFormatter.K_COMPILATION_UNIT | CodeFormatter.F_INCLUDE_COMMENTS, 0, 0, source.length(), null);
			if (!this.hasSpaceFailure && "true".equals(COMPARE)) {
				String expectedResult = expectedFormattedSource(source);
				assertLineEquals(actualResult, source, expectedResult, false);
			}
		}
	}
	catch (Exception e) {
		System.err.println(e.getMessage()+" occurred in "+getName());
		throw e;
	}
}

private String counterToString(int count) {
	int reminder = count%10;
	StringBuffer buffer = new StringBuffer();
	buffer.append(count);
	switch (reminder) {
		case 1:
			buffer.append("st");
			break;
		case 2:
			buffer.append("nd");
			break;
		case 3:
			buffer.append("rd");
			break;
		default:
			buffer.append("th");
			break;
	}
	return buffer.toString();
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
private String[] formattedComments(String source, boolean old) {
	boolean enableNewCommentFormatter = DefaultCodeFormatter.ENABLE_NEW_COMMENTS_FORMAT;
	try {
		DefaultCodeFormatter.ENABLE_NEW_COMMENTS_FORMAT = !old;
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
		return formattedComments;
	}
	finally {
		DefaultCodeFormatter.ENABLE_NEW_COMMENTS_FORMAT = enableNewCommentFormatter;
	}
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
		IPath expectedFailure= EXPECTED_FAILURES[i];
		if (this.path.matchingFirstSegments(expectedFailure) == expectedFailure.segmentCount()) {
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
	if (COMPARE == null && length == source.length()) {
		String previousResult = result;
		while (count++ < FORMAT_REPEAT) {
			edit = codeFormatter.format(kind, result, 0, result.length(), indentationLevel, lineSeparator);//$NON-NLS-1$
			if (edit == null) return null;
			previousResult = result;
			result = org.eclipse.jdt.internal.core.util.Util.editedString(result, edit);
		}
		if (!previousResult.equals(result)) {
			switch (IGNORE_SPACES) {
				case ALL_SPACES:
					String trimmedExpected = ModelTestsUtil.removeWhiteSpace(previousResult);
					String trimmedActual= ModelTestsUtil.removeWhiteSpace(result);
					if (trimmedExpected.equals(trimmedActual)) {
						this.whitespacesFailures.add(this.path);
						this.hasSpaceFailure = true;
						return previousResult;
					}
					break;
				case LINES_LEADING_SPACES:
					trimmedExpected = ModelTestsUtil.trimLinesLeadingWhitespaces(previousResult);
					trimmedActual= ModelTestsUtil.trimLinesLeadingWhitespaces(result);
					if (trimmedExpected.equals(trimmedActual)) {
						this.leadingWhitespacesFailures.add(this.path);
						this.hasSpaceFailure = true;
						return previousResult;
					}
					if (ModelTestsUtil.removeWhiteSpace(previousResult).equals(ModelTestsUtil.removeWhiteSpace(result))) {
						this.whitespacesFailures.add(this.path);
						this.hasSpaceFailure = true;
						return previousResult;
					}
					break;
			}
			if (!isExpectedFailure()) {
				String counterString = counterToString(count-1);
				assertSourceEquals(counterString+" formatting is different from first one!", Util.convertToIndependantLineDelimiter(previousResult), Util.convertToIndependantLineDelimiter(result));
			}
			result = previousResult;
		}
	}
	return result;
}

public void testCompare() throws IOException, Exception {
	compareFormattedSource();
}
}
