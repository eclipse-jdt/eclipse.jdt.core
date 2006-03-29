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
public static Class testClass() {
	return SwitchTest.class;
}
}

