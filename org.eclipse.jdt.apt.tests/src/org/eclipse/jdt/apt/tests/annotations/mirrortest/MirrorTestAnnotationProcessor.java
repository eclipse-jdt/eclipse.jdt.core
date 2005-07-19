/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    jgarms@bea.com - initial API and implementation
 *    
 *******************************************************************************/


package org.eclipse.jdt.apt.tests.annotations.mirrortest;

import java.util.Collection;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.EnumDeclaration;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.InterfaceDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.PackageDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.declaration.TypeParameterDeclaration;
import com.sun.mirror.type.InterfaceType;
import com.sun.mirror.util.Declarations;

@SuppressWarnings("nls")
public class MirrorTestAnnotationProcessor implements AnnotationProcessor {
	
	public static final String NO_ERRORS = "NO ERRORS"; //$NON-NLS-1$
	
	/** Used by the test harness to verify that no errors were encountered **/
	public static String ERROR = NO_ERRORS;
	
	/** Used by the test harness to determine if the processor was ever triggered **/
	public static boolean _processRun = false;
	
	private final AnnotationProcessorEnvironment env;
	
	public MirrorTestAnnotationProcessor(AnnotationProcessorEnvironment env) {
		this.env = env;
	}

	public void process() {
		_processRun = true;
		try {
			Collection<TypeDeclaration> decls = env.getSpecifiedTypeDeclarations();
			TypeDeclaration decl = null;
			for (TypeDeclaration declTemp : decls) {
				if (CodeExample.CODE_FULL_NAME.equals(declTemp.getQualifiedName()))
					decl = declTemp;
			}
			testTypeDecl(decl);
			testDeclarationsUtil(decl);
			testPackageImpl();
		}
		catch (Throwable t) {
			if (ERROR == NO_ERRORS) {
				ERROR = t.toString();
			}
			t.printStackTrace();
		}
	}
	
	private void testTypeDecl(TypeDeclaration typeDecl) {
		assertEquals("Type name", 
				CodeExample.CODE_PACKAGE + "." + CodeExample.CODE_CLASS_NAME,
				typeDecl.getQualifiedName());
		
		PackageDeclaration pkg = typeDecl.getPackage();
		assertEquals("Package", CodeExample.CODE_PACKAGE, pkg.getQualifiedName());
		
		Collection<FieldDeclaration> fields = typeDecl.getFields();
		assertEquals("Number of fields: " + fields, 3, fields.size());
		
		Collection<TypeParameterDeclaration> typeParams = typeDecl.getFormalTypeParameters();
		assertEquals("Number of type params", 0, typeParams.size());
		
		Collection<? extends MethodDeclaration> methods = typeDecl.getMethods();
		assertEquals("Number of methods", 3, methods.size());
		
		Collection<TypeDeclaration> nestedTypes = typeDecl.getNestedTypes();
		assertEquals("Number of nested types", 1, nestedTypes.size());
		
		Collection<InterfaceType> supers = typeDecl.getSuperinterfaces();
		assertEquals("Number of supers", 1, supers.size());
	}
	
	private void testPackageImpl() {
		PackageDeclaration pkg = env.getPackage("org.eclipse.jdt.apt.tests.annotations.mirrortest");
		assertEquals("Package name", "org.eclipse.jdt.apt.tests.annotations.mirrortest", pkg.getQualifiedName());
		// Not sure if this is the best way to test -- can we count on the number of classes 
		// remaining the same in java.util?
		
		pkg = env.getPackage("java");
		assertEquals("Package name", "java", pkg.getQualifiedName());
		assertEquals("Number of classes in java", 0, pkg.getClasses().size());
		
		pkg = env.getPackage("java.util");
		assertEquals("Package name", "java.util", pkg.getQualifiedName());
		
		Collection<ClassDeclaration> classes = pkg.getClasses();
		assertEquals("Number of classes in java.util", 79, classes.size());
		
		Collection<EnumDeclaration> enums = pkg.getEnums();
		assertEquals("Number of enums in java.util", 0, enums.size());
		
		Collection<InterfaceDeclaration> interfaces = pkg.getInterfaces();
		assertEquals("Number of interfaces in java.util", 15, interfaces.size());
	}
	
	private void testDeclarationsUtil(TypeDeclaration typeDecl) {
		Declarations utils = env.getDeclarationUtils();
		TypeDeclaration objType = env.getTypeDeclaration("java.lang.Object");
		
		// Test negative case
		assertTrue("Class hides Object", !utils.hides(typeDecl, objType));
		
		// Test positive case
		TypeDeclaration innerClass = typeDecl.getNestedTypes().iterator().next();
		
		MethodDeclaration innerMethod = null;
		for (MethodDeclaration method : innerClass.getMethods()) {
			if (method.getSimpleName().equals("staticMethod")) {
				innerMethod = method;
				break;
			}
		}
		
		MethodDeclaration outerMethod = null;
		for (MethodDeclaration method : typeDecl.getMethods()) {
			if (method.getSimpleName().equals("staticMethod")) {
				outerMethod = method;
				break;
			}
		}
		assertTrue("inner's staticMethod() should hide MirrorTestClass'", utils.hides(innerMethod, outerMethod));
	}
	
	private void assertEquals(String reason, String expected, String actual) {
		if (expected == actual)
			return;
		if (expected != null && expected.equals(actual))
			return;
		fail("Expected " + expected + ", but saw " + actual + ". Reason: " + reason);
	}
	
	private void assertEquals(String reason, int expected, int actual) {
		if (expected == actual)
			return;
		fail("Expected " + expected + ", but saw " + actual + ". Reason: " + reason);
	}
	
	private void assertTrue(String reason, boolean expected) {
		if (!expected)
			fail(reason);
	}
	
	private void fail(final String reason) {
		ERROR = reason;
		throw new IllegalStateException("Failed during test: " + reason);
	}
	
	

}
