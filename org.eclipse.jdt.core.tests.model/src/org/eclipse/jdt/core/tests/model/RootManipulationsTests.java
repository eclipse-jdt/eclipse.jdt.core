/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.core.JavaElement;

import junit.framework.Test;

public class RootManipulationsTests extends ModifyingResourceTests {
public RootManipulationsTests(String name) {
	super(name);
}
protected void assertJavaProject(String expected, IJavaProject project) throws CoreException {
	StringBuffer buffer = new StringBuffer();
	populate(buffer, project, 0);

	String actual = buffer.toString();
	if (!expected.equals(actual)) {
	 	System.out.println(Util.displayString(actual, 3));
	}
	assertEquals(expected, actual);
}
protected void copy(IPackageFragmentRoot root, IPath destination) throws JavaModelException {
	copy(root, destination, null);
}
protected void copy(IPackageFragmentRoot root, IPath destination, IClasspathEntry sibling) throws JavaModelException {
	root.copy(
		destination,
		IResource.NONE,
		IPackageFragmentRoot.DESTINATION_PROJECT_CLASSPATH,
		sibling,
		null);
}
protected void delete(IPackageFragmentRoot root) throws JavaModelException {
	// ensure indexing is not interferring with the deletion
	waitUntilIndexesReady();

	root.delete(
		IResource.NONE,
		IPackageFragmentRoot.ORIGINATING_PROJECT_CLASSPATH
			| IPackageFragmentRoot.OTHER_REFERRING_PROJECTS_CLASSPATH,
		null);
}
protected void move(IPackageFragmentRoot root, IPath destination) throws JavaModelException {
	move(root, destination, null);
}
protected void move(IPackageFragmentRoot root, IPath destination, IClasspathEntry sibling) throws JavaModelException {
	// ensure indexing is not interferring with the move
	waitUntilIndexesReady();

	root.move(
		destination,
		IResource.NONE,
		IPackageFragmentRoot.DESTINATION_PROJECT_CLASSPATH
			| IPackageFragmentRoot.ORIGINATING_PROJECT_CLASSPATH
			| IPackageFragmentRoot.OTHER_REFERRING_PROJECTS_CLASSPATH,
		sibling,
		null);
}
protected void populate(StringBuffer buffer, IJavaElement element, int indent) throws CoreException {
	if (!(element instanceof IParent) || !(element instanceof IOpenable)) return;

	if (buffer.length() != 0) {
		buffer.append("\n");
	}
	for (int i = 0; i < indent; i++) buffer.append("\t");
	buffer.append(((JavaElement)element).toDebugString());

	IParent parent = (IParent)element;
	IJavaElement[] children = null;
	try {
		children = parent.getChildren();
	} catch (JavaModelException e) {
	}
	if (children != null) {
		for (int i = 0, length = children.length; i < length; i++) {
			populate(buffer, children[i], indent+1);
		}
	}

	Object[] nonJavaResources = null;
	try {
		if (element instanceof IJavaProject) {
			nonJavaResources = ((IJavaProject)element).getNonJavaResources();
		} else if (element instanceof IPackageFragmentRoot) {
			nonJavaResources = ((IPackageFragmentRoot)element).getNonJavaResources();
		} else if (element instanceof IPackageFragment) {
			nonJavaResources = ((IPackageFragment)element).getNonJavaResources();
		}
	} catch (JavaModelException e) {
	}
	if (nonJavaResources != null) {
		for (int i = 0, length = nonJavaResources.length; i < length; i++) {
			populate(buffer, nonJavaResources[i], indent+1);
		}
	}
}
protected void populate(StringBuffer buffer, Object nonJavaResource, int indent) {
	if (buffer.length() != 0) {
		buffer.append("\n");
	}
	for (int i = 0; i < indent; i++) buffer.append("\t");
	buffer.append(nonJavaResource);
	/*
	if (nonJavaResource instanceof IContainer) {
		IResource[] members = ((IContainer)nonJavaResource).members();
		for (int i = 0, length = members.length; i < length; i++) {
			populate(buffer, members[i], indent+1);
		}
	}*/
}
public static Test suite() {
	return buildModelTestSuite(RootManipulationsTests.class);
}
/*
 * Ensure that a simple copy of a source root to another project triggers the right delta
 * and that the model is up-to-date.
 */
public void testCopySourceFolder1() throws CoreException {
	try {
		this.createJavaProject("P1", new String[] {"src"}, "bin");
		this.createJavaProject("P2", new String[] {}, "bin");
		this.createFolder("/P1/src/p");
		this.createFile(
			"/P1/src/p/X.java",
			"package p;\n" +
			"public class X {\n" +
			"}"
		);
		IPackageFragmentRoot root = this.getPackageFragmentRoot("/P1/src");
		startDeltas();
		this.copy(root, new Path("/P2/src"));
		assertDeltas(
			"Unexpected delta",
			"P2[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
			"	src[+]: {}\n" +
			"	ResourceDelta(/P2/.classpath)[*]"
		);
		ICompilationUnit cu = this.getCompilationUnit("/P2/src/p/X.java");
		assertTrue("Destination cu should exist", cu.exists());
	} finally {
		stopDeltas();
		this.deleteProject("P1");
		this.deleteProject("P2");
	}
}
/*
 * Ensure that coping and renaming a source root to another project triggers the right delta
 * and that the model is up-to-date.
 */
public void testCopySourceFolder2() throws CoreException {
	try {
		this.createJavaProject("P1", new String[] {"src"}, "bin");
		this.createJavaProject("P2", new String[] {}, "bin");
		this.createFolder("/P1/src/p");
		this.createFile(
			"/P1/src/p/X.java",
			"package p;\n" +
			"public class X {\n" +
			"}"
		);
		IPackageFragmentRoot root = this.getPackageFragmentRoot("/P1/src");
		startDeltas();
		this.copy(root, new Path("/P2/src2"));
		assertDeltas(
			"Unexpected delta",
			"P2[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
			"	src2[+]: {}\n" +
			"	ResourceDelta(/P2/.classpath)[*]"
		);
		ICompilationUnit cu = this.getCompilationUnit("/P2/src2/p/X.java");
		assertTrue("Destination cu should exist", cu.exists());
	} finally {
		stopDeltas();
		this.deleteProject("P1");
		this.deleteProject("P2");
	}
}
/*
 * Ensure that coping a source root to another project triggers the right delta
 * and doesn't copy a nested source folder.
 */
public void testCopySourceFolder3() throws CoreException {
	try {
		IJavaProject p1 = this.createJavaProject("P1", new String[] {}, "bin");
		p1.setRawClasspath(createClasspath(new String[] {"/P1/src1", "src2/**", "/P1/src1/src2", ""}, false/*no inclusion*/, true/*exclusion*/), null);
		this.createJavaProject("P2", new String[] {}, "bin");
		this.createFolder("/P1/src1/p");
		this.createFile(
			"/P1/src1/p/X.java",
			"package p;\n" +
			"public class X {\n" +
			"}"
		);
		this.createFolder("/P1/src1/src2/q");
		this.createFile(
			"/P1/src1/src2/q/Y.java",
			"package q;\n" +
			"public class Y {\n" +
			"}"
		);
		IPackageFragmentRoot root = this.getPackageFragmentRoot("/P1/src1");
		startDeltas();
		this.copy(root, new Path("/P2/src1"));
		assertDeltas(
			"Unexpected delta",
			"P2[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
			"	src1[+]: {}\n" +
			"	ResourceDelta(/P2/.classpath)[*]"
		);
		ICompilationUnit cu = this.getCompilationUnit("/P2/src1/p/X.java");
		assertTrue("Destination cu should exist", cu.exists());
		cu = this.getCompilationUnit("/P2/src1/src2/q/Y.java");
		assertTrue("Nested cu should not exist", !cu.exists());
	} finally {
		stopDeltas();
		this.deleteProject("P1");
		this.deleteProject("P2");
	}
}
/*
 * Ensure that copying a source root to another project using a sibling classpath entry triggers the right delta
 * and that the model is up-to-date.
 */
public void testCopySourceFolder4() throws CoreException {
	try {
		this.createJavaProject("P1", new String[] {"src"}, "bin");
		IJavaProject p2 = this.createJavaProject("P2", new String[] {"src1", "src2"}, "bin");
		IPackageFragmentRoot root = this.getPackageFragmentRoot("/P1/src");

		// insert first
		IClasspathEntry sibling = JavaCore.newSourceEntry(new Path("/P2/src1"));
		startDeltas();
		this.copy(root, new Path("/P2/src"), sibling);
		assertDeltas(
			"Unexpected delta (1)",
			"P2[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
			"	src[+]: {}\n" +
			"	src1[*]: {REORDERED}\n" +
			"	src2[*]: {REORDERED}\n" +
			"	ResourceDelta(/P2/.classpath)[*]"
		);
		assertElementsEqual(
			"Unexpected roots of P2 after insertion first",
			"src [in P2]\n" +
			"src1 [in P2]\n" +
			"src2 [in P2]",
			p2.getPackageFragmentRoots());

		// insert in the middle
		sibling = JavaCore.newSourceEntry(new Path("/P2/src2"));
		startDeltas();
		this.copy(root, new Path("/P2/src3"), sibling);
		assertDeltas(
			"Unexpected delta (2)",
			"P2[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
			"	src2[*]: {REORDERED}\n" +
			"	src3[+]: {}\n" +
			"	ResourceDelta(/P2/.classpath)[*]"
		);
		assertElementsEqual(
			"Unexpected roots of P2 after insertion in the middle",
			"src [in P2]\n" +
			"src1 [in P2]\n" +
			"src3 [in P2]\n" +
			"src2 [in P2]",
			p2.getPackageFragmentRoots());

		// insert last
		startDeltas();
		this.copy(root, new Path("/P2/src4"), null);
		assertDeltas(
			"Unexpected delta (3)",
			"P2[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
			"	src4[+]: {}\n" +
			"	ResourceDelta(/P2/.classpath)[*]"
		);
		assertElementsEqual(
			"Unexpected roots of P2 after insertion last",
			"src [in P2]\n" +
			"src1 [in P2]\n" +
			"src3 [in P2]\n" +
			"src2 [in P2]\n" +
			"src4 [in P2]",
			p2.getPackageFragmentRoots());
	} finally {
		stopDeltas();
		this.deleteProject("P1");
		this.deleteProject("P2");
	}
}
/*
 * Ensure that coping a source root to another project with an existing source root in
 * REPLACE mode triggers the right delta and that the model is up-to-date.
 * (regression test bug 30511 IPackageFragmentRoot:move ignores FORCE flag)
 */
public void testCopySourceFolder5() throws CoreException {
	try {
		this.createJavaProject("P1", new String[] {"src"}, "bin");
		IJavaProject p2 = this.createJavaProject("P2", new String[] {"src"}, "bin");
		this.createFolder("/P1/src/p");
		this.createFile(
			"/P1/src/p/X.java",
			"package p;\n" +
			"public class X {\n" +
			"}"
		);

		IPackageFragmentRoot root = this.getPackageFragmentRoot("/P1/src");
		startDeltas();
		root.copy(new Path("/P2/src"), IResource.NONE, IPackageFragmentRoot.REPLACE, null, null);
		assertDeltas(
			"Unexpected delta",
			"P2[*]: {CHILDREN}\n" +
			"	src[*]: {CHILDREN}\n" +
			"		p[+]: {}"
		);
		assertJavaProject(
			"P2\n" +
			"	src\n" +
			"		<default>\n" +
			"		p\n" +
			"			X.java\n" +
			"	L/P2/.classpath\n" +
			"	L/P2/.project",
			p2);
	} finally {
		stopDeltas();
		this.deleteProject("P1");
		this.deleteProject("P2");
	}
}
/*
 * Ensure that coping and renaming a source root to same project in
 * REPLACE mode triggers the right delta and that the model is up-to-date.
 * (regression test bug 30857 IPackageFragmentRoot: copy removes source folders from classpath)
 */
public void testCopySourceFolder6() throws CoreException {
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {"src"}, "bin");
		this.createFolder("/P/src/p");
		this.createFile(
			"/P/src/p/X.java",
			"package p;\n" +
			"public class X {\n" +
			"}"
		);

