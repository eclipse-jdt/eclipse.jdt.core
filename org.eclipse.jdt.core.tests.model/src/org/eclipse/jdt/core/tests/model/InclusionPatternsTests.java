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

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;

import junit.framework.Test;

public class InclusionPatternsTests extends ModifyingResourceTests {
	IJavaProject project;
public InclusionPatternsTests(String name) {
	super(name);
}

static {
//	TESTS_NAMES = new String[] { "testIncludeCUOnly02" };
}
public static Test suite() {
	return buildModelTestSuite(InclusionPatternsTests.class);
}
protected void setClasspath(String[] sourceFoldersAndInclusionPatterns) throws JavaModelException {
	this.project.setRawClasspath(createClasspath(sourceFoldersAndInclusionPatterns, true/*inclusion*/, false/*no exclusion*/), null);
}
@Override
protected void setUp() throws Exception {
	super.setUp();
	this.project = createJavaProject( "P", new String[] {"src"}, new String[] {}, new String[] {}, new boolean[] {}, "bin", new String[] {"bin"}, new String[][] {new String[] {}}, new String[][] {new String[] {"**"}}, "1.4");
	startDeltas();
}

@Override
protected void tearDown() throws Exception {
	stopDeltas();
	deleteProject("P");
	super.tearDown();
}
/*
 * Ensure that adding an inclusion on a compilation unit
 * makes it appear in the children of its package and it is removed from the non-java resources.
 */
public void testAddInclusionOnCompilationUnit() throws CoreException {
	createFolder("/P/src/p");
	createFile(
		"/P/src/p/A.java",
		"package p;\n" +
		"public class A {\n" +
		"}"
	);

	clearDeltas();
	setClasspath(new String[] {"/P/src", "**/A.java"});

	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
		"	src[*]: {ADDED TO CLASSPATH | REMOVED FROM CLASSPATH}\n" +
		"	ResourceDelta(/P/.classpath)[*]"
	);

	IPackageFragment pkg = getPackage("/P/src/p");
	assertSortedElementsEqual(
		"Unexpected children",
		"A.java [in p [in src [in P]]]",
		pkg.getChildren());

	assertResourceNamesEqual(
		"Unexpected non-java resources",
		"",
		pkg.getNonJavaResources());
}
/*
 * Ensure that adding an inclusion on a folder directly under a project (and prj=src)
 * makes it disappear from the non-java resources.
 */
public void testAddInclusionOnFolderUnderProject() throws CoreException {
	try {
		IJavaProject javaProject = createJavaProject("P1", new String[] {""}, "");
		createFolder("/P1/doc");

		clearDeltas();
		javaProject.setRawClasspath(createClasspath(new String[] {"/P1", "doc/"}, true/*inclusion*/, false/*no exclusion*/), null);

		assertDeltas(
			"Unexpected deltas",
			"P1[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
			"	<project root>[*]: {ADDED TO CLASSPATH | REMOVED FROM CLASSPATH}\n" +
			"	ResourceDelta(/P1/.classpath)[*]"
		);

		IPackageFragmentRoot root = getPackageFragmentRoot("/P1");
		assertSortedElementsEqual(
			"Unexpected children",
			"doc [in <project root> [in P1]]",
			root.getChildren());

		assertResourceNamesEqual(
			"Unexpected non-java resources of project",
			".classpath\n" +
			".project\n" +
			".settings",
			javaProject.getNonJavaResources());
	} finally {
		deleteProject("P1");
	}
}
/*
 * Ensure that adding an inclusion on a package
 * makes it appear in the list of children of its package fragment root
 * and it is removed from the non-java resources.
 */
public void testAddInclusionOnPackage() throws CoreException {
	createFolder("/P/src/p");

	clearDeltas();
	setClasspath(new String[] {"/P/src", "p/"});

	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
		"	src[*]: {ADDED TO CLASSPATH | REMOVED FROM CLASSPATH}\n" +
		"	ResourceDelta(/P/.classpath)[*]"
	);

	IPackageFragmentRoot root = getPackageFragmentRoot("/P/src");
	assertSortedElementsEqual(
		"Unexpected children",
		"p [in src [in P]]",
		root.getChildren());

	assertResourceNamesEqual(
		"Unexpected non-java resources",
		"",
		root.getNonJavaResources());
}
/*
 * Ensure that adding a file to a folder that is included reports the correct delta.
 */
