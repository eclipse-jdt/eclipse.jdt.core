/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.performance;

import java.io.ByteArrayInputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import junit.framework.*;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.core.tests.model.AbstractJavaModelTests.ProblemRequestor;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.*;

/**
 */
public class FullSourceWorkspaceModelTests extends FullSourceWorkspaceTests implements IJavaSearchConstants {

	// Tests counters
	static int TESTS_COUNT = 0;
	private final static int WARMUP_COUNT = 1; // 30;
	private final static int ITERATIONS_COUNT = 30;
	private final static int FOLDERS_COUNT = 200;
	private final static int PACKAGES_COUNT = 200;
	static int TESTS_LENGTH;

	// Log file streams
	private static PrintStream[] LOG_STREAMS = new PrintStream[LOG_TYPES.length];

	// Search variables
	IJavaSearchScope scope;
	protected JavaSearchResultCollector resultCollector;
	
	// Type path
	static IPath BIG_PROJECT_TYPE_PATH;
	static ICompilationUnit WORKING_COPY;

/**
 * @param name
 */
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

protected void setUp() throws Exception {
	super.setUp();
	this.resultCollector = new JavaSearchResultCollector();
	this.scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { JDT_CORE_PROJECT });
	if (BIG_PROJECT == null) {
		setUpBigProject();
	} else if (BIG_PROJECT_TYPE_PATH == null) {
		setUpBigProjectInfo();
	}
}
private void setUpBigProject() throws CoreException {
	try {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot workspaceRoot = workspace.getRoot();
		long start = System.currentTimeMillis();

		// Print for log in case of project creation troubles...
		System.out.println("Create project "+BIG_PROJECT_NAME+" in "+workspaceRoot.getLocation()+":");
		System.out.println("	- create "+FOLDERS_COUNT+" folders and "+PACKAGES_COUNT+" packages...");

		// setup projects with several source folders and several packages per source folder
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

		// Print for log in case of project creation troubles...
		System.out.println("		=> global time = "+(System.currentTimeMillis()-start)/1000.0+" seconds)");
		start = System.currentTimeMillis();
		System.out.print("	- add project to full source workspace...");

		// Add project to workspace
		ENV.addProject(BIG_PROJECT_NAME);
		BIG_PROJECT = (JavaProject) createJavaProject(BIG_PROJECT_NAME, sourceFolders, "bin", "1.4");
		BIG_PROJECT.setRawClasspath(BIG_PROJECT.getRawClasspath(), null);

		// Print for log in case of project creation troubles...
		System.out.println("("+(System.currentTimeMillis()-start)+"ms)");
		start = System.currentTimeMillis();
		System.out.print("	- Create compilation unit with secondary type...");

		// Add CU with secondary type
		BIG_PROJECT_TYPE_PATH = new Path("/BigProject/src" + (FOLDERS_COUNT-1) + "/org/eclipse/jdt/core/tests" + (FOLDERS_COUNT-1) + "/performance" + (PACKAGES_COUNT-1) + "/TestBigProject.java");
		IFile file = workspaceRoot.getFile(BIG_PROJECT_TYPE_PATH);
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
		WORKING_COPY = (ICompilationUnit)JavaCore.create(file);
		System.out.println("("+(System.currentTimeMillis()-start)+"ms)");
	} finally {
		// do not delete project
	}
	
}
private void setUpBigProjectInfo() {
	// Set up type path
	BIG_PROJECT_TYPE_PATH = new Path("/BigProject/src" + (FOLDERS_COUNT-1) + "/org/eclipse/jdt/core/tests" + (FOLDERS_COUNT-1) + "/performance" + (PACKAGES_COUNT-1) + "/TestBigProject.java");

	// Set up working copy
	IWorkspace workspace = ResourcesPlugin.getWorkspace();
	IWorkspaceRoot workspaceRoot = workspace.getRoot();
	IFile file = workspaceRoot.getFile(BIG_PROJECT_TYPE_PATH);
	WORKING_COPY = (ICompilationUnit)JavaCore.create(file);
}
/* (non-Javadoc)
 * @see junit.framework.TestCase#tearDown()
 */
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

protected void search(String patternString, int searchFor, int limitTo) throws CoreException {
	int matchMode = patternString.indexOf('*') != -1 || patternString.indexOf('?') != -1
		? SearchPattern.R_PATTERN_MATCH
		: SearchPattern.R_EXACT_MATCH;
	SearchPattern pattern = SearchPattern.createPattern(
		patternString, 
		searchFor,
		limitTo, 
		matchMode | SearchPattern.R_CASE_SENSITIVE);
	new SearchEngine().search(
		pattern,
		new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
		this.scope,
		this.resultCollector,
		null);
}

