/*******************************************************************************
 * Copyright (c) 2018 IBM Corporation and others.
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

import java.io.File;
import java.io.IOException;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.core.util.ClassFormatException;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "rawtypes" })
public class JEP181NestTest extends AbstractComparableTest {

	String versionString = null;

public JEP181NestTest(String name) {
	super(name);
}
// No need for a tearDown()
protected void setUp() throws Exception {
	super.setUp();
	this.versionString = AbstractCompilerTest.getVersionString(this.complianceLevel);
}

/*
 * Toggle compiler in mode -11
 */
// Static initializer to specify tests subset using TESTS_* static variables
// All specified tests which do not belong to the class are skipped...
static {
//	TESTS_NAMES = new String[] { "test055" };
//	TESTS_NUMBERS = new int[] { 50, 51, 52, 53 };
//	TESTS_RANGE = new int[] { 34, 38 };
}
private static final String[] test_series_classic = JEP181NestTest.getTestSeriesClassic();

public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_11);
}
private static String[] getTestSeriesClassic() {
	return new String[] {
			"pack1/X.java",
			"package pack1;\n" +
					"public class X {\n" +
					"	public class Y {\n" +
					"		class Z {}\n" +
					"	}\n" +
					"	public static class A {\n" +
					"		public static class B {}\n" +
					"		public class C {}\n" +
					"	}\n" +
					"	public static void main(String[] args) {\n" +
					"		System.out.println(\"SUCCESS\");\n" +
					"	}\n" +
					"	public void foo() {\n" +
					"		System.out.println(\"foo\");\n" +
					"	}\n" +
					"}\n",
	};
}
private void verifyClassFile(String expectedOutput, String classFileName, int mode, boolean positive) throws IOException,
ClassFormatException {
	File f = new File(OUTPUT_DIR + File.separator + classFileName);
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", mode);
	int index = result.indexOf(expectedOutput);
	if (positive) {
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(result, 3));
			System.out.println("...");
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, result);
		}
	} else {
		if (index != -1) {
			assertEquals("Unexpected contents", "", result);
		}
	}
}

private void verifyClassFile(String expectedOutput, String classFileName, int mode) throws IOException,
	ClassFormatException {
	verifyClassFile(expectedOutput, classFileName, mode, true);
}
private void verifyNegativeClassFile(String unExpectedOutput, String classFileName, int mode) throws IOException,
ClassFormatException {
	verifyClassFile(unExpectedOutput, classFileName, mode, false);
}
public void testBug535851_001() throws Exception {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_11);

	this.runConformTest(
			JEP181NestTest.test_series_classic,
			"SUCCESS",
			options
	);

	String expectedPartialOutput =
			"Nest Members:\n" + 
			"   #37 pack1/X$A,\n" + 
			"   #44 pack1/X$A$B,\n" + 
			"   #46 pack1/X$A$C,\n" + 
			"   #40 pack1/X$Y,\n" + 
			"   #48 pack1/X$Y$Z\n";
	verifyClassFile(expectedPartialOutput, "pack1/X.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug535851_002() throws Exception {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_11);

	this.runConformTest(
		JEP181NestTest.test_series_classic,
		"SUCCESS",
		options
	);

	String expectedPartialOutput =
		"Nest Host: #17 pack1/X\n";
	verifyClassFile(expectedPartialOutput, "pack1/X$A.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug535851_003() throws Exception {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_11);

	this.runConformTest(
		JEP181NestTest.test_series_classic,
		"SUCCESS",
		options
	);

	String unExpectedPartialOutput =
		"NestMembers:";
	verifyNegativeClassFile(unExpectedPartialOutput, "pack1/X$A.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug535851_004() throws Exception {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_11);

	this.runConformTest(
			JEP181NestTest.test_series_classic,
			"SUCCESS",
			options
	);

	String expectedPartialOutput =
		"Nest Host: #24 pack1/X\n";
	verifyClassFile(expectedPartialOutput, "pack1/X$Y$Z.class", ClassFileBytesDisassembler.SYSTEM);
}
// vanilla anonymous declaration
public void testBug535851_005() throws Exception {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_11);
	String[] files = new String[] {
			"pack1/X.java",
			"package pack1;\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(\"SUCCESS\");\n" +
			"	}\n" +
			"	public void foo() {\n" +
			"		Y y = new Y() {\n" +
			"		    void bar() {}\n" +
			"		};\n" +
			"       System.out.println(y.toString());\n" +
			"	}\n" +
			"}\n" +
			"abstract class Y {\n" +
			"	abstract void bar();\n" +
			"}\n",
	};
	this.runConformTest(
			files,
			"SUCCESS",
			options
	);

	String expectedPartialOutput =
		"Nest Members:\n" + 
		"   #33 pack1/X$1\n";
	verifyClassFile(expectedPartialOutput, "pack1/X.class", ClassFileBytesDisassembler.SYSTEM);

	expectedPartialOutput = "Nest Host: #23 pack1/X\n";
	verifyClassFile(expectedPartialOutput, "pack1/X$1.class", ClassFileBytesDisassembler.SYSTEM);
	verifyNegativeClassFile(expectedPartialOutput, "pack1/Y.class", ClassFileBytesDisassembler.SYSTEM);
}
// anonymous declaration inside another anonymous declaration
public void testBug535851_006() throws Exception {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_11);
	String[] files = new String[] {
			"pack1/X.java",
			"package pack1;\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(\"SUCCESS\");\n" +
			"	}\n" +
			"	public void foo() {\n" +
			"		Y y = new Y() {\n" +
			"		    void bar() {\n" +
			"		        Y y1 = new Y() {\n" +
			"		           void bar() {}\n" +
			"		        };\n" +
			"               System.out.println(y1.toString());\n" +
			"	        }\n" +
			"		};\n" +
			"       System.out.println(y.toString());\n" +
			"	}\n" +
			"}\n" +
			"abstract class Y {\n" +
			"	abstract void bar();\n" +
			"}\n",
	};
	this.runConformTest(
			files,
			"SUCCESS",
			options
	);

	String expectedPartialOutput =
			"Nest Members:\n" + 
			"   #33 pack1/X$1,\n" + 
			"   #48 pack1/X$1$1\n";
	verifyClassFile(expectedPartialOutput, "pack1/X.class", ClassFileBytesDisassembler.SYSTEM);

	expectedPartialOutput = "Nest Host: #48 pack1/X\n";
	verifyClassFile(expectedPartialOutput, "pack1/X$1.class", ClassFileBytesDisassembler.SYSTEM);
	expectedPartialOutput = "Nest Host: #28 pack1/X\n";
	verifyClassFile(expectedPartialOutput, "pack1/X$1$1.class", ClassFileBytesDisassembler.SYSTEM);
	verifyNegativeClassFile(expectedPartialOutput, "pack1/Y.class", ClassFileBytesDisassembler.SYSTEM);
}

