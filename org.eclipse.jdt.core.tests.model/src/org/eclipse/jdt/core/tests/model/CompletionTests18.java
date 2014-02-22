/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
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

package org.eclipse.jdt.core.tests.model;

import junit.framework.Test;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;

public class CompletionTests18 extends AbstractJavaModelCompletionTests {

static {
//		TESTS_NAMES = new String[] {"test001"};
}

public CompletionTests18(String name) {
	super(name);
}

public void setUpSuite() throws Exception {
	if (COMPLETION_PROJECT == null)  {
		COMPLETION_PROJECT = setUpJavaProject("Completion", "1.8", true);
	} else {
		setUpProjectCompliance(COMPLETION_PROJECT, "1.8", true);
	}
	super.setUpSuite();
}
public static Test suite() {
	return buildModelTestSuite(CompletionTests18.class);
}

public void test001() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"interface Foo { \n" +
			"	void run1(int s1, int s2);\n" +
			"}\n" +
			"interface X extends Foo{\n" +
			"  static Foo f = (first, second) -> System.out.print(fir);\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "fir";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults(
			"first[LOCAL_VARIABLE_REF]{first, null, I, first, null, 27}",
			requestor.getResults());
}
public void test002() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"interface Foo { \n" +
			"	void run1(int s1, int s2);\n" +
			"}\n" +
			"interface X extends Foo {\n" +
			"  public static void main(String [] args) {\n" +
			"      Foo f = (first, second) -> System.out.print(fir);\n" +
			"  }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "fir";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults(
			"first[LOCAL_VARIABLE_REF]{first, null, I, first, null, 27}",
			requestor.getResults());
}
public void test003() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
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
			"			return (xyz, pqr) -> first.c\n" +
			"		});\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "first.c";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults(
			"CASE_INSENSITIVE_ORDER[FIELD_REF]{CASE_INSENSITIVE_ORDER, Ljava.lang.String;, Ljava.util.Comparator<Ljava.lang.String;>;, CASE_INSENSITIVE_ORDER, null, 14}\n" +
			"copyValueOf[METHOD_REF]{copyValueOf(), Ljava.lang.String;, ([C)Ljava.lang.String;, copyValueOf, (arg0), 24}\n" +
			"copyValueOf[METHOD_REF]{copyValueOf(), Ljava.lang.String;, ([CII)Ljava.lang.String;, copyValueOf, (arg0, arg1, arg2), 24}\n" +
			"charAt[METHOD_REF]{charAt(), Ljava.lang.String;, (I)C, charAt, (arg0), 35}\n" +
			"chars[METHOD_REF]{chars(), Ljava.lang.CharSequence;, ()Ljava.util.stream.IntStream;, chars, null, 35}\n" +
			"clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, 35}\n" +
			"codePointAt[METHOD_REF]{codePointAt(), Ljava.lang.String;, (I)I, codePointAt, (arg0), 35}\n" +
			"codePointBefore[METHOD_REF]{codePointBefore(), Ljava.lang.String;, (I)I, codePointBefore, (arg0), 35}\n" +
			"codePointCount[METHOD_REF]{codePointCount(), Ljava.lang.String;, (II)I, codePointCount, (arg0, arg1), 35}\n" +
			"codePoints[METHOD_REF]{codePoints(), Ljava.lang.CharSequence;, ()Ljava.util.stream.IntStream;, codePoints, null, 35}\n" +
			"compareTo[METHOD_REF]{compareTo(), Ljava.lang.String;, (Ljava.lang.String;)I, compareTo, (arg0), 35}\n" +
			"compareToIgnoreCase[METHOD_REF]{compareToIgnoreCase(), Ljava.lang.String;, (Ljava.lang.String;)I, compareToIgnoreCase, (arg0), 35}\n" +
			"concat[METHOD_REF]{concat(), Ljava.lang.String;, (Ljava.lang.String;)Ljava.lang.String;, concat, (arg0), 35}\n" +
			"contains[METHOD_REF]{contains(), Ljava.lang.String;, (Ljava.lang.CharSequence;)Z, contains, (arg0), 35}\n" +
			"contentEquals[METHOD_REF]{contentEquals(), Ljava.lang.String;, (Ljava.lang.CharSequence;)Z, contentEquals, (arg0), 35}\n" +
			"contentEquals[METHOD_REF]{contentEquals(), Ljava.lang.String;, (Ljava.lang.StringBuffer;)Z, contentEquals, (arg0), 35}",
			requestor.getResults());
}
public void test004() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"interface Foo {\n" +
			"	int run1(int s1, int s2);\n" +
			"}\n" +
			"interface X extends Foo{\n" +
			"    static Foo f = (lpx5, lpx6) -> {lpx\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "lpx";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults(
			"lpx5[LOCAL_VARIABLE_REF]{lpx5, null, I, lpx5, null, 27}\n" +
			"lpx6[LOCAL_VARIABLE_REF]{lpx6, null, I, lpx6, null, 27}",
			requestor.getResults());
}

public void test005() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
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
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "arg";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults(
			"argument[LOCAL_VARIABLE_REF]{argument, null, I, argument, null, 57}",
			requestor.getResults());
}
public void test006() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"interface I {\n" +
			"	int foo(int x);\n" +
			"}\n" +
			"public class X {\n" +
			"	void go() {\n" +
			"		I i = (argument) -> {\n" +
			"			argument == 0 ? arg\n" +
			"		}\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "arg";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults(
			"argument[LOCAL_VARIABLE_REF]{argument, null, I, argument, null, 27}",
			requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=405126, [1.8][code assist] Lambda parameters incorrectly recovered as fields. 
