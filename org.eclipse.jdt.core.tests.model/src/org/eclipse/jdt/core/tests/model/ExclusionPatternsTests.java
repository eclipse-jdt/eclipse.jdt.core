/*******************************************************************************
 * Copyright (c) 2002 IBM Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.StringTokenizer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;

import junit.framework.Test;

public class ExclusionPatternsTests extends ModifyingResourceTests {
	IJavaProject project;
public ExclusionPatternsTests(String name) {
	super(name);
}
/*
 * Returns a new classpath from the given source folders and their respective exclusion patterns.
 * The given array as the following form:
 * [<source folder>, "<pattern>[|<pattern]*"]*
 * E.g. new String[] {
 *   "src1", "p/A.java",
 *   "src2", "*.txt|com.tests/**"
 * } */
protected IClasspathEntry[] createClasspath(String[] sourceFoldersAndExclusionPatterns) {
	int length = sourceFoldersAndExclusionPatterns.length;
	IClasspathEntry[] classpath = new IClasspathEntry[length/2];
	for (int i = 0; i < length; i+=2) {
		String src = sourceFoldersAndExclusionPatterns[i];
		String patterns = sourceFoldersAndExclusionPatterns[i+1];
		StringTokenizer tokenizer = new StringTokenizer(patterns, "|");
		int patternsCount =  tokenizer.countTokens();
		IPath[] patternPaths = new IPath[patternsCount];
		for (int j = 0; j < patternsCount; j++) {
			patternPaths[j] = new Path(tokenizer.nextToken());
		}
		classpath[i/2] = JavaCore.newSourceEntry(new Path(src), patternPaths); 
	}
	return classpath;
}
/*
 * @see getClasspath for the format of sourceFoldersAndExclusionPatterns */
protected void setClasspath(String[] sourceFoldersAndExclusionPatterns) throws JavaModelException {
	this.project.setRawClasspath(createClasspath(sourceFoldersAndExclusionPatterns), null);
}
protected void setUp() throws Exception {
	super.setUp();
	this.project = this.createJavaProject("P", new String[] {"src"}, "bin");
	startDeltas();
}

public static Test suite() {
	return new Suite(ExclusionPatternsTests.class);
}

protected void tearDown() throws Exception {
	stopDeltas();
	this.deleteProject("P");
	super.tearDown();
}
/*
 * Ensure that adding an exclusion on a compilation unit
 * makes it disappears from the children of its package and it is added to the non-java resources.
 */
public void testAddExclusionOnCompilationUnit() throws CoreException {
	this.createFolder("/P/src/p");
	this.createFile(
		"/P/src/p/A.java",
		"package p;\n" +
		"public class A {\n" +
		"}"
	);
	
	clearDeltas();
	this.setClasspath(new String[] {"/P/src", "A.java"});
	
	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" + 
		"	src[*]: {ADDED TO CLASSPATH | REMOVED FROM CLASSPATH}\n" + 
		"	ResourceDelta(/P/.classpath)[*]"
	);
	
	IPackageFragment pkg = getPackage("/P/src/p");
	assertElementsEqual(
		"Unexpected children",
		"",
		pkg.getChildren());
		
	assertResourcesEqual(
		"Unexpected non-java resources",
		"A.java",
		pkg.getNonJavaResources());
}
/*
 * Ensure that adding an exclusion on a folder directly under a project (and prj=src)
 * makes it appears as a non-java resources.
 * (regression test for bug 29374 Excluded folder on project not returned by Java Model)
 */
