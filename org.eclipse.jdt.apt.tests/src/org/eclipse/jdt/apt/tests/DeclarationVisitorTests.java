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

import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.AnnotationTypeElementDeclaration;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.ConstructorDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.EnumConstantDeclaration;
import com.sun.mirror.declaration.EnumDeclaration;
import com.sun.mirror.declaration.ExecutableDeclaration;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.InterfaceDeclaration;
import com.sun.mirror.declaration.MemberDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.PackageDeclaration;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.declaration.TypeParameterDeclaration;
import com.sun.mirror.util.DeclarationVisitor;

/**
 * Tests for the JDT-APT implementation of Declaration Visitors
 */
public class DeclarationVisitorTests extends APTTestBase {

	public DeclarationVisitorTests(final String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(DeclarationVisitorTests.class);
	}

	public void testPackageDeclarationVisitor() {
		testCaseIdentifier = Cases.PackageDeclaration;
		runProcessorBasedTest();
	}

	public void testClassDeclarationVisitor() {
		testCaseIdentifier = Cases.ClassDeclaration;
		runProcessorBasedTest();
	}

	public void testEnumDeclarationVisitor() {
		testCaseIdentifier = Cases.EnumDeclaration;
		runProcessorBasedTest();
	}

	public void testInterfaceDeclarationVisitor() {
		testCaseIdentifier = Cases.InterfaceDeclaration;
		runProcessorBasedTest();
	}

	public void testAnnotationTypeDeclarationVisitor() {
		testCaseIdentifier = Cases.AnnotationTypeDeclaration;
		runProcessorBasedTest();
	}

	public void testFieldDeclarationVisitor() {
		testCaseIdentifier = Cases.FieldDeclaration;
		runProcessorBasedTest();
	}

	public void testEnumConstantDeclarationVisitor() {
		testCaseIdentifier = Cases.EnumConstantDeclaration;
		runProcessorBasedTest();
	}

	public void testConstructorDeclarationVisitor() {
		testCaseIdentifier = Cases.ConstructorDeclaration;
		runProcessorBasedTest();
	}

	public void testMethodDeclarationVisitor() {
		testCaseIdentifier = Cases.MethodDeclaration;
		runProcessorBasedTest();
	}

	public void testAnnotationTypeElementDeclarationVisitor() {
		testCaseIdentifier = Cases.AnnotationTypeElementDeclaration;
		runProcessorBasedTest();
	}

	public void testParameterDeclarationVisitor() {
		testCaseIdentifier = Cases.ParameterDeclaration;
		runProcessorBasedTest();
	}

	public void testTypeParameterDeclarationVisitor() {
		testCaseIdentifier = Cases.TypeParameterDeclaration;
		runProcessorBasedTest();
	}


