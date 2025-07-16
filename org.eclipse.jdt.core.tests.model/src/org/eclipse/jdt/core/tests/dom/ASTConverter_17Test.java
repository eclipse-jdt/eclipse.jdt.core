/*******************************************************************************
 * Copyright (c) 2021, 2022 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.dom;

import java.util.List;
import junit.framework.Test;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;

@SuppressWarnings("rawtypes")
public class ASTConverter_17Test extends ConverterTestSetup {

	ICompilationUnit workingCopy;

	@SuppressWarnings("deprecation")
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		this.ast = AST.newAST(getAST17(), false);
		this.currentProject = getJavaProject("Converter_17");
		if (this.ast.apiLevel() == AST.JLS17 ) {
			this.currentProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_17);
			this.currentProject.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_17);
			this.currentProject.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_17);

		}
	}

	public ASTConverter_17Test(String name) {
		super(name);
	}

	public static Test suite() {
		return buildModelTestSuite(ASTConverter_17Test.class);
	}

	@SuppressWarnings("deprecation")
	static int getAST17() {
		return AST.JLS17;
	}
	protected void tearDown() throws Exception {
		super.tearDown();
		if (this.workingCopy != null) {
			this.workingCopy.discardWorkingCopy();
			this.workingCopy = null;
		}
	}


	public void testSealed001() throws CoreException {
		if (!isJRE17) {
			System.err.println("Test "+getName()+" requires a JRE 17");
			return;
		}
		String contents = "public sealed class X permits X1{\n" +
				"\n" +
				"}\n" +
				"non-sealed class X1 extends X {\n" +
				"\n" +
				"}\n";
		this.workingCopy = getWorkingCopy("/Converter_17/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = ((AbstractTypeDeclaration)compilationUnit.types().get(0));
		assertEquals("Not a Type Declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration type = (TypeDeclaration)node;
		List modifiers = type.modifiers();
		assertEquals("Incorrect no of modifiers", 2, modifiers.size());
		Modifier modifier = (Modifier) modifiers.get(1);
		assertSame("Incorrect modifier keyword", Modifier.ModifierKeyword.SEALED_KEYWORD, modifier.getKeyword());
		List permittedTypes = type.permittedTypes();
		assertEquals("Incorrect no of permits", 1, permittedTypes.size());
		assertEquals("Incorrect type of permit", "org.eclipse.jdt.core.dom.SimpleType", permittedTypes.get(0).getClass().getName());
		node = ((AbstractTypeDeclaration)compilationUnit.types().get(1));
		assertEquals("Not a Type Declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		type = (TypeDeclaration)node;
		modifiers = type.modifiers();
		assertEquals("Incorrect no of modfiers", 1, modifiers.size());
		modifier = (Modifier) modifiers.get(0);
		assertSame("Incorrect modifier keyword", Modifier.ModifierKeyword.NON_SEALED_KEYWORD, modifier.getKeyword());

	}

	public void testSealed002() throws CoreException {
		if (!isJRE17) {
			System.err.println("Test "+getName()+" requires a JRE 17");
			return;
		}
		String contents = "public sealed interface X permits X1{\n" +
				"\n" +
				"}\n" +
				"non-sealed interface X1 extends X {\n" +
				"\n" +
				"}\n";
		this.workingCopy = getWorkingCopy("/Converter_17/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = ((AbstractTypeDeclaration)compilationUnit.types().get(0));
		assertEquals("Not a Record Declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration type = (TypeDeclaration)node;
		List modifiers = type.modifiers();
		assertEquals("Incorrect no of modfiers", 2, modifiers.size());
		Modifier modifier = (Modifier) modifiers.get(1);
		assertSame("Incorrect modifier keyword", Modifier.ModifierKeyword.SEALED_KEYWORD, modifier.getKeyword());

	}

	public void testSealed003() throws CoreException {
		if (!isJRE17) {
			System.err.println("Test "+getName()+" requires a JRE 17");
			return;
		}
		String contents = "public sealed interface X permits X1{\n" +
				"\n" +
				"}\n" +
				"non-sealed interface X1 extends X {\n" +
				"\n" +
				"}\n";
		this.workingCopy = getWorkingCopy("/Converter_17/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		List<AbstractTypeDeclaration> types = compilationUnit.types();
		assertEquals("No. of Types is not 2", types.size(), 2);
		AbstractTypeDeclaration type = types.get(0);
		if (!type.getName().getIdentifier().equals("X")) {
			type = types.get(1);
		}
		assertTrue("type not a type", type instanceof TypeDeclaration);
		TypeDeclaration typeDecl = (TypeDeclaration)type;
		assertTrue("type not an interface", typeDecl.isInterface());
		List modifiers = type.modifiers();
		assertEquals("Incorrect no of modifiers", 2, modifiers.size());
		Modifier modifier = (Modifier) modifiers.get(1);
		assertSame("Incorrect modifier keyword", Modifier.ModifierKeyword.SEALED_KEYWORD, modifier.getKeyword());
		int startPos = modifier.getStartPosition();
		assertEquals("Restricter identifier position for sealed is not 7", startPos, contents.indexOf("sealed"));
		startPos = typeDecl.getRestrictedIdentifierStartPosition();
		assertEquals("Restricter identifier position for permits is not 26", startPos, contents.indexOf("permits"));
	}

	public void _testSealed004() throws CoreException {
		if (!isJRE17) {
			System.err.println("Test "+getName()+" requires a JRE 17");
			return;
		}
		String contents = "public sealed class X permits X1{\n" +
				"\n" +
				"}\n" +
				"non-sealed class X1 extends X {\n" +
				"\n" +
				"}\n";
		this.workingCopy = getWorkingCopy("/Converter_17/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		List<AbstractTypeDeclaration> types = compilationUnit.types();
		assertEquals("No. of Types is not 2", types.size(), 2);
		AbstractTypeDeclaration type = types.get(0);
		if (!type.getName().getIdentifier().equals("X")) {
			type = types.get(1);
		}
		assertTrue("type not a type", type instanceof TypeDeclaration);
		TypeDeclaration typeDecl = (TypeDeclaration)type;
		assertTrue("type not an class", !typeDecl.isInterface());
		List modifiers = type.modifiers();
		assertEquals("Incorrect no of modifiers", 2, modifiers.size());
		Modifier modifier = (Modifier) modifiers.get(1);
		assertSame("Incorrect modifier keyword", Modifier.ModifierKeyword.SEALED_KEYWORD, modifier.getKeyword());
		int startPos = modifier.getStartPosition();
		assertEquals("Restricter identifier position for sealed is not 7", startPos, contents.indexOf("sealed"));
		startPos = typeDecl.getRestrictedIdentifierStartPosition();
		assertEquals("Restricter identifier position for permits is not 26", startPos, contents.indexOf("permits"));
	}


}