		IPackageFragmentRoot root = this.getPackageFragmentRoot("/P/src");
		startDeltas();
		root.copy(
			new Path("/P/src1"),
			IResource.KEEP_HISTORY,
			IPackageFragmentRoot.REPLACE | IPackageFragmentRoot.DESTINATION_PROJECT_CLASSPATH,
			null,
			null);
		assertDeltas(
			"Unexpected delta",
			"P[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
			"	src1[+]: {}\n" +
			"	ResourceDelta(/P/.classpath)[*]"
		);
		assertJavaProject(
			"P\n" +
			"	src\n" +
			"		<default>\n" +
			"		p\n" +
			"			X.java\n" +
			"	src1\n" +
			"		<default>\n" +
			"		p\n" +
			"			X.java\n" +
			"	L/P/.classpath\n" +
			"	L/P/.project",
			project);
	} finally {
		stopDeltas();
		this.deleteProject("P");
	}
}
/*
 * Ensure that coping a source root to another project with an existing source root in
 * non REPLACE mode throws the right JavaModelException.
 * (regression test bug 30511 IPackageFragmentRoot:move ignores FORCE flag)
 */
public void testFailCopySourceFolder1() throws CoreException {
	try {
		this.createJavaProject("P1", new String[] {"src"}, "bin");
		this.createJavaProject("P2", new String[] {"src"}, "bin");

		IPackageFragmentRoot root = this.getPackageFragmentRoot("/P1/src");
		try {
			root.copy(new Path("/P2/src"), IResource.NONE, IPackageFragmentRoot.DESTINATION_PROJECT_CLASSPATH, null, null);
		} catch (JavaModelException e) {
			assertEquals("/P2/src already exists in target", e.getMessage());
			return;
		}
		assertTrue("Should throw a JavaModelException", false);
	} finally {
		this.deleteProject("P1");
		this.deleteProject("P2");
	}
}
/*
 * Ensure that coping a source root to another project with an existing source root in
 * non REPLACE mode throws the right JavaModelException.
 * (regression test bug 30511 IPackageFragmentRoot:move ignores FORCE flag)
 */
