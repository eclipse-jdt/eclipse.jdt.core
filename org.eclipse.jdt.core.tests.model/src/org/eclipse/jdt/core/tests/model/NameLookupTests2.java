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

import java.io.IOException;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.NameLookup;

import junit.framework.Test;

/**
 * These test ensure that modifications in Java projects are correctly reported as
 * IJavaEllementDeltas.
 */
public class NameLookupTests2 extends ModifyingResourceTests {

public NameLookupTests2(String name) {
	super(name);
}

	// Use this static initializer to specify subset for tests
	// All specified tests which do not belong to the class are skipped...
	static {
//		org.eclipse.jdt.internal.core.search.matching.MatchLocator.PRINT_BUFFER = false;
//		TESTS_PREFIX =  "testArray";
//		TESTS_NAMES = new String[] { "testFindBinaryTypeWithDollarName" };
//		TESTS_NUMBERS = new int[] { 8 };
//		TESTS_RANGE = new int[] { 6, -1 };
	}

public static Test suite() {
	return buildModelTestSuite(NameLookupTests2.class);
}

NameLookup getNameLookup(JavaProject project) throws JavaModelException {
	return project.newNameLookup((WorkingCopyOwner)null);
}
public void testAddPackageFragmentRootAndPackageFrament() throws CoreException {
	try {
		IJavaProject p1 = createJavaProject("P1", new String[] {"src1"}, "bin");
		IJavaProject p2 = createJavaProject("P2", new String[] {}, "");
		IClasspathEntry[] classpath =
			new IClasspathEntry[] {
				JavaCore.newProjectEntry(new Path("/P1"))
			};
		p2.setRawClasspath(classpath, null);

		IPackageFragment[] res = getNameLookup((JavaProject)p2).findPackageFragments("p1", false);
		assertTrue("Should get no package fragment", res == null);

		IClasspathEntry[] classpath2 =
			new IClasspathEntry[] {
				JavaCore.newSourceEntry(new Path("/P1/src1")),
				JavaCore.newSourceEntry(new Path("/P1/src2"))
			};
		p1.setRawClasspath(classpath2, null);
		createFolder("/P1/src2/p1");

		res = getNameLookup((JavaProject)p2).findPackageFragments("p1", false);
		assertTrue(
			"Should get 'p1' package fragment",
			res != null &&
			res.length == 1 &&
			res[0].getElementName().equals("p1"));

	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}
public void testAddPackageFragment() throws CoreException {
	try {
		createJavaProject("P1", new String[] {"src1"}, "bin");
		IJavaProject p2 = createJavaProject("P2", new String[] {}, "");
		IClasspathEntry[] classpath =
			new IClasspathEntry[] {
				JavaCore.newProjectEntry(new Path("/P1"))
			};
		p2.setRawClasspath(classpath, null);

		IPackageFragment[] res = getNameLookup((JavaProject)p2).findPackageFragments("p1", false);
		assertTrue("Should get no package fragment", res == null);

		createFolder("/P1/src1/p1");

		res = getNameLookup((JavaProject)p2).findPackageFragments("p1", false);
		assertTrue(
			"Should get 'p1' package fragment",
			res != null &&
			res.length == 1 &&
			res[0].getElementName().equals("p1"));

	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}
/*
 * Resolve, add pkg, resolve again: new pkg should be accessible
 * (regression test for bug 37962 Unexpected transient problem during reconcile
 */
public void testAddPackageFragment2() throws CoreException {
	try {
		JavaProject project = (JavaProject)createJavaProject("P", new String[] {"src"}, "bin");
		createFolder("/P/src/p1");

		IPackageFragment[] pkgs = getNameLookup(project).findPackageFragments("p1", false);
		assertElementsEqual(
			"Didn't find p1",
			"p1 [in src [in P]]",
			pkgs);

		createFolder("/P/src/p2");

		pkgs = getNameLookup(project).findPackageFragments("p2", false);
		assertElementsEqual(
			"Didn't find p2",
			"p2 [in src [in P]]",
			pkgs);
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a NameLookup can be created with working copies that contain duplicate types
 * (regression test for bug 63245 findPackageFragment won't return default package)
 */
public void testDuplicateTypesInWorkingCopies() throws CoreException {
//	ICompilationUnit[] workingCopies = new ICompilationUnit[3];
	this.workingCopies = new ICompilationUnit[3];
	try {
		JavaProject project = (JavaProject)createJavaProject("P");
		this.workingCopies[0] = getWorkingCopy(
			"/P/X.java",
			"public class X {\n" +
			"}\n" +
			"class Other {\n" +
			"}"
		);
		this.workingCopies[1] = getWorkingCopy(
			"/P/Y.java",
			"public class Y {\n" +
			"}\n" +
			"class Other {\n" +
			"}"
		);
		this.workingCopies[2] = getWorkingCopy(
			"/P/Z.java",
			"public class Z {\n" +
			"}\n" +
			"class Other {\n" +
			"}"
		);
		NameLookup nameLookup = project.newNameLookup(this.workingCopies);
		IType type = nameLookup.findType("Other", false, NameLookup.ACCEPT_ALL); // TODO (jerome) should use seekTypes
		assertTypesEqual(
			"Unepexted types",
			"Other\n",
			new IType[] {type}
		);
	} finally {
//		discardWorkingCopies(workingCopies);
		deleteProject("P");
	}
}
/*
 * Find a default package fragment in a non-default root by its path.
 * (regression test for bug 63245 findPackageFragment won't return default package)
 */
public void testFindDefaultPackageFragmentInNonDefaultRoot() throws CoreException {
	try {
		JavaProject project = (JavaProject)createJavaProject("P", new String[] {"src"}, "bin");

		IPackageFragment pkg = getNameLookup(project).findPackageFragment(new Path("/P/src"));
		assertElementsEqual(
			"Didn't find default package",
			"<default> [in src [in P]]",
			new IJavaElement[] {pkg});

	} finally {
		deleteProject("P");
	}
}
/*
 * Creates a package fragment and finds it in a dependent project in a batch operation
 * (regression test for bug 144776 JavaProject.resetCaches() needs to reset dependent projects
 */
public void testNameLookupFindPackageFragmentAfterCreation() throws CoreException {
	try {
		final IJavaProject p1 = createJavaProject("P1");
		final JavaProject p2 = (JavaProject) createJavaProject("P2", new String[] {""}, new String[0], new String[] {"/P1"}, "");

		// populate namelookup for p2
		getNameLookup(p2);

		IWorkspaceRunnable runnable = new IWorkspaceRunnable(){
			public void run(IProgressMonitor monitor) throws CoreException {
				p1.getPackageFragmentRoot(p1.getProject()).createPackageFragment("pkg", false/*don't force*/, monitor);
				IPackageFragment[] pkgs = getNameLookup(p2).findPackageFragments("pkg", false/*exact match*/);
				assertElementsEqual(
					"Unexpected package fragments",
					"pkg [in <project root> [in P1]]",
					pkgs);
			}
		};
		JavaCore.run(runnable, null/*no progress*/);
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}
/*
 * Ensure that finding a package fragment with a working copy opened returns one element only
 * (regression test for bug 89624 Open on selection proposes twice the same entry)
 */
public void testFindPackageFragmentWithWorkingCopy() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	try {
		JavaProject project = (JavaProject)createJavaProject("P");
		createFolder("/P/p1");
		this.workingCopies[0] = getWorkingCopy(
			"/P/p1/X.java",
			"package p1;\n" +
			"public class X {\n" +
			"}"
		);
		NameLookup nameLookup = project.newNameLookup(this.workingCopies);
		IJavaElement[] pkgs = nameLookup.findPackageFragments("p1", false/*not a partial match*/);
		assertElementsEqual(
			"Unexpected packages",
			"p1 [in <project root> [in P]]",
			pkgs);
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensure that a package fragment with a path with a length equals to an external jar path length + 1
 * is not found
 * (regression test for bug 266771 NameLookup.findPackageFragment returns very incorrect package fragments)
 */
public void testFindPackageFragment2() throws CoreException {
	try {
		JavaProject project = (JavaProject)createJavaProject("P", new String[0], new String[] {"JCL_LIB"}, "bin");
		NameLookup nameLookup =getNameLookup(project);
		IPath pathToSearch = new Path(getExternalJCLPathString() + 'a');
		IPackageFragment pkg = nameLookup.findPackageFragment(pathToSearch);
		assertElementEquals(
			"Unexpected package",
			"<null>",
			pkg);
	} finally {
		deleteProject("P");
	}
}

/*
 * Ensure that a member type with a name ending with a dollar and a number is found
 * (regression test for bug 103466 Stack Overflow: Requesting Java AST from selection)
 */
public void testFindBinaryTypeWithDollarName() throws CoreException, IOException {
	try {
		IJavaProject project = createJavaProject("P");
		addLibrary(project, "lib.jar", "libsrc.zip",
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"  public class $1 {\n" +
				"    public class $2 {\n" +
				"    }\n" +
				"  }\n" +
				"}"
			},
			"1.4");
		IType type = getNameLookup((JavaProject) project).findType("p.X$$1", false, NameLookup.ACCEPT_ALL);
		assertTypesEqual(
			"Unexpected type",
			"p.X$$1\n",
			new IType[] {type});
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensure that a type with the same simple name as its member type is found
 * (regression test for bug 102286 Error when trying F4-Type Hierarchy)
 */
public void testFindBinaryTypeWithSameNameAsMember() throws CoreException, IOException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {}, new String[] {"/P/lib"}, new String[] {}, "bin");
		createFolder("/P/lib/p");
		createFile("/P/lib/p/X.class", "");
		createFile("/P/lib/p/X$X.class", "");
		IType type = getNameLookup((JavaProject) project).findType("p.X", false, NameLookup.ACCEPT_ALL);
		assertTypesEqual(
			"Unexpected type",
			"p.X\n",
			new IType[] {type});
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a type in working copy opened in an unrelated project is not found
 * (regression test for bug 169970 [model] code assist favorites must honour build path of project in context)
 */
public void testFindTypeWithUnrelatedWorkingCopy() throws Exception {
	ICompilationUnit workingCopy = null;
	try {
		createJavaProject("P1");
		createFolder("/P1/p");
		createFile(
			"/P1/p/X.java",
			"package p;\n" +
			"public class X {\n" +
			"}"
		);
		workingCopy = getCompilationUnit("/P1/p/X.java");
		workingCopy.becomeWorkingCopy(null);
		IJavaProject p2 = createJavaProject("P2");
		NameLookup nameLookup = ((JavaProject) p2).newNameLookup(DefaultWorkingCopyOwner.PRIMARY);
		IType type = nameLookup.findType("p.X", false, NameLookup.ACCEPT_ALL);
		assertNull("Should not find p.X in P2", type);
	} finally {
		if (workingCopy != null)
			workingCopy.discardWorkingCopy();
		deleteProject("P1");
		deleteProject("P2");
	}
}
}

