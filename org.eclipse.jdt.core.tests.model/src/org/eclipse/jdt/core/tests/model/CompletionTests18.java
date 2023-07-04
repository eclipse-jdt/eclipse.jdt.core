/*******************************************************************************
 * Copyright (c) 2014, 2023 IBM Corporation and others.
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

package org.eclipse.jdt.core.tests.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.eval.IEvaluationContext;
import org.eclipse.jdt.internal.codeassist.RelevanceConstants;

import junit.framework.Test;

public class CompletionTests18 extends AbstractJavaModelCompletionTests {

static {
//		TESTS_NAMES = new String[] {"test492947"};
}

public CompletionTests18(String name) {
	super(name);
}

@Override
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
			"first[LOCAL_VARIABLE_REF]{first, null, I, first, null, " + (R_DEFAULT + R_EXACT_EXPECTED_TYPE + 22) + "}",
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
			"first[LOCAL_VARIABLE_REF]{first, null, I, first, null, " + (R_DEFAULT + R_EXACT_EXPECTED_TYPE + 22) + "}",
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
			"CASE_INSENSITIVE_ORDER[FIELD_REF]{CASE_INSENSITIVE_ORDER, Ljava.lang.String;, Ljava.util.Comparator<Ljava.lang.String;>;, CASE_INSENSITIVE_ORDER, null, " + (R_DEFAULT + 9) + "}\n" +
			"copyValueOf[METHOD_REF]{copyValueOf(), Ljava.lang.String;, ([C)Ljava.lang.String;, copyValueOf, (arg0), " + (R_DEFAULT + 19) + "}\n" +
			"copyValueOf[METHOD_REF]{copyValueOf(), Ljava.lang.String;, ([CII)Ljava.lang.String;, copyValueOf, (arg0, arg1, arg2), " + (R_DEFAULT + 19) + "}\n" +
			"chars[METHOD_REF]{chars(), Ljava.lang.CharSequence;, ()Ljava.util.stream.IntStream;, chars, null, " + (R_DEFAULT + 30) + "}\n" +
			"clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, " + (R_DEFAULT + 30) + "}\n" +
			"codePoints[METHOD_REF]{codePoints(), Ljava.lang.CharSequence;, ()Ljava.util.stream.IntStream;, codePoints, null, " + (R_DEFAULT + 30) + "}\n" +
			"concat[METHOD_REF]{concat(), Ljava.lang.String;, (Ljava.lang.String;)Ljava.lang.String;, concat, (arg0), " + (R_DEFAULT + 30) + "}\n" +
			"contains[METHOD_REF]{contains(), Ljava.lang.String;, (Ljava.lang.CharSequence;)Z, contains, (arg0), " + (R_DEFAULT + 30) + "}\n" +
			"contentEquals[METHOD_REF]{contentEquals(), Ljava.lang.String;, (Ljava.lang.CharSequence;)Z, contentEquals, (arg0), " + (R_DEFAULT + 30) + "}\n" +
			"contentEquals[METHOD_REF]{contentEquals(), Ljava.lang.String;, (Ljava.lang.StringBuffer;)Z, contentEquals, (arg0), " + (R_DEFAULT + 30) + "}\n" +
			"charAt[METHOD_REF]{charAt(), Ljava.lang.String;, (I)C, charAt, (arg0), " + (R_DEFAULT + R_EXPECTED_TYPE + 30) + "}\n" +
			"codePointAt[METHOD_REF]{codePointAt(), Ljava.lang.String;, (I)I, codePointAt, (arg0), " + (R_DEFAULT + R_EXACT_EXPECTED_TYPE + 30) + "}\n" +
			"codePointBefore[METHOD_REF]{codePointBefore(), Ljava.lang.String;, (I)I, codePointBefore, (arg0), " + (R_DEFAULT + R_EXACT_EXPECTED_TYPE + 30) + "}\n" +
			"codePointCount[METHOD_REF]{codePointCount(), Ljava.lang.String;, (II)I, codePointCount, (arg0, arg1), " + (R_DEFAULT + R_EXACT_EXPECTED_TYPE + 30) + "}\n" +
			"compareTo[METHOD_REF]{compareTo(), Ljava.lang.String;, (Ljava.lang.String;)I, compareTo, (arg0), " + (R_DEFAULT + R_EXACT_EXPECTED_TYPE + 30) + "}\n" +
			"compareToIgnoreCase[METHOD_REF]{compareToIgnoreCase(), Ljava.lang.String;, (Ljava.lang.String;)I, compareToIgnoreCase, (arg0), " + (R_DEFAULT + R_EXACT_EXPECTED_TYPE + 30) + "}",
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
			"lpx5[LOCAL_VARIABLE_REF]{lpx5, null, I, lpx5, null, " + (R_DEFAULT + 22) + "}\n" +
			"lpx6[LOCAL_VARIABLE_REF]{lpx6, null, I, lpx6, null, " + (R_DEFAULT + 22) + "}",
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
	int relevance = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_NON_RESTRICTED + R_UNQUALIFIED;
	assertResults(
			"argument[LOCAL_VARIABLE_REF]{argument, null, I, argument, null, " + relevance + "}",
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
			"argument[LOCAL_VARIABLE_REF]{argument, null, I, argument, null, " + (R_DEFAULT + 22) + "}",
			requestor.getResults());
}
// corrected syntax (expr w/o enclosing {}) should not give worse result
public void test006b() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"interface I {\n" +
			"	int foo(int x);\n" +
			"}\n" +
			"public class X {\n" +
			"	void go() {\n" +
			"		I i = (argument) -> \n" +
			"			argument == 0 ? arg\n" +
			"		;\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "arg";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults(
			"argument[LOCAL_VARIABLE_REF]{argument, null, Ljava.lang.Object;, argument, null, 51}\n" // FIXME should be "I" and 22 like test006
			+ "[LAMBDA_EXPRESSION]{->, LI;, (I)I, foo, (x), 89}",
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
			"class[FIELD_REF]{class, null, Ljava.lang.Class<LX;>;, class, null, " + (R_DEFAULT + 21) + "}\n" +
			"f[FIELD_REF]{f, LX;, LFoo;, f, null, " + (R_DEFAULT + 21) + "}\n" +
			"x1[FIELD_REF]{x1, LX;, I, x1, null, " + (R_DEFAULT + 51) + "}",
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
			"[POTENTIAL_METHOD_DECLARATION]{, LX;, ()V, , null, " + (R_DEFAULT + 9) + "}\n" +
			"abstract[KEYWORD]{abstract, null, null, abstract, null, " + (R_DEFAULT + 19) + "}\n" +
			"class[KEYWORD]{class, null, null, class, null, " + (R_DEFAULT + 19) + "}\n" +
			"enum[KEYWORD]{enum, null, null, enum, null, " + (R_DEFAULT + 19) + "}\n" +
			"final[KEYWORD]{final, null, null, final, null, " + (R_DEFAULT + 19) + "}\n" +
			"interface[KEYWORD]{interface, null, null, interface, null, " + (R_DEFAULT + 19) + "}\n" +
			"native[KEYWORD]{native, null, null, native, null, " + (R_DEFAULT + 19) + "}\n" +
			"private[KEYWORD]{private, null, null, private, null, " + (R_DEFAULT + 19) + "}\n" +
			"protected[KEYWORD]{protected, null, null, protected, null, " + (R_DEFAULT + 19) + "}\n" +
			"public[KEYWORD]{public, null, null, public, null, " + (R_DEFAULT + 19) + "}\n" +
			"static[KEYWORD]{static, null, null, static, null, " + (R_DEFAULT + 19) + "}\n" +
			"strictfp[KEYWORD]{strictfp, null, null, strictfp, null, " + (R_DEFAULT + 19) + "}\n" +
			"synchronized[KEYWORD]{synchronized, null, null, synchronized, null, " + (R_DEFAULT + 19) + "}\n" +
			"transient[KEYWORD]{transient, null, null, transient, null, " + (R_DEFAULT + 19) + "}\n" +
			"volatile[KEYWORD]{volatile, null, null, volatile, null, " + (R_DEFAULT + 19) + "}\n" +
			"I[TYPE_REF]{I, , LI;, null, null, " + (R_DEFAULT + 22) + "}\n" +
			"J[TYPE_REF]{J, , LJ;, null, null, " + (R_DEFAULT + 22) + "}\n" +
			"X[TYPE_REF]{X, , LX;, null, null, " + (R_DEFAULT + 22) + "}\n" +
			"clone[METHOD_DECLARATION]{protected Object clone() throws CloneNotSupportedException, Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, " + (R_DEFAULT + 22) + "}\n" +
			"equals[METHOD_DECLARATION]{public boolean equals(Object obj), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), " + (R_DEFAULT + 22) + "}\n" +
			"finalize[METHOD_DECLARATION]{protected void finalize() throws Throwable, Ljava.lang.Object;, ()V, finalize, null, " + (R_DEFAULT + 22) + "}\n" +
			"hashCode[METHOD_DECLARATION]{public int hashCode(), Ljava.lang.Object;, ()I, hashCode, null, " + (R_DEFAULT + 22) + "}\n" +
			"toString[METHOD_DECLARATION]{public String toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, " + (R_DEFAULT + 22) + "}",
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
			"[POTENTIAL_METHOD_DECLARATION]{, LX;, ()V, , null, " + (R_DEFAULT + 9) + "}\n" +
			"abstract[KEYWORD]{abstract, null, null, abstract, null, " + (R_DEFAULT + 19) + "}\n" +
			"class[KEYWORD]{class, null, null, class, null, " + (R_DEFAULT + 19) + "}\n" +
			"enum[KEYWORD]{enum, null, null, enum, null, " + (R_DEFAULT + 19) + "}\n" +
			"final[KEYWORD]{final, null, null, final, null, " + (R_DEFAULT + 19) + "}\n" +
			"interface[KEYWORD]{interface, null, null, interface, null, " + (R_DEFAULT + 19) + "}\n" +
			"native[KEYWORD]{native, null, null, native, null, " + (R_DEFAULT + 19) + "}\n" +
			"private[KEYWORD]{private, null, null, private, null, " + (R_DEFAULT + 19) + "}\n" +
			"protected[KEYWORD]{protected, null, null, protected, null, " + (R_DEFAULT + 19) + "}\n" +
			"public[KEYWORD]{public, null, null, public, null, " + (R_DEFAULT + 19) + "}\n" +
			"static[KEYWORD]{static, null, null, static, null, " + (R_DEFAULT + 19) + "}\n" +
			"strictfp[KEYWORD]{strictfp, null, null, strictfp, null, " + (R_DEFAULT + 19) + "}\n" +
			"synchronized[KEYWORD]{synchronized, null, null, synchronized, null, " + (R_DEFAULT + 19) + "}\n" +
			"transient[KEYWORD]{transient, null, null, transient, null, " + (R_DEFAULT + 19) + "}\n" +
			"volatile[KEYWORD]{volatile, null, null, volatile, null, " + (R_DEFAULT + 19) + "}\n" +
			"I[TYPE_REF]{I, , LI;, null, null, " + (R_DEFAULT + 22) + "}\n" +
			"J[TYPE_REF]{J, , LJ;, null, null, " + (R_DEFAULT + 22) + "}\n" +
			"X[TYPE_REF]{X, , LX;, null, null, " + (R_DEFAULT + 22) + "}\n" +
			"clone[METHOD_DECLARATION]{protected Object clone() throws CloneNotSupportedException, Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, " + (R_DEFAULT + 22) + "}\n" +
			"equals[METHOD_DECLARATION]{public boolean equals(Object obj), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), " + (R_DEFAULT + 22) + "}\n" +
			"finalize[METHOD_DECLARATION]{protected void finalize() throws Throwable, Ljava.lang.Object;, ()V, finalize, null, " + (R_DEFAULT + 22) + "}\n" +
			"hashCode[METHOD_DECLARATION]{public int hashCode(), Ljava.lang.Object;, ()I, hashCode, null, " + (R_DEFAULT + 22) + "}\n" +
			"toString[METHOD_DECLARATION]{public String toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, " + (R_DEFAULT + 22) + "}",
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
			"goo[METHOD_REF]{goo(), LX;, (LI;)V, goo, (i), " + (R_DEFAULT + 19) + "}\n" +
			"goo[METHOD_REF]{goo(), LX;, (Ljava.lang.String;)V, goo, (s), " + (R_DEFAULT + 19) + "}\n" +
			"main[METHOD_REF]{main(), LX;, ([Ljava.lang.String;)V, main, (args), " + (R_DEFAULT + 19) + "}\n" +
			"clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, " + (R_DEFAULT + 30) + "}\n" +
			"equals[METHOD_REF]{equals(), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), " + (R_DEFAULT + 30) + "}\n" +
			"finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, " + (R_DEFAULT + 30) + "}\n" +
			"getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<*>;, getClass, null, " + (R_DEFAULT + 30) + "}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, " + (R_DEFAULT + 30) + "}\n" +
			"notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, " + (R_DEFAULT + 30) + "}\n" +
			"notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, " + (R_DEFAULT + 30) + "}\n" +
			"toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, " + (R_DEFAULT + 30) + "}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, " + (R_DEFAULT + 30) + "}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, wait, (millis), " + (R_DEFAULT + 30) + "}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), " + (R_DEFAULT + 30) + "}",
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
	int relevance = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED;
	assertResults("arrayOfStrings[LOCAL_VARIABLE_REF]{arrayOfStrings, null, [Ljava.lang.String;, null, null, arrayOfStrings, null, [168, 174], " + (R_DEFAULT + 22) + "}\n" +
					"arrayOfInts[LOCAL_VARIABLE_REF]{arrayOfInts, null, [I, null, null, arrayOfInts, null, [168, 174], " + relevance + "}", requestor.getResults());
}
//https://github.com/eclipse-jdt/eclipse.jdt.core/issues/650, Templates not working in Lambda internal block.
public void test015a() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"interface I {\n" +
			"	void foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"       {\n" +
			"		I i = () -> {\n" +
			"           {\n" +
			"               syso\n" +
			"           }\n" +
			"		};\n" +
			"       }\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "syso";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("", requestor.getResults());
	assertEquals("completion offset=145\n" +
			"completion range=[141, 144]\n" +
			"completion token=\"syso\"\n" +
			"completion token kind=TOKEN_KIND_NAME\n" +
			"expectedTypesSignatures=null\n" +
			"expectedTypesKeys=null\n" +
			"completion token location={STATEMENT_START}", requestor.getContext());
}
//https://github.com/eclipse-jdt/eclipse.jdt.core/issues/650, Templates not working in Lambda internal block.
public void test015b() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"interface I {\n" +
			"	void foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"       {\n" +
			"		I i = () -> {\n" +
			"           if (args.length > 3) {\n" +
			"               syso\n" +
			"           }\n" +
			"		};\n" +
			"       }\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "syso";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("", requestor.getResults());
	assertEquals("completion offset=166\n" +
			"completion range=[162, 165]\n" +
			"completion token=\"syso\"\n" +
			"completion token kind=TOKEN_KIND_NAME\n" +
			"expectedTypesSignatures=null\n" +
			"expectedTypesKeys=null\n" +
			"completion token location={STATEMENT_START}", requestor.getContext());
}
//https://github.com/eclipse-jdt/eclipse.jdt.core/issues/650, Templates not working in Lambda internal block.
public void test015c() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"interface I {\n" +
			"	void foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"       {\n" +
			"		I i = () -> {\n" +
			"           {\n" +
			"               if (args.length > 3)\n" +
			"                   syso\n" +
			"           }\n" +
			"		};\n" +
			"       }\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "syso";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("", requestor.getResults());
	assertEquals("completion offset=185\n" +
			"completion range=[181, 184]\n" +
			"completion token=\"syso\"\n" +
			"completion token kind=TOKEN_KIND_NAME\n" +
			"expectedTypesSignatures=null\n" +
			"expectedTypesKeys=null\n" +
			"completion token location={STATEMENT_START}", requestor.getContext());
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
	assertResults("xyzBefore[LOCAL_VARIABLE_REF]{xyzBefore, null, Ljava.lang.Object;, null, null, xyzBefore, null, [163, 166], " + (R_DEFAULT + 21) + "}", requestor.getResults());
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
			"expectedTypesSignatures={Z,C,I,J,F,D,[C,Ljava.lang.String;,Ljava.lang.Object;}\n" +
			"expectedTypesKeys={Z,C,I,J,F,D,[C,Ljava/lang/String;,Ljava/lang/Object;}\n" +
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
			"	xLambdaLocal [in main(String[]) [in X [in [Working copy] X.java [in <default> [in src [in Completion]]]]]],\n" +
			"	xyz [in main(String[]) [in X [in [Working copy] X.java [in <default> [in src [in Completion]]]]]],\n" +
			"	xLocal [in main(String[]) [in X [in [Working copy] X.java [in <default> [in src [in Completion]]]]]],\n" +
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
	assertResults("StringBufferInputStream[TYPE_REF]{java.io.StringBufferInputStream, java.io, Ljava.io.StringBufferInputStream;, null, null, null, null, [155, 159], " + (R_DEFAULT + 19) + "}\n" +
			"StringCharBuffer[TYPE_REF]{java.nio.StringCharBuffer, java.nio, Ljava.nio.StringCharBuffer;, null, null, null, null, [155, 159], " + (R_DEFAULT + 19) + "}\n" +
			"StringCharacterIterator[TYPE_REF]{java.text.StringCharacterIterator, java.text, Ljava.text.StringCharacterIterator;, null, null, null, null, [155, 159], " + (R_DEFAULT + 19) + "}\n" +
			"StringJoiner[TYPE_REF]{java.util.StringJoiner, java.util, Ljava.util.StringJoiner;, null, null, null, null, [155, 159], " + (R_DEFAULT + 19) + "}\n" +
			"StringReader[TYPE_REF]{java.io.StringReader, java.io, Ljava.io.StringReader;, null, null, null, null, [155, 159], " + (R_DEFAULT + 19) + "}\n" +
			"StringTokenizer[TYPE_REF]{java.util.StringTokenizer, java.util, Ljava.util.StringTokenizer;, null, null, null, null, [155, 159], " + (R_DEFAULT + 19) + "}\n" +
			"StringWriter[TYPE_REF]{java.io.StringWriter, java.io, Ljava.io.StringWriter;, null, null, null, null, [155, 159], " + (R_DEFAULT + 19) + "}\n" +
			"StrictMath[TYPE_REF]{StrictMath, java.lang, Ljava.lang.StrictMath;, null, null, null, null, [155, 159], " + (R_DEFAULT + 22) + "}\n" +
			"String[TYPE_REF]{String, java.lang, Ljava.lang.String;, null, null, null, null, [155, 159], " + (R_DEFAULT + 22) + "}\n" +
			"StringBuffer[TYPE_REF]{StringBuffer, java.lang, Ljava.lang.StringBuffer;, null, null, null, null, [155, 159], " + (R_DEFAULT + 22) + "}\n" +
			"StringBuilder[TYPE_REF]{StringBuilder, java.lang, Ljava.lang.StringBuilder;, null, null, null, null, [155, 159], " + (R_DEFAULT + 22) + "}\n" +
			"StringCoding[TYPE_REF]{StringCoding, java.lang, Ljava.lang.StringCoding;, null, null, null, null, [155, 159], " + (R_DEFAULT + 22) + "}\n" +
			"StringIndexOutOfBoundsException[TYPE_REF]{StringIndexOutOfBoundsException, java.lang, Ljava.lang.StringIndexOutOfBoundsException;, null, null, null, null, [155, 159], " + (R_DEFAULT + 22) + "}\n" +
			"StringParameter[LOCAL_VARIABLE_REF]{StringParameter, null, LX;, null, null, StringParameter, null, [155, 159], " + (R_DEFAULT + 22) + "}", requestor.getResults());
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
	assertResults("StringParameter[LOCAL_VARIABLE_REF]{StringParameter, null, LX;, null, null, StringParameter, null, [155, 162], " + (R_DEFAULT + 22) + "}", requestor.getResults());
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
	assertResults("goo[METHOD_REF]{goo(), LX;, (LI;)V, null, null, goo, (i), [173, 173], " + (R_DEFAULT + 19) + "}\n" +
			"main[METHOD_REF]{main(), LX;, ([Ljava.lang.String;)V, null, null, main, (args), [173, 173], " + (R_DEFAULT + 19) + "}\n" +
			"clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, null, null, clone, null, [173, 173], " + (R_DEFAULT + 30) + "}\n" +
			"equals[METHOD_REF]{equals(), Ljava.lang.Object;, (Ljava.lang.Object;)Z, null, null, equals, (obj), [173, 173], " + (R_DEFAULT + 30) + "}\n" +
			"field[FIELD_REF]{field, LX;, I, null, null, field, null, [173, 173], " + (R_DEFAULT + 30) + "}\n" +
			"finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, null, null, finalize, null, [173, 173], " + (R_DEFAULT + 30) + "}\n" +
			"foo[METHOD_REF]{foo(), LX;, ()V, null, null, foo, null, [173, 173], " + (R_DEFAULT + 30) + "}\n" +
			"getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<*>;, null, null, getClass, null, [173, 173], " + (R_DEFAULT + 30) + "}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, null, null, hashCode, null, [173, 173], " + (R_DEFAULT + 30) + "}\n" +
			"notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, null, null, notify, null, [173, 173], " + (R_DEFAULT + 30) + "}\n" +
			"notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, null, null, notifyAll, null, [173, 173], " + (R_DEFAULT + 30) + "}\n" +
			"toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, null, null, toString, null, [173, 173], " + (R_DEFAULT + 30) + "}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, null, null, wait, null, [173, 173], " + (R_DEFAULT + 30) + "}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, null, null, wait, (millis), [173, 173], " + (R_DEFAULT + 30) + "}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, null, null, wait, (millis, nanos), [173, 173], " + (R_DEFAULT + 30) + "}", requestor.getResults());
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
	assertResults("goo[METHOD_REF]{goo(), LX;, (LI;)V, null, null, goo, (i), [173, 173], " + (R_DEFAULT + 19) + "}\n" +
			"main[METHOD_REF]{main(), LX;, ([Ljava.lang.String;)V, null, null, main, (args), [173, 173], " + (R_DEFAULT + 19) + "}\n" +
			"clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, null, null, clone, null, [173, 173], " + (R_DEFAULT + 30) + "}\n" +
			"equals[METHOD_REF]{equals(), Ljava.lang.Object;, (Ljava.lang.Object;)Z, null, null, equals, (obj), [173, 173], " + (R_DEFAULT + 30) + "}\n" +
			"field[FIELD_REF]{field, LX;, I, null, null, field, null, [173, 173], " + (R_DEFAULT + 30) + "}\n" +
			"finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, null, null, finalize, null, [173, 173], " + (R_DEFAULT + 30) + "}\n" +
			"foo[METHOD_REF]{foo(), LX;, ()V, null, null, foo, null, [173, 173], " + (R_DEFAULT + 30) + "}\n" +
			"getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<*>;, null, null, getClass, null, [173, 173], " + (R_DEFAULT + 30) + "}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, null, null, hashCode, null, [173, 173], " + (R_DEFAULT + 30) + "}\n" +
			"notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, null, null, notify, null, [173, 173], " + (R_DEFAULT + 30) + "}\n" +
			"notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, null, null, notifyAll, null, [173, 173], " + (R_DEFAULT + 30) + "}\n" +
			"toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, null, null, toString, null, [173, 173], " + (R_DEFAULT + 30) + "}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, null, null, wait, null, [173, 173], " + (R_DEFAULT + 30) + "}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, null, null, wait, (millis), [173, 173], " + (R_DEFAULT + 30) + "}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, null, null, wait, (millis, nanos), [173, 173], " + (R_DEFAULT + 30) + "}", requestor.getResults());
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
	    	"bars[METHOD_REF]{bars(), LB;, ()I, bars, null, " + (R_DEFAULT + R_EXACT_EXPECTED_TYPE + 22) + "}",
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
	    	"another[FIELD_REF]{another, LB;, I, another, null, " + (R_DEFAULT + R_EXACT_EXPECTED_TYPE + 22) + "}",
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
	assertResults(
			"tryit[LOCAL_VARIABLE_REF]{tryit, null, I, null, null, tryit, null, [99, 102], " + (R_DEFAULT + 22) + "}\n" +
			"try[KEYWORD]{try, null, null, null, null, try, null, [99, 102], " + (R_DEFAULT + 23) + "}",
			requestor.getResults());
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
	assertResults("TypeUse[TYPE_REF]{TypeUse, , LTypeUse;, null, null, null, null, [131, 133], " + (R_DEFAULT + 47) + "}", requestor.getResults());
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
	assertResults("Annotation[TYPE_REF]{Annotation, , LAnnotation;, null, null, null, null, [138, 141], " + (R_DEFAULT + 42) + "}", requestor.getResults());
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
	assertResults("getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<*>;, null, null, getClass, null, [186, 189], " + (R_DEFAULT + R_PACKAGE_EXPECTED_TYPE + 30) + "}\n" +
                  "getLastName[METHOD_REF]{getLastName(), LPerson;, ()Ljava.lang.String;, null, null, getLastName, null, [186, 189], " + (R_DEFAULT + R_EXACT_EXPECTED_TYPE + 30) + "}", requestor.getResults());
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
	assertResults("getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<*>;, null, null, getClass, null, [267, 270], " + (R_DEFAULT + 30) + "}\n" +
                  "getLastName[METHOD_REF]{getLastName(), LPerson;, ()Ljava.lang.String;, null, null, getLastName, null, [267, 270], " + (R_DEFAULT + 30) + "}", requestor.getResults());
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
	assertResults("getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<*>;, null, null, getClass, null, [293, 296], " + (R_DEFAULT + 30) + "}\n" +
                  "getLastName[METHOD_REF]{getLastName(), LPerson;, ()Ljava.lang.String;, null, null, getLastName, null, [293, 296], " + (R_DEFAULT + R_EXACT_EXPECTED_TYPE + 30) + "}", requestor.getResults());
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
	assertResults("getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<*>;, null, null, getClass, null, [285, 288], " + (R_DEFAULT + 30) + "}\n" +
                  "getLastName[METHOD_REF]{getLastName(), LPerson;, ()Ljava.lang.String;, null, null, getLastName, null, [285, 288], " + (R_DEFAULT + 60) + "}", requestor.getResults());
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
	assertResults("getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<*>;, null, null, getClass, null, [272, 275], " + (R_DEFAULT + 30) + "}\n" +
                  "getLastName[METHOD_REF]{getLastName(), LPerson;, ()Ljava.lang.String;, null, null, getLastName, null, [272, 275], " + (R_DEFAULT + 60) + "}", requestor.getResults());
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
	assertResults("getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<*>;, null, null, getClass, null, [203, 206], " + (R_DEFAULT + 30) + "}\n" +
               "getLastName[METHOD_REF]{getLastName(), LPerson;, ()Ljava.lang.String;, null, null, getLastName, null, [203, 206], " + (R_DEFAULT + 60) + "}", requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=428735,  [1.8][assist] Missing completion proposals inside lambda body expression - other than first token
public void test428735f() throws JavaModelException {
	// copy of test428735e with corrected syntax
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
			"              if (true) return \"\" + x.get; \n" +
			"              else return \"\";});\n" +
			"   }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "x.get";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<*>;, null, null, getClass, null, [203, 206], " + (R_DEFAULT + 30) + "}\n" +
            "getLastName[METHOD_REF]{getLastName(), LPerson;, ()Ljava.lang.String;, null, null, getLastName, null, [203, 206], " + (R_DEFAULT + 60) + "}", requestor.getResults());
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
	assertResults("longMethodName[METHOD_NAME_REFERENCE]{longMethodName, LX;, (Ljava.lang.String;)Ljava.lang.String;, null, null, longMethodName, (x), [183, 187], " + (R_DEFAULT + 30) + "}", requestor.getResults());
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
			"longMethodName[METHOD_NAME_REFERENCE]{longMethodName, Ltest.X;, (Ljava.lang.String;)Ljava.lang.String;, longMethodName, (x), " + (R_DEFAULT + 30) + "}",
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
			"longMethodName[METHOD_NAME_REFERENCE]{longMethodName, Ltest.X<Ljava.lang.String;>;, (Ljava.lang.String;)Ljava.lang.String;, longMethodName, (x), " + (R_DEFAULT + 30) + "}",
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
			"longMethodName[METHOD_NAME_REFERENCE]{longMethodName, Ltest.Y;, (Ljava.lang.String;)Ljava.lang.String;, longMethodName, (x), " + (R_DEFAULT + 30) + "}",
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
			"longMethodName[METHOD_NAME_REFERENCE]{longMethodName, Ltest.Y;, (Ljava.lang.String;)Ljava.lang.String;, longMethodName, (x), " + (R_DEFAULT + 30) + "}",
			requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=431402, [assist] NPE in AssistParser.triggerRecoveryUponLambdaClosure:483 using Content Assist
public void test431402() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/test/X.java",
				"import java.util.function.Predicate;\n" +
				"public class X {\n" +
				"	private static void writeIt(Object list) {\n" +
				"		list = replace(s -> true);\n" +
				"		Object asList = null;\n" +
				"		if(Boolean.TRUE) {\n" +
				"			Object s = removeAll(asli);\n" +
				"		}\n" +
				"	}\n" +
				"	private static Object replace(Predicate<String> tester) { return tester; }\n" +
				"	Object removeAll(Object o1) { return o1; }\n" +
				"}\n");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		String str = this.workingCopies[0].getSource();
		String completeBehind = "asli";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"asList[LOCAL_VARIABLE_REF]{asList, null, Ljava.lang.Object;, asList, null, " + (R_DEFAULT + 42) + "}",
			requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=432527, Content Assist crashes sometimes using JDK8
public void test432527() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
						"/Completion/src/test/X.java",
						"import java.util.LinkedList;\n" +
						"import java.util.List;\n" +
						"public class X {\n" +
						"	private Map	map;\n" +
						"	public X() {\n" +
						"		map = new Map();\n" +
						"	}\n" +
						"	public LinkedList<Node> getPath(int xFrom, int yFrom, int xTo, int yTo) {\n" +
						"		LinkedList<Node> result = new LinkedList<>();\n" +
						"		Node node = null;\n" +
						"		int[] nodeCoords = null;\n" +
						"		boolean nodeAdded = false;\n" +
						"		if (nodeCoords != null) {\n" +
						"			// something\n" +
						"		}\n" +
						"		else {\n" +
						"			node = map.getGraph()\n" +
						"					.getNodes()\n" +
						"					.stream()\n" +
						"					.filter((n) -> (n.x() / 100) == (xTo / 100) && (n.y() / 100) == (yTo / 100))\n" +
						"					.min((n1, n2) -> (int) Math.round(Math.sqrt(Math.pow(n1.x() - xTo, 2) + Math.pow(n1.y() - yTo, 2)) - Math.sqrt(Math.pow(n2.x() - xTo, 2) + Math.pow(n2.y() - yTo, 2))))\n" +
						"					.get();\n" +
						"			nodeAdded = true;\n" +
						"		}\n" +
						"		if (nodeAdded) {\n" +
						"			 /*here*/remov\n" +
						"		}\n" +
						"		return result;\n" +
						"	}\n" +
						"	\n" +
						"	private void removeNodeFromGraph(Node node) {\n" +
						"		map.getGraph().removeNode(node.id());\n" +
						"	}\n" +
						"	\n" +
						"	\n" +
						"	public class Map {\n" +
						"		Graph graph = new Graph();\n" +
						"		\n" +
						"		public Graph getGraph() {return graph;}\n" +
						"	}\n" +
						"	\n" +
						"	public class Graph {\n" +
						"		List<Node> nodes;\n" +
						"		\n" +
						"		public List<Node> getNodes() {return nodes;}\n" +
						"		public void addNode(Node node) {nodes.add(node);}\n" +
						"		public void removeNode(Node node) {nodes.remove(node);}\n" +
						"		public void removeNode(int id) {nodes.remove(nodes.stream().filter(node -> id == node.id()).findFirst());}\n" +
						"	}\n" +
						"	public class Node {\n" +
						"		public int id() {return hashCode();}\n" +
						"		public int x() {return 0;}\n" +
						"		public int y() {return 0;}\n" +
						"	}\n" +
						"}\n");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		String str = this.workingCopies[0].getSource();
		String completeBehind = "/*here*/remov";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"removeNodeFromGraph[METHOD_REF]{removeNodeFromGraph(), Ltest.X;, (Ltest.X$Node;)V, removeNodeFromGraph, (node), " + (R_DEFAULT + 22) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=430441,  [compiler] NPE in ImplicitNullAnnotationVerifier.collectOverriddenMethods from Content Assist in a .jpage file