public void testFailCopySourceFolder2() throws CoreException {
	try {
		this.createJavaProject("P1", new String[] {"src"}, "bin");
		this.createJavaProject("P2", new String[] {"src"}, "bin");
		this.deleteFolder("/P2/src");

		IPackageFragmentRoot root = this.getPackageFragmentRoot("/P1/src");
		try {
			root.copy(new Path("/P2/src"), IResource.NONE, IPackageFragmentRoot.DESTINATION_PROJECT_CLASSPATH, null, null);
		} catch (JavaModelException e) {
			assertEquals("/P2/src already exists in target", e.getMessage());
			return;
		}
		assertTrue("Should throw a JavaModelException", false);
	} finally {
		this.deleteProject("P1");
		this.deleteProject("P2");
	}
}
/*
 * Ensure that deleting a jar package fragment root triggers the right delta
 * and that the model is up-to-date.
 */
public void testDeleteJarFile1() throws CoreException {
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {"src"}, new String[] {"/P/myLib.jar"}, "bin");
		this.createFile("/P/myLib.jar", "");

		IPackageFragmentRoot root = this.getPackageFragmentRoot("/P/myLib.jar");
		startDeltas();
		delete(root);
		assertDeltas(
			"Unexpected delta",
			"P[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
			"	myLib.jar[-]: {}\n" +
			"	ResourceDelta(/P/.classpath)[*]"
		);
		assertJavaProject(
			"P\n" +
			"	src\n" +
			"		<default>\n" +
			"	L/P/.classpath\n" +
			"	L/P/.project",
			project);
	} finally {
		stopDeltas();
		this.deleteProject("P");
	}
}
/*
 * Ensure that deleting am external jar package fragment root triggers the right delta
 * and that the model is up-to-date.
 * (regression test for bug 30506 IPackageFragmentRoot:delete does not handle external jars)
 */
