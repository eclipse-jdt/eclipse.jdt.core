/*******************************************************************************
 * Copyright (c) 2024, 2025 GK Software SE and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.core.tests.model;

import java.util.function.Predicate;
import junit.framework.Test;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

public class CompletionTests23 extends CompletionTests10 {

static {
//		TESTS_NAMES = new String[] {"test012"};
}

private static int DEFAULT_RELEVANCE = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED;
private static int keyword_Rel= R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED;

private IJavaProject completion23Project;

public CompletionTests23(String name) {
	super(name);
}

@Override
public void setUpSuite() throws Exception {
	this.completion23Project = setUpJavaProject("Completion23", "23", false);
	super.setUpSuite();
}
@Override
public void tearDownSuite() throws Exception {
	deleteProject(this.completion23Project);
	super.tearDownSuite();
}
public static Test suite() {
	return buildModelTestSuite(CompletionTests23.class);
}

// === positive tests for module imports have moved to CompletionTests25 ===

public void testKeyword_neg() throws JavaModelException {
	// keyword not enabled, other proposals don't match
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/Completion23/src/p/X.java", """
			package p;
			import mod
			public class X {}
			""");
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "import mod";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults(
			"",
			requestor.getResults());
}

public void test001_neg() throws JavaModelException {
	// prefixed
	// only java.base available (from JCL_23_LIB)
	// module imports not enabled
	// other proposals don't match prefix
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion23/src/p/X.java",
			"""
			package p;
			import module java.b
			public class X {}
			""");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "java.b";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults(
			"",
			requestor.getResults());
}


public void testMarkdownTypeLink1() throws CoreException {
	createFolder("/Completion23/src/javadoc");
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("Completion23/src/javadoc/Test.java", """
			package javadoc;
			///
			/// see [Te]
			///
			public class Test {}
			""");
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "[Te";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults(
			"TemplateRuntime[TYPE_REF]{java.lang.runtime.TemplateRuntime, java.lang.runtime, Ljava.lang.runtime.TemplateRuntime;, null, null, "+(DEFAULT_RELEVANCE + R_JAVA_LIBRARY)+"}\n" +
			"Test[TYPE_REF]{Test, javadoc, Ljavadoc.Test;, null, null, "+(DEFAULT_RELEVANCE + R_UNQUALIFIED)+"}",
			requestor.getResults());
}

public void testMarkdownMethodLink1() throws CoreException {
	createFolder("/Completion23/src/javadoc");
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("Completion23/src/javadoc/Test.java", """
			package javadoc;
			///
			/// see [#me] for details
			public class Test {
				void method() {}
			}
			""");
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "[#me";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults(
			"method[METHOD_REF]{method(), Ljavadoc.Test;, ()V, method, null, "+(DEFAULT_RELEVANCE + R_NON_STATIC)+"}",
			requestor.getResults());
}

public void testMarkdownMethodLink2_qualified() throws CoreException {
	// type-prefixed
	// missing ']' at end of line
	createFolder("/Completion23/src/javadoc");
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("Completion23/src/javadoc/Test.java", """
			package javadoc;
			///
			/// see [String#le
			///
			public class Test {
				void method() {}
			}
			""");
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "#le";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults(
			"length[METHOD_REF]{length(), Ljava.lang.String;, ()I, length, null, "+(DEFAULT_RELEVANCE + R_NON_STATIC)+"}",
			requestor.getResults());
}

public void testMarkdownMethodLink2_qualified2() throws CoreException {
	// type-prefixed
	// missing ']' at end of line
	// link is on the last comment line
	createFolder("/Completion23/src/javadoc");
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("Completion23/src/javadoc/Test.java", """
			package javadoc;
			///
			/// see [String#le
			public class Test {
				void method() {}
			}
			""");
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "#le";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults(
			"length[METHOD_REF]{length(), Ljava.lang.String;, ()I, length, null, "+(DEFAULT_RELEVANCE + R_NON_STATIC)+"}",
			requestor.getResults());
}

public void test011() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion23/src/X.java",
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
			+ "null[KEYWORD]{null, null, null, null, null, "+keyword_Rel+"}\n" +
			"X[TYPE_REF]{X, , LX;, null, null, 72}\n" +
			"Object[TYPE_REF]{Object, java.lang, Ljava.lang.Object;, null, null, 84}",
			requestor.getResults());



}
public void test012() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion23/src/X.java",
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
	assertResults("null[KEYWORD]{null, null, null, null, null, "+keyword_Rel+"}\n" +
			"NullPointerException[TYPE_REF]{NullPointerException, java.lang, Ljava.lang.NullPointerException;, null, null, 64}\n" +
			"Number[TYPE_REF]{Number, java.lang, Ljava.lang.Number;, null, null, 64}",
			requestor.getResults());

}
public void test013() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion23/src/X.java",
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
	assertResults("default[KEYWORD]{default, null, null, default, null, "+keyword_Rel+"}\n" +
			"Deprecated[TYPE_REF]{Deprecated, java.lang, Ljava.lang.Deprecated;, null, null, 64}",
			requestor.getResults());

}
public void test014() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion23/src/X.java",
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
	assertResults("NullPointerException[TYPE_REF]{NullPointerException, java.lang, Ljava.lang.NullPointerException;, null, null, 64}\n" +
			"Number[TYPE_REF]{Number, java.lang, Ljava.lang.Number;, null, null, 64}",
			requestor.getResults());

}
public void test015() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion23/src/X.java",
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
	assertResults("Deprecated[TYPE_REF]{Deprecated, java.lang, Ljava.lang.Deprecated;, null, null, 64}",
			requestor.getResults());

}
public void test016() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion23/src/X.java",
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
	assertResults("null[KEYWORD]{null, null, null, null, null, "+keyword_Rel+"}\n" +
			"NullPointerException[TYPE_REF]{NullPointerException, java.lang, Ljava.lang.NullPointerException;, null, null, 64}\n" +
			"Number[TYPE_REF]{Number, java.lang, Ljava.lang.Number;, null, null, 64}",
			requestor.getResults());

}
public void test017() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion23/src/X.java",
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
	assertResults("default[KEYWORD]{default, null, null, default, null, "+keyword_Rel+"}\n" +
				"Deprecated[TYPE_REF]{Deprecated, java.lang, Ljava.lang.Deprecated;, null, null, 64}",
			requestor.getResults());

}
public void test018() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion23/src/X.java",
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
	assertResults("default[KEYWORD]{default, null, null, default, null, "+keyword_Rel+"}\n" +
				  "Deprecated[TYPE_REF]{Deprecated, java.lang, Ljava.lang.Deprecated;, null, null, 64}",
			requestor.getResults());

}
public void test019() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion23/src/X.java",
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
	assertResults("null[KEYWORD]{null, null, null, null, null, "+keyword_Rel+"}\n" +
			"NoClassDefFoundError[TYPE_REF]{NoClassDefFoundError, java.lang, Ljava.lang.NoClassDefFoundError;, null, null, 64}\n" +
			"NoSuchFieldError[TYPE_REF]{NoSuchFieldError, java.lang, Ljava.lang.NoSuchFieldError;, null, null, 64}\n" +
			"NoSuchMethodException[TYPE_REF]{NoSuchMethodException, java.lang, Ljava.lang.NoSuchMethodException;, null, null, 64}\n" +
			"NullPointerException[TYPE_REF]{NullPointerException, java.lang, Ljava.lang.NullPointerException;, null, null, 64}\n" +
			"Number[TYPE_REF]{Number, java.lang, Ljava.lang.Number;, null, null, 64}",
			requestor.getResults());

}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/3300
// Suboptimal/No code completion suggestions for switching on sealed interfaces
public void testIssue3300() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/Completion23/src/Foo.java",
			"""
			public sealed interface Foo {
				record FooImpl_a(String x) implements Foo {
				}

				record FooImpl_b(String y, String z) implements Foo {
				}

				private static Foo getFoo() {
					return new Foo.FooImpl_b("a", "b");
				}

				public static void main(String[] args) {
					Foo foo = getFoo();
					switch (foo) {
						case Foo  // offers no completion here.
					}
				}
			}
				""");
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "case Foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults("Foo.FooImpl_a[TYPE_REF]{FooImpl_a, , LFoo$FooImpl_a;, null, null, null, null, [300, 303], 72}\n" +
	              "Foo.FooImpl_b[TYPE_REF]{FooImpl_b, , LFoo$FooImpl_b;, null, null, null, null, [300, 303], 72}\n" +
			      "Foo[TYPE_REF]{Foo, , LFoo;, null, null, null, null, [300, 303], 86}", requestor.getResults());
}
public void testIssue3862_1() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/Completion23/src/X.java",
			"""
			public class X {
				public static void main(String[] args) {
		String builder = new String();
	    // the auto completion popup is filled
	    builder.toString().toString(/*1*/).toString(/*2*/).toString(/*3*/).toString();

	    // the auto completion popup is not filled in line 2, 4 ("builder" line == 1)
	    builder //
	            /*4*/.toString().toString().toString() //
	            /*5*/.toString().toString().toString() //
	            /*6*/.toString().toString().toString() //
	            /*7*/.toString().toString().toString(/*8*/).toS;
				}
			}
				""");
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "/*1*/).toS";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	String range = (cursorLocation - 3) + ", " + (cursorLocation + 5);
	assertResults("toString[METHOD_REF]{toString, Ljava.lang.Object;, ()Ljava.lang.String;, null, null, toString, null, [" + range +"], 60}", requestor.getResults());

	requestor = new CompletionTestsRequestor2(true, true, true, false);
	str = this.workingCopies[0].getSource();
	completeBehind = "/*2*/).toS";
	cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	range = (cursorLocation - 3) + ", " + (cursorLocation + 5);
	assertResults("toString[METHOD_REF]{toString, Ljava.lang.Object;, ()Ljava.lang.String;, null, null, toString, null, [" + range +"], 60}", requestor.getResults());

	requestor = new CompletionTestsRequestor2(true, true, true, false);
	str = this.workingCopies[0].getSource();
	completeBehind = "/*3*/).toS";
	cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	range = (cursorLocation - 3) + ", " + (cursorLocation + 8);
	assertResults("toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, null, null, toString, null, [" + range +"], 60}", requestor.getResults());

	requestor = new CompletionTestsRequestor2(true, true, true, false);
	str = this.workingCopies[0].getSource();
	completeBehind = "/*4*/.toS";
	cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	range = (cursorLocation - 3) + ", " + (cursorLocation + 5);
	assertResults("toString[METHOD_REF]{toString, Ljava.lang.Object;, ()Ljava.lang.String;, null, null, toString, null, [" + range +"], 60}", requestor.getResults());

	requestor = new CompletionTestsRequestor2(true, true, true, false);
	str = this.workingCopies[0].getSource();
	completeBehind = "/*5*/.toS";
	cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	range = (cursorLocation - 3) + ", " + (cursorLocation + 5);
	assertResults("toString[METHOD_REF]{toString, Ljava.lang.Object;, ()Ljava.lang.String;, null, null, toString, null, [" + range +"], 60}", requestor.getResults());

	requestor = new CompletionTestsRequestor2(true, true, true, false);
	str = this.workingCopies[0].getSource();
	completeBehind = "/*6*/.toS";
	cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	range = (cursorLocation - 3) + ", " + (cursorLocation + 5);
	assertResults("toString[METHOD_REF]{toString, Ljava.lang.Object;, ()Ljava.lang.String;, null, null, toString, null, [" + range +"], 60}", requestor.getResults());

	requestor = new CompletionTestsRequestor2(true, true, true, false);
	str = this.workingCopies[0].getSource();
	completeBehind = "/*7*/.toS";
	cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	range = (cursorLocation - 3) + ", " + (cursorLocation + 5);
	assertResults("toString[METHOD_REF]{toString, Ljava.lang.Object;, ()Ljava.lang.String;, null, null, toString, null, [" + range +"], 60}", requestor.getResults());

	requestor = new CompletionTestsRequestor2(true, true, true, false);
	str = this.workingCopies[0].getSource();
	completeBehind = "/*8*/).toS";
	cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	range = (cursorLocation - 3) + ", " + (cursorLocation);
	assertResults("toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, null, null, toString, null, [" + range +"], 60}", requestor.getResults());
}
public void testIssue3862_2() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/Completion23/src/X.java",
			"""
			public class X {
				public static void main(String[] args) {
		String builder = new String();
	    /// the auto completion popup is filled
	    builder.toString().toString(/*1*/).toString(/*2*/).toString(/*3*/).toString();

	    // the auto completion popup is not filled in line 2, 4 ("builder" line == 1)
	    builder ///
	            /*4*/.toString().toString().toString() ///
	            /*5*/.toString().toString().toString() ///
	            /*6*/.toString().toString().toString() ///
	            /*7*/.toString().toString().toString(/*8*/).toS;
				}
			}
				""");
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "/*1*/).toS";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	String range = (cursorLocation - 3) + ", " + (cursorLocation + 5);
	assertResults("toString[METHOD_REF]{toString, Ljava.lang.Object;, ()Ljava.lang.String;, null, null, toString, null, [" + range +"], 60}", requestor.getResults());

	requestor = new CompletionTestsRequestor2(true, true, true, false);
	str = this.workingCopies[0].getSource();
	completeBehind = "/*2*/).toS";
	cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	range = (cursorLocation - 3) + ", " + (cursorLocation + 5);
	assertResults("toString[METHOD_REF]{toString, Ljava.lang.Object;, ()Ljava.lang.String;, null, null, toString, null, [" + range +"], 60}", requestor.getResults());

	requestor = new CompletionTestsRequestor2(true, true, true, false);
	str = this.workingCopies[0].getSource();
	completeBehind = "/*3*/).toS";
	cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	range = (cursorLocation - 3) + ", " + (cursorLocation + 8);
	assertResults("toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, null, null, toString, null, [" + range +"], 60}", requestor.getResults());

	requestor = new CompletionTestsRequestor2(true, true, true, false);
	str = this.workingCopies[0].getSource();
	completeBehind = "/*4*/.toS";
	cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	range = (cursorLocation - 3) + ", " + (cursorLocation + 5);
	assertResults("toString[METHOD_REF]{toString, Ljava.lang.Object;, ()Ljava.lang.String;, null, null, toString, null, [" + range +"], 60}", requestor.getResults());

	requestor = new CompletionTestsRequestor2(true, true, true, false);
	str = this.workingCopies[0].getSource();
	completeBehind = "/*5*/.toS";
	cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	range = (cursorLocation - 3) + ", " + (cursorLocation + 5);
	assertResults("toString[METHOD_REF]{toString, Ljava.lang.Object;, ()Ljava.lang.String;, null, null, toString, null, [" + range +"], 60}", requestor.getResults());

	requestor = new CompletionTestsRequestor2(true, true, true, false);
	str = this.workingCopies[0].getSource();
	completeBehind = "/*6*/.toS";
	cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	range = (cursorLocation - 3) + ", " + (cursorLocation + 5);
	assertResults("toString[METHOD_REF]{toString, Ljava.lang.Object;, ()Ljava.lang.String;, null, null, toString, null, [" + range +"], 60}", requestor.getResults());

	requestor = new CompletionTestsRequestor2(true, true, true, false);
	str = this.workingCopies[0].getSource();
	completeBehind = "/*7*/.toS";
	cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	range = (cursorLocation - 3) + ", " + (cursorLocation + 5);
	assertResults("toString[METHOD_REF]{toString, Ljava.lang.Object;, ()Ljava.lang.String;, null, null, toString, null, [" + range +"], 60}", requestor.getResults());

	requestor = new CompletionTestsRequestor2(true, true, true, false);
	str = this.workingCopies[0].getSource();
	completeBehind = "/*8*/).toS";
	cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	range = (cursorLocation - 3) + ", " + (cursorLocation);
	assertResults("toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, null, null, toString, null, [" + range +"], 60}", requestor.getResults());
}
public void testIssue3862_3() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/Completion23/src/X.java",
			"""
			public class X {
				public static void main(String[] args) {
		String builder = new String();
	    /// the auto completion popup is filled
	    builder.toString().toString(/*1*/).toString(/*2*/).toString(/*3*/).toString();

	    // the auto completion popup is not filled in line 2, 4 ("builder" line == 1)
	    builder ///
	            /*4*/.toString().toString().toString() //
	            /*5*/.toString().toString().toString() ///
	            /*6*/.toString().toString().toString() //
	            /*7*/.toString().toString().toString() ///
	            /*8*/.toString().toString().toString(/*9*/).toS;
				}
			}
				""");
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "/*1*/).toS";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	String range = (cursorLocation - 3) + ", " + (cursorLocation + 5);
	assertResults("toString[METHOD_REF]{toString, Ljava.lang.Object;, ()Ljava.lang.String;, null, null, toString, null, [" + range +"], 60}", requestor.getResults());

	requestor = new CompletionTestsRequestor2(true, true, true, false);
	str = this.workingCopies[0].getSource();
	completeBehind = "/*2*/).toS";
	cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	range = (cursorLocation - 3) + ", " + (cursorLocation + 5);
	assertResults("toString[METHOD_REF]{toString, Ljava.lang.Object;, ()Ljava.lang.String;, null, null, toString, null, [" + range +"], 60}", requestor.getResults());

	requestor = new CompletionTestsRequestor2(true, true, true, false);
	str = this.workingCopies[0].getSource();
	completeBehind = "/*3*/).toS";
	cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	range = (cursorLocation - 3) + ", " + (cursorLocation + 8);
	assertResults("toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, null, null, toString, null, [" + range +"], 60}", requestor.getResults());

	requestor = new CompletionTestsRequestor2(true, true, true, false);
	str = this.workingCopies[0].getSource();
	completeBehind = "/*4*/.toS";
	cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	range = (cursorLocation - 3) + ", " + (cursorLocation + 5);
	assertResults("toString[METHOD_REF]{toString, Ljava.lang.Object;, ()Ljava.lang.String;, null, null, toString, null, [" + range +"], 60}", requestor.getResults());

	requestor = new CompletionTestsRequestor2(true, true, true, false);
	str = this.workingCopies[0].getSource();
	completeBehind = "/*5*/.toS";
	cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	range = (cursorLocation - 3) + ", " + (cursorLocation + 5);
	assertResults("toString[METHOD_REF]{toString, Ljava.lang.Object;, ()Ljava.lang.String;, null, null, toString, null, [" + range +"], 60}", requestor.getResults());

	requestor = new CompletionTestsRequestor2(true, true, true, false);
	str = this.workingCopies[0].getSource();
	completeBehind = "/*6*/.toS";
	cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	range = (cursorLocation - 3) + ", " + (cursorLocation + 5);
	assertResults("toString[METHOD_REF]{toString, Ljava.lang.Object;, ()Ljava.lang.String;, null, null, toString, null, [" + range +"], 60}", requestor.getResults());

	requestor = new CompletionTestsRequestor2(true, true, true, false);
	str = this.workingCopies[0].getSource();
	completeBehind = "/*7*/.toS";
	cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	range = (cursorLocation - 3) + ", " + (cursorLocation + 5);
	assertResults("toString[METHOD_REF]{toString, Ljava.lang.Object;, ()Ljava.lang.String;, null, null, toString, null, [" + range +"], 60}", requestor.getResults());

	requestor = new CompletionTestsRequestor2(true, true, true, false);
	str = this.workingCopies[0].getSource();
	completeBehind = "/*8*/.toS";
	cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	range = (cursorLocation - 3) + ", " + (cursorLocation + 5);
	assertResults("toString[METHOD_REF]{toString, Ljava.lang.Object;, ()Ljava.lang.String;, null, null, toString, null, [" + range +"], 60}", requestor.getResults());

	requestor = new CompletionTestsRequestor2(true, true, true, false);
	str = this.workingCopies[0].getSource();
	completeBehind = "/*9*/).toS";
	cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	range = (cursorLocation - 3) + ", " + (cursorLocation);
	assertResults("toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, null, null, toString, null, [" + range +"], 60}", requestor.getResults());
}
public void testIssue4649_5() throws JavaModelException {
	Predicate<CompletionProposal> javaTypeRef = (p) -> {
		char[] sig = p.getCompletion();
		if (sig == null) {
			return false;
		}
		String signature = new String(sig);
		return (p.getKind() == CompletionProposal.FIELD_REF) &&
				(signature.equals("foo") || signature.equals("bar") || signature.equals("foobar"));
	};
	CompletionResult result = complete(
			"/Completion23/src/Test.java",
			"""
			import java.util.Map;
			class Test {
				static Map<String, String> foo = null;
				static Map<String, String>  bar = null;
				void test2() {
					Name name = new Name(foo,);
				}
				record Name(Map<String, Object>... names) {}
			}
			""",
			"new Name(foo,", javaTypeRef);
	assertResults("bar[FIELD_REF]{bar, LTest;, Ljava.util.Map<Ljava.lang.String;Ljava.lang.String;>;, bar, null, 52}\n"
			+ "foo[FIELD_REF]{foo, LTest;, Ljava.util.Map<Ljava.lang.String;Ljava.lang.String;>;, foo, null, 52}",
			result.proposals);
}
}
