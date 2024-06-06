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
			"""
				interface Foo {\s
					void run1(int s1, int s2);
				}
				interface X extends Foo{
				  static Foo f = (first, second) -> System.out.print(fir);
				}
				""");

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
			"""
				interface Foo {\s
					void run1(int s1, int s2);
				}
				interface X extends Foo {
				  public static void main(String [] args) {
				      Foo f = (first, second) -> System.out.print(fir);
				  }
				}
				""");

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
							return (xyz, pqr) -> first.c
						});
					}
				}
				""");

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
			"""
				interface Foo {
					int run1(int s1, int s2);
				}
				interface X extends Foo{
				    static Foo f = (lpx5, lpx6) -> {lpx
				}
				""");

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
				""");

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
				""");

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
			"""
				interface I {
					int foo(int x);
				}
				public class X {
					void go() {
						I i = (argument) ->\s
							argument == 0 ? arg
						;
					}
				}
				""");

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
				""");

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
			"""
				interface I {
				    void doit();
				}
				interface J {
				}
				public class X {\s
					/* BEFORE */
					Object o = (I & J) () -> {};
					/* AFTER */
				}
				""");

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
			"""
				interface I {
				    void doit();
				}
				interface J {
				}
				public class X {\s
					/* BEFORE */
					Object o = (I & J) () -> {};
					/* AFTER */
				}
				""");

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
			"""
				interface I {
				  String foo(X x, X i);\s
				}\s
				public class X  {
					static void goo(I i) {
					}
					static void goo(String s) {
					}
					public static void main(String[] args) {\s
						goo((x, y) -> {
							x.
							return x + y;
						});
					}
				}
				""");

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
			"""
				interface I {
					void foo();
				}
				public class X {
					public static void main(String[] args) {
						syso
						I i = () -> {
						};
					}
				}
				""");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "syso";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("", requestor.getResults());
	assertEquals("""
		completion offset=94
		completion range=[90, 93]
		completion token="syso"
		completion token kind=TOKEN_KIND_NAME
		expectedTypesSignatures=null
		expectedTypesKeys=null
		completion token location={STATEMENT_START}""", requestor.getContext());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422901, [1.8][code assist] Code assistant sensitive to scope.referenceContext type identity.
public void test012() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"""
				interface I {
					void foo();
				}
				public class X {
					public static void main(String[] args) {
						I i = () -> {
						    syso
						};
					}
				}
				""");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "syso";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("", requestor.getResults());
	assertEquals("""
		completion offset=114
		completion range=[110, 113]
		completion token="syso"
		completion token kind=TOKEN_KIND_NAME
		expectedTypesSignatures=null
		expectedTypesKeys=null
		completion token location={STATEMENT_START}""", requestor.getContext());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422901, [1.8][code assist] Code assistant sensitive to scope.referenceContext type identity.
public void test013() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"""
				interface I {
					void foo();
				}
				public class X {
					public static void main(String[] args) {
						I i = () -> {
						};
						syso
					}
				}
				""");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "syso";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("", requestor.getResults());
	assertEquals("""
		completion offset=115
		completion range=[111, 114]
		completion token="syso"
		completion token kind=TOKEN_KIND_NAME
		expectedTypesSignatures=null
		expectedTypesKeys=null
		completion token location={STATEMENT_START}""", requestor.getContext());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422901, [1.8][code assist] Code assistant sensitive to scope.referenceContext type identity.
public void test014() throws JavaModelException { // ensure higher relevance for matching return type.
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"""
				interface I {
					int [] foo();
				}
				public class X {
					public static void main(String[] arrayOfStrings) {
				       int [] arrayOfInts = null;
						I i = () -> {
				           return arrayO
						};
					}
				}
				""");

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
			"""
				interface I {
					void foo();
				}
				public class X {
					public static void main(String[] args) {
				       {
						I i = () -> {
				           {
				               syso
				           }
						};
				       }
					}
				}
				""");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "syso";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("", requestor.getResults());
	assertEquals("""
		completion offset=145
		completion range=[141, 144]
		completion token="syso"
		completion token kind=TOKEN_KIND_NAME
		expectedTypesSignatures=null
		expectedTypesKeys=null
		completion token location={STATEMENT_START}""", requestor.getContext());
}
//https://github.com/eclipse-jdt/eclipse.jdt.core/issues/650, Templates not working in Lambda internal block.
public void test015b() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"""
				interface I {
					void foo();
				}
				public class X {
					public static void main(String[] args) {
				       {
						I i = () -> {
				           if (args.length > 3) {
				               syso
				           }
						};
				       }
					}
				}
				""");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "syso";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("", requestor.getResults());
	assertEquals("""
		completion offset=166
		completion range=[162, 165]
		completion token="syso"
		completion token kind=TOKEN_KIND_NAME
		expectedTypesSignatures=null
		expectedTypesKeys=null
		completion token location={STATEMENT_START}""", requestor.getContext());
}
//https://github.com/eclipse-jdt/eclipse.jdt.core/issues/650, Templates not working in Lambda internal block.
public void test015c() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"""
				interface I {
					void foo();
				}
				public class X {
					public static void main(String[] args) {
				       {
						I i = () -> {
				           {
				               if (args.length > 3)
				                   syso
				           }
						};
				       }
					}
				}
				""");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "syso";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("", requestor.getResults());
	assertEquals("""
		completion offset=185
		completion range=[181, 184]
		completion token="syso"
		completion token kind=TOKEN_KIND_NAME
		expectedTypesSignatures=null
		expectedTypesKeys=null
		completion token location={STATEMENT_START}""", requestor.getContext());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422901, [1.8][code assist] Code assistant sensitive to scope.referenceContext type identity.
public void test016() throws JavaModelException { // ensure higher relevance for matching return type.
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"""
				interface I {
					void foo();
				}
				public class X {
					public static void main(String[] args) {
						class Y {
							I i = () -> {
				               xyzBefore = 10;
				               xyz
							}
						}
					}
				}
				""");

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
			"""
				interface I {
					void foo();
				}
				public class X {
				   public static X xField;
				   public static X goo() { return null; }
					public static void main(String[] args) {
							I i = () -> {
				               xyz
					}
					}
				}
				""");

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
	assertEquals("""
		completion offset=192
		completion range=[189, 191]
		completion token="xyz"
		completion token kind=TOKEN_KIND_NAME
		expectedTypesSignatures=null
		expectedTypesKeys=null
		completion token location={STATEMENT_START}
		visibleElements={
			xField {key=LX;.xField)LX;} [in X [in [Working copy] X.java [in <default> [in src [in Completion]]]]],
			goo() {key=LX;.goo()LX;} [in X [in [Working copy] X.java [in <default> [in src [in Completion]]]]],
		}""" , requestor.getContext());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422468, [1.8][assist] Code assist issues with type elided lambda parameters
public void test018() throws JavaModelException { // computing visible elements in lambda scope.
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"""
				interface I {
					void foo(String x);
				}
				public class X {
					static X xField;
					static X goo(String s) {
				       return null;
					}
					static void goo(I i) {
					}
					public static void main(String[] args) {
						goo((xyz) -> {
							System.out.println(xyz.);
						});
					}
				}
				""");

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
	assertEquals("""
		completion offset=233
		completion range=[233, 232]
		completion token=""
		completion token kind=TOKEN_KIND_NAME
		expectedTypesSignatures={Z,C,I,J,F,D,[C,Ljava.lang.String;,Ljava.lang.Object;}
		expectedTypesKeys={Z,C,I,J,F,D,[C,Ljava/lang/String;,Ljava/lang/Object;}
		completion token location=UNKNOWN
		visibleElements={
			xField {key=LX;.xField)LX;} [in X [in [Working copy] X.java [in <default> [in src [in Completion]]]]],
			goo(String) {key=LX;.goo(Ljava/lang/String;)LX;} [in X [in [Working copy] X.java [in <default> [in src [in Completion]]]]],
		}""" , requestor.getContext());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422468, [1.8][assist] Code assist issues with type elided lambda parameters
public void test018a() throws JavaModelException { // computing visible elements in lambda scope.
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
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
				""");

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
			"""
				completion offset=419
				completion range=[419, 418]
				completion token=""
				completion token kind=TOKEN_KIND_NAME
				expectedTypesSignatures={Z,C,I,J,F,D,[C,Ljava.lang.String;,Ljava.lang.Object;}
				expectedTypesKeys={Z,C,I,J,F,D,[C,Ljava/lang/String;,Ljava/lang/Object;}
				completion token location=UNKNOWN
				visibleElements={
					xLambdaLocal [in main(String[]) [in X [in [Working copy] X.java [in <default> [in src [in Completion]]]]]],
					xyz [in main(String[]) [in X [in [Working copy] X.java [in <default> [in src [in Completion]]]]]],
					xLocal [in main(String[]) [in X [in [Working copy] X.java [in <default> [in src [in Completion]]]]]],
					xField {key=LX;.xField)LX;} [in X [in [Working copy] X.java [in <default> [in src [in Completion]]]]],
					goo(String) {key=LX;.goo(Ljava/lang/String;)LX;} [in X [in [Working copy] X.java [in <default> [in src [in Completion]]]]],
				}""" , requestor.getContext());
}
public void testUnspecifiedReference() throws JavaModelException { // ensure completion on ambiguous reference works and shows both types and names.
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"""
				interface I {
				    void doit(X x);
				}
				public class X {\s
					static void goo(I i) {
					}
					public static void main(String[] args) {
						goo((StringParameter) -> {
							Stri
						});
					}\s
				}
				""");

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
			"""
				interface I {
				    void doit(X x);
				}
				public class X {\s
					static void goo(I i) {
					}
					public static void main(String[] args) {
						goo((StringParameter) -> {
							StringP
						})
					}\s
				}
				""");

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
			"""
				interface I {
				    void doit(X x);
				}
				public class X {\s
				   void foo() {}
				   int field;
					static void goo(I i) {
					}
					public static void main(String[] args) {
						goo((xyz) -> xyz.)
					}\s
				}
				""");

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
			"""
				interface I {
				    void doit(X x);
				}
				public class X {\s
				   void foo() {}
				   int field;
					static void goo(I i) {
					}
					public static void main(String[] args) {
						  goo(xyz -> xyz.)
					}\s
				}
				""");

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
				"""
					public interface Foo {
						int run(int s1, int s2);
					}
					interface B {
						static Foo f = (int x5, int x2) -> bar
						static int x4 = 3;
					  	static int bars () { return 2; }
					}""");

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
				"""
					public interface Foo {
						int run(int s1, int s2);
					}
					interface B {
						static Foo f = (int x5, int x2) -> anot
						static int another = 3;
					  	static int two () { return 2; }
					}""");

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
			"""
				interface I {
					void foo();
				}
				public class X {
					I goo() {
				       int tryit = 0;
						return () -> {
							try
						};
					}
				}
				""");

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
			"""
				interface I {
					void foo();
				}
				public class X {
					I i = () -> {
						syso    // no proposals here.
					};
				}
				""");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "syso";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("", requestor.getResults());
	assertEquals("""
		completion offset=67
		completion range=[63, 66]
		completion token="syso"
		completion token kind=TOKEN_KIND_NAME
		expectedTypesSignatures=null
		expectedTypesKeys=null
		completion token location={STATEMENT_START}""", requestor.getContext());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422901, [1.8][code assist] Code assistant sensitive to scope.referenceContext type identity.
public void test422901a() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"""
				interface I {
					void foo();
				}
				public class X {
				   void foo() {
					    I i = () -> {
						    syso    // no proposals here.
					    };
				   }
				}
				""");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "syso";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("", requestor.getResults());
	assertEquals("""
		completion offset=91
		completion range=[87, 90]
		completion token="syso"
		completion token kind=TOKEN_KIND_NAME
		expectedTypesSignatures=null
		expectedTypesKeys=null
		completion token location={STATEMENT_START}""", requestor.getContext());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=426851, [1.8][content assist] content assist for a type use annotation
public void test426851() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"""
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Target;
				@Target(ElementType.TYPE_USE)
				@interface TypeUse {
				}
				@Ty
				interface I {
					default void foo() { }
				}
				""");

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
				""");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "syso";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("", requestor.getResults());
	assertEquals("""
		completion offset=160
		completion range=[156, 159]
		completion token="syso"
		completion token kind=TOKEN_KIND_NAME
		expectedTypesSignatures=null
		expectedTypesKeys=null
		completion token location={STATEMENT_START}""", requestor.getContext());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427532, [1.8][code assist] Completion engine does not like intersection casts
public void test427532a() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"""
				import java.io.Serializable;
				interface I {
					void foo();
				}
				public class X {
					public static void main(String[] args) {
						syso
						I i = (I & Serializable) () -> {};
					}
				}
				""");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "syso";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("", requestor.getResults());
	assertEquals("""
		completion offset=123
		completion range=[119, 122]
		completion token="syso"
		completion token kind=TOKEN_KIND_NAME
		expectedTypesSignatures=null
		expectedTypesKeys=null
		completion token location={STATEMENT_START}""", requestor.getContext());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427532, [1.8][code assist] Completion engine does not like intersection casts
public void test427532b() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"""
				import java.io.Serializable;
				interface I {
					void foo();
				}
				public class X {
					public static void main(String[] args) {
						I i = (I & Serializable) () -> {
				                 syso
				             };
					}
				}
				""");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "syso";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("", requestor.getResults());
	assertEquals("""
		completion offset=173
		completion range=[169, 172]
		completion token="syso"
		completion token kind=TOKEN_KIND_NAME
		expectedTypesSignatures=null
		expectedTypesKeys=null
		completion token location={STATEMENT_START}""", requestor.getContext());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427464, [1.8][content assist] CCE : MethodDeclaration incompatible with CompletionOnAnnotationOfType
