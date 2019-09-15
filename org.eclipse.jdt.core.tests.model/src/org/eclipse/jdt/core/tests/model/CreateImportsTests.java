/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

@SuppressWarnings({"rawtypes", "unchecked"})
public class CreateImportsTests extends AbstractJavaModelTests {

	public CreateImportsTests(String name) {
		super(name);
	}
	public static Test suite() {
		return buildModelTestSuite(CreateImportsTests.class);
	}
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ICompilationUnit workingCopy = getCompilationUnit("P/X.java");
		workingCopy.becomeWorkingCopy(null/*no progress*/);
		this.workingCopies = new ICompilationUnit[] {workingCopy};
		setContents(
			"public class X {\n" +
			"}"
		);
	}
	@Override
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		createJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB"}, "", "1.5");
	}
	@Override
	public void tearDownSuite() throws Exception {
		deleteProject("P");
		super.tearDownSuite();
	}

	private String createImport(String importName, int flags) throws JavaModelException {
		return createImport(importName, flags, null/*no sibling*/);
	}
	private String createImport(String importName, int flags, IJavaElement sibling) throws JavaModelException {
		ICompilationUnit workingCopy = this.workingCopies[0];
		workingCopy.createImport(importName, sibling, flags, null/*no progress*/);
		Map options = getJavaProject("P").getOptions(true);
		options.put(DefaultCodeFormatterConstants.FORMATTER_NUMBER_OF_EMPTY_LINES_TO_PRESERVE, "0");
		CodeFormatter formatter = ToolFactory.createCodeFormatter(options);
		String source = workingCopy.getSource();
		Document document = new Document(source);
		TextEdit edit = formatter.format(CodeFormatter.K_COMPILATION_UNIT, source, 0, source.length() - 1, 0, "\n");
		try {
			edit.apply(document, TextEdit.NONE);
		} catch(MalformedTreeException e) {
			// ignore
		} catch(BadLocationException e) {
			// ignore
		}
		return document.get();
	}

	private void setContents(String contents) throws JavaModelException {
		this.workingCopies[0].getBuffer().setContents(contents);
		this.workingCopies[0].makeConsistent(null/*no progress*/);
	}

	/*
	 * Ensures that adding a static import is reflected in the source.
	 */
	public void test001() throws JavaModelException {
		String actualSource = createImport("java.lang.Math.*", Flags.AccStatic);
		assertSourceEquals(
			"Unexpected source",
			"import static java.lang.Math.*;\n" +
			"\n" +
			"public class X {\n" +
			"}",
			actualSource);
	}

	/*
	 * Ensures that adding a non-static import is reflected in the source.
	 */
	public void test002() throws JavaModelException {
		String actualSource = createImport("java.util.ZipFile", Flags.AccDefault);
		assertSourceEquals(
			"Unexpected source",
			"import java.util.ZipFile;\n" +
			"\n" +
			"public class X {\n" +
			"}",
			actualSource);
	}

	/*
	 * Ensures that adding the same static import doesn't change the source.
	 */
	public void test003() throws JavaModelException {
		setContents(
			"import static java.lang.Math.*;\n" +
			"\n" +
			"public class X {\n" +
			"}"
		);
		String actualSource = createImport("java.lang.Math.*", Flags.AccStatic);
		assertSourceEquals(
			"Unexpected source",
			"import static java.lang.Math.*;\n" +
			"\n" +
			"public class X {\n" +
			"}",
			actualSource);
	}

	/*
	 * Ensures that adding the same non-static import doesn't change the source.
	 */
	public void test004() throws JavaModelException {
		setContents(
			"import java.util.ZipFile;\n" +
			"\n" +
			"public class X {\n" +
			"}"
		);
		String actualSource = createImport("java.util.ZipFile", Flags.AccDefault);
		assertSourceEquals(
			"Unexpected source",
			"import java.util.ZipFile;\n" +
			"\n" +
			"public class X {\n" +
			"}",
			actualSource);
	}

	/*
	 * Ensures that adding an onDemand static import starting with an existing non-onDemand import
	 * is reflected in the source.
	 */
	public void test005() throws JavaModelException {
		setContents(
			"import java.util.ZipFile;\n" +
			"\n" +
			"public class X {\n" +
			"}"
		);
		String actualSource = createImport("java.util.ZipFile.*", Flags.AccStatic);
		assertSourceEquals(
			"Unexpected source",
			"import java.util.ZipFile;\n" +
			"import static java.util.ZipFile.*;\n" +
			"\n" +
			"public class X {\n" +
			"}",
			actualSource);
	}

	/*
	 * Ensures that adding an onDemand non-static import starting with an existing non-onDemand import
	 * is reflected in the source.
	 */
	public void test006() throws JavaModelException {
		setContents(
			"import java.util.ZipFile;\n" +
			"import static java.util.ZipFile.*;\n" +
			"\n" +
			"public class X {\n" +
			"}"
		);
		String actualSource = createImport("java.util.ZipFile.*", Flags.AccDefault);
		assertSourceEquals(
			"Unexpected source",
			"import java.util.ZipFile;\n" +
			"import static java.util.ZipFile.*;\n" +
			"import java.util.ZipFile.*;\n" +
			"\n" +
			"public class X {\n" +
			"}",
			actualSource);
	}

	/*
	 * Ensures that adding an import triggers the correct delta.
	 */
	public void test007() throws JavaModelException {
		try {
			startDeltas();
			createImport("java.util.ZipFile", Flags.AccDefault);
			assertWorkingCopyDeltas(
				"Unexpected delta",
				"<import container>[+]: {}"
			);
		} finally {
			stopDeltas();
		}
	}

	/*
	 * Ensures that adding an import triggers the correct delta.
	 */
	public void test008() throws JavaModelException {
		setContents(
			"import static java.lang.Math.*;\n" +
			"\n" +
			"public class X {\n" +
			"}"
		);
		try {
			startDeltas();
			createImport("java.util.*", Flags.AccDefault);
			assertWorkingCopyDeltas(
				"Unexpected delta",
				"<import container>[*]: {CHILDREN | FINE GRAINED}\n" +
				"	import java.util.*[+]: {}"
			);
		} finally {
			stopDeltas();
		}
	}

	/*
	 * Ensures that adding an import before a sibling is correctly reflected in the source.
	 */
	public void test009() throws JavaModelException {
		setContents(
			"import static java.lang.Math.*;\n" +
			"\n" +
			"public class X {\n" +
			"}"
		);
		IJavaElement sibling = this.workingCopies[0].getImport("java.lang.Math.*");
		String actualSource = createImport("java.util.ZipFile", Flags.AccDefault, sibling);
		assertSourceEquals(
			"Unexpected source",
			"import java.util.ZipFile;\n" +
			"import static java.lang.Math.*;\n" +
			"\n" +
			"public class X {\n" +
			"}",
			actualSource);
	}

	/*
	 * Ensures that adding a null import throws the correct exception.
	 */
	public void test010() throws JavaModelException {
		JavaModelException exception = null;
		try {
			createImport(null, Flags.AccStatic);
		} catch (JavaModelException e) {
			exception = e;
		}
		assertExceptionEquals(
			"Unexpected exception",
			"Invalid name specified: null",
			exception);
	}

	/*
	 * Ensures that adding an import with an invalid name throws the correct exception.
	 */
	public void test011() throws JavaModelException {
		JavaModelException exception = null;
		try {
			createImport("java.,.", Flags.AccStatic);
		} catch (JavaModelException e) {
			exception = e;
		}
		assertExceptionEquals(
			"Unexpected exception",
			"Invalid name specified: java.,.",
			exception);
	}
}
