/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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
 *     Terry Parker <tparker@google.com> - DeltaProcessor misses state changes in archive files, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=357425,
 *     									   Fup of 357425: ensure all reported regressions are witnessed by tests, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=361922
 *     Thirumala Reddy Mutchukota <thirumala@google.com> - Contribution for
 *     							Bug 411423 - JavaProject.resolveClasspath is spending more than 90% time on ExternalFoldersManager.isExternalFolderPath
 *     Stephan Herrmann - Contribution for
 *								Bug 440477 - [null] Infrastructure for feeding external annotations into compilation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.InvalidPathException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;
import junit.framework.Test;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.tests.model.ClasspathInitializerTests.DefaultVariableInitializer;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.UserLibraryClasspathContainer;
import org.eclipse.jdt.internal.core.builder.State;
import org.eclipse.team.core.RepositoryProvider;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ClasspathTests extends ModifyingResourceTests {
	private static final IClasspathAttribute ATTR_IGNORE_OPTIONAL_PROBLEMS_TRUE = JavaCore.newClasspathAttribute(IClasspathAttribute.IGNORE_OPTIONAL_PROBLEMS, "true");
	private static final IClasspathAttribute ATTR_IGNORE_OPTIONAL_PROBLEMS_FALSE = JavaCore.newClasspathAttribute(IClasspathAttribute.IGNORE_OPTIONAL_PROBLEMS, "false");

	public static class TestContainer implements IClasspathContainer {
		IPath path;
		IClasspathEntry[] entries;
		TestContainer(IPath path, IClasspathEntry[] entries){
			this.path = path;
			this.entries = entries;
		}
		public IPath getPath() { return this.path; }
		public IClasspathEntry[] getClasspathEntries() { return this.entries;	}
		public String getDescription() { return this.path.toString(); 	}
		public int getKind() { return 0; }
	}

public ClasspathTests(String name) {
	super(name);
}
// Use this static initializer to specify subset for tests
// All specified tests which do not belong to the class are skipped...
static {
	// Names of tests to run: can be "testBugXXXX" or "BugXXXX")
//	TESTS_PREFIX = "testClasspathDuplicateExtraAttribute";
//	TESTS_NAMES = new String[] {"testClasspathValidation42"};
//	TESTS_NUMBERS = new int[] { 23, 28, 38 };
//	TESTS_RANGE = new int[] { 21, 38 };
}
public static Test suite() {
	return buildModelTestSuite(ClasspathTests.class, BYTECODE_DECLARATION_ORDER);
}
@Override
public void setUpSuite() throws Exception {
	super.setUpSuite();
	setupExternalJCL("jclMin");
	setupExternalJCL("jclMin1.8");
	setupExternalJCL("jclMin23");
}

void restoreAutobuild(IWorkspaceDescription preferences, boolean autoBuild) throws CoreException {
	preferences.setAutoBuilding(autoBuild);
	getWorkspace().setDescription(preferences);
	if(autoBuild) {
		try {
			// wait before autobuild job will be scheduled
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// ignore
		}
	}
	waitForAutoBuild();
}

protected void assertCycleMarkers(IJavaProject project, IJavaProject[] p, int[] expectedCycleParticipants, boolean includeAffected) throws CoreException {
	waitForAutoBuild();
	StringBuilder expected = new StringBuilder("{");
	int expectedCount = 0;
	StringBuilder computed = new StringBuilder("{");
	int computedCount = 0;
	int mask = includeAffected ? 3 : 1;
	for (int j = 0; j < p.length; j++){
		int markerFlags = cycleMarkerFlags(p[j]);
		if ((markerFlags & mask) > 0){
			if (computedCount++ > 0) computed.append(", ");
			computed.append(p[j].getElementName());
			//computed.append(" (" + markerCount + ")");
		}
		markerFlags = expectedCycleParticipants[j];
		if ((markerFlags & mask) > 0){
			if (expectedCount++ > 0) expected.append(", ");
			expected.append(p[j].getElementName());
			//expected.append(" (" + markerCount + ")");
		}
	}
	expected.append("}");
	computed.append("}");
	assertEquals("Invalid cycle detection after setting classpath for: "+project.getElementName(), expected.toString(), computed.toString());
}
private void assertEncodeDecodeEntry(String projectName, String expectedEncoded, IClasspathEntry entry) {
	IJavaProject project = getJavaProject(projectName);
	String encoded = project.encodeClasspathEntry(entry);
	assertSourceEquals(
		"Unexpected encoded entry",
		expectedEncoded,
		encoded);
	IClasspathEntry decoded = project.decodeClasspathEntry(encoded);
	assertEquals(
		"Unexpected decoded entry",
		entry,
		decoded);
}
protected File createFile(File parent, String name, String content) throws IOException {
	File file = new File(parent, name);
	try (FileOutputStream out = new FileOutputStream(file)) {
		out.write(content.getBytes());
	}
	/*
	 * Need to change the time stamp to realize that the file has been modified
	 */
	file.setLastModified(System.currentTimeMillis() + 2000);
	return file;
}
/** @return 1: participates in cycle, 2: affected by cycle (depends on) */
protected int cycleMarkerFlags(IJavaProject javaProject) throws CoreException {
	IMarker[] markers = javaProject.getProject().findMarkers(IJavaModelMarker.BUILDPATH_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);
	int result = 0;
	for (int i = 0, length = markers.length; i < length; i++) {
		IMarker marker = markers[i];
		String cycleAttr = (String)marker.getAttribute(IJavaModelMarker.CYCLE_DETECTED);
		if (cycleAttr != null && cycleAttr.equals("true")){ //$NON-NLS-1$
			String message = marker.getAttribute(IMarker.MESSAGE, "");
			boolean isCycleMember = message.indexOf("\n->{") != -1; // cycle with no prefix
			if (isCycleMember)
				result |= 1;
			else
				result |= 2;
		}
	}
	return result;
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=171136, ignore class path errors from optional
// class path entries.

public void test124117() throws CoreException {

	IJavaProject p = null;
	try {

		p = this.createJavaProject("P0", new String[] {"src0", "src1"}, "bin0");
		IClasspathAttribute attribute = JavaCore.newClasspathAttribute(IClasspathAttribute.OPTIONAL, "true");
		JavaCore.setClasspathContainer(
				new Path("container/default"),
				new IJavaProject[]{ p },
				new IClasspathContainer[] {
					new TestContainer(new Path("container/default"),
							new IClasspathEntry[]{
						JavaCore.newLibraryEntry(new Path("/P0/JUNK"), new Path("/P0/SBlah"), new Path("/P0"), null, new IClasspathAttribute[] {attribute}, false)})
				},
				null);

		IClasspathEntry newClasspath = JavaCore.newContainerEntry(new Path("container/default"));

		IJavaModelStatus status = JavaConventions.validateClasspathEntry(p, newClasspath, true);

		assertStatus(
				"should NOT have complained about missing library as it is optional",
				"OK",
				status);

	} finally {
		deleteProject ("P0");
	}
}

/*  https://bugs.eclipse.org/bugs/show_bug.cgi?id=232816: Misleading problem text for missing jar in user
 *  library. We now mention the container name in this and related diagnostics so the context and connection is clearer.
 *  The bunch of tests with names of the form test232816*() test several paths in the function
 *  org.eclipse.jdt.internal.core.ClasspathEntry.validateLibraryEntry(IPath, IJavaProject, String, IPath, String)
 *  with the sole objective of eliciting the various messages under the different conditions (internal/external,
 *  file/folder, problem with library/sources etc). The tests probably make not much sense other than to trigger
 *  errors.
 */

public void test232816() throws Exception {

	IJavaProject p = null;
	try {
		p = createJavaProject("P");
		JavaCore.setClasspathContainer(
			new Path("container/default"),
			new IJavaProject[]{ p },
			new IClasspathContainer[] {
				new TestContainer(new Path("container/default"),
				new IClasspathEntry[]{
					JavaCore.newLibraryEntry(new Path(getExternalResourcePath("libjar.jar")), new Path("/P0/SBlah"), new Path("/P0"))})
			},
			null);

		IClasspathEntry newClasspath = JavaCore.newContainerEntry(new Path("container/default"));

		IJavaModelStatus status = JavaConventions.validateClasspathEntry(p, newClasspath, true);
		assertStatus(
			"should have complained about missing library",
			"The container \'container/default\' references non existing library \'" + getExternalResourcePath("libjar.jar") + "'",
			status);
	} finally {
		deleteProject("P");
	}
}


public void test232816a() throws Exception {

	IJavaProject p = null;
	try {
		p = createJavaProject("P");
		addExternalLibrary(p, getExternalResourcePath("lib1.jar"), new String[0],
			new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" +
				"Class-Path: lib2.jar\n",
			},
			CompilerOptions.getFirstSupportedJavaVersion());
		refreshExternalArchives(p);

		JavaCore.setClasspathContainer(
			new Path("container/default"),
			new IJavaProject[]{ p },
			new IClasspathContainer[] {
				new TestContainer(new Path("container/default"),
					new IClasspathEntry[]{
						JavaCore.newLibraryEntry(new Path(getExternalResourcePath("lib1.jar")), new Path("/P0/SBlah"), new Path("/P0"))})
			},
			null);

		IClasspathEntry newClasspath = JavaCore.newContainerEntry(new Path("container/default"));

		IJavaModelStatus status = JavaConventions.validateClasspathEntry(p, newClasspath, true);
		assertStatus(
			"should have complained about source attachment",
			"Invalid source attachment: '/P0/SBlah' for required library \'" + getExternalResourcePath("lib1.jar") + "' in the container 'container/default'",
			status);
	} finally {
		deleteProject("P");
		deleteExternalResource("lib1.jar");
	}
}

public void test232816b() throws Exception {

	try {
		IJavaProject p = createJavaProject("Project");
		// Create new user library "SomeUserLibrary"
		ClasspathContainerInitializer initializer= JavaCore.getClasspathContainerInitializer(JavaCore.USER_LIBRARY_CONTAINER_ID);
		String libraryName = "SomeUserLibrary";
		IPath containerPath = new Path(JavaCore.USER_LIBRARY_CONTAINER_ID);
		UserLibraryClasspathContainer containerSuggestion = new UserLibraryClasspathContainer(libraryName);
		initializer.requestClasspathContainerUpdate(containerPath.append(libraryName), null, containerSuggestion);

		// Modify user library
		IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(JavaCore.PLUGIN_ID);
		String propertyName = JavaModelManager.CP_USERLIBRARY_PREFERENCES_PREFIX+"SomeUserLibrary";
		StringBuilder propertyValue = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<userlibrary systemlibrary=\"false\" version=\"1\">\r\n<archive");
		//String jarFullPath = getWorkspaceRoot().getLocation().append(jarFile.getFullPath()).toString();
		propertyValue.append(" path=\"" + getExternalResourcePath("idontexistthereforeiamnot.jar"));
		propertyValue.append("\"/>\r\n</userlibrary>\r\n");
		preferences.put(propertyName, propertyValue.toString());
		preferences.flush();


		IClasspathEntry[] entries = p.getRawClasspath();
		int length = entries.length;
		System.arraycopy(entries, 0, entries = new IClasspathEntry[length+1], 0, length);
		entries[length] = JavaCore.newContainerEntry(containerSuggestion.getPath());
		p.setRawClasspath(entries, null);

		assertBuildPathMarkers("Failed to find marker", "The user library 'SomeUserLibrary' references non existing library \'" + getExternalResourcePath("idontexistthereforeiamnot.jar") + "'", p);
	} finally {
		deleteProject("Project");
	}
}


public void test232816c() throws CoreException {

	IJavaProject p = null;
	try {

		p = this.createJavaProject("P0", new String[] {"src0", "src1"}, "bin0");

		JavaCore.setClasspathContainer(
			new Path("container/default"),
			new IJavaProject[]{ p },
			new IClasspathContainer[] {
				new TestContainer(new Path("container/default"),
					new IClasspathEntry[]{
						JavaCore.newLibraryEntry(new Path("/P0/JUNK"), new Path("/P0/SBlah"), new Path("/P0"))})
			},
			null);

		IClasspathEntry newClasspath = JavaCore.newContainerEntry(new Path("container/default"));

		IJavaModelStatus status = JavaConventions.validateClasspathEntry(p, newClasspath, true);
		assertStatus(
			"should have complained about missing library",
			"The container 'container/default' references non existing library 'JUNK'",
			status);

	} finally {
		deleteProject ("P0");
	}
}


public void test232816d() throws CoreException {

	IJavaProject p = null;
	try {

		p = this.createJavaProject("P0", new String[] {"src0", "src1"}, "bin0");

		JavaCore.setClasspathContainer(
		new Path("container/default"),
			new IJavaProject[]{ p },
			new IClasspathContainer[] {
				new TestContainer(new Path("container/default"),
					new IClasspathEntry[]{
						JavaCore.newLibraryEntry(new Path("/P0/src0"), new Path("/P0/SBlah"), new Path("/P0"))})
			},
			null);

		IClasspathEntry newClasspath = JavaCore.newContainerEntry(new Path("container/default"));

		IJavaModelStatus status = JavaConventions.validateClasspathEntry(p, newClasspath, true);
		assertStatus(
			"should have complained about source attachment",
			"Invalid source attachment: '/P0/SBlah' for required library '/P0/src0' in the container 'container/default'",
			status);

	} finally {
		deleteProject ("P0");
	}
}

public void test232816e() throws CoreException {

	IJavaProject p = null;
	try {
		p = this.createJavaProject("P0", new String[] {"src0", "src1"}, "bin0");
		createFile("/P0/src0/X.class", "");
		JavaCore.setClasspathContainer(
		new Path("container/default"),
			new IJavaProject[]{ p },
			new IClasspathContainer[] {
				new TestContainer(new Path("container/default"),
					new IClasspathEntry[]{
						JavaCore.newLibraryEntry(new Path("/P0/src0/X.class"), new Path("/P0/SBlah"), new Path("/P0"))})
			},
			null);

		IClasspathEntry newClasspath = JavaCore.newContainerEntry(new Path("container/default"));

		IJavaModelStatus status = JavaConventions.validateClasspathEntry(p, newClasspath, true);
		assertStatus(
			"should have complained about source attachment",
			"Invalid source attachment: '/P0/SBlah' for required library '/P0/src0/X.class' in the container 'container/default'",
			status);

	} finally {
		deleteProject ("P0");
	}
}

public void test232816f() throws Exception {

	IJavaProject p = null;
	try {
		p = createJavaProject("P");
		setUpProjectCompliance(p, CompilerOptions.getFirstSupportedJavaVersion(), true);
		p.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.getFirstSupportedJavaVersion());
		p.setOption(JavaCore.CORE_INCOMPATIBLE_JDK_LEVEL, JavaCore.WARNING);

		JavaCore.setClasspathContainer(
			new Path("container/default"),
			new IJavaProject[]{ p },
			new IClasspathContainer[] {
				new TestContainer(new Path("container/default"),
					new IClasspathEntry[]{
						JavaCore.newLibraryEntry(getExternalJCLPath(CompilerOptions.getLatestVersion()), new Path("/P0/SBlah"), new Path("/P0"))})
			},
			null);

		IClasspathEntry newClasspath = JavaCore.newContainerEntry(new Path("container/default"));

		IJavaModelStatus status = JavaConventions.validateClasspathEntry(p, newClasspath, true);
		assertStatus(
			"should have complained about jdk level mismatch",
			"Incompatible .class files version in required binaries. Project 'P' is targeting a " + CompilerOptions.getFirstSupportedJavaVersion()
					+ " runtime, but is compiled against \'" + getExternalJCLPath("23").makeRelative() + "' (from the container 'container/default') which requires a 23 runtime",
			status);
	} finally {
		deleteProject("P");
	}
}

/*
 * Ensures that adding a library entry for an existing external library folder doesn't generate a marker
 */
public void testAddExternalLibFolder1() throws CoreException {
	try {
		IJavaProject p = createJavaProject("P");
		createExternalFolder("externalLib");
		setClasspath(p, new IClasspathEntry[] {JavaCore.newLibraryEntry(new Path(getExternalResourcePath("externalLib")), null, null)});
		assertBuildPathMarkers("Unexpected markers", "", p);
	} finally {
		deleteExternalResource("externalLib");
		deleteProject("P");
	}
}

/*
 * Ensures that creating a project with a library entry for an existing external library folder doesn't generate a marker
 */
public void testAddExternalLibFolder2() throws CoreException {
	try {
		createExternalFolder("externalLib");
		IJavaProject p = createJavaProject("P", new String[0], new String[] {getExternalResourcePath("externalLib")}, "");
		assertBuildPathMarkers("Unexpected markers", "", p);
	} finally {
		deleteExternalResource("externalLib");
		deleteProject("P");
	}
}

/*
 * Ensures that adding a library entry for a non-existing external library folder generates a marker
 */
public void testAddExternalLibFolder3() throws CoreException {
	try {
		waitForAutoBuild();
		IJavaProject p = createJavaProject("P");
		IPath path = new Path(getExternalResourcePath("externalLib"));
		setClasspath(p, new IClasspathEntry[] {JavaCore.newLibraryEntry(path, null, null)});
		assertBuildPathMarkers(
			"Unexpected markers",
			"Project \'P\' is missing required library: \'"+ getExternalPath() + "externalLib\'",
			p);
	} finally {
		deleteProject("P");
	}
}

/*
 * Ensures that creating a project with a library entry for a non-existing external library folder generates a marker
 */
public void testAddExternalLibFolder4() throws CoreException {
	try {
		waitForAutoBuild();
		IJavaProject p = createJavaProject("P", new String[0], new String[] {getExternalResourcePath("externalLib")}, "");
		assertBuildPathMarkers(
			"Unexpected markers",
			"Project \'P\' is missing required library: \'"+ getExternalPath() + "externalLib\'",
			p);
	} finally {
		deleteProject("P");
	}
}

/*
 * Ensures that creating an external library folder referenced by a library entry and refreshing removes the marker
 */
public void testAddExternalLibFolder5() throws CoreException {
	try {
		IJavaProject p = createJavaProject("P", new String[0], new String[] {getExternalResourcePath("externalLib")}, "");
		waitForAutoBuild();
		createExternalFolder("externalLib");
		refresh(p);
		assertBuildPathMarkers("Unexpected markers", "", p);
	} finally {
		deleteExternalResource("externalLib");
		deleteProject("P");
	}
}

/*
 * Ensures that creating an external library folder referenced by a library entry and refreshing after a restart
 * removes the marker
 */
public void testAddExternalLibFolder6() throws CoreException {
	try {
		simulateExitRestart();
		IJavaProject p = createJavaProject("P", new String[0], new String[] {getExternalResourcePath("externalLib")}, "");
		waitForAutoBuild();
		createExternalFolder("externalLib");
		refresh(p);
		assertBuildPathMarkers("Unexpected markers", "", p);
	} finally {
		deleteExternalResource("externalLib");
		deleteProject("P");
	}
}

/*
 * Ensures that creating an external library folder referenced by a library entry and refreshing the workspace doesn't log a NPE
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=262363 )
 */
public void testAddExternalLibFolder7() throws CoreException {
	try {
		createJavaProject("P", new String[0], new String[] {getExternalResourcePath("externalLib")}, "");
		waitForAutoBuild();
		createExternalFolder("externalLib");
		startLogListening(null/*listen to Platform's log*/);
		getWorkspaceRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
		waitForManualRefresh();
		assertLogEquals("");
	} finally {
		stopLogListening();
		deleteExternalResource("externalLib");
		deleteProject("P");
	}
}

/*
 * Ensures that adding a library entry for an existing external ZIP archive doesn't generate a marker
 */
public void testAddZIPArchive1() throws CoreException, IOException {
	try {
		IJavaProject p = createJavaProject("P");
		org.eclipse.jdt.core.tests.util.Util.createEmptyJar(
				getExternalResourcePath("externalLib.abc"),
				CompilerOptions.getFirstSupportedJavaVersion());
		setClasspath(p, new IClasspathEntry[] {JavaCore.newLibraryEntry(new Path(getExternalResourcePath("externalLib.abc")), null, null)});
		assertBuildPathMarkers("Unexpected markers", "", p);
	} finally {
		deleteExternalResource("externalLib.abc");
		deleteProject("P");
	}
}

/*
 * Ensures that creating a project with a library entry for an existing external ZIP archive doesn't generate a marker
 */
public void testAddZIPArchive2() throws CoreException, IOException {
	try {
		org.eclipse.jdt.core.tests.util.Util.createEmptyJar(
				getExternalResourcePath("externalLib.abc"),
				CompilerOptions.getFirstSupportedJavaVersion());
		IJavaProject p = createJavaProject("P", new String[0], new String[] {getExternalResourcePath("externalLib.abc")}, "");
		assertBuildPathMarkers("Unexpected markers", "", p);
	} finally {
		deleteExternalResource("externalLib.abc");
		deleteProject("P");
	}
}

/*
 * Ensures that adding a library entry for a non-existing external ZIP archive generates a marker
 */
public void testAddZIPArchive3() throws CoreException {
	try {
		IJavaProject p = createJavaProject("P");
		refreshExternalArchives(p);
		waitForAutoBuild();

		setClasspath(p, new IClasspathEntry[] {JavaCore.newLibraryEntry(new Path(getExternalResourcePath("externalLib.abc")), null, null)});
		assertBuildPathMarkers(
			"Unexpected markers",
			"Project \'P\' is missing required library: \'"+ getExternalPath() + "externalLib.abc\'",
			p);
	} finally {
		deleteProject("P");
	}
}

/*
 * Ensures that creating a project with a library entry for a non-existing external ZIP archive generates a marker
 */
public void testAddZIPArchive4() throws CoreException {
	try {
		waitForAutoBuild();

		IJavaProject p = createJavaProject("P", new String[0], new String[] {getExternalResourcePath("externalLib.abc")}, "");
		assertBuildPathMarkers(
			"Unexpected markers",
			"Project \'P\' is missing required library: \'"+ getExternalPath() + "externalLib.abc\'",
			p);
	} finally {
		deleteProject("P");
	}
}

/*
 * Ensures that creating an external ZIP archive referenced by a library entry and refreshing removes the marker
 */
public void testAddZIPArchive5() throws CoreException, IOException {
	try {
		IJavaProject p = createJavaProject("P", new String[0], new String[] {getExternalResourcePath("externalLib.abc")}, "");
		refreshExternalArchives(p);
		waitForAutoBuild();
		org.eclipse.jdt.core.tests.util.Util.createEmptyJar(
				getExternalResourcePath("externalLib.abc"),
				CompilerOptions.getFirstSupportedJavaVersion());
		refreshExternalArchives(p);
		assertBuildPathMarkers("Unexpected markers", "", p);
	} finally {
		deleteExternalResource("externalLib.abc");
		deleteProject("P");
	}
}

/*
 * Ensures that creating an external ZIP archive referenced by a library entry and refreshing after a restart
 * removes the marker
 */
public void testAddZIPArchive6() throws CoreException, IOException {
	try {
		simulateExitRestart();
		IJavaProject p = createJavaProject("P", new String[0], new String[] {getExternalResourcePath("externalLib.abc")}, "");
		refreshExternalArchives(p);
		org.eclipse.jdt.core.tests.util.Util.createEmptyJar(
				getExternalResourcePath("externalLib.abc"),
				CompilerOptions.getFirstSupportedJavaVersion());
		refreshExternalArchives(p);
		assertBuildPathMarkers("Unexpected markers", "", p);
	} finally {
		deleteExternalResource("externalLib.abc");
		deleteProject("P");
	}
}

/*
 * Ensures that adding a library entry for an existing internal ZIP archive doesn't generate a marker
 */
public void testAddZIPArchive7() throws CoreException, IOException {
	try {
		IJavaProject p = createJavaProject("P");
		refreshExternalArchives(p);
		waitForAutoBuild();
		addLibrary(p, "internalLib.abc", null, new String[0],
				new String[]{"META-INF/MANIFEST.MF",
					"Manifest-Version: 1.0\n"} ,
					CompilerOptions.getFirstSupportedJavaVersion());
		setClasspath(p, new IClasspathEntry[] {JavaCore.newLibraryEntry(new Path("/P/internalLib.abc"), null, null)});
		assertBuildPathMarkers("Unexpected markers", "", p);
	} finally {
		deleteProject("P");
	}
}

/*
 * Ensures that adding a project to a container triggers a classpath delta
 * (regression test for 154071 No notification of change if a project is added or removed from a container)
 */
public void testAddProjectToContainer() throws CoreException {
	try {
		createJavaProject("P1");
		TestContainer container = new TestContainer(
			new Path("org.eclipse.jdt.core.tests.model.container/default"),
			new IClasspathEntry[] {});
		IJavaProject p2 = createJavaProject("P2", new String[] {}, new String[] {container.getPath().toString()}, "");
		JavaCore.setClasspathContainer(container.getPath(), new IJavaProject[] {p2}, new IClasspathContainer[] {container}, null);

		container = new TestContainer(
			new Path("org.eclipse.jdt.core.tests.model.container/default"),
			new IClasspathEntry[] {JavaCore.newProjectEntry(new Path("/P1"))});

		startDeltas();
		JavaCore.setClasspathContainer(container.getPath(), new IJavaProject[] {p2}, new IClasspathContainer[] {container}, null);
		assertDeltas(
			"Unexpected delta",
			"P2[*]: {RESOLVED CLASSPATH CHANGED}"
		);
	} finally {
		stopDeltas();
		deleteProject("P1");
		deleteProject("P2");
	}
}

/**
 * Add an entry to the classpath for a non-existent root. Then create
 * the root and ensure that it comes alive.
 */
public void testAddRoot1() throws CoreException {
	IJavaProject project = this.createJavaProject("P", new String[] {"src"}, "bin");
	IClasspathEntry[] originalCP= project.getRawClasspath();

	try {
		IClasspathEntry newEntry= JavaCore.newSourceEntry(project.getProject().getFullPath().append("extra"));

		IClasspathEntry[] newCP= new IClasspathEntry[originalCP.length + 1];
		System.arraycopy(originalCP, 0 , newCP, 0, originalCP.length);
		newCP[originalCP.length]= newEntry;

		project.setRawClasspath(newCP, null);

		// now create the actual resource for the root and populate it
		project.getProject().getFolder("extra").create(false, true, null);

		IPackageFragmentRoot newRoot= getPackageFragmentRoot("P", "extra");
		assertTrue("New root should now be visible", newRoot != null);
	} finally {
		// cleanup
		this.deleteProject("P");
	}
}

/*
 * Adds an entry to the classpath for a non-existent root. Then creates
 * the root and ensures that the marker is removed.
 * (regression test for bug 161581 Adding a missing folder doesn't remove classpath marker)
 */
public void testAddRoot2() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {}, "bin");
		project.setRawClasspath(createClasspath("P", new String[] {"/P/src", ""}), null);
		waitForAutoBuild();

		// now create the actual resource for the root
		project.getProject().getFolder("src").create(false, true, null);
		assertBuildPathMarkers("Unexpected markers", "", project);
	} finally {
		// cleanup
		this.deleteProject("P");
	}
}

/*
 * Ensures that changing the raw classpath and the containers so that the resolved classpath doesn't change
 * generates the correct delta.
 */
public void testChangeRawButNotResolvedClasspath() throws CoreException {
	try {
		final String containerPath1 = "org.eclipse.jdt.core.tests.model.container/con1";
		final TestContainer container1 = new TestContainer(
			new Path(containerPath1),
			new IClasspathEntry[] {JavaCore.newLibraryEntry(new Path("/P/lib1.jar"), null, null)});
		final String containerPath2 = "org.eclipse.jdt.core.tests.model.container/con2";
		final TestContainer container2 = new TestContainer(
			new Path(containerPath2),
			new IClasspathEntry[] {JavaCore.newLibraryEntry(new Path("/P/lib2.jar"), null, null)});
		final IJavaProject p = createJavaProject("P", new String[] {}, new String[] {containerPath1, containerPath2}, "");
		createFile("/P/lib1.jar", "");
		createFile("/P/lib2.jar", "");
		JavaCore.setClasspathContainer(container1.getPath(), new IJavaProject[] {p}, new IClasspathContainer[] {container1}, null);
		JavaCore.setClasspathContainer(container2.getPath(), new IJavaProject[] {p}, new IClasspathContainer[] {container2}, null);

		startDeltas();
		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				IClasspathEntry[] newRawClasspath = new IClasspathEntry[] {
					JavaCore.newContainerEntry(new Path(containerPath2)),
					JavaCore.newContainerEntry(new Path(containerPath1))
				};
				p.setRawClasspath(newRawClasspath, monitor);
				JavaCore.setClasspathContainer(container1.getPath(), new IJavaProject[] {p}, new IClasspathContainer[] {container2}, null);
				JavaCore.setClasspathContainer(container2.getPath(), new IJavaProject[] {p}, new IClasspathContainer[] {container1}, null);
			}
		};
		JavaCore.run(runnable, null);
		assertDeltas(
			"Unexpected delta",
			"P[*]: {CONTENT | RAW CLASSPATH CHANGED}\n" +
			"	ResourceDelta(/P/.classpath)[*]"
		);
	} finally {
		stopDeltas();
		deleteProject("P");
	}
}

/**
 * Ensures that the reordering external resources in the classpath
 * generates the correct deltas.
 */
public void testClasspathChangeExternalResources() throws CoreException {
	try {
		IJavaProject proj = this.createJavaProject("P", new String[] {"src"}, "bin");

		IClasspathEntry[] newEntries = new IClasspathEntry[2];
		newEntries[0] = JavaCore.newLibraryEntry(getExternalJCLPath(), null, null, false);
		newEntries[1] = JavaCore.newLibraryEntry(getExternalJCLSourcePath(), null, null, false);
		setClasspath(proj, newEntries);
		startDeltas();
		IClasspathEntry[] swappedEntries = new IClasspathEntry[2];
		swappedEntries[0] = newEntries[1];
		swappedEntries[1] = newEntries[0];
		setClasspath(proj, swappedEntries);
		assertDeltas(
			"Unexpected delta",
			"P[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
			"	"+  getExternalJCLPathString() +"[*]: {REORDERED}\n" +
			"	"+  getExternalJCLSourcePathString() +"[*]: {REORDERED}\n" +
			"	ResourceDelta(/P/.classpath)[*]"
		);
	} finally {
		stopDeltas();
		this.deleteProject("P");
	}
}

/*
 * Test classpath corruption (23977)
 */
public void testClasspathCorruption() throws CoreException {
	try {
		JavaProject p1 = (JavaProject)this.createJavaProject("P1", new String[]{""}, new String[]{}, new String[]{}, "");
		this.createJavaProject("P2", new String[]{""}, new String[]{}, new String[]{}, "");
		this.createFile("P2/foo.txt", "not a project");
		String newCPContent =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n"
			+"<classpath>	\n"
			+"	<classpathentry kind=\"src\" path=\"\"/>	\n"
			+"	<classpathentry kind=\"src\" path=\"/P2/foo.txt\"/>	\n" // corruption here: target isn't a project
			+"	<classpathentry kind=\"output\" path=\"\"/>	\n"
			+"</classpath>	\n";

		IFile fileRsc = p1.getProject().getFile(JavaProject.CLASSPATH_FILENAME);
		fileRsc.setContents(newCPContent.getBytes(), true, false, null);
/*
		File file = p1.getProject().getFile(JavaProject.CLASSPATH_FILENAME).getLocation().toFile();
		if (file.exists()){
			char[] classpath = Util.getFileCharContent(file, "UTF-8");
			System.out.println(new String(classpath));
		}
*/
		p1.close();
		JavaModelManager.PerProjectInfo perProjectInfo = JavaModelManager.getJavaModelManager().getPerProjectInfo(p1.getProject(), true/*create if missing*/);
		perProjectInfo.setRawClasspath(null, null, null);

		// shouldn't fail
		p1.getExpandedClasspath();

		// if could reach that far, then all is fine

	} catch(ClassCastException e){
		assertTrue("internal ClassCastException on corrupted classpath file", false);
	} finally {
		// cleanup
		this.deleteProject("P1");
		this.deleteProject("P2");
	}
}

/*
 * Test classpath read for non-java project or java project not opened yet (40658)
 */
public void testClasspathFileRead() throws CoreException {
	try {
		final IProject proj = createProject("P1");
		String newCPContent =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n"
			+"<classpath>	\n"
			+"	<classpathentry kind=\"src\" path=\"src\"/>	\n"
			+"	<classpathentry kind=\"output\" path=\"bin\"/>	\n"
			+"</classpath>	\n";

		this.createFile("/P1/"+JavaProject.CLASSPATH_FILENAME, newCPContent);
		final IJavaProject jproj = JavaCore.create(proj);
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
			workspace.run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor)	{

					IClasspathEntry[] entries = jproj.readRawClasspath(); // force to read classpath
					IClasspathEntry entry = entries[0];
					assertEquals("first classpath entry should have been read", "/P1/src", entry.getPath().toString());

					assertEquals("output location should have been read", "/P1/bin", jproj.readOutputLocation().toString());
				}
			}, null);
	} finally {
		// cleanup
		this.deleteProject("P1");
	}
}

/*
 * Test classpath forced reload (20931) and new way to read classpath file (40658)
 */
public void testClasspathForceReload() throws CoreException {
	try {
		final JavaProject p1 = (JavaProject)this.createJavaProject("P1", new String[]{""}, new String[]{}, new String[]{}, "");
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
			workspace.run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor)	throws CoreException {

					p1.getRawClasspath(); // force to read classpath
					createFolder("P1/src");
					createFolder("P1/bin");
					String newCPContent =
						"<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n"
						+"<classpath>	\n"
						+"	<classpathentry kind=\"src\" path=\"src\"/>	\n"
						+"	<classpathentry kind=\"output\" path=\"bin\"/>	\n"
						+"</classpath>	\n";

					IFile fileRsc = p1.getProject().getFile(JavaProject.CLASSPATH_FILENAME);
					fileRsc.setContents(newCPContent.getBytes(), true, false, null);

					p1.close();
					assertEquals("output location should not have been refreshed", "/P1", p1.getOutputLocation().toString());

					p1.setRawClasspath(p1.readRawClasspath(), p1.readOutputLocation(), null);
					assertEquals("output location should have been refreshed", "/P1/bin", p1.getOutputLocation().toString());
				}
			}, null);
	} finally {
		// cleanup
		this.deleteProject("P1");
	}
}

