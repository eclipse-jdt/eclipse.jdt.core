/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.core.tests.model.JavaSearchTests.JavaSearchResultCollector;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jdt.internal.core.search.indexing.IIndexConstants;
import org.eclipse.jdt.internal.core.search.matching.TypeDeclarationPattern;

/**
 * Tests the Java search engine where results are JavaElements and source positions.
 */
public class JavaSearchBugsTests extends AbstractJavaModelTests implements IJavaSearchConstants {

	public static List TEST_SUITES = null;
	protected static IJavaProject JAVA_PROJECT;

	protected JavaSearchTests.JavaSearchResultCollector resultCollector;

	public JavaSearchBugsTests(String name) {
		super(name, 3);
		this.displayName = true;
	}
	public static Test suite() {
		return buildTestSuite(JavaSearchBugsTests.class);
	}
	// Use this static initializer to specify subset for tests
	// All specified tests which do not belong to the class are skipped...
	static {
	//	TESTS_PREFIX =  "testVarargs";
//		TESTS_NAMES = new String[] { "testMethodReferenceBug80890" };
//		TESTS_NUMBERS = new int[] { 81084 };
	//	TESTS_RANGE = new int[] { 16, -1 };
		}

	protected void assertSearchResults(String expected) {
		assertSearchResults("Unexpected search results", expected, this.resultCollector);
	}
	IJavaSearchScope getJavaSearchScopeBugs() {
		return SearchEngine.createJavaSearchScope(new IJavaProject[] {getJavaProject("JavaSearchBugs")});
	}
	IJavaSearchScope getJavaSearchScopeBugs(String packageName, boolean addSubpackages) throws JavaModelException {
		if (packageName == null) return getJavaSearchScopeBugs();
		return getJavaSearchPackageScope("JavaSearchBugs", packageName, addSubpackages);
	}
	IJavaSearchScope getJavaSearchPackageScope(String projectName, String packageName, boolean addSubpackages) throws JavaModelException {
		IPackageFragment fragment = getPackageFragment(projectName, "src", packageName);
		if (fragment == null) return null;
		IJavaElement[] searchPackages = null;
		if (addSubpackages) {
			// Create list of package with first found one
			List packages = new ArrayList();
			packages.add(fragment);
			// Add all possible subpackages
			IJavaElement[] children= ((IPackageFragmentRoot)fragment.getParent()).getChildren();
			String[] names = ((PackageFragment)fragment).names;
			int namesLength = names.length;
			nextPackage: for (int i= 0, length = children.length; i < length; i++) {
				PackageFragment currentPackage = (PackageFragment) children[i];
				String[] otherNames = currentPackage.names;
				if (otherNames.length <= namesLength) continue nextPackage;
				for (int j = 0; j < namesLength; j++) {
					if (!names[j].equals(otherNames[j]))
						continue nextPackage;
				}
				packages.add(currentPackage);
			}
			searchPackages = new IJavaElement[packages.size()];
			packages.toArray(searchPackages);
		} else {
			searchPackages = new IJavaElement[1];
			searchPackages[0] = fragment;
		}
		return SearchEngine.createJavaSearchScope(searchPackages);
	}
	/*
	 * Overrides super method to create parent folders if necessary
	 */
	public ICompilationUnit getWorkingCopy(String fileName, String source) throws JavaModelException {
		IPath folder = new Path(fileName).removeLastSegments(1);
		try {
			createFolder(folder);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		ICompilationUnit workingCopy = super.getWorkingCopy(fileName, source, true/*compute problems*/);
		workingCopy.commitWorkingCopy(true, null);	// need to commit to index file
		return workingCopy;
	}
	/*
	 * Overrides super method to create parent folders if necessary
	 */
	public ICompilationUnit getWorkingCopy(String fileName, String source, WorkingCopyOwner owner, boolean computeProblems) throws JavaModelException {
		ICompilationUnit workingCopy = super.getWorkingCopy(fileName, source, owner, computeProblems);
		workingCopy.commitWorkingCopy(true, null);	// need to commit to index file
		return workingCopy;
	}
	/*
	 * Overrides super method to create parent folders if necessary
	 */
	public ICompilationUnit getWorkingCopy(String fileName, String source, WorkingCopyOwner owner) throws JavaModelException {
		IPath folder = new Path(fileName).removeLastSegments(1);
		try {
			createFolder(folder);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		ICompilationUnit workingCopy = super.getWorkingCopy(fileName, source, owner, true/*compute problems*/);
		workingCopy.commitWorkingCopy(true, null);	// need to commit to index file
		return workingCopy;
	}
	protected void search(IJavaElement element, int limitTo/*, IJavaSearchScope scope*/) throws CoreException {
		search(element, limitTo, SearchPattern.R_EXACT_MATCH|SearchPattern.R_CASE_SENSITIVE, getJavaSearchScopeBugs(), this.resultCollector);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.tests.model.SuiteOfTestCases#setUpSuite()
	 */
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		JAVA_PROJECT = setUpJavaProject("JavaSearchBugs", "1.5");
	}
	public void tearDownSuite() throws Exception {
		deleteProject("JavaSearchBugs");
		super.tearDownSuite();
	}
	protected void setUp () throws Exception {
		this.resultCollector = new JavaSearchTests.JavaSearchResultCollector();
		this.resultCollector.showAccuracy = true;
		super.setUp();
	}
	/**
	 * Bug 41018: Method reference not found
	 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=41018">41018</a>
	 */
	public void testBug41018() throws CoreException {
		ICompilationUnit workingCopy = null;
		try {
			workingCopy = getWorkingCopy("/JavaSearchBugs/src/b41018/A.java",
				"package b41018;\n" +
				"public class A {\n" + 
				"	protected void anotherMethod() {\n" + 
				"		methodA(null);\n" + 
				"	}\n" + 
				"	private Object methodA(ClassB.InnerInterface arg3) {\n" + 
				"		return null;\n" + 
				"	}\n" + 
				"}\n" + 
				"class ClassB implements InterfaceB {\n" + 
				"}\n" + 
				"interface InterfaceB {\n" + 
				"	interface InnerInterface {\n" + 
				"	}\n" + 
				"}\n"
				);
			IType type = workingCopy.getType("A");
			IMethod method = type.getMethod("methodA", new String[] { "QClassB.InnerInterface;" });
			search(method, REFERENCES);
			assertSearchResults(
				"src/b41018/A.java void b41018.A.anotherMethod() [methodA(null)] EXACT_MATCH"
			);
		}
		finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}
	/**
	 * Bug 70827: [Search] wrong reference match to private method of supertype
	 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=70827">70827</a>
	 */
	public void testBug70827() throws CoreException {
		ICompilationUnit workingCopy = null;
		try {
			workingCopy = getWorkingCopy("/JavaSearchBugs/src/b70827/A.java",
				"package b70827;\n" + 
				"class A {\n" + 
				"	private void privateMethod() {\n" + 
				"	}\n" + 
				"}\n" + 
				"class Second extends A {\n" + 
				"	void call() {\n" + 
				"		int i= privateMethod();\n" + 
				"	}\n" + 
				"	int privateMethod() {\n" + 
				"		return 1;\n" + 
				"	}\n" + 
				"}\n"
				);
			IType type = workingCopy.getType("A");
			IMethod method = type.getMethod("privateMethod", new String[] {});
			search(method, REFERENCES);
			assertSearchResults(
				""
			);
		}
		finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/**
	 * Bug 71279: [Search] NPE in TypeReferenceLocator when moving CU with unresolved type reference
	 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=71279">71279</a>
	 */
	public void testBug71279() throws CoreException {
		JavaSearchResultCollector result = new JavaSearchResultCollector() {
		    public void beginReporting() {
		        results.append("Starting search...");
	        }
		    public void endReporting() {
		        results.append("\nDone searching.");
	        }
		};
		ICompilationUnit workingCopy = null;
		try {
			workingCopy = getWorkingCopy("/JavaSearchBugs/src/b71279/AA.java",
				"package b71279;\n" + 
				"public class AA {\n" + 
				"	Unknown ref;\n" + 
				"}\n"
				);
			new SearchEngine().searchDeclarationsOfReferencedTypes(workingCopy, result, null);
			assertSearchResults(
				"Starting search...\n" + 
				"Done searching.",
				result);
		}
		finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/**
	 * Bug 72866: [search] references to endVisit(MethodInvocation) reports refs to endVisit(SuperMethodInvocation)
	 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=72866">72866</a>
	 */
	public void testBug72866() throws CoreException {
		ICompilationUnit[] workingCopies = new ICompilationUnit[4];
		try {
			WorkingCopyOwner owner = new WorkingCopyOwner() {};
			workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b72866/A.java",
				"package b72866;\n" + 
				"public abstract class A {\n" + 
				"	public abstract void foo(V v);\n" + 
				"}\n",
				owner
				);
			workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b72866/SX.java",
				"package b72866;\n" + 
				"public class SX extends A {\n" + 
				"	public void foo(V v) {\n" + 
				"	    v.bar(this);\n" + 
				"	}\n" + 
				"}\n"	,
				owner,
				true);
			workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b72866/V.java",
				"package b72866;\n" + 
				"public class V {\n" + 
				"	void bar(A a) {}\n" + 
				"	void bar(X x) {}\n" + 
				"	void bar(SX s) {}\n" + 
				"}\n"	,
				owner,
				true);
			workingCopies[3] = getWorkingCopy("/JavaSearchBugs/src/b72866/X.java",
				"package b72866;\n" + 
				"public class X extends A {\n" + 
				"	public void foo(V v) {\n" + 
				"	    v.bar(this);\n" + 
				"	}\n" + 
				"}\n"	,
				owner,
				true	);
			// search for first and second method should both return 2 inaccurate matches
			IType type = workingCopies[2].getType("V");
			IMethod method = type.getMethod("bar", new String[] {"QX;"});
			search(method, REFERENCES);
			assertSearchResults(
				"src/b72866/X.java void b72866.X.foo(V) [bar(this)] EXACT_MATCH"
			);
		}
		finally {
			discardWorkingCopies(workingCopies);
		}
	}

	/**
	 * Bug 73112: [Search] SearchEngine doesn't find all fields multiple field declarations
	 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=73112">73112</a>
	 */
	public void testBug73112a() throws CoreException {
		ICompilationUnit workingCopy = null;
		try {
			workingCopy = getWorkingCopy("/JavaSearchBugs/src/b73112/A.java",
				"package b73112;\n" + 
				"public class A {\n" + 
				"    int fieldA73112a = 1, fieldA73112b = new Integer(2).intValue(), fieldA73112c = fieldA73112a + fieldA73112b;\n" + 
				"    int fieldA73112d;\n" + 
				"    \n" + 
				"    public void method(){}\n" + 
				"}\n");
			// search field references to first multiple field
			search(
				"fieldA73112*",
				FIELD,
				ALL_OCCURRENCES,
				getJavaSearchScopeBugs(),
				this.resultCollector);
			assertSearchResults(
				"src/b73112/A.java b73112.A.fieldA73112a [fieldA73112a] EXACT_MATCH\n" + 
				"src/b73112/A.java b73112.A.fieldA73112b [fieldA73112b] EXACT_MATCH\n" + 
				"src/b73112/A.java b73112.A.fieldA73112c [fieldA73112c] EXACT_MATCH\n" + 
				"src/b73112/A.java b73112.A.fieldA73112c [fieldA73112a] EXACT_MATCH\n" + 
				"src/b73112/A.java b73112.A.fieldA73112c [fieldA73112b] EXACT_MATCH\n" + 
				"src/b73112/A.java b73112.A.fieldA73112d [fieldA73112d] EXACT_MATCH"
			);
		}
		finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}
	public void testBug73112b() throws CoreException {
		ICompilationUnit workingCopy = null;
		try {
			workingCopy = super.getWorkingCopy("/JavaSearchBugs/src/b73112/B.java",
				"package b73112;\n" + 
				"public class B {\n" + 
				"    int fieldB73112a, fieldB73112b = 10;\n" + 
				"    int fieldB73112c = fieldB73112a + fieldB73112b, fieldB73112d = fieldB73112c + fieldB73112a, fieldB73112e;\n" + 
				"    \n" + 
				"    public void method(){}\n" + 
				"}\n");
			workingCopy.commitWorkingCopy(true, null);
			// search field references to first multiple field
			search(
				"fieldB73112*",
				FIELD,
				ALL_OCCURRENCES,
				getJavaSearchScopeBugs(),
				this.resultCollector);
			assertSearchResults(
				"src/b73112/B.java b73112.B.fieldB73112a [fieldB73112a] EXACT_MATCH\n" + 
				"src/b73112/B.java b73112.B.fieldB73112b [fieldB73112b] EXACT_MATCH\n" + 
				"src/b73112/B.java b73112.B.fieldB73112c [fieldB73112c] EXACT_MATCH\n" + 
				"src/b73112/B.java b73112.B.fieldB73112c [fieldB73112a] EXACT_MATCH\n" + 
				"src/b73112/B.java b73112.B.fieldB73112c [fieldB73112b] EXACT_MATCH\n" + 
				"src/b73112/B.java b73112.B.fieldB73112d [fieldB73112d] EXACT_MATCH\n" + 
				"src/b73112/B.java b73112.B.fieldB73112d [fieldB73112c] EXACT_MATCH\n" + 
				"src/b73112/B.java b73112.B.fieldB73112d [fieldB73112a] EXACT_MATCH\n" + 
				"src/b73112/B.java b73112.B.fieldB73112e [fieldB73112e] EXACT_MATCH"
			);
		}
		finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/**
	 * Bug 73336: [1.5][search] Search Engine does not find type references of actual generic type parameters
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=73336">73336</a>
	 */
	public void testBug73336() throws CoreException {
		ICompilationUnit[] workingCopies = new ICompilationUnit[6];
		try {
			WorkingCopyOwner owner = new WorkingCopyOwner() {};
			workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b73336/A.java",
				"package b73336;\n" + 
				"public class A {}\n",
				owner
				);
			workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b73336/AA.java",
				"package b73336;\n" + 
				"public class AA extends A {}\n",
				owner,
				true);
			workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b73336/B.java",
				"package b73336;\n" + 
				"public class B extends X<A, A> {\n" + 
				"	<T> void foo(T t) {}\n" + 
				"}\n",
				owner,
				true);
			workingCopies[3] = getWorkingCopy("/JavaSearchBugs/src/b73336/C.java",
				"package b73336;\n" + 
				"public class C implements I<A> {\n" + 
				"	public void foo() {\n" + 
				"		B b = new B();\n" + 
				"		b.<A>foo(new A());\n" + 
				"	}\n" + 
				"}\n",
				owner,
				true	);
			workingCopies[4] = getWorkingCopy("/JavaSearchBugs/src/b73336/I.java",
				"package b73336;\n" + 
				"public interface I<T>  {\n" + 
				"	public void foo();\n" + 
				"}\n",
				owner,
				true	);
			workingCopies[5] = getWorkingCopy("/JavaSearchBugs/src/b73336/X.java",
				"package b73336;\n" + 
				"public class X<T, U> {\n" + 
				"	<V> void foo(V v) {}\n" + 
				"	class Member<T> {\n" + 
				"		void foo() {}\n" + 
				"	}\n" + 
				"}\n",
				owner,
				true	);
			// search for first and second method should both return 2 inaccurate matches
			IType type = workingCopies[0].getType("A");
			search(type,
				REFERENCES,
				getJavaSearchScopeBugs("b73336", false),
				this.resultCollector);
			assertSearchResults(
				"src/b73336/AA.java b73336.AA [A] EXACT_MATCH\n" + 
				"src/b73336/B.java b73336.B [A] EXACT_MATCH\n" + 
				"src/b73336/B.java b73336.B [A] EXACT_MATCH\n" + 
				"src/b73336/C.java b73336.C [A] EXACT_MATCH\n" + 
				"src/b73336/C.java void b73336.C.foo() [A] EXACT_MATCH\n" + 
				"src/b73336/C.java void b73336.C.foo() [A] EXACT_MATCH"
			);
		}
		finally {
			discardWorkingCopies(workingCopies);
		}
	}
	public void testBug73336b() throws CoreException {
		ICompilationUnit[] workingCopies = new ICompilationUnit[4];
		try {
			WorkingCopyOwner owner = new WorkingCopyOwner() {};
			workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b73336b/A.java",
				"package b73336b;\n" + 
				"public class A {}\n",
				owner
				);
			workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b73336b/B.java",
				"package b73336b;\n" + 
				"public class B extends X<A, A> {\n" + 
				"}\n",
				owner,
				true);
			workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b73336b/C.java",
				"package b73336b;\n" + 
				"public class C extends X<A, A>.Member<A> {\n" + 
				"	public C() {\n" + 
				"		new X<A, A>().super();\n" + 
				"	}\n" + 
				"}\n",
				owner,
				true);
			workingCopies[3] = getWorkingCopy("/JavaSearchBugs/src/b73336b/X.java",
				"package b73336b;\n" + 
				"public class X<T, U> {\n" + 
				"	<V> void foo(V v) {}\n" + 
				"	class Member<T> {\n" + 
				"		void foo() {}\n" + 
				"	}\n" + 
				"}\n",
				owner,
				true	);
			// search for first and second method should both return 2 inaccurate matches
			IType type = workingCopies[0].getType("A");
			search(type,
				REFERENCES,
				getJavaSearchScopeBugs("b73336b", false), 
				this.resultCollector);
			assertSearchResults(
				"src/b73336b/B.java b73336b.B [A] EXACT_MATCH\n" + 
				"src/b73336b/B.java b73336b.B [A] EXACT_MATCH\n" + 
				"src/b73336b/C.java b73336b.C [A] EXACT_MATCH\n" + 
				"src/b73336b/C.java b73336b.C [A] EXACT_MATCH\n" + 
				"src/b73336b/C.java b73336b.C [A] EXACT_MATCH\n" + 
				"src/b73336b/C.java b73336b.C() [A] EXACT_MATCH\n" + 
				"src/b73336b/C.java b73336b.C() [A] EXACT_MATCH"
			);
		}
		finally {
			discardWorkingCopies(workingCopies);
		}
	}
	// Verify that no NPE was raised on following case (which produces compiler error)
	public void testBug73336c() throws CoreException {
		ICompilationUnit[] workingCopies = new ICompilationUnit[4];
		try {
			WorkingCopyOwner owner = new WorkingCopyOwner() {};
			workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b73336c/A.java",
				"package b73336c;\n" + 
				"public class A {}\n",
				owner
				);
			workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b73336c/B.java",
				"package b73336c;\n" + 
				"public class B extends X<A, A> {\n" + 
				"}\n",
				owner,
				true);
			workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b73336c/C.java",
				"package b73336c;\n" + 
				"public class C implements X<A, A>.Interface<A>  {\n" + 
				"	void bar() {}\n" + 
				"}\n",
				owner,
				true);
			workingCopies[3] = getWorkingCopy("/JavaSearchBugs/src/b73336c/X.java",
				"package b73336c;\n" + 
				"public class X<T, U> {\n" + 
				"	interface Interface<V> {\n" + 
				"		void bar();\n" + 
				"	}\n" + 
				"}\n",
				owner,
				true	);
			// search for first and second method should both return 2 inaccurate matches
			IType type = workingCopies[0].getType("A");
			search(type,
				REFERENCES,
				getJavaSearchScopeBugs("b73336c", false), 
				this.resultCollector);
			assertSearchResults(
				"src/b73336c/B.java b73336c.B [A] EXACT_MATCH\n" + 
				"src/b73336c/B.java b73336c.B [A] EXACT_MATCH\n" + 
				"src/b73336c/C.java b73336c.C [A] EXACT_MATCH\n" + 
				"src/b73336c/C.java b73336c.C [A] EXACT_MATCH\n" + 
				"src/b73336c/C.java b73336c.C [A] EXACT_MATCH"
			);
		}
		finally {
			discardWorkingCopies(workingCopies);
		}
	}

	/**
	 * Bug 73696: searching only works for IJavaSearchConstants.TYPE, but not CLASS or INTERFACE
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=73696">73696</a>
	 */
	public void testBug73696() throws CoreException {
		ICompilationUnit[] workingCopies = new ICompilationUnit[2];
		try {
			WorkingCopyOwner owner = new WorkingCopyOwner() {};
			workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b73696/C.java",
				"package b73696;\n" + 
				"public class C implements  I {\n" + 
				"}",
				owner
				);
			workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b73696/I.java",
				"package b73696;\n" + 
				"public interface I {}\n",
				owner,
				true);
			IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {workingCopies[0].getParent()});
			
			// Interface declaration
			TypeDeclarationPattern pattern = new TypeDeclarationPattern(
				null,
				null,
				null,
				IIndexConstants.INTERFACE_SUFFIX,
				SearchPattern.R_PATTERN_MATCH
			);
			new SearchEngine().search(
				pattern,
				new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
				scope,
				resultCollector,
				null);
			// Class declaration
			pattern = new TypeDeclarationPattern(
				null,
				null,
				null,
				IIndexConstants.CLASS_SUFFIX,
				SearchPattern.R_PATTERN_MATCH
			);
			new SearchEngine().search(
				pattern,
				new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
				scope,
				resultCollector,
				null);
			assertSearchResults(
				"src/b73696/I.java b73696.I [I] EXACT_MATCH\n" + 
				"src/b73696/C.java b73696.C [C] EXACT_MATCH",
				this.resultCollector);
		}
		finally {
			discardWorkingCopies(workingCopies);
		}
	}

	/**
	 * Bug 74776: [Search] Wrong search results for almost identical method
	 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=74776">74776</a>
	 */
	public void testBug74776() throws CoreException {
		ICompilationUnit[] workingCopies = new ICompilationUnit[3];
		try {
			WorkingCopyOwner owner = new WorkingCopyOwner() {};
			workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b74776/A.java",
				"package b74776;\n" + 
				"public class A {\n" + 
				"	/**\n" + 
				"	 * @deprecated Use {@link #foo(IRegion)} instead\n" + 
				"	 * @param r\n" + 
				"	 */\n" + 
				"	void foo(Region r) {\n" + 
				"		foo((IRegion)r);\n" + 
				"	}\n" + 
				"	void foo(IRegion r) {\n" + 
				"	}\n" + 
				"}\n",
				owner
				);
			workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b74776/IRegion.java",
				"package b74776;\n" + 
				"public interface IRegion {\n" + 
				"}\n",
				owner,
				true);
			workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b74776/Region.java",
				"package b74776;\n" + 
				"public class Region implements IRegion {\n" + 
				"\n" + 
				"}\n",
				owner,
				true);
			// search method references
			IType type = workingCopies[0].getType("A");
			IMethod method = type.getMethod("foo", new String[] { "QRegion;" });
			search(method, REFERENCES);
			assertSearchResults("");
		}
		finally {
			discardWorkingCopies(workingCopies);
		}
	}

	/**
	 * Bug 77093: [search] No references found to method with member type argument
	 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=77093">77093</a>
	 */
	public void testBug77093constructor() throws CoreException {
		ICompilationUnit workingCopy = null;
		try {
			workingCopy = getWorkingCopy("/JavaSearchBugs/src/b77093/X.java",
				"package b77093;\n" + 
				"public class X {\n" + 
				"	class Z {\n" + 
				"	}\n" + 
				"	Z[][] z_arrays;\n" + 
				"	X() {\n" + 
				"		this(new Z[10][]);\n" + 
				"	}\n" + 
				"	X(Z[][] arrays) {\n" + 
				"		z_arrays = arrays;\n" + 
				"	}\n" + 
				"	private void foo(Z[] args) {\n" + 
				"	}\n" + 
				"	void bar() {\n" + 
				"		for (int i=0; i<z_arrays.length; i++)\n" + 
				"			foo(z_arrays[i]);\n" + 
				"	}\n" + 
				"}");
			IType type = workingCopy.getType("X");
			IMethod method = type.getMethod("X", new String[] {"[[QZ;"});
			// Search for constructor declarations and references
			search(method, ALL_OCCURRENCES);
			assertSearchResults(
				"src/b77093/X.java b77093.X() [this(new Z[10][])] EXACT_MATCH\n"+
				"src/b77093/X.java b77093.X(Z[][]) [X] EXACT_MATCH"
			);
		}
		finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}
	public void testBug77093field() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("/JavaSearchBugs/src/b77093/X.java");
		IType type = unit.getType("X");
		IField field = type.getField("z_arrays");
		// Search for field declarations and references
		search(field, ALL_OCCURRENCES);
		assertSearchResults(
			"src/b77093/X.java b77093.X.z_arrays [z_arrays] EXACT_MATCH\n" +
			"src/b77093/X.java b77093.X(Z[][]) [z_arrays] EXACT_MATCH\n" + 
			"src/b77093/X.java void b77093.X.bar() [z_arrays] EXACT_MATCH\n" + 
			"src/b77093/X.java void b77093.X.bar() [z_arrays] EXACT_MATCH"
		);
	}
	public void testBug77093method() throws CoreException {
		IType type = getCompilationUnit("JavaSearchBugs/src/b77093/X.java").getType("X");
		IMethod method = type.getMethod("foo", new String[] {"[QZ;"});
		search(method, ALL_OCCURRENCES);
		assertSearchResults(
			"src/b77093/X.java void b77093.X.foo(Z[]) [foo] EXACT_MATCH\n" +
			"src/b77093/X.java void b77093.X.bar() [foo(z_arrays[i])] EXACT_MATCH"
		);
	}
	/**
	 * Bug 77388: [compiler] Reference to constructor includes space after closing parenthesis
	 */
	public void testBug77388() throws CoreException {
		ICompilationUnit workingCopy = null;
		try {
			workingCopy = getWorkingCopy("/JavaSearchBugs/src/b77388/Test.java",
				"package b77388;\n" + 
				"class Test {\n" + 
				"	Test(int a, int b) {	}\n" + 
				"	void take(Test mc) { }\n" + 
				"	void run() {\n" + 
				"		take( new Test(1, 2) ); // space in \") )\" is in match\n" + 
				"	}\n" + 
				"}");
			IType type = workingCopy.getType("Test");
			IMethod method = type.getMethod("Test", new String[] {"I", "I"});
			// Search for constructor references
			search(method, REFERENCES);
			assertSearchResults(
				"src/b77388/Test.java void b77388.Test.run() [new Test(1, 2)] EXACT_MATCH"
			);
		}
		finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}
	/**
	 * Bug 78082: [1.5][search] FieldReferenceMatch in static import should not include qualifier
	 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=78082">78082</a>
	 */
	public void testBug78082() throws CoreException {
		ICompilationUnit[] workingCopies = new ICompilationUnit[2];
		try {
			WorkingCopyOwner owner = new WorkingCopyOwner() {};
			workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b78082/M.java",
				"package b78082;\n" + 
				"public class M {\n" + 
				"	static int VAL=78082;\n" + 
				"}\n",
				owner
				);
			workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b78082/XY.java",
				"package b78082;\n" + 
				"import static b78082.M.VAL;\n" + 
				"public class XY {\n" + 
				"	double val = VAL;\n" + 
				"	double val2= b78082.M.VAL;\n" + 
				"}\n",
				owner,
				true);
			// search field references
			IType type = workingCopies[0].getType("M");
			IField field = type.getField("VAL");
			search(field, ALL_OCCURRENCES);
			assertSearchResults(
				"src/b78082/M.java b78082.M.VAL [VAL] EXACT_MATCH\n" + 
				"src/b78082/XY.java [VAL] EXACT_MATCH\n" + 
				"src/b78082/XY.java b78082.XY.val [VAL] EXACT_MATCH\n" + 
				"src/b78082/XY.java b78082.XY.val2 [VAL] EXACT_MATCH"
			);
		}
		finally {
			discardWorkingCopies(workingCopies);
		}
	}

	/**
	 * Bug 79267: [search] Refactoring of static generic member fails partially
	 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=79267">79267</a>
	 */
	public void testBug79267() throws CoreException {
		ICompilationUnit workingCopy = null;
		try {
			workingCopy = getWorkingCopy("/JavaSearchBugs/src/b79267/Test.java",
				"package b79267;\n" + 
				"public class Test {\n" + 
				"	private static final X<String, String> BEFORE	= new X<String, String>(4);\n" + 
				"\n" + 
				"	static {\n" + 
				"		BEFORE.put(\"key1\",\"value1\");\n" + 
				"		BEFORE.put(\"key2\",\"value2\");\n" + 
				"	}\n" + 
				"	\n" + 
				"	private static final X<Y, Object>	objectToPrimitiveMap	= new X<Y, Object>(8);\n" + 
				"\n" + 
				"	static {\n" + 
				"		objectToPrimitiveMap.put(new Y<Object>(new Object()), new Object());\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"class X<T, U> {\n" + 
				"	X(int x) {}\n" + 
				"	void put(T t, U u) {}\n" + 
				"}\n" + 
				"\n" + 
				"class Y<T> {\n" + 
				"	Y(T t) {}\n" + 
				"}\n");
			// search field references
			IType type = workingCopy.getType("Test");
			IField field = type.getField("BEFORE");
			search(field, REFERENCES);
			field = type.getField("objectToPrimitiveMap");
			search(field, REFERENCES);
			assertSearchResults(
				"src/b79267/Test.java b79267.Test.static {} [BEFORE] EXACT_MATCH\n" + 
				"src/b79267/Test.java b79267.Test.static {} [BEFORE] EXACT_MATCH\n" + 
				"src/b79267/Test.java b79267.Test.static {} [objectToPrimitiveMap] EXACT_MATCH",
				resultCollector);
		}
		finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/**
	 * Bug 79803: [1.5][search] Search for references to type A reports match for type variable A
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=79803">79803</a>
	 */
	public void testBug79803() throws CoreException {
		ICompilationUnit workingCopy = null;
		try {
			workingCopy = getWorkingCopy("/JavaSearchBugs/src/b79803/A.java",
				"package b79803;\n" + 
				"class A<A> {\n" + 
				"    A a;\n" + 
				"    b79803.A pa= new b79803.A();\n" + 
				"}\n"	);
			// search for first and second method should both return 2 inaccurate matches
			IType type = workingCopy.getType("A");
			search(type,
				REFERENCES,
				SearchPattern.R_CASE_SENSITIVE|SearchPattern.R_ERASURE_MATCH,
				getJavaSearchScopeBugs("b79803", false),
				this.resultCollector);
			assertSearchResults(
				"src/b79803/A.java b79803.A.pa [b79803.A] EXACT_MATCH\n" + 
				"src/b79803/A.java b79803.A.pa [b79803.A] EXACT_MATCH"
			);
		}
		finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}
	public void testBug79803b() throws CoreException {
		search("A", TYPE, REFERENCES, getJavaSearchScopeBugs("b79803", false), this.resultCollector);
		assertSearchResults(
			"src/b79803/A.java b79803.A.a [A] EXACT_MATCH\n" + 
			"src/b79803/A.java b79803.A.pa [A] EXACT_MATCH\n" + 
			"src/b79803/A.java b79803.A.pa [A] EXACT_MATCH",
			this.resultCollector);
	}

	/**
	 * Bug 79860: [1.5][search] Search doesn't find type reference in type parameter bound
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=79860">79860</a>
	 */
	public void testBug79860() throws CoreException {
		ICompilationUnit[] workingCopies = new ICompilationUnit[2];
		try {
			WorkingCopyOwner owner = new WorkingCopyOwner() {};
			workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b79860/X.java",
				"package b79860;\n" + 
				"public class X<T extends A> { }\n" + 
				"class A { }",
				owner
				);
			workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b79860/Y.java",
				"package b79860;\n" + 
				"public class Y<T extends B&I1&I2&I3> { }\n" + 
				"class B { }\n" + 
				"interface I1 {}\n" + 
				"interface I2 {}\n" + 
				"interface I3 {}\n",
				owner,
				true);
			// search for first and second method should both return 2 inaccurate matches
			IType type = workingCopies[0].getType("A");
			search(type,
				REFERENCES,
				getJavaSearchScopeBugs("b79860", false), 
				this.resultCollector);
			assertSearchResults(
				"src/b79860/X.java b79860.X [A] EXACT_MATCH"
			);
		}
		finally {
			discardWorkingCopies(workingCopies);
		}
	}
	public void testBug79860b() throws CoreException {
		search("I?", TYPE, REFERENCES, getJavaSearchScopeBugs("b79860", false), resultCollector);
		assertSearchResults(
			"src/b79860/Y.java b79860.Y [I1] EXACT_MATCH\n" + 
			"src/b79860/Y.java b79860.Y [I2] EXACT_MATCH\n" + 
			"src/b79860/Y.java b79860.Y [I3] EXACT_MATCH"
		);
	}

	/**
	 * Bug 80890: [search] Strange search engine behaviour
	 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=80890">80890</a>
	 */
	public void testBug80890() throws CoreException, JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			workingCopy = getWorkingCopy("/JavaSearchBugs/src/b80890/A.java",
				"package b80890;\n" + 
				"public class A {\n" + 
				"	protected void foo(Exception e) {}\n" + 
				"	protected void foo(String s) {}\n" + 
				"}\n" + 
				"class B1 extends A {\n" + 
				"	public void bar1() {\n" + 
				"		foo(null);\n" + 
				"	}\n" + 
				"}\n" + 
				"class B2 extends A {\n" + 
				"	public void bar2() {\n" + 
				"		foo(null);\n" + 
				"	}\n" + 
				"}\n"
				);
			// search for first and second method should both return 2 inaccurate matches
			IType type = workingCopy.getType("A");
			IMethod method = type.getMethods()[0];
			search(method, REFERENCES);
			method = type.getMethods()[1];
			search(method, REFERENCES);
			assertSearchResults(
				"src/b80890/A.java void b80890.B1.bar1() [foo(null)] POTENTIAL_MATCH\n" + 
				"src/b80890/A.java void b80890.B2.bar2() [foo(null)] POTENTIAL_MATCH\n" + 
				"src/b80890/A.java void b80890.B1.bar1() [foo(null)] POTENTIAL_MATCH\n" + 
				"src/b80890/A.java void b80890.B2.bar2() [foo(null)] POTENTIAL_MATCH"
			);
		}
		finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/**
	 * Bug 80918: [1.5][search] ClassCastException when searching for references to binary type
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=80918">80918</a>
	 */
	public void testBug80918() throws CoreException {
		IType type = getClassFile("JavaSearchBugs", getExternalJCLPathString("1.5"), "java.lang", "Exception.class").getType();
		search(type, REFERENCES, SearchPattern.R_CASE_SENSITIVE|SearchPattern.R_ERASURE_MATCH, getJavaSearchScopeBugs("b79803", false), this.resultCollector);
		assertSearchResults(
			"", // do not expect to find anything, just verify that no CCE happens
			this.resultCollector);
	}

	/**
	 * Bug 81084: [1.5][search]Rename field fails on field based on parameterized type with member type parameter
	 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=81084">81084</a>
	 */
	public void testBug81084a() throws CoreException, JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			workingCopy = getWorkingCopy("/JavaSearchBugs/src/b81084a/Test.java",
				"package b81084a;\n" + 
				"class List<E> {}\n" + 
				"public class Test {\n" + 
				"	class Element{}\n" + 
				"	static class Inner {\n" + 
				"		private final List<Element> fList1;\n" + 
				"		private final List<Test.Element> fList2;\n" + 
				"		public Inner(List<Element> list) {\n" + 
				"			fList1 = list;\n" + 
				"			fList2 = list;\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n"
				);
			IType type = workingCopy.getType("Test").getType("Inner");
			IField field1 = type.getField("fList1");
			search(field1, REFERENCES);
			IField field2 = type.getField("fList2");
			search(field2, REFERENCES);
			assertSearchResults(
				"src/b81084a/Test.java b81084a.Test$Inner(List<Element>) [fList1] EXACT_MATCH\n" + 
				"src/b81084a/Test.java b81084a.Test$Inner(List<Element>) [fList2] EXACT_MATCH",
				this.resultCollector);
		}
		finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}
	public void testBug81084b() throws CoreException, JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			workingCopy = getWorkingCopy("/JavaSearchBugs/src/b81084b/Test.java",
				"package b81084b;\n" + 
				"class List<E> {}\n" + 
				"public class Test {\n" + 
				"	class Element{}\n" + 
				"	static class Inner {\n" + 
				"		private final List<? extends Element> fListb1;\n" + 
				"		private final List<? extends Test.Element> fListb2;\n" + 
				"		public Inner(List<Element> list) {\n" + 
				"			fListb1 = list;\n" + 
				"			fListb2 = list;\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n"
				);
			IType type = workingCopy.getType("Test").getType("Inner");
			IField field1 = type.getField("fListb1");
			search(field1, REFERENCES);
			IField field2 = type.getField("fListb2");
			search(field2, REFERENCES);
			assertSearchResults(
				"src/b81084b/Test.java b81084b.Test$Inner(List<Element>) [fListb1] EXACT_MATCH\n" + 
				"src/b81084b/Test.java b81084b.Test$Inner(List<Element>) [fListb2] EXACT_MATCH",
				this.resultCollector);
		}
		finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}
	public void testBug81084c() throws CoreException, JavaModelException {
		search("fList1", FIELD, REFERENCES, getJavaSearchScopeBugs(), this.resultCollector);
		search("fList2", FIELD, REFERENCES, getJavaSearchScopeBugs(), this.resultCollector);
		assertSearchResults(
			"src/b81084a/Test.java b81084a.Test$Inner(List<Element>) [fList1] EXACT_MATCH\n" + 
			"src/b81084a/Test.java b81084a.Test$Inner(List<Element>) [fList2] EXACT_MATCH",
			this.resultCollector);
	}
}
