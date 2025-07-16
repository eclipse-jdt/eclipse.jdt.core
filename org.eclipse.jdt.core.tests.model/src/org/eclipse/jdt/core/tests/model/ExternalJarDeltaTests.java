/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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

import java.io.File;
import java.io.IOException;
import junit.framework.Test;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
/**
 * These test ensure that modifications in external jar are correctly reported as
 * IJavaEllementDeltas after a JavaModel#refreshExternalArchives().
 */
public class ExternalJarDeltaTests extends ModifyingResourceTests {

public ExternalJarDeltaTests(String name) {
	super(name);
}
public static Test suite() {
	return buildModelTestSuite(ExternalJarDeltaTests.class);
}
// Use this static initializer to specify subset for tests
// All specified tests which do not belong to the class are skipped...
static {
//		TESTS_PREFIX =  "testBug79990";
//		TESTS_NAMES = new String[] { "testExternalJarInternalExternalJar"};
//		TESTS_NUMBERS = new int[] { 79860, 80918, 91078 };
//		TESTS_RANGE = new int[] { 83304, -1 };
}

/*
 * Ensures that passing an empty scope to refreshExternalArchives(..) doesn't throw a NPE
 * (regression test for bug 202076 NPE in DeltaProcessor)
 */
public void testEmptyScope() throws Exception {
	try {
		startDeltas();
		getJavaModel().refreshExternalArchives(new IJavaElement[0], null);
		assertDeltas("Unexpected delta", "");
	} finally {
		stopDeltas();
	}
}

/**
 * Test if a modification is detected without doing a refresh.
 * Currently no modification are detected.
 */
public void testExternalJar0() throws CoreException, IOException {
	File f = null;
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {""}, "");
		String pPath = getExternalPath() + "p.jar";
		setClasspath(project, new IClasspathEntry[]{JavaCore.newLibraryEntry(new Path(pPath), null, null)});

		f = new File(pPath);
		f.createNewFile();
		getJavaModel().refreshExternalArchives(null,null);
		waitUntilIndexesReady();

		startDeltas();
		touch(f);

		assertDeltas(
			"Unexpected delta",
			""
		);
	} finally {
		if(f != null) {
			deleteResource(f);
		}
		this.deleteProject("P");
		stopDeltas();
	}
}

/**
 * Refresh the JavaModel after a modification of an external jar.
 */
public void testExternalJarChanged1() throws CoreException, IOException {
	File f = null;
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {""}, "");

		String pPath = getExternalPath() + "p.jar";
		setClasspath(project, new IClasspathEntry[]{JavaCore.newLibraryEntry(new Path(pPath), null, null)});

		f = new File(pPath);
		f.createNewFile();
		getJavaModel().refreshExternalArchives(null,null);
		waitUntilIndexesReady();
		startDeltas();

		touch(f);
		getJavaModel().refreshExternalArchives(null,null);

		assertDeltas(
			"Unexpected delta",
			"P[*]: {CHILDREN}\n"+
			"	"+f.getCanonicalPath()+"[*]: {CONTENT | ARCHIVE CONTENT CHANGED}"
		);
	} finally {
		if(f != null) {
			deleteResource(f);
		}
		this.deleteProject("P");
		stopDeltas();
	}
}
/**
 * Refresh a JavaProject after a modification of an external jar.
 */
public void testExternalJarChanged2() throws CoreException, IOException {
	File f = null;
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {""}, "");

		String pPath = getExternalPath() + "p.jar";
		setClasspath(project, new IClasspathEntry[]{JavaCore.newLibraryEntry(new Path(pPath), null, null)});

		f = new File(pPath);
		f.createNewFile();
		getJavaModel().refreshExternalArchives(null,null);
		waitUntilIndexesReady();
		startDeltas();

		touch(f);
		getJavaModel().refreshExternalArchives(new IJavaElement[]{project},null);

		assertDeltas(
			"Unexpected delta",
			"P[*]: {CHILDREN}\n"+
			"	"+f.getCanonicalPath()+"[*]: {CONTENT | ARCHIVE CONTENT CHANGED}"
		);
	} finally {
		if(f != null) {
			deleteResource(f);
		}
		this.deleteProject("P");
		stopDeltas();
	}
}
/**
 * Refresh an external jar after a modification of this jar.
 */
