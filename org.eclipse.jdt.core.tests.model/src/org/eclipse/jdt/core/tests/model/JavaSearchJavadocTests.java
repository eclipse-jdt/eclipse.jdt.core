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
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=45518">bug 45518</a>
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
		
		return suite;
	}

	/*
	 * Test search of type declaration in javadoc comments
	 * ===================================================
	 */
	public void testJavadocTypeDeclaration() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "AnnSearched.java").getType("AnnSearched");
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
				"src/j1/AnnSearched.java j1.AnnSearched [AnnSearched] EXACT_MATCH",
				result);
	}
	public void testJavadocTypeStringDeclaration() throws CoreException {
		JavaSearchResultCollector result = new JavaSearchResultCollector();
		result.showAccuracy = true;
		new SearchEngine().search(
				getWorkspace(), 
				"AnnSearched",
				TYPE,
				DECLARATIONS, 
				getJavaSearchScope(), 
				result
				);
		assertSearchResults(
				"src/j1/AnnSearched.java j1.AnnSearched [AnnSearched] EXACT_MATCH",
				result);
	}
	public void testJavadocTypeDeclarationWithJavadoc() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "AnnSearched.java").getType("AnnSearched");
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
					"src/j1/AnnSearched.java j1.AnnSearched [AnnSearched] EXACT_MATCH",
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
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "AnnSearched.java").getType("AnnSearched");
		IField field = type.getField("annSearchedVar");
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
				"src/j1/AnnSearched.java j1.AnnSearched.annSearchedVar [annSearchedVar] EXACT_MATCH",
				result);
	}
	public void testJavadocFieldStringDeclaration() throws CoreException {
		JavaSearchResultCollector result = new JavaSearchResultCollector();
		result.showAccuracy = true;
		new SearchEngine().search(
				getWorkspace(), 
				"annSearchedVar",
				FIELD,
				DECLARATIONS, 
				getJavaSearchScope(), 
				result
				);
		assertSearchResults(
				"src/j1/AnnSearched.java j1.AnnSearched.annSearchedVar [annSearchedVar] EXACT_MATCH",
				result);
	}
	public void testJavadocFieldDeclarationWithJavadoc() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "AnnSearched.java").getType("AnnSearched");
		IField field = type.getField("annSearchedVar");
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
					"src/j1/AnnSearched.java j1.AnnSearched.annSearchedVar [annSearchedVar] EXACT_MATCH",
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
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "AnnSearched.java").getType("AnnSearched");
		IMethod method = type.getMethod("annSearchedMethod", null);
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
				"src/j1/AnnSearched.java void j1.AnnSearched.annSearchedMethod() [annSearchedMethod] EXACT_MATCH",
				result);
	}
	public void testJavadocMethodArgDeclaration() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "AnnSearched.java").getType("AnnSearched");
		IMethod method = type.getMethod("annSearchedMethod", new String[] { "QString;" });
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
				"src/j1/AnnSearched.java void j1.AnnSearched.annSearchedMethod(String) [annSearchedMethod] EXACT_MATCH",
				result);
	}
	public void testJavadocMethodStringDeclaration() throws CoreException {
		JavaSearchResultCollector result = new JavaSearchResultCollector();
		result.showAccuracy = true;
		new SearchEngine().search(
				getWorkspace(), 
				"annSearchedMethod",
				METHOD,
				DECLARATIONS, 
				getJavaSearchScope(), 
				result
				);
		assertSearchResults(
				"src/j1/AnnSearched.java void j1.AnnSearched.annSearchedMethod() [annSearchedMethod] EXACT_MATCH\n" + 
				"src/j1/AnnSearched.java void j1.AnnSearched.annSearchedMethod(String) [annSearchedMethod] EXACT_MATCH",
				result);
	}
	public void testJavadocMethodDeclarationWithJavadoc() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "AnnSearched.java").getType("AnnSearched");
		try {
			setJavadocOptions();
			IMethod method = type.getMethod("annSearchedMethod", null);
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
					"src/j1/AnnSearched.java void j1.AnnSearched.annSearchedMethod() [annSearchedMethod] EXACT_MATCH",
					result);
		} finally {
			resetProjectOptions();
		}
	}
	public void testJavadocMethodArgDeclarationWithJavadoc() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "AnnSearched.java").getType("AnnSearched");
		try {
			setJavadocOptions();
			IMethod method = type.getMethod("annSearchedMethod", new String[] { "QString;" });
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
					"src/j1/AnnSearched.java void j1.AnnSearched.annSearchedMethod(String) [annSearchedMethod] EXACT_MATCH",
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
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "AnnSearched.java").getType("AnnSearched");
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
				"src/j1/AnnValidRef.java void j1.AnnValidRef.valid() [AnnSearched] EXACT_MATCH",
				result);
	}
	public void testJavadocTypeStringReference() throws CoreException {
		JavaSearchResultCollector result = new JavaSearchResultCollector();
		result.showAccuracy = true;
		new SearchEngine().search(
				getWorkspace(), 
				"AnnSearched",
				TYPE,
				REFERENCES, 
				getJavaSearchScope(), 
				result
				);
		assertSearchResults(
				"src/j1/AnnValidRef.java void j1.AnnValidRef.valid() [AnnSearched] EXACT_MATCH",
				result);
	}
	public void testJavadocTypeReferenceWithJavadoc() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "AnnSearched.java").getType("AnnSearched");
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
					"src/j1/AnnValidRef.java void j1.AnnValidRef.valid() [AnnSearched] EXACT_MATCH",
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
					"AnnSearched",
					TYPE,
					REFERENCES, 
					getJavaSearchScope(), 
					result
					);
			assertSearchResults(
					"src/j1/AnnValidRef.java void j1.AnnValidRef.valid() [AnnSearched] EXACT_MATCH",
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
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "AnnSearched.java").getType("AnnSearched");
		IField field = type.getField("annSearchedVar");
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
				"src/j1/AnnInvalidRef.java void j1.AnnInvalidRef.invalid() [annSearchedVar] POTENTIAL_MATCH\n" + 
				"src/j1/AnnValidRef.java void j1.AnnValidRef.valid() [annSearchedVar] EXACT_MATCH",
				result);
	}
	public void testJavadocFieldStringReference() throws CoreException {
		JavaSearchResultCollector result = new JavaSearchResultCollector();
		result.showAccuracy = true;
		new SearchEngine().search(
				getWorkspace(), 
				"annSearchedVar",
				FIELD,
				REFERENCES, 
				getJavaSearchScope(), 
				result
				);
		assertSearchResults(
				"src/j1/AnnInvalidRef.java void j1.AnnInvalidRef.invalid() [annSearchedVar] EXACT_MATCH\n" + 
				"src/j1/AnnInvalidRef.java void j1.AnnInvalidRef.invalid() [annSearchedVar] POTENTIAL_MATCH\n" + 
				"src/j1/AnnValidRef.java void j1.AnnValidRef.valid() [annSearchedVar] EXACT_MATCH",
				result);
	}
	public void testJavadocFieldReferenceWithJavadoc() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "AnnSearched.java").getType("AnnSearched");
		IField field = type.getField("annSearchedVar");
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
					"src/j1/AnnInvalidRef.java void j1.AnnInvalidRef.invalid() [annSearchedVar] POTENTIAL_MATCH\n" + 
					"src/j1/AnnValidRef.java void j1.AnnValidRef.valid() [annSearchedVar] EXACT_MATCH",
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
					"annSearchedVar",
					FIELD,
					REFERENCES, 
					getJavaSearchScope(), 
					result
					);
			assertSearchResults(
					"src/j1/AnnInvalidRef.java void j1.AnnInvalidRef.invalid() [annSearchedVar] EXACT_MATCH\n" + 
					"src/j1/AnnInvalidRef.java void j1.AnnInvalidRef.invalid() [annSearchedVar] POTENTIAL_MATCH\n" + 
					"src/j1/AnnValidRef.java void j1.AnnValidRef.valid() [annSearchedVar] EXACT_MATCH",
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
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "AnnSearched.java").getType("AnnSearched");
		IMethod method = type.getMethod("annSearchedMethod", null);
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
				"src/j1/AnnInvalidRef.java void j1.AnnInvalidRef.invalid() [annSearchedMethod] POTENTIAL_MATCH\n" + 
				"src/j1/AnnValidRef.java void j1.AnnValidRef.valid() [annSearchedMethod] EXACT_MATCH",
				result);
	}
	public void testJavadocMethodArgReference() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "AnnSearched.java").getType("AnnSearched");
		IMethod method = type.getMethod("annSearchedMethod", new String[] { "QString;" });
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
				"src/j1/AnnValidRef.java void j1.AnnValidRef.valid() [annSearchedMethod] EXACT_MATCH",
				result);
	}
	public void testJavadocMethodStringReference() throws CoreException {
		JavaSearchResultCollector result = new JavaSearchResultCollector();
		result.showAccuracy = true;
		new SearchEngine().search(
				getWorkspace(), 
				"annSearchedMethod",
				METHOD,
				REFERENCES, 
				getJavaSearchScope(), 
				result
				);
		assertSearchResults(
				"src/j1/AnnInvalidRef.java void j1.AnnInvalidRef.invalid() [annSearchedMethod] EXACT_MATCH\n" + 
				"src/j1/AnnInvalidRef.java void j1.AnnInvalidRef.invalid() [annSearchedMethod] EXACT_MATCH\n" + 
				"src/j1/AnnValidRef.java void j1.AnnValidRef.valid() [annSearchedMethod] EXACT_MATCH\n" + 
				"src/j1/AnnValidRef.java void j1.AnnValidRef.valid() [annSearchedMethod] EXACT_MATCH",
				result);
	}
	public void testJavadocMethodReferenceWithJavadoc() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "AnnSearched.java").getType("AnnSearched");
		try {
			setJavadocOptions();
			IMethod method = type.getMethod("annSearchedMethod", null);
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
					"src/j1/AnnInvalidRef.java void j1.AnnInvalidRef.invalid() [annSearchedMethod] POTENTIAL_MATCH\n" + 
					"src/j1/AnnValidRef.java void j1.AnnValidRef.valid() [annSearchedMethod] EXACT_MATCH",
					result);
		} finally {
			resetProjectOptions();
		}
	}
	public void testJavadocMethodArgReferenceWithJavadoc() throws CoreException {
		IType type = getCompilationUnit("JavaSearch", "src", "j1", "AnnSearched.java").getType("AnnSearched");
		try {
			setJavadocOptions();
			IMethod method = type.getMethod("annSearchedMethod", new String[] { "QString;" });
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
					"src/j1/AnnValidRef.java void j1.AnnValidRef.valid() [annSearchedMethod] EXACT_MATCH",
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
					"annSearchedMethod",
					METHOD,
					REFERENCES, 
					getJavaSearchScope(), 
					result
					);
			assertSearchResults(
					"src/j1/AnnInvalidRef.java void j1.AnnInvalidRef.invalid() [annSearchedMethod] EXACT_MATCH\n" + 
					"src/j1/AnnInvalidRef.java void j1.AnnInvalidRef.invalid() [annSearchedMethod] EXACT_MATCH\n" + 
					"src/j1/AnnValidRef.java void j1.AnnValidRef.valid() [annSearchedMethod] EXACT_MATCH\n" + 
					"src/j1/AnnValidRef.java void j1.AnnValidRef.valid() [annSearchedMethod] EXACT_MATCH",
					result);
		} finally {
			resetProjectOptions();
		}
	}
}
