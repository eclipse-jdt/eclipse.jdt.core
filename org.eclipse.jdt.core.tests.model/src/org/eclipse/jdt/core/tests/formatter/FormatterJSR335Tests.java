/*******************************************************************************
 * Copyright (c) 2013, 2015 Jesper Steen Moller, IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jesper Steen Moller - initial API and implementation
 *                           Contribution for bug 402819
 *                           Contribution for bug 402818
 *******************************************************************************/
package org.eclipse.jdt.core.tests.formatter;

import java.util.Map;
import junit.framework.Test;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.core.tests.model.AbstractJavaModelTests;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatter;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatterOptions;
import org.eclipse.text.edits.TextEdit;

@SuppressWarnings({"rawtypes", "unchecked"})
public class FormatterJSR335Tests extends AbstractJavaModelTests {

	protected static IJavaProject JAVA_PROJECT;

	public static final int UNKNOWN_KIND = 0;
	public static final String IN = "_in";
	public static final String OUT = "_out";
	public static final boolean DEBUG = false;
	static final String LINE_SEPARATOR = System.getProperty("line.separator");
	private static final String PROJECT_NAME = "FormatterJSR335";

	DefaultCodeFormatterOptions formatterPrefs;
	Map formatterOptions;

	static {
//		TESTS_NUMBERS = new int[] { 783 };
//		TESTS_RANGE = new int[] { 734, -1 };
	}
	public static Test suite() {
		return buildModelTestSuite(FormatterJSR335Tests.class);
	}

	public FormatterJSR335Tests(String name) {
		super(name);
	}

