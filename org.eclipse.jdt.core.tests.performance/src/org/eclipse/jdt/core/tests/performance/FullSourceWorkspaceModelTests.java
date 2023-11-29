/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *     Thirumala Reddy Mutchukota <thirumala@google.com> - Contribution to bug: https://bugs.eclipse.org/bugs/show_bug.cgi?id=411423
 *     Terry Parker <tparker@google.com> - [performance] Low hit rates in JavaModel caches - https://bugs.eclipse.org/421165
 *     Vladimir Piskarev <pisv@1c.ru> - Building large Java element deltas is really slow - https://bugs.eclipse.org/443928
 *******************************************************************************/
package org.eclipse.jdt.core.tests.performance;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IModuleDescription;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.core.search.TypeNameRequestor;
import org.eclipse.jdt.core.tests.model.AbstractJavaModelTests.ProblemRequestor;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.IJavaElementRequestor;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.NameLookup;
import org.eclipse.test.performance.Performance;

import junit.framework.Test;

@SuppressWarnings({"rawtypes", "unchecked"})
public class FullSourceWorkspaceModelTests extends FullSourceWorkspaceTests implements IJavaSearchConstants {

	// Tests counters
	static int TESTS_COUNT = 0;
	private final static int WARMUP_COUNT = 50;
	private final static int FOLDERS_COUNT = 200;
	private final static int PACKAGES_COUNT = 200;
	static int TESTS_LENGTH;

	/**
	 * Internal synonym for deprecated constant AST.JSL3
	 * to alleviate deprecation warnings.
	 * @deprecated
	 */
	/*package*/ static final int JLS3_INTERNAL = AST.JLS3;

	// Log file streams
	private static PrintStream[] LOG_STREAMS = new PrintStream[DIM_NAMES.length];

