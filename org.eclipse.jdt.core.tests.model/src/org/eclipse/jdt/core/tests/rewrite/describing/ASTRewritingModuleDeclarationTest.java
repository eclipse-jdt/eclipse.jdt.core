/*******************************************************************************
 * Copyright (c) 2017, 2019 IBM Corporation and others.
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

import java.util.List;
import junit.framework.Test;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.ModuleModifier.ModuleModifierKeyword;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.compiler.util.Util;

public class ASTRewritingModuleDeclarationTest extends ASTRewritingTest {

	public ASTRewritingModuleDeclarationTest(String name) {
		super(name);
	}

	public ASTRewritingModuleDeclarationTest(String name, int apiLevel) {
		super(name, apiLevel);
	}

	public static Test suite() {
		return createSuite(ASTRewritingModuleDeclarationTest.class);
	}

	public void testBug509961_0001_since_9() throws Exception {
		IJavaProject javaProject = null ;
		try {
			javaProject = createProject("P_9", JavaCore.VERSION_9);
			IPackageFragmentRoot currentSourceFolder = getPackageFragmentRoot("P_9", "src");
			IPackageFragment pack1= currentSourceFolder.getPackageFragment(Util.EMPTY_STRING);
			StringBuilder buf= new StringBuilder();
			buf.append("module first {\n");
			buf.append("    requires second;\n");
			buf.append("    requires removeme;\n");
			buf.append("    exports pack11 to third, fourth;\n");
			buf.append("    exports pack12 to fifth;\n");
			buf.append("    exports pack12 to remove.mod1;\n");
			buf.append("    exports pack13 to well.founded.module2;\n");
			buf.append("    uses MyType;\n");
			buf.append("    uses Type.Remove;\n");
			buf.append("    provides pack22.I22 with pack11.packinternal.Z11;\n");
			buf.append("    provides pack23.I23 with pack11.Z23, pack12.ZZ23;\n");
			buf.append("}");
			ICompilationUnit cu= pack1.createCompilationUnit("module-info.java", buf.toString(), false, null);
			CompilationUnit astRoot= createAST(cu);
			ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
			AST ast= astRoot.getAST();

			ModuleDeclaration moduleDecl = astRoot.getModule();
			ListRewrite listRewrite = rewrite.getListRewrite(moduleDecl, ModuleDeclaration.MODULE_DIRECTIVES_PROPERTY);
			List<ModuleDirective> moduleStatements = moduleDecl.moduleStatements();
			int index = 0;
			{
				RequiresDirective req = (RequiresDirective) moduleStatements.get(index++); // replace the module in first required
				Name newName = ast.newSimpleName("newSecond");
				rewrite.replace(req.getName(), newName, null);
				listRewrite.remove(moduleStatements.get(index++), null); // remove the second required

				RequiresDirective newNode = ast.newRequiresDirective(); // add a new required
				newNode.setName(ast.newSimpleName("addedme"));
				listRewrite.insertAfter(newNode, req, null);
			}
			{
				// exports pack11 to third, fourth; -> exports newpack11 to third;
				ExportsDirective exp = (ExportsDirective) moduleStatements.get(index++);
				Name newName = ast.newSimpleName("newpack11");
				rewrite.replace(exp.getName(), newName, null);
				ListRewrite expListRewrite = rewrite.getListRewrite(exp, ExportsDirective.MODULES_PROPERTY);
				expListRewrite.remove((ASTNode) exp.modules().get(1), null);

				// exports pack12 to fifth -> exports pack12 to fifth, sixth
				exp = (ExportsDirective) moduleStatements.get(index++);
				newName = ast.newSimpleName("sixth");
				expListRewrite = rewrite.getListRewrite(exp, ExportsDirective.MODULES_PROPERTY);
				expListRewrite.insertLast(newName, null);

				// exports pack12 to remove.mod1 -> exports pack12
				exp = (ExportsDirective) moduleStatements.get(index++);
				expListRewrite = rewrite.getListRewrite(exp, ExportsDirective.MODULES_PROPERTY);
				expListRewrite.remove((ASTNode) exp.modules().get(0), null);

				// exports pack12 to never.to.be.module - remove the export
				listRewrite.remove((ASTNode) moduleDecl.moduleStatements().get(index++), null);

				// exports pack12 to new.found.module - add the export
				exp = ast.newExportsStatement();
				exp.setName(ast.newSimpleName("pack12"));
				Name name = ast.newName("well.founded.module3");
				exp.modules().add(name);
				listRewrite.insertLast(exp, null);
			}
			{
				// uses MyType -> uses MyNewType;
				UsesDirective usesStatement = (UsesDirective) moduleStatements.get(index++);
				Name newName = ast.newSimpleName("MyNewType");
				rewrite.replace(usesStatement.getName(), newName, null);

				// uses Type.Remove - remove the uses
				listRewrite.remove(moduleStatements.get(index++), null);

				// uses MyNewFoundType - add the uses
				usesStatement = ast.newUsesDirective();
				newName = ast.newSimpleName("MyNewFoundType");
				usesStatement.setName(newName);
				listRewrite.insertLast(usesStatement, null);
			}
			{
				// provides pack22.I22 with pack11.packinternal.Z11 ->  provides pack22.INew22 with pack11.packinternal.NewZ11, pack11.Y11
				ProvidesDirective providesStatement = (ProvidesDirective) moduleStatements.get(index++);
				Name newName = ast.newName("pack22.INew22");
				rewrite.replace(providesStatement.getName(), newName, null);
				newName = ast.newName("pack11.packinternal.NewZ11");
				ListRewrite pListRewrite = rewrite.getListRewrite(providesStatement, ProvidesDirective.IMPLEMENTATIONS_PROPERTY);
				pListRewrite.replace((ASTNode) providesStatement.implementations().get(0), newName ,null);

				newName = ast.newName("pack11.Y11");
				pListRewrite.insertLast(newName, null);
				// provides pack23.I23 with pack11.Z23, pack12.ZZ23 -> provides pack23.I23 with pack12.ZZ23
				providesStatement = (ProvidesDirective) moduleStatements.get(index++);
				pListRewrite = rewrite.getListRewrite(providesStatement, ProvidesDirective.IMPLEMENTATIONS_PROPERTY);
				pListRewrite.remove((ASTNode) providesStatement.implementations().get(0), null);
			}
			String preview= evaluateRewrite(cu, rewrite);
			buf= new StringBuilder();
			buf.append("module first {\n");
			buf.append("    requires newSecond;\n");
			buf.append("    requires addedme;\n");
			buf.append("    exports newpack11 to third;\n");
			buf.append("    exports pack12 to fifth, sixth;\n");
			buf.append("    exports pack12;\n");
			buf.append("    uses MyNewType;\n");
			buf.append("    provides pack22.INew22 with pack11.packinternal.NewZ11, pack11.Y11;\n");
			buf.append("    provides pack23.I23 with pack12.ZZ23;\n");
			buf.append("    exports pack12 to well.founded.module3;\n");
			buf.append("    uses MyNewFoundType;\n");
			buf.append("}");
			assertEqualString(preview, buf.toString());
		} finally {
			if (javaProject != null)
				deleteProject(javaProject);
		}
	}
	public void testBug509961_0002_since_9() throws Exception {
		IJavaProject javaProject = null;
		try {
			javaProject = createProject("P_9", JavaCore.VERSION_9);
			IPackageFragmentRoot currentSourceFolder = getPackageFragmentRoot("P_9", "src");
			IPackageFragment pack1= currentSourceFolder.getPackageFragment(Util.EMPTY_STRING);
			StringBuilder buf= new StringBuilder();
			buf.append("module first {\n");
			buf.append("    requires existing;\n");
			buf.append("}");

			ICompilationUnit cu= pack1.createCompilationUnit("module-info.java", buf.toString(), false, null);
			CompilationUnit astRoot= createAST(cu);
			ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
			AST ast= astRoot.getAST();

			ModuleDeclaration moduleDecl = astRoot.getModule();
			ListRewrite listRewrite = rewrite.getListRewrite(moduleDecl, ModuleDeclaration.MODULE_DIRECTIVES_PROPERTY);
			{
				RequiresDirective newNode = ast.newRequiresDirective(); // add a new required
				newNode.setName(ast.newSimpleName("addedme"));
				listRewrite.insertLast(newNode, null);
			}
			String preview= evaluateRewrite(cu, rewrite);
			buf= new StringBuilder();
			buf.append("module first {\n");
			buf.append("    requires existing;\n");
			buf.append("    requires addedme;\n");
			buf.append("}");
			assertEqualString(preview, buf.toString());
		} finally {
			if (javaProject != null) deleteProject(javaProject);
		}
	}

	public void testBug509961_0003_since_9() throws Exception {
		IJavaProject javaProject = null;
		try {
			javaProject = createProject("P_9", JavaCore.VERSION_9);
			IPackageFragmentRoot currentSourceFolder = getPackageFragmentRoot("P_9", "src");
			IPackageFragment pack1= currentSourceFolder.getPackageFragment(Util.EMPTY_STRING);
			StringBuilder buf= new StringBuilder();
			buf.append("module first {\n");
			buf.append("    requires existing;\n");
			buf.append("    requires static module1;\n");
			buf.append("    requires static module2;\n");
			buf.append("    requires static module3;\n");
			buf.append("}");
			ICompilationUnit cu= pack1.createCompilationUnit("module-info.java", buf.toString(), false, null);
			CompilationUnit astRoot= createAST(cu);
			ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
			AST ast= astRoot.getAST();
			ModuleDeclaration moduleDecl = astRoot.getModule();
			ListRewrite listRewrite = rewrite.getListRewrite(moduleDecl, ModuleDeclaration.MODULE_DIRECTIVES_PROPERTY);
			{
				RequiresDirective reqNode = (RequiresDirective) moduleDecl.moduleStatements().get(0);
				ASTNode newModifier = ast.newModuleModifier(ModuleModifierKeyword.STATIC_KEYWORD);
				rewrite.getListRewrite(reqNode, RequiresDirective.MODIFIERS_PROPERTY).insertFirst(newModifier, null);

				reqNode = (RequiresDirective) moduleDecl.moduleStatements().get(1);
				rewrite.getListRewrite(reqNode, RequiresDirective.MODIFIERS_PROPERTY).remove((ASTNode) reqNode.modifiers().get(0), null);

				reqNode = (RequiresDirective) moduleDecl.moduleStatements().get(2);
				newModifier = ast.newModuleModifier(ModuleModifierKeyword.TRANSITIVE_KEYWORD);
				rewrite.getListRewrite(reqNode, RequiresDirective.MODIFIERS_PROPERTY).replace((ASTNode) reqNode.modifiers().get(0), newModifier, null);

				reqNode = (RequiresDirective) moduleDecl.moduleStatements().get(3);
				newModifier = ast.newModuleModifier(ModuleModifierKeyword.TRANSITIVE_KEYWORD);
				rewrite.getListRewrite(reqNode, RequiresDirective.MODIFIERS_PROPERTY).insertLast(newModifier, null);

				RequiresDirective newNode = ast.newRequiresDirective(); // add a new required
				newNode.setName(ast.newSimpleName("addedme"));
				newModifier = ast.newModuleModifier(ModuleModifierKeyword.TRANSITIVE_KEYWORD);
				rewrite.getListRewrite(newNode, RequiresDirective.MODIFIERS_PROPERTY).insertFirst(newModifier, null);
				listRewrite.insertLast(newNode, null);
			}
			String preview= evaluateRewrite(cu, rewrite);
			buf= new StringBuilder();
			buf.append("module first {\n");
			buf.append("    requires static existing;\n");
			buf.append("    requires module1;\n");
			buf.append("    requires transitive module2;\n");
			buf.append("    requires static transitive module3;\n");
			buf.append("    requires transitive addedme;\n");
		buf.append("}");
			assertEqualString(preview, buf.toString());
		} finally {
			if (javaProject != null) deleteProject(javaProject);
		}
	}
	public void testBug516731_0001_since_9() throws Exception {
		IJavaProject javaProject = null;
		try {
			javaProject = createProject("P_9", JavaCore.VERSION_9);
			IPackageFragmentRoot currentSourceFolder = getPackageFragmentRoot("P_9", "src");
			IPackageFragment pack1= currentSourceFolder.getPackageFragment(Util.EMPTY_STRING);
			StringBuilder buf= new StringBuilder();
			buf.append("module first {\n");
			buf.append("    requires existing;\n");
			buf.append("}");
			ICompilationUnit cu= pack1.createCompilationUnit("module-info.java", buf.toString(), false, null);
			CompilationUnit astRoot= createAST(cu);
			ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
			ModuleDeclaration moduleDecl = astRoot.getModule();
			rewrite.set(moduleDecl, ModuleDeclaration.OPEN_PROPERTY, Boolean.TRUE, null);
			String preview= evaluateRewrite(cu, rewrite);
			buf= new StringBuilder();
			buf.append("open module first {\n");
			buf.append("    requires existing;\n");
			buf.append("}");
			assertEqualString(preview, buf.toString());
		} finally {
			if (javaProject != null) deleteProject(javaProject);
		}
	}
	public void testBug516731_0002_since_9() throws Exception {
		IJavaProject javaProject = null;
		try {
			javaProject = createProject("P_9", JavaCore.VERSION_9);
			IPackageFragmentRoot currentSourceFolder = getPackageFragmentRoot("P_9", "src");
			IPackageFragment pack1= currentSourceFolder.getPackageFragment(Util.EMPTY_STRING);
			StringBuilder buf= new StringBuilder();
			buf.append("open module first {\n");
			buf.append("    requires existing;\n");
			buf.append("}");
			ICompilationUnit cu= pack1.createCompilationUnit("module-info.java", buf.toString(), false, null);
			CompilationUnit astRoot= createAST(cu);
			ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
			ModuleDeclaration moduleDecl = astRoot.getModule();
			rewrite.set(moduleDecl, ModuleDeclaration.OPEN_PROPERTY, Boolean.FALSE, null);
			String preview= evaluateRewrite(cu, rewrite);
			buf= new StringBuilder();
			buf.append("module first {\n");
			buf.append("    requires existing;\n");
			buf.append("}");
			assertEqualString(preview, buf.toString());
		} finally {
			if (javaProject != null) deleteProject(javaProject);
		}
	}
	public void testBug516731_0003_since_9() throws Exception {
		IJavaProject javaProject = null;
		try {
			javaProject = createProject("P_9", JavaCore.VERSION_9);
			IPackageFragmentRoot currentSourceFolder = getPackageFragmentRoot("P_9", "src");
			IPackageFragment pack1= currentSourceFolder.getPackageFragment(Util.EMPTY_STRING);
			StringBuilder buf= new StringBuilder();
			buf.append("module first {\n");
			buf.append("    requires existing;\n");
			buf.append("    requires static module1;\n");
			buf.append("    requires static module2;\n");
			buf.append("    requires static module3;\n");
			buf.append("    requires static module4;\n");
			buf.append("}");
			ICompilationUnit cu= pack1.createCompilationUnit("module-info.java", buf.toString(), false, null);
			CompilationUnit astRoot= createAST(cu);
			ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
			AST ast= astRoot.getAST();
			ModuleDeclaration moduleDecl = astRoot.getModule();
			ListRewrite listRewrite = rewrite.getListRewrite(moduleDecl, ModuleDeclaration.MODULE_DIRECTIVES_PROPERTY);
			{
				int count = 0;
				RequiresDirective reqNode = (RequiresDirective) moduleDecl.moduleStatements().get(count++);
				ASTNode newModifier = ast.newModuleModifier(ModuleModifierKeyword.STATIC_KEYWORD);
				rewrite.getListRewrite(reqNode, RequiresDirective.MODIFIERS_PROPERTY).insertFirst(newModifier, null);
				newModifier = ast.newModuleModifier(ModuleModifierKeyword.TRANSITIVE_KEYWORD);
				rewrite.getListRewrite(reqNode, RequiresDirective.MODIFIERS_PROPERTY).insertLast(newModifier, null);

				reqNode = (RequiresDirective) moduleDecl.moduleStatements().get(count++);
				rewrite.getListRewrite(reqNode, RequiresDirective.MODIFIERS_PROPERTY).remove((ASTNode) reqNode.modifiers().get(0), null);

				reqNode = (RequiresDirective) moduleDecl.moduleStatements().get(count++);
				newModifier = ast.newModuleModifier(ModuleModifierKeyword.TRANSITIVE_KEYWORD);
				rewrite.getListRewrite(reqNode, RequiresDirective.MODIFIERS_PROPERTY).replace((ASTNode) reqNode.modifiers().get(0), newModifier, null);

				reqNode = (RequiresDirective) moduleDecl.moduleStatements().get(count++);
				newModifier = ast.newModuleModifier(ModuleModifierKeyword.TRANSITIVE_KEYWORD);
				rewrite.getListRewrite(reqNode, RequiresDirective.MODIFIERS_PROPERTY).insertLast(newModifier, null);

				reqNode = (RequiresDirective) moduleDecl.moduleStatements().get(count++);
				newModifier = ast.newModuleModifier(ModuleModifierKeyword.TRANSITIVE_KEYWORD);
				rewrite.getListRewrite(reqNode, RequiresDirective.MODIFIERS_PROPERTY).insertFirst(newModifier, null);

				RequiresDirective newNode = ast.newRequiresDirective(); // add a new required
				newNode.setName(ast.newSimpleName("addedme"));
				newModifier = ast.newModuleModifier(ModuleModifierKeyword.TRANSITIVE_KEYWORD);
				rewrite.getListRewrite(newNode, RequiresDirective.MODIFIERS_PROPERTY).insertFirst(newModifier, null);
				listRewrite.insertLast(newNode, null);
			}
			String preview= evaluateRewrite(cu, rewrite);
			buf= new StringBuilder();
			buf.append("module first {\n");
			buf.append("    requires static transitive existing;\n");
			buf.append("    requires module1;\n");
			buf.append("    requires transitive module2;\n");
			buf.append("    requires static transitive module3;\n");
			buf.append("    requires transitive static module4;\n");
			buf.append("    requires transitive addedme;\n");
		buf.append("}");
			assertEqualString(preview, buf.toString());
		} finally {
			if (javaProject != null) deleteProject(javaProject);
		}
	}

	public void testBug542106_since_9() throws Exception {
		IJavaProject javaProject = null;
		try {
			javaProject = createProject("P_9", JavaCore.VERSION_9);
			IPackageFragmentRoot currentSourceFolder = getPackageFragmentRoot("P_9", "src");
			IPackageFragment pack1= currentSourceFolder.getPackageFragment(Util.EMPTY_STRING);
			String content =
					"import java.util.*;\n" +
					"import java.util.function.Consumer;\n" +
					"module first {\n" +
					"}";
			ICompilationUnit cu= pack1.createCompilationUnit("module-info.java", content, false, null);
			CompilationUnit astRoot= createAST(cu);
			ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
			AST ast= astRoot.getAST();
			ListRewrite listRewrite = rewrite.getListRewrite(astRoot, CompilationUnit.IMPORTS_PROPERTY);
			{
				listRewrite.remove((ImportDeclaration) astRoot.imports().get(0), null);
				ImportDeclaration newImport = ast.newImportDeclaration();
				newImport.setName(ast.newName("java.io.Serializable"));
				listRewrite.insertFirst(newImport, null);
			}
			String preview= evaluateRewrite(cu, rewrite);
			content =
					"import java.io.Serializable;\n" +
					"import java.util.function.Consumer;\n" +
					"module first {\n" +
					"}";
			assertEqualString(preview, content);
		} finally {
			if (javaProject != null) deleteProject(javaProject);
		}
	}
}