/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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

public class CompletionParserTestKeyword extends AbstractCompletionTest {
public CompletionParserTestKeyword(String testName) {
	super(testName);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(CompletionParserTestKeyword.class);
}
/*
 * Test for 'abstract' keyword.
 */
public void test0001(){
	String str =
		"abst";

	String completeBehind = "abst";
	int cursorLocation = str.indexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:abst>;\n";

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
 * Test for 'abstract' keyword.
 */
public void test0002(){
	String str =
		"abst zzz";

	String completeBehind = "abst";
	int cursorLocation = str.indexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:abst>;\n";

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
 * Test for 'abstract' keyword.
 */
public void test0003(){
	String str =
		"package p;\n" +
		"abst";

	String completeBehind = "abst";
	int cursorLocation = str.indexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"package p;\n" +
		"import <CompleteOnKeyword:abst>;\n";

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
 * Test for 'abstract' keyword.
 */
public void test0004(){
	String str =
		"package p;\n" +
		"abst zzz";

	String completeBehind = "abst";
	int cursorLocation = str.indexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"package p;\n" +
		"import <CompleteOnKeyword:abst>;\n";

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
 * Test for 'abstract' keyword.
 */
public void test0005(){
	String str =
		"""
		package p;
		import yyy;
		abst""";

	String completeBehind = "abst";
	int cursorLocation = str.indexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"""
		package p;
		import yyy;
		import <CompleteOnKeyword:abst>;
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
 * Test for 'abstract' keyword.
 */
public void test0006(){
	String str =
		"""
		package p;
		import yyy;
		abst zzz""";

	String completeBehind = "abst";
	int cursorLocation = str.indexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"""
		package p;
		import yyy;
		import <CompleteOnKeyword:abst>;
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
 * Test for 'abstract' keyword.
 */
public void test0007(){
	String str =
		"""
		package p;
		import yyy;
		public abst""";

	String completeBehind = "abst";
	int cursorLocation = str.indexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"""
		package p;
		import yyy;
		import <CompleteOnKeyword:abst>;
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
 * Test for 'abstract' keyword.
 */
public void test0008(){
	String str =
		"""
		package p;
		import yyy;
		public abst zzz""";

	String completeBehind = "abst";
	int cursorLocation = str.indexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"""
		package p;
		import yyy;
		import <CompleteOnKeyword:abst>;
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
 * Test for 'abstract' keyword.
 */
public void test0009(){
	String str =
		"""
		package p;
		import yyy;
		abstract abst""";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"""
		package p;
		import yyy;
		import <CompleteOnKeyword:abst>;
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
 * Test for 'abstract' keyword.
 */
public void test0010(){
	String str =
		"""
		package p;
		import yyy;
		abstract abst zzz""";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"""
		package p;
		import yyy;
		import <CompleteOnKeyword:abst>;
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
 * Test for 'abstract' keyword.
 */
public void test0011(){
	String str =
		"""
		package p;
		import\s
		abst""";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnImport:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"package p;\n" +
		"import <CompleteOnImport:abst>;\n";

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
 * Test for 'abstract' keyword.
 */
public void test0012(){
	String str =
		"""
		package p;
		import yyy;
		public class X {}
		abst""";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"""
		package p;
		import yyy;
		import <CompleteOnKeyword:abst>;
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
/*
 * Test for 'abstract' keyword.
 */
public void test0013(){
	String str =
		"""
		package p;
		import yyy;
		public class X {}
		abst zzz""";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"""
		package p;
		import yyy;
		import <CompleteOnKeyword:abst>;
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
/*
 * Test for 'abstract' keyword.
 */
public void test0014(){
	String str =
		"""
		package p;
		import yyy;
		final abst""";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"""
		package p;
		import yyy;
		import <CompleteOnKeyword:abst>;
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
 * Test for 'abstract' keyword.
 */
public void test0015(){
	String str =
		"""
		package p;
		public class X {
		  abst
		}
		""";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  <CompleteOnType:abst>;
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
 * Test for 'abstract' keyword.
 */
public void test0016(){
	String str =
		"""
		package p;
		public class X {
		  abst zzz
		}
		""";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:abst>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  <CompleteOnType:abst>;
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
 * Test for 'abstract' keyword.
 */
public void test0017(){
	String str =
		"""
		package p;
		public class X {
		  public abst zzz
		}
		""";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:abst>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  <CompleteOnType:abst>;
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
 * Test for 'abstract' keyword.
 */
public void test0018(){
	String str =
		"""
		package p;
		public class X {
		  final abst
		}
		""";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  <CompleteOnType:abst>;
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
 * Test for 'abstract' keyword.
 */
public void test0019(){
	String str =
		"""
		package p;
		public class X {
		  abstract abst
		}
		""";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  <CompleteOnType:abst>;
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
 * Test for 'abstract' keyword.
 */
public void test0020(){
	String str =
		"""
		package p;
		public class X {
		  static abst
		}
		""";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  <CompleteOnType:abst>;
		  public X() {
		  }
		  <clinit>() {
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
 * Test for 'abstract' keyword.
 */
public void test0021_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    abst
		  }
		}
		""";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

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
/*
 * Test for 'abstract' keyword.
 */
public void test0021_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    abst
		  }
		}
		""";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:abst>;
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
 * Test for 'abstract' keyword.
 */
public void test0022_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    abst zzz
		  }
		}
		""";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

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
/*
 * Test for 'abstract' keyword.
 */
public void test0022_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    abst zzz
		  }
		}
		""";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:abst>;
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
 * Test for 'break' keyword.
 */
public void test0023_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    bre
		  }
		}
		""";

	String completeBehind = "bre";
	int cursorLocation = str.lastIndexOf("bre") + completeBehind.length() - 1;

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
/*
 * Test for 'break' keyword.
 */
public void test0023_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    bre
		  }
		}
		""";

	String completeBehind = "bre";
	int cursorLocation = str.lastIndexOf("bre") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:bre>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "bre";
	String expectedReplacedSource = "bre";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:bre>;
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
 * Test for 'break' keyword.
 */
public void test0024_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    for(int i; i < 10; i++) {
		      bre
		    }
		  }
		}
		""";

	String completeBehind = "bre";
	int cursorLocation = str.lastIndexOf("bre") + completeBehind.length() - 1;

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
/*
 * Test for 'break' keyword.
 */
public void test0024_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    for(int i; i < 10; i++) {
		      bre
		    }
		  }
		}
		""";

	String completeBehind = "bre";
	int cursorLocation = str.lastIndexOf("bre") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:bre>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "bre";
	String expectedReplacedSource = "bre";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    int i;
		    {
		      <CompleteOnName:bre>;
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
 * Test for 'case' keyword.
 */
public void test0025_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    cas
		  }
		}
		""";

	String completeBehind = "cas";
	int cursorLocation = str.lastIndexOf("cas") + completeBehind.length() - 1;

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
/*
 * Test for 'case' keyword.
 */
public void test0025_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    cas
		  }
		}
		""";

	String completeBehind = "cas";
	int cursorLocation = str.lastIndexOf("cas") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:cas>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "cas";
	String expectedReplacedSource = "cas";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:cas>;
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
 * Test for 'case' keyword.
 */
public void test0026_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    switch(0) {
		      cas
		    }
		  }
		}
		""";

	String completeBehind = "cas";
	int cursorLocation = str.lastIndexOf("cas") + completeBehind.length() - 1;

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
/*
 * Test for 'case' keyword.
 */
public void test0026_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    switch(0) {
		      cas
		    }
		  }
		}
		""";

	String completeBehind = "cas";
	int cursorLocation = str.lastIndexOf("cas") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:cas>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "cas";
	String expectedReplacedSource = "cas";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnKeyword:cas>;
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
 * Test for 'catch' keyword.
 */
public void test0027_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		     cat
		  }
		}
		""";

	String completeBehind = "cat";
	int cursorLocation = str.lastIndexOf("cat") + completeBehind.length() - 1;

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
/*
 * Test for 'catch' keyword.
 */
public void test0027_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		     cat
		  }
		}
		""";

	String completeBehind = "cat";
	int cursorLocation = str.lastIndexOf("cat") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:cat>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "cat";
	String expectedReplacedSource = "cat";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:cat>;
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
 * Test for 'catch' keyword.
 */
public void test0028_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    try {
		    } cat
		  }
		}
		""";

	String completeBehind = "cat";
	int cursorLocation = str.lastIndexOf("cat") + completeBehind.length() - 1;

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
/*
 * Test for 'catch' keyword.
 */
public void test0028_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    try {
		    } cat
		  }
		}
		""";

	String completeBehind = "cat";
	int cursorLocation = str.lastIndexOf("cat") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:cat>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "cat";
	String expectedReplacedSource = "cat";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnKeyword:cat>;
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
 * Test for 'class' keyword.
 */
public void test0029(){
	String str =
		"cla";

	String completeBehind = "cla";
	int cursorLocation = str.lastIndexOf("cla") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:cla>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "cla";
	String expectedReplacedSource = "cla";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:cla>;\n";

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
 * Test for 'class' keyword.
 */
public void test0030(){
	String str =
		"public cla";

	String completeBehind = "cla";
	int cursorLocation = str.lastIndexOf("cla") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:cla>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "cla";
	String expectedReplacedSource = "cla";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:cla>;\n";

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
 * Test for 'class' keyword.
 */
public void test0031(){
	String str =
		"public final cla";

	String completeBehind = "cla";
	int cursorLocation = str.lastIndexOf("cla") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:cla>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "cla";
	String expectedReplacedSource = "cla";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:cla>;\n";

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
 * Test for 'class' keyword.
 */
public void test0032(){
	String str =
		"public final cla X";

	String completeBehind = "cla";
	int cursorLocation = str.lastIndexOf("cla") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:cla>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "cla";
	String expectedReplacedSource = "cla";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:cla>;\n";

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
 * Test for 'class' keyword.
 */
public void test0033(){
	String str =
		"""
		public class X {
		  cla
		}""";

	String completeBehind = "cla";
	int cursorLocation = str.lastIndexOf("cla") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:cla>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "cla";
	String expectedReplacedSource = "cla";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:cla>;
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
 * Test for 'class' keyword.
 */
public void test0034(){
	String str =
		"""
		public class X {
		  public cla
		}""";

	String completeBehind = "cla";
	int cursorLocation = str.lastIndexOf("cla") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:cla>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "cla";
	String expectedReplacedSource = "cla";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:cla>;
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
 * Test for 'class' keyword.
 */
public void test0035(){
	String str =
		"""
		public class X {
		  public final cla
		}""";

	String completeBehind = "cla";
	int cursorLocation = str.lastIndexOf("cla") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:cla>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "cla";
	String expectedReplacedSource = "cla";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:cla>;
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
 * Test for 'class' keyword.
 */
public void test0036(){
	String str =
		"""
		public class X {
		  public final cla Y
		}""";

	String completeBehind = "cla";
	int cursorLocation = str.lastIndexOf("cla") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:cla>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "cla";
	String expectedReplacedSource = "cla";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:cla>;
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
 * Test for 'class' keyword.
 */
public void test0037_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    cla
		  }
		}""";

	String completeBehind = "cla";
	int cursorLocation = str.lastIndexOf("cla") + completeBehind.length() - 1;

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
 * Test for 'class' keyword.
 */
public void test0037_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    cla
		  }
		}""";

	String completeBehind = "cla";
	int cursorLocation = str.lastIndexOf("cla") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:cla>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "cla";
	String expectedReplacedSource = "cla";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:cla>;
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
 * Test for 'class' keyword.
 */
public void test0038_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    final cla
		  }
		}""";

	String completeBehind = "cla";
	int cursorLocation = str.lastIndexOf("cla") + completeBehind.length() - 1;

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
 * Test for 'class' keyword.
 */
public void test0038_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    final cla
		  }
		}""";

	String completeBehind = "cla";
	int cursorLocation = str.lastIndexOf("cla") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:cla>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "cla";
	String expectedReplacedSource = "cla";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:cla>;
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
 * Test for 'class' keyword.
 */
public void test0039_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    final cla Y
		  }
		}""";

	String completeBehind = "cla";
	int cursorLocation = str.lastIndexOf("cla") + completeBehind.length() - 1;

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
 * Test for 'class' keyword.
 */
public void test0039_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    final cla Y
		  }
		}""";

	String completeBehind = "cla";
	int cursorLocation = str.lastIndexOf("cla") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:cla>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "cla";
	String expectedReplacedSource = "cla";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:cla>;
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
 * Test for 'continue' keyword.
 */
public void test0040_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		     con
		  }
		}
		""";

	String completeBehind = "con";
	int cursorLocation = str.lastIndexOf("con") + completeBehind.length() - 1;

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
/*
 * Test for 'continue' keyword.
 */
public void test0040_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		     con
		  }
		}
		""";

	String completeBehind = "con";
	int cursorLocation = str.lastIndexOf("con") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:con>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "con";
	String expectedReplacedSource = "con";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:con>;
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
 * Test for 'continue' keyword.
 */
public void test0041_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		     for(int i; i < 5; i++) {
		       con
		     }
		  }
		}
		""";

	String completeBehind = "con";
	int cursorLocation = str.lastIndexOf("con") + completeBehind.length() - 1;

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
/*
 * Test for 'continue' keyword.
 */
public void test0041_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		     for(int i; i < 5; i++) {
		       con
		     }
		  }
		}
		""";

	String completeBehind = "con";
	int cursorLocation = str.lastIndexOf("con") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:con>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "con";
	String expectedReplacedSource = "con";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    int i;
		    {
		      <CompleteOnName:con>;
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
 * Test for 'default' keyword.
 */
public void test0042_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		     def
		  }
		}
		""";

	String completeBehind = "def";
	int cursorLocation = str.lastIndexOf("def") + completeBehind.length() - 1;

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
/*
 * Test for 'default' keyword.
 */
public void test0042_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		     def
		  }
		}
		""";

	String completeBehind = "def";
	int cursorLocation = str.lastIndexOf("def") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:def>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "def";
	String expectedReplacedSource = "def";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:def>;
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
 * Test for 'default' keyword.
 */
public void test0043_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		     switch(0) {
		       case 1 : break;
		       def
		     }
		  }
		}
		""";

	String completeBehind = "def";
	int cursorLocation = str.lastIndexOf("def") + completeBehind.length() - 1;

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
/*
 * Test for 'default' keyword.
 */
public void test0043_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		     switch(0) {
		       case 1 : break;
		       def
		     }
		  }
		}
		""";

	String completeBehind = "def";
	int cursorLocation = str.lastIndexOf("def") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:def>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "def";
	String expectedReplacedSource = "def";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    {
		      <CompleteOnName:def>;
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
 * Test for 'do' keyword.
 */
public void test0044_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		     do
		  }
		}
		""";

	String completeBehind = "do";
	int cursorLocation = str.lastIndexOf("do") + completeBehind.length() - 1;

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
/*
 * Test for 'do' keyword.
 */
public void test0044_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		     do
		  }
		}
		""";

	String completeBehind = "do";
	int cursorLocation = str.lastIndexOf("do") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:do>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "do";
	String expectedReplacedSource = "do";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:do>;
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
 * Test for 'else' keyword.
 */
public void test0045_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		     els
		  }
		}
		""";

	String completeBehind = "els";
	int cursorLocation = str.lastIndexOf("els") + completeBehind.length() - 1;

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
/*
 * Test for 'else' keyword.
 */
public void test0045_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		     els
		  }
		}
		""";

	String completeBehind = "els";
	int cursorLocation = str.lastIndexOf("els") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:els>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "els";
	String expectedReplacedSource = "els";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:els>;
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
 * Test for 'else' keyword.
 */
public void test0046_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		     if(true) {
		     } els
		  }
		}
		""";

	String completeBehind = "els";
	int cursorLocation = str.lastIndexOf("els") + completeBehind.length() - 1;

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
/*
 * Test for 'else' keyword.
 */
