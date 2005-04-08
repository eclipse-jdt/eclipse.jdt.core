/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;
import java.io.File;
import java.io.IOException;

import junit.framework.Test;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;

public class ArrayTest extends AbstractRegressionTest {

	public ArrayTest(String name) {
		super(name);
	}

	public static Test suite() {
		return setupSuite(testClass());
	}
	
	public static Class testClass() {
		return ArrayTest.class;
	}

public void test001() {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" + 
		"public class X {\n" + 
		"  int[] x= new int[] {,};\n" + 
		"}\n",
	});
}

/**
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=28615
 */
public void test002() {
	this.runConformTest(
		new String[] {
			"A.java",
			"public class A {\n" +
			"    public static void main(String[] args) {\n" +
			"        float[] tab = new float[] {-0.0f};\n" +
			"        System.out.print(tab[0]);\n" +
			"    }\n" +
			"}",
		},
		"-0.0");
}
/**
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=28615
 */
public void test003() {
	this.runConformTest(
		new String[] {
			"A.java",
			"public class A {\n" +
			"    public static void main(String[] args) {\n" +
			"        float[] tab = new float[] {0.0f};\n" +
			"        System.out.print(tab[0]);\n" +
			"    }\n" +
			"}",
		},
		"0.0");
}
/**
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=28615
 */
public void test004() {
	this.runConformTest(
		new String[] {
			"A.java",
			"public class A {\n" +
			"    public static void main(String[] args) {\n" +
			"        int[] tab = new int[] {-0};\n" +
			"        System.out.print(tab[0]);\n" +
			"    }\n" +
			"}",
		},
		"0");
}
/**
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=37387
 */
public void test005() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	 private static final Object X[] = new Object[]{null,null};\n" +
			"    public static void main(String[] args) {\n" +
			"		System.out.println(\"SUCCESS\");\n" +
			"    }\n" +
			"}\n",
		},
		"SUCCESS");
		
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String actualOutput = null;
	try {
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED); 
	} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
		assertTrue("ClassFormatException", false);
	} catch (IOException e) {
		assertTrue("IOException", false);
	}
	
	String expectedOutput = 
		"  static {};\n" + 
		"    0  iconst_2\n" + 
		"    1  anewarray java/lang/Object [4]\n" + 
		"    4  putstatic X.X [Ljava/lang/Object; [10]\n" + 
		"    7  return\n" + 
		"      Line numbers:\n" + 
		"        [pc: 0, line: 2]\n" + 
		"        [pc: 7, line: 1]\n";

	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 3));
	}
	if (index == -1) {
		assertEquals("unexpected bytecode sequence", expectedOutput, actualOutput);
	}
}
/**
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=80597
 */
public void test006() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo() {\n" +
			"		char[][][] array = new char[][][10];\n" +
			"	}\n" +
			"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	char[][][] array = new char[][][10];\n" + 
		"	                                ^^\n" + 
		"Cannot specify an array dimension after an empty dimension\n" + 
		"----------\n");
}
/**
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=85203
 */
public void test007() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"	static long lfield;\n" + 
			"	\n" + 
			"	public static void main(String[] args) {\n" + 
			"		lfield = args.length;\n" + 
			"		lfield = args(args).length;\n" + 
			"		\n" + 
			"	}\n" + 
			"	static String[] args(String[] args) {\n" + 
			"		return args;\n" + 
			"	}\n" + 
			"}\n",
		},
		"");
}
/**
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=85125
 */
public void test008() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	public String getTexts(int i) [] {\n" +
			"		String[] texts = new String[1];\n" +
			"		return texts; \n" +
			"	}\n" +
			"    public static void main(String[] args) {\n" +
			"		System.out.println(\"SUCCESS\");\n" +
			"    }\n" +
			"}\n",
		},
		"SUCCESS");
}
}
