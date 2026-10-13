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

import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.core.tests.util.PreviewTest;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

/**
 * Replicas of tests from {@link SuperAfterStatementsTest} whose results differ when run
 * at 28 with preview enabled.
 */
@RunJavac
@PreviewTest
public class SuperAfterStatementsTests28 extends AbstractRegressionTestCommon {

	static {
//		TESTS_NAMES = new String[] { "test008" };
	}

	public static Class<?> testClass() {
		return SuperAfterStatementsTests28.class;
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_28);
	}

	public SuperAfterStatementsTests28(String testName) {
		super(testName);
	}

	private static final JavacTestOptions JAVAC_OPTIONS = new JavacTestOptions("--enable-preview -source 28");
	private static final String[] VMARGS = new String[] {"--enable-preview"};

	protected Map<String, String> getCompilerOptions(boolean preview) {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_28);
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_28);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_28);
		defaultOptions.put(CompilerOptions.OPTION_EnablePreviews, preview ? CompilerOptions.ENABLED : CompilerOptions.DISABLED);
		defaultOptions.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.WARNING);
		return defaultOptions;
	}

	@Override
	protected void runNegativeTest(String[] testFiles, String expectedCompilerLog) {
		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedCompilerLog = expectedCompilerLog;
		runner.javacTestOptions = JAVAC_OPTIONS;
		runner.customOptions = getCompilerOptions(true);
		runner.expectedJavacOutputString = null;
		runner.runNegativeTest();
	}

	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput) {
		runConformTest(testFiles, expectedOutput, getCompilerOptions(true), VMARGS, JAVAC_OPTIONS);
	}

	@Override
	protected JavacTestOptions getJavacTestOptions() {
		return JAVAC_OPTIONS;
	}

	// i++ on a field without initializer is a legal early read under preview
	public void test008() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			class A {
			    int i;
			    A() {
			        i++;
			        hashCode();                 // Error
			        super();
			    }
			}
			"""
		},
		"----------\n" +
		"1. ERROR in X.java (at line 5)\n" +
		"	hashCode();                 // Error\n" +
		"	^^^^^^^^^^\n" +
		"Cannot invoke method hashCode() in an early construction context\n" +
		"----------\n");
	}

	public void testGH3748a() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public class X {
				final int fin1, fin2;
				{
					fin1 = 0;
					fin2 = 1;
				}
				X() {
					int abc = 0; // Commenting out this line brings out the error
					this(fin1 = 10);
					fin2 = 11;
				}
				X(int x) {}
			}
			"""
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	this(fin1 = 10);\n" +
		"	     ^^^^\n" +
		"Cannot refer to field fin1 in an early construction context\n" +
		"----------\n");
	}

	public void testGH3748b() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public class X {
				final int fin1;
				final int fin2;
				{
					fin1 = 0;
					fin2 = 1;
				}
				X() {
					this(fin1 = 10);
					fin2 = 11;
				}
				X(int x) {}
			}
			"""
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	this(fin1 = 10);\n" +
		"	     ^^^^\n" +
		"Cannot refer to field fin1 in an early construction context\n" +
		"----------\n");
	}

	public void testGH4449a() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public class X {

			  final int i;

			  public X() {
			    this.i = 1;
			    this(2);
			    System.out.print(this.i);
			  }

			  public X(int i) {
			    this.i = i;
			  }

			}
			"""
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	this.i = 1;\n" +
		"	^^^^^^\n" +
		"Cannot refer to field i in an early construction context\n" +
		"----------\n");
	}

	public void testGH4449b() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public class X {

			  final int i;
			  public X(Integer o) { this(o.intValue()); }
			  public X() {
			    this.i = 1;
			    this(2);
			    System.out.print(this.i);
			  }

			  public X(int i) {
			    this.i = i;
			  }

			}
			"""
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	this.i = 1;\n" +
		"	^^^^^^\n" +
		"Cannot refer to field i in an early construction context\n" +
		"----------\n");
	}

	public void testGH4449c() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public class X {

			  final int i;
			  public X(Integer o) { this.i = o.intValue(); }
			  public X() {
			    this.i = 1;
			    this(2);
			    System.out.print(this.i);
			  }

			  public X(int i) {
			    this.i = i;
			  }

			}
			"""
		},
		"----------\n" +
		"1. ERROR in X.java (at line 6)\n" +
		"	this.i = 1;\n" +
		"	^^^^^^\n" +
		"Cannot refer to field i in an early construction context\n" +
		"----------\n");
	}
}