/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import org.eclipse.jdt.core.*;

import junit.framework.*;

public class ResolveTests2 extends ModifyingResourceTests {

static {
//	TESTS_NAMES = new String[] { "testSecondaryTypes" };
}
public static Test suite() {
	return buildModelTestSuite(ResolveTests2.class);
}

public ResolveTests2(String name) {
	super(name);
}
public void setUpSuite() throws Exception {
	super.setUpSuite();
	
	setUpJavaProject("Resolve");
}
public void tearDownSuite() throws Exception {
	deleteProject("Resolve");
	
	super.tearDownSuite();
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227822
public void testBug227822a() throws Exception {
	try {
		// create P1
		this.createJavaProject(
			"P1",
			new String[]{"src"},
			new String[]{},
			 "bin");
		
		this.createFolder("/P1/src/a");
		this.createFile(
				"/P1/src/a/Test.java",
				"package a;\n"+
				"public class Test {\n" + 
				"  java.lang.Object var;\n"+
				"}");
		
		waitUntilIndexesReady();
		
		// do code select
		ICompilationUnit cu= getCompilationUnit("P1", "src", "a", "Test.java");
		
		String str = cu.getSource();
		
		String selection = "java.lang.Object";
		int start = str.lastIndexOf(selection);
		int length = selection.length();
		IJavaElement[] elements = cu.codeSelect(start, length);
		
		assertElementsEqual(
			"Unexpected elements",
			"",
			elements
		);
	} finally {
		this.deleteProject("P1");
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227822
public void testBug227822b() throws Exception {
	try {
		// create P1
		this.createJavaProject(
			"P1",
			new String[]{"src"},
			new String[]{},
			 "bin");
		
		this.createFolder("/P1/src/a");
		this.createFile(
				"/P1/src/a/Test.java",
				"package a;\n"+
				"public class Test {\n" + 
				"  javaz.lang.Objectz var;\n"+
				"}");
		
		waitUntilIndexesReady();
		
		// do code select
		ICompilationUnit cu= getCompilationUnit("P1", "src", "a", "Test.java");
		
		String str = cu.getSource();
		
		String selection = "javaz.lang.Objectz";
		int start = str.lastIndexOf(selection);
		int length = selection.length();
		IJavaElement[] elements = cu.codeSelect(start, length);
		
		assertElementsEqual(
			"Unexpected elements",
			"",
			elements
		);
	} finally {
		this.deleteProject("P1");
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227822
public void testBug227822c() throws Exception {
	try {
		// create P1
		this.createJavaProject(
			"P1",
			new String[]{"src"},
			new String[]{},
			 "bin");
		
		this.createFolder("/P1/src/a");
		this.createFile(
				"/P1/src/a/Test.java",
				"package a;\n"+
				"public class Test {\n" + 
				"  java var;\n"+
				"}");
		
		waitUntilIndexesReady();
		
		// do code select
		ICompilationUnit cu= getCompilationUnit("P1", "src", "a", "Test.java");
		
		String str = cu.getSource();
		
		String selection = "java";
		int start = str.lastIndexOf(selection);
		int length = selection.length();
		IJavaElement[] elements = cu.codeSelect(start, length);
		
		assertElementsEqual(
			"Unexpected elements",
			"",
			elements
		);
	} finally {
		this.deleteProject("P1");
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227822
public void testBug227822d() throws Exception {
	try {
		// create P1
		this.createJavaProject(
			"P1",
			new String[]{"src"},
			new String[]{},
			 "bin");
		
		this.createFolder("/P1/src/a");
		this.createFile(
				"/P1/src/a/Test.java",
				"package a;\n"+
				"public class Test {\n" + 
				"  javaz var;\n"+
				"}");
		
		waitUntilIndexesReady();
		
		// do code select
		ICompilationUnit cu= getCompilationUnit("P1", "src", "a", "Test.java");
		
		String str = cu.getSource();
		
		String selection = "javaz";
		int start = str.lastIndexOf(selection);
		int length = selection.length();
		IJavaElement[] elements = cu.codeSelect(start, length);
		
		assertElementsEqual(
			"Unexpected elements",
			"",
			elements
		);
	} finally {
		this.deleteProject("P1");
	}
}
}
