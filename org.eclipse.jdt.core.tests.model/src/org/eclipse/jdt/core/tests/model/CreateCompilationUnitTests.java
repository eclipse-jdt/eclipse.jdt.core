/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.model;

import java.io.File;
import java.io.IOException;
import junit.framework.Test;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

public class CreateCompilationUnitTests extends ModifyingResourceTests {
public CreateCompilationUnitTests(String name) {
	super(name);
}
public static Test suite() {
	return buildModelTestSuite(CreateCompilationUnitTests.class);
}
@Override
public void setUp() throws Exception {
	super.setUp();
	createJavaProject("P");
	createFolder("/P/p");
	startDeltas();
}
@Override
public void tearDown() throws Exception {
	stopDeltas();
	deleteProject("P");
	super.tearDown();
}
/**
 * Ensures that a compilation unit can be created with specified source
 * in a package.
 * Verifies that the proper change deltas are generated as a side effect
 * of running the operation.
 * Ensure that the import container has been created correctly.
 */
public void testCUAndImportContainer() throws JavaModelException {
	IPackageFragment pkg = getPackage("/P/p");
	ICompilationUnit cu= pkg.createCompilationUnit("HelloImports.java",
		("package p;\n" +
		"\n" +
		"import java.util.Enumeration;\n" +
		"import java.util.Vector;\n" +
		"\n" +
		"public class HelloImports {\n" +
		"\n" +
		"	public static main(String[] args) {\n" +
		"		System.out.println(\"HelloWorld\");\n" +
		"	}\n" +
		"}\n"),  false,null);
	assertCreation(cu);
	assertDeltas(
		"Unexpected delta",
		"P[*]: {CHILDREN}\n" +
		"	<project root>[*]: {CHILDREN}\n" +
		"		p[*]: {CHILDREN}\n" +
		"			HelloImports.java[+]: {}"
	);
	IImportDeclaration[] imprts= cu.getImports();
	assertTrue("Import does not exist", imprts.length == 2 && imprts[0].exists());
	cu.close();
	imprts= cu.getImports();
	assertTrue("Import does not exist", imprts.length == 2 && imprts[0].exists());
}
/**
 * Ensures that a default compilation unit is created for a type if
 * it does not yet exist.
 */
public void testDefaultCU() throws CoreException {
	IPackageFragment pkg = getPackage("/P/p");
	ICompilationUnit cu= pkg.getCompilationUnit("Default.java");
	IType type= cu.createType("public class Default {}", null, false, null);
	assertCreation(cu);
	assertCreation(type);
	assertDeltas(
		"Unexpected delta",
			"P[*]: {CHILDREN}\n" +
			"	<project root>[*]: {CHILDREN}\n" +
			"		p[*]: {CHILDREN}\n" +
			"			Default.java[+]: {}\n" +
			"\n" +
			"P[*]: {CHILDREN}\n" +
			"	<project root>[*]: {CHILDREN}\n" +
			"		p[*]: {CHILDREN}\n" +
			"			Default.java[*]: {CHILDREN | FINE GRAINED | PRIMARY RESOURCE}\n" +
			"				Default[+]: {}"
	);
	// CU should have a package statement and type
	assertElementDescendants(
		"Unexpected children",
		"Default.java\n" +
		"  package p\n" +
		"  class Default",
		cu);

	// should fail if we try again
	try {
		pkg.createCompilationUnit("Default.java", "", false, null);
	} catch (JavaModelException jme) {
		assertTrue("Exception status not correct for creating a cu that already exists", jme.getStatus().getCode() == IJavaModelStatusConstants.NAME_COLLISION);
	}
	// should fail if we try again
	try {
		pkg.createCompilationUnit("Default.java", "public class Default {}", true, null);
		return;
	} catch (JavaModelException jme) {
	}
	assertTrue("Creation should not fail if the compilation unit already exists", false);
}
/**
 * Ensures that a default compilation unit is created for a type if
 * it does not yet exist.
 */
public void testEmptyCU() {
	IPackageFragment pkg = getPackage("/P/p");
	// should fail if we try again
	try {
		pkg.createCompilationUnit("Empty.java", "", true, null);
	} catch (JavaModelException jme) {
	}
	ICompilationUnit cu= pkg.getCompilationUnit("Empty.java");
	assertCreation(cu);
}
/*
 * Ensures that a compilation unit can be created even if a file already exists on the file system.
 * (regression test for bug 41611 CreateCompilationUnitOperation.executeOperation() should probably force creation more agressively)
 */
public void testForce() throws JavaModelException, IOException {
	IPackageFragment pkg = getPackage("/P/p");
	File folder = pkg.getResource().getLocation().toFile();
	new File(folder, "X.java").createNewFile();
	ICompilationUnit cu = pkg.createCompilationUnit(
		"X.java",
		"package p;\n" +
		"public class X {\n" +
		"}",
		true, // force,
		null);
	assertCreation(cu);
	assertDeltas(
		"Unexpected delta",
		"P[*]: {CHILDREN}\n" +
		"	<project root>[*]: {CHILDREN}\n" +
		"		p[*]: {CHILDREN}\n" +
		"			X.java[+]: {}"
	);
}
/**
 * Ensures that a compilation unit cannot be created with an invalid name
 * in a package.
 */
public void testInvalidName() {
	IPackageFragment pkg = getPackage("/P/p");
	try {
		pkg.createCompilationUnit("HelloWorld.j", null,  false, null);
	} catch (JavaModelException jme) {
		assertTrue("Incorrect JavaModelException thrown for creating a cu with invalid name", jme.getStatus().getCode() == IJavaModelStatusConstants.INVALID_NAME);
		try {
			pkg.createCompilationUnit(null, null,  false,null);
		} catch (JavaModelException jme2) {
			assertTrue("Incorrect JavaModelException thrown for creating a cu with invalid name", jme2.getStatus().getCode() == IJavaModelStatusConstants.INVALID_NAME);
			return;
		}
	}
	assertTrue("No JavaModelException thrown for creating a cu with an invalid name", false);
}
/**
 * Ensures that a compilation unit cannot be created with <code>null</code> source
 * in a package.
 */
public void testNullContents() {
	IPackageFragment pkg = getPackage("/P/p");
	try {
		pkg.createCompilationUnit("HelloWorld.java", null, false, null);
	} catch (JavaModelException jme) {
		assertTrue("Incorrect JavaModelException thrown for creating a cu with null contents: " + jme, jme.getStatus().getCode() == IJavaModelStatusConstants.INVALID_CONTENTS);
		return;
	}
	assertTrue("No JavaModelException thrown for creating a cu with null contents", false);
}
/**
 * Ensures that a compilation unit can be created with specified source
 * in a package.
 * Verifies that the proper change deltas are generated as a side effect
 * of running the operation.
 */
public void testSimpleCreation() throws JavaModelException {
	IPackageFragment pkg = getPackage("/P/p");
	ICompilationUnit cu= pkg.createCompilationUnit("HelloWorld.java",
		("package p;\n" +
		"\n" +
		"public class HelloWorld {\n" +
		"\n" +
		"	public static main(String[] args) {\n" +
		"		System.out.println(\"HelloWorld\");\n" +
		"	}\n" +
		"}\n"), false, null);
	assertCreation(cu);
	assertDeltas(
		"Unexpected delta",
		"P[*]: {CHILDREN}\n" +
		"	<project root>[*]: {CHILDREN}\n" +
		"		p[*]: {CHILDREN}\n" +
		"			HelloWorld.java[+]: {}"
	);
}
/*
 * Ensures that the correct scheduling rule is used when running createCompilationUnit
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=233270 )
 */
public void testSchedulingRule() throws Exception {
	IWorkspaceRunnable runnable = new IWorkspaceRunnable(){
		public void run(IProgressMonitor monitor) throws CoreException {
			IPackageFragment pkg = getPackage("/P/p");
			ICompilationUnit cu= pkg.createCompilationUnit("HelloWorld.java",
				("package p;\n" +
				"\n" +
				"public class HelloWorld {\n" +
				"}\n"), false, null);
			assertCreation(cu);
		}
	};
	getWorkspace().run(runnable, getFolder("/P/p"), IResource.NONE, null);
}
}
