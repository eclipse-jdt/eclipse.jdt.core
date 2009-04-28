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

import junit.framework.Test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.*;
public class WorkingCopySearchTests extends JavaSearchTests {
	ICompilationUnit workingCopy;

	public WorkingCopySearchTests(String name) {
		super(name);
	}
	public static Test suite() {
		return buildModelTestSuite(WorkingCopySearchTests.class);
	}
	// Use this static initializer to specify subset for tests
	// All specified tests which do not belong to the class are skipped...
	static {
//		TESTS_PREFIX =  "testAllTypeNames";
//		TESTS_NAMES = new String[] { "testAllTypeNamesBug98684" };
//		TESTS_NUMBERS = new int[] { 8 };
//		TESTS_RANGE = new int[] { -1, -1 };
	}

	/**
	 * Get a new working copy.
	 */
	protected void setUp() throws Exception {
		super.setUp();
		try {
			this.workingCopy = this.getCompilationUnit("JavaSearch", "src", "wc", "X.java").getWorkingCopy(null);
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Destroy the working copy.
	 */
	protected void tearDown() throws Exception {
		this.workingCopy.discardWorkingCopy();
		this.workingCopy = null;
		super.tearDown();
	}

	/**
	 * Hierarchy scope on a working copy test.
	 */
	public void testHierarchyScopeOnWorkingCopy() throws CoreException {
		ICompilationUnit unit = this. getCompilationUnit("JavaSearch", "src", "a9", "A.java");
		ICompilationUnit copy = unit.getWorkingCopy(null);
		try {
			IType type = copy.getType("A");
			IJavaSearchScope scope = SearchEngine.createHierarchyScope(type);
			assertTrue("a9.A should be included in hierarchy scope", scope.encloses(type));
			assertTrue("a9.C should be included in hierarchy scope", scope.encloses(copy.getType("C")));
			assertTrue("a9.B should be included in hierarchy scope", scope.encloses(copy.getType("B")));
			IPath path = unit.getUnderlyingResource().getFullPath();
			assertTrue("a9/A.java should be included in hierarchy scope", scope.encloses(path.toString()));
		} finally {
			copy.discardWorkingCopy();
		}
	}

	/**
	 * Type declaration in a working copy test.
	 * A new type is added in the working copy only.
	 */
	public void testAddNewType() throws CoreException {
		this.workingCopy.createType(
			"class NewType {\n" +
			"}",
			null,
			false,
			null);

		IJavaSearchScope scope =
			SearchEngine.createJavaSearchScope(
				new IJavaElement[] {this.workingCopy.getParent()});
	//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		SearchPattern pattern = SearchPattern.createPattern(
			"NewType",
			TYPE,
			DECLARATIONS,
			SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE);
		new SearchEngine(new ICompilationUnit[] {this.workingCopy}).search(
			pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			scope,
			this.resultCollector,
			null);
		assertSearchResults(
			"src/wc/X.java wc.NewType [NewType]",
			this.resultCollector);
	}

	/*
	 * Search all type names in working copies test.
	 * (Regression test for bug 40793 Primary working copies: Type search does not find type in modified CU)
	 */
	public void testAllTypeNames1() throws CoreException {
		this.workingCopy.getBuffer().setContents(
			"package wc;\n" +
			"public class Y {\n" +
			"  interface I {\n" +
			"  }\n" +
			"}"
		);
		this.workingCopy.makeConsistent(null);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {this.workingCopy.getParent()});
		SearchTests.SearchTypeNameRequestor requestor = new SearchTests.SearchTypeNameRequestor();
		new SearchEngine(new ICompilationUnit[] {this.workingCopy}).searchAllTypeNames(
			null,
			SearchPattern.R_EXACT_MATCH,
			null,
			SearchPattern.R_PATTERN_MATCH, // case insensitive
			TYPE,
			scope,
			requestor,
			IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
			null
		);
		assertSearchResults(
			"Unexpected all type names",
			"wc.Y\n" +
			"wc.Y$I",
			requestor);
	}

