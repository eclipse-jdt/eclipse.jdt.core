/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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

import junit.framework.Test;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;

public class ExclusionPatternsTests extends ModifyingResourceTests {
	IJavaProject project;
public ExclusionPatternsTests(String name) {
	super(name);
}
protected void setClasspath(String[] sourceFoldersAndExclusionPatterns) throws JavaModelException {
	this.project.setRawClasspath(createClasspath(sourceFoldersAndExclusionPatterns, false/*no inclusion*/, true/*exclusion*/), null);
}
@Override
protected void setUp() throws Exception {
	super.setUp();
	this.project = createJavaProject("P", new String[] {"src"}, "bin");
	startDeltas();
}

// Use this static initializer to specify subset for tests
// All specified tests which do not belong to the class are skipped...
static {
//		TESTS_NAMES = new String[] { "testCreateExcludedPackage2" };
//		TESTS_NUMBERS = new int[] { 2, 12 };
//		TESTS_RANGE = new int[] { 16, -1 };
}
public static Test suite() {
	return buildModelTestSuite(ExclusionPatternsTests.class);
}

@Override
protected void tearDown() throws Exception {
	stopDeltas();
	deleteProject("P");
	super.tearDown();
}
/*
 * Ensure that adding an exclusion on a compilation unit
 * makes it disappear from the children of its package and it is added to the non-java resources.
 */
public void testAddExclusionOnCompilationUnit() throws CoreException {
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
		"",
		pkg.getChildren());

	assertResourceNamesEqual(
		"Unexpected non-java resources",
		"A.java",
		pkg.getNonJavaResources());
}
/*
 * Ensure that adding an exclusion on a folder directly under a project (and prj=src)
 * makes it appear as a non-java resources.
 * (regression test for bug 29374 Excluded folder on project not returned by Java Model)
 */
public void testAddExclusionOnFolderUnderProject() throws CoreException {
	try {
		IJavaProject javaProject = createJavaProject("P1", new String[] {""}, "");
		createFolder("/P1/doc");

		clearDeltas();
		javaProject.setRawClasspath(createClasspath(new String[] {"/P1", "doc/"}, false/*no inclusion*/, true/*exclusion*/), null);

		assertDeltas(
			"Unexpected deltas",
			"P1[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
			"	<project root>[*]: {ADDED TO CLASSPATH | REMOVED FROM CLASSPATH}\n" +
			"	ResourceDelta(/P1/.classpath)[*]"
		);

		IPackageFragmentRoot root = getPackageFragmentRoot("/P1");
		assertSortedElementsEqual(
			"Unexpected children",
			"<default> [in <project root> [in P1]]",
			root.getChildren());

		assertResourceNamesEqual(
			"Unexpected non-java resources of project",
			".classpath\n" +
			".project\n" +
			".settings\n" +
			"doc",
			javaProject.getNonJavaResources());
	} finally {
		deleteProject("P1");
	}
}
/*
 * Ensure that adding an exclusion on a package
 * makes it disappear from the children of its package fragment root
 * and it is added to the non-java resources.
 */
public void testAddExclusionOnPackage() throws CoreException {
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
		"<default> [in src [in P]]",
		root.getChildren());

	assertResourceNamesEqual(
		"Unexpected non-java resources",
		"p",
		root.getNonJavaResources());
}
/*
 * Ensure that adding an exclusion on a primary working copy
 * makes it disappear from the children of its package and it is added to the non-java resources.
 */
public void testAddExclusionOnPrimaryWorkingCopy() throws CoreException {
	createFolder("/P/src/p");
	createFile(
		"/P/src/p/A.java",
		"package p;\n" +
		"public class A {\n" +
		"}"
	);

	ICompilationUnit workingCopy = null;
	try {
		workingCopy = getCompilationUnit("/P/src/p/A.java");
		workingCopy.becomeWorkingCopy(null);

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
			"",
			pkg.getChildren());

		assertResourceNamesEqual(
			"Unexpected non-java resources",
			"A.java",
			pkg.getNonJavaResources());
	} finally {
		if (workingCopy != null) {
			workingCopy.discardWorkingCopy();
		}
	}
}
/*
 * Ensure that adding a file to a folder that is excluded reports the correct delta.
 * (regression test for bug 29621 Wrong Delta When Adding to Filtered Folder)
 */