public void testAddToIncludedFolder() throws CoreException {
	createFolder("/P/src/p");

	// include folder and its contents
	setClasspath(new String[] {"/P/src", "p/"});

	clearDeltas();
	createFile("/P/src/p/my.txt", "");
	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" +
		"	src[*]: {CHILDREN}\n" +
		"		p[*]: {CONTENT}\n" +
		"			ResourceDelta(/P/src/p/my.txt)[+]"
	);

	clearDeltas();
	deleteFile("/P/src/p/my.txt");
	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" +
		"	src[*]: {CHILDREN}\n" +
		"		p[*]: {CONTENT}\n" +
		"			ResourceDelta(/P/src/p/my.txt)[-]"
	);
}
/*
 * Ensure that creating an included compilation unit
 * doesn't make it appear as a non-java resource but it is a child of its package.
 */
public void testCreateIncludedCompilationUnit() throws CoreException {
	setClasspath(new String[] {"/P/src", "**/A.java"});
	createFolder("/P/src/p");
	IPackageFragment pkg = getPackage("/P/src/p");

	clearDeltas();
	pkg.createCompilationUnit(
		"A.java",
		"package p;\n" +
		"public class A {\n" +
		"}",
		false,
		null);

	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" +
		"	src[*]: {CHILDREN}\n" +
		"		p[*]: {CHILDREN}\n" +
		"			A.java[+]: {}"
	);

	assertSortedElementsEqual(
		"Unexpected children",
		"A.java [in p [in src [in P]]]",
		pkg.getChildren());

	assertResourceNamesEqual(
		"Unexpected non-java resources",
		"",
		pkg.getNonJavaResources());
}
/*
 * Ensure that creating an included package
 * makes it appear as a child of its package fragment root and not as a non-java resource.
 */
public void testCreateIncludedPackage() throws CoreException {
	setClasspath(new String[] {"/P/src", "p/"});
	IPackageFragmentRoot root = getPackageFragmentRoot("/P/src");

	clearDeltas();
	root.createPackageFragment("p", false, null);

	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" +
		"	src[*]: {CHILDREN}\n" +
		"		p[+]: {}"
	);

	assertSortedElementsEqual(
		"Unexpected children",
		"p [in src [in P]]",
		root.getChildren());

	assertResourceNamesEqual(
		"Unexpected non-java resources",
		"",
		root.getNonJavaResources());
}
/*
 * Ensure that creating a file that corresponds to an included compilation unit
 * makes it appear as a child of its package and not as a non-java resource.
 */
public void testCreateResourceIncludedCompilationUnit() throws CoreException {
	setClasspath(new String[] {"/P/src", "**/A.java"});
	createFolder("/P/src/p");

	clearDeltas();
	createFile(
		"/P/src/p/A.java",
		"package p;\n" +
		"public class A {\n" +
		"}"
	);

	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" +
		"	src[*]: {CHILDREN}\n" +
		"		p[*]: {CHILDREN}\n" +
		"			A.java[+]: {}"
	);

	IPackageFragment pkg = getPackage("/P/src/p");
	assertSortedElementsEqual(
		"Unexpected children",
		"A.java [in p [in src [in P]]]",
		pkg.getChildren());

	assertResourceNamesEqual(
		"Unexpected non-java resources",
		"",
		pkg.getNonJavaResources());
}
/*
 * Ensure that creating a file that corresponds to an included compilation unit
 * in a folder that is not included makes it appear as a child of its package and not as a non-java resource.
 * (regression test for bug 65234 Inclusion filter not working)
 */
public void testCreateResourceIncludedCompilationUnit2() throws CoreException {
	setClasspath(new String[] {"/P/src", "p1/p2/p3/A.java"});
	createFolder("/P/src/p1/p2/p3");

	clearDeltas();
	createFile(
		"/P/src/p1/p2/p3/A.java",
		"package p1.p2.p3;\n" +
		"public class A {\n" +
		"}"
	);

	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" +
		"	src[*]: {CHILDREN | CONTENT}\n" +
		"		p1.p2.p3[*]: {CHILDREN}\n" +
		"			A.java[+]: {}\n" +
		"		ResourceDelta(/P/src/p1)[*]"
	);

	IPackageFragment pkg = getPackage("/P/src/p1/p2/p3");
	assertSortedElementsEqual(
		"Unexpected children",
		"A.java [in p1.p2.p3 [in src [in P]]]",
		pkg.getChildren());

	assertResourceNamesEqual(
		"Unexpected non-java resources",
		"",
		pkg.getNonJavaResources());
}
/*
 * Ensure that creating a folder that corresponds to an included package
 * makes it appear as a child of its package fragment root and not as a non-java resource.
 */
