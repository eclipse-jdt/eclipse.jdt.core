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
package org.eclipse.jdt.core.tests.rewrite.describing;
import java.util.List;
import junit.framework.Test;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

@SuppressWarnings("rawtypes")
public class ASTRewritingRecordAnnotationsTest extends ASTRewritingTest {

	public ASTRewritingRecordAnnotationsTest(String name) {
		super(name);
	}
	public ASTRewritingRecordAnnotationsTest(String name, int apiLevel) {
		super(name, apiLevel);
	}

	public static Test suite() {
		return createSuite(ASTRewritingRecordAnnotationsTest.class, getAST16());
	}

	@SuppressWarnings("deprecation")
	protected static int getAST16() {
		return AST.JLS16;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		setUpProjectAbove16();
	}

	@SuppressWarnings("deprecation")
	private boolean checkAPILevel() {
		if (this.apiLevel < 16) {
			System.err.println("Test "+getName()+" requires a JRE 16");
			return true;
		}
		return false;
	}

	public void testCastAnnotations() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("public record X() {\n");
		buf.append("	static String myObject = \"Foo\";\n");
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
		assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		RecordDeclaration type= (RecordDeclaration)findAbstractTypeDeclaration(astRoot, "X");
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
		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("public record X() {\n");
		buf.append("	static String myObject = \"Foo\";\n");
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
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("public record X() {\n");
		buf.append("	public record Helper<T>() {\n");
		buf.append("	}\n");
		buf.append("	public record Base() {\n");
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
		RecordDeclaration type= (RecordDeclaration)findAbstractTypeDeclaration(astRoot, "X");
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
		buf = new StringBuilder();
		buf.append("package test1;\n");
		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("public record X() {\n");
		buf.append("	public record Helper<T>() {\n");
		buf.append("	}\n");
		buf.append("	public record Base() {\n");
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
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();