// lambda with anonymous inside anonymous
public void testBug535851_007() throws Exception {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_11);
	String[] files = new String[] {
			"pack1/X.java",
			"package pack1;\n" +
			"\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		System.out.println(\"SUCCESS\");\n" +
			"	}\n" +
			"	public void foo() {\n" +
			"		I i = ()->{\n" +
			"			Y y = new Y() {	\n" +
			"				@Override\n" +
			"				void bar() {\n" +
			"					Y y1 = new Y() {\n" +
			"						@Override\n" +
			"						void bar() {}\n" +
			"					};\n" +
			"					System.out.println(y1);\n" +
			"				}\n" +
			"			};\n" +
			"			System.out.println(y.toString());\n" +
			"		};\n" +
			"		i.apply();\n" +
			"	}\n" +
			"}\n" +
			"interface I {\n" +
			"	void apply();\n" +
			"}\n" +
			"abstract class Y {\n" +
			"	abstract void bar();\n" +
			"}\n",
	};
	this.runConformTest(
			files,
			"SUCCESS",
			options
	);

	String expectedPartialOutput =
			"Nest Members:\n" + 
			"   #44 pack1/X$1,\n" + 
			"   #77 pack1/X$1$1\n";
	verifyClassFile(expectedPartialOutput, "pack1/X.class", ClassFileBytesDisassembler.SYSTEM);

	expectedPartialOutput = "Nest Host: #42 pack1/X\n";
	verifyClassFile(expectedPartialOutput, "pack1/X$1.class", ClassFileBytesDisassembler.SYSTEM);
	expectedPartialOutput = "Nest Host: #28 pack1/X\n";
	verifyClassFile(expectedPartialOutput, "pack1/X$1$1.class", ClassFileBytesDisassembler.SYSTEM);
	verifyNegativeClassFile(expectedPartialOutput, "pack1/Y.class", ClassFileBytesDisassembler.SYSTEM);
}
// type declaration in method
public void testBug535851_008() throws Exception {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_11);
	String[] files = new String[] {
			"pack1/X.java",
			"package pack1;\n" +
			"\n" +
			"public class X {\n"+
			"   public void foo() {\n"+
			"       class Y {\n"+
			"           // nothing\n"+
			"       }\n"+
			"       Y y = new Y();\n"+
			"       System.out.println(\"SUCCESS\");\n"+
			"   }\n"+
			"   public static void main(String[] args) {\n"+
			"       new X().foo();\n"+
			"   }\n"+
			"}\n",
	};
	this.runConformTest(
			files,
			"SUCCESS",
			options
	);

	String expectedPartialOutput =
			"Nest Members:\n" + 
			"   #15 pack1/X$1Y\n";
	verifyClassFile(expectedPartialOutput, "pack1/X.class", ClassFileBytesDisassembler.SYSTEM);

	expectedPartialOutput = "Nest Host: #22 pack1/X\n";
	verifyClassFile(expectedPartialOutput, "pack1/X$1Y.class", ClassFileBytesDisassembler.SYSTEM);
}
public static Class testClass() {
	return JEP181NestTest.class;
}
}