public void testAddExclusionOnFolderUnderProject() throws CoreException {
	try {
		IJavaProject project = this.createJavaProject("P1", new String[] {""}, "");
		this.createFolder("/P1/doc");

		clearDeltas();
		project.setRawClasspath(createClasspath(new String[] {"/P1", "doc/"}), null);
	
		assertDeltas(
			"Unexpected deltas",
			"P1[*]: {CHILDREN}\n" + 
			"	[project root][*]: {ADDED TO CLASSPATH | REMOVED FROM CLASSPATH}\n" + 
			"	ResourceDelta(/P1/.classpath)[*]"
		);
	
		IPackageFragmentRoot root = getPackageFragmentRoot("/P1");
		assertElementsEqual(
			"Unexpected children",
			"",
			root.getChildren());
		
		assertResourcesEqual(
			"Unexpected non-java resources of project",
			".classpath\n" +
			".project\n" +
			"doc",
			project.getNonJavaResources());
	} finally {
		this.deleteProject("P1");
	}
}
/*
 * Ensure that adding an exclusion on a package
 * makes it disappears from the children of its package fragment root 
 * and it is added to the non-java resources.
 */
public void testAddExclusionOnPackage() throws CoreException {
	this.createFolder("/P/src/p");
	
	clearDeltas();
	this.setClasspath(new String[] {"/P/src", "p"});
	
	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" + 
		"	src[*]: {ADDED TO CLASSPATH | REMOVED FROM CLASSPATH}\n" + 
		"	ResourceDelta(/P/.classpath)[*]"
	);
	
	IPackageFragmentRoot root = getPackageFragmentRoot("/P/src");
	assertElementsEqual(
		"Unexpected children",
		"",
		root.getChildren());
		
	assertResourcesEqual(
		"Unexpected non-java resources",
		"p",
		root.getNonJavaResources());
}
/*
 * Ensure that adding a file to a folder that is excluded reports the correct delta.
 * (regression test for bug 29621 Wrong Delta When Adding to Filtered Folder)
 */
public void testAddToExcludedFolder() throws CoreException {
	this.createFolder("/P/src/icons");
	
	// exclude folder and its contents
	this.setClasspath(new String[] {"/P/src", "icons/"});
	
	clearDeltas();
	this.createFile("/P/src/icons/my.txt", "");
	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" + 
		"	src[*]: {CONTENT}\n" + 
		"		ResourceDelta(/P/src/icons)[*]"
	);
	
	// exclude folder only (not its contents)
	this.setClasspath(new String[] {"/P/src", "icons"});
	
	clearDeltas();
	this.deleteFile("/P/src/icons/my.txt");
	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" + 
		"	src[*]: {CONTENT}\n" + 
		"		ResourceDelta(/P/src/icons)[*]"
	);
}
/*
 * Ensure that creating an excluded compilation unit 
 * doesn't make it appear as a child of its package but it is a non-java resource. */
public void testCreateExcludedCompilationUnit() throws CoreException {
	this.setClasspath(new String[] {"/P/src", "A.java"});
	this.createFolder("/P/src/p");
	IPackageFragment pkg = getPackage("/P/src/p");

	clearDeltas();
	pkg.createCompilationUnit(
		"A.java",
		"package p;\n" +		"public class A {\n" +		"}",
		false,
		null);
	
	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" + 
		"	src[*]: {CHILDREN}\n" + 
		"		p[*]: {CONTENT}\n" + 
		"			ResourceDelta(/P/src/p/A.java)[+]"
	);
	
	assertElementsEqual(
		"Unexpected children",
		"",
		pkg.getChildren());
		
	assertResourcesEqual(
		"Unexpected non-java resources",
		"A.java",
		pkg.getNonJavaResources());
}
/*
 * Ensure that crearing an excluded package 
 * doesn't make it appear as a child of its package fragment root but it is a non-java resource.
 */
public void testCreateExcludedPackage() throws CoreException {
	this.setClasspath(new String[] {"/P/src", "p"});
	IPackageFragmentRoot root = getPackageFragmentRoot("/P/src");
	
	clearDeltas();
	root.createPackageFragment("p", false, null);
	
	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" + 
		"	src[*]: {CONTENT}\n" + 
		"		ResourceDelta(/P/src/p)[+]"
	);
	
	assertElementsEqual(
		"Unexpected children",
		"",
		root.getChildren());
		
	assertResourcesEqual(
		"Unexpected non-java resources",
		"p",
		root.getNonJavaResources());
}
/*
 * Ensure that creating a file that corresponds to an excluded compilation unit 
 * doesn't make it appear as a child of its package but it is a non-java resource.
 */