public void test007() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
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
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "X.";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults(
			"class[FIELD_REF]{class, null, Ljava.lang.Class<LX;>;, class, null, 26}\n" +
			"f[FIELD_REF]{f, LX;, LFoo;, f, null, 26}\n" +
			"this[KEYWORD]{this, null, null, this, null, 26}\n" +
			"x1[FIELD_REF]{x1, LX;, I, x1, null, 56}",
			requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422107, [1.8][code assist] Invoking code assist just before and after a variable initialized using lambda gives different result
public void test008() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"interface I {\n" +
			"    void doit();\n" +
			"}\n" +
			"interface J {\n" +
			"}\n" +
			"public class X { \n" +
			"	/* BEFORE */\n" +
			"	Object o = (I & J) () -> {};\n" +
			"	/* AFTER */\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "/* BEFORE */";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults(
			"[POTENTIAL_METHOD_DECLARATION]{, LX;, ()V, , null, 14}\n" +
			"abstract[KEYWORD]{abstract, null, null, abstract, null, 24}\n" +
			"class[KEYWORD]{class, null, null, class, null, 24}\n" +
			"enum[KEYWORD]{enum, null, null, enum, null, 24}\n" +
			"final[KEYWORD]{final, null, null, final, null, 24}\n" +
			"interface[KEYWORD]{interface, null, null, interface, null, 24}\n" +
			"native[KEYWORD]{native, null, null, native, null, 24}\n" +
			"private[KEYWORD]{private, null, null, private, null, 24}\n" +
			"protected[KEYWORD]{protected, null, null, protected, null, 24}\n" +
			"public[KEYWORD]{public, null, null, public, null, 24}\n" +
			"static[KEYWORD]{static, null, null, static, null, 24}\n" +
			"strictfp[KEYWORD]{strictfp, null, null, strictfp, null, 24}\n" +
			"synchronized[KEYWORD]{synchronized, null, null, synchronized, null, 24}\n" +
			"transient[KEYWORD]{transient, null, null, transient, null, 24}\n" +
			"volatile[KEYWORD]{volatile, null, null, volatile, null, 24}\n" +
			"I[TYPE_REF]{I, , LI;, null, null, 27}\n" +
			"J[TYPE_REF]{J, , LJ;, null, null, 27}\n" +
			"X[TYPE_REF]{X, , LX;, null, null, 27}\n" +
			"clone[METHOD_DECLARATION]{protected Object clone() throws CloneNotSupportedException, Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, 27}\n" +
			"equals[METHOD_DECLARATION]{public boolean equals(Object obj), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), 27}\n" +
			"finalize[METHOD_DECLARATION]{protected void finalize() throws Throwable, Ljava.lang.Object;, ()V, finalize, null, 27}\n" +
			"hashCode[METHOD_DECLARATION]{public int hashCode(), Ljava.lang.Object;, ()I, hashCode, null, 27}\n" +
			"toString[METHOD_DECLARATION]{public String toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, 27}",
			requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422107, [1.8][code assist] Invoking code assist just before and after a variable initialized using lambda gives different result
public void test009() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"interface I {\n" +
			"    void doit();\n" +
			"}\n" +
			"interface J {\n" +
			"}\n" +
			"public class X { \n" +
			"	/* BEFORE */\n" +
			"	Object o = (I & J) () -> {};\n" +
			"	/* AFTER */\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "/* AFTER */";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults(
			"[POTENTIAL_METHOD_DECLARATION]{, LX;, ()V, , null, 14}\n" +
			"abstract[KEYWORD]{abstract, null, null, abstract, null, 24}\n" +
			"class[KEYWORD]{class, null, null, class, null, 24}\n" +
			"enum[KEYWORD]{enum, null, null, enum, null, 24}\n" +
			"final[KEYWORD]{final, null, null, final, null, 24}\n" +
			"interface[KEYWORD]{interface, null, null, interface, null, 24}\n" +
			"native[KEYWORD]{native, null, null, native, null, 24}\n" +
			"private[KEYWORD]{private, null, null, private, null, 24}\n" +
			"protected[KEYWORD]{protected, null, null, protected, null, 24}\n" +
			"public[KEYWORD]{public, null, null, public, null, 24}\n" +
			"static[KEYWORD]{static, null, null, static, null, 24}\n" +
			"strictfp[KEYWORD]{strictfp, null, null, strictfp, null, 24}\n" +
			"synchronized[KEYWORD]{synchronized, null, null, synchronized, null, 24}\n" +
			"transient[KEYWORD]{transient, null, null, transient, null, 24}\n" +
			"volatile[KEYWORD]{volatile, null, null, volatile, null, 24}\n" +
			"I[TYPE_REF]{I, , LI;, null, null, 27}\n" +
			"J[TYPE_REF]{J, , LJ;, null, null, 27}\n" +
			"X[TYPE_REF]{X, , LX;, null, null, 27}\n" +
			"clone[METHOD_DECLARATION]{protected Object clone() throws CloneNotSupportedException, Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, 27}\n" +
			"equals[METHOD_DECLARATION]{public boolean equals(Object obj), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), 27}\n" +
			"finalize[METHOD_DECLARATION]{protected void finalize() throws Throwable, Ljava.lang.Object;, ()V, finalize, null, 27}\n" +
			"hashCode[METHOD_DECLARATION]{public int hashCode(), Ljava.lang.Object;, ()I, hashCode, null, 27}\n" +
			"toString[METHOD_DECLARATION]{public String toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, 27}",
			requestor.getResults());
}
public void test010() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"interface I {\n" +
			"  String foo(X x, X i); \n" +
			"} \n" +
			"public class X  {\n" +
			"	static void goo(I i) {\n" +
			"	}\n" +
			"	static void goo(String s) {\n" +
			"	}\n" +
			"	public static void main(String[] args) { \n" +
			"		goo((x, y) -> {\n" +
			"			x.\n" +
			"			return x + y;\n" +
			"		});\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "x.";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults(
			"goo[METHOD_REF]{goo(), LX;, (LI;)V, goo, (i), 24}\n" +
			"goo[METHOD_REF]{goo(), LX;, (Ljava.lang.String;)V, goo, (s), 24}\n" +
			"main[METHOD_REF]{main(), LX;, ([Ljava.lang.String;)V, main, (args), 24}\n" +
			"clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, 35}\n" +
			"equals[METHOD_REF]{equals(), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), 35}\n" +
			"finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, 35}\n" +
			"getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<*>;, getClass, null, 35}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, 35}\n" +
			"notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, 35}\n" +
			"notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, 35}\n" +
			"toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, 35}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, 35}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, wait, (millis), 35}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), 35}",
			requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422901, [1.8][code assist] Code assistant sensitive to scope.referenceContext type identity.
