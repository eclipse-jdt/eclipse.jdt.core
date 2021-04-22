/*******************************************************************************
 * Copyright (c) 2013, 2016 IBM Corporation and others.
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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import junit.framework.Test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.CreationReference;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeMethodReference;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.UnionType;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WildcardType;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({"rawtypes", "unchecked"})
public class TypeBindingTests308 extends ConverterTestSetup {

	ICompilationUnit workingCopy;

	public static Test suite() {
		return buildModelTestSuite(TypeBindingTests308.class);
	}
	@Override
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		this.ast = AST.newAST(getAST8(), false);
	}
	/**
	 * @deprecated
	 */
	protected int getAST8() {
		return AST.JLS8;
	}
	public TypeBindingTests308(String testName){
		super(testName);
	}
	static {
//		TESTS_NUMBERS = new int[] { };
//		TESTS_RANGE = new int[] { };
//		TESTS_NAMES = new String[] {"test027"};
	}

	private void verifyAnnotationOnType(Type type, String[] annots) {
		verifyAnnotationsOnBinding(type.resolveBinding(), annots);
	}

	private void verifyAnnotationsOnBinding(ITypeBinding binding, String[] annots) {
		IAnnotationBinding[] annotations = binding.getTypeAnnotations();
		assertNotNull("Should not be null", annotations);
		int length = annots.length;
		assertEquals("Incorrect type use annotations", length, annotations.length);
		for (int i = 0; i < length; i++) {
			assertEquals("Incorrect annotation", annots[i], (annotations[i] == null) ? null : annotations[i].toString());
		}
	}

	private void verifyAnnotationsOnBinding(IVariableBinding binding, String[] annots) {
		IAnnotationBinding[] annotations = binding.getAnnotations();
		assertNotNull("Should not be null", annotations);
		int length = annots.length;
		assertEquals("Incorrect annotations", length, annotations.length);
		for (int i = 0; i < length; i++) {
			assertEquals("Incorrect annotation", annots[i], (annotations[i] == null) ? null : annotations[i].toString());
		}
	}

	public void test000() throws Exception {
		String contents =
					"public class X extends @Marker @SingleMember(0) @Normal(value = 0) Object {\n" +
					"}\n" +
					"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
					"@interface Marker {}\n" +
					"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
					"@interface SingleMember { int value() default 0;}\n" +
					"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
					"@interface Normal { int value() default 0;}\n";

		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		List types = compilationUnit.types();
		assertEquals("Incorrect no of types", 4, types.size());
		TypeDeclaration typeDecl = (TypeDeclaration) types.get(0);
		Type type = typeDecl.getSuperclassType();
		assertNotNull("Super class should not be null", type);

		verifyAnnotationOnType(type, new String[]{"@Marker()", "@SingleMember(value = 0)", "@Normal(value = 0)"});
	}
	public void test001() throws Exception {
		String contents =
				"public class X {\n" +
						"    @Marker int x;\n" +
						"}\n" +
						"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
						"@interface Marker {}\n";

		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		List types = compilationUnit.types();
		assertEquals("Incorrect no of types", 2, types.size());
		TypeDeclaration typeDecl = (TypeDeclaration) types.get(0);
		FieldDeclaration[] fields = typeDecl.getFields();
		assertEquals("Incorrect field", 1, fields.length);
		FieldDeclaration field = fields[0];
		verifyAnnotationOnType(field.getType(), new String[]{"@Marker()"});
	}
	public void test002() throws Exception {
		String contents =
						"public class X {\n" +
						"    @Marker <@Marker2 T> int x() { return 10; };\n" +
						"}\n" +
						"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_PARAMETER)\n" +
						"@interface Marker2{}\n" +
						"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
						"@interface Marker {}\n";

		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		List types = compilationUnit.types();
		assertEquals("Incorrect no of types", 3, types.size());
		TypeDeclaration typeDecl = (TypeDeclaration) types.get(0);
		MethodDeclaration[] methods = typeDecl.getMethods();
		assertEquals("Incorrect method", 1, methods.length);
		MethodDeclaration method = methods[0];
		List params = method.typeParameters();
		TypeParameter param = (TypeParameter) params.get(0);
		ITypeBinding binding = param.resolveBinding();
		verifyAnnotationsOnBinding(binding, new String[]{"@Marker2()"});
		verifyAnnotationOnType(method.getReturnType2(), new String[]{"@Marker()"});
	}
	public void test003() throws Exception {
		String contents =
						"public class X {\n" +
						"    int x(@Marker int p) { return 10; };\n" +
						"}\n" +
						"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
						"@interface Marker {}\n";

		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		List types = compilationUnit.types();
		assertEquals("Incorrect no of types", 2, types.size());
		TypeDeclaration typeDecl = (TypeDeclaration) types.get(0);
		MethodDeclaration[] methods = typeDecl.getMethods();
		assertEquals("Incorrect method", 1, methods.length);
		MethodDeclaration method = methods[0];
		List params = method.parameters();
		SingleVariableDeclaration param = (SingleVariableDeclaration) params.get(0);
		ITypeBinding binding = param.resolveBinding().getType();

		verifyAnnotationsOnBinding(binding, new String[]{"@Marker()"});
		verifyAnnotationOnType(param.getType(), new String[]{"@Marker()"});
	}
	public void test004() throws Exception {
		String contents =
				"public class X {\n" +
						"    int x(@Marker int ... p) { return 10; };\n" +
						"}\n" +
						"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
						"@interface Marker {}\n";

		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		List types = compilationUnit.types();
		assertEquals("Incorrect no of types", 2, types.size());
		TypeDeclaration typeDecl = (TypeDeclaration) types.get(0);
		MethodDeclaration[] methods = typeDecl.getMethods();
		assertEquals("Incorrect method", 1, methods.length);
		MethodDeclaration method = methods[0];
		List params = method.parameters();
		SingleVariableDeclaration param = (SingleVariableDeclaration) params.get(0);
		verifyAnnotationOnType(param.getType(), new String[]{"@Marker()"});
	}

	public void test005() throws Exception {
			String contents =
				"public class X {\n" +
						"    int x(@Marker int @Marker2 [] @Marker3 ... p) { return 10; };\n" +
						"}\n" +
						"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
						"@interface Marker {}\n" +
						"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
						"@interface Marker2 {}\n" +
						"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
						"@interface Marker3 {}\n";

		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		List types = compilationUnit.types();
		assertEquals("Incorrect no of types", 4, types.size());
		TypeDeclaration typeDecl = (TypeDeclaration) types.get(0);
		MethodDeclaration[] methods = typeDecl.getMethods();
		assertEquals("Incorrect method", 1, methods.length);
		MethodDeclaration method = methods[0];
		List params = method.parameters();
		SingleVariableDeclaration param = (SingleVariableDeclaration) params.get(0);
		ArrayType type = (ArrayType) param.getType();
		ITypeBinding binding = type.resolveBinding();
		verifyAnnotationsOnBinding(binding, new String[]{"@Marker2()"});
		verifyAnnotationsOnBinding(param.resolveBinding().getType(), new String[]{"@Marker2()"});
		binding = binding.getComponentType();
		verifyAnnotationsOnBinding(binding, new String[]{"@Marker()"});
	}
	public void test006() throws Exception {
		String contents =
						"public class X {\n" +
						"    int x() {\n" +
						"        try {\n" +
						"        } catch (@Marker NullPointerException | @Marker2 ArrayIndexOutOfBoundsException e) {\n" +
						"        }\n" +
						"        return 10;\n" +
						"    }\n" +
						"}\n" +
						"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
						"@interface Marker {}\n" +
						"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
						"@interface Marker2 {}\n";

		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		List types = compilationUnit.types();
		assertEquals("Incorrect no of types", 3, types.size());
		TypeDeclaration typeDecl = (TypeDeclaration) types.get(0);
		MethodDeclaration[] methods = typeDecl.getMethods();
		assertEquals("Incorrect method", 1, methods.length);
		MethodDeclaration method = methods[0];
		List statements = method.getBody().statements();
		TryStatement trySt = (TryStatement) statements.get(0);
		CatchClause catchCl = (CatchClause) trySt.catchClauses().get(0);
		UnionType union = (UnionType) catchCl.getException().getType();
		types = union.types();
		assertEquals("Incorrect union types", 2, types.size());
		Type type = (Type) types.get(0);
		verifyAnnotationOnType(type, new String[]{"@Marker()"});

		type = (Type) types.get(1);
		verifyAnnotationOnType(type, new String[]{"@Marker2()"});
	}
	public void test007() throws Exception {
		String contents =
				"package java.lang;\n" +
				"public class X {\n" +
				"    public void x() throws Exception {\n" +
				"        try (@Marker LocalStream p = null; final @Marker2 LocalStream q = null; @Marker3 final LocalStream r = null) {}\n" +
				"    }\n" +
				"}\n" +
				"class LocalStream implements AutoCloseable {\n" +
				"    public void close() throws Exception {}\n" +
				"}\n" +
				"interface AutoCloseable {}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Marker {}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Marker2 {}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Marker3 {}\n";

		this.workingCopy = getWorkingCopy("/Converter18/src/java/lang/X.java", true/*resolve*/);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		List types = compilationUnit.types();
		assertEquals("Incorrect no of types", 6, types.size());
		TypeDeclaration typeDecl = (TypeDeclaration) types.get(0);
		MethodDeclaration[] methods = typeDecl.getMethods();
		assertEquals("Incorrect method", 1, methods.length);
		MethodDeclaration method = methods[0];
		List statements = method.getBody().statements();
		TryStatement trySt = (TryStatement) statements.get(0);
		List resources = trySt.resources();
		assertEquals("Incorrect no of resources", 3, resources.size());
		VariableDeclarationExpression resource = (VariableDeclarationExpression) resources.get(0);
		Type type = resource.getType();
		assertNotNull("Resource type should not be null", type);
		verifyAnnotationOnType(type, new String[]{"@Marker()"});

		resource = (VariableDeclarationExpression) resources.get(1);
		type = resource.getType();
		assertNotNull("Resource type should not be null", type);
		verifyAnnotationOnType(type, new String[]{"@Marker2()"});

		resource = (VariableDeclarationExpression) resources.get(2);
		type = resource.getType();
		assertNotNull("Resource type should not be null", type);
		verifyAnnotationOnType(type, new String[]{"@Marker3()"});
	}
	public void test008() throws Exception {
		String contents =
				"public class X {\n" +
						"    int x() {\n" +
						"        for (@Marker int i: new int[3]) {}\n" +
						"        for (final @Marker int i: new int[3]) {}\n" +
						"        for (@Marker final int i: new int[3]) {}\n" +
						"        return 10;\n" +
						"    }\n" +
						"}\n" +
						"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
						"@interface Marker {}\n";

		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		List types = compilationUnit.types();
		assertEquals("Incorrect no of types", 2, types.size());
		TypeDeclaration typeDecl = (TypeDeclaration) types.get(0);
		MethodDeclaration[] methods = typeDecl.getMethods();
		assertEquals("Incorrect method", 1, methods.length);
		MethodDeclaration method = methods[0];
		List statements = method.getBody().statements();
		EnhancedForStatement forStmt = (EnhancedForStatement) statements.get(0);
		SingleVariableDeclaration param = forStmt.getParameter();
		Type type = param.getType();
		assertNotNull("Resource type should not be null", type);
		ITypeBinding binding = param.resolveBinding().getType();
		verifyAnnotationsOnBinding(binding, new String[]{"@Marker()"});
		binding = type.resolveBinding();
		verifyAnnotationsOnBinding(binding, new String[]{"@Marker()"});
	}
	public void test009() throws Exception {
		String contents =
				"interface I {\n" +
				"    Object copy(int [] ia);\n" +
				"}\n" +
				"public class X  {\n" +
				"    public static void main(String [] args) {\n" +
				"        I i = @Marker int @Marker2 []::clone;\n" +
				"    }\n" +
				"}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Marker {}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Marker2 {}\n";

		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		List types = compilationUnit.types();
		assertEquals("Incorrect no of types", 4, types.size());
		TypeDeclaration typeDecl = (TypeDeclaration) types.get(1);
		MethodDeclaration[] methods = typeDecl.getMethods();
		assertEquals("Incorrect method", 1, methods.length);
		MethodDeclaration method = methods[0];
		List statements = method.getBody().statements();
		VariableDeclarationStatement stmt = (VariableDeclarationStatement) statements.get(0);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) stmt.fragments().get(0);
		TypeMethodReference lambda = (TypeMethodReference) fragment.getInitializer();
		ArrayType type = (ArrayType) lambda.getType();

		verifyAnnotationOnType(type, new String[]{"@Marker2()"});
		ITypeBinding binding = type.resolveBinding();
		binding = binding.getComponentType();
		verifyAnnotationsOnBinding(binding, new String[]{"@Marker()"});
	}
	public void test010() throws Exception {
		String contents =
				"public class X  {\n" +
				"    public static void main(String [] args) {\n" +
				"        int i [] = new @Marker int @Marker2 [4];\n" +
				"        int j [] = new @Marker2 int @Marker [] { 10 };\n" +
				"    }\n" +
				"}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Marker {}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Marker2 {}\n";

		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		List types = compilationUnit.types();
		assertEquals("Incorrect no of types", 3, types.size());
		TypeDeclaration typeDecl = (TypeDeclaration) types.get(0);
		MethodDeclaration[] methods = typeDecl.getMethods();
		assertEquals("Incorrect method", 1, methods.length);
		MethodDeclaration method = methods[0];
		List statements = method.getBody().statements();
		VariableDeclarationStatement stmt = (VariableDeclarationStatement) statements.get(0);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) stmt.fragments().get(0);
		ArrayCreation arrayCr = (ArrayCreation) fragment.getInitializer();

		ArrayType type = arrayCr.getType();
		ITypeBinding binding = type.resolveBinding();
		verifyAnnotationOnType(type, new String[]{"@Marker2()"});
		verifyAnnotationsOnBinding(binding, new String[]{"@Marker2()"});
		binding = binding.getComponentType();
		verifyAnnotationsOnBinding(binding, new String[]{"@Marker()"});

		stmt = (VariableDeclarationStatement) statements.get(1);
		fragment = (VariableDeclarationFragment) stmt.fragments().get(0);
		arrayCr = (ArrayCreation) fragment.getInitializer();
		type = arrayCr.getType();

		binding = type.resolveBinding();
		verifyAnnotationOnType(type, new String[]{"@Marker()"});
		verifyAnnotationsOnBinding(binding, new String[]{"@Marker()"});
		binding = binding.getComponentType();
		verifyAnnotationsOnBinding(binding, new String[]{"@Marker2()"});
	}
	public void test011() throws Exception {
		String contents =
				"public class X  {\n" +
				"    public static void main(String [] args) {\n" +
				"        int i = (@Marker int) 0;\n" +
				"        int j [] = (@Marker int @Marker2 []) null;\n" +
				"    }\n" +
				"}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Marker {}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Marker2 {}\n";

		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		List types = compilationUnit.types();
		assertEquals("Incorrect no of types", 3, types.size());
		TypeDeclaration typeDecl = (TypeDeclaration) types.get(0);
		MethodDeclaration[] methods = typeDecl.getMethods();
		assertEquals("Incorrect method", 1, methods.length);
		MethodDeclaration method = methods[0];
		List statements = method.getBody().statements();
		VariableDeclarationStatement stmt = (VariableDeclarationStatement) statements.get(0);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) stmt.fragments().get(0);
		CastExpression castExp = (CastExpression) fragment.getInitializer();
		Type type = castExp.getType();
		verifyAnnotationOnType(type, new String[]{"@Marker()"});

		stmt = (VariableDeclarationStatement) statements.get(1);
		fragment = (VariableDeclarationFragment) stmt.fragments().get(0);
		castExp = (CastExpression) fragment.getInitializer();
		ArrayType arrayType = (ArrayType) castExp.getType();

		ITypeBinding binding = arrayType.resolveBinding();
		verifyAnnotationOnType(arrayType, new String[]{"@Marker2()"});
		verifyAnnotationsOnBinding(binding, new String[]{"@Marker2()"});
		binding = binding.getComponentType();
		verifyAnnotationsOnBinding(binding, new String[]{"@Marker()"});
	}
	public void test012() throws Exception {
		String contents =
				"public class X  {\n" +
				"    public static void main(String args) {\n" +
				"        if (args instanceof @Marker String) {\n" +
				"        }\n" +
				"    }\n" +
				"}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Marker {}\n";

		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		List types = compilationUnit.types();
		assertEquals("Incorrect no of types", 2, types.size());
		TypeDeclaration typeDecl = (TypeDeclaration) types.get(0);
		MethodDeclaration[] methods = typeDecl.getMethods();
		assertEquals("Incorrect method", 1, methods.length);
		MethodDeclaration method = methods[0];
		List statements = method.getBody().statements();
		IfStatement ifStmt = (IfStatement) statements.get(0);
		InstanceofExpression instanceOf = (InstanceofExpression) ifStmt.getExpression();
		Type type = instanceOf.getRightOperand();
		verifyAnnotationOnType(type, new String[]{"@Marker()"});
	}
	public void test013() throws Exception {
			String contents =
				"public class X extends Y<@Marker(10) Integer, String> {}\n" +
				"class Y<T, V> {}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Marker {int value();}\n";

		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		List types = compilationUnit.types();
		assertEquals("Incorrect no of types", 3, types.size());
		TypeDeclaration typeDecl = (TypeDeclaration) types.get(0);
		ParameterizedType superClass = (ParameterizedType) typeDecl.getSuperclassType();
		List arguments = superClass.typeArguments();
		assertEquals("Incorrect no of type arguments", 2, arguments.size());
		Type type = (Type) arguments.get(0);

		verifyAnnotationOnType(type, new String[]{"@Marker(value = 10)"});
	}
	public void test014() throws Exception {
		String contents =
				"public class X<T extends Object & Comparable<? super @Marker String>> {}\n" +
				"class Y<T> {}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Marker {}\n";

		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		List types = compilationUnit.types();
		assertEquals("Incorrect no of types", 3, types.size());
		TypeDeclaration typeDecl = (TypeDeclaration) types.get(0);
		List typeParams = typeDecl.typeParameters();

		TypeParameter typeParam = (TypeParameter) typeParams.get(0);
		List bounds = typeParam.typeBounds();
		assertEquals("Incorrect no of type bounds", 2, bounds.size());
		ParameterizedType type = (ParameterizedType) bounds.get(1);
		typeParams = type.typeArguments();
		assertEquals("Incorrect type params", 1, typeParams.size());
		WildcardType wildcard = (WildcardType)typeParams.get(0);
		Type bound = wildcard.getBound();
		assertNotNull("Bound should not be null", bound);
		verifyAnnotationOnType(bound, new String[]{"@Marker()"});
	}
	public void test015() throws Exception {
		String contents =
				"public class X {\n" +
				"	void foo(Map<@Marker ? super @Marker2 Object, @Marker3 ? extends @Marker4 String> m){}\n" +
				"   void goo(Map<@Marker4 ? extends @Marker3 Object, @Marker2 ? super @Marker String> m){}\n" +
				"}\n" +
				"class Map<K, V>{}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Marker {}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Marker2 {}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Marker3 {}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Marker4 {}\n";

		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		List types = compilationUnit.types();
		assertEquals("Incorrect no of types", 6, types.size());

		MethodDeclaration[] methods = ((TypeDeclaration) types.get(0)).getMethods();
		assertEquals("Incorrect no of metods", 2, methods.length);
		MethodDeclaration method = methods[0];
		SingleVariableDeclaration arg = (SingleVariableDeclaration) method.parameters().get(0);


		List typeArgs = ((ParameterizedType) arg.getType()).typeArguments();

		WildcardType wildcard = (WildcardType) typeArgs.get(0);
		verifyAnnotationOnType(wildcard, new String[]{"@Marker()"});
		Type type = wildcard.getBound();
		verifyAnnotationOnType(type, new String[]{"@Marker2()"});

		wildcard = (WildcardType) typeArgs.get(1);
		verifyAnnotationOnType(wildcard, new String[]{"@Marker3()"});
		type = wildcard.getBound();
		verifyAnnotationOnType(type, new String[]{"@Marker4()"});

		method = methods[1];
		arg = (SingleVariableDeclaration) method.parameters().get(0);
		typeArgs = ((ParameterizedType) arg.getType()).typeArguments();

		wildcard = (WildcardType) typeArgs.get(0);
		verifyAnnotationOnType(wildcard, new String[]{"@Marker4()"});
		type = wildcard.getBound();
		verifyAnnotationOnType(type, new String[]{"@Marker3()"});

		wildcard = (WildcardType) typeArgs.get(1);
		verifyAnnotationOnType(wildcard, new String[]{"@Marker2()"});
		type = wildcard.getBound();
		verifyAnnotationOnType(type, new String[]{"@Marker()"});
	}
	public void test016() throws Exception {
		String contents =
				"public class X<E> {\n" +
				"  class Y {\n" +
				"    E e;\n" +
				"    E getOtherElement(Object other) {\n" +
				"      if (!(other instanceof @Marker X<?>.Y)) {};\n" +
				"      return null;\n" +
				"    }\n" +
				"  }\n" +
				"}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Marker {}\n";

		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		List types = compilationUnit.types();
		assertEquals("Incorrect no of types", 2, types.size());
		TypeDeclaration typeDecl = (TypeDeclaration) types.get(0);
		typeDecl = typeDecl.getTypes()[0];
		MethodDeclaration method = typeDecl.getMethods()[0];
		IfStatement ifStmt = (IfStatement) method.getBody().statements().get(0);
		PrefixExpression prefix = (PrefixExpression ) ifStmt.getExpression();
		ParenthesizedExpression operand = (ParenthesizedExpression) prefix.getOperand();
		InstanceofExpression expression = (InstanceofExpression) operand.getExpression();
		QualifiedType type = (QualifiedType) expression.getRightOperand();
		verifyAnnotationOnType(type, new String[]{});
		verifyAnnotationOnType(type.getQualifier(), new String[]{"@Marker()"});
	}
	public void test017() throws Exception {
		String contents =
				"public class X<P, C> {\n" +
				"  public X() {\n" +
				"    if (!(this instanceof @Marker X)) {}\n" +
				"  }\n" +
				"}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Marker {}\n";

		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		List types = compilationUnit.types();
		assertEquals("Incorrect no of types", 2, types.size());
		TypeDeclaration typeDecl = (TypeDeclaration) types.get(0);
		MethodDeclaration method = typeDecl.getMethods()[0];
		IfStatement ifStmt = (IfStatement) method.getBody().statements().get(0);
		PrefixExpression prefix = (PrefixExpression ) ifStmt.getExpression();
		ParenthesizedExpression operand = (ParenthesizedExpression) prefix.getOperand();
		InstanceofExpression expression = (InstanceofExpression) operand.getExpression();
		verifyAnnotationOnType(expression.getRightOperand(), new String[]{"@Marker()"});
	}
	public void test018() throws Exception {
		String contents =
				"interface I {\n" +
				"    void foo(Y<String>.Z z, int x);\n" +
				"}\n" +
				"public class X  {\n" +
				"    public static void main(String [] args) {\n" +
				"        I i = Y<String>.@Marker Z::foo;\n" +
				"        i.foo(new Y<String>().new Z(), 10); \n" +
				"    }\n" +
				"}\n" +
				"class Y<T> {\n" +
				"    class Z {\n" +
				"        void foo(int x) {\n" +
				"        }\n" +
				"    }\n" +
				"}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Marker {}\n";

		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		List types = compilationUnit.types();
		assertEquals("Incorrect no of types", 4, types.size());
		TypeDeclaration typeDecl = (TypeDeclaration) types.get(1);
		MethodDeclaration method = typeDecl.getMethods()[0];
		VariableDeclarationStatement statement = (VariableDeclarationStatement) method.getBody().statements().get(0);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) statement.fragments().get(0);
		TypeMethodReference initializer = (TypeMethodReference) fragment.getInitializer();
		Type type = initializer.getType();
		assertTrue(type.isQualifiedType());
		QualifiedType qualifiedType = (QualifiedType) type;
		checkSourceRange(qualifiedType.getName(), "Z", contents);
		verifyAnnotationOnType(type, new String[]{"@Marker()"});
		assertEquals("Should be a qualified type", ASTNode.QUALIFIED_TYPE, type.getNodeType());
		verifyAnnotationOnType(((QualifiedType) type).getQualifier() , new String[]{});
	}
	public void test019() throws Exception {
		String contents =
				"public class X  {\n" +
				"    public static void main(String [] args) {\n" +
				"        X [] x = new @Marker X @Marker2 [5];\n" +
				"        X [] x2 = new @Marker2 X @Marker [] { null };\n" +
				"    }\n" +
				"}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Marker {}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Marker2 {}\n";

		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		List types = compilationUnit.types();
		assertEquals("Incorrect no of types", 3, types.size());
		TypeDeclaration typeDecl = (TypeDeclaration) types.get(0);
		MethodDeclaration method = typeDecl.getMethods()[0];
		List statements = method.getBody().statements();
		assertEquals("Incorrect no of statements", 2, statements.size());
		VariableDeclarationStatement statement = (VariableDeclarationStatement) statements.get(0);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) statement.fragments().get(0);
		ArrayCreation initializer = (ArrayCreation) fragment.getInitializer();
		ArrayType arrayType = initializer.getType();
		ITypeBinding binding = arrayType.resolveBinding();

		verifyAnnotationOnType(arrayType, new String[]{"@Marker2()"});
		verifyAnnotationsOnBinding(binding, new String[]{"@Marker2()"});

		binding = binding.getComponentType();
		verifyAnnotationsOnBinding(binding, new String[]{"@Marker()"});

		statement = (VariableDeclarationStatement) statements.get(1);
		fragment = (VariableDeclarationFragment) statement.fragments().get(0);
		initializer = (ArrayCreation) fragment.getInitializer();
		arrayType = initializer.getType();
		binding = arrayType.resolveBinding();
		verifyAnnotationOnType(arrayType, new String[]{"@Marker()"});
		verifyAnnotationsOnBinding(binding, new String[]{"@Marker()"});

		binding = binding.getComponentType();
		verifyAnnotationsOnBinding(binding, new String[]{"@Marker2()"});
	}
	public void test020() throws Exception {
		String contents =
				"public class X  {\n" +
				"    public static void main(String [] args) {\n" +
				"        Map.Entry<String, String> [] e = (Map.@Marker Entry<String, String> []) null;\n" +
				"    }\n" +
				"}\n" +
				"class Map<K, V> {\n" +
				"	interface Entry<K, V> {}\n" +
				"}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Marker {}\n";

		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		List types = compilationUnit.types();
		assertEquals("Incorrect no of types", 3, types.size());
		TypeDeclaration typeDecl = (TypeDeclaration) types.get(0);
		MethodDeclaration method = typeDecl.getMethods()[0];
		List statements = method.getBody().statements();
		assertEquals("Incorrect no of statements", 1, statements.size());
		VariableDeclarationStatement statement = (VariableDeclarationStatement) statements.get(0);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) statement.fragments().get(0);
		CastExpression castExp = (CastExpression) fragment.getInitializer();
		ArrayType arrayType = (ArrayType) castExp.getType();
		verifyAnnotationOnType(arrayType, new String[]{});
		ParameterizedType type = (ParameterizedType) arrayType.getElementType();
		verifyAnnotationOnType(type.getType(), new String[]{"@Marker()"});
	}
	public void test021() throws Exception {
		String contents =
				"import java.io.Serializable;\n" +
				"import java.util.List;\n" +
				"public class X<T extends Comparable<T> & Serializable> {\n" +
				"	void foo(List<? extends @Marker @Marker2 Comparable<T>> p) {} \n" +
				"}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Marker {}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Marker2 {}\n";

		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		List types = compilationUnit.types();
		assertEquals("Incorrect no of types", 3, types.size());
		TypeDeclaration typeDecl = (TypeDeclaration) types.get(0);
		MethodDeclaration method = typeDecl.getMethods()[0];
		SingleVariableDeclaration param = (SingleVariableDeclaration) method.parameters().get(0);
		Type type = param.getType();
		assertEquals("Should be a parameterized type", ASTNode.PARAMETERIZED_TYPE, type.getNodeType());
		List typeArgs = ((ParameterizedType) type).typeArguments();
		assertEquals("Incorrect type args", 1, typeArgs.size());
		WildcardType wildcard = (WildcardType) typeArgs.get(0);
		ParameterizedType bound = (ParameterizedType) wildcard.getBound();
		verifyAnnotationOnType(bound, new String[]{"@Marker()", "@Marker2()"});
	}
	public void test022() throws Exception {
		String contents =
				"public class X {\n" +
				"    X x = new @Marker X();\n" +
				"    X y = new <String> @Marker X();\n" +
				"	<T> X(){}\n" +
				"}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Marker {}\n";

		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		List types = compilationUnit.types();
		assertEquals("Incorrect no of types", 2, types.size());
		TypeDeclaration typeDecl = (TypeDeclaration) types.get(0);
		FieldDeclaration[] fields = typeDecl.getFields();
		assertEquals("Incorrect no of fields", 2, fields.length);
		FieldDeclaration field = fields[0];
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) field.fragments().get(0);
		ClassInstanceCreation creation = (ClassInstanceCreation) fragment.getInitializer();
		verifyAnnotationOnType(creation.getType(), new String[]{"@Marker()"});

		field = fields[1];
		fragment = (VariableDeclarationFragment) field.fragments().get(0);
		creation = (ClassInstanceCreation) fragment.getInitializer();
		verifyAnnotationOnType(creation.getType(), new String[]{"@Marker()"});
	}
	public void test023() throws Exception {
		String contents =
				"public class X {\n" +
				"    class Y {\n" +
				"	    <T> Y(){}\n" +
				"    }\n" +
				"    Y y1 = new @Marker X().new @Marker2 Y();\n" +
				"    Y y2 = new @Marker2 X().new <String> @Marker Y();\n" +
				"}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Marker {}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Marker2 {}\n";

		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		List types = compilationUnit.types();
		assertEquals("Incorrect no of types", 3, types.size());
		TypeDeclaration typeDecl = (TypeDeclaration) types.get(0);
		FieldDeclaration[] fields = typeDecl.getFields();
		assertEquals("Incorrect no of fields", 2, fields.length);
		FieldDeclaration field = fields[0];
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) field.fragments().get(0);
		ClassInstanceCreation creation = (ClassInstanceCreation) fragment.getInitializer();
		verifyAnnotationOnType(creation.getType(), new String[]{"@Marker2()"});
		creation = (ClassInstanceCreation) creation.getExpression();
		verifyAnnotationOnType(creation.getType(), new String[]{"@Marker()"});

		field = fields[1];
		fragment = (VariableDeclarationFragment) field.fragments().get(0);
		creation = (ClassInstanceCreation) fragment.getInitializer();
		verifyAnnotationOnType(creation.getType(), new String[]{"@Marker()"});
		creation = (ClassInstanceCreation) creation.getExpression();
		verifyAnnotationOnType(creation.getType(), new String[]{"@Marker2()"});
	}
	public void test024() throws Exception {
		String contents =
				"public class X {\n" +
				"    void foo() throws @Marker NullPointerException, @Marker2 ArrayIndexOutOfBoundsException {}\n" +
				"}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Marker {}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Marker2 {}\n";

		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		List types = compilationUnit.types();
		assertEquals("Incorrect no of types", 3, types.size());
		TypeDeclaration typeDecl = (TypeDeclaration) types.get(0);
		MethodDeclaration method = typeDecl.getMethods()[0];
		List thrownTypes = method.thrownExceptionTypes();
		assertEquals("Incorrect no of thrown exceptions", 2, thrownTypes.size());
		Type type = (Type) thrownTypes.get(0);
		verifyAnnotationOnType(type, new String[]{"@Marker()"});
		type = (Type) thrownTypes.get(1);
		verifyAnnotationOnType(type, new String[]{"@Marker2()"});
	}
	public void test025() throws Exception {
		String contents =
				"interface I {}\n" +
				"interface J {}\n" +
				"interface K extends @Marker I, @Marker2 J {}\n" +
				"interface L {}\n" +
				"public class X implements @Marker2 K, @Marker L {\n" +
				"}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Marker {}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Marker2 {}\n";

		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		List types = compilationUnit.types();
		assertEquals("Incorrect no of types", 7, types.size());
		TypeDeclaration typeDecl = (TypeDeclaration) types.get(2);
		List interfaces = typeDecl.superInterfaceTypes();
		assertEquals("Incorrect no of super interfaces", 2, interfaces.size());
		verifyAnnotationOnType((Type) interfaces.get(0), new String[]{"@Marker()"});
		verifyAnnotationOnType((Type) interfaces.get(1), new String[]{"@Marker2()"});

		typeDecl = (TypeDeclaration) types.get(4);
		interfaces = typeDecl.superInterfaceTypes();
		assertEquals("Incorrect no of super interfaces", 2, interfaces.size());
		verifyAnnotationOnType((Type) interfaces.get(0), new String[]{"@Marker2()"});
		verifyAnnotationOnType((Type) interfaces.get(1), new String[]{"@Marker()"});
	}
	public void test026() throws Exception {
		String contents =
				"interface I {\n" +
				"    void foo(int x);\n" +
				"}\n" +
				"public class X  {\n" +
				"    public static void main(String [] args) {\n" +
				"        I i = A.Y.@Marker Z ::foo;\n" +
				"        i.foo(10); \n" +
				"    }\n" +
				"}\n" +
				"class A {\n" +
				"  static class Y {\n" +
				"    static class Z {\n" +
				"        public static void foo(int x) {\n" +
				"	        System.out.println(x);\n" +
				"        }\n" +
				"    }\n" +
				"  }\n" +
				"}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Marker {}\n";

		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		List types = compilationUnit.types();
		assertEquals("Incorrect no of types", 4, types.size());
		TypeDeclaration typeDecl = (TypeDeclaration) types.get(1);
		MethodDeclaration method = typeDecl.getMethods()[0];
		List statements = method.getBody().statements();

		VariableDeclarationStatement stmt = (VariableDeclarationStatement) statements.get(0);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) stmt.fragments().get(0);
		TypeMethodReference lambda = (TypeMethodReference) fragment.getInitializer();
		Type type = lambda.getType();

		verifyAnnotationOnType(type, new String[]{"@Marker()"});
	}
	public void test027() throws Exception {
		String contents =
				"interface I {\n" +
				"    Y foo(int x);\n" +
				"}\n" +
				"public class X  {\n" +
				"    class Z extends Y {\n" +
				"        public Z(int x) {\n" +
				"            super(x);\n" +
				"        }\n" +
				"    }\n" +
				"    public static void main(String [] args) {\n" +
				"        I i = @Marker W<@Marker2 Integer>::<@Marker3 String> new;\n" +
				"    }\n" +
				"}\n" +
				"class W<T> extends Y {\n" +
				"    public <C> W(T x) {\n" +
				"        super(0);\n" +
				"    }\n" +
				"}\n" +
				"class Y {\n" +
				"    public Y(int x) {\n" +
				"    }\n" +
				"}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Marker {}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Marker2 {}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Marker3 {}\n";

		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		List types = compilationUnit.types();
		assertEquals("Incorrect no of types", 7, types.size());
		TypeDeclaration typeDecl = (TypeDeclaration) types.get(1);
		MethodDeclaration method = typeDecl.getMethods()[0];
		List statements = method.getBody().statements();
		VariableDeclarationStatement statement = (VariableDeclarationStatement) statements.get(0);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) statement.fragments().get(0);
		CreationReference lambda = (CreationReference) fragment.getInitializer();
		Type type = lambda.getType();
		verifyAnnotationOnType(type, new String[]{"@Marker()"});
		ParameterizedType paramType = (ParameterizedType) type;
		verifyAnnotationOnType((Type) paramType.typeArguments().get(0), new String[]{"@Marker2()"});
		List typeArgs = lambda.typeArguments();
		verifyAnnotationOnType((Type) typeArgs.get(0), new String[]{"@Marker3()"});
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=418096
	public void test028() throws Exception {
		String contents =
				"public class X {\n" +
				"    @TypeUseAnnotation(\"a\") String @TypeUseAnnotation(\"a1\") [] @TypeUseAnnotation(\"a2\") [] _field2 @TypeUseAnnotation(\"a3\") [], _field3 @TypeUseAnnotation(\"a4\") [][] = null;\n" +
				"}" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface TypeUseAnnotation {\n" +
				"	String value() default \"\";\n" +
				"}\n";
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		List types = compilationUnit.types();
		assertEquals("Incorrect no of types", 2, types.size());
		TypeDeclaration typeDecl = (TypeDeclaration) types.get(0);
		FieldDeclaration[] fields = typeDecl.getFields();
		assertEquals("Incorrect no of fields", 1, fields.length);
		FieldDeclaration field = fields[0];
		List fragments = field.fragments();
		assertEquals("Incorrect no of fragments", 2, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		ITypeBinding binding = fragment.resolveBinding().getType();
		verifyAnnotationsOnBinding(binding, new String[]{"@TypeUseAnnotation(value = a3)"});
		verifyAnnotationsOnBinding(binding = binding.getComponentType(), new String[]{"@TypeUseAnnotation(value = a1)"});
		verifyAnnotationsOnBinding(binding = binding.getComponentType(), new String[]{"@TypeUseAnnotation(value = a2)"});
		verifyAnnotationsOnBinding(binding = binding.getComponentType(), new String[]{"@TypeUseAnnotation(value = a)"});
		fragment = (VariableDeclarationFragment) fragments.get(1);
		binding = fragment.resolveBinding().getType();
		verifyAnnotationsOnBinding(binding, new String[]{"@TypeUseAnnotation(value = a4)"});
		verifyAnnotationsOnBinding(binding = binding.getComponentType(), new String[]{});
		verifyAnnotationsOnBinding(binding = binding.getComponentType(), new String[]{"@TypeUseAnnotation(value = a1)"});
		verifyAnnotationsOnBinding(binding = binding.getComponentType(), new String[]{"@TypeUseAnnotation(value = a2)"});
		verifyAnnotationsOnBinding(binding = binding.getComponentType(), new String[]{"@TypeUseAnnotation(value = a)"});
	}

	public void testAnnotatedBinaryType() throws CoreException, IOException {
		String jarName = "TypeBindingTests308.jar";
		String srcName = "TypeBindingTests308_src.zip";
		IJavaProject javaProject = getJavaProject("Converter18");
		try {
			String[] pathAndContents = new String[] {
				"Outer.java",
				"public class Outer  {\n" +
				"	class Middle {\n" +
				"		class Inner {\n" +
				"		}\n" +
				"	}\n" +
				"	public @Marker(\"Outer\") Outer.@Marker (\"Middle\") Middle.@Marker(\"Inner\") Inner omi;\n" +
				"}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Marker {\n" +
				"	String value() default \"GOK\";\n" +
				"}\n"
			};

			HashMap libraryOptions = new HashMap(javaProject.getOptions(true));
			libraryOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
			addLibrary(javaProject, jarName, srcName, pathAndContents, JavaCore.VERSION_1_8, libraryOptions);

			String contents =
					"public class X {\n" +
					"    void foo(Outer o) {\n" +
					"        o.omi = null;\n" +
					"    }\n" +
					"}";

			this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
			ASTNode node = buildAST(contents, this.workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertProblemsSize(compilationUnit, 0);
			List types = compilationUnit.types();
			assertEquals("Incorrect no of types", 1, types.size());
			TypeDeclaration typeDecl = (TypeDeclaration) types.get(0);

			MethodDeclaration[] methods = typeDecl.getMethods();
			assertEquals("Incorrect no of methods", 1, methods.length);
			MethodDeclaration method = methods[0];
			Block body = method.getBody();
			ExpressionStatement stmt = (ExpressionStatement) body.statements().get(0);
			Assignment assignment = (Assignment) stmt.getExpression();
			Expression left = assignment.getLeftHandSide();
			ITypeBinding type = left.resolveTypeBinding();
			assertEquals("Wrong type", "@Marker((String)\"Outer\") Outer.@Marker((String)\"Middle\") Middle.@Marker((String)\"Inner\") Inner", type.toString());
		} finally {
			removeLibrary(javaProject, jarName, srcName);
		}
	}
	public void testAnnotatedBinaryType2() throws CoreException, IOException {
		String jarName = "TypeBindingTests308.jar";
		String srcName = "TypeBindingTests308_src.zip";
		IJavaProject javaProject = getJavaProject("Converter18");
		try {
			String[] pathAndContents = new String[] {
				"Outer.java",
				"public class Outer  {\n" +
				"	class Middle {\n" +
				"		class Inner {\n" +
				"		}\n" +
				"	}\n" +
				"	public @Marker(\"Outer\") Outer.@Marker (\"Middle\") Middle.@Marker(\"Inner\") Inner @Marker(\"Prefix []\") [] omi @Marker(\"Extended []\") [];\n" +
				"}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface Marker {\n" +
				"	String value() default \"GOK\";\n" +
				"}\n"
			};

			HashMap libraryOptions = new HashMap(javaProject.getOptions(true));
			libraryOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
			addLibrary(javaProject, jarName, srcName, pathAndContents, JavaCore.VERSION_1_8, libraryOptions);

			String contents =
					"public class X {\n" +
					"    void foo(Outer o) {\n" +
					"        o.omi = null;\n" +
					"    }\n" +
					"}";

			this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
			ASTNode node = buildAST(contents, this.workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertProblemsSize(compilationUnit, 0);
			List types = compilationUnit.types();
			assertEquals("Incorrect no of types", 1, types.size());
			TypeDeclaration typeDecl = (TypeDeclaration) types.get(0);

			MethodDeclaration[] methods = typeDecl.getMethods();
			assertEquals("Incorrect no of methods", 1, methods.length);
			MethodDeclaration method = methods[0];
			Block body = method.getBody();
			ExpressionStatement stmt = (ExpressionStatement) body.statements().get(0);
			Assignment assignment = (Assignment) stmt.getExpression();
			Expression left = assignment.getLeftHandSide();
			ITypeBinding type = left.resolveTypeBinding();
			assertEquals("Wrong type", "@Marker((String)\"Outer\") Outer.@Marker((String)\"Middle\") Middle.@Marker((String)\"Inner\") Inner @Marker((String)\"Extended []\") [] @Marker((String)\"Prefix []\") []", type.toString());
		} finally {
			removeLibrary(javaProject, jarName, srcName);
		}
	}
	public void testAnnotatedBinaryType3() throws CoreException, IOException {
		String jarName = "TypeBindingTests308.jar";
		String srcName = "TypeBindingTests308_src.zip";
		final IJavaProject javaProject = getJavaProject("Converter18");
		try {
			String[] pathAndContents = new String[] {
				"Outer.java",
				"public class Outer<K>  {\n" +
				"	class Inner<P> {\n" +
				"	}\n" +
				"	public @T(1) Outer<@T(2) String>.@T(3) Inner<@T(4) Integer> @T(5) [] omi @T(6) [];\n" +
				"}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface T {\n" +
				"	int value();\n" +
				"}\n"
			};

			HashMap libraryOptions = new HashMap(javaProject.getOptions(true));
			libraryOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
			addLibrary(javaProject, jarName, srcName, pathAndContents, JavaCore.VERSION_1_8, libraryOptions);

			String contents =
					"public class X {\n" +
					"    void foo(Outer<String> o) {\n" +
					"        o.omi = null;\n" +
					"    }\n" +
					"}";

			this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
			ASTNode node = buildAST(contents, this.workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertProblemsSize(compilationUnit, 0);
			List types = compilationUnit.types();
			assertEquals("Incorrect no of types", 1, types.size());
			TypeDeclaration typeDecl = (TypeDeclaration) types.get(0);

			MethodDeclaration[] methods = typeDecl.getMethods();
			assertEquals("Incorrect no of methods", 1, methods.length);
			MethodDeclaration method = methods[0];
			Block body = method.getBody();
			ExpressionStatement stmt = (ExpressionStatement) body.statements().get(0);
			Assignment assignment = (Assignment) stmt.getExpression();
			Expression left = assignment.getLeftHandSide();
			ITypeBinding type = left.resolveTypeBinding();
			assertEquals("Wrong type", "@T((int)1) Outer<@T((int)2) String>.@T((int)3) Inner<@T((int)4) Integer> @T((int)6) [] @T((int)5) []", type.toString());
		} finally {
			removeLibrary(javaProject, jarName, srcName);
		}
	}

	public void testAnnotatedBinaryType4() throws CoreException, IOException {
		String jarName = "TypeBindingTests308.jar";
		String srcName = "TypeBindingTests308_src.zip";
		final IJavaProject javaProject = getJavaProject("Converter18");
		try {
			String[] pathAndContents = new String[] {
				"Outer.java",
				"public class Outer<K>  {\n" +
				"	class Inner<P> {\n" +
				"	}\n" +
				"	@T(1) K @T(2) [] f @T(3) [];\n" +
				"}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface T {\n" +
				"	int value();\n" +
				"}\n"
			};

			HashMap libraryOptions = new HashMap(javaProject.getOptions(true));
			libraryOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
			addLibrary(javaProject, jarName, srcName, pathAndContents, JavaCore.VERSION_1_8, libraryOptions);

			String contents =
					"public class X {\n" +
					"    void foo(Outer<String> o) {\n" +
					"        o.f = null;\n" +
					"    }\n" +
					"}";

			this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
			ASTNode node = buildAST(contents, this.workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertProblemsSize(compilationUnit, 0);
			List types = compilationUnit.types();
			assertEquals("Incorrect no of types", 1, types.size());
			TypeDeclaration typeDecl = (TypeDeclaration) types.get(0);

			MethodDeclaration[] methods = typeDecl.getMethods();
			assertEquals("Incorrect no of methods", 1, methods.length);
			MethodDeclaration method = methods[0];
			Block body = method.getBody();
			ExpressionStatement stmt = (ExpressionStatement) body.statements().get(0);
			Assignment assignment = (Assignment) stmt.getExpression();
			Expression left = assignment.getLeftHandSide();
			ITypeBinding type = left.resolveTypeBinding();
			assertEquals("Wrong type", "@T((int)1) String @T((int)3) [] @T((int)2) []", type.toString());
		} finally {
			removeLibrary(javaProject, jarName, srcName);
		}
	}
	public void testAnnotatedBinaryType5() throws CoreException, IOException {
		String jarName = "TypeBindingTests308.jar";
		String srcName = "TypeBindingTests308_src.zip";
		final IJavaProject javaProject = getJavaProject("Converter18");
		try {
			String[] pathAndContents = new String[] {
				"Outer.java",
				"public class Outer<K>  {\n" +
				"	class Inner<P> {\n" +
				"	}\n" +
				"	@T(1) Outer<@T(2) ? extends @T(3) String>.@T(4) Inner<@T(5) ? super @T(6) Integer> @T(7) [] f @T(8) [];\n" +
				"}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface T {\n" +
				"	int value();\n" +
				"}\n"
			};

			HashMap libraryOptions = new HashMap(javaProject.getOptions(true));
			libraryOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
			addLibrary(javaProject, jarName, srcName, pathAndContents, JavaCore.VERSION_1_8, libraryOptions);

			String contents =
					"public class X {\n" +
					"    void foo(Outer<String> o) {\n" +
					"        o.f = null;\n" +
					"    }\n" +
					"}";

			this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
			ASTNode node = buildAST(contents, this.workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertProblemsSize(compilationUnit, 0);
			List types = compilationUnit.types();
			assertEquals("Incorrect no of types", 1, types.size());
			TypeDeclaration typeDecl = (TypeDeclaration) types.get(0);

			MethodDeclaration[] methods = typeDecl.getMethods();
			assertEquals("Incorrect no of methods", 1, methods.length);
			MethodDeclaration method = methods[0];
			Block body = method.getBody();
			ExpressionStatement stmt = (ExpressionStatement) body.statements().get(0);
			Assignment assignment = (Assignment) stmt.getExpression();
			Expression left = assignment.getLeftHandSide();
			ITypeBinding type = left.resolveTypeBinding();
			assertEquals("Wrong type", "@T((int)1) Outer<@T((int)2) ? extends @T((int)3) String>.@T((int)4) Inner<@T((int)5) ? super @T((int)6) Integer> @T((int)8) [] @T((int)7) []", type.toString());
		} finally {
			removeLibrary(javaProject, jarName, srcName);
		}
	}
	public void testAnnotatedBinaryType6() throws CoreException, IOException {
		String jarName = "TypeBindingTests308.jar";
		String srcName = "TypeBindingTests308_src.zip";
		final IJavaProject javaProject = getJavaProject("Converter18");
		try {
			String[] pathAndContents = new String[] {
				"Outer.java",
				"public class Outer<K>  {\n" +
				"	class Inner<P> {\n" +
				"	}\n" +
				"	@T(1) Outer.@T(2) Inner @T(3) [] f @T(4) [];\n" +
				"}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface T {\n" +
				"	int value();\n" +
				"}\n"
			};

			HashMap libraryOptions = new HashMap(javaProject.getOptions(true));
			libraryOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
			addLibrary(javaProject, jarName, srcName, pathAndContents, JavaCore.VERSION_1_8, libraryOptions);

			String contents =
					"public class X {\n" +
					"    void foo(Outer<String> o) {\n" +
					"        o.f = null;\n" +
					"    }\n" +
					"}";

			this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
			ASTNode node = buildAST(contents, this.workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertProblemsSize(compilationUnit, 0);
			List types = compilationUnit.types();
			assertEquals("Incorrect no of types", 1, types.size());
			TypeDeclaration typeDecl = (TypeDeclaration) types.get(0);

			MethodDeclaration[] methods = typeDecl.getMethods();
			assertEquals("Incorrect no of methods", 1, methods.length);
			MethodDeclaration method = methods[0];
			Block body = method.getBody();
			ExpressionStatement stmt = (ExpressionStatement) body.statements().get(0);
			Assignment assignment = (Assignment) stmt.getExpression();
			Expression left = assignment.getLeftHandSide();
			ITypeBinding type = left.resolveTypeBinding();
			assertEquals("Wrong type", "@T((int)1) Outer#RAW.@T((int)2) Inner#RAW @T((int)4) [] @T((int)3) []", type.toString());
		} finally {
			removeLibrary(javaProject, jarName, srcName);
		}
	}
	public void testIntersectionCastType() throws CoreException, IOException {
		String contents =
				"import java.lang.annotation.ElementType;\n" +
						"import java.lang.annotation.Target;\n" +
						"@Target(ElementType.TYPE_USE)\n" +
						"@interface T1 {\n" +
						"}\n" +
						"@Target(ElementType.TYPE_USE)\n" +
						"@interface T2 {\n" +
						"}\n" +
						"@Target(ElementType.TYPE_USE)\n" +
						"@interface T3 {\n" +
						"}\n" +
						"public class X {\n" +
						"	Object o = (@T1 Object & @T2 Runnable & java.io.@T3 Serializable) null;\n" +
						"	Object p = (@T1 Object & @T2 Runnable & java.io.@T3 Serializable) null;\n" +
						"}\n";
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		List types = compilationUnit.types();
		assertEquals("Incorrect no of types", 4, types.size());
		TypeDeclaration typeDecl = (TypeDeclaration) types.get(3);
		FieldDeclaration[] fields = typeDecl.getFields();
		assertEquals("Incorrect no of fields", 2, fields.length);
		FieldDeclaration field = fields[0];
		List fragments = field.fragments();
		assertEquals("Incorrect no of fragments", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		CastExpression cast = (CastExpression) fragment.getInitializer();
		Type castType = cast.getType();
		ITypeBinding binding1 = castType.resolveBinding();
		assertEquals("Wrong annotations", "@T1 Object & @T2 Runnable & @T3 Serializable", binding1.toString());

		field = fields[1];
		fragments = field.fragments();
		assertEquals("Incorrect no of fragments", 1, fragments.size());
		fragment = (VariableDeclarationFragment) fragments.get(0);
		cast = (CastExpression) fragment.getInitializer();
		castType = cast.getType();
		ITypeBinding binding2 = castType.resolveBinding();
		assertEquals("Wrong annotations", "@T1 Object & @T2 Runnable & @T3 Serializable", binding2.toString());
		assertSame("Should be equal", binding1, binding2);
	}
	public void testMemberType() throws CoreException, IOException {
		String jarName = "TypeBindingTests308.jar";
		String srcName = "TypeBindingTests308_src.zip";
		final IJavaProject javaProject = getJavaProject("Converter18");
		try {
			String[] pathAndContents = new String[] {
				"Outer.java",
				"public class Outer  {\n" +
				"	class Inner {\n" +
				"	}\n" +
				"}\n"
			};

			HashMap libraryOptions = new HashMap(javaProject.getOptions(true));
			libraryOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
			addLibrary(javaProject, jarName, srcName, pathAndContents, JavaCore.VERSION_1_8, libraryOptions);

			String contents =
					"public class X {\n" +
					"    void foo(@T Outer o) {\n" +
					"    }\n" +
					"}\n" +
					"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
					"@interface T {\n" +
					"}\n";


			this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
			ASTNode node = buildAST(contents, this.workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertProblemsSize(compilationUnit, 0);
			List types = compilationUnit.types();
			assertEquals("Incorrect no of types", 2, types.size());
			TypeDeclaration typeDecl = (TypeDeclaration) types.get(0);

			MethodDeclaration[] methods = typeDecl.getMethods();
			assertEquals("Incorrect no of methods", 1, methods.length);
			MethodDeclaration method = methods[0];
			List parameters = method.parameters();
			SingleVariableDeclaration parameter = (SingleVariableDeclaration) parameters.get(0);
			ITypeBinding binding = parameter.resolveBinding().getType();
			assertEquals("@T Outer", binding.toString());
			ITypeBinding [] memberTypes = binding.getDeclaredTypes();
			assertEquals("Incorrect no of types", 1, memberTypes.length);
			assertEquals("Incorrect no of types", "@T Outer.Inner", memberTypes[0].toString());
			assertEquals("Incorrect no of types", "@T Outer", memberTypes[0].getDeclaringClass().toString());
		} finally {
			removeLibrary(javaProject, jarName, srcName);
		}
	}
	public void testMemberType2() throws CoreException, IOException {
		String jarName = "TypeBindingTests308.jar";
		String srcName = "TypeBindingTests308_src.zip";
		final IJavaProject javaProject = getJavaProject("Converter18");
		try {
			String[] pathAndContents = new String[] {
				"Outer.java",
				"public class Outer  {\n" +
				"    @T Outer f;\n"+
				"}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface T {\n" +
				"}\n"
			};

			HashMap libraryOptions = new HashMap(javaProject.getOptions(true));
			libraryOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
			addLibrary(javaProject, jarName, srcName, pathAndContents, JavaCore.VERSION_1_8, libraryOptions);

			String contents =
					"public class X {\n" +
					"    void foo(Outer o) {\n" +
					"		o.f = null;\n" +
					"    }\n" +
					"}\n";

			this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
			ASTNode node = buildAST(contents, this.workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertProblemsSize(compilationUnit, 0);
			List types = compilationUnit.types();
			assertEquals("Incorrect no of types", 1, types.size());
			TypeDeclaration typeDecl = (TypeDeclaration) types.get(0);

			MethodDeclaration[] methods = typeDecl.getMethods();
			assertEquals("Incorrect no of methods", 1, methods.length);
			MethodDeclaration method = methods[0];
			Block body = method.getBody();
			ExpressionStatement stmt = (ExpressionStatement) body.statements().get(0);
			Assignment assignment = (Assignment) stmt.getExpression();
			Expression left = assignment.getLeftHandSide();
			ITypeBinding type = left.resolveTypeBinding();
			assertEquals("Wrong type", "@T Outer", type.toString());
			IVariableBinding[] declaredFields = type.getDeclaredFields();
			assertEquals("Wrong type", 1, declaredFields.length);
			assertEquals("Wrong type", "@T Outer", declaredFields[0].getType().toString());
		} finally {
			removeLibrary(javaProject, jarName, srcName);
		}
	}
	public void testBinarySuperInterfaces() throws CoreException, IOException {
		String jarName = "TypeBindingTests308.jar";
		String srcName = "TypeBindingTests308_src.zip";
		final IJavaProject javaProject = getJavaProject("Converter18");
		try {
			String[] pathAndContents = new String[] {
				"Y.java",
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface T1 {\n" +
				"}\n" +
				"public abstract class Y implements Comparable<@T1 Y>{  \n" +
				"}\n"
			};

			HashMap libraryOptions = new HashMap(javaProject.getOptions(true));
			libraryOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
			addLibrary(javaProject, jarName, srcName, pathAndContents, JavaCore.VERSION_1_8, libraryOptions);

			String contents =
					"public class X {\n" +
					"    void foo(Y y) {\n" +
					"    }\n" +
					"}\n";

			this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
			ASTNode node = buildAST(contents, this.workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertProblemsSize(compilationUnit, 0);
			List types = compilationUnit.types();
			assertEquals("Incorrect no of types", 1, types.size());
			TypeDeclaration typeDecl = (TypeDeclaration) types.get(0);

			MethodDeclaration[] methods = typeDecl.getMethods();
			assertEquals("Incorrect no of methods", 1, methods.length);
			MethodDeclaration method = methods[0];
			List parameters = method.parameters();
			SingleVariableDeclaration parameter = (SingleVariableDeclaration) parameters.get(0);
			ITypeBinding binding = parameter.resolveBinding().getType();
			ITypeBinding binding2 = binding.getInterfaces()[0].getTypeArguments()[0];
			assertEquals("Wrong type", "@T1 Y", binding2.toString());
			assertEquals("Wrong type", "Comparable<@T1 Y>", binding2.getInterfaces()[0].toString());
		} finally {
			removeLibrary(javaProject, jarName, srcName);
		}
	}
	public void testMemberTypeSource() throws CoreException, IOException {
		String contents =
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface T {\n" +
				"}\n" +
				"public class X {\n" +
				"    class Y {}\n" +
				"    @T X.Y xy;\n" +
				"}\n";
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		List types = compilationUnit.types();
		assertEquals("Incorrect no of types", 2, types.size());
		TypeDeclaration typeDecl = (TypeDeclaration) types.get(1);
		FieldDeclaration[] fields = typeDecl.getFields();
		assertEquals("Incorrect no of fields", 1, fields.length);
		FieldDeclaration field = fields[0];
		ITypeBinding binding = field.getType().resolveBinding();
		assertEquals("Wrong Type", "@T X", (binding = binding.getDeclaringClass()).toString());
		assertEquals("Wrong Type", "@T X.Y", (binding = binding.getDeclaredTypes()[0]).toString());
	}
	public void testAnnotatedTypeIdentity() throws CoreException, IOException {
		String contents =
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"import java.util.List;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface T {\n" +
				"}\n" +
				"public class X {\n" +
				"    @T List<@T String> ls = (@T List<@T String>) null;\n" +
				"}\n";
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		List types = compilationUnit.types();
		assertEquals("Incorrect no of types", 2, types.size());
		TypeDeclaration typeDecl = (TypeDeclaration) types.get(1);
		FieldDeclaration[] fields = typeDecl.getFields();
		assertEquals("Incorrect no of fields", 1, fields.length);
		FieldDeclaration field = fields[0];
		ITypeBinding binding = field.getType().resolveBinding();
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) field.fragments().get(0);
		CastExpression cast = (CastExpression) fragment.getInitializer();
		ITypeBinding binding2 = cast.resolveTypeBinding();
		assertEquals("Wrong Type", "@T List<@T String>", binding.toString());
		assertSame("not Equal", binding, binding2);
	}
	public void testAnnotatedTypeIdentity2() throws CoreException, IOException {
		String jarName = "TypeBindingTests308.jar";
		String srcName = "TypeBindingTests308_src.zip";
		final IJavaProject javaProject = getJavaProject("Converter18");
		try {
			String[] pathAndContents = new String[] {
				"Outer.java",
				"public class Outer  {\n" +
				"	Outer @T [] f @T [];\n" +
				"}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface T {\n" +
				"	int value() default 10;\n" +
				"}\n"
			};

			HashMap libraryOptions = new HashMap(javaProject.getOptions(true));
			libraryOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
			addLibrary(javaProject, jarName, srcName, pathAndContents, JavaCore.VERSION_1_8, libraryOptions);

			String contents =
					"public class X {\n" +
					"	 Outer @T [] f @T [];\n" +
					"    void foo(Outer o) {\n" +
					"        o.f = this.f;\n" +
					"    }\n" +
					"}";

			this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
			ASTNode node = buildAST(contents, this.workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertProblemsSize(compilationUnit, 0);
			List types = compilationUnit.types();
			assertEquals("Incorrect no of types", 1, types.size());
			TypeDeclaration typeDecl = (TypeDeclaration) types.get(0);

			MethodDeclaration[] methods = typeDecl.getMethods();
			assertEquals("Incorrect no of methods", 1, methods.length);
			MethodDeclaration method = methods[0];
			Block body = method.getBody();
			ExpressionStatement stmt = (ExpressionStatement) body.statements().get(0);
			Assignment assignment = (Assignment) stmt.getExpression();
			Expression left = assignment.getLeftHandSide();
			ITypeBinding type = left.resolveTypeBinding();
			Expression right = assignment.getRightHandSide();
			ITypeBinding type2 = right.resolveTypeBinding();
			assertEquals("Wrong type", "Outer @T [] @T []", type.toString());
			assertSame ("Should be same", type, type2);
		} finally {
			removeLibrary(javaProject, jarName, srcName);
		}
	}
	public void testAnnotatedTypeIdentity3() throws CoreException, IOException {
		String jarName = "TypeBindingTests308.jar";
		String srcName = "TypeBindingTests308_src.zip";
		final IJavaProject javaProject = getJavaProject("Converter18");
		try {
			String[] pathAndContents = new String[] {
				"Outer.java",
				"import java.util.List;\n" +
				"public class Outer  {\n" +
				"	@T List<@T String> ls;\n" +
				"}\n" +
				"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				"@interface T {\n" +
				"	int value() default 10;\n" +
				"}\n"
			};

			HashMap libraryOptions = new HashMap(javaProject.getOptions(true));
			libraryOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
			addLibrary(javaProject, jarName, srcName, pathAndContents, JavaCore.VERSION_1_8, libraryOptions);

			String contents =
					"import java.util.List;\n" +
					"public class X {\n" +
					"	@T List<@T String> ls;\n" +
					"    void foo(Outer o) {\n" +
					"        o.ls = this.ls;\n" +
					"    }\n" +
					"}";

			this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
			ASTNode node = buildAST(contents, this.workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertProblemsSize(compilationUnit, 0);
			List types = compilationUnit.types();
			assertEquals("Incorrect no of types", 1, types.size());
			TypeDeclaration typeDecl = (TypeDeclaration) types.get(0);

			MethodDeclaration[] methods = typeDecl.getMethods();
			assertEquals("Incorrect no of methods", 1, methods.length);
			MethodDeclaration method = methods[0];
			Block body = method.getBody();
			ExpressionStatement stmt = (ExpressionStatement) body.statements().get(0);
			Assignment assignment = (Assignment) stmt.getExpression();
			Expression left = assignment.getLeftHandSide();
			ITypeBinding type = left.resolveTypeBinding();
			Expression right = assignment.getRightHandSide();
			ITypeBinding type2 = right.resolveTypeBinding();
			assertEquals("Wrong type", "@T List<@T String>", type.toString());
			assertSame ("Should be same", type, type2);
		} finally {
			removeLibrary(javaProject, jarName, srcName);
		}
	}
	public void testHybridAnnotations() throws CoreException, IOException {
		String contents =
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"@interface A {\n" +
				"}\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface AUse {\n" +
				"}\n" +
				"@Target({ElementType.TYPE_USE, ElementType.PARAMETER})\n" +
				"@interface AUseParameter {\n" +
				"}\n" +
				"@Target({ElementType.TYPE_USE, ElementType.LOCAL_VARIABLE})\n" +
				"@interface AUseLocal {\n" +
				"}\n" +
				"@Target({ElementType.PARAMETER})\n" +
				"@interface AParameter {\n" +
				"}\n" +
				"public class X {    \n" +
				"	void foo(@A @AUse @AUseParameter @AUseLocal @AParameter X x) {\n" +
				"	}\n" +
				"}\n";

		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		List types = compilationUnit.types();
		assertEquals("Incorrect no of types", 6, types.size());
		TypeDeclaration typeDecl = (TypeDeclaration) types.get(5);
		MethodDeclaration[] methods = typeDecl.getMethods();
		assertEquals("Incorrect no of methods", 1, methods.length);
		MethodDeclaration method = methods[0];
		SingleVariableDeclaration parameter = (SingleVariableDeclaration) method.parameters().get(0);
		IVariableBinding parameterBinding = parameter.resolveBinding();
		verifyAnnotationsOnBinding(parameterBinding, new String [] { "@A()", "@AUseParameter()", "@AParameter()" });
		ITypeBinding type = parameterBinding.getType();
		verifyAnnotationsOnBinding(type, new String [] { "@AUse()", "@AUseParameter()", "@AUseLocal()" });
	}
	public void testGenericMethod() throws CoreException, IOException {
		String contents =
				"import java.lang.annotation.Annotation;\n" +
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface T {\n" +
				"}\n" +
				"public class X { \n" +
				"	<N extends Annotation> @T String f(N a) {\n" +
				"		return null;\n" +
				"	}\n" +
				"}\n";

		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		List types = compilationUnit.types();
		assertEquals("Incorrect no of types", 2, types.size());
		TypeDeclaration typeDecl = (TypeDeclaration) types.get(1);
		MethodDeclaration[] methods = typeDecl.getMethods();
		assertEquals("Incorrect no of methods", 1, methods.length);
		MethodDeclaration method = methods[0];
		Type returnType = method.getReturnType2();
		ITypeBinding type = returnType.resolveBinding();
		verifyAnnotationsOnBinding(type, new String [] { "@T()" });
	}
	public void testHybridAnnotations2() throws CoreException, IOException {
		String contents =
				"import java.lang.annotation.Target;\n" +
				"import java.lang.annotation.ElementType;\n" +
				"@Target({ ElementType.TYPE_USE, ElementType.METHOD })\n" +
				"@interface SillyAnnotation {  }\n" +
				"public class X {\n" +
				"    @SillyAnnotation\n" +
				"    X(@SillyAnnotation int x) {\n" +
				"    }\n" +
				"    @SillyAnnotation\n" +
				"    void foo(@SillyAnnotation int x) {\n" +
				"    }\n" +
				"    @SillyAnnotation\n" +
				"    String goo(@SillyAnnotation int x) {\n" +
				"	return null;\n" +
				"    }\n" +
				"    @SillyAnnotation\n" +
				"    X field;\n" +
				"}\n";

		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		List types = compilationUnit.types();
		assertEquals("Incorrect no of types", 2, types.size());

		TypeDeclaration typeDecl = (TypeDeclaration) types.get(1);
		MethodDeclaration[] methods = typeDecl.getMethods();
		assertEquals("Incorrect no of methods", 3, methods.length);

		MethodDeclaration method = methods[0];
		List modifiers = method.modifiers();
		int size = modifiers.size();
		assertTrue("Should be just 1", size == 1);
		MarkerAnnotation annotation = (MarkerAnnotation) modifiers.get(0);
		assertEquals("Incorrect annotation", "@SillyAnnotation", annotation.toString());
		SingleVariableDeclaration parameter = (SingleVariableDeclaration) method.parameters().get(0);
	    IAnnotationBinding [] annotations = parameter.resolveBinding().getAnnotations();
		assertTrue("should be 0", annotations == null || annotations.length == 0);
		IAnnotationBinding [] typeAnnotations = parameter.getType().resolveBinding().getTypeAnnotations();
		assertEquals("Incorrect annotation", "@SillyAnnotation()", typeAnnotations[0].toString());

		method = methods[1];
		modifiers = method.modifiers();
		size = modifiers.size();
		assertTrue("Should be just 1", size == 1);
		annotation = (MarkerAnnotation) modifiers.get(0);
		assertEquals("Incorrect annotation", "@SillyAnnotation", annotation.toString());
		typeAnnotations = method.getReturnType2().resolveBinding().getTypeAnnotations();
		assertTrue("Should be just 0", typeAnnotations.length == 0);
		parameter = (SingleVariableDeclaration) method.parameters().get(0);
	    annotations = parameter.resolveBinding().getAnnotations();
		assertTrue("should be 0", annotations == null || annotations.length == 0);
		typeAnnotations = parameter.getType().resolveBinding().getTypeAnnotations();
		assertEquals("Incorrect annotation", "@SillyAnnotation()", typeAnnotations[0].toString());

		method = methods[2];
		modifiers = method.modifiers();
		size = modifiers.size();
		assertTrue("Should be just 1", size == 1);
		annotation = (MarkerAnnotation) modifiers.get(0);
		assertEquals("Incorrect annotation", "@SillyAnnotation", annotation.toString());
		typeAnnotations = method.getReturnType2().resolveBinding().getTypeAnnotations();
		assertTrue("Should be just 1", typeAnnotations.length == 1);
		assertEquals("Incorrect annotation", "@SillyAnnotation()", typeAnnotations[0].toString());
		parameter = (SingleVariableDeclaration) method.parameters().get(0);
	    annotations = parameter.resolveBinding().getAnnotations();
		assertTrue("should be 0", annotations == null || annotations.length == 0);
		typeAnnotations = parameter.getType().resolveBinding().getTypeAnnotations();
		assertEquals("Incorrect annotation", "@SillyAnnotation()", typeAnnotations[0].toString());

		FieldDeclaration[] fields = typeDecl.getFields();
		assertEquals("Incorrect no of fields", 1, fields.length);

		FieldDeclaration field = fields[0];
		modifiers = field.modifiers();
		size = modifiers.size();
		assertTrue("Should be just 1", size == 1);
		annotation = (MarkerAnnotation) modifiers.get(0);
		assertEquals("Incorrect annotation", "@SillyAnnotation", annotation.toString());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) field.fragments().get(0);
		annotations = fragment.resolveBinding().getAnnotations();
		assertTrue("Incorrect annotation", annotations == null || annotations.length == 0);

		typeAnnotations = field.getType().resolveBinding().getTypeAnnotations();
		assertTrue("Should be just 1", typeAnnotations.length == 1);
		assertEquals("Incorrect annotation", "@SillyAnnotation()", typeAnnotations[0].toString());
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=419918, [1.8][compiler] Annotations are not restored from class files in a few situations.
	public void testBinaryWithoutGenericSignature() throws CoreException, IOException {
		String jarName = "TypeBindingTests308.jar";
		String srcName = "TypeBindingTests308_src.zip";
		final IJavaProject javaProject = getJavaProject("Converter18");
		try {
			String[] pathAndContents = new String[] {
				"Superclass.java",
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface T {\n" +
				"	int value() default 0;\n" +
				"}\n" +
				"@T(1)\n" +
				"public abstract class Superclass extends @T(2) Object implements @T(3) Runnable {\n" +
				"	Object @T(4) [] field;\n" +
				"	@T(5)\n" +
				"	public String run(@T(6) Superclass this, Object @T(7) [] that) throws @T(8) NullPointerException {\n" +
				"		return null;\n" +
				"	}\n" +
				"   @T(9)\n" +
				"   Superclass () {}\n" +
				"   @T(10)\n" +
				"   class Inner {}\n" +
				"}\n"
			};

			HashMap libraryOptions = new HashMap(javaProject.getOptions(true));
			libraryOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
			addLibrary(javaProject, jarName, srcName, pathAndContents, JavaCore.VERSION_1_8, libraryOptions);

			String contents =
					"@T(21)\n" +
					"public abstract class X extends @T(22) Superclass implements @T(23) Runnable {\n" +
					"	Object @T(24) [] field;\n" +
					"	@T(25)\n" +
					"	public String run(@T(26) X this, Object @T(27) [] that) throws @T(28) NullPointerException {\n" +
					"		return null;\n" +
					"	}\n" +
					"   @T(29)\n" +
					"   X() {\n" +
		            "   }" +
					"   @T(30)\n" +
					"   class Inner {\n" +
					"   }\n" +
					"}\n";

			this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
			ASTNode node = buildAST(contents, this.workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertProblemsSize(compilationUnit, 0);
			List types = compilationUnit.types();
			assertEquals("Incorrect no of types", 1, types.size());
			TypeDeclaration typeDecl = (TypeDeclaration) types.get(0);
			ITypeBinding typeBinding = typeDecl.resolveBinding();
			IAnnotationBinding[] annotations = typeBinding.getAnnotations();
			assertTrue("Should be 1", annotations.length == 1);
			assertEquals("Annotation mismatch", "@T(value = 21)", annotations[0].toString());
			annotations = typeBinding.getSuperclass().getTypeAnnotations();
			assertTrue("Should be 1", annotations.length == 1);
			assertEquals("Annotation mismatch", "@T(value = 22)", annotations[0].toString());
			annotations = typeBinding.getInterfaces()[0].getTypeAnnotations();
			assertTrue("Should be 1", annotations.length == 1);
			assertEquals("Annotation mismatch", "@T(value = 23)", annotations[0].toString());

			annotations = typeDecl.getFields()[0].getType().resolveBinding().getTypeAnnotations();
			assertTrue("Should be 1", annotations.length == 1);
			assertEquals("Annotation mismatch", "@T(value = 24)", annotations[0].toString());

			annotations = typeDecl.getMethods()[0].getReturnType2().resolveBinding().getTypeAnnotations();
			assertTrue("Should be 1", annotations.length == 1);
			assertEquals("Annotation mismatch", "@T(value = 25)", annotations[0].toString());

			annotations = typeDecl.getMethods()[0].getReceiverType().resolveBinding().getTypeAnnotations();
			assertTrue("Should be 1", annotations.length == 1);
			assertEquals("Annotation mismatch", "@T(value = 26)", annotations[0].toString());

			annotations = ((SingleVariableDeclaration) (typeDecl.getMethods()[0].parameters().get(0))).getType().resolveBinding().getTypeAnnotations();
			assertTrue("Should be 1", annotations.length == 1);
			assertEquals("Annotation mismatch", "@T(value = 27)", annotations[0].toString());

			annotations = ((Type) typeDecl.getMethods()[0].thrownExceptionTypes().get(0)).resolveBinding().getTypeAnnotations();
			assertTrue("Should be 1", annotations.length == 1);
			assertEquals("Annotation mismatch", "@T(value = 28)", annotations[0].toString());

			annotations = typeDecl.getMethods()[1].resolveBinding().getAnnotations();
			assertTrue("Should be 0", annotations.length == 0);

			annotations = typeDecl.getTypes()[0].resolveBinding().getAnnotations();
			assertTrue("Should be 1", annotations.length == 1);
			assertEquals("Annotation mismatch", "@T(value = 30)", annotations[0].toString());


			// Check the same set of things for the binary type.
			annotations = typeBinding.getSuperclass().getAnnotations();
			assertTrue("Should be 1", annotations.length == 1);
			assertEquals("Annotation mismatch", "@T(value = 1)", annotations[0].toString());

			annotations = typeBinding.getSuperclass().getSuperclass().getTypeAnnotations();
			assertTrue("Should be 1", annotations.length == 1);
			assertEquals("Annotation mismatch", "@T(value = 2)", annotations[0].toString());

			annotations = typeBinding.getSuperclass().getInterfaces()[0].getTypeAnnotations();
			assertTrue("Should be 1", annotations.length == 1);
			assertEquals("Annotation mismatch", "@T(value = 3)", annotations[0].toString());

			annotations = typeBinding.getSuperclass().getDeclaredFields()[0].getType().getTypeAnnotations();
			assertTrue("Should be 1", annotations.length == 1);
			assertEquals("Annotation mismatch", "@T(value = 4)", annotations[0].toString());

			// Skip past the constructor at [0]
			annotations = typeBinding.getSuperclass().getDeclaredMethods()[1].getReturnType().getTypeAnnotations();
			assertTrue("Should be 1", annotations.length == 1);
			assertEquals("Annotation mismatch", "@T(value = 5)", annotations[0].toString());

			annotations = typeBinding.getSuperclass().getDeclaredMethods()[1].getDeclaredReceiverType().getTypeAnnotations();
			assertTrue("Should be 1", annotations.length == 1);
			assertEquals("Annotation mismatch", "@T(value = 6)", annotations[0].toString());

			annotations = typeBinding.getSuperclass().getDeclaredMethods()[1].getParameterTypes()[0].getTypeAnnotations();
			assertTrue("Should be 1", annotations.length == 1);
			assertEquals("Annotation mismatch", "@T(value = 7)", annotations[0].toString());

			annotations = typeBinding.getSuperclass().getDeclaredMethods()[1].getExceptionTypes()[0].getTypeAnnotations();
			assertTrue("Should be 1", annotations.length == 1);
			assertEquals("Annotation mismatch", "@T(value = 8)", annotations[0].toString());

			annotations = typeBinding.getSuperclass().getDeclaredMethods()[0].getAnnotations();
			assertTrue("Should be 0", annotations.length == 0);

			annotations = typeBinding.getSuperclass().getDeclaredTypes()[0].getAnnotations();
			assertTrue("Should be 1", annotations.length == 1);
			assertEquals("Annotation mismatch", "@T(value = 10)", annotations[0].toString());
		} finally {
			removeLibrary(javaProject, jarName, srcName);
		}
	}
	// Variants where superclass in binary is an annotated inner/nested class
	public void testBinaryWithoutGenericSignature_b() throws CoreException, IOException {
		String jarName = "TypeBindingTests308.jar";
		String srcName = "TypeBindingTests308_src.zip";
		final IJavaProject javaProject = getJavaProject("Converter18");
		try {
			String[] pathAndContents = new String[] {
				"Superclass.java",
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface T {\n" +
				"	int value() default 0;\n" +
				"}\n" +
				"@T(1)\n" +
				"public abstract class Superclass extends @T(2) Object implements @T(3) Runnable {\n" +
				"   @T(9)\n" +
				"   Superclass () {}\n" +
				"   @T(10)\n" +
				"   class Inner {}\n" +
				"   @T(11)\n" +
				"   class SubInner extends @T(12) Inner {}\n" +
				"   @T(13)\n" +
				"   static class Nested {}\n" +
				"   @T(14)\n" +
				"   static class SubNested extends @T(15) Nested {}\n" +
				"}\n"
			};

			HashMap libraryOptions = new HashMap(javaProject.getOptions(true));
			libraryOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
			addLibrary(javaProject, jarName, srcName, pathAndContents, JavaCore.VERSION_1_8, libraryOptions);

			String contents =
					"@T(21)\n" +
					"public abstract class X extends @T(22) Superclass implements @T(23) Runnable {\n" +
					"}\n";

			this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
			ASTNode node = buildAST(contents, this.workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertProblemsSize(compilationUnit, 0);
			List types = compilationUnit.types();
			assertEquals("Incorrect no of types", 1, types.size());
			TypeDeclaration typeDecl = (TypeDeclaration) types.get(0);
			ITypeBinding typeBinding = typeDecl.resolveBinding();
			IAnnotationBinding[] annotations = typeBinding.getAnnotations();
			assertTrue("Should be 1", annotations.length == 1);
			assertEquals("Annotation mismatch", "@T(value = 21)", annotations[0].toString());

			ITypeBinding superclass = typeBinding.getSuperclass();
			ITypeBinding[] inners = superclass.getDeclaredTypes();
			assertTrue("Should be 2", inners.length == 4);

			ITypeBinding subInner = inners[2];
			assertEquals("Type name mismatch", "SubInner", subInner.getName());
			annotations = subInner.getAnnotations();
			assertTrue("Should be 1", annotations.length == 1);
			assertEquals("Annotation mismatch", "@T(value = 11)", annotations[0].toString());

			annotations = subInner.getSuperclass().getTypeAnnotations();
			assertTrue("Should be 1", annotations.length == 1);
			assertEquals("Annotation mismatch", "@T(value = 12)", annotations[0].toString());

			ITypeBinding subNested = inners[3];
			annotations = subNested.getAnnotations();
			assertTrue("Should be 1", annotations.length == 1);
			assertEquals("Annotation mismatch", "@T(value = 14)", annotations[0].toString());

			annotations = subNested.getSuperclass().getTypeAnnotations();
			assertTrue("Should be 1", annotations.length == 1);
			assertEquals("Annotation mismatch", "@T(value = 15)", annotations[0].toString());
		} finally {
			removeLibrary(javaProject, jarName, srcName);
		}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=419918, [1.8][compiler] Annotations are not restored from class files in a few situations.
	public void testBinaryAnnotationType() throws CoreException, IOException {
		String jarName = "TypeBindingTests308.jar";
		String srcName = "TypeBindingTests308_src.zip";
		final IJavaProject javaProject = getJavaProject("Converter18");
		try {
			String[] pathAndContents = new String[] {
				"T.java",
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"@Deprecated\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface T {\n" +
				"	int value() default 0;\n" +
				"}\n"
			};

			HashMap libraryOptions = new HashMap(javaProject.getOptions(true));
			libraryOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
			addLibrary(javaProject, jarName, srcName, pathAndContents, JavaCore.VERSION_1_8, libraryOptions);

			String contents =
					"@T\n" +
					"public class X {\n" +
					"}\n";

			this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
			ASTNode node = buildAST(contents, this.workingCopy, false);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertProblemsSize(compilationUnit, 1, "The type T is deprecated");
			List types = compilationUnit.types();
			assertEquals("Incorrect no of types", 1, types.size());
			TypeDeclaration typeDecl = (TypeDeclaration) types.get(0);
			ITypeBinding typeBinding = typeDecl.resolveBinding();
			IAnnotationBinding[] annotations = typeBinding.getAnnotations()[0].getAnnotationType().getAnnotations();
			assertTrue("Should be 2", annotations.length == 2);
			IMemberValuePairBinding[] allMemberValuePairs = annotations[0].getAllMemberValuePairs();
			assertEquals("Annotations mismatch", 1, allMemberValuePairs.length);
			Object value= allMemberValuePairs[0].getValue();
			if(value instanceof Object[]) {
				Object[] valueArr = (Object[]) value;
				assertEquals("Annotations mismatch", 1, valueArr.length);
				assertEquals("Annotations mismatch", "public static final java.lang.annotation.ElementType TYPE_USE", valueArr[0].toString());
			} else {
				assertEquals("Annotation mismatch", "@Target(value = {public static final java.lang.annotation.ElementType TYPE_USE})", annotations[0].toString());
			}

			assertEquals("Annotation mismatch", "@Deprecated()", annotations[1].toString());
		} finally {
			removeLibrary(javaProject, jarName, srcName);
		}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=420320, [1.8] Bad AST recovery with type annotation and a syntax error in secondary type
	public void testAnnotationRecovery() throws CoreException, IOException {
		String contents =
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"import java.util.List;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface NonNull {\n" +
				"}\n" +
				"public class X {\n" +
				"	List<@NonNull String> list2;\n" +
				"}\n" +
				"class Y {\n" +
				"    void bar()\n" +
				"    void foo() { }\n" +
				"}\n";

		String expected =
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"import java.util.List;\n" +
				"@Target(ElementType.TYPE_USE) @interface NonNull {}\n" +
				"public class X {\n" +
				"  List<@NonNull String> list2;\n" +
				"}\n" +
				"class Y {\n" +
				"  void bar(){\n" +
				"  }\n" +
				"  void foo(){\n" +
				"  }\n" +
				"}\n";

		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy, false, true);
		assertEquals("AST mismatch", expected, node.toString());
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427337
	public void testBug427337() throws CoreException, IOException {
		String contents =
				"public class X implements I {\n" +
				"}\n";

		createFile("/Converter18/src/NonNull.java",
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface NonNull {}");
		createFile("/Converter18/src/I.java",
				"import java.util.List;\n" +
				"interface I { \n" +
				"	String bar2(@NonNull String s, @NonNull List<@NonNull String> l2);\n" +
				"}");

		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy, false, true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		TypeDeclaration type = (TypeDeclaration) ((CompilationUnit) node).types().get(0);
		ITypeBinding binding = type.resolveBinding();
		ITypeBinding superInterface = binding.getInterfaces()[0];
		IMethodBinding method = superInterface.getDeclaredMethods()[0];
		binding = method.getParameterTypes()[0];
		assertEquals("Incorrect type binding", "@NonNull String", binding.toString());
		binding = method.getParameterTypes()[1];
		assertEquals("Incorrect type binding", "@NonNull List<@NonNull String>", binding.toString());
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426515
	public void testBug426515() throws CoreException {
		try {
			String contents =
					"public class X {\n" +
					"	void foo() {\n" +
					"		Outer.getInner();\n" +
					"	}\n" +
					"}\n";

			createFile("/Converter18/src/A.java",
					"import java.lang.annotation.ElementType;\n" +
					"import java.lang.annotation.Target;\n" +
					"@Target(ElementType.TYPE_USE)\n" +
					"@interface A { int value() default 0; \n }");
			createFile("/Converter18/src/Outer.java",
					"class Outer<T> { \n" +
					"	public class Inner<I> {}\n" +
					"	public static @A(1) Outer<java.lang.@A(2) String>.@A(3) Inner<java.lang.@A(4) Object> getInner() { \n" +
					"		return null;\n" +
					"	}\n" +
					"}");

			this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
			ASTNode node = buildAST(contents, this.workingCopy, false, true);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			TypeDeclaration type = (TypeDeclaration) ((CompilationUnit) node).types().get(0);
			MethodDeclaration method = type.getMethods()[0];
			ExpressionStatement statement = (ExpressionStatement) method.getBody().statements().get(0);
			MethodInvocation methodCal = (MethodInvocation) statement.getExpression();
			ITypeBinding binding = methodCal.resolveTypeBinding();
			assertEquals("Incorrect type binding", "@A((int)1) Outer<@A((int)2) String>.@A((int)3) Inner<@A((int)4) Object>", binding.toString());
		} finally {
			deleteFile("/Converter18/src/A.java");
			deleteFile("/Converter18/src/Outer.java");
		}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=425599, [1.8][compiler] ISE when trying to compile qualified and annotated class instance creation
	public void test425599() throws CoreException, IOException {
		String contents =
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"public class X {\n" +
				"    Object ax = new @A(1) Outer().new @A(2) Middle<@A(3) String>();\n" +
				"}\n" +
				"@Target(ElementType.TYPE_USE) @interface A { int value(); }\n" +
				"class Outer {\n" +
				"    class Middle<E> {}\n" +
				"}\n";

		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		List types = compilationUnit.types();
		assertEquals("Incorrect no of types", 3, types.size());
		TypeDeclaration typeDecl = (TypeDeclaration) types.get(0);
		FieldDeclaration[] fields = typeDecl.getFields();
		assertEquals("Incorrect no of methods", 1, fields.length);
		FieldDeclaration field = fields[0];
		assertEquals("Object ax=new @A(1) Outer().new @A(2) Middle<@A(3) String>();\n", field.toString());
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=425216, Bug 425216 - [1.8][dom ast] Binding for 'this' should have type annotations when receiver is annotated
	public void test425216() throws CoreException, IOException {
		String contents =
				"import static java.lang.annotation.ElementType.TYPE_USE;\n" +
				"import java.lang.annotation.Target;\n" +
				"@Target(TYPE_USE)\n" +
				"@interface NonNull {}\n" +
				"public class X {\n" +
				"   X foo(@NonNull X this) {\n" +
				"	   return this;\n" +
				"   }\n" +
				"}\n";

		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		List types = compilationUnit.types();
		assertEquals("Incorrect no of types", 2, types.size());
		TypeDeclaration typeDecl = (TypeDeclaration) types.get(1);
		MethodDeclaration[] methods = typeDecl.getMethods();
		assertEquals("Incorrect no of methods", 1, methods.length);
		MethodDeclaration method = methods[0];
		ReturnStatement statement = (ReturnStatement) method.getBody().statements().get(0);
		ThisExpression expression = (ThisExpression) statement.getExpression();
		ITypeBinding type = expression.resolveTypeBinding();
		assertEquals("@NonNull X", type.toString());
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=425216, Bug 425216 - [1.8][dom ast] Binding for 'this' should have type annotations when receiver is annotated
	public void test425216a() throws CoreException, IOException {
		String contents =
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface A {\n" +
				"    int value() default 0;\n" +
				"}\n" +
				"public class Outer {\n" +
				"    class Middle {\n" +
				"    	class Inner {\n" +
				"    		public @A(1) Inner(@A(2) Outer.@A(3) Middle Middle.this) {\n" +
				"    			Outer r1 = Outer.this;\n" +
				"    			Outer.Middle middle = Outer.Middle.this;\n" +
				"    			Inner i = this;\n" +
				"    		}\n" +
				"    	}\n" +
				"    }\n" +
				"}\n";

		this.workingCopy = getWorkingCopy("/Converter18/src/Outer.java", true);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		List types = compilationUnit.types();
		assertEquals("Incorrect no of types", 2, types.size());
		TypeDeclaration typeDecl = (TypeDeclaration) types.get(1);
		MethodDeclaration method= typeDecl.getTypes()[0].getTypes()[0].getMethods()[0];
		ITypeBinding receiverType = method.getReceiverType().resolveBinding();
		assertEquals("@A((int)2) Outer.@A((int)3) Middle", receiverType.toString());
		ITypeBinding declaringClass = receiverType.getDeclaringClass();
		assertEquals("@A((int)2) Outer", declaringClass.toString());
		final List statements = method.getBody().statements();
		VariableDeclarationStatement statement = ((VariableDeclarationStatement) statements.get(0));
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) statement.fragments().get(0);
		ITypeBinding type = fragment.getInitializer().resolveTypeBinding();
		assertEquals("@A((int)2) Outer", type.toString());
		statement = ((VariableDeclarationStatement) statements.get(1));
		fragment = (VariableDeclarationFragment) statement.fragments().get(0);
		type = fragment.getInitializer().resolveTypeBinding();
		assertEquals("@A((int)2) Outer.@A((int)3) Middle", type.toString());
		assertEquals("@A((int)2) Outer", type.getDeclaringClass().toString());
		statement = ((VariableDeclarationStatement) statements.get(2));
		fragment = (VariableDeclarationFragment) statement.fragments().get(0);
		type = fragment.getInitializer().resolveTypeBinding();
		assertTrue(type.getTypeAnnotations().length == 0);
		assertTrue(type.getName().equals("Inner"));
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=425216, Bug 425216 - [1.8][dom ast] Binding for 'this' should have type annotations when receiver is annotated
	public void test425216b() throws CoreException, IOException {
		String contents =
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface A {\n" +
				"    int value() default 0;\n" +
				"}\n" +
				"public class Outer {\n" +
				"    class Middle {\n" +
				"    	class Inner {\n" +
				"    		public @A(1) Inner(@A(2) Outer.@A(3) Middle Middle.this) {\n" +
				"    		}\n" +
				"    	}\n" +
				"    }\n" +
				"}\n";

		this.workingCopy = getWorkingCopy("/Converter18/src/Outer.java", true);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		List types = compilationUnit.types();
		assertEquals("Incorrect no of types", 2, types.size());
		TypeDeclaration typeDecl = (TypeDeclaration) types.get(1);
		MethodDeclaration method= typeDecl.getTypes()[0].getTypes()[0].getMethods()[0];
		SimpleName receiverQualifier = method.getReceiverQualifier();
		ITypeBinding type = receiverQualifier.resolveTypeBinding();
		assertEquals("@A((int)2) Outer.@A((int)3) Middle", type.toString());
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427320
	public void testBug427320() throws Exception {
		try {
			String contents =
					"public class X {\n" +
					"	@A @B @C X() {}\n" +
					"	@A @B @C String foo() {\nreturn null;\n}\n" +
					"}\n" +
					"@java.lang.annotation.Target ({java.lang.annotation.ElementType.CONSTRUCTOR, "
													+ "java.lang.annotation.ElementType.METHOD, java.lang.annotation.ElementType.TYPE_USE})\n" +
					"@interface A {}\n" +
					"@java.lang.annotation.Target ({java.lang.annotation.ElementType.CONSTRUCTOR, "
													+ "java.lang.annotation.ElementType.METHOD})\n" +
					"@interface B {}\n" +
					"@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
					"@interface C {}\n";

		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(contents, this.workingCopy, false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		List types = compilationUnit.types();
		TypeDeclaration typeDecl = (TypeDeclaration) types.get(0);
		MethodDeclaration method = typeDecl.getMethods()[0];
		assertTrue("Should be a constructor", method.isConstructor());
		IMethodBinding methodBinding = method.resolveBinding();
		IAnnotationBinding[] annots = methodBinding.getAnnotations();
		assertEquals("Incorrect no of annotations", 2, annots.length);
		assertEquals("Incorrect annotations attached","@A()", annots[0].toString());
		assertEquals("Incorrect annotations attached","@B()", annots[1].toString());
		ITypeBinding binding = methodBinding.getReturnType();
		annots = binding.getTypeAnnotations();
		assertEquals("Incorrect no of annotations", 0, annots.length);

		method = typeDecl.getMethods()[1];
		methodBinding = method.resolveBinding();
		annots = methodBinding.getAnnotations();
		assertEquals("Incorrect no of annotations", 2, annots.length);
		assertEquals("Incorrect annotations attached","@A()", annots[0].toString());
		assertEquals("Incorrect annotations attached","@B()", annots[1].toString());
		binding = methodBinding.getReturnType();
		annots = binding.getTypeAnnotations();
		assertEquals("Incorrect no of annotations", 2, annots.length);
		assertEquals("Incorrect annotations attached","@A @C String", binding.toString());
		} finally {
			deleteFile("/Converter18/src/X.java");
		}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=431810
	public void testBug431810() throws Exception {
		try {
			String contents =
				"import java.lang.annotation.ElementType; \n" +
				"import java.lang.annotation.Target; \n" +
				"@interface A {}\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"@interface B {} \n" +
				"class X {\n" +
				"	@A \n" +
				"	X() {}\n" +
				"	@B \n" +
				"	X(int x) {}\n" +
				"}\n";

		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(contents, this.workingCopy, false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);

		List types = compilationUnit.types();
		TypeDeclaration typeDecl = (TypeDeclaration) types.get(2);

		// X()
		MethodDeclaration method = typeDecl.getMethods()[0];
		assertTrue("Should be a constructor", method.isConstructor());
		IMethodBinding methodBinding = method.resolveBinding();
		IAnnotationBinding[] annots = methodBinding.getAnnotations();

		assertEquals("Incorrect no of annotations", 1, annots.length);
		assertEquals("Incorrect annotations attached","@A()", annots[0].toString());

		// X(int)
		method = typeDecl.getMethods()[1];
		assertTrue("Should be a constructor", method.isConstructor());
		methodBinding = method.resolveBinding();
		annots = methodBinding.getAnnotations();

		assertEquals("Incorrect no of annotations", 0, annots.length);
		} finally {
			deleteFile("/Converter18/src/X.java");
		}
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=431810
	// Incorrect use of annotations on constructors
	public void testBug431810a() throws Exception {
		try {
			String contents =
				"import java.lang.annotation.ElementType; \n" +
				"import java.lang.annotation.Target; \n" +
				"@Target({}) \n" +
				"@interface A {} \n" +
				"@Target(ElementType.TYPE)\n" +
				"@interface B {} \n" +
				"class X {\n" +
				"	@A \n" +
				"	X() {}\n" +
				"	@B \n" +
				"	X(int x) {}\n" +
				"}\n";

		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(contents, this.workingCopy, false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;

		List types = compilationUnit.types();
		TypeDeclaration typeDecl = (TypeDeclaration) types.get(2);

		// X()
		MethodDeclaration method = typeDecl.getMethods()[0];
		assertTrue("Should be a constructor", method.isConstructor());
		IMethodBinding methodBinding = method.resolveBinding();
		IAnnotationBinding[] annots = methodBinding.getAnnotations();
		assertEquals("Incorrect no of annotations", 0, annots.length);

		// X(int)
		method = typeDecl.getMethods()[1];
		assertTrue("Should be a constructor", method.isConstructor());
		methodBinding = method.resolveBinding();
		annots = methodBinding.getAnnotations();
		assertEquals("Incorrect no of annotations", 0, annots.length);
		} finally {
			deleteFile("/Converter18/src/X.java");
		}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=487716
	public void testBug487716() throws Exception {
		try {
			String contents =
				"import java.lang.annotation.ElementType; \n" +
				"import java.lang.annotation.Target; \n" +
				"@Target({ElementType.TYPE_USE, ElementType.CONSTRUCTOR})\n" +
				"@interface A {} \n" +
				"class X {\n" +
				"	@A X() {}\n" +
				"	X _x_ = new X();\n" +
				"}\n";

			this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(contents, this.workingCopy, false);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;

			List types = compilationUnit.types();
			TypeDeclaration typeDecl = (TypeDeclaration) types.get(1);

			// On the Allocation expression type - new X()
			FieldDeclaration field = typeDecl.getFields()[0];
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) field.fragments().get(0);
			ITypeBinding type = fragment.getInitializer().resolveTypeBinding();
			IAnnotationBinding[] annots = type.getTypeAnnotations();
			assertEquals("Incorrect no of annotations", 1, annots.length);

			// On constructor declaration - X()
			MethodDeclaration method = typeDecl.getMethods()[0];
			assertTrue("Should be a constructor", method.isConstructor());
			IMethodBinding methodBinding = method.resolveBinding();
			annots = methodBinding.getAnnotations();
			assertEquals("Incorrect no of annotations", 1, annots.length);
		} finally {
			deleteFile("/Converter18/src/X.java");
		}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=487716
	public void testBug487716a() throws Exception {
		try {
			String contents =
				"package p;\n" +
				"import java.lang.annotation.ElementType; \n" +
				"import java.lang.annotation.Target; \n" +
				"@Target({ElementType.TYPE_USE})\n" +
				"@interface A {} \n" +
				"class X {\n" +
				"	@A X() {}\n" +
				"   class Y {\n" +
				"		@A Y() {}\n" +
				"		Y _y_ = new X().new Y();\n" +
				"	}\n" +
				"}\n";

			this.workingCopy = getWorkingCopy("/Converter18/src/p/X.java", true/*resolve*/);
			ASTNode node = buildAST(contents, this.workingCopy, false);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;

			List types = compilationUnit.types();
			TypeDeclaration typeDecl = (TypeDeclaration) types.get(1);

			assertEquals(1, typeDecl.getTypes().length);
			typeDecl = typeDecl.getTypes()[0];

			// On the Qualified Allocation expression type - new X().new Y()
			FieldDeclaration field = typeDecl.getFields()[0];
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) field.fragments().get(0);
			ITypeBinding type = fragment.getInitializer().resolveTypeBinding();
			IAnnotationBinding[] annots = type.getTypeAnnotations();
			assertEquals("Incorrect no of annotations", 1, annots.length);

			// On constructor declaration - Y()
			MethodDeclaration method = typeDecl.getMethods()[0];
			assertTrue("Should be a constructor", method.isConstructor());
			IMethodBinding methodBinding = method.resolveBinding();
			annots = methodBinding.getAnnotations();
			assertEquals("Incorrect no of annotations", 0, annots.length);
		} finally {
			deleteFile("/Converter18/src/X.java");
		}
	}
	public void testBug460491_comment30() throws CoreException {
		// placed in this suite because we need type annotations enabled
		IPackageFragmentRoot srcRoot = this.currentProject.getPackageFragmentRoots()[0];
		assertFalse(srcRoot.isReadOnly());
		createFolder(srcRoot.getPath().append("test"));
		IPackageFragment testPackage = srcRoot.getPackageFragment("test");

		testPackage.createCompilationUnit("Generic.java",
				"package test;\n" +
				"\n" +
				"public class Generic<T> {\n" +
				"	public static class NestedStatic {\n" +
				"		public static final String X = \"x\";\n" +
				"	}\n" +
				"}\n",
				true,
				null);
		String contents =
				"package test;\n" +
				"\n" +
				"public class Usage {\n" +
				"	String f() {\n" +
				"		return Generic.NestedStatic.X;\n" +
				"	}\n" +
				"}\n";
		ICompilationUnit cu = testPackage.createCompilationUnit("Usage.java", contents, true, null);
		ASTNode node = buildAST(contents, cu, false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;

		List types = compilationUnit.types();
		assertEquals(1, types.size());
		List methods = ((TypeDeclaration) types.get(0)).bodyDeclarations();
		assertEquals(1, methods.size());
		List statements = ((MethodDeclaration) methods.get(0)).getBody().statements();
		assertEquals(1, statements.size());

		Expression expression = ((ReturnStatement) statements.get(0)).getExpression();
		IBinding binding = ((QualifiedName) expression).getQualifier().resolveBinding();
		assertNotNull(binding);
	}
}
