/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contribution for bug 215139
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;

import junit.framework.Test;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.core.JavaModelStatus;

/**
 * Tests the Java search engine where results are JavaElements and source positions.
 */
public class JavaSearchTests extends AbstractJavaSearchTests {

public JavaSearchTests(String name) {
	super(name);
}
public static Test suite() {
	return buildModelTestSuite(JavaSearchTests.class);
}
// Use this static initializer to specify subset for tests
// All specified tests which do not belong to the class are skipped...
static {
//	org.eclipse.jdt.internal.core.search.BasicSearchEngine.VERBOSE = true;
//	TESTS_PREFIX =  "testPackageDeclaration";
//	TESTS_NAMES = new String[] { "testMethodReference17" };
//	TESTS_NUMBERS = new int[] { 113671 };
//	TESTS_RANGE = new int[] { 16, -1 };
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.core.tests.model.AbstractJavaModelTests#copyDirectory(java.io.File, java.io.File)
 */
protected void copyDirectory(File sourceDir, File targetDir) throws IOException {
	if (!targetDir.exists()) {
		super.copyDirectory(sourceDir, targetDir);
	}
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.core.tests.model.SuiteOfTestCases#setUpSuite()
 */
public void setUpSuite() throws Exception {
	super.setUpSuite();

	if (JAVA_PROJECT == null) {
		JAVA_PROJECT = setUpJavaProject("JavaSearch");
		setUpJavaProject("JavaSearch15", "1.5");
	}
}
public void tearDownSuite() throws Exception {
	if (JAVA_SEARCH_SUITES == null) {
		deleteProject("JavaSearch");
		deleteProject("JavaSearch15");
	} else {
		JAVA_SEARCH_SUITES.remove(getClass());
		if (JAVA_SEARCH_SUITES.size() == 0) {
			deleteProject("JavaSearch");
			deleteProject("JavaSearch15");
		}
	}

	super.tearDownSuite();
}
/**
 * Simple constructor declaration test.
 */
public void testConstructorDeclaration01() throws CoreException { // was testSimpleConstructorDeclaration
	IType type = getCompilationUnit("JavaSearch", "src", "p", "A.java").getType("A");
	IMethod constructor = type.getMethod("A", new String[] {"QX;"});
	search(
		constructor,
		DECLARATIONS,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults("src/p/A.java p.A(X) [A]", this.resultCollector);
}
/**
 * Constructor declaration in jar file test.
 */
public void testConstructorDeclaration02() throws CoreException { // was testConstructorDeclarationInJar
	IType type = getClassFile("JavaSearch", "MyJar.jar", "p1", "A.class").getType();
	IMethod method = type.getMethod("A", new String[] {"Ljava.lang.String;"});
	search(
		method,
		DECLARATIONS,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"MyJar.jar p1.A(java.lang.String) [No source]",
		this.resultCollector);
}
/**
 * Simple constructor reference test.
 */
public void testConstructorReference01() throws CoreException { // was testSimpleConstructorReference1
	IType type = getCompilationUnit("JavaSearch", "src", "p", "A.java").getType("A");
	IMethod constructor = type.getMethod("A", new String[] {"QX;"});
	search(
		constructor,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/Test.java void Test.main(String[]) [new p.A(y)]",
		this.resultCollector);
}
/**
 * Simple constructor reference test.
 */
public void testConstructorReference02() throws CoreException { // was testSimpleConstructorReference2
	search(
		"p.A(X)",
		CONSTRUCTOR,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/Test.java void Test.main(String[]) [new p.A(y)]",
		this.resultCollector);
}
/**
 * Constructor reference using an explicit constructor call.
 */
public void testConstructorReference03() throws CoreException { // was testConstructorReferenceExplicitConstructorCall1
	IType type = getCompilationUnit("JavaSearch", "src", "p", "Y.java").getType("Y");
	IMethod method = type.getMethod("Y", new String[] {"I"});
	search(
		method,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/p/Z.java p.Z(int) [super(i);]",
		this.resultCollector);
}
/**
 * Constructor reference using an explicit constructor call.
 */
public void testConstructorReference04() throws CoreException { // was testConstructorReferenceExplicitConstructorCall2
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");
	IMethod method = type.getMethod("X", new String[] {"I"});
	search(
		method,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/p/Y.java p.Y(int) [super(i);]\n" +
		"src/p/Y.java p.Y(boolean) [super(1);]",
		this.resultCollector);
}
/**
 * Constructor reference using an implicit constructor call.
 * (regression test for bug 23112 search: need a way to search for references to the implicit non-arg constructor)
 */
public void testConstructorReference05() throws CoreException { // was testConstructorReferenceImplicitConstructorCall1
	IType type = getCompilationUnit("JavaSearch", "src", "c7", "X.java").getType("X");
	IMethod method = type.getMethod("X", new String[] {});
	search(
		method,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/c7/Y.java c7.Y() [Y]",
		this.resultCollector);
}
/**
 * Constructor reference using an implicit constructor call.
 * (regression test for bug 23112 search: need a way to search for references to the implicit non-arg constructor)
 */
public void testConstructorReference06() throws CoreException { // was testConstructorReferenceImplicitConstructorCall2
	search(
		"c8.X()",
		CONSTRUCTOR,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/c8/Y.java c8.Y [Y]",
		this.resultCollector);
}
/**
 * Constructor reference in a field initializer.
 * (regression test for PR 1GKZ8VZ: ITPJCORE:WINNT - Search - did not find references to member constructor)
 */
public void testConstructorReference07() throws CoreException { // was testConstructorReferenceInFieldInitializer
	IType type = getCompilationUnit("JavaSearch", "src", "", "A.java").getType("A").getType("Inner");
	IMethod method = type.getMethod("Inner", new String[] {"I"});
	search(
		method,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/A.java A.field [new Inner(1)]\n" +
		"src/A.java A.field [new Inner(2)]",
		this.resultCollector);
}
/**
 * Constructor reference in case of default constructor of member type
 * (regression test for bug 43276)
 */
public void testConstructorReference08() throws CoreException { // was testConstructorReferenceDefaultConstructorOfMemberClass
	search(
		"c10.X.Inner()",
		CONSTRUCTOR,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/c10/X.java c10.B() [new X().super();]",
		this.resultCollector);
}
/*
 * Generic constructor reference
 */
public void testConstructorReference09() throws CoreException {
	IType type = getCompilationUnit("JavaSearch15/src/p2/X.java").getType("X");
	IMethod method = type.getMethod("X", new String[] {"QE;"});
	search(
		method,
		REFERENCES,
		ERASURE_RULE,
		getJavaSearchScope15(),
		this.resultCollector);
	assertSearchResults(
		"src/p2/Y.java Object p2.Y.foo() [new X<Object>(this)]",
		this.resultCollector);
}
/**
 * Constructor reference using an implicit constructor call.
 * (regression test for bug 23112 search: need a way to search for references to the implicit non-arg constructor)
 */
public void testConstructorReference10() throws CoreException { // was testConstructorReferenceImplicitConstructorCall2
	this.resultCollector.showSynthetic();
	search(
		"c11.A()",
		CONSTRUCTOR,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/c11/A.java c11.A1 [A1] SYNTHETIC\n" +
		"src/c11/A.java c11.A2() [A2] SYNTHETIC\n" +
		"src/c11/A.java c11.A3() [super();]",
		this.resultCollector);
}
/**
 * CoreException thrown during accept.
 * (regression test for PR #1G3UI7A)
 */
public void testCoreException() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");
	SearchRequestor result = new SearchRequestor() {
		public void acceptSearchMatch(SearchMatch match) throws CoreException {
			throw new CoreException(new JavaModelStatus(-1, "test"));
		}
	};
	try {
		search(
			type,
			DECLARATIONS,
			getJavaSearchScope(),
			result);
	} catch (CoreException e) {
		assertEquals("Unexpected CoreException has been thrown", "test", e.getStatus().getMessage());
		return;
	}
	assertTrue("CoreException should have been thrown", false);
}
/**
 * Declaration of accessed fields test.
 * (regression test for bug 6538 searchDeclarationsOf* incorrect)
 */
public void testDeclarationOfAccessedFields1() throws CoreException {
	IMethod method =
		getCompilationUnit("JavaSearch", "src", "a5", "B.java").
			getType("C").getMethod("i", new String[] {});
	searchDeclarationsOfAccessedFields(
		method,
		this.resultCollector
	);
	assertSearchResults(
		"",
		this.resultCollector);
}
/**
 * Declaration of accessed fields test.
 * (regression test for bug 6538 searchDeclarationsOf* incorrect)
 */
public void testDeclarationOfAccessedFields2() throws CoreException {
	IMethod method =
		getCompilationUnit("JavaSearch", "src", "a6", "A.java").
			getType("B").getMethod("m", new String[] {});
	searchDeclarationsOfAccessedFields(
		method,
		this.resultCollector
	);
	assertSearchResults(
		"src/a6/A.java a6.B.f [f]",
		this.resultCollector);
}
/**
 * Declaration of accessed fields test.
 * (regression test for bug 10386 NPE in MatchLocator.lookupType)
 */
public void testDeclarationOfAccessedFields3() throws CoreException {
	IMethod method =
		getCompilationUnit("JavaSearch", "src", "b6", "A.java").
			getType("A").getMethod("foo", new String[] {});
	searchDeclarationsOfAccessedFields(
		method,
		this.resultCollector
	);
	assertSearchResults(
		"src/b6/A.java b6.A.field [field]",
		this.resultCollector);
}
/**
 * Declaration of referenced types test.
 */
public void testDeclarationOfReferencedTypes01() throws CoreException {
	IMethod method =
		getCompilationUnit("JavaSearch", "src", "a3", "References.java").
			getType("References").getMethod("foo", new String[] {});
	searchDeclarationsOfReferencedTypes(
		method,
		this.resultCollector
	);
	assertSearchResults(
		"src/a3/X.java a3.X [X]\n" +
		"src/a3/Z.java a3.Z [Z]\n" +
		"src/a3/b/A.java a3.b.A [A]\n" +
		"src/a3/b/A.java a3.b.A$B$C [C]\n" +
		"src/a3/b/A.java a3.b.A$B [B]\n" +
		getExternalJCLPathString() + " java.lang.Object\n" +
		"src/a3/Y.java a3.Y [Y]\n" +
		"src/a3/b/B.java a3.b.B [B]",
		this.resultCollector);
}
/**
 * Declaration of referenced types test.
 * (Regression test for bug 6779 searchDeclarationsOfReferencedTyped - missing exception types)
 */
public void testDeclarationOfReferencedTypes02() throws CoreException {
	IMethod method =
		getCompilationUnit("JavaSearch", "src", "a7", "X.java").
			getType("X").getMethod("foo", new String[] {});
	searchDeclarationsOfReferencedTypes(
		method,
		this.resultCollector
	);
	assertSearchResults(
		"src/a7/X.java a7.MyException [MyException]",
		this.resultCollector);
}
/**
 * Declaration of referenced types test.
 * (Regression test for bug 12649 Missing import after move  )
 */
public void testDeclarationOfReferencedTypes03() throws CoreException {
	ICompilationUnit unit = getCompilationUnit("JavaSearch", "src", "c1", "A.java");
	searchDeclarationsOfReferencedTypes(
		unit,
		this.resultCollector
	);
	assertSearchResults(
		"src/c1/I.java c1.I [I]",
		this.resultCollector);
}
/**
 * Declaration of referenced types test.
 * (Regression test for bug 12649 Missing import after move  )
 */
public void testDeclarationOfReferencedTypes04() throws CoreException {
	ICompilationUnit unit = getCompilationUnit("JavaSearch", "src", "c1", "B.java");
	searchDeclarationsOfReferencedTypes(
		unit,
		this.resultCollector
	);
	assertSearchResults(
		"src/c1/I.java c1.I [I]",
		this.resultCollector);
}
/**
 * Declaration of referenced types test.
 * (Regression test for bug 18418 search: searchDeclarationsOfReferencedTypes reports import declarations)
 */
public void testDeclarationOfReferencedTypes05() throws CoreException {
	ICompilationUnit unit = getCompilationUnit("JavaSearch", "src", "c2", "A.java");
	searchDeclarationsOfReferencedTypes(
		unit,
		this.resultCollector
	);
	assertSearchResults(
		"src/c3/C.java c3.C [C]",
		this.resultCollector);
}
/**
 * Declaration of referenced types test.
 * (Regression test for bug 24934 Move top level doesn't optimize the imports[refactoring])
 */
public void testDeclarationOfReferencedTypes06() throws CoreException {
	ICompilationUnit unit = getCompilationUnit("JavaSearch", "src", "d1", "X.java");
	IType innerType = unit.getType("X").getType("Inner");
	searchDeclarationsOfReferencedTypes(
		innerType,
		this.resultCollector
	);
	assertSearchResults(
		"src/d2/Y.java d2.Y [Y]",
		this.resultCollector);
}
/**
 * Declaration of referenced types test.
 * (Regression test for bug 37438 searchenging NPE in searchDeclarationsOfReferencedTypes
)
 */
public void testDeclarationOfReferencedTypes07() throws CoreException {
	IPackageFragment pkg = getPackageFragment("JavaSearch", "src", "r7");
	boolean iae = false;
	try {
		searchDeclarationsOfReferencedTypes(
			pkg,
			this.resultCollector
		);
	} catch (IllegalArgumentException exception) {
		assertSearchResults("");
		iae = true;
	}
	assertTrue("We should get an IAE!", iae);
}

/**
 * Declaration of referenced types test.
 * (Regression test for bug 47787 IJavaSearchResultCollector.aboutToStart() and done() not called)
 */
public void testDeclarationOfReferencedTypes08() throws CoreException {
	ICompilationUnit unit = getCompilationUnit("JavaSearch", "src", "r7", "A.java");
	JavaSearchResultCollector result = new JavaSearchResultCollector() {
	    public void beginReporting() {
	        addLine("Starting search...");
        }
	    public void endReporting() {
	        addLine("Done searching.");
        }
	};
	searchDeclarationsOfReferencedTypes(unit, result);
	assertSearchResults(
		"Starting search...\n"+
		"Done searching.",
		result);
}

/**
 * Declaration of referenced types test.
 * (Regression test for bug 68862 [1.5] ClassCastException when moving a a java file
)
 */
public void testDeclarationOfReferencedTypes09() throws CoreException {
	ICompilationUnit cu = getCompilationUnit("JavaSearch15/src/p3/X.java");
	JavaSearchResultCollector result = new JavaSearchResultCollector() {
	    public void beginReporting() {
	        addLine("Starting search...");
        }
	    public void endReporting() {
	        addLine("Done searching.");
        }
	};
	searchDeclarationsOfReferencedTypes(
		cu,
		result
	);
	assertSearchResults(
		"Starting search...\n" +
		getExternalJCLPathString("1.5") + " java.lang.Object\n" +
		"Done searching.",
		result);
}
/**
 * Simple declarations of sent messages test.
 */
public void testDeclarationsOfSentMessages01() throws CoreException { // was testSimpleDeclarationsOfSentMessages
	ICompilationUnit cu = getCompilationUnit("JavaSearch", "src", "", "Test.java");
	searchDeclarationsOfSentMessages(
		cu,
		this.resultCollector);
	assertSearchResults(
		"src/p/X.java void p.X.foo(int, String, X) [foo(int i, String s, X x)]\n" +
		"src/p/Y.java void p.Y.bar() [bar()]\n" +
		"src/p/Z.java void p.Z.foo(int, String, X) [foo(int i, String s, X x)]\n" +
		"src/p/A.java void p.A.foo(int, String, X) [foo(int i, String s, X x)]",
		this.resultCollector);
}

/**
 * Declaration of sent messages test.
 * (regression test for bug 6538 searchDeclarationsOf* incorrect)
 */
public void testDeclarationsOfSentMessages02() throws CoreException { // was testDeclarationOfSentMessages
	IMethod method =
		getCompilationUnit("JavaSearch", "src", "a5", "B.java").
			getType("C").getMethod("i", new String[] {});
	searchDeclarationsOfSentMessages(
		method,
		this.resultCollector
	);
	assertSearchResults(
		"",
		this.resultCollector);
}

/*
 * Ensures that a method declaration in an external library folder can be found
 */
public void testExternalFolder1() throws CoreException {
	try {
		createExternalFolder("externalLib");
		Util.compile(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"  public void foo() {\n" +
				"  }\n" +
				"}"
			},
			new HashMap(),
			getExternalResourcePath("externalLib")
		);
		createJavaProject("P", new String[0], new String[] {getExternalResourcePath("externalLib")}, "");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[] {getJavaProject("P")});
		search("foo", METHOD, DECLARATIONS, scope);
		assertSearchResults(
			getExternalPath() + "externalLib void p.X.foo()",
			this.resultCollector);
	} finally {
		deleteProject("P");
		deleteExternalResource("externalLib");
	}
}

/*
 * Ensures that search all type names returns the types in an external library folder
 */
public void testExternalFolder2() throws CoreException {
	try {
		createExternalFolder("externalLib");
		Util.compile(
			new String[] {
				"p/ExternalType.java",
				"package p;\n" +
				"public class ExternalType {\n" +
				"}"
			},
			new HashMap(),
			getExternalResourcePath("externalLib")
		);
		createJavaProject("P", new String[0], new String[] {getExternalResourcePath("externalLib")}, "");

		TypeNameMatchCollector collector = new TypeNameMatchCollector();
		new SearchEngine(this.workingCopies).searchAllTypeNames(
			null,
			SearchPattern.R_EXACT_MATCH,
			"ExternalType".toCharArray(),
			SearchPattern.R_EXACT_MATCH,
			TYPE,
			SearchEngine.createJavaSearchScope(new IJavaProject[] {getJavaProject("P")}),
			collector,
			IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
			null
		);
		assertSearchResults(
			"ExternalType (not open) [in ExternalType.class [in p [in "+ getExternalPath() + "externalLib]]]",
			collector
		);
	} finally {
		deleteProject("P");
		deleteExternalResource("externalLib");
	}

}

/*
 * Ensures that search all type names returns the types in an external library folder
 * when using a workspace scope
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=229304 )
 */
public void testExternalFolder3() throws CoreException {
	try {
		createExternalFolder("externalLib");
		Util.compile(
			new String[] {
				"p/ExternalType229304.java",
				"package p;\n" +
				"public class ExternalType229304 {\n" +
				"}"
			},
			new HashMap(),
			getExternalResourcePath("externalLib")
		);
		createJavaProject("P", new String[0], new String[] {getExternalResourcePath("externalLib")}, "");

		TypeNameMatchCollector collector = new TypeNameMatchCollector();
		new SearchEngine(this.workingCopies).searchAllTypeNames(
			null,
			SearchPattern.R_EXACT_MATCH,
			"ExternalType229304".toCharArray(),
			SearchPattern.R_EXACT_MATCH,
			TYPE,
			SearchEngine.createWorkspaceScope(),
			collector,
			IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
			null
		);
		assertSearchResults(
			"ExternalType229304 (not open) [in ExternalType229304.class [in p [in "+ getExternalPath() + "externalLib]]]",
			collector
		);
	} finally {
		deleteProject("P");
		deleteExternalResource("externalLib");
	}

}

/*
 * Ensures that a method declaration in an external ZIP archive can be found
 */
public void testZIPArchive1() throws Exception {
	try {
		createJar(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"  public void foo() {\n" +
				"  }\n" +
				"}"
			},
			getExternalResourcePath("externalLib.abc"));
		IJavaProject p = createJavaProject("P", new String[0], new String[] {getExternalResourcePath("externalLib.abc")}, "");
		refreshExternalArchives(p);

		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[] {getJavaProject("P")});
		search("foo", METHOD, DECLARATIONS, scope);
		assertSearchResults(
			getExternalPath() + "externalLib.abc void p.X.foo()",
			this.resultCollector);
	} finally {
		deleteExternalResource("externalLib.abc");
		deleteProject("P");
	}
}