/**
 * Ensures that the setting the classpath with a library entry
 * changes the kind of the root from K_SOURCE to K_BINARY.
 */
public void testClasspathCreateLibraryEntry() throws CoreException {
	try {
		IJavaProject proj = this.createJavaProject("P", new String[] {"src"}, "bin");
		this.createFile("P/src/X.java", "public class X {}");
		this.createFile("P/src/X.class", "");

		IFolder rootFolder = proj.getProject().getFolder(new Path("src"));
		IPackageFragmentRoot root = proj.getPackageFragmentRoot(rootFolder);

		assertEquals(
			"Unexpected root kind 1",
			IPackageFragmentRoot.K_SOURCE,
			root.getKind());
		IPackageFragment pkg = root.getPackageFragment("");
		assertEquals(
			"Unexpected numbers of compilation units",
			1,
			pkg.getCompilationUnits().length);
		assertEquals(
			"Unexpected numbers of .class files",
			0,
			pkg.getAllClassFiles().length);

		setClasspath(
			proj,
			new IClasspathEntry[] {
				JavaCore.newLibraryEntry(rootFolder.getFullPath(), null, null, false)
			});
		assertEquals(
			"Unexpected root kind 2",
			IPackageFragmentRoot.K_BINARY,
			root.getKind());
		assertEquals(
			"Unexpected numbers of compilation units",
			0,
			pkg.getCompilationUnits().length);
		assertEquals(
			"Unexpected numbers of .class files",
			1,
			pkg.getAllClassFiles().length);

		//ensure that the new kind has been persisted in the classpath file
		proj.close();
		assertEquals(
			"Unexpected root kind 3",
			IPackageFragmentRoot.K_BINARY,
			root.getKind());

	} finally {
		this.deleteProject("P");
	}
}
/**
 * Ensures that the setting the classpath with a new library entry for a
 * local jar works and generates the correct deltas.
 */
public void testClasspathCreateLocalJarLibraryEntry() throws CoreException {
	IJavaProject proj = this.createJavaProject("P", new String[] {""}, "");
	IClasspathEntry newEntry= JavaCore.newLibraryEntry(getExternalJCLPath(), null, null, false);
	IClasspathEntry[] newEntries= new IClasspathEntry[]{newEntry};

	startDeltas();

	setClasspath(proj,newEntries);

	try {
		assertDeltas(
			"Unexpected delta",
			"P[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
			"	<project root>[*]: {REMOVED FROM CLASSPATH}\n" +
			"	"+ getExternalJCLPathString() + "[+]: {}\n" +
			"	ResourceDelta(/P/.classpath)[*]"
		);
	} finally {
		stopDeltas();

		this.deleteProject("P");
	}
}

/**
 * Tests the cross project classpath setting
 */
public void testClasspathCrossProject() throws CoreException {
	IJavaProject project = this.createJavaProject("P1", new String[] {""}, "");
	this.createJavaProject("P2", new String[] {}, "");
	try {
		startDeltas();
		IPackageFragmentRoot oldRoot= getPackageFragmentRoot("P1", "");
 		IClasspathEntry projectEntry= JavaCore.newProjectEntry(new Path("/P2"), false);
		IClasspathEntry[] newClasspath= new IClasspathEntry[]{projectEntry};
		project.setRawClasspath(newClasspath, null);
		project.getAllPackageFragmentRoots();
		IJavaElementDelta removedDelta= this.deltaListener.getDeltaFor(oldRoot, true);
		assertDeltas(
			"Unexpected delta",
			"<project root>[*]: {REMOVED FROM CLASSPATH}",
			removedDelta);
	} finally {
		stopDeltas();
		deleteProjects(new String[] {"P1", "P2"});
	}
}
/**
 * Delete a root and ensure the classpath is not updated (i.e. entry isn't removed).
 */
public void testClasspathDeleteNestedRoot() throws CoreException {
	IJavaProject project = this.createJavaProject("P", new String[] {"nested/src"}, new String[] {getExternalJCLPathString()}, "bin");
	IPackageFragmentRoot root= getPackageFragmentRoot("P", "nested/src");
	IClasspathEntry[] originalCP= project.getRawClasspath();

	// delete the root
	deleteResource(root.getUnderlyingResource());

	IClasspathEntry[] newCP= project.getRawClasspath();

	try {
		// should still be an entry for the "src" folder
		assertTrue("classpath should not have been updated",
			newCP.length == 2 &&
			newCP[0].equals(originalCP[0]) &&
			newCP[1].equals(originalCP[1]));
	} finally {
		this.deleteProject("P");
	}
}
/*
 * Test classpath diamond (23979)
 */
public void testClasspathDiamond() throws CoreException {
	try {
		this.createJavaProject("P1", new String[]{""}, "");
		this.createJavaProject("P2", new String[]{""}, new String[]{}, new String[]{"/P1"}, "");
		this.createJavaProject("P3", new String[]{""}, new String[]{}, new String[]{"/P1", "/P2"}, "");
		IJavaProject p4 = this.createJavaProject("P4", new String[]{""}, new String[]{}, new String[]{"/P2", "/P3"}, "");

		assertTrue("Should not detect cycle", !p4.hasClasspathCycle(null));

	} finally {
		// cleanup
		deleteProjects(new String[] {"P1", "P2", "P3", "P4"});
	}
}

/**
 * Delete a nested root's parent folder and ensure the classpath is
 * not updated (i.e. entry isn't removed).
 */
public void testClasspathDeleteNestedRootParent() throws CoreException {
	IJavaProject project = this.createJavaProject("P", new String[] {"nested/src"}, new String[] {getExternalJCLPathString()}, "bin");
	IPackageFragmentRoot root= getPackageFragmentRoot("P", "nested/src");
	IClasspathEntry[] originalCP= project.getRawClasspath();

	// delete the root's parent folder
	IFolder folder= (IFolder)root.getUnderlyingResource().getParent();
	deleteResource(folder);

	IClasspathEntry[] newCP= project.getRawClasspath();

	try {

		// should still be an entry for the "src" folder
		assertTrue("classpath should not have been updated",
			newCP.length == 2 &&
			newCP[0].equals(originalCP[0]) &&
			newCP[1].equals(originalCP[1]));
	} finally {
		this.deleteProject("P");
	}
}
/*
 * Ensure that a classpath entry for an external jar is externalized
 * properly.
 */
public void testExternalize1() throws CoreException { // was testClasspathExternalize
	try {
		IJavaProject project= this.createJavaProject("P", new String[] {}, new String[] {getExternalJCLPathString()}, "");
		IClasspathEntry[] classpath= project.getRawClasspath();
		IClasspathEntry jar= null;
		for (int i= 0; i < classpath.length; i++) {
			if (classpath[i].getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
				jar= classpath[i];
				break;
			}
		}
		project.close();
		project.open(null);

		classpath= project.getRawClasspath();
		for (int i= 0; i < classpath.length; i++) {
			if (classpath[i].getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
				assertTrue("Paths must be the same", classpath[i].getPath().equals(jar.getPath()));
				break;
			}
		}
	} finally {
		this.deleteProject("P");
	}
}
/*
 * Ensures that a classpath entry for an external library is externalized properly.
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=220542 )
 */
public void testExternalize2() throws CoreException {
	String externalLibPath = getExternalResourcePath("externalLib") + File.separator;
	IClasspathEntry entry = JavaCore.newLibraryEntry(new Path(externalLibPath), null, null);
	assertEquals("Unexpected external path", externalLibPath, entry.getPath().toOSString());
}
/**
 * Move a root and ensure the classpath is not updated (i.e. entry not renamed).
 */
public void testClasspathMoveNestedRoot() throws CoreException {
	IJavaProject project = this.createJavaProject("P", new String[] {"nested/src"}, new String[] {getExternalJCLPathString()}, "bin");
	IPackageFragmentRoot root= getPackageFragmentRoot("P", "nested/src");
	IClasspathEntry[] originalCP= project.getRawClasspath();

	// delete the root
	IFolder folder= (IFolder)root.getUnderlyingResource();
	IPath originalPath= folder.getFullPath();
	IPath newPath= originalPath.removeLastSegments(1);
	newPath= newPath.append(new Path("newsrc"));

	startDeltas();

	folder.move(newPath, true, null);

	IClasspathEntry[] newCP= project.getRawClasspath();

	IPackageFragmentRoot newRoot= project.getPackageFragmentRoot(project.getProject().getFolder("nested").getFolder("newsrc"));

	try {
		// entry for the "src" folder wasn't replaced
		assertTrue("classpath not automatically updated", newCP.length == 2 &&
			newCP[1].equals(originalCP[1]) &&
			newCP[0].equals(originalCP[0]));

		IJavaElementDelta rootDelta = this.deltaListener.getDeltaFor(root, true);
		IJavaElementDelta projectDelta = this.deltaListener.getDeltaFor(newRoot.getParent(), true);
		assertTrue("should get delta for moved root", rootDelta != null &&
				rootDelta.getKind() == IJavaElementDelta.REMOVED &&
				rootDelta.getFlags() == 0);
		assertTrue("should get delta indicating content changed for project", deltaContentChanged(projectDelta));

	} finally {
		stopDeltas();
		this.deleteProject("P");
	}
}

/**
 * Move a parent of a nested root and ensure the classpath is not updated (i.e. entry not renamed).
 */
public void testClasspathMoveNestedRootParent() throws CoreException {
	try {
		IJavaProject project =this.createJavaProject("P", new String[] {"nested/src"}, new String[] {getExternalJCLPathString()}, "bin");
		IPackageFragmentRoot root= getPackageFragmentRoot("P", "nested/src");
		IClasspathEntry[] originalCP= project.getRawClasspath();

		// delete the root
		IFolder folder= (IFolder)root.getUnderlyingResource().getParent();
		IPath originalPath= folder.getFullPath();
		IPath newPath= originalPath.removeLastSegments(1);
		newPath= newPath.append(new Path("newsrc"));
		folder.move(newPath, true, null);

		IClasspathEntry[] newCP= project.getRawClasspath();

		// entry for the "src" folder wasn't replaced
		// entry for the "src" folder should not be replaced
		assertTrue("classpath should not automatically be updated", newCP.length == 2 &&
			newCP[1].equals(originalCP[1]) &&
			newCP[0].equals(originalCP[0]));

	} finally {
		this.deleteProject("P");
	}
}
/**
 * Tests that nothing occurs when setting to the same classpath
 */
public void testClasspathNoChanges() throws CoreException {
	try {
		IJavaProject p = this.createJavaProject("P", new String[] {""}, "");
		IClasspathEntry[] oldClasspath= p.getRawClasspath();
		startDeltas();
		p.setRawClasspath(oldClasspath, null);
		assertDeltas("Unexpected delta", "");
	} finally {
		stopDeltas();
		this.deleteProject("P");
	}
}
/**
 * Ensures that the setting the classpath with a reordered classpath generates
 * the correct deltas.
 */
public void testClasspathReordering() throws CoreException {
	IJavaProject proj = this.createJavaProject("P", new String[] {"src"}, new String[] {getExternalJCLPathString()}, "bin");
	IClasspathEntry[] originalCP = proj.getRawClasspath();
	try {
		IClasspathEntry[] newEntries = new IClasspathEntry[originalCP.length];
		int index = originalCP.length - 1;
		for (int i = 0; i < originalCP.length; i++) {
			newEntries[index] = originalCP[i];
			index--;
		}
		startDeltas();
		setClasspath(proj, newEntries);
		assertDeltas(
			"Unexpected delta",
			"P[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
			"	src[*]: {REORDERED}\n" +
			"	"+ getExternalJCLPathString() + "[*]: {REORDERED}\n" +
			"	ResourceDelta(/P/.classpath)[*]"
		);
	} finally {
		stopDeltas();
		this.deleteProject("P");
	}
}

/**
 * Should detect duplicate entries on the classpath
 */
public void testClasspathValidation01() throws CoreException {
	try {
		IJavaProject proj = this.createJavaProject("P", new String[] {"src"}, "bin");
		IClasspathEntry[] originalCP = proj.getRawClasspath();

		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = newCP[0];

		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());

		assertStatus(
			"should have detected duplicate entries on the classpath",
			"Build path contains duplicate entry: \'src\' for project 'P'",
			status);
	} finally {
		this.deleteProject("P");
	}
}

/**
 * Should detect nested source folders on the classpath
 */
public void testClasspathValidation02() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {"src"}, "bin");
		IClasspathEntry[] originalCP = proj.getRawClasspath();

		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P"));

		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());

		assertStatus(
			"should have detected nested source folders on the classpath",
			"Cannot nest \'P/src\' inside \'P\'. To enable the nesting exclude \'src/\' from \'P\'",
			status);
	} finally {
		this.deleteProject("P");
	}
}

/**
 * Should detect library folder nested inside source folder on the classpath
 */
public void testClasspathValidation03() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {"src"}, "bin");
		IClasspathEntry[] originalCP = proj.getRawClasspath();

		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newLibraryEntry(new Path("/P/src/lib"), null, null);
		createFolder("/P/src/lib");

		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());

		assertStatus(
			"should have detected library folder nested inside source folder on the classpath",
			"Cannot nest \'P/src/lib\' inside \'P/src\'. To enable the nesting exclude \'lib/\' from \'P/src\'",
			status);
	} finally {
		this.deleteProject("P");
	}
}

public void testClasspathValidation04() throws CoreException {

	IJavaProject[] p = null;
	try {

		p = new IJavaProject[]{
			this.createJavaProject("P0", new String[] {"src0"}, "bin0"),
			this.createJavaProject("P1", new String[] {"src1"}, "bin1"),
		};

		JavaCore.setClasspathVariable("var", new Path("/P1"), null);

		IClasspathEntry[] newClasspath = new IClasspathEntry[]{
			JavaCore.newSourceEntry(new Path("/P0/src0")),
			JavaCore.newVariableEntry(new Path("var/src1"), null, null),
		};

		// validate classpath
		IJavaModelStatus status = JavaConventions.validateClasspath(p[0], newClasspath, p[0].getOutputLocation());
		assertStatus(
			"should not detect external source folder through a variable on the classpath",
			"OK",
			status);

	} finally {
		deleteProjects(new String[] {"P0", "P1"});
	}
}

public void testClasspathValidation05() throws CoreException {

	IJavaProject[] p = null;
	try {

		p = new IJavaProject[]{
			this.createJavaProject("P0", new String[] {"src0", "src1"}, "bin0"),
			this.createJavaProject("P1", new String[] {"src1"}, "bin1"),
		};

		JavaCore.setClasspathContainer(
		new Path("container/default"),
			new IJavaProject[]{ p[0] },
			new IClasspathContainer[] {
				new TestContainer(new Path("container/default"),
					new IClasspathEntry[]{
						JavaCore.newSourceEntry(new Path("/P0/src0")),
						JavaCore.newVariableEntry(new Path("var/src1"), null, null) })
			},
			null);

		IClasspathEntry[] newClasspath = new IClasspathEntry[]{
			JavaCore.newSourceEntry(new Path("/P0/src1")),
			JavaCore.newContainerEntry(new Path("container/default")),
		};

		// validate classpath
		IJavaModelStatus status = JavaConventions.validateClasspath(p[0], newClasspath, p[0].getOutputLocation());
		assertStatus(
			"should not have detected external source folder through a container on the classpath",
			"OK",
			status);

		// validate classpath entry
		status = JavaConventions.validateClasspathEntry(p[0], newClasspath[1], true);
		assertStatus(
			"should have detected external source folder through a container on the classpath",
			"Invalid classpath container: 'container/default' in project 'P0'",
			status);

	} finally {
		deleteProjects(new String[] {"P0", "P1"});
	}
}

public void testClasspathValidation06() throws CoreException {

	IJavaProject[] p = null;
	try {

		p = new IJavaProject[]{
			this.createJavaProject("P0", new String[] {"src"}, "src"),
		};

		// validate classpath entry
		IClasspathEntry[] newClasspath = new IClasspathEntry[]{
			JavaCore.newSourceEntry(new Path("/P0")),
			JavaCore.newSourceEntry(new Path("/P0/src")),
		};

		IJavaModelStatus status = JavaConventions.validateClasspath(p[0], newClasspath, p[0].getOutputLocation());
		assertStatus(
			"should have detected nested source folder",
			"Cannot nest \'P0/src\' inside \'P0\'. To enable the nesting exclude \'src/\' from \'P0\'",
			status);
	} finally {
		this.deleteProject("P0");
	}
}
/**
 * Should allow nested source folders on the classpath as long as the outer
 * folder excludes the inner one.
 */
public void testClasspathValidation07() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {"src"}, "bin");
		IClasspathEntry[] originalCP = proj.getRawClasspath();

		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P"), new IPath[] {new Path("src/")});

		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());

		assertStatus(
			"should have allowed nested source folders with exclusion on the classpath",
			"OK",
			status);
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Should allow a nested binary folder in a source folder on the classpath as
 * long as the outer folder excludes the inner one.
 */
public void testClasspathValidation08() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {}, new String[] {"lib"}, "bin");
		IClasspathEntry[] originalCP = proj.getRawClasspath();

		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P"), new IPath[] {new Path("lib/")});

		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());

		assertStatus(
			"should have allowed nested lib folders with exclusion on the classpath",
			"OK",
			status);
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Should not allow a nested source folder in the project's output folder.
 */
public void testClasspathValidation09() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {}, "bin");
		IClasspathEntry[] originalCP = proj.getRawClasspath();

		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P/bin/src"));

		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());

		assertStatus(
			"should not allow nested source folder in putput folder",
			"Cannot nest \'P/bin/src\' inside output folder \'P/bin\'",
			status);
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Should not allow a nested output folder in a source folder on the classpath.
 */
public void testClasspathValidation10() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {"src"}, "bin");
		IClasspathEntry[] originalCP = proj.getRawClasspath();

		IJavaModelStatus status = JavaConventions.validateClasspath(proj, originalCP, new Path("/P/src/bin"));

		assertStatus(
			"should not allow nested output folder in source folder",
			"Cannot nest output folder \'P/src/bin\' inside \'P/src\'",
			status);
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Should allow a nested library folder in the project's output folder if the project's output is not used.
*/
public void testClasspathValidation11() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {}, "");
		IClasspathEntry[] originalCP = proj.getRawClasspath();

		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newLibraryEntry(new Path("/P/lib"), null, null);

		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());

		assertStatus(
			"should allow nested library folder in output folder",
			"OK",
			status);
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Should not allow a nested source folder in an output folder.
 */
public void testClasspathValidation12() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {}, "bin1");
		IClasspathEntry[] originalCP = proj.getRawClasspath();

		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] =
			JavaCore.newSourceEntry(
				new Path("/P/bin2/src"),
				new IPath[] {},
				new Path("/P/bin2"));

		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());

		assertStatus(
			"should not allow nested source folder in output folder",
			"Cannot nest \'P/bin2/src\' inside output folder \'P/bin2\'",
			status);
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Should not allow a nested output folder in a source folder on the classpath.
 */
public void testClasspathValidation13() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {}, "bin1");
		IClasspathEntry[] originalCP = proj.getRawClasspath();

		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] =
			JavaCore.newSourceEntry(
				new Path("/P/src"),
				new IPath[] {},
				new Path("/P/src/bin2"));

		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());

		assertStatus(
			"should not allow nested output folder in source folder",
			"Cannot nest output folder \'P/src/bin2\' inside \'P/src\'",
			status);
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Should allow a nested output folder in a source folder that coincidate with the project.
 */
public void testClasspathValidation14() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {}, "bin");
		IClasspathEntry[] originalCP = proj.getRawClasspath();

		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] =
			JavaCore.newSourceEntry(
				new Path("/P"),
				new IPath[] {},
				new Path("/P/bin"));

		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());

		assertStatus(
			"should allow nested output folder in source folder which is project",
			"OK",
			status);
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Should not allow nested source folders on the classpath if exclusion filter has no trailing slash.
 */
public void testClasspathValidation15() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {"src"}, "bin");
		IClasspathEntry[] originalCP = proj.getRawClasspath();

		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P"), new IPath[] {new Path("**/src")});

		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());

		assertStatus(
			"End exclusion filter \'src\' with / to fully exclude \'P/src\'",
			status);
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Should allow custom output folder to be nested in default output folder if default output is not used.
 * (regression test for bug 28596 Default output folder cause of validation error even if not used)
 */
public void testClasspathValidation16() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {}, "");
		IClasspathEntry[] originalCP = proj.getRawClasspath();

		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P"), new IPath[0], new Path("/P/bin"));

		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());

		assertStatus(
			"OK",
			status);
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Should not allow source folder to be nested in default output folder if default output is used.
 */
public void testClasspathValidation17() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {"src1"}, "bin");
		IClasspathEntry[] originalCP = proj.getRawClasspath();

		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P/bin/src2"));

		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());

		assertStatus(
			"Cannot nest \'P/bin/src2\' inside output folder \'P/bin\'",
			status);
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Should not allow custom output folder to be external to project.
 * (regression test for bug 29079 Buildpath validation: No check that output folder is inside project)
 */
public void testClasspathValidation18() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {}, "");
		IClasspathEntry[] originalCP = proj.getRawClasspath();

		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P/src"), new IPath[0], new Path("/S/bin"));

		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());

		assertStatus(
			"Path \'/S/bin\' must denote location inside project 'P'",
			status);
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Should detect source folder nested inside library folder on the classpath
 */
public void testClasspathValidation19() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {}, new String[] {"lib"}, "bin");
		IClasspathEntry[] originalCP = proj.getRawClasspath();

		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P/lib/src"));

		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());

		assertStatus(
			"should have detected library folder nested inside source folder on the classpath",
			"Cannot nest \'P/lib/src\' inside library \'P/lib\'",
			status);
	} finally {
		this.deleteProject("P");
	}
}

/**
 * Should not allow custom output folder if project preference disallow them
 */
public void testClasspathValidation20() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {}, "");
		IClasspathEntry[] originalCP = proj.getRawClasspath();

		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P/src"), new IPath[0], new Path("/S/bin"));

		Map options = new Hashtable(5);
		options.put(JavaCore.CORE_ENABLE_CLASSPATH_MULTIPLE_OUTPUT_LOCATIONS, JavaCore.DISABLED);
		proj.setOptions(options);
		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());

		assertStatus(
			"Multiple output locations are disabled in project 'P', cannot associate entry: \'src\' with a specific output",
			status);
	} finally {
		this.deleteProject("P");
	}
}

/**
 * Should not allow exclusion patterns if project preference disallow them
 */
public void testClasspathValidation21() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {}, "");
		IClasspathEntry[] originalCP = proj.getRawClasspath();

		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P/src"), new IPath[]{new Path("**/src")}, null);

		Map options = new Hashtable(5);
		options.put(JavaCore.CORE_ENABLE_CLASSPATH_EXCLUSION_PATTERNS, JavaCore.DISABLED);
		proj.setOptions(options);
		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());

		assertStatus(
			"Inclusion or exclusion patterns are disabled in project 'P', cannot selectively include or exclude from entry: \'src\'",
			status);
	} finally {
		this.deleteProject("P");
	}
}

/**
 * 33207 - Reject output folder that coincidate with distinct source folder
 */
public void testClasspathValidation22() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {}, "");
		proj.setOption(JavaCore.CORE_OUTPUT_LOCATION_OVERLAPPING_ANOTHER_SOURCE, JavaCore.ERROR);
		IClasspathEntry[] originalCP = proj.getRawClasspath();

		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+2];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P/src"), new IPath[0], new Path("/P/src2"));
		newCP[originalCP.length+1] = JavaCore.newSourceEntry(new Path("/P/src2"), new IPath[0], new Path("/P/src"));

		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());

		assertStatus(
			"Source folder \'src\' in project 'P' cannot output to distinct source folder \'src2\'",
			status);
	} finally {
		this.deleteProject("P");
	}
}

/**
 * 33207 - Reject output folder that coincidate with distinct source folder
 * but 36465 - Unable to create multiple source folders when not using bin for output
 * default output scenarii is still tolerated
 */
public void testClasspathValidation23() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {}, "");
		proj.setOption(JavaCore.CORE_OUTPUT_LOCATION_OVERLAPPING_ANOTHER_SOURCE, JavaCore.IGNORE);
		IClasspathEntry[] originalCP = proj.getRawClasspath();

		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+2];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P/"), new IPath[]{new Path("src/")}, null);
		newCP[originalCP.length+1] = JavaCore.newSourceEntry(new Path("/P/src"), new IPath[0], null);

		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());

		assertStatus(
			"OK",
			status);
//		assertStatus(
//			"Source folder 'P/src' cannot output to distinct source folder 'P/'.",
//			status);
	} finally {
		this.deleteProject("P");
	}
}

/**
 * Ensure one cannot nest source entry inside default output folder
 */
public void testClasspathValidation24() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {}, "");
		IClasspathEntry[] originalCP = proj.getRawClasspath();

		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P/src"), new IPath[0], null);

		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());

		assertStatus(
			"Cannot nest \'P/src\' inside output folder \'P\'",
			status);
	} finally {
		this.deleteProject("P");
	}
}

/**
 * Reject output folder that coincidate with library folder
 */
public void testClasspathValidation25() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {}, "");
		IClasspathEntry[] originalCP = proj.getRawClasspath();

		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+2];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P/src"), new IPath[0], new Path("/P/lib2"));
		newCP[originalCP.length+1] = JavaCore.newLibraryEntry(new Path("/P/lib2"), null, null);

		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());

		assertStatus(
			"Source folder \'src\' in project 'P' cannot output to library \'lib2\'",
			status);
	} finally {
		this.deleteProject("P");
	}
}

/**
 * Reject output folder that coincidate with library folder
 * default output scenarii
 */
public void testClasspathValidation26() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {}, "");
		IClasspathEntry[] originalCP = proj.getRawClasspath();

		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+2];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newLibraryEntry(new Path("/P/"), null, null);
		newCP[originalCP.length+1] = JavaCore.newSourceEntry(new Path("/P/src"), new IPath[0], null);

		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());

		assertStatus(
			"Cannot nest \'P/src\' inside library \'P/\'",
			status);
	} finally {
		this.deleteProject("P");
	}
}

/**
 * Checks diagnosis for incompatible binary versions
 */
public void testClasspathValidation27() throws CoreException {
	try {
		IJavaProject proj1 =  this.createJavaProject("P1", new String[] {}, "");
		proj1.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.getFirstSupportedJavaVersion());

		IJavaProject proj2 =  this.createJavaProject("P2", new String[] {}, "");
		proj2.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_1);
		proj2.setOption(JavaCore.CORE_INCOMPATIBLE_JDK_LEVEL, JavaCore.WARNING);

		IJavaModelStatus status = JavaConventions.validateClasspathEntry(proj2, JavaCore.newProjectEntry(new Path("/P1")), false);
		assertStatus(
			"Incompatible .class files version in required binaries. Project \'P2\' is targeting a 1.1 runtime, but is compiled against \'P1\' which requires a " + CompilerOptions.getFirstSupportedJavaVersion() + " runtime",
			status);
	} finally {
		deleteProjects(new String[]{"P1", "P2"});
	}
}
/**
 * bug 159325: Any idea why ClasspathEntry checks for string object reference instead of equals
 * test Ensure that validation is correctly done even for other strings than JavaCore constants...
 * 	Note that it's needed to change JavaCore options as "ignore" is the default value and set option
 * 	to this value on java project will just remove it instead of putting another string object...
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=159325"
 */
public void testClasspathValidation27_Bug159325_project() throws CoreException {
	Hashtable javaCoreOptions = JavaCore.getOptions();
	try {
		IJavaProject proj1 =  this.createJavaProject("P1", new String[] {}, "");
		proj1.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.getFirstSupportedJavaVersion());

		Hashtable options = JavaCore.getOptions();
		options.put(JavaCore.CORE_INCOMPATIBLE_JDK_LEVEL, JavaCore.WARNING);
		JavaCore.setOptions(options);
		IJavaProject proj2 =  this.createJavaProject("P2", new String[] {}, "");
		proj2.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_1);
		proj2.setOption(JavaCore.CORE_INCOMPATIBLE_JDK_LEVEL, new String("ignore".toCharArray()));

		IJavaModelStatus status = JavaConventions.validateClasspathEntry(proj2, JavaCore.newProjectEntry(new Path("/P1")), false);
		assertStatus("OK", status);
	} finally {
		JavaCore.setOptions(javaCoreOptions);
		deleteProjects(new String[]{"P1", "P2"});
	}
}
public void testClasspathValidation27_Bug159325_lib() throws CoreException {
	Hashtable javaCoreOptions = JavaCore.getOptions();
	try {
		IJavaProject proj =  this.createJavaProject("P1", new String[] {}, "");
		proj.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_1);

		Hashtable options = JavaCore.getOptions();
		options.put(JavaCore.CORE_INCOMPATIBLE_JDK_LEVEL, JavaCore.WARNING);
		JavaCore.setOptions(options);
		proj.setOption(JavaCore.CORE_INCOMPATIBLE_JDK_LEVEL, new String("ignore".toCharArray()));

		IClasspathEntry library = JavaCore.newLibraryEntry(new Path(getExternalJCLPathString(JavaCore.VERSION_1_8)), null, null, ClasspathEntry.NO_ACCESS_RULES, ClasspathEntry.NO_EXTRA_ATTRIBUTES, false);
		IJavaModelStatus status = JavaConventions.validateClasspathEntry(proj, library, false);
		assertStatus("OK", status);
	} finally {
		JavaCore.setOptions(javaCoreOptions);
		deleteProjects(new String[]{"P1", "P2"});
	}
}

/**
 * Checks it is legal for an output folder to be an excluded subfolder of some source folder
 */
public void testClasspathValidation28() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {}, "");
		IClasspathEntry[] originalCP = proj.getRawClasspath();

		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P/src"), new IPath[]{ new Path("output/") }, new Path("/P/src/output"));

		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());

		assertStatus(
			"Source folder \'src\' in project 'P' should be allowed to output to excluded source subfolder \'src/output\'",
			"OK",
			status);
	} finally {
		this.deleteProject("P");
	}
}

/**
 * Checks it is illegal for an output folder to be a subfolder of some source folder
 */
public void testClasspathValidation29() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {}, "");
		IClasspathEntry[] originalCP = proj.getRawClasspath();

		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P/src"), new IPath[0], new Path("/P/src/output"));

		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());

		assertStatus(
			"Cannot nest output folder \'P/src/output\' inside \'P/src\'",
			status);
	} finally {
		this.deleteProject("P");
	}
}

/**
 * Checks it is illegal for a nested output folder (sitting inside excluded range of source folder) to be enclosing another source folder
 */
public void testClasspathValidation30() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {}, "bin");
		IClasspathEntry[] originalCP = proj.getRawClasspath();

		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+2];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P/src1"), new IPath[]{new Path("bin/")}, new Path("/P/src1/bin"));
		newCP[originalCP.length+1] = JavaCore.newSourceEntry(new Path("/P/src1/bin/src2"));

		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());

		assertStatus(
			"Cannot nest \'P/src1/bin/src2\' inside output folder \'P/src1/bin\'",
			status);
	} finally {
		this.deleteProject("P");
	}
}

/**
 * Checks it is illegal for a nested output folder (sitting inside excluded range of source folder) to be nested in another source folder
 */
public void testClasspathValidation31() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {}, "bin");
		IClasspathEntry[] originalCP = proj.getRawClasspath();

		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+2];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P/src1"), new IPath[]{new Path("src2/")}, new Path("/P/src1/src2/bin"));
		newCP[originalCP.length+1] = JavaCore.newSourceEntry(new Path("/P/src1/src2"));

		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());

		assertStatus(
			"Cannot nest output folder \'P/src1/src2/bin\' inside \'P/src1/src2\'",
			status);
	} finally {
		this.deleteProject("P");
	}
}

/**
 * Checks it is illegal for a nested output folder (sitting inside excluded range of source folder) to be coincidating with another source folder
 */
public void testClasspathValidation32() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {}, "bin");
		IClasspathEntry[] originalCP = proj.getRawClasspath();

		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+2];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P/src1"), new IPath[]{new Path("src2/")}, new Path("/P/src1/src2"));
		newCP[originalCP.length+1] = JavaCore.newSourceEntry(new Path("/P/src1/src2"));

		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());

		assertStatus(
			"Source folder \'src1\' in project 'P' cannot output to distinct source folder \'src1/src2\'",
			status);
	} finally {
		this.deleteProject("P");
	}
}

/**
 * Checks it is illegal for a source folder to be nested in an output folder (42579)
 */
public void testClasspathValidation33() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {}, "");
		IClasspathEntry[] originalCP = proj.getRawClasspath();

		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P/dir/src"), new IPath[]{new Path("src2/")}, new Path("/P/dir"));

		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());

		assertStatus(
			"Cannot nest \'P/dir/src\' inside output folder \'P/dir\'",
			status);
	} finally {
		this.deleteProject("P");
	}
}

/**
 * Should not allow nested source folders on the classpath if the outer
 * folder includes the inner one.
 */
public void testClasspathValidation34() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {"src"}, "bin");
		IClasspathEntry[] originalCP = proj.getRawClasspath();

		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P"), new IPath[] {new Path("src/")}, new IPath[0], null);

		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());

		assertStatus(
			"should not have allowed nested source folders with inclusion on the classpath",
			"Cannot nest \'P/src\' inside \'P\'. To enable the nesting exclude \'src/\' from \'P\'",
			status);
	} finally {
		this.deleteProject("P");
	}
}

