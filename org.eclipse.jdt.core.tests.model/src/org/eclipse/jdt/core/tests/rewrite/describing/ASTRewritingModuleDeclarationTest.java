/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
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
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
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
			StringBuffer buf= new StringBuffer();
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
			ListRewrite listRewrite = rewrite.getListRewrite(moduleDecl, ModuleDeclaration.MODULE_STATEMENTS_PROPERTY);
			List<ModuleStatement> moduleStatements = moduleDecl.moduleStatements();
			int index = 0;
			{
				RequiresStatement req = (RequiresStatement) moduleStatements.get(index++); // replace the module in first required
				Name newName = ast.newSimpleName("newSecond");
				rewrite.replace(req.getName(), newName, null);
				listRewrite.remove(moduleStatements.get(index++), null); // remove the second required

				RequiresStatement newNode = ast.newRequiresStatement(); // add a new required
				newNode.setName(ast.newSimpleName("addedme"));
				listRewrite.insertAfter(newNode, req, null);
			}
			{
				// exports pack11 to third, fourth; -> exports newpack11 to third;
				ExportsStatement exp = (ExportsStatement) moduleStatements.get(index++);
				Name newName = ast.newSimpleName("newpack11");
				rewrite.replace(exp.getName(), newName, null);
				ListRewrite expListRewrite = rewrite.getListRewrite(exp, ExportsStatement.MODULES_PROPERTY);
				expListRewrite.remove((ASTNode) exp.modules().get(1), null); 

				// exports pack12 to fifth -> exports pack12 to fifth, sixth
				exp = (ExportsStatement) moduleStatements.get(index++);
				newName = ast.newSimpleName("sixth");
				expListRewrite = rewrite.getListRewrite(exp, ExportsStatement.MODULES_PROPERTY);
				expListRewrite.insertLast(newName, null);

				// exports pack12 to remove.mod1 -> exports pack12
				exp = (ExportsStatement) moduleStatements.get(index++);
				expListRewrite = rewrite.getListRewrite(exp, ExportsStatement.MODULES_PROPERTY);
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
				UsesStatement usesStatement = (UsesStatement) moduleStatements.get(index++);
				Name newName = ast.newSimpleName("MyNewType");
				SimpleType type = ast.newSimpleType(newName);
				rewrite.replace(usesStatement.getType(), type, null);

				// uses Type.Remove - remove the uses
				listRewrite.remove(moduleStatements.get(index++), null);

				// uses MyNewFoundType - add the uses
				usesStatement = ast.newUsesStatement();
				newName = ast.newSimpleName("MyNewFoundType");
				type = ast.newSimpleType(newName);
				usesStatement.setType(type);
				listRewrite.insertLast(usesStatement, null);
			}
			{
				// provides pack22.I22 with pack11.packinternal.Z11 ->  provides pack22.INew22 with pack11.packinternal.NewZ11, pack11.Y11
				ProvidesStatement providesStatement = (ProvidesStatement) moduleStatements.get(index++);
				Name newName = ast.newName("pack22.INew22");
				SimpleType type = ast.newSimpleType(newName);
				rewrite.replace(providesStatement.getType(), type, null);
				newName = ast.newName("pack11.packinternal.NewZ11");
				type = ast.newSimpleType(newName);
				ListRewrite pListRewrite = rewrite.getListRewrite(providesStatement, ProvidesStatement.IMPLEMENTATIONS_PROPERTY);
				pListRewrite.replace((ASTNode) providesStatement.implementations().get(0), type ,null);

				newName = ast.newName("pack11.Y11");
				type = ast.newSimpleType(newName);
				pListRewrite.insertLast(type, null);
				// provides pack23.I23 with pack11.Z23, pack12.ZZ23 -> provides pack23.I23 with pack12.ZZ23
				providesStatement = (ProvidesStatement) moduleStatements.get(index++);
				pListRewrite = rewrite.getListRewrite(providesStatement, ProvidesStatement.IMPLEMENTATIONS_PROPERTY);
				pListRewrite.remove((ASTNode) providesStatement.implementations().get(0), null);
			}
			String preview= evaluateRewrite(cu, rewrite);
			buf= new StringBuffer();
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
			StringBuffer buf= new StringBuffer();
			buf.append("module first {\n");
			buf.append("    requires existing;\n");
			buf.append("}");

			ICompilationUnit cu= pack1.createCompilationUnit("module-info.java", buf.toString(), false, null);
			CompilationUnit astRoot= createAST(cu);
			ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
			AST ast= astRoot.getAST();

			ModuleDeclaration moduleDecl = astRoot.getModule();
			ListRewrite listRewrite = rewrite.getListRewrite(moduleDecl, ModuleDeclaration.MODULE_STATEMENTS_PROPERTY);
			{
				RequiresStatement newNode = ast.newRequiresStatement(); // add a new required
				newNode.setName(ast.newSimpleName("addedme"));
				listRewrite.insertLast(newNode, null);
			}
			String preview= evaluateRewrite(cu, rewrite);
			buf= new StringBuffer();
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
			StringBuffer buf= new StringBuffer();
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
			ListRewrite listRewrite = rewrite.getListRewrite(moduleDecl, ModuleDeclaration.MODULE_STATEMENTS_PROPERTY);
			{
				RequiresStatement reqNode = (RequiresStatement) moduleDecl.moduleStatements().get(0);
				ASTNode newModifier = ast.newModifier(ModifierKeyword.STATIC_KEYWORD);
				rewrite.getListRewrite(reqNode, RequiresStatement.MODIFIERS_PROPERTY).insertFirst(newModifier, null);

				reqNode = (RequiresStatement) moduleDecl.moduleStatements().get(1);
				rewrite.getListRewrite(reqNode, RequiresStatement.MODIFIERS_PROPERTY).remove((ASTNode) reqNode.modifiers().get(0), null);

				reqNode = (RequiresStatement) moduleDecl.moduleStatements().get(2);
				newModifier = ast.newModifier(ModifierKeyword.TRANSIENT_KEYWORD);
				rewrite.getListRewrite(reqNode, RequiresStatement.MODIFIERS_PROPERTY).replace((ASTNode) reqNode.modifiers().get(0), newModifier, null);

				reqNode = (RequiresStatement) moduleDecl.moduleStatements().get(3);
				newModifier = ast.newModifier(ModifierKeyword.TRANSIENT_KEYWORD);
				rewrite.getListRewrite(reqNode, RequiresStatement.MODIFIERS_PROPERTY).insertLast(newModifier, null);

				RequiresStatement newNode = ast.newRequiresStatement(); // add a new required
				newNode.setName(ast.newSimpleName("addedme"));
				newModifier = ast.newModifier(ModifierKeyword.TRANSIENT_KEYWORD);
				rewrite.getListRewrite(newNode, RequiresStatement.MODIFIERS_PROPERTY).insertFirst(newModifier, null);
				listRewrite.insertLast(newNode, null);
			}
			String preview= evaluateRewrite(cu, rewrite);
			buf= new StringBuffer();
			buf.append("module first {\n");
			buf.append("    requires static existing;\n");
			buf.append("    requires module1;\n");
			buf.append("    requires transient module2;\n");
			buf.append("    requires static transient module3;\n");
			buf.append("    requires transient addedme;\n");
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
			StringBuffer buf= new StringBuffer();
			buf.append("module first {\n");
			buf.append("    requires existing;\n");
			buf.append("}");
			ICompilationUnit cu= pack1.createCompilationUnit("module-info.java", buf.toString(), false, null);
			CompilationUnit astRoot= createAST(cu);
			ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
			ModuleDeclaration moduleDecl = astRoot.getModule();
			rewrite.set(moduleDecl, ModuleDeclaration.OPEN_PROPERTY, Boolean.TRUE, null);
			String preview= evaluateRewrite(cu, rewrite);
			buf= new StringBuffer();
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
			StringBuffer buf= new StringBuffer();
			buf.append("open module first {\n");
			buf.append("    requires existing;\n");
			buf.append("}");
			ICompilationUnit cu= pack1.createCompilationUnit("module-info.java", buf.toString(), false, null);
			CompilationUnit astRoot= createAST(cu);
			ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
			ModuleDeclaration moduleDecl = astRoot.getModule();
			rewrite.set(moduleDecl, ModuleDeclaration.OPEN_PROPERTY, Boolean.FALSE, null);
			String preview= evaluateRewrite(cu, rewrite);
			buf= new StringBuffer();
			buf.append("module first {\n");
			buf.append("    requires existing;\n");
			buf.append("}");
			assertEqualString(preview, buf.toString());
		} finally {
			if (javaProject != null) deleteProject(javaProject);
		}
	}

}