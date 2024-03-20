/*******************************************************************************
 * Copyright (c) 2023, 2024 IBM Corporation and others.
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

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringFragment;
import org.eclipse.jdt.core.dom.StringTemplateComponent;
import org.eclipse.jdt.core.dom.StringTemplateExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import junit.framework.Test;

@SuppressWarnings("rawtypes")
public class ASTConverterStringTemplateTest extends ConverterTestSetup {

	ICompilationUnit workingCopy;

	public void setUpSuite() throws Exception {
		super.setUpSuite();
		this.ast = AST.newAST(getASTLatest(), false);
		this.currentProject = getJavaProject("Converter_22");
		if (this.ast.apiLevel() == AST.JLS22) {
			this.currentProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_22);
			this.currentProject.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_22);
			this.currentProject.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_22);
			this.currentProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			this.currentProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
		}
	}

	public ASTConverterStringTemplateTest(String name) {
		super(name);
	}

	public static Test suite() {
		return buildModelTestSuite(ASTConverterStringTemplateTest.class);
	}

	static int getASTLatest() {
		return AST.getJLSLatest();
	}
	protected void tearDown() throws Exception {
		super.tearDown();
		if (this.workingCopy != null) {
			this.workingCopy.discardWorkingCopy();
			this.workingCopy = null;
		}
	}

	private void printJREError() {
		System.err.println("Test "+getName()+" requires a JRE 22");
	}
	private ASTNode doBasicTestsAndGetNode(org.eclipse.jdt.core.dom.ASTNode node, int typeIndex, int bodyIndex, int statementIndex) {
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		return getASTNode(compilationUnit, typeIndex, bodyIndex, statementIndex);
	}
	// Test a simple Template expression with String Literal (no embedded expressions)
	public void test001() throws JavaModelException {
		if (!isJRE22) {
			printJREError();
			return;
		}
		String contents = """
				public class X {
					public void foo() {
						String str = STR."Hello Jay!";
					}
				}
				""";

		this.workingCopy = getWorkingCopy("/Converter_22/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		node = doBasicTestsAndGetNode(node, 0, 0, 0);
		assertEquals("Wrong type of statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		List fragments = statement.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = fragment.getInitializer();
	   	assertEquals("Not a string template expression", ASTNode.STRING_TEMPLATE_EXPRESSION, expression.getNodeType());
		StringTemplateExpression templateExpr = (StringTemplateExpression) expression;
		expression = templateExpr.getProcessor();
		assertNotNull("Template processor is null", expression);
		assertEquals("Not a simple name", ASTNode.SIMPLE_NAME, expression.getNodeType());
		assertEquals("Incorrect expression", "STR", ((SimpleName) expression).getIdentifier());
		StringFragment literal = templateExpr.getFirstFragment();
		assertEquals("Incorrect literal value", "Hello Jay!", literal.getEscapedValue());
		assertFalse("Not a text block", templateExpr.isMultiline());
	}
	// Test a simple Template expression with text block (no embedded expressions)
	public void test002() throws JavaModelException {
		if (!isJRE22) {
			printJREError();
			return;
		}
		String contents = """
				public class X {
					public void foo() {
						String str = STR.\"""
				Hello Jay!\""";
					}
				}
				""";

		this.workingCopy = getWorkingCopy("/Converter_22/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		node = doBasicTestsAndGetNode(node, 0, 0, 0);
		assertEquals("Wrong type of statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		List fragments = statement.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = fragment.getInitializer();
	   	assertEquals("Not a string template expression", ASTNode.STRING_TEMPLATE_EXPRESSION, expression.getNodeType());
		StringTemplateExpression templateExpr = (StringTemplateExpression) expression;
		expression = templateExpr.getProcessor();
		assertNotNull("Template processor is null", expression);
		assertEquals("Not a simple name", ASTNode.SIMPLE_NAME, expression.getNodeType());
		assertEquals("Incorrect expression", "STR", ((SimpleName) expression).getIdentifier());
		StringFragment literal = templateExpr.getFirstFragment();
		assertEquals("Incorrect literal value", "Hello Jay!", literal.getEscapedValue());
		assertTrue("Should not be a text block", templateExpr.isMultiline());
	}
	// Test a simple template expression with empty embedded expression (string literal)
	public void test003() throws JavaModelException {
		if (!isJRE22) {
			printJREError();
			return;
		}
		String contents = """
				public class X {
					public void foo() {
						String str = STR."Hello \\{}!";
					}
				}
				""";

		this.workingCopy = getWorkingCopy("/Converter_22/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		node = doBasicTestsAndGetNode(node, 0, 0, 0);
		assertEquals("Wrong type of statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		List fragments = statement.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = fragment.getInitializer();
	   	assertEquals("Not a string template expression", ASTNode.STRING_TEMPLATE_EXPRESSION, expression.getNodeType());
		StringTemplateExpression templateExpr = (StringTemplateExpression) expression;
		expression = templateExpr.getProcessor();
		assertNotNull("Template processor is null", expression);
		StringFragment literal = templateExpr.getFirstFragment();
		assertEquals("Incorrect literal value", "Hello ", literal.getEscapedValue());
		assertEquals("incorrect no of fragments", 1, templateExpr.components().size());
		expression = templateExpr.components().get(0);
		assertEquals("Not a string literal", ASTNode.STRING_TEMPLATE_COMPONENT, expression.getNodeType());
		StringTemplateComponent template = (StringTemplateComponent) expression;
		literal = template.getStringFragment();
		assertEquals("Incorrect literal value", "!", literal.getEscapedValue());
		expression = template.getEmbeddedExpression();
		assertEquals("Not a null literal", ASTNode.NULL_LITERAL, expression.getNodeType());
		assertFalse("Should be a text block", templateExpr.isMultiline());
	}
	// Test a simple template expression with empty embedded expression (text block)
	public void test004() throws JavaModelException {
		if (!isJRE22) {
			printJREError();
			return;
		}
		String contents = """
				public class X {
					public void foo() {
						String str = STR.\"""
				Hello \\{}!\""";
					}
				}
				""";

		this.workingCopy = getWorkingCopy("/Converter_22/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		node = doBasicTestsAndGetNode(node, 0, 0, 0);
		assertEquals("Wrong type of statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		List fragments = statement.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = fragment.getInitializer();
	   	assertEquals("Not a string template expression", ASTNode.STRING_TEMPLATE_EXPRESSION, expression.getNodeType());
		StringTemplateExpression templateExpr = (StringTemplateExpression) expression;
		expression = templateExpr.getProcessor();
		assertNotNull("Template processor is null", expression);
		StringFragment literal = templateExpr.getFirstFragment();
		assertEquals("Incorrect literal value", "Hello ", literal.getEscapedValue());
		assertEquals("incorrect no of fragments", 1, templateExpr.components().size());
		expression = templateExpr.components().get(0);
		assertEquals("Not a string literal", ASTNode.STRING_TEMPLATE_COMPONENT, expression.getNodeType());
		StringTemplateComponent template = (StringTemplateComponent) expression;
		literal = template.getStringFragment();
		assertEquals("Incorrect literal value", "!", literal.getEscapedValue());
		expression = template.getEmbeddedExpression();
		assertEquals("Not a null literal", ASTNode.NULL_LITERAL, expression.getNodeType());
		assertTrue("Should not be a text block", templateExpr.isMultiline());
	}
	// test a template expression with null literal as embedded expression (string)
	public void test005() throws JavaModelException {
		if (!isJRE22) {
			printJREError();
			return;
		}
		String contents = """
				public class X {
					public void foo() {
						String str = STR."Hello \\{null}!";
					}
				}
				""";

		this.workingCopy = getWorkingCopy("/Converter_22/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		node = doBasicTestsAndGetNode(node, 0, 0, 0);
		assertEquals("Wrong type of statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		List fragments = statement.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = fragment.getInitializer();
	   	assertEquals("Not a string template expression", ASTNode.STRING_TEMPLATE_EXPRESSION, expression.getNodeType());
		StringTemplateExpression templateExpr = (StringTemplateExpression) expression;
		expression = templateExpr.getProcessor();
		assertNotNull("Template processor is null", expression);
		StringFragment literal = templateExpr.getFirstFragment();
		assertEquals("Incorrect literal value", "Hello ", literal.getEscapedValue());
		assertEquals("incorrect no of fragments", 1, templateExpr.components().size());
		expression = templateExpr.components().get(0);
		assertEquals("Not a string literal", ASTNode.STRING_TEMPLATE_COMPONENT, expression.getNodeType());
		StringTemplateComponent template = (StringTemplateComponent) expression;
		literal = template.getStringFragment();
		assertEquals("Incorrect literal value", "!", literal.getEscapedValue());
		expression = template.getEmbeddedExpression();
		assertEquals("Not a null literal", ASTNode.NULL_LITERAL, expression.getNodeType());
		assertFalse("Should be a text block", templateExpr.isMultiline());
	}
	// test a template expression with null literal as embedded expression (text block)
	public void test006() throws JavaModelException {
		if (!isJRE22) {
			printJREError();
			return;
		}
		String contents = """
				public class X {
					public void foo() {
						String str = STR.\"""
				Hello \\{null}!\""";
					}
				}
				""";

		this.workingCopy = getWorkingCopy("/Converter_22/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		node = doBasicTestsAndGetNode(node, 0, 0, 0);
		assertEquals("Wrong type of statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		List fragments = statement.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = fragment.getInitializer();
	   	assertEquals("Not a string template expression", ASTNode.STRING_TEMPLATE_EXPRESSION, expression.getNodeType());
		StringTemplateExpression templateExpr = (StringTemplateExpression) expression;
		expression = templateExpr.getProcessor();
		assertNotNull("Template processor is null", expression);
		StringFragment literal = templateExpr.getFirstFragment();
		assertEquals("Incorrect literal value", "Hello ", literal.getEscapedValue());
		assertEquals("incorrect no of fragments", 1, templateExpr.components().size());
		expression = templateExpr.components().get(0);
		assertEquals("Not a string literal", ASTNode.STRING_TEMPLATE_COMPONENT, expression.getNodeType());
		StringTemplateComponent template = (StringTemplateComponent) expression;
		literal = template.getStringFragment();
		assertEquals("Incorrect literal value", "!", literal.getEscapedValue());
		expression = template.getEmbeddedExpression();
		assertEquals("Not a null literal", ASTNode.NULL_LITERAL, expression.getNodeType());
		assertTrue("Should not be a text block", templateExpr.isMultiline());
	}
	// Test a simple Template expression with string literal and single embedded expression
	public void test007() throws JavaModelException {
		if (!isJRE22) {
			printJREError();
			return;
		}
		String contents = """
				public class X {
					public void foo() {
						String name = "Jay";
						String str = STR."Hello \\{name}!";
					}
				}
				""";

		this.workingCopy = getWorkingCopy("/Converter_22/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		node = doBasicTestsAndGetNode(node, 0, 0, 1);
		assertEquals("Wrong type of statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		List fragments = statement.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = fragment.getInitializer();
	   	assertEquals("Not a string template expression", ASTNode.STRING_TEMPLATE_EXPRESSION, expression.getNodeType());
		StringTemplateExpression templateExpr = (StringTemplateExpression) expression;
		expression = templateExpr.getProcessor();
		assertNotNull("Template processor is null", expression);
		StringFragment literal = templateExpr.getFirstFragment();
		assertEquals("Incorrect literal value", "Hello ", literal.getEscapedValue());
		assertEquals("incorrect no of fragments", 1, templateExpr.components().size());
		expression = templateExpr.components().get(0);
		assertEquals("Not a string literal", ASTNode.STRING_TEMPLATE_COMPONENT, expression.getNodeType());
		StringTemplateComponent template = (StringTemplateComponent) expression;
		assertFalse("Should not be a text block", templateExpr.isMultiline());
		literal = template.getStringFragment();
		assertEquals("Incorrect literal value", "!", literal.getEscapedValue());
		expression = template.getEmbeddedExpression();
		assertEquals("Not a simple name", ASTNode.SIMPLE_NAME, expression.getNodeType());

		SimpleName name = (SimpleName) expression;
		IVariableBinding binding = (IVariableBinding) name.resolveBinding();
		assertNotNull("binding is null", binding);
		assertEquals("Incorrect variable name", "name", binding.getName());
		ITypeBinding type = binding.getType();
		assertEquals("Incorrect type", "java.lang.String", type.getBinaryName());
	}
	// Test a simple Template expression with text block and single embedded expression
	public void test008() throws JavaModelException {
		if (!isJRE22) {
			printJREError();
			return;
		}
		String contents = """
				public class X {
					public void foo() {
						String name = "Jay";
						String str = STR.\"""
				Hello \\{name}!\""";
					}
				}
				""";

		this.workingCopy = getWorkingCopy("/Converter_22/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		node = doBasicTestsAndGetNode(node, 0, 0, 1);
		assertEquals("Switch statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		List fragments = statement.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = fragment.getInitializer();
	   	assertEquals("Not a string template expression", ASTNode.STRING_TEMPLATE_EXPRESSION, expression.getNodeType());
		StringTemplateExpression templateExpr = (StringTemplateExpression) expression;
		expression = templateExpr.getProcessor();
		assertNotNull("Template processor is null", expression);
		StringFragment literal = templateExpr.getFirstFragment();
		assertEquals("Incorrect literal value", "Hello ", literal.getEscapedValue());
		assertEquals("incorrect no of fragments", 1, templateExpr.components().size());
		expression = templateExpr.components().get(0);
		assertEquals("Not a string literal", ASTNode.STRING_TEMPLATE_COMPONENT, expression.getNodeType());
		StringTemplateComponent template = (StringTemplateComponent) expression;
		assertTrue("Should be a text block", templateExpr.isMultiline());
		literal = template.getStringFragment();
		assertEquals("Incorrect literal value", "!", literal.getEscapedValue());
		expression = template.getEmbeddedExpression();
		assertEquals("Not a simple name", ASTNode.SIMPLE_NAME, expression.getNodeType());

		SimpleName name = (SimpleName) expression;
		IVariableBinding binding = (IVariableBinding) name.resolveBinding();
		assertNotNull("binding is null", binding);
		assertEquals("Incorrect variable name", "name", binding.getName());
		ITypeBinding type = binding.getType();
		assertEquals("Incorrect type", "java.lang.String", type.getBinaryName());
	}
	// Test a simple Template expression with String literal and multiple embedded expressions
	public void test009() throws JavaModelException {
		if (!isJRE22) {
			printJREError();
			return;
		}
		String contents = """
				public class X {
					public void foo() {
						String name = "Jay";
						bar(STR."Hello \\{
						name
						}!");
					}
					public void bar(String s) {}
				}
				""";

		this.workingCopy = getWorkingCopy("/Converter_22/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		node = doBasicTestsAndGetNode(node, 0, 0, 1);
		assertEquals("Wrong type of statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		ExpressionStatement statement = (ExpressionStatement) node;
		Expression expression = statement.getExpression();
		assertEquals("Not a metohd invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
		MethodInvocation methodInvo = (MethodInvocation) expression;
		List arguments = methodInvo.arguments();
		assertEquals("incorrect no of args", 1, arguments.size());
		expression = (Expression) arguments.get(0);
	   	assertEquals("Not a string template expression", ASTNode.STRING_TEMPLATE_EXPRESSION, expression.getNodeType());
		StringTemplateExpression templateExpr = (StringTemplateExpression) expression;
		expression = templateExpr.getProcessor();
		assertNotNull("Template processor is null", expression);
		StringFragment literal = templateExpr.getFirstFragment();
		assertEquals("Incorrect literal value", "Hello ", literal.getEscapedValue());
		assertEquals("incorrect no of fragments", 1, templateExpr.components().size());
		expression = templateExpr.components().get(0);
		assertEquals("Not a string literal", ASTNode.STRING_TEMPLATE_COMPONENT, expression.getNodeType());
		StringTemplateComponent template = (StringTemplateComponent) expression;
		assertFalse("Should not be a text block", templateExpr.isMultiline());
		literal = template.getStringFragment();
		assertEquals("Incorrect literal value", "!", literal.getEscapedValue());
		expression = template.getEmbeddedExpression();
		assertEquals("Not a simple name", ASTNode.SIMPLE_NAME, expression.getNodeType());

		SimpleName name = (SimpleName) expression;
		IVariableBinding binding = (IVariableBinding) name.resolveBinding();
		assertNotNull("binding is null", binding);
		assertEquals("Incorrect variable name", "name", binding.getName());
		ITypeBinding type = binding.getType();
		assertEquals("Incorrect type", "java.lang.String", type.getBinaryName());
	}
	// Test a simple Template expression with text block and multiple embedded expressions
	public void test0010() throws JavaModelException {
		if (!isJRE22) {
			printJREError();
			return;
		}
		String contents = """
				public class X {
					public void foo() {
						String name = "Jay";
						bar(STR.\"""
				Hello \\{name}!\""");
					}
					public void bar(String s) {}
				}
				""";

		this.workingCopy = getWorkingCopy("/Converter_22/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		node = doBasicTestsAndGetNode(node, 0, 0, 1);
		assertEquals("Wrong type of statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		ExpressionStatement statement = (ExpressionStatement) node;
		Expression expression = statement.getExpression();
		assertEquals("Not a metohd invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
		MethodInvocation methodInvo = (MethodInvocation) expression;
		List arguments = methodInvo.arguments();
		assertEquals("incorrect no of args", 1, arguments.size());
		expression = (Expression) arguments.get(0);
	   	assertEquals("Not a string template expression", ASTNode.STRING_TEMPLATE_EXPRESSION, expression.getNodeType());
		StringTemplateExpression templateExpr = (StringTemplateExpression) expression;
		expression = templateExpr.getProcessor();
		assertNotNull("Template processor is null", expression);
		StringFragment literal = templateExpr.getFirstFragment();
		assertEquals("Incorrect literal value", "Hello ", literal.getEscapedValue());
		assertEquals("incorrect no of fragments", 1, templateExpr.components().size());
		expression = templateExpr.components().get(0);
		assertEquals("Not a string literal", ASTNode.STRING_TEMPLATE_COMPONENT, expression.getNodeType());
		StringTemplateComponent template = (StringTemplateComponent) expression;
		assertTrue("Should be a text block", templateExpr.isMultiline());
		literal = template.getStringFragment();
		assertEquals("Incorrect literal value", "!", literal.getEscapedValue());
		expression = template.getEmbeddedExpression();
		assertEquals("Not a simple name", ASTNode.SIMPLE_NAME, expression.getNodeType());

		SimpleName name = (SimpleName) expression;
		IVariableBinding binding = (IVariableBinding) name.resolveBinding();
		assertNotNull("binding is null", binding);
		assertEquals("Incorrect variable name", "name", binding.getName());
		ITypeBinding type = binding.getType();
		assertEquals("Incorrect type", "java.lang.String", type.getBinaryName());
	}
	// Test a simple Template expression with text block and multiple embedded expressions
	public void test0011() throws JavaModelException {
		if (!isJRE22) {
			printJREError();
			return;
		}
		String contents = """
				public class X {
					public void foo() {
						String first = "Jay";
						Object last = "A";
						bar(STR.\"""
				Hello \\{first} \\{last}!\""");
					}
					public void bar(String s) {}
				}
				""";

		this.workingCopy = getWorkingCopy("/Converter_22/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		node = doBasicTestsAndGetNode(node, 0, 0, 2);
		assertEquals("Wrong type of statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		ExpressionStatement statement = (ExpressionStatement) node;
		Expression expression = statement.getExpression();
		assertEquals("Not a metohd invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
		MethodInvocation methodInvo = (MethodInvocation) expression;
		List arguments = methodInvo.arguments();
		assertEquals("incorrect no of args", 1, arguments.size());
		expression = (Expression) arguments.get(0);
	   	assertEquals("Not a string template expression", ASTNode.STRING_TEMPLATE_EXPRESSION, expression.getNodeType());
		StringTemplateExpression templateExpr = (StringTemplateExpression) expression;
		expression = templateExpr.getProcessor();
		assertNotNull("Template processor is null", expression);
		StringFragment literal = templateExpr.getFirstFragment();
		assertEquals("Incorrect literal value", "Hello ", literal.getEscapedValue());
		assertEquals("incorrect no of fragments", 2, templateExpr.components().size());
		expression = templateExpr.components().get(0);
		assertEquals("Not a string literal", ASTNode.STRING_TEMPLATE_COMPONENT, expression.getNodeType());
		StringTemplateComponent template = (StringTemplateComponent) expression;
		assertTrue("Should be a text block", templateExpr.isMultiline());
		literal = template.getStringFragment();
		assertEquals("Incorrect literal value", " ", literal.getEscapedValue());
		expression = template.getEmbeddedExpression();
		assertEquals("Not a simple name", ASTNode.SIMPLE_NAME, expression.getNodeType());
		SimpleName name = (SimpleName) expression;
		IVariableBinding binding = (IVariableBinding) name.resolveBinding();
		assertNotNull("binding is null", binding);
		assertEquals("Incorrect variable name", "first", binding.getName());
		ITypeBinding type = binding.getType();
		assertEquals("Incorrect type", "java.lang.String", type.getBinaryName());
		expression = templateExpr.components().get(1);
		assertEquals("Not a string literal", ASTNode.STRING_TEMPLATE_COMPONENT, expression.getNodeType());
		template = (StringTemplateComponent) expression;
		assertTrue("Should be a text block", templateExpr.isMultiline());
		literal = template.getStringFragment();
		assertEquals("Incorrect literal value", "!", literal.getEscapedValue());
		expression = template.getEmbeddedExpression();
		assertEquals("Not a simple name", ASTNode.SIMPLE_NAME, expression.getNodeType());
		name = (SimpleName) expression;
		binding = (IVariableBinding) name.resolveBinding();
		assertNotNull("binding is null", binding);
		assertEquals("Incorrect variable name", "last", binding.getName());
		type = binding.getType();
		assertEquals("Incorrect type", "java.lang.Object", type.getBinaryName());
	}
	// Test a simple Template expression with text block and multiple embedded expressions
	public void test0012() throws JavaModelException {
		if (!isJRE22) {
			printJREError();
			return;
		}
		String contents = """
				public class X {
					public void foo() {
						String first = "Jay";
						String last = "A";
						bar(STR.\"""
				Hello \\{first} \\{STR."\\{last}"}!\""");
					}
					public void bar(String s) {}
				}
				""";

		this.workingCopy = getWorkingCopy("/Converter_22/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		node = doBasicTestsAndGetNode(node, 0, 0, 2);
		assertEquals("Wrong type of statement", ASTNode.EXPRESSION_STATEMENT, node.getNodeType());
		ExpressionStatement statement = (ExpressionStatement) node;
		Expression expression = statement.getExpression();
		assertEquals("Not a metohd invocation", ASTNode.METHOD_INVOCATION, expression.getNodeType());
		MethodInvocation methodInvo = (MethodInvocation) expression;
		List arguments = methodInvo.arguments();
		assertEquals("incorrect no of args", 1, arguments.size());
		expression = (Expression) arguments.get(0);
	   	assertEquals("Not a string template expression", ASTNode.STRING_TEMPLATE_EXPRESSION, expression.getNodeType());
		StringTemplateExpression templateExpr = (StringTemplateExpression) expression;
		expression = templateExpr.getProcessor();
		assertNotNull("Template processor is null", expression);
		StringFragment literal = templateExpr.getFirstFragment();
		assertEquals("Incorrect literal value", "Hello ", literal.getEscapedValue());
		assertEquals("incorrect no of fragments", 2, templateExpr.components().size());
		expression = templateExpr.components().get(0);
		assertEquals("Not a string literal", ASTNode.STRING_TEMPLATE_COMPONENT, expression.getNodeType());
		StringTemplateComponent template = (StringTemplateComponent) expression;
		assertTrue("Should be a text block", templateExpr.isMultiline());
		literal = template.getStringFragment();
		assertEquals("Incorrect literal value", " ", literal.getEscapedValue());
		expression = template.getEmbeddedExpression();
		assertEquals("Not a simple name", ASTNode.SIMPLE_NAME, expression.getNodeType());
		SimpleName name = (SimpleName) expression;
		IVariableBinding binding = (IVariableBinding) name.resolveBinding();
		assertNotNull("binding is null", binding);
		assertEquals("Incorrect variable name", "first", binding.getName());
		ITypeBinding type = binding.getType();
		assertEquals("Incorrect type", "java.lang.String", type.getBinaryName());
		expression = templateExpr.components().get(1);
		assertEquals("Not a string literal", ASTNode.STRING_TEMPLATE_COMPONENT, expression.getNodeType());
		template = (StringTemplateComponent) expression;
		assertTrue("Should be a text block", templateExpr.isMultiline());
		literal = template.getStringFragment();
		assertEquals("Incorrect literal value", "!", literal.getEscapedValue());
		expression = template.getEmbeddedExpression();
		assertEquals("Not a simple name", ASTNode.STRING_TEMPLATE_EXPRESSION, expression.getNodeType());

		templateExpr = (StringTemplateExpression) expression;
		expression = templateExpr.getProcessor();
		assertNotNull("Template processor is null", expression);
		literal = templateExpr.getFirstFragment();
		assertEquals("Incorrect literal value", "", literal.getEscapedValue());
		assertEquals("incorrect no of fragments", 1, templateExpr.components().size());
		expression = templateExpr.components().get(0);
		assertEquals("Not a string literal", ASTNode.STRING_TEMPLATE_COMPONENT, expression.getNodeType());
		template = (StringTemplateComponent) expression;
		assertFalse("Should not be a text block", templateExpr.isMultiline());
		literal = template.getStringFragment();
		assertEquals("Incorrect literal value", "", literal.getEscapedValue());
		expression = template.getEmbeddedExpression();
		assertEquals("Not a simple name", ASTNode.SIMPLE_NAME, expression.getNodeType());
		name = (SimpleName) expression;
		binding = (IVariableBinding) name.resolveBinding();
		assertNotNull("binding is null", binding);
		assertEquals("Incorrect variable name", "last", binding.getName());
		type = binding.getType();
		assertEquals("Incorrect type", "java.lang.String", type.getBinaryName());
	}
}
