/*******************************************************************************
 * Copyright (c) 2013, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
			"""
		interface I {\s
			J foo(String x, String y);
		}
		interface J {
			K foo(String x, String y);
		}
		interface K {
			int foo(String x, int y);
		}
		public class X {
			static void goo(J i) {}
			public static void main(String[] args) {
				goo ((first, second) -> {
					return (xyz, pqr) -> first.
				});
			}
		}
		""";

	String completeBehind = "first.";
	int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:first.>";
	String expectedParentNodeToString = "(<no type> xyz, <no type> pqr) -> <CompleteOnName:first.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "first.";
	String expectedUnitDisplayString =
			"""
		interface I {
		  J foo(String x, String y);
		}
		interface J {
		  K foo(String x, String y);
		}
		interface K {
		  int foo(String x, int y);
		}
		public class X {
		  public X() {
		  }
		  static void goo(J i) {
		  }
		  public static void main(String[] args) {
		    goo((<no type> first, <no type> second) -> {
		  return (<no type> xyz, <no type> pqr) -> <CompleteOnName:first.>;
		});
		  }
		}
		""";

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
			"""
		interface Foo {\s
			void run1(int s1, int s2);
		}
		interface X extends Foo{
		  static Foo f = (first, second) -> System.out.print(fi);
		}
		""";

	String completeBehind = "fi";
	int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:fi>";
	String expectedParentNodeToString = "System.out.print(<CompleteOnName:fi>)";
	String completionIdentifier = "fi";
	String expectedReplacedSource = "fi";
	String expectedUnitDisplayString =
			"""
		interface Foo {
		  void run1(int s1, int s2);
		}
		interface X extends Foo {
		  static Foo f = (<no type> first, <no type> second) -> System.out.print(<CompleteOnName:fi>);
		  <clinit>() {
		  }
		}
		""";

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
			"""
		interface Foo {\s
			void run1(int s1, int s2);
		}
		interface X extends Foo {
		  public static void main(String [] args) {
		      Foo f = (first, second) -> System.out.print(fi);
		  }
		}
		""";

	String completeBehind = "fi";
	int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:fi>";
	String expectedParentNodeToString = "System.out.print(<CompleteOnName:fi>)";
	String completionIdentifier = "fi";
	String expectedReplacedSource = "fi";
	String expectedUnitDisplayString =
			"""
		interface Foo {
		  void run1(int s1, int s2);
		}
		interface X extends Foo {
		  public static void main(String[] args) {
		    Foo f = (<no type> first, <no type> second) -> System.out.print(<CompleteOnName:fi>);
		  }
		}
		""";

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
			"""
		interface Foo {
			int run1(int s1, int s2);
		}
		interface X extends Foo{
		    static Foo f = (x5, x6) -> {x
		}
		""";

	String completeBehind = "x";
	int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:x>";
	String expectedParentNodeToString =
			"""
		(<no type> x5, <no type> x6) -> {
		  <CompleteOnName:x>;
		}""";
	String completionIdentifier = "x";
	String expectedReplacedSource = "x";
	String expectedUnitDisplayString =
			"""
		interface Foo {
		  int run1(int s1, int s2);
		}
		interface X extends Foo {
		  static Foo f = (<no type> x5, <no type> x6) ->   {
		    <CompleteOnName:x>;
		  };
		  <clinit>() {
		  }
		}
		""";

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
			"""
		interface I {
			int foo(int x);
		}
		public class X {
			void go() {
				I i = (argument) -> {
					if (true) {
						return arg
					}
				}
			}
		}
		""";

	String completeBehind = "arg";
	int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:arg>";
	String expectedParentNodeToString = "return <CompleteOnName:arg>;";
	String completionIdentifier = "arg";
	String expectedReplacedSource = "arg";
	String expectedUnitDisplayString =
			"""
		interface I {
		  int foo(int x);
		}
		public class X {
		  public X() {
		  }
		  void go() {
		    I i = (<no type> argument) ->     {
		      if (true)
		          {
		            return <CompleteOnName:arg>;
		          }
		    };
		  }
		}
		""";

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
			"""
		interface I {
			int foo(int x);
		}
		public class X {
			void go() {
				I i = (argument) -> {
					argument == 0 ? arg
				}
			}
		}
		""";

	String completeBehind = "arg";
	int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:arg>";
	String expectedParentNodeToString =
			"""
		(<no type> argument) -> {
		  <CompleteOnName:arg>;
		}""";
	String completionIdentifier = "arg";
	String expectedReplacedSource = "arg";
	String expectedUnitDisplayString =
			"""
		interface I {
		  int foo(int x);
		}
		public class X {
		  public X() {
		  }
		  void go() {
		    I i = (<no type> argument) ->     {
		      <CompleteOnName:arg>;
		    };
		  }
		}
		""";

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
			"""
		public interface Foo {\s
			int run(int s1, int s2);\s
		}
		interface X {
		    static Foo f = (int x5, int x11) -> x;
		    static int x1 = 2;
		}
		class C {
			void method1(){
				int p = X.
			}
		}
		""";

	String completeBehind = "X.";
	int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:X.>";
	String expectedParentNodeToString = "int p = <CompleteOnName:X.>;";
	String completionIdentifier = "";
	String expectedReplacedSource = "X.";
	String expectedUnitDisplayString =
			"""
		public interface Foo {
		  int run(int s1, int s2);
		}
		interface X {
		  static Foo f;
		  static int x1;
		  <clinit>() {
		  }
		}
		class C {
		  C() {
		  }
		  void method1() {
		    int p = <CompleteOnName:X.>;
		  }
		}
		""";

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
			"""
		interface I {
			void foo(String x);
		}
		public class X {
			String xField;
			static void goo(String s) {
			}
			static void goo(I i) {
			}
			public static void main(String[] args) {
				goo((xyz) -> {
					System.out.println(xyz.);
				});
			}
		}
		""";

	String completeBehind = "xyz.";
	int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:xyz.>";
	String expectedParentNodeToString = "System.out.println(<CompleteOnName:xyz.>)";
	String completionIdentifier = "";
	String expectedReplacedSource = "xyz.";
	String expectedUnitDisplayString =
			"""
		interface I {
		  void foo(String x);
		}
		public class X {
		  String xField;
		  public X() {
		  }
		  static void goo(String s) {
		  }
		  static void goo(I i) {
		  }
		  public static void main(String[] args) {
		    goo((<no type> xyz) -> {
		  System.out.println(<CompleteOnName:xyz.>);
		});
		  }
		}
		""";

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
			"""
		import java.util.ArrayList;
		import java.util.Arrays;
		import java.util.Collections;
		import java.util.Comparator;
		public class X {
		   int compareTo(X x) { return 0; }
			void foo() {
				Collections.sort(new ArrayList<X>(Arrays.asList(new X(), new X(), new X())),
						(X o1, X o2) -> o1.compa); //[2]
			}
		}
		""";

			String completeBehind = "compa";
			int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

			String expectedCompletionNodeToString = "<CompleteOnName:o1.compa>";
			String expectedParentNodeToString = "(X o1, X o2) -> <CompleteOnName:o1.compa>";
			String completionIdentifier = "compa";
			String expectedReplacedSource = "o1.compa";
			String expectedUnitDisplayString =
					"""
				import java.util.ArrayList;
				import java.util.Arrays;
				import java.util.Collections;
				import java.util.Comparator;
				public class X {
				  public X() {
				  }
				  int compareTo(X x) {
				  }
				  void foo() {
				    Collections.sort(new ArrayList<X>(Arrays.asList(new X(), new X(), new X())), (X o1, X o2) -> <CompleteOnName:o1.compa>);
				  }
				}
				""";

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
			"""
		public interface Foo {\s
			int run(int s1, int s2);\s
		}
		interface X {
		    static Foo f = (int x5, int x11) -> x
		    static int x1 = 2;
		}
		class C {
			void method1(){
				int p = X.
			}
		}
		""";

			String completeBehind = "X.";
			int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

			String expectedCompletionNodeToString = "<CompleteOnName:X.>";
			String expectedParentNodeToString = "int p = <CompleteOnName:X.>;";
			String completionIdentifier = "";
			String expectedReplacedSource = "X.";
			String expectedUnitDisplayString =
					"""
				public interface Foo {
				  int run(int s1, int s2);
				}
				interface X {
				  static Foo f;
				  static int x1;
				  <clinit>() {
				  }
				}
				class C {
				  C() {
				  }
				  void method1() {
				    int p = <CompleteOnName:X.>;
				  }
				}
				""";

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
			"""
		interface I {
			void foo(int x);
		}
		public class X {
			static void goo(I i) {}
			public static void main(String[] args) {
		       int outerLocal;
				goo ((x) -> {
					int lambdaLocal = 10;
					System.out.println("Statement inside lambda");
					lam
				});
			}
		}
		""";

			String completeBehind = "lam";
			int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

			String expectedCompletionNodeToString = "<CompleteOnName:lam>";
			String expectedParentNodeToString =
					"""
				(<no type> x) -> {
				  int lambdaLocal;
				  System.out.println("Statement inside lambda");
				  <CompleteOnName:lam>;
				}""";
			String completionIdentifier = "lam";
			String expectedReplacedSource = "lam";
			String expectedUnitDisplayString =
					"""
				interface I {
				  void foo(int x);
				}
				public class X {
				  public X() {
				  }
				  static void goo(I i) {
				  }
				  public static void main(String[] args) {
				    int outerLocal;
				    goo((<no type> x) -> {
				  int lambdaLocal;
				  System.out.println("Statement inside lambda");
				  <CompleteOnName:lam>;
				});
				  }
				}
				""";

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
			"""
		interface I {
		    void doit();
		}
		interface J {
		}
		public class X {\s
			Object o = (I & J) () -> {};
			/* AFTER */
		}
		""";

			String completeBehind = "/* AFTER */";
			int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

			String expectedCompletionNodeToString = "<CompleteOnType:>";
			String expectedParentNodeToString = "<NONE>";
			String completionIdentifier = "";
			String expectedReplacedSource = "";
			String expectedUnitDisplayString =
					"""
				interface I {
				  void doit();
				}
				interface J {
				}
				public class X {
				  Object o;
				  <CompleteOnType:>;
				  public X() {
				  }
				}
				""";

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
			"""
		class Collections {
			public static void sort(ArrayList list, Comparator c) {
			}
		}
		interface Comparator {
			int compareTo(X t, X s);
		}
		class ArrayList {
		}
		public class X {
			int compareTo(X x) { return 0; }
			void foo() {
				Collections.sort(new ArrayList(), (X o1, X o2) -> o1.compa);
			}
		}
		""";

			String completeBehind = "compa";
			int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

			String expectedCompletionNodeToString = "<CompleteOnName:o1.compa>";
			String expectedParentNodeToString = "(X o1, X o2) -> <CompleteOnName:o1.compa>";
			String completionIdentifier = "compa";
			String expectedReplacedSource = "o1.compa";
			String expectedUnitDisplayString =
					"""
				class Collections {
				  Collections() {
				  }
				  public static void sort(ArrayList list, Comparator c) {
				  }
				}
				interface Comparator {
				  int compareTo(X t, X s);
				}
				class ArrayList {
				  ArrayList() {
				  }
				}
				public class X {
				  public X() {
				  }
				  int compareTo(X x) {
				  }
				  void foo() {
				    Collections.sort(new ArrayList(), (X o1, X o2) -> <CompleteOnName:o1.compa>);
				  }
				}
				""";

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
			"""
		class Collections {
			public static void sort(ArrayList list, Comparator c) {
			}
		}
		interface Comparator {
			int compareTo(X t, X s);
		}
		class ArrayList {
		}
		public class X {
			int compareTo(X x) { return 0; }
			void foo() {
				Collections.sort(new ArrayList(), (o1, o2) -> o1.compa);
			}
		}
		""";

			String completeBehind = "compa";
			int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

			String expectedCompletionNodeToString = "<CompleteOnName:o1.compa>";
			String expectedParentNodeToString = "(<no type> o1, <no type> o2) -> <CompleteOnName:o1.compa>";
			String completionIdentifier = "compa";
			String expectedReplacedSource = "o1.compa";
			String expectedUnitDisplayString =
					"""
				class Collections {
				  Collections() {
				  }
				  public static void sort(ArrayList list, Comparator c) {
				  }
				}
				interface Comparator {
				  int compareTo(X t, X s);
				}
				class ArrayList {
				  ArrayList() {
				  }
				}
				public class X {
				  public X() {
				  }
				  int compareTo(X x) {
				  }
				  void foo() {
				    Collections.sort(new ArrayList(), (<no type> o1, <no type> o2) -> <CompleteOnName:o1.compa>);
				  }
				}
				""";

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
			"""
		interface I {
		    void doit(X x);
		}
		class String {
		}
		public class X {\s
			static void goo(I i) {
			}
			public static void main(String[] args) {
				goo((StringParameter) -> {
					Str
				});
			}\s
		}
		""";

			String completeBehind = "Str";
			int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

			String expectedCompletionNodeToString = "<CompleteOnName:Str>";
			String expectedParentNodeToString =
					"""
				(<no type> StringParameter) -> {
				  <CompleteOnName:Str>;
				}""";
			String completionIdentifier = "Str";
			String expectedReplacedSource = "Str";
			String expectedUnitDisplayString =
					"""
				interface I {
				  void doit(X x);
				}
				class String {
				  String() {
				  }
				}
				public class X {
				  public X() {
				  }
				  static void goo(I i) {
				  }
				  public static void main(String[] args) {
				    goo((<no type> StringParameter) -> {
				  <CompleteOnName:Str>;
				});
				  }
				}
				""";

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
			"""
		interface I {
		    void doit(X x);
		}
		public class X {\s
			static void goo(I i) {
			}
			public static void main(String[] args) {
				goo((StringParameter) -> {
					Str
				})
			}\s
		}
		""";

			String completeBehind = "Str";
			int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

			String expectedCompletionNodeToString = "<CompleteOnName:Str>";
			String expectedParentNodeToString =
					"""
				(<no type> StringParameter) -> {
				  <CompleteOnName:Str>;
				}""";
			String completionIdentifier = "Str";
			String expectedReplacedSource = "Str";
			String expectedUnitDisplayString =
					"""
				interface I {
				  void doit(X x);
				}
				public class X {
				  public X() {
				  }
				  static void goo(I i) {
				  }
				  public static void main(String[] args) {
				    goo((<no type> StringParameter) -> {
				  <CompleteOnName:Str>;
				});
				  }
				}
				""";

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
			"""
	interface FI {
		public static int val = 5;
		default int run (String x) { return 1;};
		public int run (int x);
	}
	public class X {
		FI fi = x -> (new FI() { public int run (int x) {return 2;}}).run("")val;
	}
	""";

			String completeBehind = "val";
			int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

			String expectedCompletionNodeToString = "<NONE>";
			String expectedParentNodeToString = "<NONE>";
			String completionIdentifier = "val";
			String expectedReplacedSource = "val";
			String expectedUnitDisplayString =
					"""
				interface FI {
				  public static int val;
				  <clinit>() {
				  }
				  default int run(String x) {
				  }
				  public int run(int x);
				}
				public class X {
				  FI fi;
				  public X() {
				  }
				}
				""";

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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=425084, [1.8][completion] Eclipse freeze while autocompleting try block in lambda.
public void test425084() {
	String string =
			"""
		interface I {
			void foo();
		}
		public class X {
			I goo() {
					try
			}
		}
		""";

			String completeBehind = "try";
			int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

			String expectedCompletionNodeToString = "<CompleteOnName:try>";
			String expectedParentNodeToString = "<NONE>";
			String completionIdentifier = "try";
			String expectedReplacedSource = "try";
			String expectedUnitDisplayString =
					"""
				interface I {
				  void foo();
				}
				public class X {
				  public X() {
				  }
				  I goo() {
				    <CompleteOnName:try>;
				  }
				}
				""";

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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=425084, [1.8][completion] Eclipse freeze while autocompleting try block in lambda.
public void test425084b() {
	String string =
			"""
		interface I {
			void foo();
		}
		public class X {
			I goo() {
				return () -> {
					try
				};
			}
		}
		""";

			String completeBehind = "try";
			int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

			String expectedCompletionNodeToString = "<CompleteOnName:try>";
			String expectedParentNodeToString =
					"""
				() -> {
				  <CompleteOnName:try>;
				}""";
			String completionIdentifier = "try";
			String expectedReplacedSource = "try";
			String expectedUnitDisplayString =
					"""
				interface I {
				  void foo();
				}
				public class X {
				  public X() {
				  }
				  I goo() {
				    return () -> {
				  <CompleteOnName:try>;
				};
				  }
				}
				""";

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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427255, [1.8][code assist] Hang due to infinite loop in Parser.automatonWillShift
public void test427255() {
	String string =
			"""
		public class X {
		  public final String targetApplication;
		  public final String arguments;
		  public final String appUserModelID;
		  public X() {}
		}
		""";

			String completeBehind = "X";
			int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

			String expectedCompletionNodeToString = "<CompleteOnType:X>";
			String expectedParentNodeToString = "<NONE>";
			String completionIdentifier = "X";
			String expectedReplacedSource = "X";
			String expectedUnitDisplayString =
					"""
				public class X {
				  public final String targetApplication;
				  public final String arguments;
				  public final String appUserModelID;
				  <CompleteOnType:X>;
				  {
				  }
				  public X() {
				  }
				}
				""";

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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427322, [1.8][code assist] Eclipse hangs upon completion just past lambda
public void test427322() {
	String string =
			"""
		public class X {
			interface I {
				int foo();
			}
			public static void main(String[] args) {
				I i = () -> 1, i.;
			}
		}
		""";

			String completeBehind = "i.";
			int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

			String expectedCompletionNodeToString = "<CompleteOnName:>";
			String expectedParentNodeToString = "<NONE>";
			String completionIdentifier = "";
			String expectedReplacedSource = "";
			String expectedUnitDisplayString =
					"""
				public class X {
				  interface I {
				    int foo();
				  }
				  public X() {
				  }
				  public static void main(String[] args) {
				    I i;
				    I i;
				    <CompleteOnName:>;
				  }
				}
				""";

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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427322, [1.8][code assist] Eclipse hangs upon completion just past lambda
public void test427322a() {
	String string =
			"""
		public class X {
			interface I {
				int foo();
			}
			public static void main(String[] args) {
				I i = 1, i.;
			}
		}
		""";

			String completeBehind = "i.";
			int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

			String expectedCompletionNodeToString = "<CompleteOnName:>";
			String expectedParentNodeToString = "<NONE>";
			String completionIdentifier = "";
			String expectedReplacedSource = "";
			String expectedUnitDisplayString =
					"""
				public class X {
				  interface I {
				    int foo();
				  }
				  public X() {
				  }
				  public static void main(String[] args) {
				    I i;
				    I i;
				    <CompleteOnName:>;
				  }
				}
				""";

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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427463, [1.8][content assist] No completions available in throw statement within lambda body
public void test427463() {
	String string =
			"""
		interface FI1 {
			int foo(int x) throws Exception;
		}
		class Test {
			FI1 fi1= (int x) -> {
				throw new Ex
			};
			private void test() throws Exception {
				throw new Ex
			}
		}
		""";

			String completeBehind = "new Ex";
			int cursorLocation = string.indexOf(completeBehind) + completeBehind.length() - 1;

			String expectedCompletionNodeToString = "<CompleteOnException:Ex>";
			String expectedParentNodeToString = "throw new <CompleteOnException:Ex>();";
			String completionIdentifier = "Ex";
			String expectedReplacedSource = "Ex";
			String expectedUnitDisplayString =
					"""
				interface FI1 {
				  int foo(int x) throws Exception;
				}
				class Test {
				  FI1 fi1 = (int x) ->   {
				    <CompleteOnException:Ex>;
				  };
				  Test() {
				  }
				  private void test() throws Exception {
				  }
				}
				""";

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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427117, [1.8][code assist] code assist after lambda as a parameter does not work
public void test427117() {
	String string =
			"""
		import java.util.ArrayList;
		import java.util.List;
		public class X {
			public static void main(String[] args) {
				bar();
			}
			public static void bar() {
				List<Integer> list = new ArrayList<Integer>();
				list.forEach(s -> System.out.println(s));
				list.
			}
		}
		""";

			String completeBehind = "list.";
			int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

			String expectedCompletionNodeToString = "<CompleteOnName:list.>";
			String expectedParentNodeToString = "<NONE>";
			String completionIdentifier = "";
			String expectedReplacedSource = "list.";
			String expectedUnitDisplayString =
					"""
				import java.util.ArrayList;
				import java.util.List;
				public class X {
				  public X() {
				  }
				  public static void main(String[] args) {
				  }
				  public static void bar() {
				    List<Integer> list;
				    <CompleteOnName:list.>;
				  }
				}
				""";

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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427532, [1.8][code assist] Completion engine does not like intersection casts
public void test427532() {
	String string =
			"""
		import java.io.Serializable;
		interface I {
			void foo();
		}
		public class X {
			public static void main(String[] args) {
				I i = (I & Serializable) () -> {};
				syso
			}
		}
		""";

			String completeBehind = "syso";
			int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

			String expectedCompletionNodeToString = "<CompleteOnName:syso>";
			String expectedParentNodeToString = "<NONE>";
			String completionIdentifier = "syso";
			String expectedReplacedSource = "syso";
			String expectedUnitDisplayString =
					"""
				import java.io.Serializable;
				interface I {
				  void foo();
				}
				public class X {
				  public X() {
				  }
				  public static void main(String[] args) {
				    I i;
				    <CompleteOnName:syso>;
				  }
				}
				""";

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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428735,  [1.8][assist] Missing completion proposals inside lambda body expression - other than first token
public void test428735() {
	String string =
			"""
		import java.util.List;
		class Person {
		   String getLastName() { return null; }
		}
		public class X {
			void test1 (List<Person> people) {
				people.stream().forEach(p -> System.out.println(p.)); // NOK
			}
		}
		""";

			String completeBehind = "p.";
			int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

			String expectedCompletionNodeToString = "<CompleteOnName:p.>";
			String expectedParentNodeToString = "System.out.println(<CompleteOnName:p.>)";
			String completionIdentifier = "";
			String expectedReplacedSource = "p.";
			String expectedUnitDisplayString =
					"""
				import java.util.List;
				class Person {
				  Person() {
				  }
				  String getLastName() {
				  }
				}
				public class X {
				  public X() {
				  }
				  void test1(List<Person> people) {
				    people.stream().forEach((<no type> p) -> System.out.println(<CompleteOnName:p.>));
				  }
				}
				""";

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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428735,  [1.8][assist] Missing completion proposals inside lambda body expression - other than first token
public void test428735a() {
	String string =
			"""
		import java.util.List;
		class Person {
		   String getLastName() { return null; }
		}
		public class X {
			void test1 (List<Person> people) {
				people.stream().forEach(p -> System.out.println(p.)); // NOK
			}
		   void test2(List<Person> people) {
		       people.sort((x,y) -> x.);  // OK
		   }
		}
		""";

			String completeBehind = "x.";
			int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

			String expectedCompletionNodeToString = "<CompleteOnName:x.>";
			String expectedParentNodeToString = "(<no type> x, <no type> y) -> <CompleteOnName:x.>";
			String completionIdentifier = "";
			String expectedReplacedSource = "x.";
			String expectedUnitDisplayString =
					"""
				import java.util.List;
				class Person {
				  Person() {
				  }
				  String getLastName() {
				  }
				}
				public class X {
				  public X() {
				  }
				  void test1(List<Person> people) {
				  }
				  void test2(List<Person> people) {
				    people.sort((<no type> x, <no type> y) -> <CompleteOnName:x.>);
				  }
				}
				""";

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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428735,  [1.8][assist] Missing completion proposals inside lambda body expression - other than first token
public void test428735b() {
	String string =
			"""
		import java.util.List;
		class Person {
		   String getLastName() { return null; }
		}
		public class X {
			void test1 (List<Person> people) {
				people.stream().forEach(p -> System.out.println(p.)); // NOK
			}
		   void test2(List<Person> people) {
		       people.sort((x,y) -> x.getLastName().compareTo(y.));
		   }
		}
		""";

			String completeBehind = "y.";
			int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

			String expectedCompletionNodeToString = "<CompleteOnName:y.>";
			String expectedParentNodeToString = "x.getLastName().compareTo(<CompleteOnName:y.>)";
			String completionIdentifier = "";
			String expectedReplacedSource = "y.";
			String expectedUnitDisplayString =
					"""
				import java.util.List;
				class Person {
				  Person() {
				  }
				  String getLastName() {
				  }
				}
				public class X {
				  public X() {
				  }
				  void test1(List<Person> people) {
				  }
				  void test2(List<Person> people) {
				    people.sort((<no type> x, <no type> y) -> x.getLastName().compareTo(<CompleteOnName:y.>));
				  }
				}
				""";

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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428735,  [1.8][assist] Missing completion proposals inside lambda body expression - other than first token
public void test428735c() {
	String string =
			"""
		import java.util.List;
		class Person {
		   String getLastName() { return null; }
		}
		public class X {
			void test1 (List<Person> people) {
				people.stream().forEach(p -> System.out.println(p.)); // NOK
			}
		   void test2(List<Person> people) {
		       people.sort((x,y) -> x.getLastName() + y.);
		   }
		}
		""";

			String completeBehind = "y.";
			int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

			String expectedCompletionNodeToString = "<CompleteOnName:y.>";
			String expectedParentNodeToString = "(x.getLastName() + <CompleteOnName:y.>)";
			String completionIdentifier = "";
			String expectedReplacedSource = "y.";
			String expectedUnitDisplayString =
					"""
				import java.util.List;
				class Person {
				  Person() {
				  }
				  String getLastName() {
				  }
				}
				public class X {
				  public X() {
				  }
				  void test1(List<Person> people) {
				  }
				  void test2(List<Person> people) {
				    people.sort((<no type> x, <no type> y) -> (x.getLastName() + <CompleteOnName:y.>));
				  }
				}
				""";

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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428735,  [1.8][assist] Missing completion proposals inside lambda body expression - other than first token
public void test428735d() {
	String string =
			"""
		import java.util.List;
		class Person {
		   String getLastName() { return null; }
		}
		public class X {
			void test1 (List<Person> people) {
				people.stream().forEach(p -> System.out.println(p.)); // NOK
			}
		   void test2(List<Person> people) {
		       people.sort((x,y) -> "" + x.);\s
		   }
		}
		""";

			String completeBehind = "x.";
			int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

			String expectedCompletionNodeToString = "<CompleteOnName:x.>";
			String expectedParentNodeToString = "(\"\" + <CompleteOnName:x.>)";
			String completionIdentifier = "";
			String expectedReplacedSource = "x.";
			String expectedUnitDisplayString =
					"""
				import java.util.List;
				class Person {
				  Person() {
				  }
				  String getLastName() {
				  }
				}
				public class X {
				  public X() {
				  }
				  void test1(List<Person> people) {
				  }
				  void test2(List<Person> people) {
				    people.sort((<no type> x, <no type> y) -> ("" + <CompleteOnName:x.>));
				  }
				}
				""";

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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428735,  [1.8][assist] Missing completion proposals inside lambda body expression - other than first token
public void test428735e() { // field
	String string =
			"""
		class Person {
		   String getLastName() { return null; }
		}
		interface I {
			int foo(Person p, Person q);
		}
		public class X {
			I i =  (x, y) -> 10 + x.getLastName().compareTo(y.get);
		}
		""";

			String completeBehind = "y.get";
			int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

			String expectedCompletionNodeToString = "<CompleteOnName:y.get>";
			String expectedParentNodeToString = "x.getLastName().compareTo(<CompleteOnName:y.get>)";
			String completionIdentifier = "get";
			String expectedReplacedSource = "y.get";
			String expectedUnitDisplayString =
					"""
				class Person {
				  Person() {
				  }
				  String getLastName() {
				  }
				}
				interface I {
				  int foo(Person p, Person q);
				}
				public class X {
				  I i = (<no type> x, <no type> y) -> (10 + x.getLastName().compareTo(<CompleteOnName:y.get>));
				  public X() {
				  }
				}
				""";

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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428735,  [1.8][assist] Missing completion proposals inside lambda body expression - other than first token
public void test428735f() { // local
	String string =
			"""
		class Person {
		   String getLastName() { return null; }
		}
		interface I {
			int foo(Person p, Person q);
		}
		public class X {
		   void foo() {
			    I i =  (x, y) -> 10 + x.getLastName().compareTo(y.get);
		   }
		}
		""";

			String completeBehind = "y.get";
			int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

			String expectedCompletionNodeToString = "<CompleteOnName:y.get>";
			String expectedParentNodeToString = "x.getLastName().compareTo(<CompleteOnName:y.get>)";
			String completionIdentifier = "get";
			String expectedReplacedSource = "y.get";
			String expectedUnitDisplayString =
					"""
				class Person {
				  Person() {
				  }
				  String getLastName() {
				  }
				}
				interface I {
				  int foo(Person p, Person q);
				}
				public class X {
				  public X() {
				  }
				  void foo() {
				    I i = (<no type> x, <no type> y) -> (10 + x.getLastName().compareTo(<CompleteOnName:y.get>));
				  }
				}
				""";

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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428735,  [1.8][assist] Missing completion proposals inside lambda body expression - other than first token
public void test428735g() { // initializer block
	String string =
			"""
		import java.util.List;
		class Person {
		   String getLastName() { return null; }
		}
		interface I {
			int foo(Person p, Person q);
		}
		public class X {
		   List<Person> people;
		   {
		       people.sort((x,y) -> "" + x.);\s
		   }
		}
		""";

			String completeBehind = "x.";
			int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

			String expectedCompletionNodeToString = "<CompleteOnName:x.>";
			String expectedParentNodeToString = "(\"\" + <CompleteOnName:x.>)";
			String completionIdentifier = "";
			String expectedReplacedSource = "x.";
			String expectedUnitDisplayString =
					"""
				import java.util.List;
				class Person {
				  Person() {
				  }
				  String getLastName() {
				  }
				}
				interface I {
				  int foo(Person p, Person q);
				}
				public class X {
				  List<Person> people;
				  {
				    people.sort((<no type> x, <no type> y) -> ("" + <CompleteOnName:x.>));
				  }
				  public X() {
				  }
				}
				""";

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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=402081, [1.8][code complete] No proposals while completing at method/constructor references
public void test402081() { // initializer block
	String string =
			"""
		interface I {
		    String foo(String x);
		}
		public class X {
		    public  String longMethodName(String x) {
		        return null;
		    }
		    void foo() {
		    	X x = new X();
		    	I i = x::long
		       System.out.println();
		    }
		}
		""";

			String completeBehind = "long";
			int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

			String expectedCompletionNodeToString = "<CompletionOnReferenceExpressionName:x::long>";
			String expectedParentNodeToString = "I i = <CompletionOnReferenceExpressionName:x::long>;";
			String completionIdentifier = "long";
			String expectedReplacedSource = "x::long";
			String expectedUnitDisplayString =
					"""
				interface I {
				  String foo(String x);
				}
				public class X {
				  public X() {
				  }
				  public String longMethodName(String x) {
				  }
				  void foo() {
				    X x;
				    I i = <CompletionOnReferenceExpressionName:x::long>;
				  }
				}
				""";

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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430656, [1.8][content assist] Content assist does not work for method reference argument
public void test430656() {
	String string =
			"""
		import java.util.ArrayList;
		import java.util.Collections;
		import java.util.Comparator;
		import java.util.List;
		public class X {
			public void bar() {
				List<Person> people = new ArrayList<>();
				Collections.sort(people, Comparator.comparing(Person::get));\s
			}
		}
		class Person {
			String getLastName() {
				return null;
			}
		}
		""";

			String completeBehind = "get";
			int cursorLocation = string.indexOf(completeBehind) + completeBehind.length() - 1;

			String expectedCompletionNodeToString = "<CompletionOnReferenceExpressionName:Person::get>";
			String expectedParentNodeToString = "Comparator.comparing(<CompletionOnReferenceExpressionName:Person::get>)";
			String completionIdentifier = "get";
			String expectedReplacedSource = "Person::get";
			String expectedUnitDisplayString =
					"""
				import java.util.ArrayList;
				import java.util.Collections;
				import java.util.Comparator;
				import java.util.List;
				public class X {
				  public X() {
				  }
				  public void bar() {
				    List<Person> people;
				    Collections.sort(people, Comparator.comparing(<CompletionOnReferenceExpressionName:Person::get>));
				  }
				}
				class Person {
				  Person() {
				  }
				  String getLastName() {
				  }
				}
				""";

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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=438952, [1.8][content assist] StackOverflowError at org.eclipse.jdt.internal.compiler.ast.SingleTypeReference.traverse(SingleTypeReference.java:108)
public void test438952() {
	String string =
			"""
		import java.util.function.Supplier;
		class SO {
			{
				int
				Supplier<SO> m6 = SO::new;
				m6 = () -> new SO() {
					void test() {
						/* here */                           \s
					}
				};
			}
		}
		""";

			String completeBehind = "/* here */";
			int cursorLocation = string.indexOf(completeBehind) + completeBehind.length() - 1;

			String expectedCompletionNodeToString = "<CompleteOnName:>";
			String expectedParentNodeToString = "<NONE>";
			String completionIdentifier = "";
			String expectedReplacedSource = "";
			String expectedUnitDisplayString =
					"""
				import java.util.function.Supplier;
				class SO {
				  {
				    int Supplier;
				    m6 = () -> new SO() {
				  void test() {
				    <CompleteOnName:>;
				  }
				};
				  }
				  SO() {
				  }
				}
				""";

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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=435219, [1.8][content assist] No proposals for some closure cases
public void test435219() {
			String string =
				"""
				import java.util.Arrays;
				import java.util.List;
				public class X {
					public static void main(String[] args) {
						List<Integer> costBeforeTax = Arrays.asList(100, 200, 300);
						   double bill = costBeforeTax.stream().map((cost) -> cost + 0.19 * cost)
						        //                        .y                   .n             .y
						      .reduce((sum, cost) -> sum.doubleValue() + cost.dou
					}
				}
				""";

			String completeBehind = "dou";
			int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

			String expectedCompletionNodeToString = "<CompleteOnName:cost.dou>";
			String expectedParentNodeToString = "(sum.doubleValue() + <CompleteOnName:cost.dou>)";
			String completionIdentifier = "dou";
			String expectedReplacedSource = "cost.dou";
			String expectedUnitDisplayString =
					"""
				import java.util.Arrays;
				import java.util.List;
				public class X {
				  public X() {
				  }
				  public static void main(String[] args) {
				    List<Integer> costBeforeTax;
				    double bill = costBeforeTax.stream().map((<no type> cost) -> (cost + (0.19 * cost))).reduce((<no type> sum, <no type> cost) -> (sum.doubleValue() + <CompleteOnName:cost.dou>));
				  }
				}
				""";

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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=435682, [1.8] content assist not working inside lambda expression
public void test435682() {
			String string =
					"""
				import java.util.Arrays;
				import java.util.List;
				public class X {
					public static void main(String[] args) {
						List<String> words = Arrays.asList("hi", "hello", "hola", "bye", "goodbye");
						List<String> list1 = words.stream().map(so -> so.).collect(Collectors.toList());
					}
				}
				""";

			String completeBehind = "so.";
			int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

			String expectedCompletionNodeToString = "<CompleteOnName:so.>";
			String expectedParentNodeToString = "(<no type> so) -> <CompleteOnName:so.>";
			String completionIdentifier = "";
			String expectedReplacedSource = "so.";
			String expectedUnitDisplayString =
					"""
				import java.util.Arrays;
				import java.util.List;
				public class X {
				  public X() {
				  }
				  public static void main(String[] args) {
				    List<String> words;
				    List<String> list1 = words.stream().map((<no type> so) -> <CompleteOnName:so.>).collect(Collectors.toList());
				  }
				}
				""";

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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430667, [1.8][content assist] no proposals around lambda as a field
public void test430667() {
			String string =
					"""
				interface D_FI {
					void print(String value, int n);
				}
				class D_DemoRefactorings {
				\t
					D_FI fi1= (String value, int n) -> {
						for (int j = 0; j < n; j++) {
							System.out.println(value); 		\t
						}
					};
					D_F
				}
				""";

			String completeBehind = "D_F";
			int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

			String expectedCompletionNodeToString = "<CompleteOnType:D_F>";
			String expectedParentNodeToString = "<NONE>";
			String completionIdentifier = "D_F";
			String expectedReplacedSource = "D_F";
			String expectedUnitDisplayString =
					"""
				interface D_FI {
				  void print(String value, int n);
				}
				class D_DemoRefactorings {
				  D_FI fi1;
				  <CompleteOnType:D_F>;
				  D_DemoRefactorings() {
				  }
				}
				""";

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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430667, [1.8][content assist] no proposals around lambda as a field
public void test430667a() {
			String string =
					"""
				class D_DemoRefactorings {
				\t
					D_FI fi1= (String value, int n) -> {
						for (int j = 0; j < n; j++) {
							System.out.println(value); 		\t
						}
					};
					/*HERE*/D_F
				}
				interface D_FI {
					void print(String value, int n);
				}
				""";


			String completeBehind = "/*HERE*/D_F";
			int cursorLocation = string.lastIndexOf(completeBehind) + completeBehind.length() - 1;

			String expectedCompletionNodeToString = "<CompleteOnType:D_F>";
			String expectedParentNodeToString = "<NONE>";
			String completionIdentifier = "D_F";
			String expectedReplacedSource = "D_F";
			String expectedUnitDisplayString =
					"""
				class D_DemoRefactorings {
				  D_FI fi1;
				  <CompleteOnType:D_F>;
				  D_DemoRefactorings() {
				  }
				}
				interface D_FI {
				  void print(String value, int n);
				}
				""";

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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430667, [1.8][content assist] no proposals around lambda as a field
public void test430667b() {
			String string =
					"""
				public class D_DemoRefactorings {
				   D_F
					D_FI fi1= (String value, int n) -> {
						for (int j = 0; j < n; j++) {
							System.out.println(value); 		\t
						}
					};
				}
				interface D_FI {
					void print(String value, int n);
				}
				""";

			String completeBehind = "D_F";
			int cursorLocation = string.indexOf(completeBehind) + completeBehind.length() - 1;

			String expectedCompletionNodeToString = "<CompleteOnType:D_F>;";
			String expectedParentNodeToString = "<NONE>";
			String completionIdentifier = "D_F";
			String expectedReplacedSource = "D_F";
			String expectedUnitDisplayString =
					"""
				public class D_DemoRefactorings {
				  <CompleteOnType:D_F>;
				  D_FI fi1;
				  public D_DemoRefactorings() {
				  }
				}
				interface D_FI {
				  void print(String value, int n);
				}
				""";

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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430667, [1.8][content assist] no proposals around lambda as a field
public void test430667c() {
			String string =
					"""
				public interface Foo {
					int run(int s1, int s2);
				}
				interface B {
					static Foo f = (int x5, int x2) -> anot
					static int another = 3;
				  	static int two () { return 2; }
				}""";

			String completeBehind = "(int x5, int x2) -> anot";
			int cursorLocation = string.indexOf(completeBehind) + completeBehind.length() - 1;

			String expectedCompletionNodeToString = "<CompleteOnName:anot>";
			String expectedParentNodeToString = "(int x5, int x2) -> <CompleteOnName:anot>";
			String completionIdentifier = "anot";
			String expectedReplacedSource = "anot";
			String expectedUnitDisplayString =
					"""
				public interface Foo {
				  int run(int s1, int s2);
				}
				interface B {
				  static Foo f = (int x5, int x2) -> <CompleteOnName:anot>;
				  static int another;
				  <clinit>() {
				  }
				  static int two() {
				  }
				}
				""";

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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430667, [1.8][content assist] no proposals around lambda as a field
public void test430667d() {
			String string =
					"""
				import java.util.Arrays;
				import java.util.List;
				public class X {
						List<Integer> list = Arrays.asList(1, 2, 3);
						Object o = list.stream().map((x) -> x * x.hashCode()).forEach(System.out::pri);
				}
				""";

			String completeBehind = "pri";
			int cursorLocation = string.indexOf(completeBehind) + completeBehind.length() - 1;

			String expectedCompletionNodeToString = "<CompletionOnReferenceExpressionName:System.out::pri>";
			String expectedParentNodeToString = "list.stream().map((<no type> x) -> (x * x.hashCode())).forEach(<CompletionOnReferenceExpressionName:System.out::pri>)";
			String completionIdentifier = "pri";
			String expectedReplacedSource = "System.out::pri";
			String expectedUnitDisplayString =
					"""
				import java.util.Arrays;
				import java.util.List;
				public class X {
				  List<Integer> list;
				  Object o = list.stream().map((<no type> x) -> (x * x.hashCode())).forEach(<CompletionOnReferenceExpressionName:System.out::pri>);
				  public X() {
				  }
				}
				""";

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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=446765,
public void test446765() {
			String string =
					"""
				class Stepper<T> {
				    public interface Step<T> {
				        void run();
				    }
				    public Stepper(Handler<AsyncResult<T>> handler) {}
				
				    @SafeVarargs
				    public final void run(Step<T> ... steps) {}
				}
				interface AsyncResult<T> {}
				interface Handler<E> {
				    void handle(E event);
				}
				class Z {
				    void foo() {}
				}
				interface I {
				    void foo(Z z);
				}
				class Y {
				    void request(I i) {}
				}
				public class X {
				    void test() {
				        new Stepper<Void>(r -> {}) {
				            private void step1() {
				                Y y = new Y();
				                y.request(response -> {
				                    if (response.)
				                });
				            }
				        }.run();       \s
				    }   \s
				}
				""";

			String completeBehind = "response.";
			int cursorLocation = string.indexOf(completeBehind) + completeBehind.length() - 1;

			String expectedCompletionNodeToString = "<CompleteOnName:response.>";
			String expectedParentNodeToString = "if (<CompleteOnName:response.>)\n" +
												"    ;";
			String completionIdentifier = "";
			String expectedReplacedSource = "response.";
			String expectedUnitDisplayString =
					"""
				class Stepper<T> {
				  public interface Step<T> {
				    void run();
				  }
				  public Stepper(Handler<AsyncResult<T>> handler) {
				  }
				  public final @SafeVarargs void run(Step<T>... steps) {
				  }
				}
				interface AsyncResult<T> {
				}
				interface Handler<E> {
				  void handle(E event);
				}
				class Z {
				  Z() {
				  }
				  void foo() {
				  }
				}
				interface I {
				  void foo(Z z);
				}
				class Y {
				  Y() {
				  }
				  void request(I i) {
				  }
				}
				public class X {
				  public X() {
				  }
				  void test() {
				    new Stepper<Void>((<no type> r) -> {
				}) {
				  private void step1() {
				    Y y;
				    y.request((<no type> response) -> {
				  <CompleteOnName:response.>;
				});
				  }
				}.run();
				  }
				}
				""";

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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428735,  [1.8][assist] Missing completion proposals inside lambda body expression - other than first token
public void test428735h()  {
	String string =
			"""
		import java.util.List;
		class Person {
		   String getLastName() { return null; }
		}
		public class X {
		   void test2(List<Person> people) {
		       people.sort((x,y) -> {
		              if (true) return "" + x.get);\s
		              else return "";
		   }
		}
		""";

	String completeBehind = "x.get";
	int cursorLocation = string.indexOf(completeBehind) + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:x.get>";
	String expectedParentNodeToString = "(\"\" + <CompleteOnName:x.get>)";
	String completionIdentifier = "get";
	String expectedReplacedSource = "x.get";
	String expectedUnitDisplayString =
			"""
		import java.util.List;
		class Person {
		  Person() {
		  }
		  String getLastName() {
		  }
		}
		public class X {
		  public X() {
		  }
		  void test2(List<Person> people) {
		    people.sort((<no type> x, <no type> y) -> {
		  if (true)
		      return ("" + <CompleteOnName:x.get>);
		  ;
		  return "";
		});
		  }
		}
		""";

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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422468, [1.8][assist] Code assist issues with type elided lambda parameters
public void test422468() { // computing visible elements in lambda scope.
	String string =
			"""
		interface I {
			void foo(X x);
		}
		public class X {
			static X xField;
			static X goo(String s) {
		       return null;
			}
			static void goo(I i) {
			}
			public static void main(String[] args) {
		       X xLocal = null;
		       args = null;
		       if (args != null) {
		           xField = null;
		       else\s
		           xField = null;
		       while (true);
				goo((xyz) -> {
		           X xLambdaLocal = null;
					System.out.println(xyz.)
				});
			}
		}
		""";
	String completeBehind = "xyz.";
	int cursorLocation = string.indexOf(completeBehind) + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:xyz.>";
	String expectedParentNodeToString = "System.out.println(<CompleteOnName:xyz.>)";
	String completionIdentifier = "";
	String expectedReplacedSource = "xyz.";
	String expectedUnitDisplayString =
			"""
		interface I {
		  void foo(X x);
		}
		public class X {
		  static X xField;
		  public X() {
		  }
		  <clinit>() {
		  }
		  static X goo(String s) {
		  }
		  static void goo(I i) {
		  }
		  public static void main(String[] args) {
		    X xLocal;
		    {
		      {
		        goo((<no type> xyz) -> {
		  X xLambdaLocal;
		  System.out.println(<CompleteOnName:xyz.>);
		  ;
		});
		      }
		    }
		  }
		}
		""";

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=473008
public void test473008() {
	String string =
			"""
		interface FooFunctional {
		   void function();
		}
		public class Foo {
		    public void bar() {
		      private FooFunctional lambda = this::bar;
		      new StringBuffer(\
		    }
		}
		""";
	String completeBehind = "StringBuffer(";
	int cursorLocation = string.indexOf(completeBehind) + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnAllocationExpression:new StringBuffer()>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	String expectedUnitDisplayString =
			"""
		interface FooFunctional {
		  void function();
		}
		public class Foo {
		  public Foo() {
		  }
		  public void bar() {
		    private FooFunctional lambda;
		    <CompleteOnAllocationExpression:new StringBuffer()>;
		  }
		}
		""";

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
