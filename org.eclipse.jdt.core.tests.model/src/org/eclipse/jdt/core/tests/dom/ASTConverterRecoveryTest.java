/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.dom;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.internal.core.JavaModelManager;

public class ASTConverterRecoveryTest extends ConverterTestSetup {
	private Map oldOptions;
	
	public ASTConverterRecoveryTest(String name) {
		super(name);
	}

	static {
//		TESTS_NAMES = new String[] {"test0003"};
//		TESTS_NUMBERS =  new int[] { 624 };
	}
	public static Test suite() {
		return buildTestSuite(ASTConverterRecoveryTest.class);
	}
	
	private IJavaProject getConverterProject() {
		return JavaModelManager.getJavaModelManager().getJavaModel().getJavaProject("Converter");
	}
	
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		this.ast = AST.newAST(AST.JLS3);
		
		IJavaProject project = getConverterProject();
		oldOptions = project.getOptions(false);
		Hashtable newOptions = new Hashtable(oldOptions);
		newOptions.put(JavaCore.COMPILER_STATEMENTS_RECOVERY, JavaCore.ENABLED);
		project.setOptions(newOptions);
	}

	public void tearDownSuite() throws Exception {
		IJavaProject project = getConverterProject();
		project.setOptions(oldOptions);
		super.tearDownSuite();
	}
	
	public void test0001() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
			"/Converter/src/test/X.java",
			"package test;\n"+
			"\n"+
			"public class X {\n"+
			"	void foo() {\n"+
			"	    bar(0)\n"+
			"	    baz(1);\n"+
			"	}\n"+
			"}\n");
		
		char[] source = this.workingCopies[0].getSource().toCharArray();
		ASTNode result = runConversion(AST.JLS3, this.workingCopies[0], true, true);
		
		assertASTNodeEquals(
			"package test;\n" + 
			"public class X {\n" + 
			"  void foo(){\n" + 
			"    bar(0);\n" + 
			"    baz(1);\n" + 
			"  }\n" + 
			"}\n",
			result);
		
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull(node);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		Block block = methodDeclaration.getBody();
		List statements = block.statements();
		assertEquals("wrong size", 2, statements.size()); //$NON-NLS-1$
		Statement statement = (Statement) statements.get(0);
		assertTrue("Not an expression statement", statement.getNodeType() == ASTNode.EXPRESSION_STATEMENT); //$NON-NLS-1$
		ExpressionStatement expressionStatement = (ExpressionStatement) statement;
		checkSourceRange(expressionStatement, "bar(0)", source); //$NON-NLS-1$
		Expression expression = expressionStatement.getExpression();
		assertTrue("Not a method invocation", expression.getNodeType() == ASTNode.METHOD_INVOCATION); //$NON-NLS-1$
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		checkSourceRange(methodInvocation, "bar(0)", source); //$NON-NLS-1$
		List list = methodInvocation.arguments();
		assertTrue("Parameter list is empty", list.size() == 1); //$NON-NLS-1$
		Expression parameter = (Expression) list.get(0);
		assertTrue("Not a number", parameter instanceof NumberLiteral); //$NON-NLS-1$
		ITypeBinding typeBinding = parameter.resolveTypeBinding();
		assertNotNull("No binding", typeBinding); //$NON-NLS-1$
		assertEquals("Not int", "int", typeBinding.getName()); //$NON-NLS-1$ //$NON-NLS-2$
		checkSourceRange(parameter, "0", source); //$NON-NLS-1$
		Statement statement2 = (Statement) statements.get(1);
		assertTrue("Not an expression statement", statement2.getNodeType() == ASTNode.EXPRESSION_STATEMENT); //$NON-NLS-1$
		ExpressionStatement expressionStatement2 = (ExpressionStatement) statement2;
		checkSourceRange(expressionStatement2, "baz(1);", source); //$NON-NLS-1$
		Expression expression2 = expressionStatement2.getExpression();
		assertTrue("Not a method invocation", expression2.getNodeType() == ASTNode.METHOD_INVOCATION); //$NON-NLS-1$
		MethodInvocation methodInvocation2 = (MethodInvocation) expression2;
		checkSourceRange(methodInvocation2, "baz(1)", source); //$NON-NLS-1$
		List list2 = methodInvocation2.arguments();
		assertTrue("Parameter list is empty", list2.size() == 1); //$NON-NLS-1$
		Expression parameter2 = (Expression) list2.get(0);
		assertTrue("Not a number", parameter2 instanceof NumberLiteral); //$NON-NLS-1$
		ITypeBinding typeBinding2 = parameter2.resolveTypeBinding();
		assertNotNull("No binding", typeBinding2); //$NON-NLS-1$
		assertEquals("Not int", "int", typeBinding2.getName()); //$NON-NLS-1$ //$NON-NLS-2$
		checkSourceRange(parameter2, "1", source); //$NON-NLS-1$
	}
	
	public void test0002() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
			"/Converter/src/test/X.java",
			"package test;\n"+
			"\n"+
			"public class X {\n"+
			"	void foo() {\n"+
			"	    baz(0);\n"+
			"	    bar(1,\n"+
			"	}\n"+
			"}\n");
		
		char[] source = this.workingCopies[0].getSource().toCharArray();
		ASTNode result = runConversion(AST.JLS3, this.workingCopies[0], true, true);
		
		assertASTNodeEquals(
			"package test;\n" + 
			"public class X {\n" + 
			"  void foo(){\n" + 
			"    baz(0);\n" + 
			"    bar(1);\n" + 
			"  }\n" + 
			"}\n",
			result);
		
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull(node);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		Block block = methodDeclaration.getBody();
		List statements = block.statements();
		assertEquals("wrong size", 2, statements.size()); //$NON-NLS-1$
		Statement statement = (Statement) statements.get(0);
		assertTrue("Not an expression statement", statement.getNodeType() == ASTNode.EXPRESSION_STATEMENT); //$NON-NLS-1$
		ExpressionStatement expressionStatement = (ExpressionStatement) statement;
		checkSourceRange(expressionStatement, "baz(0);", source); //$NON-NLS-1$
		Expression expression = expressionStatement.getExpression();
		assertTrue("Not a method invocation", expression.getNodeType() == ASTNode.METHOD_INVOCATION); //$NON-NLS-1$
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		checkSourceRange(methodInvocation, "baz(0)", source); //$NON-NLS-1$
		List list = methodInvocation.arguments();
		assertTrue("Parameter list is empty", list.size() == 1); //$NON-NLS-1$
		Expression parameter = (Expression) list.get(0);
		assertTrue("Not a number", parameter instanceof NumberLiteral); //$NON-NLS-1$
		ITypeBinding typeBinding = parameter.resolveTypeBinding();
		assertNotNull("No binding", typeBinding); //$NON-NLS-1$
		assertEquals("Not int", "int", typeBinding.getName()); //$NON-NLS-1$ //$NON-NLS-2$
		checkSourceRange(parameter, "0", source); //$NON-NLS-1$
		Statement statement2 = (Statement) statements.get(1);
		assertTrue("Not an expression statement", statement2.getNodeType() == ASTNode.EXPRESSION_STATEMENT); //$NON-NLS-1$
		ExpressionStatement expressionStatement2 = (ExpressionStatement) statement2;
		checkSourceRange(expressionStatement2, "bar(1", source); //$NON-NLS-1$
		Expression expression2 = expressionStatement2.getExpression();
		assertTrue("Not a method invocation", expression2.getNodeType() == ASTNode.METHOD_INVOCATION); //$NON-NLS-1$
		MethodInvocation methodInvocation2 = (MethodInvocation) expression2;
		checkSourceRange(methodInvocation2, "bar(1", source); //$NON-NLS-1$
		List list2 = methodInvocation2.arguments();
		assertTrue("Parameter list is empty", list2.size() == 1); //$NON-NLS-1$
		Expression parameter2 = (Expression) list2.get(0);
		assertTrue("Not a number", parameter2 instanceof NumberLiteral); //$NON-NLS-1$
		ITypeBinding typeBinding2 = parameter2.resolveTypeBinding();
		assertNotNull("No binding", typeBinding2); //$NON-NLS-1$
		assertEquals("Not int", "int", typeBinding2.getName()); //$NON-NLS-1$ //$NON-NLS-2$
		checkSourceRange(parameter2, "1", source); //$NON-NLS-1$
	}
	
	public void test0003() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
			"/Converter/src/test/X.java",
			"package test;\n"+
			"\n"+
			"public class X {\n"+
			"	void foo() {\n"+
			"	    baz(0);\n"+
			"	    bar(1,\n"+
			"	    foo(3);\n"+
			"	}\n"+
			"}\n");
		
		char[] source = this.workingCopies[0].getSource().toCharArray();
		ASTNode result = runConversion(AST.JLS3, this.workingCopies[0], true, true);
		
		assertASTNodeEquals(
			"package test;\n" + 
			"public class X {\n" + 
			"  void foo(){\n" + 
			"    baz(0);\n" + 
			"    bar(1,foo(3));\n" + 
			"  }\n" + 
			"}\n",
			result);
		
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull(node);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		Block block = methodDeclaration.getBody();
		List statements = block.statements();
		assertEquals("wrong size", 2, statements.size()); //$NON-NLS-1$
		Statement statement = (Statement) statements.get(0);
		assertTrue("Not an expression statement", statement.getNodeType() == ASTNode.EXPRESSION_STATEMENT); //$NON-NLS-1$
		ExpressionStatement expressionStatement = (ExpressionStatement) statement;
		checkSourceRange(expressionStatement, "baz(0);", source); //$NON-NLS-1$
		Expression expression = expressionStatement.getExpression();
		assertTrue("Not a method invocation", expression.getNodeType() == ASTNode.METHOD_INVOCATION); //$NON-NLS-1$
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		checkSourceRange(methodInvocation, "baz(0)", source); //$NON-NLS-1$
		List list = methodInvocation.arguments();
		assertTrue("Parameter list is empty", list.size() == 1); //$NON-NLS-1$
		Expression parameter = (Expression) list.get(0);
		assertTrue("Not a number", parameter instanceof NumberLiteral); //$NON-NLS-1$
		ITypeBinding typeBinding = parameter.resolveTypeBinding();
		assertNotNull("No binding", typeBinding); //$NON-NLS-1$
		assertEquals("Not int", "int", typeBinding.getName()); //$NON-NLS-1$ //$NON-NLS-2$
		checkSourceRange(parameter, "0", source); //$NON-NLS-1$
		Statement statement2 = (Statement) statements.get(1);
		assertTrue("Not an expression statement", statement2.getNodeType() == ASTNode.EXPRESSION_STATEMENT); //$NON-NLS-1$
		ExpressionStatement expressionStatement2 = (ExpressionStatement) statement2;
		checkSourceRange(expressionStatement2, "bar(1,\n\t    foo(3);", source); //$NON-NLS-1$
		Expression expression2 = expressionStatement2.getExpression();
		assertTrue("Not a method invocation", expression2.getNodeType() == ASTNode.METHOD_INVOCATION); //$NON-NLS-1$
		MethodInvocation methodInvocation2 = (MethodInvocation) expression2;
		checkSourceRange(methodInvocation2, "bar(1,\n\t    foo(3)", source); //$NON-NLS-1$
		List list2 = methodInvocation2.arguments();
		assertTrue("Parameter list is empty", list2.size() == 2); //$NON-NLS-1$
		Expression parameter2 = (Expression) list2.get(0);
		assertTrue("Not a Number", parameter2 instanceof NumberLiteral); //$NON-NLS-1$
		parameter2 = (Expression) list2.get(1);
		assertTrue("Not a method invocation", parameter2 instanceof MethodInvocation); //$NON-NLS-1$
		MethodInvocation methodInvocation3 = (MethodInvocation) parameter2;
		checkSourceRange(methodInvocation3, "foo(3)", source); //$NON-NLS-1$
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=124296
	public void test0004() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
			"/Converter/src/test/X.java",
			"package test;\n"+
			"\n"+
			"public class X {\n"+
			"	void foo() {\n"+
			"	    int var= 123\n"+
			"	    System.out.println(var);\n"+
			"	}\n"+
			"}\n");
		
		char[] source = this.workingCopies[0].getSource().toCharArray();
		ASTNode result = runConversion(AST.JLS3, this.workingCopies[0], true, true);
		
		assertASTNodeEquals(
			"package test;\n" + 
			"public class X {\n" + 
			"  void foo(){\n" + 
			"    int var=123;\n" + 
			"    System.out.println(var);\n" + 
			"  }\n" + 
			"}\n",
			result);
		
		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull(node);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		Block block = methodDeclaration.getBody();
		List statements = block.statements();
		assertEquals("wrong size", 2, statements.size()); //$NON-NLS-1$
		Statement statement1 = (Statement) statements.get(0);
		assertTrue("Not an expression statement", statement1.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT); //$NON-NLS-1$
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) statement1;
		checkSourceRange(variableDeclarationStatement, "int var= 123", source); //$NON-NLS-1$
		List fragments = variableDeclarationStatement.fragments();
		assertEquals("wrong size", 1, fragments.size()); //$NON-NLS-1$
		VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment)fragments.get(0);
		checkSourceRange(variableDeclarationFragment, "var= 123", source); //$NON-NLS-1$
	}
}