/*
 * Ensures that search all type names returns the types in an external ZIP archive
 */
public void testZIPArchive2() throws Exception {
	try {
		IJavaProject p = createJavaProject("P", new String[0], new String[] {getExternalResourcePath("externalLib.abc")}, "");
		createJar(
			new String[] {
				"p/ExternalType.java",
				"package p;\n" +
				"public class ExternalType {\n" +
				"}"
			},
			getExternalResourcePath("externalLib.abc"));
		refreshExternalArchives(p);

		TypeNameMatchCollector collector = new TypeNameMatchCollector();
		new SearchEngine(this.workingCopies).searchAllTypeNames(
			null,
			SearchPattern.R_EXACT_MATCH,
			"ExternalType".toCharArray(),
			SearchPattern.R_EXACT_MATCH,
			TYPE,
			SearchEngine.createJavaSearchScope(new IJavaProject[] {getJavaProject("P")}),
			collector,
			IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
			null
		);
		assertSearchResults(
			"ExternalType (not open) [in ExternalType.class [in p [in "+ getExternalPath() + "externalLib.abc]]]",
			collector
		);
	} finally {
		deleteExternalResource("externalLib.abc");
		deleteProject("P");
	}

}

/**
 * Simple field declaration test.
 */
public void testFieldDeclaration01() throws CoreException { // was testSimpleFieldDeclaration
	IType type = getCompilationUnit("JavaSearch", "src", "p", "A.java").getType("A");
	IField field = type.getField("x");
	search(
		field,
		DECLARATIONS,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/p/A.java p.A.x [x]",
		this.resultCollector);
}
/**
 * Field declaration in jar file test.
 */
public void testFieldDeclaration02() throws CoreException { // was testFieldDeclarationInJar
	IType type = getClassFile("JavaSearch", "MyJar.jar", "p1", "A.class").getType();
	IField field = type.getField("field");
	search(
		field,
		DECLARATIONS,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"MyJar.jar p1.A.field [No source]",
		this.resultCollector);
}


/**
 * Field declaration with array type test.
 * (regression test for PR 1GKEG73: ITPJCORE:WIN2000 - search (136): missing field declaration)
 */
public void testFieldDeclaration03() throws CoreException { // was testFieldDeclarationArrayType
	IType type = getCompilationUnit("JavaSearch", "src", "", "B.java").getType("B");
	IField field = type.getField("open");
	search(
		field,
		DECLARATIONS,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/B.java B.open [open]",
		this.resultCollector);
}
/**
 * Field declaration with wild card test.
 * (regression test for bug 21763 Problem in Java search [search]  )
 */
public void testFieldDeclaration04() throws CoreException { // was testFieldDeclarationWithWildCard
	search(
		"class*path",
		FIELD,
		DECLARATIONS,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/c5/Test.java c5.Test.class_path [class_path]",
		this.resultCollector);
}
/**
 * Field reference test.
 * (regression test for bug #3433 search: missing field occurrecnces (1GKZ8J6))
 */
public void testFieldReference01() throws CoreException { // was testFieldReference
	IType type = getCompilationUnit("JavaSearch", "src", "p8", "A.java").getType("A");
	IField field = type.getField("g");
	search(
		field,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/p8/A.java void p8.A.m() [g]\n" +
		"src/p8/A.java void p8.B.m() [g]",
		this.resultCollector);
}
/**
 * Field reference test.
 * (regression test for PR 1GK8TXE: ITPJCORE:WIN2000 - search: missing field reference)
 */
public void testFieldReference02() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p9", "X.java").getType("X");
	IField field = type.getField("f");
	search(
		field,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/p9/X.java void p9.X.m() [f]",
		this.resultCollector);
}
/**
 * Field reference test.
 * (regression test for bug 5821 Refactor > Rename renames local variable instead of member in case of name clash  )
 */
public void testFieldReference03() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "q8", "EclipseTest.java").getType("EclipseTest");
	IField field = type.getField("test");
	this.resultCollector.showPotential(false);
	search(field, REFERENCES, getJavaSearchScope());
	assertSearchResults(
		"src/q8/EclipseTest.java void q8.EclipseTest.main(String[]) [test]"
	);
}
/**
 * Field reference test.
 * (regression test for bug 5923 Search for "length" field refs finds [].length)
 */
public void testFieldReference04() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "a2", "X.java").getType("X");
	IField field = type.getField("length");
	search(
		field,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/a2/X.java void a2.X.foo() [length]",
		this.resultCollector);
}
/**
 * Field reference test.
 * (regression test for bug 7987 Field reference search should do lookup in 1.4 mode)
 */
public void testFieldReference05() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "b1", "A.java").getType("A");
	IField field = type.getField("x");

	// Set 1.4 compliance level (no constant yet)
	Hashtable options = JavaCore.getOptions();
	String currentOption = (String)options.get("org.eclipse.jdt.core.compiler.compliance");
	options.put("org.eclipse.jdt.core.compiler.compliance", "1.4");
	JavaCore.setOptions(options);

	try {
		search(
			field,
			REFERENCES,
			getJavaSearchScope(),
			this.resultCollector);
		assertSearchResults(
			"src/b1/B.java void b1.B.foo() [x]",
			this.resultCollector);
	} finally {
		// Restore compliance level
		options.put("org.eclipse.jdt.core.compiler.compliance", currentOption);
		JavaCore.setOptions(options);
	}
}
/**
 * Field reference test.
 * (regression test for bug 20693 Finding references to variables does not find all occurances)
 */
public void testFieldReference06() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "c4", "X.java").getType("X");
	IField field = type.getField("x");
	search(
		field,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/c4/X.java int c4.X.foo() [x]",
		this.resultCollector);
}
/**
 * Field reference test.
 * (regression test for bug 61017 Refactoring - test case that results in uncompilable source)
 */
public void testFieldReference07() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "s5", "A.java").getType("A");
	IField field = type.getField("b");
	search(
		field,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/s5/A.java void s5.A.method() [b]",
		this.resultCollector);
}
/**
 * Simple field reference test.
 */
public void testFieldReference08() throws CoreException { // was testSimpleFieldReference
	IType type = getCompilationUnit("JavaSearch", "src", "p", "A.java").getType("A");
	IField field = type.getField("x");
	search(
		field,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/Test.java void Test.main(String[]) [x]\n" +
		"src/p/A.java p.A(X) [x]",
		this.resultCollector);
}
/**
 * Simple field read access reference test.
 */
public void testFieldReference09() throws CoreException { // was testSimpleReadFieldReference
	IType type = getCompilationUnit("JavaSearch", "src", "p", "A.java").getType("A");
	IField field = type.getField("x");
	search(
		field,
		READ_ACCESSES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/Test.java void Test.main(String[]) [x]",
		this.resultCollector);
}
/**
 * Simple write field access reference test.
 */
public void testFieldReference10() throws CoreException { // was testSimpleWriteFieldReference
	IType type = getCompilationUnit("JavaSearch", "src", "p", "A.java").getType("A");
	IField field = type.getField("x");
	search(
		field,
		WRITE_ACCESSES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/p/A.java p.A(X) [x]",
		this.resultCollector);
}
/**
 * Multiple field references in one ast test.
 * (regression test for PR 1GD79XM: ITPJCORE:WINNT - Search - search for field references - not all found)
 */
public void testFieldReference11() throws CoreException { // was testMultipleFieldReference
	IType type = getCompilationUnit("JavaSearch", "src", "p5", "A.java").getType("A");
	IField field = type.getField("x");
	search(
		field,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/p5/A.java void p5.A.k() [x]\n" +
		"src/p5/A.java void p5.A.k() [x]\n" +
		"src/p5/A.java void p5.A.k() [x]",
		this.resultCollector);
}
/**
 * Static field reference test.
 * (regression test for PR #1G2P5EP)
 */
public void testFieldReference12() throws CoreException { // was testStaticFieldReference
	IType type = getCompilationUnit("JavaSearch", "src", "p", "A.java").getType("A");
	IField field = type.getField("DEBUG");
	search(
		field,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/p/A.java void p.A.foo() [DEBUG]",
		this.resultCollector);
}
/**
 * Field reference in inner class test.
 * (regression test for PR 1GL11J6: ITPJCORE:WIN2000 - search: missing field references (nested types))
 */
public void testFieldReference13() throws CoreException { // was testFieldReferenceInInnerClass
	IType type = getCompilationUnit("JavaSearch", "src", "", "O.java").getType("O");
	IField field = type.getField("y");
	search(
		field,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/O.java void O$I.y() [y]",
		this.resultCollector);
}
/**
 * Field reference in anonymous class test.
 * (regression test for PR 1GL12XE: ITPJCORE:WIN2000 - search: missing field references in inner class)
 */
public void testFieldReference14() throws CoreException { // was testFieldReferenceInAnonymousClass
	IType type = getCompilationUnit("JavaSearch", "src", "", "D.java").getType("D");
	IField field = type.getField("h");
	search(
		field,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/D.java void void D.g():<anonymous>#1.run() [h]",
		this.resultCollector);
}
/**
 * Field reference through subclass test.
 * (regression test for PR 1GKB9YH: ITPJCORE:WIN2000 - search for field refs - incorrect results)
 */
public void testFieldReference15() throws CoreException { // was testFieldReferenceThroughSubclass
	IType type = getCompilationUnit("JavaSearch", "src", "p6", "A.java").getType("A");
	IField field = type.getField("f");
	search(
		field,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/p6/A.java void p6.A.m() [f]\n" +
		"src/p6/A.java void p6.B.m() [f]\n" +
		"src/p6/A.java void p6.B.m() [f]",
		this.resultCollector);

	type = getCompilationUnit("JavaSearch", "src", "p6", "A.java").getType("AA");
	field = type.getField("f");
	this.resultCollector = new JavaSearchResultCollector();
	search(
		field,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/p6/A.java void p6.B.m() [f]",
		this.resultCollector);

}
/**
 * Read and write access reference in compound expression test.
 * (regression test for bug 6158 Search - Prefix and postfix expression not found as write reference)
 */
public void testFieldReference16() throws CoreException { // was testReadWriteFieldReferenceInCompoundExpression
	IType type = getCompilationUnit("JavaSearch", "src", "a4", "X.java").getType("X");
	IField field = type.getField("field");

	// Read reference
	search(
		field,
		READ_ACCESSES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/a4/X.java void a4.X.foo() [field]",
		this.resultCollector);

	// Write reference
	this.resultCollector = new JavaSearchResultCollector();
	search(
		field,
		WRITE_ACCESSES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/a4/X.java void a4.X.foo() [field]",
		this.resultCollector);
}
/**
 * Write access reference in a qualified name reference test.
 * (regression test for bug 7344 Search - write acces give wrong result)
 */
public void testFieldReference17() throws CoreException { // was testReadWriteAccessInQualifiedNameReference
	IType type = getCompilationUnit("JavaSearch", "src", "a8", "A.java").getType("A");
	IField field = type.getField("a");
	search(
		field,
		WRITE_ACCESSES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"",
		this.resultCollector);
}
/**
 * Field reference in brackets test.
 * (regression test for bug 23329 search: incorrect range for type references in brackets)
 */
public void testFieldReference18() throws CoreException { // was testFieldReferenceInBrackets
	IType type = getCompilationUnit("JavaSearch", "src", "s3", "A.java").getType("A");
	IField field = type.getField("field");
	search(
		field,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/s3/A.java int s3.A.bar() [field]",
		this.resultCollector);
}

/**
 * Accurate field reference test.
 */
public void testFieldReference19() throws CoreException { // was testAccurateFieldReference1
	search(
		"d6.X.CONSTANT",
		FIELD,
		REFERENCES,
		SearchEngine.createJavaSearchScope(new IJavaElement[] {
			getPackageFragment("JavaSearch", "src", "d6")
		}),
		this.resultCollector);
	assertSearchResults(
		"src/d6/Y.java d6.Y.T [CONSTANT]",
		this.resultCollector);
}
/**
 * Field reference inside/outside doc comment.
 */
public void testFieldReference20() throws CoreException { // was testFieldReferenceInOutDocComment
	IType type = getCompilationUnit("JavaSearch", "src", "s4", "X.java").getType("X");
	IField field = type.getField("x");
	this.resultCollector.showInsideDoc();
	search(field, REFERENCES, getJavaSearchScope(), this.resultCollector);
	assertSearchResults(
		"src/s4/X.java int s4.X.foo() [x] OUTSIDE_JAVADOC\n" +
		"src/s4/X.java void s4.X.bar() [x] INSIDE_JAVADOC",
		this.resultCollector);
}
/**
 * Interface implementors test.
 */
public void testInterfaceImplementors1() throws CoreException { // was testInterfaceImplementors
	// implementors of an interface
	IType type = getCompilationUnit("JavaSearch", "src", "p", "I.java").getType("I");
	search(
		type,
		IMPLEMENTORS,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/InterfaceImplementors.java InterfaceImplementors [p.I]\n" +
		"src/p/X.java p.X [I]",
		this.resultCollector);

	/**
	 * Implementors of a class should now give a match
	 * Bug 1G5HBQA: "ITPJUI:WINNT - Search - search for implementors of a class finds subclasses" is cancelled
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=124645"
	 */
	type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");
	this.resultCollector = new JavaSearchResultCollector();
	search(
		type,
		IMPLEMENTORS,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/p/Y.java p.Y [X]",
		this.resultCollector);
}
/**
 * Interface implementors test.
 * (regression test for bug 22102 Not all implementors found for IPartListener)
 */
public void testInterfaceImplementors2() throws CoreException {
	// implementors of an interface
	IType type = getCompilationUnit("JavaSearch", "src", "r2", "I.java").getType("I");
	search(
		type,
		IMPLEMENTORS,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/r2/X.java r2.X.field:<anonymous>#1 [I]",
		this.resultCollector);
}
/*
 * Local variable declaration test.
 * (SingleNameReference)
 */
public void testLocalVariableDeclaration1() throws CoreException {
	ILocalVariable localVar = getLocalVariable("/JavaSearch/src/f1/X.java", "var1 = 1;", "var1");
	search(
		localVar,
		DECLARATIONS,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/f1/X.java void f1.X.foo1().var1 [var1]",
		this.resultCollector);
}
/*
 * Local variable declaration test.
 * (QualifiedNameReference)
 */
public void testLocalVariableDeclaration2() throws CoreException {
	ILocalVariable localVar = getLocalVariable("/JavaSearch/src/f1/X.java", "var2 = new X();", "var2");
	search(
		localVar,
		DECLARATIONS,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/f1/X.java void f1.X.foo2().var2 [var2]",
		this.resultCollector);
}
/**
 * @bug 207257: [search] SearchEngine returns incorrectly typed SearchMatch when searching for local variable declarations
 * @test The accepted match should be a {@link LocalVariableDeclarationMatch}
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=207257"
 */
public void testLocalVariableDeclaration_Bug207257() throws CoreException {
	ILocalVariable localVar = getLocalVariable("/JavaSearch/src/f1/X.java", "var1 = 1;", "var1");
	search(
		localVar,
		DECLARATIONS,
		getJavaSearchScope(),
		this.resultCollector);
	assertEquals("Invalid Search Match class", LocalVariableDeclarationMatch.class, this.resultCollector.match.getClass());
}
/*
 * Local variable occurrences test.
 * (SingleNameReference)
 */
public void testLocalVariableOccurrences1() throws CoreException {
	ILocalVariable localVar = getLocalVariable("/JavaSearch/src/f1/X.java", "var1 = 1;", "var1");
	search(
		localVar,
		ALL_OCCURRENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/f1/X.java void f1.X.foo1().var1 [var1]\n" +
		"src/f1/X.java void f1.X.foo1() [var1]",
		this.resultCollector);
}
/*
 * Local variable occurences test.
 * (QualifiedNameReference)
 */
public void testLocalVariableOccurrences2() throws CoreException {
	ILocalVariable localVar = getLocalVariable("/JavaSearch/src/f1/X.java", "var2 = new X();", "var2");
	search(
		localVar,
		ALL_OCCURRENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/f1/X.java void f1.X.foo2().var2 [var2]\n" +
		"src/f1/X.java void f1.X.foo2() [var2]",
		this.resultCollector);
}
/*
 * Local variable reference test.
 * (SingleNameReference)
 */
public void testLocalVariableReference1() throws CoreException {
	ILocalVariable localVar = getLocalVariable("/JavaSearch/src/f1/X.java", "var1 = 1;", "var1");
	search(
		localVar,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/f1/X.java void f1.X.foo1() [var1]",
		this.resultCollector);
}
/*
 * Local variable reference test.
 * (QualifiedNameReference)
 */
public void testLocalVariableReference2() throws CoreException {
	ILocalVariable localVar = getLocalVariable("/JavaSearch/src/f1/X.java", "var2 = new X();", "var2");
	search(
		localVar,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/f1/X.java void f1.X.foo2() [var2]",
		this.resultCollector);
}
/*
 * Local variable reference test.
 * (regression test for bug 48725 Cannot search for local vars in jars.)
 */
public void testLocalVariableReference3() throws CoreException {
    IClassFile classFile = getClassFile("JavaSearch", "test48725.jar", "p48725", "X.class");
	ILocalVariable localVar = (ILocalVariable) codeSelect(classFile, "local = 1;", "local")[0];
	search(
		localVar,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"test48725.jar int p48725.<anonymous>.bar()",
		this.resultCollector);
}
/**
 * Simple method declaration test.
 */
public void testMethodDeclaration01() throws CoreException { // was testSimpleMethodDeclaration
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");
	IMethod method = type.getMethod("foo", new String[] {"I", "QString;", "QX;"});

	search(
		method,
		DECLARATIONS,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/p/X.java void p.X.foo(int, String, X) [foo]\n" +
		"src/p/Z.java void p.Z.foo(int, String, X) [foo]",
		this.resultCollector);
}
/**
 * Method declaration test.
 * (regression test for bug 38568 Search for method declarations fooled by array types)
 */
public void testMethodDeclaration02() throws CoreException { // was testMethodDeclaration
	IType type = getCompilationUnit("JavaSearch", "src", "e2", "X.java").getType("X");

	search(
		"foo(String, String)",
		METHOD,
		DECLARATIONS,
		SearchEngine.createJavaSearchScope(new IJavaElement[] {type}),
		this.resultCollector);
	assertSearchResults(
		"src/e2/X.java void e2.X.foo(String, String) [foo]",
		this.resultCollector);
}


/**
 * Inner method declaration test.
 */
public void testMethodDeclaration03() throws CoreException { // was testInnerMethodDeclaration
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X").getType("Inner");
	IMethod method = type.getMethod("foo", new String[] {});

	search(
		method,
		DECLARATIONS,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/p/X.java String p.X$Inner.foo() [foo]",
		this.resultCollector);
}
/**
 * Method declaration in hierarchy test.
 * Explicitly request behavior pre https://bugs.eclipse.org/bugs/show_bug.cgi?id=215139
 */
public void testMethodDeclaration04() throws CoreException { // was testMethodDeclarationInHierarchyScope1
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");

	search(
		"foo",
		METHOD,
		DECLARATIONS,
		SearchEngine.createHierarchyScope(null, type, false, true, null),
		this.resultCollector);
	assertSearchResults(
		"src/p/X.java void p.X.foo(int, String, X) [foo]\n" +
		"src/p/Z.java void p.Z.foo(int, String, X) [foo]",
		this.resultCollector);
}
/**
 * Method declaration in hierarchy test.
 * After https://bugs.eclipse.org/bugs/show_bug.cgi?id=215139 result contains more types.
 */
public void testMethodDeclaration04a() throws CoreException { // was testMethodDeclarationInHierarchyScope1
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");

	search(
		"foo",
		METHOD,
		DECLARATIONS,
		SearchEngine.createHierarchyScope(type),
		this.resultCollector);
	assertSearchResults(
		"src/p/X.java void p.X.foo(int, String, X) [foo]\n" +
		"src/p/X.java String p.X$Inner.foo() [foo]\n" +
		"src/p/Z.java void p.Z.foo(int, String, X) [foo]",
		this.resultCollector);
}
/**
 * Method declaration in hierarchy that contains elements in external jar.
 * (regression test for PR #1G2E4F1)
 */
