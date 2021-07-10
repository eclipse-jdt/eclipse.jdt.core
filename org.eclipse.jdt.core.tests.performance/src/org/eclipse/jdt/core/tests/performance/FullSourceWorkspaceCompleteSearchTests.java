/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.performance;

import java.io.PrintStream;
import java.text.NumberFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IOrdinaryClassFile;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.search.processing.IJob;

import junit.framework.Test;

/**
 * Performance test suite which covers all main search requests:
 * <ul>
 * </ul>
 *
 * Note that this test suite was not supposed to be included in releng performance tests
 * as it would take too much time to be run...
 */
@SuppressWarnings("rawtypes")
public class FullSourceWorkspaceCompleteSearchTests extends FullSourceWorkspaceSearchTests {

	// Tests counters
	private static int TESTS_COUNT = 0;
	private final static int ITERATIONS_COUNT = 0;

	// Log file streams
	private static PrintStream[] LOG_STREAMS = new PrintStream[DIM_NAMES.length];

	// Verify VM memory arguments: should be -Xmx256M -Xms256M
	final static long MAX_MEM = 512L * 1024 * 1024;

	static {
	//		org.eclipse.jdt.internal.core.search.processing.JobManager.VERBOSE = true;
	//		TESTS_NAMES = new String[] { "testSearchField" };
	}

/*
 * Specific way to build test suite.
 * We need to know whether test perf indexing is in list to allow
 * index manager disabling.
 * CAUTION: If test perf indexing is not included in test suite,
 * then time for other tests may include time spent to index files!
 */
public static Test suite() {
	Test suite = buildSuite(testClass());
	TESTS_COUNT = suite.countTestCases();
	createPrintStream(testClass(), LOG_STREAMS, TESTS_COUNT, null);
	verifyVmArguments();
	return suite;
}

private static Class testClass() {
	return FullSourceWorkspaceCompleteSearchTests.class;
}

/*
 * Verify VM arguments when full tests are run (should be -Xmx512M)
 */
private static void verifyVmArguments() {
	StringBuffer buffer = null;
	NumberFormat floatFormat = NumberFormat.getNumberInstance();
	floatFormat.setMaximumFractionDigits(1);
	long maxMem = Runtime.getRuntime().maxMemory(); // -Xmx
	boolean tooMuch = false;
	if (maxMem < (MAX_MEM*0.98) || (tooMuch = maxMem > (MAX_MEM*1.02))) {
		if (buffer == null) buffer = new StringBuffer("WARNING: Performance tests results may be invalid !!!\n");
		buffer.append("	- ");
		buffer.append(tooMuch ? "too much " : "not enough ");
		buffer.append("max memory allocated (");
		buffer.append(floatFormat.format(((maxMem/1024.0)/1024.0)));
		buffer.append("M)!\n");
		buffer.append("		=> -Xmx");
		buffer.append(floatFormat.format(((MAX_MEM/1024.0)/1024.0)));
		buffer.append("M should have been specified.\n");
	}
}

/**
 * @param name
 */
public FullSourceWorkspaceCompleteSearchTests(String name) {
	super(name);
}

@Override
protected void setUp() throws Exception {
	super.setUp();
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

/**
 * Job to measure times in same thread than index manager.
 */
class	 Measuring implements IJob {
	boolean start;
	Measuring(boolean start) {
		this.start = start;
	}
	public boolean belongsTo(String jobFamily) {
		return true;
	}
	public void cancel() {
		// nothing to cancel
	}
	public void ensureReadyToRun() {
		// always ready to do nothing
	}
	/**
	 * Execute the current job, answer whether it was successful.
	 */
	public boolean execute(IProgressMonitor progress) {
		if (this.start) {
			startMeasuring();
		} else {
			stopMeasuring();
			commitMeasurements();
			assertPerformance();
		}
		return true;
	}
	public String getJobFamily() {
		return "FullSourceWorkspaceCompleteSearchTests.Measuring";
	}
}

protected void search(String patternString, int searchFor, int limitTo, JavaSearchResultCollector resultCollector) throws CoreException {
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
		SearchEngine.createWorkspaceScope(),
		resultCollector,
		null);
}

protected void search(IJavaElement element, int limitTo, JavaSearchResultCollector resultCollector) throws CoreException {
	SearchPattern pattern = SearchPattern.createPattern(
		element,
		limitTo,
		SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE);
	new SearchEngine().search(
		pattern,
		new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
		SearchEngine.createWorkspaceScope(),
		resultCollector,
		null);
}

/**
 * Clean last category table cache
 * @param type Tells whether previous search was a type search or not
 * @param resultCollector result collector to count the matches found
 */
protected void cleanCategoryTableCache(boolean type, JavaSearchResultCollector resultCollector) throws CoreException {
	long time = System.currentTimeMillis();
	if (type) {
		search("foo", FIELD, DECLARATIONS, resultCollector);
	} else {
		search("Foo", TYPE, DECLARATIONS, resultCollector);
	}
	if (DEBUG) System.out.println("Time to clean category table cache: "+(System.currentTimeMillis()-time));
}

/**
 * Performance tests for search types in workspace:
 * <ul>
 * <li>declarations using string pattern</i>
 * <li>references using string pattern</i>
 * <li>delcarations using java element pattern</i>
 * <li>references using java element pattern</i>
 * </ul>
 */
public void testSearchStringTypeDeclarations() throws CoreException {

	// Wait for indexing end
	waitUntilIndexesReady();

	// Warm up
	String name = "Object";
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(name, TYPE, DECLARATIONS, resultCollector);
	NumberFormat intFormat = NumberFormat.getIntegerInstance();
	System.out.println("	- "+intFormat.format(resultCollector.count)+" declarations for type '"+name+"' in workspace.");

	// Measures
	for (int i=0; i<ITERATIONS_COUNT; i++) {
		cleanCategoryTableCache(true, resultCollector);
		runGc();
		startMeasuring();
		search(name, TYPE, DECLARATIONS, resultCollector);
		stopMeasuring();
	}

	// Commit
	commitMeasurements();
	assertPerformance();
}
public void testSearchStringTypeReferences() throws CoreException {

	// Wait for indexing end
	waitUntilIndexesReady();

	// Warm up
	String name = "Object";
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(name, TYPE, REFERENCES, resultCollector);
	NumberFormat intFormat = NumberFormat.getIntegerInstance();
	System.out.println("	- "+intFormat.format(resultCollector.count)+" references for type '"+name+"' in workspace.");

	// Measures
	for (int i=0; i<ITERATIONS_COUNT; i++) {
		cleanCategoryTableCache(true, resultCollector);
		runGc();
		startMeasuring();
		search(name, TYPE, REFERENCES, resultCollector);
		stopMeasuring();
	}

	// Commit
	commitMeasurements();
	assertPerformance();
}
public void testSearchJavaElementTypeDeclarations() throws CoreException {

	// Wait for indexing end
	waitUntilIndexesReady();

	// Get Object type
	IOrdinaryClassFile object = getClassFile(JDT_CORE_PROJECT, "rt.jar", "java.lang", "Object.class");
	IType type = object.getType();
	assertTrue("Cannot find type 'Object'", type != null && type.exists());

	// Warm up
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(type, DECLARATIONS, resultCollector);
	NumberFormat intFormat = NumberFormat.getIntegerInstance();
	System.out.println("	- "+intFormat.format(resultCollector.count)+" declarations for type '"+type.getElementName()+"' in workspace.");

	// Measures
	for (int i=0; i<ITERATIONS_COUNT; i++) {
		cleanCategoryTableCache(true, resultCollector);
		runGc();
		startMeasuring();
		search(type, DECLARATIONS, resultCollector);
		stopMeasuring();
	}

	// Commit
	commitMeasurements();
	assertPerformance();
}
public void testSearchJavaElementTypeReferences() throws CoreException {

	// Wait for indexing end
	waitUntilIndexesReady();

	// Get Object type
	IOrdinaryClassFile object = getClassFile(JDT_CORE_PROJECT, "rt.jar", "java.lang", "Object.class");
	IType type = object.getType();
	assertTrue("Cannot find type 'Object'", type != null && type.exists());

	// Warm up
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(type, REFERENCES, resultCollector);
	NumberFormat intFormat = NumberFormat.getIntegerInstance();
	System.out.println("	- "+intFormat.format(resultCollector.count)+" references for type '"+type.getElementName()+"' in workspace.");

	// Measures
	for (int i=0; i<ITERATIONS_COUNT; i++) {
		cleanCategoryTableCache(true, resultCollector);
		runGc();
		startMeasuring();
		search(type, REFERENCES, resultCollector);
		stopMeasuring();
	}

	// Commit
	commitMeasurements();
	assertPerformance();
}

/**
 * Performance tests for search fields in workspace:
 * <ul>
 * <li>declarations using string pattern</i>
 * <li>references using string pattern</i>
 * <li>delcarations using java element pattern</i>
 * <li>references using java element pattern</i>
 * </ul>
 */
public void testSearchStringFieldDeclarations() throws CoreException {

	// Wait for indexing end
	waitUntilIndexesReady();

	// Warm up
	String name = "name";
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(name, FIELD, DECLARATIONS, resultCollector);
	NumberFormat intFormat = NumberFormat.getIntegerInstance();
	System.out.println("	- "+intFormat.format(resultCollector.count)+" declarations for field '"+name+"' in workspace");

	// Measures
	for (int i=0; i<ITERATIONS_COUNT; i++) {
		// clean before test
		cleanCategoryTableCache(false, resultCollector);
		runGc();

		// test
		startMeasuring();
		search(name, FIELD, DECLARATIONS, resultCollector);
		stopMeasuring();
	}

	// Commit
	commitMeasurements();
	assertPerformance();
}
public void testSearchStringFieldReferences() throws CoreException {

	// Wait for indexing end
	waitUntilIndexesReady();

	// Warm up
	String name = "name";
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(name, FIELD, REFERENCES, resultCollector);
	NumberFormat intFormat = NumberFormat.getIntegerInstance();
	System.out.println("	- "+intFormat.format(resultCollector.count)+" references for field '"+name+"' in workspace");

	// Measures
	for (int i=0; i<ITERATIONS_COUNT; i++) {
		// clean before test
		cleanCategoryTableCache(false, resultCollector);
		runGc();

		// test
		startMeasuring();
		search(name, FIELD, REFERENCES, resultCollector);
		stopMeasuring();
	}

	// Commit
	commitMeasurements();
	assertPerformance();
}
public void testSearchJavaElementFieldDeclarations() throws CoreException {

	// Wait for indexing end
	waitUntilIndexesReady();

	// Get field 'name'
	IType type = JDT_CORE_PROJECT.findType("org.eclipse.jdt.internal.core", "JavaElement");
	assertTrue("Cannot find compilation unit 'JavaElement.java' in 'org.eclipse.jdt.internal.core'", type != null && type.exists());
	IField field = type.getField("name");
	assertTrue("Cannot find field equals", field != null && field.exists());

	// Warm up
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(field, DECLARATIONS, resultCollector);
	NumberFormat intFormat = NumberFormat.getIntegerInstance();
	System.out.println("	- "+intFormat.format(resultCollector.count)+" declarations for field '"+field.getElementName()+"' in workspace");

	// Measures
	for (int i=0; i<ITERATIONS_COUNT; i++) {
		// clean before test
		cleanCategoryTableCache(false, resultCollector);
		runGc();

		// test
		startMeasuring();
		search(field, DECLARATIONS, resultCollector);
		stopMeasuring();
	}

	// Commit
	commitMeasurements();
	assertPerformance();
}
public void testSearchJavaElementFieldReferences() throws CoreException {

	// Wait for indexing end
	waitUntilIndexesReady();

	// Get field 'name'
	IType type = JDT_CORE_PROJECT.findType("org.eclipse.jdt.internal.core", "JavaElement");
	assertTrue("Cannot find compilation unit 'JavaElement.java' in 'org.eclipse.jdt.internal.core'", type != null && type.exists());
	IField field = type.getField("name");
	assertTrue("Cannot find field equals", field != null && field.exists());

	// Warm up
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(field, REFERENCES, resultCollector);
	NumberFormat intFormat = NumberFormat.getIntegerInstance();
	System.out.println("	- "+intFormat.format(resultCollector.count)+" references for field '"+field.getElementName()+"' in workspace");

	// Measures
	for (int i=0; i<ITERATIONS_COUNT; i++) {
		// clean before test
		cleanCategoryTableCache(false, resultCollector);
		runGc();

		// test
		startMeasuring();
		search(field, REFERENCES, resultCollector);
		stopMeasuring();
	}

	// Commit
	commitMeasurements();
	assertPerformance();
}

/**
 * Performance tests for search methods in workspace:
 * <ul>
 * <li>declarations using string pattern</i>
 * <li>references using string pattern</i>
 * <li>delcarations using java element pattern</i>
 * <li>references using java element pattern</i>
 * </ul>
 */
public void testSearchStringMethodDeclarations() throws CoreException {

	// Wait for indexing end
	waitUntilIndexesReady();

	// Warm up
	String name = "equals";
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(name, METHOD, DECLARATIONS, resultCollector);
	NumberFormat intFormat = NumberFormat.getIntegerInstance();
	System.out.println("	- "+intFormat.format(resultCollector.count)+" declarations for method '"+name+"' in workspace");

	// Measures
	for (int i=0; i<ITERATIONS_COUNT; i++) {
		// clean before test
		cleanCategoryTableCache(false, resultCollector);
		runGc();

		// test
		startMeasuring();
		search(name, METHOD, DECLARATIONS, resultCollector);
		stopMeasuring();
	}

	// Commit
	commitMeasurements();
	assertPerformance();
}
public void testSearchStringMethodReferences() throws CoreException {

	// Wait for indexing end
	waitUntilIndexesReady();

	// Warm up
	String name = "equals";
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(name, METHOD, REFERENCES, resultCollector);
	NumberFormat intFormat = NumberFormat.getIntegerInstance();
	System.out.println("	- "+intFormat.format(resultCollector.count)+" references for method '"+name+"' in workspace");

	// Measures
	for (int i=0; i<ITERATIONS_COUNT; i++) {
		// clean before test
		cleanCategoryTableCache(false, resultCollector);
		runGc();

		// test
		startMeasuring();
		search(name, METHOD, REFERENCES, resultCollector);
		stopMeasuring();
	}

	// Commit
	commitMeasurements();
	assertPerformance();
}
public void testSearchJavaElementMethodDeclarations() throws CoreException {

	// Wait for indexing end
	waitUntilIndexesReady();

	// Get method 'equals'
	IOrdinaryClassFile object = getClassFile(JDT_CORE_PROJECT, "rt.jar", "java.lang", "Object.class");
	IType objectType = object.getType();
	IMethod method = objectType.getMethod("equals", new String[] { "Ljava.lang.Object;" });
	assertTrue("Cannot find method equals", method != null && method.exists());

	// Warm up
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(method, DECLARATIONS, resultCollector);
	NumberFormat intFormat = NumberFormat.getIntegerInstance();
	System.out.println("	- "+intFormat.format(resultCollector.count)+" declarations for method '"+method.getElementName()+"' in workspace");

	// Measures
	for (int i=0; i<ITERATIONS_COUNT; i++) {
		// clean before test
		cleanCategoryTableCache(false, resultCollector);
		runGc();

		// test
		startMeasuring();
		search(method, DECLARATIONS, resultCollector);
		stopMeasuring();
	}

	// Commit
	commitMeasurements();
	assertPerformance();
}
public void testSearchJavaElementMethodReferences() throws CoreException {

	// Wait for indexing end
	waitUntilIndexesReady();

	// Get method 'equals'
	IOrdinaryClassFile object = getClassFile(JDT_CORE_PROJECT, "rt.jar", "java.lang", "Object.class");
	IType objectType = object.getType();
	IMethod method = objectType.getMethod("equals", new String[] { "Ljava.lang.Object;" });
	assertTrue("Cannot find method equals", method != null && method.exists());

	// Warm up
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(method, REFERENCES, resultCollector);
	NumberFormat intFormat = NumberFormat.getIntegerInstance();
	System.out.println("	- "+intFormat.format(resultCollector.count)+" references for method '"+method.getElementName()+"' in workspace");

	// Measures
	for (int i=0; i<ITERATIONS_COUNT; i++) {
		// clean before test
		cleanCategoryTableCache(false, resultCollector);
		runGc();

		// test
		startMeasuring();
		search(method, REFERENCES, resultCollector);
		stopMeasuring();
	}

	// Commit
	commitMeasurements();
	assertPerformance();
}

/**
 * Performance tests for search constructors in workspace:
 * <ul>
 * <li>declarations using string pattern</i>
 * <li>references using string pattern</i>
 * <li>delcarations using java element pattern</i>
 * <li>references using java element pattern</i>
 * </ul>
 */
public void testSearchStringConstructorDeclarations() throws CoreException {

	// Wait for indexing end
	waitUntilIndexesReady();

	// Warm up
	String name = "()";
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(name, CONSTRUCTOR, DECLARATIONS, resultCollector);
	NumberFormat intFormat = NumberFormat.getIntegerInstance();
	System.out.println("	- "+intFormat.format(resultCollector.count)+" declarations for default constructor in workspace");

	// Measures
	for (int i=0; i<ITERATIONS_COUNT; i++) {
		// clean before test
		cleanCategoryTableCache(false, resultCollector);
		runGc();

		// test
		startMeasuring();
		search(name, CONSTRUCTOR, DECLARATIONS, resultCollector);
		stopMeasuring();
	}

	// Commit
	commitMeasurements();
	assertPerformance();
}
public void testSearchStringConstructorReferences() throws CoreException {

	// Wait for indexing end
	waitUntilIndexesReady();

	// Warm up
	String name = "()";
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(name, CONSTRUCTOR, REFERENCES, resultCollector);
	NumberFormat intFormat = NumberFormat.getIntegerInstance();
	System.out.println("	- "+intFormat.format(resultCollector.count)+" references for default constructor in workspace");

	// Measures
	for (int i=0; i<ITERATIONS_COUNT; i++) {
		// clean before test
		cleanCategoryTableCache(false, resultCollector);
		runGc();

		// test
		startMeasuring();
		search(name, CONSTRUCTOR, REFERENCES, resultCollector);
		stopMeasuring();
	}

	// Commit
	commitMeasurements();
	assertPerformance();
}
public void testSearchJavaElementConstructorDeclarations() throws CoreException {

	// Wait for indexing end
	waitUntilIndexesReady();

	// Get constructor 'equals'
	IOrdinaryClassFile object = getClassFile(JDT_CORE_PROJECT, "rt.jar", "java.lang", "Object.class");
	IType objectType = object.getType();
	IMethod constructor = objectType.getMethod("Object", new String[0]);
	assertTrue("Cannot find default constructor", constructor != null && constructor.exists());

	// Warm up
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(constructor, DECLARATIONS, resultCollector);
	NumberFormat intFormat = NumberFormat.getIntegerInstance();
	System.out.println("	- "+intFormat.format(resultCollector.count)+" declarations for default constructor in workspace");

	// Measures
	for (int i=0; i<ITERATIONS_COUNT; i++) {
		// clean before test
		cleanCategoryTableCache(false, resultCollector);
		runGc();

		// test
		startMeasuring();
		search(constructor, DECLARATIONS, resultCollector);
		stopMeasuring();
	}

	// Commit
	commitMeasurements();
	assertPerformance();
}
public void testSearchJavaElementConstructorReferences() throws CoreException {

	// Wait for indexing end
	waitUntilIndexesReady();

	// Get constructor 'equals'
	IOrdinaryClassFile object = getClassFile(JDT_CORE_PROJECT, "rt.jar", "java.lang", "Object.class");
	IType objectType = object.getType();
	IMethod constructor = objectType.getMethod("Object", new String[0]);
	assertTrue("Cannot find default constructor", constructor != null && constructor.exists());

	// Warm up
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(constructor, REFERENCES, resultCollector);
	NumberFormat intFormat = NumberFormat.getIntegerInstance();
	System.out.println("	- "+intFormat.format(resultCollector.count)+" references for default constructor in workspace");

	// Measures
	for (int i=0; i<ITERATIONS_COUNT; i++) {
		// clean before test
		cleanCategoryTableCache(false, resultCollector);
		runGc();

		// test
		startMeasuring();
		search(constructor, REFERENCES, resultCollector);
		stopMeasuring();
	}

	// Commit
	commitMeasurements();
	assertPerformance();
}
}