public void test427464() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"""
				@interface Annotation {}
				interface FI1 {
					int foo(int x) throws Exception;
				}
				class Test {
					private void foo() {
						FI1 fi1 = (x) -> {\s
							@Ann
						};
					}
				}
				""");

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
			"""
				import java.util.List;
				class Person {
				   String getLastName() { return null; }
				}
				public class X {
					void test1 (List<Person> people) {
						people.stream().forEach(p -> System.out.println(p.get)); // NOK
					}
				}
				""");

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
				       people.sort((x,y) -> x.get);  // OK
				   }
				}
				""");

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
				       people.sort((x,y) -> x.getLastName().compareTo(y.get));
				   }
				}
				""");

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
				       people.sort((x,y) -> x.getLastName() + y.get);
				   }
				}
				""");

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
				       people.sort((x,y) -> "" + x.get);\s
				   }
				}
				""");

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
				""");

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
			"""
				import java.util.List;
				class Person {
				   String getLastName() { return null; }
				}
				public class X {
				   void test2(List<Person> people) {
				       people.sort((x,y) -> {
				              if (true) return "" + x.get;\s
				              else return "";});
				   }
				}
				""");

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
				""");

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
				"""
					interface I {
					    String foo(String x);
					}
					public class X {
					    public  String longMethodName(String x) {
					        return null;
					    }
					}
					public class Y {
					    X x;\
					    void foo()
					    {
					    	Y y = new Y();
					    	I i = y.x::longMethodN   \s
					    }
					}
					""");

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
				"""
					interface I {
					    String foo(X<String> xs, String x);
					}
					public class X<T> {
					    public  String longMethodName(String x) {
					        return null;
					    }
					    void foo() {
					    	I i = X<String>::lo
					    }
					}
					""");

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
				"""
					interface I {
					    String foo(String x);
					}
					class Y {
					    public  String longMethodName(String x) {
					        return null;
					    }
					}
					public class X extends Y {
					    void foo() {
					    	X x = new X();
					    	I i = super::lo;
					    }
					}
					""");

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
				"""
					interface I {
					    String foo(String x);
					}
					class Y {
					    public  String longMethodName(String x) {
					        return null;
					    }
					}
					public class X extends Y {
					    void foo() {
					    	X x = new X();
					    	I i = this::lo;
					    }
					}
					""");

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
				"""
					import java.util.function.Predicate;
					public class X {
						private static void writeIt(Object list) {
							list = replace(s -> true);
							Object asList = null;
							if(Boolean.TRUE) {
								Object s = removeAll(asli);
							}
						}
						private static Object replace(Predicate<String> tester) { return tester; }
						Object removeAll(Object o1) { return o1; }
					}
					""");

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
						"""
							import java.util.LinkedList;
							import java.util.List;
							public class X {
								private Map	map;
								public X() {
									map = new Map();
								}
								public LinkedList<Node> getPath(int xFrom, int yFrom, int xTo, int yTo) {
									LinkedList<Node> result = new LinkedList<>();
									Node node = null;
									int[] nodeCoords = null;
									boolean nodeAdded = false;
									if (nodeCoords != null) {
										// something
									}
									else {
										node = map.getGraph()
												.getNodes()
												.stream()
												.filter((n) -> (n.x() / 100) == (xTo / 100) && (n.y() / 100) == (yTo / 100))
												.min((n1, n2) -> (int) Math.round(Math.sqrt(Math.pow(n1.x() - xTo, 2) + Math.pow(n1.y() - yTo, 2)) - Math.sqrt(Math.pow(n2.x() - xTo, 2) + Math.pow(n2.y() - yTo, 2))))
												.get();
										nodeAdded = true;
									}
									if (nodeAdded) {
										 /*here*/remov
									}
									return result;
								}
							\t
								private void removeNodeFromGraph(Node node) {
									map.getGraph().removeNode(node.id());
								}
							\t
							\t
								public class Map {
									Graph graph = new Graph();
								\t
									public Graph getGraph() {return graph;}
								}
							\t
								public class Graph {
									List<Node> nodes;
								\t
									public List<Node> getNodes() {return nodes;}
									public void addNode(Node node) {nodes.add(node);}
									public void removeNode(Node node) {nodes.remove(node);}
									public void removeNode(int id) {nodes.remove(nodes.stream().filter(node -> id == node.id()).findFirst());}
								}
								public class Node {
									public int id() {return hashCode();}
									public int x() {return 0;}
									public int y() {return 0;}
								}
							}
							""");

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
		Map<String, String> customOptions = new HashMap<>(options);
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
					""");

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
				    	I i = x::ne
				       System.out.println();
				    }
				}
				""");

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
				    	I i = I::ne
				       System.out.println();
				    }
				}
				""");

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
			"""
				public class X {
					public static void main(String[] args) {
						new Thread(()->System.o);
					}
				}
				""");

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
			"""
				public class X {
					public static void main(String[] args) {
						new Thread(()->System.out.p);
					}
				}
				""");

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
			"""
				public class X {
					public static void main(String[] args) {
						new Thread(()->System.out.println("foo")).st);
					}
				}
				""");

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
			"""
				import java.util.Arrays;
				import java.util.List;
				public class X {
					public static void main(String[] args) {
						List<Integer> list = Arrays.asList(1, 2, 3);
						list.stream().map((x) -> x * x.h);
					}
				}
				""");

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
			"""
				import java.util.Arrays;
				import java.util.List;
				public class X {
					public static void main(String[] args) {
						List<Integer> list = Arrays.asList(1, 2, 3);
						list.stream().map((x) -> x * x.hashCode()).forEach(System.out::pri);
					}
				}
				""");

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
			"""
				import java.util.Arrays;
				import java.util.List;
				public class X {
					public static void main(String[] args) {
						List<Integer> costBeforeTax = Arrays.asList(100, 200, 300);
						   double bill = costBeforeTax.stream().map((cost) -> cost + 0.19 * cost)
						        //                        .y                   .n             .y
						      .reduce((sum, cost) -> sum.dou
					}
				}
				""");

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
			"""
				import java.util.Arrays;
				import java.util.List;
				public class X {
					public static void main(String[] args) {
						List<Integer> costBeforeTax = Arrays.asList(100, 200, 300);
						   double bill = costBeforeTax.stream().map((cost) -> cost + 0.19 * cost)
						        //                        .y                   .n             .y
						      .reduce((sum, cost) -> sum.doubleValue() + cost.doubleValue()).g
					}
				}
				""");

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
				""");

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
			"""
				import java.util.Arrays;
				import java.util.List;
				public class X {
					public static void main(String[] args) {
						List<String> words = Arrays.asList("hi", "hello", "hola", "bye", "goodbye");
						List<String> list1 = words.stream().map(so -> so.tr).collect(Collectors.toList());
					}
				}
				""");

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
			"""
				import java.util.Arrays;
				import java.util.List;
				public class X {
					public static void main(String[] args) {
						List<String> words = Arrays.asList("hi", "hello", "hola", "bye", "goodbye");
						List<String> list1 = words.stream().map((String so) -> so.tr).collect(Collectors.toList());
					}
				}
				""");

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
				""");

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
				"""
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
			"""
				class D_DemoRefactorings {
					/*HERE*/D_F
					D_FI fi1= (String value, int n) -> {
						for (int j = 0; j < n; j++) {
							System.out.println(value); 		\t
						}
					};
				}
				interface D_FI {
					void print(String value, int n);
				}
				"""
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
			"""
				import java.util.function.IntFunction;
				public class X {
					IntFunction<String> ts= Integer::toString;
				}
				""");

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
			"""
				import java.util.Arrays;
				import java.util.List;
				import java.util.stream.Collectors;
				public class Test {
					List<String> words = Arrays.asList("hi", "hello", "hola", "bye", "goodbye");
					List<String> list1 = words.stream().map(so -> so.ch).collect(Collectors.toList());
				}
				""");

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
			"""
				import java.util.Arrays;
				import java.util.List;
				public class X {
					List<Integer> list = Arrays.asList(1, 2, 3);
					List<String> list1 = list.stream().map((x) -> x * x.h).collect(Collectors.toList());
				}
				""");

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
			"""
				import java.util.Arrays;
				import java.util.List;
				public class X {
						List<Integer> list = Arrays.asList(1, 2, 3);
						Object o = list.stream().map((x) -> x * x.hashCode()).forEach(System.out::pri);
				}
				""");

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
			"""
				import java.util.Arrays;
				import java.util.List;
				public class X {
						List<Integer> costBeforeTax = Arrays.asList(100, 200, 300);
						double bill = costBeforeTax.stream().map((cost) -> cost + 0.19 * cost)
						      .reduce((sum, cost) -> sum.dou
				}
				""");

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
			"""
				import java.util.Arrays;
				import java.util.List;
				public class X {
						List<Integer> costBeforeTax = Arrays.asList(100, 200, 300);
						double bill = costBeforeTax.stream().map((cost) -> cost + 0.19 * cost)
						      .reduce((sum, cost) -> sum.doubleValue() + cost.doubleValue()).g
				}
				""");

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
			"""
				import java.util.Arrays;
				import java.util.List;
				public class X {
						List<Integer> costBeforeTax = Arrays.asList(100, 200, 300);
						double bill = costBeforeTax.stream().map((cost) -> cost + 0.19 * cost)
						      .reduce((sum, cost) -> sum.doubleValue() + cost.dou
				}
				""");

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
			"""
				package p4a;
				@FunctionalInterface
				public interface FI1<R> {
				    public R foo1();
				}
				""");
	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src/FI2.java",
			"""
				package p4a;
				@FunctionalInterface
				public interface FI2 {
				    public void foo2();
				}
				""");

	this.workingCopies[2] = getWorkingCopy(
			"/Completion/src/Test.java",
			"""
				package p4b;
				import p4a.FI1;
				public class Test {
					{
				                new FI2() {};
						FI1 fi1 = () -> new FI2() {
						    @Override
						    public void foo2() {}
						};
					}
				}
				""");

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
			"""
				interface Intf {
					void foo();
				}
				public class X implements Intf {
				    class Inner {
				        {
				            X.super.hashCode();
				        }
				    }
				    @Override
				    public void foo() {
				        Intf.su;
				    }
				}
				""");

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
			"""
				import java.io.Serializable;
				import java.util.function.Function;
				import java.util.function.Predicate;
				public final class X {
				    public static <T, R> Predicate<T> apply(Predicate<R> predicate, Function<? super T, ? extends R> function) {
					     syso
				        return (Predicate<T> & Serializable) t -> predicate.test(function.apply(t));
				    }
				}
				""");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "syso";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("", requestor.getResults());
	assertEquals("""
		completion offset=248
		completion range=[244, 247]
		completion token="syso"
		completion token kind=TOKEN_KIND_NAME
		expectedTypesSignatures=null
		expectedTypesKeys=null
		completion token location={STATEMENT_START}""", requestor.getContext());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=449358, Content assist inside lambda broken in all methods except last
public void test449358() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"""
				import java.util.Optional;
				public class LambdaBug {
					private final String field = "final field";
					void localmethod1() {
						Optional.of("test").map(s -> {
							String local;
							/*HERE*/localMeth
							return s;
						}).get();
					}
					void localmethod2() {
						Optional.of("test").map(s -> {
							String local;
							// content assist works there
							return s;
						}).get();
					}
				}
				""");

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
			"""
				import java.util.Optional;
				public class LambdaBug {
					private final String field = "final field";
					void localmethod1() {
						Optional.of("test").map(s -> {
							String local;
							return s;
						}).get();
					}
					void localmethod2() {
						Optional.of("test").map(s -> {
							String local;
							/*HERE*/localMeth
							return s;
						}).get();
					}
				}
				""");

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
			"""
				public class X {
					Integer foo(){
						I <Integer, X> i2 = (x) -> {ret /* type ctrl-space after ret */};
						return 0;
					}
					Integer bar(Integer x) { return null;}
				}
				interface I <T,R> {
					R apply(T t);
				}
				""");

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
			"""
					Integer bar(Integer x) { return null;}
				public class Y {
					Integer foo(){
						I <Integer, Y> i2 = (x) -> {/* HERE */ret /* type ctrl-space after ret */};
						return 0;
					}
				}
				interface I <T,R> {
					R apply(T t);
				}
				""");

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
			"""
				public class X {
					Integer foo(){
						I <Integer, X> i2 = (x) -> {try{} /* HERE */
						return 0;
					}
					Integer bar(Integer x) { return null;}
				}
				interface I <T,R> {
					R apply(T t);
				}
				""");

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
			"""
				public class X {
					Integer foo(){
						I <Integer, X> i2 = (x) -> {do{} /* HERE */
						return 0;
					}
					Integer bar(Integer x) { return null;}
				}
				interface I <T,R> {
					R apply(T t);
				}
				""");

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
			"""
				import java.util.ArrayList;
				import java.util.function.Supplier;
				public class X {
					public static void main(String[] args) {
						ArrayList<Supplier<Runnable>> list = new ArrayList<>();
						list.forEach((supp) -> {
							Supplier<Bug460/* HERE */>}
						});
					}
					public static class Bug460410 {\
					}\
				}
				""");
	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src/Bug460411.java",
			"""
				package abc;\
				public class Bug460411 {
				}
				""");

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
			"""
				package abc;
				import java.util.ArrayList;
				import java.util.stream.Collectors;
				public class X {
					public static void main(String[] args) {
						ArrayList<Entry> list = new ArrayList<>();
						list.stream().collect(Collectors.averagingInt(e -> e.a/* HERE */));
					}
				}
				""");
	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src/Entry.java",
			"""
				package abc;\
				public class Entry {
					public String age() {
						return "10";\
					}\
				}
				""");

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
			"""
				import java.util.function.Consumer;
				public class X {
					public void foo() {
						new Thread(() -> {
							som/*here*/
						});
					}
					public void poisonMethod() {
						ArrayList<String> views = new ArrayList<>();
						views.stream().filter(String::isEmpty).forEach(s -> s.length());
					}
					public void someMethod() {}
				}
				""");

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
			"""
				import java.util.function.Consumer;
				public class X {
					public static void main() {
						MyGeneric<String> mystring = new MyGeneric<>("");
						complete((String result) -> {
							mystring.get(res/* HERE */);
						}, new Consumer<Throwable>() {
							@Override
							public void accept(Throwable t) { t.printStackTrace(); }
						});
					}
					public static class MyGeneric<T> {
						public MyGeneric(T t) {}
						public T get(String value) { return null; }
					}
					static void complete(Consumer<String> success, Consumer<Throwable> failure) {}
				}""");

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
			"""
				import java.util.function.Consumer;
				public class X {
					public static void main() {
						MyGeneric<String> mystring = new MyGeneric<>("");
						complete((String result) -> {
							mystring.get(res/* HERE */);
						}, t -> t.printStackTrace());
					}
					public static class MyGeneric<T> {
						public MyGeneric(T t) {}
						public T get(String value) { return null; }
					}
					static void complete(Consumer<String> success, Consumer<Throwable> failure) {}
				}""");

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
			"""
				import java.util.function.Consumer;
				public class X {
					public static void main() {
						MyGeneric<String> mystring = new MyGeneric<>("");
						complete((String result) -> {
							mystring.get(res/* HERE */);
						}, t -> {t.printStackTrace();});
					}
					public static class MyGeneric<T> {
						public MyGeneric(T t) {}
						public T get(String value) { return null; }
					}
					static void complete(Consumer<String> success, Consumer<Throwable> failure) {}
				}""");

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
			"""
				import java.util.function.Consumer;
				public class X {
					public static void main() {
						MyGeneric<String> mystring = new MyGeneric<>("");
						complete((String result) -> {
							mystring.get(result);
							Consumer<String> success = (String result2) -> {
								mystring.get(res/* HERE */);
								};
						}, new Consumer<Throwable>() {
							@Override
							public void accept(Throwable t) {
								t.printStackTrace();
							}
						});
					}
					public static class MyGeneric<T> {
						public MyGeneric(T t) {}
						public T get(String value) { return null; }
					}
					static void complete(Consumer<String> success, Consumer<Throwable> failure) {}
				}
				""");

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
			"""
				interface Supplier<T> {
				   T get();
				}
				public interface Bar {
				    static public Bar print() {
				        return null;
				    }
				}
				class A {
				    	Supplier<Bar> c = Bar::pr
				}
				""");

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
			"""
				interface FooFunctional {
				   void function();
				}
				public class Foo {
				    private FooFunctional lambda = this::bar;
				    public void bar() {
				      new StringBuffer\
				    }
				}
				""");

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
			"""
				interface FooFunctional {
				   void function();
				}
				public class Foo {
				    public void bar() {
				      private FooFunctional lambda = this::bar;
				      new StringBuffer\
				    }
				}
				""");

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
			"""
				interface FooFunctional {
				   void function();
				}
				public class Foo {
				    public void bar() {
				      private FooFunctional lambda = () -> bar();
				      new StringBuffer\
				    }
				}
				""");

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
			"""
				public class CC extends S1 {
					private int i = disp
				}
				abstract class S1 implements I1 {}
				interface I1 extends I2 {}
				interface I2 {
					default int dispose() {
						return 0;
					}
					default void disperse() {}
				}
				""");

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
			"""
				public class X {
					public interface SomeInterface {
						public void someMethod(String builder);
				}
					public enum SomeEnum {
						SOME_ENUM((String bui) -> {
							bui.toCh
						});
						SomeEnum(SomeInterface callable) {}
					}
				}
				""");

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
			"""
				public class X {
					public interface SomeInterface {
						public void someMethod(StringBuilder builder);
				}
					public enum SomeEnum {
						SOME_ENUM((StringBui bui) -> {
						});
						SomeEnum(SomeInterface callable) {}
					}
				}
				""");

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
 */
