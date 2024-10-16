/*******************************************************************************
 * Copyright (c) 2020, 2023 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.dom;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.List;
import java.util.Map;
import junit.framework.Test;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.*;

public class ASTConverter_16Test extends ConverterTestSetup {

	ICompilationUnit workingCopy;

	@SuppressWarnings("deprecation")
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		this.ast = AST.newAST(getAST16(), false);
		this.currentProject = getJavaProject("Converter_16");
		if (this.ast.apiLevel() == AST.JLS16 ) {
			this.currentProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_16);
			this.currentProject.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_16);
			this.currentProject.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_16);

		}
	}

	public ASTConverter_16Test(String name) {
		super(name);
	}
	public static Test suite() {
		return buildModelTestSuite(ASTConverter_16Test.class);
	}

	@SuppressWarnings("deprecation")
	static int getAST16() {
		return AST.JLS16;
	}
	protected void tearDown() throws Exception {
		super.tearDown();
		if (this.workingCopy != null) {
			this.workingCopy.discardWorkingCopy();
			this.workingCopy = null;
		}
	}

	public void testRecord001() throws CoreException {
		if (!isJRE16) {
			System.err.println("Test "+getName()+" requires a JRE 16");
			return;
		}
		String contents =
			"public record X() {\n" +
			"		public X {\n" +
			"			System.out.println(\"no error\");\n" +
			"		}\n" +
			"\n" +
			"}\n";
		this.workingCopy = getWorkingCopy("/Converter_16/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
	}

	/**
	 * Added for Bug 561193 - [14]record keyword inside method not colored correctly
	 */
	public void testRecord002() throws CoreException {
		if (!isJRE16) {
			System.err.println("Test "+getName()+" requires a JRE 16");
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
		this.workingCopy = getWorkingCopy("/Converter_16/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
	}

	public void testRecord003() throws CoreException {
		if (!isJRE16) {
			System.err.println("Test "+getName()+" requires a JRE 16");
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
		this.workingCopy = getWorkingCopy("/Converter_16/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
	}

	public void testRecord004() throws CoreException {
		if (!isJRE16) {
			System.err.println("Test " + getName() + " requires a JRE 16");
			return;
		}
		String contents = "public class X {\n" +
						  "	public static void main(String[] args) {\n" +
				          "		record R(int x,int y){}\n" +
						  "		R r = new R(100, 200);\n" +
				          "		System.out.println(r.x());\n" +
						  "	}\n" +
				          "}";
		this.workingCopy = getWorkingCopy("/Converter_16/src/X.java", true/* resolve */);
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
	}

	public void testRecord005() throws CoreException {
		if (!isJRE16) {
			System.err.println("Test "+getName()+" requires a JRE 16");
			return;
		}
		String contents = "public class X {\n" +
				  "	public static void main(String[] args) {\n" +
		          "		record R(int x,String y){}\n" +
				  "		R r = new R(100, \"Point\");\n" +
		          "		System.out.println(r.x());\n" +
				  "	}\n" +
		          "}";
		this.workingCopy = getWorkingCopy("/Converter_16/src/X.java", true/*resolve*/);
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
	}

	public void testRecord006() throws CoreException {
		if (!isJRE16) {
			System.err.println("Test "+getName()+" requires a JRE 16");
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
		this.workingCopy = getWorkingCopy("/Converter_16/src/X.java", true/*resolve*/);
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
	}

	public void testRecord007() throws CoreException {
		if (!isJRE16) {
			System.err.println("Test "+getName()+" requires a JRE 16");
			return;
		}
		String contents = "record X(int lo) {\n" +
				"	public int lo() {\n" +
				"		return this.lo;\n" +
				"	}\n" +
				"\n" +
				"}\n";
		this.workingCopy = getWorkingCopy("/Converter_16/src/X.java", true/*resolve*/);
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
	}

	public void testRecord008() throws CoreException {
		if (!isJRE16) {
			System.err.println("Test "+getName()+" requires a JRE 16");
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

		this.workingCopy = getWorkingCopy("/Converter_16/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = ((AbstractTypeDeclaration)compilationUnit.types().get(0));
		assertNotEquals("Not a Record Declaration", ASTNode.RECORD_DECLARATION, node.getNodeType());

	}

	public void testRecord009() throws CoreException {
		if (!isJRE16) {
			System.err.println("Test "+getName()+" requires a JRE 16");
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
		ICompilationUnit workingCopy2 = getWorkingCopy("/Converter_16/src/java/lang/annotation/ElementType.java", true/*resolve*/);
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
		this.workingCopy = getWorkingCopy("/Converter_16/src/X.java", true/*resolve*/);
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

	}

	public void _testRecord010() throws CoreException {
		if (!isJRE16) {
			System.err.println("Test "+getName()+" requires a JRE 16");
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
		ICompilationUnit workingCopy2 = getWorkingCopy("/Converter_16/src/java/lang/annotation/ElementType.java", true/*resolve*/);
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
		this.workingCopy = getWorkingCopy("/Converter_16/src/X.java", true/*resolve*/);
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

	}

	public void testRecord011() throws CoreException {
		if (!isJRE16) {
			System.err.println("Test "+getName()+" requires a JRE 16");
			return;
		}
		String contents =
			"public record X() {\n" +
			"		public X {\n" +
			"			System.out.println(\"no error\");\n" +
			"		}\n" +
			"\n" +
			"}\n";
		this.workingCopy = getWorkingCopy("/Converter_16/src/X.java", true/*resolve*/);
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
	}

	public void testRecord012() throws CoreException {
		if (!isJRE16) {
			System.err.println("Test "+getName()+" requires a JRE 16");
			return;
		}
		String contents =
			"public record X(int myComp) {\n" +
			"		public void foo() {\n" +
			"			System.out.println(\"no error\");\n" +
			"		}\n" +
			"\n" +
			"}\n";
		this.workingCopy = getWorkingCopy("/Converter_16/src/X.java", true/*resolve*/);
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

	}

	public void testClass001() throws CoreException {
		if (!isJRE16) {
			System.err.println("Test "+getName()+" requires a JRE 16");
			return;
		}
		String contents =
			"public class X {\n" +
			"		public X() {\n" +
			"			System.out.println(\"no error\");\n" +
			"		}\n" +
			"\n" +
			"}\n";
		this.workingCopy = getWorkingCopy("/Converter_16/src/X.java", true/*resolve*/);
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
		if (!isJRE16) {
			System.err.println("Test "+getName()+" requires a JRE 16");
			return;
		}
		String contents =
			"public interface X {\n" +
			"\n" +
			"}\n";
		this.workingCopy = getWorkingCopy("/Converter_16/src/X.java", true/*resolve*/);
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

	@SuppressWarnings("deprecation")
	public void testPatternInstanceOfExpression001() throws JavaModelException {
		if (!isJRE16) {
			System.err.println("Test "+getName()+" requires a JRE 16");
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
		this.workingCopy = getWorkingCopy("/Converter_16/src/X.java", true/*resolve*/);
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
		assertEquals("Not an instanceof expression", ASTNode.PATTERN_INSTANCEOF_EXPRESSION, expression.getNodeType());
		PatternInstanceofExpression instanceofExpression = (PatternInstanceofExpression) expression;
		ITypeBinding typeBinding = instanceofExpression.resolveTypeBinding();
		assertEquals("boolean", typeBinding.toString());
		SingleVariableDeclaration var = instanceofExpression.getRightOperand();
		checkSourceRange(var, "String s", contents);
	}

	public void testPatternInstanceOfExpression002() throws JavaModelException {
		if (!isJRE16) {
			System.err.println("Test "+getName()+" requires a JRE 16");
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
		this.workingCopy = getWorkingCopy("/Converter_16/src/X.java", true/*resolve*/);
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
		InstanceofExpression instanceofExpression = (InstanceofExpression) expression;
		Expression var = instanceofExpression.getLeftOperand();
		assertNotNull(var);
	}

	public void testPatternInstanceOfExpression003() throws JavaModelException {
		if (!isJRE16) {
			System.err.println("Test "+getName()+" requires a JRE 16");
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
		this.workingCopy = getWorkingCopy("/Converter_16/src/X.java", true/*resolve*/);

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
		assertEquals("Not an instanceof expression", ASTNode.PATTERN_INSTANCEOF_EXPRESSION, expression.getNodeType());
		PatternInstanceofExpression instanceofExpression = (PatternInstanceofExpression) expression;
		@SuppressWarnings("deprecation")
		SingleVariableDeclaration var = instanceofExpression.getRightOperand();
		checkSourceRange(var, "String s", contents);
		String instanceofExpressionString = instanceofExpression.toString();
		assertEquals("o instanceof String s", instanceofExpressionString);
	}

	public void testRecordConstructor001() throws CoreException {
		if (!isJRE16) {
			System.err.println("Test "+getName()+" requires a JRE 16");
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
		this.workingCopy = getWorkingCopy("/Converter_16/src/X.java", true/*resolve*/);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
			ASTNode node = buildAST(
				contents,
				this.workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertProblemsSize(compilationUnit, 0);
			node = ((AbstractTypeDeclaration)compilationUnit.types().get(0));
			assertEquals("Not a Type", ASTNode.RECORD_DECLARATION, node.getNodeType());
			ASTParser parser= ASTParser.newParser(getAST16());
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
	}

	public void testRecordConstructor002() throws CoreException {
		if (!isJRE16) {
			System.err.println("Test "+getName()+" requires a JRE 16");
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
		this.workingCopy = getWorkingCopy("/Converter_16/src/X.java", true/*resolve*/);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = ((AbstractTypeDeclaration)compilationUnit.types().get(0));
		assertEquals("Not a Type", ASTNode.RECORD_DECLARATION, node.getNodeType());
		ASTParser parser= ASTParser.newParser(getAST16());
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
	}

	public void testLocalEnum() throws CoreException {
		if (!isJRE16) {
			System.err.println("Test "+getName()+" requires a JRE 16");
			return;
		}
		String contents = "public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"    enum Y1 { \n" +
			"		\n" +
			"		BLEU, \n" +
			"		BLANC, \n" +
			"		ROUGE,\n" +
			"		BLEUN;\n" +
			"	 }\n" +
			"	}\n" +
			"	}\n";
		this.workingCopy = getWorkingCopy("/Converter_16/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 0, 0);
		assertEquals("Not an enum statement", ASTNode.ENUM_DECLARATION, ((TypeDeclarationStatement)node).getDeclaration().getNodeType());
	}
	public void testTypeBindingMethods() {
	    var parser = ASTParser.newParser(AST.getJLSLatest());
	    parser.setResolveBindings(true);
	    parser.setEnvironment(null, null, null, true);
	    parser.setCompilerOptions(
	        Map.of(
	            JavaCore.COMPILER_RELEASE, "enabled",
	            JavaCore.COMPILER_SOURCE, "16",
	            JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, "16"
	        )
	    );
	    parser.setBindingsRecovery(true);
	    parser.setStatementsRecovery(true);
	    parser.setKind(ASTParser.K_COMPILATION_UNIT);
	    parser.setUnitName("X.java");
	    parser.setSource("class X {void main() {var x = java.util.List.of(1, 1.0);}}".toCharArray());
	    var cu = (CompilationUnit) parser.createAST(null);
	    cu.accept(new ASTVisitor() {
	      @Override
	      public boolean visit(VariableDeclarationFragment node) {
	        var varBinding = node.resolveBinding();
	        assertNotNull(varBinding);
	        var typeBinding = varBinding.getType();
	        assertNotNull(typeBinding);
	        assertTrue(typeBinding.isParameterizedType());
	        assertEquals(1, typeBinding.getTypeArguments().length);
	        var parameterType = typeBinding.getTypeArguments()[0];
	        assertNotNull(parameterType);
	        assertTrue(parameterType.isIntersectionType());
	        var bounds = parameterType.getTypeBounds();
	        assertTrue("size of type bounds should be > 1 but actual size is " + bounds.length, bounds.length > 1);
	        return super.visit(node);
	      }
	    });
	}
	@SuppressWarnings("rawtypes")
	public void test_RecordSemicolon() throws JavaModelException {
		if (!isJRE16) {
			System.err.println("Test "+getName()+" requires a JRE 16");
			return;
		}
		String contents =
		        "record Point(int x, int y) {\n" +
		        "    private static final int staticField = 16;\n"+
		        "}";
		    this.workingCopy = getWorkingCopy("/Converter_16/src/ASTtree.java", true/*resolve*/);
		    ASTNode node = buildAST(
		    		contents,
		    		this.workingCopy);
		    assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		    CompilationUnit unit = (CompilationUnit) node;
		    assertProblemsSize(unit, 0);
		    node = getASTNode(unit, 0);
		    assertEquals("Not a record declaration", ASTNode.RECORD_DECLARATION, node.getNodeType());
		    RecordDeclaration recordDeclaration = (RecordDeclaration) node;
		    final List bodyDeclarations = recordDeclaration.bodyDeclarations();
		    assertEquals("Wrong size", 1, bodyDeclarations.size());
		    FieldDeclaration fieldDeclaration = (FieldDeclaration) bodyDeclarations.get(0);
		    final List fragments = fieldDeclaration.fragments();
		    assertEquals("Wrong size", 1, fragments.size());
		    VariableDeclarationFragment fragment = (VariableDeclarationFragment)fragments.get(0);
		    final Expression initializer = fragment.getInitializer();
		    assertEquals("Not a number literal", ASTNode.NUMBER_LITERAL, initializer.getNodeType());
		    checkSourceRange(initializer, "16", contents);


	}

	public void testRecord013() throws JavaModelException {
		if (!isJRE16) {
			System.err.println("Test " + getName() + " requires a JRE 16");
			return;
		}
		String code = """
					record Test(String name) {
					    public static Builder builder() {return null;}
					    public static final class Builder {}
					}
				""";
		this.workingCopy = getWorkingCopy("/Converter_16/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			code,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = ((AbstractTypeDeclaration)compilationUnit.types().get(0));
		assertEquals("Not a Record Declaration", ASTNode.RECORD_DECLARATION, node.getNodeType());
		RecordDeclaration record = (RecordDeclaration)node;

		List<SingleVariableDeclaration> RecordComponents = record.recordComponents();
		assertEquals("Not a Single Variable Declaration Declaration", ASTNode.SINGLE_VARIABLE_DECLARATION, RecordComponents.get(0).getNodeType());

		List<ASTNode> bodyDeclaration = record.bodyDeclarations();
		MethodDeclaration md = (MethodDeclaration) bodyDeclaration.get(0);
		TypeDeclaration td = (TypeDeclaration) bodyDeclaration.get(1);
		assertEquals("Not a MethodDeclaration", ASTNode.METHOD_DECLARATION, md.getNodeType());
		assertEquals("Not a TypeDeclaration", ASTNode.TYPE_DECLARATION, td.getNodeType());
	}

	public void testClass002() throws CoreException {
		if (!isJRE16) {
			System.err.println("Test "+getName()+" requires a JRE 16");
			return;
		}
		String code = """
					class Test {
					    public static Builder builder() {return null;}
					    public static final class Builder {}
					}
				""";
		this.workingCopy = getWorkingCopy("/Converter_16/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
				code,
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

		List<ASTNode> bodyDeclaration = typeDecl.bodyDeclarations();
		MethodDeclaration md = (MethodDeclaration) bodyDeclaration.get(0);
		TypeDeclaration td = (TypeDeclaration) bodyDeclaration.get(1);
		assertEquals("Not a MethodDeclaration", ASTNode.METHOD_DECLARATION, md.getNodeType());
		assertEquals("Not a TypeDeclaration", ASTNode.TYPE_DECLARATION, td.getNodeType());
	}
}