public void test0046_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		     if(true) {
		     } els
		  }
		}
		""";

	String completeBehind = "els";
	int cursorLocation = str.lastIndexOf("els") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:els>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "els";
	String expectedReplacedSource = "els";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:els>;
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
 * Test for 'extends' keyword.
 */
public void test0047(){
	String str =
		"ext";

	String completeBehind = "ext";
	int cursorLocation = str.lastIndexOf("ext") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:ext>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ext";
	String expectedReplacedSource = "ext";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:ext>;\n";

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
 * Test for 'extends' keyword.
 */
public void test0048(){
	String str =
		"X ext";

	String completeBehind = "ext";
	int cursorLocation = str.lastIndexOf("ext") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ext";
	String expectedReplacedSource = "ext";
	String expectedUnitDisplayString =
		"";

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
 * Test for 'extends' keyword.
 */
public void test0049(){
	String str =
		"ext Y";

	String completeBehind = "ext";
	int cursorLocation = str.lastIndexOf("ext") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:ext>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ext";
	String expectedReplacedSource = "ext";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:ext>;\n";

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
 * Test for 'extends' keyword.
 */
public void test0050(){
	String str =
		"class X ext";

	String completeBehind = "ext";
	int cursorLocation = str.lastIndexOf("ext") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:ext>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ext";
	String expectedReplacedSource = "ext";
	String expectedUnitDisplayString =
		"""
		class X extends <CompleteOnKeyword:ext> {
		  X() {
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
 * Test for 'extends' keyword.
 */
public void test0051(){
	String str =
		"class X ext Y";

	String completeBehind = "ext";
	int cursorLocation = str.lastIndexOf("ext") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:ext>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ext";
	String expectedReplacedSource = "ext";
	String expectedUnitDisplayString =
		"""
		class X extends <CompleteOnKeyword:ext> {
		  X() {
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
 * Test for 'extends' keyword.
 */
public void test0052(){
	String str =
		"class X ext Y {";

	String completeBehind = "ext";
	int cursorLocation = str.lastIndexOf("ext") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:ext>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ext";
	String expectedReplacedSource = "ext";
	String expectedUnitDisplayString =
		"""
		class X extends <CompleteOnKeyword:ext> {
		  {
		  }
		  X() {
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
 * Test for 'extends' keyword.
 */
public void test0053(){
	String str =
		"class X extends Y ext";

	String completeBehind = "ext";
	int cursorLocation = str.lastIndexOf("ext") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:ext>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ext";
	String expectedReplacedSource = "ext";
	String expectedUnitDisplayString =
		"""
		class X extends <CompleteOnKeyword:ext> {
		  X() {
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
 * Test for 'extends' keyword.
 */
public void test0054(){
	String str =
		"class X implements Y ext";

	String completeBehind = "ext";
	int cursorLocation = str.lastIndexOf("ext") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ext";
	String expectedReplacedSource = "ext";
	String expectedUnitDisplayString =
		"""
		class X implements Y {
		  X() {
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
 * Test for 'finally' keyword.
 */
public void test0055_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		     fin\
		  }
		}
		""";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

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
/*
 * Test for 'finally' keyword.
 */
public void test0055_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		     fin\
		  }
		}
		""";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:fin>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "fin";
	String expectedReplacedSource = "fin";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:fin>;
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
 * Test for 'finally' keyword.
 */
public void test0056_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		     try {\
		     } fin\
		  }
		}
		""";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

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
/*
 * Test for 'finally' keyword.
 */
public void test0056_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		     try {\
		     } fin\
		  }
		}
		""";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:fin>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "fin";
	String expectedReplacedSource = "fin";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnKeyword:fin>;
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
 * Test for 'for' keyword.
 */
public void test0057_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		     for\
		  }
		}
		""";

	String completeBehind = "for";
	int cursorLocation = str.lastIndexOf("for") + completeBehind.length() - 1;

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
/*
 * Test for 'for' keyword.
 */
public void test0057_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		     for\
		  }
		}
		""";

	String completeBehind = "for";
	int cursorLocation = str.lastIndexOf("for") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:for>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "for";
	String expectedReplacedSource = "for";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:for>;
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
 * Test for 'if' keyword.
 */
public void test0058_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		     if\
		  }
		}
		""";

	String completeBehind = "if";
	int cursorLocation = str.lastIndexOf("if") + completeBehind.length() - 1;

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
/*
 * Test for 'if' keyword.
 */
public void test0058_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		     if\
		  }
		}
		""";

	String completeBehind = "if";
	int cursorLocation = str.lastIndexOf("if") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:if>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "if";
	String expectedReplacedSource = "if";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:if>;
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
 * Test for 'switch' keyword.
 */
public void test0059_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		     swi\
		  }
		}
		""";

	String completeBehind = "swi";
	int cursorLocation = str.lastIndexOf("swi") + completeBehind.length() - 1;

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
/*
 * Test for 'switch' keyword.
 */
public void test0059_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		     swi\
		  }
		}
		""";

	String completeBehind = "swi";
	int cursorLocation = str.lastIndexOf("swi") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:swi>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "swi";
	String expectedReplacedSource = "swi";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:swi>;
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
 * Test for 'implements' keyword.
 */
public void test0060(){
	String str =
		"impl";

	String completeBehind = "impl";
	int cursorLocation = str.lastIndexOf("impl") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:impl>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "impl";
	String expectedReplacedSource = "impl";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:impl>;\n";

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
 * Test for 'implements' keyword.
 */
public void test0061(){
	String str =
		"X impl";

	String completeBehind = "impl";
	int cursorLocation = str.lastIndexOf("impl") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "impl";
	String expectedReplacedSource = "impl";
	String expectedUnitDisplayString =
		"";

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
 * Test for 'implements' keyword.
 */
public void test0062(){
	String str =
		"impl Y";

	String completeBehind = "impl";
	int cursorLocation = str.lastIndexOf("impl") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:impl>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "impl";
	String expectedReplacedSource = "impl";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:impl>;\n";

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
 * Test for 'implements' keyword.
 */
public void test0063(){
	String str =
		"class X impl";

	String completeBehind = "impl";
	int cursorLocation = str.lastIndexOf("impl") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:impl>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "impl";
	String expectedReplacedSource = "impl";
	String expectedUnitDisplayString =
		"""
		class X extends <CompleteOnKeyword:impl> {
		  X() {
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
 * Test for 'implements' keyword.
 */
public void test0064(){
	String str =
		"class X impl Y";

	String completeBehind = "impl";
	int cursorLocation = str.lastIndexOf("impl") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:impl>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "impl";
	String expectedReplacedSource = "impl";
	String expectedUnitDisplayString =
		"""
		class X extends <CompleteOnKeyword:impl> {
		  X() {
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
 * Test for 'implements' keyword.
 */
public void test0065(){
	String str =
		"class X impl Y {";

	String completeBehind = "impl";
	int cursorLocation = str.lastIndexOf("impl") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:impl>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "impl";
	String expectedReplacedSource = "impl";
	String expectedUnitDisplayString =
		"""
		class X extends <CompleteOnKeyword:impl> {
		  {
		  }
		  X() {
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
 * Test for 'implements' keyword.
 */
public void test0066(){
	String str =
		"class X extends Y impl";

	String completeBehind = "impl";
	int cursorLocation = str.lastIndexOf("impl") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:impl>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "impl";
	String expectedReplacedSource = "impl";
	String expectedUnitDisplayString =
		"""
		class X extends <CompleteOnKeyword:impl> {
		  X() {
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
 * Test for 'implements' keyword.
 */
public void test0067(){
	String str =
		"class X implements Y impl";

	String completeBehind = "impl";
	int cursorLocation = str.lastIndexOf("impl") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "impl";
	String expectedReplacedSource = "impl";
	String expectedUnitDisplayString =
		"""
		class X implements Y {
		  X() {
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
 * Test for 'import' keyword.
 */
public void test0068(){
	String str =
		"impo";

	String completeBehind = "impo";
	int cursorLocation = str.lastIndexOf("impo") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:impo>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "impo";
	String expectedReplacedSource = "impo";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:impo>;\n";

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
 * Test for 'import' keyword.
 */
public void test0069(){
	String str =
		"package p;\n" +
		"impo";

	String completeBehind = "impo";
	int cursorLocation = str.lastIndexOf("impo") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:impo>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "impo";
	String expectedReplacedSource = "impo";
	String expectedUnitDisplayString =
		"package p;\n" +
		"import <CompleteOnKeyword:impo>;\n";

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
 * Test for 'import' keyword.
 */
public void test0070(){
	String str =
		"""
		package p;
		import p2.Y;
		impo""";

	String completeBehind = "impo";
	int cursorLocation = str.lastIndexOf("impo") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:impo>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "impo";
	String expectedReplacedSource = "impo";
	String expectedUnitDisplayString =
		"""
		package p;
		import p2.Y;
		import <CompleteOnKeyword:impo>;
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
 * Test for 'import' keyword.
 */
public void test0071(){
	String str =
		"impo p2.Y";

	String completeBehind = "impo";
	int cursorLocation = str.lastIndexOf("impo") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:impo>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "impo";
	String expectedReplacedSource = "impo";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:impo>;\n";

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
 * Test for 'interface' keyword.
 */
public void test0072(){
	String str =
		"int";

	String completeBehind = "int";
	int cursorLocation = str.lastIndexOf("int") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:int>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "int";
	String expectedReplacedSource = "int";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:int>;\n";

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
 * Test for 'interface' keyword.
 */
public void test0073(){
	String str =
		"public int";

	String completeBehind = "int";
	int cursorLocation = str.lastIndexOf("int") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:int>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "int";
	String expectedReplacedSource = "int";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:int>;\n";

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
 * Test for 'interface' keyword.
 */
public void test0074(){
	String str =
		"public abstract int";

	String completeBehind = "int";
	int cursorLocation = str.lastIndexOf("int") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:int>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "int";
	String expectedReplacedSource = "int";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:int>;\n";

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
 * Test for 'interface' keyword.
 */
public void test0075(){
	String str =
		"public abstract int X";

	String completeBehind = "int";
	int cursorLocation = str.lastIndexOf("int") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:int>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "int";
	String expectedReplacedSource = "int";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:int>;\n";

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
 * Test for 'interface' keyword.
 */
public void test0076(){
	String str =
		"""
		public class X {
		  int
		}""";

	String completeBehind = "int";
	int cursorLocation = str.lastIndexOf("int") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:int>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "int";
	String expectedReplacedSource = "int";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:int>;
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
 * Test for 'interface' keyword.
 */
public void test0077(){
	String str =
		"""
		public class X {
		  public int
		}""";

	String completeBehind = "int";
	int cursorLocation = str.lastIndexOf("int") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:int>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "int";
	String expectedReplacedSource = "int";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:int>;
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
 * Test for 'interface' keyword.
 */
public void test0078(){
	String str =
		"""
		public class X {
		  public abstract int
		}""";

	String completeBehind = "int";
	int cursorLocation = str.lastIndexOf("int") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:int>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "int";
	String expectedReplacedSource = "int";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:int>;
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
 * Test for 'interface' keyword.
 */
public void test0079(){
	String str =
		"""
		public class X {
		  public abstract int Y
		}""";

	String completeBehind = "int";
	int cursorLocation = str.lastIndexOf("int") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:int>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "int";
	String expectedReplacedSource = "int";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:int>;
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
 * Test for 'interface' keyword.
 */
public void test0080_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    int
		  }
		}""";

	String completeBehind = "int";
	int cursorLocation = str.lastIndexOf("int") + completeBehind.length() - 1;

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
 * Test for 'interface' keyword.
 */
public void test0080_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    int
		  }
		}""";

	String completeBehind = "int";
	int cursorLocation = str.lastIndexOf("int") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:int>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "int";
	String expectedReplacedSource = "int";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:int>;
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
 * Test for 'interface' keyword.
 */
public void test0081_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    abstract int
		  }
		}""";

	String completeBehind = "int";
	int cursorLocation = str.lastIndexOf("int") + completeBehind.length() - 1;

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
 * Test for 'interface' keyword.
 */
public void test0081_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    abstract int
		  }
		}""";

	String completeBehind = "int";
	int cursorLocation = str.lastIndexOf("int") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:int>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "int";
	String expectedReplacedSource = "int";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:int>;
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
 * Test for 'interface' keyword.
 */
public void test0082_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    abstract int Y
		  }
		}""";

	String completeBehind = "int";
	int cursorLocation = str.lastIndexOf("int") + completeBehind.length() - 1;

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
 * Test for 'interface' keyword.
 */
public void test0082_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    abstract int Y
		  }
		}""";

	String completeBehind = "int";
	int cursorLocation = str.lastIndexOf("int") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:int>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "int";
	String expectedReplacedSource = "int";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:int>;
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
 * Test for 'interface' keyword.
 */
public void test0083(){
	String str =
		"public final int";

	String completeBehind = "int";
	int cursorLocation = str.lastIndexOf("int") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:int>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "int";
	String expectedReplacedSource = "int";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:int>;\n";

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
 * Test for 'interface' keyword.
 */
public void test0084(){
	String str =
		"public final int X";

	String completeBehind = "int";
	int cursorLocation = str.lastIndexOf("int") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:int>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "int";
	String expectedReplacedSource = "int";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:int>;\n";

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
 * Test for 'package' keyword.
 */
public void test0085(){
	String str =
		"pac";

	String completeBehind = "pac";
	int cursorLocation = str.lastIndexOf("pac") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:pac>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pac";
	String expectedReplacedSource = "pac";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:pac>;\n";

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
 * Test for 'package' keyword.
 */
public void test0086(){
	String str =
		"pac p";

	String completeBehind = "pac";
	int cursorLocation = str.lastIndexOf("pac") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:pac>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pac";
	String expectedReplacedSource = "pac";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:pac>;\n";

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
 * Test for 'package' keyword.
 */
public void test0087(){
	String str =
		"package p;" +
		"pac";

	String completeBehind = "pac";
	int cursorLocation = str.lastIndexOf("pac") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:pac>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pac";
	String expectedReplacedSource = "pac";
	String expectedUnitDisplayString =
		"package p;\n" +
		"import <CompleteOnKeyword:pac>;\n";

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
 * Test for 'package' keyword.
 */
public void test0088(){
	String str =
		"import p;" +
		"pac";

	String completeBehind = "pac";
	int cursorLocation = str.lastIndexOf("pac") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:pac>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pac";
	String expectedReplacedSource = "pac";
	String expectedUnitDisplayString =
		"import p;\n" +
		"import <CompleteOnKeyword:pac>;\n";

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
 * Test for 'package' keyword.
 */
public void test0089(){
	String str =
		"class X {}" +
		"pac";

	String completeBehind = "pac";
	int cursorLocation = str.lastIndexOf("pac") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:pac>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pac";
	String expectedReplacedSource = "pac";
	String expectedUnitDisplayString =
		"""
		import <CompleteOnKeyword:pac>;
		class X {
		  X() {
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
 * Test for 'return' keyword.
 */
public void test0090_Diet(){
	String str =
		"""
		public class X {
		  int foo() {
		    ret
		  }
		}""";

	String completeBehind = "ret";
	int cursorLocation = str.lastIndexOf("ret") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  int foo() {
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
 * Test for 'return' keyword.
 */
public void test0090_Method(){
	String str =
		"""
		public class X {
		  int foo() {
		    ret
		  }
		}""";

	String completeBehind = "ret";
	int cursorLocation = str.lastIndexOf("ret") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:ret>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ret";
	String expectedReplacedSource = "ret";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  int foo() {
		    <CompleteOnName:ret>;
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
 * Test for 'throw' keyword.
 */
public void test0091_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    thr
		  }
		}""";

	String completeBehind = "thr";
	int cursorLocation = str.lastIndexOf("thr") + completeBehind.length() - 1;

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
 * Test for 'throw' keyword.
 */
public void test0091_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    thr
		  }
		}""";

	String completeBehind = "thr";
	int cursorLocation = str.lastIndexOf("thr") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:thr>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "thr";
	String expectedReplacedSource = "thr";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:thr>;
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
 * Test for 'try' keyword.
 */
public void test0092_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    try
		  }
		}""";

	String completeBehind = "try";
	int cursorLocation = str.lastIndexOf("try") + completeBehind.length() - 1;

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
 * Test for 'try' keyword.
 */
public void test0092_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    try
		  }
		}""";

	String completeBehind = "try";
	int cursorLocation = str.lastIndexOf("try") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:try>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "try";
	String expectedReplacedSource = "try";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:try>;
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
 * Test for 'try' keyword.
 */
public void test0093_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    if(try
		  }
		}""";

	String completeBehind = "try";
	int cursorLocation = str.lastIndexOf("try") + completeBehind.length() - 1;

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
 * Test for 'try' keyword.
 */
public void test0093_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    if(try
		  }
		}""";

	String completeBehind = "try";
	int cursorLocation = str.lastIndexOf("try") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:try>";
	String expectedParentNodeToString = "if (<CompleteOnName:try>)\n    ;";
	String completionIdentifier = "try";
	String expectedReplacedSource = "try";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    if (<CompleteOnName:try>)
		        ;
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
 * Test for 'do' keyword.
 */
public void test0094_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    if(do
		  }
		}""";

	String completeBehind = "do";
	int cursorLocation = str.lastIndexOf("do") + completeBehind.length() - 1;

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
 * Test for 'do' keyword.
 */
public void test0094_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    if(do
		  }
		}""";

	String completeBehind = "do";
	int cursorLocation = str.lastIndexOf("do") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:do>";
	String expectedParentNodeToString = "if (<CompleteOnName:do>)\n    ;";
	String completionIdentifier = "do";
	String expectedReplacedSource = "do";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    if (<CompleteOnName:do>)
		        ;
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
 * Test for 'for' keyword.
 */
public void test0095_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    if(for
		  }
		}""";

	String completeBehind = "for";
	int cursorLocation = str.lastIndexOf("for") + completeBehind.length() - 1;

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
 * Test for 'for' keyword.
 */
public void test0095_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    if(for
		  }
		}""";

	String completeBehind = "for";
	int cursorLocation = str.lastIndexOf("for") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:for>";
	String expectedParentNodeToString = "if (<CompleteOnName:for>)\n    ;";
	String completionIdentifier = "for";
	String expectedReplacedSource = "for";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    if (<CompleteOnName:for>)
		        ;
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
 * Test for 'if' keyword.
 */
public void test0096_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    if(if
		  }
		}""";

	String completeBehind = "if";
	int cursorLocation = str.lastIndexOf("if") + completeBehind.length() - 1;

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
 * Test for 'if' keyword.
 */
public void test0096_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    if(if
		  }
		}""";

	String completeBehind = "if";
	int cursorLocation = str.lastIndexOf("if") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:if>";
	String expectedParentNodeToString = "if (<CompleteOnName:if>)\n    ;";
	String completionIdentifier = "if";
	String expectedReplacedSource = "if";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    if (<CompleteOnName:if>)
		        ;
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
 * Test for 'switch' keyword.
 */
public void test0097_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    if(swi
		  }
		}""";

	String completeBehind = "swi";
	int cursorLocation = str.lastIndexOf("swi") + completeBehind.length() - 1;

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
 * Test for 'switch' keyword.
 */
public void test0097_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    if(swi
		  }
		}""";

	String completeBehind = "swi";
	int cursorLocation = str.lastIndexOf("swi") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:swi>";
	String expectedParentNodeToString = "if (<CompleteOnName:swi>)\n    ;";
	String completionIdentifier = "swi";
	String expectedReplacedSource = "swi";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    if (<CompleteOnName:swi>)
		        ;
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
 * Test for 'new' keyword.
 */
public void test0098_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    new
		  }
		}""";

	String completeBehind = "new";
	int cursorLocation = str.lastIndexOf("new") + completeBehind.length() - 1;

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
 * Test for 'new' keyword.
 */
public void test0098_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    new
		  }
		}""";

	String completeBehind = "new";
	int cursorLocation = str.lastIndexOf("new") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:new>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "new";
	String expectedReplacedSource = "new";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:new>;
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
 * Test for 'new' keyword.
 */
public void test0099_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    new X
		  }
		}""";

	String completeBehind = "new";
	int cursorLocation = str.lastIndexOf("new") + completeBehind.length() - 1;

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
 * Test for 'new' keyword.
 */
public void test0099_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    new X
		  }
		}""";

	String completeBehind = "new";
	int cursorLocation = str.lastIndexOf("new") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:new>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "new";
	String expectedReplacedSource = "new";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:new>;
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
 * Test for 'new' keyword.
 */
public void test0100_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    new X()
		  }
		}""";

	String completeBehind = "new";
	int cursorLocation = str.lastIndexOf("new") + completeBehind.length() - 1;

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
 * Test for 'new' keyword.
 */
public void test0100_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    new X()
		  }
		}""";

	String completeBehind = "new";
	int cursorLocation = str.lastIndexOf("new") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:new>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "new";
	String expectedReplacedSource = "new";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:new>;
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
 * Test for 'throws' keyword.
 */
public void test0101(){
	String str =
		"""
		public class X {
		  void foo() thr
		  }
		}""";

	String completeBehind = "thr";
	int cursorLocation = str.lastIndexOf("thr") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:thr>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "thr";
	String expectedReplacedSource = "thr";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() throws <CompleteOnKeyword:thr> {
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
 * Test for 'throws' keyword.
 */
public void test0102(){
	String str =
		"""
		public class X {
		  void foo() thr {
		  }
		}""";

	String completeBehind = "thr";
	int cursorLocation = str.lastIndexOf("thr") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:thr>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "thr";
	String expectedReplacedSource = "thr";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() throws <CompleteOnKeyword:thr> {
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
 * Test for 'throws' keyword.
 */
public void test0103(){
	String str =
		"""
		public class X {
		  void foo() thr E {
		  }
		}""";

	String completeBehind = "thr";
	int cursorLocation = str.lastIndexOf("thr") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:thr>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "thr";
	String expectedReplacedSource = "thr";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() throws <CompleteOnKeyword:thr> {
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
 * Test for 'throws' keyword.
 */
public void test0104(){
	String str =
		"""
		public class X {
		  void foo() throws E thr
		  }
		}""";

	String completeBehind = "thr";
	int cursorLocation = str.lastIndexOf("thr") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "thr";
	String expectedReplacedSource = "thr";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() throws E {
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
 * Test for 'throws' keyword.
 */
public void test0105(){
	String str =
		"""
		public class X {
		  X() thr
		  }
		}""";

	String completeBehind = "thr";
	int cursorLocation = str.lastIndexOf("thr") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:thr>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "thr";
	String expectedReplacedSource = "thr";
	String expectedUnitDisplayString =
		"""
		public class X {
		  X() throws <CompleteOnKeyword:thr> {
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
 * Test for 'throws' keyword.
 */
public void test0106(){
	String str =
		"""
		public class X {
		  int foo()[] thr
		  }
		}""";

	String completeBehind = "thr";
	int cursorLocation = str.lastIndexOf("thr") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:thr>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "thr";
	String expectedReplacedSource = "thr";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  int[] foo() throws <CompleteOnKeyword:thr> {
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
 * Test for 'while' keyword.
 */
public void test0107_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    whi
		  }
		}""";

	String completeBehind = "whi";
	int cursorLocation = str.lastIndexOf("whi") + completeBehind.length() - 1;

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
 * Test for 'while' keyword.
 */
public void test0107_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    whi
		  }
		}""";

	String completeBehind = "whi";
	int cursorLocation = str.lastIndexOf("whi") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:whi>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "whi";
	String expectedReplacedSource = "whi";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:whi>;
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
 * Test for 'while' keyword.
 */
public void test0108_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    if(whi
		  }
		}""";

	String completeBehind = "whi";
	int cursorLocation = str.lastIndexOf("whi") + completeBehind.length() - 1;

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
 * Test for 'while' keyword.
 */
public void test0108_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    if(whi
		  }
		}""";

	String completeBehind = "whi";
	int cursorLocation = str.lastIndexOf("whi") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:whi>";
	String expectedParentNodeToString = "if (<CompleteOnName:whi>)\n    ;";
	String completionIdentifier = "whi";
	String expectedReplacedSource = "whi";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    if (<CompleteOnName:whi>)
		        ;
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
 * Test for 'assert' keyword.
 */
public void test0109_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    ass
		  }
		}""";

	String completeBehind = "ass";
	int cursorLocation = str.lastIndexOf("ass") + completeBehind.length() - 1;

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
 * Test for 'assert' keyword.
 */
public void test0109_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    ass
		  }
		}""";

	String completeBehind = "ass";
	int cursorLocation = str.lastIndexOf("ass") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:ass>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ass";
	String expectedReplacedSource = "ass";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:ass>;
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
 * Test for 'assert' keyword.
 */
public void test0110_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    if(ass
		  }
		}""";

	String completeBehind = "ass";
	int cursorLocation = str.lastIndexOf("ass") + completeBehind.length() - 1;

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
 * Test for 'assert' keyword.
 */
public void test0110_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    if(ass
		  }
		}""";

	String completeBehind = "ass";
	int cursorLocation = str.lastIndexOf("ass") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:ass>";
	String expectedParentNodeToString = "if (<CompleteOnName:ass>)\n    ;";
	String completionIdentifier = "ass";
	String expectedReplacedSource = "ass";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    if (<CompleteOnName:ass>)
		        ;
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
 * Test for 'final' keyword.
 */
