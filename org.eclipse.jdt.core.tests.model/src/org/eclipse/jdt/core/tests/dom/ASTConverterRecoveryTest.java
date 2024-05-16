/*******************************************************************************
 * Copyright (c) 2006, 2019 IBM Corporation and others.
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

import java.util.List;

import junit.framework.Test;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;

@SuppressWarnings("rawtypes")
public class ASTConverterRecoveryTest extends ConverterTestSetup {
	public ASTConverterRecoveryTest(String name) {
		super(name);
	}

	static {
//		TESTS_NAMES = new String[] {"test0003"};
//		TESTS_NUMBERS =  new int[] { 19, 20 };
	}
	public static Test suite() {
		return buildModelTestSuite(ASTConverterRecoveryTest.class);
	}

	@Override
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		this.ast = AST.newAST(getJLS3(), false);
	}

	public void test0001() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
			"/Converter/src/test/X.java",
			"""
				package test;
				
				public class X {
					void foo() {
					    bar(0)
					    baz(1);
					}
				}
				""");

		char[] source = this.workingCopies[0].getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), this.workingCopies[0], true, true);

		assertASTNodeEquals(
			"""
				package test;
				public class X {
				  void foo(){
				    bar(0);
				    baz(1);
				  }
				}
				""",
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
			"""
				package test;
				
				public class X {
					void foo() {
					    baz(0);
					    bar(1,
					}
				}
				""");

		char[] source = this.workingCopies[0].getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), this.workingCopies[0], true, true);

		assertASTNodeEquals(
			"""
				package test;
				public class X {
				  void foo(){
				    baz(0);
				    bar(1);
				  }
				}
				""",
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
			"""
				package test;
				
				public class X {
					void foo() {
					    baz(0);
					    bar(1,
					    foo(3);
					}
				}
				""");

		char[] source = this.workingCopies[0].getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), this.workingCopies[0], true, true);

		assertASTNodeEquals(
			"""
				package test;
				public class X {
				  void foo(){
				    baz(0);
				    bar(1,foo(3));
				  }
				}
				""",
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
			"""
				package test;
				
				public class X {
					void foo() {
					    int var= 123
					    System.out.println(var);
					}
				}
				""");

		char[] source = this.workingCopies[0].getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), this.workingCopies[0], true, true);

		assertASTNodeEquals(
			"""
				package test;
				public class X {
				  void foo(){
				    int var=123;
				    System.out.println(var);
				  }
				}
				""",
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
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=126148
	public void test0005() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
			"/Converter/src/test/X.java",
			"""
				package test;
				
				public class X {
					void foo() {
					    String[] s =  {"",,,};
					}
				}
				""");

		char[] source = this.workingCopies[0].getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), this.workingCopies[0], true, true);

		assertASTNodeEquals(
			"""
				package test;
				public class X {
				  void foo(){
				    String[] s={"",$missing$};
				  }
				}
				""",
			result);

		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull(node);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		Block block = methodDeclaration.getBody();
		List statements = block.statements();
		assertEquals("wrong size", 1, statements.size()); //$NON-NLS-1$
		Statement statement1 = (Statement) statements.get(0);
		assertTrue("Not an expression variable declaration statement", statement1.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT); //$NON-NLS-1$
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) statement1;
		checkSourceRange(variableDeclarationStatement, "String[] s =  {\"\",,,};", source); //$NON-NLS-1$
		List fragments = variableDeclarationStatement.fragments();
		assertEquals("wrong size", 1, fragments.size()); //$NON-NLS-1$
		VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment)fragments.get(0);
		checkSourceRange(variableDeclarationFragment, "s =  {\"\",,,}", source); //$NON-NLS-1$
		Expression expression = variableDeclarationFragment.getInitializer();
		assertTrue("Not an array initializer", expression.getNodeType() == ASTNode.ARRAY_INITIALIZER); //$NON-NLS-1$
		ArrayInitializer arrayInitializer = (ArrayInitializer) expression;
		checkSourceRange(arrayInitializer, "{\"\",,,}", source); //$NON-NLS-1$
		List expressions = arrayInitializer.expressions();
		assertEquals("wrong size", 2, expressions.size()); //$NON-NLS-1$
		Expression expression1 = (Expression) expressions.get(0);
		assertTrue("Not a string literal", expression1.getNodeType() == ASTNode.STRING_LITERAL); //$NON-NLS-1$
		StringLiteral stringLiteral = (StringLiteral) expression1;
		checkSourceRange(stringLiteral, "\"\"", source); //$NON-NLS-1$
		Expression expression2 = (Expression) expressions.get(1);
		assertTrue("Not a string literal", expression2.getNodeType() == ASTNode.SIMPLE_NAME); //$NON-NLS-1$
		SimpleName simpleName = (SimpleName) expression2;
		checkSourceRange(simpleName, ",", source); //$NON-NLS-1$

	}

	// check RECOVERED flag (insert tokens)
	public void test0006() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
			"/Converter/src/test/X.java",
			"""
				package test;
				
				public class X {
					void foo() {
					    bar()
					}
				}
				""");

		char[] source = this.workingCopies[0].getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), this.workingCopies[0], true, true);

		assertASTNodeEquals(
			"""
				package test;
				public class X {
				  void foo(){
				    bar();
				  }
				}
				""",
			result);

		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull(node);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		assertTrue("Flag as RECOVERED", (methodDeclaration.getFlags() & ASTNode.RECOVERED) == 0);
		Block block = methodDeclaration.getBody();
		assertTrue("Flag as RECOVERED", (block.getFlags() & ASTNode.RECOVERED) == 0);
		List statements = block.statements();
		assertEquals("wrong size", 1, statements.size()); //$NON-NLS-1$
		Statement statement = (Statement) statements.get(0);
		assertTrue("Not an expression statement", statement.getNodeType() == ASTNode.EXPRESSION_STATEMENT); //$NON-NLS-1$
		ExpressionStatement expressionStatement = (ExpressionStatement) statement;
		checkSourceRange(expressionStatement, "bar()", source); //$NON-NLS-1$
		assertTrue("Not flag as RECOVERED", (expressionStatement.getFlags() & ASTNode.RECOVERED) != 0);
		Expression expression = expressionStatement.getExpression();
		assertTrue("Not a method invocation", expression.getNodeType() == ASTNode.METHOD_INVOCATION); //$NON-NLS-1$
		MethodInvocation methodInvocation = (MethodInvocation)expression;
		checkSourceRange(methodInvocation, "bar()", source); //$NON-NLS-1$
		assertTrue("Flag as RECOVERED", (methodInvocation.getFlags() & ASTNode.RECOVERED) == 0);
	}

	// check RECOVERED flag (insert tokens)
	public void test0007() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
			"/Converter/src/test/X.java",
			"""
				package test;
				
				public class X {
					void foo() {
					    bar(baz()
					}
				}
				""");

		char[] source = this.workingCopies[0].getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), this.workingCopies[0], true, true);

		assertASTNodeEquals(
			"""
				package test;
				public class X {
				  void foo(){
				    bar(baz());
				  }
				}
				""",
			result);

		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull(node);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		assertTrue("Flag as RECOVERED", (methodDeclaration.getFlags() & ASTNode.RECOVERED) == 0);
		Block block = methodDeclaration.getBody();
		assertTrue("Flag as RECOVERED", (block.getFlags() & ASTNode.RECOVERED) == 0);
		List statements = block.statements();
		assertEquals("wrong size", 1, statements.size()); //$NON-NLS-1$
		Statement statement = (Statement) statements.get(0);
		assertTrue("Not an expression statement", statement.getNodeType() == ASTNode.EXPRESSION_STATEMENT); //$NON-NLS-1$
		ExpressionStatement expressionStatement = (ExpressionStatement) statement;
		checkSourceRange(expressionStatement, "bar(baz()", source); //$NON-NLS-1$
		assertTrue("Not flag as RECOVERED", (expressionStatement.getFlags() & ASTNode.RECOVERED) != 0);
		Expression expression = expressionStatement.getExpression();
		assertTrue("Not a method invocation", expression.getNodeType() == ASTNode.METHOD_INVOCATION); //$NON-NLS-1$
		MethodInvocation methodInvocation = (MethodInvocation)expression;
		checkSourceRange(methodInvocation, "bar(baz()", source); //$NON-NLS-1$
		assertTrue("Not flag as RECOVERED", (methodInvocation.getFlags() & ASTNode.RECOVERED) != 0);
		List arguments = methodInvocation.arguments();
		assertEquals("wrong size", 1, arguments.size()); //$NON-NLS-1$
		Expression argument = (Expression) arguments.get(0);
		assertTrue("Not a method invocation", argument.getNodeType() == ASTNode.METHOD_INVOCATION); //$NON-NLS-1$
		MethodInvocation methodInvocation2 = (MethodInvocation) argument;
		checkSourceRange(methodInvocation2, "baz()", source); //$NON-NLS-1$
		assertTrue("Flag as RECOVERED", (methodInvocation2.getFlags() & ASTNode.RECOVERED) == 0);
	}

	// check RECOVERED flag (insert tokens)
	public void test0008() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
			"/Converter/src/test/X.java",
			"""
				package test;
				
				public class X {
					void foo() {
					    for(int i
					}
				}
				""");

		char[] source = this.workingCopies[0].getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), this.workingCopies[0], true, true);

		assertASTNodeEquals(
			"""
				package test;
				public class X {
				  void foo(){
				    for (int i; ; )     ;
				  }
				}
				""",
			result);

		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull(node);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		assertTrue("Flag as RECOVERED", (methodDeclaration.getFlags() & ASTNode.RECOVERED) == 0);
		Block block = methodDeclaration.getBody();
		assertTrue("Not flag as RECOVERED", (block.getFlags() & ASTNode.RECOVERED) != 0);
		List statements = block.statements();
		assertEquals("wrong size", 1, statements.size()); //$NON-NLS-1$
		Statement statement = (Statement) statements.get(0);
		assertTrue("Not a for statement", statement.getNodeType() == ASTNode.FOR_STATEMENT); //$NON-NLS-1$
		ForStatement forStatement = (ForStatement) statement;
		checkSourceRange(forStatement, "for(int i", source); //$NON-NLS-1$
		assertTrue("Not flag as RECOVERED", (forStatement.getFlags() & ASTNode.RECOVERED) != 0);
		List initializers = forStatement.initializers();
		assertEquals("wrong size", 1, statements.size()); //$NON-NLS-1$
		Expression expression = (Expression)initializers.get(0);
		assertTrue("Not a method invocation", expression.getNodeType() == ASTNode.VARIABLE_DECLARATION_EXPRESSION); //$NON-NLS-1$
		VariableDeclarationExpression variableDeclarationExpression = (VariableDeclarationExpression)expression;
		checkSourceRange(variableDeclarationExpression, "int i", source); //$NON-NLS-1$
		assertTrue("Not flag as RECOVERED", (variableDeclarationExpression.getFlags() & ASTNode.RECOVERED) != 0);
		List fragments = variableDeclarationExpression.fragments();
		assertEquals("wrong size", 1, fragments.size()); //$NON-NLS-1$
		VariableDeclarationFragment fragment = (VariableDeclarationFragment)fragments.get(0);
		checkSourceRange(fragment, "i", source); //$NON-NLS-1$
		assertTrue("Not flag as RECOVERED", (fragment.getFlags() & ASTNode.RECOVERED) != 0);
		SimpleName name = fragment.getName();
		checkSourceRange(name, "i", source); //$NON-NLS-1$
		assertTrue("Flag as RECOVERED", (name.getFlags() & ASTNode.RECOVERED) == 0);
		Statement statement2 = forStatement.getBody();
		assertTrue("Not an empty statement", statement2.getNodeType() == ASTNode.EMPTY_STATEMENT); //$NON-NLS-1$
		EmptyStatement emptyStatement = (EmptyStatement)statement2;
		assertEquals("Wrong start position", fragment.getStartPosition() + fragment.getLength(), emptyStatement.getStartPosition());
		assertEquals("Wrong length", 0, emptyStatement.getLength());
		assertTrue("Not flag as RECOVERED", (emptyStatement.getFlags() & ASTNode.RECOVERED) != 0);
	}

	// check RECOVERED flag (remove tokens)
	public void test0009() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
			"/Converter/src/test/X.java",
			"""
				package test;
				
				public class X {
					void foo() {
					    bar(baz());#
					}
				}
				""");

		char[] source = this.workingCopies[0].getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), this.workingCopies[0], true, true);

		assertASTNodeEquals(
			"""
				package test;
				public class X {
				  void foo(){
				    bar(baz());
				  }
				}
				""",
			result);

		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull(node);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		assertTrue("Flag as RECOVERED", (methodDeclaration.getFlags() & ASTNode.RECOVERED) == 0);
		Block block = methodDeclaration.getBody();
		assertTrue("Not flag as RECOVERED", (block.getFlags() & ASTNode.RECOVERED) != 0);
		List statements = block.statements();
		assertEquals("wrong size", 1, statements.size()); //$NON-NLS-1$
		Statement statement = (Statement) statements.get(0);
		assertTrue("Not an expression statement", statement.getNodeType() == ASTNode.EXPRESSION_STATEMENT); //$NON-NLS-1$
		ExpressionStatement expressionStatement = (ExpressionStatement) statement;
		checkSourceRange(expressionStatement, "bar(baz());", source); //$NON-NLS-1$
		assertTrue("Flag as RECOVERED", (expressionStatement.getFlags() & ASTNode.RECOVERED) == 0);
		Expression expression = expressionStatement.getExpression();
		assertTrue("Not a method invocation", expression.getNodeType() == ASTNode.METHOD_INVOCATION); //$NON-NLS-1$
		MethodInvocation methodInvocation = (MethodInvocation)expression;
		checkSourceRange(methodInvocation, "bar(baz())", source); //$NON-NLS-1$
		assertTrue("Flag as RECOVERED", (methodInvocation.getFlags() & ASTNode.RECOVERED) == 0);
		List arguments = methodInvocation.arguments();
		assertEquals("wrong size", 1, arguments.size()); //$NON-NLS-1$
		Expression argument = (Expression) arguments.get(0);
		assertTrue("Not a method invocation", argument.getNodeType() == ASTNode.METHOD_INVOCATION); //$NON-NLS-1$
		MethodInvocation methodInvocation2 = (MethodInvocation) argument;
		checkSourceRange(methodInvocation2, "baz()", source); //$NON-NLS-1$
		assertTrue("Flag as RECOVERED", (methodInvocation2.getFlags() & ASTNode.RECOVERED) == 0);
	}

	// check RECOVERED flag (remove tokens)
	public void test0010() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
			"/Converter/src/test/X.java",
			"""
				package test;
				
				public class X {
					void foo() {
					    bar(baz())#;
					}
				}
				""");

		char[] source = this.workingCopies[0].getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), this.workingCopies[0], true, true);

		assertASTNodeEquals(
			"""
				package test;
				public class X {
				  void foo(){
				    bar(baz());
				  }
				}
				""",
			result);

		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull(node);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		assertTrue("Flag as RECOVERED", (methodDeclaration.getFlags() & ASTNode.RECOVERED) == 0);
		Block block = methodDeclaration.getBody();
		assertTrue("Flag as RECOVERED", (block.getFlags() & ASTNode.RECOVERED) == 0);
		List statements = block.statements();
		assertEquals("wrong size", 1, statements.size()); //$NON-NLS-1$
		Statement statement = (Statement) statements.get(0);
		assertTrue("Not an expression statement", statement.getNodeType() == ASTNode.EXPRESSION_STATEMENT); //$NON-NLS-1$
		ExpressionStatement expressionStatement = (ExpressionStatement) statement;
		checkSourceRange(expressionStatement, "bar(baz())#;", source); //$NON-NLS-1$
		assertTrue("Not flag as RECOVERED", (expressionStatement.getFlags() & ASTNode.RECOVERED) != 0);
		Expression expression = expressionStatement.getExpression();
		assertTrue("Not a method invocation", expression.getNodeType() == ASTNode.METHOD_INVOCATION); //$NON-NLS-1$
		MethodInvocation methodInvocation = (MethodInvocation)expression;
		checkSourceRange(methodInvocation, "bar(baz())", source); //$NON-NLS-1$
		assertTrue("Flag as RECOVERED", (methodInvocation.getFlags() & ASTNode.RECOVERED) == 0);
		List arguments = methodInvocation.arguments();
		assertEquals("wrong size", 1, arguments.size()); //$NON-NLS-1$
		Expression argument = (Expression) arguments.get(0);
		assertTrue("Not a method invocation", argument.getNodeType() == ASTNode.METHOD_INVOCATION); //$NON-NLS-1$
		MethodInvocation methodInvocation2 = (MethodInvocation) argument;
		checkSourceRange(methodInvocation2, "baz()", source); //$NON-NLS-1$
		assertTrue("Flag as RECOVERED", (methodInvocation2.getFlags() & ASTNode.RECOVERED) == 0);
	}

	// check RECOVERED flag (remove tokens)
	public void test0011() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
			"/Converter/src/test/X.java",
			"""
				package test;
				
				public class X {
					void foo() {
					    bar(baz()#);
					}
				}
				""");

		char[] source = this.workingCopies[0].getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), this.workingCopies[0], true, true);

		assertASTNodeEquals(
			"""
				package test;
				public class X {
				  void foo(){
				    bar(baz());
				  }
				}
				""",
			result);

		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull(node);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		assertTrue("Flag as RECOVERED", (methodDeclaration.getFlags() & ASTNode.RECOVERED) == 0);
		Block block = methodDeclaration.getBody();
		assertTrue("Flag as RECOVERED", (block.getFlags() & ASTNode.RECOVERED) == 0);
		List statements = block.statements();
		assertEquals("wrong size", 1, statements.size()); //$NON-NLS-1$
		Statement statement = (Statement) statements.get(0);
		assertTrue("Not an expression statement", statement.getNodeType() == ASTNode.EXPRESSION_STATEMENT); //$NON-NLS-1$
		ExpressionStatement expressionStatement = (ExpressionStatement) statement;
		checkSourceRange(expressionStatement, "bar(baz()#);", source); //$NON-NLS-1$
		assertTrue("Flag as RECOVERED", (expressionStatement.getFlags() & ASTNode.RECOVERED) == 0);
		Expression expression = expressionStatement.getExpression();
		assertTrue("Not a method invocation", expression.getNodeType() == ASTNode.METHOD_INVOCATION); //$NON-NLS-1$
		MethodInvocation methodInvocation = (MethodInvocation)expression;
		checkSourceRange(methodInvocation, "bar(baz()#)", source); //$NON-NLS-1$
		assertTrue("Not flag as RECOVERED", (methodInvocation.getFlags() & ASTNode.RECOVERED) != 0);
		List arguments = methodInvocation.arguments();
		assertEquals("wrong size", 1, arguments.size()); //$NON-NLS-1$
		Expression argument = (Expression) arguments.get(0);
		assertTrue("Not a method invocation", argument.getNodeType() == ASTNode.METHOD_INVOCATION); //$NON-NLS-1$
		MethodInvocation methodInvocation2 = (MethodInvocation) argument;
		checkSourceRange(methodInvocation2, "baz()", source); //$NON-NLS-1$
		assertTrue("Flag as RECOVERED", (methodInvocation2.getFlags() & ASTNode.RECOVERED) == 0);
	}

	// check RECOVERED flag (insert tokens)
	public void test0012() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
			"/Converter/src/test/X.java",
			"""
				package test;
				
				public class X {
					void foo() {
					    bar()#
					}
				}
				""");

		char[] source = this.workingCopies[0].getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), this.workingCopies[0], true, true);

		assertASTNodeEquals(
			"""
				package test;
				public class X {
				  void foo(){
				    bar();
				  }
				}
				""",
			result);

		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull(node);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		assertTrue("Flag as RECOVERED", (methodDeclaration.getFlags() & ASTNode.RECOVERED) == 0);
		Block block = methodDeclaration.getBody();
		assertTrue("Flag as RECOVERED", (block.getFlags() & ASTNode.RECOVERED) == 0);
		List statements = block.statements();
		assertEquals("wrong size", 1, statements.size()); //$NON-NLS-1$
		Statement statement = (Statement) statements.get(0);
		assertTrue("Not an expression statement", statement.getNodeType() == ASTNode.EXPRESSION_STATEMENT); //$NON-NLS-1$
		ExpressionStatement expressionStatement = (ExpressionStatement) statement;
		checkSourceRange(expressionStatement, "bar()#", source); //$NON-NLS-1$
		assertTrue("Not flag as RECOVERED", (expressionStatement.getFlags() & ASTNode.RECOVERED) != 0);
		Expression expression = expressionStatement.getExpression();
		assertTrue("Not a method invocation", expression.getNodeType() == ASTNode.METHOD_INVOCATION); //$NON-NLS-1$
		MethodInvocation methodInvocation = (MethodInvocation)expression;
		checkSourceRange(methodInvocation, "bar()", source); //$NON-NLS-1$
		assertTrue("Flag as RECOVERED", (methodInvocation.getFlags() & ASTNode.RECOVERED) == 0);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=129555
	public void test0013() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
			"/Converter/src/test/X.java",
			"""
				package test;
				
				public class X {
					void foo() {
					    a[0]
					}
				}
				""");

		char[] source = this.workingCopies[0].getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), this.workingCopies[0], true, true);

		assertASTNodeEquals(
			"""
				package test;
				public class X {
				  void foo(){
				    a[0]=$missing$;
				  }
				}
				""",
			result);

		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull(node);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		assertTrue("Flag as RECOVERED", (methodDeclaration.getFlags() & ASTNode.RECOVERED) == 0);
		Block block = methodDeclaration.getBody();
		assertTrue("Flag as RECOVERED", (block.getFlags() & ASTNode.RECOVERED) != 0);
		List statements = block.statements();
		assertEquals("wrong size", 1, statements.size()); //$NON-NLS-1$
		Statement statement = (Statement) statements.get(0);
		assertTrue("Not an expression statement", statement.getNodeType() == ASTNode.EXPRESSION_STATEMENT); //$NON-NLS-1$
		ExpressionStatement expressionStatement = (ExpressionStatement) statement;
		checkSourceRange(expressionStatement, "a[0]", source); //$NON-NLS-1$
		assertTrue("Not flag as RECOVERED", (expressionStatement.getFlags() & ASTNode.RECOVERED) != 0);
		Expression expression = expressionStatement.getExpression();
		assertTrue("Not an assigment", expression.getNodeType() == ASTNode.ASSIGNMENT); //$NON-NLS-1$
		Assignment assignment = (Assignment)expression;
		checkSourceRange(assignment, "a[0]", source); //$NON-NLS-1$
		assertTrue("Flag as RECOVERED", (assignment.getFlags() & ASTNode.RECOVERED) != 0);
		Expression rhs = assignment.getRightHandSide();
		assertTrue("Not a simple name", rhs.getNodeType() == ASTNode.SIMPLE_NAME); //$NON-NLS-1$
		SimpleName simpleName = (SimpleName) rhs;
		assertEquals("Not length isn't correct", 0, simpleName.getLength()); //$NON-NLS-1$
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=129909
	public void _test0014() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
			"/Converter/src/test/X.java",
			"""
				package test;
				
				public class X {
					void foo() {
					    int[] = a[0];
					}
				}
				""");

		char[] source = this.workingCopies[0].getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), this.workingCopies[0], true, true);

		assertASTNodeEquals(
			"""
				package test;
				public class X {
				  void foo(){
				    int[] $missing$=a[0];
				  }
				}
				""",
			result);

		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull(node);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		assertTrue("Flag as RECOVERED", (methodDeclaration.getFlags() & ASTNode.RECOVERED) == 0);
		Block block = methodDeclaration.getBody();
		assertTrue("Flag as RECOVERED", (block.getFlags() & ASTNode.RECOVERED) == 0);
		List statements = block.statements();
		assertEquals("wrong size", 1, statements.size()); //$NON-NLS-1$
		Statement statement = (Statement) statements.get(0);
		assertTrue("Not a variable declaration statement", statement.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT); //$NON-NLS-1$
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) statement;
		checkSourceRange(variableDeclarationStatement, "int[] = a[0];", source); //$NON-NLS-1$
		assertTrue("Not flag as RECOVERED", (variableDeclarationStatement.getFlags() & ASTNode.RECOVERED) != 0);
		List fragments = variableDeclarationStatement.fragments();
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		SimpleName simpleName = fragment.getName();
		assertEquals("Not length isn't correct", 0, simpleName.getLength()); //$NON-NLS-1$
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=143212
	public void test0015() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
			"/Converter/src/test/X.java",
			"""
				package test;
				
				public class X {
					void foo() {
					    assert 0 == 0 : a[0;
					}
				}
				""");

		char[] source = this.workingCopies[0].getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), this.workingCopies[0], true, true);

		assertASTNodeEquals(
			"""
				package test;
				public class X {
				  void foo(){
				    assert 0 == 0 : a[0];
				  }
				}
				""",
			result);

		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull(node);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		assertTrue("Flag as RECOVERED", (methodDeclaration.getFlags() & ASTNode.RECOVERED) == 0);
		Block block = methodDeclaration.getBody();
		assertTrue("Flag as RECOVERED", (block.getFlags() & ASTNode.RECOVERED) == 0);
		List statements = block.statements();
		assertEquals("wrong size", 1, statements.size()); //$NON-NLS-1$
		Statement statement = (Statement) statements.get(0);
		assertTrue("Not an assert statement", statement.getNodeType() == ASTNode.ASSERT_STATEMENT); //$NON-NLS-1$
		AssertStatement assertStatement = (AssertStatement) statement;
		checkSourceRange(assertStatement, "assert 0 == 0 : a[0;", source); //$NON-NLS-1$
		assertTrue("Flag as RECOVERED", (assertStatement.getFlags() & ASTNode.RECOVERED) == 0);
		Expression message = assertStatement.getMessage();
		assertTrue("No message expression", message != null); //$NON-NLS-1$
		checkSourceRange(message, "a[0", source); //$NON-NLS-1$
		assertTrue("Not flag as RECOVERED", (message.getFlags() & ASTNode.RECOVERED) != 0);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=143212
	public void test0016() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
			"/Converter/src/test/X.java",
			"""
				package test;
				
				public class X {
					void foo() {
					    assert 0 == 0 : foo(;
					}
				}
				""");

		char[] source = this.workingCopies[0].getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), this.workingCopies[0], true, true);

		assertASTNodeEquals(
			"""
				package test;
				public class X {
				  void foo(){
				    assert 0 == 0 : foo();
				  }
				}
				""",
			result);

		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull(node);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		assertTrue("Flag as RECOVERED", (methodDeclaration.getFlags() & ASTNode.RECOVERED) == 0);
		Block block = methodDeclaration.getBody();
		assertTrue("Flag as RECOVERED", (block.getFlags() & ASTNode.RECOVERED) == 0);
		List statements = block.statements();
		assertEquals("wrong size", 1, statements.size()); //$NON-NLS-1$
		Statement statement = (Statement) statements.get(0);
		assertTrue("Not an assert statement", statement.getNodeType() == ASTNode.ASSERT_STATEMENT); //$NON-NLS-1$
		AssertStatement assertStatement = (AssertStatement) statement;
		checkSourceRange(assertStatement, "assert 0 == 0 : foo(;", source); //$NON-NLS-1$
		assertTrue("Flag as RECOVERED", (assertStatement.getFlags() & ASTNode.RECOVERED) == 0);
		Expression message = assertStatement.getMessage();
		assertTrue("No message expression", message != null); //$NON-NLS-1$
		checkSourceRange(message, "foo(", source); //$NON-NLS-1$
		assertTrue("Not flag as RECOVERED", (message.getFlags() & ASTNode.RECOVERED) != 0);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=143212
	public void test0017() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
			"/Converter/src/test/X.java",
			"""
				package test;
				
				public class X {
					void foo() {
					    assert 0 == 0 : ("aa";
					}
				}
				""");

		char[] source = this.workingCopies[0].getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), this.workingCopies[0], true, true);

		assertASTNodeEquals(
			"""
				package test;
				public class X {
				  void foo(){
				    assert 0 == 0 : ("aa");
				  }
				}
				""",
			result);

		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull(node);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		assertTrue("Flag as RECOVERED", (methodDeclaration.getFlags() & ASTNode.RECOVERED) == 0);
		Block block = methodDeclaration.getBody();
		assertTrue("Flag as RECOVERED", (block.getFlags() & ASTNode.RECOVERED) == 0);
		List statements = block.statements();
		assertEquals("wrong size", 1, statements.size()); //$NON-NLS-1$
		Statement statement = (Statement) statements.get(0);
		assertTrue("Not an assert statement", statement.getNodeType() == ASTNode.ASSERT_STATEMENT); //$NON-NLS-1$
		AssertStatement assertStatement = (AssertStatement) statement;
		checkSourceRange(assertStatement, "assert 0 == 0 : (\"aa\";", source); //$NON-NLS-1$
		assertTrue("Flag as RECOVERED", (assertStatement.getFlags() & ASTNode.RECOVERED) == 0);
		Expression message = assertStatement.getMessage();
		assertTrue("No message expression", message != null); //$NON-NLS-1$
		checkSourceRange(message, "(\"aa\"", source); //$NON-NLS-1$
		assertTrue("Not flag as RECOVERED", (message.getFlags() & ASTNode.RECOVERED) != 0);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=239117
	public void test0018() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[0];

		ASTResult result = this.buildMarkedAST(
				"/Converter/src/p/X.java",
				"""
					package p;
					public class X {
						void m(Object var) {
							if (1==1 && var.equals(1)[*1*][*1*] {
							}
						}
					}""");

		assertASTResult(
				"""
					===== AST =====
					package p;
					public class X {
					  void m(  Object var){
					    if (1 == 1 && var.equals(1))     [*1*];[*1*]
					  }
					}
					
					===== Details =====
					1:EMPTY_STATEMENT,[77,0],,RECOVERED,[N/A]
					===== Problems =====
					1. WARNING in /Converter/src/p/X.java (at line 4)
						if (1==1 && var.equals(1) {
						    ^^^^
					Comparing identical expressions
					2. ERROR in /Converter/src/p/X.java (at line 4)
						if (1==1 && var.equals(1) {
						                ^^^^^^
					The method equals(Object) in the type Object is not applicable for the arguments (int)
					3. ERROR in /Converter/src/p/X.java (at line 4)
						if (1==1 && var.equals(1) {
						                        ^
					Syntax error, insert ") Statement" to complete BlockStatements
					""",
				result);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=329998
	public void test0019() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
			"/Converter/src/test/X.java",
			"""
				package test;
				public class X {
					void foo() {
						return new Object() {hash};
					}
				}
				""");

		char[] source = this.workingCopies[0].getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), this.workingCopies[0], true, true);

		assertASTNodeEquals(
			"""
				package test;
				public class X {
				  void foo(){
				    return new Object(){
				    }
				;
				  }
				}
				""",
			result);

		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull(node);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		Block block = methodDeclaration.getBody();
		List statements = block.statements();
		assertEquals("wrong size", 1, statements.size()); //$NON-NLS-1$
		Statement statement = (Statement) statements.get(0);
		assertTrue("Not a return statement", statement.getNodeType() == ASTNode.RETURN_STATEMENT); //$NON-NLS-1$
		ReturnStatement returnStatement = (ReturnStatement) statement;
		checkSourceRange(returnStatement, "return new Object() {hash};", source); //$NON-NLS-1$
		Expression expression = returnStatement.getExpression();
		checkSourceRange(expression, "new Object() {hash}", source); //$NON-NLS-1$
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=329998
	public void test0020() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
			"/Converter/src/test/X.java",
			"""
				package test;
				public class X {
					void foo() {
						field= new Object() {hash};
					}
				}
				""");

		char[] source = this.workingCopies[0].getSource().toCharArray();
		ASTNode result = runConversion(getJLS3(), this.workingCopies[0], true, true);

		assertASTNodeEquals(
			"""
				package test;
				public class X {
				  void foo(){
				    field=new Object(){
				    }
				;
				  }
				}
				""",
			result);

		ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
		assertNotNull(node);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		Block block = methodDeclaration.getBody();
		List statements = block.statements();
		assertEquals("wrong size", 1, statements.size()); //$NON-NLS-1$
		Statement statement = (Statement) statements.get(0);
		assertTrue("Not an expression statement", statement.getNodeType() == ASTNode.EXPRESSION_STATEMENT); //$NON-NLS-1$
		ExpressionStatement expressionStatement = (ExpressionStatement) statement;
		checkSourceRange(expressionStatement, "field= new Object() {hash};", source); //$NON-NLS-1$
		Expression expression = expressionStatement.getExpression();
		assertTrue("Not an assignment", expression.getNodeType() == ASTNode.ASSIGNMENT); //$NON-NLS-1$
		Assignment assignment = (Assignment) expression;
		Expression anonymousClassDeclaration = assignment.getRightHandSide();
		checkSourceRange(anonymousClassDeclaration, "new Object() {hash}", source); //$NON-NLS-1$
		checkSourceRange(assignment, "field= new Object() {hash}", source); //$NON-NLS-1$
	}

		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=340691
		public void test0021() throws JavaModelException {
			this.workingCopies = new ICompilationUnit[1];
			this.workingCopies[0] = getWorkingCopy(
				"/Converter/src/test/X.java",
				"""
					package test;
					public class X {
						void foo() {
							synchronized new Object();
						}
					}
					""");
			ASTNode result = runConversion(getJLS3(), this.workingCopies[0], true, true);

			assertASTNodeEquals(
				"""
					package test;
					public class X {
					  void foo(){
					  }
					}
					""",
				result);

			ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
			assertNotNull(node);
			assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
			MethodDeclaration methodDeclaration = (MethodDeclaration) node;
			Block block = methodDeclaration.getBody();
			List statements = block.statements();
			assertEquals("wrong size", 0, statements.size()); //$NON-NLS-1$
		}
}
