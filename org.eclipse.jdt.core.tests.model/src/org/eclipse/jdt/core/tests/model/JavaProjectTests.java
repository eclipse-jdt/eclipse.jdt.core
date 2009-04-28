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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.PackageFragmentRoot;
import org.eclipse.jdt.internal.core.util.Util;

public class JavaProjectTests extends ModifyingResourceTests {
public JavaProjectTests(String name) {
	super(name);
}
public static Test suite() {
	TestSuite suite = (TestSuite) buildModelTestSuite(JavaProjectTests.class);

	// The following test must be at the end as it deletes a package and this would have side effects
	// on other tests
	if (suite.testCount() > 1) // if not running only 1 test
		suite.addTest(new JavaProjectTests("lastlyTestDeletePackageWithAutobuild"));

	return suite;
}
public void setUpSuite() throws Exception {
	super.setUpSuite();
	setUpJavaProject("JavaProjectTests");
	setUpJavaProject("JavaProjectSrcTests");
	setUpJavaProject("JavaProjectLibTests");
}
public void tearDownSuite() throws Exception {
	deleteProject("JavaProjectTests");
	deleteProject("JavaProjectSrcTests");
	deleteProject("JavaProjectLibTests");
	super.tearDownSuite();
}

/*
 * Ensures that adding a library entry for an existing empty external library folder updates the model
 */
public void testAddExternalLibFolder1() throws CoreException {
	try {
		IJavaProject p = createJavaProject("P");
		createExternalFolder("externalLib");
		setClasspath(p, new IClasspathEntry[] {JavaCore.newLibraryEntry(new Path(getExternalResourcePath("externalLib")), null, null)});
		assertElementDescendants(
			"Unexpected project content",
			"P\n" +
			"  "+ getExternalPath() + "externalLib\n" +
			"    <default> (...)",
			p
		);
	} finally {
		deleteExternalResource("externalLib");
		deleteProject("P");
	}
}

/*
 * Ensures that adding a library entry for an non-existing external library folder updates the model
 */
public void testAddExternalLibFolder2() throws CoreException {
	try {
		IJavaProject p = createJavaProject("P");
		IPath path = new Path(getExternalResourcePath("externalLib"));
		setClasspath(p, new IClasspathEntry[] {JavaCore.newLibraryEntry(path, null, null)});
		assertElementDescendants(
			"Unexpected project content",
			"P",
			p
		);
	} finally {
		deleteProject("P");
	}
}

/*
 * Ensures that adding a library entry for an existing non-empty external library folder updates the model
 */
public void testAddExternalLibFolder3() throws CoreException, IOException {
	try {
		IJavaProject p = createJavaProject("P");
		createExternalFolder("externalLib/p");
		createExternalFile("externalLib/p/X.class", "");
		setClasspath(p, new IClasspathEntry[] {JavaCore.newLibraryEntry(new Path(getExternalResourcePath("externalLib")), null, null)});
		assertElementDescendants(
			"Unexpected project content",
			"P\n" +
			"  "+ getExternalPath() + "externalLib\n" +
			"    <default> (...)\n" +
			"    p (...)\n" +
			"      X.class",
			p
		);
	} finally {
		deleteExternalResource("externalLib");
		deleteProject("P");
	}
}

/*
 * Ensures that creating an external library folder referenced by a library entry and refreshing updates the model
 */
public void testAddExternalLibFolder4() throws CoreException {
	try {
		IJavaProject p = createJavaProject("P", new String[0], new String[] {getExternalResourcePath("externalLib")}, "");
		expandAll(p);
		createExternalFolder("externalLib");
		refresh(p);
		assertElementDescendants(
			"Unexpected project content",
			"P\n" +
			"  "+ getExternalPath() + "externalLib\n" +
			"    <default> (...)",
			p
		);
	} finally {
		deleteExternalResource("externalLib");
		deleteProject("P");
	}
}

/*
 * Ensures that importing a Java project with a library entry for an existing empty external library folder after restart
 * updates the model
 */
public void testAddExternalLibFolder5() throws CoreException {
	try {
		simulateExitRestart();
		createExternalFolder("externalLib/p");
		createExternalFile("externalLib/p/X.class", "");
		IJavaProject p = importJavaProject("P", new String[0], new String[] {getExternalResourcePath("externalLib")}, "");
		waitForAutoBuild(); // since the project is imported, the linked folder can only be created by auto-build
		assertElementDescendants(
			"Unexpected project content",
			"P\n" +
			"  "+ getExternalPath() + "externalLib\n" +
			"    <default> (...)\n" +
			"    p (...)\n" +
			"      X.class",
			p
		);
	} finally {
		deleteExternalResource("externalLib");
		deleteProject("P");
	}
}

/*
 * Ensures that adding a library entry for an existing empty external ZIP archive updates the model
 */
public void testAddZIPArchive1() throws Exception {
	try {
		IJavaProject p = createJavaProject("P");
		createExternalFile("externalLib.abc", "");

		setClasspath(p, new IClasspathEntry[] {JavaCore.newLibraryEntry(new Path(getExternalResourcePath("externalLib.abc")), null, null)});
		assertElementDescendants(
			"Unexpected project content",
			"P\n" +
			"  "+ getExternalPath() + "externalLib.abc",
			p
		);
	} finally {
		deleteExternalResource("externalLib.abc");
		deleteProject("P");
	}
}

/*
 * Ensures that adding a library entry for an non-existing external ZIP archive updates the model
 */
public void testAddZIPArchive2() throws CoreException {
	try {
		IJavaProject p = createJavaProject("P");
		refreshExternalArchives(p);

		setClasspath(p, new IClasspathEntry[] {JavaCore.newLibraryEntry(new Path(getExternalResourcePath("externalLib.abc")), null, null)});
		assertElementDescendants(
			"Unexpected project content",
			"P",
			p
		);
	} finally {
		deleteProject("P");
	}
}

/*
 * Ensures that adding a library entry for an existing non-empty external ZIP archive updates the model
 */
public void testAddZIPArchive3() throws CoreException, IOException {
	try {
		IJavaProject p = createJavaProject("P");
		createJar(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"}"
			},
			getExternalResourcePath("externalLib.abc"));
		setClasspath(p, new IClasspathEntry[] {JavaCore.newLibraryEntry(new Path(getExternalResourcePath("externalLib.abc")), null, null)});
		assertElementDescendants(
			"Unexpected project content",
			"P\n" +
			"  "+ getExternalPath() + "externalLib.abc\n" +
			"    <default> (...)\n" +
			"    p (...)\n" +
			"      X.class\n" +
			"        class X\n" +
			"          X()",
			p
		);
	} finally {
		deleteExternalResource("externalLib.abc");
		deleteProject("P");
	}
}

/*
 * Ensures that creating an external ZIP archive referenced by a library entry and refreshing updates the model
 */
public void testAddZIPArchive4() throws Exception {
	try {
		IJavaProject p = createJavaProject("P", new String[0], new String[] {getExternalResourcePath("externalLib.abc")}, "");
		refreshExternalArchives(p);
		expandAll(p);

		createExternalFile("externalLib.abc", "");
		refreshExternalArchives(p);
		assertElementDescendants(
			"Unexpected project content",
			"P\n" +
			"  "+ getExternalPath() + "externalLib.abc",
			p
		);
	} finally {
		deleteExternalResource("externalLib.abc");
		deleteProject("P");
	}
}

/*
 * Ensures that importing a Java project with a library entry for an existing empty external ZIP archive after restart
 * updates the model
 */
public void testAddZIPArchive5() throws Exception {
	try {
		simulateExitRestart();
		createJar(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"}"
			},
			getExternalResourcePath("externalLib.abc"));
		IJavaProject p = importJavaProject("P", new String[0], new String[] {getExternalResourcePath("externalLib.abc")}, "");
		assertElementDescendants(
			"Unexpected project content",
			"P\n" +
			"  "+ getExternalPath() + "externalLib.abc\n" +
			"    <default> (...)\n" +
			"    p (...)\n" +
			"      X.class\n" +
			"        class X\n" +
			"          X()",
			p
		);
	} finally {
		deleteExternalResource("externalLib.abc");
		deleteProject("P");
	}
}

/*
 * Ensures that adding a library entry for an existing empty internal ZIP archive updates the model
 */
public void testAddZIPArchive6() throws Exception {
	try {
		IJavaProject p = createJavaProject("P");
		createFile("/P/internalLib.abc", "");
		setClasspath(p, new IClasspathEntry[] {JavaCore.newLibraryEntry(new Path("/P/internalLib.abc"), null, null)});
		assertElementDescendants(
			"Unexpected project content",
			"P\n" +
			"  internalLib.abc",
			p
		);
	} finally {
		deleteProject("P");
	}
}

/**
 * Test adding a non-java resource in a package fragment root that correspond to
 * the project.
 * (Regression test for PR #1G58NB8)
 */
public void testAddNonJavaResourcePackageFragmentRoot() throws JavaModelException, CoreException {
	// get resources of source package fragment root at project level
	IPackageFragmentRoot root = getPackageFragmentRoot("JavaProjectTests", "");
	Object[] resources = root.getNonJavaResources();
	assertResourceNamesEqual(
		"unexpected non Java resources",
		".classpath\n" +
		".project\n" +
		".settings",
		resources);
	IFile resource = (IFile)resources[0];
	IPath newPath = root.getUnderlyingResource().getFullPath().append("TestNonJavaResource.abc");
	try {
		// copy and rename resource
		resource.copy(
			newPath,
			true,
			null);

		// ensure the new resource is present
		resources = root.getNonJavaResources();
		assertResourcesEqual(
			"incorrect non java resources",
			"/JavaProjectTests/.classpath\n" +
			"/JavaProjectTests/.project\n" +
			"/JavaProjectTests/.settings\n" +
			"/JavaProjectTests/TestNonJavaResource.abc",
			resources);
	} finally {
		// clean up
		deleteResource(resource.getWorkspace().getRoot().getFile(newPath));
	}
}
/*
 * Ensures that adding a project prerequisite in the classpath updates the referenced projects
 */