/**
 * Should not allow a nested binary folder in a source folder on the classpath
 * if the outer folder includes the inner one.
 */
public void testClasspathValidation35() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {}, new String[] {"lib"}, "bin");
		IClasspathEntry[] originalCP = proj.getRawClasspath();

		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P"), new IPath[] {new Path("lib/")}, new Path[0], null);

		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());

		assertStatus(
			"should not have allowed nested lib folders with inclusion on the classpath",
			"Cannot nest \'P/lib\' inside \'P\'. To enable the nesting exclude \'lib/\' from \'P\'",
			status);
	} finally {
		this.deleteProject("P");
	}
}

/**
 * Should allow nested source folders on the classpath if inclusion filter has no trailing slash.
 */
public void testClasspathValidation36() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {"src"}, "bin");
		IClasspathEntry[] originalCP = proj.getRawClasspath();

		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P"), new IPath[] {new Path("**/src")}, new Path[0], null);

		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());

		assertStatus(
			"OK",
			status);
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Should not allow inclusion patterns if project preference disallow them
 */
public void testClasspathValidation37() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {}, "");
		IClasspathEntry[] originalCP = proj.getRawClasspath();

		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P/src"), new IPath[]{new Path("**/src")}, new Path[0], null);

		Map options = new Hashtable(5);
		options.put(JavaCore.CORE_ENABLE_CLASSPATH_EXCLUSION_PATTERNS, JavaCore.DISABLED);
		proj.setOptions(options);
		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());

		assertStatus(
			"Inclusion or exclusion patterns are disabled in project 'P', cannot selectively include or exclude from entry: \'src\'",
			status);
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Checks it is illegal for an output folder to be an included subfolder of some source folder
 */
public void testClasspathValidation38() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {}, "");
		IClasspathEntry[] originalCP = proj.getRawClasspath();

		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P/src"), new IPath[]{ new Path("output/") }, new Path[0], new Path("/P/src/output"));

		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());

		assertStatus(
			"Source folder \'src\' in project 'P' should not be allowed to output to included source subfolder \'src/output\'",
			"Cannot nest output folder \'P/src/output\' inside \'P/src\'",
			status);
	} finally {
		this.deleteProject("P");
	}
}
/**
 * 62713 - check nested output folder detection
 */
public void testClasspathValidation39() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {}, "");
		IClasspathEntry[] originalCP = proj.getRawClasspath();

		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+2];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P"), new IPath[]{new Path("test/")}, new Path("/P/bin/test"));
		newCP[originalCP.length+1] = JavaCore.newSourceEntry(new Path("/P/test"), new IPath[]{}, new Path("/P/bin"));

		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());

		assertStatus(
			"Cannot nest output folder \'P/bin/test\' inside output folder \'P/bin\'",
			status);
	} finally {
		this.deleteProject("P");
	}
}
/**
 * 62713 - variation
 */
public void testClasspathValidation40() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {}, "");
		IClasspathEntry[] originalCP = proj.getRawClasspath();

		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+2];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P"), new IPath[]{new Path("test/")}, new Path("/P/bin"));
		newCP[originalCP.length+1] = JavaCore.newSourceEntry(new Path("/P/test"), new IPath[]{}, new Path("/P/bin/test"));

		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());

		assertStatus(
			"Cannot nest output folder \'P/bin/test\' inside output folder \'P/bin\'",
			status);
	} finally {
		this.deleteProject("P");
	}
}
/**
 * 62713 - variation
 */
public void testClasspathValidation41() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {}, "bin");
		IClasspathEntry[] originalCP = proj.getRawClasspath();

		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P/src"), new IPath[]{}, new Path("/P/"));

		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());

		assertStatus(
			"Cannot nest \'P/src\' inside output folder \'P/\'",
			status);
	} finally {
		this.deleteProject("P");
	}
}
/*
 * Should detect nested source folders on the classpath and indicate the preference if disabled
 * (regression test for bug 122615 validate classpath propose to exlude a source folder even though exlusion patterns are disabled)
 */
public void testClasspathValidation42() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {"src"}, "bin");
		proj.setOption(JavaCore.CORE_ENABLE_CLASSPATH_EXCLUSION_PATTERNS, JavaCore.DISABLED);
		IClasspathEntry[] originalCP = proj.getRawClasspath();

		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P"));

		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());

		assertStatus(
			"should have detected nested source folders on the classpath",
			"Cannot nest \'P/src\' inside \'P\'. To allow the nesting enable use of exclusion patterns in the preferences of project \'P\' and exclude \'src/\' from \'P\'",
			status);
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Setting the classpath with two entries specifying the same path
 * should fail.
 */
public void testClasspathWithDuplicateEntries() throws CoreException {
	try {
		IJavaProject project =  this.createJavaProject("P", new String[] {"src"}, "bin");
		IClasspathEntry[] cp= project.getRawClasspath();
		IClasspathEntry[] newCp= new IClasspathEntry[cp.length *2];
		System.arraycopy(cp, 0, newCp, 0, cp.length);
		System.arraycopy(cp, 0, newCp, cp.length, cp.length);
		try {
			project.setRawClasspath(newCp, null);
		} catch (JavaModelException jme) {
			return;
		}
		assertTrue("Setting the classpath with two entries specifying the same path should fail", false);
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Bug 94404: [model] Disallow classpath attributes with same key
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=94404"
 */
public void testClasspathDuplicateExtraAttribute1() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P1", new String[] {}, "bin");
		IClasspathAttribute[] extraAttributes = new IClasspathAttribute[2];
		extraAttributes[0] = JavaCore.newClasspathAttribute("javadoc_location", "http://www.sample-url.org/doc/");
		extraAttributes[1] = JavaCore.newClasspathAttribute("javadoc_location", "d:/tmp");

		// Verify container entry validation
		IClasspathEntry container = JavaCore.newContainerEntry(new Path("JRE_CONTAINER"), ClasspathEntry.NO_ACCESS_RULES, extraAttributes, false);
		IJavaModelStatus status = JavaConventions.validateClasspathEntry(proj, container, false);
		assertStatus(
			"Duplicate extra attribute: \'javadoc_location\' in classpath entry \'JRE_CONTAINER\' for project 'P1'",
			status);
	} finally {
		deleteProject("P1");
	}
}

/**
 * Bug 94404: [model] Disallow classpath attributes with same key
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=94404"
 */
public void testClasspathDuplicateExtraAttribute2() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P1", new String[] {}, "bin");
		IClasspathAttribute[] extraAttributes = new IClasspathAttribute[2];
		extraAttributes[0] = JavaCore.newClasspathAttribute("javadoc_location", "http://www.sample-url.org/doc/");
		extraAttributes[1] = JavaCore.newClasspathAttribute("javadoc_location", "d:/tmp");

		// Verify library entry validation
		IClasspathEntry library = JavaCore.newLibraryEntry(new Path(getExternalJCLPathString()), null, null, ClasspathEntry.NO_ACCESS_RULES, extraAttributes, false);
		IJavaModelStatus status = JavaConventions.validateClasspathEntry(proj, library, false);
		assertStatus(
			"Duplicate extra attribute: \'javadoc_location\' in classpath entry \'"+getExternalJCLPath()+"\' for project 'P1'",
			status);
	} finally {
		deleteProject("P1");
	}
}

/**
 * Bug 94404: [model] Disallow classpath attributes with same key
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=94404"
 */
public void testClasspathDuplicateExtraAttribute3() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P1", new String[] {}, "bin");
		IClasspathAttribute[] extraAttributes = new IClasspathAttribute[2];
		extraAttributes[0] = JavaCore.newClasspathAttribute("javadoc_location", "http://www.sample-url.org/doc/");
		extraAttributes[1] = JavaCore.newClasspathAttribute("javadoc_location", "d:/tmp");

		// Verify project entry validation
		createJavaProject("P2");
		IClasspathEntry projectEntry = JavaCore.newProjectEntry(new Path("/P2"), ClasspathEntry.NO_ACCESS_RULES, false, extraAttributes, false);
		IJavaModelStatus status = JavaConventions.validateClasspathEntry(proj, projectEntry, false);
		assertStatus(
			"Duplicate extra attribute: \'javadoc_location\' in classpath entry \'/P2\' for project 'P1'",
			status);

	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}

/**
 * Bug 94404: [model] Disallow classpath attributes with same key
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=94404"
 */
public void testClasspathDuplicateExtraAttribute4() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P1", new String[] {}, "bin");
		IClasspathAttribute[] extraAttributes = new IClasspathAttribute[2];
		extraAttributes[0] = JavaCore.newClasspathAttribute("javadoc_location", "http://www.sample-url.org/doc/");
		extraAttributes[1] = JavaCore.newClasspathAttribute("javadoc_location", "d:/tmp");

		// Verify source entry validation
		createFolder("/P1/src");
		IClasspathEntry sourceEntry = JavaCore.newSourceEntry(new Path("/P1/src"), new IPath[0], new IPath[0], null, extraAttributes);
		IJavaModelStatus status = JavaConventions.validateClasspathEntry(proj, sourceEntry, false);
		assertStatus(
			"Duplicate extra attribute: \'javadoc_location\' in classpath entry \'src\' for project 'P1'",
			status);

	} finally {
		deleteProject("P1");
	}
}

/**
 * Bug 94404: [model] Disallow classpath attributes with same key
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=94404"
 */
public void testClasspathDuplicateExtraAttribute5() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P1", new String[] {}, "bin");
		IClasspathAttribute[] extraAttributes = new IClasspathAttribute[2];
		extraAttributes[0] = JavaCore.newClasspathAttribute("javadoc_location", "http://www.sample-url.org/doc/");
		extraAttributes[1] = JavaCore.newClasspathAttribute("javadoc_location", "d:/tmp");

		// Verify variable entry validation
		IClasspathEntry variable = JavaCore.newVariableEntry(new Path("JCL18_LIB"), new Path("JCL18_SRC"), null, ClasspathEntry.NO_ACCESS_RULES, extraAttributes, false);
		IJavaModelStatus status = JavaConventions.validateClasspathEntry(proj, variable, false);
		assertStatus(
			"Duplicate extra attribute: \'javadoc_location\' in classpath entry \'"+getExternalJCLPath()+"\' for project 'P1'",
			status);
	} finally {
		deleteProject("P1");
	}
}
/**
 * Adding an entry to the classpath for a library that does not exist
 * should not break the model. The classpath should contain the
 * entry, but the root should not appear in the children.
 */
public void testClasspathWithNonExistentLibraryEntry() throws CoreException {
	try {
		IJavaProject project=  this.createJavaProject("P", new String[] {"src"}, "bin");
		IClasspathEntry[] originalPath= project.getRawClasspath();
		IPackageFragmentRoot[] originalRoots= project.getPackageFragmentRoots();

		IClasspathEntry[] newPath= new IClasspathEntry[originalPath.length + 1];
		System.arraycopy(originalPath, 0, newPath, 0, originalPath.length);

		IClasspathEntry newEntry= JavaCore.newLibraryEntry(new Path("c:/nothing/nozip.jar").makeAbsolute(), null, null, false);
		newPath[originalPath.length]= newEntry;

		project.setRawClasspath(newPath, null);

		IClasspathEntry[] getPath= project.getRawClasspath();
		assertTrue("should be the same length", getPath.length == newPath.length);
		for (int i= 0; i < getPath.length; i++) {
			assertTrue("entries should be the same", getPath[i].equals(newPath[i]));
		}

		IPackageFragmentRoot[] newRoots= project.getPackageFragmentRoots();
		assertTrue("Should be the same number of roots", originalRoots.length == newRoots.length);
		for (int i= 0; i < newRoots.length; i++) {
			assertTrue("roots should be the same", originalRoots[i].equals(newRoots[i]));
		}
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Adding an entry to the classpath for a project that does not exist
 * should not break the model. The classpath should contain the
 * entry, but the root should not appear in the children.
 */
public void testClasspathWithNonExistentProjectEntry() throws CoreException {
	try {
		IJavaProject project= this.createJavaProject("P", new String[] {"src"}, "bin");
		IClasspathEntry[] originalPath= project.getRawClasspath();
		IPackageFragmentRoot[] originalRoots= project.getPackageFragmentRoots();

		IClasspathEntry[] newPath= new IClasspathEntry[originalPath.length + 1];
		System.arraycopy(originalPath, 0, newPath, 0, originalPath.length);

		IClasspathEntry newEntry= JavaCore.newProjectEntry(new Path("/NoProject"), false);
		newPath[originalPath.length]= newEntry;

		project.setRawClasspath(newPath, null);

		IClasspathEntry[] getPath= project.getRawClasspath();
		assertTrue("should be the same length", getPath.length == newPath.length);
		for (int i= 0; i < getPath.length; i++) {
			assertTrue("entries should be the same", getPath[i].equals(newPath[i]));
		}

		IPackageFragmentRoot[] newRoots= project.getPackageFragmentRoots();
		assertTrue("Should be the same number of roots", originalRoots.length == newRoots.length);
		for (int i= 0; i < newRoots.length; i++) {
			assertTrue("roots should be the same", originalRoots[i].equals(newRoots[i]));
		}
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Adding an entry to the classpath for a folder that does not exist
 * should not break the model. The classpath should contain the
 * entry, but the root should not appear in the children.
 */
public void testClasspathWithNonExistentSourceEntry() throws CoreException {
	try {
		IJavaProject project= this.createJavaProject("P", new String[] {"src"}, "bin");
		IClasspathEntry[] originalPath= project.getRawClasspath();
		IPackageFragmentRoot[] originalRoots= project.getPackageFragmentRoots();

		IClasspathEntry[] newPath= new IClasspathEntry[originalPath.length + 1];
		System.arraycopy(originalPath, 0, newPath, 0, originalPath.length);

		IClasspathEntry newEntry= JavaCore.newSourceEntry(new Path("/P/moreSource"));
		newPath[originalPath.length]= newEntry;

		project.setRawClasspath(newPath, null);

		IClasspathEntry[] getPath= project.getRawClasspath();
		assertTrue("should be the same length", getPath.length == newPath.length);
		for (int i= 0; i < getPath.length; i++) {
			assertTrue("entries should be the same", getPath[i].equals(newPath[i]));
		}

		IPackageFragmentRoot[] newRoots= project.getPackageFragmentRoots();
		assertTrue("Should be the same number of roots", originalRoots.length == newRoots.length);
		for (int i= 0; i < newRoots.length; i++) {
			assertTrue("roots should be the same", originalRoots[i].equals(newRoots[i]));
		}
	} finally {
		this.deleteProject("P");
	}
}

/**
 * Ensure that cycle are properly reported.
 */
public void testCycleReport() throws CoreException {

	try {
		IJavaProject p1 = this.createJavaProject("P1", new String[] {""}, "");
		IJavaProject p2 = this.createJavaProject("P2", new String[] {""}, "");
		IJavaProject p3 = this.createJavaProject("P3", new String[] {""}, new String[] {}, new String[] {"/P2"}, "");

		// Ensure no cycle reported
		IJavaProject[] projects = { p1, p2, p3 };
		int cycleMarkerCount = 0;
		for (int i = 0; i < projects.length; i++){
			cycleMarkerCount += (cycleMarkerFlags(projects[i]) & 1);
		}
		assertTrue("Should have no cycle markers", cycleMarkerCount == 0);

		// Add cycle
		IClasspathEntry[] originalP1CP= p1.getRawClasspath();
		IClasspathEntry[] originalP2CP= p2.getRawClasspath();

		// Add P1 as a prerequesite of P2
		int length = originalP2CP.length;
		IClasspathEntry[] newCP= new IClasspathEntry[length + 1];
		System.arraycopy(originalP2CP, 0 , newCP, 0, length);
		newCP[length]= JavaCore.newProjectEntry(p1.getProject().getFullPath(), false);
		p2.setRawClasspath(newCP, null);

		// Add P3 as a prerequesite of P1
		length = originalP1CP.length;
		newCP= new IClasspathEntry[length + 1];
		System.arraycopy(originalP1CP, 0 , newCP, 0, length);
		newCP[length]= JavaCore.newProjectEntry(p3.getProject().getFullPath(), false);
		p1.setRawClasspath(newCP, null);

		waitForAutoBuild(); // wait for cycle markers to be created
		cycleMarkerCount = 0;
		for (int i = 0; i < projects.length; i++){
			cycleMarkerCount += (cycleMarkerFlags(projects[i]) & 1);
		}
		assertEquals("Unexpected number of projects involved in a classpath cycle", 3, cycleMarkerCount);

	} finally {
		// cleanup
		deleteProjects(new String[] {"P1", "P2", "P3"});
	}
}
/**
 * Ensures that the default classpath and output locations are correct.
 * The default classpath should be the root of the project.
 * The default output location should be the root of the project.
 */
public void testDefaultClasspathAndOutputLocation() throws CoreException {
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {""}, "bin");
		IClasspathEntry[] classpath = project.getRawClasspath();
		assertTrue("Incorrect default classpath; to many entries", classpath.length == 1);
		assertTrue("Incorrect default classpath: " + classpath[0], classpath[0].getPath().equals(project.getUnderlyingResource().getFullPath()));
		IPath output = project.getOutputLocation();
		assertTrue("Incorrect default output location: " + output.toOSString(), output.equals(project.getUnderlyingResource().getFullPath().append("bin")));
	} finally {
		this.deleteProject("P");
	}
}

/*
 * Ensures that one can point at an external library using a ".." entry in a cp container
 */
public void testDotDotContainerEntry1() throws Exception {
	String externalJarPath = getWorkspaceRoot().getLocation().removeLastSegments(1).append("external.jar").toOSString();
	try {
		IJavaProject p = createJavaProject("P");
		org.eclipse.jdt.core.tests.util.Util.createEmptyJar(
				externalJarPath,
				CompilerOptions.getFirstSupportedJavaVersion());
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P", "../../external.jar"}));
		setClasspath(p, new IClasspathEntry[] {JavaCore.newContainerEntry(new Path("org.eclipse.jdt.core.tests.model.TEST_CONTAINER"))});
		assertElementDescendants(
			"Unexpected project content",
			"P\n" +
			"  "+ getExternalPath() + "external.jar\n" +
			"    <default> (...)",
			p
		);
	} finally {
		deleteResource(new File(externalJarPath));
		deleteProject("P");
		ContainerInitializer.setInitializer(null);
	}
}

/*
 * Ensures that a marker is created if one can point at a non-existing external library using a ".." entry in a cp container
 */
public void testDotDotContainerEntry2() throws Exception {
	try {
		IJavaProject p = createJavaProject("P");
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P", "../../nonExisting.jar"}));
		setClasspath(p, new IClasspathEntry[] {JavaCore.newContainerEntry(new Path("org.eclipse.jdt.core.tests.model.TEST_CONTAINER"))});
		assertBuildPathMarkers(
			"Unexpected markers",
			"The container 'Test container' references non existing library \'" + getExternalPath() + "nonExisting.jar\'",
			p);
	} finally {
		deleteProject("P");
		ContainerInitializer.setInitializer(null);
	}
}

/*
 * Ensures that one can point at an external library using a ".." path
 */
public void testDotDotLibraryEntry1() throws Exception {
	String externalJarPath = getWorkspaceRoot().getLocation().append("external.jar").toOSString();
	try {
		IJavaProject p = createJavaProject("P");
		org.eclipse.jdt.core.tests.util.Util.createEmptyJar(
				externalJarPath,
				CompilerOptions.getFirstSupportedJavaVersion());
		setClasspath(p, new IClasspathEntry[] {JavaCore.newLibraryEntry(new Path("../external.jar"), null, null)});
		assertElementDescendants(
			"Unexpected project content",
			"P\n" +
			"  "+ getWorkspacePath() + "external.jar\n" +
			"    <default> (...)",
			p
		);
	} finally {
		deleteResource(new File(externalJarPath));
		deleteProject("P");
	}
}

/*
 * Ensures that one can point at an external library using a ".." path
 */
public void testDotDotLibraryEntry2() throws Exception {
	String externalJarPath = getWorkspaceRoot().getLocation().removeLastSegments(1).append("external.jar").toOSString();
	try {
		IJavaProject p = createJavaProject("P");
		org.eclipse.jdt.core.tests.util.Util.createEmptyJar(
				externalJarPath,
				CompilerOptions.getFirstSupportedJavaVersion());
		setClasspath(p, new IClasspathEntry[] {JavaCore.newLibraryEntry(new Path("../../external.jar"), null, null)});
		assertElementDescendants(
			"Unexpected project content",
			"P\n" +
			"  "+ getExternalPath() + "external.jar\n" +
			"    <default> (...)",
			p
		);
	} finally {
		deleteResource(new File(externalJarPath));
		deleteProject("P");
	}
}

/*
 * Ensures that one can point at an external library using a ".." path
 */
public void testDotDotLibraryEntry3() throws Exception {
	String externalJarPath = getWorkspaceRoot().getLocation().removeLastSegments(1).append("external.jar").toOSString();
	try {
		IJavaProject p = createJavaProject("P");
		org.eclipse.jdt.core.tests.util.Util.createEmptyJar(
				externalJarPath,
				CompilerOptions.getFirstSupportedJavaVersion());
		setClasspath(p, new IClasspathEntry[] {JavaCore.newLibraryEntry(new Path("src/../../../external.jar"), null, null)});
		assertElementDescendants(
			"Unexpected project content",
			"P\n" +
			"  "+ getExternalPath() + "external.jar\n" +
			"    <default> (...)",
			p
		);
	} finally {
		deleteResource(new File(externalJarPath));
		deleteProject("P");
	}
}

/*
 * Ensures that one can point at an internal library using a ".." path
 */
public void testDotDotLibraryEntry4() throws Exception {
	try {
		IJavaProject p1 = createJavaProject("P1");
		IJavaProject p2 = createJavaProject("P2");

		addLibrary(p1, "internal.jar", null, new String[0],
				new String[]{"META-INF/MANIFEST.MF",
					"Manifest-Version: 1.0\n"} ,
					CompilerOptions.getFirstSupportedJavaVersion());
		setClasspath(p2, new IClasspathEntry[] {JavaCore.newLibraryEntry(new Path("../P1/internal.jar"), null, null)});
		assertElementDescendants(
			"Unexpected project content",
			"P2\n" +
			"  /P1/internal.jar\n" +
			"    <default> (...)",
			p2
		);
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}

/*
 * Ensures that no markers are created if one can point at an existing external library using a ".." path
 */
public void testDotDotLibraryEntry5() throws Exception {
	String externalJarPath = getWorkspaceRoot().getLocation().append("external.jar").toOSString();
	try {
		IJavaProject p = createJavaProject("P");
		org.eclipse.jdt.core.tests.util.Util.createEmptyJar(
				externalJarPath,
				CompilerOptions.getFirstSupportedJavaVersion());
		setClasspath(p, new IClasspathEntry[] {JavaCore.newLibraryEntry(new Path("../external.jar"), null, null)});
		assertBuildPathMarkers(
			"Unexpected markers",
			"",
			p);
	} finally {
		deleteResource(new File(externalJarPath));
		deleteProject("P");
	}
}

/*
 * Ensures that a marker is created if one can point at a non-existing external library using a ".." path
 */
public void testDotDotLibraryEntry6() throws Exception {
	try {
		IJavaProject p = createJavaProject("P");
		setClasspath(p, new IClasspathEntry[] {JavaCore.newLibraryEntry(new Path("../external.jar"), null, null)});
		assertBuildPathMarkers(
			"Unexpected markers",
			"Project \'P\' is missing required library: \'"+ getWorkspacePath() + "external.jar\'",
			p);
	} finally {
		deleteProject("P");
	}
}

/*
 * Ensures that one can point at an external library using a ".." path by editing the .classpath file
 */
public void testDotDotLibraryEntry7() throws Exception {
	String externalJarPath = getWorkspaceRoot().getLocation().append("external.jar").toOSString();
	try {
		IJavaProject p = createJavaProject("P");
		org.eclipse.jdt.core.tests.util.Util.createEmptyJar(
				externalJarPath,
				CompilerOptions.getFirstSupportedJavaVersion());
		editFile(
			"/P/.classpath",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"	<classpathentry kind=\"src\" path=\"\"/>\n" +
			"	<classpathentry kind=\"lib\" path=\"../external.jar\"/>\n" +
			"	<classpathentry kind=\"output\" path=\"\"/>\n" +
			"</classpath>");
		assertElementDescendants(
			"Unexpected project content",
			"P\n" +
			"  <project root>\n" +
			"    <default> (...)\n" +
			"  "+ getWorkspacePath() + "external.jar\n" +
			"    <default> (...)",
			p
		);
	} finally {
		deleteResource(new File(externalJarPath));
		deleteProject("P");
	}
}

/*
 * Ensures that the raw classpath is correct if pointing at an external library using a ".." path by editing the .classpath file
 */
public void testDotDotLibraryEntry8() throws Exception {
	try {
		IJavaProject p = createJavaProject("P");
		editFile(
			"/P/.classpath",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"	<classpathentry kind=\"src\" path=\"\"/>\n" +
			"	<classpathentry kind=\"lib\" path=\"../external.jar\"/>\n" +
			"	<classpathentry kind=\"output\" path=\"\"/>\n" +
			"</classpath>");
		assertClasspathEquals(
			p.getRawClasspath(),
			"/P[CPE_SOURCE][K_SOURCE][isExported:false]\n" +
			"../external.jar[CPE_LIBRARY][K_BINARY][isExported:false]"
		);
	} finally {
		deleteProject("P");
	}
}

/*
 * Ensures that one can point at an external library using a ".." variable entry
 */
public void testDotDotVariableEntry1() throws Exception {
	String externalJarPath = getWorkspaceRoot().getLocation().removeLastSegments(1).append("external.jar").toOSString();
	try {
		JavaCore.setClasspathVariable("TWO_UP", new Path("../.."), null);
		IJavaProject p = createJavaProject("P");
		org.eclipse.jdt.core.tests.util.Util.createEmptyJar(
				externalJarPath,
				CompilerOptions.getFirstSupportedJavaVersion());
		setClasspath(p, new IClasspathEntry[] {JavaCore.newVariableEntry(new Path("TWO_UP/external.jar"), null, null)});
		assertElementDescendants(
			"Unexpected project content",
			"P\n" +
			"  "+ getExternalPath() + "external.jar\n" +
			"    <default> (...)",
			p
		);
	} finally {
		deleteResource(new File(externalJarPath));
		deleteProject("P");
		JavaCore.removeClasspathVariable("TWO_UP", null);
	}
}

/*
 * Ensures that a marker is created if one can point at a non-existing external library using a ".." variable entry
 */
public void testDotDotVariableEntry2() throws Exception {
	try {
		JavaCore.setClasspathVariable("TWO_UP", new Path("../.."), null);
		IJavaProject p = createJavaProject("P");
		setClasspath(p, new IClasspathEntry[] {JavaCore.newVariableEntry(new Path("TWO_UP/nonExisting.jar"), null, null)});
		assertBuildPathMarkers(
			"Unexpected markers",
			"Project \'P\' is missing required library: \'"+ getExternalPath() + "nonExisting.jar\'",
			p);
	} finally {
		deleteProject("P");
		JavaCore.removeClasspathVariable("TWO_UP", null);
	}
}

/**
 * Setting the classpath to empty should result in no entries,
 * and a delta with removed roots.
 */
public void testEmptyClasspath() throws CoreException {
	IJavaProject project = this.createJavaProject("P", new String[] {""}, "");
	try {
		startDeltas();
		setClasspath(project, new IClasspathEntry[] {});
		IClasspathEntry[] cp= project.getRawClasspath();
		assertTrue("classpath should have no entries", cp.length == 0);

		// ensure the deltas are correct
		assertDeltas(
			"Unexpected delta",
			"P[*]: {CHILDREN | CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
			"	<project root>[*]: {REMOVED FROM CLASSPATH}\n" +
			"	ResourceDelta(/P/.classpath)[*]"
		);
	} finally {
		stopDeltas();
		this.deleteProject("P");
	}
}
/*
 * Ensures that a source folder that contains character that must be encoded can be written.
 * (regression test for bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=70193)
 */
public void testEncoding1() throws CoreException {
	try {
		try {
			createJavaProject("P", new String[] {"src\u3400"}, "bin");
		} catch (InvalidPathException e) {
			System.err.println("File system cannot handle UTF-8 path: "+e.getMessage());
			System.err.println("Skipping ClasspathTests.testEncoding1");
			return;
		}
		IFile file = getFile("/P/.classpath");
		String encodedContents = new String (org.eclipse.jdt.internal.core.util.Util.getResourceContentsAsCharArray(file, "UTF-8"));
		encodedContents = Util.convertToIndependantLineDelimiter(encodedContents);
		assertEquals(
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"	<classpathentry kind=\"src\" path=\"src\u3400\"/>\n" +
			"	<classpathentry kind=\"output\" path=\"bin\"/>\n" +
			"</classpath>\n",
			encodedContents);
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a .classpath encoded with a BOM can be read
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=240034 )
 */
public void testEncoding2() throws Exception {
	try {
		IJavaProject p = createJavaProject("P", new String[] {"src"}, "bin");
		IFile file = getFile("/P/.classpath");
		byte[] contents = org.eclipse.jdt.internal.core.util.Util.getResourceContentsAsByteArray(file);
		try (FileOutputStream output = new FileOutputStream(file.getLocation().toFile())) {
			output.write(IContentDescription.BOM_UTF_8); // UTF-8 BOM
			output.write(contents);
			output.flush();
		}
		file.refreshLocal(IResource.DEPTH_ONE, null);

		assertClasspathEquals(
			p.getResolvedClasspath(false),
			"/P/src[CPE_SOURCE][K_SOURCE][isExported:false]"
		);
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a source classpath entry can be encoded and decoded.
 */
public void testEncodeDecodeEntry01() {
	assertEncodeDecodeEntry(
		"P",
		"<classpathentry kind=\"src\" path=\"src\"/>\n",
		JavaCore.newSourceEntry(new Path("/P/src"))
	);
}
/*
 * Ensures that a source classpath entry with all possible attributes can be encoded and decoded.
 */
public void testEncodeDecodeEntry02() {
	assertEncodeDecodeEntry(
		"P",
		"<classpathentry excluding=\"**/X.java\" including=\"**/Y.java\" kind=\"src\" output=\"bin\" path=\"src\">\n" +
		"	<attributes>\n" +
		"		<attribute name=\"attrName\" value=\"some value\"/>\n" +
		"	</attributes>\n" +
		"</classpathentry>\n",
		JavaCore.newSourceEntry(
			new Path("/P/src"),
			new IPath[] {new Path("**/Y.java")},
			new IPath[] {new Path("**/X.java")},
			new Path("/P/bin"),
			new IClasspathAttribute[] {JavaCore.newClasspathAttribute("attrName", "some value")})
	);
}
/*
 * Ensures that a project classpath entry can be encoded and decoded.
 */
public void testEncodeDecodeEntry03() {
	assertEncodeDecodeEntry(
		"P1",
		"<classpathentry kind=\"src\" path=\"/P2\"/>\n",
		JavaCore.newProjectEntry(new Path("/P2"))
	);
}
/*
 * Ensures that a library classpath entry can be encoded and decoded.
 */
public void testEncodeDecodeEntry04() {
	assertEncodeDecodeEntry(
		"P",
		"<classpathentry exported=\"true\" kind=\"lib\" path=\"lib.jar\" rootpath=\"root\" sourcepath=\"src.zip\">\n" +
		"	<attributes>\n" +
		"		<attribute name=\"attr1\" value=\"val1\"/>\n" +
		"	</attributes>\n" +
		"	<accessrules>\n" +
		"		<accessrule kind=\"accessible\" pattern=\"**/A*.java\"/>\n" +
		"	</accessrules>\n" +
		"</classpathentry>\n",
		JavaCore.newLibraryEntry(
			new Path("/P/lib.jar"),
			new Path("/P/src.zip"),
			new Path("root"),
			new IAccessRule[] {JavaCore.newAccessRule(new Path("**/A*.java"), IAccessRule.K_ACCESSIBLE)},
			new IClasspathAttribute[] {JavaCore.newClasspathAttribute("attr1", "val1")},
			true)
	);
}
/*
 * Ensures that a library classpath entry can be encoded and decoded.
 */
public void testEncodeDecodeEntry05() {
	assertEncodeDecodeEntry(
		"P",
		"<classpathentry exported=\"true\" kind=\"lib\" path=\"lib.jar\" rootpath=\"root\" sourcepath=\"src.zip\">\n" +
		"	<attributes>\n" +
		"		<attribute name=\"attr1\" value=\"val1\"/>\n" +
		"	</attributes>\n" +
		"	<accessrules>\n" +
		"		<accessrule ignoreifbetter=\"true\" kind=\"accessible\" pattern=\"**/A*.java\"/>\n" +
		"	</accessrules>\n" +
		"</classpathentry>\n",
		JavaCore.newLibraryEntry(
			new Path("/P/lib.jar"),
			new Path("/P/src.zip"),
			new Path("root"),
			new IAccessRule[] {JavaCore.newAccessRule(new Path("**/A*.java"), IAccessRule.K_ACCESSIBLE | IAccessRule.IGNORE_IF_BETTER)},
			new IClasspathAttribute[] {JavaCore.newClasspathAttribute("attr1", "val1")},
			true)
	);
}
/**
 * Ensures that adding an empty classpath container
 * generates the correct deltas.
 */
public void testEmptyContainer() throws CoreException {
	try {
		IJavaProject proj = createJavaProject("P", new String[] {}, "bin");

		startDeltas();

		// create container
		JavaCore.setClasspathContainer(
			new Path("container/default"),
			new IJavaProject[]{ proj },
			new IClasspathContainer[] {
				new TestContainer(
					new Path("container/default"),
					new IClasspathEntry[] {})
			},
			null);

		// set P's classpath with this container
		IClasspathEntry container = JavaCore.newContainerEntry(new Path("container/default"), true);
		proj.setRawClasspath(new IClasspathEntry[] {container}, new Path("/P"), null);

		assertDeltas(
			"Unexpected delta",
			"P[*]: {CONTENT | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
			"	ResourceDelta(/P/.classpath)[*]"
		);
	} finally {
		stopDeltas();
		this.deleteProject("P");
	}
}
/*
 * Ensure that a .classpath with an empty inclusion pattern is correctly handled
 * (regression test for bug 105581 Creating a Java project from existing source fails because of "Unhandled event loop exception":)
 */
public void testEmptyInclusionPattern() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {""}, "bin");
		project.open(null);
		editFile(
			"/P/.classpath",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"  <classpathentry including=\"X.java|\" kind=\"src\" path=\"\"/>\n" +
			"  <classpathentry kind=\"output\" path=\"bin\"/>\n" +
			"</classpath>"
		);
		project.getProject().close(null);
		project.getProject().open(null);
		project.getPackageFragmentRoot(project.getProject()).open(null);
		IClasspathEntry[] classpath = project.getRawClasspath();
		assertClasspathEquals(
			classpath,
			"/P[CPE_SOURCE][K_SOURCE][isExported:false][including:X.java]"
		);
	} finally {
		deleteProject("P");
	}
}
/**
 * Exporting a container should make it visible to its dependent project.
 * (regression test for bug 21749 Exported libraries and source folders)
 */
public void testExportContainer() throws CoreException {
	try {
		IJavaProject p1 = this.createJavaProject("P1", new String[] {""}, "");

		// create container
		JavaCore.setClasspathContainer(
			new Path("container/default"),
			new IJavaProject[]{ p1 },
			new IClasspathContainer[] {
				new TestContainer(
					new Path("container/default"),
					new IClasspathEntry[] {
						JavaCore.newLibraryEntry(getExternalJCLPath(), null, null)
					})
			},
			null);

		// set P1's classpath with this container
		IClasspathEntry container = JavaCore.newContainerEntry(new Path("container/default"), true);
		p1.setRawClasspath(new IClasspathEntry[] {container}, new Path("/P1"), null);

		// create dependent project P2
		IJavaProject  p2 = this.createJavaProject("P2", new String[] {}, new String[] {}, new String[] {"/P1"}, "");
		IClasspathEntry[] classpath = ((JavaProject)p2).getExpandedClasspath();

		// ensure container is exported to P2
		assertEquals("Unexpected number of classpath entries", 2, classpath.length);
		assertEquals("Unexpected first entry", "/P1", classpath[0].getPath().toString());
		assertEquals("Unexpected second entry", getExternalJCLPathString(), classpath[1].getPath().toOSString());
	} finally {
		deleteProjects(new String[] {"P1", "P2"});
	}
}
/*
 * Ensures that adding an external jar and refreshing removed the markers
 * (regression test for 185733 Refreshing external jar doesn't update problem marker)
 */
public void testExternalJarAdd() throws CoreException, IOException {
	String externalJarPath = getExternalPath() + "test185733.jar";
	try {
		IJavaProject p = createJavaProject("P", new String[0], new String[] {externalJarPath}, "");
		waitUntilIndexesReady();
		waitForAutoBuild();
		// at this point, a marker indicates that test185733.jar has been created: "Project 'P' is missing required library: '[...]\test185733.jar'"
		org.eclipse.jdt.core.tests.util.Util.createEmptyJar(
				getExternalResourcePath("test185733.jar"),
				CompilerOptions.getFirstSupportedJavaVersion());
		refreshExternalArchives(p);
		assertBuildPathMarkers(
			"Unexpected markers",
			"",
			p);
	} finally {
		deleteResource(new File(externalJarPath));
		deleteProject("P");
	}
}

/*
 * Ensures that removing an external jar and refreshing creates the correct markers
 * (regression test for 185733 Refreshing external jar doesn't update problem marker)
 */
public void testExternalJarRemove() throws CoreException, IOException {
	try {
		File externalJar = createFile(new File(getExternalPath()), "test185733.jar", "");
		IJavaProject p = createJavaProject("P", new String[0], new String[] {externalJar.getPath()}, "");
		waitUntilIndexesReady();
		waitForAutoBuild();
		// at this point, the project has no markers

		deleteResource(externalJar);
		refreshExternalArchives(p);
		assertBuildPathMarkers(
			"Unexpected markers",
			"Project \'P\' is missing required library: \'" + externalJar.getPath() + "\'",
			p);
	} finally {
		deleteProject("P");
	}
}

/*
 * Ensures that setting 0 extra classpath attributes generates the correct .classpath file.
 */
public void testExtraAttributes1() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P");
		IClasspathEntry entry = JavaCore.newSourceEntry(new Path("/P"), new IPath[0], new IPath[0], null, new IClasspathAttribute[] {});
		project.setRawClasspath(new IClasspathEntry[] {entry}, null);
		String contents = new String (org.eclipse.jdt.internal.core.util.Util.getResourceContentsAsCharArray(getFile("/P/.classpath")));
		assertSourceEquals(
			"Unexpected content",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"	<classpathentry kind=\"src\" path=\"\"/>\n" +
			"	<classpathentry kind=\"output\" path=\"\"/>\n" +
			"</classpath>\n",
			contents);
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that setting 1 extra classpath attributes generates the correct .classpath file.
 */
public void testExtraAttributes2() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P");
		IClasspathAttribute attribute = JavaCore.newClasspathAttribute("foo", "some value");
		IClasspathEntry entry = JavaCore.newSourceEntry(new Path("/P"), new IPath[0], new IPath[0], null, new IClasspathAttribute[] {attribute});
		project.setRawClasspath(new IClasspathEntry[] {entry}, null);
		String contents = new String (org.eclipse.jdt.internal.core.util.Util.getResourceContentsAsCharArray(getFile("/P/.classpath")));
		assertSourceEquals(
			"Unexpected content",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"	<classpathentry kind=\"src\" path=\"\">\n" +
			"		<attributes>\n" +
			"			<attribute name=\"foo\" value=\"some value\"/>\n" +
			"		</attributes>\n" +
			"	</classpathentry>\n" +
			"	<classpathentry kind=\"output\" path=\"\"/>\n" +
			"</classpath>\n",
			contents);
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that setting 2 extra classpath attributes generates the correct .classpath file.
 */
public void testExtraAttributes3() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P");
		IClasspathAttribute attribute1 = JavaCore.newClasspathAttribute("foo", "some value");
		IClasspathAttribute attribute2 = JavaCore.newClasspathAttribute("bar", "other value");
		IClasspathEntry entry = JavaCore.newSourceEntry(new Path("/P"), new IPath[0], new IPath[0], null, new IClasspathAttribute[] {attribute1, attribute2});
		project.setRawClasspath(new IClasspathEntry[] {entry}, null);
		String contents = new String (org.eclipse.jdt.internal.core.util.Util.getResourceContentsAsCharArray(getFile("/P/.classpath")));
		assertSourceEquals(
			"Unexpected content",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"	<classpathentry kind=\"src\" path=\"\">\n" +
			"		<attributes>\n" +
			"			<attribute name=\"foo\" value=\"some value\"/>\n" +
			"			<attribute name=\"bar\" value=\"other value\"/>\n" +
			"		</attributes>\n" +
			"	</classpathentry>\n" +
			"	<classpathentry kind=\"output\" path=\"\"/>\n" +
			"</classpath>\n",
			contents);
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that extra classpath attributes in a .classpath file are correctly read.
 */
public void testExtraAttributes4() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P");
		editFile(
			"/P/.classpath",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"	<classpathentry kind=\"src\" path=\"\">\n" +
			"		<attributes>\n" +
			"			<attribute value=\"some value\" name=\"foo\"/>\n" +
			"		</attributes>\n" +
			"	</classpathentry>\n" +
			"	<classpathentry kind=\"output\" path=\"\"/>\n" +
			"</classpath>\n"
		);
		assertClasspathEquals(
			project.getRawClasspath(),
			"/P[CPE_SOURCE][K_SOURCE][isExported:false][attributes:foo=some value]"
		);
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that the extra libraries in the Class-Path: clause of a jar are taken into account.
 */
public void testExtraLibraries01() throws Exception {
	try {
		IJavaProject p = createJavaProject("P");
		addLibrary(p, "lib1.jar", null, new String[0],
			new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" +
				"Class-Path: lib2.jar\n",
			},
			CompilerOptions.getFirstSupportedJavaVersion());
		createFile("/P/lib2.jar", "");
		assertClasspathEquals(
			p.getResolvedClasspath(true),
			"/P[CPE_SOURCE][K_SOURCE][isExported:false]\n" +
			""+ getExternalJCLPathString() + "[CPE_LIBRARY][K_BINARY][isExported:false]\n" +
			"/P/lib2.jar[CPE_LIBRARY][K_BINARY][isExported:true]\n" +
			"/P/lib1.jar[CPE_LIBRARY][K_BINARY][isExported:true]"
		);
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that the extra libraries in the Class-Path: clause of a jar are taken into account.
 */
public void testExtraLibraries02() throws Exception {
	try {
		IJavaProject p = createJavaProject("P");
		addLibrary(p, "lib1.jar", null, new String[0],
			new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" +
				"Class-Path: lib2.jar lib3.jar\n",
			},
			CompilerOptions.getFirstSupportedJavaVersion());
		createFile("/P/lib2.jar", "");
		createFile("/P/lib3.jar", "");
		assertClasspathEquals(
			p.getResolvedClasspath(true),
			"/P[CPE_SOURCE][K_SOURCE][isExported:false]\n" +
			""+ getExternalJCLPathString() + "[CPE_LIBRARY][K_BINARY][isExported:false]\n" +
			"/P/lib2.jar[CPE_LIBRARY][K_BINARY][isExported:true]\n" +
			"/P/lib3.jar[CPE_LIBRARY][K_BINARY][isExported:true]\n" +
			"/P/lib1.jar[CPE_LIBRARY][K_BINARY][isExported:true]"
		);
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that the extra libraries in the Class-Path: clause of a jar are taken into account.
 */
public void testExtraLibraries03() throws Exception {
	try {
		IJavaProject p = createJavaProject("P");
		createFile("/P/lib3.jar", "");
		createLibrary(p, "lib2.jar", null, new String[0],
			new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" +
				"Class-Path: lib3.jar\n",
			},
			CompilerOptions.getFirstSupportedJavaVersion());
		addLibrary(p, "lib1.jar", null, new String[0],
			new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" +
				"Class-Path: lib2.jar\n",
			},
			CompilerOptions.getFirstSupportedJavaVersion());
		assertClasspathEquals(
			p.getResolvedClasspath(true),
			"/P[CPE_SOURCE][K_SOURCE][isExported:false]\n" +
			""+ getExternalJCLPathString() + "[CPE_LIBRARY][K_BINARY][isExported:false]\n" +
			"/P/lib3.jar[CPE_LIBRARY][K_BINARY][isExported:true]\n" +
			"/P/lib2.jar[CPE_LIBRARY][K_BINARY][isExported:true]\n" +
			"/P/lib1.jar[CPE_LIBRARY][K_BINARY][isExported:true]"
		);
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that the extra libraries in the Class-Path: clause of a jar are taken into account.
 */
public void testExtraLibraries04() throws Exception {
	try {
		IJavaProject p = createJavaProject("P");
		addLibrary(p, "lib1.jar", null, new String[0],
			new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" +
				"Class-Path: lib2.jar\n",
			},
			CompilerOptions.getFirstSupportedJavaVersion());
		createLibrary(p, "lib2.jar", null, new String[0],
			new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" +
				"Class-Path: lib3.jar\n",
			},
			CompilerOptions.getFirstSupportedJavaVersion());
		createFile("/P/lib3.jar", "");
		assertClasspathEquals(
			p.getResolvedClasspath(true),
			"/P[CPE_SOURCE][K_SOURCE][isExported:false]\n" +
			""+ getExternalJCLPathString() + "[CPE_LIBRARY][K_BINARY][isExported:false]\n" +
			"/P/lib3.jar[CPE_LIBRARY][K_BINARY][isExported:true]\n" +
			"/P/lib2.jar[CPE_LIBRARY][K_BINARY][isExported:true]\n" +
			"/P/lib1.jar[CPE_LIBRARY][K_BINARY][isExported:true]"
		);
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that no markers are created for correct extra libraries in the Class-Path: clause of a jar
 */
public void testExtraLibraries05() throws Exception {
	try {
		IJavaProject p = createJavaProject("P");
		addLibrary(p, "lib1.jar", null, new String[0],
			new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" +
				"Class-Path: lib2.jar\n",
			},
			CompilerOptions.getFirstSupportedJavaVersion());
		createFile("/P/lib2.jar", "");
		assertBuildPathMarkers(
			"Unexpected markers",
			"",
			p);
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a marker is not created for incorrect extra libraries in the Class-Path: clause of a jar
 * (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=252392 )
 */
public void testExtraLibraries06() throws Exception {
	try {
		IJavaProject p = createJavaProject("P");
		addLibrary(p, "lib1.jar", null, new String[0],
			new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" +
				"Class-Path: lib2.jar\n",
			},
			CompilerOptions.getFirstSupportedJavaVersion());
		assertBuildPathMarkers(
			"Unexpected markers",
			"",
			p);
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that the extra libraries in the Class-Path: clause of an external jar are taken into account.
 */
public void testExtraLibraries07() throws Exception {
	try {
		IJavaProject p = createJavaProject("P");
		createExternalFile("lib2.jar", "");
		addExternalLibrary(p, getExternalResourcePath("lib1.jar"), new String[0],
			new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" +
				"Class-Path: lib2.jar\n",
			},
			CompilerOptions.getFirstSupportedJavaVersion());
		assertClasspathEquals(
			p.getResolvedClasspath(true),
			"/P[CPE_SOURCE][K_SOURCE][isExported:false]\n" +
			""+ getExternalJCLPathString() + "[CPE_LIBRARY][K_BINARY][isExported:false]\n" +
			""+ getExternalPath() + "lib2.jar[CPE_LIBRARY][K_BINARY][isExported:true]\n" +
			""+ getExternalPath() + "lib1.jar[CPE_LIBRARY][K_BINARY][isExported:true]"
		);
	} finally {
		deleteProject("P");
		deleteExternalResource("lib1.jar");
		deleteExternalResource("lib2.jar");
	}
}
/*
 * Ensures that no markers are created for correct extra libraries in the Class-Path: clause of an external jar
 */
public void testExtraLibraries08() throws Exception {
	try {
		IJavaProject p = createJavaProject("P");
		addExternalLibrary(p, getExternalResourcePath("lib1.jar"), new String[0],
			new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" +
				"Class-Path: lib2.jar\n",
			},
			CompilerOptions.getFirstSupportedJavaVersion());
		createExternalFile("lib2.jar", "");
		refreshExternalArchives(p);
		assertBuildPathMarkers(
			"Unexpected markers",
			"",
			p);
	} finally {
		deleteProject("P");
		deleteExternalResource("lib1.jar");
		deleteExternalResource("lib2.jar");
	}
}
/*
 * Ensures that a marker is not created for incorrect extra libraries in the Class-Path: clause of an external jar
 * (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=252392 )
 */
public void testExtraLibraries09() throws Exception {
	try {
		IJavaProject p = createJavaProject("P");
		addExternalLibrary(p, getExternalResourcePath("lib1.jar"), new String[0],
			new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" +
				"Class-Path: lib2.jar\n",
			},
			CompilerOptions.getFirstSupportedJavaVersion());
		assertBuildPathMarkers(
			"Unexpected markers",
			"",
			p);
	} finally {
		deleteProject("P");
		deleteExternalResource("lib1.jar");
		deleteExternalResource("lib2.jar");
	}
}
/*
 * Ensures that a marker is not created for incorrect extra libraries in the Class-Path: clause of an external jar
 * (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=252392 )
 */
public void testExtraLibraries10() throws Exception {
	try {
		IJavaProject p = createJavaProject("P");
		addExternalLibrary(p, getExternalResourcePath("lib1.jar"), new String[0],
			new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" +
				"Class-Path: lib2.jar\n",
			},
			CompilerOptions.getFirstSupportedJavaVersion());
		createExternalFile("lib2.jar", "");
		refreshExternalArchives(p);
		waitForAutoBuild(); // wait until classpath is validated -> no markers

		deleteExternalResource("lib2.jar");
		refreshExternalArchives(p);
		assertBuildPathMarkers(
			"Unexpected markers",
			"",
			p);
	} finally {
		deleteProject("P");
		deleteExternalResource("lib1.jar");
		deleteExternalResource("lib2.jar");
	}
}
/*
 * Ensures that the correct delta is reported when editing the Class-Path: clause of a jar
 */
public void testExtraLibraries11() throws Exception {
	try {
		IJavaProject p = createJavaProject("P");
		addLibrary(p, "lib1.jar", null, new String[0],
			new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" +
				"Class-Path: lib2.jar\n",
			},
			CompilerOptions.getFirstSupportedJavaVersion());
		createFile("/P/lib2.jar", "");
		startDeltas();
		if (Util.isMacOS()) {
			// necessary for filesystems with timestamps only upto seconds (eg. Mac)
			// "lib1.jar" was created above and is modified below.
			Thread.sleep(2000);
		}
		createLibrary(p, "lib1.jar", null, new String[0],
			new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" +
				"Class-Path: lib3.jar\n",
			},
			CompilerOptions.getFirstSupportedJavaVersion());
		assertDeltas(
			"Unexpected delta",
			"P[*]: {CHILDREN | RESOLVED CLASSPATH CHANGED}\n" +
			"	lib1.jar[*]: {CONTENT | ARCHIVE CONTENT CHANGED}\n" +
			"	lib2.jar[*]: {REMOVED FROM CLASSPATH}"
		);
	} finally {
		stopDeltas();
		deleteProject("P");
	}
}
/*
 * Ensures that the correct delta is reported when editing the Class-Path: clause of an external jar
 */
public void testExtraLibraries12() throws Exception {
	try {
		IJavaProject p = createJavaProject("P");
		addExternalLibrary(p, getExternalResourcePath("lib1.jar"), new String[0],
			new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" +
				"Class-Path: lib2.jar\n",
			},
			CompilerOptions.getFirstSupportedJavaVersion());
		createExternalFile("lib2.jar", "");
		refreshExternalArchives(p);

		startDeltas();
		org.eclipse.jdt.core.tests.util.Util.createJar(new String[0],
			new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" +
				"Class-Path: lib3.jar\n",
			},
			getExternalResourcePath("lib1.jar"),
			CompilerOptions.getFirstSupportedJavaVersion());
		refreshExternalArchives(p);
		assertDeltas(
			"Unexpected delta",
			"P[*]: {CHILDREN | RESOLVED CLASSPATH CHANGED}\n" +
			"	"+ getExternalPath() + "lib1.jar[*]: {CONTENT | ARCHIVE CONTENT CHANGED}\n" +
			"	"+ getExternalPath() + "lib2.jar[*]: {REMOVED FROM CLASSPATH}"
		);
	} finally {
		stopDeltas();
		deleteProject("P");
		deleteExternalResource("lib1.jar");
		deleteExternalResource("lib2.jar");
	}
}
/*
 * Ensures that the correct delta is reported when removing an external jar with a Class-Path: clause
 */