public void testExternalJarChanged3() throws CoreException, IOException {
	File f = null;
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {""}, "");

		String pPath = getExternalPath() + "p.jar";
		setClasspath(project, new IClasspathEntry[]{JavaCore.newLibraryEntry(new Path(pPath), null, null)});

		f = new File(pPath);
		f.createNewFile();
		getJavaModel().refreshExternalArchives(null,null);
		waitUntilIndexesReady();
		startDeltas();

		touch(f);
		IPackageFragmentRoot root = project.getPackageFragmentRoot(pPath);
		getJavaModel().refreshExternalArchives(new IJavaElement[]{root},null);

		assertDeltas(
			"Unexpected delta",
			"P[*]: {CHILDREN}\n"+
			"	"+f.getCanonicalPath()+"[*]: {CONTENT | ARCHIVE CONTENT CHANGED}"
		);
	} finally {
		if(f != null) {
			deleteResource(f);
		}
		this.deleteProject("P");
		stopDeltas();
	}
}
/**
 * Refresh the JavaModel after a modification of an external jar after shutdown.
 * (regression test for bug 39856 External jar's timestamps should be persisted accross sessions)
 */
public void testExternalJarChanged4() throws CoreException, IOException {
	File f = null;
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {""}, "");

		String pPath = getExternalPath() + "p.jar";
		setClasspath(project, new IClasspathEntry[]{JavaCore.newLibraryEntry(new Path(pPath), null, null)});

		f = new File(pPath);
		f.createNewFile();
		getJavaModel().refreshExternalArchives(null,null);
		waitUntilIndexesReady();

		// exit, change the jar, and restart
		simulateExit();
		try {
			touch(f);
		} finally {
			simulateRestart();
		}

		startDeltas();
		getJavaModel().refreshExternalArchives(null,null);

		assertDeltas(
			"Unexpected delta",
			"P[*]: {CHILDREN}\n"+
			"	"+f.getCanonicalPath()+"[*]: {CONTENT | ARCHIVE CONTENT CHANGED}"
		);
	} finally {
		if(f != null) {
			deleteResource(f);
		}
		this.deleteProject("P");
		stopDeltas();
	}
}
/**
 * Ensure that the external jars are refreshed by a call to JavaCore#initializeAfterLoad()
 * (regression test for bug 93668 Search indexes not rebuild)
 */
public void testExternalJarChanged5() throws CoreException, IOException {
	System.out.println("testExternalJarChanged5");
	File f = null;
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {""}, "");

		String pPath = getExternalPath() + "p.jar";
		setClasspath(project, new IClasspathEntry[]{JavaCore.newLibraryEntry(new Path(pPath), null, null)});

		f = new File(pPath);
		f.createNewFile();
		getJavaModel().refreshExternalArchives(null,null);
		waitUntilIndexesReady();

		// exit, change the jar, and restart
		simulateExit();
		try {
			touch(f);
		} finally {
			simulateRestart();
		}

		startDeltas();
		JavaCore.initializeAfterLoad(null);

		assertDeltas(
			"Unexpected delta",
			"P[*]: {CHILDREN}\n"+
			"	"+f.getCanonicalPath()+"[*]: {CONTENT | ARCHIVE CONTENT CHANGED}"
		);
	} finally {
		if(f != null) {
			deleteResource(f);
		}
		this.deleteProject("P");
		stopDeltas();
	}
}
/*
 * Ensures that the correct delta is reported after a setRawClasspath and after a modification of an external jar.
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=212769 )
 */
