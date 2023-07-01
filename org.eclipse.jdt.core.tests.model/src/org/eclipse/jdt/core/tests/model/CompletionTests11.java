/*******************************************************************************
 * Copyright (c) 2018 IBM and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;

import junit.framework.Test;

public class CompletionTests11 extends AbstractJavaModelCompletionTests {
	static {
		// TESTS_NAMES = new String[] {};
	}

public CompletionTests11(String name) {
	super(name);
}
@Override
public void setUpSuite() throws Exception {
	if (COMPLETION_PROJECT == null)  {
		COMPLETION_PROJECT = setUpJavaProject("Completion", "11");
	} else {
		setUpProjectCompliance(COMPLETION_PROJECT, "11");
	}
	super.setUpSuite();
}
public static Test suite() {
	return buildModelTestSuite(CompletionTests11.class);
}
public void test_var_in_parameter_in_lambda() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/X.java",
			"package test;\n" +
			 		"public class X {\n" +
			 		"	public static void main(String[] args) {	\n" +
			 		"		I lambda = (va ) -> {}; \n" +
			 		"		lambda.apply(10); \n" +
			 		"		}\n" +
			 		"	}\n" +
			 		"interface I {\n" +
			 		"void apply(Integer a); \n" +
			 		"}\n");
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "(va";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults(
		"var[KEYWORD]{var, null, null, var, null, 49}",
		requestor.getResults());
}

public void test_members_matching_paramater_name_on_getter() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Smart.java",
			"package test;\n" +
					"public class Smart {\n" +
					"	public static void persist(Task task) {	\n" +
					"		create(task.);\n" +
					"	}\n" +
					"	public static void create(String name, boolean completed, int details) {}\n" +
					"	public static class Task {\n" +
					"		public String getName() {return null;}\n" +
					"		public boolean isCompleted() {return false;}\n" +
					"		public String details() {return null;}\n" +
					"	}\n" +
					"}\n");
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "task.";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertTrue(requestor.getResults(), requestor.getResults()
			.contains("getName[METHOD_REF]{getName(), Ltest.Smart$Task;, ()Ljava.lang.String;, getName, null, " +
					(R_DEFAULT + R_INTERESTING + R_EXACT_NAME + R_CASE + R_CASE + R_EXACT_EXPECTED_TYPE
							+ R_NON_STATIC + R_NON_RESTRICTED + R_RESOLVED)
					+ "}"));
}

public void test_members_matching_paramater_name_on_non_getter() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Smart.java",
			"package test;\n" +
					"public class Smart {\n" +
					"	public static void persist(Task task) {	\n" +
					"		create(task.getName(), false, task.);\n" +
					"	}\n" +
					"	public static void create(String name, boolean completed, String details, String assignee) {}\n"
					+
					"	public static class Task {\n" +
					"		public String getName() {return null;}\n" +
					"		public boolean isCompleted() {return false;}\n" +
					"		public String details() {return null;}\n" +
					"		public int getAssignee() {return null;}\n" +
					"	}\n" +
					"}\n");
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "task.";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertTrue(requestor.getResults(), requestor.getResults()
			.contains("details[METHOD_REF]{details(), Ltest.Smart$Task;, ()Ljava.lang.String;, details, null, " +
					(R_DEFAULT + R_INTERESTING + R_EXACT_NAME + R_CASE + R_CASE + R_EXACT_EXPECTED_TYPE
							+ R_NON_STATIC + R_NON_RESTRICTED + R_RESOLVED)
					+ "}"));
}

public void test_members_matching_paramater_name_on_boolean_getter() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Smart.java",
			"package test;\n" +
					"public class Smart {\n" +
					"	public static void persist(Task task) {	\n" +
					"		create(task.getName(), task.);\n" +
					"	}\n" +
					"	public static void create(String name, boolean completed, String details, String assignee) {}\n"
					+
					"	public static class Task {\n" +
					"		public String getName() {return null;}\n" +
					"		public boolean isCompleted() {return false;}\n" +
					"		public String details() {return null;}\n" +
					"		public int getAssignee() {return null;}\n" +
					"	}\n" +
					"}\n");
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "task.";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertTrue(requestor.getResults(), requestor.getResults()
			.contains(
					"isCompleted[METHOD_REF]{isCompleted(), Ltest.Smart$Task;, ()Z, isCompleted, null, "
							+
							(R_DEFAULT + R_INTERESTING + R_EXACT_NAME + R_CASE + R_CASE + R_EXACT_EXPECTED_TYPE
									+ R_NON_STATIC + R_NON_RESTRICTED + R_RESOLVED)
							+ "}"));
}

public void test_members_matching_paramater_name_on_wrong_type() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Smart.java",
			"package test;\n" +
					"public class Smart {\n" +
					"	public static void persist(Task task) {	\n" +
					"		create(task.getName(), false, \"\", task.);\n" +
					"	}\n" +
					"	public static void create(String name, boolean completed, String details, String assignee) {}\n"
					+
					"	public static class Task {\n" +
					"		public String getName() {return null;}\n" +
					"		public boolean isCompleted() {return false;}\n" +
					"		public String details() {return null;}\n" +
					"		public int getAssignee() {return null;}\n" +
					"	}\n" +
					"}\n");
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "task.";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertFalse(requestor.getResults(), requestor.getResults()
			.contains(
					"getAssignee[METHOD_REF]{getAssignee(), Ltest.Smart$Task;, ()Ljava.lang.String;, getAssignee, null, "
							+
							(R_DEFAULT + R_INTERESTING + R_EXACT_NAME + R_EXACT_EXPECTED_TYPE + R_NON_STATIC
									+ R_NON_RESTRICTED + R_RESOLVED)
							+ "}"));
}

public void test_members_matching_paramater_name_on_field() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Smart.java",
			"package test;\n" +
					"public class Smart {\n" +
					"	public static void persist(Task task) {	\n" +
					"		create(task.getName(), false, \"\", task.);\n" +
					"	}\n" +
					"	public static void create(String name, boolean completed, String details, String assignee) {}\n"
					+
					"	public static class Task {\n" +
					"		public String getName() {return null;}\n" +
					"		public boolean isCompleted() {return false;}\n" +
					"		public String details() {return null;}\n" +
					"		public int getAssignee() {return null;}\n" +
					"		public String assignee;}\n" +
					"	}\n" +
					"}\n");
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "task.";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertTrue(requestor.getResults(), requestor.getResults()
			.contains(
					"assignee[FIELD_REF]{assignee, Ltest.Smart$Task;, Ljava.lang.String;, assignee, null, "
							+
							(R_DEFAULT + R_INTERESTING + R_EXACT_NAME + R_CASE + R_CASE + R_EXACT_EXPECTED_TYPE
									+ R_NON_STATIC + R_NON_RESTRICTED + R_RESOLVED)
							+ "}"));
}

public void test_members_matching_paramater_name_on_local_variable() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Smart.java",
			"package test;\n" +
					"public class Smart {\n" +
					"	public static void persist(String name, String shortName) {	\n" +
					"		create(null, false, null, null);\n" +
					"	}\n" +
					"	public static void create(String name, boolean completed, String details, String assignee) {}\n"
					+
					"}\n");
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "create(";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertTrue(requestor.getResults(), requestor.getResults()
			.contains(
					"name[LOCAL_VARIABLE_REF]{name, null, Ljava.lang.String;, name, null, "
							+
							(R_DEFAULT + R_INTERESTING + R_EXACT_NAME + R_CASE + R_NON_STATIC + R_NON_RESTRICTED
									+ R_UNQUALIFIED)
							+ "}"));
}

public void test_members_matching_constructors_parameter_name() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/Completion/src/test/Smart.java", """
			package test;
			public class Smart {
				public static void persist(Task task) {\t
					new SmartObject(task.);
				}
				public static class Task {
					public String getName() {return null;}
					public boolean isCompleted() {return false;}
					public String details() {return null;}
				}
			}
			""");
	this.workingCopies[1] = getWorkingCopy("/Completion/src/test/SmartObject.java", """
			package test;
			public class SmartObject {
				public SmartObject(String name, String details) {}
			}
			""");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "task.";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertEquals("No constructor", 0, requestor.getResults().lines()
			.filter(line -> line.startsWith("SmartObject[METHOD_REF<CONSTRUCTOR>]")).count());
	assertTrue(requestor.getResults(),
			requestor.getResults()
					.contains(
							"getName[METHOD_REF]{getName(), Ltest.Smart$Task;, ()Ljava.lang.String;, getName, null, "
									+ (R_DEFAULT + R_INTERESTING + R_EXACT_NAME + R_CASE + R_CASE
											+ R_EXACT_EXPECTED_TYPE + R_NON_STATIC + R_NON_RESTRICTED + R_RESOLVED)
									+ "}"));
}
}
