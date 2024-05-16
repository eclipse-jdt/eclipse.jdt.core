/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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

public class GenericsSelectionTest extends AbstractSelectionTest {
public GenericsSelectionTest(String testName) {
	super(testName);
}
/*
 * Selection at specific location
 */
public void test0001() {

	String str =
		"""
		public class X {	\t
		  Z<Object> z;							\t
		}										\t
		""";

	String selection = "Z";

	String expectedCompletionNodeToString = "<SelectOnType:Z<Object>>";

	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <SelectOnType:Z<Object>> z;
		  public X() {
		  }
		}
		""";
	String expectedReplacedSource = "Z";
	String testName = "<select type>";

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
 * Selection at specific location
 */
public void test0002() {

	String str =
		"""
		public class X {	\t
		  void foo(){;							\t
		    Z<Object> z;							\t
		  }           							\t
		}										\t
		""";

	String selection = "Z";

	String expectedCompletionNodeToString = "<SelectOnType:Z<Object>>";

	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <SelectOnType:Z<Object>> z;
		  }
		}
		""";
	String expectedReplacedSource = "Z";
	String testName = "<select type>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;

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
 * Selection at specific location
 */
public void test0003() {

	String str =
		"""
		public class X {	\t
		  Y.Z<Object> z;							\t
		}										\t
		""";

	String selection = "Z";

	String expectedCompletionNodeToString = "<SelectOnType:Y.Z<Object>>";

	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <SelectOnType:Y.Z<Object>> z;
		  public X() {
		  }
		}
		""";
	String expectedReplacedSource = "Y.Z";
	String testName = "<select type>";

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
 * Selection at specific location
 */
public void test0004() {

	String str =
		"""
		public class X {	\t
		  void foo(){;							\t
		    Y.Z<Object> z;							\t
		  }           							\t
		}										\t
		""";

	String selection = "Z";

	String expectedCompletionNodeToString = "<SelectOnType:Y.Z<Object>>";

	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <SelectOnType:Y.Z<Object>> z;
		  }
		}
		""";
	String expectedReplacedSource = "Y.Z";
	String testName = "<select type>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;

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
 * Selection at specific location
 */
public void test0005() {

	String str =
		"""
		public class X {	\t
		  Y<Object>.Z z;							\t
		}										\t
		""";

	String selection = "Z";

	String expectedCompletionNodeToString = "<SelectOnType:Y<Object>.Z>";

	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <SelectOnType:Y<Object>.Z> z;
		  public X() {
		  }
		}
		""";
	String expectedReplacedSource = "Y<Object>.Z";
	String testName = "<select type>";

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
 * Selection at specific location
 */
public void test0006() {

	String str =
		"""
		public class X {	\t
		  void foo(){;							\t
		    Y<Object>.Z z;							\t
		  }           							\t
		}										\t
		""";

	String selection = "Z";

	String expectedCompletionNodeToString = "<SelectOnType:Y<Object>.Z>";

	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <SelectOnType:Y<Object>.Z> z;
		  }
		}
		""";
	String expectedReplacedSource = "Y<Object>.Z";
	String testName = "<select type>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;

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
 * Selection at specific location
 */
public void test0007() {

	String str =
		"""
		public class X {	\t
		  Y<Object>.Z<Object> z;							\t
		}										\t
		""";

	String selection = "Z";

	String expectedCompletionNodeToString = "<SelectOnType:Y<Object>.Z<Object>>";

	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <SelectOnType:Y<Object>.Z<Object>> z;
		  public X() {
		  }
		}
		""";
	String expectedReplacedSource = "Y<Object>.Z";
	String testName = "<select type>";

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
 * Selection at specific location
 */
public void test0008() {

	String str =
		"""
		public class X {	\t
		  void foo(){;							\t
		    Y<Object>.Z<Object> z;							\t
		  }           							\t
		}										\t
		""";

	String selection = "Z";

	String expectedCompletionNodeToString = "<SelectOnType:Y<Object>.Z<Object>>";

	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <SelectOnType:Y<Object>.Z<Object>> z;
		  }
		}
		""";
	String expectedReplacedSource = "Y<Object>.Z";
	String testName = "<select type>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;

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
 * Selection of simple name
 */
public void test0009() {

	String str =
		"""
		public class X {	\t
		  Z<Object> z;							\t
		}										\t
		""";

	String selection = "Z";

	String expectedCompletionNodeToString = "<SelectOnType:Z<Object>>";

	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <SelectOnType:Z<Object>> z;
		  public X() {
		  }
		}
		""";
	String expectedReplacedSource = "Z";
	String testName = "<select type>";

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
 * Selection of simple name
 */
public void test0010() {

	String str =
		"""
		public class X {	\t
		  void foo(){;							\t
		    Z<Object> z;							\t
		  }           							\t
		}										\t
		""";

	String selection = "Z";

	String expectedCompletionNodeToString = "<SelectOnType:Z<Object>>";

	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <SelectOnType:Z<Object>> z;
		  }
		}
		""";
	String expectedReplacedSource = "Z";
	String testName = "<select type>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;

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
 * Selection of qualified name
 */
public void test0011() {

	String str =
		"""
		public class X {	\t
		  Y.Z<Object> z;							\t
		}										\t
		""";

	String selection = "Z";

	String expectedCompletionNodeToString = "<SelectOnType:Y.Z<Object>>";

	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <SelectOnType:Y.Z<Object>> z;
		  public X() {
		  }
		}
		""";
	String expectedReplacedSource = "Y.Z";
	String testName = "<select type>";

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
 * Selection of qualified name
 */
public void test0012() {

	String str =
		"""
		public class X {	\t
		  void foo(){;							\t
		    Y.Z<Object> z;							\t
		  }           							\t
		}										\t
		""";

	String selection = "Z";

	String expectedCompletionNodeToString = "<SelectOnType:Y.Z<Object>>";

	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <SelectOnType:Y.Z<Object>> z;
		  }
		}
		""";
	String expectedReplacedSource = "Y.Z";
	String testName = "<select type>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;

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
 * Selection of qualified name
 */
public void test0013() {

	String str =
		"""
		public class X {	\t
		  Y<Object>.Z z;							\t
		}										\t
		""";

	String selection = "Z";

	String expectedCompletionNodeToString = "<SelectOnType:Y<Object>.Z>";

	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <SelectOnType:Y<Object>.Z> z;
		  public X() {
		  }
		}
		""";
	String expectedReplacedSource = "Y<Object>.Z";
	String testName = "<select type>";

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
 * Selection of qualified name
 */
public void test0014() {

	String str =
		"""
		public class X {	\t
		  void foo(){;							\t
		    Y<Object>.Z z;							\t
		  }           							\t
		}										\t
		""";

	String selection = "Z";

	String expectedCompletionNodeToString = "<SelectOnType:Y<Object>.Z>";

	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <SelectOnType:Y<Object>.Z> z;
		  }
		}
		""";
	String expectedReplacedSource = "Y<Object>.Z";
	String testName = "<select type>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;

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
 * Selection of qualified name
 */
public void test0015() {

	String str =
		"""
		public class X {	\t
		  Y<Object>.Z<Object> z;							\t
		}										\t
		""";

	String selection = "Z";

	String expectedCompletionNodeToString = "<SelectOnType:Y<Object>.Z<Object>>";

	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <SelectOnType:Y<Object>.Z<Object>> z;
		  public X() {
		  }
		}
		""";
	String expectedReplacedSource = "Y<Object>.Z";
	String testName = "<select type>";

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
 * Selection of qualified name
 */
public void test0016() {

	String str =
		"""
		public class X {	\t
		  void foo(){;							\t
		    Y<Object>.Z<Object> z;							\t
		  }           							\t
		}										\t
		""";

	String selection = "Z";

	String expectedCompletionNodeToString = "<SelectOnType:Y<Object>.Z<Object>>";

	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <SelectOnType:Y<Object>.Z<Object>> z;
		  }
		}
		""";
	String expectedReplacedSource = "Y<Object>.Z";
	String testName = "<select type>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;

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
public void test0017() {

	String str =
		"""
		public class X {	\t
		  public <T>X() {							\t
		  }           							\t
		  void foo(){;							\t
		    new <Object>X();							\t
		  }           							\t
		}										\t
		""";

	String selection = "X";

	String expectedCompletionNodeToString = "<SelectOnAllocationExpression:new <Object>X()>";

	String completionIdentifier = "X";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public <T>X() {
		  }
		  void foo() {
		    <SelectOnAllocationExpression:new <Object>X()>;
		  }
		}
		""";
	String expectedReplacedSource = "new <Object>X()";
	String testName = "<select type>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;

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
public void test0018() {

	String str =
		"""
		public class X <U>{	\t
		  public <T>X() {							\t
		  }           							\t
		  void foo(){;							\t
		    new <Object>X<String>();							\t
		  }           							\t
		}										\t
		""";

	String selection = "X";

	String expectedCompletionNodeToString = "<SelectOnAllocationExpression:new <Object>X<String>()>";

	String completionIdentifier = "X";
	String expectedUnitDisplayString =
		"""
		public class X<U> {
		  public <T>X() {
		  }
		  void foo() {
		    <SelectOnAllocationExpression:new <Object>X<String>()>;
		  }
		}
		""";
	String expectedReplacedSource = "new <Object>X<String>()";
	String testName = "<select type>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;

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
public void test0019() {

	String str =
		"""
		public class X {	\t
		  public class Inner {							\t
		    public <U> Inner() {  							\t
		    }           							\t
		  }           							\t
		  void foo(X x){;							\t
		    x.new <Object>Inner();							\t
		  }           							\t
		}										\t
		""";

	String selection = "Inner";

	String expectedCompletionNodeToString = "<SelectOnQualifiedAllocationExpression:x.new <Object>Inner()>";

	String completionIdentifier = "Inner";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public class Inner {
		    public <U>Inner() {
		    }
		  }
		  public X() {
		  }
		  void foo(X x) {
		    <SelectOnQualifiedAllocationExpression:x.new <Object>Inner()>;
		  }
		}
		""";
	String expectedReplacedSource = "x.new <Object>Inner()";
	String testName = "<select type>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;

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
public void test0020() {

	String str =
		"""
		public class X {	\t
		  public class Inner<T> {							\t
		    public <U> Inner() {  							\t
		    }           							\t
		  }           							\t
		  void foo(X x){;							\t
		    x.new <Object>Inner<String>();							\t
		  }           							\t
		}										\t
		""";

	String selection = "Inner";

	String expectedCompletionNodeToString = "<SelectOnQualifiedAllocationExpression:x.new <Object>Inner<String>()>";

	String completionIdentifier = "Inner";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public class Inner<T> {
		    public <U>Inner() {
		    }
		  }
		  public X() {
		  }
		  void foo(X x) {
		    <SelectOnQualifiedAllocationExpression:x.new <Object>Inner<String>()>;
		  }
		}
		""";
	String expectedReplacedSource = "x.new <Object>Inner<String>()";
	String testName = "<select type>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;

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
public void test0021() {

	String str =
		"""
		public class X<V> {	\t
		  public class Inner<T> {							\t
		    public <U> Inner() {  							\t
		    }           							\t
		  }           							\t
		  void foo(){;							\t
		    new X<String>().new <Object>Inner<String>();							\t
		  }           							\t
		}										\t
		""";

	String selection = "Inner";

	String expectedCompletionNodeToString = "<SelectOnQualifiedAllocationExpression:new X<String>().new <Object>Inner<String>()>";

	String completionIdentifier = "Inner";
	String expectedUnitDisplayString =
		"""
		public class X<V> {
		  public class Inner<T> {
		    public <U>Inner() {
		    }
		  }
		  public X() {
		  }
		  void foo() {
		    <SelectOnQualifiedAllocationExpression:new X<String>().new <Object>Inner<String>()>;
		  }
		}
		""";
	String expectedReplacedSource = "new X<String>().new <Object>Inner<String>()";
	String testName = "<select type>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;

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
 * Selection of simple name
 */
public void test0022() {

	String str =
		"""
		public class X {	\t
		  Y.Z z;							\t
		}										\t
		""";

	String selection = "Z";

	String expectedCompletionNodeToString = "<SelectOnType:Y.Z>";

	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <SelectOnType:Y.Z> z;
		  public X() {
		  }
		}
		""";
	String expectedReplacedSource = "Y.Z";
	String testName = "<select type>";

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=209639
public void test0023() {

	String str =
		"""
		package test;
		public class Test  {
			public List<String> foo() {
				return Collections.emptyList();
			}
		}""";

	String selection = "emptyList";

	String expectedCompletionNodeToString = "<SelectOnMessageSend:Collections.emptyList()>";

	String completionIdentifier = "emptyList";
	String expectedUnitDisplayString =
		"""
		package test;
		public class Test {
		  public Test() {
		  }
		  public List<String> foo() {
		    return <SelectOnMessageSend:Collections.emptyList()>;
		  }
		}
		""";
	String expectedReplacedSource = "Collections.emptyList()";
	String testName = "<select method>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=255142
public void test0024() {

	String str =
		"""
		import java.util.List;
		public class X {
		        <T> T bar(T t) { return t; }
		        void foo(boolean b, Runnable r) {
		                Zork z = null;
		                String s = (String) bar(z); // 5
		        }
		}
		
		""";

	String selection = "bar";

	String expectedCompletionNodeToString = "<SelectOnMessageSend:bar(z)>";

	String completionIdentifier = "bar";
	String expectedUnitDisplayString =
		"""
		import java.util.List;
		public class X {
		  public X() {
		  }
		  <T>T bar(T t) {
		  }
		  void foo(boolean b, Runnable r) {
		    Zork z;
		    String s = (String) <SelectOnMessageSend:bar(z)>;
		  }
		}
		""";
	String expectedReplacedSource = "bar(z)";
	String testName = "<select method>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;

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
