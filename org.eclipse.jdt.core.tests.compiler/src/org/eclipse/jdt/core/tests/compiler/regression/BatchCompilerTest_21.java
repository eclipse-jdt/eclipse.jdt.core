/*******************************************************************************
* Copyright (c) 2023 Advantest Europe GmbH and others.
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Srikanth Sankaran - initial implementation
*******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.util.Util;
import junit.framework.Test;

public class BatchCompilerTest_21 extends AbstractBatchCompilerTest {

	/**
	 * This test suite only needs to be run on one compliance.
	 *
	 * @see TestAll
	 */
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_21);
	}

	public static Class<BatchCompilerTest_21> testClass() {
		return BatchCompilerTest_21.class;
	}

	public BatchCompilerTest_21(String name) {
		super(name);
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1774
	// [switch] Code generated for statement switch doesn't handle MatchException
	public void testGHI1774_Expression() throws Exception {

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
					             case null -> 0;
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
			+ " -source 21 -warn:none"
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

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1774
	// [switch] Code generated for statement switch doesn't handle MatchException
	public void testGHI1774_Statement() throws Exception {

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
								switch (c) {
					             case null -> System.out.println("Null");
									case R -> System.out.print("R");
									case Y -> System.out.println("Y");
								};
							} catch (MatchException e) {
								System.out.print("OK!");
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
			+ " -source 21 -warn:none"
			+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
			"",
			"",
			true);
		this.verifier.execute("p.X", new String[] {OUTPUT_DIR + File.separator + "bin", libPath}, new String[0], new String[0]);
		assertEquals("Incorrect output", "REND", this.verifier.getExecutionOutput());
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
		this.verifier.execute("p.X", new String[] {OUTPUT_DIR + File.separator + "bin", libPath}, new String[0], new String[0]);
		assertEquals("Incorrect output", "OK!END", this.verifier.getExecutionOutput());
	}

}