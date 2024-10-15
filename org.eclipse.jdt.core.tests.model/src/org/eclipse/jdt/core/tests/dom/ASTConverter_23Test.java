/*******************************************************************************
 * Copyright (c) 2024 GK Software SE and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan Herrmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.dom;

import java.util.List;
import junit.framework.Test;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImplicitTypeDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;

public class ASTConverter_23Test extends ConverterTestSetup {

	ICompilationUnit workingCopy;

	public void setUpSuite() throws Exception {
		super.setUpSuite();
		this.ast = AST.newAST(getAST23(), false);
		this.currentProject = getJavaProject("Converter_23");
		this.currentProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_23);
		this.currentProject.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_23);
		this.currentProject.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_23);
		this.currentProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
		this.currentProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
	}

	public ASTConverter_23Test(String name) {
		super(name);
	}

	public static Test suite() {
		return buildModelTestSuite(ASTConverter_23Test.class);
	}

	static int getAST23() {
		return AST.JLS23;
	}
	protected void tearDown() throws Exception {
		super.tearDown();
		if (this.workingCopy != null) {
			this.workingCopy.discardWorkingCopy();
			this.workingCopy = null;
		}
	}


	public void test001() throws CoreException {
		String contents = """
				package p;
				import module java.base;
				import static java.lang.System.out;
				class X {
					void m() {
						out.println(Map.class.toString());
					}
				}
				""";
		this.workingCopy = getWorkingCopy("/Converter_23/src/p/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		List<ImportDeclaration> imports = compilationUnit.imports();
		assertEquals("Incorrect no of imports", 2, imports.size());

		{
			ImportDeclaration imp = imports.get(0);
			assertEquals("Incorrect modifier bits", Modifier.MODULE, imp.getModifiers());
			assertEquals("Incorrect no of modifiers", 1, imp.modifiers().size());
			Modifier mod = (Modifier) imp.modifiers().get(0);
			assertEquals("Incorrect modifier", "module", mod.getKeyword().toString());
			assertEquals("Incorrect modifier", Modifier.ModifierKeyword.MODULE_KEYWORD, mod.getKeyword());
			assertEquals("Incorrect position", 18, mod.getStartPosition());
			assertEquals("Incorrect content", "module", contents.substring(mod.getStartPosition(), mod.getStartPosition()+6));
			assertEquals("Incorrect name", "java.base", imp.getName().toString());
		}
		{
			ImportDeclaration imp = imports.get(1);
			assertEquals("Incorrect modifier bits", Modifier.STATIC, imp.getModifiers());
			assertEquals("Incorrect no of modifiers", 1, imp.modifiers().size());
			Modifier mod = (Modifier) imp.modifiers().get(0);
			assertEquals("Incorrect modifier", "static", mod.getKeyword().toString());
			assertEquals("Incorrect modifier", Modifier.ModifierKeyword.STATIC_KEYWORD, mod.getKeyword());
			assertEquals("Incorrect position", 43, mod.getStartPosition());
			assertEquals("Incorrect content", "static", contents.substring(mod.getStartPosition(), mod.getStartPosition()+6));
			assertEquals("Incorrect name", "java.lang.System.out", imp.getName().toString());
		}
	}

	public void test002() throws CoreException {
		String contents = """
					/** */
					void main() {
					    System.out.println("Eclipse");
					}
				""";
		this.workingCopy = getWorkingCopy("/Converter_23/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Wrong type of statement", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		ImplicitTypeDeclaration implicitTypeDeclaration = (ImplicitTypeDeclaration) compilationUnit.types().get(0);
		assertEquals("Not an ImplicitTypeDeclaration Type", implicitTypeDeclaration.getNodeType(), ASTNode.UNNAMED_CLASS);
		assertEquals("Not an ImplicitTypeDeclaration Name Type", implicitTypeDeclaration.getName().getNodeType(), ASTNode.SIMPLE_NAME);
		assertEquals("Identifier is not empty String", implicitTypeDeclaration.getName().getIdentifier(), "");
		MethodDeclaration bodyDeclaration = (MethodDeclaration) implicitTypeDeclaration.bodyDeclarations().get(0);
		assertEquals("Not a Method Declaration", bodyDeclaration.getNodeType(), ASTNode.METHOD_DECLARATION);
		assertEquals("Method Declaration start is not one", bodyDeclaration.getStartPosition(), 1);
		Javadoc javaDoc = bodyDeclaration.getJavadoc();
		assertEquals("Not a JavaDoc", javaDoc.getNodeType(), ASTNode.JAVADOC);
		assertEquals("JavaDoc startPosition is not One", javaDoc.getStartPosition(), 1);
		Block block =  bodyDeclaration.getBody();
		assertEquals("Not a Block", block.getNodeType(), ASTNode.BLOCK);
		assertEquals("Block startPosition is not correct", block.getStartPosition(), 21);
	}
}
