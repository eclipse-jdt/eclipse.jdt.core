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

import java.io.IOException;

import junit.framework.Test;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.core.tests.model.AbstractJavaSearchTests.JavaSearchResultCollector;
import org.eclipse.jdt.core.tests.model.AbstractJavaSearchTests.TypeNameMatchCollector;
import org.eclipse.jdt.internal.core.search.matching.PatternLocator;

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

@Override
protected void setUp() throws Exception {
	this.indexDisabledForTest = false;
	super.setUp();
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
		resultCollector.showProject();
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
		resultCollector.showProject();
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
		resultCollector.showProject();
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
		resultCollector.showProject();
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
		resultCollector.showAccuracy(true);
		resultCollector.showProject();
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
		resultCollector.showProject();
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
		resultCollector.showProject();
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
		resultCollector.showProject();
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
		resultCollector.showProject();
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
		resultCollector.showProject();
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
		resultCollector.showProject();
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
		resultCollector.showProject();
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
 * bug 163072: [search] method reference reports wrong potential matches
 * test Ensure that there's no potential match while searching in two projects having 1.4 and 1.5 compliances
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
		resultCollector.showProject();
		resultCollector.showAccuracy(true);
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
 * bug 167743: [search] Open Type Dialog cannot find types from projects migrated from 3.2.1 workspace
 * test Ensure that types are found even in default package fragment root
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
			@Override
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

/**
 * bug 176831: [search] No search results due to malformed search scope
 * test Verify that type are found in rt.jar even if it's added as a library on the classpath
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=176831"
 */
public void testBug176831() throws CoreException {
	try {
		// Create projects and files
		final IJavaProject p1 = createJavaProject("P1", new String[] {"src"}, null, new String[] {"/P2"}, "bin");
		final IJavaProject p2 = createJavaProject("P2", new String[] {"src"}, new String[] { getExternalJCLPathString() }, "bin");

		// Create scope and search
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { p1, p2 }, IJavaSearchScope.SOURCES | IJavaSearchScope.APPLICATION_LIBRARIES | IJavaSearchScope.REFERENCED_PROJECTS);
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		resultCollector.showProject();
		resultCollector.showAccuracy(true);
		new SearchEngine().search(
			SearchPattern.createPattern("toString", IJavaSearchConstants.METHOD, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH),
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			scope,
			resultCollector,
			null
		);
		assertSearchResults(
			"Unexpected references to /P1/p/A.java",
			""+ getExternalJCLPathString() + " [in P2] java.lang.String java.lang.Object.toString() EXACT_MATCH",
			resultCollector);
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}
public void testBug176831b() throws CoreException {
	try {
		// Create projects and files
		final IJavaProject p1 = createJavaProject("P1", new String[] {"src"}, null, new String[] {"/P2"}, "bin");
		final IJavaProject p2 = createJavaProject("P2", new String[] {"src"}, null, new String[] {"/P3"}, "bin");
		final IJavaProject p3 = createJavaProject("P3", new String[] {"src"}, new String[] { getExternalJCLPathString() }, "bin");

		// Create scope and search
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { p1, p2, p3 }, IJavaSearchScope.SOURCES | IJavaSearchScope.APPLICATION_LIBRARIES | IJavaSearchScope.REFERENCED_PROJECTS);
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		resultCollector.showProject();
		resultCollector.showAccuracy(true);
		new SearchEngine().search(
			SearchPattern.createPattern("toString", IJavaSearchConstants.METHOD, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH),
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			scope,
			resultCollector,
			null
		);
		assertSearchResults(
			"Unexpected references to /P1/p/A.java",
			""+ getExternalJCLPathString() + " [in P3] java.lang.String java.lang.Object.toString() EXACT_MATCH",
			resultCollector);
	} finally {
		deleteProject("P1");
		deleteProject("P2");
		deleteProject("P3");
	}
}

/**
 * bug 195228: [search] Invalid path in open type dialog
 * test Verify that correct types are found even with project and source folders in the classpath
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=195228"
 */
