/*******************************************************************************
 * Copyright (c) 2020 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.dom;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.RecordDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TextBlock;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import junit.framework.Test;

@SuppressWarnings("rawtypes")
public class ASTConverter_15Test extends ConverterTestSetup {

	ICompilationUnit workingCopy;
	//private static final String jclLib = "CONVERTER_JCL15_LIB";

	public void setUpSuite() throws Exception {
		super.setUpSuite();
		this.ast = AST.newAST(getAST15(), false);
		if (this.ast.apiLevel() == AST.JLS15 ) {
			this.currentProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_15);
			this.currentProject.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_15);
			this.currentProject.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_15);
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

	public void testRecord001() throws CoreException {
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
	 * @throws CoreException
	 */
	public void testRecord002() throws CoreException {
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

	public void testRecord003() throws CoreException {
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

	public void testRecord004() throws CoreException {
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

	public void testRecord005() throws CoreException {
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

	public void testRecord006() throws CoreException {
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

	public void testRecord007() throws CoreException {
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

	public void _testRecord009() throws CoreException {
		if (!isJRE15) {
			System.err.println("Test "+getName()+" requires a JRE 15");
			return;
		}
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
			ASTNode node = buildAST(
				contents,
				this.workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertProblemsSize(compilationUnit, 0);
			node = ((AbstractTypeDeclaration)compilationUnit.types().get(0));
			assertEquals("Not a Record Declaration", ASTNode.RECORD_DECLARATION, node.getNodeType());
			//RecordDeclaration record = (RecordDeclaration)node;
			// test for error

		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
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
						"      	<html>\n" +
						"        <body>\n" +
						"            <p>Hello, world</p>\n" +
						"        </body>\n" +
						"    	</html>\n" +
						"    	\"\"\";\n" +
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
				"      	<html>\n" +
				"        <body>\n" +
				"            <p>Hello, world</p>\n" +
				"        </body>\n" +
				"    	</html>\n" +
				"    	",
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
						"    	</html>\n" +
						"    	\"\"\";\n" +
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
				"    	</html>\n" +
				"    	\"\"\"",
				escapedValue);

		String literal = ((TextBlock) initializer).getLiteralValue();
		assertEquals("literal value not correct",
				"      	<html>\n" +
				"        <body>\n" +
				"            <p>Hello, world</p>\n" +
				"        </body>\n" +
				"    	</html>\n" +
				"    	",
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
			}finally {
				javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
			}
	}

	public void testSealed001() throws CoreException {
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
			assertEquals("Incorrect no of modfiers", 2, modifiers.size());
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

	public void testSealed002() throws CoreException {
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
}
