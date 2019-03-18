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

import junit.framework.Test;

public class CompletionTests12 extends AbstractJavaModelCompletionTests {

	static {
//		TESTS_NAMES = new String[]{"test018e"};
	}

	public CompletionTests12(String name) {
		super(name);
	}
	public void setUpSuite() throws Exception {
		if (COMPLETION_PROJECT == null)  {
			COMPLETION_PROJECT = setUpJavaProject("Completion", "12");
		} else {
			setUpProjectCompliance(COMPLETION_PROJECT, "12");
		}
		super.setUpSuite();
	}
	public static Test suite() {
		return buildModelTestSuite(CompletionTests12.class);
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
				"public class Switch {\n" + 
						"	public static void bar(MyDay day) {\n" + 
						"		switch (day.toS) {\n" + 
						"		case \"SATURDAY\" -> \n" + 
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
	public void _test015() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"public class Switch {\n" + 
						"	public static void bar(int arg0) {\n" + 
						"		foo(\n" + 
						"		switch (0 + arg0) {\n" + 
						"		case 1 -> {break ar;}\n" + 
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
		String completeBehind = "break ar";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"arg0[LOCAL_VARIABLE_REF]{arg0, null, I, arg0, null, 52}",
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
	public void _test017() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"public class Switch {\n" + 
						"	public static void bar(int arg0) {\n" + 
						"		int arg1 = 0;\n" + 
						"		foo(\n" + 
						"		switch (0 + arg0) {\n" + 
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
				"arg0[LOCAL_VARIABLE_REF]{arg0, null, I, arg0, null, 52}",
						requestor.getResults());
	}
	public void _test017a() throws JavaModelException {
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
				"arg0[LOCAL_VARIABLE_REF]{arg0, null, I, arg0, null, 52}\n" +
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
}