public void _test492947c() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"""
				public class X {
					public interface SomeInterface {
						public void someMethod(StringBuilder builder);
				}
					public enum SomeEnum {
						SOME_ENUM((StringBui) -> {
						});
						SomeEnum(SomeInterface callable) {}
					}
				}
				""");

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
			"""
				public class X {
					public Main(SomeInterface arg) {}
					public interface SomeInterface {
						public void someMethod(StringBuilder builder);
				}
					Main m = new Main((StringBui) -> {
						});
				}
				""");

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
			"""
				package b493705;
				
				import java.util.function.BiFunction;
				
				class Control { }
				class Composite extends Control { }
				class Label extends Control {
					public Label(Composite p, int i) {}
				}
				
				class Viewer { }
				interface ViewerSupplier {
					ViewerUI<? extends Viewer> getViewerUI();
				}
				class ViewerUI<V extends Viewer> extends SwtUI<Control>{
				
				}
				interface ControlSupplier {
					SwtUI<? extends Control> getControlUI();
				}
				class SwtUI<T> {
					public SwtUI<T> child(ControlSupplier supplier) {
						return null;
					}
					public SwtUI<T> child(ViewerSupplier supplier) {
						return null;
					}
					public static <T extends Control> SwtUI<T> create(BiFunction<Composite, Integer, T> ctor) {
						return null;
					}
					public SwtUI<T> text(String text) {
						return null;
					}
				}
				public class HelloWorld {
					void test(SwtUI<Composite> root) {
						root.child(() -> SwtUI.create(Label::new)
								.text("Selection").
								);
					}
				}
				""");

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
			"""
				public class X {
					void test() {
						new Thread(() -> sysout);
					}
				}
				""");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "sysout";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("", requestor.getResults());
	assertEquals("""
		completion offset=57
		completion range=[51, 56]
		completion token="sysout"
		completion token kind=TOKEN_KIND_NAME
		expectedTypesSignatures=null
		expectedTypesKeys=null
		completion token location={STATEMENT_START}""", requestor.getContext());
}
//https://bugs.eclipse.org/485492
public void test485492a() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/Foo.java",
			"""
				import java.util.function.Function;
				public enum Foo {
					BAR((z) -> {
					z.has
						return z;
					});
					Foo(Function<String, String> func) { }
				}
				""");

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
			"""
				import java.util.function.Function;
				public enum Foo {
					BAR((zilch) -> {
						return zil;
					});
					Foo(Function<String, String> func) { }
				}
				""");

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
			"""
				import java.util.function.Function;
				public enum Foo {
					BAR((z) -> {
						return z.has;
					});
					Foo(Function<String, String> func) { }
				}
				""");

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
		"""
			public class X {
				final String zzz = "z";
				void foo(String s){
					switch(s) {
						case zz
					}
				}
			}
			""");

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
		"""
			public class X {
				static final String zzz = "z";
				void foo(String s){
					switch(s) {
						case zz
					}
				}
			}
			""");

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
		"""
			package p;
			public class SuperSuper {}
			class Super extends SuperSuper {}
			class Y {
				static class Super {}
			}
			class X extends Sup {
			}
			""");

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
		"""
			class Foo {
			    Runnable foo() {
			        return () -> new Object() {
			            // press Ctrl+Space before the comment
			        };
			    }
			   \s
			    static void bar() { /**/ }
			}
			""");

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
		"""
			import static java.util.stream.Collectors.toList;
			import java.util.List;
			
			public class Test {
				void foo(List<Object> list) {
					bar(list.stream().map(m -> new Object() {
						// here
					}).collect(toList()));
				}
			
				private void bar(List<Object> collect) {
				}
			}
			""");

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
			"""
				public class Foo {
					 enum MyEnum {
						  FOO, BAR
						}
					public void setMyEnumValue(MyEnum myEnumValue) {
					}
					public void meth() {
						this.setMyEnumValue(new String().isEmpty() ? MyEnum.FOO:BAR);
					    }
				}
				""");

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
			"""
				public class EnumRelatedCompletions {
					 enum MyEnum {
						  FOO, BAR, QUZ\s
						}
					public void setMyEnumValue(MyEnum myEnumValue) {
					}
					public void meth() {
						this.setMyEnumValue(new String().isEmpty() ? MyEnum.FOO:BAR);
					    MyEnum e= MyEnum.FOO;
					    if(e  !=QUZ) {    \t
					    }
					    }
				}
				""");

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
				"""
					package test;
					public class FooNPE {
						public static void main(String[] args) {\t
							java.util.function.Consumer<Object> consumer = object -> {new SomeClass().something(obj -> {/*nop*/}).
							};
						}
					class SomeClass {
					public void something(java.util.function.Consumer<Object> otherConsumer) {
					 }
					}
					}
					""");
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
			"""
				package test;
				public class FooNPE {
					public static void main(String[] args) {\t
						java.util.function.Consumer<Object> consumer = object -> {new SomeClass().something(obj -> {}).
						};
					}
				class SomeClass {
				public Object something(java.util.function.Consumer<Object> otherConsumer) {
				return new Object();\s
				 }
				}
				}
				""");
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
			"""
				package p;
				import java.util.stream.Stream;
				import java.util.Optional;
				interface ProcessHandle {
					static Stream<ProcessHandle> allProcesses();
					Info info();
				}
				interface Info {
					Optional<String> command();
				}
				public class Test {
					void foo() {
						ProcessHandle.allProcesses().forEach(p -> {
							p.info().command().ifPresent(o -> {
								System.out.println(o);
							}).
						});\
					}
				}
				""");

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
			"""
				package p;
				public class Test {
					public Test(Runnable run) {}
				}
				""");
	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src/p/Test.java",
			"""
				package p;
				public class Main {
					public void myTestOfStackOverflow() {
						() -> {
							new Test(() -> {}).
						}
					}
				}
				""");

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
			"""
				package p;
				import java.util.Arrays;
				import java.util.function.Consumer;
				
				public class Snippet29 {
				
				class Display {
					public void asyncExec(Runnable runnable) { }
				}
				class Shell {
					Shell(Display display) {}
					public Shell(Shell shell, int i) { }
					public void setLayout(GridLayout gridLayout) { }
					public void setText(String string) { }
					public void pack() { }
					public Point getLocation() { return null; }
					public void open() { }
					public void close() { }
					public void setLocation(int i, int j) { }
				}
				class Point {
					int x, y;
				}
				class GridLayout {
					public GridLayout() { }
					public GridLayout(int i, boolean b) { }
				}
				class GridData {
					public GridData(int fill, int fill2, boolean b, boolean c, int i, int j) { }
					public GridData(int fill, int fill2, boolean b, boolean c) { }
				}
				class Widget {
					public void setText(String string) { }
					public void setLayoutData(GridData gridData) { }
				}
				class Button extends Widget {
					Button(Shell shell, int style) { }
					public void addListener(int selection, Consumer<Event> listener) { }
				}
				class Label extends Widget {
					public Label(Shell dialog, int none) { }
				}
				class Event {}
				class SWT {
					public static final int PUSH = 1;
					public static final int Selection = 2;
					protected static final int DIALOG_TRIM = 3;
					protected static final int APPLICATION_MODAL = 4;
					protected static final int NONE = 5;
					protected static final int FILL = 6;
				}
				class Timer {
					public void schedule(TimerTask timerTask, int i) { }
				}
				abstract class TimerTask implements Runnable {}
				public static void main (String [] args) {
					Display display = new Display ();
					Shell shell = new Shell (display);
					shell.setLayout(new GridLayout());
					Button b = new Button(shell, SWT.PUSH);
					b.setText("Open dialog in 3s");
					b.addListener(SWT.Selection, e -> {
						new Timer().schedule(new TimerTask() {
							@Override
							public void run() {
								display.asyncExec(new Runnable() {
									@Override
									public void run() {
										Shell dialog = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
										dialog.setText("Question");
										dialog.setLayout(new GridLayout(3, true));
										Label label = new Label(dialog, SWT.NONE);
										label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
										label.setText("Do you really want to clear the runtime workspace?");
										Arrays.asList("Yes", "No", "Cancel").forEach(t -> {
											Button button = new Button(dialog, SWT.PUSH);
											button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
											button.setText(t);
											button.addListener(SWT.Selection, e -> { dialog.close(); });
										});
										dialog.pack();
										dialog.setLocation(shell.getLocation().x + 40, shell.getLocation().y + 80);
										dialog.open();
									}
								}).;
							}
						}, 2000);
					});
				}
				
				}\s
				""");

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
			"""
				import java.util.concurrent.CompletableFuture;
				
				public class ShowSOEInEclipseMars2 {
				\s
				public void crashWithStackOverflowError() {
				  \s
				 CompletableFuture<Double> intermediate = CompletableFuture.supplyAsync(() -> {
				  try {
				   CompletableFuture.supplyAsync(() -> { return 0D; }).;
				  } catch (Exception e) {
				  }
				  return 1D;
				 });
				 }
				}
				""");
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
			"""
				import java.util.concurrent.CompletableFuture;
				
				public class ShowSOEInEclipseMars2 {
				\s
				public void crashWithStackOverflowError() {
				  \s
				 CompletableFuture<Double> intermediate = CompletableFuture.supplyAsync(() -> {
				  try {
				   CompletableFuture.supplyAsync(() -> { return 0D; }).a;
				  } catch (Exception e) {
				  }
				  return 1D;
				 });
				 }
				}
				""");
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
            """
				import java.util.Queue;
				
				public class Foo {
					public void foo () {
						Queue<String> res = new LinkedBlockingQueue<>();
					}
				}""");

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
            """
				import java.util.List;
				
				public class Bug570593 {
					private List<XBug570593>
				}""");
	this.workingCopies[1] = getWorkingCopy(
            "/Completion/src/XBug570593Type.java",
            """
				
				public class XBug570593Type {
				}""");

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
            """
				import java.util.Map;
				
				public class Bug570593 {
					private Map<XBug570593,V>
				}""");
	this.workingCopies[1] = getWorkingCopy(
            "/Completion/src/XBug570593Type.java",
            """
				
				public class XBug570593Type {
				}""");

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
            """
				import java.util.Map;
				
				public class Bug570593 {
					private Map<Long,XBug570593>
				}""");
	this.workingCopies[1] = getWorkingCopy(
            "/Completion/src/XBug570593Type.java",
            """
				
				public class XBug570593Type {
				}""");

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
            """
				import java.util.List;
				
				public class Bug570593 {
					private List<List<XBug570593>>
				}""");
	this.workingCopies[1] = getWorkingCopy(
            "/Completion/src/XBug570593Type.java",
            """
				
				public class XBug570593Type {
				}""");

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
            """
				import java.util.List;
				import java.util.Map;
				
				public class Bug570593 {
					private List<Map<XBug570593,V>>
				}""");
	this.workingCopies[1] = getWorkingCopy(
            "/Completion/src/XBug570593Type.java",
            """
				
				public class XBug570593Type {
				}""");

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
            """
				import java.util.List;
				import java.util.Map;
				
				public class Bug570593 {
					private List<Map<Long,XBug570593>>
				}""");
	this.workingCopies[1] = getWorkingCopy(
            "/Completion/src/XBug570593Type.java",
            """
				
				public class XBug570593Type {
				}""");

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
            """
				import java.util.List;
				import java.util.Map;
				
				public class Bug570593 {
					private Map<List<XBug570593>,V>
				}""");
	this.workingCopies[1] = getWorkingCopy(
            "/Completion/src/XBug570593Type.java",
            """
				
				public class XBug570593Type {
				}""");

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
            """
				import java.util.List;
				import java.util.Map;
				
				public class Bug570593 {
					private Map<Long,List<XBug570593>>
				}""");
	this.workingCopies[1] = getWorkingCopy(
            "/Completion/src/XBug570593Type.java",
            """
				
				public class XBug570593Type {
				}""");

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
            """
				import java.util.List;
				import java.util.Map;
				
				public class Bug570593 {
					private Map<Long,Map<XBug570593,R>>
				}""");
	this.workingCopies[1] = getWorkingCopy(
            "/Completion/src/XBug570593Type.java",
            """
				
				public class XBug570593Type {
				}""");

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
            """
				import java.util.List;
				import java.util.Map;
				
				public class Bug570593 {
					private Map<Long,Map<String,XBug570593>>
				}""");
	this.workingCopies[1] = getWorkingCopy(
            "/Completion/src/XBug570593Type.java",
            """
				
				public class XBug570593Type {
				}""");

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
            """
				import java.util.List;
				
				public class Bug572315 {
					private List<String>\s
					@Deprecated\s
					private void test(){\s
					}\s
				}""");

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
            """
				import java.util.List;
				
				public class Bug572315 {
					private List<String>\s
					@SuppressWarnings({"unchecked"})\s
					private void test(){\s
					}\s
				}""");

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
            """
				import java.util.List;
				
				public class Bug572315 {
					private List<String>\s
				}""");

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
            """
				import java.util.List;
				
				public class Bug572315 {
					private List<String>\s
					private int count;\
				}""");

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
            """
				import java.util.List;
				
				public class Bug572315 {
					private List<String>\s
					@Deprecated\s
					private int count;\
				}""");

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
            """
				import java.util.List;
				
				public class Bug572315 {
					private List<String> ;
					@Deprecated\s
					private int count;\
				}""");

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
            """
				import java.util.List;
				
				public class Bug572315 {
					@Deprecated\s
					private List<String>\s
					@Deprecated\s
					private int count;\
				}""");

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
            """
				import java.util.List;
				
				public class Bug572315 {
				   private void foo() {
				   List<String>\s
				   @Deprecated()
				   Integer age;
				   }
				}""");

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
		"""
			@FunctionalInterface
			public interface Callback<P,R> {
			    public R call(P param);
			}
			""");
	this.workingCopies[0] = getWorkingCopy(
		"Completion/src/EnumLambdaFreeze.java",
		"""
			public enum EnumLambdaFreeze {
				k1( s_arg -> {
					// freezes as soon as i'm typing a dot after s_arg
					s_arg.
					return( "" );
				}, s_arg -> {
					return( "" );
				} ),
				k2( s_arg -> s_arg, s_arg -> s_arg );
			\t
				private EnumLambdaFreeze( Callback<String, String> callback1,\s
			                                  Callback<String, String> callback2 ){ }
			}
			""");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "s_arg.";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    assertResults(
            """
				CASE_INSENSITIVE_ORDER[FIELD_REF]{CASE_INSENSITIVE_ORDER, Ljava.lang.String;, Ljava.util.Comparator<Ljava.lang.String;>;, CASE_INSENSITIVE_ORDER, null, 49}
				copyValueOf[METHOD_REF]{copyValueOf(), Ljava.lang.String;, ([C)Ljava.lang.String;, copyValueOf, (arg0), 49}
				copyValueOf[METHOD_REF]{copyValueOf(), Ljava.lang.String;, ([CII)Ljava.lang.String;, copyValueOf, (arg0, arg1, arg2), 49}
				format[METHOD_REF]{format(), Ljava.lang.String;, (Ljava.lang.String;[Ljava.lang.Object;)Ljava.lang.String;, format, (arg0, arg1), 49}
				format[METHOD_REF]{format(), Ljava.lang.String;, (Ljava.util.Locale;Ljava.lang.String;[Ljava.lang.Object;)Ljava.lang.String;, format, (arg0, arg1, arg2), 49}
				join[METHOD_REF]{join(), Ljava.lang.String;, (Ljava.lang.CharSequence;Ljava.lang.Iterable<+Ljava.lang.CharSequence;>;)Ljava.lang.String;, join, (arg0, arg1), 49}
				join[METHOD_REF]{join(), Ljava.lang.String;, (Ljava.lang.CharSequence;[Ljava.lang.CharSequence;)Ljava.lang.String;, join, (arg0, arg1), 49}
				valueOf[METHOD_REF]{valueOf(), Ljava.lang.String;, (C)Ljava.lang.String;, valueOf, (arg0), 49}
				valueOf[METHOD_REF]{valueOf(), Ljava.lang.String;, (D)Ljava.lang.String;, valueOf, (arg0), 49}
				valueOf[METHOD_REF]{valueOf(), Ljava.lang.String;, (F)Ljava.lang.String;, valueOf, (arg0), 49}
				valueOf[METHOD_REF]{valueOf(), Ljava.lang.String;, (I)Ljava.lang.String;, valueOf, (arg0), 49}
				valueOf[METHOD_REF]{valueOf(), Ljava.lang.String;, (J)Ljava.lang.String;, valueOf, (arg0), 49}
				valueOf[METHOD_REF]{valueOf(), Ljava.lang.String;, (Ljava.lang.Object;)Ljava.lang.String;, valueOf, (arg0), 49}
				valueOf[METHOD_REF]{valueOf(), Ljava.lang.String;, (Z)Ljava.lang.String;, valueOf, (arg0), 49}
				valueOf[METHOD_REF]{valueOf(), Ljava.lang.String;, ([C)Ljava.lang.String;, valueOf, (arg0), 49}
				valueOf[METHOD_REF]{valueOf(), Ljava.lang.String;, ([CII)Ljava.lang.String;, valueOf, (arg0, arg1, arg2), 49}
				charAt[METHOD_REF]{charAt(), Ljava.lang.String;, (I)C, charAt, (arg0), 60}
				chars[METHOD_REF]{chars(), Ljava.lang.CharSequence;, ()Ljava.util.stream.IntStream;, chars, null, 60}
				clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, 60}
				codePointAt[METHOD_REF]{codePointAt(), Ljava.lang.String;, (I)I, codePointAt, (arg0), 60}
				codePointBefore[METHOD_REF]{codePointBefore(), Ljava.lang.String;, (I)I, codePointBefore, (arg0), 60}
				codePointCount[METHOD_REF]{codePointCount(), Ljava.lang.String;, (II)I, codePointCount, (arg0, arg1), 60}
				codePoints[METHOD_REF]{codePoints(), Ljava.lang.CharSequence;, ()Ljava.util.stream.IntStream;, codePoints, null, 60}
				compareTo[METHOD_REF]{compareTo(), Ljava.lang.String;, (Ljava.lang.String;)I, compareTo, (arg0), 60}
				compareToIgnoreCase[METHOD_REF]{compareToIgnoreCase(), Ljava.lang.String;, (Ljava.lang.String;)I, compareToIgnoreCase, (arg0), 60}
				concat[METHOD_REF]{concat(), Ljava.lang.String;, (Ljava.lang.String;)Ljava.lang.String;, concat, (arg0), 60}
				contains[METHOD_REF]{contains(), Ljava.lang.String;, (Ljava.lang.CharSequence;)Z, contains, (arg0), 60}
				contentEquals[METHOD_REF]{contentEquals(), Ljava.lang.String;, (Ljava.lang.CharSequence;)Z, contentEquals, (arg0), 60}
				contentEquals[METHOD_REF]{contentEquals(), Ljava.lang.String;, (Ljava.lang.StringBuffer;)Z, contentEquals, (arg0), 60}
				endsWith[METHOD_REF]{endsWith(), Ljava.lang.String;, (Ljava.lang.String;)Z, endsWith, (arg0), 60}
				equals[METHOD_REF]{equals(), Ljava.lang.String;, (Ljava.lang.Object;)Z, equals, (arg0), 60}
				equalsIgnoreCase[METHOD_REF]{equalsIgnoreCase(), Ljava.lang.String;, (Ljava.lang.String;)Z, equalsIgnoreCase, (arg0), 60}
				finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, 60}
				getBytes[METHOD_REF]{getBytes(), Ljava.lang.String;, ()[B, getBytes, null, 60}
				getBytes[METHOD_REF]{getBytes(), Ljava.lang.String;, (II[BI)V, getBytes, (arg0, arg1, arg2, arg3), 60}
				getBytes[METHOD_REF]{getBytes(), Ljava.lang.String;, (Ljava.lang.String;)[B, getBytes, (arg0), 60}
				getBytes[METHOD_REF]{getBytes(), Ljava.lang.String;, (Ljava.nio.charset.Charset;)[B, getBytes, (arg0), 60}
				getChars[METHOD_REF]{getChars(), Ljava.lang.String;, (II[CI)V, getChars, (arg0, arg1, arg2, arg3), 60}
				getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<*>;, getClass, null, 60}
				hashCode[METHOD_REF]{hashCode(), Ljava.lang.String;, ()I, hashCode, null, 60}
				indexOf[METHOD_REF]{indexOf(), Ljava.lang.String;, (I)I, indexOf, (arg0), 60}
				indexOf[METHOD_REF]{indexOf(), Ljava.lang.String;, (II)I, indexOf, (arg0, arg1), 60}
				indexOf[METHOD_REF]{indexOf(), Ljava.lang.String;, (Ljava.lang.String;)I, indexOf, (arg0), 60}
				indexOf[METHOD_REF]{indexOf(), Ljava.lang.String;, (Ljava.lang.String;I)I, indexOf, (arg0, arg1), 60}
				intern[METHOD_REF]{intern(), Ljava.lang.String;, ()Ljava.lang.String;, intern, null, 60}
				isEmpty[METHOD_REF]{isEmpty(), Ljava.lang.String;, ()Z, isEmpty, null, 60}
				lastIndexOf[METHOD_REF]{lastIndexOf(), Ljava.lang.String;, (I)I, lastIndexOf, (arg0), 60}
				lastIndexOf[METHOD_REF]{lastIndexOf(), Ljava.lang.String;, (II)I, lastIndexOf, (arg0, arg1), 60}
				lastIndexOf[METHOD_REF]{lastIndexOf(), Ljava.lang.String;, (Ljava.lang.String;)I, lastIndexOf, (arg0), 60}
				lastIndexOf[METHOD_REF]{lastIndexOf(), Ljava.lang.String;, (Ljava.lang.String;I)I, lastIndexOf, (arg0, arg1), 60}
				length[METHOD_REF]{length(), Ljava.lang.String;, ()I, length, null, 60}
				matches[METHOD_REF]{matches(), Ljava.lang.String;, (Ljava.lang.String;)Z, matches, (arg0), 60}
				notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, 60}
				notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, 60}
				offsetByCodePoints[METHOD_REF]{offsetByCodePoints(), Ljava.lang.String;, (II)I, offsetByCodePoints, (arg0, arg1), 60}
				regionMatches[METHOD_REF]{regionMatches(), Ljava.lang.String;, (ILjava.lang.String;II)Z, regionMatches, (arg0, arg1, arg2, arg3), 60}
				regionMatches[METHOD_REF]{regionMatches(), Ljava.lang.String;, (ZILjava.lang.String;II)Z, regionMatches, (arg0, arg1, arg2, arg3, arg4), 60}
				replace[METHOD_REF]{replace(), Ljava.lang.String;, (CC)Ljava.lang.String;, replace, (arg0, arg1), 60}
				replace[METHOD_REF]{replace(), Ljava.lang.String;, (Ljava.lang.CharSequence;Ljava.lang.CharSequence;)Ljava.lang.String;, replace, (arg0, arg1), 60}
				replaceAll[METHOD_REF]{replaceAll(), Ljava.lang.String;, (Ljava.lang.String;Ljava.lang.String;)Ljava.lang.String;, replaceAll, (arg0, arg1), 60}
				replaceFirst[METHOD_REF]{replaceFirst(), Ljava.lang.String;, (Ljava.lang.String;Ljava.lang.String;)Ljava.lang.String;, replaceFirst, (arg0, arg1), 60}
				split[METHOD_REF]{split(), Ljava.lang.String;, (Ljava.lang.String;)[Ljava.lang.String;, split, (arg0), 60}
				split[METHOD_REF]{split(), Ljava.lang.String;, (Ljava.lang.String;I)[Ljava.lang.String;, split, (arg0, arg1), 60}
				startsWith[METHOD_REF]{startsWith(), Ljava.lang.String;, (Ljava.lang.String;)Z, startsWith, (arg0), 60}
				startsWith[METHOD_REF]{startsWith(), Ljava.lang.String;, (Ljava.lang.String;I)Z, startsWith, (arg0, arg1), 60}
				subSequence[METHOD_REF]{subSequence(), Ljava.lang.String;, (II)Ljava.lang.CharSequence;, subSequence, (arg0, arg1), 60}
				substring[METHOD_REF]{substring(), Ljava.lang.String;, (I)Ljava.lang.String;, substring, (arg0), 60}
				substring[METHOD_REF]{substring(), Ljava.lang.String;, (II)Ljava.lang.String;, substring, (arg0, arg1), 60}
				toCharArray[METHOD_REF]{toCharArray(), Ljava.lang.String;, ()[C, toCharArray, null, 60}
				toLowerCase[METHOD_REF]{toLowerCase(), Ljava.lang.String;, ()Ljava.lang.String;, toLowerCase, null, 60}
				toLowerCase[METHOD_REF]{toLowerCase(), Ljava.lang.String;, (Ljava.util.Locale;)Ljava.lang.String;, toLowerCase, (arg0), 60}
				toString[METHOD_REF]{toString(), Ljava.lang.String;, ()Ljava.lang.String;, toString, null, 60}
				toUpperCase[METHOD_REF]{toUpperCase(), Ljava.lang.String;, ()Ljava.lang.String;, toUpperCase, null, 60}
				toUpperCase[METHOD_REF]{toUpperCase(), Ljava.lang.String;, (Ljava.util.Locale;)Ljava.lang.String;, toUpperCase, (arg0), 60}
				trim[METHOD_REF]{trim(), Ljava.lang.String;, ()Ljava.lang.String;, trim, null, 60}
				wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, 60}
				wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, wait, (millis), 60}
				wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), 60}""",
            requestor.getResults());
}
public void testBug539685a() throws Exception {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"Completion/src/ReproduceHang.java",
		"""
			import java.util.Objects;
			import java.util.function.Function;
			
			public class ReproduceHang {
			    public static void main(String[] args) {
			        Function<String, Object> localVar = (value -> new Object() {
			            private final int i = Objects.requireNull(1);
			        });
			    }
			}
			""");
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "Objects.r";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    assertResults(
            """
				requireNonNull[METHOD_REF]{requireNonNull, Ljava.util.Objects;, <T:Ljava.lang.Object;>(TT;)TT;, requireNonNull, (arg0), 51}
				requireNonNull[METHOD_REF]{requireNonNull, Ljava.util.Objects;, <T:Ljava.lang.Object;>(TT;Ljava.lang.String;)TT;, requireNonNull, (arg0, arg1), 51}
				requireNonNull[METHOD_REF]{requireNonNull, Ljava.util.Objects;, <T:Ljava.lang.Object;>(TT;Ljava.util.function.Supplier<Ljava.lang.String;>;)TT;, requireNonNull, (arg0, arg1), 51}""",
            requestor.getResults());
}
public void testBug558530() throws Exception {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/Bug558530.java",
            """
				import java.util.function.Function;
				
				public class LambdaCrash {
				
				    public enum Problem {
				        ONE(s -> s.trim())
				        TWO(k -> k.\
				        ;
				
				        private final Function<String, String> function;
				
				        private Problem(Function<String, String> function) {
				            this.function = function;
				        }
				
				    }
				
				}""");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "k -> k.";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    assertResults(
            """
				CASE_INSENSITIVE_ORDER[FIELD_REF]{CASE_INSENSITIVE_ORDER, Ljava.lang.String;, Ljava.util.Comparator<Ljava.lang.String;>;, CASE_INSENSITIVE_ORDER, null, 49}
				finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, 55}
				getBytes[METHOD_REF]{getBytes(), Ljava.lang.String;, (II[BI)V, getBytes, (arg0, arg1, arg2, arg3), 55}
				getChars[METHOD_REF]{getChars(), Ljava.lang.String;, (II[CI)V, getChars, (arg0, arg1, arg2, arg3), 55}
				notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, 55}
				notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, 55}
				wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, 55}
				wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, wait, (millis), 55}
				wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), 55}
				charAt[METHOD_REF]{charAt(), Ljava.lang.String;, (I)C, charAt, (arg0), 60}
				chars[METHOD_REF]{chars(), Ljava.lang.CharSequence;, ()Ljava.util.stream.IntStream;, chars, null, 60}
				clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, 60}
				codePointAt[METHOD_REF]{codePointAt(), Ljava.lang.String;, (I)I, codePointAt, (arg0), 60}
				codePointBefore[METHOD_REF]{codePointBefore(), Ljava.lang.String;, (I)I, codePointBefore, (arg0), 60}
				codePointCount[METHOD_REF]{codePointCount(), Ljava.lang.String;, (II)I, codePointCount, (arg0, arg1), 60}
				codePoints[METHOD_REF]{codePoints(), Ljava.lang.CharSequence;, ()Ljava.util.stream.IntStream;, codePoints, null, 60}
				compareTo[METHOD_REF]{compareTo(), Ljava.lang.String;, (Ljava.lang.String;)I, compareTo, (arg0), 60}
				compareToIgnoreCase[METHOD_REF]{compareToIgnoreCase(), Ljava.lang.String;, (Ljava.lang.String;)I, compareToIgnoreCase, (arg0), 60}
				contains[METHOD_REF]{contains(), Ljava.lang.String;, (Ljava.lang.CharSequence;)Z, contains, (arg0), 60}
				contentEquals[METHOD_REF]{contentEquals(), Ljava.lang.String;, (Ljava.lang.CharSequence;)Z, contentEquals, (arg0), 60}
				contentEquals[METHOD_REF]{contentEquals(), Ljava.lang.String;, (Ljava.lang.StringBuffer;)Z, contentEquals, (arg0), 60}
				endsWith[METHOD_REF]{endsWith(), Ljava.lang.String;, (Ljava.lang.String;)Z, endsWith, (arg0), 60}
				equals[METHOD_REF]{equals(), Ljava.lang.String;, (Ljava.lang.Object;)Z, equals, (arg0), 60}
				equalsIgnoreCase[METHOD_REF]{equalsIgnoreCase(), Ljava.lang.String;, (Ljava.lang.String;)Z, equalsIgnoreCase, (arg0), 60}
				getBytes[METHOD_REF]{getBytes(), Ljava.lang.String;, ()[B, getBytes, null, 60}
				getBytes[METHOD_REF]{getBytes(), Ljava.lang.String;, (Ljava.lang.String;)[B, getBytes, (arg0), 60}
				getBytes[METHOD_REF]{getBytes(), Ljava.lang.String;, (Ljava.nio.charset.Charset;)[B, getBytes, (arg0), 60}
				getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<*>;, getClass, null, 60}
				hashCode[METHOD_REF]{hashCode(), Ljava.lang.String;, ()I, hashCode, null, 60}
				indexOf[METHOD_REF]{indexOf(), Ljava.lang.String;, (I)I, indexOf, (arg0), 60}
				indexOf[METHOD_REF]{indexOf(), Ljava.lang.String;, (II)I, indexOf, (arg0, arg1), 60}
				indexOf[METHOD_REF]{indexOf(), Ljava.lang.String;, (Ljava.lang.String;)I, indexOf, (arg0), 60}
				indexOf[METHOD_REF]{indexOf(), Ljava.lang.String;, (Ljava.lang.String;I)I, indexOf, (arg0, arg1), 60}
				isEmpty[METHOD_REF]{isEmpty(), Ljava.lang.String;, ()Z, isEmpty, null, 60}
				lastIndexOf[METHOD_REF]{lastIndexOf(), Ljava.lang.String;, (I)I, lastIndexOf, (arg0), 60}
				lastIndexOf[METHOD_REF]{lastIndexOf(), Ljava.lang.String;, (II)I, lastIndexOf, (arg0, arg1), 60}
				lastIndexOf[METHOD_REF]{lastIndexOf(), Ljava.lang.String;, (Ljava.lang.String;)I, lastIndexOf, (arg0), 60}
				lastIndexOf[METHOD_REF]{lastIndexOf(), Ljava.lang.String;, (Ljava.lang.String;I)I, lastIndexOf, (arg0, arg1), 60}
				length[METHOD_REF]{length(), Ljava.lang.String;, ()I, length, null, 60}
				matches[METHOD_REF]{matches(), Ljava.lang.String;, (Ljava.lang.String;)Z, matches, (arg0), 60}
				offsetByCodePoints[METHOD_REF]{offsetByCodePoints(), Ljava.lang.String;, (II)I, offsetByCodePoints, (arg0, arg1), 60}
				regionMatches[METHOD_REF]{regionMatches(), Ljava.lang.String;, (ILjava.lang.String;II)Z, regionMatches, (arg0, arg1, arg2, arg3), 60}
				regionMatches[METHOD_REF]{regionMatches(), Ljava.lang.String;, (ZILjava.lang.String;II)Z, regionMatches, (arg0, arg1, arg2, arg3, arg4), 60}
				split[METHOD_REF]{split(), Ljava.lang.String;, (Ljava.lang.String;)[Ljava.lang.String;, split, (arg0), 60}
				split[METHOD_REF]{split(), Ljava.lang.String;, (Ljava.lang.String;I)[Ljava.lang.String;, split, (arg0, arg1), 60}
				startsWith[METHOD_REF]{startsWith(), Ljava.lang.String;, (Ljava.lang.String;)Z, startsWith, (arg0), 60}
				startsWith[METHOD_REF]{startsWith(), Ljava.lang.String;, (Ljava.lang.String;I)Z, startsWith, (arg0, arg1), 60}
				subSequence[METHOD_REF]{subSequence(), Ljava.lang.String;, (II)Ljava.lang.CharSequence;, subSequence, (arg0, arg1), 60}
				toCharArray[METHOD_REF]{toCharArray(), Ljava.lang.String;, ()[C, toCharArray, null, 60}
				copyValueOf[METHOD_REF]{copyValueOf(), Ljava.lang.String;, ([C)Ljava.lang.String;, copyValueOf, (arg0), 79}
				copyValueOf[METHOD_REF]{copyValueOf(), Ljava.lang.String;, ([CII)Ljava.lang.String;, copyValueOf, (arg0, arg1, arg2), 79}
				format[METHOD_REF]{format(), Ljava.lang.String;, (Ljava.lang.String;[Ljava.lang.Object;)Ljava.lang.String;, format, (arg0, arg1), 79}
				format[METHOD_REF]{format(), Ljava.lang.String;, (Ljava.util.Locale;Ljava.lang.String;[Ljava.lang.Object;)Ljava.lang.String;, format, (arg0, arg1, arg2), 79}
				join[METHOD_REF]{join(), Ljava.lang.String;, (Ljava.lang.CharSequence;Ljava.lang.Iterable<+Ljava.lang.CharSequence;>;)Ljava.lang.String;, join, (arg0, arg1), 79}
				join[METHOD_REF]{join(), Ljava.lang.String;, (Ljava.lang.CharSequence;[Ljava.lang.CharSequence;)Ljava.lang.String;, join, (arg0, arg1), 79}
				valueOf[METHOD_REF]{valueOf(), Ljava.lang.String;, (C)Ljava.lang.String;, valueOf, (arg0), 79}
				valueOf[METHOD_REF]{valueOf(), Ljava.lang.String;, (D)Ljava.lang.String;, valueOf, (arg0), 79}
				valueOf[METHOD_REF]{valueOf(), Ljava.lang.String;, (F)Ljava.lang.String;, valueOf, (arg0), 79}
				valueOf[METHOD_REF]{valueOf(), Ljava.lang.String;, (I)Ljava.lang.String;, valueOf, (arg0), 79}
				valueOf[METHOD_REF]{valueOf(), Ljava.lang.String;, (J)Ljava.lang.String;, valueOf, (arg0), 79}
				valueOf[METHOD_REF]{valueOf(), Ljava.lang.String;, (Ljava.lang.Object;)Ljava.lang.String;, valueOf, (arg0), 79}
				valueOf[METHOD_REF]{valueOf(), Ljava.lang.String;, (Z)Ljava.lang.String;, valueOf, (arg0), 79}
				valueOf[METHOD_REF]{valueOf(), Ljava.lang.String;, ([C)Ljava.lang.String;, valueOf, (arg0), 79}
				valueOf[METHOD_REF]{valueOf(), Ljava.lang.String;, ([CII)Ljava.lang.String;, valueOf, (arg0, arg1, arg2), 79}
				concat[METHOD_REF]{concat(), Ljava.lang.String;, (Ljava.lang.String;)Ljava.lang.String;, concat, (arg0), 90}
				intern[METHOD_REF]{intern(), Ljava.lang.String;, ()Ljava.lang.String;, intern, null, 90}
				replace[METHOD_REF]{replace(), Ljava.lang.String;, (CC)Ljava.lang.String;, replace, (arg0, arg1), 90}
				replace[METHOD_REF]{replace(), Ljava.lang.String;, (Ljava.lang.CharSequence;Ljava.lang.CharSequence;)Ljava.lang.String;, replace, (arg0, arg1), 90}
				replaceAll[METHOD_REF]{replaceAll(), Ljava.lang.String;, (Ljava.lang.String;Ljava.lang.String;)Ljava.lang.String;, replaceAll, (arg0, arg1), 90}
				replaceFirst[METHOD_REF]{replaceFirst(), Ljava.lang.String;, (Ljava.lang.String;Ljava.lang.String;)Ljava.lang.String;, replaceFirst, (arg0, arg1), 90}
				substring[METHOD_REF]{substring(), Ljava.lang.String;, (I)Ljava.lang.String;, substring, (arg0), 90}
				substring[METHOD_REF]{substring(), Ljava.lang.String;, (II)Ljava.lang.String;, substring, (arg0, arg1), 90}
				toLowerCase[METHOD_REF]{toLowerCase(), Ljava.lang.String;, ()Ljava.lang.String;, toLowerCase, null, 90}
				toLowerCase[METHOD_REF]{toLowerCase(), Ljava.lang.String;, (Ljava.util.Locale;)Ljava.lang.String;, toLowerCase, (arg0), 90}
				toString[METHOD_REF]{toString(), Ljava.lang.String;, ()Ljava.lang.String;, toString, null, 90}
				toUpperCase[METHOD_REF]{toUpperCase(), Ljava.lang.String;, ()Ljava.lang.String;, toUpperCase, null, 90}
				toUpperCase[METHOD_REF]{toUpperCase(), Ljava.lang.String;, (Ljava.util.Locale;)Ljava.lang.String;, toUpperCase, (arg0), 90}
				trim[METHOD_REF]{trim(), Ljava.lang.String;, ()Ljava.lang.String;, trim, null, 90}""",
            requestor.getResults());
}
public void testBug548779() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/test/Test.java",
            """
				package test;
				
				public class Test {
					String val = "";
					{
				//		val.match
					}
				}
				
				interface ConditionChecker {
					boolean check(String line);
				}
				
				enum MyGuesser {
					INT_LONG("INT_LONG", (line) -> {
						return line.contains("int");
					}, (line) -> {
						return line.contains("long");
					});
				
					String name;
					ConditionChecker checker;
					ConditionChecker checkerOld;
				
					MyGuesser(String name, ConditionChecker checker, ConditionChecker checkerOld) {
						this.name = name;
						this.checker = checker;
						this.checkerOld = checkerOld;
					}
				}
				""");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "line.";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    assertResults(
            """
				copyValueOf[METHOD_REF]{copyValueOf, Ljava.lang.String;, ([C)Ljava.lang.String;, copyValueOf, (arg0), 49}
				copyValueOf[METHOD_REF]{copyValueOf, Ljava.lang.String;, ([CII)Ljava.lang.String;, copyValueOf, (arg0, arg1, arg2), 49}
				format[METHOD_REF]{format, Ljava.lang.String;, (Ljava.lang.String;[Ljava.lang.Object;)Ljava.lang.String;, format, (arg0, arg1), 49}
				format[METHOD_REF]{format, Ljava.lang.String;, (Ljava.util.Locale;Ljava.lang.String;[Ljava.lang.Object;)Ljava.lang.String;, format, (arg0, arg1, arg2), 49}
				join[METHOD_REF]{join, Ljava.lang.String;, (Ljava.lang.CharSequence;Ljava.lang.Iterable<+Ljava.lang.CharSequence;>;)Ljava.lang.String;, join, (arg0, arg1), 49}
				join[METHOD_REF]{join, Ljava.lang.String;, (Ljava.lang.CharSequence;[Ljava.lang.CharSequence;)Ljava.lang.String;, join, (arg0, arg1), 49}
				valueOf[METHOD_REF]{valueOf, Ljava.lang.String;, (C)Ljava.lang.String;, valueOf, (arg0), 49}
				valueOf[METHOD_REF]{valueOf, Ljava.lang.String;, (D)Ljava.lang.String;, valueOf, (arg0), 49}
				valueOf[METHOD_REF]{valueOf, Ljava.lang.String;, (F)Ljava.lang.String;, valueOf, (arg0), 49}
				valueOf[METHOD_REF]{valueOf, Ljava.lang.String;, (I)Ljava.lang.String;, valueOf, (arg0), 49}
				valueOf[METHOD_REF]{valueOf, Ljava.lang.String;, (J)Ljava.lang.String;, valueOf, (arg0), 49}
				valueOf[METHOD_REF]{valueOf, Ljava.lang.String;, (Ljava.lang.Object;)Ljava.lang.String;, valueOf, (arg0), 49}
				valueOf[METHOD_REF]{valueOf, Ljava.lang.String;, (Z)Ljava.lang.String;, valueOf, (arg0), 49}
				valueOf[METHOD_REF]{valueOf, Ljava.lang.String;, ([C)Ljava.lang.String;, valueOf, (arg0), 49}
				valueOf[METHOD_REF]{valueOf, Ljava.lang.String;, ([CII)Ljava.lang.String;, valueOf, (arg0, arg1, arg2), 49}
				finalize[METHOD_REF]{finalize, Ljava.lang.Object;, ()V, finalize, null, 55}
				getBytes[METHOD_REF]{getBytes, Ljava.lang.String;, (II[BI)V, getBytes, (arg0, arg1, arg2, arg3), 55}
				getChars[METHOD_REF]{getChars, Ljava.lang.String;, (II[CI)V, getChars, (arg0, arg1, arg2, arg3), 55}
				notify[METHOD_REF]{notify, Ljava.lang.Object;, ()V, notify, null, 55}
				notifyAll[METHOD_REF]{notifyAll, Ljava.lang.Object;, ()V, notifyAll, null, 55}
				wait[METHOD_REF]{wait, Ljava.lang.Object;, ()V, wait, null, 55}
				wait[METHOD_REF]{wait, Ljava.lang.Object;, (J)V, wait, (millis), 55}
				wait[METHOD_REF]{wait, Ljava.lang.Object;, (JI)V, wait, (millis, nanos), 55}
				charAt[METHOD_REF]{charAt, Ljava.lang.String;, (I)C, charAt, (arg0), 60}
				chars[METHOD_REF]{chars, Ljava.lang.CharSequence;, ()Ljava.util.stream.IntStream;, chars, null, 60}
				clone[METHOD_REF]{clone, Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, 60}
				codePointAt[METHOD_REF]{codePointAt, Ljava.lang.String;, (I)I, codePointAt, (arg0), 60}
				codePointBefore[METHOD_REF]{codePointBefore, Ljava.lang.String;, (I)I, codePointBefore, (arg0), 60}
				codePointCount[METHOD_REF]{codePointCount, Ljava.lang.String;, (II)I, codePointCount, (arg0, arg1), 60}
				codePoints[METHOD_REF]{codePoints, Ljava.lang.CharSequence;, ()Ljava.util.stream.IntStream;, codePoints, null, 60}
				compareTo[METHOD_REF]{compareTo, Ljava.lang.String;, (Ljava.lang.String;)I, compareTo, (arg0), 60}
				compareToIgnoreCase[METHOD_REF]{compareToIgnoreCase, Ljava.lang.String;, (Ljava.lang.String;)I, compareToIgnoreCase, (arg0), 60}
				concat[METHOD_REF]{concat, Ljava.lang.String;, (Ljava.lang.String;)Ljava.lang.String;, concat, (arg0), 60}
				getBytes[METHOD_REF]{getBytes, Ljava.lang.String;, ()[B, getBytes, null, 60}
				getBytes[METHOD_REF]{getBytes, Ljava.lang.String;, (Ljava.lang.String;)[B, getBytes, (arg0), 60}
				getBytes[METHOD_REF]{getBytes, Ljava.lang.String;, (Ljava.nio.charset.Charset;)[B, getBytes, (arg0), 60}
				getClass[METHOD_REF]{getClass, Ljava.lang.Object;, ()Ljava.lang.Class<*>;, getClass, null, 60}
				hashCode[METHOD_REF]{hashCode, Ljava.lang.String;, ()I, hashCode, null, 60}
				indexOf[METHOD_REF]{indexOf, Ljava.lang.String;, (I)I, indexOf, (arg0), 60}
				indexOf[METHOD_REF]{indexOf, Ljava.lang.String;, (II)I, indexOf, (arg0, arg1), 60}
				indexOf[METHOD_REF]{indexOf, Ljava.lang.String;, (Ljava.lang.String;)I, indexOf, (arg0), 60}
				indexOf[METHOD_REF]{indexOf, Ljava.lang.String;, (Ljava.lang.String;I)I, indexOf, (arg0, arg1), 60}
				intern[METHOD_REF]{intern, Ljava.lang.String;, ()Ljava.lang.String;, intern, null, 60}
				lastIndexOf[METHOD_REF]{lastIndexOf, Ljava.lang.String;, (I)I, lastIndexOf, (arg0), 60}
				lastIndexOf[METHOD_REF]{lastIndexOf, Ljava.lang.String;, (II)I, lastIndexOf, (arg0, arg1), 60}
				lastIndexOf[METHOD_REF]{lastIndexOf, Ljava.lang.String;, (Ljava.lang.String;)I, lastIndexOf, (arg0), 60}
				lastIndexOf[METHOD_REF]{lastIndexOf, Ljava.lang.String;, (Ljava.lang.String;I)I, lastIndexOf, (arg0, arg1), 60}
				length[METHOD_REF]{length, Ljava.lang.String;, ()I, length, null, 60}
				offsetByCodePoints[METHOD_REF]{offsetByCodePoints, Ljava.lang.String;, (II)I, offsetByCodePoints, (arg0, arg1), 60}
				replace[METHOD_REF]{replace, Ljava.lang.String;, (CC)Ljava.lang.String;, replace, (arg0, arg1), 60}
				replace[METHOD_REF]{replace, Ljava.lang.String;, (Ljava.lang.CharSequence;Ljava.lang.CharSequence;)Ljava.lang.String;, replace, (arg0, arg1), 60}
				replaceAll[METHOD_REF]{replaceAll, Ljava.lang.String;, (Ljava.lang.String;Ljava.lang.String;)Ljava.lang.String;, replaceAll, (arg0, arg1), 60}
				replaceFirst[METHOD_REF]{replaceFirst, Ljava.lang.String;, (Ljava.lang.String;Ljava.lang.String;)Ljava.lang.String;, replaceFirst, (arg0, arg1), 60}
				split[METHOD_REF]{split, Ljava.lang.String;, (Ljava.lang.String;)[Ljava.lang.String;, split, (arg0), 60}
				split[METHOD_REF]{split, Ljava.lang.String;, (Ljava.lang.String;I)[Ljava.lang.String;, split, (arg0, arg1), 60}
				subSequence[METHOD_REF]{subSequence, Ljava.lang.String;, (II)Ljava.lang.CharSequence;, subSequence, (arg0, arg1), 60}
				substring[METHOD_REF]{substring, Ljava.lang.String;, (I)Ljava.lang.String;, substring, (arg0), 60}
				substring[METHOD_REF]{substring, Ljava.lang.String;, (II)Ljava.lang.String;, substring, (arg0, arg1), 60}
				toCharArray[METHOD_REF]{toCharArray, Ljava.lang.String;, ()[C, toCharArray, null, 60}
				toLowerCase[METHOD_REF]{toLowerCase, Ljava.lang.String;, ()Ljava.lang.String;, toLowerCase, null, 60}
				toLowerCase[METHOD_REF]{toLowerCase, Ljava.lang.String;, (Ljava.util.Locale;)Ljava.lang.String;, toLowerCase, (arg0), 60}
				toString[METHOD_REF]{toString, Ljava.lang.String;, ()Ljava.lang.String;, toString, null, 60}
				toUpperCase[METHOD_REF]{toUpperCase, Ljava.lang.String;, ()Ljava.lang.String;, toUpperCase, null, 60}
				toUpperCase[METHOD_REF]{toUpperCase, Ljava.lang.String;, (Ljava.util.Locale;)Ljava.lang.String;, toUpperCase, (arg0), 60}
				trim[METHOD_REF]{trim, Ljava.lang.String;, ()Ljava.lang.String;, trim, null, 60}
				contains[METHOD_REF]{contains, Ljava.lang.String;, (Ljava.lang.CharSequence;)Z, contains, (arg0), 90}
				contentEquals[METHOD_REF]{contentEquals, Ljava.lang.String;, (Ljava.lang.CharSequence;)Z, contentEquals, (arg0), 90}
				contentEquals[METHOD_REF]{contentEquals, Ljava.lang.String;, (Ljava.lang.StringBuffer;)Z, contentEquals, (arg0), 90}
				endsWith[METHOD_REF]{endsWith, Ljava.lang.String;, (Ljava.lang.String;)Z, endsWith, (arg0), 90}
				equals[METHOD_REF]{equals, Ljava.lang.String;, (Ljava.lang.Object;)Z, equals, (arg0), 90}
				equalsIgnoreCase[METHOD_REF]{equalsIgnoreCase, Ljava.lang.String;, (Ljava.lang.String;)Z, equalsIgnoreCase, (arg0), 90}
				isEmpty[METHOD_REF]{isEmpty, Ljava.lang.String;, ()Z, isEmpty, null, 90}
				matches[METHOD_REF]{matches, Ljava.lang.String;, (Ljava.lang.String;)Z, matches, (arg0), 90}
				regionMatches[METHOD_REF]{regionMatches, Ljava.lang.String;, (ILjava.lang.String;II)Z, regionMatches, (arg0, arg1, arg2, arg3), 90}
				regionMatches[METHOD_REF]{regionMatches, Ljava.lang.String;, (ZILjava.lang.String;II)Z, regionMatches, (arg0, arg1, arg2, arg3, arg4), 90}
				startsWith[METHOD_REF]{startsWith, Ljava.lang.String;, (Ljava.lang.String;)Z, startsWith, (arg0), 90}
				startsWith[METHOD_REF]{startsWith, Ljava.lang.String;, (Ljava.lang.String;I)Z, startsWith, (arg0, arg1), 90}""",
    		requestor.getResults());
}
public void testBug543617() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/test/Test.java",
            """
				package test.module;
				
				import java.util.Collections;
				import java.util.Iterator;
				import java.util.List;
				
				public class TestApp {
					private <E> void print(Iterator<E> iterator) {
				                // doesn't shows chain proposals
						iterator.forEachRemaining(e -> load(C1));\s
				
						this.load(C2);\s
					}
				
					public List<String> findAll() {
						return load(Collections.EMPTY_LIST);
					}
				
					public List<String> load(List<Long> ids) {
						return null;
					}
				}
				""");

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
			"""
				public class CodeCompletion {
					public static void main(String[] args) {
						new Thread( () -> {
							Double d = new Double(
						});
					}
				}
				""");
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
			"""
				public class CodeCompletion {
					public static void main(String[] args) {
						new Thread( () -> {
							Double d = meth(
						});
					}
					static Double meth(String arg) { return null; }
					static Number meth(String arg, boolean flag) { return null; }
				}
				""");
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
			"""
				import java.util.function.Supplier;
				public class TestCase<S extends GenericThing> {
					TestCase(Supplier<S> s) {}
				}
				""");
	this.workingCopies[3] = getWorkingCopy("/Completion/src/Test.java",
			"""
				public class Test extends TestCase<SpecificThing> {
					private final Foo foo;
					public Test(Foo foo, Bar bar) {
						super(() -> new SpecificThing(foo, bar) {
								// press Ctrl+Space before the comment
						});
						this.foo = foo;
					}
				}
				""");
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
            """
				import java.util.concurrent.Callable;
				
				
				public class TestClass {
					public enum TestEnum {
						ENUM1(() -> ),
						ENUM2(() -> );
				
						private Callable<Object> callable;
				
						private TestEnum(Callable<Object> callable) {
						{
							this.callable = callable;
						}
					}
				}
				""");

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
            """
				import java.util.stream.Stream;
				import java.nio.file.Files;
				import java.nio.file.Paths;
				
				public class Bug573105 {
					private void test(){\s
						Stream.of(new Element()).map(element -> Files.lines(Paths.get(element.)))
					}\s
					private class Element {
						public java.net.URI foo(){return null;}
					}
				}""");

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
            """
				import java.util.stream.Stream;
				import java.nio.file.Paths;
				
				public class Bug573105 {
					private void test(){\s
						Stream.of(new Element()).map(element -> Paths.get(element.))
					}\s
					private class Element {
						public java.net.URI foo(){return null;}
					}
				}""");

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
            """
				import java.util.stream.Stream;
				import java.nio.file.Files;
				import java.nio.file.Paths;
				
				public class Bug573105 {
					private void test(){\s
						Stream.of(new Element()).map(element -> Files.lines(Paths.get(element.)));
					}\s
					private class Element {
						public java.net.URI foo(){return null;}
					}
				}""");

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
            """
				import java.util.stream.Stream;
				import java.nio.file.Files;
				import java.nio.file.Paths;
				
				public class Bug573105 {
					private void test(){\s
						Stream.of(new Element()).map(element -> {\
							return Files.lines(Paths.get(element.)))
						}\
					}\s
					private class Element {
						public java.net.URI foo(){return null;}
					}
				}""");

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
            """
				import java.util.stream.Stream;
				import java.nio.file.Paths;
				
				public class Bug573105 {
					private void test(boolean flag){\s
						Stream.of(new Element()).map(flag ? null : element -> Paths.get(element.))
					}\s
					private class Element {
						public java.net.URI foo(){return null;}
					}
				}""");

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
			"""
				import java.util.Map;
				import java.util.stream.Stream;
				class Path {}
				public class CompletionParserResumeFailure
				{
				    Stream<Path> list(Path dir) throws IOException { return null; }
				    public void freeze()
				    {
				        list(null).map(p -> new Object()
				            {
				                public String name = p.getFileName().toString();
				                public Map<String, Date> clients = p;
				            });
				    }
				}
				""");
    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "Date";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

	assertResults(
			"""
				DateFormat[TYPE_REF]{java.text.DateFormat, java.text, Ljava.text.DateFormat;, null, null, 69}
				DateFormatSymbols[TYPE_REF]{java.text.DateFormatSymbols, java.text, Ljava.text.DateFormatSymbols;, null, null, 69}
				Date[TYPE_REF]{java.sql.Date, java.sql, Ljava.sql.Date;, null, null, 73}
				Date[TYPE_REF]{java.util.Date, java.util, Ljava.util.Date;, null, null, 73}""",
			requestor.getResults());
}
public void testBug574215() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/jdt/Something.java",
			"""
				package jdt;
				class S {
					void foo() {}
					String bar;
				}
				public class Something {
					private void test(S s, int i) {
						Runnable r = () -> {
							if (i > 2) {
								System.out.println("a");
							} else {
								s. // <--
								System.out.println("b");
							}
						}
					}
				}
				""");
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.setAllowsRequiredProposals(CompletionProposal.FIELD_REF, CompletionProposal.TYPE_REF, true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "s.";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults(
			"""
				bar[FIELD_REF]{bar, Ljdt.S;, Ljava.lang.String;, bar, null, 60}
				clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, 60}
				equals[METHOD_REF]{equals(), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), 60}
				finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, 60}
				foo[METHOD_REF]{foo(), Ljdt.S;, ()V, foo, null, 60}
				getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<*>;, getClass, null, 60}
				hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, 60}
				notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, 60}
				notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, 60}
				toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, 60}
				wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, 60}
				wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, wait, (millis), 60}
				wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), 60}""",
			requestor.getResults());
}
public void testBug574215_withToken() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/jdt/Something.java",
			"""
				package jdt;
				class S {
					void foo() {}
					int found;
					String bar;
				}
				public class Something {
					private void test(S s, int i) {
						Runnable r = () -> {
							if (i > 2) {
								System.out.println("a");
							} else {
								s.fo // <--
								System.out.println("b");
							}
						}
					}
				}
				""");
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
            """
				import java.util.stream.Stream;
				import java.util.ArrayList;
				import java.util.concurrent.Callable;
				import java.util.concurrent.TimeUnit;
				
				public class Bug573313 {
					private void test(){\s
						foo(5, SE, null);
					}\s
					private void foo(int i, TimeUnit unit, Callable<String> callback) {\s
					}\s
				}""");

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
            """
				import java.util.stream.Stream;
				import java.util.ArrayList;
				import java.util.concurrent.Callable;
				import java.util.concurrent.TimeUnit;
				
				public class Bug573313 {
					private void test(){\s
						foo(5, TimeUnit.SE, null);
					}\s
					private void foo(int i, TimeUnit unit, Callable<String> callback) {\s
					}\s
				}""");

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
            """
				import java.util.stream.Stream;
				import java.util.ArrayList;
				import java.util.concurrent.Callable;
				import java.util.concurrent.TimeUnit;
				
				public class Bug573313 {
					private void test(){\s
						foo(5, SE);
					}\s
					private void foo(int i, TimeUnit unit, Callable<String> callback) {\s
					}\s
				}""");

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
            """
				import java.util.stream.Stream;
				import java.util.ArrayList;
				import java.util.concurrent.Callable;
				import java.util.concurrent.TimeUnit;
				
				public class Bug573313 {
					private void test(){\s
						foo(5, () -> "call", SE);
					}\s
					private void foo(int i, Callable<String> callback, TimeUnit unit) {\s
					}\s
				}""");

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
            """
				import java.util.stream.Stream;
				import java.util.ArrayList;
				import java.util.concurrent.Callable;
				import java.util.concurrent.TimeUnit;
				
				public class Bug573313 {
					private void test(){\s
						foo(5, , null);
					}\s
					private void foo(int i, TimeUnit unit, Callable<String> callback) {\s
					}\s
				}""");

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
            """
				import java.util.stream.Stream;
				import java.util.ArrayList;
				import java.util.concurrent.Callable;
				import java.util.concurrent.TimeUnit;
				
				public class Bug573313 {
					private void test(){\s
						foo(5,, null);
					}\s
					private void foo(int i, TimeUnit unit, Callable<String> callback) {\s
					}\s
				}""");

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
            """
				import java.util.stream.Stream;
				import java.util.ArrayList;
				import java.util.concurrent.Callable;
				import java.util.concurrent.TimeUnit;
				
				public class Bug573313 {
					private void test(){\s
						foo(5,null,);
					}\s
					private void foo(int i, Callable<String> callback, TimeUnit unit) {\s
					}\s
				}""");

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
            """
				import java.util.stream.Stream;
				import java.util.ArrayList;
				import java.util.concurrent.Callable;
				import java.util.concurrent.TimeUnit;
				
				public class Bug573313 {
					private void test(){\s
						foo(5,defaultParam();
					}\s
					private void foo(int i, TimeUnit unit, Callable<String> callback) {\s
					}\s
					private TimeUnit defaultParam(int amout) {
						return null;\
					}\
					private Callable defaultParam() {
						return null;\
					}\
				}""");

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
            """
				import java.util.stream.Stream;
				import java.util.ArrayList;
				import java.util.concurrent.Callable;
				import java.util.concurrent.TimeUnit;
				
				public class Bug573313 {
					private void test(){\s
						foo(5,defaultParam(),null);
					}\s
					private void foo(int i, TimeUnit unit, Callable<String> callback) {\s
					}\s
					private TimeUnit defaultParam1(int amout) {
						return null;\
					}\
					private Callable defaultParam2() {
						return null;\
					}\
				}""");

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
			"""
				public class App {
					public static void main(String[] args) {
						(new StringBuilder()).append(1).append(2).toString();
					}
				}
				""");

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
			"""
				public class App {
					public static void main(String[] args) {
						(new StringBuilder()).append(1).append(2).toString();
					}
				}
				""");

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
			"""
				public class App {
					public static void main(String[] args) {
						(new StringBuilder()).append(1).append(2).toString();
					}
				}
				""");

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
			"""
				public class App {
					public static void main(String[] args) {
						new StringBuilder().append(1).append(2).toString();
					}
				}
				""");

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
    assertResults("""
		boo[METHOD_REF]{boo(), LApp;, ()LApp;, boo, null, 51}
		class[FIELD_REF]{class, null, Ljava.lang.Class<LApp;>;, class, null, 51}
		foo[METHOD_REF]{foo(), LApp;, ()LApp;, foo, null, 51}
		main[METHOD_REF]{main(), LApp;, ([Ljava.lang.String;)V, main, (args), 51}""", result);
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
            """
				import java.util.ArrayList;
				import java.util.Arrays;
				public class Temp {
				    public static void main(String[] args) {
				    	Arrays.asList(1,2,3).stream()
				    		.map(i -> {
				    			return new ArrayList<>(1);
				    		}).toArray();
				    }
				}""");

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
            """
				import java.util.List;
				import java.util.Arrays;
				public class Temp {
				    public static void main(String[] args) {
				    	Arrays.asList(1,2,3).stream()
				    		.map(i -> {
				    			return new List<>(1);
				    		}).toArray();
				    }
				}""");

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
			"""
				public class Temp {
				    public void foo() {
							Enclosed<String> list = new Temp().new Enclosed<>(1);\
				    }
					public class Enclosed<T> {\
						public Enclosed(int i){}
					}
				}""");

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
			"""
				public class Temp {
				    public void foo() {
							Enclosed<String> list = new Temp().new Enclosed<>(1);\
				    }
					public interface Enclosed<T> {\
						public Enclosed(int i){}
					}
				}""");

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
			"""
				import java.util.Arrays;
				public class Bug563020 {
				    public void foo() {
							Arrays.asList("1").stream().map(String::toUpperCase).\
				    }
				}""");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "map(String::toUpperCase).";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    String result = requestor.getResults();
	assertTrue(String.format("Result doesn't contain expected methods (%s)", result),
    		result.contains("""
				iterator[METHOD_REF]{iterator(), Ljava.util.stream.BaseStream<Ljava.lang.String;Ljava.util.stream.Stream<Ljava.lang.String;>;>;, ()Ljava.util.Iterator<Ljava.lang.String;>;, iterator, null, 60}
				limit[METHOD_REF]{limit(), Ljava.util.stream.Stream<Ljava.lang.String;>;, (J)Ljava.util.stream.Stream<Ljava.lang.String;>;, limit, (arg0), 60}
				map[METHOD_REF]{map(), Ljava.util.stream.Stream<Ljava.lang.String;>;, <R:Ljava.lang.Object;>(Ljava.util.function.Function<-Ljava.lang.String;+TR;>;)Ljava.util.stream.Stream<TR;>;, map, (arg0), 60}
				mapToDouble[METHOD_REF]{mapToDouble(), Ljava.util.stream.Stream<Ljava.lang.String;>;, (Ljava.util.function.ToDoubleFunction<-Ljava.lang.String;>;)Ljava.util.stream.DoubleStream;, mapToDouble, (arg0), 60}
				mapToInt[METHOD_REF]{mapToInt(), Ljava.util.stream.Stream<Ljava.lang.String;>;, (Ljava.util.function.ToIntFunction<-Ljava.lang.String;>;)Ljava.util.stream.IntStream;, mapToInt, (arg0), 60}
				mapToLong[METHOD_REF]{mapToLong(), Ljava.util.stream.Stream<Ljava.lang.String;>;, (Ljava.util.function.ToLongFunction<-Ljava.lang.String;>;)Ljava.util.stream.LongStream;, mapToLong, (arg0), 60}"""));
}
public void testBug563020_lambdaWithMethodRef_exactMethodRef_expectCompletions() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/Bug563020.java",
			"""
				import java.util.Arrays;
				public class Bug563020 {
				    public void foo() {
							Arrays.asList("1").stream().map(String::toString).\
				    }
				}""");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "map(String::toString).";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    String result = requestor.getResults();
	assertTrue(String.format("Result doesn't contain expected methods (%s)", result),
    		result.contains("""
				iterator[METHOD_REF]{iterator(), Ljava.util.stream.BaseStream<Ljava.lang.String;Ljava.util.stream.Stream<Ljava.lang.String;>;>;, ()Ljava.util.Iterator<Ljava.lang.String;>;, iterator, null, 60}
				limit[METHOD_REF]{limit(), Ljava.util.stream.Stream<Ljava.lang.String;>;, (J)Ljava.util.stream.Stream<Ljava.lang.String;>;, limit, (arg0), 60}
				map[METHOD_REF]{map(), Ljava.util.stream.Stream<Ljava.lang.String;>;, <R:Ljava.lang.Object;>(Ljava.util.function.Function<-Ljava.lang.String;+TR;>;)Ljava.util.stream.Stream<TR;>;, map, (arg0), 60}
				mapToDouble[METHOD_REF]{mapToDouble(), Ljava.util.stream.Stream<Ljava.lang.String;>;, (Ljava.util.function.ToDoubleFunction<-Ljava.lang.String;>;)Ljava.util.stream.DoubleStream;, mapToDouble, (arg0), 60}
				mapToInt[METHOD_REF]{mapToInt(), Ljava.util.stream.Stream<Ljava.lang.String;>;, (Ljava.util.function.ToIntFunction<-Ljava.lang.String;>;)Ljava.util.stream.IntStream;, mapToInt, (arg0), 60}
				mapToLong[METHOD_REF]{mapToLong(), Ljava.util.stream.Stream<Ljava.lang.String;>;, (Ljava.util.function.ToLongFunction<-Ljava.lang.String;>;)Ljava.util.stream.LongStream;, mapToLong, (arg0), 60}"""));
}
public void testBug563020_lambdaWithMethodRef_overloadedMethodref_expectCompletionForNextChain() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/Bug563020.java",
			"""
				import java.util.Arrays;
				public class Bug563020 {
				    public void foo() {
							Arrays.asList("1").stream().map(String::toUpperCase).sorted(\
				    }
				}""");

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
			"""
				import java.util.Arrays;
				public class Bug563020 {
				    public void foo() {
							Arrays.asList("1").stream().map(String::toUpperCase).mapTo\
				    }
				}""");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

    String str = this.workingCopies[0].getSource();
    String completeBehind = "map(String::toUpperCase).mapTo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

    String result = requestor.getResults();
	assertTrue(String.format("Result doesn't contain expected methods (%s)", result),
    		result.contains("""
				mapToDouble[METHOD_REF]{mapToDouble(), Ljava.util.stream.Stream<Ljava.lang.String;>;, (Ljava.util.function.ToDoubleFunction<-Ljava.lang.String;>;)Ljava.util.stream.DoubleStream;, mapToDouble, (arg0), 60}
				mapToInt[METHOD_REF]{mapToInt(), Ljava.util.stream.Stream<Ljava.lang.String;>;, (Ljava.util.function.ToIntFunction<-Ljava.lang.String;>;)Ljava.util.stream.IntStream;, mapToInt, (arg0), 60}
				mapToLong[METHOD_REF]{mapToLong(), Ljava.util.stream.Stream<Ljava.lang.String;>;, (Ljava.util.function.ToLongFunction<-Ljava.lang.String;>;)Ljava.util.stream.LongStream;, mapToLong, (arg0), 60}"""));
}
public void testBug563020_methodref_checkParserForBug559677_expectCompletions() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
            "/Completion/src/Bug563020.java",
			"""
				public class Bug563020 {
					private void myRun() {
					}
					private void myMethod(final Runnable r) {
					}
					public void test() {
						// second opening brace causes endless loop while saving
						myMethod((this::myRun);
					}
				}
				"""
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
			"""
				import java.util.Calendar;
				import java.util.Date;
				import java.util.function.Supplier;
				
				public class LambdaFreeze{
				
				   public static final Supplier<Date> SUPPLIER = () -> {
				      Calendar calendar = Calendar.getInstance();
				      calendar.set(Calendar., // try to autocomplete after the "." here freezes eclipse's main thread
				                   0);
				      return calendar.getTime();
				   };
				}
				""");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "Calendar.";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	String result = requestor.getResults();
	assertResults("""
		Calendar.Builder[TYPE_REF]{Builder, java.util, Ljava.util.Calendar$Builder;, null, null, 51}
		class[FIELD_REF]{class, null, Ljava.lang.Class<Ljava.util.Calendar;>;, class, null, 51}
		getAvailableCalendarTypes[METHOD_REF]{getAvailableCalendarTypes(), Ljava.util.Calendar;, ()Ljava.util.Set<Ljava.lang.String;>;, getAvailableCalendarTypes, null, 51}
		getAvailableLocales[METHOD_REF]{getAvailableLocales(), Ljava.util.Calendar;, ()[Ljava.util.Locale;, getAvailableLocales, null, 51}
		getInstance[METHOD_REF]{getInstance(), Ljava.util.Calendar;, ()Ljava.util.Calendar;, getInstance, null, 51}
		getInstance[METHOD_REF]{getInstance(), Ljava.util.Calendar;, (Ljava.util.Locale;)Ljava.util.Calendar;, getInstance, (arg0), 51}
		getInstance[METHOD_REF]{getInstance(), Ljava.util.Calendar;, (Ljava.util.TimeZone;)Ljava.util.Calendar;, getInstance, (arg0), 51}
		getInstance[METHOD_REF]{getInstance(), Ljava.util.Calendar;, (Ljava.util.TimeZone;Ljava.util.Locale;)Ljava.util.Calendar;, getInstance, (arg0, arg1), 51}
		ALL_STYLES[FIELD_REF]{ALL_STYLES, Ljava.util.Calendar;, I, ALL_STYLES, null, 81}
		AM[FIELD_REF]{AM, Ljava.util.Calendar;, I, AM, null, 81}
		AM_PM[FIELD_REF]{AM_PM, Ljava.util.Calendar;, I, AM_PM, null, 81}
		APRIL[FIELD_REF]{APRIL, Ljava.util.Calendar;, I, APRIL, null, 81}
		AUGUST[FIELD_REF]{AUGUST, Ljava.util.Calendar;, I, AUGUST, null, 81}
		DATE[FIELD_REF]{DATE, Ljava.util.Calendar;, I, DATE, null, 81}
		DAY_OF_MONTH[FIELD_REF]{DAY_OF_MONTH, Ljava.util.Calendar;, I, DAY_OF_MONTH, null, 81}
		DAY_OF_WEEK[FIELD_REF]{DAY_OF_WEEK, Ljava.util.Calendar;, I, DAY_OF_WEEK, null, 81}
		DAY_OF_WEEK_IN_MONTH[FIELD_REF]{DAY_OF_WEEK_IN_MONTH, Ljava.util.Calendar;, I, DAY_OF_WEEK_IN_MONTH, null, 81}
		DAY_OF_YEAR[FIELD_REF]{DAY_OF_YEAR, Ljava.util.Calendar;, I, DAY_OF_YEAR, null, 81}
		DECEMBER[FIELD_REF]{DECEMBER, Ljava.util.Calendar;, I, DECEMBER, null, 81}
		DST_OFFSET[FIELD_REF]{DST_OFFSET, Ljava.util.Calendar;, I, DST_OFFSET, null, 81}
		ERA[FIELD_REF]{ERA, Ljava.util.Calendar;, I, ERA, null, 81}
		FEBRUARY[FIELD_REF]{FEBRUARY, Ljava.util.Calendar;, I, FEBRUARY, null, 81}
		FIELD_COUNT[FIELD_REF]{FIELD_COUNT, Ljava.util.Calendar;, I, FIELD_COUNT, null, 81}
		FRIDAY[FIELD_REF]{FRIDAY, Ljava.util.Calendar;, I, FRIDAY, null, 81}
		HOUR[FIELD_REF]{HOUR, Ljava.util.Calendar;, I, HOUR, null, 81}
		HOUR_OF_DAY[FIELD_REF]{HOUR_OF_DAY, Ljava.util.Calendar;, I, HOUR_OF_DAY, null, 81}
		JANUARY[FIELD_REF]{JANUARY, Ljava.util.Calendar;, I, JANUARY, null, 81}
		JULY[FIELD_REF]{JULY, Ljava.util.Calendar;, I, JULY, null, 81}
		JUNE[FIELD_REF]{JUNE, Ljava.util.Calendar;, I, JUNE, null, 81}
		LONG[FIELD_REF]{LONG, Ljava.util.Calendar;, I, LONG, null, 81}
		LONG_FORMAT[FIELD_REF]{LONG_FORMAT, Ljava.util.Calendar;, I, LONG_FORMAT, null, 81}
		LONG_STANDALONE[FIELD_REF]{LONG_STANDALONE, Ljava.util.Calendar;, I, LONG_STANDALONE, null, 81}
		MARCH[FIELD_REF]{MARCH, Ljava.util.Calendar;, I, MARCH, null, 81}
		MAY[FIELD_REF]{MAY, Ljava.util.Calendar;, I, MAY, null, 81}
		MILLISECOND[FIELD_REF]{MILLISECOND, Ljava.util.Calendar;, I, MILLISECOND, null, 81}
		MINUTE[FIELD_REF]{MINUTE, Ljava.util.Calendar;, I, MINUTE, null, 81}
		MONDAY[FIELD_REF]{MONDAY, Ljava.util.Calendar;, I, MONDAY, null, 81}
		MONTH[FIELD_REF]{MONTH, Ljava.util.Calendar;, I, MONTH, null, 81}
		NARROW_FORMAT[FIELD_REF]{NARROW_FORMAT, Ljava.util.Calendar;, I, NARROW_FORMAT, null, 81}
		NARROW_STANDALONE[FIELD_REF]{NARROW_STANDALONE, Ljava.util.Calendar;, I, NARROW_STANDALONE, null, 81}
		NOVEMBER[FIELD_REF]{NOVEMBER, Ljava.util.Calendar;, I, NOVEMBER, null, 81}
		OCTOBER[FIELD_REF]{OCTOBER, Ljava.util.Calendar;, I, OCTOBER, null, 81}
		PM[FIELD_REF]{PM, Ljava.util.Calendar;, I, PM, null, 81}
		SATURDAY[FIELD_REF]{SATURDAY, Ljava.util.Calendar;, I, SATURDAY, null, 81}
		SECOND[FIELD_REF]{SECOND, Ljava.util.Calendar;, I, SECOND, null, 81}
		SEPTEMBER[FIELD_REF]{SEPTEMBER, Ljava.util.Calendar;, I, SEPTEMBER, null, 81}
		SHORT[FIELD_REF]{SHORT, Ljava.util.Calendar;, I, SHORT, null, 81}
		SHORT_FORMAT[FIELD_REF]{SHORT_FORMAT, Ljava.util.Calendar;, I, SHORT_FORMAT, null, 81}
		SHORT_STANDALONE[FIELD_REF]{SHORT_STANDALONE, Ljava.util.Calendar;, I, SHORT_STANDALONE, null, 81}
		SUNDAY[FIELD_REF]{SUNDAY, Ljava.util.Calendar;, I, SUNDAY, null, 81}
		THURSDAY[FIELD_REF]{THURSDAY, Ljava.util.Calendar;, I, THURSDAY, null, 81}
		TUESDAY[FIELD_REF]{TUESDAY, Ljava.util.Calendar;, I, TUESDAY, null, 81}
		UNDECIMBER[FIELD_REF]{UNDECIMBER, Ljava.util.Calendar;, I, UNDECIMBER, null, 81}
		WEDNESDAY[FIELD_REF]{WEDNESDAY, Ljava.util.Calendar;, I, WEDNESDAY, null, 81}
		WEEK_OF_MONTH[FIELD_REF]{WEEK_OF_MONTH, Ljava.util.Calendar;, I, WEEK_OF_MONTH, null, 81}
		WEEK_OF_YEAR[FIELD_REF]{WEEK_OF_YEAR, Ljava.util.Calendar;, I, WEEK_OF_YEAR, null, 81}
		YEAR[FIELD_REF]{YEAR, Ljava.util.Calendar;, I, YEAR, null, 81}
		ZONE_OFFSET[FIELD_REF]{ZONE_OFFSET, Ljava.util.Calendar;, I, ZONE_OFFSET, null, 81}""",
			result);
}
public void testBug574823_completeOn_methodInvocationWithParams_inIfConidtion_insideIfBlock_followedByChainedStatments() throws Exception {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/Bug574823.java",
			"""
				import java.util.ArrayList;
				public class Bug574823 {
					public void foo() {
						ArrayList<String> ints = new ArrayList<String>();
						if(ints.subList(1,1).) {
							String message = "PASS";
							System.out.println(message);
						}
					}
				}
				"""
			);
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, true, true, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "ints.subList(1,1).";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	String result = requestor.getResults();
	int relevance = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_VOID + R_NON_STATIC + R_NON_RESTRICTED;
	assertTrue(String.format("Result doesn't contain method forEach (%s)", result),
			result.contains("forEach[METHOD_REF]{forEach(), Ljava.lang.Iterable<Ljava.lang.String;>;, (Ljava.util.function.Consumer<-Ljava.lang.String;>;)V," +
					" null, null, forEach, (arg0), replace[149, 149], token[149, 149], "+relevance+"}"));
}
public void testBug574823_completeOn_methodInvocationWithParams_inIfConidtion_insideIf_followedByChainedStatment() throws Exception {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/Bug574823.java",
			"""
				import java.util.ArrayList;
				public class Bug574823 {
					public void foo() {
						ArrayList<String> ints = new ArrayList<String>();
						if(ints.subList(1,1).)
							System.out.println(message);
					}
				}
				"""
			);
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, true, true, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "ints.subList(1,1).";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	String result = requestor.getResults();
	int relevance = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_VOID + R_NON_STATIC + R_NON_RESTRICTED;
	assertTrue(String.format("Result doesn't contain method forEach (%s)", result),
			result.contains("forEach[METHOD_REF]{forEach(), Ljava.lang.Iterable<Ljava.lang.String;>;, (Ljava.util.function.Consumer<-Ljava.lang.String;>;)V," +
					" null, null, forEach, (arg0), replace[149, 149], token[149, 149], "+relevance+"}"));
}
public void testBug574823_completeOn_methodInvocationWithParams_inWhileConidtion_insideWhileBlock_followedByChainedStatment() throws Exception {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/Bug574823.java",
			"""
				import java.util.ArrayList;
				public class Bug574823 {
					public void foo() {
						ArrayList<String> ints = new ArrayList<String>();
						while(ints.subList(1,1).){
							System.out.println(message);
						}
					}
				}
				"""
			);
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, true, true, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "ints.subList(1,1).";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	String result = requestor.getResults();
	int relevance = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_VOID + R_NON_STATIC + R_NON_RESTRICTED;
	assertTrue(String.format("Result doesn't contain method forEach (%s)", result),
		result.contains("forEach[METHOD_REF]{forEach(), Ljava.lang.Iterable<Ljava.lang.String;>;, (Ljava.util.function.Consumer<-Ljava.lang.String;>;)V," +
				" null, null, forEach, (arg0), replace[152, 152], token[152, 152], "+relevance+"}"));
}
public void testBug574823_completeOn_methodInvocationWithParams_inIfConidtionWithExpression_insideIfBlock_followedByChainedStatment() throws Exception {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/Bug574823.java",
			"""
				import java.util.ArrayList;
				public class Bug574823 {
					public void foo() {
						ArrayList<String> ints = new ArrayList<String>();
						while(ints.subList(1,1). != null){
							System.out.println(message);
						}
					}
				}
				"""
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
			"""
				import java.util.Calendar;
				import java.util.Date;
				import java.util.function.Supplier;
				
				public class LambdaFreeze2 {
					static int num = 13;
				
					public static final Supplier<Date> SUPPLIER = () -> {
						Calendar calendar = Calendar.getInstance();
						calendar.set(Calendar.ALL_STYLES, calendar.getMinimum(0));
						return calendar.getTime();
					};
				}
				""");

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
			"""
				import java.util.concurrent.ExecutorService;
				import java.util.concurrent.Executors;
				import java.util.concurrent.atomic.AtomicInteger;
				
				public class ForLoop {
					public static void main(String[] args) {
						AtomicInteger executions = new AtomicInteger();
						ExecutorService pool = Executors.newFixedThreadPool(1);
						for (int i = 0; i < 42; i++) {
							pool.execute(() -> {
								// sys| offers sysout etc templates here\s
								executions.incrementAndGet();
								// sys | content assist doesn't offer "sysout" etc templates here
							});
						}
					}
				}
				""");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(false, true, false, false, false, false, false, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBefore = "// sys | content assist doesn't offer";
	int cursorLocation = str.lastIndexOf(completeBefore);
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	String result = requestor.getResults();
	assertResults("""
		ForLoop[TYPE_REF]{ForLoop, , LForLoop;, null, null, 52}
		args[LOCAL_VARIABLE_REF]{args, null, [Ljava.lang.String;, args, null, 52}
		executions[LOCAL_VARIABLE_REF]{executions, null, LAtomicInteger;, executions, null, 52}
		i[LOCAL_VARIABLE_REF]{i, null, I, i, null, 52}
		main[METHOD_REF]{main(), LForLoop;, ([Ljava.lang.String;)V, main, (args), 52}
		pool[LOCAL_VARIABLE_REF]{pool, null, Ljava.util.concurrent.ExecutorService;, pool, null, 52}""",
			result);
	assertEquals("""
		completion offset=449
		completion range=[449, 448]
		completion token=""
		completion token kind=TOKEN_KIND_NAME
		expectedTypesSignatures=null
		expectedTypesKeys=null
		completion token location={STATEMENT_START}""", // this is required for sysout template proposal
			requestor.getContext());
}
public void testBug575149_expectOverloadedMethodsAndVariablesRankedWithExpectedType() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"Completion/src/Bug443091.java",
			"""
				import java.util.function.Consumer;
				import java.util.function.Function;
				
				public class Bug443091 {
					private void foo() {
				 		Consumer<Integer> capture = null;
						forEach()\
					}
					private void forEach(Consumer<Integer> in) {}
					private void forEach(Function<Integer, String> in) {}
				}
				""");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "forEach(";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	String result = requestor.getResults();
	assertResults(
			"""
				capture[LOCAL_VARIABLE_REF]{capture, null, Ljava.util.function.Consumer<Ljava.lang.Integer;>;, capture, null, 52}
				forEach[METHOD_REF]{, LBug443091;, (Ljava.util.function.Consumer<Ljava.lang.Integer;>;)V, forEach, (in), 56}
				forEach[METHOD_REF]{, LBug443091;, (Ljava.util.function.Function<Ljava.lang.Integer;Ljava.lang.String;>;)V, forEach, (in), 56}
				[LAMBDA_EXPRESSION]{->, Ljava.util.function.Function<Ljava.lang.Integer;Ljava.lang.String;>;, (Ljava.lang.Integer;)Ljava.lang.String;, apply, (arg0), 89}
				[LAMBDA_EXPRESSION]{->, Ljava.util.function.Consumer<Ljava.lang.Integer;>;, (Ljava.lang.Integer;)V, accept, (t), 89}""",
			result);
	assertTrue("expected type signatures don't match", CharOperation.equals(requestor.getExpectedTypesSignatures(),
			new char[][] {"Ljava.util.function.Function<Ljava.lang.Integer;Ljava.lang.String;>;".toCharArray(),
			"Ljava.util.function.Consumer<Ljava.lang.Integer;>;".toCharArray()}, true));
}
public void testBug575149_expectRemainingOverloadedMethodsMatchingFilledArguments() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"Completion/src/Bug443091.java",
			"""
				import java.util.function.Consumer;
				import java.util.function.Function;
				
				public class Bug443091 {
					private void foo() {
				 		Consumer<Integer> capture = null;
						forEach(capture, )\
					}
					private void forEach(Consumer<Integer> in) {}
					private void forEach(Consumer<Integer> in, Integer limit) {}
				}
				""");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "forEach(capture,";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	String result = requestor.getResults();
	assertResults("""
		hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, 52}
		forEach[METHOD_REF]{, LBug443091;, (Ljava.util.function.Consumer<Ljava.lang.Integer;>;)V, forEach, (in), 56}
		forEach[METHOD_REF]{, LBug443091;, (Ljava.util.function.Consumer<Ljava.lang.Integer;>;Ljava.lang.Integer;)V, forEach, (in, limit), 56}""",
			result);
	assertTrue("expected type signatures don't match", CharOperation.equals(requestor.getExpectedTypesSignatures(), new char[][] {"Ljava.lang.Integer;".toCharArray()}, true));
}
public void testBug575149_expectOverloadsOverEnumLiterals() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"Completion/src/Bug443091.java",
			"""
				import java.util.function.Consumer;
				import java.util.function.Function;
				
				public class Bug443091 {
					private void foo() {
				 		Consumer<Integer> capture = null;
						forEach(capture, )\
					}
					private Thread.State defaultState() { return null;}\s
					private void forEach(Consumer<Integer> in, Thread.State state) {}
					private void forEach(Consumer<Integer> in, Thread.State state, Integer limit) {}
				}
				""");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "forEach(capture,";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	String result = requestor.getResults();
	assertResults("""
		BLOCKED[FIELD_REF]{State.BLOCKED, Ljava.lang.Thread$State;, Ljava.lang.Thread$State;, BLOCKED, null, 49}
		NEW[FIELD_REF]{State.NEW, Ljava.lang.Thread$State;, Ljava.lang.Thread$State;, NEW, null, 49}
		RUNNABLE[FIELD_REF]{State.RUNNABLE, Ljava.lang.Thread$State;, Ljava.lang.Thread$State;, RUNNABLE, null, 49}
		TERMINATED[FIELD_REF]{State.TERMINATED, Ljava.lang.Thread$State;, Ljava.lang.Thread$State;, TERMINATED, null, 49}
		TIMED_WAITING[FIELD_REF]{State.TIMED_WAITING, Ljava.lang.Thread$State;, Ljava.lang.Thread$State;, TIMED_WAITING, null, 49}
		WAITING[FIELD_REF]{State.WAITING, Ljava.lang.Thread$State;, Ljava.lang.Thread$State;, WAITING, null, 49}
		defaultState[METHOD_REF]{defaultState(), LBug443091;, ()Ljava.lang.Thread$State;, defaultState, null, 52}
		forEach[METHOD_REF]{, LBug443091;, (Ljava.util.function.Consumer<Ljava.lang.Integer;>;Ljava.lang.Thread$State;)V, forEach, (in, state), 56}
		forEach[METHOD_REF]{, LBug443091;, (Ljava.util.function.Consumer<Ljava.lang.Integer;>;Ljava.lang.Thread$State;Ljava.lang.Integer;)V, forEach, (in, state, limit), 56}""",
			result);
	assertTrue("expected type signatures don't match", CharOperation.equals(requestor.getExpectedTypesSignatures(), new char[][] {"Ljava.lang.Thread$State;".toCharArray()}, true));
}
public void testBug443091_expectLambdaCompletions_forFunctionalInterfaceArgumentAssignment() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"Completion/src/Bug443091.java",
			"""
				import java.util.function.Consumer;
				
				public class Bug443091 {
					private void foo() {
						forEach(capture)\
					}
					private void forEach(Consumer<Integer> in) {}
				}
				""");

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
			"""
				import java.util.function.Consumer;
				
				public class Bug443091 {
					private void foo() {
				 		Consumer<Integer> in =\s
					}
				}
				""");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "in =";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	String result = requestor.getResults();
	assertResults("""
		finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, 47}
		foo[METHOD_REF]{foo(), LBug443091;, ()V, foo, null, 47}
		notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, 47}
		notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, 47}
		wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, 47}
		wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, wait, (millis), 47}
		wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), 47}
		Bug443091[TYPE_REF]{Bug443091, , LBug443091;, null, null, 52}
		clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, 52}
		equals[METHOD_REF]{equals(), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), 52}
		getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<*>;, getClass, null, 52}
		hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, 52}
		toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, 52}
		Consumer<java.lang.Integer>[TYPE_REF]{Consumer, java.util.function, Ljava.util.function.Consumer<Ljava.lang.Integer;>;, null, null, 82}
		[LAMBDA_EXPRESSION]{->, Ljava.util.function.Consumer<Ljava.lang.Integer;>;, (Ljava.lang.Integer;)V, accept, (t), 89}""",
			result);
}
public void testBug576068() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"Completion/src/Bug576068.java",
			"""
				public class Bug576068 {
				
					// Type a new member here and content assist won't find anything.
				
					public void methodA(){
						switch( 1 ){
							case 0:
						}
					}
					public void methodB(){
						Runnable r = ()->{};
					}
				}""");
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBefore = "// Type";
	int cursorLocation = str.indexOf(completeBefore);
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	String result = requestor.getResults();
	assertResults("""
		[POTENTIAL_METHOD_DECLARATION]{, LBug576068;, ()V, , null, 39}
		abstract[KEYWORD]{abstract, null, null, abstract, null, 49}
		class[KEYWORD]{class, null, null, class, null, 49}
		enum[KEYWORD]{enum, null, null, enum, null, 49}
		final[KEYWORD]{final, null, null, final, null, 49}
		interface[KEYWORD]{interface, null, null, interface, null, 49}
		native[KEYWORD]{native, null, null, native, null, 49}
		private[KEYWORD]{private, null, null, private, null, 49}
		protected[KEYWORD]{protected, null, null, protected, null, 49}
		public[KEYWORD]{public, null, null, public, null, 49}
		static[KEYWORD]{static, null, null, static, null, 49}
		strictfp[KEYWORD]{strictfp, null, null, strictfp, null, 49}
		synchronized[KEYWORD]{synchronized, null, null, synchronized, null, 49}
		transient[KEYWORD]{transient, null, null, transient, null, 49}
		volatile[KEYWORD]{volatile, null, null, volatile, null, 49}
		Bug576068[TYPE_REF]{Bug576068, , LBug576068;, null, null, 52}
		clone[METHOD_DECLARATION]{protected Object clone() throws CloneNotSupportedException, Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, 52}
		equals[METHOD_DECLARATION]{public boolean equals(Object obj), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), 52}
		finalize[METHOD_DECLARATION]{protected void finalize() throws Throwable, Ljava.lang.Object;, ()V, finalize, null, 52}
		hashCode[METHOD_DECLARATION]{public int hashCode(), Ljava.lang.Object;, ()I, hashCode, null, 52}
		toString[METHOD_DECLARATION]{public String toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, 52}""",
			result);
}
public void testBug577883_expectCompletions_onLambdaVars_inNestedLambdas() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"Completion/src/Bug577883.java",
			"""
				import java.util.stream.Stream;
				
				public class Bug577883 {
					private static class Int { void boo(){} }
					private void foo() {
						Runnable run = () -> {
							Stream.of(new Int()).map(t -> t.)
						};
					}
				}
				""");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "t.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	String result = requestor.getResults();
	assertResults("""
		boo[METHOD_REF]{boo(), LBug577883$Int;, ()V, boo, null, 55}
		finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, 55}
		notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, 55}
		notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, 55}
		wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, 55}
		wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, wait, (millis), 55}
		wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), 55}
		equals[METHOD_REF]{equals(), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), 80}
		hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, 80}
		getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<*>;, getClass, null, 85}
		toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, 85}
		clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, 90}""",
			result);
}
public void testBug577883_expectCompletions_onLambdaVars_inNestedLambdasL2() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"Completion/src/Bug577883.java",
			"""
				import java.util.stream.Stream;
				import java.util.Optional;
				
				public class Bug577883 {
					private static class Int { Integer boo(){ return 0;} }
					private void foo() {
						Runnable run = () -> {
							Stream.of(new Int()).map(t -> Optional.ofNullable(t).map(t -> t.))
						};
					}
				}
				""");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "t.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	String result = requestor.getResults();
	assertResults("""
		finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, 55}
		notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, 55}
		notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, 55}
		wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, 55}
		wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, wait, (millis), 55}
		wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), 55}
		equals[METHOD_REF]{equals(), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), 80}
		hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, 80}
		boo[METHOD_REF]{boo(), LBug577883$Int;, ()Ljava.lang.Integer;, boo, null, 85}
		getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<*>;, getClass, null, 85}
		toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, 85}
		clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, 90}""",
			result);
}
public void testBug577883_expectCompletions_onIntermediateLambdaVars_inNestedLambdas() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"Completion/src/Bug577883.java",
			"""
				import java.util.stream.Stream;
				import java.util.Optional;
				
				public class Bug577883 {
					private static class Int { \
						Integer boo(){ return 0;} \
						boolean canBoo(){ return true;} \
					}
					private void foo() {
						Runnable run = () -> {
							Stream.of(new Int()).map(t -> Optional.ofNullable(t).map(t -> t.boo() && t.))
						};
					}
				}
				""");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "&& t.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	String result = requestor.getResults();
	assertResults("""
		finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, 55}
		notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, 55}
		notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, 55}
		wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, 55}
		wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, wait, (millis), 55}
		wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), 55}
		boo[METHOD_REF]{boo(), LBug577883$Int;, ()Ljava.lang.Integer;, boo, null, 60}
		clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, 60}
		getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<*>;, getClass, null, 60}
		hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, 60}
		toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, 60}
		canBoo[METHOD_REF]{canBoo(), LBug577883$Int;, ()Z, canBoo, null, 90}
		equals[METHOD_REF]{equals(), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), 90}""",
			result);
}
public void testBug577883_expectCompletions_onOuterLambdaVars_inNestedLambdas() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"Completion/src/Bug577883.java",
			"""
				import java.util.stream.Stream;
				import java.util.Optional;
				import java.util.function.Consumer;
				
				public class Bug577883 {
					private static class Int { \
						Integer boo(){ return 0;} \
						boolean canBoo(){ return true;} \
					}
					private static class Dbl { \
						Double boo(){ return 0.0;} \
					}
					private void foo() {
						Consumer<Dbl> consu = (d) -> {
							Stream.of(new Int()).filter(t -> d.)
						};
					}
				}
				""");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "d.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	String result = requestor.getResults();
	assertResults("""
		finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, 55}
		notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, 55}
		notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, 55}
		wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, 55}
		wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, wait, (millis), 55}
		wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), 55}
		boo[METHOD_REF]{boo(), LBug577883$Dbl;, ()Ljava.lang.Double;, boo, null, 60}
		clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, 60}
		getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<*>;, getClass, null, 60}
		hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, 60}
		toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, 60}
		equals[METHOD_REF]{equals(), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), 90}""",
			result);
}
public void testBug577885_expectCompletions_onMethodArguments_followingMethodInvocationWithMethodRefArguments() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"Completion/src/Bug577885.java",
			"""
				import java.util.stream.Stream;
				
				public class Bug577885 {
					private void foo() {
				 		Stream.of("1").map(Long::valueOf).filter()
					}
				}
				""");

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
			"""
				import java.util.stream.Stream;
				
				public class Bug577885 {
					private void foo() {
						Runnable run = () -> {
				 			Stream.of("1").map(Long::valueOf).filter()
						};
					}
				}
				""");

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
			"""
				import java.util.ArrayList;
				
				public class Bug578116 {
					private void foo() {
						Runnable run = () -> {
							ArrayList<String> list = new\s
						};
					}
				}
				""");

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
			"""
				import java.util.Map;
				
				public class Bug578817 {
					private void foo() {
						Map map = new LinkedHashMap
					}
				}
				""");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.setAllowsRequiredProposals(CompletionProposal.TYPE_REF, CompletionProposal.TYPE_REF, true);
	requestor.setAllowsRequiredProposals(CompletionProposal.CONSTRUCTOR_INVOCATION, CompletionProposal.TYPE_REF, true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "new LinkedHashMap";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());
	String result = requestor.getResults();
	assertResults("""
		LinkedHashMap[CONSTRUCTOR_INVOCATION]{(), Ljava.util.LinkedHashMap;, ()V, LinkedHashMap, null, 81}
		LinkedHashMap[CONSTRUCTOR_INVOCATION]{(), Ljava.util.LinkedHashMap;, (I)V, LinkedHashMap, (arg0), 81}
		LinkedHashMap[CONSTRUCTOR_INVOCATION]{(), Ljava.util.LinkedHashMap;, (IF)V, LinkedHashMap, (arg0, arg1), 81}
		LinkedHashMap[CONSTRUCTOR_INVOCATION]{(), Ljava.util.LinkedHashMap;, (IFZ)V, LinkedHashMap, (arg0, arg1, arg2), 81}
		LinkedHashMap[CONSTRUCTOR_INVOCATION]{(), Ljava.util.LinkedHashMap;, (Ljava.util.Map<+TK;+TV;>;)V, LinkedHashMap, (arg0), 81}""", result);

	requestor = new CompletionTestsRequestor2(true);
	requestor.setAllowsRequiredProposals(CompletionProposal.TYPE_REF, CompletionProposal.TYPE_REF, true);
	requestor.setAllowsRequiredProposals(CompletionProposal.CONSTRUCTOR_INVOCATION, CompletionProposal.TYPE_REF, true);
	requestor.setTypeProposalFilter(typeName -> typeName.startsWith("java.util."));
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());
	result = requestor.getResults();
	assertResults("", result);
}
	public void testBug564875() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"""
					import java.util.List;
					class Person {
					   String getLastName() { return null; }
					   Person getLastPerson() { return null; }
					}
					public class X {
						void test1 (List<Person> people) {
							people.stream().forEach(p -> System.out.println(p.get));\s
						}
					}
					""");

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
			"""
				import java.util.stream.Stream;
				
				public class GH109
				{
				  public static void main(String[] args)
				  {
				    if(args.length > 0) {
				      Stream.of(args).forEach(name -> {
				        try
				        {
				          if (name.startsWith("A"))
				          {
				            name.
				          }
				         \s
				        }
				        catch (Exception e)
				        {
				        }
				      });
				    }
				  }
				}""");

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
				"""
					public class A {
					  public void test() {
					    List<String> list = new java.util.ArrayL
					  }
					}
					""");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "ArrayL";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());
		assertResults(
				"""
					ArrayList[CONSTRUCTOR_INVOCATION]{(), Ljava.util.ArrayList;, ()V, null, null, ArrayList, null, [84, 84], 54}
					ArrayList[CONSTRUCTOR_INVOCATION]{(), Ljava.util.ArrayList;, (I)V, null, null, ArrayList, (arg0), [84, 84], 54}
					ArrayList[CONSTRUCTOR_INVOCATION]{(), Ljava.util.ArrayList;, (Ljava.util.Collection<+TE;>;)V, null, null, ArrayList, (arg0), [84, 84], 54}""",
				requestor.getResults());
	}

