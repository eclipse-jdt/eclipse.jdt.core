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

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PatternInstanceofExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import junit.framework.Test;

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
		String str = """
			package test1;
			public class X {
			    void foo(Object o) {
			 	}
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", str, false, null);

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

		String str1 = """
			package test1;
			public class X {
			    void foo(Object o) {
			        if (o instanceof String s)
			            ;
			 	}
			}
			""";
		assertEqualString(preview, str1);

	}

	@SuppressWarnings({ "rawtypes", "deprecation" })
	public void test002() throws Exception {
		if (this.apiLevel != 16) {
			System.err.println("Test "+getName()+" requires a JRE 16");
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			public class X {
			    void foo(Object o) {
			        if (o instanceof String s)
			            ;
			 	}
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", str, false, null);

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

		String str1 = """
			package test1;
			public class X {
			    void foo(Object o) {
			        if (o instanceof String str1)
			            ;
			 	}
			}
			""";
		assertEqualString(preview, str1);

	}

	@SuppressWarnings({ "rawtypes", "deprecation" })
	public void test003() throws Exception {
		if (this.apiLevel != 16) {
			System.err.println("Test "+getName()+" requires a JRE 16");
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			public class X {
			    void foo(Object o) {
			        if (o instanceof String s)
			            ;
			 	}
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", str, false, null);

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

		String str1 = """
			package test1;
			public class X {
			    void foo(Object o) {
			        if (x instanceof String s)
			            ;
			 	}
			}
			""";
		assertEqualString(preview, str1);

	}
}
