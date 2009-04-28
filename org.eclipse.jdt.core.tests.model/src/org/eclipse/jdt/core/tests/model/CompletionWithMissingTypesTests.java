/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.Hashtable;

import junit.framework.Test;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

public class CompletionWithMissingTypesTests extends AbstractJavaModelCompletionTests {

public CompletionWithMissingTypesTests(String name) {
	super(name);
}
public void setUpSuite() throws Exception {
	if (COMPLETION_PROJECT == null)  {
		COMPLETION_PROJECT = setUpJavaProject("Completion");
	} else {
		setUpProjectCompliance(COMPLETION_PROJECT, "1.4");
	}
	super.setUpSuite();
}
static {
//	TESTS_NAMES = new String[] { "testZZZ"};
}
public static Test suite() {
	return buildModelTestSuite(CompletionWithMissingTypesTests.class);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44984
public void test0001() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"  void foo() {\n" +
 		"    MissingType m = null;\n" +
 		"    m.b\n" +
		"  }\n" +
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/missing/MissingType.java",
		"package missing;"+
		"public class MissingType {\n" +
		"  public void bar() {}\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "m.b";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED + R_NO_PROBLEMS;
	int start1 = str.lastIndexOf("m.b") + "m.".length();
	int end1 = start1 + "b".length();
	int start2 = str.lastIndexOf("MissingType");
	int end2 = start2 + "MissingType".length();
	assertResults(
			"bar[METHOD_REF]{bar(), Lmissing.MissingType;, ()V, bar, null, ["+start1+", "+end1+"], " + (relevance1) + "}\n" +
			"   MissingType[TYPE_REF]{missing.MissingType, missing, Lmissing.MissingType;, null, null, ["+start2+", "+end2+"], " + (relevance1) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44984
public void test0002() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"  void foo() {\n" +
 		"    MissingType m = null;\n" +
 		"    m.b\n" +
		"  }\n" +
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/missing/MissingType.java",
		"package missing;"+
		"public class MissingType {\n" +
		"  public int bar;\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "m.b";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED + R_NO_PROBLEMS;
	int start1 = str.lastIndexOf("m.b") + "m.".length();
	int end1 = start1 + "b".length();
	int start2 = str.lastIndexOf("MissingType");
	int end2 = start2 + "MissingType".length();
	assertResults(
			"bar[FIELD_REF]{bar, Lmissing.MissingType;, I, bar, null, ["+start1+", "+end1+"], " + (relevance1) + "}\n" +
			"   MissingType[TYPE_REF]{missing.MissingType, missing, Lmissing.MissingType;, null, null, ["+start2+", "+end2+"], " + (relevance1) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44984
public void test0003() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"  void foo() {\n" +
 		"    MissingType m = null;\n" +
 		"    m.b\n" +
		"  }\n" +
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/missing/MissingType.java",
		"package missing;"+
		"public class MissingType extends SuperType {\n" +
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/missing/SuperType.java",
		"package missing;"+
		"public class SuperType {\n" +
		"  public int bar;\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "m.b";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED + R_NO_PROBLEMS;
	int start1 = str.lastIndexOf("m.b") + "m.".length();
	int end1 = start1 + "b".length();
	int start2 = str.lastIndexOf("MissingType");
	int end2 = start2 + "MissingType".length();
	assertResults(
			"bar[FIELD_REF]{bar, Lmissing.SuperType;, I, bar, null, ["+start1+", "+end1+"], " + (relevance1) + "}\n" +
			"   MissingType[TYPE_REF]{missing.MissingType, missing, Lmissing.MissingType;, null, null, ["+start2+", "+end2+"], " + (relevance1) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44984
public void test0004() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"  void foo() {\n" +
 		"    MissingType[] m = null;\n" +
 		"    m.\n" +
		"  }\n" +
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/missing/MissingType.java",
		"package missing;"+
		"public class MissingType {\n" +
		"  public void bar() {};\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "m.";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_NO_PROBLEMS;
	int relevance2 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED + R_NO_PROBLEMS;
	int start1 = str.lastIndexOf("m.") + "m.".length();
	int end1 = start1;
	int start2 = str.lastIndexOf("MissingType");
	int end2 = start2 + "MissingType".length();
	assertResults(
			"length[FIELD_REF]{length, [Lmissing.MissingType;, I, length, null, ["+start1+", "+end1+"], " + (relevance1) + "}\n" +
			"   MissingType[TYPE_REF]{missing.MissingType, missing, Lmissing.MissingType;, null, null, ["+start2+", "+end2+"], " + (relevance1) + "}\n" +
			"clone[METHOD_REF]{clone(), [Lmissing.MissingType;, ()Ljava.lang.Object;, clone, null, ["+start1+", "+end1+"], " + (relevance2) + "}\n" +
			"   MissingType[TYPE_REF]{missing.MissingType, missing, Lmissing.MissingType;, null, null, ["+start2+", "+end2+"], " + (relevance2) + "}\n" +
			"equals[METHOD_REF]{equals(), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), ["+start1+", "+end1+"], " + (relevance2) + "}\n" +
			"   MissingType[TYPE_REF]{missing.MissingType, missing, Lmissing.MissingType;, null, null, ["+start2+", "+end2+"], " + (relevance2) + "}\n" +
			"finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, ["+start1+", "+end1+"], " + (relevance2) + "}\n" +
			"   MissingType[TYPE_REF]{missing.MissingType, missing, Lmissing.MissingType;, null, null, ["+start2+", "+end2+"], " + (relevance2) + "}\n" +
			"getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class;, getClass, null, ["+start1+", "+end1+"], " + (relevance2) + "}\n" +
			"   MissingType[TYPE_REF]{missing.MissingType, missing, Lmissing.MissingType;, null, null, ["+start2+", "+end2+"], " + (relevance2) + "}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, ["+start1+", "+end1+"], " + (relevance2) + "}\n" +
			"   MissingType[TYPE_REF]{missing.MissingType, missing, Lmissing.MissingType;, null, null, ["+start2+", "+end2+"], " + (relevance2) + "}\n" +
			"notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, ["+start1+", "+end1+"], " + (relevance2) + "}\n" +
			"   MissingType[TYPE_REF]{missing.MissingType, missing, Lmissing.MissingType;, null, null, ["+start2+", "+end2+"], " + (relevance2) + "}\n" +
			"notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, ["+start1+", "+end1+"], " + (relevance2) + "}\n" +
			"   MissingType[TYPE_REF]{missing.MissingType, missing, Lmissing.MissingType;, null, null, ["+start2+", "+end2+"], " + (relevance2) + "}\n" +
			"toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, ["+start1+", "+end1+"], " + (relevance2) + "}\n" +
			"   MissingType[TYPE_REF]{missing.MissingType, missing, Lmissing.MissingType;, null, null, ["+start2+", "+end2+"], " + (relevance2) + "}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, ["+start1+", "+end1+"], " + (relevance2) + "}\n" +
			"   MissingType[TYPE_REF]{missing.MissingType, missing, Lmissing.MissingType;, null, null, ["+start2+", "+end2+"], " + (relevance2) + "}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, wait, (millis), ["+start1+", "+end1+"], " + (relevance2) + "}\n" +
			"   MissingType[TYPE_REF]{missing.MissingType, missing, Lmissing.MissingType;, null, null, ["+start2+", "+end2+"], " + (relevance2) + "}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), ["+start1+", "+end1+"], " + (relevance2) + "}\n" +
			"   MissingType[TYPE_REF]{missing.MissingType, missing, Lmissing.MissingType;, null, null, ["+start2+", "+end2+"], " + (relevance2) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44984
public void test0005() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"  MissingType m;\n" +
		"  void foo() {\n" +
 		"    m.b\n" +
		"  }\n" +
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/missing/MissingType.java",
		"package missing;"+
		"public class MissingType {\n" +
		"  public void bar() {}\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "m.b";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED + R_NO_PROBLEMS;
	int start1 = str.lastIndexOf("m.b") + "m.".length();
	int end1 = start1 + "b".length();
	int start2 = str.lastIndexOf("MissingType");
	int end2 = start2 + "MissingType".length();
	assertResults(
			"bar[METHOD_REF]{bar(), Lmissing.MissingType;, ()V, bar, null, ["+start1+", "+end1+"], " + (relevance1) + "}\n" +
			"   MissingType[TYPE_REF]{missing.MissingType, missing, Lmissing.MissingType;, null, null, ["+start2+", "+end2+"], " + (relevance1) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44984
public void test0006() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"  MissingType m;\n" +
		"  public class Test1 {\n" +
		"    void foo() {\n" +
 		"      m.b\n" +
		"    }\n" +
		"  }\n" +
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/missing/MissingType.java",
		"package missing;"+
		"public class MissingType {\n" +
		"  public void bar() {}\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "m.b";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED + R_NO_PROBLEMS;
	int start1 = str.lastIndexOf("m.b") + "m.".length();
	int end1 = start1 + "b".length();
	int start2 = str.lastIndexOf("MissingType");
	int end2 = start2 + "MissingType".length();
	assertResults(
			"bar[METHOD_REF]{bar(), Lmissing.MissingType;, ()V, bar, null, ["+start1+", "+end1+"], " + (relevance1) + "}\n" +
			"   MissingType[TYPE_REF]{missing.MissingType, missing, Lmissing.MissingType;, null, null, ["+start2+", "+end2+"], " + (relevance1) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44984
public void test0007() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"  MissingType m;\n" +
		"  public class Test1 extends test.SuperType {\n" +
		"    void foo() {\n" +
 		"      m.e\n" +
		"    }\n" +
		"  }\n" +
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/missing/MissingType.java",
		"package missing;"+
		"public class MissingType {\n" +
		"  public void bar() {}\n" +
		"}\n");

	this.workingCopies[2] = getWorkingCopy(
		"/Completion/src/test/SuperType.java",
		"package test;"+
		"public class SuperType {\n" +
		"  public Object m;\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "m.e";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED;
	int start1 = str.lastIndexOf("m.e") + "m.".length();
	int end1 = start1 + "e".length();
	assertResults(
			"equals[METHOD_REF]{equals(), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), ["+start1+", "+end1+"], " + (relevance1) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44984
public void test0008() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"  MissingType m;\n" +
		"  public class Test1 extends test.SuperType {\n" +
		"    void foo() {\n" +
 		"      m.b\n" +
		"    }\n" +
		"  }\n" +
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/missing/MissingType.java",
		"package missing;"+
		"public class MissingType {\n" +
		"  public void bar() {}\n" +
		"}\n");

	this.workingCopies[2] = getWorkingCopy(
		"/Completion/src/test/SuperType.java",
		"package test;"+
		"public class SuperType {\n" +
		"  public Object m;\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "m.b";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44984
public void test0009() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"  void foo() {\n" +
 		"    MissingType m = null;\n" +
 		"    m.b\n" +
		"  }\n" +
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/missing/MissingType.java",
		"package missing;"+
		"public class MissingType {\n" +
		"  public void bar() {}\n" +
		"}\n");

	this.workingCopies[2] = getWorkingCopy(
		"/Completion/src/missing2/MissingType.java",
		"package missing2;"+
		"public class MissingType {\n" +
		"  public void bar() {}\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "m.b";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED + R_NO_PROBLEMS;
	int start1 = str.lastIndexOf("m.b") + "m.".length();
	int end1 = start1 + "b".length();
	int start2 = str.lastIndexOf("MissingType");
	int end2 = start2 + "MissingType".length();
	assertResults(
			"bar[METHOD_REF]{bar(), Lmissing.MissingType;, ()V, bar, null, ["+start1+", "+end1+"], " + (relevance1) + "}\n" +
			"   MissingType[TYPE_REF]{missing.MissingType, missing, Lmissing.MissingType;, null, null, ["+start2+", "+end2+"], " + (relevance1) + "}\n" +
			"bar[METHOD_REF]{bar(), Lmissing2.MissingType;, ()V, bar, null, ["+start1+", "+end1+"], " + (relevance1) + "}\n" +
			"   MissingType[TYPE_REF]{missing2.MissingType, missing2, Lmissing2.MissingType;, null, null, ["+start2+", "+end2+"], " + (relevance1) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44984
public void test0010() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"  void foo() {\n" +
 		"    MissingMemberType m = null;\n" +
 		"    m.b\n" +
		"  }\n" +
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/missing/MissingType.java",
		"package missing;"+
		"public class MissingType {\n" +
		"  public class MissingMemberType {\n" +
		"    public void bar() {}\n" +
		"  }\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "m.b";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED + R_NO_PROBLEMS;
	int start1 = str.lastIndexOf("m.b") + "m.".length();
	int end1 = start1 + "b".length();
	int start2 = str.lastIndexOf("MissingMemberType");
	int end2 = start2 + "MissingMemberType".length();
	assertResults(
			"bar[METHOD_REF]{bar(), Lmissing.MissingType$MissingMemberType;, ()V, bar, null, ["+start1+", "+end1+"], " + (relevance1) + "}\n" +
			"   MissingType.MissingMemberType[TYPE_REF]{missing.MissingType.MissingMemberType, missing, Lmissing.MissingType$MissingMemberType;, null, null, ["+start2+", "+end2+"], " + (relevance1) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44984
public void test0011() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"  MissingType m() {return null;}\n" +
		"  void foo() {\n" +
 		"    m().b\n" +
		"  }\n" +
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/missing/MissingType.java",
		"package missing;"+
		"public class MissingType {\n" +
		"  public void bar() {}\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "m().b";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED + R_NO_PROBLEMS;
	int start1 = str.lastIndexOf("m().b") + "m().".length();
	int end1 = start1 + "b".length();
	int start2 = str.lastIndexOf("MissingType");
	int end2 = start2 + "MissingType".length();
	assertResults(
			"bar[METHOD_REF]{bar(), Lmissing.MissingType;, ()V, bar, null, ["+start1+", "+end1+"], " + (relevance1) + "}\n" +
			"   MissingType[TYPE_REF]{missing.MissingType, missing, Lmissing.MissingType;, null, null, ["+start2+", "+end2+"], " + (relevance1) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44984
public void test0012() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"  MissingType m(int i) {return null;}\n" +
		"  void foo() {\n" +
 		"    m(0).b\n" +
		"  }\n" +
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/missing/MissingType.java",
		"package missing;"+
		"public class MissingType {\n" +
		"  public void bar() {}\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = ".b";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED + R_NO_PROBLEMS;
	int start1 = str.lastIndexOf("m(0).b") + "m(0).".length();
	int end1 = start1 + "b".length();
	int start2 = str.lastIndexOf("MissingType");
	int end2 = start2 + "MissingType".length();
	assertResults(
			"bar[METHOD_REF]{bar(), Lmissing.MissingType;, ()V, bar, null, ["+start1+", "+end1+"], " + (relevance1) + "}\n" +
			"   MissingType[TYPE_REF]{missing.MissingType, missing, Lmissing.MissingType;, null, null, ["+start2+", "+end2+"], " + (relevance1) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44984
public void test0013() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"  MissingType m() {return null;}\n" +
		"  public class Inner extends missing.SuperType{\n" +
		"    void foo() {\n" +
 		"      m().e\n" +
		"    }\n" +
		"  }\n" +
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/missing/MissingType.java",
		"package missing;"+
		"public class MissingType {\n" +
		"  public void bar() {}\n" +
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/missing/SuperType.java",
		"package missing;"+
		"public class SuperType {\n" +
		"  public Object m() {return null;}\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "m().e";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED;
	int start1 = str.lastIndexOf("m().e") + "m().".length();
	int end1 = start1 + "e".length();
	assertResults(
			"equals[METHOD_REF]{equals(), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), ["+start1+", "+end1+"], " + (relevance1) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44984
public void test0014() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"  MissingType m() {return null;}\n" +
		"  public class Inner extends missing.SuperType{\n" +
		"    void foo() {\n" +
 		"      m().b\n" +
		"    }\n" +
		"  }\n" +
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/missing/MissingType.java",
		"package missing;"+
		"public class MissingType {\n" +
		"  public void bar() {}\n" +
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/missing/SuperType.java",
		"package missing;"+
		"public class SuperType {\n" +
		"  public Object m() {return null;}\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "m().b";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44984
public void test0015() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"  MissingType m() {return null;}\n" +
		"  public class Inner extends missing.SuperType{\n" +
		"    void foo() {\n" +
 		"      m().b\n" +
		"    }\n" +
		"  }\n" +
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/missing/MissingType.java",
		"package missing;"+
		"public class MissingType {\n" +
		"  public void bar() {}\n" +
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/missing/SuperType.java",
		"package missing;"+
		"public class SuperType {\n" +
		"  public Object m(int i) {return null;}\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "m().b";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED + R_NO_PROBLEMS;
	int start1 = str.lastIndexOf("m().b") + "m().".length();
	int end1 = start1 + "b".length();
	int start2 = str.lastIndexOf("MissingType");
	int end2 = start2 + "MissingType".length();
	assertResults(
			"bar[METHOD_REF]{bar(), Lmissing.MissingType;, ()V, bar, null, ["+start1+", "+end1+"], " + (relevance1) + "}\n" +
			"   MissingType[TYPE_REF]{missing.MissingType, missing, Lmissing.MissingType;, null, null, ["+start2+", "+end2+"], " + (relevance1) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44984
public void test0016() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"  void foo() {\n" +
 		"    MissingType.MissingMemberType m = null;\n" +
 		"    m.b\n" +
		"  }\n" +
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/missing/MissingType.java",
		"package missing;"+
		"public class MissingType {\n" +
		"  public class MissingMemberType {\n" +
		"    public void bar() {}\n" +
		"  }\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "m.b";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED + R_NO_PROBLEMS;
	int start1 = str.lastIndexOf("m.b") + "m.".length();
	int end1 = start1 + "b".length();
	int start2 = str.lastIndexOf("MissingType");
	int end2 = start2 + "MissingType".length();
	assertResults(
			"bar[METHOD_REF]{bar(), Lmissing.MissingType$MissingMemberType;, ()V, bar, null, ["+start1+", "+end1+"], " + (relevance1) + "}\n" +
			"   MissingType[TYPE_REF]{missing.MissingType, missing, Lmissing.MissingType;, null, null, ["+start2+", "+end2+"], " + (relevance1) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44984
public void test0017() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"  void foo() {\n" +
 		"    missing2.MissingType.MissingMemberType m = null;\n" +
 		"    m.b\n" +
		"  }\n" +
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/missing1/missing2/MissingType.java",
		"package missing1.missing2;"+
		"public class MissingType {\n" +
		"  public class MissingMemberType {\n" +
		"    public void bar() {}\n" +
		"  }\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "m.b";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44984
public void test0018() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"  void foo() {\n" +
 		"    missing2.missing3.MissingType.MissingMemberType m = null;\n" +
 		"    m.b\n" +
		"  }\n" +
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/missing1/missing2/missing3/MissingType.java",
		"package missing1.missing2.missing3;"+
		"public class MissingType {\n" +
		"  public class MissingMemberType {\n" +
		"    public void bar() {}\n" +
		"  }\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "m.b";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44984
public void test0019() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"  void foo() {\n" +
 		"    MissingType.MissingMemberType[] m = null;\n" +
 		"    m.e\n" +
		"  }\n" +
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/missing/MissingType.java",
		"package missing;"+
		"public class MissingType {\n" +
		"  public class MissingMemberType {\n" +
		"    public void bar() {}\n" +
		"  }\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "m.e";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED + R_NO_PROBLEMS;
	int start1 = str.lastIndexOf("m.e") + "m.".length();
	int end1 = start1 + "e".length();
	int start2 = str.lastIndexOf("MissingType");
	int end2 = start2 + "MissingType".length();
	assertResults(
			"equals[METHOD_REF]{equals(), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), ["+start1+", "+end1+"], " + (relevance1) + "}\n" +
			"   MissingType[TYPE_REF]{missing.MissingType, missing, Lmissing.MissingType;, null, null, ["+start2+", "+end2+"], " + (relevance1) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44984
public void test0020() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();

	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_DEPRECATION_CHECK, JavaCore.DISABLED);
		JavaCore.setOptions(options);

		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;\n"+
			"public class Test {\n"+
			"	void foo() {\n"+
			"		MissingMemberType m = null;\n"+
			"		m.b\n"+
			"	}\n"+
			"}");

		this.workingCopies[1] = getWorkingCopy(
			"/Completion/src/missing/MissingType.java",
			"package missing;"+
			"public class MissingType {\n" +
			"  /** @deprecated */\n" +
			"  public class MissingMemberType {\n" +
			"    public void bar() {}\n" +
			"  }\n" +
			"}\n");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "m.b";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

		int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED + R_NO_PROBLEMS;
		int start1 = str.lastIndexOf("m.b") + "m.".length();
		int end1 = start1 + "b".length();
		int start2 = str.lastIndexOf("MissingMemberType");
		int end2 = start2 + "MissingMemberType".length();
		assertResults(
				"bar[METHOD_REF]{bar(), Lmissing.MissingType$MissingMemberType;, ()V, bar, null, ["+start1+", "+end1+"], " + (relevance1) + "}\n" +
				"   MissingType.MissingMemberType[TYPE_REF]{missing.MissingType.MissingMemberType, missing, Lmissing.MissingType$MissingMemberType;, null, null, ["+start2+", "+end2+"], " + (relevance1) + "}",
				requestor.getResults());
	} finally {
		JavaCore.setOptions(this.oldOptions);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44984
public void test0021() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();

	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_DEPRECATION_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);

		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;\n"+
			"public class Test {\n"+
			"	void foo() {\n"+
			"		MissingMemberType m = null;\n"+
			"		m.b\n"+
			"	}\n"+
			"}");

		this.workingCopies[1] = getWorkingCopy(
			"/Completion/src/missing/MissingType.java",
			"package missing;"+
			"public class MissingType {\n" +
			"  /** @deprecated */\n" +
			"  public class MissingMemberType {\n" +
			"    public void bar() {}\n" +
			"  }\n" +
			"}\n");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "m.b";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

		assertResults(
				"",
				requestor.getResults());
	} finally {
		JavaCore.setOptions(this.oldOptions);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44984
public void test0022() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();

	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_DEPRECATION_CHECK, JavaCore.DISABLED);
		JavaCore.setOptions(options);

		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;\n"+
			"public class Test {\n"+
			"	void foo() {\n"+
			"		MissingMemberMemberType m = null;\n"+
			"		m.b\n"+
			"	}\n"+
			"}");

		this.workingCopies[1] = getWorkingCopy(
			"/Completion/src/missing/MissingType.java",
			"package missing;"+
			"public class MissingType {\n" +
			"  	/** @deprecated */\n" +
			"  public class MissingMemberType {\n" +
			"  	 public class MissingMemberMemberType {\n" +
			"      public void bar() {}\n" +
			"    }\n" +
			"  }\n" +
			"}\n");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "m.b";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

		int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED + R_NO_PROBLEMS;
		int start1 = str.lastIndexOf("m.b") + "m.".length();
		int end1 = start1 + "b".length();
		int start2 = str.lastIndexOf("MissingMemberMemberType");
		int end2 = start2 + "MissingMemberMemberType".length();
		assertResults(
				"bar[METHOD_REF]{bar(), Lmissing.MissingType$MissingMemberType$MissingMemberMemberType;, ()V, bar, null, ["+start1+", "+end1+"], " + (relevance1) + "}\n" +
				"   MissingType.MissingMemberType.MissingMemberMemberType[TYPE_REF]{missing.MissingType.MissingMemberType.MissingMemberMemberType, missing, Lmissing.MissingType$MissingMemberType$MissingMemberMemberType;, null, null, ["+start2+", "+end2+"], " + (relevance1) + "}",
				requestor.getResults());
	} finally {
		JavaCore.setOptions(this.oldOptions);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44984
public void test0023() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();

	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_DEPRECATION_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);

		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;\n"+
			"public class Test {\n"+
			"	void foo() {\n"+
			"		MissingMemberMemberType m = null;\n"+
			"		m.b\n"+
			"	}\n"+
			"}");

		this.workingCopies[1] = getWorkingCopy(
			"/Completion/src/missing/MissingType.java",
			"package missing;"+
			"public class MissingType {\n" +
			"  /** @deprecated */\n" +
			"  public class MissingMemberType {\n" +
			"  	 public class MissingMemberMemberType {\n" +
			"      public void bar() {}\n" +
			"    }\n" +
			"  }\n" +
			"}\n");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "m.b";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

		assertResults(
				"",
				requestor.getResults());
	} finally {
		JavaCore.setOptions(this.oldOptions);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44984
public void _test0024() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();

	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.DISABLED);
		JavaCore.setOptions(options);

		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;\n"+
			"public class Test {\n"+
			"	void foo() {\n"+
			"		MissingMemberType m = null;\n"+
			"		m.b\n"+
			"	}\n"+
			"}");

		this.workingCopies[1] = getWorkingCopy(
			"/Completion/src/missing/MissingType.java",
			"package missing;"+
			"public class MissingType {\n" +
			"  private class MissingMemberType {\n" +
			"    public void bar() {}\n" +
			"  }\n" +
			"}\n");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "m.b";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

		int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED + R_NO_PROBLEMS;
		int start1 = str.lastIndexOf("m.b") + "m.".length();
		int end1 = start1 + "b".length();
		int start2 = str.lastIndexOf("MissingMemberType");
		int end2 = start2 + "MissingMemberType".length();
		assertResults(
				"bar[METHOD_REF]{bar(), Lmissing.MissingType$MissingMemberType;, ()V, bar, null, ["+start1+", "+end1+"], " + (relevance1) + "}\n" +
				"   MissingType.MissingMemberType[TYPE_REF]{missing.MissingType.MissingMemberType, missing, Lmissing.MissingType$MissingMemberType;, null, null, ["+start2+", "+end2+"], " + (relevance1) + "}",
				requestor.getResults());
	} finally {
		JavaCore.setOptions(this.oldOptions);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44984
public void test0025() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();

	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);

		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;\n"+
			"public class Test {\n"+
			"	void foo() {\n"+
			"		MissingMemberType m = null;\n"+
			"		m.b\n"+
			"	}\n"+
			"}");

		this.workingCopies[1] = getWorkingCopy(
			"/Completion/src/missing/MissingType.java",
			"package missing;"+
			"public class MissingType {\n" +
			"  private class MissingMemberType {\n" +
			"    public void bar() {}\n" +
			"  }\n" +
			"}\n");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "m.b";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

		assertResults(
				"",
				requestor.getResults());
	} finally {
		JavaCore.setOptions(this.oldOptions);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44984
public void _test0026() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();

	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.DISABLED);
		JavaCore.setOptions(options);

		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;\n"+
			"public class Test {\n"+
			"	void foo() {\n"+
			"		MissingMemberMemberType m = null;\n"+
			"		m.b\n"+
			"	}\n"+
			"}");

		this.workingCopies[1] = getWorkingCopy(
			"/Completion/src/missing/MissingType.java",
			"package missing;"+
			"public class MissingType {\n" +
			"  private class MissingMemberType {\n" +
			"  	 public class MissingMemberMemberType {\n" +
			"      public void bar() {}\n" +
			"    }\n" +
			"  }\n" +
			"}\n");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "m.b";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

		int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED + R_NO_PROBLEMS;
		int start1 = str.lastIndexOf("m.b") + "m.".length();
		int end1 = start1 + "b".length();
		int start2 = str.lastIndexOf("MissingMemberMemberType");
		int end2 = start2 + "MissingMemberMemberType".length();
		assertResults(
				"bar[METHOD_REF]{bar(), Lmissing.MissingType$MissingMemberType$MissingMemberMemberType;, ()V, bar, null, ["+start1+", "+end1+"], " + (relevance1) + "}\n" +
				"   MissingType.MissingMemberType.MissingMemberMemberType[TYPE_REF]{missing.MissingType.MissingMemberType.MissingMemberMemberType, missing, Lmissing.MissingType$MissingMemberType$MissingMemberMemberType;, null, null, ["+start2+", "+end2+"], " + (relevance1) + "}",
				requestor.getResults());
	} finally {
		JavaCore.setOptions(this.oldOptions);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44984
public void test0027() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();

	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);

		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;\n"+
			"public class Test {\n"+
			"	void foo() {\n"+
			"		MissingMemberMemberType m = null;\n"+
			"		m.b\n"+
			"	}\n"+
			"}");

