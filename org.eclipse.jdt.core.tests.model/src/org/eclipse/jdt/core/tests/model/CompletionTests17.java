/*******************************************************************************
 * Copyright (c) 2021, 2022 IBM Corporation and others.
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

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import junit.framework.Test;

public class CompletionTests17 extends AbstractJavaModelCompletionTests {

	private static int expected_Rel = R_DEFAULT+R_RESOLVED+ R_CASE+ R_INTERESTING+R_EXACT_EXPECTED_TYPE+R_NON_STATIC+R_NON_RESTRICTED;
	private static int void_Rel = R_DEFAULT+R_RESOLVED+ R_CASE+ R_INTERESTING+ R_VOID +R_NON_STATIC+R_NON_RESTRICTED;
	private static int nonVoid_Rel = R_DEFAULT+R_RESOLVED+ R_CASE+ R_INTERESTING +R_NON_STATIC+R_NON_RESTRICTED;
	private static int unqualified_Rel = R_DEFAULT+R_RESOLVED+ R_CASE+ R_INTERESTING +R_UNQUALIFIED+R_NON_RESTRICTED;
	private static int unqualifiedExact_Rel = R_DEFAULT+R_RESOLVED+R_EXACT_EXPECTED_TYPE+ R_CASE+ R_INTERESTING +R_UNQUALIFIED+R_NON_RESTRICTED;
	private static int keyword_Rel= R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED;
	static {
		// TESTS_NAMES = new String[]{"test034"};
	}

	public CompletionTests17(String name) {
		super(name);
	}

	public void setUpSuite() throws Exception {
		if (COMPLETION_PROJECT == null) {

			COMPLETION_PROJECT = setUpJavaProject("Completion", "17");
		} else {
			setUpProjectCompliance(COMPLETION_PROJECT, "17");
		}
		super.setUpSuite();
		COMPLETION_PROJECT.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
	}

	public static Test suite() {
		return buildModelTestSuite(CompletionTests17.class);
	}
	//content assist of a java lang class in case statement in switch pattern
	public void test001() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"public class X {\n" +
						"public static void main(String[] args) {\n" +
						"foo(Integer.valueOf(5));\n" +
						"foo(new Object());\n" +
						"}\n" +
						"private static void foo(Object o) {\n" +
						" switch (o) {\n" +
						"	case Integer i     -> System.out.println(\"Integer:\" + i);\n" +
						"	case String /*here*/s     -> System.out.println(\"String:\" + s.);\n" +
						"	default       -> System.out.println(\"Object\" + o);\n" +
						" 	}\n" +
						"}\n" +
						"}\n"
				);
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "s.";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, "+void_Rel+"}\n"
				+ "notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, "+void_Rel+"}\n"
				+ "notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, "+void_Rel+"}\n"
				+ "wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, "+void_Rel+"}\n"
				+ "wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, wait, (millis), "+void_Rel+"}\n"
				+ "wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), "+void_Rel+"}\n"
				+ "clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, "+nonVoid_Rel+"}\n"
				+ "equals[METHOD_REF]{equals(), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), "+nonVoid_Rel+"}\n"
				+ "getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<+Ljava.lang.Object;>;, getClass, null, "+nonVoid_Rel+"}\n"
				+ "codePointAt[METHOD_REF]{codePointAt(), Ljava.lang.String;, (I)I, codePointAt, (index), "+expected_Rel+"}\n"
				+ "hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, "+expected_Rel+"}\n"
				+ "length[METHOD_REF]{length(), Ljava.lang.String;, ()I, length, null, "+expected_Rel+"}\n"
				+ "toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, "+expected_Rel+"}",
				requestor.getResults());

	}

	//content assist of a local variable in case statement in switch pattern
	public void test002() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"public class X {\n" +
						"public static  int field \n" +
						"public static void main(String[] args) {\n" +
						"foo(Integer.valueOf(5));\n" +
						"foo(new Object());\n" +
						"}\n" +
						"private static void foo(Object o) {\n" +
						" switch (o) {\n" +
						"	case Integer i   -> System.out.println(\"Integer:\" + i);\n" +
						"	case String s     -> System.out.println(\"String:\" + s + /*here*/fie);\n" +
						"	default       -> System.out.println(\"Object\" + o);\n" +
						" 	}\n" +
						"}\n" +
						"}\n"
				);
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "/*here*/fie";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("field[FIELD_REF]{field, LX;, I, field, null, "+unqualifiedExact_Rel+"}",
				requestor.getResults());

	}

	//content assist of a field  in guarded expression in switch pattern
	public void test003() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"public class X {\n" +
						"public static  int field; \n" +
						"public static void main(String[] args) {\n" +
						"foo(Integer.valueOf(5));\n" +
						"foo(new Object());\n" +
						"}\n" +
						"private static void foo(Object o) {\n" +
						" switch (o) {\n" +
						"	case Integer i   -> System.out.println(\"Integer:\" + i);\n" +
						"	case String s && /*here*/fie    -> System.out.println(\"String:\" + s );\n" +
						"	default       -> System.out.println(\"Object\" + o);\n" +
						" 	}\n" +
						"}\n" +
						"}\n"
				);
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "/*here*/fie";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("field[FIELD_REF]{field, LX;, I, field, null, "+unqualified_Rel+"}",
				requestor.getResults());

	}
	//content assist of a primitive local  in switch pattern's case statement
	public void test004() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"public class X {\n" +
						"public static  int /*here*/field \n" +
						"public static void main(String[] args) {\n" +
						"foo(Integer.valueOf(5));\n" +
						"foo(new Object());\n" +
						"}\n" +
						"private static void foo(Object o) {\n" +
						" int local=0;" +
						" switch (o) {\n" +
						"	case Integer i   -> System.out.println(\"Integer:\" + i);\n" +
						"	case String s     -> System.out.println(\"String:\" + s + /*here*/loc);\n" +
						"	default       -> System.out.println(\"Object\" + o);\n" +
						" 	}\n" +
						"}\n" +
						"}\n"
				);
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "/*here*/loc";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("local[LOCAL_VARIABLE_REF]{local, null, I, local, null, "+unqualifiedExact_Rel+"}",
				requestor.getResults());



	}

	//content assist of a primitive local  in switch pattern's guarded expression
	public void test005() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"public class X {\n" +
						"public static  int /*here*/field \n" +
						"public static void main(String[] args) {\n" +
						"foo(Integer.valueOf(5));\n" +
						"foo(new Object());\n" +
						"}\n" +
						"private static void foo(Object o) {\n" +
						" int local=0;" +
						" switch (o) {\n" +
						"	case Integer i   -> System.out.println(\"Integer:\" + i);\n" +
						"	case String s  && /*here*/loc   -> System.out.println(\"String:\" + s);\n" +
						"	default       -> System.out.println(\"Object\" + o);\n" +
						" 	}\n" +
						"}\n" +
						"}\n"
				);
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "/*here*/loc";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("local[LOCAL_VARIABLE_REF]{local, null, I, local, null, "+unqualified_Rel+"}",
				requestor.getResults());

	}

	//content assist of a class field  in switch pattern's guarded expression
	public void test006() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"public class X {\n" +
						"public static  String field = new String();\n" +
						"public static void main(String[] args) {\n" +
						"foo(Integer.valueOf(5));\n" +
						"foo(new Object());\n" +
						"}\n" +
						"private static void foo(Object o) {\n" +
						" switch (o) {\n" +
						"	case Integer i   -> System.out.println(\"Integer:\" + i);\n" +
						"	case String s && /*here*/field.   -> System.out.println(\"String:\" + s );\n" +
						"	default       -> System.out.println(\"Object\" + o);\n" +
						" 	}\n" +
						"}\n" +
						"}\n"
				);
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "/*here*/field.";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, "+nonVoid_Rel+"}\n"
				+ "codePointAt[METHOD_REF]{codePointAt(), Ljava.lang.String;, (I)I, codePointAt, (index), "+nonVoid_Rel+"}\n"
				+ "equals[METHOD_REF]{equals(), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), "+nonVoid_Rel+"}\n"
				+ "finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, "+nonVoid_Rel+"}\n"
				+ "getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<+Ljava.lang.Object;>;, getClass, null, "+nonVoid_Rel+"}\n"
				+ "hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, "+nonVoid_Rel+"}\n"
				+ "length[METHOD_REF]{length(), Ljava.lang.String;, ()I, length, null, "+nonVoid_Rel+"}\n"
				+ "notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, "+nonVoid_Rel+"}\n"
				+ "notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, "+nonVoid_Rel+"}\n"
				+ "toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, "+nonVoid_Rel+"}\n"
				+ "wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, "+nonVoid_Rel+"}\n"
				+ "wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, wait, (millis), "+nonVoid_Rel+"}\n"
				+ "wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), "+nonVoid_Rel+"}",
				requestor.getResults());

	}
	//content assist of a custom class  in switch pattern's guarded expression ( switch statement)
	public void test007() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"sealed interface I permits AClass, B {}\n" +
						"final class AClass implements S {}\n" +
						"final class B implements S {}\n" +
						"public class X {\n" +
						"public static void main(String[] args) {\n" +
						"foo(new A());\n" +
						"}\n" +
						"private static void foo(S o) {\n" +
						" switch (o) {\n" +
						"	case *here*ACla :     System.out.println(\"A:\" + a +a); break;\n" +
						"	case B b :     System.out.println(\"B:\" + b);\n" +
						"	default  : System.out.println(\"Object\" + o);\n" +
						" 	}\n" +
						"}\n" +
						"}\n"
				);
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "*here*ACla";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("AClass[TYPE_REF]{AClass, , LAClass;, null, null, "+unqualified_Rel+"}",
				requestor.getResults());

	}

	//content assist of a custom class's methods  in switch pattern's guarded expression ( switch statement)
	public void test008() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"sealed interface I permits AClass, B {}\n" +
						"final class AClass implements S {}\n" +
						"final class B implements S {}\n" +
						"public class X {\n" +
						"public static void main(String[] args) {\n" +
						"foo(new A());\n" +
						"}\n" +
						"private static void foo(S o) {\n" +
						" switch (o) {\n" +
						"	case AClass a && a. :     System.out.println(\"A:\" + a +a); break;\n" +
						"	case B b :     System.out.println(\"B:\" + b);\n" +
						"	default  : System.out.println(\"Object\" + o);\n" +
						" 	}\n" +
						"}\n" +
						"}\n"
				);
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "a.";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, "+nonVoid_Rel+"}\n"
				+ "equals[METHOD_REF]{equals(), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), "+nonVoid_Rel+"}\n"
				+ "finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, "+nonVoid_Rel+"}\n"
				+ "getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<+Ljava.lang.Object;>;, getClass, null, "+nonVoid_Rel+"}\n"
				+ "hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, "+nonVoid_Rel+"}\n"
				+ "notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, "+nonVoid_Rel+"}\n"
				+ "notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, "+nonVoid_Rel+"}\n"
				+ "toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, "+nonVoid_Rel+"}\n"
				+ "wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, "+nonVoid_Rel+"}\n"
				+ "wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, wait, (millis), "+nonVoid_Rel+"}\n"
				+ "wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), "+nonVoid_Rel+"}",
				requestor.getResults());

	}
	//content assist of a java lang class  in switch pattern's  case statement ( switch statement)
	public void test009() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"public class X {\n" +
						"public static void main(String[] args) {\n" +
						"foo(Integer.valueOf(5));\n" +
						"foo(new Object());\n" +
						"}\n" +
						"private static void foo(Object o) {\n" +
						" switch (o) {\n" +
						"	case Integer i     : System.out.println(\"Integer:\" + i);break;\n" +
						"	case String /*here*/s     : System.out.println(\"String:\" + s.);break;\n" +
						"	default       : System.out.println(\"Object\" + o);\n" +
						" 	}\n" +
						"}\n" +
						"}\n"
				);
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "s.";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, "+void_Rel+"}\n"
				+ "notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, "+void_Rel+"}\n"
				+ "notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, "+void_Rel+"}\n"
				+ "wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, "+void_Rel+"}\n"
				+ "wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, wait, (millis), "+void_Rel+"}\n"
				+ "wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), "+void_Rel+"}\n"
				+ "clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, "+nonVoid_Rel+"}\n"
				+ "equals[METHOD_REF]{equals(), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), "+nonVoid_Rel+"}\n"
				+ "getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<+Ljava.lang.Object;>;, getClass, null, "+nonVoid_Rel+"}\n"
				+ "codePointAt[METHOD_REF]{codePointAt(), Ljava.lang.String;, (I)I, codePointAt, (index), "+expected_Rel+"}\n"
				+ "hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, "+expected_Rel+"}\n"
				+ "length[METHOD_REF]{length(), Ljava.lang.String;, ()I, length, null, "+expected_Rel+"}\n"
				+ "toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, "+expected_Rel+"}",
				requestor.getResults());

	}

	//content assist of a java lang class  in switch pattern's  guarded expression ( switch statement)
	public void test010() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"public class X {\n" +
						"public static void main(String[] args) {\n" +
						"foo(Integer.valueOf(5));\n" +
						"foo(new Object());\n" +
						"}\n" +
						"private static void foo(Object o) {\n" +
						" switch (o) {\n" +
						"	case Integer i     : System.out.println(\"Integer:\" + i);break;\n" +
						"	case String /*here*/s  && s.   : System.out.println(\"String:\" + s);break;\n" +
						"	default       : System.out.println(\"Object\" + o);\n" +
						" 	}\n" +
						"}\n" +
						"}\n"
				);
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "s.";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, "+nonVoid_Rel+"}\n"
				+ "codePointAt[METHOD_REF]{codePointAt(), Ljava.lang.String;, (I)I, codePointAt, (index), "+nonVoid_Rel+"}\n"
				+ "equals[METHOD_REF]{equals(), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), "+nonVoid_Rel+"}\n"
				+ "finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, "+nonVoid_Rel+"}\n"
				+ "getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<+Ljava.lang.Object;>;, getClass, null, "+nonVoid_Rel+"}\n"
				+ "hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, "+nonVoid_Rel+"}\n"
				+ "length[METHOD_REF]{length(), Ljava.lang.String;, ()I, length, null, "+nonVoid_Rel+"}\n"
				+ "notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, "+nonVoid_Rel+"}\n"
				+ "notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, "+nonVoid_Rel+"}\n"
				+ "toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, "+nonVoid_Rel+"}\n"
				+ "wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, "+nonVoid_Rel+"}\n"
				+ "wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, wait, (millis), "+nonVoid_Rel+"}\n"
				+ "wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), "+nonVoid_Rel+"}",
				requestor.getResults());

	}
	public void test011() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"public class X {\n" +
						"public static void main(String[] args) {\n" +
						"foo(Integer.valueOf(5));\n" +
						"foo(new Object());\n" +
						"}\n" +
						"private static void foo(Object o) {\n" +
						" switch (o) {\n" +
						"	case     : System.out.println(\"Integer:\" + i);break;\n" +
						" 	}\n" +
						"}\n" +
						"}\n"
				);
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "case ";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("default[KEYWORD]{default, null, null, default, null, "+keyword_Rel+"}\n"
				+ "null[KEYWORD]{null, null, null, null, null, "+keyword_Rel+"}",
				requestor.getResults());

	}
	public void test012() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"public class X {\n" +
						"public static void main(String[] args) {\n" +
						"foo(Integer.valueOf(5));\n" +
						"foo(new Object());\n" +
						"}\n" +
						"private static void foo(Object o) {\n" +
						" switch (o) {\n" +
						"	case nu    : System.out.println(\"Integer:\" + i);break;\n" +
						" 	}\n" +
						"}\n" +
						"}\n"
				);
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "case nu";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("null[KEYWORD]{null, null, null, null, null, "+keyword_Rel+"}",
				requestor.getResults());

	}
	public void test013() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"public class X {\n" +
						"public static void main(String[] args) {\n" +
						"foo(Integer.valueOf(5));\n" +
						"foo(new Object());\n" +
						"}\n" +
						"private static void foo(Object o) {\n" +
						" switch (o) {\n" +
						"	case de    : System.out.println(\"Integer:\" + i);break;\n" +
						" 	}\n" +
						"}\n" +
						"}\n"
				);
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "case de";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("default[KEYWORD]{default, null, null, default, null, "+keyword_Rel+"}",
				requestor.getResults());

	}
	public void test014() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"public class X {\n" +
						"public static void main(String[] args) {\n" +
						"foo(Integer.valueOf(5));\n" +
						"foo(new Object());\n" +
						"}\n" +
						"private static void foo(Object o) {\n" +
						" switch (o) {\n" +
						"	case null    : System.out.println(\"Integer:\" + i);break;\n" +
						"	/*here*/case nu    : System.out.println(\"Integer:\" + i);break;\n" +
						" 	}\n" +
						"}\n" +
						"}\n"
				);
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "/*here*/case nu";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("",
				requestor.getResults());

	}
	public void test015() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"public class X {\n" +
						"public static void main(String[] args) {\n" +
						"foo(Integer.valueOf(5));\n" +
						"foo(new Object());\n" +
						"}\n" +
						"private static void foo(Object o) {\n" +
						" switch (o) {\n" +
						"	case default    : System.out.println(\"Integer:\" + i);break;\n" +
						"	/*here*/case de    : System.out.println(\"Integer:\" + i);break;\n" +
						" 	}\n" +
						"}\n" +
						"}\n"
				);
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "/*here*/case de";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("",
				requestor.getResults());

	}
	public void test016() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"public class X {\n" +
						"public static void main(String[] args) {\n" +
						"foo(Integer.valueOf(5));\n" +
						"foo(new Object());\n" +
						"}\n" +
						"private static void foo(Object o) {\n" +
						" switch (o) {\n" +
						"	case nu    -> System.out.println(\"Integer:\" + i);\n" +
						" 	}\n" +
						"}\n" +
						"}\n"
				);
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "case nu";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("null[KEYWORD]{null, null, null, null, null, "+keyword_Rel+"}",
				requestor.getResults());

	}
	public void test017() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"public class X {\n" +
						"public static void main(String[] args) {\n" +
						"foo(Integer.valueOf(5));\n" +
						"foo(new Object());\n" +
						"}\n" +
						"private static void foo(Object o) {\n" +
						" switch (o) {\n" +
						"	case de    -> System.out.println(\"Integer:\" + i);\n" +
						" 	}\n" +
						"}\n" +
						"}\n"
				);
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "case de";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("default[KEYWORD]{default, null, null, default, null, "+keyword_Rel+"}",
				requestor.getResults());

	}
	public void test018() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"public class X {\n" +
						"public static void main(String[] args) {\n" +
						"foo(Integer.valueOf(5));\n" +
						"foo(new Object());\n" +
						"}\n" +
						"private static void foo(Object o) {\n" +
						" switch (o) {\n" +
						"	case Integer i    -> System.out.println(\"Integer:\"  +i);\n" +
						"	case de    -> System.out.println(\"Integer:\" + i);\n" +
						" 	}\n" +
						"}\n" +
						"}\n"
				);
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "case de";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("default[KEYWORD]{default, null, null, default, null, "+keyword_Rel+"}",
				requestor.getResults());

	}
	public void test019() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"public class X {\n" +
						"public static void main(String[] args) {\n" +
						"foo(Integer.valueOf(5));\n" +
						"foo(new Object());\n" +
						"}\n" +
						"private static void foo(Object o) {\n" +
						" switch (o) {\n" +
						"	case Integer i    -> System.out.println(\"Integer:\"  +i);\n" +
						"	case n    -> System.out.println(\"Integer:\" + i);\n" +
						" 	}\n" +
						"}\n" +
						"}\n"
				);
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "case n";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("null[KEYWORD]{null, null, null, null, null, "+keyword_Rel+"}",
				requestor.getResults());

	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1100
	// ContentAssist / CompletionScanner running into deadlock
	public void testGH1100() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"import java.lang.StackWalker.Option;\n" +
				"public class X {\n" +
				"	private void test() {\n" +
				"		Option opt = Option.RETAIN_CLASS_REFERENCE;\n" +
				"		boolean testswitch (opt) { // <- remove pipe and press CTRL+Space\n" +
				"			case RETAIN_CLASS_REFERENCE -> {\n" +
				"			}\n" +
				"			case SHOW_HIDDEN_FRAMES -> {\n" +
				"			}\n" +
				"			case SHOW_REFLECT_FRAMES -> throw new UnsupportedOperationException(\"Unimplemented case: \");\n" +
				"		}\n" +
				"	}\n" +
				"}\n"
				);
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "test";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("",
				requestor.getResults());

	}
}
