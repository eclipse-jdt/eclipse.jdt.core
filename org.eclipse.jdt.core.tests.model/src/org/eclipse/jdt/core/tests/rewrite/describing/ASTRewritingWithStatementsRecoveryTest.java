/*******************************************************************************
 * Copyright (c) 2013, 2015 IBM Corporation and others.
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

@SuppressWarnings("rawtypes")
public class ASTRewritingWithStatementsRecoveryTest extends ASTRewritingTest {

	public ASTRewritingWithStatementsRecoveryTest(String name) {
		super(name);
	}

	public ASTRewritingWithStatementsRecoveryTest(String name, int apiLevel) {
		super(name, apiLevel);
	}

	public static Test suite() {
		return createSuite(ASTRewritingWithStatementsRecoveryTest.class);
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=272711
	public void testBug272711_01_since_3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        this.foo#3);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu, true);
		AST ast= astRoot.getAST();
		ASTRewrite rewrite= ASTRewrite.create(ast);

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List statements= block.statements();
		assertTrue("Number of statements not 1", statements.size() == 1);
		{ // add type arguments
			ExpressionStatement stmt= (ExpressionStatement) statements.get(0);
			MethodInvocation invocation= (MethodInvocation) stmt.getExpression();
			ASTNode firstArgument = (ASTNode) invocation.arguments().get(0);
			NumberLiteral newNumberLiteral = ast.newNumberLiteral("0");
			ListRewrite listRewriter= rewrite.getListRewrite(invocation, MethodInvocation.ARGUMENTS_PROPERTY);
			listRewriter.replace(firstArgument, newNumberLiteral, null);
		}
		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        this.foo#0);\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=272711
	public void testBug272711_02_since_3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        throws new UnsupportedOperationException();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu, true);
		AST ast= astRoot.getAST();
		ASTRewrite rewrite= ASTRewrite.create(ast);

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		List bodyDeclarations = type.bodyDeclarations();
		assertTrue("Number of body declarations not 1", bodyDeclarations.size() == 1);
		{ // add field declaration
			MethodDeclaration methodDeclaration =  (MethodDeclaration)bodyDeclarations.get(0);
			VariableDeclarationFragment newFragment = ast.newVariableDeclarationFragment();
			newFragment.setName(ast.newSimpleName("field"));
			FieldDeclaration newFieldDeclaration = ast.newFieldDeclaration(newFragment);
			newFieldDeclaration.setType(ast.newSimpleType(ast.newName("Object")));
			ListRewrite listRewriter= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
			listRewriter.insertBefore(newFieldDeclaration, methodDeclaration, null);
		}
		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    Object field;\n");
		buf.append("\n");
		buf.append("    public void foo() {\n");
		buf.append("        throws new UnsupportedOperationException();\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=272711
	public void testBug272711_03_since_3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        do {\n");
		buf.append("        } (a);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu, true);
		AST ast= astRoot.getAST();
		ASTRewrite rewrite= ASTRewrite.create(ast);

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List statements= block.statements();
		assertTrue("Number of statements not 1", statements.size() == 1);
		{ // replace the 'a' simple name with another simple name
			DoStatement stmt= (DoStatement) statements.get(0);
			Statement body = stmt.getBody();
			EmptyStatement newEmptyStatement = ast.newEmptyStatement();
			rewrite.replace(body, newEmptyStatement, null);
		}
		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        do\n");
		buf.append("            ;  (a);\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

}
