/*******************************************************************************
 * Copyright (c) 2024 GK Software SE, and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
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
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import junit.framework.Test;

public class ModuleImportTests extends AbstractModuleCompilationTest {

	static {
//		 TESTS_NAMES = new String[] { "test001" };
		// TESTS_NUMBERS = new int[] { 1 };
		// TESTS_RANGE = new int[] { 298, -1 };
	}

	public ModuleImportTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_23);
	}

	public static Class<?> testClass() {
		return ModuleImportTests.class;
	}

	@Override
	protected StringBuilder trimJavacLog(StringBuilder log) {
		// suppress preview warnings from javac:
		if (log.substring(0, 6).equals("Note: ")) {
			StringBuilder filtered = new StringBuilder();
			filtered.append(
				log.toString().lines()
					.filter(line -> !line.equals("Note: Recompile with -Xlint:preview for details."))
					.filter(line -> !(line.startsWith("Note: ") && line.endsWith("uses preview features of Java SE 23.")))
					.collect(Collectors.joining("\n")));
			return filtered;
		}
		return log;
	}

	public void test000_previewDisabled() {
		runNegativeModuleTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					import module java.sql;
					public class X {
						public static void main(String[] args) {
							@SuppressWarnings("unused")
							Connection con = null;
						}
					}
					""",
				"module-info.java",
				"""
					module mod.one {
					}
					"""
	        },
			" -23 \"" + OUTPUT_DIR +  File.separator + "module-info.java\" "
	        + "\"" + OUTPUT_DIR +  File.separator + "p/X.java\"",
	        "",
	        """
				----------
				1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/p/X.java (at line 2)
					import module java.sql;
					              ^^^^^^^^
				Module Import Declarations is a preview feature and disabled by default. Use --enable-preview to enable
				----------
				2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/p/X.java (at line 6)
					Connection con = null;
					^^^^^^^^^^
				Connection cannot be resolved to a type
				----------
				2 problems (2 errors)
				""",
	        true,
	        "disabled");
	}

	public void test000_previewWrongVersion() {
		runNegativeModuleTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					import module java.sql;
					public class X {
						public static void main(String[] args) {
							@SuppressWarnings("unused")
							Connection con = null;
						}
					}
					""",
				"module-info.java",
				"""
					module mod.one {
					}
					"""
	        },
			" -22 --enable-preview \"" + OUTPUT_DIR +  File.separator + "module-info.java\" "
	        + "\"" + OUTPUT_DIR +  File.separator + "p/X.java\"",
	        "",
	        """
				Preview of features is supported only at the latest source level
				""",
	        true,
	        "only supported for release 23");
	}

	public void test001_simpleOK() {
		runConformModuleTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					import /*ignoreme*/ module java.sql;
					@SuppressWarnings("preview")
					public class X {
						public static void main(String[] args) {
							@SuppressWarnings("unused")
							Connection con = null;
						}
					}
					""",
				"module-info.java",
				"""
					module mod.one {
						requires java.sql;
					}
					"""
	        },
			" -23 --enable-preview \"" + OUTPUT_DIR +  File.separator + "module-info.java\" "
	        + "\"" + OUTPUT_DIR +  File.separator + "p/X.java\"",
	        "",
	        "",
	        true);
	}

	public void test002_moduleNotRead() {
		runNegativeModuleTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					import module java.sql;
					public class X {
						public static void main(String[] args) {
							@SuppressWarnings("unused")
							Connection con = null;
						}
					}
					""",
				"module-info.java",
				"""
					module mod.one {
					}
					"""
	        },
			" -23 --enable-preview \"" + OUTPUT_DIR +  File.separator + "module-info.java\" "
	        + "\"" + OUTPUT_DIR +  File.separator + "p/X.java\"",
	        "",
	        """
				----------
				1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/X.java (at line 2)
					import module java.sql;
					              ^^^^^^^^
				You are using a preview language feature that may or may not be supported in a future release
				----------
				2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/p/X.java (at line 2)
					import module java.sql;
					              ^^^^^^^^
				Module mod.one does not read module java.sql
				----------
				3. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/p/X.java (at line 6)
					Connection con = null;
					^^^^^^^^^^
				Connection cannot be resolved to a type
				----------
				3 problems (2 errors, 1 warning)
				""",
	        true,
	        "read");
	}

	public void test003_unresolvableModule() {
		runNegativeModuleTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					import module missing;
					@SuppressWarnings("preview")
					public class X {
						public static void main(String[] args) {
							@SuppressWarnings("unused")
							Connection con = null;
						}
					}
					""",
				"module-info.java",
				"""
					module mod.one {
					}
					"""
	        },
			" -23 --enable-preview \"" + OUTPUT_DIR +  File.separator + "module-info.java\" "
	        + "\"" + OUTPUT_DIR +  File.separator + "p/X.java\"",
	        "",
	        """
				----------
				1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/p/X.java (at line 2)
					import module missing;
					              ^^^^^^^
				The import missing cannot be resolved
				----------
				2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/p/X.java (at line 7)
					Connection con = null;
					^^^^^^^^^^
				Connection cannot be resolved to a type
				----------
				2 problems (2 errors)
				""",
	        true,
	        "imported module not found");
	}

	public void test004_selfImport_OK() {
		String modsDir = OUTPUT_DIR +  File.separator + "mods";
		String modOneDir = modsDir + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, modOneDir + File.separator + "p1", "X1.java",
				"""
					package p1;
					public class X1 {}
					""");
		writeFileCollecting(files, modOneDir + File.separator + "p2", "X2.java",
				"""
					package p2;
					public class X2 {}
					""");
		writeFileCollecting(files, modOneDir + File.separator + "p", "X.java",
				"""
					package p;
					import module mod.one;
					@SuppressWarnings("preview")
					public class X {
						X1 x1;
						X2 x2;
					}
					""");
		writeFileCollecting(files, modOneDir, "module-info.java",
				"""
					module mod.one {
						exports p1;
						exports p2 to mod.one;
					}
					""");
		StringBuilder commandLine = new StringBuilder();
		commandLine.append(" -23 --enable-preview ");
		runConformModuleTest(
				files,
				commandLine,
				"",
				"",
				OUTPUT_DIR,
				JavacTestOptions.JavacHasABug.JavacBugReflexiveRead);// javac 23 ea build 24 reports "module mod.one does not read: mod.one"
	}

	public void test005_selfImport_NOK() {
		String modsDir = OUTPUT_DIR +  File.separator + "mods";
		String modOneDir = modsDir + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, modOneDir + File.separator + "p1", "X1.java",
				"""
					package p1;
					public class X1 {}
					""");
		writeFileCollecting(files, modOneDir + File.separator + "p", "X.java",
				"""
					package p;
					import module mod.one;
					@SuppressWarnings("preview")
					public class X {
						X1 x1;
					}
					""");
		writeFileCollecting(files, modOneDir, "module-info.java",
				"""
					module mod.one {
						exports p1 to mod.other;
					}
					""");
		StringBuilder commandLine = new StringBuilder();
		commandLine.append(" -23 --enable-preview ");

		runNegativeModuleTest(
				files,
				commandLine,
				"",
				"""
				----------
				1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/mods/mod.one/p/X.java (at line 5)
					X1 x1;
					^^
				X1 cannot be resolved to a type
				----------
				1 problem (1 error)
				""",
				"cannot find symbol"); // javac 23 ea build 24 additionally reports "module mod.one does not read: mod.one"
	}
}
