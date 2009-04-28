/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.io.IOException;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;

public class HierarchyOnWorkingCopiesTests extends WorkingCopyTests {

public HierarchyOnWorkingCopiesTests(String name) {
	super(name);
}

public static Test suite() {
	return buildModelTestSuite(HierarchyOnWorkingCopiesTests.class);
	/* NOTE: cannot use 'new Suite(HierarchyOnWorkingCopiesTests.class)' as this would include tests from super class
	TestSuite suite = new Suite(HierarchyOnWorkingCopiesTests.class.getName());

	suite.addTest(new HierarchyOnWorkingCopiesTests("testSimpleSuperTypeHierarchy"));
	suite.addTest(new HierarchyOnWorkingCopiesTests("testSimpleSubTypeHierarchy"));

	return suite;
	*/
}
/**
 */
public void testSimpleSubTypeHierarchy() throws CoreException {
	String newContents =
		"package x.y;\n" +
		"public class A extends B {\n" +
		"}";
	this.copy.getBuffer().setContents(newContents);
	this.copy.reconcile(ICompilationUnit.NO_AST, false, null, null);

	IFile file = null;
	try {
		file = this.createFile(
			"P/src/x/y/B.java",
			"package x.y;\n" +
			"public class B {\n" +
			"}");

		IType type = this.getCompilationUnit("P/src/x/y/B.java").getType("B");
		ITypeHierarchy h = type.newTypeHierarchy(new ICompilationUnit[] {this.copy}, null);

		assertHierarchyEquals(
			"Focus: B [in B.java [in x.y [in src [in P]]]]\n" +
			"Super types:\n" +
			"  Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
			"Sub types:\n" +
			"  A [in [Working copy] A.java [in x.y [in src [in P]]]]\n",
			h);
	} finally {
		if (file != null) {
			this.deleteResource(file);
		}
	}
}
/**
 */
public void testSimpleSuperTypeHierarchy() throws CoreException {
	String newContents =
		"package x.y;\n" +
		"public class A {\n" +
		"}\n"  +
		"class B {\n" +
		"}";
	this.copy.getBuffer().setContents(newContents);
	this.copy.reconcile(ICompilationUnit.NO_AST, false, null, null);

	IFile file = null;
	try {
		file = this.createFile(
			"P/src/x/y/C.java",
			"package x.y;\n" +
			"public class C extends B {\n" +
			"}");

		IType type = this.getCompilationUnit("P/src/x/y/C.java").getType("C");
		ITypeHierarchy h = type.newSupertypeHierarchy(new ICompilationUnit[] {this.copy}, null);

		assertHierarchyEquals(
			"Focus: C [in C.java [in x.y [in src [in P]]]]\n" +
			"Super types:\n" +
			"  B [in [Working copy] A.java [in x.y [in src [in P]]]]\n" +
			"    Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
			"Sub types:\n",
			h);
	} finally {
		if (file != null) {
			this.deleteResource(file);
		}
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=228845
//make sure uncommitted changes to primary working copy shows up in hierarchy 
public void test228845() throws CoreException {
	String newContents =
		"package x.y;\n" +
		"public class A extends B {\n" +
		"}";
	
	ICompilationUnit primaryCu = this.copy.getPrimary();
	primaryCu.becomeWorkingCopy(null);
	
	primaryCu.getBuffer().setContents(newContents);
	primaryCu.reconcile(ICompilationUnit.NO_AST, false, null, null);
			
	IFile file = null;
	try {
		file = this.createFile(
			"P/src/x/y/B.java",
			"package x.y;\n" +
			"public class B {\n" +
			"}");

		IType type = this.getCompilationUnit("P/src/x/y/B.java").getType("B");
		ITypeHierarchy h = type.newTypeHierarchy(null);  // no working copies explicitly passed, should still honor primary working copies.

		assertHierarchyEquals(
			"Focus: B [in B.java [in x.y [in src [in P]]]]\n" +
			"Super types:\n" +
			"  Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
			"Sub types:\n" +
			"  A [in [Working copy] A.java [in x.y [in src [in P]]]]\n",
			h);
	} finally {
		primaryCu.discardWorkingCopy();
		if (file != null) {
			this.deleteResource(file);
		}
	}
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=228845
//make sure uncommitted changes to primary working copy shows up in hierarchy
//created out of a BinaryType.
public void test228845b() throws CoreException, IOException {
	
	addLibrary(getJavaProject("P"), "myLib.jar", "myLibsrc.zip", new String[] {
			"my/pkg/X.java",
			"package my.pkg;\n" +
			"public class X {\n" +
			"}",
			"my/pkg/Y.java",
			"package my.pkg;\n" +
			"public class Y {\n" +
			"  }\n",
		}, JavaCore.VERSION_1_4);
	
	
	IFile file = null;
	ICompilationUnit primaryCu = null;
	
	try {
		file = this.createFile(
			"P/src/Q.java",
			"public class Q {} \n");

		primaryCu = this.getCompilationUnit("P/src/Q.java").getWorkingCopy(null).getPrimary();
		primaryCu.becomeWorkingCopy(null);
		
		String newContents =
		"public class Q extends my.pkg.X {\n" +
		"}";
		
		primaryCu.getBuffer().setContents(newContents);
		primaryCu.reconcile(ICompilationUnit.NO_AST, false, null, null);
		
		IClassFile cf = getClassFile("P", "myLib.jar", "my.pkg", "X.class");
		IType typ = cf.getType();
	
		ITypeHierarchy h = typ.newTypeHierarchy(null);	
		
		assertHierarchyEquals(
			"Focus: X [in X.class [in my.pkg [in myLib.jar [in P]]]]\n" + 
			"Super types:\n" +
			"  Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
			"Sub types:\n" +
			"  Q [in [Working copy] Q.java [in <default> [in src [in P]]]]\n",
			h);
	} finally {
		if (primaryCu != null) {
			primaryCu.discardWorkingCopy();
		}
		if (file!= null) {
			this.deleteResource(file);
		}
	}
}

}
