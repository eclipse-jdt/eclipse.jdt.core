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

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.util.Util;

import junit.framework.Test;

public class ModuleCompilationTests extends BatchCompilerTest {

	static {
//		 TESTS_NAMES = new String[] { "test034" };
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
    		"	^^^^^^^^\n" + 
    		"java.sql cannot be resolved to a type\n" + 
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
						"	requires mod.two;\n" +
						"	requires public java.sql;\n" +
						"}");
		writeFile(moduleLoc + File.separator + "p", "X.java", 
						"package p;\n" +
						"import q.Y;\n" +
						"public class X {\n" +
						"	public static java.sql.Connection getConnection() {\n" +
						"		return Y.con;\n" +
						"	}\n" +
						"}");
		moduleLoc = directory + File.separator + "mod.two";
		writeFile(moduleLoc, "module-info.java", 
						"module mod.two { \n" +
						"	exports q;\n" +
						"	requires java.sql;\n" +
						"}");
		writeFile(moduleLoc + File.separator + "q", "Y.java", 
						"package q;\n" +
						"public class Y {\n" +
						"   public static java.sql.Connection con = null;\n" +
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
	public void test008a() {
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
		// This one is not exported (i.e. internal to this module)
		writeFile(moduleLoc + File.separator + "p1", "X1.java", 
				"package p1;\n" +
				"public class X1 {\n" +
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
			.append(" -mp \"")
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
		writeFile(moduleLoc + File.separator + "r", "Z.java", 
						"package r;\n" +
						"public class Z extends Object {\n" +
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
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=495500
	//-source 9
	public void testBug495500a() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"/** */\n" +
				"public class X {\n" +
				"}",
			},
	  "\"" + OUTPUT_DIR +  File.separator + "X.java\""
	  + " -9 -d \"" + OUTPUT_DIR + "\"",
	  "",
	  "",
	  true);
		String expectedOutput = "// Compiled from X.java (version 9 : 53.0, super bit)";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
	}
	//-source 8 -target 9
	public void testBug495500b() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"/** */\n" +
				"public class X {\n" +
				"}",
			},
	"\"" + OUTPUT_DIR +  File.separator + "X.java\""
	+ " -9 -source 8 -target 9 -d \"" + OUTPUT_DIR + "\"",
	"",
	"",
	true);
		String expectedOutput = "// Compiled from X.java (version 9 : 53.0, super bit)";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
	}
	// compliance 9 -source 9 -target 9
	public void testBug495500c() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"/** */\n" +
				"public class X {\n" +
				"}",
			},
	"\"" + OUTPUT_DIR +  File.separator + "X.java\""
	+ " -9 -source 9 -target 9 -d \"" + OUTPUT_DIR + "\"",
	"",
	"",
	true);
		String expectedOutput = "// Compiled from X.java (version 9 : 53.0, super bit)";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
	}
	public void test014() {
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
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -modulesourcepath " + "\"" + directory + "\"")
			.append(" --add-exports mod.one/p=mod.two");

		runConformTest(new String[]{}, 
				buffer.toString(), 
				"",
				"",
				false);
	}
	public void test015() {
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
						"	requires java.sql;\n" +
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
			.append(" -modulesourcepath " + "\"" + directory + "\"")
			.append(" --add-exports mod.one/p=mod.two");

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
	public void test016() {
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
			.append(" -modulesourcepath " + "\"" + directory + "\"")
			.append(" --add-exports mod.one/p=mod.two")
			.append(" --add-reads mod.two=mod.one");

		runConformTest(new String[]{}, 
				buffer.toString(), 
				"",
				"",
				false);
	}
	public void test017() {
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
			.append(" -modulesourcepath " + "\"" + directory + "\"")
			.append(" --add-exports mod.one/p=mod.three")
			.append(" --add-reads mod.two=mod.one");

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
	public void test018() {
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
						"}");
		writeFile(moduleLoc + File.separator + "q", "Y.java", 
						"package q;\n" +
						"public class Y {\n" +
						"   java.sql.Connection con = p.X.getConnection();\n" +
						"}");
		moduleLoc = directory + File.separator + "mod.three";
		writeFile(moduleLoc, "module-info.java", 
						"module mod.three { \n" +
						"	requires java.base;\n" +
						"}");
		writeFile(moduleLoc + File.separator + "r", "Z.java", 
						"package r;\n" +
						"public class Z {\n" +
						"   java.sql.Connection con = p.X.getConnection();\n" +
						"}");


		StringBuffer buffer = new StringBuffer();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -modulesourcepath " + "\"" + directory + "\"")
			.append(" --add-exports mod.one/p=mod.two,mod.three")
			.append(" --add-reads mod.two=mod.one")
			.append(" --add-reads mod.three=mod.one");

		runConformTest(new String[]{}, 
				buffer.toString(), 
				"",
				"",
				false);
	}
	/*
	 * Unnamed module tries to access a type from an unexported package successfully. 
	 */
	public void test019() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		writeFile(moduleLoc + File.separator + "p", "X.java", 
						"package p;\n" +
						"public abstract class X extends sun.management.FileSystem {\n" +
						"}");

		StringBuffer buffer = new StringBuffer();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -sourcepath " + "\"" + moduleLoc + "\" ")
			.append(moduleLoc + File.separator + "p" + File.separator + "X.java");

		runConformTest(new String[]{}, 
				buffer.toString(), 
				"",
				"",
				false);
	}
	public void test020() {
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

		StringBuffer buffer = new StringBuffer();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -modulesourcepath " + "\"" + directory + "\"")
			.append(" --add-exports mod.one=mod.two,mod.three");

		runNegativeTest(new String[]{}, 
				buffer.toString(),
				"",
				"incorrectly formatted option: --add-exports mod.one=mod.two,mod.three\n",
				false);
	}
	public void test021() {
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

		StringBuffer buffer = new StringBuffer();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -modulesourcepath " + "\"" + directory + "\"")
			.append(" --add-reads mod.one/mod.two");

		runNegativeTest(new String[]{}, 
				buffer.toString(), 
				"",
				"incorrectly formatted option: --add-reads mod.one/mod.two\n",
				false);
	}
	public void test022() {
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

		StringBuffer buffer = new StringBuffer();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -modulesourcepath " + "\"" + directory + "\"")
			.append(" --add-exports mod.one/p=mod.three")
			.append(" --add-exports mod.one/p=mod.three");

		runNegativeTest(new String[]{}, 
				buffer.toString(), 
				"",
				"can specify a package in a module only once with --add-export\n",
				false);
	}
	public void test023() {
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

		StringBuffer buffer = new StringBuffer();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append("\"" + OUTPUT_DIR +  File.separator + "module-info.java\" ")
			.append(" -extdirs " + OUTPUT_DIR + File.separator + "src");

		runNegativeTest(new String[]{}, 
				buffer.toString(), 
				"",
				"option -extdirs not supported at compliance level 9 and above\n",
				false);
	}
	public void test024() {
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

		StringBuffer buffer = new StringBuffer();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" \"" + OUTPUT_DIR +  File.separator + "module-info.java\" ")
			.append(" -bootclasspath " + OUTPUT_DIR + File.separator + "src");

		runNegativeTest(new String[]{}, 
				buffer.toString(), 
				"",
				"option -bootclasspath not supported at compliance level 9 and above\n",
				false);
	}
	public void test025() {
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

		StringBuffer buffer = new StringBuffer();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append("\"" + OUTPUT_DIR +  File.separator + "module-info.java\" ")
			.append(" -endorseddirs " + OUTPUT_DIR + File.separator + "src");

		runNegativeTest(new String[]{}, 
				buffer.toString(), 
				"",
				"option -endorseddirs not supported at compliance level 9 and above\n",
				false);
	}
	public void test026() {
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
		String javaHome = System.getProperty("java.home");
		StringBuffer buffer = new StringBuffer();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -system \"").append(javaHome).append("\"")
			.append(" \"" + moduleLoc +  File.separator + "module-info.java\" ");

		runConformTest(new String[]{}, 
				buffer.toString(), 
				"",
				"",
				false);
	}
	public void test027() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		writeFile(moduleLoc, "module-info.java", 
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"}");
		String javaHome = System.getProperty("java.home");
		StringBuffer buffer = new StringBuffer();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -system \"").append(javaHome).append(File.separator)
			.append("lib\"")
			.append(" \"" + moduleLoc +  File.separator + "module-info.java\" ");

		runNegativeTest(new String[]{}, 
				buffer.toString(), 
				"",
				"----------\n"+
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 1)\n"+
				"	module mod.one { \n"+
				"	^\n"+
				"The type java.lang.Object cannot be resolved. It is indirectly referenced from required .class files\n"+
				"----------\n"+
				"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 2)\n"+
				"	requires java.base;\n"+
				"	         ^^^^^^^^^\n"+
				"java.base cannot be resolved to a module\n"+
				"----------\n"+
				"2 problems (2 errors)\n",
				false);
	}
	/**
	 * Mixed case of exported and non exported packages being referred to in another module
	 */
	public void test028() {
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
		writeFile(moduleLoc + File.separator + "r", "Z.java", 
						"package r;\n" +
						"public class Z extends Object {\n" +
						"	p.X x = null;\n" +
						"	p1.X1 x1 = null;\n" +
						"}");

		StringBuffer buffer = new StringBuffer();
		buffer.append("-d " + outDir )
		.append(" -9 ")
		.append(" -mp \"")
		.append(Util.getJavaClassLibsAsString())
		.append(modDir.getAbsolutePath())
		.append("\" ")
		.append(" -modulesourcepath " + "\"" + srcDir + "\"");

		runNegativeTest(new String[]{},
				buffer.toString(), 
				"",
				"----------\n" + 
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.three/r/Z.java (at line 4)\n"+
				"	p1.X1 x1 = null;\n" + 
				"	^^\n" + 
				"p1 cannot be resolved to a type\n" + 
				"----------\n" + 
				"1 problem (1 error)\n",
				false);
	}
	public void test029() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		writeFile(moduleLoc, "module-info.java", 
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"	requires java.sql;\n" +
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
			.append(" -modulesourcepath " + "\"" + directory + "\"")
			.append(" --add-exports mod.one/p=mod.two,mod.three")
			.append(" --add-reads mod.two=mod.one");

		runNegativeTest(new String[]{}, 
			buffer.toString(), 
			"",
			"----------\n"+
			"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/q/Y.java (at line 3)\n"+
			"	java.sql.Connection con = p.X.getConnection();\n"+
			"	^^^^^^^^\n"+
			"java.sql cannot be resolved to a type\n"+
			"----------\n"+
			"1 problem (1 error)\n",
			false);
	}
	public void test030() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		writeFile(moduleLoc, "module-info.java", 
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"	requires java.sql;\n" +
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
						"}");
		writeFile(moduleLoc + File.separator + "q", "Y.java", 
						"package q;\n" +
						"import java.sql.*;\n" +
						"public class Y {\n" +
						"   Connection con = null;\n" +
						"}");

		StringBuffer buffer = new StringBuffer();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -modulesourcepath " + "\"" + directory + "\"")
			.append(" --add-exports mod.one/p=mod.two,mod.three")
			.append(" --add-reads mod.two=mod.one");

		runNegativeTest(new String[]{}, 
			buffer.toString(), 
			"",
			"----------\n"+
			"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/q/Y.java (at line 2)\n"+
			"	import java.sql.*;\n"+
			"	       ^^^^^^^^\n"+
			"The import java.sql cannot be resolved\n"+
			"----------\n"+
			"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/q/Y.java (at line 4)\n"+
			"	Connection con = null;\n"+
			"	^^^^^^^^^^\n"+
			"Connection cannot be resolved to a type\n"+
			"----------\n"+
			"2 problems (2 errors)\n",
			false);
	}
	public void test031() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		writeFile(moduleLoc, "module-info.java", 
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"	requires java.sql;\n" +
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
						"}");
		writeFile(moduleLoc + File.separator + "q", "Y.java", 
						"package q;\n" +
						"import java.sql.Connection;\n" +
						"public class Y {\n" +
						"   Connection con = null;\n" +
						"}");

		StringBuffer buffer = new StringBuffer();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -modulesourcepath " + "\"" + directory + "\"")
			.append(" --add-exports mod.one/p=mod.two,mod.three")
			.append(" --add-reads mod.two=mod.one");

		runNegativeTest(new String[]{}, 
			buffer.toString(), 
			"",
			"----------\n"+
			"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/q/Y.java (at line 2)\n"+
			"	import java.sql.Connection;\n"+
			"	       ^^^^^^^^\n"+
			"The import java.sql cannot be resolved\n"+
			"----------\n"+
			"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/q/Y.java (at line 4)\n"+
			"	Connection con = null;\n"+
			"	^^^^^^^^^^\n"+
			"Connection cannot be resolved to a type\n"+
			"----------\n"+
			"2 problems (2 errors)\n",
			false);
	}
	public void test032() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		writeFile(moduleLoc, "module-info.java", 
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"}");
		writeFile(moduleLoc, "X.java", 
						"public class X {\n" +
						"	public static class Inner {\n" +
						"	}\n" +
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
	/**
	 * Test that a module can't access types/packages in a plain Jar put in classpath
	 */
	public void test033() {
		File libDir = new File(LIB_DIR);
		Util.delete(libDir); // make sure we recycle the libs
 		libDir.mkdirs();
		try {
			Util.createJar(
				new String[] {
					"a/A.java",
					"package a;\n" +
					"public class A {\n" +
					"}"
				},
				LIB_DIR + "/lib1.jar",
				JavaCore.VERSION_9);
		} catch (IOException e) {
			// ignore
		}
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		writeFile(moduleLoc, "module-info.java", 
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"	requires java.sql;\n" +
						"}");
		writeFile(moduleLoc + File.separator + "p", "X.java", 
						"package p;\n" +
						"public class X extends a.A {\n" +
						"}");

		StringBuffer buffer = new StringBuffer();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append(LIB_DIR).append(File.separator).append("lib1.jar").append(File.pathSeparator).append("\" ")
			.append(" -modulesourcepath " + "\"" + directory + "\"");
		runNegativeTest(new String[]{}, 
				buffer.toString(), 
				"",
				"----------\n" + 
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/X.java (at line 2)\n" + 
				"	public class X extends a.A {\n" + 
				"	                       ^\n" + 
				"a cannot be resolved to a type\n" + 
				"----------\n" + 
				"1 problem (1 error)\n",
				false);
	}
	/**
	 * Test that a module can access types/packages in a plain Jar put in modulepath,
	 * this upgrading it to an automatic module
	 */
	public void test034() {
		File libDir = new File(LIB_DIR);
		Util.delete(libDir); // make sure we recycle the libs
 		libDir.mkdirs();
		try {
			Util.createJar(
				new String[] {
					"a/A.java",
					"package a;\n" +
					"public class A {\n" +
					"}"
				},
				LIB_DIR + "/lib1.jar",
				JavaCore.VERSION_9);
		} catch (IOException e) {
			// ignore
		}
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		writeFile(moduleLoc, "module-info.java", 
						"module mod.one { \n" +
						"	requires java.base;\n" +
						"	requires java.sql;\n" +
						"}");
		writeFile(moduleLoc + File.separator + "p", "X.java", 
						"package p;\n" +
						"public class X extends a.A {\n" +
						"}");

		StringBuffer buffer = new StringBuffer();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString()).append("\" ")
			.append("-mp \"")
			.append(LIB_DIR).append("\" ")
			.append(" -modulesourcepath " + "\"" + directory + "\"");
		runConformTest(new String[]{}, 
				buffer.toString(), 
				"",
				"",
				false);
	}
}
