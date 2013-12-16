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
			"    goo((<no type> first, <no type> second) -> {\n" + 
			"  return (<no type> xyz, <no type> pqr) -> <CompleteOnName:first.>;\n" + 
			"});\n" + 
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
	String expectedParentNodeToString = "static Foo f = (<no type> x5, <no type> x6) -> {\n" +
										"  <CompleteOnName:x>;\n" +
										"};";
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
			"      if (true)\n" + 
			"          {\n" + 
			"            return <CompleteOnName:arg>;\n" + 
			"          }\n" + 
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
public void test0007() {
	String string =
			"public interface Foo { \n" +
			"	int run(int s1, int s2); \n" +
			"}\n" +
			"interface X {\n" +
			"    static Foo f = (int x5, int x11) -> x;\n" +
			"    static int x1 = 2;\n" +
			"}\n" +
			"class C {\n" +
			"	void method1(){\n" +
			"		int p = X.\n" +
			"	}\n" +
			"}\n";

	String completeBehind = "X.";
	int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:X.>";
	String expectedParentNodeToString = "int p = <CompleteOnName:X.>;";
	String completionIdentifier = "";
	String expectedReplacedSource = "X.";
	String expectedUnitDisplayString =
			"public interface Foo {\n" + 
			"  int run(int s1, int s2);\n" + 
			"}\n" + 
			"interface X {\n" + 
			"  static Foo f;\n" +
			"  static int x1;\n" + 
			"  <clinit>() {\n" + 
			"  }\n" + 
			"}\n" + 
			"class C {\n" + 
			"  C() {\n" + 
			"  }\n" + 
			"  void method1() {\n" + 
			"    int p = <CompleteOnName:X.>;\n" + 
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
public void test0010() {
	String string =
			"interface I {\n" +
			"	void foo(String x);\n" +
			"}\n" +
			"public class X {\n" +
			"	String xField;\n" +
			"	static void goo(String s) {\n" +
			"	}\n" +
			"	static void goo(I i) {\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		goo((xyz) -> {\n" +
			"			System.out.println(xyz.);\n" +
			"		});\n" +
			"	}\n" +
			"}\n";

	String completeBehind = "xyz.";
	int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:xyz.>";
	String expectedParentNodeToString = "System.out.println(<CompleteOnName:xyz.>)";
	String completionIdentifier = "";
	String expectedReplacedSource = "xyz.";
	String expectedUnitDisplayString =
			"interface I {\n" +
			"  void foo(String x);\n" +
			"}\n" +
			"public class X {\n" +
			"  String xField;\n" +
			"  public X() {\n" +
			"  }\n" +
			"  static void goo(String s) {\n" +
			"  }\n" +
			"  static void goo(I i) {\n" +
			"  }\n" +
			"  public static void main(String[] args) {\n" +
			"    goo((<no type> xyz) -> {\n" +
			"  System.out.println(<CompleteOnName:xyz.>);\n" +
			"});\n" +
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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=417935, [1.8][code select] ICU#codeSelect doesn't work on reference to lambda parameter
public void test417935() {
	String string = 
			"import java.util.ArrayList;\n" +
			"import java.util.Arrays;\n" +
			"import java.util.Collections;\n" +
			"import java.util.Comparator;\n" +
			"public class X {\n" +
			"   int compareTo(X x) { return 0; }\n" +
			"	void foo() {\n" +
			"		Collections.sort(new ArrayList<X>(Arrays.asList(new X(), new X(), new X())),\n" +
			"				(X o1, X o2) -> o1.compa); //[2]\n" +
			"	}\n" +
			"}\n";

			String completeBehind = "compa";
			int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

			String expectedCompletionNodeToString = "<CompleteOnName:o1.compa>";
			String expectedParentNodeToString = "<NONE>";
			String completionIdentifier = "compa";
			String expectedReplacedSource = "o1.compa";
			String expectedUnitDisplayString =
					"import java.util.ArrayList;\n" + 
					"import java.util.Arrays;\n" + 
					"import java.util.Collections;\n" + 
					"import java.util.Comparator;\n" + 
					"public class X {\n" + 
					"  public X() {\n" + 
					"  }\n" + 
					"  int compareTo(X x) {\n" + 
					"  }\n" + 
					"  void foo() {\n" + 
					"    Collections.sort(new ArrayList<X>(Arrays.asList(new X(), new X(), new X())), (X o1, X o2) -> <CompleteOnName:o1.compa>);\n" + 
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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=405126, [1.8][code assist] Lambda parameters incorrectly recovered as fields. 
public void test405126() {
	String string = 
			"public interface Foo { \n" +
			"	int run(int s1, int s2); \n" +
			"}\n" +
			"interface X {\n" +
			"    static Foo f = (int x5, int x11) -> x\n" +
			"    static int x1 = 2;\n" +
			"}\n" +
			"class C {\n" +
			"	void method1(){\n" +
			"		int p = X.\n" +
			"	}\n" +
			"}\n";

			String completeBehind = "X.";
			int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

			String expectedCompletionNodeToString = "<CompleteOnName:X.>";
			String expectedParentNodeToString = "int p = <CompleteOnName:X.>;";
			String completionIdentifier = "";
			String expectedReplacedSource = "X.";
			String expectedUnitDisplayString =
					"public interface Foo {\n" + 
					"  int run(int s1, int s2);\n" + 
					"}\n" + 
					"interface X {\n" + 
					"  static Foo f;\n" + 
					"  static int x1;\n" + 
					"  <clinit>() {\n" + 
					"  }\n" + 
					"}\n" + 
					"class C {\n" + 
					"  C() {\n" + 
					"  }\n" + 
					"  void method1() {\n" + 
					"    int p = <CompleteOnName:X.>;\n" + 
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
// Verify that locals inside a lambda block don't get promoted to the parent block.
public void testLocalsPromotion() {
	String string = 
			"interface I {\n" +
			"	void foo(int x);\n" +
			"}\n" +
			"public class X {\n" +
			"	static void goo(I i) {}\n" +
			"	public static void main(String[] args) {\n" +
			"       int outerLocal;\n" +
			"		goo ((x) -> {\n" +
			"			int lambdaLocal = 10;\n" +
			"			System.out.println(\"Statement inside lambda\");\n" +
			"			lam\n" +
			"		});\n" +
			"	}\n" +
			"}\n";

			String completeBehind = "lam";
			int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

			String expectedCompletionNodeToString = "<CompleteOnName:lam>";
			String expectedParentNodeToString = "<NONE>";
			String completionIdentifier = "lam";
			String expectedReplacedSource = "lam";
			String expectedUnitDisplayString =
					"interface I {\n" + 
					"  void foo(int x);\n" + 
					"}\n" + 
					"public class X {\n" + 
					"  public X() {\n" + 
					"  }\n" + 
					"  static void goo(I i) {\n" + 
					"  }\n" + 
					"  public static void main(String[] args) {\n" + 
					"    int outerLocal;\n" + 
					"    goo((<no type> x) -> {\n" + 
					"  int lambdaLocal;\n" + 
					"  System.out.println(\"Statement inside lambda\");\n" + 
					"  <CompleteOnName:lam>;\n" + 
					"});\n" + 
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

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422107, [1.8][code assist] Invoking code assist just before and after a variable initialized using lambda gives different result
public void testCompletionLocation() {
	String string = 
			"interface I {\n" +
			"    void doit();\n" +
			"}\n" +
			"interface J {\n" +
			"}\n" +
			"public class X { \n" +
			"	Object o = (I & J) () -> {};\n" +
			"	/* AFTER */\n" +
			"}\n";

			String completeBehind = "/* AFTER */";
			int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

			String expectedCompletionNodeToString = "<CompleteOnType:>";
			String expectedParentNodeToString = "<NONE>";
			String completionIdentifier = "";
			String expectedReplacedSource = "";
			String expectedUnitDisplayString =
					"interface I {\n" + 
					"  void doit();\n" + 
					"}\n" + 
					"interface J {\n" + 
					"}\n" + 
					"public class X {\n" + 
					"  Object o;\n" + 
					"  <CompleteOnType:>;\n" + 
					"  public X() {\n" + 
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
public void testElidedCompletion() {
	String string = 
			"class Collections {\n" +
			"	public static void sort(ArrayList list, Comparator c) {\n" +
			"	}\n" +
			"}\n" +
			"interface Comparator {\n" +
			"	int compareTo(X t, X s);\n" +
			"}\n" +
			"class ArrayList {\n" +
			"}\n" +
			"public class X {\n" +
			"	int compareTo(X x) { return 0; }\n" +
			"	void foo() {\n" +
			"		Collections.sort(new ArrayList(), (X o1, X o2) -> o1.compa);\n" +
			"	}\n" +
			"}\n";

			String completeBehind = "compa";
			int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

			String expectedCompletionNodeToString = "<CompleteOnName:o1.compa>";
			String expectedParentNodeToString = "<NONE>";
			String completionIdentifier = "compa";
			String expectedReplacedSource = "o1.compa";
			String expectedUnitDisplayString =
					"class Collections {\n" + 
					"  Collections() {\n" + 
					"  }\n" + 
					"  public static void sort(ArrayList list, Comparator c) {\n" + 
					"  }\n" + 
					"}\n" + 
					"interface Comparator {\n" + 
					"  int compareTo(X t, X s);\n" + 
					"}\n" + 
					"class ArrayList {\n" + 
					"  ArrayList() {\n" + 
					"  }\n" + 
					"}\n" + 
					"public class X {\n" + 
					"  public X() {\n" + 
					"  }\n" + 
					"  int compareTo(X x) {\n" + 
					"  }\n" + 
					"  void foo() {\n" + 
					"    Collections.sort(new ArrayList(), (X o1, X o2) -> <CompleteOnName:o1.compa>);\n" + 
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
public void testElidedCompletion2() {
	String string = 
			"class Collections {\n" +
			"	public static void sort(ArrayList list, Comparator c) {\n" +
			"	}\n" +
			"}\n" +
			"interface Comparator {\n" +
			"	int compareTo(X t, X s);\n" +
			"}\n" +
			"class ArrayList {\n" +
			"}\n" +
			"public class X {\n" +
			"	int compareTo(X x) { return 0; }\n" +
			"	void foo() {\n" +
			"		Collections.sort(new ArrayList(), (o1, o2) -> o1.compa);\n" +
			"	}\n" +
			"}\n";

			String completeBehind = "compa";
			int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

			String expectedCompletionNodeToString = "<CompleteOnName:o1.compa>";
			String expectedParentNodeToString = "<NONE>";
			String completionIdentifier = "compa";
			String expectedReplacedSource = "o1.compa";
			String expectedUnitDisplayString =
					"class Collections {\n" + 
					"  Collections() {\n" + 
					"  }\n" + 
					"  public static void sort(ArrayList list, Comparator c) {\n" + 
					"  }\n" + 
					"}\n" + 
					"interface Comparator {\n" + 
					"  int compareTo(X t, X s);\n" + 
					"}\n" + 
					"class ArrayList {\n" + 
					"  ArrayList() {\n" + 
					"  }\n" + 
					"}\n" + 
					"public class X {\n" + 
					"  public X() {\n" + 
					"  }\n" + 
					"  int compareTo(X x) {\n" + 
					"  }\n" + 
					"  void foo() {\n" + 
					"    Collections.sort(new ArrayList(), (<no type> o1, <no type> o2) -> <CompleteOnName:o1.compa>);\n" + 
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
public void testUnspecifiedReference() {  // verify that completion works on unspecified reference and finds types and names.
	String string = 
			"interface I {\n" +
			"    void doit(X x);\n" +
			"}\n" +
			"class String {\n" +
			"}\n" +
			"public class X { \n" +
			"	static void goo(I i) {\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		goo((StringParameter) -> {\n" +
			"			Str\n" +
			"		});\n" +
			"	} \n" +
			"}\n";

			String completeBehind = "Str";
			int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

			String expectedCompletionNodeToString = "<CompleteOnName:Str>";
			String expectedParentNodeToString = "<NONE>";
			String completionIdentifier = "Str";
			String expectedReplacedSource = "Str";
			String expectedUnitDisplayString =
					"interface I {\n" + 
					"  void doit(X x);\n" + 
					"}\n" + 
					"class String {\n" + 
					"  String() {\n" + 
					"  }\n" + 
					"}\n" + 
					"public class X {\n" + 
					"  public X() {\n" + 
					"  }\n" + 
					"  static void goo(I i) {\n" + 
					"  }\n" + 
					"  public static void main(String[] args) {\n" + 
					"    goo((<no type> StringParameter) -> {\n" + 
					"  <CompleteOnName:Str>;\n" + 
					"});\n" + 
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
public void testBrokenMethodCall() {  // verify that completion works when the call containing the lambda is broken - i.e missing a semicolon.
	String string = 
			"interface I {\n" +
			"    void doit(X x);\n" +
			"}\n" +
			"public class X { \n" +
			"	static void goo(I i) {\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		goo((StringParameter) -> {\n" +
			"			Str\n" +
			"		})\n" +
			"	} \n" +
			"}\n";

			String completeBehind = "Str";
			int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

			String expectedCompletionNodeToString = "<CompleteOnName:Str>";
			String expectedParentNodeToString = "<NONE>";
			String completionIdentifier = "Str";
			String expectedReplacedSource = "Str";
			String expectedUnitDisplayString =
					"interface I {\n" + 
					"  void doit(X x);\n" + 
					"}\n" + 
					"public class X {\n" + 
					"  public X() {\n" + 
					"  }\n" + 
					"  static void goo(I i) {\n" + 
					"  }\n" + 
					"  public static void main(String[] args) {\n" + 
					"    goo((<no type> StringParameter) -> {\n" + 
					"  <CompleteOnName:Str>;\n" + 
					"});\n" + 
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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=424080, [1.8][completion] Workbench hanging on code completion with lambda expression containing anonymous class
public void test424080() {
	String string = 
			"interface FI {\n" +
			"	public static int val = 5;\n" +
			"	default int run (String x) { return 1;};\n" +
			"	public int run (int x);\n" +
			"}\n" +
			"public class X {\n" +
			"	FI fi = x -> (new FI() { public int run (int x) {return 2;}}).run(\"\")val;\n" +
			"}\n";

			String completeBehind = "val";
			int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

			String expectedCompletionNodeToString = "<CompleteOnName:val>";
			String expectedParentNodeToString = "<NONE>";
			String completionIdentifier = "val";
			String expectedReplacedSource = "val";
			String expectedUnitDisplayString =
					"interface FI {\n" + 
					"  public static int val;\n" + 
					"  <clinit>() {\n" + 
					"  }\n" + 
					"  default int run(String x) {\n" + 
					"  }\n" + 
					"  public int run(int x);\n" + 
					"}\n" + 
					"public class X {\n" + 
					"  FI fi = <CompleteOnName:val>;\n" + 
					"  public X() {\n" + 
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