public void testExtraLibraries13() throws Exception {
	try {
		IJavaProject p = createJavaProject("P");
		addExternalLibrary(p, getExternalResourcePath("lib1.jar"), new String[0],
			new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" +
				"Class-Path: lib2.jar\n",
			},
			CompilerOptions.getFirstSupportedJavaVersion());
		createExternalFile("lib2.jar", "");
		refreshExternalArchives(p);

		startDeltas();
		deleteExternalResource("lib1.jar");
		refreshExternalArchives(p);
		assertDeltas(
			"Unexpected delta",
			"P[*]: {CHILDREN | RESOLVED CLASSPATH CHANGED}\n" +
			"	"+ getExternalPath() + "lib1.jar[-]: {}\n" +
			"	"+ getExternalPath() + "lib2.jar[*]: {REMOVED FROM CLASSPATH}"
		);
	} finally {
		stopDeltas();
		deleteProject("P");
		deleteExternalResource("lib1.jar");
		deleteExternalResource("lib2.jar");
	}
}
/*
 * Ensures that no marker is created for incorrect extra libraries in the Class-Path: clause of an external jar referenced by a container
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=250946 )
 */
public void testExtraLibraries14() throws Exception {
	try {
		Util.createJar(
			new String[0],
			new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" +
				"Class-Path: nonExisting.jar\n",
			},
			getExternalResourcePath("lib1.jar"),
			CompilerOptions.getFirstSupportedJavaVersion());
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P", getExternalResourcePath("lib1.jar")}));
		IJavaProject p = createJavaProject("P", new String[0], new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"}, "");
		assertBuildPathMarkers(
			"Unexpected markers",
			"",
			p);
	} finally {
		deleteProject("P");
		deleteExternalResource("lib1.jar");
	}
}
/*
 * Ensures that the extra libraries in the Class-Path: clause of a jar with a \r\n separator don't cause an exception
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=251079 )
 */
public void testExtraLibraries15() throws Exception {
	try {
		Util.createJar(
			new String[0],
			new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\r\n" +
				"Class-Path: \r\n" +
				"\r\n",
			},
			getExternalResourcePath("lib1.jar"),
			CompilerOptions.getFirstSupportedJavaVersion());
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P", getExternalResourcePath("lib1.jar")}));
		IJavaProject p = createJavaProject("P", new String[0], new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"}, "");
		assertClasspathEquals(
			p.getResolvedClasspath(true),
			""+ getExternalPath() + "lib1.jar[CPE_LIBRARY][K_BINARY][isExported:false]"
		);
	} finally {
		deleteProject("P");
		deleteExternalResource("lib1.jar");
	}
}
/*
 * Ensures that an invalid Class-Path: clause of a jar doesn't generate messages in the .log
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=252264 )
 */
public void testExtraLibraries16() throws Exception {
	try {
		startLogListening();
		Util.createJar(
			new String[0],
			new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\r\n" +
				"Class-Path: \n",
			},
			getExternalResourcePath("lib1.jar"),
			CompilerOptions.getFirstSupportedJavaVersion());
		IJavaProject p = createJavaProject("P", new String[0], new String[] {getExternalResourcePath("lib1.jar")}, "");
		p.getResolvedClasspath(true);
		assertLogEquals("");
	} finally {
		stopLogListening();
		deleteProject("P");
		deleteExternalResource("lib1.jar");
	}
}
/*
 * Ensures that nested libraries in the Class-Path: clause of a jar doesn't create a marker.
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=259685 )
 */
public void testExtraLibraries17() throws Exception {
	try {
		IJavaProject p = createJavaProject("P");
		addExternalLibrary(p, getExternalResourcePath("lib.jar"), new String[0],
			new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" +
				"Class-Path: lib/ \n",
			},
			CompilerOptions.getFirstSupportedJavaVersion());
		waitForAutoBuild();

		org.eclipse.jdt.core.tests.util.Util.createJar(
			null/*no classes*/,
			new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" +
				"Class-Path: lib/ lib/sublib/ \n",
			},
			getExternalResourcePath("lib.jar"),
			null/*no classpath*/,
			CompilerOptions.getFirstSupportedJavaVersion());
		refreshExternalArchives(p);
		assertBuildPathMarkers(
			"Unexpected markers",
			"",
			p);
	} finally {
		deleteProject("P");
		deleteExternalResource("lib.jar");
	}
}
/*
 * Ensures that a marker is removed if adding an internal jar that is on the classpath in another project
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=213723 )
 */