public void testBug195228() throws CoreException {
	try {
		// Create projects and files
		final IJavaProject project = createJavaProject("P1", new String[] {"src"}, "bin");
		createFolder("/P1/src/pack1/pack2");
		createFile(
			"/P1/src/pack1/pack2/X.java",
			"package pack1.pack2;\n" +
			"public class X {}"
		);
		createFile(
			"/P1/src/pack1/Y.java",
			"package pack1;\n" +
			"public class Y {}"
		);
		createFile(
			"/P1/test.properties",
			"bug=195228"
		);
		// Create additional projects to force the rehash of the workspace scope while creating it
		createJavaProject("P2", new String[] {"src"}, "bin");
		createJavaProject("P3", new String[] {"src"}, "bin");
		IJavaSearchScope scope = SearchEngine.createWorkspaceScope();

		// Store all types found in project
		TypeNameMatchCollector requestor = new TypeNameMatchCollector();
		new SearchEngine().searchAllTypeNames(
			null,
			SearchPattern.R_EXACT_MATCH,
			null,
			SearchPattern.R_PREFIX_MATCH,
			IJavaSearchConstants.TYPE,
			scope,
			requestor,
			IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
			null);
		String allTypes = requestor.toString();

		// Add project folder to classpath with inclusion and exclusion patterns
		getWorkspace().run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				IClasspathEntry[] entries = project.getRawClasspath();
				int length = entries.length;
				System.arraycopy(entries, 0, entries = new IClasspathEntry[length+1], 0, length);
				entries[length] = JavaCore.newSourceEntry(new Path("/P1"), new IPath[] { new Path("test.properties") }, new IPath[] { new Path("src/") }, null);
				project.setRawClasspath(entries, null);
			}
		}, null);

		// Search for all types and verify that same are found
		requestor = new TypeNameMatchCollector();
		new SearchEngine().searchAllTypeNames(
			null,
			SearchPattern.R_EXACT_MATCH,
			null,
			SearchPattern.R_PREFIX_MATCH,
			IJavaSearchConstants.TYPE,
			scope,
			requestor,
			IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
			null);
		assertEquals("Should found same types!", allTypes, requestor.toString());
	} finally {
		deleteProject("P1");
		deleteProject("P2");
		deleteProject("P3");
	}
}

/**
 * bug 199392: [search] Type Dialog Error 'Items filtering ... Reason: Class file name must end with .class'
 * test Ensure that types are found even in project which name ends either with ".jar" or ".zip"
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=199392"
 */
public void testBug199392_Jar() throws CoreException {
	try {
		IJavaProject project = createJavaProject("Test.jar");
		createFolder("/Test.jar/test");
		createFile(
			"/Test.jar/test/MyClass.java",
			"package test;\n" +
			"public class MyClass {\n" +
			"}\n"
		);

		// Search all type names with TypeNameMatchRequestor
		AbstractJavaSearchTests.TypeNameMatchCollector collector = new AbstractJavaSearchTests.TypeNameMatchCollector() {
			@Override
			public String toString(){
				return toFullyQualifiedNamesString();
			}
		};
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project });
		new SearchEngine().searchAllTypeNames(
			null,
			SearchPattern.R_EXACT_MATCH,
			new char[] { 'M', 'y' },
			SearchPattern.R_CAMELCASE_MATCH,
			IJavaSearchConstants.TYPE,
			scope,
			collector,
			IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
			null);
		assertEquals("Found types sounds not to be correct",
			"test.MyClass",
			collector.toString()
		);
	} finally {
		deleteProject("Test.jar");
	}
}
public void testBug199392_Jar_SamePartCount() throws CoreException {
	try {
		IJavaProject project = createJavaProject("Test.jar");
		createFolder("/Test.jar/test");
		createFile(
			"/Test.jar/test/MyClass.java",
			"package test;\n" +
			"public class MyClass {\n" +
			"}\n"
		);

		// Search all type names with TypeNameMatchRequestor
		AbstractJavaSearchTests.TypeNameMatchCollector collector = new AbstractJavaSearchTests.TypeNameMatchCollector() {
			@Override
			public String toString(){
				return toFullyQualifiedNamesString();
			}
		};
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project });
		new SearchEngine().searchAllTypeNames(
			null,
			SearchPattern.R_EXACT_MATCH,
			new char[] { 'M', 'y' },
			SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH,
			IJavaSearchConstants.TYPE,
			scope,
			collector,
			IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
			null);
		assertEquals("Found types sounds not to be correct",
			"",
			collector.toString()
		);
	} finally {
		deleteProject("Test.jar");
	}
}
public void testBug199392_Zip() throws CoreException {
	try {
		IJavaProject project = createJavaProject("Test.zip");
		createFolder("/Test.zip/test");
		createFile(
			"/Test.zip/test/MyClass.java",
			"package test;\n" +
			"public class MyClass {\n" +
			"}\n"
		);

		// Search all type names with TypeNameMatchRequestor
		AbstractJavaSearchTests.TypeNameMatchCollector collector = new AbstractJavaSearchTests.TypeNameMatchCollector() {
			@Override
			public String toString(){
				return toFullyQualifiedNamesString();
			}
		};
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project });
		new SearchEngine().searchAllTypeNames(
			null,
			SearchPattern.R_EXACT_MATCH,
			new char[] { 'M', 'y' },
			SearchPattern.R_CAMELCASE_MATCH,
			IJavaSearchConstants.TYPE,
			scope,
			collector,
			IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
			null);
		assertEquals("Found types sounds not to be correct",
			"test.MyClass",
			collector.toString()
		);
	} finally {
		deleteProject("Test.zip");
	}
}
public void testBug199392_Zip_SamePartCount() throws CoreException {
	try {
		IJavaProject project = createJavaProject("Test.zip");
		createFolder("/Test.zip/test");
		createFile(
			"/Test.zip/test/MyClass.java",
			"package test;\n" +
			"public class MyClass {\n" +
			"}\n"
		);

		// Search all type names with TypeNameMatchRequestor
		AbstractJavaSearchTests.TypeNameMatchCollector collector = new AbstractJavaSearchTests.TypeNameMatchCollector() {
			@Override
			public String toString(){
				return toFullyQualifiedNamesString();
			}
		};
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project });
		new SearchEngine().searchAllTypeNames(
			null,
			SearchPattern.R_EXACT_MATCH,
			new char[] { 'M', 'y' },
			SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH,
			IJavaSearchConstants.TYPE,
			scope,
			collector,
			IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
			null);
		assertEquals("Found types sounds not to be correct",
			"",
			collector.toString()
		);
	} finally {
		deleteProject("Test.zip");
	}
}