public void testGH109_expectCompletionsWithCast_insideLambdaNestedBlocksWithInstanceOf() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"Completion/src/GH109.java",
			"""
				import java.util.stream.Stream;
				
				public class GH109
				{
				  public static void main(String[] args)
				  {
				    if(args.length > 0) {
				      Stream.of(args).map(Object.class::cast).forEach(name -> {
				        try
				        {
				          if (name instanceof String)
				          {
				            name.sta
				          }
				         \s
				        }
				        catch (Exception e)
				        {
				        }
				      });
				    }
				  }
				}""");

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
			"""
				import java.util.Arrays;
				public class GH583 {
				    public void foo() {
							Arrays.asList("1").stream().toArray(String[]::)\
				    }
				}""");

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
					"""
						public class Foo {
						    public void foo() {
									"abc".substring(i).\
						    }
						}""");

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
			"""
				public class Foo {
				    public void foo() {
							java.util.Optional.of(true ? 0 : "").\
				    }
				}""");

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

public void testGH1587_onConstructorWithArgumentsBeforeFirstArgument_withinMethodInvocation_expectConstructorCompletion()
		throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[2] = getWorkingCopy("/Completion/src/GHXYZObject.java", """
			public class GHXYZObject {
				public GHXYZObject(String name, int type){}
			}
			""");
	this.workingCopies[1] = getWorkingCopy("/Completion/src/GHXYZReciever.java", """
			public class GHXYZReciever {
				public GHXYZReciever foo(GHXYZObject input) {
					return this;
				}
			}
			""");
	this.workingCopies[0] = getWorkingCopy("/Completion/src/GHXYZ.java", """
			public class GHXYZ {
				public static void foo() {
					new GHXYZReciever().foo(new GHXYZObject(null, 0));
				}
			}
			""");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

	String str = this.workingCopies[0].getSource();
	String completeBehind = "new GHXYZObject(";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

	String result = requestor.getResults();
	assertResults(
			"GHXYZObject[METHOD_REF<CONSTRUCTOR>]{, LGHXYZObject;, (Ljava.lang.String;I)V, GHXYZObject, (name, type), "
					+ (R_DEFAULT + R_INTERESTING + R_RESOLVED + R_NON_RESTRICTED) + "}\n"
					+ "foo[METHOD_REF]{foo(), LGHXYZ;, ()V, foo, null, "
					+ (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_STATIC) + "}\n"
					+ "GHXYZ[TYPE_REF]{GHXYZ, , LGHXYZ;, null, null, "
					+ (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			result);
}

