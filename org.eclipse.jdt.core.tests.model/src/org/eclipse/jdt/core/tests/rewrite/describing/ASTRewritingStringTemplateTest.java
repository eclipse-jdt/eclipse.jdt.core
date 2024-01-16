/*******************************************************************************
 * Copyright (c) 2023 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.rewrite.describing;

import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringFragment;
import org.eclipse.jdt.core.dom.StringTemplateComponent;
import org.eclipse.jdt.core.dom.StringTemplateExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import junit.framework.Test;

public class ASTRewritingStringTemplateTest extends ASTRewritingTest {

	static {
//		TESTS_NAMES = new String[] {"test005"};
	}

	public ASTRewritingStringTemplateTest(String name, int apiLevel) {
		super(name, apiLevel);
	}

	public static Test suite() {
		return createSuite(ASTRewritingStringTemplateTest.class, 22);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (this.apiLevel == AST.JLS22 ) {
			this.project1.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_22);
			this.project1.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_22);
			this.project1.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_22);
			this.project1.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
		}
	}
	// Insert a newly created string template expression
	@SuppressWarnings({ "rawtypes", "deprecation" })
	public void test001() throws Exception {
		if (this.apiLevel != 22) {
			System.err.println("Test "+getName()+" requires a JRE 22");
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class X {\n");
		buf.append("  void foo(Object o) {\n");
		buf.append("    String name = \"Jay\";\n");
		buf.append("  }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List blockStatements = block.statements();
		assertEquals("Incorrect number of statements", 1, blockStatements.size());
		{
			VariableDeclarationFragment varFragment = ast.newVariableDeclarationFragment();
			varFragment.setName(ast.newSimpleName("s")); //$NON-NLS-1$
			StringTemplateExpression templateExp = ast.newStringTemplateExpression();
			List<StringTemplateComponent> components = templateExp.components();
			StringFragment fragment = ast.newStringFragment();
			templateExp.setIsMultiline(false);
			fragment.setEscapedValue("Hello ");
			templateExp.setFirstFragment(fragment);

			StringTemplateComponent component = ast.newStringTemplateComponent();
			SimpleName name = ast.newSimpleName("name");
			fragment = ast.newStringFragment();
			fragment.setEscapedValue("!");
			component.setEmbeddedExpression(name);
			component.setStringFragment(fragment);
			components.add(component);

			templateExp.setProcessor(ast.newSimpleName("STR"));
			varFragment.setInitializer(templateExp);
			templateExp.toString();
			VariableDeclarationStatement varDec = ast.newVariableDeclarationStatement(varFragment);
			varDec.setType(ast.newSimpleType(ast.newSimpleName("String")));//$NON-NLS-1$
			rewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY).insertLast(varDec, null);
		}

		String preview = evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class X {\n");
		buf.append("  void foo(Object o) {\n");
		buf.append("    String name = \"Jay\";\n");
		buf.append("    String s = STR.\"Hello \\{name}!\";\n");
		buf.append("  }\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

		{
			VariableDeclarationFragment varFragment = ast.newVariableDeclarationFragment();
			varFragment.setName(ast.newSimpleName("s1")); //$NON-NLS-1$
			StringTemplateExpression templateExp = ast.newStringTemplateExpression();
			List<StringTemplateComponent> components = templateExp.components();
			StringFragment fragment = ast.newStringFragment();
			templateExp.setIsMultiline(true);
			fragment.setEscapedValue("Hello ");
			templateExp.setFirstFragment(fragment);

			StringTemplateComponent component = ast.newStringTemplateComponent();
			SimpleName name = ast.newSimpleName("name");
			fragment = ast.newStringFragment();
			fragment.setEscapedValue("!");
			component.setEmbeddedExpression(name);
			component.setStringFragment(fragment);
			components.add(component);

			templateExp.setProcessor(ast.newSimpleName("STR"));
			varFragment.setInitializer(templateExp);
			templateExp.toString();
			VariableDeclarationStatement varDec = ast.newVariableDeclarationStatement(varFragment);
			varDec.setType(ast.newSimpleType(ast.newSimpleName("String")));//$NON-NLS-1$
			rewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY).insertLast(varDec, null);
		}

		preview = evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class X {\n");
		buf.append("  void foo(Object o) {\n");
		buf.append("    String name = \"Jay\";\n");
		buf.append("    String s = STR.\"Hello \\{name}!\";\n");
		buf.append("    String s1 = STR.\"\"\"\nHello \\{name}!\"\"\";\n");
		buf.append("  }\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());
	}
	// Rewrite the first fragment
	@SuppressWarnings({ "rawtypes", "deprecation" })
	public void test002() throws Exception {
		if (this.apiLevel != 21) {
			System.err.println("Test "+getName()+" requires a JRE 21");
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class X {\n");
		buf.append("  void foo(Object o) {\n");
		buf.append("    String name = \"Jay\";\n");
		buf.append("    String s = STR.\"Hello \\{name}!\";\n");
		buf.append("  }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List blockStatements = block.statements();
		assertEquals("Incorrect number of statements", 2, blockStatements.size());
		{
			VariableDeclarationStatement varStmt = (VariableDeclarationStatement) blockStatements.get(1);
			assertEquals("Incorrect number of fragents", 1, varStmt.fragments().size());
			VariableDeclarationFragment varFragment = (VariableDeclarationFragment) varStmt.fragments().get(0);
			StringTemplateExpression templateExp = (StringTemplateExpression) varFragment.getInitializer();
			List<StringTemplateComponent> components = templateExp.components();
			assertEquals("Incorrect number of string template components", 1, components.size());
			StringFragment literal = astRoot.getAST().newStringFragment();
			literal.setEscapedValue("Hey there ");
			rewrite.set(templateExp,  StringTemplateExpression.FIRST_STRING_FRAGMENT, literal, null);
		}

		String preview = evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class X {\n");
		buf.append("  void foo(Object o) {\n");
		buf.append("    String name = \"Jay\";\n");
		buf.append("    String s = STR.\"Hey there \\{name}!\";\n");
		buf.append("  }\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());
	}
	// replace the first fragment and remove the only string fragment component
	@SuppressWarnings({ "rawtypes", "deprecation" })
	public void test003() throws Exception {
		if (this.apiLevel != 21) {
			System.err.println("Test "+getName()+" requires a JRE 21");
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class X {\n");
		buf.append("  void foo(Object o) {\n");
		buf.append("    String name = \"Jay\";\n");
		buf.append("    String s = STR.\"Hello \\{name}!\";\n");
		buf.append("  }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List blockStatements = block.statements();
		assertEquals("Incorrect number of statements", 2, blockStatements.size());
		{
			VariableDeclarationStatement varStmt = (VariableDeclarationStatement) blockStatements.get(1);
			assertEquals("Incorrect number of fragents", 1, varStmt.fragments().size());
			VariableDeclarationFragment varFragment = (VariableDeclarationFragment) varStmt.fragments().get(0);
			StringTemplateExpression templateExp = (StringTemplateExpression) varFragment.getInitializer();
			List<StringTemplateComponent> components = templateExp.components();
			assertEquals("Incorrect number of string template components", 1, components.size());
			StringFragment literal = astRoot.getAST().newStringFragment();
			literal.setEscapedValue("Hello!");
			rewrite.set(templateExp,  StringTemplateExpression.FIRST_STRING_FRAGMENT, literal, null);
			rewrite.getListRewrite(templateExp, StringTemplateExpression.STRING_TEMPLATE_COMPONENTS).remove(components.get(0), null);
		}

		String preview = evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class X {\n");
		buf.append("  void foo(Object o) {\n");
		buf.append("    String name = \"Jay\";\n");
		buf.append("    String s = STR.\"Hello!\";\n");
		buf.append("  }\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());
	}
	// Replace just the embedded expression in an existing template component
	@SuppressWarnings({ "rawtypes", "deprecation" })
	public void test004() throws Exception {
		if (this.apiLevel != 21) {
			System.err.println("Test "+getName()+" requires a JRE 21");
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class X {\n");
		buf.append("  void foo(Object o) {\n");
		buf.append("    String name = \"Jay\";\n");
		buf.append("    String s = STR.\"Hello \\{name}!\";\n");
		buf.append("  }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot = createAST(cu);
		ASTRewrite rewrite = ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List blockStatements = block.statements();
		assertEquals("Incorrect number of statements", 2, blockStatements.size());
		{
			VariableDeclarationStatement varStmt = (VariableDeclarationStatement) blockStatements.get(1);
			assertEquals("Incorrect number of fragents", 1, varStmt.fragments().size());
			VariableDeclarationFragment varFragment = (VariableDeclarationFragment) varStmt.fragments().get(0);
			StringTemplateExpression templateExp = (StringTemplateExpression) varFragment.getInitializer();
			List<StringTemplateComponent> components = templateExp.components();
			assertEquals("Incorrect number of string template components", 1, components.size());
			StringFragment fragment = astRoot.getAST().newStringFragment();
			fragment.setEscapedValue("Hello!");
			SimpleName name = ast.newSimpleName("first");
			fragment = ast.newStringFragment();
			StringTemplateComponent component = templateExp.components().get(0);
			rewrite.set(component, StringTemplateComponent.EMBEDDED_EXPRESSION_PROPERTY, name, null);
		}

		String preview = evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class X {\n");
		buf.append("  void foo(Object o) {\n");
		buf.append("    String name = \"Jay\";\n");
		buf.append("    String s = STR.\"Hello \\{first}!\";\n");
		buf.append("  }\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());
	}
	// Replace an existing template component
	@SuppressWarnings({ "rawtypes", "deprecation" })
	public void _test005() throws Exception {
		if (this.apiLevel != 21) {
			System.err.println("Test "+getName()+" requires a JRE 21");
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class X {\n");
		buf.append("  void foo(Object o) {\n");
		buf.append("    String name = \"Jay\";\n");
		buf.append("    String s = STR.\"Hello \";\n");
		buf.append("  }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot = createAST(cu);
		ASTRewrite rewrite = ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List blockStatements = block.statements();
		assertEquals("Incorrect number of statements", 2, blockStatements.size());
		{
			VariableDeclarationStatement varStmt = (VariableDeclarationStatement) blockStatements.get(1);
			assertEquals("Incorrect number of fragents", 1, varStmt.fragments().size());
			VariableDeclarationFragment varFragment = (VariableDeclarationFragment) varStmt.fragments().get(0);
			StringTemplateExpression templateExp = (StringTemplateExpression) varFragment.getInitializer();
			List<StringTemplateComponent> components = templateExp.components();
			assertEquals("Incorrect number of string template components", 0, components.size());

			StringTemplateComponent component = ast.newStringTemplateComponent();
			SimpleName name = ast.newSimpleName("name");
			StringFragment fragment = ast.newStringFragment();
			fragment.setEscapedValue("!");
			component.setEmbeddedExpression(name);
			component.setStringFragment(fragment);

			rewrite.getListRewrite(templateExp, StringTemplateExpression.STRING_TEMPLATE_COMPONENTS).insertFirst(component, null);
		}

		String preview = evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class X {\n");
		buf.append("  void foo(Object o) {\n");
		buf.append("    String name = \"Jay\";\n");
		buf.append("    String s = STR.\"Hello \\{name}!\";\n");
		buf.append("  }\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());
	}
	@SuppressWarnings({ "rawtypes", "deprecation" })
	public void _test006() throws Exception {
		if (this.apiLevel != 21) {
			System.err.println("Test "+getName()+" requires a JRE 21");
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class X {\n");
		buf.append("  void foo(Object o) {\n");
		buf.append("    String name = \"Jay\";\n");
		buf.append("    String s = STR.\"Hello \\{name}!\";\n");
		buf.append("  }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List blockStatements = block.statements();
		assertEquals("Incorrect number of statements", 2, blockStatements.size());
		{
			VariableDeclarationStatement varStmt = (VariableDeclarationStatement) blockStatements.get(1);
			assertEquals("Incorrect number of fragents", 1, varStmt.fragments().size());
			VariableDeclarationFragment varFragment = (VariableDeclarationFragment) varStmt.fragments().get(0);
			StringTemplateExpression templateExp = (StringTemplateExpression) varFragment.getInitializer();
			rewrite.set(templateExp,  StringTemplateExpression.MULTI_LINE, Boolean.TRUE, null);
		}

		String preview = evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class X {\n");
		buf.append("  void foo(Object o) {\n");
		buf.append("    String name = \"Jay\";\n");
		buf.append("    String s = STR.\"\"\"\nHello \\{name}!\"\"\";\n");
		buf.append("  }\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());
	}
	@SuppressWarnings({ "rawtypes", "deprecation" })
	public void _test007() throws Exception {
		if (this.apiLevel != 21) {
			System.err.println("Test "+getName()+" requires a JRE 21");
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class X {\n");
		buf.append("  void foo(Object o) {\n");
		buf.append("    String name = \"Jay\";\n");
		buf.append("    String s = STR.\"\"\"\nHello \\{name}!\"\"\";\n");
		buf.append("  }\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());

		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDecl= findMethodDeclaration(type, "foo");
		Block block= methodDecl.getBody();
		List blockStatements = block.statements();
		assertEquals("Incorrect number of statements", 2, blockStatements.size());
		{
			VariableDeclarationStatement varStmt = (VariableDeclarationStatement) blockStatements.get(1);
			assertEquals("Incorrect number of fragents", 1, varStmt.fragments().size());
			VariableDeclarationFragment varFragment = (VariableDeclarationFragment) varStmt.fragments().get(0);
			StringTemplateExpression templateExp = (StringTemplateExpression) varFragment.getInitializer();
			rewrite.set(templateExp,  StringTemplateExpression.MULTI_LINE, Boolean.FALSE, null);
		}

		String preview = evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public class X {\n");
		buf.append("  void foo(Object o) {\n");
		buf.append("    String name = \"Jay\";\n");
		buf.append("    String s = STR.\"Hey there \\{name}!\";\n");
		buf.append("  }\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());
	}
}
