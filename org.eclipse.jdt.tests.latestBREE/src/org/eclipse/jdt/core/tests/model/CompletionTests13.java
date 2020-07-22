/*******************************************************************************
 * Copyright (c) 2019 IBM and others.
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
import org.eclipse.jdt.internal.codeassist.RelevanceConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class CompletionTests13 extends AbstractJavaModelCompletionTests {

	static {
//		TESTS_NAMES = new String[]{"test034"};
	}

	public CompletionTests13(String name) {
		super(name);
	}
	public void setUpSuite() throws Exception {
		if (COMPLETION_PROJECT == null)  {
			COMPLETION_PROJECT = setUpJavaProject("Completion", "13");
		} else {
			setUpProjectCompliance(COMPLETION_PROJECT, "13");
		}
		super.setUpSuite();
	}
	public static Test suite() {
		return buildModelTestSuite(CompletionTests13.class);
	}
	public void test001() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Switch.java",
				"public class Switch {\n" + 
						"	public static void bar(MyDay day) {\n" + 
						"		switch (day) {\n" + 
						"		case SATURDAY, SUN ->\n" + 
						"			System.out.println(day.toString());\n" + 
						"		}\n" + 
						"	}\n" + 
						"	public static void main(String[] args) {\n" + 
						"		bar(MyDay.SUNDAY);\n" + 
						"	}\n" + 
						"}\n" + 
				"enum MyDay { SATURDAY, SUNDAY}");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "SATURDAY, SUN";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"SUNDAY[FIELD_REF]{SUNDAY, LMyDay;, LMyDay;, SUNDAY, null, "+ 
						(RelevanceConstants.R_DEFAULT + RelevanceConstants.R_RESOLVED + RelevanceConstants.R_INTERESTING+
								RelevanceConstants.R_CASE + RelevanceConstants.R_EXACT_EXPECTED_TYPE +
								RelevanceConstants.R_ENUM + RelevanceConstants.R_ENUM_CONSTANT +
								RelevanceConstants.R_UNQUALIFIED + RelevanceConstants.R_NON_RESTRICTED)+ "}",
						requestor.getResults());
	}
	public void test002() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Switch.java",
				"public class Switch {\n" + 
						"	public static void bar(MyDay day) {\n" + 
						"		switch (day) {\n" + 
						"		case SATURDAY, SUN :\n" + 
						"			System.out.println(day.toString());\n" + 
						"		}\n" + 
						"	}\n" + 
						"	public static void main(String[] args) {\n" + 
						"		bar(MyDay.SUNDAY);\n" + 
						"	}\n" + 
						"}\n" + 
				"enum MyDay { SATURDAY, SUNDAY}");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "SATURDAY, SUN";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"SUNDAY[FIELD_REF]{SUNDAY, LMyDay;, LMyDay;, SUNDAY, null, "+ 
						(RelevanceConstants.R_DEFAULT + RelevanceConstants.R_RESOLVED + RelevanceConstants.R_INTERESTING+
								RelevanceConstants.R_CASE + RelevanceConstants.R_EXACT_EXPECTED_TYPE +
								RelevanceConstants.R_ENUM + RelevanceConstants.R_ENUM_CONSTANT +
								RelevanceConstants.R_UNQUALIFIED + RelevanceConstants.R_NON_RESTRICTED)+ "}",
						requestor.getResults());
	}
	public void test003() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Switch.java",
				"public class Switch {\n" + 
						"	public static void bar(MyDay day) {\n" + 
						"		switch (day) {\n" + 
						"		case SATU -> \n" + 
						"			System.out.println(day.toString());\n" + 
						"		}\n" + 
						"	}\n" + 
						"	public static void main(String[] args) {\n" + 
						"		bar(MyDay.SUNDAY);\n" + 
						"	}\n" + 
						"}\n" + 
				"enum MyDay { SATURDAY, SUNDAY}");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "SATU";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"SATURDAY[FIELD_REF]{SATURDAY, LMyDay;, LMyDay;, SATURDAY, null, "+ 
						(RelevanceConstants.R_DEFAULT + RelevanceConstants.R_RESOLVED + RelevanceConstants.R_INTERESTING+
								RelevanceConstants.R_CASE + RelevanceConstants.R_EXACT_EXPECTED_TYPE +
								RelevanceConstants.R_ENUM + RelevanceConstants.R_ENUM_CONSTANT +
								RelevanceConstants.R_UNQUALIFIED + RelevanceConstants.R_NON_RESTRICTED)+ "}",
						requestor.getResults());
	}
	public void test004() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Switch.java",
				"public class Switch {\n" + 
						"	public static void bar(MyDay day) {\n" + 
						"		switch (day) {\n" + 
						"		case SATURDAY, SUNDAY, MOND -> \n" + 
						"			System.out.println(day.toString());\n" + 
						"		}\n" + 
						"	}\n" + 
						"	public static void main(String[] args) {\n" + 
						"		bar(MyDay.SUNDAY);\n" + 
						"	}\n" + 
						"}\n" + 
				"enum MyDay { SATURDAY, SUNDAY, MONDAY}");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "MOND";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"MONDAY[FIELD_REF]{MONDAY, LMyDay;, LMyDay;, MONDAY, null, "+ 
						(RelevanceConstants.R_DEFAULT + RelevanceConstants.R_RESOLVED + RelevanceConstants.R_INTERESTING+
								RelevanceConstants.R_CASE + RelevanceConstants.R_EXACT_EXPECTED_TYPE +
								RelevanceConstants.R_ENUM + RelevanceConstants.R_ENUM_CONSTANT +
								RelevanceConstants.R_UNQUALIFIED + RelevanceConstants.R_NON_RESTRICTED)+ "}",
						requestor.getResults());
	}
	public void test005() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Switch.java",
				"public class Switch {\n" + 
						"	public static void bar(MyDay day) {\n" + 
						"		switch (day) {\n" + 
						"		case SATURDAY, SUND, MONDAY -> \n" + 
						"			System.out.println(day.toString());\n" + 
						"		}\n" + 
						"	}\n" + 
						"	public static void main(String[] args) {\n" + 
						"		bar(MyDay.SUNDAY);\n" + 
						"	}\n" + 
						"}\n" + 
				"enum MyDay { SATURDAY, SUNDAY, MONDAY}");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "SUND";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"SUNDAY[FIELD_REF]{SUNDAY, LMyDay;, LMyDay;, SUNDAY, null, "+ 
						(RelevanceConstants.R_DEFAULT + RelevanceConstants.R_RESOLVED + RelevanceConstants.R_INTERESTING+
								RelevanceConstants.R_CASE + RelevanceConstants.R_EXACT_EXPECTED_TYPE +
								RelevanceConstants.R_ENUM + RelevanceConstants.R_ENUM_CONSTANT +
								RelevanceConstants.R_UNQUALIFIED + RelevanceConstants.R_NON_RESTRICTED)+ "}",
						requestor.getResults());
	}
	public void test005a() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Switch.java",
				"public class Switch {\n" + 
						"	public static void bar(MyDay day) {\n" + 
						"		switch (day) {\n" + 
						"		case SATURDAY, SUNDAY -> \n" + 
						"			System.out.println(day.toString());\n" + 
						"		case MON -> \n" + 
						"			System.out.println(day.toString());\n" + 
						"		}\n" + 
						"	}\n" + 
						"	public static void main(String[] args) {\n" + 
						"		bar(MyDay.SUNDAY);\n" + 
						"	}\n" + 
						"}\n" + 
				"enum MyDay { SATURDAY, SUNDAY, MONDAY}");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "case MON";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"MONDAY[FIELD_REF]{MONDAY, LMyDay;, LMyDay;, MONDAY, null, "+ 
						(RelevanceConstants.R_DEFAULT + RelevanceConstants.R_RESOLVED + RelevanceConstants.R_INTERESTING+
								RelevanceConstants.R_CASE + RelevanceConstants.R_EXACT_EXPECTED_TYPE +
								RelevanceConstants.R_ENUM + RelevanceConstants.R_ENUM_CONSTANT +
								RelevanceConstants.R_UNQUALIFIED + RelevanceConstants.R_NON_RESTRICTED)+ "}",
						requestor.getResults());
	}
	public void test006() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Switch.java",
				""" 
					public class Switch { 
						public static void bar(MyDay day) {
							switch (day.toS) {
							case "SATURDAY" -> 
								System.out.println(day.toString());
							}
						}
						public static void main(String[] args) {
							bar(MyDay.SUNDAY);
						}
					}
					enum MyDay { SATURDAY, SUNDAY}
					"""
							);
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "day.toS";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, 60}",
						requestor.getResults());
	}
	public void test007() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Switch.java",
				"public class Switch {\n" + 
						"	public static void bar(MyDay day) {\n" + 
						"		switch (day.o) {\n" + 
						"		case 0 -> \n" + 
						"			System.out.println(day.toString());\n" + 
						"		}\n" + 
						"	}\n" + 
						"	public static void main(String[] args) {\n" + 
						"		bar(MyDay.SUNDAY);\n" + 
						"	}\n" + 
						"}\n" + 
						"enum MyDay { SATURDAY, SUNDAY}");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "day.o";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"ordinal[METHOD_REF]{ordinal(), Ljava.lang.Enum<LMyDay;>;, ()I, ordinal, null, 60}",
						requestor.getResults());
	}
	public void test008() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Switch.java",
				"public class Switch {\n" + 
						"	public static void bar(MyDay day) {\n" + 
						"		switch (1 + day.o) {\n" + 
						"		case 0 -> \n" + 
						"			System.out.println(day.toString());\n" + 
						"		}\n" + 
						"	}\n" + 
						"	public static void main(String[] args) {\n" + 
						"		bar(MyDay.SUNDAY);\n" + 
						"	}\n" + 
						"}\n" + 
						"enum MyDay { SATURDAY, SUNDAY}");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "day.o";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"ordinal[METHOD_REF]{ordinal(), Ljava.lang.Enum<LMyDay;>;, ()I, ordinal, null, 90}",
						requestor.getResults());
	}
	public void test009() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Switch.java",
				"public class Switch {\n" + 
						"	public static void bar(MyDay day) {\n" + 
						"		switch (1 + da) {\n" + 
						"		case 0 -> \n" + 
						"			System.out.println(day.toString());\n" + 
						"		}\n" + 
						"	}\n" + 
						"	public static void main(String[] args) {\n" + 
						"		bar(MyDay.SUNDAY);\n" + 
						"	}\n" + 
						"}\n" + 
						"enum MyDay { SATURDAY, SUNDAY}");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "1 + da";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"day[LOCAL_VARIABLE_REF]{day, null, LMyDay;, day, null, 52}",
						requestor.getResults());
	}
	public void test010() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Switch.java",
				"public class Switch {\n" + 
						"	public static void bar(MyDay day) {\n" + 
						"		switch (1 + da + 1) {\n" + 
						"		case 0 -> \n" + 
						"			System.out.println(day.toString());\n" + 
						"		}\n" + 
						"	}\n" + 
						"	public static void main(String[] args) {\n" + 
						"		bar(MyDay.SUNDAY);\n" + 
						"	}\n" + 
						"}\n" + 
						"enum MyDay { SATURDAY, SUNDAY}");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "1 + da";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"day[LOCAL_VARIABLE_REF]{day, null, LMyDay;, day, null, 52}",
						requestor.getResults());
	}
	public void test011() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Switch.java",
				"public class Switch {\n" + 
						"	public static void bar(int arg0) {\n" + 
						"		foo(\n" + 
						"		switch (arg) {\n" + 
						"		case 1 -> 1;\n" + 
						"		default -> 0;\n" +
						"		}\n" + 
						"	});\n" + 
						"	public static void foo(int arg0) {\n" + 
						"		bar(MyDay.SUNDAY);\n" + 
						"	}\n" + 
						"}\n");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "switch (arg";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"arg0[LOCAL_VARIABLE_REF]{arg0, null, I, arg0, null, 52}",
						requestor.getResults());
	}
	public void test012() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Switch.java",
				"public class Switch {\n" + 
						"	public static void bar(int arg0) {\n" + 
						"		foo(\n" + 
						"		switch (arg0) {\n" + 
						"		case 1 -> arg;\n" + 
						"		default -> 0;\n" +
						"		}\n" + 
						"	});\n" + 
						"	public static void foo(int arg0) {\n" + 
						"		bar(MyDay.SUNDAY);\n" + 
						"	}\n" + 
						"}\n");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "-> arg";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"arg0[LOCAL_VARIABLE_REF]{arg0, null, I, arg0, null, 52}",
						requestor.getResults());
	}
	public void test013() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Switch.java",
				"public class Switch {\n" + 
						"	public static void bar(int arg0) {\n" + 
						"		foo(\n" + 
						"		switch (0 + arg) {\n" + 
						"		case 1 -> 1;\n" + 
						"		default -> 0;\n" +
						"		}\n" + 
						"	});\n" + 
						"	public static void foo(int arg0) {\n" + 
						"		bar(MyDay.SUNDAY);\n" + 
						"	}\n" + 
						"}\n");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "0 + arg";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"arg0[LOCAL_VARIABLE_REF]{arg0, null, I, arg0, null, 82}",
						requestor.getResults());
	}
	public void test014() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"public class X {\n" + 
						"	public static void bar(int arg0) {\n" + 
						"		foo(\n" + 
						"		swi);\n" + 
						"	public static void foo(int arg0) {\n" + 
						"		bar(MyDay.SUNDAY);\n" + 
						"	}\n" + 
						"}\n");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "swi";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"switch[KEYWORD]{switch, null, null, switch, null, 49}",
						requestor.getResults());
	}
	public void test015() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"""
				public class Switch {
					public static void bar(int arg0) {
						pointer: foo(
						switch (0 + arg0) {
							case 1 -> {break ;}
							default -> 0;
						}
					});
					public static void foo(int arg0) {
						bar(MyDay.SUNDAY);
					}
				}""");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "break ";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"pointer[LABEL_REF]{pointer, null, null, pointer, null, 49}",
						requestor.getResults());
	}
	public void test016() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"public class Switch {\n" + 
						"	public static void bar(int arg0) {\n" + 
						"		foo(\n" + 
						"		switch (0 + arg0) {\n" + 
						"		case 1 -> {break 1;}\n" + 
						"		default -> ar;\n" +
						"		}\n" + 
						"	});\n" + 
						"	public static void foo(int arg0) {\n" + 
						"		bar(MyDay.SUNDAY);\n" + 
						"	}\n" + 
						"}\n");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "-> ar";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"arg0[LOCAL_VARIABLE_REF]{arg0, null, I, arg0, null, 52}",
						requestor.getResults());
	}
	public void test017() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"""
				public class Switch {
					public static void bar(int arg0) {
						int arg1 = 0;
						pointer: foo(
						switch (0 + arg0) {
						case 1 -> 1;
						default -> {break p;}
						}
					});
					public static void foo(int arg0) {
						bar(MyDay.SUNDAY);
					}
				}""");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "break p";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"pointer[LABEL_REF]{pointer, null, null, pointer, null, 49}",
						requestor.getResults());
	}
	public void test017a() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"public class Switch {\n" + 
						"	public static void bar(int arg0) {\n" + 
						"		foo(\n" + 
						"		argLabel: switch (0 + arg0) {\n" + 
						"		case 1 -> 1;\n" + 
						"		default -> {break ar;}\n" +
						"		}\n" + 
						"	});\n" + 
						"	public static void foo(int arg0) {\n" + 
						"		bar(MyDay.SUNDAY);\n" + 
						"	}\n" + 
						"}\n");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "break ar";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"argLabel[LABEL_REF]{argLabel, null, null, argLabel, null, 49}",
						requestor.getResults());
	}
	public void test018a() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"import org.eclipse.jdt.annotation.*;\n" + 
				"import java.util.function.*;\n" + 
				"interface IN0 {} \n" + 
				"interface IN1 extends IN0 {} \n" + 
				"interface IN2 extends IN0 {}\n" + 
				"public class X {\n" + 
				"	@NonNull IN1 n_1() { return new IN1() {}; } \n" + 
				"	IN2 n_2() { return null; } \n" + 
				"	<M> void m(@NonNull Supplier<@NonNull M> m2) { } \n" + 
				"	void testSw(int i) { \n" + 
				"		m(switch(i) { \n" + 
				"			case 1 -> this::n_; \n" + 
				"			case 2 -> () -> n1(); \n" + 
				"			case 3 -> null; \n" + 
				"			case 4 -> () -> n2(); \n" + 
				"			default -> this::n2; }); \n" + 
				"	}\n" + 
				"}");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "this::n_";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"n_1[METHOD_NAME_REFERENCE]{n_1, LX;, ()LIN1;, n_1, null, 60}\n" + 
				"n_2[METHOD_NAME_REFERENCE]{n_2, LX;, ()LIN2;, n_2, null, 60}",
						requestor.getResults());
	}
	public void test018b() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"import org.eclipse.jdt.annotation.*;\n" + 
				"interface IN0 {} \n" + 
				"interface IN1 extends IN0 {} \n" + 
				"interface IN2 extends IN0 {}\n" + 
				"public class X {\n" + 
				"	@NonNull IN1 n_1() { return new IN1() {}; } \n" + 
				"	IN2 n_2() { return null; } \n" + 
				"	<M> void m(@NonNull Supplier<@NonNull M> m2) { } \n" + 
				"	void testSw(int i) { \n" + 
				"		m(switch(i) { \n" + 
				"			case 2 -> () -> n_; \n" + 
				"	}\n" + 
				"}\n" +
				"interface Supplier<T> {\n" + 
				"    T get();\n" + 
				"}");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "-> n_";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"n_1[METHOD_REF]{n_1(), LX;, ()LIN1;, n_1, null, 52}\n" + 
				"n_2[METHOD_REF]{n_2(), LX;, ()LIN2;, n_2, null, 52}",
						requestor.getResults());
	}
	public void test018c() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"import org.eclipse.jdt.annotation.*;\n" + 
				"interface IN0 {} \n" + 
				"interface IN1 extends IN0 {} \n" + 
				"interface IN2 extends IN0 {}\n" + 
				"public class X {\n" + 
				"	@NonNull IN1 n_1() { return new IN1() {}; } \n" + 
				"	IN2 n_2() { return null; } \n" + 
				"	<M> void m(@NonNull Supplier<@NonNull M> m2) { } \n" + 
				"	void testSw(int i) { \n" + 
				"		m(switch(i) { \n" + 
				"			default -> this::n_; }); \n" + 
				"	}\n" + 
				"}\n" +
				"interface Supplier<T> {\n" + 
				"    T get();\n" + 
				"}");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "this::n_";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"n_1[METHOD_NAME_REFERENCE]{n_1, LX;, ()LIN1;, n_1, null, 60}\n" + 
				"n_2[METHOD_NAME_REFERENCE]{n_2, LX;, ()LIN2;, n_2, null, 60}",
						requestor.getResults());
	}
	public void test018d() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"import org.eclipse.jdt.annotation.*;\n" + 
				"interface IN0 {} \n" + 
				"interface IN1 extends IN0 {} \n" + 
				"interface IN2 extends IN0 {}\n" + 
				"public class X {\n" + 
				"	@NonNull IN1 n_1() { return new IN1() {}; } \n" + 
				"	IN2 n_2() { return null; } \n" + 
				"	<M> void m(@NonNull Supplier<@NonNull M> m2) { } \n" + 
				"	void testSw(int i) { \n" + 
				"		m(switch(i) { \n" + 
				"			default -> () -> n_; }); \n" + 
				"	}\n" + 
				"}\n" +
				"interface Supplier<T> {\n" + 
				"    T get();\n" + 
				"}");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "() -> n_";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"n_1[METHOD_REF]{n_1(), LX;, ()LIN1;, n_1, null, 52}\n" + 
				"n_2[METHOD_REF]{n_2(), LX;, ()LIN2;, n_2, null, 52}",
						requestor.getResults());
	}
	public void test018e() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"import org.eclipse.jdt.annotation.*;\n" + 
				"interface IN0 {} \n" + 
				"interface IN1 extends IN0 {} \n" + 
				"interface IN2 extends IN0 {}\n" + 
				"public class X {\n" + 
				"	@NonNull IN1 n_1() { return new IN1() {}; } \n" + 
				"	IN2 n_2() { return null; } \n" + 
				"	<M> void m(@NonNull Supplier<@NonNull M> m2) { } \n" + 
				"	void testSw(int i) { \n" + 
				"		m(switch(i) { \n" + 
				"			case 1 -> this::n_1; \n" + 
				"			case 2 -> () -> n_; \n" + 
				"	}\n" + 
				"}\n" +
				"interface Supplier<T> {\n" + 
				"    T get();\n" + 
				"}");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "() -> n_";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"n_1[LOCAL_VARIABLE_REF]{n_1, null, Ljava.lang.Object;, n_1, null, 51}\n" + 
				"n_1[METHOD_REF]{n_1(), LX;, ()LIN1;, n_1, null, 52}\n" + 
				"n_2[METHOD_REF]{n_2(), LX;, ()LIN2;, n_2, null, 52}",
						requestor.getResults());
	}
	public void test018f() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"import org.eclipse.jdt.annotation.*;\n" + 
				"interface IN0 {} \n" + 
				"interface IN1 extends IN0 {} \n" + 
				"interface IN2 extends IN0 {}\n" + 
				"public class X {\n" + 
				"	@NonNull IN1 n_1() { return new IN1() {}; } \n" + 
				"	IN2 n_2() { return null; } \n" + 
				"	<M> void m(@NonNull Supplier<@NonNull M> m2) { } \n" + 
				"	void testSw(int i) { \n" + 
				"		m(switch(i) { \n" + 
				"			case 1 -> () -> n_1; \n" + 
				"			case 2 -> this::n_; \n" + 
				"	}\n" + 
				"}\n" +
				"interface Supplier<T> {\n" + 
				"    T get();\n" + 
				"}");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "this::n_";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"n_1[METHOD_NAME_REFERENCE]{n_1, LX;, ()LIN1;, n_1, null, 60}\n" + 
				"n_2[METHOD_NAME_REFERENCE]{n_2, LX;, ()LIN2;, n_2, null, 60}",
						requestor.getResults());
	}
	public void test019() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"import org.eclipse.jdt.annotation.*;\n" + 
				"	<M> void m(@NonNull Supplier<@NonNull M> m2) { } \n" + 
				"public class X {\n" + 
				"	void testSw(int i) { \n" + 
				"		m(swi);\n" + 
				"}\n" +
				"interface Supplier<T> {\n" + 
				"    T get();\n" + 
				"}");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "swi";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"switch[KEYWORD]{switch, null, null, switch, null, 49}",
						requestor.getResults());
	}
	public void test020() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"public class X {\n" + 
				"	static final String MONDAY = \"MONDAY\";\n" + 
				"	static final String TUESDAY = \"TUESDAY\";\n" + 
				"	static final String WEDNESDAY = \"WEDNESDAY\";\n" + 
				"	static final String THURSDAY = \"THURSDAY\";\n" + 
				"	static final String FRIDAY = \"FRIDAY\";\n" + 
				"	static final String SATURDAY = \"SATURDAY\";\n" + 
				"	static final String SUNDAY = \"SUNDAY\"; \n" + 
				"	@SuppressWarnings(\"preview\")\n" + 
				"	public static void main(String[] args) {\n" + 
				"		String day = \"MONDAY\";\n" + 
				"		switch (day) {\n" + 
				"		    case MONDAY, FRIDAY, SUNDAY  -> System.out.println(6);\n" + 
				"		    case TUESDAY           		 -> System.out.println(7);\n" + 
				"		    case THURSDAY, SATURDAY     -> System.out.println(8);\n" + 
				"		    case WEDNESDAY              -> System.out.println(9);\n" + 
				"		}\n" + 
				"		int k = switch (day) {\n" + 
				"	    case MONDAY  -> throw new NullPointerException();\n" + 
				"	    case TUESDAY -> 1;\n" + 
				"	    case WEDNESDAY -> {break 10;}\n" + 
				"	    default      -> {\n" + 
				"	        int g = day.h();\n" + 
				"	        int result = f(g);\n" + 
				"	        break result;\n" + 
				"	    }};\n" + 
				"	}\n" + 
				"	static int f(int k) {\n" + 
				"		return k*k;\n" + 
				"	}\n" + 
				"}");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "day.h";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"hashCode[METHOD_REF]{hashCode, Ljava.lang.Object;, ()I, hashCode, null, 90}",
						requestor.getResults());
	}
	public void test021() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"public class X {\n" + 
				"	static final String MONDAY = \"MONDAY\";\n" + 
				"	static final String TUESDAY = \"TUESDAY\";\n" + 
				"	static final String WEDNESDAY = \"WEDNESDAY\";\n" + 
				"	static final String THURSDAY = \"THURSDAY\";\n" + 
				"	static final String FRIDAY = \"FRIDAY\";\n" + 
				"	static final String SATURDAY = \"SATURDAY\";\n" + 
				"	static final String SUNDAY = \"SUNDAY\"; \n" + 
				"	@SuppressWarnings(\"preview\")\n" + 
				"	public static void main(String[] args) {\n" + 
				"		String day = \"MONDAY\";\n" + 
				"		int k = switch (day) {\n" + 
				"	    case MONDAY  -> throw new NullPointerException();\n" + 
				"	    case TUESDAY -> 1;\n" + 
				"	    case WEDNESDAY -> {break 10;}\n" + 
				"	    default      -> {\n" + 
				"	        int g = day.h();\n" + 
				"	        int result = f(g);\n" + 
				"	        break result;\n" + 
				"	    }};\n" + 
				"	}\n" + 
				"	static int f(int k) {\n" + 
				"		return k*k;\n" + 
				"	}\n" + 
				"}");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "day.h";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"hashCode[METHOD_REF]{hashCode, Ljava.lang.Object;, ()I, hashCode, null, 90}",
						requestor.getResults());
	}
	/*
	 * Try completion for break keyword inside switch expression - negative
	 */
	public void test022() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"public class X {\n" + 
				"	static final String MONDAY = \"MONDAY\";\n" + 
				"	static final String TUESDAY = \"TUESDAY\";\n" + 
				"	static final String WEDNESDAY = \"WEDNESDAY\";\n" + 
				"	@SuppressWarnings(\"preview\")\n" + 
				"	public static void main(String[] args) {\n" + 
				"		String day = \"MONDAY\";\n" + 
				"		int k = switch (day) {\n" + 
				"	    case MONDAY  -> throw new NullPointerException();\n" + 
				"	    case TUESDAY -> 1;\n" + 
				"	    case WEDNESDAY -> {br;}\n" + 
				"	    default      -> {\n" + 
				"	        int g = day.hashCode();\n" + 
				"	        int result = f(g);\n" + 
				"	        yield result;\n" + 
				"	    }};\n" + 
				"	}\n" + 
				"	static int f(int k) {\n" + 
				"		return k*k;\n" + 
				"	}\n" + 
				"}");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "case WEDNESDAY -> {br";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"break[KEYWORD]{break, null, null, break, null, 49}",
						requestor.getResults());
	}
	/*
	 * Try completion for yield keyword - positive
	 */
	public void test023() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"""
					public class X {
						@SuppressWarnings("preview")
						public static void main(String[] args) {
							String day = "MONDAY";
							int k = switch (day) {
						    case "M"  -> throw new NullPointerException();
						    case "T" -> 1;
						    case "W" -> {yi ;}
						    default      -> {
						        int g = day.hashCode();
						        int result = f(g);
						        yield result;
						    }};
						}
						static int f(int k) {
							return k*k;
						}
					}
				""");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "case \"W\" -> {yi";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"yield[KEYWORD]{yield, null, null, yield, null, 49}",
						requestor.getResults());
	}
	/*
	 * Try completion for yield with identifier - positive
	 */
	public void test024() throws JavaModelException {
		String old = COMPLETION_PROJECT.getOption(CompilerOptions.OPTION_EnablePreviews, true);
		COMPLETION_PROJECT.setOption(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"public class X {\n" + 
				"	@SuppressWarnings(\"preview\")\n" + 
				"	public static void main(String day) {\n" + 
				"		int k = switch (day) {\n" + 
				"	    default      -> {\n" + 
				"	        int g = day.hashCode();\n" + 
				"	        int result = f(g);\n" + 
				"	        yield res;\n" + 
				"	    }};\n" + 
				"	}\n" + 
				"	static int f(int k) {\n" + 
				"		return k*k;\n" + 
				"	}\n" + 
				"}");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "yield res";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"result[LOCAL_VARIABLE_REF]{result, null, I, result, null, 52}",
						requestor.getResults());
		COMPLETION_PROJECT.setOption(CompilerOptions.OPTION_EnablePreviews, old);
	}
	/*
	 * Try completion for yield with identifier with a preceding case with yield - positive
	 */
	public void test024a() throws JavaModelException {
		String old = COMPLETION_PROJECT.getOption(CompilerOptions.OPTION_EnablePreviews, true);
		COMPLETION_PROJECT.setOption(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"public class X {\n" + 
				"	@SuppressWarnings(\"preview\")\n" + 
				"	public static void main(String day) {\n" + 
				"		int k = switch (day) {\n" + 
				"	    case TUESDAY -> { yield 1;}\n" + 
				"	    default      -> {\n" + 
				"	        int g = day.hashCode();\n" + 
				"	        int result = f(g);\n" + 
				"	        yield res;\n" + 
				"	    }};\n" + 
				"	}\n" + 
				"	static int f(int k) {\n" + 
				"		return k*k;\n" + 
				"	}\n" + 
				"}");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "yield res";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"result[LOCAL_VARIABLE_REF]{result, null, I, result, null, 52}",
						requestor.getResults());
		COMPLETION_PROJECT.setOption(CompilerOptions.OPTION_EnablePreviews, old);
	}
	/*
	 * Try completion for yield with identifier with a preceding case with yield - positive
	 */
	public void test024b() throws JavaModelException {
		String old = COMPLETION_PROJECT.getOption(CompilerOptions.OPTION_EnablePreviews, true);
		COMPLETION_PROJECT.setOption(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"public class X {\n" + 
				"	@SuppressWarnings(\"preview\")\n" + 
				"	public static void main(String day) {\n" + 
				"		int k = switch (day) {\n" + 
				"	    case TUESDAY -> { yield 1;}\n" + 
				"	    default      -> {\n" + 
				"	        int g = day.hashCode();\n" + 
				"	        int result = f(g);\n" + 
				"	        yield 0 + res;\n" + 
				"	    }};\n" + 
				"	}\n" + 
				"	static int f(int k) {\n" + 
				"		return k*k;\n" + 
				"	}\n" + 
				"}");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "yield 0 + res";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"result[LOCAL_VARIABLE_REF]{result, null, I, result, null, 52}",
						requestor.getResults());
		COMPLETION_PROJECT.setOption(CompilerOptions.OPTION_EnablePreviews, old);
	}
	/*
	 * Try completion in a switch expression case block without any assist keyword
	 */
	public void test024c() throws JavaModelException {
		String old = COMPLETION_PROJECT.getOption(CompilerOptions.OPTION_EnablePreviews, true);
		COMPLETION_PROJECT.setOption(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"public class X {\n" + 
				"	@SuppressWarnings(\"preview\")\n" + 
				"	public static void main(String day) {\n" + 
				"		int k = switch (day) {\n" + 
				"	    case TUESDAY -> { yield 1;}\n" + 
				"	    default      -> {\n" + 
				"	        int[] g = new int[0];\n" + 
				"	        yield g.;\n" + 
				"	    }};\n" + 
				"	}\n" + 
				"	static int f(int k) {\n" + 
				"		return k*k;\n" + 
				"	}\n" + 
				"}");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "yield g.";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"length[FIELD_REF]{length, [I, I, length, null, 49}\n" + 
				"clone[METHOD_REF]{clone(), [I, ()[I, clone, null, 60}\n" + 
				"equals[METHOD_REF]{equals(), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), 60}\n" + 
				"finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, 60}\n" + 
				"getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<+Ljava.lang.Object;>;, getClass, null, 60}\n" + 
				"hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, 60}\n" + 
				"notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, 60}\n" + 
				"notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, 60}\n" + 
				"toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, 60}\n" + 
				"wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, 60}\n" + 
				"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, wait, (millis), 60}\n" + 
				"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), 60}",
						requestor.getResults());
		COMPLETION_PROJECT.setOption(CompilerOptions.OPTION_EnablePreviews, old);
	}
	/*
	 * Try completion in a switch expression case block without any assist keyword
	 */
	public void test024d() throws JavaModelException {
		String old = COMPLETION_PROJECT.getOption(CompilerOptions.OPTION_EnablePreviews, true);
		COMPLETION_PROJECT.setOption(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"public class X {\n" + 
				"	@SuppressWarnings(\"preview\")\n" + 
				"	public static void main(String[] args) {\n" + 
				"	String day = args[0];\n" + 
				"		int k = switch (day) {\n" + 
				"	    case TUESDAY -> { yield 1;}\n" + 
				"	    default      -> {\n" + 
				"	        int[] g = args.;\n" + 
				"	        yield g;\n" + 
				"	    }};\n" + 
				"	}\n" + 
				"	static int f(int k) {\n" + 
				"		return k*k;\n" + 
				"	}\n" + 
				"}");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "int[] g = args.";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"length[FIELD_REF]{length, [Ljava.lang.String;, I, length, null, 49}\n" + 
				"finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, 55}\n" + 
				"notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, 55}\n" + 
				"notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, 55}\n" + 
				"wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, 55}\n" + 
				"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, wait, (millis), 55}\n" + 
				"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), 55}\n" + 
				"clone[METHOD_REF]{clone(), [Ljava.lang.String;, ()[Ljava.lang.String;, clone, null, 60}\n" + 
				"equals[METHOD_REF]{equals(), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), 60}\n" + 
				"getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<+Ljava.lang.Object;>;, getClass, null, 60}\n" + 
				"hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, 60}\n" + 
				"toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, 60}",
						requestor.getResults());
		COMPLETION_PROJECT.setOption(CompilerOptions.OPTION_EnablePreviews, old);
	}
	/*
	 * Try completion for yield with value inside switch - positive
	 * 
	 */
	public void test025() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"public class X {\n" + 
				"	static final String MONDAY = \"MONDAY\";\n" + 
				"	static final String TUESDAY = \"TUESDAY\";\n" + 
				"	static final String WEDNESDAY = \"WEDNESDAY\";\n" + 
				"	@SuppressWarnings(\"preview\")\n" + 
				"	public static void main(String[] args) {\n" + 
				"	 resolve: {" + 
				"		String day = \"MONDAY\";\n" + 
				"		int k = switch (day) {\n" + 
				"	    case MONDAY  -> throw new NullPointerException();\n" + 
				"	    case TUESDAY -> 1;\n" + 
				"	    case WEDNESDAY -> {yield 10;}\n" + 
				"	    default      -> {\n" + 
				"	        yield day.h;\n" + 
				"	    }};\n" + 
				"	 }" + 
				"	}\n" + 
				"	static int f(int k) {\n" + 
				"		return k*k;\n" + 
				"	}\n" + 
				"}");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "yield day.h";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, 60}",
						requestor.getResults());
	}
	/*
	 * Try completion for yield keyword - inside nested block
	 */
	public void test026() throws JavaModelException {
		String old = COMPLETION_PROJECT.getOption(CompilerOptions.OPTION_EnablePreviews, true);
		COMPLETION_PROJECT.setOption(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"""
					public class X {
						@SuppressWarnings("preview")
						public static void main(String day) {
							int k = switch (day) {
						    case "W" -> {yield 2;}
						    default      -> {
						        int g = day.hashCode();
						        int result = f(g);
						        {
						        	yie;
						        }
						    }};
						}
						static int f(int k) {
							return k*k;
						}
					}
				""");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "yie";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"yield[KEYWORD]{yield, null, null, yield, null, 49}",
						requestor.getResults());
		COMPLETION_PROJECT.setOption(CompilerOptions.OPTION_EnablePreviews, old);
	}
	/*
	 * Try completion for yield - inside nested block
	 */
	public void test027() throws JavaModelException {
		String old = COMPLETION_PROJECT.getOption(CompilerOptions.OPTION_EnablePreviews, true);
		COMPLETION_PROJECT.setOption(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"""
					public class X {
						@SuppressWarnings("preview")
						public static void main(String day) {
							int k = switch (day) {
						    case "W" -> {yield 2;}
						    default      -> {
						        int g = day.hashCode();
						        int result = f(g);
						        {
						        	yield res;
						        }
						    }};
						}
						static int f(int k) {
							return k*k;
						}
					}
				""");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "yield res";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"result[LOCAL_VARIABLE_REF]{result, null, I, result, null, 52}",
						requestor.getResults());
		COMPLETION_PROJECT.setOption(CompilerOptions.OPTION_EnablePreviews, old);
	}
	/*
	 * Try completion for yield - inside nested block
	 */
	public void test028() throws JavaModelException {
		String old = COMPLETION_PROJECT.getOption(CompilerOptions.OPTION_EnablePreviews, true);
		COMPLETION_PROJECT.setOption(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"""
					public class X {
						@SuppressWarnings("preview")
						public static void main(String day) {
							int k = switch (day) {
						    case "W" -> {yield 2;}
						    default      -> {
						        int g = day.hashCode();
						        int result = f(g);
						        {
						        	for(int i = 0; i < 3; i++) {
						        		if (i == 0) {
						        			yield day.h
						        		}
						        	}
						        	yield 0;
						        }
						    }};
						}
						static int f(int k) {
							return k*k;
						}
					}
				""");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "yield day.h";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, 60}",
						requestor.getResults());
		COMPLETION_PROJECT.setOption(CompilerOptions.OPTION_EnablePreviews, old);
	}
	/*
	 * Try completion of other keywords inside switch expression nested block
	 */
	public void test029() throws JavaModelException {
		String old = COMPLETION_PROJECT.getOption(CompilerOptions.OPTION_EnablePreviews, true);
		COMPLETION_PROJECT.setOption(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"""
					public class X {
						@SuppressWarnings("preview")
						public static void main(String day) {
							int k = switch (day) {
						    case "W" -> {yield 2;}
						    default      -> {
						        int g = day.hashCode();
						        int result = f(g);
						        {
						        	for(int i = 0; i < 3; i++) {
						        		if (i == 0) {
						        			thr
						        		}
						        	}
						        	yield 0;
						        }
						    }};
						}
						static int f(int k) {
							return k*k;
						}
					}
				""");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "thr";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"Throwable[TYPE_REF]{Throwable, java.lang, Ljava.lang.Throwable;, null, null, 42}\n" + 
				"throw[KEYWORD]{throw, null, null, throw, null, 49}",
						requestor.getResults());
		COMPLETION_PROJECT.setOption(CompilerOptions.OPTION_EnablePreviews, old);
	}
	/*
	 * Try completion of other keywords inside switch expression nested block
	 */
	public void test030() throws JavaModelException {
		String old = COMPLETION_PROJECT.getOption(CompilerOptions.OPTION_EnablePreviews, true);
		COMPLETION_PROJECT.setOption(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"""
					public class X {
						@SuppressWarnings("preview")
						public static void main(String day) {
							int k = switch (day) {
						    case "W" -> {yield 2;}
						    default      -> {
						        int g = day.hashCode();
						        int result = f(g);
						        {
						        	for(int i = 0; i < 3; i++) {
						        		if (i == 0) {
						        			throw Exc
						        		}
						        	}
						        	yield 0;
						        }
						    }};
						}
						static int f(int k) {
							return k*k;
						}
					}
				""");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "throw Exc";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"Exception[TYPE_REF]{Exception, java.lang, Ljava.lang.Exception;, null, null, 52}",
						requestor.getResults());
		COMPLETION_PROJECT.setOption(CompilerOptions.OPTION_EnablePreviews, old);
	}
	/*
	 * Try completion inside a lambda nested inside a switch expression
	 */
	public void test031() throws JavaModelException {
		String old = COMPLETION_PROJECT.getOption(CompilerOptions.OPTION_EnablePreviews, true);
		COMPLETION_PROJECT.setOption(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"""
				public class X {
					@SuppressWarnings("preview")
					public static void main(String[] args) {
						String day = "";
						int i = switch (day) {
						default -> {
							for (int j = 0; j < 3; j++) {
								if (j == 0) {
									IntPredicate pre = (_value) -> !test(_v);
									};
								}
							}
							yield 0;
						}
						};
					}
					private static boolean test(int value) {
						return false;
					}
				}
				interface IntPredicate {
					boolean test(int value);
				}
				""");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "test(_v";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"_value[LOCAL_VARIABLE_REF]{_value, null, I, _value, null, 52}",
						requestor.getResults());
		COMPLETION_PROJECT.setOption(CompilerOptions.OPTION_EnablePreviews, old);
	}
	/*
	 * Try completion inside a lambda (block) nested inside a switch expression
	 */
	public void test032() throws JavaModelException {
		String old = COMPLETION_PROJECT.getOption(CompilerOptions.OPTION_EnablePreviews, true);
		COMPLETION_PROJECT.setOption(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"""
				public class X {
					@SuppressWarnings("preview")
					public static void main(String[] args) {
						String day = "";
						int i = switch (day) {
						default -> {
							for (int j = 0; j < 3; j++) {
								if (j == 0) {
									IntPredicate pre = (_value) -> {
										return !test(_v);
									};
								}
							}
							yield 0;
						}
						};
					}
					private static boolean test(int value) {
						return false;
					}
				}
				interface IntPredicate {
					boolean test(int value);
				}
				""");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "test(_v";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"_value[LOCAL_VARIABLE_REF]{_value, null, I, _value, null, 52}",
						requestor.getResults());
		COMPLETION_PROJECT.setOption(CompilerOptions.OPTION_EnablePreviews, old);
	}
	/*
	 * Try completion inside a switch expression nested inside a lambda expression
	 */
	public void test033() throws JavaModelException {
		String old = COMPLETION_PROJECT.getOption(CompilerOptions.OPTION_EnablePreviews, true);
		COMPLETION_PROJECT.setOption(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"""
				public class X {
					@SuppressWarnings("preview")
					public static void main(String[] args) {
						IntPredicate pre = (_value) -> !test(
								switch (_value) {
									default -> {
										yield _v;
									}
								}
								);
					}
					private static boolean test(int value) {
						return false;
					}
				}
				interface IntPredicate {
					boolean test(int value);
				}
				""");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "yield _v";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"_value[LOCAL_VARIABLE_REF]{_value, null, I, _value, null, 52}",
						requestor.getResults());
		COMPLETION_PROJECT.setOption(CompilerOptions.OPTION_EnablePreviews, old);
	}
	/*
	 * Try completion inside a switch expression nested inside a lambda expression
	 */
	public void test034() throws JavaModelException {
		String old = COMPLETION_PROJECT.getOption(CompilerOptions.OPTION_EnablePreviews, true);
		COMPLETION_PROJECT.setOption(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"""
				public class X {
					@SuppressWarnings("preview")
					public static void main(String[] args) {
						IntPredicate pre = (_value) -> {
							return !test(
								switch (_value) {
									default -> {
										yield _v;
									}
								}
								);
						};
					}
					private static boolean test(int value) {
						return false;
					}
				}
				interface IntPredicate {
					boolean test(int value);
				}
				""");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "yield _v";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"_value[LOCAL_VARIABLE_REF]{_value, null, I, _value, null, 52}",
						requestor.getResults());
		COMPLETION_PROJECT.setOption(CompilerOptions.OPTION_EnablePreviews, old);
	}
	public void testBug545783() throws JavaModelException {
		String old = COMPLETION_PROJECT.getOption(CompilerOptions.OPTION_EnablePreviews, true);
		COMPLETION_PROJECT.setOption(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/bugs/C.java",
				"""
				package bugs;
				public class C {
					int foo(String str) {
						return switch (str) {
						case "x", "y" -> {
							yield 0;
						}
						default -> {
							i: for (int i = 0; i < 10; i++) {
								if (str.) {
									yield i;
								}
							}
							yield -1;
						}
						};
					}
				}
				""");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "if (str.";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, 55}\n" + 
				"notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, 55}\n" + 
				"notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, 55}\n" + 
				"wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, 55}\n" + 
				"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, wait, (millis), 55}\n" + 
				"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), 55}\n" + 
				"clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, 60}\n" + 
				"getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<+Ljava.lang.Object;>;, getClass, null, 60}\n" + 
				"hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, 60}\n" + 
				"length[METHOD_REF]{length(), Ljava.lang.String;, ()I, length, null, 60}\n" + 
				"toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, 60}\n" + 
				"equals[METHOD_REF]{equals(), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), 90}",
						requestor.getResults());
		COMPLETION_PROJECT.setOption(CompilerOptions.OPTION_EnablePreviews, old);
	}
}
