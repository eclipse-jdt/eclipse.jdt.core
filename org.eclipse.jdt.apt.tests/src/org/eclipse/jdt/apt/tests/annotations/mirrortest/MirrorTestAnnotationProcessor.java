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

import org.eclipse.jdt.apt.tests.annotations.BaseProcessor;
import org.eclipse.jdt.apt.tests.annotations.ProcessorTestStatus;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.EnumDeclaration;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.InterfaceDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.PackageDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.declaration.TypeParameterDeclaration;
import com.sun.mirror.type.ArrayType;
import com.sun.mirror.type.InterfaceType;
import com.sun.mirror.type.PrimitiveType;
import com.sun.mirror.util.Declarations;

@SuppressWarnings("nls")
public class MirrorTestAnnotationProcessor extends BaseProcessor {
	
	private static final String[] CLASSNAMES = {
		"java.util.Map.Entry",
		"java.lang.Object",
		"java.lang.String",
		"java.util.concurrent.TimeUnit",
		"java.lang.Override"
	};
	
	private static final String[] NONEXISTANT_CLASSNAMES = {
		"java.util.NotExist",
		"java.annotation.NotExist.ReallyNotExist",
		"bar.baz.Foo"
	};
	
	public static boolean _processRun = false;
	
	public MirrorTestAnnotationProcessor(AnnotationProcessorEnvironment env) {
		super(env);
	}

	public void process() {
		_processRun = true;
		try {
			Collection<TypeDeclaration> decls = _env.getSpecifiedTypeDeclarations();
			TypeDeclaration decl = null;
			for (TypeDeclaration declTemp : decls) {
				if (CodeExample.CODE_FULL_NAME.equals(declTemp.getQualifiedName()))
					decl = declTemp;
			}
			testTypeDecl(decl);
			testDeclarationsUtil(decl);
			testPackageImpl();
			testGetTypeDeclarations();
		}
		catch (Throwable t) {
			if (!ProcessorTestStatus.hasErrors()) {
				ProcessorTestStatus.failWithoutException(t.toString());
			}
			t.printStackTrace();
		}
	}
	
	private void testTypeDecl(TypeDeclaration typeDecl) {
		ProcessorTestStatus.assertEquals("Type name", 
				CodeExample.CODE_PACKAGE + "." + CodeExample.CODE_CLASS_NAME,
				typeDecl.getQualifiedName());
		
		PackageDeclaration pkg = typeDecl.getPackage();
		ProcessorTestStatus.assertEquals("Package", CodeExample.CODE_PACKAGE, pkg.getQualifiedName());
		
		Collection<FieldDeclaration> fields = typeDecl.getFields();
		ProcessorTestStatus.assertEquals("Number of fields: " + fields, 4, fields.size());
		
		// Test for multi-dimensional arrays
		boolean testedMultiDimensionalCase = false;
		for (FieldDeclaration fd : fields) {
			if (fd.getSimpleName().equals("multiArray")) {
				ArrayType outerArray = (ArrayType)fd.getType();
				ArrayType innerArray = (ArrayType)outerArray.getComponentType();
				PrimitiveType primitiveType = (PrimitiveType)innerArray.getComponentType();
				ProcessorTestStatus.assertTrue("Expected boolean, but found " + primitiveType, PrimitiveType.Kind.BOOLEAN == primitiveType.getKind());
				testedMultiDimensionalCase = true;
			}
		}
		ProcessorTestStatus.assertTrue("Never hit the multidimensional array case. Check if boolean[][] multiArray exists in source", testedMultiDimensionalCase);
		
		Collection<TypeParameterDeclaration> typeParams = typeDecl.getFormalTypeParameters();
		ProcessorTestStatus.assertEquals("Number of type params", 0, typeParams.size());
		
		Collection<? extends MethodDeclaration> methods = typeDecl.getMethods();
		ProcessorTestStatus.assertEquals("Number of methods", 3, methods.size());
		
		Collection<TypeDeclaration> nestedTypes = typeDecl.getNestedTypes();
		ProcessorTestStatus.assertEquals("Number of nested types", 1, nestedTypes.size());
		
		Collection<InterfaceType> supers = typeDecl.getSuperinterfaces();
		ProcessorTestStatus.assertEquals("Number of supers", 1, supers.size());
	}
	
	private void testPackageImpl() {
		PackageDeclaration pkg = _env.getPackage("org.eclipse.jdt.apt.tests.annotations.mirrortest");
		ProcessorTestStatus.assertEquals("Package name", "org.eclipse.jdt.apt.tests.annotations.mirrortest", pkg.getQualifiedName());
		// Not sure if this is the best way to test -- can we count on the number of classes 
		// remaining the same in java.util?
		
		pkg = _env.getPackage("java");
		ProcessorTestStatus.assertEquals("Package name", "java", pkg.getQualifiedName());
		ProcessorTestStatus.assertEquals("Number of classes in java", 0, pkg.getClasses().size());
		
		pkg = _env.getPackage("java.util");
		ProcessorTestStatus.assertEquals("Package name", "java.util", pkg.getQualifiedName());
		
		Collection<ClassDeclaration> classes = pkg.getClasses();
		ProcessorTestStatus.assertEquals("Number of classes in java.util", 79, classes.size());
		
		Collection<EnumDeclaration> enums = pkg.getEnums();
		ProcessorTestStatus.assertEquals("Number of enums in java.util", 0, enums.size());
		
		Collection<InterfaceDeclaration> interfaces = pkg.getInterfaces();
		ProcessorTestStatus.assertEquals("Number of interfaces in java.util", 15, interfaces.size());
	}
	
	private void testDeclarationsUtil(TypeDeclaration typeDecl) {
		Declarations utils = _env.getDeclarationUtils();
		TypeDeclaration objType = _env.getTypeDeclaration("java.lang.Object");
		
		// Test negative case
		ProcessorTestStatus.assertTrue("Class hides Object", !utils.hides(typeDecl, objType));
		
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
		ProcessorTestStatus.assertTrue("inner's staticMethod() should hide MirrorTestClass'", utils.hides(innerMethod, outerMethod));
	}
	
	private void testGetTypeDeclarations() {
		for (String className : CLASSNAMES) {
			testGetTypeDeclaration(className);
		}
		for (String className : NONEXISTANT_CLASSNAMES) {
			testNonExistantTypeDeclaration(className);
		}
	}
	
	private void testGetTypeDeclaration(String className) {
		TypeDeclaration type = _env.getTypeDeclaration(className);
		ProcessorTestStatus.assertTrue("Could not find " + className, type != null);
		ProcessorTestStatus.assertTrue("Name is incorrect", className.equals(type.getQualifiedName()));
	}
	
	private void testNonExistantTypeDeclaration(String className) {
		TypeDeclaration type = _env.getTypeDeclaration(className);
		ProcessorTestStatus.assertTrue("Bad class found: " + className, null == type);
	}
}
