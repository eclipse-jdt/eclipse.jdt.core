/*******************************************************************************
 * Copyright (c) 2020, 2021 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.rewrite.describing;

import java.util.List;
import junit.framework.Test;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

public class ASTRewritingInstanceOfPatternExpressionTest extends ASTRewritingTest {


	public ASTRewritingInstanceOfPatternExpressionTest(String name, int apiLevel) {
		super(name, apiLevel);
	}

	public static Test suite() {
		return createSuite(ASTRewritingInstanceOfPatternExpressionTest.class, 16);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (this.apiLevel == AST.JLS16 ) {
			this.project1.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_16);
			this.project1.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_16);
			this.project1.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_16);
		}
	}

	@SuppressWarnings({ "rawtypes", "deprecation" })
	public void test001() throws Exception {
		if (this.apiLevel != 16) {
			System.err.println("Test "+getName()+" requires a JRE 16");
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class X {\n");
		buf.append("    void foo(Object o) {\n");
		buf.append(" 	}\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List blockStatements= block.statements();
		assertTrue("Number of statements not 0", blockStatements.size() == 0);
		{ // add InstanceOfPattern expression
			IfStatement ifStatement= ast.newIfStatement();
			PatternInstanceofExpression instanceOfExpression = ast.newPatternInstanceofExpression();
			instanceOfExpression.setLeftOperand(ast.newSimpleName("o"));//$NON-NLS-1$
			SingleVariableDeclaration singleVariableDeclaration = ast.newSingleVariableDeclaration();
			singleVariableDeclaration.setType(ast.newSimpleType(ast.newSimpleName("String")));//$NON-NLS-1$
			singleVariableDeclaration.setName(ast.newSimpleName("s"));
			instanceOfExpression.setRightOperand(singleVariableDeclaration);
			ifStatement.setExpression(instanceOfExpression);
			ifStatement.setThenStatement(ast.newEmptyStatement());
			rewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY).insertLast(ifStatement, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class X {\n");
		buf.append("    void foo(Object o) {\n");
		buf.append("        if (o instanceof String s)\n");
		buf.append("            ;\n");
		buf.append(" 	}\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}

	@SuppressWarnings({ "rawtypes", "deprecation" })
	public void test002() throws Exception {
		if (this.apiLevel != 16) {
			System.err.println("Test "+getName()+" requires a JRE 16");
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class X {\n");
		buf.append("    void foo(Object o) {\n");
		buf.append("        if (o instanceof String s)\n");
		buf.append("            ;\n");
		buf.append(" 	}\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List blockStatements= block.statements();
		assertTrue("Number of statements not 1", blockStatements.size() == 1);
		{ // add InstanceOfPattern expression

			IfStatement ifStatement = (IfStatement)blockStatements.get(0);
			PatternInstanceofExpression instanceOfExpression = (PatternInstanceofExpression)ifStatement.getExpression();
			SingleVariableDeclaration singleVariableDeclaration = ast.newSingleVariableDeclaration();
			singleVariableDeclaration.setType(ast.newSimpleType(ast.newSimpleName("String")));//$NON-NLS-1$
			singleVariableDeclaration.setName(ast.newSimpleName("str1"));
			rewrite.set(instanceOfExpression, PatternInstanceofExpression.RIGHT_OPERAND_PROPERTY, singleVariableDeclaration, null);

		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class X {\n");
		buf.append("    void foo(Object o) {\n");
		buf.append("        if (o instanceof String str1)\n");
		buf.append("            ;\n");
		buf.append(" 	}\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}

	@SuppressWarnings({ "rawtypes", "deprecation" })
	public void test003() throws Exception {
		if (this.apiLevel != 16) {
			System.err.println("Test "+getName()+" requires a JRE 16");
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class X {\n");
		buf.append("    void foo(Object o) {\n");
		buf.append("        if (o instanceof String s)\n");
		buf.append("            ;\n");
		buf.append(" 	}\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List blockStatements= block.statements();
		assertTrue("Number of statements not 1", blockStatements.size() == 1);
		{ // change left side
			IfStatement ifStatement = (IfStatement)blockStatements.get(0);
			PatternInstanceofExpression expr = (PatternInstanceofExpression)ifStatement.getExpression();

			SimpleName name= ast.newSimpleName("x");
			rewrite.replace(expr.getLeftOperand(), name, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class X {\n");
		buf.append("    void foo(Object o) {\n");
		buf.append("        if (x instanceof String s)\n");
		buf.append("            ;\n");
		buf.append(" 	}\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}
}
