/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
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

package org.eclipse.jdt.core.tests.compiler.parser;

import junit.framework.Test;

public class CompletionParserTest18 extends AbstractCompletionTest {

static {
//	TESTS_NAMES = new String [] { "test0001" };
}

public CompletionParserTest18(String testName) {
	super(testName);
}

public static Test suite() {
	return buildMinimalComplianceTestSuite(CompletionParserTest18.class, F_1_8);
}

public void test0001() {
	String string =
			"interface I { \n" +
			"	J foo(String x, String y);\n" +
			"}\n" +
			"interface J {\n" +
			"	K foo(String x, String y);\n" +
			"}\n" +
			"interface K {\n" +
			"	int foo(String x, int y);\n" +
			"}\n" +
			"public class X {\n" +
			"	static void goo(J i) {}\n" +
			"	public static void main(String[] args) {\n" +
			"		goo ((first, second) -> {\n" +
			"			return (xyz, pqr) -> first.\n" +
			"		});\n" +
			"	}\n" +
			"}\n";

	String completeBehind = "first.";
	int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:first.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "first.";
	String expectedUnitDisplayString =
			"interface I {\n" + 
			"  J foo(String x, String y);\n" + 
			"}\n" + 
			"interface J {\n" + 
			"  K foo(String x, String y);\n" + 
			"}\n" + 
			"interface K {\n" + 
			"  int foo(String x, int y);\n" + 
			"}\n" + 
			"public class X {\n" + 
			"  public X() {\n" + 
			"  }\n" + 
			"  static void goo(J i) {\n" + 
			"  }\n" + 
			"  public static void main(String[] args) {\n" + 
			"    (<no type> first, <no type> second) ->     {\n" + 
			"      (<no type> xyz, <no type> pqr) -> <CompleteOnName:first.>;\n" + 
			"    };\n" + 
			"  }\n" + 
			"}\n";

	checkMethodParse(
		string.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0002() {
	String string =
			"interface Foo { \n" +
			"	void run1(int s1, int s2);\n" +
			"}\n" +
			"interface X extends Foo{\n" +
			"  static Foo f = (first, second) -> System.out.print(fi);\n" +
			"}\n";

	String completeBehind = "fi";
	int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:fi>";
	String expectedParentNodeToString = "System.out.print(<CompleteOnName:fi>)";
	String completionIdentifier = "fi";
	String expectedReplacedSource = "fi";
	String expectedUnitDisplayString =
			"interface Foo {\n" + 
			"  void run1(int s1, int s2);\n" + 
			"}\n" + 
			"interface X extends Foo {\n" + 
			"  static Foo f = (<no type> first, <no type> second) -> System.out.print(<CompleteOnName:fi>);\n" + 
			"  <clinit>() {\n" + 
			"  }\n" + 
			"}\n";

	checkMethodParse(
		string.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0003() {
	String string =
			"interface Foo { \n" +
			"	void run1(int s1, int s2);\n" +
			"}\n" +
			"interface X extends Foo {\n" +
			"  public static void main(String [] args) {\n" +
			"      Foo f = (first, second) -> System.out.print(fi);\n" +
			"  }\n" +
			"}\n";

	String completeBehind = "fi";
	int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:fi>";
	String expectedParentNodeToString = "System.out.print(<CompleteOnName:fi>)";
	String completionIdentifier = "fi";
	String expectedReplacedSource = "fi";
	String expectedUnitDisplayString =
			"interface Foo {\n" + 
			"  void run1(int s1, int s2);\n" + 
			"}\n" + 
			"interface X extends Foo {\n" + 
			"  public static void main(String[] args) {\n" + 
			"    Foo f = (<no type> first, <no type> second) -> System.out.print(<CompleteOnName:fi>);\n" + 
			"  }\n" + 
			"}\n";

	checkMethodParse(
		string.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0004() {
	String string =
			"interface Foo {\n" +
			"	int run1(int s1, int s2);\n" +
			"}\n" +
			"interface X extends Foo{\n" +
			"    static Foo f = (x5, x6) -> {x\n" +
			"}\n";

	String completeBehind = "x";
	int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:x>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "x";
	String expectedReplacedSource = "x";
	String expectedUnitDisplayString =
			"interface Foo {\n" + 
			"  int run1(int s1, int s2);\n" + 
			"}\n" + 
			"interface X extends Foo {\n" + 
			"  static Foo f = (<no type> x5, <no type> x6) ->   {\n" + 
			"    <CompleteOnName:x>;\n" + 
			"  };\n" + 
			"  <clinit>() {\n" + 
			"  }\n" + 
			"}\n";

	checkMethodParse(
		string.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0005() {
	String string =
			"interface I {\n" +
			"	int foo(int x);\n" +
			"}\n" +
			"public class X {\n" +
			"	void go() {\n" +
			"		I i = (argument) -> {\n" +
			"			if (true) {\n" +
			"				return arg\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"}\n";

	String completeBehind = "arg";
	int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:arg>";
	String expectedParentNodeToString = "return <CompleteOnName:arg>;";
	String completionIdentifier = "arg";
	String expectedReplacedSource = "arg";
	String expectedUnitDisplayString =
			"interface I {\n" + 
			"  int foo(int x);\n" + 
			"}\n" + 
			"public class X {\n" + 
			"  public X() {\n" + 
			"  }\n" + 
			"  void go() {\n" + 
			"    I i = (<no type> argument) ->     {\n" + 
			"      {\n" + 
			"        if (true)\n" + 
			"            return <CompleteOnName:arg>;\n" + 
			"      }\n" + 
			"    };\n" + 
			"  }\n" + 
			"}\n";

	checkMethodParse(
		string.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0006() {
	String string =
			"interface I {\n" +
			"	int foo(int x);\n" +
			"}\n" +
			"public class X {\n" +
			"	void go() {\n" +
			"		I i = (argument) -> {\n" +
			"			argument == 0 ? arg\n" +
			"		}\n" +
			"	}\n" +
			"}\n";

	String completeBehind = "arg";
	int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:arg>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "arg";
	String expectedReplacedSource = "arg";
	String expectedUnitDisplayString =
			"interface I {\n" + 
			"  int foo(int x);\n" + 
			"}\n" + 
			"public class X {\n" + 
			"  public X() {\n" + 
			"  }\n" + 
			"  void go() {\n" + 
			"    I i = (<no type> argument) ->     {\n" + 
			"      <CompleteOnName:arg>;\n" + 
			"    };\n" + 
			"  }\n" + 
			"}\n";

	checkMethodParse(
		string.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
}