/**
 * bug 210689: [search] Import references not found on working copies not written on disk
 * test Ensure that import references are found when searching on working copies not written on disk
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=210689"
 */
public void testBug210689() throws CoreException {
	try {
		// setup project P0
		createJavaProject("P0");
		createFolder("/P0/p");
		createFile(
			"/P0/p/A.java",
			"package p;\n" +
			"public class A {}\n"
		);

		// setup project P1
		createJavaProject("P1");
		createFolder("/P1/p");
		createFile(
			"/P1/p/A.java",
			"package p;\n" +
			"public class A {}\n"
		);

		// setup project P2
		createJavaProject("P2", new String[] {""}, new String[] {"JCL_LIB"}, new String[] { "/P1" }, "");
		createFolder("/P2/p");
		createFile(
			"/P2/p/B.java",
			"package p;\n" +
			"public class B {}\n"
		);
		createFile(
			"/P2/p/Ref.java",
			"package p;\n" +
			"public class Ref {\n" +
			"	A a;\n" +
			"	B b;\n" +
			"}\n"
		);

		// Create OR pattern
		IType typeA0 = getCompilationUnit("/P0/p/A.java").getType("A");
		IType typeA1 = getCompilationUnit("/P1/p/A.java").getType("A");
		SearchPattern rightPattern = SearchPattern.createPattern(typeA0, REFERENCES);
		SearchPattern leftPattern = SearchPattern.createPattern(typeA1, REFERENCES);
		SearchPattern pattern = SearchPattern.createOrPattern(leftPattern, rightPattern);
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		resultCollector.showProject();
		resultCollector.showAccuracy(true);
		new SearchEngine().search(
			pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			SearchEngine.createWorkspaceScope(),
			resultCollector,
			null
		);
		assertSearchResults(
			"Unexpected references to /P1/p/A.java",
			"p/Ref.java [in P2] p.Ref.a [A] EXACT_MATCH",
			resultCollector);
	} finally {
		deleteProject("P0");
		deleteProject("P1");
		deleteProject("P2");
	}
}

/**
 * bug 229128: JDT Search finding matches in working copies that are not part of scope
 * test Ensure that an annotation reference is not found in a working copy if outside the scope
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=229128"
 */
