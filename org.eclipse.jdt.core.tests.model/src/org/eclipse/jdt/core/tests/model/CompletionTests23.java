/*******************************************************************************
 * Copyright (c) 2024 GK Software SE and others.
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

import junit.framework.Test;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

public class CompletionTests23 extends AbstractJavaModelCompletionTests {

static {
//		TESTS_NAMES = new String[] {"test006"};
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
	this.completion23Project.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
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

public void testKeyword() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion23/src/p/X.java",
			"""
			package p;
			import m
			public class X {}
			""");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "import m";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	int relevanceWithoutCase = DEFAULT_RELEVANCE - R_CASE;
	assertResults(
			"Map[TYPE_REF]{java.util.Map;, java.util, Ljava.util.Map;, null, null, "+relevanceWithoutCase+"}\n" +
			"MethodHandle[TYPE_REF]{java.lang.invoke.MethodHandle;, java.lang.invoke, Ljava.lang.invoke.MethodHandle;, null, null, "+relevanceWithoutCase+"}\n" +
			"MethodHandles[TYPE_REF]{java.lang.invoke.MethodHandles;, java.lang.invoke, Ljava.lang.invoke.MethodHandles;, null, null, "+relevanceWithoutCase+"}\n" +
			"MethodType[TYPE_REF]{java.lang.invoke.MethodType;, java.lang.invoke, Ljava.lang.invoke.MethodType;, null, null, "+relevanceWithoutCase+"}\n" +
			"module[KEYWORD]{module, null, null, module, null, " + DEFAULT_RELEVANCE + "}",
			requestor.getResults());
}
public void testKeyword_neg() throws JavaModelException {
	// keyword not enabled, other proposals don't match
	this.completion23Project.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
	try {
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
	} finally {
		this.completion23Project.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
	}
}

public void test001() throws JavaModelException {
	// prefixed
	// only java.base available (from JCL_23_LIB)
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion23/src/p/X.java",
			"""
			package p;
			import module java.
			public class X {}
			""");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "java.";
	int nameStart = str.lastIndexOf(completeBehind);
	int cursorLocation = nameStart + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults(
			"[MODULE_REF]{java.base, java.base, null, null, null, ["+nameStart+", "+cursorLocation+"], "+DEFAULT_RELEVANCE + "}",
			requestor.getResults());
}

public void test001_neg() throws JavaModelException {
	// prefixed
	// only java.base available (from JCL_23_LIB)
	// preview JEP 476 not enabled
	// other proposals don't match prefix
	this.completion23Project.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
	try {
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
	} finally {
		this.completion23Project.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
	}
}

public void test002() throws JavaModelException {
	// no prefix
	// only java.base available (from JCL_23_LIB)
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion23/src/p/X.java",
			"""
			package p;
			import module\s
			public class X {}
			""");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "module ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults(
			"[MODULE_REF]{java.base, java.base, null, null, null, " + DEFAULT_RELEVANCE + "}",
			requestor.getResults());
}

public void test003() throws JavaModelException {
	// no prefix
	// 2 modules available: mod.one & java.base
	// unnamed module reads them all
	IPath jarPath = this.completion23Project.getPath().append("mod.one.jar");
	try {
		addClasspathEntry(this.completion23Project, newModularLibraryEntry(jarPath, null, null));
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Completion23/src/p/X.java", """
				package p;
				import module\s
				public class X {}
				""");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "module ";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"[MODULE_REF]{java.base, java.base, null, null, null, " + DEFAULT_RELEVANCE + "}\n" +
				"[MODULE_REF]{mod.one, mod.one, null, null, null, " + DEFAULT_RELEVANCE + "}",
				requestor.getResults());
	} finally {
		removeClasspathEntry(this.completion23Project, jarPath);
	}
}

public void test004() throws JavaModelException {
	// with prefix
	// 3 modules on the module path: mod.two, mod.one & java.base
	// prefix selects 2 out of 3
	IPath jarOnePath = this.completion23Project.getPath().append("mod.one.jar");
	IPath jarTwoPath = this.completion23Project.getPath().append("mod.two.jar");
	try {
		addClasspathEntry(this.completion23Project, newModularLibraryEntry(jarOnePath, null, null));
		addClasspathEntry(this.completion23Project, newModularLibraryEntry(jarTwoPath, null, null));
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Completion23/src/p/X.java", """
				package p;
				import module mo
				public class X {}
				""");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "module mo";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"[MODULE_REF]{mod.one, mod.one, null, null, null, " + DEFAULT_RELEVANCE + "}\n" +
				"[MODULE_REF]{mod.two, mod.two, null, null, null, " + DEFAULT_RELEVANCE + "}",
				requestor.getResults());
	} finally {
		removeClasspathEntry(this.completion23Project, jarOnePath);
		removeClasspathEntry(this.completion23Project, jarTwoPath);
	}
}

public void test005() throws CoreException {
	// with prefix
	// 4 modules available: mod.test (self), mod.two (required), mod.one (transitively required) & java.base (from JCL_23_LIB)
	// prefix selects 3 out of 4
	IPath jarOnePath = this.completion23Project.getPath().append("mod.one.jar");
	IPath jarTwoPath = this.completion23Project.getPath().append("mod.two.jar");
	IFile moduleFile = null;
	try {
		addClasspathEntry(this.completion23Project, newModularLibraryEntry(jarOnePath, null, null));
		addClasspathEntry(this.completion23Project, newModularLibraryEntry(jarTwoPath, null, null));
		moduleFile = createFile("Completion23/src/module-info.java",
				"""
				module mod.test {
					requires mod.two; // mod.two requires transitive mod.one
				}
				""");
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Completion23/src/p/X.java", """
				package p;
				import module mo
				public class X {}
				""");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "module mo";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"[MODULE_REF]{mod.one, mod.one, null, null, null, " + DEFAULT_RELEVANCE + "}\n" +
				"[MODULE_REF]{mod.test, mod.test, null, null, null, " + DEFAULT_RELEVANCE + "}\n" +
				"[MODULE_REF]{mod.two, mod.two, null, null, null, " + DEFAULT_RELEVANCE + "}",
				requestor.getResults());
	} finally {
		removeClasspathEntry(this.completion23Project, jarOnePath);
		removeClasspathEntry(this.completion23Project, jarTwoPath);
		if (moduleFile != null)
			deleteResource(moduleFile);
	}
}
public void test006() throws CoreException {
	// with prefix
	// 4 modules present: mod.test(self), mod.one, mod.two & java.base available
	// + prefix rules out java.base
	// + mod.two is proposed with lower relevance, because it is not read by the current module
	IPath jarOnePath = this.completion23Project.getPath().append("mod.one.jar");
	IPath jarTwoPath = this.completion23Project.getPath().append("mod.two.jar");
	try {
		addClasspathEntry(this.completion23Project, newModularLibraryEntry(jarOnePath, null, null));
		addClasspathEntry(this.completion23Project, newModularLibraryEntry(jarTwoPath, null, null)); // not read my the current module
		createFile("Completion23/src/module-info.java",
				"""
				module mod.test {
					requires mod.one;
				}
				""");
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Completion23/src/p/X.java", """
				package p;
				import module mo
				public class X {}
				""");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "module mo";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"[MODULE_REF]{mod.two, mod.two, null, null, null, " + (DEFAULT_RELEVANCE - R_NON_RESTRICTED) + "}\n" + // lower relevance, not read
				"[MODULE_REF]{mod.one, mod.one, null, null, null, " + DEFAULT_RELEVANCE + "}\n" +
				"[MODULE_REF]{mod.test, mod.test, null, null, null, " + DEFAULT_RELEVANCE + "}",
				requestor.getResults());
	} finally {
		removeClasspathEntry(this.completion23Project, jarOnePath);
		removeClasspathEntry(this.completion23Project, jarTwoPath);
	}
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
}
