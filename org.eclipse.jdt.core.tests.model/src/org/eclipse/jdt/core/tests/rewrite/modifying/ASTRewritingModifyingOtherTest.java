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
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.TypeDeclaration;

@SuppressWarnings({"rawtypes", "unchecked"})
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
		StringBuilder buf= new StringBuilder();
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
		StringBuilder buf= new StringBuilder();
		buf.append("package test0001;\n");
		buf.append("public class X {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuilder();
		buf.append("package test0001;\n");
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

		AST a = astRoot.getAST();

		List imports = astRoot.imports();
		imports.remove(1);
		Name name = a.newSimpleName("aaa");
		ImportDeclaration importDeclaration = a.newImportDeclaration();
		importDeclaration.setName(name);
		importDeclaration.setOnDemand(true);
		imports.add(importDeclaration);

		String preview = evaluateRewrite(cu, astRoot);

		buf= new StringBuilder();
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
		StringBuilder buf= new StringBuilder();
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

		buf= new StringBuilder();
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
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=308754
	public void test0005() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0005", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test0005;\n");
		buf.append("@A(X.class) public class C {}");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, true, getJLS3());
		astRoot.recordModifications();
		{
			// change to interface
			astRoot.accept(new ASTVisitor() {
				public boolean visit(TypeDeclaration node) {
					node.setInterface(true);
					return false;
				}
			});
		}
		String preview= evaluateRewrite(cu, astRoot);

		buf= new StringBuilder();
		buf.append("package test0005;\n");
		buf.append("@A(X.class) public interface C {}");
		assertEqualString(preview, buf.toString());
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=308754
	public void test0006() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0006", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test0006;\n");
		buf.append("public @A(X.class) class C {}");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		CompilationUnit astRoot= createCU(cu, true, getJLS3());
		astRoot.recordModifications();
		{
			// change to interface
			astRoot.accept(new ASTVisitor() {
				public boolean visit(TypeDeclaration node) {
					node.setInterface(true);
					return false;
				}
			});
		}
		String preview= evaluateRewrite(cu, astRoot);

		buf= new StringBuilder();
		buf.append("package test0006;\n");
		buf.append("public @A(X.class) interface C {}");
		assertEqualString(preview, buf.toString());
	}
}