public void testFixClasspath1() throws CoreException, IOException {
	try {
		createProject("P1");
		IJavaProject project = createJavaProject("P2", new String[0], new String[0], "bin");
		waitForAutoBuild();
		addLibrary(project, "lib.jar", null, new String[0],
				new String[]{"META-INF/MANIFEST.MF",
					"Manifest-Version: 1.0\n"} ,
					CompilerOptions.getFirstSupportedJavaVersion());

		assertBuildPathMarkers(
			"Unexpected markers",
			"",
			project);
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}
/*
 * Ensures that a marker is removed if adding an external jar, restarting and refreshing
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=216446 )
 */
public void testFixClasspath2() throws CoreException, IOException {
	try {
		IJavaProject p = createJavaProject("P", new String[0], new String[] {getExternalResourcePath("externalLib.abc")}, "");
		waitForAutoBuild(); // 1 marker
		org.eclipse.jdt.core.tests.util.Util.createEmptyJar(
				getExternalResourcePath("externalLib.abc"),
				CompilerOptions.getFirstSupportedJavaVersion());

		simulateExitRestart();
		refreshExternalArchives(p);

		assertBuildPathMarkers("Unexpected markers", "", p);
	} finally {
		deleteExternalResource("externalLib.abc");
		deleteProject("P");
	}
}
/**
 * Test IJavaProject.hasClasspathCycle(IClasspathEntry[]).
 */
public void testHasClasspathCycle() throws CoreException {
	try {
		IJavaProject p1 = this.createJavaProject("P1", new String[] {""}, "");
		IJavaProject p2 = this.createJavaProject("P2", new String[] {""}, "");
		this.createJavaProject("P3", new String[] {""}, new String[] {}, new String[] {"/P1"}, "");

		IClasspathEntry[] originalP1CP= p1.getRawClasspath();
		IClasspathEntry[] originalP2CP= p2.getRawClasspath();

		// Ensure no cycle reported
		assertTrue("P1 should not have a cycle", !p1.hasClasspathCycle(originalP1CP));

		// Ensure that adding NervousTest as a prerequesite of P2 doesn't report a cycle
		int length = originalP2CP.length;
		IClasspathEntry[] newCP= new IClasspathEntry[length + 1];
		System.arraycopy(originalP2CP, 0 , newCP, 0, length);
		newCP[length]= JavaCore.newProjectEntry(p1.getProject().getFullPath(), false);
		assertTrue("P2 should not have a cycle", !p2.hasClasspathCycle(newCP));
		p2.setRawClasspath(newCP, null);

		// Ensure that adding P3 as a prerequesite of P1 reports a cycle
		length = originalP1CP.length;
		newCP= new IClasspathEntry[length + 1];
		System.arraycopy(originalP1CP, 0 , newCP, 0, length);
		newCP[length]= JavaCore.newProjectEntry(p2.getProject().getFullPath(), false);
		assertTrue("P3 should have a cycle", p2.hasClasspathCycle(newCP));

		// Ensure a cycle is not reported through markers
		IWorkspace workspace = getJavaModel().getWorkspace();
		IMarker[] markers = workspace.getRoot().findMarkers(IJavaModelMarker.TRANSIENT_PROBLEM, true, 1);
		boolean hasCycleMarker = false;
		for (int i = 0; i < markers.length; i++){
			if (markers[i].getAttribute(IJavaModelMarker.CYCLE_DETECTED) != null) {
				hasCycleMarker = true;
				break;
			}
		}
	assertTrue("Should have no cycle markers", !hasCycleMarker);

	} finally {
		// cleanup
		deleteProjects(new String[] {"P1", "P2", "P3"});
	}
}
/**
 * Ensures that a marker is created if editing the .classpath results in an invalid classpath.
 */
public void testInvalidClasspath1() throws CoreException {
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {"src"}, "bin");
		// see 180769 Fatal error in log for java tests N20070403-0010
		System.err.println("ClasspathTests#testInvalidClasspath1() may generate an expected Fatal Error...");
		editFile(
			"/P/.classpath",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"    <classpathentry kind=\"src\" path=\"src\"/\n" + // missing closing >
			"    <classpathentry kind=\"output\" path=\"bin\"/>\n" +
			"</classpath>"
		);
		assertBuildPathMarkers(
			"Unexpected markers",
			"XML format error in \'.classpath\' file of project 'P': Bad format",
			project);
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Ensures that a marker is created if editing the .classpath results in an invalid classpath.
 */
public void testInvalidClasspath2() throws CoreException {
	try {
		IJavaProject javaProject = this.createJavaProject("P", new String[] {"src"}, "bin");
		editFile(
			"/P/.classpath",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"    <classpathentry kind=\"src1\" path=\"src\"/>\n" + // invalid kind: src1
			"    <classpathentry kind=\"output\" path=\"bin\"/>\n" +
			"</classpath>"
		);
		assertBuildPathMarkers(
			"Unexpected markers",
			"Illegal entry in \'.classpath\' of project 'P' file: Unknown kind: \'src1\'",
			javaProject);

		// Verify that error marker is not removed after build
		// (regression test for bug 42366: Classpath validation error message removed while rebuilding a project.)
		IProject project = javaProject.getProject();
		project.build(IncrementalProjectBuilder.FULL_BUILD, null);
		waitForAutoBuild();
		assertBuildPathMarkers(
			"Unexpected markers",
			"Illegal entry in \'.classpath\' of project 'P' file: Unknown kind: \'src1\'",
			javaProject);
	} finally {
		this.deleteProject("P");
	}
}
/*
 * Ensures that an attempting to put a non-existing external class folder on the classpath
 * creates the correct marker.
 */
public void testInvalidExternalClassFolder() throws CoreException {
	try {
		String externalPath = getExternalPath() + "nonExisting";
		// remove trailing slash
		if (Path.fromOSString(externalPath).segmentCount() > 0)
			externalPath = externalPath.substring(0, externalPath.length()-1);
		IJavaProject proj =  createJavaProject("P", new String[] {}, new String[] {externalPath}, "bin");
		assertBuildPathMarkers(
			"Unexpected markers",
			"Project \'P\' is missing required library: \'" + externalPath + "\'",
			proj);
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a non existing external jar cannot be put on the classpath.
 */
public void testInvalidExternalJar() throws CoreException {
	try {
		String jarPath = getExternalPath() + "nonExisting.jar";
		IJavaProject proj = createJavaProject("P", new String[] {}, new String[] {jarPath}, "bin");
		assertBuildPathMarkers(
			"Unexpected markers",
			"Project 'P' is missing required library: \'" + jarPath + "\'",
			proj);
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that validateClasspathEntry() sees a transition from an invalid/missing jar to a valid jar.
 */
public void testTransitionFromInvalidToValidJar() throws CoreException, IOException {
	String transitioningJarName = "transitioningJar.jar";
	String transitioningJar = getExternalPath() + transitioningJarName;
	String invalidJar = getExternalPath() + "invalidJar.jar";
	IClasspathEntry transitioningEntry = JavaCore.newLibraryEntry(new Path(transitioningJar), null, null);
	IClasspathEntry nonExistingEntry = JavaCore.newLibraryEntry(new Path(invalidJar), null, null);

	try {
		Util.createFile(transitioningJar, "");
		Util.createFile(invalidJar, "");
		IJavaProject proj = createJavaProject("P", new String[] {}, new String[] {transitioningJar, invalidJar}, "bin");

		IJavaModelStatus status1 = ClasspathEntry.validateClasspathEntry(proj, transitioningEntry, false, false);
		IJavaModelStatus status2 = ClasspathEntry.validateClasspathEntry(proj, nonExistingEntry, false, false);
		assertFalse("Non-existing jar should be invalid", status1.isOK());
		assertFalse("Non-existing jar should be invalid", status2.isOK());

		Util.createJar(
			new String[0],
			new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n"
			},
			transitioningJar,
			CompilerOptions.getFirstSupportedJavaVersion());
		status1 = ClasspathEntry.validateClasspathEntry(proj, transitioningEntry, false, false);
		status2 = ClasspathEntry.validateClasspathEntry(proj, nonExistingEntry, false, false);
		assertTrue("Existing jar should be valid", status1.isOK());
		assertFalse("Non-existing jar should be invalid", status2.isOK());
	} finally {
		deleteExternalResource(transitioningJarName);
		deleteExternalResource("invalidJar.jar");
		deleteProject("P");
	}
}
/*
 * Ensures that a non existing internal jar cannot be put on the classpath.
 */
public void testInvalidInternalJar1() throws CoreException {
	try {
		IJavaProject proj = createJavaProject("P", new String[] {}, new String[] {"/P/nonExisting.jar"}, "bin");
		assertBuildPathMarkers(
			"Unexpected markers",
			"Project 'P' is missing required library: \'nonExisting.jar\'",
			proj);
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a file not ending with .jar or .zip can be put on the classpath.
 * (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=182360 )
 */
public void testInvalidInternalJar2() throws CoreException, IOException {
	try {
		IJavaProject proj =  createJavaProject("P1", new String[] {}, new String[0], "bin");

		addLibrary(proj, "existing.txt", null, new String[0],
				new String[]{"META-INF/MANIFEST.MF",
					"Manifest-Version: 1.0\n"} ,
					CompilerOptions.getFirstSupportedJavaVersion());
		proj =  createJavaProject("P2", new String[] {}, new String[] {"/P1/existing.txt"}, "bin");
		assertBuildPathMarkers(
			"Unexpected markers",
			"",
			proj);
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}

/*
 * Ensures that a non existing source folder cannot be put on the classpath.
 * (regression test for bug 66512 Invalid classpath entry not rejected)
 */
public void testInvalidSourceFolder() throws CoreException {
	try {
		createJavaProject("P1");
		IJavaProject proj = createJavaProject("P2", new String[] {}, new String[] {}, new String[] {"/P1/src1/src2"}, "bin");
		assertBuildPathMarkers(
			"Unexpected markers",
			"Project 'P2' is missing required source folder: \'/P1/src1/src2\'",
			proj);
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}
/**
 * Ensures that only one marker is created if building a project that is
 * missing its .classpath file multiple times.
 * (regression test for bug 39877 Rebuild All generates extra "Unable to read classpath" entry.)
 */
public void _testMissingClasspath() throws CoreException {
	try {
		IJavaProject javaProject = createJavaProject("P");
		IProject project = javaProject.getProject();
		project.close(null);
		deleteResource(new File(project.getLocation().toOSString(), ".classpath"));
		waitForAutoBuild();
		project.open(null);
		waitForAutoBuild();
		project.build(IncrementalProjectBuilder.FULL_BUILD, null);
		waitForAutoBuild();
		project.build(IncrementalProjectBuilder.FULL_BUILD, null);
		waitForAutoBuild();
		assertBuildPathMarkers(
			"Unexpected markers",
			"Unable to read \'.classpath\' file of project 'P'",
			javaProject);
	} finally {
		this.deleteProject("P");
	}
}
/**
 * Test that a marker is added when a project as a missing project in its classpath.
 */
public void testMissingPrereq1() throws CoreException {
	try {
		IJavaProject javaProject = this.createJavaProject("A", new String[] {}, "");
		IClasspathEntry[] classpath =
			new IClasspathEntry[] {
				JavaCore.newProjectEntry(new Path("/B"))
			};
		javaProject.setRawClasspath(classpath, null);
		this.assertBuildPathMarkers(
			"Unexpected markers",
			"Project 'A' is missing required Java project: \'B\'",
			javaProject);
	} finally {
		this.deleteProject("A");
	}
}
/**
 * Test that a marker is added when a project as a missing project in its classpath.
 */
public void testMissingPrereq2() throws CoreException {
	try {
		IJavaProject javaProject =
			this.createJavaProject(
				"A",
				new String[] {}, // source folders
				new String[] {}, // lib folders
				new String[] {"/B"}, // projects
				"");
		this.assertBuildPathMarkers(
			"Unexpected markers",
			"Project 'A' is missing required Java project: \'B\'",
			javaProject);
	} finally {
		this.deleteProject("A");
	}
}
/**
 * Test that a marker indicating a missing project is removed when the project is added.
 */
public void testMissingPrereq3() throws CoreException {
	try {
		IJavaProject javaProject =
			this.createJavaProject(
				"A",
				new String[] {}, // source folders
				new String[] {}, // lib folders
				new String[] {"/B"}, // projects
				"");
		this.createJavaProject("B", new String[] {}, "");
		this.assertBuildPathMarkers("Unexpected markers", "", javaProject);
	} finally {
		deleteProjects(new String[] {"A", "B"});
	}
}
/**
 * Test that a marker indicating a cycle is removed when a project in the cycle is deleted
 * and replaced with a missing prereq marker.
 * (regression test for bug 15168 circular errors not reported)
 */
public void testMissingPrereq4() throws CoreException {
	try {
		IJavaProject projectA =
			this.createJavaProject(
				"A",
				new String[] {}, // source folders
				new String[] {}, // lib folders
				new String[] {"/B"}, // projects
				"");
		IJavaProject projectB =
			this.createJavaProject(
				"B",
				new String[] {}, // source folders
				new String[] {}, // lib folders
				new String[] {"/A"}, // projects
				"");
		this.assertBuildPathMarkers(
			"Unexpected markers for project A",
			"One or more cycles were detected in the build path of project 'A'. The paths towards the cycle and cycle are:\n" +
			"->{A, B}",
			projectA);
		this.assertBuildPathMarkers(
			"Unexpected markers for project B",
			"One or more cycles were detected in the build path of project 'B'. The paths towards the cycle and cycle are:\n" +
			"->{A, B}",
			projectB);

		// delete project B
		this.deleteProject("B");
		this.assertBuildPathMarkers(
			"Unexpected markers for project A after deleting of project B",
			"Project 'A' is missing required Java project: \'B\'",
			projectA);

		// add project B back
		projectB =
			this.createJavaProject(
				"B",
				new String[] {}, // source folders
				new String[] {}, // lib folders
				new String[] {"/A"}, // projects
				"");
		this.assertBuildPathMarkers(
			"Unexpected markers for project A after adding project B back",
			"One or more cycles were detected in the build path of project 'A'. The paths towards the cycle and cycle are:\n" +
			"->{A, B}",
			projectA);
		this.assertBuildPathMarkers(
			"Unexpected markers for project B after adding project B back",
			"One or more cycles were detected in the build path of project 'B'. The paths towards the cycle and cycle are:\n" +
			"->{A, B}",
			projectB);

	} finally {
		deleteProjects(new String[] {"A", "B"});
	}
}
/**
 * Setting the classpath to null should be the same as using the
 * default classpath.
 */
public void testNullClasspath() throws CoreException {
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {""}, "");
		setClasspath(project, null);
		IClasspathEntry[] cp= project.getRawClasspath();
		assertTrue("classpath should have one root entry", cp.length == 1 && cp[0].getPath().equals(project.getUnderlyingResource().getFullPath()));
	} finally {
		this.deleteProject("P");
	}
}
/*
 * Ensures that one can set a raw classpath if a pessimistic repository provider is used
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=243692 )
 */
public void testPessimisticProvider() throws CoreException {
	try {
		IJavaProject javaProject = createJavaProject("P", new String[] {"src"}, "bin");
		IProject project = javaProject.getProject();
		try {
			RepositoryProvider.map(project, TestPessimisticProvider.NATURE_ID);
			IClasspathEntry[] rawClasspath = new IClasspathEntry[] {JavaCore.newSourceEntry(new Path("/P/src2"))};
			setClasspath(javaProject, rawClasspath);
			assertClasspathEquals(
				javaProject.getRawClasspath(),
				"/P/src2[CPE_SOURCE][K_SOURCE][isExported:false]"
			);
		} finally {
			RepositoryProvider.unmap(project);
		}
	} finally {
		deleteProject("P");
	}
}
/**
 * Ensure that reading an empty custom output from the .classpath returns a non-null output location.
 * (regression test for 28531 Classpath Entry: Output folder can not be set to project)
 */
public void testReadEmptyCustomOutput() throws CoreException {
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {}, "");
		editFile(
			"/P/.classpath",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"    <classpathentry kind=\"src\" output=\"\" path=\"\"/>\n" +
			"    <classpathentry kind=\"output\" path=\"bin\"/>\n" +
			"</classpath>"
		);
		IClasspathEntry[] classpath = project.getRawClasspath();
		assertEquals("Unexpected classpath length", 1, classpath.length);
		assertEquals("Unexpected custom output location", new Path("/P"), classpath[0].getOutputLocation());
	} finally {
		this.deleteProject("P");
	}
}

/*
 * Ensures that a JavaModelException is thrown if attempting to set a raw classpath
 * when .classpath file is read-only
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=245576 )
 */
public void testReadOnly() throws CoreException {
	if (!org.eclipse.jdt.internal.core.util.Util.isReadOnlySupported())
		return;
	IJavaProject project = this.createJavaProject("P", new String[] {"src"}, "bin");
	IClasspathEntry[] originalCP= project.getRawClasspath();

	IFile classpathFile = null;
	try {
		IClasspathEntry newEntry= JavaCore.newSourceEntry(project.getProject().getFullPath().append("extra"));
		IClasspathEntry[] newCP= new IClasspathEntry[originalCP.length + 1];
		System.arraycopy(originalCP, 0 , newCP, 0, originalCP.length);
		newCP[originalCP.length]= newEntry;

		classpathFile = getFile("/P/.classpath");
		org.eclipse.jdt.internal.core.util.Util.setReadOnly(classpathFile, true);
		JavaModelException expected = null;
		try {
			project.setRawClasspath(newCP, null);
		} catch (JavaModelException e) {
			expected = e;
		}
		assertExceptionEquals("Unexpected exception", "File /P/.classpath is read-only.", expected);
	} finally {
		// cleanup
		if (classpathFile != null) {
			org.eclipse.jdt.internal.core.util.Util.setReadOnly(classpathFile, false);
		}
		this.deleteProject("P");
	}
}

/*
 * Ensures that setting the 'combineAccessRules' flag to false on a project entry generates the correct .classpath file.
 */
public void testCombineAccessRules1() throws CoreException {
	try {
		createJavaProject("P1");
		IJavaProject project = createJavaProject("P2");
		IClasspathEntry entry = JavaCore.newProjectEntry(new Path("/P1"), (IAccessRule[]) null, false/*don't combine*/, new IClasspathAttribute[] {}, false);
		project.setRawClasspath(new IClasspathEntry[] {entry}, null);
		String contents = new String (org.eclipse.jdt.internal.core.util.Util.getResourceContentsAsCharArray(getFile("/P2/.classpath")));
		assertSourceEquals(
			"Unexpected content",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"	<classpathentry combineaccessrules=\"false\" kind=\"src\" path=\"/P1\"/>\n" +
			"	<classpathentry kind=\"output\" path=\"\"/>\n" +
			"</classpath>\n",
			contents);
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}

/*
 * Ensures that setting the 'combineAccessRules' flag to true on a project entry generates the correct .classpath file.
 */
public void testCombineAccessRules2() throws CoreException {
	try {
		createJavaProject("P1");
		IJavaProject project = createJavaProject("P2");
		IClasspathEntry entry = JavaCore.newProjectEntry(new Path("/P1"), (IAccessRule[]) null, true/*combine*/, new IClasspathAttribute[] {}, false);
		project.setRawClasspath(new IClasspathEntry[] {entry}, null);
		String contents = new String (org.eclipse.jdt.internal.core.util.Util.getResourceContentsAsCharArray(getFile("/P2/.classpath")));
		assertSourceEquals(
			"Unexpected content",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"	<classpathentry kind=\"src\" path=\"/P1\"/>\n" +
			"	<classpathentry kind=\"output\" path=\"\"/>\n" +
			"</classpath>\n",
			contents);
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}

/*
 * Ensures that 'combineAccessRules' flag in a .classpath file is correctly read.
 */
public void testCombineAccessRules3() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P2");
		editFile(
			"/P2/.classpath",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"	<classpathentry kind=\"src\" combineaccessrules=\"false\" path=\"/P1\"/>\n" +
			"	<classpathentry kind=\"output\" path=\"\"/>\n" +
			"</classpath>\n"
		);
		assertClasspathEquals(
			project.getRawClasspath(),
			"/P1[CPE_PROJECT][K_SOURCE][isExported:false][combine access rules:false]"
		);
	} finally {
		deleteProject("P2");
	}
}

/*
 * Ensures that the absence of 'combineAccessRules' flag in a .classpath file is correctly handled.
 */
public void testCombineAccessRules4() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P2");
		editFile(
			"/P2/.classpath",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"	<classpathentry kind=\"src\" path=\"/P1\"/>\n" +
			"	<classpathentry kind=\"output\" path=\"\"/>\n" +
			"</classpath>\n"
		);
		assertClasspathEquals(
			project.getRawClasspath(),
			"/P1[CPE_PROJECT][K_SOURCE][isExported:false][combine access rules:true]"
		);
	} finally {
		deleteProject("P2");
	}
}

/*
 * Ensures that the absence of 'combineAccessRules' flag in a .classpath file is correctly handled.
 */
public void testCombineAccessRules5() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P2");
		editFile(
			"/P2/.classpath",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"	<classpathentry kind=\"src\" path=\"src\"/>\n" +
			"	<classpathentry kind=\"output\" path=\"bin\"/>\n" +
			"</classpath>\n"
		);
		assertClasspathEquals(
			project.getRawClasspath(),
			"/P2/src[CPE_SOURCE][K_SOURCE][isExported:false]"
		);
	} finally {
		deleteProject("P2");
	}
}

public void testCycleDetection() throws CoreException {

	int max = 5;
	IJavaProject[] p = new IJavaProject[max];
	String[] projectNames = new String[max];
	try {
		for (int i = 0; i < max; i++) {
			projectNames[i] = "P"+i;
			p[i] = this.createJavaProject(projectNames[i], new String[] {""}, "");
		}

		IClasspathEntry[][] extraEntries = new IClasspathEntry[][]{
			{ JavaCore.newProjectEntry(p[1].getPath()), JavaCore.newProjectEntry(p[3].getPath()) },
			{ JavaCore.newProjectEntry(p[2].getPath()), JavaCore.newProjectEntry(p[3].getPath()) },
			{ JavaCore.newProjectEntry(p[1].getPath()) },
			{ JavaCore.newProjectEntry(p[4].getPath())},
			{ JavaCore.newProjectEntry(p[3].getPath()), JavaCore.newProjectEntry(p[0].getPath()) }
		};

		int[][] expectedCycleParticipants = new int[][] {
			{ 0, 0, 0, 0, 0 }, // after setting CP p[0]
			{ 0, 0, 0, 0, 0 }, // after setting CP p[1]
			{ 0, 1, 1, 0, 0 }, // after setting CP p[2]
			{ 0, 1, 1, 0, 0 }, // after setting CP p[3]
			{ 1, 1, 1, 1, 1 }, // after setting CP p[4]
		};

		int[][] expectedAffectedProjects = new int[][] {
			{ 0, 0, 0, 0, 0 }, // after setting CP p[0]
			{ 0, 0, 0, 0, 0 }, // after setting CP p[1]
			{ 1, 1, 1, 0, 0 }, // after setting CP p[2]
			{ 1, 1, 1, 0, 0 }, // after setting CP p[3]
			{ 1, 1, 1, 1, 1 }, // after setting CP p[4]
		};

		for (int i = 0; i < p.length; i++){

			// append project references
			IClasspathEntry[] oldClasspath = p[i].getRawClasspath();
			IClasspathEntry[] newClasspath = new IClasspathEntry[oldClasspath.length+extraEntries[i].length];
			System.arraycopy(oldClasspath, 0 , newClasspath, 0, oldClasspath.length);
			System.arraycopy(extraEntries[i], 0, newClasspath, oldClasspath.length, extraEntries[i].length);
			// set classpath
			p[i].setRawClasspath(newClasspath, null);

			// check cycle markers
			assertCycleMarkers(p[i], p, expectedCycleParticipants[i], false);
			assertCycleMarkers(p[i], p, expectedAffectedProjects[i], true);
		}
		//this.startDeltas();

	} finally {
		deleteProjects(projectNames);
	}
}


public void testCycleDetectionThroughVariables() throws CoreException {

	int max = 5;
	IJavaProject[] p = new IJavaProject[max];
	String[] projectNames = new String[max];
	try {
		for (int i = 0; i < max; i++) {
			projectNames[i] = "P"+i;
			p[i] = this.createJavaProject(projectNames[i], new String[] {""}, "");
		}

		String[] var = new String[]{ "v0", "v1", "v2"};
		IClasspathEntry[][] extraEntries = new IClasspathEntry[][]{
			{ JavaCore.newProjectEntry(p[1].getPath()), JavaCore.newVariableEntry(new Path(var[0]), null, null) },
			{ JavaCore.newProjectEntry(p[2].getPath()), JavaCore.newProjectEntry(p[3].getPath()) },
			{ JavaCore.newVariableEntry(new Path(var[1]), null, null) },
			{ JavaCore.newVariableEntry(new Path(var[2]), null, null)},
			{ JavaCore.newProjectEntry(p[3].getPath()), JavaCore.newProjectEntry(p[0].getPath()) }
		};

		int[][] expectedCycleParticipants = new int[][] {
			{ 0, 0, 0, 0, 0 }, // after setting CP p[0]
			{ 0, 0, 0, 0, 0 }, // after setting CP p[1]
			{ 0, 0, 0, 0, 0 }, // after setting CP p[2]
			{ 0, 0, 0, 0, 0 }, // after setting CP p[3]
			{ 1, 1, 1, 1, 1 }, // after setting CP p[4]
		};

		IPath[][] variableValues = new IPath[][]{
			{ null, null, null },
			{ null, null, null },
			{ null, null, null },
			{ null, null, null },
			{ p[3].getPath(), p[1].getPath(), p[4].getPath() },
		};

		for (int i = 0; i < p.length; i++){

			// append project references
			IClasspathEntry[] oldClasspath = p[i].getRawClasspath();
			IClasspathEntry[] newClasspath = new IClasspathEntry[oldClasspath.length+extraEntries[i].length];
			System.arraycopy(oldClasspath, 0 , newClasspath, 0, oldClasspath.length);
			System.arraycopy(extraEntries[i], 0, newClasspath, oldClasspath.length, extraEntries[i].length);
			// set classpath
			p[i].setRawClasspath(newClasspath, null);

			// update variable values
			JavaCore.setClasspathVariables(var, variableValues[i], null);

			// check cycle markers
			assertCycleMarkers(p[i], p, expectedCycleParticipants[i], false);
		}
		//this.startDeltas();

	} finally {
		//this.stopDeltas();
		deleteProjects(projectNames);
	}
}

public void testCycleDetectionThroughContainers() throws CoreException {

	int max = 5;
	IJavaProject[] p = new IJavaProject[max];
	String[] projectNames = new String[max];
	try {
		for (int i = 0; i < max; i++) {
			projectNames[i] = "P"+i;
			p[i] = this.createJavaProject(projectNames[i], new String[] {""}, "");
		}

		IClasspathContainer[] containers = new IClasspathContainer[]{
			new TestContainer(
				new Path("container0/default"),
				new IClasspathEntry[]{ JavaCore.newProjectEntry(p[3].getPath()) }),
			new TestContainer(
				new Path("container1/default"),
				new IClasspathEntry[]{ JavaCore.newProjectEntry(p[1].getPath()) }),
			new TestContainer(
				new Path("container2/default"),
				new IClasspathEntry[]{ JavaCore.newProjectEntry(p[4].getPath()) }),
		};

		IClasspathEntry[][] extraEntries = new IClasspathEntry[][]{
			{ JavaCore.newProjectEntry(p[1].getPath()), JavaCore.newContainerEntry(containers[0].getPath()) },
			{ JavaCore.newProjectEntry(p[2].getPath()), JavaCore.newProjectEntry(p[3].getPath()) },
			{ JavaCore.newContainerEntry(containers[1].getPath()) },
			{ JavaCore.newContainerEntry(containers[2].getPath())},
			{ JavaCore.newProjectEntry(p[3].getPath()), JavaCore.newProjectEntry(p[0].getPath()) }
		};

		int[][] expectedCycleParticipants = new int[][] {
			{ 0, 0, 0, 0, 0 }, // after setting CP p[0]
			{ 0, 0, 0, 0, 0 }, // after setting CP p[1]
			{ 0, 0, 0, 0, 0 }, // after setting CP p[2]
			{ 0, 0, 0, 0, 0 }, // after setting CP p[3]
			{ 1, 1, 1, 1, 1 }, // after setting CP p[4]
		};

		for (int i = 0; i < p.length; i++){

			// append project references
			IClasspathEntry[] oldClasspath = p[i].getRawClasspath();
			IClasspathEntry[] newClasspath = new IClasspathEntry[oldClasspath.length+extraEntries[i].length];
			System.arraycopy(oldClasspath, 0 , newClasspath, 0, oldClasspath.length);
			System.arraycopy(extraEntries[i], 0, newClasspath, oldClasspath.length, extraEntries[i].length);
			// set classpath
			p[i].setRawClasspath(newClasspath, null);

			// update container paths
			if (i == p.length - 1){
				JavaCore.setClasspathContainer(
					containers[0].getPath(),
					new IJavaProject[]{ p[0] },
					new IClasspathContainer[] { containers[0] },
					null);

				JavaCore.setClasspathContainer(
					containers[1].getPath(),
					new IJavaProject[]{ p[2] },
					new IClasspathContainer[] { containers[1] },
					null);

				JavaCore.setClasspathContainer(
					containers[2].getPath(),
					new IJavaProject[]{ p[3] },
					new IClasspathContainer[] { containers[2] },
					null);
			}

			// check cycle markers
			assertCycleMarkers(p[i], p, expectedCycleParticipants[i], false);
		}
		//this.startDeltas();

	} finally {
		//this.stopDeltas();
		deleteProjects(projectNames);
	}
}
public void testCycleDetectionThroughContainerVariants() throws CoreException {

	int max = 5;
	IJavaProject[] p = new IJavaProject[max];
	String[] projectNames = new String[max];
	try {
		for (int i = 0; i < max; i++) {
			projectNames[i] = "P"+i;
			p[i] = this.createJavaProject(projectNames[i], new String[] {""}, "");
		}

		class LocalTestContainer implements IClasspathContainer {
			IPath path;
			IClasspathEntry[] entries;
			LocalTestContainer(IPath path, IClasspathEntry[] entries){
				this.path = path;
				this.entries = entries;
			}
			public IPath getPath() { return this.path; }
			public IClasspathEntry[] getClasspathEntries() { return this.entries;	}
			public String getDescription() { return null; 	}
			public int getKind() { return 0; }
		}

		IClasspathContainer[] containers = new IClasspathContainer[]{
			new LocalTestContainer(
				new Path("container0/default"),
				new IClasspathEntry[]{ JavaCore.newProjectEntry(p[3].getPath()) }),
			new LocalTestContainer(
				new Path("container0/default"),
				new IClasspathEntry[]{ JavaCore.newProjectEntry(p[1].getPath()) }),
			new LocalTestContainer(
				new Path("container0/default"),
				new IClasspathEntry[]{ JavaCore.newProjectEntry(p[4].getPath()) }),
		};

		IClasspathEntry[][] extraEntries = new IClasspathEntry[][]{
			{ JavaCore.newProjectEntry(p[1].getPath()), JavaCore.newContainerEntry(containers[0].getPath()) },
			{ JavaCore.newProjectEntry(p[2].getPath()), JavaCore.newProjectEntry(p[3].getPath()) },
			{ JavaCore.newContainerEntry(containers[1].getPath()) },
			{ JavaCore.newContainerEntry(containers[2].getPath())},
			{ JavaCore.newProjectEntry(p[3].getPath()), JavaCore.newProjectEntry(p[0].getPath()) }
		};

		int[][] expectedCycleParticipants = new int[][] {
			{ 0, 0, 0, 0, 0 }, // after setting CP p[0]
			{ 0, 0, 0, 0, 0 }, // after setting CP p[1]
			{ 0, 0, 0, 0, 0 }, // after setting CP p[2]
			{ 0, 0, 0, 0, 0 }, // after setting CP p[3]
			{ 1, 1, 1, 1, 1 }, // after setting CP p[4]
		};

		for (int i = 0; i < p.length; i++){

			// append project references
			IClasspathEntry[] oldClasspath = p[i].getRawClasspath();
			IClasspathEntry[] newClasspath = new IClasspathEntry[oldClasspath.length+extraEntries[i].length];
			System.arraycopy(oldClasspath, 0 , newClasspath, 0, oldClasspath.length);
			System.arraycopy(extraEntries[i], 0, newClasspath, oldClasspath.length, extraEntries[i].length);
			// set classpath
			p[i].setRawClasspath(newClasspath, null);

			// update same container path for multiple projects
			if (i == p.length - 1){
				JavaCore.setClasspathContainer(
					containers[0].getPath(),
					new IJavaProject[]{ p[0], p[2], p[3] },
					new IClasspathContainer[] { containers[0], containers[1], containers[2] },
					null);
			}

			// check cycle markers
			assertCycleMarkers(p[i], p, expectedCycleParticipants[i], false);
		}
		//this.startDeltas();

	} finally {
		//this.stopDeltas();
		deleteProjects(projectNames);
	}
}
public void testCycleDetection2() throws CoreException {

	int max = 5;
	IJavaProject[] p = new IJavaProject[max];
	String[] projectNames = new String[max];
	try {
		for (int i = 0; i < max; i++) {
			projectNames[i] = "P"+i;
			p[i] = this.createJavaProject(projectNames[i], new String[] {""}, "");
		}

		IClasspathEntry[][] extraEntries = new IClasspathEntry[][]{
			{ JavaCore.newProjectEntry(p[1].getPath()), JavaCore.newProjectEntry(p[3].getPath()) },
			{ JavaCore.newProjectEntry(p[2].getPath()) },
			{ JavaCore.newProjectEntry(p[0].getPath()) },
			{ JavaCore.newProjectEntry(p[4].getPath())},
			{ JavaCore.newProjectEntry(p[0].getPath()) }
		};

		int[][] expectedCycleParticipants = new int[][] {
			{ 0, 0, 0, 0, 0 }, // after setting CP p[0]
			{ 0, 0, 0, 0, 0 }, // after setting CP p[1]
			{ 1, 1, 1, 0, 0 }, // after setting CP p[2]
			{ 1, 1, 1, 0, 0 }, // after setting CP p[3]
			{ 1, 1, 1, 1, 1 }, // after setting CP p[4]
		};

		for (int i = 0; i < p.length; i++){

			// append project references
			IClasspathEntry[] oldClasspath = p[i].getRawClasspath();
			IClasspathEntry[] newClasspath = new IClasspathEntry[oldClasspath.length+extraEntries[i].length];
			System.arraycopy(oldClasspath, 0 , newClasspath, 0, oldClasspath.length);
			System.arraycopy(extraEntries[i], 0, newClasspath, oldClasspath.length, extraEntries[i].length);
			// set classpath
			p[i].setRawClasspath(newClasspath, null);

			// check cycle markers
			assertCycleMarkers(p[i], p, expectedCycleParticipants[i], false);
			assertCycleMarkers(p[i], p, expectedCycleParticipants[i], true);
		}
		//this.startDeltas();

	} finally {
		//this.stopDeltas();
		deleteProjects(projectNames);
	}
}

public void testCycleDetection3() throws CoreException {

	int max = 6;
	IJavaProject[] p = new IJavaProject[max];
	String[] projectNames = new String[max];
	try {
		for (int i = 0; i < max; i++) {
			projectNames[i] = "P"+i;
			p[i] = this.createJavaProject(projectNames[i], new String[] {""}, "");
		}

		IClasspathEntry[][] extraEntries = new IClasspathEntry[][]{
			{ JavaCore.newProjectEntry(p[2].getPath()), JavaCore.newProjectEntry(p[4].getPath()) },
			{ JavaCore.newProjectEntry(p[0].getPath()) },
			{ JavaCore.newProjectEntry(p[3].getPath()) },
			{ JavaCore.newProjectEntry(p[1].getPath())},
			{ JavaCore.newProjectEntry(p[5].getPath()) },
			{ JavaCore.newProjectEntry(p[1].getPath()) }
		};

		int[][] expectedCycleParticipants = new int[][] {
			{ 0, 0, 0, 0, 0, 0 }, // after setting CP p[0]
			{ 0, 0, 0, 0, 0, 0 }, // after setting CP p[1]
			{ 0, 0, 0, 0, 0, 0 }, // after setting CP p[2]
			{ 1, 1, 1, 1, 0, 0 }, // after setting CP p[3]
			{ 1, 1, 1, 1, 0, 0 }, // after setting CP p[4]
			{ 1, 1, 1, 1, 1 ,1 }, // after setting CP p[5]
		};

		for (int i = 0; i < p.length; i++){

			// append project references
			IClasspathEntry[] oldClasspath = p[i].getRawClasspath();
			IClasspathEntry[] newClasspath = new IClasspathEntry[oldClasspath.length+extraEntries[i].length];
			System.arraycopy(oldClasspath, 0 , newClasspath, 0, oldClasspath.length);
			System.arraycopy(extraEntries[i], 0, newClasspath, oldClasspath.length, extraEntries[i].length);
			// set classpath
			p[i].setRawClasspath(newClasspath, null);

			// check cycle markers
			assertCycleMarkers(p[i], p, expectedCycleParticipants[i], false);
			assertCycleMarkers(p[i], p, expectedCycleParticipants[i], true);
		}

		IMarker[] markers = p[0].getProject().findMarkers(IJavaModelMarker.BUILDPATH_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);
		// additionally see that we actually have 2 cycles for P0!
		assertMarkers("Markers of P0",
				"One or more cycles were detected in the build path of project 'P0'. The paths towards the cycle and cycle are:\n" +
				"->{P0, P2, P3, P1}\n" +
				"->{P0, P4, P5, P1}",
				markers);
		//this.startDeltas();

	} finally {
		//this.stopDeltas();
		deleteProjects(projectNames);
	}
}
/*
 * Ensures that a cycle is detected if introduced during a post-change event.
 * (regression test for bug 113051 No classpath marker produced when cycle through PDE container)
 */
public void testCycleDetection4() throws CoreException {
	IResourceChangeListener listener = new IResourceChangeListener() {
		boolean containerNeedUpdate = true;
		public void resourceChanged(IResourceChangeEvent event) {
			if (this.containerNeedUpdate) {
				TestContainer container = new TestContainer(
					new Path("org.eclipse.jdt.core.tests.model.container/default"),
					new IClasspathEntry[] { JavaCore.newProjectEntry(new Path("/P1")) });
				try {
					JavaCore.setClasspathContainer(container.getPath(), new IJavaProject[] {getJavaProject("P2")}, new IClasspathContainer[] {container}, null);
				} catch (JavaModelException e) {
					e.printStackTrace();
				}
				this.containerNeedUpdate = false;
			}
		}
	};
	try {
		IJavaProject p1 = createJavaProject("P1", new String[] {}, new String[] {}, new String[] {"/P2"}, "");
		TestContainer container = new TestContainer(
			new Path("org.eclipse.jdt.core.tests.model.container/default"),
			new IClasspathEntry[] {});
		IJavaProject p2 = createJavaProject("P2", new String[] {}, new String[] {container.getPath().toString()}, "");
		JavaCore.setClasspathContainer(container.getPath(), new IJavaProject[] {p2}, new IClasspathContainer[] {container}, null);
		waitForAutoBuild();
		getWorkspace().addResourceChangeListener(listener, IResourceChangeEvent.POST_CHANGE);
		createFile("/P1/test.txt", "");
		assertCycleMarkers(p1, new IJavaProject[] {p1, p2}, new int[] {1, 1}, false);
	} finally {
		getWorkspace().removeResourceChangeListener(listener);
		deleteProjects(new String[] {"P1", "P2"});
	}
}
public void testPerfDenseCycleDetection1() throws CoreException {
	// each project prereqs all other projects
	denseCycleDetection(5);
}
public void testPerfDenseCycleDetection2() throws CoreException {
	// each project prereqs all other projects
	denseCycleDetection(10);
}
public void testPerfDenseCycleDetection3() throws CoreException {
	// each project prereqs all other projects
	denseCycleDetection(20);
}
/*
 * Create projects and set classpaths in one batch
 */
public void testNoCycleDetection1() throws CoreException {

	// each project prereqs all the previous ones
	noCycleDetection(5, false, false);
	noCycleDetection(10, false, false);
	noCycleDetection(20, false, false);

	// each project prereqs all the next ones
	noCycleDetection(5, true, false);
	noCycleDetection(10, true, false);
	noCycleDetection(20, true, false);
}
/*
 * Create projects first, then set classpaths
 */
public void testNoCycleDetection2() throws CoreException {

	// each project prereqs all the previous ones
	noCycleDetection(5, false, true);
	noCycleDetection(10, false, true);
	noCycleDetection(20, false, true);

	// each project prereqs all the next ones
	noCycleDetection(5, true, true);
	noCycleDetection(10, true, true);
	noCycleDetection(20, true, true);
}
/*
 * Ensures that the .classpath file is not written to disk when setting the raw classpath with no resource change.
 */
public void testNoResourceChange01() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src1"}, "bin");
		IClasspathEntry[] newClasspath = createClasspath("P", new String[] {"/P/src2", ""});
		project.setRawClasspath(newClasspath, false/*cannot modify resources*/, null/*no progress*/);
		String contents = new String (org.eclipse.jdt.internal.core.util.Util.getResourceContentsAsCharArray(getFile("/P/.classpath")));
		assertSourceEquals(
			"Unexpected content",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"	<classpathentry kind=\"src\" path=\"src1\"/>\n" +
			"	<classpathentry kind=\"output\" path=\"bin\"/>\n" +
			"</classpath>\n",
			contents);
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that the in-memory classpath is correct when setting the raw classpath with no resource change.
 */
public void testNoResourceChange02() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src1"}, "bin");
		IClasspathEntry[] newClasspath = createClasspath("P", new String[] {"/P/src2", ""});
		project.setRawClasspath(newClasspath, false/*cannot modify resources*/, null/*no progress*/);
		assertClasspathEquals(
			project.getRawClasspath(),
			"/P/src2[CPE_SOURCE][K_SOURCE][isExported:false]"
		);
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a compilation unit on the old classpath doesn't exist after setting a new raw classpath with no resource change.
 */
public void testNoResourceChange03() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src1"}, "bin");
		createFile(
			"/P/src1/X.java",
			"public class X {\n" +
			"}"
		);
		ICompilationUnit cu = getCompilationUnit("/P/src1/X.java");
		cu.open(null);
		IClasspathEntry[] newClasspath = createClasspath("P", new String[] {"/P/src2", ""});
		project.setRawClasspath(newClasspath, false/*cannot modify resources*/, null/*no progress*/);
		assertFalse("Compilation unit should not exist", cu.exists());
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that the delta is correct when setting a new raw classpath with no resource change.
 */
public void testNoResourceChange04() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src1"}, "bin");
		createFolder("/P/src2");
		IClasspathEntry[] newClasspath = createClasspath("P", new String[] {"/P/src2", ""});
		startDeltas();
		project.setRawClasspath(newClasspath, false/*cannot modify resources*/, null/*no progress*/);
		assertDeltas(
			"Unexpected delta",
			"P[*]: {CHILDREN | RAW CLASSPATH CHANGED | RESOLVED CLASSPATH CHANGED}\n" +
			"	src1[*]: {REMOVED FROM CLASSPATH}\n" +
			"	src2[*]: {ADDED TO CLASSPATH}",
			false/*don't wait for resource delta*/
		);
	} finally {
		stopDeltas();
		deleteProject("P");
	}
}
/*
 * Ensures that the in-memory output is correct when setting the output with no resource change.
 */