public void testCreateResourceExcludedCompilationUnit() throws CoreException {
	this.setClasspath(new String[] {"/P/src", "A.java"});
	this.createFolder("/P/src/p");
	
	clearDeltas();
	this.createFile(
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
	assertElementsEqual(
		"Unexpected children",
		"",
		pkg.getChildren());
		
	assertResourcesEqual(
		"Unexpected non-java resources",
		"A.java",
		pkg.getNonJavaResources());
}
/*
 * Ensure that creating a folder that corresponds to an excluded package 
 * doesn't make it appear as a child of its package fragment root but it is a non-java resource.
 */
public void testCreateResourceExcludedPackage() throws CoreException {
	this.setClasspath(new String[] {"/P/src", "p"});
	
	clearDeltas();
	this.createFolder("/P/src/p");
	
	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" + 
		"	src[*]: {CONTENT}\n" + 
		"		ResourceDelta(/P/src/p)[+]"
	);
	
	IPackageFragmentRoot root = getPackageFragmentRoot("/P/src");
	assertElementsEqual(
		"Unexpected children",
		"",
		root.getChildren());
		
	assertResourcesEqual(
		"Unexpected non-java resources",
		"p",
		root.getNonJavaResources());
}
/*
 * Ensures that a cu that is not excluded is on the classpath of the project.
 */
public void testIsOnClasspath1() throws CoreException {
	this.setClasspath(new String[] {"/P/src", ""});
	this.createFolder("/P/src/p");
	IFile file = this.createFile(
		"/P/src/p/A.java",
		"package p;\n" +
		"public class A {\n" +
		"}"
	);
	assertTrue("Resource should be on classpath", project.isOnClasspath(file));
	
	ICompilationUnit cu = getCompilationUnit("/P/src/p/A.java");
	assertTrue("CU should be on classpath", project.isOnClasspath(cu));
}
/*
 * Ensures that a cu that is excluded is not on the classpath of the project.
 */
public void testIsOnClasspath2() throws CoreException {
	this.setClasspath(new String[] {"/P/src", "A.java"});
	this.createFolder("/P/src/p");
	IFile file = this.createFile(
		"/P/src/p/A.java",
		"package p;\n" +
		"public class A {\n" +
		"}"
	);
	assertTrue("Resource should not be on classpath", !project.isOnClasspath(file));
	
	ICompilationUnit cu = getCompilationUnit("/P/src/p/A.java");
	assertTrue("CU should not be on classpath", !project.isOnClasspath(cu));
}
/*
 * Ensures that a non-java resource that is not excluded is on the classpath of the project.
 */
public void testIsOnClasspath3() throws CoreException {
	this.setClasspath(new String[] {"/P/src", ""});
	this.createFolder("/P/src/p");
	IFile file = this.createFile("/P/src/p/readme.txt", "");
	assertTrue("Resource should be on classpath", project.isOnClasspath(file));
}
/*
 * Ensures that a non-java resource that is excluded is not on the classpath of the project.
 */
public void testIsOnClasspath4() throws CoreException {
	this.setClasspath(new String[] {"/P/src", "p/**"});
	this.createFolder("/P/src/p");
	IFile file = this.createFile("/P/src/p/readme.txt", "");
	assertTrue("Resource should not be on classpath", !project.isOnClasspath(file));
}
/*
 * Ensures that an excluded nested source folder doesn't appear as a non-java resource of the outer folder.
 * (regression test for bug 28115 Ubiquitous resource in the JavaModel)
 * 
 */
