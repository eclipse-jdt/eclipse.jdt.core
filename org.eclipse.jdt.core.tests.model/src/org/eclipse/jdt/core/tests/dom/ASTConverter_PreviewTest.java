/*******************************************************************************
 * Copyright (c) 2020, 2021 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.dom;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.GuardedPattern;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.Pattern;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypePattern;

import junit.framework.Test;

public class ASTConverter_PreviewTest extends ConverterTestSetup {

	ICompilationUnit workingCopy;

	public void setUpSuite() throws Exception {
		super.setUpSuite();
		this.ast = AST.newAST(getASTLatest(), false);
		this.currentProject = getJavaProject("Converter_17");
		if (this.ast.apiLevel() == AST.JLS17) {
			this.currentProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_17);
			this.currentProject.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_17);
			this.currentProject.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_17);
			this.currentProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			this.currentProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);

		}
	}

	public ASTConverter_PreviewTest(String name) {
		super(name);
	}

	public static Test suite() {
		return buildModelTestSuite(ASTConverter_PreviewTest.class);
	}

	static int getASTLatest() {
		return AST.JLS17;
	}
	protected void tearDown() throws Exception {
		super.tearDown();
		if (this.workingCopy != null) {
			this.workingCopy.discardWorkingCopy();
			this.workingCopy = null;
		}
	}

	private void printJREError() {
		System.err.println("Test "+getName()+" requires a JRE 17");
	}

	@SuppressWarnings("rawtypes")
	public void testTypePattern() throws CoreException {
		if (!isJRE17) {
			printJREError();
			return;
		}
		String contents = "public class X {\n" +
				"void foo(Object o) {\n" +
				"	switch (o) {\n" +
			    "		case Integer i  -> System.out.println(i.toString());\n" +
			    "		case String s   -> System.out.println(s);\n" +
			    "		default       	-> System.out.println(o.toString());\n" +
			    "	}\n" +
			    "}\n" +
				"\n" +
				"}\n";
		this.workingCopy = getWorkingCopy("/Converter_17/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 0, 0);
		assertEquals("Switch statement", node.getNodeType(), ASTNode.SWITCH_STATEMENT);
		List statements = ((SwitchStatement)node).statements();
		assertEquals("incorrect no of statements", 6, statements.size());
		SwitchCase caseInteger = (SwitchCase) statements.get(0);
		Expression typePattern = (Expression)caseInteger.expressions().get(0);
		assertEquals("Type Pattern", typePattern.getNodeType(), ASTNode.TYPE_PATTERN);
		SingleVariableDeclaration patternVariable = ((TypePattern)typePattern).getPatternVariable();
		assertEquals("Type Pattern Integer", "Integer", patternVariable.getType().toString());
		SingleVariableDeclaration patternVariable2 = ((TypePattern)typePattern).patternVariables().get(0);
		assertEquals(patternVariable, patternVariable2);

		SwitchCase caseString = (SwitchCase) statements.get(2);
		typePattern = (Expression)caseString.expressions().get(0);
		assertEquals("Type Pattern", typePattern.getNodeType(), ASTNode.TYPE_PATTERN);
		patternVariable = ((TypePattern)typePattern).getPatternVariable();
		assertEquals("Type Pattern Integer", "String", patternVariable.getType().toString());
		patternVariable2 = ((TypePattern)typePattern).patternVariables().get(0);
		assertEquals(patternVariable, patternVariable2);

		SwitchCase caseDefault = (SwitchCase) statements.get(4);
		assertTrue("Default case", caseDefault.isDefault());

	}

	@SuppressWarnings("rawtypes")
	public void testGuardedPattern() throws CoreException {
		if (!isJRE17) {
			printJREError();
			return;
		}
		String contents = "public class X {\n" +
				"void foo(Object o) {\n" +
				"	switch (o) {\n" +
			    "		case Integer i  && (i.intValue() > 10)   -> System.out.println(\"Greater than 10 \");\n" +
			    "		case String s && s.equals(\"ff\")   -> System.out.println(s);\n" +
			    "		default       	-> System.out.println(o.toString());\n" +
			    "	}\n" +
			    "}\n" +
				"\n" +
				"}\n";
		this.workingCopy = getWorkingCopy("/Converter_17/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 0, 0);
		assertEquals("Switch statement", node.getNodeType(), ASTNode.SWITCH_STATEMENT);
		List statements = ((SwitchStatement)node).statements();
		assertEquals("incorrect no of statements", 6, statements.size());
		SwitchCase caseInteger = (SwitchCase) statements.get(0);
		Expression guardedPattern = (Expression)caseInteger.expressions().get(0);
		assertEquals("Guarded Pattern", guardedPattern.getNodeType(), ASTNode.GUARDED_PATTERN);
		Pattern pattern = ((GuardedPattern)guardedPattern).getPattern();
		SingleVariableDeclaration patternVariable = ((TypePattern)pattern).getPatternVariable();
		assertEquals("Type Pattern Integer", "Integer", patternVariable.getType().toString());
		SingleVariableDeclaration patternVariable2 = ((TypePattern)pattern).patternVariables().get(0);
		assertEquals(patternVariable, patternVariable2);
		Expression expression = ((GuardedPattern)guardedPattern).getExpression();
		Expression expression2 = ((ParenthesizedExpression)expression).getExpression();
		assertEquals("Infix expression", "i.intValue() > 10", expression2.toString());


		SwitchCase caseString = (SwitchCase) statements.get(2);
		guardedPattern = (Expression)caseString.expressions().get(0);
		assertEquals("Guarded Pattern", guardedPattern.getNodeType(), ASTNode.GUARDED_PATTERN);
		pattern = ((GuardedPattern)guardedPattern).getPattern();
		patternVariable = ((TypePattern)pattern).getPatternVariable();
		assertEquals("Type Pattern String", "String", patternVariable.getType().toString());
		patternVariable2 = ((TypePattern)pattern).patternVariables().get(0);
		assertEquals(patternVariable, patternVariable2);
		expression = ((GuardedPattern)guardedPattern).getExpression();
		assertEquals("Infix expression", "s.equals(\"ff\")",expression.toString());

		SwitchCase caseDefault = (SwitchCase) statements.get(4);
		assertTrue("Default case", caseDefault.isDefault());

	}

	@SuppressWarnings("rawtypes")
	public void testParenthesizedExpressionPattern() throws CoreException {
		if (!isJRE17) {
			printJREError();
			return;
		}
		String contents = "public class X {\n" +
				"void foo(Object o) {\n" +
				"	switch (o) {\n" +
			    "		case (Integer i)  : System.out.println(i.toString());\n" +
			    "		default       	  : System.out.println(o.toString());\n" +
			    "	}\n" +
			    "}\n" +
				"\n" +
				"}\n";
		this.workingCopy = getWorkingCopy("/Converter_17/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 0, 0);
		assertEquals("Switch statement", node.getNodeType(), ASTNode.SWITCH_STATEMENT);
		List statements = ((SwitchStatement)node).statements();
		assertEquals("incorrect no of statements", 4, statements.size());
		SwitchCase caseInteger = (SwitchCase) statements.get(0);
		Expression parenthesizedExpression = (Expression)caseInteger.expressions().get(0);
		assertEquals("Parenthesized Expression", parenthesizedExpression.getNodeType(), ASTNode.PARENTHESIZED_EXPRESSION);
		Expression targetPattern = ((ParenthesizedExpression)parenthesizedExpression).getExpression();
		assertEquals("Type Pattern", targetPattern.getNodeType(), ASTNode.TYPE_PATTERN);
		SingleVariableDeclaration patternVariable = ((TypePattern)targetPattern).getPatternVariable();
		assertEquals("Type Pattern Integer", "Integer", patternVariable.getType().toString());
		SingleVariableDeclaration patternVariable2 = ((TypePattern)targetPattern).patternVariables().get(0);
		assertEquals(patternVariable, patternVariable2);

		SwitchCase caseDefault = (SwitchCase) statements.get(2);
		assertTrue("Default case", caseDefault.isDefault());

	}

	@SuppressWarnings("rawtypes")
	public void testNullPattern() throws CoreException {
		if (!isJRE17) {
			printJREError();
			return;
		}
		String contents = "public class X {\n" +
				"void foo(Object o) {\n" +
				"	switch (o) {\n" +
			    "		case Integer i  : System.out.println(i.toString());\n" +
				"						  break;\n" +
			    "		case null  		: System.out.println(\"null\");\n" +
			    "		default       	: System.out.println(o.toString());\n" +
			    "	}\n" +
			    "}\n" +
				"\n" +
				"}\n";
		this.workingCopy = getWorkingCopy("/Converter_17/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 0, 0);
		assertEquals("Switch statement", node.getNodeType(), ASTNode.SWITCH_STATEMENT);
		List statements = ((SwitchStatement)node).statements();
		assertEquals("incorrect no of statements", 7, statements.size());
		SwitchCase caseInteger = (SwitchCase) statements.get(3);
		Expression nullExpression = (Expression)caseInteger.expressions().get(0);
		assertEquals("Null Expression", nullExpression.getNodeType(), ASTNode.NULL_LITERAL);
	}

	@SuppressWarnings("rawtypes")
	public void testCaseDefaultExpressionPattern() throws CoreException {
		if (!isJRE17) {
			printJREError();
			return;
		}
		String contents = "public class X {\n" +
				"void foo(Object o) {\n" +
				"	switch (o) {\n" +
			    "		case Integer i  : System.out.println(i.toString());\n" +
			    "		case default    : System.out.println(o.toString());\n" +
			    "	}\n" +
			    "}\n" +
				"\n" +
				"}\n";
		this.workingCopy = getWorkingCopy("/Converter_17/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 0, 0);
		assertEquals("Switch statement", node.getNodeType(), ASTNode.SWITCH_STATEMENT);
		List statements = ((SwitchStatement)node).statements();
		assertEquals("incorrect no of statements", 4, statements.size());
		SwitchCase caseDefault = (SwitchCase) statements.get(2);
		Expression caseDefaultExpression = (Expression) caseDefault.expressions().get(0);
		assertEquals("Case Default Expression",caseDefaultExpression.getNodeType() , ASTNode.CASE_DEFAULT_EXPRESSION);


	}

	public void testBug575250() throws CoreException {
		if (!isJRE17) {
			System.err.println("Test "+getName()+" requires a JRE 17");
			return;
		}
		String contents = "public class X {\n" +
				"  static void foo(Object o) {\n" +
				"    switch (o) {\n" +
				"      case (Integer i_1): System.out.println(\"Integer\");\n" +
				"      default: System.out.println(\"Object\");" +
				"    }\n" +
				"  }\n" +
				"}\n";
		this.workingCopy = getWorkingCopy("/Converter_17/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		List<AbstractTypeDeclaration> types = compilationUnit.types();
		assertEquals("incorrect types", types.size(), 1);
		AbstractTypeDeclaration type = types.get(0);
		assertTrue("should be a type", type instanceof TypeDeclaration);
		TypeDeclaration typeDecl = (TypeDeclaration)type;
		final List<TypePattern> result = new ArrayList<>();
		typeDecl.accept(new ASTVisitor() {
			public boolean visit(TypePattern n) {
				result.add(n);
				return true;
			}
		});
		assertEquals("incorrect no of patterns", 1, result.size());
		TypePattern typePattern = result.get(0);
		assertNotNull("pattern is null", typePattern);
		int start = contents.indexOf("Integer");
		int length = "Integer i_1".length();
		assertEquals("wrong source range", typePattern.getStartPosition(), start);
		assertEquals("wrong source range", typePattern.getLength(), length);

		SingleVariableDeclaration patternVariable = typePattern.getPatternVariable();
		assertEquals("wrong source range", patternVariable.getStartPosition(), start);
		assertEquals("wrong source range", patternVariable.getLength(), length);

		Type type2 = patternVariable.getType();
		assertEquals("wrong source range", type2.getStartPosition(), start);
		assertEquals("wrong source range", type2.getLength(), "Integer".length());

		SimpleName name = patternVariable.getName();
		start = contents.indexOf("i_1");
		assertEquals("wrong source range", name.getStartPosition(), start);
		assertEquals("wrong source range", name.getLength(), "i_1".length());
	}
}
