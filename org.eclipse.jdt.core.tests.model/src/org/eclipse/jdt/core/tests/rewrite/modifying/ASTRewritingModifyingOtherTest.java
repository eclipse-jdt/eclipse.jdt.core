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
package org.eclipse.jdt.core.tests.rewrite.modifying;

import java.util.List;

import junit.framework.Test;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;

import org.eclipse.jdt.core.dom.*;

public class ASTRewritingModifyingOtherTest extends ASTRewritingModifyingTest {
	private static final Class THIS = ASTRewritingModifyingOtherTest.class;

	public ASTRewritingModifyingOtherTest(String name) {
		super(name);
	}

	public static Test allTests() {
		return new Suite(THIS);
	}

	public static Test suite() {
		return allTests();
	}

	public void test0000() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0000", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0000;\n");
		buf.append("public class X {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false);

		try {
			evaluateRewrite(cu, astRoot);
			assertTrue("rewrite did not fail even though recording not on", false);
		} catch (IllegalStateException e) {
		}
	}

	public void test0001() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0001", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0001;\n");
		buf.append("public class X {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuffer();
		buf.append("package test0001;\n");
		buf.append("public class X {\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}



	public void test0002() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0002", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0002;\n");
		buf.append("import java.util.*;\n");
		buf.append("import java.lang.*;\n");
		buf.append("import java.awt.*;\n");
		buf.append("public class X {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		AST a = astRoot.getAST();

		List imports = astRoot.imports();
		imports.remove(1);
		Name name = a.newSimpleName("aaa");
		ImportDeclaration importDeclaration = a.newImportDeclaration();
		importDeclaration.setName(name);
		importDeclaration.setOnDemand(true);
		imports.add(importDeclaration);

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuffer();
		buf.append("package test0002;\n");
		buf.append("import java.util.*;\n");
		buf.append("import java.awt.*;\n");
		buf.append("import aaa.*;\n");
		buf.append("public class X {\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void test0003() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0003", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0003;\n");
		buf.append("import java.util.*;\n");
		buf.append("import java.lang.*;\n");
		buf.append("import java.awt.*;\n");
		buf.append("public class X {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		ImportDeclaration importDeclaration = (ImportDeclaration)astRoot.imports().get(0);
		importDeclaration.setOnDemand(false);

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuffer();
		buf.append("package test0003;\n");
		buf.append("import java.util;\n");
		buf.append("import java.lang.*;\n");
		buf.append("import java.awt.*;\n");
		buf.append("public class X {\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}


	public void test0004() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0004", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0004;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("\n");
		buf.append("}\n");
		buf.append("class Y {\n");
		buf.append("\n");
		buf.append("}\n");
		buf.append("class Z {\n");
		buf.append("\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		AST a = astRoot.getAST();

		List types = astRoot.types();
		TypeDeclaration typeDeclaration1 = a.newTypeDeclaration();
		typeDeclaration1.setName(a.newSimpleName("A"));
		types.add(1, typeDeclaration1);
		types.remove(1);

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuffer();
		buf.append("package test0004;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("\n");
		buf.append("}\n");
		buf.append("class Y {\n");
		buf.append("\n");
		buf.append("}\n");
		buf.append("class Z {\n");
		buf.append("\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

//	public void _test000X() throws Exception {
//		IPackageFragment pack1= fSourceFolder.createPackageFragment("test0004", false, null);
//		StringBuffer buf= new StringBuffer();
//		buf.append("package test0004;\n");
//		buf.append("\n");
//		buf.append("public class X {\n");
//		buf.append("    void foo(){\n");
//		buf.append("        //rien\n");
//		buf.append("    \n");
//		buf.append("    \n");
//		buf.append("    \n");
//		buf.append("    \n");
//		buf.append("    }\n");
//		buf.append("}\n");
//		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);
//
//		CompilationUnit astRoot= parseCompilationUnit(cu, false);
//
//		astRoot.recordModifications();
//
//		AST a = astRoot.getAST();
//
//		Comment[] comments = astRoot.getCommentTable();
//		Comment comment1 = comments[0];
//		Comment comment2 = a.newBlockComment();
//		comment2.setSourceRange(comment1.getStartPosition(), comment1.getLength() - 2);
//		comments[0] = comment2;
//
//		String preview = evaluateRewrite(cu, astRoot);
//
//		buf= new StringBuffer();
//		buf.append("package test0004;\n");
//		buf.append("\n");
//		buf.append("public class X {\n");
//		buf.append("\n");
//		buf.append("}\n");
//		buf.append("class Y {\n");
//		buf.append("\n");
//		buf.append("}\n");
//		buf.append("class Z {\n");
//		buf.append("\n");
//		buf.append("}\n");
//		assertEqualString(preview, buf.toString());
//	}
}
