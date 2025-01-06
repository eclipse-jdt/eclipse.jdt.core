/*******************************************************************************
 * Copyright (c) 2022, 2024 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.rewrite.describing;

import java.util.List;
import junit.framework.Test;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

@SuppressWarnings({"rawtypes", "deprecation"})
public class ASTRewritingRecordPatternTest extends ASTRewritingTest {


	public ASTRewritingRecordPatternTest(String name, int apiLevel) {
		super(name, apiLevel);
	}

	public static Test suite() {
		return createSuite(ASTRewritingRecordPatternTest.class, 21);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		setUpProjectAbove21();
	}

	private boolean checkAPILevel() {
		if (this.apiLevel < 21) {
			System.err.println("Test "+getName()+" requires a JRE 21");
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
			TypePattern tPattern = ast.newTypePattern();
			SingleVariableDeclaration patternVariable = ast.newSingleVariableDeclaration();
			patternVariable.setType(ast.newSimpleType(ast.newSimpleName("Rectangle")));
			patternVariable.setName(ast.newSimpleName("r1"));

			if (this.apiLevel < AST.JLS22) {
				tPattern.setPatternVariable(patternVariable);
			} else {
				tPattern.setPatternVariable((VariableDeclaration) patternVariable);
			}

			caseStatement.expressions().add(tPattern);
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


	public void testModifyRecordSwitchPattern() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String buf =  "public class X {\n"
				+ "  public static void printLowerRight(Rectangle r) {\n"
				+ "    int res = switch(r) {\n"
				+ "        case Rectangle(ColoredPoint clr) -> {\n"
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
		assertEquals("Incorrect number of statements",1, blockStatements.size());
		{ // Modify Record pattern
			VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement)blockStatements.get(0);

			VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment)variableDeclarationStatement.fragments().get(0);
			SwitchExpression switchExpression= (SwitchExpression) variableDeclarationFragment.getInitializer();
			List statements= switchExpression.statements();
			SwitchCase caseStatement = (SwitchCase)statements.get(0);
			RecordPattern recordPatternR = (RecordPattern)(caseStatement.expressions().get(0));
			TypePattern typePattern = ast.newTypePattern();
			SingleVariableDeclaration variableDeclaration = ast.newSingleVariableDeclaration();
			variableDeclaration.setType(ast.newSimpleType(ast.newSimpleName("ColoredPoint")));
			variableDeclaration.setName(ast.newSimpleName("clr1"));

			if (this.apiLevel < AST.JLS22) {
				typePattern.setPatternVariable(variableDeclaration);
			} else {
				typePattern.setPatternVariable((VariableDeclaration) variableDeclaration);
			}

			ListRewrite listRewrite= rewrite.getListRewrite(recordPatternR, RecordPattern.PATTERNS_PROPERTY);
			listRewrite.insertAt(typePattern, 0, null);
		}

		String preview= evaluateRewrite(cu, rewrite);
		buf =  "public class X {\n"
				+ "  public static void printLowerRight(Rectangle r) {\n"
				+ "    int res = switch(r) {\n"
				+ "        case Rectangle(ColoredPoint clr1, ColoredPoint clr) -> {\n"
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


	public void testAddRecordInstanceOfPattern() throws Exception {
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
			if (this.apiLevel < AST.JLS22) {
				typePattern.setPatternVariable(patternVariable);
			} else {
				typePattern.setPatternVariable((VariableDeclaration) patternVariable);
			}

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

	public void testModifyGuardedPattern() throws Exception {
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

			if (this.apiLevel < AST.JLS22) {
				typePattern.setPatternVariable(patternVariable);
			} else {
				typePattern.setPatternVariable((VariableDeclaration) patternVariable);
			}

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

	public void testRemoveRecordInstanceOfPattern() throws Exception {
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

	public void testModifyRecordInstanceOfPattern() throws Exception {
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

	public void testModifyTypePattern_a() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		String contents = """
					public class X {
					    protected String getString(Number number) {
					        if (number instanceof Long n) {
					            return n.toString();
					        }
					        if (number instanceof Float n) {
					            return n.toString();
					        }
					        if (number instanceof Double n) {
					            return n.toString();
					        }
					        if (number instanceof Float n && n.isInfinite()) {
					            return "Inf"; //$NON-NLS-1$
					        }
					        if (number instanceof Double m && m.isInfinite()) {
                            }
					        return null;
					    }
					}
				""";
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", contents, false, null);

		CompilationUnit astRoot= createAST(this.apiLevel, cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "getString");
		Block block= methodDecl.getBody();
		List blockStatements= block.statements();
		assertTrue("Number of statements not 1", blockStatements.size() == 6);
		{
			PatternInstanceofExpression pia1 = ast.newPatternInstanceofExpression();
			pia1.setLeftOperand(ast.newSimpleName("number"));

			SingleVariableDeclaration svd1 = ast.newSingleVariableDeclaration();
			svd1.setType(ast.newSimpleType(ast.newSimpleName("Float")));
			svd1.setName(ast.newSimpleName("n"));

			TypePattern tp1 = ast.newTypePattern();
			if(this.apiLevel < AST.JLS22) {
				tp1.setPatternVariable(svd1);
			} else {
				tp1.setPatternVariable((VariableDeclaration) svd1);
			}
			pia1.setPattern(tp1);


			InfixExpression ie1 = ast.newInfixExpression();
			ie1.setLeftOperand(pia1);

			MethodInvocation mi1 = ast.newMethodInvocation();
			mi1.setName(ast.newSimpleName("isInfinite"));
			mi1.setExpression(ast.newSimpleName("n"));

			ie1.setRightOperand(mi1);
			ie1.setOperator(InfixExpression.Operator.CONDITIONAL_AND);

			ParenthesizedExpression pe1 = ast.newParenthesizedExpression();
			pe1.setExpression(ie1);



			PatternInstanceofExpression pia2 = ast.newPatternInstanceofExpression();
			pia2.setLeftOperand(ast.newSimpleName("number"));

			SingleVariableDeclaration svd2 = ast.newSingleVariableDeclaration();
			svd2.setType(ast.newSimpleType(ast.newSimpleName("Double")));
			svd2.setName(ast.newSimpleName("n"));

			TypePattern tp2 = ast.newTypePattern();
			if(this.apiLevel < AST.JLS22) {
				tp2.setPatternVariable(svd2);
			} else {
				tp2.setPatternVariable((VariableDeclaration) svd2);
			}
			pia2.setPattern(tp2);


			InfixExpression ie2 = ast.newInfixExpression();
			ie2.setLeftOperand(pia2);

			MethodInvocation mi2 = ast.newMethodInvocation();
			mi2.setName(ast.newSimpleName("isInfinite"));
			mi2.setExpression(ast.newSimpleName("n"));

			ie2.setRightOperand(mi2);
			ie2.setOperator(InfixExpression.Operator.CONDITIONAL_AND);

			ParenthesizedExpression pe2 = ast.newParenthesizedExpression();
			pe2.setExpression(ie2);

			InfixExpression ieMain = ast.newInfixExpression();
			ieMain.setLeftOperand(pe1);
			ieMain.setRightOperand(pe2);
			ieMain.setOperator(InfixExpression.Operator.CONDITIONAL_OR);
			IfStatement ifStatement = ast.newIfStatement();
			ifStatement.setExpression(ieMain);


			rewrite.remove((ASTNode) blockStatements.get(3), null);

			rewrite.replace((ASTNode) blockStatements.get(4), ifStatement, null);
		}

		String modifiedString = """
					public class X {
					    protected String getString(Number number) {
					        if (number instanceof Long n) {
					            return n.toString();
					        }
					        if (number instanceof Float n) {
					            return n.toString();
					        }
					        if (number instanceof Double n) {
					            return n.toString();
					        }
					        if ((number instanceof Float n && n.isInfinite()) || (number instanceof Double n && n.isInfinite())) {
                }
					        return null;
					    }
					}
				""";
		String preview= evaluateRewrite(cu, rewrite);
		assertEqualString(preview, modifiedString);

	}

	public void testModifyTypePattern_b() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);

		String content = """
					public class X {
				        public void foo(Object x) {
				            if (x instanceof E1.InternalStaticClass) { // comment 1
				                System.out.println("xxx");
				            }
				        }
				    }
				""";

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", content, false, null);

		CompilationUnit astRoot= createAST(this.apiLevel, cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List blockStatements= block.statements();
		assertTrue("Number of statements not 1", blockStatements.size() == 1);

		{
			IfStatement ifStatement = (IfStatement) blockStatements.get(0);
			Expression ie = ifStatement.getExpression();

			SingleVariableDeclaration svd = ast.newSingleVariableDeclaration();
			svd.setName(ast.newSimpleName("t"));

			QualifiedName qn = ast.newQualifiedName(ast.newSimpleName("E1"), ast.newSimpleName("InternalStaticClass"));
			svd.setType(ast.newSimpleType(qn));

			TypePattern tp = ast.newTypePattern();
			if(this.apiLevel < AST.JLS22) {
				tp.setPatternVariable(svd);
			} else {
				tp.setPatternVariable((VariableDeclaration)svd);
			}

			PatternInstanceofExpression pie = ast.newPatternInstanceofExpression();
			pie.setPattern(tp);
			pie.setLeftOperand(ast.newSimpleName("x"));

			rewrite.replace(ie, pie, null);

		}

		String modifiedString = """
					public class X {
				        public void foo(Object x) {
				            if (x instanceof E1.InternalStaticClass t) { // comment 1
				                System.out.println("xxx");
				            }
				        }
				    }
				""";

		String preview= evaluateRewrite(cu, rewrite);
		assertEqualString(preview, modifiedString);
	}

}