public void testMethodDeclaration05() throws CoreException { // was testMethodDeclarationInHierarchyScope2
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");
	IMethod method = type.getMethod("foo", new String[] {"I", "QString;", "QX;"});

	search(
		method,
		DECLARATIONS,
		SearchEngine.createHierarchyScope(type),
		this.resultCollector);
	assertSearchResults(
		"src/p/X.java void p.X.foo(int, String, X) [foo]\n" +
		"src/p/Z.java void p.Z.foo(int, String, X) [foo]",
		this.resultCollector);
}
/**
 * Method declaration in hierarchy on a secondary type.
 */
public void testMethodDeclaration06() throws CoreException { // was testMethodDeclarationInHierarchyScope3
	IType type = getCompilationUnit("JavaSearch", "src", "d3", "A.java").getType("B");
	IMethod method = type.getMethod("foo", new String[] {});

	search(
		method,
		DECLARATIONS,
		SearchEngine.createHierarchyScope(type),
		this.resultCollector);
	assertSearchResults(
		"src/d3/A.java void d3.B.foo() [foo]",
		this.resultCollector);
}
/**
 * Method declaration in package test.
 * (regression tets for PR #1G2KA97)
 */
public void testMethodDeclaration07() throws CoreException { // was testMethodDeclarationInPackageScope
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {type.getPackageFragment()});

	search(
		"main(String[])",
		METHOD,
		DECLARATIONS,
		scope,
		this.resultCollector);
	assertSearchResults(
		"src/p/A.java void p.A.main(String[]) [main]",
		this.resultCollector);
}
/**
 * Method declaration in jar file test.
 */
public void testMethodDeclaration08() throws CoreException { // was testMethodDeclarationInJar
	IType type = getClassFile("JavaSearch", "MyJar.jar", "p1", "A.class").getType();
	IMethod method = type.getMethod("foo", new String[] {"Ljava.lang.String;"});

	search(
		method,
		DECLARATIONS,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"MyJar.jar boolean p1.A.foo(java.lang.String) [No source]",
		this.resultCollector);
}
/**
 * Method declaration in field initialzer.
 * (regression test for bug 24346 Method declaration not found in field initializer  )
 */
public void testMethodDeclaration09() throws CoreException { // was testMethodDeclarationInInitializer

	search(
		"foo24346",
		METHOD,
		DECLARATIONS,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/c6/X.java void c6.X.x:<anonymous>#1.foo24346() [foo24346]",
		this.resultCollector);
}
/*
 * Method declaration with a missing return type.
 * (regression test for bug 43080 NPE when searching in CU with incomplete method declaration)
 */
public void testMethodDeclaration10() throws CoreException { // was testMethodDeclarationNoReturnType
	IType type = getCompilationUnit("JavaSearch", "src", "e8", "A.java").getType("A");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {type.getPackageFragment()});

	search(
		"m() int",
		METHOD,
		DECLARATIONS,
		scope,
		this.resultCollector);
	assertSearchResults(
		"src/e8/A.java void e8.A.m() [m]",
		this.resultCollector);
}
/**
 * Method declaration with source folder as java search scope.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=92210"
 */
public void testMethodDeclaration11() throws CoreException {
	ICompilationUnit unit = getCompilationUnit("JavaSearch", "otherSrc()", "", "X92210.java");
	assertNotNull("Should have found an unit", unit);
	IJavaElement root = unit.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
	assertNotNull("Should have found package fragment root", root);
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {root});

	search(
		"foo",
		METHOD,
		DECLARATIONS,
		scope,
		this.resultCollector);
	assertSearchResults(
		"otherSrc()/X92210.java void X92210.foo() [foo]",
		this.resultCollector);
}
/**
 * Method reference test.
 * (regression test for bug 5068 search: missing method reference)
 */
public void testMethodReference01() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "q5", "AQ.java").getType("I");
	IMethod method = type.getMethod("k", new String[] {});

	search(
		method,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/q5/AQ.java void q5.T.m() [k()]",
		this.resultCollector);
}
/**
 * Method reference test.
 * (regression test for bug 5069 search: method reference in super missing)
 */
public void testMethodReference02() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "q6", "CD.java").getType("AQ");
	IMethod method = type.getMethod("k", new String[] {});

	search(
		method,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/q6/CD.java void q6.AQE.k() [k()]",
		this.resultCollector);
}
/**
 * Method reference test.
 * (regression test for bug 5070 search: missing interface method reference  )
 */
public void testMethodReference03() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "q7", "AQ.java").getType("I");
	IMethod method = type.getMethod("k", new String[] {});

	search(
		method,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/q7/AQ.java void q7.D.h() [k()]",
		this.resultCollector);
}
/**
 * Method reference test.
 * (regression test for bug 8928 Unable to find references or declarations of methods that use static inner classes in the signature)
 */
public void testMethodReference04() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "b2", "Y.java").getType("Y");
	IMethod method = type.getMethod("foo", new String[] {"QX.Inner;"});

	search(
		method,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/b2/Z.java void b2.Z.bar() [foo(inner)]",
		this.resultCollector);
}
/**
 * Method reference test.
 * (regression test for bug 49120 search doesn't find references to anonymous inner methods)
 */
public void testMethodReference05() throws CoreException {
	IType type = getCompilationUnit("JavaSearch/src/e9/A.java").getType("A").getMethod("foo", new String[] {}).getType("", 1);
	IMethod method = type.getMethod("bar", new String[] {});

	search(
		method,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/e9/A.java void e9.A.foo() [bar()]",
		this.resultCollector);
}
/*
 * Method reference in second anonymous and second local type of a method test.
 */
public void testMethodReference06() throws CoreException {
	IMethod method= getCompilationUnit("JavaSearch/src/f3/X.java").getType("X").getMethod("bar", new String[] {});

	search(
		method,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/f3/X.java void void f3.X.foo():<anonymous>#2.foobar() [bar()]\n" +
		"src/f3/X.java void void f3.X.foo():Y#2.foobar() [bar()]",
		this.resultCollector);
}
/**
 * Simple method reference test.
 */