public void testGH1587_onConstructorWithArgumentsBeforeNthArgument_withinMethodInvocation_expectConstructorCompletion()
		throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[2] = getWorkingCopy("/Completion/src/GHXYZObject.java", """
			public class GHXYZObject {
				public GHXYZObject(String name, int type){}
			}
			""");
	this.workingCopies[1] = getWorkingCopy("/Completion/src/GHXYZReciever.java", """
			public class GHXYZReciever {
				public GHXYZReciever foo(GHXYZObject input) {
					return this;
				}
			}
			""");
	this.workingCopies[0] = getWorkingCopy("/Completion/src/GHXYZ.java", """
			public class GHXYZ {
				public static void foo() {
					new GHXYZReciever().foo(new GHXYZObject(null, 0));
				}
			}
			""");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

	String str = this.workingCopies[0].getSource();
	String completeBehind = "new GHXYZObject(null,";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

	String result = requestor.getResults();
	assertResults(
			"GHXYZObject[METHOD_REF<CONSTRUCTOR>]{, LGHXYZObject;, (Ljava.lang.String;I)V, GHXYZObject, (name, type), "
					+ (R_DEFAULT + R_INTERESTING + R_RESOLVED + R_NON_RESTRICTED) + "}\n"
					+ "foo[METHOD_REF]{foo(), LGHXYZ;, ()V, foo, null, "
					+ (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_STATIC) + "}\n"
					+ "GHXYZ[TYPE_REF]{GHXYZ, , LGHXYZ;, null, null, "
					+ (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			result);
}