		this.workingCopies[1] = getWorkingCopy(
			"/Completion/src/missing/MissingType.java",
			"package missing;"+
			"public class MissingType {\n" +
			"  private class MissingMemberType {\n" +
			"  	 public class MissingMemberMemberType {\n" +
			"      public void bar() {}\n" +
			"    }\n" +
			"  }\n" +
			"}\n");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "m.b";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

		assertResults(
				"",
				requestor.getResults());
	} finally {
		JavaCore.setOptions(this.oldOptions);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44984
public void test0028() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"import known.KnownType;\n"+
		"public class Test {\n" +
		"  void foo() {\n" +
 		"    MissingType m = null;\n" +
 		"    m.field.b\n" +
		"  }\n" +
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/missing/MissingType.java",
		"package missing;"+
		"public class MissingType {\n" +
		"  public known.KnownType field\n" +
		"}\n");

	this.workingCopies[2] = getWorkingCopy(
		"/Completion/src/test/known/KnownType.java",
		"package known;"+
		"public class KnownType {\n" +
		"  public void bar() {}\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "m.field.b";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44984
public void test0029() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"import known.KnownType;\n"+
		"public class Test {\n" +
		"  MissingType m = null;\n" +
		"  void foo() {\n" +
 		"    m.field.b\n" +
		"  }\n" +
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/missing/MissingType.java",
		"package missing;"+
		"public class MissingType {\n" +
		"  public known.KnownType field\n" +
		"}\n");

	this.workingCopies[2] = getWorkingCopy(
		"/Completion/src/test/known/KnownType.java",
		"package known;"+
		"public class KnownType {\n" +
		"  public void bar() {}\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "m.field.b";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44984
public void test0030() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"import known.KnownType;\n"+
		"public class Test {\n" +
		"  MissingType m(){return null;}\n" +
		"  void foo() {\n" +
 		"    m().field.b\n" +
		"  }\n" +
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/missing/MissingType.java",
		"package missing;"+
		"public class MissingType {\n" +
		"  public known.KnownType field\n" +
		"}\n");

	this.workingCopies[2] = getWorkingCopy(
		"/Completion/src/test/known/KnownType.java",
		"package known;"+
		"public class KnownType {\n" +
		"  public void bar() {}\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "m().field.b";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44984
public void test0031() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"import known.KnownType;\n"+
		"public class Test {\n" +
		"  void foo() {\n" +
		"    MissingType m = null;\n" +
 		"    m.method().b\n" +
		"  }\n" +
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/missing/MissingType.java",
		"package missing;"+
		"public class MissingType {\n" +
		"  public known.KnownType method() {return null;}\n" +
		"}\n");

	this.workingCopies[2] = getWorkingCopy(
		"/Completion/src/test/known/KnownType.java",
		"package known;"+
		"public class KnownType {\n" +
		"  public void bar() {}\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "m.method().b";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44984
public void test0032() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"import known.KnownType;\n"+
		"public class Test {\n" +
		"  MissingType m(){return null;}\n" +
		"  void foo() {\n" +
 		"    m().method().b\n" +
		"  }\n" +
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/missing/MissingType.java",
		"package missing;"+
		"public class MissingType {\n" +
		"  public known.KnownType method() {return null;}\n" +
		"}\n");

	this.workingCopies[2] = getWorkingCopy(
		"/Completion/src/test/known/KnownType.java",
		"package known;"+
		"public class KnownType {\n" +
		"  public void bar() {}\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "m().method().b";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44984
public void test0033() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"  /** @see MissingType#b */\n" +
		"  void foo() {\n" +
		"  }\n" +
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/missing/MissingType.java",
		"package missing;"+
		"public class MissingType {\n" +
		"  public void bar()\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "MissingType#b";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44627
public void test0034() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"  void foo() {\n" +
		"    MissingType.foo\n" +
		"  }\n" +
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/missing/MissingType.java",
		"package missing;"+
		"public class MissingType {\n" +
		"  public void foo1()\n" +
		"  public static void foo2()\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_NON_INHERITED + R_NO_PROBLEMS;
	int start1 = str.lastIndexOf("foo") + "".length();
	int end1 = start1 + "foo".length();
	int start2 = str.lastIndexOf("MissingType");
	int end2 = start2 + "MissingType".length();
	assertResults(
			"foo2[METHOD_REF]{foo2(), Lmissing.MissingType;, ()V, foo2, null, ["+start1+", "+end1+"], " + (relevance1) + "}\n" +
			"   MissingType[TYPE_REF]{missing.MissingType, missing, Lmissing.MissingType;, null, null, ["+start2+", "+end2+"], " + (relevance1) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44627
public void test0035() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"  void foo() {\n" +
		"    MissingType.foo\n" +
		"  }\n" +
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/missing/MissingType.java",
		"package missing;"+
		"public class MissingType {\n" +
		"  public int foo1;\n" +
		"  public static int foo2;\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_NON_INHERITED + R_NO_PROBLEMS;
	int start1 = str.lastIndexOf("foo") + "".length();
	int end1 = start1 + "foo".length();
	int start2 = str.lastIndexOf("MissingType");
	int end2 = start2 + "MissingType".length();
	assertResults(
			"foo2[FIELD_REF]{foo2, Lmissing.MissingType;, I, foo2, null, ["+start1+", "+end1+"], " + (relevance1) + "}\n" +
			"   MissingType[TYPE_REF]{missing.MissingType, missing, Lmissing.MissingType;, null, null, ["+start2+", "+end2+"], " + (relevance1) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44627
public void test0036() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"  void foo() {\n" +
		"    MissingType.foo\n" +
		"  }\n" +
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/missing/MissingType.java",
		"package missing;"+
		"public class MissingType {\n" +
		"  public class foo1 {}\n" +
		"  public static class foo2 {}\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_NON_INHERITED + R_NO_PROBLEMS;
	int start1 = str.lastIndexOf("foo") + "".length();
	int end1 = start1 + "foo".length();
	int start2 = str.lastIndexOf("MissingType");
	int end2 = start2 + "MissingType".length();
	assertResults(
			"MissingType.foo1[TYPE_REF]{foo1, missing, Lmissing.MissingType$foo1;, null, null, ["+start1+", "+end1+"], " + (relevance1) + "}\n" +
			"   MissingType[TYPE_REF]{missing.MissingType, missing, Lmissing.MissingType;, null, null, ["+start2+", "+end2+"], " + (relevance1) + "}\n"+
			"MissingType.foo2[TYPE_REF]{foo2, missing, Lmissing.MissingType$foo2;, null, null, ["+start1+", "+end1+"], " + (relevance1) + "}\n" +
			"   MissingType[TYPE_REF]{missing.MissingType, missing, Lmissing.MissingType;, null, null, ["+start2+", "+end2+"], " + (relevance1) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44627
public void test0037() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"  void foo() {\n" +
		"    MissingType.MissingType2.foo\n" +
		"  }\n" +
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/missing/MissingType.java",
		"package missing;"+
		"public class MissingType {\n" +
		"  public class MissingType2 {\n" +
		"    public void foo1()\n" +
		"    public static void foo2()\n" +
		"  }\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=207929
public void test0038() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"  void foo() {\n" +
		"    MissingType.cla\n" +
		"  }\n" +
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/missing/MissingType.java",
		"package missing;"+
		"public class MissingType {\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "cla";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_NON_INHERITED + R_NO_PROBLEMS;
	int start1 = str.lastIndexOf("cla") + "".length();
	int end1 = start1 + "cla".length();
	int start2 = str.lastIndexOf("MissingType");
	int end2 = start2 + "MissingType".length();
	assertResults(
			"class[FIELD_REF]{class, null, Ljava.lang.Class;, class, null, ["+start1+", "+end1+"], " + (relevance1) + "}\n" +
			"   MissingType[TYPE_REF]{missing.MissingType, missing, Lmissing.MissingType;, null, null, ["+start2+", "+end2+"], " + (relevance1) + "}",
			requestor.getResults());
}
public void test0039() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"  MissingType m;\n" +
		"  void foo() {\n" +
		"    class MissingType {\n" +
		"      public void bar2() {}\n" +
		"    }\n" +
		"    m.b\n" +
		"  }\n" +
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/missing/MissingType.java",
		"package missing;"+
		"public class MissingType {\n" +
		"  public void bar1() {}\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "m.b";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED + R_NO_PROBLEMS;
	int start1 = str.lastIndexOf("m.b") + "m.".length();
	int end1 = start1 + "b".length();
	int start2 = str.indexOf("MissingType");
	int end2 = start2 + "MissingType".length();
	assertResults(
			"bar1[METHOD_REF]{bar1(), Lmissing.MissingType;, ()V, bar1, null, ["+start1+", "+end1+"], " + (relevance1) + "}\n" +
			"   MissingType[TYPE_REF]{missing.MissingType, missing, Lmissing.MissingType;, null, null, ["+start2+", "+end2+"], " + (relevance1) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=223479
public void test0040() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"  MissingType.Mem\n" +
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/missing/MissingType.java",
		"package missing;"+
		"public class MissingType {\n" +
		"  public class Member {}\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "MissingType.Mem";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_NO_PROBLEMS;
	int start1 = str.lastIndexOf("Mem") + "".length();
	int end1 = start1 + "Mem".length();
	int start2 = str.indexOf("MissingType");
	int end2 = start2 + "MissingType".length();
	assertResults(
			"MissingType.Member[TYPE_REF]{Member, missing, Lmissing.MissingType$Member;, null, null, ["+start1+", "+end1+"], " + (relevance1) + "}\n" +
			"   MissingType[TYPE_REF]{missing.MissingType, missing, Lmissing.MissingType;, null, null, ["+start2+", "+end2+"], " + (relevance1) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=260717
public void test0041() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"  void foo() {\n" +
		"    new MissingType(\n" +
		"  }\n" +
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/missing/MissingType.java",
		"package missing;"+
		"public class MissingType {\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "new MissingType(";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_NO_PROBLEMS;
	int start1 = str.lastIndexOf("new MissingType(") + "new MissingType(".length();
	int end1 = start1 + "".length();
	int start2 = str.indexOf("MissingType");
	int end2 = start2 + "MissingType".length();
	assertResults(
			"MissingType[ANONYMOUS_CLASS_DECLARATION]{, Lmissing.MissingType;, ()V, null, null, ["+start1+", "+end1+"], " + (relevance1) + "}\n" +
			"   MissingType[TYPE_REF]{missing.MissingType, missing, Lmissing.MissingType;, null, null, ["+start2+", "+end2+"], " + (relevance1) + "}\n" +
			"MissingType[METHOD_REF<CONSTRUCTOR>]{, Lmissing.MissingType;, ()V, MissingType, null, ["+start1+", "+end1+"], " + (relevance1) + "}\n" +
			"   MissingType[TYPE_REF]{missing.MissingType, missing, Lmissing.MissingType;, null, null, ["+start2+", "+end2+"], " + (relevance1) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=260717
public void test0042() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"  void foo() {\n" +
		"    new MissingType.Member(\n" +
		"  }\n" +
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/missing/MissingType.java",
		"package missing;"+
		"public class MissingType {\n" +
		"  public static class Member {}\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "new MissingType.Member(";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_NO_PROBLEMS;
	int start1 = str.lastIndexOf("new MissingType.Member(") + "new MissingType.Member(".length();
	int end1 = start1 + "".length();
	int start2 = str.indexOf("MissingType");
	int end2 = start2 + "MissingType".length();
	assertResults(
			"Member[METHOD_REF<CONSTRUCTOR>]{, Lmissing.MissingType$Member;, ()V, Member, null, ["+start1+", "+end1+"], " + (relevance1) + "}\n" +
			"   MissingType[TYPE_REF]{missing.MissingType, missing, Lmissing.MissingType;, null, null, ["+start2+", "+end2+"], " + (relevance1) + "}\n" +
			"MissingType.Member[ANONYMOUS_CLASS_DECLARATION]{, Lmissing.MissingType$Member;, ()V, null, null, ["+start1+", "+end1+"], " + (relevance1) + "}\n" +
			"   MissingType[TYPE_REF]{missing.MissingType, missing, Lmissing.MissingType;, null, null, ["+start2+", "+end2+"], " + (relevance1) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=260717
public void test0043() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"  void foo() {\n" +
		"    this.new MissingType(\n" +
		"  }\n" +
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/missing/MissingType.java",
		"package missing;"+
		"public class MissingType {\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "new MissingType(";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=260717
public void test0044() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"  void foo() {\n" +
		"    MissingType m = null;\n" +
		"    m.new Member(\n" +
		"  }\n" +
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/missing/MissingType.java",
		"package missing;"+
		"public class MissingType {\n" +
		"  public class Member {}\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "new Member(";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=260717
public void test0045() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"  void foo() {\n" +
		"    new MissingType(\n" +
		"  }\n" +
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/missing/MissingType.java",
		"package missing;"+
		"public interface MissingType {\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "new MissingType(";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_NO_PROBLEMS;
	int start1 = str.lastIndexOf("new MissingType(") + "new MissingType(".length();
	int end1 = start1 + "".length();
	int start2 = str.indexOf("MissingType");
	int end2 = start2 + "MissingType".length();
	assertResults(
			"MissingType[ANONYMOUS_CLASS_DECLARATION]{, Lmissing.MissingType;, ()V, null, null, ["+start1+", "+end1+"], " + (relevance1) + "}\n" +
			"   MissingType[TYPE_REF]{missing.MissingType, missing, Lmissing.MissingType;, null, null, ["+start2+", "+end2+"], " + (relevance1) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=260717
public void test0046() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"  void foo() {\n" +
		"    new MissingType(\n" +
		"  }\n" +
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/missing/MissingType.java",
		"package missing;"+
		"public abstract class MissingType {\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "new MissingType(";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_NO_PROBLEMS;
	int start1 = str.lastIndexOf("new MissingType(") + "new MissingType(".length();
	int end1 = start1 + "".length();
	int start2 = str.indexOf("MissingType");
	int end2 = start2 + "MissingType".length();
	assertResults(
			"MissingType[ANONYMOUS_CLASS_DECLARATION]{, Lmissing.MissingType;, ()V, null, null, ["+start1+", "+end1+"], " + (relevance1) + "}\n" +
			"   MissingType[TYPE_REF]{missing.MissingType, missing, Lmissing.MissingType;, null, null, ["+start2+", "+end2+"], " + (relevance1) + "}",
			requestor.getResults());
}
}