public void test011() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"interface I {\n" +
			"	void foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		syso\n" +
			"		I i = () -> {\n" +
			"		};\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "syso";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("", requestor.getResults());
	assertEquals("completion offset=94\n" +
			"completion range=[90, 93]\n" +
			"completion token=\"syso\"\n" +
			"completion token kind=TOKEN_KIND_NAME\n" +
			"expectedTypesSignatures=null\n" +
			"expectedTypesKeys=null\n" +
			"completion token location={STATEMENT_START}", requestor.getContext());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422901, [1.8][code assist] Code assistant sensitive to scope.referenceContext type identity.
public void test012() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"interface I {\n" +
			"	void foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		I i = () -> {\n" +
			"		    syso\n" +
			"		};\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "syso";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("", requestor.getResults());
	assertEquals("completion offset=114\n" +
			"completion range=[110, 113]\n" +
			"completion token=\"syso\"\n" +
			"completion token kind=TOKEN_KIND_NAME\n" +
			"expectedTypesSignatures=null\n" +
			"expectedTypesKeys=null\n" +
			"completion token location={STATEMENT_START}", requestor.getContext());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422901, [1.8][code assist] Code assistant sensitive to scope.referenceContext type identity.
public void test013() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"interface I {\n" +
			"	void foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		I i = () -> {\n" +
			"		};\n" +
			"		syso\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "syso";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("", requestor.getResults());
	assertEquals("completion offset=115\n" +
			"completion range=[111, 114]\n" +
			"completion token=\"syso\"\n" +
			"completion token kind=TOKEN_KIND_NAME\n" +
			"expectedTypesSignatures=null\n" +
			"expectedTypesKeys=null\n" +
			"completion token location={STATEMENT_START}", requestor.getContext());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422901, [1.8][code assist] Code assistant sensitive to scope.referenceContext type identity.
