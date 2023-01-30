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
 *******************************************************************************/
package org.eclipse.jdt.core.tests.performance;

import java.io.File;
import java.io.PrintStream;

import junit.framework.Test;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatter;

/**
 */
@SuppressWarnings("rawtypes")
public class FullSourceWorkspaceFormatterTests extends FullSourceWorkspaceTests {

	// Tests counters
	static int TESTS_COUNT = 0;
	private final static int WARMUP_COUNT = 5;
	static int TESTS_LENGTH;

	// Log file streams
	private static PrintStream[] LOG_STREAMS = new PrintStream[DIM_NAMES.length];

	// Type path
	static IPath FORMAT_TYPE_PATH;
	static String FORMAT_TYPE_SOURCE;

/**
 * @param name
 */
public FullSourceWorkspaceFormatterTests(String name) {
	super(name);
}

static {
//	TESTS_NAMES = new String[] {
//	};
}
public static Test suite() {
	Test suite = buildSuite(testClass());
	TESTS_LENGTH = TESTS_COUNT = suite.countTestCases();
	createPrintStream(testClass(), LOG_STREAMS, TESTS_COUNT, null);
	return suite;
}

private static Class testClass() {
	return FullSourceWorkspaceFormatterTests.class;
}

@Override
protected void setUp() throws Exception {
	super.setUp();

	// Read big file
	System.out.print("	- Read big file source...");
	File file = fetchFromBinariesProject("GenericTypeTest.java", 1_258_374);
	String sourceFilePath = file.getAbsolutePath();

	long start = System.currentTimeMillis();
	FORMAT_TYPE_SOURCE = Util.fileContent(sourceFilePath);
	System.out.println("("+(System.currentTimeMillis()-start)+"ms)");
}

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
//		System.out.println("-------------------------------------");
//		System.out.println("Format performance test statistics:");
//		NumberFormat intFormat = NumberFormat.getIntegerInstance();
//		System.out.println("-------------------------------------\n");
	}
	super.tearDown();
}

/**
 * Format file (Parser.java - 225176 chars) using code formatter default options.
 */
public void testFormatDefault() throws JavaModelException {
	tagAsSummary("Format file with default options", false); // do NOT put in fingerprint

	// Warm up
	String source = PARSER_WORKING_COPY.getSource();
	int warmup = WARMUP_COUNT;
	for (int i=0; i<warmup; i++) {
		long start = System.currentTimeMillis();
		new DefaultCodeFormatter().format(CodeFormatter.K_COMPILATION_UNIT, source, 0, source.length(), 0, null);
		if (i==0) {
			System.out.println("	Time to format file ("+source.length()+" chars) = "+(System.currentTimeMillis()-start)+"ms");
		}
	}

	// Measures
	resetCounters();
	int measures = MEASURES_COUNT;
	for (int i=0; i<measures; i++) {
		runGc();
		startMeasuring();
		for (int j=1; j<10; j++)
			new DefaultCodeFormatter().format(CodeFormatter.K_COMPILATION_UNIT, source, 0, source.length(), 0, null);
		stopMeasuring();
	}

	// Commit
	commitMeasurements();
	assertPerformance();
}

/**
 * Format big file (GenericTypeTest.java - 1297242 chars) using code formatter default options.
 */
public void testFormatDefaultBigFile() {
	tagAsSummary("Format big file with default options", false); // do NOT put in fingerprint yet...

	// Warm up
	String source = FORMAT_TYPE_SOURCE;
	int warmup = WARMUP_COUNT;
	for (int i=0; i<warmup; i++) {
		long start = System.currentTimeMillis();
		new DefaultCodeFormatter().format(CodeFormatter.K_COMPILATION_UNIT, source, 0, source.length(), 0, null);
		if (i==0) {
			System.out.println("	Time to format big file ("+source.length()+" chars) = "+(System.currentTimeMillis()-start)+"ms");
		}
	}

	// Measures
	resetCounters();
	int measures = MEASURES_COUNT;
	for (int i=0; i<measures; i++) {
		runGc();
		startMeasuring();
		new DefaultCodeFormatter().format(CodeFormatter.K_COMPILATION_UNIT, source, 0, source.length(), 0, null);
		stopMeasuring();
	}

	// Commit
	commitMeasurements();
	assertPerformance();
}

protected void resetCounters() {
	// do nothing
}
}