public void test0111(){
	String str =
		"fin";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:fin>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "fin";
	String expectedReplacedSource = "fin";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:fin>;\n";

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
 * Test for 'final' keyword.
 */
public void test0112(){
	String str =
		"public fin";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:fin>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "fin";
	String expectedReplacedSource = "fin";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:fin>;\n";

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
 * Test for 'final' keyword.
 */
public void test0113(){
	String str =
		"fin zzz";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:fin>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "fin";
	String expectedReplacedSource = "fin";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:fin>;\n";

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
 * Test for 'final' keyword.
 */
public void test0114(){
	String str =
		"final fin";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:fin>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "fin";
	String expectedReplacedSource = "fin";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:fin>;\n";

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
 * Test for 'final' keyword.
 */
public void test0115(){
	String str =
		"abstract fin";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:fin>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "fin";
	String expectedReplacedSource = "fin";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:fin>;\n";

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
 * Test for 'final' keyword.
 */
public void test0116(){
	String str =
		"public fin class X {}";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:fin>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "fin";
	String expectedReplacedSource = "fin";
	String expectedUnitDisplayString =
		"""
		import <CompleteOnKeyword:fin>;
		class X {
		  X() {
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
 * Test for 'final' keyword.
 */
public void test0117(){
	String str =
		"""
		public class X {
		  fin
		}""";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:fin>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "fin";
	String expectedReplacedSource = "fin";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:fin>;
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
 * Test for 'final' keyword.
 */
public void test0118(){
	String str =
		"""
		public class X {
		  public fin
		}""";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:fin>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "fin";
	String expectedReplacedSource = "fin";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:fin>;
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
 * Test for 'final' keyword.
 */
public void test0119(){
	String str =
		"""
		public class X {
		  fin zzz
		}""";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:fin>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "fin";
	String expectedReplacedSource = "fin";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:fin>;
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
 * Test for 'final' keyword.
 */
public void test0120(){
	String str =
		"""
		public class X {
		  final fin
		}""";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:fin>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "fin";
	String expectedReplacedSource = "fin";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:fin>;
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
 * Test for 'final' keyword.
 */
public void test0121(){
	String str =
		"""
		public class X {
		  abstract fin
		}""";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:fin>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "fin";
	String expectedReplacedSource = "fin";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:fin>;
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
 * Test for 'native' keyword.
 */
public void test0122(){
	String str =
		"""
		public class X {
		  nat
		}""";

	String completeBehind = "nat";
	int cursorLocation = str.lastIndexOf("nat") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:nat>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "nat";
	String expectedReplacedSource = "nat";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:nat>;
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
 * Test for 'native' keyword.
 */
public void test0123(){
	String str =
		"""
		public class X {
		  public nat
		}""";

	String completeBehind = "nat";
	int cursorLocation = str.lastIndexOf("nat") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:nat>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "nat";
	String expectedReplacedSource = "nat";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:nat>;
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
 * Test for 'native' keyword.
 */
public void test0124(){
	String str =
		"""
		public class X {
		  transient nat
		}""";

	String completeBehind = "nat";
	int cursorLocation = str.lastIndexOf("nat") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:nat>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "nat";
	String expectedReplacedSource = "nat";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:nat>;
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
 * Test for 'native' keyword.
 */
public void test0125(){
	String str =
		"""
		public class X {
		  transient nat
		}""";

	String completeBehind = "nat";
	int cursorLocation = str.lastIndexOf("nat") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:nat>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "nat";
	String expectedReplacedSource = "nat";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:nat>;
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
 * Test for 'native' keyword.
 */
public void test0126(){
	String str =
		"""
		public class X {
		  volatile nat
		}""";

	String completeBehind = "nat";
	int cursorLocation = str.lastIndexOf("nat") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:nat>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "nat";
	String expectedReplacedSource = "nat";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:nat>;
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
 * Test for 'strictfp' keyword.
 */
public void test0127(){
	String str =
		"""
		public class X {
		  str
		}""";

	String completeBehind = "str";
	int cursorLocation = str.lastIndexOf("str") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:str>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "str";
	String expectedReplacedSource = "str";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:str>;
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
 * Test for 'strictfp' keyword.
 */
public void test0128(){
	String str =
		"""
		public class X {
		  public str
		}""";

	String completeBehind = "str";
	int cursorLocation = str.lastIndexOf("str") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:str>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "str";
	String expectedReplacedSource = "str";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:str>;
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
 * Test for 'strictfp' keyword.
 */
public void test0129(){
	String str =
		"""
		public class X {
		  transient str
		}""";

	String completeBehind = "str";
	int cursorLocation = str.lastIndexOf("str") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:str>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "str";
	String expectedReplacedSource = "str";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:str>;
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
 * Test for 'strictfp' keyword.
 */
public void test0130(){
	String str =
		"""
		public class X {
		  transient str
		}""";

	String completeBehind = "str";
	int cursorLocation = str.lastIndexOf("str") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:str>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "str";
	String expectedReplacedSource = "str";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:str>;
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
 * Test for 'strictfp' keyword.
 */
public void test0131(){
	String str =
		"""
		public class X {
		  volatile str
		}""";

	String completeBehind = "str";
	int cursorLocation = str.lastIndexOf("str") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:str>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "str";
	String expectedReplacedSource = "str";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:str>;
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
 * Test for 'volatile' keyword.
 */
public void test0132(){
	String str =
		"""
		public class X {
		  vol
		}""";

	String completeBehind = "vol";
	int cursorLocation = str.lastIndexOf("vol") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:vol>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "vol";
	String expectedReplacedSource = "vol";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:vol>;
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
 * Test for 'volatile' keyword.
 */
public void test0133(){
	String str =
		"""
		public class X {
		  public vol
		}""";

	String completeBehind = "vol";
	int cursorLocation = str.lastIndexOf("vol") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:vol>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "vol";
	String expectedReplacedSource = "vol";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:vol>;
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
 * Test for 'volatile' keyword.
 */
public void test0134(){
	String str =
		"""
		public class X {
		  transient vol
		}""";

	String completeBehind = "vol";
	int cursorLocation = str.lastIndexOf("vol") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:vol>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "vol";
	String expectedReplacedSource = "vol";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:vol>;
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
 * Test for 'volatile' keyword.
 */
public void test0135(){
	String str =
		"""
		public class X {
		  volatile vol
		}""";

	String completeBehind = "vol";
	int cursorLocation = str.lastIndexOf("vol") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:vol>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "vol";
	String expectedReplacedSource = "vol";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:vol>;
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
 * Test for 'volatile' keyword.
 */
public void test0136(){
	String str =
		"""
		public class X {
		  native vol
		}""";

	String completeBehind = "vol";
	int cursorLocation = str.lastIndexOf("vol") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:vol>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "vol";
	String expectedReplacedSource = "vol";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:vol>;
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
 * Test for 'transient' keyword.
 */
public void test0137(){
	String str =
		"""
		public class X {
		  tra
		}""";

	String completeBehind = "tra";
	int cursorLocation = str.lastIndexOf("tra") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:tra>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "tra";
	String expectedReplacedSource = "tra";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:tra>;
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
 * Test for 'transient' keyword.
 */
public void test0138(){
	String str =
		"""
		public class X {
		  public tra
		}""";

	String completeBehind = "tra";
	int cursorLocation = str.lastIndexOf("tra") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:tra>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "tra";
	String expectedReplacedSource = "tra";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:tra>;
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
 * Test for 'transient' keyword.
 */
public void test0139(){
	String str =
		"""
		public class X {
		  transient tra
		}""";

	String completeBehind = "tra";
	int cursorLocation = str.lastIndexOf("tra") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:tra>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "tra";
	String expectedReplacedSource = "tra";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:tra>;
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
 * Test for 'transient' keyword.
 */
public void test0140(){
	String str =
		"""
		public class X {
		  volatile tra
		}""";

	String completeBehind = "tra";
	int cursorLocation = str.lastIndexOf("tra") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:tra>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "tra";
	String expectedReplacedSource = "tra";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:tra>;
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
 * Test for 'transient' keyword.
 */
public void test0141(){
	String str =
		"""
		public class X {
		  native tra
		}""";

	String completeBehind = "tra";
	int cursorLocation = str.lastIndexOf("tra") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:tra>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "tra";
	String expectedReplacedSource = "tra";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:tra>;
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
 * Test for 'synchronized' keyword.
 */
public void test0142(){
	String str =
		"""
		public class X {
		  syn
		}""";

	String completeBehind = "syn";
	int cursorLocation = str.lastIndexOf("syn") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:syn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "syn";
	String expectedReplacedSource = "syn";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:syn>;
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
 * Test for 'synchronized' keyword.
 */
public void test0143(){
	String str =
		"""
		public class X {
		  public syn
		}""";

	String completeBehind = "syn";
	int cursorLocation = str.lastIndexOf("syn") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:syn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "syn";
	String expectedReplacedSource = "syn";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:syn>;
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
 * Test for 'synchronized' keyword.
 */
public void test0144(){
	String str =
		"""
		public class X {
		  transient syn
		}""";

	String completeBehind = "syn";
	int cursorLocation = str.lastIndexOf("syn") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:syn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "syn";
	String expectedReplacedSource = "syn";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:syn>;
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
 * Test for 'synchronized' keyword.
 */
public void test0145(){
	String str =
		"""
		public class X {
		  transient syn
		}""";

	String completeBehind = "syn";
	int cursorLocation = str.lastIndexOf("syn") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:syn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "syn";
	String expectedReplacedSource = "syn";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:syn>;
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
 * Test for 'synchronized' keyword.
 */
public void test0146(){
	String str =
		"""
		public class X {
		  volatile syn
		}""";

	String completeBehind = "syn";
	int cursorLocation = str.lastIndexOf("syn") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:syn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "syn";
	String expectedReplacedSource = "syn";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:syn>;
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
 * Test for 'synchronized' keyword.
 */
public void test0147_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    syn
		  }
		}""";

	String completeBehind = "syn";
	int cursorLocation = str.lastIndexOf("syn") + completeBehind.length() - 1;

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
 * Test for 'synchronized' keyword.
 */
public void test0147_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    syn
		  }
		}""";

	String completeBehind = "syn";
	int cursorLocation = str.lastIndexOf("syn") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:syn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "syn";
	String expectedReplacedSource = "syn";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:syn>;
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
 * Test for 'synchronized' keyword.
 */
public void test0148_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    if(syn
		  }
		}""";

	String completeBehind = "syn";
	int cursorLocation = str.lastIndexOf("syn") + completeBehind.length() - 1;

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
 * Test for 'synchronized' keyword.
 */
public void test0148_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    if(syn
		  }
		}""";

	String completeBehind = "syn";
	int cursorLocation = str.lastIndexOf("syn") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:syn>";
	String expectedParentNodeToString = "if (<CompleteOnName:syn>)\n    ;";
	String completionIdentifier = "syn";
	String expectedReplacedSource = "syn";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    if (<CompleteOnName:syn>)
		        ;
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
 * Test for 'static' keyword.
 */
public void test0149(){
	String str =
		"""
		public class X {
		  sta
		}""";

	String completeBehind = "sta";
	int cursorLocation = str.lastIndexOf("sta") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:sta>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "sta";
	String expectedReplacedSource = "sta";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:sta>;
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
 * Test for 'static' keyword.
 */
public void test0150(){
	String str =
		"""
		public class X {
		  public sta
		}""";

	String completeBehind = "sta";
	int cursorLocation = str.lastIndexOf("sta") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:sta>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "sta";
	String expectedReplacedSource = "sta";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:sta>;
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
 * Test for 'public' keyword.
 */
public void test0151(){
	String str =
		"pub";

	String completeBehind = "pub";
	int cursorLocation = str.lastIndexOf("pub") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:pub>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pub";
	String expectedReplacedSource = "pub";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:pub>;\n";

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
 * Test for 'public' keyword.
 */
public void test0152(){
	String str =
		"final pub";

	String completeBehind = "pub";
	int cursorLocation = str.lastIndexOf("pub") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:pub>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pub";
	String expectedReplacedSource = "pub";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:pub>;\n";

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
 * Test for 'public' keyword.
 */
public void test0153(){
	String str =
		"public pub";

	String completeBehind = "pub";
	int cursorLocation = str.lastIndexOf("pub") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:pub>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pub";
	String expectedReplacedSource = "pub";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:pub>;\n";

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
 * Test for 'public' keyword.
 */
public void test0154(){
	String str =
		"private pub";

	String completeBehind = "pub";
	int cursorLocation = str.lastIndexOf("pub") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:pub>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pub";
	String expectedReplacedSource = "pub";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:pub>;\n";

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
 * Test for 'public' keyword.
 */
public void test0155(){
	String str =
		"public class X{}\n" +
		"pub";

	String completeBehind = "pub";
	int cursorLocation = str.lastIndexOf("pub") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:pub>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pub";
	String expectedReplacedSource = "pub";
	String expectedUnitDisplayString =
		"""
		import <CompleteOnKeyword:pub>;
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
/*
 * Test for 'public' keyword.
 */
public void test0156(){
	String str =
		"""
		public class X{
		  pub
		}""";

	String completeBehind = "pub";
	int cursorLocation = str.lastIndexOf("pub") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pub>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pub";
	String expectedReplacedSource = "pub";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:pub>;
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
 * Test for 'public' keyword.
 */
public void test0157(){
	String str =
		"""
		public class X{
		  public pub
		}""";

	String completeBehind = "pub";
	int cursorLocation = str.lastIndexOf("pub") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pub>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pub";
	String expectedReplacedSource = "pub";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:pub>;
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
 * Test for 'public' keyword.
 */
public void test0158(){
	String str =
		"""
		public class X{
		  private pub
		}""";

	String completeBehind = "pub";
	int cursorLocation = str.lastIndexOf("pub") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pub>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pub";
	String expectedReplacedSource = "pub";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:pub>;
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
 * Test for 'public' keyword.
 */
public void test0159(){
	String str =
		"""
		public class X{
		  protected pub
		}""";

	String completeBehind = "pub";
	int cursorLocation = str.lastIndexOf("pub") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pub>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pub";
	String expectedReplacedSource = "pub";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:pub>;
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
 * Test for 'public' keyword.
 */
public void test0160(){
	String str =
		"""
		public class X{
		  abstract pub
		}""";

	String completeBehind = "pub";
	int cursorLocation = str.lastIndexOf("pub") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pub>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pub";
	String expectedReplacedSource = "pub";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:pub>;
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
 * Test for 'protected' keyword.
 */
public void test0161(){
	String str =
		"pro";

	String completeBehind = "pro";
	int cursorLocation = str.lastIndexOf("") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"";

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
 * Test for 'protected' keyword.
 */
public void test0162(){
	String str =
		"final pro";

	String completeBehind = "pro";
	int cursorLocation = str.lastIndexOf("pro") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:pro>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pro";
	String expectedReplacedSource = "pro";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:pro>;\n";

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
 * Test for 'protected' keyword.
 */
public void test0163(){
	String str =
		"public pro";

	String completeBehind = "pro";
	int cursorLocation = str.lastIndexOf("pro") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:pro>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pro";
	String expectedReplacedSource = "pro";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:pro>;\n";

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
 * Test for 'protected' keyword.
 */
public void test0164(){
	String str =
		"private pro";

	String completeBehind = "pro";
	int cursorLocation = str.lastIndexOf("pro") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:pro>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pro";
	String expectedReplacedSource = "pro";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:pro>;\n";

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
 * Test for 'protected' keyword.
 */
public void test0165(){
	String str =
		"public class X{}\n" +
		"pro";

	String completeBehind = "pro";
	int cursorLocation = str.lastIndexOf("pro") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:pro>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pro";
	String expectedReplacedSource = "pro";
	String expectedUnitDisplayString =
		"""
		import <CompleteOnKeyword:pro>;
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
/*
 * Test for 'protected' keyword.
 */
public void test0166(){
	String str =
		"""
		public class X{
		  pro
		}""";

	String completeBehind = "pro";
	int cursorLocation = str.lastIndexOf("pro") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pro>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pro";
	String expectedReplacedSource = "pro";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:pro>;
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
 * Test for 'protected' keyword.
 */
public void test0167(){
	String str =
		"""
		public class X{
		  public pro
		}""";

	String completeBehind = "pro";
	int cursorLocation = str.lastIndexOf("pro") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pro>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pro";
	String expectedReplacedSource = "pro";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:pro>;
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
 * Test for 'protected' keyword.
 */
public void test0168(){
	String str =
		"""
		public class X{
		  private pro
		}""";

	String completeBehind = "pro";
	int cursorLocation = str.lastIndexOf("pro") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pro>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pro";
	String expectedReplacedSource = "pro";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:pro>;
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
 * Test for 'protected' keyword.
 */
public void test0169(){
	String str =
		"""
		public class X{
		  protected pro
		}""";

	String completeBehind = "pro";
	int cursorLocation = str.lastIndexOf("pro") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pro>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pro";
	String expectedReplacedSource = "pro";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:pro>;
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
 * Test for 'protected' keyword.
 */
public void test0170(){
	String str =
		"""
		public class X{
		  abstract pro
		}""";

	String completeBehind = "pro";
	int cursorLocation = str.lastIndexOf("pro") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pro>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pro";
	String expectedReplacedSource = "pro";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:pro>;
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
 * Test for 'private' keyword.
 */
public void test0171(){
	String str =
		"pri";

	String completeBehind = "pri";
	int cursorLocation = str.lastIndexOf("") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"";

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
 * Test for 'private' keyword.
 */
public void test0172(){
	String str =
		"final pri";

	String completeBehind = "pri";
	int cursorLocation = str.lastIndexOf("pri") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:pri>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pri";
	String expectedReplacedSource = "pri";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:pri>;\n";

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
 * Test for 'private' keyword.
 */
public void test0173(){
	String str =
		"public pri";

	String completeBehind = "pri";
	int cursorLocation = str.lastIndexOf("pri") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:pri>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pri";
	String expectedReplacedSource = "pri";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:pri>;\n";

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
 * Test for 'private' keyword.
 */
public void test0174(){
	String str =
		"private pri";

	String completeBehind = "pri";
	int cursorLocation = str.lastIndexOf("pri") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:pri>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pri";
	String expectedReplacedSource = "pri";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:pri>;\n";

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
 * Test for 'private' keyword.
 */
public void test0175(){
	String str =
		"public class X{}\n" +
		"pri";

	String completeBehind = "pri";
	int cursorLocation = str.lastIndexOf("pri") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:pri>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pri";
	String expectedReplacedSource = "pri";
	String expectedUnitDisplayString =
		"""
		import <CompleteOnKeyword:pri>;
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
/*
 * Test for 'private' keyword.
 */
public void test0176(){
	String str =
		"""
		public class X{
		  pri
		}""";

	String completeBehind = "pri";
	int cursorLocation = str.lastIndexOf("pri") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pri>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pri";
	String expectedReplacedSource = "pri";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:pri>;
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
 * Test for 'private' keyword.
 */
public void test0177(){
	String str =
		"""
		public class X{
		  public pri
		}""";

	String completeBehind = "pri";
	int cursorLocation = str.lastIndexOf("pri") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pri>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pri";
	String expectedReplacedSource = "pri";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:pri>;
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
 * Test for 'private' keyword.
 */
public void test0178(){
	String str =
		"""
		public class X{
		  private pri
		}""";

	String completeBehind = "pri";
	int cursorLocation = str.lastIndexOf("pri") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pri>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pri";
	String expectedReplacedSource = "pri";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:pri>;
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
 * Test for 'private' keyword.
 */
public void test0179(){
	String str =
		"""
		public class X{
		  protected pri
		}""";

	String completeBehind = "pri";
	int cursorLocation = str.lastIndexOf("pri") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pri>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pri";
	String expectedReplacedSource = "pri";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:pri>;
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
 * Test for 'private' keyword.
 */
public void test0180(){
	String str =
		"""
		public class X{
		  abstract pri
		}""";

	String completeBehind = "pri";
	int cursorLocation = str.lastIndexOf("pri") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pri>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pri";
	String expectedReplacedSource = "pri";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:pri>;
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
 * Test for 'super' keyword.
 */
public void test0181_Diet(){
	String str =
		"""
		public class X {
		  void foo(){
		     sup
		  }
		}
		""";

	String completeBehind = "sup";
	int cursorLocation = str.lastIndexOf("sup") + completeBehind.length() - 1;

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
 * Test for 'super' keyword.
 */
public void test0181_Method(){
	String str =
		"""
		public class X {
		  void foo(){
		     sup
		  }
		}
		""";

	String completeBehind = "sup";
	int cursorLocation = str.lastIndexOf("sup") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:sup>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "sup";
	String expectedReplacedSource = "sup";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:sup>;
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
 * Test for 'this' keyword.
 */
public void test0182_Diet(){
	String str =
		"""
		public class X {
		  void foo(){
		     thi
		  }
		}
		""";

	String completeBehind = "thi";
	int cursorLocation = str.lastIndexOf("thi") + completeBehind.length() - 1;

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
 * Test for 'this' keyword.
 */
public void test0182_Method(){
	String str =
		"""
		public class X {
		  void foo(){
		     thi
		  }
		}
		""";

	String completeBehind = "thi";
	int cursorLocation = str.lastIndexOf("thi") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:thi>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "thi";
	String expectedReplacedSource = "thi";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:thi>;
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
 * Test for 'true' keyword.
 */
public void test0183_Diet(){
	String str =
		"""
		public class X {
		  void foo(){
		     tru
		  }
		}
		""";

	String completeBehind = "tru";
	int cursorLocation = str.lastIndexOf("tru") + completeBehind.length() - 1;

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
 * Test for 'true' keyword.
 */
public void test0183_Method(){
	String str =
		"""
		public class X {
		  void foo(){
		     tru
		  }
		}
		""";

	String completeBehind = "tru";
	int cursorLocation = str.lastIndexOf("tru") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:tru>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "tru";
	String expectedReplacedSource = "tru";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:tru>;
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
 * Test for 'false' keyword.
 */
public void test0184_Diet(){
	String str =
		"""
		public class X {
		  void foo(){
		     fal
		  }
		}
		""";

	String completeBehind = "fal";
	int cursorLocation = str.lastIndexOf("fal") + completeBehind.length() - 1;

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
 * Test for 'false' keyword.
 */
public void test0184_Method(){
	String str =
		"""
		public class X {
		  void foo(){
		     fal
		  }
		}
		""";

	String completeBehind = "fal";
	int cursorLocation = str.lastIndexOf("fal") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:fal>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "fal";
	String expectedReplacedSource = "fal";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:fal>;
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
 * Test for 'null' keyword.
 */
public void test0185_Diet(){
	String str =
		"""
		public class X {
		  void foo(){
		     nul
		  }
		}
		""";

	String completeBehind = "nul";
	int cursorLocation = str.lastIndexOf("nul") + completeBehind.length() - 1;

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
 * Test for 'null' keyword.
 */
public void test0185_Method(){
	String str =
		"""
		public class X {
		  void foo(){
		     nul
		  }
		}
		""";

	String completeBehind = "nul";
	int cursorLocation = str.lastIndexOf("nul") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:nul>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "nul";
	String expectedReplacedSource = "nul";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:nul>;
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
 * Test for 'instanceof' keyword.
 */
public void test0186_Diet(){
	String str =
		"""
		public class X {
		  void foo(){
		     if(zzz ins
		  }
		}
		""";

	String completeBehind = "ins";
	int cursorLocation = str.lastIndexOf("ins") + completeBehind.length() - 1;

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
 * Test for 'instanceof' keyword.
 */
public void test0186_Method(){
	String str =
		"""
		public class X {
		  void foo(){
		     if(zzz ins
		  }
		}
		""";

	String completeBehind = "ins";
	int cursorLocation = str.lastIndexOf("ins") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:ins>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ins";
	String expectedReplacedSource = "ins";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnKeyword:ins>;
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
 * Test for 'instanceof' keyword.
 */
public void test0187_Diet(){
	String str =
		"""
		public class X {
		  void foo(){
		     ins
		  }
		}
		""";

	String completeBehind = "ins";
	int cursorLocation = str.lastIndexOf("ins") + completeBehind.length() - 1;

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
 * Test for 'instanceof' keyword.
 */
public void test0187_Method(){
	String str =
		"""
		public class X {
		  void foo(){
		     ins
		  }
		}
		""";

	String completeBehind = "ins";
	int cursorLocation = str.lastIndexOf("ins") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:ins>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ins";
	String expectedReplacedSource = "ins";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:ins>;
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
 * Test for 'instanceof' keyword.
 */
public void test0188_Diet(){
	String str =
		"""
		public class X {
		  void foo(){
		     if(zzz zzz ins
		  }
		}
		""";

	String completeBehind = "ins";
	int cursorLocation = str.lastIndexOf("ins") + completeBehind.length() - 1;

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
 * Test for 'instanceof' keyword.
 */
public void test0188_Method(){
	String str =
		"""
		public class X {
		  void foo(){
		     if(zzz zzz ins
		  }
		}
		""";

	String completeBehind = "ins";
	int cursorLocation = str.lastIndexOf("ins") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:ins>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ins";
	String expectedReplacedSource = "ins";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    zzz zzz;
		    <CompleteOnName:ins>;
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
 * Test for 'while' keyword.
 */
public void test0189_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    do{
		    } whi
		  }
		}""";

	String completeBehind = "whi";
	int cursorLocation = str.lastIndexOf("whi") + completeBehind.length() - 1;

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
 * Test for 'while' keyword.
 */
public void test0189_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    do{
		    } whi
		  }
		}""";

	String completeBehind = "whi";
	int cursorLocation = str.lastIndexOf("whi") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:whi>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "whi";
	String expectedReplacedSource = "whi";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnKeyword:whi>;
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
 * Test for 'catch' keyword.
 */
public void test0190_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    try {
		    } catch(E e) {
		    } cat
		  }
		}
		""";

	String completeBehind = "cat";
	int cursorLocation = str.lastIndexOf("cat") + completeBehind.length() - 1;

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
/*
 * Test for 'catch' keyword.
 */
public void test0190_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    try {
		    } catch(E e) {
		    } cat
		  }
		}
		""";

	String completeBehind = "cat";
	int cursorLocation = str.lastIndexOf("cat") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:cat>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "cat";
	String expectedReplacedSource = "cat";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:cat>;
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
 * Test for 'finally' keyword.
 */
public void test0191_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		     try {\
		     } catch(E e) {\
		     } fin\
		  }
		}
		""";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

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
/*
 * Test for 'finally' keyword.
 */
public void test0191_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		     try {\
		     } catch(E e) {\
		     } fin\
		  }
		}
		""";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:fin>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "fin";
	String expectedReplacedSource = "fin";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:fin>;
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
 * Test for 'finally' keyword.
 */
public void test0192_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		     try {\
		     } finally {\
		     } fin\
		  }
		}
		""";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

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
/*
 * Test for 'finally' keyword.
 */
public void test0192_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		     try {\
		     } finally {\
		     } fin\
		  }
		}
		""";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:fin>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "fin";
	String expectedReplacedSource = "fin";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:fin>;
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
 * Test for 'this' keyword.
 */
public void test0193_Diet(){
	String str =
		"""
		public class X {
		  void foo(){
		     X.thi
		  }
		}
		""";

	String completeBehind = "thi";
	int cursorLocation = str.lastIndexOf("thi") + completeBehind.length() - 1;

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
 * Test for 'this' keyword.
 */
public void test0193_Method(){
	String str =
		"""
		public class X {
		  void foo(){
		     X.thi
		  }
		}
		""";

	String completeBehind = "thi";
	int cursorLocation = str.lastIndexOf("thi") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:X.thi>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "thi";
	String expectedReplacedSource = "X.thi";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:X.thi>;
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
/////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////
/*
 * Test for 'abstract' keyword.
 */
public void test0194(){
	String str =
		"#\n" +
		"abst";

	String completeBehind = "abst";
	int cursorLocation = str.indexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:abst>;\n";

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
 * Test for 'abstract' keyword.
 */
public void test0195(){
	String str =
		"#\n" +
		"abst zzz";

	String completeBehind = "abst";
	int cursorLocation = str.indexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:abst>;\n";

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
 * Test for 'abstract' keyword.
 */
public void test0196(){
	String str =
		"""
		#
		package p;
		abst""";

	String completeBehind = "abst";
	int cursorLocation = str.indexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"package p;\n" +
		"import <CompleteOnKeyword:abst>;\n";

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
 * Test for 'abstract' keyword.
 */
public void test0197(){
	String str =
		"""
		#
		package p;
		abst zzz""";

	String completeBehind = "abst";
	int cursorLocation = str.indexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"package p;\n" +
		"import <CompleteOnKeyword:abst>;\n";

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
 * Test for 'abstract' keyword.
 */
public void test0198(){
	String str =
		"""
		#
		package p;
		import yyy;
		abst""";

	String completeBehind = "abst";
	int cursorLocation = str.indexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"""
		package p;
		import yyy;
		import <CompleteOnKeyword:abst>;
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
 * Test for 'abstract' keyword.
 */
public void test0199(){
	String str =
		"""
		#
		package p;
		import yyy;
		abst zzz""";

	String completeBehind = "abst";
	int cursorLocation = str.indexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"""
		package p;
		import yyy;
		import <CompleteOnKeyword:abst>;
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
 * Test for 'abstract' keyword.
 */
public void test0200(){
	String str =
		"""
		#
		package p;
		import yyy;
		public abst""";

	String completeBehind = "abst";
	int cursorLocation = str.indexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"""
		package p;
		import yyy;
		import <CompleteOnKeyword:abst>;
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
 * Test for 'abstract' keyword.
 */
public void test0201(){
	String str =
		"""
		#
		package p;
		import yyy;
		public abst zzz""";

	String completeBehind = "abst";
	int cursorLocation = str.indexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"""
		package p;
		import yyy;
		import <CompleteOnKeyword:abst>;
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
 * Test for 'abstract' keyword.
 */
public void test0202(){
	String str =
		"""
		#
		package p;
		import yyy;
		abstract abst""";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"""
		package p;
		import yyy;
		import <CompleteOnKeyword:abst>;
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
 * Test for 'abstract' keyword.
 */
public void test0203(){
	String str =
		"""
		#
		package p;
		import yyy;
		abstract abst zzz""";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"""
		package p;
		import yyy;
		import <CompleteOnKeyword:abst>;
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
 * Test for 'abstract' keyword.
 */
public void test0204(){
	String str =
		"""
		#
		package p;
		import\s
		abst""";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnImport:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"package p;\n" +
		"import <CompleteOnImport:abst>;\n";

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
 * Test for 'abstract' keyword.
 */
public void test0205(){
	String str =
		"""
		#
		package p;
		import yyy;
		public class X {}
		abst""";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"""
		package p;
		import yyy;
		import <CompleteOnKeyword:abst>;
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
/*
 * Test for 'abstract' keyword.
 */
public void test0206(){
	String str =
		"""
		#
		package p;
		import yyy;
		public class X {}
		abst zzz""";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"""
		package p;
		import yyy;
		import <CompleteOnKeyword:abst>;
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
/*
 * Test for 'abstract' keyword.
 */
public void test0207(){
	String str =
		"""
		#
		package p;
		import yyy;
		final abst""";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"""
		package p;
		import yyy;
		import <CompleteOnKeyword:abst>;
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
 * Test for 'abstract' keyword.
 */
public void test0208(){
	String str =
		"""
		#
		package p;
		public class X {
		  abst
		}
		""";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  <CompleteOnType:abst>;
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
 * Test for 'abstract' keyword.
 */
public void test0209(){
	String str =
		"""
		#
		package p;
		public class X {
		  abst zzz
		}
		""";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:abst>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  <CompleteOnType:abst>;
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
 * Test for 'abstract' keyword.
 */
public void test0210(){
	String str =
		"""
		#
		package p;
		public class X {
		  public abst zzz
		}
		""";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:abst>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  <CompleteOnType:abst>;
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
 * Test for 'abstract' keyword.
 */
public void test0211(){
	String str =
		"""
		#
		package p;
		public class X {
		  final abst
		}
		""";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  <CompleteOnType:abst>;
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
 * Test for 'abstract' keyword.
 */
public void test0212(){
	String str =
		"""
		#
		package p;
		public class X {
		  abstract abst
		}
		""";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  <CompleteOnType:abst>;
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
 * Test for 'abstract' keyword.
 */
public void test0213(){
	String str =
		"""
		#
		package p;
		public class X {
		  static abst
		}
		""";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  <CompleteOnType:abst>;
		  public X() {
		  }
		  <clinit>() {
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
 * Test for 'abstract' keyword.
 */
public void test0214_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    abst
		  }
		}
		""";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

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
/*
 * Test for 'abstract' keyword.
 */
public void test0214_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    abst
		  }
		}
		""";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:abst>;
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
 * Test for 'abstract' keyword.
 */
public void test0216_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    abst zzz
		  }
		}
		""";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

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
/*
 * Test for 'abstract' keyword.
 */
public void test0216_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    abst zzz
		  }
		}
		""";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:abst>;
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
 * Test for 'break' keyword.
 */
public void test0217_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    bre
		  }
		}
		""";

	String completeBehind = "bre";
	int cursorLocation = str.lastIndexOf("bre") + completeBehind.length() - 1;

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
/*
 * Test for 'break' keyword.
 */
public void test0217_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    bre
		  }
		}
		""";

	String completeBehind = "bre";
	int cursorLocation = str.lastIndexOf("bre") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:bre>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "bre";
	String expectedReplacedSource = "bre";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:bre>;
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
 * Test for 'break' keyword.
 */
public void test0218_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    for(int i; i < 10; i++) {
		      bre
		    }
		  }
		}
		""";

	String completeBehind = "bre";
	int cursorLocation = str.lastIndexOf("bre") + completeBehind.length() - 1;

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
/*
 * Test for 'break' keyword.
 */
public void test0218_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    for(int i; i < 10; i++) {
		      bre
		    }
		  }
		}
		""";

	String completeBehind = "bre";
	int cursorLocation = str.lastIndexOf("bre") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:bre>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "bre";
	String expectedReplacedSource = "bre";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    int i;
		    {
		      <CompleteOnName:bre>;
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
 * Test for 'case' keyword.
 */
public void test0219_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    cas
		  }
		}
		""";

	String completeBehind = "cas";
	int cursorLocation = str.lastIndexOf("cas") + completeBehind.length() - 1;

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
/*
 * Test for 'case' keyword.
 */
public void test0219_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    cas
		  }
		}
		""";

	String completeBehind = "cas";
	int cursorLocation = str.lastIndexOf("cas") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:cas>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "cas";
	String expectedReplacedSource = "cas";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:cas>;
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
 * Test for 'case' keyword.
 */
public void test0220_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    switch(0) {
		      cas
		    }
		  }
		}
		""";

	String completeBehind = "cas";
	int cursorLocation = str.lastIndexOf("cas") + completeBehind.length() - 1;

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
/*
 * Test for 'case' keyword.
 */
public void test0220_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    switch(0) {
		      cas
		    }
		  }
		}
		""";

	String completeBehind = "cas";
	int cursorLocation = str.lastIndexOf("cas") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:cas>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "cas";
	String expectedReplacedSource = "cas";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    {
		      <CompleteOnKeyword:cas>;
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
 * Test for 'catch' keyword.
 */
public void test0221_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    cat
		  }
		}
		""";

	String completeBehind = "cat";
	int cursorLocation = str.lastIndexOf("cat") + completeBehind.length() - 1;

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
/*
 * Test for 'catch' keyword.
 */
public void test0221_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    cat
		  }
		}
		""";

	String completeBehind = "cat";
	int cursorLocation = str.lastIndexOf("cat") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:cat>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "cat";
	String expectedReplacedSource = "cat";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:cat>;
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
 * Test for 'catch' keyword.
 */
public void test0222_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    try {
		    } cat
		  }
		}
		""";

	String completeBehind = "cat";
	int cursorLocation = str.lastIndexOf("cat") + completeBehind.length() - 1;

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
/*
 * Test for 'catch' keyword.
 */
public void test0222_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    try {
		    } cat
		  }
		}
		""";

	String completeBehind = "cat";
	int cursorLocation = str.lastIndexOf("cat") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:cat>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "cat";
	String expectedReplacedSource = "cat";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnKeyword:cat>;
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
 * Test for 'class' keyword.
 */
public void test0223(){
	String str =
		"#\n" +
		"cla";

	String completeBehind = "cla";
	int cursorLocation = str.lastIndexOf("cla") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:cla>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "cla";
	String expectedReplacedSource = "cla";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:cla>;\n";

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
 * Test for 'class' keyword.
 */
public void test0224(){
	String str =
		"#\n" +
		"public cla";

	String completeBehind = "cla";
	int cursorLocation = str.lastIndexOf("cla") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:cla>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "cla";
	String expectedReplacedSource = "cla";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:cla>;\n";

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
 * Test for 'class' keyword.
 */
public void test0225(){
	String str =
		"#\n" +
		"public final cla";

	String completeBehind = "cla";
	int cursorLocation = str.lastIndexOf("cla") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:cla>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "cla";
	String expectedReplacedSource = "cla";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:cla>;\n";

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
 * Test for 'class' keyword.
 */
public void test0226(){
	String str =
		"#\n" +
		"public final cla X";

	String completeBehind = "cla";
	int cursorLocation = str.lastIndexOf("cla") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:cla>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "cla";
	String expectedReplacedSource = "cla";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:cla>;\n";

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
 * Test for 'class' keyword.
 */
public void test0227(){
	String str =
		"""
		#
		public class X {
		  cla
		}""";

	String completeBehind = "cla";
	int cursorLocation = str.lastIndexOf("cla") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:cla>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "cla";
	String expectedReplacedSource = "cla";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:cla>;
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
 * Test for 'class' keyword.
 */
public void test0228(){
	String str =
		"""
		#
		public class X {
		  public cla
		}""";

	String completeBehind = "cla";
	int cursorLocation = str.lastIndexOf("cla") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:cla>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "cla";
	String expectedReplacedSource = "cla";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:cla>;
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
 * Test for 'class' keyword.
 */
public void test0229(){
	String str =
		"""
		#
		public class X {
		  public final cla
		}""";

	String completeBehind = "cla";
	int cursorLocation = str.lastIndexOf("cla") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:cla>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "cla";
	String expectedReplacedSource = "cla";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:cla>;
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
 * Test for 'class' keyword.
 */
public void test0230(){
	String str =
		"""
		#
		public class X {
		  public final cla Y
		}""";

	String completeBehind = "cla";
	int cursorLocation = str.lastIndexOf("cla") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:cla>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "cla";
	String expectedReplacedSource = "cla";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:cla>;
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
 * Test for 'class' keyword.
 */
public void test0231_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    cla
		  }
		}""";

	String completeBehind = "cla";
	int cursorLocation = str.lastIndexOf("cla") + completeBehind.length() - 1;

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
 * Test for 'class' keyword.
 */
public void test0231_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    cla
		  }
		}""";

	String completeBehind = "cla";
	int cursorLocation = str.lastIndexOf("cla") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:cla>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "cla";
	String expectedReplacedSource = "cla";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:cla>;
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
 * Test for 'class' keyword.
 */
public void test0232_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    final cla
		  }
		}""";

	String completeBehind = "cla";
	int cursorLocation = str.lastIndexOf("cla") + completeBehind.length() - 1;

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
 * Test for 'class' keyword.
 */
public void test0232_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    final cla
		  }
		}""";

	String completeBehind = "cla";
	int cursorLocation = str.lastIndexOf("cla") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:cla>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "cla";
	String expectedReplacedSource = "cla";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:cla>;
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
 * Test for 'class' keyword.
 */
public void test0233_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    final cla Y
		  }
		}""";

	String completeBehind = "cla";
	int cursorLocation = str.lastIndexOf("cla") + completeBehind.length() - 1;

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
 * Test for 'class' keyword.
 */
public void test0233_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    final cla Y
		  }
		}""";

	String completeBehind = "cla";
	int cursorLocation = str.lastIndexOf("cla") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:cla>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "cla";
	String expectedReplacedSource = "cla";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:cla>;
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
 * Test for 'continue' keyword.
 */
public void test0234_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    con
		  }
		}
		""";

	String completeBehind = "con";
	int cursorLocation = str.lastIndexOf("con") + completeBehind.length() - 1;

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
/*
 * Test for 'continue' keyword.
 */
public void test0234_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    con
		  }
		}
		""";

	String completeBehind = "con";
	int cursorLocation = str.lastIndexOf("con") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:con>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "con";
	String expectedReplacedSource = "con";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:con>;
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
 * Test for 'continue' keyword.
 */
public void test0235_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    for(int i; i < 5; i++) {
		      con
		    }
		  }
		}
		""";

	String completeBehind = "con";
	int cursorLocation = str.lastIndexOf("con") + completeBehind.length() - 1;

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
/*
 * Test for 'continue' keyword.
 */
public void test0235_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    for(int i; i < 5; i++) {
		      con
		    }
		  }
		}
		""";

	String completeBehind = "con";
	int cursorLocation = str.lastIndexOf("con") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:con>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "con";
	String expectedReplacedSource = "con";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    int i;
		    {
		      <CompleteOnName:con>;
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
 * Test for 'default' keyword.
 */
public void test0236_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    def
		  }
		}
		""";

	String completeBehind = "def";
	int cursorLocation = str.lastIndexOf("def") + completeBehind.length() - 1;

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
/*
 * Test for 'default' keyword.
 */
public void test0236_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    def
		  }
		}
		""";

	String completeBehind = "def";
	int cursorLocation = str.lastIndexOf("def") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:def>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "def";
	String expectedReplacedSource = "def";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:def>;
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
 * Test for 'default' keyword.
 */
public void test0237_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    switch(0) {
		      case 1 : break;
		      def
		    }
		  }
		}
		""";

	String completeBehind = "def";
	int cursorLocation = str.lastIndexOf("def") + completeBehind.length() - 1;

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
/*
 * Test for 'default' keyword.
 */
public void test0237_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    switch(0) {
		      case 1 : break;
		      def
		    }
		  }
		}
		""";

	String completeBehind = "def";
	int cursorLocation = str.lastIndexOf("def") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:def>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "def";
	String expectedReplacedSource = "def";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    {
		      <CompleteOnName:def>;
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
 * Test for 'do' keyword.
 */
public void test0238_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    do
		  }
		}
		""";

	String completeBehind = "do";
	int cursorLocation = str.lastIndexOf("do") + completeBehind.length() - 1;

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
/*
 * Test for 'do' keyword.
 */
public void test0238_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    do
		  }
		}
		""";

	String completeBehind = "do";
	int cursorLocation = str.lastIndexOf("do") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:do>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "do";
	String expectedReplacedSource = "do";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:do>;
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
 * Test for 'else' keyword.
 */
public void test0239_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    els
		  }
		}
		""";

	String completeBehind = "els";
	int cursorLocation = str.lastIndexOf("els") + completeBehind.length() - 1;

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
/*
 * Test for 'else' keyword.
 */
public void test0239_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    els
		  }
		}
		""";

	String completeBehind = "els";
	int cursorLocation = str.lastIndexOf("els") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:els>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "els";
	String expectedReplacedSource = "els";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:els>;
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
 * Test for 'else' keyword.
 */
public void test0240_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    if(true) {
		    } els
		  }
		}
		""";

	String completeBehind = "els";
	int cursorLocation = str.lastIndexOf("els") + completeBehind.length() - 1;

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
/*
 * Test for 'else' keyword.
 */
public void test0240_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    if(true) {
		    } els
		  }
		}
		""";

	String completeBehind = "els";
	int cursorLocation = str.lastIndexOf("els") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:els>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "els";
	String expectedReplacedSource = "els";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:els>;
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
 * Test for 'extends' keyword.
 */
public void test0241(){
	String str =
		"#\n" +
		"ext";

	String completeBehind = "ext";
	int cursorLocation = str.lastIndexOf("ext") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:ext>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ext";
	String expectedReplacedSource = "ext";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:ext>;\n";

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
 * Test for 'extends' keyword.
 */
public void test0242(){
	String str =
		"#\n" +
		"X ext";

	String completeBehind = "ext";
	int cursorLocation = str.lastIndexOf("ext") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ext";
	String expectedReplacedSource = "ext";
	String expectedUnitDisplayString =
		"";

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
 * Test for 'extends' keyword.
 */
public void test0243(){
	String str =
		"#\n" +
		"ext Y";

	String completeBehind = "ext";
	int cursorLocation = str.lastIndexOf("ext") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:ext>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ext";
	String expectedReplacedSource = "ext";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:ext>;\n";

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
 * Test for 'extends' keyword.
 */
public void test0244(){
	String str =
		"#\n" +
		"class X ext";

	String completeBehind = "ext";
	int cursorLocation = str.lastIndexOf("ext") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:ext>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ext";
	String expectedReplacedSource = "ext";
	String expectedUnitDisplayString =
		"""
		class X extends <CompleteOnKeyword:ext> {
		  X() {
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
 * Test for 'extends' keyword.
 */
public void test0245(){
	String str =
		"#\n" +
		"class X ext Y";

	String completeBehind = "ext";
	int cursorLocation = str.lastIndexOf("ext") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:ext>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ext";
	String expectedReplacedSource = "ext";
	String expectedUnitDisplayString =
		"""
		class X extends <CompleteOnKeyword:ext> {
		  X() {
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
 * Test for 'extends' keyword.
 */
public void test0246(){
	String str =
		"#\n" +
		"class X ext Y {";

	String completeBehind = "ext";
	int cursorLocation = str.lastIndexOf("ext") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:ext>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ext";
	String expectedReplacedSource = "ext";
	String expectedUnitDisplayString =
		"""
		class X extends <CompleteOnKeyword:ext> {
		  {
		  }
		  X() {
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
 * Test for 'extends' keyword.
 */
public void test0247(){
	String str =
		"#\n" +
		"class X extends Y ext";

	String completeBehind = "ext";
	int cursorLocation = str.lastIndexOf("ext") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:ext>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ext";
	String expectedReplacedSource = "ext";
	String expectedUnitDisplayString =
		"""
		class X extends <CompleteOnKeyword:ext> {
		  X() {
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
 * Test for 'extends' keyword.
 */
public void test0248(){
	String str =
		"#\n" +
		"class X implements Y ext";

	String completeBehind = "ext";
	int cursorLocation = str.lastIndexOf("ext") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ext";
	String expectedReplacedSource = "ext";
	String expectedUnitDisplayString =
		"""
		class X implements Y {
		  X() {
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
 * Test for 'finally' keyword.
 */
public void test0249_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    fin\
		  }
		}
		""";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

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
/*
 * Test for 'finally' keyword.
 */
public void test0249_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    fin\
		  }
		}
		""";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:fin>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "fin";
	String expectedReplacedSource = "fin";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:fin>;
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
 * Test for 'finally' keyword.
 */
public void test0250_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    try {\
		    } fin\
		  }
		}
		""";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

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
/*
 * Test for 'finally' keyword.
 */
public void test0250_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    try {\
		    } fin\
		  }
		}
		""";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:fin>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "fin";
	String expectedReplacedSource = "fin";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnKeyword:fin>;
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
 * Test for 'for' keyword.
 */
public void test0251_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    for\
		  }
		}
		""";

	String completeBehind = "for";
	int cursorLocation = str.lastIndexOf("for") + completeBehind.length() - 1;

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
/*
 * Test for 'for' keyword.
 */
public void test0251_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    for\
		  }
		}
		""";

	String completeBehind = "for";
	int cursorLocation = str.lastIndexOf("for") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:for>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "for";
	String expectedReplacedSource = "for";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:for>;
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
 * Test for 'if' keyword.
 */
public void test0252_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    if\
		  }
		}
		""";

	String completeBehind = "if";
	int cursorLocation = str.lastIndexOf("if") + completeBehind.length() - 1;

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
/*
 * Test for 'if' keyword.
 */
public void test0252_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    if\
		  }
		}
		""";

	String completeBehind = "if";
	int cursorLocation = str.lastIndexOf("if") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:if>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "if";
	String expectedReplacedSource = "if";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:if>;
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
 * Test for 'switch' keyword.
 */
public void test0253_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    swi\
		  }
		}
		""";

	String completeBehind = "swi";
	int cursorLocation = str.lastIndexOf("swi") + completeBehind.length() - 1;

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
/*
 * Test for 'switch' keyword.
 */
public void test0253_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    swi\
		  }
		}
		""";

	String completeBehind = "swi";
	int cursorLocation = str.lastIndexOf("swi") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:swi>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "swi";
	String expectedReplacedSource = "swi";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:swi>;
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
 * Test for 'implements' keyword.
 */
