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
 *                           Contribution for bug 403881
 */

package org.eclipse.jdt.core.tests.formatter;

import java.util.Map;
import junit.framework.Test;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.tests.model.AbstractJavaModelTests;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatter;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatterOptions;
import org.eclipse.text.edits.TextEdit;

@SuppressWarnings({"rawtypes", "unchecked"})
public class FormatterJSR308Tests extends AbstractJavaModelTests {

	protected static IJavaProject JAVA_PROJECT;

	public static final int UNKNOWN_KIND = 0;
	public static final String IN = "_in";
	public static final String OUT = "_out";
	public static final boolean DEBUG = false;
	static final String LINE_SEPARATOR = System.getProperty("line.separator");
	private static final String PROJECT_NAME = "FormatterJSR308";
	private long time;

	DefaultCodeFormatterOptions formatterPrefs;
	Map formatterOptions;

	static {
//		TESTS_NUMBERS = new int[] { 783 };
//		TESTS_RANGE = new int[] { 734, -1 };
//		TESTS_NAMES = new String [] { "test015" };
	}
	public static Test suite() {
		return buildModelTestSuite(FormatterJSR308Tests.class);
	}

	public FormatterJSR308Tests(String name) {
		super(name);
	}

	/*
	 * helper function for tests that are compatible with earlier page width
	 */
	private void setPageWidth80() {
		this.formatterPrefs.page_width = 80;
	}

	String runFormatter(CodeFormatter codeFormatter, String source, int kind, int indentationLevel, int offset, int length, String lineSeparator, boolean repeat) {
//		long time = System.currentTimeMillis();
		TextEdit edit = codeFormatter.format(kind, source, offset, length, indentationLevel, lineSeparator);//$NON-NLS-1$
//		System.out.println((System.currentTimeMillis() - time) + " ms");
		if (edit == null) return null;
//		System.out.println(edit.getChildrenSize() + " edits");
		String result = org.eclipse.jdt.internal.core.util.Util.editedString(source, edit);

		if (repeat && length == source.length()) {
//			time = System.currentTimeMillis();
			edit = codeFormatter.format(kind, result, 0, result.length(), indentationLevel, lineSeparator);//$NON-NLS-1$
//			System.out.println((System.currentTimeMillis() - time) + " ms");
			if (edit == null) return null;
//			assertEquals("Should not have edits", 0, edit.getChildren().length);
			final String result2 = org.eclipse.jdt.internal.core.util.Util.editedString(result, edit);
			if (!result.equals(result2)) {
				assertSourceEquals("Second formatting is different from first one!", Util.convertToIndependantLineDelimiter(result), Util.convertToIndependantLineDelimiter(result2));
			}
		}
		return result;
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

		if (DEBUG) {
			this.time = System.currentTimeMillis();
		}
	}

	/**
	 * Reset the jar placeholder and delete project.
	 */
	@Override
	public void tearDownSuite() throws Exception {
		deleteProject(JAVA_PROJECT); //$NON-NLS-1$
		JAVA_PROJECT = null;
		if (DEBUG) {
			System.out.println("Time spent = " + (System.currentTimeMillis() - this.time));//$NON-NLS-1$
		}
		super.tearDownSuite();
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

	private void runTest(String packageName, String compilationUnitName) {
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(this.formatterPrefs, this.formatterOptions);
		runTest(codeFormatter, packageName, compilationUnitName, CodeFormatter.K_COMPILATION_UNIT, 0);
	}

	private void runTest(CodeFormatter codeFormatter, String packageName, String compilationUnitName, int kind, int indentationLevel) {
		runTest(codeFormatter, packageName, compilationUnitName, kind, indentationLevel, false, 0, -1);
	}
	private void runTest(CodeFormatter codeFormatter, String packageName, String compilationUnitName, int kind, int indentationLevel, boolean checkNull, int offset, int length) {
		runTest(codeFormatter, packageName, compilationUnitName, kind, indentationLevel, checkNull, offset, length, null);
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

	public void testReferenceExpression() {
		setPageWidth80();
		runTest("testReferenceExpression", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test000() {
		setPageWidth80();
		runTest("test000", "I.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test001() {
		runTest("test001", "X.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test002() {
		runTest("test002", "X.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test003() {
		runTest("test003", "X.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test004() {
		runTest("test004", "X.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test005() {
		runTest("test005", "X.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test006() {
		runTest("test006", "X.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test007() {
		runTest("test007", "X.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test008() {
		setPageWidth80();
		runTest("test008", "X.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test009() {
		runTest("test009", "X.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test010() {
		runTest("test010", "X.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test011() {
		runTest("test011", "X.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test012() {
		runTest("test012", "X.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test013() {
		runTest("test013", "X.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test014() {
		runTest("test014", "X.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test015() {
		runTest("test015", "X.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test016() {
		runTest("test016", "X.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test017() {
		runTest("test017", "X.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test018() {
		runTest("test018", "X.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test019() {
		runTest("test019", "X.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test020() {
		runTest("test020", "X.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test021() {
		runTest("test021", "X.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test022() {
		runTest("test022", "X.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test023() {
		setPageWidth80();
		runTest("test023", "X.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test024() {
		runTest("test024", "X.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test025() {
		runTest("test025", "X.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test026() {
		runTest("test026", "X.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test027() {
		runTest("test027", "X.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test028() {
		runTest("test028", "X.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test029() {
		runTest("test029", "X.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test030() {
		runTest("test030", "X.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test031() {
		runTest("test031", "X.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test032() {
		runTest("test032", "X.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test033() {
		runTest("test033", "X.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test034() {
		runTest("test034", "X.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test035() {
		setPageWidth80();
		runTest("test035", "X.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test036() {
		runTest("test036", "X.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test037() {
		runTest("test037", "X.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test038() {
		runTest("test038", "X.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test039() {
		runTest("test039", "X.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void testBug403881() {
		runTest("testBugs", "Bug403881.java");//$NON-NLS-1$ //$NON-NLS-2$
	}
}
