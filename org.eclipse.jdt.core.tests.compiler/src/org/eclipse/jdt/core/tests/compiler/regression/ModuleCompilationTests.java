/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.jdt.core.tests.util.Util;

import junit.framework.Test;

public class ModuleCompilationTests extends BatchCompilerTest {

	static {
//		 TESTS_NAMES = new String[] { "test001" };
		// TESTS_NUMBERS = new int[] { 1 };
		// TESTS_RANGE = new int[] { 298, -1 };
	}

	public ModuleCompilationTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_9);
	}

	public static Class testClass() {
		return ModuleCompilationTests.class;
	}

	protected void writeFile(String directoryName, String fileName, String source) {
		File directory = new File(directoryName);
		if (!directory.exists()) {
			if (!directory.mkdirs()) {
				System.out.println("Could not create " + directoryName);
				return;
			}
		}
		String filePath = directory.getAbsolutePath() + File.separator + fileName;
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
			writer.write(source);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

	public void test001() {
		this.runNegativeTest(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"     java.sql.Connection con = null;\n" +
				"	}\n" +
				"}",
				"module-info.java",
				"module mod.one { \n" +
				"	requires java.base;\n" +
				"}"
	        },
			"\"" + OUTPUT_DIR +  File.separator + "module-info.java\" "
	        + "\"" + OUTPUT_DIR +  File.separator + "p/X.java\"",
	        "",
	        "----------\n" + 
    		"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/p/X.java (at line 4)\n" + 
    		"	java.sql.Connection con = null;\n" + 
    		"	^^^^^^^^^^^^^^^^^^^\n" + 
    		"java.sql.Connection cannot be resolved to a type\n" + 
    		"----------\n" + 
    		"1 problem (1 error)\n",
	        true);
	}
	public void test002() {
		this.runConformTest(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"     java.sql.Connection con = null;\n" +
				"     System.out.println(con);\n" +
				"	}\n" +
				"}",
				"module-info.java",
				"module mod.one { \n" +
				"	requires java.base;\n" +
				"	requires java.sql;\n" +
				"}"
	        },
			"\"" + OUTPUT_DIR +  File.separator + "module-info.java\" "
	        + "\"" + OUTPUT_DIR +  File.separator + "p/X.java\"",
	        "",
	        "",
	        true);
	}
	public void test003() {
		this.runConformTest(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"     java.sql.Connection con = null;\n" +
				"     System.out.println(con);\n" +
				"	}\n" +
				"}",
	        },
	        "\"" + OUTPUT_DIR +  File.separator + "p/X.java\"",
	        "",
	        "",
	        true);
	}
	public void test004() {
		this.runConformTest(
			new String[] {
				"module-info.java",
				"module mod.one { \n" +
				"	requires java.base;\n" +
				"	requires java.sql;\n" +
				"}"
	        },
			"\"" + OUTPUT_DIR +  File.separator + "module-info.java\"",
	        "",
	        "",
	        true);
		String fileName = OUTPUT_DIR + File.separator + "module-info.class";
		assertTrue("Missing modul-info.class: " + fileName, (new File(fileName)).exists());
	}
	public void test005() {
		String out = "bin";
		this.runConformTest(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"	java.sql.Connection con;\n" +
				"}",
				"module-info.java",
				"module mod.one { \n" +
				"	requires java.base;\n" +
				"	requires java.sql;\n" +
				"	requires java.desktop;\n" +
				"}",
				"q/Y.java",
				"package q;\n" +
				"public class Y {\n" +
				"   java.awt.Image image;\n" +
				"}"
	        },
			"\"" + OUTPUT_DIR + File.separator + "module-info.java\" "
			+ "\"" + OUTPUT_DIR + File.separator + "q/Y.java\" "
	        + "\"" + OUTPUT_DIR + File.separator + "p/X.java\" "
	        + "-d " + OUTPUT_DIR + File.separator + out,
	        "",
	        "",
	        true);
		String fileName = OUTPUT_DIR + File.separator + out + File.separator + "module-info.class";
		assertTrue("Missing modul-info.class: " + fileName, (new File(fileName)).exists());
	}
	public void test006() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		writeFile(moduleLoc, "module-info.java", 
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"	requires java.sql;\n" +
						"	requires java.desktop;\n" +
						"}");
		writeFile(moduleLoc + File.separator + "p", "X.java", 
						"package p;\n" +
						"public class X {\n" +
						"	java.sql.Connection con;\n" +
						"}");
		writeFile(moduleLoc + File.separator + "q", "Y.java", 
						"package q;\n" +
						"public class Y {\n" +
						"   java.awt.Image image;\n" +
						"}");

		StringBuffer buffer = new StringBuffer();
			buffer.append("-d " + OUTPUT_DIR + File.separator + out );
			buffer.append(" -9 ");
		buffer.append(" -classpath \"")
		.append(Util.getJavaClassLibsAsString())
		.append("\" ");
		buffer.append(" -modulesourcepath " + "\"" + directory + "\"");
		runConformTest(new String[]{}, buffer.toString(), "", "", false);
	}
	public void test007() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		writeFile(moduleLoc, "module-info.java", 
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"	requires public java.sql;\n" +
						"}");
		writeFile(moduleLoc + File.separator + "p", "X.java", 
						"package p;\n" +
						"public class X {\n" +
						"	public static java.sql.Connection getConnection() {\n" +
						"		return null;\n" +
						"	}\n" +
						"}");
		moduleLoc = directory + File.separator + "mod.two";
		writeFile(moduleLoc, "module-info.java", 
						"module mod.two { \n" +
						"	requires java.base;\n" +
						"	requires mod.one;\n" +
						"}");
		writeFile(moduleLoc + File.separator + "q", "Y.java", 
						"package q;\n" +
						"public class Y {\n" +
						"   java.sql.Connection con = p.X.getConnection();\n" +
						"}");
		
		StringBuffer buffer = new StringBuffer();
			buffer.append("-d " + OUTPUT_DIR + File.separator + out );
			buffer.append(" -9 ");
		buffer.append(" -classpath \"")
		.append(Util.getJavaClassLibsAsString())
		.append("\" ");
		buffer.append(" -modulesourcepath " + "\"" + directory + "\"");
		
		runNegativeTest(new String[]{}, 
				buffer.toString(), 
				"",
				"----------\n" + 
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/q/Y.java (at line 3)\n" + 
				"	java.sql.Connection con = p.X.getConnection();\n" + 
				"	                          ^^^\n" + 
				"The type p.X is not visible\n" + 
				"----------\n" + 
				"1 problem (1 error)\n",
				false);
	}
	public void test008() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		writeFile(moduleLoc, "module-info.java", 
						"module mod.one { \n" +
						"	exports p;\n" +
						"	requires java.base;\n" +
						"	requires public java.sql;\n" +
						"}");
		writeFile(moduleLoc + File.separator + "p", "X.java", 
						"package p;\n" +
						"public class X {\n" +
						"	public static java.sql.Connection getConnection() {\n" +
						"		return null;\n" +
						"	}\n" +
						"}");
		moduleLoc = directory + File.separator + "mod.two";
		writeFile(moduleLoc, "module-info.java", 
						"module mod.two { \n" +
						"	requires java.base;\n" +
						"	requires mod.one;\n" +
						"}");
		writeFile(moduleLoc + File.separator + "q", "Y.java", 
						"package q;\n" +
						"public class Y {\n" +
						"   java.sql.Connection con = p.X.getConnection();\n" +
						"}");

		StringBuffer buffer = new StringBuffer();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -modulesourcepath " + "\"" + directory + "\"");

		runConformTest(new String[]{}, 
				buffer.toString(), 
				"",
				"",
				false);
	}
	public void test009() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		writeFile(moduleLoc, "module-info.java", 
						"module mod.one { \n" +
						"	exports p;\n" +
						"	requires java.base;\n" +
						"	requires public java.sql;\n" +
						"}");
		writeFile(moduleLoc + File.separator + "p", "X.java", 
						"package p;\n" +
						"public class X {\n" +
						"	public static java.sql.Connection getConnection() {\n" +
						"		return null;\n" +
						"	}\n" +
						"}");
		moduleLoc = directory + File.separator + "mod.two";
		writeFile(moduleLoc, "module-info.java", 
						"module mod.two { \n" +
						"	requires java.base;\n" +
						"	requires mod.one;\n" +
						"}");
		writeFile(moduleLoc + File.separator + "q", "Y.java", 
						"package q;\n" +
						"public class Y {\n" +
						"   java.sql.Connection con = p.X.getConnection();\n" +
						"}");

		StringBuffer buffer = new StringBuffer();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -modulesourcepath " + "\"" + directory + "\"");

		runConformTest(new String[]{}, 
				buffer.toString(), 
				"",
				"",
				false);
	}
	private void createReusableModules(String srcDir, String outDir, File modDir) {
		String moduleLoc = srcDir + File.separator + "mod.one";
		writeFile(moduleLoc, "module-info.java", 
						"module mod.one { \n" +
						"	exports p;\n" +
						"	requires java.base;\n" +
						"	requires public java.sql;\n" +
						"}");
		writeFile(moduleLoc + File.separator + "p", "X.java", 
						"package p;\n" +
						"public class X {\n" +
						"	public static java.sql.Connection getConnection() {\n" +
						"		return null;\n" +
						"	}\n" +
						"}");

		moduleLoc = srcDir + File.separator + "mod.two";
		writeFile(moduleLoc, "module-info.java", 
						"module mod.two { \n" +
						"	exports q;\n" +
						"	requires java.base;\n" +
						"	requires mod.one;\n" +
						"}");
		writeFile(moduleLoc + File.separator + "q", "Y.java", 
						"package q;\n" +
						"public class Y {\n" +
						"   java.sql.Connection con = p.X.getConnection();\n" +
						"}");

		StringBuffer buffer = new StringBuffer();
			buffer.append("-d " + outDir )
			.append(" -9 ")
			.append(" -modulepath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -modulesourcepath " + "\"" + srcDir + "\"");

		runConformTest(new String[]{}, 
				buffer.toString(), 
				"",
				"",
				false);

		String fileName = modDir + File.separator + "mod.one.jar";
		try {
			Util.zip(new File(outDir + File.separator + "mod.one"), 
								fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (!modDir.exists()) {
			if (!modDir.mkdirs()) {
				fail("Coult not create folder " + modDir);
			}
		}
		File mod2 = new File(modDir, "mod.two");
		if (!mod2.mkdir()) {
			fail("Coult not create folder " + mod2);
		}
		Util.copy(outDir + File.separator + "mod.two", mod2.getAbsolutePath());

		Util.flushDirectoryContent(new File(outDir));
		Util.flushDirectoryContent(new File(srcDir));
	}
	public void test010() {
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin";
		String srcDir = OUTPUT_DIR + File.separator + "src";
		File modDir = new File(OUTPUT_DIR + File.separator + "mod");
		createReusableModules(srcDir, outDir, modDir);
		String moduleLoc = srcDir + File.separator + "mod.three";
		writeFile(moduleLoc, "module-info.java", 
						"module mod.three { \n" +
						"	requires mod.one;\n" +
						"	requires mod.two;\n" +
						"}");
		writeFile(moduleLoc + File.separator + "p", "Z.java", 
						"package r;\n" +
						"public class Z extends Object {\n" +
						"	p.X x = null;\n" +
						"	q.Y y = null;\n" +
						"}");

		StringBuffer buffer = new StringBuffer();
			buffer.append("-d " + outDir )
			.append(" -9 ")
			.append(" -modulepath \"")
			.append(Util.getJavaClassLibsAsString())
			.append(modDir.getAbsolutePath())
			.append("\" ")
			.append(" -modulesourcepath " + "\"" + srcDir + "\"");

		runConformTest(new String[]{}, 
				buffer.toString(), 
				"",
				"",
				false);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=487421
	public void test011() {
		this.runConformTest(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"   java.lang.SecurityManager man = null;\n" +
				"}",
				"module-info.java",
				"module mod.one { \n" +
				"	requires java.base;\n" +
				"}"
	        },
			"\"" + OUTPUT_DIR +  File.separator + "module-info.java\" "
	        + "\"" + OUTPUT_DIR +  File.separator + "p/X.java\"",
	        "",
	        "",
	        true);
	}
	// Modules used as regular -classpath (as opposed to -modulepath) and module-info referencing
	// those modules are reported as missing.
	public void test012() {
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin";
		String srcDir = OUTPUT_DIR + File.separator + "src";
		File modDir = new File(OUTPUT_DIR + File.separator + "mod");
		createReusableModules(srcDir, outDir, modDir);
		String moduleLoc = srcDir + File.separator + "mod.three";
		writeFile(moduleLoc, "module-info.java", 
						"module mod.three { \n" +
						"	requires mod.one;\n" +
						"	requires mod.two;\n" +
						"}");
		writeFile(moduleLoc + File.separator + "p", "Z.java", 
						"package r;\n" +
						"public class Z extends Object {\n" +
						"	p.X x = null;\n" +
						"	q.Y y = null;\n" +
						"}");

		StringBuffer buffer = new StringBuffer();
			buffer.append("-d " + outDir )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append(modDir + File.separator + "mod.one.jar").append(File.pathSeparator)
			.append(modDir + File.separator + "mod.two").append(File.pathSeparator)
			.append("\" ")
			.append(" -modulesourcepath " + "\"" + srcDir + "\"");

		runNegativeTest(new String[]{},
				buffer.toString(), 
				"",
				"----------\n" + 
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.three/module-info.java (at line 2)\n" + 
				"	requires mod.one;\n" + 
				"	         ^^^^^^^\n" + 
				"mod.one cannot be resolved to a module\n" + 
				"----------\n" + 
				"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.three/module-info.java (at line 3)\n" + 
				"	requires mod.two;\n" + 
				"	         ^^^^^^^\n" + 
				"mod.two cannot be resolved to a module\n" + 
				"----------\n" + 
				"2 problems (2 errors)\n",
				false);
	}
	// Modules used as regular -classpath as opposed to -modulepath. The files being compiled
	// aren't part of any modules (i.e. module-info is missing). The files should be able to
	// reference the types from referenced classpath.
	public void test013() {
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin";
		String srcDir = OUTPUT_DIR + File.separator + "src";
		File modDir = new File(OUTPUT_DIR + File.separator + "mod");
		createReusableModules(srcDir, outDir, modDir);
		String moduleLoc = srcDir + File.separator + "mod.three";
		writeFile(moduleLoc + File.separator + "p", "Z.java", 
						"package r;\n" +
						"public class Z extends Object {\n" +
						"	p.X x = null;\n" +
						"	q.Y y = null;\n" +
						"}");

		StringBuffer buffer = new StringBuffer();
			buffer.append("-d " + outDir )
			.append(" -9")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append(modDir + File.separator + "mod.one.jar").append(File.pathSeparator)
			.append(modDir + File.separator + "mod.two").append(File.pathSeparator)
			.append("\" ")
			.append(" -modulesourcepath " + "\"" + srcDir + "\"");
		runConformTest(new String[]{},
				buffer.toString(), 
				"",
				"",
				false);
	}
}
