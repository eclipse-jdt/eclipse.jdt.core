/*******************************************************************************
 * Copyright (c) 2003 IBM Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
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
protected void populate(StringBuffer buffer, IJavaElement element, int indent) throws CoreException {
	if (!(element instanceof IParent) || !(element instanceof IOpenable)) return;

	if (buffer.length() != 0) {	
		buffer.append("\n");
	}
	for (int i = 0; i < indent; i++) buffer.append("\t");
	buffer.append(((JavaElement)element).toDebugString());
	
	IParent parent = (IParent)element;
	IJavaElement[] children = parent.getChildren();
	for (int i = 0, length = children.length; i < length; i++) {
		populate(buffer, children[i], indent+1);
	}
	Object[] nonJavaResources = null;
	if (element instanceof IJavaProject) {
		nonJavaResources = ((IJavaProject)element).getNonJavaResources();
	} else if (element instanceof IPackageFragmentRoot) {
		nonJavaResources = ((IPackageFragmentRoot)element).getNonJavaResources();
	} else if (element instanceof IPackageFragment) {
		nonJavaResources = ((IPackageFragment)element).getNonJavaResources();
	}
	if (nonJavaResources != null) {
		for (int i = 0, length = nonJavaResources.length; i < length; i++) {
			populate(buffer, nonJavaResources[i], indent+1);
		}
	}
}
protected void populate(StringBuffer buffer, Object nonJavaResource, int indent) throws CoreException {
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
	return new Suite(RootManipulationsTests.class);
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
		this.startDeltas();
		root.copy(new Path("/P2/src"), IResource.NONE, true, null, null);
		assertDeltas(
			"Unexpected delta",
			"P2[*]: {CHILDREN}\n" + 
			"	src[+]: {}\n" + 
			"	ResourceDelta(/P2/.classpath)[*]"
		);
		ICompilationUnit cu = this.getCompilationUnit("/P2/src/p/X.java");
		assertTrue("Destination cu should exist", cu.exists());
	} finally {
		this.stopDeltas();
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
		this.startDeltas();
		root.copy(new Path("/P2/src2"), IResource.NONE, true, null, null);
		assertDeltas(
			"Unexpected delta",
			"P2[*]: {CHILDREN}\n" + 
			"	src2[+]: {}\n" + 
			"	ResourceDelta(/P2/.classpath)[*]"
		);
		ICompilationUnit cu = this.getCompilationUnit("/P2/src2/p/X.java");
		assertTrue("Destination cu should exist", cu.exists());
	} finally {
		this.stopDeltas();
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
		p1.setRawClasspath(createClasspath(new String[] {"/P1/src1", "src2/**", "/P1/src1/src2", ""}), null);
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
		this.startDeltas();
		root.copy(new Path("/P2/src1"), IResource.NONE, true, null, null);
		assertDeltas(
			"Unexpected delta",
			"P2[*]: {CHILDREN}\n" + 
			"	src1[+]: {}\n" + 
			"	ResourceDelta(/P2/.classpath)[*]"
		);
		ICompilationUnit cu = this.getCompilationUnit("/P2/src1/p/X.java");
		assertTrue("Destination cu should exist", cu.exists());
		cu = this.getCompilationUnit("/P2/src1/src2/q/Y.java");
		assertTrue("Nested cu should not exist", !cu.exists());
	} finally {
		this.stopDeltas();
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
		this.startDeltas();
		root.copy(new Path("/P2/src"), IResource.NONE, true, sibling, null);
		assertDeltas(
			"Unexpected delta (1)",
			"P2[*]: {CHILDREN}\n" + 
			"	src[+]: {}\n" + 
			"	ResourceDelta(/P2/.classpath)[*]"
		);
		assertElementsEqual(
			"Unexpected roots of P2 after insertion first",
			"src\n" + 
			"src1\n" + 
			"src2",
			p2.getPackageFragmentRoots());

		// insert in the middle
		sibling = JavaCore.newSourceEntry(new Path("/P2/src2"));
		this.startDeltas();
		root.copy(new Path("/P2/src3"), IResource.NONE, true, sibling, null);
		assertDeltas(
			"Unexpected delta (2)",
			"P2[*]: {CHILDREN}\n" + 
			"	src3[+]: {}\n" + 
			"	ResourceDelta(/P2/.classpath)[*]"
		);
		assertElementsEqual(
			"Unexpected roots of P2 after insertion in the middle",
			"src\n" + 
			"src1\n" + 
			"src3\n" + 
			"src2",
			p2.getPackageFragmentRoots());
			
		// insert last
		this.startDeltas();
		root.copy(new Path("/P2/src4"), IResource.NONE, true, null, null);
		assertDeltas(
			"Unexpected delta (3)",
			"P2[*]: {CHILDREN}\n" + 
			"	src4[+]: {}\n" + 
			"	ResourceDelta(/P2/.classpath)[*]"
		);
		assertElementsEqual(
			"Unexpected roots of P2 after insertion last",
			"src\n" + 
			"src1\n" + 
			"src3\n" + 
			"src2\n" +
			"src4",
			p2.getPackageFragmentRoots());
	} finally {
		this.stopDeltas();
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
		this.startDeltas();
		root.delete(IResource.NONE, true, null);
		// TODO: (jerome) Improve deltas (it should really only show root deltas)
		assertDeltas(
			"Unexpected delta",
			"P[*]: {CHILDREN}\n" + 
			"	src[*]: {REMOVED FROM CLASSPATH}\n" + 
			"	ResourceDelta(/P/.classpath)[*]\n" + 
			"	ResourceDelta(/P/src)[-]"
		);
		assertJavaProject(
			"P\n" + 
			"	L/P/.classpath\n" + 
			"	L/P/.project",
			project);
	} finally {
		this.stopDeltas();
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
		project.setRawClasspath(createClasspath(new String[] {"/P/src1", "src2/**", "/P/src1/src2", ""}), null);
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
		this.startDeltas();
		root.delete(IResource.NONE, true, null);
		
		assertDeltas(
			"Unexpected delta",
			"P[*]: {CHILDREN}\n" + 
			"	src1[*]: {REMOVED FROM CLASSPATH}\n" + 
			"	src1/src2[*]: {REORDERED IN CLASSPATH}\n" + 
			"	ResourceDelta(/P/.classpath)[*]\n" + 
			"	ResourceDelta(/P/src1)[*]"
		);
		
		assertJavaProject(
			"P\n" + 
			"	src1/src2\n" + 
			"		[default]\n" + 
			"		q\n" + 
			"			Y.java\n" + 
			"	L/P/.classpath\n" + 
			"	L/P/.project\n" + 
			"	F/P/src1",
			project);
			
		assertTrue("Original package folder should not exist", !folder.exists());
		assertTrue("Original cu file should not exist", !file.exists());
	} finally {
		this.stopDeltas();
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
		this.startDeltas();
		root.move(new Path("/P2/src"), IResource.NONE, true, null, null);
		// TODO: (jerome) Improve deltas (it should really only show root deltas)
		assertDeltas(
			"Unexpected delta",
			"P2[*]: {CHILDREN}\n" + 
			"	src[+]: {}\n" + 
			"	ResourceDelta(/P2/.classpath)[*]\n" + 
			"P1[*]: {CHILDREN}\n" + 
			"	src[*]: {REMOVED FROM CLASSPATH}\n" + 
			"	ResourceDelta(/P1/.classpath)[*]\n" + 
			"	ResourceDelta(/P1/.project)[*]\n" + 
			"	ResourceDelta(/P1/src)[-]"
		);
		assertJavaProject(
			"P1\n" + 
			"	L/P1/.classpath\n" + 
			"	L/P1/.project",
			p1);
		assertJavaProject(
			"P2\n" + 
			"	src\n" + 
			"		[default]\n" + 
			"		p\n" + 
			"			X.java\n" + 
			"	L/P2/.classpath\n" + 
			"	L/P2/.project",
			p2);
	} finally {
		this.stopDeltas();
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
		this.startDeltas();
		root.move(new Path("/P2/src2"), IResource.NONE, true, null, null);
		
		assertDeltas(
			"Unexpected delta",
			"P2[*]: {CHILDREN}\n" + 
			"	src2[+]: {}\n" + 
			"	ResourceDelta(/P2/.classpath)[*]\n" + 
			"P1[*]: {CHILDREN}\n" + 
			"	src[*]: {REMOVED FROM CLASSPATH}\n" + 
			"	ResourceDelta(/P1/.classpath)[*]\n" + 
			"	ResourceDelta(/P1/.project)[*]\n" + 
			"	ResourceDelta(/P1/src)[-]"
		);
		
		assertJavaProject(
			"P1\n" + 
			"	L/P1/.classpath\n" + 
			"	L/P1/.project",
			p1);
		assertJavaProject(
			"P2\n" + 
			"	src2\n" + 
			"		[default]\n" + 
			"		p\n" + 
			"			X.java\n" + 
			"	L/P2/.classpath\n" + 
			"	L/P2/.project",
			p2);
	} finally {
		this.stopDeltas();
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
		p1.setRawClasspath(createClasspath(new String[] {"/P1/src1", "src2/**", "/P1/src1/src2", ""}), null);
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
		this.startDeltas();
		root.move(new Path("/P2/src1"), IResource.NONE, true, null, null);
		
		assertDeltas(
			"Unexpected delta",
			"P2[*]: {CHILDREN}\n" + 
			"	src1[+]: {}\n" + 
			"	ResourceDelta(/P2/.classpath)[*]\n" + 
			"P1[*]: {CHILDREN}\n" + 
			"	src1[*]: {REMOVED FROM CLASSPATH}\n" + 
			"	ResourceDelta(/P1/.classpath)[*]\n" + 
			"	ResourceDelta(/P1/.project)[*]\n" + 
			"	ResourceDelta(/P1/src1)[*]"
		);
		
		assertJavaProject(
			"P1\n" + 
			"	src1/src2\n" + 
			"		[default]\n" + 
			"		q\n" + 
			"			Y.java\n" + 
			"	L/P1/.classpath\n" + 
			"	L/P1/.project\n" + 
			"	F/P1/src1",
			p1);
		assertJavaProject(
			"P2\n" + 
			"	src1\n" + 
			"		[default]\n" + 
			"		p\n" + 
			"			X.java\n" + 
			"	L/P2/.classpath\n" + 
			"	L/P2/.project",
			p2);
	} finally {
		this.stopDeltas();
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
		this.startDeltas();
		root.move(new Path("/P2/src"), IResource.NONE, true, sibling, null);
		assertDeltas(
			"Unexpected delta",
			"P2[*]: {CHILDREN}\n" + 
			"	src[+]: {}\n" + 
			"	ResourceDelta(/P2/.classpath)[*]\n" + 
			"P1[*]: {CHILDREN}\n" + 
			"	src[*]: {REMOVED FROM CLASSPATH}\n" + 
			"	ResourceDelta(/P1/.classpath)[*]\n" + 
			"	ResourceDelta(/P1/.project)[*]\n" + 
			"	ResourceDelta(/P1/src)[-]"
		);
		assertJavaProject(
			"P1\n" + 
			"	L/P1/.classpath\n" + 
			"	L/P1/.project",
			p1);
		assertJavaProject(
			"P2\n" + 
			"	src\n" + 
			"		[default]\n" + 
			"	src1\n" + 
			"		[default]\n" + 
			"	src2\n" + 
			"		[default]\n" + 
			"	L/P2/.classpath\n" + 
			"	L/P2/.project",
			p2);
	} finally {
		this.stopDeltas();
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
		this.startDeltas();
		root.move(new Path("/P2/src"), IResource.NONE, true, sibling, null);
		assertDeltas(
			"Unexpected delta",
			"P2[*]: {CHILDREN}\n" + 
			"	src[+]: {}\n" + 
			"	ResourceDelta(/P2/.classpath)[*]\n" + 
			"P1[*]: {CHILDREN}\n" + 
			"	src[*]: {REMOVED FROM CLASSPATH}\n" + 
			"	ResourceDelta(/P1/.classpath)[*]\n" + 
			"	ResourceDelta(/P1/.project)[*]\n" + 
			"	ResourceDelta(/P1/src)[-]"
		);
		assertJavaProject(
			"P1\n" + 
			"	L/P1/.classpath\n" + 
			"	L/P1/.project",
			p1);
		assertJavaProject(
			"P2\n" + 
			"	src1\n" + 
			"		[default]\n" + 
			"	src\n" + 
			"		[default]\n" + 
			"	src2\n" + 
			"		[default]\n" + 
			"	L/P2/.classpath\n" + 
			"	L/P2/.project",
			p2);
	} finally {
		this.stopDeltas();
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
		this.startDeltas();
		root.move(new Path("/P2/src"), IResource.NONE, true, null, null);
		assertDeltas(
			"Unexpected delta",
			"P2[*]: {CHILDREN}\n" + 
			"	src[+]: {}\n" + 
			"	ResourceDelta(/P2/.classpath)[*]\n" + 
			"P1[*]: {CHILDREN}\n" + 
			"	src[*]: {REMOVED FROM CLASSPATH}\n" + 
			"	ResourceDelta(/P1/.classpath)[*]\n" + 
			"	ResourceDelta(/P1/.project)[*]\n" + 
			"	ResourceDelta(/P1/src)[-]"
		);
		assertJavaProject(
			"P1\n" + 
			"	L/P1/.classpath\n" + 
			"	L/P1/.project",
			p1);
		assertJavaProject(
			"P2\n" + 
			"	src1\n" + 
			"		[default]\n" + 
			"	src2\n" + 
			"		[default]\n" + 
			"	src\n" + 
			"		[default]\n" + 
			"	L/P2/.classpath\n" + 
			"	L/P2/.project",
			p2);
	} finally {
		this.stopDeltas();
		this.deleteProject("P1");
		this.deleteProject("P2");
	}
}
}