public void testCreateResourceIncludedPackage() throws CoreException {
	setClasspath(new String[] {"/P/src", "p/"});

	clearDeltas();
	createFolder("/P/src/p");

	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" +
		"	src[*]: {CHILDREN}\n" +
		"		p[+]: {}"
	);

	IPackageFragmentRoot root = getPackageFragmentRoot("/P/src");
	assertSortedElementsEqual(
		"Unexpected children",
		"p [in src [in P]]",
		root.getChildren());

	assertResourceNamesEqual(
		"Unexpected non-java resources",
		"",
		root.getNonJavaResources());
}
/*
 * Ensure that creating a folder that is included in a folder that is not included
 * makes it appear as a child of its package fragment root and not as a non-java resource.
 * (regression test for bug 65234 Inclusion filter not working)
 */
public void testCreateResourceIncludedPackage2() throws CoreException {
	setClasspath(new String[] {"/P/src", "p1/p2/p3/"});
	createFolder("/P/src/p1/p2");

	clearDeltas();
	createFolder("/P/src/p1/p2/p3");

	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" +
		"	src[*]: {CHILDREN | CONTENT}\n" +
		"		p1.p2.p3[+]: {}\n" +
		"		ResourceDelta(/P/src/p1)[*]"
	);

	IPackageFragmentRoot root = getPackageFragmentRoot("/P/src");
	assertSortedElementsEqual(
		"Unexpected children",
		"p1.p2.p3 [in src [in P]]",
		root.getChildren());

	assertResourceNamesEqual(
		"Unexpected non-java resources",
		"p1",
		root.getNonJavaResources());
}
/*
 * Ensures that the default package is included if the project is a source folder and one of the
 * compilation unit of the default package is included.
 * (regression test for bug 148278 Default-package classes missing in Package Explorer)
 */
public void testDefaultPackageProjectIsSource() throws CoreException {
	setClasspath(new String[] {"/P", "**/*.java"});
	deleteFolder("/P/src");
	createFile("/P/A.java", "public class A {}");
	IPackageFragmentRoot root = getPackageFragmentRoot("/P");
	assertElementDescendants(
		"Unexpected descendants of root",
		"<project root>\n" +
		"  <default> (...)\n" +
		"    A.java\n" +
		"      class A",
		root);
}
/**
 * Ensure that a type can be resolved if it is included but not its super packages.
 * (regression test for bug 119161 classes in "deep" packages not fully recognized when using tight inclusion filters)
 * @deprecated
 */
public void testIncludeCUOnly01() throws CoreException {
	setClasspath(new String[] {"/P/src", "p1/p2/*.java|q/*.java"});
	addLibraryEntry(getJavaProject("P"), getExternalJCLPathString(), false);
	createFolder("/P/src/p1/p2");
	createFile(
		"/P/src/p1/p2/X.java",
		"package p1.p2;\n" +
		"public class X {\n" +
		"}"
	);
	ICompilationUnit workingCopy = null;
	try {
		ProblemRequestor problemRequestor = new ProblemRequestor();
		workingCopy = getWorkingCopy(
			"/P/src/Y.java",
			"import p1.p2.X;\n" +
			"public class Y extends X {\n" +
			"}",
			null/*primary owner*/,
			problemRequestor
		);
		assertProblems(
			"Unepected problems",
			"----------\n" +
			"----------\n",
			problemRequestor);
	} finally {
		if (workingCopy != null)
			workingCopy.discardWorkingCopy();
	}
}
public void testIncludeCUOnly01_new() throws CoreException {
	setClasspath(new String[] {"/P/src", "p1/p2/*.java|q/*.java"});
	addLibraryEntry(getJavaProject("P"), getExternalJCLPathString(), false);
	createFolder("/P/src/p1/p2");
	createFile(
		"/P/src/p1/p2/X.java",
		"package p1.p2;\n" +
		"public class X {\n" +
		"}"
	);
	ICompilationUnit workingCopy = null;
	try {
		ProblemRequestor problemRequestor = new ProblemRequestor();
		workingCopy = getWorkingCopy(
			"/P/src/Y.java",
			"import p1.p2.X;\n" +
			"public class Y extends X {\n" +
			"}",
			newWorkingCopyOwner(problemRequestor)
		);
		assertProblems(
			"Unepected problems",
			"----------\n" +
			"----------\n",
			problemRequestor);
	} finally {
		if (workingCopy != null)
			workingCopy.discardWorkingCopy();
	}
}

