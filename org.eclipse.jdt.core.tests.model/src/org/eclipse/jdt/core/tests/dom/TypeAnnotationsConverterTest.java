/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.dom;

import junit.framework.Test;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;

public class TypeAnnotationsConverterTest extends ConverterTestSetup {

	ICompilationUnit workingCopy;

	public void setUpSuite() throws Exception {
		super.setUpSuite();
		this.ast = AST.newAST(AST_INTERNAL_JLS10);
	}

	public TypeAnnotationsConverterTest(String name) {
		super(name);
	}

	static {
//		TESTS_NUMBERS = new int[] { 9 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] {"test0001"};
	}
	public static Test suite() {
		return buildModelTestSuite(TypeAnnotationsConverterTest.class);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		if (this.workingCopy != null) {
			this.workingCopy.discardWorkingCopy();
			this.workingCopy = null;
		}
	}

	// Test QualifiedTypeReference
	public void test0001() throws JavaModelException {
		String contents =
			"public class X {\n" +
		    "    class Y {\n" +
			"        class Z {\n" +
		    "        }\n" +
			"    }\n" +
			"    Object o = (@Marker X. @Marker Y.@Marker Z) null;\n" +
			
			"    @java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
			"    @interface Marker {\n" +
			"    }\n" +
			"}\n";
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		String expectedOutput = 
				"public class X {\n" + 
				"class Y {\n" + 
				"class Z {\n" + 
				"    }\n" + 
				"  }\n" + 
				"  Object o=(@Marker X.@Marker Y.@Marker Z)null;\n" + 
				"  @java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE_USE) @interface Marker {}\n" + 
				"}\n";
		assertASTNodeEquals(expectedOutput, node);
	}
	// Test ParameterizedQualifiedTypeReference
	public void test0002() throws JavaModelException {
		String contents =
			"public class X {\n" +
		    "    class Y {\n" +
			"        class Z<T> {\n" +
		    "        }\n" +
			"    }\n" +
			"    Object o = (@Marker X. @Marker Y.@Marker Z<String>) null;\n" +
			
			"    @java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
			"    @interface Marker {\n" +
			"    }\n" +
			"}\n";
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		String expectedOutput = 
				"public class X {\n" + 
				"class Y {\n" + 
				"class Z<T> {\n" + 
				"    }\n" + 
				"  }\n" + 
				"  Object o=(@Marker X.@Marker Y.@Marker Z<String>)null;\n" + 
				"  @java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE_USE) @interface Marker {}\n" + 
				"}\n";
		assertASTNodeEquals(expectedOutput, node);
	}
	// Test ParameterizedQualifiedReference
	public void test0003() throws JavaModelException {
		String contents =
			"public class X<T> {\n" +
		    "    class Y<R> {\n" +
			"        class Z<Q> {\n" +
		    "        }\n" +
			"    }\n" +
			"    Object o = (@Marker X<String>. @Marker Y<Integer>.@Marker Z<Object>) null;\n" +
			
			"    @java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
			"    @interface Marker {\n" +
			"    }\n" +
			"}\n";
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		String expectedOutput = 
				"public class X<T> {\n" + 
				"class Y<R> {\n" + 
				"class Z<Q> {\n" + 
				"    }\n" + 
				"  }\n" + 
				"  Object o=(@Marker X<String>.@Marker Y<Integer>.@Marker Z<Object>)null;\n" + 
				"  @java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE_USE) @interface Marker {}\n" + 
				"}\n";
		assertASTNodeEquals(expectedOutput, node);
	}
	// Test ParameterizedQualifiedReference with arrays.
	public void test0004() throws JavaModelException {
		String contents =
			"public class X<T> {\n" +
		    "    class Y<R> {\n" +
			"        class Z<Q> {\n" +
		    "        }\n" +
			"    }\n" +
			"    Object o = (@Marker X<@Marker String>. @Marker Y<@Marker Integer>.@Marker Z<@Marker Object> @Marker [] [] @Marker [] []) null;\n" +
			
			"    @java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
			"    @interface Marker {\n" +
			"    }\n" +
			"}\n";
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		String expectedOutput = 
				"public class X<T> {\n" + 
				"class Y<R> {\n" + 
				"class Z<Q> {\n" + 
				"    }\n" + 
				"  }\n" + 
				"  Object o=(@Marker X<@Marker String>.@Marker Y<@Marker Integer>.@Marker Z<@Marker Object> @Marker [][] @Marker [][])null;\n" + 
				"  @java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE_USE) @interface Marker {}\n" + 
				"}\n";
		assertASTNodeEquals(expectedOutput, node);
	}
	// Test ParameterizedQualifiedReference with arrays.
	public void test0005() throws JavaModelException {
		String contents =
			"public class X<T> {\n" +
		    "    class Y<R> {\n" +
			"        class Z<Q> {\n" +
		    "        }\n" +
			"    }\n" +
			"    Object o = (@Marker X<@Marker String>. @Marker Y<@Marker Integer>.@Marker Z<@Marker Object> [] @Marker [] [] @Marker []) null;\n" +
			
			"    @java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
			"    @interface Marker {\n" +
			"    }\n" +
			"}\n";
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		String expectedOutput = 
				"public class X<T> {\n" + 
				"class Y<R> {\n" + 
				"class Z<Q> {\n" + 
				"    }\n" + 
				"  }\n" + 
				"  Object o=(@Marker X<@Marker String>.@Marker Y<@Marker Integer>.@Marker Z<@Marker Object>[] @Marker [][] @Marker [])null;\n" + 
				"  @java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE_USE) @interface Marker {}\n" + 
				"}\n";
		assertASTNodeEquals(expectedOutput, node);
	}
	// Test PrimitiveType with arrays
	public void test0006() throws JavaModelException {
		String contents =
			"public class X<T> {\n" +
		    "    class Y<R> {\n" +
			"        class Z<Q> {\n" +
		    "        }\n" +
			"    }\n" +
			"    int[][][][] o = (@One int[] @Two [][] @Three []) null;\n" +
			
			"    @java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
			"    @interface Marker {\n" +
			"    }\n" +
			"}\n";
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy, false);
		String expectedOutput = 
				"public class X<T> {\n" + 
				"class Y<R> {\n" + 
				"class Z<Q> {\n" + 
				"    }\n" + 
				"  }\n" + 
				"  int[][][][] o=(@One int[] @Two [][] @Three [])null;\n" + 
				"  @java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE_USE) @interface Marker {}\n" + 
				"}\n";
		assertASTNodeEquals(expectedOutput, node);
	}
	// Test SingleTypeReference with arrays.
	public void test0007() throws JavaModelException {
		String contents =
			"public class X<T> {\n" +
		    "    class Y<R> {\n" +
			"        class Z<Q> {\n" +
		    "        }\n" +
			"    }\n" +
			"    String [][][][] o = (@One String[]@Two [][]@Three []) null;\n" +
			
			"    @java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
			"    @interface Marker {\n" +
			"    }\n" +
			"}\n";
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy, false);
		String expectedOutput = 
				"public class X<T> {\n" + 
				"class Y<R> {\n" + 
				"class Z<Q> {\n" + 
				"    }\n" + 
				"  }\n" + 
				"  String[][][][] o=(@One String[] @Two [][] @Three [])null;\n" + 
				"  @java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE_USE) @interface Marker {}\n" + 
				"}\n";
		assertASTNodeEquals(expectedOutput, node);
	}
	// Test ParameterizedSingleTypeReference with arrays.
	public void test0008() throws JavaModelException {
		String contents =
			"public class X<T> {\n" +
		    "    class Y<R> {\n" +
			"        class Z<Q> {\n" +
		    "        }\n" +
			"    }\n" +
			"    Object o = (@One X<String> [] @Two [][]@Three []) null;\n" +
			
			"    @java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
			"    @interface Marker {\n" +
			"    }\n" +
			"}\n";
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy, false);
		String expectedOutput = 
				"public class X<T> {\n" + 
				"class Y<R> {\n" + 
				"class Z<Q> {\n" + 
				"    }\n" + 
				"  }\n" + 
				"  Object o=(@One X<String>[] @Two [][] @Three [])null;\n" + 
				"  @java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE_USE) @interface Marker {}\n" + 
				"}\n";
		assertASTNodeEquals(expectedOutput, node);
	}
	// Test type parameters.
	public void test0009() throws JavaModelException {
		String contents =
			"public class X<@NonNull T> {\n" +
		    "    class Y<@Nullable R> {\n" +
			"        class Z<@Readonly Q> {\n" +
		    "        }\n" +
			"    }\n" +
			"}\n";
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy, false);
		String expectedOutput = 
				"public class X<@NonNull T> {\n" + 
				"class Y<@Nullable R> {\n" + 
				"class Z<@Readonly Q> {\n" + 
				"    }\n" + 
				"  }\n" + 
				"}\n";
		assertASTNodeEquals(expectedOutput, node);
	}
	// Test wildcard types.
	public void test0010() throws JavaModelException {
		String contents =
			"public class X<@NonNull T> {\n" +
		    "    X<@NonNull ? extends @Nullable String> x;\n" +
			"}\n";
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy, false);
		String expectedOutput = 
				"public class X<@NonNull T> {\n" + 
				"  X<@NonNull ? extends @Nullable String> x;\n" + 
				"}\n";
		assertASTNodeEquals(expectedOutput, node);
	}
	// Test union types.
	public void test0011() throws JavaModelException {
		String contents =
			"public class X<@NonNull T> {\n" +
		    "    void foo() {\n" +
		    "        try {\n" +
		    "        } catch (@NonNull NullPointerException | @Nullable ArrayIndexOutOfBoundsException e) {\n" +
		    "        }\n" +
		    "    }\n" +
			"}\n";
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy, false);
		String expectedOutput = 
				"public class X<@NonNull T> {\n" + 
				"  void foo(){\n" + 
				"    try {\n" + 
				"    }\n" + 
				" catch (    @NonNull NullPointerException|@Nullable ArrayIndexOutOfBoundsException e) {\n" + 
				"    }\n" + 
				"  }\n" + 
				"}\n";
		assertASTNodeEquals(expectedOutput, node);
	}
	// Test thrown types.
	public void test0012() throws JavaModelException {
		String contents =
			"public class X<@NonNull T> {\n" +
		    "    void foo() throws @NonNull NullPointerException, @Nullable ArrayIndexOutOfBoundsException {\n" +
		    "        try {\n" +
		    "        } catch (@NonNull NullPointerException | @Nullable ArrayIndexOutOfBoundsException e) {\n" +
		    "        }\n" +
		    "    }\n" +
			"}\n";
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy, false);
		String expectedOutput = 
				"public class X<@NonNull T> {\n" + 
				"  void foo() throws @NonNull NullPointerException, @Nullable ArrayIndexOutOfBoundsException {\n" + 
				"    try {\n" + 
				"    }\n" + 
				" catch (    @NonNull NullPointerException|@Nullable ArrayIndexOutOfBoundsException e) {\n" + 
				"    }\n" + 
				"  }\n" + 
				"}\n";
		assertASTNodeEquals(expectedOutput, node);
	}
}
