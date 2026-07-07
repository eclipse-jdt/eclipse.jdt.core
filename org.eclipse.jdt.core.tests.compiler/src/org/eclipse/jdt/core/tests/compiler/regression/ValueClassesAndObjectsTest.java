/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;
import junit.framework.Test;
import org.eclipse.jdt.core.tests.util.PreviewTest;

@PreviewTest
public class ValueClassesAndObjectsTest extends AbstractRegressionTestCommon {
	static {
//		TESTS_NUMBERS = new int [] { 1 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] { "testIssue3536" };
	}
	public static Class<?> testClass() {
		return ValueClassesAndObjectsTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_26);
	}
	public ValueClassesAndObjectsTest(String testName) {
		super(testName);
	}
	// ========= OPT-IN to run.javac mode: ===========
//	@Override
//	protected void setUp() throws Exception {
//		this.runJavacOptIn = true;
//		super.setUp();
//	}
//	@Override
//	protected void tearDown() throws Exception {
//		super.tearDown();
//		this.runJavacOptIn = false; // do it last, so super can still clean up
//	}
//
//	@Override
//	protected JavacTestOptions getJavacTestOptions() {
//		return JAVAC_OPTIONS;
//	}
	// =================================================
	// https://cr.openjdk.org/~dlsmith/jep401/latest/
  public void testValueTypes_001() {
		runNegativeTest(new String[] {
			"X.java",
				"""
				 public class X {
				    public static void main(String[] args) {
						System.out.println("");
					}
				}
				class value {}
			"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	class value {}\n" +
			"	      ^^^^^\n" +
			"\'value\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 26\n" +
			"----------\n");
	}
  public void testValueTypes_002() {
		runNegativeTest(new String[] {
			"X.java",
				"""
				 public value class X {
				    public static void main(String[] args) {
				    	Zork();
					}
				}
			"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 3)\n" +
			"	Zork();\n" +
			"	^^^^\n" +
			"The method Zork() is undefined for the type X\n" +
			"----------\n");
	}
 }