public void testMethodReference07() throws CoreException { // was testSimpleMethodReference
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");
	IMethod method = type.getMethod("foo", new String[] {"I", "QString;", "QX;"});

	search(
		method,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/Test.java void Test.main(String[]) [foo(1, \"a\", y)]\n" +
		// since bug 160301 fix, subclass overridden method calls are not reported
		//"src/Test.java void Test.main(String[]) [foo(1, \"a\", z)]\n" +
		"src/p/Z.java void p.Z.foo(int, String, X) [foo(i, s, new Y(true))]",
		this.resultCollector);
}
/**
 * Static method reference test.
 */
public void testMethodReference08() throws CoreException { // was testStaticMethodReference1
	IType type = getCompilationUnit("JavaSearch", "src", "p", "Y.java").getType("Y");
	IMethod method = type.getMethod("bar", new String[] {});

	search(
		method,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/Test.java void Test.main(String[]) [bar()]",
		this.resultCollector);
}
/**
 * Static method reference test.
 */
public void testMethodReference09() throws CoreException { // was testStaticMethodReference2
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");
	IMethod method = type.getMethod("bar", new String[] {});

	search(
		method,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"",
		this.resultCollector);
}
/**
 * Inner method reference test.
 */
public void testMethodReference10() throws CoreException { // was testInnerMethodReference
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X").getType("Inner");
	IMethod method = type.getMethod("foo", new String[] {});

	search(
		method,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/p/A.java void p.A.foo(int, String, X) [foo()]",
		this.resultCollector);
}
/**
 * Method reference through super test.
 */
public void testMethodReference11() throws CoreException { // was testMethodReferenceThroughSuper
	IType type = getCompilationUnit("JavaSearch", "src", "sd", "AQ.java").getType("AQ");
	IMethod method = type.getMethod("k", new String[] {});

	search(
		method,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/sd/AQ.java void sd.AQE.k() [k()]",
		this.resultCollector);
}
/**
 * Method reference in inner class test.
 */
public void testMethodReference12() throws CoreException { // was testMethodReferenceInInnerClass
	IType type = getCompilationUnit("JavaSearch", "src", "", "CA.java").getType("CA");
	IMethod method = type.getMethod("m", new String[] {});

	search(
		method,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/CA.java void CA$CB.f() [m()]\n" +
		"src/CA.java void CA$CB$CC.f() [m()]",
		this.resultCollector);
}
/**
 * Method reference in anonymous class test.
 * (regression test for PR 1GGNOTF: ITPJCORE:WINNT - Search doesn't find method referenced in anonymous inner class)
 */
public void testMethodReference13() throws CoreException { // was testMethodReferenceInAnonymousClass
	IType type = getCompilationUnit("JavaSearch", "src", "", "PR_1GGNOTF.java").getType("PR_1GGNOTF");
	IMethod method = type.getMethod("method", new String[] {});

	search(
		method,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/PR_1GGNOTF.java void void PR_1GGNOTF.method2():<anonymous>#1.run() [method()]",
		this.resultCollector);
}
/**
 * Method reference through array test.
 * (regression test for 1GHDA2V: ITPJCORE:WINNT - ClassCastException when doing a search)
 */
public void testMethodReference14() throws CoreException { // was testMethodReferenceThroughArray
	IType type = getClassFile("JavaSearch", getExternalJCLPathString(), "java.lang", "Object.class").getType();
	IMethod method = type.getMethod("clone", new String[] {});

	search(
		method,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/E.java Object E.foo() [clone()]",
		this.resultCollector);
}
/**
 * Method reference inside/outside doc comment.
 */
public void testMethodReference15() throws CoreException { // was testMethodReferenceInOutDocComment
	IType type = getCompilationUnit("JavaSearch", "src", "s4", "X.java").getType("X");
	IMethod method = type.getMethod("foo", new String[] {});
	this.resultCollector.showInsideDoc();
	search(method, REFERENCES, getJavaSearchScope(), this.resultCollector);
	assertSearchResults(
		"src/s4/X.java void s4.X.bar() [foo()] INSIDE_JAVADOC\n" +
		"src/s4/X.java void s4.X.fred() [foo()] OUTSIDE_JAVADOC",
		this.resultCollector);
}
/*
 * Generic method reference.
 */
public void testMethodReference16() throws CoreException {
	IType type = getCompilationUnit("JavaSearch15/src/p2/X.java").getType("X");
	IMethod method = type.getMethod("foo", new String[] {"QE;"});
	search(method, REFERENCES, ERASURE_RULE, getJavaSearchScope15(), this.resultCollector);
	assertSearchResults(
		"src/p2/Y.java void p2.Y.bar() [foo(this)]",
		this.resultCollector);
}
/**
 * Bug 111416: [search] wrong potential matches on a static method open
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=111416"
 */
public void testMethodReference17() throws CoreException {
	IType type = getCompilationUnit("JavaSearch/src/b111416/X.java").getType("X");
	IMethod method = type.getMethod("open", new String[] {"QString;"});
	this.resultCollector.showAccuracy(true);
	search(method, REFERENCES, ERASURE_RULE, getJavaSearchScope(), this.resultCollector);
	assertSearchResults(
		"src/b111416/X.java void b111416.X.foo() [open(\"\")] EXACT_MATCH",
		this.resultCollector);
}
/**
 * OrPattern test.
 * (regression test for bug 5862 search : too many matches on search with OrPattern)
 */
public void testOrPattern() throws CoreException {
	IMethod leftMethod = getCompilationUnit("JavaSearch", "src", "q9", "I.java")
		.getType("I").getMethod("m", new String[] {});
	SearchPattern leftPattern = createPattern(leftMethod, ALL_OCCURRENCES);
	IMethod rightMethod = getCompilationUnit("JavaSearch", "src", "q9", "I.java")
		.getType("A1").getMethod("m", new String[] {});
	SearchPattern rightPattern = createPattern(rightMethod, ALL_OCCURRENCES);
	SearchPattern orPattern = SearchPattern.createOrPattern(leftPattern, rightPattern);
	this.resultCollector.showAccuracy(true);
	search(
		orPattern,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/e8/A.java void e8.A.m() [m] POTENTIAL_MATCH\n" +
		"src/q9/I.java void q9.I.m() [m] EXACT_MATCH\n" +
		"src/q9/I.java void q9.A1.m() [m] EXACT_MATCH",
		this.resultCollector);
}
/**
 * Simple package declaration test.
 */
public void testPackageDeclaration1() throws CoreException { // was testSimplePackageDeclaration
	IPackageFragment pkg = getPackageFragment("JavaSearch", "src", "p");
	search(
		pkg,
		DECLARATIONS,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/p p",
		this.resultCollector);
}

/**
 * Various package declarations test.
 */
public void testPackageDeclaration2() throws CoreException { // was testVariousPackageDeclarations
	JavaSearchResultCollector packageCollector = new JavaSearchResultCollector(true);
	search(
		"p3*",
		PACKAGE,
		DECLARATIONS,
		getJavaSearchScope(),
		packageCollector);
	assertSearchResults(
		"src/p3 p3\n" +
		"src/p3/p2 p3.p2\n" +
		"src/p3/p2/p p3.p2.p",
		packageCollector);
}
/**
 * Package declaration test.
 * (regression test for bug 62698 NPE while searching for declaration of binary package)
 */
public void testPackageDeclaration3() throws CoreException { // was testPackageDeclaration
	IPackageFragment pkg = getPackageFragment("JavaSearch", getExternalJCLPathString(), "java.lang");

	search(
		pkg,
		DECLARATIONS,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		getExternalJCLPath() + " java.lang",
		this.resultCollector);
}
/**
 * Package declaration with corrupt jar on the classpath test.
 * (regression test for bug 75561 Rename package results in I/O exception)
 */
public void testPackageDeclaration4() throws CoreException {
	IJavaProject project = getJavaProject("JavaSearch");
	IClasspathEntry[] originalCP = project.getRawClasspath();
	try {
		// add corrupt.jar to classpath
		int cpLength = originalCP.length;
		IClasspathEntry[] newCP = new IClasspathEntry[cpLength+1];
		System.arraycopy(originalCP, 0, newCP, 0, cpLength);
		newCP[cpLength] = JavaCore.newLibraryEntry(new Path("/JavaSearch/corrupt.jar"), null, null);
		project.setRawClasspath(newCP, null);
		search("r9",
			PACKAGE,
			DECLARATIONS,
			getJavaSearchScope(),
			this.resultCollector);
		assertSearchResults(
			"src/r9 r9",
			this.resultCollector);
	} finally {
		project.setRawClasspath(originalCP, null);
	}
}
/**
 * Bug 73551: NPE while searching package declaration
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=73551"
 */
public void testPackageDeclarationBug73551() throws CoreException {
	JavaSearchResultCollector result = new JavaSearchResultCollector();
	result.showAccuracy(true);
	IPackageDeclaration packDecl = getCompilationUnit("JavaSearch", "src", "p71267", "Test.java").getPackageDeclaration("p71267");
	search(packDecl, DECLARATIONS, getJavaSearchScope(),  result);
	assertSearchResults(
		"src/p71267/Test.java p71267 [No source] EXACT_MATCH",
		result);
}
/**
 * Bug 117020: [search] Search for '*' does not report empty packages
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=117020"
 */
public void testPackageDeclarationBug117020() throws CoreException {
	IFolder srcFolder = getFolder(new Path("/JavaSearch/src"));
	IPackageFragmentRoot srcRoot = JAVA_PROJECT.getPackageFragmentRoot(srcFolder);
	IPackageFragment test = null;
	try {
		test = srcRoot.createPackageFragment("b117020", true, null);
		JavaSearchResultCollector result = new JavaSearchResultCollector();
		result.showAccuracy(true);
		search(test, DECLARATIONS, getJavaSearchScope(),  result);
		assertSearchResults(
			"src/b117020 b117020 EXACT_MATCH",
			result);
	}
	catch (JavaModelException jme) {
		// give up
	}
	finally {
		if (test != null && test.exists()) {
			test.delete(true, null);
		}
	}
}
public void testPackageDeclarationBug183062a() throws CoreException {
	JavaSearchResultCollector packageCollector = new JavaSearchResultCollector(true);
	search("j?",
		PACKAGE,
		DECLARATIONS,
		getJavaSearchScope(),
		packageCollector);
	assertSearchResults(
		"src/j1 j1\n" +
		"src/j2 j2\n" +
		"src/j3 j3\n" +
		"src/j4 j4\n" +
		"src/j5 j5\n" +
		"src/j6 j6\n" +
		"src/j7 j7\n" +
		"test47909.jar j3 [No source]",
		packageCollector);
}
public void testPackageDeclarationBug183062b() throws CoreException {
	JavaSearchResultCollector packageCollector = new JavaSearchResultCollector(true);
	search("j*",
		PACKAGE,
		DECLARATIONS,
		getJavaSearchScope(),
		packageCollector);
	assertSearchResults(
		""+ getExternalJCLPathString() + " java\n" +
		""+ getExternalJCLPathString() + " java.io\n" +
		""+ getExternalJCLPathString() + " java.lang\n" +
		"src/j1 j1\n" +
		"src/j2 j2\n" +
		"src/j3 j3\n" +
		"src/j4 j4\n" +
		"src/j5 j5\n" +
		"src/j6 j6\n" +
		"src/j7 j7\n" +
		"src/j7/qua j7.qua\n" +
		"src/j7/qua/li j7.qua.li\n" +
		"src/j7/qua/li/fied j7.qua.li.fied\n" +
		"test47909.jar j3 [No source]",
		packageCollector);
}
public void testPackageDeclarationBug183062c() throws CoreException {
	JavaSearchResultCollector packageCollector = new JavaSearchResultCollector(true);
	search("j7.*",
		PACKAGE,
		DECLARATIONS,
		getJavaSearchScope(),
		packageCollector);
	assertSearchResults(
		"src/j7/qua j7.qua\n" +
		"src/j7/qua/li j7.qua.li\n" +
		"src/j7/qua/li/fied j7.qua.li.fied",
		packageCollector);
}
public void testPackageDeclarationBug183062d() throws CoreException {
	JavaSearchResultCollector packageCollector = new JavaSearchResultCollector(true);
	search("j7.*.*",
		PACKAGE,
		DECLARATIONS,
		getJavaSearchScope(),
		packageCollector);
	assertSearchResults(
		"src/j7/qua/li j7.qua.li\n" +
		"src/j7/qua/li/fied j7.qua.li.fied",
		packageCollector);
}
public void testPackageDeclarationBug183062e() throws CoreException {
	JavaSearchResultCollector packageCollector = new JavaSearchResultCollector(true);
	search("????.????",
		PACKAGE,
		DECLARATIONS,
		getJavaSearchScope(),
		packageCollector);
	assertSearchResults(
		""+ getExternalJCLPathString() + " java.lang",
		packageCollector);
}
/**
 * Package reference test.
 * (regression test for PR 1GK90H4: ITPJCORE:WIN2000 - search: missing package reference)
 */
public void testPackageReference1() throws CoreException {
	IPackageFragment pkg = getPackageFragment("JavaSearch", "src", "q2");

	search(
		pkg,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/q1/B.java void q1.B.m(AA) [q2]",
		this.resultCollector);
}
/**
 * Package reference test.
 * (regression test for bug 17906 Rename package fails when inner classes are imported)
 */
public void testPackageReference2() throws CoreException {
	IPackageFragment pkg = getPackageFragment("JavaSearch", "src", "b8");

	search(
		pkg,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/b9/Foo.java [b8]",
		this.resultCollector);
}
/**
 * Package reference in jar test.
 * (regression test for bug 47989 Exception when searching for IPackageFragment "java.util.zip")
 */
public void testPackageReference3() throws CoreException {
	IPackageFragment pkg = getPackageFragment("JavaSearch", "test47989.jar", "p1");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {pkg.getParent()});

	search(
		pkg,
		REFERENCES,
		scope,
		this.resultCollector);
	assertSearchResults(
		"test47989.jar java.lang.Object p2.Y.foo()",
		this.resultCollector);
}
/**
 * Simple package reference test.
 */
public void testPackageReference4() throws CoreException { // was testSimplePackageReference
	IPackageFragment pkg = getPackageFragment("JavaSearch", "src", "p");

	search(
		pkg,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/InterfaceImplementors.java InterfaceImplementors [p]\n" +
		"src/Test.java void Test.main(String[]) [p]\n" +
		"src/Test.java void Test.main(String[]) [p]\n" +
		"src/Test.java void Test.main(String[]) [p]\n" +
		"src/Test.java void Test.main(String[]) [p]\n" +
		"src/Test.java void Test.main(String[]) [p]\n" +
		"src/Test.java void Test.main(String[]) [p]\n" +
		"src/Test.java void Test.main(String[]) [p]\n" +
		"src/TypeReferenceInImport/X.java [p]",
		this.resultCollector);
}
/**
 * Various package reference test.
 */
public void testPackageReference5() throws CoreException { // was testVariousPackageReference
	IPackageFragment pkg = getPackageFragment("JavaSearch", "src", "p3.p2.p");

	search(
		pkg,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/PackageReference/A.java [p3.p2.p]\n" +
		"src/PackageReference/B.java [p3.p2.p]\n" +
		"src/PackageReference/C.java PackageReference.C [p3.p2.p]\n" +
		"src/PackageReference/D.java PackageReference.D.x [p3.p2.p]\n" +
		"src/PackageReference/E.java PackageReference.E.x [p3.p2.p]\n" +
		"src/PackageReference/F.java p3.p2.p.X PackageReference.F.foo() [p3.p2.p]\n" +
		"src/PackageReference/G.java void PackageReference.G.foo(p3.p2.p.X) [p3.p2.p]\n" +
		"src/PackageReference/H.java void PackageReference.H.foo() [p3.p2.p]\n" +
		"src/PackageReference/I.java void PackageReference.I.foo() [p3.p2.p]\n" +
		"src/PackageReference/J.java void PackageReference.J.foo() [p3.p2.p]",
		this.resultCollector);
}
/**
 * Regression test for 1GBK7B2: ITPJCORE:WINNT - package references: could be more precise
 */
public void testPackageReference6() throws CoreException { // was testAccuratePackageReference
	IPackageFragment pkg = getPackageFragment("JavaSearch", "src", "p3.p2");

	search(
		pkg,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/PackageReference/K.java [p3.p2]",
		this.resultCollector);
}
/**
 * Test pattern match package references
 */
public void testPackageReference7() throws CoreException { // was testPatternMatchPackageReference

	search(
		"*p2.*",
		PACKAGE,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/PackageReference/A.java [p3.p2.p]\n" +
		"src/PackageReference/B.java [p3.p2.p]\n" +
		"src/PackageReference/C.java PackageReference.C [p3.p2.p]\n" +
		"src/PackageReference/D.java PackageReference.D.x [p3.p2.p]\n" +
		"src/PackageReference/E.java PackageReference.E.x [p3.p2.p]\n" +
		"src/PackageReference/F.java p3.p2.p.X PackageReference.F.foo() [p3.p2.p]\n" +
		"src/PackageReference/G.java void PackageReference.G.foo(p3.p2.p.X) [p3.p2.p]\n" +
		"src/PackageReference/H.java void PackageReference.H.foo() [p3.p2.p]\n" +
		"src/PackageReference/I.java void PackageReference.I.foo() [p3.p2.p]\n" +
		"src/PackageReference/J.java void PackageReference.J.foo() [p3.p2.p]",
		this.resultCollector);
}
/**
 * Test pattern match package references
 * Just verify that there's no ArrayOutOfBoundException to validate fix for
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=64421
 */
public void testPackageReference8() throws CoreException { // was testPatternMatchPackageReference2

	search(
		"*",
		PACKAGE,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	this.resultCollector.toString();
}
/**
 * Test that we find potential matches in binaries even if we can't resolve the entire
 * class file.
 * (Regression test for 1G4IN3E: ITPJCORE:WINNT - AbortCompilation using J9 to search for class declaration)
 */
public void testSearchFieldInBinaryWithResolution() throws CoreException {
	IClasspathEntry[] classpath = JAVA_PROJECT.getRawClasspath();
	try {
		// add AbortCompilation.jar to classpath
		int length = classpath.length;
		IClasspathEntry[] newClasspath = new IClasspathEntry[length+1];
		System.arraycopy(classpath, 0, newClasspath, 0, length);
		newClasspath[length] = JavaCore.newLibraryEntry(new Path("/JavaSearch/AbortCompilation.jar"), null, null);
		JAVA_PROJECT.setRawClasspath(newClasspath, null);

		// potential match for a field declaration
		this.resultCollector.showAccuracy(true);
		search("MissingFieldType.*", FIELD, DECLARATIONS,  getJavaSearchScope());
		assertSearchResults(
			"AbortCompilation.jar AbortCompilation.MissingFieldType.field [No source] EXACT_MATCH\n" +
			"AbortCompilation.jar AbortCompilation.MissingFieldType.otherField [No source] EXACT_MATCH\n" +
			"AbortCompilation.jar AbortCompilation.MissingFieldType.missing [No source] POTENTIAL_MATCH"
		);
	} finally {
		// reset classpath
		JAVA_PROJECT.setRawClasspath(classpath, null);
	}
}
// bug 186333: Pattern which does not resolve should find all the matches exact
public void testSearchFieldInBinaryNoResolution() throws CoreException {
	IClasspathEntry[] classpath = JAVA_PROJECT.getRawClasspath();
	try {
		// add AbortCompilation.jar to classpath
		int length = classpath.length;
		IClasspathEntry[] newClasspath = new IClasspathEntry[length+1];
		System.arraycopy(classpath, 0, newClasspath, 0, length);
		newClasspath[length] = JavaCore.newLibraryEntry(new Path("/JavaSearch/AbortCompilation.jar"), null, null);
		JAVA_PROJECT.setRawClasspath(newClasspath, null);

		// exact match for a field declaration
		JavaSearchResultCollector collector = new JavaSearchResultCollector() {
			public void acceptSearchMatch(SearchMatch searchMatch) throws CoreException {
	            IField field = (IField) searchMatch.getElement();
	            if (field.getDeclaringType().getElementName().equals("MissingFieldType")) {
		            super.acceptSearchMatch(searchMatch);
	            }
            }
		};
		collector.showAccuracy(true);
		search("*", FIELD, DECLARATIONS,  getJavaSearchScope(), collector);
		assertSearchResults(
			"AbortCompilation.jar AbortCompilation.MissingFieldType.field [No source] EXACT_MATCH\n" +
			"AbortCompilation.jar AbortCompilation.MissingFieldType.missing [No source] EXACT_MATCH\n" +
			"AbortCompilation.jar AbortCompilation.MissingFieldType.otherField [No source] EXACT_MATCH",
			collector
		);
	} finally {
		// reset classpath
		JAVA_PROJECT.setRawClasspath(classpath, null);
	}
}
/**
 * Test that we find potential matches in binaries even if we can't resolve the entire
 * class file.
 * (Regression test for 1G4IN3E: ITPJCORE:WINNT - AbortCompilation using J9 to search for class declaration)
 */
public void testSearchMethodInBinaryWithResolution() throws CoreException {
	IClasspathEntry[] classpath = JAVA_PROJECT.getRawClasspath();
	try {
		// add AbortCompilation.jar to classpath
		int length = classpath.length;
		IClasspathEntry[] newClasspath = new IClasspathEntry[length+1];
		System.arraycopy(classpath, 0, newClasspath, 0, length);
		newClasspath[length] = JavaCore.newLibraryEntry(new Path("/JavaSearch/AbortCompilation.jar"), null, null);
		JAVA_PROJECT.setRawClasspath(newClasspath, null);

		// potential match for a method declaration
		this.resultCollector.showAccuracy(true);
		search("MissingArgumentType.foo*", METHOD, DECLARATIONS, getJavaSearchScope());
		assertSearchResults(
			"AbortCompilation.jar void AbortCompilation.MissingArgumentType.foo() [No source] EXACT_MATCH\n" +
			"AbortCompilation.jar void AbortCompilation.MissingArgumentType.foo2() [No source] EXACT_MATCH\n" +
			"AbortCompilation.jar void AbortCompilation.MissingArgumentType.foo(java.util.EventListener) [No source] POTENTIAL_MATCH"
		);
	} finally {
		// reset classpath
		JAVA_PROJECT.setRawClasspath(classpath, null);
	}
}
// bug 186333: Pattern which does not resolve should find all the matches exact
public void testSearchMethodInBinaryNoResolution() throws CoreException {
	IClasspathEntry[] classpath = JAVA_PROJECT.getRawClasspath();
	try {
		// add AbortCompilation.jar to classpath
		int length = classpath.length;
		IClasspathEntry[] newClasspath = new IClasspathEntry[length+1];
		System.arraycopy(classpath, 0, newClasspath, 0, length);
		newClasspath[length] = JavaCore.newLibraryEntry(new Path("/JavaSearch/AbortCompilation.jar"), null, null);
		JAVA_PROJECT.setRawClasspath(newClasspath, null);

		// exact match for a method declaration
		JavaSearchResultCollector collector = new JavaSearchResultCollector() {
			public void acceptSearchMatch(SearchMatch searchMatch) throws CoreException {
	            IMethod method = (IMethod) searchMatch.getElement();
	            if (method.getDeclaringType().getElementName().equals("MissingArgumentType")) {
		            super.acceptSearchMatch(searchMatch);
	            }
            }
		};
		collector.showAccuracy(true);
		search("*", METHOD, DECLARATIONS,  getJavaSearchScope(), collector);
		assertSearchResults(
			"AbortCompilation.jar void AbortCompilation.MissingArgumentType.foo() [No source] EXACT_MATCH\n" +
			"AbortCompilation.jar void AbortCompilation.MissingArgumentType.foo(java.util.EventListener) [No source] EXACT_MATCH\n" +
			"AbortCompilation.jar void AbortCompilation.MissingArgumentType.foo2() [No source] EXACT_MATCH\n" +
			"AbortCompilation.jar AbortCompilation.MissingArgumentType() [No source] EXACT_MATCH",
			collector
		);
	} finally {
		// reset classpath
		JAVA_PROJECT.setRawClasspath(classpath, null);
	}
}
/**
 * Test that we find potential matches in binaries even if we can't resolve the entire
 * class file.
 * (Regression test for 1G4IN3E: ITPJCORE:WINNT - AbortCompilation using J9 to search for class declaration)
 */
public void testSearchTypesInBinaryWithResolution() throws CoreException {
	IClasspathEntry[] classpath = JAVA_PROJECT.getRawClasspath();
	try {
		// add AbortCompilation.jar to classpath
		int length = classpath.length;
		IClasspathEntry[] newClasspath = new IClasspathEntry[length+1];
		System.arraycopy(classpath, 0, newClasspath, 0, length);
		newClasspath[length] = JavaCore.newLibraryEntry(new Path("/JavaSearch/AbortCompilation.jar"), null, null);
		JAVA_PROJECT.setRawClasspath(newClasspath, null);

		// exact match for a type declaration
		this.resultCollector.showAccuracy(true);
		search("AbortCompilation.*Missing*", TYPE, DECLARATIONS,  getJavaSearchScope());
		assertSearchResults(
			"AbortCompilation.jar AbortCompilation.EnclosingType$MissingEnclosingType [No source] EXACT_MATCH\n" +
			"AbortCompilation.jar AbortCompilation.MissingArgumentType [No source] EXACT_MATCH\n" +
			"AbortCompilation.jar AbortCompilation.MissingFieldType [No source] EXACT_MATCH"
		);
	} finally {
		// reset classpath
		JAVA_PROJECT.setRawClasspath(classpath, null);
	}
}
public void testSearchTypeInBinaryNoResolution() throws CoreException {
	IClasspathEntry[] classpath = JAVA_PROJECT.getRawClasspath();
	try {
		// add AbortCompilation.jar to classpath
		int length = classpath.length;
		IClasspathEntry[] newClasspath = new IClasspathEntry[length+1];
		System.arraycopy(classpath, 0, newClasspath, 0, length);
		newClasspath[length] = JavaCore.newLibraryEntry(new Path("/JavaSearch/AbortCompilation.jar"), null, null);
		JAVA_PROJECT.setRawClasspath(newClasspath, null);

		// exact match for a type declaration
		this.resultCollector.showAccuracy(true);
		search("Missing*", TYPE, DECLARATIONS,  getJavaSearchScope());
		assertSearchResults(
			"AbortCompilation.jar AbortCompilation.EnclosingType$MissingEnclosingType [No source] EXACT_MATCH\n" +
			"AbortCompilation.jar AbortCompilation.MissingArgumentType [No source] EXACT_MATCH\n" +
			"AbortCompilation.jar AbortCompilation.MissingFieldType [No source] EXACT_MATCH"
		);
	} finally {
		// reset classpath
		JAVA_PROJECT.setRawClasspath(classpath, null);
	}
}
/**
 * Hierarchy scope test.
 * (regression test for bug 3445 search: type hierarchy scope incorrect (1GLC8VS))
 */
public void testSearchScope01() throws CoreException { // was testHierarchyScope
	ICompilationUnit cu = this. getCompilationUnit("JavaSearch", "src", "a9", "A.java");
	IType type = cu.getType("C");
	IJavaSearchScope scope = SearchEngine.createHierarchyScope(type);
	assertTrue("a9.C should be included in hierarchy scope", scope.encloses(type));
	assertTrue("a9.A should be included in hierarchy scope", scope.encloses(cu.getType("A")));
	assertTrue("a9.B should be included in hierarchy scope", scope.encloses(cu.getType("B")));
	assertTrue("a9/A.java should be included in hierarchy scope", scope.encloses(cu.getUnderlyingResource().getFullPath().toString()));
}
/**
 * Sub-cu java search scope test.
 * (regression test for bug 9041 search: cannot create a sub-cu scope)
 */
public void testSearchScope02() throws CoreException { // was testSubCUSearchScope1
	IType type = getCompilationUnit("JavaSearch", "src", "b3", "X.java").getType("X");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {type});

	search(
		type,
		REFERENCES,
		scope,
		this.resultCollector);
	assertSearchResults(
		"src/b3/X.java b3.X.field [X]\n" +
		"src/b3/X.java Object b3.X.foo() [X]\n" +
		"src/b3/X.java b3.X$Y.field2 [X]\n" +
		"src/b3/X.java Object b3.X$Y.foo2() [X]",
		this.resultCollector);
}
/**
 * Sub-cu java search scope test.
 * (regression test for bug 9041 search: cannot create a sub-cu scope)
 */
public void testSearchScope03() throws CoreException { // was testSubCUSearchScope2
	IType type = getCompilationUnit("JavaSearch", "src", "b3", "X.java").getType("X");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {type.getField("field")});

	search(
		type,
		REFERENCES,
		scope,
		this.resultCollector);
	assertSearchResults(
		"src/b3/X.java b3.X.field [X]",
		this.resultCollector);
}
/**
 * Sub-cu java search scope test.
 * (regression test for bug 9041 search: cannot create a sub-cu scope)
 */
public void testSearchScope04() throws CoreException { // was testSubCUSearchScope3
	IType type = getCompilationUnit("JavaSearch", "src", "b3", "X.java").getType("X");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {type.getType("Y")});

	search(
		type,
		REFERENCES,
		scope,
		this.resultCollector);
	assertSearchResults(
		"src/b3/X.java b3.X$Y.field2 [X]\n" +
		"src/b3/X.java Object b3.X$Y.foo2() [X]",
		this.resultCollector);
}
/**
 * Java search scope on java element in external jar test.
 */
public void testSearchScope05() throws CoreException, IOException { // was testExternalJarScope
	IWorkspace workspace = ResourcesPlugin.getWorkspace();
	File workspaceLocation = new File(workspace.getRoot().getLocation().toOSString());
	File minimalJar = new File(workspaceLocation, "JavaSearch/MyJar.jar");
	File externalJar = new File(workspaceLocation.getParentFile().getCanonicalFile(), "MyJar.jar"); // canonicalize the external path as this is not done on case sensitive platforms when creating a new lib entry
	IJavaProject project = getJavaProject("JavaSearch");
	IClasspathEntry[] classpath = project.getRawClasspath();
	try {
		copy(minimalJar, externalJar);
		int length = classpath.length;
		IClasspathEntry[] newClasspath = new IClasspathEntry[length];
		System.arraycopy(classpath, 0, newClasspath, 0, length-1);
		String externalPath = externalJar.getAbsolutePath();
		newClasspath[length-1] = JavaCore.newLibraryEntry(new Path(externalPath), new Path(externalPath), null, false);
		project.setRawClasspath(newClasspath, null);

		IPackageFragment pkg = getPackageFragment("JavaSearch", externalPath, "p0");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {pkg});

		search(
			"X",
			TYPE,
			DECLARATIONS,
			scope,
			this.resultCollector);
		assertSearchResults(
			externalJar + " p0.X",
			this.resultCollector);

		IClassFile classFile = pkg.getClassFile("X.class");
		scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {classFile});
		this.resultCollector = new JavaSearchResultCollector();
		search(
			classFile.getType(),
			DECLARATIONS,
			scope,
			this.resultCollector);
		assertSearchResults(
			externalJar.getCanonicalPath()+ " p0.X",
			this.resultCollector);

	} finally {
		Util.delete(externalJar);
		project.setRawClasspath(classpath, null);
	}

}
/**
 * Hierarchy scope test.
 * (test for enhancement bug 215139 encloses(): find only subtypes).
 */
public void testSearchScope06() throws CoreException {
    	ICompilationUnit cuB = this. getCompilationUnit("JavaSearch", "src", "a10", "B.java");
        ICompilationUnit cuC = this. getCompilationUnit("JavaSearch", "src", "a10", "C.java");
        IType type = cuC.getType("C");
        IJavaSearchScope scope = SearchEngine.createHierarchyScope(null, type, true, true, null);
        
        // don't include super-classes:
        assertFalse("a10.A should not be included in hierarchy scope", scope.encloses(cuB.getType("A")));
        assertFalse("a10.B should not be included in hierarchy scope", scope.encloses(cuB.getType("B")));
        assertFalse("a10/B.java should not be included in hierarchy scope", scope.encloses(cuB.getUnderlyingResource().getFullPath().toString()));
}
/**
 * Hierarchy scope test.
 * (test for enhancement bug 215139 encloses(): find only subtypes).
 */
public void testSearchScope07() throws CoreException {
    	ICompilationUnit cuC = this. getCompilationUnit("JavaSearch", "src", "a10", "C.java");
        IType type = cuC.getType("C");
        IJavaSearchScope scope = SearchEngine.createHierarchyScope(null, type, true, true, null);
        
        // don't include focus type:
        assertFalse("a10.C should be not included in hierarchy scope", scope.encloses(type));
        assertFalse("a10/C.java should be included in hierarchy scope", scope.encloses(cuC.getUnderlyingResource().getFullPath().toString()));       
}
/**
 * Hierarchy scope test.
 * (test for enhancement bug 215139 encloses(): find only subtypes).
 */
public void testSearchScope08() throws CoreException {
    	ICompilationUnit cuC = this. getCompilationUnit("JavaSearch", "src", "a10", "C.java");
        ICompilationUnit cuD = this. getCompilationUnit("JavaSearch", "src", "a10", "D.java");
        ICompilationUnit cuE = this. getCompilationUnit("JavaSearch", "src", "a10", "E.java");
        IType type = cuC.getType("C");
        IJavaSearchScope scope = SearchEngine.createHierarchyScope(null, type, true, true, null);
        
        // regular sub-types:
        assertTrue("a10.D should be included in hierarchy scope", scope.encloses(cuD.getType("D")));
        assertTrue("a10/D.java should be included in hierarchy scope", scope.encloses(cuD.getUnderlyingResource().getFullPath().toString()));
        
        assertTrue("a10.E should be included in hierarchy scope", scope.encloses(cuE.getType("E")));
        assertTrue("a10.F should be included in hierarchy scope", scope.encloses(cuE.getType("F")));
}
/**
 * Hierarchy scope test.
 * (test for enhancement bug 215139 encloses(): find only subtypes).
 */
