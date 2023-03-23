/*******************************************************************************
 * Copyright (c) 2022, 2023 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.rewrite.describing;

import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.GuardedPattern;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullPattern;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.RecordPattern;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchExpression;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypePattern;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.YieldStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import junit.framework.Test;

@SuppressWarnings({"rawtypes", "deprecation"})
public class ASTRewritingRecordPatternTest extends ASTRewritingTest {


	public ASTRewritingRecordPatternTest(String name, int apiLevel) {
		super(name, apiLevel);
	}

	public static Test suite() {
		return createSuite(ASTRewritingRecordPatternTest.class, 20);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (this.apiLevel == AST.JLS20 ) { // Remove this after it is a standard feature
			this.project1.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_20);
			this.project1.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_20);
			this.project1.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_20);
			this.project1.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
		}
	}

	private boolean checkAPILevel() {
		if (this.apiLevel < 20) {
			System.err.println("Test "+getName()+" requires a JRE 20");
			return true;
		}
		return false;
	}


	public void testAddRecordSwitchPattern() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String buf =  "public class X {\n"
				+ "  public static void printLowerRight(Rectangle r) {\n"
				+ "    int res = switch(r) {\n"
				+ "        default -> 0;\n"
				+ "    }; \n"
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

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(this.apiLevel, cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "printLowerRight");
		Block block= methodDecl.getBody();
		List blockStatements= block.statements();
		assertTrue("Number of statements not 1", blockStatements.size() == 1);
		{ // insert record pattern
			VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement)blockStatements.get(0);

			VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment)variableDeclarationStatement.fragments().get(0);
			SwitchExpression switchExpression= (SwitchExpression) variableDeclarationFragment.getInitializer();
			List statements= switchExpression.statements();
			assertTrue("Number of statements not 2", statements.size() == 2);

			SwitchCase caseStatement= ast.newSwitchCase();
			caseStatement.setSwitchLabeledRule(true);
			RecordPattern recordPattern = ast.newRecordPattern();
			recordPattern.setPatternName(ast.newSimpleName("r1"));
			recordPattern.setPatternType(ast.newSimpleType(ast.newSimpleName("Rectangle")));
			caseStatement.expressions().add(recordPattern);
			ListRewrite listRewrite= rewrite.getListRewrite(switchExpression, SwitchExpression.STATEMENTS_PROPERTY);
			listRewrite.insertAt(caseStatement, 0, null);
			Block block1 = ast.newBlock();
			YieldStatement yieldStatement = ast.newYieldStatement();
			yieldStatement.setExpression(ast.newNumberLiteral("1"));
			block1.statements().add(yieldStatement);
			listRewrite.insertAt(block1, 1, null);

		}

		String preview= evaluateRewrite(cu, rewrite);

		buf =  "public class X {\n"
				+ "  public static void printLowerRight(Rectangle r) {\n"
				+ "    int res = switch(r) {\n"
				+ "        case Rectangle r1 -> {\n"
				+ "    yield 1;\n"
				+ "}\n"
				+ "        default -> 0;\n"
				+ "    }; \n"
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
		assertEqualString(preview, buf.toString());
	}


	// TRACKED via https://github.com/eclipse-jdt/eclipse.jdt.core/issues/784
	public void _testModifyRecordSwitchPattern() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String buf =  "public class X {\n"
				+ "  public static void printLowerRight(Rectangle r) {\n"
				+ "    int res = switch(r) {\n"
				+ "        case Rectangle(ColoredPoint clr) r1 -> {\n"
				+ "				yield 1;\n"
				+ "			}\n"
				+ "        default -> 0;\n"
				+ "    }; \n"
				+ "  }\n"
				+ "  public static void main(String[] args) {\n"
				+ "    printLowerRight(new Rectangle(new ColoredPoint(new Point(15, 5), Color.BLUE)));\n"
				+ "  }\n"
				+ "}\n"
				+ "record Point(int x, int y) {}\n"
				+ "enum Color { RED, GREEN, BLUE }\n"
				+ "record ColoredPoint(Point p, Color c) {}\n"
				+ "record Rectangle(ColoredPoint upperLeft) {}";

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(this.apiLevel, cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "printLowerRight");
		Block block= methodDecl.getBody();
		List blockStatements= block.statements();
		assertTrue("Number of statements not 1", blockStatements.size() == 1);
		{ // Modify Record pattern
			VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement)blockStatements.get(0);

			VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment)variableDeclarationStatement.fragments().get(0);
			SwitchExpression switchExpression= (SwitchExpression) variableDeclarationFragment.getInitializer();
			List statements= switchExpression.statements();
			SwitchCase caseStatement = (SwitchCase)statements.get(0);
			RecordPattern recordPatternR = (RecordPattern)(caseStatement.expressions().get(0));
			RecordPattern recordPatternC = ast.newRecordPattern();
			recordPatternC.setPatternName(ast.newSimpleName("clr1"));
			recordPatternC.setPatternType(ast.newSimpleType(ast.newSimpleName("ColoredPoint")));
			ListRewrite listRewrite= rewrite.getListRewrite(recordPatternR, RecordPattern.PATTERNS_PROPERTY);
			listRewrite.insertAt(recordPatternC, 0, null);

		}

		String preview= evaluateRewrite(cu, rewrite);

		buf =  "public class X {\n"
				+ "  public static void printLowerRight(Rectangle r) {\n"
				+ "    int res = switch(r) {\n"
				+ "        case Rectangle(ColoredPoint clr1, ColoredPoint clr) r1 -> {\n"
				+ "				yield 1;\n"
				+ "			}\n"
				+ "        default -> 0;\n"
				+ "    }; \n"
				+ "  }\n"
				+ "  public static void main(String[] args) {\n"
				+ "    printLowerRight(new Rectangle(new ColoredPoint(new Point(15, 5), Color.BLUE)));\n"
				+ "  }\n"
				+ "}\n"
				+ "record Point(int x, int y) {}\n"
				+ "enum Color { RED, GREEN, BLUE }\n"
				+ "record ColoredPoint(Point p, Color c) {}\n"
				+ "record Rectangle(ColoredPoint upperLeft) {}";
		assertEqualString(preview, buf.toString());
	}

	public void testRemoveRecordSwitchPattern() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String buf =  "public class X {\n"
				+ "  public static void printLowerRight(Rectangle r) {\n"
				+ "    int res = switch(r) {\n"
				+ "        case Rectangle r1 -> {\n"
				+ "    yield 1;\n"
				+ "}\n"
				+ "        default -> 0;\n"
				+ "    }; \n"
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

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(this.apiLevel, cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "printLowerRight");
		Block block= methodDecl.getBody();
		List blockStatements= block.statements();
		assertTrue("Number of statements not 1", blockStatements.size() == 1);
		{ // insert record pattern
			VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement)blockStatements.get(0);

			VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment)variableDeclarationStatement.fragments().get(0);
			SwitchExpression switchExpression= (SwitchExpression) variableDeclarationFragment.getInitializer();
			List statements= switchExpression.statements();
			assertTrue("Number of statements not 1", statements.size() == 4);


			rewrite.remove((ASTNode) statements.get(0), null);
			rewrite.remove((ASTNode) statements.get(1), null);
		}


		String preview= evaluateRewrite(cu, rewrite);

		buf =  "public class X {\n"
				+ "  public static void printLowerRight(Rectangle r) {\n"
				+ "    int res = switch(r) {\n"
				+ "        default -> 0;\n"
				+ "    }; \n"
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

		assertEqualString(preview, buf.toString());
	}


	public void _testAddRecordInstanceOfPattern() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("public class X {\n");
		buf.append(		"void foo(Object o) {\n");
		buf.append(		"	switch (o) {\n");
		buf.append(	    "		default       	: System.out.println(\"0\");\n");
		buf.append(	    "	}\n");
		buf.append(	    "}\n");
		buf.append(		"\n");
		buf.append(		"}\n");

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(this.apiLevel, cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List blockStatements= block.statements();
		assertTrue("Number of statements not 1", blockStatements.size() == 1);
		{ // insert Guarded pattern
			SwitchStatement switchStatement = (SwitchStatement) blockStatements.get(0);

			List statements= switchStatement.statements();
			assertTrue("Number of statements not 2", statements.size() == 2);

			SwitchCase caseStatement= ast.newSwitchCase();
			caseStatement.setSwitchLabeledRule(false);
			GuardedPattern guardedPattern = ast.newGuardedPattern();
			TypePattern typePattern = ast.newTypePattern();
			SingleVariableDeclaration patternVariable = ast.newSingleVariableDeclaration();
			patternVariable.setType(ast.newSimpleType(ast.newSimpleName("Integer")));
			patternVariable.setName(ast.newSimpleName("i"));
			typePattern.setPatternVariable(patternVariable);
			guardedPattern.setPattern(typePattern);
			InfixExpression infixExpression = ast.newInfixExpression();
			infixExpression.setOperator(InfixExpression.Operator.GREATER);
			infixExpression.setLeftOperand(ast.newSimpleName("i"));
			infixExpression.setRightOperand(ast.newNumberLiteral("10"));//$NON-NLS
			guardedPattern.setExpression(infixExpression);
			caseStatement.expressions().add(guardedPattern);

			ListRewrite listRewrite= rewrite.getListRewrite(switchStatement, SwitchStatement.STATEMENTS_PROPERTY);
			listRewrite.insertAt(caseStatement, 0, null);

			MethodInvocation methodInvocation = ast.newMethodInvocation();
			QualifiedName name =
				ast.newQualifiedName(
				ast.newSimpleName("System"),//$NON-NLS-1$
				ast.newSimpleName("out"));//$NON-NLS-1$
			methodInvocation.setExpression(name);
			methodInvocation.setName(ast.newSimpleName("println")); //$NON-NLS-1$
			StringLiteral stringLiteral = ast.newStringLiteral();
			stringLiteral.setLiteralValue("Greater than 10");//$NON-NLS-1$
			methodInvocation.arguments().add(stringLiteral);//$NON-NLS-1$
			ExpressionStatement expressionStatement = ast.newExpressionStatement(methodInvocation);
			listRewrite.insertAt(expressionStatement, 1, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("public class X {\n");
		buf.append(		"void foo(Object o) {\n");
		buf.append(		"	switch (o) {\n");
		buf.append(	    "		case Integer i when i > 10:\n");
		buf.append(	    "            System.out.println(\"Greater than 10\");\n");
		buf.append(	    "        default       	: System.out.println(\"0\");\n");
		buf.append(	    "	}\n");
		buf.append(	    "}\n");
		buf.append(		"\n");
		buf.append(		"}\n");
		assertEqualString(preview, buf.toString());
	}

	public void _testModifyGuardedPattern() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("public class X {\n");
		buf.append(		"void foo(Object o) {\n");
		buf.append(		"	switch (o) {\n");
		buf.append(	    "       case String s when s.equals(\"hi\") : System.out.println(\"hi\");\n");
		buf.append(	    "		default       	: System.out.println(\"0\");\n");
		buf.append(	    "	}\n");
		buf.append(	    "}\n");
		buf.append(		"\n");
		buf.append(		"}\n");

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(this.apiLevel, cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List blockStatements= block.statements();
		assertTrue("Number of statements not 1", blockStatements.size() == 1);
		{ // replace Guarded pattern
			SwitchStatement switchStatement = (SwitchStatement) blockStatements.get(0);

			List statements= switchStatement.statements();
			assertTrue("Number of statements not 4", statements.size() == 4);

			SwitchCase caseStatement= (SwitchCase)statements.get(0);
			GuardedPattern guardedPattern = ast.newGuardedPattern();
			TypePattern typePattern = ast.newTypePattern();
			SingleVariableDeclaration patternVariable = ast.newSingleVariableDeclaration();
			patternVariable.setType(ast.newSimpleType(ast.newSimpleName("Integer")));
			patternVariable.setName(ast.newSimpleName("i"));
			typePattern.setPatternVariable(patternVariable);
			guardedPattern.setPattern(typePattern);
			InfixExpression infixExpression = ast.newInfixExpression();
			infixExpression.setOperator(InfixExpression.Operator.GREATER);
			infixExpression.setLeftOperand(ast.newSimpleName("i"));
			infixExpression.setRightOperand(ast.newNumberLiteral("10"));//$NON-NLS
			guardedPattern.setExpression(infixExpression);
			rewrite.replace((ASTNode) caseStatement.expressions().get(0),guardedPattern, null);

		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("public class X {\n");
		buf.append(		"void foo(Object o) {\n");
		buf.append(		"	switch (o) {\n");
		buf.append(	    "       case Integer i when i > 10 : System.out.println(\"hi\");\n");
		buf.append(	    "		default       	: System.out.println(\"0\");\n");
		buf.append(	    "	}\n");
		buf.append(	    "}\n");
		buf.append(		"\n");
		buf.append(		"}\n");
		assertEqualString(preview, buf.toString());
	}

	public void _testRemoveRecordInstanceOfPattern() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("public class X {\n");
		buf.append(		"void foo(Object o) {\n");
		buf.append(		"	int i = switch (o) {\n");
		buf.append(	    "		case Integer i when i > 10 : System.out.println(\"hi\");\n");
		buf.append(	    "       case String s when s.equals(\"hi\") : System.out.println(\"hi\");\n");
		buf.append(	    "		default       	-> 0;\n");
		buf.append(	    "	};\n");
		buf.append(	    "}\n");
		buf.append(		"\n");
		buf.append(		"}\n");

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(this.apiLevel, cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List blockStatements= block.statements();
		assertTrue("Number of statements not 1", blockStatements.size() == 1);
		{ // remove guarded pattern statement
			VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement)blockStatements.get(0);

			VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment)variableDeclarationStatement.fragments().get(0);
			SwitchExpression switchStatement= (SwitchExpression) variableDeclarationFragment.getInitializer();
			List statements= switchStatement.statements();
			assertTrue("Number of statements not 6", statements.size() == 6);

			// remove statements

			rewrite.remove((ASTNode) statements.get(0), null);
			rewrite.remove((ASTNode) statements.get(1), null);
		}


		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("public class X {\n");
		buf.append(		"void foo(Object o) {\n");
		buf.append(		"	int i = switch (o) {\n");
		buf.append(	    "		case String s when s.equals(\"hi\") : System.out.println(\"hi\");\n");
		buf.append(	    "		default       	-> 0;\n");
		buf.append(	    "	};\n");
		buf.append(	    "}\n");
		buf.append(		"\n");
		buf.append(		"}\n");
		assertEqualString(preview, buf.toString());
	}

	public void _testModifyRecordInstanceOfPattern() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("public class X {\n");
		buf.append(		"void foo(Object o) {\n");
		buf.append(		"	switch (o) {\n");
		buf.append(	    "		case Integer i -> System.out.println(\"Integer\");\n");
		buf.append(	    "		default       	-> 0;\n");
		buf.append(	    "	}\n");
		buf.append(	    "}\n");
		buf.append(		"\n");
		buf.append(		"}\n");

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(this.apiLevel, cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List blockStatements= block.statements();
		assertTrue("Number of statements not 1", blockStatements.size() == 1);
		{ // insert null pattern
			SwitchStatement switchStatement = (SwitchStatement) blockStatements.get(0);

			List statements= switchStatement.statements();
			assertTrue("Number of statements not 4", statements.size() == 4);

			SwitchCase caseStatement= ast.newSwitchCase();
			caseStatement.setSwitchLabeledRule(true);
			NullPattern nullPattern = ast.newNullPattern();
			caseStatement.expressions().add(nullPattern);

			ListRewrite listRewrite= rewrite.getListRewrite(switchStatement, SwitchStatement.STATEMENTS_PROPERTY);
			listRewrite.insertAt(caseStatement, 2, null);

			MethodInvocation methodInvocation = ast.newMethodInvocation();
			QualifiedName name =
				ast.newQualifiedName(
				ast.newSimpleName("System"),//$NON-NLS-1$
				ast.newSimpleName("out"));//$NON-NLS-1$
			methodInvocation.setExpression(name);
			methodInvocation.setName(ast.newSimpleName("println")); //$NON-NLS-1$
			StringLiteral stringLiteral = ast.newStringLiteral();
			stringLiteral.setLiteralValue("Null");//$NON-NLS-1$
			methodInvocation.arguments().add(stringLiteral);//$NON-NLS-1$
			ExpressionStatement expressionStatement = ast.newExpressionStatement(methodInvocation);
			listRewrite.insertAt(expressionStatement, 3, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("public class X {\n");
		buf.append(		"void foo(Object o) {\n");
		buf.append(		"	switch (o) {\n");
		buf.append(	    "		case Integer i -> System.out.println(\"Integer\");\n");
		buf.append(	    "        case null -> System.out.println(\"Null\");\n");
		buf.append(	    "		default       	-> 0;\n");
		buf.append(	    "	}\n");
		buf.append(	    "}\n");
		buf.append(		"\n");
		buf.append(		"}\n");
		assertEqualString(preview, buf.toString());
	}

}
