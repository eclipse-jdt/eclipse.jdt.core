/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

/**
 * This class is meant to gather test cases related to the invocation of the
 * compiler, be it at an API or non API level.
 */
public class CompilerInvocationTests extends AbstractRegressionTest {

public CompilerInvocationTests(String name) {
    super(name);
}

	// Static initializer to specify tests subset using TESTS_* static variables
  	// All specified tests which does not belong to the class are skipped...
  	// Only the highest compliance level is run; add the VM argument
  	// -Dcompliance=1.4 (for example) to lower it if needed
  	static {
//    	TESTS_NAMES = new String[] { "test001" };
//    	TESTS_NUMBERS = new int[] { 1 };   
//    	TESTS_RANGE = new int[] { 1, -1 }; 
//  	TESTS_RANGE = new int[] { 1, 2049 }; 
//  	TESTS_RANGE = new int[] { 449, 451 }; 
//    	TESTS_RANGE = new int[] { 900, 999 }; 
  	}

public static Test suite() {
    return buildTestSuite(testClass());
}
  
public static Class testClass() {
    return CompilerInvocationTests.class;
}

// irritant vs warning token
public void test001_irritant_warning_token() {
	String [] tokens = new String[64];
	Map matcher = new HashMap();
	long irritant;
	String token;
	for (int i = 0; i < 64; i++) {
		if ((token = tokens[i] = CompilerOptions.warningTokenFromIrritant(irritant = 1L << i)) != null) {
			matcher.put(token, token);
			assertTrue((irritant & CompilerOptions.warningTokenToIrritant(token)) != 0);
		}
	}
	String [] allTokens = CompilerOptions.warningTokens;
	int length = allTokens.length;
	matcher.put("all", "all"); // all gets undetected in the From/To loop
	assertEquals(allTokens.length, matcher.size());
	for (int i = 0; i < length; i++) {
		assertNotNull(matcher.get(allTokens[i]));
	}
}
  
}