public void testAddProjectPrerequisite() throws CoreException {
	try {
		createJavaProject("P1");
		createJavaProject("P2");
		waitForAutoBuild();
		editFile(
			"/P2/.classpath",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"    <classpathentry kind=\"src\" path=\"/P1\"/>\n" +
			"    <classpathentry kind=\"output\" path=\"\"/>\n" +
			"</classpath>"
		);
		waitForAutoBuild();
		IProject[] referencedProjects = getProject("P2").getReferencedProjects();
		assertResourcesEqual(
			"Unexpected project references",
			"/P1",
			referencedProjects);
	} finally {
		deleteProjects(new String[] {"P1", "P2"});
	}
}
/**
 * Test that a class file in a jar has no corresponding resource.
 */
public void testArchiveClassFileCorrespondingResource() throws JavaModelException {
	IPackageFragmentRoot root = getPackageFragmentRoot("JavaProjectTests", "lib.jar");
	IPackageFragment element = root.getPackageFragment("p");
	IClassFile cf= element.getClassFile("X.class");
	IResource corr = cf.getCorrespondingResource();
	assertTrue("incorrect corresponding resource", corr == null);
}
/**
 * Test that a binary type
 * has a corresponding resource.
 */
public void testBinaryTypeCorrespondingResource() throws CoreException {
	IClassFile element= getClassFile("/JavaProjectLibTests/lib/p/Y.class");
	IType type= element.getType();
	IResource corr= type.getCorrespondingResource();
	assertTrue("incorrect corresponding resource", corr == null);
}

/*
 * Ensures that changing the content of an external library folder and refreshing updates the model
 */
public void testChangeExternalLibFolder1() throws CoreException, IOException {
	try {
		createExternalFolder("externalLib");
		IJavaProject p = createJavaProject("P", new String[0], new String[] {getExternalResourcePath("externalLib")}, "bin");
		expandAll(p);

		createExternalFolder("externalLib/p");
		createExternalFile("externalLib/p/X.class", "");
		refresh(p);
		assertElementDescendants(
			"Unexpected project content",
			"P\n" +
			"  "+ getExternalPath() + "externalLib\n" +
			"    <default> (...)\n" +
			"    p (...)\n" +
			"      X.class",
			p
		);
	} finally {
		deleteExternalResource("externalLib");
		deleteProject("P");
	}
}

/*
 * Ensures that changing the content of an external library folder and refreshing updates the model
 */
public void testChangeExternalLibFolder2() throws CoreException, IOException {
	try {
		createExternalFolder("externalLib/p");
		createExternalFile("externalLib/p/X.class", "");
		IJavaProject p = createJavaProject("P", new String[0], new String[] {getExternalResourcePath("externalLib")}, "bin");
		expandAll(p);

		deleteExternalResource("externalLib/p");
		refresh(p);
		assertElementDescendants(
			"Unexpected project content",
			"P\n" +
			"  "+ getExternalPath() + "externalLib\n" +
			"    <default> (...)",
			p
		);
	} finally {
		deleteExternalResource("externalLib");
		deleteProject("P");
	}
}

/*
 * Ensures that changing the content of an external ZIP archive and refreshing updates the model
 */
public void testChangeZIPArchive1() throws CoreException, IOException {
	try {
		createExternalFile("externalLib.abc", "");
		IJavaProject p = createJavaProject("P", new String[0], new String[] {getExternalResourcePath("externalLib.abc")}, "bin");
		refreshExternalArchives(p);
		expandAll(p);

		createJar(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"}"
			},
			getExternalResourcePath("externalLib.abc"));
		refreshExternalArchives(p);
		assertElementDescendants(
			"Unexpected project content",
			"P\n" +
			"  "+ getExternalPath() + "externalLib.abc\n" +
			"    <default> (...)\n" +
			"    p (...)\n" +
			"      X.class\n" +
			"        class X\n" +
			"          X()",
			p
		);
	} finally {
		deleteExternalResource("externalLib.abc");
		deleteProject("P");
	}
}

/*
 * Ensures that changing the content of an external ZIP archive and refreshing updates the model
 */
public void testChangeZIPArchive2() throws CoreException, IOException {
	try {
		createJar(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"}"
			},
			getExternalResourcePath("externalLib.abc"));
		IJavaProject p = createJavaProject("P", new String[0], new String[] {getExternalResourcePath("externalLib.abc")}, "bin");
		refreshExternalArchives(p);
		expandAll(p);

		createJar(
			new String[] {
				"p2/X.java",
				"package p2;\n" +
				"public class X {\n" +
				"}"
			},
			getExternalResourcePath("externalLib.abc"));
		refreshExternalArchives(p);
		assertElementDescendants(
			"Unexpected project content",
			"P\n" +
			"  "+ getExternalPath() + "externalLib.abc\n" +
			"    <default> (...)\n" +
			"    p2 (...)\n" +
			"      X.class\n" +
			"        class X\n" +
			"          X()",
			p
		);
	} finally {
		deleteExternalResource("externalLib.abc");
		deleteProject("P");
	}
}

/*
 * Ensures that changing the content of an internal ZIP archive and refreshing updates the model
 */
public void testChangeZIPArchive3() throws CoreException, IOException {
	try {
		IJavaProject p = createJavaProject("P", new String[0], new String[] {"/P/internalLib.abc"}, "bin");
		IFile lib = createFile("/P/internalLib.abc", "");
		expandAll(p);

		createJar(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"}"
			},
			lib.getLocation().toOSString());
		p.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
		assertElementDescendants(
			"Unexpected project content",
			"P\n" +
			"  internalLib.abc\n" +
			"    <default> (...)\n" +
			"    p (...)\n" +
			"      X.class\n" +
			"        class X\n" +
			"          X()",
			p
		);
	} finally {
		deleteProject("P");
	}
}

/**
 * When the output location is changed, package fragments can be added/removed
 */
public void testChangeOutputLocation() throws JavaModelException, CoreException {
	IJavaProject project= getJavaProject("JavaProjectTests");
	IContainer underLyingResource = (IContainer)project.getUnderlyingResource();
	IFolder folder= underLyingResource.getFolder(new Path("output"));

	try {
		startDeltas();
		project.setOutputLocation(folder.getFullPath(), null);
		assertDeltas(
			"Unexpected delta 1",
			"JavaProjectTests[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
			"	<project root>[*]: {CHILDREN}\n" +
			"		bin[+]: {}\n" +
			"	ResourceDelta(/JavaProjectTests/.classpath)[*]"
		);
	} finally {
		stopDeltas();
		try {
			startDeltas();
			folder= underLyingResource.getFolder(new Path("bin"));
			project.setOutputLocation(folder.getFullPath(), null);
			assertDeltas(
				"Unexpected delta 2",
				"JavaProjectTests[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
				"	<project root>[*]: {CHILDREN}\n" +
				"		bin[-]: {}\n" +
				"	ResourceDelta(/JavaProjectTests/.classpath)[*]"
			);
		} finally {
			stopDeltas();
		}
	}
}
/**
 * Test that a class file
 * has a corresponding resource.
 */
public void testClassFileCorrespondingResource() throws JavaModelException {
	IClassFile element= getClassFile("JavaProjectLibTests", "lib", "p", "Y.class");
	IResource corr= element.getCorrespondingResource();
	IResource res= getWorkspace().getRoot().getProject("JavaProjectLibTests").getFolder("lib").getFolder("p").getFile("Y.class");
	assertTrue("incorrect corresponding resource", corr.equals(res));
}
/**
 * Test that a compilation unit
 * has a corresponding resource.
 */
public void testCompilationUnitCorrespondingResource() throws JavaModelException {
	ICompilationUnit element= getCompilationUnit("JavaProjectTests", "", "q", "A.java");
	IResource corr= element.getCorrespondingResource();
	IResource res= getWorkspace().getRoot().getProject("JavaProjectTests").getFolder("q").getFile("A.java");
	assertTrue("incorrect corresponding resource", corr.equals(res));
	assertEquals("Project is incorrect for the compilation unit", "JavaProjectTests", corr.getProject().getName());
}
/**
 * Tests the fix for "1FWNMKD: ITPJCORE:ALL - Package Fragment Removal not reported correctly"
 */
public void lastlyTestDeletePackageWithAutobuild() throws CoreException {
	// close all project except JavaProjectTests so as to avoid side effects while autobuilding
	IProject[] projects = getWorkspaceRoot().getProjects();
	for (int i = 0; i < projects.length; i++) {
		IProject project = projects[i];
		if (project.getName().equals("JavaProjectTests")) continue;
		project.close(null);
	}

	// turn autobuilding on
	IWorkspace workspace = getWorkspace();
	boolean autoBuild = workspace.isAutoBuilding();
	IWorkspaceDescription description = workspace.getDescription();
	description.setAutoBuilding(true);
	workspace.setDescription(description);

	startDeltas();
	IPackageFragment frag = getPackageFragment("JavaProjectTests", "", "x.y");
	IFolder folder = (IFolder) frag.getUnderlyingResource();
	try {
		deleteResource(folder);
		assertDeltas(
			"Unexpected delta",
			"JavaProjectTests[*]: {CHILDREN}\n" +
			"	<project root>[*]: {CHILDREN}\n" +
			"		x.y[-]: {}"
		);
	} finally {
		stopDeltas();

		// turn autobuild off
		description.setAutoBuilding(autoBuild);
		workspace.setDescription(description);

		// reopen projects
		projects = getWorkspaceRoot().getProjects();
		for (int i = 0; i < projects.length; i++) {
			IProject project = projects[i];
			if (project.getName().equals("JavaProjectTests")) continue;
			project.open(null);
		}
	}
}
/**
 * Test that an (external) jar
 * has no corresponding resource.
 */
public void testExternalArchiveCorrespondingResource() throws JavaModelException {
	IJavaProject project= getJavaProject("JavaProjectTests");
	IPackageFragmentRoot element= project.getPackageFragmentRoot(getExternalJCLPathString());
	IResource corr= element.getCorrespondingResource();
	assertTrue("incorrect corresponding resource", corr == null);
}
/*
 * Ensures that a file with an extra Java-like extension is listed in the children of a package.
 */