public void testNestedSourceFolder1() throws CoreException {
	this.setClasspath(new String[] {"/P/src1", "src2/**", "/P/src1/src2", ""});
	this.createFolder("/P/src1/src2");
	IPackageFragmentRoot root1 = getPackageFragmentRoot("/P/src1");
	assertResourcesEqual(
		"Unexpected non-java resources for /P/src1",
		"",
		root1.getNonJavaResources());
}
/*
 * Ensures that adding a .java file in a nested source folder reports 
 * a delta on the nested source folder and not on the outer one.
 */
public void testNestedSourceFolder2() throws CoreException {
	this.setClasspath(new String[] {"/P/src1", "src2/**", "/P/src1/src2", ""});
	this.createFolder("/P/src1/src2");
	
	this.clearDeltas();
	this.createFile(
		"/P/src1/src2/A.java",
		"public class A {\n" +
		"}"
	);
	
	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" + 
		"	src1/src2[*]: {CHILDREN}\n" + 
		"		[default][*]: {CHILDREN}\n" + 
		"			A.java[+]: {}"
	);
}
/*
 * Ensures that adding a .txt file in a nested source folder reports 
 * a resource delta on the nested source folder and not on the outer one.
 */
public void testNestedSourceFolder3() throws CoreException {
	this.setClasspath(new String[] {"/P/src1", "src2/**", "/P/src1/src2", ""});
	this.createFolder("/P/src1/src2");
	
	this.clearDeltas();
	this.createFile("/P/src1/src2/readme.txt", "");
	
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
	this.setClasspath(new String[] {"/P/src1", "src2/**", "/P/src1/src2", ""});
	this.createFolder("/P/src1/src2");
	
	this.clearDeltas();
	this.createFolder("/P/src1/src2/p");
	
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
	this.setClasspath(new String[] {"/P/src1", "src2/**", "/P/src1/src2", ""});
	this.createFolder("/P/src1/src2");
	
	this.clearDeltas();
	this.createFolder("/P/src1/p");
	
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
	this.setClasspath(new String[] {"/P/src1", "src2/**", "/P/src1/src2", ""});
	this.createFolder("/P/src1/src2");
	this.createFolder("/P/src1/p");
	
	this.clearDeltas();
	this.moveFolder("/P/src1/p", "/P/src1/src2/p");
	
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
	this.setClasspath(new String[] {"/P/src", "A.java"});
	this.createFolder("/P/src/p");
	IPackageFragment pkg = getPackage("/P/src/p");
	ICompilationUnit cu = pkg.createCompilationUnit(
		"A.java",
		"package p;\n" +
		"public class A {\n" +
		"}",
		false,
		null);

	clearDeltas();
	cu.rename("B.java", false, null);
	
	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" + 
		"	src[*]: {CHILDREN}\n" + 
		"		p[*]: {CHILDREN | CONTENT}\n" + 
		"			B.java[+]: {}\n" + 
		"			ResourceDelta(/P/src/p/A.java)[-]"
	);
	
	assertElementsEqual(
		"Unexpected children",
		"B.java",
		pkg.getChildren());
		
	assertResourcesEqual(
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
	this.setClasspath(new String[] {"/P/src", "p"});
	IPackageFragmentRoot root = getPackageFragmentRoot("/P/src");
	IPackageFragment pkg = root.createPackageFragment("p", false, null);
	
	clearDeltas();
	pkg.rename("q", false, null);
	
	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" + 
		"	src[*]: {CHILDREN | CONTENT}\n" + 
		"		q[+]: {}\n" + 
		"		ResourceDelta(/P/src/p)[-]"
	);
	
	assertElementsEqual(
		"Unexpected children",
		"\n" + // default package
		"q",
		root.getChildren());
		
	assertResourcesEqual(
		"Unexpected non-java resources",
		"",
		root.getNonJavaResources());
}
/*
 * Ensure that renaming a file that corresponds to an excluded compilation unit so that it is not excluded any longer
 * makes it appears as a child of its package and it is removed from the non-java resources.
 */
