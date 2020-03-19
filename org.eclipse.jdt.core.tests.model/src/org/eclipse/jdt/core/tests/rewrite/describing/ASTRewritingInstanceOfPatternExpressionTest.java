/*******************************************************************************
 * Copyright (c) 2020 IBM Corporation and others.
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

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import junit.framework.Test;

public class ASTRewritingInstanceOfPatternExpressionTest extends ASTRewritingTest {


	public ASTRewritingInstanceOfPatternExpressionTest(String name) {
		super(name, 14);
	}

	public ASTRewritingInstanceOfPatternExpressionTest(String name, int apiLevel) {
		super(name, apiLevel);
	}

	public static Test suite() {
		return createSuite(ASTRewritingInstanceOfPatternExpressionTest.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (this.apiLevel == AST.JLS14 ) {
			this.project1.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_14);
			this.project1.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_14);
			this.project1.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_14);
			this.project1.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
		}
	}

	@SuppressWarnings({ "rawtypes", "deprecation" })
	public void test001() throws Exception {
		if (this.apiLevel != 14) {
			System.err.println("Test "+getName()+" requires a JRE 14");
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
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
			InstanceofExpression instanceOfExpression = ast.newInstanceofExpression();
			instanceOfExpression.setLeftOperand(ast.newSimpleName("o"));//$NON-NLS-1$
			SimpleType simpleType = ast.newSimpleType(ast.newSimpleName("String"));//$NON-NLS-1$
			instanceOfExpression.setRightOperand(simpleType);
			SingleVariableDeclaration patternVariable = ast.newSingleVariableDeclaration();
			patternVariable.setName(ast.newSimpleName("s"));
			patternVariable.setType(ast.newSimpleType(ast.newSimpleName("String")));//$NON-NLS-1$
			instanceOfExpression.setPatternVariable(patternVariable);
			ifStatement.setExpression(instanceOfExpression);
			ifStatement.setThenStatement(ast.newEmptyStatement());
			rewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY).insertLast(ifStatement, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class X {\n");
		buf.append("    void foo(Object o) {\n");
		buf.append("        if (o instanceof String s)\n");
		buf.append("            ;\n");
		buf.append(" 	}\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}
}