public void testSearchScope09() throws CoreException {
    	ICompilationUnit cuC = this. getCompilationUnit("JavaSearch", "src", "a10", "C.java");
        ICompilationUnit cuE = this. getCompilationUnit("JavaSearch", "src", "a10", "E.java");
        IType type = cuC.getType("C");
        IJavaSearchScope scope = SearchEngine.createHierarchyScope(null, type, true, true, null);
        
        // sub-type is a nested type:
        assertTrue("a10.H$I should be included in hierarchy scope", scope.encloses(cuE.getType("H").getType("I")));
}
/**
 * Hierarchy scope test.
 * (test for enhancement bug 215139 encloses(): find only subtypes).
 */
public void testSearchScope10() throws CoreException {
    	ICompilationUnit cuC = this. getCompilationUnit("JavaSearch", "src", "a10", "C.java");
        ICompilationUnit cuE = this. getCompilationUnit("JavaSearch", "src", "a10", "E.java");
        IType type = cuC.getType("C");
        IJavaSearchScope scope = SearchEngine.createHierarchyScope(null, type, true, true, null);
        
        // member of a sub-type:
        assertFalse("a10.F$G should not be included in hierarchy scope", scope.encloses(cuE.getType("F").getType("G")));
}
/**
 * Hierarchy scope test.
 * (test for enhancement bug 215139 encloses(): find only subtypes and their member types).
 */
public void testSearchScope11() throws CoreException {
    	ICompilationUnit cuC = this. getCompilationUnit("JavaSearch", "src", "a10", "C.java");
        ICompilationUnit cuE = this. getCompilationUnit("JavaSearch", "src", "a10", "E.java");
        IType type = cuC.getType("C");
        IJavaSearchScope scope = SearchEngine.createHierarchyScope(null, type, true, false, null);
        
        // member of a sub-type:
        assertTrue("a10.F$G should be included in hierarchy scope", scope.encloses(cuE.getType("F").getType("G")));
}
/**
 * Hierarchy scope test.
 * (test for enhancement bug 215139 encloses(): find only subtypes).
 */
public void testSearchScope12() throws CoreException {
    	ICompilationUnit cuC = this. getCompilationUnit("JavaSearch", "src", "a10", "C.java");
        ICompilationUnit cuE = this. getCompilationUnit("JavaSearch", "src", "a10", "E.java");
        IType type = cuC.getType("C");
        IJavaSearchScope scope = SearchEngine.createHierarchyScope(null, type, true, true, null);
        
        // enclosing of a sub-type:
        assertFalse("a10.H should not be included in hierarchy scope", scope.encloses(cuE.getType("H")));
        assertTrue("a10/E.java should be included in hierarchy scope", scope.encloses(cuE.getUnderlyingResource().getFullPath().toString()));
}
/**
 * Hierarchy scope test.
 * (test for enhancement bug 215139 search: find only subtypes).
 */
public void testSearchScope13() throws CoreException {
        ICompilationUnit cuC = this. getCompilationUnit("JavaSearch", "src", "a10", "C.java");
        IType type = cuC.getType("C");
        IJavaSearchScope scope = SearchEngine.createHierarchyScope(null, type, true, true, null);
        
        search("**", TYPE, DECLARATIONS, scope);
        assertSearchResults(
        		"src/a10/D.java a10.D [D]\n" + 
        		"src/a10/E.java a10.E [E]\n" + 
        		"src/a10/E.java a10.F [F]\n" + 
        		"src/a10/E.java a10.H$I [I]"
        		);
}
/**
 * Hierarchy scope test.
 * (test for enhancement bug 215139 search: find only subtypes - disabled).
 */
public void testSearchScope14() throws CoreException {
        ICompilationUnit cuC = this. getCompilationUnit("JavaSearch", "src", "a10", "C.java");
        IType type = cuC.getType("C");
        IJavaSearchScope scope = SearchEngine.createHierarchyScope(null, type, /*onlySubTypes*/false, true, null);
        
        search("**", TYPE, DECLARATIONS, scope);
        assertSearchResults(
        		"src/a10/B.java a10.A [A]\n" + 
        		"src/a10/B.java a10.B [B]\n" + 
        		"src/a10/C.java a10.C [C]\n" + 
        		"src/a10/D.java a10.D [D]\n" + 
        		"src/a10/E.java a10.E [E]\n" + 
        		"src/a10/E.java a10.F [F]\n" + 
        		"src/a10/E.java a10.H$I [I]\n" + 
        		getExternalJCLPathString() + " java.lang.Object"
        		);
}
/**
 * Hierarchy scope test.
 * (test for enhancement bug 215139 search: find only subtypes - different call chain).
 */
public void testSearchScope15() throws CoreException {
        ICompilationUnit cuC = this. getCompilationUnit("JavaSearch", "src", "a10", "C.java");
        IType type = cuC.getType("C");
        IJavaSearchScope scope = SearchEngine.createHierarchyScope(null, type, true, true, null);
    	TypeNameMatchCollector collector = new TypeNameMatchCollector() {
    		public String toString(){
    			return toFullyQualifiedNamesString();
    		}
    	};
    	new SearchEngine().searchAllTypeNames(
    		null,
    		null,
    		scope,
    		collector,
    		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
    		null);
    	String expected = 
    			"a10.D\n" +
    			"a10.E\n" +
    			"a10.F\n" +
    			"a10.H$I";
    	assertTrue("We should get some types!", collector.size() > 0);
    	assertEquals("Found types sound not to be correct", expected, collector.toString());
}
/**
 * Hierarchy scope test.
 * (test for enhancement bug 215139 search: find only subtypes plus member & enclosing types - different call chain).
 */
public void testSearchScope16() throws CoreException {
        ICompilationUnit cuC = this. getCompilationUnit("JavaSearch", "src", "a10", "C.java");
        IType type = cuC.getType("C");
        IJavaSearchScope scope = SearchEngine.createHierarchyScope(null, type, true, false, null);
    	TypeNameMatchCollector collector = new TypeNameMatchCollector() {
    		public String toString(){
    			return toFullyQualifiedNamesString();
    		}
    	};
    	new SearchEngine().searchAllTypeNames(
    		null,
    		null,
    		scope,
    		collector,
    		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
    		null);
    	String expected = 
    			"a10.D\n" +
    			"a10.E\n" +
    			"a10.F\n" +
    			"a10.F$G\n" +
    			"a10.H\n" +
    			"a10.H$I";
    	assertTrue("We should get some types!", collector.size() > 0);
    	assertEquals("Found types sound not to be correct", expected, collector.toString());
}
/**
 * Hierarchy scope test.
 * (test for enhancement bug 215139 encloses(method): find only subtypes).
 */
public void testSearchScope17() throws CoreException {
    	ICompilationUnit cuC = this. getCompilationUnit("JavaSearch", "src", "a10", "C.java");
        ICompilationUnit cuE = this. getCompilationUnit("JavaSearch", "src", "a10", "E.java");
        IType type = cuC.getType("C");
        IJavaSearchScope scope = SearchEngine.createHierarchyScope(null, type, true, true, null);
        
        // method of a member of a sub-type:
        assertFalse("a10.F$G.m() should not be included in hierarchy scope", scope.encloses(cuE.getType("F").getType("G").getMethod("m", new String[0])));
}
/**
 * Hierarchy scope test.
 * (test for enhancement bug 215139 encloses(method): find only subtypes but also member types).
 */
public void testSearchScope18() throws CoreException {
    	ICompilationUnit cuC = this. getCompilationUnit("JavaSearch", "src", "a10", "C.java");
        ICompilationUnit cuE = this. getCompilationUnit("JavaSearch", "src", "a10", "E.java");
        IType type = cuC.getType("C");
        IJavaSearchScope scope = SearchEngine.createHierarchyScope(null, type, true, false, null);
        
        // method of a member of a sub-type:
        assertTrue("a10.F$G.m() should be included in hierarchy scope", scope.encloses(cuE.getType("F").getType("G").getMethod("m", new String[0])));
}
/**
 * Simple type declaration test.
 */
public void testTypeDeclaration01() throws CoreException { // was testSimpleTypeDeclaration
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");

	search(
		type,
		DECLARATIONS,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults("src/p/X.java p.X [X]", this.resultCollector);
}
/**
 * Type declaration test.
 * (generic type)
 */
public void testTypeDeclaration02() throws CoreException {
	IPackageFragment pkg = getPackageFragment("JavaSearch15", "src", "p1");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {pkg});

	search(
		"Y",
		TYPE,
		DECLARATIONS,
		scope,
		this.resultCollector);
	assertSearchResults(
		"src/p1/Y.java p1.Y [Y]",
		this.resultCollector);
}
/**
 * Type declaration test.
 * (regression test for bug 29524 Search for declaration via patterns adds '"*")
 */
public void testTypeDeclaration03() throws CoreException { // was testTypeDeclaration
	IPackageFragment pkg = getPackageFragment("JavaSearch", "src", "d8");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {pkg});

	search(
		"A",
		TYPE,
		DECLARATIONS,
		scope,
		this.resultCollector);
	assertSearchResults("src/d8/A.java d8.A [A]", this.resultCollector);
}
/**
 * Type declaration in jar file test.
 */
public void testTypeDeclaration04() throws CoreException { // was testTypeDeclarationInJar
	IType type = getClassFile("JavaSearch", "MyJar.jar", "p1", "A.class").getType();

	search(
		type,
		DECLARATIONS,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"MyJar.jar p1.A [No source]",
		this.resultCollector);
}
/**
 * Type declaration in jar file and in anonymous class test.
 * (regression test for 20631 Declaration of local binary type not found)
 */
public void testTypeDeclaration05() throws CoreException { // was testTypeDeclarationInJar2
	IPackageFragmentRoot root = getPackageFragmentRoot("JavaSearch", "test20631.jar");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {root});

	search(
		"Y",
		TYPE,
		DECLARATIONS,
		scope,
		this.resultCollector);
	assertSearchResults(
		"test20631.jar Y",
		this.resultCollector);
}
/**
 * Type declaration using a package scope test.
 * (check that subpackages are not included)
 */
public void testTypeDeclaration06() throws CoreException { // was testTypeDeclarationInPackageScope
	IType type = getCompilationUnit("JavaSearch", "src", "p3", "X.java").getType("X");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {type.getPackageFragment()});

	search(
		"X",
		TYPE,
		DECLARATIONS,
		scope,
		this.resultCollector);
	assertSearchResults(
		"src/p3/X.java p3.X [X]",
		this.resultCollector);
}
/**
 * Type declaration using a binary package scope test.
 * (check that subpackages are not included)
 */
public void testTypeDeclaration07() throws CoreException { // was testTypeDeclarationInPackageScope2
	IType type = getClassFile("JavaSearch", "MyJar.jar", "p0", "X.class").getType();
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {type.getPackageFragment()});

	search(
		"X",
		TYPE,
		DECLARATIONS,
		scope,
		this.resultCollector);
	assertSearchResults(
		"MyJar.jar p0.X [No source]",
		this.resultCollector);
}
/**
 * Member type declaration test.
 * (regression test for bug 9992 Member class declaration not found)
 */
public void testTypeDeclaration08() throws CoreException { // was testMemberTypeDeclaration
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[]{
		getPackageFragment("JavaSearch", "src", "b4")
	});

	search(
		"*.A.B",
		TYPE,
		DECLARATIONS,
		scope,
		this.resultCollector);
	assertSearchResults(
		"src/b4/A.java b4.A$B [B]",
		this.resultCollector);
}
/**
 * Test pattern match type declaration
 * (regression test for bug 17210 No match found when query contains '?')
 */
public void testTypeDeclaration09() throws CoreException {

	search(
		"X?Z",
		TYPE,
		DECLARATIONS,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults("src/r5/XYZ.java r5.XYZ [XYZ]", this.resultCollector);
}
/**
 * Long declaration (>255) test.
 * (regression test for bug 25859 Error doing Java Search)
 */
public void testTypeDeclaration10() throws CoreException { // was testLongDeclaration

	search(
		"AbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyz",
		TYPE,
		DECLARATIONS,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/c9/X.java c9.AbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyz [AbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyz]",
		this.resultCollector);
}
/*
 * Local type declaration test.
 */
public void testTypeDeclaration11() throws CoreException { // was testLocalTypeDeclaration1
	IPackageFragment pkg = getPackageFragment("JavaSearch", "src", "f2");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {pkg});

	search(
		"Y",
		TYPE,
		DECLARATIONS,
		scope,
		this.resultCollector);
	assertSearchResults(
		"src/f2/X.java Object f2.X.foo1():Y#1 [Y]",
		this.resultCollector);
}
/*
 * Local type declaration test.
 */
public void testTypeDeclaration12() throws CoreException { // was testLocalTypeDeclaration2
	IType type = getCompilationUnit("JavaSearch/src/f2/X.java").getType("X").getMethod("foo1", new String[0]).getType("Y", 1);

	IJavaSearchScope scope = SearchEngine.createWorkspaceScope();

	search(
		type,
		DECLARATIONS,
		scope,
		this.resultCollector);
	assertSearchResults(
		"src/f2/X.java Object f2.X.foo1():Y#1 [Y]",
		this.resultCollector);
}
/**
 * Type ocurrence test.
 * (regression test for PR 1GKAQJS: ITPJCORE:WIN2000 - search: incorrect results for nested types)
 */
public void testTypeOccurence1() throws CoreException { // was testTypeOccurence
	IType type = getCompilationUnit("JavaSearch", "src", "r", "A.java").getType("A").getType("X");

	search(
		type,
		ALL_OCCURRENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/r/A.java A r.A.m() [X]\n" +
		"src/r/A.java r.A$X [X]\n" +
		"src/r/A.java r.A$X(X) [X]\n" +
		"src/r/A.java r.A$X(X) [X]\n" +
		"src/r/A.java r.B.ax [A.X]\n" +
		"src/r/A.java r.B.ax [X]",
		this.resultCollector);
}
/**
 * Type ocuurence in unresolvable import test.
 * (regression test for bug 37166 NPE in SearchEngine when matching type against ProblemReferenceBinding )
 */
public void testTypeOccurence2() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "r8", "B.java").getType("B");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {type.getPackageFragment()});

	search(
		type,
		ALL_OCCURRENCES,
		scope,
		this.resultCollector);
	assertSearchResults(
		"src/r8/A.java [B]",
		this.resultCollector);
}
/**
 * Type occurences test.
 * Ensures that correct positions are reported for an inner type reference using a ALL_OCCURENCES pattern
 */
public void testTypeOccurence3() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "e4", "A.java").getType("A").getType("Inner");

	search(
		type,
		ALL_OCCURRENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/e4/A.java e4.A$Inner [Inner]\n" +
		"src/e5/A1.java [e4.A.Inner]\n" +
		"src/e5/A1.java e5.A1.a [e4.A.Inner]\n" +
		"src/e5/A1.java e5.A1.a1 [e4.A.Inner]\n" +
		"src/e5/A1.java e5.A1.a2 [Inner]\n" +
		"src/e5/A1.java e5.A1.a3 [Inner]",
		this.resultCollector);
}
/**
 * Type name with $ ocurrence test.
 * (regression test for bug 3310 Smoke 124: Compile errors introduced with rename refactoring (1GFBK2G))
 */
public void testTypeOccurence4() throws CoreException { // was testTypeOccurenceWithDollar
	IType type = getCompilationUnit("JavaSearch", "src", "q3", "A$B.java").getType("A$B");

	search(
		type,
		ALL_OCCURRENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/q3/A$B.java q3.A$B [A$B]\n" +
		"src/q4/C.java Object q4.C.foo() [q3.A$B]",
		this.resultCollector);
}
/**
 * Type reference test.
 * (Regression test for PR 1GK7K17: ITPJCORE:WIN2000 - search: missing type reference)
 */
public void testTypeReference01() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "", "X.java").getType("X");

	search(
		type,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/X.java AA() [X]",
		this.resultCollector);
}
/**
 * Type reference test.
 * (Regression test for bug 29516 SearchEngine regressions in 20030114)
 */
public void testTypeReference02() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "d7", "A.java").getType("A");

	search(
		type,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/d7/A.java d7.A.A [A]\n" +
		"src/d7/A.java A d7.A.A(A) [A]\n" +
		"src/d7/A.java A d7.A.A(A) [A]",
		this.resultCollector);
}
/**
 * Type reference test.
 * (Regression test for bug 31985 NPE searching non-qualified and case insensitive type ref)
 */
public void testTypeReference03() throws CoreException {
	SearchPattern pattern = createPattern("x31985", TYPE, REFERENCES, false);

	this.resultCollector.showAccuracy(true);
	search(
		pattern,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/e3/X31985.java e3.X31985.CONSTANT [X31985] EXACT_MATCH\n" +
		"src/e3/Y31985.java Object e3.Y31985.foo() [X31985] EXACT_MATCH",
		this.resultCollector);
}
/**
 * Type reference test.
 * (Regression test for bug 31997 Refactoring d.n. work for projects with brackets in name.)
 */
public void testTypeReference04() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "otherSrc()", "", "X31997.java").getType("X31997");

	search(
		type,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"otherSrc()/Y31997.java Y31997 [X31997]",
		this.resultCollector);
}
/**
 * Type reference test.
 * (Regression test for bug 48261 Search does not show results)
 */
public void testTypeReference05() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "test48261.jar", "p", "X.java").getType("X");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {type.getPackageFragment().getParent()});

	search(
		type,
		REFERENCES,
		scope,
		this.resultCollector);
	assertSearchResults(
		"test48261.jar p.X$Y(java.lang.String)",
		this.resultCollector);
}
/**
 * Type reference test
 * (in a generic type)
 */
public void testTypeReference06() throws CoreException {
	IType type = getCompilationUnit("JavaSearch15/src/p1/X.java").getType("X");

	search(
		type,
		REFERENCES,
		getJavaSearchScope15("p1", true),
		this.resultCollector);
	assertSearchResults(
		"src/p1/Y.java Object p1.Y.foo() [X]",
		this.resultCollector);
}
/**
 * Simple type reference test.
 */
public void testTypeReference07() throws CoreException { // was testTypeDeclaration01
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");

	search(
		type,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/p/A.java p.A.x [X]\n" +
		"src/p/A.java p.A(X) [X]\n" +
		"src/p/A.java void p.A.foo(int, String, X) [X]\n" +
		"src/p/X.java p.X() [X]\n" +
		"src/p/X.java void p.X.foo(int, String, X) [X]\n" +
		"src/p/Y.java p.Y [X]\n" +
		"src/p/Z.java void p.Z.foo(int, String, X) [X]",
		this.resultCollector);
}
/**
 * Type reference in initializer test.
 * (regression test for PR #1G4GO4O)
 */
public void testTypeReference08() throws CoreException { // was testTypeReferenceInInitializer
	IType type = getCompilationUnit("JavaSearch", "src", "", "Test.java").getType("Test");

	search(
		type,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/Test.java Test.static {} [Test]\n" +
		"src/Test.java Test.static {} [Test]\n" +
		"src/Test.java Test.{} [Test]",
		this.resultCollector);
}
/**
 * Type reference as a single name reference test.
 */