	// Type path
	static IPath BIG_PROJECT_TYPE_PATH;
	static ICompilationUnit WORKING_COPY;

public FullSourceWorkspaceModelTests(String name) {
	super(name);
}

static {
//	TESTS_NAMES = new String[] {
//		"testPerfNameLookupFindKnownSecondaryType",
//		"testPerfNameLookupFindUnknownType",
//		"testPerfReconcile",
//		"testPerfSearchAllTypeNamesAndReconcile",
//	};

//	TESTS_PREFIX = "testPerfReconcile";
}
public static Test suite() {
	Test suite = buildSuite(testClass());
	TESTS_LENGTH = TESTS_COUNT = suite.countTestCases();
	createPrintStream(testClass(), LOG_STREAMS, TESTS_COUNT, null);
	return suite;
}

private static Class testClass() {
	return FullSourceWorkspaceModelTests.class;
}

@Override
protected void setUp() throws Exception {
	super.setUp();
	setUpBigProject();
	setUpBigJars();
}
private void setUpBigProject() throws CoreException, IOException {
	try {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot workspaceRoot = workspace.getRoot();
		String targetWorkspacePath = workspaceRoot.getLocation().toFile().getCanonicalPath();
		long start = System.currentTimeMillis();

		// Print for log in case of project creation troubles...
		File wkspDir = new File(targetWorkspacePath);
		File projectDir = new File(wkspDir, BIG_PROJECT_NAME);
		if (projectDir.exists()) {
			System.out.print("Add existing project "+BIG_PROJECT_NAME+" in "+workspaceRoot.getLocation()+" to workspace...");
			IProject bigProject = workspaceRoot.getProject(BIG_PROJECT_NAME);
			if (bigProject.exists()) {
				ENV.addProject(bigProject);
			} else {
				ENV.addProject(BIG_PROJECT_NAME);
			}
			BIG_PROJECT = (JavaProject) ENV.getJavaProject(BIG_PROJECT_NAME);
			BIG_PROJECT.setRawClasspath(BIG_PROJECT.getRawClasspath(), null);
		} else {
			System.out.println("Create project "+BIG_PROJECT_NAME+" in "+workspaceRoot.getLocation()+":");

			// setup projects with several source folders and several packages per source folder
			System.out.println("	- create "+FOLDERS_COUNT+" folders x "+PACKAGES_COUNT+" packages...");
			final String[] sourceFolders = new String[FOLDERS_COUNT];
			for (int i = 0; i < FOLDERS_COUNT; i++) {
				sourceFolders[i] = "src" + i;
			}
			String path = workspaceRoot.getLocation().toString() + "/BigProject/src";
			for (int i = 0; i < FOLDERS_COUNT; i++) {
				if (PRINT && i>0 && i%10==0) System.out.print("		+ folder src"+i+"...");
				long top = System.currentTimeMillis();
				for (int j = 0; j < PACKAGES_COUNT; j++) {
					new java.io.File(path + i + "/org/eclipse/jdt/core/tests" + i + "/performance" + j).mkdirs();
				}
				if (PRINT && i>0 && i%10==0) System.out.println("("+(System.currentTimeMillis()-top)+"ms)");
			}
			System.out.println("		=> global time = "+(System.currentTimeMillis()-start)/1000.0+" seconds)");

			// Add project to workspace
			start = System.currentTimeMillis();
			System.out.print("	- add project to full source workspace...");
			ENV.addProject(BIG_PROJECT_NAME);
			BIG_PROJECT = (JavaProject) createJavaProject(BIG_PROJECT_NAME, sourceFolders, "bin", "1.4");
			BIG_PROJECT.setRawClasspath(BIG_PROJECT.getRawClasspath(), null);
		}
		System.out.println("("+(System.currentTimeMillis()-start)+"ms)");

		// Add CU with secondary type
		System.out.print("	- Create compilation unit with secondary type...");
		start = System.currentTimeMillis();
		BIG_PROJECT_TYPE_PATH = new Path("/BigProject/src" + (FOLDERS_COUNT-1) + "/org/eclipse/jdt/core/tests" + (FOLDERS_COUNT-1) + "/performance" + (PACKAGES_COUNT-1) + "/TestBigProject.java");
		IFile file = workspaceRoot.getFile(BIG_PROJECT_TYPE_PATH);
		if (!file.exists()) {
			String content = "package org.eclipse.jdt.core.tests" + (FOLDERS_COUNT-1) + ".performance" + (PACKAGES_COUNT-1) + ";\n" +
				"public class TestBigProject {\n" +
				"	class Level1 {\n" +
				"		class Level2 {\n" +
				"			class Level3 {\n" +
				"				class Level4 {\n" +
				"					class Level5 {\n" +
				"						class Level6 {\n" +
				"							class Level7 {\n" +
				"								class Level8 {\n" +
				"									class Level9 {\n" +
				"										class Level10 {}\n" +
				"									}\n" +
				"								}\n" +
				"							}\n" +
				"						}\n" +
				"					}\n" +
				"				}\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}\n" +
				"class TestSecondary {}\n";
			file.create(new ByteArrayInputStream(content.getBytes()), true, null);
		}
		WORKING_COPY = (ICompilationUnit)JavaCore.create(file);
		System.out.println("("+(System.currentTimeMillis()-start)+"ms)");
	} finally {
		// do not delete project
	}

}
private void setUpBigJars() throws Exception {
	String bigProjectLocation = BIG_PROJECT.getResource().getLocation().toOSString();
	int size = PACKAGES_COUNT * 10;
	File bigJar1 = new File(bigProjectLocation, BIG_JAR1_NAME);
	if (!bigJar1.exists()) {
		String[] pathAndContents = new String[size * 2];
		for (int i = 0; i < size; i++) {
			pathAndContents[i*2] = "/p" + i + "/X" + i + ".java";
			pathAndContents[i*2 + 1] =
				"package p" + i + ";\n" +
				"public class X" + i + "{\n" +
				"}";
		}
		org.eclipse.jdt.core.tests.util.Util.createJar(pathAndContents, bigJar1.getPath(), "1.3");
		BIG_PROJECT.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
	}
	File bigJar2 = new File(bigProjectLocation, BIG_JAR2_NAME);
	if (!bigJar2.exists()) {
		String[] pathAndContents = new String[size * 2];
		for (int i = 0; i < size; i++) {
			pathAndContents[i*2] = "/q" + i + "/Y" + i + ".java";
			pathAndContents[i*2 + 1] =
				"package q" + i + ";\n" +
				"public class Y" + i + "{\n" +
				"}";
		}
		org.eclipse.jdt.core.tests.util.Util.createJar(pathAndContents, bigJar2.getPath(), "1.3");
		BIG_PROJECT.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
	}
}
/* (non-Javadoc)
 * @see junit.framework.TestCase#tearDown()
 */
@Override
protected void tearDown() throws Exception {

	// End of execution => one test less
	TESTS_COUNT--;

	// Log perf result
	if (LOG_DIR != null) {
		logPerfResult(LOG_STREAMS, TESTS_COUNT);
	}

	// Print statistics
	if (TESTS_COUNT == 0) {
		System.out.println("-------------------------------------");
		System.out.println("Model performance test statistics:");
//		NumberFormat intFormat = NumberFormat.getIntegerInstance();
		System.out.println("-------------------------------------\n");
	}
	super.tearDown();
}
/**
 * Simple search result collector: only count matches.
 */
class JavaSearchResultCollector extends SearchRequestor {
	int count = 0;
	public void acceptSearchMatch(SearchMatch match) throws CoreException {
		this.count++;
	}
}

/*
protected void search(String patternString, int searchFor, int limitTo) throws CoreException {
	int matchMode = patternString.indexOf('*') != -1 || patternString.indexOf('?') != -1
		? SearchPattern.R_PATTERN_MATCH
		: SearchPattern.R_EXACT_MATCH;
	SearchPattern pattern = SearchPattern.createPattern(
		patternString,
		searchFor,
		limitTo,
		matchMode | SearchPattern.R_CASE_SENSITIVE);
	this.resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		pattern,
		new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
		this.scope,
		this.resultCollector,
		null);
}
*/

protected void searchAllTypeNames(IJavaSearchScope scope) throws CoreException {
	class TypeNameCounter extends TypeNameRequestor {
		int count = 0;
		public void acceptType(int modifiers, char[] packageName, char[] simpleTypeName, char[][] enclosingTypeNames, String path) {
			this.count++;
		}
	}
	TypeNameCounter requestor = new TypeNameCounter();
	new SearchEngine().searchAllTypeNames(
		null,
		SearchPattern.R_EXACT_MATCH,
		null,
		SearchPattern.R_PREFIX_MATCH, // not case sensitive
		IJavaSearchConstants.TYPE,
		scope,
		requestor,
		WAIT_UNTIL_READY_TO_SEARCH,
		null);
	assertTrue("We should have found at least one type!", requestor.count>0);
}

/**
 * @see org.eclipse.jdt.core.tests.model.AbstractJavaModelTests#assertElementEquals(String, String, IJavaElement)
 */
protected void assertElementEquals(String message, String expected, IJavaElement element) {
	String actual = element == null ? "<null>" : ((JavaElement) element).toStringWithAncestors(false/*don't show key*/);
	if (!expected.equals(actual)) {
		System.out.println(getName()+" actual result is:");
		System.out.println(actual + ',');
	}
	assertEquals(message, expected, actual);
}
/**
 * @see org.eclipse.jdt.core.tests.model.AbstractJavaModelTests#assertElementsEqual(String, String, IJavaElement[])
 */
protected void assertElementsEqual(String message, String expected, IJavaElement[] elements) {
	assertElementsEqual(message, expected, elements, false/*don't show key*/);
}
/**
 * @see org.eclipse.jdt.core.tests.model.AbstractJavaModelTests#assertElementsEqual(String, String, IJavaElement[], boolean)
 */
protected void assertElementsEqual(String message, String expected, IJavaElement[] elements, boolean showResolvedInfo) {
	StringBuilder buffer = new StringBuilder();
	if (elements != null) {
		for (int i = 0, length = elements.length; i < length; i++){
			JavaElement element = (JavaElement)elements[i];
			if (element == null) {
				buffer.append("<null>");
			} else {
				buffer.append(element.toStringWithAncestors(showResolvedInfo));
			}
			if (i != length-1) buffer.append("\n");
		}
	} else {
		buffer.append("<null>");
	}
	String actual = buffer.toString();
	if (!expected.equals(actual)) {
		System.out.println(getName()+" actual result is:");
		System.out.println(actual + ',');
	}
	assertEquals(message, expected, actual);
}
private void touchFiles(File[] files) {
	for(int index=0; index < files.length; index++) {
		files[index].setLastModified(System.currentTimeMillis());
	}
}
/*
 * Creates a simple Java project with no source folder and only rt.jar on its classpath.
 */
private IJavaProject createJavaProject(String name) throws CoreException {
	IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
	if (project.exists())
		project.delete(true, null);
	project.create(null);
	project.open(null);
	IProjectDescription description = project.getDescription();
	description.setNatureIds(new String[] {JavaCore.NATURE_ID});
	project.setDescription(description, null);
	IJavaProject javaProject = JavaCore.create(project);
	javaProject.setRawClasspath(new IClasspathEntry[] {JavaCore.newVariableEntry(new Path("JRE_LIB"), null, null)}, null);
	return javaProject;
}

private NameLookup getNameLookup(JavaProject project) throws JavaModelException {
	return project.newNameLookup((WorkingCopyOwner)null);
}

/**
 * Performance tests for model: Find known type in name lookup.
 *
 * First wait that already started indexing jobs end before perform test.
 * Perform one find before measure performance for warm-up.
 */
public void testNameLookupFindKnownType() throws CoreException {

	// Wait for indexing end
	waitUntilIndexesReady();

	// Warm up
	String fullQualifiedName = BIG_PROJECT_TYPE_PATH.removeFileExtension().removeFirstSegments(2).toString();
	fullQualifiedName = fullQualifiedName.replace('/', '.');
	for (int i=0; i<WARMUP_COUNT; i++) {
		NameLookup nameLookup = BIG_PROJECT.newNameLookup(DefaultWorkingCopyOwner.PRIMARY);
		IType type = nameLookup.findType(fullQualifiedName, false /*full match*/, NameLookup.ACCEPT_ALL);
		if (i==0) assertNotNull("We should find type '"+fullQualifiedName+"' in project "+BIG_PROJECT_NAME, type);
	}

	// Measures
	resetCounters();
	for (int i=0; i<MEASURES_COUNT; i++) {
		runGc();
		startMeasuring();
		for (int n=0; n<50000; n++) {
			NameLookup nameLookup = BIG_PROJECT.newNameLookup(DefaultWorkingCopyOwner.PRIMARY);
			nameLookup.findType(fullQualifiedName, false /*full match*/, NameLookup.ACCEPT_ALL);
		}
		stopMeasuring();
	}

	// Commit
	commitMeasurements();
	assertPerformance();
}

/**
 * Performance tests for model: Find known secondary type in name lookup.
 *
 * First wait that already started indexing jobs end before perform test.
 * Perform one find before measure performance for warm-up.
 */
public void testNameLookupFindKnownSecondaryType() throws CoreException {

	// Wait for indexing end
	waitUntilIndexesReady();

	// Warm up
	String fullQualifiedName = BIG_PROJECT_TYPE_PATH.removeFileExtension().removeFirstSegments(2).removeLastSegments(1).toString();
	fullQualifiedName = fullQualifiedName.replace('/', '.')+".TestSecondary";
	for (int i=0; i<WARMUP_COUNT; i++) {
		NameLookup nameLookup = BIG_PROJECT.newNameLookup(DefaultWorkingCopyOwner.PRIMARY);
		IType type = nameLookup.findType(fullQualifiedName, false /*full match*/, NameLookup.ACCEPT_ALL);
		if (i==0 && LOG_VERSION.compareTo("v_623") > 0) {
			assertNotNull("We should find type '"+fullQualifiedName+"' in project "+BIG_PROJECT_NAME, type);
		}
	}

	// Measures
	resetCounters();
	for (int i=0; i<MEASURES_COUNT; i++) {
		runGc();
		startMeasuring();
		for (int n=0; n<50000; n++) {
			NameLookup nameLookup = BIG_PROJECT.newNameLookup(DefaultWorkingCopyOwner.PRIMARY);
			nameLookup.findType(fullQualifiedName, false /*full match*/, NameLookup.ACCEPT_ALL);
		}
		stopMeasuring();
	}

	// Commit
	commitMeasurements();
	assertPerformance();
}

/**
 * Performance tests for model: Find Unknown type in name lookup.
 *
 * First wait that already started indexing jobs end before perform test.
 * Perform one find before measure performance for warm-up.
 */
public void testNameLookupFindUnknownType() throws CoreException {

	// Wait for indexing end
	waitUntilIndexesReady();

	// Warm up
	String fullQualifiedName = BIG_PROJECT_TYPE_PATH.removeFileExtension().removeFirstSegments(2).removeLastSegments(1).toString();
	fullQualifiedName = fullQualifiedName.replace('/', '.')+".Unknown";
	for (int i=0; i<WARMUP_COUNT; i++) {
		NameLookup nameLookup = BIG_PROJECT.newNameLookup(DefaultWorkingCopyOwner.PRIMARY);
		IType type = nameLookup.findType(fullQualifiedName, false /*full match*/, NameLookup.ACCEPT_ALL);
		if (i==0) assertNull("We should not find an unknown type in project "+BIG_PROJECT_NAME, type);
	}

	// Measures
	resetCounters();
	for (int i=0; i<MEASURES_COUNT; i++) {
		runGc();
		startMeasuring();
		for (int n=0; n<50000; n++) {
			NameLookup nameLookup = BIG_PROJECT.newNameLookup(DefaultWorkingCopyOwner.PRIMARY);
			nameLookup.findType(fullQualifiedName, false /*full match*/, NameLookup.ACCEPT_ALL);
		}
		stopMeasuring();
	}

	// Commit
	commitMeasurements();
	assertPerformance();
}

/**
 * Performance tests for model: Find known type.
 *
 * First wait that already started indexing jobs end before perform test.
 * Perform one find before measure performance for warm-up.
 */
public void testProjectFindKnownType() throws CoreException {
	tagAsSummary("Find known type in project", false); // do NOT put in fingerprint

	// Wait for indexing end
	waitUntilIndexesReady();

	// Warm up
	String fullQualifiedName = BIG_PROJECT_TYPE_PATH.removeFileExtension().removeFirstSegments(2).toString();
	fullQualifiedName = fullQualifiedName.replace('/', '.');
	for (int i=0; i<WARMUP_COUNT; i++) {
		IType type = BIG_PROJECT.findType(fullQualifiedName);
		if (i==0) assertNotNull("We should find type '"+fullQualifiedName+"' in project "+BIG_PROJECT_NAME, type);
	}

	// Measures
	resetCounters();
	for (int i=0; i<MEASURES_COUNT; i++) {
		runGc();
		startMeasuring();
		for (int n=0; n<50000; n++) {
			BIG_PROJECT.findType(fullQualifiedName);
		}
		stopMeasuring();
	}

	// Commit
	commitMeasurements();
	assertPerformance();
}

/**
 * Performance tests for model: Find known member type.
 *
 * First wait that already started indexing jobs end before perform test.
 * Perform one find before measure performance for warm-up.
 */
public void testProjectFindKnownMemberType() throws CoreException {
	tagAsSummary("Find known member type in project", false); // do NOT put in fingerprint

	// Wait for indexing end
	waitUntilIndexesReady();

	// Warm up
	String fullQualifiedName = BIG_PROJECT_TYPE_PATH.removeFileExtension().removeFirstSegments(2).toString();
	fullQualifiedName = fullQualifiedName.replace('/', '.');
	for (int i=1; i<=10; i++) {
		fullQualifiedName += ".Level" + i;
	}
	for (int i=0; i<WARMUP_COUNT; i++) {
		IType type = BIG_PROJECT.findType(fullQualifiedName);
		if (i==0) assertNotNull("We should find type '"+fullQualifiedName+"' in project "+BIG_PROJECT_NAME, type);
	}

	// Measures
	resetCounters();
	for (int i=0; i<MEASURES_COUNT; i++) {
		runGc();
		startMeasuring();
		for (int n=0; n<4000; n++) {
			BIG_PROJECT.findType(fullQualifiedName);
		}
		stopMeasuring();
	}

	// Commit
	commitMeasurements();
	assertPerformance();
}

/**
 * Performance tests for model: Find known secondary type.
 *
 * First wait that already started indexing jobs end before perform test.
 * Perform one find before measure performance for warm-up.
 */
public void testProjectFindKnownSecondaryType() throws CoreException {
	tagAsSummary("Find known secondary type in project", false); // do NOT put in fingerprint

	// Wait for indexing end
	waitUntilIndexesReady();

	// Warm up
	String fullQualifiedName = BIG_PROJECT_TYPE_PATH.removeFileExtension().removeFirstSegments(2).removeLastSegments(1).toString();
	fullQualifiedName = fullQualifiedName.replace('/', '.')+".TestSecondary";
	for (int i=0; i<WARMUP_COUNT; i++) {
		BIG_PROJECT.findType(fullQualifiedName);
	}

	// Measures
	resetCounters();
	for (int i=0; i<MEASURES_COUNT; i++) {
		runGc();
		startMeasuring();
		for (int n=0; n<1000; n++) {
			BIG_PROJECT.findType(fullQualifiedName);
		}
		stopMeasuring();
	}

	// Commit
	commitMeasurements();
	assertPerformance();
}

/**
 * Performance tests for model: Find Unknown type.
 *
 * First wait that already started indexing jobs end before perform test.
 * Perform one find before measure performance for warm-up.
 */
public void testProjectFindUnknownType() throws CoreException {
	tagAsSummary("Find unknown type in project", false); // do NOT put in fingerprint

	// Wait for indexing end
	waitUntilIndexesReady();

	// Warm up
	String fullQualifiedName = BIG_PROJECT_TYPE_PATH.removeFileExtension().removeFirstSegments(2).removeLastSegments(1).toString();
	fullQualifiedName = fullQualifiedName.replace('/', '.')+".Unknown";
	for (int i=0; i<WARMUP_COUNT; i++) {
		IType type = BIG_PROJECT.findType(fullQualifiedName);
		assertNull("We should not find an unknown type in project "+BIG_PROJECT_NAME, type);
	}

	// Measures
	resetCounters();
	for (int i=0; i<MEASURES_COUNT; i++) {
		runGc();
		startMeasuring();
		for (int n=0; n<2000; n++) {
			BIG_PROJECT.findType(fullQualifiedName);
		}
		stopMeasuring();
	}

	// Commit
	commitMeasurements();
	assertPerformance();
}

/*
 * Performance tests for model: Find Unknown type after resetting the classpath
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=217059 )
 */
public void testProjectFindUnknownTypeAfterSetClasspath() throws CoreException {
	tagAsSummary("Find unknown type in project after resetting classpath", false); // do NOT put in fingerprint

	// Wait for indexing end
	waitUntilIndexesReady();

	// First findType populates the package fragment roots in the Java model cache
	String fullQualifiedName = BIG_PROJECT_TYPE_PATH.removeFileExtension().removeFirstSegments(2).removeLastSegments(1).toString();
	fullQualifiedName = fullQualifiedName.replace('/', '.')+".Unknown";
	IType type = BIG_PROJECT.findType(fullQualifiedName);
	assertNull("We should not find an unknown type in project "+BIG_PROJECT_NAME, type);

	// Reset classpath
	BIG_PROJECT.setRawClasspath(BIG_PROJECT.getRawClasspath(), null);

	// Warm up
	for (int i=0; i<WARMUP_COUNT; i++) {
		BIG_PROJECT.findType(fullQualifiedName);
	}

	// Measures
	resetCounters();
	for (int i=0; i<MEASURES_COUNT; i++) {
		runGc();
		startMeasuring();
		for (int n=0; n<2000; n++) {
			BIG_PROJECT.findType(fullQualifiedName);
		}
		stopMeasuring();
	}

	// Commit
	commitMeasurements();
	assertPerformance();
}

/**
 * Ensures that the reconciler does nothing when the source
 * to reconcile with is the same as the current contents.
 */
public void testPerfReconcile() throws CoreException {
	tagAsGlobalSummary("Reconcile editor change", true); // put in global fingerprint

	// Wait for indexing end
	waitUntilIndexesReady();

	// Warm up
	ICompilationUnit workingCopy = null;
	try {
		final ProblemRequestor requestor = new ProblemRequestor();
		WorkingCopyOwner owner = new WorkingCopyOwner() {
			public IProblemRequestor getProblemRequestor(ICompilationUnit cu) {
				return requestor;
            }
		};
		workingCopy = PARSER_WORKING_COPY.getWorkingCopy(owner, null);
		int warmup = WARMUP_COUNT / 5;
		for (int i=0; i<warmup; i++) {
			CompilationUnit unit = workingCopy.reconcile(JLS3_INTERNAL, true, null, null);
			assertNotNull("Compilation Unit should not be null!", unit);
			assertNotNull("Bindings were not resolved!", unit.getPackage().resolveBinding());
		}

		// Measures
		resetCounters();
		int iterations = 2;
		for (int i=0; i<MEASURES_COUNT; i++) {
			runGc();
			startMeasuring();
			for (int n=0; n<iterations; n++) {
				workingCopy.reconcile(JLS3_INTERNAL, true, null, null);
			}
			stopMeasuring();
		}
	}
	finally {
		workingCopy.discardWorkingCopy();
	}

	// Commit
	commitMeasurements();
	assertPerformance();

}

/*
 * Ensures that the performance of reconcile on a big CU when there are syntax errors is acceptable.
 * (regression test for bug 135083 RangeUtil#isInInterval(...) takes significant amount of time while editing)
 */
public void testPerfReconcileBigFileWithSyntaxError() throws JavaModelException {
	tagAsSummary("Reconcile editor change on big file with syntax error", false); // do NOT put in fingerprint

	// build big file contents
	String method =
		"() {\n" +
		"  bar(\n" +
		"    \"public class X <E extends Exception> {\\n\" + \r\n" +
		"	 \"    void foo(E e) throws E {\\n\" + \r\n" +
		"	 \"        throw e;\\n\" + \r\n" +
		"	 \"    }\\n\" + \r\n" +
		"	 \"    void bar(E e) {\\n\" + \r\n" +
		"	 \"        try {\\n\" + \r\n" +
		"	 \"            foo(e);\\n\" + \r\n" +
		"	 \"        } catch(Exception ex) {\\n\" + \r\n" +
		"	 \"	        System.out.println(\\\"SUCCESS\\\");\\n\" + \r\n" +
		"	 \"        }\\n\" + \r\n" +
		"	 \"    }\\n\" + \r\n" +
		"	 \"    public static void main(String[] args) {\\n\" + \r\n" +
		"	 \"        new X<Exception>().bar(new Exception());\\n\" + \r\n" +
		"	 \"    }\\n\" + \r\n" +
		"	 \"}\\n\"" +
		"  );\n" +
		"}\n";
	StringBuilder bigContents = new StringBuilder();
	bigContents.append("public class BigCU {\n");
	int fooIndex = 0;
	while (fooIndex < 2000) { // add 2000 methods (so that source is close to 1MB)
		bigContents.append("public void foo");
		bigContents.append(fooIndex++);
		bigContents.append(method);
	}
	// don't add closing } for class def so as to have a syntax error

	ICompilationUnit workingCopy = null;
	try {
		// Setup
		workingCopy = (ICompilationUnit) JavaCore.create(ResourcesPlugin.getWorkspace().getRoot().getFile(new Path("/BigProject/src/org/eclipse/jdt/core/tests/BigCu.java")));
		workingCopy.becomeWorkingCopy(null);

		// Warm up
		int warmup = WARMUP_COUNT / 10;
		for (int i=0; i<warmup; i++) {
			workingCopy.getBuffer().setContents(bigContents.append("a").toString());
			workingCopy.reconcile(JLS3_INTERNAL, false/*no pb detection*/, null/*no owner*/, null/*no progress*/);
		}

		// Measures
		resetCounters();
		for (int i=0; i<MEASURES_COUNT; i++) {
			workingCopy.getBuffer().setContents(bigContents.append("a").toString());
			runGc();
			startMeasuring();
			workingCopy.reconcile(JLS3_INTERNAL, false/*no pb detection*/, null/*no owner*/, null/*no progress*/);
			stopMeasuring();
		}

		// Commit
		commitMeasurements();
		assertPerformance();

	} finally {
		if (workingCopy != null)
			workingCopy.discardWorkingCopy();
	}
}

/*
 * Ensures that the performance of reconcile on a CU with lots of duplicates is acceptable.
 * (regression test for bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=135906 )
 */
public void testReconcileDuplicates() throws JavaModelException {
	tagAsSummary("Reconcile editor change on file with lots of duplicates", false); // do NOT put in fingerprint

	// build big file contents
	StringBuilder contents = new StringBuilder();
	contents.append("public class CUWithDuplicates {\n");
	int fooIndex = 0;
	while (fooIndex < 2000) { // add 2000 duplicate methods
		contents.append("  void foo() {}\n");
		contents.append(fooIndex++);
	}
	contents.append("} //"); // ensure it ends with a line comment that is edited below

	ICompilationUnit workingCopy = null;
	try {
		// Setup
		workingCopy = (ICompilationUnit) JavaCore.create(ResourcesPlugin.getWorkspace().getRoot().getFile(new Path("/BigProject/src/CUWithDuplicates.java")));
		workingCopy.becomeWorkingCopy(null);

		// Warm up
		int warmup = WARMUP_COUNT / 10;
		for (int i=0; i<warmup; i++) {
			workingCopy.getBuffer().setContents(contents.append('a').toString());
			workingCopy.reconcile(JLS3_INTERNAL, false/*no pb detection*/, null/*no owner*/, null/*no progress*/);
		}

		// Measures
		resetCounters();
		for (int i=0; i<MEASURES_COUNT; i++) {
			workingCopy.getBuffer().setContents(contents.append('a').toString());
			runGc();
			startMeasuring();
			workingCopy.reconcile(JLS3_INTERNAL, false/*no pb detection*/, null/*no owner*/, null/*no progress*/);
			stopMeasuring();
		}

		// Commit
		commitMeasurements();
		assertPerformance();

	} finally {
		if (workingCopy != null)
			workingCopy.discardWorkingCopy();
	}
}

/*
 * Ensures that the performance of reconcile after deleting lots of members is acceptable
 * (regression test for bug 443928 Building large Java element deltas is really slow)
 */
public void testPerfDeleteLotsOfMembersAndReconcile() throws JavaModelException {
	tagAsSummary("Reconcile editor change after deleting lots of members", false); // do NOT put in fingerprint

	// build big file contents
	StringBuilder contents = new StringBuilder();
	contents.append("public class LotsOfMembers {\n");
	int fooIndex = 0;
	while (fooIndex < 15000) { // add 15000 methods
		contents.append("  void foo");
		contents.append(fooIndex++);
		contents.append("() {}\n");
	}
	contents.append("}");

	String oldContents = contents.toString(); // initially, 15000 members
	String newContents = "public class LotsOfMembers {\n}"; // then, no members

	ICompilationUnit workingCopy = null;
	try {
		// Setup
		workingCopy = (ICompilationUnit) JavaCore.create(ResourcesPlugin.getWorkspace().getRoot().getFile(new Path("/BigProject/src/LotsOfMembers.java")));
		workingCopy.becomeWorkingCopy(null);

		// Warm up
		int warmup = WARMUP_COUNT / 10;
		for (int i=0; i<warmup; i++) {
			workingCopy.getBuffer().setContents(oldContents);
			workingCopy.reconcile(JLS3_INTERNAL, false/*no pb detection*/, null/*no owner*/, null/*no progress*/);
			workingCopy.getBuffer().setContents(newContents);
			workingCopy.reconcile(JLS3_INTERNAL, false/*no pb detection*/, null/*no owner*/, null/*no progress*/);
		}

		// Measures
		resetCounters();
		for (int i=0; i<MEASURES_COUNT; i++) {
			workingCopy.getBuffer().setContents(oldContents);
			workingCopy.reconcile(JLS3_INTERNAL, false/*no pb detection*/, null/*no owner*/, null/*no progress*/);
			workingCopy.getBuffer().setContents(newContents);
			runGc();
			startMeasuring();
			workingCopy.reconcile(JLS3_INTERNAL, false/*no pb detection*/, null/*no owner*/, null/*no progress*/);
			stopMeasuring();
		}

		// Commit
		commitMeasurements();
		assertPerformance();

	} finally {
		if (workingCopy != null)
			workingCopy.discardWorkingCopy();
	}
}

/*
 * Ensures that the performance of reconcile after creating a package fragment in a batch operation is acceptable
 * (regression test for bug 234718 JarPackageFragmentRoot.computeChildren(..) is slow )
 */
public void testPerfBatchCreatePackageAndReconcile() throws CoreException {
	tagAsSummary("Reconcile editor change after creating a package fragment in a batch operation", false); // do NOT put in fingerprint

	IJavaProject project = null;
	try {
		project = createJavaProject("P234718");
		IFile bigJar1 = BIG_PROJECT.getProject().getFile(BIG_JAR1_NAME);
		IFile bigJar2 = BIG_PROJECT.getProject().getFile(BIG_JAR2_NAME);
		project.setRawClasspath(
			new IClasspathEntry[] {
				JavaCore.newSourceEntry(project.getPath()),
				JavaCore.newLibraryEntry(bigJar1.getFullPath(), null, null),
				JavaCore.newLibraryEntry(bigJar2.getFullPath(), null, null),
			}, null);
		final IPackageFragmentRoot root = project.getPackageFragmentRoot(project.getProject());
		ICompilationUnit workingCopy  = root.getPackageFragment("").createCompilationUnit(
			"X.java",
			"public class  {\n" +
			"}"
			, false, null);
		workingCopy.becomeWorkingCopy(null);
		waitUntilIndexesReady();
		waitForAutoBuild();

		// Warm up
		try {
			final ICompilationUnit copy = workingCopy;
			IWorkspaceRunnable runnable = new IWorkspaceRunnable(){
				public void run(IProgressMonitor monitor) throws CoreException {
					root.createPackageFragment("p2", false/*don't force*/, monitor);
					copy.reconcile(JLS3_INTERNAL, true, null, monitor);
					int warmup = WARMUP_COUNT / 5;
					for (int i=0; i<warmup; i++) {
						copy.reconcile(JLS3_INTERNAL, true, null, monitor);
					}
				}
			};
			try {
				JavaCore.run(runnable, null);
			} finally {
				root.getPackageFragment("p2").delete(false/*don't force*/, null);
			}

			// Measures
			resetCounters();
			runnable = new IWorkspaceRunnable(){
				public void run(IProgressMonitor monitor) throws CoreException {
					root.createPackageFragment("p2", false/*don't force*/, monitor);
					copy.reconcile(JLS3_INTERNAL, true, null, monitor);
					int iterations = 10;
					startMeasuring();
					for (int n=0; n<iterations; n++) {
						copy.reconcile(JLS3_INTERNAL, true, null, monitor);
					}
					stopMeasuring();
				}
			};
			for (int i=0; i<MEASURES_COUNT; i++) {
				runGc();
				try {
					JavaCore.run(runnable, null);
				} finally {
					root.getPackageFragment("p2").delete(false/*don't force*/, null);
				}
			}
		}
		finally {
			workingCopy.discardWorkingCopy();
		}

		// Commit
		commitMeasurements();
		assertPerformance();

	} finally {
		if (project != null)
			project.getProject().delete(true, null);
	}
}

/**
 * Ensures that the reconciler does nothing when the source
 * to reconcile with is the same as the current contents.
 */
public void testPerfSearchAllTypeNamesAndReconcile() throws CoreException {
	tagAsSummary("Reconcile editor change and complete", true); // put in fingerprint

	// Wait for indexing end
	waitUntilIndexesReady();

	// Warm up
	ICompilationUnit workingCopy = null;
	try {
		final ProblemRequestor requestor = new ProblemRequestor();
		WorkingCopyOwner owner = new WorkingCopyOwner() {
			public IProblemRequestor getProblemRequestor(ICompilationUnit cu) {
				return requestor;
            }
		};
		workingCopy = PARSER_WORKING_COPY.getWorkingCopy(owner, null);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { JDT_CORE_PROJECT });
		int warmup = WARMUP_COUNT / 5;
		for (int i=0; i<warmup; i++) {
			searchAllTypeNames(scope);
			CompilationUnit unit = workingCopy.reconcile(JLS3_INTERNAL, true, null, null);
			if (i == 0) {
				assertNotNull("Compilation Unit should not be null!", unit);
				assertNotNull("Bindings were not resolved!", unit.getPackage().resolveBinding());
			}
		}

		// Measures
		int iterations = 2;
		resetCounters();
		for (int i=0; i<MEASURES_COUNT; i++) {
			runGc();
			startMeasuring();
			for (int n=0; n<iterations; n++) {
				searchAllTypeNames(scope);
				workingCopy.reconcile(JLS3_INTERNAL, true, null, null);
			}
			stopMeasuring();
		}
	}
	finally {
		workingCopy.discardWorkingCopy();
	}