/**
 * Same test as above but checking in modular environment, where search failed to see package in unnamed module
 */
public void testIncludeCUOnly01_new_modular() throws CoreException {
	this.project.setOption(JavaCore.COMPILER_COMPLIANCE, "11");
	testIncludeCUOnly01_new();
}
/**
 * Ensure that a type can be resolved if it is included but not its super packages.
 * (regression test for bug 119161 classes in "deep" packages not fully recognized when using tight inclusion filters)
 * @deprecated
 */
public void testIncludeCUOnly02() throws CoreException {
	setClasspath(new String[] {"/P/src", "p1/p2/p3/*.java|q/*.java"});
	addLibraryEntry(getJavaProject("P"), getExternalJCLPathString(), false);
	createFolder("/P/src/p1/p2/p3");
	createFile(
		"/P/src/p1/p2/p3/X.java",
		"package p1.p2.p3;\n" +
		"public class X {\n" +
		"}"
	);
	ICompilationUnit workingCopy = null;
	try {
		ProblemRequestor problemRequestor = new ProblemRequestor();
		workingCopy = getWorkingCopy(
			"/P/src/Y.java",
			"import p1.p2.p3.X;\n" +
			"public class Y extends X {\n" +
			"}",
			null/*primary owner*/,
			problemRequestor
		);
		assertProblems(
			"Unepected problems",
			"----------\n" +
			"----------\n",
			problemRequestor);
	} finally {
		if (workingCopy != null)
			workingCopy.discardWorkingCopy();
	}
}
public void testIncludeCUOnly02_new() throws CoreException {
	setClasspath(new String[] {"/P/src", "p1/p2/p3/*.java|q/*.java"});
	addLibraryEntry(getJavaProject("P"), getExternalJCLPathString(), false);
	createFolder("/P/src/p1/p2/p3");
	createFile(
		"/P/src/p1/p2/p3/X.java",
		"package p1.p2.p3;\n" +
		"public class X {\n" +
		"}"
	);
	ICompilationUnit workingCopy = null;
	try {
		ProblemRequestor problemRequestor = new ProblemRequestor();
		workingCopy = getWorkingCopy(
			"/P/src/Y.java",
			"import p1.p2.p3.X;\n" +
			"public class Y extends X {\n" +
			"}",
			newWorkingCopyOwner(problemRequestor)
		);
		assertProblems(
			"Unepected problems",
			"----------\n" +
			"----------\n",
			problemRequestor);
	} finally {
		if (workingCopy != null)
			workingCopy.discardWorkingCopy();
	}
}
/*
 * Ensures that a cu that is not included is not on the classpath of the project.
 */
public void testIsOnClasspath1() throws CoreException {
	setClasspath(new String[] {"/P/src", "**/B.java"});
	createFolder("/P/src/p");
	IFile file = createFile(
		"/P/src/p/A.java",
		"package p;\n" +
		"public class A {\n" +
		"}"
	);
	assertTrue("Resource should not be on classpath", !this.project.isOnClasspath(file));

	assertResourceNotOnClasspathEntry(this.project, file);

	ICompilationUnit cu = getCompilationUnit("/P/src/p/A.java");
	assertTrue("CU should not be on classpath", !this.project.isOnClasspath(cu));
}
/*
 * Ensures that a cu that is included is on the classpath of the project.
 */
public void testIsOnClasspath2() throws CoreException {
	setClasspath(new String[] {"/P/src", "**/A.java"});
	createFolder("/P/src/p");
	IFile file = createFile(
		"/P/src/p/A.java",
		"package p;\n" +
		"public class A {\n" +
		"}"
	);
	assertTrue("Resource should be on classpath", this.project.isOnClasspath(file));

	assertResourceOnClasspathEntry(this.project, file, "/P/src");

	ICompilationUnit cu = getCompilationUnit("/P/src/p/A.java");
	assertTrue("CU should be on classpath", this.project.isOnClasspath(cu));
}
/*
 * Ensures that a non-java resource that is not included is not on the classpath of the project.
 */
public void testIsOnClasspath3() throws CoreException {
	setClasspath(new String[] {"/P/src", "**/X.java"});
	createFolder("/P/src/p");
	IFile file = createFile("/P/src/p/readme.txt", "");
	assertTrue("Resource should not be on classpath", !this.project.isOnClasspath(file));
	assertResourceNotOnClasspathEntry(this.project, file);
}
/*
 * Ensures that a non-java resource that is included is on the classpath of the project.
 */
