/*******************************************************************************
 * Copyright (c) 2020, 2023 IBM Corporation and others.
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
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.Pattern;
import org.eclipse.jdt.core.dom.PatternInstanceofExpression;
import org.eclipse.jdt.core.dom.RecordPattern;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchExpression;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypePattern;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import junit.framework.Test;

public class ASTConverter_RecordPattern_Test extends ConverterTestSetup {

	ICompilationUnit workingCopy;

	public void setUpSuite() throws Exception {
		super.setUpSuite();
		this.ast = AST.newAST(getASTLatest(), false);
		this.currentProject = getJavaProject("Converter_19");
		if (this.ast.apiLevel() == AST.JLS20) {
			this.currentProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_20);
			this.currentProject.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_20);
			this.currentProject.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_20);
			this.currentProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			this.currentProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
		}
	}
	protected void disablePreview() {
		this.currentProject = getJavaProject("Converter_19");
		if (this.ast.apiLevel() == AST.JLS20) {
			this.currentProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_20);
			this.currentProject.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_20);
			this.currentProject.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_20);
			this.currentProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
		}
	}
	protected void enabledPreview() {
		this.currentProject = getJavaProject("Converter_19");
		if (this.ast.apiLevel() == AST.JLS20) {
			this.currentProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_20);
			this.currentProject.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_20);
			this.currentProject.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_20);
			this.currentProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			this.currentProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
		}
	}

	public ASTConverter_RecordPattern_Test(String name) {
		super(name);
	}

	public static Test suite() {
		return buildModelTestSuite(ASTConverter_RecordPattern_Test.class);
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
		System.err.println("Test "+getName()+" requires a JRE 20");
	}

	@SuppressWarnings("rawtypes")
	public void testTypePattern() throws CoreException {
		if (!isJRE20) {
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
		this.workingCopy = getWorkingCopy("/Converter_19/src/X.java", true/*resolve*/);
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
		if (!isJRE20) {
			printJREError();
			return;
		}
		String contents = "public class X {\n" +
				"void foo(Object o) {\n" +
				"	switch (o) {\n" +
			    "		case Integer i when (i.intValue() > 10)   -> System.out.println(\"Greater than 10 \");\n" +
			    "		case String s when s.equals(\"ff\")   -> System.out.println(s);\n" +
			    "		default       	-> System.out.println(o.toString());\n" +
			    "	}\n" +
			    "}\n" +
				"\n" +
				"}\n";
		this.workingCopy = getWorkingCopy("/Converter_19/src/X.java", true/*resolve*/);
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
		if (!isJRE20) {
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
		this.workingCopy = getWorkingCopy("/Converter_19/src/X.java", true/*resolve*/);
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
		if (!isJRE20) {
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
		this.workingCopy = getWorkingCopy("/Converter_19/src/X.java", true/*resolve*/);
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
		if (!isJRE20) {
			printJREError();
			return;
		}
		String contents = "public class X {\n" +
				"void foo(Object o) {\n" +
				"	switch (o) {\n" +
			    "		case Integer i  : System.out.println(i.toString());\n" +
			    "		case null, default    : System.out.println(o.toString());\n" +
			    "	}\n" +
			    "}\n" +
				"\n" +
				"}\n";
		this.workingCopy = getWorkingCopy("/Converter_19/src/X.java", true/*resolve*/);
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
		Expression exp = (Expression) caseDefault.expressions().get(0);
		assertEquals("Case Default Expression",exp.getNodeType() , ASTNode.NULL_LITERAL);
		exp = (Expression) caseDefault.expressions().get(1);
		assertEquals("Case Default Expression",exp.getNodeType() , ASTNode.CASE_DEFAULT_EXPRESSION);
	}

	public void testBug575250() throws CoreException {
		if (!isJRE20) {
			System.err.println("Test "+getName()+" requires a JRE 20");
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
		this.workingCopy = getWorkingCopy("/Converter_19/src/X.java", true/*resolve*/);
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

	public void testRecordPattern001() throws CoreException {
		if (!isJRE20) {
			printJREError();
			return;
		}
		String contents = "public class X {\n"
						+ "  static void print(Rectangle r) {\n"
						+ "    if (r instanceof (Rectangle(ColoredPoint(Point(int x, int y), Color c),\n"
						+ "                               ColoredPoint lr))) {\n"
						+ "        System.out.println(\"Upper-left corner: \");\n"
						+ "        System.out.println(lr.toString());\n"
						+ "    }\n"
						+ "  }\n"
						+ "  public static void main(String[] obj) {\n"
						+ "    print(new Rectangle(new ColoredPoint(new Point(0, 0), Color.BLUE), \n"
						+ "                               new ColoredPoint(new Point(10, 15), Color.RED)));\n"
						+ "  }\n"
						+ "}\n"
						+ "record Point(int x, int y) {}\n"
						+ "enum Color { RED, GREEN, BLUE }\n"
						+ "record ColoredPoint(Point p, Color c) {}\n"
						+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}";
		this.workingCopy = getWorkingCopy("/Converter_19/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = ((AbstractTypeDeclaration)compilationUnit.types().get(0));
		assertEquals("Not a Type Declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration type = (TypeDeclaration)node;
		MethodDeclaration[] methods = type.getMethods();
		assertEquals("Method list empty", 2, methods.length);
		MethodDeclaration printMethod = methods[0];
		assertEquals("Method name is not print", "print", printMethod.getName().toString());
		List<ASTNode> statements = printMethod.getBody().statements();
		IfStatement ifStatement = (IfStatement)statements.get(0);
		assertEquals("Not a PatternInstanceOf Expression", "org.eclipse.jdt.core.dom.PatternInstanceofExpression", ifStatement.getExpression().getClass().getName());
		PatternInstanceofExpression patternExpression = (PatternInstanceofExpression)ifStatement.getExpression();
		Pattern pattern = patternExpression.getPattern();
		assertNotNull("Pattern should not be null", pattern);
		assertEquals("Should be a record pattern", "org.eclipse.jdt.core.dom.RecordPattern", pattern.getClass().getName());
		RecordPattern recPattern = (RecordPattern) pattern;
		assertNull("Pattern name should be null", recPattern.getPatternName());
		List<Pattern> patterns = recPattern.patterns();
		assertEquals("Incorrect nested pattern size", 2, patterns.size());
		pattern = patterns.get(0);
		assertEquals("Should be a type pattern", "org.eclipse.jdt.core.dom.RecordPattern", pattern.getClass().getName());
		recPattern = (RecordPattern) pattern;
		assertNull("Pattern name should be null", recPattern.getPatternName());
		pattern = patterns.get(1);
		assertEquals("Should be a type pattern", "org.eclipse.jdt.core.dom.TypePattern", pattern.getClass().getName());
		TypePattern tPattern = (TypePattern) pattern;
		assertNotNull("Pattern name should not be null", tPattern.getPatternVariable());
		assertEquals("Incorrect pattern variable name", "lr", tPattern.getPatternVariable().getName().toString());
	}

	@SuppressWarnings("rawtypes")
	public void testRecordPattern002() throws CoreException {
		if (!isJRE20) {
			System.err.println("Test "+getName()+" requires a JRE 20");
			return;
		}
		String contents =  "public class X {\n"
				+ "  public static void print(Record r) {\n"
				+ "    int res = switch(r) {\n"
				+ "       case Record(int x) -> x ;\n"
				+ "        default -> 0;\n"
				+ "    }; \n"
				+ "    System.out.println(\"Returns: \");\n"
				+ "    System.out.println(res);\n"
				+ "  }\n"
				+ "  public static void main(String[] args) {\n"
				+ "    print(new Record(3));\n"
				+ "  }\n"
				+ "}\n"
				+ "record Record(int x) {}";

	this.workingCopy = getWorkingCopy("/Converter_19/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = ((AbstractTypeDeclaration)compilationUnit.types().get(0));
		assertEquals("Not a Type Declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration type = (TypeDeclaration)node;
		MethodDeclaration[] methods = type.getMethods();
		assertEquals("Method list empty", 2, methods.length);
		MethodDeclaration printMethod = methods[0];
		assertEquals("Method name is not print", "print", printMethod.getName().toString());
		List<ASTNode> statements = printMethod.getBody().statements();
		VariableDeclarationStatement switchCasestatement = (VariableDeclarationStatement)statements.get(0);
		List fragments = switchCasestatement.fragments();
		assertEquals("Incorrect no of fragments", 1, fragments.size());
		node = (ASTNode) fragments.get(0);
		assertEquals("Switch statement", node.getNodeType(), ASTNode.VARIABLE_DECLARATION_FRAGMENT);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) node;
		Expression initializer = fragment.getInitializer();
		assertEquals("incorrect type", ASTNode.SWITCH_EXPRESSION, initializer.getNodeType());
		List switchStatements = ((SwitchExpression) initializer).statements();

		SwitchCase caseStmt = (SwitchCase) switchStatements.get(0);
		assertEquals("incorrect type", ASTNode.RECORD_PATTERN, ((Expression)caseStmt.expressions().get(0)).getNodeType());
		RecordPattern recordPattern = (RecordPattern)caseStmt.expressions().get(0);
		assertEquals("StartPosition of Record Pattern is not 94",94 , recordPattern.getStartPosition());
		assertEquals("Length of Record Pattern is not 13",13 , recordPattern.getLength());
		assertEquals("Type of RecordPattern variable is not Record","Record" , recordPattern.getPatternType().toString());
		assertEquals("StartPosition of Type variable is not 94",94 , recordPattern.getPatternType().getStartPosition());
		assertEquals("Length of Record Pattern is not 6",6 , recordPattern.getPatternType().getLength());
		assertNull("Name of RecordPattern variableis not null", recordPattern.getPatternName());
		assertEquals("Type of Nested pattern in RecordPattern is not TypePattern",ASTNode.TYPE_PATTERN , recordPattern.patterns().get(0).getNodeType());
	}

	@SuppressWarnings("rawtypes")
	public void testRecordPattern003() throws CoreException {
		if (!isJRE20) {
			System.err.println("Test "+getName()+" requires a JRE 20");
			return;
		}
		String contents =  "public class X {\n"
				+ "  public static void printLowerRight(Rectangle r) {\n"
				+ "    int res = switch(r) {\n"
				+ "       case Rectangle(ColoredPoint(Point(int x, int y), Color c),\n"
				+ "                               ColoredPoint lr) -> {\n"
				+ "        		yield 1;  \n"
				+ "        } \n"
				+ "        default -> 0;\n"
				+ "    }; \n"
				+ "    System.out.println(res);\n"
				+ "  }\n"
				+ "  public static void main(String[] args) {\n"
				+ "    printLowerRight(new Rectangle(new ColoredPoint(new Point(15, 5), Color.BLUE), \n"
				+ "        new ColoredPoint(new Point(30, 10), Color.RED)));\n"
				+ "  }\n"
				+ "}\n"
				+ "record Point(int x, int y) {}\n"
				+ "enum Color { RED, GREEN, BLUE }\n"
				+ "record ColoredPoint(Point p, Color c) {}\n"
				+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}";

	this.workingCopy = getWorkingCopy("/Converter_19/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = ((AbstractTypeDeclaration)compilationUnit.types().get(0));
		assertEquals("Not a Type Declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration type = (TypeDeclaration)node;
		MethodDeclaration[] methods = type.getMethods();
		assertEquals("Method list empty", 2, methods.length);
		MethodDeclaration printMethod = methods[0];
		assertEquals("Method name is not printLowerRight", "printLowerRight", printMethod.getName().toString());
		List<ASTNode> statements = printMethod.getBody().statements();
		VariableDeclarationStatement switchCasestatement = (VariableDeclarationStatement)statements.get(0);
		List fragments = switchCasestatement.fragments();
		assertEquals("Incorrect no of fragments", 1, fragments.size());
		node = (ASTNode) fragments.get(0);
		assertEquals("Switch statement", node.getNodeType(), ASTNode.VARIABLE_DECLARATION_FRAGMENT);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) node;
		Expression initializer = fragment.getInitializer();
		assertEquals("incorrect type", ASTNode.SWITCH_EXPRESSION, initializer.getNodeType());
		List switchStatements = ((SwitchExpression) initializer).statements();

		SwitchCase caseStmt = (SwitchCase) switchStatements.get(0);
		assertEquals("incorrect type", ASTNode.RECORD_PATTERN, ((Expression)caseStmt.expressions().get(0)).getNodeType());
		RecordPattern recordPattern = (RecordPattern)caseStmt.expressions().get(0);
		assertEquals("Type of RecordPattern variable is not Rectangle","Rectangle" , recordPattern.getPatternType().toString());
		assertNull("Name of RecordPattern variable is not null", recordPattern.getPatternName());
		assertEquals("There should be 2 nested Patterns in Rectangle", 2 , recordPattern.patterns().size());
		assertEquals("Type of Nested pattern in Rectangle is not RecordPattern",ASTNode.RECORD_PATTERN , recordPattern.patterns().get(0).getNodeType());
		assertEquals("Type of Nested pattern in Rectangle is not TypePattern",ASTNode.TYPE_PATTERN , recordPattern.patterns().get(1).getNodeType());
		RecordPattern recordPattern1 = (RecordPattern)recordPattern.patterns().get(0);
		assertEquals("Type of RecordPattern variable is not ColoredPoint","ColoredPoint" , recordPattern1.getPatternType().toString());
		assertEquals("Name of RecordPattern variable is not null", null , recordPattern1.getPatternName());
		assertEquals("There should be 2 nested Patterns in ColoredPoint", 2 , recordPattern1.patterns().size());
		assertEquals("Type of Nested pattern in ColoredPoint is not RecordPattern",ASTNode.RECORD_PATTERN , recordPattern1.patterns().get(0).getNodeType());
		assertEquals("Type of Nested pattern in ColoredPoint is not TypePattern",ASTNode.TYPE_PATTERN , recordPattern1.patterns().get(1).getNodeType());
		RecordPattern recordPattern2 = (RecordPattern)recordPattern1.patterns().get(0);
		assertEquals("Type of RecordPattern variable is not Point","Point" , recordPattern2.getPatternType().toString());
		assertEquals("Name of RecordPattern variable is not null", null , recordPattern2.getPatternName());
		assertEquals("There should be 2 nested Patterns in Point", 2 , recordPattern2.patterns().size());
		assertEquals("Type of Nested pattern in Point is not TypePattern",ASTNode.TYPE_PATTERN , recordPattern2.patterns().get(0).getNodeType());
		assertEquals("Type of Nested pattern in Point is not TypePattern",ASTNode.TYPE_PATTERN , recordPattern2.patterns().get(1).getNodeType());

	}

	@SuppressWarnings("rawtypes")
	public void testRecordPattern004() throws CoreException {
		if (!isJRE20) {
			System.err.println("Test "+getName()+" requires a JRE 20");
			return;
		}
		String contents =  "public class X {\n"
				+ "  public static void printLowerRight(Rectangle r) {\n"
				+ "    int res = switch(r) {\n"
				+ "       case Rectangle(ColoredPoint(Point(int x, int y), Color c),\n"
				+ "                               ColoredPoint lr) when x > 0 -> {\n"
				+ "        		yield 1;  \n"
				+ "        } \n"
				+ "        default -> 0;\n"
				+ "    }; \n"
				+ "    System.out.println(res);\n"
				+ "  }\n"
				+ "  public static void main(String[] args) {\n"
				+ "    printLowerRight(new Rectangle(new ColoredPoint(new Point(15, 5), Color.BLUE), \n"
				+ "        new ColoredPoint(new Point(30, 10), Color.RED)));\n"
				+ "  }\n"
				+ "}\n"
				+ "record Point(int x, int y) {}\n"
				+ "enum Color { RED, GREEN, BLUE }\n"
				+ "record ColoredPoint(Point p, Color c) {}\n"
				+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}";

	this.workingCopy = getWorkingCopy("/Converter_19/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = ((AbstractTypeDeclaration)compilationUnit.types().get(0));
		assertEquals("Not a Type Declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration type = (TypeDeclaration)node;
		MethodDeclaration[] methods = type.getMethods();
		assertEquals("Method list empty", 2, methods.length);
		MethodDeclaration printMethod = methods[0];
		assertEquals("Method name is not printLowerRight", "printLowerRight", printMethod.getName().toString());
		List<ASTNode> statements = printMethod.getBody().statements();
		VariableDeclarationStatement switchCasestatement = (VariableDeclarationStatement)statements.get(0);
		List fragments = switchCasestatement.fragments();
		assertEquals("Incorrect no of fragments", 1, fragments.size());
		node = (ASTNode) fragments.get(0);
		assertEquals("Switch statement", node.getNodeType(), ASTNode.VARIABLE_DECLARATION_FRAGMENT);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) node;
		Expression initializer = fragment.getInitializer();
		assertEquals("incorrect type", ASTNode.SWITCH_EXPRESSION, initializer.getNodeType());
		List switchStatements = ((SwitchExpression) initializer).statements();

		SwitchCase caseStmt = (SwitchCase) switchStatements.get(0);
		assertEquals("incorrect type", ASTNode.GUARDED_PATTERN, ((Expression)caseStmt.expressions().get(0)).getNodeType());
		GuardedPattern guardedPattern = (GuardedPattern)caseStmt.expressions().get(0);
		assertEquals("There should be 1 Record Pattern", ASTNode.RECORD_PATTERN , guardedPattern.getPattern().getNodeType());
	}
	public void testIssue882_1() throws Exception {
		if (!isJRE20) {
			System.err.println("Test "+getName()+" requires a JRE 20");
			return;
		}
		try {
			disablePreview();
			String contents =  "import java.util.List;\n"
					+ "public class X {\n"
					+ "	public static void foo(List<R> rList) {\n"
					+ "		for(R(Integer abcs):rList) {\n"
					+ "			System.out.println(abcs);\n"
					+ "		}\n"
					+ "	}\n"
					+ "	record R(int i) {}\n"
					+ "}";

		this.workingCopy = getWorkingCopy("/Converter_19/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				this.workingCopy,
				false);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertProblemsSize(compilationUnit, 1, "Record Pattern is a preview feature and disabled by default. Use --enable-preview to enable");
			node = ((AbstractTypeDeclaration)compilationUnit.types().get(0));
			assertEquals("Not a Type Declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
			TypeDeclaration type = (TypeDeclaration)node;
			MethodDeclaration[] methods = type.getMethods();
			assertEquals("Method size incorrect", 1, methods.length);
			MethodDeclaration printMethod = methods[0];
			assertEquals("Method name is not foo", "foo", printMethod.getName().toString());
			List<ASTNode> statements = printMethod.getBody().statements();
			assertEquals("statement count incorrect", 1, statements.size());
			Statement stmt = (Statement) statements.get(0);
			assertEquals("Should be an empty statement", ASTNode.EMPTY_STATEMENT, stmt.getNodeType());
		} finally {
			enabledPreview();
		}
	}
	public void testIssue882_2() throws Exception {
		if (!isJRE20) {
			System.err.println("Test "+getName()+" requires a JRE 20");
			return;
		}
		try {
			disablePreview();
			String contents =  "public class X {\n"
					+ "	public static void foo(Object o) {\n"
					+ "		if(o instanceof R(Integer abcs)) {\n"
					+ "			System.out.println(\"\");\n"
					+ "		}\n"
					+ "	}\n"
					+ "	record R(int i) {}\n"
					+ "}";

		this.workingCopy = getWorkingCopy("/Converter_19/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(
				contents,
				this.workingCopy,
				false);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertProblemsSize(compilationUnit, 1, "Record Pattern is a preview feature and disabled by default. Use --enable-preview to enable");
			node = ((AbstractTypeDeclaration)compilationUnit.types().get(0));
			assertEquals("Not a Type Declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
			TypeDeclaration type = (TypeDeclaration)node;
			MethodDeclaration[] methods = type.getMethods();
			assertEquals("Method size incorrect", 1, methods.length);
			MethodDeclaration printMethod = methods[0];
			assertEquals("Method name is not foo", "foo", printMethod.getName().toString());
			List<ASTNode> statements = printMethod.getBody().statements();
			assertEquals("statement count incorrect", 1, statements.size());
			Statement stmt = (Statement) statements.get(0);
			assertEquals("Should be an IF statement", ASTNode.IF_STATEMENT, stmt.getNodeType());
			IfStatement ifStmt = (IfStatement) stmt;
			Expression expression = ifStmt.getExpression();
			assertEquals("Should be an instanceof expression", ASTNode.PATTERN_INSTANCEOF_EXPRESSION, expression.getNodeType());
			// Doesn't matter what else goes inside the PatternInstanceofExpression, because it's broken code and
			// nothing is promised
		} finally {
			enabledPreview();
		}
	}
}