	// Commit
	commitMeasurements();
	assertPerformance();

}

/*
 * Performance test for the opening of class files in 2 big jars (that each would fill the Java model cache if all pkgs were opened).
 * (see bug 190094 Java Outline Causes Eclipse Lock-up.)
 */
public void testPopulateTwoBigJars() throws CoreException {

	IJavaProject project = null;
	try {
		project = createJavaProject("HugeJarProject");
		IFile bigJar1 = BIG_PROJECT.getProject().getFile(BIG_JAR1_NAME);
		IFile bigJar2 = BIG_PROJECT.getProject().getFile(BIG_JAR2_NAME);
		project.setRawClasspath(
			new IClasspathEntry[] {
				JavaCore.newLibraryEntry(bigJar1.getFullPath(), null, null),
				JavaCore.newLibraryEntry(bigJar2.getFullPath(), null, null),
			}, null);
		waitUntilIndexesReady();
		waitForAutoBuild();
		IPackageFragmentRoot root1 = project.getPackageFragmentRoot(bigJar1);
		IPackageFragmentRoot root2 = project.getPackageFragmentRoot(bigJar2);

		// warm up
		int max = 20;
		int warmup = WARMUP_COUNT / 10;
		for (int i = 0; i < warmup; i++) {
			project.close();
			for (int j = 0; j < max; j++) {
				root1.getPackageFragment("p" + j).open(null);
				root2.getPackageFragment("q" + j).open(null);
			}
		}

		// measure performance
		for (int i = 0; i < MEASURES_COUNT; i++) {
			project.close();
			runGc();
			startMeasuring();
			for (int j = 0; j < max; j++) {
				root1.getPackageFragment("p" + j).open(null);
				root2.getPackageFragment("q" + j).open(null);
			}
			stopMeasuring();
		}

		commitMeasurements();
		assertPerformance();
	} finally {
		if (project != null)
			project.getProject().delete(false, null);
	}
}