protected void searchAllTypeNames() throws CoreException {
	class TypeNameCounter extends TypeNameRequestor {
		int count = 0;
		public void acceptType(int modifiers, char[] packageName, char[] simpleTypeName, char[][] enclosingTypeNames, String path) {
			count++;
		}
	}
	TypeNameCounter requestor = new TypeNameCounter();
	new SearchEngine().searchAllTypeNames(
		null,
		null,
		SearchPattern.R_PREFIX_MATCH, // not case sensitive
		IJavaSearchConstants.TYPE,
		this.scope,
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
	StringBuffer buffer = new StringBuffer();
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

/**
 * @see org.eclipse.jdt.core.tests.model.AbstractJavaModelTests#createJavaProject(String, String[], String[], String[][], String[][], String[], String[][], String[][], boolean[], String, String[], String[][], String[][], String)
 */
protected IJavaProject createJavaProject(final String projectName, final String[] sourceFolders, final String projectOutput, final String compliance) throws CoreException {
	final IJavaProject[] result = new IJavaProject[1];
	IWorkspaceRunnable create = new IWorkspaceRunnable() {
		public void run(IProgressMonitor monitor) throws CoreException {
			
			// create classpath entries 
			IProject project = ENV.getProject(projectName);
			IPath projectPath = project.getFullPath();
			int sourceLength = sourceFolders == null ? 0 : sourceFolders.length;
			IClasspathEntry[] entries = new IClasspathEntry[sourceLength];
			for (int i= 0; i < sourceLength; i++) {
				IPath sourcePath = new Path(sourceFolders[i]);
				int segmentCount = sourcePath.segmentCount();
				if (segmentCount > 0) {
					// create folder and its parents
					IContainer container = project;
					for (int j = 0; j < segmentCount; j++) {
						IFolder folder = container.getFolder(new Path(sourcePath.segment(j)));
						if (!folder.exists()) {
							folder.create(true, true, null);
						}
						container = folder;
					}
				}
				// create source entry
				entries[i] = 
					JavaCore.newSourceEntry(
						projectPath.append(sourcePath), 
						new IPath[0],
						new IPath[0], 
						null
					);
			}
			
			// create project's output folder
			IPath outputPath = new Path(projectOutput);
			if (outputPath.segmentCount() > 0) {
				IFolder output = project.getFolder(outputPath);
				if (!output.exists()) {
					output.create(true, true, null);
				}
			}
			
			// set classpath and output location
			IJavaProject javaProject = ENV.getJavaProject(projectName);
			javaProject.setRawClasspath(entries, projectPath.append(outputPath), null);
			
			// set compliance level options
			if ("1.5".equals(compliance)) {
				Map options = new HashMap();
				options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
				options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);	
				options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);	
				javaProject.setOptions(options);
			}
			
			result[0] = javaProject;
		}
	};
	ResourcesPlugin.getWorkspace().run(create, null);	
	return result[0];
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
public void testPerfNameLookupFindKnownType() throws CoreException {
	tagAsSummary("Model>NameLookup>Find>KnownType", false); // do NOT put in fingerprint

	// Wait for indexing end
	waitUntilIndexesReady();

	// Warm up
	String fullQualifiedName = BIG_PROJECT_TYPE_PATH.removeFileExtension().removeFirstSegments(2).toString();
	fullQualifiedName = fullQualifiedName.replace('/', '.');
	if (WARMUP_COUNT > 0) {
		for (int i=0; i<WARMUP_COUNT; i++) {
			NameLookup nameLookup = BIG_PROJECT.newNameLookup(DefaultWorkingCopyOwner.PRIMARY);
			IType type = nameLookup.findType(fullQualifiedName, false /*full match*/, NameLookup.ACCEPT_ALL);
			assertNotNull("We should find type '"+fullQualifiedName+"' in project "+BIG_PROJECT_NAME, type);
		}
	}

	// Measures
	resetCounters();
	for (int i=0; i<MEASURES_COUNT; i++) {
		runGc();
		startMeasuring();
		for (int n=0; n<ITERATIONS_COUNT; n++) {
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
public void testPerfNameLookupFindKnownSecondaryType() throws CoreException {
	tagAsSummary("Model>NameLookup>Find>SecondaryType", false); // do NOT put in fingerprint

	// Wait for indexing end
	waitUntilIndexesReady();

	// Warm up
	String fullQualifiedName = BIG_PROJECT_TYPE_PATH.removeFileExtension().removeFirstSegments(2).removeLastSegments(1).toString();
	fullQualifiedName = fullQualifiedName.replace('/', '.')+".TestSecondary";
	if (WARMUP_COUNT > 0) {
		for (int i=0; i<WARMUP_COUNT; i++) {
			NameLookup nameLookup = BIG_PROJECT.newNameLookup(DefaultWorkingCopyOwner.PRIMARY);
			IType type = nameLookup.findType(fullQualifiedName, false /*full match*/, NameLookup.ACCEPT_ALL);
			if (LOG_VERSION.compareTo("v_623") > 0) {
				assertNotNull("We should find type '"+fullQualifiedName+"' in project "+BIG_PROJECT_NAME, type);
			}
		}
	}

	// Measures
	resetCounters();
	for (int i=0; i<MEASURES_COUNT; i++) {
		runGc();
		startMeasuring();
		for (int n=0; n<ITERATIONS_COUNT; n++) {
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
public void testPerfNameLookupFindUnknownType() throws CoreException {
	tagAsSummary("Model>NameLookup>Find>UnknownType", false); // do NOT put in fingerprint

	// Wait for indexing end
	waitUntilIndexesReady();

	// Warm up
	String fullQualifiedName = BIG_PROJECT_TYPE_PATH.removeFileExtension().removeFirstSegments(2).removeLastSegments(1).toString();
	fullQualifiedName = fullQualifiedName.replace('/', '.')+".Unknown";
	if (WARMUP_COUNT > 0) {
		for (int i=0; i<WARMUP_COUNT; i++) {
			NameLookup nameLookup = BIG_PROJECT.newNameLookup(DefaultWorkingCopyOwner.PRIMARY);
			IType type = nameLookup.findType(fullQualifiedName, false /*full match*/, NameLookup.ACCEPT_ALL);
			assertNull("We should not find an unknown type in project "+BIG_PROJECT_NAME, type);
		}
	}

	// Measures
	resetCounters();
	for (int i=0; i<MEASURES_COUNT; i++) {
		runGc();
		startMeasuring();
		for (int n=0; n<ITERATIONS_COUNT; n++) {
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
public void testPerfProjectFindKnownType() throws CoreException {
	tagAsSummary("Model>Project>Find>KnownType", false); // do NOT put in fingerprint

	// Wait for indexing end
	waitUntilIndexesReady();

	// Warm up
	String fullQualifiedName = BIG_PROJECT_TYPE_PATH.removeFileExtension().removeFirstSegments(2).toString();
	fullQualifiedName = fullQualifiedName.replace('/', '.');
	if (WARMUP_COUNT > 0) {
		for (int i=0; i<WARMUP_COUNT; i++) {
			IType type = BIG_PROJECT.findType(fullQualifiedName);
			assertNotNull("We should find type '"+fullQualifiedName+"' in project "+BIG_PROJECT_NAME, type);
		}
	}

	// Measures
	resetCounters();
	for (int i=0; i<MEASURES_COUNT; i++) {
		runGc();
		startMeasuring();
		for (int n=0; n<ITERATIONS_COUNT; n++) {
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
public void testPerfProjectFindKnownMemberType() throws CoreException {
	tagAsSummary("Model>Project>Find>MemberType", false); // do NOT put in fingerprint

	// Wait for indexing end
	waitUntilIndexesReady();

	// Warm up
	String fullQualifiedName = BIG_PROJECT_TYPE_PATH.removeFileExtension().removeFirstSegments(2).toString();
	fullQualifiedName = fullQualifiedName.replace('/', '.');
	for (int i=1; i<=10; i++) {
		fullQualifiedName += ".Level" + i;
	}
	if (WARMUP_COUNT > 0) {
		for (int i=0; i<WARMUP_COUNT; i++) {
			IType type = BIG_PROJECT.findType(fullQualifiedName);
			assertNotNull("We should find type '"+fullQualifiedName+"' in project "+BIG_PROJECT_NAME, type);
		}
	}

	// Measures
	resetCounters();
	for (int i=0; i<MEASURES_COUNT; i++) {
		runGc();
		startMeasuring();
		for (int n=0; n<ITERATIONS_COUNT; n++) {
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
public void testPerfProjectFindKnownSecondaryType() throws CoreException {
	tagAsSummary("Model>Project>Find>SecondaryType", false); // do NOT put in fingerprint

	// Wait for indexing end
	waitUntilIndexesReady();

	// Warm up
	String fullQualifiedName = BIG_PROJECT_TYPE_PATH.removeFileExtension().removeFirstSegments(2).removeLastSegments(1).toString();
	fullQualifiedName = fullQualifiedName.replace('/', '.')+".TestSecondary";
	if (WARMUP_COUNT > 0) {
		for (int i=0; i<WARMUP_COUNT; i++) {
			BIG_PROJECT.findType(fullQualifiedName);
		}
	}

	// Measures
	resetCounters();
	for (int i=0; i<MEASURES_COUNT; i++) {
		runGc();
		startMeasuring();
		for (int n=0; n<ITERATIONS_COUNT; n++) {
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
public void testPerfProjectFindUnknownType() throws CoreException {
	tagAsSummary("Model>Project>Find>UnknownType", false); // do NOT put in fingerprint

	// Wait for indexing end
	waitUntilIndexesReady();

	// Warm up
	String fullQualifiedName = BIG_PROJECT_TYPE_PATH.removeFileExtension().removeFirstSegments(2).removeLastSegments(1).toString();
	fullQualifiedName = fullQualifiedName.replace('/', '.')+".Unknown";
	if (WARMUP_COUNT > 0) {
		for (int i=0; i<WARMUP_COUNT; i++) {
			IType type = BIG_PROJECT.findType(fullQualifiedName);
			assertNull("We should not find an unknown type in project "+BIG_PROJECT_NAME, type);
		}
	}

	// Measures
	resetCounters();
	for (int i=0; i<MEASURES_COUNT; i++) {
		runGc();
		startMeasuring();
		for (int n=0; n<ITERATIONS_COUNT; n++) {
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
	tagAsSummary("Model>Reconcile>Parser", true); // put in fingerprint

	// Wait for indexing end
	waitUntilIndexesReady();

	// Warm up
	ICompilationUnit workingCopy = null;
	try {
		ProblemRequestor requestor = new ProblemRequestor();
		workingCopy = PARSER_WORKING_COPY.getWorkingCopy(new WorkingCopyOwner() {}, requestor, null);
		if (WARMUP_COUNT > 0) {
			for (int i=0; i<WARMUP_COUNT; i++) {
				CompilationUnit unit = workingCopy.reconcile(AST.JLS3, true, null, null);
				assertNotNull("Compilation Unit should not be null!", unit);
				assertNotNull("Bindings were not resolved!", unit.getPackage().resolveBinding());
			}
		}

		// Measures
		resetCounters();
		int iterations = 2;
		for (int i=0; i<MEASURES_COUNT; i++) {
			runGc();
			startMeasuring();
			for (int n=0; n<iterations; n++) {
				workingCopy.reconcile(AST.JLS3, true, null, null);
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

/**
 * Ensures that the reconciler does nothing when the source
 * to reconcile with is the same as the current contents.
 */
public void testPerfSearchAllTypeNamesAndReconcile() throws CoreException {
	tagAsSummary("Model>Reconcile>Parser", false); // do NOT put in fingerprint

	// Wait for indexing end
	waitUntilIndexesReady();

	// Warm up
	ICompilationUnit workingCopy = null;
	try {
		ProblemRequestor requestor = new ProblemRequestor();
		workingCopy = PARSER_WORKING_COPY.getWorkingCopy(new WorkingCopyOwner() {}, requestor, null);
		if (WARMUP_COUNT > 0) {
			for (int i=0; i<WARMUP_COUNT; i++) {
				searchAllTypeNames();
				CompilationUnit unit = workingCopy.reconcile(AST.JLS3, true, null, null);
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
				searchAllTypeNames();
				workingCopy.reconcile(AST.JLS3, true, null, null);
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
 * Performance test for looking up package fragments
 * (see bug 72683 Slow code assist in Display view)
 */
public void testPerfSeekPackageFragments() throws CoreException {
	assertNotNull("We should have the 'BigProject' in workspace!", BIG_PROJECT);
	class PackageRequestor implements IJavaElementRequestor {
		ArrayList pkgs = new ArrayList();
		public void acceptField(IField field) {}
		public void acceptInitializer(IInitializer initializer) {}
		public void acceptMemberType(IType type) {}
		public void acceptMethod(IMethod method) {}
		public void acceptPackageFragment(IPackageFragment packageFragment) {
			if (pkgs != null)
				pkgs.add(packageFragment);
		}
		public void acceptType(IType type) {}
		public boolean isCanceled() {
			return false;
		}
	}
	
	// first pass: ensure all class are loaded, and ensure that the test works as expected
	PackageRequestor requestor = new PackageRequestor();
	getNameLookup(BIG_PROJECT).seekPackageFragments("org.eclipse.jdt.core.tests78.performance5", false/*not partial match*/, requestor);
	int size = requestor.pkgs.size();
	IJavaElement[] result = new IJavaElement[size];
	requestor.pkgs.toArray(result);
	assertElementsEqual(
		"Unexpected packages",
		"org.eclipse.jdt.core.tests78.performance5 [in src78 [in "+BIG_PROJECT_NAME+"]]",
		result
	);
	
	// measure performance
	requestor.pkgs = null;
	resetCounters();
	for (int i = 0; i < MEASURES_COUNT; i++) {
		runGc();
		startMeasuring();
		for (int j = 0; j < ITERATIONS_COUNT; j++) {
			getNameLookup(BIG_PROJECT).seekPackageFragments("org.eclipse.jdt.core.tests" + j + "0.performance" + j, false/*not partial match*/, requestor);
		}
		stopMeasuring();
	}
	commitMeasurements();
	assertPerformance();
}

protected void resetCounters() {
	// do nothing
}
}
