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
package org.eclipse.jdt.core.tests.rewrite.describing;
import java.util.List;

import junit.framework.Test;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

public class ASTRewritingTypeAnnotationsTest extends ASTRewritingTest {

	public ASTRewritingTypeAnnotationsTest(String name) {
		super(name);
	}
	public ASTRewritingTypeAnnotationsTest(String name, int apiLevel) {
		super(name, apiLevel);
	}

	public static Test suite() {
		return createSuite(ASTRewritingTypeAnnotationsTest.class);
	}

	public void testCastAnnotations() throws Exception {
		if (this.apiLevel < AST.JLS8) return;
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("public class X {\n");
		buf.append("	String myObject = \"Foo\";\n");
		buf.append("	public void foo() {\n");
		buf.append("		String myString = (@Annot String) myObject;\n");
		buf.append("		String myString1 = (@Annot1 @Annot String) myObject;\n");
		buf.append("	}\n");
		buf.append("}\n");
		buf.append("@java.lang.annotation.Target(value = {ElementType.TYPE_USE})\n");
		buf.append("@interface Annot {}\n");
		buf.append("@java.lang.annotation.Target(value = {ElementType.TYPE_USE})\n");
		buf.append("@interface Annot1 {}\n");

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);
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
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("public class X {\n");
		buf.append("	String myObject = \"Foo\";\n");
		buf.append("	public void foo() {\n");
		buf.append("		String myString = (@Annot @Annot2 String) myObject;\n");
		buf.append("		String myString1 = (@Annot2 String) myObject;\n");
		buf.append("	}\n");
		buf.append("}\n");
		buf.append("@java.lang.annotation.Target(value = {ElementType.TYPE_USE})\n");
		buf.append("@interface Annot {}\n");
		buf.append("@java.lang.annotation.Target(value = {ElementType.TYPE_USE})\n");
		buf.append("@interface Annot1 {}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testWildcardTypeArgumentAnnotations() throws Exception {
		if (this.apiLevel < AST.JLS8) return;
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("public class X {\n");
		buf.append("	public class Helper<T> {\n");
		buf.append("	}\n");
		buf.append("	public class Base {\n");
		buf.append("	}\n");
		buf.append("	public static void UnboundedWildcard1 (Helper<@Annot ?> x) {\n");
		buf.append("	}\n");
		buf.append("	public static void UnboundedWildcard2 (Helper<@Annot1 @Annot ?> x) {\n");
		buf.append("	}\n");
		buf.append("	public static void BoundedWildcard1 (Helper<@Annot ? extends Base> x) {\n");
		buf.append("	}\n");
		buf.append("	public static void BoundedWildcard2 (Helper<@Annot1 @Annot ? extends Base> x) {\n");
		buf.append("	}\n");
		buf.append("}\n");
		buf.append("@java.lang.annotation.Target (ElementType.TYPE_USE)\n");
		buf.append("@interface Annot {}\n");
		buf.append("@java.lang.annotation.Target (ElementType.TYPE_USE)\n");
		buf.append("@interface Annot1 {}\n");

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);
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
		buf = new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("public class X {\n");
		buf.append("	public class Helper<T> {\n");
		buf.append("	}\n");
		buf.append("	public class Base {\n");
		buf.append("	}\n");
		buf.append("	public static void UnboundedWildcard1 (Helper<@Annot @Annot2 ?> x) {\n");
		buf.append("	}\n");
		buf.append("	public static void UnboundedWildcard2 (Helper<@Annot2 ?> x) {\n");
		buf.append("	}\n");
		buf.append("	public static void BoundedWildcard1 (Helper<@Annot @Annot2 ? extends Base> x) {\n");
		buf.append("	}\n");
		buf.append("	public static void BoundedWildcard2 (Helper<@Annot2 ? extends Base> x) {\n");
		buf.append("	}\n");
		buf.append("}\n");
		buf.append("@java.lang.annotation.Target (ElementType.TYPE_USE)\n");
		buf.append("@interface Annot {}\n");
		buf.append("@java.lang.annotation.Target (ElementType.TYPE_USE)\n");
		buf.append("@interface Annot1 {}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testWildcardBoudAnnotation() throws Exception {
		if (this.apiLevel < AST.JLS8) return;
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();

		buf.append("public class X {\n");
		buf.append("	public class Helper<T> {\n");
		buf.append("	}\n");
		buf.append("	public class Base {\n");
		buf.append("	}\n");
		buf.append("	public static void foo1 (Helper<? extends @Annot Base> x) {\n");
		buf.append("	}\n");
		buf.append("	public static void foo2 (Helper<? extends @Annot1 @Annot Base> x) {\n");
		buf.append("	}\n");
		buf.append("}\n");

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);
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
		buf = new StringBuffer();
		buf.append("public class X {\n");
		buf.append("	public class Helper<T> {\n");
		buf.append("	}\n");
		buf.append("	public class Base {\n");
		buf.append("	}\n");
		buf.append("	public static void foo1 (Helper<? extends @Annot @Annot2 Base> x) {\n");
		buf.append("	}\n");
		buf.append("	public static void foo2 (Helper<? extends @Annot2 Base> x) {\n");
		buf.append("	}\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testTypeParameterBoundAnnotations() throws Exception {
		if (this.apiLevel < AST.JLS8) return;
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();

		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("	public class Base {\n");
		buf.append("	}\n");
		buf.append("	public <X extends @Annot Base> void foo1 (X x) {\n");
		buf.append("	}\n");
		buf.append("	public <X extends @Annot1 @Annot Base> void foo2 (X x) {\n");
		buf.append("	}\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("@java.lang.annotation.Target (ElementType.TYPE_USE) \n");
		buf.append("@interface Annot {}\n");
		buf.append("\n");
		buf.append("@java.lang.annotation.Target (ElementType.TYPE_USE) \n");
		buf.append("@interface Annot1 {}\n");

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);
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
		buf = new StringBuffer();
		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("	public class Base {\n");
		buf.append("	}\n");
		buf.append("	public <X extends @Annot @Annot2 Base> void foo1 (X x) {\n");
		buf.append("	}\n");
		buf.append("	public <X extends @Annot2 Base> void foo2 (X x) {\n");
		buf.append("	}\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("@java.lang.annotation.Target (ElementType.TYPE_USE) \n");
		buf.append("@interface Annot {}\n");
		buf.append("\n");
		buf.append("@java.lang.annotation.Target (ElementType.TYPE_USE) \n");
		buf.append("@interface Annot1 {}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testTypeArgumentsParameterizedClassesAnnotations() throws Exception {
		if (this.apiLevel < AST.JLS8) return;
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();

		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("	public class Helper <T1, T2> {\n");
		buf.append("	}\n");
		buf.append("	public void foo() {\n");
		buf.append("		Helper<@Annot String, @Annot @Annot1 String> x;\n");
		buf.append("	}\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("@java.lang.annotation.Target (ElementType.TYPE_USE) \n");
		buf.append("@interface Annot {}\n");
		buf.append("\n");
		buf.append("@java.lang.annotation.Target (ElementType.TYPE_USE) \n");
		buf.append("@interface Annot1 {}\n");

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);
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
		buf = new StringBuffer();
		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("	public class Helper <T1, T2> {\n");
		buf.append("	}\n");
		buf.append("	public void foo() {\n");
		buf.append("		Helper<@Annot3 @Annot2 String, String> x;\n");
		buf.append("	}\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("@java.lang.annotation.Target (ElementType.TYPE_USE) \n");
		buf.append("@interface Annot {}\n");
		buf.append("\n");
		buf.append("@java.lang.annotation.Target (ElementType.TYPE_USE) \n");
		buf.append("@interface Annot1 {}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testTypeArgumentsMethodInvocation() throws Exception {
		if (this.apiLevel < AST.JLS8) return;
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();

		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("	public class Bar {\n");
		buf.append("	}\n");
		buf.append("	\n");
		buf.append("	public class Helper {\n");
		buf.append("		public <T> void foo() {\n");
		buf.append("		}\n");
		buf.append("	}\n");
		buf.append("	\n");
		buf.append("	public void zoo() {\n");
		buf.append("		Helper o = new Helper();\n");
		buf.append("		o.<@Annot Bar>foo();\n");
		buf.append("		o.<@Annot @Annot1 Bar>foo();\n");
		buf.append("	}\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("@java.lang.annotation.Target (ElementType.TYPE_USE) \n");
		buf.append("@interface Annot {}\n");
		buf.append("\n");
		buf.append("@java.lang.annotation.Target (ElementType.TYPE_USE) \n");
		buf.append("@interface Annot1 {}\n");

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);
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
		buf = new StringBuffer();
		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("	public class Bar {\n");
		buf.append("	}\n");
		buf.append("	\n");
		buf.append("	public class Helper {\n");
		buf.append("		public <T> void foo() {\n");
		buf.append("		}\n");
		buf.append("	}\n");
		buf.append("	\n");
		buf.append("	public void zoo() {\n");
		buf.append("		Helper o = new Helper();\n");
		buf.append("		o.<@Annot @Annot2 Bar>foo();\n");
		buf.append("		o.<@Annot2 Bar>foo();\n");
		buf.append("	}\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("@java.lang.annotation.Target (ElementType.TYPE_USE) \n");
		buf.append("@interface Annot {}\n");
		buf.append("\n");
		buf.append("@java.lang.annotation.Target (ElementType.TYPE_USE) \n");
		buf.append("@interface Annot1 {}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testClassInheritenceAnnotations() throws Exception {
		if (this.apiLevel < AST.JLS8) return;
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();

		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("	public interface Helper<T> {\n");
		buf.append("	}\n");
		buf.append("	public class Foo1<T> implements @Annot Helper<T> {\n");
		buf.append("	}\n");
		buf.append("	public class Foo2<T> implements @Annot @Annot1 Helper<T> {\n");
		buf.append("	}\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("@java.lang.annotation.Target (ElementType.TYPE_USE)\n");
		buf.append("@interface Annot {}\n");
		buf.append("\n");
		buf.append("@java.lang.annotation.Target (ElementType.TYPE_USE)\n");
		buf.append("@interface Annot1 {}\n");

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);
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
		buf = new StringBuffer();
		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("	public interface Helper<T> {\n");
		buf.append("	}\n");
		buf.append("	public class Foo1<T> implements @Annot @Annot2 Helper<T> {\n");
		buf.append("	}\n");
		buf.append("	public class Foo2<T> implements @Annot2 Helper<T> {\n");
		buf.append("	}\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("@java.lang.annotation.Target (ElementType.TYPE_USE)\n");
		buf.append("@interface Annot {}\n");
		buf.append("\n");
		buf.append("@java.lang.annotation.Target (ElementType.TYPE_USE)\n");
		buf.append("@interface Annot1 {}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testTypeTests() throws Exception {
		if (this.apiLevel < AST.JLS8) return;
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();

		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("	public class Helper {\n");
		buf.append("	}\n");
		buf.append("	\n");
		buf.append("	public void foo() {\n");
		buf.append("	Helper a = new @Annot Helper();\n");
		buf.append("	boolean x = true;\n");
		buf.append("	x = a instanceof @Annot Helper;\n");
		buf.append("	x = a instanceof @Annot @Annot1 Helper;\n");
		buf.append("	}\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("@java.lang.annotation.Target (ElementType.TYPE_USE)\n");
		buf.append("@interface Annot {}\n");
		buf.append("\n");
		buf.append("@java.lang.annotation.Target (ElementType.TYPE_USE)\n");
		buf.append("@interface Annot1 {}\n");

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);
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
		buf = new StringBuffer();
		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("	public class Helper {\n");
		buf.append("	}\n");
		buf.append("	\n");
		buf.append("	public void foo() {\n");
		buf.append("	Helper a = new @Annot Helper();\n");
		buf.append("	boolean x = true;\n");
		buf.append("	x = a instanceof @Annot @Annot2 Helper;\n");
		buf.append("	x = a instanceof @Annot2 Helper;\n");
		buf.append("	}\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("@java.lang.annotation.Target (ElementType.TYPE_USE)\n");
		buf.append("@interface Annot {}\n");
		buf.append("\n");
		buf.append("@java.lang.annotation.Target (ElementType.TYPE_USE)\n");
		buf.append("@interface Annot1 {}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testConstructorInvocation() throws Exception {
		if (this.apiLevel < AST.JLS8) return;
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append(" \n");
		buf.append("public class X {\n");
		buf.append("	public class Helper {\n");
		buf.append("	}	\n");
		buf.append("	public void foo() {\n");
		buf.append("		Helper obj = new @Annot Helper();\n");
		buf.append("		obj = new @Annot @Annot1 Helper();\n");
		buf.append("	}\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("@java.lang.annotation.Target (ElementType.TYPE_USE)\n");
		buf.append("@interface Annot {}\n");
		buf.append("\n");
		buf.append("@java.lang.annotation.Target (ElementType.TYPE_USE)\n");
		buf.append("@interface Annot1 {}\n");

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);
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
		buf = new StringBuffer();
		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append(" \n");
		buf.append("public class X {\n");
		buf.append("	public class Helper {\n");
		buf.append("	}	\n");
		buf.append("	public void foo() {\n");
		buf.append("		Helper obj = new @Annot @Annot2 Helper();\n");
		buf.append("		obj = new @Annot2 Helper();\n");
		buf.append("	}\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("@java.lang.annotation.Target (ElementType.TYPE_USE)\n");
		buf.append("@interface Annot {}\n");
		buf.append("\n");
		buf.append("@java.lang.annotation.Target (ElementType.TYPE_USE)\n");
		buf.append("@interface Annot1 {}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testConstructorDeclaration() throws Exception {
		if (this.apiLevel < AST.JLS8) return;
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("	@Annot X () {\n");
		buf.append("	}\n");
		buf.append("	@Annot @Annot1 X (int x) {\n");
		buf.append("	}\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("@java.lang.annotation.Target (ElementType.TYPE_USE)\n");
		buf.append("@interface Annot {}\n");
		buf.append("\n");
		buf.append("@java.lang.annotation.Target (ElementType.TYPE_USE)\n");
		buf.append("@interface Annot1 {}\n");

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);
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
		buf = new StringBuffer();
		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("	@Annot\n");
		buf.append("    @Annot2 X () {\n");
		buf.append("	}\n");
		buf.append("	@Annot2 X (int x) {\n");
		buf.append("	}\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("@java.lang.annotation.Target (ElementType.TYPE_USE)\n");
		buf.append("@interface Annot {}\n");
		buf.append("\n");
		buf.append("@java.lang.annotation.Target (ElementType.TYPE_USE)\n");
		buf.append("@interface Annot1 {}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testRewriteInsertAPIAnnotation() throws Exception {
		if (this.apiLevel < AST.JLS8) return;
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();

		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("	public class Helper <T1, T2> {\n");
		buf.append("	}\n");
		buf.append("	public void foo() {\n");
		buf.append("		Helper<@Annot @Annot1 String> x;\n");
		buf.append("	}\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("@java.lang.annotation.Target (ElementType.TYPE_USE) \n");
		buf.append("@interface Annot {}\n");
		buf.append("\n");
		buf.append("@java.lang.annotation.Target (ElementType.TYPE_USE) \n");
		buf.append("@interface Annot1 {}\n");

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);
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
		buf = new StringBuffer();
		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("\n");
		buf.append("public class X {\n");
		buf.append("	public class Helper <T1, T2> {\n");
		buf.append("	}\n");
		buf.append("	public void foo() {\n");
		buf.append("		Helper<@Annot2 @Annot3 @Annot @Annot5 @Annot1 @Annot4 String> x;\n");
		buf.append("	}\n");
		buf.append("}\n");
		buf.append("\n");
		buf.append("@java.lang.annotation.Target (ElementType.TYPE_USE) \n");
		buf.append("@interface Annot {}\n");
		buf.append("\n");
		buf.append("@java.lang.annotation.Target (ElementType.TYPE_USE) \n");
		buf.append("@interface Annot1 {}\n");
		assertEqualString(preview, buf.toString());
	}

	public void _testEmptyListInsertAnnotation() throws Exception {
		if (this.apiLevel < AST.JLS8) return;
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class X {\n");
		buf.append("	String myObject = \"Foo\";\n");
		buf.append("	public void foo() {\n");
		buf.append("		String myString = (String) myObject;\n");
		buf.append("	}\n");
		buf.append("}\n");

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);
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
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class X {\n");
		buf.append("	String myObject = \"Foo\";\n");
		buf.append("	public void foo() {\n");
		buf.append("		String myString = (@Annot String) myObject;\n");//Actual: String myString = ( @AnnotString) myObject;
		buf.append("	}\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

}