public void testExtraJavaLikeExtension1() throws CoreException {
	try {
		createJavaProject("P");
		createFolder("/P/pack");
		createFile("/P/pack/X.java", "package pack; public class X {}");
		createFile("/P/pack/Y.bar", "package pack; public class Y {}");
		IPackageFragment pkg = getPackage("/P/pack");
		assertSortedElementsEqual(
			"Unexpected children of package pack",
			"X.java [in pack [in <project root> [in P]]]\n" +
			"Y.bar [in pack [in <project root> [in P]]]",
			pkg.getChildren());
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a file with an extra Java-like extension is not listed in the non-Java resources of a package.
 */
public void testExtraJavaLikeExtension2() throws CoreException {
	try {
		createJavaProject("P");
		createFolder("/P/pack");
		createFile("/P/pack/X.txt", "");
		createFile("/P/pack/Y.bar", "package pack; public class Y {}");
		IPackageFragment pkg = getPackage("/P/pack");
		assertResourceNamesEqual(
			"Unexpected non-Java resources of package pack",
			"X.txt",
			pkg.getNonJavaResources());
	} finally {
		deleteProject("P");
	}
}
/**
 * Test that a compilation unit can be found for a binary type
 */
public void testFindElementClassFile() throws JavaModelException {
	IJavaProject project= getJavaProject("JavaProjectTests");
	IJavaElement element= project.findElement(new Path("java/lang/Object.java"));
	assertTrue("CU not found" , element != null && element.getElementType() == IJavaElement.CLASS_FILE
		&& element.getElementName().equals("Object.class"));
}
/**
 * Test that a compilation unit can be found
 */
public void testFindElementCompilationUnit() throws JavaModelException {
	IJavaProject project= getJavaProject("JavaProjectTests");
	IJavaElement element= project.findElement(new Path("x/y/Main.java"));
	assertTrue("CU not found" , element != null && element.getElementType() == IJavaElement.COMPILATION_UNIT
		&& element.getElementName().equals("Main.java"));
}
/**
 * Test that a compilation unit can be found in a default package
 */
public void testFindElementCompilationUnitDefaultPackage() throws JavaModelException {
	IJavaProject project= getJavaProject("JavaProjectTests");
	IJavaElement element= project.findElement(new Path("B.java"));
	assertTrue("CU not found" , element != null && element.getElementType() == IJavaElement.COMPILATION_UNIT
		&& element.getElementName().equals("B.java"));
}
/**
 * Test that an invlaid path throws an exception
 */
public void testFindElementInvalidPath() throws JavaModelException {
	IJavaProject project= getJavaProject("JavaProjectTests");
	boolean failed= false;
	try {
		project.findElement(null);
	} catch (JavaModelException e) {
		failed= true;
		assertTrue("wrong status code" , e.getStatus().getCode() == IJavaModelStatusConstants.INVALID_PATH);
	}
	assertTrue("Shold have failed", failed);

	failed = false;
	try {
		project.findElement(new Path("/something/absolute"));
	} catch (JavaModelException e) {
		failed= true;
		assertTrue("wrong status code" , e.getStatus().getCode() == IJavaModelStatusConstants.INVALID_PATH);
	}
	assertTrue("Shold have failed", failed);

	IJavaElement element= project.findElement(new Path("does/not/exist/HelloWorld.java"));
	assertTrue("should get no element", element == null);
}
/**
 * Test that a package can be found
 */
public void testFindElementPackage() throws JavaModelException {
	IJavaProject project= getJavaProject("JavaProjectTests");
	IJavaElement element= project.findElement(new Path("x/y"));
	assertTrue("package not found" , element != null && element.getElementType() == IJavaElement.PACKAGE_FRAGMENT
		&& element.getElementName().equals("x.y"));
}
/**
 * Test that a class can be found even if the project prereq a simple project
 * (regression test for bug 28434 Open Type broken when workspace has build path problems)
 */
public void testFindElementPrereqSimpleProject() throws CoreException {
	try {
		createProject("R");
		IJavaProject project = this.createJavaProject("J", new String[] {"src"}, new String[] {}, new String[] {"/R"}, "bin");
		this.createFile(
			"J/src/X.java",
			"public class X {\n" +
			"}"
		);
		assertTrue("X.java not found", project.findElement(new Path("X.java")) != null);
	} finally {
		this.deleteProject("R");
		this.deleteProject("J");
	}
}
/**
 * Test that a package fragment root can be found from a classpath entry.
 */
public void testFindPackageFragmentRootFromClasspathEntry() {
	IJavaProject project = getJavaProject("JavaProjectTests");

	// existing classpath entry
	IClasspathEntry entry = JavaCore.newLibraryEntry(new Path("/JavaProjectTests/lib.jar"), null, null);
	IPackageFragmentRoot[] roots = project.findPackageFragmentRoots(entry);
	assertEquals("Unexpected number of roots for existing entry", 1, roots.length);
	assertEquals("Unexpected root", "/JavaProjectTests/lib.jar", roots[0].getPath().toString());

	// non-existing classpath entry
	entry = JavaCore.newSourceEntry(new Path("/JavaProjectTests/nonExisting"));
	roots = project.findPackageFragmentRoots(entry);
	assertEquals("Unexpected number of roots for non existing entry", 0, roots.length);

}
/*
 * Ensures that a type can be found if run after setting the classpath in a runnable
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=212769 )
 */
public void testFindTypeAfterSetClasspath() throws CoreException {
	try {
		final IJavaProject project = createJavaProject("P", new String[] {"src1"}, "bin");
		createFolder("/P/src2/p");
		createFile("/P/src2/p/X.java", "package p; public class X {}");
		project.findType("p.X"); // populate project's cache
		final IType[] result = new IType[1];
		ResourcesPlugin.getWorkspace().run(
			new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					addClasspathEntry(project, JavaCore.newSourceEntry(new Path("/P/src2")));
					result[0] = project.findType("p.X");
				}
			},
			null);
		assertElementsEqual(
			"Unexpected type found",
			"X [in X.java [in p [in src2 [in P]]]]",
			result);
	} finally {
		deleteProject("P");
	}
}
/**
 * Test that a folder with a dot name does not relate to a package fragment
 */
public void testFolderWithDotName() throws JavaModelException, CoreException {
	IPackageFragmentRoot root= getPackageFragmentRoot("JavaProjectTests", "");
	IContainer folder= (IContainer)root.getCorrespondingResource();
	try {
		startDeltas();
		folder.getFolder(new Path("org.eclipse")).create(false, true, null);
		assertDeltas(
			"Unexpected delta", 
			"JavaProjectTests[*]: {CONTENT}\n" + 
			"	ResourceDelta(/JavaProjectTests/org.eclipse)[+]"
		);
		stopDeltas();

		IJavaElement[] children = root.getChildren();
		IPackageFragment bogus = root.getPackageFragment("org.eclipse");
		for (int i = 0; i < children.length; i++) {
			assertTrue("org.eclipse should not be present as child", !children[i].equals(bogus));
		}
		assertTrue("org.eclipse should not exist", !bogus.exists());
	} finally {
		deleteResource(folder.getFolder(new Path("org.eclipse")));
	}
}
/*
 * Ensures that getting the classpath on a closed project throws a JavaModelException
 * (regression test for bug 25358 Creating a new Java class - Browse for parent)
 */
public void testGetClasspathOnClosedProject() throws CoreException {
	IProject project = getProject("JavaProjectTests");
	try {
		project.close(null);
		boolean gotException = false;
		IJavaProject javaProject = JavaCore.create(project);
		try {
			javaProject.getRawClasspath();
		} catch (JavaModelException e) {
			if (e.isDoesNotExist()) {
				gotException = true;
			}
		}
		assertTrue("Should get a not present exception for getRawClasspath()", gotException);
		gotException = false;
		try {
			javaProject.getResolvedClasspath(true);
		} catch (JavaModelException e) {
			if (e.isDoesNotExist()) {
				gotException = true;
			}
		}
		assertTrue("Should get a not present exception for getResolvedClasspath(true)", gotException);
	} finally {
		project.open(null);
	}
}
/*
 * Ensures that the non-java resources for a project do not contain the project output location.
 */
public void testGetNonJavaResources1() throws CoreException {
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {"src"}, "bin");
		assertResourcesEqual(
			"Unexpected non-java resources for project",
			"/P/.classpath\n" +
			"/P/.project",
			project.getNonJavaResources());
	} finally {
		this.deleteProject("P");
	}
}
/*
 * Ensures that the non-java resources for a project do not contain a custom output location.
 * (regression test for 27494  Source folder output folder shown in Package explorer)
 */
public void testGetNonJavaResources2() throws CoreException {
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {"src"}, "bin1", new String[] {"bin2"});
		assertResourcesEqual(
			"Unexpected non-java resources for project",
			"/P/.classpath\n" +
			"/P/.project",
			project.getNonJavaResources());
	} finally {
		this.deleteProject("P");
	}
}
/*
 * Ensures that the non-java resources for a project do not contain a folder that should be a package fragment.
 */
public void testGetNonJavaResources3() throws CoreException {
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {""}, "");
		this.createFolder("/P/p1");
		assertResourcesEqual(
			"Unexpected non-java resources for project",
			"/P/.classpath\n" +
			"/P/.project",
			project.getNonJavaResources());
	} finally {
		this.deleteProject("P");
	}
}
/*
 * Ensures that the non-java resources for a project contain a folder that have an invalid name for a package fragment.
 * (regression test for bug 31757 Folder with invalid pkg name should be non-Java resource)
 */
public void testGetNonJavaResources4() throws CoreException {
	try {
		IJavaProject project = this.createJavaProject("P");
		this.createFolder("/P/x.y");
		assertResourcesEqual(
			"Unexpected non-java resources for project",
			"/P/.classpath\n" +
			"/P/.project\n" +
			"/P/x.y",
			project.getNonJavaResources());
	} finally {
		this.deleteProject("P");
	}
}
/*
 * Ensures that an internal jar referred to with its OS-path is not part of the non-Java resources
 */
public void testGetNonJavaResources5() throws CoreException {
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {"src"}, "bin");
		IFile file = createFile("/P/lib.jar", "");
		addLibraryEntry(project, file.getLocation(), false/*not exported*/);
		assertResourcesEqual(
			"Unexpected non-java resources for project",
			"/P/.classpath\n" + 
			"/P/.project",
			project.getNonJavaResources());
	} finally {
		this.deleteProject("P");
	}
}
/*
 * Ensures that getRequiredProjectNames() returns the project names in the classpath order
 * (regression test for bug 25605 [API] someJavaProject.getRequiredProjectNames(); API should specify that the array is returned in ClassPath order)
 */
