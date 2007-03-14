/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.core.tests.model.AbstractJavaSearchTests.JavaSearchResultCollector;

/**
 * Tests the Java search engine accross multiple projects.
 */
public class JavaSearchMultipleProjectsTests extends ModifyingResourceTests implements IJavaSearchConstants {
	private final static int UI_DECLARATIONS = DECLARATIONS|IGNORE_DECLARING_TYPE|IGNORE_RETURN_TYPE;
public JavaSearchMultipleProjectsTests(String name) {
	super(name);
}
public static Test suite() {
	return buildModelTestSuite(JavaSearchMultipleProjectsTests.class);
}
// Use this static initializer to specify subset for tests
// All specified tests which do not belong to the class are skipped...
static {
//	TESTS_NAMES = new String[] { "testJavaSearchScopeBug101426" };
//	TESTS_NUMBERS = new int[] { 101426 };
//	TESTS_RANGE = new int[] { 16, -1 };
//	TESTS_PREFIX = "testScopeEncloses";
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
		wc1.reconcile(ICompilationUnit.NO_AST, false, null, null);
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
		SearchPattern pattern = SearchPattern.createPattern(field, ALL_OCCURRENCES);
		new SearchEngine(new ICompilationUnit[] {wc1, wc2}).search(
			pattern, 
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			scope, 
			resultCollector,
			null);
		assertEquals(
			"Unexpected occurences of field p1.X.BAR",
			"p1/X.java [in P1] p1.X.BAR [BAR]\n" +
			"p2/Y.java [in P2] int p2.Y.bar() [BAR]", 
			resultCollector.toString());
	} finally {
		if (wc1 != null) {
			wc1.discardWorkingCopy();
		}
		if (wc2 != null) {
			wc2.discardWorkingCopy();
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
		search(
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
		search(
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
		search(
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
		search(
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
		search(
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
		search(
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
 * Package reference with fragments in 2 different source projects.
 * (regression test for bug 47415 [Search] package references confused with multiple fragments)
 */
public void testPackageReference1() throws CoreException {
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
		
		// setup project P2
		IJavaProject p2 = createJavaProject(
			"P2", 
			new String[] {""}, 
			new String[] {"JCL_LIB"}, 
			new String[] {"/P1"}, 
			"");
		createFolder("/P2/p");
		createFile(
			"/P2/p/Y.java",
			"package p;\n" +
			"public class Y {\n" +
			"}"
		);
		
		// create package references
		createFolder("/P2/q");
		createFile(
			"/P2/q/Z.java",
			"package q;\n" +
			"import p.X;\n" +
			"import p.Y;\n" +
			"public class Z {\n" +
			"  X onlyHereForTheImport = null;\n" +
			"  Y alsoOnlyHereForTheImport = null;\n" +
			"  void foo(){\n" +
			"    p.X x = (p.X)null;\n" +
			"    p.Y y = (p.Y)null;\n" +
			"  }\n" +
			"}"
		);
		
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {p1, p2});
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IPackageFragment pkg = getPackage("/P1/p");
		search(
			pkg, 
			REFERENCES,
			scope, 
			resultCollector);
		assertSearchResults(
			"Unexpected package references",
			"q/Z.java [p]\n" + 
			"q/Z.java void q.Z.foo() [p]\n" + 
			"q/Z.java void q.Z.foo() [p]",
			resultCollector);
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}
/**
 * Package reference with fragments in 2 different binary projects.
 * (regression test for bug 47415 [Search] package references confused with multiple fragments)
 */
public void testPackageReference2() throws CoreException, IOException {
	try {
		// setup project JavaSearchMultipleProjects1
		IJavaProject p1 = setUpJavaProject("JavaSearchMultipleProjects1");
		
		// setup project JavaSearchMultipleProjects2
		IJavaProject p2 = setUpJavaProject("JavaSearchMultipleProjects2");
				
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {p1, p2});
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IPackageFragment pkg = getPackage("/JavaSearchMultipleProjects1/lib/p");
		search(
			pkg, 
			REFERENCES,
			scope, 
			resultCollector);
		assertSearchResults(
			"Unexpected package references",
			"src/q/Z.java [p]\n" + 
			"src/q/Z.java void q.Z.foo() [p]\n" + 
			"src/q/Z.java void q.Z.foo() [p]",
			resultCollector);
	} finally {
		deleteProject("JavaSearchMultipleProjects1");
		deleteProject("JavaSearchMultipleProjects2");
	}
}
/**
 * Method reference with 2 working copies in 2 different project.
 * (regression test for bug 57749 Search in working copies doesn't find all matches)
 */
public void testReferenceInWorkingCopies() throws CoreException {
	ICompilationUnit workingCopy1 = null;
	ICompilationUnit workingCopy2 = null;
	try {
		// setup project P1
		IJavaProject p1 = createJavaProject("P1");
		createFolder("/P1/p1");
		createFile(
			"/P1/p1/X.java",
			"package p1;\n" +
			"public class X {\n" +
			"  void foo() {\n" +
			"  }\n" +
			"}"
		);
		createFile(
			"/P1/p1/Test.java",
			"package p1;\n" +
			"public class Test {\n" +
			"}"
		);
		
		// setup project P2
		IJavaProject p2 = createJavaProject(
			"P2", 
			new String[] {""}, 
			new String[] {"JCL_LIB"}, 
			new String[] {"/P1"}, 
			"");
		createFolder("/P2/p2");
		createFile(
			"/P2/p2/Y.java",
			"package p2;\n" +
			"public class Y {\n" +
			"}"
		);
		// need a second potential match to see the problem
		createFile(
			"/P2/p2/Z.java",
			"public class Z {\n" +
			"  void bar(p1.Test test) {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    bar(null);\n" + // potential match
			"  }\n" +
			"}"
		);
		
		// create working copies
		WorkingCopyOwner owner = new WorkingCopyOwner() {};
		workingCopy1 = getCompilationUnit("/P1/p1/X.java").getWorkingCopy(owner, null/*no progress monitor*/);
		workingCopy1.getBuffer().setContents(
			"package p1;\n" +
			"public class X {\n" +
			"  void bar(Test test) {\n" +
			"  }\n" +
			"}"
		);
		workingCopy1.makeConsistent(null);
		workingCopy2 = getCompilationUnit("/P2/p2/Y.java").getWorkingCopy(owner, null/*no progress monitor*/);
		workingCopy2.getBuffer().setContents(
			"package p2;\n" +
			"import p1.X;\n" +
			"public class Y {\n" +
			"  void fred() {\n" +
			"    new X().bar(null);\n" +
			"  }\n" +
			"}"
		);
		workingCopy2.makeConsistent(null);
		
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {p1, p2});
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		IMethod method = workingCopy1.getType("X").getMethod("bar", new String[] {"QTest;"});
		new SearchEngine(owner).search(
			SearchPattern.createPattern(method, REFERENCES), 
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			scope,
			resultCollector,
			null
		);
		assertSearchResults(
			"Unexpected package references",
			"p2/Y.java void p2.Y.fred() [bar(null)]",
			resultCollector);
	} finally {
		if (workingCopy1 != null) workingCopy1.discardWorkingCopy();
		if (workingCopy2 != null) workingCopy1.discardWorkingCopy();
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
		search(
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
		search(
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

/**
 * Bug 151189: [search] Declaration search does not find all matches
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=151189"
 */
public void testBug151189_Workspace() throws CoreException {
	try {
		// setup project P1
		/*IJavaProject p1 = */createJavaProject("P1");
		createFolder("/P1/pack");
		createFile(
			"/P1/pack/Declaration.java",
			"package pack;\n" + 
			"public class Declaration implements Interface {\n" + 
			"	public void doOperation(int val) {}\n" + 
			"}\n"
		);
		createFile(
			"/P1/pack/Interface.java",
			"package pack;\n" + 
			"public interface Interface {\n" + 
			"	void doOperation(int val);\n" + 
			"}\n"
		);

		// setup project P2
		createJavaProject("P2", new String[] {""}, new String[] {"JCL_LIB"}, new String[] { "/P1" }, "");
		createFolder("/P2/test");
		createFile(
			"/P2/test/Declaration_bis.java",
			"package test;\n" + 
			"import pack.Interface;\n" + 
			"public class Declaration_bis implements Interface {\n" + 
			"	public void doOperation(int val) {}\n" + 
			"}\n"
		);

		// Get method
		IMethod method = getCompilationUnit("/P2/test/Declaration_bis.java").getType("Declaration_bis").getMethod("doOperation", new String[] {"I"});

		// search method declaration in workspace scope
		IJavaSearchScope scope = SearchEngine.createWorkspaceScope(); //JavaSearchScope(new IJavaElement[] {p1, p2});
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		resultCollector.showProject = true;
		search(
			method, 
			DECLARATIONS,
			scope, 
			resultCollector);
		assertSearchResults(
			"Unexpected declarations of method test.Declaration_bis.doOperation(int)",
			"test/Declaration_bis.java [in P2] void test.Declaration_bis.doOperation(int) [doOperation]",
			resultCollector);

		// search method declaration in workspace scope with JDT-UI flags
		resultCollector = new JavaSearchResultCollector();
		resultCollector.showProject = true;
		search(
			method, 
			UI_DECLARATIONS,
			scope, 
			resultCollector);
		assertSearchResults(
			"Unexpected declarations of method test.Declaration_bis.doOperation(int)",
			"pack/Declaration.java [in P1] void pack.Declaration.doOperation(int) [doOperation]\n" + 
			"pack/Interface.java [in P1] void pack.Interface.doOperation(int) [doOperation]\n" + 
			"test/Declaration_bis.java [in P2] void test.Declaration_bis.doOperation(int) [doOperation]",
			resultCollector);
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}
public void testBug151189_Project() throws CoreException {
	try {
		// setup project P1
		createJavaProject("P1");
		createFolder("/P1/pack");
		createFile(
			"/P1/pack/Declaration.java",
			"package pack;\n" + 
			"public class Declaration implements Interface {\n" + 
			"	public void doOperation(int val) {}\n" + 
			"}\n"
		);
		createFile(
			"/P1/pack/Interface.java",
			"package pack;\n" + 
			"public interface Interface {\n" + 
			"	void doOperation(int val);\n" + 
			"}\n"
		);

		// setup project P2
		IJavaProject p2 = createJavaProject("P2", new String[] {""}, new String[] {"JCL_LIB"}, new String[] { "/P1" }, "");
		createFolder("/P2/test");
		createFile(
			"/P2/test/Declaration_bis.java",
			"package test;\n" + 
			"import pack.Interface;\n" + 
			"public class Declaration_bis implements Interface {\n" + 
			"	public void doOperation(int val) {}\n" + 
			"}\n"
		);

		// Get method
		IMethod method = getCompilationUnit("/P2/test/Declaration_bis.java").getType("Declaration_bis").getMethod("doOperation", new String[] {"I"});

		// search method declaration in project scope
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {p2});
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		resultCollector.showProject = true;
		search(
			method, 
			UI_DECLARATIONS,
			scope, 
			resultCollector);
		assertSearchResults(
			"Unexpected declarations of method test.Declaration_bis.doOperation(int)",
			"pack/Declaration.java [in P1] void pack.Declaration.doOperation(int) [doOperation]\n" + 
			"pack/Interface.java [in P1] void pack.Interface.doOperation(int) [doOperation]\n" + 
			"test/Declaration_bis.java [in P2] void test.Declaration_bis.doOperation(int) [doOperation]",
			resultCollector);
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}

/**
 * @bug 163072: [search] method reference reports wrong potential matches
 * @test Ensure that there's no potential match while searching in two projects having 1.4 and 1.5 compliances
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=163072"
 */
public void testBug163072() throws CoreException {
	try {
		// setup project P1
		/*IJavaProject p1 = */createJavaProject("P1"); // standard project using 1.4 compliance
		createFolder("/P1/test");
		createFile(
			"/P1/test/Test.java",
			"package test;\n" + 
			"public class Test {\n" + 
			"	public Object getType() {\n" + 
			"		return null;\n" + 
			"	}\n" + 
			"	public void foo() {\n" + 
			"		if (getType() == null) {\n" + 
			"			System.out.println(\"null\");\n" + 
			"		}\n" + 
			"	}\n" + 
			"}\n"
		);

		// setup project P2
		createJavaProject("P2", new String[] {""}, new String[] {"JCL15_LIB"}, new String[] { "/P1" }, "", "1.5");
		createFolder("/P2/pack");
		createFile(
			"/P2/pack/FactoryContainer.java",
			"package pack;\n" + 
			"public class FactoryContainer {\n" + 
			"	public enum FactoryType { PLUGIN }\n" + 
			"	public FactoryType getType() {\n" + 
			"		return FactoryType.PLUGIN;\n" + 
			"	}\n" + 
			"}\n"
		);
		createFile(
			"/P2/pack/Reference.java",
			"package pack;\n" + 
			"public class Reference {\n" + 
			"	private final FactoryContainer _fc;\n" + 
			"	public Reference() {\n" + 
			"		_fc = new FactoryContainer();\n" + 
			"	}\n" + 
			"	boolean foo() {\n" + 
			"		return _fc.getType() == FactoryContainer.FactoryType.PLUGIN;\n" + 
			"	}\n" + 
			"}\n"
		);

		// Get method
		IMethod method = getCompilationUnit("/P1/test/Test.java").getType("Test").getMethod("getType", new String[0]);
		assertTrue("Method 'Test.getType()' should exist!", method.exists());

		// search method declaration in workspace scope
		IJavaSearchScope scope = SearchEngine.createWorkspaceScope(); //JavaSearchScope(new IJavaElement[] {p1, p2});
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		resultCollector.showProject = true;
		resultCollector.showAccuracy = true;
		search(method, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"Unexpected references of method Test.getType()",
			"test/Test.java [in P1] void test.Test.foo() [getType()] EXACT_MATCH",
			resultCollector);
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}

/**
 * @bug 167743: [search] Open Type Dialog cannot find types from projects migrated from 3.2.1 workspace
 * @test Ensure that types are found even in default package fragment root
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=167743"
 */
public void testBug167743() throws CoreException {
	try {
		IJavaProject p = createJavaProject("P");
		createFolder("/P/test");
		createFile(
			"/P/test/TestClass.java",
			"package test;\n" + 
			"public class Test {\n" + 
			"}\n"
		);

		// Search all type names with TypeNameMatchRequestor
		AbstractJavaSearchTests.TypeNameMatchCollector collector = new AbstractJavaSearchTests.TypeNameMatchCollector() {
			public String toString(){
				return toFullyQualifiedNamesString();
			}
		};
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { p });
		new SearchEngine().searchAllTypeNames(
			null,
			SearchPattern.R_EXACT_MATCH,
			new char[] { '*' },
			SearchPattern.R_PATTERN_MATCH,
			IJavaSearchConstants.TYPE,
			scope,
			collector,
			IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
			null);
		// Search all type names with TypeNameRequestor
		SearchTests.SearchTypeNameRequestor requestor = new SearchTests.SearchTypeNameRequestor();
		new SearchEngine().searchAllTypeNames(
			null,
			SearchPattern.R_EXACT_MATCH,
			new char[] { '*' },
			SearchPattern.R_PATTERN_MATCH,
			IJavaSearchConstants.TYPE,
			scope,
			requestor,
			IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
			null);
		// Should have same types with these 2 searches
		assertEquals("Invalid number of types found!", requestor.size(), collector.size());
		assertEquals("Found types sounds not to be correct", requestor.toString(), collector.toString());
	} finally {
		deleteProject("P");
	}
}
}