public void test430441() throws JavaModelException {
	String str = "String str = \"foo\";\n" +
			"str.";
	String completeBehind = "str";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length() + 1;
	IJavaProject javaProject = getJavaProject("Completion");

	Map<String, String> options = javaProject.getOptions(true);
	try {
		Map<String, String> customOptions = new HashMap<String, String>(options);
		customOptions.put(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);
		customOptions.put(JavaCore.COMPILER_INHERIT_NULL_ANNOTATIONS, JavaCore.ENABLED);
		javaProject.setOptions(customOptions);

		IEvaluationContext context = javaProject.newEvaluationContext();
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		context.codeComplete(str, cursorLocation, requestor);
	} finally {
		javaProject.setOptions(options);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430656, [1.8][content assist] Content assist does not work for method reference argument
public void test430656() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/test/X.java",
				"import java.util.ArrayList;\n" +
				"import java.util.Collections;\n" +
				"import java.util.Comparator;\n" +
				"import java.util.List;\n" +
				"public class X {\n" +
				"	public void bar() {\n" +
				"		List<Person> people = new ArrayList<>();\n" +
				"		Collections.sort(people, Comparator.comparing(Person::get)); \n" +
				"	}\n" +
				"}\n" +
				"class Person {\n" +
				"	String getLastName() {\n" +
				"		return null;\n" +
				"	}\n" +
				"}\n");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		String str = this.workingCopies[0].getSource();
		String completeBehind = "get";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
			"getClass[METHOD_NAME_REFERENCE]{getClass, Ljava.lang.Object;, ()Ljava.lang.Class<*>;, getClass, null, " + (R_DEFAULT + 30) + "}\n" +
			"getLastName[METHOD_NAME_REFERENCE]{getLastName, Ltest.Person;, ()Ljava.lang.String;, getLastName, null, " + (R_DEFAULT + 30) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=433178
public void test433178() throws JavaModelException {
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
			"    	I i = x::ne\n" +
			"       System.out.println();\n" +
			"    }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "ne";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("new[KEYWORD]{new, null, null, null, null, new, null, [183, 185], " +
											(RelevanceConstants.R_DEFAULT + RelevanceConstants.R_RESOLVED + RelevanceConstants.R_INTERESTING + RelevanceConstants.R_NON_RESTRICTED
											+ RelevanceConstants.R_CASE + RelevanceConstants.R_UNQUALIFIED) + "}", requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=433178
public void test433178a() throws JavaModelException {
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
			"    	I i = I::ne\n" +
			"       System.out.println();\n" +
			"    }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "ne";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("", requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=435219, [1.8][content assist] No proposals for some closure cases
public void test435219() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		new Thread(()->System.o);\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "System.o";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("out[FIELD_REF]{out, Ljava.lang.System;, Ljava.io.PrintStream;, null, null, out, null, [83, 84], " + (R_DEFAULT + 21) + "}", requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=435219, [1.8][content assist] No proposals for some closure cases
public void test435219a() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		new Thread(()->System.out.p);\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "System.out.p";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("print[METHOD_REF]{print(), Ljava.io.PrintStream;, (C)V, null, null, print, (arg0), [87, 88], " + (R_DEFAULT + 30) + "}\n" +
			"print[METHOD_REF]{print(), Ljava.io.PrintStream;, (D)V, null, null, print, (arg0), [87, 88], " + (R_DEFAULT + 30) + "}\n" +
			"print[METHOD_REF]{print(), Ljava.io.PrintStream;, (F)V, null, null, print, (arg0), [87, 88], " + (R_DEFAULT + 30) + "}\n" +
			"print[METHOD_REF]{print(), Ljava.io.PrintStream;, (I)V, null, null, print, (arg0), [87, 88], " + (R_DEFAULT + 30) + "}\n" +
			"print[METHOD_REF]{print(), Ljava.io.PrintStream;, (J)V, null, null, print, (arg0), [87, 88], " + (R_DEFAULT + 30) + "}\n" +
			"print[METHOD_REF]{print(), Ljava.io.PrintStream;, (Ljava.lang.Object;)V, null, null, print, (arg0), [87, 88], " + (R_DEFAULT + 30) + "}\n" +
			"print[METHOD_REF]{print(), Ljava.io.PrintStream;, (Ljava.lang.String;)V, null, null, print, (arg0), [87, 88], " + (R_DEFAULT + 30) + "}\n" +
			"print[METHOD_REF]{print(), Ljava.io.PrintStream;, (Z)V, null, null, print, (arg0), [87, 88], " + (R_DEFAULT + 30) + "}\n" +
			"print[METHOD_REF]{print(), Ljava.io.PrintStream;, ([C)V, null, null, print, (arg0), [87, 88], " + (R_DEFAULT + 30) + "}\n" +
			"printf[METHOD_REF]{printf(), Ljava.io.PrintStream;, (Ljava.lang.String;[Ljava.lang.Object;)Ljava.io.PrintStream;, null, null, printf, (arg0, arg1), [87, 88], " + (R_DEFAULT + 30) + "}\n" +
			"printf[METHOD_REF]{printf(), Ljava.io.PrintStream;, (Ljava.util.Locale;Ljava.lang.String;[Ljava.lang.Object;)Ljava.io.PrintStream;, null, null, printf, (arg0, arg1, arg2), [87, 88], " + (R_DEFAULT + 30) + "}\n" +
			"println[METHOD_REF]{println(), Ljava.io.PrintStream;, ()V, null, null, println, null, [87, 88], " + (R_DEFAULT + 30) + "}\n" +
			"println[METHOD_REF]{println(), Ljava.io.PrintStream;, (C)V, null, null, println, (arg0), [87, 88], " + (R_DEFAULT + 30) + "}\n" +
			"println[METHOD_REF]{println(), Ljava.io.PrintStream;, (D)V, null, null, println, (arg0), [87, 88], " + (R_DEFAULT + 30) + "}\n" +
			"println[METHOD_REF]{println(), Ljava.io.PrintStream;, (F)V, null, null, println, (arg0), [87, 88], " + (R_DEFAULT + 30) + "}\n" +
			"println[METHOD_REF]{println(), Ljava.io.PrintStream;, (I)V, null, null, println, (arg0), [87, 88], " + (R_DEFAULT + 30) + "}\n" +
			"println[METHOD_REF]{println(), Ljava.io.PrintStream;, (J)V, null, null, println, (arg0), [87, 88], " + (R_DEFAULT + 30) + "}\n" +
			"println[METHOD_REF]{println(), Ljava.io.PrintStream;, (Ljava.lang.Object;)V, null, null, println, (arg0), [87, 88], " + (R_DEFAULT + 30) + "}\n" +
			"println[METHOD_REF]{println(), Ljava.io.PrintStream;, (Ljava.lang.String;)V, null, null, println, (arg0), [87, 88], " + (R_DEFAULT + 30) + "}\n" +
			"println[METHOD_REF]{println(), Ljava.io.PrintStream;, (Z)V, null, null, println, (arg0), [87, 88], " + (R_DEFAULT + 30) + "}\n" +
			"println[METHOD_REF]{println(), Ljava.io.PrintStream;, ([C)V, null, null, println, (arg0), [87, 88], " + (R_DEFAULT + 30) + "}", requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=435219, [1.8][content assist] No proposals for some closure cases
public void test435219b() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		new Thread(()->System.out.println(\"foo\")).st);\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "st";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("start[METHOD_REF]{start(), Ljava.lang.Thread;, ()V, null, null, start, null, [103, 105], " + (R_DEFAULT + 30) + "}\n" +
			"stop[METHOD_REF]{stop(), Ljava.lang.Thread;, ()V, null, null, stop, null, [103, 105], " + (R_DEFAULT + 30) + "}\n" +
			"stop[METHOD_REF]{stop(), Ljava.lang.Thread;, (Ljava.lang.Throwable;)V, null, null, stop, (arg0), [103, 105], " + (R_DEFAULT + 30) + "}", requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=435219, [1.8][content assist] No proposals for some closure cases
public void test435219c() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"import java.util.Arrays;\n" +
			"import java.util.List;\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		List<Integer> list = Arrays.asList(1, 2, 3);\n" +
			"		list.stream().map((x) -> x * x.h);\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "x.h";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("hashCode[METHOD_REF]{hashCode(), Ljava.lang.Integer;, (I)I, null, null, hashCode, (arg0), [187, 188], " + (R_DEFAULT + 49) + "}\n" +
			"highestOneBit[METHOD_REF]{highestOneBit(), Ljava.lang.Integer;, (I)I, null, null, highestOneBit, (arg0), [187, 188], " + (R_DEFAULT + 49) + "}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.lang.Integer;, ()I, null, null, hashCode, null, [187, 188], " + (R_DEFAULT + 60) + "}", requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=435219, [1.8][content assist] No proposals for some closure cases
public void test435219d() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"import java.util.Arrays;\n" +
			"import java.util.List;\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		List<Integer> list = Arrays.asList(1, 2, 3);\n" +
			"		list.stream().map((x) -> x * x.hashCode()).forEach(System.out::pri);\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "pri";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("print[METHOD_NAME_REFERENCE]{print, Ljava.io.PrintStream;, (C)V, null, null, print, null, [219, 222], " + (R_DEFAULT + 25) + "}\n" +
			"print[METHOD_NAME_REFERENCE]{print, Ljava.io.PrintStream;, (D)V, null, null, print, null, [219, 222], " + (R_DEFAULT + 25) + "}\n" +
			"print[METHOD_NAME_REFERENCE]{print, Ljava.io.PrintStream;, (F)V, null, null, print, null, [219, 222], " + (R_DEFAULT + 25) + "}\n" +
			"print[METHOD_NAME_REFERENCE]{print, Ljava.io.PrintStream;, (I)V, null, null, print, null, [219, 222], " + (R_DEFAULT + 25) + "}\n" +
			"print[METHOD_NAME_REFERENCE]{print, Ljava.io.PrintStream;, (J)V, null, null, print, null, [219, 222], " + (R_DEFAULT + 25) + "}\n" +
			"print[METHOD_NAME_REFERENCE]{print, Ljava.io.PrintStream;, (Ljava.lang.Object;)V, null, null, print, null, [219, 222], " + (R_DEFAULT + 25) + "}\n" +
			"print[METHOD_NAME_REFERENCE]{print, Ljava.io.PrintStream;, (Ljava.lang.String;)V, null, null, print, null, [219, 222], " + (R_DEFAULT + 25) + "}\n" +
			"print[METHOD_NAME_REFERENCE]{print, Ljava.io.PrintStream;, (Z)V, null, null, print, null, [219, 222], " + (R_DEFAULT + 25) + "}\n" +
			"print[METHOD_NAME_REFERENCE]{print, Ljava.io.PrintStream;, ([C)V, null, null, print, null, [219, 222], " + (R_DEFAULT + 25) + "}\n" +
			"println[METHOD_NAME_REFERENCE]{println, Ljava.io.PrintStream;, ()V, null, null, println, null, [219, 222], " + (R_DEFAULT + 25) + "}\n" +
			"println[METHOD_NAME_REFERENCE]{println, Ljava.io.PrintStream;, (C)V, null, null, println, null, [219, 222], " + (R_DEFAULT + 25) + "}\n" +
			"println[METHOD_NAME_REFERENCE]{println, Ljava.io.PrintStream;, (D)V, null, null, println, null, [219, 222], " + (R_DEFAULT + 25) + "}\n" +
			"println[METHOD_NAME_REFERENCE]{println, Ljava.io.PrintStream;, (F)V, null, null, println, null, [219, 222], " + (R_DEFAULT + 25) + "}\n" +
			"println[METHOD_NAME_REFERENCE]{println, Ljava.io.PrintStream;, (I)V, null, null, println, null, [219, 222], " + (R_DEFAULT + 25) + "}\n" +
			"println[METHOD_NAME_REFERENCE]{println, Ljava.io.PrintStream;, (J)V, null, null, println, null, [219, 222], " + (R_DEFAULT + 25) + "}\n" +
			"println[METHOD_NAME_REFERENCE]{println, Ljava.io.PrintStream;, (Ljava.lang.Object;)V, null, null, println, null, [219, 222], " + (R_DEFAULT + 25) + "}\n" +
			"println[METHOD_NAME_REFERENCE]{println, Ljava.io.PrintStream;, (Ljava.lang.String;)V, null, null, println, null, [219, 222], " + (R_DEFAULT + 25) + "}\n" +
			"println[METHOD_NAME_REFERENCE]{println, Ljava.io.PrintStream;, (Z)V, null, null, println, null, [219, 222], " + (R_DEFAULT + 25) + "}\n" +
			"println[METHOD_NAME_REFERENCE]{println, Ljava.io.PrintStream;, ([C)V, null, null, println, null, [219, 222], " + (R_DEFAULT + 25) + "}\n" +
			"printf[METHOD_NAME_REFERENCE]{printf, Ljava.io.PrintStream;, (Ljava.lang.String;[Ljava.lang.Object;)Ljava.io.PrintStream;, null, null, printf, null, [219, 222], " + (R_DEFAULT + 30) + "}\n" +
			"printf[METHOD_NAME_REFERENCE]{printf, Ljava.io.PrintStream;, (Ljava.util.Locale;Ljava.lang.String;[Ljava.lang.Object;)Ljava.io.PrintStream;, null, null, printf, null, [219, 222], " + (R_DEFAULT + 30) + "}", requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=435219, [1.8][content assist] No proposals for some closure cases
public void test435219e() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"import java.util.Arrays;\n" +
			"import java.util.List;\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		List<Integer> costBeforeTax = Arrays.asList(100, 200, 300);\n" +
			"		   double bill = costBeforeTax.stream().map((cost) -> cost + 0.19 * cost)\n" +
			"		        //                        .y                   .n             .y\n" +
			"		      .reduce((sum, cost) -> sum.dou\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "dou";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("doubleToLongBits[METHOD_REF]{doubleToLongBits(), Ljava.lang.Double;, (D)J, null, null, doubleToLongBits, (arg0), [355, 358], " + (R_DEFAULT + 19) + "}\n" +
			"doubleToRawLongBits[METHOD_REF]{doubleToRawLongBits(), Ljava.lang.Double;, (D)J, null, null, doubleToRawLongBits, (arg0), [355, 358], " + (R_DEFAULT + 19) + "}\n" +
			"doubleValue[METHOD_REF]{doubleValue(), Ljava.lang.Double;, ()D, null, null, doubleValue, null, [355, 358], " + (R_DEFAULT + R_EXPECTED_TYPE + 30) + "}", requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=435219, [1.8][content assist] No proposals for some closure cases
public void test435219f() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"import java.util.Arrays;\n" +
			"import java.util.List;\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		List<Integer> costBeforeTax = Arrays.asList(100, 200, 300);\n" +
			"		   double bill = costBeforeTax.stream().map((cost) -> cost + 0.19 * cost)\n" +
			"		        //                        .y                   .n             .y\n" +
			"		      .reduce((sum, cost) -> sum.doubleValue() + cost.doubleValue()).g\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "g";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<*>;, null, null, getClass, null, [391, 392], " + (R_DEFAULT + 30) + "}\n" +
			"get[METHOD_REF]{get(), Ljava.util.Optional<Ljava.lang.Double;>;, ()Ljava.lang.Double;, null, null, get, null, [391, 392], " + (R_DEFAULT + 50) + "}", requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=435219, [1.8][content assist] No proposals for some closure cases
public void test435219g() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"import java.util.Arrays;\n" +
			"import java.util.List;\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		List<Integer> costBeforeTax = Arrays.asList(100, 200, 300);\n" +
			"		   double bill = costBeforeTax.stream().map((cost) -> cost + 0.19 * cost)\n" +
			"		        //                        .y                   .n             .y\n" +
			"		      .reduce((sum, cost) -> sum.doubleValue() + cost.dou\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "dou";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("doubleToLongBits[METHOD_REF]{doubleToLongBits(), Ljava.lang.Double;, (D)J, null, null, doubleToLongBits, (arg0), [376, 379], " + (R_DEFAULT + 49) + "}\n" +
				  "doubleToRawLongBits[METHOD_REF]{doubleToRawLongBits(), Ljava.lang.Double;, (D)J, null, null, doubleToRawLongBits, (arg0), [376, 379], " + (R_DEFAULT + 49) + "}\n" +
				  "doubleValue[METHOD_REF]{doubleValue(), Ljava.lang.Double;, ()D, null, null, doubleValue, null, [376, 379], " + (R_DEFAULT + 60) + "}", requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=435682, [1.8] content assist not working inside lambda expression
public void test435682() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"import java.util.Arrays;\n" +
			"import java.util.List;\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		List<String> words = Arrays.asList(\"hi\", \"hello\", \"hola\", \"bye\", \"goodbye\");\n" +
			"		List<String> list1 = words.stream().map(so -> so.tr).collect(Collectors.toList());\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "so.tr";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("trim[METHOD_REF]{trim(), Ljava.lang.String;, ()Ljava.lang.String;, null, null, trim, null, [237, 239], " + (R_DEFAULT + R_PACKAGE_EXPECTED_TYPE + 30) + "}", requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=435682, [1.8] content assist not working inside lambda expression
public void test435682a() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"import java.util.Arrays;\n" +
			"import java.util.List;\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		List<String> words = Arrays.asList(\"hi\", \"hello\", \"hola\", \"bye\", \"goodbye\");\n" +
			"		List<String> list1 = words.stream().map((String so) -> so.tr).collect(Collectors.toList());\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "so.tr";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("trim[METHOD_REF]{trim(), Ljava.lang.String;, ()Ljava.lang.String;, null, null, trim, null, [246, 248], " + (R_DEFAULT + R_EXPECTED_TYPE + 30) + "}", requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430667, [1.8][content assist] no proposals around lambda as a field
public void test430667() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"interface D_FI {\n" +
			"	void print(String value, int n);\n" +
			"}\n" +
			"class D_DemoRefactorings {\n" +
			"	\n" +
			"	D_FI fi1= (String value, int n) -> {\n" +
			"		for (int j = 0; j < n; j++) {\n" +
			"			System.out.println(value); 			\n" +
			"		}\n" +
			"	};\n" +
			"	D_F\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "D_F";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("D_F[POTENTIAL_METHOD_DECLARATION]{D_F, LD_DemoRefactorings;, ()V, null, null, D_F, null, [195, 198], " + (R_DEFAULT + 9) + "}\n" +
				  "D_FI[TYPE_REF]{D_FI, , LD_FI;, null, null, null, null, [195, 198], " + (R_DEFAULT + 22) + "}", requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430667, [1.8][content assist] no proposals around lambda as a field
public void test430667a() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"class D_DemoRefactorings {\n" +
			"	\n" +
			"	D_FI fi1= (String value, int n) -> {\n" +
			"		for (int j = 0; j < n; j++) {\n" +
			"			System.out.println(value); 			\n" +
			"		}\n" +
			"	};\n" +
			"	/*HERE*/D_F\n" +
			"}\n" +
			"interface D_FI {\n" +
			"	void print(String value, int n);\n" +
			"}\n"
			);

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "/*HERE*/D_F";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("D_F[POTENTIAL_METHOD_DECLARATION]{D_F, LD_DemoRefactorings;, ()V, null, null, D_F, null, [150, 153], " + (R_DEFAULT + 9) + "}\n" +
			"D_FI[TYPE_REF]{D_FI, , LD_FI;, null, null, null, null, [150, 153], " + (R_DEFAULT + 22) + "}", requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430667, [1.8][content assist] no proposals around lambda as a field
public void test430667b() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"class D_DemoRefactorings {\n" +
			"	/*HERE*/D_F\n" +
			"	D_FI fi1= (String value, int n) -> {\n" +
			"		for (int j = 0; j < n; j++) {\n" +
			"			System.out.println(value); 			\n" +
			"		}\n" +
			"	};\n" +
			"}\n" +
			"interface D_FI {\n" +
			"	void print(String value, int n);\n" +
			"}\n"
			);

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "/*HERE*/D_F";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("D_F[POTENTIAL_METHOD_DECLARATION]{D_F, LD_DemoRefactorings;, ()V, null, null, D_F, null, [36, 39], " + (R_DEFAULT + 9) + "}\n" +
			"D_FI[TYPE_REF]{D_FI, , LD_FI;, null, null, null, null, [36, 39], " + (R_DEFAULT + 22) + "}", requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=443932, [1.8][code complete] method reference proposals not applied when caret inside method name
public void test443932() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"import java.util.function.IntFunction;\n" +
			"public class X {\n" +
			"	IntFunction<String> ts= Integer::toString;\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "to";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("toBinaryString[METHOD_NAME_REFERENCE]{toBinaryString, Ljava.lang.Integer;, (I)Ljava.lang.String;, null, null, toBinaryString, null, [90, 98], " + (R_DEFAULT + 19) + "}\n" +
			"toHexString[METHOD_NAME_REFERENCE]{toHexString, Ljava.lang.Integer;, (I)Ljava.lang.String;, null, null, toHexString, null, [90, 98], " + (R_DEFAULT + 19) + "}\n" +
			"toOctalString[METHOD_NAME_REFERENCE]{toOctalString, Ljava.lang.Integer;, (I)Ljava.lang.String;, null, null, toOctalString, null, [90, 98], " + (R_DEFAULT + 19) + "}\n" +
			"toString[METHOD_NAME_REFERENCE]{toString, Ljava.lang.Integer;, (I)Ljava.lang.String;, null, null, toString, null, [90, 98], " + (R_DEFAULT + 19) + "}\n" +
			"toString[METHOD_NAME_REFERENCE]{toString, Ljava.lang.Integer;, (II)Ljava.lang.String;, null, null, toString, null, [90, 98], " + (R_DEFAULT + 19) + "}\n" +
			"toUnsignedLong[METHOD_NAME_REFERENCE]{toUnsignedLong, Ljava.lang.Integer;, (I)J, null, null, toUnsignedLong, null, [90, 98], " + (R_DEFAULT + 19) + "}\n" +
			"toUnsignedString[METHOD_NAME_REFERENCE]{toUnsignedString, Ljava.lang.Integer;, (I)Ljava.lang.String;, null, null, toUnsignedString, null, [90, 98], " + (R_DEFAULT + 19) + "}\n" +
			"toUnsignedString[METHOD_NAME_REFERENCE]{toUnsignedString, Ljava.lang.Integer;, (II)Ljava.lang.String;, null, null, toUnsignedString, null, [90, 98], " + (R_DEFAULT + 19) + "}\n" +
			"toString[METHOD_NAME_REFERENCE]{toString, Ljava.lang.Integer;, ()Ljava.lang.String;, null, null, toString, null, [90, 98], " + (R_DEFAULT + 30) + "}", requestor.getResults());
	assertTrue(str.substring(90, 98).equals("toString"));

}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=444300, [1.8] content assist not working inside lambda expression in case of fields
public void test444300() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"import java.util.Arrays;\n" +
			"import java.util.List;\n" +
			"import java.util.stream.Collectors;\n" +
			"public class Test {\n" +
			"	List<String> words = Arrays.asList(\"hi\", \"hello\", \"hola\", \"bye\", \"goodbye\");\n" +
			"	List<String> list1 = words.stream().map(so -> so.ch).collect(Collectors.toList());\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "so.ch";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("charAt[METHOD_REF]{charAt(), Ljava.lang.String;, (I)C, null, null, charAt, (arg0), [232, 234], " + (R_DEFAULT + R_EXPECTED_TYPE + 30) + "}\n" +
			"chars[METHOD_REF]{chars(), Ljava.lang.CharSequence;, ()Ljava.util.stream.IntStream;, null, null, chars, null, [232, 234], " + (R_DEFAULT + R_EXPECTED_TYPE + 30) + "}", requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=435219, [1.8][content assist] No proposals for some closure cases
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=444300, [1.8] content assist not working inside lambda expression in case of fields
public void test435219h() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"import java.util.Arrays;\n" +
			"import java.util.List;\n" +
			"public class X {\n" +
			"	List<Integer> list = Arrays.asList(1, 2, 3);\n" +
			"	List<String> list1 = list.stream().map((x) -> x * x.h).collect(Collectors.toList());\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "x.h";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("hashCode[METHOD_REF]{hashCode(), Ljava.lang.Integer;, (I)I, null, null, hashCode, (arg0), [164, 165], " + (R_DEFAULT + 49) + "}\n" +
			"highestOneBit[METHOD_REF]{highestOneBit(), Ljava.lang.Integer;, (I)I, null, null, highestOneBit, (arg0), [164, 165], " + (R_DEFAULT + 49) + "}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.lang.Integer;, ()I, null, null, hashCode, null, [164, 165], " + (R_DEFAULT + 60) + "}", requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=435219, [1.8][content assist] No proposals for some closure cases
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=444300, [1.8] content assist not working inside lambda expression in case of fields
public void test435219i() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"import java.util.Arrays;\n" +
			"import java.util.List;\n" +
			"public class X {\n" +
			"		List<Integer> list = Arrays.asList(1, 2, 3);\n" +
			"		Object o = list.stream().map((x) -> x * x.hashCode()).forEach(System.out::pri);\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "pri";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("print[METHOD_NAME_REFERENCE]{print, Ljava.io.PrintStream;, (C)V, null, null, print, null, [188, 191], " + (R_DEFAULT + 25) + "}\n" +
			"print[METHOD_NAME_REFERENCE]{print, Ljava.io.PrintStream;, (D)V, null, null, print, null, [188, 191], " + (R_DEFAULT + 25) + "}\n" +
			"print[METHOD_NAME_REFERENCE]{print, Ljava.io.PrintStream;, (F)V, null, null, print, null, [188, 191], " + (R_DEFAULT + 25) + "}\n" +
			"print[METHOD_NAME_REFERENCE]{print, Ljava.io.PrintStream;, (I)V, null, null, print, null, [188, 191], " + (R_DEFAULT + 25) + "}\n" +
			"print[METHOD_NAME_REFERENCE]{print, Ljava.io.PrintStream;, (J)V, null, null, print, null, [188, 191], " + (R_DEFAULT + 25) + "}\n" +
			"print[METHOD_NAME_REFERENCE]{print, Ljava.io.PrintStream;, (Ljava.lang.Object;)V, null, null, print, null, [188, 191], " + (R_DEFAULT + 25) + "}\n" +
			"print[METHOD_NAME_REFERENCE]{print, Ljava.io.PrintStream;, (Ljava.lang.String;)V, null, null, print, null, [188, 191], " + (R_DEFAULT + 25) + "}\n" +
			"print[METHOD_NAME_REFERENCE]{print, Ljava.io.PrintStream;, (Z)V, null, null, print, null, [188, 191], " + (R_DEFAULT + 25) + "}\n" +
			"print[METHOD_NAME_REFERENCE]{print, Ljava.io.PrintStream;, ([C)V, null, null, print, null, [188, 191], " + (R_DEFAULT + 25) + "}\n" +
			"println[METHOD_NAME_REFERENCE]{println, Ljava.io.PrintStream;, ()V, null, null, println, null, [188, 191], " + (R_DEFAULT + 25) + "}\n" +
			"println[METHOD_NAME_REFERENCE]{println, Ljava.io.PrintStream;, (C)V, null, null, println, null, [188, 191], " + (R_DEFAULT + 25) + "}\n" +
			"println[METHOD_NAME_REFERENCE]{println, Ljava.io.PrintStream;, (D)V, null, null, println, null, [188, 191], " + (R_DEFAULT + 25) + "}\n" +
			"println[METHOD_NAME_REFERENCE]{println, Ljava.io.PrintStream;, (F)V, null, null, println, null, [188, 191], " + (R_DEFAULT + 25) + "}\n" +
			"println[METHOD_NAME_REFERENCE]{println, Ljava.io.PrintStream;, (I)V, null, null, println, null, [188, 191], " + (R_DEFAULT + 25) + "}\n" +
			"println[METHOD_NAME_REFERENCE]{println, Ljava.io.PrintStream;, (J)V, null, null, println, null, [188, 191], " + (R_DEFAULT + 25) + "}\n" +
			"println[METHOD_NAME_REFERENCE]{println, Ljava.io.PrintStream;, (Ljava.lang.Object;)V, null, null, println, null, [188, 191], " + (R_DEFAULT + 25) + "}\n" +
			"println[METHOD_NAME_REFERENCE]{println, Ljava.io.PrintStream;, (Ljava.lang.String;)V, null, null, println, null, [188, 191], " + (R_DEFAULT + 25) + "}\n" +
			"println[METHOD_NAME_REFERENCE]{println, Ljava.io.PrintStream;, (Z)V, null, null, println, null, [188, 191], " + (R_DEFAULT + 25) + "}\n" +
			"println[METHOD_NAME_REFERENCE]{println, Ljava.io.PrintStream;, ([C)V, null, null, println, null, [188, 191], " + (R_DEFAULT + 25) + "}\n" +
			"printf[METHOD_NAME_REFERENCE]{printf, Ljava.io.PrintStream;, (Ljava.lang.String;[Ljava.lang.Object;)Ljava.io.PrintStream;, null, null, printf, null, [188, 191], " + (R_DEFAULT + 30) + "}\n" +
			"printf[METHOD_NAME_REFERENCE]{printf, Ljava.io.PrintStream;, (Ljava.util.Locale;Ljava.lang.String;[Ljava.lang.Object;)Ljava.io.PrintStream;, null, null, printf, null, [188, 191], " + (R_DEFAULT + 30) + "}", requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=435219, [1.8][content assist] No proposals for some closure cases
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=444300, [1.8] content assist not working inside lambda expression in case of fields
public void test435219j() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"import java.util.Arrays;\n" +
			"import java.util.List;\n" +
			"public class X {\n" +
			"		List<Integer> costBeforeTax = Arrays.asList(100, 200, 300);\n" +
			"		double bill = costBeforeTax.stream().map((cost) -> cost + 0.19 * cost)\n" +
			"		      .reduce((sum, cost) -> sum.dou\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "dou";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("doubleToLongBits[METHOD_REF]{doubleToLongBits(), Ljava.lang.Double;, (D)J, null, null, doubleToLongBits, (arg0), [235, 238], " + (R_DEFAULT + 19) + "}\n" +
			"doubleToRawLongBits[METHOD_REF]{doubleToRawLongBits(), Ljava.lang.Double;, (D)J, null, null, doubleToRawLongBits, (arg0), [235, 238], " + (R_DEFAULT + 19) + "}\n" +
			"doubleValue[METHOD_REF]{doubleValue(), Ljava.lang.Double;, ()D, null, null, doubleValue, null, [235, 238], " + (R_DEFAULT + R_EXPECTED_TYPE + 30) + "}", requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=435219, [1.8][content assist] No proposals for some closure cases
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=444300, [1.8] content assist not working inside lambda expression in case of fields
public void test435219k() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"import java.util.Arrays;\n" +
			"import java.util.List;\n" +
			"public class X {\n" +
			"		List<Integer> costBeforeTax = Arrays.asList(100, 200, 300);\n" +
			"		double bill = costBeforeTax.stream().map((cost) -> cost + 0.19 * cost)\n" +
			"		      .reduce((sum, cost) -> sum.doubleValue() + cost.doubleValue()).g\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "g";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<*>;, null, null, getClass, null, [271, 272], " + (R_DEFAULT + 30) + "}\n" +
			"get[METHOD_REF]{get(), Ljava.util.Optional<Ljava.lang.Double;>;, ()Ljava.lang.Double;, null, null, get, null, [271, 272], " + (R_DEFAULT + 50) + "}", requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=435219, [1.8][content assist] No proposals for some closure cases
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=444300, [1.8] content assist not working inside lambda expression in case of fields
public void test435219l() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"import java.util.Arrays;\n" +
			"import java.util.List;\n" +
			"public class X {\n" +
			"		List<Integer> costBeforeTax = Arrays.asList(100, 200, 300);\n" +
			"		double bill = costBeforeTax.stream().map((cost) -> cost + 0.19 * cost)\n" +
			"		      .reduce((sum, cost) -> sum.doubleValue() + cost.dou\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "dou";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("doubleToLongBits[METHOD_REF]{doubleToLongBits(), Ljava.lang.Double;, (D)J, null, null, doubleToLongBits, (arg0), [256, 259], " + (R_DEFAULT + 49) + "}\n" +
			"doubleToRawLongBits[METHOD_REF]{doubleToRawLongBits(), Ljava.lang.Double;, (D)J, null, null, doubleToRawLongBits, (arg0), [256, 259], " + (R_DEFAULT + 49) + "}\n" +
			"doubleValue[METHOD_REF]{doubleValue(), Ljava.lang.Double;, ()D, null, null, doubleValue, null, [256, 259], " + (R_DEFAULT + 60) + "}", requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=435281, [1.8][code assist] No import or completion proposal for anonymous class inside lambda
public void test435281() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/FI1.java",
			"package p4a;\n" +
			"@FunctionalInterface\n" +
			"public interface FI1<R> {\n" +
			"    public R foo1();\n" +
			"}\n");
	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src/FI2.java",
			"package p4a;\n" +
			"@FunctionalInterface\n" +
			"public interface FI2 {\n" +
			"    public void foo2();\n" +
			"}\n");

	this.workingCopies[2] = getWorkingCopy(
			"/Completion/src/Test.java",
			"package p4b;\n" +
			"import p4a.FI1;\n" +
			"public class Test {\n" +
			"	{\n" +
			"                new FI2() {};\n" +
			"		FI1 fi1 = () -> new FI2() {\n" +
			"		    @Override\n" +
			"		    public void foo2() {}\n" +
			"		};\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[2].getSource();
	String completeBehind = "FI2";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[2].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("FI2[TYPE_REF]{p4a.FI2, p4a, Lp4a.FI2;, null, null, null, null, [104, 107], " + (R_DEFAULT + 23) + "}", requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=431811, content assist should propose keyword 'super' after type name
public void test431811() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/FI1.java",
			"interface Intf {\n" +
			"	void foo();\n" +
			"}\n" +
			"public class X implements Intf {\n" +
			"    class Inner {\n" +
			"        {\n" +
			"            X.super.hashCode();\n" +
			"        }\n" +
			"    }\n" +
			"    @Override\n" +
			"    public void foo() {\n" +
			"        Intf.su;\n" +
			"    }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "su";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("super[KEYWORD]{super, null, null, null, null, super, null, [192, 194], " + (R_DEFAULT + 21) + "}", requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=447774, Auto complete does not work when using lambdas with cast
public void test447774() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"import java.io.Serializable;\n" +
			"import java.util.function.Function;\n" +
			"import java.util.function.Predicate;\n" +
			"public final class X {\n" +
			"    public static <T, R> Predicate<T> apply(Predicate<R> predicate, Function<? super T, ? extends R> function) {\n" +
			"	     syso\n" +
			"        return (Predicate<T> & Serializable) t -> predicate.test(function.apply(t));\n" +
			"    }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "syso";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("", requestor.getResults());
	assertEquals("completion offset=248\n" +
			"completion range=[244, 247]\n" +
			"completion token=\"syso\"\n" +
			"completion token kind=TOKEN_KIND_NAME\n" +
			"expectedTypesSignatures=null\n" +
			"expectedTypesKeys=null\n" +
			"completion token location={STATEMENT_START}", requestor.getContext());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=449358, Content assist inside lambda broken in all methods except last
public void test449358() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"import java.util.Optional;\n" +
			"public class LambdaBug {\n" +
			"	private final String field = \"final field\";\n" +
			"	void localmethod1() {\n" +
			"		Optional.of(\"test\").map(s -> {\n" +
			"			String local;\n" +
			"			/*HERE*/localMeth\n" +
			"			return s;\n" +
			"		}).get();\n" +
			"	}\n" +
			"	void localmethod2() {\n" +
			"		Optional.of(\"test\").map(s -> {\n" +
			"			String local;\n" +
			"			// content assist works there\n" +
			"			return s;\n" +
			"		}).get();\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "/*HERE*/localMeth";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("localmethod1[METHOD_REF]{localmethod1(), LLambdaBug;, ()V, null, null, localmethod1, null, [181, 190], " + (R_DEFAULT + 12) + "}\n" +
                  "localmethod2[METHOD_REF]{localmethod2(), LLambdaBug;, ()V, null, null, localmethod2, null, [181, 190], " + (R_DEFAULT + 12) + "}", requestor.getResults());

}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=449358, Content assist inside lambda broken in all methods except last
public void test449358a() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"import java.util.Optional;\n" +
			"public class LambdaBug {\n" +
			"	private final String field = \"final field\";\n" +
			"	void localmethod1() {\n" +
			"		Optional.of(\"test\").map(s -> {\n" +
			"			String local;\n" +
			"			return s;\n" +
			"		}).get();\n" +
			"	}\n" +
			"	void localmethod2() {\n" +
			"		Optional.of(\"test\").map(s -> {\n" +
			"			String local;\n" +
			"			/*HERE*/localMeth\n" +
			"			return s;\n" +
			"		}).get();\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "/*HERE*/localMeth";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("localmethod1[METHOD_REF]{localmethod1(), LLambdaBug;, ()V, null, null, localmethod1, null, [282, 291], " + (R_DEFAULT + 12) + "}\n" +
				  "localmethod2[METHOD_REF]{localmethod2(), LLambdaBug;, ()V, null, null, localmethod2, null, [282, 291], " + (R_DEFAULT + 12) + "}", requestor.getResults());

}
public void testBug459189_001() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"public class X {\n"+
			"	Integer foo(){\n"+
			"		I <Integer, X> i2 = (x) -> {ret /* type ctrl-space after ret */};\n"+
			"		return 0;\n"+
			"	}\n"+
			"	Integer bar(Integer x) { return null;}\n"+
			"}\n"+
			"interface I <T,R> {\n"+
			"	R apply(T t);\n"+
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "ret";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults(
			"Retention[TYPE_REF]{java.lang.annotation.Retention, java.lang.annotation, Ljava.lang.annotation.Retention;, null, null, " + (R_DEFAULT + 9) + "}\n"+
			"RetentionPolicy[TYPE_REF]{java.lang.annotation.RetentionPolicy, java.lang.annotation, Ljava.lang.annotation.RetentionPolicy;, null, null, " + (R_DEFAULT + 9) + "}\n"+
			"return[KEYWORD]{return, null, null, return, null, " + (R_DEFAULT + 19) + "}",
			requestor.getResults());
}
public void testBug459189_002() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"	Integer bar(Integer x) { return null;}\n"+
			"public class X {\n"+
			"	Integer foo(){\n"+
			"		I <Integer, X> i2 = (x) -> {/* HERE */ret /* type ctrl-space after ret */};\n"+
			"		return 0;\n"+
			"	}\n"+
			"}\n"+
			"interface I <T,R> {\n"+
			"	R apply(T t);\n"+
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "/* HERE */ret";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults(
			"Retention[TYPE_REF]{java.lang.annotation.Retention, java.lang.annotation, Ljava.lang.annotation.Retention;, null, null, " + (R_DEFAULT + 9) + "}\n"+
			"RetentionPolicy[TYPE_REF]{java.lang.annotation.RetentionPolicy, java.lang.annotation, Ljava.lang.annotation.RetentionPolicy;, null, null, " + (R_DEFAULT + 9) + "}\n" +
			"return[KEYWORD]{return, null, null, return, null, " + (R_DEFAULT + 19) + "}",
			requestor.getResults());
}
public void testBug459189_003() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"public class X {\n"+
			"	Integer foo(){\n"+
			"		I <Integer, X> i2 = (x) -> {try{} /* HERE */\n"+
			"		return 0;\n"+
			"	}\n"+
			"	Integer bar(Integer x) { return null;}\n"+
			"}\n"+
			"interface I <T,R> {\n"+
			"	R apply(T t);\n"+
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "/* HERE */";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults(
			"catch[KEYWORD]{catch, null, null, catch, null, " + (R_DEFAULT + 19) + "}\n"+
			"finally[KEYWORD]{finally, null, null, finally, null, " + (R_DEFAULT + 19) + "}",
			requestor.getResults());
}
public void testBug459189_004() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"public class X {\n"+
			"	Integer foo(){\n"+
			"		I <Integer, X> i2 = (x) -> {do{} /* HERE */\n"+
			"		return 0;\n"+
			"	}\n"+
			"	Integer bar(Integer x) { return null;}\n"+
			"}\n"+
			"interface I <T,R> {\n"+
			"	R apply(T t);\n"+
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "/* HERE */";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults(
			"while[KEYWORD]{while, null, null, while, null, " + (R_DEFAULT + 19) + "}",
			requestor.getResults());
}
public void testBug460410() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"import java.util.ArrayList;\n" +
			"import java.util.function.Supplier;\n" +
			"public class X {\n"+
			"	public static void main(String[] args) {\n"+
			"		ArrayList<Supplier<Runnable>> list = new ArrayList<>();\n"+
			"		list.forEach((supp) -> {\n"+
			"			Supplier<Bug460/* HERE */>}\n"+
			"		});\n"+
			"	}\n"+
			"	public static class Bug460410 {" +
			"	}" +
			"}\n");
	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src/Bug460411.java",
			"package abc;" +
			"public class Bug460411 {\n"+
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "/* HERE */";
	int cursorLocation = str.indexOf(completeBehind);
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults(
			"Bug460411[TYPE_REF]{abc.Bug460411, abc, Labc.Bug460411;, null, null, " + (R_DEFAULT + 39) + "}\n" +
			"Bug460410[TYPE_REF]{Bug460410, , LBug460410;, null, null, " + (R_DEFAULT + 42) + "}",
			requestor.getResults());
}
public void testBug462015() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"package abc;\n" +
			"import java.util.ArrayList;\n" +
			"import java.util.stream.Collectors;\n" +
			"public class X {\n"+
			"	public static void main(String[] args) {\n"+
			"		ArrayList<Entry> list = new ArrayList<>();\n"+
			"		list.stream().collect(Collectors.averagingInt(e -> e.a/* HERE */));\n"+
			"	}\n"+
			"}\n");
	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src/Entry.java",
			"package abc;" +
			"public class Entry {\n"+
			"	public String age() {\n"+
			"		return \"10\";"+
			"	}"+
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "/* HERE */";
	int cursorLocation = str.indexOf(completeBehind);
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults(
			"age[METHOD_REF]{age(), LEntry;, ()Ljava.lang.String;, age, null, " + (R_DEFAULT + 30) + "}",
			requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=481564
public void testBug481564() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"import java.util.function.Consumer;\n" +
			"public class X {\n" +
			"	public void foo() {\n" +
			"		new Thread(() -> {\n" +
			"			som/*here*/\n" +
			"		});\n" +
			"	}\n" +
			"	public void poisonMethod() {\n" +
			"		ArrayList<String> views = new ArrayList<>();\n" +
			"		views.stream().filter(String::isEmpty).forEach(s -> s.length());\n" +
			"	}\n" +
			"	public void someMethod() {}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "/*here*/";
	int cursorLocation = str.indexOf(completeBehind) ;
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults(
			"someMethod[METHOD_REF]{someMethod(), LX;, ()V, someMethod, null, " + (R_DEFAULT + 22) + "}", requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=481215
public void testBug481215a() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"import java.util.function.Consumer;\n" +
			"public class X {\n" +
			"	public static void main() {\n" +
			"		MyGeneric<String> mystring = new MyGeneric<>(\"\");\n" +
			"		complete((String result) -> {\n" +
			"			mystring.get(res/* HERE */);\n" +
			"		}, new Consumer<Throwable>() {\n" +
			"			@Override\n" +
			"			public void accept(Throwable t) { t.printStackTrace(); }\n" +
			"		});\n" +
			"	}\n" +
			"	public static class MyGeneric<T> {\n" +
			"		public MyGeneric(T t) {}\n" +
			"		public T get(String value) { return null; }\n" +
			"	}\n" +
			"	static void complete(Consumer<String> success, Consumer<Throwable> failure) {}\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "/* HERE */";
	int cursorLocation = str.indexOf(completeBehind) ;
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults(
			"ResourceBundle[TYPE_REF]{java.util.ResourceBundle, java.util, Ljava.util.ResourceBundle;, null, null, " + (R_DEFAULT + 9) + "}\n" +
			"ResponseCache[TYPE_REF]{java.net.ResponseCache, java.net, Ljava.net.ResponseCache;, null, null, " + (R_DEFAULT + 9) + "}\n" +
			"ResultSet[TYPE_REF]{java.sql.ResultSet, java.sql, Ljava.sql.ResultSet;, null, null, " + (R_DEFAULT + 9) + "}\n" +
			"ResultSetMetaData[TYPE_REF]{java.sql.ResultSetMetaData, java.sql, Ljava.sql.ResultSetMetaData;, null, null, " + (R_DEFAULT + 9) + "}\n" +
			"result[LOCAL_VARIABLE_REF]{result, null, Ljava.lang.String;, result, null, " + (R_DEFAULT + R_EXACT_EXPECTED_TYPE + 22) + "}", requestor.getResults());
}
public void testBug481215b() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"import java.util.function.Consumer;\n" +
			"public class X {\n" +
			"	public static void main() {\n" +
			"		MyGeneric<String> mystring = new MyGeneric<>(\"\");\n" +
			"		complete((String result) -> {\n" +
			"			mystring.get(res/* HERE */);\n" +
			"		}, t -> t.printStackTrace());\n" +
			"	}\n" +
			"	public static class MyGeneric<T> {\n" +
			"		public MyGeneric(T t) {}\n" +
			"		public T get(String value) { return null; }\n" +
			"	}\n" +
			"	static void complete(Consumer<String> success, Consumer<Throwable> failure) {}\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "/* HERE */";
	int cursorLocation = str.indexOf(completeBehind) ;
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults(
			"ResourceBundle[TYPE_REF]{java.util.ResourceBundle, java.util, Ljava.util.ResourceBundle;, null, null, " + (R_DEFAULT + 9) + "}\n" +
			"ResponseCache[TYPE_REF]{java.net.ResponseCache, java.net, Ljava.net.ResponseCache;, null, null, " + (R_DEFAULT + 9) + "}\n" +
			"ResultSet[TYPE_REF]{java.sql.ResultSet, java.sql, Ljava.sql.ResultSet;, null, null, " + (R_DEFAULT + 9) + "}\n" +
			"ResultSetMetaData[TYPE_REF]{java.sql.ResultSetMetaData, java.sql, Ljava.sql.ResultSetMetaData;, null, null, " + (R_DEFAULT + 9) + "}\n" +
			"result[LOCAL_VARIABLE_REF]{result, null, Ljava.lang.String;, result, null, " + (R_DEFAULT + R_EXACT_EXPECTED_TYPE + 22) + "}", requestor.getResults());
}
public void testBug481215c() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"import java.util.function.Consumer;\n" +
			"public class X {\n" +
			"	public static void main() {\n" +
			"		MyGeneric<String> mystring = new MyGeneric<>(\"\");\n" +
			"		complete((String result) -> {\n" +
			"			mystring.get(res/* HERE */);\n" +
			"		}, t -> {t.printStackTrace();});\n" +
			"	}\n" +
			"	public static class MyGeneric<T> {\n" +
			"		public MyGeneric(T t) {}\n" +
			"		public T get(String value) { return null; }\n" +
			"	}\n" +
			"	static void complete(Consumer<String> success, Consumer<Throwable> failure) {}\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "/* HERE */";
	int cursorLocation = str.indexOf(completeBehind) ;
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults(
			"ResourceBundle[TYPE_REF]{java.util.ResourceBundle, java.util, Ljava.util.ResourceBundle;, null, null, " + (R_DEFAULT + 9) + "}\n" +
			"ResponseCache[TYPE_REF]{java.net.ResponseCache, java.net, Ljava.net.ResponseCache;, null, null, " + (R_DEFAULT + 9) + "}\n" +
			"ResultSet[TYPE_REF]{java.sql.ResultSet, java.sql, Ljava.sql.ResultSet;, null, null, " + (R_DEFAULT + 9) + "}\n" +
			"ResultSetMetaData[TYPE_REF]{java.sql.ResultSetMetaData, java.sql, Ljava.sql.ResultSetMetaData;, null, null, " + (R_DEFAULT + 9) + "}\n" +
			"result[LOCAL_VARIABLE_REF]{result, null, Ljava.lang.String;, result, null, " + (R_DEFAULT + R_EXACT_EXPECTED_TYPE + 22) + "}", requestor.getResults());
}
public void testBug481215d() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"import java.util.function.Consumer;\n" +
			"public class X {\n" +
			"	public static void main() {\n" +
			"		MyGeneric<String> mystring = new MyGeneric<>(\"\");\n" +
			"		complete((String result) -> {\n" +
			"			mystring.get(result);\n" +
			"			Consumer<String> success = (String result2) -> {\n" +
			"				mystring.get(res/* HERE */);\n" +
			"				};\n" +
			"		}, new Consumer<Throwable>() {\n" +
			"			@Override\n" +
			"			public void accept(Throwable t) {\n" +
			"				t.printStackTrace();\n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"	public static class MyGeneric<T> {\n" +
			"		public MyGeneric(T t) {}\n" +
			"		public T get(String value) { return null; }\n" +
			"	}\n" +
			"	static void complete(Consumer<String> success, Consumer<Throwable> failure) {}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "/* HERE */";
	int cursorLocation = str.indexOf(completeBehind) ;
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults(
			"ResourceBundle[TYPE_REF]{java.util.ResourceBundle, java.util, Ljava.util.ResourceBundle;, null, null, " + (R_DEFAULT + 9) + "}\n" +
			"ResponseCache[TYPE_REF]{java.net.ResponseCache, java.net, Ljava.net.ResponseCache;, null, null, " + (R_DEFAULT + 9) + "}\n" +
			"ResultSet[TYPE_REF]{java.sql.ResultSet, java.sql, Ljava.sql.ResultSet;, null, null, " + (R_DEFAULT + 9) + "}\n" +
			"ResultSetMetaData[TYPE_REF]{java.sql.ResultSetMetaData, java.sql, Ljava.sql.ResultSetMetaData;, null, null, " + (R_DEFAULT + 9) + "}\n" +
			"result[LOCAL_VARIABLE_REF]{result, null, Ljava.lang.String;, result, null, " + (R_DEFAULT + R_EXACT_EXPECTED_TYPE + 22) + "}\n" +
			"result2[LOCAL_VARIABLE_REF]{result2, null, Ljava.lang.String;, result2, null, " + (R_DEFAULT + R_EXACT_EXPECTED_TYPE + 22) + "}", requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=484479
public void test484479() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/Bar.java",
			"interface Supplier<T> {\n" +
			"   T get();\n" +
			"}\n" +
			"public interface Bar {\n" +
			"    static public Bar print() {\n" +
			"        return null;\n" +
			"    }\n" +
			"}\n" +
			"class A {\n" +
			"    	Supplier<Bar> c = Bar::pr\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "::pr";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("print[METHOD_NAME_REFERENCE]{print, LBar;, ()LBar;, null, null, print, null, [160, 162], " +
											(RelevanceConstants.R_DEFAULT + RelevanceConstants.R_RESOLVED +
													RelevanceConstants.R_INTERESTING + RelevanceConstants.R_NON_RESTRICTED +
													RelevanceConstants.R_CASE) + "}", requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=473008
public void test473008a() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/Foo.java",
			"interface FooFunctional {\n" +
			"   void function();\n" +
			"}\n" +
			"public class Foo {\n" +
			"    private FooFunctional lambda = this::bar;\n" +
			"    public void bar() {\n" +
			"      new StringBuffer" +
			"    }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "new StringBuffer";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	int relevance = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_CASE;
	assertResults(
			"StringBufferInputStream[TYPE_REF]{java.io.StringBufferInputStream, java.io, Ljava.io.StringBufferInputStream;, null, null, null, null, [147, 159], " + relevance + "}\n" +
			"StringBuffer[TYPE_REF]{StringBuffer, java.lang, Ljava.lang.StringBuffer;, null, null, null, null, [147, 159], " + (relevance + R_UNQUALIFIED + R_EXACT_NAME) + "}"
			, requestor.getResults());
}
public void test473008b() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/Foo.java",
			"interface FooFunctional {\n" +
			"   void function();\n" +
			"}\n" +
			"public class Foo {\n" +
			"    public void bar() {\n" +
			"      private FooFunctional lambda = this::bar;\n" +
			"      new StringBuffer" +
			"    }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "new StringBuffer";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	int relevance = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_CASE;
	assertResults(
			"StringBufferInputStream[TYPE_REF]{java.io.StringBufferInputStream, java.io, Ljava.io.StringBufferInputStream;, null, null, null, null, [149, 161], " + relevance + "}\n" +
			"StringBuffer[TYPE_REF]{StringBuffer, java.lang, Ljava.lang.StringBuffer;, null, null, null, null, [149, 161], " + (relevance + R_UNQUALIFIED + R_EXACT_NAME) + "}"
			, requestor.getResults());
}
public void test473008c() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/Foo.java",
			"interface FooFunctional {\n" +
			"   void function();\n" +
			"}\n" +
			"public class Foo {\n" +
			"    public void bar() {\n" +
			"      private FooFunctional lambda = () -> bar();\n" +
			"      new StringBuffer" +
			"    }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "new StringBuffer";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	int relevance = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_CASE;
	assertResults(
			"StringBufferInputStream[TYPE_REF]{java.io.StringBufferInputStream, java.io, Ljava.io.StringBufferInputStream;, null, null, null, null, [151, 163], " + relevance + "}\n" +
			"StringBuffer[TYPE_REF]{StringBuffer, java.lang, Ljava.lang.StringBuffer;, null, null, null, null, [151, 163], " + (relevance + R_UNQUALIFIED + R_EXACT_NAME) + "}"
			, requestor.getResults());
}
public void test489962() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/CC.java",
			"public class CC extends S1 {\n" +
			"	private int i = disp\n" +
			"}\n" +
			"abstract class S1 implements I1 {}\n" +
			"interface I1 extends I2 {}\n" +
			"interface I2 {\n" +
			"	default int dispose() {\n" +
			"		return 0;\n" +
			"	}\n" +
			"	default void disperse() {}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "disp";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	int relevance = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED;
	assertResults(
			"disperse[METHOD_REF]{disperse(), LI2;, ()V, null, null, disperse, null, [46, 50], " + (relevance + R_VOID) + "}\n" +
			"dispose[METHOD_REF]{dispose(), LI2;, ()I, null, null, dispose, null, [46, 50], " + (relevance + R_EXACT_EXPECTED_TYPE) + "}"
			, requestor.getResults());
}
public void test492947() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"public class X {\n" +
			"	public interface SomeInterface {\n" +
			"		public void someMethod(String builder);\n" +
			"}\n" +
			"	public enum SomeEnum {\n" +
			"		SOME_ENUM((String bui) -> {\n" +
			"			bui.toCh\n" +
			"		});\n" +
			"		SomeEnum(SomeInterface callable) {}\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "bui.toCh";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	int relevance = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED;
	assertResults(
			"toCharArray[METHOD_REF]{toCharArray(), Ljava.lang.String;, ()[C, null, null, toCharArray, null, [156, 160], " + relevance + "}"
			, requestor.getResults());
}
public void test492947b() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"public class X {\n" +
			"	public interface SomeInterface {\n" +
			"		public void someMethod(StringBuilder builder);\n" +
			"}\n" +
			"	public enum SomeEnum {\n" +
			"		SOME_ENUM((StringBui bui) -> {\n" +
			"		});\n" +
			"		SomeEnum(SomeInterface callable) {}\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "StringBui";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	int relevance = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED;
	assertResults(
			"StringBuilder[TYPE_REF]{StringBuilder, java.lang, Ljava.lang.StringBuilder;, null, null, null, null, [139, 148], " + relevance + "}",
			 requestor.getResults());
}
/**
 * Bug - No proposal yet for types on lambda arguments
 * @throws JavaModelException
 */
