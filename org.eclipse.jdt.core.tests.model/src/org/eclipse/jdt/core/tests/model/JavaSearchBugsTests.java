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
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.search.*;
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
	//	TESTS_NUMBERS = new int[] { 79860, 79803, 73336 };
	//	TESTS_RANGE = new int[] { 16, -1 };
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
		try {
			createFolder(new Path(fileName).removeLastSegments(1));
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return super.getWorkingCopy(fileName, source, true/*compute problems*/);
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
	 * Regression test for bug 77093: [search] No references found to method with member type argument
	 */
	public void testConstructorDeclarationBug77093() throws CoreException {
		IType type = getCompilationUnit("JavaSearchBugs/src/b77093/X.java").getType("X");
		IMethod method = type.getMethod("X", new String[] {"[[QZ;"});
		search(method, DECLARATIONS, getJavaSearchScopeBugs(), resultCollector);
		assertSearchResults(
			"src/b77093/X.java b77093.X(Z[][]) [X] EXACT_MATCH",
			resultCollector);
	}
	/**
	 * Regression test for bug 77093: [search] No references found to method with member type argument
	 */
	public void testConstructorReferenceBug77093() throws CoreException {
		IType type = getCompilationUnit("JavaSearchBugs/src/b77093/X.java").getType("X");
		IMethod method = type.getMethod("X", new String[] {"[[QZ;"});
		search(method, REFERENCES, getJavaSearchScopeBugs(), resultCollector);
		assertSearchResults(
			"src/b77093/X.java b77093.X() [this(new Z[10][])] EXACT_MATCH",
			resultCollector);
	}
	/**
	 * Regression test for bug 77388: [compiler] Reference to constructor includes space after closing parenthesis
	 */
	public void testConstructorReferenceBug77388() throws CoreException {
		IType type = getCompilationUnit("JavaSearchBugs/src/b77388/Test.java").getType("Test");
		IMethod method = type.getMethod("Test", new String[] {"I", "I"});
		search(method, REFERENCES, getJavaSearchScopeBugs(), resultCollector);
		assertSearchResults(
			"src/b77388/Test.java void b77388.Test.run() [new Test(1, 2)] EXACT_MATCH",
			resultCollector);
	}
	/**
	 * Regression test for bug 77093: [search] No references found to method with member type argument
	 */
	public void testFieldDeclarationBug77093() throws CoreException {
		IType type = getCompilationUnit("JavaSearchBugs/src/b77093/X.java").getType("X");
		IField field = type.getField("z_arrays");
		search(field, DECLARATIONS, getJavaSearchScopeBugs(), resultCollector);
		assertSearchResults(
			"src/b77093/X.java b77093.X.z_arrays [z_arrays] EXACT_MATCH",
			resultCollector);
	}
	/**
	 * Field reference test.
	 * (regression test for bug 73112: [Search] SearchEngine doesn't find all fields multiple field declarations
	 */
	public void testFieldReferenceBug73112a() throws CoreException {
	//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
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
			"src/b73112/A.java b73112.A.fieldA73112d [fieldA73112d] EXACT_MATCH",
			this.resultCollector);
	}
	public void testFieldReferenceBug73112b() throws CoreException {
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
			"src/b73112/B.java b73112.B.fieldB73112e [fieldB73112e] EXACT_MATCH",
			this.resultCollector);
	}
	/**
	 * Field reference test.
	 * (regression test for bug 78082: [1.5][search] FieldReferenceMatch in static import should not include qualifier
	 */
	public void testFieldReferenceBug78082() throws CoreException {
		IField field = getCompilationUnit("JavaSearchBugs", "src", "b78082", "M.java").getType("M").getField("VAL");
		search(
			field,
			ALL_OCCURRENCES,
			getJavaSearchScopeBugs(), 
			this.resultCollector);
		assertSearchResults(
			"src/b78082/M.java b78082.M.VAL [VAL] EXACT_MATCH\n" + 
			"src/b78082/XY.java [VAL] EXACT_MATCH\n" + 
			"src/b78082/XY.java b78082.XY.val [VAL] EXACT_MATCH\n" + 
			"src/b78082/XY.java b78082.XY.val2 [VAL] EXACT_MATCH",
			this.resultCollector);
	}
	/**
	 * Regression test for bug 77093: [search] No references found to method with member type argument
	 */
	public void testFieldReferenceBug77093() throws CoreException {
		IType type = getCompilationUnit("JavaSearchBugs/src/b77093/X.java").getType("X");
		IField field = type.getField("z_arrays");
		search(field, REFERENCES, getJavaSearchScopeBugs(), resultCollector);
		assertSearchResults(
			"src/b77093/X.java b77093.X(Z[][]) [z_arrays] EXACT_MATCH\n" + 
			"src/b77093/X.java void b77093.X.bar() [z_arrays] EXACT_MATCH\n" + 
			"src/b77093/X.java void b77093.X.bar() [z_arrays] EXACT_MATCH",
			resultCollector);
	}
	/**
	 * Regression test for bug 79267: [search] Refactoring of static generic member fails partially
	 */
	public void testFieldReferenceBug79267() throws CoreException {
		IType type = getCompilationUnit("JavaSearchBugs/src/b79267/Test.java").getType("Test");
		IField field = type.getField("BEFORE");
		search(field, REFERENCES, getJavaSearchScopeBugs(), resultCollector);
		field = type.getField("objectToPrimitiveMap");
		search(field, REFERENCES, getJavaSearchScopeBugs(), resultCollector);
		assertSearchResults(
			"src/b79267/Test.java b79267.Test.static {} [BEFORE] EXACT_MATCH\n" + 
			"src/b79267/Test.java b79267.Test.static {} [BEFORE] EXACT_MATCH\n" + 
			"src/b79267/Test.java b79267.Test.static {} [objectToPrimitiveMap] EXACT_MATCH",
			resultCollector);
	}
	/**
	 * Regression test for bug 77093: [search] No references found to method with member type argument
	 */
	public void testMethodDeclarationBug77093() throws CoreException {
		IType type = getCompilationUnit("JavaSearchBugs/src/b77093/X.java").getType("X");
		IMethod method = type.getMethod("foo", new String[] {"[QZ;"});
		search(method, DECLARATIONS, getJavaSearchScopeBugs(), resultCollector);
		assertSearchResults(
			"src/b77093/X.java void b77093.X.foo(Z[]) [foo] EXACT_MATCH",
			resultCollector);
	}
	/**
	 * Regression test for bug 41018: Method reference not found
	 * (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=41018)
	 */
	public void testMethodReferenceBug41018() throws CoreException {
		IType type = getCompilationUnit("JavaSearchBugs", "src", "b41018", "A.java").getType("A");
		IMethod method = type.getMethod("methodA", new String[] { "QClassB.InnerInterface;" });
	//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
			method, 
			REFERENCES, 
			getJavaSearchScopeBugs(), 
			this.resultCollector);
		assertSearchResults(
			"src/b41018/A.java void b41018.A.anotherMethod() [methodA(null)] EXACT_MATCH",
			this.resultCollector);
	}
	/**
	 * Regression test for bug 70827: [Search] wrong reference match to private method of supertype
	 * (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=70827)
	 */
	public void testMethodReferenceBug70827() throws CoreException {
		IType type = getCompilationUnit("JavaSearchBugs", "src", "b70827", "A.java").getType("A");
		IMethod method = type.getMethod("privateMethod", new String[] {});
		search(method, REFERENCES, getJavaSearchScopeBugs(), resultCollector);
		assertSearchResults("", resultCollector);
	}
	/**
	 * Regression test for bug 74776: [Search] Wrong search results for almost identical method
	 * (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=74776)
	 */
	public void testMethodReferenceBug74776() throws CoreException {
		IType type = getCompilationUnit("JavaSearchBugs", "src", "b74776", "A.java").getType("A");
		IMethod method = type.getMethod("foo", new String[] { "QRegion;" });
		search(method, REFERENCES, getJavaSearchScopeBugs(), resultCollector);
		assertSearchResults("", resultCollector);
	}
	/**
	 * Regression test for bug 72866: [search] references to endVisit(MethodInvocation) reports refs to endVisit(SuperMethodInvocation)
	 */
	public void testMethodReferenceBug72866() throws CoreException {
		IType type = getCompilationUnit("JavaSearchBugs/src/b72866/V.java").getType("V");
		IMethod method = type.getMethod("bar", new String[] {"QX;"});
		
		search(method, REFERENCES, getJavaSearchScopeBugs(), resultCollector);
		assertSearchResults(
			"src/b72866/X.java void b72866.X.foo(V) [bar(this)] EXACT_MATCH",
			resultCollector);
	}
	/**
	 * Regression test for bug 77093: [search] No references found to method with member type argument
	 */
	public void testMethodReferenceBug77093() throws CoreException {
		IType type = getCompilationUnit("JavaSearchBugs/src/b77093/X.java").getType("X");
		IMethod method = type.getMethod("foo", new String[] {"[QZ;"});
		search(method, REFERENCES, getJavaSearchScopeBugs(), resultCollector);
		assertSearchResults(
			"src/b77093/X.java void b77093.X.bar() [foo(z_arrays[i])] EXACT_MATCH",
			resultCollector);
	}
	/**
	 * bug 80890: [search] Strange search engine behaviour
	 */
	public void testMethodReferenceBug80890() throws CoreException, JavaModelException {
		ICompilationUnit workingCopy = null;
		try {
			workingCopy = getWorkingCopy("/JavaSearchBugs/src/bugs/A.java",
				"package bugs;\n" + 
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
			workingCopy.commitWorkingCopy(true, null);	// need to commit to index file
			IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new ICompilationUnit[] { workingCopy });
			// search for first and second method should both return 2 inaccurate matches
			IMethod method = workingCopy.getType("A").getMethods()[0];
			search(method, REFERENCES, scope, this.resultCollector);
			method = workingCopy.getType("A").getMethods()[1];
			search(method, REFERENCES, scope, this.resultCollector);
			assertSearchResults(
				"src/bugs/A.java void bugs.B1.bar1() [foo(null)] POTENTIAL_MATCH\n" + 
				"src/bugs/A.java void bugs.B2.bar2() [foo(null)] POTENTIAL_MATCH\n" + 
				"src/bugs/A.java void bugs.B1.bar1() [foo(null)] POTENTIAL_MATCH\n" + 
				"src/bugs/A.java void bugs.B2.bar2() [foo(null)] POTENTIAL_MATCH",
				this.resultCollector);
		}
		finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}
	/**
	 * Type declaration test.
	 * Test fix for bug 73696: searching only works for IJavaSearchConstants.TYPE, but not CLASS or INTERFACE
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=73696">73696</a>
	 */
	public void testTypeDeclarationBug73696() throws CoreException {
		IPackageFragment pkg = this.getPackageFragment("JavaSearchBugs", "src", "b73696");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {pkg});
	//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		
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

	/**
	 * Type reference for 1.5.
	 * Bug 73336: [1.5][search] Search Engine does not find type references of actual generic type parameters
	 * (see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=73336)
	 */
	public void testTypeReferenceBug73336() throws CoreException {
		IType type = getCompilationUnit("JavaSearchBugs/src/b73336/A.java").getType("A");
		
		search(type,
			REFERENCES,
			getJavaSearchScopeBugs("b73336", false),
			resultCollector);
		assertSearchResults(
			"src/b73336/AA.java b73336.AA [A] EXACT_MATCH\n" + 
			"src/b73336/B.java b73336.B [A] EXACT_MATCH\n" + 
			"src/b73336/B.java b73336.B [A] EXACT_MATCH\n" + 
			"src/b73336/C.java b73336.C [A] EXACT_MATCH\n" + 
			"src/b73336/C.java void b73336.C.foo() [A] EXACT_MATCH\n" + 
			"src/b73336/C.java void b73336.C.foo() [A] EXACT_MATCH",
			resultCollector);
	}
	public void testTypeReferenceBug73336b() throws CoreException {
		IType type = getCompilationUnit("JavaSearchBugs/src/b73336b/A.java").getType("A");
		
		search(type,
			REFERENCES,
			getJavaSearchScopeBugs("b73336b", false), 
			resultCollector);
		assertSearchResults(
			"src/b73336b/B.java b73336b.B [A] EXACT_MATCH\n" + 
			"src/b73336b/B.java b73336b.B [A] EXACT_MATCH\n" + 
			"src/b73336b/C.java b73336b.C [A] EXACT_MATCH\n" + 
			"src/b73336b/C.java b73336b.C [A] EXACT_MATCH\n" + 
			"src/b73336b/C.java b73336b.C [A] EXACT_MATCH\n" + 
			"src/b73336b/C.java b73336b.C() [A] EXACT_MATCH\n" + 
			"src/b73336b/C.java b73336b.C() [A] EXACT_MATCH",
			resultCollector);
	}
	// Verify that no NPE was raised on following case (which produces compiler error)
	public void testTypeReferenceBug73336c() throws CoreException {
		IType type = getCompilationUnit("JavaSearchBugs/src/b73336c/A.java").getType("A");
		
		search(type,
			REFERENCES,
			getJavaSearchScopeBugs("b73336c", false), 
			resultCollector);
		assertSearchResults(
				"src/b73336c/B.java b73336c.B [A] EXACT_MATCH\n" + 
				"src/b73336c/B.java b73336c.B [A] EXACT_MATCH\n" + 
				"src/b73336c/C.java b73336c.C [A] EXACT_MATCH\n" + 
				"src/b73336c/C.java b73336c.C [A] EXACT_MATCH\n" + 
				"src/b73336c/C.java b73336c.C [A] EXACT_MATCH",
			resultCollector);
	}
	
	/**
	 * Regression tests for bug.
	 * Bug 79860: [1.5][search] Search doesn't find type reference in type parameter bound
	 * (see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=79860)
	 */
	public void testTypeReferenceBug79860() throws CoreException {
		IType type = getCompilationUnit("JavaSearchBugs/src/b79860/X.java").getType("A");
		search(type, REFERENCES, getJavaSearchScopeBugs("b79860", false), resultCollector);
		assertSearchResults(
			"src/b79860/X.java b79860.X [A] EXACT_MATCH",
			resultCollector);
	}
	public void testTypeReferenceBug79860b() throws CoreException {
		search("I?", TYPE, REFERENCES, getJavaSearchScopeBugs("b79860", false), resultCollector);
		assertSearchResults(
			"src/b79860/Y.java b79860.Y [I1] EXACT_MATCH\n" + 
			"src/b79860/Y.java b79860.Y [I2] EXACT_MATCH\n" + 
			"src/b79860/Y.java b79860.Y [I3] EXACT_MATCH",
			resultCollector);
	}

	/**
	 * Type parameter with class name
	 * Regression tests for bug 79803: [1.5][search] Search for references to type A reports match for type variable A
	 */
	public void testTypeReferenceBug79803() throws CoreException {
		IType type = getCompilationUnit("JavaSearchBugs", "src", "b79803", "A.java").getType("A");
		search(type, REFERENCES, SearchPattern.R_CASE_SENSITIVE|SearchPattern.R_ERASURE_MATCH, getJavaSearchScopeBugs("b79803", false), this.resultCollector);
		assertSearchResults(
			"src/b79803/A.java b79803.A.pa [b79803.A] EXACT_MATCH\n" + 
			"src/b79803/A.java b79803.A.pa [b79803.A] EXACT_MATCH",
			this.resultCollector);
	}
	public void testTypeReferenceBug79803b() throws CoreException {
		search("A", TYPE, REFERENCES, getJavaSearchScopeBugs("b79803", false), this.resultCollector);
		assertSearchResults(
			"src/b79803/A.java b79803.A.a [A] EXACT_MATCH\n" + 
			"src/b79803/A.java b79803.A.pa [A] EXACT_MATCH\n" + 
			"src/b79803/A.java b79803.A.pa [A] EXACT_MATCH",
			this.resultCollector);
	}

	/**
	 * Regression tests for bug 80918: [1.5][search] ClassCastException when searching for references to binary type
	 */
	public void testTypeReferenceBug80918() throws CoreException {
		IType type = getClassFile("JavaSearchBugs", getExternalJCLPathString("1.5"), "java.lang", "Exception.class").getType();
		search(type, REFERENCES, SearchPattern.R_CASE_SENSITIVE|SearchPattern.R_ERASURE_MATCH, getJavaSearchScopeBugs("b79803", false), this.resultCollector);
		assertSearchResults(
			"", // do not expect to find anything, just verify that no CCE happens
			this.resultCollector);
	}
}