public void testDeleteJarFile3() throws CoreException {
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {"src"}, new String[] {getExternalJCLPathString()}, "bin");

		IPackageFragmentRoot root = project.getPackageFragmentRoot(getExternalJCLPathString());
		startDeltas();
		delete(root);
		assertDeltas(
			"Unexpected delta",
			"P[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
			"	" + getExternalJCLPathString() + "[*]: {REMOVED FROM CLASSPATH}\n" +
			"	ResourceDelta(/P/.classpath)[*]"
		);
		assertJavaProject(
			"P\n" +
			"	src\n" +
			"		<default>\n" +
			"	L/P/.classpath\n" +
			"	L/P/.project",
			project);
	} finally {
		stopDeltas();
		this.deleteProject("P");
	}
}
/*
 * Ensure that deleting a jar file that is referenced by 2 projects triggers the right delta
 * and that the model is up-to-date.
 */
public void testDeleteJarFile2() throws CoreException {
	try {
		IJavaProject p1 = this.createJavaProject("P1", new String[] {"src"}, new String[] {"/P1/myLib.jar"}, "bin");
		this.createFile("/P1/myLib.jar", "");
		IJavaProject p2 = this.createJavaProject("P2", new String[] {"src"}, new String[] {"/P1/myLib.jar"}, "bin");

		IPackageFragmentRoot root = this.getPackageFragmentRoot("/P1/myLib.jar");
		startDeltas();
		delete(root);
		assertDeltas(
			"Unexpected delta",
			"P1[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
			"	myLib.jar[-]: {}\n" +
			"	ResourceDelta(/P1/.classpath)[*]\n" +
			"P2[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
			"	/P1/myLib.jar[-]: {}\n" +
			"	ResourceDelta(/P2/.classpath)[*]"
		);
		assertJavaProject(
			"P1\n" +
			"	src\n" +
			"		<default>\n" +
			"	L/P1/.classpath\n" +
			"	L/P1/.project",
			p1);
		assertJavaProject(
			"P2\n" +
			"	src\n" +
			"		<default>\n" +
			"	L/P2/.classpath\n" +
			"	L/P2/.project",
			p2);
	} finally {
		stopDeltas();
		this.deleteProject("P1");
		this.deleteProject("P2");
	}
}
/*
 * Ensure that a simple delete of a source root triggers the right delta
 * and that the model is up-to-date.
 */
public void testDeleteSourceFolder1() throws CoreException {
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {"src"}, "bin");
		this.createFolder("/P/src/p");
		this.createFile(
			"/P/src/p/X.java",
			"package p;\n" +
			"public class X {\n" +
			"}"
		);
		IPackageFragmentRoot root = this.getPackageFragmentRoot("/P/src");
		startDeltas();
		delete(root);
		assertDeltas(
			"Unexpected delta",
			"P[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
			"	src[-]: {}\n" +
			"	ResourceDelta(/P/.classpath)[*]"
		);
		assertJavaProject(
			"P\n" +
			"	L/P/.classpath\n" +
			"	L/P/.project",
			project);
	} finally {
		stopDeltas();
		this.deleteProject("P");
	}
}
/*
 * Ensure that deleting a source root triggers the right delta
 * and doesn't delete a nested source folder.
 */
public void testDeleteSourceFolder2() throws CoreException {
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {}, "bin");
		project.setRawClasspath(createClasspath(new String[] {"/P/src1", "src2/**", "/P/src1/src2", ""}, false/*no inclusion*/, true/*exclusion*/), null);
		IFolder folder = this.createFolder("/P/src1/p");
		IFile file = this.createFile(
			"/P/src1/p/X.java",
			"package p;\n" +
			"public class X {\n" +
			"}"
		);
		this.createFolder("/P/src1/src2/q");
		this.createFile(
			"/P/src1/src2/q/Y.java",
			"package q;\n" +
			"public class Y {\n" +
			"}"
		);
		IPackageFragmentRoot root = this.getPackageFragmentRoot("/P/src1");
		startDeltas();
		delete(root);

		assertDeltas(
			"Unexpected delta",
			"P[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
			"	src1[*]: {REMOVED FROM CLASSPATH}\n" +
			"	src1/src2[*]: {REORDERED}\n" +
			"	ResourceDelta(/P/.classpath)[*]\n" +
			"	ResourceDelta(/P/src1)[*]"
		);

		assertJavaProject(
			"P\n" +
			"	src1/src2\n" +
			"		<default>\n" +
			"		q\n" +
			"			Y.java\n" +
			"	L/P/.classpath\n" +
			"	L/P/.project\n" +
			"	F/P/src1",
			project);

		assertTrue("Original package folder should not exist", !folder.exists());
		assertTrue("Original cu file should not exist", !file.exists());
	} finally {
		stopDeltas();
		this.deleteProject("P");
	}
}
/*
 * Ensure that a simple move of a source root to another project triggers the right delta
 * and that the model is up-to-date.
 */
