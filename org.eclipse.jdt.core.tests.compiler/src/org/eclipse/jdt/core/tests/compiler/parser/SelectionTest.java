/*******************************************************************************
 * Copyright (c) 2000, 2022 IBM Corporation and others.
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

public class SelectionTest extends AbstractSelectionTest {
static {
//		TESTS_NUMBERS = new int[] { 53 };
}
public static Test suite() {
	return buildAllCompliancesTestSuite(SelectionTest.class);
}

public SelectionTest(String testName) {
	super(testName);
}
/*
 * Select superclass
 */
public void test01() {

	String str =
		"""
		import java.io.*;						\t
												\t
		public class X extends IOException {	\t
		}										\t
		""";

	String selectionStartBehind = "extends ";
	String selectionEndBehind = "IOException";

	String expectedCompletionNodeToString = "<SelectOnType:IOException>";
	String completionIdentifier = "IOException";
	String expectedUnitDisplayString =
		"""
		import java.io.*;
		public class X extends <SelectOnType:IOException> {
		  public X() {
		  }
		}
		""";
	String expectedReplacedSource = "IOException";
	String testName = "<select superclass>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

	checkDietParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Select superinterface
 */
public void test02() {

	String str =
		"""
		import java.io.*;												\t
																		\t
		public class X extends IOException implements Serializable {	\t
		 int foo(){} 													\t
		}																\t
		""";

	String selectionStartBehind = "implements ";
	String selectionEndBehind = "Serializable";

	String expectedCompletionNodeToString = "<SelectOnType:Serializable>";
	String completionIdentifier = "Serializable";
	String expectedUnitDisplayString =
		"""
		import java.io.*;
		public class X extends IOException implements <SelectOnType:Serializable> {
		  public X() {
		  }
		  int foo() {
		  }
		}
		""";
	String expectedReplacedSource = "Serializable";
	String testName = "<select superinterface>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

	checkDietParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Select qualified superclass
 */
public void test03() {

	String str =
		"public class X extends java.io.IOException {	\n" +
		"}												\n";

	String selectionStartBehind = "java.io.";
	String selectionEndBehind = "IOException";

	String expectedCompletionNodeToString = "<SelectOnType:java.io.IOException>";
	String completionIdentifier = "IOException";
	String expectedUnitDisplayString =
		"""
		public class X extends <SelectOnType:java.io.IOException> {
		  public X() {
		  }
		}
		""";
	String expectedReplacedSource = "java.io.IOException";
	String testName = "<select qualified superclass>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

	checkDietParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Select package from qualified superclass
 */
public void test04() {

	String str =
		"public class X extends java.io.IOException {	\n" +
		"}												\n";

	String selectionStartBehind = "java.";
	String selectionEndBehind = "java.io";

	String expectedCompletionNodeToString = "<SelectOnType:java.io>";
	String completionIdentifier = "io";
	String expectedUnitDisplayString =
		"""
		public class X extends <SelectOnType:java.io> {
		  public X() {
		  }
		}
		""";
	String expectedReplacedSource = "java.io.IOException";
	String testName = "<select package from qualified superclass>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

	checkDietParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Select message send
 */
public void test05() {

	String str =
		"""
		public class X extends java.io.IOException {\t
			int foo(){								\t
				System.out.println("hello");		\t
		""";

	String selectionStartBehind = "System.out.";
	String selectionEndBehind = "println";

	String expectedCompletionNodeToString = "<SelectOnMessageSend:System.out.println(\"hello\")>";
	String completionIdentifier = "println";
	String expectedUnitDisplayString =
		"""
		public class X extends java.io.IOException {
		  public X() {
		  }
		  int foo() {
		    <SelectOnMessageSend:System.out.println("hello")>;
		  }
		}
		""";
	String expectedReplacedSource = "System.out.println(\"hello\")";
	String testName = "<select message send>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Select message send with recovery before
 */
public void test06() {

	String str =
		"""
		public class X extends 					\t
			int foo(){								\t
				System.out.println("hello");		\t
		""";

	String selectionStartBehind = "System.out.";
	String selectionEndBehind = "println";

	String expectedCompletionNodeToString = "<SelectOnMessageSend:System.out.println(\"hello\")>";
	String completionIdentifier = "println";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  int foo() {
		    <SelectOnMessageSend:System.out.println("hello")>;
		  }
		}
		""";
	String expectedReplacedSource = "System.out.println(\"hello\")";
	String testName = "<select message send with recovery before>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Select message send with sibling method
 */
public void test07() {

	String str =
		"""
		public class X extends 					\t
			int foo(){								\t
				this.bar("hello");				\t
			int bar(String s){						\t
				return s.length();					\t
			}										\t
		}											\t
		""";

	String selectionStartBehind = "this.";
	String selectionEndBehind = "this.bar";

	String expectedCompletionNodeToString = "<SelectOnMessageSend:this.bar(\"hello\")>";
	String completionIdentifier = "bar";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  int foo() {
		    <SelectOnMessageSend:this.bar("hello")>;
		  }
		  int bar(String s) {
		  }
		}
		""";
	String expectedReplacedSource = "this.bar(\"hello\")";
	String testName = "<select message send with sibling method>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Select field reference
 */
public void test08() {

	String str =
		"""
		public class X {		 					\t
			int num = 0;							\t
			int foo(){								\t
				int j = this.num;					\t
		}											\t
		""";

	String selectionStartBehind = "this.";
	String selectionEndBehind = "this.num";

	String expectedCompletionNodeToString = "<SelectionOnFieldReference:this.num>";
	String completionIdentifier = "num";
	String expectedUnitDisplayString =
		"""
		public class X {
		  int num;
		  public X() {
		  }
		  int foo() {
		    int j = <SelectionOnFieldReference:this.num>;
		  }
		}
		""";
	String expectedReplacedSource = "this.num";
	String testName = "<select field reference>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Select field reference with syntax errors
 */
public void test09() {

	String str =
		"""
		public class X 		 					\t
			int num 								\t
			int foo(){								\t
				int j = this.num;					\t
		}											\t
		""";

	String selectionStartBehind = "this.";
	String selectionEndBehind = "this.num";

	String expectedCompletionNodeToString = "<SelectionOnFieldReference:this.num>";
	String completionIdentifier = "num";
	String expectedUnitDisplayString =
		"""
		public class X {
		  int num;
		  public X() {
		  }
		  int foo() {
		    int j = <SelectionOnFieldReference:this.num>;
		  }
		}
		""";
	String expectedReplacedSource = "this.num";
	String testName = "<select field reference with syntax errors>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Select field reference inside message receiver
 */
public void test10() {

	String str =
		"""
		public class X {		 				\t
			X x; 								\t
			int foo(){							\t
				int j = this.x.foo();			\t
		}										\t
		""";

	String selectionStartBehind = "this.";
	String selectionEndBehind = "this.x";

	String expectedCompletionNodeToString = "<SelectionOnFieldReference:this.x>";
	String completionIdentifier = "x";
	String expectedUnitDisplayString =
		"""
		public class X {
		  X x;
		  public X() {
		  }
		  int foo() {
		    int j = <SelectionOnFieldReference:this.x>;
		  }
		}
		""";
	String expectedReplacedSource = "this.x";
	String testName = "<select field reference inside message receiver>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Select allocation
 */
public void test11() {

	String str =
		"""
		public class X {		 				\t
			X(int i){}							\t
			int foo(){							\t
				int j = 0;						\t
				X x = new X(j);					\t
			}									\t
		}										\t
		""";

	String selectionStartBehind = "new ";
	String selectionEndBehind = "new X";

	String expectedCompletionNodeToString = "<SelectOnAllocationExpression:new X(j)>";
	String completionIdentifier = "X";
	String expectedUnitDisplayString =
		"""
		public class X {
		  X(int i) {
		  }
		  int foo() {
		    int j;
		    X x = <SelectOnAllocationExpression:new X(j)>;
		  }
		}
		""";
	String expectedReplacedSource = "new X(j)";
	String testName = "<select allocation>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Select qualified allocation
 */
public void test12() {

	String str =
		"""
		public class X {		 				\t
		 	class Y {							\t
				Y(int i){}						\t
			}									\t
			X(int i){}							\t
			int foo(){							\t
				int j = 0;						\t
				X x = new X(j);					\t
				x.new Y(1);						\t
			}									\t
		}										\t
		""";

	String selectionStartBehind = "x.new ";
	String selectionEndBehind = "x.new Y";

	String expectedCompletionNodeToString = "<SelectOnQualifiedAllocationExpression:x.new Y(1)>";
	String completionIdentifier = "Y";
	String expectedUnitDisplayString =
		"""
		public class X {
		  class Y {
		    Y(int i) {
		    }
		  }
		  X(int i) {
		  }
		  int foo() {
		    int j;
		    X x;
		    <SelectOnQualifiedAllocationExpression:x.new Y(1)>;
		  }
		}
		""";
	String expectedReplacedSource = "x.new Y(1)";
	String testName = "<select qualified allocation>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Select qualified name reference receiver
 */
public void test13() {

	String str =
		"""
		public class X {		 				\t
			int foo(){							\t
				java.lang.System.out.println();	\t
			}									\t
		}										\t
		""";

	String selectionStartBehind = "java.lang.";
	String selectionEndBehind = "java.lang.System";

	String expectedCompletionNodeToString = "<SelectOnName:java.lang.System>";
	String completionIdentifier = "System";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  int foo() {
		    <SelectOnName:java.lang.System>;
		  }
		}
		""";
	String expectedReplacedSource = "java.lang.System.out";
	String testName = "<select qualified name receiver>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Select qualified name reference
 */
public void test14() {

	String str =
		"""
		public class X {		 				\t
			int foo(){							\t
				System sys = java.lang.System;	\t
			}									\t
		}										\t
		""";

	String selectionStartBehind = "java.lang.";
	String selectionEndBehind = "java.lang.System";

	String expectedCompletionNodeToString = "<SelectOnName:java.lang.System>";
	String completionIdentifier = "System";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  int foo() {
		    System sys = <SelectOnName:java.lang.System>;
		  }
		}
		""";
	String expectedReplacedSource = "java.lang.System";
	String testName = "<select qualified name>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Select variable type with modifier
 */
public void test15() {

	String str =
		"""
		public class X {		 				\t
			int foo(){							\t
				final System sys = null;		\t
			}									\t
		}										\t
		""";

	String selectionStartBehind = "final ";
	String selectionEndBehind = "final System";

	String expectedCompletionNodeToString = "<SelectOnType:System>";
	String completionIdentifier = "System";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  int foo() {
		    final <SelectOnType:System> sys;
		  }
		}
		""";
	String expectedReplacedSource = "System";
	String testName = "<select variable type with modifier>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Select variable type
 */
public void test16() {

	String str =
		"""
		public class X {		 				\t
			int foo(){							\t
				System sys = null;				\t
			}									\t
		}										\t
		""";

	String selectionStartBehind = "\n		";
	String selectionEndBehind = "\n		System";

	String expectedCompletionNodeToString = "<SelectOnType:System>";
	String completionIdentifier = "System";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  int foo() {
		    <SelectOnType:System> sys;
		  }
		}
		""";
	String expectedReplacedSource = "System";
	String testName = "<select variable type>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Select name
 */
public void test17() {

	String str =
		"""
		public class X {		 				\t
			int foo(){							\t
				System 							\t
			}									\t
		}										\t
		""";

	String selectionStartBehind = "\n		";
	String selectionEndBehind = "\n		System";

	String expectedCompletionNodeToString = "<SelectOnName:System>";
	String completionIdentifier = "System";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  int foo() {
		    <SelectOnName:System>;
		  }
		}
		""";

	String expectedReplacedSource = "System";
	String testName = "<select name>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Select anonymous type
 */
public void test18() {

	String str =
		"""
		public class X {		 				\t
			int foo(){							\t
				new Object(){					\t
					int bar(){}					\t
				}								\t
			}									\t
		}										\t
		""";

	String selectionStartBehind = "new ";
	String selectionEndBehind = "new Object";

	String expectedCompletionNodeToString =
		"<SelectOnAllocationExpression:new Object() {\n" +
		"}>";
	String completionIdentifier = "Object";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  int foo() {
		    <SelectOnAllocationExpression:new Object() {
		    }>;
		  }
		}
		""";

	String expectedReplacedSource = "new Object()";
	String testName = "<select anonymous type>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Select cast type
 */
public void test19() {

	String str =
		"""
		public class X {		 				\t
			Object foo(){						\t
				return (Object) this;			\t
				}								\t
			}									\t
		}										\t
		""";

	String selectionStartBehind = "return (";
	String selectionEndBehind = "return (Object";

	String expectedCompletionNodeToString = "<SelectOnType:Object>";
	String completionIdentifier = "Object";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  Object foo() {
		    return (<SelectOnType:Object>) this;
		  }
		}
		""";

	String expectedReplacedSource = "Object";
	String testName = "<select cast type>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Select package
 */
public void test20() {

	String str =
		"""
		package x.y.other;				\t
		public class X {		 				\t
			int foo(){							\t
				}								\t
			}									\t
		}										\t
		""";

	String selectionStartBehind = "x.";
	String selectionEndBehind = "x.y";

	String expectedCompletionNodeToString = "<SelectOnPackage:x.y>";
	String completionIdentifier = "y";
	String expectedUnitDisplayString =
		"""
		package <SelectOnPackage:x.y>;
		public class X {
		  public X() {
		  }
		  int foo() {
		  }
		}
		""";

	String expectedReplacedSource = "x.y.other";
	String testName = "<select package>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

	checkDietParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Select import
 */
public void test21() {

	String str =
		"""
		import x.y.Other;				\t
		public class X {		 				\t
			int foo(){							\t
				}								\t
			}									\t
		}										\t
		""";

	String selectionStartBehind = "y.";
	String selectionEndBehind = "y.Other";

	String expectedCompletionNodeToString = "<SelectOnImport:x.y.Other>";
	String completionIdentifier = "Other";
	String expectedUnitDisplayString =
		"""
		import <SelectOnImport:x.y.Other>;
		public class X {
		  public X() {
		  }
		  int foo() {
		  }
		}
		""";

	String expectedReplacedSource = "x.y.Other";
	String testName = "<select import>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

	checkDietParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Select import on demand
 */
public void test22() {

	String str =
		"""
		import x.y.other.*;				\t
		public class X {		 				\t
			int foo(){							\t
				}								\t
			}									\t
		}										\t
		""";

	String selectionStartBehind = "y.";
	String selectionEndBehind = "y.other";

	String expectedCompletionNodeToString = "<SelectOnImport:x.y.other>";
	String completionIdentifier = "other";
	String expectedUnitDisplayString =
		"""
		import <SelectOnImport:x.y.other>;
		public class X {
		  public X() {
		  }
		  int foo() {
		  }
		}
		""";

	String expectedReplacedSource = "x.y.other";
	String testName = "<select import on demand>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

	checkDietParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Select array initializer type
 */
public void test23() {

	String str =
		"""
		public class X {		 				\t
			int foo(){							\t
				String[] p = new String[]{"Left"};
			}									\t
		}										\t
		""";

	String selectionStartBehind = "new ";
	String selectionEndBehind = "new String";
	String expectedCompletionNodeToString = "<SelectOnType:String>";
	String completionIdentifier = "String";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  int foo() {
		    String[] p = <SelectOnType:String>;
		  }
		}
		""";

	String expectedReplacedSource = "String";
	String testName = "<select array initializer type>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Select nested type superclass with syntax error behind
 */
public void test24() {

	String str =
		"""
		public class G {				\t
			void foo() {				\t
				class X {				\t
					class Y extends G {	\t
						int foo()		\t
					}					\t
				}						\t
			}							\t
		}								\t
		""";

	String selectionStartBehind = "extends ";
	String selectionEndBehind = "extends G";

	String expectedCompletionNodeToString = "<SelectOnType:G>";

	String completionIdentifier = "G";
	String expectedUnitDisplayString =
		"""
		public class G {
		  public G() {
		  }
		  void foo() {
		    class X {
		      class Y extends <SelectOnType:G> {
		        Y() {
		        }
		        int foo() {
		        }
		      }
		      X() {
		      }
		    }
		  }
		}
		""";

	String expectedReplacedSource = "G";
	String testName = "<select nested type superclass>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Select super
 */
public void test25() {

	String str =
		"""
		public class G {				\t
			Object foo() {				\t
				return super.foo();		\t
			}							\t
		}								\t
		""";

	String selectionStartBehind = "return ";
	String selectionEndBehind = "return super";

	String expectedCompletionNodeToString = "<SelectOnSuper:super>";

	String completionIdentifier = "super";
	String expectedUnitDisplayString =
		"""
		public class G {
		  public G() {
		  }
		  Object foo() {
		    return <SelectOnSuper:super>;
		  }
		}
		""";

	String expectedReplacedSource = "super";
	String testName = "<select super>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Select qualified super
 */
public void test26() {

	String str =
		"""
		public class G {					\t
			Object foo() {					\t
				new X(){					\t
					Object bar(){			\t
						return G.super.foo();\t
					}						\t
				}							\t
			}								\t
		}									\t
		""";

	String selectionStartBehind = "G.";
	String selectionEndBehind = "G.super";

	String expectedCompletionNodeToString = "<SelectOnQualifiedSuper:G.super>";

	String completionIdentifier = "super";
	String expectedUnitDisplayString =
		"""
		public class G {
		  public G() {
		  }
		  Object foo() {
		    new X() {
		      Object bar() {
		        return <SelectOnQualifiedSuper:G.super>;
		      }
		    };
		  }
		}
		""";
	String expectedReplacedSource = "G.super";
	String testName = "<select qualified super>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Select super constructor call
 */
public void test27() {

	String str =
		"""
		public class G {				\t
			G() {						\t
				super();				\t
			}							\t
		}								\t
		""";

	String selectionStartBehind = "\n\t\t";
	String selectionEndBehind = "super";

	String expectedCompletionNodeToString = "<SelectOnExplicitConstructorCall:super()>;";

	String completionIdentifier = "super";
	String expectedUnitDisplayString =
		"""
		public class G {
		  G() {
		    <SelectOnExplicitConstructorCall:super()>;
		  }
		}
		""";

	String expectedReplacedSource = "super()";
	String testName = "<select super constructor call>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Select qualified super constructor call
 */
public void test28() {

	String str =
		"""
		public class G {					\t
			class M {}						\t
			static Object foo() {			\t
				class X extends M {			\t
					X (){					\t
						new G().super();	\t
					}						\t
				}							\t
			}								\t
		}									\t
		""";

	String selectionStartBehind = "new G().";
	String selectionEndBehind = "new G().super";

	String expectedCompletionNodeToString = "<SelectOnExplicitConstructorCall:new G().super()>;";

	String completionIdentifier = "super";
	String expectedUnitDisplayString =
		"""
		public class G {
		  class M {
		    M() {
		    }
		  }
		  public G() {
		  }
		  static Object foo() {
		    class X extends M {
		      X() {
		        <SelectOnExplicitConstructorCall:new G().super()>;
		      }
		    }
		  }
		}
		""";
	String expectedReplacedSource = "new G().super()";
	String testName = "<select qualified super constructor call>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Select qualified super constructor call with arguments
 */
public void test29() {

	String str =
		"""
		public class G {							\t
			class M {}								\t
			static Object foo() {					\t
				class X extends M {					\t
					X (){							\t
						new G().super(23 + "hello");\t
					}								\t
				}									\t
			}										\t
		}											\t
		""";

	String selectionStartBehind = "new G().";
	String selectionEndBehind = "new G().super";

	String expectedCompletionNodeToString = "<SelectOnExplicitConstructorCall:new G().super((23 + \"hello\"))>;";

	String completionIdentifier = "super";
	String expectedUnitDisplayString =
		"""
		public class G {
		  class M {
		    M() {
		    }
		  }
		  public G() {
		  }
		  static Object foo() {
		    class X extends M {
		      X() {
		        <SelectOnExplicitConstructorCall:new G().super((23 + "hello"))>;
		      }
		    }
		  }
		}
		""";
	String expectedReplacedSource = "new G().super(23 + \"hello\")";
	String testName = "<select qualified super constructor call with arguments>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Select super constructor call with arguments
 */
public void test30() {

	String str =
		"""
		public class G {				\t
			G() {						\t
				super(new G());			\t
			}							\t
		}								\t
		""";

	String selectionStartBehind = "\n\t\t";
	String selectionEndBehind = "super";

	String expectedCompletionNodeToString = "<SelectOnExplicitConstructorCall:super(new G())>;";

	String completionIdentifier = "super";
	String expectedUnitDisplayString =
		"""
		public class G {
		  G() {
		    <SelectOnExplicitConstructorCall:super(new G())>;
		  }
		}
		""";

	String expectedReplacedSource = "super(new G())";
	String testName = "<select super constructor call with arguments>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Regression test for 1FVQ0LK
 */
public void test31() {

	String str =
		"""
		class X {						\t
			Y f;						\t
			void foo() {				\t
				new Bar(fred());		\t
				Z z= new Z();			\t
			}							\t
		}								\t
		""";

	String selectionStartBehind = "\n\t";
	String selectionEndBehind = "Y";

	String expectedCompletionNodeToString = "<SelectOnType:Y>";

	String completionIdentifier = "Y";
	String expectedUnitDisplayString =
		"""
		class X {
		  <SelectOnType:Y> f;
		  X() {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedReplacedSource = "Y";
	String testName = "<regression test for 1FVQ0LK>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

	checkDietParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Regression test for 1FWT4AJ: ITPCOM:WIN98 - SelectionParser produces duplicate type declaration
 */
public void test32() {

	String str =
		"""
		package search;											\t
		import java.io.*;											\t
		public class PhraseQuery {									\t
			public boolean containsPhrase(){						\t
				try {												\t
						char currentChar = "hello".toLowerCase()	\t
			}														\t
		}															\t
		""";

	String selectionStartBehind = "\"hello\".";
	String selectionEndBehind = "\"hello\".toLowerCase";

	String expectedCompletionNodeToString = "<SelectOnMessageSend:\"hello\".toLowerCase()>";

	String completionIdentifier = "toLowerCase";
	String expectedUnitDisplayString =
		"""
		package search;
		import java.io.*;
		public class PhraseQuery {
		  public PhraseQuery() {
		  }
		  public boolean containsPhrase() {
		    {
		      char currentChar = <SelectOnMessageSend:"hello".toLowerCase()>;
		    }
		  }
		}
		""";

	String expectedReplacedSource = "\"hello\".toLowerCase()";
	String testName = "<1FWT4AJ: ITPCOM:WIN98 - SelectionParser produces duplicate type declaration>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Regression test for 1G4CLZM: ITPJUI:WINNT - 'Import Selection' - Set not found
 */
public void test33() {

	String str =
		"""
			import java.util.AbstractMap;			\t
			public class c4 extends AbstractMap {	\t
				/**									\t
				 * @see AbstractMap#entrySet		\t
				 */									\t
				public Set entrySet() {				\t
					return null;					\t
				}									\t
			}										\t
		""";

	String selectionStartBehind = "\n\t\tpublic ";
	String selectionEndBehind = "public Set";

	String expectedCompletionNodeToString = "<SelectOnType:Set>";

	String completionIdentifier = "Set";
	String expectedUnitDisplayString =
		"""
		import java.util.AbstractMap;
		public class c4 extends AbstractMap {
		  public c4() {
		  }
		  public <SelectOnType:Set> entrySet() {
		  }
		}
		""";

	String expectedReplacedSource = "Set";
	String testName = "<1G4CLZM: ITPJUI:WINNT - 'Import Selection' - Set not found>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

	checkDietParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Regression test for 1GB99S3: ITPJUI:WINNT - SH: NPE in editor while getting hover help
 */
public void test34() {

	String str =
		"""
		public class X {						\t
			public int foo() {					\t
				Object[] array = new Object[0];	\t
				return array.length;			\t
			}									\t
		}										\t
		""";

	String selectionStartBehind = "\n\t\treturn ";
	String selectionEndBehind = "array.length";

	String expectedCompletionNodeToString = NONE;

	String completionIdentifier = NONE;
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  public int foo() {
		    Object[] array;
		    return array.length;
		  }
		}
		""";

	String expectedReplacedSource = NONE;
	String testName = "<1GB99S3: ITPJUI:WINNT - SH: NPE in editor while getting hover help>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}

/*
 * Select this constructor call
 */
public void test35() {

	String str =
		"""
		public class G {				\t
			G() {						\t
			}							\t
			G(int x) {					\t
				this();					\t
			}							\t
		}								\t
		""";

	String selectionStartBehind = "\n\t\t";
	String selectionEndBehind = "this";

	String expectedCompletionNodeToString = "<SelectOnExplicitConstructorCall:this()>;";

	String completionIdentifier = "this";
	String expectedUnitDisplayString =
		"""
		public class G {
		  G() {
		  }
		  G(int x) {
		    <SelectOnExplicitConstructorCall:this()>;
		  }
		}
		""";

	String expectedReplacedSource = "this()";
	String testName = "<select this constructor call>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}

/*
 * Select qualified this constructor call
 */
public void test36() {

	String str =
		"""
		public class G {					\t
			static Object foo() {			\t
				class X {					\t
					X (){					\t
					}						\t
					X (int x){				\t
						new G().this();		\t
					}						\t
				}							\t
			}								\t
		}									\t
		""";

	String selectionStartBehind = "new G().";
	String selectionEndBehind = "new G().this";

	String expectedCompletionNodeToString = "<SelectOnExplicitConstructorCall:new G().this()>;";

	String completionIdentifier = "this";
	String expectedUnitDisplayString =
		"""
		public class G {
		  public G() {
		  }
		  static Object foo() {
		    class X {
		      X() {
		        super();
		      }
		      X(int x) {
		        <SelectOnExplicitConstructorCall:new G().this()>;
		      }
		    }
		  }
		}
		""";
	String expectedReplacedSource = "new G().this()";
	String testName = "<select qualified this constructor call>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Select qualified this constructor call with arguments
 */
public void test37() {

	String str =
		"""
		public class G {							\t
			static Object foo() {					\t
				class X {							\t
					X (){							\t
					}								\t
					X (int x){						\t
						new G().this(23 + "hello");\t
					}								\t
				}									\t
			}										\t
		}											\t
		""";

	String selectionStartBehind = "new G().";
	String selectionEndBehind = "new G().this";

	String expectedCompletionNodeToString = "<SelectOnExplicitConstructorCall:new G().this((23 + \"hello\"))>;";

	String completionIdentifier = "this";
	String expectedUnitDisplayString =
		"""
		public class G {
		  public G() {
		  }
		  static Object foo() {
		    class X {
		      X() {
		        super();
		      }
		      X(int x) {
		        <SelectOnExplicitConstructorCall:new G().this((23 + "hello"))>;
		      }
		    }
		  }
		}
		""";
	String expectedReplacedSource = "new G().this(23 + \"hello\")";
	String testName = "<select qualified this constructor call with arguments>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Select this constructor call with arguments
 */
public void test38() {

	String str =
		"""
		public class G {				\t
			G() {						\t
				this(new G());			\t
			}							\t
		}								\t
		""";

	String selectionStartBehind = "\n\t\t";
	String selectionEndBehind = "this";

	String expectedCompletionNodeToString = "<SelectOnExplicitConstructorCall:this(new G())>;";

	String completionIdentifier = "this";
	String expectedUnitDisplayString =
		"""
		public class G {
		  G() {
		    <SelectOnExplicitConstructorCall:this(new G())>;
		  }
		}
		""";

	String expectedReplacedSource = "this(new G())";
	String testName = "<select this constructor call with arguments>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * bugs 3293 search does not work in inner class (1GEUQHJ)
 */
public void test39() {

	String str =
		"""
		public class X {               \s
		  Object hello = new Object(){ \s
		    public void foo(String s){ \s
		      s.length();              \s
		    }                          \s
		  };                           \s
		}								\s
		""";

	String selectionStartBehind = "s.";
	String selectionEndBehind = "length";

	String expectedCompletionNodeToString = "<SelectOnMessageSend:s.length()>";

	String completionIdentifier = "length";
	String expectedUnitDisplayString =
		"""
		public class X {
		  Object hello = new Object() {
		    public void foo(String s) {
		      <SelectOnMessageSend:s.length()>;
		    }
		  };
		  public X() {
		  }
		}
		""";
	String expectedReplacedSource = "s.length()";
	String testName = "<select message send in anonymous class>";

	int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
	int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

	checkDietParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}

/*
 * bugs 3229 OpenOnSelection - strange behaviour of code resolve (1GAVL08)
 */
public void test40() {

	String str =
		"""
		public class X {               \s
		  Object                       \s
		}								\s
		""";

	String selection = "Object";

	String expectedCompletionNodeToString = "<SelectOnType:Object>";

	String completionIdentifier = "Object";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <SelectOnType:Object>;
		  public X() {
		  }
		}
		""";
	String expectedReplacedSource = "Object";
	String testName = "<select fake field>";

	int selectionStart = str.indexOf(selection);
	int selectionEnd = str.indexOf(selection) + selection.length() - 1;

	checkDietParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * bugs 11475 selection on local name.
 */
public void test41() {

	String str =
		"""
		public class X {               \s
		  public void foo(){                  \s
		    Object var;             \s
		  }                            \s
		}								\s
		""";

	String selection = "var";

	String expectedCompletionNodeToString = "<SelectionOnLocalName:Object var>;";

	String completionIdentifier = "var";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  public void foo() {
		    <SelectionOnLocalName:Object var>;
		  }
		}
		""";
	String expectedReplacedSource = "var";
	String testName = "<select local name>";

	int selectionStart = str.indexOf(selection);
	int selectionEnd = str.indexOf(selection) + selection.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * bugs 11475 selection on argument name.
 */
public void test42() {

	String str =
		"""
		public class X {               \s
		  public void foo(Object var){         \s
		  }                            \s
		}								\s
		""";

	String selection = "var";

	String expectedCompletionNodeToString = "<SelectionOnArgumentName:Object var>";

	String completionIdentifier = "var";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  public void foo(<SelectionOnArgumentName:Object var>) {
		  }
		}
		""";
	String expectedReplacedSource = "var";
	String testName = "<select argument name>";

	int selectionStart = str.indexOf(selection);
	int selectionEnd = str.indexOf(selection) + selection.length() - 1;

	checkDietParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * bugs 11475 selection on argument name inside catch statement.
 */
public void test43() {

	String str =
		"""
		public class X {               \s
		  public void foo(){                  \s
		    try{             \s
		    }catch(Object var){}
		  }                            \s
		}								\s
		""";

	String selection = "var";

	String expectedCompletionNodeToString = "<SelectionOnArgumentName:Object var>";

	String completionIdentifier = "var";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  public void foo() {
		    <SelectionOnArgumentName:Object var>;
		  }
		}
		""";
	String expectedReplacedSource = "var";
	String testName = "<select argument name inside catch statement>";

	int selectionStart = str.indexOf(selection);
	int selectionEnd = str.indexOf(selection) + selection.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * bugs 15430
 */
public void test44() {

	String str =
		"""
		public class X {               \s
		  String x = super.foo() \s
		}								\s
		""";

	String selection = "super";

	String expectedCompletionNodeToString = "<SelectOnSuper:super>";

	String completionIdentifier = "super";
	String expectedUnitDisplayString =
		"""
		public class X {
		  String x = <SelectOnSuper:super>;
		  public X() {
		  }
		}
		""";
	String expectedReplacedSource = "super";
	String testName = "<select super in field initializer>";

	int selectionStart = str.indexOf(selection);
	int selectionEnd = str.indexOf(selection) + selection.length() - 1;

	checkDietParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * bugs 14468
 */
public void test45() {

	String str =
		"""
		public class X {               \s
		  void foo() {
		    if(x instanceof Object s){
		    }
		  } \s
		}								\s
		""";

	String selection = "Object";

	String expectedCompletionNodeToString = "<SelectOnType:Object>";

	String completionIdentifier = "Object";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    if ((x instanceof <SelectOnType:Object> s))
		        {
		        }
		  }
		}
		""";
	String expectedReplacedSource = "Object";
	String testName = "<select inside instanceof statement>";

	int selectionStart = str.indexOf(selection);
	int selectionEnd = str.indexOf(selection) + selection.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}

/*
 * bugs 14468
 */
public void test46() {

	String str =
		"""
		public class X {               \s
		  void foo() {
		    y = x instanceof Object;
		  } \s
		}								\s
		""";

	String selection = "Object";

	String expectedCompletionNodeToString = "<SelectOnType:Object>";

	String completionIdentifier = "Object";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    y = (x instanceof <SelectOnType:Object>);
		  }
		}
		""";
	String expectedReplacedSource = "Object";
	String testName = "<select inside instanceof statement>";

	int selectionStart = str.indexOf(selection);
	int selectionEnd = str.indexOf(selection) + selection.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * bugs 14468
 */
public void test47() {

	String str =
		"""
		public class X {               \s
		  void foo() {
		   boolean y = x instanceof Object;
		  } \s
		}								\s
		""";

	String selection = "Object";

	String expectedCompletionNodeToString = "<SelectOnType:Object>";

	String completionIdentifier = "Object";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    boolean y = (x instanceof <SelectOnType:Object>);
		  }
		}
		""";
	String expectedReplacedSource = "Object";
	String testName = "<select inside instanceof statement>";

	int selectionStart = str.indexOf(selection);
	int selectionEnd = str.indexOf(selection) + selection.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * bugs 14468
 */
public void test48() {

	String str =
		"""
		public class X {               \s
		  boolean y = x instanceof Object;
		}								\s
		""";

	String selection = "Object";

	String expectedCompletionNodeToString = "<SelectOnType:Object>";

	String completionIdentifier = "Object";
	String expectedUnitDisplayString =
		"""
		public class X {
		  boolean y = (x instanceof <SelectOnType:Object>);
		  public X() {
		  }
		}
		""";
	String expectedReplacedSource = "Object";
	String testName = "<select inside instanceof statement>";

	int selectionStart = str.indexOf(selection);
	int selectionEnd = str.indexOf(selection) + selection.length() - 1;

	checkDietParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * bugs 28064
 */
public void test49() {

	String str =
		"""
		public class X {               \s
		  X x = new X(){}
		}								\s
		""";

	String selection = "X";

	String expectedCompletionNodeToString = "<SelectOnAllocationExpression:new X() {\n" +
											"}>";

	String completionIdentifier = "X";
	String expectedUnitDisplayString =
		"""
		public class X {
		  X x = <SelectOnAllocationExpression:new X() {
		  }>;
		  public X() {
		  }
		}
		""";
	String expectedReplacedSource = "new X()";
	String testName = "<select anonymous type>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;

	checkDietParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * bugs https://bugs.eclipse.org/bugs/show_bug.cgi?id=52422
 */
public void test50() {

	String str =
		"""
		public class X {               \s
		  void foo() {
		    new Object(){
		      void bar(){
		        bar2();
		      }
		      void bar2() {
		      }
		    }
		  } \s
		}								\s
		""";

	String selection = "bar2";

	String expectedCompletionNodeToString = "<SelectOnMessageSend:bar2()>";

	String completionIdentifier = "bar2";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    new Object() {
		      void bar() {
		        <SelectOnMessageSend:bar2()>;
		      }
		      void bar2() {
		      }
		    };
		  }
		}
		""";
	String expectedReplacedSource = "bar2()";
	String testName = "<select inside anonymous type>";

	int selectionStart = str.indexOf(selection);
	int selectionEnd = str.indexOf(selection) + selection.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * bugs https://bugs.eclipse.org/bugs/show_bug.cgi?id=52422
 */
public void test51() {

	String str =
		"""
		public class X {               \s
		  void foo() {
		    new Object(){
		      void foo0(){
		        new Object(){
		          void bar(){
		            bar2();
		          }
		          void bar2() {
		          }
		        }
		      }
		    }
		  }
		}								\s
		""";

	String selection = "bar2";

	String expectedCompletionNodeToString = "<SelectOnMessageSend:bar2()>";

	String completionIdentifier = "bar2";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    new Object() {
		      void foo0() {
		        new Object() {
		          void bar() {
		            <SelectOnMessageSend:bar2()>;
		          }
		          void bar2() {
		          }
		        };
		      }
		    };
		  }
		}
		""";
	String expectedReplacedSource = "bar2()";
	String testName = "<select inside anonymous type>";

	int selectionStart = str.indexOf(selection);
	int selectionEnd = str.indexOf(selection) + selection.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * bugs https://bugs.eclipse.org/bugs/show_bug.cgi?id=52422
 */
public void test52() {

	String str =
		"""
		public class X {               \s
		  void foo() {
		    new Object(){
		      void foo0(){
		        new Object(){
		          void bar(){
		            bar2();
		          }
		        }
		      }
		      void bar2() {
		      }
		    }
		  }
		}								\s
		""";

	String selection = "bar2";

	String expectedCompletionNodeToString = "<SelectOnMessageSend:bar2()>";

	String completionIdentifier = "bar2";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    new Object() {
		      void foo0() {
		        new Object() {
		          void bar() {
		            <SelectOnMessageSend:bar2()>;
		          }
		        };
		      }
		      void bar2() {
		      }
		    };
		  }
		}
		""";
	String expectedReplacedSource = "bar2()";
	String testName = "<select inside anonymous type>";

	int selectionStart = str.indexOf(selection);
	int selectionEnd = str.indexOf(selection) + selection.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void test53() {

	String str =
		"""
		public class X {               \s
		  void foo(String[] stringArray) {
		    for(String string2 : stringArray);
		  }
		}								\s
		""";

	String selection = "string2";

	String expectedCompletionNodeToString = "<SelectionOnLocalName:String string2>;";

	String completionIdentifier = "string2";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo(String[] stringArray) {
		    for (<SelectionOnLocalName:String string2> : stringArray)\s
		      ;
		  }
		}
		""";
	String expectedReplacedSource = "string2";
	String testName = "<select>";

	int selectionStart = str.indexOf(selection);
	int selectionEnd = str.indexOf(selection) + selection.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84001
public void test54() {

	String str =
		"""
		public class X {               \s
		  void foo() {
		    new Test.Sub();
		  }
		}								\s
		""";

	String selection = "Test";

	String expectedCompletionNodeToString = "<SelectOnType:Test>";

	String completionIdentifier = "Test";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    new <SelectOnType:Test>();
		  }
		}
		""";
	String expectedReplacedSource = "Test";
	String testName = "<select>";

	int selectionStart = str.indexOf(selection);
	int selectionEnd = str.indexOf(selection) + selection.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84001
public void test55() {

	String str =
		"""
		public class X {               \s
		  void foo() {
		    new Test.Sub();
		  }
		}								\s
		""";

	String selection = "Sub";

	String expectedCompletionNodeToString = "<SelectOnAllocationExpression:new Test.Sub()>";

	String completionIdentifier = "Sub";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <SelectOnAllocationExpression:new Test.Sub()>;
		  }
		}
		""";
	String expectedReplacedSource = "new Test.Sub()";
	String testName = "<select>";

	int selectionStart = str.indexOf(selection);
	int selectionEnd = str.indexOf(selection) + selection.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=291040
public void _test56() {

	String str =
			"""
		class X {
		    void foo() {
		        new X(null) {
		            void goo() {
		                new X(zoo()) {
		                    void voo() {
		                    }
		                };
		            }
		
		            Object zoo() {
		                return null;
		            }
		        };
		    }
		
		    X(Object k) {
		    }
		}
		""";

	String selection = "zoo";

	String expectedCompletionNodeToString = "<SelectOnMessageSend:zoo()>";

	String completionIdentifier = "zoo";
	String expectedUnitDisplayString =
			"""
		class X {
		  void foo() {
		    new X(null) {
		      void goo() {
		        new X(<SelectOnMessageSend:zoo()>) {
		          void voo() {
		          }
		        };
		      }
		      Object zoo() {
		      }
		    };
		  }
		  X(Object k) {
		  }
		}
		""";
	String expectedReplacedSource = "zoo()";
	String testName = "<select>";

	int selectionStart = str.indexOf(selection);
	int selectionEnd = str.indexOf(selection) + selection.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Test that an {@code instanceof} expression for an object field
 * doesn't result in a bad selection when nested in a method invocation.
 * See: https://github.com/eclipse-jdt/eclipse.jdt.core/issues/848
 */
public void testInstanceOfFieldGh848() {
	String str =
		"""
		public class X {                    \s
		    public Object o = new Object();
		    public static void bar() {
		        Test x = new Test();
		        foo(x.o instanceof Object);
		    }
		    private static void foo(boolean b) {
		    }\
		}								    \s
		""";

	String selection = "foo";

	String expectedCompletionNodeToString = "<SelectOnMessageSend:foo((x.o instanceof Object))>";

	String completionIdentifier = "foo";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public Object o;
		  public X() {
		  }
		  public static void bar() {
		    Test x;
		    <SelectOnMessageSend:foo((x.o instanceof Object))>;
		  }
		  private static void foo(boolean b) {
		  }
		}
		""";
	String expectedReplacedSource = "foo(x.o instanceof Object)";
	String testName = "<select inside instanceof statement>";

	int selectionStart = str.indexOf(selection);
	int selectionEnd = str.indexOf(selection) + selection.length() - 1;

	this.checkMethodParse(
		str.toCharArray(),
		selectionStart,
		selectionEnd,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
}