public void testRenameResourceExcludedCompilationUnit() throws CoreException {
	this.setClasspath(new String[] {"/P/src", "A.java"});
	this.createFolder("/P/src/p");
	IFile file = this.createFile(
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
	assertElementsEqual(
		"Unexpected children",
		"B.java",
		pkg.getChildren());
		
	assertResourcesEqual(
		"Unexpected non-java resources",
		"",
		pkg.getNonJavaResources());
}
/*
 * Ensure search doesn't find matches in an excluded compilation unit.
 */
public void testSearchWithExcludedCompilationUnit1() throws CoreException {
	this.setClasspath(new String[] {"/P/src", "A.java"});
	this.createFolder("/P/src/p");
	this.createFile(
		"/P/src/p/A.java",
		"package p;\n" +
		"public class A {\n" +
		"}"
	);
	
	JavaSearchTests.JavaSearchResultCollector resultCollector = new JavaSearchTests.JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
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
	this.setClasspath(new String[] {"/P/src", "A.java"});
	this.createFolder("/P/src/p");
	this.createFile(
		"/P/src/p/A.java",
		"package p;\n" +
		"public class A {\n" +
		"}"
	);
	
	this.setClasspath(new String[] {"/P/src", ""});
	JavaSearchTests.JavaSearchResultCollector resultCollector = new JavaSearchTests.JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
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
 * Ensure that renaming a folder that corresponds to an excluded package 
 * so that it is not excluded any longer
 * makes it appears as a child of its package fragment root 
 * and it is removed from the non-java resources.
 */
public void testRenameResourceExcludedPackage() throws CoreException {
	this.setClasspath(new String[] {"/P/src", "p"});
	IFolder folder = this.createFolder("/P/src/p");
	
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
	assertElementsEqual(
		"Unexpected children",
		"\n" + // default package
		"q",
		root.getChildren());
		
	assertResourcesEqual(
		"Unexpected non-java resources",
		"",
		root.getNonJavaResources());
}
/*
 * Ensure that removing the exclusion on a compilation unit
 * makes it appears as a child of its package and it is removed from the non-java resources.
 */
public void testRemoveExclusionOnCompilationUnit() throws CoreException {
	this.setClasspath(new String[] {"/P/src", "A.java"});
	this.createFolder("/P/src/p");
	this.createFile(
		"/P/src/p/A.java",
		"package p;\n" +
		"public class A {\n" +
		"}"
	);
	
	clearDeltas();
	this.setClasspath(new String[] {"/P/src", ""});
	
	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" + 
		"	src[*]: {ADDED TO CLASSPATH | REMOVED FROM CLASSPATH}\n" + 
		"	ResourceDelta(/P/.classpath)[*]"
	);
	
	IPackageFragment pkg = getPackage("/P/src/p");
	assertElementsEqual(
		"Unexpected children",
		"A.java",
		pkg.getChildren());
		
	assertResourcesEqual(
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
	this.setClasspath(new String[] {"/P/src", "p"});
	this.createFolder("/P/src/p");
	
	clearDeltas();
	this.setClasspath(new String[] {"/P/src", ""});
	
	assertDeltas(
		"Unexpected deltas",
		"P[*]: {CHILDREN}\n" + 
		"	src[*]: {ADDED TO CLASSPATH | REMOVED FROM CLASSPATH}\n" + 
		"	ResourceDelta(/P/.classpath)[*]"
	);
	
	IPackageFragmentRoot root = getPackageFragmentRoot("/P/src");
	assertElementsEqual(
		"Unexpected children",
		"\n" + // default package		"p",
		root.getChildren());
		
	assertResourcesEqual(
		"Unexpected non-java resources",
		"",
		root.getNonJavaResources());
}
}