public void testMoveSourceFolder1() throws CoreException {
	try {
		IJavaProject p1 = this.createJavaProject("P1", new String[] {"src"}, "bin");
		IJavaProject p2 = this.createJavaProject("P2", new String[] {}, "bin");
		this.createFolder("/P1/src/p");
		this.createFile(
			"/P1/src/p/X.java",
			"package p;\n" +
			"public class X {\n" +
			"}"
		);
		IPackageFragmentRoot root = this.getPackageFragmentRoot("/P1/src");
		startDeltas();
		this.move(root, new Path("/P2/src"));
		assertDeltas(
			"Unexpected delta",
			"P1[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
			"	src[-]: {MOVED_TO(src [in P2])}\n" +
			"	ResourceDelta(/P1/.classpath)[*]\n" +
			"P2[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
			"	src[+]: {MOVED_FROM(src [in P1])}\n" +
			"	ResourceDelta(/P2/.classpath)[*]"
		);
		assertJavaProject(
			"P1\n" +
			"	L/P1/.classpath\n" +
			"	L/P1/.project",
			p1);
		assertJavaProject(
			"P2\n" +
			"	src\n" +
			"		<default>\n" +
			"		p\n" +
			"			X.java\n" +
			"	L/P2/.classpath\n" +
			"	L/P2/.project",
			p2);
	} finally {
		stopDeltas();
		this.deleteProject("P1");
		this.deleteProject("P2");
	}
}
/*
 * Ensure that moving and renaming a source root to another project triggers the right delta
 * and that the model is up-to-date.
 */
public void testMoveSourceFolder2() throws CoreException {
	try {
		IJavaProject p1 = this.createJavaProject("P1", new String[] {"src"}, "bin");
		IJavaProject p2 = this.createJavaProject("P2", new String[] {}, "bin");
		this.createFolder("/P1/src/p");
		this.createFile(
			"/P1/src/p/X.java",
			"package p;\n" +
			"public class X {\n" +
			"}"
		);
		IPackageFragmentRoot root = this.getPackageFragmentRoot("/P1/src");
		startDeltas();
		this.move(root, new Path("/P2/src2"));

		assertDeltas(
			"Unexpected delta",
			"P1[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
			"	src[-]: {MOVED_TO(src2 [in P2])}\n" +
			"	ResourceDelta(/P1/.classpath)[*]\n" +
			"P2[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
			"	src2[+]: {MOVED_FROM(src [in P1])}\n" +
			"	ResourceDelta(/P2/.classpath)[*]"
		);

		assertJavaProject(
			"P1\n" +
			"	L/P1/.classpath\n" +
			"	L/P1/.project",
			p1);
		assertJavaProject(
			"P2\n" +
			"	src2\n" +
			"		<default>\n" +
			"		p\n" +
			"			X.java\n" +
			"	L/P2/.classpath\n" +
			"	L/P2/.project",
			p2);
	} finally {
		stopDeltas();
		this.deleteProject("P1");
		this.deleteProject("P2");
	}
}
/*
 * Ensure that moving a source root to another project triggers the right delta
 * and doesn't move a nested source folder.
 */
public void testMoveSourceFolder3() throws CoreException {
	try {
		IJavaProject p1 = this.createJavaProject("P1", new String[] {}, "bin");
		p1.setRawClasspath(createClasspath(new String[] {"/P1/src1", "src2/**", "/P1/src1/src2", ""}, false/*no inclusion*/, true/*exclusion*/), null);
		IJavaProject p2 = this.createJavaProject("P2", new String[] {}, "bin");
		this.createFolder("/P1/src1/p");
		this.createFile(
			"/P1/src1/p/X.java",
			"package p;\n" +
			"public class X {\n" +
			"}"
		);
		this.createFolder("/P1/src1/src2/q");
		this.createFile(
			"/P1/src1/src2/q/Y.java",
			"package q;\n" +
			"public class Y {\n" +
			"}"
		);
		IPackageFragmentRoot root = this.getPackageFragmentRoot("/P1/src1");
		startDeltas();
		this.move(root, new Path("/P2/src1"));

		assertDeltas(
			"Unexpected delta",
			"P1[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
			"	src1[*]: {REMOVED FROM CLASSPATH}\n" +
			"	src1/src2[*]: {REORDERED}\n" +
			"	ResourceDelta(/P1/.classpath)[*]\n" +
			"	ResourceDelta(/P1/src1)[*]\n" +
			"P2[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
			"	src1[+]: {}\n" +
			"	ResourceDelta(/P2/.classpath)[*]"
		);

		assertJavaProject(
			"P1\n" +
			"	src1/src2\n" +
			"		<default>\n" +
			"		q\n" +
			"			Y.java\n" +
			"	L/P1/.classpath\n" +
			"	L/P1/.project\n" +
			"	F/P1/src1",
			p1);
		assertJavaProject(
			"P2\n" +
			"	src1\n" +
			"		<default>\n" +
			"		p\n" +
			"			X.java\n" +
			"	L/P2/.classpath\n" +
			"	L/P2/.project",
			p2);
	} finally {
		stopDeltas();
		this.deleteProject("P1");
		this.deleteProject("P2");
	}
}
/*
 * Ensure that moving a source root to another project before the first root triggers the right delta
 * and that the model is up-to-date.
 */
