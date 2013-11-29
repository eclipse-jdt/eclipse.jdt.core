/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
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
		COMPLETION_PROJECT = setUpJavaProject("Completion", "1.8");
	} else {
		setUpProjectCompliance(COMPLETION_PROJECT, "1.8");
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
			"  static Foo f = (first, second) -> System.out.print(fi);\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "fi";
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
			"      Foo f = (first, second) -> System.out.print(fi);\n" +
			"  }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "fi";
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
			"			return (xyz, pqr) -> first.\n" +
			"		});\n" +
			"	}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "first.";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults(
			// INTERIM RESULTS, WILL FAIL ONCE ELIDED TYPE IS CORRECTLY INFERRED.
			"clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, 35}\n" +
			"equals[METHOD_REF]{equals(), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), 35}\n" +
			"finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, 35}\n" +
			"getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<+Ljava.lang.Object;>;, getClass, null, 35}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, 35}\n" +
			"notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, 35}\n" +
			"notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, 35}\n" +
			"toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, 35}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, 35}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, wait, (millis), 35}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), 35}",
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
			"argument[LOCAL_VARIABLE_REF]{argument, null, I, argument, null, 27}",
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
}
