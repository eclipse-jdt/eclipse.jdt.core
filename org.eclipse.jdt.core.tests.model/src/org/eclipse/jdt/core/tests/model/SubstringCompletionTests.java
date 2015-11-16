/*******************************************************************************
 * Copyright (c) 2015 Gábor Kövesdán and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Gábor Kövesdán - initial version
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.Hashtable;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import junit.framework.Test;
import junit.framework.TestSuite;

public class SubstringCompletionTests extends AbstractJavaModelCompletionTests {

public static Test suite() {
	if (TESTS_PREFIX != null || TESTS_NAMES != null || TESTS_NUMBERS != null || TESTS_RANGE != null) {
		return buildModelTestSuite(SubstringCompletionTests.class);
	}
	TestSuite suite = new Suite(SubstringCompletionTests.class.getName());
	suite.addTest(new SubstringCompletionTests("testQualifiedNonStaticMethod"));
	suite.addTest(new SubstringCompletionTests("testQualifiedStaticMethod"));
	suite.addTest(new SubstringCompletionTests("testUnqualifiedNonStaticMethod"));
	suite.addTest(new SubstringCompletionTests("testUnqualifiedStaticMethod"));
	suite.addTest(new SubstringCompletionTests("testQualifiedNonStaticField"));
	suite.addTest(new SubstringCompletionTests("testQualifiedStaticField"));
	suite.addTest(new SubstringCompletionTests("testUnqualifiedNonStaticField"));
	suite.addTest(new SubstringCompletionTests("testUnqualifiedStaticField"));
	suite.addTest(new SubstringCompletionTests("testLocalVariable"));
	suite.addTest(new SubstringCompletionTests("testMethodParamVariable"));
	suite.addTest(new SubstringCompletionTests("testClassTypeInstantiation"));
	suite.addTest(new SubstringCompletionTests("testClassTypeFieldDeclaration"));
	suite.addTest(new SubstringCompletionTests("testClassTypeParamDeclaration"));
	suite.addTest(new SubstringCompletionTests("testClassTypeLocalVarDeclaration"));
	suite.addTest(new SubstringCompletionTests("testClassTypeThrowsDeclaration"));
	suite.addTest(new SubstringCompletionTests("testClassTypeExtends"));
	suite.addTest(new SubstringCompletionTests("testClassTypeImplements"));
	suite.addTest(new SubstringCompletionTests("testInnerClassTypeInstantiation"));
	suite.addTest(new SubstringCompletionTests("testInnerClassTypeFieldDeclaration"));
	suite.addTest(new SubstringCompletionTests("testInnerClassTypeParamDeclaration"));
	suite.addTest(new SubstringCompletionTests("testInnerClassTypeLocalVarDeclaration"));
	suite.addTest(new SubstringCompletionTests("testInnerClassTypeThrowsDeclaration"));
	suite.addTest(new SubstringCompletionTests("testInnerClassTypeExtends"));
	suite.addTest(new SubstringCompletionTests("testInnerClassTypeImplements"));
	suite.addTest(new SubstringCompletionTests("testStaticNestedClassTypeInstantiation"));
	suite.addTest(new SubstringCompletionTests("testStaticNestedClassTypeFieldDeclaration"));
	suite.addTest(new SubstringCompletionTests("testStaticNestedClassTypeParamDeclaration"));
	suite.addTest(new SubstringCompletionTests("testStaticNestedClassTypeLocalVarDeclaration"));
	suite.addTest(new SubstringCompletionTests("testStaticNestedClassTypeThrowsDeclaration"));
	suite.addTest(new SubstringCompletionTests("testStaticNestedClassTypeExtends"));
	suite.addTest(new SubstringCompletionTests("testStaticNestedClassTypeImplements"));
	suite.addTest(new SubstringCompletionTests("testLocalClassTypeInstantiation"));
	suite.addTest(new SubstringCompletionTests("testLocalClassTypeLocalVarDeclaration"));
	suite.addTest(new SubstringCompletionTests("testLocalClassTypeExtends"));
	return suite;
}
public SubstringCompletionTests(String name) {
	super(name);
}
public void setUpSuite() throws Exception {
	if (COMPLETION_PROJECT == null)  {
		COMPLETION_PROJECT = setUpJavaProject("Completion");
	} else {
		setUpProjectCompliance(COMPLETION_PROJECT, "1.8");
	}
	super.setUpSuite();
	Hashtable<String, String> options = new Hashtable<>(this.oldOptions);
	options.put(JavaCore.CODEASSIST_SUBSTRING_MATCH, JavaCore.ENABLED);
	JavaCore.setOptions(options);
}
public void tearDownSuite() throws Exception {
	if (COMPLETION_SUITES == null) {
		deleteProject("Completion");
	} else {
		COMPLETION_SUITES.remove(getClass());
		if (COMPLETION_SUITES.size() == 0) {
			deleteProject("Completion");
			COMPLETION_SUITES = null;
		}
	}
	if (COMPLETION_SUITES == null) {
		COMPLETION_PROJECT = null;
	}
	super.tearDownSuite();
}

public void testQualifiedNonStaticMethod() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"  public Object bar1() {}\n" +
		"  public Zork Bar2() {}\n" +
		"  public void removeBar() {}\n" +
		"  void foo() {\n" +
		"    this.bar\n" +
		"  }\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "this.bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"removeBar[METHOD_REF]{removeBar(), Ltest.Test;, ()V, removeBar, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_STATIC + R_NON_RESTRICTED + R_SUBSTRING) + "}\n" +
			"Bar2[METHOD_REF]{Bar2(), Ltest.Test;, ()LZork;, Bar2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_STATIC + R_NON_RESTRICTED) + "}\n" +
			"bar1[METHOD_REF]{bar1(), Ltest.Test;, ()Ljava.lang.Object;, bar1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}

public void testUnqualifiedNonStaticMethod() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"  public Object bar1() {}\n" +
		"  public Zork Bar2() {}\n" +
		"  public void removeBar() {}\n" +
		"  void foo() {\n" +
		"    bar\n" +
		"  }\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"removeBar[METHOD_REF]{removeBar(), Ltest.Test;, ()V, removeBar, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBSTRING) + "}\n" +
			"Bar2[METHOD_REF]{Bar2(), Ltest.Test;, ()LZork;, Bar2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"bar1[METHOD_REF]{bar1(), Ltest.Test;, ()Ljava.lang.Object;, bar1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
public void testQualifiedStaticMethod() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"  public static Object bar1() {}\n" +
		"  public Zork Bar2() {}\n" +
		"  public static void removeBar() {}\n" +
		"  void foo() {\n" +
		"    Test.bar\n" +
		"  }\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "Test.bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"removeBar[METHOD_REF]{removeBar(), Ltest.Test;, ()V, removeBar, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_NON_INHERITED + R_SUBSTRING) + "}\n" +
			"bar1[METHOD_REF]{bar1(), Ltest.Test;, ()Ljava.lang.Object;, bar1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_NON_INHERITED) + "}",
			requestor.getResults());
}
public void testUnqualifiedStaticMethod() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"  public static Object bar1() {}\n" +
		"  public Zork Bar2() {}\n" +
		"  public static void removeBar() {}\n" +
		"  void foo() {\n" +
		"    Bar\n" +
		"  }\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "Bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"removeBar[METHOD_REF]{removeBar(), Ltest.Test;, ()V, removeBar, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBSTRING) + "}\n" +
			"bar1[METHOD_REF]{bar1(), Ltest.Test;, ()Ljava.lang.Object;, bar1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"Bar2[METHOD_REF]{Bar2(), Ltest.Test;, ()LZork;, Bar2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
public void testQualifiedNonStaticField() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"  int element;\n" +
		"  int otherElement;\n" +
		"  long elementCount;\n" +
		"  void foo() {\n" +
		"    this.elem\n" +
		"  }\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "this.elem";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"otherElement[FIELD_REF]{otherElement, Ltest.Test;, I, otherElement, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_STATIC + R_NON_RESTRICTED + R_SUBSTRING) + "}\n" +
			"element[FIELD_REF]{element, Ltest.Test;, I, element, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + + R_CASE + R_NON_STATIC + R_NON_RESTRICTED) + "}\n" +
			"elementCount[FIELD_REF]{elementCount, Ltest.Test;, J, elementCount, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}

public void testUnqualifiedNonStaticField() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  int element;\n" +
			"  int otherElement;\n" +
			"  long elementCount;\n" +
			"  void foo() {\n" +
			"    elem\n" +
			"  }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "elem";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"ElementType[TYPE_REF]{java.lang.annotation.ElementType, java.lang.annotation, Ljava.lang.annotation.ElementType;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED) + "}\n" +
			"otherElement[FIELD_REF]{otherElement, Ltest.Test;, I, otherElement, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBSTRING) + "}\n" +
			"element[FIELD_REF]{element, Ltest.Test;, I, element, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"elementCount[FIELD_REF]{elementCount, Ltest.Test;, J, elementCount, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
public void testQualifiedStaticField() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  static int element;\n" +
			"  int otherElement;\n" +
			"  static long elementCount;\n" +
			"  void foo() {\n" +
			"    Test.elem\n" +
			"  }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "Test.elem";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"element[FIELD_REF]{element, Ltest.Test;, I, element, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_NON_INHERITED) + "}\n" +
			"elementCount[FIELD_REF]{elementCount, Ltest.Test;, J, elementCount, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_NON_INHERITED) + "}",
			requestor.getResults());
}
public void testUnqualifiedStaticField() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  static int element;\n" +
			"  int otherElement;\n" +
			"  static long elementCount;\n" +
			"  void foo() {\n" +
			"    elem\n" +
			"  }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "elem";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"ElementType[TYPE_REF]{java.lang.annotation.ElementType, java.lang.annotation, Ljava.lang.annotation.ElementType;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED) + "}\n" +
			"otherElement[FIELD_REF]{otherElement, Ltest.Test;, I, otherElement, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBSTRING) + "}\n" +
			"element[FIELD_REF]{element, Ltest.Test;, I, element, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"elementCount[FIELD_REF]{elementCount, Ltest.Test;, J, elementCount, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
public void testLocalVariable() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  static int element;\n" +
			"  int otherElement;\n" +
			"  static long elementCount;\n" +
			"  void foo() {\n" +
			"    int temporaryElement = 0;\n" +
			"    elem\n" +
			"  }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "elem";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"ElementType[TYPE_REF]{java.lang.annotation.ElementType, java.lang.annotation, Ljava.lang.annotation.ElementType;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED) + "}\n" +
			"otherElement[FIELD_REF]{otherElement, Ltest.Test;, I, otherElement, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBSTRING) + "}\n" +
			"temporaryElement[LOCAL_VARIABLE_REF]{temporaryElement, null, I, temporaryElement, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBSTRING) + "}\n" +
			"element[FIELD_REF]{element, Ltest.Test;, I, element, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"elementCount[FIELD_REF]{elementCount, Ltest.Test;, J, elementCount, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
public void testMethodParamVariable() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  static int element;\n" +
			"  int otherElement;\n" +
			"  static long elementCount;\n" +
			"  void foo(int initElement) {\n" +
			"    elem\n" +
			"  }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "elem";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"ElementType[TYPE_REF]{java.lang.annotation.ElementType, java.lang.annotation, Ljava.lang.annotation.ElementType;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED) + "}\n" +
			"initElement[LOCAL_VARIABLE_REF]{initElement, null, I, initElement, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBSTRING) + "}\n" +
			"otherElement[FIELD_REF]{otherElement, Ltest.Test;, I, otherElement, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBSTRING) + "}\n" +
			"element[FIELD_REF]{element, Ltest.Test;, I, element, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"elementCount[FIELD_REF]{elementCount, Ltest.Test;, J, elementCount, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
public void testClassTypeInstantiation() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"interface Foobar {}\n" +
			"class SpecificFooBar implements Foobar {}\n" +
			"class EvenMoreSpecificFooBar extends SpecificFooBar {}\n" +
			"interface Foobaz {}\n" +
			"class SpecificFooBaz implements Foobaz {}\n" +
			"public class Test {\n" +
			"  {\n" +
			"    Foobar f = new bar\n" +
			"  }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "new bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"EvenMoreSpecificFooBar[TYPE_REF]{EvenMoreSpecificFooBar, test, Ltest.EvenMoreSpecificFooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBSTRING) + "}\n" +
			"SpecificFooBar[TYPE_REF]{SpecificFooBar, test, Ltest.SpecificFooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBSTRING) + "}\n" +
			"Foobar[TYPE_REF]{Foobar, test, Ltest.Foobar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBSTRING) + "}",
			requestor.getResults());
}
public void testClassTypeFieldDeclaration() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"interface Foobar {}\n" +
			"class SpecificFooBar implements Foobar {}\n" +
			"class EvenMoreSpecificFooBar extends SpecificFooBar {}\n" +
			"interface Foobaz {}\n" +
			"class SpecificFooBaz implements Foobaz {}\n" +
			"public class Test {\n" +
			"  public bar\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "public bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"EvenMoreSpecificFooBar[TYPE_REF]{EvenMoreSpecificFooBar, test, Ltest.EvenMoreSpecificFooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBSTRING) + "}\n" +
			"Foobar[TYPE_REF]{Foobar, test, Ltest.Foobar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBSTRING) + "}\n" +
			"SpecificFooBar[TYPE_REF]{SpecificFooBar, test, Ltest.SpecificFooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBSTRING) + "}",
			requestor.getResults());
}
public void testClassTypeParamDeclaration() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"interface Foobar {}\n" +
			"class SpecificFooBar implements Foobar {}\n" +
			"class EvenMoreSpecificFooBar extends SpecificFooBar {}\n" +
			"interface Foobaz {}\n" +
			"class SpecificFooBaz implements Foobaz {}\n" +
			"public class Test {\n" +
			"  void setFoo(bar\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "void setFoo(bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"EvenMoreSpecificFooBar[TYPE_REF]{EvenMoreSpecificFooBar, test, Ltest.EvenMoreSpecificFooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBSTRING) + "}\n" +
			"Foobar[TYPE_REF]{Foobar, test, Ltest.Foobar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBSTRING) + "}\n" +
			"SpecificFooBar[TYPE_REF]{SpecificFooBar, test, Ltest.SpecificFooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBSTRING) + "}",
			requestor.getResults());
}
public void testClassTypeLocalVarDeclaration() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"interface Foobar {}\n" +
			"class SpecificFooBar implements Foobar {}\n" +
			"class EvenMoreSpecificFooBar extends SpecificFooBar {}\n" +
			"interface Foobaz {}\n" +
			"class SpecificFooBaz implements Foobaz {}\n" +
			"public class Test {\n" +
			"  void foo() {\n" +
			"    final bar" +
			"  }" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "final bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"EvenMoreSpecificFooBar[TYPE_REF]{EvenMoreSpecificFooBar, test, Ltest.EvenMoreSpecificFooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBSTRING) + "}\n" +
			"Foobar[TYPE_REF]{Foobar, test, Ltest.Foobar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBSTRING) + "}\n" +
			"SpecificFooBar[TYPE_REF]{SpecificFooBar, test, Ltest.SpecificFooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBSTRING) + "}",
			requestor.getResults());
}
public void testClassTypeThrowsDeclaration() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"interface Foobar {}\n" +
			"class SpecificFooBar implements Foobar extends Exception {}\n" +
			"class EvenMoreSpecificFooBar extends SpecificFooBar {}\n" +
			"interface Foobaz {}\n" +
			"class SpecificFooBaz implements Foobaz extends Exception {}\n" +
			"public class Test {\n" +
			"  void foo() throws bar {\n" +
			"  }" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "void foo() throws bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"EvenMoreSpecificFooBar[TYPE_REF]{EvenMoreSpecificFooBar, test, Ltest.EvenMoreSpecificFooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBSTRING) + "}\n" +
			"SpecificFooBar[TYPE_REF]{SpecificFooBar, test, Ltest.SpecificFooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBSTRING) + "}",
			requestor.getResults());
}
public void testClassTypeExtends() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"interface Foobar {}\n" +
			"class SpecificFooBar implements Foobar {}\n" +
			"class EvenMoreSpecificFooBar extends SpecificFooBar {}\n" +
			"interface Foobaz {}\n" +
			"class SpecificFooBaz implements Foobaz {}\n" +
			"public class Test extends bar {\n" +
			"  }" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "public class Test extends bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"EvenMoreSpecificFooBar[TYPE_REF]{EvenMoreSpecificFooBar, test, Ltest.EvenMoreSpecificFooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_CLASS + R_SUBSTRING) + "}\n" +
			"SpecificFooBar[TYPE_REF]{SpecificFooBar, test, Ltest.SpecificFooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_CLASS + R_SUBSTRING) + "}",
			requestor.getResults());
}
public void testClassTypeImplements() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"interface Foobar {}\n" +
			"interface FoobarExtension extends Foobar {}\n" +
			"class SpecificFooBar implements Foobar {}\n" +
			"class EvenMoreSpecificFooBar extends SpecificFooBar {}\n" +
			"interface Foobaz {}\n" +
			"class SpecificFooBaz implements Foobaz {}\n" +
			"public class Test implements bar {\n" +
			"  }" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "public class Test implements bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Foobar[TYPE_REF]{Foobar, test, Ltest.Foobar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_INTERFACE + R_SUBSTRING) + "}\n" +
			"FoobarExtension[TYPE_REF]{FoobarExtension, test, Ltest.FoobarExtension;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_INTERFACE + R_SUBSTRING) + "}",
			requestor.getResults());
}
public void testInnerClassTypeInstantiation() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  class FooBar {}\n" +
			"  {\n" +
			"    Test t = new Test();\n" +
			"    Object f = t.new bar\n" +
			"  }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "t.new bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Test.FooBar[TYPE_REF]{FooBar, test, Ltest.Test$FooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXPECTED_TYPE + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBSTRING) + "}",
			requestor.getResults());
}
public void testInnerClassTypeFieldDeclaration() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  class FooBar {}\n" +
			"  public bar\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "public bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Test.FooBar[TYPE_REF]{FooBar, test, Ltest.Test$FooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBSTRING) + "}",
			requestor.getResults());
}
public void testInnerClassTypeParamDeclaration() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  class FooBar {}\n" +
			"  void foo(bar\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "void foo(bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Test.FooBar[TYPE_REF]{FooBar, test, Ltest.Test$FooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBSTRING) + "}",
			requestor.getResults());
}
public void testInnerClassTypeLocalVarDeclaration() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  class FooBar {}\n" +
			"  {\n" +
			"    final bar\n" +
			"  }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "final bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Test.FooBar[TYPE_REF]{FooBar, test, Ltest.Test$FooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBSTRING) + "}",
			requestor.getResults());
}
public void testInnerClassTypeThrowsDeclaration() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  class FooBar extends Exception {}\n" +
			"  void foo() throws bar" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "void foo() throws bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Test.FooBar[TYPE_REF]{FooBar, test, Ltest.Test$FooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBSTRING) + "}",
			requestor.getResults());
}
public void testInnerClassTypeExtends() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  class FooBar {}\n" +
			"  class SpecificFooBar extends bar\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "SpecificFooBar extends bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Test.FooBar[TYPE_REF]{FooBar, test, Ltest.Test$FooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXPECTED_TYPE + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBSTRING) + "}",
			requestor.getResults());
}
public void testInnerClassTypeImplements() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  interface FooBar {}\n" +
			"  class SpecificFooBar implements bar\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "class SpecificFooBar implements bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Test.FooBar[TYPE_REF]{FooBar, test, Ltest.Test$FooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXPECTED_TYPE + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBSTRING) + "}",
			requestor.getResults());
}
public void testStaticNestedClassTypeInstantiation() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  static class FooBar {}\n" +
			"  {\n" +
			"    Object f = new bar\n" +
			"  }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "new bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Test.FooBar[TYPE_REF]{FooBar, test, Ltest.Test$FooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXPECTED_TYPE + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBSTRING) + "}",
			requestor.getResults());
}
public void testStaticNestedClassTypeFieldDeclaration() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  static class FooBar {}\n" +
			"  public bar\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "public bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Test.FooBar[TYPE_REF]{FooBar, test, Ltest.Test$FooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBSTRING) + "}",
			requestor.getResults());
}
public void testStaticNestedClassTypeParamDeclaration() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  static class FooBar {}\n" +
			"  void foo(bar\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "void foo(bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Test.FooBar[TYPE_REF]{FooBar, test, Ltest.Test$FooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBSTRING) + "}",
			requestor.getResults());
}
public void testStaticNestedClassTypeLocalVarDeclaration() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  static class FooBar {}\n" +
			"  {\n" +
			"    final bar\n" +
			"  }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "final bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Test.FooBar[TYPE_REF]{FooBar, test, Ltest.Test$FooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBSTRING) + "}",
			requestor.getResults());
}
public void testStaticNestedClassTypeThrowsDeclaration() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  static class FooBar extends Exception {}\n" +
			"  void foo() throws bar" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "void foo() throws bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Test.FooBar[TYPE_REF]{FooBar, test, Ltest.Test$FooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBSTRING) + "}",
			requestor.getResults());
}
public void testStaticNestedClassTypeExtends() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  static class FooBar {}\n" +
			"  class SpecificFooBar extends bar\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "SpecificFooBar extends bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Test.FooBar[TYPE_REF]{FooBar, test, Ltest.Test$FooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXPECTED_TYPE + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBSTRING) + "}",
			requestor.getResults());
}
public void testStaticNestedClassTypeImplements() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  static interface FooBar {}\n" +
			"  class SpecificFooBar implements bar\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "class SpecificFooBar implements bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Test.FooBar[TYPE_REF]{FooBar, test, Ltest.Test$FooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXPECTED_TYPE + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBSTRING) + "}",
			requestor.getResults());
}
public void testLocalClassTypeInstantiation() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  void foo() {\n" +
			"    class FooBar {}\n" +
			"    Object f = new bar\n" +
			"  }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "new bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"FooBar[TYPE_REF]{FooBar, test, LFooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXPECTED_TYPE + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBSTRING) + "}",
			requestor.getResults());
}
public void testLocalClassTypeLocalVarDeclaration() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  void foo() {\n" +
			"    class FooBar {}\n" +
			"    final bar\n" +
			"  }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "final bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"FooBar[TYPE_REF]{FooBar, test, LFooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBSTRING) + "}",
			requestor.getResults());
}
public void testLocalClassTypeExtends() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  void foo() {\n" +
			"    class FooBar {}\n" +
			"    class SpecificFooBar extends bar\n" +
			"  }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "class SpecificFooBar extends bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"FooBar[TYPE_REF]{FooBar, test, LFooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXPECTED_TYPE + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBSTRING) + "}",
			requestor.getResults());
}
}