/*******************************************************************************
 * Copyright (c) 2006 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    sbandow@bea.com - initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.jdt.apt.tests;

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.apt.tests.annotations.generic.AbstractGenericProcessor;
import org.eclipse.jdt.apt.tests.annotations.generic.GenericFactory;

import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.declaration.TypeParameterDeclaration;
import com.sun.mirror.type.AnnotationType;
import com.sun.mirror.type.ArrayType;
import com.sun.mirror.type.ClassType;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.EnumType;
import com.sun.mirror.type.InterfaceType;
import com.sun.mirror.type.PrimitiveType;
import com.sun.mirror.type.ReferenceType;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.type.TypeVariable;
import com.sun.mirror.type.VoidType;
import com.sun.mirror.type.WildcardType;
import com.sun.mirror.util.TypeVisitor;
import com.sun.mirror.util.Types;

/**
 * Tests for the JDT-APT implementation of Type Visitors
 */
public class TypeVisitorTests extends APTTestBase {

	public TypeVisitorTests(final String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(TypeVisitorTests.class);
	}

	public void testPrimitiveTypeVisitor() {
		testCaseIdentifier = Cases.PrimitiveType;
		runProcessorBasedTest();
	}

	public void testVoidTypeVisitor() {
		testCaseIdentifier = Cases.VoidType;
		runProcessorBasedTest();
	}

	public void testClassTypeVisitor() {
		testCaseIdentifier = Cases.ClassType;
		runProcessorBasedTest();
	}

	public void testEnumTypeVisitor() {
		testCaseIdentifier = Cases.EnumType;
		runProcessorBasedTest();
	}

	public void testInterfaceTypeVisitor() {
		testCaseIdentifier = Cases.InterfaceType;
		runProcessorBasedTest();
	}

	public void testAnnotationTypeVisitor() {
		testCaseIdentifier = Cases.AnnotationType;
		runProcessorBasedTest();
	}

	public void testArrayTypeVisitor() {
		testCaseIdentifier = Cases.ArrayType;
		runProcessorBasedTest();
	}

	public void testTypeVariableVisitor() {
		testCaseIdentifier = Cases.TypeVariable;
		runProcessorBasedTest();
	}

	public void testWildcardTypeVisitor() {
		testCaseIdentifier = Cases.WildcardType;
		runProcessorBasedTest();
	}


	/**
	 * Instantiate the AnnotationProcessor to run the actual tests
	 */
	void runProcessorBasedTest() {
		TypeVisitorProc p = new TypeVisitorProc();
		GenericFactory.PROCESSOR = p;

		IProject project = env.getProject(getProjectName());
		IPath srcRoot = getSourcePath();

		env.addClass(srcRoot, "test", "Test", code);

		fullBuild( project.getFullPath() );
		expectingNoProblems();

		assertTrue("Processor not invoked", p.called);
	}


	/**
	 * Annotation Processor containing the actual tests
	 */
	class TypeVisitorProc extends AbstractGenericProcessor {
		boolean called;

