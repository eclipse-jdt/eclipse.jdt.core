/*******************************************************************************
 * Copyright (c) 2018, 2019 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.dom;

import junit.framework.Test;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;

public class ASTConverter11Test extends ConverterTestSetup {

	ICompilationUnit workingCopy;


	@SuppressWarnings("deprecation")
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		this.ast = AST.newAST(AST_INTERNAL_JLS11, true);
	}

	public ASTConverter11Test(String name) {
		super(name);
	}

	static {
//		TESTS_NUMBERS = new int[] { 19 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] {"testBug535249_001"};
	}
	public static Test suite() {
		return buildModelTestSuite(ASTConverter11Test.class);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
   		if (this.workingCopy != null) {
			this.workingCopy.discardWorkingCopy();
			this.workingCopy = null;
		}
	}

	public void testBug535249_001() throws JavaModelException {
		String contents =
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		I lambda = (var x) -> {System.out.println(x);};\n" +
				"       lambda.apply(10);\n" +
				"	}\n" +
				"}\n" +
				"interface I {\n" +
				"	public void apply(Integer k);\n" +
				"}";
			this.workingCopy = getWorkingCopy("/Converter11/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(contents, this.workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			node = getASTNode((CompilationUnit)node, 0, 0);
			MethodDeclaration methodDeclaration = (MethodDeclaration) node;
			VariableDeclarationStatement vStmt = (VariableDeclarationStatement) methodDeclaration.getBody().statements().get(0);
			checkSourceRange(vStmt, "I lambda = (var x) -> {System.out.println(x);};", contents);
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) vStmt.fragments().get(0);
			LambdaExpression lambda = (LambdaExpression) fragment.getInitializer();
			SingleVariableDeclaration varDecl = (SingleVariableDeclaration) lambda.parameters().get(0);
			Type type = varDecl.getType();
			assertTrue("Not SimpleType", type instanceof SimpleType);
			SimpleType simpleType = (SimpleType) type;
			assertTrue("not a var", simpleType.isVar());
			ITypeBinding binding = simpleType.resolveBinding();
			assertTrue("null binding", binding != null);
			SimpleName simpleName = (SimpleName) simpleType.getName();
			binding = simpleName.resolveTypeBinding();
			assertTrue("null binding", binding != null);
	}

	public void testBug535249_002() throws JavaModelException {
		String contents =
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		I lambda = (var x, var y) -> {System.out.println(x);};\n" +
				"       lambda.apply(10, 20);\n" +
				"	}\n" +
				"}\n" +
				"interface I {\n" +
				"	public void apply(Integer k, Integer l);\n" +
				"}";
			this.workingCopy = getWorkingCopy("/Converter11/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(contents, this.workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			node = getASTNode((CompilationUnit)node, 0, 0);
			MethodDeclaration methodDeclaration = (MethodDeclaration) node;
			VariableDeclarationStatement vStmt = (VariableDeclarationStatement) methodDeclaration.getBody().statements().get(0);
			checkSourceRange(vStmt, "I lambda = (var x, var y) -> {System.out.println(x);};", contents);
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) vStmt.fragments().get(0);
			LambdaExpression lambda = (LambdaExpression) fragment.getInitializer();
			for (int i = 0, l = lambda.parameters().size(); i < l; ++i) {
				SingleVariableDeclaration varDecl = (SingleVariableDeclaration) lambda.parameters().get(i);
				Type type = varDecl.getType();
				assertTrue("Not SimpleType", type instanceof SimpleType);
				SimpleType simpleType = (SimpleType) type;
				assertTrue("not a var", simpleType.isVar());
				ITypeBinding binding = simpleType.resolveBinding();
				assertTrue("null binding", binding != null);
				SimpleName simpleName = (SimpleName) simpleType.getName();
				binding = simpleName.resolveTypeBinding();
				assertTrue("null binding", binding != null);
				assertTrue("Wrong Binding", "Integer".equals(simpleName.resolveBinding().getName()));

			}
	}
// Add new tests here
}