public void testNoResourceChange05() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src1"}, "bin");
		project.setRawClasspath(project.getRawClasspath(), new Path("/P/bin2"), false/*cannot modify resources*/, null/*no progress*/);
		assertEquals(
			"/P/bin2",
			project.getOutputLocation().toString());
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that no error is reported when deleting a project after setting its raw classpath with no resource change.
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=216895 )
 */
public void testNoResourceChange06() throws CoreException {
	ILogListener listener = new ILogListener(){
		private final StringBuilder buffer = new StringBuilder();
		public void logging(IStatus status, String plugin) {
			this.buffer.append(status);
			this.buffer.append('\n');
		}
		public String toString() {
			return this.buffer.toString();
		}
	};
	try {
		Platform.addLogListener(listener);
		IJavaProject project = createJavaProject("P", new String[] {"src1"}, "bin");
		IClasspathEntry[] newClasspath = createClasspath("P", new String[] {"/P/src2", ""});
		project.setRawClasspath(newClasspath, false/*cannot modify resources*/, null/*no progress*/);
		deleteProject("P");
		assertSourceEquals(
			"Unexpected error logged",
			"",
			listener.toString());
	} finally {
		Platform.removeLogListener(listener);
		deleteProject("P");
	}
}
/**
 * Ensures that a duplicate entry created by editing the .classpath is detected.
 * (regression test for bug 24498 Duplicate entries on classpath cause CP marker to no longer refresh)
 */
public void testDuplicateEntries1() throws CoreException {
	try {
		IJavaProject project = this.createJavaProject("P", new String[] {"src"}, "bin");
		editFile(
			"/P/.classpath",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"    <classpathentry kind=\"src\" path=\"src\"/>\n" +
			"    <classpathentry kind=\"src\" path=\"src\"/>\n" +
			"    <classpathentry kind=\"output\" path=\"bin\"/>\n" +
			"</classpath>"
		);
		assertBuildPathMarkers(
			"Unexpected markers",
			"Build path contains duplicate entry: \'src\' for project 'P'",
			project);
	} finally {
		this.deleteProject("P");
	}
}
/*
 * Ensures that duplicate entries due to resolution are not reported
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=175226 )
 */
public void testDuplicateEntries2() throws CoreException, IOException {
	try {
		IJavaProject project = createJavaProject("P");
		VariablesInitializer.setInitializer(new DefaultVariableInitializer(new String[] {"TEST_LIB", "/P/lib.jar"}));
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P", "/P/lib.jar"}));
		addLibrary(project, "lib.jar", null, new String[0],
				new String[]{"META-INF/MANIFEST.MF",
					"Manifest-Version: 1.0\n"} ,
					CompilerOptions.getFirstSupportedJavaVersion());
		editFile(
			"/P/.classpath",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"    <classpathentry kind=\"var\" path=\"TEST_LIB\"/>\n" +
			"    <classpathentry kind=\"con\" path=\"org.eclipse.jdt.core.tests.model.TEST_CONTAINER\"/>\n" +
			"    <classpathentry kind=\"output\" path=\"bin\"/>\n" +
			"</classpath>"
		);
		waitForAutoBuild();
		assertBuildPathMarkers(
			"Unexpected markers",
			"",
			project);
	} finally {
		ContainerInitializer.setInitializer(null);
		VariablesInitializer.setInitializer(null);
		deleteProject("P");
	}
}
/*
 * Ensures that the resolved classpath doesn't contain duplicate entries due to resolution
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=175226 )
 */
public void testDuplicateEntries3() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P");
		VariablesInitializer.setInitializer(new DefaultVariableInitializer(new String[] {"TEST_LIB", "/P/lib.jar"}));
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P", "/P/lib.jar"}));
		createFile("/P/lib.jar", "");
		editFile(
			"/P/.classpath",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"    <classpathentry kind=\"var\" path=\"TEST_LIB\"/>\n" +
			"    <classpathentry kind=\"con\" path=\"org.eclipse.jdt.core.tests.model.TEST_CONTAINER\"/>\n" +
			"    <classpathentry kind=\"output\" path=\"bin\"/>\n" +
			"</classpath>"
		);
		assertClasspathEquals(
			project.getResolvedClasspath(true),
			"/P/lib.jar[CPE_LIBRARY][K_BINARY][isExported:false]"
		);
	} finally {
		ContainerInitializer.setInitializer(null);
		VariablesInitializer.setInitializer(null);
		deleteProject("P");
	}
}
private void denseCycleDetection(final int numberOfParticipants) throws CoreException {

	final IJavaProject[] projects = new IJavaProject[numberOfParticipants];
	final String[] projectNames  = new String[numberOfParticipants];
	final int[] allProjectsInCycle = new int[numberOfParticipants];

	try {
		JavaCore.run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				for (int i = 0; i < numberOfParticipants; i++){
					projectNames[i] = "P"+i;
					projects[i] = createJavaProject(projectNames[i], new String[]{""}, "");
					allProjectsInCycle[i] = 1;
				}
				for (int i = 0; i < numberOfParticipants; i++){
					IClasspathEntry[] extraEntries = new IClasspathEntry[numberOfParticipants-1];
					int index = 0;
					for (int j = 0; j < numberOfParticipants; j++){
						if (i == j) continue;
						extraEntries[index++] = JavaCore.newProjectEntry(projects[j].getPath());
					}
					// append project references
					IClasspathEntry[] oldClasspath = projects[i].getRawClasspath();
					IClasspathEntry[] newClasspath = new IClasspathEntry[oldClasspath.length+extraEntries.length];
					System.arraycopy(oldClasspath, 0 , newClasspath, 0, oldClasspath.length);
					System.arraycopy(extraEntries, 0, newClasspath, oldClasspath.length, extraEntries.length);
					// set classpath
					projects[i].setRawClasspath(newClasspath, null);
				}
			}
		},
		null);

		for (int i = 0; i < numberOfParticipants; i++){
			// check cycle markers
			assertCycleMarkers(projects[i], projects, allProjectsInCycle, false);
		}

	} finally {
		deleteProjects(projectNames);
	}
}
/*
 * When using forward references, all projects prereq all of the ones which have a greater index,
 * e.g. P0, P1, P2:  P0 prereqs {P1, P2}, P1 prereqs {P2}, P2 prereqs {}
 * When no using forward references (i.e. backward refs), all projects prereq projects with smaller index
 * e.g. P0, P1, P2:  P0 prereqs {}, P1 prereqs {P0}, P2 prereqs {P0, P1}
 */
private void noCycleDetection(final int numberOfParticipants, final boolean useForwardReferences, final boolean createProjectsFirst) throws CoreException {

	final IJavaProject[] projects = new IJavaProject[numberOfParticipants];
	final String[] projectNames  = new String[numberOfParticipants];
	final int[] allProjectsInCycle = new int[numberOfParticipants];

	final long[] start = new long[1];
	final long[] time = new long[1];

	try {
		if (createProjectsFirst) {
			JavaCore.run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					for (int i = 0; i < numberOfParticipants; i++){
						projectNames[i] = "P"+i;
						projects[i] = createJavaProject(projectNames[i], new String[]{""}, "");
						allProjectsInCycle[i] = 0;
					}
				}
			},
			null);
		}
		JavaCore.run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				if (!createProjectsFirst) {
					for (int i = 0; i < numberOfParticipants; i++){
						projectNames[i] = "P"+i;
						projects[i] = createJavaProject(projectNames[i], new String[]{""}, "");
						allProjectsInCycle[i] = 0;
					}
				}
				for (int i = 0; i < numberOfParticipants; i++){
					IClasspathEntry[] extraEntries = new IClasspathEntry[useForwardReferences ? numberOfParticipants - i -1 : i];
					int index = 0;
					for (int j = useForwardReferences ? i+1 : 0;
						useForwardReferences ? (j < numberOfParticipants) : (j < i);
						j++){
						extraEntries[index++] = JavaCore.newProjectEntry(projects[j].getPath());
					}
					// append project references
					IClasspathEntry[] oldClasspath = projects[i].getRawClasspath();
					IClasspathEntry[] newClasspath = new IClasspathEntry[oldClasspath.length+extraEntries.length];
					System.arraycopy(oldClasspath, 0 , newClasspath, 0, oldClasspath.length);
					System.arraycopy(extraEntries, 0, newClasspath, oldClasspath.length, extraEntries.length);
					// set classpath
					long innerStart = System.currentTimeMillis(); // time spent in individual CP setting
					projects[i].setRawClasspath(newClasspath, null);
					time[0] += System.currentTimeMillis() - innerStart;
				}
				start[0] = System.currentTimeMillis(); // time spent in delta refresh
			}
		},
		null);
		time[0] += System.currentTimeMillis()-start[0];
		//System.out.println("No cycle check ("+numberOfParticipants+" participants) : "+ time[0]+" ms, "+ (useForwardReferences ? "forward references" : "backward references") + ", " + (createProjectsFirst ? "two steps (projects created first, then classpaths are set)" : "one step (projects created and classpaths set in one batch)"));

		for (int i = 0; i < numberOfParticipants; i++){
			// check cycle markers
			assertCycleMarkers(projects[i], projects, allProjectsInCycle, false);
		}

	} finally {
		deleteProjects(projectNames);
	}
}

/*
 * test for bug 32690
 * simulate checkout of project with invalid classpath
 */
public void testNestedSourceFolders() throws CoreException, IOException {
	try {
		// create project using Platform/Resources API
		final IProject project = getProject("P");
		IWorkspaceRunnable create = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				project.create(null, null);
				project.open(null);
			}
		};
		getWorkspace().run(create, null);

		// create folders src and src/src2 using java.io API
		File pro = project.getLocation().toFile();
		File src = createFolder(pro, "src");
		createFolder(src, "src2");

		// create .project using java.io API
		createFile(pro, ".project",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<projectDescription>\n" +
			"	<name>org.eclipse.jdt.core</name>\n" +
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
			"</projectDescription>");

		// create .classpath using java.io API
		createFile(pro, ".classpath",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"    <classpathentry kind=\"src\" path=\"src\"/>\n" +
			"    <classpathentry kind=\"src\" path=\"src/src2\"/>\n" +
			"    <classpathentry kind=\"output\" path=\"bin\"/>\n" +
			"</classpath>"
		);

		// refresh
		project.refreshLocal(IResource.DEPTH_INFINITE, null);

		assertBuildPathMarkers(
			"Unexpected markers",
			"Cannot nest \'P/src/src2\' inside \'P/src\'. To enable the nesting exclude \'src2/\' from \'P/src\'",
			JavaCore.create(project));
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that no problems are reported for an optional source entry with no corresponding folder.
 */
public void testOptionalEntry1() throws CoreException {
	try {
		IJavaProject javaProject = this.createJavaProject("A", new String[] {}, "");
		IClasspathAttribute attribute = JavaCore.newClasspathAttribute(IClasspathAttribute.OPTIONAL, "true");
		IClasspathEntry[] classpath =
			new IClasspathEntry[] {
				JavaCore.newSourceEntry(new Path("/A/src"), new IPath[0], new IPath[0], new Path("/A/bin"), new IClasspathAttribute[] {attribute})
			};
		javaProject.setRawClasspath(classpath, null);
		assertBuildPathMarkers(
			"Unexpected markers",
			"",
			javaProject);
	} finally {
		this.deleteProject("A");
	}
}
/*
 * Ensures that no problems are reported for an optional libary entry with no corresponding folder.
 */
public void testOptionalEntry2() throws CoreException {
	try {
		IJavaProject javaProject = this.createJavaProject("A", new String[] {}, "");
		IClasspathAttribute attribute = JavaCore.newClasspathAttribute(IClasspathAttribute.OPTIONAL, "true");
		IClasspathEntry[] classpath =
			new IClasspathEntry[] {
				JavaCore.newLibraryEntry(new Path("/A/lib"), null, null, null, new IClasspathAttribute[] {attribute}, false)
			};
		javaProject.setRawClasspath(classpath, null);
		assertBuildPathMarkers(
			"Unexpected markers",
			"",
			javaProject);
	} finally {
		this.deleteProject("A");
	}
}
/*
 * Ensures that no problems are reported for an optional project entry with no corresponding project.
 */
public void testOptionalEntry3() throws CoreException {
	try {
		IJavaProject javaProject = this.createJavaProject("A", new String[] {}, "");
		IClasspathAttribute attribute = JavaCore.newClasspathAttribute(IClasspathAttribute.OPTIONAL, "true");
		IClasspathEntry[] classpath =
			new IClasspathEntry[] {
				JavaCore.newProjectEntry(new Path("/B"), null/*no access rules*/, false/*don't combine access rule*/, new IClasspathAttribute[] {attribute}, false/*not exported*/)
			};
		javaProject.setRawClasspath(classpath, null);
		assertBuildPathMarkers(
			"Unexpected markers",
			"",
			javaProject);
	} finally {
		this.deleteProject("A");
	}
}
/*
 * test for bug 32974
 */
public void testOutputFolder1() throws CoreException, IOException {
	try {
		// create project using Platform/Resources API
		final IProject project = getProject("P");
		IWorkspaceRunnable create = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				project.create(null, null);
				project.open(null);
			}
		};
		getWorkspace().run(create, null);

		// create folders src and src/src2 using java.io API
		File pro = project.getLocation().toFile();
		File src = createFolder(pro, "src");
		createFolder(src, "src2");

		// create .project using java.io API
		createFile(pro, ".project",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<projectDescription>\n" +
			"	<name>org.eclipse.jdt.core</name>\n" +
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
			"</projectDescription>");

		// create .classpath using java.io API
		createFile(pro, ".classpath",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"    <classpathentry kind=\"src\" output=\"bin2\" path=\"src1\"/>\n" +
			"    <classpathentry kind=\"src\" path=\"src2\"/>\n" +
			"    <classpathentry kind=\"output\" path=\"bin\"/>\n" +
			"</classpath>"
		);

		// refresh
		project.refreshLocal(IResource.DEPTH_INFINITE,null);

		assertBuildPathMarkers(
			"Unexpected markers",
			"Project 'P' is missing required source folder: \'src1\'\n" +
			"Project 'P' is missing required source folder: \'src2\'",
			JavaCore.create(project));
	} finally {
		deleteProject("P");
	}
}

/**
 * Ensure classpath is refreshed when project is replaced (43670)
 */
public void testReplaceProject() throws CoreException {
	try {
		final IJavaProject javaProject = createJavaProject("P", new String[] {"src"}, "bin");

		ResourcesPlugin.getWorkspace().run(
			new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					IProjectDescription descr = javaProject.getProject().getDescription();
					descr.setComment("dummy"); // ensure it is changed
					javaProject.getProject().setDescription(descr, monitor);
					editFile(
						"/P/.classpath",
						"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
						"<classpath>\n" +
						"    <classpathentry kind=\"src\" path=\"src2\"/>\n" +
						"    <classpathentry kind=\"output\" path=\"bin\"/>\n" +
						"</classpath>"
					);
				}
			},
			null);
		IClasspathEntry[] classpath = javaProject.getRawClasspath();
		assertEquals("classpath should have been refreshed", new Path("/P/src2"), classpath[0].getPath());
	} finally {
		deleteProject("P");
	}
}

/*
 * Ensures that removing a library entry for an existing external library folder doesn't generate a marker
 */
public void testRemoveExternalLibFolder1() throws CoreException {
	try {
		createExternalFolder("externalLib");
		IJavaProject p = createJavaProject("P", new String[0], new String[] {getExternalResourcePath("externalLib")}, "");
		waitForAutoBuild();
		setClasspath(p, new IClasspathEntry[] {});
		assertBuildPathMarkers("Unexpected markers", "", p);
	} finally {
		deleteExternalResource("externalLib");
		deleteProject("P");
	}
}

/*
 * Ensures that removing a library entry for a non-existing external library folder removes the marker
 */
public void testRemoveExternalLibFolder2() throws CoreException {
	try {
		IJavaProject p = createJavaProject("P", new String[0], new String[] {getExternalResourcePath("externalLib")}, "");
		waitForAutoBuild();
		setClasspath(p, new IClasspathEntry[] {});
		assertBuildPathMarkers(
			"Unexpected markers",
			"",
			p);
	} finally {
		deleteProject("P");
	}
}

/*
 * Ensures that removing an external library folder referenced by a library entry creates a marker
 */
public void testRemoveExternalLibFolder3() throws CoreException {
	try {
		createExternalFolder("externalLib");
		IJavaProject p = createJavaProject("P", new String[0], new String[] {getExternalResourcePath("externalLib")}, "");
		waitForAutoBuild();
		deleteExternalResource("externalLib");
		refresh(p);
		assertBuildPathMarkers(
			"Unexpected markers",
			"Project \'P\' is missing required library: \'"+ getExternalPath() + "externalLib\'",
			p);
	} finally {
		deleteExternalResource("externalLib");
		deleteProject("P");
	}
}

/*
 * Ensures that removing an external library folder referenced by a library entry and refreshing after a restart
 * creates a marker
 */
public void testRemoveExternalLibFolder4() throws CoreException {
	try {
		simulateExitRestart();
		createExternalFolder("externalLib");
		IJavaProject p = createJavaProject("P", new String[0], new String[] {getExternalResourcePath("externalLib")}, "");
		waitForAutoBuild();
		deleteExternalResource("externalLib");
		refresh(p);
		assertBuildPathMarkers(
			"Unexpected markers",
			"Project \'P\' is missing required library: \'"+ getExternalPath() + "externalLib\'",
			p);
	} finally {
		deleteExternalResource("externalLib");
		deleteProject("P");
	}
}

/*
 * Ensures that removing a library entry for an existing external ZIP archive doesn't generate a marker
 */
public void testRemoveZIPArchive1() throws CoreException {
	try {
		createExternalFile("externalLib.abc", "");
		IJavaProject p = createJavaProject("P", new String[0], new String[] {getExternalResourcePath("externalLib.abc")}, "");
		refreshExternalArchives(p);
		waitForAutoBuild();

		setClasspath(p, new IClasspathEntry[] {});
		assertBuildPathMarkers("Unexpected markers", "", p);
	} finally {
		deleteExternalResource("externalLib.abc");
		deleteProject("P");
	}
}

/*
 * Ensures that removing a library entry for a non-existing external ZIP archive removes the marker
 */
public void testRemoveZIPArchive2() throws CoreException {
	try {
		IJavaProject p = createJavaProject("P", new String[0], new String[] {getExternalResourcePath("externalLib.abc")}, "");
		refreshExternalArchives(p);
		waitForAutoBuild();

		setClasspath(p, new IClasspathEntry[] {});
		assertBuildPathMarkers(
			"Unexpected markers",
			"",
			p);
	} finally {
		deleteProject("P");
	}
}

/*
 * Ensures that removing an external ZIP archive referenced by a library entry creates a marker
 */
public void testRemoveZIPArchive3() throws CoreException {
	try {
		createExternalFile("externalLib.abc", "");
		IJavaProject p = createJavaProject("P", new String[0], new String[] {getExternalResourcePath("externalLib.abc")}, "");
		refreshExternalArchives(p);
		waitForAutoBuild();

		deleteExternalResource("externalLib.abc");
		refreshExternalArchives(p);
		assertBuildPathMarkers(
			"Unexpected markers",
			"Project \'P\' is missing required library: \'"+ getExternalPath() + "externalLib.abc\'",
			p);
	} finally {
		deleteExternalResource("externalLib.abc");
		deleteProject("P");
	}
}

/*
 * Ensures that removing an external ZIP archive referenced by a library entry and refreshing after a restart
 * creates a marker
 */
public void testRemoveZIPArchive4() throws CoreException {
	try {
		simulateExitRestart();
		createExternalFile("externalLib.abc", "");
		IJavaProject p = createJavaProject("P", new String[0], new String[] {getExternalResourcePath("externalLib.abc")}, "");
		refreshExternalArchives(p);

		deleteExternalResource("externalLib.abc");
		refreshExternalArchives(p);
		assertBuildPathMarkers(
			"Unexpected markers",
			"Project \'P\' is missing required library: \'"+ getExternalPath() + "externalLib.abc\'",
			p);
	} finally {
		deleteExternalResource("externalLib.abc");
		deleteProject("P");
	}
}

/*
 * Ensures that removing a library entry for an existing internal ZIP archive doesn't generate a marker
 */
public void testRemoveZIPArchive5() throws CoreException {
	try {
		IJavaProject p = createJavaProject("P", new String[0], new String[] {"/P/internalLib.abc"}, "");
		createFile("/P/internalLib.abc", "");
		waitForAutoBuild();

		setClasspath(p, new IClasspathEntry[] {});
		assertBuildPathMarkers("Unexpected markers", "", p);
	} finally {
		deleteProject("P");
	}
}

/*
 * Ensures that removing an internal ZIP archive referenced by a library entry creates a marker
 */
public void testRemoveZIPArchive6() throws CoreException, IOException {
	try {
		IJavaProject p = createJavaProject("P", new String[0], new String[0], "");

		addLibrary(p, "internalLib.abc", null, new String[0],
				new String[]{"META-INF/MANIFEST.MF",
					"Manifest-Version: 1.0\n"} ,
					CompilerOptions.getFirstSupportedJavaVersion());
		waitForAutoBuild();

		deleteFile("/P/internalLib.abc");
		waitForAutoBuild();
		assertBuildPathMarkers(
			"Unexpected markers",
			"Project \'P\' is missing required library: \'internalLib.abc\'",
			p);
	} finally {
		deleteProject("P");
	}
}

/*
 * Ensures that renaming a .jar file and updating the classpath in a PRE_BUILD event doesn't leave markers
 * (regression test for bug 177922 FlexibleProjectContainer refresh logic sporadically leaves project with "missing library" error on rename/delete)
 */
public void testRenameJar() throws CoreException, IOException {
	IResourceChangeListener listener = null;
	try {
		final IJavaProject p = createJavaProject("P", new String[0], new String[0], "");
		createFolder("/P/lib");
		addLibrary(p, "lib/test1.jar", null, new String[0],
				new String[]{"META-INF/MANIFEST.MF",
					"Manifest-Version: 1.0\n"} ,
					CompilerOptions.getFirstSupportedJavaVersion());
		// at this point no markers exist

		// register a listener that updates the classpath in a PRE_BUILD event
		listener = new IResourceChangeListener(){
			public void resourceChanged(IResourceChangeEvent event) {
				try {
					p.setRawClasspath(new IClasspathEntry[] {JavaCore.newLibraryEntry(new Path("/P/lib/test2.jar"), null, null)}, null);
				} catch (JavaModelException e) {
					e.printStackTrace();
				}
			}
		};
		getWorkspace().addResourceChangeListener(listener, IResourceChangeEvent.PRE_BUILD);

		// rename .jar
		getFile("/P/lib/test1.jar").move(new Path("/P/lib/test2.jar"), false, null);
		assertBuildPathMarkers("Unexpected markers", "", p);
	} finally {
		if (listener != null)
			getWorkspace().removeResourceChangeListener(listener);
		deleteProject("P");
	}
}

/*
 * Ensures that updating the project references doesn't throw a runtime excetion
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=216747 )
 */
public void testUpdateProjectReferences() throws CoreException {
	try {
		createJavaProject("P1");
		IJavaProject p = createJavaProject("P2");
		p.setRawClasspath(new IClasspathEntry[] {JavaCore.newProjectEntry(new Path("/P1"))}, null);
		IProject[] references = p.getProject().getReferencedProjects();
		assertResourcesEqual(
			"Unexpected resources",
			"/P1",
			references);
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}

/*
 * Ensures that unknown classpath attributes in a .classpath file are not lost when read and rewritten.
 * (regression test for bug 101425 Classpath persistence should be resilient with unknown attributes)
 */
public void testUnknownAttributes() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P");
		editFile(
			"/P/.classpath",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"	<classpathentry unknown=\"test\" kind=\"src\" path=\"src1\"/>\n" +
			"	<classpathentry kind=\"src\" path=\"src2\"/>\n" +
			"	<classpathentry kind=\"output\" path=\"bin\"/>\n" +
			"</classpath>\n"
		);
		IClasspathEntry[] classpath = project.getRawClasspath();

		// swap 2 entries
		IClasspathEntry src1 = classpath[0];
		classpath[0] = classpath[1];
		classpath[1] = src1;
		project.setRawClasspath(classpath, null);

		// check that .classpath has correct content
		String contents = new String (org.eclipse.jdt.internal.core.util.Util.getResourceContentsAsCharArray(getFile("/P/.classpath")));
		assertSourceEquals(
			"Unexpected content",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"	<classpathentry kind=\"src\" path=\"src2\"/>\n" +
			"	<classpathentry kind=\"src\" path=\"src1\" unknown=\"test\"/>\n" +
			"	<classpathentry kind=\"output\" path=\"bin\"/>\n" +
			"</classpath>\n",
			contents);
	} finally {
		deleteProject("P");
	}
}

/*
 * Ensures that unknown classpath elements in a .classpath file are not lost when read and rewritten.
 * (regression test for bug 101425 Classpath persistence should be resilient with unknown attributes)
 */
public void testUnknownElements1() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P");
		editFile(
			"/P/.classpath",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"	<classpathentry kind=\"src\" path=\"src1\">\n" +
			"		<unknown>\n" +
			"			<test kind=\"\"/>\n" +
			"		</unknown>\n" +
			"	</classpathentry>\n" +
			"	<classpathentry kind=\"src\" path=\"src2\"/>\n" +
			"	<classpathentry kind=\"output\" path=\"bin\"/>\n" +
			"</classpath>\n"
		);
		IClasspathEntry[] classpath = project.getRawClasspath();

		// swap 2 entries
		IClasspathEntry src1 = classpath[0];
		classpath[0] = classpath[1];
		classpath[1] = src1;
		project.setRawClasspath(classpath, null);

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		waitForManualRefresh();

		// check that .classpath has correct content
		String contents = new String (org.eclipse.jdt.internal.core.util.Util.getResourceContentsAsCharArray(getFile("/P/.classpath")));
		assertSourceEquals(
			"Unexpected content",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"	<classpathentry kind=\"src\" path=\"src2\"/>\n" +
			"	<classpathentry kind=\"src\" path=\"src1\">\n" +
			"		<unknown>\n" +
			"			<test kind=\"\"/>\n" +
			"		</unknown>\n" +
			"	</classpathentry>\n" +
			"	<classpathentry kind=\"output\" path=\"bin\"/>\n" +
			"</classpath>\n",
			contents);
	} finally {
		deleteProject("P");
	}
}

/*
 * Ensures that unknown classpath elements in a .classpath file are not lost when read and rewritten.
 * (regression test for bug 101425 Classpath persistence should be resilient with unknown attributes)
 */
public void testUnknownElements2() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P");
		editFile(
			"/P/.classpath",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"	<classpathentry kind=\"src\" unknownattribute=\"abcde\" path=\"src1\">\n" +
			"		<unknown1>\n" +
			"			<test kind=\"1\"/>\n" +
			"			<test kind=\"2\"/>\n" +
			"		</unknown1>\n" +
			"		<unknown2 attribute2=\"\">\n" +
			"			<test>\n" +
			"				<other a=\"b\"/>\n" +
			"			</test>\n" +
			"		</unknown2>\n" +
			"	</classpathentry>\n" +
			"	<classpathentry kind=\"src\" path=\"src2\"/>\n" +
			"	<classpathentry kind=\"output\" path=\"bin\"/>\n" +
			"</classpath>\n"
		);
		IClasspathEntry[] classpath = project.getRawClasspath();

		// swap 2 entries
		IClasspathEntry src1 = classpath[0];
		classpath[0] = classpath[1];
		classpath[1] = src1;
		project.setRawClasspath(classpath, null);

		// check that .classpath has correct content
		String contents = new String (org.eclipse.jdt.internal.core.util.Util.getResourceContentsAsCharArray(getFile("/P/.classpath")));
		assertSourceEquals(
			"Unexpected content",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"	<classpathentry kind=\"src\" path=\"src2\"/>\n" +
			"	<classpathentry kind=\"src\" path=\"src1\" unknownattribute=\"abcde\">\n" +
			"		<unknown1>\n" +
			"			<test kind=\"1\"/>\n" +
			"			<test kind=\"2\"/>\n" +
			"		</unknown1>\n" +
			"		<unknown2 attribute2=\"\">\n" +
			"			<test>\n" +
			"				<other a=\"b\"/>\n" +
			"			</test>\n" +
			"		</unknown2>\n" +
			"	</classpathentry>\n" +
			"	<classpathentry kind=\"output\" path=\"bin\"/>\n" +
			"</classpath>\n",
			contents);
	} finally {
		deleteProject("P");
	}
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=55992
 * Check that Assert.AssertionFailedException exception is well catched
 * 	a) when verifying a classpath entry
 * 	b) when verifying whole classpath
 */
public void testBug55992a() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {}, "");

		IPath path = getExternalJCLPath();
		IPath sourceAttachmentPath = new Path("jclMin.zip");
		JavaCore.setClasspathVariables(
			new String[] {"TEST_LIB", "TEST_SRC"},
			new IPath[] {path, sourceAttachmentPath},
			null);

		ClasspathEntry cp = new ClasspathEntry(
			IPackageFragmentRoot.K_SOURCE,
			IClasspathEntry.CPE_VARIABLE,
			new Path("TEST_LIB"),
			ClasspathEntry.INCLUDE_ALL,
			ClasspathEntry.EXCLUDE_NONE,
			new Path("TEST_SRC"),
			null,
			null,
			null, // specific output folder
			false,
			(IAccessRule[]) null,
			false,
			ClasspathEntry.NO_EXTRA_ATTRIBUTES);
		IJavaModelStatus status = JavaConventions.validateClasspathEntry(proj, cp, false);
		assertEquals(
			"Source attachment path \'jclMin.zip\' for IClasspathEntry must be absolute",
			status.getMessage());
	} finally {
		this.deleteProject("P");
	}
}
public void testBug55992b() throws CoreException {

	boolean autoBuild = getWorkspace().isAutoBuilding();
	IWorkspaceDescription preferences = getWorkspace().getDescription();
	try {
		preferences.setAutoBuilding(false);
		IJavaProject javaProject = this.createJavaProject("P", new String[] {"src", "lib"}, "bin");
		JavaCore.setClasspathVariables(
			new String[] {"TEST_LIB", "TEST_SRC"},
			new IPath[] {new Path("/lib/tmp.jar"), new Path("tmp.zip")},
			null);
		editFile(
			"/P/.classpath",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"    <classpathentry kind=\"src\" path=\"src\"/>\n" +
			"    <classpathentry kind=\"output\" path=\"bin\"/>\n" +
			"    <classpathentry kind=\"var\" path=\"TEST_LIB\" sourcepath=\"TEST_SRC\"/>\n" +
			"</classpath>"
		);
		assertBuildPathMarkers(
			"Unexpected markers",
			"Source attachment path \'tmp.zip\' for IClasspathEntry must be absolute",
			javaProject);
	} finally {
		try {
			this.deleteProject("P");
		} finally {
			restoreAutobuild(preferences, autoBuild);
		}
	}
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=61214
 */
public void testRemoveDuplicates() throws CoreException {
	try {
		IJavaProject p1 = this.createJavaProject("P1", new String[] {""}, "");
		IClasspathEntry[] p1ClasspathEntries = new IClasspathEntry[]{JavaCore.newLibraryEntry(getExternalJCLPath(), null, null, true)};
		setClasspath(p1, p1ClasspathEntries);
		IJavaProject  p2 = this.createJavaProject("P2", new String[] {""}, "");
		IClasspathEntry[] p2ClasspathEntries = new IClasspathEntry[]{
				JavaCore.newProjectEntry(new Path("/P1")),
				JavaCore.newLibraryEntry(getExternalJCLPath(), null, null, false)
		};
		setClasspath(p2, p2ClasspathEntries);

		IClasspathEntry[] classpath = ((JavaProject)p2).getExpandedClasspath();
		assertEquals("Unexpected number of classpath entries", 2, classpath.length);
		assertEquals("Unexpected first entry", "/P1", classpath[0].getPath().toString());
		assertEquals("Unexpected second entry", getExternalJCLPathString(), classpath[1].getPath().toOSString());
	} finally {
		deleteProjects(new String[] {"P1", "P2"});
	}
}
/**
 * Make sure null references don't make their way into ClasspathEntry's state
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=170197"
 */
public void testForceNullArgumentsToEmptySet() throws CoreException {
	IClasspathEntry e =	JavaCore.newContainerEntry(new Path("JRE_CONTAINER"), null, null, false);
	assertTrue("Access rule was null", e.getAccessRules() != null);
	assertTrue("Extra attributes was null", e.getExtraAttributes() != null);
	assertTrue("Inclusion pattern was null", e.getInclusionPatterns() != null);
	assertTrue("Exclusion pattern was null", e.getExclusionPatterns() != null);
}

/**
 * Make sure null references don't make their way into ClasspathEntry's state
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=170197"
 */
public void testForceNullArgumentsToEmptySet2() throws CoreException {
	IClasspathEntry e = JavaCore.newLibraryEntry(new Path("/P0/JUNK"), new Path("/P0/SBlah"), new Path("/P0"), null, null, false);
	assertTrue("Access rule was null", e.getAccessRules() != null);
	assertTrue("Extra attributes was null", e.getExtraAttributes() != null);
	assertTrue("Inclusion pattern was null", e.getInclusionPatterns() != null);
	assertTrue("Exclusion pattern was null", e.getExclusionPatterns() != null);
}

/**
 * Make sure null references don't make their way into ClasspathEntry's state
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=170197"
 */
public void testForceNullArgumentsToEmptySet3() throws CoreException {
	IClasspathEntry e = JavaCore.newProjectEntry(new Path("/P2"), null, false, null, false);
	assertTrue("Access rule was null", e.getAccessRules() != null);
	assertTrue("Extra attributes was null", e.getExtraAttributes() != null);
	assertTrue("Inclusion pattern was null", e.getInclusionPatterns() != null);
	assertTrue("Exclusion pattern was null", e.getExclusionPatterns() != null);
}

/**
 * Make sure null references don't make their way into ClasspathEntry's state
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=170197"
 */
public void testForceNullArgumentsToEmptySet4() throws CoreException {
	IClasspathEntry e = JavaCore.newSourceEntry(new Path("/P"), null, null, null, null);
	assertTrue("Access rule was null", e.getAccessRules() != null);
	assertTrue("Extra attributes was null", e.getExtraAttributes() != null);
	assertTrue("Inclusion pattern was null", e.getInclusionPatterns() != null);
	assertTrue("Exclusion pattern was null", e.getExclusionPatterns() != null);
}

/**
 * Make sure null references don't make their way into ClasspathEntry's state
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=170197"
 */
public void testForceNullArgumentsToEmptySet5() throws CoreException {
	IClasspathEntry e = JavaCore.newVariableEntry(new Path("JCL18_LIB"), new Path("JCL18_SRC"), null, null, null, false);
	assertTrue("Access rule was null", e.getAccessRules() != null);
	assertTrue("Extra attributes was null", e.getExtraAttributes() != null);
	assertTrue("Inclusion pattern was null", e.getInclusionPatterns() != null);
	assertTrue("Exclusion pattern was null", e.getExclusionPatterns() != null);
}
/**
 * Test adding jar files with absolute file system path using the following modes and ensure the comparison
 * with a relative path return true.
 * 1. Variable Entry
 * 2. User Library
 * 3. External Jar file
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=276373"
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=280497"
 */
public void testBug276373() throws Exception {
	File libDir = null;
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {}, "bin");
		IPath libPath = proj.getResource().getLocation().append("lib");
		JavaCore.setClasspathVariable("MyVar", libPath, null);
		libDir = new File(libPath.toPortableString());
		libDir.mkdirs();
		IClasspathEntry[] classpath = new IClasspathEntry[3];
		File libJar = new File(libDir, "variableLib.jar");
		libJar.createNewFile();
		classpath[0] = JavaCore.newVariableEntry(new Path("/MyVar/variableLib.jar"), null, null);

		libJar = new File(libDir, "externalLib.jar");
		libJar.createNewFile();
		classpath[1] = JavaCore.newLibraryEntry(new Path(libJar.getAbsolutePath()), null, null);

		libJar = new File(libDir, "userLib.jar");
		libJar.createNewFile();

		ClasspathContainerInitializer initializer= JavaCore.getClasspathContainerInitializer(JavaCore.USER_LIBRARY_CONTAINER_ID);
		String libraryName = "TestUserLibrary";
		IPath containerPath = new Path(JavaCore.USER_LIBRARY_CONTAINER_ID);
		UserLibraryClasspathContainer containerSuggestion = new UserLibraryClasspathContainer(libraryName);
		initializer.requestClasspathContainerUpdate(containerPath.append(libraryName), null, containerSuggestion);

		IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(JavaCore.PLUGIN_ID);
		String propertyName = JavaModelManager.CP_USERLIBRARY_PREFERENCES_PREFIX+"TestUserLibrary";
		StringBuilder propertyValue = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<userlibrary systemlibrary=\"false\" version=\"1\">\r\n<archive");
		propertyValue.append(" path=\"" + libJar.getAbsolutePath());
		propertyValue.append("\"/>\r\n</userlibrary>\r\n");
		preferences.put(propertyName, propertyValue.toString());
		preferences.flush();

		classpath[2] = JavaCore.newContainerEntry(containerSuggestion.getPath());
		proj.setRawClasspath(classpath, null);

		IResource resource = getWorkspaceRoot().getProject("P").getFile("lib/variableLib.jar");
		assertTrue(proj.isOnClasspath(resource));
		IJavaElement element = proj.getPackageFragmentRoot(resource);
		assertTrue(proj.isOnClasspath(element));
		assertNotNull(((JavaProject)proj).getClasspathEntryFor(resource.getFullPath()));

		resource = getWorkspaceRoot().getProject("P").getFile("lib/externalLib.jar");
		assertTrue(proj.isOnClasspath(resource));
		element = proj.getPackageFragmentRoot(resource);
		assertTrue(proj.isOnClasspath(element));
		assertNotNull(((JavaProject)proj).getClasspathEntryFor(resource.getFullPath()));

		resource = getWorkspaceRoot().getProject("P").getFile("lib/userLib.jar");
		assertTrue(proj.isOnClasspath(resource));
		element = proj.getPackageFragmentRoot(resource);
		assertTrue(proj.isOnClasspath(element));
		assertNotNull(((JavaProject)proj).getClasspathEntryFor(resource.getFullPath()));
	}
	finally {
		if (libDir != null) {
			org.eclipse.jdt.core.tests.util.Util.delete(libDir);
		}
		this.deleteProject("P");
		JavaCore.removeClasspathVariable("MyVar", null);
	}
}
/**
 * bug 248661:Axis2: Missing required libraries in Axis 2 WS Client Projects
 * test that a variable classpath entry that is mappped to a folder/jar with external path (but still
 * inside the project folder) can be validated using both system absolute path and relative path.
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=248661"
 */