public void testIsOnClasspath4() throws CoreException {
	setClasspath(new String[] {"/P/src", "p/**"});
	createFolder("/P/src/p");
	IFile file = createFile("/P/src/p/readme.txt", "");
	assertTrue("Resource should be on classpath", this.project.isOnClasspath(file));
	assertResourceOnClasspathEntry(this.project, file, "/P/src");
}
/*
 * Ensures that moving a folder that contains an included package reports the correct delta.
 * (regression test for bug 67324 Package Explorer doesn't update included package after moving contents of source folder)
 */
public void testMoveFolderContainingPackage1() throws CoreException {
	setClasspath(new String[] {"/P/src", "p1/p2/"});
	createFolder("/P/src/p1/p2");

	clearDeltas();
	getFolder("/P/src/p1").move(new Path("/P/p1"), false, null);
	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN | CONTENT}\n" +
		"	src[*]: {CHILDREN | CONTENT}\n" +
		"		p1.p2[-]: {}\n" +
		"		ResourceDelta(/P/src/p1)[-]\n" +
		"	ResourceDelta(/P/p1)[+]"
	);
}
/*
 * Ensures that moving a folder that contains an included package reports the correct delta.
 * (regression test for bug 67324 Package Explorer doesn't update included package after moving contents of source folder)
 */
public void testMoveFolderContainingPackage2() throws CoreException {
	setClasspath(new String[] {"/P/src", "p1/p2/"});
	createFolder("/P/p1/p2");

	clearDeltas();
	getFolder("/P//p1").move(new Path("/P/src/p1"), false, null);
	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN | CONTENT}\n" +
		"	src[*]: {CHILDREN | CONTENT}\n" +
		"		p1.p2[+]: {}\n" +
		"		ResourceDelta(/P/src/p1)[+]\n" +
		"	ResourceDelta(/P/p1)[-]"
	);
}
/*
 * Ensures that a non-included nested source folder doesn't appear as a non-java resource of the outer folder.
 *
 */
public void testNestedSourceFolder1() throws CoreException {
	setClasspath(new String[] {"/P/src1", "**/A.java", "/P/src1/src2", ""});
	createFolder("/P/src1/src2");
	IPackageFragmentRoot root1 = getPackageFragmentRoot("/P/src1");
	assertResourceNamesEqual(
		"Unexpected non-java resources for /P/src1",
		"",
		root1.getNonJavaResources());
}
/*
 * Ensures that adding a .java file in a nested source folder reports
 * a delta on the nested source folder and not on the outer one.
 */
public void testNestedSourceFolder2() throws CoreException {
	setClasspath(new String[] {"/P/src1", "**/X.java", "/P/src1/src2", ""});
	createFolder("/P/src1/src2");

	clearDeltas();
	createFile(
		"/P/src1/src2/A.java",
		"public class A {\n" +
		"}"
	);

	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" +
		"	src1/src2[*]: {CHILDREN}\n" +
		"		<default>[*]: {CHILDREN}\n" +
		"			A.java[+]: {}"
	);
}
/*
 * Ensures that adding a .txt file in a nested source folder reports
 * a resource delta on the nested source folder and not on the outer one.
 */
public void testNestedSourceFolder3() throws CoreException {
	setClasspath(new String[] {"/P/src1", "**/X.java", "/P/src1/src2", ""});
	createFolder("/P/src1/src2");

	clearDeltas();
	createFile("/P/src1/src2/readme.txt", "");

	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" +
		"	src1/src2[*]: {CONTENT}\n" +
		"		ResourceDelta(/P/src1/src2/readme.txt)[+]"
	);
}
/*
 * Ensures that adding a folder in a nested source folder reports
 * a delta on the nested source folder and not on the outer one.
 */
public void testNestedSourceFolder4() throws CoreException {
	setClasspath(new String[] {"/P/src1", "**/X.java", "/P/src1/src2", "**/X.java"});
	createFolder("/P/src1/src2");

	clearDeltas();
	createFolder("/P/src1/src2/p");

	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" +
		"	src1/src2[*]: {CHILDREN}\n" +
		"		p[+]: {}"
	);
}
/*
 * Ensures that adding a folder in a outer source folder reports
 * a delta on the outer source folder and not on the nested one.
 */