	/**
	 * Init formatter preferences with Eclipse default settings.
	 */
	@Override
	protected void setUp() throws Exception {
	    super.setUp();
		this.formatterPrefs = DefaultCodeFormatterOptions.getEclipseDefaultSettings();
		if (JAVA_PROJECT != null) {
			this.formatterOptions = JAVA_PROJECT.getOptions(true);
		}
		this.formatterOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_8);
		this.formatterOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_8);
		this.formatterOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_8);
	}

	/**
	 * Create project and set the jar placeholder.
	 */
	@Override
	public void setUpSuite() throws Exception {
		// ensure autobuilding is turned off
		IWorkspaceDescription description = getWorkspace().getDescription();
		if (description.isAutoBuilding()) {
			description.setAutoBuilding(false);
			getWorkspace().setDescription(description);
		}

		if (JAVA_PROJECT == null) {
			JAVA_PROJECT = setUpJavaProject(PROJECT_NAME, "1.8"); //$NON-NLS-1$
		}
	}

	/**
	 * Reset the jar placeholder and delete project.
	 */
	@Override
	public void tearDownSuite() throws Exception {
		deleteProject(JAVA_PROJECT); //$NON-NLS-1$
		JAVA_PROJECT = null;
		super.tearDownSuite();
	}

	/*
	 * helper function for tests that are compatible with earlier page width
	 */
	private void setPageWidth80() {
		this.formatterPrefs.page_width = 80;
	}

	/*
	 * helper function for tests that are compatible with earlier page width
	 */
	private void setPageWidth80(DefaultCodeFormatterOptions preferences) {
		preferences.page_width = 80;
	}

	String runFormatter(CodeFormatter codeFormatter, String source, int kind, int indentationLevel, int offset, int length, String lineSeparator, boolean repeat) {
		TextEdit edit = codeFormatter.format(kind, source, offset, length, indentationLevel, lineSeparator);//$NON-NLS-1$
		if (edit == null) return null;
		String result = org.eclipse.jdt.internal.core.util.Util.editedString(source, edit);

		if (repeat && length == source.length()) {
			edit = codeFormatter.format(kind, result, 0, result.length(), indentationLevel, lineSeparator);//$NON-NLS-1$
			if (edit == null) return null;
			final String result2 = org.eclipse.jdt.internal.core.util.Util.editedString(result, edit);
			if (!result.equals(result2)) {
				assertSourceEquals("Second formatting is different from first one!", Util.convertToIndependantLineDelimiter(result), Util.convertToIndependantLineDelimiter(result2));
			}
		}
		return result;
	}

	private String getIn(String compilationUnitName) {
		assertNotNull(compilationUnitName);
		int dotIndex = compilationUnitName.indexOf('.');
		assertTrue(dotIndex != -1);
		return compilationUnitName.substring(0, dotIndex) + IN + compilationUnitName.substring(dotIndex);
	}

	private String getOut(String compilationUnitName) {
		assertNotNull(compilationUnitName);
		int dotIndex = compilationUnitName.indexOf('.');
		assertTrue(dotIndex != -1);
		return compilationUnitName.substring(0, dotIndex) + OUT + compilationUnitName.substring(dotIndex);
	}

	void assertLineEquals(String actualContents, String originalSource, String expectedContents, boolean checkNull) {
		if (actualContents == null) {
			assertTrue("actualContents is null", checkNull);
			assertEquals(expectedContents, originalSource);
			return;
		}
		assertSourceEquals("Different number of length", Util.convertToIndependantLineDelimiter(expectedContents), actualContents);
	}
	private void runTest(String packageName, String compilationUnitName, DefaultCodeFormatterOptions codeFormatterOptions) {
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(codeFormatterOptions, this.formatterOptions);
		runTest(codeFormatter, packageName, compilationUnitName, CodeFormatter.K_COMPILATION_UNIT, 0);
	}

	private void runTest(String packageName, String compilationUnitName) {
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(this.formatterPrefs, this.formatterOptions);
		runTest(codeFormatter, packageName, compilationUnitName, CodeFormatter.K_COMPILATION_UNIT, 0);
	}
	private void runTest(CodeFormatter codeFormatter, String packageName, String compilationUnitName, int kind, int indentationLevel) {
		runTest(codeFormatter, packageName, compilationUnitName, kind, indentationLevel, false, 0, -1, null);
	}
	private void runTest(CodeFormatter codeFormatter, String packageName, String compilationUnitName, int kind, int indentationLevel, boolean checkNull, int offset, int length, String lineSeparator) {
		try {
			ICompilationUnit sourceUnit = getCompilationUnit(PROJECT_NAME , "", packageName, getIn(compilationUnitName)); //$NON-NLS-1$ //$NON-NLS-2$
			String source = sourceUnit.getSource();
			assertNotNull(source);
			ICompilationUnit outputUnit = getCompilationUnit(PROJECT_NAME , "", packageName, getOut(compilationUnitName)); //$NON-NLS-1$ //$NON-NLS-2$
			assertNotNull(outputUnit);
			String result;
			if (length == -1) {
				result = runFormatter(codeFormatter, source, kind, indentationLevel, offset, source.length(), lineSeparator, true);
			} else {
				result = runFormatter(codeFormatter, source, kind, indentationLevel, offset, length, lineSeparator, true);
			}
			assertLineEquals(result, source, outputUnit.getSource(), checkNull);
		} catch (JavaModelException e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void testLambda() {
		setPageWidth80();
		runTest("testLambda", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testLambdaOptions() {
		DefaultCodeFormatterOptions options = DefaultCodeFormatterOptions.getEclipseDefaultSettings();
		options.brace_position_for_lambda_body = DefaultCodeFormatterConstants.NEXT_LINE;
		options.insert_space_after_lambda_arrow = false;
		options.insert_space_before_lambda_arrow = false;
		setPageWidth80(options);
		runTest("testLambdaOptions", "A.java", options);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testMethodReference() {
		runTest("testMethodReference", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testBug402819() {
		runTest("testBugs", "Bug402819.java");//$NON-NLS-1$ //$NON-NLS-2$
	}
	public void testBug402818() {
		runTest("testBugs", "Bug402818.java");//$NON-NLS-1$ //$NON-NLS-2$
	}
}
