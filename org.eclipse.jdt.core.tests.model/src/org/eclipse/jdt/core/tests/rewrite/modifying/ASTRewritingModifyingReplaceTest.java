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
		String str = """
			package test0001;
			public class X {
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", str, false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		Name name = astRoot.getAST().newSimpleName("aaa");
		astRoot.getPackage().setName(name);

		String preview = evaluateRewrite(cu, astRoot);

		String str1 = """
			package aaa;
			public class X {
			}
			""";
		assertEqualString(preview, str1);
	}

	public void test0002() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0002", false, null);
		String str = """
			package test0002;
			public class X {
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", str, false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		AST a = astRoot.getAST();

		Name name = a.newSimpleName("aaa");
		PackageDeclaration pack = a.newPackageDeclaration();
		pack.setName(name);
		astRoot.setPackage(pack);

		String preview = evaluateRewrite(cu, astRoot);

		String str1 = """
			package aaa;
			public class X {
			}
			""";
		assertEqualString(preview, str1);
	}

	public void test0003() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0003", false, null);
		String str = """
			package test0003;
			import java.util.*;
			import java.lang.*;
			import java.awt.*;
			public class X {
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", str, false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		Name name = astRoot.getAST().newSimpleName("aaa");
		List imports = astRoot.imports();
		ImportDeclaration imp = (ImportDeclaration)imports.get(0);
		imp.setName(name);

		String preview = evaluateRewrite(cu, astRoot);

		String str1 = """
			package test0003;
			import aaa.*;
			import java.lang.*;
			import java.awt.*;
			public class X {
			}
			""";
		assertEqualString(preview, str1);
	}

	public void test0004() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0004", false, null);
		String str = """
			package test0004;
			import java.util.*;
			import java.lang.*;
			import java.awt.*;
			public class X {
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", str, false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		List types = astRoot.types();
		SimpleName name = astRoot.getAST().newSimpleName("AAA");
		TypeDeclaration t = (TypeDeclaration)types.get(0);
		t.setName(name);

		String preview = evaluateRewrite(cu, astRoot);

		String str1 = """
			package test0004;
			import java.util.*;
			import java.lang.*;
			import java.awt.*;
			public class AAA {
			}
			""";
		assertEqualString(preview, str1);
	}

	public void test0005() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0005", false, null);
		String str = """
			package test0005;
			
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
		TypeDeclaration typeDeclaration = a.newTypeDeclaration();
		SimpleName name = a.newSimpleName("AAA");
		typeDeclaration.setName(name);
		types.set(1, typeDeclaration);

		String preview = evaluateRewrite(cu, astRoot);

		String str1 = """
			package test0005;
			
			public class X {
			
			}
			class AAA {
			}
			class Z {
			
			}
			""";
		assertEqualString(preview, str1);
	}

	public void test0006() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0006", false, null);
		String str = """
			package test0006;
			
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
		types.set(1, typeDeclaration1);

		String preview = evaluateRewrite(cu, astRoot);

		String str1 = """
			package test0006;
			
			public class X {
			
			}
			class A {
			}
			class Z {
			
			}
			""";
		assertEqualString(preview, str1);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=192233
	public void test0007() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0007", false, null);
		String str = """
			package test0007;
			public class X {
			    List/**/getUsers() {}
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", str, false, null);

		CompilationUnit astRoot= createCU(cu, false, getJLS3());

		astRoot.recordModifications();

		AST ast = astRoot.getAST();

		List types = astRoot.types();
		List list= ((TypeDeclaration) types.get(0)).bodyDeclarations();
		MethodDeclaration methodDecl= (MethodDeclaration) list.get(0);

		methodDecl.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));

		String preview = evaluateRewrite(cu, astRoot);

		String str1 = """
			package test0007;
			public class X {
			    void getUsers() {}
			}
			""";
		assertEqualString(preview, str1);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=192233
	public void test0008() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0008", false, null);
		String str = """
			package test0008;
			public class X {
			    List /**/getUsers() {}
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", str, false, null);

		CompilationUnit astRoot= createCU(cu, false, getJLS3());

		astRoot.recordModifications();

		AST ast = astRoot.getAST();

		List types = astRoot.types();
		List list= ((TypeDeclaration) types.get(0)).bodyDeclarations();
		MethodDeclaration methodDecl= (MethodDeclaration) list.get(0);

		methodDecl.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));

		String preview = evaluateRewrite(cu, astRoot);

		String str1 = """
			package test0008;
			public class X {
			    void getUsers() {}
			}
			""";
		assertEqualString(preview, str1);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=192233
	public void test0009() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0009", false, null);
		String str = """
			package test0009;
			public class X {
			    List/**/ getUsers() {}
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", str, false, null);

		CompilationUnit astRoot= createCU(cu, false, getJLS3());

		astRoot.recordModifications();

		AST ast = astRoot.getAST();

		List types = astRoot.types();
		List list= ((TypeDeclaration) types.get(0)).bodyDeclarations();
		MethodDeclaration methodDecl= (MethodDeclaration) list.get(0);

		methodDecl.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));

		String preview = evaluateRewrite(cu, astRoot);

		String str1 = """
			package test0009;
			public class X {
			    void getUsers() {}
			}
			""";
		assertEqualString(preview, str1);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=192233
	public void test0010() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0010", false, null);
		String str = """
			package test0010;
			public class X {
			    void getUsers(List/**/list) {}
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", str, false, null);

		CompilationUnit astRoot= createCU(cu, false, getJLS3());

		astRoot.recordModifications();

		AST ast = astRoot.getAST();

		List types = astRoot.types();
		List list= ((TypeDeclaration) types.get(0)).bodyDeclarations();
		MethodDeclaration methodDecl= (MethodDeclaration) list.get(0);
		List parameters = methodDecl.parameters();
		SingleVariableDeclaration variableDeclaration = (SingleVariableDeclaration) parameters.get(0);
		variableDeclaration.setType(ast.newPrimitiveType(PrimitiveType.INT));

		String preview = evaluateRewrite(cu, astRoot);

		String str1 = """
			package test0010;
			public class X {
			    void getUsers(int list) {}
			}
			""";
		assertEqualString(preview, str1);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=192233
	public void test0011() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0011", false, null);
		String str = """
			package test0011;
			public class X {
			    void getUsers(int i, List/**/list) {}
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", str, false, null);

		CompilationUnit astRoot= createCU(cu, false, getJLS3());

		astRoot.recordModifications();

		AST ast = astRoot.getAST();

		List types = astRoot.types();
		List list= ((TypeDeclaration) types.get(0)).bodyDeclarations();
		MethodDeclaration methodDecl= (MethodDeclaration) list.get(0);
		List parameters = methodDecl.parameters();
		SingleVariableDeclaration variableDeclaration = (SingleVariableDeclaration) parameters.get(1);
		variableDeclaration.setType(ast.newPrimitiveType(PrimitiveType.INT));

		String preview = evaluateRewrite(cu, astRoot);

		String str1 = """
			package test0011;
			public class X {
			    void getUsers(int i, int list) {}
			}
			""";
		assertEqualString(preview, str1);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=192233
	public void test0012() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0012", false, null);
		String str = """
			package test0012;
			public class X {
			    void getUsers(int i, List.../**/list) {}
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", str, false, null);

		CompilationUnit astRoot= createCU(cu, false, getJLS3());

		astRoot.recordModifications();

		AST ast = astRoot.getAST();

		List types = astRoot.types();
		List list= ((TypeDeclaration) types.get(0)).bodyDeclarations();
		MethodDeclaration methodDecl= (MethodDeclaration) list.get(0);
		List parameters = methodDecl.parameters();
		SingleVariableDeclaration variableDeclaration = (SingleVariableDeclaration) parameters.get(1);
		variableDeclaration.setType(ast.newPrimitiveType(PrimitiveType.INT));

		String preview = evaluateRewrite(cu, astRoot);

		String str1 = """
			package test0012;
			public class X {
			    void getUsers(int i, int.../**/list) {}
			}
			""";
		assertEqualString(preview, str1);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=192233
	public void test0013() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0013", false, null);
		String str = """
			package test0013;
			public class X {
			    List/**/list;
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", str, false, null);

		CompilationUnit astRoot= createCU(cu, false, getJLS3());

		astRoot.recordModifications();

		AST ast = astRoot.getAST();

		List types = astRoot.types();
		List list= ((TypeDeclaration) types.get(0)).bodyDeclarations();
		FieldDeclaration fieldDeclaration= (FieldDeclaration) list.get(0);
		fieldDeclaration.setType(ast.newPrimitiveType(PrimitiveType.INT));

		String preview = evaluateRewrite(cu, astRoot);

		String str1 = """
			package test0013;
			public class X {
			    int list;
			}
			""";
		assertEqualString(preview, str1);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331138
	// Make sure comments between removed and replaced node are not removed.
	public void test0014a() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0014a", false, null);
		String str = """
			package test0014;
			public class X {
				public void m() {
			    	String abc;
			
					// do not delete this
			
					/* do not delete this
					 */
			
			     	abc = "";
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
		VariableDeclarationStatement varDeclaration = (VariableDeclarationStatement) statements.get(0);
		statements.remove(0);

		statements.set(0, varDeclaration);
		String preview = evaluateRewrite(cu, astRoot);

		String str1 = """
			package test0014;
			public class X {
				public void m() {
			    \t
			
					// do not delete this
			
					/* do not delete this
					 */
			
			     	String abc;
				}
			}
			""";
		assertEqualString(preview, str1);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331138
	// Make sure comments between removed and replaced node are not removed.
	public void test0014b() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0014b", false, null);
		String str = """
			package test0014;
			public class X {
				public void m() {
			    	String abc;
			
					// do not delete this
			
					/* do not delete this
					 */
			
					// do not delete this
			     	abc = "";
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
		VariableDeclarationStatement varDeclaration = (VariableDeclarationStatement) statements.get(0);
		statements.remove(0);

		statements.set(0, varDeclaration);
		String preview = evaluateRewrite(cu, astRoot);

		String str1 = """
			package test0014;
			public class X {
				public void m() {
			    \t
			
					// do not delete this
			
					/* do not delete this
					 */
			
					String abc;
				}
			}
			""";
		assertEqualString(preview, str1);
	}
}
