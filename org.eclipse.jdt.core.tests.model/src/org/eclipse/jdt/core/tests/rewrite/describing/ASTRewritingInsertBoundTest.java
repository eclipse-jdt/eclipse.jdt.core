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
package org.eclipse.jdt.core.tests.rewrite.describing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.core.dom.rewrite.TargetSourceRangeComputer;
import org.eclipse.jdt.core.dom.rewrite.TargetSourceRangeComputer.SourceRange;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ASTRewritingInsertBoundTest extends ASTRewritingTest {

	public ASTRewritingInsertBoundTest(String name) {
		super(name);
	}

	public ASTRewritingInsertBoundTest(String name, int apiLevel) {
		super(name, apiLevel);
	}

	public static Test suite() {
		return createSuite(ASTRewritingInsertBoundTest.class);
	}

	/** @deprecated using deprecated code */
	private MethodDeclaration newMethodDeclaration(AST ast, String name) {
		MethodDeclaration decl= ast.newMethodDeclaration();
		decl.setName(ast.newSimpleName(name));
		decl.setBody(null);
		decl.setReturnType(ast.newPrimitiveType(PrimitiveType.VOID));
		return decl;
	}

	private FieldDeclaration newFieldDeclaration(AST ast, String name) {
		VariableDeclarationFragment frag= ast.newVariableDeclarationFragment();
		frag.setName(ast.newSimpleName(name));
		FieldDeclaration decl= ast.newFieldDeclaration(frag);
		decl.setType(ast.newPrimitiveType(PrimitiveType.INT));
		return decl;
	}


	/** @deprecated using deprecated code */
	public void testInsert1_only_2() throws Exception {
		// insert first and last

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			public class C {
			//c1
			
			    public void foo1();
			
			//c2
			
			    public void foo2();
			
			//c3
			
			    public void foo3();
			
			//c4
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();

		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");

		MethodDeclaration decl1= newMethodDeclaration(ast, "new1");
		MethodDeclaration decl2= newMethodDeclaration(ast, "new2");

		ListRewrite listRewrite= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertFirst(decl1, null);
		listRewrite.insertLast(decl2, null);

		String preview= evaluateRewrite(cu, rewrite);

		String str1 = """
			package test1;
			public class C {
			//c1
			
			    void new1();
			
			    public void foo1();
			
			//c2
			
			    public void foo2();
			
			//c3
			
			    public void foo3();
			
			    void new2();
			
			//c4
			}
			""";
		assertEqualString(preview, str1);

	}

	/** @deprecated using deprecated code */
	public void testInsert3_only_2() throws Exception {
		// insert 2 x before

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			public class C {
			//c1
			
			    public void foo1();
			
			//c2
			
			    public void foo2();
			
			//c3
			
			    public void foo3();
			
			//c4
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();

		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");
		List decls= type.bodyDeclarations();

		MethodDeclaration decl1= newMethodDeclaration(ast, "new1");
		MethodDeclaration decl2= newMethodDeclaration(ast, "new2");

		ASTNode middleDecl= (ASTNode) decls.get(1);
		ASTNode lastDecl= (ASTNode) decls.get(2);

		ListRewrite listRewrite= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertBefore(decl1, middleDecl, null);
		listRewrite.insertBefore(decl2, lastDecl, null);

		String preview= evaluateRewrite(cu, rewrite);

		String str1 = """
			package test1;
			public class C {
			//c1
			
			    public void foo1();
			
			//c2
			
			    void new1();
			
			    public void foo2();
			
			//c3
			
			    void new2();
			
			    public void foo3();
			
			//c4
			}
			""";
		assertEqualString(preview, str1);

	}


	public void testInsert2_only_2() throws Exception {
		// insert 2 x first and 2 x last

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			public class C {
			//c1
			
			    public void foo1();
			
			//c2
			
			    public void foo2();
			
			//c3
			
			    public void foo3();
			
			//c4
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();

		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");

		MethodDeclaration decl1= newMethodDeclaration(ast, "new1");
		MethodDeclaration decl2= newMethodDeclaration(ast, "new2");
		MethodDeclaration decl3= newMethodDeclaration(ast, "new3");
		MethodDeclaration decl4= newMethodDeclaration(ast, "new4");

		ListRewrite listRewrite= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertFirst(decl1, null);
		listRewrite.insertAfter(decl2, decl1, null);
		listRewrite.insertLast(decl3, null);
		listRewrite.insertAfter(decl4, decl3, null);

		String preview= evaluateRewrite(cu, rewrite);

		String str1 = """
			package test1;
			public class C {
			//c1
			
			    void new1();
			
			    void new2();
			
			    public void foo1();
			
			//c2
			
			    public void foo2();
			
			//c3
			
			    public void foo3();
			
			    void new3();
			
			    void new4();
			
			//c4
			}
			""";
		assertEqualString(preview, str1);

	}

	public void testInsert1Before_only_2_3_4() throws Exception {
		// insert first and last

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			public class C {
			//c1
			
			    public int x1;
			
			//c2
			
			    public int x2;
			
			//c3
			
			    public int x3;
			
			//c4
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();

		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");

		FieldDeclaration decl1= newFieldDeclaration(ast, "new1");
		FieldDeclaration decl2= newFieldDeclaration(ast, "new2");

		ListRewrite listRewrite= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertFirst(decl1, null);
		listRewrite.insertLast(decl2, null);

		String preview= evaluateRewrite(cu, rewrite);

		String str1 = """
			package test1;
			public class C {
			//c1
			
			    int new1;
			
			    public int x1;
			
			//c2
			
			    public int x2;
			
			//c3
			
			    public int x3;
			
			    int new2;
			
			//c4
			}
			""";
		assertEqualString(preview, str1);

	}

	public void testInsert2Before_only_2_3_4() throws Exception {
		// insert 2x first and 2 x last

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			public class C {
			//c1
			
			    public int x1;
			
			//c2
			
			    public int x2;
			
			//c3
			
			    public int x3;
			
			//c4
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();

		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");

		FieldDeclaration decl1= newFieldDeclaration(ast, "new1");
		FieldDeclaration decl2= newFieldDeclaration(ast, "new2");
		FieldDeclaration decl3= newFieldDeclaration(ast, "new3");
		FieldDeclaration decl4= newFieldDeclaration(ast, "new4");

		ListRewrite listRewrite= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertFirst(decl1, null);
		listRewrite.insertAfter(decl2, decl1, null);
		listRewrite.insertLast(decl3, null);
		listRewrite.insertAfter(decl4, decl3, null);


		String preview= evaluateRewrite(cu, rewrite);

		String str1 = """
			package test1;
			public class C {
			//c1
			
			    int new1;
			
			    int new2;
			
			    public int x1;
			
			//c2
			
			    public int x2;
			
			//c3
			
			    public int x3;
			
			    int new3;
			
			    int new4;
			
			//c4
			}
			""";
		assertEqualString(preview, str1);

	}

	public void testInsert3Before_only_2_3_4() throws Exception {
		// insert 2 x after

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			public class C {
			//c1
			
			    public int x1;
			
			//c2
			
			    public int x2;
			
			//c3
			
			    public int x3;
			
			//c4
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();

		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");
		List decls= type.bodyDeclarations();

		FieldDeclaration decl1= newFieldDeclaration(ast, "new1");
		FieldDeclaration decl2= newFieldDeclaration(ast, "new2");

		ASTNode firstDecl= (ASTNode) decls.get(0);
		ASTNode middleDecl= (ASTNode) decls.get(1);

		ListRewrite listRewrite= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertAfter(decl1, firstDecl, null);
		listRewrite.insertAfter(decl2, middleDecl, null);

		String preview= evaluateRewrite(cu, rewrite);

		String str1 = """
			package test1;
			public class C {
			//c1
			
			    public int x1;
			
			    int new1;
			
			//c2
			
			    public int x2;
			
			    int new2;
			
			//c3
			
			    public int x3;
			
			//c4
			}
			""";
		assertEqualString(preview, str1);

	}

	public void testRemove1() throws Exception {
		// remove first and last

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			public class C {
			//c1
			
			    public void foo1();
			
			//c2
			
			    public void foo2();
			
			//c3
			
			    public void foo3();
			
			//c4
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

		CompilationUnit astRoot= createAST(cu);

		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");
		List decls= type.bodyDeclarations();

		rewrite.remove((ASTNode) decls.get(0), null);
		rewrite.remove((ASTNode) decls.get(2), null);

		String preview= evaluateRewrite(cu, rewrite);

		String str1 = """
			package test1;
			public class C {
			//c1
			
			   \s
			
			//c2
			
			    public void foo2();
			
			//c3
			
			
			//c4
			}
			""";
		assertEqualString(preview, str1);

	}

	public void testRemove2() throws Exception {
		// remove second

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			public class C {
			//c1
			
			    public void foo1();
			
			//c2
			
			    public void foo2();
			
			//c3
			
			    public void foo3();
			
			//c4
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

		CompilationUnit astRoot= createAST(cu);

		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");
		List decls= type.bodyDeclarations();

		rewrite.remove((ASTNode) decls.get(1), null);

		String preview= evaluateRewrite(cu, rewrite);

		String str1 = """
			package test1;
			public class C {
			//c1
			
			    public void foo1();
			
			//c2
			
			   \s
			
			//c3
			
			    public void foo3();
			
			//c4
			}
			""";
		assertEqualString(preview, str1);

	}

	public void testRemove3() throws Exception {
		// remove 2nd and 3rd

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			public class C {
			//c1
			
			    public void foo1();
			
			//c2
			
			    public void foo2();
			
			//c3
			
			    public void foo3();
			
			//c4
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

		CompilationUnit astRoot= createAST(cu);

		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");
		List decls= type.bodyDeclarations();

		rewrite.remove((ASTNode) decls.get(1), null);
		rewrite.remove((ASTNode) decls.get(2), null);

		String preview= evaluateRewrite(cu, rewrite);

		String str1 = """
			package test1;
			public class C {
			//c1
			
			    public void foo1();
			
			//c2
			
			
			//c3
			
			
			//c4
			}
			""";
		assertEqualString(preview, str1);

	}

	public void testRemove4() throws Exception {
		// remove all

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			public class C {
			//c1
			
			    public void foo1();
			
			//c2
			
			    public void foo2();
			
			//c3
			
			    public void foo3();
			
			//c4
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

		CompilationUnit astRoot= createAST(cu);

		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");
		List decls= type.bodyDeclarations();

		rewrite.remove((ASTNode) decls.get(0), null);
		rewrite.remove((ASTNode) decls.get(1), null);
		rewrite.remove((ASTNode) decls.get(2), null);

		String preview= evaluateRewrite(cu, rewrite);

		String str1 = """
			package test1;
			public class C {
			//c1
			
			   \s
			
			//c2
			
			   \s
			
			//c3
			
			   \s
			
			//c4
			}
			""";
		assertEqualString(preview, str1);

	}


	public void testRemoveInsert1_only_2() throws Exception {
		// remove first add before first, remove last add after last

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			public class C {
			//c1
			
			    public void foo1();
			
			//c2
			
			    public void foo2();
			
			//c3
			
			    public void foo3();
			
			//c4
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();

		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");
		List decls= type.bodyDeclarations();

		rewrite.remove((ASTNode) decls.get(0), null);
		rewrite.remove((ASTNode) decls.get(2), null);

		MethodDeclaration decl1= newMethodDeclaration(ast, "new1");
		MethodDeclaration decl2= newMethodDeclaration(ast, "new2");

		ListRewrite listRewrite= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertFirst(decl1, null);
		listRewrite.insertLast(decl2, null);

		String preview= evaluateRewrite(cu, rewrite);

		String str1 = """
			package test1;
			public class C {
			//c1
			
			    void new1();
			
			   \s
			
			//c2
			
			    public void foo2();
			
			//c3
			
			    void new2();
			
			//c4
			}
			""";
		assertEqualString(preview, str1);

	}

	public void testRemoveInsert2_only_2() throws Exception {
		// remove first add 2x first, remove last add 2x  last

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			public class C {
			//c1
			
			    public void foo1();
			
			//c2
			
			    public void foo2();
			
			//c3
			
			    public void foo3();
			
			//c4
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();

		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");
		List decls= type.bodyDeclarations();

		rewrite.remove((ASTNode) decls.get(0), null);
		rewrite.remove((ASTNode) decls.get(2), null);

		MethodDeclaration decl1= newMethodDeclaration(ast, "new1");
		MethodDeclaration decl2= newMethodDeclaration(ast, "new2");
		MethodDeclaration decl3= newMethodDeclaration(ast, "new3");
		MethodDeclaration decl4= newMethodDeclaration(ast, "new4");

		ASTNode firstDecl= (ASTNode) decls.get(0);

		ListRewrite listRewrite= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertBefore(decl1, firstDecl, null);
		listRewrite.insertAfter(decl2, firstDecl, null);
		listRewrite.insertLast(decl3, null);
		listRewrite.insertAfter(decl4, decl3, null);


		String preview= evaluateRewrite(cu, rewrite);

		String str1 = """
			package test1;
			public class C {
			//c1
			
			    void new1();
			
			    void new2();
			
			   \s
			
			//c2
			
			    public void foo2();
			
			//c3
			
			    void new3();
			
			    void new4();
			
			//c4
			}
			""";
		assertEqualString(preview, str1);

	}

	public void testRemoveInsert3_only_2() throws Exception {
		// remove middle, add before, add after

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			public class C {
			//c1
			
			    public void foo1();
			
			//c2
			
			    public void foo2();
			
			//c3
			
			    public void foo3();
			
			//c4
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();

		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");
		List decls= type.bodyDeclarations();

		rewrite.remove((ASTNode) decls.get(1), null);

		MethodDeclaration decl1= newMethodDeclaration(ast, "new1");
		MethodDeclaration decl2= newMethodDeclaration(ast, "new2");

		ASTNode middleDecl= (ASTNode) decls.get(1);

		ListRewrite listRewrite= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertBefore(decl1, middleDecl, null);
		listRewrite.insertAfter(decl2, middleDecl, null);

		String preview= evaluateRewrite(cu, rewrite);

		String str1 = """
			package test1;
			public class C {
			//c1
			
			    public void foo1();
			
			//c2
			
			    void new1();
			
			    void new2();
			
			   \s
			
			//c3
			
			    public void foo3();
			
			//c4
			}
			""";
		assertEqualString(preview, str1);

	}


	public void testRemoveInsert1Before_only_2_3_4() throws Exception {
		// remove first add before first, remove last add after last

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			public class C {
			//c1
			
			    public int x1;
			
			//c2
			
			    public int x2;
			
			//c3
			
			    public int x3;
			
			//c4
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();

		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");
		List decls= type.bodyDeclarations();

		rewrite.remove((ASTNode) decls.get(0), null);
		rewrite.remove((ASTNode) decls.get(2), null);

		FieldDeclaration decl1= newFieldDeclaration(ast, "new1");
		FieldDeclaration decl2= newFieldDeclaration(ast, "new2");

		ListRewrite listRewrite= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertFirst(decl1, null);
		listRewrite.insertLast(decl2, null);


		String preview= evaluateRewrite(cu, rewrite);

		String str1 = """
			package test1;
			public class C {
			//c1
			
			    int new1;
			
			   \s
			
			//c2
			
			    public int x2;
			
			//c3
			
			    int new2;
			
			//c4
			}
			""";
		assertEqualString(preview, str1);

	}

	public void testRemoveInsert2Before_only_2_3_4() throws Exception {
		// remove first add 2x first, remove last add 2x  last

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			public class C {
			//c1
			
			    public int x1;
			
			//c2
			
			    public int x2;
			
			//c3
			
			    public int x3;
			
			//c4
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();

		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");
		List decls= type.bodyDeclarations();

		rewrite.remove((ASTNode) decls.get(0), null);
		rewrite.remove((ASTNode) decls.get(2), null);

		FieldDeclaration decl1= newFieldDeclaration(ast, "new1");
		FieldDeclaration decl2= newFieldDeclaration(ast, "new2");
		FieldDeclaration decl3= newFieldDeclaration(ast, "new3");
		FieldDeclaration decl4= newFieldDeclaration(ast, "new4");

		ASTNode firstDecl= (ASTNode) decls.get(0);

		ListRewrite listRewrite= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertBefore(decl1, firstDecl, null);
		listRewrite.insertAfter(decl2, firstDecl, null);
		listRewrite.insertLast(decl3, null);
		listRewrite.insertAfter(decl4, decl3, null);

		String preview= evaluateRewrite(cu, rewrite);

		String str1 = """
			package test1;
			public class C {
			//c1
			
			    int new1;
			
			    int new2;
			
			   \s
			
			//c2
			
			    public int x2;
			
			//c3
			
			    int new3;
			
			    int new4;
			
			//c4
			}
			""";
		assertEqualString(preview, str1);

	}

	public void testRemoveInsert3Before_only_2_3_4() throws Exception {
		// remove middle, add before, add after

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			public class C {
			//c1
			
			    public int x1;
			
			//c2
			
			    public int x2;
			
			//c3
			
			    public int x3;
			
			//c4
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();

		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");
		List decls= type.bodyDeclarations();

		FieldDeclaration decl1= newFieldDeclaration(ast, "new1");
		FieldDeclaration decl2= newFieldDeclaration(ast, "new2");

		ASTNode middleDecl= (ASTNode) decls.get(1);

		ListRewrite listRewrite= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.remove(middleDecl, null);

		listRewrite.insertBefore(decl1, middleDecl, null);
		listRewrite.insertAfter(decl2, middleDecl, null);

		String preview= evaluateRewrite(cu, rewrite);

		String str1 = """
			package test1;
			public class C {
			//c1
			
			    public int x1;
			
			//c2
			
			    int new1;
			
			    int new2;
			
			   \s
			
			//c3
			
			    public int x3;
			
			//c4
			}
			""";
		assertEqualString(preview, str1);

	}

	public void testRemoveInsert4_only_2() throws Exception {
		// remove first and add after first, remove last and add before last

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			public class C {
			//c1
			
			    public void foo1();
			
			//c2
			
			    public void foo2();
			
			//c3
			
			    public void foo3();
			
			//c4
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();

		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");
		List decls= type.bodyDeclarations();

		MethodDeclaration decl1= newMethodDeclaration(ast, "new1");
		MethodDeclaration decl2= newMethodDeclaration(ast, "new2");

		ASTNode firstDecl= (ASTNode) decls.get(0);
		ASTNode lastDecl= (ASTNode) decls.get(2);

		ListRewrite listRewrite= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);

		listRewrite.remove(firstDecl, null);
		listRewrite.remove(lastDecl, null);

		listRewrite.insertAfter(decl1, firstDecl, null);
		listRewrite.insertBefore(decl2, lastDecl, null);

		String preview= evaluateRewrite(cu, rewrite);

		String str1 = """
			package test1;
			public class C {
			//c1
			
			    void new1();
			
			   \s
			
			//c2
			
			    public void foo2();
			
			//c3
			
			    void new2();
			
			//c4
			}
			""";
		assertEqualString(preview, str1);

	}

	public void testRemoveInsert4Before_only_2_3_4() throws Exception {
		// remove first and add after first, remove last and add before last

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			public class C {
			//c1
			
			    public int x1;
			
			//c2
			
			    public int x2;
			
			//c3
			
			    public int x3;
			
			//c4
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();

		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");
		List decls= type.bodyDeclarations();

		FieldDeclaration decl1= newFieldDeclaration(ast, "new1");
		FieldDeclaration decl2= newFieldDeclaration(ast, "new2");

		ASTNode firstDecl= (ASTNode) decls.get(0);
		ASTNode lastDecl= (ASTNode) decls.get(2);

		ListRewrite listRewrite= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.remove(firstDecl, null);
		listRewrite.remove(lastDecl, null);

		listRewrite.insertAfter(decl1, firstDecl, null);
		listRewrite.insertBefore(decl2, lastDecl, null);

		String preview= evaluateRewrite(cu, rewrite);

		String str1 = """
			package test1;
			public class C {
			//c1
			
			    int new1;
			
			   \s
			
			//c2
			
			    public int x2;
			
			//c3
			
			    int new2;
			
			//c4
			}
			""";
		assertEqualString(preview, str1);

	}

	public void testRemoveInsert5_only_2() throws Exception {
		// remove first and add after and before first, remove last and add after and before last

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			public class C {
			//c1
			
			    public void foo1();
			
			//c2
			
			    public void foo2();
			
			//c3
			
			    public void foo3();
			
			//c4
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();

		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");
		List decls= type.bodyDeclarations();

		MethodDeclaration decl1= newMethodDeclaration(ast, "new1");
		MethodDeclaration decl2= newMethodDeclaration(ast, "new2");
		MethodDeclaration decl3= newMethodDeclaration(ast, "new3");
		MethodDeclaration decl4= newMethodDeclaration(ast, "new4");

		ASTNode firstDecl= (ASTNode) decls.get(0);
		ASTNode lastDecl= (ASTNode) decls.get(2);

		ListRewrite listRewrite= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);

		rewrite.remove(firstDecl, null);
		rewrite.remove(lastDecl, null);

		listRewrite.insertBefore(decl1, firstDecl, null);
		listRewrite.insertAfter(decl2, firstDecl, null);
		listRewrite.insertBefore(decl3, lastDecl, null);
		listRewrite.insertAfter(decl4, lastDecl, null);


		String preview= evaluateRewrite(cu, rewrite);

		String str1 = """
			package test1;
			public class C {
			//c1
			
			    void new1();
			
			    void new2();
			
			   \s
			
			//c2
			
			    public void foo2();
			
			//c3
			
			    void new3();
			
			    void new4();
			
			//c4
			}
			""";
		assertEqualString(preview, str1);

	}

	public void testRemoveInsert5Before_only_2_3_4() throws Exception {
		// remove first and add after first, remove last and add before last

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			public class C {
			//c1
			
			    public int x1;
			
			//c2
			
			    public int x2;
			
			//c3
			
			    public int x3;
			
			//c4
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();

		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");
		List decls= type.bodyDeclarations();

		FieldDeclaration decl1= newFieldDeclaration(ast, "new1");
		FieldDeclaration decl2= newFieldDeclaration(ast, "new2");
		FieldDeclaration decl3= newFieldDeclaration(ast, "new3");
		FieldDeclaration decl4= newFieldDeclaration(ast, "new4");

		ASTNode firstDecl= (ASTNode) decls.get(0);
		ASTNode lastDecl= (ASTNode) decls.get(2);

		ListRewrite listRewrite= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);

		rewrite.remove(firstDecl, null);
		rewrite.remove(lastDecl, null);

		listRewrite.insertBefore(decl1, firstDecl, null);
		listRewrite.insertAfter(decl2, firstDecl, null);
		listRewrite.insertBefore(decl3, lastDecl, null);
		listRewrite.insertAfter(decl4, lastDecl, null);

		String preview= evaluateRewrite(cu, rewrite);

		String str1 = """
			package test1;
			public class C {
			//c1
			
			    int new1;
			
			    int new2;
			
			   \s
			
			//c2
			
			    public int x2;
			
			//c3
			
			    int new3;
			
			    int new4;
			
			//c4
			}
			""";
		assertEqualString(preview, str1);

	}


	public void testRemoveInsert6_only_2() throws Exception {
		// remove all, add before first and after last

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			public class C {
			//c1
			
			    public void foo1();
			
			//c2
			
			    public void foo2();
			
			//c3
			
			    public void foo3();
			
			//c4
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();

		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");
		List decls= type.bodyDeclarations();

		rewrite.remove((ASTNode) decls.get(0), null);
		rewrite.remove((ASTNode) decls.get(1), null);
		rewrite.remove((ASTNode) decls.get(2), null);

		MethodDeclaration decl1= newMethodDeclaration(ast, "new1");
		MethodDeclaration decl2= newMethodDeclaration(ast, "new2");

		ListRewrite listRewrite= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertFirst(decl1, null);
		listRewrite.insertLast(decl2, null);

		String preview= evaluateRewrite(cu, rewrite);

		String str1 = """
			package test1;
			public class C {
			//c1
			
			    void new1();
			
			   \s
			
			//c2
			
			   \s
			
			//c3
			
			    void new2();
			
			//c4
			}
			""";
		assertEqualString(preview, str1);

	}

	public void testRemoveInsert6Before_only_2_3_4() throws Exception {
		// remove all, add before first and after last

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			public class C {
			//c1
			
			    public int x1;
			
			//c2
			
			    public int x2;
			
			//c3
			
			    public int x3;
			
			//c4
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();

		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");
		List decls= type.bodyDeclarations();

		rewrite.remove((ASTNode) decls.get(0), null);
		rewrite.remove((ASTNode) decls.get(1), null);
		rewrite.remove((ASTNode) decls.get(2), null);

		FieldDeclaration decl1= newFieldDeclaration(ast, "new1");
		FieldDeclaration decl2= newFieldDeclaration(ast, "new2");

		ListRewrite listRewrite= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertFirst(decl1, null);
		listRewrite.insertLast(decl2, null);

		String preview= evaluateRewrite(cu, rewrite);

		String str1 = """
			package test1;
			public class C {
			//c1
			
			    int new1;
			
			   \s
			
			//c2
			
			   \s
			
			//c3
			
			    int new2;
			
			//c4
			}
			""";
		assertEqualString(preview, str1);

	}


	public void testRemoveInsert7_only_2() throws Exception {
		// remove all, add after first and before last

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			public class C {
			//c1
			
			    public void foo1();
			
			//c2
			
			    public void foo2();
			
			//c3
			
			    public void foo3();
			
			//c4
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();

		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");
		List decls= type.bodyDeclarations();

		rewrite.remove((ASTNode) decls.get(0), null);
		rewrite.remove((ASTNode) decls.get(1), null);
		rewrite.remove((ASTNode) decls.get(2), null);

		MethodDeclaration decl1= newMethodDeclaration(ast, "new1");
		MethodDeclaration decl2= newMethodDeclaration(ast, "new2");

		ASTNode middleDecl= (ASTNode) decls.get(1);

		ListRewrite listRewrite= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertBefore(decl1, middleDecl, null);
		listRewrite.insertAfter(decl2, middleDecl, null);

		String preview= evaluateRewrite(cu, rewrite);

		String str1 = """
			package test1;
			public class C {
			//c1
			
			    void new1();
			
			   \s
			
			//c2
			
			    void new2();
			
			//c3
			
			   \s
			
			//c4
			}
			""";
		assertEqualString(preview, str1);

	}

	public void testRemoveInsert7Before_only_2_3_4() throws Exception {
		// remove all, add after first and before last

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			public class C {
			//c1
			
			    public int x1;
			
			//c2
			
			    public int x2;
			
			//c3
			
			    public int x3;
			
			//c4
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", str, false, null);

		CompilationUnit astRoot= createAST(cu);
		AST ast= astRoot.getAST();

		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		TypeDeclaration type= findTypeDeclaration(astRoot, "C");
		List decls= type.bodyDeclarations();

		rewrite.remove((ASTNode) decls.get(0), null);
		rewrite.remove((ASTNode) decls.get(1), null);
		rewrite.remove((ASTNode) decls.get(2), null);

		FieldDeclaration decl1= newFieldDeclaration(ast, "new1");
		FieldDeclaration decl2= newFieldDeclaration(ast, "new2");

		ASTNode middleDecl= (ASTNode) decls.get(1);

		ListRewrite listRewrite= rewrite.getListRewrite(type, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertBefore(decl1, middleDecl, null);
		listRewrite.insertAfter(decl2, middleDecl, null);

		String preview= evaluateRewrite(cu, rewrite);

		String str1 = """
			package test1;
			public class C {
			//c1
			
			    int new1;
			
			   \s
			
			//c2
			
			    int new2;
			
			//c3
			
			   \s
			
			//c4
			}
			""";
		assertEqualString(preview, str1);

	}


	public void testTargetSourceRangeComputer () throws Exception {
		// remove all, add after first and before last

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class C {\n");
		buf.append("    //c1\n");
		buf.append("\n");
		buf.append("    public int x1;\n");
		buf.append("\n");
		buf.append("    //c2\n");
		buf.append("\n");
		buf.append("    public int x2;\n");
		buf.append("\n");
		buf.append("    //c3\n");
		buf.append("\n");
		buf.append("    public int x3;\n");
		buf.append("\n");
		buf.append("    //c4\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);

		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		TypeDeclaration type= findTypeDeclaration(astRoot, "C");
		FieldDeclaration[] decls= type.getFields();

		final Map extendedRanges= new HashMap();

		FieldDeclaration f1= decls[0];
		int off1= buf.indexOf("//c1");
		int end1= f1.getStartPosition() + f1.getLength();
		extendedRanges.put(f1, new SourceRange(off1, end1 - off1));

		rewrite.setTargetSourceRangeComputer(new TargetSourceRangeComputer() {
			public SourceRange computeSourceRange(ASTNode node) {
				SourceRange range= (SourceRange) extendedRanges.get(node);
				if (range != null)
					return range;
				return super.computeSourceRange(node);
			}
		});

		rewrite.remove(f1, null);

		String preview= evaluateRewrite(cu, rewrite);
		// Note that c1 is absent because source range has been calculated from c1
		String str = """
			package test1;
			public class C {
			   \s
			
			    //c2
			
			    public int x2;
			
			    //c3
			
			    public int x3;
			
			    //c4
			}
			""";
		assertEqualString(preview, str);

	}

}



