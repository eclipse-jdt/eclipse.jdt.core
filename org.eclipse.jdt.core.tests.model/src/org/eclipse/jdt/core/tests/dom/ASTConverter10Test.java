/*******************************************************************************
 * Copyright (c) 2018, 2019 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.dom;

import junit.framework.Test;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import java.io.IOException;
import java.util.Hashtable;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;

public class ASTConverter10Test extends ConverterTestSetup {

	ICompilationUnit workingCopy;
//	private static final String jcl9lib = "CONVERTER_JCL9_LIB";


	@SuppressWarnings("deprecation")
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		this.ast = AST.newAST(AST_INTERNAL_JLS10);
	}

	public ASTConverter10Test(String name) {
		super(name);
	}

	static {
//		TESTS_NUMBERS = new int[] { 19 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] {"testBug532421_002"};
	}
	public static Test suite() {
		String javaVersion = System.getProperty("java.version");
		if (javaVersion.length() > 3) {
			javaVersion = javaVersion.substring(0, 3);
		}
		long jdkLevel = CompilerOptions.versionToJdkLevel(javaVersion);
		if (jdkLevel >= ClassFileConstants.JDK9) {
			isJRE9 = true;
		}
		return buildModelTestSuite(ASTConverter10Test.class);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		if (this.workingCopy != null) {
			this.workingCopy.discardWorkingCopy();
			this.workingCopy = null;
		}
	}

	public void testBug527558_001() throws JavaModelException {
		String contents =
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		var x = new X();\n" +
				"       for (var i = 0; i < 10; ++i) {}\n" +
				"	}\n" +
				"}";
			this.workingCopy = getWorkingCopy("/Converter10/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(contents, this.workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			node = getASTNode((CompilationUnit)node, 0, 0);
			MethodDeclaration methodDeclaration = (MethodDeclaration) node;
			VariableDeclarationStatement vStmt = (VariableDeclarationStatement) methodDeclaration.getBody().statements().get(0);
			checkSourceRange(vStmt, "var x = new X();", contents);
			Type type = vStmt.getType();
			assertNotNull(type);
			assertTrue("not a var", type.isVar());
			IBinding binding = type.resolveBinding();
			assertTrue("null binding", binding != null);
	}
	public void testBug527558_002() throws JavaModelException {
		String contents =
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		var i = y -> 1;\n" +
				"	}\n" +
				"}\n" +
				"interface I {\n" +
				"	public int foo(int i);\n" +
				"}\n";
			this.workingCopy = getWorkingCopy("/Converter10/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(contents, this.workingCopy, false);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			node = getASTNode((CompilationUnit)node, 0, 0);
			MethodDeclaration methodDeclaration = (MethodDeclaration) node;
			VariableDeclarationStatement vStmt = (VariableDeclarationStatement) methodDeclaration.getBody().statements().get(0);
			checkSourceRange(vStmt, "var i = y -> 1;", contents);
			Type type = vStmt.getType();
			assertTrue("not a var", type.isVar());
			IBinding binding = type.resolveBinding();
			assertTrue("null binding", binding != null);
	}
	public void testBug532421_001() throws JavaModelException {
		String contents =
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		var arr1 = new String[10];\n" +
				"	}\n" +
				"}";
			this.workingCopy = getWorkingCopy("/Converter10/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(contents, this.workingCopy, false);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			node = getASTNode((CompilationUnit)node, 0, 0);
			MethodDeclaration methodDeclaration = (MethodDeclaration) node;
			VariableDeclarationStatement vStmt = (VariableDeclarationStatement) methodDeclaration.getBody().statements().get(0);
			checkSourceRange(vStmt, "var arr1 = new String[10];", contents);
			Type type = vStmt.getType();
			assertTrue("not a var", type.isVar());
			IBinding binding = type.resolveBinding();
			assertTrue("null binding", binding != null);
			assertTrue("binding incorrect", binding.getName().equals("String[]"));
	}
	public void testBug532421_002() throws JavaModelException {
		String contents =
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		var list = new Y<String>();\n" +
				"	}\n" +
				"}\n" +
				"class Y<T> {}";
			this.workingCopy = getWorkingCopy("/Converter10/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(contents, this.workingCopy, false);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			node = getASTNode((CompilationUnit)node, 0, 0);
			MethodDeclaration methodDeclaration = (MethodDeclaration) node;
			VariableDeclarationStatement vStmt = (VariableDeclarationStatement) methodDeclaration.getBody().statements().get(0);
			checkSourceRange(vStmt, "var list = new Y<String>();", contents);
			Type type = vStmt.getType();
			assertTrue("not a var", type.isVar());
			IBinding binding = type.resolveBinding();
			assertTrue("null binding", binding != null);
			assertTrue("binding incorrect", binding.getName().equals("Y<String>"));
	}
	public void testBug532535_001() throws JavaModelException {
		String contents =
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		var s = new Y();\n" +
				"	}\n" +
				"}\n" +
				"class Y {}";
			this.workingCopy = getWorkingCopy("/Converter10/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(contents, this.workingCopy, false);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			node = getASTNode((CompilationUnit)node, 0, 0);
			MethodDeclaration methodDeclaration = (MethodDeclaration) node;
			VariableDeclarationStatement vStmt = (VariableDeclarationStatement) methodDeclaration.getBody().statements().get(0);
			checkSourceRange(vStmt, "var s = new Y();", contents);
			Type type = vStmt.getType();
			IBinding binding = type.resolveBinding();
			assertTrue("null binding", binding != null);
			assertTrue("binding incorrect", binding.getName().equals("Y"));
			assertTrue("not a var", type.isVar());
			SimpleType simpleType = (SimpleType) type;
			Name name = simpleType.getName();
			SimpleName simpleName = (SimpleName) name;
			binding = simpleName.resolveBinding();
			assertTrue(binding instanceof ITypeBinding);
			ITypeBinding typeBinding = (ITypeBinding) binding;
			assertTrue("wrong type binding", typeBinding.getName().equals("Y"));
	}
	public void testBug532535_002() throws JavaModelException {
		String contents =
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"for (var x= 10; x < 20; x++) {\n" +
				"		// do nothing\n" +
				"	}\n" +
				"}\n" +
				"class Y {}";
			this.workingCopy = getWorkingCopy("/Converter10/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(contents, this.workingCopy, false);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			node = getASTNode((CompilationUnit)node, 0, 0);
			MethodDeclaration methodDeclaration = (MethodDeclaration) node;
			ForStatement forStmt = (ForStatement) methodDeclaration.getBody().statements().get(0);
			VariableDeclarationExpression expr = (VariableDeclarationExpression) forStmt.initializers().get(0);
			Type type = expr.getType();
			IBinding binding = type.resolveBinding();
			assertTrue("null binding", binding != null);
			assertTrue("binding incorrect", binding.getName().equals("int"));
			assertTrue("not a var", type.isVar());
			SimpleType simpleType = (SimpleType) type;
			Name name = simpleType.getName();
			SimpleName simpleName = (SimpleName) name;
			binding = simpleName.resolveBinding();
			assertTrue(binding instanceof ITypeBinding);
			ITypeBinding typeBinding = (ITypeBinding) binding;
			assertTrue("wrong type binding", typeBinding.getName().equals("int"));
	}
	@SuppressWarnings("deprecation")
	public void testBug525580_comment38() throws CoreException, IOException {
		// not really using Java 10, but apiLevel JLS10 as per bug report
		String jarPath = null;
		try {
			Hashtable<String, String> options = JavaCore.getDefaultOptions();
			options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
			options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
			options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);

			String srcFolderInWS = "/Converter10/src";
			createFolder(srcFolderInWS + "/cardManager/");

			String srcFilePathInWS = srcFolderInWS + "/cardManager/CardManagerFragment.java";
			createFile(srcFilePathInWS,
					"package cardManager;\n" +
					"\n" +
					"public class CardManagerFragment {\n" +
					"    private view.View i;\n" +
					"\n" +
					"    private <T> T a() {\n" +
					"        return this.i.findViewById(-1);\n" +
					"    }\n" +
					"}\n");

			jarPath = getWorkspacePath() + "Converter10/P.jar";
			createJar(new String[] {
				"view/View.java",
				"package view;\n" +
				"public class View {\n" +
				"	public final <T extends View> T findViewById(int i) { return null; }\n" +
				"}\n"
				},
				jarPath,
				options);

			ASTParser parser = ASTParser.newParser(AST_INTERNAL_JLS10);
			parser.setResolveBindings(true);
			parser.setStatementsRecovery(true);
			parser.setBindingsRecovery(true);
		    parser.setCompilerOptions(options);
			parser.setEnvironment(new String[] {jarPath}, new String[] {getWorkspacePath() + srcFolderInWS}, null, true);
			parser.setKind(ASTParser.K_COMPILATION_UNIT);

			class MyFileASTRequestor extends FileASTRequestor {
				boolean accepted = false;
				@SuppressWarnings("synthetic-access")
				@Override
				public void acceptAST(String sourceFilePath, CompilationUnit cu) {
					if (sourceFilePath.equals(getWorkspacePath() + srcFilePathInWS))
						this.accepted = true;
					assertEquals(1, cu.getProblems().length);
					IProblem problem = cu.getProblems()[0];
					assertEquals("Unexpected problem", "Pb(17) Type mismatch: cannot convert from View to T", problem.toString());
				}
			}
			MyFileASTRequestor requestor = new MyFileASTRequestor();
			parser.createASTs(new String[] {getWorkspacePath() + srcFilePathInWS}, null, new String[0], requestor, null);
			assertTrue("file should have been accepted", requestor.accepted);
		} finally {
			if (jarPath != null)
				deleteFile(jarPath);
		}
	}
// Add new tests here
}