public void testNestedSourceFolder5() throws CoreException {
	setClasspath(new String[] {"/P/src1", "**/X.java", "/P/src1/src2", ""});
	createFolder("/P/src1/src2");

	clearDeltas();
	createFolder("/P/src1/p");

	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" +
		"	src1[*]: {CHILDREN}\n" +
		"		p[+]: {}"
	);
}
/*
 * Ensures that moving a package from an outer source folder to a nested
 * source folder reports a move delta.
 */
public void testNestedSourceFolder6() throws CoreException {
	setClasspath(new String[] {"/P/src1", "**/X.java", "/P/src1/src2", "**/X.java"});
	createFolder("/P/src1/src2");
	createFolder("/P/src1/p");

	clearDeltas();
	moveFolder("/P/src1/p", "/P/src1/src2/p");

	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" +
		"	src1[*]: {CHILDREN}\n" +
		"		p[-]: {MOVED_TO(p [in src1/src2 [in P]])}\n" +
		"	src1/src2[*]: {CHILDREN}\n" +
		"		p[+]: {MOVED_FROM(p [in src1 [in P]])}"
	);
}
/*
 * Ensure that removing the inclusion on a compilation unit
 * makes it disappears from the children of its package and it is added to the non-java resources.
 */
public void testRemoveInclusionOnCompilationUnit() throws CoreException {
	setClasspath(new String[] {"/P/src", "**/A.java"});
	createFolder("/P/src/p");
	createFile(
		"/P/src/p/A.java",
		"package p;\n" +
		"public class A {\n" +
		"}"
	);

	clearDeltas();
	setClasspath(new String[] {"/P/src", "**/B.java"});

	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
		"	src[*]: {ADDED TO CLASSPATH | REMOVED FROM CLASSPATH}\n" +
		"	ResourceDelta(/P/.classpath)[*]"
	);

	IPackageFragment pkg = getPackage("/P/src/p");
	assertSortedElementsEqual(
		"Unexpected children",
		"",
		pkg.getChildren());

	assertResourceNamesEqual(
		"Unexpected non-java resources",
		"A.java",
		pkg.getNonJavaResources());
}
/*
 * Ensure that removing the inclusion on a package
 * makes it disappears from the children of its package fragment root
 * and it is added to the non-java resources.
 */
public void testRemoveInclusionOnPackage() throws CoreException {
	setClasspath(new String[] {"/P/src", "p"});
	createFolder("/P/src/p");

	clearDeltas();
	setClasspath(new String[] {"/P/src", "q"});

	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
		"	src[*]: {ADDED TO CLASSPATH | REMOVED FROM CLASSPATH}\n" +
		"	ResourceDelta(/P/.classpath)[*]"
	);

	IPackageFragmentRoot root = getPackageFragmentRoot("/P/src");
	assertSortedElementsEqual(
		"Unexpected children",
		"<default> [in src [in P]]",
		root.getChildren());

	assertResourceNamesEqual(
		"Unexpected non-java resources",
		"p",
		root.getNonJavaResources());
}
/*
 * Ensure that renaming an included compilation unit so that it is not included any longer
 * makes it disappears from the children of its package and it is added to the non-java resources.
 */
public void testRenameIncludedCompilationUnit() throws CoreException {
	setClasspath(new String[] {"/P/src", "**/A.java"});
	createFolder("/P/src/p");
	IFile file = createFile(
		"/P/src/p/A.java",
		"package p;\n" +
		"public class A {\n" +
		"}"
	);

	clearDeltas();
	file.move(new Path("/P/src/p/B.java"), false, null);

	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" +
		"	src[*]: {CHILDREN}\n" +
		"		p[*]: {CHILDREN | CONTENT}\n" +
		"			A.java[-]: {}\n" +
		"			ResourceDelta(/P/src/p/B.java)[+]"
	);

	IPackageFragment pkg = getPackage("/P/src/p");
	assertSortedElementsEqual(
		"Unexpected children",
		"",
		pkg.getChildren());

	assertResourceNamesEqual(
		"Unexpected non-java resources",
		"B.java",
		pkg.getNonJavaResources());
}
/*
 * Ensure that renaming an included package so that it is not included any longer
 * makes it disappears from the children of its package fragment root
 * and it is added to the non-java resources.
 */