public void _test492947c() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"public class X {\n" +
			"	public interface SomeInterface {\n" +
			"		public void someMethod(StringBuilder builder);\n" +
			"}\n" +
			"	public enum SomeEnum {\n" +
			"		SOME_ENUM((StringBui) -> {\n" +
			"		});\n" +
			"		SomeEnum(SomeInterface callable) {}\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "StringBui";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	int relevance = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED;
	assertResults(
			"StringBuilder[TYPE_REF]{StringBuilder, java.lang, Ljava.lang.StringBuilder;, null, null, null, null, [139, 148], " + relevance + "}"
			, requestor.getResults());
}
public void _test492947d() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"public class X {\n" +
			"	public Main(SomeInterface arg) {}\n" +
			"	public interface SomeInterface {\n" +
			"		public void someMethod(StringBuilder builder);\n" +
			"}\n" +
			"	Main m = new Main((StringBui) -> {\n" +
			"		});\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "StringBui";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	int relevance = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED;
	assertResults(
			"StringBuilder[TYPE_REF]{StringBuilder, java.lang, Ljava.lang.StringBuilder;, null, null, null, null, [139, 148], " + relevance + "}"
			, requestor.getResults());
}
public void testBug493705() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/HelloWorld.java",
			"package b493705;\n" +
			"\n" +
			"import java.util.function.BiFunction;\n" +
			"\n" +
			"class Control { }\n" +
			"class Composite extends Control { }\n" +
			"class Label extends Control {\n" +
			"	public Label(Composite p, int i) {}\n" +
			"}\n" +
			"\n" +
			"class Viewer { }\n" +
			"interface ViewerSupplier {\n" +
			"	ViewerUI<? extends Viewer> getViewerUI();\n" +
			"}\n" +
			"class ViewerUI<V extends Viewer> extends SwtUI<Control>{\n" +
			"\n" +
			"}\n" +
			"interface ControlSupplier {\n" +
			"	SwtUI<? extends Control> getControlUI();\n" +
			"}\n" +
			"class SwtUI<T> {\n" +
			"	public SwtUI<T> child(ControlSupplier supplier) {\n" +
			"		return null;\n" +
			"	}\n" +
			"	public SwtUI<T> child(ViewerSupplier supplier) {\n" +
			"		return null;\n" +
			"	}\n" +
			"	public static <T extends Control> SwtUI<T> create(BiFunction<Composite, Integer, T> ctor) {\n" +
			"		return null;\n" +
			"	}\n" +
			"	public SwtUI<T> text(String text) {\n" +
			"		return null;\n" +
			"	}\n" +
			"}\n" +
			"public class HelloWorld {\n" +
			"	void test(SwtUI<Composite> root) {\n" +
			"		root.child(() -> SwtUI.create(Label::new)\n" +
			"				.text(\"Selection\").\n" +
			"				);\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = ".text(\"Selection\").";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	// TODO: compute relevances
	int relevance1 =  R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED;
	int relevance2a = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_NON_STATIC + R_VOID;
	int relevance2 =  R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_NON_STATIC;
	assertResults(
			"create[METHOD_REF]{create(), LSwtUI<LLabel;>;, <T:LControl;>(Ljava.util.function.BiFunction<LComposite;Ljava.lang.Integer;TT;>;)LSwtUI<TT;>;, null, null, create, (ctor), [853, 853], "+relevance1+"}\n" +
			"new[KEYWORD]{new, null, null, null, null, new, null, [853, 853], "+relevance1+"}\n" +
			"finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, null, null, finalize, null, [853, 853], "+relevance2a+"}\n" +
			"notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, null, null, notify, null, [853, 853], "+relevance2a+"}\n" +
			"notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, null, null, notifyAll, null, [853, 853], "+relevance2a+"}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, null, null, wait, null, [853, 853], "+relevance2a+"}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, null, null, wait, (millis), [853, 853], "+relevance2a+"}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, null, null, wait, (millis, nanos), [853, 853], "+relevance2a+"}\n" +
			"child[METHOD_REF]{child(), LSwtUI<LLabel;>;, (LControlSupplier;)LSwtUI<LLabel;>;, null, null, child, (supplier), [853, 853], "+relevance2+"}\n" +
			"child[METHOD_REF]{child(), LSwtUI<LLabel;>;, (LViewerSupplier;)LSwtUI<LLabel;>;, null, null, child, (supplier), [853, 853], "+relevance2+"}\n" +
			"clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, null, null, clone, null, [853, 853], "+relevance2+"}\n" +
			"equals[METHOD_REF]{equals(), Ljava.lang.Object;, (Ljava.lang.Object;)Z, null, null, equals, (obj), [853, 853], "+relevance2+"}\n" +
			"getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<*>;, null, null, getClass, null, [853, 853], "+relevance2+"}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, null, null, hashCode, null, [853, 853], "+relevance2+"}\n" +
			"text[METHOD_REF]{text(), LSwtUI<LLabel;>;, (Ljava.lang.String;)LSwtUI<LLabel;>;, null, null, text, (text), [853, 853], "+relevance2+"}\n" +
			"toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, null, null, toString, null, [853, 853], "+relevance2+"}"
			, requestor.getResults());
}
// https://bugs.eclipse.org/515809 - Syso shortcut lambda expression
public void test515809() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"public class X {\n" +
			"	void test() {\n" +
			"		new Thread(() -> sysout);\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "sysout";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("", requestor.getResults());
	assertEquals("completion offset=57\n" +
			"completion range=[51, 56]\n" +
			"completion token=\"sysout\"\n" +
			"completion token kind=TOKEN_KIND_NAME\n" +
			"expectedTypesSignatures=null\n" +
			"expectedTypesKeys=null\n" +
			"completion token location={STATEMENT_START}", requestor.getContext());
}
//https://bugs.eclipse.org/485492
public void test485492a() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/Foo.java",
			"import java.util.function.Function;\n" +
			"public enum Foo {\n" +
			"	BAR((z) -> {\n" +
			"	z.has\n" +
			"		return z;\n" +
			"	});\n" +
			"	Foo(Function<String, String> func) { }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "z.has";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults(
			"hashCode[METHOD_REF]{hashCode(), Ljava.lang.String;, ()I, null, null, hashCode, null, [71, 74], 60}",
			requestor.getResults());
}
public void test485492b() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/Foo.java",
			"import java.util.function.Function;\n" +
			"public enum Foo {\n" +
			"	BAR((zilch) -> {\n" +
			"		return zil;\n" +
			"	});\n" +
			"	Foo(Function<String, String> func) { }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "return zil";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	int relevance = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED;
	assertResults(
			"zilch[LOCAL_VARIABLE_REF]{zilch, null, Ljava.lang.String;, null, null, zilch, null, [81, 84], " + relevance + "}",
			requestor.getResults());
}
public void test485492c() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/Foo.java",
			"import java.util.function.Function;\n" +
			"public enum Foo {\n" +
			"	BAR((z) -> {\n" +
			"		return z.has;\n" +
			"	});\n" +
			"	Foo(Function<String, String> func) { }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "z.has";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults(
			"hashCode[METHOD_REF]{hashCode(), Ljava.lang.String;, ()I, null, null, hashCode, null, [79, 82], 60}",
			requestor.getResults());
}
public void testBug528938a() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/X.java",
		"public class X {\n" +
		"	final String zzz = \"z\";\n" +
		"	void foo(String s){\n" +
		"		switch(s) {\n" +
		"			case zz\n" +
		"		}\n" +
		"	}\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "zz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"zzz[FIELD_REF]{zzz, LX;, Ljava.lang.String;, zzz, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_EXPECTED_TYPE + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED + R_FINAL) + "}",
			requestor.getResults());
}
public void testBug528938b() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/X.java",
		"public class X {\n" +
		"	static final String zzz = \"z\";\n" +
		"	void foo(String s){\n" +
		"		switch(s) {\n" +
		"			case zz\n" +
		"		}\n" +
		"	}\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "zz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"zzz[FIELD_REF]{zzz, LX;, Ljava.lang.String;, zzz, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_EXPECTED_TYPE + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED + R_FINAL) + "}",
			requestor.getResults());
}
/*
 * Test that completion doesn't throw NPE and produces valid completions.
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=529349
 */
public void testBug529349a() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/p/SuperSuper.java",
		"package p;\n" +
		"public class SuperSuper {}\n" +
		"class Super extends SuperSuper {}\n" +
		"class Y {\n" +
		"	static class Super {}\n" +
		"}\n" +
		"class X extends Sup {\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "Sup";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Y.Super[TYPE_REF]{p.Y.Super, p, Lp.Y$Super;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE  + R_NON_RESTRICTED + R_CLASS ) + "}\n" +
			"Super[TYPE_REF]{Super, p, Lp.Super;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE  + R_UNQUALIFIED + R_NON_RESTRICTED + R_CLASS) + "}\n" +
			"SuperSuper[TYPE_REF]{SuperSuper, p, Lp.SuperSuper;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED + R_CLASS) + "}",
			requestor.getResults());
}
public void testBug473654() throws Exception {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/Foo.java",
		"class Foo {\n" +
		"    Runnable foo() {\n" +
		"        return () -> new Object() {\n" +
		"            // press Ctrl+Space before the comment\n" +
		"        };\n" +
		"    }\n" +
		"    \n" +
		"    static void bar() { /**/ }\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBefore = "// press Ctrl+Space before the comment";
	int cursorLocation = str.indexOf(completeBefore);
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int keywordRelevance = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_CASE;
	int overrideRelevance = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_CASE + R_METHOD_OVERIDE;

	assertResults(
			"[POTENTIAL_METHOD_DECLARATION]{, LObject;, ()V, , null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED)+"}\n" +
			"abstract[KEYWORD]{abstract, null, null, abstract, null, "+keywordRelevance+"}\n" +
			"class[KEYWORD]{class, null, null, class, null, "+keywordRelevance+"}\n" +
			"enum[KEYWORD]{enum, null, null, enum, null, "+keywordRelevance+"}\n" +
			"final[KEYWORD]{final, null, null, final, null, "+keywordRelevance+"}\n" +
			"interface[KEYWORD]{interface, null, null, interface, null, "+keywordRelevance+"}\n" +
			"native[KEYWORD]{native, null, null, native, null, "+keywordRelevance+"}\n" +
			"private[KEYWORD]{private, null, null, private, null, "+keywordRelevance+"}\n" +
			"protected[KEYWORD]{protected, null, null, protected, null, "+keywordRelevance+"}\n" +
			"public[KEYWORD]{public, null, null, public, null, "+keywordRelevance+"}\n" +
			"static[KEYWORD]{static, null, null, static, null, "+keywordRelevance+"}\n" +
			"strictfp[KEYWORD]{strictfp, null, null, strictfp, null, "+keywordRelevance+"}\n" +
			"synchronized[KEYWORD]{synchronized, null, null, synchronized, null, "+keywordRelevance+"}\n" +
			"transient[KEYWORD]{transient, null, null, transient, null, "+keywordRelevance+"}\n" +
			"volatile[KEYWORD]{volatile, null, null, volatile, null, "+keywordRelevance+"}\n" +
			"Foo[TYPE_REF]{Foo, , LFoo;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_CASE + R_UNQUALIFIED)+"}\n" +
			"clone[METHOD_DECLARATION]{protected Object clone() throws CloneNotSupportedException, Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, "+overrideRelevance+"}\n" +
			"equals[METHOD_DECLARATION]{public boolean equals(Object obj), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), "+overrideRelevance+"}\n" +
			"finalize[METHOD_DECLARATION]{protected void finalize() throws Throwable, Ljava.lang.Object;, ()V, finalize, null, "+overrideRelevance+"}\n" +
			"hashCode[METHOD_DECLARATION]{public int hashCode(), Ljava.lang.Object;, ()I, hashCode, null, "+overrideRelevance+"}\n" +
			"toString[METHOD_DECLARATION]{public String toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, "+overrideRelevance+"}",
			requestor.getResults());
}
public void testBug537679() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/p/SuperSuper.java",
		"import static java.util.stream.Collectors.toList;\n" +
		"import java.util.List;\n" +
		"\n" +
		"public class Test {\n" +
		"	void foo(List<Object> list) {\n" +
		"		bar(list.stream().map(m -> new Object() {\n" +
		"			// here\n" +
		"		}).collect(toList()));\n" +
		"	}\n" +
		"\n" +
		"	private void bar(List<Object> collect) {\n" +
		"	}\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	int cursorLocation = str.lastIndexOf("// here");
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int keywordRelevance = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_CASE;
	int overrideRelevance = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_CASE + R_METHOD_OVERIDE;
	assertResults(
			"[POTENTIAL_METHOD_DECLARATION]{, LObject;, ()V, , null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED)+"}\n" +
			"abstract[KEYWORD]{abstract, null, null, abstract, null, "+keywordRelevance+"}\n" +
			"class[KEYWORD]{class, null, null, class, null, "+keywordRelevance+"}\n" +
			"enum[KEYWORD]{enum, null, null, enum, null, "+keywordRelevance+"}\n" +
			"final[KEYWORD]{final, null, null, final, null, "+keywordRelevance+"}\n" +
			"interface[KEYWORD]{interface, null, null, interface, null, "+keywordRelevance+"}\n" +
			"native[KEYWORD]{native, null, null, native, null, "+keywordRelevance+"}\n" +
			"private[KEYWORD]{private, null, null, private, null, "+keywordRelevance+"}\n" +
			"protected[KEYWORD]{protected, null, null, protected, null, "+keywordRelevance+"}\n" +
			"public[KEYWORD]{public, null, null, public, null, "+keywordRelevance+"}\n" +
			"static[KEYWORD]{static, null, null, static, null, "+keywordRelevance+"}\n" +
			"strictfp[KEYWORD]{strictfp, null, null, strictfp, null, "+keywordRelevance+"}\n" +
			"synchronized[KEYWORD]{synchronized, null, null, synchronized, null, "+keywordRelevance+"}\n" +
			"transient[KEYWORD]{transient, null, null, transient, null, "+keywordRelevance+"}\n" +
			"volatile[KEYWORD]{volatile, null, null, volatile, null, "+keywordRelevance+"}\n" +
			"Test[TYPE_REF]{Test, p, Lp.Test;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_CASE + R_UNQUALIFIED)+"}\n" +
			"clone[METHOD_DECLARATION]{protected Object clone() throws CloneNotSupportedException, Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, "+overrideRelevance+"}\n" +
			"equals[METHOD_DECLARATION]{public boolean equals(Object obj), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), "+overrideRelevance+"}\n" +
			"finalize[METHOD_DECLARATION]{protected void finalize() throws Throwable, Ljava.lang.Object;, ()V, finalize, null, "+overrideRelevance+"}\n" +
			"hashCode[METHOD_DECLARATION]{public int hashCode(), Ljava.lang.Object;, ()I, hashCode, null, "+overrideRelevance+"}\n" +
			"toString[METHOD_DECLARATION]{public String toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, "+overrideRelevance+"}",
			requestor.getResults());
}

public void testBug460750a() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/Foo.java",
			"public class Foo {\n" +
			"	 enum MyEnum {\n" +
			"		  FOO, BAR\n" +
			"		}\n" +
			"	public void setMyEnumValue(MyEnum myEnumValue) {\n" +
			"	}\n" +
			"	public void meth() {\n" +
			"		this.setMyEnumValue(new String().isEmpty() ? MyEnum.FOO:BAR);\n" +
			"	    }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "FOO:BAR";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"BAR[FIELD_REF]{MyEnum.BAR, LFoo$MyEnum;, LFoo$MyEnum;, BAR, null, 108}",
			requestor.getResults());
}

public void testBug460750b() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/EnumRelatedCompletions.java",
			"public class EnumRelatedCompletions {\n" +
			"	 enum MyEnum {\n" +
			"		  FOO, BAR, QUZ \n" +
			"		}\n" +
			"	public void setMyEnumValue(MyEnum myEnumValue) {\n" +
			"	}\n" +
			"	public void meth() {\n" +
			"		this.setMyEnumValue(new String().isEmpty() ? MyEnum.FOO:BAR);\n" +
			"	    MyEnum e= MyEnum.FOO;\n" +
			"	    if(e  !=QUZ) {    	\n" +
			"	    }\n" +
			"	    }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "=QUZ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"QUZ[FIELD_REF]{MyEnum.QUZ, LEnumRelatedCompletions$MyEnum;, LEnumRelatedCompletions$MyEnum;, QUZ, null, 108}",
			requestor.getResults());
}