public void testAddToExcludedFolder() throws CoreException {
	createFolder("/P/src/icons");

	// exclude folder and its contents
	setClasspath(new String[] {"/P/src", "icons/"});

	clearDeltas();
	createFile("/P/src/icons/my.txt", "");
	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" +
		"	src[*]: {CONTENT}\n" +
		"		ResourceDelta(/P/src/icons)[*]"
	);

	clearDeltas();
	deleteFile("/P/src/icons/my.txt");
	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" +
		"	src[*]: {CONTENT}\n" +
		"		ResourceDelta(/P/src/icons)[*]"
	);
}
/*
 * Ensure that creating an excluded compilation unit
 * doesn't make it appear as a child of its package but it is a non-java resource.
 */
public void testCreateExcludedCompilationUnit() throws CoreException {
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
		"		p[*]: {CONTENT}\n" +
		"			ResourceDelta(/P/src/p/A.java)[+]"
	);

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
 * Ensure that crearing an excluded package
 * doesn't make it appear as a child of its package fragment root but it is a non-java resource.
 */
public void testCreateExcludedPackage() throws CoreException {
	setClasspath(new String[] {"/P/src", "p/"});
	IPackageFragmentRoot root = getPackageFragmentRoot("/P/src");

	clearDeltas();
	root.createPackageFragment("p", false, null);

	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" +
		"	src[*]: {CONTENT}\n" +
		"		ResourceDelta(/P/src/p)[+]"
	);

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
 * Ensure that creating an excluded package doesn't make it appear as a child of its package fragment root but it is a non-java resource.
 * (regression test for bug 65637 [model] Excluded package still in Java model)
 */
public void testCreateExcludedPackage2() throws CoreException {
	setClasspath(new String[] {"/P/src", "org/*|org/eclipse/*"});

	// Trigger population of cache to check if it is properly invalidated by the delta processor.
	// See http://bugs.eclipse.org/500714
	getPackageFragmentRoot("/P/src").getChildren();

	clearDeltas();
	createFolder("/P/src/org/eclipse/mypack");

	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" +
		"	src[*]: {CONTENT}\n" +
		"		ResourceDelta(/P/src/org)[+]"
	);

	IPackageFragmentRoot root = getPackageFragmentRoot("/P/src");
	assertSortedElementsEqual(
		"Unexpected children",
		"<default> [in src [in P]]\n" +
		"org.eclipse.mypack [in src [in P]]",
		root.getChildren());

	assertResourceNamesEqual(
		"Unexpected non-java resources",
		"org",
		root.getNonJavaResources());
}
/*
 * Ensure that creating a folder that represents an excluded and included package
 * have the correct delta, Java elements and non-Java resources.
 * (regression test for 67789 Java element delta from refresh contains excluded package)
 */
public void testCreateExcludedAndIncludedPackages() throws CoreException {
	setClasspath(new String[] {"/P/src", "p1/p2/"});
	IPackageFragmentRoot root = getPackageFragmentRoot("/P/src");

	clearDeltas();
	createFolder("/P/src/p1/p2");

	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" +
		"	src[*]: {CHILDREN}\n" +
		"		p1[+]: {}"
	);

	assertSortedElementsEqual(
		"Unexpected children",
		"<default> [in src [in P]]\n" +
		"p1 [in src [in P]]",
		root.getChildren());

	assertResourceNamesEqual(
		"Unexpected non-java resources",
		"p2",
		root.getPackageFragment("p1").getNonJavaResources());
}
/*
 * Ensure that creating a file that corresponds to an excluded compilation unit
 * doesn't make it appear as a child of its package but it is a non-java resource.
 */
