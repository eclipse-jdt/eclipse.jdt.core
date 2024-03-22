/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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
 *     Stephan Herrmann - Contribution for
 *								Bug 425183 - [1.8][inference] make CaptureBinding18 safe
 *******************************************************************************/
package org.eclipse.jdt.core.tests.dom;

import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.BindingKey;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTRequestor;
import org.eclipse.jdt.core.dom.AnnotatableType;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.CreationReference;
import org.eclipse.jdt.core.dom.Dimension;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.IntersectionType;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.MethodRefParameter;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NameQualifiedType;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperMethodReference;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeMethodReference;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WildcardType;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.core.ResolvedBinaryMethod;

import junit.framework.Test;

@SuppressWarnings({"rawtypes"})
public class ASTConverter18Test extends ConverterTestSetup {

	ICompilationUnit workingCopy;

	@Override
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		this.ast = AST.newAST(getAST8(), false);
	}

	public ASTConverter18Test(String name) {
		super(name);
	}

	static {
//		TESTS_NUMBERS = new int[] { 19 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] {"testLambdaSynthetic"};
	}
	public static Test suite() {
		return buildModelTestSuite(ASTConverter18Test.class);
	}
	/**
	 * @deprecated
	 */
	static int getAST8() {
		return AST.JLS8;
	}
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		if (this.workingCopy != null) {
			this.workingCopy.discardWorkingCopy();
			this.workingCopy = null;
		}
	}

	/*
	 * Type Annotations on Variable Arguments
	 */
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=391898
	public void test0001() throws JavaModelException {
		String contents =
			"""
			    @java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)
			    @interface Marker {
			    }
			public class X {
				public void foo(int @Marker... args) {
				}
			 	public void bar(@Marker int @Marker... args) {
			 	}
			}""";
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 1, 0);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION);
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		List parameters = methodDeclaration.parameters();
		assertEquals("wrong size", 1, parameters.size());
		SingleVariableDeclaration parameter = (SingleVariableDeclaration) parameters.get(0);
		List annotations = parameter.varargsAnnotations();
		assertEquals("Wrong number of annotations", 1, annotations.size());
		ASTNode annotation = (ASTNode) annotations.get(0);
		checkSourceRange(annotation, "@Marker", contents);
		node = getASTNode(compilationUnit,1,1);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION);
		parameters = methodDeclaration.parameters();
		assertEquals("Wrong number of parameters", 1, parameters.size());
		parameter = (SingleVariableDeclaration) parameters.get(0);
		annotations = parameter.varargsAnnotations();
		assertEquals("Wrong number of annotations", 1, annotations.size());
		annotation = (ASTNode) annotations.get(0);
		checkSourceRange(annotation, "@Marker", contents);
	}
	/*
	 * Type Annotations on Variable Argument of ArrayType
	 */
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=413569
	public void test413569() throws JavaModelException {
		String contents =
			"""
			import java.lang.annotation.*;
			public class X {
				@Target(ElementType.TYPE_USE) static @interface A {}
				@Target(ElementType.TYPE_USE) static @interface B {}
				@Target(ElementType.TYPE_USE) static @interface C { Class<?> value() default Object.class; }
				@Target(ElementType.TYPE_USE) static @interface D { Class<?> d(); }
				void foo(@A int @B()[] @C(int[].class) [] @D(d=String[].class)... arg) {}
			}""";
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 4);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION);
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		List parameters = methodDeclaration.parameters();
		assertEquals("wrong size", 1, parameters.size());
		SingleVariableDeclaration parameter = (SingleVariableDeclaration) parameters.get(0);

		ArrayType type = (ArrayType) parameter.getType();
		List dimensions = type.dimensions();
		assertEquals(2, dimensions.size());

		Dimension dimension = (Dimension) dimensions.get(0);
		List annotations = dimension.annotations();
		assertEquals("Wrong number of annotations", 1, annotations.size());
		Annotation annotation = (Annotation) annotations.get(0);
		checkSourceRange(annotation, "@B()", contents);

		dimension = (Dimension) dimensions.get(1);
		annotations = dimension.annotations();
		assertEquals("Wrong number of annotations", 1, annotations.size());
		annotation = (Annotation) annotations.get(0);
		checkSourceRange(annotation, "@C(int[].class)", contents);

		annotations = parameter.varargsAnnotations();
		assertEquals("Wrong number of annotations", 1, annotations.size());
		annotation = (Annotation) annotations.get(0);
		checkSourceRange(annotation, "@D(d=String[].class)", contents);

	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=395886 tests annotations on
	 * QTR in multiple scenarios of occurrence.
	 */
	public void test0002() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter18/src/test0002/X.java",
				true/* resolve */);
		String contents = """
			package test0002;
			import java.lang.annotation.Target;
			public class X {
				public static void main(String[] args) {
					Outer outer = new Outer();
					Object myObject = new Object();
					String myString;
					myString = (java.lang.@Marker String) myObject;
					Outer.Inner first = outer.new Inner();
					Outer. @Marker2 Inner second = outer.new Inner() ;
					Outer.Inner. @Marker1 Deeper deeper = second.new Deeper();
					Outer.@Marker1 Inner.@Marker2 Deeper deeper2 =  second.new Deeper();
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
		CompilationUnit cu = (CompilationUnit) buildAST(contents,
				this.workingCopy);
		MethodDeclaration methodDeclaration = (MethodDeclaration) getASTNode(cu, 0, 0);
		List statements = methodDeclaration.getBody().statements();
		int sCount = 3;

		// case 1 - annotation just before the last field
		ExpressionStatement expressionStatement = (ExpressionStatement) statements.get(sCount++);
		Assignment assignment = (Assignment) expressionStatement.getExpression();
		assertNotNull(assignment);
		CastExpression castExpression = (CastExpression) assignment.getRightHandSide();
		assertNotNull(castExpression);
		NameQualifiedType nameQualifiedType = (NameQualifiedType) castExpression.getType();
		assertNotNull(nameQualifiedType);
		assertEquals("java.lang.@Marker String", nameQualifiedType.toString());
		List annotations = nameQualifiedType.annotations();
		assertTrue(annotations.size() == 1);
		assertEquals("@Marker", annotations.get(0).toString());

		// case 2 - QualifiedType without annotations.
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) statements.get(sCount++);
		Type type = variableDeclarationStatement.getType();
		assertTrue(type.isSimpleType());
		SimpleType simpleType = (SimpleType) type;
		assertEquals("Outer.Inner", simpleType.toString());
		annotations = simpleType.annotations();
		assertTrue(annotations.size() == 0);

		// case 3 - Qaulified Type with outer without annotations and inner with
		// annotations.
		variableDeclarationStatement = (VariableDeclarationStatement) statements.get(sCount++);
		type = variableDeclarationStatement.getType();
		assertTrue(type.isNameQualifiedType());
		nameQualifiedType = (NameQualifiedType) type;
		assertNotNull(nameQualifiedType);
		assertEquals("Outer.@Marker2 Inner", nameQualifiedType.toString());
		annotations = nameQualifiedType.annotations();
		assertTrue(annotations.size() == 1);
		assertEquals("@Marker2", annotations.get(0).toString());

		// case 4 - Multiple levels with annotations at the last only.
		variableDeclarationStatement = (VariableDeclarationStatement) statements.get(sCount++);
		type = variableDeclarationStatement.getType();
		assertTrue(type.isNameQualifiedType());
		nameQualifiedType = (NameQualifiedType) type;
		assertNotNull(nameQualifiedType);
		assertEquals("Outer.Inner.@Marker1 Deeper", nameQualifiedType.toString());
		annotations = nameQualifiedType.annotations();
		assertTrue(annotations.size() == 1);
		assertEquals("@Marker1", annotations.get(0).toString());

		// case 5 - Multiple annotations
		variableDeclarationStatement = (VariableDeclarationStatement) statements.get(sCount++);
		type = variableDeclarationStatement.getType();
		assertTrue(type.isQualifiedType());
		QualifiedType qualifiedType = (QualifiedType) type;
		assertNotNull(qualifiedType);
		assertEquals("Outer.@Marker1 Inner.@Marker2 Deeper", qualifiedType.toString());
		annotations = qualifiedType.annotations();
		assertTrue(annotations.size() == 1);
		assertEquals("@Marker2", annotations.get(0).toString());
		SimpleName simpleName = qualifiedType.getName();
		assertEquals("Deeper", simpleName.toString());
		Type qualifierType = qualifiedType.getQualifier();
		assertTrue(qualifierType.isNameQualifiedType());
		nameQualifiedType = (NameQualifiedType) qualifierType;
		assertEquals("Outer.@Marker1 Inner", nameQualifiedType.toString());
		annotations = nameQualifiedType.annotations();
		assertTrue(annotations.size() == 1);
		assertEquals("@Marker1", annotations.get(0).toString());
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=395886 tests the
	 * representation of type annotations on a possible JAVA 7 and 8 place.
	 */
	public void test0003() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter18/src/test0003/X.java",
				true/* resolve */);
		String contents = """
			package test0003;
			import java.lang.annotation.Target;
			public class X {
				public static void main(String[] args) {
					@Marker Outer.Inner first[] = new Outer.Inner[1];
				}
			}
			class Outer {
				public class Inner {
				}
			}
			@Target (java.lang.annotation.ElementType.TYPE_USE)
			@interface Marker {}
			""";
		CompilationUnit cu = (CompilationUnit) buildAST(contents, this.workingCopy);
		MethodDeclaration methodDeclaration = (MethodDeclaration) getASTNode(cu, 0, 0);
		List statements = methodDeclaration.getBody().statements();
		int sCount = 0;

		// Current design expects annotation only at the JAVA 7 place if it is
		// expected at JAVA 8.
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) statements.get(sCount);
		List modifiers = variableDeclarationStatement.modifiers();
		assertTrue(modifiers.size() == 1);
		Annotation annotation = (Annotation) modifiers.get(0);
		assertEquals("@Marker", annotation.toString());
		Type type = variableDeclarationStatement.getType();
		assertTrue(type.isSimpleType());
		SimpleType simpleType = (SimpleType) type;
		assertEquals("Outer.Inner", simpleType.toString());
		List annotations = simpleType.annotations();
		assertTrue(annotations.size() == 0);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=395886 tests QTR with
	 * annotations
	 */
	public void test0004() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter18/src/test0004/X.java",
				true/* resolve */);
		String contents = """
			package test0004;\
			import java.lang.annotation.Target;
			public class X implements One</*start*/@Marker1 Outer<Integer>. @Marker2 Inner<Double>[]/*end*/> {
			}
			interface One<T> {}
			class Outer<T> {
				public class Inner<S> {}
			}
			@Target (java.lang.annotation.ElementType.TYPE_USE)
			@interface Marker1 {}
			@Target (java.lang.annotation.ElementType.TYPE_USE)
			@interface Marker2 {}
			""";
		ArrayType type = (ArrayType) buildAST(contents, this.workingCopy);
		assertNotNull("No annotation", type);
		ITypeBinding binding = type.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong qualified name", "test0004.Outer<java.lang.Integer>.Inner<java.lang.Double>[]", binding.getQualifiedName());
		Type elementType = type.getElementType();
		binding = elementType.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong qualified name",
				"test0004.Outer<java.lang.Integer>.Inner<java.lang.Double>", binding.getQualifiedName());
		assertTrue("Not parameterized", elementType.isParameterizedType());
		ParameterizedType parameterizedType = (ParameterizedType) elementType;
		Type type2 = parameterizedType.getType();
		assertTrue("Not qualified", type2.isQualifiedType());
		QualifiedType qualifiedType = (QualifiedType) type2;
		binding = qualifiedType.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong qualified name","test0004.Outer<java.lang.Integer>.Inner<java.lang.Double>", binding.getQualifiedName());
		Type qualifier = qualifiedType.getQualifier();
		assertTrue("Not parameterized", qualifier.isParameterizedType());
		binding = qualifier.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong qualified name", "test0004.Outer<java.lang.Integer>", binding.getQualifiedName());
		parameterizedType = (ParameterizedType) qualifier;
		type2 = parameterizedType.getType();
		assertTrue("Not simple type", type2.isSimpleType());
		binding = type2.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong qualified name","test0004.Outer<java.lang.Integer>", binding.getQualifiedName());
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=395886 tests QTR with
	 * annotations
	 */
	public void test0005() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter18/src/test0005/X.java",
				true/* resolve */);
		String contents = """
			package test0005;\
			import java.lang.annotation.Target;
			public class X implements One< Outer.Inner > {
			}
			class Y implements One< Outer. @Marker1 Inner > {
			}
			class Z implements One< @Marker1 Outer.Inner > {
			}
			class W implements One< @Marker1 Outer. @Marker2 Inner > {
			}
			interface One<T> {}
			class Outer {
				public class Inner {}
			}
			@Target (java.lang.annotation.ElementType.TYPE_USE)
			@interface Marker1 {}
			@Target (java.lang.annotation.ElementType.TYPE_USE)
			@interface Marker2 {}
			""";
		CompilationUnit cu = (CompilationUnit) buildAST(contents,
				this.workingCopy);
		int tCount = 0;

		// case 1 - no annotations Outer.Inner
		TypeDeclaration typedeclaration = (TypeDeclaration) getASTNode(cu, tCount++);
		ParameterizedType parametrizedType = (ParameterizedType) typedeclaration.superInterfaceTypes().get(0);
		List typeArguments = parametrizedType.typeArguments();
		assertEquals(1, typeArguments.size());
		Type type = (Type) typeArguments.get(0);
		assertTrue(type.isSimpleType());
		assertEquals("Outer.Inner", type.toString());

		// case 2 - QTR with one annotation Outer.@Marker1 Inner
		typedeclaration = (TypeDeclaration) getASTNode(cu, tCount++);
		parametrizedType = (ParameterizedType) typedeclaration
				.superInterfaceTypes().get(0);
		typeArguments = parametrizedType.typeArguments();
		assertEquals(1, typeArguments.size());
		type = (Type) typeArguments.get(0);
		assertTrue(type.isNameQualifiedType());
		NameQualifiedType nameQualifiedType = (NameQualifiedType) type;
		assertEquals("Outer.@Marker1 Inner", nameQualifiedType.toString());
		List annotations = nameQualifiedType.annotations();
		assertTrue(annotations.size() == 1);
		Annotation annotation = (Annotation) annotations.get(0);
		assertEquals("@Marker1", annotation.toString());

		// case 3 - QTR with one annotation at the beginning @Marker1
		// Outer.Inner
		typedeclaration = (TypeDeclaration) getASTNode(cu, tCount++);
		parametrizedType = (ParameterizedType) typedeclaration.superInterfaceTypes().get(0);
		typeArguments = parametrizedType.typeArguments();
		assertEquals(1, typeArguments.size());
		type = (Type) typeArguments.get(0);
		assertTrue(type.isQualifiedType());
		assertEquals("@Marker1 Outer.Inner", type.toString());
		QualifiedType qualifiedType = (QualifiedType) type;
		assertEquals("Inner", qualifiedType.getName().toString());
		annotations = qualifiedType.annotations();
		assertTrue(annotations.size() == 0);
		Type qualifierType = qualifiedType.getQualifier();
		assertTrue(qualifierType.isSimpleType());
		SimpleType simpleType = (SimpleType) qualifierType;
		assertEquals("@Marker1 Outer", simpleType.toString());
		annotations = simpleType.annotations();
		assertTrue(annotations.size() == 1);
		annotation = (Annotation) annotations.get(0);
		assertEquals("@Marker1", annotation.toString());

		// case 4 - QTR with annotations at both the types @Marker1
		// Outer.@Marker2 Inner
		typedeclaration = (TypeDeclaration) getASTNode(cu, tCount++);
		parametrizedType = (ParameterizedType) typedeclaration.superInterfaceTypes().get(0);
		typeArguments = parametrizedType.typeArguments();
		assertEquals(1, typeArguments.size());
		type = (Type) typeArguments.get(0);
		assertTrue(type.isQualifiedType());
		assertEquals("@Marker1 Outer.@Marker2 Inner", type.toString());
		qualifiedType = (QualifiedType) type;
		assertEquals("Inner", qualifiedType.getName().toString());
		annotations = qualifiedType.annotations();
		assertTrue(annotations.size() == 1);
		annotation = (Annotation) annotations.get(0);
		assertEquals("@Marker2", annotation.toString());
		qualifierType = qualifiedType.getQualifier();
		assertTrue(qualifierType.isSimpleType());
		simpleType = (SimpleType) qualifierType;
		assertEquals("@Marker1 Outer", simpleType.toString());
		annotations = simpleType.annotations();
		assertTrue(annotations.size() == 1);
		annotation = (Annotation) annotations.get(0);
		assertEquals("@Marker1", annotation.toString());

	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=395886 tests PQTR with
	 * annotations part
	 */
	public void test0006() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter18/src/test0006/X.java",
				true);
		String contents = """
			package test0006;\
			import java.lang.annotation.Target;
			public class X implements One<Outer.  Inner.Deeper<Double>> {
			}
			class X1 implements One<Outer. @Marker1 Inner.Deeper<Double>> {
			}
			class X2 implements One<Outer. @Marker1 Inner.@Marker2 Deeper<Double>> {
			}
			class X3 implements One<@Marker1 Outer. @Marker2 Inner. Deeper<Double>> {
			}
			class Y implements One<Outer1. Inner<Integer>. Deeper<Double>> {
			}
			class Y1 implements One<Outer1. Inner<Integer>. @Marker1 Deeper<Double>> {
			}
			class Y2 implements One<Outer1. @Marker1 Inner<Integer>. Deeper<Double>> {
			}
			class Y3 implements One<@Marker1 Outer1. Inner<Integer>. Deeper<Double>> {
			}
			class Y4 implements One<@Marker1 Outer1. @Marker2 Inner<Integer>. Deeper<Double>> {
			}
			class Z implements One<Outer2<Integer>.Inner.Deeper<Double>> {
			}
			class Z1 implements One<@Marker1 Outer2<Integer>.Inner.Deeper<Double>> {
			}
			class Z2 implements One<Outer2<Integer>. @Marker1 Inner.@Marker2 Deeper<Double>> {
			}
			class W implements One<Outer3<Double>. @Marker1 @Marker2 Inner<Integer, Character>. Deeper<Double>> {
			}
			interface One<T> {}
			class Outer {
				public class Inner {
			       public class Deeper<S> {
			       }
			   }
			}
			class Outer1 {
				public class Inner<T> {
			       public class Deeper<S> {
			       }
			   }
			}
			class Outer2 <T> {
				public class Inner {
			       public class Deeper<S> {}
			   }
			}
			class Outer3 <T> {
				public class Inner<K, V> {
			       public class Deeper<S> {}
			   }
			}
			@Target (java.lang.annotation.ElementType.TYPE_USE)
			@interface Marker1 {}
			@Target (java.lang.annotation.ElementType.TYPE_USE)
			@interface Marker2 {}
			""";
		CompilationUnit cu = (CompilationUnit) buildAST(contents,
				this.workingCopy);
		int tCount = 0;

		// case 1: vanilla case without annotations and with single typeArgument
		// Outer.Inner.Deeper<Double>
		TypeDeclaration typedeclaration = (TypeDeclaration) getASTNode(cu, tCount++);
		ParameterizedType parametrizedType = (ParameterizedType) typedeclaration.superInterfaceTypes().get(0);
		List typeArguments = parametrizedType.typeArguments();
		assertEquals(1, typeArguments.size());
		parametrizedType = (ParameterizedType) typeArguments.get(0);
		assertEquals("Outer.Inner.Deeper<Double>", parametrizedType.toString());
		typeArguments = parametrizedType.typeArguments();
		assertTrue(typeArguments.size() == 1);
		Type type = (Type) typeArguments.get(0);
		assertEquals("Double", type.toString());
		type = parametrizedType.getType();
		assertTrue(type.isSimpleType());
		SimpleType simpleType = (SimpleType) type;
		assertEquals("Outer.Inner.Deeper", simpleType.toString());
		Name name = simpleType.getName();
		assertTrue(name.isQualifiedName());
		QualifiedName qualifiedName = (QualifiedName) name;
		assertEquals("Outer.Inner.Deeper", qualifiedName.toString());

		// case 2 - One annotation after the first class
		// Outer. @Marker1 Inner.Deeper<Double>
		typedeclaration = (TypeDeclaration) getASTNode(cu, tCount++);
		parametrizedType = (ParameterizedType) typedeclaration.superInterfaceTypes().get(0);
		typeArguments = parametrizedType.typeArguments();
		assertEquals(1, typeArguments.size());
		parametrizedType = (ParameterizedType) typeArguments.get(0);
		assertEquals("Outer.@Marker1 Inner.Deeper<Double>",	parametrizedType.toString());
		typeArguments = parametrizedType.typeArguments();
		assertTrue(typeArguments.size() == 1);
		type = (Type) typeArguments.get(0);
		assertEquals("Double", type.toString());
		type = parametrizedType.getType();
		assertTrue(type.isQualifiedType());
		QualifiedType qualifiedType = (QualifiedType) type;
		assertEquals("Outer.@Marker1 Inner.Deeper", qualifiedType.toString());
		name = qualifiedType.getName();
		assertEquals("Deeper", name.toString());
		List annotations = qualifiedType.annotations();
		assertTrue(annotations.size() == 0);
		NameQualifiedType nameQualifiedType = (NameQualifiedType) qualifiedType.getQualifier();
		assertEquals("Outer.@Marker1 Inner", nameQualifiedType.toString());
		annotations = nameQualifiedType.annotations();
		assertTrue(annotations.size() == 1);
		Annotation annotation = (Annotation) annotations.get(0);
		assertEquals("@Marker1", annotation.toString());

		// case 3 - Inner types annotated with outer not annotated with last
		// type arg
		// Outer. @Marker1 Inner.@Marker2 Deeper<Double>
		typedeclaration = (TypeDeclaration) getASTNode(cu, tCount++);
		parametrizedType = (ParameterizedType) typedeclaration.superInterfaceTypes().get(0);
		typeArguments = parametrizedType.typeArguments();
		assertEquals(1, typeArguments.size());
		parametrizedType = (ParameterizedType) typeArguments.get(0);
		assertEquals("Outer.@Marker1 Inner.@Marker2 Deeper<Double>", parametrizedType.toString());
		typeArguments = parametrizedType.typeArguments();
		assertTrue(typeArguments.size() == 1);
		type = (Type) typeArguments.get(0);
		assertEquals("Double", type.toString());
		type = parametrizedType.getType();
		assertTrue(type.isQualifiedType());
		qualifiedType = (QualifiedType) type;
		assertEquals("Outer.@Marker1 Inner.@Marker2 Deeper", qualifiedType.toString());
		name = qualifiedType.getName();
		assertEquals("Deeper", name.toString());
		annotations = qualifiedType.annotations();
		assertTrue(annotations.size() == 1);
		annotation = (Annotation) annotations.get(0);
		assertEquals("@Marker2", annotation.toString());
		Type qualifierType = qualifiedType.getQualifier();
		assertTrue(qualifierType.isNameQualifiedType());
		nameQualifiedType = (NameQualifiedType) qualifierType;
		assertEquals("Outer.@Marker1 Inner", nameQualifiedType.toString());
		annotations = nameQualifiedType.annotations();
		assertTrue(annotations.size() == 1);
		annotation = (Annotation) annotations.get(0);
		assertEquals("@Marker1", annotation.toString());

		// case 4 - one annotation on the outermost, one in middle and one
		// typearg in innermost
		// @Marker1 Outer. @Marker2 Inner. Deeper<Double>
		typedeclaration = (TypeDeclaration) getASTNode(cu, tCount++);
		parametrizedType = (ParameterizedType) typedeclaration
				.superInterfaceTypes().get(0);
		typeArguments = parametrizedType.typeArguments();
		assertEquals(1, typeArguments.size());
		parametrizedType = (ParameterizedType) typeArguments.get(0);
		assertEquals("@Marker1 Outer.@Marker2 Inner.Deeper<Double>", parametrizedType.toString());
		ITypeBinding typeBinding = parametrizedType.resolveBinding();
		assertNotNull("Binding non-null", typeBinding);
		assertEquals("wrong qualified name", "test0006.Outer.Inner.Deeper<java.lang.Double>", typeBinding.getQualifiedName());
		assertTrue("Not a Parameterized Type", typeBinding.isParameterizedType());
		typeArguments = parametrizedType.typeArguments();
		assertTrue(typeArguments.size() == 1);
		type = (Type) typeArguments.get(0);
		assertEquals("Double", type.toString());
		type = parametrizedType.getType();
		assertTrue(type.isQualifiedType());
		qualifiedType = (QualifiedType) type;
		assertEquals("@Marker1 Outer.@Marker2 Inner.Deeper", qualifiedType.toString());
		name = qualifiedType.getName();
		assertEquals("Deeper", name.toString());
		annotations = qualifiedType.annotations();
		qualifierType = qualifiedType.getQualifier();
		assertTrue(qualifierType.isQualifiedType());
		qualifiedType = (QualifiedType) qualifierType;
		assertEquals("@Marker1 Outer.@Marker2 Inner", qualifierType.toString());
		typeBinding = qualifiedType.resolveBinding();
		assertNotNull("Binding non-null", typeBinding);
		typeBinding = qualifiedType.resolveBinding();
		assertEquals("wrong qualified name", "test0006.Outer.Inner", typeBinding.getQualifiedName());
		assertTrue(qualifierType.isAnnotatable());
		AnnotatableType annotatableType = (AnnotatableType) qualifierType;
		annotations = annotatableType.annotations();
		assertTrue(annotations.size() == 1);
		annotation = (Annotation) annotations.get(0);
		assertEquals("@Marker2", annotation.toString());
		name = qualifiedType.getName();
		assertEquals("Inner", name.toString());
		type = qualifiedType.getQualifier();
		assertTrue(type.isSimpleType());
		simpleType = (SimpleType) type;
		assertEquals("@Marker1 Outer", simpleType.toString());
		annotations = simpleType.annotations();
		assertTrue(annotations.size() == 1);
		annotation = (Annotation) annotations.get(0);
		assertEquals("@Marker1", annotation.toString());

		// case 5 - without annotations, but with typeargs at second and third
		// types
		// Outer1. Inner<Integer>. Deeper<Double>
		typedeclaration = (TypeDeclaration) getASTNode(cu, tCount++);
		parametrizedType = (ParameterizedType) typedeclaration.superInterfaceTypes().get(0);
		typeArguments = parametrizedType.typeArguments();
		assertEquals(1, typeArguments.size());
		parametrizedType = (ParameterizedType) typeArguments.get(0);
		assertEquals("Outer1.Inner<Integer>.Deeper<Double>",parametrizedType.toString());
		typeArguments = parametrizedType.typeArguments();
		assertTrue(typeArguments.size() == 1);
		type = (Type) typeArguments.get(0);
		assertEquals("Double", type.toString());
		type = parametrizedType.getType();
		assertTrue(type.isQualifiedType());
		qualifiedType = (QualifiedType) type;
		assertEquals("Outer1.Inner<Integer>.Deeper", qualifiedType.toString());
		name = qualifiedType.getName();
		assertEquals("Deeper", name.toString());
		annotations = qualifiedType.annotations();
		assertTrue(annotations.size() == 0);
		qualifierType = qualifiedType.getQualifier();
		assertTrue(qualifierType.isParameterizedType());
		parametrizedType = (ParameterizedType) qualifierType;
		assertEquals("Outer1.Inner<Integer>", parametrizedType.toString());
		typeArguments = parametrizedType.typeArguments();
		assertTrue(typeArguments.size() == 1);
		type = (Type) typeArguments.get(0);
		assertEquals("Integer", type.toString());
		type = parametrizedType.getType();
		assertTrue(type.isSimpleType());
		simpleType = (SimpleType) type;
		name = simpleType.getName();
		assertTrue(name.isQualifiedName());
		qualifiedName = (QualifiedName) name;
		assertEquals("Outer1.Inner", qualifiedName.toString());

		// case 6 - Annot in between two PQRT with outermost neither annotated
		// nor having typeargs
		// Outer1. Inner<Integer>. @Marker1 Deeper<Double>
		typedeclaration = (TypeDeclaration) getASTNode(cu, tCount++);
		parametrizedType = (ParameterizedType) typedeclaration.superInterfaceTypes().get(0);
		typeArguments = parametrizedType.typeArguments();
		assertEquals(1, typeArguments.size());
		parametrizedType = (ParameterizedType) typeArguments.get(0);
		assertEquals("Outer1.Inner<Integer>.@Marker1 Deeper<Double>", parametrizedType.toString());
		typeArguments = parametrizedType.typeArguments();
		assertTrue(typeArguments.size() == 1);
		type = (Type) typeArguments.get(0);
		assertEquals("Double", type.toString());
		type = parametrizedType.getType();
		assertTrue(type.isQualifiedType());
		qualifiedType = (QualifiedType) type;
		assertEquals("Outer1.Inner<Integer>.@Marker1 Deeper", qualifiedType.toString());
		name = qualifiedType.getName();
		assertEquals("Deeper", name.toString());
		annotations = qualifiedType.annotations();
		assertTrue(annotations.size() == 1);
		annotation = (Annotation) annotations.get(0);
		assertEquals("@Marker1", annotation.toString());
		qualifierType = qualifiedType.getQualifier();
		assertTrue(qualifierType.isParameterizedType());
		parametrizedType = (ParameterizedType) qualifierType;
		assertEquals("Outer1.Inner<Integer>", parametrizedType.toString());
		typeArguments = parametrizedType.typeArguments();
		assertTrue(typeArguments.size() == 1);
		type = (Type) typeArguments.get(0);
		assertEquals("Integer", type.toString());
		type = parametrizedType.getType();
		assertTrue(type.isSimpleType());
		simpleType = (SimpleType) type;
		name = simpleType.getName();
		assertTrue(name.isQualifiedName());
		qualifiedName = (QualifiedName) name;
		assertEquals("Outer1.Inner", qualifiedName.toString());

		// case 7 - Outermost still empty (no annotations, no type args),
		// followed by annotation, and then typeargs
		// Outer1. @Marker1 Inner<Integer>. Deeper<Double>
		typedeclaration = (TypeDeclaration) getASTNode(cu, tCount++);
		parametrizedType = (ParameterizedType) typedeclaration.superInterfaceTypes().get(0);
		typeArguments = parametrizedType.typeArguments();
		assertEquals(1, typeArguments.size());
		parametrizedType = (ParameterizedType) typeArguments.get(0);
		assertEquals("Outer1.@Marker1 Inner<Integer>.Deeper<Double>", parametrizedType.toString());
		typeArguments = parametrizedType.typeArguments();
		assertTrue(typeArguments.size() == 1);
		type = (Type) typeArguments.get(0);
		assertEquals("Double", type.toString());
		type = parametrizedType.getType();
		assertTrue(type.isQualifiedType());
		qualifiedType = (QualifiedType) type;
		assertEquals("Outer1.@Marker1 Inner<Integer>.Deeper", qualifiedType.toString());
		name = qualifiedType.getName();
		assertEquals("Deeper", name.toString());
		annotations = qualifiedType.annotations();
		assertTrue(annotations.size() == 0);
		qualifierType = qualifiedType.getQualifier();
		assertTrue(qualifierType.isParameterizedType());
		parametrizedType = (ParameterizedType) qualifierType;
		assertEquals("Outer1.@Marker1 Inner<Integer>", parametrizedType.toString());
		typeArguments = parametrizedType.typeArguments();
		assertTrue(typeArguments.size() == 1);
		type = (Type) typeArguments.get(0);
		assertEquals("Integer", type.toString());
		type = parametrizedType.getType();
		assertTrue(type.isNameQualifiedType());
		nameQualifiedType = (NameQualifiedType) type;
		assertEquals("Outer1.@Marker1 Inner", nameQualifiedType.toString());
		annotations = nameQualifiedType.annotations();
		assertTrue(annotations.size() == 1);
		annotation = (Annotation) annotations.get(0);
		assertEquals("@Marker1", annotation.toString());

		// case 8 - Similar to above, but with the major difference of
		// annotation shifted to outermost.
		// @Marker1 Outer1. Inner<Integer>. Deeper<Double>
		typedeclaration = (TypeDeclaration) getASTNode(cu, tCount++);
		parametrizedType = (ParameterizedType) typedeclaration.superInterfaceTypes().get(0);
		typeArguments = parametrizedType.typeArguments();
		assertEquals(1, typeArguments.size());
		parametrizedType = (ParameterizedType) typeArguments.get(0);
		assertEquals("@Marker1 Outer1.Inner<Integer>.Deeper<Double>", parametrizedType.toString());
		typeArguments = parametrizedType.typeArguments();
		assertTrue(typeArguments.size() == 1);
		type = (Type) typeArguments.get(0);
		assertEquals("Double", type.toString());
		type = parametrizedType.getType();
		assertTrue(type.isQualifiedType());
		qualifiedType = (QualifiedType) type;
		assertEquals("@Marker1 Outer1.Inner<Integer>.Deeper", qualifiedType.toString());
		name = qualifiedType.getName();
		assertEquals("Deeper", name.toString());
		annotations = qualifiedType.annotations();
		assertTrue(annotations.size() == 0);
		qualifierType = qualifiedType.getQualifier();
		assertTrue(qualifierType.isParameterizedType());
		parametrizedType = (ParameterizedType) qualifierType;
		assertEquals("@Marker1 Outer1.Inner<Integer>", parametrizedType.toString());
		typeArguments = parametrizedType.typeArguments();
		assertTrue(typeArguments.size() == 1);
		type = (Type) typeArguments.get(0);
		assertEquals("Integer", type.toString());
		type = parametrizedType.getType();
		assertTrue(type.isQualifiedType());
		qualifiedType = (QualifiedType) type;
		assertEquals("@Marker1 Outer1.Inner", qualifiedType.toString());
		annotations = qualifiedType.annotations();
		assertTrue(annotations.size() == 0);
		name = qualifiedType.getName();
		assertTrue(name.isSimpleName());
		assertEquals("Inner", name.toString());
		type = qualifiedType.getQualifier();
		assertTrue(type.isSimpleType());
		simpleType = (SimpleType) type;
		annotations = simpleType.annotations();
		assertTrue(annotations.size() == 1);
		annotation = (Annotation) annotations.get(0);
		assertEquals("@Marker1", annotation.toString());
		name = simpleType.getName();
		assertTrue(name.isSimpleName());
		assertEquals("Outer1", name.toString());

		// case 9: scenario of the above case plus another annotation at
		// mid-level.
		// @Marker1 Outer1.@Marker2 Inner<Integer>. Deeper<Double>
		typedeclaration = (TypeDeclaration) getASTNode(cu, tCount++);
		parametrizedType = (ParameterizedType) typedeclaration.superInterfaceTypes().get(0);
		typeArguments = parametrizedType.typeArguments();
		assertEquals(1, typeArguments.size());
		parametrizedType = (ParameterizedType) typeArguments.get(0);
		assertEquals("@Marker1 Outer1.@Marker2 Inner<Integer>.Deeper<Double>",parametrizedType.toString());
		typeArguments = parametrizedType.typeArguments();
		assertTrue(typeArguments.size() == 1);
		type = (Type) typeArguments.get(0);
		assertEquals("Double", type.toString());
		type = parametrizedType.getType();
		assertTrue(type.isQualifiedType());
		qualifiedType = (QualifiedType) type;
		assertEquals("@Marker1 Outer1.@Marker2 Inner<Integer>.Deeper", qualifiedType.toString());
		name = qualifiedType.getName();
		assertEquals("Deeper", name.toString());
		annotations = qualifiedType.annotations();
		assertTrue(annotations.size() == 0);
		qualifierType = qualifiedType.getQualifier();
		assertTrue(qualifierType.isParameterizedType());
		parametrizedType = (ParameterizedType) qualifierType;
		assertEquals("@Marker1 Outer1.@Marker2 Inner<Integer>", parametrizedType.toString());
		typeArguments = parametrizedType.typeArguments();
		assertTrue(typeArguments.size() == 1);
		type = (Type) typeArguments.get(0);
		assertEquals("Integer", type.toString());
		type = parametrizedType.getType();
		assertTrue(type.isQualifiedType());
		qualifiedType = (QualifiedType) type;
		assertEquals("@Marker1 Outer1.@Marker2 Inner", qualifiedType.toString());
		annotations = qualifiedType.annotations();
		assertTrue(annotations.size() == 1);
		annotation = (Annotation) annotations.get(0);
		assertEquals("@Marker2", annotation.toString());
		name = qualifiedType.getName();
		assertTrue(name.isSimpleName());
		assertEquals("Inner", name.toString());
		type = qualifiedType.getQualifier();
		assertTrue(type.isSimpleType());
		simpleType = (SimpleType) type;
		annotations = simpleType.annotations();
		assertTrue(annotations.size() == 1);
		annotation = (Annotation) annotations.get(0);
		assertEquals("@Marker1", annotation.toString());
		name = simpleType.getName();
		assertTrue(name.isSimpleName());
		assertEquals("Outer1", name.toString());

		// case 10 - PQRT with two type args but without annotations
		// Outer2<Integer>.Inner.Deeper<Double>
		typedeclaration = (TypeDeclaration) getASTNode(cu, tCount++);
		parametrizedType = (ParameterizedType) typedeclaration.superInterfaceTypes().get(0);
		typeArguments = parametrizedType.typeArguments();
		assertEquals(1, typeArguments.size());
		parametrizedType = (ParameterizedType) typeArguments.get(0);
		assertEquals("Outer2<Integer>.Inner.Deeper<Double>", parametrizedType.toString());
		typeArguments = parametrizedType.typeArguments();
		assertTrue(typeArguments.size() == 1);
		type = (Type) typeArguments.get(0);
		assertEquals("Double", type.toString());
		type = parametrizedType.getType();
		assertTrue(type.isQualifiedType());
		qualifiedType = (QualifiedType) type;
		assertEquals("Outer2<Integer>.Inner.Deeper", qualifiedType.toString());
		ITypeBinding binding = qualifiedType.resolveBinding();
		name = qualifiedType.getName();
		assertEquals("Deeper", name.toString());
		annotations = qualifiedType.annotations();
		assertTrue(annotations.size() == 0);
		qualifierType = qualifiedType.getQualifier();
		assertTrue(qualifierType.isQualifiedType());
		qualifiedType = (QualifiedType) qualifierType;
		binding = qualifiedType.resolveBinding();
		assertEquals("Outer2<Integer>.Inner", qualifiedType.toString());
		assertEquals("wrong qualified binding", "test0006.Outer2<java.lang.Integer>.Inner",	binding.getQualifiedName());
		name = qualifiedType.getName();
		assertEquals("Inner", name.toString());
		qualifierType = qualifiedType.getQualifier();
		assertTrue(qualifierType.isParameterizedType());
		parametrizedType = (ParameterizedType) qualifierType;
		typeArguments = parametrizedType.typeArguments();
		assertTrue(typeArguments.size() == 1);
		type = (Type) typeArguments.get(0);
		assertEquals("Integer", type.toString());
		type = parametrizedType.getType();
		assertEquals("Outer2", type.toString());

		// case 11 - annotation at outermost in addition to scenario in case 10.
		// @Marker1 Outer2<Integer>.Inner.Deeper<Double>
		typedeclaration = (TypeDeclaration) getASTNode(cu, tCount++);
		parametrizedType = (ParameterizedType) typedeclaration.superInterfaceTypes().get(0);
		typeArguments = parametrizedType.typeArguments();
		assertEquals(1, typeArguments.size());
		parametrizedType = (ParameterizedType) typeArguments.get(0);
		assertEquals("@Marker1 Outer2<Integer>.Inner.Deeper<Double>", parametrizedType.toString());
		typeArguments = parametrizedType.typeArguments();
		assertTrue(typeArguments.size() == 1);
		type = (Type) typeArguments.get(0);
		assertEquals("Double", type.toString());
		type = parametrizedType.getType();
		assertTrue(type.isQualifiedType());
		qualifiedType = (QualifiedType) type;
		assertEquals("@Marker1 Outer2<Integer>.Inner.Deeper", qualifiedType.toString());
		name = qualifiedType.getName();
		assertEquals("Deeper", name.toString());
		annotations = qualifiedType.annotations();
		assertTrue(annotations.size() == 0);
		qualifierType = qualifiedType.getQualifier();
		assertTrue(qualifierType.isQualifiedType());
		qualifiedType = (QualifiedType) qualifierType;
		assertEquals("@Marker1 Outer2<Integer>.Inner", qualifiedType.toString());
		name = qualifiedType.getName();
		assertEquals("Inner", name.toString());
		qualifierType = qualifiedType.getQualifier();
		assertTrue(qualifierType.isParameterizedType());
		parametrizedType = (ParameterizedType) qualifierType;
		typeArguments = parametrizedType.typeArguments();
		assertTrue(typeArguments.size() == 1);
		type = (Type) typeArguments.get(0);
		assertEquals("Integer", type.toString());
		type = parametrizedType.getType();
		assertEquals("@Marker1 Outer2", type.toString());
		assertTrue(type.isSimpleType());
		simpleType = (SimpleType) type;
		annotations = simpleType.annotations();
		assertTrue(annotations.size() == 1);
		annotation = (Annotation) annotations.get(0);
		assertEquals("@Marker1", annotation.toString());

		// case 12 - No annotations at outermost, but outermost has
		// typeAnnotations.
		// Outer2<Integer>. @Marker1 Inner.@Marker2 Deeper<Double>
		typedeclaration = (TypeDeclaration) getASTNode(cu, tCount++);
		parametrizedType = (ParameterizedType) typedeclaration.superInterfaceTypes().get(0);
		typeArguments = parametrizedType.typeArguments();
		assertEquals(1, typeArguments.size());
		parametrizedType = (ParameterizedType) typeArguments.get(0);
		assertEquals("Outer2<Integer>.@Marker1 Inner.@Marker2 Deeper<Double>", parametrizedType.toString());
		typeArguments = parametrizedType.typeArguments();
		assertTrue(typeArguments.size() == 1);
		type = (Type) typeArguments.get(0);
		assertEquals("Double", type.toString());
		type = parametrizedType.getType();
		assertTrue(type.isQualifiedType());
		qualifiedType = (QualifiedType) type;
		assertEquals("Outer2<Integer>.@Marker1 Inner.@Marker2 Deeper",qualifiedType.toString());
		name = qualifiedType.getName();
		assertEquals("Deeper", name.toString());
		annotations = qualifiedType.annotations();
		assertTrue(annotations.size() == 1);
		annotation = (Annotation) annotations.get(0);
		assertEquals("@Marker2", annotation.toString());
		qualifierType = qualifiedType.getQualifier();
		assertTrue(qualifierType.isQualifiedType());
		qualifiedType = (QualifiedType) qualifierType;
		assertEquals("Outer2<Integer>.@Marker1 Inner", qualifiedType.toString());
		name = qualifiedType.getName();
		assertEquals("Inner", name.toString());
		annotations = qualifiedType.annotations();
		assertTrue(annotations.size() == 1);
		annotation = (Annotation) annotations.get(0);
		assertEquals("@Marker1", annotation.toString());
		qualifierType = qualifiedType.getQualifier();
		assertTrue(qualifierType.isParameterizedType());
		parametrizedType = (ParameterizedType) qualifierType;
		typeArguments = parametrizedType.typeArguments();
		assertTrue(typeArguments.size() == 1);
		type = (Type) typeArguments.get(0);
		assertEquals("Integer", type.toString());
		type = parametrizedType.getType();
		assertEquals("Outer2", type.toString());
		assertTrue(type.isSimpleType());

		// case 13 - a list of annotations and multiple typeArgument element
		// lists.
		// Outer3<Double>. @Marker1 @Marker2 Inner<Integer, Character>.
		// Deeper<Double>
		typedeclaration = (TypeDeclaration) getASTNode(cu, tCount++);
		parametrizedType = (ParameterizedType) typedeclaration.superInterfaceTypes().get(0);
		typeArguments = parametrizedType.typeArguments();
		assertEquals(1, typeArguments.size());
		parametrizedType = (ParameterizedType) typeArguments.get(0);
		assertEquals("Outer3<Double>.@Marker1 @Marker2 Inner<Integer,Character>.Deeper<Double>",parametrizedType.toString());
		typeArguments = parametrizedType.typeArguments();
		assertTrue(typeArguments.size() == 1);
		type = (Type) typeArguments.get(0);
		assertEquals("Double", type.toString());
		type = parametrizedType.getType();
		assertTrue(type.isQualifiedType());
		qualifiedType = (QualifiedType) type;
		assertEquals("Outer3<Double>.@Marker1 @Marker2 Inner<Integer,Character>.Deeper", qualifiedType.toString());
		name = qualifiedType.getName();
		assertEquals("Deeper", name.toString());
		annotations = qualifiedType.annotations();
		assertTrue(annotations.size() == 0);
		qualifierType = qualifiedType.getQualifier();
		assertTrue(qualifierType.isParameterizedType());
		parametrizedType = (ParameterizedType) qualifierType;
		typeArguments = parametrizedType.typeArguments();
		assertTrue(typeArguments.size() == 2);
		assertEquals("Integer", typeArguments.get(0).toString());
		assertEquals("Character", typeArguments.get(1).toString());
		type = parametrizedType.getType();
		assertTrue(type.isQualifiedType());
		qualifiedType = (QualifiedType) type;
		assertEquals("Inner", qualifiedType.getName().toString());
		annotations = qualifiedType.annotations();
		assertTrue(annotations.size() == 2);
		assertEquals("@Marker1", annotations.get(0).toString());
		assertEquals("@Marker2", annotations.get(1).toString());
		qualifierType = qualifiedType.getQualifier();
		assertTrue(qualifierType.isParameterizedType());
		parametrizedType = (ParameterizedType) qualifierType;
		typeArguments = parametrizedType.typeArguments();
		assertTrue(typeArguments.size() == 1);
		assertEquals("Double", typeArguments.get(0).toString());
		type = parametrizedType.getType();
		assertTrue(type.isSimpleType());
		simpleType = (SimpleType) type;
		assertEquals("Outer3", simpleType.toString());
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=391893
	public void test0007() throws JavaModelException {
		String contents =
			"""
			public class X {
				public void foo(@Marker @Marker2 X this, @Marker2 @Marker int i){}
			}
			@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)
			@interface Marker {}
			@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)
			@interface Marker2 {}""";
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		node = getASTNode(unit, 0, 0);
		assertEquals("Not a method Declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration method = (MethodDeclaration) node;
		Type receiver = method.getReceiverType();
		assertEquals("Not a simple type", ASTNode.SIMPLE_TYPE, receiver.getNodeType());
		assertEquals("Incorrect receiver signature", "@Marker @Marker2 X", ((SimpleType) receiver).toString());
		assertEquals("Incorrect annotations on receiver", 2, ((SimpleType) receiver).annotations().size());
		assertNull("Incorrect receiver qualfier", method.getReceiverQualifier());
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=391893
	public void test0008() throws JavaModelException {
		String contents =
			"""
			public class X {
				class Y {
					public Y(@Marker @Marker2 X X.this, @Marker2 @Marker int i){}
				}
			}
			@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)
			@interface Marker {}
			@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)
			@interface Marker2 {}""";

		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		node = getASTNode(unit, 0, 0);
		assertEquals("Not a type Declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration innerType = (TypeDeclaration) node;
		assertEquals("Incorrect no of methods", 1, innerType.getMethods().length);
		MethodDeclaration method = innerType.getMethods()[0];
		Type receiver = method.getReceiverType();
		assertEquals("Not a simple type", ASTNode.SIMPLE_TYPE, receiver.getNodeType());
		assertEquals("Incorrect receiver signature", "@Marker @Marker2 X", ((SimpleType) receiver).toString());
		assertEquals("Incorrect annotations on receiver", 2, ((SimpleType) receiver).annotations().size());
		assertNotNull("Incorrect receiver qualfier", method.getReceiverQualifier());
		assertEquals("Incorrect receiver qualfier", "X", method.getReceiverQualifier().getFullyQualifiedName());
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=391895
	public void test0009() throws JavaModelException {
		String contents =
				"""
			import java.lang.annotation.ElementType;
			public class X {
			 	class Y {
					@Annot int @Annot1 [] a @Annot2 @Annot3 [] @Annot3 @Annot2 [] @Annot4 [], b @Annot2 @Annot3 [] @Annot4 [], c [][][];
					public void foo1(@Annot int @Annot1 [] p @Annot2 @Annot3 [] @Annot3 @Annot2 [] @Annot4 @Annot3 []) {}
					public void foo2(@Annot int p [][]) {}
					@Annot String @Annot1 [] foo3() @Annot1 @Annot2 [][] { return null; }
				}
			}
			@java.lang.annotation.Target(value = {ElementType.TYPE_USE})
			@interface Annot {}
			@java.lang.annotation.Target(value = {ElementType.TYPE_USE})
			@interface Annot1 {}
			@java.lang.annotation.Target(value = {ElementType.TYPE_USE})
			@interface Annot2 {}
			@java.lang.annotation.Target(value = {ElementType.TYPE_USE})
			@interface Annot3 {}
			@java.lang.annotation.Target(value = {ElementType.TYPE_USE})
			@interface Annot4 {}""";
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		node = getASTNode(unit, 0, 0);
		assertEquals("Not a type Declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration type = (TypeDeclaration) node;
		FieldDeclaration field = type.getFields()[0];
		List fragments = field.fragments();
		assertEquals("Incorrect no of fragments", 3, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		assertExtraDimensionsEqual("Incorrect extra dimensions", fragment.extraDimensions(), "@Annot2 @Annot3 [] @Annot3 @Annot2 [] @Annot4 []");
		fragment = (VariableDeclarationFragment) fragments.get(1);
		assertExtraDimensionsEqual("Incorrect extra dimensions", fragment.extraDimensions(), "@Annot2 @Annot3 [] @Annot4 []");
		fragment = (VariableDeclarationFragment) fragments.get(2);
		assertExtraDimensionsEqual("Incorrect extra dimensions", fragment.extraDimensions(), "[] [] []");
		MethodDeclaration[] methods = type.getMethods();
		assertEquals("Incorrect no of methods", 3, methods.length);
		MethodDeclaration method = methods[0];
		List parameters = method.parameters();
		assertEquals("Incorrect no of parameters", 1, parameters.size());
		assertExtraDimensionsEqual("Incorrect extra dimensions", ((SingleVariableDeclaration) parameters.get(0)).extraDimensions(), "@Annot2 @Annot3 [] @Annot3 @Annot2 [] @Annot4 @Annot3 []");

		method = methods[1];
		parameters = method.parameters();
		assertEquals("Incorrect no of parameters", 1, parameters.size());
		assertExtraDimensionsEqual("Incorrect extra dimensions", ((SingleVariableDeclaration) parameters.get(0)).extraDimensions(), "[] []");

		method = methods[2];
		assertExtraDimensionsEqual("Incorrect extra dimensions", method.extraDimensions(), "@Annot1 @Annot2 [] []");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399600
	public void test0010() throws JavaModelException {
		String contents =
				"""
			import java.lang.annotation.ElementType;
			public class X {
				@Marker int foo(@Marker("Blah") int z) @Marker [] @Marker [] {
					return null;
				}
			}
			@java.lang.annotation.Target (ElementType.TYPE_USE)
			@interface Marker {
				String value() default "Blah";
			}""";
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		node = getASTNode(unit, 0, 0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration method = (MethodDeclaration) node;
		assertExtraDimensionsEqual("Incorrect extra dimensions", method.extraDimensions(), "@Marker [] @Marker []");
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=391894
	public void test0011() throws JavaModelException {
		String contents =
				"""
			import java.lang.annotation.ElementType;
			public class X {
			 	public void foo() {
					int @Marker [][][] i = new @Marker2 int @Marker @Marker2 [2] @Marker2 @Marker3 [bar()] @Marker3 @Marker [];\s
					int @Marker [][][] j = new @Marker int @Marker3 @Marker [2] @Marker @Marker2 [X.bar2(2)] @Marker2 @Marker3 [];
				}
				public int bar() {
					return 2;
				}
				public static int bar2(int k) {
					return k;
				}
			}
			@java.lang.annotation.Target (ElementType.TYPE_USE)
			@interface Marker {}
			@java.lang.annotation.Target (ElementType.TYPE_USE)
			@interface Marker2 {}
			@java.lang.annotation.Target (ElementType.TYPE_USE)
			@interface Marker3 {}""";

		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		node = getASTNode(unit, 0, 0);
		assertEquals("Not a Method Declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration method = (MethodDeclaration) node;
		List list = method.getBody().statements();
		assertEquals("Incorrect no of statements", 2, list.size());
		VariableDeclarationStatement statement1 = (VariableDeclarationStatement) list.get(0);
		VariableDeclarationStatement statement2 = (VariableDeclarationStatement) list.get(1);
		list = statement1.fragments();
		assertEquals("Incorrect no of fragments", 1, list.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) list.get(0);
		ArrayCreation creation = (ArrayCreation) fragment.getInitializer();
		Type type = creation.getType();
		assertEquals("Incorrect type", true, type.isArrayType());
		checkSourceRange(type, "@Marker2 int @Marker @Marker2 [2] @Marker2 @Marker3 [bar()] @Marker3 @Marker []", contents.toCharArray());
		Dimension dimension = (Dimension) ((ArrayType) type).dimensions().get(2);
		assertEquals("Incorrect annotations", "@Marker3 @Marker ", convertAnnotationsList(dimension.annotations()));
		dimension = (Dimension) ((ArrayType) type).dimensions().get(1);
		assertEquals("Incorrect annotations", "@Marker2 @Marker3 ", convertAnnotationsList(dimension.annotations()));
		dimension = (Dimension) ((ArrayType) type).dimensions().get(0);
		assertEquals("Incorrect annotations", "@Marker @Marker2 ", convertAnnotationsList(dimension.annotations()));
		List dimensions = creation.dimensions();
		assertEquals("Incorrect expressions", 2, dimensions.size());
		assertEquals("Incorrect expressions", "2", dimensions.get(0).toString());
		assertEquals("Incorrect expressions", "bar()", dimensions.get(1).toString());

		list = statement2.fragments();
		assertEquals("Incorrect no of fragments", 1, list.size());
		fragment = (VariableDeclarationFragment) list.get(0);
		creation = (ArrayCreation) fragment.getInitializer();
		checkSourceRange(creation.getType(), "@Marker int @Marker3 @Marker [2] @Marker @Marker2 [X.bar2(2)] @Marker2 @Marker3 []", contents.toCharArray());

		type = creation.getType();
		assertEquals("Incorrect type", true, type.isArrayType());
		dimension = (Dimension) ((ArrayType) type).dimensions().get(2);
		assertEquals("Incorrect annotations", "@Marker2 @Marker3 ", convertAnnotationsList(dimension.annotations()));
		dimension = (Dimension) ((ArrayType) type).dimensions().get(1);
		assertEquals("Incorrect annotations", "@Marker @Marker2 ", convertAnnotationsList(dimension.annotations()));
		dimension = (Dimension) ((ArrayType) type).dimensions().get(0);
		assertEquals("Incorrect annotations", "@Marker3 @Marker ", convertAnnotationsList(dimension.annotations()));
		dimensions = creation.dimensions();
		assertEquals("Incorrect expressions", 2, dimensions.size());
		assertEquals("Incorrect expressions", "2", dimensions.get(0).toString());
		assertEquals("Incorrect expressions", "X.bar2(2)", dimensions.get(1).toString());
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=391894
	public void test0012() throws JavaModelException {
		String contents =
				"""
			import java.lang.annotation.ElementType;
			public class X {
			 	public void foo() {
					int @Marker [][][] i = new @Marker2 int @Marker @Marker2 [] @Marker2 @Marker3 [] @Marker3 @Marker [] {{{1, 2, 3}}};\s
				}
			}
			@java.lang.annotation.Target (ElementType.TYPE_USE)
			@interface Marker {}
			@java.lang.annotation.Target (ElementType.TYPE_USE)
			@interface Marker2 {}
			@java.lang.annotation.Target (ElementType.TYPE_USE)
			@interface Marker3 {}""";
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		node = getASTNode(unit, 0, 0);
		assertEquals("Not a Method Declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration method = (MethodDeclaration) node;
		List list = method.getBody().statements();
		assertEquals("Incorrect no of statements", 1, list.size());
		VariableDeclarationStatement statement1 = (VariableDeclarationStatement) list.get(0);
		list = statement1.fragments();
		assertEquals("Incorrect no of fragments", 1, list.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) list.get(0);
		ArrayCreation creation = (ArrayCreation) fragment.getInitializer();
		Type type = creation.getType();
		assertEquals("Incorrect type", true, type.isArrayType());
		checkSourceRange(type, "@Marker2 int @Marker @Marker2 [] @Marker2 @Marker3 [] @Marker3 @Marker []", contents.toCharArray());
		ArrayInitializer initializer = creation.getInitializer();
		checkSourceRange(initializer, "{{{1, 2, 3}}}", contents.toCharArray());
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=391894
	// Force to use JLS4 and confirm malformed flags are set.
	public void test0021() throws JavaModelException {
		String contents =
				"""
			import java.lang.annotation.ElementType;
			public class X {
			 	public void foo() {
					int @Marker [][][] i = new @Marker2 int @Marker @Marker2 [] @Marker2 @Marker3 [] @Marker3 @Marker [] {{{1, 2, 3}}};\s
				}
			}
			@java.lang.annotation.Target (ElementType.TYPE_USE)
			@interface Marker {}
			@java.lang.annotation.Target (ElementType.TYPE_USE)
			@interface Marker2 {}
			@java.lang.annotation.Target (ElementType.TYPE_USE)
			@interface Marker3 {}""";
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		CompilationUnit unit = (CompilationUnit) buildAST(getJLS4(), contents, this.workingCopy, true, true, true);

		ASTNode node = getASTNode(unit, 0, 0);
		assertEquals("Not a Method Declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration method = (MethodDeclaration) node;
		List list = method.getBody().statements();
		assertEquals("Incorrect no of statements", 1, list.size());
		VariableDeclarationStatement statement1 = (VariableDeclarationStatement) list.get(0);
		list = statement1.fragments();
		assertEquals("Incorrect no of fragments", 1, list.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) list.get(0);
		ArrayCreation creation = (ArrayCreation) fragment.getInitializer();
		Type type = creation.getType();
		assertEquals("Incorrect type", true, type.isArrayType());
		assertEquals("Type should be malformed", ASTNode.MALFORMED, (type.getFlags() & ASTNode.MALFORMED));
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=399768
	 */
	public void test0013() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter18/src/test0010/X.java",
				true/* resolve */);
		String contents = """
			package test0010;\
			import java.lang.annotation.Target;
			public class X implements One<@Marker1 Integer, @Marker2 Boolean> {
			}
			class Y implements One<@Marker1 @Marker2 Integer, @Marker2 @Marker1 Double> {
			}
			interface One<T, U> {}
			@Target (java.lang.annotation.ElementType.TYPE_USE)
			@interface Marker1 {}
			@Target (java.lang.annotation.ElementType.TYPE_USE)
			@interface Marker2 {}
			""";
		CompilationUnit cu = (CompilationUnit) buildAST(contents, this.workingCopy);
		// simple types for generic type arguments to parameterized classes
		TypeDeclaration typedeclaration = (TypeDeclaration) getASTNode(cu, 0);
		Type type = (Type)((ParameterizedType) typedeclaration.superInterfaceTypes().get(0)).typeArguments().get(0);
		assertTrue(type.isSimpleType());
		SimpleType simpleType = (SimpleType) type;
		List annotations = simpleType.annotations();
		assertEquals("wrong number of annotations", 1, annotations.size());
		assertEquals("@Marker1", annotations.get(0).toString());
		assertNotNull("No annotation", type);
		typedeclaration = (TypeDeclaration) getASTNode(cu, 1);
		type = (Type)((ParameterizedType) typedeclaration.superInterfaceTypes().get(0)).typeArguments().get(0);
		assertTrue(type.isSimpleType());
		simpleType = (SimpleType) type;
		annotations = simpleType.annotations();
		assertEquals("wrong number of annotations", 2, annotations.size());
		assertEquals("@Marker2", annotations.get(1).toString());
		assertNotNull("No annotation", type);
		type = (Type)((ParameterizedType) typedeclaration.superInterfaceTypes().get(0)).typeArguments().get(1);
		assertTrue(type.isSimpleType());
		simpleType = (SimpleType) type;
		annotations = simpleType.annotations();
		assertEquals("wrong number of annotations", 2, annotations.size());
		assertEquals("@Marker1", annotations.get(1).toString());
		assertNotNull("No annotation", type);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=399768
	 */
	public void test0014() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter18/src/test0011/X.java",
				true/* resolve */);
		String contents = """
			package test0011;\
			import java.lang.annotation.Target;
			public class X {
				public void foo() {
			 	Y y = new <@Marker2 @Marker1 String> Y(new String("Hello"));
			 	len = y.<@Marker1 @Marker2 String> bar(new String("World"));
			 }
				public int len;
			}
			class Y {
				public <T> Y(T t) {
					len = t instanceof String ? ((String)t).length() : 0;
				}
				public <T> int bar(T t) {
					return t instanceof String ? ((String)t).length() : len;
				}
			 private int len;
			}
			@Target (java.lang.annotation.ElementType.TYPE_USE)
			@interface Marker1 {}
			@Target (java.lang.annotation.ElementType.TYPE_USE)
			@interface Marker2 {}
			""";
		CompilationUnit cu = (CompilationUnit) buildAST(contents, this.workingCopy);
		// simple type for generic type arguments in a generic method or constructor invocation
		MethodDeclaration methodDeclaration = (MethodDeclaration) getASTNode(cu, 0, 0);
		List statements = methodDeclaration.getBody().statements();
		Statement statement = (Statement)statements.get(0);
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) statement;
		VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) variableDeclarationStatement.fragments().get(0);
		Expression expression = variableDeclarationFragment.getInitializer();
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expression;
		Type type = (Type) classInstanceCreation.typeArguments().get(0);
		assertEquals("@Marker2 @Marker1 String", type.toString());
		assertTrue(type.isSimpleType());
		SimpleType simpleType = (SimpleType) type;
		List annotations = simpleType.annotations();
		assertEquals("wrong number of annotations", 2, annotations.size());
		assertEquals("@Marker2", annotations.get(0).toString());
		statement = (Statement) statements.get(1);
		Assignment assignment  = (Assignment) ((ExpressionStatement)statement).getExpression();
		expression = assignment.getRightHandSide();
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		simpleType = (SimpleType)methodInvocation.typeArguments().get(0);
		annotations = simpleType.annotations();
		assertEquals("wrong number of annotations", 2, annotations.size());
		assertEquals("@Marker1", annotations.get(0).toString());
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=399768
	 */
	public void test0015() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter18/src/test0012/X.java",
				true/* resolve */);
		String contents = """
			package test0012;\
			import java.lang.annotation.Target;
			import java.io.File;
			public class X <@Marker1 @Marker3 F extends @Marker1 @Marker2 File> {
				public int foo(F f) {
			 	Y <@Marker2 @Marker3 ? super @Marker1 @Marker2 File> y = new @Marker2 @Marker1 Y<File>();
					Outer o = new @Marker1 @Marker2 Outer();
					Outer.Inner inner = o.new @Marker1 @Marker2 Inner();
			 	ZZ zz = new <String> @Marker1 @Marker2 ZZ();
			 	return f.getName().length() + y.hashCode() + inner.hashCode();
			 }
			}
			class Y<@Marker3 T> {
				public int bar(T t) {
					return t instanceof @Marker1 @Marker2 File ? t.toString().length() : 0;
				}
			}
			class Outer {
				public class Inner {
					public class Deeper {
					}
				}
			}
			class ZZ {
			public @Marker1 @Marker2 <T> ZZ() {
				T t = null;
				len =  t instanceof String ? t.hashCode() : 0;
			}
			public @Marker1 int  getint(@Marker2 @Marker1 ZZ this) {return len;}
			public int len;
			}
			@Target (java.lang.annotation.ElementType.TYPE_USE)
			@interface Marker1 {}
			@Target (java.lang.annotation.ElementType.TYPE_USE)
			@interface Marker2 {}
			@Target (java.lang.annotation.ElementType.TYPE_USE)
			@interface Marker3 {}
			""";
		CompilationUnit cu = (CompilationUnit) buildAST(contents, this.workingCopy);

		TypeDeclaration typedeclaration = (TypeDeclaration) getASTNode(cu, 0);
		TypeParameter typeParameter = (TypeParameter) typedeclaration.typeParameters().get(0);

		// TypeParameter with TYPE_USE and TYPE_PARAMETER annotation combination.
		assertEquals("@Marker1 @Marker3 F extends @Marker1 @Marker2 File", typeParameter.toString());
		assertTrue(typeParameter.modifiers().size() == 2);
		Annotation annotation = (Annotation)typeParameter.modifiers().get(1);
		assertEquals("@Marker3", annotation.toString());
		IAnnotationBinding abinding = annotation.resolveAnnotationBinding();
		assertEquals("@Marker3()", abinding.toString());

		// simpletype for type parameter bounds
		SimpleType simpleType = (SimpleType) typeParameter.typeBounds().get(0);
		annotation = (Annotation) simpleType.annotations().get(1);
		assertEquals("@Marker2", annotation.toString());
		abinding = annotation.resolveAnnotationBinding();
		assertEquals("@Marker2()", abinding.toString());

		MethodDeclaration methodDeclaration = (MethodDeclaration) getASTNode(cu, 0, 0);
		List statements = methodDeclaration.getBody().statements();
		Statement statement = (Statement)statements.get(0);
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement)statement;
		Type type = variableDeclarationStatement.getType();
		assertTrue(type.isParameterizedType());
		type = (Type)((ParameterizedType)type).typeArguments().get(0);
		assertTrue(type.isWildcardType());

		// for constructor invocation results 1/4
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) variableDeclarationStatement.fragments().get(0);
		Expression expression = fragment.getInitializer();
		assertEquals("new @Marker2 @Marker1 Y<File>()", expression.toString());
		simpleType = (SimpleType) ((ParameterizedType)((ClassInstanceCreation)expression).getType()).getType();
		assertEquals("@Marker2 @Marker1 Y", simpleType.toString());
		annotation = (Annotation) simpleType.annotations().get(1);
		assertEquals("@Marker1", annotation.toString());
		abinding = annotation.resolveAnnotationBinding();
		assertEquals("@Marker1()", abinding.toString());

		// for constructor invocation results 2/4
		variableDeclarationStatement = (VariableDeclarationStatement) statements.get(1);
		fragment = (VariableDeclarationFragment) variableDeclarationStatement.fragments().get(0);
		expression = fragment.getInitializer();
		assertEquals("new @Marker1 @Marker2 Outer()", expression.toString());
		simpleType = (SimpleType) ((ClassInstanceCreation)expression).getType();
		assertEquals("@Marker1 @Marker2 Outer", simpleType.toString());
		annotation = (Annotation) simpleType.annotations().get(1);
		assertEquals("@Marker2", annotation.toString());
		abinding = annotation.resolveAnnotationBinding();
		assertEquals("@Marker2()", abinding.toString());

		// for constructor invocation results 3/4
		variableDeclarationStatement = (VariableDeclarationStatement) statements.get(2);
		fragment = (VariableDeclarationFragment) variableDeclarationStatement.fragments().get(0);
		expression = fragment.getInitializer();
		assertEquals("o.new @Marker1 @Marker2 Inner()", expression.toString());
		simpleType = (SimpleType) ((ClassInstanceCreation)expression).getType();
		assertEquals("@Marker1 @Marker2 Inner", simpleType.toString());
		annotation = (Annotation) simpleType.annotations().get(1);
		assertEquals("@Marker2", annotation.toString());
		abinding = annotation.resolveAnnotationBinding();
		assertEquals("@Marker2()", abinding.toString());

		// for constructor invocation results 4/4
		variableDeclarationStatement = (VariableDeclarationStatement) statements.get(3);
		fragment = (VariableDeclarationFragment) variableDeclarationStatement.fragments().get(0);
		expression = fragment.getInitializer();
		assertEquals("new <String>@Marker1 @Marker2 ZZ()", expression.toString());
		simpleType = (SimpleType) ((ClassInstanceCreation)expression).getType();
		assertEquals("@Marker1 @Marker2 ZZ", simpleType.toString());
		annotation = (Annotation) simpleType.annotations().get(1);
		assertEquals("@Marker2", annotation.toString());
		abinding = annotation.resolveAnnotationBinding();
		assertEquals("@Marker2()", abinding.toString());

		// annotations on wildcardtypes with TYPE_USE and TYPE_PARAMETER combination.
		WildcardType wildCardType = (WildcardType) type;
		assertEquals("@Marker2 @Marker3 ? super @Marker1 @Marker2 File", wildCardType.toString());
		assertTrue(wildCardType.annotations().size() == 2);
		annotation = (Annotation) wildCardType.annotations().get(1);
		assertEquals("@Marker3", annotation.toString());
		abinding = annotation.resolveAnnotationBinding();
		assertEquals("@Marker3()", abinding.toString());

		// simpleType for  wildcard bounds
		simpleType = (SimpleType) wildCardType.getBound();
		annotation = (Annotation) simpleType.annotations().get(1);
		assertEquals("@Marker2", annotation.toString());
		abinding = annotation.resolveAnnotationBinding();
		assertEquals("@Marker2()", abinding.toString());

		// class declaration with TYPE_PARAMETER annotation
		typedeclaration = (TypeDeclaration) getASTNode(cu, 1);
		typeParameter = (TypeParameter) typedeclaration.typeParameters().get(0);
		assertEquals("@Marker3 T", typeParameter.toString());
		assertTrue(typeParameter.modifiers().size() == 1);
		annotation = (Annotation)typeParameter.modifiers().get(0);
		assertEquals("@Marker3", annotation.toString());
		abinding = annotation.resolveAnnotationBinding();
		assertEquals("@Marker3()", abinding.toString());

		// for type tests
		methodDeclaration = (MethodDeclaration) getASTNode(cu, 1, 0);
		statements = methodDeclaration.getBody().statements();
		statement = (Statement)statements.get(0);
		ConditionalExpression conditionalExpression = (ConditionalExpression)((ReturnStatement)statement).getExpression();
		simpleType = (SimpleType) ((InstanceofExpression)conditionalExpression.getExpression()).getRightOperand();
		assertEquals("@Marker1 @Marker2 File", simpleType.toString());
		assertTrue(simpleType.annotations().size() == 2);
		annotation = (Annotation) simpleType.annotations().get(1);
		assertEquals("@Marker2", annotation.toString());

		// type annotation in front of a constructor declaration
		methodDeclaration = (MethodDeclaration) getASTNode(cu, 3, 0);
		annotation = (Annotation) methodDeclaration.modifiers().get(2);
		assertEquals("@Marker2", annotation.toString());
		abinding = annotation.resolveAnnotationBinding();
		assertEquals("@Marker2()", abinding.toString());

		// type annotation on "this"
		methodDeclaration = (MethodDeclaration) getASTNode(cu, 3, 1);
		simpleType = (SimpleType) methodDeclaration.getReceiverType();
		assertEquals("@Marker2 @Marker1 ZZ", simpleType.toString());
		annotation = (Annotation) simpleType.annotations().get(1);
		assertEquals("@Marker1", annotation.toString());
		abinding = annotation.resolveAnnotationBinding();
		assertEquals("@Marker1()", abinding.toString());
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=399793
	 */
	public void test399793a() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter18/src/test399793/X.java",
				true/* resolve */);
		String contents = """
			package test399793;\
			interface I {
				int foo(int x);
			}
			public class X {
			 I i =  vlambda -> {return 200;};
			}
			""";
		CompilationUnit cu = (CompilationUnit) buildAST(contents, this.workingCopy);
		TypeDeclaration typedeclaration = (TypeDeclaration) getASTNode(cu, 1);
		FieldDeclaration fieldDeclaration = (FieldDeclaration) typedeclaration.bodyDeclarations().get(0);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment)fieldDeclaration.fragments().get(0);
		Expression expression = fragment.getInitializer();
		assertTrue(expression instanceof LambdaExpression);
		LambdaExpression lambdaExpression = (LambdaExpression)expression;
		assertEquals("vlambda -> {\n  return 200;\n}\n", lambdaExpression.toString());
		assertTrue(lambdaExpression.parameters().size() == 1);
		IMethodBinding binding = lambdaExpression.resolveMethodBinding();
		assertEquals("public int foo(int) ", binding.toString());
		assertEquals("real modifiers", ClassFileConstants.AccPublic, binding.getModifiers());
		VariableDeclaration variableDeclaration = (VariableDeclaration) lambdaExpression.parameters().get(0);
		assertTrue(variableDeclaration instanceof VariableDeclarationFragment);
		fragment = (VariableDeclarationFragment)variableDeclaration;
		assertEquals("vlambda", fragment.toString());
		IVariableBinding variableBinding = fragment.resolveBinding();
		ITypeBinding typeBinding = variableBinding.getType();
		assertNotNull("Null Binding for lambda argument", typeBinding);
		assertEquals("binding of int expected for lambda","int",typeBinding.getName());
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=399793
	 */
	public void test399793b() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter18/src/test399793/X.java",
				true/* resolve */);
		String contents = """
			package test399793;\
			interface I {
				int foo(int x);
			}
			public class X {
			 I i =  vlambda -> 200;
			}
			""";
		CompilationUnit cu = (CompilationUnit) buildAST(contents, this.workingCopy);
		TypeDeclaration typedeclaration = (TypeDeclaration) getASTNode(cu, 1);
		FieldDeclaration fieldDeclaration = (FieldDeclaration) typedeclaration.bodyDeclarations().get(0);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment)fieldDeclaration.fragments().get(0);
		Expression expression = fragment.getInitializer();
		assertTrue(expression instanceof LambdaExpression);
		LambdaExpression lambdaExpression = (LambdaExpression)expression;
		assertEquals("vlambda -> 200", lambdaExpression.toString());
		IMethodBinding binding = lambdaExpression.resolveMethodBinding();
		assertEquals("public int foo(int) ", binding.toString());
		assertEquals("real modifiers", ClassFileConstants.AccPublic, binding.getModifiers());
		assertTrue(lambdaExpression.parameters().size() == 1);
		VariableDeclaration variableDeclaration = (VariableDeclaration) lambdaExpression.parameters().get(0);
		assertTrue(variableDeclaration instanceof VariableDeclarationFragment);
		fragment = (VariableDeclarationFragment)variableDeclaration;
		assertEquals("vlambda", fragment.toString());
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=399793
	 */
	public void test399793c() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter18/src/test399793/X.java",
				true/* resolve */);
		String contents = """
			package test399793;\
			interface I {
				Object foo(int [] ia);
			}
			public class X {
			 I i = (int [] ia) ->{
			  	return ia.clone();\
			};
			}
			""";
		CompilationUnit cu = (CompilationUnit) buildAST(contents, this.workingCopy);
		TypeDeclaration typedeclaration = (TypeDeclaration) getASTNode(cu, 1);
		FieldDeclaration fieldDeclaration = (FieldDeclaration) typedeclaration.bodyDeclarations().get(0);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment)fieldDeclaration.fragments().get(0);
		Expression expression = fragment.getInitializer();
		assertTrue(expression instanceof LambdaExpression);
		LambdaExpression lambdaExpression = (LambdaExpression)expression;
		assertEquals("(int[] ia) -> {\n  return ia.clone();\n}\n", lambdaExpression.toString());
		IMethodBinding binding = lambdaExpression.resolveMethodBinding();
		assertEquals("public java.lang.Object foo(int[]) ", binding.toString());
		assertEquals("real modifiers", ClassFileConstants.AccPublic, binding.getModifiers());
		assertTrue(lambdaExpression.parameters().size() == 1);
		VariableDeclaration variableDeclaration = (VariableDeclaration) lambdaExpression.parameters().get(0);
		assertTrue(variableDeclaration instanceof SingleVariableDeclaration);
		SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration)variableDeclaration;
		assertEquals("int[] ia", singleVariableDeclaration.toString());
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=399793
	 */
	public void test399793d() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter18/src/test399793/X.java",
				true/* resolve */);
		String contents = """
			package test399793;\
			interface I {
				void doit();
			}
			public class X {
					I i = () -> {
						System.out.println(this);
						I j = () -> {
							System.out.println(this);
							I k = () -> {
								System.out.println(this);
							};
						};
					};
				}
			""";
		CompilationUnit cu = (CompilationUnit) buildAST(contents, this.workingCopy);
		TypeDeclaration typedeclaration = (TypeDeclaration) getASTNode(cu, 1);
		FieldDeclaration fieldDeclaration = (FieldDeclaration) typedeclaration.bodyDeclarations().get(0);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment)fieldDeclaration.fragments().get(0);
		IVariableBinding variableBinding = fragment.resolveBinding();
		assertEquals("test399793.I i", variableBinding.toString());
		Expression expression = fragment.getInitializer();
		assertTrue(expression instanceof LambdaExpression);
		LambdaExpression lambdaExpression = (LambdaExpression)expression;
		assertEquals("() -> {\n  System.out.println(this);\n  I j=() -> {\n    System.out.println(this);\n    I k=() -> {\n      System.out.println(this);\n    }\n;\n  }\n;\n}\n", lambdaExpression.toString());
		IMethodBinding binding = lambdaExpression.resolveMethodBinding();
		assertEquals("public void doit() ", binding.toString());
		assertEquals("real modifiers", ClassFileConstants.AccPublic, binding.getModifiers());
		assertTrue(lambdaExpression.parameters().size() == 0);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=399794
	 * ReferenceExpression Family Tests
	 */
	public void test399794() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter18/src/test399794/X.java",
				true/* resolve */);
		String contents = """
			package test399794;\
			import java.lang.annotation.*;
			 \
			interface I {
			    Object copy(int [] ia);
			}
			interface J {
				void foo(int x);
			}
			class XX {
				public  void foo(int x) {}
			}
			
			class Y {
			       static class Z {
			               public static void foo(int x) {
			                       System.out.print(x);
			               }
			       }
			       public void foo(int x) {
			               System.out.print(x);
			       }
					public <T> void foo(T t){t.hashCode();}
			}
			
			public class X extends XX {
			       @SuppressWarnings("unused")
			       public  void bar(String [] args) {
			                Y y = new Y();
			                I i = @Marker int []::<String>clone;
			                J j = Y.@Marker Z  :: foo;
			                J j1 = Y.@Marker Z  :: <String> foo;
			                J jdash = @Marker W<@Marker Integer> :: <String> new ;
			                J jj = y :: foo;
			                J jx = super ::  foo;
					 	     class Z {
								void foo() {
									J jz = X.super :: foo;
								}
							}
			       }
			       public static void main (String [] args) {}
			}
			class W<T> extends Y {
			       public W(T x) {}
			}
			
			@Target (ElementType.TYPE_USE)
			@interface Marker {}""";

		CompilationUnit cu = (CompilationUnit) buildAST(contents, this.workingCopy);
		TypeDeclaration typeDeclaration = (TypeDeclaration) getASTNode(cu, 4);
		MethodDeclaration method = typeDeclaration.getMethods()[0];
		List statements = method.getBody().statements();
		assertTrue(statements.size() == 8);
		int fCount = 1;

		// type method reference with primitive type with type arguments
		VariableDeclarationStatement statement = (VariableDeclarationStatement) statements.get(fCount++);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) statement.fragments().get(0);
		Expression expression = fragment.getInitializer();
		assertTrue(expression instanceof TypeMethodReference);
		TypeMethodReference typeMethodReference = (TypeMethodReference) expression;
		checkSourceRange(typeMethodReference, "@Marker int []::<String>clone", contents);
		ITypeBinding typeBinding = typeMethodReference.resolveTypeBinding();
		assertNotNull(typeBinding);
		IMethodBinding methodBinding = typeMethodReference.resolveMethodBinding();
		assertNull(methodBinding);
		Type type = typeMethodReference.getType();
		checkSourceRange(type, "@Marker int []", contents);
		assertTrue(type.isArrayType());
		List typeArguments = typeMethodReference.typeArguments();
		assertTrue(typeArguments.size() == 1);
		type = (Type) typeArguments.get(0);
		checkSourceRange(type, "String", contents);
		assertTrue(type.isSimpleType());
		SimpleName name = typeMethodReference.getName();
		checkSourceRange(name, "clone", contents);
		typeBinding = name.resolveTypeBinding();
		assertNotNull(typeBinding);

		// type method reference with qualified type without type arguments
		statement = (VariableDeclarationStatement) statements.get(fCount++);
		fragment = (VariableDeclarationFragment) statement.fragments().get(0);
		expression = fragment.getInitializer();
		assertTrue(expression instanceof TypeMethodReference);
		typeMethodReference = (TypeMethodReference) expression;
		checkSourceRange(typeMethodReference, "Y.@Marker Z  :: foo", contents);
		typeBinding = typeMethodReference.resolveTypeBinding();
		assertNotNull(typeBinding);
		methodBinding = typeMethodReference.resolveMethodBinding();
		assertNotNull(methodBinding);
		type = typeMethodReference.getType();
		assertTrue(type.isNameQualifiedType());
		checkSourceRange(type, "Y.@Marker Z", contents);
		typeArguments = typeMethodReference.typeArguments();
		assertTrue(typeArguments.size() == 0);
		name = typeMethodReference.getName();
		checkSourceRange(name, "foo", contents);
		typeBinding = name.resolveTypeBinding();
		assertNotNull(typeBinding);

		// type method reference with qualified type with type arguments
		statement = (VariableDeclarationStatement) statements.get(fCount++);
		fragment = (VariableDeclarationFragment) statement.fragments().get(0);
		expression = fragment.getInitializer();
		assertTrue(expression instanceof TypeMethodReference);
		typeMethodReference = (TypeMethodReference) expression;
		checkSourceRange(typeMethodReference, "Y.@Marker Z  :: <String> foo", contents);
		typeBinding = typeMethodReference.resolveTypeBinding();
		assertNotNull(typeBinding);
		methodBinding = typeMethodReference.resolveMethodBinding();
		assertNotNull(methodBinding);
		type = typeMethodReference.getType();
		assertTrue(type.isNameQualifiedType());
		checkSourceRange(type, "Y.@Marker Z", contents);
		typeArguments = typeMethodReference.typeArguments();
		assertTrue(typeArguments.size() == 1);
		type = (Type) typeArguments.get(0);
		assertTrue(type.isSimpleType());
		checkSourceRange(type, "String", contents);
		name = typeMethodReference.getName();
		checkSourceRange(name, "foo", contents);
		typeBinding = name.resolveTypeBinding();
		assertNotNull(typeBinding);

		// creation method reference
		statement = (VariableDeclarationStatement) statements.get(fCount++);
		fragment = (VariableDeclarationFragment) statement.fragments().get(0);
		expression = fragment.getInitializer();
		assertTrue(expression instanceof CreationReference);
		CreationReference creationReference = (CreationReference) expression;
		checkSourceRange(creationReference, "@Marker W<@Marker Integer> :: <String> new", contents);
		typeBinding = creationReference.resolveTypeBinding();
		assertNotNull(typeBinding);
		methodBinding = creationReference.resolveMethodBinding();
		assertNotNull(methodBinding);
		type = creationReference.getType();
		checkSourceRange(type, "@Marker W<@Marker Integer>", contents);
		assertTrue(type instanceof ParameterizedType);
		assertASTNodeEquals("@Marker W<@Marker Integer>", type);
		typeArguments = creationReference.typeArguments();
		assertTrue(typeArguments.size() == 1);
		type = (Type) typeArguments.get(0);
		assertTrue(type.isSimpleType());
		checkSourceRange(type, "String", contents);

		// expression method reference
		statement = (VariableDeclarationStatement) statements.get(fCount++);
		fragment = (VariableDeclarationFragment) statement.fragments().get(0);
		expression = fragment.getInitializer();
		assertTrue(expression instanceof ExpressionMethodReference);
		ExpressionMethodReference expressionMethodReference = (ExpressionMethodReference) expression;
		checkSourceRange(expressionMethodReference, "y :: foo", contents);
		typeBinding = expressionMethodReference.resolveTypeBinding();
		assertNotNull(typeBinding);
		methodBinding = expressionMethodReference.resolveMethodBinding();
		assertNotNull(methodBinding);
		Expression lhs = expressionMethodReference.getExpression();
		checkSourceRange(lhs, "y", contents);
		typeArguments = expressionMethodReference.typeArguments();
		assertTrue(typeArguments.size() == 0);
		name = expressionMethodReference.getName();
		checkSourceRange(name, "foo", contents);
		typeBinding = name.resolveTypeBinding();
		assertNotNull(typeBinding);

		// super method reference without qualifier
		statement = (VariableDeclarationStatement) statements.get(fCount++);
		fragment = (VariableDeclarationFragment) statement.fragments().get(0);
		expression = fragment.getInitializer();
		assertTrue(expression instanceof SuperMethodReference);
		SuperMethodReference superMethodReference = (SuperMethodReference) expression;
		checkSourceRange(superMethodReference, "super ::  foo", contents);
		typeBinding = superMethodReference.resolveTypeBinding();
		assertNotNull(typeBinding);
		methodBinding = superMethodReference.resolveMethodBinding();
		assertNotNull(methodBinding);
		assertNull(superMethodReference.getQualifier());
		typeArguments = superMethodReference.typeArguments();
		assertTrue(typeArguments.size() == 0);
		name = superMethodReference.getName();
		checkSourceRange(name, "foo", contents);
		typeBinding = name.resolveTypeBinding();
		assertNotNull(typeBinding);

		// super method reference with qualifier
		TypeDeclarationStatement typeDeclarationStatement = (TypeDeclarationStatement) statements.get(fCount);
		typeDeclaration = (TypeDeclaration) typeDeclarationStatement.getDeclaration();
		method = typeDeclaration.getMethods()[0];
		statements = method.getBody().statements();
		assertTrue(statements.size() == 1);
		statement = (VariableDeclarationStatement) statements.get(0);
		fragment = (VariableDeclarationFragment) statement.fragments().get(0);
		expression = fragment.getInitializer();
		assertTrue(expression instanceof SuperMethodReference);
		superMethodReference = (SuperMethodReference) expression;
		checkSourceRange(superMethodReference, "X.super :: foo", contents);
		typeBinding = superMethodReference.resolveTypeBinding();
		assertNotNull(typeBinding);
		methodBinding = superMethodReference.resolveMethodBinding();
		assertNotNull(methodBinding);
		name = (SimpleName) superMethodReference.getQualifier();
		checkSourceRange(name, "X", contents);
		typeArguments = superMethodReference.typeArguments();
		assertTrue(typeArguments.size() == 0);
		name = superMethodReference.getName();
		checkSourceRange(name, "foo", contents);
		typeBinding = name.resolveTypeBinding();
		assertNotNull(typeBinding);

	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=399793
	 */
	public void test399793e() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter18/src/test399793/X.java",
				true/* resolve */);
		String contents = """
			package test399793;\
			interface I {
			  J foo();
			}
			interface J {
			  int foo();
			}
			public class X {
			    I I = () -> () -> 10;
			}
			""";

		CompilationUnit cu = (CompilationUnit) buildAST(contents, this.workingCopy);
		TypeDeclaration typedeclaration = (TypeDeclaration) getASTNode(cu, 2);
		FieldDeclaration fieldDeclaration = (FieldDeclaration) typedeclaration.bodyDeclarations().get(0);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment)fieldDeclaration.fragments().get(0);
		Expression expression = fragment.getInitializer();
		assertTrue(expression instanceof LambdaExpression);
		LambdaExpression lambdaExpression = (LambdaExpression)expression;
		assertEquals("() -> () -> 10", lambdaExpression.toString());
		IMethodBinding binding = lambdaExpression.resolveMethodBinding();
		assertEquals("public test399793.J foo() ", binding.toString());
		assertEquals("real modifiers", ClassFileConstants.AccPublic, binding.getModifiers());
		assertTrue(lambdaExpression.parameters().size() == 0);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=402665
	 */
	public void test402665a() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter18/src/test402665/X.java",
				true/* resolve */);
		String contents = """
			package test402665;\
			public class X {
			  public static interface StringToInt {
			   	int stoi(String s);
			  }
			  public static interface ReduceInt {
			      int reduce(int a, int b);
			  }
			  void foo(StringToInt s) { }
			  void bar(ReduceInt r) { }
			  void bar() {
			      foo(s -> s.length());
			      foo((s) -> s.length());
			      foo((String s) -> s.length()); //SingleVariableDeclaration is OK
			      bar((x, y) -> x+y);
			      bar((int x, int y) -> x+y); //SingleVariableDeclarations are OK
			  }
			}
			""";

		CompilationUnit cu = (CompilationUnit) buildAST(contents, this.workingCopy);
		TypeDeclaration typedeclaration = (TypeDeclaration) getASTNode(cu, 0);
		MethodDeclaration methoddecl = (MethodDeclaration)typedeclaration.bodyDeclarations().get(4);
		List statements = methoddecl.getBody().statements();
		int sCount = 0;

		ExpressionStatement statement = (ExpressionStatement)statements.get(sCount++);
		MethodInvocation methodInvocation = (MethodInvocation)statement.getExpression();
		LambdaExpression lambdaExpression = (LambdaExpression) methodInvocation.arguments().get(0);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment)lambdaExpression.parameters().get(0);
		checkSourceRange(fragment, "s", contents);

		statement = (ExpressionStatement)statements.get(sCount++);
		methodInvocation = (MethodInvocation)statement.getExpression();
		lambdaExpression = (LambdaExpression) methodInvocation.arguments().get(0);
		fragment = (VariableDeclarationFragment)lambdaExpression.parameters().get(0);
		checkSourceRange(fragment, "s", contents);

		statement = (ExpressionStatement)statements.get(sCount++);
		methodInvocation = (MethodInvocation)statement.getExpression();
		lambdaExpression = (LambdaExpression) methodInvocation.arguments().get(0);
		SingleVariableDeclaration singleVarDecl = (SingleVariableDeclaration)lambdaExpression.parameters().get(0);
		checkSourceRange(singleVarDecl, "String s", contents);

		statement = (ExpressionStatement)statements.get(sCount++);
		methodInvocation = (MethodInvocation)statement.getExpression();
		lambdaExpression = (LambdaExpression) methodInvocation.arguments().get(0);
		fragment = (VariableDeclarationFragment)lambdaExpression.parameters().get(0);
		checkSourceRange(fragment, "x", contents);
		fragment = (VariableDeclarationFragment)lambdaExpression.parameters().get(1);
		checkSourceRange(fragment, "y", contents);

		statement = (ExpressionStatement)statements.get(sCount++);
		methodInvocation = (MethodInvocation)statement.getExpression();
		lambdaExpression = (LambdaExpression) methodInvocation.arguments().get(0);
		singleVarDecl = (SingleVariableDeclaration)lambdaExpression.parameters().get(0);
		checkSourceRange(singleVarDecl, "int x", contents);
		singleVarDecl = (SingleVariableDeclaration)lambdaExpression.parameters().get(1);
		checkSourceRange(singleVarDecl, "int y", contents);
	}
	public void testBug403132() throws JavaModelException {
		String contents =
			"""
			import java.lang.annotation.*;
			public class X {
				class Y {
					class Z {
						public Z(@A X.@B Y Y.this,String str){
			}\
			    	 	public void foo(@A X.@B Y.@C Z this,String str){
			}
					}
			    }
			}
			@Target(ElementType.TYPE_USE)
			@interface A {}
			@Target(ElementType.TYPE_USE)
			@interface B {}
			@Target(ElementType.TYPE_USE)
			@interface C {}
			""";
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		node = getASTNode(unit, 0, 0);
		TypeDeclaration type = (TypeDeclaration)node;
		node = (ASTNode) type.bodyDeclarations().get(0);
		type = (TypeDeclaration) node;
		node = (ASTNode) type.bodyDeclarations().get(0);
		assertEquals("Not a method Declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration method = (MethodDeclaration) node;
		Type receiver = method.getReceiverType();
		assertEquals("Not a qualified type", ASTNode.QUALIFIED_TYPE, receiver.getNodeType());
		assertEquals("Incorrect receiver", "@A X.@B Y", ((QualifiedType) receiver).toString());
		assertEquals("Incorrect method signature", "public Z(@A X.@B Y Y.this,String str){\n}\n", method.toString());

		method = (MethodDeclaration) type.bodyDeclarations().get(1);
		receiver = method.getReceiverType();
		assertEquals("Incorrect receiver", "@A X.@B Y.@C Z", ((QualifiedType) receiver).toString());
		assertEquals("Incorrect method signature", "public void foo(@A X.@B Y.@C Z this,String str){\n}\n", method.toString());
	}
	public void testParameterizedReceiverType() throws JavaModelException {
		String contents =
				"""
			import java.lang.annotation.*;
			public class X<T extends Exception> {
				class Y<K, V> {
					class Z {
						public Z(@A X<T>.@B Y<K, V> Y.this, boolean a){ }
						public void foo(@B Y<K, V>.@C Z this, boolean a){ }
						public Z(X<T>.@B Y<K, V> Y.this){ }
						public void foo(Y<K, V>.@C Z this){ }
					}
				}
			}
			@Target(ElementType.TYPE_USE)
			@interface A {}
			@Target(ElementType.TYPE_USE)
			@interface B {}
			@Target(ElementType.TYPE_USE)
			@interface C {}
			""";
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		node = getASTNode(unit, 0, 0);
		TypeDeclaration type = (TypeDeclaration)node;
		node = (ASTNode) type.bodyDeclarations().get(0);
		type = (TypeDeclaration) node;

		MethodDeclaration method = (MethodDeclaration) type.bodyDeclarations().get(0);
		Type receiver = method.getReceiverType();
		assertEquals("Not a ParameterizedType", ASTNode.PARAMETERIZED_TYPE, receiver.getNodeType());
		checkSourceRange(receiver, "@A X<T>.@B Y<K, V>", contents);
		assertEquals("Incorrect method signature", "public Z(@A X<T>.@B Y<K,V> Y.this,boolean a){\n}\n", method.toString());

		method = (MethodDeclaration) type.bodyDeclarations().get(1);
		receiver = method.getReceiverType();
		assertEquals("Not a QualifiedType", ASTNode.QUALIFIED_TYPE, receiver.getNodeType());
		checkSourceRange(receiver, "@B Y<K, V>.@C Z", contents);
		assertEquals("Incorrect method signature", "public void foo(@B Y<K,V>.@C Z this,boolean a){\n}\n", method.toString());

		method = (MethodDeclaration) type.bodyDeclarations().get(2);
		receiver = method.getReceiverType();
		assertEquals("Not a ParameterizedType", ASTNode.PARAMETERIZED_TYPE, receiver.getNodeType());
		checkSourceRange(receiver, "X<T>.@B Y<K, V>", contents);
		assertEquals("Incorrect method signature", "public Z(X<T>.@B Y<K,V> Y.this){\n}\n", method.toString());

		method = (MethodDeclaration) type.bodyDeclarations().get(3);
		receiver = method.getReceiverType();
		assertEquals("Not a QualifiedType", ASTNode.QUALIFIED_TYPE, receiver.getNodeType());
		checkSourceRange(receiver, "Y<K, V>.@C Z", contents);
		assertEquals("Incorrect method signature", "public void foo(Y<K,V>.@C Z this){\n}\n", method.toString());
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=403410
	public void testBug403410() throws JavaModelException {
		String contents =
			"""
			import java.lang.annotation.*;
			public class X {
				class Y {
					class Z {
						public Z(final Y Y.this){
			}\
			    	 	public void foo(static @A Z this){
			}
					}
			    }
			}
			@Target(ElementType.TYPE_USE)
			@interface A {}
			""";
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", false);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		node = getASTNode(unit, 0, 0);
		TypeDeclaration type = (TypeDeclaration)node;
		node = (ASTNode) type.bodyDeclarations().get(0);
		type = (TypeDeclaration) node;
		node = (ASTNode) type.bodyDeclarations().get(0);
		assertEquals("Not a method Declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration method = (MethodDeclaration) node;
		assertEquals("Type should be malformed", ASTNode.MALFORMED, (method.getFlags() & ASTNode.MALFORMED));

		method = (MethodDeclaration) type.bodyDeclarations().get(1);
		assertEquals("Type should be malformed", ASTNode.MALFORMED, (method.getFlags() & ASTNode.MALFORMED));
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=402674
	 */
	public void test402674() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter18/src/test402674/X.java",
				true/* resolve */);
		String contents = """
			package test402674;\
			public class X {
			  public static interface StringToInt {
			   	int stoi(String s);
			  }
			  public static interface ReduceInt {
			      int reduce(int a, int b);
			  }
			  void foo(StringToInt s) { }
			  void bar(ReduceInt r) { }
			  void bar() {
			      foo(s -> s.length());
			      foo((s) -> s.length());
			      foo((String s) -> s.length()); //SingleVariableDeclaration is OK
			      bar((x, y) -> x+y);
			      bar((int x, int y) -> x+y); //SingleVariableDeclarations are OK
			  }
			}
			""";

		CompilationUnit cu = (CompilationUnit) buildAST(contents, this.workingCopy);
		TypeDeclaration typedeclaration = (TypeDeclaration) getASTNode(cu, 0);
		MethodDeclaration methoddecl = (MethodDeclaration)typedeclaration.bodyDeclarations().get(4);
		List statements = methoddecl.getBody().statements();
		int sCount = 0;

		ExpressionStatement statement = (ExpressionStatement)statements.get(sCount++);
		MethodInvocation methodInvocation = (MethodInvocation)statement.getExpression();
		LambdaExpression lambdaExpression = (LambdaExpression) methodInvocation.arguments().get(0);
		ITypeBinding binding = lambdaExpression.resolveTypeBinding();
		assertNotNull(binding);
		assertEquals("StringToInt", binding.getName());

		statement = (ExpressionStatement)statements.get(sCount++);
		methodInvocation = (MethodInvocation)statement.getExpression();
		lambdaExpression = (LambdaExpression) methodInvocation.arguments().get(0);
		binding = lambdaExpression.resolveTypeBinding();
		assertNotNull(binding);
		assertEquals("StringToInt", binding.getName());

		statement = (ExpressionStatement)statements.get(sCount++);
		methodInvocation = (MethodInvocation)statement.getExpression();
		lambdaExpression = (LambdaExpression) methodInvocation.arguments().get(0);
		binding = lambdaExpression.resolveTypeBinding();
		assertNotNull(binding);
		assertEquals("StringToInt", binding.getName());

		statement = (ExpressionStatement)statements.get(sCount++);
		methodInvocation = (MethodInvocation)statement.getExpression();
		lambdaExpression = (LambdaExpression) methodInvocation.arguments().get(0);
		binding = lambdaExpression.resolveTypeBinding();
		assertNotNull(binding);
		assertEquals("ReduceInt", binding.getName());

		statement = (ExpressionStatement)statements.get(sCount++);
		methodInvocation = (MethodInvocation)statement.getExpression();
		lambdaExpression = (LambdaExpression) methodInvocation.arguments().get(0);
		binding = lambdaExpression.resolveTypeBinding();
		assertNotNull(binding);
		assertEquals("ReduceInt", binding.getName());
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399791
	public void testBug399791() throws JavaModelException {
		String contents =
			"""
			public interface X {
				static void foo(){}
			   public default void foo(int i){}
			   native void foo(float f){}
			   abstract void foo(long l){}
			}
			""";
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", false);
		ASTNode node = buildAST(contents, this.workingCopy, false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		TypeDeclaration type =  (TypeDeclaration) unit.types().get(0);
		node = (ASTNode) type.bodyDeclarations().get(0);
		assertEquals("Not a method Declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration method = (MethodDeclaration) node;
		assertEquals("Method should not be malformed", 0, (method.getFlags() & ASTNode.MALFORMED));
		List modifiers = method.modifiers();
		assertEquals("Incorrect no of modfiers", 1, modifiers.size());
		Modifier modifier = (Modifier) modifiers.get(0);
		assertSame("Incorrect modifier keyword", Modifier.ModifierKeyword.STATIC_KEYWORD, modifier.getKeyword());

		method = (MethodDeclaration) type.bodyDeclarations().get(1);
		assertEquals("Method should not be malformed", 0, (method.getFlags() & ASTNode.MALFORMED));

		modifiers = method.modifiers();
		assertEquals("Incorrect no of modfiers", 2, modifiers.size());
		modifier = (Modifier) modifiers.get(1);
		assertSame("Incorrect modifier keyword", Modifier.ModifierKeyword.DEFAULT_KEYWORD, modifier.getKeyword());
		assertTrue("Incorrect modifier", modifier.isDefault());
		assertEquals("Incorrect AST", "public default void foo(int i){\n}\n", method.toString());

		method = (MethodDeclaration) type.bodyDeclarations().get(2);
		assertEquals("Method should be malformed", ASTNode.MALFORMED, (method.getFlags() & ASTNode.MALFORMED));

		method = (MethodDeclaration) type.bodyDeclarations().get(3);
		assertEquals("Method should be malformed", ASTNode.MALFORMED, (method.getFlags() & ASTNode.MALFORMED));
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=404489
	public void testBug404489a() throws JavaModelException {
		String contents =
		"""
			package test404489.bug;
			public class X {\s
				class Y {\s
					class Z {
						public Z(@A X.@B Y Y.this){}
						}
			  		}
			  		Object o=(@A X.@B Y.@Marker  Z)null;
				}
			@java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE_USE) @interface Marker {}\s
			@java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE_USE) @interface A {}\s
			@java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE_USE) @interface B {}\s
			""";
		this.workingCopy = getWorkingCopy("/Converter18/src/test404489/bug/X.java", true/* resolve */);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		TypeDeclaration type =  (TypeDeclaration) unit.types().get(0);
		node = (ASTNode) type.bodyDeclarations().get(0);
		assertEquals("Not a Type Declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		node = (ASTNode) ((TypeDeclaration)node).bodyDeclarations().get(0);
		assertEquals("Not a Type Declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		node = (ASTNode) ((TypeDeclaration)node).bodyDeclarations().get(0);
		assertEquals("Not a method Declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration method = (MethodDeclaration) node;
		assertEquals("Method should not be malformed", 0, (method.getFlags() & ASTNode.MALFORMED));
		Type annotatableType = method.getReceiverType();
		assertTrue(annotatableType.isQualifiedType());
		QualifiedType qualifiedType = (QualifiedType) annotatableType;
		assertEquals("wrong qualified type", "@A X.@B Y", qualifiedType.toString());
		ITypeBinding binding = qualifiedType.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong qualified name", "test404489.bug.X.Y", binding.getQualifiedName());
		SimpleType simpleType = (SimpleType) qualifiedType.getQualifier();
		assertEquals("incorrect type", "@A X", simpleType.toString());
		binding = simpleType.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong qualified name", "test404489.bug.X", binding.getQualifiedName());
		List annotations = qualifiedType.annotations();
		assertTrue(annotations.size() == 1);
		MarkerAnnotation marker	= (MarkerAnnotation) annotations.get(0);
		assertEquals("wrong annotation name", "@B", marker.toString());
		binding = marker.resolveTypeBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong qualified name", "test404489.bug.B", binding.getQualifiedName());
		IAnnotationBinding annotationBinding = marker.resolveAnnotationBinding();
		assertNotNull(annotationBinding);
		assertEquals("wrong annotation binding", "B", annotationBinding.getName());
		Name name = marker.getTypeName();
		assertTrue(name.isSimpleName());
		SimpleName simpleName = (SimpleName) name;
		assertEquals("wrong type name", "B", simpleName.toString());
		assertEquals("wrong simple name", "B",simpleName.getIdentifier());
		binding = simpleName.resolveTypeBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong qualified name", "test404489.bug.B", binding.getQualifiedName());
		assertTrue(qualifiedType.getQualifier().isSimpleType());
		simpleType = (SimpleType) qualifiedType.getQualifier();
		assertEquals("incorrect type", "@A X", simpleType.toString());
		binding = simpleType.resolveBinding();
		assertNotNull("No binding", binding);
		assertEquals("Wrong qualified name", "test404489.bug.X", binding.getQualifiedName());
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=404489
	public void testBug404489b() throws JavaModelException {
		ICompilationUnit sourceUnit = getCompilationUnit("Converter18" , "src", "test404489.bug", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(this.ast.apiLevel(), sourceUnit, true);
		char[] source = sourceUnit.getSource().toCharArray();
		assertTrue("Not a compilation unit", result.getNodeType() == ASTNode.COMPILATION_UNIT);
		CompilationUnit compilationUnit = (CompilationUnit) result;
		assertProblemsSize(compilationUnit, 0);
		ASTNode node = getASTNode(compilationUnit, 0, 0, 0);
		TypeDeclaration typeDeclaration =  (TypeDeclaration) compilationUnit.types().get(0);

		node = (ASTNode) typeDeclaration.bodyDeclarations().get(2);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration methodDecl = (MethodDeclaration) node;
		Type type = methodDecl.getReturnType2();
		assertTrue(type.isNameQualifiedType());
		NameQualifiedType nameQualifiedType = (NameQualifiedType) type;
		checkSourceRange(nameQualifiedType, "test404489.bug.@NonNull IOException", source);
		ITypeBinding typeBinding = nameQualifiedType.resolveBinding();
		assertNotNull("null binding", typeBinding);
		assertEquals("not a valid binding", "test404489.bug.IOException", typeBinding.getQualifiedName());

		// qualifier of the name qualified type
		Name name = nameQualifiedType.getQualifier();
		assertTrue(name.isQualifiedName());
		QualifiedName qualifiedName = (QualifiedName) name;
		checkSourceRange(qualifiedName, "test404489.bug", source);
		typeBinding = qualifiedName.resolveTypeBinding();
		assertNull(typeBinding);
		IBinding binding = qualifiedName.resolveBinding();
		assertTrue("not a package binding", binding.getKind() == IBinding.PACKAGE);
		assertEquals("wrong package binding", "package test404489.bug", binding.toString());
		name = qualifiedName.getQualifier();
		assertTrue("wrong name type", name.isSimpleName());
		SimpleName simpleName = (SimpleName) name;
		checkSourceRange(simpleName, "test404489", source);
		typeBinding = simpleName.resolveTypeBinding();
		assertNull(typeBinding);
		binding = simpleName.resolveBinding();
		assertTrue("not a package binding", binding.getKind() == IBinding.PACKAGE);
		assertEquals("wrong package binding", "package test404489", binding.toString());
		simpleName = qualifiedName.getName();
		checkSourceRange(simpleName, "bug", source);
		typeBinding = simpleName.resolveTypeBinding();
		assertNull(typeBinding);
		binding = simpleName.resolveBinding();
		assertTrue("not a package binding", binding.getKind() == IBinding.PACKAGE);
		assertEquals("wrong package binding", "package test404489.bug", binding.toString());

		// annotations of name qualified type
		List annotations = nameQualifiedType.annotations();
		assertTrue(annotations.size() == 1);
		Annotation annotation = (Annotation) annotations.get(0);
		typeBinding = annotation.resolveTypeBinding();
		assertNotNull("null binding", typeBinding);
		assertEquals("not a valid binding", "test404489.bug.X.NonNull", typeBinding.getQualifiedName());
		IAnnotationBinding annotationBinding = annotation.resolveAnnotationBinding();
		assertEquals("not a valid annotation binding", "@NonNull()", annotationBinding.toString());
		name = annotation.getTypeName();
		assertTrue(name.isSimpleName());
		simpleName = (SimpleName) name;
		typeBinding = simpleName.resolveTypeBinding();
		checkSourceRange(simpleName, "NonNull", source);
		assertNotNull(typeBinding);

		// name of the name qualified type
		simpleName = nameQualifiedType.getName();
		checkSourceRange(simpleName, "IOException", source);
		typeBinding = simpleName.resolveTypeBinding();
		assertNotNull(typeBinding);

		// parameter
		SingleVariableDeclaration param = (SingleVariableDeclaration) methodDecl.parameters().get(0);
		type = param.getType();
		assertTrue(type.isNameQualifiedType());
		nameQualifiedType = (NameQualifiedType) type;
		checkSourceRange(nameQualifiedType, "test404489.bug.@NonNull FileNotFoundException", source);
		typeBinding = nameQualifiedType.resolveBinding();
		assertNotNull("null binding", typeBinding);
		assertEquals("not a valid binding", "test404489.bug.FileNotFoundException", typeBinding.getQualifiedName());

		// qualifier of the name qualified type
		name = nameQualifiedType.getQualifier();
		assertTrue(name.isQualifiedName());
		qualifiedName = (QualifiedName) name;
		checkSourceRange(qualifiedName, "test404489.bug", source);
		typeBinding = qualifiedName.resolveTypeBinding();
		assertNull(typeBinding);
		binding = qualifiedName.resolveBinding();
		assertTrue("not a package binding", binding.getKind() == IBinding.PACKAGE);
		assertEquals("wrong package binding", "package test404489.bug", binding.toString());
		name = qualifiedName.getQualifier();
		assertTrue("wrong name type", name.isSimpleName());
		simpleName = (SimpleName) name;
		checkSourceRange(simpleName, "test404489", source);
		typeBinding = simpleName.resolveTypeBinding();
		assertNull(typeBinding);
		binding = simpleName.resolveBinding();
		assertTrue("not a package binding", binding.getKind() == IBinding.PACKAGE);
		assertEquals("wrong package binding", "package test404489", binding.toString());
		simpleName = qualifiedName.getName();
		checkSourceRange(simpleName, "bug", source);
		typeBinding = simpleName.resolveTypeBinding();
		assertNull(typeBinding);
		binding = simpleName.resolveBinding();
		assertTrue("not a package binding", binding.getKind() == IBinding.PACKAGE);
		assertEquals("wrong package binding", "package test404489.bug", binding.toString());

		// annotations of name qualified type
		annotations = nameQualifiedType.annotations();
		assertTrue(annotations.size() == 1);
		annotation = (Annotation) annotations.get(0);
		typeBinding = annotation.resolveTypeBinding();
		assertNotNull("null binding", typeBinding);
		assertEquals("not a valid binding", "test404489.bug.X.NonNull", typeBinding.getQualifiedName());
		annotationBinding = annotation.resolveAnnotationBinding();
		assertEquals("not a valid annotation binding", "@NonNull()", annotationBinding.toString());
		name = annotation.getTypeName();
		assertTrue(name.isSimpleName());
		simpleName = (SimpleName) name;
		typeBinding = simpleName.resolveTypeBinding();
		checkSourceRange(simpleName, "NonNull", source);
		assertNotNull(typeBinding);

		// name of the name qualified type
		simpleName = nameQualifiedType.getName();
		checkSourceRange(simpleName, "FileNotFoundException", source);
		typeBinding = simpleName.resolveTypeBinding();
		assertNotNull(typeBinding);

		// throws
		type = (Type) methodDecl.thrownExceptionTypes().get(0);
		assertTrue(type.isNameQualifiedType());
		nameQualifiedType = (NameQualifiedType) type;
		checkSourceRange(nameQualifiedType, "test404489.bug.@NonNull EOFException", source);
		typeBinding = nameQualifiedType.resolveBinding();
		assertNotNull("null binding", typeBinding);
		assertEquals("not a valid binding", "test404489.bug.EOFException", typeBinding.getQualifiedName());

		// qualifier of the name qualified type
		name = nameQualifiedType.getQualifier();
		assertTrue(name.isQualifiedName());
		qualifiedName = (QualifiedName) name;
		checkSourceRange(qualifiedName, "test404489.bug", source);
		typeBinding = qualifiedName.resolveTypeBinding();
		assertNull(typeBinding);
		binding = qualifiedName.resolveBinding();
		assertTrue("not a package binding", binding.getKind() == IBinding.PACKAGE);
		assertEquals("wrong package binding", "package test404489.bug", binding.toString());
		name = qualifiedName.getQualifier();
		assertTrue("wrong name type", name.isSimpleName());
		simpleName = (SimpleName) name;
		checkSourceRange(simpleName, "test404489", source);
		typeBinding = simpleName.resolveTypeBinding();
		assertNull(typeBinding);
		binding = simpleName.resolveBinding();
		assertTrue("not a package binding", binding.getKind() == IBinding.PACKAGE);
		assertEquals("wrong package binding", "package test404489", binding.toString());
		simpleName = qualifiedName.getName();
		checkSourceRange(simpleName, "bug", source);
		typeBinding = simpleName.resolveTypeBinding();
		assertNull(typeBinding);
		binding = simpleName.resolveBinding();
		assertTrue("not a package binding", binding.getKind() == IBinding.PACKAGE);
		assertEquals("wrong package binding", "package test404489.bug", binding.toString());

		// annotations of name qualified type
		annotations = nameQualifiedType.annotations();
		assertTrue(annotations.size() == 1);
		annotation = (Annotation) annotations.get(0);
		typeBinding = annotation.resolveTypeBinding();
		assertNotNull("null binding", typeBinding);
		assertEquals("not a valid binding", "test404489.bug.X.NonNull", typeBinding.getQualifiedName());
		annotationBinding = annotation.resolveAnnotationBinding();
		assertEquals("not a valid annotation binding", "@NonNull()", annotationBinding.toString());
		name = annotation.getTypeName();
		assertTrue(name.isSimpleName());
		simpleName = (SimpleName) name;
		typeBinding = simpleName.resolveTypeBinding();
		checkSourceRange(simpleName, "NonNull", source);
		assertNotNull(typeBinding);

		// name of the name qualified type
		simpleName = nameQualifiedType.getName();
		checkSourceRange(simpleName, "EOFException", source);
		typeBinding = simpleName.resolveTypeBinding();
		assertNotNull(typeBinding);

		node = (ASTNode) typeDeclaration.bodyDeclarations().get(3);
		assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
		FieldDeclaration field = (FieldDeclaration) node;
		type = field.getType();
		assertTrue(type.isQualifiedType());
		QualifiedType qualifiedType = (QualifiedType) type;
		nameQualifiedType = (NameQualifiedType)qualifiedType.getQualifier();
		checkSourceRange(nameQualifiedType, "test404489.bug.@NonNull X", source);
		typeBinding = nameQualifiedType.resolveBinding();
		assertNotNull("null binding", typeBinding);
		assertEquals("not a valid binding", "test404489.bug.X", typeBinding.getQualifiedName());
		name = nameQualifiedType.getName();
		assertSame("bindings different for name qualified type and assocated name", typeBinding, name.resolveTypeBinding());

		// qualifier of the name qualified type
		name = nameQualifiedType.getQualifier();
		assertTrue(name.isQualifiedName());
		qualifiedName = (QualifiedName) name;
		checkSourceRange(qualifiedName, "test404489.bug", source);
		typeBinding = qualifiedName.resolveTypeBinding();
		assertNull(typeBinding);
		binding = qualifiedName.resolveBinding();
		assertTrue("not a package binding", binding.getKind() == IBinding.PACKAGE);
		assertEquals("wrong package binding", "package test404489.bug", binding.toString());
		name = qualifiedName.getQualifier();
		assertTrue("wrong name type", name.isSimpleName());
		simpleName = (SimpleName) name;
		checkSourceRange(simpleName, "test404489", source);
		typeBinding = simpleName.resolveTypeBinding();
		assertNull(typeBinding);
		binding = simpleName.resolveBinding();
		assertTrue("not a package binding", binding.getKind() == IBinding.PACKAGE);
		assertEquals("wrong package binding", "package test404489", binding.toString());
		simpleName = qualifiedName.getName();
		checkSourceRange(simpleName, "bug", source);
		typeBinding = simpleName.resolveTypeBinding();
		assertNull(typeBinding);
		binding = simpleName.resolveBinding();
		assertTrue("not a package binding", binding.getKind() == IBinding.PACKAGE);
		assertEquals("wrong package binding", "package test404489.bug", binding.toString());

		// annotations of name qualified type
		annotations = nameQualifiedType.annotations();
		assertTrue(annotations.size() == 1);
		annotation = (Annotation) annotations.get(0);
		typeBinding = annotation.resolveTypeBinding();
		assertNotNull("null binding", typeBinding);
		assertEquals("not a valid binding", "test404489.bug.X.NonNull", typeBinding.getQualifiedName());
		annotationBinding = annotation.resolveAnnotationBinding();
		assertEquals("not a valid annotation binding", "@NonNull()", annotationBinding.toString());
		name = annotation.getTypeName();
		assertTrue(name.isSimpleName());
		simpleName = (SimpleName) name;
		typeBinding = simpleName.resolveTypeBinding();
		checkSourceRange(simpleName, "NonNull", source);
		assertNotNull(typeBinding);

		// name of the name qualified type
		simpleName = nameQualifiedType.getName();
		checkSourceRange(simpleName, "X", source);
		typeBinding = simpleName.resolveTypeBinding();
		assertNotNull(typeBinding);

	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399792
	public void testBug399792() throws JavaModelException {
		String content =
				"""
			import java.lang.annotation.ElementType;
			import java.io.Serializable;
			public class X {
			      Object o = (@Marker1 @Marker2 Serializable & I & @Marker3 @Marker1 J) () -> {};\
			      public Serializable main(Object o) {
			    	  Serializable oo = (Serializable & @Marker3 @Marker1 @Marker2 I & J) o;
			    	  return oo;
			      }
			}
			interface I {
			  public void foo();
			}
			interface J {
			  public void foo();
			  public void bar();
			}
			interface K {
			  public void foo();
			  public void bar();
			}
			@java.lang.annotation.Target (ElementType.TYPE_USE)
			@interface Marker {}
			@java.lang.annotation.Target (ElementType.TYPE_USE)
			@interface Marker2 {}
			@java.lang.annotation.Target (ElementType.TYPE_USE)
			@interface Marker3 {}""";

		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", false);
		ASTNode node = buildAST(content, this.workingCopy, false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		TypeDeclaration type =  (TypeDeclaration) unit.types().get(0);
		node = (ASTNode) type.bodyDeclarations().get(0);
		assertEquals("Not a field Declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
		FieldDeclaration field = (FieldDeclaration) node;
		assertEquals("Field should not be malformed", 0, (field.getFlags() & ASTNode.MALFORMED));

		List fragments = field.fragments();
		assertEquals("Incorrect no of fragments", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		CastExpression cast = (CastExpression) fragment.getInitializer();
		Type castType = cast.getType();
		assertEquals("Not an intersection cast type", ASTNode.INTERSECTION_TYPE, castType.getNodeType());
		assertTrue("Not an intersection cast type", castType.isIntersectionType());
		assertEquals("Type should not be malformed", 0, (castType.getFlags() & ASTNode.MALFORMED));

		List intersectionTypes = ((IntersectionType) castType).types();
		assertEquals("Incorrect no of types", 3, intersectionTypes.size());
		castType = (Type) intersectionTypes.get(0);
		assertEquals("Incorrect type", ASTNode.SIMPLE_TYPE, castType.getNodeType());
		SimpleName name = (SimpleName) ((SimpleType) castType).getName();
		assertEquals("Incorrect name", "Serializable", name.getIdentifier());

		List annotations = ((SimpleType) castType).annotations();
		assertEquals("Incorrect no of annotations", 2, annotations.size());
		assertEquals("Incorrect receiver", "@Marker1 @Marker2 Serializable", castType.toString());

		castType = (Type) intersectionTypes.get(1);
		assertEquals("Incorrect type", ASTNode.SIMPLE_TYPE, castType.getNodeType());
		name = (SimpleName) ((SimpleType) castType).getName();
		assertEquals("Incorrect name", "I", name.getIdentifier());

		annotations = ((SimpleType) castType).annotations();
		assertEquals("Incorrect no of annotations", 0, annotations.size());
		assertEquals("Incorrect receiver", "I", castType.toString());

		castType = (Type) intersectionTypes.get(2);
		assertEquals("Incorrect type", ASTNode.SIMPLE_TYPE, castType.getNodeType());
		name = (SimpleName) ((SimpleType) castType).getName();
		assertEquals("Incorrect name", "J", name.getIdentifier());

		annotations = ((SimpleType) castType).annotations();
		assertEquals("Incorrect no of annotations", 2, annotations.size());
		assertEquals("Incorrect receiver", "@Marker3 @Marker1 J", castType.toString());

		node = (ASTNode) type.bodyDeclarations().get(1);
		assertEquals("Not a method Declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration method = (MethodDeclaration) node;
		assertEquals("Method should not be malformed", 0, (method.getFlags() & ASTNode.MALFORMED));

		List statements = method.getBody().statements();
		VariableDeclarationStatement statement = (VariableDeclarationStatement) statements.get(0);
		fragment = (VariableDeclarationFragment) statement.fragments().get(0);
		cast = (CastExpression) fragment.getInitializer();
		castType = cast.getType();

		intersectionTypes = ((IntersectionType) castType).types();
		assertEquals("Incorrect no of types", 3, intersectionTypes.size());
		castType = (Type) intersectionTypes.get(0);

		annotations = ((SimpleType) castType).annotations();
		assertEquals("Incorrect no of annotations", 0, annotations.size());
		assertEquals("Incorrect receiver", "Serializable", castType.toString());

		castType = (Type) intersectionTypes.get(1);
		annotations = ((SimpleType) castType).annotations();
		assertEquals("Incorrect no of annotations", 3, annotations.size());
		assertEquals("Incorrect receiver", "@Marker3 @Marker1 @Marker2 I", castType.toString());

		castType = (Type) intersectionTypes.get(2);

		annotations = ((SimpleType) castType).annotations();
		assertEquals("Incorrect no of annotations", 0, annotations.size());
		assertEquals("Incorrect receiver", "J", castType.toString());
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=406505
	 * tests the source range issue that resulted in bad ast node.
	 */
	public void testBug406505() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter18/src/test406505/X.java",
				true/* resolve */);
		String contents = """
			package test406505;\
			import java.lang.annotation.Target;
			import java.io.File;
			public class X {
				class Folder<@Marker  F extends File> { }
			}
			@Target (java.lang.annotation.ElementType.TYPE_USE)
			@interface Marker {}
			""";
		CompilationUnit cu = (CompilationUnit) buildAST(contents, this.workingCopy);
		TypeDeclaration typedeclaration = (TypeDeclaration) getASTNode(cu, 0);
		typedeclaration = (TypeDeclaration)typedeclaration.bodyDeclarations().get(0);
		TypeParameter typeParameter = (TypeParameter) typedeclaration.typeParameters().get(0);
		checkSourceRange(typeParameter, "@Marker  F extends File", contents);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=412726
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428526
	public void testBug412726() throws JavaModelException {
		String contents =
			"""
			public interface X {
				abstract void foo();
			}
			interface Y1 {
			}
			interface Y2 {
				default void foo() {}
			}
			interface Z1 {
				default void foo(){}
				abstract void bar();
			}
			interface Z2 {
				default void foo(){}
				abstract void bar1();
				abstract void bar2();
			}
			""";
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy, true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		/* case 0: vanilla case - interface with one abstract method */
		TypeDeclaration type =  (TypeDeclaration) unit.types().get(0);
		ITypeBinding typeBinding = type.resolveBinding();
		assertEquals("Incorrect method", typeBinding.getDeclaredMethods()[0], typeBinding.getFunctionalInterfaceMethod());
		/* case 1: interface without any method */
		type =  (TypeDeclaration) unit.types().get(1);
		typeBinding = type.resolveBinding();
		assertNull(typeBinding.getFunctionalInterfaceMethod());
		/* case 2: interface with just one default method and without any abstract method */
		type =  (TypeDeclaration) unit.types().get(2);
		typeBinding = type.resolveBinding();
		assertNull(typeBinding.getFunctionalInterfaceMethod());
		/* case 3: interface with just one default method and one abstract method */
		type =  (TypeDeclaration) unit.types().get(3);
		typeBinding = type.resolveBinding();
		IMethodBinding functionalInterfaceMethod = typeBinding.getFunctionalInterfaceMethod();
		assertNotNull(functionalInterfaceMethod);
		assertEquals("Incorrect method", "public abstract void bar() ", functionalInterfaceMethod.toString());
		assertEquals(typeBinding, functionalInterfaceMethod.getDeclaringClass());
		/* case 4: interface with just one default method and two abstract methods */
		type =  (TypeDeclaration) unit.types().get(4);
		typeBinding = type.resolveBinding();
		assertNull(typeBinding.getFunctionalInterfaceMethod());
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=417017
	 */
	public void test417017a() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter18/src/test417017/X.java",
				true/* resolve */);
		String contents = """
			package test417017;\
			interface I {
				int foo(int x);
			}
			public class X {
			 void fun(int a) {
			  	I i1 = x1-> x1;
			  	I i2 = xxx-> {
			  		i1.foo(a);
			  		return xxx;
			  	};
			  }
			}
			""";
		CompilationUnit cu = (CompilationUnit) buildAST(contents, this.workingCopy);
		TypeDeclaration typedeclaration = (TypeDeclaration) getASTNode(cu, 1);
		MethodDeclaration methodDeclaration = typedeclaration.getMethods()[0];
		VariableDeclarationFragment vdf= (VariableDeclarationFragment) ((VariableDeclarationStatement) methodDeclaration.getBody().statements().get(1)).fragments().get(0);
		LambdaExpression lambda= (LambdaExpression) vdf.getInitializer();
		List parameters = lambda.parameters();
		assertTrue("Incorrect Number of parameters", parameters.size() == 1);
		ITypeBinding[] parameterTypes= lambda.resolveMethodBinding().getParameterTypes();
		assertTrue("Incorrect Number of parameter type", parameterTypes.length == 1);
		assertEquals("Incorrect parameter type", "int", parameterTypes[0].toString());
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=417017
	 */
	public void test417017b() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter18/src/test417017/X.java",
				true/* resolve */);
		String contents = """
			package test417017;\
			interface I1 {
				int foo(int a);
			}
			
			interface I2 {
				public default int foo() {
					I1 i1 = (a) -> {
						return a;
					};
					//return 0;
				}
			}
			""" ;
		CompilationUnit cu = (CompilationUnit) buildAST(contents, this.workingCopy, false);
		TypeDeclaration typedeclaration = (TypeDeclaration) getASTNode(cu, 1);
		MethodDeclaration methodDeclaration = typedeclaration.getMethods()[0];
		VariableDeclarationFragment vdf= (VariableDeclarationFragment) ((VariableDeclarationStatement) methodDeclaration.getBody().statements().get(0)).fragments().get(0);
		LambdaExpression lambda= (LambdaExpression) vdf.getInitializer();
		List parameters = lambda.parameters();
		assertTrue("Incorrect Number of parameters", parameters.size() == 1);
		ITypeBinding[] parameterTypes= lambda.resolveMethodBinding().getParameterTypes();
		assertTrue("Incorrect Number of parameter type", parameterTypes.length == 1);
		assertEquals("Incorrect parameter type", "int", parameterTypes[0].toString());
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=417017
	 */
	public void test417017c() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter18/src/test417017/X.java",
				true/* resolve */);
		String contents = """
			package test417017;\
			interface I1 {
				int foo(int a);
			}
			
			interface I2 {
				public default int foo() {
					I1 i1 = (float a) -> {
						return a;
					};
					//return 0;
				}
			}
			""" ;
		CompilationUnit cu = (CompilationUnit) buildAST(contents, this.workingCopy, false);
		TypeDeclaration typedeclaration = (TypeDeclaration) getASTNode(cu, 1);
		MethodDeclaration methodDeclaration = typedeclaration.getMethods()[0];
		VariableDeclarationFragment vdf= (VariableDeclarationFragment) ((VariableDeclarationStatement) methodDeclaration.getBody().statements().get(0)).fragments().get(0);
		LambdaExpression lambda= (LambdaExpression) vdf.getInitializer();
		List parameters = lambda.parameters();
		assertTrue("Incorrect Number of parameters", parameters.size() == 1);
		ITypeBinding[] parameterTypes= lambda.resolveMethodBinding().getParameterTypes();
		assertTrue("Incorrect Number of parameter type", parameterTypes.length == 1);
		assertEquals("Incorrect parameter type", "float", parameterTypes[0].toString());
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=417017
	 */
	public void test417017d() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter18/src/test399794/X.java",
				true/* resolve */);
		String contents = """
			package test399794;\
			interface I {
				void foo(X x);
			}
			public class X {
				void foo(X x) {
				}
				I i = this::foo;
			}
			""";

		CompilationUnit cu = (CompilationUnit) buildAST(contents, this.workingCopy);
		TypeDeclaration typeDeclaration = (TypeDeclaration) getASTNode(cu, 1);
		FieldDeclaration field = typeDeclaration.getFields()[0];

		VariableDeclarationFragment fragment = (VariableDeclarationFragment) field.fragments().get(0);
		Expression expression = fragment.getInitializer();
		ExpressionMethodReference methodReference = (ExpressionMethodReference) expression;
		IMethodBinding methodBinding = methodReference.resolveMethodBinding();
		assertNotNull(methodBinding);
		ITypeBinding [] parameterTypes = methodBinding.getParameterTypes();
		assertTrue("Incorrect Number of parameter type", parameterTypes.length == 1);
		assertEquals("Incorrect parameter type", "X", parameterTypes[0].getName());
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=417017
	 */
	public void test417017e() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter18/src/test399794/X.java",
				true/* resolve */);
		String contents = """
			package test399794;\
			interface I {
				int [] foo(int x);
			}
			public class X {
				I i = int []::new;
			}
			""";

		CompilationUnit cu = (CompilationUnit) buildAST(contents, this.workingCopy);
		TypeDeclaration typeDeclaration = (TypeDeclaration) getASTNode(cu, 1);
		FieldDeclaration field = typeDeclaration.getFields()[0];

		VariableDeclarationFragment fragment = (VariableDeclarationFragment) field.fragments().get(0);
		Expression expression = fragment.getInitializer();
		CreationReference creationReference = (CreationReference) expression;
		IMethodBinding methodBinding = creationReference.resolveMethodBinding();
		assertNull(methodBinding);
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=417017
	 */
	public void test417017f() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter18/src/test399794/X.java",
				true/* resolve */);
		String contents = """
			package test399794;\
			interface I {
				void foo(X x);
			}
			public class X {
				private void foo(X x) {
				}
				class Y {
					I i = X.this::foo;
				}
			}
			""";

		CompilationUnit cu = (CompilationUnit) buildAST(contents, this.workingCopy);
		TypeDeclaration typeDeclaration = (TypeDeclaration) getASTNode(cu, 1);
		typeDeclaration = typeDeclaration.getTypes()[0];
		FieldDeclaration field = typeDeclaration.getFields()[0];

		VariableDeclarationFragment fragment = (VariableDeclarationFragment) field.fragments().get(0);
		Expression expression = fragment.getInitializer();
		ExpressionMethodReference reference = (ExpressionMethodReference) expression;
		IMethodBinding methodBinding = reference.resolveMethodBinding();
		assertNotNull(methodBinding);
		assertEquals("Wrong name", "foo", methodBinding.getName());
		ITypeBinding [] parameterTypes = methodBinding.getParameterTypes();
		assertTrue("Incorrect Number of parameter type", parameterTypes.length == 1);
		assertEquals("Incorrect parameter type", "X", parameterTypes[0].getName());
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=413942
	// also refer https://bugs.eclipse.org/bugs/show_bug.cgi?id=413569
	public void testBug413942() throws JavaModelException {
		String contents =
		"""
			public class X extends @NonNull(int[].class) Object {
			    Object field = new ArrayList< @NonEmpty(0) int @NonNull(value1 = 1) [] @NonEmpty(1) [ ]>() ;
			    @Annot int @Annot1 [] a1 @Annot2 @Annot3 @NonNull (value = int[].class, value1 = 0)[/* [] */ ] @Annot3 @Annot2 [] @Annot4 [];
			    int[] xxx[];
			    int [][] ii = new int[2][3];\
			    ArrayList<int[]> [][] yyy; // source ranges already broken in AST.JLS4
			    ArrayList<int[][]> [][][][] zzz;
			    ArrayList<Float> [][][] zzz2;
			    Object a = new ArrayList< @TakeType(int[][].class) int @TakeType(float.class) [] @TakeType(double.class) []>() ;
			    Object b = new @NonNull(value1 = Math.PI) ArrayList< >() ;\s
			    Object c = new ArrayList<@NonNull(value1= Math.PI ) Object[]>() ;
			
			    int foo(@TakeType(int[].class)int i ) @TakeType(int[].class) [] {
			        int[] arr =  new int[2];
			        for (String tab @TakeType(int[].class) [] = null;; ++i) { break; }
			        for (@Deprecated String tab@TakeType(int[].class) [][]  = null;; ++i) {}
			    }
			    int bar(int [] /*@TakeType(int[].class)*/ [] a ) {
			    	return 0;
			    }
			public int var1(int @TakeType(int[].class)... args) { return 0;}
			public int var2(int @Annot ... args) { return 0;}
			}
			
			@Target(ElementType.TYPE_USE)
			@Retention(RetentionPolicy.RUNTIME)
			@Documented
			@interface NonNull {
				Class value() default int.class;
				double value1() default 0;
			}
			
			@Target(ElementType.TYPE_USE)
			@Retention(RetentionPolicy.RUNTIME)
			@Documented
			@interface NonEmpty {
				int value() default 0;
			}
			
			@Target(ElementType.TYPE_USE)
			@Retention(RetentionPolicy.RUNTIME)
			@Documented
			@interface TakeType {
				Class value() default int[].class;
			}
			
			@Target(ElementType.TYPE_USE)
			@Retention(RetentionPolicy.RUNTIME)
			@Documented
			@interface Annot {}
			
			@Target(ElementType.TYPE_USE)
			@Retention(RetentionPolicy.RUNTIME)
			@Documented
			@interface Annot1 {}
			
			@Target(ElementType.TYPE_USE)
			@Retention(RetentionPolicy.RUNTIME)
			@Documented
			@interface Annot2 {}
			
			@Target(ElementType.TYPE_USE)
			@Retention(RetentionPolicy.RUNTIME)
			@Documented
			@interface Annot3 {}
			
			@Target(ElementType.TYPE_USE)
			@Retention(RetentionPolicy.RUNTIME)
			@Documented
			@interface Annot4 {}
			
			""";
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy, false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;

		TypeDeclaration type =  (TypeDeclaration) unit.types().get(0);
		SimpleType simpleType =  (SimpleType) type.getSuperclassType();
		checkSourceRange(simpleType, "@NonNull(int[].class) Object", contents);
		SingleMemberAnnotation singleMemberAnnotation = (SingleMemberAnnotation) simpleType.annotations().get(0);
		checkSourceRange(singleMemberAnnotation, "@NonNull(int[].class)", contents);
		TypeLiteral typeLiteral = (TypeLiteral) singleMemberAnnotation.getValue();
		checkSourceRange(typeLiteral, "int[].class", contents);
		ArrayType arrayType = (ArrayType) typeLiteral.getType();
		checkSourceRange(arrayType, "int[]", contents);

		int count = 0;
		FieldDeclaration field = (FieldDeclaration) type.bodyDeclarations().get(count++);
		checkSourceRange(field, "Object field = new ArrayList< @NonEmpty(0) int @NonNull(value1 = 1) [] @NonEmpty(1) [ ]>() ;", contents);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) field.fragments().get(0);
		ClassInstanceCreation instance = (ClassInstanceCreation) fragment.getInitializer();
		ParameterizedType parameterizedType = (ParameterizedType) instance.getType();
		arrayType = (ArrayType) parameterizedType.typeArguments().get(0);
		checkSourceRange(arrayType, "@NonEmpty(0) int @NonNull(value1 = 1) [] @NonEmpty(1) [ ]", contents);
		PrimitiveType primitiveType = (PrimitiveType) arrayType.getElementType();
		checkSourceRange(primitiveType, "@NonEmpty(0) int", contents);
		Dimension dimension = (Dimension) arrayType.dimensions().get(0);
		checkSourceRange(dimension, "@NonNull(value1 = 1) []", contents);
		dimension = (Dimension) arrayType.dimensions().get(1);
		checkSourceRange(dimension, "@NonEmpty(1) [ ]", contents);

		field = (FieldDeclaration) type.bodyDeclarations().get(count++);
		checkSourceRange(field, "@Annot int @Annot1 [] a1 @Annot2 @Annot3 @NonNull (value = int[].class, value1 = 0)[/* [] */ ] @Annot3 @Annot2 [] @Annot4 [];", contents);
		arrayType = (ArrayType) field.getType();
		checkSourceRange(arrayType, "int @Annot1 []", contents);
		fragment = (VariableDeclarationFragment) field.fragments().get(0);
		dimension = (Dimension) fragment.extraDimensions().get(0);
		checkSourceRange(dimension, "@Annot2 @Annot3 @NonNull (value = int[].class, value1 = 0)[/* [] */ ]", contents);
		dimension = (Dimension) fragment.extraDimensions().get(1);
		checkSourceRange(dimension, "@Annot3 @Annot2 []", contents);

		field = (FieldDeclaration) type.bodyDeclarations().get(count++);
		checkSourceRange(field, "int[] xxx[];", contents);
		assertTrue(field.getType().isArrayType());
		arrayType = (ArrayType) field.getType();
		checkSourceRange(arrayType, "int[]", contents);

		field = (FieldDeclaration) type.bodyDeclarations().get(count++);
		checkSourceRange(field, "int [][] ii = new int[2][3];", contents);
		arrayType = (ArrayType) field.getType();
		checkSourceRange(arrayType, "int [][]", contents);
		fragment = (VariableDeclarationFragment) field.fragments().get(0);
		ArrayCreation arrayCreation = (ArrayCreation) fragment.getInitializer();
		arrayType = arrayCreation.getType();
		assertTrue(arrayType.getElementType().isPrimitiveType());
		assertTrue(arrayType.getDimensions() == 2);

		field = (FieldDeclaration) type.bodyDeclarations().get(count++);
		checkSourceRange(field, "ArrayList<int[]> [][] yyy;", contents);
		arrayType = (ArrayType) field.getType();
		checkSourceRange(arrayType, "ArrayList<int[]> [][]", contents);

		field = (FieldDeclaration) type.bodyDeclarations().get(count++);
		checkSourceRange(field, "ArrayList<int[][]> [][][][] zzz;", contents);
		arrayType = (ArrayType) field.getType();
		assertTrue(arrayType.getElementType().isParameterizedType());
		assertTrue(arrayType.getDimensions() == 4);

		field = (FieldDeclaration) type.bodyDeclarations().get(count++);
		checkSourceRange(field, "ArrayList<Float> [][][] zzz2;", contents);
		arrayType = (ArrayType) field.getType();
		assertTrue(arrayType.getElementType().isParameterizedType());
		assertTrue(arrayType.getDimensions() == 3);

		field = (FieldDeclaration) type.bodyDeclarations().get(count++);
		checkSourceRange(field, "Object a = new ArrayList< @TakeType(int[][].class) int @TakeType(float.class) [] @TakeType(double.class) []>() ;", contents);
		fragment = (VariableDeclarationFragment) field.fragments().get(0);
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) fragment.getInitializer();
		parameterizedType = (ParameterizedType) classInstanceCreation.getType();
		arrayType = (ArrayType) parameterizedType.typeArguments().get(0);
		checkSourceRange(arrayType, "@TakeType(int[][].class) int @TakeType(float.class) [] @TakeType(double.class) []", contents);
		checkSourceRange(arrayType.getElementType(), "@TakeType(int[][].class) int", contents);
		assertTrue(arrayType.getElementType().isPrimitiveType());
		dimension = (Dimension) arrayType.dimensions().get(0);
		checkSourceRange(dimension, "@TakeType(float.class) []", contents);
		dimension = (Dimension) arrayType.dimensions().get(1);
		Annotation annotation = (Annotation) dimension.annotations().get(0);
		assertTrue(annotation.isSingleMemberAnnotation());
		singleMemberAnnotation = (SingleMemberAnnotation) annotation;
		typeLiteral = (TypeLiteral) singleMemberAnnotation.getValue();
		checkSourceRange(typeLiteral, "double.class", contents);

		field = (FieldDeclaration) type.bodyDeclarations().get(count++);
		checkSourceRange(field, "Object b = new @NonNull(value1 = Math.PI) ArrayList< >() ;", contents);
		fragment = (VariableDeclarationFragment) field.fragments().get(0);
		classInstanceCreation = (ClassInstanceCreation) fragment.getInitializer();
		parameterizedType = (ParameterizedType) classInstanceCreation.getType();
		checkSourceRange(parameterizedType.getType(), "@NonNull(value1 = Math.PI) ArrayList", contents);

		field = (FieldDeclaration) type.bodyDeclarations().get(count++);
		checkSourceRange(field, "Object c = new ArrayList<@NonNull(value1= Math.PI ) Object[]>() ;", contents);
		fragment = (VariableDeclarationFragment) field.fragments().get(0);
		classInstanceCreation = (ClassInstanceCreation) fragment.getInitializer();
		parameterizedType = (ParameterizedType) classInstanceCreation.getType();
		arrayType = (ArrayType) parameterizedType.typeArguments().get(0);
		assertTrue(arrayType.getDimensions() == 1);

		MethodDeclaration method = (MethodDeclaration) type.bodyDeclarations().get(count++);
		dimension = (Dimension) method.extraDimensions().get(0);
		checkSourceRange(dimension, "@TakeType(int[].class) []", contents);
		singleMemberAnnotation = (SingleMemberAnnotation) dimension.annotations().get(0);
		typeLiteral = (TypeLiteral) singleMemberAnnotation.getValue();
		arrayType = (ArrayType) typeLiteral.getType();
		assertTrue(arrayType.getElementType().isPrimitiveType());
		assertTrue(arrayType.getDimensions() == 1);
		SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) method.parameters().get(0);
		singleMemberAnnotation = (SingleMemberAnnotation) singleVariableDeclaration.modifiers().get(0);
		checkSourceRange(singleMemberAnnotation, "@TakeType(int[].class)", contents);
		typeLiteral = (TypeLiteral) singleMemberAnnotation.getValue();
		arrayType = (ArrayType) typeLiteral.getType();
		assertTrue(arrayType.getElementType().isPrimitiveType());
		assertTrue(arrayType.getDimensions() == 1);
		ForStatement forStatement = (ForStatement) method.getBody().statements().get(1);
		VariableDeclarationExpression variableDeclarationExpression = (VariableDeclarationExpression) forStatement.initializers().get(0);
		fragment = (VariableDeclarationFragment) variableDeclarationExpression.fragments().get(0);
		dimension = (Dimension) fragment.extraDimensions().get(0);
		checkSourceRange(dimension, "@TakeType(int[].class) []", contents);
		forStatement = (ForStatement) method.getBody().statements().get(1);
		variableDeclarationExpression = (VariableDeclarationExpression) forStatement.initializers().get(0);
		fragment = (VariableDeclarationFragment) variableDeclarationExpression.fragments().get(0);
		dimension = (Dimension) fragment.extraDimensions().get(0);
		checkSourceRange(dimension, "@TakeType(int[].class) []", contents);

		method = (MethodDeclaration) type.bodyDeclarations().get(count++);
		singleVariableDeclaration = (SingleVariableDeclaration) method.parameters().get(0);
		// test case active only after bug 417660 is fixed (uncomment)
		checkSourceRange(singleVariableDeclaration, "int [] /*@TakeType(int[].class)*/ [] a", contents);

		method = (MethodDeclaration) type.bodyDeclarations().get(count++);
		singleVariableDeclaration = (SingleVariableDeclaration) method.parameters().get(0);
		checkSourceRange(singleVariableDeclaration, "int @TakeType(int[].class)... args", contents);
		singleMemberAnnotation = (SingleMemberAnnotation) singleVariableDeclaration.varargsAnnotations().get(0);
		typeLiteral = (TypeLiteral) singleMemberAnnotation.getValue();
		arrayType = (ArrayType) typeLiteral.getType();
		assertTrue(arrayType.getElementType().isPrimitiveType());
		assertTrue(arrayType.getDimensions() == 1);

		method = (MethodDeclaration) type.bodyDeclarations().get(count++);
		singleVariableDeclaration = (SingleVariableDeclaration) method.parameters().get(0);
		checkSourceRange(singleVariableDeclaration, "int @Annot ... args", contents);
		assertTrue(singleVariableDeclaration.varargsAnnotations().size() == 1);

	}
	// 	https://bugs.eclipse.org/bugs/show_bug.cgi?id=409586
	public void testBug409586() throws JavaModelException {
		String contents =
				"""
			@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)
			@interface Marker {
			 	String value() default "";
			}
			@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)
			@interface Marker2 {
			 	String value() default "22";
			}
			public class X {
				public @Marker("1") String foo(int @Marker @Marker2 [] args) {
			      return null;
				}
				public @Marker("3") String bar() {
			      return null;
				}
			   public String @Marker("i0") @Marker2 [] [] @Marker("i1") [] str = null;
			   public @Marker String str2 = null;
			   public @Marker String str3 = null;
			   public String str4 = null;
			}""";

		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 2, 0);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION);
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		IMethodBinding mBinding = methodDeclaration.resolveBinding();
		assertNotNull("Should not be null", mBinding);
		ITypeBinding tBinding1 = mBinding.getReturnType();
		assertNotNull("Should not be null", tBinding1);

		/* public @Marker("1") String foo(int @Marker @Marker2 [] args) */
		List params = methodDeclaration.parameters();
		assertEquals("Incorrect params", 1, params.size());
		SingleVariableDeclaration param = (SingleVariableDeclaration) params.get(0);
		ArrayType type = (ArrayType) param.getType();
		ITypeBinding tBinding = type.resolveBinding();
		assertNotNull("Should not be null", tBinding);
		IAnnotationBinding[] annots = tBinding.getTypeAnnotations();
		assertEquals("Incorrect type annotations", 2, annots.length);
		assertEquals("Incorrect annotation", "@Marker()", annots[0].toString());
		assertEquals("Incorrect annotation", "@Marker2()", annots[1].toString());

		/* public @Marker("3") String bar()*/
		node = getASTNode(compilationUnit, 2, 1);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION);
		methodDeclaration = (MethodDeclaration) node;
		mBinding = methodDeclaration.resolveBinding();
		assertNotNull("Should not be null", mBinding);
		ITypeBinding tBinding2 = mBinding.getReturnType();
		assertNotNull("Should not be null", tBinding1);
		assertNotSame("Type bindings should not be same", tBinding1, tBinding2);
		annots = tBinding1.getTypeAnnotations();
		assertEquals("Incorrect type annotations", 1, annots.length);
		IAnnotationBinding annot = annots[0];
		assertEquals("Incorrect annotation", "@Marker(value = 1)", annot.toString());
		annots = tBinding2.getTypeAnnotations();
		assertEquals("Incorrect type annotations", 1, annots.length);
		annot = annots[0];
		assertEquals("Incorrect annotation", "@Marker(value = 3)", annot.toString());

		/* public String @Marker("i0") @Marker2 [] [] @Marker("i1") [] str = null; */
		node = getASTNode(compilationUnit, 2, 2);
		assertTrue("Not a field declaration", node.getNodeType() == ASTNode.FIELD_DECLARATION);
		FieldDeclaration field = (FieldDeclaration) node;
		List fragments = field.fragments();
		assertEquals("Incorrect no of fragments", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		IVariableBinding variable = fragment.resolveBinding();
		assertNotNull("Should not be null", variable);
		ITypeBinding tBinding3 = variable.getType();
		assertNotNull("Should not be null", tBinding3);
		annots = tBinding3.getTypeAnnotations();

		assertEquals("Incorrect type annotations", 2, annots.length);
		assertEquals("Incorrect annotation", "@Marker(value = i0)", annots[0].toString());
		assertEquals("Incorrect annotation", "@Marker2()", annots[1].toString());
		tBinding3 = tBinding3.getComponentType();
		annots = tBinding3.getTypeAnnotations();
		assertEquals("Incorrect type annotations", 0, annots.length);
		tBinding3 = tBinding3.getComponentType();
		annots = tBinding3.getTypeAnnotations();
		assertEquals("Incorrect type annotations", 1, annots.length);
		assertEquals("Incorrect annotation", "@Marker(value = i1)", annots[0].toString());

		/* public @Marker String str2 = null; */
		node = getASTNode(compilationUnit, 2, 3);
		assertTrue("Not a field declaration", node.getNodeType() == ASTNode.FIELD_DECLARATION);
		field = (FieldDeclaration) node;
		fragments = field.fragments();
		assertEquals("Incorrect no of fragments", 1, fragments.size());
		fragment = (VariableDeclarationFragment) fragments.get(0);
		variable = fragment.resolveBinding();
		assertNotNull("Should not be null", variable);
		tBinding1 = variable.getType();

		/* public @Marker String str3 = null; */
		node = getASTNode(compilationUnit, 2, 4);
		assertTrue("Not a field declaration", node.getNodeType() == ASTNode.FIELD_DECLARATION);
		field = (FieldDeclaration) node;
		fragments = field.fragments();
		assertEquals("Incorrect no of fragments", 1, fragments.size());
		fragment = (VariableDeclarationFragment) fragments.get(0);
		variable = fragment.resolveBinding();
		assertNotNull("Should not be null", variable);
		tBinding2 = variable.getType();
		assertSame("Type bindings should be same", tBinding1, tBinding2);
		assertTrue("Unannotated bindings should be same", tBinding1.isEqualTo(tBinding2));

		/* public String str4 = null; */
		node = getASTNode(compilationUnit, 2, 5);
		assertTrue("Not a field declaration", node.getNodeType() == ASTNode.FIELD_DECLARATION);
		field = (FieldDeclaration) node;
		fragments = field.fragments();
		assertEquals("Incorrect no of fragments", 1, fragments.size());
		fragment = (VariableDeclarationFragment) fragments.get(0);
		variable = fragment.resolveBinding();
		assertNotNull("Should not be null", variable);
		tBinding2 = variable.getType();
		assertNotSame("Type bindings should not be same", tBinding1, tBinding2);
		assertTrue("Unannotated bindings should be same", tBinding1.isEqualTo(tBinding2));
	}

	public void testExtendedDimensions() throws JavaModelException {
		String contents =
				"""
			@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)
			@interface Marker {
			 	String value() default "";
			}
			@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)
			@interface Marker2 {
			 	String value() default "22";
			}
			@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)
			@interface Marker3 {
			 	String value() default "22";
			}
			public class X {
				public @Marker("1") String @Marker("2") [] foo(int @Marker @Marker2 [] args @Marker3 []) @Marker3("3") [] {
			      return null;
				}
			   public String @Marker("i0") @Marker2 [] [] @Marker("i1") [] str @Marker("Extended") [] = null;
			}
			""";

		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 3, 0);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION);
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		Type returnType = methodDeclaration.getReturnType2();
		ITypeBinding tBinding1 = returnType.resolveBinding();
		assertEquals("Unexpected type", tBinding1.toString(), "@Marker((String)\"1\") String @Marker((String)\"2\") []");
		assertEquals("Unexpected type", methodDeclaration.resolveBinding().getReturnType().toString(), "@Marker((String)\"1\") String @Marker3((String)\"3\") [] @Marker((String)\"2\") []");

		List params = methodDeclaration.parameters();
		assertEquals("Incorrect params", 1, params.size());
		SingleVariableDeclaration param = (SingleVariableDeclaration) params.get(0);
		ArrayType type = (ArrayType) param.getType();
		ITypeBinding tBinding = type.resolveBinding();
		assertEquals("Unexpected type", tBinding.toString(), "int @Marker @Marker2 []");
		assertEquals("Unexpected type", param.resolveBinding().getType().toString(), "int @Marker3 [] @Marker @Marker2 []");

		// public String @Marker(\"i0\") @Marker2 [] [] @Marker(\"i1\") [] str @Marker(\"Extended\") [] = null;
		node = getASTNode(compilationUnit, 3, 1);
		assertTrue("Not a field declaration", node.getNodeType() == ASTNode.FIELD_DECLARATION);
		FieldDeclaration field = (FieldDeclaration) node;
		List fragments = field.fragments();
		assertEquals("Incorrect no of fragments", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		assertEquals("Unexpected type", fragment.resolveBinding().getType().toString(), "String @Marker((String)\"Extended\") [] @Marker((String)\"i0\") @Marker2 [] [] @Marker((String)\"i1\") []");
		assertEquals("Unexpected type", "String @Marker(\"i0\") @Marker2 [][] @Marker(\"i1\") []", field.getType().toString());
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=417669
	public void testBug417669() throws JavaModelException {
		String contents =
				"""
			@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)
			@interface Marker {}
			public class X {
				public static void main(String [] args) {
			      W<String> w = (@Marker W<String>) null;
				}
			}
			class W<T> {}""";

		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 1, 0);
		assertTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION);
		MethodDeclaration method = (MethodDeclaration) node;
		assertEquals("Method should not be malformed", 0, (method.getFlags() & ASTNode.MALFORMED));

		List statements = method.getBody().statements();
		VariableDeclarationStatement statement = (VariableDeclarationStatement) statements.get(0);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) statement.fragments().get(0);
		CastExpression cast = (CastExpression) fragment.getInitializer();
		ParameterizedType castType = (ParameterizedType) cast.getType();
		Type type = castType.getType();
		checkSourceRange(castType, "@Marker W<String>", contents);
		checkSourceRange(type, "@Marker W", contents);
	}

	// Bug 414113 - [1.8] Method Binding for default method has abstract modifier instead of default
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=414113
	public void testBug414113() throws JavaModelException {
		String contents =
			"""
			public interface X {
				int i = foo();
				default int foo_default() { return 1;}
				static int foo_static() { return 1;}
			}
			""";
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy, false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		TypeDeclaration type =  (TypeDeclaration) unit.types().get(0);
		MethodDeclaration method = (MethodDeclaration) type.bodyDeclarations().get(1);
		IMethodBinding binding =  method.resolveBinding();
		assertTrue("binding is default", (binding.getModifiers() & Modifier.DEFAULT) != 0);
		method = (MethodDeclaration) type.bodyDeclarations().get(2);
		binding =  method.resolveBinding();
		assertTrue("binding is static", (binding.getModifiers() & Modifier.STATIC) != 0);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=420660
	 */
	public void testBug420660() throws JavaModelException {
		String contents =
			"""
			package java.lang;
			public class X {
					public void foo(int p, int q) {
						final int finalVar = 1; // not effectively final!\s
						int effectivelyFinalVar = 2;
						int nonFinalVar = 3;
						nonFinalVar = 4;\s
						q = 0;
						try (FIS fis = new FIS()) {
							if (q == 0) { throw new IOError();	} else { throw new IllegalStateException(); }
						} catch (IOError | IllegalStateException implicitlyFinalExc) {
			    			// implicitlyFinalExc is not effectively final!\t
						} catch (Exception effectivelyFinalExc) {\t
						}
					}
			}
			class IOError extends Exception {private static final long serialVersionUID = 1L;}
			class IllegalStateException extends Exception {private static final long serialVersionUID = 1L;}
			class FIS implements AutoCloseable {
				public void close() throws Exception {}\s
			 }
			interface AutoCloseable {\s
			 void close() throws Exception;\s
			}""";
		this.workingCopy = getWorkingCopy("/Converter18/src/java/lang/X.java", true/*resolve*/);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		MethodDeclaration[] methods = ((TypeDeclaration) node).getMethods();
		assertEquals("Incorrect no of methods", 1, methods.length);
		MethodDeclaration method = methods[0];
		List params = method.parameters();
		assertEquals("Incorrect no of parameters", 2, params.size());
		SingleVariableDeclaration variable = (SingleVariableDeclaration) params.get(0);
		IVariableBinding binding = variable.resolveBinding();
		assertTrue("Should be effectively final", binding.isEffectivelyFinal());
		variable = (SingleVariableDeclaration) params.get(1);
		binding = variable.resolveBinding();
		assertFalse("Should not be effectively final", binding.isEffectivelyFinal());

		List statements = method.getBody().statements();
		VariableDeclarationStatement statement = (VariableDeclarationStatement) statements.get(0);
		List fragments = statement.fragments();
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		binding = fragment.resolveBinding();
		assertFalse("Should not be effectively final", binding.isEffectivelyFinal());
		statement = (VariableDeclarationStatement) statements.get(1);
		fragments = statement.fragments();
		fragment = (VariableDeclarationFragment) fragments.get(0);
		binding = fragment.resolveBinding();
		assertTrue("Should be effectively final", binding.isEffectivelyFinal());

		statement = (VariableDeclarationStatement) statements.get(2);
		fragments = statement.fragments();
		fragment = (VariableDeclarationFragment) fragments.get(0);
		binding = fragment.resolveBinding();
		assertFalse("Should not be effectively final", binding.isEffectivelyFinal());

		TryStatement tryStmt = (TryStatement) statements.get(5);
		List resources = tryStmt.resources();
		VariableDeclarationExpression resourceExp = (VariableDeclarationExpression) resources.get(0);
		fragment = (VariableDeclarationFragment) resourceExp.fragments().get(0);
		binding = fragment.resolveBinding();
		assertFalse("Should not be effectively final", binding.isEffectivelyFinal());

		List catches = tryStmt.catchClauses();
		CatchClause catchCl = (CatchClause) catches.get(1);
		variable = catchCl.getException();
		binding = variable.resolveBinding();
		assertTrue("Should be effectively final", binding.isEffectivelyFinal());
		catchCl = (CatchClause) catches.get(0);
		variable = catchCl.getException();
		binding = variable.resolveBinding();
		assertFalse("Should not be effectively final", binding.isEffectivelyFinal());
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=420660
	 */
	public void testBug420660a() throws JavaModelException {
		String contents =
			"""
			interface I {
			void foo();
			}
			interface J {}
			public class X {
					void foo(int [] p) {
						for (int is : p) {
							I j = new I () {
								public void foo() {
									System.out.println(is);
								}
							};
						}
					}
			}
			""";
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 2);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		MethodDeclaration[] methods = ((TypeDeclaration) node).getMethods();
		assertEquals("Incorrect no of methods", 1, methods.length);
		MethodDeclaration method = methods[0];
		List statements = method.getBody().statements();
		EnhancedForStatement stmt = (EnhancedForStatement) statements.get(0);
		SingleVariableDeclaration variable = stmt.getParameter();
		IVariableBinding binding = variable.resolveBinding();
		assertTrue("Should be effectively final", binding.isEffectivelyFinal());
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=423872
	 */
	public void testBug423872() throws JavaModelException {
		String contents =
			"""
			public interface X {
				void foo(Y.Z<?>... events);
			}
			interface Y<T> {
				public static interface Z<T> {}
			}
			""";
		try {
			this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true/*resolve*/);
			ASTNode node = buildAST(contents, this.workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertProblemsSize(compilationUnit, 0);
			node = getASTNode(compilationUnit, 0);
			assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
			MethodDeclaration[] methods = ((TypeDeclaration) node).getMethods();
			assertEquals("Incorrect no of methods", 1, methods.length);
			MethodDeclaration method = methods[0];
			List parameters = method.parameters();
			assertTrue(parameters.size() == 1);
			SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) parameters.get(0);
			checkSourceRange(singleVariableDeclaration, "Y.Z<?>... events", contents);
			assertTrue(singleVariableDeclaration.isVarargs());
			Type type = singleVariableDeclaration.getType();
			assertTrue(type.isParameterizedType());
		} catch (IllegalArgumentException e) {
			assertTrue(false);
		}
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=424138
	 */
	public void testBug424138_001() throws JavaModelException {
		String contents =
				"""
			package jsr308.myex;
			
			public class X extends @jsr308.myex.X.Anno Object {
			    void foo(@jsr308.myex.X.Anno X this) {}
			    Y<@jsr308.myex.X.Anno Object> l;
			    int o @jsr308.myex.X.Anno[];
			
				 @jsr308.myex.X.Anno X f;
			    int @jsr308.myex.X.Anno[] ok;
			    @jsr308.myex.X.Anno X g;
				 void bar(@jsr308.myex.X.Anno X ok) {
			        @jsr308.myex.X.Anno X l;
			    }
			    @java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE_USE)
			    public @interface Anno {}
			}
			class Y<T> {}
			""";
		this.workingCopy = getWorkingCopy("/Converter18/src/jsr308/myex/X.java", true/*resolve*/);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		SimpleType simpleType = (SimpleType) typeDeclaration.getSuperclassType();
		checkSourceRange(simpleType, "@jsr308.myex.X.Anno Object", contents);
		Annotation annotation = (Annotation) simpleType.annotations().get(0);
		checkSourceRange(annotation, "@jsr308.myex.X.Anno", contents);
		MethodDeclaration[] methods = ((TypeDeclaration) node).getMethods();
		assertEquals("Incorrect no of methods", 2, methods.length);
		MethodDeclaration method = methods[0];
		simpleType = (SimpleType) method.getReceiverType();
		checkSourceRange(simpleType, "@jsr308.myex.X.Anno X", contents);
		FieldDeclaration [] fields = typeDeclaration.getFields();
		FieldDeclaration f = fields[0];
		checkSourceRange(f, "Y<@jsr308.myex.X.Anno Object> l;", contents);
		Type type = f.getType();
		assertTrue(type.isParameterizedType());
		ParameterizedType parameterizedType = (ParameterizedType) type;
		checkSourceRange((ASTNode) parameterizedType.typeArguments().get(0), "@jsr308.myex.X.Anno Object", contents);
		f = fields[1];
		checkSourceRange(f, "int o @jsr308.myex.X.Anno[];", contents);
		assertTrue(f.getType().isPrimitiveType());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) f.fragments().get(0);
		checkSourceRange((ASTNode) fragment.extraDimensions().get(0), "@jsr308.myex.X.Anno[]", contents);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=424138
	 */
	public void testBug424138_002() throws JavaModelException {
		String contents =
				"""
			package jsr308.myex;
			
			public class X{
			    int o2[];
			    int o1 @jsr308.myex.X.Anno[];
			    int @jsr308.myex.X.Anno[][] o3 @jsr308.myex.X.Anno[][];
			    @java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE_USE)
			    public @interface Anno {}
			}
			""";
		this.workingCopy = getWorkingCopy("/Converter18/src/jsr308/myex/X.java", true/*resolve*/);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		FieldDeclaration [] fields = typeDeclaration.getFields();
		FieldDeclaration f = fields[0];
		checkSourceRange(f, "int o2[];", contents);
		f = fields[1];
		checkSourceRange(f, "int o1 @jsr308.myex.X.Anno[];", contents);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=424138
	 */
	public void testBug424138_003() throws JavaModelException {
		String contents =
				"""
			package jsr308.myex;
			
			public class X{
				public void foo() {
					for (int i @jsr308.myex.X.Anno[]: new int[10][12]) {
						System.out.println("hello");
					}
				}
			   @java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE_USE)
			   public @interface Anno {}
			}
			""";
		this.workingCopy = getWorkingCopy("/Converter18/src/jsr308/myex/X.java", true/*resolve*/);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration typeDeclaration = (TypeDeclaration) node;
		MethodDeclaration [] methods = typeDeclaration.getMethods();
		EnhancedForStatement statement = (EnhancedForStatement) methods[0].getBody().statements().get(0);
		SingleVariableDeclaration variable = statement.getParameter();
		checkSourceRange(variable, "int i @jsr308.myex.X.Anno[]", contents);
		Dimension dim = (Dimension) variable.extraDimensions().get(0);
		checkSourceRange(dim, "@jsr308.myex.X.Anno[]", contents);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=423584, [1.8][dom ast] NPE in LambdaExpression#getMethodBinding() for lambda with unresolved type
	 */
	public void test423584() throws JavaModelException {
		String contents =
				"""
			interface I { }
			public class X {
			    static void goo(I i) {
			        System.out.println("goo(I)");
			    }
			    public static void main(String[] args) {
			        goo(s -> 0);
			    }
			}
			""";
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(contents, this.workingCopy, false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 2, "The method goo(I) in the type X is not applicable for the arguments ((<no type> s) -> {})\n" +
												"The target type of this expression must be a functional interface");
		node = getASTNode(compilationUnit, 1);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		MethodDeclaration[] methods = ((TypeDeclaration) node).getMethods();
		assertEquals("Incorrect no of methods", 2, methods.length);
		MethodDeclaration method = methods[1];
		List statements = method.getBody().statements();
		LambdaExpression lambda = (LambdaExpression) ((MethodInvocation) ((ExpressionStatement) statements.get(0)).getExpression()).arguments().get(0);
		IMethodBinding resolveMethodBinding = lambda.resolveMethodBinding();
		assertTrue("Should be null", resolveMethodBinding == null); // no NPE, just a null method binding.
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=418979
	 */
	public void testBug418979_001() throws JavaModelException {
		String contents =
				"""
			import java.lang.annotation.*;
			public class X {
			    void foo(Y.@A Z<?> events) {}
			    void foo(Y.@A ZZ events) {}
			 }
			class Y {
				class Z<T> {}
			   class ZZ{}
			}
			@Target (ElementType.TYPE_USE)
			@interface A{}""";
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(contents, this.workingCopy, false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		MethodDeclaration[] methods = ((TypeDeclaration) node).getMethods();
		assertEquals("Incorrect no of methods", 2, methods.length);
		MethodDeclaration method = methods[0];
		List params = method.parameters();
		assertEquals("Incorrect no of parameters", 1, params.size());
		SingleVariableDeclaration variable = (SingleVariableDeclaration) params.get(0);
		Type type = variable.getType();
		assertTrue(type.isParameterizedType());
		ParameterizedType parameterizedType = (ParameterizedType) type;
		checkSourceRange(parameterizedType, "Y.@A Z<?>", contents);
		type = parameterizedType.getType();
		assertTrue(type.isNameQualifiedType());
		NameQualifiedType nameQualifiedType = (NameQualifiedType) type;
		checkSourceRange(nameQualifiedType, "Y.@A Z", contents);

		method = methods[1];
		params = method.parameters();
		assertEquals("Incorrect no of parameters", 1, params.size());
		variable = (SingleVariableDeclaration) params.get(0);
		type = variable.getType();
		assertTrue(type.isNameQualifiedType());
		nameQualifiedType = (NameQualifiedType) type;
		checkSourceRange(nameQualifiedType, "Y.@A ZZ", contents);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=418979
	 */
	public void testBug418979_002() throws JavaModelException {
		String contents =
				"""
			package test;
			import java.lang.annotation.*;
			public class X {
			    test.@A Outer<C>.@A Inner<C> i;
			 }
			class Outer<T> {
				class Inner<S> {}
			}
			class C {}
			@Target (ElementType.TYPE_USE)
			@interface A{}""";
		this.workingCopy = getWorkingCopy("/Converter18/src/test/X.java", true/*resolve*/);
		ASTNode node = buildAST(contents, this.workingCopy, false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		node = getASTNode(compilationUnit, 0);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		FieldDeclaration field = ((TypeDeclaration) node).getFields()[0];
		checkSourceRange(field, "test.@A Outer<C>.@A Inner<C> i;", contents);
		ParameterizedType parameterizedType = (ParameterizedType) field.getType();
		checkSourceRange(parameterizedType, "test.@A Outer<C>.@A Inner<C>", contents);
		QualifiedType qualifiedType = (QualifiedType) parameterizedType.getType();
		checkSourceRange(qualifiedType, "test.@A Outer<C>.@A Inner", contents);
		parameterizedType = (ParameterizedType) qualifiedType.getQualifier();
		checkSourceRange(parameterizedType, "test.@A Outer<C>", contents);
		NameQualifiedType nameQualifiedType = (NameQualifiedType) parameterizedType.getType();
		checkSourceRange(nameQualifiedType, "test.@A Outer", contents);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=418979
	 */
	public void testBug418979_003() throws JavaModelException {
		String contents =
				"""
			package test;
			import java.lang.annotation.*;
			public class X {
			    public void foo() {
			        new java.util.@A HashMap<>();
			    }
			 }
			@Target (ElementType.TYPE_USE)
			@interface A{}""";
		this.workingCopy = getWorkingCopy("/Converter18/src/test/X.java", true/*resolve*/);
		ASTNode node = buildAST(contents, this.workingCopy, false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		node = getASTNode(compilationUnit, 0);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		MethodDeclaration method = ((TypeDeclaration) node).getMethods()[0];
		ExpressionStatement statement = (ExpressionStatement) method.getBody().statements().get(0);
		ClassInstanceCreation instance = (ClassInstanceCreation) statement.getExpression();
		ParameterizedType parameterizedType = (ParameterizedType) instance.getType();
		checkSourceRange(parameterizedType, "java.util.@A HashMap<>", contents);
		NameQualifiedType nameQualifiedType = (NameQualifiedType) parameterizedType.getType();
		checkSourceRange(nameQualifiedType, "java.util.@A HashMap", contents);
		checkSourceRange(nameQualifiedType.getQualifier(), "java.util", contents);
		checkSourceRange(nameQualifiedType.getName(), "HashMap", contents);
		checkSourceRange((ASTNode) nameQualifiedType.annotations().get(0), "@A", contents);
	}
	/*
	 * [1.8][dom ast] variable binding for LambdaExpression parameter has non-unique key (https://bugs.eclipse.org/bugs/show_bug.cgi?id=416559)
	 */
	public void test416559() throws JavaModelException {
		String contents =
				"""
			interface I {
				int f (int x);
			}
			
			class X {
				I i1 = (x) -> 1;
			   I i2 = (x) -> 1;
			}
			""" ;
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(contents, this.workingCopy, false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 1);
		FieldDeclaration[] field = ((TypeDeclaration) node).getFields();

		List fragments = field[0].fragments();
		VariableDeclarationFragment fragment = (VariableDeclarationFragment)fragments.get(0);
		Expression expression = fragment.getInitializer();
		LambdaExpression lambdaExpression = (LambdaExpression)expression;
		VariableDeclaration variableDeclaration = (VariableDeclaration) lambdaExpression.parameters().get(0);
		IVariableBinding variableBinding = variableDeclaration.resolveBinding();
		IMethodBinding methodBinding = lambdaExpression.resolveMethodBinding();
		String methodKey = ((IBinding) methodBinding).getKey();
		String variableKey = ((IBinding) variableBinding).getKey();
		assertTrue(variableKey.regionMatches(0, methodKey, 0, methodKey.length()));

		fragments = field[1].fragments();
		fragment = (VariableDeclarationFragment)fragments.get(0);
		expression = fragment.getInitializer();
		lambdaExpression = (LambdaExpression)expression;
		variableDeclaration = (VariableDeclaration) lambdaExpression.parameters().get(0);
		variableBinding = variableDeclaration.resolveBinding();

		assertNotSame(variableKey.intern(), ((IBinding) variableBinding).getKey().intern());
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=420458
	 */
	public void testBug420458() throws JavaModelException {
		String contents =
				"""
			/**
			 * Hello
			 * @see #foo(Object[][][])
			 **/
			public class X {}
			""";
		this.workingCopy = getWorkingCopy("/Converter18/src/test/X.java", true/*resolve*/);
		ASTNode node = buildAST(contents, this.workingCopy, false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		node = getASTNode(compilationUnit, 0);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		Javadoc javaDoc = ((TypeDeclaration) node).getJavadoc();
		TagElement tagElement = (TagElement) javaDoc.tags().get(1);
		MethodRef methodRef = (MethodRef) tagElement.fragments().get(0);
		MethodRefParameter parameter = (MethodRefParameter) methodRef.parameters().get(0);
		ArrayType arrayType = (ArrayType) parameter.getType();
		checkSourceRange(arrayType, "Object[][][]", contents);
		checkSourceRange(arrayType.getElementType(), "Object", contents);
		assertTrue(arrayType.getDimensions() == 3);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=420458
	 */
	public void testBug425741() throws JavaModelException {
		String contents =
				"""
			@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_USE)
			@interface Annot { String value(); }
			@Annot("decl") public class X {
				@Annot("field") X x = null;
				public void foo(@Annot("param") X i) {
				}
			}""";
		this.workingCopy = getWorkingCopy("/Converter18/src/test/X.java", true/*resolve*/);
		ASTNode node = buildAST(contents, this.workingCopy, false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		node = getASTNode(compilationUnit, 1);
		assertEquals("Not a type declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		FieldDeclaration field = ((TypeDeclaration) node).getFields()[0];
		List fragments = field.fragments();
		ITypeBinding typeBinding = field.getType().resolveBinding();
		IAnnotationBinding[] annots = typeBinding.getTypeAnnotations();
		assertEquals("Incorrect type annotations", 1, annots.length);
		assertEquals("Incorrect annotation", "@Annot(value = field)", annots[0].toString());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		typeBinding = typeBinding.getTypeDeclaration();
		annots = typeBinding.getTypeAnnotations();
		assertEquals("Incorrect type annotations", 0, annots.length);
		typeBinding = fragment.resolveBinding().getType().getTypeDeclaration();
		annots = typeBinding.getTypeAnnotations();
		assertEquals("Incorrect type annotations", 0, annots.length);
		MethodDeclaration method = ((TypeDeclaration) node).getMethods()[0];
		SingleVariableDeclaration param = (SingleVariableDeclaration) method.parameters().get(0);
		typeBinding = param.getType().resolveBinding();
		annots = typeBinding.getTypeAnnotations();
		assertEquals("Incorrect type annotations", 1, annots.length);
		assertEquals("Incorrect annotation", "@Annot(value = param)", annots[0].toString());
		typeBinding = typeBinding.getTypeDeclaration();
		annots = typeBinding.getTypeAnnotations();
		assertEquals("Incorrect type annotations", 0, annots.length);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=416560, [1.8] Incorrect source range for lambda expression's parameter after reconciliation
	 */
	public void testBug416560_001() throws JavaModelException {
		String contents =
				"""
			import java.lang.annotation.ElementType;
			@Target (ElementType.FIELD)
			public class X{
			    public FI fi= /*a*/(int n1, int n2) -> n1 * n2;
			}
			public class X
			    public FI fi= /*a*/(int n1, int n2) -> n1 * n2;
			interface FI {
			    int foo(int s1, int s2);
			}
			""";
		this.workingCopy = getWorkingCopy("/Converter18/src/test/X.java", true/*resolve*/);
		this.workingCopy.getBuffer().setContents(contents);
		CompilationUnit compilationUnit = this.workingCopy.reconcile(getAST8(), ICompilationUnit.FORCE_PROBLEM_DETECTION, null, null);
		ASTNode node = getASTNode(compilationUnit, 0);
		FieldDeclaration[] field = ((TypeDeclaration) node).getFields();
		List fragments = field[0].fragments();
		VariableDeclarationFragment fragment = (VariableDeclarationFragment)fragments.get(0);
		Expression expression = fragment.getInitializer();
		LambdaExpression lambdaExpression = (LambdaExpression)expression;
		VariableDeclaration variableDeclaration = (VariableDeclaration) lambdaExpression.parameters().get(0);
		checkSourceRange(variableDeclaration, "int n1", contents);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=416560, [1.8] Incorrect source range for lambda expression's parameter after reconciliation
	 */
	public void testBug416560_002() throws JavaModelException {
		String contents =
				"""
			import java.lang.annotation.ElementType;
			@Target (ElementType.FIELD)
			public class X{
			    public FI fi= /*a*/(int n1, int n2) -> n1 * n2;
			}
			public class X
			    public FI fi= /*a*/(int n1, int n2) -> n1 * n2;
			interface FI {
			    int foo(int s1, int s2);
			}
			""";
		this.workingCopy = getWorkingCopy("/Converter18/src/test/X.java", true/*resolve*/);
		ASTNode node = buildAST(contents, this.workingCopy, false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		node = getASTNode(compilationUnit, 0);
		FieldDeclaration[] field = ((TypeDeclaration) node).getFields();
		List fragments = field[0].fragments();
		VariableDeclarationFragment fragment = (VariableDeclarationFragment)fragments.get(0);
		Expression expression = fragment.getInitializer();
		LambdaExpression lambdaExpression = (LambdaExpression)expression;
		VariableDeclaration variableDeclaration = (VariableDeclaration) lambdaExpression.parameters().get(0);
		checkSourceRange(variableDeclaration, "int n1", contents);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=416560, [1.8] Incorrect source range for lambda expression's parameter after reconciliation
	 */
	public void testBug416560_003() throws JavaModelException {
		String contents =
				"""
			public class X{
			    void f() {
				    //a
				    //few
				    //comments
				    //here
			       bar((int x) -> 91);
				 }
			    int bar(FI f){ return 1;}\
			}
			interface FI {
			    int foo(int s1);
			}
			""";
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(contents, this.workingCopy, true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		node = getASTNode(compilationUnit, 0);
		MethodDeclaration[] methods = ((TypeDeclaration) node).getMethods();
		MethodDeclaration method = methods[0];
		Block block = method.getBody();
		List statements = block.statements();
		Statement statement = (Statement) statements.get(0);
		MethodInvocation methodInvocation = (MethodInvocation) ((ExpressionStatement) statement).getExpression();
		LambdaExpression lambdaExpression = (LambdaExpression) methodInvocation.arguments().get(0);
		VariableDeclaration variableDeclaration = (VariableDeclaration) lambdaExpression.parameters().get(0);
		checkSourceRange(variableDeclaration, "int x", contents);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=425743, [1.8][api] CompilationUnit#findDeclaringNode(IBinding binding) returns null for type inferred lambda parameter
	 */
	public void testBug425743() throws JavaModelException {
		String contents =
				"""
			public class X{
			    FI fi = (x2) -> x2;
			}
			interface FI {
			    int foo(int n);
			}
			""";
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(contents, this.workingCopy, true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		node = getASTNode(compilationUnit, 0);
		FieldDeclaration fi = ((TypeDeclaration) node).getFields()[0];
		VariableDeclarationFragment vdf = (VariableDeclarationFragment) fi.fragments().get(0);
		LambdaExpression lambda = (LambdaExpression) vdf.getInitializer();
		VariableDeclaration param = (VariableDeclaration) lambda.parameters().get(0);
		IBinding binding = param.getName().resolveBinding();
		ASTNode astNode = compilationUnit.findDeclaringNode(binding);
		assertNotNull(astNode);
		assertEquals("Not a variable declaration fragment", ASTNode.VARIABLE_DECLARATION_FRAGMENT, astNode.getNodeType());
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427357
	public void testBug427357() throws JavaModelException {
		String contents =
			"""
			public class X {
				public static void foo(X this, int i){}
				public void foo(Inner this){}
				I I = new I() {
					public void bar(I this, int i) {}
				};
				static class Inner {
					public Inner(Test2 Test2.this){}
					public Inner(Inner Inner.this, int i){}
				}
			}
			interface I {}""";
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy, false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		TypeDeclaration typeDecl = (TypeDeclaration) unit.types().get(0);
		MethodDeclaration method = (MethodDeclaration) typeDecl.bodyDeclarations().get(0);
		Type receiver = method.getReceiverType();
		assertNotNull("Receiver should not be null", receiver);
		assertEquals("Incorrect receiver type", "X", receiver.toString());

		method = (MethodDeclaration) typeDecl.bodyDeclarations().get(1);
		receiver = method.getReceiverType();
		assertNotNull("Receiver should not be null", receiver);

		FieldDeclaration field = (FieldDeclaration) typeDecl.bodyDeclarations().get(2);
		ClassInstanceCreation anonymousInst =  (ClassInstanceCreation ) ((VariableDeclarationFragment) field.fragments().get(0)).getInitializer();
		AnonymousClassDeclaration anonymousDecl = anonymousInst.getAnonymousClassDeclaration();
		method = (MethodDeclaration) anonymousDecl.bodyDeclarations().get(0);
		receiver = method.getReceiverType();
		assertNotNull("Receiver should not be null", receiver);
		assertEquals("Incorrect receiver type", "I", receiver.toString());
		typeDecl = (TypeDeclaration) typeDecl.bodyDeclarations().get(3);
		method = (MethodDeclaration) typeDecl.bodyDeclarations().get(0);
		receiver = method.getReceiverType();
		assertNotNull("Receiver should not be null", receiver);
		assertEquals("Incorrect receiver type", "Test2", receiver.toString());
		method = (MethodDeclaration) typeDecl.bodyDeclarations().get(1);
		receiver = method.getReceiverType();
		assertNotNull("Receiver should not be null", receiver);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426459
	public void testBug426459() throws JavaModelException {
		String contents =
			"""
			import java.lang.annotation.*;
			@Target(ElementType.TYPE_USE) @interface A {}
			@Target(ElementType.TYPE_USE) @interface B {}
			@Target(ElementType.TYPE_USE) @interface C {}
			public class X {
					@A int @B [] @C [] @A [] is;
					@C String @A [] @B [] @C [] ss;
					@C String @A [] [] [] [] sss;
			}""";
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 3, 0);
		assertTrue("Not a field declaration", node.getNodeType() == ASTNode.FIELD_DECLARATION);
		FieldDeclaration field = (FieldDeclaration) node;
		Type type = field.getType();
		ITypeBinding original = type.resolveBinding();
		assertEquals("Incorrect type binding", "@A int @B [] @C [] @A []", original.toString());
		ITypeBinding binding = original.createArrayType(1);
		assertEquals("Incorrect type binding", "@A int [] @B [] @C [] @A []", binding.toString());
		int dims = binding.getDimensions();
		assertEquals("Incorrect no of dimensions", 4, dims);
		IAnnotationBinding[] annotations = binding.getTypeAnnotations();
		assertEquals("Incorrect no of type annotations", 0, annotations.length);
		binding = binding.getComponentType();
		annotations = binding.getTypeAnnotations();
		assertEquals("Incorrect no of type annotations", 1, annotations.length);
		assertEquals("Incorrect annotation", "@B()", annotations[0].toString());
		binding = binding.getComponentType();
		annotations = binding.getTypeAnnotations();
		assertEquals("Incorrect no of type annotations", 1, annotations.length);
		assertEquals("Incorrect annotation", "@C()", annotations[0].toString());
		binding = binding.getComponentType();
		annotations = binding.getTypeAnnotations();
		assertEquals("Incorrect no of type annotations", 1, annotations.length);
		assertEquals("Incorrect annotation", "@A()", annotations[0].toString());
		binding = binding.getElementType();
		annotations = binding.getTypeAnnotations();
		assertEquals("Incorrect no of type annotations", 1, annotations.length);
		assertEquals("Incorrect annotation", "@A()", annotations[0].toString());

		node = getASTNode(compilationUnit, 3, 1);
		assertTrue("Not a field declaration", node.getNodeType() == ASTNode.FIELD_DECLARATION);
		field = (FieldDeclaration) node;
		type = field.getType();
		original = type.resolveBinding();
		assertEquals("Incorrect type binding", "@C String @A [] @B [] @C []", original.toString());
		binding = original.createArrayType(1);
		assertEquals("Incorrect type binding", "@C String [] @A [] @B [] @C []", binding.toString());
		dims = binding.getDimensions();
		assertEquals("Incorrect no of dimensions", 4, dims);

		binding = original.createArrayType(-1);
		assertEquals("Incorrect type binding", "@C String @B [] @C []", binding.toString());
		dims = binding.getDimensions();
		assertEquals("Incorrect no of dimensions", 2, dims);

		binding = original.createArrayType(-2);
		assertEquals("Incorrect type binding", "@C String @C []", binding.toString());
		dims = binding.getDimensions();
		assertEquals("Incorrect no of dimensions", 1, dims);

		node = getASTNode(compilationUnit, 3, 2);
		assertTrue("Not a field declaration", node.getNodeType() == ASTNode.FIELD_DECLARATION);
		field = (FieldDeclaration) node;
		type = field.getType();
		original = type.resolveBinding();
		assertEquals("Incorrect type binding", "@C String @A [] [] [] []", original.toString());
		binding = original.createArrayType(-1);
		assertEquals("Incorrect type binding", "@C String [] [] []", binding.toString());
		dims = binding.getDimensions();
		assertEquals("Incorrect no of dimensions", 3, dims);
		binding = binding.createArrayType(-2);
		assertEquals("Incorrect type binding", "@C String []", binding.toString());
		dims = binding.getDimensions();
		assertEquals("Incorrect no of dimensions", 1, dims);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426459
	public void testBug426459a() throws JavaModelException {
		String contents =
			"""
			import java.lang.annotation.*;
			@Target(ElementType.TYPE_USE) @interface A {}
			@Target(ElementType.TYPE_USE) @interface B {}
			@Target(ElementType.TYPE_USE) @interface C {}
			public class X {
					public void foo() {}
					public @A X.@B Y foo(@C int i){ return null;}
					public @A @B X foo(int i, int j){ return null;}
					public @A X.Y [] @B [] foo(float f){ return null;}
					class Y {}
			}""";
		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);

		node = getASTNode(compilationUnit, 3, 0);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		Type type = ((MethodDeclaration) node).getReturnType2();
		ITypeBinding binding = type.resolveBinding();
		assertNotNull("Binding should not be null", binding);
		try {
			binding = binding.createArrayType(1);
			binding = null;
		} catch(IllegalArgumentException iae) {
		}
		assertNotNull("IllegalArgumentException should have been thrown", binding);

		node = getASTNode(compilationUnit, 3, 1);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		type = ((MethodDeclaration) node).getReturnType2();
		binding = type.resolveBinding();
		assertEquals("Incorrect type binding", "@A X.@B Y", binding.toString());
		binding = binding.createArrayType(2);
		assertEquals("Incorrect type binding", "@A X.@B Y [] []", binding.toString());
		int dims = binding.getDimensions();
		assertEquals("Incorrect no of dimensions", 2, dims);
		SingleVariableDeclaration param = (SingleVariableDeclaration) ((MethodDeclaration) node).parameters().get(0);
		type = param.getType();
		binding = type.resolveBinding();
		assertEquals("Incorrect type binding", "@C int", binding.toString());
		binding = binding.createArrayType(2);
		assertEquals("Incorrect type binding", "@C int [] []", binding.toString());

		node = getASTNode(compilationUnit, 3, 2);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		type = ((MethodDeclaration) node).getReturnType2();
		binding = type.resolveBinding();
		assertEquals("Incorrect type binding", "@A @B X", binding.toString());
		binding = binding.createArrayType(2);
		assertEquals("Incorrect type binding", "@A @B X [] []", binding.toString());
		dims = binding.getDimensions();
		assertEquals("Incorrect no of dimensions", 2, dims);

		node = getASTNode(compilationUnit, 3, 3);
		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		type = ((MethodDeclaration) node).getReturnType2();
		ITypeBinding original = type.resolveBinding();
		assertEquals("Incorrect type binding", "@A X.Y [] @B []", original.toString());
		binding = original.createArrayType(1);
		assertEquals("Incorrect type binding", "@A X.Y [] [] @B []", binding.toString());
		dims = binding.getDimensions();
		assertEquals("Incorrect no of dimensions", 3, dims);

		binding = original.createArrayType(-1);
		assertEquals("Incorrect type binding", "@A X.Y @B []", binding.toString());
		dims = binding.getDimensions();
		assertEquals("Incorrect no of dimensions", 1, dims);

	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428526, [1.8] API to get the single abstract method in a functional interface
	public void test428526() throws JavaModelException {
		String contents =
				"""
			interface Foo<T, N extends Number> {
			    void m(T arg);
			    void m(N arg);
			}
			interface Baz extends Foo<Integer, Integer> {}
			""";

		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);

		TypeDeclaration type = (TypeDeclaration) getASTNode(compilationUnit, 0);
		assertEquals("Not a Type declaration", ASTNode.TYPE_DECLARATION, type.getNodeType());
		ITypeBinding binding = type.resolveBinding();
		assertNotNull("Binding should not be null", binding);
		IMethodBinding functionalInterfaceMethod = binding.getFunctionalInterfaceMethod();
		assertNull("Should not be a functional interface", functionalInterfaceMethod);

		type = (TypeDeclaration) getASTNode(compilationUnit, 1);
		assertEquals("Not a Type declaration", ASTNode.TYPE_DECLARATION, type.getNodeType());
		binding = type.resolveBinding();
		assertNotNull("Binding should not be null", binding);
		functionalInterfaceMethod = binding.getFunctionalInterfaceMethod();
		assertNotNull("Should be a functional interface", functionalInterfaceMethod);
		ITypeBinding declaringClass = functionalInterfaceMethod.getDeclaringClass();
		assertEquals("Foo<Integer,Integer>", declaringClass.getName());
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428526, [1.8] API to get the single abstract method in a functional interface
	public void test428526a() throws JavaModelException {
		String contents =
				"""
			interface I { X foo(); }
			interface J { Y foo(); }
			interface K extends I, J {}
			class X {}
			class Y extends X {}
			""";

		this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);

		TypeDeclaration type = (TypeDeclaration) getASTNode(compilationUnit, 0);
		assertEquals("Not a Type declaration", ASTNode.TYPE_DECLARATION, type.getNodeType());
		ITypeBinding binding = type.resolveBinding();
		assertNotNull("Binding should not be null", binding);
		IMethodBinding functionalInterfaceMethod = binding.getFunctionalInterfaceMethod();
		assertNotNull("Should not be a functional interface", functionalInterfaceMethod);

		type = (TypeDeclaration) getASTNode(compilationUnit, 1);
		assertEquals("Not a Type declaration", ASTNode.TYPE_DECLARATION, type.getNodeType());
		binding = type.resolveBinding();
		assertNotNull("Binding should not be null", binding);
		functionalInterfaceMethod = binding.getFunctionalInterfaceMethod();
		assertNotNull("Should be a functional interface", functionalInterfaceMethod);

		type = (TypeDeclaration) getASTNode(compilationUnit, 2);
		assertEquals("Not a Type declaration", ASTNode.TYPE_DECLARATION, type.getNodeType());
		binding = type.resolveBinding();
		assertNotNull("Binding should not be null", binding);
		functionalInterfaceMethod = binding.getFunctionalInterfaceMethod();
		assertNotNull("Should be a functional interface", functionalInterfaceMethod);
		ITypeBinding returnType = functionalInterfaceMethod.getReturnType();
		assertEquals("Y", returnType.getName());
	}

// round-trip for binding keys of CaptureBinding18:
public void testBug425183a() throws JavaModelException {
	String contents =
			"""
		interface Comparator<T> {
		    public static <T extends Comparable<? super T>> Comparator<T> naturalOrder() { return null; }
		}
		public class Bug425183a {
		    @SuppressWarnings("unchecked")
			<T> void test() {
				Comparator<? super T> comparator = (Comparator<? super T>) Comparator.naturalOrder();
				System.out.println("OK");
			}
			public static void main(String[] args) {
				new Bug425183a().test();
			}
		}
		""";

	this.workingCopy = getWorkingCopy("/Converter18/src/Bug425183a.java", true);
	ASTNode node = buildAST(contents, this.workingCopy);
	assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
	CompilationUnit compilationUnit = (CompilationUnit) node;
	assertProblemsSize(compilationUnit, 0);

	String selection = "naturalOrder";
	int start = contents.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.workingCopy.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"naturalOrder() {key=LBug425183a~Comparator<>;.naturalOrder<T::Ljava/lang/Comparable<-TT;>;>()LComparator<TT;>;%<^{291#0};>} [in Comparator [in [Working copy] Bug425183a.java [in <default> [in src [in Converter18]]]]]",
		elements,
		true
	);
	IMethod method = (IMethod)elements[0];
	String[] keys = new String[] { method.getKey() };
	BindingRequestor requestor = new BindingRequestor();
	resolveASTs(new ICompilationUnit[] { this.workingCopy } , keys, requestor, getJavaProject("Converter18"), null);
	assertBindingsEqual(
			keys[0],
			requestor.getBindings(keys));

	// assert that KeyToSignature doesn't throw AIOOBE, the result containing '!*' is a workaround for now, see https://bugs.eclipse.org/429264
	assertEquals("wrong signature", "<T::Ljava.lang.Comparable<-TT;>;>()LComparator<!*>;", new BindingKey(method.getKey()).toSignature());
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=432051
 */
public void testBug432051() throws JavaModelException {
	String contents =
			"""
		public class X {
		     * Delete line '/**' above.
		     *
		     * @param a (for example 'QVector')
		     * @param declaringMember the context for resolving (made in)
		     * @return if
		     */
		    void foo() {
		    }
		}
		""";
	this.workingCopy = getWorkingCopy("/Converter18/src/test432051/X.java", contents, true/*computeProblems*/);
	IJavaProject javaProject = this.workingCopy.getJavaProject();
	class BindingRequestor extends ASTRequestor {
		ITypeBinding _result = null;
		public void acceptBinding(String bindingKey, IBinding binding) {
			if (this._result == null && binding != null && binding.getKind() == IBinding.TYPE)
				this._result = (ITypeBinding) binding;
		}
	}
	final BindingRequestor requestor = new BindingRequestor();
	final ASTParser parser = ASTParser.newParser(getAST8());
	parser.setResolveBindings(false);
	parser.setProject(javaProject);
	try {
		parser.createASTs(new ICompilationUnit[] {this.workingCopy}, new String[0], requestor, null);
	} catch (IllegalArgumentException e) {
		assertTrue("Test Failed", false);
	}
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=426977
 */
public void testBug426977() throws JavaModelException {
	String contents =
			"""
		package com.test.todo;\r
		import java.lang.annotation.ElementType;\r
		import java.lang.annotation.Target;\r
		public class Test {\r
			static void m() {}\r
			new Runnable() {\r
				public void run(R) {}\r
			};\r
		}\r
		@Target(ElementType.TYPE_USE)\r
		@interface TU { }""";
	this.workingCopy = getWorkingCopy("/Converter18/src/com/test/todo/Test.java", true/*computeProblems*/);
	try {
		buildAST(getAST8(), contents, this.workingCopy, false, true, true);
	} catch (ClassCastException e) {
		fail(e.getMessage());
	}
}

//Bug 406805 - [1.8] Parameter names for enum constructor not available
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406805
public void test406805() throws CoreException, IOException {
	String contents1 =
			"""
		package test406805;
		public class X {
		}""";
	createFolder("/Converter18/src/test406805");
	createFile("/Converter18/src/test406805/X.java", contents1);
	this.workingCopy = getWorkingCopy(
			"/Converter18/src/test406805/X.java",
			contents1,
			true
		);

	String jarName = "getParameters.jar";
	String srcName = "getParameters_src.zip";
	final IJavaProject javaProject = this.workingCopy.getJavaProject();
	try {
	String[] contents = new String[] {
		"TestEnum.java",
		"""
			package test406805;
			public enum TestEnum {
				FirstValue("Zonk") {
					@Override
					public String toString() {
						return super.toString();
					}
				},
				SecondValue("Bla");
				String string;
				TestEnum(String string) {
					this.string = string;
				}
			}"""
	};
	addLibrary(javaProject, jarName, srcName, contents, JavaCore.VERSION_1_8);

	ASTParser parser = ASTParser.newParser(getAST8());
	parser.setIgnoreMethodBodies(true);
	parser.setProject(javaProject);
	IType type = javaProject.findType("test406805.TestEnum");
	IBinding[] bindings = parser.createBindings(new IJavaElement[] { type }, null);
	ITypeBinding typeBinding = (ITypeBinding) bindings[0];
	IMethodBinding[] methods = typeBinding.getDeclaredMethods();

	for (int i = 0, length = methods.length; i < length; i++) {
		IMethodBinding method = methods[i];
		if (method.isConstructor()) {
			ResolvedBinaryMethod bm = (ResolvedBinaryMethod) method.getJavaElement();
			assertTrue(bm.getParameterNames().length == 1);
			assertEquals(bm.getParameterNames()[0], "string");
			assertEquals(bm.getParameterTypes()[0], "Ljava.lang.String;");
		}
	}
	} finally {
		removeLibrary(javaProject, jarName, srcName);
	}
}

//Bug 406805 - [1.8] Parameter names for enum constructor not available
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=406805
//Nested Enum Constructor
public void test406805a() throws CoreException, IOException {
	String contents1 =
			"""
		package test406805;
		public class X {
		}""";
	createFolder("/Converter18/src/test406805a");
	createFile("/Converter18/src/test406805a/X.java", contents1);
	this.workingCopy = getWorkingCopy(
			"/Converter18/src/test406805a/X.java",
			contents1,
			true
		);

	String jarName = "getParameters.jar";
	String srcName = "getParameters_src.zip";
	final IJavaProject javaProject = this.workingCopy.getJavaProject();
	try {
	String[] contents = new String[] {
		"NestedTestEnum.java",
		"""
			package test406805a;
			public class NestedTestEnum {
				public enum TestEnum {
					FirstValue("Zonk") {
						@Override
						public String toString() {
							return super.toString();
						}
					},
				SecondValue("Bla");
				String string;
				TestEnum(String string) {
					this.string = string;
				}
			}
			}"""
	};
	addLibrary(javaProject, jarName, srcName, contents, JavaCore.VERSION_1_8, null);

	ASTParser parser = ASTParser.newParser(getAST8());
	parser.setIgnoreMethodBodies(true);
	parser.setProject(javaProject);
	IType type = javaProject.findType("test406805a.NestedTestEnum");
	IBinding[] bindings = parser.createBindings(new IJavaElement[] { type }, null);
	ITypeBinding typeBinding = (ITypeBinding) bindings[0];

	typeBinding = typeBinding.getDeclaredTypes()[0];
	IMethodBinding[] methods = typeBinding.getDeclaredMethods();

	for (int i = 0, length = methods.length; i < length; i++) {
		IMethodBinding method = methods[i];
		if (method.isConstructor()) {
			ResolvedBinaryMethod bm = (ResolvedBinaryMethod) method.getJavaElement();
			assertTrue(bm.getParameterNames().length == 1);
		}
	}
	} finally {
		removeLibrary(javaProject, jarName, srcName);
	}
}

//Bug 406805 - [1.8] Parameter names for enum constructor not available
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=406805
//Parameterized enum constructor.
public void test406805b() throws CoreException, IOException {
	String contents1 =
			"""
		package test406805b;
		public class X {
		}""";
	createFolder("/Converter18/src/test406805b");
	createFile("/Converter18/src/test406805b/X.java", contents1);
	this.workingCopy = getWorkingCopy(
			"/Converter18/src/test406805b/X.java",
			contents1,
			true
		);

	String jarName = "getParameters.jar";
	String srcName = "getParameters_src.zip";
	final IJavaProject javaProject = this.workingCopy.getJavaProject();
	try {
	String[] contents = new String[] {
		"TestEnum.java",
		"""
			package test406805b;
			interface A<T> {}
			
			class Y {
				static A<String> A1;
			}
			public enum TestEnum implements A<String> {
			 FirstValue(Y.A1);
			  A<String> xyzabcdef;
			  TestEnum(A<String> abcdefghi) {
				this.xyzabcdef = abcdefghi;
			  }
			 int SecondValue() { return 0;}
			}
			"""
	};
	addLibrary(javaProject, jarName, srcName, contents, JavaCore.VERSION_1_8);

	ASTParser parser = ASTParser.newParser(getAST8());
	parser.setIgnoreMethodBodies(true);
	parser.setProject(javaProject);
	IType type = javaProject.findType("test406805b.TestEnum");
	IBinding[] bindings = parser.createBindings(new IJavaElement[] { type }, null);
	ITypeBinding typeBinding = (ITypeBinding) bindings[0];
	IMethodBinding[] methods = typeBinding.getDeclaredMethods();

	for (int i = 0, length = methods.length; i < length; i++) {
		IMethodBinding method = methods[i];
		if (method.isConstructor()) {
			ResolvedBinaryMethod bm = (ResolvedBinaryMethod) method.getJavaElement();
			assertTrue(bm.getParameterNames().length == 1);
		}
	}
	} finally {
		removeLibrary(javaProject, jarName, srcName);
	}
}
//Bug 406805 - [1.8] Parameter names for enum constructor not available
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=406805
//Testing types and names of parameters.
public void test406805d() throws CoreException, IOException {
	String contents1 =
			"""
		package test406805;
		public class X {
		}""";
	createFolder("/Converter18/src/test406805d");
	createFile("/Converter18/src/test406805d/X.java", contents1);
	this.workingCopy = getWorkingCopy(
			"/Converter18/src/test406805d/X.java",
			contents1,
			true
		);

	String jarName = "getParameters.jar";
	String srcName = "getParameters_src.zip";
	final IJavaProject javaProject = this.workingCopy.getJavaProject();
	try {
	String[] contents = new String[] {
		"NestedTestEnum.java",
		"""
			package test406805d;
			public class NestedTestEnum {
				public enum TestEnum {
					FirstValue("Zonk", 1) {
						@Override
						public String toString() {
							return super.toString();
						}
					},
				SecondValue("Bla", 2);
				String string;
			   int xyz;
				TestEnum(String string, int xyz) {
					this.string = string;
			       this.xyz = xyz;
				}
			}
			}"""
	};
	addLibrary(javaProject, jarName, srcName, contents, JavaCore.VERSION_1_8, null);

	ASTParser parser = ASTParser.newParser(getAST8());
	parser.setIgnoreMethodBodies(true);
	parser.setProject(javaProject);
	IType type = javaProject.findType("test406805d.NestedTestEnum");
	IBinding[] bindings = parser.createBindings(new IJavaElement[] { type }, null);
	ITypeBinding typeBinding = (ITypeBinding) bindings[0];

	typeBinding = typeBinding.getDeclaredTypes()[0];
	IMethodBinding[] methods = typeBinding.getDeclaredMethods();

	for (int i = 0, length = methods.length; i < length; i++) {
		IMethodBinding method = methods[i];
		if (method.isConstructor()) {
			ResolvedBinaryMethod bm = (ResolvedBinaryMethod) method.getJavaElement();
			String[] parameterNames = bm.getParameterNames();
			assertTrue(parameterNames.length == 2);
			assertEquals(parameterNames[0], "string");
			assertEquals(parameterNames[1], "xyz");
			String[] parameterTypes = bm.getParameterTypes();
			assertEquals(parameterTypes[0], "Ljava.lang.String;");
			assertEquals(parameterTypes[1], "I");
		}
	}
	} finally {
		removeLibrary(javaProject, jarName, srcName);
	}
}

//Bug 436347 - Regression: NegativeArraySizeException at org.eclipse.jdt.internal.core.ClassFileInfo.generateMethodInfos
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=436347
//Enum constructor with no parameters.
public void test436347() throws CoreException, IOException {
	String contents1 =
			"""
		package test436347;
		public class X {
		}""";
	createFolder("/Converter18/src/test436347");
	createFile("/Converter18/src/test436347/X.java", contents1);
	this.workingCopy = getWorkingCopy(
			"/Converter18/src/test436347/X.java",
			contents1,
			true
		);

	String jarName = "getParameters.jar";
	String srcName = "getParameters_src.zip";
	final IJavaProject javaProject = this.workingCopy.getJavaProject();
	try {
	String[] contents = new String[] {
		"TestEnum.java",
		"""
			package test436347;
			public enum TestEnum {
				FirstValue() {
					@Override
					public String toString() {
						return super.toString();
					}
				},
				SecondValue();
				TestEnum() {
					return;
				}
			}"""
	};
	addLibrary(javaProject, jarName, srcName, contents, JavaCore.VERSION_1_8);

	ASTParser parser = ASTParser.newParser(getAST8());
	parser.setIgnoreMethodBodies(true);
	parser.setProject(javaProject);
	IType type = javaProject.findType("test436347.TestEnum");
	IBinding[] bindings = parser.createBindings(new IJavaElement[] { type }, null);
	ITypeBinding typeBinding = (ITypeBinding) bindings[0];
	IMethodBinding[] methods = typeBinding.getDeclaredMethods();

	for (int i = 0, length = methods.length; i < length; i++) {
		IMethodBinding method = methods[i];
		if (method.isConstructor()) {
			ResolvedBinaryMethod bm = (ResolvedBinaryMethod) method.getJavaElement();
			assertTrue(bm.getParameterNames().length == 0);
			assertTrue(bm.getParameterTypes().length == 0);
		}
	}
	} finally {
		removeLibrary(javaProject, jarName, srcName);
	}
}


//https://bugs.eclipse.org/bugs/show_bug.cgi?id=433879, org.eclipse.jdt.internal.compiler.lookup.ArrayBinding cannot be cast to org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding
//@throws ClassCastException without the fix
//Array type access in catch block. Test case picked up from the bug.
public void testBug433879() throws JavaModelException {
	String contents =
			"""
		package Bug433879;\r
		public class X {
		Class<? extends Exception>[] exceptions;
			void foo() {
			try {
				// some stuff here
			} catch (exceptions[0] e) {
		   	// some more stuff here
			}
			}
		}
		""";
	this.workingCopy = getWorkingCopy("/Converter18/src/Bug433879/X.java", true/*computeProblems*/);
	try {
		buildAST(getAST8(), contents, this.workingCopy, false, true, true);
	} catch (ClassCastException e) {
		fail(e.getMessage());
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=433879, org.eclipse.jdt.internal.compiler.lookup.ArrayBinding cannot be cast to org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding
//@throws ClassCastException without the fix
//Simplified version of the test case picked up from the bug report.
public void testBug433879a() throws JavaModelException {
	String contents =
			"""
		package Bug433879a;
		public class X {
			void foo() {
				try {
				    // some stuff here
				} catch (A[0] e) {
				    // some more stuff here
				}
			}
		}
		""";
	this.workingCopy = getWorkingCopy("/Converter18/src/Bug433879a/X.java", true/*computeProblems*/);
	try {
		buildAST(getAST8(), contents, this.workingCopy, false, true, true);
	} catch (ClassCastException e) {
		fail(e.getMessage());
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=433879, org.eclipse.jdt.internal.compiler.lookup.ArrayBinding cannot be cast to org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding
//Catch parameters Union Type
public void testBug433879b() throws JavaModelException {
	String contents =
			"""
		package Bug433879c;
		public class X {
			void foo() {
				try {
				    // some stuff here
				} catch (A[0] | B[0] e) {
				    // some more stuff here
				}
			}
		}
		""";
	this.workingCopy = getWorkingCopy("/Converter18/src/Bug433879c/X.java", true/*computeProblems*/);
	try {
		buildAST(getAST8(), contents, this.workingCopy, false, true, true);
	} catch (ClassCastException e) {
		fail(e.getMessage());
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=433879, org.eclipse.jdt.internal.compiler.lookup.ArrayBinding cannot be cast to org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding
//@throws ClassCastException without the fix
//Catch parameters union type. Multiple Catch handlers.
public void testBug433879c() throws JavaModelException {
	String contents =
			"""
		package Bug433879d;
		class E1 extends Exception {private static final long serialVersionUID = 1L;}
		class E2 extends Exception { private static final long serialVersionUID = 1L;}
		
		
		public class X {
			Class<? extends Exception>[] exceptions;
			void foo() {
				try {
					bar();
				} catch (exceptions[0] e) {
				} catch (E1 | E2 eU) {}
			}
			private void bar() throws E1, E2 {}
		}
		""";
	this.workingCopy = getWorkingCopy("/Converter18/src/Bug433879d/X.java", true/*computeProblems*/);
	try {
		buildAST(getAST8(), contents, this.workingCopy, false, true, true);
	} catch (ClassCastException e) {
		fail(e.getMessage());
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=433879, org.eclipse.jdt.internal.compiler.lookup.ArrayBinding cannot be cast to org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding
//@throws ClassCastException without the fix
//Multiple Catch handlers.
public void testBug433879d() throws JavaModelException {
	String contents =
			"""
		package Bug433879e;
		class E1 extends Exception {private static final long serialVersionUID = 1L;}
		class E2 extends Exception { private static final long serialVersionUID = 1L;}
		public class X {
			Class<? extends Exception>[] exceptions;
			Class<? extends Exception>[] exceptions2;
			void foo() {
				try {
					bar();
				} catch (E2 e2) {
				} catch (exceptions[0] e) {
				} catch (E1 e1) {
				} catch (exceptions2[0] e) {
		       }
			}
			private void bar() throws E1, E2 {}
		}
		""";
	this.workingCopy = getWorkingCopy("/Converter18/src/Bug433879e/X.java", true/*computeProblems*/);
	try {
		buildAST(getAST8(), contents, this.workingCopy, false, true, true);
	} catch (ClassCastException e) {
		fail(e.getMessage());
	}
}
public void testBug432175() throws JavaModelException {
	this.workingCopy = getWorkingCopy("/Converter18/src/testBug432175/X.java",
			true/* resolve */);
	String contents = """
		package testBug432175;
		interface Collection <T> {}
		class Collections {
		    public static final <T> T emptyList() {  return null; }
		}
		public class X {
		    public static <T extends Number & Comparable<?>> void method2(Collection<T> coll) {}
		
		    public static void main(String[] args) {
		        method2(Collections.emptyList());
		    }
		}
		""" ;

	CompilationUnit cu = (CompilationUnit) buildAST(contents, this.workingCopy);
	TypeDeclaration typeDeclaration = (TypeDeclaration) getASTNode(cu, 2);
	MethodDeclaration [] methods =  typeDeclaration.getMethods();

	{
		MethodDeclaration method = methods[1];
		ExpressionStatement expressionStatement = (ExpressionStatement) method.getBody().statements().get(0);
		MethodInvocation methodInvocation = (MethodInvocation) expressionStatement.getExpression();
		methodInvocation = (MethodInvocation) methodInvocation.arguments().get(0);
		ITypeBinding typeBinding = methodInvocation.resolveTypeBinding();
		typeBinding = typeBinding.getTypeArguments()[0];
		String res = typeBinding.getTypeDeclaration().getName();
		this.ast.newSimpleType(this.ast.newName(res));
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=435348, [1.8][compiler] NPE in JDT Core during AST creation
// NPE without fix
public void testBug435348() throws JavaModelException {
	this.workingCopy = getWorkingCopy("/Converter18/src/testBug435348/X.java",
		true/* resolve */);
	String contents = """
		package testBug435348;
		class Y {}
		class Z{}
		class X {
		  void bar2(@ Z z) {}
		  //        ^  Illegal @\s
		  static  {
		        bar(new Y() {});
		  }
		}
		""";
	buildAST(contents, this.workingCopy, false);
}

public void testBug432614() throws JavaModelException {
	this.workingCopy = getWorkingCopy("/Converter18/src/testBug432614/X.java", true);
	String contents = """
		package testBug432614;
		import java.lang.annotation.ElementType;
		import java.lang.annotation.Target;
		public class X {
			FI fi1= (@T2 int i) -> {};
		}
		interface FI {
			void foo(@T1 int iii);
		}
		@Target(ElementType.TYPE_USE) @interface T1 {}
		@Target(ElementType.TYPE_USE) @interface T2 {}""";
	CompilationUnit cu = (CompilationUnit) buildAST(contents, this.workingCopy);
	TypeDeclaration typedeclaration = (TypeDeclaration) getASTNode(cu, 0);
	FieldDeclaration fieldDeclaration = (FieldDeclaration) typedeclaration.bodyDeclarations().get(0);
	VariableDeclarationFragment fragment = (VariableDeclarationFragment)fieldDeclaration.fragments().get(0);
	Expression expression = fragment.getInitializer();
	assertTrue(expression instanceof LambdaExpression);
	LambdaExpression lambdaExpression = (LambdaExpression)expression;
	assertEquals("(@T2 int i) -> {\n}\n", lambdaExpression.toString());
	assertTrue(lambdaExpression.parameters().size() == 1);
	IMethodBinding binding = lambdaExpression.resolveMethodBinding();
	ITypeBinding[] params = binding.getParameterTypes();
	assertEquals("Incorrect no of parameters", 1, params.length);
	ITypeBinding thatParam = params[0];
	assertEquals("Incorrect param", "@T2 int", thatParam.toString());
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=447062
 */
public void testBug447062() throws JavaModelException {
	String contents =
			"""
		public class X {
		    Runnable foo = () -> {
		   \s
		    }
		}
		""";
	this.workingCopy = getWorkingCopy("/Converter18/src/test447062/X.java", contents, true/*computeProblems*/);
	IJavaProject javaProject = this.workingCopy.getJavaProject();
	class BindingRequestor extends ASTRequestor {
		ITypeBinding _result = null;
		public void acceptBinding(String bindingKey, IBinding binding) {
			if (this._result == null && binding != null && binding.getKind() == IBinding.TYPE)
				this._result = (ITypeBinding) binding;
		}
	}
	final BindingRequestor requestor = new BindingRequestor();
	final ASTParser parser = ASTParser.newParser(getAST8());
	parser.setResolveBindings(false);
	parser.setProject(javaProject);
	parser.setIgnoreMethodBodies(true);
	try {
		parser.createASTs(new ICompilationUnit[] {this.workingCopy}, new String[0], requestor, null);
	} catch (IllegalArgumentException e) {
		assertTrue("Test Failed", false);
	}
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=399793
 */
public void testBug425601_001() throws JavaModelException {
	this.workingCopy = getWorkingCopy("/Converter18/src/testBug425601_001/Outer.java",
			true/* resolve */);
	String contents = """
		package testBug425601_001;
		@Deprecated
		public class Outer<O> {
		    @Deprecated
		    public class Middle<X> {
		        @Deprecated
		        public class Inner<E> {
		        }
		    }
		   \s
		    Outer<String> o;
		    Middle<String> m; // Middle should be deprecated - Middle Case one
		    Outer<String>.Middle<String> m2; // Middle should be deprecated - Middle Case Two\s
		    @SuppressWarnings("rawtypes")\
		    Outer.Middle m3;\s
		    Middle<String>.Inner<Object> i; // Inner should be deprecated - Inner Case One
		}
		class Ref {
		    Outer<String> o;
		    Outer<String>.Middle<String> m;
		    Outer<String>.Middle<String>.Inner<Object> i;
		}
		""";
	CompilationUnit cu = (CompilationUnit) buildAST(contents, this.workingCopy);
	TypeDeclaration typedeclaration = (TypeDeclaration) getASTNode(cu, 0);
	FieldDeclaration[] fields = typedeclaration.getFields();
	ITypeBinding binding = fields[0].getType().resolveBinding();
	assertTrue(binding.isDeprecated());
	binding = fields[3].getType().resolveBinding();
	assertTrue(binding.isDeprecated());
	binding = fields[1].getType().resolveBinding(); // Middle Case One
	assertTrue(binding.isDeprecated());
	binding = fields[2].getType().resolveBinding(); // Middle Case Two
	assertTrue(binding.isDeprecated());
	binding = fields[4].getType().resolveBinding(); // Inner Case One
	assertTrue(binding.isDeprecated());
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=399793
 */
public void testBug425601_002() throws JavaModelException {
	this.workingCopy = getWorkingCopy("/Converter18/src/testBug425601_002/Outer.java",
			true/* resolve */);
	String contents = """
		package testBug425601_002;
		public class Outer<O> {
		    @Deprecated
		    public class Middle<X> {
		        @Deprecated
		        public class Inner<E> {
		        }
		    }
		   \s
		    Outer<String> o;
		    Middle<String> m; // Middle should be deprecated - Middle Case one
		    Outer<String>.Middle<String> m2; // Middle should be deprecated - Middle Case Two\s
		    @SuppressWarnings("rawtypes")\
		    Outer.Middle m3;\s
		    Middle<String>.Inner<Object> i; // Inner should be deprecated - Inner Case One
		}
		class Ref {
		    Outer<String> o;
		    Outer<String>.Middle<String> m;
		    Outer<String>.Middle<String>.Inner<Object> i;
		}
		""";
	CompilationUnit cu = (CompilationUnit) buildAST(contents, this.workingCopy);
	TypeDeclaration typedeclaration = (TypeDeclaration) getASTNode(cu, 0);
	FieldDeclaration[] fields = typedeclaration.getFields();
	ITypeBinding binding = fields[0].getType().resolveBinding();
	assertTrue(!binding.isDeprecated());
	binding = fields[3].getType().resolveBinding();
	assertTrue(binding.isDeprecated());
	binding = fields[1].getType().resolveBinding(); // Middle Case One
	assertTrue(binding.isDeprecated());
	binding = fields[2].getType().resolveBinding(); // Middle Case Two
	assertTrue(binding.isDeprecated());
	binding = fields[4].getType().resolveBinding(); // Inner Case One
	assertTrue(binding.isDeprecated());
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44000
 *
 * bug Bug 440000 [1.8][dom] MethodReference#resolveMethodBinding() API should return null for CreationReference of an ArrayType
 */
public void testBug440000_001() throws JavaModelException {
	String contents =
			"""
		interface I<T, R> {
		    R apply(T t);
		}
		public class X {
		    I<Integer, int[]> m1 = int[]::new;
		}
		""";
	this.workingCopy = getWorkingCopy("/Converter18/src/X.java", contents, true/*computeProblems*/);
	CompilationUnit cu = (CompilationUnit) buildAST(contents, this.workingCopy);
	TypeDeclaration typedeclaration = (TypeDeclaration) getASTNode(cu, 1);
	FieldDeclaration fieldDeclaration = (FieldDeclaration) typedeclaration.bodyDeclarations().get(0);
	VariableDeclarationFragment fragment = (VariableDeclarationFragment)fieldDeclaration.fragments().get(0);
	Expression expression = fragment.getInitializer();
	assertTrue(expression instanceof CreationReference);
	CreationReference creationReference = (CreationReference) expression;
	assertEquals("int[]::new", creationReference.toString());
	IMethodBinding binding = creationReference.resolveMethodBinding();
	assertNull(binding);
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=459344
 */
public void testBug459344_001() throws JavaModelException {
	this.workingCopy = getWorkingCopy("/Converter18/src/test459344/X.java",
			true/* resolve */);
	String contents = """
		package test459344;\
		interface I {
			int foo();
		}
		public class X {
			private void foo(Object arg) {
				I i = arg :: hashCode;
			}
		}
		""";

	CompilationUnit cu = (CompilationUnit) buildAST(contents, this.workingCopy);
	TypeDeclaration typeDeclaration = (TypeDeclaration) getASTNode(cu, 1);
	MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
	VariableDeclarationStatement stmt = (VariableDeclarationStatement) methodDeclaration.getBody().statements().get(0);
	VariableDeclarationFragment fragment = (VariableDeclarationFragment) stmt.fragments().get(0);
	ExpressionMethodReference reference = (ExpressionMethodReference) fragment.getInitializer();
	IMethodBinding methodBinding = reference.resolveMethodBinding();
	assertNotNull(methodBinding);
	assertEquals("Wrong name", "hashCode", methodBinding.getName());
	assertNull("Non-Null",cu.findDeclaringNode(methodBinding));
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=460186
 * bug Bug 460186 IAE in ASTNode.setSourceRange with broken code case 2
 */
public void testBug460186() throws JavaModelException {
	String contents =
			"""
		class Foo {
			void foo()
		 {\
				foo();[]
			}
		}""";
	this.workingCopy = getWorkingCopy("/Converter18/src/test460186/NPE.java", contents, false/*computeProblems*/);
	IJavaProject javaProject = this.workingCopy.getJavaProject();

	final ASTParser parser = ASTParser.newParser(getAST8());
	parser.setResolveBindings(false);
	parser.setProject(javaProject);
	parser.setIgnoreMethodBodies(false);
	ASTRequestor requestor = new ASTRequestor() {};
	parser.createASTs(new ICompilationUnit[] {this.workingCopy}, new String[0], requestor, null);
	// Implicitly check that no exception is thrown by createASTs.
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=443232
 * bug Bug 443232 IAE in ASTNode.setSourceRange with broken code
 */
public void testBug443232() throws JavaModelException {
	String contents =
			"""
		package test443232;
		public class E21 {
			{private int[] nums;
			void foo() {
		        nums
			}
		}""";
	this.workingCopy = getWorkingCopy("/Converter18/src/test443232/E21.java", contents, false/*computeProblems*/);
	IJavaProject javaProject = this.workingCopy.getJavaProject();

	final ASTParser parser = ASTParser.newParser(getAST8());
	parser.setResolveBindings(false);
	parser.setProject(javaProject);
	parser.setIgnoreMethodBodies(false);
	ASTRequestor requestor = new ASTRequestor() {};
	try {
		parser.createASTs(new ICompilationUnit[] {this.workingCopy}, new String[0], requestor, null);
	} catch (IllegalArgumentException e) {
		assertTrue("Test Failed", false);
	}
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=429813
 */
public void test429813() throws JavaModelException {
	this.workingCopy = getWorkingCopy("/Converter18/src/test429813/Snippet.java",
			true/* resolve */);
	String contents = """
		package test429813;\
		public class Snippet {
				Function<Integer, int[]> m1L = n -> new int[n];
		}\
		interface Function<T, R> {
		   public R apply(T t);
		}
		""";
	CompilationUnit cu = (CompilationUnit) buildAST(contents, this.workingCopy);
	TypeDeclaration typedeclaration = (TypeDeclaration) getASTNode(cu, 0);
	FieldDeclaration fieldDeclaration = (FieldDeclaration) typedeclaration.bodyDeclarations().get(0);
	VariableDeclarationFragment fragment = (VariableDeclarationFragment)fieldDeclaration.fragments().get(0);
	Expression expression = fragment.getInitializer();
	assertTrue(expression instanceof LambdaExpression);
	LambdaExpression lambdaExpression = (LambdaExpression)expression;
	IMethodBinding binding = lambdaExpression.resolveMethodBinding();
	IJavaElement element = binding.getJavaElement();
	assertEquals("Not a method", IJavaElement.METHOD, element.getElementType());
	assertFalse("Should not be a synthetic", binding.isSynthetic());
}

public void test429813a() throws JavaModelException {
	this.workingCopy = getWorkingCopy("/Converter18/src/test429813/Snippet.java",
			true/* resolve */);
	String contents = """
		package test429813;\
		interface FTest {
				Object foo (int[]... ints);
		};\
		class TestX {\
				FTest fi= ints -> null;
		}
		""";
	CompilationUnit cu = (CompilationUnit) buildAST(contents, this.workingCopy);
	TypeDeclaration typedeclaration = (TypeDeclaration) getASTNode(cu, 1);
	FieldDeclaration fieldDeclaration = (FieldDeclaration) typedeclaration.bodyDeclarations().get(0);
	VariableDeclarationFragment fragment = (VariableDeclarationFragment)fieldDeclaration.fragments().get(0);
	Expression expression = fragment.getInitializer();
	assertTrue(expression instanceof LambdaExpression);
	LambdaExpression lambdaExpression = (LambdaExpression)expression;
	IMethodBinding binding = lambdaExpression.resolveMethodBinding();
	assertTrue("Should be a varargs", binding.isVarargs());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=399792
public void testBug470794_001() throws JavaModelException {
	String content =
			"""
		public class X {
		      Object o = (I & J) () -> {};\
		      public K main(Object o) {
		    	  K oo = (I & J & K) o;
		    	  return oo;
		      }
		}
		interface I {
		  public void foo();
		}
		interface J {
		  public void foo();
		  public default void bar() {}
		}
		interface K {
		  public void foo();
		  public void bar();
		}
		""";

	this.workingCopy = getWorkingCopy("/Converter18/src/X.java", true);
	ASTNode node = buildAST(content, this.workingCopy, true);
	assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
	CompilationUnit unit = (CompilationUnit) node;
	TypeDeclaration type =  (TypeDeclaration) unit.types().get(0);
	node = (ASTNode) type.bodyDeclarations().get(0);
	assertEquals("Not a field Declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
	FieldDeclaration field = (FieldDeclaration) node;
	assertEquals("Field should not be malformed", 0, (field.getFlags() & ASTNode.MALFORMED));

	List fragments = field.fragments();
	assertEquals("Incorrect no of fragments", 1, fragments.size());
	VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
	CastExpression cast = (CastExpression) fragment.getInitializer();
	Type castType = cast.getType();
	assertEquals("Not an intersection cast type", ASTNode.INTERSECTION_TYPE, castType.getNodeType());
	assertTrue("Not an intersection cast type", castType.isIntersectionType());
	assertEquals("Type should not be malformed", 0, (castType.getFlags() & ASTNode.MALFORMED));
	ITypeBinding binding = castType.resolveBinding();
	assertNotNull("binding is null", binding);
	assertTrue("Not an intersection type binding", binding.isIntersectionType());
	{
		ITypeBinding [] intersectionBindings = binding.getTypeBounds();
		String[] expectedTypes = new String[]{"I", "J"};
		assertTrue("Incorrect number of intersection bindings", intersectionBindings.length == expectedTypes.length);
		for (int i = 0, l = intersectionBindings.length; i < l; ++i) {
			assertTrue("Unexpected Intersection Type", expectedTypes[i].equals(intersectionBindings[i].getName()));
		}
	}

	node = (ASTNode) type.bodyDeclarations().get(1);
	assertEquals("Not a method Declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
	MethodDeclaration method = (MethodDeclaration) node;
	assertEquals("Method should not be malformed", 0, (method.getFlags() & ASTNode.MALFORMED));

	List statements = method.getBody().statements();
	VariableDeclarationStatement statement = (VariableDeclarationStatement) statements.get(0);
	fragment = (VariableDeclarationFragment) statement.fragments().get(0);
	cast = (CastExpression) fragment.getInitializer();
	castType = cast.getType();
	binding = castType.resolveBinding();
	assertNotNull("binding is null", binding);
	assertTrue("Not an intersection type binding", binding.isIntersectionType());
	{
		ITypeBinding [] intersectionBindings = binding.getTypeBounds();
		String[] expectedTypes = new String[]{"I", "J", "K"};
		assertTrue("Incorrect number of intersection bindings", intersectionBindings.length == expectedTypes.length);
		for (int i = 0, l = intersectionBindings.length; i < l; ++i) {
			assertTrue("Unexpected Intersection Type", expectedTypes[i].equals(intersectionBindings[i].getName()));
		}
	}
}
@SuppressWarnings("deprecation")
public void testBug500503() throws JavaModelException {
	String contents =
			"""
		package test432051;
		public class Colon
		{
		   void foo()
		   {
		   }
		
		   void bar()
		   {
		      run( this:foo );
		   }
		  \s
		   void run( Runnable r ) { }
		}
		""";
	this.workingCopy = getWorkingCopy("/Converter18/src/test432051/Colon.java", contents, true/*computeProblems*/);
	IJavaProject javaProject = this.workingCopy.getJavaProject();
	class BindingRequestor extends ASTRequestor {
		ITypeBinding _result = null;
		public void acceptBinding(String bindingKey, IBinding binding) {
			if (this._result == null && binding != null && binding.getKind() == IBinding.TYPE)
				this._result = (ITypeBinding) binding;
		}
	}
	final BindingRequestor requestor = new BindingRequestor();
	final ASTParser parser = ASTParser.newParser(AST.JLS8);
	parser.setResolveBindings(true);
	parser.setProject(javaProject);
	parser.setBindingsRecovery(true);
	parser.setStatementsRecovery(true);
	parser.createASTs(new ICompilationUnit[] {this.workingCopy}, new String[0], requestor, null);
}

public void testBug497719_0001() throws JavaModelException {
	String contents =
			"""
		import java.io.IOException;
		
		class Z {
			 final Y yz = new Y();
		}
		public class X extends Z {
			final  Y y2 = new Y();
		\t
			 Y bar() {
				 return new Y();
			 }
			public void foo() {
				Y y3 = new Y();
				int a[];
				try (y3; y3;super.yz;super.yz;this.y2;Y y4 = new Y())  { \s
					System.out.println("In Try");
				} catch (IOException e) {			 \s
				}\s
			}
			public static void main(String[] args) {
				new X().foo();
			}
		}
		class Y implements AutoCloseable {
			@Override
			public void close() throws IOException {
				System.out.println("Closed");
			} \s
		}""";
		this.workingCopy = getWorkingCopy("/Converter8/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(contents, this.workingCopy, false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		node = getASTNode((CompilationUnit)node, 1, 2);
		MethodDeclaration methodDeclaration = (MethodDeclaration) node;
		TryStatement tryStatement = (TryStatement)methodDeclaration.getBody().statements().get(2);
		assertEquals("Try Statement should be malformed", ASTNode.MALFORMED, (tryStatement.getFlags() & ASTNode.MALFORMED));
}
public void testBug490698_001() throws JavaModelException {
	String contents =
			"""
		package jsr308.myex;
		
		public class X extends @jsr308.myex.X.Anno Object {
		    public class Inner {}
		    void foo(@jsr308.myex.X.Anno X this) {}
		    int o @jsr308.myex.X.Anno[];
		
			 @jsr308.myex.X.Anno1 X.Inner java;
		    @java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE_USE)
		    public @interface Anno {}
		}
		class Y<T> {}
		""";
	this.workingCopy = getWorkingCopy("/Converter18/src/jsr308/myex/X.java", true/*resolve*/);
	ASTNode node = buildAST(contents, this.workingCopy, false);
	assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
}
public void testBug526449_001() throws JavaModelException {
	String contents =
			"""
		class com.google.android.gms.common.X {
		    class zzc {
		        void getBytes() {
		            this;
		        }
		    }
		
		    class zze {
		        zza zzLC[] = {
		            new zzc(){},\s
		            new zzc(){}
		        };
		    }
		}
		""";
	this.workingCopy = getWorkingCopy("/Converter18/src/jsr308/myex/X.java", true/*resolve*/);
	ASTNode node = buildAST(contents, this.workingCopy, false);
	assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
}
public void testLambdaSynthetic() throws JavaModelException {
	this.workingCopy = getWorkingCopy("/Converter18/src/xyz/X.java",
			true/* resolve */);
	String contents =
			"""
		package xyz;
		
		interface Function<T, R> {
		    R apply(T t);
		 }
		
		public class X {
		
		    private int instanceField = 952;
		
		    static public void main(String[] args) throws Exception {
		        new X().callLambda("hello");
		    }
		
		    void callLambda(String outerArg) throws Exception {
		        double outerLocal = 1.0; // Effectively final
		
		        Function<String, Integer> lambda = lambdaArg -> {
		            int lambdaLocal = 6;
		            System.out.println(instanceField);
		            System.out.println(outerArg);
		            System.out.println(outerLocal);
		            System.out.println(lambdaArg);
		            System.out.println(lambdaLocal);
		            return lambdaArg.length();
		        };
		        int result = lambda.apply("degenerate case");
		        System.out.println(result);
		    }
		}
		""";
	CompilationUnit cu = (CompilationUnit) buildAST(contents, this.workingCopy);
	TypeDeclaration typedeclaration = (TypeDeclaration) getASTNode(cu, 1);
	MethodDeclaration methodDeclaration = (MethodDeclaration) typedeclaration.bodyDeclarations().get(2);
	VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) methodDeclaration.getBody().statements().get(1);
	VariableDeclarationFragment fragment = (VariableDeclarationFragment)variableDeclarationStatement.fragments().get(0);
	LambdaExpression lambdaExpression = (LambdaExpression) fragment.getInitializer();
	IMethodBinding binding = lambdaExpression.resolveMethodBinding();
	IVariableBinding[] synVars = binding.getSyntheticOuterLocals();
	assertEquals(2, synVars.length);
	assertEquals("val$outerArg",synVars[0].getName());
	assertEquals("val$outerLocal",synVars[1].getName());
}
public void testCaptureBinding18() throws CoreException {
	this.workingCopy = getWorkingCopy("/Converter18/src/xyz/X.java", true/* resolve */);
	String content= """
		package xyz;
		
		import java.util.List;
		
		public class X {
		
			protected <E extends Comparable<E>> List<E> createEmptySet() {
				return null;
			}
		
			public void emptySet() {
				s = createEmptySet();
			}
		
		}""";
	CompilationUnit cu = (CompilationUnit) buildAST(content, this.workingCopy, false /*i.e. ignore errors*/);
	MethodDeclaration method= ((TypeDeclaration)cu.types().get(0)).getMethods()[1];
	Assignment assignment= (Assignment) ((ExpressionStatement) method.getBody().statements().get(0)).getExpression();
	ITypeBinding binding = assignment.getRightHandSide().resolveTypeBinding();
	assertTrue("main type is parameterized", binding.isParameterizedType());
	binding = binding.getTypeArguments()[0];
	assertTrue("treat as wildcard", binding.isWildcardType());
	assertFalse("don't treat as capture", binding.isCapture());
	assertTrue("has upper bound", binding.isUpperbound());
	ITypeBinding[] typeBounds = binding.getTypeBounds();
	assertEquals("number of bounds", 1, typeBounds.length);
	ITypeBinding bound = typeBounds[0];
	assertTrue("bound is parameterized", bound.isParameterizedType());
	assertEquals("bound's type argument is the original type argument", binding, bound.getTypeArguments()[0]);
}

}