public void testMoveSourceFolder4() throws CoreException {
	try {
		IJavaProject p1 = this.createJavaProject("P1", new String[] {"src"}, "bin");
		IJavaProject p2 = this.createJavaProject("P2", new String[] {"src1", "src2"}, "bin");
		IPackageFragmentRoot root = this.getPackageFragmentRoot("/P1/src");

		// insert first
		IClasspathEntry sibling = JavaCore.newSourceEntry(new Path("/P2/src1"));
		startDeltas();
		this.move(root, new Path("/P2/src"), sibling);
		assertDeltas(
			"Unexpected delta",
			"P1[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
			"	src[-]: {MOVED_TO(src [in P2])}\n" +
			"	ResourceDelta(/P1/.classpath)[*]\n" +
			"P2[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
			"	src[+]: {MOVED_FROM(src [in P1])}\n" +
			"	src1[*]: {REORDERED}\n" +
			"	src2[*]: {REORDERED}\n" +
			"	ResourceDelta(/P2/.classpath)[*]"
		);
		assertJavaProject(
			"P1\n" +
			"	L/P1/.classpath\n" +
			"	L/P1/.project",
			p1);
		assertJavaProject(
			"P2\n" +
			"	src\n" +
			"		<default>\n" +
			"	src1\n" +
			"		<default>\n" +
			"	src2\n" +
			"		<default>\n" +
			"	L/P2/.classpath\n" +
			"	L/P2/.project",
			p2);
	} finally {
		stopDeltas();
		this.deleteProject("P1");
		this.deleteProject("P2");
	}
}
/*
 * Ensure that moving a source root to another project in the middle of existing roots triggers the right delta
 * and that the model is up-to-date.
 */
public void testMoveSourceFolder5() throws CoreException {
	try {
		IJavaProject p1 = this.createJavaProject("P1", new String[] {"src"}, "bin");
		IJavaProject p2 = this.createJavaProject("P2", new String[] {"src1", "src2"}, "bin");
		IPackageFragmentRoot root = this.getPackageFragmentRoot("/P1/src");

		// insert in the middle
		IClasspathEntry sibling = JavaCore.newSourceEntry(new Path("/P2/src2"));
		startDeltas();
		this.move(root, new Path("/P2/src"), sibling);
		assertDeltas(
			"Unexpected delta",
			"P1[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
			"	src[-]: {MOVED_TO(src [in P2])}\n" +
			"	ResourceDelta(/P1/.classpath)[*]\n" +
			"P2[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
			"	src[+]: {MOVED_FROM(src [in P1])}\n" +
			"	src2[*]: {REORDERED}\n" +
			"	ResourceDelta(/P2/.classpath)[*]"
		);
		assertJavaProject(
			"P1\n" +
			"	L/P1/.classpath\n" +
			"	L/P1/.project",
			p1);
		assertJavaProject(
			"P2\n" +
			"	src1\n" +
			"		<default>\n" +
			"	src\n" +
			"		<default>\n" +
			"	src2\n" +
			"		<default>\n" +
			"	L/P2/.classpath\n" +
			"	L/P2/.project",
			p2);
	} finally {
		stopDeltas();
		this.deleteProject("P1");
		this.deleteProject("P2");
	}
}
/*
 * Ensure that moving a source root to another project at the end of the classpath triggers the right delta
 * and that the model is up-to-date.
 */
public void testMoveSourceFolder6() throws CoreException {
	try {
		IJavaProject p1 = this.createJavaProject("P1", new String[] {"src"}, "bin");
		IJavaProject p2 = this.createJavaProject("P2", new String[] {"src1", "src2"}, "bin");
		IPackageFragmentRoot root = this.getPackageFragmentRoot("/P1/src");

		// insert last
		startDeltas();
		this.move(root, new Path("/P2/src"), null);
		assertDeltas(
			"Unexpected delta",
			"P1[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
			"	src[-]: {MOVED_TO(src [in P2])}\n" +
			"	ResourceDelta(/P1/.classpath)[*]\n" +
			"P2[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
			"	src[+]: {MOVED_FROM(src [in P1])}\n" +
			"	ResourceDelta(/P2/.classpath)[*]"
		);
		assertJavaProject(
			"P1\n" +
			"	L/P1/.classpath\n" +
			"	L/P1/.project",
			p1);
		assertJavaProject(
			"P2\n" +
			"	src1\n" +
			"		<default>\n" +
			"	src2\n" +
			"		<default>\n" +
			"	src\n" +
			"		<default>\n" +
			"	L/P2/.classpath\n" +
			"	L/P2/.project",
			p2);
	} finally {
		stopDeltas();
		this.deleteProject("P1");
		this.deleteProject("P2");
	}
}
/*
 * Ensure that a simple rename of a source root triggers the right delta
 * and that the model is up-to-date.
 */