public void testTypeReference09() throws CoreException { // was testTypeReferenceAsSingleNameReference
	IType type = getCompilationUnit("JavaSearch", "src", "", "TypeReferenceAsSingleNameReference.java").getType("TypeReferenceAsSingleNameReference");

	search(
		type,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/TypeReferenceAsSingleNameReference.java void TypeReferenceAsSingleNameReference.hasReference() [TypeReferenceAsSingleNameReference]\n" +
		"src/TypeReferenceAsSingleNameReference.java void TypeReferenceAsSingleNameReference.hasReference() [TypeReferenceAsSingleNameReference]",
		this.resultCollector);
}
/**
 * Member type reference test.
 */
public void testTypeReference10() throws CoreException { // was testMemberTypeReference
	// references to second level member type

	this.resultCollector.showAccuracy(true);
	search(
		"BMember",
		TYPE,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"References to type BMember",
		"src/MemberTypeReference/Azz.java void MemberTypeReference.Azz.poo() [BMember] EXACT_MATCH\n" +
		"src/MemberTypeReference/Azz.java MemberTypeReference.Azz$AzzMember [BMember] EXACT_MATCH\n" +
		"src/MemberTypeReference/Azz.java MemberTypeReference.X.val [BMember] EXACT_MATCH\n" +
		"src/MemberTypeReference/B.java void MemberTypeReference.B.foo() [BMember] EXACT_MATCH",
		this.resultCollector);

	// references to first level member type
	this.resultCollector = new JavaSearchResultCollector();
	this.resultCollector.showAccuracy(true);
	search(
		"AzzMember",
		TYPE,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"References to type AzzMember",
		"src/MemberTypeReference/Azz.java MemberTypeReference.X.val [AzzMember] EXACT_MATCH\n" +
		"src/MemberTypeReference/B.java void MemberTypeReference.B.foo() [AzzMember] EXACT_MATCH",
		this.resultCollector);

	// no reference to a field with same name as member type
	this.resultCollector = new JavaSearchResultCollector();
	this.resultCollector.showAccuracy(true);
	search(
		"BMember",
		FIELD,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"References to field BMember",
		"",
		this.resultCollector);
}
/**
 * Member type reference test.
 * (regression test for PR 1GL0MN9: ITPJCORE:WIN2000 - search: not consistent results for nested types)
 */
public void testTypeReference11() throws CoreException { // was testMemberTypeReference2
	IType type = getCompilationUnit("JavaSearch", "src", "a", "A.java").getType("A").getType("X");

	search(
		type,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/a/A.java a.B.ax [A.X]\n" +
		"src/a/A.java a.B.sx [S.X]",
		this.resultCollector);
}
/**
 * Member type named "Object" reference test.
 * (regression test for 1G4GHPS: ITPJUI:WINNT - Strange error message in search)
 */
public void testTypeReference12() throws CoreException { // was testObjectMemberTypeReference
	IType type = getCompilationUnit("JavaSearch", "src", "ObjectMemberTypeReference", "A.java")
		.getType("A")
		.getType("Object");

	this.resultCollector.showAccuracy(true);
	search(
		type,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/ObjectMemberTypeReference/A.java void ObjectMemberTypeReference.A.foo() [Object] EXACT_MATCH",
		this.resultCollector);
}
/**
 * Type reference inside a qualified name reference test.
 * (Regression test for PR #1G4TSC0)
 */
public void testTypeReference13() throws CoreException { // was testTypeReferenceInQualifiedNameReference
	IType type = getCompilationUnit("JavaSearch", "src", "p", "A.java").getType("A");

	search(
		type,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/Test.java void Test.main(String[]) [p.A]\n" +
		"src/Test.java void Test.main(String[]) [p.A]\n" +
		"src/p/A.java void p.A.foo() [A]",
		this.resultCollector);
}
/**
 * Type reference inside a qualified name reference test.
 * (Regression test for PR #1GLBP65)
 */
public void testTypeReference14() throws CoreException { // was testTypeReferenceInQualifiedNameReference2
	IType type = getCompilationUnit("JavaSearch", "src", "p4", "A.java").getType("A");

	search(
		type,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/p4/A.java p4.A.A [A]\n" +
		"src/p4/A.java p4.X [p4.A]\n" +
		"src/p4/A.java void p4.X.x() [p4.A]",
		this.resultCollector);
}
/**
 * Type reference inside a qualified name reference test.
 * (Regression test for PR 1GL9UMH: ITPJCORE:WIN2000 - search: missing type occurrences)
 */
public void testTypeReference15() throws CoreException { // was testTypeReferenceInQualifiedNameReference3
	IType type = getCompilationUnit("JavaSearch", "src", "", "W.java").getType("W");

	search(
		type,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/W.java int W.m() [W]",
		this.resultCollector);
}
/**
 * Type reference inside a qualified name reference test.
 * (Regression test for bug 16751 Renaming a class doesn't update all references  )
 */
public void testTypeReference16() throws CoreException { // was testTypeReferenceInQualifiedNameReference4
	IType type = getCompilationUnit("JavaSearch", "src", "b7", "X.java").getType("SubClass");

	search(
		type,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/b7/X.java void b7.Test.main(String[]) [SubClass]",
		this.resultCollector);
}
/**
 * Type reference in a folder that is not in the classpath.
 * (regression test for PR #1G5N8KS)
 */
public void testTypeReference17() throws CoreException { // was testTypeReferenceNotInClasspath
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");

	search(
		type,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/p/A.java p.A.x [X]\n" +
		"src/p/A.java p.A(X) [X]\n" +
		"src/p/A.java void p.A.foo(int, String, X) [X]\n" +
		"src/p/X.java p.X() [X]\n" +
		"src/p/X.java void p.X.foo(int, String, X) [X]\n" +
		"src/p/Y.java p.Y [X]\n" +
		"src/p/Z.java void p.Z.foo(int, String, X) [X]",
		this.resultCollector);
}
/**
 * Type reference inside an argument, a return type or a field type.
 * (Regression test for PR #1GA7QA1)
 */
public void testTypeReference18() throws CoreException { // was testVariousTypeReferences
	IType type = getCompilationUnit("JavaSearch", "src", "NoReference", "A.java").getType("A");

	search(
		type,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"", // no reference should be found
		this.resultCollector);
}
/**
 * Type reference in import test.
 * (regression test for PR #1GA7PAS)
 */
public void testTypeReference19() throws CoreException { // was testTypeReferenceInImport
	IType type = getCompilationUnit("JavaSearch", "src", "p2", "Z.java").getType("Z");

	search(
		type,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/TypeReferenceInImport/X.java [p2.Z]",
		this.resultCollector);
}
/**
 * Type reference in import test.
 * (regression test for bug 23077 search: does not find type references in some imports)
 */
public void testTypeReference20() throws CoreException { // was testTypeReferenceInImport2
	IType type = getCompilationUnit("JavaSearch", "src", "r6", "A.java").getType("A");

	search(
		type,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/r6/B.java [r6.A]\n" +
		"src/r6/B.java [r6.A]\n" +
		"src/r6/B.java [r6.A]\n" +
		"src/r6/B.java [r6.A]\n" +
		"src/r6/B.java [r6.A]\n" +
		"src/r6/B.java [r6.A]",
		this.resultCollector);
}
/**
 * Type reference in array test.
 * (regression test for PR #1GAL424)
 */
public void testTypeReference21() throws CoreException { // was testTypeReferenceInArray
	IType type = getCompilationUnit("JavaSearch", "src", "TypeReferenceInArray", "A.java").getType("A");

	search(
		type,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/TypeReferenceInArray/A.java TypeReferenceInArray.A.a [A]\n" +
		"src/TypeReferenceInArray/A.java TypeReferenceInArray.A.b [TypeReferenceInArray.A]",
		this.resultCollector);
}
/**
 * Type reference in array test.
 * (regression test for bug 3230 Search - Too many type references for query ending with * (1GAZVGI)  )
 */
public void testTypeReference22() throws CoreException { // was testTypeReferenceInArray2
	IType type = getCompilationUnit("JavaSearch", "src", "s1", "X.java").getType("X");

	search(
		type,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/s1/Y.java s1.Y.f [X]",
		this.resultCollector);
}
/**
 * Negative type reference test.
 * (regression test for 1G52F7P: ITPJCORE:WINNT - Search - finds bogus references to class)
 */
public void testTypeReference23() throws CoreException { // testNegativeTypeReference
	IType type = getCompilationUnit("JavaSearch", "src", "p7", "A.java").getType("A");

	search(
		type,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"",
		this.resultCollector);
}
/**
 * Type reference in a throw clause test.
 * (Regression test for bug 6779 searchDeclarationsOfReferencedTyped - missing exception types)
 */
public void testTypeReference24() throws CoreException { // was testTypeReferenceInThrows
	IType type = getCompilationUnit("JavaSearch", "src", "a7", "X.java").getType("MyException");

	this.resultCollector.showAccuracy(true);
	search(
		type,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/a7/X.java void a7.X.foo() [MyException] EXACT_MATCH",
		this.resultCollector);
}
/**
 * Type reference test.
 * (Regression test for bug 9642 Search - missing inaccurate type matches)
 */
public void testTypeReference25() throws CoreException { // was testInnacurateTypeReference1

	search(
		"Zork",
		TYPE,
		REFERENCES,
		SearchEngine.createJavaSearchScope(new IJavaElement[] {
			getPackageFragment("JavaSearch", "src", "b5")
		}),
		this.resultCollector);
	assertSearchResults(
		"src/b5/A.java [Zork]\n" +
		"src/b5/A.java b5.A.{} [Zork]\n" +
		"src/b5/A.java b5.A.{} [Zork]\n" +
		"src/b5/A.java b5.A.{} [Zork]",
		this.resultCollector);
}
/**
 * Type reference test.
 * (Regression test for bug 9642 Search - missing inaccurate type matches)
 */
public void testTypeReference26() throws CoreException { // was testInnacurateTypeReference2

	this.resultCollector.showAccuracy(true);
	this.resultCollector.showSelection();
	search(
		"p.Zork",
		TYPE,
		REFERENCES,
		SearchEngine.createJavaSearchScope(new IJavaElement[] {
			getPackageFragment("JavaSearch", "src", "b5")
		}),
		this.resultCollector);
	assertSearchResults(
			"src/b5/A.java b5.A.{} [    §|Zork|§[] zork = new Zork[0];] POTENTIAL_MATCH\n" +
			"src/b5/A.java b5.A.{} [    Zork[] zork = new §|Zork|§[0];] POTENTIAL_MATCH\n" +
			"src/b5/A.java b5.A.{} [    int i = §|Zork|§.foo;] POTENTIAL_MATCH",
		this.resultCollector);
}
/**
 * Type reference test.
 * (Regression test for bug 21485  NPE when doing a reference search to a package)
 */
public void testTypeReference27() throws CoreException { // was testInnacurateTypeReference3
	IType type = getCompilationUnit("JavaSearch", "src", "r3", "A21485.java").getType("A21485");

	this.resultCollector.showAccuracy(true);
	search(
		type,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/r4/B21485.java [r3.A21485] EXACT_MATCH\n" +
		"src/r4/B21485.java r4.B21485 [A21485] POTENTIAL_MATCH",
		this.resultCollector);
}
/**
 * Type reference in cast test.
 * (regression test for bug 23329 search: incorrect range for type references in brackets)
 */
public void testTypeReference28() throws CoreException { // was testTypeReferenceInCast
	IType type = getCompilationUnit("JavaSearch", "src", "s3", "A.java").getType("B");

	search(
		type,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/s3/A.java Object s3.A.foo() [B]",
		this.resultCollector);
}
/**
 * Test pattern match type reference in binary
 * (regression test for bug 24741 Search does not find patterned type reference in binary project  )
 */
public void testTypeReference29() throws CoreException { // was testPatternMatchTypeReference

	search(
		"p24741.*",
		TYPE,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"test24741.jar q24741.B",
		this.resultCollector);
}
/**
 * Type reference test (not case sensitive)
 */
public void testTypeReference30() throws CoreException { // was testTypeReferenceNotCaseSensitive
	IPackageFragment pkg = getPackageFragment("JavaSearch", "src", "d4");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {pkg});
	SearchPattern pattern = createPattern("Y", TYPE, REFERENCES, false);

	search(
		pattern,
		scope,
		this.resultCollector);
	assertSearchResults(
		"src/d4/X.java Object d4.X.foo() [Y]",
		this.resultCollector);
}
/**
 * Type reference test.
 */
public void testTypeReference31() throws CoreException { // was testAccurateTypeReference

	search(
		"d5.X",
		TYPE,
		REFERENCES,
		SearchEngine.createJavaSearchScope(new IJavaElement[] {
			getPackageFragment("JavaSearch", "src", "d5")
		}),
		this.resultCollector);
	assertSearchResults(
		"src/d5/Y.java d5.Y.T [d5.X]\n" +
		"src/d5/Y.java d5.Y.c [d5.X]\n" +
		"src/d5/Y.java d5.Y.o [d5.X]",
		this.resultCollector);
}
/**
 * Type reference in hierarchy test.
 * (regression test for bug 28236 Search for refs to class in hierarchy matches class outside hierarchy )
 */
public void testTypeReference32() throws CoreException { // was testTypeReferenceInHierarchy
	IType type = getCompilationUnit("JavaSearch", "src", "d9.p1", "A.java").getType("A");

	IJavaSearchScope scope = SearchEngine.createHierarchyScope(type);
	search(
		type,
		REFERENCES,
		scope,
		this.resultCollector);
	assertSearchResults(
		"",
		this.resultCollector);
}
/**
 * Type reference with recovery test.
 * (Regression test for bug 29366 Search reporting invalid inaccurate match )
 */
public void testTypeReference33() throws CoreException { // was testTypeReferenceWithRecovery
	IType type = getCompilationUnit("JavaSearch", "src", "e1", "A29366.java").getType("A29366");

	this.resultCollector.showAccuracy(true);
	search(
		type,
		REFERENCES,
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/e1/A29366.java void e1.A29366.foo() [A29366] EXACT_MATCH",
		this.resultCollector);
}
/**
 * Type reference with problem test.
 * (Regression test for bug 36479 Rename operation during refactoring fails)
 */
public void testTypeReference34() throws CoreException { // was testTypeReferenceWithProblem
	IType type = getCompilationUnit("JavaSearch", "src", "e6", "A.java").getType("A");

	this.resultCollector.showAccuracy(true);
	search(
		"B36479",
		TYPE,
		REFERENCES,
		SearchEngine.createJavaSearchScope(new IJavaElement[] {type}),
		this.resultCollector);
	assertSearchResults(
		"src/e6/A.java Object e6.A.foo() [B36479] POTENTIAL_MATCH",
		this.resultCollector);
}
/**
 * Type reference with corrupt jar on the classpath test.
 * (Regression test for bug 39831 Search finds only "inexact" matches)
 */
public void testTypeReference35() throws CoreException { // was testTypeReferenceWithCorruptJar
	IJavaProject project = getJavaProject("JavaSearch");
	IClasspathEntry[] originalCP = project.getRawClasspath();
	try {
		// add corrupt.jar to classpath
		int cpLength = originalCP.length;
		IClasspathEntry[] newCP = new IClasspathEntry[cpLength+1];
		System.arraycopy(originalCP, 0, newCP, 0, cpLength);
		newCP[cpLength] = JavaCore.newLibraryEntry(new Path("/JavaSearch/corrupt.jar"), null, null);
		project.setRawClasspath(newCP, null);

		IType type = getCompilationUnit("JavaSearch", "src", "e7", "A.java").getType("A");

		this.resultCollector.showAccuracy(true);
		search(
			type,
			REFERENCES,
			SearchEngine.createJavaSearchScope(new IJavaElement[] {project}),
			this.resultCollector);
		assertSearchResults(
			"src/e7/A.java e7.A.a [A] EXACT_MATCH",
			this.resultCollector);
	} finally {
		project.setRawClasspath(originalCP, null);
	}
}
/*
 * Local type reference test.
 */
public void testTypeReference36() throws CoreException { // was testLocalTypeReference1
	IPackageFragment pkg = getPackageFragment("JavaSearch", "src", "f2");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {pkg});

	this.resultCollector.showSelection();
	search(
		"Y",
		TYPE,
		REFERENCES,
		scope,
		this.resultCollector);
	assertSearchResults(
		"src/f2/X.java Object f2.X.foo1() [		return new §|Y|§();]",
		this.resultCollector);
}
/*
 * Local type reference test.
 */
public void testTypeReference37() throws CoreException { // was testLocalTypeReference2
	IType type = getCompilationUnit("JavaSearch/src/f2/X.java").getType("X");
	IMethod method = type.getMethod("foo1", new String[0]);
	IType localType = method.getType("Y", 1);

	IJavaSearchScope scope = SearchEngine.createWorkspaceScope();

	this.resultCollector.showSelection();
	search(
		localType,
		REFERENCES,
		scope,
		this.resultCollector);
	assertSearchResults(
		"src/f2/X.java Object f2.X.foo1() [		return new §|Y|§();]",
		this.resultCollector);
}
/**
 * Type reference inside/outside doc comment.
 */
public void testTypeReference38() throws CoreException { // was testTypeReferenceInOutDocComment
	IType type = getCompilationUnit("JavaSearch", "src", "s4", "X.java").getType("X");
	this.resultCollector.showInsideDoc();
	search(type, REFERENCES, getJavaSearchScope(), this.resultCollector);
	assertSearchResults(
		"src/s4/X.java void s4.X.bar() [X] INSIDE_JAVADOC\n" +
		"src/s4/X.java void s4.X.bar() [X] INSIDE_JAVADOC\n" +
		"src/s4/X.java void s4.X.bar() [X] INSIDE_JAVADOC\n" +
		"src/s4/X.java void s4.X.fred() [X] OUTSIDE_JAVADOC",
		this.resultCollector);
}

/**
 * Search for enumerations
 */
public void testEnum01() throws CoreException {
	IType type = getCompilationUnit("JavaSearch15", "src", "e1", "Team.java").getType("Team");
	search(type, REFERENCES, getJavaSearchScope15(), this.resultCollector);
	assertSearchResults(
		"src/a1/Author.java [e1.Team]\n" +
		"src/a1/Author.java [e1.Team]\n" +
		"src/a1/Author.java Team[] a1.Author.name() [Team]\n" +
		"src/a1/Test.java [e1.Team]\n" +
		"src/e1/Test.java void e1.Test.main(String[]) [Team]\n" +
		"src/e1/Test.java void e1.Test.main(String[]) [Team]\n" +
		"src/e1/Test.java Location e1.Test.location(Team) [Team]",
		this.resultCollector);
}
public void testEnum02() throws CoreException {
	IType type = getCompilationUnit("JavaSearch15", "src", "e1", "Team.java").getType("Team");
	IMethod method = type.getMethod("Team", new String[0]);
	search(method, REFERENCES, getJavaSearchScope15("e1", false), this.resultCollector);
	assertSearchResults(
		"src/e1/Team.java e1.Team.FREDERIC [FREDERIC]",
		this.resultCollector);
}
public void testEnum03() throws CoreException {
	IType type = getCompilationUnit("JavaSearch15", "src", "e1", "Team.java").getType("Team");
	IMethod method = type.getMethod("Team", new String[] { "I" });
	search(method, REFERENCES, getJavaSearchScope15("e1", false), this.resultCollector);
	assertSearchResults(
		"src/e1/Team.java e1.Team.PHILIPPE [PHILIPPE(37)]\n" +
		"src/e1/Team.java e1.Team.DAVID [DAVID(27)]\n" +
		"src/e1/Team.java e1.Team.JEROME [JEROME(33)]\n" +
		"src/e1/Team.java e1.Team.OLIVIER [OLIVIER(35)]\n" +
		"src/e1/Team.java e1.Team.KENT [KENT(40)]",
		this.resultCollector);
}
public void testEnum04() throws CoreException {
	IType type = getCompilationUnit("JavaSearch15", "src", "e1", "Team.java").getType("Team");
	IMethod method = type.getMethod("age", new String[0]);
	search(method, REFERENCES, getJavaSearchScope15("e1", false), this.resultCollector);
	assertSearchResults(
		"src/e1/Test.java void e1.Test.main(String[]) [age()]",
		this.resultCollector);
}
public void testEnum05() throws CoreException {
	IType type = getCompilationUnit("JavaSearch15", "src", "e1", "Team.java").getType("Team");
	IMethod method = type.getMethod("isManager", new String[0]);
	search(method, ALL_OCCURRENCES, getJavaSearchScope15("e1", false), this.resultCollector);
	assertSearchResults(
		"src/e1/Team.java boolean e1.Team.PHILIPPE:<anonymous>#1.isManager() [isManager]\n" +
		"src/e1/Team.java boolean e1.Team.isManager() [isManager]\n" +
		"src/e1/Test.java void e1.Test.main(String[]) [isManager()]",
		this.resultCollector);
}
public void testEnum06() throws CoreException {
	IType type = getCompilationUnit("JavaSearch15", "src", "e1", "Team.java").getType("Team");
	IMethod method = type.getMethod("setRole", new String[] { "Z" });
	search(method, REFERENCES, getJavaSearchScope15("e1", false), this.resultCollector);
	assertSearchResults(
		"src/e1/Test.java void e1.Test.main(String[]) [setRole(t.isManager())]",
		this.resultCollector);
}
/**
 * Search method with varargs
 */