public void testExternalJarChanged6() throws CoreException, IOException {
	File f = null;
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {""}, "");

		String pPath = getExternalPath() + "p.jar";
		setClasspath(project, new IClasspathEntry[]{JavaCore.newLibraryEntry(new Path(pPath), null, null)});

		f = new File(pPath);
		f.createNewFile();
		getJavaModel().refreshExternalArchives(null,null);
		waitUntilIndexesReady();
		startDeltas();

		touch(f);
		setClasspath(project, new IClasspathEntry[]{JavaCore.newLibraryEntry(new Path(pPath), null, null), JavaCore.newSourceEntry(new Path("/P"))});

		assertDeltas(
			"Unexpected delta",
			"P[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
			"	<project root>[*]: {ADDED TO CLASSPATH}\n" +
			"	"+f.getCanonicalPath()+"[*]: {CONTENT | ARCHIVE CONTENT CHANGED}\n" +
			"	ResourceDelta(/P/.classpath)[*]"
		);
	} finally {
		if(f != null) {
			deleteResource(f);
		}
		this.deleteProject("P");
		stopDeltas();
	}
}
/**
 * Refresh the JavaModel after an addition of an external jar.
 */
public void testExternalJarAdded1() throws CoreException, IOException {
	File f = null;
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {""}, "");

		String pPath = getExternalPath() + "pAdded1.jar";
		setClasspath(project, new IClasspathEntry[]{JavaCore.newLibraryEntry(new Path(pPath), null, null)});

		getJavaModel().refreshExternalArchives(null,null);
		waitUntilIndexesReady();
		startDeltas();

		f = new File(pPath);
		f.createNewFile();
		getJavaModel().refreshExternalArchives(null,null);

		assertDeltas(
			"Unexpected delta",
			"P[*]: {CHILDREN}\n"+
			"	"+f.getCanonicalPath()+"[+]: {}"
		);
	} finally {
		if(f != null) {
			deleteResource(f);
		}
		this.deleteProject("P");
		stopDeltas();
	}
}
/**
 * Refresh a JavaProject after an addition of an external jar.
 */
public void testExternalJarAdded2() throws CoreException, IOException {
	File f = null;
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {""}, "");

		String pPath = getExternalPath() + "pAdded2.jar";
		setClasspath(project, new IClasspathEntry[]{JavaCore.newLibraryEntry(new Path(pPath), null, null)});

		getJavaModel().refreshExternalArchives(null,null);
		waitUntilIndexesReady();
		startDeltas();

		f = new File(pPath);
		f.createNewFile();
		getJavaModel().refreshExternalArchives(new IJavaElement[]{project},null);

		assertDeltas(
			"Unexpected delta",
			"P[*]: {CHILDREN}\n"+
			"	"+f.getCanonicalPath()+"[+]: {}"
		);
	} finally {
		if(f != null) {
			deleteResource(f);
		}
		this.deleteProject("P");
		stopDeltas();
	}
}
/**
 * Refresh an external jar after an addition of this jar.
 */
public void testExternalJarAdded3() throws CoreException, IOException {
	File f = null;
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {""}, "");

		String pPath = getExternalPath() + "pAdded3.jar";
		setClasspath(project, new IClasspathEntry[]{JavaCore.newLibraryEntry(new Path(pPath), null, null)});

		getJavaModel().refreshExternalArchives(null,null);
		waitUntilIndexesReady();
		startDeltas();

		f = new File(pPath);
		f.createNewFile();
		IPackageFragmentRoot root = project.getPackageFragmentRoot(pPath);
		getJavaModel().refreshExternalArchives(new IJavaElement[]{root},null);

		assertDeltas(
			"Unexpected delta",
			"P[*]: {CHILDREN}\n"+
			"	"+f.getCanonicalPath()+"[+]: {}"
		);
	} finally {
		if(f != null) {
			deleteResource(f);
		}
		this.deleteProject("P");
		stopDeltas();
	}
}
/**
 * Refresh the JavaModel after a removal of an external jar.
 */
public void testExternalJarRemoved1() throws CoreException, IOException {
	File f = null;
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {""}, "");

		String pPath = getExternalPath() + "p.jar";
		setClasspath(project, new IClasspathEntry[]{JavaCore.newLibraryEntry(new Path(pPath), null, null)});

		f = new File(pPath);
		f.createNewFile();
		getJavaModel().refreshExternalArchives(null,null);
		waitUntilIndexesReady();
		startDeltas();

		deleteResource(f);
		getJavaModel().refreshExternalArchives(null,null);

		assertDeltas(
			"Unexpected delta",
			"P[*]: {CHILDREN}\n"+
			"	"+f.getCanonicalPath()+"[-]: {}"
		);
	} finally {
		if(f != null) {
			deleteResource(f);
		}
		this.deleteProject("P");
		stopDeltas();
	}
}
/**
 * Refresh a JavaProject after a removal of an external jar.
 */
