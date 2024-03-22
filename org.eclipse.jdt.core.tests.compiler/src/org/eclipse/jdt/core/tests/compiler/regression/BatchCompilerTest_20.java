/*******************************************************************************
 * Copyright (c) 2023 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.util.Util;

import junit.framework.Test;

public class BatchCompilerTest_20 extends AbstractBatchCompilerTest {

	static {
//			TESTS_NAMES = new String[] { "testIssue558_1" };
	//		TESTS_NUMBERS = new int[] { 306 };
	//		TESTS_RANGE = new int[] { 298, -1 };
	}
	/**
	 * This test suite only needs to be run on one compliance.
	 *
	 * @see TestAll
	 */
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_20);
	}

	public static Class<BatchCompilerTest_20> testClass() {
		return BatchCompilerTest_20.class;
	}

	public BatchCompilerTest_20(String name) {
		super(name);
	}

	public void testIssue558_1() throws Exception {
		String path = LIB_DIR;
		String libPath = null;
		if (path.endsWith(File.separator)) {
			libPath = path + "lib.jar";
		} else {
			libPath = path + File.separator + "lib.jar";
		}
		Util.createJar(new String[] {
			"p/Color.java",
			"""
				package p;
				public enum Color {
					R, Y;
					public static Color getColor() {
						return R;
					}
				}""",
		},
		libPath,
		JavaCore.VERSION_20);
		this.runConformTest(
			new String[] {
				"src/p/X.java",
				"""
					package p;
					import p.Color;
					public class X {
						public static void main(String argv[]) {
							Color c = Color.getColor();
							try {
								int a = switch (c) {
									case R -> 1;
									case Y -> 2;
								};
							} catch (MatchException e) {
								System.out.print("OK");
							} catch (Exception e) {
								System.out.print("NOT OK: " + e);
							}
								System.out.print("END");
						}
					}""",
			},
			"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
			+ " -cp \"" + LIB_DIR + File.separator + "lib.jar\""
			+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
			+ " --enable-preview -source 20 -warn:none"
			+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
			"",
			"",
			true);
		this.verifier.execute("p.X", new String[] {OUTPUT_DIR + File.separator + "bin", libPath}, new String[0], new String[] {"--enable-preview"});
		assertEquals("Incorrect output", "END", this.verifier.getExecutionOutput());
		Util.createJar(new String[] {
				"p/Color.java",
				"""
					package p;
					public enum Color {
						R, Y, B;
						public static Color getColor() {
							return B;
						}
					}""",
			},
			libPath,
			JavaCore.VERSION_20);
		this.verifier.execute("p.X", new String[] {OUTPUT_DIR + File.separator + "bin", libPath}, new String[0], new String[] {"--enable-preview"});
		assertEquals("Incorrect output", "OKEND", this.verifier.getExecutionOutput());
	}
	public void testIssue558_2() throws Exception {
		String path = LIB_DIR;
		String libPath = null;
		if (path.endsWith(File.separator)) {
			libPath = path + "lib.jar";
		} else {
			libPath = path + File.separator + "lib.jar";
		}
		Util.createJar(new String[] {
			"p/I.java",
			"""
				package p;
				public sealed interface I {
					public static I getImpl() {
						return new A();
					}
				}
				final class A implements I {}
				final class B implements I {}""",
		},
		libPath,
		JavaCore.VERSION_20);
		this.runConformTest(
			new String[] {
				"src/p/X.java",
				"""
					package p;
					import p.I;
					public class X {
						public static void main(String argv[]) {
							I i = I.getImpl();
							try {
								int r = switch (i) {
									case A a -> 1;
									case B b -> 2;
								};
							} catch (MatchException e) {
								System.out.print("OK");
							} catch (Exception e) {
								System.out.print("NOT OK: " + e);
							}
								System.out.print("END");
						}
					}""",
			},
			"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
			+ " -cp \"" + LIB_DIR + File.separator + "lib.jar\""
			+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
			+ " --enable-preview -source 20 -warn:none"
			+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
			"",
			"",
			true);
		this.verifier.execute("p.X", new String[] {OUTPUT_DIR + File.separator + "bin", libPath}, new String[0], new String[] {"--enable-preview"});
		assertEquals("Incorrect output", "END", this.verifier.getExecutionOutput());
		Util.createJar(new String[] {
				"p/I.java",
				"""
					package p;
					public sealed interface I {
						public static I getImpl() {
							return new C();
						}
					}
					final class A implements I {}
					final class B implements I {}
					final class C implements I {}""",
			},
			libPath,
			JavaCore.VERSION_20);
		this.verifier.execute("p.X", new String[] {OUTPUT_DIR + File.separator + "bin", libPath}, new String[0], new String[] {"--enable-preview"});
		assertEquals("Incorrect output", "OKEND", this.verifier.getExecutionOutput());
	}
}
