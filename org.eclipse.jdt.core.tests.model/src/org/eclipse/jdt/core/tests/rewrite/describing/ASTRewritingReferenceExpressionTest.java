/*******************************************************************************
 * Copyright (c) 2013, 2015 IBM Corporation and others.
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

import java.util.Iterator;

import junit.framework.Test;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings("rawtypes")
public class ASTRewritingReferenceExpressionTest extends ASTRewritingTest {

	public ASTRewritingReferenceExpressionTest(String name) {
		super(name);
	}

	public ASTRewritingReferenceExpressionTest(String name, int apiLevel) {
		super(name, apiLevel);
	}

	public static Test suite() {
		return createSuite(ASTRewritingReferenceExpressionTest.class);
	}

	/**
	 * tests various aspects of CreationReference (Constructor Method Reference) with ClassType as lhs
	 */
	public void testReferenceExpressions_test001_since_8() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test407131", false, null);
		String contents = """
			package test407131;
			
			interface J {
				void foo(int x);
			}
			class Y {
				static class Z {
					public Z(int x) {
						System.out.print(x);
					}
					public void foo(int x) {
						System.out.print(x);
					}
				}
			}
			class W<T> {
				public W(int x) {}
			}
			
			public class X {
				@SuppressWarnings("unused")
				public static void main (String [] args) {
					J j1 = W <Integer> :: <String> new;
					J j2 = W <Integer> :: <String> new ;
					J j3 = W <Integer> :: <String, Integer> new ;
					J j4 = W <Integer> :: <String> new ;
					J j5 = W <Integer> :: <String> new ;
					J j6 = W <Integer> :: new;
					J j7 = W <Integer> :: new;
					J j8 = W <Integer> :: new;
				}
			}
			
			""" ;
		StringBuilder buf= new StringBuilder(contents);
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();

		TypeDeclaration typedeclaration= findTypeDeclaration(astRoot, "X");

		MethodDeclaration methodDecl= findMethodDeclaration(typedeclaration, "main");
		{
			// case 1: replace the type argument.
			VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) methodDecl.getBody().statements().get(0);
			VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) variableDeclarationStatement.fragments().get(0);
			CreationReference creationReference = (CreationReference) variableDeclarationFragment.getInitializer();
			TypeParameter typeParameter= ast.newTypeParameter();
			typeParameter.setName(ast.newSimpleName("Integer"));
			ListRewrite listRewrite = rewrite.getListRewrite(creationReference, CreationReference.TYPE_ARGUMENTS_PROPERTY);
			listRewrite.replace((ASTNode)(creationReference.typeArguments().get(0)), typeParameter, null);

			// case 2: add a type argument.
			variableDeclarationStatement = (VariableDeclarationStatement) methodDecl.getBody().statements().get(1);
			variableDeclarationFragment = (VariableDeclarationFragment) variableDeclarationStatement.fragments().get(0);
			creationReference = (CreationReference) variableDeclarationFragment.getInitializer();
			SimpleType simpleType = ast.newSimpleType(ast.newSimpleName("Integer"));
			listRewrite = rewrite.getListRewrite(creationReference, CreationReference.TYPE_ARGUMENTS_PROPERTY);
			listRewrite.insertLast(simpleType, null);

			// case 3: delete a type argument.
			variableDeclarationStatement = (VariableDeclarationStatement) methodDecl.getBody().statements().get(2);
			variableDeclarationFragment = (VariableDeclarationFragment) variableDeclarationStatement.fragments().get(0);
			creationReference = (CreationReference) variableDeclarationFragment.getInitializer();
			listRewrite = rewrite.getListRewrite(creationReference, CreationReference.TYPE_ARGUMENTS_PROPERTY);
			listRewrite.remove((ASTNode)(creationReference.typeArguments().get(1)), null);

			// case 4: do not change existing type arg - implicit test case.
			// case 5: delete the only type argument.
			variableDeclarationStatement = (VariableDeclarationStatement) methodDecl.getBody().statements().get(4);
			variableDeclarationFragment = (VariableDeclarationFragment) variableDeclarationStatement.fragments().get(0);
			creationReference = (CreationReference) variableDeclarationFragment.getInitializer();
			listRewrite = rewrite.getListRewrite(creationReference, CreationReference.TYPE_ARGUMENTS_PROPERTY);
			for (Iterator iter= listRewrite.getOriginalList().iterator(); iter.hasNext(); ) {//loop added for general case - iterates once here.
				ASTNode typeArgument= (ASTNode) iter.next();
				listRewrite.remove(typeArgument, null);
			}

			// case 6: do not change a CreationReference originally having no type arguments - implicit test case.
			// case 7: Insert a type argument for a CreationReference originally having no type arguments.
			variableDeclarationStatement = (VariableDeclarationStatement) methodDecl.getBody().statements().get(6);
			variableDeclarationFragment = (VariableDeclarationFragment) variableDeclarationStatement.fragments().get(0);
			creationReference = (CreationReference) variableDeclarationFragment.getInitializer();
			listRewrite = rewrite.getListRewrite(creationReference, CreationReference.TYPE_ARGUMENTS_PROPERTY);
			simpleType = ast.newSimpleType(ast.newSimpleName("String"));
			listRewrite.insertFirst(simpleType, null);

			// case 8: change a CreationReference to a TypeMethodReference.
			variableDeclarationStatement = (VariableDeclarationStatement) methodDecl.getBody().statements().get(7);
			variableDeclarationFragment = (VariableDeclarationFragment) variableDeclarationStatement.fragments().get(0);
			creationReference = (CreationReference) variableDeclarationFragment.getInitializer();
			simpleType = ast.newSimpleType(ast.newQualifiedName(ast.newSimpleName("Y"), ast.newSimpleName("Z")));
			rewrite.replace(creationReference.getType(), simpleType, null);
		}

		String preview= evaluateRewrite(cu, rewrite);
		contents = """
			package test407131;
			
			interface J {
				void foo(int x);
			}
			class Y {
				static class Z {
					public Z(int x) {
						System.out.print(x);
					}
					public void foo(int x) {
						System.out.print(x);
					}
				}
			}
			class W<T> {
				public W(int x) {}
			}
			
			public class X {
				@SuppressWarnings("unused")
				public static void main (String [] args) {
					J j1 = W <Integer> :: <Integer> new;
					J j2 = W <Integer> :: <String, Integer> new ;
					J j3 = W <Integer> :: <String> new ;
					J j4 = W <Integer> :: <String> new ;
					J j5 = W <Integer> :: new ;
					J j6 = W <Integer> :: new;
					J j7 = W <Integer> ::<String> new;
					J j8 = Y.Z :: new;
				}
			}
			
			""" ;
		buf= new StringBuilder(contents);
		assertEqualString(preview, buf.toString());
	}

	/**
	 * tests various aspects of CreationReference (Constructor Method Reference) with ArrayType as lhs
	 */
	public void testReferenceExpressions_test002_since_8() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test407131", false, null);
		String contents = """
			package test407131;
			interface I {
				Object foo(int x);
			}
			public class X {
				@SuppressWarnings("unused")
				public static void main (String [] args) {
					I i1 = int[] :: new;
				}
			}
			
			""" ;
		StringBuilder buf= new StringBuilder(contents);
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();

		TypeDeclaration typedeclaration= findTypeDeclaration(astRoot, "X");

		MethodDeclaration methodDecl= findMethodDeclaration(typedeclaration, "main");
		{
			// replace the primitive type of lhs from int to char.
			VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) methodDecl.getBody().statements().get(0);
			VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) variableDeclarationStatement.fragments().get(0);
			CreationReference creationReference = (CreationReference) variableDeclarationFragment.getInitializer();
			ArrayType arrayType = (ArrayType) creationReference.getType();
			rewrite.replace(arrayType.getElementType(), ast.newPrimitiveType(PrimitiveType.CHAR), null);
		}
		String preview= evaluateRewrite(cu, rewrite);
		contents = """
			package test407131;
			interface I {
				Object foo(int x);
			}
			public class X {
				@SuppressWarnings("unused")
				public static void main (String [] args) {
					I i1 = char[] :: new;
				}
			}
			
			""" ;
		buf= new StringBuilder(contents);
		assertEqualString(preview, buf.toString());
	}

	/**
	 * tests various aspects of {@link ExpressionMethodReference}
	 */
	public void testReferenceExpressions_test003_since_8() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test407131", false, null);
		String contents = """
			package test407131;
			interface J {
				void foo(int x);
			}
			class Y {
				public void foo(int x) {}
			}
			class Z {
				public void bar(int x) {}
			}
			
			public class X {
				@SuppressWarnings("unused")
				public static void main (String [] args) {
					Y y = new Y();
					Z z = new Z();
					J j1 = y :: foo;
					J j2 = z :: <String> bar;
				}
			}
			
			""" ;
		StringBuilder buf= new StringBuilder(contents);
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();

		TypeDeclaration typedeclaration= findTypeDeclaration(astRoot, "X");

		MethodDeclaration methodDecl= findMethodDeclaration(typedeclaration, "main");
		{
			// replace the lhs and the rhs.
			VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) methodDecl.getBody().statements().get(2);
			VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) variableDeclarationStatement.fragments().get(0);
			ExpressionMethodReference expressionMethodReference = (ExpressionMethodReference) variableDeclarationFragment.getInitializer();
			SimpleType simpleType = ast.newSimpleType((ast.newSimpleName("String")));
			rewrite.replace(expressionMethodReference.getExpression(), ast.newSimpleName("z"), null);
			rewrite.getListRewrite(expressionMethodReference, ExpressionMethodReference.TYPE_ARGUMENTS_PROPERTY).insertFirst(simpleType, null);
			rewrite.replace(expressionMethodReference.getName(), ast.newSimpleName("bar"), null);

			variableDeclarationStatement = (VariableDeclarationStatement) methodDecl.getBody().statements().get(3);
			variableDeclarationFragment = (VariableDeclarationFragment) variableDeclarationStatement.fragments().get(0);
			expressionMethodReference = (ExpressionMethodReference) variableDeclarationFragment.getInitializer();
			rewrite.replace(expressionMethodReference.getExpression(), ast.newSimpleName("y"), null);
			ASTNode typeArgument = (ASTNode) expressionMethodReference.typeArguments().get(0);
			rewrite.getListRewrite(expressionMethodReference, ExpressionMethodReference.TYPE_ARGUMENTS_PROPERTY).remove(typeArgument, null);
			rewrite.replace(expressionMethodReference.getName(), ast.newSimpleName("foo"), null);

		}
		String preview= evaluateRewrite(cu, rewrite);
		contents = """
			package test407131;
			interface J {
				void foo(int x);
			}
			class Y {
				public void foo(int x) {}
			}
			class Z {
				public void bar(int x) {}
			}
			
			public class X {
				@SuppressWarnings("unused")
				public static void main (String [] args) {
					Y y = new Y();
					Z z = new Z();
					J j1 = z ::<String> bar;
					J j2 = y :: foo;
				}
			}
			
			""" ;
		buf= new StringBuilder(contents);
		assertEqualString(preview, buf.toString());
	}

	/**
	 * tests various aspects of {@link TypeMethodReference}
	 */
	public void testReferenceExpressions_test004_since_8() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test407131", false, null);
		String contents = """
			package test407131;
			package test407131;
			
			interface J {
				void foo(int x);
			}
			class Y {
				static class Z {
					public Z(int x) {}
					public static void foo(int x) {}
				}
			}
			class W<T> {
				public static void bar(int x) {}
			}
			
			public class X {
				@SuppressWarnings("unused")
				public static void main (String [] args) {
					J j1 = Y.@Marker Z :: foo;
					J j2 = @Marker W :: <Integer> bar;
				}
			@Target (ElementType.TYPE_USE)
			@interface @Marker {}
			}
			""" ;
		StringBuilder buf= new StringBuilder(contents);
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();

		TypeDeclaration typedeclaration= findTypeDeclaration(astRoot, "X");

		MethodDeclaration methodDecl= findMethodDeclaration(typedeclaration, "main");
		{
			// replace the lhs and the rhs.
			VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) methodDecl.getBody().statements().get(0);
			VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) variableDeclarationStatement.fragments().get(0);
			TypeMethodReference typeMethodReference = (TypeMethodReference) variableDeclarationFragment.getInitializer();
			SimpleType simpleType = ast.newSimpleType((ast.newSimpleName("Integer")));
			Type newType = ast.newSimpleType(ast.newSimpleName("W"));
			rewrite.replace(typeMethodReference.getType(), newType, null);
			rewrite.getListRewrite(typeMethodReference, TypeMethodReference.TYPE_ARGUMENTS_PROPERTY).insertFirst(simpleType, null);
			rewrite.replace(typeMethodReference.getName(), ast.newSimpleName("bar"), null);

			variableDeclarationStatement = (VariableDeclarationStatement) methodDecl.getBody().statements().get(1);
			variableDeclarationFragment = (VariableDeclarationFragment) variableDeclarationStatement.fragments().get(0);
			typeMethodReference = (TypeMethodReference) variableDeclarationFragment.getInitializer();
			newType = ast.newSimpleType(ast.newQualifiedName(ast.newSimpleName("Y"),ast.newSimpleName("Z")));
			rewrite.replace(typeMethodReference.getType(), newType, null);
			ASTNode typeArgument = (ASTNode) typeMethodReference.typeArguments().get(0);
			rewrite.getListRewrite(typeMethodReference, TypeMethodReference.TYPE_ARGUMENTS_PROPERTY).remove(typeArgument, null);
			rewrite.replace(typeMethodReference.getName(), ast.newSimpleName("foo"), null);
		}
		String preview= evaluateRewrite(cu, rewrite);
		contents = """
			package test407131;
			package test407131;
			
			interface J {
				void foo(int x);
			}
			class Y {
				static class Z {
					public Z(int x) {}
					public static void foo(int x) {}
				}
			}
			class W<T> {
				public static void bar(int x) {}
			}
			
			public class X {
				@SuppressWarnings("unused")
				public static void main (String [] args) {
					J j1 = W ::<Integer> bar;
					J j2 = Y.Z :: foo;
				}
			@Target (ElementType.TYPE_USE)
			@interface @Marker {}
			}
			""" ;
		buf= new StringBuilder(contents);
		assertEqualString(preview, buf.toString());
	}

	/**
	 * tests various aspects of SuperMethodReference
	 */
	public void testReferenceExpressions_test005_since_8() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test407131", false, null);
		String contents = """
			package test407131;
			interface J {
				void foo(int x);
			}
			
			class XX {
				public void foo(int x) {}
				public void bar(int x) {}
			}
			
			public class X extends XX {
			       @SuppressWarnings("unused")
			       public  void bar(int i) {
			           J jx = super :: <Integer> foo;
			           J jz = X.super :: bar;
			       }
			       public static void main (String [] args) {}
			}
			
			""" ;
		StringBuilder buf= new StringBuilder(contents);
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();

		TypeDeclaration typedeclaration= findTypeDeclaration(astRoot, "X");

		MethodDeclaration methodDecl= findMethodDeclaration(typedeclaration, "bar");
		{
			// replace the lhs and the rhs, add/delete type arguments.
			VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) methodDecl.getBody().statements().get(0);
			VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) variableDeclarationStatement.fragments().get(0);
			SuperMethodReference superMethodReference = (SuperMethodReference) variableDeclarationFragment.getInitializer();
			rewrite.set(superMethodReference, SuperMethodReference.QUALIFIER_PROPERTY, ast.newSimpleName("X"), null);
			ASTNode typeArgument = (ASTNode) superMethodReference.typeArguments().get(0);
			rewrite.getListRewrite(superMethodReference, SuperMethodReference.TYPE_ARGUMENTS_PROPERTY).remove(typeArgument, null);
			rewrite.replace(superMethodReference.getName(), ast.newSimpleName("bar"), null);

			variableDeclarationStatement = (VariableDeclarationStatement) methodDecl.getBody().statements().get(1);
			variableDeclarationFragment = (VariableDeclarationFragment) variableDeclarationStatement.fragments().get(0);
			superMethodReference = (SuperMethodReference) variableDeclarationFragment.getInitializer();
			rewrite.remove(superMethodReference.getQualifier(), null);
			SimpleType simpleType = ast.newSimpleType(ast.newSimpleName("String"));
			rewrite.getListRewrite(superMethodReference, SuperMethodReference.TYPE_ARGUMENTS_PROPERTY).insertFirst(simpleType, null);
			rewrite.replace(superMethodReference.getName(), ast.newSimpleName("foo"), null);

		}
		String preview = evaluateRewrite(cu, rewrite);
		contents = """
			package test407131;
			interface J {
				void foo(int x);
			}
			
			class XX {
				public void foo(int x) {}
				public void bar(int x) {}
			}
			
			public class X extends XX {
			       @SuppressWarnings("unused")
			       public  void bar(int i) {
			           J jx = X.super :: bar;
			           J jz = super ::<String> foo;
			       }
			       public static void main (String [] args) {}
			}
			
			""" ;
		buf = new StringBuilder(contents);
		assertEqualString(preview, buf.toString());
	}
	/**
	 * tests cross rewriting - changing from one member of reference expressions to another type.
	 */
	public void testReferenceExpressions_test006_since_8() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test407131", false, null);
		this.project1.setOption(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_8);
		String contents = """
			package test407131;
			import java.lang.annotation.*;
			interface J {
				void foo(int x);
			}
			class Y {
				static class Z {
					public Z(int x) {}
					public static void foo(int x) {}
				}
			}
			class W<T> {
				public W(int x) {}
				public static void bar(int x) {}
			}
			
			class XX {
				public void foo(int x) {}
				public void bar(int x) {}
			}
			
			public class X extends XX {
				public static void main (String [] args) {}
				@SuppressWarnings("unused")
				public void bar () {
					J j1 = W <Integer> :: <String> new;
					J j2 = Y.Z :: new;
					J j3 = Y.@Marker Z :: foo;
					J jx = super :: foo;
					J jz = X.super :: bar;
				}
			}
			@Target (ElementType.TYPE_USE)
			@interface Marker {}
			}
			""";
		StringBuilder buf= new StringBuilder(contents);
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);
		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();

		TypeDeclaration typedeclaration= findTypeDeclaration(astRoot, "X");

		MethodDeclaration methodDecl= findMethodDeclaration(typedeclaration, "bar");
		{
			// case 1: creationReference to TypeMethodReference
			VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) methodDecl.getBody().statements().get(0);
			VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) variableDeclarationStatement.fragments().get(0);
			CreationReference creationReference = (CreationReference) variableDeclarationFragment.getInitializer();
			TypeMethodReference typeMethodReference = ast.newTypeMethodReference();
			QualifiedType qualifiedType = ast.newQualifiedType(ast.newSimpleType(ast.newSimpleName("Y")), ast.newSimpleName("Z"));
			MarkerAnnotation markerAnnotation = ast.newMarkerAnnotation();
			markerAnnotation.setTypeName(ast.newSimpleName("Marker"));
			qualifiedType.annotations().add(markerAnnotation);
			typeMethodReference.setType(qualifiedType);
			typeMethodReference.setName(ast.newSimpleName("foo"));
			rewrite.replace(creationReference, typeMethodReference, null);

			// case 2: CreationReference to SuperMethodReference
			variableDeclarationStatement = (VariableDeclarationStatement) methodDecl.getBody().statements().get(1);
			variableDeclarationFragment = (VariableDeclarationFragment) variableDeclarationStatement.fragments().get(0);
			creationReference = (CreationReference) variableDeclarationFragment.getInitializer();
			SuperMethodReference superMethodReference = ast.newSuperMethodReference();
			superMethodReference.setName(ast.newSimpleName("foo"));
			rewrite.replace(creationReference, superMethodReference, null);

			// case 3: TypeMethodReference to SuperMethodReference
			variableDeclarationStatement = (VariableDeclarationStatement) methodDecl.getBody().statements().get(2);
			variableDeclarationFragment = (VariableDeclarationFragment) variableDeclarationStatement.fragments().get(0);
			typeMethodReference = (TypeMethodReference) variableDeclarationFragment.getInitializer();
			superMethodReference = ast.newSuperMethodReference();
			superMethodReference.setName(ast.newSimpleName("bar"));
			superMethodReference.setQualifier(ast.newSimpleName("X"));
			rewrite.replace(typeMethodReference, superMethodReference, null);

			// case 4: SuperMethodReference to CreationMethodReference
			variableDeclarationStatement = (VariableDeclarationStatement) methodDecl.getBody().statements().get(3);
			variableDeclarationFragment = (VariableDeclarationFragment) variableDeclarationStatement.fragments().get(0);
			superMethodReference = (SuperMethodReference) variableDeclarationFragment.getInitializer();
			ParameterizedType parameterizedType = ast.newParameterizedType(ast.newSimpleType(ast.newSimpleName("W")));
			parameterizedType.typeArguments().add(ast.newSimpleType(ast.newSimpleName("Integer")));
			creationReference = ast.newCreationReference();
			creationReference.setType(parameterizedType);
			creationReference.typeArguments().add(ast.newSimpleType(ast.newSimpleName("String")));
			rewrite.replace(superMethodReference, creationReference, null);

			// case 5: SuperMethodReference to ExpressionMethodReference
			variableDeclarationStatement = (VariableDeclarationStatement) methodDecl.getBody().statements().get(4);
			variableDeclarationFragment = (VariableDeclarationFragment) variableDeclarationStatement.fragments().get(0);
			superMethodReference = (SuperMethodReference) variableDeclarationFragment.getInitializer();
			ExpressionMethodReference expressionMethodReference = ast.newExpressionMethodReference();
			expressionMethodReference.setExpression(ast.newQualifiedName(ast.newSimpleName("Y"), ast.newSimpleName("Z")));
			expressionMethodReference.setName(ast.newSimpleName("foo"));
			rewrite.replace(superMethodReference, expressionMethodReference, null);

		}

		String preview= evaluateRewrite(cu, rewrite);
		contents = """
			package test407131;
			import java.lang.annotation.*;
			interface J {
				void foo(int x);
			}
			class Y {
				static class Z {
					public Z(int x) {}
					public static void foo(int x) {}
				}
			}
			class W<T> {
				public W(int x) {}
				public static void bar(int x) {}
			}
			
			class XX {
				public void foo(int x) {}
				public void bar(int x) {}
			}
			
			public class X extends XX {
				public static void main (String [] args) {}
				@SuppressWarnings("unused")
				public void bar () {
					J j1 = Y.@Marker Z::foo;
					J j2 = super::foo;
					J j3 = X.super::bar;
					J jx = W<Integer>::<String>new;
					J jz = Y.Z::foo;
				}
			}
			@Target (ElementType.TYPE_USE)
			@interface Marker {}
			}
			""";
		buf= new StringBuilder(contents);
		assertEqualString(preview, buf.toString());
	}
}
