/*******************************************************************************
 * Copyright (c) 2025 GK Software SE and others.
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
import org.eclipse.jdt.core.JavaModelException;

public class CompletionTests25 extends AbstractJavaModelCompletionTests {

static {
//		TESTS_NAMES = new String[] {"test012"};
}

private static int DEFAULT_RELEVANCE = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED;

private IJavaProject completion25Project;

public CompletionTests25(String name) {
	super(name);
}

@Override
public void setUpSuite() throws Exception {
	this.completion25Project = setUpJavaProject("Completion25", "25", false);
	super.setUpSuite();
}
@Override
public void tearDownSuite() throws Exception {
	deleteProject(this.completion25Project);
	super.tearDownSuite();
}
public static Test suite() {
	return buildModelTestSuite(CompletionTests25.class);
}

//=== negative tests for module imports remain in CompletionTests23 ===

public void testKeyword() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion25/src/p/X.java",
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

public void test001() throws JavaModelException {
	// prefixed
	// only java.base available (from JCL_25_LIB)
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion25/src/p/X.java",
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

public void test002() throws JavaModelException {
	// no prefix
	// only java.base available (from JCL_25_LIB)
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion25/src/p/X.java",
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
	IPath jarPath = this.completion25Project.getPath().append("mod.one.jar");
	try {
		addClasspathEntry(this.completion25Project, newModularLibraryEntry(jarPath, null, null));
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Completion25/src/p/X.java", """
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
		removeClasspathEntry(this.completion25Project, jarPath);
	}
}

public void test004() throws JavaModelException {
	// with prefix
	// 3 modules on the module path: mod.two, mod.one & java.base
	// prefix selects 2 out of 3
	IPath jarOnePath = this.completion25Project.getPath().append("mod.one.jar");
	IPath jarTwoPath = this.completion25Project.getPath().append("mod.two.jar");
	try {
		addClasspathEntry(this.completion25Project, newModularLibraryEntry(jarOnePath, null, null));
		addClasspathEntry(this.completion25Project, newModularLibraryEntry(jarTwoPath, null, null));
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Completion25/src/p/X.java", """
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
		removeClasspathEntry(this.completion25Project, jarOnePath);
		removeClasspathEntry(this.completion25Project, jarTwoPath);
	}
}

public void test005() throws CoreException {
	// with prefix
	// 4 modules available: mod.test (self), mod.two (required), mod.one (transitively required) & java.base (from JCL_25_LIB)
	// prefix selects 3 out of 4
	IPath jarOnePath = this.completion25Project.getPath().append("mod.one.jar");
	IPath jarTwoPath = this.completion25Project.getPath().append("mod.two.jar");
	IFile moduleFile = null;
	try {
		addClasspathEntry(this.completion25Project, newModularLibraryEntry(jarOnePath, null, null));
		addClasspathEntry(this.completion25Project, newModularLibraryEntry(jarTwoPath, null, null));
		moduleFile = createFile("Completion25/src/module-info.java",
				"""
				module mod.test {
					requires mod.two; // mod.two requires transitive mod.one
				}
				""");
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Completion25/src/p/X.java", """
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
		removeClasspathEntry(this.completion25Project, jarOnePath);
		removeClasspathEntry(this.completion25Project, jarTwoPath);
		if (moduleFile != null)
			deleteResource(moduleFile);
	}
}
public void test006() throws CoreException {
	// with prefix
	// 4 modules present: mod.test(self), mod.one, mod.two & java.base available
	// + prefix rules out java.base
	// + mod.two is proposed with lower relevance, because it is not read by the current module
	IPath jarOnePath = this.completion25Project.getPath().append("mod.one.jar");
	IPath jarTwoPath = this.completion25Project.getPath().append("mod.two.jar");
	try {
		addClasspathEntry(this.completion25Project, newModularLibraryEntry(jarOnePath, null, null));
		addClasspathEntry(this.completion25Project, newModularLibraryEntry(jarTwoPath, null, null)); // not read my the current module
		createFile("Completion25/src/module-info.java",
				"""
				module mod.test {
					requires mod.one;
				}
				""");
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Completion25/src/p/X.java", """
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
		removeClasspathEntry(this.completion25Project, jarOnePath);
		removeClasspathEntry(this.completion25Project, jarTwoPath);
	}
}
}
