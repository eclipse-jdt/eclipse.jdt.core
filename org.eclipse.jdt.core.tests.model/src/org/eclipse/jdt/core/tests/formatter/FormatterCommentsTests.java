/*******************************************************************************
 * Copyright (c) 2000, 2022 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Mateusz Matela <mateusz.matela@gmail.com> - [formatter] Formatter does not format Java code correctly, especially when max line width is set - https://bugs.eclipse.org/303519
 *******************************************************************************/
package org.eclipse.jdt.core.tests.formatter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.ComparisonFailure;
import junit.framework.Test;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
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

/**
 * Javadoc formatter test suite using the Eclipse default settings.
 * <p>
 * See also sub-classes which run the same tests bundle but with different
 * formatter options:
 * <ul>
 * 	<li>{@link FormatterCommentsClearBlankLinesTests}</li>
 * 	<li>{@link FormatterJavadocDontIndentTagsTests}</li>
 * 	<li>{@link FormatterJavadocDontIndentTagsDescriptionTests}</li>
 * </ul>
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class FormatterCommentsTests extends FormatterRegressionTests {

	private static final IPath OUTPUT_FOLDER = new Path("out").append("default");

	public static List ALL_TEST_SUITES = null;

public static Test suite() {
	return buildModelTestSuite(FormatterCommentsTests.class);
}

public FormatterCommentsTests(String name) {
	super(name);
}

/**
 * Create project and set the jar placeholder.
 */
@Override
public void setUpSuite() throws Exception {
	if (JAVA_PROJECT == null) {
		JAVA_PROJECT = setUpJavaProject("FormatterJavadoc", CompilerOptions.getFirstSupportedJavaVersion()); //$NON-NLS-1$
	}
	super.setUpSuite();
}

void compareFormattedSource(ICompilationUnit compilationUnit) throws JavaModelException {
	DefaultCodeFormatter codeFormatter = codeFormatter();
	String source = compilationUnit.getSource();
	String expectedResult = expectedFormattedSource(source);
	String actualResult = runFormatter(codeFormatter, source, CodeFormatter.K_COMPILATION_UNIT | CodeFormatter.F_INCLUDE_COMMENTS, 0, 0, source.length(), null, true);
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
	Scanner scanner = new Scanner(true, true, false/*nls*/, ClassFileConstants.JDK1_4/*sourceLevel*/, null/*taskTags*/, null/*taskPriorities*/, true/*taskCaseSensitive*/,
			codeFormatter.previewEnabled);
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
		formattedComments[i] = runFormatter(codeFormatter, source.substring(commentStart, commentEnd), CodeFormatter.K_JAVA_DOC, indentationLevel, 0, commentEnd - commentStart, Util.LINE_SEPARATOR, true);
	}
	SimpleDocument document = new SimpleDocument(source);
	for (int i=length-1; i>=0; i--) {
		int[] positions = commentsPositions[i];
		int commentStart = positions[0] > 0 ? positions [0] : -positions[0];
		int commentEnd = positions[1] > 0 ? positions [1] : -positions[1];
		document.replace(commentStart, commentEnd - commentStart, formattedComments[i]);
	}
	String newSource = document.get();
	String oldResult = runFormatter(codeFormatter, newSource, CodeFormatter.K_COMPILATION_UNIT, 0, 0, newSource.length(), null, true);
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
	optionsMap.put(CompilerOptions.OPTION_ReportAssertIdentifier, CompilerOptions.ERROR);
	optionsMap.put(CompilerOptions.OPTION_ReportEnumIdentifier, CompilerOptions.ERROR);
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
	optionsMap.put(CompilerOptions.OPTION_Compliance, CompilerOptions.getFirstSupportedJavaVersion());
	optionsMap.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.getFirstSupportedJavaVersion());
	optionsMap.put(CompilerOptions.OPTION_TaskTags, ""); //$NON-NLS-1$
	optionsMap.put(CompilerOptions.OPTION_TaskPriorities, ""); //$NON-NLS-1$
	optionsMap.put(CompilerOptions.OPTION_TaskCaseSensitive, CompilerOptions.DISABLED);
	optionsMap.put(CompilerOptions.OPTION_ReportUnusedParameterWhenImplementingAbstract, CompilerOptions.DISABLED);
	optionsMap.put(CompilerOptions.OPTION_ReportUnusedParameterWhenOverridingConcrete, CompilerOptions.DISABLED);
	optionsMap.put(CompilerOptions.OPTION_ReportSpecialParameterHidingField, CompilerOptions.DISABLED);
	optionsMap.put(CompilerOptions.OPTION_MaxProblemPerUnit, String.valueOf(100));
	optionsMap.put(CompilerOptions.OPTION_Source, CompilerOptions.getFirstSupportedJavaVersion());
	return optionsMap;
}

void formatUnit(String packageName, String unitName) throws JavaModelException{
	useOldCommentWidthCounting();
	formatUnit(packageName, unitName, CodeFormatter.K_COMPILATION_UNIT | CodeFormatter.F_INCLUDE_COMMENTS, 0, false, 0, -1, null);
}

