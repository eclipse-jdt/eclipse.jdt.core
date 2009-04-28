/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.rewrite.describing;
import junit.framework.Test;
import junit.framework.TestSuite;

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
	private static final Class THIS= ASTRewritingRevertTest.class;

	public ASTRewritingRevertTest(String name) {
		super(name);
	}

	public static Test allTests() {
		return new Suite(THIS);
	}

	public static Test setUpTest(Test someTest) {
		TestSuite suite= new Suite("one test");
		suite.addTest(someTest);
		return suite;
	}

	public static Test suite() {
		return allTests();
	}


	public void testRemoveInserted() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST3(cu);
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

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("    }\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());
	}

	public void testReplaceInserted() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST3(cu);
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

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public boolean foo(float y) {\n");
		buf.append("    }\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());
	}
}
