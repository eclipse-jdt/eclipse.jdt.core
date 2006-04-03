/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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

public class SwitchTest extends AbstractRegressionTest {
	
public SwitchTest(String name) {
	super(name);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}

public void test001() {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" + 
		"public class X {\n" + 
		"  public static void main(String args[]) {\n" + 
		"    foo();\n" + 
		"  }\n" + 
		"  public static void foo() {\n" + 
		"    try {\n" + 
		"      switch(0) {\n" + 
		"      case 0 :\n" + 
		"      case 1 - (1 << 31) :\n" + 
		"      case (1 << 30) :\n" + 
		"      }\n" + 
		"    } catch (OutOfMemoryError e) {\n" + 
		"    }\n" + 
		"  }\n" + 
		"}\n",
	});
}
public void test002() {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" + 
		"public class X {\n" + 
		"  int k;\n" + 
		"  public void foo() {\n" + 
		"    int c;\n" + 
		"    switch (k) {\n" + 
		"      default :\n" + 
		"        c = 2;\n" + 
		"        break;\n" + 
		"      case 2 :\n" + 
		"        c = 3;\n" + 
		"        break;\n" + 
		"    }\n" + 
		"  }\n" + 
		"}\n",
	});
}

public void test003() {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" + 
		"public class X {\n" + 
		"  int i = 0;\n" + 
		"  void foo() {\n" + 
		"    switch (i) {\n" + 
		"      case 1 :\n" + 
		"        {\n" + 
		"          int j;\n" + 
		"          break;\n" + 
		"        }\n" + 
		"    }\n" + 
		"  }\n" + 
		"}\n",
	});
}

public void test004() {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" + 
		"public class X {\n" + 
		"  public static int foo() {\n" + 
		"    int i = 0, j;\n" + 
		"    switch (i) {\n" + 
		"      default :\n" + 
		"        int k = 2;\n" + 
		"        j = k;\n" + 
		"    }\n" + 
		"    if (j != -2) {\n" + 
		"      return 1;\n" + 
		"    }\n" + 
		"    return 0;\n" + 
		"  }\n" + 
		"}\n",
	});
}

public void test005() {
	this.runConformTest(new String[] {
		"p/BugJavaCase.java",
		"package p;\n" + 
		"class BugJavaCase {\n" + 
		"  public static final int BC_ZERO_ARG = 1;\n" + 
		"  public void test01(int i) {\n" + 
		"    switch (i) {\n" + 
		"      case BC_ZERO_ARG :\n" + 
		"        System.out.println(\"i = \" + i);\n" + 
		"        break;\n" + 
		"    }\n" + 
		"  }\n" + 
		"}\n",
	});
}


public void test006() {
	this.runConformTest(new String[] {
		"p/X.java",
		"package p;\n" + 
		"public class X {\n" + 
		"  public static void main(String args[]) {\n" + 
		"    foo(); \n" + 
		"  } \n" + 
		" \n" + 
		"  public static void foo() { \n" + 
		"    char x = 5;\n" + 
		"    final short b = 5;\n" + 
		"    int a;\n" + 
		"    \n" + 
		"    switch (x) {\n" + 
		"      case b:        // compile time error\n" + 
		"        a = 0;\n" + 
		"        break; \n" + 
		"      default:\n" + 
		"        a=1;\n" + 
		"    }\n" + 
		"    \n" + 
		"  }\n" + 
		"}\n",
	});
}

