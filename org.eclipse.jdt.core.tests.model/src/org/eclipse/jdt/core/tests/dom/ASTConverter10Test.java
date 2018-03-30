/*******************************************************************************
 * Copyright (c) 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.dom;

import junit.framework.Test;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;

public class ASTConverter10Test extends ConverterTestSetup {

	ICompilationUnit workingCopy;
//	private static final String jcl9lib = "CONVERTER_JCL9_LIB";
	

	public void setUpSuite() throws Exception {
		super.setUpSuite();
		this.ast = AST.newAST(AST_INTERNAL_JLS10);
	}

	public ASTConverter10Test(String name) {
		super(name);
	}

	static {
//		TESTS_NUMBERS = new int[] { 19 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] {"testBug532421_002"};
	}
	public static Test suite() {
		String javaVersion = System.getProperty("java.version");
		if (javaVersion.length() > 3) {
			javaVersion = javaVersion.substring(0, 3);
		}
		long jdkLevel = CompilerOptions.versionToJdkLevel(javaVersion);
		if (jdkLevel >= ClassFileConstants.JDK9) {
			isJRE9 = true;
		}
		return buildModelTestSuite(ASTConverter10Test.class);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		if (this.workingCopy != null) {
			this.workingCopy.discardWorkingCopy();
			this.workingCopy = null;
		}
	}

	public void testBug527558_001() throws JavaModelException {
		String contents =
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		var x = new X();\n" +
				"       for (var i = 0; i < 10; ++i) {}\n" +
				"	}\n" +
				"}";
			this.workingCopy = getWorkingCopy("/Converter10/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(contents, this.workingCopy); 
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			node = getASTNode((CompilationUnit)node, 0, 0);
			MethodDeclaration methodDeclaration = (MethodDeclaration) node;
			VariableDeclarationStatement vStmt = (VariableDeclarationStatement) methodDeclaration.getBody().statements().get(0);
			checkSourceRange(vStmt, "var x = new X();", contents);
			Type type = vStmt.getType();
			assertNotNull(type);
			assertTrue("not a var", type.isVar());
			IBinding binding = type.resolveBinding();
			assertTrue("null binding", binding != null);
	}
	public void testBug527558_002() throws JavaModelException {
		String contents =
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		var i = y -> 1;\n" +
				"	}\n" +
				"}\n" +
				"interface I {\n" +
				"	public int foo(int i);\n" +
				"}\n";
			this.workingCopy = getWorkingCopy("/Converter10/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(contents, this.workingCopy, false);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			node = getASTNode((CompilationUnit)node, 0, 0);
			MethodDeclaration methodDeclaration = (MethodDeclaration) node;
			VariableDeclarationStatement vStmt = (VariableDeclarationStatement) methodDeclaration.getBody().statements().get(0);
			checkSourceRange(vStmt, "var i = y -> 1;", contents);
			Type type = vStmt.getType();
			assertTrue("not a var", type.isVar());
			IBinding binding = type.resolveBinding();
			assertTrue("null binding", binding != null);
	}
	public void testBug532421_001() throws JavaModelException {
		String contents =
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		var arr1 = new String[10];\n" +
				"	}\n" +
				"}";
			this.workingCopy = getWorkingCopy("/Converter10/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(contents, this.workingCopy, false);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			node = getASTNode((CompilationUnit)node, 0, 0);
			MethodDeclaration methodDeclaration = (MethodDeclaration) node;
			VariableDeclarationStatement vStmt = (VariableDeclarationStatement) methodDeclaration.getBody().statements().get(0);
			checkSourceRange(vStmt, "var arr1 = new String[10];", contents);
			Type type = vStmt.getType();
			assertTrue("not a var", type.isVar());
			IBinding binding = type.resolveBinding();
			assertTrue("null binding", binding != null);
			assertTrue("binding incorrect", binding.getName().equals("String[]"));
	}
	public void testBug532421_002() throws JavaModelException {
		String contents =
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		var list = new Y<String>();\n" + 
				"	}\n" +
				"}\n" +
				"class Y<T> {}";
			this.workingCopy = getWorkingCopy("/Converter10/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(contents, this.workingCopy, false);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			node = getASTNode((CompilationUnit)node, 0, 0);
			MethodDeclaration methodDeclaration = (MethodDeclaration) node;
			VariableDeclarationStatement vStmt = (VariableDeclarationStatement) methodDeclaration.getBody().statements().get(0);
			checkSourceRange(vStmt, "var list = new Y<String>();", contents);
			Type type = vStmt.getType();
			assertTrue("not a var", type.isVar());
			IBinding binding = type.resolveBinding();
			assertTrue("null binding", binding != null);
			assertTrue("binding incorrect", binding.getName().equals("Y<String>"));
	}
	public void testBug532535_001() throws JavaModelException {
		String contents =
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		var s = new Y();\n" + 
				"	}\n" +
				"}\n" +
				"class Y {}";
			this.workingCopy = getWorkingCopy("/Converter10/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(contents, this.workingCopy, false);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			node = getASTNode((CompilationUnit)node, 0, 0);
			MethodDeclaration methodDeclaration = (MethodDeclaration) node;
			VariableDeclarationStatement vStmt = (VariableDeclarationStatement) methodDeclaration.getBody().statements().get(0);
			checkSourceRange(vStmt, "var s = new Y();", contents);
			Type type = vStmt.getType();
			IBinding binding = type.resolveBinding();
			assertTrue("null binding", binding != null);
			assertTrue("binding incorrect", binding.getName().equals("Y"));
			assertTrue("not a var", type.isVar());
			SimpleType simpleType = (SimpleType) type;
			Name name = simpleType.getName();
			SimpleName simpleName = (SimpleName) name;
			binding = simpleName.resolveBinding();
			assertTrue(binding instanceof ITypeBinding);
			ITypeBinding typeBinding = (ITypeBinding) binding;
			assertTrue("wrong type binding", typeBinding.getName().equals("Y"));
	}
	public void testBug532535_002() throws JavaModelException {
		String contents =
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"for (var x= 10; x < 20; x++) {\n" + 
				"		// do nothing\n" + 
				"	}\n" +
				"}\n" +
				"class Y {}";
			this.workingCopy = getWorkingCopy("/Converter10/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(contents, this.workingCopy, false);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			node = getASTNode((CompilationUnit)node, 0, 0);
			MethodDeclaration methodDeclaration = (MethodDeclaration) node;
			ForStatement forStmt = (ForStatement) methodDeclaration.getBody().statements().get(0);
			VariableDeclarationExpression expr = (VariableDeclarationExpression) forStmt.initializers().get(0);
			Type type = expr.getType();
			IBinding binding = type.resolveBinding();
			assertTrue("null binding", binding != null);
			assertTrue("binding incorrect", binding.getName().equals("int"));
			assertTrue("not a var", type.isVar());
			SimpleType simpleType = (SimpleType) type;
			Name name = simpleType.getName();
			SimpleName simpleName = (SimpleName) name;
			binding = simpleName.resolveBinding();
			assertTrue(binding instanceof ITypeBinding);
			ITypeBinding typeBinding = (ITypeBinding) binding;
			assertTrue("wrong type binding", typeBinding.getName().equals("int"));
	}
// Add new tests here
}