		buf.append("public record X() {\n");
		buf.append("	public record Helper<T>() {\n");
		buf.append("	}\n");
		buf.append("	public record Base() {\n");
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
		RecordDeclaration type= (RecordDeclaration)findAbstractTypeDeclaration(astRoot, "X");
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
		buf = new StringBuilder();
		buf.append("public record X() {\n");
		buf.append("	public record Helper<T>() {\n");
		buf.append("	}\n");
		buf.append("	public record Base() {\n");
		buf.append("	}\n");
		buf.append("	public static void foo1 (Helper<? extends @Annot @Annot2 Base> x) {\n");
		buf.append("	}\n");
		buf.append("	public static void foo2 (Helper<? extends @Annot2 Base> x) {\n");
		buf.append("	}\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testTypeParameterBoundAnnotations() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();

		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("\n");
		buf.append("public record X() {\n");
		buf.append("	public record Base() {\n");
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
		RecordDeclaration type= (RecordDeclaration)findAbstractTypeDeclaration(astRoot, "X");
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
		buf = new StringBuilder();
		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("\n");
		buf.append("public record X() {\n");
		buf.append("	public record Base() {\n");
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
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();

		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("\n");
		buf.append("public record X() {\n");
		buf.append("	public record Helper <T1, T2>() {\n");
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
		RecordDeclaration type= (RecordDeclaration)findAbstractTypeDeclaration(astRoot, "X");
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
		buf = new StringBuilder();
		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("\n");
		buf.append("public record X() {\n");
		buf.append("	public record Helper <T1, T2>() {\n");
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
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();

		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("\n");
		buf.append("public record X() {\n");
		buf.append("	public record Bar() {\n");
		buf.append("	}\n");
		buf.append("	\n");
		buf.append("	public record Helper() {\n");
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
		RecordDeclaration type= (RecordDeclaration)findAbstractTypeDeclaration(astRoot, "X");
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
		buf = new StringBuilder();
		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("\n");
		buf.append("public record X() {\n");
		buf.append("	public record Bar() {\n");
		buf.append("	}\n");
		buf.append("	\n");
		buf.append("	public record Helper() {\n");
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
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();

		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("\n");
		buf.append("public record X() {\n");
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
		RecordDeclaration type= (RecordDeclaration)findAbstractTypeDeclaration(astRoot, "X");
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
		buf = new StringBuilder();
		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("\n");
		buf.append("public record X() {\n");
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
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();

		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("\n");
		buf.append("public record X() {\n");
		buf.append("	public record Helper() {\n");
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
		RecordDeclaration type= (RecordDeclaration)findAbstractTypeDeclaration(astRoot, "X");
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
		buf = new StringBuilder();
		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("\n");
		buf.append("public record X() {\n");
		buf.append("	public record Helper() {\n");
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
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append(" \n");
		buf.append("public record X() {\n");
		buf.append("	public record Helper() {\n");
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
		RecordDeclaration type= (RecordDeclaration)findAbstractTypeDeclaration(astRoot, "X");
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
		buf = new StringBuilder();
		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append(" \n");
		buf.append("public record X() {\n");
		buf.append("	public record Helper() {\n");
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
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("\n");
		buf.append("public record X() {\n");
		buf.append("	@Annot X {\n");
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
		RecordDeclaration type= (RecordDeclaration)findAbstractTypeDeclaration(astRoot, "X");
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
		buf = new StringBuilder();
		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("\n");
		buf.append("public record X() {\n");
		buf.append("	@Annot\n");
		buf.append("    @Annot2 X {\n");
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
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();

		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("\n");
		buf.append("public record X() {\n");
		buf.append("	public record Helper <T1, T2>() {\n");
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
		RecordDeclaration type= (RecordDeclaration)findAbstractTypeDeclaration(astRoot, "X");
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
		buf = new StringBuilder();
		buf.append("import java.lang.annotation.ElementType;\n");
		buf.append("\n");
		buf.append("public record X() {\n");
		buf.append("	public record Helper <T1, T2>() {\n");
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

	public void testEmptyListInsertAnnotation() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public record X() {\n");
		buf.append("	static String myObject = \"Foo\";\n");
		buf.append("	public void foo() {\n");
		buf.append("		String myString = (String) myObject;\n");
		buf.append("	}\n");
		buf.append("}\n");

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		RecordDeclaration type= (RecordDeclaration)findAbstractTypeDeclaration(astRoot, "X");
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
		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("public record X() {\n");
		buf.append("	static String myObject = \"Foo\";\n");
		buf.append("	public void foo() {\n");
		buf.append("		String myString = (@Annot String) myObject;\n");
		buf.append("	}\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testNameQualifiedTypeAnnotations() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test406469.bug", false, null);
		String contents = "package test406469.bug;\n" +
				"import java.lang.annotation.*;\n" +
				"public record X() {\n" +
				"	@Target(ElementType.TYPE_USE)\n" +
				"	@Retention(RetentionPolicy.RUNTIME)\n" +
				"	@Documented\n" +
				"	static @interface NonNull { }\n" +
				"	class Inner {}\n" +
				"	\n" +
				"	/**\n" +
				" 	* @param arg  \n" +
				" 	*/\n" +
				"	test406469.bug.@NonNull IOException foo(\n" +
				"			test406469.bug.@NonNull FileNotFoundException arg)\n" +
				"		throws test406469.bug.@NonNull EOFException {\n" +
				"		try {\n" +
				"			test406469.bug.@NonNull IOError e = new test406469.bug.IOError();\n" +
				"			throw e;\n" +
				"		} catch (test406469.bug.@NonNull IOError e) {\n" +
				"		}\n" +
				"		return null;\n" +
				"	} \n" +
				"	test406469.bug.@NonNull X.@NonNull Inner fInner;\n" +
				"} \n" +
				"@java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE_USE) @interface Marker {} \n" +
				"\n" +
				"class Outer {\n" +
				"	public class Inner {\n" +
				"		public class Deeper {}\n" +
				"	}\n" +
				"}\n" +
				"class IOException extends Exception {private static final long serialVersionUID=10001L;}\n" +
				"class FileNotFoundException extends Exception{private static final long serialVersionUID=10002L;}\n" +
				"class EOFException extends Exception{private static final long serialVersionUID=10003L;}\n" +
				"class IOError extends Exception{private static final long serialVersionUID=10004L;}\n";
		StringBuilder buf = new StringBuilder(contents);
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);
		CompilationUnit astRoot= createAST(cu, /* resolve */ true, false);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		RecordDeclaration typeDeclaration= (RecordDeclaration)findAbstractTypeDeclaration(astRoot, "X");
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
		String contentsmodified = "package test406469.bug;\n" +
				"import java.lang.annotation.*;\n" +
				"public record X() {\n" +
				"	@Target(ElementType.TYPE_USE)\n" +
				"	@Retention(RetentionPolicy.RUNTIME)\n" +
				"	@Documented\n" +
				"	static @interface NonNull { }\n" +
				"	class Inner {}\n" +
				"	\n" +
				"	/**\n" +
				" 	* @param arg  \n" +
				" 	*/\n" +
				"	test406469.bug.@Marker IOException foo(\n" +
				"			test406469.bug.FileNotFoundException arg)\n" +
				"		throws test406469.bug.@NonNull @Marker EOFException {\n" +
				"		try {\n" +
				"			test406469.bug.@NonNull IOError e = new test406469.bug.@Marker IOError();\n" +
				"			throw e;\n" +
				"		} catch (test406469.bug.@NonNull IOError e) {\n" +
				"		}\n" +
				"		return null;\n" +
				"	} \n" +
				"	test406469.bug.@NonNull X.@NonNull Inner fInner;\n" +
				"} \n" +
				"@java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE_USE) @interface Marker {} \n" +
				"\n" +
				"class Outer {\n" +
				"	public class Inner {\n" +
				"		public class Deeper {}\n" +
				"	}\n" +
				"}\n" +
				"class IOException extends Exception {private static final long serialVersionUID=10001L;}\n" +
				"class FileNotFoundException extends Exception{private static final long serialVersionUID=10002L;}\n" +
				"class EOFException extends Exception{private static final long serialVersionUID=10003L;}\n" +
				"class IOError extends Exception{private static final long serialVersionUID=10004L;}\n";
		assertEqualString(preview, contentsmodified);
	}