public void testBug248661() throws Exception {
	IFolder folder = null;
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {}, "bin");
		IPath folderPath = proj.getResource().getFullPath().append("classes");
		folder = createFolder(folderPath);
		assertNotNull(folder);
		IClasspathEntry[] classpath = new IClasspathEntry[1];
		classpath[0] = JavaCore.newVariableEntry(folderPath, null, null);
		setClasspath(proj, classpath);

		IClasspathEntry cpe = JavaCore.newLibraryEntry(folder.getFullPath(), null, null);
		IJavaModelStatus status = JavaConventions.validateClasspathEntry(proj, cpe, true);
		assertEquals(IStatus.OK, status.getCode());

		cpe = JavaCore.newLibraryEntry(folder.getLocation(), null, null);
		status = JavaConventions.validateClasspathEntry(proj, cpe, true);
		assertEquals(IStatus.OK, status.getCode());
	}
	finally{
		if (folder != null) {
			org.eclipse.jdt.core.tests.util.Util.delete(folder);
		}
		this.deleteProject("P");
	}
}
/**
 * bug 300136:classpathentry OPTIONAL attribute not honored for var entries
 *
 * Test that classpath entries (CPE_LIB, CPE_CONTAINER and CPE_VARIABLE) that are marked as optional
 * in the .classpath file are not reported for errors.
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=300136"
 */
public void testBug300136() throws Exception {
	boolean autoBuild = getWorkspace().isAutoBuilding();
	IWorkspaceDescription preferences = getWorkspace().getDescription();
	try {
		preferences.setAutoBuilding(false);
		IJavaProject project = createJavaProject("P");
		JavaCore.setClasspathVariables(
				new String[] {"INVALID_LIB",},
				new IPath[] {new Path("/lib/tmp.jar")},
				null);

		StringBuilder buffer = new StringBuilder(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<classpath>\n" +
				"   <classpathentry  kind=\"var\" path=\"INVALID_LIB\">\n" +
				"    	<attributes>\n" +
				"   	 <attribute name=\"optional\" value=\"true\"/>" +
				"    	</attributes>\n" +
				"	</classpathentry>\n" +
				"   <classpathentry  kind=\"var\" path=\"UNBOUND_VAR\">\n" +
				"    	<attributes>\n" +
				"   	 <attribute name=\"optional\" value=\"true\"/>" +
				"    	</attributes>\n" +
				"	</classpathentry>\n" +
				"   <classpathentry kind=\"con\" path=\"org.eclipse.jdt.core.tests.model.TEST_CONTAINER\">\n" +
				"    	<attributes>\n" +
				"   	 <attribute name=\"optional\" value=\"true\"/>" +
				"    	</attributes>\n" +
				"	</classpathentry>\n" +
				"   <classpathentry kind=\"output\" path=\"bin\"/>\n" +
				"</classpath>"
				);
		editFile(
			"/P/.classpath",
			buffer.toString()
		);
		assertBuildPathMarkers(
				"Unexpected markers",
				"",
				project);
	} finally {
		try {
			deleteProject("P");
			JavaCore.removeClasspathVariable("INVALID_LIB", null);
		} finally {
			restoreAutobuild(preferences, autoBuild);
		}
	}
}
/**
 * Additional test for bug 300136 - Test that the errors are reported when the
 * optional attribute is not used.
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=300136"
 */
public void testBug300136a() throws Exception {
	boolean autoBuild = getWorkspace().isAutoBuilding();
	IWorkspaceDescription preferences = getWorkspace().getDescription();
	try {
		preferences.setAutoBuilding(false);
		IJavaProject project = createJavaProject("P");
		IPath libPath = new Path("/lib/tmp.jar");
		JavaCore.setClasspathVariables(
				new String[] {"INVALID_LIB",},
				new IPath[] {libPath},
				null);

		StringBuilder buffer = new StringBuilder(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<classpath>\n" +
				"    <classpathentry  kind=\"var\" path=\"INVALID_LIB\" />\n" +
				"    <classpathentry  kind=\"var\" path=\"UNBOUND_VAR\" />\n" +
				"    <classpathentry kind=\"con\" path=\"org.eclipse.jdt.core.tests.model.TEST_CONTAINER\">\n" +
				"	</classpathentry>\n" +
				"    <classpathentry kind=\"output\" path=\"bin\"/>\n" +
				"</classpath>"
				);
		editFile(
			"/P/.classpath",
			buffer.toString()
		);
		assertBuildPathMarkers(
				"Unexpected markers",
				"Project \'P\' is missing required library: \'" + libPath.toOSString() + "'\n" +
				"Unbound classpath container: \'org.eclipse.jdt.core.tests.model.TEST_CONTAINER\' in project \'P\'\n" +
				"Unbound classpath variable: \'UNBOUND_VAR\' in project \'P\'",
				project);
	} finally {
		try {
			deleteProject("P");
			JavaCore.removeClasspathVariable("INVALID_LIB", null);
		} finally {
			restoreAutobuild(preferences, autoBuild);
		}
	}
}
/**
 * bug 294360:Duplicate entries in Classpath Resolution when importing dependencies from parent project
 * Test that duplicate entries are not added to the resolved classpath
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=294360"
 */
public void testBug294360a() throws Exception {
	try {
		IJavaProject p = createJavaProject("P");
		addExternalLibrary(p, getExternalResourcePath("lib.jar"), new String[0],
				new String[] {
					"META-INF/MANIFEST.MF",
					"Manifest-Version: 1.0\n",
				},
				CompilerOptions.getFirstSupportedJavaVersion());
		IClasspathEntry[] classpath = new IClasspathEntry[2];
		classpath[0] = JavaCore.newLibraryEntry(new Path(getExternalResourcePath("lib.jar")), null, null);
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P", getExternalResourcePath("lib.jar")}));
		classpath[1] = JavaCore.newContainerEntry(new Path("org.eclipse.jdt.core.tests.model.TEST_CONTAINER"));
		setClasspath(p, classpath);

		StringBuilder buffer = new StringBuilder(
						"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
						"<classpath>\n" +
						"	<classpathentry kind=\"con\" path=\"org.eclipse.jdt.core.tests.model.TEST_CONTAINER\"/>\n" +
						"	<classpathentry kind=\"lib\" path=\""+ getExternalResourcePath("lib.jar") + "\">\n" +
						"    	<attributes>\n" +
						"   	 <attribute name=\"optional\" value=\"true\"/>\n" +
						"    	</attributes>\n" +
						"	</classpathentry>\n" +
						"	<classpathentry kind=\"output\" path=\"\"/>\n" +
						"</classpath>\n");

		editFile(
			"/P/.classpath",
			buffer.toString()
		);

		IClasspathEntry[] resolvedClasspath = p.getResolvedClasspath(true);
		assertClasspathEquals(resolvedClasspath,
				""+ getExternalPath() + "lib.jar[CPE_LIBRARY][K_BINARY][isExported:false]");
	} finally {
		deleteProject("P");
		deleteExternalResource("lib.jar");
	}
}
/**
 * bug 252431:New API is needed to better identify referenced jars in the Class-Path: entry
 * Test that 1) referenced libraries are added to the resolved classpath in the right order
 * 			 2) referenced libraries are added to the appropriate referencing library in the correct order
 * 			 3) referenced libraries and top-level libraries retain the source attachment and source attachment root path
 * 			 4) referenced libraries point to the correct entry as their referencingEntry.
 * 			 5) referenced libraries and their attributes are persisted in the .classpath file
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=252431"
 */
public void testBug252341a() throws Exception {
	try {
		IJavaProject p = createJavaProject("P");
		addLibrary(p, "lib1.jar", "abc.zip", new String[0],
			new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" +
				"Class-Path: lib2.jar lib3.jar\n",
			},
			CompilerOptions.getFirstSupportedJavaVersion());
		createFile("/P/lib2.jar", "");
		createFile("/P/lib3.jar", "");

		// Test referenced entries are included in the right order in the resolved classpath
		IClasspathEntry[] resolvedClasspath = p.getResolvedClasspath(true);
		assertClasspathEquals(resolvedClasspath,
				"/P[CPE_SOURCE][K_SOURCE][isExported:false]\n" +
				""+ getExternalJCLPathString() + "[CPE_LIBRARY][K_BINARY][isExported:false]\n" +
				"/P/lib2.jar[CPE_LIBRARY][K_BINARY][isExported:true]\n" +
				"/P/lib3.jar[CPE_LIBRARY][K_BINARY][isExported:true]\n" +
				"/P/lib1.jar[CPE_LIBRARY][K_BINARY][sourcePath:/P/abc.zip][isExported:true]");

		IClasspathEntry[] rawClasspath = p.getRawClasspath();
		assertClasspathEquals(rawClasspath,
				"/P[CPE_SOURCE][K_SOURCE][isExported:false]\n" +
				"JCL18_LIB[CPE_VARIABLE][K_SOURCE][isExported:false]\n" +
				"/P/lib1.jar[CPE_LIBRARY][K_BINARY][sourcePath:/P/abc.zip][isExported:true]");

		// Test referenced entries for a particular entry appear in the right order and the referencingEntry
		// attribute has the correct value
		IClasspathEntry[] chains = JavaCore.getReferencedClasspathEntries(rawClasspath[2], null);
		assertClasspathEquals(chains,
				"/P/lib2.jar[CPE_LIBRARY][K_BINARY][isExported:true]\n" +
				"/P/lib3.jar[CPE_LIBRARY][K_BINARY][isExported:true]");

		chains = JavaCore.getReferencedClasspathEntries(rawClasspath[2], p);
		assertClasspathEquals(chains,
				"/P/lib2.jar[CPE_LIBRARY][K_BINARY][isExported:true]\n" +
				"/P/lib3.jar[CPE_LIBRARY][K_BINARY][isExported:true]");

		assertSame("Referencing Entry", rawClasspath[2], chains[0].getReferencingEntry());
		assertSame("Referencing Entry", rawClasspath[2], chains[1].getReferencingEntry());

		// Test a newly created library entry with similar attributes but without any referencing entry is equal to
		// the original referenced entry
		IClasspathEntry tempLibEntry = JavaCore.newLibraryEntry(chains[0].getPath(), chains[0].getSourceAttachmentPath(), chains[0].getSourceAttachmentRootPath(), true);
		assertEquals("Library Entry", tempLibEntry, chains[0]);

		// Test the source attachment and other attributes added to the referenced entries are stored and retrieved properly
		assertEquals("source attachment", resolvedClasspath[4].getSourceAttachmentPath().toPortableString(), "/P/abc.zip");
		assertNull("source attachment", chains[0].getSourceAttachmentPath());
		assertNull("source attachment", chains[1].getSourceAttachmentPath());
		assertNull("source attachment root", chains[0].getSourceAttachmentRootPath());
		assertNull("source attachment root", chains[1].getSourceAttachmentRootPath());

		((ClasspathEntry)chains[0]).sourceAttachmentPath = new Path("/P/efg.zip");
		((ClasspathEntry)chains[1]).sourceAttachmentPath = new Path("/P/xyz.zip");
		((ClasspathEntry)chains[0]).sourceAttachmentRootPath = new Path("/src2");
		((ClasspathEntry)chains[1]).sourceAttachmentRootPath = new Path("/src3");

		IClasspathAttribute javadocLoc = JavaCore.newClasspathAttribute("javadoc_location", "/P/efg.zip");
		((ClasspathEntry)chains[0]).extraAttributes = new IClasspathAttribute[]{javadocLoc};

		p.setRawClasspath(rawClasspath, chains, p.getOutputLocation(), null);

		// Test the .classpath file contains all the referenced entries and their attributes
		String contents = new String (org.eclipse.jdt.internal.core.util.Util.getResourceContentsAsCharArray(getFile("/P/.classpath")));
		assertSourceEquals(
			"Unexpected content",
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<classpath>\n" +
			"	<classpathentry kind=\"src\" path=\"\"/>\n" +
			"	<classpathentry kind=\"var\" path=\"JCL18_LIB\"/>\n" +
			"	<classpathentry exported=\"true\" kind=\"lib\" path=\"lib1.jar\" sourcepath=\"abc.zip\"/>\n" +
			"	<classpathentry kind=\"output\" path=\"\"/>\n" +
			"	<referencedentry exported=\"true\" kind=\"lib\" path=\"lib2.jar\" rootpath=\"/src2\" sourcepath=\"efg.zip\">\n" +
			"		<attributes>\n" +
			"			<attribute name=\"javadoc_location\" value=\"/P/efg.zip\"/>\n" +
			"		</attributes>\n" +
			"	</referencedentry>\n" +
			"	<referencedentry exported=\"true\" kind=\"lib\" path=\"lib3.jar\" rootpath=\"/src3\" sourcepath=\"xyz.zip\"/>\n" +
			"</classpath>\n",
			contents);

		p.close();
		p.open(null);
		rawClasspath = p.getRawClasspath();
		resolvedClasspath = p.getResolvedClasspath(true);

		assertClasspathEquals(rawClasspath,
				"/P[CPE_SOURCE][K_SOURCE][isExported:false]\n" +
				"JCL18_LIB[CPE_VARIABLE][K_SOURCE][isExported:false]\n" +
				"/P/lib1.jar[CPE_LIBRARY][K_BINARY][sourcePath:/P/abc.zip][isExported:true]");

		assertClasspathEquals(resolvedClasspath,
				"/P[CPE_SOURCE][K_SOURCE][isExported:false]\n" +
				""+ getExternalJCLPathString() + "[CPE_LIBRARY][K_BINARY][isExported:false]\n" +
				"/P/lib2.jar[CPE_LIBRARY][K_BINARY][sourcePath:/P/efg.zip][rootPath:/src2][isExported:true][attributes:javadoc_location=/P/efg.zip]\n" +
				"/P/lib3.jar[CPE_LIBRARY][K_BINARY][sourcePath:/P/xyz.zip][rootPath:/src3][isExported:true]\n" +
				"/P/lib1.jar[CPE_LIBRARY][K_BINARY][sourcePath:/P/abc.zip][isExported:true]");

	} finally {
		deleteProject("P");
	}
}
/**
 * Additional tests for bug 252431.
 * When multiple libraries have one or more common referenced library in their MANIFEST
 * 1) The common referenced libries are added to the first library entry in the raw classpath
 * 2) Removing one of the top-level library from the raw classpath doesn't remove the referenced
 *    entry that was commonly referenced by another entry and the referenced entry's source
 *    attachment and other attributes are retained.
 * 3) Passing a NULL referencedEntries array retains the referenced entries
 * 4) Passing an empty array as referencedEntries clears the earlier referenced entries
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=252431"
 */
public void testBug252341b() throws Exception {
	try {
		IJavaProject p = createJavaProject("P");
		addLibrary(p, "lib1.jar", null, new String[0],
			new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" +
				"Class-Path: lib3.jar lib4.jar\n",
			},
			CompilerOptions.getFirstSupportedJavaVersion());

		addLibrary(p, "lib2.jar", null, new String[0],
				new String[] {
					"META-INF/MANIFEST.MF",
					"Manifest-Version: 1.0\n" +
					"Class-Path: lib3.jar lib5.jar\n",
				},
				CompilerOptions.getFirstSupportedJavaVersion());
		createFile("/P/lib3.jar", "");
		createFile("/P/lib4.jar", "");
		createFile("/P/lib5.jar", "");

		// Test that the referenced entries are not included in the raw classpath
		IClasspathEntry[] rawClasspath = p.getRawClasspath();
		assertClasspathEquals(
				rawClasspath,
				"/P[CPE_SOURCE][K_SOURCE][isExported:false]\n" +
				"JCL18_LIB[CPE_VARIABLE][K_SOURCE][isExported:false]\n" +
				"/P/lib1.jar[CPE_LIBRARY][K_BINARY][isExported:true]\n" +
				"/P/lib2.jar[CPE_LIBRARY][K_BINARY][isExported:true]");

		IClasspathEntry[] rawEntries = new IClasspathEntry[2];
		rawEntries[0] = JavaCore.newLibraryEntry(new Path("/P/lib1.jar"), null, null, true);
		rawEntries[1] = JavaCore.newLibraryEntry(new Path("/P/lib2.jar"), null, null, true);

		// Test that the referenced entries are included in the raw classpath and in the right order
		IClasspathEntry[] resolvedClasspath = p.getResolvedClasspath(true);
		assertClasspathEquals(resolvedClasspath,
				"/P[CPE_SOURCE][K_SOURCE][isExported:false]\n" +
				""+ getExternalJCLPathString() + "[CPE_LIBRARY][K_BINARY][isExported:false]\n" +
				"/P/lib3.jar[CPE_LIBRARY][K_BINARY][isExported:true]\n" +
				"/P/lib4.jar[CPE_LIBRARY][K_BINARY][isExported:true]\n" +
				"/P/lib1.jar[CPE_LIBRARY][K_BINARY][isExported:true]\n" +
				"/P/lib5.jar[CPE_LIBRARY][K_BINARY][isExported:true]\n" +
				"/P/lib2.jar[CPE_LIBRARY][K_BINARY][isExported:true]");

		// Test that the referenced classpath entries has the appropriate referencingEntry value
		IClasspathEntry[] chains = JavaCore.getReferencedClasspathEntries(rawEntries[0], p);
		assertClasspathEquals(
				chains,
				"/P/lib3.jar[CPE_LIBRARY][K_BINARY][isExported:true]\n" +
				"/P/lib4.jar[CPE_LIBRARY][K_BINARY][isExported:true]");
		assertEquals("Referencing Entry" , rawEntries[0], chains[0].getReferencingEntry());
		assertEquals("Referencing Entry" , rawEntries[0], chains[1].getReferencingEntry());

		chains = JavaCore.getReferencedClasspathEntries(rawEntries[1], p);
		assertClasspathEquals(
				chains,
				"/P/lib3.jar[CPE_LIBRARY][K_BINARY][isExported:true]\n" +
				"/P/lib5.jar[CPE_LIBRARY][K_BINARY][isExported:true]");

		assertEquals("Referencing Entry" , rawEntries[0], chains[0].getReferencingEntry());
		assertEquals("Referencing Entry" , rawEntries[1], chains[1].getReferencingEntry());

//		// Test IPackageFragmentRoot#getResolvedClasspathEntry
		IPackageFragmentRoot[] roots = p.getPackageFragmentRoots();
		assertEquals("Package fragment root", roots[2].getResolvedClasspathEntry(), resolvedClasspath[2]);
		assertEquals("Package fragment root", roots[3].getResolvedClasspathEntry(), resolvedClasspath[3]);
		assertEquals("Package fragment root", roots[4].getResolvedClasspathEntry(), resolvedClasspath[4]);
		assertEquals("Package fragment root", roots[5].getResolvedClasspathEntry(), resolvedClasspath[5]);
		assertEquals("Package fragment root", roots[6].getResolvedClasspathEntry(), resolvedClasspath[6]);

		// Test the attributes added to the referenced classpath entries are stored and retrieved properly
		((ClasspathEntry)chains[0]).sourceAttachmentPath = new Path("/P/efg.zip");
		((ClasspathEntry)chains[1]).sourceAttachmentPath = new Path("/P/xyz.zip");
		((ClasspathEntry)chains[0]).sourceAttachmentRootPath = new Path("/src2");
		((ClasspathEntry)chains[1]).sourceAttachmentRootPath = new Path("/src3");

		IClasspathAttribute javadocLoc = JavaCore.newClasspathAttribute("javadoc_location", "/P/efg.zip");
		((ClasspathEntry)chains[0]).extraAttributes = new IClasspathAttribute[]{javadocLoc};

		p.setRawClasspath(rawClasspath, chains, p.getOutputLocation(), null);
		resolvedClasspath = p.getResolvedClasspath(true);

		assertClasspathEquals(resolvedClasspath,
				"/P[CPE_SOURCE][K_SOURCE][isExported:false]\n" +
				""+ getExternalJCLPathString() + "[CPE_LIBRARY][K_BINARY][isExported:false]\n" +
				"/P/lib3.jar[CPE_LIBRARY][K_BINARY][sourcePath:/P/efg.zip][rootPath:/src2][isExported:true][attributes:javadoc_location=/P/efg.zip]\n" +
				"/P/lib4.jar[CPE_LIBRARY][K_BINARY][isExported:true]\n" +
				"/P/lib1.jar[CPE_LIBRARY][K_BINARY][isExported:true]\n" +
				"/P/lib5.jar[CPE_LIBRARY][K_BINARY][sourcePath:/P/xyz.zip][rootPath:/src3][isExported:true]\n" +
				"/P/lib2.jar[CPE_LIBRARY][K_BINARY][isExported:true]");

		// Test that removing any of the referencing entry from the raw classpath has the correct effect
		// on the resolved classpath. Also test passing referencedEntries = null retains the existing
		// referenced entries
		IClasspathEntry[] newRawClasspath = new IClasspathEntry[rawClasspath.length-1];
		System.arraycopy(rawClasspath, 0, newRawClasspath, 0, 2);
		System.arraycopy(rawClasspath, 3, newRawClasspath, 2, 1);
		p.setRawClasspath(newRawClasspath, null, p.getOutputLocation(), null);
		resolvedClasspath = p.getResolvedClasspath(true);
		assertClasspathEquals(resolvedClasspath,
				"/P[CPE_SOURCE][K_SOURCE][isExported:false]\n" +
				""+ getExternalJCLPathString() + "[CPE_LIBRARY][K_BINARY][isExported:false]\n" +
				"/P/lib3.jar[CPE_LIBRARY][K_BINARY][sourcePath:/P/efg.zip][rootPath:/src2][isExported:true][attributes:javadoc_location=/P/efg.zip]\n" +
				"/P/lib5.jar[CPE_LIBRARY][K_BINARY][sourcePath:/P/xyz.zip][rootPath:/src3][isExported:true]\n" +
				"/P/lib2.jar[CPE_LIBRARY][K_BINARY][isExported:true]");

		// Test that passing empty array of referencedEntries clears all the earlier ones.
		p.setRawClasspath(newRawClasspath, new IClasspathEntry[]{}, p.getOutputLocation(), null);
		resolvedClasspath = p.getResolvedClasspath(true);
		assertClasspathEquals(resolvedClasspath,
				"/P[CPE_SOURCE][K_SOURCE][isExported:false]\n" +
				""+ getExternalJCLPathString() + "[CPE_LIBRARY][K_BINARY][isExported:false]\n" +
				"/P/lib3.jar[CPE_LIBRARY][K_BINARY][isExported:true]\n" +
				"/P/lib5.jar[CPE_LIBRARY][K_BINARY][isExported:true]\n" +
				"/P/lib2.jar[CPE_LIBRARY][K_BINARY][isExported:true]");

	} finally {
		deleteProject("P");
	}
}
/**
 * Additional tests for bug 252431.
 * Test that duplicate referenced entries or entries that are already present in the raw classpath
 * are excluded from the referenced entries when invoking
 * {@link IJavaProject#setRawClasspath(IClasspathEntry[], IClasspathEntry[], IPath, IProgressMonitor)}
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=252431"
 */
public void testBug252341c() throws Exception {
	try {
		IJavaProject p = createJavaProject("P");
		addLibrary(p, "lib1.jar", null, new String[0],
			new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" +
				"Class-Path: lib3.jar lib4.jar\n",
			},
			CompilerOptions.getFirstSupportedJavaVersion());

		addLibrary(p, "lib2.jar", null, new String[0],
				new String[] {
					"META-INF/MANIFEST.MF",
					"Manifest-Version: 1.0\n" +
					"Class-Path: lib3.jar lib5.jar\n",
				},
				CompilerOptions.getFirstSupportedJavaVersion());
		createFile("/P/lib3.jar", "");
		createFile("/P/lib4.jar", "");
		createFile("/P/lib5.jar", "");

		IClasspathEntry[] rawClasspath = p.getRawClasspath();

		IClasspathEntry[] rawEntries = new IClasspathEntry[2];
		rawEntries[0] = JavaCore.newLibraryEntry(new Path("/P/lib1.jar"), null, null, true);
		rawEntries[1] = JavaCore.newLibraryEntry(new Path("/P/lib2.jar"), null, null, true);

		// Test that the referenced classpath entries has the appropriate referencingEntry value
		IClasspathEntry[] chains = JavaCore.getReferencedClasspathEntries(rawEntries[0], p);

		IClasspathEntry[] referencedEntries = new IClasspathEntry[5];
		referencedEntries[0] = chains[0];
		referencedEntries[1] = chains[1];

		chains = JavaCore.getReferencedClasspathEntries(rawEntries[1], p);

		referencedEntries[2] = chains[0];
		referencedEntries[3] = chains[1];
		referencedEntries[4] = chains[1];

		p.setRawClasspath(rawClasspath, referencedEntries, p.getOutputLocation(), null);

		p.close();
		p.open(null);

		IClasspathEntry[] storedReferencedEnties = p.getReferencedClasspathEntries();
		assertClasspathEquals(storedReferencedEnties,
				"/P/lib3.jar[CPE_LIBRARY][K_BINARY][isExported:true]\n" +
				"/P/lib4.jar[CPE_LIBRARY][K_BINARY][isExported:true]\n" +
				"/P/lib5.jar[CPE_LIBRARY][K_BINARY][isExported:true]");
	}
	finally {
		deleteProject("P");
	}
}
/**
 * bug 304081:IJavaProject#isOnClasspath(IJavaElement) returns false for type from referenced JAR
 * When the JAR, which a variable classpath entry resolves to, references other JAR via
 * MANIFEST, test that {@link IJavaProject#isOnClasspath(IJavaElement)} returns true
 * for the referenced classpath entries.
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=304081"
 */
public void testBug304081() throws Exception {
	File libDir = null;
	try {

		IJavaProject proj = this.createJavaProject("P", new String[] {}, "bin");
		IPath libPath = proj.getResource().getLocation();
		JavaCore.setClasspathVariable("MyVar", libPath, null);
		libDir = new File(libPath.toPortableString());
		IClasspathEntry[] classpath = new IClasspathEntry[1];
		File libJar = new File(libDir, "variable.jar");
		libJar.createNewFile();

		addLibrary(proj, "variable.jar", null, new String[0],
				new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" +
				"Class-Path: lib1.jar\n",
			},
			CompilerOptions.getFirstSupportedJavaVersion());

		createFile("/P/lib1.jar", "");

		classpath[0] = JavaCore.newVariableEntry(new Path(
				"/MyVar/variable.jar"), null, null);

		proj.setRawClasspath(classpath, null);
		waitForAutoBuild();
		IProject project = getWorkspaceRoot().getProject("P");
		IResource resource = project.getFile("variable.jar");
		assertTrue(proj.isOnClasspath(resource));
		IJavaElement element = proj.getPackageFragmentRoot(resource);
		assertTrue(proj.isOnClasspath(element));

		resource = project.getFile("lib1.jar");
		assertTrue(proj.isOnClasspath(resource));
		element = proj.getPackageFragmentRoot(resource);
		assertTrue(proj.isOnClasspath(element));
	} finally {
		this.deleteProject("P");
		JavaCore.removeClasspathVariable("MyVar", null);
	}
}
/**
 * Additional tests for 304081
 * When the JAR, which is in the raw classpath, references other JAR via
 * MANIFEST, test that {@link IJavaProject#isOnClasspath(IJavaElement)} returns true
 * for the referenced classpath entries.
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=304081"
 */
public void testBug304081a() throws Exception {
	try {

		IJavaProject proj = this.createJavaProject("P", new String[] {}, "bin");
		IClasspathEntry[] classpath = new IClasspathEntry[1];

		addLibrary(proj, "library.jar", null, new String[0],
				new String[] {
					"META-INF/MANIFEST.MF",
					"Manifest-Version: 1.0\n" +
					"Class-Path: lib1.jar\n",
				},
				CompilerOptions.getFirstSupportedJavaVersion());
		createFile("/P/lib1.jar", "");
		classpath[0] = JavaCore.newLibraryEntry(new Path("/P/library.jar"), null, null);

		proj.setRawClasspath(classpath, null);
		waitForAutoBuild();
		IProject project = getWorkspaceRoot().getProject("P");
		IResource resource = project.getFile("library.jar");
		assertTrue(proj.isOnClasspath(resource));
		IJavaElement element = proj.getPackageFragmentRoot(resource);
		assertTrue(proj.isOnClasspath(element));

		resource = project.getFile("lib1.jar");
		assertTrue(proj.isOnClasspath(resource));
		element = proj.getPackageFragmentRoot(resource);
		assertTrue(proj.isOnClasspath(element));
	} finally {
		this.deleteProject("P");
	}
}
/**
 * bug 302949: FUP of 302949
 * Test that
 * 1) non-chaining jars are cached during classpath resolution and can be retrieved later on
 * 2) A full save (after resetting the non chaining jars cache) caches the non-chaining jars information
 * 3) when a project is deleted, the non-chaining jar cache is reset.
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=305122"
 */
