/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
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
 ********************************************************************************/
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
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatterOptions.Alignment;
import org.eclipse.text.edits.TextEdit;

public class FormatterTextBlockTests extends AbstractJavaModelTests {

	DefaultCodeFormatterOptions formatterPrefs;
	Map<String, String> formatterOptions;
	private long startNanos;

	protected static IJavaProject JAVA_PROJECT;
	public static final String IN = "_in";
	public static final String OUT = "_out";
	public static final boolean DEBUG = false;

	private static final String PROJECT_NAME = "FormatterTextBlock";


	public static Test suite() {
		return buildModelTestSuite(FormatterTextBlockTests.class);
	}

	public FormatterTextBlockTests(String name) {
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
			JAVA_PROJECT = setUpJavaProject(PROJECT_NAME, "15"); //$NON-NLS-1$
		}

		if (DEBUG) {
			this.startNanos = System.nanoTime();
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
			System.out.println("Time spent = " + (System.nanoTime() - this.startNanos) / 1_000_000L);//$NON-NLS-1$
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

	public void test01() {
		this.formatterPrefs.put_text_block_quotes_on_new_line = true;
		runTest("test01", "X.java");//$NON-NLS-1$ //$NON-NLS-2$
		this.formatterPrefs.text_block_indentation = Alignment.M_INDENT_DEFAULT;
		this.formatterPrefs.put_text_block_quotes_on_new_line = false;
	}

	public void test02() {
		this.formatterPrefs.put_text_block_quotes_on_new_line = true;
		this.formatterPrefs.text_block_indentation = Alignment.M_INDENT_BY_ONE;
		runTest("test02", "X.java");//$NON-NLS-1$ //$NON-NLS-2$
		this.formatterPrefs.text_block_indentation = Alignment.M_INDENT_DEFAULT;
		this.formatterPrefs.put_text_block_quotes_on_new_line = false;
	}

	public void test03() {
		this.formatterPrefs.put_text_block_quotes_on_new_line = true;
		this.formatterPrefs.text_block_indentation = Alignment.M_INDENT_PRESERVE;
		runTest("test03", "X.java");//$NON-NLS-1$ //$NON-NLS-2$
		this.formatterPrefs.text_block_indentation = Alignment.M_INDENT_DEFAULT;
		this.formatterPrefs.put_text_block_quotes_on_new_line = false;
	}

	public void test04() {
		this.formatterPrefs.put_text_block_quotes_on_new_line = true;
		this.formatterPrefs.text_block_indentation = Alignment.M_INDENT_ON_COLUMN;
		runTest("test04", "X.java");//$NON-NLS-1$ //$NON-NLS-2$
		this.formatterPrefs.text_block_indentation = Alignment.M_INDENT_DEFAULT;
		this.formatterPrefs.put_text_block_quotes_on_new_line = false;
	}

	public void test05() {
		this.formatterPrefs.put_text_block_quotes_on_new_line = true;
		runTest("test05", "X.java");//$NON-NLS-1$ //$NON-NLS-2$
		this.formatterPrefs.text_block_indentation = Alignment.M_INDENT_DEFAULT;
		this.formatterPrefs.put_text_block_quotes_on_new_line = false;
	}

	public void test06() {
		this.formatterPrefs.put_text_block_quotes_on_new_line = true;
		this.formatterPrefs.text_block_indentation = Alignment.M_INDENT_BY_ONE;
		runTest("test06", "X.java");//$NON-NLS-1$ //$NON-NLS-2$
		this.formatterPrefs.text_block_indentation = Alignment.M_INDENT_DEFAULT;
		this.formatterPrefs.put_text_block_quotes_on_new_line = false;
	}

	public void test07() {
		this.formatterPrefs.put_text_block_quotes_on_new_line = true;
		this.formatterPrefs.text_block_indentation = Alignment.M_INDENT_PRESERVE;
		runTest("test07", "X.java");//$NON-NLS-1$ //$NON-NLS-2$
		this.formatterPrefs.text_block_indentation = Alignment.M_INDENT_DEFAULT;
		this.formatterPrefs.put_text_block_quotes_on_new_line = false;
	}

	public void test08() {
		this.formatterPrefs.put_text_block_quotes_on_new_line = true;
		this.formatterPrefs.text_block_indentation = Alignment.M_INDENT_ON_COLUMN;
		runTest("test08", "X.java");//$NON-NLS-1$ //$NON-NLS-2$
		this.formatterPrefs.text_block_indentation = Alignment.M_INDENT_DEFAULT;
		this.formatterPrefs.put_text_block_quotes_on_new_line = false;
	}

}
