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

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;

import junit.framework.Test;

public class EnclosingMethodAttributeTest extends AbstractComparableTest {
	public EnclosingMethodAttributeTest(String name) {
		super(name);
	}

	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "test127" };
//		TESTS_NUMBERS = new int[] { 176 };
//		TESTS_RANGE = new int[] { 169, 180 };
	}

	public static Test suite() {
		Test suite = buildTestSuite(testClass());
		TESTS_COUNTERS.put(testClass().getName(), new Integer(suite.countTestCases()));
		return suite;
	}

	public static Class testClass() {  
		return EnclosingMethodAttributeTest.class;
	}

	public void test001() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"public static void main(String[] args) throws Exception  {\n" +
				"	class MyLocal$A {\n" +
				"		class Member {\n" +
				"		}\n" +
				"	};\n" +
				"	System.out.print(MyLocal$A.Member.class.getEnclosingMethod() != null);\n" +
				"	System.out.print(MyLocal$A.Member.class.getEnclosingConstructor() != null);\n" +
				"\n" +
				"	System.out.print(MyLocal$A.class.getEnclosingMethod()!= null);\n" +
				"	System.out.print(MyLocal$A.class.getEnclosingConstructor() != null);	\n" +
				"	\n" +
				"	System.out.print(X.class.getEnclosingMethod() != null);\n" +
				"	System.out.print(X.class.getEnclosingConstructor() != null);	\n" +
				"}\n" +
				"public Object foo() {\n" +
				"	return new Object() {};\n" +
				"}\n" +
				"}"
			},
			"falsefalsetruefalsefalsefalse");
		
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String actualOutput = null;
		try {
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X$1.class"));
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
		
		String expectedOutput = "  Enclosing Method: #23  #25 X.foo()Ljava/lang/Object;\n";
			
		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 2));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, actualOutput);
		}		
	}
	
	public void test002() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"public static void main(String[] args) throws Exception  {\n" +
				"	class MyLocal$A {\n" +
				"		class Member {\n" +
				"			public Object foo() {\n" +
				"				return new Object() {};\n" +
				"			}\n" +
				"		}\n" +
				"	};\n" +
				"	System.out.print(MyLocal$A.Member.class.getEnclosingMethod() != null);\n" +
				"	System.out.print(MyLocal$A.Member.class.getEnclosingConstructor() != null);\n" +
				"\n" +
				"	System.out.print(MyLocal$A.class.getEnclosingMethod()!= null);\n" +
				"	System.out.print(MyLocal$A.class.getEnclosingConstructor() != null);	\n" +
				"	\n" +
				"	System.out.print(X.class.getEnclosingMethod() != null);\n" +
				"	System.out.print(X.class.getEnclosingConstructor() != null);	\n" +
				"}\n" +
				"}"
			},
			"falsefalsetruefalsefalsefalse");
		
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String actualOutput = null;
		try {
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X$1.class"));
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
		
		String expectedOutput = " Enclosing Method: #25  #29 X$1MyLocal$A$Member.foo()Ljava/lang/Object;\n";
			
		int index = actualOutput.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(actualOutput, 2));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, actualOutput);
		}		
	}	
}
