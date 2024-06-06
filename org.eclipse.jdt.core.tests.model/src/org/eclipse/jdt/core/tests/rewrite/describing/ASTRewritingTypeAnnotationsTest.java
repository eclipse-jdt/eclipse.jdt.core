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
package org.eclipse.jdt.core.tests.rewrite.describing;
import java.util.List;

import junit.framework.Test;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

@SuppressWarnings("rawtypes")
public class ASTRewritingTypeAnnotationsTest extends ASTRewritingTest {

	public ASTRewritingTypeAnnotationsTest(String name) {
		super(name);
	}
	public ASTRewritingTypeAnnotationsTest(String name, int apiLevel) {
		super(name, apiLevel);
	}

	public static Test suite() {
		return createSuite(ASTRewritingTypeAnnotationsTest.class, getAST8());
	}
	/**
	 * @deprecated references deprecated old AST level
	 */
	protected static int getAST8() {
		return AST.JLS8;
	}

	public void testCastAnnotations() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			import java.lang.annotation.ElementType;
			public class X {
				String myObject = "Foo";
				public void foo() {
					String myString = (@Annot String) myObject;
					String myString1 = (@Annot1 @Annot String) myObject;
				}
			}
			@java.lang.annotation.Target(value = {ElementType.TYPE_USE})
			@interface Annot {}
			@java.lang.annotation.Target(value = {ElementType.TYPE_USE})
			@interface Annot1 {}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", str, false, null);
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDeclaration= findMethodDeclaration(type, "foo");
		List statements= methodDeclaration.getBody().statements();
		{//Add an use of annotation.
			VariableDeclarationStatement  variableDeclarationStatement= (VariableDeclarationStatement) statements.get(0);
			VariableDeclarationFragment variableDeclarationFragment= (VariableDeclarationFragment) variableDeclarationStatement.fragments().get(0);
			CastExpression castExpression= (CastExpression) variableDeclarationFragment.getInitializer();
			SimpleType simpleType= (SimpleType) castExpression.getType();
			MarkerAnnotation markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot2"));
			rewrite.getListRewrite(simpleType, SimpleType.ANNOTATIONS_PROPERTY).insertLast(markerAnnotation, null);
		}
		{
			VariableDeclarationStatement  variableDeclarationStatement= (VariableDeclarationStatement) statements.get(1);
			VariableDeclarationFragment variableDeclarationFragment= (VariableDeclarationFragment) variableDeclarationStatement.fragments().get(0);
			CastExpression castExpression= (CastExpression) variableDeclarationFragment.getInitializer();
			SimpleType simpleType= (SimpleType) castExpression.getType();
			List annotations = simpleType.annotations();
			//Remove the use of an Annotation
			rewrite.remove((ASTNode)annotations.get(1), null);
			//Replace the use of an Annotation
			MarkerAnnotation markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot2"));
			rewrite.replace((ASTNode)annotations.get(0), markerAnnotation, null);
		}
		String preview= evaluateRewrite(cu, rewrite);
		String str1 = """
			package test1;
			import java.lang.annotation.ElementType;
			public class X {
				String myObject = "Foo";
				public void foo() {
					String myString = (@Annot @Annot2 String) myObject;
					String myString1 = (@Annot2 String) myObject;
				}
			}
			@java.lang.annotation.Target(value = {ElementType.TYPE_USE})
			@interface Annot {}
			@java.lang.annotation.Target(value = {ElementType.TYPE_USE})
			@interface Annot1 {}
			""";
		assertEqualString(preview, str1);
	}

	public void testWildcardTypeArgumentAnnotations() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			import java.lang.annotation.ElementType;
			public class X {
				public class Helper<T> {
				}
				public class Base {
				}
				public static void UnboundedWildcard1 (Helper<@Annot ?> x) {
				}
				public static void UnboundedWildcard2 (Helper<@Annot1 @Annot ?> x) {
				}
				public static void BoundedWildcard1 (Helper<@Annot ? extends Base> x) {
				}
				public static void BoundedWildcard2 (Helper<@Annot1 @Annot ? extends Base> x) {
				}
			}
			@java.lang.annotation.Target (ElementType.TYPE_USE)
			@interface Annot {}
			@java.lang.annotation.Target (ElementType.TYPE_USE)
			@interface Annot1 {}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", str, false, null);
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		{//Add an use of an annotation.
			MethodDeclaration methodDeclaration= findMethodDeclaration(type, "UnboundedWildcard1");
			List parameters= methodDeclaration.parameters();
			SingleVariableDeclaration singleVariableDeclaration= (SingleVariableDeclaration) parameters.get(0);
			ParameterizedType parameterizedType= (ParameterizedType) singleVariableDeclaration.getType();
			WildcardType wildcardType= (WildcardType) parameterizedType.typeArguments().get(0);
			MarkerAnnotation markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot2"));
			rewrite.getListRewrite(wildcardType, WildcardType.ANNOTATIONS_PROPERTY).insertLast(markerAnnotation, null);

			methodDeclaration= findMethodDeclaration(type, "BoundedWildcard1");
			parameters= methodDeclaration.parameters();
			singleVariableDeclaration= (SingleVariableDeclaration) parameters.get(0);
			parameterizedType= (ParameterizedType) singleVariableDeclaration.getType();
			wildcardType= (WildcardType) parameterizedType.typeArguments().get(0);
			markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot2"));
			rewrite.getListRewrite(wildcardType, WildcardType.ANNOTATIONS_PROPERTY).insertLast(markerAnnotation, null);
		}
		{
			MethodDeclaration methodDeclaration= findMethodDeclaration(type, "UnboundedWildcard2");
			List parameters= methodDeclaration.parameters();
			SingleVariableDeclaration singleVariableDeclaration= (SingleVariableDeclaration) parameters.get(0);
			ParameterizedType parameterizedType= (ParameterizedType) singleVariableDeclaration.getType();
			WildcardType wildcardType= (WildcardType) parameterizedType.typeArguments().get(0);

			methodDeclaration= findMethodDeclaration(type, "BoundedWildcard2");
			parameters= methodDeclaration.parameters();
			singleVariableDeclaration= (SingleVariableDeclaration) parameters.get(0);
			parameterizedType= (ParameterizedType) singleVariableDeclaration.getType();
			WildcardType wildcardType2= (WildcardType) parameterizedType.typeArguments().get(0);

			//Remove the use of an annotation
			rewrite.remove((MarkerAnnotation) wildcardType.annotations().get(1), null);
			rewrite.remove((MarkerAnnotation) wildcardType2.annotations().get(1), null);

			MarkerAnnotation markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot2"));
			MarkerAnnotation markerAnnotation2= ast.newMarkerAnnotation();
			markerAnnotation2.setTypeName(ast.newSimpleName("Annot2"));

			//Replace the use of an annotation
			rewrite.replace((MarkerAnnotation) wildcardType.annotations().get(0), markerAnnotation, null);
			rewrite.replace((MarkerAnnotation) wildcardType2.annotations().get(0), markerAnnotation2, null);
		}
		String preview= evaluateRewrite(cu, rewrite);
		String str1 = """
			package test1;
			import java.lang.annotation.ElementType;
			public class X {
				public class Helper<T> {
				}
				public class Base {
				}
				public static void UnboundedWildcard1 (Helper<@Annot @Annot2 ?> x) {
				}
				public static void UnboundedWildcard2 (Helper<@Annot2 ?> x) {
				}
				public static void BoundedWildcard1 (Helper<@Annot @Annot2 ? extends Base> x) {
				}
				public static void BoundedWildcard2 (Helper<@Annot2 ? extends Base> x) {
				}
			}
			@java.lang.annotation.Target (ElementType.TYPE_USE)
			@interface Annot {}
			@java.lang.annotation.Target (ElementType.TYPE_USE)
			@interface Annot1 {}
			""";
		assertEqualString(preview, str1);
	}

	public void testWildcardBoudAnnotation() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			public class X {
				public class Helper<T> {
				}
				public class Base {
				}
				public static void foo1 (Helper<? extends @Annot Base> x) {
				}
				public static void foo2 (Helper<? extends @Annot1 @Annot Base> x) {
				}
			}
			""";

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", str, false, null);
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		{
			MethodDeclaration methodDeclaration= findMethodDeclaration(type, "foo1");
			List parameters= methodDeclaration.parameters();
			SingleVariableDeclaration singleVariableDeclaration= (SingleVariableDeclaration) parameters.get(0);
			ParameterizedType parameterizedType= (ParameterizedType) singleVariableDeclaration.getType();
			WildcardType wildcardType= (WildcardType) parameterizedType.typeArguments().get(0);
			SimpleType simpleType= (SimpleType) wildcardType.getBound();
			MarkerAnnotation markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot2"));
			rewrite.getListRewrite(simpleType, SimpleType.ANNOTATIONS_PROPERTY).insertLast(markerAnnotation, null);
		}
		{
			MethodDeclaration methodDeclaration= findMethodDeclaration(type, "foo2");
			List parameters= methodDeclaration.parameters();
			SingleVariableDeclaration singleVariableDeclaration= (SingleVariableDeclaration) parameters.get(0);
			ParameterizedType parameterizedType= (ParameterizedType) singleVariableDeclaration.getType();
			WildcardType wildcardType= (WildcardType) parameterizedType.typeArguments().get(0);
			SimpleType simpleType= (SimpleType) wildcardType.getBound();
			//Remove the use of annotation
			rewrite.remove((MarkerAnnotation) simpleType.annotations().get(1), null);
			//Replace the use of annotation
			MarkerAnnotation markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot2"));
			rewrite.replace((MarkerAnnotation) simpleType.annotations().get(0), markerAnnotation, null);
		}
		String preview= evaluateRewrite(cu, rewrite);
		String str1 = """
			public class X {
				public class Helper<T> {
				}
				public class Base {
				}
				public static void foo1 (Helper<? extends @Annot @Annot2 Base> x) {
				}
				public static void foo2 (Helper<? extends @Annot2 Base> x) {
				}
			}
			""";
		assertEqualString(preview, str1);
	}

	public void testTypeParameterBoundAnnotations() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			import java.lang.annotation.ElementType;
			
			public class X {
				public class Base {
				}
				public <X extends @Annot Base> void foo1 (X x) {
				}
				public <X extends @Annot1 @Annot Base> void foo2 (X x) {
				}
			}
			
			@java.lang.annotation.Target (ElementType.TYPE_USE)\s
			@interface Annot {}
			
			@java.lang.annotation.Target (ElementType.TYPE_USE)\s
			@interface Annot1 {}
			""";

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", str, false, null);
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		{//Add an use of annotation
			MethodDeclaration methodDeclaration= findMethodDeclaration(type, "foo1");
			TypeParameter typeParameter= (TypeParameter) methodDeclaration.typeParameters().get(0);
			SimpleType simpleType= (SimpleType) typeParameter.typeBounds().get(0);
			MarkerAnnotation markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot2"));
			rewrite.getListRewrite(simpleType, SimpleType.ANNOTATIONS_PROPERTY).insertLast(markerAnnotation, null);
		}
		{
			MethodDeclaration methodDeclaration= findMethodDeclaration(type, "foo2");
			TypeParameter typeParameter= (TypeParameter) methodDeclaration.typeParameters().get(0);
			SimpleType simpleType= (SimpleType) typeParameter.typeBounds().get(0);
			//Remove an use of annotation
			rewrite.remove((MarkerAnnotation) simpleType.annotations().get(1), null);
			//Replace an use of annotation
			MarkerAnnotation markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot2"));
			rewrite.replace((MarkerAnnotation) simpleType.annotations().get(0), markerAnnotation, null);
		}
		String preview= evaluateRewrite(cu, rewrite);
		String str1 = """
			import java.lang.annotation.ElementType;
			
			public class X {
				public class Base {
				}
				public <X extends @Annot @Annot2 Base> void foo1 (X x) {
				}
				public <X extends @Annot2 Base> void foo2 (X x) {
				}
			}
			
			@java.lang.annotation.Target (ElementType.TYPE_USE)\s
			@interface Annot {}
			
			@java.lang.annotation.Target (ElementType.TYPE_USE)\s
			@interface Annot1 {}
			""";
		assertEqualString(preview, str1);
	}

	public void testTypeArgumentsParameterizedClassesAnnotations() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			import java.lang.annotation.ElementType;
			
			public class X {
				public class Helper <T1, T2> {
				}
				public void foo() {
					Helper<@Annot String, @Annot @Annot1 String> x;
				}
			}
			
			@java.lang.annotation.Target (ElementType.TYPE_USE)\s
			@interface Annot {}
			
			@java.lang.annotation.Target (ElementType.TYPE_USE)\s
			@interface Annot1 {}
			""";

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", str, false, null);
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDeclaration= findMethodDeclaration(type, "foo");
		List statements= methodDeclaration.getBody().statements();
		VariableDeclarationStatement  variableDeclarationStatement= (VariableDeclarationStatement) statements.get(0);
		ParameterizedType parameterizedType= (ParameterizedType) variableDeclarationStatement.getType();
		{// Add an use of annotation
			SimpleType simpleType= (SimpleType) parameterizedType.typeArguments().get(0);
			MarkerAnnotation markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot2"));
			rewrite.getListRewrite(simpleType, SimpleType.ANNOTATIONS_PROPERTY).insertLast(markerAnnotation, null);
			//Replace an use of an annotation
			markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot3"));
			rewrite.replace((ASTNode) simpleType.annotations().get(0), markerAnnotation, null);
		}
		{
			SimpleType simpleType= (SimpleType) parameterizedType.typeArguments().get(1);
			//Remove an use of an annotation
			rewrite.remove((ASTNode) simpleType.annotations().get(1), null);
			//Empty annotations list
			rewrite.remove((ASTNode) simpleType.annotations().get(0), null);
		}
		String preview= evaluateRewrite(cu, rewrite);
		String str1 = """
			import java.lang.annotation.ElementType;
			
			public class X {
				public class Helper <T1, T2> {
				}
				public void foo() {
					Helper<@Annot3 @Annot2 String, String> x;
				}
			}
			
			@java.lang.annotation.Target (ElementType.TYPE_USE)\s
			@interface Annot {}
			
			@java.lang.annotation.Target (ElementType.TYPE_USE)\s
			@interface Annot1 {}
			""";
		assertEqualString(preview, str1);
	}

	public void testTypeArgumentsMethodInvocation() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			import java.lang.annotation.ElementType;
			
			public class X {
				public class Bar {
				}
			\t
				public class Helper {
					public <T> void foo() {
					}
				}
			\t
				public void zoo() {
					Helper o = new Helper();
					o.<@Annot Bar>foo();
					o.<@Annot @Annot1 Bar>foo();
				}
			}
			
			@java.lang.annotation.Target (ElementType.TYPE_USE)\s
			@interface Annot {}
			
			@java.lang.annotation.Target (ElementType.TYPE_USE)\s
			@interface Annot1 {}
			""";

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", str, false, null);
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDeclaration = findMethodDeclaration(type, "zoo");
		List statemList = methodDeclaration.getBody().statements();
		{// Add an use of an annotation
			ExpressionStatement expression= (ExpressionStatement) statemList.get(1);
			MethodInvocation methodInvocation= (MethodInvocation) expression.getExpression();
			SimpleType simpleType = (SimpleType) methodInvocation.typeArguments().get(0);
			MarkerAnnotation markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot2"));
			rewrite.getListRewrite(simpleType, SimpleType.ANNOTATIONS_PROPERTY).insertLast(markerAnnotation, null);
		}
		{
			ExpressionStatement expression= (ExpressionStatement) statemList.get(2);
			MethodInvocation methodInvocation= (MethodInvocation) expression.getExpression();
			SimpleType simpleType = (SimpleType) methodInvocation.typeArguments().get(0);
			rewrite.remove((MarkerAnnotation)simpleType.annotations().get(1), null);
			MarkerAnnotation markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot2"));
			rewrite.replace((MarkerAnnotation)simpleType.annotations().get(0), markerAnnotation, null);
		}
		String preview= evaluateRewrite(cu, rewrite);
		String str1 = """
			import java.lang.annotation.ElementType;
			
			public class X {
				public class Bar {
				}
			\t
				public class Helper {
					public <T> void foo() {
					}
				}
			\t
				public void zoo() {
					Helper o = new Helper();
					o.<@Annot @Annot2 Bar>foo();
					o.<@Annot2 Bar>foo();
				}
			}
			
			@java.lang.annotation.Target (ElementType.TYPE_USE)\s
			@interface Annot {}
			
			@java.lang.annotation.Target (ElementType.TYPE_USE)\s
			@interface Annot1 {}
			""";
		assertEqualString(preview, str1);
	}

	public void testClassInheritenceAnnotations() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			import java.lang.annotation.ElementType;
			
			public class X {
				public interface Helper<T> {
				}
				public class Foo1<T> implements @Annot Helper<T> {
				}
				public class Foo2<T> implements @Annot @Annot1 Helper<T> {
				}
			}
			
			@java.lang.annotation.Target (ElementType.TYPE_USE)
			@interface Annot {}
			
			@java.lang.annotation.Target (ElementType.TYPE_USE)
			@interface Annot1 {}
			""";

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", str, false, null);
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		{//Add the use of an annotation
			TypeDeclaration typeDeclaration= (TypeDeclaration) type.bodyDeclarations().get(1);
			ParameterizedType parameterizedType= (ParameterizedType) typeDeclaration.superInterfaceTypes().get(0);
			SimpleType simpleType= (SimpleType) parameterizedType.getType();
			MarkerAnnotation markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot2"));
			rewrite.getListRewrite(simpleType, SimpleType.ANNOTATIONS_PROPERTY).insertLast(markerAnnotation, null);
		}
		{
			TypeDeclaration typeDeclaration= (TypeDeclaration) type.bodyDeclarations().get(2);
			ParameterizedType parameterizedType= (ParameterizedType) typeDeclaration.superInterfaceTypes().get(0);
			SimpleType simpleType= (SimpleType) parameterizedType.getType();
			//Remove the use of an annotation
			rewrite.remove((MarkerAnnotation) simpleType.annotations().get(1), null);
			//Replace the use of an annotation
			MarkerAnnotation markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot2"));
			rewrite.replace((MarkerAnnotation) simpleType.annotations().get(0), markerAnnotation, null);
		}
		String preview= evaluateRewrite(cu, rewrite);
		String str1 = """
			import java.lang.annotation.ElementType;
			
			public class X {
				public interface Helper<T> {
				}
				public class Foo1<T> implements @Annot @Annot2 Helper<T> {
				}
				public class Foo2<T> implements @Annot2 Helper<T> {
				}
			}
			
			@java.lang.annotation.Target (ElementType.TYPE_USE)
			@interface Annot {}
			
			@java.lang.annotation.Target (ElementType.TYPE_USE)
			@interface Annot1 {}
			""";
		assertEqualString(preview, str1);
	}

	public void testTypeTests() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			import java.lang.annotation.ElementType;
			
			public class X {
				public class Helper {
				}
			\t
				public void foo() {
				Helper a = new @Annot Helper();
				boolean x = true;
				x = a instanceof @Annot Helper;
				x = a instanceof @Annot @Annot1 Helper;
				}
			}
			
			@java.lang.annotation.Target (ElementType.TYPE_USE)
			@interface Annot {}
			
			@java.lang.annotation.Target (ElementType.TYPE_USE)
			@interface Annot1 {}
			""";

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", str, false, null);
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDeclaration= findMethodDeclaration(type, "foo");
		{//Add an use of annotation
			ExpressionStatement expressionStatement= (ExpressionStatement) methodDeclaration.getBody().statements().get(2);
			Assignment assignment= (Assignment) expressionStatement.getExpression();
			InstanceofExpression instanceofExpression= (InstanceofExpression) assignment.getRightHandSide();
			SimpleType simpleType = (SimpleType) instanceofExpression.getRightOperand();
			MarkerAnnotation markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot2"));
			rewrite.getListRewrite(simpleType, SimpleType.ANNOTATIONS_PROPERTY).insertLast(markerAnnotation, null);
		}
		{
			ExpressionStatement expressionStatement= (ExpressionStatement) methodDeclaration.getBody().statements().get(3);
			Assignment assignment= (Assignment) expressionStatement.getExpression();
			InstanceofExpression instanceofExpression= (InstanceofExpression) assignment.getRightHandSide();
			SimpleType simpleType = (SimpleType) instanceofExpression.getRightOperand();
			//Remove an use of annotation
			rewrite.remove((MarkerAnnotation) simpleType.annotations().get(1), null);
			MarkerAnnotation markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot2"));
			//Replace an use of annotation
			rewrite.replace((MarkerAnnotation) simpleType.annotations().get(0), markerAnnotation, null);
		}
		String preview= evaluateRewrite(cu, rewrite);
		String str1 = """
			import java.lang.annotation.ElementType;
			
			public class X {
				public class Helper {
				}
			\t
				public void foo() {
				Helper a = new @Annot Helper();
				boolean x = true;
				x = a instanceof @Annot @Annot2 Helper;
				x = a instanceof @Annot2 Helper;
				}
			}
			
			@java.lang.annotation.Target (ElementType.TYPE_USE)
			@interface Annot {}
			
			@java.lang.annotation.Target (ElementType.TYPE_USE)
			@interface Annot1 {}
			""";
		assertEqualString(preview, str1);
	}

	public void testConstructorInvocation() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			import java.lang.annotation.ElementType;
			\s
			public class X {
				public class Helper {
				}\t
				public void foo() {
					Helper obj = new @Annot Helper();
					obj = new @Annot @Annot1 Helper();
				}
			}
			
			@java.lang.annotation.Target (ElementType.TYPE_USE)
			@interface Annot {}
			
			@java.lang.annotation.Target (ElementType.TYPE_USE)
			@interface Annot1 {}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", str, false, null);
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDeclaration= findMethodDeclaration(type, "foo");
		List statements= methodDeclaration.getBody().statements();
		{//Add an use of annotation
			VariableDeclarationStatement  variableDeclarationStatement= (VariableDeclarationStatement) statements.get(0);
			VariableDeclarationFragment variableDeclarationFragment= (VariableDeclarationFragment) variableDeclarationStatement.fragments().get(0);
			ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) variableDeclarationFragment.getInitializer();
			SimpleType simpleType = (SimpleType) classInstanceCreation.getType();
			MarkerAnnotation markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot2"));
			rewrite.getListRewrite(simpleType, SimpleType.ANNOTATIONS_PROPERTY).insertLast(markerAnnotation, null);
		}
		{
			ExpressionStatement expressionStatement= (ExpressionStatement) methodDeclaration.getBody().statements().get(1);
			Assignment assignment= (Assignment) expressionStatement.getExpression();
			ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) assignment.getRightHandSide();
			SimpleType simpleType = (SimpleType) classInstanceCreation.getType();
			//Remove an use of annotation
			rewrite.remove((MarkerAnnotation) simpleType.annotations().get(1), null);
			MarkerAnnotation markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot2"));
			//Replace an use of annotation
			rewrite.replace((MarkerAnnotation) simpleType.annotations().get(0), markerAnnotation, null);
		}
		String preview= evaluateRewrite(cu, rewrite);
		String str1 = """
			import java.lang.annotation.ElementType;
			\s
			public class X {
				public class Helper {
				}\t
				public void foo() {
					Helper obj = new @Annot @Annot2 Helper();
					obj = new @Annot2 Helper();
				}
			}
			
			@java.lang.annotation.Target (ElementType.TYPE_USE)
			@interface Annot {}
			
			@java.lang.annotation.Target (ElementType.TYPE_USE)
			@interface Annot1 {}
			""";
		assertEqualString(preview, str1);
	}

	public void testConstructorDeclaration() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			import java.lang.annotation.ElementType;
			
			public class X {
				@Annot X () {
				}
				@Annot @Annot1 X (int x) {
				}
			}
			
			@java.lang.annotation.Target (ElementType.TYPE_USE)
			@interface Annot {}
			
			@java.lang.annotation.Target (ElementType.TYPE_USE)
			@interface Annot1 {}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", str, false, null);
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		{//Add the use of annotation
			MethodDeclaration methodDeclaration= (MethodDeclaration) type.bodyDeclarations().get(0);
			MarkerAnnotation markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot2"));
			rewrite.getListRewrite(methodDeclaration, MethodDeclaration.MODIFIERS2_PROPERTY).insertLast(markerAnnotation, null);
		}
		{
			//Remove the use of annotation
			MethodDeclaration methodDeclaration= (MethodDeclaration) type.bodyDeclarations().get(1);
			rewrite.remove((MarkerAnnotation) methodDeclaration.modifiers().get(1), null);
			//Replace the use of annotation
			MarkerAnnotation markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot2"));
			rewrite.replace((MarkerAnnotation) methodDeclaration.modifiers().get(0), markerAnnotation, null);
		}
		String preview= evaluateRewrite(cu, rewrite);
		String str1 = """
			import java.lang.annotation.ElementType;
			
			public class X {
				@Annot
			    @Annot2 X () {
				}
				@Annot2 X (int x) {
				}
			}
			
			@java.lang.annotation.Target (ElementType.TYPE_USE)
			@interface Annot {}
			
			@java.lang.annotation.Target (ElementType.TYPE_USE)
			@interface Annot1 {}
			""";
		assertEqualString(preview, str1);
	}

	public void testRewriteInsertAPIAnnotation() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			import java.lang.annotation.ElementType;
			
			public class X {
				public class Helper <T1, T2> {
				}
				public void foo() {
					Helper<@Annot @Annot1 String> x;
				}
			}
			
			@java.lang.annotation.Target (ElementType.TYPE_USE)\s
			@interface Annot {}
			
			@java.lang.annotation.Target (ElementType.TYPE_USE)\s
			@interface Annot1 {}
			""";

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", str, false, null);
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDeclaration= findMethodDeclaration(type, "foo");
		List statements= methodDeclaration.getBody().statements();
		VariableDeclarationStatement variableDeclarationStatement= (VariableDeclarationStatement) statements.get(0);
		ParameterizedType parameterizedType= (ParameterizedType) variableDeclarationStatement.getType();
		{
			SimpleType simpleType= (SimpleType) parameterizedType.typeArguments().get(0);
			//Insert first
			MarkerAnnotation markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot2"));
			rewrite.getListRewrite(simpleType, SimpleType.ANNOTATIONS_PROPERTY).insertFirst(markerAnnotation, null);

			//InsertAt
			markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot3"));
			rewrite.getListRewrite(simpleType, SimpleType.ANNOTATIONS_PROPERTY).insertAt(markerAnnotation, 1, null);

			//Insert after
			markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot4"));
			rewrite.getListRewrite(simpleType, SimpleType.ANNOTATIONS_PROPERTY).insertAfter(markerAnnotation, (MarkerAnnotation) simpleType.annotations().get(1), null);

			//Insert before
			markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot5"));
			rewrite.getListRewrite(simpleType, SimpleType.ANNOTATIONS_PROPERTY).insertBefore(markerAnnotation, (MarkerAnnotation) simpleType.annotations().get(1), null);
		}
		String preview= evaluateRewrite(cu, rewrite);
		String str1 = """
			import java.lang.annotation.ElementType;
			
			public class X {
				public class Helper <T1, T2> {
				}
				public void foo() {
					Helper<@Annot2 @Annot3 @Annot @Annot5 @Annot1 @Annot4 String> x;
				}
			}
			
			@java.lang.annotation.Target (ElementType.TYPE_USE)\s
			@interface Annot {}
			
			@java.lang.annotation.Target (ElementType.TYPE_USE)\s
			@interface Annot1 {}
			""";
		assertEqualString(preview, str1);
	}

	public void testEmptyListInsertAnnotation() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		String str = """
			package test1;
			public class X {
				String myObject = "Foo";
				public void foo() {
					String myString = (String) myObject;
				}
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", str, false, null);
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDeclaration= findMethodDeclaration(type, "foo");
		List statements= methodDeclaration.getBody().statements();
		{//Add an use of annotation.
			VariableDeclarationStatement  variableDeclarationStatement= (VariableDeclarationStatement) statements.get(0);
			VariableDeclarationFragment variableDeclarationFragment= (VariableDeclarationFragment) variableDeclarationStatement.fragments().get(0);
			CastExpression castExpression= (CastExpression) variableDeclarationFragment.getInitializer();
			SimpleType simpleType= (SimpleType) castExpression.getType();
			MarkerAnnotation markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Annot"));
			rewrite.getListRewrite(simpleType, SimpleType.ANNOTATIONS_PROPERTY).insertLast(markerAnnotation, null);
		}
		String preview= evaluateRewrite(cu, rewrite);
		String str1 = """
			package test1;
			public class X {
				String myObject = "Foo";
				public void foo() {
					String myString = (@Annot String) myObject;
				}
			}
			""";
		assertEqualString(preview, str1);
	}

	/**
	 * ASTRewriterTests for NameQualifiedType
	 * @throws Exception
	 *
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=406469
	 */
	public void testNameQualifiedTypeAnnotations() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test406469.bug", false, null);
		String contents = """
			package test406469.bug;
			import java.lang.annotation.*;
			public class X {
				@Target(ElementType.TYPE_USE)
				@Retention(RetentionPolicy.RUNTIME)
				@Documented
				static @interface NonNull { }
				class Inner {}
			\t
				/**
			 	* @param arg \s
			 	*/
				test406469.bug.@NonNull IOException foo(
						test406469.bug.@NonNull FileNotFoundException arg)
					throws test406469.bug.@NonNull EOFException {
					try {
						test406469.bug.@NonNull IOError e = new test406469.bug.IOError();
						throw e;
					} catch (test406469.bug.@NonNull IOError e) {
					}
					return null;
				}\s
				test406469.bug.@NonNull X.@NonNull Inner fInner;
			}\s
			@java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE_USE) @interface Marker {}\s
			
			class Outer {
				public class Inner {
					public class Deeper {}
				}
			}
			class IOException extends Exception {private static final long serialVersionUID=10001L;}
			class FileNotFoundException extends Exception{private static final long serialVersionUID=10002L;}
			class EOFException extends Exception{private static final long serialVersionUID=10003L;}
			class IOError extends Exception{private static final long serialVersionUID=10004L;}
			""";
		StringBuilder buf = new StringBuilder(contents);
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);
		CompilationUnit astRoot= createAST(cu, /* resolve */ true, false);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration typeDeclaration= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDeclaration= findMethodDeclaration(typeDeclaration, "foo");
		{   //replace an annotation.
			NameQualifiedType nameQualifiedType = (NameQualifiedType) methodDeclaration.getReturnType2();
			MarkerAnnotation markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Marker"));
			rewrite.replace((ASTNode) nameQualifiedType.annotations().get(0), markerAnnotation, null);

			// remove an annotation
			SingleVariableDeclaration param = (SingleVariableDeclaration) methodDeclaration.parameters().get(0);
			nameQualifiedType = (NameQualifiedType) param.getType();
			rewrite.remove((ASTNode) nameQualifiedType.annotations().get(0), null);

			// insert an annotation after an existing annotation
			nameQualifiedType = (NameQualifiedType) methodDeclaration.thrownExceptionTypes().get(0);
			markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Marker"));
			rewrite.getListRewrite(nameQualifiedType, NameQualifiedType.ANNOTATIONS_PROPERTY).insertLast(markerAnnotation, null);

			/* insert an annotation in a type not converted as a NameQualifiedType. This would involve
			 *  creation of a NameQualifiedType from fields of the existing type.
			 */
			TryStatement tryStatement = (TryStatement) methodDeclaration.getBody().statements().get(0);
			VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) tryStatement.getBody().statements().get(0);
			VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) variableDeclarationStatement.fragments().get(0);
			ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) variableDeclarationFragment.getInitializer();
			SimpleType simpleType = (SimpleType) classInstanceCreation.getType();
			QualifiedName qualifiedName = (QualifiedName) simpleType.getName();
			SimpleName simpleName = ast.newSimpleName(qualifiedName.getName().getIdentifier());
			qualifiedName = (QualifiedName) qualifiedName.getQualifier();
			qualifiedName = ast.newQualifiedName(ast.newName(qualifiedName.getQualifier().toString()), ast.newSimpleName(qualifiedName.getName().toString()));
			nameQualifiedType = ast.newNameQualifiedType(qualifiedName, simpleName);
			markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Marker"));
			rewrite.getListRewrite(nameQualifiedType, NameQualifiedType.ANNOTATIONS_PROPERTY).insertLast(markerAnnotation, null);
			rewrite.replace(classInstanceCreation.getType(), nameQualifiedType, null);
		}
		String preview= evaluateRewrite(cu, rewrite);
		String contentsmodified = """
			package test406469.bug;
			import java.lang.annotation.*;
			public class X {
				@Target(ElementType.TYPE_USE)
				@Retention(RetentionPolicy.RUNTIME)
				@Documented
				static @interface NonNull { }
				class Inner {}
			\t
				/**
			 	* @param arg \s
			 	*/
				test406469.bug.@Marker IOException foo(
						test406469.bug.FileNotFoundException arg)
					throws test406469.bug.@NonNull @Marker EOFException {
					try {
						test406469.bug.@NonNull IOError e = new test406469.bug.@Marker IOError();
						throw e;
					} catch (test406469.bug.@NonNull IOError e) {
					}
					return null;
				}\s
				test406469.bug.@NonNull X.@NonNull Inner fInner;
			}\s
			@java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE_USE) @interface Marker {}\s
			
			class Outer {
				public class Inner {
					public class Deeper {}
				}
			}
			class IOException extends Exception {private static final long serialVersionUID=10001L;}
			class FileNotFoundException extends Exception{private static final long serialVersionUID=10002L;}
			class EOFException extends Exception{private static final long serialVersionUID=10003L;}
			class IOError extends Exception{private static final long serialVersionUID=10004L;}
			""";
		assertEqualString(preview, contentsmodified);
	}

	/**
	 * ASTRewriterTests for QualifiedType
	 * @throws Exception
	 *
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=407364
	 */
	public void testQualifiedTypeAnnotations() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test407364.bug", false, null);
		String contents =  """
			package test0002;
			import java.lang.annotation.Target;
			public class X {
				public static void main(String[] args) {
					Outer outer = new Outer();
					Outer.@Marker1 Inner first = outer.new Inner();
					Outer.@Marker2 Inner second = outer.new Inner() ;
					Outer.Inner.@Marker1 Deeper deeper = second.new Deeper();
					Outer.Inner.Deeper deeper2 =  second.new Deeper();
				}
			}
			class Outer {
				public class Inner {
					public class Deeper {
					}
				}
			}
			@Target (java.lang.annotation.ElementType.TYPE_USE)
			@interface Marker {}
			@Target (java.lang.annotation.ElementType.TYPE_USE)
			@interface Marker1 {}
			@Target (java.lang.annotation.ElementType.TYPE_USE)
			@interface Marker2 {}
			""";

		StringBuilder buf = new StringBuilder(contents);
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);
		CompilationUnit astRoot= createAST(cu, /* resolve */ true, false);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		TypeDeclaration typeDeclaration= findTypeDeclaration(astRoot, "X");
		MethodDeclaration methodDeclaration= findMethodDeclaration(typeDeclaration, "main");
		List statements = methodDeclaration.getBody().statements();
		int sCount = 1;

		{   //replace an annotation.
			VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) statements.get(sCount++);
			NameQualifiedType nameQualifiedType = (NameQualifiedType) variableDeclarationStatement.getType();
			MarkerAnnotation markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("NewMarker"));
			rewrite.replace((ASTNode) nameQualifiedType.annotations().get(0), markerAnnotation, null);

			// remove an annotation
			variableDeclarationStatement = (VariableDeclarationStatement) statements.get(sCount++);
			nameQualifiedType = (NameQualifiedType) variableDeclarationStatement.getType();
			rewrite.remove((ASTNode) nameQualifiedType.annotations().get(0), null);

			// insert an annotation after an existing annotation
			variableDeclarationStatement = (VariableDeclarationStatement) statements.get(sCount++);
			nameQualifiedType = (NameQualifiedType) variableDeclarationStatement.getType();
			markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("NewMarker"));
			rewrite.getListRewrite(nameQualifiedType, NameQualifiedType.ANNOTATIONS_PROPERTY).insertLast(markerAnnotation, null);

			/* insert an annotation in a type not converted as QualifiedType. This would involve
			 *  creation of a QualifiedType from fields of the existing type.
			 */
			variableDeclarationStatement = (VariableDeclarationStatement) statements.get(sCount++);
			SimpleType simpleType = (SimpleType) variableDeclarationStatement.getType();
			QualifiedName qualifiedName = (QualifiedName) simpleType.getName();
			SimpleName simpleName = ast.newSimpleName(qualifiedName.getName().getIdentifier());
			qualifiedName = (QualifiedName) qualifiedName.getQualifier();
			qualifiedName = ast.newQualifiedName(ast.newName(qualifiedName.getQualifier().toString()), ast.newSimpleName(qualifiedName.getName().toString()));
			nameQualifiedType = ast.newNameQualifiedType(qualifiedName, simpleName);

			markerAnnotation= ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("NewMarker"));
			rewrite.getListRewrite(nameQualifiedType, NameQualifiedType.ANNOTATIONS_PROPERTY).insertLast(markerAnnotation, null);
			rewrite.replace(variableDeclarationStatement.getType(), nameQualifiedType, null);
		}
		String preview= evaluateRewrite(cu, rewrite);
		String contentsmodified = """
			package test0002;
			import java.lang.annotation.Target;
			public class X {
				public static void main(String[] args) {
					Outer outer = new Outer();
					Outer.@NewMarker Inner first = outer.new Inner();
					Outer.Inner second = outer.new Inner() ;
					Outer.Inner.@Marker1 @NewMarker Deeper deeper = second.new Deeper();
					Outer.Inner.@NewMarker Deeper deeper2 =  second.new Deeper();
				}
			}
			class Outer {
				public class Inner {
					public class Deeper {
					}
				}
			}
			@Target (java.lang.annotation.ElementType.TYPE_USE)
			@interface Marker {}
			@Target (java.lang.annotation.ElementType.TYPE_USE)
			@interface Marker1 {}
			@Target (java.lang.annotation.ElementType.TYPE_USE)
			@interface Marker2 {}
			""";
		assertEqualString(preview, contentsmodified);
	}
}