public void testRenameSourceFolder1() throws CoreException {
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {"src1"}, "bin");
		this.createFolder("/P/src1/p");
		this.createFile(
			"/P/src1/p/X.java",
			"package p;\n" +
			"public class X {\n" +
			"}"
		);
		IPackageFragmentRoot root = this.getPackageFragmentRoot("/P/src1");
		startDeltas();
		this.move(root, new Path("/P/src2"));
		assertDeltas(
			"Unexpected delta",
			"P[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
			"	src1[-]: {MOVED_TO(src2 [in P])}\n" +
			"	src2[+]: {MOVED_FROM(src1 [in P])}\n" +
			"	ResourceDelta(/P/.classpath)[*]"
		);
		assertJavaProject(
			"P\n" +
			"	src2\n" +
			"		<default>\n" +
			"		p\n" +
			"			X.java\n" +
			"	L/P/.classpath\n" +
			"	L/P/.project",
			project);
	} finally {
		stopDeltas();
		this.deleteProject("P");
	}
}
/*
 * Ensure that renaming a nested source root doesn't throw a JavaModelException
 * (regression test for bug 129991 [refactoring] Rename sourcefolder fails with JME)
 */
public void testRenameSourceFolder3() throws CoreException {
	try {
		createJavaProject("P");
		editFile(
			"/P/.classpath",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"	<classpathentry excluding=\"src1/\" kind=\"src\" path=\"\"/>\n" +
			"	<classpathentry kind=\"src\" path=\"src1\"/>\n" +
			"</classpath>"
		);
		createFolder("/P/src1");
		IPackageFragmentRoot root = getPackageFragmentRoot("/P/src1");
		startDeltas();
		move(root, new Path("/P/src2"));
		assertDeltas(
			"Unexpected delta",
			"P[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
			"	<project root>[*]: {ADDED TO CLASSPATH | REMOVED FROM CLASSPATH}\n" +
			"	src1[-]: {MOVED_TO(src2 [in P])}\n" +
			"	src2[+]: {MOVED_FROM(src1 [in P])}\n" +
			"	ResourceDelta(/P/.classpath)[*]"
		);
	} finally {
		stopDeltas();
		deleteProject("P");
	}
}
/*
 * Ensure that renaming a nested source root doesn't throw a JavaModelException
 * (regression test for bug 129991 [refactoring] Rename sourcefolder fails with JME)
 */
public void testRenameSourceFolder4() throws CoreException {
	try {
		createJavaProject("P");
		editFile(
			"/P/.classpath",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"	<classpathentry excluding=\"src1/**\" kind=\"src\" path=\"\"/>\n" +
			"	<classpathentry kind=\"src\" path=\"src1\"/>\n" +
			"</classpath>"
		);
		createFolder("/P/src1");
		IPackageFragmentRoot root = getPackageFragmentRoot("/P/src1");
		startDeltas();
		move(root, new Path("/P/src2"));
		assertDeltas(
			"Unexpected delta",
			"P[*]: {CHILDREN}\n" +
			"	<project root>[*]: {CHILDREN}\n" +
			"		src2[+]: {MOVED_FROM(<default> [in src1 [in P]])}\n" +
			"	src1[-]: {MOVED_TO(src2 [in <project root> [in P]])}"
		);
	} finally {
		stopDeltas();
		deleteProject("P");
	}
}
/*
 * Ensure that renaming a source root keeps the same roots order,
 * and that it triggers the right delta and that the model is up-to-date.
 */
public void testRenameSourceFolder2() throws CoreException {
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {"src1", "src2", "src3"}, "bin");

		// rename src1
		IPackageFragmentRoot root = this.getPackageFragmentRoot("/P/src1");
		startDeltas();
		this.move(root, new Path("/P/src4"));
		assertDeltas(
			"Unexpected delta after renaming src1",
			"P[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
			"	src1[-]: {MOVED_TO(src4 [in P])}\n" +
			"	src4[+]: {MOVED_FROM(src1 [in P])}\n" +
			"	ResourceDelta(/P/.classpath)[*]"
		);
		assertJavaProject(
			"P\n" +
			"	src4\n" +
			"		<default>\n" +
			"	src2\n" +
			"		<default>\n" +
			"	src3\n" +
			"		<default>\n" +
			"	L/P/.classpath\n" +
			"	L/P/.project",
			project);

		// rename src2
		root = this.getPackageFragmentRoot("/P/src2");
		clearDeltas();
		this.move(root, new Path("/P/src5"));
		assertDeltas(
			"Unexpected delta after renaming src2",
			"P[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
			"	src2[-]: {MOVED_TO(src5 [in P])}\n" +
			"	src5[+]: {MOVED_FROM(src2 [in P])}\n" +
			"	ResourceDelta(/P/.classpath)[*]"
		);
		assertJavaProject(
			"P\n" +
			"	src4\n" +
			"		<default>\n" +
			"	src5\n" +
			"		<default>\n" +
			"	src3\n" +
			"		<default>\n" +
			"	L/P/.classpath\n" +
			"	L/P/.project",
			project);

		// rename src3
		root = this.getPackageFragmentRoot("/P/src3");
		clearDeltas();
		this.move(root, new Path("/P/src6"));
		assertDeltas(
			"Unexpected delta after renaming src3",
			"P[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
			"	src3[-]: {MOVED_TO(src6 [in P])}\n" +
			"	src6[+]: {MOVED_FROM(src3 [in P])}\n" +
			"	ResourceDelta(/P/.classpath)[*]"
		);
		assertJavaProject(
			"P\n" +
			"	src4\n" +
			"		<default>\n" +
			"	src5\n" +
			"		<default>\n" +
			"	src6\n" +
			"		<default>\n" +
			"	L/P/.classpath\n" +
			"	L/P/.project",
			project);
	} finally {
		stopDeltas();
		this.deleteProject("P");
	}
}
/*
 * Ensure that a simple rename of a jar file triggers the right delta
 * and that the model is up-to-date.
 */
