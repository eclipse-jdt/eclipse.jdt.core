/*******************************************************************************
 * Copyright (c) 2003 IBM Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import junit.framework.Test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.tests.model.JavaSearchTests.JavaSearchResultCollector;

/**
 * Tests the Java search engine accross multiple projects.
 */
public class JavaSearchMultipleProjectsTests extends ModifyingResourceTests implements IJavaSearchConstants {
public JavaSearchMultipleProjectsTests(String name) {
	super(name);
}
public static Test suite() {
	return new Suite(JavaSearchMultipleProjectsTests.class);
}
/**
 * Search for references in a hierarchy should find matches in super type.
 * (regression test for bug 31748 [search] search for reference is broken 2.1 M5)
 */
public void testHierarchyScope1() throws CoreException {
	try {
		createJavaProject("P1");
		createFolder("/P1/p");
		createFile(
			"/P1/p/X.java",
			"package p;\n" +
			"public class X {\n" +
			"	protected void foo() {\n" +
			"	}\n" +
			"	void bar() {\n" +
			"		foo();\n" +
			"	}\n" +
			"}" 
		);
		createJavaProject("P2", new String[] {""}, new String[] {"JCL_LIB"}, new String[] {"/P1"}, "");
		createFile(
			"/P2/Y.java",
			"import p.X;\n" +
			"public class Y extends X {\n" +
			"	void foo() {\n" +
			"	}\n" +
			"}" 
		);
		ICompilationUnit cu = getCompilationUnit("/P2/Y.java");
		IType type = cu.getType("Y");
		IMethod method = type.getMethod("foo", new String[] {});
		IJavaSearchScope scope = SearchEngine.createHierarchyScope(type);
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		resultCollector.showProject = true;
		new SearchEngine().search(
			getWorkspace(), 
			method, 
			REFERENCES, 
			scope, 
			resultCollector);
		assertEquals(
			"p/X.java [in P1] p.X.bar() -> void [foo()]", 
			resultCollector.toString());
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}
/**
 * Search for references in a hierarchy should find matches in super type.
 * (regression test for bug 31748 [search] search for reference is broken 2.1 M5)
 */
public void testHierarchyScope2() throws CoreException {
	try {
		createJavaProject("P1");
		createFolder("/P1/p");
		createFile(
			"/P1/p/X.java",
			"package p;\n" +
			"public class X {\n" +
			"	protected void foo() {\n" +
			"	}\n" +
			"	void bar() {\n" +
			"		foo();\n" +
			"	}\n" +
			"}" 
		);
		createJavaProject("P2", new String[] {""}, new String[] {"JCL_LIB"}, new String[] {"/P1"}, "");
		createFile(
			"/P2/Y.java",
			"import p.X;\n" +
			"public class Y extends X {\n" +
			"	void foo() {\n" +
			"	}\n" +
			"}" 
		);
		createFile(
			"/P2/Z.java",
			"public class Z extends Y {\n" +
			"	void foo() {\n" +
			"	}\n" +
			"}" 
		);

		ICompilationUnit cu = getCompilationUnit("/P2/Z.java");
		IType type = cu.getType("Z");
		IMethod method = type.getMethod("foo", new String[] {});
		IJavaSearchScope scope = SearchEngine.createHierarchyScope(type);
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		resultCollector.showProject = true;
		new SearchEngine().search(
			getWorkspace(), 
			method, 
			REFERENCES, 
			scope, 
			resultCollector);
		assertEquals(
			"p/X.java [in P1] p.X.bar() -> void [foo()]", 
			resultCollector.toString());
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}
/**
 * Method occurences with 2 unrelated projects that contain the same source.
 * (regression test for bug 33800 search: reporting too many method occurrences)
 */
public void testMethodOccurences() throws CoreException {
	try {
		// setup project P1
		IJavaProject p1 = createJavaProject("P1");
		createFolder("/P1/p");
		createFile(
			"/P1/p/I.java",
			"package p;\n" +
			"public interface I {\n" +
			"    void method(Object o);\n" +
			"}"
		);
		createFile(
			"/P1/p/C.java",
			"package p;\n" +
			"public class C implements I {\n" +
			"    void method(Object o) {\n" +
			"    }\n" +
			"}"
		);
		
		// copy to project P2
		p1.getProject().copy(new Path("/P2"), false, null);
		IJavaProject p2 = getJavaProject("P2");
		
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {p1, p2});
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		resultCollector.showProject = true;
		IMethod method = getCompilationUnit("/P1/p/I.java").getType("I").getMethod("method", new String[] {"QObject;"});
		new SearchEngine().search(
			getWorkspace(), 
			method,
			ALL_OCCURRENCES, 
			scope, 
			resultCollector);
		assertEquals(
			"Unexpected occurences of method p.I.method(Object)",
			"p/C.java [in P1] p.C.method(Object) -> void [method]\n" +
			"p/I.java [in P1] p.I.method(Object) -> void [method]", 
			resultCollector.toString());
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}
/**
 * Type declaration in external jar file that is shared by 2 projects.
 * (regression test for bug 27485 SearchEngine returns wrong java element when searching in an archive that is included by two distinct java projects.)
 */
public void testTypeDeclarationInJar() throws CoreException {
	try {
		IJavaProject p1 = createJavaProject("P1", new String[] {}, new String[] {"JCL_LIB"}, "");
		IJavaProject p2 = createJavaProject("P2", new String[] {}, new String[] {"JCL_LIB"}, "");
		
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {p1});
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		resultCollector.showProject = true;
		new SearchEngine().search(
			getWorkspace(), 
			"Object",
			TYPE, 
			DECLARATIONS, 
			scope, 
			resultCollector);
		assertEquals(
			"Unexpected result in scope of P1",
			getExternalJCLPathString() + " [in P1] java.lang.Object", 
			resultCollector.toString());
			
		scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {p2});
		resultCollector = new JavaSearchResultCollector();
		resultCollector.showProject = true;
		new SearchEngine().search(
			getWorkspace(), 
			"Object",
			TYPE, 
			DECLARATIONS, 
			scope, 
			resultCollector);
		assertEquals(
			"Unexpected result in scope of P2",
			getExternalJCLPathString() + " [in P2] java.lang.Object", 
			resultCollector.toString());
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}
}