/*
* Test that completion doesn't throw NPE
*/
public void testBug535743a() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/test/FooNPE.java",
				"package test;\n" +
				 		"public class FooNPE {\n" +
				 		"	public static void main(String[] args) {	\n" +
				 		"		java.util.function.Consumer<Object> consumer = object -> {new SomeClass().something(obj -> {/*nop*/}).\n" +
				 		"		};\n" +
				 		"	}\n" +
				 		"class SomeClass {\n" +
				 		"public void something(java.util.function.Consumer<Object> otherConsumer) {\n" +
				 		" }\n" +
				 		"}\n" +
				 		"}\n");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = ".something(obj -> {/*nop*/}).";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
			"",
			requestor.getResults());
		assertTrue(requestor.getResults().equals(""));
}
/*
* Test that completion produces valid completions.
*/
public void testBug535743b() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/FooNPE.java",
			"package test;\n" +
			 		"public class FooNPE {\n" +
			 		"	public static void main(String[] args) {	\n" +
			 		"		java.util.function.Consumer<Object> consumer = object -> {new SomeClass().something(obj -> {}).\n" +
			 		"		};\n" +
			 		"	}\n" +
			 		"class SomeClass {\n" +
			 		"public Object something(java.util.function.Consumer<Object> otherConsumer) {\n" +
			 		"return new Object(); \n" +
			 		" }\n" +
			 		"}\n" +
			 		"}\n");
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = ".something(obj -> {}).";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertTrue(!requestor.getResults().equals(""));
	assertTrue(requestor.getResults().contains("toString"));
}
public void testBug526044() throws Exception {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/p/Test.java",
			"package p;\n" +
			"import java.util.stream.Stream;\n" +
			"import java.util.Optional;\n" +
			"interface ProcessHandle {\n" +
			"	static Stream<ProcessHandle> allProcesses();\n" +
			"	Info info();\n" +
			"}\n" +
			"interface Info {\n" +
			"	Optional<String> command();\n" +
			"}\n" +
			"public class Test {\n" +
			"	void foo() {\n" +
			"		ProcessHandle.allProcesses().forEach(p -> {\n" +
			"			p.info().command().ifPresent(o -> {\n" +
			"				System.out.println(o);\n" +
			"			}).\n" +
			"		});" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "}).";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"",
			requestor.getResults());
}
public void testBug539546() throws Exception {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/p/Test.java",
			"package p;\n" +
			"public class Test {\n" +
			"	public Test(Runnable run) {}\n" +
			"}\n");
	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src/p/Test.java",
			"package p;\n" +
			"public class Main {\n" +
			"	public void myTestOfStackOverflow() {\n" +
			"		() -> {\n" +
			"			new Test(() -> {}).\n" +
			"		}\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[1].getSource();
	String completeBehind = "}).";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"",
			requestor.getResults());
}
public void testBug477626() throws Exception {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/p/Snippet29.java",
			"package p;\n" +
			"import java.util.Arrays;\n" +
			"import java.util.function.Consumer;\n" +
			"\n" +
			"public class Snippet29 {\n" +
			"\n" +
			"class Display {\n" +
			"	public void asyncExec(Runnable runnable) { }\n" +
			"}\n" +
			"class Shell {\n" +
			"	Shell(Display display) {}\n" +
			"	public Shell(Shell shell, int i) { }\n" +
			"	public void setLayout(GridLayout gridLayout) { }\n" +
			"	public void setText(String string) { }\n" +
			"	public void pack() { }\n" +
			"	public Point getLocation() { return null; }\n" +
			"	public void open() { }\n" +
			"	public void close() { }\n" +
			"	public void setLocation(int i, int j) { }\n" +
			"}\n" +
			"class Point {\n" +
			"	int x, y;\n" +
			"}\n" +
			"class GridLayout {\n" +
			"	public GridLayout() { }\n" +
			"	public GridLayout(int i, boolean b) { }\n" +
			"}\n" +
			"class GridData {\n" +
			"	public GridData(int fill, int fill2, boolean b, boolean c, int i, int j) { }\n" +
			"	public GridData(int fill, int fill2, boolean b, boolean c) { }\n" +
			"}\n" +
			"class Widget {\n" +
			"	public void setText(String string) { }\n" +
			"	public void setLayoutData(GridData gridData) { }\n" +
			"}\n" +
			"class Button extends Widget {\n" +
			"	Button(Shell shell, int style) { }\n" +
			"	public void addListener(int selection, Consumer<Event> listener) { }\n" +
			"}\n" +
			"class Label extends Widget {\n" +
			"	public Label(Shell dialog, int none) { }\n" +
			"}\n" +
			"class Event {}\n" +
			"class SWT {\n" +
			"	public static final int PUSH = 1;\n" +
			"	public static final int Selection = 2;\n" +
			"	protected static final int DIALOG_TRIM = 3;\n" +
			"	protected static final int APPLICATION_MODAL = 4;\n" +
			"	protected static final int NONE = 5;\n" +
			"	protected static final int FILL = 6;\n" +
			"}\n" +
			"class Timer {\n" +
			"	public void schedule(TimerTask timerTask, int i) { }\n" +
			"}\n" +
			"abstract class TimerTask implements Runnable {}\n" +
			"public static void main (String [] args) {\n" +
			"	Display display = new Display ();\n" +
			"	Shell shell = new Shell (display);\n" +
			"	shell.setLayout(new GridLayout());\n" +
			"	Button b = new Button(shell, SWT.PUSH);\n" +
			"	b.setText(\"Open dialog in 3s\");\n" +
			"	b.addListener(SWT.Selection, e -> {\n" +
			"		new Timer().schedule(new TimerTask() {\n" +
			"			@Override\n" +
			"			public void run() {\n" +
			"				display.asyncExec(new Runnable() {\n" +
			"					@Override\n" +
			"					public void run() {\n" +
			"						Shell dialog = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);\n" +
			"						dialog.setText(\"Question\");\n" +
			"						dialog.setLayout(new GridLayout(3, true));\n" +
			"						Label label = new Label(dialog, SWT.NONE);\n" +
			"						label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));\n" +
			"						label.setText(\"Do you really want to clear the runtime workspace?\");\n" +
			"						Arrays.asList(\"Yes\", \"No\", \"Cancel\").forEach(t -> {\n" +
			"							Button button = new Button(dialog, SWT.PUSH);\n" +
			"							button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));\n" +
			"							button.setText(t);\n" +
			"							button.addListener(SWT.Selection, e -> { dialog.close(); });\n" +
			"						});\n" +
			"						dialog.pack();\n" +
			"						dialog.setLocation(shell.getLocation().x + 40, shell.getLocation().y + 80);\n" +
			"						dialog.open();\n" +
			"					}\n" +
			"				}).;\n" +
			"			}\n" +
			"		}, 2000);\n" +
			"	});\n" +
			"}\n" +
			"\n" +
			"} \n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "}).";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"",
			requestor.getResults());
}
public void testBug490096() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/Completion/src/ShowSOEInEclipseMars2.java",
			"import java.util.concurrent.CompletableFuture;\n" +
			"\n" +
			"public class ShowSOEInEclipseMars2 {\n" +
			" \n" +
			"public void crashWithStackOverflowError() {\n" +
			"   \n" +
			" CompletableFuture<Double> intermediate = CompletableFuture.supplyAsync(() -> {\n" +
			"  try {\n" +
			"   CompletableFuture.supplyAsync(() -> { return 0D; }).;\n" +
			"  } catch (Exception e) {\n" +
			"  }\n" +
			"  return 1D;\n" +
			" });\n" +
			" }\n" +
			"}\n");
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "}).";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED;
	int relevance2 = relevance1 + R_NON_STATIC;
	assertResults(
			"allOf[METHOD_REF]{allOf(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, ([Ljava.util.concurrent.CompletableFuture<*>;)Ljava.util.concurrent.CompletableFuture<Ljava.lang.Void;>;, allOf, (arg0), " + relevance1 + "}\n" +
			"anyOf[METHOD_REF]{anyOf(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, ([Ljava.util.concurrent.CompletableFuture<*>;)Ljava.util.concurrent.CompletableFuture<Ljava.lang.Object;>;, anyOf, (arg0), " + relevance1 + "}\n" +
			"completedFuture[METHOD_REF]{completedFuture(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, <U:Ljava.lang.Object;>(TU;)Ljava.util.concurrent.CompletableFuture<TU;>;, completedFuture, (arg0), " + relevance1 + "}\n" +
			"new[KEYWORD]{new, null, null, new, null, " + relevance1 + "}\n" +
			"runAsync[METHOD_REF]{runAsync(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, (Ljava.lang.Runnable;)Ljava.util.concurrent.CompletableFuture<Ljava.lang.Void;>;, runAsync, (arg0), " + relevance1 + "}\n" +
			"runAsync[METHOD_REF]{runAsync(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, (Ljava.lang.Runnable;Ljava.util.concurrent.Executor;)Ljava.util.concurrent.CompletableFuture<Ljava.lang.Void;>;, runAsync, (arg0, arg1), " + relevance1 + "}\n" +
			"supplyAsync[METHOD_REF]{supplyAsync(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, <U:Ljava.lang.Object;>(Ljava.util.function.Supplier<TU;>;)Ljava.util.concurrent.CompletableFuture<TU;>;, supplyAsync, (arg0), " + relevance1 + "}\n" +
			"supplyAsync[METHOD_REF]{supplyAsync(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, <U:Ljava.lang.Object;>(Ljava.util.function.Supplier<TU;>;Ljava.util.concurrent.Executor;)Ljava.util.concurrent.CompletableFuture<TU;>;, supplyAsync, (arg0, arg1), " + relevance1 + "}\n" +
			"acceptEither[METHOD_REF]{acceptEither(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, (Ljava.util.concurrent.CompletionStage<+Ljava.lang.Double;>;Ljava.util.function.Consumer<-Ljava.lang.Double;>;)Ljava.util.concurrent.CompletableFuture<Ljava.lang.Void;>;, acceptEither, (arg0, arg1), " + relevance2 + "}\n" +
			"acceptEitherAsync[METHOD_REF]{acceptEitherAsync(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, (Ljava.util.concurrent.CompletionStage<+Ljava.lang.Double;>;Ljava.util.function.Consumer<-Ljava.lang.Double;>;)Ljava.util.concurrent.CompletableFuture<Ljava.lang.Void;>;, acceptEitherAsync, (arg0, arg1), " + relevance2 + "}\n" +
			"acceptEitherAsync[METHOD_REF]{acceptEitherAsync(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, (Ljava.util.concurrent.CompletionStage<+Ljava.lang.Double;>;Ljava.util.function.Consumer<-Ljava.lang.Double;>;Ljava.util.concurrent.Executor;)Ljava.util.concurrent.CompletableFuture<Ljava.lang.Void;>;, acceptEitherAsync, (arg0, arg1, arg2), " + relevance2 + "}\n" +
			"applyToEither[METHOD_REF]{applyToEither(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, <U:Ljava.lang.Object;>(Ljava.util.concurrent.CompletionStage<+Ljava.lang.Double;>;Ljava.util.function.Function<-Ljava.lang.Double;TU;>;)Ljava.util.concurrent.CompletableFuture<TU;>;, applyToEither, (arg0, arg1), " + relevance2 + "}\n" +
			"applyToEitherAsync[METHOD_REF]{applyToEitherAsync(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, <U:Ljava.lang.Object;>(Ljava.util.concurrent.CompletionStage<+Ljava.lang.Double;>;Ljava.util.function.Function<-Ljava.lang.Double;TU;>;)Ljava.util.concurrent.CompletableFuture<TU;>;, applyToEitherAsync, (arg0, arg1), " + relevance2 + "}\n" +
			"applyToEitherAsync[METHOD_REF]{applyToEitherAsync(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, <U:Ljava.lang.Object;>(Ljava.util.concurrent.CompletionStage<+Ljava.lang.Double;>;Ljava.util.function.Function<-Ljava.lang.Double;TU;>;Ljava.util.concurrent.Executor;)Ljava.util.concurrent.CompletableFuture<TU;>;, applyToEitherAsync, (arg0, arg1, arg2), " + relevance2 + "}\n" +
			"cancel[METHOD_REF]{cancel(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, (Z)Z, cancel, (arg0), " + relevance2 + "}\n" +
			"clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, " + relevance2 + "}\n" +
			"complete[METHOD_REF]{complete(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, (Ljava.lang.Double;)Z, complete, (arg0), " + relevance2 + "}\n" +
			"completeExceptionally[METHOD_REF]{completeExceptionally(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, (Ljava.lang.Throwable;)Z, completeExceptionally, (arg0), " + relevance2 + "}\n" +
			"equals[METHOD_REF]{equals(), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), " + relevance2 + "}\n" +
			"exceptionally[METHOD_REF]{exceptionally(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, (Ljava.util.function.Function<Ljava.lang.Throwable;+Ljava.lang.Double;>;)Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, exceptionally, (arg0), " + relevance2 + "}\n" +
			"finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, " + relevance2 + "}\n" +
			"get[METHOD_REF]{get(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, ()Ljava.lang.Double;, get, null, " + relevance2 + "}\n" +
			"get[METHOD_REF]{get(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, (JLjava.util.concurrent.TimeUnit;)Ljava.lang.Double;, get, (arg0, arg1), " + relevance2 + "}\n" +
			"getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<*>;, getClass, null, " + relevance2 + "}\n" +
			"getNow[METHOD_REF]{getNow(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, (Ljava.lang.Double;)Ljava.lang.Double;, getNow, (arg0), " + relevance2 + "}\n" +
			"getNumberOfDependents[METHOD_REF]{getNumberOfDependents(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, ()I, getNumberOfDependents, null, " + relevance2 + "}\n" +
			"handle[METHOD_REF]{handle(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, <U:Ljava.lang.Object;>(Ljava.util.function.BiFunction<-Ljava.lang.Double;Ljava.lang.Throwable;+TU;>;)Ljava.util.concurrent.CompletableFuture<TU;>;, handle, (arg0), " + relevance2 + "}\n" +
			"handleAsync[METHOD_REF]{handleAsync(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, <U:Ljava.lang.Object;>(Ljava.util.function.BiFunction<-Ljava.lang.Double;Ljava.lang.Throwable;+TU;>;)Ljava.util.concurrent.CompletableFuture<TU;>;, handleAsync, (arg0), " + relevance2 + "}\n" +
			"handleAsync[METHOD_REF]{handleAsync(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, <U:Ljava.lang.Object;>(Ljava.util.function.BiFunction<-Ljava.lang.Double;Ljava.lang.Throwable;+TU;>;Ljava.util.concurrent.Executor;)Ljava.util.concurrent.CompletableFuture<TU;>;, handleAsync, (arg0, arg1), " + relevance2 + "}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, " + relevance2 + "}\n" +
			"isCancelled[METHOD_REF]{isCancelled(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, ()Z, isCancelled, null, " + relevance2 + "}\n" +
			"isCompletedExceptionally[METHOD_REF]{isCompletedExceptionally(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, ()Z, isCompletedExceptionally, null, " + relevance2 + "}\n" +
			"isDone[METHOD_REF]{isDone(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, ()Z, isDone, null, " + relevance2 + "}\n" +
			"join[METHOD_REF]{join(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, ()Ljava.lang.Double;, join, null, " + relevance2 + "}\n" +
			"notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, " + relevance2 + "}\n" +
			"notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, " + relevance2 + "}\n" +
			"obtrudeException[METHOD_REF]{obtrudeException(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, (Ljava.lang.Throwable;)V, obtrudeException, (arg0), " + relevance2 + "}\n" +
			"obtrudeValue[METHOD_REF]{obtrudeValue(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, (Ljava.lang.Double;)V, obtrudeValue, (arg0), " + relevance2 + "}\n" +
			"runAfterBoth[METHOD_REF]{runAfterBoth(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, (Ljava.util.concurrent.CompletionStage<*>;Ljava.lang.Runnable;)Ljava.util.concurrent.CompletableFuture<Ljava.lang.Void;>;, runAfterBoth, (arg0, arg1), " + relevance2 + "}\n" +
			"runAfterBothAsync[METHOD_REF]{runAfterBothAsync(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, (Ljava.util.concurrent.CompletionStage<*>;Ljava.lang.Runnable;)Ljava.util.concurrent.CompletableFuture<Ljava.lang.Void;>;, runAfterBothAsync, (arg0, arg1), " + relevance2 + "}\n" +
			"runAfterBothAsync[METHOD_REF]{runAfterBothAsync(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, (Ljava.util.concurrent.CompletionStage<*>;Ljava.lang.Runnable;Ljava.util.concurrent.Executor;)Ljava.util.concurrent.CompletableFuture<Ljava.lang.Void;>;, runAfterBothAsync, (arg0, arg1, arg2), " + relevance2 + "}\n" +
			"runAfterEither[METHOD_REF]{runAfterEither(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, (Ljava.util.concurrent.CompletionStage<*>;Ljava.lang.Runnable;)Ljava.util.concurrent.CompletableFuture<Ljava.lang.Void;>;, runAfterEither, (arg0, arg1), " + relevance2 + "}\n" +
			"runAfterEitherAsync[METHOD_REF]{runAfterEitherAsync(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, (Ljava.util.concurrent.CompletionStage<*>;Ljava.lang.Runnable;)Ljava.util.concurrent.CompletableFuture<Ljava.lang.Void;>;, runAfterEitherAsync, (arg0, arg1), " + relevance2 + "}\n" +
			"runAfterEitherAsync[METHOD_REF]{runAfterEitherAsync(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, (Ljava.util.concurrent.CompletionStage<*>;Ljava.lang.Runnable;Ljava.util.concurrent.Executor;)Ljava.util.concurrent.CompletableFuture<Ljava.lang.Void;>;, runAfterEitherAsync, (arg0, arg1, arg2), " + relevance2 + "}\n" +
			"thenAccept[METHOD_REF]{thenAccept(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, (Ljava.util.function.Consumer<-Ljava.lang.Double;>;)Ljava.util.concurrent.CompletableFuture<Ljava.lang.Void;>;, thenAccept, (arg0), " + relevance2 + "}\n" +
			"thenAcceptAsync[METHOD_REF]{thenAcceptAsync(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, (Ljava.util.function.Consumer<-Ljava.lang.Double;>;)Ljava.util.concurrent.CompletableFuture<Ljava.lang.Void;>;, thenAcceptAsync, (arg0), " + relevance2 + "}\n" +
			"thenAcceptAsync[METHOD_REF]{thenAcceptAsync(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, (Ljava.util.function.Consumer<-Ljava.lang.Double;>;Ljava.util.concurrent.Executor;)Ljava.util.concurrent.CompletableFuture<Ljava.lang.Void;>;, thenAcceptAsync, (arg0, arg1), " + relevance2 + "}\n" +
			"thenAcceptBoth[METHOD_REF]{thenAcceptBoth(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, <U:Ljava.lang.Object;>(Ljava.util.concurrent.CompletionStage<+TU;>;Ljava.util.function.BiConsumer<-Ljava.lang.Double;-TU;>;)Ljava.util.concurrent.CompletableFuture<Ljava.lang.Void;>;, thenAcceptBoth, (arg0, arg1), " + relevance2 + "}\n" +
			"thenAcceptBothAsync[METHOD_REF]{thenAcceptBothAsync(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, <U:Ljava.lang.Object;>(Ljava.util.concurrent.CompletionStage<+TU;>;Ljava.util.function.BiConsumer<-Ljava.lang.Double;-TU;>;)Ljava.util.concurrent.CompletableFuture<Ljava.lang.Void;>;, thenAcceptBothAsync, (arg0, arg1), " + relevance2 + "}\n" +
			"thenAcceptBothAsync[METHOD_REF]{thenAcceptBothAsync(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, <U:Ljava.lang.Object;>(Ljava.util.concurrent.CompletionStage<+TU;>;Ljava.util.function.BiConsumer<-Ljava.lang.Double;-TU;>;Ljava.util.concurrent.Executor;)Ljava.util.concurrent.CompletableFuture<Ljava.lang.Void;>;, thenAcceptBothAsync, (arg0, arg1, arg2), " + relevance2 + "}\n" +
			"thenApply[METHOD_REF]{thenApply(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, <U:Ljava.lang.Object;>(Ljava.util.function.Function<-Ljava.lang.Double;+TU;>;)Ljava.util.concurrent.CompletableFuture<TU;>;, thenApply, (arg0), " + relevance2 + "}\n" +
			"thenApplyAsync[METHOD_REF]{thenApplyAsync(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, <U:Ljava.lang.Object;>(Ljava.util.function.Function<-Ljava.lang.Double;+TU;>;)Ljava.util.concurrent.CompletableFuture<TU;>;, thenApplyAsync, (arg0), " + relevance2 + "}\n" +
			"thenApplyAsync[METHOD_REF]{thenApplyAsync(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, <U:Ljava.lang.Object;>(Ljava.util.function.Function<-Ljava.lang.Double;+TU;>;Ljava.util.concurrent.Executor;)Ljava.util.concurrent.CompletableFuture<TU;>;, thenApplyAsync, (arg0, arg1), " + relevance2 + "}\n" +
			"thenCombine[METHOD_REF]{thenCombine(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, <U:Ljava.lang.Object;V:Ljava.lang.Object;>(Ljava.util.concurrent.CompletionStage<+TU;>;Ljava.util.function.BiFunction<-Ljava.lang.Double;-TU;+TV;>;)Ljava.util.concurrent.CompletableFuture<TV;>;, thenCombine, (arg0, arg1), " + relevance2 + "}\n" +
			"thenCombineAsync[METHOD_REF]{thenCombineAsync(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, <U:Ljava.lang.Object;V:Ljava.lang.Object;>(Ljava.util.concurrent.CompletionStage<+TU;>;Ljava.util.function.BiFunction<-Ljava.lang.Double;-TU;+TV;>;)Ljava.util.concurrent.CompletableFuture<TV;>;, thenCombineAsync, (arg0, arg1), " + relevance2 + "}\n" +
			"thenCombineAsync[METHOD_REF]{thenCombineAsync(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, <U:Ljava.lang.Object;V:Ljava.lang.Object;>(Ljava.util.concurrent.CompletionStage<+TU;>;Ljava.util.function.BiFunction<-Ljava.lang.Double;-TU;+TV;>;Ljava.util.concurrent.Executor;)Ljava.util.concurrent.CompletableFuture<TV;>;, thenCombineAsync, (arg0, arg1, arg2), " + relevance2 + "}\n" +
			"thenCompose[METHOD_REF]{thenCompose(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, <U:Ljava.lang.Object;>(Ljava.util.function.Function<-Ljava.lang.Double;+Ljava.util.concurrent.CompletionStage<TU;>;>;)Ljava.util.concurrent.CompletableFuture<TU;>;, thenCompose, (arg0), " + relevance2 + "}\n" +
			"thenComposeAsync[METHOD_REF]{thenComposeAsync(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, <U:Ljava.lang.Object;>(Ljava.util.function.Function<-Ljava.lang.Double;+Ljava.util.concurrent.CompletionStage<TU;>;>;)Ljava.util.concurrent.CompletableFuture<TU;>;, thenComposeAsync, (arg0), " + relevance2 + "}\n" +
			"thenComposeAsync[METHOD_REF]{thenComposeAsync(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, <U:Ljava.lang.Object;>(Ljava.util.function.Function<-Ljava.lang.Double;+Ljava.util.concurrent.CompletionStage<TU;>;>;Ljava.util.concurrent.Executor;)Ljava.util.concurrent.CompletableFuture<TU;>;, thenComposeAsync, (arg0, arg1), " + relevance2 + "}\n" +
			"thenRun[METHOD_REF]{thenRun(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, (Ljava.lang.Runnable;)Ljava.util.concurrent.CompletableFuture<Ljava.lang.Void;>;, thenRun, (arg0), " + relevance2 + "}\n" +
			"thenRunAsync[METHOD_REF]{thenRunAsync(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, (Ljava.lang.Runnable;)Ljava.util.concurrent.CompletableFuture<Ljava.lang.Void;>;, thenRunAsync, (arg0), " + relevance2 + "}\n" +
			"thenRunAsync[METHOD_REF]{thenRunAsync(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, (Ljava.lang.Runnable;Ljava.util.concurrent.Executor;)Ljava.util.concurrent.CompletableFuture<Ljava.lang.Void;>;, thenRunAsync, (arg0, arg1), " + relevance2 + "}\n" +
			"toCompletableFuture[METHOD_REF]{toCompletableFuture(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, ()Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, toCompletableFuture, null, " + relevance2 + "}\n" +
			"toString[METHOD_REF]{toString(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, ()Ljava.lang.String;, toString, null, " + relevance2 + "}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, " + relevance2 + "}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, wait, (millis), " + relevance2 + "}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), " + relevance2 + "}\n" +
			"whenComplete[METHOD_REF]{whenComplete(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, (Ljava.util.function.BiConsumer<-Ljava.lang.Double;-Ljava.lang.Throwable;>;)Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, whenComplete, (arg0), " + relevance2 + "}\n" +
			"whenCompleteAsync[METHOD_REF]{whenCompleteAsync(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, (Ljava.util.function.BiConsumer<-Ljava.lang.Double;-Ljava.lang.Throwable;>;)Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, whenCompleteAsync, (arg0), " + relevance2 + "}\n" +
			"whenCompleteAsync[METHOD_REF]{whenCompleteAsync(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, (Ljava.util.function.BiConsumer<-Ljava.lang.Double;-Ljava.lang.Throwable;>;Ljava.util.concurrent.Executor;)Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, whenCompleteAsync, (arg0, arg1), " + relevance2 + "}",
			requestor.getResults());
}
public void testBug490096a() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/Completion/src/ShowSOEInEclipseMars2.java",
			"import java.util.concurrent.CompletableFuture;\n" +
			"\n" +
			"public class ShowSOEInEclipseMars2 {\n" +
			" \n" +
			"public void crashWithStackOverflowError() {\n" +
			"   \n" +
			" CompletableFuture<Double> intermediate = CompletableFuture.supplyAsync(() -> {\n" +
			"  try {\n" +
			"   CompletableFuture.supplyAsync(() -> { return 0D; }).a;\n" +
			"  } catch (Exception e) {\n" +
			"  }\n" +
			"  return 1D;\n" +
			" });\n" +
			" }\n" +
			"}\n");
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "}).a";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED;
	int relevance2 = relevance1 + R_NON_STATIC;
	assertResults(
			"allOf[METHOD_REF]{allOf(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, ([Ljava.util.concurrent.CompletableFuture<*>;)Ljava.util.concurrent.CompletableFuture<Ljava.lang.Void;>;, allOf, (arg0), " + relevance1 + "}\n" +
			"anyOf[METHOD_REF]{anyOf(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, ([Ljava.util.concurrent.CompletableFuture<*>;)Ljava.util.concurrent.CompletableFuture<Ljava.lang.Object;>;, anyOf, (arg0), " + relevance1 + "}\n" +
			"acceptEither[METHOD_REF]{acceptEither(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, (Ljava.util.concurrent.CompletionStage<+Ljava.lang.Double;>;Ljava.util.function.Consumer<-Ljava.lang.Double;>;)Ljava.util.concurrent.CompletableFuture<Ljava.lang.Void;>;, acceptEither, (arg0, arg1), " + relevance2 + "}\n" +
			"acceptEitherAsync[METHOD_REF]{acceptEitherAsync(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, (Ljava.util.concurrent.CompletionStage<+Ljava.lang.Double;>;Ljava.util.function.Consumer<-Ljava.lang.Double;>;)Ljava.util.concurrent.CompletableFuture<Ljava.lang.Void;>;, acceptEitherAsync, (arg0, arg1), " + relevance2 + "}\n" +
			"acceptEitherAsync[METHOD_REF]{acceptEitherAsync(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, (Ljava.util.concurrent.CompletionStage<+Ljava.lang.Double;>;Ljava.util.function.Consumer<-Ljava.lang.Double;>;Ljava.util.concurrent.Executor;)Ljava.util.concurrent.CompletableFuture<Ljava.lang.Void;>;, acceptEitherAsync, (arg0, arg1, arg2), " + relevance2 + "}\n" +
			"applyToEither[METHOD_REF]{applyToEither(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, <U:Ljava.lang.Object;>(Ljava.util.concurrent.CompletionStage<+Ljava.lang.Double;>;Ljava.util.function.Function<-Ljava.lang.Double;TU;>;)Ljava.util.concurrent.CompletableFuture<TU;>;, applyToEither, (arg0, arg1), " + relevance2 + "}\n" +
			"applyToEitherAsync[METHOD_REF]{applyToEitherAsync(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, <U:Ljava.lang.Object;>(Ljava.util.concurrent.CompletionStage<+Ljava.lang.Double;>;Ljava.util.function.Function<-Ljava.lang.Double;TU;>;)Ljava.util.concurrent.CompletableFuture<TU;>;, applyToEitherAsync, (arg0, arg1), " + relevance2 + "}\n" +
			"applyToEitherAsync[METHOD_REF]{applyToEitherAsync(), Ljava.util.concurrent.CompletableFuture<Ljava.lang.Double;>;, <U:Ljava.lang.Object;>(Ljava.util.concurrent.CompletionStage<+Ljava.lang.Double;>;Ljava.util.function.Function<-Ljava.lang.Double;TU;>;Ljava.util.concurrent.Executor;)Ljava.util.concurrent.CompletableFuture<TU;>;, applyToEitherAsync, (arg0, arg1, arg2), " + relevance2 + "}",
			requestor.getResults());
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=277691
 */
public void testCompletionConstructorRelevance() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/Foo.java",
            "import java.util.Queue;\n" +
            "\n" +
            "public class Foo {\n" +
            "	public void foo () {\n" +
            "		Queue<String> res = new LinkedBlockingQueue<>();\n" +
            "	}\n" +
            "}");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
    requestor.setAllowsRequiredProposals(CompletionProposal.CONSTRUCTOR_INVOCATION, CompletionProposal.TYPE_REF, true);

    String str = this.workingCopies[0].getSource();
    String completeBehind = "new LinkedBlocking";
    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    int expectedConstructorRelevance = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_CASE + R_EXPECTED_TYPE + R_CONSTRUCTOR;
    assertResults(
            "LinkedBlockingDeque[CONSTRUCTOR_INVOCATION]{(), Ljava.util.concurrent.LinkedBlockingDeque;, ()V, LinkedBlockingDeque, null, " + expectedConstructorRelevance + "}\n" +
            "LinkedBlockingDeque[CONSTRUCTOR_INVOCATION]{(), Ljava.util.concurrent.LinkedBlockingDeque;, (I)V, LinkedBlockingDeque, (arg0), " + expectedConstructorRelevance + "}\n" +
            "LinkedBlockingDeque[CONSTRUCTOR_INVOCATION]{(), Ljava.util.concurrent.LinkedBlockingDeque;, (Ljava.util.Collection<+TE;>;)V, LinkedBlockingDeque, (arg0), " + expectedConstructorRelevance + "}\n" +
            "LinkedBlockingQueue[CONSTRUCTOR_INVOCATION]{(), Ljava.util.concurrent.LinkedBlockingQueue;, ()V, LinkedBlockingQueue, null, " + expectedConstructorRelevance + "}\n" +
            "LinkedBlockingQueue[CONSTRUCTOR_INVOCATION]{(), Ljava.util.concurrent.LinkedBlockingQueue;, (I)V, LinkedBlockingQueue, (arg0), " + expectedConstructorRelevance + "}\n" +
            "LinkedBlockingQueue[CONSTRUCTOR_INVOCATION]{(), Ljava.util.concurrent.LinkedBlockingQueue;, (Ljava.util.Collection<+TE;>;)V, LinkedBlockingQueue, (arg0), " + expectedConstructorRelevance + "}",
            requestor.getResults());
}

public void testBug570593_SingleTypeParam() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/Bug570593.java",
            "import java.util.List;\n" +
            "\n" +
            "public class Bug570593 {\n" +
            "	private List<XBug570593>\n" +
            "}");
	this.workingCopies[1] = getWorkingCopy(
            "/Completion/src/XBug570593Type.java",
            "\n" +
            "public class XBug570593Type {\n" +
            "}");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "XBug570593";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    assertResults(
            "XBug570593Type[TYPE_REF]{XBug570593Type, , LXBug570593Type;, null, null, 72}",
            requestor.getResults());
}

public void testBug570593_MultipleTypeParams_OnFirstTP() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/Bug570593.java",
            "import java.util.Map;\n" +
            "\n" +
            "public class Bug570593 {\n" +
            "	private Map<XBug570593,V>\n" +
            "}");
	this.workingCopies[1] = getWorkingCopy(
            "/Completion/src/XBug570593Type.java",
            "\n" +
            "public class XBug570593Type {\n" +
            "}");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "XBug570593";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    assertResults(
            "XBug570593Type[TYPE_REF]{XBug570593Type, , LXBug570593Type;, null, null, 72}",
            requestor.getResults());
}

public void testBug570593_MultipleTypeParams_OnSecondTP() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/Bug570593.java",
            "import java.util.Map;\n" +
            "\n" +
            "public class Bug570593 {\n" +
            "	private Map<Long,XBug570593>\n" +
            "}");
	this.workingCopies[1] = getWorkingCopy(
            "/Completion/src/XBug570593Type.java",
            "\n" +
            "public class XBug570593Type {\n" +
            "}");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "XBug570593";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    assertResults(
            "XBug570593Type[TYPE_REF]{XBug570593Type, , LXBug570593Type;, null, null, 72}",
            requestor.getResults());
}

public void testBug570593_SingleTypeParam_NestedSingleParam() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/Bug570593.java",
            "import java.util.List;\n" +
            "\n" +
            "public class Bug570593 {\n" +
            "	private List<List<XBug570593>>\n" +
            "}");
	this.workingCopies[1] = getWorkingCopy(
            "/Completion/src/XBug570593Type.java",
            "\n" +
            "public class XBug570593Type {\n" +
            "}");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "XBug570593";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    assertResults(
            "XBug570593Type[TYPE_REF]{XBug570593Type, , LXBug570593Type;, null, null, 72}",
            requestor.getResults());
}

public void testBug570593_SingleTypeParam_NestedMultiParams_OnFirst() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/Bug570593.java",
            "import java.util.List;\n" +
            "import java.util.Map;\n" +
            "\n" +
            "public class Bug570593 {\n" +
            "	private List<Map<XBug570593,V>>\n" +
            "}");
	this.workingCopies[1] = getWorkingCopy(
            "/Completion/src/XBug570593Type.java",
            "\n" +
            "public class XBug570593Type {\n" +
            "}");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "XBug570593";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    assertResults(
            "XBug570593Type[TYPE_REF]{XBug570593Type, , LXBug570593Type;, null, null, 72}",
            requestor.getResults());
}

public void testBug570593_SingleTypeParam_NestedMultiParams_OnSecond() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/Bug570593.java",
            "import java.util.List;\n" +
            "import java.util.Map;\n" +
            "\n" +
            "public class Bug570593 {\n" +
            "	private List<Map<Long,XBug570593>>\n" +
            "}");
	this.workingCopies[1] = getWorkingCopy(
            "/Completion/src/XBug570593Type.java",
            "\n" +
            "public class XBug570593Type {\n" +
            "}");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "XBug570593";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    assertResults(
            "XBug570593Type[TYPE_REF]{XBug570593Type, , LXBug570593Type;, null, null, 72}",
            requestor.getResults());
}

public void testBug570593_MultiTypeParam_OnFirst_NestedSingleParam() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/Bug570593.java",
            "import java.util.List;\n" +
            "import java.util.Map;\n" +
            "\n" +
            "public class Bug570593 {\n" +
            "	private Map<List<XBug570593>,V>\n" +
            "}");
	this.workingCopies[1] = getWorkingCopy(
            "/Completion/src/XBug570593Type.java",
            "\n" +
            "public class XBug570593Type {\n" +
            "}");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "XBug570593";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    assertResults(
            "XBug570593Type[TYPE_REF]{XBug570593Type, , LXBug570593Type;, null, null, 72}",
            requestor.getResults());
}

