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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.core.util.CodeSnippetParsingUtil;
import org.eclipse.jdt.internal.core.util.SimpleDocument;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatter;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatterOptions;

/**
 * Javadoc formatter test suite using the Eclipse default settings.
 * <p>
 * See also sub-classes which run the same tests bundle but with different
 * formatter options:
 * <ul>
 * 	<li>{@link FormatterJavadocClearBlankLinesTests}</li>
 * 	<li>{@link FormatterJavadocDontIndentTagsTests}</li>
 * 	<li>{@link FormatterJavadocDontIndentTagsDescriptionTests}</li>
 * </ul>
 */
public class FormatterJavadocTests extends FormatterRegressionTests {

	public static List ALL_TEST_SUITES = null;

public static Test suite() {
	return buildModelTestSuite(FormatterJavadocTests.class);
}

public FormatterJavadocTests(String name) {
	super(name);
}
protected void setUp() throws Exception {
    super.setUp();
}

/**
 * Create project and set the jar placeholder.
 */
public void setUpSuite() throws Exception {
	if (JAVA_PROJECT == null) {
		JAVA_PROJECT = setUpJavaProject("FormatterJavadoc"); //$NON-NLS-1$
	}
	super.setUpSuite();
}	

/**
 * Reset the jar placeholder and delete project.
 */
public void tearDownSuite() throws Exception {
	if (ALL_TEST_SUITES == null) {
		deleteProject(JAVA_PROJECT); //$NON-NLS-1$
	} else {
		ALL_TEST_SUITES.remove(getClass());
		if (ALL_TEST_SUITES.size() == 0) {
			deleteProject(JAVA_PROJECT); //$NON-NLS-1$
		}
	}
}

DefaultCodeFormatter codeFormatter() {
	DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getEclipseDefaultSettings();
	DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
	return codeFormatter;
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.core.tests.formatter.FormatterRegressionTests#assertLineEquals(java.lang.String, java.lang.String, java.lang.String, boolean)
 */
void assertLineEquals(String actualContents, String originalSource, String expectedContents, boolean checkNull) {
	String outputSource = expectedContents == null ? originalSource : expectedContents;
	super.assertLineEquals(actualContents, originalSource, outputSource, checkNull);
}

void formatSource(String source, String formattedOutput) {
	formatSource(source, formattedOutput, CodeFormatter.K_COMPILATION_UNIT | CodeFormatter.F_INCLUDE_COMMENTS, 0, false, 0, -1, null);
}

void formatSource(String source, String formattedOutput, int kind, int indentationLevel, boolean checkNull, int offset, int length, String lineSeparator) {
	DefaultCodeFormatter codeFormatter = codeFormatter();
	String result;
	if (length == -1) {
		result = runFormatter(codeFormatter, source, kind, indentationLevel, offset, source.length(), lineSeparator);
	} else {
		result = runFormatter(codeFormatter, source, kind, indentationLevel, offset, length, lineSeparator);
	}
	assertLineEquals(result, source, formattedOutput, checkNull);
}

void compareFormattedSource(ICompilationUnit compilationUnit) throws JavaModelException {
	DefaultCodeFormatter codeFormatter = codeFormatter();
	String source = compilationUnit.getSource();
	String expectedResult = expectedFormattedSource(source);
	String actualResult = runFormatter(codeFormatter, source, CodeFormatter.K_COMPILATION_UNIT | CodeFormatter.F_INCLUDE_COMMENTS, 0, 0, source.length(), null);
	assumeSourceEquals(compilationUnit.getPath()+" is not formatted the same way than before!",
		org.eclipse.jdt.core.tests.util.Util.convertToIndependantLineDelimiter(expectedResult),
		org.eclipse.jdt.core.tests.util.Util.convertToIndependantLineDelimiter(actualResult));
}
/*
 * Asserts that the given actual source (usually coming from a file content) is equal to the expected one.
 * Note that 'expected' is assumed to have the '\n' line separator. 
 * The line separators in 'actual' are converted to '\n' before the comparison.
 */
protected void assumeSourceEquals(String message, String expected, String actual) {
	if (actual == null) {
		assertEquals(message, expected, null);
		return;
	}
	actual = org.eclipse.jdt.core.tests.util.Util.convertToIndependantLineDelimiter(actual);
	boolean invalid = !actual.equals(expected);
	if (invalid) {
		System.out.println("================================================================================");
	}
	assumeEquals(message, expected, actual);
	if (invalid) {
		System.out.print(org.eclipse.jdt.core.tests.util.Util.displayString(actual.toString(), 2));
		System.out.println(this.endChar);
		System.out.println("--------------------------------------------------------------------------------");
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
		int commentStart = positions[0] > 0 ? positions [0] : -positions[0];
		int commentEnd = positions[1] > 0 ? positions [1] : -positions[1];
		int indentationLevel = getIndentationLevel(scanner, commentStart);
		formattedComments[i] = runFormatter(codeFormatter, source.substring(commentStart, commentEnd), CodeFormatter.K_JAVA_DOC, indentationLevel, 0, commentEnd - commentStart, Util.LINE_SEPARATOR);
	}
	SimpleDocument document = new SimpleDocument(source);
	for (int i=length-1; i>=0; i--) {
		int[] positions = commentsPositions[i];
		int commentStart = positions[0] > 0 ? positions [0] : -positions[0];
		int commentEnd = positions[1] > 0 ? positions [1] : -positions[1];
		document.replace(commentStart, commentEnd - commentStart, formattedComments[i]);
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

void formatUnit(String packageName, String unitName) throws JavaModelException{
	formatUnit(packageName, unitName, CodeFormatter.K_COMPILATION_UNIT | CodeFormatter.F_INCLUDE_COMMENTS, 0, false, 0, -1, null);
}

void formatUnit(String packageName, String unitName, int kind, int indentationLevel, boolean checkNull, int offset, int length, String lineSeparator) throws JavaModelException{
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getCompilationUnit(JAVA_PROJECT.getElementName() , "", "test."+packageName, unitName); //$NON-NLS-1$ //$NON-NLS-2$
	String outputSource = getOutputSource(this.workingCopies[0]);
	formatSource(this.workingCopies[0].getSource(), outputSource, kind, indentationLevel, checkNull, offset, length, lineSeparator);
}

/**
 * Returns all compilation units of a given project.
 * @param javaProject Project to collect units
 * @return List of org.eclipse.jdt.core.ICompilationUnit
 */
protected List getProjectCompilationUnits(IJavaProject javaProject) throws JavaModelException {
	IPackageFragmentRoot[] fragmentRoots = javaProject.getPackageFragmentRoots();
	int length = fragmentRoots.length;
	List allUnits = new ArrayList();
	for (int i=0; i<length; i++) {
		if (fragmentRoots[i] instanceof JarPackageFragmentRoot) continue;
		IJavaElement[] packages= fragmentRoots[i].getChildren();
		for (int k= 0; k < packages.length; k++) {
			IPackageFragment pack = (IPackageFragment) packages[k];
			ICompilationUnit[] units = pack.getCompilationUnits();
			for (int u=0; u<units.length; u++) {
				allUnits.add(units[u]);
			}
		}
	}
	return allUnits;
}

private String getOutputSource(ICompilationUnit unit) throws JavaModelException {
	IPath outputPath = JAVA_PROJECT.getProject().getLocation().removeLastSegments(1)
		.append(unit.getParent().getPath())
		.append("out")
		.append(getOutputFolder())
		.append(unit.getElementName());
	File outputFile = outputPath.toFile();
	if (!outputFile.exists()) {
		outputFile = JAVA_PROJECT.getProject().getLocation().removeLastSegments(1)
			.append(unit.getParent().getPath())
			.append("out")
			.append("default")
			.append(unit.getElementName())
			.toFile();
		if (!outputFile.exists()) {
			// will use the unit source in this case
			return null;
		}
	}
	try {
		return new String(org.eclipse.jdt.internal.compiler.util.Util.getFileCharContent(outputFile, null));
	}
	catch (IOException e) {
		// should never happen
		throw new RuntimeException(e);
	}
	
}

String getOutputFolder() {
	return "default";
}

/*
 * Test formatter on copyright comment.
 */
public void testCopyright_DEF() throws JavaModelException {
	formatUnit("copyright", "X_DEF.java");
}
public void testCopyright_CBL() throws JavaModelException {
	formatUnit("copyright", "X_CBL.java");
}
public void testCopyright01() throws JavaModelException {
	formatUnit("copyright", "X1.java");
}
public void testCopyright02() throws JavaModelException {
	formatUnit("copyright", "X2.java");
}
public void testCopyright03() throws JavaModelException {
	formatUnit("copyright", "X3.java");
}
public void testCopyright04() throws JavaModelException {
	formatUnit("copyright", "X4.java");
}
public void testCopyright05() throws JavaModelException {
	formatUnit("copyright", "X5.java");
}
public void testCopyright06() throws JavaModelException {
	formatUnit("copyright", "X6.java");
}

/*
 * Test other formatting tags (<li>, <br>)
 */
public void testHtmlOthers01() throws JavaModelException {
	formatUnit("html.others", "X01.java");
}
public void testHtmlOthers01b() throws JavaModelException {
	formatUnit("html.others", "X01b.java");
}
public void testHtmlOthers01c() throws JavaModelException {
	formatUnit("html.others", "X01c.java");
}
public void testHtmlOthers01d() throws JavaModelException {
	formatUnit("html.others", "X01d.java");
}
public void testHtmlOthers01e() throws JavaModelException {
	formatUnit("html.others", "X01e.java");
}
public void testHtmlOthers02() throws JavaModelException {
	formatUnit("html.others", "X02.java");
}
public void testHtmlOthers02b() throws JavaModelException {
	formatUnit("html.others", "X02b.java");
}
public void testHtmlOthers02c() throws JavaModelException {
	formatUnit("html.others", "X02c.java");
}
public void testHtmlOthers02d() throws JavaModelException {
	formatUnit("html.others", "X02d.java");
}
public void testHtmlOthers02e() throws JavaModelException {
	formatUnit("html.others", "X02e.java");
}
public void testHtmlOthers03() throws JavaModelException {
	formatUnit("html.others", "X03.java");
}
public void testHtmlOthers03b() throws JavaModelException {
	formatUnit("html.others", "X03b.java");
}
public void testHtmlOthers04() throws JavaModelException {
	formatUnit("html.others", "X04.java");
}
public void testHtmlOthers05() throws JavaModelException {
	formatUnit("html.others", "X05.java");
}
public void testHtmlOthers06() throws JavaModelException {
	formatUnit("html.others", "X06.java");
}
public void testHtmlOthers07() throws JavaModelException {
	formatUnit("html.others", "X07.java");
}
/*
 * TODO (eric) Fix following issue
 */
public void _testHtmlOthers08() throws JavaModelException {
	formatUnit("html.others", "X08.java");
}

/*
 * Test formatter tags <pre>
 */
public void testHtmlPre() throws JavaModelException {
	formatUnit("html.pre", "X.java");
}
/* 
 * Blank lines inside the <pre> tag are preserved although there were removed
 * in JDT/Text formatter.
 * 
 * TODO (eric) open a bug for this a retrieve an existing one
 */
public void testHtmlPre02() throws JavaModelException {
	formatUnit("html.pre", "X02.java");
}
public void testHtmlPre03() throws JavaModelException {
	formatUnit("html.pre", "X03.java");
}
/* 
 * Blank lines inside the <pre> tag are preserved although there were removed
 * in JDT/Text formatter.
 * 
 * TODO (eric) open a bug for this a retrieve an existing one
 */
public void testHtmlPre04() throws JavaModelException {
	formatUnit("html.pre", "X04.java");
}
public void testHtmlPre05() throws JavaModelException {
	formatUnit("html.pre", "X05.java");
}
public void testHtmlPre06() throws JavaModelException {
	formatUnit("html.pre", "X06.java");
}
public void testHtmlPre07() throws JavaModelException {
	formatUnit("html.pre", "X07.java");
}
public void testHtmlPre08() throws JavaModelException {
	formatUnit("html.pre", "X08.java");
}
public void testHtmlPre09() throws JavaModelException {
	formatUnit("html.pre", "X09.java");
}
public void testHtmlPre10() throws JavaModelException {
	formatUnit("html.pre", "X10.java");
}
public void testHtmlPre11() throws JavaModelException {
	formatUnit("html.pre", "X11.java");
}
public void testHtmlPre12() throws JavaModelException {
	formatUnit("html.pre", "X12.java");
}

/*
 * Test formatter tags <ul>
 */
public void testHtmlUl01() throws JavaModelException {
	formatUnit("html.ul", "X01.java");
}
public void testHtmlUl02() throws JavaModelException {
	formatUnit("html.ul", "X02.java");
}
public void testHtmlUl03() throws JavaModelException {
	formatUnit("html.ul", "X03.java");
}
public void testHtmlUl04() throws JavaModelException {
	formatUnit("html.ul", "X04.java");
}
public void testHtmlUl05() throws JavaModelException {
	formatUnit("html.ul", "X05.java");
}
public void testHtmlUl06() throws JavaModelException {
	formatUnit("html.ul", "X06.java");
}
public void testHtmlUl07() throws JavaModelException {
	formatUnit("html.ul", "X07.java");
}
public void testHtmlUl08() throws JavaModelException {
	formatUnit("html.ul", "X08.java");
}
public void testHtmlUl09() throws JavaModelException {
	formatUnit("html.ul", "X09.java");
}
public void testHtmlUl10() throws JavaModelException {
	formatUnit("html.ul", "X10.java");
}
public void testHtmlUl11() throws JavaModelException {
	formatUnit("html.ul", "X11.java");
}

/**
 * @test Test formatter one line comment
 */
public void testLines() throws JavaModelException {
	formatUnit("lines", "X.java");
}
public void testLines02() throws JavaModelException {
	formatUnit("lines", "X02.java");
}
public void testLines03() throws JavaModelException {
	formatUnit("lines", "X03.java");
}
public void testLines04() throws JavaModelException {
	formatUnit("lines", "X04.java");
}
public void testLines05() throws JavaModelException {
	formatUnit("lines", "X05.java");
}
public void testLines06() throws JavaModelException {
	formatUnit("lines", "X06.java");
}
public void testLines07() throws JavaModelException {
	formatUnit("lines", "X07.java");
}
public void testLines08() throws JavaModelException {
	formatUnit("lines", "X08.java");
}
public void testLines09() throws JavaModelException {
	formatUnit("lines", "X09.java");
}

/*
 * Test formatter preferences example
 */
public void testPreferencesExample() throws JavaModelException {
	formatUnit("example", "X01.java");
}
public void testPreferencesExample02() throws JavaModelException {
	formatUnit("example", "X02.java");
}
public void _testPreferencesExample03() throws JavaModelException {
	formatUnit("example", "X03.java");
}
public void _testPreferencesExample04() throws JavaModelException {
	formatUnit("example", "X04.java");
}
public void testPreferencesExample05() throws JavaModelException {
	formatUnit("example", "X05.java");
}
public void testPreferencesExample06() throws JavaModelException {
	formatUnit("example", "X06.java");
}
/*
 * TODO (eric) Fix the 4 following failing tests
 */
public void _testPreferencesExample07() throws JavaModelException {
	formatUnit("example", "X07.java");
}
public void _testPreferencesExample08() throws JavaModelException {
	formatUnit("example", "X08.java");
}
public void _testPreferencesExample09() throws JavaModelException {
	formatUnit("example", "X09.java");
}
public void _testPreferencesExample10() throws JavaModelException {
	formatUnit("example", "X10.java");
}
public void testPreferencesExample11() throws JavaModelException {
	formatUnit("example", "X11.java");
}
/*
 * TODO (eric) Fix the 2 following failing tests
 */
public void _testPreferencesExample12() throws JavaModelException {
	formatUnit("example", "X12.java");
}
public void _testPreferencesExample13() throws JavaModelException {
	formatUnit("example", "X13.java");
}
public void testPreferencesExample14a() throws JavaModelException {
	formatUnit("example", "X14a.java");
}
public void testPreferencesExample14b() throws JavaModelException {
	formatUnit("example", "X14b.java");
}
public void testPreferencesExample14c() throws JavaModelException {
	formatUnit("example", "X14c.java");
}
public void testPreferencesExample15a() throws JavaModelException {
	formatUnit("example", "X15a.java");
}
public void testPreferencesExample15b() throws JavaModelException {
	formatUnit("example", "X15b.java");
}
public void testPreferencesExample15c() throws JavaModelException {
	formatUnit("example", "X15c.java");
}

/*
 * Test formatter immutable tags
 */
public void testTagImmutable01() throws JavaModelException {
	formatUnit("tags.immutable", "X01.java");
}

/*
 * Test formatter other tags
 */
public void testTagOthers01() throws JavaModelException {
	formatUnit("tags.others", "X01.java");
}
public void testTagOthers02() throws JavaModelException {
	formatUnit("tags.others", "X02.java");
}

/*
 * Test formatter @param
 */
public void testTagParam() throws JavaModelException {
	formatUnit("tags.param", "X.java");
}
public void testTagParam02() throws JavaModelException {
	formatUnit("tags.param", "X02.java");
}
public void testTagParam03() throws JavaModelException {
	formatUnit("tags.param", "X03.java");
}
public void testTagParam04() throws JavaModelException {
	formatUnit("tags.param", "X04.java");
}

/*
 * Test formatter see tags
 */
public void testTagSee01() throws JavaModelException {
	formatUnit("tags.see", "X01.java");
}
public void testTagSee02() throws JavaModelException {
	formatUnit("tags.see", "X02.java");
}
public void testTagSee03() throws JavaModelException {
	formatUnit("tags.see", "X03.java");
}

/*
 * Test formatter see tags
 */
public void testTagLink01() throws JavaModelException {
	formatUnit("tags.link", "X01.java");
}
/*
 * TODO (eric) Fix the 2 following failing tests
 */
public void _testTagLink02() throws JavaModelException {
	formatUnit("tags.link", "X02.java");
}
public void _testTagLink02b() throws JavaModelException {
	formatUnit("tags.link", "X02b.java");
}
public void testTagLink03a() throws JavaModelException {
	formatUnit("tags.link", "X03a.java");
}
public void testTagLink03b() throws JavaModelException {
	formatUnit("tags.link", "X03b.java");
}
}