public void testCreateResourceExcludedCompilationUnit() throws CoreException {
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
		"		p[*]: {CONTENT}\n" +
		"			ResourceDelta(/P/src/p/A.java)[+]"
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
 * Ensure that creating a folder that corresponds to an excluded package
 * doesn't make it appear as a child of its package fragment root but it is a non-java resource.
 */
public void testCreateResourceExcludedPackage() throws CoreException {
	setClasspath(new String[] {"/P/src", "p/"});

	clearDeltas();
	createFolder("/P/src/p");

	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" +
		"	src[*]: {CONTENT}\n" +
		"		ResourceDelta(/P/src/p)[+]"
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
 * Ensures that a cu that is not excluded is on the classpath of the project.
 */
public void testIsOnClasspath1() throws CoreException {
	setClasspath(new String[] {"/P/src", ""});
	createFolder("/P/src/p");
	IFile file = createFile(
		"/P/src/p/A.java",
		"package p;\n" +
		"public class A {\n" +
		"}"
	);
	assertTrue("Resource should be on classpath", this.project.isOnClasspath(file));

	ICompilationUnit cu = getCompilationUnit("/P/src/p/A.java");
	assertTrue("CU should be on classpath", this.project.isOnClasspath(cu));

	assertResourceOnClasspathEntry(this.project, file, "/P/src");
}
/*
 * Ensures that a cu that is excluded is not on the classpath of the project.
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
	assertTrue("Resource should not be on classpath", !this.project.isOnClasspath(file));

	assertResourceNotOnClasspathEntry(this.project, file);

	ICompilationUnit cu = getCompilationUnit("/P/src/p/A.java");
	assertTrue("CU should not be on classpath", !this.project.isOnClasspath(cu));
}
/*
 * Ensures that a non-java resource that is not excluded is on the classpath of the project.
 */
public void testIsOnClasspath3() throws CoreException {
	setClasspath(new String[] {"/P/src", ""});
	createFolder("/P/src/p");
	IFile file = createFile("/P/src/p/readme.txt", "");
	assertTrue("Resource should be on classpath", this.project.isOnClasspath(file));

	assertResourceOnClasspathEntry(this.project, file, "/P/src");
}
/*
 * Ensures that a non-java resource that is excluded is not on the classpath of the project.
 */
public void testIsOnClasspath4() throws CoreException {
	setClasspath(new String[] {"/P/src", "p/**"});
	createFolder("/P/src/p");
	IFile file = createFile("/P/src/p/readme.txt", "");
	assertTrue("Resource should not be on classpath", !this.project.isOnClasspath(file));

	assertResourceNotOnClasspathEntry(this.project, file);
}
/*
 * Ensures that an excluded nested source folder doesn't appear as a non-java resource of the outer folder.
 * (regression test for bug 28115 Ubiquitous resource in the JavaModel)
 */
public void testNestedSourceFolder1() throws CoreException {
	setClasspath(new String[] {"/P/src1", "src2/**", "/P/src1/src2", ""});
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
	setClasspath(new String[] {"/P/src1", "src2/**", "/P/src1/src2", ""});
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
	setClasspath(new String[] {"/P/src1", "src2/**", "/P/src1/src2", ""});
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
	setClasspath(new String[] {"/P/src1", "src2/**", "/P/src1/src2", ""});
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
	setClasspath(new String[] {"/P/src1", "src2/**", "/P/src1/src2", ""});
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
	setClasspath(new String[] {"/P/src1", "src2/**", "/P/src1/src2", ""});
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
 * Ensure that renaming an excluded compilation unit so that it is not excluded any longer
 * makes it appears as a child of its package and it is removed from the non-java resources.
 */
public void testRenameExcludedCompilationUnit() throws CoreException {
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
		"			B.java[+]: {}\n" +
		"			ResourceDelta(/P/src/p/A.java)[-]"
	);

	IPackageFragment pkg = getPackage("/P/src/p");
	assertSortedElementsEqual(
		"Unexpected children",
		"B.java [in p [in src [in P]]]",
		pkg.getChildren());

	assertResourceNamesEqual(
		"Unexpected non-java resources",
		"",
		pkg.getNonJavaResources());
}
/*
 * Ensure that renaming an excluded package so that it is not excluded any longer
 * makes it appears as a child of its package fragment root
 * and it is removed from the non-java resources.
 */
public void testRenameExcludedPackage() throws CoreException {
	setClasspath(new String[] {"/P/src", "p/"});
	IPackageFragmentRoot root = getPackageFragmentRoot("/P/src");
	IPackageFragment pkg = root.createPackageFragment("p", false, null);

	clearDeltas();
	pkg.getResource().move(new Path("/P/src/q"), false, null);

	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" +
		"	src[*]: {CHILDREN | CONTENT}\n" +
		"		q[+]: {}\n" +
		"		ResourceDelta(/P/src/p)[-]"
	);

	assertSortedElementsEqual(
		"Unexpected children",
		"<default> [in src [in P]]\n" +
		"q [in src [in P]]",
		root.getChildren());

	assertResourceNamesEqual(
		"Unexpected non-java resources",
		"",
		root.getNonJavaResources());
}
/*
 * Ensure that renaming a file that corresponds to an excluded compilation unit so that it is not excluded any longer
 * makes it appears as a child of its package and it is removed from the non-java resources.
 */
public void testRenameResourceExcludedCompilationUnit() throws CoreException {
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
		"			B.java[+]: {}\n" +
		"			ResourceDelta(/P/src/p/A.java)[-]"
	);

	IPackageFragment pkg = getPackage("/P/src/p");
	assertSortedElementsEqual(
		"Unexpected children",
		"B.java [in p [in src [in P]]]",
		pkg.getChildren());

	assertResourceNamesEqual(
		"Unexpected non-java resources",
		"",
		pkg.getNonJavaResources());
}
/*
 * Ensure search doesn't find matches in an excluded compilation unit.
 */
