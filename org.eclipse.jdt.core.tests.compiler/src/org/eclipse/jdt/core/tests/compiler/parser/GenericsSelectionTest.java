/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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
		"public class X {		\n" + 
		"  Z<Object> z;								\n" +
		"}											\n"; 

	String selection = "Z";
	
	String expectedCompletionNodeToString = "<SelectOnType:Z<Object>>";
	
	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  <SelectOnType:Z<Object>> z;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	String expectedReplacedSource = "Z";
	String testName = "<select type>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;
		
	this.checkDietParse(
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
		"public class X {		\n" + 
		"  void foo(){;								\n" +
		"    Z<Object> z;								\n" +
		"  }           								\n" +
		"}											\n"; 

	String selection = "Z";
	
	String expectedCompletionNodeToString = "<SelectOnType:Z<Object>>";
	
	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    <SelectOnType:Z<Object>> z;\n" + 
		"  }\n" + 
		"}\n";
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
		"public class X {		\n" + 
		"  Y.Z<Object> z;								\n" +
		"}											\n"; 

	String selection = "Z";
	
	String expectedCompletionNodeToString = "<SelectOnType:Y.Z<Object>>";
	
	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  <SelectOnType:Y.Z<Object>> z;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	String expectedReplacedSource = "Y.Z";
	String testName = "<select type>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;
		
	this.checkDietParse(
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
		"public class X {		\n" + 
		"  void foo(){;								\n" +
		"    Y.Z<Object> z;								\n" +
		"  }           								\n" +
		"}											\n"; 

	String selection = "Z";
	
	String expectedCompletionNodeToString = "<SelectOnType:Y.Z<Object>>";
	
	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    <SelectOnType:Y.Z<Object>> z;\n" + 
		"  }\n" + 
		"}\n";
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
		"public class X {		\n" + 
		"  Y<Object>.Z z;								\n" +
		"}											\n"; 

	String selection = "Z";
	
	String expectedCompletionNodeToString = "<SelectOnType:Y<Object>.Z>";
	
	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  <SelectOnType:Y<Object>.Z> z;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	String expectedReplacedSource = "Y<Object>.Z";
	String testName = "<select type>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;
		
	this.checkDietParse(
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
		"public class X {		\n" + 
		"  void foo(){;								\n" +
		"    Y<Object>.Z z;								\n" +
		"  }           								\n" +
		"}											\n"; 

	String selection = "Z";
	
	String expectedCompletionNodeToString = "<SelectOnType:Y<Object>.Z>";
	
	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    <SelectOnType:Y<Object>.Z> z;\n" + 
		"  }\n" + 
		"}\n";
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
		"public class X {		\n" + 
		"  Y<Object>.Z<Object> z;								\n" +
		"}											\n"; 

	String selection = "Z";
	
	String expectedCompletionNodeToString = "<SelectOnType:Y<Object>.Z<Object>>";
	
	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  <SelectOnType:Y<Object>.Z<Object>> z;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	String expectedReplacedSource = "Y<Object>.Z";
	String testName = "<select type>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;
		
	this.checkDietParse(
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
		"public class X {		\n" + 
		"  void foo(){;								\n" +
		"    Y<Object>.Z<Object> z;								\n" +
		"  }           								\n" +
		"}											\n"; 

	String selection = "Z";
	
	String expectedCompletionNodeToString = "<SelectOnType:Y<Object>.Z<Object>>";
	
	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    <SelectOnType:Y<Object>.Z<Object>> z;\n" + 
		"  }\n" + 
		"}\n";
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
		"public class X {		\n" + 
		"  Z<Object> z;								\n" +
		"}											\n"; 

	String selection = "Z";
	
	String expectedCompletionNodeToString = "<SelectOnType:Z<Object>>";
	
	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  <SelectOnType:Z<Object>> z;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	String expectedReplacedSource = "Z";
	String testName = "<select type>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;
		
	this.checkDietParse(
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
		"public class X {		\n" + 
		"  void foo(){;								\n" +
		"    Z<Object> z;								\n" +
		"  }           								\n" +
		"}											\n"; 

	String selection = "Z";
	
	String expectedCompletionNodeToString = "<SelectOnType:Z<Object>>";
	
	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    <SelectOnType:Z<Object>> z;\n" + 
		"  }\n" + 
		"}\n";
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
		"public class X {		\n" + 
		"  Y.Z<Object> z;								\n" +
		"}											\n"; 

	String selection = "Z";
	
	String expectedCompletionNodeToString = "<SelectOnType:Y.Z<Object>>";
	
	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  <SelectOnType:Y.Z<Object>> z;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	String expectedReplacedSource = "Y.Z";
	String testName = "<select type>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;
		
	this.checkDietParse(
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
		"public class X {		\n" + 
		"  void foo(){;								\n" +
		"    Y.Z<Object> z;								\n" +
		"  }           								\n" +
		"}											\n"; 

	String selection = "Z";
	
	String expectedCompletionNodeToString = "<SelectOnType:Y.Z<Object>>";
	
	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    <SelectOnType:Y.Z<Object>> z;\n" + 
		"  }\n" + 
		"}\n";
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
		"public class X {		\n" + 
		"  Y<Object>.Z z;								\n" +
		"}											\n"; 

	String selection = "Z";
	
	String expectedCompletionNodeToString = "<SelectOnType:Y<Object>.Z>";
	
	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  <SelectOnType:Y<Object>.Z> z;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	String expectedReplacedSource = "Y<Object>.Z";
	String testName = "<select type>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;
		
	this.checkDietParse(
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
		"public class X {		\n" + 
		"  void foo(){;								\n" +
		"    Y<Object>.Z z;								\n" +
		"  }           								\n" +
		"}											\n"; 

	String selection = "Z";
	
	String expectedCompletionNodeToString = "<SelectOnType:Y<Object>.Z>";
	
	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    <SelectOnType:Y<Object>.Z> z;\n" + 
		"  }\n" + 
		"}\n";
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
		"public class X {		\n" + 
		"  Y<Object>.Z<Object> z;								\n" +
		"}											\n"; 

	String selection = "Z";
	
	String expectedCompletionNodeToString = "<SelectOnType:Y<Object>.Z<Object>>";
	
	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  <SelectOnType:Y<Object>.Z<Object>> z;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	String expectedReplacedSource = "Y<Object>.Z";
	String testName = "<select type>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;
		
	this.checkDietParse(
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
		"public class X {		\n" + 
		"  void foo(){;								\n" +
		"    Y<Object>.Z<Object> z;								\n" +
		"  }           								\n" +
		"}											\n"; 

	String selection = "Z";
	
	String expectedCompletionNodeToString = "<SelectOnType:Y<Object>.Z<Object>>";
	
	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    <SelectOnType:Y<Object>.Z<Object>> z;\n" + 
		"  }\n" + 
		"}\n";
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
public void _test000X() {

	String str =
		"public class X {		\n" + 
		"  Y.Z z;								\n" +
		"}											\n"; 

	String selection = "Z";
	
	String expectedCompletionNodeToString = "<SelectOnType:Y.Z>";
	
	String completionIdentifier = "Z";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  <SelectOnType:Y.Z> z;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";
	String expectedReplacedSource = "Y.Z";
	String testName = "<select type>";

	int selectionStart = str.lastIndexOf(selection);
	int selectionEnd = str.lastIndexOf(selection) + selection.length() - 1;
		
	this.checkDietParse(
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