public void testVarargs01() throws CoreException {
	IType type = getCompilationUnit("JavaSearch15", "src", "v1", "X.java").getType("X");
	IMethod method = type.getMethod("vargs", new String[] { "I", "I" });
	search(method, ALL_OCCURRENCES, getJavaSearchScope15(), this.resultCollector);
	assertSearchResults(
		"src/v1/X.java void v1.X.vargs(int, int) [vargs]\n" +
		"src/v1/X.java void v1.X.bar() [vargs(1, 2)]",
		this.resultCollector);
}
public void testVarargs02() throws CoreException {
	IType type = getCompilationUnit("JavaSearch15", "src", "v1", "X.java").getType("X");
	IMethod method = type.getMethod("vargs", new String[] { "I", "[I" });
	search(method, ALL_OCCURRENCES, getJavaSearchScope15(), this.resultCollector);
	assertSearchResults(
		"src/v1/X.java void v1.X.vargs(int, int ...) [vargs]\n" +
		"src/v1/X.java void v1.X.bar() [vargs(1, 2, 3)]\n" +
		"src/v1/X.java void v1.X.bar() [vargs(1, 2, 3, 4, 5, 6)]",
		this.resultCollector);
}
public void testVarargs03() throws CoreException {
	IType type = getCompilationUnit("JavaSearch15", "src", "v1", "X.java").getType("X");
	IMethod method = type.getMethod("vargs", new String[] { "[QString;" });
	search(method, ALL_OCCURRENCES, getJavaSearchScope15(), this.resultCollector);
	assertSearchResults(
		"src/v1/X.java void v1.X.vargs(String ...) [vargs]\n" +
		"src/v1/X.java void v1.X.bar() [vargs(\"x\", \"a\",\"'b\", \"c\")]",
		this.resultCollector);
}
public void testVarargs04() throws CoreException {
	IType type = getCompilationUnit("JavaSearch15", "src", "v1", "X.java").getType("X");
	IMethod method = type.getMethod("vargs", new String[] { "QString;", "[Z" });
	search(method, ALL_OCCURRENCES, getJavaSearchScope15(), this.resultCollector);
	assertSearchResults(
		"src/v1/X.java void v1.X.vargs(String, boolean ...) [vargs]\n" +
		"src/v1/X.java void v1.X.bar() [vargs(\"x\", false, true)]",
		this.resultCollector);
}
public void testVarargs05() throws CoreException {
	search("vargs", METHOD, DECLARATIONS, getJavaSearchScope15(), this.resultCollector);
	assertSearchResults(
		"src/v1/X.java void v1.X.vargs(int, int) [vargs]\n" +
		"src/v1/X.java void v1.X.vargs(int, int ...) [vargs]\n" +
		"src/v1/X.java void v1.X.vargs(String ...) [vargs]\n" +
		"src/v1/X.java void v1.X.vargs(String, boolean ...) [vargs]",
		this.resultCollector);
}
public void testVarargs06() throws CoreException {
	search("vargs", METHOD, REFERENCES, getJavaSearchScope15(), this.resultCollector);
	assertSearchResults(
		"src/v1/X.java void v1.X.bar() [vargs(1, 2)]\n" +
		"src/v1/X.java void v1.X.bar() [vargs(1, 2, 3)]\n" +
		"src/v1/X.java void v1.X.bar() [vargs(1, 2, 3, 4, 5, 6)]\n" +
		"src/v1/X.java void v1.X.bar() [vargs(\"x\", \"a\",\"\'b\", \"c\")]\n" +
		"src/v1/X.java void v1.X.bar() [vargs(\"x\", false, true)]",
		this.resultCollector);
}
/**
 * Search for annotations
 */
public void testAnnotationType01() throws CoreException {
	IType type = getCompilationUnit("JavaSearch15", "src", "a1", "Author.java").getType("Author");
	search(type, REFERENCES, getJavaSearchScope15(), this.resultCollector);
	assertSearchResults(
		"src/a1/Test.java a1.Test [Author]\n" +
		"src/a1/Test.java a1.Test.t [Author]\n" +
		"src/a1/Test.java void a1.Test.foo() [Author]",
		this.resultCollector);
}
public void testAnnotationType02() throws CoreException {
	ICompilationUnit unit = getCompilationUnit("JavaSearch15", "src", "a1", "Test.java");
	IType type = selectType(unit, "Author");
	search(type, DECLARATIONS, getJavaSearchScope15(), this.resultCollector);
	assertSearchResults(
		"src/a1/Author.java a1.Author [Author]",
		this.resultCollector);
}

/**
 * Search for auto-boxing
 */
public void testAutoBoxing01() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearch15/src/p/X.java",
		"package p;\n" +
		"public class X {\n" +
		"	void foo(int x) {}\n" +
		"	void bar() {\n" +
		"		foo(new Integer(0));\n" +
		"	}\n" +
		"}\n"
		);
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(this.workingCopies);
	IMethod method = this.workingCopies[0].getType("X").getMethod("foo", new String[] { "I" });
	search(method, REFERENCES, scope);
	assertSearchResults(
		"src/p/X.java void p.X.bar() [foo(new Integer(0))]"
	);
}

/**
 * Search for type parameters
 */
/*
 * Type type parameters
 */
public void testTypeParameterTypes01() throws CoreException {
	ICompilationUnit unit = getCompilationUnit("JavaSearch15", "src", "g5.m.def", "Single.java");
	ITypeParameter typeParam = selectTypeParameter(unit, "T");
	search(typeParam, REFERENCES, getJavaSearchScope15());
	assertSearchResults(
		"src/g5/m/def/Single.java void g5.m.def.Single.standard(T) [T]\n" +
		"src/g5/m/def/Single.java T g5.m.def.Single.generic(U) [T]\n" +
		"src/g5/m/def/Single.java Single<T> g5.m.def.Single.returnParamType() [T]\n" +
		"src/g5/m/def/Single.java Single<T> g5.m.def.Single.returnParamType() [T]\n" +
		"src/g5/m/def/Single.java void g5.m.def.Single.paramTypesArgs(Single<T>) [T]\n" +
		"src/g5/m/def/Single.java Single<T> g5.m.def.Single.complete(U, Single<T>) [T]\n" +
		"src/g5/m/def/Single.java Single<T> g5.m.def.Single.complete(U, Single<T>) [T]\n" +
		"src/g5/m/def/Single.java Single<T> g5.m.def.Single.complete(U, Single<T>) [T]"
	);
}
public void testTypeParameterTypes02() throws CoreException {
	ICompilationUnit unit = getCompilationUnit("JavaSearch15", "src", "g5.m.def", "Single.java");
	ITypeParameter typeParam = selectTypeParameter(unit, "T", 3); // return type of returnParamType() method
	search(typeParam, DECLARATIONS, getJavaSearchScope15());
	assertSearchResults(
		"src/g5/m/def/Single.java g5.m.def.Single [T]"
	);
}
public void testTypeParameterTypes03() throws CoreException {
	ICompilationUnit unit = getCompilationUnit("JavaSearch15", "src", "g5.m.def", "Single.java");
	ITypeParameter typeParam = selectTypeParameter(unit, "T", 4); // type argument of Single<T>
	search(typeParam, ALL_OCCURRENCES, getJavaSearchScope15());
	assertSearchResults(
		"src/g5/m/def/Single.java g5.m.def.Single [T]\n" +
		"src/g5/m/def/Single.java void g5.m.def.Single.standard(T) [T]\n" +
		"src/g5/m/def/Single.java T g5.m.def.Single.generic(U) [T]\n" +
		"src/g5/m/def/Single.java Single<T> g5.m.def.Single.returnParamType() [T]\n" +
		"src/g5/m/def/Single.java Single<T> g5.m.def.Single.returnParamType() [T]\n" +
		"src/g5/m/def/Single.java void g5.m.def.Single.paramTypesArgs(Single<T>) [T]\n" +
		"src/g5/m/def/Single.java Single<T> g5.m.def.Single.complete(U, Single<T>) [T]\n" +
		"src/g5/m/def/Single.java Single<T> g5.m.def.Single.complete(U, Single<T>) [T]\n" +
		"src/g5/m/def/Single.java Single<T> g5.m.def.Single.complete(U, Single<T>) [T]"
	);
}
public void testTypeParameterTypes04() throws CoreException {
	ICompilationUnit unit = getCompilationUnit("JavaSearch15", "src", "g5.m.def", "Multiple.java");
	ITypeParameter typeParam = selectTypeParameter(unit, "T3");
	search(typeParam, REFERENCES, getJavaSearchScope15());
	assertSearchResults(
		"src/g5/m/def/Multiple.java void g5.m.def.Multiple.standard(T1, T2, T3) [T3]\n" +
		"src/g5/m/def/Multiple.java Multiple<T1,T2,T3> g5.m.def.Multiple.returnParamType() [T3]\n" +
		"src/g5/m/def/Multiple.java Multiple<T1,T2,T3> g5.m.def.Multiple.returnParamType() [T3]\n" +
		"src/g5/m/def/Multiple.java void g5.m.def.Multiple.paramTypesArgs(Single<T1>, Single<T2>, Single<T3>, Multiple<T1,T2,T3>) [T3]\n" +
		"src/g5/m/def/Multiple.java void g5.m.def.Multiple.paramTypesArgs(Single<T1>, Single<T2>, Single<T3>, Multiple<T1,T2,T3>) [T3]\n" +
		"src/g5/m/def/Multiple.java Multiple<T1,T2,T3> g5.m.def.Multiple.complete(U1, U2, U3, Multiple<T1,T2,T3>) [T3]\n" +
		"src/g5/m/def/Multiple.java Multiple<T1,T2,T3> g5.m.def.Multiple.complete(U1, U2, U3, Multiple<T1,T2,T3>) [T3]\n" +
		"src/g5/m/def/Multiple.java Multiple<T1,T2,T3> g5.m.def.Multiple.complete(U1, U2, U3, Multiple<T1,T2,T3>) [T3]"
	);
}
public void testTypeParameterTypes05() throws CoreException {
	ICompilationUnit unit = getCompilationUnit("JavaSearch15", "src", "g5.m.def", "Multiple.java");
	ITypeParameter typeParam = selectTypeParameter(unit, "T1", 2); // return type of returnParamType() method
	search(typeParam, DECLARATIONS, getJavaSearchScope15());
	assertSearchResults(
		"src/g5/m/def/Multiple.java g5.m.def.Multiple [T1]"
	);
}
public void testTypeParameterTypes06() throws CoreException {
	ICompilationUnit unit = getCompilationUnit("JavaSearch15", "src", "g5.m.def", "Multiple.java");
	ITypeParameter typeParam = selectTypeParameter(unit, "T2", 3); // type argument of Multiple<T1,T2,T3>
	search(typeParam, ALL_OCCURRENCES, getJavaSearchScope15());
	assertSearchResults(
		"src/g5/m/def/Multiple.java g5.m.def.Multiple [T2]\n" +
		"src/g5/m/def/Multiple.java void g5.m.def.Multiple.standard(T1, T2, T3) [T2]\n" +
		"src/g5/m/def/Multiple.java Multiple<T1,T2,T3> g5.m.def.Multiple.returnParamType() [T2]\n" +
		"src/g5/m/def/Multiple.java Multiple<T1,T2,T3> g5.m.def.Multiple.returnParamType() [T2]\n" +
		"src/g5/m/def/Multiple.java void g5.m.def.Multiple.paramTypesArgs(Single<T1>, Single<T2>, Single<T3>, Multiple<T1,T2,T3>) [T2]\n" +
		"src/g5/m/def/Multiple.java void g5.m.def.Multiple.paramTypesArgs(Single<T1>, Single<T2>, Single<T3>, Multiple<T1,T2,T3>) [T2]\n" +
		"src/g5/m/def/Multiple.java Multiple<T1,T2,T3> g5.m.def.Multiple.complete(U1, U2, U3, Multiple<T1,T2,T3>) [T2]\n" +
		"src/g5/m/def/Multiple.java Multiple<T1,T2,T3> g5.m.def.Multiple.complete(U1, U2, U3, Multiple<T1,T2,T3>) [T2]\n" +
		"src/g5/m/def/Multiple.java Multiple<T1,T2,T3> g5.m.def.Multiple.complete(U1, U2, U3, Multiple<T1,T2,T3>) [T2]"
	);
}
/*
 * Methods type parameters
 */
public void testTypeParameterMethods01() throws CoreException {
	ICompilationUnit unit = getCompilationUnit("JavaSearch15", "src", "g5.m.def", "Single.java");
	ITypeParameter typeParam = selectTypeParameter(unit, "U");
	search(typeParam, REFERENCES, getJavaSearchScope15());
	assertSearchResults(
		"src/g5/m/def/Single.java T g5.m.def.Single.generic(U) [U]"
	);
}
public void testTypeParameterMethods02() throws CoreException {
	ICompilationUnit unit = getCompilationUnit("JavaSearch15", "src", "g5.m.def", "Single.java");
	ITypeParameter typeParam = selectTypeParameter(unit, "U", 2); // argument of generic method
	search(typeParam, DECLARATIONS, getJavaSearchScope15());
	assertSearchResults(
		"src/g5/m/def/Single.java T g5.m.def.Single.generic(U) [U]"
	);
}
public void testTypeParameterMethods03() throws CoreException {
	ICompilationUnit unit = getCompilationUnit("JavaSearch15", "src", "g5.m.def", "Single.java");
	ITypeParameter typeParam = selectTypeParameter(unit, "U", 4); // argument of complete method
	search(typeParam, ALL_OCCURRENCES, getJavaSearchScope15());
	assertSearchResults(
		"src/g5/m/def/Single.java Single<T> g5.m.def.Single.complete(U, Single<T>) [U]\n" +
		"src/g5/m/def/Single.java Single<T> g5.m.def.Single.complete(U, Single<T>) [U]"
	);
}
public void testTypeParameterMethods04() throws CoreException {
	ICompilationUnit unit = getCompilationUnit("JavaSearch15", "src", "g5.m.def", "Multiple.java");
	ITypeParameter typeParam = selectTypeParameter(unit, "U3", 3); // type parameter of complete method (extends)
	search(typeParam, REFERENCES, getJavaSearchScope15());
	assertSearchResults(
		"src/g5/m/def/Multiple.java Multiple<T1,T2,T3> g5.m.def.Multiple.complete(U1, U2, U3, Multiple<T1,T2,T3>) [U3]"
	);
}
public void testTypeParameterMethods05() throws CoreException {
	ICompilationUnit unit = getCompilationUnit("JavaSearch15", "src", "g5.m.def", "Multiple.java");
	ITypeParameter typeParam = selectTypeParameter(unit, "U1", 2); // argument of generic method
	search(typeParam, DECLARATIONS, getJavaSearchScope15());
	assertSearchResults(
		"src/g5/m/def/Multiple.java T1 g5.m.def.Multiple.generic(U1, U2, U3) [U1]"
	);
}
public void testTypeParameterMethods06() throws CoreException {
	ICompilationUnit unit = getCompilationUnit("JavaSearch15", "src", "g5.m.def", "Multiple.java");
	ITypeParameter typeParam = selectTypeParameter(unit, "U2", 4); // argument of complete method
	search(typeParam, ALL_OCCURRENCES, getJavaSearchScope15());
	assertSearchResults(
		"src/g5/m/def/Multiple.java Multiple<T1,T2,T3> g5.m.def.Multiple.complete(U1, U2, U3, Multiple<T1,T2,T3>) [U2]\n" +
		"src/g5/m/def/Multiple.java Multiple<T1,T2,T3> g5.m.def.Multiple.complete(U1, U2, U3, Multiple<T1,T2,T3>) [U2]"
	);
}
/*
 * Constructors type parameters
 */
public void testTypeParameterConstructors01() throws CoreException {
	ICompilationUnit unit = getCompilationUnit("JavaSearch15", "src", "g5.c.def", "Single.java");
	ITypeParameter typeParam = selectTypeParameter(unit, "U");
	search(typeParam, REFERENCES, getJavaSearchScope15());
	assertSearchResults(
		"src/g5/c/def/Single.java g5.c.def.Single(T, U) [U]"
	);
}
public void testTypeParameterConstructors02() throws CoreException {
	ICompilationUnit unit = getCompilationUnit("JavaSearch15", "src", "g5.c.def", "Single.java");
	ITypeParameter typeParam = selectTypeParameter(unit, "U");
	// TODO (frederic) use following line instead when bug 83438 will be fixed
	// ITypeParameter typeParam = selectTypeParameter(unit, "U", 2); // argument of generic method
	search(typeParam, DECLARATIONS, getJavaSearchScope15());
	assertSearchResults(
		"src/g5/c/def/Single.java g5.c.def.Single(T, U) [U]"
	);
}
public void testTypeParameterConstructors03() throws CoreException {
	ICompilationUnit unit = getCompilationUnit("JavaSearch15", "src", "g5.c.def", "Single.java");
	ITypeParameter typeParam = selectTypeParameter(unit, "U");
	// TODO (frederic) use following line instead when bug 83438 will be fixed
	//ITypeParameter typeParam = selectTypeParameter(unit, "U", 4); // argument of complete method
	search(typeParam, ALL_OCCURRENCES, getJavaSearchScope15());
	assertSearchResults(
		"src/g5/c/def/Single.java g5.c.def.Single(T, U) [U]\n" +
		"src/g5/c/def/Single.java g5.c.def.Single(T, U) [U]"
	);
}
public void testTypeParameterConstructors04() throws CoreException {
	ICompilationUnit unit = getCompilationUnit("JavaSearch15", "src", "g5.c.def", "Multiple.java");
	ITypeParameter typeParam = selectTypeParameter(unit, "U3", 3); // type parameter of complete method (extends)
	search(typeParam, REFERENCES, getJavaSearchScope15());
	assertSearchResults(
		"src/g5/c/def/Multiple.java g5.c.def.Multiple(U1, U2, U3, Multiple<T1,T2,T3>) [U3]"
	);
}
public void testTypeParameterConstructors05() throws CoreException {
	ICompilationUnit unit = getCompilationUnit("JavaSearch15", "src", "g5.c.def", "Multiple.java");
	ITypeParameter typeParam = selectTypeParameter(unit, "U1");
	// TODO (frederic) use following line instead when bug 83438 will be fixed
	//ITypeParameter typeParam = selectTypeParameter(unit, "U1", 2); // argument of generic method
	search(typeParam, DECLARATIONS, getJavaSearchScope15());
	assertSearchResults(
		"src/g5/c/def/Multiple.java g5.c.def.Multiple(Multiple<T1,T2,T3>, U1, U2, U3) [U1]"
	);
}
public void testTypeParameterConstructors06() throws CoreException {
	ICompilationUnit unit = getCompilationUnit("JavaSearch15", "src", "g5.c.def", "Multiple.java");
	ITypeParameter typeParam = selectTypeParameter(unit, "U2", 3);
	// TODO (frederic) use following line instead when bug 83438 will be fixed
	//ITypeParameter typeParam = selectTypeParameter(unit, "U2", 4); // argument of complete method
	search(typeParam, ALL_OCCURRENCES, getJavaSearchScope15());
	assertSearchResults(
		"src/g5/c/def/Multiple.java g5.c.def.Multiple(U1, U2, U3, Multiple<T1,T2,T3>) [U2]\n" +
		"src/g5/c/def/Multiple.java g5.c.def.Multiple(U1, U2, U3, Multiple<T1,T2,T3>) [U2]"
	);
}

