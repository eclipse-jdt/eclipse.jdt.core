/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
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
 * Field occurences in 2 working copies within 2 projects (one prereq this other one).
 * (regression test for bug 41534 incorrect shadowing reported by rename [refactoring])
 */
public void testFieldOccurencesInWorkingCopies() throws CoreException {
	ICompilationUnit wc1 = null, wc2 = null;
	try {
		// setup project P1
		IJavaProject p1 = createJavaProject("P1");
		createFolder("/P1/p1");
		createFile(
			"/P1/p1/X.java",
			"package p1;\n" +
			"public class X {\n" +
			"    public static int FOO;\n" +
			"}"
		);
		
		// setup project P2
		IJavaProject p2 = createJavaProject("P2", new String[] {""}, new String[] {"JCL_LIB"}, new String[] {"/P1"}, "");
		createFolder("/P2/p2");
		createFile(
			"/P2/p2/Y.java",
			"package p2;\n" +
			"import p1.X;\n" +
			"public class Y {\n" +
			"    int bar() {\n" +
			"      return X.FOO;\n" +
			"}"
		);
		
		// create working copies and rename X.FOO to X.BAR in these working copies
		wc1 = getCompilationUnit("P1/p1/X.java").getWorkingCopy(null);
		wc1.getBuffer().setContents(
			"package p1;\n" +
			"public class X {\n" +
			"    public static int BAR;\n" +
			"}"
		);
		wc1.reconcile();
		wc2 = getCompilationUnit("P2/p2/Y.java").getWorkingCopy(null);
		wc2.getBuffer().setContents(
			"package p2;\n" +
			"import p1.X;\n" +
			"public class Y {\n" +
			"    int bar() {\n" +
			"      return X.BAR;\n" +
			"}"
		);
		
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {p1, p2});
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		resultCollector.showProject = true;
		IField field = wc1.getType("X").getField("BAR");
		new SearchEngine(new IWorkingCopy[] {wc1, wc2}).search(
			getWorkspace(), 
			field,
			ALL_OCCURRENCES, 
			scope, 
			resultCollector);
		assertEquals(
			"Unexpected occurences of fiel p1.X.BAR",
			"p1/X.java [in P1] p1.X.BAR [BAR]\n" +
			"p2/Y.java [in P2] int p2.Y.bar() [BAR]", 
			resultCollector.toString());
	} finally {
		if (wc1 != null) {
			wc1.destroy();
		}
		if (wc2 != null) {
			wc2.destroy();
		}
		deleteProject("P1");
		deleteProject("P2");
	}
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
			"	protected void foo() {\n" +
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
		assertSearchResults(
			"p/X.java [in P1] void p.X.bar() [foo()]", 
			resultCollector);
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
			"	protected void foo() {\n" +
			"	}\n" +
			"}" 
		);
		createFile(
			"/P2/Z.java",
			"public class Z extends Y {\n" +
			"	protected void foo() {\n" +
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
		assertSearchResults(
			"p/X.java [in P1] void p.X.bar() [foo()]", 
			resultCollector);
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}
/**
 * Search for references in a hierarchy should find matches in super type.
 * (regression test for bug 35755 Search in hierarchy misses dependent projects )
 */
