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
package org.eclipse.jdt.core.tests.dom;

import java.io.IOException;

import junit.framework.Test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Type;

public class ASTConverterBugsTest extends ConverterTestSetup {
	
	public void setUpSuite() throws Exception {
		PROJECT_SETUP = true; // do not copy Converter* directories
		super.setUpSuite();
		setUpJCLClasspathVariables("1.4");
		waitUntilIndexesReady();
	}

	public ASTConverterBugsTest(String name) {
		super(name);
	}

	public static Test suite() {
		return buildModelTestSuite(ASTConverterBugsTest.class);
	}
	
	/**
	 * @bug 186410: [dom] StackOverflowError due to endless superclass bindings hierarchy
	 * @test Ensures that the superclass of "java.lang.Object" class is null even when it's a recovered binding
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=186410"
	 */
	public void testBug186410() throws CoreException, IOException {
		try {
			createJavaProject("P", new String[] {""}, new String[0], "");
			createFile("P/A.java",
				"public class A {\n" +
				"	void method(){}\n" +
				"}"
			);
			ICompilationUnit cuA = getCompilationUnit("P/A.java");
			CompilationUnit unitA = (CompilationUnit) runConversion(AST.JLS3, cuA, true, false, true);
			AbstractTypeDeclaration typeA = (AbstractTypeDeclaration)unitA.types().get(0);
			ITypeBinding objectType = typeA.resolveBinding().getSuperclass();
			assertEquals("Unexpected superclass", "Object", objectType .getName());
			ITypeBinding objectSuperclass = objectType.getSuperclass();
			assertNull("java.lang.Object should  not have any superclass", objectSuperclass);
		} finally {
			deleteProject("P");
		}
	}
	public void testBug186410b() throws CoreException, IOException {
		try {
			createJavaProject("P", new String[] {""}, new String[0], "");
			createFile("P/A.java",
				"public class A {\n" +
				"	Object field;\n" +
				"}"
			);
			ICompilationUnit cuA = getCompilationUnit("P/A.java");
			CompilationUnit unitA = (CompilationUnit) runConversion(AST.JLS3, cuA, true, false, true);
			AbstractTypeDeclaration type = (AbstractTypeDeclaration)unitA.types().get(0);
			FieldDeclaration field = (FieldDeclaration) type.bodyDeclarations().get(0);
			Type fieldType = field.getType();
			ITypeBinding typeBinding = fieldType.resolveBinding();
			ITypeBinding objectType = typeBinding.createArrayType(2).getElementType();
			assertEquals("Unexpected superclass", "Object", objectType.getName());
			ITypeBinding objectSuperclass = objectType.getSuperclass();
			assertNull("java.lang.Object should  not have any superclass", objectSuperclass);
		} finally {
			deleteProject("P");
		}
	}
	
	/**
	 * @bug 209150: [dom] Recovered type binding for "java.lang.Object" information are not complete
	 * @test Ensures that getPackage() and getQualifiedName() works properly for the "java.lang.Object" recovered binding
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=209150"
	 */
	public void testBug209150a() throws CoreException, IOException {
		try {
			createJavaProject("P", new String[] {""}, new String[0], "");
			createFile("P/A.java",
				"public class A {\n" +
				"	void method(){}\n" +
				"}"
			);
			ICompilationUnit cuA = getCompilationUnit("P/A.java");
			CompilationUnit unitA = (CompilationUnit) runConversion(AST.JLS3, cuA, true, false, true);
			AbstractTypeDeclaration typeA = (AbstractTypeDeclaration)unitA.types().get(0);
			ITypeBinding objectType = typeA.resolveBinding().getSuperclass();
			assertTrue("'java.lang.Object' should be recovered!", objectType.isRecovered());
			assertEquals("Unexpected package for recovered 'java.lang.Object'", "java.lang", objectType .getPackage().getName());
			assertEquals("Unexpected qualified name for recovered 'java.lang.Object'", "java.lang.Object", objectType .getQualifiedName());
		} finally {
			deleteProject("P");
		}
	}
	public void testBug209150b() throws CoreException, IOException {
		try {
			createJavaProject("P", new String[] {""}, new String[0], "");
			createFile("P/A.java",
				"public class A {\n" +
				"	Object field;\n" +
				"}"
			);
			ICompilationUnit cuA = getCompilationUnit("P/A.java");
			CompilationUnit unitA = (CompilationUnit) runConversion(AST.JLS3, cuA, true, false, true);
			AbstractTypeDeclaration type = (AbstractTypeDeclaration)unitA.types().get(0);
			FieldDeclaration field = (FieldDeclaration) type.bodyDeclarations().get(0);
			Type fieldType = field.getType();
			ITypeBinding typeBinding = fieldType.resolveBinding();
			ITypeBinding arrayType = typeBinding.createArrayType(2);
			assertTrue("'java.lang.Object' should be recovered!", arrayType.isRecovered());
			assertNull("Unexpected package for recovered 'array of java.lang.Object'", arrayType .getPackage());
			assertEquals("Unexpected qualified name for recovered 'java.lang.Object'", "java.lang.Object[][]", arrayType .getQualifiedName());
		} finally {
			deleteProject("P");
		}
	}
	public void testBug209150c() throws CoreException, IOException {
		try {
			createJavaProject("P", new String[] {""}, new String[0], "");
			createFile("P/A.java",
				"public class A {\n" +
				"	Object[] array;\n" +
				"}"
			);
			ICompilationUnit cuA = getCompilationUnit("P/A.java");
			CompilationUnit unitA = (CompilationUnit) runConversion(AST.JLS3, cuA, true, false, true);
			AbstractTypeDeclaration type = (AbstractTypeDeclaration)unitA.types().get(0);
			FieldDeclaration field = (FieldDeclaration) type.bodyDeclarations().get(0);
			Type fieldType = field.getType();
			ITypeBinding arrayType = fieldType.resolveBinding();
			assertTrue("'java.lang.Object' should be recovered!", arrayType.isRecovered());
			assertNull("Unexpected package for recovered 'array of java.lang.Object'", arrayType .getPackage());
			assertEquals("Unexpected qualified name for recovered 'java.lang.Object'", "java.lang.Object[]", arrayType .getQualifiedName());
		} finally {
			deleteProject("P");
		}
	}
	
}