void formatUnit(String packageName, String unitName, int kind, int indentationLevel, boolean checkNull, int offset, int length, String lineSeparator) throws JavaModelException{
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getCompilationUnit(JAVA_PROJECT.getElementName() , "", "test."+packageName, unitName); //$NON-NLS-1$ //$NON-NLS-2$
	File expectedFile = getExpectedOutput(this.workingCopies[0]);
	String expectedOutput;
	try {
		expectedOutput = expectedFile == null ? null : Files.readString(expectedFile.toPath());
	} catch (IOException e) {
		throw new RuntimeException(e);
	}
	try {
		formatSource(this.workingCopies[0].getSource(), expectedOutput, kind, indentationLevel, offset, length,
				lineSeparator, true);
	} catch (ComparisonFailure e) {
		e.addSuppressed(new RuntimeException("Happend when formating: \n\"" + this.workingCopies[0].getPath()
				+ "\"\nExpected output:\n\"" + expectedFile + "\""));
		throw e;
	}
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
			allUnits.addAll(Arrays.asList(units));
		}
	}
	return allUnits;
}

private File getExpectedOutput(ICompilationUnit unit) throws JavaModelException {
	IPath outputPath = JAVA_PROJECT.getProject().getLocation().removeLastSegments(1)
		.append(unit.getParent().getPath())
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
	return outputFile;
}