		@Override
		public void _process() {
			called = true;
			assertTrue(decls.size() == 1);

			initTypeVisitList();

			TypeDeclaration typeDecl = env.getTypeDeclarations().iterator().next();
			Collection<TypeDeclaration> nestedTypes = typeDecl.getNestedTypes();
			Collection<FieldDeclaration> fieldDecls = typeDecl.getFields();
			ClassDeclaration classDecl = null;
			FieldDeclaration fieldDecl = null;
			PrimitiveType primitiveType = null;
			AnnotationMirror annoMirror = null;
			Types typesUtil = env.getTypeUtils();

			switch (testCaseIdentifier) {

			case PrimitiveType :
				for(FieldDeclaration tempDecl : fieldDecls) {
					if(tempDecl.getSimpleName().equals("j")) {
						fieldDecl = tempDecl;
					}
				}
				primitiveType = (PrimitiveType)fieldDecl.getType();
				primitiveType.accept(new TypeVisitorImpl());
				assertEquals("Expect one visitor", 1, typesVisited.size());
				assertEquals("Expected PrimitiveType visitor", "PrimitiveTypeImpl", typesVisited.get(0).getClass().getSimpleName());
				break;

			case VoidType :
				MethodDeclaration methodDecl = typeDecl.getMethods().iterator().next();
				VoidType voidType = (VoidType)methodDecl.getReturnType();
				voidType.accept(new TypeVisitorImpl());
				assertEquals("Expect one visitor", 1, typesVisited.size());
				assertEquals("Expected VoidType visitor", "VoidTypeImpl", typesVisited.get(0).getClass().getSimpleName());
				break;

			case ClassType :
				for(TypeDeclaration tempDecl : nestedTypes) {
					if(tempDecl.getSimpleName().equals("C")) {
						classDecl = (ClassDeclaration)tempDecl;
					}
				}
				ClassType classType = classDecl.getSuperclass();
				classType.accept(new TypeVisitorImpl());
				assertEquals("Expect one visitor", 1, typesVisited.size());
				assertEquals("Expected ClassType visitor", "ClassDeclarationImpl", typesVisited.get(0).getClass().getSimpleName());
				break;

			case EnumType :
				for(FieldDeclaration tempDecl : fieldDecls) {
					if(tempDecl.getSimpleName().equals("s")) {
						fieldDecl = tempDecl;
					}
				}
				annoMirror = fieldDecl.getAnnotationMirrors().iterator().next();
				EnumType enumType = (EnumType)annoMirror.getElementValues().keySet().iterator().next().getReturnType();
				enumType.accept(new TypeVisitorImpl());
				assertEquals("Expect one visitor", 1, typesVisited.size());
				assertEquals("Expected EnumType visitor", "EnumDeclarationImpl", typesVisited.get(0).getClass().getSimpleName());
				break;

			case InterfaceType :
				for(TypeDeclaration tempDecl : nestedTypes) {
					if(tempDecl.getSimpleName().equals("D")) {
						classDecl = (ClassDeclaration)tempDecl;
					}
				}
				InterfaceType interfaceType = classDecl.getSuperinterfaces().iterator().next();
				interfaceType.accept(new TypeVisitorImpl());
				assertEquals("Expect one visitor", 1, typesVisited.size());
				assertEquals("Expected InterfaceType visitor", "InterfaceDeclarationImpl", typesVisited.get(0).getClass().getSimpleName());
				break;

			case AnnotationType :
				for(FieldDeclaration tempDecl : fieldDecls) {
					if(tempDecl.getSimpleName().equals("s")) {
						fieldDecl = tempDecl;
					}
				}
				annoMirror = fieldDecl.getAnnotationMirrors().iterator().next();
				AnnotationType annoType = annoMirror.getAnnotationType();
				annoType.accept(new TypeVisitorImpl());
				assertEquals("Expect one visitor", 1, typesVisited.size());
				assertEquals("Expected AnnotationType visitor", "AnnotationDeclarationImpl", typesVisited.get(0).getClass().getSimpleName());
				break;

			case ArrayType :
				for(FieldDeclaration tempDecl : fieldDecls) {
					if(tempDecl.getSimpleName().equals("k")) {
						fieldDecl = tempDecl;
					}
				}
				ArrayType arrayType = (ArrayType)fieldDecl.getType();
				arrayType.accept(new TypeVisitorImpl());
				assertEquals("Expect one visitor", 1, typesVisited.size());
				assertEquals("Expected ArrayType visitor", "ArrayTypeImpl", typesVisited.get(0).getClass().getSimpleName());
				break;

			case TypeVariable :
				for(TypeDeclaration tempDecl : nestedTypes) {
					if(tempDecl.getSimpleName().equals("P")) {
						classDecl = (ClassDeclaration)tempDecl;
					}
				}
				TypeParameterDeclaration typeParamDecl = classDecl.getFormalTypeParameters().iterator().next();
				TypeVariable typeVariable = typesUtil.getTypeVariable(typeParamDecl);
				typeVariable.accept(new TypeVisitorImpl());
				assertEquals("Expect one visitor", 1, typesVisited.size());
				assertEquals("Expected TypeVariable visitor", "TypeParameterDeclarationImpl", typesVisited.get(0).getClass().getSimpleName());
				break;

			case WildcardType :
				for(FieldDeclaration tempDecl : fieldDecls) {
					if(tempDecl.getSimpleName().equals("ln")) {
						fieldDecl = tempDecl;
					}
				}
				InterfaceType wildcardList = (InterfaceType)fieldDecl.getType();
				WildcardType wildcardType = (WildcardType)wildcardList.getActualTypeArguments().iterator().next();
				wildcardType.accept(new TypeVisitorImpl());
				assertEquals("Expect one visitor", 1, typesVisited.size());
				assertEquals("Expected WildcardType visitor", "WildcardTypeImpl", typesVisited.get(0).getClass().getSimpleName());
				break;
			}
		}
	}