public void testBug570593_MultiTypeParam_OnSecond_NestedSingleParam() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/Bug570593.java",
            "import java.util.List;\n" +
            "import java.util.Map;\n" +
            "\n" +
            "public class Bug570593 {\n" +
            "	private Map<Long,List<XBug570593>>\n" +
            "}");
	this.workingCopies[1] = getWorkingCopy(
            "/Completion/src/XBug570593Type.java",
            "\n" +
            "public class XBug570593Type {\n" +
            "}");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "XBug570593";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    assertResults(
            "XBug570593Type[TYPE_REF]{XBug570593Type, , LXBug570593Type;, null, null, 72}",
            requestor.getResults());
}

public void testBug570593_MultiTypeParam_NestedMultiParam_OnFirst() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/Bug570593.java",
            "import java.util.List;\n" +
            "import java.util.Map;\n" +
            "\n" +
            "public class Bug570593 {\n" +
            "	private Map<Long,Map<XBug570593,R>>\n" +
            "}");
	this.workingCopies[1] = getWorkingCopy(
            "/Completion/src/XBug570593Type.java",
            "\n" +
            "public class XBug570593Type {\n" +
            "}");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "XBug570593";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    assertResults(
            "XBug570593Type[TYPE_REF]{XBug570593Type, , LXBug570593Type;, null, null, 72}",
            requestor.getResults());
}

public void testBug570593_MultiTypeParam_NestedMultiParam_OnSecond() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/Bug570593.java",
            "import java.util.List;\n" +
            "import java.util.Map;\n" +
            "\n" +
            "public class Bug570593 {\n" +
            "	private Map<Long,Map<String,XBug570593>>\n" +
            "}");
	this.workingCopies[1] = getWorkingCopy(
            "/Completion/src/XBug570593Type.java",
            "\n" +
            "public class XBug570593Type {\n" +
            "}");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "XBug570593";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    assertResults(
            "XBug570593Type[TYPE_REF]{XBug570593Type, , LXBug570593Type;, null, null, 72}",
            requestor.getResults());
}

public void testBug572315_OnFieldAboveAnnotatedMember_VariableNameSuggestion() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/Bug570593.java",
            "import java.util.List;\n" +
            "\n" +
            "public class Bug572315 {\n" +
            "	private List<String> \n" +
            "	@Deprecated \n" +
            "	private void test(){ \n" +
            "	} \n" +
            "}");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "List<String> ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    assertResults(
            "list[VARIABLE_DECLARATION]{list, null, Ljava.util.List<Ljava.lang.String;>;, list, null, 48}\n"
            + "strings[VARIABLE_DECLARATION]{strings, null, Ljava.util.List<Ljava.lang.String;>;, strings, null, 48}",
            requestor.getResults());
}

public void testBug572315_OnFieldAboveParameterizedAnnotatedMember_VariableNameSuggestion() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/Bug570593.java",
            "import java.util.List;\n" +
            "\n" +
            "public class Bug572315 {\n" +
            "	private List<String> \n" +
            "	@SuppressWarnings({\"unchecked\"}) \n" +
            "	private void test(){ \n" +
            "	} \n" +
            "}");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "List<String> ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    assertResults(
            "list[VARIABLE_DECLARATION]{list, null, Ljava.util.List<Ljava.lang.String;>;, list, null, 48}\n"
            + "strings[VARIABLE_DECLARATION]{strings, null, Ljava.util.List<Ljava.lang.String;>;, strings, null, 48}",
            requestor.getResults());
}

public void testBug572315_OnField_VariableNameSuggestion() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/Bug570593.java",
            "import java.util.List;\n" +
            "\n" +
            "public class Bug572315 {\n" +
            "	private List<String> \n" +
            "}");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "List<String> ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    assertResults(
            "list[VARIABLE_DECLARATION]{list, null, Ljava.util.List<Ljava.lang.String;>;, list, null, 48}\n"
            + "strings[VARIABLE_DECLARATION]{strings, null, Ljava.util.List<Ljava.lang.String;>;, strings, null, 48}",
            requestor.getResults());
}

public void testBug572315_OnFieldAboveMember_VariableNameSuggestion() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/Bug570593.java",
            "import java.util.List;\n" +
            "\n" +
            "public class Bug572315 {\n" +
            "	private List<String> \n" +
            "	private int count;" +
            "}");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "List<String> ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    assertResults(
            "list[VARIABLE_DECLARATION]{list, null, Ljava.util.List<Ljava.lang.String;>;, list, null, 48}\n"
            + "strings[VARIABLE_DECLARATION]{strings, null, Ljava.util.List<Ljava.lang.String;>;, strings, null, 48}",
            requestor.getResults());
}

public void testBug572315_OnFieldAboveAnnotatedField_VariableNameSuggestion() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/Bug570593.java",
            "import java.util.List;\n" +
            "\n" +
            "public class Bug572315 {\n" +
            "	private List<String> \n" +
            "	@Deprecated \n" +
            "	private int count;" +
            "}");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "List<String> ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    assertResults(
            "list[VARIABLE_DECLARATION]{list, null, Ljava.util.List<Ljava.lang.String;>;, list, null, 48}\n"
            + "strings[VARIABLE_DECLARATION]{strings, null, Ljava.util.List<Ljava.lang.String;>;, strings, null, 48}",
            requestor.getResults());
}

public void testBug572315_OnFieldAboveAnnotatedMemberWithSemicolon_VariableNameSuggestion() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/Bug570593.java",
            "import java.util.List;\n" +
            "\n" +
            "public class Bug572315 {\n" +
            "	private List<String> ;\n" +
            "	@Deprecated \n" +
            "	private int count;" +
            "}");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "List<String> ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    assertResults(
            "list[VARIABLE_DECLARATION]{list, null, Ljava.util.List<Ljava.lang.String;>;, list, null, 48}\n"
            + "strings[VARIABLE_DECLARATION]{strings, null, Ljava.util.List<Ljava.lang.String;>;, strings, null, 48}",
            requestor.getResults());
}

public void testBug572315_OnFieldAboveAnnotatedMemberWhichIsAnnotated_VariableNameSuggestion() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/Bug570593.java",
            "import java.util.List;\n" +
            "\n" +
            "public class Bug572315 {\n" +
            "	@Deprecated \n" +
            "	private List<String> \n" +
            "	@Deprecated \n" +
            "	private int count;" +
            "}");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "List<String> ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    assertResults(
            "list[VARIABLE_DECLARATION]{list, null, Ljava.util.List<Ljava.lang.String;>;, list, null, 48}\n"
            + "strings[VARIABLE_DECLARATION]{strings, null, Ljava.util.List<Ljava.lang.String;>;, strings, null, 48}",
            requestor.getResults());
}

public void testBug572315_OnLocalVariableAboveAnnotatedMember_VariableNameSuggestion() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/Bug570593.java",
            "import java.util.List;\n" +
            "\n" +
            "public class Bug572315 {\n" +
            "   private void foo() {\n" +
            "   List<String> \n" +
            "   @Deprecated()\n" +
            "   Integer age;\n" +
            "   }\n" +
            "}");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "List<String> ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    assertResults(
            "list[VARIABLE_DECLARATION]{list, null, Ljava.util.List<Ljava.lang.String;>;, list, null, 48}\n"
            + "strings[VARIABLE_DECLARATION]{strings, null, Ljava.util.List<Ljava.lang.String;>;, strings, null, 48}",
            requestor.getResults());
}
public void testBug530556() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"Completion/src/Callback.java",
		"@FunctionalInterface\n" +
		"public interface Callback<P,R> {\n" +
		"    public R call(P param);\n" +
		"}\n");
	this.workingCopies[0] = getWorkingCopy(
		"Completion/src/EnumLambdaFreeze.java",
		"public enum EnumLambdaFreeze {\n" +
		"	k1( s_arg -> {\n" +
		"		// freezes as soon as i'm typing a dot after s_arg\n" +
		"		s_arg.\n" +
		"		return( \"\" );\n" +
		"	}, s_arg -> {\n" +
		"		return( \"\" );\n" +
		"	} ),\n" +
		"	k2( s_arg -> s_arg, s_arg -> s_arg );\n" +
		"	\n" +
		"	private EnumLambdaFreeze( Callback<String, String> callback1, \n" +
		"                                  Callback<String, String> callback2 ){ }\n" +
		"}\n");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "s_arg.";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    assertResults(
            "CASE_INSENSITIVE_ORDER[FIELD_REF]{CASE_INSENSITIVE_ORDER, Ljava.lang.String;, Ljava.util.Comparator<Ljava.lang.String;>;, CASE_INSENSITIVE_ORDER, null, 49}\n" +
            "copyValueOf[METHOD_REF]{copyValueOf(), Ljava.lang.String;, ([C)Ljava.lang.String;, copyValueOf, (arg0), 49}\n" +
            "copyValueOf[METHOD_REF]{copyValueOf(), Ljava.lang.String;, ([CII)Ljava.lang.String;, copyValueOf, (arg0, arg1, arg2), 49}\n" +
            "format[METHOD_REF]{format(), Ljava.lang.String;, (Ljava.lang.String;[Ljava.lang.Object;)Ljava.lang.String;, format, (arg0, arg1), 49}\n" +
            "format[METHOD_REF]{format(), Ljava.lang.String;, (Ljava.util.Locale;Ljava.lang.String;[Ljava.lang.Object;)Ljava.lang.String;, format, (arg0, arg1, arg2), 49}\n" +
            "join[METHOD_REF]{join(), Ljava.lang.String;, (Ljava.lang.CharSequence;Ljava.lang.Iterable<+Ljava.lang.CharSequence;>;)Ljava.lang.String;, join, (arg0, arg1), 49}\n" +
            "join[METHOD_REF]{join(), Ljava.lang.String;, (Ljava.lang.CharSequence;[Ljava.lang.CharSequence;)Ljava.lang.String;, join, (arg0, arg1), 49}\n" +
            "valueOf[METHOD_REF]{valueOf(), Ljava.lang.String;, (C)Ljava.lang.String;, valueOf, (arg0), 49}\n" +
            "valueOf[METHOD_REF]{valueOf(), Ljava.lang.String;, (D)Ljava.lang.String;, valueOf, (arg0), 49}\n" +
            "valueOf[METHOD_REF]{valueOf(), Ljava.lang.String;, (F)Ljava.lang.String;, valueOf, (arg0), 49}\n" +
            "valueOf[METHOD_REF]{valueOf(), Ljava.lang.String;, (I)Ljava.lang.String;, valueOf, (arg0), 49}\n" +
            "valueOf[METHOD_REF]{valueOf(), Ljava.lang.String;, (J)Ljava.lang.String;, valueOf, (arg0), 49}\n" +
            "valueOf[METHOD_REF]{valueOf(), Ljava.lang.String;, (Ljava.lang.Object;)Ljava.lang.String;, valueOf, (arg0), 49}\n" +
            "valueOf[METHOD_REF]{valueOf(), Ljava.lang.String;, (Z)Ljava.lang.String;, valueOf, (arg0), 49}\n" +
            "valueOf[METHOD_REF]{valueOf(), Ljava.lang.String;, ([C)Ljava.lang.String;, valueOf, (arg0), 49}\n" +
            "valueOf[METHOD_REF]{valueOf(), Ljava.lang.String;, ([CII)Ljava.lang.String;, valueOf, (arg0, arg1, arg2), 49}\n" +
            "charAt[METHOD_REF]{charAt(), Ljava.lang.String;, (I)C, charAt, (arg0), 60}\n" +
            "chars[METHOD_REF]{chars(), Ljava.lang.CharSequence;, ()Ljava.util.stream.IntStream;, chars, null, 60}\n" +
            "clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, 60}\n" +
            "codePointAt[METHOD_REF]{codePointAt(), Ljava.lang.String;, (I)I, codePointAt, (arg0), 60}\n" +
            "codePointBefore[METHOD_REF]{codePointBefore(), Ljava.lang.String;, (I)I, codePointBefore, (arg0), 60}\n" +
            "codePointCount[METHOD_REF]{codePointCount(), Ljava.lang.String;, (II)I, codePointCount, (arg0, arg1), 60}\n" +
            "codePoints[METHOD_REF]{codePoints(), Ljava.lang.CharSequence;, ()Ljava.util.stream.IntStream;, codePoints, null, 60}\n" +
            "compareTo[METHOD_REF]{compareTo(), Ljava.lang.String;, (Ljava.lang.String;)I, compareTo, (arg0), 60}\n" +
            "compareToIgnoreCase[METHOD_REF]{compareToIgnoreCase(), Ljava.lang.String;, (Ljava.lang.String;)I, compareToIgnoreCase, (arg0), 60}\n" +
            "concat[METHOD_REF]{concat(), Ljava.lang.String;, (Ljava.lang.String;)Ljava.lang.String;, concat, (arg0), 60}\n" +
            "contains[METHOD_REF]{contains(), Ljava.lang.String;, (Ljava.lang.CharSequence;)Z, contains, (arg0), 60}\n" +
            "contentEquals[METHOD_REF]{contentEquals(), Ljava.lang.String;, (Ljava.lang.CharSequence;)Z, contentEquals, (arg0), 60}\n" +
            "contentEquals[METHOD_REF]{contentEquals(), Ljava.lang.String;, (Ljava.lang.StringBuffer;)Z, contentEquals, (arg0), 60}\n" +
            "endsWith[METHOD_REF]{endsWith(), Ljava.lang.String;, (Ljava.lang.String;)Z, endsWith, (arg0), 60}\n" +
            "equals[METHOD_REF]{equals(), Ljava.lang.String;, (Ljava.lang.Object;)Z, equals, (arg0), 60}\n" +
            "equalsIgnoreCase[METHOD_REF]{equalsIgnoreCase(), Ljava.lang.String;, (Ljava.lang.String;)Z, equalsIgnoreCase, (arg0), 60}\n" +
            "finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, 60}\n" +
            "getBytes[METHOD_REF]{getBytes(), Ljava.lang.String;, ()[B, getBytes, null, 60}\n" +
            "getBytes[METHOD_REF]{getBytes(), Ljava.lang.String;, (II[BI)V, getBytes, (arg0, arg1, arg2, arg3), 60}\n" +
            "getBytes[METHOD_REF]{getBytes(), Ljava.lang.String;, (Ljava.lang.String;)[B, getBytes, (arg0), 60}\n" +
            "getBytes[METHOD_REF]{getBytes(), Ljava.lang.String;, (Ljava.nio.charset.Charset;)[B, getBytes, (arg0), 60}\n" +
            "getChars[METHOD_REF]{getChars(), Ljava.lang.String;, (II[CI)V, getChars, (arg0, arg1, arg2, arg3), 60}\n" +
            "getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<*>;, getClass, null, 60}\n" +
            "hashCode[METHOD_REF]{hashCode(), Ljava.lang.String;, ()I, hashCode, null, 60}\n" +
            "indexOf[METHOD_REF]{indexOf(), Ljava.lang.String;, (I)I, indexOf, (arg0), 60}\n" +
            "indexOf[METHOD_REF]{indexOf(), Ljava.lang.String;, (II)I, indexOf, (arg0, arg1), 60}\n" +
            "indexOf[METHOD_REF]{indexOf(), Ljava.lang.String;, (Ljava.lang.String;)I, indexOf, (arg0), 60}\n" +
            "indexOf[METHOD_REF]{indexOf(), Ljava.lang.String;, (Ljava.lang.String;I)I, indexOf, (arg0, arg1), 60}\n" +
            "intern[METHOD_REF]{intern(), Ljava.lang.String;, ()Ljava.lang.String;, intern, null, 60}\n" +
            "isEmpty[METHOD_REF]{isEmpty(), Ljava.lang.String;, ()Z, isEmpty, null, 60}\n" +
            "lastIndexOf[METHOD_REF]{lastIndexOf(), Ljava.lang.String;, (I)I, lastIndexOf, (arg0), 60}\n" +
            "lastIndexOf[METHOD_REF]{lastIndexOf(), Ljava.lang.String;, (II)I, lastIndexOf, (arg0, arg1), 60}\n" +
            "lastIndexOf[METHOD_REF]{lastIndexOf(), Ljava.lang.String;, (Ljava.lang.String;)I, lastIndexOf, (arg0), 60}\n" +
            "lastIndexOf[METHOD_REF]{lastIndexOf(), Ljava.lang.String;, (Ljava.lang.String;I)I, lastIndexOf, (arg0, arg1), 60}\n" +
            "length[METHOD_REF]{length(), Ljava.lang.String;, ()I, length, null, 60}\n" +
            "matches[METHOD_REF]{matches(), Ljava.lang.String;, (Ljava.lang.String;)Z, matches, (arg0), 60}\n" +
            "notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, 60}\n" +
            "notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, 60}\n" +
            "offsetByCodePoints[METHOD_REF]{offsetByCodePoints(), Ljava.lang.String;, (II)I, offsetByCodePoints, (arg0, arg1), 60}\n" +
            "regionMatches[METHOD_REF]{regionMatches(), Ljava.lang.String;, (ILjava.lang.String;II)Z, regionMatches, (arg0, arg1, arg2, arg3), 60}\n" +
            "regionMatches[METHOD_REF]{regionMatches(), Ljava.lang.String;, (ZILjava.lang.String;II)Z, regionMatches, (arg0, arg1, arg2, arg3, arg4), 60}\n" +
            "replace[METHOD_REF]{replace(), Ljava.lang.String;, (CC)Ljava.lang.String;, replace, (arg0, arg1), 60}\n" +
            "replace[METHOD_REF]{replace(), Ljava.lang.String;, (Ljava.lang.CharSequence;Ljava.lang.CharSequence;)Ljava.lang.String;, replace, (arg0, arg1), 60}\n" +
            "replaceAll[METHOD_REF]{replaceAll(), Ljava.lang.String;, (Ljava.lang.String;Ljava.lang.String;)Ljava.lang.String;, replaceAll, (arg0, arg1), 60}\n" +
            "replaceFirst[METHOD_REF]{replaceFirst(), Ljava.lang.String;, (Ljava.lang.String;Ljava.lang.String;)Ljava.lang.String;, replaceFirst, (arg0, arg1), 60}\n" +
            "split[METHOD_REF]{split(), Ljava.lang.String;, (Ljava.lang.String;)[Ljava.lang.String;, split, (arg0), 60}\n" +
            "split[METHOD_REF]{split(), Ljava.lang.String;, (Ljava.lang.String;I)[Ljava.lang.String;, split, (arg0, arg1), 60}\n" +
            "startsWith[METHOD_REF]{startsWith(), Ljava.lang.String;, (Ljava.lang.String;)Z, startsWith, (arg0), 60}\n" +
            "startsWith[METHOD_REF]{startsWith(), Ljava.lang.String;, (Ljava.lang.String;I)Z, startsWith, (arg0, arg1), 60}\n" +
            "subSequence[METHOD_REF]{subSequence(), Ljava.lang.String;, (II)Ljava.lang.CharSequence;, subSequence, (arg0, arg1), 60}\n" +
            "substring[METHOD_REF]{substring(), Ljava.lang.String;, (I)Ljava.lang.String;, substring, (arg0), 60}\n" +
            "substring[METHOD_REF]{substring(), Ljava.lang.String;, (II)Ljava.lang.String;, substring, (arg0, arg1), 60}\n" +
            "toCharArray[METHOD_REF]{toCharArray(), Ljava.lang.String;, ()[C, toCharArray, null, 60}\n" +
            "toLowerCase[METHOD_REF]{toLowerCase(), Ljava.lang.String;, ()Ljava.lang.String;, toLowerCase, null, 60}\n" +
            "toLowerCase[METHOD_REF]{toLowerCase(), Ljava.lang.String;, (Ljava.util.Locale;)Ljava.lang.String;, toLowerCase, (arg0), 60}\n" +
            "toString[METHOD_REF]{toString(), Ljava.lang.String;, ()Ljava.lang.String;, toString, null, 60}\n" +
            "toUpperCase[METHOD_REF]{toUpperCase(), Ljava.lang.String;, ()Ljava.lang.String;, toUpperCase, null, 60}\n" +
            "toUpperCase[METHOD_REF]{toUpperCase(), Ljava.lang.String;, (Ljava.util.Locale;)Ljava.lang.String;, toUpperCase, (arg0), 60}\n" +
            "trim[METHOD_REF]{trim(), Ljava.lang.String;, ()Ljava.lang.String;, trim, null, 60}\n" +
            "wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, 60}\n" +
            "wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, wait, (millis), 60}\n" +
            "wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), 60}",
            requestor.getResults());
}
public void testBug539685a() throws Exception {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"Completion/src/ReproduceHang.java",
		"import java.util.Objects;\n" +
		"import java.util.function.Function;\n" +
		"\n" +
		"public class ReproduceHang {\n" +
		"    public static void main(String[] args) {\n" +
		"        Function<String, Object> localVar = (value -> new Object() {\n" +
		"            private final int i = Objects.requireNull(1);\n" +
		"        });\n" +
		"    }\n" +
		"}\n");
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "Objects.r";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    assertResults(
            "requireNonNull[METHOD_REF]{requireNonNull, Ljava.util.Objects;, <T:Ljava.lang.Object;>(TT;)TT;, requireNonNull, (arg0), 51}\n" +
            "requireNonNull[METHOD_REF]{requireNonNull, Ljava.util.Objects;, <T:Ljava.lang.Object;>(TT;Ljava.lang.String;)TT;, requireNonNull, (arg0, arg1), 51}\n" +
            "requireNonNull[METHOD_REF]{requireNonNull, Ljava.util.Objects;, <T:Ljava.lang.Object;>(TT;Ljava.util.function.Supplier<Ljava.lang.String;>;)TT;, requireNonNull, (arg0, arg1), 51}",
            requestor.getResults());
}
public void testBug558530() throws Exception {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/Bug558530.java",
            "import java.util.function.Function;\n" +
            "\n" +
            "public class LambdaCrash {\n" +
            "\n" +
            "    public enum Problem {\n" +
            "        ONE(s -> s.trim())\n" +
            "        TWO(k -> k." +
            "        ;\n" +
            "\n" +
            "        private final Function<String, String> function;\n" +
            "\n" +
            "        private Problem(Function<String, String> function) {\n" +
            "            this.function = function;\n" +
            "        }\n" +
            "\n" +
            "    }\n" +
            "\n" +
            "}");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "k -> k.";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    assertResults(
            "CASE_INSENSITIVE_ORDER[FIELD_REF]{CASE_INSENSITIVE_ORDER, Ljava.lang.String;, Ljava.util.Comparator<Ljava.lang.String;>;, CASE_INSENSITIVE_ORDER, null, 49}\n" +
            "finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, 55}\n" +
            "getBytes[METHOD_REF]{getBytes(), Ljava.lang.String;, (II[BI)V, getBytes, (arg0, arg1, arg2, arg3), 55}\n" +
            "getChars[METHOD_REF]{getChars(), Ljava.lang.String;, (II[CI)V, getChars, (arg0, arg1, arg2, arg3), 55}\n" +
            "notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, 55}\n" +
            "notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, 55}\n" +
            "wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, 55}\n" +
            "wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, wait, (millis), 55}\n" +
            "wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), 55}\n" +
            "charAt[METHOD_REF]{charAt(), Ljava.lang.String;, (I)C, charAt, (arg0), 60}\n" +
            "chars[METHOD_REF]{chars(), Ljava.lang.CharSequence;, ()Ljava.util.stream.IntStream;, chars, null, 60}\n" +
            "clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, 60}\n" +
            "codePointAt[METHOD_REF]{codePointAt(), Ljava.lang.String;, (I)I, codePointAt, (arg0), 60}\n" +
            "codePointBefore[METHOD_REF]{codePointBefore(), Ljava.lang.String;, (I)I, codePointBefore, (arg0), 60}\n" +
            "codePointCount[METHOD_REF]{codePointCount(), Ljava.lang.String;, (II)I, codePointCount, (arg0, arg1), 60}\n" +
            "codePoints[METHOD_REF]{codePoints(), Ljava.lang.CharSequence;, ()Ljava.util.stream.IntStream;, codePoints, null, 60}\n" +
            "compareTo[METHOD_REF]{compareTo(), Ljava.lang.String;, (Ljava.lang.String;)I, compareTo, (arg0), 60}\n" +
            "compareToIgnoreCase[METHOD_REF]{compareToIgnoreCase(), Ljava.lang.String;, (Ljava.lang.String;)I, compareToIgnoreCase, (arg0), 60}\n" +
            "contains[METHOD_REF]{contains(), Ljava.lang.String;, (Ljava.lang.CharSequence;)Z, contains, (arg0), 60}\n" +
            "contentEquals[METHOD_REF]{contentEquals(), Ljava.lang.String;, (Ljava.lang.CharSequence;)Z, contentEquals, (arg0), 60}\n" +
            "contentEquals[METHOD_REF]{contentEquals(), Ljava.lang.String;, (Ljava.lang.StringBuffer;)Z, contentEquals, (arg0), 60}\n" +
            "endsWith[METHOD_REF]{endsWith(), Ljava.lang.String;, (Ljava.lang.String;)Z, endsWith, (arg0), 60}\n" +
            "equals[METHOD_REF]{equals(), Ljava.lang.String;, (Ljava.lang.Object;)Z, equals, (arg0), 60}\n" +
            "equalsIgnoreCase[METHOD_REF]{equalsIgnoreCase(), Ljava.lang.String;, (Ljava.lang.String;)Z, equalsIgnoreCase, (arg0), 60}\n" +
            "getBytes[METHOD_REF]{getBytes(), Ljava.lang.String;, ()[B, getBytes, null, 60}\n" +
            "getBytes[METHOD_REF]{getBytes(), Ljava.lang.String;, (Ljava.lang.String;)[B, getBytes, (arg0), 60}\n" +
            "getBytes[METHOD_REF]{getBytes(), Ljava.lang.String;, (Ljava.nio.charset.Charset;)[B, getBytes, (arg0), 60}\n" +
            "getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<*>;, getClass, null, 60}\n" +
            "hashCode[METHOD_REF]{hashCode(), Ljava.lang.String;, ()I, hashCode, null, 60}\n" +
            "indexOf[METHOD_REF]{indexOf(), Ljava.lang.String;, (I)I, indexOf, (arg0), 60}\n" +
            "indexOf[METHOD_REF]{indexOf(), Ljava.lang.String;, (II)I, indexOf, (arg0, arg1), 60}\n" +
            "indexOf[METHOD_REF]{indexOf(), Ljava.lang.String;, (Ljava.lang.String;)I, indexOf, (arg0), 60}\n" +
            "indexOf[METHOD_REF]{indexOf(), Ljava.lang.String;, (Ljava.lang.String;I)I, indexOf, (arg0, arg1), 60}\n" +
            "isEmpty[METHOD_REF]{isEmpty(), Ljava.lang.String;, ()Z, isEmpty, null, 60}\n" +
            "lastIndexOf[METHOD_REF]{lastIndexOf(), Ljava.lang.String;, (I)I, lastIndexOf, (arg0), 60}\n" +
            "lastIndexOf[METHOD_REF]{lastIndexOf(), Ljava.lang.String;, (II)I, lastIndexOf, (arg0, arg1), 60}\n" +
            "lastIndexOf[METHOD_REF]{lastIndexOf(), Ljava.lang.String;, (Ljava.lang.String;)I, lastIndexOf, (arg0), 60}\n" +
            "lastIndexOf[METHOD_REF]{lastIndexOf(), Ljava.lang.String;, (Ljava.lang.String;I)I, lastIndexOf, (arg0, arg1), 60}\n" +
            "length[METHOD_REF]{length(), Ljava.lang.String;, ()I, length, null, 60}\n" +
            "matches[METHOD_REF]{matches(), Ljava.lang.String;, (Ljava.lang.String;)Z, matches, (arg0), 60}\n" +
            "offsetByCodePoints[METHOD_REF]{offsetByCodePoints(), Ljava.lang.String;, (II)I, offsetByCodePoints, (arg0, arg1), 60}\n" +
            "regionMatches[METHOD_REF]{regionMatches(), Ljava.lang.String;, (ILjava.lang.String;II)Z, regionMatches, (arg0, arg1, arg2, arg3), 60}\n" +
            "regionMatches[METHOD_REF]{regionMatches(), Ljava.lang.String;, (ZILjava.lang.String;II)Z, regionMatches, (arg0, arg1, arg2, arg3, arg4), 60}\n" +
            "split[METHOD_REF]{split(), Ljava.lang.String;, (Ljava.lang.String;)[Ljava.lang.String;, split, (arg0), 60}\n" +
            "split[METHOD_REF]{split(), Ljava.lang.String;, (Ljava.lang.String;I)[Ljava.lang.String;, split, (arg0, arg1), 60}\n" +
            "startsWith[METHOD_REF]{startsWith(), Ljava.lang.String;, (Ljava.lang.String;)Z, startsWith, (arg0), 60}\n" +
            "startsWith[METHOD_REF]{startsWith(), Ljava.lang.String;, (Ljava.lang.String;I)Z, startsWith, (arg0, arg1), 60}\n" +
            "subSequence[METHOD_REF]{subSequence(), Ljava.lang.String;, (II)Ljava.lang.CharSequence;, subSequence, (arg0, arg1), 60}\n" +
            "toCharArray[METHOD_REF]{toCharArray(), Ljava.lang.String;, ()[C, toCharArray, null, 60}\n" +
            "copyValueOf[METHOD_REF]{copyValueOf(), Ljava.lang.String;, ([C)Ljava.lang.String;, copyValueOf, (arg0), 79}\n" +
            "copyValueOf[METHOD_REF]{copyValueOf(), Ljava.lang.String;, ([CII)Ljava.lang.String;, copyValueOf, (arg0, arg1, arg2), 79}\n" +
            "format[METHOD_REF]{format(), Ljava.lang.String;, (Ljava.lang.String;[Ljava.lang.Object;)Ljava.lang.String;, format, (arg0, arg1), 79}\n" +
            "format[METHOD_REF]{format(), Ljava.lang.String;, (Ljava.util.Locale;Ljava.lang.String;[Ljava.lang.Object;)Ljava.lang.String;, format, (arg0, arg1, arg2), 79}\n" +
            "join[METHOD_REF]{join(), Ljava.lang.String;, (Ljava.lang.CharSequence;Ljava.lang.Iterable<+Ljava.lang.CharSequence;>;)Ljava.lang.String;, join, (arg0, arg1), 79}\n" +
            "join[METHOD_REF]{join(), Ljava.lang.String;, (Ljava.lang.CharSequence;[Ljava.lang.CharSequence;)Ljava.lang.String;, join, (arg0, arg1), 79}\n" +
            "valueOf[METHOD_REF]{valueOf(), Ljava.lang.String;, (C)Ljava.lang.String;, valueOf, (arg0), 79}\n" +
            "valueOf[METHOD_REF]{valueOf(), Ljava.lang.String;, (D)Ljava.lang.String;, valueOf, (arg0), 79}\n" +
            "valueOf[METHOD_REF]{valueOf(), Ljava.lang.String;, (F)Ljava.lang.String;, valueOf, (arg0), 79}\n" +
            "valueOf[METHOD_REF]{valueOf(), Ljava.lang.String;, (I)Ljava.lang.String;, valueOf, (arg0), 79}\n" +
            "valueOf[METHOD_REF]{valueOf(), Ljava.lang.String;, (J)Ljava.lang.String;, valueOf, (arg0), 79}\n" +
            "valueOf[METHOD_REF]{valueOf(), Ljava.lang.String;, (Ljava.lang.Object;)Ljava.lang.String;, valueOf, (arg0), 79}\n" +
            "valueOf[METHOD_REF]{valueOf(), Ljava.lang.String;, (Z)Ljava.lang.String;, valueOf, (arg0), 79}\n" +
            "valueOf[METHOD_REF]{valueOf(), Ljava.lang.String;, ([C)Ljava.lang.String;, valueOf, (arg0), 79}\n" +
            "valueOf[METHOD_REF]{valueOf(), Ljava.lang.String;, ([CII)Ljava.lang.String;, valueOf, (arg0, arg1, arg2), 79}\n" +
            "concat[METHOD_REF]{concat(), Ljava.lang.String;, (Ljava.lang.String;)Ljava.lang.String;, concat, (arg0), 90}\n" +
            "intern[METHOD_REF]{intern(), Ljava.lang.String;, ()Ljava.lang.String;, intern, null, 90}\n" +
            "replace[METHOD_REF]{replace(), Ljava.lang.String;, (CC)Ljava.lang.String;, replace, (arg0, arg1), 90}\n" +
            "replace[METHOD_REF]{replace(), Ljava.lang.String;, (Ljava.lang.CharSequence;Ljava.lang.CharSequence;)Ljava.lang.String;, replace, (arg0, arg1), 90}\n" +
            "replaceAll[METHOD_REF]{replaceAll(), Ljava.lang.String;, (Ljava.lang.String;Ljava.lang.String;)Ljava.lang.String;, replaceAll, (arg0, arg1), 90}\n" +
            "replaceFirst[METHOD_REF]{replaceFirst(), Ljava.lang.String;, (Ljava.lang.String;Ljava.lang.String;)Ljava.lang.String;, replaceFirst, (arg0, arg1), 90}\n" +
            "substring[METHOD_REF]{substring(), Ljava.lang.String;, (I)Ljava.lang.String;, substring, (arg0), 90}\n" +
            "substring[METHOD_REF]{substring(), Ljava.lang.String;, (II)Ljava.lang.String;, substring, (arg0, arg1), 90}\n" +
            "toLowerCase[METHOD_REF]{toLowerCase(), Ljava.lang.String;, ()Ljava.lang.String;, toLowerCase, null, 90}\n" +
            "toLowerCase[METHOD_REF]{toLowerCase(), Ljava.lang.String;, (Ljava.util.Locale;)Ljava.lang.String;, toLowerCase, (arg0), 90}\n" +
            "toString[METHOD_REF]{toString(), Ljava.lang.String;, ()Ljava.lang.String;, toString, null, 90}\n" +
            "toUpperCase[METHOD_REF]{toUpperCase(), Ljava.lang.String;, ()Ljava.lang.String;, toUpperCase, null, 90}\n" +
            "toUpperCase[METHOD_REF]{toUpperCase(), Ljava.lang.String;, (Ljava.util.Locale;)Ljava.lang.String;, toUpperCase, (arg0), 90}\n" +
            "trim[METHOD_REF]{trim(), Ljava.lang.String;, ()Ljava.lang.String;, trim, null, 90}",
            requestor.getResults());
}
public void testBug548779() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/test/Test.java",
            "package test;\n" +
            "\n" +
            "public class Test {\n" +
            "	String val = \"\";\n" +
            "	{\n" +
            "//		val.match\n" +
            "	}\n" +
            "}\n" +
            "\n" +
            "interface ConditionChecker {\n" +
            "	boolean check(String line);\n" +
            "}\n" +
            "\n" +
            "enum MyGuesser {\n" +
            "	INT_LONG(\"INT_LONG\", (line) -> {\n" +
            "		return line.contains(\"int\");\n" +
            "	}, (line) -> {\n" +
            "		return line.contains(\"long\");\n" +
            "	});\n" +
            "\n" +
            "	String name;\n" +
            "	ConditionChecker checker;\n" +
            "	ConditionChecker checkerOld;\n" +
            "\n" +
            "	MyGuesser(String name, ConditionChecker checker, ConditionChecker checkerOld) {\n" +
            "		this.name = name;\n" +
            "		this.checker = checker;\n" +
            "		this.checkerOld = checkerOld;\n" +
            "	}\n" +
            "}\n");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "line.";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    assertResults(
            "copyValueOf[METHOD_REF]{copyValueOf, Ljava.lang.String;, ([C)Ljava.lang.String;, copyValueOf, (arg0), 49}\n" +
            "copyValueOf[METHOD_REF]{copyValueOf, Ljava.lang.String;, ([CII)Ljava.lang.String;, copyValueOf, (arg0, arg1, arg2), 49}\n" +
            "format[METHOD_REF]{format, Ljava.lang.String;, (Ljava.lang.String;[Ljava.lang.Object;)Ljava.lang.String;, format, (arg0, arg1), 49}\n" +
            "format[METHOD_REF]{format, Ljava.lang.String;, (Ljava.util.Locale;Ljava.lang.String;[Ljava.lang.Object;)Ljava.lang.String;, format, (arg0, arg1, arg2), 49}\n" +
            "join[METHOD_REF]{join, Ljava.lang.String;, (Ljava.lang.CharSequence;Ljava.lang.Iterable<+Ljava.lang.CharSequence;>;)Ljava.lang.String;, join, (arg0, arg1), 49}\n" +
            "join[METHOD_REF]{join, Ljava.lang.String;, (Ljava.lang.CharSequence;[Ljava.lang.CharSequence;)Ljava.lang.String;, join, (arg0, arg1), 49}\n" +
            "valueOf[METHOD_REF]{valueOf, Ljava.lang.String;, (C)Ljava.lang.String;, valueOf, (arg0), 49}\n" +
            "valueOf[METHOD_REF]{valueOf, Ljava.lang.String;, (D)Ljava.lang.String;, valueOf, (arg0), 49}\n" +
            "valueOf[METHOD_REF]{valueOf, Ljava.lang.String;, (F)Ljava.lang.String;, valueOf, (arg0), 49}\n" +
            "valueOf[METHOD_REF]{valueOf, Ljava.lang.String;, (I)Ljava.lang.String;, valueOf, (arg0), 49}\n" +
            "valueOf[METHOD_REF]{valueOf, Ljava.lang.String;, (J)Ljava.lang.String;, valueOf, (arg0), 49}\n" +
            "valueOf[METHOD_REF]{valueOf, Ljava.lang.String;, (Ljava.lang.Object;)Ljava.lang.String;, valueOf, (arg0), 49}\n" +
            "valueOf[METHOD_REF]{valueOf, Ljava.lang.String;, (Z)Ljava.lang.String;, valueOf, (arg0), 49}\n" +
            "valueOf[METHOD_REF]{valueOf, Ljava.lang.String;, ([C)Ljava.lang.String;, valueOf, (arg0), 49}\n" +
            "valueOf[METHOD_REF]{valueOf, Ljava.lang.String;, ([CII)Ljava.lang.String;, valueOf, (arg0, arg1, arg2), 49}\n" +
            "finalize[METHOD_REF]{finalize, Ljava.lang.Object;, ()V, finalize, null, 55}\n" +
            "getBytes[METHOD_REF]{getBytes, Ljava.lang.String;, (II[BI)V, getBytes, (arg0, arg1, arg2, arg3), 55}\n" +
            "getChars[METHOD_REF]{getChars, Ljava.lang.String;, (II[CI)V, getChars, (arg0, arg1, arg2, arg3), 55}\n" +
            "notify[METHOD_REF]{notify, Ljava.lang.Object;, ()V, notify, null, 55}\n" +
            "notifyAll[METHOD_REF]{notifyAll, Ljava.lang.Object;, ()V, notifyAll, null, 55}\n" +
            "wait[METHOD_REF]{wait, Ljava.lang.Object;, ()V, wait, null, 55}\n" +
            "wait[METHOD_REF]{wait, Ljava.lang.Object;, (J)V, wait, (millis), 55}\n" +
            "wait[METHOD_REF]{wait, Ljava.lang.Object;, (JI)V, wait, (millis, nanos), 55}\n" +
            "charAt[METHOD_REF]{charAt, Ljava.lang.String;, (I)C, charAt, (arg0), 60}\n" +
            "chars[METHOD_REF]{chars, Ljava.lang.CharSequence;, ()Ljava.util.stream.IntStream;, chars, null, 60}\n" +
            "clone[METHOD_REF]{clone, Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, 60}\n" +
            "codePointAt[METHOD_REF]{codePointAt, Ljava.lang.String;, (I)I, codePointAt, (arg0), 60}\n" +
            "codePointBefore[METHOD_REF]{codePointBefore, Ljava.lang.String;, (I)I, codePointBefore, (arg0), 60}\n" +
            "codePointCount[METHOD_REF]{codePointCount, Ljava.lang.String;, (II)I, codePointCount, (arg0, arg1), 60}\n" +
            "codePoints[METHOD_REF]{codePoints, Ljava.lang.CharSequence;, ()Ljava.util.stream.IntStream;, codePoints, null, 60}\n" +
            "compareTo[METHOD_REF]{compareTo, Ljava.lang.String;, (Ljava.lang.String;)I, compareTo, (arg0), 60}\n" +
            "compareToIgnoreCase[METHOD_REF]{compareToIgnoreCase, Ljava.lang.String;, (Ljava.lang.String;)I, compareToIgnoreCase, (arg0), 60}\n" +
            "concat[METHOD_REF]{concat, Ljava.lang.String;, (Ljava.lang.String;)Ljava.lang.String;, concat, (arg0), 60}\n" +
            "getBytes[METHOD_REF]{getBytes, Ljava.lang.String;, ()[B, getBytes, null, 60}\n" +
            "getBytes[METHOD_REF]{getBytes, Ljava.lang.String;, (Ljava.lang.String;)[B, getBytes, (arg0), 60}\n" +
            "getBytes[METHOD_REF]{getBytes, Ljava.lang.String;, (Ljava.nio.charset.Charset;)[B, getBytes, (arg0), 60}\n" +
            "getClass[METHOD_REF]{getClass, Ljava.lang.Object;, ()Ljava.lang.Class<*>;, getClass, null, 60}\n" +
            "hashCode[METHOD_REF]{hashCode, Ljava.lang.String;, ()I, hashCode, null, 60}\n" +
            "indexOf[METHOD_REF]{indexOf, Ljava.lang.String;, (I)I, indexOf, (arg0), 60}\n" +
            "indexOf[METHOD_REF]{indexOf, Ljava.lang.String;, (II)I, indexOf, (arg0, arg1), 60}\n" +
            "indexOf[METHOD_REF]{indexOf, Ljava.lang.String;, (Ljava.lang.String;)I, indexOf, (arg0), 60}\n" +
            "indexOf[METHOD_REF]{indexOf, Ljava.lang.String;, (Ljava.lang.String;I)I, indexOf, (arg0, arg1), 60}\n" +
            "intern[METHOD_REF]{intern, Ljava.lang.String;, ()Ljava.lang.String;, intern, null, 60}\n" +
            "lastIndexOf[METHOD_REF]{lastIndexOf, Ljava.lang.String;, (I)I, lastIndexOf, (arg0), 60}\n" +
            "lastIndexOf[METHOD_REF]{lastIndexOf, Ljava.lang.String;, (II)I, lastIndexOf, (arg0, arg1), 60}\n" +
            "lastIndexOf[METHOD_REF]{lastIndexOf, Ljava.lang.String;, (Ljava.lang.String;)I, lastIndexOf, (arg0), 60}\n" +
            "lastIndexOf[METHOD_REF]{lastIndexOf, Ljava.lang.String;, (Ljava.lang.String;I)I, lastIndexOf, (arg0, arg1), 60}\n" +
            "length[METHOD_REF]{length, Ljava.lang.String;, ()I, length, null, 60}\n" +
            "offsetByCodePoints[METHOD_REF]{offsetByCodePoints, Ljava.lang.String;, (II)I, offsetByCodePoints, (arg0, arg1), 60}\n" +
            "replace[METHOD_REF]{replace, Ljava.lang.String;, (CC)Ljava.lang.String;, replace, (arg0, arg1), 60}\n" +
            "replace[METHOD_REF]{replace, Ljava.lang.String;, (Ljava.lang.CharSequence;Ljava.lang.CharSequence;)Ljava.lang.String;, replace, (arg0, arg1), 60}\n" +
            "replaceAll[METHOD_REF]{replaceAll, Ljava.lang.String;, (Ljava.lang.String;Ljava.lang.String;)Ljava.lang.String;, replaceAll, (arg0, arg1), 60}\n" +
            "replaceFirst[METHOD_REF]{replaceFirst, Ljava.lang.String;, (Ljava.lang.String;Ljava.lang.String;)Ljava.lang.String;, replaceFirst, (arg0, arg1), 60}\n" +
            "split[METHOD_REF]{split, Ljava.lang.String;, (Ljava.lang.String;)[Ljava.lang.String;, split, (arg0), 60}\n" +
            "split[METHOD_REF]{split, Ljava.lang.String;, (Ljava.lang.String;I)[Ljava.lang.String;, split, (arg0, arg1), 60}\n" +
            "subSequence[METHOD_REF]{subSequence, Ljava.lang.String;, (II)Ljava.lang.CharSequence;, subSequence, (arg0, arg1), 60}\n" +
            "substring[METHOD_REF]{substring, Ljava.lang.String;, (I)Ljava.lang.String;, substring, (arg0), 60}\n" +
            "substring[METHOD_REF]{substring, Ljava.lang.String;, (II)Ljava.lang.String;, substring, (arg0, arg1), 60}\n" +
            "toCharArray[METHOD_REF]{toCharArray, Ljava.lang.String;, ()[C, toCharArray, null, 60}\n" +
            "toLowerCase[METHOD_REF]{toLowerCase, Ljava.lang.String;, ()Ljava.lang.String;, toLowerCase, null, 60}\n" +
            "toLowerCase[METHOD_REF]{toLowerCase, Ljava.lang.String;, (Ljava.util.Locale;)Ljava.lang.String;, toLowerCase, (arg0), 60}\n" +
            "toString[METHOD_REF]{toString, Ljava.lang.String;, ()Ljava.lang.String;, toString, null, 60}\n" +
            "toUpperCase[METHOD_REF]{toUpperCase, Ljava.lang.String;, ()Ljava.lang.String;, toUpperCase, null, 60}\n" +
            "toUpperCase[METHOD_REF]{toUpperCase, Ljava.lang.String;, (Ljava.util.Locale;)Ljava.lang.String;, toUpperCase, (arg0), 60}\n" +
            "trim[METHOD_REF]{trim, Ljava.lang.String;, ()Ljava.lang.String;, trim, null, 60}\n" +
            "contains[METHOD_REF]{contains, Ljava.lang.String;, (Ljava.lang.CharSequence;)Z, contains, (arg0), 90}\n" +
            "contentEquals[METHOD_REF]{contentEquals, Ljava.lang.String;, (Ljava.lang.CharSequence;)Z, contentEquals, (arg0), 90}\n" +
            "contentEquals[METHOD_REF]{contentEquals, Ljava.lang.String;, (Ljava.lang.StringBuffer;)Z, contentEquals, (arg0), 90}\n" +
            "endsWith[METHOD_REF]{endsWith, Ljava.lang.String;, (Ljava.lang.String;)Z, endsWith, (arg0), 90}\n" +
            "equals[METHOD_REF]{equals, Ljava.lang.String;, (Ljava.lang.Object;)Z, equals, (arg0), 90}\n" +
            "equalsIgnoreCase[METHOD_REF]{equalsIgnoreCase, Ljava.lang.String;, (Ljava.lang.String;)Z, equalsIgnoreCase, (arg0), 90}\n" +
            "isEmpty[METHOD_REF]{isEmpty, Ljava.lang.String;, ()Z, isEmpty, null, 90}\n" +
            "matches[METHOD_REF]{matches, Ljava.lang.String;, (Ljava.lang.String;)Z, matches, (arg0), 90}\n" +
            "regionMatches[METHOD_REF]{regionMatches, Ljava.lang.String;, (ILjava.lang.String;II)Z, regionMatches, (arg0, arg1, arg2, arg3), 90}\n" +
            "regionMatches[METHOD_REF]{regionMatches, Ljava.lang.String;, (ZILjava.lang.String;II)Z, regionMatches, (arg0, arg1, arg2, arg3, arg4), 90}\n" +
            "startsWith[METHOD_REF]{startsWith, Ljava.lang.String;, (Ljava.lang.String;)Z, startsWith, (arg0), 90}\n" +
            "startsWith[METHOD_REF]{startsWith, Ljava.lang.String;, (Ljava.lang.String;I)Z, startsWith, (arg0, arg1), 90}",
    		requestor.getResults());
}
public void testBug543617() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/test/Test.java",
            "package test.module;\n" +
            "\n" +
            "import java.util.Collections;\n" +
            "import java.util.Iterator;\n" +
            "import java.util.List;\n" +
            "\n" +
            "public class TestApp {\n" +
            "	private <E> void print(Iterator<E> iterator) {\n" +
            "                // doesn't shows chain proposals\n" +
            "		iterator.forEachRemaining(e -> load(C1)); \n" +
            "\n" +
            "		this.load(C2); \n" +
            "	}\n" +
            "\n" +
            "	public List<String> findAll() {\n" +
            "		return load(Collections.EMPTY_LIST);\n" +
            "	}\n" +
            "\n" +
            "	public List<String> load(List<Long> ids) {\n" +
            "		return null;\n" +
            "	}\n" +
            "}\n");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "e -> load(";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    int normalRelevance = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED;
    int voidRelevance = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED + R_VOID;
    int expectedTypeRelevance = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED + R_EXACT_EXPECTED_TYPE;

    assertResults(
            "finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, "+voidRelevance+"}\n" +
            "notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, "+voidRelevance+"}\n" +
            "notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, "+voidRelevance+"}\n" +
            "print[METHOD_REF]{print(), Ltest.TestApp;, <E:Ljava.lang.Object;>(Ljava.util.Iterator<TE;>;)V, print, (iterator), "+voidRelevance+"}\n" +
            "wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, "+voidRelevance+"}\n" +
            "wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, wait, (millis), "+voidRelevance+"}\n" +
            "wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), "+voidRelevance+"}\n" +
            "E[TYPE_REF]{E, null, TE;, null, null, "+normalRelevance+"}\n" +
            "TestApp[TYPE_REF]{TestApp, test, Ltest.TestApp;, null, null, "+normalRelevance+"}\n" +
            "clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, "+normalRelevance+"}\n" +
            "e[LOCAL_VARIABLE_REF]{e, null, TE;, e, null, "+normalRelevance+"}\n" +
            "equals[METHOD_REF]{equals(), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), "+normalRelevance+"}\n" +
            "getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<*>;, getClass, null, "+normalRelevance+"}\n" +
            "hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, "+normalRelevance+"}\n" +
            "iterator[LOCAL_VARIABLE_REF]{iterator, null, Ljava.util.Iterator<TE;>;, iterator, null, "+normalRelevance+"}\n" +
            "toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, "+normalRelevance+"}\n" +
            "List<java.lang.Long>[TYPE_REF]{List, java.util, Ljava.util.List<Ljava.lang.Long;>;, null, null, "+expectedTypeRelevance+"}\n" +
            "findAll[METHOD_REF]{findAll(), Ltest.TestApp;, ()Ljava.util.List<Ljava.lang.String;>;, findAll, null, "+expectedTypeRelevance+"}\n" +
            "load[METHOD_REF]{load(), Ltest.TestApp;, (Ljava.util.List<Ljava.lang.Long;>;)Ljava.util.List<Ljava.lang.String;>;, load, (ids), "+expectedTypeRelevance+"}",
    		requestor.getResults());
}
public void testBug539617_alloc() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/Completion/src/CodeCompletion.java",
			"public class CodeCompletion {\n" +
			"	public static void main(String[] args) {\n" +
			"		new Thread( () -> {\n" +
			"			Double d = new Double(\n" +
			"		});\n" +
			"	}\n" +
			"}\n");
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "new Double(";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	int relevance = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED;
	assertResults(
			"Double[METHOD_REF<CONSTRUCTOR>]{, Ljava.lang.Double;, (D)V, Double, (arg0), "+relevance+"}\n" +
			"Double[METHOD_REF<CONSTRUCTOR>]{, Ljava.lang.Double;, (Ljava.lang.String;)V, Double, (arg0), "+relevance+"}",
			requestor.getResults());
}
public void testBug539617_msg() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/Completion/src/CodeCompletion.java",
			"public class CodeCompletion {\n" +
			"	public static void main(String[] args) {\n" +
			"		new Thread( () -> {\n" +
			"			Double d = meth(\n" +
			"		});\n" +
			"	}\n" +
			"	static Double meth(String arg) { return null; }\n" +
			"	static Number meth(String arg, boolean flag) { return null; }\n" +
			"}\n");
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "meth(";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	int relevance =  R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_UNQUALIFIED + R_NON_RESTRICTED;
	int relevanceExpectedType = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_UNQUALIFIED + R_NON_RESTRICTED + R_EXACT_EXPECTED_TYPE;
	assertResults(
			"meth[METHOD_REF]{, LCodeCompletion;, (Ljava.lang.String;Z)Ljava.lang.Number;, meth, (arg, flag), "+relevance+"}\n" +
			"meth[METHOD_REF]{, LCodeCompletion;, (Ljava.lang.String;)Ljava.lang.Double;, meth, (arg), "+relevanceExpectedType+"}",
			requestor.getResults());
}
public void testBug473654_comment33() throws Exception {
	this.workingCopies = new ICompilationUnit[4];
	this.workingCopies[0] = getWorkingCopy("/Completion/src/GenericThing.java",
			"public class GenericThing {}\n");
	this.workingCopies[1] = getWorkingCopy("/Completion/src/SpecificThing.java",
			"public class SpecificThing extends GenericThing {}\n");
	this.workingCopies[2] = getWorkingCopy("/Completion/src/CodeCompletion.java",
			"import java.util.function.Supplier;\n" +
			"public class TestCase<S extends GenericThing> {\n" +
			"	TestCase(Supplier<S> s) {}\n" +
			"}\n");
	this.workingCopies[3] = getWorkingCopy("/Completion/src/Test.java",
			"public class Test extends TestCase<SpecificThing> {\n" +
			"	private final Foo foo;\n" +
			"	public Test(Foo foo, Bar bar) {\n" +
			"		super(() -> new SpecificThing(foo, bar) {\n" +
			"				// press Ctrl+Space before the comment\n" +
			"		});\n" +
			"		this.foo = foo;\n" +
			"	}\n" +
			"}\n");
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[3].getSource();
	String completeBefore = "// press Ctrl+Space before the comment";
	int cursorLocation = str.indexOf(completeBefore);
	this.workingCopies[3].codeComplete(cursorLocation, requestor, this.wcOwner);
	int newMethodRelevance =  R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED;
	int keywordRelevance = newMethodRelevance + R_CASE;
	int fieldTypeRelevance =  keywordRelevance + R_UNQUALIFIED;
	int overrideRelevance = keywordRelevance + R_METHOD_OVERIDE;
	assertResults(
			"[POTENTIAL_METHOD_DECLARATION]{, LSpecificThing;, ()V, , null, "+newMethodRelevance+"}\n" +
			"abstract[KEYWORD]{abstract, null, null, abstract, null, "+keywordRelevance+"}\n" +
			"class[KEYWORD]{class, null, null, class, null, "+keywordRelevance+"}\n" +
			"enum[KEYWORD]{enum, null, null, enum, null, "+keywordRelevance+"}\n" +
			"final[KEYWORD]{final, null, null, final, null, "+keywordRelevance+"}\n" +
			"interface[KEYWORD]{interface, null, null, interface, null, "+keywordRelevance+"}\n" +
			"native[KEYWORD]{native, null, null, native, null, "+keywordRelevance+"}\n" +
			"private[KEYWORD]{private, null, null, private, null, "+keywordRelevance+"}\n" +
			"protected[KEYWORD]{protected, null, null, protected, null, "+keywordRelevance+"}\n" +
			"public[KEYWORD]{public, null, null, public, null, "+keywordRelevance+"}\n" +
			"static[KEYWORD]{static, null, null, static, null, "+keywordRelevance+"}\n" +
			"strictfp[KEYWORD]{strictfp, null, null, strictfp, null, "+keywordRelevance+"}\n" +
			"synchronized[KEYWORD]{synchronized, null, null, synchronized, null, "+keywordRelevance+"}\n" +
			"transient[KEYWORD]{transient, null, null, transient, null, "+keywordRelevance+"}\n" +
			"volatile[KEYWORD]{volatile, null, null, volatile, null, "+keywordRelevance+"}\n" +
			"Test[TYPE_REF]{Test, , LTest;, null, null, "+fieldTypeRelevance+"}\n" +
			"clone[METHOD_DECLARATION]{protected Object clone() throws CloneNotSupportedException, Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, "+overrideRelevance+"}\n" +
			"equals[METHOD_DECLARATION]{public boolean equals(Object obj), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), "+overrideRelevance+"}\n" +
			"finalize[METHOD_DECLARATION]{protected void finalize() throws Throwable, Ljava.lang.Object;, ()V, finalize, null, "+overrideRelevance+"}\n" +
			"hashCode[METHOD_DECLARATION]{public int hashCode(), Ljava.lang.Object;, ()I, hashCode, null, "+overrideRelevance+"}\n" +
			"toString[METHOD_DECLARATION]{public String toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, "+overrideRelevance+"}",
			requestor.getResults());
}
public void testBug546097() throws Exception {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/TestClass.java",
            "import java.util.concurrent.Callable;\n" +
            "\n" +
            "\n" +
            "public class TestClass {\n" +
            "	public enum TestEnum {\n" +
            "		ENUM1(() -> ),\n" +
            "		ENUM2(() -> );\n" +
            "\n" +
            "		private Callable<Object> callable;\n" +
            "\n" +
            "		private TestEnum(Callable<Object> callable) {\n" +
            "		{\n" +
            "			this.callable = callable;\n" +
            "		}\n" +
            "	}\n" +
            "}\n");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "ENUM1(() -> ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    int expectedTypeRelevance = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED + R_EXPECTED_TYPE;
    int exactExpectedTypeRelevance = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED + R_EXACT_EXPECTED_TYPE;

    assertResults(
            "ENUM1[FIELD_REF]{ENUM1, LTestClass$TestEnum;, LTestClass$TestEnum;, ENUM1, null, "+expectedTypeRelevance+"}\n" +
            "ENUM2[FIELD_REF]{ENUM2, LTestClass$TestEnum;, LTestClass$TestEnum;, ENUM2, null, "+expectedTypeRelevance+"}\n" +
            "TestClass[TYPE_REF]{TestClass, , LTestClass;, null, null, "+expectedTypeRelevance+"}\n" +
            "valueOf[METHOD_REF]{valueOf(), LTestClass$TestEnum;, (Ljava.lang.String;)LTestClass$TestEnum;, valueOf, (arg0), "+expectedTypeRelevance+"}\n" +
            "valueOf[METHOD_REF]{valueOf(), Ljava.lang.Enum<LTestClass$TestEnum;>;, <T:Ljava.lang.Enum<TT;>;>(Ljava.lang.Class<TT;>;Ljava.lang.String;)TT;, valueOf, (arg0, arg1), "+expectedTypeRelevance+"}\n" +
            "values[METHOD_REF]{values(), LTestClass$TestEnum;, ()[LTestClass$TestEnum;, values, null, "+expectedTypeRelevance+"}\n" +
            "Object[TYPE_REF]{Object, java.lang, Ljava.lang.Object;, null, null, "+exactExpectedTypeRelevance+"}",
    		requestor.getResults());
}
public void testBug573105_OnLambdaParamAtNestedMethodInvocationInsideLambda_MemberCompletions() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/Bug573105.java",
            "import java.util.stream.Stream;\n" +
            "import java.nio.file.Files;\n" +
            "import java.nio.file.Paths;\n" +
            "\n" +
            "public class Bug573105 {\n" +
            "	private void test(){ \n" +
            "		Stream.of(new Element()).map(element -> Files.lines(Paths.get(element.)))\n"+
            "	} \n" +
            "	private class Element {\n"+
            "		public java.net.URI foo(){return null;}\n"+
            "	}\n"+
            "}");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "element.";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    String result = requestor.getResults();
    assertTrue(String.format("Result doesn''t contain method foo (%s)", result),
    		result.contains("foo[METHOD_REF]{foo(), LBug573105$Element;, ()Ljava.net.URI;, foo, null, 60}\n"));
}

