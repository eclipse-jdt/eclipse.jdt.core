/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
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

import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;

@SuppressWarnings({"rawtypes"})
public class ASTConverter9Test extends ConverterTestSetup {

	ICompilationUnit workingCopy;

	public void setUpSuite() throws Exception {
		super.setUpSuite();
		this.ast = AST.newAST(AST.JLS9);
	}

	public ASTConverter9Test(String name) {
		super(name);
	}

	static {
//		TESTS_NUMBERS = new int[] { 19 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] {"testBug497719_0001"};
	}
	public static Test suite() {
		return buildModelTestSuite(ASTConverter9Test.class);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		if (this.workingCopy != null) {
			this.workingCopy.discardWorkingCopy();
			this.workingCopy = null;
		}
	}

	public void testBug497719_0001() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter9" , "src", "testBug497719_001", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(this.ast.apiLevel(), sourceUnit, true, true);
		char[] source = sourceUnit.getSource().toCharArray();
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0, 0);
		assertEquals("Not a compilation unit", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		TryStatement tryStatement = (TryStatement) methodDeclaration.getBody().statements().get(1);
		List list = tryStatement.resources();
		VariableDeclarationExpression variableDeclarationExpression = (VariableDeclarationExpression) list.get(0);
		checkSourceRange(variableDeclarationExpression, "final Y y = new Y()", source);
		SimpleName simpleName = (SimpleName) list.get(1);
		checkSourceRange(simpleName, "y1;", source); // TODO: semicolon should not be part of source range
		variableDeclarationExpression = (VariableDeclarationExpression) list.get(2);
		checkSourceRange(variableDeclarationExpression, "final Y y2 = new Y()", source);
		
	}
	
	public void testBug497719_0002() throws JavaModelException {
		String contents =
				"import java.io.IOException;\n" +
				"\n" +
				"class Z {\n" +
				"	 final Y yz = new Y();\n" +
				"}\n" +
				"public class X extends Z {\n" +
				"	final  Y y2 = new Y();\n" +
				"	\n" +
				"	 Y bar() {\n" +
				"		 return new Y();\n" +
				"	 }\n" +
				"	public void foo() {\n" +
				"		Y y3 = new Y();\n" +
				"		int a[];\n" +
				"		try (y3; y3;super.yz;super.yz;this.y2;Y y4 = new Y())  {  \n" +
				"			System.out.println(\"In Try\");\n" +
				"		} catch (IOException e) {			  \n" +
				"		} \n" +
				"	}\n" +
				"	public static void main(String[] args) {\n" +
				"		new X().foo();\n" +
				"	}\n" +
				"}\n" +
				"class Y implements AutoCloseable {\n" +
				"	@Override\n" +
				"	public void close() throws IOException {\n" +
				"		System.out.println(\"Closed\");\n" +
				"	}  \n" +
				"}";
			this.workingCopy = getWorkingCopy("/Converter9/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(contents, this.workingCopy, false);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			node = getASTNode((CompilationUnit)node, 1, 2);
			MethodDeclaration methodDeclaration = (MethodDeclaration) node;
			TryStatement tryStatement = (TryStatement)methodDeclaration.getBody().statements().get(2);
			List<Expression> resources = tryStatement.resources();
			Expression expr = resources.get(0);
			SimpleName simpleName = (SimpleName) expr;
			checkSourceRange(simpleName, "y3;", contents);
			expr = resources.get(1);
			simpleName = (SimpleName) expr;
			checkSourceRange(expr, "y3;", contents);
			expr = resources.get(2);
			SuperFieldAccess superFieldAccess = (SuperFieldAccess) expr;
			checkSourceRange(superFieldAccess, "super.yz;", contents);
			expr = resources.get(3);
			superFieldAccess = (SuperFieldAccess) expr;
			checkSourceRange(superFieldAccess, "super.yz;", contents);
			expr = resources.get(4);
			FieldAccess fieldAccess = (FieldAccess) expr;
			checkSourceRange(fieldAccess, "this.y2;", contents);
			expr = resources.get(5);
			VariableDeclarationExpression variableDeclarationExpression = (VariableDeclarationExpression) expr;
			checkSourceRange(variableDeclarationExpression, "Y y4 = new Y()", contents);			
	}

}