	/**
	 * TypeVisitor implementation for the purposes of these tests
	 */
	class TypeVisitorImpl implements TypeVisitor {

		public void visitTypeMirror(TypeMirror t) {
			fail("Should never visit a TypeMirror, only a subclass: " + t);
		}

		public void visitPrimitiveType(PrimitiveType t) {
			typeVisited(t);
		}

		public void visitVoidType(VoidType t) {
			typeVisited(t);
		}

		public void visitReferenceType(ReferenceType t) {
			fail("Should never visit a ReferenceType, only a subclass: " + t);
		}

		public void visitDeclaredType(DeclaredType t) {
			fail("Should never visit a DeclaredType, only a subclass: " + t);
		}

		public void visitClassType(ClassType t) {
			typeVisited(t);
		}

		public void visitEnumType(EnumType t) {
			typeVisited(t);
		}

		public void visitInterfaceType(InterfaceType t) {
			typeVisited(t);
		}

		public void visitAnnotationType(AnnotationType t) {
			typeVisited(t);
		}

		public void visitArrayType(ArrayType t) {
			typeVisited(t);
		}

		public void visitTypeVariable(TypeVariable t) {
			typeVisited(t);
		}

		public void visitWildcardType(WildcardType t) {
			typeVisited(t);
		}
	}


	/*
	 * Utilities for running the TypeVisitor tests
	 */

	enum Cases {
		PrimitiveType,
		VoidType,
		ClassType,
		EnumType,
		InterfaceType,
		AnnotationType,
		ArrayType,
		TypeVariable,
		WildcardType
	}

	Cases testCaseIdentifier;

	ArrayList<TypeMirror> typesVisited = new ArrayList<>();

	void typeVisited(TypeMirror t) {
			typesVisited.add(t);
	}

	void initTypeVisitList() {
		if(typesVisited.size() > 0) {
			typesVisited.clear();
		}
	}

	final String code =
		"package test;" + "\n" +
		"import org.eclipse.jdt.apt.tests.annotations.generic.*;" + "\n" +
		"import java.util.List;" + "\n" +
		"@GenericAnnotation public class Test" + "\n" +
		"{" + "\n" +
		"    int j;" + "\n" +
		"    int k[];" + "\n" +
		"    List<? extends Number> ln;" + "\n" +
		"    class C {}" + "\n" +
		"    class P<T> { }" + "\n" +
		"    class D implements Runnable { public void run () {} }" + "\n" +
		"    void m() {}" + "\n" +
		"    enum E { elephant }" + "\n" +
		"    @interface B { E e(); }" + "\n" +
		"    @B(e = E.elephant)" + "\n" +
		"    String s;" + "\n" +
		"}";
}
