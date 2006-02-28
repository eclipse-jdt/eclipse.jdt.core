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
package org.eclipse.jdt.core.tests.junit.extension;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

import org.eclipse.jdt.core.Flags;
import org.eclipse.test.performance.PerformanceTestCase;

import junit.framework.ComparisonFailure;

public class TestCase extends PerformanceTestCase {

	public static final String METHOD_PREFIX = "test";
	public  static String RUN_ONLY_ID = "ONLY_";

	// static variables for subsets tests
	public static String TESTS_PREFIX = null; // prefix of test names to perform
	public static String[] TESTS_NAMES = null; // list of test names to perform
	public static int[] TESTS_NUMBERS = null; // list of test numbers to perform
	public static int[] TESTS_RANGE = null; // range of test numbers to perform

	public TestCase(String name) {
		setName(name);
	}
public static void assertEquals(String expected, String actual) {
    assertEquals(null, expected, actual);
}
public static void assertEquals(String message, String expected, String actual) {
	assertStringEquals(message, expected, actual, true);
}
public static void assertStringEquals(String expected, String actual, boolean showLineSeparators) {
	assertStringEquals(null, expected, actual, showLineSeparators);
}
public static void assertStringEquals(String message, String expected, String actual, boolean showLineSeparators) {
	if (expected == null && actual == null)
		return;
	if (expected != null && expected.equals(actual))
		return;
	final String formatted;
	if (message != null) {
		formatted = message+"."; //$NON-NLS-1$
	} else {
		formatted = ""; //$NON-NLS-1$
	}
	if (showLineSeparators) {
		final String expectedWithLineSeparators = showLineSeparators(expected);
		final String actualWithLineSeparators = showLineSeparators(actual);
		throw new ComparisonFailure(
			    formatted
					+ "\n----------- Expected ------------\n" //$NON-NLS-1$
					+ expectedWithLineSeparators
					+ "\n------------ but was ------------\n" //$NON-NLS-1$
					+ actualWithLineSeparators
					+ "\n--------- Difference is ----------\n", //$NON-NLS-1$
			    expectedWithLineSeparators, 
			    actualWithLineSeparators);
	} else {
		throw new ComparisonFailure(
			    formatted
					+ "\n----------- Expected ------------\n" //$NON-NLS-1$
					+ expected
					+ "\n------------ but was ------------\n" //$NON-NLS-1$
					+ actual
					+ "\n--------- Difference is ----------\n", //$NON-NLS-1$
			    expected, 
			    actual);
	}
}
/*
 * Shows the line separators in the given String.
 */
protected static String showLineSeparators(String string) {
	if (string == null) return null;
	StringBuffer buffer = new StringBuffer();
	int length = string.length();
	for (int i = 0; i < length; i++) {
		char car = string.charAt(i);
		switch (car) {
			case '\n': 
				buffer.append("\\n\n"); //$NON-NLS-1$
				break;
			case '\r':
				if (i < length-1 && string.charAt(i+1) == '\n') {
					buffer.append("\\r\\n\n"); //$NON-NLS-1$
					i++;
				} else {
					buffer.append("\\r\n"); //$NON-NLS-1$
				}
				break;
			default:
				buffer.append(car);
				break;
		}
	}
	return buffer.toString();
}

public static List buildTestsList(Class evaluationTestClass) {
	return buildTestsList(evaluationTestClass, 0);
}

public static List buildTestsList(Class evaluationTestClass, int inheritedDepth) {
	List tests = new ArrayList();
	List testNames = new ArrayList();
	List onlyNames = new ArrayList();
	Constructor constructor = null;
	try {
		// Get class constructor
		Class[] paramTypes = new Class[] { String.class };
		constructor = evaluationTestClass.getConstructor(paramTypes);
	}
	catch (Exception e) {
		// cannot get constructor, skip suite
		return tests;
	}

	// Get all tests from "test%" methods
	Method[] methods = evaluationTestClass.getDeclaredMethods();
	Class evaluationTestSuperclass = evaluationTestClass.getSuperclass();
	for (int i=0; i<inheritedDepth && !Flags.isAbstract(evaluationTestSuperclass.getModifiers()); i++) {
		Method[] superMethods = evaluationTestSuperclass.getDeclaredMethods();
		Method[] mergedMethods = new Method[methods.length+superMethods.length];
		System.arraycopy(superMethods, 0, mergedMethods, 0, superMethods.length);
		System.arraycopy(methods, 0, mergedMethods, superMethods.length, methods.length);
		methods = mergedMethods;
		evaluationTestSuperclass = evaluationTestSuperclass.getSuperclass();
	}

	// Build test names list
	final int methodPrefixLength = METHOD_PREFIX.length();
	nextMethod: for (int m = 0, max = methods.length; m < max; m++) {
		int modifiers = methods[m].getModifiers();
		if (Flags.isPublic(modifiers) && !Flags.isStatic(modifiers)) {
			String methName = methods[m].getName();
			if (methName.startsWith(METHOD_PREFIX)) {

				// look if this is a run only method
				boolean isOnly = RUN_ONLY_ID != null && methName.substring(methodPrefixLength).startsWith(RUN_ONLY_ID);
				if (isOnly) {
					if (!onlyNames.contains(methName)) {
						onlyNames.add(methName);
					}
					continue;
				}

				// no prefix, no subsets => add method
				if (TESTS_PREFIX == null && TESTS_NAMES == null && TESTS_NUMBERS == null && TESTS_RANGE == null) {
					if (!testNames.contains(methName)) {
						testNames.add(methName);
					}
					continue nextMethod;
				}

				// no prefix or method matches prefix
				if (TESTS_PREFIX == null || methName.startsWith(TESTS_PREFIX)) {
					int numStart = TESTS_PREFIX==null ? methodPrefixLength : TESTS_PREFIX.length();
					// tests names subset
					if (TESTS_NAMES != null) {
						for (int i = 0, imax= TESTS_NAMES.length; i<imax; i++) {
							if (methName.indexOf(TESTS_NAMES[i]) >= 0) {
								if (!testNames.contains(methName)) {
									testNames.add(methName);
								}
								continue nextMethod;
							}
						}
					}
					// look for test number
					int length = methName.length();
					if (numStart < length) {
						// get test number
						while (numStart<length && !Character.isDigit(methName.charAt(numStart))) numStart++; // skip to first digit
						while (numStart<length && methName.charAt(numStart) == '0') numStart++; // skip to first non-nul digit
						int n = numStart;
						while (n<length && Character.isDigit(methName.charAt(n))) n++; // skip to next non-digit
						if (n>numStart && n <= length) {
							try {
								int num = Integer.parseInt(methName.substring(numStart, n));
								// tests numbers subset
								if (TESTS_NUMBERS != null && !tests.contains(methName)) {
									for (int i = 0; i < TESTS_NUMBERS.length; i++) {
										if (TESTS_NUMBERS[i] == num) {
											testNames.add(methName);
											continue nextMethod;
										}
									}
								}
								// tests range subset
								if (TESTS_RANGE != null && TESTS_RANGE.length == 2 && !tests.contains(methName)) {
									if ((TESTS_RANGE[0]==-1 || num>=TESTS_RANGE[0]) && (TESTS_RANGE[1]==-1 || num<=TESTS_RANGE[1])) {
										testNames.add(methName);
										continue nextMethod;
									}
								}
							} catch (NumberFormatException e) {
								System.out.println("Method "+methods[m]+" has an invalid number format: "+e.getMessage());
							}
						}
					}

					// no subset, add all tests
					if (TESTS_NAMES==null && TESTS_NUMBERS==null && TESTS_RANGE==null) {
						if (!testNames.contains(methName)) {
							testNames.add(methName);
						}
					}
				}
			}
		}
	}

	// Add corresponding tests
	List names = onlyNames.size() > 0 ? onlyNames : testNames;
	Iterator iterator = names.iterator();
	while (iterator.hasNext()) {
		String testName = (String) iterator.next();
		try {
			tests.add(constructor.newInstance(new Object[] { testName } ));
		}
		catch (Exception e) {
			System.err.println("Method "+testName+" removed from suite due to exception: "+e.getMessage());
		}
	}
	return tests;
}
public void startMeasuring() {
	// make it public to avoid compiler warning about synthetic access
	super.startMeasuring();
}
public void stopMeasuring() {
	// make it public to avoid compiler warning about synthetic access
	super.stopMeasuring();
}
public void assertPerformance() {
	// make it public to avoid compiler warning about synthetic access
	super.assertPerformance();
}
public void commitMeasurements() {
	// make it public to avoid compiler warning about synthetic access
	super.commitMeasurements();
}
}