/*
 * Performance test for looking up package fragments
 * (see bug 72683 Slow code assist in Display view)
 */
public void testSeekPackageFragments() throws CoreException {
	assertNotNull("We should have the 'BigProject' in workspace!", BIG_PROJECT);
	class PackageRequestor implements IJavaElementRequestor {
		ArrayList pkgs = new ArrayList();
		public void acceptField(IField field) {}
		public void acceptInitializer(IInitializer initializer) {}
		public void acceptMemberType(IType type) {}
		public void acceptMethod(IMethod method) {}
		public void acceptPackageFragment(IPackageFragment packageFragment) {
			if (this.pkgs != null)
				this.pkgs.add(packageFragment);
		}
		public void acceptType(IType type) {}
		public boolean isCanceled() {
			return false;
		}
		@Override
		public void acceptModule(IModuleDescription module) {}
	}

	// first pass: ensure all class are loaded, and ensure that the test works as expected
	PackageRequestor requestor = new PackageRequestor();
	for (int i=0; i<WARMUP_COUNT; i++) {
		getNameLookup(BIG_PROJECT).seekPackageFragments("org.eclipse.jdt.core.tests78.performance5", false/*not partial match*/, requestor);
		if (i == 0) {
			int size = requestor.pkgs.size();
			IJavaElement[] result = new IJavaElement[size];
			requestor.pkgs.toArray(result);
			assertElementsEqual(
				"Unexpected packages",
				"org.eclipse.jdt.core.tests78.performance5 [in src78 [in "+BIG_PROJECT_NAME+"]]",
				result
			);
		}
	}

	// measure performance
	requestor.pkgs = null;
	resetCounters();
	for (int i = 0; i < MEASURES_COUNT; i++) {
		runGc();
		startMeasuring();
		for (int j = 0; j < 50000; j++) {
			getNameLookup(BIG_PROJECT).seekPackageFragments("org.eclipse.jdt.core.tests" + i + "0.performance" + i, false/*not partial match*/, requestor);
		}
		stopMeasuring();
	}
	commitMeasurements();
	assertPerformance();
}

