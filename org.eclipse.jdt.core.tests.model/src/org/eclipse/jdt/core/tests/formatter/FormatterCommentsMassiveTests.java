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
import java.util.HashMap;
import java.util.Map;

import junit.framework.ComparisonFailure;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.core.tests.model.ModelTestsUtil;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
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
 * 	<li>4 failures:
 * 		<ol>
 * 			<li>TestCase.java:
 * 				incorrect line length in old formatter
 * 				incorrect indentation in tag param in old formatter
 * 			</li>
 * 			<li>TestCaseClassLoader.java:
 * 				incorrect line length in old formatter
 * 			</li>
 * 			<li>SimpleTest.java:
 * 				word break on punctuation
 * 			</li>
 * 			<li>AssertTest.java:
 * 				word break on ponctuation
 * 			</li>
 * 		</lo>
 * 	</li>
 * 	<li>8 lines leading spaces differences on 2nd formatting:
 * 		<ol>
 * 			<li>n°1: Assert.java</li>
 * 			<li>n°3: TestSuite.java</li>
 * 			<li>n°4: BaseTestRunner.java</li>
 * 			<li>n°5: AllTests.java</li>
 * 			<li>n°6: AllTests.java</li>
 * 			<li>n°7: AllTests.java</li>
 * 			<li>n°8: BaseTestRunnerTest.java</li>
 * 			<li>n°9: ResultPrinter.java</li>
 * 		</ol>
 * 	</li>
 * 	<li>1 lines leading spaces differences with old formatter:
 * 		<ol>
 * 			<li>n°2: ComparisonFailure.java</li>
 * 		</ol>
 * 	</li>
 *		</ul></li>
 * </p><p>
 * TODO Fix failures while running on workspaces without comparing.
 * <i>
 * <ul>
 * <li>0 error and 425 failures for 9950 tests on 3.0 performance workspace.</li>
 * <li>0 error and 4220 failures for 25819 tests on ganymede workspace</li>
 * </ul>
 * </i>
 * 
 * Numbers above where before the line comments formatting was activated.
 * It was not possible to continue to compare the entire files after 2 formatting
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
 * 		<li>104 failures</li>
 * 		<li>799 different lines leading spaces</li>
 *		</ul></li>
 *		<li>ganymede workspace (25819 units):<ul>
 * 		<li>0 error</li>
 * 		<li>202 failures</li>
 * 		<li>1410 different lines leading spaces</li>
 *		</ul></li>
 * </ul>
 * 
 * Here are the results when setting the system property to
 * <code>all</code> (e.g. ignore all white spaces):
 * <ul>
 * 	<li>3.0 performance workspace (9951 units):<ul>
 * 		<li>0 error</li>
 * 		<li>376 failures</li>
 * 		<li>732 different spaces</li>
 *		</ul></li>
 *		<li>ganymede workspace (25819 units):<ul>
 * 		<li>? error</li>
 * 		<li>? failures</li>
 * 		<li>? different spaces</li>
 *		</ul></li>
 * </ul>
 */
public class FormatterCommentsMassiveTests extends FormatterRegressionTests {

	private static final String LINE_SEPARATOR = org.eclipse.jdt.internal.compiler.util.Util.LINE_SEPARATOR;
	final File file;
	int failures = 0, spaceFailures= 0;
	boolean hasSpaceFailure;
	private final static String DIR = System.getProperty("dir"); //$NON-NLS-1$
	private final static boolean COMPARE = DefaultCodeFormatterConstants.TRUE.equals(System.getProperty("compare")); //$NON-NLS-1$
	private final static boolean IGNORE_WHITESPACES = "all".equals(System.getProperty("ignoreWhitespaces")); //$NON-NLS-1$
	private final static boolean IGNORE_LINES_LEADING_WHITESPACES = "linesLeading".equals(System.getProperty("ignoreWhitespaces")); //$NON-NLS-1$
	private final static int FORMAT_REPEAT  = Integer.parseInt(System.getProperty("repeat", "2")); //$NON-NLS-1$
	private static final int MAX_FAILURES = 100; // Max failures using string comparison
	private static boolean ASSERT_EQUALS_STRINGS = true;
	
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
}

/* (non-Javadoc)
 * @see junit.framework.TestCase#getName()
 */
