/*******************************************************************************
 * Copyright (c) 2002 IBM Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.IWorkingCopy;
import org.eclipse.jdt.core.tests.model.*;

import junit.framework.Test;
import junit.framework.TestSuite;

public class HierarchyOnWorkingCopiesTests extends WorkingCopyTests {

public HierarchyOnWorkingCopiesTests(String name) {
	super(name);
}

public static Test suite() {
	TestSuite suite = new Suite(HierarchyOnWorkingCopiesTests.class.getName());

	suite.addTest(new HierarchyOnWorkingCopiesTests("testSimpleSuperTypeHierarchy"));
	suite.addTest(new HierarchyOnWorkingCopiesTests("testSimpleSubTypeHierarchy"));

	return suite;
}
/**
 */
public void testSimpleSubTypeHierarchy() throws CoreException {
	String newContents =
		"package x.y;\n" +
		"public class A extends B {\n" +
		"}";
	this.copy.getBuffer().setContents(newContents);
	this.copy.reconcile();
	
	IFile file = null;
	try {
		file = this.createFile(
			"P/src/x/y/B.java", 
			"package x.y;\n" +
			"public class B {\n" +
			"}");
	
		IType type = this.getCompilationUnit("P/src/x/y/B.java").getType("B");
		ITypeHierarchy h = type.newTypeHierarchy(new IWorkingCopy[] {this.copy}, null);

		assertEquals(
			"Unexpected hierarchy",
			"Focus: x.y.B\n" +
			"Super types:\n" +
			"  B [in B.java [in x.y [in src [in P]]]]\n" +
			"    Object [in Object.class [in java.lang [in " + this.getExternalJCLPath() + " [in P]]]]\n" +
			"Sub types:\n" +
			"  B [in B.java [in x.y [in src [in P]]]]\n" +
			"    A [in [Working copy] A.java [in x.y [in src [in P]]]]\n",
			h.toString());
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
	this.copy.reconcile();
	
	IFile file = null;
	try {
		file = this.createFile(
			"P/src/x/y/C.java", 
			"package x.y;\n" +
			"public class C extends B {\n" +
			"}");
	
		IType type = this.getCompilationUnit("P/src/x/y/C.java").getType("C");
		ITypeHierarchy h = type.newSupertypeHierarchy(new IWorkingCopy[] {this.copy}, null);

		assertEquals(
			"Unexpected hierarchy",
			"Focus: x.y.C\n" +
			"Super types:\n" +
			"  C [in C.java [in x.y [in src [in P]]]]\n" +
			"    B [in [Working copy] A.java [in x.y [in src [in P]]]]\n" +
			"      Object [in Object.class [in java.lang [in " + this.getExternalJCLPath() + " [in P]]]]\n" +
			"Sub types:\n" +
			"  C [in C.java [in x.y [in src [in P]]]]\n",
			h.toString());
	} finally {
		if (file != null) {
			this.deleteResource(file);
		}
	}
}

}