public void testSearchWithExcludedCompilationUnit1() throws CoreException {
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
		"",
		resultCollector.toString());
}
/*
 * Ensure search find matches in a compilation unit that was excluded but that is not any longer.
 */
public void testSearchWithExcludedCompilationUnit2() throws CoreException {
	setClasspath(new String[] {"/P/src", "A.java"});
	createFolder("/P/src/p");
	createFile(
		"/P/src/p/A.java",
		"package p;\n" +
		"public class A {\n" +
		"}"
	);

	setClasspath(new String[] {"/P/src", ""});
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
 * Ensure that removing a folder that represents an excluded and included package
 * have the correct delta, Java elements and non-Java resources.
 * (regression test for 67789 Java element delta from refresh contains excluded package)
 */
public void testRemoveExcludedAndIncludedPackages() throws CoreException {
	setClasspath(new String[] {"/P/src", "p1/p2/"});
	IPackageFragmentRoot root = getPackageFragmentRoot("/P/src");
	createFolder("/P/src/p1/p2");

	clearDeltas();
	deleteFolder("/P/src/p1");

	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" +
		"	src[*]: {CHILDREN}\n" +
		"		p1[-]: {}"
	);

	assertSortedElementsEqual(
		"Unexpected children",
		"<default> [in src [in P]]",
		root.getChildren());

	assertResourceNamesEqual(
		"Unexpected non-java resources",
		"",
		root.getNonJavaResources());
}

/*
 * Ensure that renaming a folder that corresponds to an excluded package
 * so that it is not excluded any longer
 * makes it appears as a child of its package fragment root
 * and it is removed from the non-java resources.
 */
public void testRenameResourceExcludedPackage() throws CoreException {
	setClasspath(new String[] {"/P/src", "p/"});
	IFolder folder = createFolder("/P/src/p");

	clearDeltas();
	folder.move(new Path("/P/src/q"), false, null);

	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" +
		"	src[*]: {CHILDREN | CONTENT}\n" +
		"		q[+]: {}\n" +
		"		ResourceDelta(/P/src/p)[-]"
	);

	IPackageFragmentRoot root = getPackageFragmentRoot("/P/src");
	assertSortedElementsEqual(
		"Unexpected children",
		"<default> [in src [in P]]\n" +
		"q [in src [in P]]",
		root.getChildren());

	assertResourceNamesEqual(
		"Unexpected non-java resources",
		"",
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
				javaProject.setRawClasspath(createClasspath(new String[] {"/P2", "src/", "/P2/src", ""}, false/*no inclusion*/, true/*exclusion*/), null);
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
 * Ensure that removing the exclusion on a compilation unit
 * makes it appears as a child of its package and it is removed from the non-java resources.
 */
public void testRemoveExclusionOnCompilationUnit() throws CoreException {
	setClasspath(new String[] {"/P/src", "A.java"});
	createFolder("/P/src/p");
	createFile(
		"/P/src/p/A.java",
		"package p;\n" +
		"public class A {\n" +
		"}"
	);

	clearDeltas();
	setClasspath(new String[] {"/P/src", ""});

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
 * Ensure that removing the exclusion on a package
 * makes it appears as a child of its package fragment root
 * and it is removed from the non-java resources.
 */
public void testRemoveExclusionOnPackage() throws CoreException {
	setClasspath(new String[] {"/P/src", "p"});
	createFolder("/P/src/p");

	clearDeltas();
	setClasspath(new String[] {"/P/src", ""});

	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
		"	src[*]: {ADDED TO CLASSPATH | REMOVED FROM CLASSPATH}\n" +
		"	ResourceDelta(/P/.classpath)[*]"
	);

	IPackageFragmentRoot root = getPackageFragmentRoot("/P/src");
	assertSortedElementsEqual(
		"Unexpected children",
		"<default> [in src [in P]]\n" +
		"p [in src [in P]]",
		root.getChildren());

	assertResourceNamesEqual(
		"Unexpected non-java resources",
		"",
		root.getNonJavaResources());
}
}