public void testBug229128() throws CoreException {
	ICompilationUnit[] copies = new ICompilationUnit[2];
	try {
		// setup project P1
		createJavaProject("P1", new String[] {""}, new String[] {"JCL15_LIB"}, "", "1.5");
		createFolder("/P1/p");
		createFile(
			"/P1/p/MyAnnot.java",
			"package p;\n" +
			"public @interface MyAnnot {\n" +
			"}\n"
		);
		copies[0] = getWorkingCopy(
			"/P1/p/X.java",
			"package p;\n" +
			"@MyAnnot\n" +
			"public class X {\n" +
			"}\n",
			null/*default working copy owner*/
		);

		// setup project P2
		IJavaProject p2 = createJavaProject("P2", new String[] {""}, new String[] {"JCL15_LIB"}, new String[] { "/P1" }, "", "1.5");
		createFolder("/P2/q");
		copies[1] = getWorkingCopy(
			"/P2/q/Y.java",
			"package q;\n" +
			"@p.MyAnnot\n" +
			"public class Y {\n" +
			"}\n",
			null/*default working copy owner*/
		);

		// Get annotation type
		IType type = getCompilationUnit("/P1/p/MyAnnot.java").getType("MyAnnot");

		// search annotation type reference in P2 scope
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {p2}, false/*don't include referenced projects*/);
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		resultCollector.showProject();
		resultCollector.showAccuracy(true);
		search(type, ANNOTATION_TYPE_REFERENCE, scope, resultCollector);
		assertSearchResults(
			"Unexpected references of annotation type MyAnnot",
			"q/Y.java [in P2] q.Y [p.MyAnnot] EXACT_MATCH",
			resultCollector);
	} finally {
		discardWorkingCopies(copies);
		deleteProject("P1");
		deleteProject("P2");
	}
}

/**
 * bug 229951: StackOverflowError during JavaSearchScope.add for large workspace
 * test Ensure that no StackOverFlowError occurs when searching in a project referencing a cycle
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=229951"
 */
public void testBug229951a() throws Exception {
	try {
		createJavaProject("P1");
		createFile("/P1/test.jar", "");
		editFile(
			"/P1/.classpath",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"	<classpathentry kind=\"lib\" path=\"test.jar\"/>\n" +
			"	<classpathentry exported=\"true\" kind=\"src\" path=\"/P2\"/>\n" +
			"	<classpathentry kind=\"output\" path=\"bin\"/>\n" +
			"</classpath>"
		);
		createJavaProject("P2");
		createFile("/P2/test.jar", "");
		editFile(
			"/P2/.classpath",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"	<classpathentry kind=\"lib\" path=\"test.jar\"/>\n" +
			"	<classpathentry exported=\"true\" kind=\"src\" path=\"/P1\"/>\n" +
			"	<classpathentry kind=\"output\" path=\"bin\"/>\n" +
			"</classpath>"
		);
		createJavaProject("P3", new String[] {""}, new String[] {"JCL_LIB"}, new String[] {"/P2"}, "");
		createFile(
			"/P3/X229951.java",
			"public class X229951 {\n" +
			"}"
		);
		IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search("X229951", TYPE, DECLARATIONS, scope, resultCollector);
		assertSearchResults(
			"Unexpected references of annotation type MyAnnot",
			"X229951.java X229951 [X229951]",
			resultCollector);
	} finally {
		deleteProjects(new String[] {"P1", "P2", "P3"});
	}
}

/**
 * bug 229951: StackOverflowError during JavaSearchScope.add for large workspace
 * test Ensure that no StackOverFlowError occurs when creating a search scope on a project referencing a cycle
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=229951"
 */
public void testBug229951b() throws Exception {
	try {
		createJavaProject("P1");
		createFile("/P1/test.jar", "");
		editFile(
			"/P1/.classpath",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"	<classpathentry kind=\"lib\" path=\"test.jar\"/>\n" +
			"	<classpathentry exported=\"true\" kind=\"src\" path=\"/P2\"/>\n" +
			"	<classpathentry kind=\"output\" path=\"bin\"/>\n" +
			"</classpath>"
		);
		createJavaProject("P2");
		createFile("/P2/test.jar", "");
		editFile(
			"/P2/.classpath",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"	<classpathentry kind=\"lib\" path=\"test.jar\"/>\n" +
			"	<classpathentry exported=\"true\" kind=\"src\" path=\"/P1\"/>\n" +
			"	<classpathentry kind=\"output\" path=\"bin\"/>\n" +
			"</classpath>"
		);
		IJavaProject p3 = createJavaProject("P3", new String[] {""}, new String[] {"JCL_LIB"}, new String[] {"/P2"}, "");
		createFile(
			"/P3/X229951.java",
			"public class X229951 {\n" +
			"}"
		);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {p3});
		assertScopeEquals(
			"JavaSearchScope on [\n" +
			"	/P3\n" +
			"	" + getExternalJCLPathString() + "\n" +
			"]",
			scope);
	} finally {
		deleteProjects(new String[] {"P1", "P2", "P3"});
	}
}