	public void testQualifiedTypeAnnotations() throws Exception {
		if (checkAPILevel()) {
			return;
		}
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test407364.bug", false, null);
		String contents =  "package test0002;\n" +
				 "import java.lang.annotation.Target;\n" +
				 "public record X() {\n" +
				 "	public static void main(String[] args) {\n" +
				 "		Outer outer = new Outer();\n" +
				 "		Outer.@Marker1 Inner first = outer.new Inner();\n" +
				 "		Outer.@Marker2 Inner second = outer.new Inner() ;\n" +
				 "		Outer.Inner.@Marker1 Deeper deeper = second.new Deeper();\n" +
				 "		Outer.Inner.Deeper deeper2 =  second.new Deeper();\n" +
				 "	}\n" + "}\n" + "class Outer {\n" +
				 "	public class Inner {\n" +
				 "		public class Deeper {\n" +
				 "		}\n" +
				 "	}\n" +
				 "}\n" +
				 "@Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				 "@interface Marker {}\n" +
				 "@Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				 "@interface Marker1 {}\n" +
				 "@Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				 "@interface Marker2 {}\n";

		StringBuilder buf = new StringBuilder(contents);
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);
		CompilationUnit astRoot= createAST(cu, /* resolve */ true, false);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();
		RecordDeclaration typeDeclaration= (RecordDeclaration)findAbstractTypeDeclaration(astRoot, "X");
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
		String contentsmodified = "package test0002;\n" +
				 "import java.lang.annotation.Target;\n" +
				 "public record X() {\n" +
				 "	public static void main(String[] args) {\n" +
				 "		Outer outer = new Outer();\n" +
				 "		Outer.@NewMarker Inner first = outer.new Inner();\n" +
				 "		Outer.Inner second = outer.new Inner() ;\n" +
				 "		Outer.Inner.@Marker1 @NewMarker Deeper deeper = second.new Deeper();\n" +
				 "		Outer.Inner.@NewMarker Deeper deeper2 =  second.new Deeper();\n" +
				 "	}\n" + "}\n" + "class Outer {\n" +
				 "	public class Inner {\n" +
				 "		public class Deeper {\n" +
				 "		}\n" +
				 "	}\n" +
				 "}\n" +
				 "@Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				 "@interface Marker {}\n" +
				 "@Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				 "@interface Marker1 {}\n" +
				 "@Target (java.lang.annotation.ElementType.TYPE_USE)\n" +
				 "@interface Marker2 {}\n";
		assertEqualString(preview, contentsmodified);
	}
}
