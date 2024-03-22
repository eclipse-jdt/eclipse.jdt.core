/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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

public class CompletionParserTest2 extends AbstractCompletionTest {
public CompletionParserTest2(String testName) {
	super(testName);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(CompletionParserTest2.class);
}
public void test0001(){
	String str =
		"""
		package p;
		public class X {
		  Object o = zzz;
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "Object o = <CompleteOnName:zzz>;";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  Object o = <CompleteOnName:zzz>;
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0002_Diet(){
	String str =
		"""
		package p;
		public class X {
		  {
		    Object o = zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  {
		  }
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0002_Method(){
	String str =
		"""
		package p;
		public class X {
		  {
		    Object o = zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "Object o = <CompleteOnName:zzz>;";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  {
		    Object o = <CompleteOnName:zzz>;
		  }
		  public X() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0003_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    Object o = zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0003_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    Object o = zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "Object o = <CompleteOnName:zzz>;";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    Object o = <CompleteOnName:zzz>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0004(){
	String str =
		"""
		package p;
		public class X {
		  #
		  Object o = zzz;
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "Object o = <CompleteOnName:zzz>;";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  Object o = <CompleteOnName:zzz>;
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0005_Diet(){
	String str =
		"""
		package p;
		public class X {
		  {
		    #
		    Object o = zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  {
		  }
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0005_Method(){
	String str =
		"""
		package p;
		public class X {
		  {
		    #
		    Object o = zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "Object o = <CompleteOnName:zzz>;";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  {
		    {
		      Object o = <CompleteOnName:zzz>;
		    }
		  }
		  public X() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0006_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    Object o = zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0006_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    Object o = zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "Object o = <CompleteOnName:zzz>;";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    Object o = <CompleteOnName:zzz>;
		  }
		}
		""";
	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0007(){
	String str =
		"""
		package p;
		public class X {
		  Object o = new zzz;
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:zzz>";
	String expectedParentNodeToString = "Object o = new <CompleteOnType:zzz>();";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  Object o = new <CompleteOnType:zzz>();
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0008_Diet(){
	String str =
		"""
		package p;
		public class X {
		  {
		    Object o = new zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  {
		  }
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0008_Method(){
	String str =
		"""
		package p;
		public class X {
		  {
		    Object o = new zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:zzz>";
	String expectedParentNodeToString = "Object o = new <CompleteOnType:zzz>();";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  {
		    Object o = new <CompleteOnType:zzz>();
		  }
		  public X() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0009_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    Object o = new zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0009_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    Object o = new zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:zzz>";
	String expectedParentNodeToString = "Object o = new <CompleteOnType:zzz>();";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    Object o = new <CompleteOnType:zzz>();
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0010(){
	String str =
		"""
		package p;
		public class X {
		  #
		  Object o = new zzz;
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:zzz>";
	String expectedParentNodeToString = "Object o = new <CompleteOnType:zzz>();";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  Object o = new <CompleteOnType:zzz>();
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0011_Diet(){
	String str =
		"""
		package p;
		public class X {
		  {
		    #
		    Object o = new zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  {
		  }
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0011_Method(){
	String str =
		"""
		package p;
		public class X {
		  {
		    #
		    Object o = new zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:zzz>";
	String expectedParentNodeToString = "Object o = new <CompleteOnType:zzz>();";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  {
		    {
		      Object o = new <CompleteOnType:zzz>();
		    }
		  }
		  public X() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0012_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    Object o = new zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0012_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    Object o = new zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:zzz>";
	String expectedParentNodeToString = "Object o = new <CompleteOnType:zzz>();";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    Object o = new <CompleteOnType:zzz>();
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0013(){
	String str =
		"""
		package p;
		public class X {
		  Object o = yyy;
		  zzz
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  Object o;
		  <CompleteOnType:zzz>;
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}

public void test0014_Diet(){
	String str =
		"""
		package p;
		public class X {
		  {
		    Object o = yyy;
		    zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  {
		  }
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0014_Method(){
	String str =
		"""
		package p;
		public class X {
		  {
		    Object o = yyy;
		    zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  {
		    Object o;
		    <CompleteOnName:zzz>;
		  }
		  public X() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0015_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    Object o = yyy;
		    zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0015_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    Object o = yyy;
		    zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    Object o;
		    <CompleteOnName:zzz>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0016(){
	String str =
		"""
		package p;
		public class X {
		  #
		  Object o = yyy;
		  zzz
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  Object o;
		  <CompleteOnType:zzz>;
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}

public void test0017_Diet(){
	String str =
		"""
		package p;
		public class X {
		  {
		    #
		    Object o = yyy;
		    zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  {
		  }
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0017_Method(){
	String str =
		"""
		package p;
		public class X {
		  {
		    #
		    Object o = yyy;
		    zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  {
		    {
		      Object o;
		      <CompleteOnName:zzz>;
		    }
		  }
		  public X() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0018_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    Object o = yyy;
		    zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0018_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    Object o = yyy;
		    zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    Object o;
		    <CompleteOnName:zzz>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0019(){
	String str =
		"""
		package p;
		public class X {
		  Object o = bar(zzz;
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "bar(<CompleteOnName:zzz>)";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  Object o = bar(<CompleteOnName:zzz>);
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}

public void test0020_Diet(){
	String str =
		"""
		package p;
		public class X {
		  {
		    Object o = bar(zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  {
		  }
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0020_Method(){
	String str =
		"""
		package p;
		public class X {
		  {
		    Object o = bar(zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "bar(<CompleteOnName:zzz>)";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  {
		    Object o = bar(<CompleteOnName:zzz>);
		  }
		  public X() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0021_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    Object o = bar(zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0021_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    Object o = bar(zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "bar(<CompleteOnName:zzz>)";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    Object o = bar(<CompleteOnName:zzz>);
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0022(){
	String str =
		"""
		package p;
		public class X {
		  #
		  Object o = bar(zzz;
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "bar(<CompleteOnName:zzz>)";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  Object o = bar(<CompleteOnName:zzz>);
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}

public void test0023_Diet(){
	String str =
		"""
		package p;
		public class X {
		  {
		    #
		    Object o = bar(zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  {
		  }
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0023_Method(){
	String str =
		"""
		package p;
		public class X {
		  {
		    #
		    Object o = bar(zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "bar(<CompleteOnName:zzz>)";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  {
		    {
		      Object o = bar(<CompleteOnName:zzz>);
		    }
		  }
		  public X() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0024_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    Object o = bar(zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0024_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    Object o = bar(zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "bar(<CompleteOnName:zzz>)";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    Object o = bar(<CompleteOnName:zzz>);
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0025(){
	String str =
		"""
		package p;
		public class X {
		  Object o = new X(zzz;
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X(<CompleteOnName:zzz>)";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  Object o = new X(<CompleteOnName:zzz>);
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}


public void test0026_Diet(){
	String str =
		"""
		package p;
		public class X {
		  {
		    Object o = new X(zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  {
		  }
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0026_Method(){
	String str =
		"""
		package p;
		public class X {
		  {
		    Object o = new X(zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X(<CompleteOnName:zzz>)";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  {
		    Object o = new X(<CompleteOnName:zzz>);
		  }
		  public X() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0027_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    Object o = new X(zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0027_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    Object o = new X(zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X(<CompleteOnName:zzz>)";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    Object o = new X(<CompleteOnName:zzz>);
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0028(){
	String str =
		"""
		package p;
		public class X {
		  #
		  Object o = new X(zzz;
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X(<CompleteOnName:zzz>)";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  Object o = new X(<CompleteOnName:zzz>);
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}


public void test0029_Diet(){
	String str =
		"""
		package p;
		public class X {
		  {
		    #
		    Object o = new X(zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  {
		  }
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0029_Method(){
	String str =
		"""
		package p;
		public class X {
		  {
		    #
		    Object o = new X(zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X(<CompleteOnName:zzz>)";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  {
		    {
		      Object o = new X(<CompleteOnName:zzz>);
		    }
		  }
		  public X() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0030_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    Object o = new X(zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0030_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    Object o = new X(zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X(<CompleteOnName:zzz>)";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    Object o = new X(<CompleteOnName:zzz>);
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0031_Diet(){
	String str =
		"""
		package p;
		public class X {
		  Object o = {zzz;
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  Object o;
		  {
		    <CompleteOnName:zzz>;
		  }
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0031_Method(){
	String str =
		"""
		package p;
		public class X {
		  Object o = {zzz;
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  Object o;
		  {
		    <CompleteOnName:zzz>;
		  }
		  public X() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0032_Diet(){
	String str =
		"""
		package p;
		public class X {
		  {
		    Object o = {zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  {
		  }
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0032_Method(){
	String str =
		"""
		package p;
		public class X {
		  {
		    Object o = {zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  {
		    Object o;
		    {
		      <CompleteOnName:zzz>;
		    }
		  }
		  public X() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0033_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    Object o = {zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    {
		      <CompleteOnName:zzz>;
		    }
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0033_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    Object o = {zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    Object o;
		    {
		      <CompleteOnName:zzz>;
		    }
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0034_Diet(){
	String str =
		"""
		package p;
		public class X {
		  #
		  Object o = {zzz;
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  Object o;
		  {
		    <CompleteOnName:zzz>;
		  }
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0034_Method(){
	String str =
		"""
		package p;
		public class X {
		  #
		  Object o = {zzz;
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  Object o;
		  {
		    <CompleteOnName:zzz>;
		  }
		  public X() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0035_Diet(){
	String str =
		"""
		package p;
		public class X {
		  {
		    #
		    Object o = {zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  {
		  }
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0035_Method(){
	String str =
		"""
		package p;
		public class X {
		  {
		    #
		    Object o = {zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  {
		    {
		      Object o;
		      {
		        <CompleteOnName:zzz>;
		      }
		    }
		  }
		  public X() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0036_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    Object o = {zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    {
		      <CompleteOnName:zzz>;
		    }
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0036_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    Object o = {zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    Object o;
		    {
		      <CompleteOnName:zzz>;
		    }
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0037(){
	String str =
		"""
		package p;
		public class X {
		  Object[] o = {zzz;
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "Object[] o = {<CompleteOnName:zzz>};";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  Object[] o = {<CompleteOnName:zzz>};
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}


public void test0038_Diet(){
	String str =
		"""
		package p;
		public class X {
		  {
		    Object[] o = {zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  {
		  }
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0038_Method(){
	String str =
		"""
		package p;
		public class X {
		  {
		    Object[] o = {zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "Object[] o = {<CompleteOnName:zzz>};";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  {
		    Object[] o = {<CompleteOnName:zzz>};
		  }
		  public X() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0039_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    Object[] o = {zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    {
		      <CompleteOnName:zzz>;
		    }
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0039_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    Object[] o = {zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "Object[] o = {<CompleteOnName:zzz>};";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    Object[] o = {<CompleteOnName:zzz>};
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0040(){
	String str =
		"""
		package p;
		public class X {
		  #
		  Object[] o = {zzz;
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "Object[] o = {<CompleteOnName:zzz>};";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  Object[] o = {<CompleteOnName:zzz>};
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}


public void test0041_Diet(){
	String str =
		"""
		package p;
		public class X {
		  {
		    #
		    Object[] o = {zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  {
		  }
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0041_Method(){
	String str =
		"""
		package p;
		public class X {
		  {
		    #
		    Object[] o = {zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "Object[] o = {<CompleteOnName:zzz>};";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  {
		    {
		      Object[] o = {<CompleteOnName:zzz>};
		    }
		  }
		  public X() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0042_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    Object[] o = {zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    {
		      <CompleteOnName:zzz>;
		    }
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0042_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    Object[] o = {zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "Object[] o = {<CompleteOnName:zzz>};";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    Object[] o = {<CompleteOnName:zzz>};
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0043(){
	String str =
		"""
		package p;
		public class X {
		  Object[] o = new X[zzz;
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  Object[] o = new X[<CompleteOnName:zzz>];
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}



public void test0044_Diet(){
	String str =
		"""
		package p;
		public class X {
		  {
		    Object[] o = new X[zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  {
		  }
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0044_Method(){
	String str =
		"""
		package p;
		public class X {
		  {
		    Object[] o = new X[zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  {
		    Object[] o = new X[<CompleteOnName:zzz>];
		  }
		  public X() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}



public void test0045_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    Object[] o = new X[zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0045_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    Object[] o = new X[zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    Object[] o = new X[<CompleteOnName:zzz>];
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}



public void test0046(){
	String str =
		"""
		package p;
		public class X {
		  #
		  Object[] o = new X[zzz;
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  Object[] o = new X[<CompleteOnName:zzz>];
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}



public void test0047_Diet(){
	String str =
		"""
		package p;
		public class X {
		  {
		    #
		    Object[] o = new X[zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  {
		  }
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0047_Method(){
	String str =
		"""
		package p;
		public class X {
		  {
		    #
		    Object[] o = new X[zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  {
		    {
		      Object[] o = new X[<CompleteOnName:zzz>];
		    }
		  }
		  public X() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}



public void test0048_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    Object[] o = new X[zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0048_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    Object[] o = new X[zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    Object[] o = new X[<CompleteOnName:zzz>];
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0049(){
	String str =
		"""
		package p;
		public class X {
		  Object[] o = new X[]{zzz;
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  Object[] o = new X[]{<CompleteOnName:zzz>};
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0050_Diet(){
	String str =
		"""
		package p;
		public class X {
		  {
		    Object[] o = new X[]{zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  {
		  }
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0050_Method(){
	String str =
		"""
		package p;
		public class X {
		  {
		    Object[] o = new X[]{zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  {
		    Object[] o = new X[]{<CompleteOnName:zzz>};
		  }
		  public X() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0051_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    Object[] o = new X[]{zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    {
		      new X[]{<CompleteOnName:zzz>};
		    }
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0051_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    Object[] o = new X[]{zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    Object[] o = new X[]{<CompleteOnName:zzz>};
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0052(){
	String str =
		"""
		package p;
		public class X {
		  #
		  Object[] o = new X[]{zzz;
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  Object[] o = new X[]{<CompleteOnName:zzz>};
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0053_Diet(){
	String str =
		"""
		package p;
		public class X {
		  {
		    #
		    Object[] o = new X[]{zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  {
		  }
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0053_Method(){
	String str =
		"""
		package p;
		public class X {
		  {
		    #
		    Object[] o = new X[]{zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  {
		    {
		      Object[] o = new X[]{<CompleteOnName:zzz>};
		    }
		  }
		  public X() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0054_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    Object[] o = new X[]{zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    {
		      new X[]{<CompleteOnName:zzz>};
		    }
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0054_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    Object[] o = new X[]{zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    Object[] o = new X[]{<CompleteOnName:zzz>};
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0055(){
	String str =
		"""
		package p;
		public class X {
		  Object[] o = zzz;
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "Object[] o = <CompleteOnName:zzz>;";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  Object[] o = <CompleteOnName:zzz>;
		  public X() {
		  }
		}
		""";


	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}

public void test0056_Diet(){
	String str =
		"""
		package p;
		public class X {
		  {
		    Object[] o = zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  {
		  }
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0056_Method(){
	String str =
		"""
		package p;
		public class X {
		  {
		    Object[] o = zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "Object[] o = <CompleteOnName:zzz>;";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  {
		    Object[] o = <CompleteOnName:zzz>;
		  }
		  public X() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0057_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    Object[] o = zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0057_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    Object[] o = zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "Object[] o = <CompleteOnName:zzz>;";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    Object[] o = <CompleteOnName:zzz>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0058(){
	String str =
		"""
		package p;
		public class X {
		  #
		  Object[] o = zzz;
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "Object[] o = <CompleteOnName:zzz>;";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  Object[] o = <CompleteOnName:zzz>;
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}

public void test0059_Diet(){
	String str =
		"""
		package p;
		public class X {
		  {
		    #
		    Object[] o = zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  {
		  }
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0059_Method(){
	String str =
		"""
		package p;
		public class X {
		  {
		    #
		    Object[] o = zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "Object[] o = <CompleteOnName:zzz>;";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  {
		    {
		      Object[] o = <CompleteOnName:zzz>;
		    }
		  }
		  public X() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0060_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    Object[] o = zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0060_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    Object[] o = zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "Object[] o = <CompleteOnName:zzz>;";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    Object[] o = <CompleteOnName:zzz>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0061(){
	String str =
		"""
		package p;
		public class X {
		  Object o = new X[zzz;
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  Object o = new X[<CompleteOnName:zzz>];
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}

public void test0062_Diet(){
	String str =
		"""
		package p;
		public class X {
		  {
		    Object o = new X[zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  {
		  }
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0062_Method(){
	String str =
		"""
		package p;
		public class X {
		  {
		    Object o = new X[zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  {
		    Object o = new X[<CompleteOnName:zzz>];
		  }
		  public X() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0063_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    Object o = new X[zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0063_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    Object o = new X[zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    Object o = new X[<CompleteOnName:zzz>];
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0064(){
	String str =
		"""
		package p;
		public class X {
		  #
		  Object o = new X[zzz;
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  Object o = new X[<CompleteOnName:zzz>];
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}

public void test0065_Diet(){
	String str =
		"""
		package p;
		public class X {
		  {
		    #
		    Object o = new X[zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  {
		  }
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0065_Method(){
	String str =
		"""
		package p;
		public class X {
		  {
		    #
		    Object o = new X[zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  {
		    {
		      Object o = new X[<CompleteOnName:zzz>];
		    }
		  }
		  public X() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0066_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    Object o = new X[zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0066_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    Object o = new X[zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    Object o = new X[<CompleteOnName:zzz>];
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0067_Diet(){
	String str =
		"""
		package p;
		public class X {
		  Object o = new X[]{zzz;
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  Object o = new X[]{<CompleteOnName:zzz>};
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0067_Method(){
	String str =
		"""
		package p;
		public class X {
		  Object o = new X[]{zzz;
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  Object o = new X[]{<CompleteOnName:zzz>};
		  public X() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0068_Diet(){
	String str =
		"""
		package p;
		public class X {
		  {
		    Object o = new X[]{zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  {
		  }
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0068_Method(){
	String str =
		"""
		package p;
		public class X {
		  {
		    Object o = new X[]{zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  {
		    Object o = new X[]{<CompleteOnName:zzz>};
		  }
		  public X() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0069_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    Object o = new X[]{zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    {
		      new X[]{<CompleteOnName:zzz>};
		    }
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0069_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    Object o = new X[]{zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    Object o = new X[]{<CompleteOnName:zzz>};
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0070_Diet(){
	String str =
		"""
		package p;
		public class X {
		  #
		  Object o = new X[]{zzz;
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  Object o = new X[]{<CompleteOnName:zzz>};
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0070_Method(){
	String str =
		"""
		package p;
		public class X {
		  #
		  Object o = new X[]{zzz;
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  Object o = new X[]{<CompleteOnName:zzz>};
		  public X() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0071_Diet(){
	String str =
		"""
		package p;
		public class X {
		  {
		    #
		    Object o = new X[]{zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  {
		  }
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0071_Method(){
	String str =
		"""
		package p;
		public class X {
		  {
		    #
		    Object o = new X[]{zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  {
		    {
		      Object o;
		      {
		        <CompleteOnName:zzz>;
		      }
		    }
		  }
		  public X() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0072_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    Object o = new X[]{zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    {
		      new X[]{<CompleteOnName:zzz>};
		    }
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0072_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    Object o = new X[]{zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    Object o;
		    {
		      <CompleteOnName:zzz>;
		    }
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0073(){
	String str =
		"""
		package p;
		public class X {
		  int o = new int[zzz;
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new int[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  int o = new int[<CompleteOnName:zzz>];
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}


public void test0074_Diet(){
	String str =
		"""
		package p;
		public class X {
		  {
		    int o = new int[zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  {
		  }
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0074_Method(){
	String str =
		"""
		package p;
		public class X {
		  {
		    int o = new int[zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new int[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  {
		    int o = new int[<CompleteOnName:zzz>];
		  }
		  public X() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0075_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    int o = new int[zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0075_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    int o = new int[zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new int[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    int o = new int[<CompleteOnName:zzz>];
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0076(){
	String str =
		"""
		package p;
		public class X {
		  #
		  int o = new int[zzz;
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new int[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  int o = new int[<CompleteOnName:zzz>];
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}


public void test0077_Diet(){
	String str =
		"""
		package p;
		public class X {
		  {
		    #
		    int o = new int[zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  {
		  }
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0077_Method(){
	String str =
		"""
		package p;
		public class X {
		  {
		    #
		    int o = new int[zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new int[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  {
		    {
		      int o = new int[<CompleteOnName:zzz>];
		    }
		  }
		  public X() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0078_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    int o = new int[zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0078_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    int o = new int[zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new int[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    int o = new int[<CompleteOnName:zzz>];
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0079_Diet(){
	String str =
		"""
		package p;
		public class X {
		  int o = new int[]{zzz;
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new int[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  int o = new int[]{<CompleteOnName:zzz>};
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0079_Method(){
	String str =
		"""
		package p;
		public class X {
		  int o = new int[]{zzz;
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new int[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  int o = new int[]{<CompleteOnName:zzz>};
		  public X() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0080_Diet(){
	String str =
		"""
		package p;
		public class X {
		  {
		    int o = new int[]{zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  {
		  }
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0080_Method(){
	String str =
		"""
		package p;
		public class X {
		  {
		    int o = new int[]{zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new int[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  {
		    int o = new int[]{<CompleteOnName:zzz>};
		  }
		  public X() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0081_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    int o = new int[]{zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new int[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    {
		      new int[]{<CompleteOnName:zzz>};
		    }
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0081_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    int o = new int[]{zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new int[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    int o = new int[]{<CompleteOnName:zzz>};
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0082_Diet(){
	String str =
		"""
		package p;
		public class X {
		  #
		  int o = new int[]{zzz;
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new int[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  int o = new int[]{<CompleteOnName:zzz>};
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0082_Method(){
	String str =
		"""
		package p;
		public class X {
		  #
		  int o = new int[]{zzz;
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new int[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  int o = new int[]{<CompleteOnName:zzz>};
		  public X() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0083_Diet(){
	String str =
		"""
		package p;
		public class X {
		  {
		    #
		    int o = new int[]{zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  {
		  }
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0083_Method(){
	String str =
		"""
		package p;
		public class X {
		  {
		    #
		    int o = new int[]{zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  {
		    {
		      int o;
		      {
		        <CompleteOnName:zzz>;
		      }
		    }
		  }
		  public X() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0084_Diet(){


	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    int o = new int[]{zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new int[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    {
		      new int[]{<CompleteOnName:zzz>};
		    }
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0084_Method(){


	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    int o = new int[]{zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    int o;
		    {
		      <CompleteOnName:zzz>;
		    }
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0085_Diet(){
	String str =
		"""
		package p;
		public class X {
		  X o = new X[]{zzz;
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  X o = new X[]{<CompleteOnName:zzz>};
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0085_Method(){
	String str =
		"""
		package p;
		public class X {
		  X o = new X[]{zzz;
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  X o = new X[]{<CompleteOnName:zzz>};
		  public X() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0086_Diet(){
	String str =
		"""
		package p;
		public class X {
		  {
		    X o = new X[]{zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  {
		  }
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0086_Method(){
	String str =
		"""
		package p;
		public class X {
		  {
		    X o = new X[]{zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  {
		    X o = new X[]{<CompleteOnName:zzz>};
		  }
		  public X() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0087_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    X o = new X[]{zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    {
		      new X[]{<CompleteOnName:zzz>};
		    }
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0087_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    X o = new X[]{zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    X o = new X[]{<CompleteOnName:zzz>};
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0088_Diet(){
	String str =
		"""
		package p;
		public class X {
		  #
		  X o = new X[]{zzz;
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  X o = new X[]{<CompleteOnName:zzz>};
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0088_Method(){
	String str =
		"""
		package p;
		public class X {
		  #
		  X o = new X[]{zzz;
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  X o = new X[]{<CompleteOnName:zzz>};
		  public X() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0089_Diet(){
	String str =
		"""
		package p;
		public class X {
		  {
		    #
		    X o = new X[]{zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  {
		  }
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0089_Method(){
	String str =
		"""
		package p;
		public class X {
		  {
		    #
		    X o = new X[]{zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  {
		    {
		      X o;
		      {
		        <CompleteOnName:zzz>;
		      }
		    }
		  }
		  public X() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0090_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    X o = new X[]{zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "new X[]{<CompleteOnName:zzz>}";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    {
		      new X[]{<CompleteOnName:zzz>};
		    }
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0090_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    X o = new X[]{zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    X o;
		    {
		      <CompleteOnName:zzz>;
		    }
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0091(){
	String str =
		"""
		package p;
		public class X {
		  Object o = "yyy;
		  zzz
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  Object o;
		  <CompleteOnType:zzz>;
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}


public void test0092_Diet(){
	String str =
		"""
		package p;
		public class X {
		  {
		    Object o = "yyy;
		    zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  {
		  }
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0092_Method(){
	String str =
		"""
		package p;
		public class X {
		  {
		    Object o = "yyy;
		    zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  {
		    Object o;
		    <CompleteOnName:zzz>;
		  }
		  public X() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0093_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    Object o = "yyy;
		    zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0093_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    Object o = "yyy;
		    zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    Object o;
		    <CompleteOnName:zzz>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0094(){
	String str =
		"""
		package p;
		public class X {
		  #
		  Object o = "yyy;
		  zzz
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  Object o;
		  <CompleteOnType:zzz>;
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}


public void test0095_Diet(){
	String str =
		"""
		package p;
		public class X {
		  {
		    #
		    Object o = "yyy;
		    zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  {
		  }
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0095_Method(){
	String str =
		"""
		package p;
		public class X {
		  {
		    #
		    Object o = "yyy;
		    zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  {
		    {
		      Object o;
		      <CompleteOnName:zzz>;
		    }
		  }
		  public X() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}


public void test0096_Diet(){
	String str =
		"""
		package p;
		public class X {
		  {
		    #
		    Object o = "yyy;
		    zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  {
		  }
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0096_Method(){
	String str =
		"""
		package p;
		public class X {
		  {
		    #
		    Object o = "yyy;
		    zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  {
		    {
		      Object o;
		      <CompleteOnName:zzz>;
		    }
		  }
		  public X() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0097_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    Object o;
		    o = zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0097_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    Object o;
		    o = zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "o = <CompleteOnName:zzz>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    Object o;
		    o = <CompleteOnName:zzz>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0098_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    Object o;
		    o = zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0098_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    Object o;
		    o = zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "o = <CompleteOnName:zzz>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    Object o;
		    o = <CompleteOnName:zzz>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0099_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    Object o;
		    o = new zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0099_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    Object o;
		    o = new zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:zzz>";
	String expectedParentNodeToString = "o = new <CompleteOnType:zzz>()";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    Object o;
		    o = new <CompleteOnType:zzz>();
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0100_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    Object o;
		    o = new zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0100_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    Object o;
		    o = new zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:zzz>";
	String expectedParentNodeToString = "o = new <CompleteOnType:zzz>()";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    Object o;
		    o = new <CompleteOnType:zzz>();
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0101_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    Object o;
		    o = yyy;
		    zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0101_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    Object o;
		    o = yyy;
		    zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    Object o;
		    <CompleteOnName:zzz>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0102_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    Object o;
		    o = yyy;
		    zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0102_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    Object o;
		    o = yyy;
		    zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    Object o;
		    <CompleteOnName:zzz>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0103_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    Object o;
		    o = "yyy;
		    zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0103_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    Object o;
		    o = "yyy;
		    zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    Object o;
		    <CompleteOnName:zzz>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0104_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    Object o;
		    o = "yyy;
		    zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0104_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    Object o;
		    o = "yyy;
		    zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    Object o;
		    <CompleteOnName:zzz>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}

public void test0105_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    int x = 1 + zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0105_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    int x = 1 + zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "(1 + <CompleteOnName:zzz>)";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    int x = (1 + <CompleteOnName:zzz>);
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0106_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    int x = 1 + (zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0106_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    int x = 1 + (zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "(1 + <CompleteOnName:zzz>)";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    int x = (1 + <CompleteOnName:zzz>);
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0107_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    int x = 0;
		    int y = 1 + x;
		    zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0107_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    int x = 0;
		    int y = 1 + x;
		    zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    int x;
		    int y;
		    <CompleteOnName:zzz>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0108_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    int x = -zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0108_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    int x = -zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "(- <CompleteOnName:zzz>)";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    int x = (- <CompleteOnName:zzz>);
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0109_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    int x = -(zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0109_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    int x = -(zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "(- <CompleteOnName:zzz>)";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    int x = (- <CompleteOnName:zzz>);
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0110_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    int x = 0;
		    int y = -x;
		    zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0110_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    int x = 0;
		    int y = -x;
		    zzz;
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    int x;
		    int y;
		    <CompleteOnName:zzz>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0111_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    if(1 == zzz) {}
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0111_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    if(1 == zzz) {}
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "(1 == <CompleteOnName:zzz>)";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    if ((1 == <CompleteOnName:zzz>))
		        {
		        }
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0112_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    if(1 == (zzz)) {}
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0112_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    if(1 == (zzz)) {}
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "(1 == <CompleteOnName:zzz>)";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    if ((1 == <CompleteOnName:zzz>))
		        {
		        }
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0113_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(Object x){
		    if(x instanceof ZZZ) {}
		  }
		}
		""";

	String completeBehind = "ZZZ";
	int cursorLocation = str.indexOf("ZZZ") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo(Object x) {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0113_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(Object x){
		    if(x instanceof ZZZ) {}
		  }
		}
		""";

	String completeBehind = "ZZZ";
	int cursorLocation = str.indexOf("ZZZ") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:ZZZ>";
	String expectedParentNodeToString = "(x instanceof <CompleteOnType:ZZZ>)";
	String completionIdentifier = "ZZZ";
	String expectedReplacedSource = "ZZZ";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo(Object x) {
		    if ((x instanceof <CompleteOnType:ZZZ>))
		        {
		        }
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0114_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    boolean a, b, c;
		    c = a == b ? zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0114_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    boolean a, b, c;
		    c = a == b ? zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "c = <CompleteOnName:zzz>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    boolean a;
		    boolean b;
		    boolean c;
		    c = <CompleteOnName:zzz>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0115_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    boolean a, b;
		    a == b ? zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0115_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    boolean a, b;
		    a == b ? zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    boolean a;
		    boolean b;
		    <CompleteOnName:zzz>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0116_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    boolean a, b, c;
		    c = a == b ? a : zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0116_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    boolean a, b, c;
		    c = a == b ? a : zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "c = ((a == b) ? a : <CompleteOnName:zzz>)";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    boolean a;
		    boolean b;
		    boolean c;
		    c = ((a == b) ? a : <CompleteOnName:zzz>);
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0117_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    boolean a, b, c;
		    c = a == b ? a : (zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0117_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    boolean a, b, c;
		    c = a == b ? a : (zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "c = <CompleteOnName:zzz>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    boolean a;
		    boolean b;
		    boolean c;
		    c = <CompleteOnName:zzz>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0118_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    boolean a, b, c;
		    c = a# == b ? a : zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0118_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    boolean a, b, c;
		    c = a# == b ? a : zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    boolean a;
		    boolean b;
		    boolean c;
		    <CompleteOnName:zzz>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0119_Diet(){
	String str =
		"""
		public class X {
		  void foo(){
		    switch(1) {
		      case zzz
		    }
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0119_Method(){
	String str =
		"""
		public class X {
		  void foo(){
		    switch(1) {
		      case zzz
		    }
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString =
			"""
		switch (1) {
		case <CompleteOnName:zzz> :
		}""";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    {
		      switch (1) {
		      case <CompleteOnName:zzz> :
		      }
		    }
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0120_Diet(){
	String str =
		"""
		public class X {
		  void foo(){
		    switch(1) {
		      case Something :
		      case zzz
		    }
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0120_Method(){
	String str =
		"""
		public class X {
		  void foo(){
		    switch(1) {
		      case Something :
		      case zzz
		    }
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString =
			"""
		switch (1) {
		case Something :
		case <CompleteOnName:zzz> :
		}""";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    {
		      switch (1) {
		      case Something :
		      case <CompleteOnName:zzz> :
		      }
		    }
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0121_Diet(){
	String str =
		"""
		public class X {
		  void foo(){
		    tab[zzz]
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0121_Method(){
	String str =
		"""
		public class X {
		  void foo(){
		    tab[zzz]
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "tab[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    tab[<CompleteOnName:zzz>];
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0122_Diet(){
	String str =
		"""
		public class X {
		  void foo(){
		    tab[].zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0122_Method(){
	String str =
		"""
		public class X {
		  void foo(){
		    tab[].zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnClassLiteralAccess:tab[].zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "tab[].zzz";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnClassLiteralAccess:tab[].zzz>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0123_Diet(){
	String str =
		"""
		public class X {
		  void foo(){
		    tab[0].zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0123_Method(){
	String str =
		"""
		public class X {
		  void foo(){
		    tab[0].zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:tab[0].zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnMemberAccess:tab[0].zzz>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0124_Diet(){
	String str =
		"""
		public class X {
		  void foo(){
		    foo()[zzz]
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0124_Method(){
	String str =
		"""
		public class X {
		  void foo(){
		    foo()[zzz]
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "foo()[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    foo()[<CompleteOnName:zzz>];
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0125_Diet(){
	String str =
		"""
		public class X {
		  void foo(){
		    foo()[].zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0125_Method(){
	String str =
		"""
		public class X {
		  void foo(){
		    foo()[].zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:zzz>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0126_Diet(){
	String str =
		"""
		public class X {
		  void foo(){
		    foo()[1].zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0126_Method(){
	String str =
		"""
		public class X {
		  void foo(){
		    foo()[1].zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:foo()[1].zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnMemberAccess:foo()[1].zzz>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0127(){
	String str =
		"""
		public class X {
		  void foo(){
		    if (zzz() == null) bar = null;
		  }
		  Object o = new O();
		}
		""";

	String completeBehind = "O";
	int cursorLocation = str.lastIndexOf("O") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:O>";
	String expectedParentNodeToString = "Object o = new <CompleteOnType:O>();";
	String completionIdentifier = "O";
	String expectedReplacedSource = "O";
	String expectedUnitDisplayString =
		"""
		public class X {
		  Object o = new <CompleteOnType:O>();
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0128_Diet(){
	String str =
		"""
		public class X {
		  void foo(){
		    new Object() {
		      void bar() {
		        a[zzz
		      }
		    }
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0128_Method(){
	String str =
		"""
		public class X {
		  void foo(){
		    new Object() {
		      void bar() {
		        a[zzz
		      }
		    }
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "a[<CompleteOnName:zzz>]";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    new Object() {
		      void bar() {
		        a[<CompleteOnName:zzz>];
		      }
		    };
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
public void test0129_Diet(){
	String str =
		"""
		public class X {
		  void foo(){
		    Object local;
		    double bar;
		    for(;;) {
		      bar = (double)0;
		    }
		    zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
public void test0129_Method(){
	String str =
		"""
		public class X {
		  void foo(){
		    Object local;
		    double bar;
		    for(;;) {
		      bar = (double)0;
		    }
		    zzz
		  }
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    Object local;
		    double bar;
		    <CompleteOnName:zzz>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=42856
 */
public void test0130_Diet(){
	String str =
		"""
		public class X {
		  void foo(){
		    A.B c = null;
		    zzz();
		  }
		}
		""";

	String completeBehind = "zzz(";
	int cursorLocation = str.indexOf("zzz(") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=42856
 */
public void test0130_Method(){
	String str =
		"""
		public class X {
		  void foo(){
		    A.B c = null;
		    zzz();
		  }
		}
		""";

	String completeBehind = "zzz(";
	int cursorLocation = str.indexOf("zzz(") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnMessageSend:zzz()>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "zzz(";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    A.B c;
		    <CompleteOnMessageSend:zzz()>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=42856
 */
public void test0131_Diet(){
	String str =
		"""
		public class X {
		  void foo(){
		    try {
		    } catch(A.B e) {
		      zzz();
		    }
		  }
		}
		""";

	String completeBehind = "zzz(";
	int cursorLocation = str.indexOf("zzz(") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=42856
 */
public void test0131_Method(){
	String str =
		"""
		public class X {
		  void foo(){
		    try {
		    } catch(A.B e) {
		      zzz();
		    }
		  }
		}
		""";

	String completeBehind = "zzz(";
	int cursorLocation = str.indexOf("zzz(") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnMessageSend:zzz()>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "zzz(";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    {
		      A.B e;
		      <CompleteOnMessageSend:zzz()>;
		    }
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44647
 */
public void test0132_Diet(){
	String str =
		"""
		public class A
		{
		   public A(final String str1, final String str2)
		   {
		     \s
		   }
		  \s
		   private A[] methodA(final String str1, final String str2)
		      {
		         return new A[]
		         {
		            new A(str1, str2)
		            {
		               //initialiser!
		               {
		                  methodA("1", "2");
		               }
		            },
		            new A("hello".c) //<--------code complete to "hello".concat()
		         };
		     \s
		      }
		}
		""";


	String completeBehind = "\"2\");";
	int cursorLocation = str.indexOf("\"2\");") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class A {
		  public A(final String str1, final String str2) {
		  }
		  private A[] methodA(final String str1, final String str2) {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44647
 */
public void test0132_Method(){
	String str =
		"""
		public class A
		{
		   public A(final String str1, final String str2)
		   {
		     \s
		   }
		  \s
		   private A[] methodA(final String str1, final String str2)
		      {
		         return new A[]
		         {
		            new A(str1, str2)
		            {
		               //initialiser!
		               {
		                  methodA("1", "2");
		               }
		            },
		            new A("hello".c) //<--------code complete to "hello".concat()
		         };
		     \s
		      }
		}
		""";


	String completeBehind = "\"2\");";
	int cursorLocation = str.indexOf("\"2\");") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:>";
	String expectedParentNodeToString =
			"""
		new A(str1, str2) {
		  {
		    <CompleteOnName:>;
		  }
		}""";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	String expectedUnitDisplayString =
			"""
		public class A {
		  public A(final String str1, final String str2) {
		  }
		  private A[] methodA(final String str1, final String str2) {
		    new A(str1, str2) {
		      {
		        <CompleteOnName:>;
		      }
		    };
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=46470
 */
public void test0133_Diet(){
	String str =
	"""
		public class X {
		   int x;
		   void foo() {
		      switch(x){
		         case 0:
		            break;
		      }
		      bar
		   }
		}
		""";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  int x;
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=46470
 */
public void test0133_Method(){
	String str =
	"""
		public class X {
		   int x;
		   void foo() {
		      switch(x){
		         case 0:
		            break;
		      }
		      bar
		   }
		}
		""";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:bar>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "bar";
	String expectedReplacedSource = "bar";
	String expectedUnitDisplayString =
			"""
		public class X {
		  int x;
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:bar>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=43212
 */
public void test0134(){
	String str =
	"""
		public class X {
			Object o = new Object() {
				void foo() {
					try {
					} catch(Exception e) {
						e.
					}
				}
			};
		}
		""";


	String completeBehind = "e.";
	int cursorLocation = str.indexOf("e.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:e.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "e.";
	String expectedUnitDisplayString =
		"""
		public class X {
		  Object o = new Object() {
		    void foo() {
		      {
		        Exception e;
		        <CompleteOnName:e.>;
		      }
		    }
		  };
		  public X() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=43212
 */
public void test0135_Diet(){
	String str =
	"""
		public class X {
			void bar(){
				#
				class Inner {
					void foo() {
						try {
						} catch(Exception e) {
							e.
						}
					}
				}
			}
		}
		""";


	String completeBehind = "e.";
	int cursorLocation = str.indexOf("e.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void bar() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=43212
 */
public void test0135_Method(){
	String str =
	"""
		public class X {
			void bar(){
				#
				class Inner {
					void foo() {
						try {
						} catch(Exception e) {
							e.
						}
					}
				}
			}
		}
		""";


	String completeBehind = "e.";
	int cursorLocation = str.indexOf("e.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:e.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "e.";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void bar() {
		    class Inner {
		      Inner() {
		      }
		      void foo() {
		        {
		          Exception e;
		          <CompleteOnName:e.>;
		        }
		      }
		    }
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=48070
 */
public void test0136(){
	String str =
		"""
		public class X {
			void bar(){
			}
		}
		""";


	String completeBehind = "ba";
	int cursorLocation = str.indexOf("ba") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompletionOnMethodName:void ba()>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ba";
	String expectedReplacedSource = "bar()";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  <CompletionOnMethodName:void ba()>
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=53624
 */
public void test0137_Diet(){
	String str =
		"""
		public class X {
			void foo(){
				new Object(){
					void bar(){
						super.zzz();
					}
				};
			}
		}
		""";


	String completeBehind = "zzz(";
	int cursorLocation = str.indexOf("zzz(") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=53624
 */
public void test0137_Method(){
	String str =
		"""
		public class X {
			void foo(){
				new Object(){
					void bar(){
						super.zzz();
					}
				};
			}
		}
		""";


	String completeBehind = "zzz(";
	int cursorLocation = str.indexOf("zzz(") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnMessageSend:super.zzz()>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "zzz()";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    new Object() {
		      void bar() {
		        <CompleteOnMessageSend:super.zzz()>;
		      }
		    };
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=41395
 */
public void test0138_Diet(){
	String str =
		"""
		public class X{
		  public void foo() {
		    new Y() {
		      public void bar() {
		        switch (zzz){
		          case 1 :
		          };
		        }
		        new Z() {
		          public void toto() {	\t
		        }
		      });
		    });
		  }
		}
		
		""";


	String completeBehind = "to";
	int cursorLocation = str.indexOf("to") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  public void foo() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=41395
 */
public void test0138_Method(){
	String str =
		"""
		public class X{
		  public void foo() {
		    new Y() {
		      public void bar() {
		        switch (zzz){
		          case 1 :
		          };
		        }
		        new Z() {
		          public void toto() {	\t
		        }
		      });
		    });
		  }
		}
		
		""";


	String completeBehind = "to";
	int cursorLocation = str.indexOf("to") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnFieldName:void to>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "to";
	String expectedReplacedSource = "toto";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  public void foo() {
		    new Y() {
		      public void bar() {
		        new Z() {
		          <CompleteOnFieldName:void to>;
		          {
		          }
		        };
		      }
		    };
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0139(){
	String str =
		"public class X  extends Z. #  {\n" +
		"}";


	String completeBehind = "Z.";
	int cursorLocation = str.indexOf("Z.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnClass:Z.>";
	String expectedParentNodeToString =
		"""
		public class X extends <CompleteOnClass:Z.> {
		  {
		  }
		  public X() {
		  }
		}""";
	String completionIdentifier = "";
	String expectedReplacedSource = "Z.";
	String expectedUnitDisplayString =
		"""
		public class X extends <CompleteOnClass:Z.> {
		  {
		  }
		  public X() {
		  }
		}
		"""
		;

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=32061
 */
public void test0140(){
	String str =
		"""
		public class X  {
		    public void baz() {
		    	new Object() {
		            public void bar() {
		            }
		        };
		    }
		    private Object var = new Object() {
		        public void foo(Object e) {
		           e.
		        }
		    };
		}""";


	String completeBehind = "e.";
	int cursorLocation = str.indexOf("e.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:e.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "e.";
	String expectedUnitDisplayString =
		"""
		public class X {
		  private Object var = new Object() {
		    public void foo(Object e) {
		      <CompleteOnName:e.>;
		    }
		  };
		  public X() {
		  }
		  public void baz() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=32061
 */
public void test0141(){
	String str =
		"""
		public class X  {
		    Object var1 = new Object() {};
		    void bar() {
		        new Object() {};
		        bar();
		    }
		    Object var2 = new\s
		}""";


	String completeBehind = "var2 = new ";
	int cursorLocation = str.indexOf("var2 = new ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:>";
	String expectedParentNodeToString = "Object var2 = new <CompleteOnType:>();";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	String expectedUnitDisplayString =
		"""
		public class X {
		  Object var1;
		  Object var2 = new <CompleteOnType:>();
		  public X() {
		  }
		  void bar() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=39499
 */
public void test0142_Diet(){
	String str =
		"""
		public class X{
		  public void foo() {
		    bar(new Object(){
		      public void toto() {
		        if(a instanceof Object) {}
		      }
		    });
		  }
		}
		
		""";


	String completeBehind = "instanceof";
	int cursorLocation = str.indexOf("instanceof") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  public void foo() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=39499
 */
public void test0142_Method(){
	String str =
		"""
		public class X{
		  public void foo() {
		    bar(new Object(){
		      public void toto() {
		        if(a instanceof Object) {}
		      }
		    });
		  }
		}
		
		""";


	String completeBehind = "instanceof";
	int cursorLocation = str.indexOf("instanceof") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnKeyword:instanceof>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "instanceof";
	String expectedReplacedSource = "instanceof";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  public void foo() {
		    new Object() {
		      public void toto() {
		        <CompleteOnKeyword:instanceof>;
		      }
		    };
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=71702
 */
public void test0143_Diet(){
	String str =
		"""
		public class X{
		  public void foo() {
		    Object o =(int) tmp;
		    bar
		  }
		}
		
		""";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  public void foo() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=71702
 */
public void test0143_Method(){
	String str =
		"""
		public class X{
		  public void foo() {
		    Object o =(int) tmp;
		    bar
		  }
		}
		
		""";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:bar>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "bar";
	String expectedReplacedSource = "bar";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  public void foo() {
		    Object o;
		    <CompleteOnName:bar>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=71702
 */
public void test0144_Diet(){
	String str =
		"""
		public class X{
		  public void foo() {
		    Object o =(int[]) tmp;
		    bar
		  }
		}
		
		""";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  public void foo() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=71702
 */
public void test0144_Method(){
	String str =
		"""
		public class X{
		  public void foo() {
		    Object o =(int[]) tmp;
		    bar
		  }
		}
		
		""";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:bar>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "bar";
	String expectedReplacedSource = "bar";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  public void foo() {
		    Object o;
		    <CompleteOnName:bar>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=71702
 */
public void test0145_Diet(){
	String str =
		"""
		public class X{
		  public void foo() {
		    Object o =(X) tmp;
		    bar
		  }
		}
		
		""";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  public void foo() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=71702
 */
public void test0145_Method(){
	String str =
		"""
		public class X{
		  public void foo() {
		    Object o =(X) tmp;
		    bar
		  }
		}
		
		""";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:bar>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "bar";
	String expectedReplacedSource = "bar";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  public void foo() {
		    Object o;
		    <CompleteOnName:bar>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=71702
 */
public void test0146_Diet(){
	String str =
		"""
		public class X{
		  public void foo() {
		    Object o =(X[]) tmp;
		    bar
		  }
		}
		
		""";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  public void foo() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=71702
 */
public void test0146_Method(){
	String str =
		"""
		public class X{
		  public void foo() {
		    Object o =(X[]) tmp;
		    bar
		  }
		}
		
		""";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:bar>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "bar";
	String expectedReplacedSource = "bar";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  public void foo() {
		    Object o;
		    <CompleteOnName:bar>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=72352
 */
public void test0147(){
	String str =
		"""
		public class Test {
		  Object m;
		  String[] values = (String[]) m;
		  lo
		  }""";

	String completeBehind = "lo";
	int cursorLocation = str.indexOf("lo") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:lo>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "lo";
	String expectedReplacedSource = "lo";
	String expectedUnitDisplayString =
		"""
		public class Test {
		  Object m;
		  String[] values;
		  <CompleteOnType:lo>;
		  public Test() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=83236
 */
public void test0148(){
	String str =
		"""
		public class Test {
		  Boolean
		   * some text <b>bold<i>both</i></b>
		   */
		  public void foo(String s) {
		  }
		}
		""";

	String completeBehind = "Boolean";
	int cursorLocation = str.indexOf("Boolean") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Boolean>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "Boolean";
	String expectedReplacedSource = "Boolean";
	String expectedUnitDisplayString =
		"""
		public class Test {
		  <CompleteOnType:Boolean>;
		  some text;
		  bold<i> both;
		  public Test() {
		  }
		  public void foo(String s) {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=91371
 */
public void test0149_Diet(){
	String str =
		"""
		public class X{
		  public void foo() {
		    new Object(){
		      void bar(){
		        if((titi & (ZZ
		}
		
		""";


	String completeBehind = "ZZ";
	int cursorLocation = str.indexOf("ZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ZZ";
	String expectedReplacedSource = "ZZ";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  public void foo() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=91371
 */
public void test0149_Method(){
	String str =
		"""
		public class X{
		  public void foo() {
		    new Object(){
		      void bar(){
		        if((titi & (ZZ
		}
		
		""";


	String completeBehind = "ZZ";
	int cursorLocation = str.indexOf("ZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:ZZ>";
	String expectedParentNodeToString = "(titi & <CompleteOnName:ZZ>)";
	String completionIdentifier = "ZZ";
	String expectedReplacedSource = "ZZ";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  public void foo() {
		    new Object() {
		      void bar() {
		        (titi & <CompleteOnName:ZZ>);
		      }
		    };
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=91371
 */
public void test0150_Diet(){
	String str =
		"""
		public class X{
		  public void foo() {
		    if((titi & (ZZ
		}
		
		""";


	String completeBehind = "ZZ";
	int cursorLocation = str.indexOf("ZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ZZ";
	String expectedReplacedSource = "ZZ";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  public void foo() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=91371
 */
public void test0150_Method(){
	String str =
		"""
		public class X{
		  public void foo() {
		    if((titi & (ZZ
		}
		
		""";


	String completeBehind = "ZZ";
	int cursorLocation = str.indexOf("ZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:ZZ>";
	String expectedParentNodeToString = "(titi & <CompleteOnName:ZZ>)";
	String completionIdentifier = "ZZ";
	String expectedReplacedSource = "ZZ";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  public void foo() {
		    (titi & <CompleteOnName:ZZ>);
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=92451
 */
public void test0151_Diet(){
	String str =
		"""
		public class X {
		  public static void main(String[] args) {
		    java.util.List elements = null;
		    new Test(Test.toStrings((Test[])elements.toArray(new Test[0])));
		     //code assist fails on this line
		  }
		}
		""";


	String completeBehind = "";
	int cursorLocation = str.indexOf(" //code assis") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  public static void main(String[] args) {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=92451
 */
public void test0151_Method(){
	String str =
		"""
		public class X {
		  public static void main(String[] args) {
		    java.util.List elements = null;
		    new Test(Test.toStrings((Test[])elements.toArray(new Test[0])));
		     //code assist fails on this line
		  }
		}
		""";


	String completeBehind = "";
	int cursorLocation = str.indexOf(" //code assis") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  public static void main(String[] args) {
		    java.util.List elements;
		    <CompleteOnName:>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=98115
 */
public void test0152(){
	String str =
		"""
		public class X {
		  Object var = new Object() {
		    void bar() {
		      int i = 0;
		    }
		    void foo() {
		      zzz
		    }
		  };
		}
		""";


	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
		"""
		public class X {
		  Object var = new Object() {
		    void bar() {
		    }
		    void foo() {
		      <CompleteOnName:zzz>;
		    }
		  };
		  public X() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=22072
 */
public void test0153_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    label1 : for(;;) {
		      break lab
		    }
		  }
		}
		""";


	String completeBehind = "lab";
	int cursorLocation = str.lastIndexOf("lab") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=22072
 */
public void test0153_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    label1 : for(;;) {
		      break lab
		    }
		  }
		}
		""";


	String completeBehind = "lab";
	int cursorLocation = str.lastIndexOf("lab") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "break <CompleteOnLabel:lab>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "lab";
	String expectedReplacedSource = "lab";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    break <CompleteOnLabel:lab>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=22072
 */
public void test0154_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    label1 : for(;;) {
		      break lab
		    }
		  }
		}
		""";


	String completeBehind = "lab";
	int cursorLocation = str.lastIndexOf("lab") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=22072
 */
public void test0154_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    label1 : for(;;) {
		      break lab
		    }
		  }
		}
		""";


	String completeBehind = "lab";
	int cursorLocation = str.lastIndexOf("lab") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "break <CompleteOnLabel:lab>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "lab";
	String expectedReplacedSource = "lab";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    {
		      break <CompleteOnLabel:lab>;
		    }
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=22072
 */
public void test0155_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    label1 : for(;;) {
		      continue lab
		    }
		  }
		}
		""";


	String completeBehind = "lab";
	int cursorLocation = str.lastIndexOf("lab") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=22072
 */
public void test0155_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    label1 : for(;;) {
		      continue lab
		    }
		  }
		}
		""";


	String completeBehind = "lab";
	int cursorLocation = str.lastIndexOf("lab") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "continue <CompleteOnLabel:lab>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "lab";
	String expectedReplacedSource = "lab";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    continue <CompleteOnLabel:lab>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=22072
 */
public void test0156_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    label1 : for(;;) {
		      continue lab
		    }
		  }
		}
		""";


	String completeBehind = "lab";
	int cursorLocation = str.lastIndexOf("lab") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=22072
 */
public void test0156_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    label1 : for(;;) {
		      continue lab
		    }
		  }
		}
		""";


	String completeBehind = "lab";
	int cursorLocation = str.lastIndexOf("lab") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "continue <CompleteOnLabel:lab>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "lab";
	String expectedReplacedSource = "lab";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    {
		      continue <CompleteOnLabel:lab>;
		    }
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=22072
 */
public void test0157_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    label1 : for(;;) {
		      class X {
		        void foo() {
		          label2 : for(;;) foo();
		        }
		      }
		      continue lab
		    }
		  }
		}
		""";


	String completeBehind = "lab";
	int cursorLocation = str.lastIndexOf("lab") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=22072
 */
public void test0157_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    label1 : for(;;) {
		      class X {
		        void foo() {
		          label2 : for(;;) foo();
		        }
		      }
		      continue lab
		    }
		  }
		}
		""";


	String completeBehind = "lab";
	int cursorLocation = str.lastIndexOf("lab") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "continue <CompleteOnLabel:lab>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "lab";
	String expectedReplacedSource = "lab";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    {
		      class X {
		        X() {
		          super();
		        }
		        void foo() {
		        }
		      }
		      continue <CompleteOnLabel:lab>;
		    }
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=22072
 */
public void test0158_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    label1 : for(;;) {
		      class X {
		        void foo() {
		          label2 : for(;;) {
		            continue lab
		          }
		        }
		      }
		    }
		  }
		}
		""";


	String completeBehind = "lab";
	int cursorLocation = str.lastIndexOf("lab") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=22072
 */
public void test0158_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    label1 : for(;;) {
		      class X {
		        void foo() {
		          label2 : for(;;) {
		            continue lab
		          }
		        }
		      }
		    }
		  }
		}
		""";


	String completeBehind = "lab";
	int cursorLocation = str.lastIndexOf("lab") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "continue <CompleteOnLabel:lab>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "lab";
	String expectedReplacedSource = "lab";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    {
		      class X {
		        X() {
		        }
		        void foo() {
		          {
		            continue <CompleteOnLabel:lab>;
		          }
		        }
		      }
		    }
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0159() {

	String str =
		"""
		public class X {
			String s = "ZZZZZ";
		}
		""";

	String completeBehind = "ZZZ";
	String expectedCompletionNodeToString = "<CompletionOnString:\"ZZZ\">";
	String completionIdentifier = "ZZZ";
	String expectedUnitDisplayString =
		"""
		public class X {
		  String s = <CompletionOnString:"ZZZ">;
		  public X() {
		  }
		}
		""";
	String expectedReplacedSource = "\"ZZZZZ\"";
	String testName = "<complete inside a string literal>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void test0160() {

	String str =
		"""
		public class X {
			String s = \\u0022ZZ\\u005AZZ\\u0022;
		}
		""";

	String completeBehind = "ZZ\\u005A";
	String expectedCompletionNodeToString = "<CompletionOnString:\"ZZZ\">";
	String completionIdentifier = "ZZZ";
	String expectedUnitDisplayString =
		"""
		public class X {
		  String s = <CompletionOnString:"ZZZ">;
		  public X() {
		  }
		}
		""";
	String expectedReplacedSource = "\\u0022ZZ\\u005AZZ\\u0022";
	String testName = "<complete inside a string literal>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void test0161() {

	String str =
		"""
		public class X {
			String s = "AAAAA" + "ZZZZZ";
		}
		""";

	String completeBehind = "ZZZ";
	String expectedCompletionNodeToString = "<CompletionOnString:\"ZZZ\">";
	String completionIdentifier = "ZZZ";
	String expectedUnitDisplayString =
		"""
		public class X {
		  String s = ("AAAAA" + <CompletionOnString:"ZZZ">);
		  public X() {
		  }
		}
		""";
	String expectedReplacedSource = "\"ZZZZZ\"";
	String testName = "<complete inside a string literal>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void test0162() {

	String str =
		"""
		public class X {
			String s = "ZZZZZ
		}
		""";

	String completeBehind = "ZZZ";
	String expectedCompletionNodeToString = "<CompletionOnString:\"ZZZ\">";
	String completionIdentifier = "ZZZ";
	String expectedUnitDisplayString =
		"""
		public class X {
		  String s = <CompletionOnString:"ZZZ">;
		  public X() {
		  }
		}
		""";
	String expectedReplacedSource = "\"ZZZZZ";
	String testName = "<complete inside a string literal>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void test0163() {

	String str =
		"public class X {\n" +
		"	String s = \"ZZZZZ";

	String completeBehind = "ZZZ";
	String expectedCompletionNodeToString = "<CompletionOnString:\"ZZZ\">";
	String completionIdentifier = "ZZZ";
	String expectedUnitDisplayString =
		"""
		public class X {
		  String s = <CompletionOnString:"ZZZ">;
		  public X() {
		  }
		}
		""";
	String expectedReplacedSource = "\"ZZZZZ";
	String testName = "<complete inside a string literal>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void test0164() {

	String str =
		"""
		public class X {
			String s = "\\u005AZZZZ\\u000D\\u0022\
		}
		""";

	String completeBehind = "\\u005AZZ";
	String expectedCompletionNodeToString = "<CompletionOnString:\"ZZZ\">";
	String completionIdentifier = "ZZZ";
	String expectedUnitDisplayString =
		"""
		public class X {
		  String s = <CompletionOnString:"ZZZ">;
		  public X() {
		  }
		}
		""";
	String expectedReplacedSource = "\"\\u005AZZZZ\\u000D\\u0022";
	String testName = "<complete inside a string literal>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=122755
public void test0165_Diet() {

	String str =
		"""
		public class X {
			void foo() {\
			/**\
			 *\
			 */.\
			}\
		}
		""";

	String completeBehind = "/.";
	int cursorLocation = str.lastIndexOf("/.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=122755
public void test0165_Method() {

	String str =
		"""
		public class X {
			void foo() {\
			/**\
			 *\
			 */.\
			}\
		}
		""";

	String completeBehind = "/.";
	int cursorLocation = str.lastIndexOf("/.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=137623
public void test0166_Diet() {

	String str =
		"""
		public class X {
			public boolean foo() {
		      if(this.equals(null))
		      {
		         (zzz==int.
		      }
		   }\
		}
		""";

	String completeBehind = "int.";
	int cursorLocation = str.lastIndexOf("int.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  public boolean foo() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=137623
public void test0166_Method() {

	String str =
		"""
		public class X {
			public boolean foo() {
		      if(this.equals(null))
		      {
		         (zzz==int.
		      }
		   }\
		}
		""";

	String completeBehind = "int.";
	int cursorLocation = str.lastIndexOf("int.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnClassLiteralAccess:int.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "int.";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  public boolean foo() {
		    <CompleteOnClassLiteralAccess:int.>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=157584
public void test0167_Diet() {

	String str =
		"""
		public class X {
			public boolean foo() {
		      try {
		         throwing();
		      }
		      catch (IllegalAccessException e) {
		         bar();
		      }
		      catch (IZZ) {
		      }
		   }\
		}
		""";

	String completeBehind = "IZZ";
	int cursorLocation = str.lastIndexOf("IZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  public boolean foo() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=157584
public void test0167_Method() {

	String str =
		"""
		public class X {
			public boolean foo() {
		      try {
		         throwing();
		      }
		      catch (IllegalAccessException e) {
		         bar();
		      }
		      catch (IZZ) {
		      }
		   }\
		}
		""";

	String completeBehind = "IZZ";
	int cursorLocation = str.lastIndexOf("IZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnException:IZZ>";
	String expectedParentNodeToString =
			"""
		try
		  {
		    throwing();
		  }
		catch (IllegalAccessException e)
		  {
		  }
		catch (<CompleteOnException:IZZ>  )
		  {
		  }""";
	String completionIdentifier = "IZZ";
	String expectedReplacedSource = "IZZ";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  public boolean foo() {
		    try
		      {
		        throwing();
		      }
		    catch (IllegalAccessException e)
		      {
		      }
		    catch (<CompleteOnException:IZZ>  )
		      {
		      }
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=157584
public void test0168_Diet() {

	String str =
		"""
		public class X {
			public boolean foo() {
		      try {
		         throwing();
		      }
		      catch (IllegalAccessException e) {
		         bar();
		      }
		      catch (IZZ
		   }\
		}
		""";

	String completeBehind = "IZZ";
	int cursorLocation = str.lastIndexOf("IZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  public boolean foo() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=157584
public void test0168_Method() {

	String str =
		"""
		public class X {
			public boolean foo() {
		      try {
		         throwing();
		      }
		      catch (IllegalAccessException e) {
		         bar();
		      }
		      catch (IZZ
		   }\
		}
		""";

	String completeBehind = "IZZ";
	int cursorLocation = str.lastIndexOf("IZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnException:IZZ>";
	String expectedParentNodeToString =
			"""
		try
		  {
		    throwing();
		  }
		catch (IllegalAccessException e)
		  {
		  }
		catch (<CompleteOnException:IZZ>  )
		  {
		  }""";
	String completionIdentifier = "IZZ";
	String expectedReplacedSource = "IZZ";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  public boolean foo() {
		    try
		      {
		        throwing();
		      }
		    catch (IllegalAccessException e)
		      {
		      }
		    catch (<CompleteOnException:IZZ>  )
		      {
		      }
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=157584
public void test0169_Diet() {

	String str =
		"""
		public class X {
			public boolean foo() {
		      try {
		         throwing();
		      }
		      catch (IllegalAccessException e) {
		         bar()
		      }
		      catch (IZZ) {
		      }
		   }\
		}
		""";

	String completeBehind = "IZZ";
	int cursorLocation = str.lastIndexOf("IZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  public boolean foo() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=157584
public void test0169_Method() {

	String str =
		"""
		public class X {
			public boolean foo() {
		      try {
		         throwing();
		      }
		      catch (IllegalAccessException e) {
		         bar()
		      }
		      catch (IZZ) {
		      }
		   }\
		}
		""";

	String completeBehind = "IZZ";
	int cursorLocation = str.lastIndexOf("IZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnException:IZZ>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "IZZ";
	String expectedReplacedSource = "IZZ";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  public boolean foo() {
		    <CompleteOnException:IZZ>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=157584
public void test0170_Diet() {

	String str =
		"""
		public class X {
			public boolean foo() {
		      #
		      try {
		         throwing();
		      }
		      catch (IllegalAccessException e) {
		         bar();
		      }
		      catch (IZZ) {
		      }
		   }\
		}
		""";

	String completeBehind = "IZZ";
	int cursorLocation = str.lastIndexOf("IZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  public boolean foo() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=157584
public void test0170_Method() {

	String str =
		"""
		public class X {
			public boolean foo() {
		      #
		      try {
		         throwing();
		      }
		      catch (IllegalAccessException e) {
		         bar();
		      }
		      catch (IZZ) {
		      }
		   }\
		}
		""";

	String completeBehind = "IZZ";
	int cursorLocation = str.lastIndexOf("IZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnException:IZZ>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "IZZ";
	String expectedReplacedSource = "IZZ";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  public boolean foo() {
		    <CompleteOnException:IZZ>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=157584
public void test0171_Diet() {

	String str =
		"""
		public class X {
			public boolean foo() {
		      try {
		         throwing();
		      }
		      catch (IZZ) {
		      }
		   }\
		}
		""";

	String completeBehind = "IZZ";
	int cursorLocation = str.lastIndexOf("IZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  public boolean foo() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=157584
public void test0171_Method() {

	String str =
		"""
		public class X {
			public boolean foo() {
		      try {
		         throwing();
		      }
		      catch (IZZ) {
		      }
		   }\
		}
		""";

	String completeBehind = "IZZ";
	int cursorLocation = str.lastIndexOf("IZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnException:IZZ>";
	String expectedParentNodeToString =
			"""
		try
		  {
		    throwing();
		  }
		catch (<CompleteOnException:IZZ>  )
		  {
		  }"""
	;
	String completionIdentifier = "IZZ";
	String expectedReplacedSource = "IZZ";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  public boolean foo() {
		    try
		      {
		        throwing();
		      }
		    catch (<CompleteOnException:IZZ>  )
		      {
		      }
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=150632
public void test0172() {

	String str =
		"""
		abstract class MatchFilter {
		    private static final String SETTINGS_LAST_USED_FILTERS= "filters_last_used";\s
		
		    // works if next line is commented out or moved to after PUBLIC_FILTER
		    public abstract String getName();
		
		    // content assist at new ModifierFilter(|):
		    private static final MatchFilter PUBLIC_FILTER= new ModifierFilter();
		}
		
		class ModifierFilter extends MatchFilter {
		   private final String fName;
		    public ModifierFilter(String name) {
		        fName= name;
		    }
		   public String getName() {
		        return fName;
		    }
		}
		""";

	String completeBehind = "new ModifierFilter(";
	int cursorLocation = str.lastIndexOf("new ModifierFilter(") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAllocationExpression:new ModifierFilter()>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	String expectedUnitDisplayString =
		"""
		abstract class MatchFilter {
		  private static final String SETTINGS_LAST_USED_FILTERS;
		  private static final MatchFilter PUBLIC_FILTER = <CompleteOnAllocationExpression:new ModifierFilter()>;
		  MatchFilter() {
		  }
		  <clinit>() {
		  }
		  public abstract String getName();
		}
		class ModifierFilter extends MatchFilter {
		  private final String fName;
		  public ModifierFilter(String name) {
		  }
		  public String getName() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=6004
public void test0173_Diet() {

	String str =
		"""
		public class Y {
		
			int foo(){
		\t
		\t
			int bar() {\t
		\t
			public int x = new Object(;
			/*<CODE ASSIST>*/
			}
			}
		}
		}
		""";

	String completeBehind = "";
	int cursorLocation = str.lastIndexOf("/*<CODE ASSIST>*/") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	String expectedUnitDisplayString =
		"""
		public class Y {
		  public int x;
		  <CompleteOnType:>;
		  public Y() {
		  }
		  int foo() {
		  }
		  int bar() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=6004
public void test0173_Method() {

	String str =
		"""
		public class Y {
		
			int foo(){
		\t
		\t
			int bar() {\t
		\t
			public int x = new Object(;
			/*<CODE ASSIST>*/
			}
			}
		}
		}
		""";

	String completeBehind = "";
	int cursorLocation = str.lastIndexOf("/*<CODE ASSIST>*/") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	String expectedUnitDisplayString =
			"""
		public class Y {
		  public int x;
		  <CompleteOnType:>;
		  public Y() {
		  }
		  int foo() {
		  }
		  int bar() {
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227546
public void test0174_Diet() {

	String str =
		"""
		public class X {
			# ; ZZZ
		}
		""";

	String completeBehind = "ZZZ";
	int cursorLocation = str.lastIndexOf("ZZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ZZZ";
	String expectedReplacedSource = "ZZZ";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227546
public void test0175_Diet() {

	String str =
		"""
		public class X {
			int # ZZZ
		}
		""";

	String completeBehind = "ZZZ";
	int cursorLocation = str.lastIndexOf("ZZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ZZZ";
	String expectedReplacedSource = "ZZZ";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227546
public void test0176_Diet() {

	String str =
		"""
		public class X {
			# int i; ZZZ
		}
		""";

	String completeBehind = "ZZZ";
	int cursorLocation = str.lastIndexOf("ZZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:ZZZ>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ZZZ";
	String expectedReplacedSource = "ZZZ";
	String expectedUnitDisplayString =
		"""
		public class X {
		  int i;
		  <CompleteOnType:ZZZ>;
		  public X() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227546
public void test0177_Diet() {

	String str =
		"""
		public class X {
			# void foo() {} ZZZ
		}
		""";

	String completeBehind = "ZZZ";
	int cursorLocation = str.lastIndexOf("ZZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:ZZZ>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ZZZ";
	String expectedReplacedSource = "ZZZ";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:ZZZ>;
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=229927
public void test0178_Method() {

	String str =
		"""
		package p;
		
		public class P {
		        private void foo(String key){
		                if (key != null) {
		                        String[] keys= { k };
		                }
		        }
		}
		""";

	String completeBehind = "k";
	int cursorLocation = str.lastIndexOf("k") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:k>";
	String expectedParentNodeToString = "String[] keys = {<CompleteOnName:k>};";
	String completionIdentifier = "k";
	String expectedReplacedSource = "k";
	String expectedUnitDisplayString =
			"""
		package p;
		public class P {
		  public P() {
		  }
		  private void foo(String key) {
		    if ((key != null))
		        {
		          String[] keys = {<CompleteOnName:k>};
		        }
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
}