public void testGetRequiredProjectNames() throws CoreException {
	try {
		IJavaProject project = this.createJavaProject(
			"P",
			new String[] {},
			new String[] {},
			new String[] {"/JavaProjectTests", "/P1", "/P0", "/P2", "/JavaProjectSrcTests"},
			"");
		String[] requiredProjectNames = project.getRequiredProjectNames();
		StringBuffer buffer = new StringBuffer();
		for (int i = 0, length = requiredProjectNames.length; i < length; i++) {
			buffer.append(requiredProjectNames[i]);
			if (i != length-1) {
				buffer.append(", ");
			}
		}
		assertEquals(
			"Unexpected required project names",
			"JavaProjectTests, P1, P0, P2, JavaProjectSrcTests",
			buffer.toString());
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Test that an (internal) jar
 * has a corresponding resource.
 */
public void testInternalArchiveCorrespondingResource() throws JavaModelException {
	IPackageFragmentRoot element= getPackageFragmentRoot("JavaProjectTests", "lib.jar");
	IResource corr= element.getCorrespondingResource();
	IResource res= getWorkspace().getRoot().getProject("JavaProjectTests").getFile("lib.jar");
	assertTrue("incorrect corresponding resource", corr.equals(res));
}
/**
 * Test IJavaPackageFragment.isDefaultPackage().
 */
public void testIsDefaultPackage() throws JavaModelException {
	IPackageFragment def = getPackageFragment("JavaProjectTests", "", "");
	assertTrue("should be default package", def.isDefaultPackage());
	IPackageFragment y =
		getPackageFragment("JavaProjectTests", "", "x.y");
	assertTrue("x.y should not be default pakackage", !y.isDefaultPackage());

	IPackageFragment def2 = getPackageFragment("JavaProjectTests", "lib.jar", "");
	assertTrue("lib.jar should have default package", def2.isDefaultPackage());
	IPackageFragment p =
		getPackageFragment("JavaProjectTests", "lib.jar", "p");
	assertTrue("p should not be default package", !p.isDefaultPackage());
}
/**
 * Test that a package fragment in a jar has no corresponding resource.
 */
public void testJarPackageFragmentCorrespondingResource() throws JavaModelException {
	IPackageFragmentRoot root = getPackageFragmentRoot("JavaProjectTests", "lib.jar");
	IPackageFragment element = root.getPackageFragment("p");
	IResource corr = element.getCorrespondingResource();
	assertTrue("incorrect corresponding resource", corr == null);
}
/**
 * Test that an output location can't be set to a location inside a package fragment
 * root, except the root project folder.
 */
public void testOutputLocationNestedInRoot() throws JavaModelException, CoreException {
	IPackageFragmentRoot root= getPackageFragmentRoot("JavaProjectSrcTests", "src");
	IFolder folder= (IFolder) root.getUnderlyingResource();
	IJavaProject project= getJavaProject("JavaProjectSrcTests");
	folder= folder.getFolder("x");
	boolean failed= false;
	try {
		project.setOutputLocation(folder.getFullPath(), null);
	} catch (JavaModelException e) {
		assertTrue("should be an invalid classpath", e.getStatus().getCode() == IJavaModelStatusConstants.INVALID_CLASSPATH);
		failed= true;
	}
	assertTrue("should have failed", failed);

}
/**
 * Test that an output location folder is not created as a package fragment.
 */
public void testOutputLocationNotAddedAsPackageFragment() throws JavaModelException, CoreException {
	IPackageFragmentRoot root= getPackageFragmentRoot("JavaProjectTests", "");
	IJavaElement[] packages= root.getChildren();
	assertElementsEqual(
		"unexpected package fragments in source folder",
		"<default> [in <project root> [in JavaProjectTests]]\n" +
		"q [in <project root> [in JavaProjectTests]]\n" +
		"x [in <project root> [in JavaProjectTests]]\n" +
		"x.y [in <project root> [in JavaProjectTests]]",
		packages);


	// create a nested folder in the output location and make sure it does not appear
	// as a package fragment
	IContainer underLyingResource = (IContainer)root.getUnderlyingResource();
	IFolder newFolder= underLyingResource.getFolder(new Path("bin")).getFolder(new Path("nested"));
	try {
		startDeltas();
		newFolder.create(false, true, null);
		assertDeltas(
			"Unexpected delta", 
			""
		);
	} finally {
		stopDeltas();
		deleteResource(newFolder);
	}
}
/**
 * Test that a package fragment (non-external, non-jar, non-default)
 * has a corresponding resource.
 */
public void testPackageFragmentCorrespondingResource() throws JavaModelException {
	IPackageFragment element= getPackageFragment("JavaProjectTests", "", "x.y");
	IResource corr= element.getCorrespondingResource();
	IResource res= getWorkspace().getRoot().getProject("JavaProjectTests").getFolder("x").getFolder("y");
	assertTrue("incorrect corresponding resource", corr.equals(res));
}
/**
 * Test that a package fragment (non-external, non-jar, non-default)
 * has a corresponding resource.
 */
public void testPackageFragmentHasSubpackages() throws JavaModelException {
	IPackageFragment def=		getPackageFragment("JavaProjectTests", "", "");
	IPackageFragment x=		getPackageFragment("JavaProjectTests", "", "x");
	IPackageFragment y=		getPackageFragment("JavaProjectTests", "", "x.y");
	assertTrue("default should have subpackages",							def.hasSubpackages());
	assertTrue("x should have subpackages",								x.hasSubpackages());
	assertTrue("x.y should NOT have subpackages",		!y.hasSubpackages());

	IPackageFragment java = getPackageFragment("JavaProjectTests", getExternalJCLPathString(), "java");
	IPackageFragment lang= getPackageFragment("JavaProjectTests", getExternalJCLPathString(), "java.lang");

	assertTrue("java should have subpackages",					java.hasSubpackages());
	assertTrue("java.lang  should NOT have subpackages",			!lang.hasSubpackages());
}
/*
 * Ensures that the structure is known for a package fragment on the classpath.
 */
public void testPackageFragmentIsStructureKnown1() throws CoreException {
	IPackageFragment pkg = getPackageFragment("JavaProjectTests", "", "x");
	assertTrue("Structure of package 'x' should be known", pkg.isStructureKnown());
}
/*
 * Ensures that asking if the structure is known for a package fragment outside the classpath throws a JavaModelException.
 * (regression test for bug 138577 Package content disapear in package explorer)
 */
public void testPackageFragmentIsStructureKnown2() throws CoreException {
	try {
		createJavaProject("P");
		createFolder("/P/pack");
		IPackageFragment pkg = getPackage("/P/pack");
		editFile(
			"/P/.classpath",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"	<classpathentry excluding=\"pack/\" kind=\"src\" path=\"\"/>\n" +
			"	<classpathentry kind=\"output\" path=\"\"/>\n" +
			"</classpath>"
		);
		JavaModelException exception = null;
		try {
			pkg.isStructureKnown();
		} catch (JavaModelException e) {
			exception = e;
		}
		assertExceptionEquals(
			"Unexpected exception",
			"pack [in <project root> [in P]] does not exist",
			exception);
	} finally {
		deleteProject("P");
	}
}

/*
 * Ensure that the non-Java resources of a source package are correct.
 */
public void testPackageFragmentNonJavaResources01() throws CoreException {
	// regular source package with resources
	IPackageFragment pkg = getPackageFragment("JavaProjectTests", "", "x");
	Object[] resources = pkg.getNonJavaResources();
	assertResourceTreeEquals(
		"Unexpected resources",
		"readme.txt\n" +
		"readme2.txt",
		resources);
}

/*
 * Ensure that the non-Java resources of a source package without resources are correct.
 */
public void testPackageFragmentNonJavaResources02() throws CoreException {
	IPackageFragment pkg = getPackageFragment("JavaProjectTests", "", "x.y");
	Object[] resources = pkg.getNonJavaResources();
	assertResourceTreeEquals(
		"Unexpected resources",
		"",
		resources);
}

/*
 * Ensure that the non-Java resources of the default package are correct.
 */
public void testPackageFragmentNonJavaResources03() throws CoreException {
	IPackageFragment pkg = getPackageFragment("JavaProjectTests", "", "");
	Object[] resources = pkg.getNonJavaResources();
	assertResourceTreeEquals(
		"Unexpected resources",
		"",
		resources);
}

/*
 * Ensure that the non-Java resources of a jar package fragment without resources are correct.
 */
public void testPackageFragmentNonJavaResources04() throws CoreException {
	IPackageFragment pkg = getPackageFragment("JavaProjectTests", "lib.jar", "p");
	Object[] resources = pkg.getNonJavaResources();
	assertResourceTreeEquals(
		"Unexpected resources",
		"",
		resources);
}

// TODO: zip default package with potentialy resources

/*
 * Ensure that the non-Java resources of a zip default package without resources are correct.
 */
public void testPackageFragmentNonJavaResources05() throws CoreException {
	IPackageFragment pkg = getPackageFragment("JavaProjectTests", "lib.jar", "");
	Object[] resources = pkg.getNonJavaResources();
	assertResourceTreeEquals(
		"Unexpected resources",
		"",
		resources);
}

/*
 * Ensure that the non-Java resources of a jar package fragment with resources are correct.
 * (regression test for bug 142530 [hierarchical packages] '.' in folder names confuses package explorer)
 */
public void testPackageFragmentNonJavaResources06() throws CoreException {
	IPackageFragment pkg = getPackageFragment("JavaProjectTests", "lib142530.jar", "p");
	Object[] resources = pkg.getNonJavaResources();
	assertResourceTreeEquals(
		"Unexpected resources",
		"x.y\n" +
		"  Test.txt",
		resources);
}

/*
 * Ensure that the non-Java resources of a jar package fragment with resources are correct.
 * (regression test for bug 148949 JarEntryFile now returning 'null')
 */
public void testPackageFragmentNonJavaResources07() throws CoreException {
	IPackageFragment pkg = getPackageFragment("JavaProjectTests", "lib148949.jar", "p");
	Object[] resources = pkg.getNonJavaResources();
	assertResourceTreeEquals(
		"Unexpected resources",
		"test.txt",
		resources);
}

/*
 * Ensures that the parent of a non-Java resource of a jar package fragment is correct
 */
public void testPackageFragmentNonJavaResources08() throws CoreException {
	IPackageFragment pkg = getPackageFragment("JavaProjectTests", "lib148949.jar", "p");
	Object[] resources = pkg.getNonJavaResources();
	Object parent = ((IJarEntryResource) resources[0]).getParent();
	assertElementEquals(
		"unexpected parent",
		"p [in lib148949.jar [in JavaProjectTests]]",
		(IPackageFragment) parent);
}

/*
 * Ensures that the full path of a non-Java resource of a jar package fragment is correct
 */
public void testPackageFragmentNonJavaResources09() throws CoreException {
	IPackageFragment pkg = getPackageFragment("JavaProjectTests", "lib148949.jar", "p");
	Object[] resources = pkg.getNonJavaResources();
	IPath path = ((IJarEntryResource) resources[0]).getFullPath();
	assertEquals(
		"unexpected full path",
		"/p/test.txt",
		path.toString());
}

/*
 * Ensures that the full path of a non-Java resource of a jar package fragment is correct
 */
public void testPackageFragmentNonJavaResources10() throws CoreException {
	IPackageFragment pkg = getPackageFragment("JavaProjectTests", "lib142530.jar", "p");
	Object[] resources = pkg.getNonJavaResources();
	IJarEntryResource resource = (IJarEntryResource) resources[0];
	IPath path = resource.getChildren()[0].getFullPath();
	assertEquals(
		"unexpected full path",
		"/p/x.y/Test.txt",
		path.toString());
}

/*
 * Ensures that the package fragment root of a non-Java resource of a jar package fragment is correct
 */
public void testPackageFragmentNonJavaResources11() throws CoreException {
	IPackageFragment pkg = getPackageFragment("JavaProjectTests", "lib148949.jar", "p");
	Object[] resources = pkg.getNonJavaResources();
	IPackageFragmentRoot parentRoot = ((IJarEntryResource) resources[0]).getPackageFragmentRoot();
	assertElementEquals(
		"unexpected package fragment root",
		"lib148949.jar [in JavaProjectTests]",
		parentRoot);
}

/*
 * Ensure that the non-Java resources of a package in an external library folder are correct.
 */
public void testPackageFragmentNonJavaResources12() throws CoreException {
	try {
		createExternalFolder("externalLib/p/META-INF");
		createExternalFile("externalLib/p/test.txt", "test");
		createJavaProject("P", new String[0], new String[] {getExternalResourcePath("externalLib")}, "");
		IPackageFragment pkg = getPackageFragmentRoot("P", getExternalResourcePath("externalLib")).getPackageFragment("p");
		Object[] resources = pkg.getNonJavaResources();
		assertResourceTreeEquals(
			"unexpected non java resources",
			"META-INF\n" +
			"test.txt",
			resources);
	} finally {
		deleteProject("P");
		deleteExternalResource("externalLib");
	}
}

/*
 * Ensures that the package-info.class file doesn't appear as a child of a package if proj=src
 * (regression test for bug 99654 [5.0] JavaModel returns both IClassFile and ICompilationUnit for package-info.java)
 */
public void testPackageFragmentPackageInfoClass() throws CoreException {
	try {
		createJavaProject("P");
		createFolder("/P/p1");
		IPackageFragment pkg = getPackage("/P/p1");
		pkg.open(null);
		createFile("/P/p1/package-info.class", "");
		assertResourceNamesEqual(
			"Unexpected resources of /P/p1",
			"",
			pkg.getNonJavaResources());
	} finally {
		deleteProject("P");
	}
}
/**
 * Tests that after a package "foo" has been renamed into "bar", it is possible to recreate
 * a "foo" package.
 * @see "1FWX0HY: ITPCORE:WIN98 - Problem after renaming a Java package"
 */
public void testPackageFragmentRenameAndCreate() throws JavaModelException, CoreException {
	IPackageFragment y = getPackageFragment("JavaProjectTests", "", "x.y");
	IFolder yFolder = (IFolder) y.getUnderlyingResource();
	IPath yPath = yFolder.getFullPath();
	IPath fooPath = yPath.removeLastSegments(1).append("foo");

	yFolder.move(fooPath, true, null);
	try {
		yFolder.create(true, true, null);
	} catch (Throwable e) {
		e.printStackTrace();
		assertTrue("should be able to recreate the y folder", false);
	}
	// restore the original state
	deleteResource(yFolder);
	IPackageFragment foo = getPackageFragment("JavaProjectTests", "", "x.foo");
	IFolder fooFolder = (IFolder) foo.getUnderlyingResource();
	fooFolder.move(yPath, true, null);
}
/**
 * Test that a package fragment root (non-external, non-jar, non-default root)
 * has a corresponding resource.
 */
public void testPackageFragmentRootCorrespondingResource() throws JavaModelException {
	IPackageFragmentRoot element= getPackageFragmentRoot("JavaProjectTests", "");
	IResource corr= element.getCorrespondingResource();
	IResource res= getWorkspace().getRoot().getProject("JavaProjectTests");
	assertTrue("incorrect corresponding resource", corr.equals(res));
	assertEquals("Project incorrect for folder resource", "JavaProjectTests", corr.getProject().getName());
}
/*
 * Ensures that the non-Java resources of a source package fragment root are correct
 * (case of a non empty set of non-Java resources)
 */
public void testPackageFragmentRootNonJavaResources1() throws JavaModelException {
	IPackageFragmentRoot root = getPackageFragmentRoot("JavaProjectTests", "");
	Object[] resources = root.getNonJavaResources();
	assertResourceNamesEqual(
		"unexpected non java resources",
		".classpath\n" +
		".project\n" +
		".settings",
		resources);
}

/*
 * Ensures that the non-Java resources of a source package fragment root are correct
 * (case of an empty set of non-Java resources)
 */
public void testPackageFragmentRootNonJavaResources2() throws JavaModelException {
 	IPackageFragmentRoot root = getPackageFragmentRoot("JavaProjectSrcTests", "src");
	Object[] resources = root.getNonJavaResources();
	assertResourceNamesEqual(
		"unexpected non java resources",
		"",
		resources);
}
/*
 * Ensures that the children of a non-Java resource of a jar package fragment root are correct
 */
public void testPackageFragmentRootNonJavaResources3() throws CoreException {
	IPackageFragmentRoot root = getPackageFragmentRoot("JavaProjectTests", "lib.jar");
	Object[] resources = root.getNonJavaResources();
	assertResourceTreeEquals(
		"unexpected non java resources",
		"META-INF\n" +
		"  MANIFEST.MF",
		resources);
}
/*
 * Ensures that the parent of a non-Java resource of a jar package fragment root is correct
 */
public void testPackageFragmentRootNonJavaResources4() throws CoreException {
	IPackageFragmentRoot root = getPackageFragmentRoot("JavaProjectTests", "lib.jar");
	Object[] resources = root.getNonJavaResources();
	Object parent = ((IJarEntryResource) resources[0]).getParent();
	assertElementEquals(
		"unexpected parent",
		"lib.jar [in JavaProjectTests]",
		(IPackageFragmentRoot) parent);
}
/*
 * Ensures that the full path of a non-Java resource of a jar package fragment root is correct
 */
public void testPackageFragmentRootNonJavaResources5() throws CoreException {
	IPackageFragmentRoot root = getPackageFragmentRoot("JavaProjectTests", "lib.jar");
	Object[] resources = root.getNonJavaResources();
	IPath path = ((IJarEntryResource) resources[0]).getFullPath();
	assertEquals(
		"unexpected full path",
		"/META-INF",
		path.toString());
}
/*
 * Ensures that the full path of a non-Java resource of a jar package fragment root is correct
 */
public void testPackageFragmentRootNonJavaResources6() throws CoreException {
	IPackageFragmentRoot root = getPackageFragmentRoot("JavaProjectTests", "lib.jar");
	Object[] resources = root.getNonJavaResources();
	IJarEntryResource resource = (IJarEntryResource) resources[0];
	IPath path = resource.getChildren()[0].getFullPath();
	assertEquals(
		"unexpected full path",
		"/META-INF/MANIFEST.MF",
		path.toString());
}
/*
 * Ensures that the package fragment root of a non-Java resource of a jar package fragment root is correct
 */
public void testPackageFragmentRootNonJavaResources7() throws CoreException {
	IPackageFragmentRoot root = getPackageFragmentRoot("JavaProjectTests", "lib.jar");
	Object[] resources = root.getNonJavaResources();
	IPackageFragmentRoot parentRoot = ((IJarEntryResource) resources[0]).getPackageFragmentRoot();
	assertElementEquals(
		"unexpected package fragment root",
		"lib.jar [in JavaProjectTests]",
		parentRoot);
}
/*
 * Ensures that the non-Java resources of an external library package fragment root are correct
 */
public void testPackageFragmentRootNonJavaResources8() throws CoreException {
	try {
		createExternalFolder("externalLib/META-INF");
		createExternalFile("externalLib/test.txt", "test");
		createJavaProject("P", new String[0], new String[] {getExternalResourcePath("externalLib")}, "");
		IPackageFragmentRoot root = getPackageFragmentRoot("P", getExternalResourcePath("externalLib"));
		Object[] resources = root.getNonJavaResources();
		assertResourceTreeEquals(
			"unexpected non java resources",
			"META-INF\n" +
			"test.txt",
			resources);
	} finally {
		deleteProject("P");
		deleteExternalResource("externalLib");
	}
}
/*
 * Ensures that the non-Java resources of an external jar package fragment root with non-standard META-INF are correct
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=222665 )
 */
public void testPackageFragmentRootNonJavaResources9() throws Exception {
	try {
		ZipOutputStream zip = null;
		try {
			zip = new ZipOutputStream(new FileOutputStream(getExternalFile("lib.jar")));
			// the bug occurred only if META-INF/MANIFEST.MF was before META-INF in the ZIP file
			zip.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
			zip.putNextEntry(new ZipEntry("META-INF"));
		} finally {
			if (zip != null)
				zip.close();
		}
		createJavaProject("P", new String[0], new String[] {getExternalResourcePath("lib.jar")}, "");
		IPackageFragmentRoot root = getPackageFragmentRoot("P", getExternalResourcePath("lib.jar"));
		Object[] resources = root.getNonJavaResources();
		assertResourceTreeEquals(
			"unexpected non java resources",
			"META-INF\n" +
			"  MANIFEST.MF",
			resources);
	} finally {
		deleteExternalResource("lib.jar");
		deleteProject("P");
	}
}
/**
 * Test raw entry inference performance for package fragment root
 */
public void testPackageFragmentRootRawEntry1() throws CoreException, IOException {
	File libDir = null;
	try {
		String libPath = getExternalPath() + "lib";
		JavaCore.setClasspathVariable("MyVar", new Path(libPath), null);
		IJavaProject proj =  this.createJavaProject("P", new String[] {}, "bin");
		libDir = new File(libPath);
		libDir.mkdirs();
		final int length = 200;
		IClasspathEntry[] classpath = new IClasspathEntry[length];
		for (int i = 0; i < length; i++){
			File libJar = new File(libDir, "lib"+i+".jar");
			libJar.createNewFile();
			classpath[i] = JavaCore.newVariableEntry(new Path("/MyVar/lib"+i+".jar"), null, null);
		}
		proj.setRawClasspath(classpath, null);

		IPackageFragmentRoot[] roots = proj.getPackageFragmentRoots();
		assertEquals("wrong number of entries:", length, roots.length);
		//long start = System.currentTimeMillis();
		for (int i = 0; i < roots.length; i++){
			IClasspathEntry rawEntry = roots[i].getRawClasspathEntry();
			assertEquals("unexpected root raw entry:", classpath[i], rawEntry);
		}
		//System.out.println((System.currentTimeMillis() - start)+ "ms for "+roots.length+" roots");
	} finally {
		if (libDir != null) {
			org.eclipse.jdt.core.tests.util.Util.delete(libDir);
		}
		this.deleteProject("P");
		JavaCore.removeClasspathVariable("MyVar", null);
	}
}
/**
 * Test raw entry inference performance for package fragment root in case
 * original classpath had duplicate entries pointing to it: first raw entry should be found
 */
public void testPackageFragmentRootRawEntry2() throws CoreException, IOException {
	File libDir = null;
	try {
		String externalPath = getExternalPath();
		String libPath = externalPath + "lib";
		JavaCore.setClasspathVariable("MyVar", new Path(externalPath), null);
		IJavaProject proj =  this.createJavaProject("P", new String[] {}, "bin");
		libDir = new File(libPath);
		libDir.mkdirs();
		IClasspathEntry[] classpath = new IClasspathEntry[2];
		File libJar = new File(libDir, "lib.jar");
		libJar.createNewFile();
		classpath[0] = JavaCore.newLibraryEntry(new Path(libPath).append("lib.jar"), null, null);
		classpath[1] = JavaCore.newVariableEntry(new Path("/MyVar").append("lib.jar"), null, null);
		proj.setRawClasspath(classpath, null);
		JavaCore.setClasspathVariable("MyVar", new Path(libPath), null); // change CP var value to cause collision

		IPackageFragmentRoot[] roots = proj.getPackageFragmentRoots();
		assertEquals("wrong number of entries:", 1, roots.length);
		IClasspathEntry rawEntry = roots[0].getRawClasspathEntry();
		assertEquals("unexpected root raw entry:", classpath[0], rawEntry); // ensure first entry is associated to the root
	} finally {
		if (libDir != null) {
			org.eclipse.jdt.core.tests.util.Util.delete(libDir);
		}
		this.deleteProject("P");
		JavaCore.removeClasspathVariable("MyVar", null);
	}
}
/**
 * @bug 162104: NPE in PackageExplorerContentProvider.getPackageFragmentRoots()
 * @test That a JME is thrown when a classpath entry is no longer on the classpath
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=162104"
 */
public void testPackageFragmentRootRawEntry3() throws CoreException, IOException {
	File libDir = null;
	try {
		String libPath = getExternalPath() + "lib";
		JavaCore.setClasspathVariable("MyVar", new Path(libPath), null);
		IJavaProject proj =  this.createJavaProject("P", new String[] {}, "bin");
		libDir = new File(libPath);
		libDir.mkdirs();
		final int length = 10;
		IClasspathEntry[] classpath = new IClasspathEntry[length];
		for (int i = 0; i < length; i++){
			File libJar = new File(libDir, "lib"+i+".jar");
			libJar.createNewFile();
			classpath[i] = JavaCore.newVariableEntry(new Path("/MyVar/lib"+i+".jar"), null, null);
		}
		proj.setRawClasspath(classpath, null);

		IPackageFragmentRoot[] roots = proj.getPackageFragmentRoots();
		assertEquals("wrong number of entries:", length, roots.length);

		// remove last classpath entry
		System.arraycopy(classpath, 0, classpath = new IClasspathEntry[length-1], 0, length-1);
		proj.setRawClasspath(classpath, null);

		// verify that JME occurs
		IPackageFragmentRoot lastRoot = roots[length-1];
		String rootPath = ((PackageFragmentRoot)lastRoot).toStringWithAncestors();
		try {
			IClasspathEntry rawEntry = roots[length-1].getRawClasspathEntry();
			assertNotNull("We should no longer get a null classpath entry:", rawEntry);
		} catch (JavaModelException jme) {
			assertStatus(rootPath+" is not on its project's build path", jme.getJavaModelStatus());
		}
	} finally {
		if (libDir != null) {
			org.eclipse.jdt.core.tests.util.Util.delete(libDir);
		}
		this.deleteProject("P");
		JavaCore.removeClasspathVariable("MyVar", null);
	}
}
/**
 * Ensures that the ".." raw classpath entry for a root is not resolved
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=249321 )
 */
public void testPackageFragmentRootRawEntry4() throws CoreException, IOException {
	String externalJarPath = getWorkspaceRoot().getLocation().removeLastSegments(1).append("external.jar").toOSString();
	try {
		IJavaProject p = createJavaProject("P");
		org.eclipse.jdt.core.tests.util.Util.writeToFile("", externalJarPath);
		setClasspath(p, new IClasspathEntry[] {JavaCore.newLibraryEntry(new Path("../../external.jar"), null, null)});
		IPackageFragmentRoot root = p.getPackageFragmentRoots()[0];
		IPath path = root.getRawClasspathEntry().getPath();
		assertEquals("Unexpected path for raw classpath entry", "../../external.jar", path.toString());
	} finally {
		deleteResource(new File(externalJarPath));
		deleteProject("P");
	}
}
/*
 * Ensures that opening a project update the project references
 * (regression test for bug 73253 [model] Project references not set on project open)
 */
public void testProjectOpen() throws CoreException {
	try {
		createJavaProject("P1");
		createJavaProject("P2", new String[0], new String[0], new String[] {"/P1"}, "");
		IProject p2 = getProject("P2");
		p2.close(null);
		p2.open(null);
		IProject[] references = p2.getDescription().getDynamicReferences();
		assertResourcesEqual(
			"Unexpected referenced projects",
			"/P1",
			references);
	} finally {
		deleteProjects(new String[] {"P1", "P2"});
	}
}
/**
 * Tests that opening a project triggers the correct deltas.
 */
public void testProjectOpen2() throws JavaModelException, CoreException {
	IJavaProject jproject= getJavaProject("JavaProjectTests");
	IProject project= jproject.getProject();
	project.close(null);

	try {
		startDeltas();
		project.open(null);
		assertDeltas(
			"Unexpected delta 2",
			"JavaProjectTests[*]: {OPENED}\n" +
			"ResourceDelta(/JavaProjectTests)"
		);
	} finally {
		stopDeltas();
	}
}
/**
 * Tests that opening a project keeps the same roots.
 */
public void testProjectOpen3() throws JavaModelException, CoreException {
	IJavaProject jproject= getJavaProject("JavaProjectTests");
	IPackageFragmentRoot[] originalRoots = jproject.getPackageFragmentRoots();
	IProject project= jproject.getProject();
	project.close(null);

	project.open(null);
	IPackageFragmentRoot[] openRoots = jproject.getPackageFragmentRoots();
	assertTrue("should have same number of roots", openRoots.length == originalRoots.length);
	for (int i = 0; i < openRoots.length; i++) {
		assertTrue("root not the same", openRoots[i].equals(originalRoots[i]));
	}
}
/**
 * Tests that closing a project triggers the correct deltas.
 */
public void testProjectClose() throws JavaModelException, CoreException {
	IJavaProject jproject= getJavaProject("JavaProjectTests");
	IProject project= jproject.getProject();

	try {
		startDeltas();
		project.close(null);
		assertDeltas(
			"Unexpected delta 1",
			"JavaProjectTests[*]: {CLOSED}\n" +
			"ResourceDelta(/JavaProjectTests)"
		);
	} finally {
		stopDeltas();
		project.open(null);
	}
}
/**
 * Test that a project has a corresponding resource.
 */
public void testProjectCorrespondingResource() throws JavaModelException {
	IJavaProject project= getJavaProject("JavaProjectTests");
	IResource corr= project.getCorrespondingResource();
	IResource res= getWorkspace().getRoot().getProject("JavaProjectTests");
	assertTrue("incorrect corresponding resource", corr.equals(res));
}
/**
 * Test that the correct children exist in a project
 */
public void testProjectGetChildren() throws JavaModelException {
	IJavaProject project = getJavaProject("JavaProjectTests");
	IJavaElement[] roots= project.getChildren();
	assertElementsEqual(
		"Unexpected package fragment roots",
		"<project root> [in JavaProjectTests]\n" +
		getExternalJCLPathString() + "\n" +
		"lib.jar [in JavaProjectTests]\n" +
		"lib142530.jar [in JavaProjectTests]\n" +
		"lib148949.jar [in JavaProjectTests]",
		roots);
}
/**
 * Test that the correct package fragments exist in the project.
 */
public void testProjectGetPackageFragments() throws JavaModelException {
	IJavaProject project= getJavaProject("JavaProjectTests");
	IPackageFragment[] fragments= project.getPackageFragments();
	assertSortedElementsEqual(
		"unexpected package fragments",
		"<default> [in "+ getExternalJCLPathString() + "]\n" +
		"<default> [in <project root> [in JavaProjectTests]]\n" +
		"<default> [in lib.jar [in JavaProjectTests]]\n" +
		"<default> [in lib142530.jar [in JavaProjectTests]]\n" +
		"<default> [in lib148949.jar [in JavaProjectTests]]\n" +
		"java [in "+ getExternalJCLPathString() + "]\n" +
		"java.io [in "+ getExternalJCLPathString() + "]\n" +
		"java.lang [in "+ getExternalJCLPathString() + "]\n" +
		"p [in lib.jar [in JavaProjectTests]]\n" +
		"p [in lib142530.jar [in JavaProjectTests]]\n" +
		"p [in lib148949.jar [in JavaProjectTests]]\n" +
		"q [in <project root> [in JavaProjectTests]]\n" +
		"x [in <project root> [in JavaProjectTests]]\n" +
		"x.y [in <project root> [in JavaProjectTests]]",
		fragments);
}
/*
 * Ensures that importing a project correctly update the project references
 * (regression test for bug 121569 [Import/Export] Importing projects in workspace, the default build order is alphabetical instead of by dependency)
 */
public void testProjectImport() throws CoreException {
	try {
		createJavaProject("P1");
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				createJavaProject("P2");
				editFile(
					"/P2/.classpath",
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
					"<classpath>\n" +
					"    <classpathentry kind=\"src\" path=\"/P1\"/>\n" +
					"    <classpathentry kind=\"output\" path=\"\"/>\n" +
					"</classpath>"
				);
			}
		};
		getWorkspace().run(runnable, null);
		waitForAutoBuild();
		IProject[] referencedProjects = getProject("P2").getReferencedProjects();
		assertResourcesEqual(
			"Unexpected project references",
			"/P1",
			referencedProjects);
	} finally {
		deleteProjects(new String[] {"P1", "P2"});
	}
}