public void test007() {
	this.runNegativeTest(
		new String[] {
			"p/X.java",
			"package p;\n" + 
			"class X {\n" + 
			"  void v() {\n" + 
			"    switch (1) {\n" + 
			"      case (int) (1.0 / 0.0) :\n" + 
			"        break;\n" + 
			"      case (int) (2.0 / 0.0) :\n" + 
			"        break;\n" + 
			"    }\n" + 
			"  }\n" + 
			"}",
		}, 
		"----------\n" + 
		"1. ERROR in p\\X.java (at line 5)\n" + 
		"	case (int) (1.0 / 0.0) :\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Duplicate case\n" + 
		"----------\n" + 
		"2. ERROR in p\\X.java (at line 7)\n" + 
		"	case (int) (2.0 / 0.0) :\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Duplicate case\n" + 
		"----------\n"
	);
}
public void test008() {
	this.runConformTest(new String[] {
		"X.java",
		"public class X {\n" + 
		"	public static void main(String[] args) {\n" + 
		"		switch(args.length){\n" + 
		"		}\n" + 
		"		System.out.println(\"SUCCESS\");\n" + 
		"	}\n" + 
		"}\n",
	},
	"SUCCESS");
}
public void test009() {
	this.runConformTest(new String[] {
		"X.java",
		"public class X {\n" + 
		"    public static void main(String argv[]) {\n" + 
		"        switch (81391861) {\n" + 
		"        case (81391861) :\n" + 
		"        	System.out.println(\"SUCCESS\");\n" + 
		"            break;\n" + 
		"        default:\n" + 
		"        	System.out.println(\"FAILED\");\n" + 
		"        }\n" + 
		"    }\n" + 
		"}\n",
	},
	"SUCCESS");
}
public void test010() {
	this.runNegativeTest(new String[] {
		"X.java",
		"public class X {\n" + 
		"	\n" + 
		"	void foo(){\n" + 
		"		switch(this){\n" + 
		"			case 0 : \n" + 
		"				Zork z;\n" + 
		"		}\n" + 
		"	}\n" + 
		"	\n" + 
		"	void bar(){\n" + 
		"		switch(x){\n" + 
		"			case 0 : \n" + 
		"				Zork z;\n" + 
		"		}\n" + 
		"	}	\n" + 
		"}\n",
	},
	"----------\n" + 
	"1. ERROR in X.java (at line 4)\n" + 
	"	switch(this){\n" + 
	"	       ^^^^\n" + 
	"Cannot switch on a value of type X. Only int values or enum constants are permitted\n" + 
	"----------\n" + 
	"2. ERROR in X.java (at line 6)\n" + 
	"	Zork z;\n" + 
	"	^^^^\n" + 
	"Zork cannot be resolved to a type\n" + 
	"----------\n" + 
	"3. ERROR in X.java (at line 11)\n" + 
	"	switch(x){\n" + 
	"	       ^\n" + 
	"x cannot be resolved\n" + 
	"----------\n" + 
	"4. ERROR in X.java (at line 13)\n" + 
	"	Zork z;\n" + 
	"	^^^^\n" + 
	"Zork cannot be resolved to a type\n" + 
	"----------\n");
}
public void test011() {
	this.runConformTest(new String[] {
		"X.java",
		"public class X {\n" + 
		"	public static void main(String args[]) {\n" + 
		"		switch (args.length) {\n" + 
		"			case 1 :\n" + 
		"				System.out.println();\n" + 
		"			case 3 :\n" + 
		"				break;\n" + 
		"			default :\n" + 
		"		}\n" + 
		"		System.out.println(\"SUCCESS\");\n" + 
		"	}\n" + 
		"}\n",
	},
	"SUCCESS");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=86813
public void test012() {
	this.runConformTest(new String[] {
		"X.java",
		"public class X {\n" + 
		"  public static void main(String[] args) {\n" + 
		"    boolean x= true;\n" + 
		"    try {\n" + 
		"      int i= 1;\n" + 
		"      switch (i) { // <-- breakpoint here\n" + 
		"        case 1:\n" + 
		"          break;      //step 1 \n" + 
		"        case 2:\n" + 
		"          x = false;   //step 2 \n" + 
		"          break;\n" + 
		"      }\n" + 
		"    }catch(Exception e) {\n" + 
		"    }\n" + 
		"    System.out.println(\"SUCCESS\");\n" + 
		"  }\n" + 
		"}\n",
	},
	"SUCCESS");

	String expectedOutput = 
		"  // Method descriptor #15 ([Ljava/lang/String;)V\n" + 
		"  // Stack: 2, Locals: 3\n" + 
		"  public static void main(java.lang.String[] args);\n" + 
		"     0  iconst_1\n" + 
		"     1  istore_1 [x]\n" + 
		"     2  iconst_1\n" + 
		"     3  istore_2 [i]\n" + 
		"     4  iload_2 [i]\n" + 
		"     5  tableswitch default: 33\n" + 
		"          case 1: 28\n" + 
		"          case 2: 31\n" + 
		"    28  goto 37\n" + 
		"    31  iconst_0\n" + 
		"    32  istore_1 [x]\n" + 
		"    33  goto 37\n" + 
		"    36  astore_2\n" + 
		"    37  getstatic java.lang.System.out : java.io.PrintStream [16]\n" + 
		"    40  ldc <String \"SUCCESS\"> [22]\n" + 
		"    42  invokevirtual java.io.PrintStream.println(java.lang.String) : void [24]\n" + 
		"    45  return\n" + 
		"      Exception Table:\n" + 
		"        [pc: 2, pc: 36] -> 36 when : java.lang.Exception\n" + 
		"      Line numbers:\n" + 
		"        [pc: 0, line: 3]\n" + 
		"        [pc: 2, line: 5]\n" + 
		"        [pc: 4, line: 6]\n" + 
		"        [pc: 28, line: 8]\n" + 
		"        [pc: 31, line: 10]\n" + 
		"        [pc: 36, line: 13]\n" + 
		"        [pc: 37, line: 15]\n" + 
		"        [pc: 45, line: 16]\n" + 
		"      Local variable table:\n" + 
		"        [pc: 0, pc: 46] local: args index: 0 type: java.lang.String[]\n" + 
		"        [pc: 2, pc: 46] local: x index: 1 type: boolean\n" + 
		"        [pc: 4, pc: 36] local: i index: 2 type: int\n";
	
	try {
		File f = new File(OUTPUT_DIR + File.separator + "X.class");
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
		int index = result.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(result, 3));
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, result);
		}
	} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
		e.printStackTrace();
		assertTrue("ClassFormatException", false);
	} catch (IOException e) {
		e.printStackTrace();
		assertTrue("IOException", false);
	}		
}
public static Class testClass() {
	return SwitchTest.class;
}
}