public void test014() throws JavaModelException { // ensure higher relevance for matching return type.
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"interface I {\n" +
			"	int [] foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] arrayOfStrings) {\n" +
			"       int [] arrayOfInts = null;\n" +
			"		I i = () -> {\n" +
			"           return arrayO\n" +
			"		};\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "arrayO";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("arrayOfStrings[LOCAL_VARIABLE_REF]{arrayOfStrings, null, [Ljava.lang.String;, null, null, arrayOfStrings, null, [168, 174], 27}\n" +
					"arrayOfInts[LOCAL_VARIABLE_REF]{arrayOfInts, null, [I, null, null, arrayOfInts, null, [168, 174], 57}", requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422901, [1.8][code assist] Code assistant sensitive to scope.referenceContext type identity.
public void test015() throws JavaModelException { // ensure higher relevance for matching return type.
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"interface I {\n" +
			"	void foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		class Y {\n" +
			"			I i = () -> {\n" +
			"               xyz\n" +
			"               xyzAfter = 10;\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "xyz";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("xyzAfter[LOCAL_VARIABLE_REF]{xyzAfter, null, Ljava.lang.Object;, null, null, xyzAfter, null, [132, 135], 26}", requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422901, [1.8][code assist] Code assistant sensitive to scope.referenceContext type identity.
public void test016() throws JavaModelException { // ensure higher relevance for matching return type.
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"interface I {\n" +
			"	void foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		class Y {\n" +
			"			I i = () -> {\n" +
			"               xyzBefore = 10;\n" +
			"               xyz\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "xyz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("xyzBefore[LOCAL_VARIABLE_REF]{xyzBefore, null, Ljava.lang.Object;, null, null, xyzBefore, null, [163, 166], 26}", requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422901, [1.8][code assist] Code assistant sensitive to scope.referenceContext type identity.
public void test017() throws JavaModelException { // ensure higher relevance for matching return type.
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"interface I {\n" +
			"	void foo();\n" +
			"}\n" +
			"public class X {\n" +
			"   public static X xField;\n" +
			"   public static X goo() { return null; }\n" +
			"	public static void main(String[] args) {\n" +
			"			I i = () -> {\n" +
			"               xyz\n" +
			"	}\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	requestor.setRequireExtendedContext(true);
	requestor.setComputeEnclosingElement(false);
	requestor.setComputeVisibleElements(true);
	requestor.setAssignableType("LX;");
	
	String str = this.workingCopies[0].getSource();
	String completeBehind = "xyz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertEquals("completion offset=192\n" +
			"completion range=[189, 191]\n" +
			"completion token=\"xyz\"\n" +
			"completion token kind=TOKEN_KIND_NAME\n" +
			"expectedTypesSignatures=null\n" +
			"expectedTypesKeys=null\n" +
			"completion token location={STATEMENT_START}\n" +
			"visibleElements={\n" +
			"	xField {key=LX;.xField)LX;} [in X [in [Working copy] X.java [in <default> [in src [in Completion]]]]],\n" +
			"	goo() {key=LX;.goo()LX;} [in X [in [Working copy] X.java [in <default> [in src [in Completion]]]]],\n" +
			"}" , requestor.getContext());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422468, [1.8][assist] Code assist issues with type elided lambda parameters
public void test018() throws JavaModelException { // computing visible elements in lambda scope.
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"interface I {\n" +
			"	void foo(String x);\n" +
			"}\n" +
			"public class X {\n" +
			"	static X xField;\n" +
			"	static X goo(String s) {\n" +
			"       return null;\n" +
			"	}\n" +
			"	static void goo(I i) {\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		goo((xyz) -> {\n" +
			"			System.out.println(xyz.);\n" +
			"		});\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	requestor.setRequireExtendedContext(true);
	requestor.setComputeEnclosingElement(false);
	requestor.setComputeVisibleElements(true);
	requestor.setAssignableType("LX;");
	
	String str = this.workingCopies[0].getSource();
	String completeBehind = "xyz.";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertEquals("completion offset=233\n" +
			"completion range=[233, 232]\n" +
			"completion token=\"\"\n" +
			"completion token kind=TOKEN_KIND_NAME\n" +
			"expectedTypesSignatures=null\n" +
			"expectedTypesKeys=null\n" +
			"completion token location=UNKNOWN\n" +
			"visibleElements={\n" +
			"	xField {key=LX;.xField)LX;} [in X [in [Working copy] X.java [in <default> [in src [in Completion]]]]],\n" +
			"	goo(String) {key=LX;.goo(Ljava/lang/String;)LX;} [in X [in [Working copy] X.java [in <default> [in src [in Completion]]]]],\n" +
			"}" , requestor.getContext());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422468, [1.8][assist] Code assist issues with type elided lambda parameters
public void test018a() throws JavaModelException { // computing visible elements in lambda scope.
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"interface I {\n" +
			"	void foo(X x);\n" +
			"}\n" +
			"public class X {\n" +
			"	static X xField;\n" +
			"	static X goo(String s) {\n" +
			"       return null;\n" +
			"	}\n" +
			"	static void goo(I i) {\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"       X xLocal = null;\n" +
			"       args = null;\n" +
			"       if (args != null) {\n" +
			"           xField = null;\n" +
			"       else \n" +
			"           xField = null;\n" +
			"       while (true);\n" +
			"		goo((xyz) -> {\n" +
			"           X xLambdaLocal = null;\n" +
			"			System.out.println(xyz.)\n" +
			"		});\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	requestor.setRequireExtendedContext(true);
	requestor.setComputeEnclosingElement(false);
	requestor.setComputeVisibleElements(true);
	requestor.setAssignableType("LX;");
	
	String str = this.workingCopies[0].getSource();
	String completeBehind = "xyz.";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertEquals(
			"completion offset=419\n" +
			"completion range=[419, 418]\n" +
			"completion token=\"\"\n" +
			"completion token kind=TOKEN_KIND_NAME\n" +
			"expectedTypesSignatures={Z,C,I,J,F,D,[C,Ljava.lang.String;,Ljava.lang.Object;}\n" +
			"expectedTypesKeys={Z,C,I,J,F,D,[C,Ljava/lang/String;,Ljava/lang/Object;}\n" +
			"completion token location=UNKNOWN\n" +
			"visibleElements={\n" +
			"	xLocal [in main(String[]) [in X [in [Working copy] X.java [in <default> [in src [in Completion]]]]]],\n" +
	        "	xLambdaLocal [in main(String[]) [in X [in [Working copy] X.java [in <default> [in src [in Completion]]]]]],\n" +
			"	xField {key=LX;.xField)LX;} [in X [in [Working copy] X.java [in <default> [in src [in Completion]]]]],\n" +
			"	goo(String) {key=LX;.goo(Ljava/lang/String;)LX;} [in X [in [Working copy] X.java [in <default> [in src [in Completion]]]]],\n" +
			"}" , requestor.getContext());
}
public void testUnspecifiedReference() throws JavaModelException { // ensure completion on ambiguous reference works and shows both types and names.
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"interface I {\n" +
			"    void doit(X x);\n" +
			"}\n" +
			"public class X { \n" +
			"	static void goo(I i) {\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		goo((StringParameter) -> {\n" +
			"			Stri\n" +
			"		});\n" +
			"	} \n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "Stri";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("StringBufferInputStream[TYPE_REF]{java.io.StringBufferInputStream, java.io, Ljava.io.StringBufferInputStream;, null, null, null, null, [155, 159], 24}\n" +
			"StringCharBuffer[TYPE_REF]{java.nio.StringCharBuffer, java.nio, Ljava.nio.StringCharBuffer;, null, null, null, null, [155, 159], 24}\n" +
			"StringCharacterIterator[TYPE_REF]{java.text.StringCharacterIterator, java.text, Ljava.text.StringCharacterIterator;, null, null, null, null, [155, 159], 24}\n" +
			"StringJoiner[TYPE_REF]{java.util.StringJoiner, java.util, Ljava.util.StringJoiner;, null, null, null, null, [155, 159], 24}\n" +
			"StringReader[TYPE_REF]{java.io.StringReader, java.io, Ljava.io.StringReader;, null, null, null, null, [155, 159], 24}\n" +
			"StringTokenizer[TYPE_REF]{java.util.StringTokenizer, java.util, Ljava.util.StringTokenizer;, null, null, null, null, [155, 159], 24}\n" +
			"StringWriter[TYPE_REF]{java.io.StringWriter, java.io, Ljava.io.StringWriter;, null, null, null, null, [155, 159], 24}\n" +
			"StrictMath[TYPE_REF]{StrictMath, java.lang, Ljava.lang.StrictMath;, null, null, null, null, [155, 159], 27}\n" +
			"String[TYPE_REF]{String, java.lang, Ljava.lang.String;, null, null, null, null, [155, 159], 27}\n" +
			"StringBuffer[TYPE_REF]{StringBuffer, java.lang, Ljava.lang.StringBuffer;, null, null, null, null, [155, 159], 27}\n" +
			"StringBuilder[TYPE_REF]{StringBuilder, java.lang, Ljava.lang.StringBuilder;, null, null, null, null, [155, 159], 27}\n" +
			"StringCoding[TYPE_REF]{StringCoding, java.lang, Ljava.lang.StringCoding;, null, null, null, null, [155, 159], 27}\n" +
			"StringIndexOutOfBoundsException[TYPE_REF]{StringIndexOutOfBoundsException, java.lang, Ljava.lang.StringIndexOutOfBoundsException;, null, null, null, null, [155, 159], 27}\n" +
			"StringParameter[LOCAL_VARIABLE_REF]{StringParameter, null, LX;, null, null, StringParameter, null, [155, 159], 27}", requestor.getResults());
}
public void testBrokenMethodCall() throws JavaModelException { // ensure completion works when the containing call is not terminated properly.
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"interface I {\n" +
			"    void doit(X x);\n" +
			"}\n" +
			"public class X { \n" +
			"	static void goo(I i) {\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		goo((StringParameter) -> {\n" +
			"			StringP\n" +
			"		})\n" +
			"	} \n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "StringP";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("StringParameter[LOCAL_VARIABLE_REF]{StringParameter, null, LX;, null, null, StringParameter, null, [155, 162], 27}", requestor.getResults());
}
public void testExpressionBody() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"interface I {\n" +
			"    void doit(X x);\n" +
			"}\n" +
			"public class X { \n" +
			"   void foo() {}\n" +
			"   int field;\n" +
			"	static void goo(I i) {\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		goo((xyz) -> xyz.)\n" +
			"	} \n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "xyz.";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("goo[METHOD_REF]{goo(), LX;, (LI;)V, null, null, goo, (i), [173, 173], 24}\n" +
			"main[METHOD_REF]{main(), LX;, ([Ljava.lang.String;)V, null, null, main, (args), [173, 173], 24}\n" +
			"clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, null, null, clone, null, [173, 173], 35}\n" +
			"equals[METHOD_REF]{equals(), Ljava.lang.Object;, (Ljava.lang.Object;)Z, null, null, equals, (obj), [173, 173], 35}\n" +
			"field[FIELD_REF]{field, LX;, I, null, null, field, null, [173, 173], 35}\n" +
			"finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, null, null, finalize, null, [173, 173], 35}\n" +
			"foo[METHOD_REF]{foo(), LX;, ()V, null, null, foo, null, [173, 173], 35}\n" +
			"getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<*>;, null, null, getClass, null, [173, 173], 35}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, null, null, hashCode, null, [173, 173], 35}\n" +
			"notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, null, null, notify, null, [173, 173], 35}\n" +
			"notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, null, null, notifyAll, null, [173, 173], 35}\n" +
			"toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, null, null, toString, null, [173, 173], 35}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, null, null, wait, null, [173, 173], 35}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, null, null, wait, (millis), [173, 173], 35}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, null, null, wait, (millis, nanos), [173, 173], 35}", requestor.getResults());
}
public void testExpressionBody2() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"interface I {\n" +
			"    void doit(X x);\n" +
			"}\n" +
			"public class X { \n" +
			"   void foo() {}\n" +
			"   int field;\n" +
			"	static void goo(I i) {\n" +
			"	}\n" +
			"	public static void main(String[] args) {\n" +
			"		  goo(xyz -> xyz.)\n" +
			"	} \n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "xyz.";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("goo[METHOD_REF]{goo(), LX;, (LI;)V, null, null, goo, (i), [173, 173], 24}\n" +
			"main[METHOD_REF]{main(), LX;, ([Ljava.lang.String;)V, null, null, main, (args), [173, 173], 24}\n" +
			"clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, null, null, clone, null, [173, 173], 35}\n" +
			"equals[METHOD_REF]{equals(), Ljava.lang.Object;, (Ljava.lang.Object;)Z, null, null, equals, (obj), [173, 173], 35}\n" +
			"field[FIELD_REF]{field, LX;, I, null, null, field, null, [173, 173], 35}\n" +
			"finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, null, null, finalize, null, [173, 173], 35}\n" +
			"foo[METHOD_REF]{foo(), LX;, ()V, null, null, foo, null, [173, 173], 35}\n" +
			"getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<*>;, null, null, getClass, null, [173, 173], 35}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, null, null, hashCode, null, [173, 173], 35}\n" +
			"notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, null, null, notify, null, [173, 173], 35}\n" +
			"notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, null, null, notifyAll, null, [173, 173], 35}\n" +
			"toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, null, null, toString, null, [173, 173], 35}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, null, null, wait, null, [173, 173], 35}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, null, null, wait, (millis), [173, 173], 35}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, null, null, wait, (millis, nanos), [173, 173], 35}", requestor.getResults());
}
// Bug 405125 - [1.8][code assist] static members of an interface appearing after the declaration of a static member lambda expression are not being suggested.
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=405125
public void testBug405125a() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Foo.java",
				"public interface Foo {\n" +
				"	int run(int s1, int s2);\n" +
				"}\n" +
				"interface B {\n" +
				"	static Foo f = (int x5, int x2) -> bar\n" +
				"	static int x4 = 3;\n" +
				"  	static int bars () { return 2; }\n" +
				"}");

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.setRequireExtendedContext(true);
		requestor.setComputeVisibleElements(true);
		requestor.allowAllRequiredProposals();
	
	    String str = this.workingCopies[0].getSource();
	    String completeBehind = "(int x5, int x2) -> bar";
	    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	    
	    assertResults(
	    	"bars[METHOD_REF]{bars(), LB;, ()I, bars, null, 27}",
	    	requestor.getResults());
}
public void testBug405125b() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Foo.java",
				"public interface Foo {\n" +
				"	int run(int s1, int s2);\n" +
				"}\n" +
				"interface B {\n" +
				"	static Foo f = (int x5, int x2) -> anot\n" +
				"	static int another = 3;\n" +
				"  	static int two () { return 2; }\n" +
				"}");

		// do completion
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.setRequireExtendedContext(true);
		requestor.setComputeVisibleElements(true);
		requestor.allowAllRequiredProposals();

	    String str = this.workingCopies[0].getSource();
	    String completeBehind = "(int x5, int x2) -> anot";
	    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	    
	    assertResults(
	    	"another[FIELD_REF]{another, LB;, I, another, null, 27}",
	    	requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=425084, [1.8][completion] Eclipse freeze while autocompleting try block in lambda.
public void test425084() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"interface I {\n" +
			"	void foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	I goo() {\n" +
			"       int tryit = 0;\n" +
			"		return () -> {\n" +
			"			try\n" +
			"		};\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "try";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("tryit[LOCAL_VARIABLE_REF]{tryit, null, I, null, null, tryit, null, [99, 102], 27}", requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422901, [1.8][code assist] Code assistant sensitive to scope.referenceContext type identity.
public void test422901() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"interface I {\n" +
			"	void foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	I i = () -> {\n" +
			"		syso    // no proposals here.\n" +
			"	};\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "syso";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("", requestor.getResults());
	assertEquals("completion offset=67\n" +
			"completion range=[63, 66]\n" +
			"completion token=\"syso\"\n" +
			"completion token kind=TOKEN_KIND_NAME\n" +
			"expectedTypesSignatures=null\n" +
			"expectedTypesKeys=null\n" +
			"completion token location={STATEMENT_START}", requestor.getContext());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422901, [1.8][code assist] Code assistant sensitive to scope.referenceContext type identity.
public void test422901a() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"interface I {\n" +
			"	void foo();\n" +
			"}\n" +
			"public class X {\n" +
			"   void foo() {\n" +
			"	    I i = () -> {\n" +
			"		    syso    // no proposals here.\n" +
			"	    };\n" +
			"   }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "syso";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("", requestor.getResults());
	assertEquals("completion offset=91\n" +
			"completion range=[87, 90]\n" +
			"completion token=\"syso\"\n" +
			"completion token kind=TOKEN_KIND_NAME\n" +
			"expectedTypesSignatures=null\n" +
			"expectedTypesKeys=null\n" +
			"completion token location={STATEMENT_START}", requestor.getContext());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426851, [1.8][content assist] content assist for a type use annotation
public void test426851() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"import java.lang.annotation.ElementType;\n" +
			"import java.lang.annotation.Target;\n" +
			"@Target(ElementType.TYPE_USE)\n" +
			"@interface TypeUse {\n" +
			"}\n" +
			"@Ty\n" +
			"interface I {\n" +
			"	default void foo() { }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "Ty";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("TypeUse[TYPE_REF]{TypeUse, , LTypeUse;, null, null, null, null, [131, 133], 52}", requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427532, [1.8][code assist] Completion engine does not like intersection casts
public void test427532() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"import java.io.Serializable;\n" +
			"interface I {\n" +
			"	void foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		I i = (I & Serializable) () -> {};\n" +
			"		syso\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "syso";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("", requestor.getResults());
	assertEquals("completion offset=160\n" +
			"completion range=[156, 159]\n" +
			"completion token=\"syso\"\n" +
			"completion token kind=TOKEN_KIND_NAME\n" +
			"expectedTypesSignatures=null\n" +
			"expectedTypesKeys=null\n" +
			"completion token location={STATEMENT_START}", requestor.getContext());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427532, [1.8][code assist] Completion engine does not like intersection casts
public void test427532a() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"import java.io.Serializable;\n" +
			"interface I {\n" +
			"	void foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		syso\n" +
			"		I i = (I & Serializable) () -> {};\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "syso";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("", requestor.getResults());
	assertEquals("completion offset=123\n" +
			"completion range=[119, 122]\n" +
			"completion token=\"syso\"\n" +
			"completion token kind=TOKEN_KIND_NAME\n" +
			"expectedTypesSignatures=null\n" +
			"expectedTypesKeys=null\n" +
			"completion token location={STATEMENT_START}", requestor.getContext());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427532, [1.8][code assist] Completion engine does not like intersection casts
public void test427532b() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"import java.io.Serializable;\n" +
			"interface I {\n" +
			"	void foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		I i = (I & Serializable) () -> {\n" +
			"                 syso\n" +
			"             };\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "syso";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("", requestor.getResults());
	assertEquals("completion offset=173\n" +
			"completion range=[169, 172]\n" +
			"completion token=\"syso\"\n" +
			"completion token kind=TOKEN_KIND_NAME\n" +
			"expectedTypesSignatures=null\n" +
			"expectedTypesKeys=null\n" +
			"completion token location={STATEMENT_START}", requestor.getContext());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427464, [1.8][content assist] CCE : MethodDeclaration incompatible with CompletionOnAnnotationOfType 
public void test427464() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"@interface Annotation {}\n" +
			"interface FI1 {\n" +
			"	int foo(int x) throws Exception;\n" +
			"}\n" +
			"class Test {\n" +
			"	private void foo() {\n" +
			"		FI1 fi1 = (x) -> { \n" +
			"			@Ann\n" +
			"		};\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "@Ann";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("Annotation[TYPE_REF]{Annotation, , LAnnotation;, null, null, null, null, [138, 141], 47}", requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428735,  [1.8][assist] Missing completion proposals inside lambda body expression - other than first token
public void test428735() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"import java.util.List;\n" +
			"class Person {\n" +
			"   String getLastName() { return null; }\n" +
			"}\n" +
			"public class X {\n" +
			"	void test1 (List<Person> people) {\n" +
			"		people.stream().forEach(p -> System.out.println(p.get)); // NOK\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "p.get";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<*>;, null, null, getClass, null, [186, 189], 35}\n" +
                  "getLastName[METHOD_REF]{getLastName(), LPerson;, ()Ljava.lang.String;, null, null, getLastName, null, [186, 189], 35}", requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428735,  [1.8][assist] Missing completion proposals inside lambda body expression - other than first token
public void test428735a() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"import java.util.List;\n" +
			"class Person {\n" +
			"   String getLastName() { return null; }\n" +
			"}\n" +
			"public class X {\n" +
			"	void test1 (List<Person> people) {\n" +
			"		people.stream().forEach(p -> System.out.println(p.)); // NOK\n" +
			"	}\n" +
			"   void test2(List<Person> people) {\n" +
			"       people.sort((x,y) -> x.get);  // OK\n" +
			"   }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "x.get";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<*>;, null, null, getClass, null, [267, 270], 35}\n" +
                  "getLastName[METHOD_REF]{getLastName(), LPerson;, ()Ljava.lang.String;, null, null, getLastName, null, [267, 270], 35}", requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428735,  [1.8][assist] Missing completion proposals inside lambda body expression - other than first token
public void test428735b() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"import java.util.List;\n" +
			"class Person {\n" +
			"   String getLastName() { return null; }\n" +
			"}\n" +
			"public class X {\n" +
			"	void test1 (List<Person> people) {\n" +
			"		people.stream().forEach(p -> System.out.println(p.)); // NOK\n" +
			"	}\n" +
			"   void test2(List<Person> people) {\n" +
			"       people.sort((x,y) -> x.getLastName().compareTo(y.get));\n" + 
			"   }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "y.get";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<*>;, null, null, getClass, null, [293, 296], 35}\n" +
                  "getLastName[METHOD_REF]{getLastName(), LPerson;, ()Ljava.lang.String;, null, null, getLastName, null, [293, 296], 35}", requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428735,  [1.8][assist] Missing completion proposals inside lambda body expression - other than first token
public void test428735c() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"import java.util.List;\n" +
			"class Person {\n" +
			"   String getLastName() { return null; }\n" +
			"}\n" +
			"public class X {\n" +
			"	void test1 (List<Person> people) {\n" +
			"		people.stream().forEach(p -> System.out.println(p.)); // NOK\n" +
			"	}\n" +
			"   void test2(List<Person> people) {\n" +
			"       people.sort((x,y) -> x.getLastName() + y.get);\n" + 
			"   }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "y.get";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<*>;, null, null, getClass, null, [285, 288], 35}\n" +
                  "getLastName[METHOD_REF]{getLastName(), LPerson;, ()Ljava.lang.String;, null, null, getLastName, null, [285, 288], 65}", requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428735,  [1.8][assist] Missing completion proposals inside lambda body expression - other than first token
public void test428735d() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"import java.util.List;\n" +
			"class Person {\n" +
			"   String getLastName() { return null; }\n" +
			"}\n" +
			"public class X {\n" +
			"	void test1 (List<Person> people) {\n" +
			"		people.stream().forEach(p -> System.out.println(p.)); // NOK\n" +
			"	}\n" +
			"   void test2(List<Person> people) {\n" +
			"       people.sort((x,y) -> \"\" + x.get); \n" + 
			"   }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "x.get";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<*>;, null, null, getClass, null, [272, 275], 35}\n" +
                  "getLastName[METHOD_REF]{getLastName(), LPerson;, ()Ljava.lang.String;, null, null, getLastName, null, [272, 275], 65}", requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428735,  [1.8][assist] Missing completion proposals inside lambda body expression - other than first token
public void test428735e() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"import java.util.List;\n" +
			"class Person {\n" +
			"   String getLastName() { return null; }\n" +
			"}\n" +
			"public class X {\n" +
			"   void test2(List<Person> people) {\n" +
			"       people.sort((x,y) -> {\n" +
			"              if (true) return \"\" + x.get); \n" +
			"              else return \"\";\n" +
			"   }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "x.get";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<*>;, null, null, getClass, null, [203, 206], 35}\n" +
               "getLastName[METHOD_REF]{getLastName(), LPerson;, ()Ljava.lang.String;, null, null, getLastName, null, [203, 206], 65}", requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=402081, [1.8][code complete] No proposals while completing at method/constructor references
public void test402081() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"interface I {\n" +
			"    String foo(String x);\n" +
			"}\n" +
			"public class X {\n" +
			"    public  String longMethodName(String x) {\n" +
			"        return null;\n" +
			"    }\n" +
			"    void foo() {\n" +
			"    	X x = new X();\n" +
			"    	I i = x::long\n" +
			"       System.out.println();\n" +
			"    }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "long";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("longMethodName[METHOD_IMPORT]{longMethodName, LX;, (Ljava.lang.String;)Ljava.lang.String;, null, null, longMethodName, (x), [183, 187], 35}", requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=402081, [1.8][code complete] No proposals while completing at method/constructor references
public void test402081a() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/test/X.java",
				"interface I {\n" +
				"    String foo(String x);\n" +
				"}\n" +
				"public class X {\n" +
				"    public  String longMethodName(String x) {\n" +
				"        return null;\n" +
				"    }\n" +
				"}\n" +
				"public class Y {\n" +
				"    X x;" +
				"    void foo()\n" +
				"    {\n" +
				"    	Y y = new Y();\n" +
				"    	I i = y.x::longMethodN    \n" +
				"    }\n" +
				"}\n");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		String str = this.workingCopies[0].getSource();
		String completeBehind = " y.x::longMethodN";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
			"longMethodName[METHOD_IMPORT]{longMethodName, Ltest.X;, (Ljava.lang.String;)Ljava.lang.String;, longMethodName, (x), 35}",
			requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=402081, [1.8][code complete] No proposals while completing at method/constructor references
