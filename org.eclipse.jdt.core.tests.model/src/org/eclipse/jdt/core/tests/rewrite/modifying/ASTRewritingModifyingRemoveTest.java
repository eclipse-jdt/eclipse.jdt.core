/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.rewrite.modifying;

import java.util.List;
import junit.framework.Test;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.*;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ASTRewritingModifyingRemoveTest extends ASTRewritingModifyingTest {
	private static final Class THIS = ASTRewritingModifyingRemoveTest.class;

	public ASTRewritingModifyingRemoveTest(String name) {
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
		StringBuilder buf= new StringBuilder();
		buf.append("package test0001;\n");
		buf.append("public class X {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		astRoot.setPackage(null);

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuilder();
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void test0002() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0002", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test0002;\n");
		buf.append("import java.util.*;\n");
		buf.append("import java.lang.*;\n");
		buf.append("import java.awt.*;\n");
		buf.append("public class X {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		List imports = astRoot.imports();
		imports.remove(0);

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuilder();
		buf.append("package test0002;\n");
		buf.append("import java.lang.*;\n");
		buf.append("import java.awt.*;\n");
		buf.append("public class X {\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void test0003() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0003", false, null);
		StringBuilder buf= new StringBuilder();
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

		List types = astRoot.types();
		types.remove(1);

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuilder();
		buf.append("package test0003;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("\n");
		buf.append("}\n");
		buf.append("class Z {\n");
		buf.append("\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void test0004() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0004", false, null);
		StringBuilder buf= new StringBuilder();
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

		buf= new StringBuilder();
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

//	public void test0005() throws Exception {
//		IPackageFragment pack1= fSourceFolder.createPackageFragment("test0005", false, null);
//		StringBuffer buf= new StringBuffer();
//		buf.append("package test0005;\n");
//		buf.append("\n");
//		buf.append("/**\n");
//		buf.append(" * NOTHING\n");
//		buf.append(" * @since now\n");
//		buf.append(" */\n");
//		buf.append("public class X {\n");
//		buf.append("\n");
//		buf.append("}\n");
//		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);
//
//		CompilationUnit astRoot= parseCompilationUnit(cu, false);
//
//		astRoot.recordModifications();
//
//		List types = astRoot.types();
//		TypeDeclaration typeDeclaration = (TypeDeclaration)types.get(0);
//		typeDeclaration.setJavadoc(null);
//
//		String preview = evaluateRewrite(cu, astRoot);
//
//		buf= new StringBuffer();
//		buf.append("package test0005;\n");
//		buf.append("\n");
//		buf.append("\n");
//		buf.append("public class X {\n");
//		buf.append("\n");
//		buf.append("}\n");
//		assertEqualString(preview, buf.toString());
//	}

	public void test0006() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0006", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test0006;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("    void foo() {\n");
		buf.append("        bar1();\n");
		buf.append("        \n");
		buf.append("        //comment1\n");
		buf.append("        bar2();//comment2\n");
		buf.append("        //comment3\n");
		buf.append("        bar3();\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		List types = astRoot.types();
		TypeDeclaration typeDeclaration = (TypeDeclaration)types.get(0);
		MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
		Block body = methodDeclaration.getBody();
		List statements = body.statements();
		statements.remove(1);

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuilder();
		buf.append("package test0006;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("    void foo() {\n");
		buf.append("        bar1();\n");
		buf.append("        \n");
		buf.append("        //comment3\n");
		buf.append("        bar3();\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

//	public void test0007() throws Exception {
//		IPackageFragment pack1= fSourceFolder.createPackageFragment("test0007", false, null);
//		StringBuffer buf= new StringBuffer();
//		buf.append("package test0007;\n");
//		buf.append("\n");
//		buf.append("public class X {\n");
//		buf.append("    /*\\u002A\n");
//		buf.append("     * NOTHING\n");
//		buf.append("     * @see Object\n");
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
//		List types = astRoot.types();
//		TypeDeclaration typeDeclaration = (TypeDeclaration)types.get(0);
//		typeDeclaration = typeDeclaration.getTypes()[0];
//		Javadoc javadoc = typeDeclaration.getJavadoc();
//		List tags = javadoc.tags();
//		tags.remove(0);
//
//		String preview = evaluateRewrite(cu, astRoot);
//
//		buf= new StringBuffer();
//		buf.append("package test0007;\n");
//		buf.append("\n");
//		buf.append("public class X {\n");
//		buf.append("    /*\\u002A\n");
//		buf.append("     * @see Object\n");
//		buf.append("     */\n");
//		buf.append("    public class Y {\n");
//		buf.append("    \n");
//		buf.append("    }\n");
//		buf.append("}\n");
//		assertEqualString(preview, buf.toString());
//	}
//
//	public void test0008() throws Exception {
//		IPackageFragment pack1= fSourceFolder.createPackageFragment("test0008", false, null);
//		StringBuffer buf= new StringBuffer();
//		buf.append("package test0008;\n");
//		buf.append("\n");
//		buf.append("public class X {\n");
//		buf.append("    /*\\u002A\n");
//		buf.append("     * NOTHING\n");
//		buf.append("     * @see Object\n");
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
//		List types = astRoot.types();
//		TypeDeclaration typeDeclaration = (TypeDeclaration)types.get(0);
//		typeDeclaration = typeDeclaration.getTypes()[0];
//		Javadoc javadoc = typeDeclaration.getJavadoc();
//		List tags = javadoc.tags();
//		tags.remove(1);
//
//		String preview = evaluateRewrite(cu, astRoot);
//
//		buf= new StringBuffer();
//		buf.append("package test0008;\n");
//		buf.append("\n");
//		buf.append("public class X {\n");
//		buf.append("    /*\\u002A\n");
//		buf.append("     * NOTHING\n");
//		buf.append("     */\n");
//		buf.append("    public class Y {\n");
//		buf.append("    \n");
//		buf.append("    }\n");
//		buf.append("}\n");
//		assertEqualString(preview, buf.toString());
//	}
	public void test0009() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0009", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test0009;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("    // comment1\n");
		buf.append("\n");
		buf.append("    // comment2\n");
		buf.append("    // comment3\n");
		buf.append("    void foo() {\n");
		buf.append("    }\n");
		buf.append("    // comment4\n");
		buf.append("    void foo2() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		List types = astRoot.types();
		TypeDeclaration typeDeclaration = (TypeDeclaration)types.get(0);
		MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
		typeDeclaration.bodyDeclarations().remove(methodDeclaration);

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuilder();
		buf.append("package test0009;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("    // comment1\n");
		buf.append("\n");
		buf.append("    // comment4\n");
		buf.append("    void foo2() {\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void test0010() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0010", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test0010;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("    // comment1\n");
		buf.append("\n");
		buf.append("    // comment2\n");
		buf.append("    // comment3\n");
		buf.append("    void foo() {\n");
		buf.append("    }\n");
		buf.append("    // comment4\n");
		buf.append("\n");
		buf.append("    // comment5\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		List types = astRoot.types();
		TypeDeclaration typeDeclaration = (TypeDeclaration)types.get(0);
		MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
		typeDeclaration.bodyDeclarations().remove(methodDeclaration);

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuilder();
		buf.append("package test0010;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("    // comment1\n");
		buf.append("\n");
		buf.append("    \n");
		buf.append("\n");
		buf.append("    // comment5\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void test0011() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0011", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test0011;\n");
		buf.append("public class X {\n");
		buf.append("    // one line comment\n");
		buf.append("    private void foo(){\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    /**\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    private void foo1(){\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    private void foo2(){\n");
		buf.append("    }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		List types = astRoot.types();
		TypeDeclaration typeDeclaration = (TypeDeclaration)types.get(0);
		MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
		typeDeclaration.bodyDeclarations().remove(methodDeclaration);

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuilder();
		buf.append("package test0011;\n");
		buf.append("public class X {\n");
		buf.append("    /**\n");
		buf.append("     *\n");
		buf.append("     */\n");
		buf.append("    private void foo1(){\n");
		buf.append("    }\n");
		buf.append("\n");
		buf.append("    private void foo2(){\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=306524
	 * To test that when types are removed, only the comments in the extended source range are
	 * removed, and the rest are left untouched.
	 */
	public void test0012() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0012", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test0012;\n");
		buf.append("public class X {\n");
		buf.append("\n");
		buf.append("    // one line comment1\n");
		buf.append("\n");
		buf.append("    /*\n");
		buf.append("     * comment2\n");
		buf.append("     */\n");
		buf.append("\n");
		buf.append("    // one line comment3\n");
		buf.append("    class X1{\n");
		buf.append("    }\n");
		buf.append("    // one line comment4\n");
		buf.append("\n");
		buf.append("\n");
		buf.append("    // one line comment5\n");
		buf.append("\n");
		buf.append("    /*\n");
		buf.append("     * comment6\n");
		buf.append("     */\n");
		buf.append("\n");
		buf.append("\n");
		buf.append("    /*\n");
		buf.append("     * comment7\n");
		buf.append("     */\n");
		buf.append("\n");
		buf.append("    // one line comment8\n");
		buf.append("    // one line comment9\n");
		buf.append("    class X2{\n");
		buf.append("    }\n");
		buf.append("    /*\n");
		buf.append("     * comment10\n");
		buf.append("     */\n");
		buf.append("\n");
		buf.append("    // one line comment11\n");
		buf.append("    // one line comment12\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		List types = astRoot.types();
		TypeDeclaration typeDeclaration = (TypeDeclaration)types.get(0);
		TypeDeclaration [] members = typeDeclaration.getTypes();
		typeDeclaration.bodyDeclarations().remove(members[0]);
		typeDeclaration.bodyDeclarations().remove(members[1]);

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuilder();
		buf.append("package test0012;\n");
		buf.append("public class X {\n");
		buf.append("\n");
		buf.append("    // one line comment1\n");
		buf.append("\n");
		buf.append("    /*\n");
		buf.append("     * comment2\n");
		buf.append("     */\n");
		buf.append("\n");
		buf.append("    \n");
		buf.append("\n");
		buf.append("\n");
		buf.append("    // one line comment5\n");
		buf.append("\n");
		buf.append("    /*\n");
		buf.append("     * comment6\n");
		buf.append("     */\n");
		buf.append("\n");
		buf.append("\n");
		buf.append("    /*\n");
		buf.append("     * comment7\n");
		buf.append("     */\n");
		buf.append("\n");
		buf.append("    \n");
		buf.append("\n");
		buf.append("    // one line comment11\n");
		buf.append("    // one line comment12\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=306524
	 * To test that when types are removed, only the comments in the extended source range are
	 * removed, and the rest are left untouched. This test is for cases where type to be removed is the
	 * last one in file and there are more than one leading comments that are not part of its
	 * extended source range
	 */
	public void test0013() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0013", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test0013;\n");
		buf.append("public class X {\n");
		buf.append("\n");
		buf.append("    // one line comment1a\n");
		buf.append("\n");
		buf.append("    /*\n");
		buf.append("     * comment2\n");
		buf.append("     */\n");
		buf.append("\n");
		buf.append("    // one line comment1b\n");
		buf.append("\n");
		buf.append("    // one line comment3\n");
		buf.append("    class X1{\n");
		buf.append("    }\n");
		buf.append("    // one line comment4\n");
		buf.append("\n");
		buf.append("\n");
		buf.append("    // one line comment5\n");
		buf.append("\n");
		buf.append("    /*\n");
		buf.append("     * comment6\n");
		buf.append("     */\n");
		buf.append("\n");
		buf.append("\n");
		buf.append("    /*\n");
		buf.append("     * comment7\n");
		buf.append("     */\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		List types = astRoot.types();
		TypeDeclaration typeDeclaration = (TypeDeclaration)types.get(0);
		TypeDeclaration [] members = typeDeclaration.getTypes();
		typeDeclaration.bodyDeclarations().remove(members[0]);

		String preview = evaluateRewrite(cu, astRoot);
		buf= new StringBuilder();
		buf.append("package test0013;\n");
		buf.append("public class X {\n");
		buf.append("\n");
		buf.append("    // one line comment1a\n");
		buf.append("\n");
		buf.append("    /*\n");
		buf.append("     * comment2\n");
		buf.append("     */\n");
		buf.append("\n");
		buf.append("    // one line comment1b\n");
		buf.append("\n");
		buf.append("    \n");
		buf.append("\n");
		buf.append("\n");
		buf.append("    // one line comment5\n");
		buf.append("\n");
		buf.append("    /*\n");
		buf.append("     * comment6\n");
		buf.append("     */\n");
		buf.append("\n");
		buf.append("\n");
		buf.append("    /*\n");
		buf.append("     * comment7\n");
		buf.append("     */\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=446446
	 */
	public void testBug446446_001() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("testBug446446_001", false, null);
		String contents =
				"package testBug446446_001;\n"+
				"public class X {\n " +
				"    public static void main(String[] args) {\n" +
				"        X bug = new X(\n" +
				"                1.0e-3  // some comment\n" +
				"                , null);\n" +
				"    }\n" +
				"\n" +
				"    X(double d) {\n" +
				"    }\n" +
				"}\n";

		StringBuilder buf= new StringBuilder(contents);
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot = createCU(cu, false);

		astRoot.recordModifications();

		List types = astRoot.types();
		TypeDeclaration typeDeclaration = (TypeDeclaration)types.get(0);
		MethodDeclaration method = (MethodDeclaration) typeDeclaration.bodyDeclarations().get(0);
		VariableDeclarationStatement statement = (VariableDeclarationStatement) method.getBody().statements().get(0);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) statement.fragments().get(0);
		ClassInstanceCreation instance = (ClassInstanceCreation) fragment.getInitializer();
		instance.arguments().remove(1);

		String preview = evaluateRewrite(cu, astRoot);
		String expected =
				"package testBug446446_001;\n"+
				"public class X {\n " +
				"    public static void main(String[] args) {\n" +
				"        X bug = new X(\n" +
				"                1.0e-3  // some comment\n" +
				");\n" +
				"    }\n" +
				"\n" +
				"    X(double d) {\n" +
				"    }\n" +
				"}\n";
		assertEqualString(preview, expected);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=446446
	 */
	public void testBug446446_002() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("testBug446446_002", false, null);
		String contents =
				"package testBug446446_002;\n"+
				"public class X {\n"+
				"    public void foo() {\n"+
				"        if (i == 0) {\n"+
				"            foo();\n"+
				"            i++; // comment\n"+
				"            i++;}"+
				"    }\n"+
				"}\n";

		StringBuilder buf= new StringBuilder(contents);
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		List types = astRoot.types();
		TypeDeclaration typeDeclaration = (TypeDeclaration)types.get(0);
		MethodDeclaration methodDecl = (MethodDeclaration) typeDeclaration.bodyDeclarations().get(0);
		List statements= methodDecl.getBody().statements();
		IfStatement ifStatement= (IfStatement) statements.get(0);
		Block thenBlock= (Block) ifStatement.getThenStatement();
		thenBlock.statements().remove(2);
		String preview = evaluateRewrite(cu, astRoot);
		String expected =
				"package testBug446446_002;\n"+
				"public class X {\n"+
				"    public void foo() {\n"+
				"        if (i == 0) {\n"+
				"            foo();\n"+
				"            i++; // comment\n"+
				"}    }\n"+
				"}\n";
		assertEqualString(preview, expected);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=446446
	 */
	public void testBug446446_003() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("testBug446446_003", false, null);
		String contents =
				"package testBug446446_003;\n"+
				"public class X {\n"+
				"    public void foo() {\n"+
				"        if (i == 0) {\n"+
				"            foo();\n"+
				"            i++; // comment\n"+
				"            i++;\n"+
				"        }\n"+
				"    }\n"+
				"}\n";

		StringBuilder buf= new StringBuilder(contents);
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		List types = astRoot.types();
		TypeDeclaration typeDeclaration = (TypeDeclaration)types.get(0);
		MethodDeclaration methodDecl = (MethodDeclaration) typeDeclaration.bodyDeclarations().get(0);
		List statements= methodDecl.getBody().statements();
		IfStatement ifStatement= (IfStatement) statements.get(0);
		Block thenBlock= (Block) ifStatement.getThenStatement();
		thenBlock.statements().remove(2);
		String preview = evaluateRewrite(cu, astRoot);
		String expected =
				"package testBug446446_003;\n"+
				"public class X {\n"+
				"    public void foo() {\n"+
				"        if (i == 0) {\n"+
				"            foo();\n"+
				"            i++; // comment\n"+
				"        }\n"+
				"    }\n"+
				"}\n";
		assertEqualString(preview, expected);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=446446
	 */
	public void testBug446446_004() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("testBug446446_004", false, null);
		String contents =
				"package testBug446446_004;\n"+
				"public class X {\n"+
				"    public void foo() {\n"+
				"        if (i == 0) {\n"+
				"            foo();\n"+
				"            i++; // comment\n"+
				"            i++;\n"+
				"            i++;}\n"+
				"    }\n"+
				"}\n";

		StringBuilder buf= new StringBuilder(contents);
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		List types = astRoot.types();
		TypeDeclaration typeDeclaration = (TypeDeclaration)types.get(0);
		MethodDeclaration methodDecl = (MethodDeclaration) typeDeclaration.bodyDeclarations().get(0);
		List statements= methodDecl.getBody().statements();
		IfStatement ifStatement= (IfStatement) statements.get(0);
		Block thenBlock= (Block) ifStatement.getThenStatement();
		thenBlock.statements().remove(2);
		thenBlock.statements().remove(2);
		String preview = evaluateRewrite(cu, astRoot);
		String expected =
				"package testBug446446_004;\n"+
				"public class X {\n"+
				"    public void foo() {\n"+
				"        if (i == 0) {\n"+
				"            foo();\n"+
				"            i++; // comment\n"+
				"}\n"+
				"    }\n"+
				"}\n";
		assertEqualString(preview, expected);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=446446
	 */
	public void testBug446446_005() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("testBug446446_005", false, null);
		String contents =
				"package testBug446446_005;\n"+
				"public class X {\n"+
				"    public void foo() {\n"+
				"        if (i == 0) {\n"+
				"            foo();\n"+
				"            i++; // comment\n"+
				"            i++;}"+
				"    }\n"+
				"}\n";

		StringBuilder buf= new StringBuilder(contents);
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		List types = astRoot.types();
		TypeDeclaration typeDeclaration = (TypeDeclaration)types.get(0);
		MethodDeclaration methodDecl = (MethodDeclaration) typeDeclaration.bodyDeclarations().get(0);
		List statements= methodDecl.getBody().statements();
		IfStatement ifStatement= (IfStatement) statements.get(0);
		Block thenBlock= (Block) ifStatement.getThenStatement();
		thenBlock.statements().remove(2);

		AST ast= astRoot.getAST();
		PrefixExpression expression= ast.newPrefixExpression();
		expression.setOperand(ast.newSimpleName("i"));
		expression.setOperator(PrefixExpression.Operator.DECREMENT);
		ExpressionStatement newStatement= ast.newExpressionStatement(expression);
		thenBlock.statements().add(newStatement);

		String preview = evaluateRewrite(cu, astRoot);
		String expected =
				"package testBug446446_005;\n"+
				"public class X {\n"+
				"    public void foo() {\n"+
				"        if (i == 0) {\n"+
				"            foo();\n"+
				"            i++; // comment\n"+
				"            --i;}    }\n"+
				"}\n";
		assertEqualString(preview, expected);
	}
}