public void testBug573105_OnLambdaParamAtMethodInvocationInsideLambda_MemberCompletions() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/Bug573105.java",
            "import java.util.stream.Stream;\n" +
            "import java.nio.file.Paths;\n" +
            "\n" +
            "public class Bug573105 {\n" +
            "	private void test(){ \n" +
            "		Stream.of(new Element()).map(element -> Paths.get(element.))\n"+
            "	} \n" +
            "	private class Element {\n"+
            "		public java.net.URI foo(){return null;}\n"+
            "	}\n"+
            "}");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "element.";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    String result = requestor.getResults();
    assertTrue(String.format("Result doesn''t contain method foo (%s)", result),
    		result.contains("foo[METHOD_REF]{foo(), LBug573105$Element;, ()Ljava.net.URI;, foo, null, 60}\n"));
}
public void testBug573105_OnLambdaParamAtMethodInvocationInsideLambdaWithSemicolon_MemberCompletions() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/Bug573105.java",
            "import java.util.stream.Stream;\n" +
            "import java.nio.file.Files;\n" +
            "import java.nio.file.Paths;\n" +
            "\n" +
            "public class Bug573105 {\n" +
            "	private void test(){ \n" +
            "		Stream.of(new Element()).map(element -> Files.lines(Paths.get(element.)));\n"+
            "	} \n" +
            "	private class Element {\n"+
            "		public java.net.URI foo(){return null;}\n"+
            "	}\n"+
            "}");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "element.";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    String result = requestor.getResults();
    assertTrue(String.format("Result doesn''t contain method foo (%s)", result),
    		result.contains("foo[METHOD_REF]{foo(), LBug573105$Element;, ()Ljava.net.URI;, foo, null, 60}\n"));
}

public void testBug573105_OnLambdaParamAtMethodInvocationInsideLambdaBlock_MemberCompletions() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/Bug573105.java",
            "import java.util.stream.Stream;\n" +
            "import java.nio.file.Files;\n" +
            "import java.nio.file.Paths;\n" +
            "\n" +
            "public class Bug573105 {\n" +
            "	private void test(){ \n" +
            "		Stream.of(new Element()).map(element -> {" +
            "			return Files.lines(Paths.get(element.)))\n" +
            "		}"+
            "	} \n" +
            "	private class Element {\n"+
            "		public java.net.URI foo(){return null;}\n"+
            "	}\n"+
            "}");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "element.";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    String result = requestor.getResults();
    assertTrue(String.format("Result doesn''t contain method foo (%s)", result),
    		result.contains("foo[METHOD_REF]{foo(), LBug573105$Element;, ()Ljava.net.URI;, foo, null, 60}\n"));
}
public void testBug573105_OnLambdaParamAtMethodInvocationInsideLambdaInConditional_MemberCompletions() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/Bug573105.java",
            "import java.util.stream.Stream;\n" +
            "import java.nio.file.Paths;\n" +
            "\n" +
            "public class Bug573105 {\n" +
            "	private void test(boolean flag){ \n" +
            "		Stream.of(new Element()).map(flag ? null : element -> Paths.get(element.))\n"+
            "	} \n" +
            "	private class Element {\n"+
            "		public java.net.URI foo(){return null;}\n"+
            "	}\n"+
            "}");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "element.";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    String result = requestor.getResults();
    assertTrue(String.format("Result doesn''t contain method foo (%s)", result),
    		result.contains("foo[METHOD_REF]{foo(), LBug573105$Element;, ()Ljava.net.URI;, foo, null, 60}\n"));
}
public void testBug482663() throws Exception {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/CompletionParserResumeFailure.java",
			"import java.util.Map;\n" +
			"import java.util.stream.Stream;\n" +
			"class Path {}\n" +
			"public class CompletionParserResumeFailure\n" +
			"{\n" +
			"    Stream<Path> list(Path dir) throws IOException { return null; }\n" +
			"    public void freeze()\n" +
			"    {\n" +
			"        list(null).map(p -> new Object()\n" +
			"            {\n" +
			"                public String name = p.getFileName().toString();\n" +
			"                public Map<String, Date> clients = p;\n" +
			"            });\n" +
			"    }\n" +
			"}\n");
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "Date";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

	assertResults(
			"DateFormat[TYPE_REF]{java.text.DateFormat, java.text, Ljava.text.DateFormat;, null, null, 69}\n" +
			"DateFormatSymbols[TYPE_REF]{java.text.DateFormatSymbols, java.text, Ljava.text.DateFormatSymbols;, null, null, 69}\n" +
			"Date[TYPE_REF]{java.sql.Date, java.sql, Ljava.sql.Date;, null, null, 73}\n" +
			"Date[TYPE_REF]{java.util.Date, java.util, Ljava.util.Date;, null, null, 73}",
			requestor.getResults());
}
public void testBug574215() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/jdt/Something.java",
			"package jdt;\n" +
			"class S {\n" +
			"	void foo() {}\n" +
			"	String bar;\n" +
			"}\n" +
			"public class Something {\n" +
			"	private void test(S s, int i) {\n" +
			"		Runnable r = () -> {\n" +
			"			if (i > 2) {\n" +
			"				System.out.println(\"a\");\n" +
			"			} else {\n" +
			"				s. // <--\n" +
			"				System.out.println(\"b\");\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"}\n");
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.setAllowsRequiredProposals(CompletionProposal.FIELD_REF, CompletionProposal.TYPE_REF, true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "s.";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults(
			"bar[FIELD_REF]{bar, Ljdt.S;, Ljava.lang.String;, bar, null, 60}\n" +
			"clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, 60}\n" +
			"equals[METHOD_REF]{equals(), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), 60}\n" +
			"finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, 60}\n" +
			"foo[METHOD_REF]{foo(), Ljdt.S;, ()V, foo, null, 60}\n" +
			"getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<*>;, getClass, null, 60}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, 60}\n" +
			"notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, 60}\n" +
			"notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, 60}\n" +
			"toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, 60}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, 60}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, wait, (millis), 60}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), 60}",
			requestor.getResults());
}
public void testBug574215_withToken() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/jdt/Something.java",
			"package jdt;\n" +
			"class S {\n" +
			"	void foo() {}\n" +
			"	int found;\n" +
			"	String bar;\n" +
			"}\n" +
			"public class Something {\n" +
			"	private void test(S s, int i) {\n" +
			"		Runnable r = () -> {\n" +
			"			if (i > 2) {\n" +
			"				System.out.println(\"a\");\n" +
			"			} else {\n" +
			"				s.fo // <--\n" +
			"				System.out.println(\"b\");\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"}\n");
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.setAllowsRequiredProposals(CompletionProposal.FIELD_REF, CompletionProposal.TYPE_REF, true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "s.fo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults(
			"foo[METHOD_REF]{foo(), Ljdt.S;, ()V, foo, null, 60}\n" +
			"found[FIELD_REF]{found, Ljdt.S;, I, found, null, 60}",
			requestor.getResults());
}
public void testBug573313_MethodParametersCompletions_CorrectCompletionsForType() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/Bug573313.java",
            "import java.util.stream.Stream;\n" +
            "import java.util.ArrayList;\n" +
            "import java.util.concurrent.Callable;\n" +
            "import java.util.concurrent.TimeUnit;\n" +
            "\n" +
            "public class Bug573313 {\n" +
            "	private void test(){ \n" +
            "		foo(5, SE, null);\n"+
            "	} \n" +
            "	private void foo(int i, TimeUnit unit, Callable<String> callback) { \n" +
            "	} \n" +
            "}");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = ", SE";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

	assertResults("SECONDS[FIELD_REF]{TimeUnit.SECONDS, Ljava.util.concurrent.TimeUnit;, Ljava.util.concurrent.TimeUnit;, SECONDS, null, 104}",
			requestor.getResults());
}
public void testBug573313_MethodParametersCompletions_QualifiedName_CorrectCompletionsForType() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/Bug573313.java",
            "import java.util.stream.Stream;\n" +
            "import java.util.ArrayList;\n" +
            "import java.util.concurrent.Callable;\n" +
            "import java.util.concurrent.TimeUnit;\n" +
            "\n" +
            "public class Bug573313 {\n" +
            "	private void test(){ \n" +
            "		foo(5, TimeUnit.SE, null);\n"+
            "	} \n" +
            "	private void foo(int i, TimeUnit unit, Callable<String> callback) { \n" +
            "	} \n" +
            "}");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = ", TimeUnit.SE";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

	assertResults("SECONDS[FIELD_REF]{SECONDS, Ljava.util.concurrent.TimeUnit;, Ljava.util.concurrent.TimeUnit;, SECONDS, null, 81}",
			requestor.getResults());
}
public void testBug573313_MethodParametersCompletions_InCompleteMessageSend_CorrectCompletionsForType() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/Bug573313.java",
            "import java.util.stream.Stream;\n" +
            "import java.util.ArrayList;\n" +
            "import java.util.concurrent.Callable;\n" +
            "import java.util.concurrent.TimeUnit;\n" +
            "\n" +
            "public class Bug573313 {\n" +
            "	private void test(){ \n" +
            "		foo(5, SE);\n"+
            "	} \n" +
            "	private void foo(int i, TimeUnit unit, Callable<String> callback) { \n" +
            "	} \n" +
            "}");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = ", SE";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

	assertResults("SECONDS[FIELD_REF]{TimeUnit.SECONDS, Ljava.util.concurrent.TimeUnit;, Ljava.util.concurrent.TimeUnit;, SECONDS, null, 104}",
			requestor.getResults());
}

