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
public class JavaSearchAnnotationTests extends JavaSearchTests {

	Map originalOptions;

	/**
	 * @param name
	 */
	public JavaSearchAnnotationTests(String name) {
		super(name);
	}
	private void resetProjectOptions() {
		this.javaProject.setOptions(originalOptions);
	}
	private void setJavadocOptions() {
		this.originalOptions = this.javaProject.getOptions(true);
		this.javaProject.setOption(JavaCore.COMPILER_PB_INVALID_ANNOTATION, JavaCore.WARNING);
		this.javaProject.setOption(JavaCore.COMPILER_PB_MISSING_ANNOTATION, JavaCore.ENABLED);
	}
	public static Test suite() {
		// NOTE: cannot use 'new Suite(JavaSearchAnnotationTests.class)' as this would include tests from super class
		TestSuite suite = new Suite(JavaSearchAnnotationTests.class.getName());
		
		// Tests on type declarations
		suite.addTest(new JavaSearchAnnotationTests("testAnnotationTypeDeclaration"));
		suite.addTest(new JavaSearchAnnotationTests("testAnnotationTypeDeclarationWithJavadoc"));
		suite.addTest(new JavaSearchAnnotationTests("testAnnotationTypeStringDeclaration"));
		
		// Tests on field declarations
		suite.addTest(new JavaSearchAnnotationTests("testAnnotationFieldDeclaration"));
		suite.addTest(new JavaSearchAnnotationTests("testAnnotationFieldDeclarationWithJavadoc"));
		suite.addTest(new JavaSearchAnnotationTests("testAnnotationFieldStringDeclaration"));
		
		// Tests on method declarations
		suite.addTest(new JavaSearchAnnotationTests("testAnnotationMethodDeclaration"));
		suite.addTest(new JavaSearchAnnotationTests("testAnnotationMethodArgDeclaration"));
		suite.addTest(new JavaSearchAnnotationTests("testAnnotationMethodDeclarationWithJavadoc"));
		suite.addTest(new JavaSearchAnnotationTests("testAnnotationMethodArgDeclarationWithJavadoc"));
		suite.addTest(new JavaSearchAnnotationTests("testAnnotationMethodStringDeclaration"));
		
		// Tests on type references
		suite.addTest(new JavaSearchAnnotationTests("testAnnotationTypeReference"));
		suite.addTest(new JavaSearchAnnotationTests("testAnnotationTypeReferenceWithJavadoc"));
		suite.addTest(new JavaSearchAnnotationTests("testAnnotationTypeStringReference"));
		suite.addTest(new JavaSearchAnnotationTests("testAnnotationTypeStringReferenceWithJavadoc"));
		
		// Tests on field references
		suite.addTest(new JavaSearchAnnotationTests("testAnnotationFieldReference"));
		suite.addTest(new JavaSearchAnnotationTests("testAnnotationFieldReferenceWithJavadoc"));
		suite.addTest(new JavaSearchAnnotationTests("testAnnotationFieldStringReference"));
		suite.addTest(new JavaSearchAnnotationTests("testAnnotationFieldStringReferenceWithJavadoc"));
		
		// Tests on method references
		suite.addTest(new JavaSearchAnnotationTests("testAnnotationMethodReference"));
		suite.addTest(new JavaSearchAnnotationTests("testAnnotationMethodArgReference"));
		suite.addTest(new JavaSearchAnnotationTests("testAnnotationMethodReferenceWithJavadoc"));
		suite.addTest(new JavaSearchAnnotationTests("testAnnotationMethodArgReferenceWithJavadoc"));
		suite.addTest(new JavaSearchAnnotationTests("testAnnotationMethodStringReference"));
		suite.addTest(new JavaSearchAnnotationTests("testAnnotationMethodStringReferenceWithJavadoc"));
		
		return suite;
	}

	/*
	 * Test search of type declaration in annotations
	 * ==============================================
	 */
	public void testAnnotationTypeDeclaration() throws CoreException {
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
	public void testAnnotationTypeStringDeclaration() throws CoreException {
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
	public void testAnnotationTypeDeclarationWithJavadoc() throws CoreException {
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
	 * Test search of field declaration in annotations
	 * ===============================================
	 */
	public void testAnnotationFieldDeclaration() throws CoreException {
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
	public void testAnnotationFieldStringDeclaration() throws CoreException {
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
	public void testAnnotationFieldDeclarationWithJavadoc() throws CoreException {
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
	 * Test search of method declarations in annotations
	 * =================================================
	 */
	public void testAnnotationMethodDeclaration() throws CoreException {
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
	public void testAnnotationMethodArgDeclaration() throws CoreException {
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
	public void testAnnotationMethodStringDeclaration() throws CoreException {
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
	public void testAnnotationMethodDeclarationWithJavadoc() throws CoreException {
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
	public void testAnnotationMethodArgDeclarationWithJavadoc() throws CoreException {
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
	 * Test search of type references in annotations
	 * =============================================
	 */
	public void testAnnotationTypeReference() throws CoreException {
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
	public void testAnnotationTypeStringReference() throws CoreException {
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
	public void testAnnotationTypeReferenceWithJavadoc() throws CoreException {
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
	public void testAnnotationTypeStringReferenceWithJavadoc() throws CoreException {
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
	 * Test search of field references in annotations
	 * ==============================================
	 */
	public void testAnnotationFieldReference() throws CoreException {
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
	public void testAnnotationFieldStringReference() throws CoreException {
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
	public void testAnnotationFieldReferenceWithJavadoc() throws CoreException {
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
	public void testAnnotationFieldStringReferenceWithJavadoc() throws CoreException {
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
	 * Test search of method references in annotations
	 * ===============================================
	 */
	public void testAnnotationMethodReference() throws CoreException {
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
	public void testAnnotationMethodArgReference() throws CoreException {
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
	public void testAnnotationMethodStringReference() throws CoreException {
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
	public void testAnnotationMethodReferenceWithJavadoc() throws CoreException {
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
	public void testAnnotationMethodArgReferenceWithJavadoc() throws CoreException {
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
	public void testAnnotationMethodStringReferenceWithJavadoc() throws CoreException {
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