/*
 * Ensures that importing a project correctly update the project references
 * (regression test for bug 172666 Importing pde.ui and dependencies as binary gives compile error)
 */
public void testProjectImport2() throws CoreException {
	IResourceChangeListener resourceChangeListener = new IResourceChangeListener() {
		public void resourceChanged(IResourceChangeEvent event) {
			try {
				ContainerInitializer.initializer.initialize(new Path("org.eclipse.jdt.core.tests.model.TEST_CONTAINER"), getJavaProject("P2"));
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}

	};
	try {
		createJavaProject("P1");
		createFile("/P1/lib.jar", "");
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				createProject("P2");
				createFile(
					"/P2/.classpath",
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
					"<classpath>\n" +
					"	<classpathentry kind=\"con\" path=\"org.eclipse.jdt.core.tests.model.TEST_CONTAINER\"/>\n" +
					"</classpath>"
				);
				ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P2", "/P1/lib.jar"}));
				getWorkspace().checkpoint(false/*don't build*/);
				editFile(
					"/P2/.project",
					"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
					"<projectDescription>\n" +
					"	<name>P2</name>\n" +
					"	<comment></comment>\n" +
					"	<projects>\n" +
					"	</projects>\n" +
					"	<buildSpec>\n" +
					"		<buildCommand>\n" +
					"			<name>org.eclipse.jdt.core.javabuilder</name>\n" +
					"			<arguments>\n" +
					"			</arguments>\n" +
					"		</buildCommand>\n" +
					"	</buildSpec>\n" +
					"	<natures>\n" +
					"		<nature>org.eclipse.jdt.core.javanature</nature>\n" +
					"	</natures>\n" +
					"</projectDescription>"
				);
				ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P2", "/P1"}));
			}
		};
		JavaCore.addPreProcessingResourceChangedListener(resourceChangeListener, IResourceChangeEvent.POST_CHANGE);
		getWorkspace().run(runnable, null);
		waitForAutoBuild();
		IProject[] referencedProjects = getProject("P2").getReferencedProjects();
		assertResourcesEqual(
			"Unexpected project references",
			"/P1",
			referencedProjects);
	} finally {
		JavaCore.removePreProcessingResourceChangedListener(resourceChangeListener);
		deleteProjects(new String[] {"P1", "P2"});
	}
}

