/*******************************************************************************
 * Copyright (c) 2003, 2011 IBM Corporation and others.
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

import junit.framework.Test;
public class TryStatement17Test extends AbstractRegressionTest {

static {
//	TESTS_NAMES = new String[] { "test000" };
//	TESTS_NUMBERS = new int[] { 40, 41, 43, 45, 63, 64 };
//	TESTS_RANGE = new int[] { 11, -1 };
}
public TryStatement17Test(String name) {
	super(name);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_7);
}
public void test001() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.*;\n" + 
			"\n" + 
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		try {\n" + 
			"			System.out.println();\n" + 
			"			Reader r = new FileReader(args[0]);\n" + 
			"			r.read();\n" + 
			"		} catch(IOException | FileNotFoundException e) {\n" + 
			"			e.printStackTrace();\n" + 
			"		}\n" + 
			"	}\n" + 
			"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 9)\n" + 
		"	} catch(IOException | FileNotFoundException e) {\n" + 
		"	        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"The exception FileNotFoundException is already caught by the exception IOException\n" + 
		"----------\n");
}
public void test002() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.*;\n" + 
			"\n" + 
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		try {\n" + 
			"			System.out.println();\n" + 
			"			Reader r = new FileReader(args[0]);\n" + 
			"			r.read();\n" + 
			"		} catch(FileNotFoundException | FileNotFoundException | IOException e) {\n" + 
			"			e.printStackTrace();\n" + 
			"		}\n" + 
			"	}\n" + 
			"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 9)\n" + 
		"	} catch(FileNotFoundException | FileNotFoundException | IOException e) {\n" + 
		"	        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"The exception FileNotFoundException is already caught by the exception FileNotFoundException\n" + 
		"----------\n");
}
public void test003() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.*;\n" + 
			"\n" + 
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		try {\n" + 
			"			System.out.println();\n" + 
			"			Reader r = new FileReader(args[0]);\n" + 
			"			r.read();\n" + 
			"		} catch(FileNotFoundException e) {" +
			"			e.printStackTrace();\n" + 
			"		} catch(FileNotFoundException | IOException e) {\n" + 
			"			e.printStackTrace();\n" + 
			"		}\n" + 
			"	}\n" + 
			"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 10)\n" + 
		"	} catch(FileNotFoundException | IOException e) {\n" + 
		"	        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Unreachable catch block for FileNotFoundException. It is already handled by the catch block for FileNotFoundException\n" + 
		"----------\n");
}
public void test004() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.*;\n" + 
			"\n" + 
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		try {\n" + 
			"			System.out.println();\n" + 
			"			Reader r = new FileReader(args[0]);\n" + 
			"			r.read();\n" + 
			"		} catch(RuntimeException | Exception e) {" +
			"			e.printStackTrace();\n" + 
			"		} catch(FileNotFoundException | IOException e) {\n" + 
			"			e.printStackTrace();\n" + 
			"		}\n" + 
			"	}\n" + 
			"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 10)\n" + 
		"	} catch(FileNotFoundException | IOException e) {\n" + 
		"	        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Unreachable catch block for FileNotFoundException. It is already handled by the catch block for Exception\n" + 
		"----------\n");
}
public void test005() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.io.*;\n" + 
			"\n" + 
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		try {\n" + 
			"			System.out.println();\n" + 
			"			Reader r = new FileReader(\"Zork\");\n" + 
			"			r.read();\n" + 
			"		} catch(NumberFormatException | RuntimeException e) {\n" + 
			"			e.printStackTrace();\n" + 
			"		} catch(FileNotFoundException | IOException e) {\n" + 
			"			// ignore\n" + 
			"		}\n" + 
			"	}\n" + 
			"}",
		},
		"");
}
public void test006() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.io.*;\n" + 
			"\n" + 
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		try {\n" + 
			"		} catch(IOException | RuntimeException e) {\n" + 
			"			e = new IOException();\n" +
			"		}\n" + 
			"	}\n" + 
			"}",
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	e = new IOException();\n" + 
		"	^\n" + 
		"The parameter e of a multi-catch block cannot be assigned\n" + 
		"----------\n");
}
public static Class testClass() {
	return TryStatement17Test.class;
}
}