/**
 * Test static import
 */
// for fields
public void testStaticImportField01() throws CoreException {
	IType type = getCompilationUnit("JavaSearch15", "src", "s1.pack.age", "S.java").getType("S");
	search(type, REFERENCES, getJavaSearchScope15(), this.resultCollector);
	assertSearchResults(
		"src/s1/A.java [s1.pack.age.S]\n" +
		"src/s1/A.java [s1.pack.age.S]\n" +
		"src/s1/A.java [s1.pack.age.S]\n" +
		"src/s1/B.java [s1.pack.age.S]\n" +
		"src/s1/B.java [s1.pack.age.S]\n" +
		"src/s1/C.java [s1.pack.age.S]\n" +
		"src/s1/C.java [s1.pack.age.S]\n" +
		"src/s1/D.java [s1.pack.age.S]"
	);
}
public void testStaticImportField02() throws CoreException {
	IType type = getCompilationUnit("JavaSearch15", "src", "s1.pack.age", "S.java").getType("S");
	IField field = type.getField("out");
	search(field, REFERENCES, getJavaSearchScope15(), this.resultCollector);
	assertSearchResults(
		"src/s1/A.java [out]\n" +
		"src/s1/B.java void s1.B.foo() [out]\n" +
		"src/s1/C.java [out]\n" +
		"src/s1/C.java void s1.C.foo() [out]"
	);
}
public void testStaticImportField03() throws CoreException {
	IType type = getCompilationUnit("JavaSearch15", "src", "s1.pack.age", "S.java").getType("S");
	IType member = type.getType("M");
	search(member, REFERENCES, getJavaSearchScope15(), this.resultCollector);
	assertSearchResults(
		"src/s1/A.java [s1.pack.age.S.M]\n" +
		"src/s1/B.java [s1.pack.age.S.M]\n" +
		"src/s1/C.java [s1.pack.age.S.M]\n" +
		"src/s1/D.java [s1.pack.age.S.M]\n" +
		"src/s1/D.java void s1.D.foo() [M]\n" +
		"src/s1/D.java void s1.D.foo() [M]"
	);
}
public void testStaticImportField04() throws CoreException {
	IType type = getCompilationUnit("JavaSearch15", "src", "s1.pack.age", "S.java").getType("S");
	IType member = type.getType("M");
	IField field = member.getField("in");
	search(field, REFERENCES, getJavaSearchScope15(), this.resultCollector);
	assertSearchResults(
		"src/s1/A.java [in]\n" +
		"src/s1/B.java void s1.B.foo() [in]\n" +
		"src/s1/C.java [in]\n" +
		"src/s1/C.java void s1.C.foo() [in]\n" +
		"src/s1/D.java void s1.D.foo() [in]"
	);
}
// for methods
public void testStaticImportMethod01() throws CoreException {
	IType type = getCompilationUnit("JavaSearch15", "src", "s2.pack.age", "S.java").getType("S");
	search(type, REFERENCES, getJavaSearchScope15(), this.resultCollector);
	assertSearchResults(
		"src/s2/A.java [s2.pack.age.S]\n" +
		"src/s2/A.java [s2.pack.age.S]\n" +
		"src/s2/A.java [s2.pack.age.S]\n" +
		"src/s2/B.java [s2.pack.age.S]\n" +
		"src/s2/B.java [s2.pack.age.S]\n" +
		"src/s2/C.java [s2.pack.age.S]\n" +
		"src/s2/C.java [s2.pack.age.S]\n" +
		"src/s2/D.java [s2.pack.age.S]"
	);
}
public void testStaticImportMethod02() throws CoreException {
	IType type = getCompilationUnit("JavaSearch15", "src", "s2.pack.age", "S.java").getType("S");
	IMethod method = type.getMethod("out", new String[0]);
	search(method, REFERENCES, getJavaSearchScope15(), this.resultCollector);
	assertSearchResults(
		"src/s2/A.java [s2.pack.age.S.out]\n" +
		"src/s2/B.java void s2.B.foo() [out()]\n" +
		"src/s2/C.java [s2.pack.age.S.out]\n" +
		"src/s2/C.java void s2.C.foo() [out()]"
	);
}
public void testStaticImportMethod03() throws CoreException {
	IType type = getCompilationUnit("JavaSearch15", "src", "s2.pack.age", "S.java").getType("S");
	IType member = type.getType("M");
	search(member, REFERENCES, getJavaSearchScope15(), this.resultCollector);
	assertSearchResults(
		"src/s2/A.java [s2.pack.age.S.M]\n" +
		"src/s2/B.java [s2.pack.age.S.M]\n" +
		"src/s2/C.java [s2.pack.age.S.M]\n" +
		"src/s2/D.java [s2.pack.age.S.M]\n" +
		"src/s2/D.java void s2.D.foo() [M]\n" +
		"src/s2/D.java void s2.D.foo() [M]"
	);
}
public void testStaticImportMethod04() throws CoreException {
	IType type = getCompilationUnit("JavaSearch15", "src", "s2.pack.age", "S.java").getType("S");
	IType member = type.getType("M");
	IMethod method = member.getMethod("in", new String[0]);
	search(method, REFERENCES, getJavaSearchScope15(), this.resultCollector);
	assertSearchResults(
		"src/s2/A.java [s2.pack.age.S.M.in]\n" +
		"src/s2/B.java void s2.B.foo() [in()]\n" +
		"src/s2/C.java [s2.pack.age.S.M.in]\n" +
		"src/s2/C.java void s2.C.foo() [in()]\n" +
		"src/s2/D.java void s2.D.foo() [in()]"
	);
}
// for packages
public void testStaticImportPackage01() throws CoreException {
	IPackageFragment pkg = getPackageFragment("JavaSearch15", "src", "s1.pack.age");
	search(pkg, REFERENCES, getJavaSearchScope15(), this.resultCollector);
	assertSearchResults(
		"src/s1/A.java [s1.pack.age]\n" +
		"src/s1/A.java [s1.pack.age]\n" +
		"src/s1/A.java [s1.pack.age]\n" +
		"src/s1/B.java [s1.pack.age]\n" +
		"src/s1/B.java [s1.pack.age]\n" +
		"src/s1/C.java [s1.pack.age]\n" +
		"src/s1/C.java [s1.pack.age]\n" +
		"src/s1/D.java [s1.pack.age]"
	);
}
public void testStaticImportPackage02() throws CoreException {
	IPackageFragment pkg = getPackageFragment("JavaSearch15", "src", "s2.pack.age");
	search(pkg, REFERENCES, getJavaSearchScope15(), this.resultCollector);
	assertSearchResults(
		"src/s2/A.java [s2.pack.age]\n" +
		"src/s2/A.java [s2.pack.age]\n" +
		"src/s2/A.java [s2.pack.age]\n" +
		"src/s2/B.java [s2.pack.age]\n" +
		"src/s2/B.java [s2.pack.age]\n" +
		"src/s2/C.java [s2.pack.age]\n" +
		"src/s2/C.java [s2.pack.age]\n" +
		"src/s2/D.java [s2.pack.age]"
	);
}

/**
 * @test Bug 110060: [plan][search] Add support for Camel Case search pattern
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=110060"
 *
 * These tests are not really duplicates of {@link JavaSearchBugsTests} ones
 * as they also test camel case in indexes...
 */
public void testCamelCaseTypePattern01_CamelCase() throws CoreException {
	search("RE", TYPE, DECLARATIONS, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/a3/References.java a3.References [References]\n" +
		""+ getExternalJCLPathString() + " java.lang.RuntimeException"
	);
}
public void testCamelCaseTypePattern02_CamelCase() throws CoreException {
	search("RException", TYPE, DECLARATIONS, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		""+ getExternalJCLPathString() + " java.lang.RuntimeException"
	);
}
public void testCamelCaseTypePattern03_CamelCase() throws CoreException {
	search("RuntimeException", TYPE, DECLARATIONS, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		""+ getExternalJCLPathString() + " java.lang.RuntimeException"
	);
}
public void testCamelCaseTypePattern04_CamelCase() throws CoreException {
	search("RUNTIMEEXCEPTION", TYPE, DECLARATIONS, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		""+ getExternalJCLPathString() + " java.lang.RuntimeException"
	);
}
public void testCamelCaseTypePattern05_CamelCase() throws CoreException {
	search("R*E*", TYPE, DECLARATIONS, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/a3/References.java a3.References [References]\n" +
		""+ getExternalJCLPathString() + " java.lang.RuntimeException"
	);
}
public void testCamelCaseTypePattern06_CamelCase() throws CoreException {
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	searchAllTypeNames("CNS", SearchPattern.R_CAMELCASE_MATCH, requestor);
	assertSearchResults(
		"Unexpected all type names",
		"java.lang.CloneNotSupportedException",
		requestor);
}
public void testCamelCaseTypePattern07_CamelCase() throws CoreException {
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	searchAllTypeNames("AA", SearchPattern.R_CAMELCASE_MATCH, requestor);
	assertSearchResults(
		"Unexpected all type names",
		"AA\n" +
		"c9.AbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyz\n" +
		"d8.AA\n" +
		"p6.AA\n" +
		"q1.AA",
		requestor);
}
public void testCamelCaseTypePattern08_CamelCase() throws CoreException {
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	searchAllTypeNames("aa", SearchPattern.R_CAMELCASE_MATCH, requestor);
	assertSearchResults(
		"Unexpected all type names",
		"AA\n" +
		"d8.AA\n" +
		"p6.AA\n" +
		"q1.AA",
		requestor);
}
public void testCamelCaseTypePattern09_CamelCase() throws CoreException {
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	searchAllTypeNames("aa", SearchPattern.R_CAMELCASE_MATCH | SearchPattern.R_PREFIX_MATCH, requestor);
	assertSearchResults(
		"Unexpected all type names",
		"AA\n" +
		"d8.AA\n" +
		"p6.AA\n" +
		"q1.AA",
		requestor);
}
public void testCamelCaseTypePattern10_CamelCase() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearch/src/Test.java",
		"class Null {}\n" +
		"class NULL {}\n" +
		"class NullPointerException {}\n" +
		"class Nullpointerexception {}\n" +
		"class NULLPointerException {}\n" +
		"class NULLPOINTERException {}\n" +
		"class NULLPOINTEREXCEPTION {}\n"
	);
	search("Null", IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/Test.java Null [Null]\n" +
		"src/Test.java NULL [NULL]\n" +
		"src/Test.java NullPointerException [NullPointerException]\n" +
		"src/Test.java Nullpointerexception [Nullpointerexception]\n" +
		"src/Test.java NULLPointerException [NULLPointerException]\n" +
		"src/Test.java NULLPOINTERException [NULLPOINTERException]\n" +
		"src/Test.java NULLPOINTEREXCEPTION [NULLPOINTEREXCEPTION]"
	);
}
public void testCamelCaseTypePattern11_CamelCase() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearch/src/Test.java",
		"class Null {}\n" +
		"class NULL {}\n" +
		"class NullPointerException {}\n" +
		"class Nullpointerexception {}\n" +
		"class NULLPointerException {}\n" +
		"class NULLPOINTERException {}\n" +
		"class NULLPOINTEREXCEPTION {}\n"
	);
	search("Null", IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_CAMELCASE_MATCH | SearchPattern.R_PREFIX_MATCH);
	assertSearchResults(
		"src/Test.java Null [Null]\n" +
		"src/Test.java NULL [NULL]\n" +
		"src/Test.java NullPointerException [NullPointerException]\n" +
		"src/Test.java Nullpointerexception [Nullpointerexception]\n" +
		"src/Test.java NULLPointerException [NULLPointerException]\n" +
		"src/Test.java NULLPOINTERException [NULLPOINTERException]\n" +
		"src/Test.java NULLPOINTEREXCEPTION [NULLPOINTEREXCEPTION]"
	);
}
public void testCamelCaseTypePattern12_CamelCase() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearch/src/Test.java",
		"class Null {}\n" +
		"class NULL {}\n" +
		"class NullPointerException {}\n" +
		"class Nullpointerexception {}\n" +
		"class NULLPointerException {}\n" +
		"class NULLPOINTERException {}\n" +
		"class NULLPOINTEREXCEPTION {}\n"
	);
	search("Null", IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_CAMELCASE_MATCH | SearchPattern.R_CASE_SENSITIVE);
	assertSearchResults(
		"src/Test.java Null [Null]\n" +
		"src/Test.java NullPointerException [NullPointerException]\n" +
		"src/Test.java Nullpointerexception [Nullpointerexception]"
	);
}
public void testCamelCaseTypePattern13_CamelCase() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearch/src/Test.java",
		"class Null {}\n" +
		"class NULL {}\n" +
		"class NullPointerException {}\n" +
		"class Nullpointerexception {}\n" +
		"class NULLPointerException {}\n" +
		"class NULLPOINTERException {}\n" +
		"class NULLPOINTEREXCEPTION {}\n"
	);
	search("Null", IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_CAMELCASE_MATCH | SearchPattern.R_PREFIX_MATCH | SearchPattern.R_CASE_SENSITIVE);
	assertSearchResults(
		"src/Test.java Null [Null]\n" +
		"src/Test.java NullPointerException [NullPointerException]\n" +
		"src/Test.java Nullpointerexception [Nullpointerexception]"
	);
}
// Same tests using SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH instead
public void testCamelCaseTypePattern01_CamelCaseSamePartCount() throws CoreException {
	search("RE", TYPE, DECLARATIONS, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults(
		""+ getExternalJCLPathString() + " java.lang.RuntimeException"
	);
}
public void testCamelCaseTypePattern02_CamelCaseSamePartCount() throws CoreException {
	search("RException", TYPE, DECLARATIONS, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults(
		""+ getExternalJCLPathString() + " java.lang.RuntimeException"
	);
}
public void testCamelCaseTypePattern03_CamelCaseSamePartCount() throws CoreException {
	search("RuntimeException", TYPE, DECLARATIONS, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults(
		""+ getExternalJCLPathString() + " java.lang.RuntimeException"
	);
}
public void testCamelCaseTypePattern04_CamelCaseSamePartCount() throws CoreException {
	search("RUNTIMEEXCEPTION", TYPE, DECLARATIONS, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults("");
}
public void testCamelCaseTypePattern05_CamelCaseSamePartCount() throws CoreException {
	search("R*E*", TYPE, DECLARATIONS, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults(
		"src/a3/References.java a3.References [References]\n" +
		""+ getExternalJCLPathString() + " java.lang.RuntimeException"
	);
}
public void testCamelCaseTypePattern06_CamelCaseSamePartCount() throws CoreException {
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	searchAllTypeNames("CNS", SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH, requestor);
	assertSearchResults(
		"Unexpected all type names",
		"",
		requestor);
}
public void testCamelCaseTypePattern07_CamelCaseSamePartCount() throws CoreException {
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	searchAllTypeNames("AA", SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH, requestor);
	assertSearchResults(
		"Unexpected all type names",
		"AA\n" +
		"d8.AA\n" +
		"p6.AA\n" +
		"q1.AA",
		requestor);
}
public void testCamelCaseTypePattern08_CamelCaseSamePartCount() throws CoreException {
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	searchAllTypeNames("aa", SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH, requestor);
	assertSearchResults(
		"Unexpected all type names",
		"AA\n" +
		"d8.AA\n" +
		"p6.AA\n" +
		"q1.AA",
		requestor);
}
public void testCamelCaseTypePattern09_CamelCaseSamePartCount() throws CoreException {
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	searchAllTypeNames("aa", SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH | SearchPattern.R_PREFIX_MATCH, requestor);
	assertSearchResults(
		"Unexpected all type names",
		"AA\n" +
		"d8.AA\n" +
		"p6.AA\n" +
		"q1.AA",
		requestor);
}
public void testCamelCaseTypePattern10_CamelCaseSamePartCount() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearch/src/Test.java",
		"class Null {}\n" +
		"class NULL {}\n" +
		"class NullPointerException {}\n" +
		"class Nullpointerexception {}\n" +
		"class NULLPointerException {}\n" +
		"class NULLPOINTERException {}\n" +
		"class NULLPOINTEREXCEPTION {}\n"
	);
	search("Null", IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults(
		"src/Test.java Null [Null]\n" +
		"src/Test.java NULL [NULL]"
	);
}
public void testCamelCaseTypePattern11_CamelCaseSamePartCount() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearch/src/Test.java",
		"class Null {}\n" +
		"class NULL {}\n" +
		"class NullPointerException {}\n" +
		"class Nullpointerexception {}\n" +
		"class NULLPointerException {}\n" +
		"class NULLPOINTERException {}\n" +
		"class NULLPOINTEREXCEPTION {}\n"
	);
	search("Null", IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH | SearchPattern.R_PREFIX_MATCH);
	assertSearchResults(
		"src/Test.java Null [Null]\n" +
		"src/Test.java NULL [NULL]"
	);
}
public void testCamelCaseTypePattern12_CamelCaseSamePartCount() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearch/src/Test.java",
		"class Null {}\n" +
		"class NULL {}\n" +
		"class NullPointerException {}\n" +
		"class Nullpointerexception {}\n" +
		"class NULLPointerException {}\n" +
		"class NULLPOINTERException {}\n" +
		"class NULLPOINTEREXCEPTION {}\n"
	);
	search("Null", IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH | SearchPattern.R_CASE_SENSITIVE);
	assertSearchResults(
		"src/Test.java Null [Null]"
	);
}
public void testCamelCaseTypePattern13_CamelCaseSamePartCount() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearch/src/Test.java",
		"class Null {}\n" +
		"class NULL {}\n" +
		"class NullPointerException {}\n" +
		"class Nullpointerexception {}\n" +
		"class NULLPointerException {}\n" +
		"class NULLPOINTERException {}\n" +
		"class NULLPOINTEREXCEPTION {}\n"
	);
	search("Null", IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH | SearchPattern.R_PREFIX_MATCH | SearchPattern.R_CASE_SENSITIVE);
	assertSearchResults(
		"src/Test.java Null [Null]"
	);
}

/**
 * @bug 160323: [search] TypeNameMatch: support hashCode/equals
 * @test Ensure that match equals and hashCode methods return same values than those of stored {@link IType}.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=160323"
 */
public void testBug160323() throws CoreException {
	// Search all type names with TypeNameMatchRequestor
	TypeNameMatchCollector collector = new TypeNameMatchCollector() {
		public String toString(){
			return toFullyQualifiedNamesString();
		}
	};
	new SearchEngine().searchAllTypeNames(
		null,
		SearchPattern.R_EXACT_MATCH,
		null,
		SearchPattern.R_PREFIX_MATCH,
		IJavaSearchConstants.TYPE,
		getJavaSearchScope(),
		collector,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null);
	// Search all type names with TypeNameRequestor
	SearchTests.SearchTypeNameRequestor requestor = new SearchTests.SearchTypeNameRequestor();
	new SearchEngine().searchAllTypeNames(
		null,
		SearchPattern.R_EXACT_MATCH,
		null,
		SearchPattern.R_PREFIX_MATCH,
		IJavaSearchConstants.TYPE,
		getJavaSearchScope(),
		requestor,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null);
	// Should have same types with these 2 searches
	assertEquals("Found types sounds not to be correct", requestor.toString(), collector.toString());
}
}
