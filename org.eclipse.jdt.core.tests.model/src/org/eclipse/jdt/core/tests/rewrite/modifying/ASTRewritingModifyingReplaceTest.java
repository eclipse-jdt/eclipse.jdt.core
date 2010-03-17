/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
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

public class ASTRewritingModifyingReplaceTest extends ASTRewritingModifyingTest {
	private static final Class THIS = ASTRewritingModifyingReplaceTest.class;

	public ASTRewritingModifyingReplaceTest(String name) {
		super(name);
	}

	public static Test allTests() {
		return new Suite(THIS);
	}

	public static Test suite() {
		return allTests();
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

		Name name = astRoot.getAST().newSimpleName("aaa");
		astRoot.getPackage().setName(name);

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuffer();
		buf.append("package aaa;\n");
		buf.append("public class X {\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void test0002() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0002", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0002;\n");
		buf.append("public class X {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		AST a = astRoot.getAST();

		Name name = a.newSimpleName("aaa");
		PackageDeclaration pack = a.newPackageDeclaration();
		pack.setName(name);
		astRoot.setPackage(pack);

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuffer();
		buf.append("package aaa;\n");
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

		Name name = astRoot.getAST().newSimpleName("aaa");
		List imports = astRoot.imports();
		ImportDeclaration imp = (ImportDeclaration)imports.get(0);
		imp.setName(name);

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuffer();
		buf.append("package test0003;\n");
		buf.append("import aaa.*;\n");
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
		buf.append("import java.util.*;\n");
		buf.append("import java.lang.*;\n");
		buf.append("import java.awt.*;\n");
		buf.append("public class X {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		List types = astRoot.types();
		SimpleName name = astRoot.getAST().newSimpleName("AAA");
		TypeDeclaration t = (TypeDeclaration)types.get(0);
		t.setName(name);

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuffer();
		buf.append("package test0004;\n");
		buf.append("import java.util.*;\n");
		buf.append("import java.lang.*;\n");
		buf.append("import java.awt.*;\n");
		buf.append("public class AAA {\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void test0005() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0005", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0005;\n");
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
		TypeDeclaration typeDeclaration = a.newTypeDeclaration();
		SimpleName name = a.newSimpleName("AAA");
		typeDeclaration.setName(name);
		types.set(1, typeDeclaration);

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuffer();
		buf.append("package test0005;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("\n");
		buf.append("}\n");
		buf.append("class AAA {\n");
		buf.append("}\n");
		buf.append("class Z {\n");
		buf.append("\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void test0006() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0006", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0006;\n");
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
		types.set(1, typeDeclaration1);

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuffer();
		buf.append("package test0006;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("\n");
		buf.append("}\n");
		buf.append("class A {\n");
		buf.append("}\n");
		buf.append("class Z {\n");
		buf.append("\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=192233
	public void test0007() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0007", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0007;\n");
		buf.append("public class X {\n");
		buf.append("    List/**/getUsers() {}\n");
		buf.append("}\n");

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false, AST.JLS3);

		astRoot.recordModifications();

		AST ast = astRoot.getAST();

		List types = astRoot.types();
		List list= ((TypeDeclaration) types.get(0)).bodyDeclarations();
		MethodDeclaration methodDecl= (MethodDeclaration) list.get(0);

		methodDecl.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuffer();
		buf.append("package test0007;\n");
		buf.append("public class X {\n");
		buf.append("    void getUsers() {}\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=192233
	public void test0008() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0008", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0008;\n");
		buf.append("public class X {\n");
		buf.append("    List /**/getUsers() {}\n");
		buf.append("}\n");

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false, AST.JLS3);

		astRoot.recordModifications();

		AST ast = astRoot.getAST();

		List types = astRoot.types();
		List list= ((TypeDeclaration) types.get(0)).bodyDeclarations();
		MethodDeclaration methodDecl= (MethodDeclaration) list.get(0);

		methodDecl.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuffer();
		buf.append("package test0008;\n");
		buf.append("public class X {\n");
		buf.append("    void getUsers() {}\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=192233
	public void test0009() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0009", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0009;\n");
		buf.append("public class X {\n");
		buf.append("    List/**/ getUsers() {}\n");
		buf.append("}\n");

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false, AST.JLS3);

		astRoot.recordModifications();

		AST ast = astRoot.getAST();

		List types = astRoot.types();
		List list= ((TypeDeclaration) types.get(0)).bodyDeclarations();
		MethodDeclaration methodDecl= (MethodDeclaration) list.get(0);

		methodDecl.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuffer();
		buf.append("package test0009;\n");
		buf.append("public class X {\n");
		buf.append("    void getUsers() {}\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=192233
	public void test0010() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0010", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0010;\n");
		buf.append("public class X {\n");
		buf.append("    void getUsers(List/**/list) {}\n");
		buf.append("}\n");

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false, AST.JLS3);

		astRoot.recordModifications();

		AST ast = astRoot.getAST();

		List types = astRoot.types();
		List list= ((TypeDeclaration) types.get(0)).bodyDeclarations();
		MethodDeclaration methodDecl= (MethodDeclaration) list.get(0);
		List parameters = methodDecl.parameters();
		SingleVariableDeclaration variableDeclaration = (SingleVariableDeclaration) parameters.get(0);
		variableDeclaration.setType(ast.newPrimitiveType(PrimitiveType.INT));

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuffer();
		buf.append("package test0010;\n");
		buf.append("public class X {\n");
		buf.append("    void getUsers(int list) {}\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=192233
	public void test0011() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0011", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0011;\n");
		buf.append("public class X {\n");
		buf.append("    void getUsers(int i, List/**/list) {}\n");
		buf.append("}\n");

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false, AST.JLS3);

		astRoot.recordModifications();

		AST ast = astRoot.getAST();

		List types = astRoot.types();
		List list= ((TypeDeclaration) types.get(0)).bodyDeclarations();
		MethodDeclaration methodDecl= (MethodDeclaration) list.get(0);
		List parameters = methodDecl.parameters();
		SingleVariableDeclaration variableDeclaration = (SingleVariableDeclaration) parameters.get(1);
		variableDeclaration.setType(ast.newPrimitiveType(PrimitiveType.INT));

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuffer();
		buf.append("package test0011;\n");
		buf.append("public class X {\n");
		buf.append("    void getUsers(int i, int list) {}\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=192233
	public void test0012() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0012", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0012;\n");
		buf.append("public class X {\n");
		buf.append("    void getUsers(int i, List.../**/list) {}\n");
		buf.append("}\n");

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false, AST.JLS3);

		astRoot.recordModifications();

		AST ast = astRoot.getAST();

		List types = astRoot.types();
		List list= ((TypeDeclaration) types.get(0)).bodyDeclarations();
		MethodDeclaration methodDecl= (MethodDeclaration) list.get(0);
		List parameters = methodDecl.parameters();
		SingleVariableDeclaration variableDeclaration = (SingleVariableDeclaration) parameters.get(1);
		variableDeclaration.setType(ast.newPrimitiveType(PrimitiveType.INT));

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuffer();
		buf.append("package test0012;\n");
		buf.append("public class X {\n");
		buf.append("    void getUsers(int i, int.../**/list) {}\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=192233
	public void test0013() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0013", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0013;\n");
		buf.append("public class X {\n");
		buf.append("    List/**/list;\n");
		buf.append("}\n");

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false, AST.JLS3);

		astRoot.recordModifications();

		AST ast = astRoot.getAST();

		List types = astRoot.types();
		List list= ((TypeDeclaration) types.get(0)).bodyDeclarations();
		FieldDeclaration fieldDeclaration= (FieldDeclaration) list.get(0);
		fieldDeclaration.setType(ast.newPrimitiveType(PrimitiveType.INT));

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuffer();
		buf.append("package test0013;\n");
		buf.append("public class X {\n");
		buf.append("    int list;\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
}