public void testRenameIncludedPackage1() throws CoreException {
	setClasspath(new String[] {"/P/src", "p/"});
	IPackageFragmentRoot root = getPackageFragmentRoot("/P/src");
	IPackageFragment pkg = root.createPackageFragment("p", false, null);

	clearDeltas();
	pkg.rename("q", false, null);

	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" +
		"	src[*]: {CHILDREN | CONTENT}\n" +
		"		p[-]: {}\n" +
		"		ResourceDelta(/P/src/q)[+]"
	);

	assertSortedElementsEqual(
		"Unexpected children",
		"",
		root.getChildren());

	assertResourceNamesEqual(
		"Unexpected non-java resources",
		"q",
		root.getNonJavaResources());
}
/*
 * Ensure that renaming an included package that has compilation units
 * so that it is not included any longer doesn't throw a JavaModelException.
 * (regression test for bug 67297 Renaming included package folder throws JME)
 */
public void testRenameIncludedPackage2() throws CoreException {
	setClasspath(new String[] {"/P/src", "p/"});
	IPackageFragmentRoot root = getPackageFragmentRoot("/P/src");
	IPackageFragment pkg = root.createPackageFragment("p", false, null);
	createFile(
		"/P/src/p/X.java",
		"package p;\n" +
		"public class X {\n" +
		"}"
	);

	clearDeltas();
	pkg.rename("q", false, null);

	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" +
		"	src[*]: {CHILDREN | CONTENT}\n" +
		"		p[-]: {}\n" +
		"		ResourceDelta(/P/src/q)[+]"
	);

	assertSortedElementsEqual(
		"Unexpected children",
		"",
		root.getChildren());

	assertResourceNamesEqual(
		"Unexpected non-java resources",
		"q",
		root.getNonJavaResources());
}
/*
 * Ensure that renaming a file that corresponds to an included compilation unit so that it is not included any longer
 * makes it disappears from the children of its package and it is added to the non-java resources.
 */
public void testRenameResourceIncludedCompilationUnit() throws CoreException {
	setClasspath(new String[] {"/P/src", "**/A.java"});
	createFolder("/P/src/p");
	IFile file = createFile(
		"/P/src/p/A.java",
		"package p;\n" +
		"public class A {\n" +
		"}"
	);

	clearDeltas();
	file.move(new Path("/P/src/p/B.java"),  false, null);

	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" +
		"	src[*]: {CHILDREN}\n" +
		"		p[*]: {CHILDREN | CONTENT}\n" +
		"			A.java[-]: {}\n" +
		"			ResourceDelta(/P/src/p/B.java)[+]"
	);

	IPackageFragment pkg = getPackage("/P/src/p");
	assertSortedElementsEqual(
		"Unexpected children",
		"",
		pkg.getChildren());

	assertResourceNamesEqual(
		"Unexpected non-java resources",
		"B.java",
		pkg.getNonJavaResources());
}
/*
 * Ensure that renaming a folder that corresponds to an included package
 * so that it is not included any longer
 * makes it disappears afrom the children of its package fragment root
 * and it is added to the non-java resources.
 */
public void testRenameResourceIncludedPackage() throws CoreException {
	setClasspath(new String[] {"/P/src", "p/"});
	IFolder folder = createFolder("/P/src/p");

	clearDeltas();
	folder.move(new Path("/P/src/q"), false, null);

	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" +
		"	src[*]: {CHILDREN | CONTENT}\n" +
		"		p[-]: {}\n" +
		"		ResourceDelta(/P/src/q)[+]"
	);

	IPackageFragmentRoot root = getPackageFragmentRoot("/P/src");
	assertSortedElementsEqual(
		"Unexpected children",
		"",
		root.getChildren());

	assertResourceNamesEqual(
		"Unexpected non-java resources",
		"q",
		root.getNonJavaResources());
}
/*
 * Ensure that a potential match in the output folder is not indexed.
 * (regression test for bug 32041 Multiple output folders fooling Java Model)
 */
public void testSearchPotentialMatchInOutput() throws CoreException {
	try {
		JavaCore.run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				IJavaProject javaProject = createJavaProject("P2", new String[] {}, "bin");
				javaProject.setRawClasspath(createClasspath(new String[] {"/P2", "**/X.java", "/P2/src", ""}, true/*inclusion*/, false/*no exclusion*/), null);
				createFile(
					"/P2/bin/X.java",
					"public class X {\n" +
					"}"
				);
			}
		}, null);

		JavaSearchTests.JavaSearchResultCollector resultCollector = new JavaSearchTests.JavaSearchResultCollector();
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {getJavaProject("P")});
		search(
			"X",
			IJavaSearchConstants.TYPE,
			IJavaSearchConstants.DECLARATIONS,
			scope,
			resultCollector);
		assertEquals("", resultCollector.toString());
	} finally {
		deleteProject("P2");
	}
}
/*
 * Ensure search finds matches in an included compilation unit.
 */
