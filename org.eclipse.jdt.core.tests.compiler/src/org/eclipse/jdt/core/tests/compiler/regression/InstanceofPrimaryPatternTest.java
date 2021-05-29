/*******************************************************************************
 * Copyright (c) 2021 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
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

import java.util.Map;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class InstanceofPrimaryPatternTest extends AbstractRegressionTest {

	private static final JavacTestOptions JAVAC_OPTIONS = new JavacTestOptions("-source 17 --enable-preview -Xlint:-preview");
	static {
//		TESTS_NUMBERS = new int [] { 40 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] { "test005" };
	}

	public static Class<?> testClass() {
		return InstanceofPrimaryPatternTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_17);
	}
	public InstanceofPrimaryPatternTest(String testName){
		super(testName);
	}
	// Enables the tests to run individually
	protected Map<String, String> getCompilerOptions(boolean preview) {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		if (this.complianceLevel >= ClassFileConstants.getLatestJDKLevel()
				&& preview) {
			defaultOptions.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		}
		return defaultOptions;
	}

	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput, Map<String, String> customOptions) {
		if(!isJRE17Plus)
			return;
		runConformTest(testFiles, expectedOutput, customOptions, new String[] {"--enable-preview"}, JAVAC_OPTIONS);
	}
	protected void runNegativeTest(
			String[] testFiles,
			String expectedCompilerLog,
			String javacLog,
			String[] classLibraries,
			boolean shouldFlushOutputDirectory,
			Map<String, String> customOptions) {
		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedCompilerLog = expectedCompilerLog;
		runner.javacTestOptions = JAVAC_OPTIONS;
		runner.customOptions = customOptions;
		runner.expectedJavacOutputString = javacLog;
		runner.runNegativeTest();
	}
	public void test001() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  public static void foo(Object obj) {\n" +
				"		if (obj instanceof String s) {\n" +
				"			System.out.println(s);\n" +
				"		}\n " +
				"	}\n" +
				"  public static void main(String[] obj) {\n" +
				"		foo(\"Hello World!\");\n" +
				"	}\n" +
				"}\n",
			},
			"Hello World!",
			options);
	}
	public void test002() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  public static void foo(Object obj) {\n" +
				"		if (obj instanceof (String s)) {\n" +
				"			System.out.println(s);\n" +
				"		}\n " +
				"	}\n" +
				"  public static void main(String[] obj) {\n" +
				"		foo(\"Hello World!\");\n" +
				"	}\n" +
				"}\n",
			},
			"Hello World!",
			options);
	}
	public void test003() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  public static void foo(Object obj) {\n" +
				"		if (obj instanceof ((String s))) {\n" +
				"			System.out.println(s);\n" +
				"		}\n " +
				"	}\n" +
				"  public static void main(String[] obj) {\n" +
				"		foo(\"Hello World!\");\n" +
				"	}\n" +
				"}\n",
			},
			"Hello World!",
			options);
	}
	public void test004() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  public static void foo(Object obj) {\n" +
				"		if (obj instanceof ((((String s))))) {\n" +
				"			System.out.println(s);\n" +
				"		}\n " +
				"	}\n" +
				"  public static void main(String[] obj) {\n" +
				"		foo(\"Hello World!\");\n" +
				"	}\n" +
				"}\n",
			},
			"Hello World!",
			options);
	}
	public void test005() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  public static void foo(Object obj) {\n" +
				"		if (obj instanceof (String s) && (s.length() > 0)) {\n" +
				"			System.out.println(s);\n" +
				"		}\n " +
				"	}\n" +
				"  public static void main(String[] obj) {\n" +
				"		foo(\"Hello World!\");\n" +
				"	}\n" +
				"}\n",
			},
			"Hello World!",
			options);
	}
}