public void testCloseProjects() throws JavaModelException {
	tagAsSummary("Close all workspace projects", false); // do NOT put in fingerprint

	// Warm-up
	int length=ALL_PROJECTS.length;
	int wmax = WARMUP_COUNT / 10;
	for (int i=0; i<wmax; i++) {
		for (int j=0; j<length; j++) {
			ENV.closeProject(ALL_PROJECTS[j].getPath());
		}
		for (int j=0; j<length; j++) {
			ENV.openProject(ALL_PROJECTS[j].getPath());
		}
	}

	// Measures
	for (int i=0; i<MEASURES_COUNT; i++) {
		waitUntilIndexesReady();
		runGc();
		startMeasuring();
		for (int j=0; j<length; j++) {
			ENV.closeProject(ALL_PROJECTS[j].getPath());
		}
		stopMeasuring();
		for (int j=0; j<length; j++) {
			ENV.openProject(ALL_PROJECTS[j].getPath());
		}
	}

	// Commit
	commitMeasurements();
	assertPerformance();
}

/*
 * Tests the performance of JavaCore#create(IResource).
 * (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=133141)
 */
public void testCreateJavaElement() throws CoreException {
	// setup (force the project cache to be created)
	IFile file = (IFile) WORKING_COPY.getResource();
	getNameLookup(BIG_PROJECT);

	// warm up
	int warmup = WARMUP_COUNT / 10;
	int iterations = 5000;
	for (int i = 0; i < warmup; i++) {
		for (int j = 0; j < iterations; j++) {
			JavaCore.create(file);
		}
	}

	// measure performance
	for (int i = 0; i < MEASURES_COUNT; i++) {
		runGc();
		startMeasuring();
		for (int j = 0; j < iterations; j++) {
			JavaCore.create(file);
		}
		stopMeasuring();
	}

	commitMeasurements();
	assertPerformance();
}

