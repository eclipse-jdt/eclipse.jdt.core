/*******************************************************************************
 * Copyright (c) 2007, 2013 IBM Corporation and others.
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

import junit.framework.Test;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

public class ASTRewritingRevertTest extends ASTRewritingTest {

	public ASTRewritingRevertTest(String name) {
		super(name);
	}

	public ASTRewritingRevertTest(String name, int apiLevel) {
		super(name, apiLevel);
	}

	public static Test suite() {
		return createSuite(ASTRewritingRevertTest.class);
	}

	public void testRemoveInserted_since_3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		{
			// revert inserted node
			PrimitiveType newType= ast.newPrimitiveType(PrimitiveType.INT);

			rewrite.set(methodDecl, MethodDeclaration.RETURN_TYPE2_PROPERTY, newType, null);
			rewrite.remove(newType, null);

		}
		{
			// revert inserted list child
			SingleVariableDeclaration newParam= createNewParam(ast, "x");

			rewrite.getListRewrite(methodDecl, MethodDeclaration.PARAMETERS_PROPERTY).insertFirst(newParam, null);

			rewrite.remove(newParam, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("    }\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());
	}

	public void testReplaceInserted_since_3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		{
			// replace inserted node
			PrimitiveType newType= ast.newPrimitiveType(PrimitiveType.INT);

			rewrite.set(methodDecl, MethodDeclaration.RETURN_TYPE2_PROPERTY, newType, null);

			PrimitiveType betterType= ast.newPrimitiveType(PrimitiveType.BOOLEAN);

			rewrite.replace(newType, betterType, null);

		}
		{
			// replace inserted list child
			SingleVariableDeclaration newParam= createNewParam(ast, "x");

			rewrite.getListRewrite(methodDecl, MethodDeclaration.PARAMETERS_PROPERTY).insertFirst(newParam, null);

			SingleVariableDeclaration betterParam= createNewParam(ast, "y");

			rewrite.replace(newParam, betterParam, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public boolean foo(float y) {\n");
		buf.append("    }\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());
	}
}
