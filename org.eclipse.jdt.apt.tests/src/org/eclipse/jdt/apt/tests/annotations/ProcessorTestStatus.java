/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    jgarms@bea.com - initial API and implementation
 *    
 *******************************************************************************/
package org.eclipse.jdt.apt.tests.annotations;

/**
 * Utility class to hold results of processor-based tests.
 * All methods are static.
 */
public final class ProcessorTestStatus {
	
	/** 
	 * Marker string to indicate that no errors were encountered.
	 */
	public static final String NO_ERRORS = "NO ERRORS";
	
	/** Error status. Will be == NO_ERRORS if no errors were encountered **/
	private static String s_errorStatus = NO_ERRORS;
	
	/** An expected condition failed. Record the error **/
	public static void failWithoutException(final String error) {
		s_errorStatus = error;
	}
	
	/** Returns true if any errors were encountered **/
	public static boolean hasErrors() {
		return s_errorStatus == NO_ERRORS;
	}
	
	/** Get the error string. Will be NO_ERRORS if none were encountered **/
	public static String getErrors() {
		return s_errorStatus;
	}
	
	/** Reset the status. Needs to be called before each set of tests that could fail **/
	public static void reset() {
		s_errorStatus = NO_ERRORS;
	}
	
	// Private c-tor to prevent construction
	private ProcessorTestStatus() {}

	public static void assertEquals(String reason, Object expected, Object actual) {
		if (expected == actual)
			return;
		if (expected != null && expected.equals(actual))
			return;
		ProcessorTestStatus.fail("Expected " + expected + ", but saw " + actual + ". Reason: " + reason);
	}

	public static void assertEquals(String reason, String expected, String actual) {
		if (expected == actual)
			return;
		if (expected != null && expected.equals(actual))
			return;
		ProcessorTestStatus.fail("Expected " + expected + ", but saw " + actual + ". Reason: " + reason);
	}

	public static void assertEquals(String reason, int expected, int actual) {
		if (expected == actual)
			return;
		ProcessorTestStatus.fail("Expected " + expected + ", but saw " + actual + ". Reason: " + reason);
	}

	public static void assertTrue(String reason, boolean expected) {
		if (!expected)
			ProcessorTestStatus.fail(reason);
	}

	public static void fail(final String reason) {
		failWithoutException(reason);
		throw new IllegalStateException("Failed during test: " + reason);
	}

}