public void testInitJDTPlugin() throws JavaModelException, CoreException {
	tagAsSummary("JDT/Core plugin initialization", true); // put in fingerprint
	setComment(Performance.EXPLAINS_DEGRADATION_COMMENT, "Bug 338649:Extra check for source attachment in missing drive causing regression");
	// Warm-up
	int wmax = WARMUP_COUNT / 5;
	for (int i=0; i<wmax; i++) {
		simulateExitRestart();
		JavaCore.initializeAfterLoad(null);
		waitUntilIndexesReady();
	}

	// Measures
	for (int i=0; i<MEASURES_COUNT; i++) {
		// shutdwon
		simulateExit();
		runGc();
		startMeasuring();
		// restart
		simulateRestart();
		JavaCore.initializeAfterLoad(null);
		waitUntilIndexesReady();
		stopMeasuring();
	}
	// Commit
	commitMeasurements();
	assertPerformance();
}

/*
 * Performance test for the first use of findType(...)
 * (see bug 161175 JarPackageFragmentRoot slow to initialize)
 */
public void testFindType() throws CoreException {

	IJavaModel model = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot());
	IJavaProject[] existingProjects = model.getJavaProjects();

	try {
		// close existing projects
		for (int i = 0, length = existingProjects.length; i < length; i++) {
			existingProjects[i].getProject().close(null);
		}

		// get 20 projects
		int max = 20;
		IJavaProject[] projects = new IJavaProject[max];
		for (int i = 0; i < max; i++) {
			projects[i] = createJavaProject("FindType" + i);
		}
		waitUntilIndexesReady();
		waitForAutoBuild();

		try {
			// warm up
			int warmup = WARMUP_COUNT / 10;
			for (int i = 0; i < warmup; i++) {
				model.close();
				for (int j = 0; j < max; j++) {
					projects[j].findType("java.lang.Object");
				}
			}

			// measure performance
			for (int i = 0; i < MEASURES_COUNT; i++) {
				model.close();
				runGc();
				startMeasuring();
				for (int j = 0; j < max; j++) {
					projects[j].findType("java.lang.Object");
				}
				stopMeasuring();
			}

			commitMeasurements();
			assertPerformance();
		} finally {
			for (int i = 0; i < max; i++) {
				projects[i].getProject().delete(false, null);
			}
		}
	} finally {
		// reopen existing projects
		for (int i = 0, length = existingProjects.length; i < length; i++) {
			existingProjects[i].getProject().open(null);
		}
	}
}