public void testHierarchyScope3() throws CoreException {
	try {
		createJavaProject("P1");
		createFolder("/P1/p");
		createFile(
			"/P1/p/X.java",
			"package p;\n" +
			"public class X {\n" +
			"	protected void foo() {\n" +
			"	}\n" +
			"}" 
		);
		createJavaProject("P2", new String[] {""}, new String[] {"JCL_LIB"}, new String[] {"/P1"}, "");
		createFolder("/P2/q");
		createFile(
			"/P2/q/Y.java",
			"package q;\n" +
			"import p.X;\n" +
			"public class Y extends X {\n" +
			"	void bar() {\n" +
			"		foo();\n" +
			"	}\n" +
			"}" 
		);

		ICompilationUnit cu = getCompilationUnit("/P1/p/X.java");
		IType type = cu.getType("X");
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
		assertSearchResults(
			"q/Y.java [in P2] void q.Y.bar() [foo()]", 
			resultCollector);
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}
/**
 * Search for references in a hierarchy should not find inaccurate match if reference is indirect.
 * (regression test for bug 35755 Search in hierarchy misses dependent projects )
 */
public void testHierarchyScope4() throws CoreException {
	try {
		createJavaProject("P0");
		createFolder("/P0/p0");
		createFile(
			"/P0/p0/X.java",
			"package p0;\n" +
			"public class X {\n" +
			"  public static X TheX;\n" +
			"	public void foo() {\n" +
			"	}\n" +
			"}" 
		);
		createJavaProject("P1", new String[] {""}, new String[] {"JCL_LIB"}, new String[] {"/P0"}, "");
		createFolder("/P1/p1");
		createFile(
			"/P1/p1/T.java",
			"package p1;\n" +
			"import p0.X;\n" +
			"public class T {\n" +
			"	public X zork() {\n" +
			"		return X.TheX;\n" +
			"	}\n" +
			"}" 
		);
		createJavaProject("P2", new String[] {""}, new String[] {"JCL_LIB"}, new String[] {"/P0", "/P1"}, "");
		createFolder("/P2/p2");
		createFile(
			"/P2/p2/Y.java",
			"package p2;\n" +
			"import p0.X;\n" +
			"import p1.T;\n" +
			"public class Y extends X {\n" +
			"	public void bar() {\n" +
			"		new T().zork().foo();\n" +
			"	}\n" +
			"}" 
		);
		createJavaProject("P3", new String[] {""}, new String[] {"JCL_LIB"}, new String[] {"/P0", "/P2"}, "");
		createFolder("/P3/p3");
		createFile(
			"/P3/p3/Z.java",
			"package p3;\n" +
			"import p0.X;\n" +
			"import p2.Y;\n" +
			"public class Z extends Y {\n" +
			"	static {\n" +
			"		X.TheX = new Z(); // zork() will actually answer an instance of Z\n" +
			"	}\n" +
			"	public void foo() {\n" +
			"	} // refs should find one in Y.bar()\n" +
			"}" 
		);

		ICompilationUnit cu = getCompilationUnit("/P3/p3/Z.java");
		IType type = cu.getType("Z");
		IMethod method = type.getMethod("foo", new String[] {});
		IJavaSearchScope scope = SearchEngine.createHierarchyScope(type);
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		resultCollector.showAccuracy = true;
		resultCollector.showProject = true;
		new SearchEngine().search(
			getWorkspace(), 
			method, 
			REFERENCES, 
			scope, 
			resultCollector);
		assertSearchResults(
			"p2/Y.java [in P2] void p2.Y.bar() [foo()] EXACT_MATCH", 
			resultCollector);
	} finally {
		deleteProjects(new String[] {"P0", "P1", "P2", "P3"});
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
		assertSearchResults(
			"Unexpected occurences of method p.I.method(Object)",
			"p/C.java [in P1] void p.C.method(Object) [method]\n" +
			"p/I.java [in P1] void p.I.method(Object) [method]", 
			resultCollector);
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}
/**
 * Package declaration with 2 unrelated projects that contain the same source.
 * (regression test for bug 46276 Search for package declarations incorrectly finds matches in clone project)
 */
public void testPackageDeclaration() throws CoreException {
	try {
		// setup project P1
		IJavaProject p1 = createJavaProject("P1");
		createFolder("/P1/p");
		createFile(
			"/P1/p/X.java",
			"package p;\n" +
			"public class X {\n" +
			"}"
		);
		
		// copy to project P2
		p1.getProject().copy(new Path("/P2"), false, null);
		IJavaProject p2 = getJavaProject("P2");
		
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {p1, p2});
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		resultCollector.showProject = true;
		IPackageFragment pkg = getPackage("/P1/p");
		new SearchEngine().search(
			getWorkspace(), 
			pkg,
			DECLARATIONS, 
			scope, 
			resultCollector);
		assertSearchResults(
			"Unexpected package declarations",
			"p [in P1] p",
			resultCollector);
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
		assertSearchResults(
			"Unexpected result in scope of P1",
			getExternalJCLPathString() + " [in P1] java.lang.Object", 
			resultCollector);
			
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
		assertSearchResults(
			"Unexpected result in scope of P2",
			getExternalJCLPathString() + " [in P2] java.lang.Object", 
			resultCollector);
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}
}
