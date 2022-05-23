/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
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

public class RecordPatternTest extends AbstractRegressionTest9 {

	private static final JavacTestOptions JAVAC_OPTIONS = new JavacTestOptions("-source 19 --enable-preview -Xlint:-preview");
	static {
//		TESTS_NUMBERS = new int [] { 40 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] { "test001" };
	}

	public static Class<?> testClass() {
		return RecordPatternTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_19);
	}
	public RecordPatternTest(String testName){
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

	protected Map<String, String> getCompilerOptions() {
		return getCompilerOptions(true);
	}

	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput) {
		runConformTest(testFiles, expectedOutput, getCompilerOptions(true));
	}
	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput, Map<String, String> customOptions) {
		if(!isJRE19Plus)
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
	/*
	 * Basic tests that accept a valid record pattern and make the pattern variable available
	 */
	public void test001() {
		Map<String, String> options = getCompilerOptions(true);
		runConformTest(new String[] {
				"X.java",
				"public class X {\n"
				+ "  static void print(Rectangle r) {\n"
				+ "    if (r instanceof (Rectangle(ColoredPoint(Point(int x, int y), Color c),\n"
				+ "                               ColoredPoint lr) r1)) {\n"
				+ "        System.out.println(\"Upper-left corner: \" + r1);\n"
				+ "    }\n"
				+ "  }\n"
				+ "  public static void main(String[] obj) {\n"
				+ "    print(new Rectangle(new ColoredPoint(new Point(0, 0), Color.BLUE), \n"
				+ "                               new ColoredPoint(new Point(10, 15), Color.RED)));\n"
				+ "  }\n"
				+ "}\n"
				+ "record Point(int x, int y) {}\n"
				+ "enum Color { RED, GREEN, BLUE }\n"
				+ "record ColoredPoint(Point p, Color c) {}\n"
				+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
				},
				"Upper-left corner: Rectangle[upperLeft=ColoredPoint[p=Point[x=0, y=0], c=BLUE], lowerRight=ColoredPoint[p=Point[x=10, y=15], c=RED]]",
				options);
	}
	public void test002() {
		runConformTest(new String[] {
				"X.java",
				"@SuppressWarnings(\"preview\")"
				+ "public class X {\n"
				+ "  public static void printLowerRight(Rectangle r) {\n"
				+ "    int res = switch(r) {\n"
				+ "       case Rectangle(ColoredPoint(Point(int x, int y), Color c),\n"
				+ "                               ColoredPoint lr) r1 -> {\n"
				+ "        yield r1.lowerRight().p().y();  \n"
				+ "      } \n"
				+ "      default -> 0;\n"
				+ "    }; \n"
				+ "    System.out.println(res);\n"
				+ "  }\n"
				+ "  public static void main(String[] args) {\n"
				+ "    printLowerRight(new Rectangle(new ColoredPoint(new Point(0, 0), Color.BLUE), \n"
				+ "        new ColoredPoint(new Point(10, 15), Color.RED)));\n"
				+ "  }\n"
				+ "}\n"
				+ "record Point(int x, int y) {}\n"
				+ "enum Color { RED, GREEN, BLUE }\n"
				+ "record ColoredPoint(Point p, Color c) {}\n"
				+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
				},
				"15");
	}
	public void test003() {
		runNegativeTest(new String[] {
				"X.java",
				"public class X {\n"
				+ "  static void print(Rectangle r) {\n"
				+ "    if (r instanceof (Rectangle(ColoredPoint(Point(int x, int y), Color c),\n"
				+ "                               ColoredPoint lr))) {\n"
				+ "        System.out.println(\"Upper-left corner: \" + r1);\n"
				+ "    }\n"
				+ "  }\n"
				+ "  public static void main(String[] obj) {\n"
				+ "    print(new Rectangle(new ColoredPoint(new Point(0, 0), Color.BLUE), \n"
				+ "    new ColoredPoint(new Point(10, 15), Color.RED)));\n"
				+ "  }\n"
				+ "}\n"
				+ "record Point(int x, int y) {}\n"
				+ "enum Color { RED, GREEN, BLUE }\n"
				+ "record ColoredPoint(Point p, Color c) {}\n"
				+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 5)\n" +
				"	System.out.println(\"Upper-left corner: \" + r1);\n" +
				"	                                           ^^\n" +
				"r1 cannot be resolved to a variable\n" +
				"----------\n");
	}
	public void test004() {
		runNegativeTest(new String[] {
				"X.java",
				"public class X {\n"
				+ "  static void print(Rectangle r) {\n"
				+ "    if (r instanceof (Rectangle(ColoredPoint(Point(int x, int y), Color c),\n"
				+ "                               ColoredPoint lr))) {\n"
				+ "    }\n"
				+ "  }\n"
				+ "  public static void main(String[] obj) {\n"
				+ "    print(new Rectangle(new ColoredPoint(new PointTypo(0, 0), Color.BLUE), \n"
				+ "    new ColoredPoint(new Point(10, 15), Color.RED)));\n"
				+ "  }\n"
				+ "}\n"
				+ "record Point(int x, int y) {}\n"
				+ "enum Color { RED, GREEN, BLUE }\n"
				+ "record ColoredPoint(Point p, Color c) {}\n"
				+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
				},
				"----------\n" +
				"1. ERROR in X.java (at line 8)\n" +
				"	print(new Rectangle(new ColoredPoint(new PointTypo(0, 0), Color.BLUE), \n" +
				"	                                         ^^^^^^^^^\n" +
				"PointTypo cannot be resolved to a type\n" +
				"----------\n");
	}

}