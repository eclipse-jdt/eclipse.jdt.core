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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;

/**
 * Tests the Java search engine in Javadoc comment.
 *
 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=45518">bug 45518</a>
 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=46761">bug 46761</a>
 */
public class JavaSearchJavadocTests extends JavaSearchTests {

	Map<String, String> originalOptions;

	public JavaSearchJavadocTests(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.tests.model.SuiteOfTestCases#setUpSuite()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.originalOptions = JAVA_PROJECT.getOptions(true);
		JAVA_PROJECT.setOption(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
		this.resultCollector.showAccuracy(true);
		this.resultCollector.showInsideDoc();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.tests.model.SuiteOfTestCases#tearDownSuite()
	 */
	@Override
	public void tearDown() throws Exception {
		JAVA_PROJECT.setOptions(this.originalOptions);
		super.tearDown();
	}
	private void setJavadocOptions() {
		JAVA_PROJECT.setOption(JavaCore.COMPILER_PB_INVALID_JAVADOC, JavaCore.WARNING);
		JAVA_PROJECT.setOption(JavaCore.COMPILER_PB_MISSING_JAVADOC_COMMENTS, JavaCore.ERROR);
	}
	private void disableJavadocOptions() {
		JAVA_PROJECT.setOption(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.DISABLED);
	}
	public static Test suite() {
		return buildModelTestSuite(JavaSearchJavadocTests.class);
	}
	// Use this static initializer to specify subset for tests
	// All specified tests which do not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "testGenericFieldReferenceAC04" };
//		TESTS_NUMBERS = new int[] { 83285 };
//		TESTS_RANGE = new int[] { -1, -1 };
	}

	/*
	 * Test search of type declaration in javadoc comments
	 * ===================================================
	 */
	public void testJavadocTypeDeclaration() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		search(
				type,
				DECLARATIONS,
				getJavaSearchScope(),
				this.resultCollector);
		assertSearchResults(
				"src/j1/JavadocSearched.java j1.JavadocSearched [JavadocSearched] EXACT_MATCH OUTSIDE_JAVADOC",
				this.resultCollector);
	}
	public void testJavadocTypeStringDeclaration() throws CoreException {
		search(
				"JavadocSearched",
				TYPE,
				DECLARATIONS,
				getJavaSearchScope(),
				this.resultCollector);
		assertSearchResults(
				"src/j1/JavadocSearched.java j1.JavadocSearched [JavadocSearched] EXACT_MATCH OUTSIDE_JAVADOC",
				this.resultCollector);
	}
	public void testJavadocTypeDeclarationWithJavadoc() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		setJavadocOptions();
		search(
				type,
				DECLARATIONS,
				getJavaSearchScope(),
				this.resultCollector);
		assertSearchResults(
				"src/j1/JavadocSearched.java j1.JavadocSearched [JavadocSearched] EXACT_MATCH OUTSIDE_JAVADOC",
				this.resultCollector);
	}

	/*
	 * Test search of field declaration in javadoc comments
	 * ====================================================
	 */
	public void testJavadocFieldDeclaration() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		IField field = type.getField("javadocSearchedVar");
		search(
				field,
				DECLARATIONS,
				getJavaSearchScope(),
				this.resultCollector);
		assertSearchResults(
				"src/j1/JavadocSearched.java j1.JavadocSearched.javadocSearchedVar [javadocSearchedVar] EXACT_MATCH OUTSIDE_JAVADOC",
				this.resultCollector);
	}
	public void testJavadocFieldStringDeclaration() throws CoreException {
		search(
				"javadocSearchedVar",
				FIELD,
				DECLARATIONS,
				getJavaSearchScope(),
				this.resultCollector);
		assertSearchResults(
				"src/j1/JavadocSearched.java j1.JavadocSearched.javadocSearchedVar [javadocSearchedVar] EXACT_MATCH OUTSIDE_JAVADOC",
				this.resultCollector);
	}
	public void testJavadocFieldDeclarationWithJavadoc() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		IField field = type.getField("javadocSearchedVar");
		setJavadocOptions();
		search(
				field,
				DECLARATIONS,
				getJavaSearchScope(),
				this.resultCollector);
		assertSearchResults(
				"src/j1/JavadocSearched.java j1.JavadocSearched.javadocSearchedVar [javadocSearchedVar] EXACT_MATCH OUTSIDE_JAVADOC",
				this.resultCollector);
	}

	/*
	 * Test search of method declarations in javadoc comments
	 * ======================================================
	 */
	public void testJavadocMethodDeclaration() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		IMethod method = type.getMethod("javadocSearchedMethod", null);
		search(
				method,
				DECLARATIONS,
				getJavaSearchScope(),
				this.resultCollector);
		assertSearchResults(
				"src/j1/JavadocSearched.java void j1.JavadocSearched.javadocSearchedMethod() [javadocSearchedMethod] EXACT_MATCH OUTSIDE_JAVADOC",
				this.resultCollector);
	}
	public void testJavadocMethodArgDeclaration() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		IMethod method = type.getMethod("javadocSearchedMethod", new String[] { "QString;" });
		search(
				method,
				DECLARATIONS,
				getJavaSearchScope(),
				this.resultCollector);
		assertSearchResults(
				"src/j1/JavadocSearched.java void j1.JavadocSearched.javadocSearchedMethod(String) [javadocSearchedMethod] EXACT_MATCH OUTSIDE_JAVADOC",
				this.resultCollector);
	}
	public void testJavadocMethodStringDeclaration() throws CoreException {
		search(
				"javadocSearchedMethod",
				METHOD,
				DECLARATIONS,
				getJavaSearchScope(),
				this.resultCollector);
		assertSearchResults(
				"src/j1/JavadocSearched.java void j1.JavadocSearched.javadocSearchedMethod() [javadocSearchedMethod] EXACT_MATCH OUTSIDE_JAVADOC\n" +
				"src/j1/JavadocSearched.java void j1.JavadocSearched.javadocSearchedMethod(String) [javadocSearchedMethod] EXACT_MATCH OUTSIDE_JAVADOC",
				this.resultCollector);
	}
	public void testJavadocMethodDeclarationWithJavadoc() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		setJavadocOptions();
		IMethod method = type.getMethod("javadocSearchedMethod", null);
		search(
				method,
				DECLARATIONS,
				getJavaSearchScope(),
				this.resultCollector);
		assertSearchResults(
				"src/j1/JavadocSearched.java void j1.JavadocSearched.javadocSearchedMethod() [javadocSearchedMethod] EXACT_MATCH OUTSIDE_JAVADOC",
				this.resultCollector);
	}
	public void testJavadocMethodArgDeclarationWithJavadoc() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		setJavadocOptions();
		IMethod method = type.getMethod("javadocSearchedMethod", new String[] { "QString;" });
		search(
				method,
				DECLARATIONS,
				getJavaSearchScope(),
				this.resultCollector);
		assertSearchResults(
				"src/j1/JavadocSearched.java void j1.JavadocSearched.javadocSearchedMethod(String) [javadocSearchedMethod] EXACT_MATCH OUTSIDE_JAVADOC",
				this.resultCollector);
	}

	/*
	 * Test search of type references in javadoc comments
	 * ==================================================
	 */
	public void testJavadocTypeReference() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		search(
				type,
				REFERENCES,
				getJavaSearchScope(),
				this.resultCollector);
		assertSearchResults(
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [j1.JavadocSearched] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH INSIDE_JAVADOC\n"+
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH INSIDE_JAVADOC\n"+
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [j1.JavadocSearched] EXACT_MATCH INSIDE_JAVADOC\n"+
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH INSIDE_JAVADOC\n"+
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH INSIDE_JAVADOC\n"+
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH INSIDE_JAVADOC",
			this.resultCollector);
	}
	public void testJavadocTypeStringReference() throws CoreException {
		search(
				"JavadocSearched",
				TYPE,
				REFERENCES,
				getJavaSearchScope(),
				this.resultCollector);
		assertSearchResults(
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH INSIDE_JAVADOC\n"+
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH INSIDE_JAVADOC\n"+
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH INSIDE_JAVADOC\n"+
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH INSIDE_JAVADOC\n"+
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH INSIDE_JAVADOC\n"+
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH INSIDE_JAVADOC",
			this.resultCollector);
	}
	public void testJavadocTypeReferenceWithJavadoc() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		setJavadocOptions();
		search(
				type,
				REFERENCES,
				getJavaSearchScope(),
				this.resultCollector);
		assertSearchResults(
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [j1.JavadocSearched] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH INSIDE_JAVADOC\n"+
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH INSIDE_JAVADOC\n"+
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [j1.JavadocSearched] EXACT_MATCH INSIDE_JAVADOC\n"+
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH INSIDE_JAVADOC\n"+
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH INSIDE_JAVADOC\n"+
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH INSIDE_JAVADOC",
			this.resultCollector);
	}
	public void testJavadocTypeStringReferenceWithJavadoc() throws CoreException {
		setJavadocOptions();
		search(
				"JavadocSearched",
				TYPE,
				REFERENCES,
				getJavaSearchScope(),
				this.resultCollector);
		assertSearchResults(
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH INSIDE_JAVADOC\n"+
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH INSIDE_JAVADOC\n"+
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH INSIDE_JAVADOC\n"+
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH INSIDE_JAVADOC\n"+
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH INSIDE_JAVADOC\n"+
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH INSIDE_JAVADOC",
			this.resultCollector);
	}
	public void testJavadocTypeStringReferenceWithJavadocDisabled() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		disableJavadocOptions();
		search(
				type,
				REFERENCES,
				getJavaSearchScope(),
				this.resultCollector);
		assertSearchResults("", this.resultCollector);
	}

	/*
	 * Test search of field references in javadoc comments
	 * ===================================================
	 */
	public void testJavadocFieldReference() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		IField field = type.getField("javadocSearchedVar");
		search(
				field,
				REFERENCES,
				getJavaSearchScope(),
				this.resultCollector);
		assertSearchResults(
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [javadocSearchedVar] POTENTIAL_MATCH INSIDE_JAVADOC\n" +
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [javadocSearchedVar] POTENTIAL_MATCH INSIDE_JAVADOC\n" +
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [javadocSearchedVar] EXACT_MATCH INSIDE_JAVADOC",
				this.resultCollector);
	}
	public void testJavadocFieldStringReference() throws CoreException {
		search(
				"javadocSearchedVar",
				FIELD,
				REFERENCES,
				getJavaSearchScope(),
				this.resultCollector);
		assertSearchResults(
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [javadocSearchedVar] POTENTIAL_MATCH INSIDE_JAVADOC\n" +
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [javadocSearchedVar] POTENTIAL_MATCH INSIDE_JAVADOC\n" +
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [javadocSearchedVar] EXACT_MATCH INSIDE_JAVADOC",
				this.resultCollector);
	}
	public void testJavadocFieldReferenceWithJavadoc() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		IField field = type.getField("javadocSearchedVar");
		setJavadocOptions();
		search(
				field,
				REFERENCES,
				getJavaSearchScope(),
				this.resultCollector);
		assertSearchResults(
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [javadocSearchedVar] POTENTIAL_MATCH INSIDE_JAVADOC\n" +
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [javadocSearchedVar] POTENTIAL_MATCH INSIDE_JAVADOC\n" +
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [javadocSearchedVar] EXACT_MATCH INSIDE_JAVADOC",
				this.resultCollector);
	}
	public void testJavadocFieldStringReferenceWithJavadoc() throws CoreException {
		setJavadocOptions();
		search(
				"javadocSearchedVar",
				FIELD,
				REFERENCES,
				getJavaSearchScope(),
				this.resultCollector);
		assertSearchResults(
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [javadocSearchedVar] POTENTIAL_MATCH INSIDE_JAVADOC\n" +
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [javadocSearchedVar] POTENTIAL_MATCH INSIDE_JAVADOC\n" +
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [javadocSearchedVar] EXACT_MATCH INSIDE_JAVADOC",
				this.resultCollector);
	}
	public void testJavadocFieldStringReferenceWithJavadocDisabled() throws CoreException {
		disableJavadocOptions();
		search(
				"javadocSearchedVar",
				FIELD,
				REFERENCES,
				getJavaSearchScope(),
				this.resultCollector);
		assertSearchResults("", this.resultCollector);
	}

	/*
	 * Test search of method references in javadoc comments
	 * ====================================================
	 */
	public void testJavadocMethodReference() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		IMethod method = type.getMethod("javadocSearchedMethod", null);
		search(
				method,
				REFERENCES,
				getJavaSearchScope(),
				this.resultCollector);
		assertSearchResults(
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [javadocSearchedMethod()] POTENTIAL_MATCH INSIDE_JAVADOC\n" +
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [javadocSearchedMethod()] EXACT_MATCH INSIDE_JAVADOC",
				this.resultCollector);
	}
	public void testJavadocMethodArgReference() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		IMethod method = type.getMethod("javadocSearchedMethod", new String[] { "QString;" });
		search(
				method,
				REFERENCES,
				getJavaSearchScope(),
				this.resultCollector);
		assertSearchResults(
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [javadocSearchedMethod(int)] EXACT_MATCH INSIDE_JAVADOC\n" +
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [javadocSearchedMethod(String)] EXACT_MATCH INSIDE_JAVADOC",
				this.resultCollector);
	}
	public void testJavadocMethodStringReference() throws CoreException {
		search(
				"javadocSearchedMethod",
				METHOD,
				REFERENCES,
				getJavaSearchScope(),
				this.resultCollector);
		assertSearchResults(
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [javadocSearchedMethod()] EXACT_MATCH INSIDE_JAVADOC\n" +
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [javadocSearchedMethod(int)] EXACT_MATCH INSIDE_JAVADOC\n" +
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [javadocSearchedMethod()] EXACT_MATCH INSIDE_JAVADOC\n" +
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [javadocSearchedMethod(String)] EXACT_MATCH INSIDE_JAVADOC",
				this.resultCollector);
	}
	public void testJavadocMethodReferenceWithJavadoc() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		setJavadocOptions();
		IMethod method = type.getMethod("javadocSearchedMethod", null);
		search(
				method,
				REFERENCES,
				getJavaSearchScope(),
				this.resultCollector);
		assertSearchResults(
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [javadocSearchedMethod()] POTENTIAL_MATCH INSIDE_JAVADOC\n" +
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [javadocSearchedMethod()] EXACT_MATCH INSIDE_JAVADOC",
				this.resultCollector);
	}
	public void testJavadocMethodArgReferenceWithJavadoc() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		setJavadocOptions();
		IMethod method = type.getMethod("javadocSearchedMethod", new String[] { "QString;" });
		search(
				method,
				REFERENCES,
				getJavaSearchScope(),
				this.resultCollector);
		assertSearchResults(
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [javadocSearchedMethod(int)] EXACT_MATCH INSIDE_JAVADOC\n" +
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [javadocSearchedMethod(String)] EXACT_MATCH INSIDE_JAVADOC",
				this.resultCollector);
	}
	public void testJavadocMethodStringReferenceWithJavadoc() throws CoreException {
		setJavadocOptions();
		search(
				"javadocSearchedMethod",
				METHOD,
				REFERENCES,
				getJavaSearchScope(),
				this.resultCollector);
		assertSearchResults(
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [javadocSearchedMethod()] EXACT_MATCH INSIDE_JAVADOC\n" +
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [javadocSearchedMethod(int)] EXACT_MATCH INSIDE_JAVADOC\n" +
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [javadocSearchedMethod()] EXACT_MATCH INSIDE_JAVADOC\n" +
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [javadocSearchedMethod(String)] EXACT_MATCH INSIDE_JAVADOC",
				this.resultCollector);
	}
	public void testJavadocMethodArgReferenceWithJavadocDisabled() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		IMethod method = type.getMethod("javadocSearchedMethod", new String[] { "QString;" });
		disableJavadocOptions();
		search(
				method,
				REFERENCES,
				getJavaSearchScope(),
				this.resultCollector);
		assertSearchResults("", this.resultCollector);
	}

	/*
	 * Test search of constructor references in javadoc comments
	 * ====================================================
	 */
	public void testJavadocConstructorReference() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		IMethod method = type.getMethod("JavadocSearched", null);
		search(
				method,
				REFERENCES,
				getJavaSearchScope(),
				this.resultCollector);
		assertSearchResults(
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched()] POTENTIAL_MATCH INSIDE_JAVADOC\n" +
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched()] EXACT_MATCH INSIDE_JAVADOC"
		);
	}
	public void testJavadocConstructorArgReference() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		IMethod method = type.getMethod("JavadocSearched", new String[] { "QString;" });
		search(
				method,
				REFERENCES,
				getJavaSearchScope(),
				this.resultCollector);
		assertSearchResults(
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched()] POTENTIAL_MATCH INSIDE_JAVADOC\n" +
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched(String)] EXACT_MATCH INSIDE_JAVADOC"
		);
	}
	public void testJavadocConstructorStringReference() throws CoreException {
		search(
				"JavadocSearched",
				CONSTRUCTOR,
				REFERENCES,
				getJavaSearchScope(),
				this.resultCollector);
		assertSearchResults(
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched()] POTENTIAL_MATCH INSIDE_JAVADOC\n" +
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched(int)] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched()] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched(String)] EXACT_MATCH INSIDE_JAVADOC"
		);
	}
	public void testJavadocConstructorReferenceWithJavadoc() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		setJavadocOptions();
		IMethod method = type.getMethod("JavadocSearched", null);
		search(
				method,
				REFERENCES,
				getJavaSearchScope(),
				this.resultCollector);
		assertSearchResults(
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched()] POTENTIAL_MATCH INSIDE_JAVADOC\n" +
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched()] EXACT_MATCH INSIDE_JAVADOC"
		);
	}
	public void testJavadocConstructorArgReferenceWithJavadoc() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		setJavadocOptions();
		IMethod method = type.getMethod("JavadocSearched", new String[] { "QString;" });
		search(
				method,
				REFERENCES,
				getJavaSearchScope(),
				this.resultCollector);
		assertSearchResults(
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched()] POTENTIAL_MATCH INSIDE_JAVADOC\n" +
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched(String)] EXACT_MATCH INSIDE_JAVADOC"
		);
	}
	public void testJavadocConstructorStringReferenceWithJavadoc() throws CoreException {
		setJavadocOptions();
		search(
			"JavadocSearched",
			CONSTRUCTOR,
			REFERENCES,
			getJavaSearchScope(),
			this.resultCollector);
		assertSearchResults(
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched()] POTENTIAL_MATCH INSIDE_JAVADOC\n" +
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched(int)] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched()] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched(String)] EXACT_MATCH INSIDE_JAVADOC"
		);
	}
	public void testJavadocConstructorReferenceWithJavadocDisabled() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		IMethod method = type.getMethod("JavadocSearched", null);
		disableJavadocOptions();
		search(
				method,
				REFERENCES,
				getJavaSearchScope(),
				this.resultCollector);
		assertSearchResults("");
	}
	private void setUpJavadocTypeParameterReferences() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearch15/src/b81190/Test.java",
			"package b81190;\n" +
			"/**\n" +
			" * @param <T1> First class type parameter\n" +
			" * @param <T2> Second class type parameter\n" +
			" * @param <T3> Last class type parameter\n" +
			" */\n" +
			"public class Test<T1, T2, T3> {\n" +
			"	/**\n" +
			"	 * @param <U> Method type parameter\n" +
			"	 * @param x Method parameter\n" +
			"	 */\n" +
			"	<U> void generic(U x, T1 t) {\n" +
			"		Object o = x;\n" +
			"		o.toString();\n" +
			"	}\n" +
			"}\n"
		);
	}
	public void testJavadocTypeParameterReferences01() throws CoreException {
		setUpJavadocTypeParameterReferences();
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(this.workingCopies);
		ITypeParameter typeParam = selectTypeParameter(this.workingCopies[0], "T1", 2);
		search(typeParam, REFERENCES, scope);
		assertSearchResults(
			"src/b81190/Test.java b81190.Test [T1] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/b81190/Test.java void b81190.Test.generic(U, T1) [T1] EXACT_MATCH OUTSIDE_JAVADOC"
		);
	}
	public void testJavadocTypeParameterReferences02() throws CoreException {
		setUpJavadocTypeParameterReferences();
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(this.workingCopies);
		ITypeParameter typeParam = selectTypeParameter(this.workingCopies[0], "U", 2);
		search(typeParam, REFERENCES, scope);
		assertSearchResults(
			"src/b81190/Test.java void b81190.Test.generic(U, T1) [U] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/b81190/Test.java void b81190.Test.generic(U, T1) [U] EXACT_MATCH OUTSIDE_JAVADOC"
		);
	}
	// Local variables references in Javadoc have been fixed while implementing 81190
	public void testJavadocParameterReferences01() throws CoreException {
		setUpJavadocTypeParameterReferences();
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(this.workingCopies);
		ILocalVariable local = selectLocalVariable(this.workingCopies[0], "x", 2);
		search(local, REFERENCES, scope);
		assertSearchResults(
			"src/b81190/Test.java void b81190.Test.generic(U, T1) [x] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/b81190/Test.java void b81190.Test.generic(U, T1) [x] EXACT_MATCH OUTSIDE_JAVADOC"
		);
	}

	/**
	 * Test fix for bug 47909.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=47909">47909</a>
	 */
	public void testBug47909() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j3", "Y.java").getType("Y");
		setJavadocOptions();
		IMethod method = type.getMethod("Y", new String[] { "I" });
		search(
			method,
			REFERENCES,
			getJavaSearchScope(),
			this.resultCollector);
		assertSearchResults(
			"test47909.jar void j3.X.bar() EXACT_MATCH OUTSIDE_JAVADOC",
			this.resultCollector);
	}

	/**
	 * Test fix for bug 47968.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=47968">47968</a>
	 */
	public void testBug47968type() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j2", "Bug47968.java").getType("Bug47968");
		setJavadocOptions();
		search(
			type,
			REFERENCES,
			getJavaSearchScope(),
			this.resultCollector);
		assertSearchResults(
			// These matches were not found before...
			"src/j2/Bug47968s.java j2.Bug47968s [Bug47968] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/j2/Bug47968s.java j2.Bug47968s [Bug47968] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/j2/Bug47968s.java j2.Bug47968s [Bug47968] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/j2/Bug47968s.java j2.Bug47968s [Bug47968] EXACT_MATCH INSIDE_JAVADOC\n" +
			// ...end
			"src/j2/Bug47968s.java j2.Bug47968s.y [Bug47968] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/j2/Bug47968s.java j2.Bug47968s.y [Bug47968] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/j2/Bug47968s.java j2.Bug47968s.y [Bug47968] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/j2/Bug47968s.java j2.Bug47968s.y [Bug47968] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/j2/Bug47968s.java j2.Bug47968s() [Bug47968] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/j2/Bug47968s.java j2.Bug47968s() [Bug47968] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/j2/Bug47968s.java j2.Bug47968s() [Bug47968] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/j2/Bug47968s.java j2.Bug47968s() [Bug47968] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/j2/Bug47968s.java void j2.Bug47968s.bar() [Bug47968] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/j2/Bug47968s.java void j2.Bug47968s.bar() [Bug47968] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/j2/Bug47968s.java void j2.Bug47968s.bar() [Bug47968] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/j2/Bug47968s.java void j2.Bug47968s.bar() [Bug47968] EXACT_MATCH INSIDE_JAVADOC",
			this.resultCollector);
	}
	public void testBug47968field() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j2", "Bug47968.java").getType("Bug47968");
		setJavadocOptions();
		IField field = type.getField("x");
		search(
			field,
			REFERENCES,
			getJavaSearchScope(),
			this.resultCollector);
		assertSearchResults(
			"src/j2/Bug47968s.java j2.Bug47968s [x] EXACT_MATCH INSIDE_JAVADOC\n" + // This match was not found before...
			"src/j2/Bug47968s.java j2.Bug47968s.y [x] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/j2/Bug47968s.java j2.Bug47968s() [x] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/j2/Bug47968s.java void j2.Bug47968s.bar() [x] EXACT_MATCH INSIDE_JAVADOC",
			this.resultCollector);
	}
	public void testBug47968method() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j2", "Bug47968.java").getType("Bug47968");
		setJavadocOptions();
		IMethod method = type.getMethod("foo", new String[] { "I" });
		search(
			method,
			REFERENCES,
			getJavaSearchScope(),
			this.resultCollector);
		assertSearchResults(
			"src/j2/Bug47968s.java j2.Bug47968s [foo(int)] EXACT_MATCH INSIDE_JAVADOC\n" + // This match was not found before...
			"src/j2/Bug47968s.java j2.Bug47968s.y [foo(int)] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/j2/Bug47968s.java j2.Bug47968s() [foo(int)] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/j2/Bug47968s.java void j2.Bug47968s.bar() [foo(int)] EXACT_MATCH INSIDE_JAVADOC",
			this.resultCollector);
	}
	public void testBug47968constructor() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j2", "Bug47968.java").getType("Bug47968");
		setJavadocOptions();
		IMethod method = type.getMethod("Bug47968", new String[] { "QString;" });
		search(
			method,
			REFERENCES,
			getJavaSearchScope(),
			this.resultCollector);
		assertSearchResults(
			"src/j2/Bug47968s.java j2.Bug47968s [Bug47968(String)] EXACT_MATCH INSIDE_JAVADOC\n" + // This match was not found before...
			"src/j2/Bug47968s.java j2.Bug47968s.y [Bug47968(String)] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/j2/Bug47968s.java j2.Bug47968s() [Bug47968(String)] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/j2/Bug47968s.java void j2.Bug47968s.bar() [Bug47968(String)] EXACT_MATCH INSIDE_JAVADOC",
			this.resultCollector);
	}

	/**
	 * Test fix for bug 47209.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=47209">47209</a>
	 */
	public void testBug47209type() throws CoreException {
		setJavadocOptions();
		IType type = getCompilationUnit("JavaSearch", "src", "j4", "TT47209.java").getType("TT47209");
		search(type,  REFERENCES, getJavaSearchScope());
		type = getCompilationUnit("JavaSearch", "src", "j4", "TF47209.java").getType("TF47209");
		search(type,  REFERENCES, getJavaSearchScope());
		type = getCompilationUnit("JavaSearch", "src", "j4", "TC47209.java").getType("TC47209");
		search(type,  REFERENCES, getJavaSearchScope());
		type = getCompilationUnit("JavaSearch", "src", "j4", "TT47209.java").getType("TM47209");
		search(type,  REFERENCES, getJavaSearchScope());
		assertSearchResults(
			"src/j4/TT47209.java j4.TT47209 [TT47209] EXACT_MATCH INSIDE_JAVADOC\n" +
				"src/j4/TF47209.java j4.TF47209.f47209 [TF47209] EXACT_MATCH INSIDE_JAVADOC\n" +
				"src/j4/TC47209.java j4.TC47209(String) [TC47209] EXACT_MATCH INSIDE_JAVADOC\n" +
				"src/j4/TM47209.java void j4.TM47209.m47209(int) [TM47209] EXACT_MATCH INSIDE_JAVADOC",
			this.resultCollector);
	}
	public void testBug47209field() throws CoreException {
		setJavadocOptions();
		IType type = getCompilationUnit("JavaSearch", "src", "j4", "FT47209.java").getType("FT47209");
		search(type,  REFERENCES, getJavaSearchScope());
		type = getCompilationUnit("JavaSearch", "src", "j4", "FF47209.java").getType("FF47209");
		search(type,  REFERENCES, getJavaSearchScope());
		type = getCompilationUnit("JavaSearch", "src", "j4", "FC47209.java").getType("FC47209");
		search(type,  REFERENCES, getJavaSearchScope());
		type = getCompilationUnit("JavaSearch", "src", "j4", "FT47209.java").getType("FM47209");
		search(type,  REFERENCES, getJavaSearchScope());
		assertSearchResults(
			"src/j4/FT47209.java j4.FT47209 [FT47209] EXACT_MATCH INSIDE_JAVADOC\n" +
				"src/j4/FF47209.java j4.FF47209.f47209 [FF47209] EXACT_MATCH INSIDE_JAVADOC\n" +
				"src/j4/FC47209.java j4.FC47209(String) [FC47209] EXACT_MATCH INSIDE_JAVADOC\n" +
				"src/j4/FM47209.java void j4.FM47209.m47209(int) [FM47209] EXACT_MATCH INSIDE_JAVADOC",
			this.resultCollector);
	}
	public void testBug47209method() throws CoreException {
		setJavadocOptions();
		IType type = getCompilationUnit("JavaSearch", "src", "j4", "MT47209.java").getType("MT47209");
		search(type,  REFERENCES, getJavaSearchScope());
		type = getCompilationUnit("JavaSearch", "src", "j4", "MF47209.java").getType("MF47209");
		search(type,  REFERENCES, getJavaSearchScope());
		type = getCompilationUnit("JavaSearch", "src", "j4", "MC47209.java").getType("MC47209");
		search(type,  REFERENCES, getJavaSearchScope());
		type = getCompilationUnit("JavaSearch", "src", "j4", "MT47209.java").getType("MM47209");
		search(type,  REFERENCES, getJavaSearchScope());
		assertSearchResults(
			"src/j4/MT47209.java j4.MT47209 [MT47209] EXACT_MATCH INSIDE_JAVADOC\n" +
				"src/j4/MF47209.java j4.MF47209.f47209 [MF47209] EXACT_MATCH INSIDE_JAVADOC\n" +
				"src/j4/MC47209.java j4.MC47209(String) [MC47209] EXACT_MATCH INSIDE_JAVADOC\n" +
				"src/j4/MM47209.java void j4.MM47209.m47209(int) [MM47209] EXACT_MATCH INSIDE_JAVADOC",
			this.resultCollector);
	}
	public void testBug47209constructor() throws CoreException {
		setJavadocOptions();
		IType type = getCompilationUnit("JavaSearch", "src", "j4", "CT47209.java").getType("CT47209");
		search(type,  REFERENCES, getJavaSearchScope());
		type = getCompilationUnit("JavaSearch", "src", "j4", "CF47209.java").getType("CF47209");
		search(type,  REFERENCES, getJavaSearchScope());
		type = getCompilationUnit("JavaSearch", "src", "j4", "CC47209.java").getType("CC47209");
		search(type,  REFERENCES, getJavaSearchScope());
		type = getCompilationUnit("JavaSearch", "src", "j4", "CT47209.java").getType("CM47209");
		search(type,  REFERENCES, getJavaSearchScope());
		assertSearchResults(
			"src/j4/CT47209.java j4.CT47209 [CT47209] EXACT_MATCH INSIDE_JAVADOC\n" +
				"src/j4/CF47209.java j4.CF47209.f47209 [CF47209] EXACT_MATCH INSIDE_JAVADOC\n" +
				"src/j4/CC47209.java j4.CC47209(String) [CC47209] EXACT_MATCH INSIDE_JAVADOC\n" +
				"src/j4/CM47209.java void j4.CM47209.m47209(int) [CM47209] EXACT_MATCH INSIDE_JAVADOC",
			this.resultCollector);
	}

	/**
	 * Test fix for bug 49994.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=49994">49994</a>
	 */
	public void testBug49994() throws CoreException {
		setJavadocOptions();
		IType type = getCompilationUnit("JavaSearch", "src", "j5", "Bug49994.java").getType("Bug49994");
		search(type,  REFERENCES, getJavaSearchScope());
		assertSearchResults("", this.resultCollector);
	}
	public void testBug49994field() throws CoreException {
		setJavadocOptions();
		IType type = getCompilationUnit("JavaSearch", "src", "j5", "Bug49994.java").getType("Bug49994");
		IField field = type.getField("field");
		search(field, REFERENCES, getJavaSearchScope(), this.resultCollector);
		assertSearchResults("src/j5/Bug49994.java void j5.Bug49994.foo() [field] EXACT_MATCH INSIDE_JAVADOC", this.resultCollector);
	}
	public void testBug49994method() throws CoreException {
		setJavadocOptions();
		IType type = getCompilationUnit("JavaSearch", "src", "j5", "Bug49994.java").getType("Bug49994");
		IMethod method = type.getMethod("bar", new String[0]);
		search(method, REFERENCES, getJavaSearchScope(), this.resultCollector);
		assertSearchResults("src/j5/Bug49994.java void j5.Bug49994.foo() [bar()] EXACT_MATCH INSIDE_JAVADOC", this.resultCollector);
	}
	public void testBug49994constructor() throws CoreException {
		setJavadocOptions();
		IType type = getCompilationUnit("JavaSearch", "src", "j5", "Bug49994.java").getType("Bug49994");
		IMethod method = type.getMethod("Bug49994", new String[] { "QString;" });
		search(method, REFERENCES, getJavaSearchScope(), this.resultCollector);
		assertSearchResults("src/j5/Bug49994.java void j5.Bug49994.foo() [Bug49994(String)] EXACT_MATCH INSIDE_JAVADOC", this.resultCollector);
	}

	/**
	 * Test fix for bug 54962.
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=54962">54962</a>
	 * @throws CoreException
	 * TODO (frederic) should no longer get POTENTIAL after 196200 got fixed
	 */
	public void testBug54962() throws CoreException {
		setJavadocOptions();
		this.resultCollector.showInsideDoc();
		this.resultCollector.showSelection();
		IPackageDeclaration packDecl = getCompilationUnit("JavaSearch", "src", "j6", "Bug54962.java").getPackageDeclaration("j6");
		search(packDecl, REFERENCES, getJavaSearchScope());
		assertSearchResults(
				"src/j6/Bug54962.java j6.Bug54962 [ * @see !|j6|!] EXACT_MATCH INSIDE_JAVADOC\n" +
				"src/j6/Bug54962.java j6.Bug54962 [ * @see !|j6|!.BUG54962] EXACT_MATCH INSIDE_JAVADOC\n" +
				"src/j6/Bug54962.java j6.Bug54962 [ * @see !|j6|!.Bug54962] EXACT_MATCH INSIDE_JAVADOC\n" +
				"src/j7/qua/li/fied/Bug54962a.java [import !|j6|!.Bug54962;] EXACT_MATCH OUTSIDE_JAVADOC",
			this.resultCollector);
	}
	//	TODO (frederic) should no longer get POTENTIAL after 196200 got fixed
	public void testBug54962qualified() throws CoreException {
		setJavadocOptions();
		this.resultCollector.showInsideDoc();
		this.resultCollector.showSelection();
		IPackageDeclaration packDecl = getCompilationUnit("JavaSearch", "src", "j7.qua.li.fied", "Bug54962a.java").getPackageDeclaration("j7.qua.li.fied");
		search(packDecl, REFERENCES, getJavaSearchScope());
		assertSearchResults(
				"src/j7/qua/li/fied/Bug54962a.java j7.qua.li.fied.Bug54962a [ * @see !|j7.qua.li.fied|!] EXACT_MATCH INSIDE_JAVADOC\n" +
				"src/j7/qua/li/fied/Bug54962a.java j7.qua.li.fied.Bug54962a [ * @see !|j7.qua.li.fied|!.BUG54962a] EXACT_MATCH INSIDE_JAVADOC\n" +
				"src/j7/qua/li/fied/Bug54962a.java j7.qua.li.fied.Bug54962a [ * @see !|j7.qua.li.fied|!.Bug54962a] EXACT_MATCH INSIDE_JAVADOC",
			this.resultCollector);
	}

	/**
	 * Test fix for bug 71267: [Search][Javadoc] SearchMatch in class javadoc reported with element of type IImportDeclaration
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=71267">71267</a>
	 */
	public void testBug71267() throws CoreException {
		setJavadocOptions();
		this.resultCollector.showInsideDoc();
		IPackageDeclaration packDecl = getCompilationUnit("JavaSearch", "src", "p71267", "Test.java").getPackageDeclaration("p71267");
		search(packDecl, REFERENCES, getJavaSearchScope());
		assertSearchResults(
			"src/p71267/Test.java p71267.Test [p71267] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/p71267/Test.java p71267.Test [p71267] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/p71267/q71267/Test.java p71267.q71267.Test [p71267] EXACT_MATCH INSIDE_JAVADOC",
			this.resultCollector);
	}
	public void testBug71267qualified() throws CoreException {
		setJavadocOptions();
		this.resultCollector.showInsideDoc();
		IPackageDeclaration packDecl = getCompilationUnit("JavaSearch", "src", "p71267.q71267", "Test.java").getPackageDeclaration("p71267.q71267");
		search(packDecl, REFERENCES, getJavaSearchScope());
		assertSearchResults(
			"src/p71267/q71267/Test.java p71267.q71267.Test [p71267.q71267] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/p71267/q71267/Test.java p71267.q71267.Test [p71267.q71267] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/p71267/q71267/Test.java p71267.q71267.Test.field [p71267.q71267] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/p71267/q71267/Test.java p71267.q71267.Test.field [p71267.q71267] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/p71267/q71267/Test.java void p71267.q71267.Test.method() [p71267.q71267] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/p71267/q71267/Test.java void p71267.q71267.Test.method() [p71267.q71267] EXACT_MATCH INSIDE_JAVADOC",
			this.resultCollector);
	}

	/**
	 * Bug 83285: [javadoc] Javadoc reference to constructor of secondary type has no binding / not found by search
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=83285"
	 */
	public void testBug83285() throws CoreException {
		this.resultCollector.showRule();
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearch/src/b83285/A.java",
			"package b83285;\n" +
			"class A { }\n" +
			"class C {\n" +
			"    /**\n" +
			"     * Link {@link #C(String)} was also wrongly warned...\n" +
			"     */\n" +
			"    private String fGerman;\n" +
			"    public C(String german) {\n" +
			"        fGerman = german;\n" +
			"    }\n" +
			"}"
			);
		IMethod[] methods = this.workingCopies[0].getType("C").getMethods();
		assertEquals("Invalid number of methods", 1, methods.length);
		search(methods[0], REFERENCES, getJavaSearchScope());
		assertSearchResults(
			"src/b83285/A.java b83285.C.fGerman [C(String)] EXACT_MATCH INSIDE_JAVADOC"
		);
	}
	public void testBug83285a() throws CoreException {
		this.resultCollector.showRule();
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearch/src/b83285/A.java",
			"package b83285;\n" +
			"class A {\n" +
			"	A(char c) {}\n" +
			"}\n" +
			"class B {\n" +
			"	B(Exception ex) {}\n" +
			"	void foo() {} \n" +
			"	class C { \n" +
			"	    /**\n" +
			"	     * Link {@link #B(Exception)} OK\n" +
			"	     * Link {@link #C(String)} OK\n" +
			"	     * Link {@link #foo()} OK\n" +
			"	     * Link {@link #bar()} OK\n" +
			"	     */\n" +
			"	    public C(String str) {}\n" +
			"		void bar() {}\n" +
			"	}\n" +
			"}"
		);
		IMethod[] methods = this.workingCopies[0].getType("B").getMethods();
		assertEquals("Invalid number of methods", 2, methods.length);
		for (int i=0,l=methods.length; i<l; i++) {
			search(methods[i], REFERENCES, getJavaSearchScope());
		}
		assertSearchResults(
			"src/b83285/A.java b83285.B$C(String) [B(Exception)] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/b83285/A.java b83285.B$C(String) [foo()] EXACT_MATCH INSIDE_JAVADOC"
		);
	}
	public void testBug83285b() throws CoreException {
		this.resultCollector.showRule();
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearch/src/b83285/A.java",
			"package b83285;\n" +
			"class A {\n" +
			"	A(char c) {}\n" +
			"}\n" +
			"class B {\n" +
			"	B(Exception ex) {}\n" +
			"	void foo() {} \n" +
			"	class C { \n" +
			"	    /**\n" +
			"	     * Link {@link #B(Exception)} OK\n" +
			"	     * Link {@link #C(String)} OK\n" +
			"	     * Link {@link #foo()} OK\n" +
			"	     * Link {@link #bar()} OK\n" +
			"	     */\n" +
			"	    public C(String str) {}\n" +
			"		void bar() {}\n" +
			"	}\n" +
			"}"
		);
		IMethod[] methods = this.workingCopies[0].getType("B").getType("C").getMethods();
		assertEquals("Invalid number of methods", 2, methods.length);
		for (int i=0,l=methods.length; i<l; i++) {
			search(methods[i], REFERENCES, getJavaSearchScope());
		}
		assertSearchResults(
			"src/b83285/A.java b83285.B$C(String) [C(String)] EXACT_MATCH INSIDE_JAVADOC\n" +
			"src/b83285/A.java b83285.B$C(String) [bar()] EXACT_MATCH INSIDE_JAVADOC"
		);
	}
}
