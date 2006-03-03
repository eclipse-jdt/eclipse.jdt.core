/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	return buildAllCompliancesRegressionTestSetupSuite(CompletionParserTestKeyword.class);
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
		"package p;\n" +
		"import yyy;\n" +
		"abst";

	String completeBehind = "abst";
	int cursorLocation = str.indexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"package p;\n" +
		"import yyy;\n" +
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
public void test0006(){
	String str =
		"package p;\n" +
		"import yyy;\n" +
		"abst zzz";

	String completeBehind = "abst";
	int cursorLocation = str.indexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"package p;\n" +
		"import yyy;\n" +
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
public void test0007(){
	String str =
		"package p;\n" +
		"import yyy;\n" +
		"public abst";

	String completeBehind = "abst";
	int cursorLocation = str.indexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"package p;\n" +
		"import yyy;\n" +
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
public void test0008(){
	String str =
		"package p;\n" +
		"import yyy;\n" +
		"public abst zzz";

	String completeBehind = "abst";
	int cursorLocation = str.indexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"package p;\n" +
		"import yyy;\n" +
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
public void test0009(){
	String str =
		"package p;\n" +
		"import yyy;\n" +
		"abstract abst";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"import yyy;\n" + 
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
public void test0010(){
	String str =
		"package p;\n" +
		"import yyy;\n" +
		"abstract abst zzz";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"import yyy;\n" + 
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
public void test0011(){
	String str =
		"package p;\n" +
		"import \n" +
		"abst";

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
		"package p;\n" +
		"import yyy;\n" +
		"public class X {}\n" +
		"abst";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"package p;\n" +
		"import yyy;\n" +
		"import <CompleteOnKeyword:abst>;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"package p;\n" +
		"import yyy;\n" +
		"public class X {}\n" +
		"abst zzz";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"package p;\n" +
		"import yyy;\n" +
		"import <CompleteOnKeyword:abst>;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"package p;\n" +
		"import yyy;\n" +
		"final abst";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"import yyy;\n" + 
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
public void test0015(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  abst\n" +
		"}\n";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  <CompleteOnType:abst>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"package p;\n" +
		"public class X {\n" +
		"  abst zzz\n" +
		"}\n";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:abst>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  <CompleteOnType:abst>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"package p;\n" +
		"public class X {\n" +
		"  public abst zzz\n" +
		"}\n";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:abst>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  <CompleteOnType:abst>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"package p;\n" +
		"public class X {\n" +
		"  final abst\n" +
		"}\n";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  <CompleteOnType:abst>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"package p;\n" +
		"public class X {\n" +
		"  abstract abst\n" +
		"}\n";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  <CompleteOnType:abst>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"package p;\n" +
		"public class X {\n" +
		"  static abst\n" +
		"}\n";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  <CompleteOnType:abst>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
public void test0021(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    abst\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:abst>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "abst";
	expectedReplacedSource = "abst";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:abst>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0022(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    abst zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:abst>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "abst";
	expectedReplacedSource = "abst";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:abst>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0023(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    bre\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "bre";
	int cursorLocation = str.lastIndexOf("bre") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:bre>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "bre";
	expectedReplacedSource = "bre";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:bre>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0024(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    for(int i; i < 10; i++) {\n" +
		"      bre\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "bre";
	int cursorLocation = str.lastIndexOf("bre") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:bre>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "bre";
	expectedReplacedSource = "bre";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    int i;\n" +
		"    {\n" +
		"      <CompleteOnName:bre>;\n" +
		"    }\n" +
		"  }\n" +
		"}\n";
	
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
public void test0025(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    cas\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "cas";
	int cursorLocation = str.lastIndexOf("cas") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:cas>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "cas";
	expectedReplacedSource = "cas";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:cas>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0026(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    switch(0) {\n" +
		"      cas\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "cas";
	int cursorLocation = str.lastIndexOf("cas") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnKeyword:cas>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "cas";
	expectedReplacedSource = "cas";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnKeyword:cas>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0027(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"     cat\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "cat";
	int cursorLocation = str.lastIndexOf("cat") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:cat>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "cat";
	expectedReplacedSource = "cat";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:cat>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0028(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    try {\n" +
		"    } cat\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "cat";
	int cursorLocation = str.lastIndexOf("cat") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnKeyword:cat>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "cat";
	expectedReplacedSource = "cat";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnKeyword:cat>;\n" +
		"  }\n" +
		"}\n";
	
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
		"public class X {\n" +
		"  cla\n" +
		"}";

	String completeBehind = "cla";
	int cursorLocation = str.lastIndexOf("cla") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:cla>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "cla";
	String expectedReplacedSource = "cla";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:cla>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X {\n" +
		"  public cla\n" +
		"}";

	String completeBehind = "cla";
	int cursorLocation = str.lastIndexOf("cla") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:cla>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "cla";
	String expectedReplacedSource = "cla";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:cla>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X {\n" +
		"  public final cla\n" +
		"}";

	String completeBehind = "cla";
	int cursorLocation = str.lastIndexOf("cla") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:cla>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "cla";
	String expectedReplacedSource = "cla";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:cla>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X {\n" +
		"  public final cla Y\n" +
		"}";

	String completeBehind = "cla";
	int cursorLocation = str.lastIndexOf("cla") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:cla>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "cla";
	String expectedReplacedSource = "cla";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:cla>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
public void test0037(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    cla\n" +
		"  }\n" +
		"}";

	String completeBehind = "cla";
	int cursorLocation = str.lastIndexOf("cla") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:cla>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "cla";
	expectedReplacedSource = "cla";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:cla>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0038(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    final cla\n" +
		"  }\n" +
		"}";

	String completeBehind = "cla";
	int cursorLocation = str.lastIndexOf("cla") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:cla>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "cla";
	expectedReplacedSource = "cla";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:cla>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0039(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    final cla Y\n" +
		"  }\n" +
		"}";

	String completeBehind = "cla";
	int cursorLocation = str.lastIndexOf("cla") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:cla>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "cla";
	expectedReplacedSource = "cla";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:cla>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0040(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"     con\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "con";
	int cursorLocation = str.lastIndexOf("con") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:con>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "con";
	expectedReplacedSource = "con";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:con>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0041(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"     for(int i; i < 5; i++) {\n" +
		"       con\n" +
		"     }\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "con";
	int cursorLocation = str.lastIndexOf("con") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:con>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "con";
	expectedReplacedSource = "con";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    int i;\n" +
		"    {\n" +
		"      <CompleteOnName:con>;\n" +
		"    }\n" +
		"  }\n" +
		"}\n";
	
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
public void test0042(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"     def\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "def";
	int cursorLocation = str.lastIndexOf("def") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:def>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "def";
	expectedReplacedSource = "def";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:def>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0043(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"     switch(0) {\n" +
		"       case 1 : break;\n" +
		"       def\n" +
		"     }\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "def";
	int cursorLocation = str.lastIndexOf("def") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:def>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "def";
	expectedReplacedSource = "def";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    {\n" +
		"      <CompleteOnName:def>;\n" +
		"    }\n" +
		"  }\n" +
		"}\n";
	
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
public void test0044(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"     do\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "do";
	int cursorLocation = str.lastIndexOf("do") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:do>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "do";
	expectedReplacedSource = "do";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:do>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0045(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"     els\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "els";
	int cursorLocation = str.lastIndexOf("els") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:els>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "els";
	expectedReplacedSource = "els";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:els>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0046(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"     if(true) {\n" +
		"     } els\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "els";
	int cursorLocation = str.lastIndexOf("els") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:els>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "els";
	expectedReplacedSource = "els";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:els>;\n" +
		"  }\n" +
		"}\n";
	
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
		"class X extends <CompleteOnKeyword:ext> {\n" +
		"  X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"class X extends <CompleteOnKeyword:ext> {\n" +
		"  X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"class X extends <CompleteOnKeyword:ext> {\n" +
		"  {\n" +
		"  }\n" +
		"  X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"class X extends <CompleteOnKeyword:ext> {\n" +
		"  X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"class X implements Y {\n" +
		"  X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
public void test0055(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"     fin" +
		"  }\n" +
		"}\n";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:fin>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "fin";
	expectedReplacedSource = "fin";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:fin>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0056(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"     try {" +
		"     } fin" +
		"  }\n" +
		"}\n";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnKeyword:fin>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "fin";
	expectedReplacedSource = "fin";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnKeyword:fin>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0057(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"     for" +
		"  }\n" +
		"}\n";

	String completeBehind = "for";
	int cursorLocation = str.lastIndexOf("for") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:for>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "for";
	expectedReplacedSource = "for";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:for>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0058(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"     if" +
		"  }\n" +
		"}\n";

	String completeBehind = "if";
	int cursorLocation = str.lastIndexOf("if") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:if>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "if";
	expectedReplacedSource = "if";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:if>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0059(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"     swi" +
		"  }\n" +
		"}\n";

	String completeBehind = "swi";
	int cursorLocation = str.lastIndexOf("swi") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:swi>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "swi";
	expectedReplacedSource = "swi";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:swi>;\n" +
		"  }\n" +
		"}\n";
	
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
		"class X extends <CompleteOnKeyword:impl> {\n" +
		"  X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"class X extends <CompleteOnKeyword:impl> {\n" +
		"  X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"class X extends <CompleteOnKeyword:impl> {\n" +
		"  {\n" +
		"  }\n" +
		"  X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"class X extends <CompleteOnKeyword:impl> {\n" +
		"  X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"class X implements Y {\n" +
		"  X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"package p;\n" +
		"import p2.Y;\n" +
		"impo";

	String completeBehind = "impo";
	int cursorLocation = str.lastIndexOf("impo") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:impo>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "impo";
	String expectedReplacedSource = "impo";
	String expectedUnitDisplayString =
		"package p;\n" +
		"import p2.Y;\n" +
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
		"public class X {\n" +
		"  int\n" +
		"}";

	String completeBehind = "int";
	int cursorLocation = str.lastIndexOf("int") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:int>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "int";
	String expectedReplacedSource = "int";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:int>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X {\n" +
		"  public int\n" +
		"}";

	String completeBehind = "int";
	int cursorLocation = str.lastIndexOf("int") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:int>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "int";
	String expectedReplacedSource = "int";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:int>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X {\n" +
		"  public abstract int\n" +
		"}";

	String completeBehind = "int";
	int cursorLocation = str.lastIndexOf("int") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:int>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "int";
	String expectedReplacedSource = "int";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:int>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X {\n" +
		"  public abstract int Y\n" +
		"}";

	String completeBehind = "int";
	int cursorLocation = str.lastIndexOf("int") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:int>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "int";
	String expectedReplacedSource = "int";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:int>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
public void test0080(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    int\n" +
		"  }\n" +
		"}";

	String completeBehind = "int";
	int cursorLocation = str.lastIndexOf("int") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:int>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "int";
	expectedReplacedSource = "int";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:int>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0081(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    abstract int\n" +
		"  }\n" +
		"}";

	String completeBehind = "int";
	int cursorLocation = str.lastIndexOf("int") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:int>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "int";
	expectedReplacedSource = "int";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:int>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0082(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    abstract int Y\n" +
		"  }\n" +
		"}";

	String completeBehind = "int";
	int cursorLocation = str.lastIndexOf("int") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:int>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "int";
	expectedReplacedSource = "int";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:int>;\n" +
		"  }\n" +
		"}\n";
	
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
		"import <CompleteOnKeyword:pac>;\n" + 
		"class X {\n" + 
		"  X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
public void test0090(){
	String str =
		"public class X {\n" +
		"  int foo() {\n" +
		"    ret\n" +
		"  }\n" +
		"}";

	String completeBehind = "ret";
	int cursorLocation = str.lastIndexOf("ret") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:ret>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "ret";
	expectedReplacedSource = "ret";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"    <CompleteOnName:ret>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0091(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    thr\n" +
		"  }\n" +
		"}";

	String completeBehind = "thr";
	int cursorLocation = str.lastIndexOf("thr") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:thr>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "thr";
	expectedReplacedSource = "thr";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:thr>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0092(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    try\n" +
		"  }\n" +
		"}";

	String completeBehind = "try";
	int cursorLocation = str.lastIndexOf("try") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:try>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "try";
	expectedReplacedSource = "try";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:try>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0093(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    if(try\n" +
		"  }\n" +
		"}";

	String completeBehind = "try";
	int cursorLocation = str.lastIndexOf("try") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:try>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "try";
	expectedReplacedSource = "try";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:try>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0094(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    if(do\n" +
		"  }\n" +
		"}";

	String completeBehind = "do";
	int cursorLocation = str.lastIndexOf("do") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:do>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "do";
	expectedReplacedSource = "do";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:do>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0095(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    if(for\n" +
		"  }\n" +
		"}";

	String completeBehind = "for";
	int cursorLocation = str.lastIndexOf("for") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:for>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "for";
	expectedReplacedSource = "for";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:for>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0096(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    if(if\n" +
		"  }\n" +
		"}";

	String completeBehind = "if";
	int cursorLocation = str.lastIndexOf("if") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:if>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "if";
	expectedReplacedSource = "if";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:if>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0097(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    if(swi\n" +
		"  }\n" +
		"}";

	String completeBehind = "swi";
	int cursorLocation = str.lastIndexOf("swi") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:swi>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "swi";
	expectedReplacedSource = "swi";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:swi>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0098(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    new\n" +
		"  }\n" +
		"}";

	String completeBehind = "new";
	int cursorLocation = str.lastIndexOf("new") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:new>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "new";
	expectedReplacedSource = "new";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:new>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0099(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    new X\n" +
		"  }\n" +
		"}";

	String completeBehind = "new";
	int cursorLocation = str.lastIndexOf("new") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:new>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "new";
	expectedReplacedSource = "new";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:new>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0100(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    new X()\n" +
		"  }\n" +
		"}";

	String completeBehind = "new";
	int cursorLocation = str.lastIndexOf("new") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:new>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "new";
	expectedReplacedSource = "new";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:new>;\n" +
		"  }\n" +
		"}\n";
	
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
		"public class X {\n" +
		"  void foo() thr\n" +
		"  }\n" +
		"}";

	String completeBehind = "thr";
	int cursorLocation = str.lastIndexOf("thr") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:thr>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "thr";
	String expectedReplacedSource = "thr";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() throws <CompleteOnKeyword:thr> {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X {\n" +
		"  void foo() thr {\n" +
		"  }\n" +
		"}";

	String completeBehind = "thr";
	int cursorLocation = str.lastIndexOf("thr") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:thr>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "thr";
	String expectedReplacedSource = "thr";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() throws <CompleteOnKeyword:thr> {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X {\n" +
		"  void foo() thr E {\n" +
		"  }\n" +
		"}";

	String completeBehind = "thr";
	int cursorLocation = str.lastIndexOf("thr") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:thr>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "thr";
	String expectedReplacedSource = "thr";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() throws <CompleteOnKeyword:thr> {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X {\n" +
		"  void foo() throws E thr\n" +
		"  }\n" +
		"}";

	String completeBehind = "thr";
	int cursorLocation = str.lastIndexOf("thr") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "thr";
	String expectedReplacedSource = "thr";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() throws E {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X {\n" +
		"  X() thr\n" +
		"  }\n" +
		"}";

	String completeBehind = "thr";
	int cursorLocation = str.lastIndexOf("thr") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:thr>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "thr";
	String expectedReplacedSource = "thr";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  X() throws <CompleteOnKeyword:thr> {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X {\n" +
		"  int foo()[] thr\n" +
		"  }\n" +
		"}";

	String completeBehind = "thr";
	int cursorLocation = str.lastIndexOf("thr") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:thr>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "thr";
	String expectedReplacedSource = "thr";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  int[] foo() throws <CompleteOnKeyword:thr> {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
public void test0107(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    whi\n" +
		"  }\n" +
		"}";

	String completeBehind = "whi";
	int cursorLocation = str.lastIndexOf("whi") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:whi>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "whi";
	expectedReplacedSource = "whi";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:whi>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0108(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    if(whi\n" +
		"  }\n" +
		"}";

	String completeBehind = "whi";
	int cursorLocation = str.lastIndexOf("whi") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:whi>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "whi";
	expectedReplacedSource = "whi";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:whi>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0109(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    ass\n" +
		"  }\n" +
		"}";

	String completeBehind = "ass";
	int cursorLocation = str.lastIndexOf("ass") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:ass>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "ass";
	expectedReplacedSource = "ass";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:ass>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0110(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    if(ass\n" +
		"  }\n" +
		"}";

	String completeBehind = "ass";
	int cursorLocation = str.lastIndexOf("ass") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:ass>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "ass";
	expectedReplacedSource = "ass";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:ass>;\n" +
		"  }\n" +
		"}\n";
	
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
		"import <CompleteOnKeyword:fin>;\n" +
		"class X {\n" +
		"  X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X {\n" +
		"  fin\n" +
		"}";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:fin>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "fin";
	String expectedReplacedSource = "fin";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:fin>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X {\n" +
		"  public fin\n" +
		"}";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:fin>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "fin";
	String expectedReplacedSource = "fin";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:fin>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X {\n" +
		"  fin zzz\n" +
		"}";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:fin>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "fin";
	String expectedReplacedSource = "fin";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:fin>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X {\n" +
		"  final fin\n" +
		"}";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:fin>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "fin";
	String expectedReplacedSource = "fin";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:fin>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X {\n" +
		"  abstract fin\n" +
		"}";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:fin>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "fin";
	String expectedReplacedSource = "fin";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:fin>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X {\n" +
		"  nat\n" +
		"}";

	String completeBehind = "nat";
	int cursorLocation = str.lastIndexOf("nat") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:nat>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "nat";
	String expectedReplacedSource = "nat";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:nat>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X {\n" +
		"  public nat\n" +
		"}";

	String completeBehind = "nat";
	int cursorLocation = str.lastIndexOf("nat") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:nat>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "nat";
	String expectedReplacedSource = "nat";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:nat>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X {\n" +
		"  transient nat\n" +
		"}";

	String completeBehind = "nat";
	int cursorLocation = str.lastIndexOf("nat") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:nat>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "nat";
	String expectedReplacedSource = "nat";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:nat>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X {\n" +
		"  transient nat\n" +
		"}";

	String completeBehind = "nat";
	int cursorLocation = str.lastIndexOf("nat") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:nat>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "nat";
	String expectedReplacedSource = "nat";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:nat>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X {\n" +
		"  volatile nat\n" +
		"}";

	String completeBehind = "nat";
	int cursorLocation = str.lastIndexOf("nat") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:nat>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "nat";
	String expectedReplacedSource = "nat";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:nat>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X {\n" +
		"  str\n" +
		"}";

	String completeBehind = "str";
	int cursorLocation = str.lastIndexOf("str") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:str>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "str";
	String expectedReplacedSource = "str";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:str>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X {\n" +
		"  public str\n" +
		"}";

	String completeBehind = "str";
	int cursorLocation = str.lastIndexOf("str") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:str>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "str";
	String expectedReplacedSource = "str";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:str>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X {\n" +
		"  transient str\n" +
		"}";

	String completeBehind = "str";
	int cursorLocation = str.lastIndexOf("str") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:str>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "str";
	String expectedReplacedSource = "str";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:str>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X {\n" +
		"  transient str\n" +
		"}";

	String completeBehind = "str";
	int cursorLocation = str.lastIndexOf("str") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:str>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "str";
	String expectedReplacedSource = "str";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:str>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X {\n" +
		"  volatile str\n" +
		"}";

	String completeBehind = "str";
	int cursorLocation = str.lastIndexOf("str") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:str>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "str";
	String expectedReplacedSource = "str";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:str>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X {\n" +
		"  vol\n" +
		"}";

	String completeBehind = "vol";
	int cursorLocation = str.lastIndexOf("vol") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:vol>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "vol";
	String expectedReplacedSource = "vol";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:vol>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X {\n" +
		"  public vol\n" +
		"}";

	String completeBehind = "vol";
	int cursorLocation = str.lastIndexOf("vol") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:vol>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "vol";
	String expectedReplacedSource = "vol";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:vol>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X {\n" +
		"  transient vol\n" +
		"}";

	String completeBehind = "vol";
	int cursorLocation = str.lastIndexOf("vol") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:vol>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "vol";
	String expectedReplacedSource = "vol";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:vol>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X {\n" +
		"  volatile vol\n" +
		"}";

	String completeBehind = "vol";
	int cursorLocation = str.lastIndexOf("vol") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:vol>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "vol";
	String expectedReplacedSource = "vol";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:vol>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X {\n" +
		"  native vol\n" +
		"}";

	String completeBehind = "vol";
	int cursorLocation = str.lastIndexOf("vol") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:vol>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "vol";
	String expectedReplacedSource = "vol";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:vol>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X {\n" +
		"  tra\n" +
		"}";

	String completeBehind = "tra";
	int cursorLocation = str.lastIndexOf("tra") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:tra>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "tra";
	String expectedReplacedSource = "tra";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:tra>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X {\n" +
		"  public tra\n" +
		"}";

	String completeBehind = "tra";
	int cursorLocation = str.lastIndexOf("tra") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:tra>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "tra";
	String expectedReplacedSource = "tra";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:tra>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X {\n" +
		"  transient tra\n" +
		"}";

	String completeBehind = "tra";
	int cursorLocation = str.lastIndexOf("tra") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:tra>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "tra";
	String expectedReplacedSource = "tra";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:tra>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X {\n" +
		"  volatile tra\n" +
		"}";

	String completeBehind = "tra";
	int cursorLocation = str.lastIndexOf("tra") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:tra>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "tra";
	String expectedReplacedSource = "tra";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:tra>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X {\n" +
		"  native tra\n" +
		"}";

	String completeBehind = "tra";
	int cursorLocation = str.lastIndexOf("tra") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:tra>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "tra";
	String expectedReplacedSource = "tra";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:tra>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X {\n" +
		"  syn\n" +
		"}";

	String completeBehind = "syn";
	int cursorLocation = str.lastIndexOf("syn") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:syn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "syn";
	String expectedReplacedSource = "syn";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:syn>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X {\n" +
		"  public syn\n" +
		"}";

	String completeBehind = "syn";
	int cursorLocation = str.lastIndexOf("syn") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:syn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "syn";
	String expectedReplacedSource = "syn";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:syn>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X {\n" +
		"  transient syn\n" +
		"}";

	String completeBehind = "syn";
	int cursorLocation = str.lastIndexOf("syn") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:syn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "syn";
	String expectedReplacedSource = "syn";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:syn>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X {\n" +
		"  transient syn\n" +
		"}";

	String completeBehind = "syn";
	int cursorLocation = str.lastIndexOf("syn") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:syn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "syn";
	String expectedReplacedSource = "syn";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:syn>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X {\n" +
		"  volatile syn\n" +
		"}";

	String completeBehind = "syn";
	int cursorLocation = str.lastIndexOf("syn") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:syn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "syn";
	String expectedReplacedSource = "syn";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:syn>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
public void test0147(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    syn\n" +
		"  }\n" +
		"}";

	String completeBehind = "syn";
	int cursorLocation = str.lastIndexOf("syn") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:syn>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "syn";
	expectedReplacedSource = "syn";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:syn>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0148(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    if(syn\n" +
		"  }\n" +
		"}";

	String completeBehind = "syn";
	int cursorLocation = str.lastIndexOf("syn") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:syn>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "syn";
	expectedReplacedSource = "syn";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:syn>;\n" +
		"  }\n" +
		"}\n";
	
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
		"public class X {\n" +
		"  sta\n" +
		"}";

	String completeBehind = "sta";
	int cursorLocation = str.lastIndexOf("sta") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:sta>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "sta";
	String expectedReplacedSource = "sta";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:sta>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X {\n" +
		"  public sta\n" +
		"}";

	String completeBehind = "sta";
	int cursorLocation = str.lastIndexOf("sta") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:sta>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "sta";
	String expectedReplacedSource = "sta";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:sta>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"import <CompleteOnKeyword:pub>;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X{\n" +
		"  pub\n" +
		"}";

	String completeBehind = "pub";
	int cursorLocation = str.lastIndexOf("pub") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pub>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pub";
	String expectedReplacedSource = "pub";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:pub>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X{\n" +
		"  public pub\n" +
		"}";

	String completeBehind = "pub";
	int cursorLocation = str.lastIndexOf("pub") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pub>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pub";
	String expectedReplacedSource = "pub";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:pub>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X{\n" +
		"  private pub\n" +
		"}";

	String completeBehind = "pub";
	int cursorLocation = str.lastIndexOf("pub") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pub>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pub";
	String expectedReplacedSource = "pub";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:pub>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X{\n" +
		"  protected pub\n" +
		"}";

	String completeBehind = "pub";
	int cursorLocation = str.lastIndexOf("pub") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pub>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pub";
	String expectedReplacedSource = "pub";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:pub>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X{\n" +
		"  abstract pub\n" +
		"}";

	String completeBehind = "pub";
	int cursorLocation = str.lastIndexOf("pub") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pub>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pub";
	String expectedReplacedSource = "pub";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:pub>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"import <CompleteOnKeyword:pro>;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X{\n" +
		"  pro\n" +
		"}";

	String completeBehind = "pro";
	int cursorLocation = str.lastIndexOf("pro") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pro>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pro";
	String expectedReplacedSource = "pro";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:pro>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X{\n" +
		"  public pro\n" +
		"}";

	String completeBehind = "pro";
	int cursorLocation = str.lastIndexOf("pro") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pro>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pro";
	String expectedReplacedSource = "pro";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:pro>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X{\n" +
		"  private pro\n" +
		"}";

	String completeBehind = "pro";
	int cursorLocation = str.lastIndexOf("pro") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pro>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pro";
	String expectedReplacedSource = "pro";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:pro>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X{\n" +
		"  protected pro\n" +
		"}";

	String completeBehind = "pro";
	int cursorLocation = str.lastIndexOf("pro") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pro>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pro";
	String expectedReplacedSource = "pro";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:pro>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X{\n" +
		"  abstract pro\n" +
		"}";

	String completeBehind = "pro";
	int cursorLocation = str.lastIndexOf("pro") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pro>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pro";
	String expectedReplacedSource = "pro";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:pro>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"import <CompleteOnKeyword:pri>;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X{\n" +
		"  pri\n" +
		"}";

	String completeBehind = "pri";
	int cursorLocation = str.lastIndexOf("pri") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pri>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pri";
	String expectedReplacedSource = "pri";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:pri>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X{\n" +
		"  public pri\n" +
		"}";

	String completeBehind = "pri";
	int cursorLocation = str.lastIndexOf("pri") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pri>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pri";
	String expectedReplacedSource = "pri";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:pri>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X{\n" +
		"  private pri\n" +
		"}";

	String completeBehind = "pri";
	int cursorLocation = str.lastIndexOf("pri") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pri>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pri";
	String expectedReplacedSource = "pri";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:pri>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X{\n" +
		"  protected pri\n" +
		"}";

	String completeBehind = "pri";
	int cursorLocation = str.lastIndexOf("pri") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pri>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pri";
	String expectedReplacedSource = "pri";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:pri>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"public class X{\n" +
		"  abstract pri\n" +
		"}";

	String completeBehind = "pri";
	int cursorLocation = str.lastIndexOf("pri") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pri>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pri";
	String expectedReplacedSource = "pri";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:pri>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
public void test0181(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"     sup\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "sup";
	int cursorLocation = str.lastIndexOf("sup") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:sup>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "sup";
	expectedReplacedSource = "sup";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:sup>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0182(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"     thi\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "thi";
	int cursorLocation = str.lastIndexOf("thi") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:thi>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "thi";
	expectedReplacedSource = "thi";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:thi>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0183(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"     tru\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "tru";
	int cursorLocation = str.lastIndexOf("tru") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:tru>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "tru";
	expectedReplacedSource = "tru";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:tru>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0184(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"     fal\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "fal";
	int cursorLocation = str.lastIndexOf("fal") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:fal>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "fal";
	expectedReplacedSource = "fal";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:fal>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0185(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"     nul\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "nul";
	int cursorLocation = str.lastIndexOf("nul") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:nul>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "nul";
	expectedReplacedSource = "nul";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:nul>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0186(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"     if(zzz ins\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "ins";
	int cursorLocation = str.lastIndexOf("ins") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnKeyword:ins>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "ins";
	expectedReplacedSource = "ins";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnKeyword:ins>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0187(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"     ins\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "ins";
	int cursorLocation = str.lastIndexOf("ins") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:ins>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "ins";
	expectedReplacedSource = "ins";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:ins>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0188(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"     if(zzz zzz ins\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "ins";
	int cursorLocation = str.lastIndexOf("ins") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:ins>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "ins";
	expectedReplacedSource = "ins";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    zzz zzz;\n" +
		"    <CompleteOnName:ins>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0189(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    do{\n" +
		"    } whi\n" +
		"  }\n" +
		"}";

	String completeBehind = "whi";
	int cursorLocation = str.lastIndexOf("whi") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnKeyword:whi>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "whi";
	expectedReplacedSource = "whi";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnKeyword:whi>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0190(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    try {\n" +
		"    } catch(E e) {\n" +
		"    } cat\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "cat";
	int cursorLocation = str.lastIndexOf("cat") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:cat>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "cat";
	expectedReplacedSource = "cat";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:cat>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0191(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"     try {" +
		"     } catch(E e) {" +
		"     } fin" +
		"  }\n" +
		"}\n";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:fin>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "fin";
	expectedReplacedSource = "fin";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:fin>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0192(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"     try {" +
		"     } finally {" +
		"     } fin" +
		"  }\n" +
		"}\n";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:fin>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "fin";
	expectedReplacedSource = "fin";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:fin>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0193(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"     X.thi\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "thi";
	int cursorLocation = str.lastIndexOf("thi") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:X.thi>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "thi";
	expectedReplacedSource = "X.thi";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:X.thi>;\n" +
		"  }\n" +
		"}\n";
	
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
		"#\n" +
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
public void test0197(){
	String str =
		"#\n" +
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
public void test0198(){
	String str =
		"#\n" +
		"package p;\n" +
		"import yyy;\n" +
		"abst";

	String completeBehind = "abst";
	int cursorLocation = str.indexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"package p;\n" +
		"import yyy;\n" +
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
public void test0199(){
	String str =
		"#\n" +
		"package p;\n" +
		"import yyy;\n" +
		"abst zzz";

	String completeBehind = "abst";
	int cursorLocation = str.indexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"package p;\n" +
		"import yyy;\n" +
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
public void test0200(){
	String str =
		"#\n" +
		"package p;\n" +
		"import yyy;\n" +
		"public abst";

	String completeBehind = "abst";
	int cursorLocation = str.indexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"package p;\n" +
		"import yyy;\n" +
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
public void test0201(){
	String str =
		"#\n" +
		"package p;\n" +
		"import yyy;\n" +
		"public abst zzz";

	String completeBehind = "abst";
	int cursorLocation = str.indexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"package p;\n" +
		"import yyy;\n" +
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
public void test0202(){
	String str =
		"#\n" +
		"package p;\n" +
		"import yyy;\n" +
		"abstract abst";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"import yyy;\n" + 
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
public void test0203(){
	String str =
		"#\n" +
		"package p;\n" +
		"import yyy;\n" +
		"abstract abst zzz";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"import yyy;\n" + 
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
public void test0204(){
	String str =
		"#\n" +
		"package p;\n" +
		"import \n" +
		"abst";

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
		"#\n" +
		"package p;\n" +
		"import yyy;\n" +
		"public class X {}\n" +
		"abst";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"package p;\n" +
		"import yyy;\n" +
		"import <CompleteOnKeyword:abst>;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"package p;\n" +
		"import yyy;\n" +
		"public class X {}\n" +
		"abst zzz";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"package p;\n" +
		"import yyy;\n" +
		"import <CompleteOnKeyword:abst>;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"package p;\n" +
		"import yyy;\n" +
		"final abst";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"package p;\n" + 
		"import yyy;\n" + 
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
public void test0208(){
	String str =
		"#\n" +
		"package p;\n" +
		"public class X {\n" +
		"  abst\n" +
		"}\n";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  <CompleteOnType:abst>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"package p;\n" +
		"public class X {\n" +
		"  abst zzz\n" +
		"}\n";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:abst>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  <CompleteOnType:abst>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"package p;\n" +
		"public class X {\n" +
		"  public abst zzz\n" +
		"}\n";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:abst>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  <CompleteOnType:abst>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"package p;\n" +
		"public class X {\n" +
		"  final abst\n" +
		"}\n";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  <CompleteOnType:abst>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"package p;\n" +
		"public class X {\n" +
		"  abstract abst\n" +
		"}\n";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  <CompleteOnType:abst>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"package p;\n" +
		"public class X {\n" +
		"  static abst\n" +
		"}\n";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:abst>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "abst";
	String expectedReplacedSource = "abst";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  <CompleteOnType:abst>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"  <clinit>() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
public void test0214(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    abst\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:abst>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "abst";
	expectedReplacedSource = "abst";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:abst>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0216(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    abst zzz\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "abst";
	int cursorLocation = str.lastIndexOf("abst") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:abst>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "abst";
	expectedReplacedSource = "abst";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:abst>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0217(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    bre\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "bre";
	int cursorLocation = str.lastIndexOf("bre") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:bre>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "bre";
	expectedReplacedSource = "bre";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:bre>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0218(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    for(int i; i < 10; i++) {\n" +
		"      bre\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "bre";
	int cursorLocation = str.lastIndexOf("bre") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:bre>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "bre";
	expectedReplacedSource = "bre";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    int i;\n" +
		"    {\n" +
		"      <CompleteOnName:bre>;\n" +
		"    }\n" +
		"  }\n" +
		"}\n";
	
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
public void test0219(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    cas\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "cas";
	int cursorLocation = str.lastIndexOf("cas") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:cas>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "cas";
	expectedReplacedSource = "cas";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:cas>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0220(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    switch(0) {\n" +
		"      cas\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "cas";
	int cursorLocation = str.lastIndexOf("cas") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnKeyword:cas>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "cas";
	expectedReplacedSource = "cas";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    {\n" +
		"      <CompleteOnKeyword:cas>;\n" +
		"    }\n"+
		"  }\n" +
		"}\n";
	
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
public void test0221(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    cat\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "cat";
	int cursorLocation = str.lastIndexOf("cat") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:cat>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "cat";
	expectedReplacedSource = "cat";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:cat>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0222(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    try {\n" +
		"    } cat\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "cat";
	int cursorLocation = str.lastIndexOf("cat") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnKeyword:cat>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "cat";
	expectedReplacedSource = "cat";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnKeyword:cat>;\n" +
		"  }\n" +
		"}\n";
	
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
		"#\n" +
		"public class X {\n" +
		"  cla\n" +
		"}";

	String completeBehind = "cla";
	int cursorLocation = str.lastIndexOf("cla") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:cla>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "cla";
	String expectedReplacedSource = "cla";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:cla>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X {\n" +
		"  public cla\n" +
		"}";

	String completeBehind = "cla";
	int cursorLocation = str.lastIndexOf("cla") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:cla>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "cla";
	String expectedReplacedSource = "cla";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:cla>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X {\n" +
		"  public final cla\n" +
		"}";

	String completeBehind = "cla";
	int cursorLocation = str.lastIndexOf("cla") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:cla>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "cla";
	String expectedReplacedSource = "cla";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:cla>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X {\n" +
		"  public final cla Y\n" +
		"}";

	String completeBehind = "cla";
	int cursorLocation = str.lastIndexOf("cla") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:cla>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "cla";
	String expectedReplacedSource = "cla";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:cla>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
public void test0231(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    #\n" +
		"    cla\n" +
		"  }\n" +
		"}";

	String completeBehind = "cla";
	int cursorLocation = str.lastIndexOf("cla") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:cla>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "cla";
	expectedReplacedSource = "cla";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:cla>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0232(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    #\n" +
		"    final cla\n" +
		"  }\n" +
		"}";

	String completeBehind = "cla";
	int cursorLocation = str.lastIndexOf("cla") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:cla>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "cla";
	expectedReplacedSource = "cla";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:cla>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0233(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    #\n" +
		"    final cla Y\n" +
		"  }\n" +
		"}";

	String completeBehind = "cla";
	int cursorLocation = str.lastIndexOf("cla") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:cla>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "cla";
	expectedReplacedSource = "cla";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:cla>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0234(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    con\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "con";
	int cursorLocation = str.lastIndexOf("con") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:con>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "con";
	expectedReplacedSource = "con";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:con>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0235(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    for(int i; i < 5; i++) {\n" +
		"      con\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "con";
	int cursorLocation = str.lastIndexOf("con") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:con>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "con";
	expectedReplacedSource = "con";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    int i;\n" +
		"    {\n" +
		"      <CompleteOnName:con>;\n" +
		"    }\n" +
		"  }\n" +
		"}\n";
	
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
public void test0236(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    def\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "def";
	int cursorLocation = str.lastIndexOf("def") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:def>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "def";
	expectedReplacedSource = "def";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:def>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0237(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    switch(0) {\n" +
		"      case 1 : break;\n" +
		"      def\n" +
		"    }\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "def";
	int cursorLocation = str.lastIndexOf("def") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:def>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "def";
	expectedReplacedSource = "def";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    {\n" +
		"      <CompleteOnName:def>;\n" +
		"    }\n" +
		"  }\n" +
		"}\n";
	
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
public void test0238(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    do\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "do";
	int cursorLocation = str.lastIndexOf("do") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:do>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "do";
	expectedReplacedSource = "do";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:do>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0239(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    els\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "els";
	int cursorLocation = str.lastIndexOf("els") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:els>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "els";
	expectedReplacedSource = "els";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:els>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0240(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    if(true) {\n" +
		"    } els\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "els";
	int cursorLocation = str.lastIndexOf("els") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:els>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "els";
	expectedReplacedSource = "els";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:els>;\n" +
		"  }\n" +
		"}\n";
	
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
		"class X extends <CompleteOnKeyword:ext> {\n" +
		"  X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"class X extends <CompleteOnKeyword:ext> {\n" +
		"  X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"class X extends <CompleteOnKeyword:ext> {\n" +
		"  {\n" +
		"  }\n" +
		"  X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"class X extends <CompleteOnKeyword:ext> {\n" +
		"  X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"class X implements Y {\n" +
		"  X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
public void test0249(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    fin" +
		"  }\n" +
		"}\n";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:fin>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "fin";
	expectedReplacedSource = "fin";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:fin>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0250(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    try {" +
		"    } fin" +
		"  }\n" +
		"}\n";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnKeyword:fin>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "fin";
	expectedReplacedSource = "fin";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnKeyword:fin>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0251(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    for" +
		"  }\n" +
		"}\n";

	String completeBehind = "for";
	int cursorLocation = str.lastIndexOf("for") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:for>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "for";
	expectedReplacedSource = "for";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:for>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0252(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    if" +
		"  }\n" +
		"}\n";

	String completeBehind = "if";
	int cursorLocation = str.lastIndexOf("if") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:if>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "if";
	expectedReplacedSource = "if";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:if>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0253(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    swi" +
		"  }\n" +
		"}\n";

	String completeBehind = "swi";
	int cursorLocation = str.lastIndexOf("swi") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:swi>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "swi";
	expectedReplacedSource = "swi";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:swi>;\n" +
		"  }\n" +
		"}\n";
	
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
		"class X extends <CompleteOnKeyword:impl> {\n" +
		"  X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"class X extends <CompleteOnKeyword:impl> {\n" +
		"  X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"class X extends <CompleteOnKeyword:impl> {\n" +
		"  {\n" +
		"  }\n" +
		"  X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"class X extends <CompleteOnKeyword:impl> {\n" +
		"  X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"class X implements Y {\n" +
		"  X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
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
public void test0264(){
	String str =
		"#\n" +
		"package p;\n" +
		"import p2.Y;\n" +
		"impo";

	String completeBehind = "impo";
	int cursorLocation = str.lastIndexOf("impo") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:impo>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "impo";
	String expectedReplacedSource = "impo";
	String expectedUnitDisplayString =
		"package p;\n" +
		"import p2.Y;\n" +
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
		"#\n" +
		"public class X {\n" +
		"  int\n" +
		"}";

	String completeBehind = "int";
	int cursorLocation = str.lastIndexOf("int") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:int>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "int";
	String expectedReplacedSource = "int";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:int>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X {\n" +
		"  public int\n" +
		"}";

	String completeBehind = "int";
	int cursorLocation = str.lastIndexOf("int") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:int>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "int";
	String expectedReplacedSource = "int";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:int>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X {\n" +
		"  public abstract int\n" +
		"}";

	String completeBehind = "int";
	int cursorLocation = str.lastIndexOf("int") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:int>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "int";
	String expectedReplacedSource = "int";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:int>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X {\n" +
		"  public abstract int Y\n" +
		"}";

	String completeBehind = "int";
	int cursorLocation = str.lastIndexOf("int") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:int>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "int";
	String expectedReplacedSource = "int";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:int>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
public void test0274(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    #\n" +
		"    int\n" +
		"  }\n" +
		"}";

	String completeBehind = "int";
	int cursorLocation = str.lastIndexOf("int") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:int>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "int";
	expectedReplacedSource = "int";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:int>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0275(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    #\n" +
		"    abstract int\n" +
		"  }\n" +
		"}";

	String completeBehind = "int";
	int cursorLocation = str.lastIndexOf("int") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:int>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "int";
	expectedReplacedSource = "int";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:int>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0276(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    #\n" +
		"    abstract int Y\n" +
		"  }\n" +
		"}";

	String completeBehind = "int";
	int cursorLocation = str.lastIndexOf("int") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:int>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "int";
	expectedReplacedSource = "int";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:int>;\n" +
		"  }\n" +
		"}\n";
	
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
		"#\n" +
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
public void test0282(){
	String str =
		"#\n" +
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
public void test0283(){
	String str =
		"#\n" +
		"class X {}" +
		"pac";

	String completeBehind = "pac";
	int cursorLocation = str.lastIndexOf("pac") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:pac>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pac";
	String expectedReplacedSource = "pac";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:pac>;\n" + 
		"class X {\n" + 
		"  X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
public void test0284(){
	String str =
		"public class X {\n" +
		"  int foo() {\n" +
		"    #\n" +
		"    ret\n" +
		"  }\n" +
		"}";

	String completeBehind = "ret";
	int cursorLocation = str.lastIndexOf("ret") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:ret>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "ret";
	expectedReplacedSource = "ret";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  int foo() {\n" +
		"    <CompleteOnName:ret>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0285(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    #\n" +
		"    thr\n" +
		"  }\n" +
		"}";

	String completeBehind = "thr";
	int cursorLocation = str.lastIndexOf("thr") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:thr>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "thr";
	expectedReplacedSource = "thr";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:thr>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0286(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    #\n" +
		"    try\n" +
		"  }\n" +
		"}";

	String completeBehind = "try";
	int cursorLocation = str.lastIndexOf("try") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:try>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "try";
	expectedReplacedSource = "try";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:try>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0287(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    #\n" +
		"    if(try\n" +
		"  }\n" +
		"}";

	String completeBehind = "try";
	int cursorLocation = str.lastIndexOf("try") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:try>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "try";
	expectedReplacedSource = "try";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:try>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0288(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    #\n" +
		"    if(do\n" +
		"  }\n" +
		"}";

	String completeBehind = "do";
	int cursorLocation = str.lastIndexOf("do") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:do>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "do";
	expectedReplacedSource = "do";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:do>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0289(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    #\n" +
		"    if(for\n" +
		"  }\n" +
		"}";

	String completeBehind = "for";
	int cursorLocation = str.lastIndexOf("for") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:for>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "for";
	expectedReplacedSource = "for";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:for>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0290(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    #\n" +
		"    if(if\n" +
		"  }\n" +
		"}";

	String completeBehind = "if";
	int cursorLocation = str.lastIndexOf("if") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:if>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "if";
	expectedReplacedSource = "if";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:if>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0291(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    #\n" +
		"    if(swi\n" +
		"  }\n" +
		"}";

	String completeBehind = "swi";
	int cursorLocation = str.lastIndexOf("swi") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:swi>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "swi";
	expectedReplacedSource = "swi";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:swi>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0292(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    #\n" +
		"    new\n" +
		"  }\n" +
		"}";

	String completeBehind = "new";
	int cursorLocation = str.lastIndexOf("new") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:new>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "new";
	expectedReplacedSource = "new";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:new>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0293(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    #\n" +
		"    new X\n" +
		"  }\n" +
		"}";

	String completeBehind = "new";
	int cursorLocation = str.lastIndexOf("new") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:new>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "new";
	expectedReplacedSource = "new";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:new>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0294(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    #\n" +
		"    new X()\n" +
		"  }\n" +
		"}";

	String completeBehind = "new";
	int cursorLocation = str.lastIndexOf("new") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:new>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "new";
	expectedReplacedSource = "new";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:new>;\n" +
		"  }\n" +
		"}\n";
	
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
		"#\n" +
		"public class X {\n" +
		"  void foo() thr\n" +
		"  }\n" +
		"}";

	String completeBehind = "thr";
	int cursorLocation = str.lastIndexOf("thr") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:thr>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "thr";
	String expectedReplacedSource = "thr";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() throws <CompleteOnKeyword:thr> {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X {\n" +
		"  void foo() thr {\n" +
		"  }\n" +
		"}";

	String completeBehind = "thr";
	int cursorLocation = str.lastIndexOf("thr") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:thr>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "thr";
	String expectedReplacedSource = "thr";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() throws <CompleteOnKeyword:thr> {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X {\n" +
		"  void foo() thr E {\n" +
		"  }\n" +
		"}";

	String completeBehind = "thr";
	int cursorLocation = str.lastIndexOf("thr") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:thr>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "thr";
	String expectedReplacedSource = "thr";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() throws <CompleteOnKeyword:thr> {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X {\n" +
		"  void foo() throws E thr\n" +
		"  }\n" +
		"}";

	String completeBehind = "thr";
	int cursorLocation = str.lastIndexOf("thr") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "thr";
	String expectedReplacedSource = "thr";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() throws E {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X {\n" +
		"  X() thr\n" +
		"  }\n" +
		"}";

	String completeBehind = "thr";
	int cursorLocation = str.lastIndexOf("thr") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:thr>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "thr";
	String expectedReplacedSource = "thr";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  X() throws <CompleteOnKeyword:thr> {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X {\n" +
		"  int foo()[] thr\n" +
		"  }\n" +
		"}";

	String completeBehind = "thr";
	int cursorLocation = str.lastIndexOf("thr") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:thr>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "thr";
	String expectedReplacedSource = "thr";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  int[] foo() throws <CompleteOnKeyword:thr> {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
public void test0301(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    #\n" +
		"    whi\n" +
		"  }\n" +
		"}";

	String completeBehind = "whi";
	int cursorLocation = str.lastIndexOf("whi") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:whi>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "whi";
	expectedReplacedSource = "whi";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:whi>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0302(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    #\n" +
		"    if(whi\n" +
		"  }\n" +
		"}";

	String completeBehind = "whi";
	int cursorLocation = str.lastIndexOf("whi") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:whi>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "whi";
	expectedReplacedSource = "whi";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:whi>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0303(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    #\n" +
		"    ass\n" +
		"  }\n" +
		"}";

	String completeBehind = "ass";
	int cursorLocation = str.lastIndexOf("ass") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:ass>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "ass";
	expectedReplacedSource = "ass";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:ass>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0304(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    #\n" +
		"    if(ass\n" +
		"  }\n" +
		"}";

	String completeBehind = "ass";
	int cursorLocation = str.lastIndexOf("ass") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:ass>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "ass";
	expectedReplacedSource = "ass";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:ass>;\n" +
		"  }\n" +
		"}\n";
	
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
		"import <CompleteOnKeyword:fin>;\n" +
		"class X {\n" +
		"  X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X {\n" +
		"  fin\n" +
		"}";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:fin>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "fin";
	String expectedReplacedSource = "fin";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:fin>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X {\n" +
		"  public fin\n" +
		"}";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:fin>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "fin";
	String expectedReplacedSource = "fin";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:fin>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X {\n" +
		"  fin zzz\n" +
		"}";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:fin>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "fin";
	String expectedReplacedSource = "fin";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:fin>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X {\n" +
		"  final fin\n" +
		"}";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:fin>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "fin";
	String expectedReplacedSource = "fin";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:fin>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X {\n" +
		"  abstract fin\n" +
		"}";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:fin>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "fin";
	String expectedReplacedSource = "fin";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:fin>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X {\n" +
		"  nat\n" +
		"}";

	String completeBehind = "nat";
	int cursorLocation = str.lastIndexOf("nat") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:nat>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "nat";
	String expectedReplacedSource = "nat";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:nat>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X {\n" +
		"  public nat\n" +
		"}";

	String completeBehind = "nat";
	int cursorLocation = str.lastIndexOf("nat") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:nat>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "nat";
	String expectedReplacedSource = "nat";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:nat>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X {\n" +
		"  transient nat\n" +
		"}";

	String completeBehind = "nat";
	int cursorLocation = str.lastIndexOf("nat") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:nat>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "nat";
	String expectedReplacedSource = "nat";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:nat>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X {\n" +
		"  transient nat\n" +
		"}";

	String completeBehind = "nat";
	int cursorLocation = str.lastIndexOf("nat") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:nat>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "nat";
	String expectedReplacedSource = "nat";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:nat>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X {\n" +
		"  volatile nat\n" +
		"}";

	String completeBehind = "nat";
	int cursorLocation = str.lastIndexOf("nat") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:nat>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "nat";
	String expectedReplacedSource = "nat";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:nat>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X {\n" +
		"  str\n" +
		"}";

	String completeBehind = "str";
	int cursorLocation = str.lastIndexOf("str") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:str>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "str";
	String expectedReplacedSource = "str";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:str>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X {\n" +
		"  public str\n" +
		"}";

	String completeBehind = "str";
	int cursorLocation = str.lastIndexOf("str") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:str>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "str";
	String expectedReplacedSource = "str";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:str>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X {\n" +
		"  transient str\n" +
		"}";

	String completeBehind = "str";
	int cursorLocation = str.lastIndexOf("str") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:str>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "str";
	String expectedReplacedSource = "str";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:str>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X {\n" +
		"  transient str\n" +
		"}";

	String completeBehind = "str";
	int cursorLocation = str.lastIndexOf("str") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:str>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "str";
	String expectedReplacedSource = "str";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:str>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X {\n" +
		"  volatile str\n" +
		"}";

	String completeBehind = "str";
	int cursorLocation = str.lastIndexOf("str") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:str>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "str";
	String expectedReplacedSource = "str";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:str>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X {\n" +
		"  vol\n" +
		"}";

	String completeBehind = "vol";
	int cursorLocation = str.lastIndexOf("vol") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:vol>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "vol";
	String expectedReplacedSource = "vol";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:vol>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X {\n" +
		"  public vol\n" +
		"}";

	String completeBehind = "vol";
	int cursorLocation = str.lastIndexOf("vol") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:vol>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "vol";
	String expectedReplacedSource = "vol";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:vol>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X {\n" +
		"  transient vol\n" +
		"}";

	String completeBehind = "vol";
	int cursorLocation = str.lastIndexOf("vol") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:vol>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "vol";
	String expectedReplacedSource = "vol";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:vol>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X {\n" +
		"  volatile vol\n" +
		"}";

	String completeBehind = "vol";
	int cursorLocation = str.lastIndexOf("vol") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:vol>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "vol";
	String expectedReplacedSource = "vol";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:vol>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X {\n" +
		"  native vol\n" +
		"}";

	String completeBehind = "vol";
	int cursorLocation = str.lastIndexOf("vol") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:vol>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "vol";
	String expectedReplacedSource = "vol";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:vol>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X {\n" +
		"  tra\n" +
		"}";

	String completeBehind = "tra";
	int cursorLocation = str.lastIndexOf("tra") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:tra>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "tra";
	String expectedReplacedSource = "tra";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:tra>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X {\n" +
		"  public tra\n" +
		"}";

	String completeBehind = "tra";
	int cursorLocation = str.lastIndexOf("tra") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:tra>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "tra";
	String expectedReplacedSource = "tra";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:tra>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X {\n" +
		"  transient tra\n" +
		"}";

	String completeBehind = "tra";
	int cursorLocation = str.lastIndexOf("tra") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:tra>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "tra";
	String expectedReplacedSource = "tra";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:tra>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X {\n" +
		"  volatile tra\n" +
		"}";

	String completeBehind = "tra";
	int cursorLocation = str.lastIndexOf("tra") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:tra>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "tra";
	String expectedReplacedSource = "tra";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:tra>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X {\n" +
		"  native tra\n" +
		"}";

	String completeBehind = "tra";
	int cursorLocation = str.lastIndexOf("tra") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:tra>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "tra";
	String expectedReplacedSource = "tra";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:tra>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X {\n" +
		"  syn\n" +
		"}";

	String completeBehind = "syn";
	int cursorLocation = str.lastIndexOf("syn") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:syn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "syn";
	String expectedReplacedSource = "syn";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:syn>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X {\n" +
		"  public syn\n" +
		"}";

	String completeBehind = "syn";
	int cursorLocation = str.lastIndexOf("syn") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:syn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "syn";
	String expectedReplacedSource = "syn";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:syn>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X {\n" +
		"  transient syn\n" +
		"}";

	String completeBehind = "syn";
	int cursorLocation = str.lastIndexOf("syn") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:syn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "syn";
	String expectedReplacedSource = "syn";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:syn>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X {\n" +
		"  transient syn\n" +
		"}";

	String completeBehind = "syn";
	int cursorLocation = str.lastIndexOf("syn") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:syn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "syn";
	String expectedReplacedSource = "syn";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:syn>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X {\n" +
		"  volatile syn\n" +
		"}";

	String completeBehind = "syn";
	int cursorLocation = str.lastIndexOf("syn") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:syn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "syn";
	String expectedReplacedSource = "syn";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:syn>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
public void test0341(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    #\n" +
		"    syn\n" +
		"  }\n" +
		"}";

	String completeBehind = "syn";
	int cursorLocation = str.lastIndexOf("syn") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:syn>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "syn";
	expectedReplacedSource = "syn";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:syn>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0342(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    #\n" +
		"    if(syn\n" +
		"  }\n" +
		"}";

	String completeBehind = "syn";
	int cursorLocation = str.lastIndexOf("syn") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:syn>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "syn";
	expectedReplacedSource = "syn";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:syn>;\n" +
		"  }\n" +
		"}\n";
	
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
		"#\n" +
		"public class X {\n" +
		"  sta\n" +
		"}";

	String completeBehind = "sta";
	int cursorLocation = str.lastIndexOf("sta") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:sta>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "sta";
	String expectedReplacedSource = "sta";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:sta>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X {\n" +
		"  public sta\n" +
		"}";

	String completeBehind = "sta";
	int cursorLocation = str.lastIndexOf("sta") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:sta>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "sta";
	String expectedReplacedSource = "sta";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:sta>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X{}\n" +
		"pub";

	String completeBehind = "pub";
	int cursorLocation = str.lastIndexOf("pub") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:pub>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pub";
	String expectedReplacedSource = "pub";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:pub>;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X{\n" +
		"  pub\n" +
		"}";

	String completeBehind = "pub";
	int cursorLocation = str.lastIndexOf("pub") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pub>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pub";
	String expectedReplacedSource = "pub";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:pub>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X{\n" +
		"  public pub\n" +
		"}";

	String completeBehind = "pub";
	int cursorLocation = str.lastIndexOf("pub") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pub>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pub";
	String expectedReplacedSource = "pub";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:pub>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X{\n" +
		"  private pub\n" +
		"}";

	String completeBehind = "pub";
	int cursorLocation = str.lastIndexOf("pub") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pub>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pub";
	String expectedReplacedSource = "pub";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:pub>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X{\n" +
		"  protected pub\n" +
		"}";

	String completeBehind = "pub";
	int cursorLocation = str.lastIndexOf("pub") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pub>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pub";
	String expectedReplacedSource = "pub";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:pub>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X{\n" +
		"  abstract pub\n" +
		"}";

	String completeBehind = "pub";
	int cursorLocation = str.lastIndexOf("pub") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pub>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pub";
	String expectedReplacedSource = "pub";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:pub>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X{}\n" +
		"pro";

	String completeBehind = "pro";
	int cursorLocation = str.lastIndexOf("pro") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:pro>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pro";
	String expectedReplacedSource = "pro";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:pro>;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X{\n" +
		"  pro\n" +
		"}";

	String completeBehind = "pro";
	int cursorLocation = str.lastIndexOf("pro") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pro>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pro";
	String expectedReplacedSource = "pro";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:pro>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X{\n" +
		"  public pro\n" +
		"}";

	String completeBehind = "pro";
	int cursorLocation = str.lastIndexOf("pro") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pro>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pro";
	String expectedReplacedSource = "pro";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:pro>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X{\n" +
		"  private pro\n" +
		"}";

	String completeBehind = "pro";
	int cursorLocation = str.lastIndexOf("pro") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pro>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pro";
	String expectedReplacedSource = "pro";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:pro>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X{\n" +
		"  protected pro\n" +
		"}";

	String completeBehind = "pro";
	int cursorLocation = str.lastIndexOf("pro") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pro>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pro";
	String expectedReplacedSource = "pro";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:pro>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X{\n" +
		"  abstract pro\n" +
		"}";

	String completeBehind = "pro";
	int cursorLocation = str.lastIndexOf("pro") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pro>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pro";
	String expectedReplacedSource = "pro";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:pro>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X{}\n" +
		"pri";

	String completeBehind = "pri";
	int cursorLocation = str.lastIndexOf("pri") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnKeyword:pri>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pri";
	String expectedReplacedSource = "pri";
	String expectedUnitDisplayString =
		"import <CompleteOnKeyword:pri>;\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X{\n" +
		"  pri\n" +
		"}";

	String completeBehind = "pri";
	int cursorLocation = str.lastIndexOf("pri") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pri>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pri";
	String expectedReplacedSource = "pri";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:pri>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X{\n" +
		"  public pri\n" +
		"}";

	String completeBehind = "pri";
	int cursorLocation = str.lastIndexOf("pri") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pri>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pri";
	String expectedReplacedSource = "pri";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:pri>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X{\n" +
		"  private pri\n" +
		"}";

	String completeBehind = "pri";
	int cursorLocation = str.lastIndexOf("pri") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pri>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pri";
	String expectedReplacedSource = "pri";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:pri>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X{\n" +
		"  protected pri\n" +
		"}";

	String completeBehind = "pri";
	int cursorLocation = str.lastIndexOf("pri") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pri>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pri";
	String expectedReplacedSource = "pri";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:pri>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
		"#\n" +
		"public class X{\n" +
		"  abstract pri\n" +
		"}";

	String completeBehind = "pri";
	int cursorLocation = str.lastIndexOf("pri") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<CompleteOnType:pri>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "pri";
	String expectedReplacedSource = "pri";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  <CompleteOnType:pri>;\n" +
		"  public X() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
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
public void test0375(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    sup\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "sup";
	int cursorLocation = str.lastIndexOf("sup") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:sup>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "sup";
	expectedReplacedSource = "sup";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:sup>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0376(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    thi\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "thi";
	int cursorLocation = str.lastIndexOf("thi") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:thi>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "thi";
	expectedReplacedSource = "thi";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:thi>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0377(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    tru\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "tru";
	int cursorLocation = str.lastIndexOf("tru") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:tru>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "tru";
	expectedReplacedSource = "tru";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:tru>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0378(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    fal\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "fal";
	int cursorLocation = str.lastIndexOf("fal") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:fal>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "fal";
	expectedReplacedSource = "fal";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:fal>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0379(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    nul\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "nul";
	int cursorLocation = str.lastIndexOf("nul") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:nul>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "nul";
	expectedReplacedSource = "nul";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:nul>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0380(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    if(zzz ins\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "ins";
	int cursorLocation = str.lastIndexOf("ins") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnKeyword:ins>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "ins";
	expectedReplacedSource = "ins";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnKeyword:ins>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0381(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    ins\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "ins";
	int cursorLocation = str.lastIndexOf("ins") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:ins>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "ins";
	expectedReplacedSource = "ins";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:ins>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0382(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    if(zzz zzz ins\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "ins";
	int cursorLocation = str.lastIndexOf("ins") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:ins>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "ins";
	expectedReplacedSource = "ins";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    zzz zzz;\n" +
		"    <CompleteOnName:ins>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0384(){
	String str =
		"public class X {\n" +
		"  void foo() {\n" +
		"    #\n" +
		"    do{\n" +
		"    } whi\n" +
		"  }\n" +
		"}";

	String completeBehind = "whi";
	int cursorLocation = str.lastIndexOf("whi") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnKeyword:whi>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "whi";
	expectedReplacedSource = "whi";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnKeyword:whi>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0385(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    try {\n" +
		"    } catch(E e) {\n" +
		"    } cat\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "cat";
	int cursorLocation = str.lastIndexOf("cat") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:cat>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "cat";
	expectedReplacedSource = "cat";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:cat>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0386(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    try {" +
		"    } catch(E e) {" +
		"    } fin" +
		"  }\n" +
		"}\n";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:fin>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "fin";
	expectedReplacedSource = "fin";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:fin>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0387(){
	String str =
		"package p;\n" +
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    try {" +
		"    } finally {" +
		"    } fin" +
		"  }\n" +
		"}\n";

	String completeBehind = "fin";
	int cursorLocation = str.lastIndexOf("fin") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:fin>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "fin";
	expectedReplacedSource = "fin";
	expectedUnitDisplayString =
		"package p;\n" +
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:fin>;\n" +
		"  }\n" +
		"}\n";
	
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
public void test0388(){
	String str =
		"public class X {\n" +
		"  void foo(){\n" +
		"    #\n" +
		"    X.thi\n" +
		"  }\n" +
		"}\n";

	String completeBehind = "thi";
	int cursorLocation = str.lastIndexOf("thi") + completeBehind.length() - 1;

	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnName:X.thi>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "thi";
	expectedReplacedSource = "X.thi";
	expectedUnitDisplayString =
		"public class X {\n" +
		"  public X() {\n" +
		"  }\n" +
		"  void foo() {\n" +
		"    <CompleteOnName:X.thi>;\n" +
		"  }\n" +
		"}\n";
	
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
