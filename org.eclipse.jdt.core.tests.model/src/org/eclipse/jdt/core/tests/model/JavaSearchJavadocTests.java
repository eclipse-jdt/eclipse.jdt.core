/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.search.SearchEngine;

/**
 * Tests the Java search engine in Javadoc comment.
 *
 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=45518">bug 45518</a>
 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=46761">bug 46761</a>
 */
public class JavaSearchJavadocTests extends JavaSearchTests {

	Map originalOptions;

	/**
	 * @param name
	 */
	public JavaSearchJavadocTests(String name) {
		super(name);
	}
	private void resetProjectOptions() {
		this.javaProject.setOptions(originalOptions);
	}
	private void setJavadocOptions() {
		this.originalOptions = this.javaProject.getOptions(true);
		this.javaProject.setOption(JavaCore.COMPILER_PB_INVALID_JAVADOC, JavaCore.WARNING);
		this.javaProject.setOption(JavaCore.COMPILER_PB_MISSING_JAVADOC, JavaCore.ENABLED);
	}
	public static Test suite() {
		// NOTE: cannot use 'new Suite(JavaSearchJavadocTests.class)' as this would include tests from super class
		TestSuite suite = new Suite(JavaSearchJavadocTests.class.getName());
		
		// Tests on type declarations
		suite.addTest(new JavaSearchJavadocTests("testJavadocTypeDeclaration"));
		suite.addTest(new JavaSearchJavadocTests("testJavadocTypeDeclarationWithJavadoc"));
		suite.addTest(new JavaSearchJavadocTests("testJavadocTypeStringDeclaration"));
		
		// Tests on field declarations
		suite.addTest(new JavaSearchJavadocTests("testJavadocFieldDeclaration"));
		suite.addTest(new JavaSearchJavadocTests("testJavadocFieldDeclarationWithJavadoc"));
		suite.addTest(new JavaSearchJavadocTests("testJavadocFieldStringDeclaration"));
		
		// Tests on method declarations
		suite.addTest(new JavaSearchJavadocTests("testJavadocMethodDeclaration"));
		suite.addTest(new JavaSearchJavadocTests("testJavadocMethodArgDeclaration"));
		suite.addTest(new JavaSearchJavadocTests("testJavadocMethodDeclarationWithJavadoc"));
		suite.addTest(new JavaSearchJavadocTests("testJavadocMethodArgDeclarationWithJavadoc"));
		suite.addTest(new JavaSearchJavadocTests("testJavadocMethodStringDeclaration"));
		
		// Tests on type references
		suite.addTest(new JavaSearchJavadocTests("testJavadocTypeReference"));
		suite.addTest(new JavaSearchJavadocTests("testJavadocTypeReferenceWithJavadoc"));
		suite.addTest(new JavaSearchJavadocTests("testJavadocTypeStringReference"));
		suite.addTest(new JavaSearchJavadocTests("testJavadocTypeStringReferenceWithJavadoc"));
		
		// Tests on field references
		suite.addTest(new JavaSearchJavadocTests("testJavadocFieldReference"));
		suite.addTest(new JavaSearchJavadocTests("testJavadocFieldReferenceWithJavadoc"));
		suite.addTest(new JavaSearchJavadocTests("testJavadocFieldStringReference"));
		suite.addTest(new JavaSearchJavadocTests("testJavadocFieldStringReferenceWithJavadoc"));
		
		// Tests on method references
		suite.addTest(new JavaSearchJavadocTests("testJavadocMethodReference"));
		suite.addTest(new JavaSearchJavadocTests("testJavadocMethodArgReference"));
		suite.addTest(new JavaSearchJavadocTests("testJavadocMethodReferenceWithJavadoc"));
		suite.addTest(new JavaSearchJavadocTests("testJavadocMethodArgReferenceWithJavadoc"));
		suite.addTest(new JavaSearchJavadocTests("testJavadocMethodStringReference"));
		suite.addTest(new JavaSearchJavadocTests("testJavadocMethodStringReferenceWithJavadoc"));
		
		// Tests on constrcutor references
		suite.addTest(new JavaSearchJavadocTests("testJavadocConstructorReference"));
		suite.addTest(new JavaSearchJavadocTests("testJavadocConstructorArgReference"));
		suite.addTest(new JavaSearchJavadocTests("testJavadocConstructorReferenceWithJavadoc"));
		suite.addTest(new JavaSearchJavadocTests("testJavadocConstructorArgReferenceWithJavadoc"));
		suite.addTest(new JavaSearchJavadocTests("testJavadocConstructorStringReference"));
		suite.addTest(new JavaSearchJavadocTests("testJavadocConstructorStringReferenceWithJavadoc"));
		
		return suite;
	}

