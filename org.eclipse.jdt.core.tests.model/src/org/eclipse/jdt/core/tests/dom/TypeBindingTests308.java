/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.dom;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import junit.framework.Test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
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
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
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

public class TypeBindingTests308 extends ConverterTestSetup {

	ICompilationUnit workingCopy;

	public static Test suite() {
		return buildModelTestSuite(TypeBindingTests308.class);
	}
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		this.ast = AST.newAST(AST.JLS8);
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
		verifyAnnotationOnType(((QualifiedType)type).getQualifier(), new String[]{});
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
	
	public void testAnnotatedBinaryMemberType() throws CoreException, IOException {
		String jarName = "TypeBindingTests308.jar";
		String srcName = "TypeBindingTests308_src.zip";
		final IJavaProject javaProject = getJavaProject("Converter18");
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
			assertEquals("Wrong type", "@Marker{ value = (String)\"Outer\"} Outer.@Marker{ value = (String)\"Middle\"} Middle.@Marker{ value = (String)\"Inner\"} Inner", type.toString());		
		} finally {
				removeLibrary(javaProject, jarName, srcName);
		}
	}
	public void testAnnotatedBinaryMemberType2() throws CoreException, IOException {
		String jarName = "TypeBindingTests308.jar";
		String srcName = "TypeBindingTests308_src.zip";
		final IJavaProject javaProject = getJavaProject("Converter18");
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
			assertEquals("Wrong type", "@Marker{ value = (String)\"Outer\"} Outer.Middle.@Marker{ value = (String)\"Middle\"} @Marker{ value = (String)\"Inner\"} Inner @Marker{ value = (String)\"Extended []\"} [] @Marker{ value = (String)\"Prefix []\"} []", type.toString());		
		} finally {
				removeLibrary(javaProject, jarName, srcName);
		}
	}
	public void _testAnnotatedBinaryMemberType3() throws CoreException, IOException {
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
			assertEquals("Wrong type", "@Marker{ value = (String)\"Outer\"} Outer.Middle.@Marker{ value = (String)\"Middle\"} @Marker{ value = (String)\"Inner\"} Inner @Marker{ value = (String)\"Extended []\"} [] @Marker{ value = (String)\"Prefix []\"} []", type.toString());		
		} finally {
				removeLibrary(javaProject, jarName, srcName);
		}
	}
}