public void testBug305122() throws Exception {
	try {

		IJavaProject proj = this.createJavaProject("P", new String[] {}, "bin");
		IClasspathEntry[] classpath = new IClasspathEntry[2];

		addLibrary(proj, "nonchaining.jar", null, new String[0],
				new String[] {
					"META-INF/MANIFEST.MF",
					"Manifest-Version: 1.0\n",
				},
				CompilerOptions.getFirstSupportedJavaVersion());
		addLibrary(proj, "chaining.jar", null, new String[0],
				new String[] {
					"META-INF/MANIFEST.MF",
					"Manifest-Version: 1.0\n" +
					"Class-Path: chained.jar\n",
				},
				CompilerOptions.getFirstSupportedJavaVersion());

		classpath[0] = JavaCore.newLibraryEntry(new Path("/P/nonchaining.jar"), null, null);
		classpath[1] = JavaCore.newLibraryEntry(new Path("/P/chaining.jar"), null, null);
		createFile("/P/chained.jar", "");

		proj.setRawClasspath(classpath, null);
		waitForAutoBuild();
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		assertTrue("Non chaining Jar", manager.isNonChainingJar(classpath[0].getPath()));
		assertFalse("Chaining Jar", manager.isNonChainingJar(classpath[1].getPath()));

		((JavaProject)proj).resetResolvedClasspath();
		proj.getResolvedClasspath(true);
		manager = JavaModelManager.getJavaModelManager();
		assertTrue("Non chaining Jar", manager.isNonChainingJar(classpath[0].getPath()));
		assertFalse("Chaining Jar", manager.isNonChainingJar(classpath[1].getPath()));

		((JavaProject)proj).resetResolvedClasspath();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.save(true, null);
		assertTrue("Non chaining Jar", manager.isNonChainingJar(classpath[0].getPath()));
		assertFalse("Chaining Jar", manager.isNonChainingJar(classpath[1].getPath()));

		this.deleteProject("P");
		assertFalse("Chaining Jar", manager.isNonChainingJar(classpath[0].getPath()));
		assertFalse("Chaining Jar", manager.isNonChainingJar(classpath[1].getPath()));

	} finally {
		IProject proj = this.getProject("P");
		if ( proj != null && proj.exists())
			this.deleteProject("P");
	}
}
/**
 * bug 308150: JAR with invalid Class-Path entry in MANIFEST.MF crashes the project
 * Test that an invalid referenced library entry in the Class-Path of the MANIFEST doesn't
 * create any exceptions and is NOT added to the resolved classpath.
 *
 *  @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=308150"
 */
public void testBug308150() throws Exception {
	try {

		IJavaProject proj = this.createJavaProject("P", new String[] {}, "bin");
		IClasspathEntry[] classpath = new IClasspathEntry[1];

		// The Class-Path references an entry that points to the workspace root (hence invalid)
		addLibrary(proj, "invalid.jar", null, new String[0],
				new String[] {
					"META-INF/MANIFEST.MF",
					"Manifest-Version: 1.0\n" +
					"Class-Path: ../..\n",
				},
				CompilerOptions.getFirstSupportedJavaVersion());

		classpath[0] = JavaCore.newLibraryEntry(new Path("/P/invalid.jar"), null, null);
		proj.setRawClasspath(classpath, null);
		waitForAutoBuild();
		IClasspathEntry[] resolvedClasspath = proj.getResolvedClasspath(true);

		assertClasspathEquals(resolvedClasspath,
				"/P/invalid.jar[CPE_LIBRARY][K_BINARY][isExported:false]");
	} finally {
		this.deleteProject("P");
	}
}
/**
 * bug 305037: missing story for attributes of referenced JARs in classpath containers
 * Test that attributes set on referenced libraries of variable entries via MANIFEST are persisted
 * and can be retrieved
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=305037"
 */
public void testBug305037() throws Exception {
	File libDir = null;
	try {

		IJavaProject proj = this.createJavaProject("P", new String[] {}, "bin");
		IPath libPath = proj.getResource().getLocation();
		JavaCore.setClasspathVariable("MyVar", libPath, null);
		libDir = new File(libPath.toPortableString());
		IClasspathEntry[] classpath = new IClasspathEntry[1];
		File libJar = new File(libDir, "variable.jar");
		libJar.createNewFile();

		addLibrary(proj, "variable.jar", null, new String[0],
				new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" +
				"Class-Path: lib1.jar\n",
			},
			CompilerOptions.getFirstSupportedJavaVersion());

		createFile("/P/lib1.jar", "");

		classpath = proj.getResolvedClasspath(true);
		assertClasspathEquals(classpath,
				"/P/lib1.jar[CPE_LIBRARY][K_BINARY][isExported:true]\n" +
				"/P/variable.jar[CPE_LIBRARY][K_BINARY][isExported:true]");

		IClasspathEntry[] chains = JavaCore.getReferencedClasspathEntries(classpath[1], null);
		assertClasspathEquals(chains, "/P/lib1.jar[CPE_LIBRARY][K_BINARY][isExported:true]");
		((ClasspathEntry)chains[0]).sourceAttachmentPath = new Path("/P/efg.zip");
		((ClasspathEntry)chains[0]).sourceAttachmentRootPath = new Path("/src2");

		IClasspathAttribute javadocLoc = JavaCore.newClasspathAttribute("javadoc_location", "/P/efg.zip");
		((ClasspathEntry)chains[0]).extraAttributes = new IClasspathAttribute[]{javadocLoc};

		proj.setRawClasspath(proj.getRawClasspath(), chains, proj.getOutputLocation(), null);
		classpath = proj.getResolvedClasspath(true);
		assertClasspathEquals(classpath,
				"/P/lib1.jar[CPE_LIBRARY][K_BINARY][sourcePath:/P/efg.zip][rootPath:/src2][isExported:true][attributes:javadoc_location=/P/efg.zip]\n" +
				"/P/variable.jar[CPE_LIBRARY][K_BINARY][isExported:true]");
	} finally {
		this.deleteProject("P");
		JavaCore.removeClasspathVariable("MyVar", null);
	}
}
/**
 * bug 313965: Breaking change in classpath container API
 * Test that when the resolveReferencedLibrariesForContainers system property is set to true, the referenced libraries
 * for JARs from containers are resolved.
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=313965"
 */
public void testBug313965() throws Exception {
	try {
		simulateExit();
		// Use the literal attribute and not constant. Attribute once documented is not supposed to change.
		try {
			System.setProperty("resolveReferencedLibrariesForContainers", "true");
		} finally {
			simulateRestart();
		}
		IJavaProject p = this.createJavaProject("P", new String[] {}, "bin");
		addLibrary(p, "lib.jar", null, new String[0],
				new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" +
				"Class-Path: lib1.jar\n",
			},
			CompilerOptions.getFirstSupportedJavaVersion());
		IClasspathEntry[] classpath = new IClasspathEntry[1];
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P", "/P/lib.jar"}));
		classpath[0] = JavaCore.newContainerEntry(new Path("org.eclipse.jdt.core.tests.model.TEST_CONTAINER"));
		setClasspath(p, classpath);
		createFile("/P/lib1.jar", "");

		IClasspathEntry[] resolvedClasspath = p.getResolvedClasspath(true);
		assertClasspathEquals(resolvedClasspath,
				"/P/lib1.jar[CPE_LIBRARY][K_BINARY][isExported:false]\n" +
				"/P/lib.jar[CPE_LIBRARY][K_BINARY][isExported:false]");
	} finally {
		deleteProject("P");
		ContainerInitializer.setInitializer(null);
		simulateExit();
		System.setProperty("resolveReferencedLibrariesForContainers", "");
		simulateRestart();
	}
}
/**
 * bug 313965: Breaking change in classpath container API
 * Test that when the resolveReferencedLibrariesForContainers system property is set to false (or default),
 * the referenced libraries for JARs from containers are NOT resolved or added to the project's classpath.
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=313965"
 */
public void testBug313965a() throws Exception {
	try {
		// Do not set the resolveReferencedLibrariesForContainers system property (the default value is false)
		IJavaProject p = this.createJavaProject("P", new String[] {}, "bin");
		addLibrary(p, "lib.jar", null, new String[0],
				new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" +
				"Class-Path: lib1.jar\n",
			},
			CompilerOptions.getFirstSupportedJavaVersion());
		IClasspathEntry[] classpath = new IClasspathEntry[1];
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {"P", "/P/lib.jar"}));
		classpath[0] = JavaCore.newContainerEntry(new Path("org.eclipse.jdt.core.tests.model.TEST_CONTAINER"));
		setClasspath(p, classpath);

		createFile("/P/lib1.jar", "");

		IClasspathEntry[] resolvedClasspath = p.getResolvedClasspath(true);
		assertClasspathEquals(resolvedClasspath,
				"/P/lib.jar[CPE_LIBRARY][K_BINARY][isExported:false]");
	} finally {
		deleteProject("P");
		ContainerInitializer.setInitializer(null);
	}
}
public void testBug321170() throws Exception {
	try {
		IJavaProject p = this.createJavaProject("P", new String[] {}, "bin");

		IFile file = this.createFile("/P/bin/X.java", "public class X {}");

		IClasspathEntry[] classpath = new IClasspathEntry[1];
		classpath[0] = JavaCore.newLibraryEntry(new Path("/P/bin/X.java"), null, null);
		setClasspath(p, classpath);
		ICompilationUnit element = (ICompilationUnit)JavaCore.create(file, p);
		assertNotNull("File created", element);
	}
	catch(ClassCastException e){
		fail("classcast exception");
	}
	finally {
		deleteProject("P");
		ContainerInitializer.setInitializer(null);
	}
}
/**
 * bug 229042: [buildpath] could create build path error in case of invalid external JAR format
 *
 * Test that an invalid archive (JAR) creates a buildpath error
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=229042"
 */
public void testBug229042() throws Exception {
	try {
		IJavaProject p = createJavaProject("P");
		createFile("/P/library.jar", "");
		setClasspath(p, new IClasspathEntry[] { JavaCore.newLibraryEntry(new Path("/P/library.jar"), null,null)});
		assertBuildPathMarkers("Expected marker",
				"Archive for required library: \'library.jar\' in project \'P\' cannot be read or is not a valid ZIP file", p);
		setClasspath(p, new IClasspathEntry[0]);
		addLibrary(p, "library.jar", null, new String[0],
				new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {}\n",
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n",
			},
			CompilerOptions.getFirstSupportedJavaVersion());
		IFile file = p.getProject().getFile("library.jar");
		assertNotNull(file);
		file.touch(null);
		waitForAutoBuild();
		assertBuildPathMarkers("Unexpected marker",
				"", p);

	} finally {
		deleteProject("P");
	}
}
/**
 * bug 274737: Relative Classpath entries should not be resolved relative to the workspace
 *
 * Test that for an external project, relative paths are resolve relative to the project location.
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=274737"
 */
public void testBug274737() throws Exception {
	try {
		createExternalFolder("development/third_party");
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("ExternalProject");
		URI uri=  URIUtil.toURI(new Path(getExternalResourcePath("development")).append("ExternalProject"));
		IProjectDescription desc = project.getWorkspace().newProjectDescription(project.getName());
		desc.setLocationURI(uri);
		project.create(desc, null);
		if (!project.isOpen()) {
			project.open(null);
		}

		addJavaNature("ExternalProject");
		IJavaProject javaProject = JavaCore.create(project);
		IClasspathEntry[] classpath = new IClasspathEntry[2];
		Util.createJar(
				new String[0],
				new String[] {
					"META-INF/MANIFEST.MF",
					"Manifest-Version: 1.0\n" +
					"Class-Path: \n",
				},
				getExternalResourcePath("development/third_party/lib.jar"),
				CompilerOptions.getFirstSupportedJavaVersion());
		createExternalFolder("development/ExternalProject/src");
		createExternalFolder("development/ExternalProject/bin");
		classpath[0] = JavaCore.newSourceEntry(new Path("/ExternalProject/src"), null, null);
		classpath[1] = JavaCore.newLibraryEntry(new Path("../third_party/lib.jar"), null, null);
		javaProject.setRawClasspath(classpath, new Path("/ExternalProject/bin"), null);

		refresh(javaProject);
		waitForAutoBuild();
		assertBuildPathMarkers("Unexpected markers", "", javaProject);

	} finally {
		deleteProject("ExternalProject");
		deleteExternalResource("development");

	}
}

/**
 * bug 338006: IJavaProject#getPackageFragmentRoots() should return roots in order
 *
 * Test whether the {@link IJavaProject#getPackageFragmentRoots()} returns the package fragment
 * roots in the same order as defined by the .classpath even when the elements are added in a
 * different order.
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=338006"
 */
public void testBug338006() throws Exception {
	try {
		IJavaProject p = this.createJavaProject("P", new String[] {}, "bin");

		String content = new String(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ "<classpath>\n"
				+ "<classpathentry kind=\"src\" path=\"src a\"/>\n"
				+ "<classpathentry kind=\"src\" path=\"src x\"/>\n"
				+ "<classpathentry kind=\"lib\" path=\""
				+ getExternalJCLPath()
				+ "\"/>\n"
				+ "<classpathentry kind=\"output\" path=\"bin\"/>\n"
				+ "</classpath>\n");

		editFile("/P/.classpath", content);
		p.open(null);
		createFolder("/P/src x");
		createFolder("/P/src a");

		IPackageFragmentRoot[] roots = p.getPackageFragmentRoots();
		assertPackageFragmentRootsEqual(roots,
				"src a (not open) [in P]\n" +
				"src x (not open) [in P]\n" +
				""+ getExternalJCLPathString() + " (not open)");

	} finally {
		deleteProject("P");
	}
}

/*
 * Ensures that the correct delta is reported when changing the Class-Path: clause
 * of an external jar from not containing a chained jar to containing a chained jar.
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=357425)
 */
public void testBug357425() throws Exception {
	try {
		IJavaProject p = createJavaProject("P");
		addExternalLibrary(p, getExternalResourcePath("lib357425_a.jar"), new String[0],
			new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n"
			},
			CompilerOptions.getFirstSupportedJavaVersion());
		refreshExternalArchives(p);

		startDeltas();
		org.eclipse.jdt.core.tests.util.Util.createJar(new String[0],
			new String[] {
				"META-INF/MANIFEST.MF",
				"Manifest-Version: 1.0\n" +
				"Class-Path: lib357425_b.jar\n",
			},
			getExternalResourcePath("lib357425_a.jar"),
			CompilerOptions.getFirstSupportedJavaVersion());
		createExternalFile("lib357425_b.jar", "");

		refreshExternalArchives(p);
		assertDeltas(
			"Unexpected delta",
			"P[*]: {CHILDREN | RESOLVED CLASSPATH CHANGED}\n" +
			"	"+ getExternalPath() + "lib357425_a.jar[*]: {CONTENT | REORDERED | ARCHIVE CONTENT CHANGED}\n" +
			"	"+ getExternalPath() + "lib357425_b.jar[+]: {}"
				);
	} finally {
		stopDeltas();
		deleteProject("P");
		deleteExternalResource("lib357425_a.jar");
		deleteExternalResource("lib357425_b.jar");
	}
}
/**
 * bug287164: Report build path error if source folder has other source folder as output folder
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=287164"
 */
public void testBug287164() throws CoreException {
	try {
		IJavaProject proj =  this.createJavaProject("P", new String[] {}, "");

		// Test that with the option set to IGNORE, the Java model status returns OK
		proj.setOption(JavaCore.CORE_OUTPUT_LOCATION_OVERLAPPING_ANOTHER_SOURCE, JavaCore.IGNORE);
		IClasspathEntry[] originalCP = proj.getRawClasspath();

		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length+2];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P/src"), new IPath[0], new Path("/P/src2"));
		newCP[originalCP.length+1] = JavaCore.newSourceEntry(new Path("/P/src2"), new IPath[0], new Path("/P/src"));

		createFolder("/P/src");
		createFolder("/P/src2");

		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());
		assertTrue(status.isOK());
		assertStatus(
			"OK",
			status);

		proj.setRawClasspath(newCP, null);
		assertBuildPathMarkers("Unexpected markers", "", proj);

		// Test that with the option set to WARNING, status.isOK() returns true
		proj.setOption(JavaCore.CORE_OUTPUT_LOCATION_OVERLAPPING_ANOTHER_SOURCE, JavaCore.WARNING);

		status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());
		assertTrue(status.isOK());
		assertStatus(
			"Source folder \'src\' in project \'P\' cannot output to distinct source folder \'src2\'",
			status);

		assertBuildPathMarkers("Unexpected markers",
				"Source folder \'src\' in project \'P\' cannot output to distinct source folder \'src2\'", proj);

		// Test that with the option set to WARNING and the presence of a more severe error scenario, the error status
		// is returned
		IClasspathEntry[] newCP2 = new IClasspathEntry[newCP.length+1];
		System.arraycopy(newCP, 0, newCP2, 0, newCP.length-1);
		newCP2[newCP.length] = JavaCore.newLibraryEntry(new Path("/P/lib2"), null, null);
		newCP2[newCP.length-1] = JavaCore.newSourceEntry(new Path("/P/src2"), new IPath[0], new Path("/P/lib2"));

		status = JavaConventions.validateClasspath(proj, newCP2, proj.getOutputLocation());
		assertFalse(status.isOK());
		assertStatus(
			"Source folder \'src2\' in project 'P' cannot output to library \'lib2\'",
			status);

		proj.setOption(JavaCore.CORE_OUTPUT_LOCATION_OVERLAPPING_ANOTHER_SOURCE, JavaCore.ERROR);

		status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());
		assertStatus(
			"Source folder \'src\' in project \'P\' cannot output to distinct source folder \'src2\'",
			status);

	} finally {
		this.deleteProject("P");
	}
}

/**
 * bug220928: [buildpath] Should be able to ignore warnings from certain source folders
 *
 * Verify that adding the {@link IClasspathAttribute#IGNORE_OPTIONAL_PROBLEMS} attribute is
 * correctly reflected by the {@link ClasspathEntry#ignoreOptionalProblems()} method.
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=220928"
 */
public void testBug220928a() throws CoreException {
	ClasspathEntry entry;

	entry = (ClasspathEntry) JavaCore.newSourceEntry(new Path("/P/src"));
	assertFalse(entry.ignoreOptionalProblems());

	entry = (ClasspathEntry) JavaCore.newSourceEntry(new Path("/P/src"), null, null, null,
			new IClasspathAttribute[] { ATTR_IGNORE_OPTIONAL_PROBLEMS_TRUE });
	assertTrue(entry.ignoreOptionalProblems());

	entry = (ClasspathEntry) JavaCore.newSourceEntry(new Path("/P/src"), null, null, null,
			new IClasspathAttribute[] { ATTR_IGNORE_OPTIONAL_PROBLEMS_FALSE });
	assertFalse(entry.ignoreOptionalProblems());
}

/**
 * bug220928: [buildpath] Should be able to ignore warnings from certain source folders
 *
 * Verify that value of the {@link IClasspathAttribute#IGNORE_OPTIONAL_PROBLEMS} attribute is
 * correctly saved on workspace save.
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=220928"
 */
public void testBug220928b() throws CoreException {
	boolean autoBuild = getWorkspace().isAutoBuilding();
	IWorkspaceDescription preferences = getWorkspace().getDescription();
	try {
		// ensure that the workspace auto-build is ON
		preferences.setAutoBuilding(true);
		getWorkspace().setDescription(preferences);

		IJavaProject project = createJavaProject("P", new String[] {}, "bin");
		createFolder("/P/src");
		IClasspathEntry[] originalCP = project.getRawClasspath();
		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length + 1];
		State state;

		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P/src"));
		getJavaProject("P").setRawClasspath(newCP, null);
		simulateExitRestart();
		state = (State) JavaModelManager.getJavaModelManager().getLastBuiltState(getJavaProject("P").getProject(), null);
		assertFalse(state.sourceLocations[0].ignoreOptionalProblems);

		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P/src"), null, null, null,
				new IClasspathAttribute[] { ATTR_IGNORE_OPTIONAL_PROBLEMS_TRUE });
		getJavaProject("P").setRawClasspath(newCP, null);
		simulateExitRestart();
		state = (State) JavaModelManager.getJavaModelManager().getLastBuiltState(getJavaProject("P").getProject(), null);
		assertTrue(state.sourceLocations[0].ignoreOptionalProblems);

		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P/src"), null, null, null,
				new IClasspathAttribute[] { ATTR_IGNORE_OPTIONAL_PROBLEMS_FALSE });
		getJavaProject("P").setRawClasspath(newCP, null);
		simulateExitRestart();
		state = (State) JavaModelManager.getJavaModelManager().getLastBuiltState(getJavaProject("P").getProject(), null);
		assertFalse(state.sourceLocations[0].ignoreOptionalProblems);
	} finally {
		try {
			deleteProject("P");
		} finally {
			restoreAutobuild(preferences, autoBuild);
		}
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=396299
public void testBug396299() throws Exception {
	boolean autoBuild = getWorkspace().isAutoBuilding();
	IWorkspaceDescription preferences = getWorkspace().getDescription();
	try {
		JavaModelManager.EclipsePreferencesListener prefListener = new JavaModelManager.EclipsePreferencesListener();
		preferences.setAutoBuilding(true);
		getWorkspace().setDescription(preferences);

		JavaProject proj1 =  (JavaProject) this.createJavaProject("P1", new String[] {}, "");
		addLibrary(proj1, "abc.jar", null, new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {}\n"},
				CompilerOptions.getLatestVersion());
		proj1.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.getLatestVersion());

		Map map = proj1.getOptions(false);
		map.put(JavaCore.CORE_INCOMPATIBLE_JDK_LEVEL, JavaCore.WARNING);
		proj1.setOptions(map);

		IEclipsePreferences eclipsePreferences = proj1.getEclipsePreferences();
		eclipsePreferences.addPreferenceChangeListener(prefListener);
		simulateExitRestart();
		waitForAutoBuild();
		assertBuildPathMarkers("Unexpected markers", "", proj1);
		map = proj1.getOptions(false);
		map.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.getFirstSupportedJavaVersion());
		proj1.setOptions(map);
		waitForManualRefresh();
		assertBuildPathMarkers("Unexpected markers",
				"Incompatible .class files version in required binaries. Project \'P1\' is targeting a " + CompilerOptions.getFirstSupportedJavaVersion()
						+ " runtime, but is compiled against \'P1/abc.jar\' which requires a " + CompilerOptions.getLatestVersion() + " runtime", proj1);
		 eclipsePreferences.removePreferenceChangeListener(prefListener);
	} finally {
		try {
			deleteProject("P1");
		} finally {
			restoreAutobuild(preferences, autoBuild);
		}
	}
}
/**
 * bug 411423: JavaProject.resolveClasspath is spending more than 90% time on ExternalFoldersManager.isExternalFolderPath
 * Tests that
 * 1) External file paths are cached during classpath resolution and can be retrieved later on
 * 2) A full save (after resetting the external files cache) caches the external files information
 * 3) when a project is deleted, the external files cache is reset.
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=411423"
 */
public void testBug411423() throws Exception {
	try {
		IJavaProject proj = this.createJavaProject("P", new String[] {}, "bin");

		createFile("/P/internal.jar", "");
		createFolder("/P/internalSrcFolder");
		createExternalFile("external.jar", "");
		createExternalFile("external-src.jar", "");
		createExternalFolder("externalFolder");
		createExternalFolder("externalSrcFolder");

		IClasspathEntry[] classpath = new IClasspathEntry[3];
		classpath[0] = JavaCore.newLibraryEntry(new Path("/P/internal.jar"), new Path("/P/internalSrcFolder"), null);
		classpath[1] = JavaCore.newLibraryEntry(new Path(getExternalFile("external.jar").getPath()), new Path(getExternalFile("external-src.jar").getPath()), null);
		classpath[2] = JavaCore.newLibraryEntry(new Path(getExternalFile("externalFolder").getPath()), new Path(getExternalFile("externalSrcFolder").getPath()), null);

		proj.setRawClasspath(classpath, null);
		waitForAutoBuild();
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		assertFalse("Internal Jar", manager.isExternalFile(classpath[0].getPath()));
		assertFalse("Internal Folder", manager.isExternalFile(classpath[0].getSourceAttachmentPath()));
		assertTrue("External Jar", manager.isExternalFile(classpath[1].getPath()));
		assertTrue("External Jar", manager.isExternalFile(classpath[1].getSourceAttachmentPath()));
		assertFalse("External Folder", manager.isExternalFile(classpath[2].getPath()));
		assertFalse("External Folder", manager.isExternalFile(classpath[2].getSourceAttachmentPath()));

		((JavaProject)proj).resetResolvedClasspath();
		proj.getResolvedClasspath(true);
		assertFalse("Internal Jar", manager.isExternalFile(classpath[0].getPath()));
		assertFalse("Internal Jar", manager.isExternalFile(classpath[0].getSourceAttachmentPath()));
		assertTrue("External Jar", manager.isExternalFile(classpath[1].getPath()));
		assertTrue("External Jar", manager.isExternalFile(classpath[1].getSourceAttachmentPath()));
		assertFalse("External Folder", manager.isExternalFile(classpath[2].getPath()));
		assertFalse("External Folder", manager.isExternalFile(classpath[2].getSourceAttachmentPath()));

		((JavaProject)proj).resetResolvedClasspath();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.save(true, null);
		assertFalse("Internal Jar", manager.isExternalFile(classpath[0].getPath()));
		assertFalse("Internal Jar", manager.isExternalFile(classpath[0].getSourceAttachmentPath()));
		assertTrue("External Jar", manager.isExternalFile(classpath[1].getPath()));
		assertTrue("External Jar", manager.isExternalFile(classpath[1].getSourceAttachmentPath()));
		assertFalse("External Folder", manager.isExternalFile(classpath[2].getPath()));
		assertFalse("External Folder", manager.isExternalFile(classpath[2].getSourceAttachmentPath()));

		this.deleteProject("P");
		assertFalse("Cache reset", manager.isExternalFile(classpath[0].getPath()));
		assertFalse("Cache reset", manager.isExternalFile(classpath[0].getSourceAttachmentPath()));
		assertFalse("Cache reset", manager.isExternalFile(classpath[1].getPath()));
		assertFalse("Cache reset", manager.isExternalFile(classpath[1].getSourceAttachmentPath()));
		assertFalse("Cache reset", manager.isExternalFile(classpath[2].getPath()));
		assertFalse("Cache reset", manager.isExternalFile(classpath[2].getSourceAttachmentPath()));
	} finally {
		IProject proj = this.getProject("P");
		if ( proj != null && proj.exists())
			this.deleteProject("P");
		deleteExternalResource("external.jar");
		deleteExternalResource("external-src.jar");
		deleteExternalResource("externalFolder");
		deleteExternalResource("externalSrcFolder");
	}
}

public void testClasspathTestSourceValidation1() throws CoreException {
	try {
		IJavaProject proj = this.createJavaProject("P", new String[] {}, "bin");
		IClasspathEntry[] originalCP = proj.getRawClasspath();

		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length + 2];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P/src"), null, null, null,
				new IClasspathAttribute[] {});
		newCP[originalCP.length + 1] = JavaCore.newSourceEntry(new Path("/P/src-tests"), null, null,
				new Path("/P/bin-tests"),
				new IClasspathAttribute[] { JavaCore.newClasspathAttribute(IClasspathAttribute.TEST, "true") });

		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());

		assertStatus("should not complain", "OK", status);
	} finally {
		this.deleteProject("P");
	}
}
public void testClasspathTestSourceValidation2() throws CoreException {
	try {
		IJavaProject proj = this.createJavaProject("P", new String[] {}, "bin");
		IClasspathEntry[] originalCP = proj.getRawClasspath();

		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length + 2];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P/src"), null, null, null,
				new IClasspathAttribute[] {});
		newCP[originalCP.length + 1] = JavaCore.newSourceEntry(new Path("/P/src-tests"), null, null, null,
				new IClasspathAttribute[] { JavaCore.newClasspathAttribute(IClasspathAttribute.TEST, "true") });

		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());

		assertStatus("should complain because tests have no output folder", "Test source folder 'src-tests' in project 'P' must have a separate output folder", status);
	} finally {
		this.deleteProject("P");
	}
}
public void testClasspathTestSourceValidation3() throws CoreException {
	try {
		IJavaProject proj = this.createJavaProject("P", new String[] {}, "bin");
		IClasspathEntry[] originalCP = proj.getRawClasspath();

		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length + 1];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P/src-tests"), null, null, null,
				new IClasspathAttribute[] { JavaCore.newClasspathAttribute(IClasspathAttribute.TEST, "true") });

		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());

		assertStatus("should not complain because no main sources are present", "OK", status);
	} finally {
		this.deleteProject("P");
	}
}
public void testClasspathTestSourceValidation4() throws CoreException {
	try {
		IJavaProject proj = this.createJavaProject("P", new String[] {}, "");
		IClasspathEntry[] originalCP = proj.getRawClasspath();

		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length + 2];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P/src"), null, null, new Path("/P/bin"),
				new IClasspathAttribute[] {});
		newCP[originalCP.length + 1] = JavaCore.newSourceEntry(new Path("/P/src-tests"), null, null, new Path("/P/bin-tests"),
				new IClasspathAttribute[] { JavaCore.newClasspathAttribute(IClasspathAttribute.TEST, "true") });

		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());

		assertStatus("should not complain because main sources have their own output folder", "OK", status);
	} finally {
		this.deleteProject("P");
	}
}
public void testClasspathTestSourceValidation5() throws CoreException {
	try {
		IJavaProject proj = this.createJavaProject("P", new String[] {}, "bin");
		IClasspathEntry[] originalCP = proj.getRawClasspath();

		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length + 2];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P/src"), null, null, new Path("/P/bin"),
				new IClasspathAttribute[] {});
		newCP[originalCP.length + 1] = JavaCore.newSourceEntry(new Path("/P/src-tests"), null, null, new Path("/P/bin"),
				new IClasspathAttribute[] { JavaCore.newClasspathAttribute(IClasspathAttribute.TEST, "true") });

		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());

		assertStatus("should complain because main sources have the same own output folder", "Test source folder 'src-tests' in project 'P' must have an output folder that is not also used for main sources", status);
	} finally {
		this.deleteProject("P");
	}
}

public void testBug539998() throws CoreException {
	try {
		IJavaProject proj1TestOnly = this.createJavaProject("P1", new String[] {}, "bin-tests");
		IClasspathEntry[] originalCP1 = proj1TestOnly.getRawClasspath();

		IClasspathEntry[] newCP1 = new IClasspathEntry[originalCP1.length + 1];
		System.arraycopy(originalCP1, 0, newCP1, 0, originalCP1.length);
		newCP1[originalCP1.length] = JavaCore.newSourceEntry(new Path("/P1/src-tests"), null, null, null,
				new IClasspathAttribute[] { JavaCore.newClasspathAttribute(IClasspathAttribute.TEST, "true") });
		proj1TestOnly.setRawClasspath(newCP1, new NullProgressMonitor());
		IJavaProject proj = this.createJavaProject("P2", new String[] {}, "bin");
		IClasspathEntry[] originalCP = proj.getRawClasspath();

		IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length + 2];
		System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
		newCP[originalCP.length] = JavaCore.newSourceEntry(new Path("/P2/src"), null, null, null,
				new IClasspathAttribute[] {});
		newCP[originalCP.length + 1] = JavaCore.newProjectEntry(new Path("/P1/"));

		IJavaModelStatus status = JavaConventions.validateClasspath(proj, newCP, proj.getOutputLocation());

		assertStatus("should complain",
				"Project has only main sources but depends on project 'P1' which has only test sources.",
				status);
	} finally {
		this.deleteProjects(new String[] { "P1", "P2" });
	}
}

public void testBug576735a() throws Exception {
	IJavaProject project = this.createJavaProject("P1", new String[] {"src"}, new String[] { "JCL18_LIB"}, "bin", "1.8" );
	try {
		String projectLocation = project.getProject().getLocation().toOSString();

		// create a jar that will be missing from P1's dependencies:
		Util.createJar(
				new String[] {
						"lib576735a1/Missing.java",
						"package libMissing;\n" +
						"public class Missing {\n" +
						"}\n"
				},
				getDefaultJavaCoreOptions(),
				projectLocation + File.separator + "libMissing.jar");

		// create another jar depending on libMissing.jar:
		String[] classpath = getJCLLibrary("1.8");
		classpath = Arrays.copyOf(classpath, classpath.length+1);
		classpath[classpath.length-1] = projectLocation + File.separator + "libMissing.jar";
		Util.createJar(
				new String[] {
						"lib576735a/Lib.java",
						"package lib576735a;\n" +
						"public class Lib {\n" +
						"	void m(libMissing.Missing arg) {}\n" +
						"}\n"
				},
				new String[0],
				getDefaultJavaCoreOptions(),
				classpath,
				projectLocation + File.separator + "lib576735a.jar");
		project.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
		addLibraryEntry(project, "/P1/lib576735a.jar", false);

		createFile(
				"/P1/src/X.java",
				"/* header comment */\n" + // #1 (locationless): The type libMissing.Missing cannot be resolved. It is indirectly referenced from required type lib576735a.Lib
				"import libMissing.Missing;\n" + // #2: The import libMissing.Missing cannot be resolved
				"public class X extends lib576735a.Lib {\n" +
				"	Missing f;\n" + // #4: Missing cannot be resolved to a type
				"}\n"
			);

		project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
		IMarker[] markers = project.getProject().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);
		sortMarkers(markers);
		assertMarkers("Unexpected markers",
				"The project was not built since its build path is incomplete. Cannot find the class file for libMissing.Missing. Fix the build path then try building this project\n" +
				"The type libMissing.Missing cannot be resolved. It is indirectly referenced from required type lib576735a.Lib",
				markers);
	} finally {
		if ( project != null && project.exists())
			this.deleteProject("P1");
	}
}
}
