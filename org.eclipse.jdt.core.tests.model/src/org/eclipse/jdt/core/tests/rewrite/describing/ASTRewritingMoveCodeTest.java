/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.rewrite.describing;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

public class ASTRewritingMoveCodeTest extends ASTRewritingTest {

	private static final Class THIS= ASTRewritingMoveCodeTest.class;

	public ASTRewritingMoveCodeTest(String name) {
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

	public void testMove() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    int x;\n");
		buf.append("    public void foo() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		assertTrue("Code has errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);

		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		List bodyDecls= type.bodyDeclarations();

		ASTNode first= (ASTNode) bodyDecls.get(0);
		ASTNode placeholder= rewrite.createMoveTarget(first);

		rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertLast(placeholder, null);

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("    }\n");
		buf.append("    int x;\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testMoveDeclSameLevelCD() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E extends Exception implements Runnable, Serializable {\n");
		buf.append("    public static class EInner {\n");
		buf.append("        public void xee() {\n");
		buf.append("            /* does nothing */\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("    private /* inner comment */ int i;\n");
		buf.append("    private int k;\n");
		buf.append("    public E() {\n");
		buf.append("        super();\n");
		buf.append("        i= 0;\n");
		buf.append("        k= 9;\n");
		buf.append("        if (System.out == null) {\n");
		buf.append("            gee(); // cool\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("    public void gee() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("interface G {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		assertTrue("Code has errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);

		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		{ // move inner type to the end of the type & move, copy statements from constructor to method

			List members= type.bodyDeclarations();
			assertTrue("Has declarations", !members.isEmpty());

			assertTrue("Cannot find inner class", members.get(0) instanceof TypeDeclaration);
			TypeDeclaration innerType= (TypeDeclaration) members.get(0);

			rewrite.remove(innerType, null);
			ASTNode movedNode= rewrite.createCopyTarget(innerType);

			rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertLast(movedNode, null);

			Statement toMove;
			Statement toCopy;
			{
				MethodDeclaration methodDecl= findMethodDeclaration(type, "E");
				assertTrue("Cannot find Constructor E", methodDecl != null);
				Block body= methodDecl.getBody();
				assertTrue("No body", body != null);
				List statements= body.statements();
				assertTrue("Not expected number of statements", statements.size() == 4);

				toMove= (Statement) statements.get(1);
				toCopy= (Statement) statements.get(3);

				rewrite.remove(toMove, null);
			}
			{
				MethodDeclaration methodDecl= findMethodDeclaration(type, "gee");
				assertTrue("Cannot find gee()", methodDecl != null);
				Block body= methodDecl.getBody();
				assertTrue("No body", body != null);
				List statements= body.statements();
				assertTrue("Has statements", statements.isEmpty());

				ASTNode insertNodeForMove= rewrite.createCopyTarget(toMove);
				ASTNode insertNodeForCopy= rewrite.createCopyTarget(toCopy);

				rewrite.getListRewrite(body, Block.STATEMENTS_PROPERTY).insertLast(insertNodeForCopy, null);
				rewrite.getListRewrite(body, Block.STATEMENTS_PROPERTY).insertLast(insertNodeForMove, null);

			}
		}


		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E extends Exception implements Runnable, Serializable {\n");
		buf.append("    private /* inner comment */ int i;\n");
		buf.append("    private int k;\n");
		buf.append("    public E() {\n");
		buf.append("        super();\n");
		buf.append("        k= 9;\n");
		buf.append("        if (System.out == null) {\n");
		buf.append("            gee(); // cool\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("    public void gee() {\n");
		buf.append("        if (System.out == null) {\n");
		buf.append("            gee(); // cool\n");
		buf.append("        }\n");
		buf.append("        i= 0;\n");
		buf.append("    }\n");
		buf.append("    public static class EInner {\n");
		buf.append("        public void xee() {\n");
		buf.append("            /* does nothing */\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("interface G {\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testMoveDeclSameLevel() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E extends Exception implements Runnable, Serializable {\n");
		buf.append("    public static class EInner {\n");
		buf.append("        public void xee() {\n");
		buf.append("            /* does nothing */\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("    private /* inner comment */ int i;\n");
		buf.append("    private int k;\n");
		buf.append("    public E() {\n");
		buf.append("        super();\n");
		buf.append("        i= 0;\n");
		buf.append("        k= 9;\n");
		buf.append("        if (System.out == null) {\n");
		buf.append("            gee(); // cool\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("    public void gee() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("interface G {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		assertTrue("Code has errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);

		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		{ // move inner type to the end of the type & move, copy statements from constructor to method

			List members= type.bodyDeclarations();
			assertTrue("Has declarations", !members.isEmpty());

			assertTrue("Cannot find inner class", members.get(0) instanceof TypeDeclaration);
			TypeDeclaration innerType= (TypeDeclaration) members.get(0);

			ASTNode movedNode= rewrite.createMoveTarget(innerType);
			rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertLast(movedNode, null);


			Statement toMove;
			Statement toCopy;
			{
				MethodDeclaration methodDecl= findMethodDeclaration(type, "E");
				assertTrue("Cannot find Constructor E", methodDecl != null);
				Block body= methodDecl.getBody();
				assertTrue("No body", body != null);
				List statements= body.statements();
				assertTrue("Not expected number of statements", statements.size() == 4);

				toMove= (Statement) statements.get(1);
				toCopy= (Statement) statements.get(3);
			}
			{
				MethodDeclaration methodDecl= findMethodDeclaration(type, "gee");
				assertTrue("Cannot find gee()", methodDecl != null);
				Block body= methodDecl.getBody();
				assertTrue("No body", body != null);
				List statements= body.statements();
				assertTrue("Has statements", statements.isEmpty());

				ASTNode insertNodeForMove= rewrite.createMoveTarget(toMove);
				ASTNode insertNodeForCopy= rewrite.createCopyTarget(toCopy);

				rewrite.getListRewrite(body, Block.STATEMENTS_PROPERTY).insertLast(insertNodeForCopy, null);
				rewrite.getListRewrite(body, Block.STATEMENTS_PROPERTY).insertLast(insertNodeForMove, null);
			}
		}


		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E extends Exception implements Runnable, Serializable {\n");
		buf.append("    private /* inner comment */ int i;\n");
		buf.append("    private int k;\n");
		buf.append("    public E() {\n");
		buf.append("        super();\n");
		buf.append("        k= 9;\n");
		buf.append("        if (System.out == null) {\n");
		buf.append("            gee(); // cool\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("    public void gee() {\n");
		buf.append("        if (System.out == null) {\n");
		buf.append("            gee(); // cool\n");
		buf.append("        }\n");
		buf.append("        i= 0;\n");
		buf.append("    }\n");
		buf.append("    public static class EInner {\n");
		buf.append("        public void xee() {\n");
		buf.append("            /* does nothing */\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("interface G {\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}


	public void testMoveDeclDifferentLevelCD() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E extends Exception implements Runnable, Serializable {\n");
		buf.append("    public static class EInner {\n");
		buf.append("        public void xee() {\n");
		buf.append("            /* does nothing */\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("    private /* inner comment */ int i;\n");
		buf.append("    private int k;\n");
		buf.append("    public E() {\n");
		buf.append("        super();\n");
		buf.append("        i= 0;\n");
		buf.append("        k= 9;\n");
		buf.append("        if (System.out == null) {\n");
		buf.append("            gee(); // cool\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("    public void gee() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("interface G {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		assertTrue("Code has errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);

		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		{
			List members= type.bodyDeclarations();
			assertTrue("Has declarations", !members.isEmpty());

			assertTrue("Cannot find inner class", members.get(0) instanceof TypeDeclaration);
			TypeDeclaration innerType= (TypeDeclaration) members.get(0);

			List innerMembers= innerType.bodyDeclarations();
			assertTrue("Not expected number of inner members", innerMembers.size() == 1);

			{ // move outer as inner of inner.
				TypeDeclaration outerType= findTypeDeclaration(astRoot, "G");
				assertTrue("G not found", outerType != null);

				rewrite.remove(outerType, null);

				ASTNode insertNodeForCopy= rewrite.createCopyTarget(outerType);

				rewrite.getListRewrite(innerType, TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertLast(insertNodeForCopy, null);


			}
			{ // copy method of inner to main type
				MethodDeclaration methodDecl= (MethodDeclaration) innerMembers.get(0);
				ASTNode insertNodeForMove= rewrite.createCopyTarget(methodDecl);

				rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertLast(insertNodeForMove, null);

			}
			{ // nest body of constructor in a while statement
				MethodDeclaration methodDecl= findMethodDeclaration(type, "E");
				assertTrue("Cannot find Constructor E", methodDecl != null);

				Block body= methodDecl.getBody();

				WhileStatement whileStatement= ast.newWhileStatement();
				whileStatement.setExpression(ast.newBooleanLiteral(true));

				Statement insertNodeForCopy= (Statement) rewrite.createCopyTarget(body);

				whileStatement.setBody(insertNodeForCopy); // set existing body

				Block newBody= ast.newBlock();
				List newStatements= newBody.statements();
				newStatements.add(whileStatement);

				rewrite.replace(body, newBody, null);
			}
		}


		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E extends Exception implements Runnable, Serializable {\n");
		buf.append("    public static class EInner {\n");
		buf.append("        public void xee() {\n");
		buf.append("            /* does nothing */\n");
		buf.append("        }\n");
		buf.append("\n");
		buf.append("        interface G {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("    private /* inner comment */ int i;\n");
		buf.append("    private int k;\n");
		buf.append("    public E() {\n");
		buf.append("        while (true) {\n");
		buf.append("            super();\n");
		buf.append("            i= 0;\n");
		buf.append("            k= 9;\n");
		buf.append("            if (System.out == null) {\n");
		buf.append("                gee(); // cool\n");
		buf.append("            }\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("    public void gee() {\n");
		buf.append("    }\n");
		buf.append("    public void xee() {\n");
		buf.append("        /* does nothing */\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testMoveDeclDifferentLevel() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E extends Exception implements Runnable, Serializable {\n");
		buf.append("    public static class EInner {\n");
		buf.append("        public void xee() {\n");
		buf.append("            /* does nothing */\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("    private /* inner comment */ int i;\n");
		buf.append("    private int k;\n");
		buf.append("    public E() {\n");
		buf.append("        super();\n");
		buf.append("        i= 0;\n");
		buf.append("        k= 9;\n");
		buf.append("        if (System.out == null) {\n");
		buf.append("            gee(); // cool\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("    public void gee() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("interface G {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		assertTrue("Code has errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);

		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		{
			List members= type.bodyDeclarations();
			assertTrue("Has declarations", !members.isEmpty());

			assertTrue("Cannot find inner class", members.get(0) instanceof TypeDeclaration);
			TypeDeclaration innerType= (TypeDeclaration) members.get(0);

			List innerMembers= innerType.bodyDeclarations();
			assertTrue("Not expected number of inner members", innerMembers.size() == 1);

			{ // move outer as inner of inner.
				TypeDeclaration outerType= findTypeDeclaration(astRoot, "G");
				assertTrue("G not found", outerType != null);

				ASTNode insertNodeForCopy= rewrite.createMoveTarget(outerType);

				rewrite.getListRewrite(innerType, TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertLast(insertNodeForCopy, null);
			}
			{ // copy method of inner to main type
				MethodDeclaration methodDecl= (MethodDeclaration) innerMembers.get(0);
				ASTNode insertNodeForMove= rewrite.createCopyTarget(methodDecl);

				rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY).insertLast(insertNodeForMove, null);
			}
			{ // nest body of constructor in a while statement
				MethodDeclaration methodDecl= findMethodDeclaration(type, "E");
				assertTrue("Cannot find Constructor E", methodDecl != null);

				Block body= methodDecl.getBody();

				WhileStatement whileStatement= ast.newWhileStatement();
				whileStatement.setExpression(ast.newBooleanLiteral(true));

				Statement insertNodeForCopy= (Statement) rewrite.createCopyTarget(body);

				whileStatement.setBody(insertNodeForCopy); // set existing body

				Block newBody= ast.newBlock();
				List newStatements= newBody.statements();
				newStatements.add(whileStatement);

				rewrite.replace(body, newBody, null);
			}
		}


		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E extends Exception implements Runnable, Serializable {\n");
		buf.append("    public static class EInner {\n");
		buf.append("        public void xee() {\n");
		buf.append("            /* does nothing */\n");
		buf.append("        }\n");
		buf.append("\n");
		buf.append("        interface G {\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("    private /* inner comment */ int i;\n");
		buf.append("    private int k;\n");
		buf.append("    public E() {\n");
		buf.append("        while (true) {\n");
		buf.append("            super();\n");
		buf.append("            i= 0;\n");
		buf.append("            k= 9;\n");
		buf.append("            if (System.out == null) {\n");
		buf.append("                gee(); // cool\n");
		buf.append("            }\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("    public void gee() {\n");
		buf.append("    }\n");
		buf.append("    public void xee() {\n");
		buf.append("        /* does nothing */\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testMoveStatementsCD() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E extends Exception implements Runnable, Serializable {\n");
		buf.append("    public static class EInner {\n");
		buf.append("        public void xee() {\n");
		buf.append("            /* does nothing */\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("    private /* inner comment */ int i;\n");
		buf.append("    private int k;\n");
		buf.append("    public E() {\n");
		buf.append("        super();\n");
		buf.append("        i= 0;\n");
		buf.append("        k= 9;\n");
		buf.append("        if (System.out == null) {\n");
		buf.append("            gee(); // cool\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("    public void gee() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("interface G {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		assertTrue("Code has errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);

		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		{ // move first statments inside an ifstatement, move second statment inside a new while statement
		   // that is in the ifstatement
			MethodDeclaration methodDecl= findMethodDeclaration(type, "E");
			assertTrue("Cannot find Constructor E", methodDecl != null);

			Block body= methodDecl.getBody();
			List statements= body.statements();

			assertTrue("Cannot find if statement", statements.get(3) instanceof IfStatement);

			IfStatement ifStatement= (IfStatement) statements.get(3);

			Statement insertNodeForCopy1= (Statement) rewrite.createCopyTarget((ASTNode) statements.get(1));
			Statement insertNodeForCopy2= (Statement) rewrite.createCopyTarget((ASTNode) statements.get(2));

			Block whileBody= ast.newBlock();

			WhileStatement whileStatement= ast.newWhileStatement();
			whileStatement.setExpression(ast.newBooleanLiteral(true));
			whileStatement.setBody(whileBody);

			List whileBodyStatements= whileBody.statements();
			whileBodyStatements.add(insertNodeForCopy2);


			assertTrue("if statement body not a block", ifStatement.getThenStatement() instanceof Block);


			Block block= (Block)ifStatement.getThenStatement();

			rewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY).insertFirst(whileStatement, null);
			rewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY).insertAfter(insertNodeForCopy1, whileStatement, null);

			rewrite.remove((ASTNode) statements.get(1), null);
			rewrite.remove((ASTNode) statements.get(2), null);
		}


		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E extends Exception implements Runnable, Serializable {\n");
		buf.append("    public static class EInner {\n");
		buf.append("        public void xee() {\n");
		buf.append("            /* does nothing */\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("    private /* inner comment */ int i;\n");
		buf.append("    private int k;\n");
		buf.append("    public E() {\n");
		buf.append("        super();\n");
		buf.append("        if (System.out == null) {\n");
		buf.append("            while (true) {\n");
		buf.append("                k= 9;\n");
		buf.append("            }\n");
		buf.append("            i= 0;\n");
		buf.append("            gee(); // cool\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("    public void gee() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("interface G {\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testMoveStatements() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E extends Exception implements Runnable, Serializable {\n");
		buf.append("    private /* inner comment */ int i;\n");
		buf.append("    private int k;\n");
		buf.append("    public E() {\n");
		buf.append("        super();\n");
		buf.append("        i= 0;\n");
		buf.append("        k= 9;\n");
		buf.append("        if (System.out == null) {\n");
		buf.append("            gee(); // cool\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("    public void gee() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("interface G {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		assertTrue("Code has errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);

		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		{ // move first statments inside an ifstatement, move second statment inside a new while statement
		   // that is in the ifstatement
			MethodDeclaration methodDecl= findMethodDeclaration(type, "E");
			assertTrue("Cannot find Constructor E", methodDecl != null);

			Block body= methodDecl.getBody();
			List statements= body.statements();

			assertTrue("Cannot find if statement", statements.get(3) instanceof IfStatement);

			IfStatement ifStatement= (IfStatement) statements.get(3);

			Statement insertNodeForCopy1= (Statement) rewrite.createMoveTarget((ASTNode) statements.get(1));
			Statement insertNodeForCopy2= (Statement) rewrite.createMoveTarget((ASTNode) statements.get(2));

			Block whileBody= ast.newBlock();

			WhileStatement whileStatement= ast.newWhileStatement();
			whileStatement.setExpression(ast.newBooleanLiteral(true));
			whileStatement.setBody(whileBody);

			List whileBodyStatements= whileBody.statements();
			whileBodyStatements.add(insertNodeForCopy2);


			assertTrue("if statement body not a block", ifStatement.getThenStatement() instanceof Block);

			Block block= (Block) ifStatement.getThenStatement();
			rewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY).insertFirst(whileStatement, null);
			rewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY).insertAfter(insertNodeForCopy1, whileStatement, null);
		}


		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E extends Exception implements Runnable, Serializable {\n");
		buf.append("    private /* inner comment */ int i;\n");
		buf.append("    private int k;\n");
		buf.append("    public E() {\n");
		buf.append("        super();\n");
		buf.append("        if (System.out == null) {\n");
		buf.append("            while (true) {\n");
		buf.append("                k= 9;\n");
		buf.append("            }\n");
		buf.append("            i= 0;\n");
		buf.append("            gee(); // cool\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("    public void gee() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("interface G {\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testCopyFromDeleted() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (System.out == null) {\n");
		buf.append("            gee(); // cool\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("    public void goo() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		assertTrue("Code has errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);

		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		{ // delete method foo, but copy if statement to goo
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
			assertTrue("Cannot find foo", methodDecl != null);

			rewrite.remove(methodDecl, null);

			Block body= methodDecl.getBody();
			List statements= body.statements();
			assertTrue("Cannot find if statement", statements.size() == 1);

			ASTNode placeHolder= rewrite.createMoveTarget((ASTNode) statements.get(0));


			MethodDeclaration methodGoo= findMethodDeclaration(type, "goo");
			assertTrue("Cannot find goo", methodGoo != null);

			rewrite.getListRewrite(methodGoo.getBody(), Block.STATEMENTS_PROPERTY).insertLast(placeHolder, null);
		}


		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void goo() {\n");
		buf.append("        if (System.out == null) {\n");
		buf.append("            gee(); // cool\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testChangesInMoveCD() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (System.out == null) {\n");
		buf.append("            gee( /* cool */);\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("    public void goo() {\n");
		buf.append("        x= 1;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		assertTrue("Code has errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);

		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		{ // replace statement in goo with moved ifStatement from foo. add a node to if statement
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
			assertTrue("Cannot find foo", methodDecl != null);

			List fooStatements= methodDecl.getBody().statements();
			assertTrue("Cannot find if statement", fooStatements.size() == 1);

			// prepare ifStatement to move
			IfStatement ifStatement= (IfStatement) fooStatements.get(0);
			rewrite.remove(ifStatement, null);

			ASTNode placeHolder= rewrite.createCopyTarget(ifStatement);

			// add return statement to ifStatement block
			ReturnStatement returnStatement= ast.newReturnStatement();
			Block then= (Block) ifStatement.getThenStatement();
			rewrite.getListRewrite(then, Block.STATEMENTS_PROPERTY).insertLast(returnStatement, null);

			// replace statement in goo with moved ifStatement
			MethodDeclaration methodGoo= findMethodDeclaration(type, "goo");
			assertTrue("Cannot find goo", methodGoo != null);

			List gooStatements= methodGoo.getBody().statements();
			assertTrue("Cannot find statement in goo", gooStatements.size() == 1);

			rewrite.replace((ASTNode) gooStatements.get(0), placeHolder, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("    }\n");
		buf.append("    public void goo() {\n");
		buf.append("        if (System.out == null) {\n");
		buf.append("            gee( /* cool */);\n");
		buf.append("            return;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testChangesInMove() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (System.out == null) {\n");
		buf.append("            gee( /* cool */);\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("    public void goo() {\n");
		buf.append("        x= 1;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		assertTrue("Code has errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);

		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		{ // replace statement in goo with moved ifStatement from foo. add a node to if statement
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
			assertTrue("Cannot find foo", methodDecl != null);

			List fooStatements= methodDecl.getBody().statements();
			assertTrue("Cannot find if statement", fooStatements.size() == 1);

			// prepare ifStatement to move
			IfStatement ifStatement= (IfStatement) fooStatements.get(0);

			ASTNode placeHolder= rewrite.createMoveTarget(ifStatement);

			// add return statement to ifStatement block
			ReturnStatement returnStatement= ast.newReturnStatement();

			Block then= (Block) ifStatement.getThenStatement();
			rewrite.getListRewrite(then, Block.STATEMENTS_PROPERTY).insertLast(returnStatement, null);


			// replace statement in goo with moved ifStatement
			MethodDeclaration methodGoo= findMethodDeclaration(type, "goo");
			assertTrue("Cannot find goo", methodGoo != null);

			List gooStatements= methodGoo.getBody().statements();
			assertTrue("Cannot find statement in goo", gooStatements.size() == 1);

			rewrite.replace((ASTNode) gooStatements.get(0), placeHolder, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("    }\n");
		buf.append("    public void goo() {\n");
		buf.append("        if (System.out == null) {\n");
		buf.append("            gee( /* cool */);\n");
		buf.append("            return;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testSwapCD() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        goo(xoo(/*hello*/), k * 2);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		assertTrue("Code has errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);

		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		{ // swap the two arguments
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
			assertTrue("Cannot find foo", methodDecl != null);

			List fooStatements= methodDecl.getBody().statements();
			assertTrue("More statements than expected", fooStatements.size() == 1);

			ExpressionStatement statement= (ExpressionStatement) fooStatements.get(0);
			MethodInvocation invocation= (MethodInvocation) statement.getExpression();

			List arguments= invocation.arguments();
			assertTrue("More arguments than expected", arguments.size() == 2);

			ASTNode arg0= (ASTNode) arguments.get(0);
			ASTNode arg1= (ASTNode) arguments.get(1);

			ASTNode placeHolder0= rewrite.createCopyTarget(arg0);
			ASTNode placeHolder1= rewrite.createCopyTarget(arg1);

			rewrite.replace(arg0, placeHolder1, null);
			rewrite.replace(arg1, placeHolder0, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        goo(k * 2, xoo(/*hello*/));\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testSwap() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        goo(xoo(/*hello*/), k * 2);\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		assertTrue("Code has errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);

		TypeDeclaration type= findTypeDeclaration(astRoot, "E");

		{ // swap the two arguments
			MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
			assertTrue("Cannot find foo", methodDecl != null);

			List fooStatements= methodDecl.getBody().statements();
			assertTrue("More statements than expected", fooStatements.size() == 1);

			ExpressionStatement statement= (ExpressionStatement) fooStatements.get(0);
			MethodInvocation invocation= (MethodInvocation) statement.getExpression();

			List arguments= invocation.arguments();
			assertTrue("More arguments than expected", arguments.size() == 2);

			ASTNode arg0= (ASTNode) arguments.get(0);
			ASTNode arg1= (ASTNode) arguments.get(1);

			ASTNode placeHolder0= rewrite.createMoveTarget(arg0);
			ASTNode placeHolder1= rewrite.createMoveTarget(arg1);

			rewrite.replace(arg0, placeHolder1, null); // replace instead of remove
			rewrite.replace(arg1, placeHolder0, null); // replace instead of remove
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        goo(k * 2, xoo(/*hello*/));\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}


	public void testMultipleCopiesOfSameNode() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (i == 0) {\n");
		buf.append("            foo();\n");
		buf.append("            i++; // comment\n");
		buf.append("            i++;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		assertTrue("Code has errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);

		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		{
			List statements= methodDecl.getBody().statements();
			IfStatement ifStatement= (IfStatement) statements.get(0);
			List ifStatementBody= ((Block) ifStatement.getThenStatement()).statements();

			ASTNode node= (ASTNode) ifStatementBody.get(1);
			ASTNode placeholder1= rewrite.createCopyTarget(node);
			rewrite.getListRewrite(methodDecl.getBody(), Block.STATEMENTS_PROPERTY).insertLast(placeholder1, null);

			ASTNode placeholder2= rewrite.createCopyTarget(node);
			rewrite.replace((ASTNode) ifStatementBody.get(0), placeholder2, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (i == 0) {\n");
		buf.append("            i++; // comment\n");
		buf.append("            i++; // comment\n");
		buf.append("            i++;\n");
		buf.append("        }\n");
		buf.append("        i++; // comment\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testCopyMultipleNodes() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (i == 0) {\n");
		buf.append("            foo();\n");
		buf.append("            i++; // comment\n");
		buf.append("            i++;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		assertTrue("Code has errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);

		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		{
			List statements= methodDecl.getBody().statements();
			IfStatement ifStatement= (IfStatement) statements.get(0);
			Block thenBlock= (Block) ifStatement.getThenStatement();

			ListRewrite listRewrite= rewrite.getListRewrite(thenBlock, Block.STATEMENTS_PROPERTY);
			List thenStatements= thenBlock.statements();
			ASTNode first= (ASTNode) thenStatements.get(0);
			ASTNode last= (ASTNode) thenStatements.get(1);

			ASTNode placeholder= listRewrite.createCopyTarget(first, last);

			rewrite.getListRewrite(methodDecl.getBody(), Block.STATEMENTS_PROPERTY).insertLast(placeholder, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (i == 0) {\n");
		buf.append("            foo();\n");
		buf.append("            i++; // comment\n");
		buf.append("            i++;\n");
		buf.append("        }\n");
		buf.append("        foo();\n");
		buf.append("        i++; // comment\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testCopyMultipleNodes0() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (i == 0) {\n");
		buf.append("            foo();\n");
		buf.append("            i++; // comment\n");
		buf.append("            i++;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		assertTrue("Code has errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);

		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		{

			List statements= methodDecl.getBody().statements();
			IfStatement ifStatement= (IfStatement) statements.get(0);
			Block thenBlock= (Block) ifStatement.getThenStatement();

			ListRewrite listRewrite= rewrite.getListRewrite(thenBlock, Block.STATEMENTS_PROPERTY);
			List thenStatements= thenBlock.statements();
			ASTNode first= (ASTNode) thenStatements.get(0);
			ASTNode second= (ASTNode) thenStatements.get(1);

			ASTNode placeholder= listRewrite.createCopyTarget(first, second);

			// replace the first
			AST ast= astRoot.getAST();
			PrefixExpression expression= ast.newPrefixExpression();
			expression.setOperand(ast.newSimpleName("i"));
			expression.setOperator(PrefixExpression.Operator.DECREMENT);
			ExpressionStatement newStatement= ast.newExpressionStatement(expression);

			listRewrite.replace(first, newStatement, null);

			rewrite.getListRewrite(methodDecl.getBody(), Block.STATEMENTS_PROPERTY).insertLast(placeholder, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (i == 0) {\n");
		buf.append("            --i;\n");
		buf.append("            i++; // comment\n");
		buf.append("            i++;\n");
		buf.append("        }\n");
		buf.append("        --i;\n");
		buf.append("        i++; // comment\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}


	public void testCopyMultipleNodes1() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (i == 0) {\n");
		buf.append("            foo();\n");
		buf.append("            i++; // comment\n");
		buf.append("            i++;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		assertTrue("Code has errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);

		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		{

			List statements= methodDecl.getBody().statements();
			IfStatement ifStatement= (IfStatement) statements.get(0);
			Block thenBlock= (Block) ifStatement.getThenStatement();

			ListRewrite listRewrite= rewrite.getListRewrite(thenBlock, Block.STATEMENTS_PROPERTY);
			List thenStatements= thenBlock.statements();
			ASTNode first= (ASTNode) thenStatements.get(0);
			ASTNode second= (ASTNode) thenStatements.get(1);

			ASTNode placeholder= listRewrite.createCopyTarget(first, second);

			// insert between the copied
			AST ast= astRoot.getAST();
			PrefixExpression expression= ast.newPrefixExpression();
			expression.setOperand(ast.newSimpleName("i"));
			expression.setOperator(PrefixExpression.Operator.DECREMENT);
			ExpressionStatement newStatement= ast.newExpressionStatement(expression);
			listRewrite.insertAfter(newStatement, first, null);

			// remove the last
			listRewrite.remove((ASTNode) thenStatements.get(2), null);

			rewrite.getListRewrite(methodDecl.getBody(), Block.STATEMENTS_PROPERTY).insertLast(placeholder, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (i == 0) {\n");
		buf.append("            foo();\n");
		buf.append("            --i;\n");
		buf.append("            i++; // comment\n");
		buf.append("        }\n");
		buf.append("        foo();\n");
		buf.append("        --i;\n");
		buf.append("        i++; // comment\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}


	public void testCopyMultipleNodes2() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (i == 0) {\n");
		buf.append("            foo(1);\n");
		buf.append("            foo(2); // comment\n");
		buf.append("            foo(3);\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		assertTrue("Code has errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);

		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		{

			List statements= methodDecl.getBody().statements();
			IfStatement ifStatement= (IfStatement) statements.get(0);
			Block thenBlock= (Block) ifStatement.getThenStatement();

			ListRewrite listRewrite= rewrite.getListRewrite(thenBlock, Block.STATEMENTS_PROPERTY);
			List thenStatements= thenBlock.statements();
			ASTNode second= (ASTNode) thenStatements.get(1);
			ASTNode last= (ASTNode) thenStatements.get(2);

			ASTNode placeholder1= listRewrite.createCopyTarget(second, last);
			rewrite.replace(ifStatement, placeholder1, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        foo(2); // comment\n");
		buf.append("        foo(3);\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testCopyMultipleNodes3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (i == 0) {\n");
		buf.append("            foo(1);\n");
		buf.append("            foo(2);\n");
		buf.append("            foo(3);\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		assertTrue("Code has errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);

		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		{

			List statements= methodDecl.getBody().statements();
			IfStatement ifStatement= (IfStatement) statements.get(0);
			Block thenBlock= (Block) ifStatement.getThenStatement();

			ListRewrite listRewrite= rewrite.getListRewrite(thenBlock, Block.STATEMENTS_PROPERTY);
			List thenStatements= thenBlock.statements();
			ASTNode first= (ASTNode) thenStatements.get(0);
			ASTNode second= (ASTNode) thenStatements.get(1);
			ASTNode last= (ASTNode) thenStatements.get(2);

			ASTNode placeholder1= listRewrite.createCopyTarget(first, second);
			ASTNode placeholder2= listRewrite.createCopyTarget(first, last);

			listRewrite.insertAfter(placeholder1, last, null);

			rewrite.getListRewrite(methodDecl.getBody(), Block.STATEMENTS_PROPERTY).insertLast(placeholder2, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (i == 0) {\n");
		buf.append("            foo(1);\n");
		buf.append("            foo(2);\n");
		buf.append("            foo(3);\n");
		buf.append("            foo(1);\n");
		buf.append("            foo(2);\n");
		buf.append("        }\n");
		buf.append("        foo(1);\n");
		buf.append("        foo(2);\n");
		buf.append("        foo(3);\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testCopyMultipleNodes4() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (i == 0) {\n");
		buf.append("            foo(1);\n");
		buf.append("            foo(2);\n");
		buf.append("            foo(3);\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		assertTrue("Code has errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);

		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		{

			List statements= methodDecl.getBody().statements();
			IfStatement ifStatement= (IfStatement) statements.get(0);
			Block thenBlock= (Block) ifStatement.getThenStatement();

			ListRewrite listRewrite= rewrite.getListRewrite(thenBlock, Block.STATEMENTS_PROPERTY);
			List thenStatements= thenBlock.statements();
			ASTNode first= (ASTNode) thenStatements.get(0);
			ASTNode second= (ASTNode) thenStatements.get(1);

			ASTNode last= (ASTNode) thenStatements.get(2);

			ASTNode placeholder1= listRewrite.createCopyTarget(first, last);
			ASTNode placeholder2= listRewrite.createCopyTarget(first, second);
			ASTNode placeholder3= rewrite.createMoveTarget(first);

			listRewrite.insertAfter(placeholder1, last, null);

			rewrite.getListRewrite(methodDecl.getBody(), Block.STATEMENTS_PROPERTY).insertLast(placeholder2, null);
			rewrite.getListRewrite(methodDecl.getBody(), Block.STATEMENTS_PROPERTY).insertFirst(placeholder3, null);

		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        foo(1);\n");
		buf.append("        if (i == 0) {\n");
		buf.append("            foo(2);\n");
		buf.append("            foo(3);\n");
		buf.append("            foo(2);\n");
		buf.append("            foo(3);\n");
		buf.append("        }\n");
		buf.append("        foo(2);\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testMultipleCopiesOfSameNodeAndMove() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (i == 0) {\n");
		buf.append("            foo();\n");
		buf.append("            i++; // comment\n");
		buf.append("            i++;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		assertTrue("Code has errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);

		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		{
			List statements= methodDecl.getBody().statements();
			IfStatement ifStatement= (IfStatement) statements.get(0);
			List ifStatementBody= ((Block) ifStatement.getThenStatement()).statements();

			ASTNode node= (ASTNode) ifStatementBody.get(1);
			ASTNode placeholder1= rewrite.createCopyTarget(node);
			rewrite.getListRewrite(methodDecl.getBody(), Block.STATEMENTS_PROPERTY).insertLast(placeholder1, null);

			ASTNode placeholder2= rewrite.createCopyTarget(node);
			rewrite.replace((ASTNode) ifStatementBody.get(0), placeholder2, null);

			ASTNode placeholder3= rewrite.createMoveTarget(node);
			rewrite.getListRewrite(methodDecl.getBody(), Block.STATEMENTS_PROPERTY).insertFirst(placeholder3, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        i++; // comment\n");
		buf.append("        if (i == 0) {\n");
		buf.append("            i++; // comment\n");
		buf.append("            i++;\n");
		buf.append("        }\n");
		buf.append("        i++; // comment\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testMoveMultipleNodes() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (i == 0) {\n");
		buf.append("            foo();\n");
		buf.append("            i++; // comment\n");
		buf.append("            i++;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		assertTrue("Code has errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);

		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		{
			List statements= methodDecl.getBody().statements();
			IfStatement ifStatement= (IfStatement) statements.get(0);
			Block thenBlock= (Block) ifStatement.getThenStatement();

			ListRewrite listRewrite= rewrite.getListRewrite(thenBlock, Block.STATEMENTS_PROPERTY);
			List thenStatements= thenBlock.statements();
			ASTNode first= (ASTNode) thenStatements.get(0);
			ASTNode last= (ASTNode) thenStatements.get(1);

			ASTNode placeholder= listRewrite.createMoveTarget(first, last);

			rewrite.getListRewrite(methodDecl.getBody(), Block.STATEMENTS_PROPERTY).insertLast(placeholder, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (i == 0) {\n");
		buf.append("            i++;\n");
		buf.append("        }\n");
		buf.append("        foo();\n");
		buf.append("        i++; // comment\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testMoveMultipleNodes2() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (i == 0) {\n");
		buf.append("            foo();\n");
		buf.append("            i++; // comment\n");
		buf.append("            i++;\n");
		buf.append("            i--;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		assertTrue("Code has errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);

		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		{
			List statements= methodDecl.getBody().statements();
			IfStatement ifStatement= (IfStatement) statements.get(0);
			Block thenBlock= (Block) ifStatement.getThenStatement();

			ListRewrite listRewrite= rewrite.getListRewrite(thenBlock, Block.STATEMENTS_PROPERTY);
			List thenStatements= thenBlock.statements();
			ASTNode first1= (ASTNode) thenStatements.get(0);
			ASTNode last1= (ASTNode) thenStatements.get(1);

			ASTNode placeholder1= listRewrite.createMoveTarget(first1, last1);

			ASTNode first2= (ASTNode) thenStatements.get(2);
			ASTNode last2= (ASTNode) thenStatements.get(3);

			ASTNode placeholder2= listRewrite.createMoveTarget(first2, last2);

			rewrite.getListRewrite(methodDecl.getBody(), Block.STATEMENTS_PROPERTY).insertFirst(placeholder1, null);
			rewrite.getListRewrite(methodDecl.getBody(), Block.STATEMENTS_PROPERTY).insertLast(placeholder2, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        foo();\n");
		buf.append("        i++; // comment\n");
		buf.append("        if (i == 0) {\n");
		buf.append("        }\n");
		buf.append("        i++;\n");
		buf.append("        i--;\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}


	public void testMoveMultipleNodes3() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (i == 0) {\n");
		buf.append("            foo();\n");
		buf.append("            i++; // comment\n");
		buf.append("            i++;\n");
		buf.append("            i--;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		assertTrue("Code has errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);

		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		{
			List statements= methodDecl.getBody().statements();
			IfStatement ifStatement= (IfStatement) statements.get(0);
			Block thenBlock= (Block) ifStatement.getThenStatement();

			ListRewrite listRewrite= rewrite.getListRewrite(thenBlock, Block.STATEMENTS_PROPERTY);
			List thenStatements= thenBlock.statements();
			ASTNode first= (ASTNode) thenStatements.get(0);
			ASTNode between= (ASTNode) thenStatements.get(1);
			ASTNode last= (ASTNode) thenStatements.get(3);

			ASTNode placeholder1= listRewrite.createMoveTarget(first, last);
			ASTNode placeholder2= listRewrite.createCopyTarget(first, between);

			rewrite.replace(ifStatement, placeholder1, null);

			rewrite.getListRewrite(methodDecl.getBody(), Block.STATEMENTS_PROPERTY).insertFirst(placeholder2, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        foo();\n");
		buf.append("        i++; // comment\n");
		buf.append("        foo();\n");
		buf.append("        i++; // comment\n");
		buf.append("        i++;\n");
		buf.append("        i--;\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testMoveMultipleNodes4() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        if (i == 0) {\n");
		buf.append("            foo();\n");
		buf.append("            i++; // comment\n");
		buf.append("            i++;\n");
		buf.append("            i--;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		assertTrue("Code has errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);

		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		{
			List statements= methodDecl.getBody().statements();
			IfStatement ifStatement= (IfStatement) statements.get(0);
			Block thenBlock= (Block) ifStatement.getThenStatement();

			ListRewrite listRewrite= rewrite.getListRewrite(thenBlock, Block.STATEMENTS_PROPERTY);
			List thenStatements= thenBlock.statements();
			ASTNode first1= (ASTNode) thenStatements.get(0);
			ASTNode last1= (ASTNode) thenStatements.get(2);

			listRewrite.createMoveTarget(first1, last1);

			ASTNode first2= (ASTNode) thenStatements.get(2);
			ASTNode last2= (ASTNode) thenStatements.get(3);

			try {
				listRewrite.createMoveTarget(first2, last2);
				assertFalse(true);
			} catch (IllegalArgumentException e) {
				// overlapping move ranges
			}
		}
	}


	public void testReplaceMoveMultiple() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        foo();\n");
		buf.append("        i++; // comment\n");
		buf.append("        i--;\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		assertTrue("Code has errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);

		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		{
			List statements= methodDecl.getBody().statements();
			ASTNode first= (ASTNode) statements.get(0);
			ASTNode last= (ASTNode) statements.get(2);

			ListRewrite listRewrite= rewrite.getListRewrite(methodDecl.getBody(), Block.STATEMENTS_PROPERTY);

			Block newBlock= astRoot.getAST().newBlock();

			ASTNode placeholder1= listRewrite.createMoveTarget(first, last, newBlock, null);
			newBlock.statements().add(placeholder1);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        {\n");
		buf.append("            foo();\n");
		buf.append("            i++; // comment\n");
		buf.append("            i--;\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}


	public void testMoveForStatementToForBlockCD() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        for(int i= 0; i < 8; i++)\n");
		buf.append("            foo();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		assertTrue("Code has errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);

		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		{
			List statements= methodDecl.getBody().statements();
			ForStatement forStatement= (ForStatement) statements.get(0);
			Statement body= forStatement.getBody();

			ASTNode placeholder= rewrite.createCopyTarget(body);

			Block newBody= ast.newBlock();
			newBody.statements().add(placeholder);

			rewrite.replace(body, newBody, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        for(int i= 0; i < 8; i++) {\n");
		buf.append("            foo();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testMoveForStatementToForBlock() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        for(int i= 0; i < 8; i++)\n");
		buf.append("            foo();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		assertTrue("Code has errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);

		TypeDeclaration type= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		{
			List statements= methodDecl.getBody().statements();
			ForStatement forStatement= (ForStatement) statements.get(0);
			Statement body= forStatement.getBody();

			ASTNode placeholder= rewrite.createMoveTarget(body);

			Block newBody= ast.newBlock();
			newBody.statements().add(placeholder);

			rewrite.replace(body, newBody, null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo() {\n");
		buf.append("        for(int i= 0; i < 8; i++) {\n");
		buf.append("            foo();\n");
		buf.append("        }\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());

	}

	public void testNestedCopies() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);

		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Object o) {\n");
		buf.append("        int i= (String) o.indexOf('1');\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("E.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		TypeDeclaration typeDecl= findTypeDeclaration(astRoot, "E");
		MethodDeclaration methDecl= findMethodDeclaration(typeDecl, "foo");
		VariableDeclarationStatement varStat= (VariableDeclarationStatement) methDecl.getBody().statements().get(0);

		CastExpression expression= (CastExpression) ((VariableDeclarationFragment) varStat.fragments().get(0)).getInitializer();
		MethodInvocation invocation= (MethodInvocation) expression.getExpression();

		CastExpression newCast= ast.newCastExpression();
		newCast.setType((Type) rewrite.createCopyTarget(expression.getType()));
		newCast.setExpression((Expression) rewrite.createCopyTarget(invocation.getExpression()));
		ParenthesizedExpression parents= ast.newParenthesizedExpression();
		parents.setExpression(newCast);

		ASTNode node= rewrite.createCopyTarget(invocation);
		rewrite.replace(expression, node, null);
		rewrite.replace(invocation.getExpression(), parents, null);

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class E {\n");
		buf.append("    public void foo(Object o) {\n");
		buf.append("        int i= ((String) o).indexOf('1');\n");
		buf.append("    }\n");
		buf.append("}\n");
		String expected= buf.toString();

		assertEqualString(preview, expected);
	}


}
