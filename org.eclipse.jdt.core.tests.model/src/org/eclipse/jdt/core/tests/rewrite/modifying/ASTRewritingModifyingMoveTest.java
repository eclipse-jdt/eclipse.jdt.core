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
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ASTRewritingModifyingMoveTest extends ASTRewritingModifyingTest {
	private static final Class THIS = ASTRewritingModifyingMoveTest.class;

	public ASTRewritingModifyingMoveTest(String name) {
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
		TypeDeclaration typeDeclaration = (TypeDeclaration)types.get(1);
		types.remove(1);
		types.add(typeDeclaration);

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuilder();
		buf.append("package test0001;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("\n");
		buf.append("}\n");
		buf.append("class Z {\n");
		buf.append("\n");
		buf.append("}\n");
		buf.append("class Y {\n");
		buf.append("\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void test0002() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0002", false, null);
		StringBuilder buf= new StringBuilder();
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
		TypeDeclaration typeDeclaration1 = (TypeDeclaration)types.get(1);
		types.remove(1);
		TypeDeclaration typeDeclaration2 = a.newTypeDeclaration();
		typeDeclaration2.setName(a.newSimpleName("A"));
		typeDeclaration2.bodyDeclarations().add(typeDeclaration1);
		types.add(typeDeclaration2);

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuilder();
		buf.append("package test0002;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("\n");
		buf.append("}\n");
		buf.append("class Z {\n");
		buf.append("\n");
		buf.append("}\n");
		buf.append("class A {\n");
		buf.append("    class Y {\n");
		buf.append("    \n");
		buf.append("    }\n");
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

		AST a = astRoot.getAST();

		List types = astRoot.types();
		TypeDeclaration typeDeclaration1 = (TypeDeclaration)types.get(1);
		types.remove(1);
		TypeDeclaration typeDeclaration2 = a.newTypeDeclaration();
		typeDeclaration2.setName(a.newSimpleName("A"));
		typeDeclaration2.bodyDeclarations().add(typeDeclaration1);

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

		List types = astRoot.types();
		TypeDeclaration typeDeclaration1 = (TypeDeclaration)types.get(1);
		types.remove(1);
		types.add(typeDeclaration1);
		types.remove(2);

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuilder();
		buf.append("package test0004;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("\n");
		buf.append("}\n");
		buf.append("class Z {\n");
		buf.append("\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void test0005() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0005", false, null);
		StringBuilder buf= new StringBuilder();
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

		List types = astRoot.types();
		TypeDeclaration typeDeclaration1 = (TypeDeclaration)types.get(1);
		types.remove(1);
		types.add(1, typeDeclaration1);

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuilder();
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
		assertEqualString(preview, buf.toString());
	}

	public void test0006() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0006", false, null);
		StringBuilder buf= new StringBuilder();
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

		List types = astRoot.types();
		TypeDeclaration typeDeclaration = (TypeDeclaration)types.get(0);
		types.remove(0);
		types.set(1, typeDeclaration);

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuilder();
		buf.append("package test0006;\n");
		buf.append("\n");
		buf.append("class Y {\n");
		buf.append("\n");
		buf.append("}\n");
		buf.append("public class X {\n");
		buf.append("\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void test0007() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0007", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test0007;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("\n");
		buf.append("}\n");
		buf.append("class Y {\n");
		buf.append("    int i;\n");
		buf.append("    int foo() {\n");
		buf.append("        \n");
		buf.append("        return i;\n");
		buf.append("    }\n");
		buf.append("}\n");
		buf.append("class Z {\n");
		buf.append("\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		List types = astRoot.types();
		TypeDeclaration typeDeclaration = (TypeDeclaration)types.get(1);
		types.remove(1);
		types.add(typeDeclaration);

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuilder();
		buf.append("package test0007;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("\n");
		buf.append("}\n");
		buf.append("class Z {\n");
		buf.append("\n");
		buf.append("}\n");
		buf.append("class Y {\n");
		buf.append("    int i;\n");
		buf.append("    int foo() {\n");
		buf.append("        \n");
		buf.append("        return i;\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	/** @deprecated using deprecated code */
	public void test0008() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0008", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test0008;\n");
		buf.append("\n");
		buf.append("public class X extends Z1\n");
		buf.append("                        .Z2\n");
		buf.append("                            .Z3 {\n");
		buf.append("\n");
		buf.append("}\n");
		buf.append("class Y {\n");
		buf.append("\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		List types = astRoot.types();
		TypeDeclaration typeDeclaration1 = (TypeDeclaration)types.get(0);
		TypeDeclaration typeDeclaration2 = (TypeDeclaration)types.get(1);
		Name name = typeDeclaration1.getSuperclass();
		typeDeclaration1.setSuperclass(null);
		typeDeclaration2.setSuperclass(name);

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuilder();
		buf.append("package test0008;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("\n");
		buf.append("}\n");
		buf.append("class Y extends Z1\n");
		buf.append("                        .Z2\n");
		buf.append("                            .Z3 {\n");
		buf.append("\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void test0009() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0009", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test0009;\n");
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
		Statement statement = (Statement)statements.get(1);
		statements.remove(1);
		statements.add(statement);

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuilder();
		buf.append("package test0009;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("    void foo() {\n");
		buf.append("        bar1();\n");
		buf.append("        \n");
		buf.append("        //comment3\n");
		buf.append("        bar3();\n");
		buf.append("        //comment1\n");
		buf.append("        bar2();//comment2\n");
		buf.append("    }\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

//	public void test0010() throws Exception {
//		IPackageFragment pack1= fSourceFolder.createPackageFragment("test0010", false, null);
//		StringBuffer buf= new StringBuffer();
//		buf.append("package test0010;\n");
//		buf.append("\n");
//		buf.append("public class X {\n");
//		buf.append("    /**\n");
//		buf.append("     * NOTHING\n");
//		buf.append("     */\n");
//		buf.append("    void foo() {\n");
//		buf.append("    \n");
//		buf.append("    }\n");
//		buf.append("    void bar() {\n");
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
//		MethodDeclaration methodDeclaration1 = typeDeclaration.getMethods()[0];
//		MethodDeclaration methodDeclaration2 = typeDeclaration.getMethods()[1];
//		Javadoc javadoc = methodDeclaration1.getJavadoc();
//		methodDeclaration1.setJavadoc(null);
//		methodDeclaration2.setJavadoc(javadoc);
//
//		String preview = evaluateRewrite(cu, astRoot);
//
//		buf= new StringBuffer();
//		buf.append("package test0010;\n");
//		buf.append("\n");
//		buf.append("public class X {\n");
//		buf.append("    \n");
//		buf.append("    void foo() {\n");
//		buf.append("    \n");
//		buf.append("    }\n");
//		buf.append("    /**\n");
//		buf.append("     * NOTHING\n");
//		buf.append("     */\n");
//		buf.append("    void bar() {\n");
//		buf.append("    \n");
//		buf.append("    }\n");
//		buf.append("}\n");
//		assertEqualString(preview, buf.toString());
//	}
}