IPath getOutputFolder() {
	return OUTPUT_FOLDER;
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
	// Difference with old formatter:
	// 1) fixed tags issue with max length
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
public void testHtmlOthers08() throws JavaModelException {
	formatUnit("html.others", "X08.java");
}
public void testHtmlOthers09() throws JavaModelException {
	formatUnit("html.others", "X09.java");
}
public void testHtmlOthers10() throws JavaModelException {
	formatUnit("html.others", "X10.java");
}
public void testHtmlOthers11() throws JavaModelException {
	formatUnit("html.others", "X11.java");
}

/*
 * Test formatter tags <pre>
 */
public void testHtmlPre01() throws JavaModelException {
	formatUnit("html.pre", "X01.java");
}
public void testHtmlPre02() throws JavaModelException {
	// Difference with old formatter:
	// 1) Blank lines inside the <pre> tag are now preserved
	// 		see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=231845
	this.formatterPrefs.number_of_empty_lines_to_preserve = 4;
	formatUnit("html.pre", "X02.java");
}
public void testHtmlPre02b() throws JavaModelException {
	formatUnit("html.pre", "X02b.java");
}
public void testHtmlPre03() throws JavaModelException {
	formatUnit("html.pre", "X03.java");
}
public void testHtmlPre04() throws JavaModelException {
	// Difference with old formatter:
	// 1) Blank lines inside the <pre> tag are now preserved
	// 		see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=231845
	formatUnit("html.pre", "X04.java");
}
public void testHtmlPre05() throws JavaModelException {
	formatUnit("html.pre", "X05.java");
}
public void testHtmlPre06() throws JavaModelException {
	formatUnit("html.pre", "X06.java");
}
public void testHtmlPre07() throws JavaModelException {
	// Difference with old formatter:
	// 1) Blank lines inside the <pre> tag are now preserved
	// 		see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=231845
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
public void testHtmlPre13() throws JavaModelException {
	formatUnit("html.pre", "X13.java");
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
public void testHtmlUl12() throws JavaModelException {
	formatUnit("html.ul", "X12.java");
}

/**
 * test Test formatter one line comment
 */
public void testLines() throws JavaModelException {
	formatUnit("lines", "X01.java");
}
public void testLines02() throws JavaModelException {
	// Difference with old formatter:
	// 1) fixed max length issue when comment has only one line
	formatUnit("lines", "X02.java");
}
public void testLines03() throws JavaModelException {
	// Difference with old formatter:
	// 1) fixed max length issue when comment has only one line
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
	// Difference with old formatter:
	// 1) fixed max length issue when comment has only one line
	formatUnit("lines", "X09.java");
}

/*
 * Test formatter preferences example
 */
public void testPreferencesExample01() throws JavaModelException {
	useOldJavadocTagsFormatting();
	formatUnit("example", "X01.java");
}
public void testPreferencesExample02() throws JavaModelException {
	formatUnit("example", "X02.java");
}
public void testPreferencesExample03() throws JavaModelException {
	formatUnit("example", "X03.java");
}
public void testPreferencesExample04() throws JavaModelException {
	formatUnit("example", "X04.java");
}
public void testPreferencesExample05() throws JavaModelException {
	useOldJavadocTagsFormatting();
	formatUnit("example", "X05.java");
}
public void testPreferencesExample06() throws JavaModelException {
	useOldJavadocTagsFormatting();
	formatUnit("example", "X06.java");
}
// NOT_FIXED_YET: https://bugs.eclipse.org/bugs/show_bug.cgi?id=196124
public void _testPreferencesExample07() throws JavaModelException {
	formatUnit("example", "X07.java");
}
public void testPreferencesExample08() throws JavaModelException {
	useOldJavadocTagsFormatting();
	// Difference with old formatter:
	// 1) fixed wrong max length with immutable tags
	formatUnit("example", "X08.java");
}
public void testPreferencesExample09() throws JavaModelException {
	useOldJavadocTagsFormatting();
	formatUnit("example", "X09.java");
}
public void testPreferencesExample10() throws JavaModelException {
	formatUnit("example", "X10.java");
}
public void testPreferencesExample11() throws JavaModelException {
	formatUnit("example", "X11.java");
}
public void testPreferencesExample12() throws JavaModelException {
	// Difference with old formatter:
	// TODO Decide how split line when closing line is over the max length
	formatUnit("example", "X12.java");
}
public void testPreferencesExample13() throws JavaModelException {
	// Difference with old formatter:
	// 1) fixed wrong max length with immutable tags
	// 2) bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=231800
	formatUnit("example", "X13.java");
}
public void testPreferencesExample14() throws JavaModelException {
	useOldJavadocTagsFormatting();
	formatUnit("example", "X14.java");
}
public void testPreferencesExample14a() throws JavaModelException {
	useOldJavadocTagsFormatting();
	formatUnit("example", "X14a.java");
}
public void testPreferencesExample14b() throws JavaModelException {
	useOldJavadocTagsFormatting();
	formatUnit("example", "X14b.java");
}
public void testPreferencesExample14c() throws JavaModelException {
	useOldJavadocTagsFormatting();
	formatUnit("example", "X14c.java");
}
public void testPreferencesExample14d() throws JavaModelException {
	useOldJavadocTagsFormatting();
	formatUnit("example", "X14d.java");
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
public void testPreferencesExample16a() throws JavaModelException {
	useOldJavadocTagsFormatting();
	formatUnit("example", "X16a.java");
}
public void testPreferencesExample16b() throws JavaModelException {
	useOldJavadocTagsFormatting();
	// Difference with old formatter:
	// 1) fixed wrong max length with immutable tags
	formatUnit("example", "X16b.java");
}
public void testPreferencesExample17a() throws JavaModelException {
	formatUnit("example", "X17a.java");
}
public void testPreferencesExample17b() throws JavaModelException {
	useOldJavadocTagsFormatting();
	formatUnit("example", "X17b.java");
}
public void testPreferencesExample17c() throws JavaModelException {
	useOldJavadocTagsFormatting();
	formatUnit("example", "X17c.java");
}

/*
 * Test formatter immutable tags
 */
public void testTagImmutable01() throws JavaModelException {
	formatUnit("tags.immutable", "X01.java");
}
public void testTagImmutable02() throws JavaModelException {
	formatUnit("tags.immutable", "X02.java");
}
public void testTagImmutable03() throws JavaModelException {
	formatUnit("tags.immutable", "X03.java");
}

/*
 * Test formatter other tags
 */
public void testTagOthers01() throws JavaModelException {
	formatUnit("tags.others", "X01.java");
}
public void testTagOthers02() throws JavaModelException {
	useOldJavadocTagsFormatting();
	// Difference with old formatter:
	// 1) tag description is not indented when an empty line exists in the description
	formatUnit("tags.others", "X02.java");
}
public void testTagOthers03() throws JavaModelException {
	formatUnit("tags.others", "X03.java");
}

/*
 * Test formatter @param
 */
public void testTagParam01() throws JavaModelException {
	useOldJavadocTagsFormatting();
	formatUnit("tags.param", "X01.java");
}
public void testTagParam02() throws JavaModelException {
	useOldJavadocTagsFormatting();
	formatUnit("tags.param", "X02.java");
}
public void testTagParam03() throws JavaModelException {
	useOldJavadocTagsFormatting();
	// Difference with old formatter:
	// 1) tag description is not indented when an empty line exists in the description
	formatUnit("tags.param", "X03.java");
}
public void testTagParam04() throws JavaModelException {
	useOldJavadocTagsFormatting();
	formatUnit("tags.param", "X04.java");
}
public void testTagParam05() throws JavaModelException {
	useOldJavadocTagsFormatting();
	formatUnit("tags.param", "X05.java");
}
public void testTagParam06() throws JavaModelException {
	useOldJavadocTagsFormatting();
	formatUnit("tags.param", "X06.java");
}

/*
 * Test formatter see tags
 */
public void testTagSee01() throws JavaModelException {
	formatUnit("tags.see", "X01.java");
}
public void testTagSee02() throws JavaModelException {
	useOldJavadocTagsFormatting();
	formatUnit("tags.see", "X02.java");
}
public void testTagSee03() throws JavaModelException {
	useOldJavadocTagsFormatting();
	formatUnit("tags.see", "X03.java");
}
public void testTagSee04() throws JavaModelException {
	formatUnit("tags.see", "X04.java");
}
public void testTagSee05() throws JavaModelException {
	useOldJavadocTagsFormatting();
	formatUnit("tags.see", "X05.java");
}
public void testTagSee06() throws JavaModelException {
	useOldJavadocTagsFormatting();
	formatUnit("tags.see", "X06.java");
}

/*
 * Test formatter see tags
 */
public void testTagLink01() throws JavaModelException {
	formatUnit("tags.link", "X01.java");
}
public void testTagLink02() throws JavaModelException {
	// Difference with old formatter:
	// 1) fixed description in inline tag we should be formatted as text
	// 		see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=231297
	formatUnit("tags.link", "X02.java");
}
public void testTagLink02b() throws JavaModelException {
	// Difference with old formatter:
	// 1) fixed description in inline tag we should be formatted as text
	// 		see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=231297
	formatUnit("tags.link", "X02b.java");
}
public void testTagLink03a() throws JavaModelException {
	formatUnit("tags.link", "X03a.java");
}
public void testTagLink03b() throws JavaModelException {
	formatUnit("tags.link", "X03b.java");
}
public void testTagLink04() throws JavaModelException {
	useOldJavadocTagsFormatting();
	formatUnit("tags.link", "X04.java");
}

/*
 * Test formatter comment lines
 */
public void testLineComments01() throws JavaModelException {
	formatUnit("comments.line", "X01.java");
}
public void testLineComments02() throws JavaModelException {
	useOldCommentWidthCounting();
	String source =
		"public class X02 {\r\n" +
		"	int field; // This is a long comment that should be split in multiple line comments in case the line comment formatting is enabled\r\n" +
		"}\r\n";
	formatSource(source,
		"public class X02 {\n" +
		"	int field; // This is a long comment that should be split in multiple line\n" +
		"				// comments in case the line comment formatting is enabled\n" +
		"}\n",
		false /* do not repeat */
	);
}
public void testLineComments02b() throws JavaModelException {
	useOldCommentWidthCounting();
	String source =
		"public interface X02b {\r\n" +
		"\r\n" +
		"	int foo(); // This is a long comment that should be split in multiple line comments in case the line comment formatting is enabled\r\n" +
		"\r\n" +
		"	int bar();\r\n" +
		"}\r\n";
	formatSource(source,
		"public interface X02b {\n" +
		"\n" +
		"	int foo(); // This is a long comment that should be split in multiple line\n" +
		"				// comments in case the line comment formatting is enabled\n" +
		"\n" +
		"	int bar();\n" +
		"}\n",
		false /* do not repeat */
	);
}
public void testLineComments03() throws JavaModelException {
	formatUnit("comments.line", "X03.java");
}
public void testLineComments04() throws JavaModelException {
	formatUnit("comments.line", "X04.java");
}
public void testLineComments05() throws JavaModelException {
	formatUnit("comments.line", "X05.java");
}
public void testLineComments06() throws JavaModelException {
	setFormatLineCommentOnFirstColumn();
	formatUnit("comments.line", "X06.java");
}
public void testLineComments07() throws JavaModelException {
	useOldCommentWidthCounting();
	String source =
		"package test.comments.line;\r\n" +
		"\r\n" +
		"public class X07 {\r\n" +
		"\r\n" +
		"boolean inTitle;\r\n" +
		"boolean inMetaTag;\r\n" +
		"boolean inStyle;\r\n" +
		"boolean inImg;\r\n" +
		"\r\n" +
		"void foo(String tagName) {\r\n" +
		"    inTitle = tagName.equalsIgnoreCase(\"<title\"); // keep track if in <TITLE>\r\n" +
		"    inMetaTag = tagName.equalsIgnoreCase(\"<META\"); // keep track if in <META>\r\n" +
		"    inStyle = tagName.equalsIgnoreCase(\"<STYLE\"); // keep track if in <STYLE>\r\n" +
		"    inImg = tagName.equalsIgnoreCase(\"<img\");     // keep track if in <IMG>\r\n" +
		"}\r\n" +
		"}\r\n";
	formatSource(source,
		"package test.comments.line;\r\n" +
		"\r\n" +
		"public class X07 {\r\n" +
		"\r\n" +
		"	boolean inTitle;\r\n" +
		"	boolean inMetaTag;\r\n" +
		"	boolean inStyle;\r\n" +
		"	boolean inImg;\r\n" +
		"\r\n" +
		"	void foo(String tagName) {\r\n" +
		"		inTitle = tagName.equalsIgnoreCase(\"<title\"); // keep track if in\r\n" +
		"														// <TITLE>\r\n" +
		"		inMetaTag = tagName.equalsIgnoreCase(\"<META\"); // keep track if in\r\n" +
		"														// <META>\r\n" +
		"		inStyle = tagName.equalsIgnoreCase(\"<STYLE\"); // keep track if in\r\n" +
		"														// <STYLE>\r\n" +
		"		inImg = tagName.equalsIgnoreCase(\"<img\"); // keep track if in <IMG>\r\n" +
		"	}\r\n" +
		"}\r\n",
		false /* do not repeat */
	);
}
public void testLineComments08() throws JavaModelException {
	formatUnit("comments.line", "X08.java");
}
public void testLineComments09() throws JavaModelException {
	formatUnit("comments.line", "X09.java");
}
public void testLineComments10() throws JavaModelException {
	formatUnit("comments.line", "X10.java");
}
public void testLineComments11() throws JavaModelException {
	useOldCommentWidthCounting();
	this.formatterPrefs.comment_line_length = 40;
	String source =
		"package test.comments.line;\r\n" +
		"\r\n" +
		"public class X11 { // This comment will go____over the max line length\r\n" +
		"}\r\n";
	formatSource(source,
		"package test.comments.line;\r\n" +
		"\r\n" +
		"public class X11 { // This comment will\r\n" +
		"					// go____over the\r\n" +
		"					// max line length\r\n" +
		"}\r\n",
		false /* do not repeat */
	);
}

/*
 * Test formatter block lines
 */
//static { TESTS_PREFIX = "testBlockComments"; }
public void testBlockComments01() throws JavaModelException {
	formatUnit("comments.block", "X01.java");
}
public void testBlockComments02() throws JavaModelException {
	formatUnit("comments.block", "X02.java");
}
public void testBlockComments03() throws JavaModelException {
	formatUnit("comments.block", "X03.java");
}
public void testBlockComments03b() throws JavaModelException {
	formatUnit("comments.block", "X03b.java");
}
public void testBlockComments04() throws JavaModelException {
	formatUnit("comments.block", "X04.java");
}
public void testBlockComments05() throws JavaModelException {
	formatUnit("comments.block", "X05.java");
}
public void testBlockComments05b() throws JavaModelException {
	formatUnit("comments.block", "X05b.java");
}
public void testBlockComments05c() throws JavaModelException {
	formatUnit("comments.block", "X05c.java");
}
public void testBlockComments05d() throws JavaModelException {
	formatUnit("comments.block", "X05d.java");
}
public void testBlockComments05db() throws JavaModelException {
	formatUnit("comments.block", "X05db.java");
}
public void testBlockComments05dc() throws JavaModelException {
	formatUnit("comments.block", "X05dc.java");
}
public void testBlockComments05e() throws JavaModelException {
	formatUnit("comments.block", "X05e.java");
}
public void testBlockComments06() throws JavaModelException {
	formatUnit("comments.block", "X06.java");
}
public void testBlockComments07() throws JavaModelException {
	formatUnit("comments.block", "X07.java");
}
public void testBlockComments08() throws JavaModelException {
	formatUnit("comments.block", "X08.java");
}
public void testBlockComments09() throws JavaModelException {
	formatUnit("comments.block", "X09.java");
}
public void testBlockComments10() throws JavaModelException {
	formatUnit("comments.block", "X10.java");
}
public void testBlockComments11() throws JavaModelException {
	setPageWidth80();
	formatUnit("comments.block", "X11.java");
}
public void testBlockComments12() throws JavaModelException {
	formatUnit("comments.block", "X12.java");
}
public void testBlockComments13() throws JavaModelException {
	setPageWidth80();
	String source =
		"package test.comments.block;\r\n" +
		"\r\n" +
		"public class X13 {\r\n" +
		"\r\n" +
		"protected void handleWarningToken(String token, boolean isEnabling) {\r\n" +
		"	if (token.equals(\"pkgDefaultMethod___\") || token.equals(\"packageDefaultMethod___\")/*_backward_ _compatible_*/ ) {\r\n" +
		"	}\r\n" +
		"}\r\n" +
		"}\r\n";
	// Difference with old formatter:
	// 1) split comment block starts one tab before to avoid possible words over the max line length
	//		note that in this peculiar this was not necessary as even the first word is over the max line length!
	formatSource(source,
		"package test.comments.block;\n" +
		"\n" +
		"public class X13 {\n" +
		"\n" +
		"	protected void handleWarningToken(String token, boolean isEnabling) {\n" +
		"		if (token.equals(\"pkgDefaultMethod___\") || token.equals(\n" +
		"				\"packageDefaultMethod___\")/* _backward_ _compatible_ */ ) {\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBlockComments14() throws JavaModelException {
	setPageWidth80();
	formatUnit("comments.block", "X14.java");
}
public void testBlockComments15() throws JavaModelException {
	formatUnit("comments.block", "X15.java");
}
public void testBlockComments16() throws JavaModelException {
	formatUnit("comments.block", "X16.java");
}
public void testBlockComments17() throws JavaModelException {
	setPageWidth80();
	formatUnit("comments.block", "X17.java");
}
public void testBlockComments18() throws JavaModelException {
	formatUnit("comments.block", "X18.java");
}
public void testBlockComments19() throws JavaModelException {
	formatUnit("comments.block", "X19.java");
}

/*
 * Test formatter on example got from workspaces
 */
// Full source performances 3.0
public void testWkspEclipse01() throws JavaModelException {
	useOldJavadocTagsFormatting();
	// Difference with old formatter:
	// 1) tag description is not indented when an empty line exists in the description
	formatUnit("wksp.eclipse", "X01.java");
}
public void testWkspEclipse02() throws JavaModelException {
	useOldJavadocTagsFormatting();
	// Difference with old formatter:
	// 1) fixed wrong max length with immutable tags
	// 2) fixed space after open html tag
	formatUnit("wksp.eclipse", "X02.java");
}
public void testWkspEclipse02b() throws JavaModelException {
	useOldJavadocTagsFormatting();
	// Difference with old formatter:
	// 1) fixed wrong max length with immutable tags
	// 2) fixed extra space after open html tag
	formatUnit("wksp.eclipse", "X02b.java");
}
public void testWkspEclipse03() throws JavaModelException {
	formatUnit("wksp.eclipse", "X03.java");
}
// TODO (frederic) Pass this test
public void _testWkspEclipse04() throws JavaModelException {
	formatUnit("wksp.eclipse", "X04.java");
}
public void testWkspEclipse05() throws JavaModelException {
	useOldJavadocTagsFormatting();
	formatUnit("wksp.eclipse", "X05.java");
}
public void testWkspEclipse06() throws JavaModelException {
	useOldJavadocTagsFormatting();
	formatUnit("wksp.eclipse", "X06.java");
}
public void testWkspEclipse07() throws JavaModelException {
	formatUnit("wksp.eclipse", "X07.java");
}
public void testWkspEclipse08() throws JavaModelException {
	// Difference with old formatter:
	// 1) fixed wrong max length with immutable tags
	formatUnit("wksp.eclipse", "X08.java");
}
public void testWkspEclipse08b() throws JavaModelException {
	formatUnit("wksp.eclipse", "X08b.java");
}
public void testWkspEclipse08c() throws JavaModelException {
	formatUnit("wksp.eclipse", "X08c.java");
}
public void testWkspEclipse09() throws JavaModelException {
	// Difference with old formatter:
	// 1) fixed max length issue when comment has only one line
	formatUnit("wksp.eclipse", "X09.java");
}
public void testWkspEclipse10() throws JavaModelException {
	useOldJavadocTagsFormatting();
	formatUnit("wksp.eclipse", "X10.java");
}
public void testWkspEclipse11() throws JavaModelException {
	formatUnit("wksp.eclipse", "X11.java");
}
public void testWkspEclipse11b() throws JavaModelException {
	formatUnit("wksp.eclipse", "X11b.java");
}
public void testWkspEclipse11c() throws JavaModelException {
	formatUnit("wksp.eclipse", "X11c.java");
}
public void testWkspEclipse12() throws JavaModelException {
	// Difference with old formatter:
	// 1) fixed wrong max length with immutable tags
	formatUnit("wksp.eclipse", "X12.java");
}
public void testWkspEclipse12b() throws JavaModelException {
	// Difference with old formatter:
	// 1) fixed wrong max length with immutable tags
	formatUnit("wksp.eclipse", "X12b.java");
}
public void testWkspEclipse13() throws JavaModelException {
	useOldJavadocTagsFormatting();
	formatUnit("wksp.eclipse", "X13.java");
}
public void testWkspEclipse14() throws JavaModelException {
	useOldJavadocTagsFormatting();
	// Difference with old formatter:
	// 1) fixed description in inline tag we should be formatted as text
	// 		see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=231297
	formatUnit("wksp.eclipse", "X14.java");
}
public void testWkspEclipse15() throws JavaModelException {
	formatUnit("wksp.eclipse", "X15.java");
}
public void testWkspEclipse16() throws JavaModelException {
	// Difference with old formatter:
	// 1) Allow split between text tokens when max length is reached
	// TODO verify that this behavior is OK
	useOldJavadocTagsFormatting();
	formatUnit("wksp.eclipse", "X16.java");
}
public void testWkspEclipse17() throws JavaModelException {
	// Difference with old formatter:
	// 1) Consider that code is immutable => do not change the content of <pre> inside
	//		 see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=229580
	formatUnit("wksp.eclipse", "X17.java");
}
public void testWkspEclipse18() throws JavaModelException {
	useOldJavadocTagsFormatting();
	// Difference with old formatter:
	// 1) fixed description in inline tag we should be formatted as text
	// 		see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=231297
	// 2) fixed wrong max length with immutable tags
	// 3) bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=231800
	formatUnit("wksp.eclipse", "X18.java");
}
public void testWkspEclipse19() throws JavaModelException {
	useOldJavadocTagsFormatting();
	// Difference with old formatter:
	// 1) fixed extra space between link tag name and reference
	// 2) fixed description in inline tag we should be formatted as text
	// see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=231297
	formatUnit("wksp.eclipse", "X19.java");
}
public void testWkspEclipse20() throws JavaModelException {
	// Difference with old formatter:
	// 1) fixed wrong max length with immutable tags
	formatUnit("wksp.eclipse", "X20.java");
}
public void testWkspEclipse21() throws JavaModelException {
	useOldJavadocTagsFormatting();
	// Difference with old formatter:
	// 1) fixed wrong max length with immutable tags
	// 2) tag description is not indented when an empty line exists in the description
	// 3) split line on closing html tags when over the max length
	// TODO Verify that 3) is OK
	formatUnit("wksp.eclipse", "X21.java");
}
public void testWkspEclipse22() throws JavaModelException {
	// Difference with old formatter:
	// 1) fixed max length issue when comment has only one line
	// 2) bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=231800
	formatUnit("wksp.eclipse", "X22.java");
}
public void testWkspEclipse23() throws JavaModelException {
	useOldJavadocTagsFormatting();
	// Difference with old formatter:
	// 1) fixed wrong max length with immutable tags
	// 2) fixed issue with javadoc line start (' *' instead of expected  ' * ')
	// 3) fixed issue with </table> closing tag
	// 4) fixed extra space after open html tag
	formatUnit("wksp.eclipse", "X23.java");
}
public void testWkspEclipse24() throws JavaModelException {
	useOldJavadocTagsFormatting();
	// Difference with old formatter:
	// 1) fixed wrong max length with immutable tags
	// 2) bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=231800
	formatUnit("wksp.eclipse", "X24.java");
}
public void testWkspEclipse25() throws JavaModelException {
	// Difference with old formatter:
	// 1) fixed wrong max length with immutable tags
	formatUnit("wksp.eclipse", "X25.java");
}
public void testWkspEclipse26() throws JavaModelException {
	// Difference with old formatter:
	// 1) Consider that code is immutable => do not change the content of <pre> inside
	// 		see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=229580
	formatUnit("wksp.eclipse", "X26.java");
}
public void testWkspEclipse27() throws JavaModelException {
	useOldJavadocTagsFormatting();
	formatUnit("wksp.eclipse", "X27.java");
}
public void testWkspEclipse28() throws JavaModelException {
	useOldJavadocTagsFormatting();
	formatUnit("wksp.eclipse", "X28.java");
}
public void testWkspEclipse28b() throws JavaModelException {
	useOldJavadocTagsFormatting();
	formatUnit("wksp.eclipse", "X28b.java");
}
// NOT_FIXED_YET: https://bugs.eclipse.org/bugs/show_bug.cgi?id=248543
public void _testWkspEclipse28c() throws JavaModelException {
	formatUnit("wksp.eclipse", "X28c.java");
}
public void testWkspEclipse28d() throws JavaModelException {
	useOldJavadocTagsFormatting();
	// Difference with old formatter:
	// 1) Do not split line when it will start with '@'
	// 		see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=229683
	formatUnit("wksp.eclipse", "X28d.java");
}
public void testWkspEclipse29() throws JavaModelException {
	useOldJavadocTagsFormatting();
	formatUnit("wksp.eclipse", "X29.java");
}
public void testWkspEclipse30() throws JavaModelException {
	useOldJavadocTagsFormatting();
	formatUnit("wksp.eclipse", "X30.java");
}
public void testWkspEclipse31() throws JavaModelException {
	// Difference with old formatter:
	// 1) fixed wrong max length with inline tags
	formatUnit("wksp.eclipse", "X31.java");
}
public void testWkspEclipse32() throws JavaModelException {
	// Difference with old formatter:
	// 1) fixed wrong max length with immutable tags
	// 2) bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=231800
	formatUnit("wksp.eclipse", "X32.java");
}
public void testWkspEclipse33() throws JavaModelException {
	// Difference with old formatter:
	// 1) fixed wrong max length with '*'
	formatUnit("wksp.eclipse", "X33.java");
}
public void testWkspEclipse34() throws JavaModelException {
	// Difference with old formatter:
	// 1) fixed wrong max length with immutable tags
	formatUnit("wksp.eclipse", "X34.java");
}
// Ganymede
public void testWkspGanymede01() throws JavaModelException {
	formatUnit("wksp.ganymede", "X02.java");
}
public void testWkspGanymede02() throws JavaModelException {
	// Difference with old formatter:
	// 1) fixed extra space between link tag name and reference
	formatUnit("wksp.ganymede", "X02.java");
}
public void testWkspGanymede03() throws JavaModelException {
	useOldJavadocTagsFormatting();
	// Difference with old formatter:
	// 1) fixed wrong max length with immutable tags
	// 2) fixed description in inline tag we should be formatted as text
	// see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=231297
	// 3) fixed string split
	formatUnit("wksp.ganymede", "X03.java");
}
public void testWkspGanymede04() throws JavaModelException {
	useOldJavadocTagsFormatting();
	// Difference with old formatter:
	// 1) fixed wrong max length with immutable tags
	// 2) fixed string split
	formatUnit("wksp.ganymede", "X04.java");
}
// JUnit 3.8.2
public void testWkspJUnit01() throws JavaModelException {
	formatUnit("wksp.junit", "X01.java");
}
public void testSnippet01() {
	setComplianceLevel(CompilerOptions.VERSION_18);
	this.formatterPrefs.comment_line_length = 40;
	String source =
		"/**\n" +
		" * Code sample:" +
		" * {@snippet lang=java :\n" +
		" *   public static void main(String... args) {\n" +
		" *       for (var arg : args) {                 // @highlight  type=italic regex = \"\\barg\\b\"\n" +
		" *           if (!arg.isBlank()) {  System.out.println(arg);    }\n" +
		" *       }                                      // @end\n" +
		" *   }\n" +
		" *   } OK?\n" +
		" */" +
		"class Test{}";
	formatSource(source,
		"/**\n" +
		" * Code sample: *\n" +
		" * {@snippet lang = java :\n" +
		" * public static void main(String... args) {\n" +
		" * 	for (var arg : args) {                 // @highlight type = italic regex = \"\\barg\\b\"\n" +
		" * 		if (!arg.isBlank()) {\n" +
		" * 			System.out.println(arg);\n" +
		" * 		}\n" +
		" * 	}                                      // @end\n" +
		" * }\n" +
		" * }\n" +
		" * OK?\n" +
		" */\n" +
		"class Test {\n" +
		"}"
	);
}
public void testSnippet02() {
	setComplianceLevel(CompilerOptions.VERSION_18);
	this.formatterPrefs.insert_space_before_assignment_operator = false;
	String source =
		"/**\n" +
		" * Here are the configuration properties:\n" +
		" * {@snippet file=\"config.properties\" id=\"testtest\"  \n" +
		" * lang  = properties}\n" +
		" */\n" +
		"public class T {}";
	formatSource(source,
		"/**\n" +
		" * Here are the configuration properties:\n" +
		" * {@snippet file= \"config.properties\" id= \"testtest\" lang= properties}\n" +
		" */\n" +
		"public class T {\n" +
		"}"
	);
}
public void testSnippet03() {
	setComplianceLevel(CompilerOptions.VERSION_18);
	this.formatterPrefs.insert_space_after_assignment_operator = false;
	String source =
		"/**\n" +
		" * Here are the configuration properties:\n" +
		" * {@snippet id=\"test   test\"   lang  = properties:\n" +
		" *   config1=value1;\n" +
		" *   config2=value2;\n" +
		" * }\n" +
		" */\n" +
		"public class T {}";
	formatSource(source,
		"/**\n" +
		" * Here are the configuration properties:\n" +
		" * {@snippet id =\"test   test\" lang =properties:\n" +
		" *   config1=value1;\n" +
		" *   config2=value2;\n" +
		" * }\n" +
		" */\n" +
		"public class T {\n" +
		"}"
	);
}
public void testSnippet04() {
	setComplianceLevel(CompilerOptions.VERSION_18);
	String source =
		"/**\n" +
		" * Here are the configuration properties:\n" +
		" * {@snippet  id=\"test   test\"  \n" +
		" * :\n" +
		" *   config1=value1;\n" +
		" *   config2=value2;\n" +
		" * }\n" +
		" */\n" +
		"public class T {}";
	formatSource(source,
		"/**\n" +
		" * Here are the configuration properties:\n" +
		" * {@snippet id = \"test   test\" :\n" +
		" * config1 = value1;\n" +
		" * config2 = value2;\n" +
		" * }\n" +
		" */\n" +
		"public class T {\n" +
		"}"
	);
}
public void testSnippet05() {
	setComplianceLevel(CompilerOptions.VERSION_18);
	this.formatterPrefs.insert_space_before_assignment_operator = false;
	String source =
		"/**\n" +
		" * Here are the configuration properties:\n" +
		" * {@snippet\n" +
		" *  id=\"testtest\" \n" +
		" *  lang='java' :\n" +
		" *   // @replace region substring='value		'	replacement=\"value:	\" \n" +
		" *   config1=\"value		1\";\n" +
		" *   config2=\"value		2\";\n" +
		" *   // @end\n" +
		" * } */\n" +
		"public class T {}";
	formatSource(source,
		"/**\n" +
		" * Here are the configuration properties:\n" +
		" * {@snippet id= \"testtest\" lang= 'java' :\n" +
		" * // @replace region substring= 'value		' replacement= \"value:	\"\n" +
		" * config1= \"value		1\";\n" +
		" * config2= \"value		2\";\n" +
		" * // @end\n" +
		" * }\n" +
		" */\n" +
		"public class T {\n" +
		"}"
	);
}
public void testSnippet06() {
	setComplianceLevel(CompilerOptions.VERSION_18);
	this.formatterPrefs.insert_space_after_assignment_operator = false;
	String source =
		"/**\n" +
		" * {@snippet :\n" +
		" *   // @replace substring	=	'value		' replacement=\"value:	\" :\n" +
		" *   config1=\"value		1\";\n" +
		" *   config2=\"value		2\";\n" +
		" * }\n" +
		" */\n" +
		"public class T {}";
	formatSource(source,
		"/**\n" +
		" * {@snippet :\n" +
		" * // @replace substring ='value		' replacement =\"value:	\" :\n" +
		" * config1 =\"value		1\";\n" +
		" * config2 =\"value		2\";\n" +
		" * }\n" +
		" */\n" +
		"public class T {\n" +
		"}"
	);
}
public void testJoinLineComment01() {
	this.formatterPrefs.join_line_comments = true;
	String source =
		"""
		class A {
		int a = 5; // one  two
		            // three
		}
		""";
	formatSource(source,
		"""
		class A {
			int a = 5; // one two three
		}
		""");
}
public void testJoinLineComment02() {
	this.formatterPrefs.join_line_comments = true;
	String source =
		"""
		class A {
		int a = 5; // one  two
		// three
		}
		""";
	formatSource(source,
		"""
		class A {
			int a = 5; // one two
		// three
		}
		""");
}
public void testJoinLineComment03() {
	this.formatterPrefs.join_line_comments = true;
	String source =
		"""
		class A {
		 int a = 5; // one  two
		 // three
		}
		""";
	formatSource(source,
		"""
		class A {
			int a = 5; // one two
			// three
		}
		""");
}
public void testJoinLineComment04() {
	this.formatterPrefs.join_line_comments = true;
	String source =
		"""
		class A {
			int a = 5; // one  two three four five six seven eight nine ten eleven twelve thirteen fourteen fifteen sixteen seventeen eighteen nineteen
						// one  two three four five six seven
						// eight nine ten eleven twelve
						// thirteen fourteen fifteen sixteen
						// seventeen eighteen nineteen
						// one  two three four five six seven eight nine ten eleven twelve thirteen fourteen fifteen sixteen seventeen eighteen nineteen
			// one  two three four five six seven eight nine ten eleven twelve thirteen fourteen fifteen sixteen seventeen eighteen nineteen
		}
		""";
	formatSource(source,
		"""
		class A {
			int a = 5; // one two three four five six seven eight nine ten eleven twelve thirteen
						// fourteen fifteen sixteen seventeen eighteen nineteen one two three four five
						// six seven eight nine ten eleven twelve thirteen fourteen fifteen sixteen
						// seventeen eighteen nineteen one two three four five six seven eight nine ten
						// eleven twelve thirteen fourteen fifteen sixteen seventeen eighteen nineteen
			// one two three four five six seven eight nine ten eleven twelve thirteen
			// fourteen fifteen sixteen seventeen eighteen nineteen
		}
		""");
}
}