public void testBug573313_MethodParametersCompletions_InCompleteMessageSendOnLastParamWithToken_CorrectCompletionsForType() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/Bug573313.java",
            "import java.util.stream.Stream;\n" +
            "import java.util.ArrayList;\n" +
            "import java.util.concurrent.Callable;\n" +
            "import java.util.concurrent.TimeUnit;\n" +
            "\n" +
            "public class Bug573313 {\n" +
            "	private void test(){ \n" +
            "		foo(5, () -> \"call\", SE);\n"+
            "	} \n" +
            "	private void foo(int i, Callable<String> callback, TimeUnit unit) { \n" +
            "	} \n" +
            "}");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = ", SE";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

	assertResults("SECONDS[FIELD_REF]{TimeUnit.SECONDS, Ljava.util.concurrent.TimeUnit;, Ljava.util.concurrent.TimeUnit;, SECONDS, null, 104}",
			requestor.getResults());
}
public void testBug573313_MethodParametersCompletions_InCompleteMessageSendOnMiddleParam_CorrectCompletionsForType() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/Bug573313.java",
            "import java.util.stream.Stream;\n" +
            "import java.util.ArrayList;\n" +
            "import java.util.concurrent.Callable;\n" +
            "import java.util.concurrent.TimeUnit;\n" +
            "\n" +
            "public class Bug573313 {\n" +
            "	private void test(){ \n" +
            "		foo(5, , null);\n"+
            "	} \n" +
            "	private void foo(int i, TimeUnit unit, Callable<String> callback) { \n" +
            "	} \n" +
            "}");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "5, ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    String result = requestor.getResults();
	assertTrue(String.format("Result doesn''t contain enum literal SECONDS (%s)", result),
    		result.contains("SECONDS[FIELD_REF]{TimeUnit.SECONDS, Ljava.util.concurrent.TimeUnit;, Ljava.util.concurrent.TimeUnit;, SECONDS, null, 104}"));

}
public void testBug573313_MethodParametersCompletions_InCompleteMessageSendOnMiddleParamNoSpace_CorrectCompletionsForType() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/Bug573313.java",
            "import java.util.stream.Stream;\n" +
            "import java.util.ArrayList;\n" +
            "import java.util.concurrent.Callable;\n" +
            "import java.util.concurrent.TimeUnit;\n" +
            "\n" +
            "public class Bug573313 {\n" +
            "	private void test(){ \n" +
            "		foo(5,, null);\n"+
            "	} \n" +
            "	private void foo(int i, TimeUnit unit, Callable<String> callback) { \n" +
            "	} \n" +
            "}");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "5,";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    String result = requestor.getResults();
	assertTrue(String.format("Result doesn''t contain enum literal SECONDS (%s)", result),
    		result.contains("SECONDS[FIELD_REF]{TimeUnit.SECONDS, Ljava.util.concurrent.TimeUnit;, Ljava.util.concurrent.TimeUnit;, SECONDS, null, 104}"));

}
public void testBug573313_MethodParametersCompletions_InCompleteMessageSendOnLastParam_CorrectCompletionsForType() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/Bug573313.java",
            "import java.util.stream.Stream;\n" +
            "import java.util.ArrayList;\n" +
            "import java.util.concurrent.Callable;\n" +
            "import java.util.concurrent.TimeUnit;\n" +
            "\n" +
            "public class Bug573313 {\n" +
            "	private void test(){ \n" +
            "		foo(5,null,);\n"+
            "	} \n" +
            "	private void foo(int i, Callable<String> callback, TimeUnit unit) { \n" +
            "	} \n" +
            "}");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "5,null,";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    String result = requestor.getResults();
	assertTrue(String.format("Result doesn''t contain enum literal SECONDS (%s)", result),
    		result.contains("SECONDS[FIELD_REF]{TimeUnit.SECONDS, Ljava.util.concurrent.TimeUnit;, Ljava.util.concurrent.TimeUnit;, SECONDS, null, 104}"));

}
public void testBug573313_MethodParametersCompletions_InCompleteMessageSendOnMiddleParam_MethodCompletionsForType() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/Bug573313.java",
            "import java.util.stream.Stream;\n" +
            "import java.util.ArrayList;\n" +
            "import java.util.concurrent.Callable;\n" +
            "import java.util.concurrent.TimeUnit;\n" +
            "\n" +
            "public class Bug573313 {\n" +
            "	private void test(){ \n" +
            "		foo(5,defaultParam();\n"+
            "	} \n" +
            "	private void foo(int i, TimeUnit unit, Callable<String> callback) { \n" +
            "	} \n" +
            "	private TimeUnit defaultParam(int amout) {\n" +
            "		return null;" +
            "	}" +
            "	private Callable defaultParam() {\n" +
            "		return null;" +
            "	}" +
            "}");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "5,defaultParam(";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    String result = requestor.getResults();
	assertTrue(String.format("Result doesn''t contain enum literal SECONDS (%s)", result),
    		result.contains("defaultParam[METHOD_REF]{, LBug573313;, (I)Ljava.util.concurrent.TimeUnit;, defaultParam, (amout), 86}"));

}
public void testBug573313_MethodParametersCompletions_InCompleteMessageSendOnMiddleParam_MethodCompletionsForType_2() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/Bug573313.java",
            "import java.util.stream.Stream;\n" +
            "import java.util.ArrayList;\n" +
            "import java.util.concurrent.Callable;\n" +
            "import java.util.concurrent.TimeUnit;\n" +
            "\n" +
            "public class Bug573313 {\n" +
            "	private void test(){ \n" +
            "		foo(5,defaultParam(),null);\n"+
            "	} \n" +
            "	private void foo(int i, TimeUnit unit, Callable<String> callback) { \n" +
            "	} \n" +
            "	private TimeUnit defaultParam1(int amout) {\n" +
            "		return null;" +
            "	}" +
            "	private Callable defaultParam2() {\n" +
            "		return null;" +
            "	}" +
            "}");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "5,defaultParam";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    int relevance = R_DEFAULT + R_CASE + R_INTERESTING + R_NON_RESTRICTED + R_RESOLVED + R_NON_STATIC;
    assertEquals(
    		"defaultParam2[METHOD_REF]{defaultParam2, LBug573313;, ()Ljava.util.concurrent.Callable;, defaultParam2, null, "+relevance+"}\n" +
    		"defaultParam1[METHOD_REF]{defaultParam1, LBug573313;, (I)Ljava.util.concurrent.TimeUnit;, defaultParam1, (amout), "+(relevance + R_EXACT_EXPECTED_TYPE)+"}",
    		requestor.getResults());
}
public void testBug573789_atFirstChainMethodWithToken() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"Completion/src/App.java",
			"public class App {\n" +
			"	public static void main(String[] args) {\n"+
			"		(new StringBuilder()).append(1).append(2).toString();\n"+
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "(new StringBuilder()).app";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
    String result = requestor.getResults();
    assertTrue(String.format("Result doesn't match actual:  (%s)", result),
    		result.contains("appendCodePoint[METHOD_REF]{appendCodePoint, Ljava.lang.StringBuilder;, (I)Ljava.lang.StringBuilder;, appendCodePoint, (arg0), 60}"));
}
public void testBug573789_atFirstChainMethod() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"Completion/src/App.java",
			"public class App {\n" +
			"	public static void main(String[] args) {\n"+
			"		(new StringBuilder()).append(1).append(2).toString();\n"+
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "(new StringBuilder()).";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
    String result = requestor.getResults();
    assertTrue(String.format("Result doesn't match actual: (%s)", result),
    		result.contains("appendCodePoint[METHOD_REF]{appendCodePoint, Ljava.lang.StringBuilder;, (I)Ljava.lang.StringBuilder;, appendCodePoint, (arg0), 60}"));
}
public void testBug573789_atSecondChainMethodWithToken() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"Completion/src/App.java",
			"public class App {\n" +
			"	public static void main(String[] args) {\n"+
			"		(new StringBuilder()).append(1).append(2).toString();\n"+
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "append(1).app";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
    String result = requestor.getResults();
    assertTrue(String.format("Result doesn't match actual:  (%s)", result),
    		result.contains("appendCodePoint[METHOD_REF]{appendCodePoint, Ljava.lang.StringBuilder;, (I)Ljava.lang.StringBuilder;, appendCodePoint, (arg0), 60}"));
}
public void testBug573789_atFirstChainMethod_noBraces() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"Completion/src/App.java",
			"public class App {\n" +
			"	public static void main(String[] args) {\n"+
			"		new StringBuilder().append(1).append(2).toString();\n"+
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "new StringBuilder().";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
    String result = requestor.getResults();
    assertTrue(String.format("Result doesn't match actual: (%s)", result),
    		result.contains("appendCodePoint[METHOD_REF]{appendCodePoint, Ljava.lang.StringBuilder;, (I)Ljava.lang.StringBuilder;, appendCodePoint, (arg0), 60}"));
}
public void testBug573789_staticOnlyMethodCompletion() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"Completion/src/App.java",
			"public class App {\n" +
			"	public static void main(String[] args) {\n"+
			"		App.\n"+
			"	}\n" +
			"	" +
			"	public static App foo() {\n" +
			"		return null;\n" +
			"	}\n" +
			"	" +
			"	public static App boo() {\n" +
			"		return null;\n" +
			"	}\n" +
			"	" +
			"	public App moo() {\n" +
			"		return null;\n" +
			"	}\n" +
			"" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "App.";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
    String result = requestor.getResults();
    assertResults("boo[METHOD_REF]{boo(), LApp;, ()LApp;, boo, null, 51}\n"
    		+ "class[FIELD_REF]{class, null, Ljava.lang.Class<LApp;>;, class, null, 51}\n"
    		+ "foo[METHOD_REF]{foo(), LApp;, ()LApp;, foo, null, 51}\n"
    		+ "main[METHOD_REF]{main(), LApp;, ([Ljava.lang.String;)V, main, (args), 51}", result);
}
public void testBug573789_allMethodCompletion_withToken() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"Completion/src/App.java",
			"public class App {\n" +
			"	public static void main(String[] args) {\n"+
			"		App.xfoo().x\n"+
			"	}\n" +
			"	" +
			"	public static App xfoo() {\n" +
			"		return null;\n" +
			"	}\n" +
			"	" +
			"	public static App xboo() {\n" +
			"		return null;\n" +
			"	}\n" +
			"	" +
			"	public App xmoo() {\n" +
			"		return null;\n" +
			"	}\n" +
			"" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "App.xfoo().";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
    String result = requestor.getResults();
    assertTrue(String.format("Result doesn't match actual:  (%s)", result),
    		result.contains("xboo[METHOD_REF]{xboo(), LApp;, ()LApp;, xboo, null, 49}\n"
    				+ "xfoo[METHOD_REF]{xfoo(), LApp;, ()LApp;, xfoo, null, 49}\n"));

    assertTrue(String.format("Result doesn't match actual:  (%s)", result),
    		result.contains("xmoo[METHOD_REF]{xmoo(), LApp;, ()LApp;, xmoo, null, 60}"));
}
public void test574366_onParameterizedClassConstructor_insideLambda() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/Bug574366.java",
            "import java.util.ArrayList;\n" +
            "import java.util.Arrays;\n" +
			"public class Temp {\n"
			+ "    public static void main(String[] args) {\n"
			+ "    	Arrays.asList(1,2,3).stream()\n"
			+ "    		.map(i -> {\n"
			+ "    			return new ArrayList<>(1);\n"
			+ "    		}).toArray();\n"
			+ "    }\n"
			+ "}");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "new ArrayList<>(";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    String result = requestor.getResults();
	assertTrue(String.format("Result doesn''t contain ArrayList constructor (%s)", result),
    		result.contains("ArrayList<>[ANONYMOUS_CLASS_DECLARATION]{, Ljava.util.ArrayList<>;, (I)V, null, (arg0), 39}\n"
    				+ "ArrayList<>[ANONYMOUS_CLASS_DECLARATION]{, Ljava.util.ArrayList<>;, (Ljava.util.Collection<+TE;>;)V, null, (arg0), 39}"));
}
public void test574366_onParameterizedInterfaceConstructor_insideLambda() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/Bug574366.java",
            "import java.util.List;\n" +
            "import java.util.Arrays;\n" +
			"public class Temp {\n"
			+ "    public static void main(String[] args) {\n"
			+ "    	Arrays.asList(1,2,3).stream()\n"
			+ "    		.map(i -> {\n"
			+ "    			return new List<>(1);\n"
			+ "    		}).toArray();\n"
			+ "    }\n"
			+ "}");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "new List<>(";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    String result = requestor.getResults();
	assertTrue(String.format("Result doesn't contain List constructor (%s)", result),
    		result.contains("List<>[ANONYMOUS_CLASS_DECLARATION]{, Ljava.util.List<>;, ()V, null, null, 39}"));
}
public void test574366_onParameterizedClassConstructor_enclosedInstance() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/Bug574366.java",
			"public class Temp {\n"
			+ "    public void foo() {\n"
			+ "			Enclosed<String> list = new Temp().new Enclosed<>(1);"
			+ "    }\n"
			+ "	public class Enclosed<T> {"
			+ "		public Enclosed(int i){}\n"
			+ "	}\n"
			+ "}");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "new Temp().new Enclosed<>(";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    String result = requestor.getResults();
	assertTrue(String.format("Result doesn't contain expected constructor (%s)", result),
    		result.contains("Enclosed[METHOD_REF<CONSTRUCTOR>]{, LTemp$Enclosed<>;, (I)V, Enclosed, (i), 39}\n"
    				+ "Temp.Enclosed<>[ANONYMOUS_CLASS_DECLARATION]{, LTemp$Enclosed<>;, (I)V, null, (i), 39}"));
}
public void test574366_onParameterizedInterfaceConstructor_enclosedInstance() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/Bug574366.java",
			"public class Temp {\n"
			+ "    public void foo() {\n"
			+ "			Enclosed<String> list = new Temp().new Enclosed<>(1);"
			+ "    }\n"
			+ "	public interface Enclosed<T> {"
			+ "		public Enclosed(int i){}\n"
			+ "	}\n"
			+ "}");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "new Temp().new Enclosed<>(";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    assertResults(
    		"Temp.Enclosed<>[ANONYMOUS_CLASS_DECLARATION]{, LTemp$Enclosed<>;, ()V, null, null, 39}",
    		requestor.getResults());
}
public void testBug563020_lambdaWithMethodRef_overloadedMethodRef_expectCompletions() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/Bug563020.java",
			"import java.util.Arrays;\n"
			+ "public class Bug563020 {\n"
			+ "    public void foo() {\n"
			+ "			Arrays.asList(\"1\").stream().map(String::toUpperCase)."
			+ "    }\n"
			+ "}");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "map(String::toUpperCase).";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    String result = requestor.getResults();
	assertTrue(String.format("Result doesn't contain expected methods (%s)", result),
    		result.contains("iterator[METHOD_REF]{iterator(), Ljava.util.stream.BaseStream<Ljava.lang.String;Ljava.util.stream.Stream<Ljava.lang.String;>;>;, ()Ljava.util.Iterator<Ljava.lang.String;>;, iterator, null, 60}\n"
    				+ "limit[METHOD_REF]{limit(), Ljava.util.stream.Stream<Ljava.lang.String;>;, (J)Ljava.util.stream.Stream<Ljava.lang.String;>;, limit, (arg0), 60}\n"
    				+ "map[METHOD_REF]{map(), Ljava.util.stream.Stream<Ljava.lang.String;>;, <R:Ljava.lang.Object;>(Ljava.util.function.Function<-Ljava.lang.String;+TR;>;)Ljava.util.stream.Stream<TR;>;, map, (arg0), 60}\n"
    				+ "mapToDouble[METHOD_REF]{mapToDouble(), Ljava.util.stream.Stream<Ljava.lang.String;>;, (Ljava.util.function.ToDoubleFunction<-Ljava.lang.String;>;)Ljava.util.stream.DoubleStream;, mapToDouble, (arg0), 60}\n"
    				+ "mapToInt[METHOD_REF]{mapToInt(), Ljava.util.stream.Stream<Ljava.lang.String;>;, (Ljava.util.function.ToIntFunction<-Ljava.lang.String;>;)Ljava.util.stream.IntStream;, mapToInt, (arg0), 60}\n"
    				+ "mapToLong[METHOD_REF]{mapToLong(), Ljava.util.stream.Stream<Ljava.lang.String;>;, (Ljava.util.function.ToLongFunction<-Ljava.lang.String;>;)Ljava.util.stream.LongStream;, mapToLong, (arg0), 60}"));
}
public void testBug563020_lambdaWithMethodRef_exactMethodRef_expectCompletions() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/Bug563020.java",
			"import java.util.Arrays;\n"
			+ "public class Bug563020 {\n"
			+ "    public void foo() {\n"
			+ "			Arrays.asList(\"1\").stream().map(String::toString)."
			+ "    }\n"
			+ "}");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "map(String::toString).";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    String result = requestor.getResults();
	assertTrue(String.format("Result doesn't contain expected methods (%s)", result),
    		result.contains("iterator[METHOD_REF]{iterator(), Ljava.util.stream.BaseStream<Ljava.lang.String;Ljava.util.stream.Stream<Ljava.lang.String;>;>;, ()Ljava.util.Iterator<Ljava.lang.String;>;, iterator, null, 60}\n"
    				+ "limit[METHOD_REF]{limit(), Ljava.util.stream.Stream<Ljava.lang.String;>;, (J)Ljava.util.stream.Stream<Ljava.lang.String;>;, limit, (arg0), 60}\n"
    				+ "map[METHOD_REF]{map(), Ljava.util.stream.Stream<Ljava.lang.String;>;, <R:Ljava.lang.Object;>(Ljava.util.function.Function<-Ljava.lang.String;+TR;>;)Ljava.util.stream.Stream<TR;>;, map, (arg0), 60}\n"
    				+ "mapToDouble[METHOD_REF]{mapToDouble(), Ljava.util.stream.Stream<Ljava.lang.String;>;, (Ljava.util.function.ToDoubleFunction<-Ljava.lang.String;>;)Ljava.util.stream.DoubleStream;, mapToDouble, (arg0), 60}\n"
    				+ "mapToInt[METHOD_REF]{mapToInt(), Ljava.util.stream.Stream<Ljava.lang.String;>;, (Ljava.util.function.ToIntFunction<-Ljava.lang.String;>;)Ljava.util.stream.IntStream;, mapToInt, (arg0), 60}\n"
    				+ "mapToLong[METHOD_REF]{mapToLong(), Ljava.util.stream.Stream<Ljava.lang.String;>;, (Ljava.util.function.ToLongFunction<-Ljava.lang.String;>;)Ljava.util.stream.LongStream;, mapToLong, (arg0), 60}"));
}
public void testBug563020_lambdaWithMethodRef_overloadedMethodref_expectCompletionForNextChain() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/Bug563020.java",
			"import java.util.Arrays;\n"
			+ "public class Bug563020 {\n"
			+ "    public void foo() {\n"
			+ "			Arrays.asList(\"1\").stream().map(String::toUpperCase).sorted("
			+ "    }\n"
			+ "}");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "map(String::toUpperCase).sorted(";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    String result = requestor.getResults();
	assertTrue(String.format("Result doesn't contain expected methods (%s)", result),
    		result.contains("sorted[METHOD_REF]{, Ljava.util.stream.Stream<Ljava.lang.String;>;, ()Ljava.util.stream.Stream<Ljava.lang.String;>;, sorted, null, 56}\n"
    				+ "sorted[METHOD_REF]{, Ljava.util.stream.Stream<Ljava.lang.String;>;, (Ljava.util.Comparator<-Ljava.lang.String;>;)Ljava.util.stream.Stream<Ljava.lang.String;>;, sorted, (arg0), 56}"));
}
public void testBug563020_lambdaWithMethodRef_overloadedMethodref_expectCompletionForNextChainWithToken() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/Bug563020.java",
			"import java.util.Arrays;\n"
			+ "public class Bug563020 {\n"
			+ "    public void foo() {\n"
			+ "			Arrays.asList(\"1\").stream().map(String::toUpperCase).mapTo"
			+ "    }\n"
			+ "}");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "map(String::toUpperCase).mapTo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    String result = requestor.getResults();
	assertTrue(String.format("Result doesn't contain expected methods (%s)", result),
    		result.contains("mapToDouble[METHOD_REF]{mapToDouble(), Ljava.util.stream.Stream<Ljava.lang.String;>;, (Ljava.util.function.ToDoubleFunction<-Ljava.lang.String;>;)Ljava.util.stream.DoubleStream;, mapToDouble, (arg0), 60}\n"
    				+ "mapToInt[METHOD_REF]{mapToInt(), Ljava.util.stream.Stream<Ljava.lang.String;>;, (Ljava.util.function.ToIntFunction<-Ljava.lang.String;>;)Ljava.util.stream.IntStream;, mapToInt, (arg0), 60}\n"
    				+ "mapToLong[METHOD_REF]{mapToLong(), Ljava.util.stream.Stream<Ljava.lang.String;>;, (Ljava.util.function.ToLongFunction<-Ljava.lang.String;>;)Ljava.util.stream.LongStream;, mapToLong, (arg0), 60}"));
}
public void testBug563020_methodref_checkParserForBug559677_expectCompletions() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/Bug563020.java",
			"public class Bug563020 {\n" +
			"	private void myRun() {\n" +
			"	}\n" +
			"	private void myMethod(final Runnable r) {\n" +
			"	}\n" +
			"	public void test() {\n" +
			"		// second opening brace causes endless loop while saving\n" +
			"		myMethod((this::myRun);\n" +
			"	}\n" +
			"}\n"
			);

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "myMethod((this::";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    String result = requestor.getResults();
	assertTrue(String.format("Result doesn't contain expected methods (%s)", result),
    		result.contains("myRun[METHOD_NAME_REFERENCE]{myRun, LBug563020;, ()V, myRun, null, 55}"));
}
public void testBug574912() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"Completion/src/LambdaFreeze.java",
			"import java.util.Calendar;\n" +
			"import java.util.Date;\n" +
			"import java.util.function.Supplier;\n" +
			"\n" +
			"public class LambdaFreeze{\n" +
			"\n" +
			"   public static final Supplier<Date> SUPPLIER = () -> {\n" +
			"      Calendar calendar = Calendar.getInstance();\n" +
			"      calendar.set(Calendar., // try to autocomplete after the \".\" here freezes eclipse's main thread\n" +
			"                   0);\n" +
			"      return calendar.getTime();\n" +
			"   };\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "Calendar.";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	String result = requestor.getResults();
	assertResults("Calendar.Builder[TYPE_REF]{Builder, java.util, Ljava.util.Calendar$Builder;, null, null, 51}\n" +
			"class[FIELD_REF]{class, null, Ljava.lang.Class<Ljava.util.Calendar;>;, class, null, 51}\n" +
			"getAvailableCalendarTypes[METHOD_REF]{getAvailableCalendarTypes(), Ljava.util.Calendar;, ()Ljava.util.Set<Ljava.lang.String;>;, getAvailableCalendarTypes, null, 51}\n" +
			"getAvailableLocales[METHOD_REF]{getAvailableLocales(), Ljava.util.Calendar;, ()[Ljava.util.Locale;, getAvailableLocales, null, 51}\n" +
			"getInstance[METHOD_REF]{getInstance(), Ljava.util.Calendar;, ()Ljava.util.Calendar;, getInstance, null, 51}\n" +
			"getInstance[METHOD_REF]{getInstance(), Ljava.util.Calendar;, (Ljava.util.Locale;)Ljava.util.Calendar;, getInstance, (arg0), 51}\n" +
			"getInstance[METHOD_REF]{getInstance(), Ljava.util.Calendar;, (Ljava.util.TimeZone;)Ljava.util.Calendar;, getInstance, (arg0), 51}\n" +
			"getInstance[METHOD_REF]{getInstance(), Ljava.util.Calendar;, (Ljava.util.TimeZone;Ljava.util.Locale;)Ljava.util.Calendar;, getInstance, (arg0, arg1), 51}\n" +
			"ALL_STYLES[FIELD_REF]{ALL_STYLES, Ljava.util.Calendar;, I, ALL_STYLES, null, 81}\n" +
			"AM[FIELD_REF]{AM, Ljava.util.Calendar;, I, AM, null, 81}\n" +
			"AM_PM[FIELD_REF]{AM_PM, Ljava.util.Calendar;, I, AM_PM, null, 81}\n" +
			"APRIL[FIELD_REF]{APRIL, Ljava.util.Calendar;, I, APRIL, null, 81}\n" +
			"AUGUST[FIELD_REF]{AUGUST, Ljava.util.Calendar;, I, AUGUST, null, 81}\n" +
			"DATE[FIELD_REF]{DATE, Ljava.util.Calendar;, I, DATE, null, 81}\n" +
			"DAY_OF_MONTH[FIELD_REF]{DAY_OF_MONTH, Ljava.util.Calendar;, I, DAY_OF_MONTH, null, 81}\n" +
			"DAY_OF_WEEK[FIELD_REF]{DAY_OF_WEEK, Ljava.util.Calendar;, I, DAY_OF_WEEK, null, 81}\n" +
			"DAY_OF_WEEK_IN_MONTH[FIELD_REF]{DAY_OF_WEEK_IN_MONTH, Ljava.util.Calendar;, I, DAY_OF_WEEK_IN_MONTH, null, 81}\n" +
			"DAY_OF_YEAR[FIELD_REF]{DAY_OF_YEAR, Ljava.util.Calendar;, I, DAY_OF_YEAR, null, 81}\n" +
			"DECEMBER[FIELD_REF]{DECEMBER, Ljava.util.Calendar;, I, DECEMBER, null, 81}\n" +
			"DST_OFFSET[FIELD_REF]{DST_OFFSET, Ljava.util.Calendar;, I, DST_OFFSET, null, 81}\n" +
			"ERA[FIELD_REF]{ERA, Ljava.util.Calendar;, I, ERA, null, 81}\n" +
			"FEBRUARY[FIELD_REF]{FEBRUARY, Ljava.util.Calendar;, I, FEBRUARY, null, 81}\n" +
			"FIELD_COUNT[FIELD_REF]{FIELD_COUNT, Ljava.util.Calendar;, I, FIELD_COUNT, null, 81}\n" +
			"FRIDAY[FIELD_REF]{FRIDAY, Ljava.util.Calendar;, I, FRIDAY, null, 81}\n" +
			"HOUR[FIELD_REF]{HOUR, Ljava.util.Calendar;, I, HOUR, null, 81}\n" +
			"HOUR_OF_DAY[FIELD_REF]{HOUR_OF_DAY, Ljava.util.Calendar;, I, HOUR_OF_DAY, null, 81}\n" +
			"JANUARY[FIELD_REF]{JANUARY, Ljava.util.Calendar;, I, JANUARY, null, 81}\n" +
			"JULY[FIELD_REF]{JULY, Ljava.util.Calendar;, I, JULY, null, 81}\n" +
			"JUNE[FIELD_REF]{JUNE, Ljava.util.Calendar;, I, JUNE, null, 81}\n" +
			"LONG[FIELD_REF]{LONG, Ljava.util.Calendar;, I, LONG, null, 81}\n" +
			"LONG_FORMAT[FIELD_REF]{LONG_FORMAT, Ljava.util.Calendar;, I, LONG_FORMAT, null, 81}\n" +
			"LONG_STANDALONE[FIELD_REF]{LONG_STANDALONE, Ljava.util.Calendar;, I, LONG_STANDALONE, null, 81}\n" +
			"MARCH[FIELD_REF]{MARCH, Ljava.util.Calendar;, I, MARCH, null, 81}\n" +
			"MAY[FIELD_REF]{MAY, Ljava.util.Calendar;, I, MAY, null, 81}\n" +
			"MILLISECOND[FIELD_REF]{MILLISECOND, Ljava.util.Calendar;, I, MILLISECOND, null, 81}\n" +
			"MINUTE[FIELD_REF]{MINUTE, Ljava.util.Calendar;, I, MINUTE, null, 81}\n" +
			"MONDAY[FIELD_REF]{MONDAY, Ljava.util.Calendar;, I, MONDAY, null, 81}\n" +
			"MONTH[FIELD_REF]{MONTH, Ljava.util.Calendar;, I, MONTH, null, 81}\n" +
			"NARROW_FORMAT[FIELD_REF]{NARROW_FORMAT, Ljava.util.Calendar;, I, NARROW_FORMAT, null, 81}\n" +
			"NARROW_STANDALONE[FIELD_REF]{NARROW_STANDALONE, Ljava.util.Calendar;, I, NARROW_STANDALONE, null, 81}\n" +
			"NOVEMBER[FIELD_REF]{NOVEMBER, Ljava.util.Calendar;, I, NOVEMBER, null, 81}\n" +
			"OCTOBER[FIELD_REF]{OCTOBER, Ljava.util.Calendar;, I, OCTOBER, null, 81}\n" +
			"PM[FIELD_REF]{PM, Ljava.util.Calendar;, I, PM, null, 81}\n" +
			"SATURDAY[FIELD_REF]{SATURDAY, Ljava.util.Calendar;, I, SATURDAY, null, 81}\n" +
			"SECOND[FIELD_REF]{SECOND, Ljava.util.Calendar;, I, SECOND, null, 81}\n" +
			"SEPTEMBER[FIELD_REF]{SEPTEMBER, Ljava.util.Calendar;, I, SEPTEMBER, null, 81}\n" +
			"SHORT[FIELD_REF]{SHORT, Ljava.util.Calendar;, I, SHORT, null, 81}\n" +
			"SHORT_FORMAT[FIELD_REF]{SHORT_FORMAT, Ljava.util.Calendar;, I, SHORT_FORMAT, null, 81}\n" +
			"SHORT_STANDALONE[FIELD_REF]{SHORT_STANDALONE, Ljava.util.Calendar;, I, SHORT_STANDALONE, null, 81}\n" +
			"SUNDAY[FIELD_REF]{SUNDAY, Ljava.util.Calendar;, I, SUNDAY, null, 81}\n" +
			"THURSDAY[FIELD_REF]{THURSDAY, Ljava.util.Calendar;, I, THURSDAY, null, 81}\n" +
			"TUESDAY[FIELD_REF]{TUESDAY, Ljava.util.Calendar;, I, TUESDAY, null, 81}\n" +
			"UNDECIMBER[FIELD_REF]{UNDECIMBER, Ljava.util.Calendar;, I, UNDECIMBER, null, 81}\n" +
			"WEDNESDAY[FIELD_REF]{WEDNESDAY, Ljava.util.Calendar;, I, WEDNESDAY, null, 81}\n" +
			"WEEK_OF_MONTH[FIELD_REF]{WEEK_OF_MONTH, Ljava.util.Calendar;, I, WEEK_OF_MONTH, null, 81}\n" +
			"WEEK_OF_YEAR[FIELD_REF]{WEEK_OF_YEAR, Ljava.util.Calendar;, I, WEEK_OF_YEAR, null, 81}\n" +
			"YEAR[FIELD_REF]{YEAR, Ljava.util.Calendar;, I, YEAR, null, 81}\n" +
			"ZONE_OFFSET[FIELD_REF]{ZONE_OFFSET, Ljava.util.Calendar;, I, ZONE_OFFSET, null, 81}",
			result);
}
public void testBug574823_completeOn_methodInvocationWithParams_inIfConidtion_insideIfBlock_followedByChainedStatments() throws Exception {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/Bug574823.java",
			"import java.util.ArrayList;\n" +
			"public class Bug574823 {\n" +
			"	public void foo() {\n" +
			"		ArrayList<String> ints = new ArrayList<String>();\n" +
			"		if(ints.subList(1,1).) {\n" +
			"			String message = \"PASS\";\n" +
			"			System.out.println(message);\n" +
			"		}\n"+
			"	}\n" +
			"}\n"
			);
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, true, true, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "ints.subList(1,1).";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	String result = requestor.getResults();
    assertTrue(String.format("Result doesn't contain method forEach (%s)", result),
    		result.contains("forEach[METHOD_REF]{forEach(), Ljava.lang.Iterable<Ljava.lang.String;>;, (Ljava.util.function.Consumer<-Ljava.lang.String;>;)V, null, null, forEach, (arg0), replace[149, 149], token[149, 149], 60}"));
}
public void testBug574823_completeOn_methodInvocationWithParams_inIfConidtion_insideIf_followedByChainedStatment() throws Exception {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/Bug574823.java",
			"import java.util.ArrayList;\n" +
			"public class Bug574823 {\n" +
			"	public void foo() {\n" +
			"		ArrayList<String> ints = new ArrayList<String>();\n" +
			"		if(ints.subList(1,1).)\n" +
			"			System.out.println(message);\n" +
			"	}\n" +
			"}\n"
			);
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, true, true, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "ints.subList(1,1).";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	String result = requestor.getResults();
    assertTrue(String.format("Result doesn't contain method forEach (%s)", result),
    		result.contains("forEach[METHOD_REF]{forEach(), Ljava.lang.Iterable<Ljava.lang.String;>;, (Ljava.util.function.Consumer<-Ljava.lang.String;>;)V, null, null, forEach, (arg0), replace[149, 149], token[149, 149], 60}"));
}
public void testBug574823_completeOn_methodInvocationWithParams_inWhileConidtion_insideWhileBlock_followedByChainedStatment() throws Exception {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/Bug574823.java",
			"import java.util.ArrayList;\n" +
			"public class Bug574823 {\n" +
			"	public void foo() {\n" +
			"		ArrayList<String> ints = new ArrayList<String>();\n" +
			"		while(ints.subList(1,1).){\n" +
			"			System.out.println(message);\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
			);
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, true, true, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "ints.subList(1,1).";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	String result = requestor.getResults();
    assertTrue(String.format("Result doesn't contain method forEach (%s)", result),
    		result.contains("forEach[METHOD_REF]{forEach(), Ljava.lang.Iterable<Ljava.lang.String;>;, (Ljava.util.function.Consumer<-Ljava.lang.String;>;)V, null, null, forEach, (arg0), replace[152, 152], token[152, 152], 60}"));
}
public void testBug574823_completeOn_methodInvocationWithParams_inIfConidtionWithExpression_insideIfBlock_followedByChainedStatment() throws Exception {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/Bug574823.java",
			"import java.util.ArrayList;\n" +
			"public class Bug574823 {\n" +
			"	public void foo() {\n" +
			"		ArrayList<String> ints = new ArrayList<String>();\n" +
			"		while(ints.subList(1,1). != null){\n" +
			"			System.out.println(message);\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
			);
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, true, true, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "ints.subList(1,1).";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	String result = requestor.getResults();
    assertTrue(String.format("Result doesn't contain method forEach (%s)", result),
    		result.contains("forEach[METHOD_REF]{forEach(), Ljava.lang.Iterable<Ljava.lang.String;>;, (Ljava.util.function.Consumer<-Ljava.lang.String;>;)V, null, null, forEach, (arg0), replace[152, 152], token[152, 152], 60}"));
}
public void testBug574912_comment6() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"Completion/src/LambdaFreeze2.java",
			"import java.util.Calendar;\n" +
			"import java.util.Date;\n" +
			"import java.util.function.Supplier;\n" +
			"\n" +
			"public class LambdaFreeze2 {\n" +
			"	static int num = 13;\n" +
			"\n" +
			"	public static final Supplier<Date> SUPPLIER = () -> {\n" +
			"		Calendar calendar = Calendar.getInstance();\n" +
			"		calendar.set(Calendar.ALL_STYLES, calendar.getMinimum(0));\n" +
			"		return calendar.getTime();\n" +
			"	};\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "calendar.getMinimum(";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	String result = requestor.getResults();
	assertResults("num[FIELD_REF]{num, LLambdaFreeze2;, I, num, null, 52}\n"
			+ "getMinimum[METHOD_REF]{, Ljava.util.Calendar;, (I)I, getMinimum, (arg0), 86}", result);
}
public void testBug574912_comment6b() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"Completion/src/LambdaFreeze2.java",
			"import java.util.Calendar;\n" +
			"import java.util.Date;\n" +
			"import java.util.function.Supplier;\n" +
			"\n" +
			"public class LambdaFreeze2 {\n" +
			"	static int xyz = 13;\n" +
			"\n" +
			"	public static final Supplier<Date> SUPPLIER = () -> {\n" +
			"		Calendar calendar = Calendar.getInstance();\n" +
			"		calendar.set(Calendar.ALL_STYLES, calendar.getMinimum(xy0));\n" + // once we have a non-empty assist id, use it!
			"		return calendar.getTime();\n" +
			"	};\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "calendar.getMinimum(xy";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	String result = requestor.getResults();
	assertResults("xyz[FIELD_REF]{xyz, LLambdaFreeze2;, I, xyz, null, 82}",
			result);
}
public void testBug574882() throws Exception {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"Completion/src/ForLoop.java",
			"import java.util.concurrent.ExecutorService;\n" +
			"import java.util.concurrent.Executors;\n" +
			"import java.util.concurrent.atomic.AtomicInteger;\n" +
			"\n" +
			"public class ForLoop {\n" +
			"	public static void main(String[] args) {\n" +
			"		AtomicInteger executions = new AtomicInteger();\n" +
			"		ExecutorService pool = Executors.newFixedThreadPool(1);\n" +
			"		for (int i = 0; i < 42; i++) {\n" +
			"			pool.execute(() -> {\n" +
			"				// sys| offers sysout etc templates here \n" +
			"				executions.incrementAndGet();\n" +
			"				// sys | content assist doesn't offer \"sysout\" etc templates here\n" +
			"			});\n" +
			"		}\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(false, true, false, false, false, false, false, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBefore = "// sys | content assist doesn't offer";
	int cursorLocation = str.lastIndexOf(completeBefore);
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	String result = requestor.getResults();
	assertResults("ForLoop[TYPE_REF]{ForLoop, , LForLoop;, null, null, 52}\n" +
			"args[LOCAL_VARIABLE_REF]{args, null, [Ljava.lang.String;, args, null, 52}\n" +
			"executions[LOCAL_VARIABLE_REF]{executions, null, LAtomicInteger;, executions, null, 52}\n" +
			"i[LOCAL_VARIABLE_REF]{i, null, I, i, null, 52}\n" +
			"main[METHOD_REF]{main(), LForLoop;, ([Ljava.lang.String;)V, main, (args), 52}\n" +
			"pool[LOCAL_VARIABLE_REF]{pool, null, Ljava.util.concurrent.ExecutorService;, pool, null, 52}",
			result);
	assertEquals("completion offset=449\n" +
			"completion range=[449, 448]\n" +
			"completion token=\"\"\n" +
			"completion token kind=TOKEN_KIND_NAME\n" +
			"expectedTypesSignatures=null\n" +
			"expectedTypesKeys=null\n" +
			"completion token location={STATEMENT_START}", // this is required for sysout template proposal
			requestor.getContext());
}
public void testBug575149_expectOverloadedMethodsAndVariablesRankedWithExpectedType() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"Completion/src/Bug443091.java",
			"import java.util.function.Consumer;\n" +
			"import java.util.function.Function;\n" +
			"\n" +
			"public class Bug443091 {\n" +
			"	private void foo() {\n" +
			" 		Consumer<Integer> capture = null;\n" +
			"		forEach()" +
			"	}\n" +
			"	private void forEach(Consumer<Integer> in) {}\n" +
			"	private void forEach(Function<Integer, String> in) {}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "forEach(";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	String result = requestor.getResults();
	assertResults(
			"capture[LOCAL_VARIABLE_REF]{capture, null, Ljava.util.function.Consumer<Ljava.lang.Integer;>;, capture, null, 52}\n"
			+ "forEach[METHOD_REF]{, LBug443091;, (Ljava.util.function.Consumer<Ljava.lang.Integer;>;)V, forEach, (in), 56}\n"
			+ "forEach[METHOD_REF]{, LBug443091;, (Ljava.util.function.Function<Ljava.lang.Integer;Ljava.lang.String;>;)V, forEach, (in), 56}\n"
			+ "[LAMBDA_EXPRESSION]{->, Ljava.util.function.Function<Ljava.lang.Integer;Ljava.lang.String;>;, (Ljava.lang.Integer;)Ljava.lang.String;, apply, (arg0), 89}\n"
			+ "[LAMBDA_EXPRESSION]{->, Ljava.util.function.Consumer<Ljava.lang.Integer;>;, (Ljava.lang.Integer;)V, accept, (t), 89}",
			result);
	assertTrue("expected type signatures don't match", CharOperation.equals(requestor.getExpectedTypesSignatures(),
			new char[][] {"Ljava.util.function.Function<Ljava.lang.Integer;Ljava.lang.String;>;".toCharArray(),
			"Ljava.util.function.Consumer<Ljava.lang.Integer;>;".toCharArray()}, true));
}
public void testBug575149_expectRemainingOverloadedMethodsMatchingFilledArguments() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"Completion/src/Bug443091.java",
			"import java.util.function.Consumer;\n" +
			"import java.util.function.Function;\n" +
			"\n" +
			"public class Bug443091 {\n" +
			"	private void foo() {\n" +
			" 		Consumer<Integer> capture = null;\n" +
			"		forEach(capture, )" +
			"	}\n" +
			"	private void forEach(Consumer<Integer> in) {}\n" +
			"	private void forEach(Consumer<Integer> in, Integer limit) {}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "forEach(capture,";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	String result = requestor.getResults();
	assertResults("hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, 52}\n"
			+ "forEach[METHOD_REF]{, LBug443091;, (Ljava.util.function.Consumer<Ljava.lang.Integer;>;)V, forEach, (in), 56}\n"
			+ "forEach[METHOD_REF]{, LBug443091;, (Ljava.util.function.Consumer<Ljava.lang.Integer;>;Ljava.lang.Integer;)V, forEach, (in, limit), 56}",
			result);
	assertTrue("expected type signatures don't match", CharOperation.equals(requestor.getExpectedTypesSignatures(), new char[][] {"Ljava.lang.Integer;".toCharArray()}, true));
}
public void testBug575149_expectOverloadsOverEnumLiterals() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"Completion/src/Bug443091.java",
			"import java.util.function.Consumer;\n" +
			"import java.util.function.Function;\n" +
			"\n" +
			"public class Bug443091 {\n" +
			"	private void foo() {\n" +
			" 		Consumer<Integer> capture = null;\n" +
			"		forEach(capture, )" +
			"	}\n" +
			"	private Thread.State defaultState() { return null;} \n" +
			"	private void forEach(Consumer<Integer> in, Thread.State state) {}\n" +
			"	private void forEach(Consumer<Integer> in, Thread.State state, Integer limit) {}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "forEach(capture,";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	String result = requestor.getResults();
	assertResults("BLOCKED[FIELD_REF]{State.BLOCKED, Ljava.lang.Thread$State;, Ljava.lang.Thread$State;, BLOCKED, null, 49}\n" +
			"NEW[FIELD_REF]{State.NEW, Ljava.lang.Thread$State;, Ljava.lang.Thread$State;, NEW, null, 49}\n" +
			"RUNNABLE[FIELD_REF]{State.RUNNABLE, Ljava.lang.Thread$State;, Ljava.lang.Thread$State;, RUNNABLE, null, 49}\n" +
			"TERMINATED[FIELD_REF]{State.TERMINATED, Ljava.lang.Thread$State;, Ljava.lang.Thread$State;, TERMINATED, null, 49}\n" +
			"TIMED_WAITING[FIELD_REF]{State.TIMED_WAITING, Ljava.lang.Thread$State;, Ljava.lang.Thread$State;, TIMED_WAITING, null, 49}\n" +
			"WAITING[FIELD_REF]{State.WAITING, Ljava.lang.Thread$State;, Ljava.lang.Thread$State;, WAITING, null, 49}\n" +
			"defaultState[METHOD_REF]{defaultState(), LBug443091;, ()Ljava.lang.Thread$State;, defaultState, null, 52}\n" +
			"forEach[METHOD_REF]{, LBug443091;, (Ljava.util.function.Consumer<Ljava.lang.Integer;>;Ljava.lang.Thread$State;)V, forEach, (in, state), 56}\n" +
			"forEach[METHOD_REF]{, LBug443091;, (Ljava.util.function.Consumer<Ljava.lang.Integer;>;Ljava.lang.Thread$State;Ljava.lang.Integer;)V, forEach, (in, state, limit), 56}",
			result);
	assertTrue("expected type signatures don't match", CharOperation.equals(requestor.getExpectedTypesSignatures(), new char[][] {"Ljava.lang.Thread$State;".toCharArray()}, true));
}
public void testBug443091_expectLambdaCompletions_forFunctionalInterfaceArgumentAssignment() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"Completion/src/Bug443091.java",
			"import java.util.function.Consumer;\n" +
			"\n" +
			"public class Bug443091 {\n" +
			"	private void foo() {\n" +
			"		forEach(capture)" +
			"	}\n" +
			"	private void forEach(Consumer<Integer> in) {}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "forEach(";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	String result = requestor.getResults();
	assertResults("forEach[METHOD_REF]{, LBug443091;, (Ljava.util.function.Consumer<Ljava.lang.Integer;>;)V, forEach, (in), 56}\n"
			+ "[LAMBDA_EXPRESSION]{->, Ljava.util.function.Consumer<Ljava.lang.Integer;>;, (Ljava.lang.Integer;)V, accept, (t), 89}",
			result);
}
public void testBug443091_expectLambdaCompletions_forFunctionalInterfaceVariableAssigments() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"Completion/src/Bug443091.java",
			"import java.util.function.Consumer;\n" +
			"\n" +
			"public class Bug443091 {\n" +
			"	private void foo() {\n" +
			" 		Consumer<Integer> in = \n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "in =";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	String result = requestor.getResults();
	assertResults("finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, 47}\n"
			+ "foo[METHOD_REF]{foo(), LBug443091;, ()V, foo, null, 47}\n"
			+ "notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, 47}\n"
			+ "notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, 47}\n"
			+ "wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, 47}\n"
			+ "wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, wait, (millis), 47}\n"
			+ "wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), 47}\n"
			+ "Bug443091[TYPE_REF]{Bug443091, , LBug443091;, null, null, 52}\n"
			+ "clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, 52}\n"
			+ "equals[METHOD_REF]{equals(), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), 52}\n"
			+ "getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<*>;, getClass, null, 52}\n"
			+ "hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, 52}\n"
			+ "toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, 52}\n"
			+ "Consumer<java.lang.Integer>[TYPE_REF]{Consumer, java.util.function, Ljava.util.function.Consumer<Ljava.lang.Integer;>;, null, null, 82}\n"
			+ "[LAMBDA_EXPRESSION]{->, Ljava.util.function.Consumer<Ljava.lang.Integer;>;, (Ljava.lang.Integer;)V, accept, (t), 89}",
			result);
}
public void testBug576068() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"Completion/src/Bug576068.java",
			"public class Bug576068 {\n" +
			"\n" +
			"	// Type a new member here and content assist won't find anything.\n" +
			"\n" +
			"	public void methodA(){\n" +
			"		switch( 1 ){\n" +
			"			case 0:\n" +
			"		}\n" +
			"	}\n" +
			"	public void methodB(){\n" +
			"		Runnable r = ()->{};\n" +
			"	}\n" +
			"}");
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBefore = "// Type";
	int cursorLocation = str.indexOf(completeBefore);
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	String result = requestor.getResults();
	assertResults("[POTENTIAL_METHOD_DECLARATION]{, LBug576068;, ()V, , null, 39}\n" +
			"abstract[KEYWORD]{abstract, null, null, abstract, null, 49}\n" +
			"class[KEYWORD]{class, null, null, class, null, 49}\n" +
			"enum[KEYWORD]{enum, null, null, enum, null, 49}\n" +
			"final[KEYWORD]{final, null, null, final, null, 49}\n" +
			"interface[KEYWORD]{interface, null, null, interface, null, 49}\n" +
			"native[KEYWORD]{native, null, null, native, null, 49}\n" +
			"private[KEYWORD]{private, null, null, private, null, 49}\n" +
			"protected[KEYWORD]{protected, null, null, protected, null, 49}\n" +
			"public[KEYWORD]{public, null, null, public, null, 49}\n" +
			"static[KEYWORD]{static, null, null, static, null, 49}\n" +
			"strictfp[KEYWORD]{strictfp, null, null, strictfp, null, 49}\n" +
			"synchronized[KEYWORD]{synchronized, null, null, synchronized, null, 49}\n" +
			"transient[KEYWORD]{transient, null, null, transient, null, 49}\n" +
			"volatile[KEYWORD]{volatile, null, null, volatile, null, 49}\n" +
			"Bug576068[TYPE_REF]{Bug576068, , LBug576068;, null, null, 52}\n" +
			"clone[METHOD_DECLARATION]{protected Object clone() throws CloneNotSupportedException, Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, 52}\n" +
			"equals[METHOD_DECLARATION]{public boolean equals(Object obj), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), 52}\n" +
			"finalize[METHOD_DECLARATION]{protected void finalize() throws Throwable, Ljava.lang.Object;, ()V, finalize, null, 52}\n" +
			"hashCode[METHOD_DECLARATION]{public int hashCode(), Ljava.lang.Object;, ()I, hashCode, null, 52}\n" +
			"toString[METHOD_DECLARATION]{public String toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, 52}",
			result);
}
public void testBug577883_expectCompletions_onLambdaVars_inNestedLambdas() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"Completion/src/Bug577883.java",
			"import java.util.stream.Stream;\n" +
			"\n" +
			"public class Bug577883 {\n" +
			"	private static class Int { void boo(){} }\n"+
			"	private void foo() {\n" +
			"		Runnable run = () -> {\n" +
			"			Stream.of(new Int()).map(t -> t.)\n"+
			"		};\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "t.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	String result = requestor.getResults();
	assertResults("boo[METHOD_REF]{boo(), LBug577883$Int;, ()V, boo, null, 55}\n"
			+ "finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, 55}\n"
			+ "notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, 55}\n"
			+ "notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, 55}\n"
			+ "wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, 55}\n"
			+ "wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, wait, (millis), 55}\n"
			+ "wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), 55}\n"
			+ "equals[METHOD_REF]{equals(), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), 80}\n"
			+ "hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, 80}\n"
			+ "getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<*>;, getClass, null, 85}\n"
			+ "toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, 85}\n"
			+ "clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, 90}",
			result);
}
public void testBug577883_expectCompletions_onLambdaVars_inNestedLambdasL2() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"Completion/src/Bug577883.java",
			"import java.util.stream.Stream;\n" +
			"import java.util.Optional;\n" +
			"\n" +
			"public class Bug577883 {\n" +
			"	private static class Int { Integer boo(){ return 0;} }\n"+
			"	private void foo() {\n" +
			"		Runnable run = () -> {\n" +
			"			Stream.of(new Int()).map(t -> Optional.ofNullable(t).map(t -> t.))\n"+
			"		};\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "t.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	String result = requestor.getResults();
	assertResults("finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, 55}\n"
			+ "notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, 55}\n"
			+ "notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, 55}\n"
			+ "wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, 55}\n"
			+ "wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, wait, (millis), 55}\n"
			+ "wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), 55}\n"
			+ "equals[METHOD_REF]{equals(), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), 80}\n"
			+ "hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, 80}\n"
			+ "boo[METHOD_REF]{boo(), LBug577883$Int;, ()Ljava.lang.Integer;, boo, null, 85}\n"
			+ "getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<*>;, getClass, null, 85}\n"
			+ "toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, 85}\n"
			+ "clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, 90}",
			result);
}
public void testBug577883_expectCompletions_onIntermediateLambdaVars_inNestedLambdas() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"Completion/src/Bug577883.java",
			"import java.util.stream.Stream;\n" +
			"import java.util.Optional;\n" +
			"\n" +
			"public class Bug577883 {\n" +
			"	private static class Int { " +
			"		Integer boo(){ return 0;} " +
			"		boolean canBoo(){ return true;} " +
			"	}\n"+
			"	private void foo() {\n" +
			"		Runnable run = () -> {\n" +
			"			Stream.of(new Int()).map(t -> Optional.ofNullable(t).map(t -> t.boo() && t.))\n"+
			"		};\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "&& t.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	String result = requestor.getResults();
	assertResults("finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, 55}\n"
			+ "notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, 55}\n"
			+ "notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, 55}\n"
			+ "wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, 55}\n"
			+ "wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, wait, (millis), 55}\n"
			+ "wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), 55}\n"
			+ "boo[METHOD_REF]{boo(), LBug577883$Int;, ()Ljava.lang.Integer;, boo, null, 60}\n"
			+ "clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, 60}\n"
			+ "getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<*>;, getClass, null, 60}\n"
			+ "hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, 60}\n"
			+ "toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, 60}\n"
			+ "canBoo[METHOD_REF]{canBoo(), LBug577883$Int;, ()Z, canBoo, null, 90}\n"
			+ "equals[METHOD_REF]{equals(), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), 90}",
			result);
}
public void testBug577883_expectCompletions_onOuterLambdaVars_inNestedLambdas() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"Completion/src/Bug577883.java",
			"import java.util.stream.Stream;\n" +
			"import java.util.Optional;\n" +
			"import java.util.function.Consumer;\n" +
			"\n" +
			"public class Bug577883 {\n" +
			"	private static class Int { " +
			"		Integer boo(){ return 0;} " +
			"		boolean canBoo(){ return true;} " +
			"	}\n"+
			"	private static class Dbl { " +
			"		Double boo(){ return 0.0;} " +
			"	}\n"+
			"	private void foo() {\n" +
			"		Consumer<Dbl> consu = (d) -> {\n" +
			"			Stream.of(new Int()).filter(t -> d.)\n"+
			"		};\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "d.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	String result = requestor.getResults();
	assertResults("finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, 55}\n"
			+ "notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, 55}\n"
			+ "notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, 55}\n"
			+ "wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, 55}\n"
			+ "wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, wait, (millis), 55}\n"
			+ "wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), 55}\n"
			+ "boo[METHOD_REF]{boo(), LBug577883$Dbl;, ()Ljava.lang.Double;, boo, null, 60}\n"
			+ "clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, 60}\n"
			+ "getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<*>;, getClass, null, 60}\n"
			+ "hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, 60}\n"
			+ "toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, 60}\n"
			+ "equals[METHOD_REF]{equals(), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), 90}",
			result);
}
public void testBug577885_expectCompletions_onMethodArguments_followingMethodInvocationWithMethodRefArguments() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"Completion/src/Bug577885.java",
			"import java.util.stream.Stream;\n" +
			"\n" +
			"public class Bug577885 {\n" +
			"	private void foo() {\n" +
			" 		Stream.of(\"1\").map(Long::valueOf).filter()\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "filter(";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	String result = requestor.getResults();
	assertResults("filter[METHOD_REF]{, Ljava.util.stream.Stream<Ljava.lang.Long;>;, (Ljava.util.function.Predicate<-Ljava.lang.Long;>;)Ljava.util.stream.Stream<Ljava.lang.Long;>;, filter, (arg0), 56}\n"
			+ "[LAMBDA_EXPRESSION]{->, Ljava.util.function.Predicate<Ljava.lang.Long;>;, (Ljava.lang.Long;)Z, test, (arg0), 89}",
			result);
}
public void testBug577885_expectCompletions_onMethodArguments_followingMethodInvocationWithMethodRefArguments_InsideLambda() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"Completion/src/Bug577885.java",
			"import java.util.stream.Stream;\n" +
			"\n" +
			"public class Bug577885 {\n" +
			"	private void foo() {\n" +
			"		Runnable run = () -> {\n" +
			" 			Stream.of(\"1\").map(Long::valueOf).filter()\n" +
			"		};\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "filter(";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	String result = requestor.getResults();
	assertResults("filter[METHOD_REF]{, Ljava.util.stream.Stream<Ljava.lang.Long;>;, (Ljava.util.function.Predicate<-Ljava.lang.Long;>;)Ljava.util.stream.Stream<Ljava.lang.Long;>;, filter, (arg0), 56}\n"
			+ "[LAMBDA_EXPRESSION]{->, Ljava.util.function.Predicate<Ljava.lang.Long;>;, (Ljava.lang.Long;)Z, test, (arg0), 89}",
			result);
}
public void testBug578116_expectCompletions_forConstructorsInsideLamndaBlock() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"Completion/src/Bug578116.java",
			"import java.util.ArrayList;\n" +
			"\n" +
			"public class Bug578116 {\n" +
			"	private void foo() {\n" +
			"		Runnable run = () -> {\n" +
			"			ArrayList<String> list = new \n"+
			"		};\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "= new ";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	String result = requestor.getResults();
	assertResults("Bug578116[TYPE_REF]{Bug578116, , LBug578116;, null, null, 52}\n"
			+ "ArrayList<java.lang.String>[TYPE_REF]{ArrayList, java.util, Ljava.util.ArrayList<Ljava.lang.String;>;, null, null, 82}",
			result);
}
public void testBug578817() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"Completion/src/Bug578817.java",
			"import java.util.Map;\n" +
			"\n" +
			"public class Bug578817 {\n" +
			"	private void foo() {\n" +
			"		Map map = new LinkedHashMap\n"+
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.setAllowsRequiredProposals(CompletionProposal.TYPE_REF, CompletionProposal.TYPE_REF, true);
	requestor.setAllowsRequiredProposals(CompletionProposal.CONSTRUCTOR_INVOCATION, CompletionProposal.TYPE_REF, true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "new LinkedHashMap";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());
	String result = requestor.getResults();
	assertResults("LinkedHashMap[CONSTRUCTOR_INVOCATION]{(), Ljava.util.LinkedHashMap;, ()V, LinkedHashMap, null, 81}\n"
			+ "LinkedHashMap[CONSTRUCTOR_INVOCATION]{(), Ljava.util.LinkedHashMap;, (I)V, LinkedHashMap, (arg0), 81}\n"
			+ "LinkedHashMap[CONSTRUCTOR_INVOCATION]{(), Ljava.util.LinkedHashMap;, (IF)V, LinkedHashMap, (arg0, arg1), 81}\n"
			+ "LinkedHashMap[CONSTRUCTOR_INVOCATION]{(), Ljava.util.LinkedHashMap;, (IFZ)V, LinkedHashMap, (arg0, arg1, arg2), 81}\n"
			+ "LinkedHashMap[CONSTRUCTOR_INVOCATION]{(), Ljava.util.LinkedHashMap;, (Ljava.util.Map<+TK;+TV;>;)V, LinkedHashMap, (arg0), 81}", result);

	requestor = new CompletionTestsRequestor2(true);
	requestor.setAllowsRequiredProposals(CompletionProposal.TYPE_REF, CompletionProposal.TYPE_REF, true);
	requestor.setAllowsRequiredProposals(CompletionProposal.CONSTRUCTOR_INVOCATION, CompletionProposal.TYPE_REF, true);
	requestor.setTypeProposalFilter((typeName) -> {
		return typeName.startsWith("java.util.");
	});
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());
	result = requestor.getResults();
	assertResults("", result);
}
	public void testBug564875() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"import java.util.List;\n" +
				"class Person {\n" +
				"   String getLastName() { return null; }\n" +
				"   Person getLastPerson() { return null; }\n" +
				"}\n" +
				"public class X {\n" +
				"	void test1 (List<Person> people) {\n" +
				"		people.stream().forEach(p -> System.out.println(p.get)); \n" +
				"	}\n" +
				"}\n");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "p.get";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("getLastPerson[METHOD_REF]{getLastPerson(), LPerson;, ()LPerson;, null, null, getLastPerson, null, [229, 232], "+ (R_DEFAULT+R_EXPECTED_TYPE+30)+"}\n"
				+ "getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<*>;, null, null, getClass, null, [229, 232], "+(R_DEFAULT+R_PACKAGE_EXPECTED_TYPE+30)+"}\n"
				+ "getLastName[METHOD_REF]{getLastName(), LPerson;, ()Ljava.lang.String;, null, null, getLastName, null, [229, 232], "+(R_DEFAULT+R_EXACT_EXPECTED_TYPE+30)+"}", requestor.getResults());
	}

