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
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

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
		String str = """
			package test0001;
			public class X {
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", str, false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		astRoot.setPackage(null);

		String preview = evaluateRewrite(cu, astRoot);

		String str1 = """
			
			public class X {
			}
			""";
		assertEqualString(preview, str1);
	}

	public void test0002() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0002", false, null);
		String str = """
			package test0002;
			import java.util.*;
			import java.lang.*;
			import java.awt.*;
			public class X {
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", str, false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		List imports = astRoot.imports();
		imports.remove(0);

		String preview = evaluateRewrite(cu, astRoot);

		String str1 = """
			package test0002;
			import java.lang.*;
			import java.awt.*;
			public class X {
			}
			""";
		assertEqualString(preview, str1);
	}

	public void test0003() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0003", false, null);
		String str = """
			package test0003;
			
			public class X {
			
			}
			class Y {
			
			}
			class Z {
			
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", str, false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		List types = astRoot.types();
		types.remove(1);

		String preview = evaluateRewrite(cu, astRoot);

		String str1 = """
			package test0003;
			
			public class X {
			
			}
			class Z {
			
			}
			""";
		assertEqualString(preview, str1);
	}

	public void test0004() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0004", false, null);
		String str = """
			package test0004;
			
			public class X {
			
			}
			class Y {
			
			}
			class Z {
			
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", str, false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		AST a = astRoot.getAST();

		List types = astRoot.types();
		TypeDeclaration typeDeclaration1 = a.newTypeDeclaration();
		typeDeclaration1.setName(a.newSimpleName("A"));
		types.add(1, typeDeclaration1);
		types.remove(1);

		String preview = evaluateRewrite(cu, astRoot);

		String str1 = """
			package test0004;
			
			public class X {
			
			}
			class Y {
			
			}
			class Z {
			
			}
			""";
		assertEqualString(preview, str1);
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
		String str = """
			package test0006;
			
			public class X {
			    void foo() {
			        bar1();
			       \s
			        //comment1
			        bar2();//comment2
			        //comment3
			        bar3();
			    }
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", str, false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		List types = astRoot.types();
		TypeDeclaration typeDeclaration = (TypeDeclaration)types.get(0);
		MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
		Block body = methodDeclaration.getBody();
		List statements = body.statements();
		statements.remove(1);

		String preview = evaluateRewrite(cu, astRoot);

		String str1 = """
			package test0006;
			
			public class X {
			    void foo() {
			        bar1();
			       \s
			        //comment3
			        bar3();
			    }
			}
			""";
		assertEqualString(preview, str1);
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
		String str = """
			package test0009;
			
			public class X {
			    // comment1
			
			    // comment2
			    // comment3
			    void foo() {
			    }
			    // comment4
			    void foo2() {
			    }
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", str, false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		List types = astRoot.types();
		TypeDeclaration typeDeclaration = (TypeDeclaration)types.get(0);
		MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
		typeDeclaration.bodyDeclarations().remove(methodDeclaration);

		String preview = evaluateRewrite(cu, astRoot);

		String str1 = """
			package test0009;
			
			public class X {
			    // comment1
			
			    // comment4
			    void foo2() {
			    }
			}
			""";
		assertEqualString(preview, str1);
	}

	public void test0010() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0010", false, null);
		String str = """
			package test0010;
			
			public class X {
			    // comment1
			
			    // comment2
			    // comment3
			    void foo() {
			    }
			    // comment4
			
			    // comment5
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", str, false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		List types = astRoot.types();
		TypeDeclaration typeDeclaration = (TypeDeclaration)types.get(0);
		MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
		typeDeclaration.bodyDeclarations().remove(methodDeclaration);

		String preview = evaluateRewrite(cu, astRoot);

		String str1 = """
			package test0010;
			
			public class X {
			    // comment1
			
			   \s
			
			    // comment5
			}
			""";
		assertEqualString(preview, str1);
	}

	public void test0011() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0011", false, null);
		String str = """
			package test0011;
			public class X {
			    // one line comment
			    private void foo(){
			    }
			
			    /**
			     *
			     */
			    private void foo1(){
			    }
			
			    private void foo2(){
			    }
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", str, false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		List types = astRoot.types();
		TypeDeclaration typeDeclaration = (TypeDeclaration)types.get(0);
		MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
		typeDeclaration.bodyDeclarations().remove(methodDeclaration);

		String preview = evaluateRewrite(cu, astRoot);

		String str1 = """
			package test0011;
			public class X {
			    /**
			     *
			     */
			    private void foo1(){
			    }
			
			    private void foo2(){
			    }
			}
			""";
		assertEqualString(preview, str1);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=306524
	 * To test that when types are removed, only the comments in the extended source range are
	 * removed, and the rest are left untouched.
	 */
	public void test0012() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0012", false, null);
		String str = """
			package test0012;
			public class X {
			
			    // one line comment1
			
			    /*
			     * comment2
			     */
			
			    // one line comment3
			    class X1{
			    }
			    // one line comment4
			
			
			    // one line comment5
			
			    /*
			     * comment6
			     */
			
			
			    /*
			     * comment7
			     */
			
			    // one line comment8
			    // one line comment9
			    class X2{
			    }
			    /*
			     * comment10
			     */
			
			    // one line comment11
			    // one line comment12
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", str, false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		List types = astRoot.types();
		TypeDeclaration typeDeclaration = (TypeDeclaration)types.get(0);
		TypeDeclaration [] members = typeDeclaration.getTypes();
		typeDeclaration.bodyDeclarations().remove(members[0]);
		typeDeclaration.bodyDeclarations().remove(members[1]);

		String preview = evaluateRewrite(cu, astRoot);

		String str1 = """
			package test0012;
			public class X {
			
			    // one line comment1
			
			    /*
			     * comment2
			     */
			
			   \s
			
			
			    // one line comment5
			
			    /*
			     * comment6
			     */
			
			
			    /*
			     * comment7
			     */
			
			   \s
			
			    // one line comment11
			    // one line comment12
			}
			""";
		assertEqualString(preview, str1);
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
		String str = """
			package test0013;
			public class X {
			
			    // one line comment1a
			
			    /*
			     * comment2
			     */
			
			    // one line comment1b
			
			    // one line comment3
			    class X1{
			    }
			    // one line comment4
			
			
			    // one line comment5
			
			    /*
			     * comment6
			     */
			
			
			    /*
			     * comment7
			     */
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", str, false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		List types = astRoot.types();
		TypeDeclaration typeDeclaration = (TypeDeclaration)types.get(0);
		TypeDeclaration [] members = typeDeclaration.getTypes();
		typeDeclaration.bodyDeclarations().remove(members[0]);

		String preview = evaluateRewrite(cu, astRoot);
		String str1 = """
			package test0013;
			public class X {
			
			    // one line comment1a
			
			    /*
			     * comment2
			     */
			
			    // one line comment1b
			
			   \s
			
			
			    // one line comment5
			
			    /*
			     * comment6
			     */
			
			
			    /*
			     * comment7
			     */
			}
			""";
		assertEqualString(preview, str1);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=446446
	 */
	public void testBug446446_001() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("testBug446446_001", false, null);
		String contents =
				"""
			package testBug446446_001;
			public class X {
			 \
			    public static void main(String[] args) {
			        X bug = new X(
			                1.0e-3  // some comment
			                , null);
			    }
			
			    X(double d) {
			    }
			}
			""";

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
				"""
			package testBug446446_001;
			public class X {
			 \
			    public static void main(String[] args) {
			        X bug = new X(
			                1.0e-3  // some comment
			);
			    }
			
			    X(double d) {
			    }
			}
			""";
		assertEqualString(preview, expected);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=446446
	 */
	public void testBug446446_002() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("testBug446446_002", false, null);
		String contents =
				"""
			package testBug446446_002;
			public class X {
			    public void foo() {
			        if (i == 0) {
			            foo();
			            i++; // comment
			            i++;}\
			    }
			}
			""";

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
				"""
			package testBug446446_002;
			public class X {
			    public void foo() {
			        if (i == 0) {
			            foo();
			            i++; // comment
			}    }
			}
			""";
		assertEqualString(preview, expected);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=446446
	 */
	public void testBug446446_003() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("testBug446446_003", false, null);
		String contents =
				"""
			package testBug446446_003;
			public class X {
			    public void foo() {
			        if (i == 0) {
			            foo();
			            i++; // comment
			            i++;
			        }
			    }
			}
			""";

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
				"""
			package testBug446446_003;
			public class X {
			    public void foo() {
			        if (i == 0) {
			            foo();
			            i++; // comment
			        }
			    }
			}
			""";
		assertEqualString(preview, expected);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=446446
	 */
	public void testBug446446_004() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("testBug446446_004", false, null);
		String contents =
				"""
			package testBug446446_004;
			public class X {
			    public void foo() {
			        if (i == 0) {
			            foo();
			            i++; // comment
			            i++;
			            i++;}
			    }
			}
			""";

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
				"""
			package testBug446446_004;
			public class X {
			    public void foo() {
			        if (i == 0) {
			            foo();
			            i++; // comment
			}
			    }
			}
			""";
		assertEqualString(preview, expected);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=446446
	 */
	public void testBug446446_005() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("testBug446446_005", false, null);
		String contents =
				"""
			package testBug446446_005;
			public class X {
			    public void foo() {
			        if (i == 0) {
			            foo();
			            i++; // comment
			            i++;}\
			    }
			}
			""";

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
				"""
			package testBug446446_005;
			public class X {
			    public void foo() {
			        if (i == 0) {
			            foo();
			            i++; // comment
			            --i;}    }
			}
			""";
		assertEqualString(preview, expected);
	}
}