	/*
	 * Test search of type declaration in javadoc comments
	 * ===================================================
	 */
	public void testJavadocTypeDeclaration() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		JavaSearchResultCollector result = new JavaSearchResultCollector();
		result.showAccuracy = true;
		new SearchEngine().search(
				getWorkspace(), 
				type,
				DECLARATIONS, 
				getJavaSearchScope(), 
				result
				);
		assertSearchResults(
				"src/j1/JavadocSearched.java j1.JavadocSearched [JavadocSearched] EXACT_MATCH",
				result);
	}
	public void testJavadocTypeStringDeclaration() throws CoreException {
		JavaSearchResultCollector result = new JavaSearchResultCollector();
		result.showAccuracy = true;
		new SearchEngine().search(
				getWorkspace(), 
				"JavadocSearched",
				TYPE,
				DECLARATIONS, 
				getJavaSearchScope(), 
				result
				);
		assertSearchResults(
				"src/j1/JavadocSearched.java j1.JavadocSearched [JavadocSearched] EXACT_MATCH",
				result);
	}
	public void testJavadocTypeDeclarationWithJavadoc() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		try {
			setJavadocOptions();
			JavaSearchResultCollector result = new JavaSearchResultCollector();
			result.showAccuracy = true;
			new SearchEngine().search(
					getWorkspace(), 
					type,
					DECLARATIONS, 
					getJavaSearchScope(), 
					result
					);
			assertSearchResults(
					"src/j1/JavadocSearched.java j1.JavadocSearched [JavadocSearched] EXACT_MATCH",
					result);
		} finally {
			resetProjectOptions();
		}
	}

	/*
	 * Test search of field declaration in javadoc comments
	 * ====================================================
	 */
	public void testJavadocFieldDeclaration() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		IField field = type.getField("javadocSearchedVar");
		JavaSearchResultCollector result = new JavaSearchResultCollector();
		result.showAccuracy = true;
		new SearchEngine().search(
				getWorkspace(), 
				field,
				DECLARATIONS, 
				getJavaSearchScope(), 
				result
				);
		assertSearchResults(
				"src/j1/JavadocSearched.java j1.JavadocSearched.javadocSearchedVar [javadocSearchedVar] EXACT_MATCH",
				result);
	}
	public void testJavadocFieldStringDeclaration() throws CoreException {
		JavaSearchResultCollector result = new JavaSearchResultCollector();
		result.showAccuracy = true;
		new SearchEngine().search(
				getWorkspace(), 
				"javadocSearchedVar",
				FIELD,
				DECLARATIONS, 
				getJavaSearchScope(), 
				result
				);
		assertSearchResults(
				"src/j1/JavadocSearched.java j1.JavadocSearched.javadocSearchedVar [javadocSearchedVar] EXACT_MATCH",
				result);
	}
	public void testJavadocFieldDeclarationWithJavadoc() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		IField field = type.getField("javadocSearchedVar");
		try {
			setJavadocOptions();
			JavaSearchResultCollector result = new JavaSearchResultCollector();
			result.showAccuracy = true;
			new SearchEngine().search(
					getWorkspace(), 
					field,
					DECLARATIONS, 
					getJavaSearchScope(), 
					result
					);
			assertSearchResults(
					"src/j1/JavadocSearched.java j1.JavadocSearched.javadocSearchedVar [javadocSearchedVar] EXACT_MATCH",
					result);
		} finally {
			resetProjectOptions();
		}
	}

	/*
	 * Test search of method declarations in javadoc comments
	 * ======================================================
	 */
	public void testJavadocMethodDeclaration() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		IMethod method = type.getMethod("javadocSearchedMethod", null);
		JavaSearchResultCollector result = new JavaSearchResultCollector();
		result.showAccuracy = true;
		new SearchEngine().search(
				getWorkspace(), 
				method,
				DECLARATIONS, 
				getJavaSearchScope(), 
				result
				);
		assertSearchResults(
				"src/j1/JavadocSearched.java void j1.JavadocSearched.javadocSearchedMethod() [javadocSearchedMethod] EXACT_MATCH",
				result);
	}
	public void testJavadocMethodArgDeclaration() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		IMethod method = type.getMethod("javadocSearchedMethod", new String[] { "QString;" });
		JavaSearchResultCollector result = new JavaSearchResultCollector();
		result.showAccuracy = true;
		new SearchEngine().search(
				getWorkspace(), 
				method,
				DECLARATIONS, 
				getJavaSearchScope(), 
				result
				);
		assertSearchResults(
				"src/j1/JavadocSearched.java void j1.JavadocSearched.javadocSearchedMethod(String) [javadocSearchedMethod] EXACT_MATCH",
				result);
	}
	public void testJavadocMethodStringDeclaration() throws CoreException {
		JavaSearchResultCollector result = new JavaSearchResultCollector();
		result.showAccuracy = true;
		new SearchEngine().search(
				getWorkspace(), 
				"javadocSearchedMethod",
				METHOD,
				DECLARATIONS, 
				getJavaSearchScope(), 
				result
				);
		assertSearchResults(
				"src/j1/JavadocSearched.java void j1.JavadocSearched.javadocSearchedMethod() [javadocSearchedMethod] EXACT_MATCH\n" + 
				"src/j1/JavadocSearched.java void j1.JavadocSearched.javadocSearchedMethod(String) [javadocSearchedMethod] EXACT_MATCH",
				result);
	}
	public void testJavadocMethodDeclarationWithJavadoc() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		try {
			setJavadocOptions();
			IMethod method = type.getMethod("javadocSearchedMethod", null);
			JavaSearchResultCollector result = new JavaSearchResultCollector();
			result.showAccuracy = true;
			new SearchEngine().search(
					getWorkspace(), 
					method,
					DECLARATIONS, 
					getJavaSearchScope(), 
					result
					);
			assertSearchResults(
					"src/j1/JavadocSearched.java void j1.JavadocSearched.javadocSearchedMethod() [javadocSearchedMethod] EXACT_MATCH",
					result);
		} finally {
			resetProjectOptions();
		}
	}
	public void testJavadocMethodArgDeclarationWithJavadoc() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		try {
			setJavadocOptions();
			IMethod method = type.getMethod("javadocSearchedMethod", new String[] { "QString;" });
			JavaSearchResultCollector result = new JavaSearchResultCollector();
			result.showAccuracy = true;
			new SearchEngine().search(
					getWorkspace(), 
					method,
					DECLARATIONS, 
					getJavaSearchScope(), 
					result
					);
			assertSearchResults(
					"src/j1/JavadocSearched.java void j1.JavadocSearched.javadocSearchedMethod(String) [javadocSearchedMethod] EXACT_MATCH",
					result);
		} finally {
			resetProjectOptions();
		}
	}

	/*
	 * Test search of type references in javadoc comments
	 * ==================================================
	 */
	public void testJavadocTypeReference() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		JavaSearchResultCollector result = new JavaSearchResultCollector();
		result.showAccuracy = true;
		new SearchEngine().search(
				getWorkspace(), 
				type,
				REFERENCES, 
				getJavaSearchScope(), 
				result
				);
		assertSearchResults(
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [j1.JavadocSearched] EXACT_MATCH\n" + 
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH\n" + 
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH\n" + 
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH\n" + 
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH\n" + 
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH\n"+
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH\n"+
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [j1.JavadocSearched] EXACT_MATCH\n"+
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH\n"+
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH\n"+
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH",
			result);
	}
	public void testJavadocTypeStringReference() throws CoreException {
		JavaSearchResultCollector result = new JavaSearchResultCollector();
		result.showAccuracy = true;
		new SearchEngine().search(
				getWorkspace(), 
				"JavadocSearched",
				TYPE,
				REFERENCES, 
				getJavaSearchScope(), 
				result
				);
		assertSearchResults(
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH\n" + 
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH\n" + 
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH\n" + 
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH\n" + 
			"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH\n" + 
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH\n"+
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH\n"+
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH\n"+
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH\n"+
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH\n"+
			"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH",
			result);
	}
	public void testJavadocTypeReferenceWithJavadoc() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		try {
			setJavadocOptions();
			JavaSearchResultCollector result = new JavaSearchResultCollector();
			result.showAccuracy = true;
			new SearchEngine().search(
					getWorkspace(), 
					type,
					REFERENCES, 
					getJavaSearchScope(), 
					result
					);
			assertSearchResults(
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [j1.JavadocSearched] EXACT_MATCH\n" + 
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH\n" + 
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH\n" + 
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH\n" + 
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH\n" + 
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH\n"+
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH\n"+
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [j1.JavadocSearched] EXACT_MATCH\n"+
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH\n"+
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH\n"+
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH",
				result);
		} finally {
			resetProjectOptions();
		}
	}
	public void testJavadocTypeStringReferenceWithJavadoc() throws CoreException {
		try {
			setJavadocOptions();
			JavaSearchResultCollector result = new JavaSearchResultCollector();
			result.showAccuracy = true;
			new SearchEngine().search(
					getWorkspace(), 
					"JavadocSearched",
					TYPE,
					REFERENCES, 
					getJavaSearchScope(), 
					result
					);
			assertSearchResults(
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH\n" + 
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH\n" + 
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH\n" + 
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH\n" + 
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH\n" + 
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH\n"+
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH\n"+
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH\n"+
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH\n"+
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH\n"+
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH",
				result);
		} finally {
			resetProjectOptions();
		}
	}

	/*
	 * Test search of field references in javadoc comments
	 * ===================================================
	 */
	public void testJavadocFieldReference() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		IField field = type.getField("javadocSearchedVar");
		JavaSearchResultCollector result = new JavaSearchResultCollector();
		result.showAccuracy = true;
		new SearchEngine().search(
				getWorkspace(), 
				field,
				REFERENCES, 
				getJavaSearchScope(), 
				result
				);
		assertSearchResults(
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [javadocSearchedVar] POTENTIAL_MATCH\n" + 
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [javadocSearchedVar] EXACT_MATCH",
				result);
	}
	public void testJavadocFieldStringReference() throws CoreException {
		JavaSearchResultCollector result = new JavaSearchResultCollector();
		result.showAccuracy = true;
		new SearchEngine().search(
				getWorkspace(), 
				"javadocSearchedVar",
				FIELD,
				REFERENCES, 
				getJavaSearchScope(), 
				result
				);
		assertSearchResults(
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [javadocSearchedVar] EXACT_MATCH\n" + 
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [javadocSearchedVar] POTENTIAL_MATCH\n" + 
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [javadocSearchedVar] EXACT_MATCH",
				result);
	}
	public void testJavadocFieldReferenceWithJavadoc() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		IField field = type.getField("javadocSearchedVar");
		try {
			setJavadocOptions();
			JavaSearchResultCollector result = new JavaSearchResultCollector();
			result.showAccuracy = true;
			new SearchEngine().search(
					getWorkspace(), 
					field,
					REFERENCES, 
					getJavaSearchScope(), 
					result
					);
			assertSearchResults(
					"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [javadocSearchedVar] POTENTIAL_MATCH\n" + 
					"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [javadocSearchedVar] EXACT_MATCH",
					result);
		} finally {
			resetProjectOptions();
		}
	}
	public void testJavadocFieldStringReferenceWithJavadoc() throws CoreException {
		try {
			setJavadocOptions();
			JavaSearchResultCollector result = new JavaSearchResultCollector();
			result.showAccuracy = true;
			new SearchEngine().search(
					getWorkspace(), 
					"javadocSearchedVar",
					FIELD,
					REFERENCES, 
					getJavaSearchScope(), 
					result
					);
			assertSearchResults(
					"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [javadocSearchedVar] EXACT_MATCH\n" + 
					"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [javadocSearchedVar] POTENTIAL_MATCH\n" + 
					"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [javadocSearchedVar] EXACT_MATCH",
					result);
		} finally {
			resetProjectOptions();
		}
	}

	/*
	 * Test search of method references in javadoc comments
	 * ====================================================
	 */
	public void testJavadocMethodReference() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		IMethod method = type.getMethod("javadocSearchedMethod", null);
		JavaSearchResultCollector result = new JavaSearchResultCollector();
		result.showAccuracy = true;
		new SearchEngine().search(
				getWorkspace(), 
				method,
				REFERENCES, 
				getJavaSearchScope(), 
				result
				);
		assertSearchResults(
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [javadocSearchedMethod] POTENTIAL_MATCH\n" + 
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [javadocSearchedMethod] EXACT_MATCH",
				result);
	}
	public void testJavadocMethodArgReference() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		IMethod method = type.getMethod("javadocSearchedMethod", new String[] { "QString;" });
		JavaSearchResultCollector result = new JavaSearchResultCollector();
		result.showAccuracy = true;
		new SearchEngine().search(
				getWorkspace(), 
				method,
				REFERENCES, 
				getJavaSearchScope(), 
				result
				);
		assertSearchResults(
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [javadocSearchedMethod] EXACT_MATCH",
				result);
	}
	public void testJavadocMethodStringReference() throws CoreException {
		JavaSearchResultCollector result = new JavaSearchResultCollector();
		result.showAccuracy = true;
		new SearchEngine().search(
				getWorkspace(), 
				"javadocSearchedMethod",
				METHOD,
				REFERENCES, 
				getJavaSearchScope(), 
				result
				);
		assertSearchResults(
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [javadocSearchedMethod] EXACT_MATCH\n" + 
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [javadocSearchedMethod] EXACT_MATCH\n" + 
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [javadocSearchedMethod] EXACT_MATCH\n" + 
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [javadocSearchedMethod] EXACT_MATCH",
				result);
	}
	public void testJavadocMethodReferenceWithJavadoc() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		try {
			setJavadocOptions();
			IMethod method = type.getMethod("javadocSearchedMethod", null);
			JavaSearchResultCollector result = new JavaSearchResultCollector();
			result.showAccuracy = true;
			new SearchEngine().search(
					getWorkspace(), 
					method,
					REFERENCES, 
					getJavaSearchScope(), 
					result
					);
			assertSearchResults(
					"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [javadocSearchedMethod] POTENTIAL_MATCH\n" + 
					"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [javadocSearchedMethod] EXACT_MATCH",
					result);
		} finally {
			resetProjectOptions();
		}
	}
	public void testJavadocMethodArgReferenceWithJavadoc() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		try {
			setJavadocOptions();
			IMethod method = type.getMethod("javadocSearchedMethod", new String[] { "QString;" });
			JavaSearchResultCollector result = new JavaSearchResultCollector();
			result.showAccuracy = true;
			new SearchEngine().search(
					getWorkspace(), 
					method,
					REFERENCES, 
					getJavaSearchScope(), 
					result
					);
			assertSearchResults(
					"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [javadocSearchedMethod] EXACT_MATCH",
					result);
		} finally {
			resetProjectOptions();
		}
	}
	public void testJavadocMethodStringReferenceWithJavadoc() throws CoreException {
		try {
			setJavadocOptions();
			JavaSearchResultCollector result = new JavaSearchResultCollector();
			result.showAccuracy = true;
			new SearchEngine().search(
					getWorkspace(), 
					"javadocSearchedMethod",
					METHOD,
					REFERENCES, 
					getJavaSearchScope(), 
					result
					);
			assertSearchResults(
					"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [javadocSearchedMethod] EXACT_MATCH\n" + 
					"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [javadocSearchedMethod] EXACT_MATCH\n" + 
					"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [javadocSearchedMethod] EXACT_MATCH\n" + 
					"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [javadocSearchedMethod] EXACT_MATCH",
					result);
		} finally {
			resetProjectOptions();
		}
	}

	/*
	 * Test search of constrcutor references in javadoc comments
	 * ====================================================
	 */
	public void testJavadocConstructorReference() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		IMethod method = type.getMethod("JavadocSearched", null);
		JavaSearchResultCollector result = new JavaSearchResultCollector();
		result.showAccuracy = true;
		new SearchEngine().search(
				getWorkspace(), 
				method,
				REFERENCES, 
				getJavaSearchScope(), 
				result
				);
		assertSearchResults(
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH",
				result);
	}
	public void testJavadocConstructorArgReference() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		IMethod method = type.getMethod("JavadocSearched", new String[] { "QString;" });
		JavaSearchResultCollector result = new JavaSearchResultCollector();
		result.showAccuracy = true;
		new SearchEngine().search(
				getWorkspace(), 
				method,
				REFERENCES, 
				getJavaSearchScope(), 
				result
				);
		assertSearchResults(
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH",
				result);
	}
	public void testJavadocConstructorStringReference() throws CoreException {
		JavaSearchResultCollector result = new JavaSearchResultCollector();
		result.showAccuracy = true;
		new SearchEngine().search(
				getWorkspace(), 
				"JavadocSearched",
				CONSTRUCTOR,
				REFERENCES, 
				getJavaSearchScope(), 
				result
				);
		assertSearchResults(
				"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH\n" + 
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH\n" + 
				"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH",
				result);
	}
	public void testJavadocConstructorReferenceWithJavadoc() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		try {
			setJavadocOptions();
			IMethod method = type.getMethod("JavadocSearched", null);
			JavaSearchResultCollector result = new JavaSearchResultCollector();
			result.showAccuracy = true;
			new SearchEngine().search(
					getWorkspace(), 
					method,
					REFERENCES, 
					getJavaSearchScope(), 
					result
					);
			assertSearchResults(
					"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH",
					result);
		} finally {
			resetProjectOptions();
		}
	}
	public void testJavadocConstructorArgReferenceWithJavadoc() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "JavadocSearched.java").getType("JavadocSearched");
		try {
			setJavadocOptions();
			IMethod method = type.getMethod("JavadocSearched", new String[] { "QString;" });
			JavaSearchResultCollector result = new JavaSearchResultCollector();
			result.showAccuracy = true;
			new SearchEngine().search(
					getWorkspace(), 
					method,
					REFERENCES, 
					getJavaSearchScope(), 
					result
					);
			assertSearchResults(
					"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH",
					result);
		} finally {
			resetProjectOptions();
		}
	}
	public void testJavadocConstructorStringReferenceWithJavadoc() throws CoreException {
		try {
			setJavadocOptions();
			JavaSearchResultCollector result = new JavaSearchResultCollector();
			result.showAccuracy = true;
			new SearchEngine().search(
					getWorkspace(), 
					"JavadocSearched",
					CONSTRUCTOR,
					REFERENCES, 
					getJavaSearchScope(), 
					result
					);
			assertSearchResults(
					"src/j1/JavadocInvalidRef.java void j1.JavadocInvalidRef.invalid() [JavadocSearched] EXACT_MATCH\n" + 
					"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH\n" + 
					"src/j1/JavadocValidRef.java void j1.JavadocValidRef.valid() [JavadocSearched] EXACT_MATCH",
					result);
		} finally {
			resetProjectOptions();
		}
	}
}