public String getName() {
	return super.getName()+" - " + this.file.toString();
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
	if (ASSERT_EQUALS_STRINGS) {
		try {
			assertEquals(message, expected, actual);
		}
		catch (ComparisonFailure cf) {
			if (IGNORE_WHITESPACES) {
				String trimmedExpected = ModelTestsUtil.removeWhiteSpace(expected);
				String trimmedActual= ModelTestsUtil.removeWhiteSpace(actual);
				if (trimmedExpected.equals(trimmedActual)) {
					this.spaceFailures++;
					System.out.println("n°"+this.spaceFailures+": Different spaces than old formatter for "+this.file.getName());
					return;
				}
			} else if (IGNORE_LINES_LEADING_WHITESPACES) {
				String trimmedExpected = ModelTestsUtil.trimLinesLeadingWhitespaces(expected);
				String trimmedActual= ModelTestsUtil.trimLinesLeadingWhitespaces(actual);
				if (trimmedExpected.equals(trimmedActual)) {
					this.spaceFailures++;
					System.out.println("n°"+this.spaceFailures+": Different line leading spaces than old formatter for "+this.file.getName());
					return;
				}
			}
			this.failures++;
			ASSERT_EQUALS_STRINGS = this.failures < MAX_FAILURES;
			throw cf;
		}
	} else {
		assertTrue(message, actual.equals(expected));
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
	DefaultCodeFormatter codeFormatter = codeFormatter();
	Scanner scanner = new Scanner(true, true, false/*nls*/, ClassFileConstants.JDK1_4/*sourceLevel*/, null/*taskTags*/, null/*taskPriorities*/, true/*taskCaseSensitive*/);
	CodeSnippetParsingUtil codeSnippetParsingUtil = new CodeSnippetParsingUtil();
	CompilationUnitDeclaration compilationUnitDeclaration = codeSnippetParsingUtil.parseCompilationUnit(source.toCharArray(), getDefaultCompilerOptions(), true);
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
		int indentationLevel = getIndentationLevel(scanner, commentStart);
		formattedComments[i] = runFormatter(codeFormatter, source.substring(commentStart, commentEnd), commentKind, indentationLevel, 0, commentEnd - commentStart, LINE_SEPARATOR);
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
					String comment = formattedComments[i];
					if (comment.trim().length() > 2) { // non empty comment
						char ch = source.charAt(commentEnd);
						if (ch == '\r' || ch == '\n') {
							commentEnd++;
							ch = source.charAt(commentEnd);
							if (ch == '\r' || ch == '\n') {
								commentEnd++;
							}
						}
					}
				}
			}
			document.replace(commentStart, commentEnd - commentStart, formattedComments[i]);
		}
	}
	String newSource = document.get();
	String oldResult = runFormatter(codeFormatter, newSource, CodeFormatter.K_COMPILATION_UNIT, 0, 0, newSource.length(), null);
	return oldResult == null ? newSource : oldResult;
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

String runFormatter(CodeFormatter codeFormatter, String source, int kind, int indentationLevel, int offset, int length, String lineSeparator) {
	TextEdit edit = codeFormatter.format(kind, source, offset, length, indentationLevel, lineSeparator);//$NON-NLS-1$
	if (edit == null) return null;
	String result = org.eclipse.jdt.internal.core.util.Util.editedString(source, edit);

	int count = 1;
	if (length == source.length()) {
		while (count++ < FORMAT_REPEAT) {
			edit = codeFormatter.format(kind, result, 0, result.length(), indentationLevel, lineSeparator);//$NON-NLS-1$
			if (edit == null) return null;
			String newResult = org.eclipse.jdt.internal.core.util.Util.editedString(result, edit);
			if (!result.equals(newResult)) {
				String counterString = counterString(count);
				if (IGNORE_WHITESPACES) {
					String trimmedResult = ModelTestsUtil.removeWhiteSpace(result);
					String trimmedNewResult = ModelTestsUtil.removeWhiteSpace(newResult);
					if (trimmedResult.equals(trimmedNewResult)) {
						this.spaceFailures++;
						System.out.println("n°"+this.spaceFailures+": "+counterString+" formatting has different spaces than first one for "+this.file.getName());
						this.hasSpaceFailure = true;
						return result;
					}
				} else if (IGNORE_LINES_LEADING_WHITESPACES) {
					String trimmedResult = ModelTestsUtil.trimLinesLeadingWhitespaces(result);
					String trimmedNewResult = ModelTestsUtil.trimLinesLeadingWhitespaces(newResult);
					if (trimmedResult.equals(trimmedNewResult)) {
						this.spaceFailures++;
						System.out.println("n°"+this.spaceFailures+": "+counterString+" formatting has different lines leading spaces than first one for "+this.file.getName());
						this.hasSpaceFailure = true;
						return result;
					}
				}
				assertSourceEquals(counterString+" formatting is different from first one!", Util.convertToIndependantLineDelimiter(result), Util.convertToIndependantLineDelimiter(newResult));
			}
		}
	}
	return result;
}

private String counterString(int count) {
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

public void testCompare() throws IOException, Exception {
	compareFormattedSource();
}
}