public void testRenameJarFile1() throws CoreException {
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {"src"}, new String[] {"/P/myLib.jar"}, "bin");
		this.createFile("/P/myLib.jar", "");

		IPackageFragmentRoot root = this.getPackageFragmentRoot("/P/myLib.jar");
		startDeltas();
		this.move(root, new Path("/P/myLib2.jar"));
		assertDeltas(
			"Unexpected delta",
			"P[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
			"	myLib.jar[-]: {MOVED_TO(myLib2.jar [in P])}\n" +
			"	myLib2.jar[+]: {MOVED_FROM(myLib.jar [in P])}\n" +
			"	ResourceDelta(/P/.classpath)[*]"
		);
		assertJavaProject(
			"P\n" +
			"	src\n" +
			"		<default>\n" +
			"	myLib2.jar\n" +
			"	L/P/.classpath\n" +
			"	L/P/.project",
			project);
	} finally {
		stopDeltas();
		this.deleteProject("P");
	}
}
/*
 * Ensure that renaming of a jar file that is referenced by 2 projects triggers the right delta
 * and that the model is up-to-date.
 */
public void testRenameJarFile2() throws CoreException {
	try {
		IJavaProject p1 = this.createJavaProject("P1", new String[] {"src"}, new String[] {"/P1/myLib.jar"}, "bin");
		this.createFile("/P1/myLib.jar", "");
		IJavaProject p2 = this.createJavaProject("P2", new String[] {"src"}, new String[] {"/P1/myLib.jar"}, "bin");

		IPackageFragmentRoot root = this.getPackageFragmentRoot("/P1/myLib.jar");
		startDeltas();
		this.move(root, new Path("/P1/myLib2.jar"));
		assertDeltas(
			"Unexpected delta",
			"P1[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
			"	myLib.jar[-]: {MOVED_TO(myLib2.jar [in P1])}\n" +
			"	myLib2.jar[+]: {MOVED_FROM(myLib.jar [in P1])}\n" +
			"	ResourceDelta(/P1/.classpath)[*]\n" +
			"P2[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
			"	/P1/myLib.jar[-]: {MOVED_TO(myLib2.jar [in P1])}\n" +
			"	/P1/myLib2.jar[+]: {MOVED_FROM(myLib.jar [in P1])}\n" +
			"	ResourceDelta(/P2/.classpath)[*]"
		);
		assertJavaProject(
			"P1\n" +
			"	src\n" +
			"		<default>\n" +
			"	myLib2.jar\n" +
			"	L/P1/.classpath\n" +
			"	L/P1/.project",
			p1);
		assertJavaProject(
			"P2\n" +
			"	src\n" +
			"		<default>\n" +
			"	/P1/myLib2.jar\n" +
			"	L/P2/.classpath\n" +
			"	L/P2/.project",
			p2);
	} finally {
		stopDeltas();
		this.deleteProject("P1");
		this.deleteProject("P2");
	}
}
/*
 * Ensure that renaming of a jar file to an existing file in REPLACE mode
 * triggers the right delta and that the model is up-to-date.
 */
public void testRenameJarFile3() throws CoreException {
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {"src"}, new String[] {"/P/myLib1.jar", "/P/myLib2.jar"}, "bin");
		this.createFile("/P/myLib1.jar", "");
		this.createFile("/P/myLib2.jar", "");

		IPackageFragmentRoot root = this.getPackageFragmentRoot("/P/myLib1.jar");
		startDeltas();
		root.move(
			new Path("/P/myLib2.jar"),
			IResource.NONE,
			IPackageFragmentRoot.ORIGINATING_PROJECT_CLASSPATH
				| IPackageFragmentRoot.DESTINATION_PROJECT_CLASSPATH
				| IPackageFragmentRoot.REPLACE,
			null,
			null);
		assertDeltas(
			"Unexpected delta",
			"P[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
			"	myLib1.jar[-]: {MOVED_TO(myLib2.jar [in P])}\n" +
			"	myLib2.jar[*]: {CONTENT | REORDERED | ARCHIVE CONTENT CHANGED}\n" +
			"	ResourceDelta(/P/.classpath)[*]"
		);
		assertJavaProject(
			"P\n" +
			"	src\n" +
			"		<default>\n" +
			"	myLib2.jar\n" +
			"	L/P/.classpath\n" +
			"	L/P/.project",
			project);
	} finally {
		stopDeltas();
		this.deleteProject("P");
	}
}
}