/*
 * Ensures that importing a project correctly update the project references
 * (regression test for bug 172666 Importing pde.ui and dependencies as binary gives compile error)
 */
public void testProjectImport3() throws CoreException {
	IResourceChangeListener resourceChangeListener = new IResourceChangeListener() {
		public void resourceChanged(IResourceChangeEvent event) {
			try {
				ContainerInitializer.initializer.initialize(new Path("org.eclipse.jdt.core.tests.model.TEST_CONTAINER"), getJavaProject("P2"));
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}

	};
	try {
		createJavaProject("P1");
		createFile("/P1/lib.jar", "");
		createProject("P2");
		createFile(
			"/P2/.classpath",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"	<classpathentry kind=\"con\" path=\"org.eclipse.jdt.core.tests.model.TEST_CONTAINER\"/>\n" +
			"</classpath>"
		);
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P2", "/P1"}));
		JavaCore.addPreProcessingResourceChangedListener(resourceChangeListener, IResourceChangeEvent.POST_CHANGE);
		editFile(
			"/P2/.project",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<projectDescription>\n" +
			"	<name>P2</name>\n" +
			"	<comment></comment>\n" +
			"	<projects>\n" +
			"	</projects>\n" +
			"	<buildSpec>\n" +
			"		<buildCommand>\n" +
			"			<name>org.eclipse.jdt.core.javabuilder</name>\n" +
			"			<arguments>\n" +
			"			</arguments>\n" +
			"		</buildCommand>\n" +
			"	</buildSpec>\n" +
			"	<natures>\n" +
			"		<nature>org.eclipse.jdt.core.javanature</nature>\n" +
			"	</natures>\n" +
			"</projectDescription>"
		);
		waitForAutoBuild();
		IProject[] referencedProjects = getProject("P2").getReferencedProjects();
		assertResourcesEqual(
			"Unexpected project references",
			"/P1",
			referencedProjects);
	} finally {
		JavaCore.removePreProcessingResourceChangedListener(resourceChangeListener);
		deleteProjects(new String[] {"P1", "P2"});
	}
}

/*
 * Ensures that removing a library entry for an existing external library folder updates the model
 */
public void testRemoveExternalLibFolder1() throws CoreException {
	try {
		createExternalFolder("externalLib");
		IJavaProject p = createJavaProject("P", new String[0], new String[] {getExternalResourcePath("externalLib")}, "");
		expandAll(p);
		setClasspath(p, new IClasspathEntry[] {});
		assertElementDescendants(
			"Unexpected project content",
			"P",
			p
		);
	} finally {
		deleteExternalResource("externalLib");
		deleteProject("P");
	}
}

/*
 * Ensures that removing a library entry for a non-existing external library folder updates the model
 */
public void testRemoveExternalLibFolder2() throws CoreException {
	try {
		IJavaProject p = createJavaProject("P", new String[0], new String[] {getExternalResourcePath("externalLib")}, "");
		expandAll(p);
		setClasspath(p, new IClasspathEntry[] {});
		assertElementDescendants(
			"Unexpected project content",
			"P",
			p
		);
	} finally {
		deleteProject("P");
	}
}


/*
 * Ensures that removing an external library folder referenced by a library entry and refreshing updates the model
 */
public void testRemoveExternalLibFolder3() throws CoreException {
	try {
		createExternalFolder("externalLib");
		IJavaProject p = createJavaProject("P", new String[0], new String[] {getExternalResourcePath("externalLib")}, "");
		expandAll(p);
		deleteExternalResource("externalLib");
		refresh(p);
		assertElementDescendants(
			"Unexpected project content",
			"P",
			p
		);
	} finally {
		deleteExternalResource("externalLib");
		deleteProject("P");
	}
}

/*
 * Ensures that removing a library entry for an existing external ZIP archive updates the model
 */
public void testRemoveZIPArchive1() throws CoreException {
	try {
		createExternalFile("externalLib.abc", "");
		IJavaProject p = createJavaProject("P", new String[0], new String[] {getExternalResourcePath("externalLib.abc")}, "");
		refreshExternalArchives(p);
		expandAll(p);

		setClasspath(p, new IClasspathEntry[] {});
		assertElementDescendants(
			"Unexpected project content",
			"P",
			p
		);
	} finally {
		deleteExternalResource("externalLib.abc");
		deleteProject("P");
	}
}

/*
 * Ensures that removing a library entry for a non-existing external ZIP archive updates the model
 */
public void testRemoveZIPArchive2() throws CoreException {
	try {
		IJavaProject p = createJavaProject("P", new String[0], new String[] {getExternalResourcePath("externalLib.abc")}, "");
		refreshExternalArchives(p);
		expandAll(p);

		setClasspath(p, new IClasspathEntry[] {});
		assertElementDescendants(
			"Unexpected project content",
			"P",
			p
		);
	} finally {
		deleteProject("P");
	}
}


/*
 * Ensures that removing an external ZIP archive referenced by a library entry and refreshing updates the model
 */
public void testRemoveZIPArchive3() throws CoreException {
	try {
		createExternalFile("externalLib.abc", "");
		IJavaProject p = createJavaProject("P", new String[0], new String[] {getExternalResourcePath("externalLib.abc")}, "");
		refreshExternalArchives(p);
		expandAll(p);

		deleteExternalResource("externalLib.abc");
		refreshExternalArchives(p);
		assertElementDescendants(
			"Unexpected project content",
			"P",
			p
		);
	} finally {
		deleteExternalResource("externalLib.abc");
		deleteProject("P");
	}
}

/*
 * Ensures that removing a library entry for an existing internal ZIP archive updates the model
 */
public void testRemoveZIPArchive4() throws CoreException {
	try {
		IJavaProject p = createJavaProject("P", new String[0], new String[] {"/P/internalLib.abc"}, "");
		createFile("/P/internalLib.abc", "");
		expandAll(p);

		setClasspath(p, new IClasspathEntry[] {});
		assertElementDescendants(
			"Unexpected project content",
			"P",
			p
		);
	} finally {
		deleteProject("P");
	}
}

/**
 * Test that the correct package fragments exist in the project.
 */
public void testRootGetPackageFragments() throws JavaModelException {
	IPackageFragmentRoot root= getPackageFragmentRoot("JavaProjectTests", "");
	IJavaElement[] fragments= root.getChildren();
	assertElementsEqual(
		"unexpected package fragments in source folder",
		"<default> [in <project root> [in JavaProjectTests]]\n" +
		"q [in <project root> [in JavaProjectTests]]\n" +
		"x [in <project root> [in JavaProjectTests]]\n" +
		"x.y [in <project root> [in JavaProjectTests]]",
		fragments);

	root= getPackageFragmentRoot("JavaProjectTests", "lib.jar");
	fragments= root.getChildren();
	assertSortedElementsEqual(
		"unexpected package fragments in library",
		"<default> [in lib.jar [in JavaProjectTests]]\n" +
		"p [in lib.jar [in JavaProjectTests]]",
		fragments);
}
/**
 * Test that the correct package fragments exist in the project.
 * (regression test for bug 32041 Multiple output folders fooling Java Model)
 */
public void testRootGetPackageFragments2() throws CoreException {
	try {
		this.createJavaProject("P");
		this.createFolder("/P/bin");
		this.createFolder("/P/bin2");
		editFile(
			"/P/.classpath",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"    <classpathentry kind=\"src\" output=\"bin2\" path=\"\"/>\n" +
			"    <classpathentry kind=\"output\" path=\"bin\"/>\n" +
			"</classpath>"
		);
		IPackageFragmentRoot root = getPackageFragmentRoot("/P");
		assertElementsEqual(
			"Unexpected packages",
			"<default> [in <project root> [in P]]",
			root.getChildren());
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Test that the correct package fragments exist in the project.
 * (regression test for bug 65693 Package Explorer shows .class files instead of .java)
 */
public void testRootGetPackageFragments3() throws CoreException {
	try {
		IJavaProject p1 = createJavaProject("P1");
		createFile(
			"/P1/X.java",
			"public class X {\n" +
			"}"
		);
		getProject("P1").build(IncrementalProjectBuilder.FULL_BUILD, null);
		IJavaProject p2 = createJavaProject("P2");
		editFile(
			"/P2/.classpath",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"    <classpathentry kind=\"src\" path=\"\"/>\n" +
			"    <classpathentry kind=\"lib\" path=\"/P1\"/>\n" +
			"    <classpathentry kind=\"output\" path=\"\"/>\n" +
			"</classpath>"
		);
		IPackageFragment pkg = p1.getPackageFragmentRoot(p1.getProject()).getPackageFragment("");
		assertElementsEqual(
			"Unexpected packages for P1",
			"X.java [in <default> [in <project root> [in P1]]]",
			pkg.getChildren());
		pkg = p2.getPackageFragmentRoot(p1.getProject()).getPackageFragment("");
		assertElementsEqual(
			"Unexpected packages for P2",
			"X.class [in <default> [in /P1 [in P2]]]",
			pkg.getChildren());
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}
/**
 * Ensure a source folder can have a name ending with ".jar"
 */
public void testSourceFolderWithJarName() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {"src.jar"}, "bin");
		IFile file = createFile("/P/src.jar/X.java", "class X {}");
		ICompilationUnit unit = (ICompilationUnit)JavaCore.create(file);
		unit.getAllTypes(); // force to open
	} catch (CoreException e) {
		assertTrue("unable to open unit in 'src.jar' source folder", false);
	} finally {
		this.deleteProject("P");
	}
}/**
 * Test that a method
 * has no corresponding resource.
 */
public void testSourceMethodCorrespondingResource() throws JavaModelException {
	ICompilationUnit element= getCompilationUnit("JavaProjectTests", "", "q", "A.java");
	IMethod[] methods = element.getType("A").getMethods();
	assertTrue("missing methods", methods.length > 0);
	IResource corr= methods[0].getCorrespondingResource();
	assertTrue("incorrect corresponding resource", corr == null);
}
/**
 * Test the jdklevel of the package fragment root
 */
public void testJdkLevelRoot() throws JavaModelException {
	IPackageFragmentRoot root= getPackageFragmentRoot("JavaProjectLibTests", "lib/");
	assertEquals("wrong type", IPackageFragmentRoot.K_BINARY, root.getKind());
	assertEquals("wrong jdk level", ClassFileConstants.JDK1_1, Util.getJdkLevel(root.getResource()));
}
/**
 * Test User Library preference. External jar file referenced in library entry does not exist.
 * It does not need to as we only test the preference value...
 *
 * @test bug 88719: UserLibrary.serialize /createFromString need support for access restriction / attributes
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=88719"
 */
public void testUserLibrary() throws JavaModelException {

	IClasspathEntry[] userEntries = new IClasspathEntry[2];

	// Set first classpath entry
	IPath path = new Path("/tmp/test.jar");
	IAccessRule[] pathRules = new IAccessRule[3];
	pathRules[0] = JavaCore.newAccessRule(new Path("**/forbidden/**"), IAccessRule.K_NON_ACCESSIBLE);
	pathRules[1] = JavaCore.newAccessRule(new Path("**/discouraged/**"), IAccessRule.K_DISCOURAGED);
	pathRules[2] = JavaCore.newAccessRule(new Path("**/accessible/**"), IAccessRule.K_ACCESSIBLE);
	IClasspathAttribute[] extraAttributes = new IClasspathAttribute[2];
	extraAttributes[0] = JavaCore.newClasspathAttribute("javadoc_location", "http://www.sample-url.org/doc/");
	extraAttributes[1] = JavaCore.newClasspathAttribute("org.eclipse.jdt.launching.CLASSPATH_ATTR_LIBRARY_PATH_ENTRY", "/tmp");
	userEntries[0] = JavaCore.newLibraryEntry(path, null, null, pathRules, extraAttributes, false);

	// Set second classpath entry
	path = new Path("/tmp/test.jar");
	pathRules = new IAccessRule[3];
	pathRules[0] = JavaCore.newAccessRule(new Path("/org/eclipse/forbidden/**"), IAccessRule.K_NON_ACCESSIBLE);
	pathRules[1] = JavaCore.newAccessRule(new Path("/org/eclipse/discouraged/**"), IAccessRule.K_DISCOURAGED);
	pathRules[2] = JavaCore.newAccessRule(new Path("/org/eclipse/accessible/**"), IAccessRule.K_ACCESSIBLE);
	extraAttributes = new IClasspathAttribute[2];
	extraAttributes[0] = JavaCore.newClasspathAttribute("javadoc_location", "http://www.sample-url.org/doc/");
	extraAttributes[1] = JavaCore.newClasspathAttribute("org.eclipse.jdt.launching.CLASSPATH_ATTR_LIBRARY_PATH_ENTRY", "/tmp");
	userEntries[1] = JavaCore.newLibraryEntry(path, null, null, pathRules, extraAttributes, false);

	// Create user library
	JavaModelManager.getUserLibraryManager().setUserLibrary("TEST", userEntries, false);

	// Verify it has been written in preferences
	IEclipsePreferences instancePreferences = JavaModelManager.getJavaModelManager().getInstancePreferences();
	String containerKey = JavaModelManager.CP_USERLIBRARY_PREFERENCES_PREFIX+"TEST";
	String libraryPreference = instancePreferences.get(containerKey, null);
	assertNotNull("Should get a preference for TEST user library", libraryPreference);

	assertSourceEquals(
		"Invalid library contents",
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
		"<userlibrary systemlibrary=\"false\" version=\"1\">\n" +
		"	<archive path=\"/tmp/test.jar\">\n" +
		"		<attributes>\n" +
		"			<attribute name=\"javadoc_location\" value=\"http://www.sample-url.org/doc/\"/>\n" +
		"			<attribute name=\"org.eclipse.jdt.launching.CLASSPATH_ATTR_LIBRARY_PATH_ENTRY\" value=\"/tmp\"/>\n" +
		"		</attributes>\n" +
		"		<accessrules>\n" +
		"			<accessrule kind=\"nonaccessible\" pattern=\"**/forbidden/**\"/>\n" +
		"			<accessrule kind=\"discouraged\" pattern=\"**/discouraged/**\"/>\n" +
		"			<accessrule kind=\"accessible\" pattern=\"**/accessible/**\"/>\n" +
		"		</accessrules>\n" +
		"	</archive>\n" +
		"	<archive path=\"/tmp/test.jar\">\n" +
		"		<attributes>\n" +
		"			<attribute name=\"javadoc_location\" value=\"http://www.sample-url.org/doc/\"/>\n" +
		"			<attribute name=\"org.eclipse.jdt.launching.CLASSPATH_ATTR_LIBRARY_PATH_ENTRY\" value=\"/tmp\"/>\n" +
		"		</attributes>\n" +
		"		<accessrules>\n" +
		"			<accessrule kind=\"nonaccessible\" pattern=\"/org/eclipse/forbidden/**\"/>\n" +
		"			<accessrule kind=\"discouraged\" pattern=\"/org/eclipse/discouraged/**\"/>\n" +
		"			<accessrule kind=\"accessible\" pattern=\"/org/eclipse/accessible/**\"/>\n" +
		"		</accessrules>\n" +
		"	</archive>\n" +
		"</userlibrary>\n",
		libraryPreference);
}

/**
 * @bug 148859: [model][delta] Package Explorer only shows default package after import
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=148859"
 */
public void testBug148859() throws CoreException {
	try {
		ResourcesPlugin.getWorkspace().run(
			new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					IJavaProject project = createJavaProject("P");
					project.findType("X");
					createFolder("/P/pack");
				}
			},
			null);
		IPackageFragmentRoot root = getPackageFragmentRoot("P", "");
		assertElementsEqual(
			"Unexpected children size in 'P' default source folder",
			"<default> [in <project root> [in P]]\n" +
			"pack [in <project root> [in P]]",
			root.getChildren());
	} finally {
		deleteProject("P");
	}
}

/**
 * @bug 183923: [prefs] NPE in JavaProject#setOptions
 * @test Verify that no NPE occurs when options is set on an invalid project
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=183923"
 */
public void testBug183923() throws CoreException, IOException {
	try {
		setUpJavaProject("JavaProjectTestsInvalidProject");
	} catch (JavaModelException jme) {
		assertEquals("Unexpected JavaModelException", "JavaProjectTestsInvalidProject does not exist", jme.getMessage());
	} finally {
		deleteProject("JavaProjectTestsInvalidProject");
	}
}
}
