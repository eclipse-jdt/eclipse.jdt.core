/*******************************************************************************
 * Copyright (c) 2020, 2024 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import junit.framework.Test;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;

//java 16 scenarios run with java 16 compliance
public class CompletionTests16_2 extends AbstractJavaModelCompletionTests {

	static {
		// TESTS_NAMES = new String[]{"test034"};
	}

	public CompletionTests16_2(String name) {
		super(name);
	}

	public void setUpSuite() throws Exception {
		if (COMPLETION_PROJECT == null) {
			COMPLETION_PROJECT = setUpJavaProject("Completion", "16");
		} else {
			setUpProjectCompliance(COMPLETION_PROJECT, "16");
		}
		super.setUpSuite();
	}

	public static Test suite() {
		return buildModelTestSuite(CompletionTests16_2.class);
	}

	// completion for local interface
	public void test001() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Completion/src/X.java",
				"public seal class X permits Y{\n"
						+ " public static void main(String[] args){\n"
						+ "    interf;\n}\n}\n"
						+ "	");
		this.workingCopies[0].getJavaProject(); // assuming single project for all working copies
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "interf";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("interface[KEYWORD]{interface, null, null, interface, null, 49}", requestor.getResults());

	}

	// completion for local enum
	public void test002() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Completion/src/X.java",
				"public seal class X permits Y{\n"
						+ " public static void main(String[] args){\n"
						+ "    enu;\n}\n}\n"
						+ "	");
		this.workingCopies[0].getJavaProject(); // assuming single project for all working copies
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "enu";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("Enum[TYPE_REF]{Enum, java.lang, Ljava.lang.Enum;, null, null, 44}\n"
				+ "enum[KEYWORD]{enum, null, null, enum, null, 49}",
				requestor.getResults());

	}
	// completion for final keyword in instanceof pattern variable
	public void test003() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Point.java",
				"public class Point {\n" +
						"private void method(Object o) throws Exception{\n" +
						"if ((o instanceof fina Record xvar )) \n" +
						"{\n" +
						"}\n" +
						"}\n" +
				"}");
		this.workingCopies[0].getJavaProject(); // assuming single project for all working copies
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "fina";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("final[KEYWORD]{final, null, null, final, null, " + (R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_NON_RESTRICTED+R_FINAL+R_EXPECTED_TYPE) + "}",
				requestor.getResults());


	}
	// completion for final keyword in instanceof pattern variable at higher relevance than Fin* classes
	public void test004() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Point.java",
				"public class Point {\n" +
						"private void method(Object o) throws Exception{\n" +
						"if ((o instanceof fina Record xvar )) \n" +
						"{\n" +
						"}\n" +
						"}\n" +
				"}");
		this.workingCopies[1] = getWorkingCopy(
				"/Completion/src/Bug460411.java",
				"package abc;" +
						"public class FinalCl {\n"+
				"}\n");
		this.workingCopies[0].getJavaProject(); // assuming single project for all working copies
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "fina";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("FinalCl[TYPE_REF]{abc.FinalCl, abc, Labc.FinalCl;, null, null, "+(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_NON_RESTRICTED+R_EXPECTED_TYPE)+"}\n"
				+ "final[KEYWORD]{final, null, null, final, null, "+(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_NON_RESTRICTED+R_FINAL+R_EXPECTED_TYPE)+"}",
				requestor.getResults());


	}

	// completion for instanceof pattern variable for false condition - inside the instanceof block
	public void test005() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Point.java",
				"public class Point {\n" +
						"private void method(Object o) throws Exception{\n" +
						"if (!(o instanceof Record xvar )) \n" +
						"{\n" +
						" System.out.println(xvar.);\n" +
						" throw new Exception();\n" +
						"}\n" +

				"}\n" +

				"}");
		this.workingCopies[0].getJavaProject(); // assuming single project for all working copies
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "xvar.";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("",
				requestor.getResults());

	}
	// completion for instanceof pattern variable for true condition - outside the instanceof block
	public void test006() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Point.java",
				"public class Point {\n" +
						"private void method(Object o) throws Exception{\n" +
						"if ((o instanceof Record xvar )) \n" +
						"{\n" +
						" throw new Exception();\n" +
						"}\n" +
						" System.out.println(xvar.);\n" +
						"}\n" +

				"}");
		this.workingCopies[0].getJavaProject(); // assuming single project for all working copies
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "xvar.";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("",
				requestor.getResults());

	}
	// completion for instanceof pattern variable for false condition - outside the instanceof block - in do-while loop
	public void test007() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Point.java",
				"public class Point {\n" +
						"private void method(Object o) throws Exception{\n" +
						"do { \n" +
						"} while (!(o instanceof Record var1));\n" +
						"System.out.println(var1.);\n" +

					"}\n" +

				"}");
		this.workingCopies[0].getJavaProject(); // assuming single project for all working copies
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "var1.";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, 55}\n"
				+ "notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, 55}\n"
				+ "notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, 55}\n"
				+ "wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, 55}\n"
				+ "wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, wait, (millis), 55}\n"
				+ "wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), 55}\n"
				+ "clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, 60}\n"
				+ "equals[METHOD_REF]{equals(), Ljava.lang.Record;, (Ljava.lang.Object;)Z, equals, (obj), 60}\n"
				+ "getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<+Ljava.lang.Object;>;, getClass, null, 60}\n"
				+ "hashCode[METHOD_REF]{hashCode(), Ljava.lang.Record;, ()I, hashCode, null, 90}\n"
				+ "toString[METHOD_REF]{toString(), Ljava.lang.Record;, ()Ljava.lang.String;, toString, null, 90}",
				requestor.getResults());
	}
	// completion for instanceof pattern variable - double negation
	public void test008() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Point.java",
				"public class Point {\n" +
						"private void method(Object o) throws Exception{\n" +
						"if (!!(o instanceof Record xvar )) \n" +
						"{\n" +
						" throw new Exception();\n" +
						"}\n" +
						" System.out.println(xvar.);\n" +
						"}\n" +

				"}");
		this.workingCopies[0].getJavaProject(); // assuming single project for all working copies
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "xvar.";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("",
				requestor.getResults());

	}
	// completion for instanceof pattern variable - triple negation
	public void test009() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Point.java",
				"public class Point {\n" +
						"private void method(Object o) throws Exception{\n" +
						"if (!!!(o instanceof Record xvar )) \n" +
						"{\n" +
						" throw new Exception();\n" +
						"}\n" +
						" System.out.println(xvar.);\n" +
						"}\n" +

				"}");
		this.workingCopies[0].getJavaProject(); // assuming single project for all working copies
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "xvar.";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, 55}\n"
				+ "notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, 55}\n"
				+ "notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, 55}\n"
				+ "wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, 55}\n"
				+ "wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, wait, (millis), 55}\n"
				+ "wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), 55}\n"
				+ "clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, 60}\n"
				+ "equals[METHOD_REF]{equals(), Ljava.lang.Record;, (Ljava.lang.Object;)Z, equals, (obj), 60}\n"
				+ "getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<+Ljava.lang.Object;>;, getClass, null, 60}\n"
				+ "hashCode[METHOD_REF]{hashCode(), Ljava.lang.Record;, ()I, hashCode, null, 90}\n"
				+ "toString[METHOD_REF]{toString(), Ljava.lang.Record;, ()Ljava.lang.String;, toString, null, 90}",
				requestor.getResults());

	}
	// completion for instanceof pattern variable - double negation
	public void test010() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Point.java",
				"public class Point {\n" +
						"private void method(Object o) throws Exception{\n" +
						"if (!!(o instanceof Record xvar )) \n" +
						"{\n" +
						" System.out.println(xvar.);\n" +
						"}\n" +

					"}\n" +

				"}");
		this.workingCopies[0].getJavaProject(); // assuming single project for all working copies
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "xvar.";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, 55}\n"
				+ "notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, 55}\n"
				+ "notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, 55}\n"
				+ "wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, 55}\n"
				+ "wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, wait, (millis), 55}\n"
				+ "wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), 55}\n"
				+ "clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, 60}\n"
				+ "equals[METHOD_REF]{equals(), Ljava.lang.Record;, (Ljava.lang.Object;)Z, equals, (obj), 60}\n"
				+ "getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<+Ljava.lang.Object;>;, getClass, null, 60}\n"
				+ "hashCode[METHOD_REF]{hashCode(), Ljava.lang.Record;, ()I, hashCode, null, 90}\n"
				+ "toString[METHOD_REF]{toString(), Ljava.lang.Record;, ()Ljava.lang.String;, toString, null, 90}",
				requestor.getResults());

	}
	// completion for instanceof pattern variable for false condition
	public void test011() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Point.java",
				"public class Point {\n" +
						"private void method(Object o) throws Exception{\n" +
						"if (!(o instanceof Record xvar )) \n" +
						"{\n" +
						" throw new Exception();\n" +
						"}\n" +
						" System.out.println(xvar.);\n" +
						"}\n" +

				"}");
		this.workingCopies[0].getJavaProject(); // assuming single project for all working copies
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "xvar.";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, 55}\n"
				+ "notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, 55}\n"
				+ "notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, 55}\n"
				+ "wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, 55}\n"
				+ "wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, wait, (millis), 55}\n"
				+ "wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), 55}\n"
				+ "clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, 60}\n"
				+ "equals[METHOD_REF]{equals(), Ljava.lang.Record;, (Ljava.lang.Object;)Z, equals, (obj), 60}\n"
				+ "getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<+Ljava.lang.Object;>;, getClass, null, 60}\n"
				+ "hashCode[METHOD_REF]{hashCode(), Ljava.lang.Record;, ()I, hashCode, null, 90}\n"
				+ "toString[METHOD_REF]{toString(), Ljava.lang.Record;, ()Ljava.lang.String;, toString, null, 90}",
				requestor.getResults());

	}

	// completion for instanceof pattern variable for true condition
	public void test012() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Point.java",
				"public class Point {\n" +
						"private void method(Object o) {\n" +
						"if ((o instanceof Record xvar )) \n" +
						"{\n" +
						" System.out.println(xvar.);\n" +
						"}\n" +

						"}\n" +

				"}");
		this.workingCopies[0].getJavaProject(); // assuming single project for all working copies
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "xvar.";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, 55}\n"
				+ "notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, 55}\n"
				+ "notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, 55}\n"
				+ "wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, 55}\n"
				+ "wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, wait, (millis), 55}\n"
				+ "wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), 55}\n"
				+ "clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, 60}\n"
				+ "equals[METHOD_REF]{equals(), Ljava.lang.Record;, (Ljava.lang.Object;)Z, equals, (obj), 60}\n"
				+ "getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<+Ljava.lang.Object;>;, getClass, null, 60}\n"
				+ "hashCode[METHOD_REF]{hashCode(), Ljava.lang.Record;, ()I, hashCode, null, 90}\n"
				+ "toString[METHOD_REF]{toString(), Ljava.lang.Record;, ()Ljava.lang.String;, toString, null, 90}",
				requestor.getResults());

	}

	public void bug575599() throws Exception {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Bug_575599.java",
				"class Bug_575599 {\n" +
				"	void sample(CharSequence param1, CharSequence param2) {\n" +
				"		if (param1 instanceof String s1 && param2 instanceof String s2) {\n" +
				"			// s1.| completion doesn't work here: `No Default Proposals`\n" +
				"			// ; <- adding `;` here makes completion above work (similar to bug 574267)\n" +
				"			s1.toUpperCase();\n" +
				"			// s1.| completion works here, showing expected options\n" +
				"			if (s1.strip().equals(\"FOO\")) {\n" +
				"				s1.\n" +
				"			}\n" +
				"		}\n" +
				"	}\n" +
				"}");
		this.workingCopies[0].getJavaProject(); // assuming single project for all working copies
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "s1.";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, 60}\n" +
				"codePointAt[METHOD_REF]{codePointAt(), Ljava.lang.String;, (I)I, codePointAt, (index), 60}\n" +
				"equals[METHOD_REF]{equals(), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), 60}\n" +
				"finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, 60}\n" +
				"getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<+Ljava.lang.Object;>;, getClass, null, 60}\n" +
				"hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, 60}\n" +
				"length[METHOD_REF]{length(), Ljava.lang.String;, ()I, length, null, 60}\n" +
				"notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, 60}\n" +
				"notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, 60}\n" +
				"toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, 60}\n" +
				"wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, 60}\n" +
				"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, wait, (millis), 60}\n" +
				"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), 60}",
				requestor.getResults());
	}
}