/**
 * bug 250454: [search] Cannot find method references between projects
 * test Ensure that search does not find illegal references with given projects setup
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=250454"
 */
public void testBug250454() throws CoreException {
	try {
		// setup project P0
		createJavaProject("P0");
		createFolder("/P0/p");
		createFile(
			"/P0/p/Shape.java",
			"package p;\n" +
			"public interface Shape {\n" +
			"	public void f();\n" +
			"}\n"
		);

		// setup project P1
		createJavaProject("P1", new String[] {""}, new String[] {"JCL_LIB"}, new String[] { "/P0" }, "");
		createFolder("/P1/p");
		createFile(
			"/P1/p/Square.java",
			"package p;\n" +
			"public class Square implements Shape {\n" +
			"	public void f() {}\n" +
			"}\n"
		);

		// setup project P2
		createJavaProject("P2", new String[] {""}, new String[] {"JCL_LIB"}, new String[] { "/P0" }, "");
		createFolder("/P2/p");
		createFile(
			"/P2/p/ShapeUser.java",
			"package p;\n" +
			"public class ShapeUser {\n" +
			"	public void useShape(Shape p_shape) {\n" +
			"		p_shape.f();\n" +
			"	}\n"
		);

		// Perform search
		IType type = getCompilationUnit("/P1/p/Square.java").getType("Square");
		IMethod method = type.getMethod("f", new String[0]);
		SearchPattern pattern = SearchPattern.createPattern(method, REFERENCES);
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		resultCollector.showProject();
		resultCollector.showAccuracy(true);
		resultCollector.showFlavors = PatternLocator.SUPER_INVOCATION_FLAVOR;
		new SearchEngine().search(
			pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			SearchEngine.createWorkspaceScope(),
			resultCollector,
			null
		);
		assertSearchResults(
			"Unexpected references to "+method,
			"p/ShapeUser.java [in P2] void p.ShapeUser.useShape(Shape) [f()] EXACT_MATCH SUPER INVOCATION",
			resultCollector);
	} finally {
		deleteProject("P0");
		deleteProject("P1");
		deleteProject("P2");
	}
}
public void testBug250454_jars() throws CoreException, IOException {
	String jarPath = getExternalPath()+"b250454.jar";
	try {
		// setup jar
		String[] pathsAndContents= new String[] {
			"p/Shape.java",
			"package p;\n" +
			"public interface Shape {\n" +
			"	public void f();\n" +
			"}\n"
		};
		createJar(pathsAndContents, jarPath);

		// setup project P1
		createJavaProject("P1", new String[] {""}, new String[] {"JCL_LIB", jarPath}, "");
		createFolder("/P1/p");
		createFile(
			"/P1/p/Square.java",
			"package p;\n" +
			"public class Square implements Shape {\n" +
			"	public void f() {}\n" +
			"}\n"
		);

		// setup project P2
		createJavaProject("P2", new String[] {""}, new String[] {"JCL_LIB", jarPath}, "");
		createFolder("/P2/p");
		createFile(
			"/P2/p/ShapeUser.java",
			"package p;\n" +
			"public class ShapeUser {\n" +
			"	public void useShape(Shape p_shape) {\n" +
			"		p_shape.f();\n" +
			"	}\n"
		);

		// Perform search
		IType type = getCompilationUnit("/P1/p/Square.java").getType("Square");
		IMethod method = type.getMethod("f", new String[0]);
		SearchPattern pattern = SearchPattern.createPattern(method, REFERENCES);
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		resultCollector.showProject();
		resultCollector.showAccuracy(true);
		resultCollector.showFlavors = PatternLocator.SUPER_INVOCATION_FLAVOR;
		new SearchEngine().search(
			pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			SearchEngine.createWorkspaceScope(),
			resultCollector,
			null
		);
		assertSearchResults(
			"Unexpected references to "+method,
			"p/ShapeUser.java [in P2] void p.ShapeUser.useShape(Shape) [f()] EXACT_MATCH SUPER INVOCATION",
			resultCollector);
	} finally {
		deleteExternalFile(jarPath);
		deleteProject("P1");
		deleteProject("P2");
	}
}
}
