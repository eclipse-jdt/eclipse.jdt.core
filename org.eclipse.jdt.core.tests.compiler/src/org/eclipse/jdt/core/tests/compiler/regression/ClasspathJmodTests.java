/*******************************************************************************
 * Copyright (c) 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.tests.util.Util;

import junit.framework.Test;

public class ClasspathJmodTests extends ModuleCompilationTests {

	static {
//		 TESTS_NAMES = new String[] { "testPackageConflict4a" };
		// TESTS_NUMBERS = new int[] { 1 };
		// TESTS_RANGE = new int[] { 298, -1 };
	}

	public ClasspathJmodTests(String name) {
		super(name);
	}
	@Override
	public void setUp() throws Exception {
		super.setUp();
		System.setProperty("modules.to.load", "java.base");
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		System.setProperty("modules.to.load", "");
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_9);
	}

	public static Class<?> testClass() {
		return ClasspathJmodTests.class;
	}
	private String getJavaSqlJMod() {
		String home = Util.getJREDirectory();
		return home + File.separator + "jmods" + File.separator + "java.sql.jmod" + File.pathSeparator;
	}
	@Override
	public void test001() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
							  requires java.sql;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
						"""
							package p;
							public class X {
								public static java.sql.Connection getConnection() {
									return null;
								}
							}""");
		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" --module-path \"")
			.append(getJavaSqlJMod())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");
		runConformModuleTest(files,
				buffer,
				"",
				"",
				false,
				"p.X");
	}
	@Override
	public void test002() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
							  requires java.sql;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
						"""
							package p;
							public class X {
								public static java.sql.Connection getConnection() {
									return null;
								}
							}""");
		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" --module-path \"")
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");
		runNegativeModuleTest(files,
				buffer,
				"",
				"""
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 2)
						requires java.sql;
						         ^^^^^^^^
					java.sql cannot be resolved to a module
					----------
					----------
					2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/X.java (at line 3)
						public static java.sql.Connection getConnection() {
						              ^^^^^^^^
					java.sql cannot be resolved to a type
					----------
					2 problems (2 errors)
					""",
				false,
				"p.X");
	}
	@Override
	public void test003() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
							  requires java.sql;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
						"""
							package p;
							public class X {
								public static java.sql.Connection getConnection() {
									return null;
								}
							}""");
		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(getJavaSqlJMod())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");
		runNegativeModuleTest(files,
				buffer,
				"",
				"""
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 2)
						requires java.sql;
						         ^^^^^^^^
					java.sql cannot be resolved to a module
					----------
					----------
					2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/X.java (at line 3)
						public static java.sql.Connection getConnection() {
						              ^^^^^^^^
					java.sql cannot be resolved to a type
					----------
					2 problems (2 errors)
					""",
				false,
				"p.X");
	}
	@Override
	public void test004() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
						"""
							package p;
							public class X {
								public static java.sql.Connection getConnection() {
									return null;
								}
							}""");
		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" --module-path \"")
			.append(getJavaSqlJMod())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");
		runNegativeModuleTest(files,
				buffer,
				"",
				"""
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/X.java (at line 3)
						public static java.sql.Connection getConnection() {
						              ^^^^^^^^
					java.sql cannot be resolved to a type
					----------
					1 problem (1 error)
					""",
				false,
				"p.X");
	}
	@Override
	public void test005() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								exports p;
								requires mod.two;
								requires transitive java.sql;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
						"""
							package p;
							import q.Y;
							public class X {
								public static java.sql.Connection getConnection() {
									return Y.con;
								}
							}""");
		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.two {\s
								exports q;
								requires java.sql;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "q", "Y.java",
						"""
							package q;
							public class Y {
							   public static java.sql.Connection con = null;
							}""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" --module-path \"")
			.append(getJavaSqlJMod())
			.append("\" ")
			.append(" -warn:-exports") // Y.con unreliably refers to Connection (missing requires transitive)
			.append(" --module-source-path " + "\"" + directory + "\"");

		runConformModuleTest(files,
				buffer,
				"",
				"",
				false);
	}
}