public void test402081b() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/test/X.java",
				"interface I {\n" +
				"    String foo(X<String> xs, String x);\n" +
				"}\n" +
				"public class X<T> {\n" +
				"    public  String longMethodName(String x) {\n" +
				"        return null;\n" +
				"    }\n" +
				"    void foo() {\n" +
				"    	I i = X<String>::lo\n" +
				"    }\n" +
				"}\n");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		String str = this.workingCopies[0].getSource();
		String completeBehind = "lo";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
			"longMethodName[METHOD_IMPORT]{longMethodName, Ltest.X<Ljava.lang.String;>;, (Ljava.lang.String;)Ljava.lang.String;, longMethodName, (x), 35}",
			requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=402081, [1.8][code complete] No proposals while completing at method/constructor references
public void test402081c() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/test/X.java",
				"interface I {\n" +
				"    String foo(String x);\n" +
				"}\n" +
				"class Y {\n" +
				"    public  String longMethodName(String x) {\n" +
				"        return null;\n" +
				"    }\n" +
				"}\n" +
				"public class X extends Y {\n" +
				"    void foo() {\n" +
				"    	X x = new X();\n" +
				"    	I i = super::lo;\n" +
				"    }\n" +
				"}\n");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		String str = this.workingCopies[0].getSource();
		String completeBehind = "lo";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
			"longMethodName[METHOD_IMPORT]{longMethodName, Ltest.Y;, (Ljava.lang.String;)Ljava.lang.String;, longMethodName, (x), 35}",
			requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=402081, [1.8][code complete] No proposals while completing at method/constructor references
public void test402081d() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/test/X.java",
				"interface I {\n" +
				"    String foo(String x);\n" +
				"}\n" +
				"class Y {\n" +
				"    public  String longMethodName(String x) {\n" +
				"        return null;\n" +
				"    }\n" +
				"}\n" +
				"public class X extends Y {\n" +
				"    void foo() {\n" +
				"    	X x = new X();\n" +
				"    	I i = this::lo;\n" +
				"    }\n" +
				"}\n");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		String str = this.workingCopies[0].getSource();
		String completeBehind = "lo";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
			"longMethodName[METHOD_IMPORT]{longMethodName, Ltest.Y;, (Ljava.lang.String;)Ljava.lang.String;, longMethodName, (x), 35}",
			requestor.getResults());
}
}
