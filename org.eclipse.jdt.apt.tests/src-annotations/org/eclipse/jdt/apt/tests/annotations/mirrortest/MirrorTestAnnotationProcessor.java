/*******************************************************************************
 * Copyright (c) 2005, 2008 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    jgarms@bea.com - initial API and implementation
 *
 *******************************************************************************/


package org.eclipse.jdt.apt.tests.annotations.mirrortest;

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
import java.util.Collection;
import org.eclipse.jdt.apt.tests.annotations.BaseProcessor;
import org.eclipse.jdt.apt.tests.annotations.ProcessorTestStatus;

public class MirrorTestAnnotationProcessor extends BaseProcessor {

	public MirrorTestAnnotationProcessor(AnnotationProcessorEnvironment env) {
		super(env);
	}

	public void process() {
		ProcessorTestStatus.setProcessorRan();
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
		ProcessorTestStatus.assertEquals("Number of fields: " + fields, 3, fields.size());

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
		// We used to test by counting the number of elements returned by the various calls,
		// but that is not stable between different JDKs.

		pkg = _env.getPackage("java");
		ProcessorTestStatus.assertEquals("Package name", "java", pkg.getQualifiedName());
		ProcessorTestStatus.assertEquals("Number of classes in java", 0, pkg.getClasses().size());

		pkg = _env.getPackage("java.util");
		ProcessorTestStatus.assertEquals("Package name", "java.util", pkg.getQualifiedName());

		Collection<ClassDeclaration> classes = pkg.getClasses();
		TypeDeclaration stringDecl = _env.getTypeDeclaration("java.util.Collections");
		ProcessorTestStatus.assertTrue("java.util contains String", classes.contains(stringDecl));

		Collection<EnumDeclaration> enums = pkg.getEnums();
		ProcessorTestStatus.assertEquals("Number of enums in java.util", 0, enums.size());

		TypeDeclaration iteratorDecl = _env.getTypeDeclaration("java.util.Iterator");
		Collection<InterfaceDeclaration> interfaces = pkg.getInterfaces();
		ProcessorTestStatus.assertTrue("java.util contains Iterator", interfaces.contains(iteratorDecl));
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
}
