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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.core.tests.model.ModelTestsUtil;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.jdt.internal.core.util.CodeSnippetParsingUtil;
import org.eclipse.jdt.internal.core.util.SimpleDocument;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatter;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatterOptions;

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
 * TODO Only the javadoc comments are formatted and compared. Currently,
 * only javadoc comments are formatted on the first pass. We obviously need to
 * implement this feature also for line and block comments before to be able to
 * enable the comparison on them.
 * TODO (eric) See how fix the remaining failing tests when comparing the
 * formatting of JUnit 3.8.2 files.
 * TODO (eric) Fix failures while running on workspaces without comparing:
 * <ul>
 * <li>0 error and 938 failures for 9950 tests on 3.0 performance workspace.</li>
 * <li>1 error and 5658 failures for 25819 tests on ganymede workspace</li>
 * </ul>
 */
public class FormatterCommentsMassiveTests extends FormatterRegressionTests {

	final File file;
	private final static String DIR = System.getProperty("dir"); //$NON-NLS-1$
	private final static boolean COMPARE = DefaultCodeFormatterConstants.TRUE.equals(System.getProperty("compare")); //$NON-NLS-1$
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
		ASSERT_EQUALS_STRINGS = length < 15000; 
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
	actual = org.eclipse.jdt.core.tests.util.Util.convertToIndependantLineDelimiter(actual);
	if (ASSERT_EQUALS_STRINGS) {
		assertEquals(message, expected, actual);
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
	String source = new String(Util.getFileCharContent(this.file, null));
	try {
		String actualResult = runFormatter(codeFormatter, source, CodeFormatter.K_COMPILATION_UNIT | CodeFormatter.F_INCLUDE_COMMENTS, 0, 0, source.length(), null);
		if (COMPARE) {
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
		if (commentKind == CodeFormatter.K_JAVA_DOC) { // Only process javadoc for now
			formattedComments[i] = runFormatter(codeFormatter, source.substring(commentStart, commentEnd), commentKind, indentationLevel, 0, commentEnd - commentStart, Util.LINE_SEPARATOR);
		}
	}
	SimpleDocument document = new SimpleDocument(source);
	for (int i=length-1; i>=0; i--) {
		if (formattedComments[i] != null) {
			int[] positions = commentsPositions[i];
			int commentStart = positions[0] > 0 ? positions [0] : -positions[0];
			int commentEnd = positions[1] > 0 ? positions [1] : -positions[1];
			document.replace(commentStart, commentEnd - commentStart, formattedComments[i]);
		}
	}
	String newSource = document.get();
	String oldResult = runFormatter(codeFormatter, newSource, CodeFormatter.K_COMPILATION_UNIT, 0, 0, newSource.length(), null);
	return oldResult;
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

public void testCompare() throws IOException, Exception {
	compareFormattedSource();
}
}