public void testSearchWithIncludedCompilationUnit1() throws CoreException {
	setClasspath(new String[] {"/P/src", "**/A.java"});
	createFolder("/P/src/p");
	createFile(
		"/P/src/p/A.java",
		"package p;\n" +
		"public class A {\n" +
		"}"
	);

	JavaSearchTests.JavaSearchResultCollector resultCollector = new JavaSearchTests.JavaSearchResultCollector();
	search(
		"A",
		IJavaSearchConstants.TYPE,
		IJavaSearchConstants.DECLARATIONS,
		SearchEngine.createJavaSearchScope(new IJavaProject[] {getJavaProject("P")}),
		resultCollector);
	assertEquals(
		"Unexpected matches found",
		"src/p/A.java p.A [A]",
		resultCollector.toString());
}
/*
 * Ensure search doesn't find matches in a compilation unit that was included but that is not any longer.
 */
public void testSearchWithIncludedCompilationUnit2() throws CoreException {
	setClasspath(new String[] {"/P/src", "**/A.java"});
	createFolder("/P/src/p");
	createFile(
		"/P/src/p/A.java",
		"package p;\n" +
		"public class A {\n" +
		"}"
	);

	setClasspath(new String[] {"/P/src", "**/B.java"});
	JavaSearchTests.JavaSearchResultCollector resultCollector = new JavaSearchTests.JavaSearchResultCollector();
	search(
		"A",
		IJavaSearchConstants.TYPE,
		IJavaSearchConstants.DECLARATIONS,
		SearchEngine.createJavaSearchScope(new IJavaProject[] {getJavaProject("P")}),
		resultCollector);
	assertEquals(
		"Unexpected matches found",
		"",
		resultCollector.toString());
}
/*
 * Ensure search finds matches in an included package.
 * (case of setting the classpath)
 */
public void testSearchWithIncludedPackage1() throws CoreException {
	createFolder("/P/src/p");
	createFile(
		"/P/src/p/A.java",
		"package p;\n" +
		"public class A {\n" +
		"}"
	);
	setClasspath(new String[] {"/P/src", "p/"});

	JavaSearchTests.JavaSearchResultCollector resultCollector = new JavaSearchTests.JavaSearchResultCollector();
	search(
		"A",
		IJavaSearchConstants.TYPE,
		IJavaSearchConstants.DECLARATIONS,
		SearchEngine.createJavaSearchScope(new IJavaProject[] {getJavaProject("P")}),
		resultCollector);
	assertEquals(
		"Unexpected matches found",
		"src/p/A.java p.A [A]",
		resultCollector.toString());
}
/*
 * Ensure search finds matches in an included package.
 * (case of opening the project)
 */
public void testSearchWithIncludedPackage2() throws CoreException {
	setClasspath(new String[] {"/P/src", "p/"});
	createFolder("/P/src/p");
	createFile(
		"/P/src/p/A.java",
		"package p;\n" +
		"public class A {\n" +
		"}"
	);
	IProject p = this.project.getProject();
	p.close(null);
	p.open(null);

	JavaSearchTests.JavaSearchResultCollector resultCollector = new JavaSearchTests.JavaSearchResultCollector();
	search(
		"A",
		IJavaSearchConstants.TYPE,
		IJavaSearchConstants.DECLARATIONS,
		SearchEngine.createJavaSearchScope(new IJavaProject[] {getJavaProject("P")}),
		resultCollector);
	assertEquals(
		"Unexpected matches found",
		"src/p/A.java p.A [A]",
		resultCollector.toString());
}
/*
 * Ensure that an included pattern of the form "com/" includes all the subtree
 * (regression test for bug 62608 Include pattern ending with slash should include all subtree)
 */
public void testTrailingSlash() throws CoreException {
	setClasspath(new String[] {"/P/src", "a/"});
	createFolder("/P/src/a/b/c");
	IPackageFragmentRoot root = getPackageFragmentRoot("/P/src");
	assertSortedElementsEqual(
		"Unexpected children of root",
		"a [in src [in P]]\n" +
		"a.b [in src [in P]]\n" +
		"a.b.c [in src [in P]]",
		root.getChildren());
}
}