	/**
	 * Instantiate the AnnotationProcessor to run the actual tests
	 */
	void runProcessorBasedTest() {
		DeclarationVisitorProc p = new DeclarationVisitorProc();
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
	class DeclarationVisitorProc extends AbstractGenericProcessor {
		boolean called;

		@Override
		public void _process() {

			called = true;
			assertTrue(decls.size() == 1);

			initDeclVisitList();

			TypeDeclaration typeDecl = env.getTypeDeclarations().iterator().next();
			Collection<TypeDeclaration> nestedTypes = typeDecl.getNestedTypes();
			ClassDeclaration classDecl = null;
			EnumDeclaration enumDecl = null;
			InterfaceDeclaration interfaceDecl = null;
			AnnotationTypeDeclaration annoTypeDecl = null;
			EnumConstantDeclaration enumConstantDecl = null;
			MethodDeclaration methodDecl = null;

			switch (testCaseIdentifier) {

			case PackageDeclaration :
				PackageDeclaration packageDecl = typeDecl.getPackage();
				packageDecl.accept(new DeclarationVisitorImpl());
				assertEquals("Expect one visitor", 1, declarationsVisited.size());
				assertEquals("Expected PackageDeclaration visitor", "PackageDeclarationImpl", declarationsVisited.get(0).getClass().getSimpleName());
				break;

			case ClassDeclaration :
				for(TypeDeclaration tempDecl : nestedTypes) {
					if(tempDecl.getSimpleName().equals("C")) {
						classDecl = (ClassDeclaration)tempDecl;
					}
				}
				classDecl.accept(new DeclarationVisitorImpl());
				assertEquals("Expect one visitor", 1, declarationsVisited.size());
				assertEquals("Expected ClassDeclaration visitor", "ClassDeclarationImpl", declarationsVisited.get(0).getClass().getSimpleName());
				break;

			case EnumDeclaration :
				for(TypeDeclaration tempDecl : nestedTypes) {
					if(tempDecl.getSimpleName().equals("E")) {
						enumDecl = (EnumDeclaration)tempDecl;
					}
				}
				enumDecl.accept(new DeclarationVisitorImpl());
				assertEquals("Expect one visitor", 1, declarationsVisited.size());
				assertEquals("Expected EnumDeclaration visitor", "EnumDeclarationImpl", declarationsVisited.get(0).getClass().getSimpleName());
				break;

			case InterfaceDeclaration :
				for(TypeDeclaration tempDecl : nestedTypes) {
					if(tempDecl.getSimpleName().equals("I")) {
						interfaceDecl = (InterfaceDeclaration)tempDecl;
					}
				}
				interfaceDecl.accept(new DeclarationVisitorImpl());
				assertEquals("Expect one visitor", 1, declarationsVisited.size());
				assertEquals("Expected InterfaceDeclaration visitor", "InterfaceDeclarationImpl", declarationsVisited.get(0).getClass().getSimpleName());
				break;

			case AnnotationTypeDeclaration :
				for(TypeDeclaration tempDecl : nestedTypes) {
					if(tempDecl.getSimpleName().equals("A")) {
						annoTypeDecl = (AnnotationTypeDeclaration)tempDecl;
					}
				}
				annoTypeDecl.accept(new DeclarationVisitorImpl());
				assertEquals("Expect one visitor", 1, declarationsVisited.size());
				assertEquals("Expected AnnotationTypeDeclaration visitor", "AnnotationDeclarationImpl", declarationsVisited.get(0).getClass().getSimpleName());
				break;

			case FieldDeclaration :
				FieldDeclaration fieldDecl = typeDecl.getFields().iterator().next();
				fieldDecl.accept(new DeclarationVisitorImpl());
				assertEquals("Expect one visitor", 1, declarationsVisited.size());
				assertEquals("Expected FieldDeclaration visitor", "FieldDeclarationImpl", declarationsVisited.get(0).getClass().getSimpleName());
				break;

			case EnumConstantDeclaration :
				for(TypeDeclaration tempDecl : nestedTypes) {
					if(tempDecl.getSimpleName().equals("E")) {
						enumDecl = (EnumDeclaration)tempDecl;
					}
				}
				enumConstantDecl = enumDecl.getEnumConstants().iterator().next();
				enumConstantDecl.accept(new DeclarationVisitorImpl());
				assertEquals("Expect one visitor", 1, declarationsVisited.size());
				assertEquals("Expected EnumConstantDeclaration visitor", "EnumConstantDeclarationImpl", declarationsVisited.get(0).getClass().getSimpleName());
				break;

			case ConstructorDeclaration :
				for(TypeDeclaration tempDecl : nestedTypes) {
					if(tempDecl.getSimpleName().equals("C")) {
						classDecl = (ClassDeclaration)tempDecl;
					}
				}
				ConstructorDeclaration constructorDecl = classDecl.getConstructors().iterator().next();
				constructorDecl.accept(new DeclarationVisitorImpl());
				assertEquals("Expect one visitor", 1, declarationsVisited.size());
				assertEquals("Expected ConstructorDeclaration visitor", "ConstructorDeclarationImpl", declarationsVisited.get(0).getClass().getSimpleName());
				break;

			case MethodDeclaration :
				methodDecl = typeDecl.getMethods().iterator().next();
				methodDecl.accept(new DeclarationVisitorImpl());
				assertEquals("Expect one visitor", 1, declarationsVisited.size());
				assertEquals("Expected MethodDeclaration visitor", "MethodDeclarationImpl", declarationsVisited.get(0).getClass().getSimpleName());
				break;

			case AnnotationTypeElementDeclaration :
				for(TypeDeclaration tempDecl : nestedTypes) {
					if(tempDecl.getSimpleName().equals("A")) {
						annoTypeDecl = (AnnotationTypeDeclaration)tempDecl;
					}
				}
				AnnotationTypeElementDeclaration annoTypeElementDecl = annoTypeDecl.getMethods().iterator().next();
				annoTypeElementDecl.accept(new DeclarationVisitorImpl());
				assertEquals("Expect one visitor", 1, declarationsVisited.size());
				assertEquals("Expected AnnotationTypeElementDeclaration visitor", "AnnotationElementDeclarationImpl", declarationsVisited.get(0).getClass().getSimpleName());
				break;

			case ParameterDeclaration :
				methodDecl = typeDecl.getMethods().iterator().next();
				ParameterDeclaration paramDecl = methodDecl.getParameters().iterator().next();
				paramDecl.accept(new DeclarationVisitorImpl());
				assertEquals("Expect one visitor", 1, declarationsVisited.size());
				assertEquals("Expected ParameterDeclaration visitor", "SourceParameterDeclarationImpl", declarationsVisited.get(0).getClass().getSimpleName());
				break;

			case TypeParameterDeclaration :
				for(TypeDeclaration tempDecl : nestedTypes) {
					if(tempDecl.getSimpleName().equals("P")) {
						classDecl = (ClassDeclaration)tempDecl;
					}
				}
				TypeParameterDeclaration typeParamDecl = classDecl.getFormalTypeParameters().iterator().next();
				typeParamDecl.accept(new DeclarationVisitorImpl());
				assertEquals("Expect one visitor", 1, declarationsVisited.size());
				assertEquals("Expected TypeParameterDeclaration visitor", "TypeParameterDeclarationImpl", declarationsVisited.get(0).getClass().getSimpleName());
				break;
			}

		}

	}


	/**
	 * DeclarationVisitor implementation for the purposes of these tests
	 */
	class DeclarationVisitorImpl implements DeclarationVisitor {

		public void visitDeclaration(Declaration d) {
			fail("Should never visit a Declaration, only a subclass: " + d);
		}

		public void visitPackageDeclaration(PackageDeclaration d) {
			declarationVisited(d);
		}

		public void visitMemberDeclaration(MemberDeclaration d) {
			fail("Should never visit a Member, only a subclass: " + d);
		}

		public void visitTypeDeclaration(TypeDeclaration d) {
			fail("Should never visit a Type, only a subclass: " + d);
		}

		public void visitClassDeclaration(ClassDeclaration d) {
			declarationVisited(d);
		}

		public void visitEnumDeclaration(EnumDeclaration d) {
			declarationVisited(d);
		}

		public void visitInterfaceDeclaration(InterfaceDeclaration d) {
			declarationVisited(d);
		}

		public void visitAnnotationTypeDeclaration(AnnotationTypeDeclaration d) {
			declarationVisited(d);
		}

		public void visitFieldDeclaration(FieldDeclaration d) {
			declarationVisited(d);
		}

		public void visitEnumConstantDeclaration(EnumConstantDeclaration d) {
			declarationVisited(d);
		}

		public void visitExecutableDeclaration(ExecutableDeclaration d) {
			fail("Should never visit an ExecutableDeclaration, only a subclass: " + d);
		}

		public void visitConstructorDeclaration(ConstructorDeclaration d) {
			declarationVisited(d);
		}

		public void visitMethodDeclaration(MethodDeclaration d) {
			declarationVisited(d);
		}

		public void visitAnnotationTypeElementDeclaration(AnnotationTypeElementDeclaration d) {
			declarationVisited(d);
		}

		public void visitParameterDeclaration(ParameterDeclaration d) {
			declarationVisited(d);
		}

		public void visitTypeParameterDeclaration(TypeParameterDeclaration d) {
			declarationVisited(d);
		}
	}


	/*
	 * Utilities for running the DeclarationVisitor tests
	 */

	enum Cases {
		PackageDeclaration,
		ClassDeclaration,
		EnumDeclaration,
		InterfaceDeclaration,
		AnnotationTypeDeclaration,
		FieldDeclaration,
		EnumConstantDeclaration,
		ConstructorDeclaration,
		MethodDeclaration,
		AnnotationTypeElementDeclaration,
		ParameterDeclaration,
		TypeParameterDeclaration
	}

	Cases testCaseIdentifier;

	ArrayList<Declaration> declarationsVisited = new ArrayList<>();

	void declarationVisited(Declaration d) {
			declarationsVisited.add(d);
	}

	void initDeclVisitList() {
		if(declarationsVisited.size() > 0) {
			declarationsVisited.clear();
		}
	}

	final String code =
		"package test;" + "\n" +
		"import org.eclipse.jdt.apt.tests.annotations.generic.*;" + "\n" +
		"@GenericAnnotation public class Test" + "\n" +
		"{" + "\n" +
		"    Test() {}" + "\n" +
		"    String s;" + "\n" +
		"    class C {}" + "\n" +
		"    class P<T> {}" + "\n" +
		"    interface I {}" + "\n" +
		"    void m(int i) {}" + "\n" +
		"    enum E { elephant }" + "\n" +
		"    @interface A { String strValue() default \"\"; }" + "\n" +
		"}";
}