public void testGH109_expectCompletions_insideLambdaNestedBlocks() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"Completion/src/GH109.java",
			"import java.util.stream.Stream;\n"
			+ "\n"
			+ "public class GH109\n"
			+ "{\n"
			+ "  public static void main(String[] args)\n"
			+ "  {\n"
			+ "    if(args.length > 0) {\n"
			+ "      Stream.of(args).forEach(name -> {\n"
			+ "        try\n"
			+ "        {\n"
			+ "          if (name.startsWith(\"A\"))\n"
			+ "          {\n"
			+ "            name.\n"
			+ "          }\n"
			+ "          \n"
			+ "        }\n"
			+ "        catch (Exception e)\n"
			+ "        {\n"
			+ "        }\n"
			+ "      });\n"
			+ "    }\n"
			+ "  }\n"
			+ "}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "  name.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	String result = requestor.getResults();
	assertTrue(result.contains("startsWith[METHOD_REF]{startsWith(), Ljava.lang.String;, (Ljava.lang.String;)Z, startsWith, (arg0), 60}\n"
			+ "startsWith[METHOD_REF]{startsWith(), Ljava.lang.String;, (Ljava.lang.String;I)Z, startsWith, (arg0, arg1), 60}\n"));
}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/195
	public void testIssue195() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"Completion/src/A.java",
				"public class A {\n" +
				"  public void test() {\n" +
				"    List<String> list = new java.util.ArrayL\n" +
				"  }\n" +
				"}\n");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "ArrayL";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());
		assertResults(
				"ArrayList[CONSTRUCTOR_INVOCATION]{(), Ljava.util.ArrayList;, ()V, null, null, ArrayList, null, [84, 84], 54}\n" +
				"ArrayList[CONSTRUCTOR_INVOCATION]{(), Ljava.util.ArrayList;, (I)V, null, null, ArrayList, (arg0), [84, 84], 54}\n" +
				"ArrayList[CONSTRUCTOR_INVOCATION]{(), Ljava.util.ArrayList;, (Ljava.util.Collection<+TE;>;)V, null, null, ArrayList, (arg0), [84, 84], 54}",
				requestor.getResults());
	}

public void testGH109_expectCompletionsWithCast_insideLambdaNestedBlocksWithInstanceOf() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"Completion/src/GH109.java",
			"import java.util.stream.Stream;\n"
			+ "\n"
			+ "public class GH109\n"
			+ "{\n"
			+ "  public static void main(String[] args)\n"
			+ "  {\n"
			+ "    if(args.length > 0) {\n"
			+ "      Stream.of(args).map(Object.class::cast).forEach(name -> {\n"
			+ "        try\n"
			+ "        {\n"
			+ "          if (name instanceof String)\n"
			+ "          {\n"
			+ "            name.sta\n"
			+ "          }\n"
			+ "          \n"
			+ "        }\n"
			+ "        catch (Exception e)\n"
			+ "        {\n"
			+ "        }\n"
			+ "      });\n"
			+ "    }\n"
			+ "  }\n"
			+ "}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "  name.sta";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	String result = requestor.getResults();
	assertTrue(result.contains("startsWith[METHOD_REF_WITH_CASTED_RECEIVER]{((String)name).startsWith(), Ljava.lang.String;, (Ljava.lang.String;)Z, Ljava.lang.String;, startsWith, (arg0), 60}\n"
			+ "startsWith[METHOD_REF_WITH_CASTED_RECEIVER]{((String)name).startsWith(), Ljava.lang.String;, (Ljava.lang.String;I)Z, Ljava.lang.String;, startsWith, (arg0, arg1), 60}"));
}

public void testGH583_onArrayCreationSupplier_expectNewMethodRefCompletions() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/GH583.java",
			"import java.util.Arrays;\n"
					+ "public class GH583 {\n"
					+ "    public void foo() {\n"
					+ "			Arrays.asList(\"1\").stream().toArray(String[]::)"
					+ "    }\n"
					+ "}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

	String str = this.workingCopies[0].getSource();
	String completeBehind = "toArray(String[]::";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

	String result = requestor.getResults();
	assertTrue(String.format("Result doesn't contain expected methods (%s)", result),
			result.contains("new[KEYWORD]{new, null, null, new, null, 49}"));
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/767
public void testGH767() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/Foo.java",
					"public class Foo {\n"
					+ "    public void foo() {\n"
					+ "			\"abc\".substring(i)."
					+ "    }\n"
					+ "}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

	String str = this.workingCopies[0].getSource();
	String completeBehind = "substring(i).";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

	String result = requestor.getResults();
	assertTrue(String.format("Result doesn't contain expected method (%s)", result),
			result.contains("length[METHOD_REF]{length(), Ljava.lang.String;, ()I, length, null, 60}\n"));
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/831
public void testIntersection18GH831() throws Exception {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/Foo.java",
			"public class Foo {\n"
			+ "    public void foo() {\n"
			+ "			java.util.Optional.of(true ? 0 : \"\")."
			+ "    }\n"
			+ "}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

	String str = this.workingCopies[0].getSource();
	String completeBehind = "of(true ? 0 : \"\").";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

	String result = requestor.getResults();
	assertTrue(String.format("Result doesn't contain expected method (%s)", result),
	result.contains("get[METHOD_REF]{get(), Ljava.util.Optional<Ljava.io.Serializable;>;, ()Ljava.io.Serializable;, get, null, 60}\n"));
}

public void testGH960_onVarargArgument_expectCompletionsMatchingElementType() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/Completion/src/GH960.java", """
			public class GH960 {
				public void foo(GH960.State... states) {
				}

				public void boo() {
					GH960.State currentState = GH960.State.BLOCKED;

					foo()
				}

				public static enum State {
					BLOCKED, RUNNABLE;
				}
			}
			""");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo(";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

	String result = requestor.getResults();
	assertResults(
			"BLOCKED[FIELD_REF]{State.BLOCKED, LGH960$State;, LGH960$State;, BLOCKED, null, "
					+ (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED) + "}\n"
					+ "RUNNABLE[FIELD_REF]{State.RUNNABLE, LGH960$State;, LGH960$State;, RUNNABLE, null, "
					+ (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED) + "}\n"
					+ "currentState[LOCAL_VARIABLE_REF]{currentState, null, LGH960$State;, currentState, null, "
					+ (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n"
					+ "foo[METHOD_REF]{, LGH960;, ([LGH960$State;)V, foo, (states), " + (R_DEFAULT
							+ R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_UNQUALIFIED + R_NON_RESTRICTED)
					+ "}",
			result);
}

public void testGH960_onVarargArguments_expectCompletionsMatchingElementType() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/Completion/src/GH960.java", """
			public class GH960 {
				public void foo(GH960.State... states) {
				}

				public void boo() {
					GH960.State currentState = GH960.State.BLOCKED;

					foo(State.BLOCKED, )
				}

				public static enum State {
					BLOCKED, RUNNABLE;
				}
			}
			""");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo(State.BLOCKED, ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

	String result = requestor.getResults();
	int relevanceExpectedTypes = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED
			+ R_EXACT_EXPECTED_TYPE;
	assertContains("Enums",
			"BLOCKED[FIELD_REF]{State.BLOCKED, LGH960$State;, LGH960$State;, BLOCKED, null, "
					+ relevanceExpectedTypes + "}\n"
					+ "RUNNABLE[FIELD_REF]{State.RUNNABLE, LGH960$State;, LGH960$State;, RUNNABLE, null, "
					+ relevanceExpectedTypes + "}",
			result);
	assertContains("Variables",
			"currentState[LOCAL_VARIABLE_REF]{currentState, null, LGH960$State;, currentState, null, "
					+ (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED
							+ R_EXACT_EXPECTED_TYPE)
					+ "}",
			result);
}

public void testGH960_onBeforeVarargArguments_expectCompletionsMatchingElementType() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/Completion/src/GH960.java", """
			public class GH960 {
				public void foo(String name, Thread.State... states) {
				}

				public void boo() {
				   Thread.State currentState = Thread.State.BLOCKED;
				   String threadName = "name";

				   foo(, State.BLOCKED)
				}
			}
			""");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo(";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

	String result = requestor.getResults();
	assertContains("Variables",
			"threadName[LOCAL_VARIABLE_REF]{threadName, null, Ljava.lang.String;, threadName, null, "
					+ (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			result);
}

public void testGH979_on1stConstructorArgument_expectCompletionsMatchinType() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[1] = getWorkingCopy("/Completion/src/GH979List.java", """
			public class GH979List<T> {

			}
			""");
	this.workingCopies[0] = getWorkingCopy("/Completion/src/GH979.java", """
			public class GH979 {
				public GH979(GH979List<String> names, int age) {}

				public static void foo() {
					GH979 value = new GH979();
				}

				public static GH979List<String> newInstance() {
					return null;
				}
			}
			""");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

	String str = this.workingCopies[0].getSource();
	String completeBehind = "new GH979(";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

	String result = requestor.getResults();
	assertResults(
			"GH979[ANONYMOUS_CLASS_DECLARATION]{, LGH979;, (LGH979List<Ljava.lang.String;>;I)V, null, (names, age), "
					+ (R_DEFAULT + R_INTERESTING + R_RESOLVED + R_NON_RESTRICTED) + "}\n"
					+ "GH979[METHOD_REF<CONSTRUCTOR>]{, LGH979;, (LGH979List<Ljava.lang.String;>;I)V, GH979, (names, age), "
					+ (R_DEFAULT + R_INTERESTING + R_RESOLVED + R_NON_RESTRICTED) + "}\n"
					+ "newInstance[METHOD_REF]{newInstance(), LGH979;, ()LGH979List<Ljava.lang.String;>;, newInstance, null, "
					+ (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			result);
}

public void testGH979_onAnonClassConstructorWith_expectOnlyAnonClassCompletion() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[1] = getWorkingCopy("/Completion/src/Serial.java", """
			public interface Serial {

			}
			""");
	this.workingCopies[0] = getWorkingCopy("/Completion/src/GH979.java", """
		public class GH979 {
				public GH979() {}

				public void foo() {
					Serial run= new Serial() {
					};
				}

				public Serial toString1() {
					return null;
				}
			}
			""");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

	String str = this.workingCopies[0].getSource();
	String completeBehind = "Serial run= new Serial(";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

	String result = requestor.getResults();
	assertResults("Serial[ANONYMOUS_CLASS_DECLARATION]{, LSerial;, ()V, null, null, "
			+ (R_DEFAULT + R_INTERESTING + R_RESOLVED + R_NON_RESTRICTED) + "}", result);
}

public void testGH979_on1stConstructorArgumentWithFilledArgumentNames_expectCompletionsMatchinType()
		throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[1] = getWorkingCopy("/Completion/src/GH979List.java", """
			public class GH979List<T> {

			}
			""");
	this.workingCopies[0] = getWorkingCopy("/Completion/src/GH979.java", """
			public class GH979 {
				public GH979(GH979List<String> names, int age, boolean valid) {}

				public static void foo() {
					GH979.instance().boo(new GH979(, 20, false));
				}

				public GH979 instance() {
					return null;
				}

				public void boo(GH979 g) {

				}

				public static GH979List<String> newInstance() {
					return null;
				}
			}
			""");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

	String str = this.workingCopies[0].getSource();
	String completeBehind = "new GH979(";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

	String result = requestor.getResults();
	assertResults(
			"GH979[ANONYMOUS_CLASS_DECLARATION]{, LGH979;, (LGH979List<Ljava.lang.String;>;IZ)V, null, (names, age, valid), "
					+ (R_DEFAULT + R_INTERESTING + R_RESOLVED + R_NON_RESTRICTED) + "}\n"
					+ "GH979[METHOD_REF<CONSTRUCTOR>]{, LGH979;, (LGH979List<Ljava.lang.String;>;IZ)V, GH979, (names, age, valid), "
					+ (R_DEFAULT + R_INTERESTING + R_RESOLVED + R_NON_RESTRICTED) + "}\n"
					+ "newInstance[METHOD_REF]{newInstance(), LGH979;, ()LGH979List<Ljava.lang.String;>;, newInstance, null, "
					+ (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			result);
}
}
