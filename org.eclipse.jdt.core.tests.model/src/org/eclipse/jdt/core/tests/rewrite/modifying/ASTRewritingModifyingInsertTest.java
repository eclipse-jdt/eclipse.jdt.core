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

public class ASTRewritingModifyingInsertTest extends ASTRewritingModifyingTest {
	private static final Class THIS = ASTRewritingModifyingInsertTest.class;

	public ASTRewritingModifyingInsertTest(String name) {
		super(name);
	}

	public static Test allTests() {
		return new Suite(THIS);
	}

	public static Test suite() {
		return allTests();
	}
	/**
	 * insert a new import declaration
	 */
	public void test0001() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0001", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0001;\n");
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
		Name name = a.newSimpleName("aaa");
		ImportDeclaration importDeclaration = a.newImportDeclaration();
		importDeclaration.setName(name);
		importDeclaration.setOnDemand(true);
		imports.add(importDeclaration);

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuffer();
		buf.append("package test0001;\n");
		buf.append("import java.util.*;\n");
		buf.append("import java.lang.*;\n");
		buf.append("import java.awt.*;\n");
		buf.append("import aaa.*;\n");
		buf.append("public class X {\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	/**
	 * insert a new type at first position
	 */
	public void test0002() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0002", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0002;\n");
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
		types.add(0, typeDeclaration);

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuffer();
		buf.append("package test0002;\n");
		buf.append("\n");
		buf.append("class AAA {\n");
		buf.append("}\n");
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

	/**
	 * insert a new type
	 */
	public void test0003() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0003", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0003;\n");
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
		types.add(1, typeDeclaration);

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuffer();
		buf.append("package test0003;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("\n");
		buf.append("}\n");
		buf.append("class AAA {\n");
		buf.append("}\n");
		buf.append("class Y {\n");
		buf.append("\n");
		buf.append("}\n");
		buf.append("class Z {\n");
		buf.append("\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	/**
	 * insert a new type at last position
	 */
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
		TypeDeclaration typeDeclaration = a.newTypeDeclaration();
		SimpleName name = a.newSimpleName("AAA");
		typeDeclaration.setName(name);
		types.add(3, typeDeclaration);

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
		buf.append("class AAA {\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
//	/**
//	 * insert a new javadoc for a type
//	 */
//	public void test0005() throws Exception {
//		IPackageFragment pack1= fSourceFolder.createPackageFragment("test0005", false, null);
//		StringBuffer buf= new StringBuffer();
//		buf.append("package test0005;\n");
//		buf.append("\n");
//		buf.append("public class X {\n");
//		buf.append("\n");
//		buf.append("}\n");
//		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);
//
//		CompilationUnit astRoot= parseCompilationUnit(cu, false);
//
//		astRoot.recordModifications();
//
//		AST a = astRoot.getAST();
//
//		List types = astRoot.types();
//		TypeDeclaration typeDeclaration = (TypeDeclaration)types.get(0);
//		Javadoc javadoc = a.newJavadoc();
//		List tags = javadoc.tags();
//		TagElement tag = a.newTagElement();
//		List fragment = tag.fragments();
//		TextElement text = a.newTextElement();
//		text.setText("NOTHING");
//		fragment.add(text);
//		tags.add(tag);
//		typeDeclaration.setJavadoc(javadoc);
//
//		String preview = evaluateRewrite(cu, astRoot);
//
//		buf= new StringBuffer();
//		buf.append("package test0005;\n");
//		buf.append("\n");
//		buf.append("/**\n");
//		buf.append(" * NOTHING\n");
//		buf.append(" */\n");
//		buf.append("public class X {\n");
//		buf.append("\n");
//		buf.append("}\n");
//		assertEqualString(preview, buf.toString());
//	}

	/**
	 * insert a new member type
	 */
	public void test0006() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0006", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0006;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		AST a = astRoot.getAST();

		List types = astRoot.types();
		TypeDeclaration typeDeclaration = (TypeDeclaration)types.get(0);
		List body = typeDeclaration.bodyDeclarations();
		TypeDeclaration typeDeclaration2 = a.newTypeDeclaration();
		typeDeclaration2.setName(a.newSimpleName("Z"));
		body.add(typeDeclaration2);

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuffer();
		buf.append("package test0006;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("\n");
		buf.append("    class Z {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	/**
	 * insert a new member type after another member type
	 */
	public void test0007() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0007", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0007;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("    public class Y {\n");
		buf.append("    \n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		AST a = astRoot.getAST();

		List types = astRoot.types();
		TypeDeclaration typeDeclaration = (TypeDeclaration)types.get(0);
		List body = typeDeclaration.bodyDeclarations();
		TypeDeclaration typeDeclaration2 = a.newTypeDeclaration();
		typeDeclaration2.setName(a.newSimpleName("Z"));
		body.add(typeDeclaration2);

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuffer();
		buf.append("package test0007;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("    public class Y {\n");
		buf.append("    \n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    class Z {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

//	/**
//	 * insert a new javadoc fort a member type
//	 */
//	public void test0008() throws Exception {
//		IPackageFragment pack1= fSourceFolder.createPackageFragment("test0008", false, null);
//		StringBuffer buf= new StringBuffer();
//		buf.append("package test0008;\n");
//		buf.append("\n");
//		buf.append("public class X {\n");
//		buf.append("    public class Y {\n");
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
//		List types = astRoot.types();
//		TypeDeclaration typeDeclaration = (TypeDeclaration)types.get(0);
//		typeDeclaration = typeDeclaration.getTypes()[0];
//		Javadoc javadoc = a.newJavadoc();
//		List tags = javadoc.tags();
//		TagElement tag = a.newTagElement();
//		List fragment = tag.fragments();
//		TextElement text = a.newTextElement();
//		text.setText("NOTHING");
//		fragment.add(text);
//		tags.add(tag);
//		typeDeclaration.setJavadoc(javadoc);
//
//		String preview = evaluateRewrite(cu, astRoot);
//
//		buf= new StringBuffer();
//		buf.append("package test0008;\n");
//		buf.append("\n");
//		buf.append("public class X {\n");
//		buf.append("    /**\n");
//		buf.append("     * NOTHING\n");
//		buf.append("     */\n");
//		buf.append("    public class Y {\n");
//		buf.append("    \n");
//		buf.append("    }\n");
//		buf.append("}\n");
//		assertEqualString(preview, buf.toString());
//	}
//
//	/**
//	 * insert a new javadoc fort a member type
//	 */
//	public void test0009() throws Exception {
//		IPackageFragment pack1= fSourceFolder.createPackageFragment("test0009", false, null);
//		StringBuffer buf= new StringBuffer();
//		buf.append("package test0009;\n");
//		buf.append("\n");
//		buf.append("public class X {\n");
//		buf.append("    /*\\u002A\n");
//		buf.append("     * NOTHING\n");
//		buf.append("     */\n");
//		buf.append("    public class Y {\n");
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
//		List types = astRoot.types();
//		TypeDeclaration typeDeclaration = (TypeDeclaration)types.get(0);
//		typeDeclaration = typeDeclaration.getTypes()[0];
//		Javadoc javadoc = typeDeclaration.getJavadoc();
//		List tags = javadoc.tags();
//		TagElement tag = a.newTagElement();
//		tag.setTagName("@nothing");
//		TextElement textElement = a.newTextElement();
//		textElement.setText(" none");
//		tag.fragments().add(textElement);
//		tags.add(tag);
//
//		String preview = evaluateRewrite(cu, astRoot);
//
//		buf= new StringBuffer();
//		buf.append("package test0008;\n");
//		buf.append("\n");
//		buf.append("public class X {\n");
//		buf.append("    public class Y {\n");
//		buf.append("    \n");
//		buf.append("    }\n");
//		buf.append("}\n");
//		assertEqualString(preview, buf.toString());
//	}

	/** @deprecated using deprecated code */
	public void test0010() throws Exception {
		String source = "\n";
		CompilationUnit astRoot= createCU(source.toCharArray());
		astRoot.recordModifications();

		AST a = astRoot.getAST();

		PackageDeclaration packageDeclaration =  a.newPackageDeclaration();
		packageDeclaration.setName(a.newSimpleName("test0010"));
		astRoot.setPackage(packageDeclaration);
		TypeDeclaration typeDeclaration =  a.newTypeDeclaration();
		typeDeclaration.setName(a.newSimpleName("X"));
		typeDeclaration.setModifiers(Modifier.PUBLIC);

		astRoot.types().add(typeDeclaration);

		String preview = evaluateRewrite(source, astRoot);

		StringBuffer buf= new StringBuffer();
		buf.append("package test0010;\n");
		buf.append("\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void test0011() throws Exception {
		String source = "\n";
		CompilationUnit astRoot= createCU(source.toCharArray());
		astRoot.recordModifications();

		AST a = astRoot.getAST();

		PackageDeclaration packageDeclaration =  a.newPackageDeclaration();
		packageDeclaration.setName(a.newSimpleName("test0011"));
		astRoot.setPackage(packageDeclaration);

		String preview = evaluateRewrite(source, astRoot);

		StringBuffer buf= new StringBuffer();
		buf.append("package test0011;\n");
		buf.append("\n");
		assertEqualString(preview, buf.toString());
	}

	/** @deprecated using deprecated code */
	public void test0012() throws Exception {
		String source = "\n";
		CompilationUnit astRoot= createCU(source.toCharArray());
		astRoot.recordModifications();

		AST a = astRoot.getAST();

		TypeDeclaration typeDeclaration =  a.newTypeDeclaration();
		typeDeclaration.setName(a.newSimpleName("X"));
		typeDeclaration.setModifiers(Modifier.PUBLIC);

		astRoot.types().add(typeDeclaration);

		String preview = evaluateRewrite(source, astRoot);

		StringBuffer buf= new StringBuffer();
		buf.append("\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=317468
	 */
	public void test0013() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0013", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0013;\n");
		buf.append("public enum X {\n");
		buf.append("	A, B,\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false, AST.JLS3);

		astRoot.recordModifications();

		AST ast = astRoot.getAST();

		final List types = astRoot.types();
		VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
		fragment.setName(ast.newSimpleName("field"));
		final FieldDeclaration fieldDeclaration = ast.newFieldDeclaration(fragment);
		fieldDeclaration.setType(ast.newPrimitiveType(PrimitiveType.INT));
		((AbstractTypeDeclaration) types.get(0)).bodyDeclarations().add(fieldDeclaration);

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuffer();
		buf.append("package test0013;\n");
		buf.append("public enum X {\n");
		buf.append("	A, B,;\n");
		buf.append("\n");
		buf.append("    int field;\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=317468
	 */
	public void test0014() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0014", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0014;\n");
		buf.append("public enum X {\n");
		buf.append("	;\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false, AST.JLS3);

		astRoot.recordModifications();

		AST ast = astRoot.getAST();

		final List types = astRoot.types();
		VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
		fragment.setName(ast.newSimpleName("field"));
		final FieldDeclaration fieldDeclaration = ast.newFieldDeclaration(fragment);
		fieldDeclaration.setType(ast.newPrimitiveType(PrimitiveType.INT));
		((AbstractTypeDeclaration) types.get(0)).bodyDeclarations().add(fieldDeclaration);

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuffer();
		buf.append("package test0014;\n");
		buf.append("public enum X {\n");
		buf.append("	;\n");
		buf.append("\n");
		buf.append("    int field;\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=317468
	 */
	public void test0015() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0015", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test0015;\n");
		buf.append("public enum X {\n");
		buf.append("	A, B,;\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false, AST.JLS3);

		astRoot.recordModifications();

		AST ast = astRoot.getAST();

		final List types = astRoot.types();
		VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
		fragment.setName(ast.newSimpleName("field"));
		final FieldDeclaration fieldDeclaration = ast.newFieldDeclaration(fragment);
		fieldDeclaration.setType(ast.newPrimitiveType(PrimitiveType.INT));
		((AbstractTypeDeclaration) types.get(0)).bodyDeclarations().add(fieldDeclaration);

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuffer();
		buf.append("package test0015;\n");
		buf.append("public enum X {\n");
		buf.append("	A, B,;\n");
		buf.append("\n");
		buf.append("    int field;\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
}
