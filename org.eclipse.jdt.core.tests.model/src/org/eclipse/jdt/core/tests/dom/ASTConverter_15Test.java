/*******************************************************************************
 * Copyright (c) 2020, 2021 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.dom;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.List;
import junit.framework.Test;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.*;

@SuppressWarnings("rawtypes")
public class ASTConverter_15Test extends ConverterTestSetup {

	ICompilationUnit workingCopy;
	//private static final String jclLib = "CONVERTER_JCL18_LIB";

	@SuppressWarnings("deprecation")
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		this.ast = AST.newAST(getAST15(), false);
		this.currentProject = getJavaProject("Converter_15");
		if (this.ast.apiLevel() == AST.JLS15 ) {
			this.currentProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_17);
			this.currentProject.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_17);
			this.currentProject.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_17);

		}
	}

	public ASTConverter_15Test(String name) {
		super(name);
	}

	static {
//		TESTS_NUMBERS = new int[] { 19 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] {"test0001"};
	}
	public static Test suite() {
		return buildModelTestSuite(ASTConverter_15Test.class);
	}

	@SuppressWarnings("deprecation")
	static int getAST15() {
		return AST.JLS15;
	}
	protected void tearDown() throws Exception {
		super.tearDown();
		if (this.workingCopy != null) {
			this.workingCopy.discardWorkingCopy();
			this.workingCopy = null;
		}
	}

	public void _testRecord001() throws CoreException {
		if (!isJRE15) {
			System.err.println("Test "+getName()+" requires a JRE 15");
			return;
		}
		String contents =
			"public record X() {\n" +
			"		public X {\n" +
			"			System.out.println(\"no error\");\n" +
			"		}\n" +
			"\n" +
			"}\n";
		this.workingCopy = getWorkingCopy("/Converter_15/src/X.java", true/*resolve*/);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			javaProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
			ASTNode node = buildAST(
				contents,
				this.workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertProblemsSize(compilationUnit, 0);
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}

	/**
	 * Added for Bug 561193 - [14]record keyword inside method not colored correctly
	 */
	public void _testRecord002() throws CoreException {
		if (!isJRE15) {
			System.err.println("Test "+getName()+" requires a JRE 15");
			return;
		}
		String contents =
			"public record X(int param1, int param2) {\n" +
			"		public X {\n" +
			"			if (param1 > 5) {\n" +
			"				System.out.println(\"error\");\n" +
			"			}\n" +
			"		}\n" +
			"\n" +
			"}\n";
		this.workingCopy = getWorkingCopy("/Converter_15/src/X.java", true/*resolve*/);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			javaProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
			ASTNode node = buildAST(
				contents,
				this.workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertProblemsSize(compilationUnit, 0);
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}

	public void _testRecord003() throws CoreException {
		if (!isJRE15) {
			System.err.println("Test "+getName()+" requires a JRE 15");
			return;
		}
		String contents =
			"public record X(int param1, int param2) {\n" +
			"		public X {\n" +
			"			if (param1 > 5) {\n" +
			"				System.out.println(\"error\");\n" +
			"			}\n" +
			"		}\n" +
			"\n" +
			"		public X(int a) {\n" +
			"			this(6,16);\n" +
			"			System.out.println(a);\n" +
			"		}\n" +
			"}\n";
		this.workingCopy = getWorkingCopy("/Converter_15/src/X.java", true/*resolve*/);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			javaProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
			ASTNode node = buildAST(
				contents,
				this.workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertProblemsSize(compilationUnit, 0);
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}

	public void _testRecord004() throws CoreException {
		if (!isJRE15) {
			System.err.println("Test " + getName() + " requires a JRE 15");
			return;
		}
		String contents = "public class X {\n" +
						  "	public static void main(String[] args) {\n" +
				          "		record R(int x,int y){}\n" +
						  "		R r = new R(100, 200);\n" +
				          "		System.out.println(r.x());\n" +
						  "	}\n" +
				          "}";
		this.workingCopy = getWorkingCopy("/Converter_15/src/X.java", true/* resolve */);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			javaProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
			ASTNode node = buildAST(contents, this.workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertProblemsSize(compilationUnit, 0);
			node = getASTNode(compilationUnit, 0, 0);
			assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
			MethodDeclaration methodDeclaration = (MethodDeclaration) node;
			List<ASTNode> statements = methodDeclaration.getBody().statements();
			node = statements.get(0);
			assertEquals("Not a TypDeclaration statement", ASTNode.TYPE_DECLARATION_STATEMENT, node.getNodeType());
			TypeDeclarationStatement tdStmt = (TypeDeclarationStatement) node;
			node = tdStmt.getDeclaration();
			assertEquals("Not a RecordDeclaration", ASTNode.RECORD_DECLARATION, node.getNodeType());
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}

	public void _testRecord005() throws CoreException {
		if (!isJRE15) {
			System.err.println("Test "+getName()+" requires a JRE 15");
			return;
		}
		String contents = "public class X {\n" +
				  "	public static void main(String[] args) {\n" +
		          "		record R(int x,String y){}\n" +
				  "		R r = new R(100, \"Point\");\n" +
		          "		System.out.println(r.x());\n" +
				  "	}\n" +
		          "}";
		this.workingCopy = getWorkingCopy("/Converter_15/src/X.java", true/*resolve*/);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			javaProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
			ASTNode node = buildAST(
				contents,
				this.workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertProblemsSize(compilationUnit, 0);
			node = getASTNode(compilationUnit, 0, 0);
			assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
			MethodDeclaration methodDeclaration = (MethodDeclaration) node;
			List<ASTNode> statements = methodDeclaration.getBody().statements();
			node = statements.get(0);
			assertEquals("Not a TypDeclaration statement", ASTNode.TYPE_DECLARATION_STATEMENT, node.getNodeType());
			TypeDeclarationStatement tdStmt = (TypeDeclarationStatement) node;
			node = tdStmt.getDeclaration();
			assertEquals("Not a RecordDeclaration", ASTNode.RECORD_DECLARATION, node.getNodeType());
			RecordDeclaration record = (RecordDeclaration)node;
			List<SingleVariableDeclaration> recordComponents = record.recordComponents();
			assertEquals("There should be 2 record components", 2, recordComponents.size());
			SingleVariableDeclaration recordComponent = recordComponents.get(0);
			assertEquals("First record component name should be x","x" , recordComponent.getName().toString());
			assertEquals("First record component type is int" , "int", recordComponent.getType().toString());
			IVariableBinding resolveBinding = recordComponent.resolveBinding();
			assertEquals("First record component binding" , true, resolveBinding.isRecordComponent());
			recordComponent = recordComponents.get(1);
			assertEquals("Second record component name should be y","y" , recordComponent.getName().toString());
			assertEquals("Second record component type is String" , "String", recordComponent.getType().toString());
			resolveBinding = recordComponent.resolveBinding();
			assertEquals("Second record component binding" , true, resolveBinding.isRecordComponent());
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}

	public void _testRecord006() throws CoreException {
		if (!isJRE15) {
			System.err.println("Test "+getName()+" requires a JRE 15");
			return;
		}
		String contents = "import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"record X(@MyAnnot int lo) {\n" +
				"	public int lo() {\n" +
				"		return this.lo;\n" +
				"	}\n" +
				"\n" +
				"}\n" +
				"@Target({ElementType.FIELD})\n" +
				"@interface MyAnnot {}";
		this.workingCopy = getWorkingCopy("/Converter_15/src/X.java", true/*resolve*/);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			javaProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
			ASTNode node = buildAST(
				contents,
				this.workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertProblemsSize(compilationUnit, 0);
			node = ((AbstractTypeDeclaration)compilationUnit.types().get(0));
			assertEquals("Not a Record Declaration", ASTNode.RECORD_DECLARATION, node.getNodeType());
			RecordDeclaration record = (RecordDeclaration)node;
			List<SingleVariableDeclaration> recordComponents = record.recordComponents();
			assertEquals("There should be 1 record component", 1, recordComponents.size());
			SingleVariableDeclaration recordComponent = recordComponents.get(0);
			assertEquals("Record component name should be lo","lo" , recordComponent.getName().toString());
			assertEquals("Record component type is int" , "int", recordComponent.getType().toString());
			IVariableBinding resolveBinding = recordComponent.resolveBinding();
			assertEquals("Record component binding" , true, resolveBinding.isRecordComponent());
			MarkerAnnotation annotation = (MarkerAnnotation)recordComponent.modifiers().get(0);
			assertEquals("Record component annotation name should be MyAnnot","@MyAnnot" , annotation.toString());
			assertEquals("Record component binding should not have annotation",0 , resolveBinding.getAnnotations().length);


		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}

	public void _testRecord007() throws CoreException {
		if (!isJRE15) {
			System.err.println("Test "+getName()+" requires a JRE 15");
			return;
		}
		String contents = "record X(int lo) {\n" +
				"	public int lo() {\n" +
				"		return this.lo;\n" +
				"	}\n" +
				"\n" +
				"}\n";
		this.workingCopy = getWorkingCopy("/Converter_15/src/X.java", true/*resolve*/);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			javaProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
			ASTNode node = buildAST(
				contents,
				this.workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertProblemsSize(compilationUnit, 0);
			node = ((AbstractTypeDeclaration)compilationUnit.types().get(0));
			assertEquals("Not a Record Declaration", ASTNode.RECORD_DECLARATION, node.getNodeType());
			RecordDeclaration record = (RecordDeclaration)node;
			List<SingleVariableDeclaration> recordComponents = record.recordComponents();
			assertEquals("There should be 1 record component", 1, recordComponents.size());
			SingleVariableDeclaration recordComponent = recordComponents.get(0);
			SimpleName recordComponentName = recordComponent.getName();
			assertEquals("Record component name should be lo","lo" , recordComponentName.toString());
			ITypeBinding resolveTypeBinding = recordComponentName.resolveTypeBinding();
			assertEquals("Record component type is int" , "int",resolveTypeBinding.getName() );
			IVariableBinding resolveBinding = (IVariableBinding)recordComponentName.resolveBinding();
			assertEquals("Record component binding" , true, resolveBinding.isRecordComponent());


		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}

	public void _testRecord008() throws CoreException {
		if (!isJRE15) {
			System.err.println("Test "+getName()+" requires a JRE 15");
			return;
		}
		String contents = "import java.lang.annotation.*;\n" +
				"@Target (ElementType.METHOD)\n" +
				"@interface MyAnnot {}\n" +
				"public record X(@MyAnnot int lo) {\n" +
				"	public int lo() {\n" +
				"		return this.lo;\n" +
				"	}\n" +
				"\n" +
				"}\n";

		this.workingCopy = getWorkingCopy("/Converter_15/src/X.java", true/*resolve*/);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			javaProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
			ASTNode node = buildAST(
				contents,
				this.workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertProblemsSize(compilationUnit, 0);
			node = ((AbstractTypeDeclaration)compilationUnit.types().get(0));
			assertNotEquals("Not a Record Declaration", ASTNode.RECORD_DECLARATION, node.getNodeType());



		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}

	public void _testRecord009() throws CoreException {
		if (!isJRE15) {
			System.err.println("Test "+getName()+" requires a JRE 15");
			return;
		}
		String elementType = "package java.lang.annotation;\n" +
				"public enum ElementType {\n" +
				"    TYPE,\n" +
				"    FIELD,\n" +
				"    METHOD,\n" +
				"    PARAMETER,\n" +
				"    CONSTRUCTOR,\n" +
				"    LOCAL_VARIABLE,\n" +
				"    ANNOTATION_TYPE,\n" +
				"    PACKAGE,\n" +
				"    TYPE_PARAMETER,\n" +
				"    TYPE_USE,\n" +
				"    MODULE,\n" +
				"    RECORD_COMPONENT\n" +
				"}\n";
		ICompilationUnit workingCopy2 = getWorkingCopy("/Converter_15/src/java/lang/annotation/ElementType.java", true/*resolve*/);
		String contents = "import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"record X(@MyAnnot int lo) {\n" +
				"	public int lo() {\n" +
				"		return this.lo;\n" +
				"	}\n" +
				"\n" +
				"}\n" +
				"@Target({ElementType.RECORD_COMPONENT})\n" +
				"@interface MyAnnot {}";
		this.workingCopy = getWorkingCopy("/Converter_15/src/X.java", true/*resolve*/);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			javaProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
			buildAST(
					elementType,
					workingCopy2);
			ASTNode node = buildAST(
				contents,
				this.workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertProblemsSize(compilationUnit, 0);
			node = ((AbstractTypeDeclaration)compilationUnit.types().get(0));
			assertEquals("Not a Record Declaration", ASTNode.RECORD_DECLARATION, node.getNodeType());
			RecordDeclaration record = (RecordDeclaration)node;
			List<SingleVariableDeclaration> recordComponents = record.recordComponents();
			assertEquals("There should be 1 record component", 1, recordComponents.size());
			SingleVariableDeclaration recordComponent = recordComponents.get(0);
			SimpleName recordComponentName = recordComponent.getName();
			assertEquals("Record component name should be lo","lo" , recordComponentName.toString());
			ITypeBinding resolveTypeBinding = recordComponentName.resolveTypeBinding();
			assertEquals("Record component type is int" , "int",resolveTypeBinding.getName() );
			IVariableBinding resolveBinding = (IVariableBinding)recordComponentName.resolveBinding();
			assertEquals("Record component binding" , true, resolveBinding.isRecordComponent());

		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}

	public void _testRecord010() throws CoreException {
		if (!isJRE15) {
			System.err.println("Test "+getName()+" requires a JRE 15");
			return;
		}
		String elementType = "package java.lang.annotation;\n" +
				"public enum ElementType {\n" +
				"    TYPE,\n" +
				"    FIELD,\n" +
				"    METHOD,\n" +
				"    PARAMETER,\n" +
				"    CONSTRUCTOR,\n" +
				"    LOCAL_VARIABLE,\n" +
				"    ANNOTATION_TYPE,\n" +
				"    PACKAGE,\n" +
				"    TYPE_PARAMETER,\n" +
				"    TYPE_USE,\n" +
				"    MODULE,\n" +
				"    RECORD_COMPONENT\n" +
				"}\n";
		ICompilationUnit workingCopy2 = getWorkingCopy("/Converter_15/src/java/lang/annotation/ElementType.java", true/*resolve*/);
		String contents = "import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"record X(@MyAnnot int lo) {\n" +
				"	public static @MyAnnot int x;\n" +
				"	public int lo() {\n" +
				"		return this.lo;\n" +
				"	}\n" +
				"\n" +
				"}\n" +
				"@Target({ElementType.RECORD_COMPONENT})\n" +
				"@interface MyAnnot {}";
		this.workingCopy = getWorkingCopy("/Converter_15/src/X.java", true/*resolve*/);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			javaProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
			buildAST(
					elementType,
					workingCopy2);
			try {
				buildAST(
				contents,
				this.workingCopy);
			} catch (Exception ex) {
				// This can not be compiled
			}

		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}

	public void _testRecord011() throws CoreException {
		if (!isJRE15) {
			System.err.println("Test "+getName()+" requires a JRE 15");
			return;
		}
		String contents =
			"public record X() {\n" +
			"		public X {\n" +
			"			System.out.println(\"no error\");\n" +
			"		}\n" +
			"\n" +
			"}\n";
		this.workingCopy = getWorkingCopy("/Converter_15/src/X.java", true/*resolve*/);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			javaProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
			ASTNode node = buildAST(
				contents,
				this.workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertProblemsSize(compilationUnit, 0);
			List<AbstractTypeDeclaration> types = compilationUnit.types();
			assertEquals("No. of Types is not 1", types.size(), 1);
			AbstractTypeDeclaration type = types.get(0);
			assertTrue("type not a Record", type instanceof RecordDeclaration);
			RecordDeclaration recDecl = (RecordDeclaration)type;
			int startPos = recDecl.getRestrictedIdentifierStartPosition();
			assertEquals("Start position of 'record' keyword is not 7", startPos, 7);
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}

	public void _testRecord012() throws CoreException {
		if (!isJRE15) {
			System.err.println("Test "+getName()+" requires a JRE 15");
			return;
		}
		String contents =
			"public record X(int myComp) {\n" +
			"		public void foo() {\n" +
			"			System.out.println(\"no error\");\n" +
			"		}\n" +
			"\n" +
			"}\n";
		this.workingCopy = getWorkingCopy("/Converter_15/src/X.java", true/*resolve*/);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			javaProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
			ASTNode node = buildAST(
				contents,
				this.workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertProblemsSize(compilationUnit, 0);
			List<AbstractTypeDeclaration> types = compilationUnit.types();
			assertEquals("No. of Types is not 1", types.size(), 1);
			AbstractTypeDeclaration type = types.get(0);
			assertTrue("type not a Record", type instanceof RecordDeclaration);
			RecordDeclaration recDecl = (RecordDeclaration)type;
			MethodDeclaration[] methods = recDecl.getMethods();
			assertEquals("No. of methods is not 1", methods.length, 1);
			ITypeBinding typeBinding = type.resolveBinding();
			assertNotNull("typeBinding is null", typeBinding);
			IMethodBinding[] mBindings = typeBinding.getDeclaredMethods();
			assertEquals("No. of declared methods is not 6", mBindings.length, 6);
			for (IMethodBinding mBinding : mBindings) {
				if (mBinding.getName().equals("X") || mBinding.getName().equals("foo")) {
					assertFalse("foo is not a synthetic method", mBinding.isSyntheticRecordMethod());
					if (mBinding.getName().equals("X")) {
						assertArrayEquals(mBinding.getParameterNames(), new String[] { "myComp" });
					}
				} else {
					assertTrue("expected a synthetic method", mBinding.isSyntheticRecordMethod());
				}
			}

		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}

	public void testClass001() throws CoreException {
		if (!isJRE15) {
			System.err.println("Test "+getName()+" requires a JRE 15");
			return;
		}
		String contents =
			"public class X {\n" +
			"		public X() {\n" +
			"			System.out.println(\"no error\");\n" +
			"		}\n" +
			"\n" +
			"}\n";
		this.workingCopy = getWorkingCopy("/Converter_15/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
				contents,
				this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		List<AbstractTypeDeclaration> types = compilationUnit.types();
		assertEquals("No. of Types is not 1", types.size(), 1);
		AbstractTypeDeclaration type = types.get(0);
		assertTrue("type not a type", type instanceof TypeDeclaration);
		TypeDeclaration typeDecl = (TypeDeclaration)type;
		assertTrue("type not a class", !typeDecl.isInterface());
		int startPos = typeDecl.getRestrictedIdentifierStartPosition();
		assertEquals("Restricter identifier position for class' is not -1", startPos, -1);
	}

	public void testInterface001() throws CoreException {
		if (!isJRE15) {
			System.err.println("Test "+getName()+" requires a JRE 15");
			return;
		}
		String contents =
			"public interface X {\n" +
			"\n" +
			"}\n";
		this.workingCopy = getWorkingCopy("/Converter_15/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
				contents,
				this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		List<AbstractTypeDeclaration> types = compilationUnit.types();
		assertEquals("No. of Types is not 1", types.size(), 1);
		AbstractTypeDeclaration type = types.get(0);
		assertTrue("type not a type", type instanceof TypeDeclaration);
		TypeDeclaration typeDecl = (TypeDeclaration)type;
		assertTrue("type not an interface", typeDecl.isInterface());
		int startPos = typeDecl.getRestrictedIdentifierStartPosition();
		assertEquals("Restricter identifier position for interface' is not -1", startPos, -1);
	}

	public void testTextBlock001() throws JavaModelException {
		if (!isJRE15) {
			System.err.println("Test "+getName()+" requires a JRE 15");
			return;
		}
		String contents =
				"public class X {\n" +
						"	public String test001() {\n" +
						"		String s = \"\"\"\n" +
						"       <html>\n" +
						"        <body>\n" +
						"            <p>Hello, world</p>\n" +
						"        </body>\n" +
						"       </html>\n" +
						"   \"\"\";\n" +
						"    	System.out.println(s);" +
						"		return s;\n" +
						"	}" +
						"}" ;
		this.workingCopy = getWorkingCopy("/Converter_15/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
				contents,
				this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 0, 0);
		assertEquals("Text block statement", node.getNodeType(), ASTNode.VARIABLE_DECLARATION_STATEMENT);
		List fragments = ((VariableDeclarationStatement) node).fragments();
		assertEquals("Incorrect no of fragments", 1, fragments.size());
		node = (ASTNode) fragments.get(0);
		assertEquals("Switch statement", node.getNodeType(), ASTNode.VARIABLE_DECLARATION_FRAGMENT);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) node;
		Expression initializer = fragment.getInitializer();
		assertTrue("Initializer is not a TextBlock", initializer instanceof TextBlock);
			String escapedValue = ((TextBlock) initializer).getEscapedValue();

			assertTrue("String should not be empty", escapedValue.length() != 0);
		assertTrue("String should start with \"\"\"", escapedValue.startsWith("\"\"\""));

		String literal = ((TextBlock) initializer).getLiteralValue();
		assertEquals("literal value not correct",
				"    <html>\n" +
				"     <body>\n" +
				"         <p>Hello, world</p>\n" +
				"     </body>\n" +
				"    </html>\n",
				literal);

	}

	public void testTextBlock003() throws JavaModelException {
		if (!isJRE15) {
			System.err.println("Test "+getName()+" requires a JRE 15");
			return;
		}
		String contents =
				"public class X {\n" +
						"	public String test001() {\n" +
						"		String s = \"\"\"\n" +
						"      	<html>\n" +
						"        <body>\n" +
						"            <p>Hello, world</p>\n" +
						"        </body>\n" +
						"      	</html>\n" +
						"\"\"\";\n" +
						"    	System.out.println(s);" +
						"		return s;\n" +
						"	}" +
						"}" ;
		this.workingCopy = getWorkingCopy("/Converter_15/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
				contents,
				this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 0, 0);
		assertEquals("Text block statement", node.getNodeType(), ASTNode.VARIABLE_DECLARATION_STATEMENT);
		List fragments = ((VariableDeclarationStatement) node).fragments();
		assertEquals("Incorrect no of fragments", 1, fragments.size());
		node = (ASTNode) fragments.get(0);
		assertEquals("Switch statement", node.getNodeType(), ASTNode.VARIABLE_DECLARATION_FRAGMENT);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) node;
		Expression initializer = fragment.getInitializer();
		assertTrue("Initializer is not a TextBlock", initializer instanceof TextBlock);
		ITypeBinding binding = initializer.resolveTypeBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong qualified name", "java.lang.String", binding.getQualifiedName());

			String escapedValue = ((TextBlock) initializer).getEscapedValue();

			assertTrue("String should not be empty", escapedValue.length() != 0);
		assertTrue("String should start with \"\"\"", escapedValue.startsWith("\"\"\""));
		assertEquals("escaped value not correct",
				"\"\"\"\n" +
				"      	<html>\n" +
				"        <body>\n" +
				"            <p>Hello, world</p>\n" +
				"        </body>\n" +
				"      	</html>\n" +
				"\"\"\"",
				escapedValue);

		String literal = ((TextBlock) initializer).getLiteralValue();
		assertEquals("literal value not correct",
				"      	<html>\n" +
				"        <body>\n" +
				"            <p>Hello, world</p>\n" +
				"        </body>\n" +
				"      	</html>\n" +
				"",
				literal);
	}

	public void testTextBlock004() throws JavaModelException {
		if (!isJRE15) {
			System.err.println("Test "+getName()+" requires a JRE 15");
			return;
		}
		String contents =
				"public class test14 {\n" +
				"	public static void main(String[] args) {\n" +
				"		String s = \"\"\"\n" +
				"				nadknaks vgvh \n" +
				"				\"\"\";\n" +
				"\n" +
				"		int m = 10;\n" +
				"		m = m* 6;\n" +
				"		System.out.println(s);\n" +
				"	}\n" +
				"}" ;
		this.workingCopy = getWorkingCopy("/Converter_15/src/test14.java", true/*resolve*/);
		ASTNode node = buildAST(
				contents,
				this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 0, 0);
		assertEquals("wrong line number", 3, compilationUnit.getLineNumber(node.getStartPosition()));
		node = getASTNode(compilationUnit, 0, 0, 1);
		assertEquals("wrong line number", 7, compilationUnit.getLineNumber(node.getStartPosition()));
		node = getASTNode(compilationUnit, 0, 0, 2);
		assertEquals("wrong line number", 8, compilationUnit.getLineNumber(node.getStartPosition()));
		node = getASTNode(compilationUnit, 0, 0, 3);
		assertEquals("wrong line number", 9, compilationUnit.getLineNumber(node.getStartPosition()));
	}

	@SuppressWarnings("deprecation")
	public void _testPatternInstanceOfExpression001() throws JavaModelException {
		if (!isJRE15) {
			System.err.println("Test "+getName()+" requires a JRE 15");
			return;
		}
		String contents =
				"public class X {\n" +
						"	public String test001(Object o) {\n" +
						"		if (o instanceof String s){\n" +
						"    		System.out.println(s);\n" +
						"			return s;\n" +
						"		}\n" +
						"		return null;\n" +
						"	}\n" +
						"}" ;
		this.workingCopy = getWorkingCopy("/Converter_15/src/X.java", true/*resolve*/);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
				javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
				javaProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);

				ASTNode node = buildAST(
						contents,
						this.workingCopy);
				assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
				CompilationUnit compilationUnit = (CompilationUnit) node;
				assertProblemsSize(compilationUnit, 0);
				node = getASTNode(compilationUnit, 0, 0, 0);
				assertEquals("Not an if statement", ASTNode.IF_STATEMENT, node.getNodeType());
				IfStatement ifStatement = (IfStatement) node;
				Expression expression = ifStatement.getExpression();
				checkSourceRange(expression, "o instanceof String s", contents);
				assertEquals("Not an instanceof expression", ASTNode.INSTANCEOF_EXPRESSION, expression.getNodeType());
				PatternInstanceofExpression instanceofExpression = (PatternInstanceofExpression) expression;
				SingleVariableDeclaration var = instanceofExpression.getRightOperand();
				checkSourceRange(var, "String s", contents);
			}finally {
				javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
			}
	}

	@SuppressWarnings("deprecation")
	public void _testPatternInstanceOfExpression002() throws JavaModelException {
		if (!isJRE15) {
			System.err.println("Test "+getName()+" requires a JRE 15");
			return;
		}
		String contents =
				"public class X {\n" +
						"	public String test001(Object o) {\n" +
						"		if (o instanceof String){\n" +
						"    		String s = (String)o;\n" +
						"    		System.out.println(s);\n" +
						"			return s;\n" +
						"		}\n" +
						"		return null;\n" +
						"	}\n" +
						"}" ;
		this.workingCopy = getWorkingCopy("/Converter_15/src/X.java", true/*resolve*/);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
				javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
				javaProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);

				ASTNode node = buildAST(
						contents,
						this.workingCopy);
				assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
				CompilationUnit compilationUnit = (CompilationUnit) node;
				assertProblemsSize(compilationUnit, 0);
				node = getASTNode(compilationUnit, 0, 0, 0);
				assertEquals("Not an if statement", ASTNode.IF_STATEMENT, node.getNodeType());
				IfStatement ifStatement = (IfStatement) node;
				Expression expression = ifStatement.getExpression();
				checkSourceRange(expression, "o instanceof String", contents);
				assertEquals("Not an instanceof expression", ASTNode.INSTANCEOF_EXPRESSION, expression.getNodeType());
				PatternInstanceofExpression instanceofExpression = (PatternInstanceofExpression) expression;
				SingleVariableDeclaration var = instanceofExpression.getRightOperand();
				assertNull(var);
			}finally {
				javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
			}
	}

	@SuppressWarnings("deprecation")
	public void _testPatternInstanceOfExpression003() throws JavaModelException {
		if (!isJRE15) {
			System.err.println("Test "+getName()+" requires a JRE 15");
			return;
		}
		String contents =
				"public class X {\n" +
						"	public String test001(Object o) {\n" +
						"		if (o instanceof String s){\n" +
						"    		System.out.println(s);\n" +
						"			return s;\n" +
						"		}\n" +
						"		return null;\n" +
						"	}\n" +
						"}" ;
		this.workingCopy = getWorkingCopy("/Converter_15/src/X.java", true/*resolve*/);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
				javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
				javaProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);

				ASTNode node = buildAST(
						contents,
						this.workingCopy);
				assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
				CompilationUnit compilationUnit = (CompilationUnit) node;
				assertProblemsSize(compilationUnit, 0);
				node = getASTNode(compilationUnit, 0, 0, 0);
				assertEquals("Not an if statement", ASTNode.IF_STATEMENT, node.getNodeType());
				IfStatement ifStatement = (IfStatement) node;
				Expression expression = ifStatement.getExpression();
				checkSourceRange(expression, "o instanceof String s", contents);
				assertEquals("Not an instanceof expression", ASTNode.INSTANCEOF_EXPRESSION, expression.getNodeType());
				PatternInstanceofExpression instanceofExpression = (PatternInstanceofExpression) expression;
				SingleVariableDeclaration var = instanceofExpression.getRightOperand();
				checkSourceRange(var, "String s", contents);
				String instanceofExpressionString = instanceofExpression.toString();
				assertEquals("o instanceof String s", instanceofExpressionString);
			}finally {
				javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
			}
	}

	// Move sealed test to AST 17 converter class
	public void _testSealed001() throws CoreException {
		if (!isJRE15) {
			System.err.println("Test "+getName()+" requires a JRE 15");
			return;
		}
		String contents = "public sealed class X permits X1{\n" +
				"\n" +
				"}\n" +
				"non-sealed class X1 extends X {\n" +
				"\n" +
				"}\n";
		this.workingCopy = getWorkingCopy("/Converter_15/src/X.java", true/*resolve*/);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			javaProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
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

		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}

	// Move sealed test to AST 17 converter class
	public void _testSealed002() throws CoreException {
		if (!isJRE15) {
			System.err.println("Test "+getName()+" requires a JRE 15");
			return;
		}
		String contents = "public sealed interface X permits X1{\n" +
				"\n" +
				"}\n" +
				"non-sealed interface X1 extends X {\n" +
				"\n" +
				"}\n";
		this.workingCopy = getWorkingCopy("/Converter_15/src/X.java", true/*resolve*/);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			javaProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
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

		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}

	// Move sealed test to AST 17 converter class
	public void _testSealed003() throws CoreException {
		if (!isJRE15) {
			System.err.println("Test "+getName()+" requires a JRE 15");
			return;
		}
		String contents = "public sealed interface X permits X1{\n" +
				"\n" +
				"}\n" +
				"non-sealed interface X1 extends X {\n" +
				"\n" +
				"}\n";
		this.workingCopy = getWorkingCopy("/Converter_15/src/X.java", true/*resolve*/);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			javaProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
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
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}

	// Move sealed test to AST 17 converter class
	public void _testSealed004() throws CoreException {
		if (!isJRE15) {
			System.err.println("Test "+getName()+" requires a JRE 15");
			return;
		}
		String contents = "public sealed class X permits X1{\n" +
				"\n" +
				"}\n" +
				"non-sealed class X1 extends X {\n" +
				"\n" +
				"}\n";
		this.workingCopy = getWorkingCopy("/Converter_15/src/X.java", true/*resolve*/);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			javaProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
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
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}

	public void _testRecordConstructor001() throws CoreException {
		if (!isJRE15) {
			System.err.println("Test "+getName()+" requires a JRE 15");
			return;
		}
		String contents = "record X(int lo) {\n" +
				"   public X {\n" +
				"   \n}\n" +
				"   public X(String str) {\n" +
				"		this((str != null) ? str.length() : 0);" +
				"   \n}\n" +
				"	public int abc() {\n" +
				"		return this.lo;\n" +
				"	}\n" +
				"\n" +
				"}\n";
		this.workingCopy = getWorkingCopy("/Converter_15/src/X.java", true/*resolve*/);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			javaProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
			ASTNode node = buildAST(
				contents,
				this.workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertProblemsSize(compilationUnit, 0);
			node = ((AbstractTypeDeclaration)compilationUnit.types().get(0));
			assertEquals("Not a Type", ASTNode.RECORD_DECLARATION, node.getNodeType());
			ASTParser parser= ASTParser.newParser(getAST15());
			parser.setProject(javaProject);
			IBinding[] bindings = parser.createBindings(new IJavaElement[] { this.workingCopy.findPrimaryType() }, null);
			IMethodBinding methodBinding= ((ITypeBinding) bindings[0]).getDeclaredMethods()[0];
			assertEquals("compact constructor name", "X", methodBinding.getName());
			assertTrue("not a Constructor", methodBinding.isConstructor());
			assertTrue("not a CompactConstructor", methodBinding.isCompactConstructor());
			assertTrue("not a CanonicalConstructor", methodBinding.isCanonicalConstructor());
			methodBinding= ((ITypeBinding) bindings[0]).getDeclaredMethods()[1];
			assertEquals("constructor name", "X", methodBinding.getName());
			assertTrue("not a Constructor", methodBinding.isConstructor());
			assertFalse("Is CompactConstructor?", methodBinding.isCompactConstructor());
			assertFalse("Is CanonicalConstructor?", methodBinding.isCanonicalConstructor());
			methodBinding= ((ITypeBinding) bindings[0]).getDeclaredMethods()[2];
			assertEquals("method name", "abc", methodBinding.getName());
			assertFalse("Is a Constructor?", methodBinding.isConstructor());
			assertFalse("Is a CompactConstructor?", methodBinding.isCompactConstructor());
			assertFalse("Is CanonicalConstructor?", methodBinding.isCanonicalConstructor());
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}

	public void _testRecordConstructor002() throws CoreException {
		if (!isJRE15) {
			System.err.println("Test "+getName()+" requires a JRE 15");
			return;
		}
		String contents = "record X(int lo) {\n" +
				"   public X(int lo) {\n" +
				"		this.lo = lo;" +
				"   \n}\n" +
				"   public X(String str) {\n" +
				"		this((str != null) ? str.length() : 0);" +
				"   \n}\n" +
				"	public int abc() {\n" +
				"		return this.lo;\n" +
				"	}\n" +
				"\n" +
				"}\n";
		this.workingCopy = getWorkingCopy("/Converter_15/src/X.java", true/*resolve*/);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			javaProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
			ASTNode node = buildAST(
				contents,
				this.workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertProblemsSize(compilationUnit, 0);
			node = ((AbstractTypeDeclaration)compilationUnit.types().get(0));
			assertEquals("Not a Type", ASTNode.RECORD_DECLARATION, node.getNodeType());
			ASTParser parser= ASTParser.newParser(getAST15());
			parser.setProject(javaProject);
			IBinding[] bindings = parser.createBindings(new IJavaElement[] { this.workingCopy.findPrimaryType() }, null);
			IMethodBinding methodBinding= ((ITypeBinding) bindings[0]).getDeclaredMethods()[0];
			assertEquals("compact constructor name", "X", methodBinding.getName());
			assertTrue("not a Constructor", methodBinding.isConstructor());
			assertTrue("is a CanonicalConstructor", methodBinding.isCanonicalConstructor());
			assertFalse("is a CompactConstructor", methodBinding.isCompactConstructor());
			methodBinding= ((ITypeBinding) bindings[0]).getDeclaredMethods()[1];
			assertEquals("constructor name", "X", methodBinding.getName());
			assertTrue("not a Constructor", methodBinding.isConstructor());
			assertFalse("Is CanonicalConstructor?", methodBinding.isCanonicalConstructor());
			assertFalse("Is CompactConstructor?", methodBinding.isCompactConstructor());
			methodBinding= ((ITypeBinding) bindings[0]).getDeclaredMethods()[2];
			assertEquals("method name", "abc", methodBinding.getName());
			assertFalse("Is a Constructor?", methodBinding.isConstructor());
			assertFalse("Is a CanonicalConstructor?", methodBinding.isCompactConstructor());
			assertFalse("Is a CompactConstructor?", methodBinding.isCompactConstructor());
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	public void testBug571461() throws JavaModelException {
		if (!isJRE15) {
			System.err.println("Test "+getName()+" requires a JRE 15");
			return;
		}
		String contents =
						"public class X {\n"
						+ "    String example = \"\"\"\n"
						+ "            Example text\"\"\";\n"
						+ "    final String expected = \"Example text\";\n"
						+ "}" ;
		this.workingCopy = getWorkingCopy("/Converter_15/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
				contents,
				this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 0, 0);
		assertEquals("Text block statement", node.getNodeType(), ASTNode.FIELD_DECLARATION);
		FieldDeclaration field = (FieldDeclaration) node;
		List fragments = field.fragments();
		assertEquals("Incorrect no of fragments", 1, fragments.size());
		node = (ASTNode) fragments.get(0);
		assertEquals("Switch statement", node.getNodeType(), ASTNode.VARIABLE_DECLARATION_FRAGMENT);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) node;
		Expression initializer = fragment.getInitializer();
		assertTrue("Initializer is not a TextBlock", initializer instanceof TextBlock);
		String escapedValue = ((TextBlock) initializer).getEscapedValue();

		assertTrue("String should not be empty", escapedValue.length() != 0);
		assertTrue("String should start with \"\"\"", escapedValue.startsWith("\"\"\""));

		String literal = ((TextBlock) initializer).getLiteralValue();
		assertEquals("literal value not correct",
				"Example text",
				literal);
	}
}
