/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;

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

	public void testJEP445() throws Exception {
		this.runConformTest(
			new String[] {
				"src/X.java",
				"""
				void main() {hello();}
				void hello() {new Y().hello();}
				class Y {
					public void hello() {
						System.out.print("hello");
					}
				}
				"""
			},
			'"' + OUTPUT_DIR +  File.separator + "src/X.java" + '"'
			+ " -sourcepath " + '"' + OUTPUT_DIR +  File.separator + "src" + '"'
			+ " --enable-preview -source 21 -warn:none"
			+ " -d " + '"' + OUTPUT_DIR + File.separator + "bin" + '"',
			"",
			"",
			true);
		File targetDir = new File(OUTPUT_DIR + File.separator + "bin");
		assertTrue(new File(targetDir, "X.class").isFile());
		assertTrue(new File(targetDir, "X$Y.class").isFile());
		this.verifier.execute("X", new String[] {targetDir.getAbsolutePath()}, new String[0], new String[] {"--enable-preview"});
		assertEquals("Incorrect output", "hello", this.verifier.getExecutionOutput());
	}

	public void testCannotResolveName() throws Exception {
		this.runNegativeTest(
			new String[] {
				"src/X.java",
				"""
				void main() {new X().hello();}
				void hello() {System.out.print("hello");}
				"""
			},
			'"' + OUTPUT_DIR +  File.separator + "src/X.java" + '"'
			+ " -sourcepath " + '"' + OUTPUT_DIR +  File.separator + "src" + '"'
			+ " --enable-preview -source 21 -warn:none"
			+ " -d " + '"' + OUTPUT_DIR + File.separator + "bin" + '"',
			"",
			"""
			----------
			1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/X.java (at line 1)
				void main() {new X().hello();}
				                 ^
			The type X is not accessible
			----------
			1 problem (1 error)
			"""
			/* javac says:
			X.java:1: error: cannot find symbol
			void main(){new X().toString();}
			                ^
			symbol:   class X
			location: class X*/,
			true);
	}

	public void testWithoutMain() throws Exception {
		this.runNegativeTest(
			new String[] {
				"src/X.java",
				"""
				void hello() {System.out.print("hello");}
				"""
			},
			'"' + OUTPUT_DIR +  File.separator + "src/X.java" + '"'
			+ " -sourcepath " + '"' + OUTPUT_DIR +  File.separator + "src" + '"'
			+ " --enable-preview -source 21 -warn:none"
			+ " -d " + '"' + OUTPUT_DIR + File.separator + "bin" + '"',
			"",
			"""
			----------
			1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/X.java (at line 1)
				void hello() {System.out.print("hello");}
				^
			unnamed class does not have main method in the form of void main() or void main(String[] args)
			----------
			1 problem (1 error)
			"""
			/* javac says:
			X.java:1: error: unnamed class does not have main method in the form of void main() or void main(String[] args)
			void hello(){}
			     ^
			*/,
			true);
	}


	public void testRedefineUnnamed() {
		this.runNegativeTest(
			new String[] { "src/X.java",
			"""
			public class X {}
			void main() {}
			""" },
			'"' + OUTPUT_DIR +  File.separator + "src/X.java" + '"'
			+ " -sourcepath " + '"' + OUTPUT_DIR +  File.separator + "src" + '"'
			+ " --enable-preview -source 21 -warn:none"
			+ " -d " + '"' + OUTPUT_DIR + File.separator + "bin" + '"',
			"",
			"""
			----------
			1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/X.java (at line 0)
				public class X {}
				^
			No other top-level types allowed with unnamed class
			----------
			2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/X.java (at line 1)
				public class X {}
				             ^
			The type X is already defined
			----------
			2 problems (2 errors)
			"""
			/* javac says:
			X.java:1: error: class X is already defined in package unnamed package
			public class X {
			       ^
			1 error
			error: compilation failed
			*/,
			true);
	}

}