public void test0254(){
	String str =
		"#\n" +
		"impl";

	String completeBehind = "impl";
	int cursorLocation = str.lastIndexOf("impl") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:impl>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "impl";
	String expectedReplacedSource = "impl";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:impl>;\n";

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
 * Test for 'implements' keyword.
 */
public void test0255(){
	String str =
		"#\n" +
		"X impl";

	String completeBehind = "impl";
	int cursorLocation = str.lastIndexOf("impl") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "impl";
	String expectedReplacedSource = "impl";
	String expectedUnitDisplayString =
		"";

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
 * Test for 'implements' keyword.
 */
public void test0256(){
	String str =
		"#\n" +
		"impl Y";

	String completeBehind = "impl";
	int cursorLocation = str.lastIndexOf("impl") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:impl>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "impl";
	String expectedReplacedSource = "impl";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:impl>;\n";

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
 * Test for 'implements' keyword.
 */
public void test0257(){
	String str =
		"#\n" +
		"class X impl";

	String completeBehind = "impl";
	int cursorLocation = str.lastIndexOf("impl") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:impl>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "impl";
	String expectedReplacedSource = "impl";
	String expectedUnitDisplayString =
		"""
		class X extends <CompleteOnKeyword:impl> {
		  X() {
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
 * Test for 'implements' keyword.
 */
public void test0258(){
	String str =
		"#\n" +
		"class X impl Y";

	String completeBehind = "impl";
	int cursorLocation = str.lastIndexOf("impl") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:impl>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "impl";
	String expectedReplacedSource = "impl";
	String expectedUnitDisplayString =
		"""
		class X extends <CompleteOnKeyword:impl> {
		  X() {
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
 * Test for 'implements' keyword.
 */
public void test0259(){
	String str =
		"#\n" +
		"class X impl Y {";

	String completeBehind = "impl";
	int cursorLocation = str.lastIndexOf("impl") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:impl>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "impl";
	String expectedReplacedSource = "impl";
	String expectedUnitDisplayString =
		"""
		class X extends <CompleteOnKeyword:impl> {
		  {
		  }
		  X() {
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
 * Test for 'implements' keyword.
 */
public void test0260(){
	String str =
		"#\n" +
		"class X extends Y impl";

	String completeBehind = "impl";
	int cursorLocation = str.lastIndexOf("impl") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:impl>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "impl";
	String expectedReplacedSource = "impl";
	String expectedUnitDisplayString =
		"""
		class X extends <CompleteOnKeyword:impl> {
		  X() {
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
 * Test for 'implements' keyword.
 */
public void test0261(){
	String str =
		"#\n" +
		"class X implements Y impl";

	String completeBehind = "impl";
	int cursorLocation = str.lastIndexOf("impl") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "impl";
	String expectedReplacedSource = "impl";
	String expectedUnitDisplayString =
		"""
		class X implements Y {
		  X() {
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
 * Test for 'import' keyword.
 */
public void test0262(){
	String str =
		"#\n" +
		"impo";

	String completeBehind = "impo";
	int cursorLocation = str.lastIndexOf("impo") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:impo>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "impo";
	String expectedReplacedSource = "impo";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:impo>;\n";

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
 * Test for 'import' keyword.
 */
public void test0263(){
	String str =
		"""
		#
		package p;
		impo""";

	String completeBehind = "impo";
	int cursorLocation = str.lastIndexOf("impo") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:impo>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "impo";
	String expectedReplacedSource = "impo";
	String expectedUnitDisplayString =
		"package p;\n" +
		"import <CompleteOnKeyword:impo>;\n";

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
 * Test for 'import' keyword.
 */
public void test0264(){
	String str =
		"""
		#
		package p;
		import p2.Y;
		impo""";

	String completeBehind = "impo";
	int cursorLocation = str.lastIndexOf("impo") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:impo>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "impo";
	String expectedReplacedSource = "impo";
	String expectedUnitDisplayString =
		"""
		package p;
		import p2.Y;
		import <CompleteOnKeyword:impo>;
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
 * Test for 'import' keyword.
 */
public void test0265(){
	String str =
		"#\n" +
		"impo p2.Y";

	String completeBehind = "impo";
	int cursorLocation = str.lastIndexOf("impo") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:impo>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "impo";
	String expectedReplacedSource = "impo";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:impo>;\n";

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
 * Test for 'interface' keyword.
 */
public void test0266(){
	String str =
		"#\n" +
		"int";

	String completeBehind = "int";
	int cursorLocation = str.lastIndexOf("int") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:int>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "int";
	String expectedReplacedSource = "int";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:int>;\n";

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
 * Test for 'interface' keyword.
 */
public void test0267(){
	String str =
		"#\n" +
		"public int";

	String completeBehind = "int";
	int cursorLocation = str.lastIndexOf("int") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:int>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "int";
	String expectedReplacedSource = "int";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:int>;\n";

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
 * Test for 'interface' keyword.
 */
public void test0268(){
	String str =
		"#\n" +
		"public abstract int";

	String completeBehind = "int";
	int cursorLocation = str.lastIndexOf("int") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:int>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "int";
	String expectedReplacedSource = "int";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:int>;\n";

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
 * Test for 'interface' keyword.
 */
public void test0269(){
	String str =
		"#\n" +
		"public abstract int X";

	String completeBehind = "int";
	int cursorLocation = str.lastIndexOf("int") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:int>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "int";
	String expectedReplacedSource = "int";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:int>;\n";

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
 * Test for 'interface' keyword.
 */
public void test0270(){
	String str =
		"""
		#
		public class X {
		  int
		}""";

	String completeBehind = "int";
	int cursorLocation = str.lastIndexOf("int") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:int>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "int";
	String expectedReplacedSource = "int";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:int>;
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
 * Test for 'interface' keyword.
 */
public void test0271(){
	String str =
		"""
		#
		public class X {
		  public int
		}""";

	String completeBehind = "int";
	int cursorLocation = str.lastIndexOf("int") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:int>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "int";
	String expectedReplacedSource = "int";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:int>;
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
 * Test for 'interface' keyword.
 */
public void test0272(){
	String str =
		"""
		#
		public class X {
		  public abstract int
		}""";

	String completeBehind = "int";
	int cursorLocation = str.lastIndexOf("int") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:int>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "int";
	String expectedReplacedSource = "int";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:int>;
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
 * Test for 'interface' keyword.
 */
public void test0273(){
	String str =
		"""
		#
		public class X {
		  public abstract int Y
		}""";

	String completeBehind = "int";
	int cursorLocation = str.lastIndexOf("int") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:int>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "int";
	String expectedReplacedSource = "int";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:int>;
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
 * Test for 'interface' keyword.
 */
public void test0274_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    int
		  }
		}""";

	String completeBehind = "int";
	int cursorLocation = str.lastIndexOf("int") + completeBehind.length() - 1;

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
 * Test for 'interface' keyword.
 */
public void test0274_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    int
		  }
		}""";

	String completeBehind = "int";
	int cursorLocation = str.lastIndexOf("int") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:int>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "int";
	String expectedReplacedSource = "int";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:int>;
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
 * Test for 'interface' keyword.
 */
public void test0275_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    abstract int
		  }
		}""";

	String completeBehind = "int";
	int cursorLocation = str.lastIndexOf("int") + completeBehind.length() - 1;

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
 * Test for 'interface' keyword.
 */
public void test0275_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    abstract int
		  }
		}""";

	String completeBehind = "int";
	int cursorLocation = str.lastIndexOf("int") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:int>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "int";
	String expectedReplacedSource = "int";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:int>;
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
 * Test for 'interface' keyword.
 */
public void test0276_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    abstract int Y
		  }
		}""";

	String completeBehind = "int";
	int cursorLocation = str.lastIndexOf("int") + completeBehind.length() - 1;

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
 * Test for 'interface' keyword.
 */
public void test0276_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    abstract int Y
		  }
		}""";

	String completeBehind = "int";
	int cursorLocation = str.lastIndexOf("int") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:int>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "int";
	String expectedReplacedSource = "int";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:int>;
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
 * Test for 'interface' keyword.
 */
public void test0277(){
	String str =
		"#\n" +
		"public final int";

	String completeBehind = "int";
	int cursorLocation = str.lastIndexOf("int") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:int>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "int";
	String expectedReplacedSource = "int";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:int>;\n";

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
 * Test for 'interface' keyword.
 */
public void test0278(){
	String str =
		"#\n" +
		"public final int X";

	String completeBehind = "int";
	int cursorLocation = str.lastIndexOf("int") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:int>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "int";
	String expectedReplacedSource = "int";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:int>;\n";

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
 * Test for 'package' keyword.
 */
public void test0279(){
	String str =
		"#\n" +
		"pac";

	String completeBehind = "pac";
	int cursorLocation = str.lastIndexOf("pac") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:pac>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pac";
	String expectedReplacedSource = "pac";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:pac>;\n";

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
 * Test for 'package' keyword.
 */
public void test0280(){
	String str =
		"#\n" +
		"pac p";

	String completeBehind = "pac";
	int cursorLocation = str.lastIndexOf("pac") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:pac>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pac";
	String expectedReplacedSource = "pac";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:pac>;\n";

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
 * Test for 'package' keyword.
 */
public void test0281(){
	String str =
		"""
		#
		package p;\
		pac""";

	String completeBehind = "pac";
	int cursorLocation = str.lastIndexOf("pac") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:pac>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pac";
	String expectedReplacedSource = "pac";
	String expectedUnitDisplayString =
		"package p;\n" +
		"import <CompleteOnKeyword:pac>;\n";

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
 * Test for 'package' keyword.
 */
public void test0282(){
	String str =
		"""
		#
		import p;\
		pac""";

	String completeBehind = "pac";
	int cursorLocation = str.lastIndexOf("pac") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:pac>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pac";
	String expectedReplacedSource = "pac";
	String expectedUnitDisplayString =
		"import p;\n" +
		"import <CompleteOnKeyword:pac>;\n";

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
 * Test for 'package' keyword.
 */
public void test0283(){
	String str =
		"""
		#
		class X {}\
		pac""";

	String completeBehind = "pac";
	int cursorLocation = str.lastIndexOf("pac") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:pac>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pac";
	String expectedReplacedSource = "pac";
	String expectedUnitDisplayString =
		"""
		import <CompleteOnKeyword:pac>;
		class X {
		  X() {
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
 * Test for 'return' keyword.
 */
public void test0284_Diet(){
	String str =
		"""
		public class X {
		  int foo() {
		    #
		    ret
		  }
		}""";

	String completeBehind = "ret";
	int cursorLocation = str.lastIndexOf("ret") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  int foo() {
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
 * Test for 'return' keyword.
 */
public void test0284_Method(){
	String str =
		"""
		public class X {
		  int foo() {
		    #
		    ret
		  }
		}""";

	String completeBehind = "ret";
	int cursorLocation = str.lastIndexOf("ret") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:ret>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ret";
	String expectedReplacedSource = "ret";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  int foo() {
		    <CompleteOnName:ret>;
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
 * Test for 'throw' keyword.
 */
public void test0285_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    thr
		  }
		}""";

	String completeBehind = "thr";
	int cursorLocation = str.lastIndexOf("thr") + completeBehind.length() - 1;

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
 * Test for 'throw' keyword.
 */
public void test0285_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    thr
		  }
		}""";

	String completeBehind = "thr";
	int cursorLocation = str.lastIndexOf("thr") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:thr>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "thr";
	String expectedReplacedSource = "thr";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:thr>;
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
 * Test for 'try' keyword.
 */
public void test0286_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    try
		  }
		}""";

	String completeBehind = "try";
	int cursorLocation = str.lastIndexOf("try") + completeBehind.length() - 1;

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
 * Test for 'try' keyword.
 */
public void test0286_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    try
		  }
		}""";

	String completeBehind = "try";
	int cursorLocation = str.lastIndexOf("try") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:try>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "try";
	String expectedReplacedSource = "try";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:try>;
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
 * Test for 'try' keyword.
 */
public void test0287_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    if(try
		  }
		}""";

	String completeBehind = "try";
	int cursorLocation = str.lastIndexOf("try") + completeBehind.length() - 1;

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
 * Test for 'try' keyword.
 */
public void test0287_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    if(try
		  }
		}""";

	String completeBehind = "try";
	int cursorLocation = str.lastIndexOf("try") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:try>";
	String expectedParentNodeToString = "if (<CompleteOnName:try>)\n    ;";
	String completionIdentifier = "try";
	String expectedReplacedSource = "try";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    if (<CompleteOnName:try>)
		        ;
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
 * Test for 'do' keyword.
 */
public void test0288_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    if(do
		  }
		}""";

	String completeBehind = "do";
	int cursorLocation = str.lastIndexOf("do") + completeBehind.length() - 1;

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
 * Test for 'do' keyword.
 */
public void test0288_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    if(do
		  }
		}""";

	String completeBehind = "do";
	int cursorLocation = str.lastIndexOf("do") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:do>";
	String expectedParentNodeToString = "if (<CompleteOnName:do>)\n    ;";
	String completionIdentifier = "do";
	String expectedReplacedSource = "do";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    if (<CompleteOnName:do>)
		        ;
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
 * Test for 'for' keyword.
 */
public void test0289_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    if(for
		  }
		}""";

	String completeBehind = "for";
	int cursorLocation = str.lastIndexOf("for") + completeBehind.length() - 1;

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
 * Test for 'for' keyword.
 */
public void test0289_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    if(for
		  }
		}""";

	String completeBehind = "for";
	int cursorLocation = str.lastIndexOf("for") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:for>";
	String expectedParentNodeToString = "if (<CompleteOnName:for>)\n    ;";
	String completionIdentifier = "for";
	String expectedReplacedSource = "for";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    if (<CompleteOnName:for>)
		        ;
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
 * Test for 'if' keyword.
 */
public void test0290_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    if(if
		  }
		}""";

	String completeBehind = "if";
	int cursorLocation = str.lastIndexOf("if") + completeBehind.length() - 1;

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
 * Test for 'if' keyword.
 */
public void test0290_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    if(if
		  }
		}""";

	String completeBehind = "if";
	int cursorLocation = str.lastIndexOf("if") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:if>";
	String expectedParentNodeToString = "if (<CompleteOnName:if>)\n    ;";
	String completionIdentifier = "if";
	String expectedReplacedSource = "if";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    if (<CompleteOnName:if>)
		        ;
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
 * Test for 'switch' keyword.
 */
public void test0291_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    if(swi
		  }
		}""";

	String completeBehind = "swi";
	int cursorLocation = str.lastIndexOf("swi") + completeBehind.length() - 1;

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
 * Test for 'switch' keyword.
 */
public void test0291_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    if(swi
		  }
		}""";

	String completeBehind = "swi";
	int cursorLocation = str.lastIndexOf("swi") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:swi>";
	String expectedParentNodeToString = "if (<CompleteOnName:swi>)\n    ;";
	String completionIdentifier = "swi";
	String expectedReplacedSource = "swi";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    if (<CompleteOnName:swi>)
		        ;
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
 * Test for 'new' keyword.
 */
public void test0292_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    new
		  }
		}""";

	String completeBehind = "new";
	int cursorLocation = str.lastIndexOf("new") + completeBehind.length() - 1;

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
 * Test for 'new' keyword.
 */
public void test0292_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    new
		  }
		}""";

	String completeBehind = "new";
	int cursorLocation = str.lastIndexOf("new") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:new>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "new";
	String expectedReplacedSource = "new";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:new>;
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
 * Test for 'new' keyword.
 */
public void test0293_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    new X
		  }
		}""";

	String completeBehind = "new";
	int cursorLocation = str.lastIndexOf("new") + completeBehind.length() - 1;

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
 * Test for 'new' keyword.
 */
public void test0293_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    new X
		  }
		}""";

	String completeBehind = "new";
	int cursorLocation = str.lastIndexOf("new") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:new>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "new";
	String expectedReplacedSource = "new";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:new>;
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
 * Test for 'new' keyword.
 */
public void test0294_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    new X()
		  }
		}""";

	String completeBehind = "new";
	int cursorLocation = str.lastIndexOf("new") + completeBehind.length() - 1;

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
 * Test for 'new' keyword.
 */
public void test0294_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    new X()
		  }
		}""";

	String completeBehind = "new";
	int cursorLocation = str.lastIndexOf("new") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:new>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "new";
	String expectedReplacedSource = "new";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:new>;
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
 * Test for 'throws' keyword.
 */
public void test0295(){
	String str =
		"""
		#
		public class X {
		  void foo() thr
		  }
		}""";

	String completeBehind = "thr";
	int cursorLocation = str.lastIndexOf("thr") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:thr>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "thr";
	String expectedReplacedSource = "thr";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() throws <CompleteOnKeyword:thr> {
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
 * Test for 'throws' keyword.
 */
public void test0296(){
	String str =
		"""
		#
		public class X {
		  void foo() thr {
		  }
		}""";

	String completeBehind = "thr";
	int cursorLocation = str.lastIndexOf("thr") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:thr>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "thr";
	String expectedReplacedSource = "thr";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() throws <CompleteOnKeyword:thr> {
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
 * Test for 'throws' keyword.
 */
public void test0297(){
	String str =
		"""
		#
		public class X {
		  void foo() thr E {
		  }
		}""";

	String completeBehind = "thr";
	int cursorLocation = str.lastIndexOf("thr") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:thr>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "thr";
	String expectedReplacedSource = "thr";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() throws <CompleteOnKeyword:thr> {
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
 * Test for 'throws' keyword.
 */
public void test0298(){
	String str =
		"""
		#
		public class X {
		  void foo() throws E thr
		  }
		}""";

	String completeBehind = "thr";
	int cursorLocation = str.lastIndexOf("thr") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "thr";
	String expectedReplacedSource = "thr";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() throws E {
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
 * Test for 'throws' keyword.
 */
public void test0299(){
	String str =
		"""
		#
		public class X {
		  X() thr
		  }
		}""";

	String completeBehind = "thr";
	int cursorLocation = str.lastIndexOf("thr") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:thr>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "thr";
	String expectedReplacedSource = "thr";
	String expectedUnitDisplayString =
		"""
		public class X {
		  X() throws <CompleteOnKeyword:thr> {
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
 * Test for 'throws' keyword.
 */
public void test0300(){
	String str =
		"""
		#
		public class X {
		  int foo()[] thr
		  }
		}""";

	String completeBehind = "thr";
	int cursorLocation = str.lastIndexOf("thr") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:thr>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "thr";
	String expectedReplacedSource = "thr";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  int[] foo() throws <CompleteOnKeyword:thr> {
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
 * Test for 'while' keyword.
 */
public void test0301_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    whi
		  }
		}""";

	String completeBehind = "whi";
	int cursorLocation = str.lastIndexOf("whi") + completeBehind.length() - 1;

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
 * Test for 'while' keyword.
 */
public void test0301_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    whi
		  }
		}""";

	String completeBehind = "whi";
	int cursorLocation = str.lastIndexOf("whi") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:whi>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "whi";
	String expectedReplacedSource = "whi";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:whi>;
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
 * Test for 'while' keyword.
 */
public void test0302_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    if(whi
		  }
		}""";

	String completeBehind = "whi";
	int cursorLocation = str.lastIndexOf("whi") + completeBehind.length() - 1;

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
 * Test for 'while' keyword.
 */
public void test0302_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    if(whi
		  }
		}""";

	String completeBehind = "whi";
	int cursorLocation = str.lastIndexOf("whi") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:whi>";
	String expectedParentNodeToString = "if (<CompleteOnName:whi>)\n    ;";
	String completionIdentifier = "whi";
	String expectedReplacedSource = "whi";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    if (<CompleteOnName:whi>)
		        ;
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
 * Test for 'assert' keyword.
 */
public void test0303_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    ass
		  }
		}""";

	String completeBehind = "ass";
	int cursorLocation = str.lastIndexOf("ass") + completeBehind.length() - 1;

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
 * Test for 'assert' keyword.
 */
public void test0303_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    ass
		  }
		}""";

	String completeBehind = "ass";
	int cursorLocation = str.lastIndexOf("ass") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:ass>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ass";
	String expectedReplacedSource = "ass";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:ass>;
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
 * Test for 'assert' keyword.
 */
public void test0304_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    if(ass
		  }
		}""";

	String completeBehind = "ass";
	int cursorLocation = str.lastIndexOf("ass") + completeBehind.length() - 1;

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
 * Test for 'assert' keyword.
 */
public void test0304_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    if(ass
		  }
		}""";

	String completeBehind = "ass";
	int cursorLocation = str.lastIndexOf("ass") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:ass>";
	String expectedParentNodeToString = "if (<CompleteOnName:ass>)\n    ;";
	String completionIdentifier = "ass";
	String expectedReplacedSource = "ass";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    if (<CompleteOnName:ass>)
		        ;
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
 * Test for 'final' keyword.
 */