public void testExternalJarRemoved2() throws CoreException, IOException {
	File f = null;
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {""}, "");

		String pPath = getExternalPath() + "p.jar";
		setClasspath(project, new IClasspathEntry[]{JavaCore.newLibraryEntry(new Path(pPath), null, null)});

		f = new File(pPath);
		f.createNewFile();
		getJavaModel().refreshExternalArchives(null,null);
		waitUntilIndexesReady();
		startDeltas();

		deleteResource(f);
		getJavaModel().refreshExternalArchives(new IJavaElement[]{project},null);

		assertDeltas(
			"Unexpected delta",
			"P[*]: {CHILDREN}\n"+
			"	"+f.getCanonicalPath()+"[-]: {}"
		);
	} finally {
		if(f != null) {
			deleteResource(f);
		}
		this.deleteProject("P");
		stopDeltas();
	}
}
/**
 * Refresh an external jar after a removal of this jar.
 */
public void testExternalJarRemoved3() throws CoreException, IOException {
	File f = null;
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {""}, "");

		String pPath = getExternalPath() + "p.jar";
		setClasspath(project, new IClasspathEntry[]{JavaCore.newLibraryEntry(new Path(pPath), null, null)});

		f = new File(pPath);
		f.createNewFile();
		getJavaModel().refreshExternalArchives(null,null);
		waitUntilIndexesReady();
		startDeltas();

		deleteResource(f);
		IPackageFragmentRoot root = project.getPackageFragmentRoot(pPath);
		getJavaModel().refreshExternalArchives(new IJavaElement[]{root},null);

		assertDeltas(
			"Unexpected delta",
			"P[*]: {CHILDREN}\n"+
			"	"+f.getCanonicalPath()+"[-]: {}"
		);
	} finally {
		if(f != null) {
			deleteResource(f);
		}
		this.deleteProject("P");
		stopDeltas();
	}
}
/**
 * - add an internal jar to claspath
 * - remove internal jar and the same jar as external jar
 * - touch the extenal jar
 * - refresh the JavaModel
 */
public void testExternalJarInternalExternalJar() throws CoreException, IOException {
	File f = null;
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {""}, "");

		String internalFooPath = "/P/foo.jar";
		IFile fooIFile = this.createFile(internalFooPath, new byte[0]);

		setClasspath(project, new IClasspathEntry[]{JavaCore.newLibraryEntry(new Path(internalFooPath), null, null)});
		getJavaModel().refreshExternalArchives(null,null);
		waitUntilIndexesReady();
		startDeltas();

		// canonicalize the external path as this is not done on case sensitive platforms when creating a new lib entry
		IPath externalFooPath = new Path(fooIFile.getLocation().toFile().getCanonicalPath());
		setClasspath(project, new IClasspathEntry[]{JavaCore.newLibraryEntry(externalFooPath, null, null)});

		project.open(null); // ensure that project is opened, as external jar delta is optimized for this case (if the project is not opened, there is no need for broadcasting an external jar delta)
		f = new File(externalFooPath.toOSString());
		touch(f);
		getJavaModel().refreshExternalArchives(null,null);

		String externalFooPathString = f.getCanonicalPath();
		assertDeltas(
			"Unexpected delta",
			"P[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n"+
			"	foo.jar[*]: {REMOVED FROM CLASSPATH}\n"+
			"	"+externalFooPathString+"[+]: {}\n"+
			"	ResourceDelta(/P/.classpath)[*]\n"+
			"\n"+
			"P[*]: {CHILDREN}\n"+
			"	"+externalFooPathString+"[*]: {CONTENT | ARCHIVE CONTENT CHANGED}"
		);
	} finally {
		if(f != null) {
			deleteResource(f);
		}
		this.deleteProject("P");
		stopDeltas();
	}
}
}