/*
 * Performance test for the first time we get the source of a class file in a bug jar with no source attached
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=190840 )
 */
public void testGetSourceBigJarNoAttachment() throws CoreException {

	IJavaProject project = null;
	try {
		project = createJavaProject("HugeJarProject");
		IFile bigJar1 = BIG_PROJECT.getProject().getFile(BIG_JAR1_NAME);
		project.setRawClasspath(
			new IClasspathEntry[] {
				JavaCore.newLibraryEntry(bigJar1.getFullPath(), null, null),
			}, null);
		waitUntilIndexesReady();
		waitForAutoBuild();
		IPackageFragmentRoot root = project.getPackageFragmentRoot(bigJar1);
		IClassFile classFile = root.getPackageFragment("p0").getClassFile("X0.class");

		// warm up
		int max = 20;
		int warmup = WARMUP_COUNT / 10;
		for (int i = 0; i < warmup; i++) {
			for (int j = 0; j < max; j++) {
				root.close();
				classFile.getSource();
			}
		}

		// measure performance
		for (int i = 0; i < MEASURES_COUNT; i++) {
			runGc();
			startMeasuring();
			for (int j = 0; j < max; j++) {
				root.close();
				classFile.getSource();
			}
			stopMeasuring();
		}

		commitMeasurements();
		assertPerformance();
	} finally {
		if (project != null)
			project.getProject().delete(false, null);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=331632
public void testReopenSingleProject() throws CoreException {
	tagAsSummary("Reopen a single project in a workspace", false); // do NOT put in fingerprint

	// First close all Eclipse projects
	long startTime = 0;
	if (PRINT) {
		System.out.print("Close all Eclipse projects...");
		startTime = System.currentTimeMillis();
	}
	int length=ALL_PROJECTS.length;
	for (int j=0; j<length; j++) {
		ALL_PROJECTS[j].getProject().close(null);
	}
	waitUntilIndexesReady();
	waitForAutoBuild();
	if (PRINT) {
		System.out.println((System.currentTimeMillis()-startTime)+"ms");
	}


	// Warm-up
	if (PRINT) {
		System.out.print("Warmup test...");
		startTime = System.currentTimeMillis();
	}
	final int warmup = WARMUP_COUNT / 10;
	for (int i=0; i<warmup; i++) {
		BIG_PROJECT.getProject().close(null);
		waitUntilIndexesReady();
		waitForAutoBuild();
		BIG_PROJECT.getProject().open(null);
		waitUntilIndexesReady();
		waitForAutoBuild();
		waitForManualRefresh();
	}
	if (PRINT) {
		System.out.println((System.currentTimeMillis()-startTime)+"ms");
	}

	// Measures
	if (PRINT) {
		System.out.println();
		System.out.println("Start measures:");
		startTime = System.currentTimeMillis();
	}
	for (int i=0; i<MEASURES_COUNT; i++) {
		runGc();
		BIG_PROJECT.getProject().close(null);
		waitUntilIndexesReady();
		waitForAutoBuild();
		startMeasuring();
		BIG_PROJECT.getProject().open(null);
		waitUntilIndexesReady();
		waitForAutoBuild();
		waitForManualRefresh();
		stopMeasuring();
	}
	if (PRINT) {
		System.out.println("	total time: "+((System.currentTimeMillis()-startTime)/1000.0)+"s");
	}

	// Commit
	if (PRINT) {
		System.out.println();
		System.out.println("Commit measures:");
		startTime = System.currentTimeMillis();
	}
	commitMeasurements();
	assertPerformance();

	// Finally reopen all Eclipse projects
	if (PRINT) {
		System.out.print("Reopen Eclipse projects...");
		startTime = System.currentTimeMillis();
	}
	for (int i=0; i<length; i++) {
		ALL_PROJECTS[i].getProject().open(null);
	}
	waitUntilIndexesReady();
	waitForAutoBuild();
	waitForManualRefresh();
	runGc();
	if (PRINT) {
		System.out.println((System.currentTimeMillis()-startTime)+"ms");
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=354332
public void testRefreshExternalArchives() throws Exception {
	int jarCount = 100;
	File[] files = new File[jarCount];
	IJavaModel model = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot());
	IClasspathEntry[] oldClasspath = BIG_PROJECT.getRawClasspath();
	try {
		IClasspathEntry[] classpath = new IClasspathEntry[jarCount];
		for (int index = 0; index < jarCount; index++) {
			String filePath = getExternalResourcePath("lib"+ index +".jar");
			org.eclipse.jdt.core.tests.util.Util.createJar(new String[0],
				new String[] {
					"META-INF/MANIFEST.MF",
					"Manifest-Version: 1.0\n",
				},
				filePath,
				JavaCore.VERSION_1_4);
			classpath[index] = JavaCore.newLibraryEntry(new Path(filePath), null, null);
			files[index]  = new File(filePath);
		}
		BIG_PROJECT.setRawClasspath(classpath, null);

		// warm up
		int max = 20;
		int warmup = WARMUP_COUNT / 10;
		for (int i = 0; i < warmup; i++) {
			for (int j = 0; j < max; j++) {
				touchFiles(files);
				model.refreshExternalArchives(new IJavaElement[] {BIG_PROJECT}, null);
			}
		}

		// measure performance
		for (int i = 0; i < MEASURES_COUNT; i++) {
			runGc();
			startMeasuring();
			for (int j = 0; j < max; j++) {
				touchFiles(files);
				model.refreshExternalArchives(new IJavaElement[] {BIG_PROJECT}, null);
			}
			stopMeasuring();
		}

		commitMeasurements();
		assertPerformance();

	} finally {
		BIG_PROJECT.setRawClasspath(oldClasspath, null);
		for(int index=0; index < files.length; index++) {
			files[index].delete();
		}
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=411423
public void testResolveClasspath() throws Exception {
	int jarCount = 100;
	File[] libraryFiles = new File[jarCount];
	File[] srcAttachmentFiles = new File[jarCount];
	IClasspathEntry[] oldClasspath = BIG_PROJECT.getRawClasspath();
	try {
		IClasspathEntry[] classpath = new IClasspathEntry[jarCount];
		for (int index = 0; index < jarCount; index++) {
			String libraryFilePath = getExternalResourcePath("lib" + index + ".jar");
			org.eclipse.jdt.core.tests.util.Util.createJar(new String[0],
					new String[] {
						"META-INF/MANIFEST.MF",
						"Manifest-Version: 1.0\n",
					},
					libraryFilePath,
					JavaCore.VERSION_1_4);
			libraryFiles[index] = new File(libraryFilePath);

			String srcAttachmentFilePath = getExternalResourcePath("lib" + index + "-src.jar");
			org.eclipse.jdt.core.tests.util.Util.createJar(new String[0],
					new String[] {
						"META-INF/MANIFEST.MF",
						"Manifest-Version: 1.0\n",
					},
					srcAttachmentFilePath,
					JavaCore.VERSION_1_4);
			srcAttachmentFiles[index] = new File(srcAttachmentFilePath);

			classpath[index] = JavaCore.newLibraryEntry(new Path(libraryFilePath), new Path(srcAttachmentFilePath), null);
		}
		BIG_PROJECT.setRawClasspath(classpath, null);

		// warm up
		int max = 20;
		int warmup = WARMUP_COUNT / 10;
		for (int i = 0; i < warmup; i++) {
			for (int j = 0; j < max; j++) {
				BIG_PROJECT.resolveClasspath(classpath);
			}
		}

		// measure performance
		for (int i = 0; i < MEASURES_COUNT; i++) {
			runGc();
			startMeasuring();
			for (int j = 0; j < max; j++) {
				BIG_PROJECT.resolveClasspath(classpath);
			}
			stopMeasuring();
		}

		commitMeasurements();
		assertPerformance();

	} finally {
		BIG_PROJECT.setRawClasspath(oldClasspath, null);
		for (int index = 0; index < libraryFiles.length; index++) {
			libraryFiles[index].delete();
			srcAttachmentFiles[index].delete();
		}
	}
}

/*
 * Overriding getExternalPath() to be on a non-local disk (e.g., NFS) shows the advantages
 * of caching file existence checks in the testJavaModelManagerExternalFilesCache() test.
 */
@Override
protected String getExternalPath() {
	// NOTE: Do something similar to this commented-out code to set up the tests to
	// use a non-local file system.
	//	return "/home/" + System.getProperty("user.name") + "/performance_test/";
	return super.getExternalPath();
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=421165
public void testGetAllPackageFragmentRoots() throws Exception {
	int jarCount = 100;
	IClasspathEntry[] oldClasspath = BIG_PROJECT.getRawClasspath();
	try {
	    IClasspathEntry[] classpath = new IClasspathEntry[jarCount];
	    for (int index = 0; index < jarCount; index++) {
	        String filePath = getExternalResourcePath("lib"+ index +".jar");
	        org.eclipse.jdt.core.tests.util.Util.createJar(new String[0],
	            new String[] {
	                "META-INF/MANIFEST.MF",
	                "Manifest-Version: 1.0\n",
	            },
	            filePath,
	            JavaCore.VERSION_1_4);
	        classpath[index] = JavaCore.newLibraryEntry(new Path(filePath), null, null);
	    }
	    BIG_PROJECT.setRawClasspath(classpath, null);
	    IFile file = (IFile) WORKING_COPY.getResource();

	    // warm up
	    int max = 20;
	    int warmup = WARMUP_COUNT / 10;
	    for (int i = 0; i < warmup; i++) {
	        for (int j = 0; j < max; j++) {
	            file.touch(null/*no progress*/);
	            BIG_PROJECT.getAllPackageFragmentRoots();
	        }
	    }

	    // measure performance
	    for (int i = 0; i < MEASURES_COUNT; i++) {
	        runGc();
	        startMeasuring();
	        for (int j = 0; j < max; j++) {
	          file.touch(null/*no progress*/);
	          BIG_PROJECT.getAllPackageFragmentRoots();
	        }
	        stopMeasuring();
	    }

	    commitMeasurements();
	    assertPerformance();

	} finally {
	    BIG_PROJECT.setRawClasspath(oldClasspath, null);
	}
}

protected void resetCounters() {
	// do nothing
}
}