public void testGH1587_onConstructorWithArgumentsBeforeFirstArgument_withinChainedMethodInvocationExpression_expectConstructorCompletion()
		throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[2] = getWorkingCopy("/Completion/src/GHXYZObject.java", """
			public class GHXYZObject {
				public GHXYZObject(String name, int type){}
			}
			""");
	this.workingCopies[1] = getWorkingCopy("/Completion/src/GHXYZReciever.java", """
			public class GHXYZReciever {
				public GHXYZReciever foo(GHXYZObject input) {
					return this;
				}
				public GHXYZReciever bar(String name) {
					return this;
				}
			}
			""");
	this.workingCopies[0] = getWorkingCopy("/Completion/src/GHXYZ.java", """
			public class GHXYZ {
				public static void foo() {
					new GHXYZReciever().name("bar").foo(new GHXYZObject(null, 0));
				}
			}
			""");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();

	String str = this.workingCopies[0].getSource();
	String completeBehind = "new GHXYZObject(";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner, new NullProgressMonitor());

	String result = requestor.getResults();
	assertResults(
			"GHXYZObject[METHOD_REF<CONSTRUCTOR>]{, LGHXYZObject;, (Ljava.lang.String;I)V, GHXYZObject, (name, type), "
					+ (R_DEFAULT + R_INTERESTING + R_RESOLVED + R_NON_RESTRICTED) + "}\n"
					+ "foo[METHOD_REF]{foo(), LGHXYZ;, ()V, foo, null, "
					+ (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_STATIC) + "}\n"
					+ "GHXYZ[TYPE_REF]{GHXYZ, , LGHXYZ;, null, null, "
					+ (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			result);
}

}