	/*
	 * Search all type names in working copies test (without reconciling working copies).
	 * (Regression test for bug 40793 Primary working copies: Type search does not find type in modified CU)
	 */
	public void testAllTypeNames2() throws CoreException {
		this.workingCopy.getBuffer().setContents(
			"package wc;\n" +
			"public class Y {\n" +
			"  interface I {\n" +
			"  }\n" +
			"}"
		);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {this.workingCopy.getParent()});
		SearchTests.SearchTypeNameRequestor requestor = new SearchTests.SearchTypeNameRequestor();
		new SearchEngine(new ICompilationUnit[] {this.workingCopy}).searchAllTypeNames(
			null,
			SearchPattern.R_EXACT_MATCH,
			null,
			SearchPattern.R_PATTERN_MATCH, // case insensitive
			TYPE,
			scope,
			requestor,
			IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
			null
		);
		assertSearchResults(
			"Unexpected all type names",
			"wc.Y\n" +
			"wc.Y$I",
			requestor);
	}

	/*
	 * Search all type names with a prefix in a primary working copy.
	 * (regression test for bug 44884 Wrong list displayed while code completion)
	 */
	public void testAllTypeNames3() throws CoreException {
		ICompilationUnit wc = getCompilationUnit("/JavaSearch/wc3/X44884.java");
		try {
			wc.becomeWorkingCopy(null);
			wc.getBuffer().setContents(
				"package wc3;\n" +
				"public class X44884 {\n" +
				"}\n" +
				"interface I {\n" +
				"}"
			);
			wc.makeConsistent(null);

			IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {wc.getParent()});
			SearchTests.SearchTypeNameRequestor requestor = new SearchTests.SearchTypeNameRequestor();
			new SearchEngine().searchAllTypeNames(
				"wc3".toCharArray(),
				SearchPattern.R_EXACT_MATCH,
				"X".toCharArray(),
				SearchPattern.R_PREFIX_MATCH, // case insensitive
				TYPE,
				scope,
				requestor,
				IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
				null
			);
			assertSearchResults(
				"Unexpected all type names",
				"wc3.X44884",
				requestor);
		} finally {
			wc.discardWorkingCopy();
		}
	}

	/*
	 * Search all type names with a prefix in a primary working copy (without reconciling it).
	 * (regression test for bug 44884 Wrong list displayed while code completion)
	 */
	public void testAllTypeNames4() throws CoreException {
		ICompilationUnit wc = getCompilationUnit("/JavaSearch/wc3/X44884.java");
		try {
			wc.becomeWorkingCopy(null);
			wc.getBuffer().setContents(
				"package wc3;\n" +
				"public class X44884 {\n" +
				"}\n" +
				"interface I {\n" +
				"}"
			);

			IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {wc.getParent()});
			SearchTests.SearchTypeNameRequestor requestor = new SearchTests.SearchTypeNameRequestor();
			new SearchEngine().searchAllTypeNames(
				"wc3".toCharArray(),
				SearchPattern.R_EXACT_MATCH,
				"X".toCharArray(),
				SearchPattern.R_PREFIX_MATCH, // case insensitive
				TYPE,
				scope,
				requestor,
				IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
				null
			);
			assertSearchResults(
				"Unexpected all type names",
				"wc3.X44884",
				requestor);
		} finally {
			wc.discardWorkingCopy();
		}
	}

	/**
	 * Bug 99915: [search] Open Type: not yet saved types not found if case-sensitve name is entered
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=99915"
	 */
	public void testAllTypeNamesBug99915() throws CoreException {
		this.workingCopy.getBuffer().setContents(
			"package wc;\n" +
			"public class X {\n" +
			"}\n"  +
			" class AAABBB {}\n" +
			" class BBBCCC {}\n"
		);
		this.workingCopy.makeConsistent(null);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {this.workingCopy.getParent()});
		SearchTests.SearchTypeNameRequestor requestor = new SearchTests.SearchTypeNameRequestor();
		new SearchEngine(new ICompilationUnit[] {this.workingCopy}).searchAllTypeNames(
			null,
			SearchPattern.R_EXACT_MATCH,
			"A*".toCharArray(),
			SearchPattern.R_PATTERN_MATCH, // case insensitive
			TYPE,
			scope,
			requestor,
			IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
			null
		);
		assertSearchResults(
			"Unexpected all type names",
			"wc.AAABBB",
			requestor);
	}

	/**
	 * Bug 98684: [search] Code assist shown inner types of unreleated project
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=98684"
	 */
	public void testAllTypeNamesBug98684() throws CoreException {
		try {
			IJavaProject[] projects = new IJavaProject[2];
			projects[0] = createJavaProject("P1");
			projects[1] = createJavaProject("P2");
			this.workingCopies = new ICompilationUnit[2];
			this.workingCopies[0] = getWorkingCopy("/P1/p1/A1.java",
				"package p1;\n" +
				"public class A1 {\n" +
				"	public static class A1Inner1 {}" +
				"	public static class A1Inner2 {}" +
				"}"
			);
			this.workingCopies[1] = getWorkingCopy("/P2/p2/A2.java",
				"package p2;\n" +
				"public class A2 {\n" +
				"	public static class A2Inner1 {}" +
				"	public static class A2Inner2 {}" +
				"}"
			);
			TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
			IJavaSearchScope scope = 	SearchEngine.createJavaSearchScope(new IJavaElement[] { projects[1] });
			new SearchEngine(this.workingCopies).searchAllTypeNames(
				null,
				SearchPattern.R_EXACT_MATCH,
				"A".toCharArray(),
				SearchPattern.R_PREFIX_MATCH,
				TYPE,
				scope,
				requestor,
				IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
				null
			);
			assertSearchResults(
				"Unexpected all type names",
				"p2.A2\n" +
				"p2.A2$A2Inner1\n" +
				"p2.A2$A2Inner2",
				requestor);
		}
		finally {
			deleteProject("P1");
			deleteProject("P2");
		}
	}

	/**
	 * Declaration of referenced types test.
	 * (Regression test for bug 5355 search: NPE in searchDeclarationsOfReferencedTypes  )
	 */
	public void testDeclarationOfReferencedTypes() throws CoreException {
		IMethod method = this.workingCopy.getType("X").createMethod(
			"public void foo() {\n" +
			"  X x = new X();\n" +
			"}",
			null,
			true,
			null);
	//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		searchDeclarationsOfReferencedTypes(
			method,
			this.resultCollector
		);
		assertSearchResults(
			"src/wc/X.java wc.X [X]",
			this.resultCollector);
	}

	/**
	 * Type declaration in a working copy test.
	 * A type is moved from one working copy to another.
	 */
	public void testMoveType() throws CoreException {

		// move type X from working copy in one package to a working copy in another package
		ICompilationUnit workingCopy1 = getCompilationUnit("JavaSearch", "src", "wc1", "X.java").getWorkingCopy(null);
		ICompilationUnit workingCopy2 = getCompilationUnit("JavaSearch", "src", "wc2", "Y.java").getWorkingCopy(null);

		try {
			workingCopy1.getType("X").move(workingCopy2, null, null, true, null);

			SearchEngine searchEngine = new SearchEngine(new ICompilationUnit[] {workingCopy1, workingCopy2});

			// type X should not be visible in old package
			IJavaSearchScope scope1 = SearchEngine.createJavaSearchScope(new IJavaElement[] {workingCopy1.getParent()});
	//		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();

			SearchPattern pattern = SearchPattern.createPattern(
				"X",
				TYPE,
				DECLARATIONS,
				SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE);
			searchEngine.search(
				pattern,
				new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
				scope1,
				this.resultCollector,
				null);
			assertEquals(
				"",
				this.resultCollector.toString());

			// type X should be visible in new package
			IJavaSearchScope scope2 = SearchEngine.createJavaSearchScope(new IJavaElement[] {workingCopy2.getParent()});
			this.resultCollector = new JavaSearchResultCollector();
			searchEngine.search(
				pattern,
				new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
				scope2,
				this.resultCollector,
				null);
			assertSearchResults(
				"src/wc2/Y.java wc2.X [X]",
				this.resultCollector);
		} finally {
			workingCopy1.discardWorkingCopy();
			workingCopy2.discardWorkingCopy();
		}
	}

	/**
	 * Type declaration in a working copy test.
	 * A type is removed from the working copy only.
	 */
	public void testRemoveType() throws CoreException {
		this.workingCopy.getType("X").delete(true, null);

		IJavaSearchScope scope =
			SearchEngine.createJavaSearchScope(
				new IJavaElement[] {this.workingCopy.getParent()});

		// type X should not be visible when working copy hides it
	//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		SearchPattern pattern = SearchPattern.createPattern(
			"X",
			TYPE,
			DECLARATIONS,
			SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE);
		new SearchEngine(new ICompilationUnit[] {this.workingCopy}).search(
			pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			scope,
			this.resultCollector,
			null);
		assertSearchResults(
			"",
			this.resultCollector);

		// ensure the type is still present in the compilation unit
		this.resultCollector = new JavaSearchResultCollector();
		new SearchEngine().search(
			pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			scope,
			this.resultCollector,
			null);
		assertSearchResults(
			"src/wc/X.java wc.X [X]",
			this.resultCollector);

	}

}