public void test0305(){
	String str =
		"#\n" +
		"fin";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:fin>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "fin";
	String expectedReplacedSource = "fin";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:fin>;\n";

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
 * Test for 'final' keyword.
 */
public void test0306(){
	String str =
		"#\n" +
		"public fin";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:fin>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "fin";
	String expectedReplacedSource = "fin";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:fin>;\n";

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
 * Test for 'final' keyword.
 */
public void test0307(){
	String str =
		"#\n" +
		"fin zzz";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:fin>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "fin";
	String expectedReplacedSource = "fin";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:fin>;\n";

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
 * Test for 'final' keyword.
 */
public void test0308(){
	String str =
		"#\n" +
		"final fin";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:fin>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "fin";
	String expectedReplacedSource = "fin";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:fin>;\n";

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
 * Test for 'final' keyword.
 */
public void test0309(){
	String str =
		"#\n" +
		"abstract fin";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:fin>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "fin";
	String expectedReplacedSource = "fin";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:fin>;\n";

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
 * Test for 'final' keyword.
 */
public void test0310(){
	String str =
		"#\n" +
		"public fin class X {}";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:fin>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "fin";
	String expectedReplacedSource = "fin";
	String expectedUnitDisplayString =
		"""
		import <CompleteOnKeyword:fin>;
		class X {
		  X() {
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
 * Test for 'final' keyword.
 */
public void test0311(){
	String str =
		"""
		#
		public class X {
		  fin
		}""";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:fin>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "fin";
	String expectedReplacedSource = "fin";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:fin>;
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
 * Test for 'final' keyword.
 */
public void test0312(){
	String str =
		"""
		#
		public class X {
		  public fin
		}""";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:fin>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "fin";
	String expectedReplacedSource = "fin";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:fin>;
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
 * Test for 'final' keyword.
 */
public void test0313(){
	String str =
		"""
		#
		public class X {
		  fin zzz
		}""";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:fin>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "fin";
	String expectedReplacedSource = "fin";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:fin>;
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
 * Test for 'final' keyword.
 */
public void test0314(){
	String str =
		"""
		#
		public class X {
		  final fin
		}""";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:fin>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "fin";
	String expectedReplacedSource = "fin";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:fin>;
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
 * Test for 'final' keyword.
 */
public void test0315(){
	String str =
		"""
		#
		public class X {
		  abstract fin
		}""";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:fin>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "fin";
	String expectedReplacedSource = "fin";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:fin>;
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
 * Test for 'native' keyword.
 */
public void test0316(){
	String str =
		"""
		#
		public class X {
		  nat
		}""";

	String completeBehind = "nat";
	int cursorLocation = str.lastIndexOf("nat") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:nat>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "nat";
	String expectedReplacedSource = "nat";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:nat>;
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
 * Test for 'native' keyword.
 */
public void test0317(){
	String str =
		"""
		#
		public class X {
		  public nat
		}""";

	String completeBehind = "nat";
	int cursorLocation = str.lastIndexOf("nat") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:nat>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "nat";
	String expectedReplacedSource = "nat";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:nat>;
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
 * Test for 'native' keyword.
 */
public void test0318(){
	String str =
		"""
		#
		public class X {
		  transient nat
		}""";

	String completeBehind = "nat";
	int cursorLocation = str.lastIndexOf("nat") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:nat>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "nat";
	String expectedReplacedSource = "nat";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:nat>;
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
 * Test for 'native' keyword.
 */
public void test0319(){
	String str =
		"""
		#
		public class X {
		  transient nat
		}""";

	String completeBehind = "nat";
	int cursorLocation = str.lastIndexOf("nat") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:nat>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "nat";
	String expectedReplacedSource = "nat";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:nat>;
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
 * Test for 'native' keyword.
 */
public void test0320(){
	String str =
		"""
		#
		public class X {
		  volatile nat
		}""";

	String completeBehind = "nat";
	int cursorLocation = str.lastIndexOf("nat") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:nat>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "nat";
	String expectedReplacedSource = "nat";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:nat>;
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
 * Test for 'strictfp' keyword.
 */
public void test0321(){
	String str =
		"""
		#
		public class X {
		  str
		}""";

	String completeBehind = "str";
	int cursorLocation = str.lastIndexOf("str") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:str>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "str";
	String expectedReplacedSource = "str";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:str>;
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
 * Test for 'strictfp' keyword.
 */
public void test0322(){
	String str =
		"""
		#
		public class X {
		  public str
		}""";

	String completeBehind = "str";
	int cursorLocation = str.lastIndexOf("str") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:str>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "str";
	String expectedReplacedSource = "str";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:str>;
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
 * Test for 'strictfp' keyword.
 */
public void test0323(){
	String str =
		"""
		#
		public class X {
		  transient str
		}""";

	String completeBehind = "str";
	int cursorLocation = str.lastIndexOf("str") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:str>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "str";
	String expectedReplacedSource = "str";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:str>;
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
 * Test for 'strictfp' keyword.
 */
public void test0324(){
	String str =
		"""
		#
		public class X {
		  transient str
		}""";

	String completeBehind = "str";
	int cursorLocation = str.lastIndexOf("str") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:str>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "str";
	String expectedReplacedSource = "str";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:str>;
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
 * Test for 'strictfp' keyword.
 */
public void test0325(){
	String str =
		"""
		#
		public class X {
		  volatile str
		}""";

	String completeBehind = "str";
	int cursorLocation = str.lastIndexOf("str") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:str>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "str";
	String expectedReplacedSource = "str";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:str>;
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
 * Test for 'volatile' keyword.
 */
public void test0326(){
	String str =
		"""
		#
		public class X {
		  vol
		}""";

	String completeBehind = "vol";
	int cursorLocation = str.lastIndexOf("vol") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:vol>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "vol";
	String expectedReplacedSource = "vol";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:vol>;
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
 * Test for 'volatile' keyword.
 */
public void test0327(){
	String str =
		"""
		#
		public class X {
		  public vol
		}""";

	String completeBehind = "vol";
	int cursorLocation = str.lastIndexOf("vol") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:vol>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "vol";
	String expectedReplacedSource = "vol";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:vol>;
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
 * Test for 'volatile' keyword.
 */
public void test0328(){
	String str =
		"""
		#
		public class X {
		  transient vol
		}""";

	String completeBehind = "vol";
	int cursorLocation = str.lastIndexOf("vol") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:vol>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "vol";
	String expectedReplacedSource = "vol";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:vol>;
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
 * Test for 'volatile' keyword.
 */
public void test0329(){
	String str =
		"""
		#
		public class X {
		  volatile vol
		}""";

	String completeBehind = "vol";
	int cursorLocation = str.lastIndexOf("vol") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:vol>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "vol";
	String expectedReplacedSource = "vol";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:vol>;
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
 * Test for 'volatile' keyword.
 */
public void test0330(){
	String str =
		"""
		#
		public class X {
		  native vol
		}""";

	String completeBehind = "vol";
	int cursorLocation = str.lastIndexOf("vol") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:vol>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "vol";
	String expectedReplacedSource = "vol";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:vol>;
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
 * Test for 'transient' keyword.
 */
public void test0331(){
	String str =
		"""
		#
		public class X {
		  tra
		}""";

	String completeBehind = "tra";
	int cursorLocation = str.lastIndexOf("tra") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:tra>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "tra";
	String expectedReplacedSource = "tra";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:tra>;
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
 * Test for 'transient' keyword.
 */
public void test0332(){
	String str =
		"""
		#
		public class X {
		  public tra
		}""";

	String completeBehind = "tra";
	int cursorLocation = str.lastIndexOf("tra") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:tra>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "tra";
	String expectedReplacedSource = "tra";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:tra>;
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
 * Test for 'transient' keyword.
 */
public void test0333(){
	String str =
		"""
		#
		public class X {
		  transient tra
		}""";

	String completeBehind = "tra";
	int cursorLocation = str.lastIndexOf("tra") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:tra>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "tra";
	String expectedReplacedSource = "tra";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:tra>;
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
 * Test for 'transient' keyword.
 */
public void test0334(){
	String str =
		"""
		#
		public class X {
		  volatile tra
		}""";

	String completeBehind = "tra";
	int cursorLocation = str.lastIndexOf("tra") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:tra>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "tra";
	String expectedReplacedSource = "tra";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:tra>;
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
 * Test for 'transient' keyword.
 */
public void test0335(){
	String str =
		"""
		#
		public class X {
		  native tra
		}""";

	String completeBehind = "tra";
	int cursorLocation = str.lastIndexOf("tra") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:tra>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "tra";
	String expectedReplacedSource = "tra";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:tra>;
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
 * Test for 'synchronized' keyword.
 */
public void test0336(){
	String str =
		"""
		#
		public class X {
		  syn
		}""";

	String completeBehind = "syn";
	int cursorLocation = str.lastIndexOf("syn") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:syn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "syn";
	String expectedReplacedSource = "syn";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:syn>;
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
 * Test for 'synchronized' keyword.
 */
public void test0337(){
	String str =
		"""
		#
		public class X {
		  public syn
		}""";

	String completeBehind = "syn";
	int cursorLocation = str.lastIndexOf("syn") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:syn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "syn";
	String expectedReplacedSource = "syn";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:syn>;
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
 * Test for 'synchronized' keyword.
 */
public void test0338(){
	String str =
		"""
		#
		public class X {
		  transient syn
		}""";

	String completeBehind = "syn";
	int cursorLocation = str.lastIndexOf("syn") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:syn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "syn";
	String expectedReplacedSource = "syn";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:syn>;
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
 * Test for 'synchronized' keyword.
 */
public void test0339(){
	String str =
		"""
		#
		public class X {
		  transient syn
		}""";

	String completeBehind = "syn";
	int cursorLocation = str.lastIndexOf("syn") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:syn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "syn";
	String expectedReplacedSource = "syn";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:syn>;
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
 * Test for 'synchronized' keyword.
 */
public void test0340(){
	String str =
		"""
		#
		public class X {
		  volatile syn
		}""";

	String completeBehind = "syn";
	int cursorLocation = str.lastIndexOf("syn") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:syn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "syn";
	String expectedReplacedSource = "syn";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:syn>;
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
 * Test for 'synchronized' keyword.
 */
public void test0341_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    syn
		  }
		}""";

	String completeBehind = "syn";
	int cursorLocation = str.lastIndexOf("syn") + completeBehind.length() - 1;

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
 * Test for 'synchronized' keyword.
 */
public void test0341_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    syn
		  }
		}""";

	String completeBehind = "syn";
	int cursorLocation = str.lastIndexOf("syn") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:syn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "syn";
	String expectedReplacedSource = "syn";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:syn>;
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
 * Test for 'synchronized' keyword.
 */
public void test0342_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    if(syn
		  }
		}""";

	String completeBehind = "syn";
	int cursorLocation = str.lastIndexOf("syn") + completeBehind.length() - 1;

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
 * Test for 'synchronized' keyword.
 */
public void test0342_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    if(syn
		  }
		}""";

	String completeBehind = "syn";
	int cursorLocation = str.lastIndexOf("syn") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:syn>";
	String expectedParentNodeToString = "if (<CompleteOnName:syn>)\n    ;";
	String completionIdentifier = "syn";
	String expectedReplacedSource = "syn";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    if (<CompleteOnName:syn>)
		        ;
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
 * Test for 'static' keyword.
 */
public void test0343(){
	String str =
		"""
		#
		public class X {
		  sta
		}""";

	String completeBehind = "sta";
	int cursorLocation = str.lastIndexOf("sta") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:sta>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "sta";
	String expectedReplacedSource = "sta";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:sta>;
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
 * Test for 'static' keyword.
 */
public void test0344(){
	String str =
		"""
		#
		public class X {
		  public sta
		}""";

	String completeBehind = "sta";
	int cursorLocation = str.lastIndexOf("sta") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:sta>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "sta";
	String expectedReplacedSource = "sta";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:sta>;
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
 * Test for 'public' keyword.
 */
public void test0345(){
	String str =
		"#\n" +
		"pub";

	String completeBehind = "pub";
	int cursorLocation = str.lastIndexOf("pub") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:pub>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pub";
	String expectedReplacedSource = "pub";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:pub>;\n";

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
 * Test for 'public' keyword.
 */
public void test0346(){
	String str =
		"#\n" +
		"final pub";

	String completeBehind = "pub";
	int cursorLocation = str.lastIndexOf("pub") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:pub>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pub";
	String expectedReplacedSource = "pub";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:pub>;\n";

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
 * Test for 'public' keyword.
 */
public void test0347(){
	String str =
		"#\n" +
		"public pub";

	String completeBehind = "pub";
	int cursorLocation = str.lastIndexOf("pub") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:pub>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pub";
	String expectedReplacedSource = "pub";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:pub>;\n";

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
 * Test for 'public' keyword.
 */
public void test0348(){
	String str =
		"#\n" +
		"private pub";

	String completeBehind = "pub";
	int cursorLocation = str.lastIndexOf("pub") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:pub>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pub";
	String expectedReplacedSource = "pub";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:pub>;\n";

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
 * Test for 'public' keyword.
 */
public void test0349(){
	String str =
		"""
		#
		public class X{}
		pub""";

	String completeBehind = "pub";
	int cursorLocation = str.lastIndexOf("pub") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:pub>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pub";
	String expectedReplacedSource = "pub";
	String expectedUnitDisplayString =
		"""
		import <CompleteOnKeyword:pub>;
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
/*
 * Test for 'public' keyword.
 */
public void test0350(){
	String str =
		"""
		#
		public class X{
		  pub
		}""";

	String completeBehind = "pub";
	int cursorLocation = str.lastIndexOf("pub") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pub>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pub";
	String expectedReplacedSource = "pub";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:pub>;
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
 * Test for 'public' keyword.
 */
public void test0351(){
	String str =
		"""
		#
		public class X{
		  public pub
		}""";

	String completeBehind = "pub";
	int cursorLocation = str.lastIndexOf("pub") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pub>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pub";
	String expectedReplacedSource = "pub";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:pub>;
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
 * Test for 'public' keyword.
 */
public void test0352(){
	String str =
		"""
		#
		public class X{
		  private pub
		}""";

	String completeBehind = "pub";
	int cursorLocation = str.lastIndexOf("pub") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pub>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pub";
	String expectedReplacedSource = "pub";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:pub>;
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
 * Test for 'public' keyword.
 */
public void test0353(){
	String str =
		"""
		#
		public class X{
		  protected pub
		}""";

	String completeBehind = "pub";
	int cursorLocation = str.lastIndexOf("pub") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pub>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pub";
	String expectedReplacedSource = "pub";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:pub>;
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
 * Test for 'public' keyword.
 */
public void test0354(){
	String str =
		"""
		#
		public class X{
		  abstract pub
		}""";

	String completeBehind = "pub";
	int cursorLocation = str.lastIndexOf("pub") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pub>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pub";
	String expectedReplacedSource = "pub";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:pub>;
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
 * Test for 'protected' keyword.
 */
public void test0355(){
	String str =
		"#\n" +
		"pro";

	String completeBehind = "pro";
	int cursorLocation = str.lastIndexOf("") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"";

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
 * Test for 'protected' keyword.
 */
public void test0356(){
	String str =
		"#\n" +
		"final pro";

	String completeBehind = "pro";
	int cursorLocation = str.lastIndexOf("pro") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:pro>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pro";
	String expectedReplacedSource = "pro";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:pro>;\n";

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
 * Test for 'protected' keyword.
 */
public void test0357(){
	String str =
		"#\n" +
		"public pro";

	String completeBehind = "pro";
	int cursorLocation = str.lastIndexOf("pro") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:pro>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pro";
	String expectedReplacedSource = "pro";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:pro>;\n";

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
 * Test for 'protected' keyword.
 */
public void test0358(){
	String str =
		"#\n" +
		"private pro";

	String completeBehind = "pro";
	int cursorLocation = str.lastIndexOf("pro") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:pro>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pro";
	String expectedReplacedSource = "pro";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:pro>;\n";

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
 * Test for 'protected' keyword.
 */
public void test0359(){
	String str =
		"""
		#
		public class X{}
		pro""";

	String completeBehind = "pro";
	int cursorLocation = str.lastIndexOf("pro") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:pro>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pro";
	String expectedReplacedSource = "pro";
	String expectedUnitDisplayString =
		"""
		import <CompleteOnKeyword:pro>;
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
/*
 * Test for 'protected' keyword.
 */
public void test0360(){
	String str =
		"""
		#
		public class X{
		  pro
		}""";

	String completeBehind = "pro";
	int cursorLocation = str.lastIndexOf("pro") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pro>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pro";
	String expectedReplacedSource = "pro";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:pro>;
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
 * Test for 'protected' keyword.
 */
public void test0361(){
	String str =
		"""
		#
		public class X{
		  public pro
		}""";

	String completeBehind = "pro";
	int cursorLocation = str.lastIndexOf("pro") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pro>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pro";
	String expectedReplacedSource = "pro";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:pro>;
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
 * Test for 'protected' keyword.
 */
public void test0362(){
	String str =
		"""
		#
		public class X{
		  private pro
		}""";

	String completeBehind = "pro";
	int cursorLocation = str.lastIndexOf("pro") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pro>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pro";
	String expectedReplacedSource = "pro";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:pro>;
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
 * Test for 'protected' keyword.
 */
public void test0363(){
	String str =
		"""
		#
		public class X{
		  protected pro
		}""";

	String completeBehind = "pro";
	int cursorLocation = str.lastIndexOf("pro") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pro>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pro";
	String expectedReplacedSource = "pro";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:pro>;
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
 * Test for 'protected' keyword.
 */
public void test0364(){
	String str =
		"""
		#
		public class X{
		  abstract pro
		}""";

	String completeBehind = "pro";
	int cursorLocation = str.lastIndexOf("pro") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pro>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pro";
	String expectedReplacedSource = "pro";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:pro>;
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
 * Test for 'private' keyword.
 */
public void test0365(){
	String str =
		"#\n" +
		"pri";

	String completeBehind = "pri";
	int cursorLocation = str.lastIndexOf("") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"";

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
 * Test for 'private' keyword.
 */
public void test0366(){
	String str =
		"#\n" +
		"final pri";

	String completeBehind = "pri";
	int cursorLocation = str.lastIndexOf("pri") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:pri>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pri";
	String expectedReplacedSource = "pri";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:pri>;\n";

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
 * Test for 'private' keyword.
 */
public void test0367(){
	String str =
		"#\n" +
		"public pri";

	String completeBehind = "pri";
	int cursorLocation = str.lastIndexOf("pri") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:pri>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pri";
	String expectedReplacedSource = "pri";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:pri>;\n";

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
 * Test for 'private' keyword.
 */
public void test0368(){
	String str =
		"#\n" +
		"private pri";

	String completeBehind = "pri";
	int cursorLocation = str.lastIndexOf("pri") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:pri>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pri";
	String expectedReplacedSource = "pri";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:pri>;\n";

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
 * Test for 'private' keyword.
 */
public void test0369(){
	String str =
		"""
		#
		public class X{}
		pri""";

	String completeBehind = "pri";
	int cursorLocation = str.lastIndexOf("pri") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:pri>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pri";
	String expectedReplacedSource = "pri";
	String expectedUnitDisplayString =
		"""
		import <CompleteOnKeyword:pri>;
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
/*
 * Test for 'private' keyword.
 */
public void test0370(){
	String str =
		"""
		#
		public class X{
		  pri
		}""";

	String completeBehind = "pri";
	int cursorLocation = str.lastIndexOf("pri") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pri>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pri";
	String expectedReplacedSource = "pri";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:pri>;
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
 * Test for 'private' keyword.
 */
public void test0371(){
	String str =
		"""
		#
		public class X{
		  public pri
		}""";

	String completeBehind = "pri";
	int cursorLocation = str.lastIndexOf("pri") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pri>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pri";
	String expectedReplacedSource = "pri";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:pri>;
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
 * Test for 'private' keyword.
 */
public void test0372(){
	String str =
		"""
		#
		public class X{
		  private pri
		}""";

	String completeBehind = "pri";
	int cursorLocation = str.lastIndexOf("pri") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pri>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pri";
	String expectedReplacedSource = "pri";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:pri>;
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
 * Test for 'private' keyword.
 */
public void test0373(){
	String str =
		"""
		#
		public class X{
		  protected pri
		}""";

	String completeBehind = "pri";
	int cursorLocation = str.lastIndexOf("pri") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pri>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pri";
	String expectedReplacedSource = "pri";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:pri>;
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
 * Test for 'private' keyword.
 */
public void test0374(){
	String str =
		"""
		#
		public class X{
		  abstract pri
		}""";

	String completeBehind = "pri";
	int cursorLocation = str.lastIndexOf("pri") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pri>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pri";
	String expectedReplacedSource = "pri";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:pri>;
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
 * Test for 'super' keyword.
 */
public void test0375_Diet(){
	String str =
		"""
		public class X {
		  void foo(){
		    #
		    sup
		  }
		}
		""";

	String completeBehind = "sup";
	int cursorLocation = str.lastIndexOf("sup") + completeBehind.length() - 1;

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
 * Test for 'super' keyword.
 */
public void test0375_Method(){
	String str =
		"""
		public class X {
		  void foo(){
		    #
		    sup
		  }
		}
		""";

	String completeBehind = "sup";
	int cursorLocation = str.lastIndexOf("sup") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:sup>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "sup";
	String expectedReplacedSource = "sup";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:sup>;
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
 * Test for 'this' keyword.
 */
public void test0376_Diet(){
	String str =
		"""
		public class X {
		  void foo(){
		    #
		    thi
		  }
		}
		""";

	String completeBehind = "thi";
	int cursorLocation = str.lastIndexOf("thi") + completeBehind.length() - 1;

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
 * Test for 'this' keyword.
 */
public void test0376_Method(){
	String str =
		"""
		public class X {
		  void foo(){
		    #
		    thi
		  }
		}
		""";

	String completeBehind = "thi";
	int cursorLocation = str.lastIndexOf("thi") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:thi>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "thi";
	String expectedReplacedSource = "thi";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:thi>;
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
 * Test for 'true' keyword.
 */
public void test0377_Diet(){
	String str =
		"""
		public class X {
		  void foo(){
		    #
		    tru
		  }
		}
		""";

	String completeBehind = "tru";
	int cursorLocation = str.lastIndexOf("tru") + completeBehind.length() - 1;

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
 * Test for 'true' keyword.
 */
public void test0377_Method(){
	String str =
		"""
		public class X {
		  void foo(){
		    #
		    tru
		  }
		}
		""";

	String completeBehind = "tru";
	int cursorLocation = str.lastIndexOf("tru") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:tru>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "tru";
	String expectedReplacedSource = "tru";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:tru>;
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
 * Test for 'false' keyword.
 */
public void test0378_Diet(){
	String str =
		"""
		public class X {
		  void foo(){
		    #
		    fal
		  }
		}
		""";

	String completeBehind = "fal";
	int cursorLocation = str.lastIndexOf("fal") + completeBehind.length() - 1;

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
 * Test for 'false' keyword.
 */
public void test0378_Method(){
	String str =
		"""
		public class X {
		  void foo(){
		    #
		    fal
		  }
		}
		""";

	String completeBehind = "fal";
	int cursorLocation = str.lastIndexOf("fal") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:fal>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "fal";
	String expectedReplacedSource = "fal";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:fal>;
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
 * Test for 'null' keyword.
 */
public void test0379_Diet(){
	String str =
		"""
		public class X {
		  void foo(){
		    #
		    nul
		  }
		}
		""";

	String completeBehind = "nul";
	int cursorLocation = str.lastIndexOf("nul") + completeBehind.length() - 1;

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
 * Test for 'null' keyword.
 */
public void test0379_Method(){
	String str =
		"""
		public class X {
		  void foo(){
		    #
		    nul
		  }
		}
		""";

	String completeBehind = "nul";
	int cursorLocation = str.lastIndexOf("nul") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:nul>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "nul";
	String expectedReplacedSource = "nul";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:nul>;
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
 * Test for 'instanceof' keyword.
 */
public void test0380_Diet(){
	String str =
		"""
		public class X {
		  void foo(){
		    #
		    if(zzz ins
		  }
		}
		""";

	String completeBehind = "ins";
	int cursorLocation = str.lastIndexOf("ins") + completeBehind.length() - 1;

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
 * Test for 'instanceof' keyword.
 */
public void test0380_Method(){
	String str =
		"""
		public class X {
		  void foo(){
		    #
		    if(zzz ins
		  }
		}
		""";

	String completeBehind = "ins";
	int cursorLocation = str.lastIndexOf("ins") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:ins>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ins";
	String expectedReplacedSource = "ins";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnKeyword:ins>;
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
 * Test for 'instanceof' keyword.
 */
public void test0381_Diet(){
	String str =
		"""
		public class X {
		  void foo(){
		    #
		    ins
		  }
		}
		""";

	String completeBehind = "ins";
	int cursorLocation = str.lastIndexOf("ins") + completeBehind.length() - 1;

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
 * Test for 'instanceof' keyword.
 */
public void test0381_Method(){
	String str =
		"""
		public class X {
		  void foo(){
		    #
		    ins
		  }
		}
		""";

	String completeBehind = "ins";
	int cursorLocation = str.lastIndexOf("ins") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:ins>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ins";
	String expectedReplacedSource = "ins";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:ins>;
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
 * Test for 'instanceof' keyword.
 */
public void test0382_Diet(){
	String str =
		"""
		public class X {
		  void foo(){
		    #
		    if(zzz zzz ins
		  }
		}
		""";

	String completeBehind = "ins";
	int cursorLocation = str.lastIndexOf("ins") + completeBehind.length() - 1;

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
 * Test for 'instanceof' keyword.
 */
public void test0382_Method(){
	String str =
		"""
		public class X {
		  void foo(){
		    #
		    if(zzz zzz ins
		  }
		}
		""";

	String completeBehind = "ins";
	int cursorLocation = str.lastIndexOf("ins") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:ins>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ins";
	String expectedReplacedSource = "ins";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    zzz zzz;
		    <CompleteOnName:ins>;
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
 * Test for 'while' keyword.
 */
public void test0384_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    do{
		    } whi
		  }
		}""";

	String completeBehind = "whi";
	int cursorLocation = str.lastIndexOf("whi") + completeBehind.length() - 1;

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
 * Test for 'while' keyword.
 */
public void test0384_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    #
		    do{
		    } whi
		  }
		}""";

	String completeBehind = "whi";
	int cursorLocation = str.lastIndexOf("whi") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:whi>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "whi";
	String expectedReplacedSource = "whi";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnKeyword:whi>;
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
 * Test for 'catch' keyword.
 */
public void test0385_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    try {
		    } catch(E e) {
		    } cat
		  }
		}
		""";

	String completeBehind = "cat";
	int cursorLocation = str.lastIndexOf("cat") + completeBehind.length() - 1;

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
/*
 * Test for 'catch' keyword.
 */
public void test0385_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    try {
		    } catch(E e) {
		    } cat
		  }
		}
		""";

	String completeBehind = "cat";
	int cursorLocation = str.lastIndexOf("cat") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:cat>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "cat";
	String expectedReplacedSource = "cat";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:cat>;
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
 * Test for 'finally' keyword.
 */
public void test0386_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    try {\
		    } catch(E e) {\
		    } fin\
		  }
		}
		""";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

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
/*
 * Test for 'finally' keyword.
 */
public void test0386_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    try {\
		    } catch(E e) {\
		    } fin\
		  }
		}
		""";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:fin>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "fin";
	String expectedReplacedSource = "fin";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:fin>;
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
 * Test for 'finally' keyword.
 */
public void test0387_Diet(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    try {\
		    } finally {\
		    } fin\
		  }
		}
		""";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

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
/*
 * Test for 'finally' keyword.
 */
public void test0387_Method(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    #
		    try {\
		    } finally {\
		    } fin\
		  }
		}
		""";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:fin>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "fin";
	String expectedReplacedSource = "fin";
	String expectedUnitDisplayString =
			"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:fin>;
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
 * Test for 'this' keyword.
 */
public void test0388_Diet(){
	String str =
		"""
		public class X {
		  void foo(){
		    #
		    X.thi
		  }
		}
		""";

	String completeBehind = "thi";
	int cursorLocation = str.lastIndexOf("thi") + completeBehind.length() - 1;

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
 * Test for 'this' keyword.
 */
public void test0388_Method(){
	String str =
		"""
		public class X {
		  void foo(){
		    #
		    X.thi
		  }
		}
		""";

	String completeBehind = "thi";
	int cursorLocation = str.lastIndexOf("thi") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnName:X.thi>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "thi";
	String expectedReplacedSource = "X.thi";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnName:X.thi>;
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
