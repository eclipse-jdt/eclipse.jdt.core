/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * Contributors:
 *		IBM Corporation - initial API and implementation
 *		Stephan Herrmann - Copy-adjusted structure from ReconcilerTests, filled with new content
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;


import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.tests.util.Util;

import junit.framework.Test;

public class ReconcilerTests9 extends ModifyingResourceTests {

	protected ICompilationUnit workingCopy;
	protected ProblemRequestor problemRequestor;

	/*package*/ static final int JLS_LATEST = AST.getJLSLatest();

/**
 */
public ReconcilerTests9(String name) {
	super(name);
}
// Use this static initializer to specify subset for tests
// All specified tests which do not belong to the class are skipped...
static {
//	JavaModelManager.VERBOSE = true;
//	TESTS_PREFIX = "testAnnotations";
//	TESTS_NAMES = new String[] { "testBug564289_001" };
//	TESTS_NUMBERS = new int[] { 118823 };
//	TESTS_RANGE = new int[] { 16, -1 };
}
public static Test suite() {
	return buildModelTestSuite(ReconcilerTests9.class);
}
protected void assertProblems(String message, String expected) {
	assertProblems(message, expected, this.problemRequestor);
}
/**
 * Setup for the next test.
 */
@Override
public void setUp() throws Exception {
	super.setUp();
	this.problemRequestor =  new ProblemRequestor();
	this.wcOwner = new WorkingCopyOwner() {
		public IProblemRequestor getProblemRequestor(ICompilationUnit unit) {
			return ReconcilerTests9.this.problemRequestor;
		}
	};
	this.workingCopy = getCompilationUnit("Reconciler9/src/module-info.java").getWorkingCopy(this.wcOwner, null);
	this.problemRequestor.initialize(this.workingCopy.getSource().toCharArray());
	startDeltas();
}
@Override
public void setUpSuite() throws Exception {
	super.setUpSuite();

	// Create project with 9 compliance
	IJavaProject project9 = createJava9Project("Reconciler9");
	createFile(
		"/Reconciler9/src/module-info.java",
		"/**\n" +
		"  * @category before" +
		"  */\n" +
		"@Deprecated\n" +
		"module mod.one {\n" +
		"}"
	);
	project9.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_9);
	project9.setOption(JavaCore.COMPILER_PB_UNUSED_LOCAL, JavaCore.IGNORE);
	project9.setOption(JavaCore.COMPILER_PB_INVALID_JAVADOC, JavaCore.WARNING);


}
protected void setUpWorkingCopy(String path, String contents) throws JavaModelException {
	setUpWorkingCopy(path, contents, this.wcOwner);
}
private void setUpWorkingCopy(String path, String contents, WorkingCopyOwner owner) throws JavaModelException {
	this.workingCopy.discardWorkingCopy();
	this.workingCopy = getCompilationUnit(path).getWorkingCopy(owner, null);
	assertEquals("Invalid problem requestor!", this.problemRequestor, this.wcOwner.getProblemRequestor(this.workingCopy));
	setWorkingCopyContents(contents);
	this.workingCopy.makeConsistent(null);
}
void setWorkingCopyContents(String contents) throws JavaModelException {
	this.workingCopy.getBuffer().setContents(contents);
	this.problemRequestor.initialize(contents.toCharArray());
}
/**
 * Cleanup after the previous test.
 */
@Override
public void tearDown() throws Exception {
	TestCompilationParticipant.PARTICIPANT = null;
	if (this.workingCopy != null) {
		this.workingCopy.discardWorkingCopy();
	}
	stopDeltas();
	super.tearDown();
}
@Override
public void tearDownSuite() throws Exception {
	deleteProject("Reconciler9");
	super.tearDownSuite();
}
private String deprecatedForRemoval(String element) {
	if (isJRE9)
		return element + " has been deprecated and marked for removal\n";
	else
		return element + " is deprecated\n";
}
/*
 * Ensures that the delta is correct when adding an annotation
 */
public void testAnnotations1() throws JavaModelException {
	setWorkingCopyContents(
		"/**\n" +
		"  * @category before\n" +
		"  * @category after\n" +
		"  */\n" +
		"@Deprecated\n" +
		"@MyAnnot\n" +
		"module mod.one {\n" +
		"}"
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta",
		"mod.one[*]: {CATEGORIES | ANNOTATIONS}\n" +
		"	@MyAnnot[+]: {}"
	);
}
/*
 * Ensures that the delta is correct when removing an annotation
 */
public void testAnnotations2deprecated() throws JavaModelException {
	setWorkingCopyContents(
		"/**\n" +
		"  * @category before\n" +
		"  */\n" +
		"module mod.one {\n" +
		"}"
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta",
		"mod.one[*]: {MODIFIERS CHANGED | ANNOTATIONS}"
	);
}
/*
 * Ensures that the delta is correct when removing an annotation
 */
public void testAnnotations2() throws JavaModelException {
	setWorkingCopyContents(
		"/**\n" +
		"  * @category before\n" +
		"  */\n" +
		"  @MyAnnot(x=1)\n" +
		"module mod.one {\n" +
		"}"
	);
	this.workingCopy.makeConsistent(null);	setWorkingCopyContents(
		"/**\n" +
		"  * @category before\n" +
		"  */\n" +
		"module mod.one {\n" +
		"}"
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta",
		"mod.one[*]: {ANNOTATIONS}\n" +
		"	@MyAnnot[-]: {}"
	);
}
/*
 * Ensures that the delta is correct when changing an annotation
 */
public void testAnnotations3() throws JavaModelException {
	setWorkingCopyContents(
		"/**\n" +
		"  * @category before\n" +
		"  */\n" +
		"  @MyAnnot(x=1)\n" +
		"module mod.one {\n" +
		"}"
	);
	this.workingCopy.makeConsistent(null);

	setWorkingCopyContents(
		"/**\n" +
		"  * @category before\n" +
		"  * @category after\n" +
		"  */\n" +
		"  @MyAnnot(y=1)\n" +
		"module mod.one {\n" +
		"}"
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta",
		"mod.one[*]: {CATEGORIES | ANNOTATIONS}\n" +
		"	@MyAnnot[*]: {CONTENT}"
	);
}
/**
 * Ensures that the delta is correct when adding a category
 */
public void testCategories1() throws JavaModelException {
	setWorkingCopyContents(
		"/**\n" +
		"  * @category before\n" +
		"  * @category after\n" +
		"  */\n" +
		"@Deprecated\n" +
		"module mod.one {\n" +
		"}"
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta",
		"mod.one[*]: {CATEGORIES}"
	);
}
/**
 * Ensures that the delta is correct when removing a category
 */
public void testCategories2() throws JavaModelException {
	setWorkingCopyContents(
		"/**\n" +
		"  */\n" +
		"@Deprecated\n" +
		"module mod.one {\n" +
		"}"
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta",
		"mod.one[*]: {CATEGORIES}"
	);
}
/**
 * Ensures that the delta is correct when changing a category
 */
public void testCategories3() throws JavaModelException {
	setWorkingCopyContents(
		"/**\n" +
		"  * @category never\n" +
		"  */\n" +
		"@Deprecated\n" +
		"module mod.one {\n" +
		"}"
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta",
		"mod.one[*]: {CATEGORIES}"
	);
}
public void testTerminalDeprecation1() throws CoreException {
	try {
		createJava9Project("P1");
		createFolder("/P1/src/p");
		createFile("/P1/src/p/X1.java",
				"package p;\n" +
				"@Deprecated(forRemoval=true)\n" +
				"public class X1 {}");
		createFile("/P1/src/p/X2.java",
				"package p;\n" +
				"public class X2 {\n" +
				"   @Deprecated(forRemoval=true)\n" +
				"	public Object field;\n" +
				"   @Deprecated(forRemoval=true)\n" +
				"	public void m() {}\n" +
				"}\n");

		setUpWorkingCopy("/P1/src/Y.java",
				"public class Y extends p.X1 {\n" +
				"	Object foo(p.X2 x2) {\n" +
				"		x2.m();\n" +
				"		return x2.field;\n" +
				"	}\n" +
				"}\n");
		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"1. WARNING in /P1/src/Y.java (at line 1)\n" +
			"	public class Y extends p.X1 {\n" +
			"	                         ^^\n" +
			"The type X1 has been deprecated and marked for removal\n" +
			"----------\n" +
			"2. WARNING in /P1/src/Y.java (at line 3)\n" +
			"	x2.m();\n" +
			"	   ^^^\n" +
			"The method m() from the type X2 has been deprecated and marked for removal\n" +
			"----------\n" +
			"3. WARNING in /P1/src/Y.java (at line 4)\n" +
			"	return x2.field;\n" +
			"	          ^^^^^\n" +
			"The field X2.field has been deprecated and marked for removal\n" +
			"----------\n"
		);
	} finally {
		deleteProject("P1");
	}
}
public void testTerminalDeprecation2() throws CoreException, IOException {
	try {
		IJavaProject p1 = createJava9Project("P1");
		String x1Source = "package p;\n" +
				"@Deprecated(forRemoval=true)\n" +
				"public class X1 {}";
		String x2Source = "package p;\n" +
			"public class X2 {\n" +
			"   @Deprecated(forRemoval=true)\n" +
			"	public Object field;\n" +
			"   @Deprecated(forRemoval=true)\n" +
			"	public void m() {}\n" +
			"	@Deprecated public void m2() {}\n" +
			"}\n";
		String[] allJarSources = (isJRE9)
				? new String[] {
					"p/X1.java",
					x1Source,
					"/P1/src/p/X2.java",
					x2Source }
				: new String[] {
					"java/lang/Deprecated.java",
					"package java.lang;\n" +
					"public @interface Deprecated {\n" +
					"	boolean forRemoval() default false;" +
					"}\n",
					"p/X1.java",
					x1Source,
					"/P1/src/p/X2.java",
					x2Source };
		createJar(
			allJarSources,
			p1.getProject().getLocation().append("lib.jar").toOSString(),
			null,
			"9");
		p1.getProject().refreshLocal(2, null);
		addLibraryEntry(p1, "/P1/lib.jar", false);

		setUpWorkingCopy("/P1/src/Y.java",
				"public class Y extends p.X1 {\n" +
				"	Object foo(p.X2 x2) {\n" +
				"		x2.m();\n" +
				"		x2.m2();\n" +
				"		return x2.field;\n" +
				"	}\n" +
				"}\n");
		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"1. WARNING in /P1/src/Y.java (at line 1)\n" +
			"	public class Y extends p.X1 {\n" +
			"	                         ^^\n" +
			deprecatedForRemoval("The type X1") +
			"----------\n" +
			"2. WARNING in /P1/src/Y.java (at line 3)\n" +
			"	x2.m();\n" +
			"	   ^^^\n" +
			deprecatedForRemoval("The method m() from the type X2") +
			"----------\n" +
			"3. WARNING in /P1/src/Y.java (at line 4)\n" +
			"	x2.m2();\n" +
			"	   ^^^^\n" +
			"The method m2() from the type X2 is deprecated\n" +
			"----------\n" +
			"4. WARNING in /P1/src/Y.java (at line 5)\n" +
			"	return x2.field;\n" +
			"	          ^^^^^\n" +
			deprecatedForRemoval("The field X2.field") +
			"----------\n");
	} finally {
		deleteProject("P1");
	}
}
public void testBug540541() throws CoreException, IOException {
	if (!isJRE9) return;
	IJavaProject project1 = null;
	IJavaProject project2 = null;
	Hashtable<String, String> options = JavaCore.getOptions();
	try {
		project1 = createJava9Project("java.base", "9");
		createFile("/java.base/src/module-info.java",
					"module java.base {\n" +
					"	exports java.lang;\n" +
					"}");
		createFolder("/java.base/src/java/lang");
		createFile("/java.base/src/java/lang/Object.java",
					"package java.lang;\n" +
					"public class Object {\n" +
					"}\n");

		project1.setRawClasspath(new IClasspathEntry[] {JavaCore.newSourceEntry(new Path("/java.base/src"))}, null);
		project1.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
		IMarker[] markers = project1.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
		assertMarkers("Unexpected markers on java.base", "", markers);

		project2 = createJava9Project("client", "9");
		IClasspathAttribute[] attributes = new IClasspathAttribute[] { JavaCore.newClasspathAttribute("module", "true") };
		IClasspathEntry projectEntry = JavaCore.newProjectEntry(project1.getPath(), null, false, attributes, false);
		project2.setRawClasspath(new IClasspathEntry[] {projectEntry, JavaCore.newSourceEntry(new Path("/client/src"))}, null);
		createFolder("/client/src/p");
		createFile("/client/src/p/X.java",
					"package p;\n" +
					"public class X {\n" +
					"}\n");
		this.workingCopy = getCompilationUnit("client/src/p/X.java").getWorkingCopy(this.wcOwner, null);
		this.problemRequestor.initialize(this.workingCopy.getSource().toCharArray());
		this.workingCopy.reconcile(JLS_LATEST, true, this.wcOwner, null);
		assertProblems("Expecting no problems",
						"----------\n" +
						"----------\n",
						this.problemRequestor);

		markers = project2.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
		assertMarkers("Unexpected markers on client", "", markers);
	} finally {
		if (project1 != null)
			deleteProject(project1);
		if (project2 != null)
			deleteProject(project2);
		JavaCore.setOptions(options);
	}
}
public void testBug543092() throws Exception {
	if (!isJRE9) {
		System.err.println("Test "+getName()+" requires a JRE 9");
		return;
	}
	IJavaProject p = null;
	try {
		// ---- module log:
		//      - has log4j on the module path
		p = createJava9ProjectWithJREAttributes("p", new String[] {"src"},
				new IClasspathAttribute[] {JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true")});
		String jarAbsPath = p.getProject().getLocation()+"/lib-modular.jar";
		createJar(new String[] {
				"module-info.java",
				"module lib {\n" +
				"	exports lib.lab;\n" +
				"}\n",
				"lib/lab/Lib.java",
				"package lib.lab;\n" +
				"public class Lib {}\n"
			},
			jarAbsPath,
			null,
			"9");
		addLibraryEntry(p, new Path(jarAbsPath), false);

		String jarAbsPath2 = p.getProject().getLocation()+"/lib-nonmodular.jar";
		createJar(new String[] {
				"lib/lab/Lib.java",
				"package lib.lab;\n" +
				"public class Lib {}\n",
			},
			jarAbsPath2);
		addLibraryEntry(p, new Path(jarAbsPath2), false);

		createFolder("p/src/test");
		createFile("p/src/test/Test.java",
				"package test;\n" +
				"public class Test {\n" +
				"	lib.lab.Lib lob;\n" +
				"}\n");
		p.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
		IMarker[] markers = p.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
		assertMarkers("markers in p",
				"",
				markers);

		this.workingCopy = getCompilationUnit("p/src/test/Test.java").getWorkingCopy(this.wcOwner, null);
		this.problemRequestor.initialize(this.workingCopy.getSource().toCharArray());
		this.workingCopy.reconcile(JLS_LATEST, true, this.wcOwner, null);
		assertProblems("Expecting no problems",
						"----------\n" +
						"----------\n",
						this.problemRequestor);

	} finally {
		deleteProject(p);
	}
}
public void testBug543092b() throws Exception {
	if (!isJRE9) {
		System.err.println("Test "+getName()+" requires a JRE 9");
		return;
	}
	IJavaProject p = null;
	try {
		// ---- module log:
		//      - has log4j on the module path
		IClasspathAttribute[] moduleAttributes = new IClasspathAttribute[] {JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, "true")};
		p = createJava9ProjectWithJREAttributes("p", new String[] {"src"}, moduleAttributes);
		String jarAbsPath = p.getProject().getLocation()+"/lib-modular.jar";
		createJar(new String[] {
				"module-info.java",
				"module lib {\n" +
				"	exports lib.lab;\n" +
				"}\n",
				"lib/lab/Lib.java",
				"package lib.lab;\n" +
				"public class Lib {}\n"
			},
			jarAbsPath,
			null,
			"9");
		addLibraryEntry(p, new Path(jarAbsPath), null, null, null, null, moduleAttributes, false);

		String jarAbsPath2 = p.getProject().getLocation()+"/lib-nonmodular.jar";
		createJar(new String[] {
				"lib/lab/Lib.java",
				"package lib.lab;\n" +
				"public class Lib {}\n",
			},
			jarAbsPath2);
		addLibraryEntry(p, new Path(jarAbsPath2), false);

		createFolder("p/src/test");
		createFile("p/src/test/Test.java",
				"package test;\n" +
				"public class Test {\n" +
				"	lib.lab.Lib lob;\n" +
				"}\n");
		p.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
		IMarker[] markers = p.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
		assertMarkers("markers in p",
				"The package lib.lab is accessible from more than one module: <unnamed>, lib",
				markers);

		this.workingCopy = getCompilationUnit("p/src/test/Test.java").getWorkingCopy(this.wcOwner, null);
		this.problemRequestor.initialize(this.workingCopy.getSource().toCharArray());
		this.workingCopy.reconcile(JLS_LATEST, true, this.wcOwner, null);
		assertProblems("Expecting no problems",
						"----------\n" +
						"1. ERROR in /p/src/test/Test.java (at line 3)\n" +
						"	lib.lab.Lib lob;\n" +
						"	^^^^^^^\n" +
						"The package lib.lab is accessible from more than one module: <unnamed>, lib\n" +
						"----------\n",
						this.problemRequestor);
	} finally {
		deleteProject(p);
	}
}
public void testBug544017() throws CoreException {
	if (!isJRE9) {
		System.err.println("Test "+getName()+" requires a JRE 9");
		return;
	}
	IJavaProject testa = createJava9Project("testa");
	IJavaProject testb = createJava9Project("testb");
	IJavaProject testmain = createJava9Project("testmain");
	try {
		createFolder("testb/src/com/example/sub/b");
		createFile("testb/src/com/example/sub/b/B.java",
				"package com.example.sub.b;\n" +
				"public class B {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(\"B\");\n" +
				"	}\n" +
				"}\n");
		createFile("testb/src/module-info.java",
				"open module com.example.sub.b {\n" +
				"	exports com.example.sub.b;\n" +
				"}\n");

		addModularProjectEntry(testa, testb);
		createFolder("testa/src/com/example/sub/a");
		createFile("testa/src/com/example/sub/a/A.java",
				"package com.example.sub.a;\n" +
				"public class A {\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(\"A\");\n" +
				"	}\n" +
				"}\n");
		createFile("testa/src/module-info.java",
				"open module com.example.sub.a {\n" +
				"	exports com.example.sub.a;\n" +
				"	requires com.example.sub.b;\n" +
				"}\n");

		addModularProjectEntry(testmain, testa);
		addModularProjectEntry(testmain, testb);
		createFolder("testmain/src/com/example");
		createFile("testmain/src/module-info.java",
				"open module com.example {\n" +
				"    requires com.example.sub.a;\n" +
				"    requires com.example.sub.b;\n" +
				"}\n");
		String pathExample = "testmain/src/com/example/Example.java";
		String sourceExample =
				"package com.example;\n" +
				"import com.example.sub.a.A;\n" +
				"import com.example.sub.b.B;\n" +
				"\n" +
				"public class Example {\n" +
				"    public static void main(String[] args) {\n" +
				"    	A.main(null);\n" +
				"    	B.main(null);\n" +
				"    }\n" +
				"}\n";
		createFile(pathExample, sourceExample);

		getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
		IMarker[] markers = testmain.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
		assertMarkers("markers in testmain",
				"",
				markers);

		ICompilationUnit wc = getCompilationUnit(pathExample).getWorkingCopy(this.wcOwner, null);
		wc.getBuffer().append(" ");
		this.problemRequestor.initialize((sourceExample+" ").toCharArray());
		wc.reconcile(JLS_LATEST, true, this.wcOwner, null);
		assertProblems("Expecting no problems",
						"----------\n" +
						"----------\n",
						this.problemRequestor);
	} finally {
		deleteProject(testa);
		deleteProject(testb);
		deleteProject(testmain);
	}
}

public void testBug545687() throws CoreException, IOException {
	if (!isJRE9)
		return;
	IJavaProject p = null;
	Hashtable<String, String> options = JavaCore.getOptions();
	try {
		p = createJava9Project("testproj", "9");
		createFolder("/testproj/src/javax/xml/dummy");
		createFile("/testproj/src/javax/xml/dummy/Dummy.java", //
				"package javax.xml.dummy;\n" + //
				"public class Dummy {\n" + //
				"}\n");
		createFolder("/testproj/src/test");
		String testSrc = "package test;\n" + //
				"import javax.xml.XMLConstants;\n" + //
				"public class Test {\n" + //
				"    String s = XMLConstants.NULL_NS_URI;\n" + //
				"}\n";
		createFile("/testproj/src/test/Test.java", testSrc);
		this.workingCopy.discardWorkingCopy();
		this.problemRequestor.initialize(testSrc.toCharArray());
		this.workingCopy = getCompilationUnit("testproj/src/test/Test.java").getWorkingCopy(this.wcOwner, null);
		this.problemRequestor.initialize(this.workingCopy.getSource().toCharArray());
		this.workingCopy.reconcile(JLS_LATEST, true, this.wcOwner, null);
		assertProblems("Expecting no problems", "----------\n" + "----------\n", this.problemRequestor);

		IMarker[] markers = p.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
		assertMarkers("Unexpected markers on client", "", markers);
	} finally {
		if (p != null)
			deleteProject(p);
		JavaCore.setOptions(options);
	}
}
public void testBug546315() throws Exception {
	if (!isJRE9)
		return;
	IJavaProject p = null;
	String outputDirectory = Util.getOutputDirectory();
	try {
		String fooPath = "externalLib/foo.jar";
		Util.createJar(
				new String[] {
					"test/src/foo/Foo.java", //$NON-NLS-1$
					"package foo;\n" +
					"public class Foo {\n" +
					"	public static String get() { return \"\"; }\n" +
					"}",
				},
				null,
				new HashMap<>(),
				null,
				getExternalResourcePath(fooPath));

		String fooBarPath = "externalLib/foo.bar.jar";
		Util.createJar(
				new String[] {
					"test/src/foo/bar/FooBar.java", //$NON-NLS-1$
					"package foo.bar;\n" +
					"public class FooBar {\n" +
					"	public static String get() { return \"\"; }\n" +
					"}",
				},
				null,
				new HashMap<>(),
				null,
				getExternalResourcePath(fooBarPath));

		String fooBar2Path = "externalLib/foo.bar2.jar";
		Util.createJar(
				new String[] {
					"test/src/foo/bar2/FooBar2.java", //$NON-NLS-1$
					"package foo.bar2;\n" +
					"public class FooBar2 {\n" +
					"	public static String get() { return \"\"; }\n" +
					"}",
				},
				null,
				new HashMap<>(),
				null,
				getExternalResourcePath(fooBar2Path));

		p = createJava9Project("p", "11");
		IClasspathAttribute[] testAttrs = { JavaCore.newClasspathAttribute("test", "true") };
		addClasspathEntry(p, JavaCore.newSourceEntry(new Path("/p/src-test"), null, null, new Path("/p/bin-test"), testAttrs));
		addModularLibraryEntry(p, new Path(getExternalResourcePath(fooBarPath)), null);
		addLibraryEntry(p, new Path(getExternalResourcePath(fooPath)), null, null, null, null, testAttrs, false);
		addLibraryEntry(p, new Path(getExternalResourcePath(fooBar2Path)), null, null, null, null, testAttrs, false);

		createFolder("p/src/main");
		createFile("p/src/main/Main.java",
				"package main;\n" +
				"\n" +
				"import foo.bar.FooBar;\n" +
				"\n" +
				"public class Main {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(FooBar.get());\n" +
				"	}\n" +
				"\n" +
				"}\n");
		createFile("p/src/module-info.java",
				"module com.example.main {\n" +
				"	requires foo.bar;\n" +
				"}\n");
		String testSource =
				"package test;\n" +
				"\n" +
				"// errors shown in Java editor (but not in Problems view)\n" +
				"// can be run without dialog \"error exists...\"\n" +
				"\n" +
				"import foo.bar.FooBar;\n" +
				"import foo.bar2.FooBar2;\n" +
				"import foo.Foo; // <- The package foo is accessible from more than one module: <unnamed>, foo.bar\n" +
				"\n" +
				"public class Test {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"		System.out.println(Foo.get()); // <- Foo cannot be resolved\n" +
				"		System.out.println(FooBar.get());\n" +
				"		System.out.println(FooBar2.get());\n" +
				"	}\n" +
				"\n" +
				"}\n";
		createFolder("p/src-test/test");
		String mPath = "p/src-test/test/Test.java";
		createFile(mPath,
				testSource);
		p.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
		waitForAutoBuild();
		IMarker[] markers = p.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
		assertMarkers("Unexpected markers",
				"Name of automatic module \'foo.bar\' is unstable, it is derived from the module\'s file name.",  markers);

		this.workingCopy.discardWorkingCopy();
		this.problemRequestor.initialize(testSource.toCharArray());
		this.workingCopy = getCompilationUnit("p/src-test/test/Test.java").getWorkingCopy(this.wcOwner, null);
		this.problemRequestor.initialize(this.workingCopy.getSource().toCharArray());
		this.workingCopy.reconcile(JLS_LATEST, true, this.wcOwner, null);
		assertProblems("Expecting no problems", "----------\n" + "----------\n", this.problemRequestor);

		markers = p.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
		assertMarkers("Unexpected markers", "Name of automatic module \'foo.bar\' is unstable, it is derived from the module\'s file name.", markers);
	} finally {
		deleteExternalResource("externalLib");
		deleteProject(p);
		File outputDir = new File(outputDirectory);
		if (outputDir.exists())
			Util.flushDirectoryContent(outputDir);
	}
}
public void testBug544306() throws Exception {
	if (!isJRE9)
		return;
	IJavaProject p1 = createJava9Project("p1");
	IJavaProject p2 = createJava9Project("p2");
	try {
		createFolder("p1/src/p1");
		createFile("p1/src/p1/P1.java",
				"package p1;\n" +
				"public class P1 {\n" +
				"}\n");
		createFile("p1/src/module-info.java",
				"module p1 {\n" +
				"	exports p1;\n" +
				"}\n");

		IClasspathAttribute[] testAttrs = { JavaCore.newClasspathAttribute("test", "true") };
		addClasspathEntry(p2, JavaCore.newProjectEntry(p1.getPath(), null, false, testAttrs, false));
		addClasspathEntry(p2, JavaCore.newSourceEntry(new Path("/p2/src-test"), null, null, new Path("/p2/bin-test"), testAttrs));
		createFolder("p2/src/p2");
		createFolder("p2/src-test/p2");

		createFile("p2/src/module-info.java",
				"module p2 {\n" +
				"}\n");
		String testSource = "package p2;\n" +
		"import p1.P1;\n" +
		"class Test extends P1{ }";

		createFile("p2/src-test/p2/Test.java", testSource);
		waitForAutoBuild();
		IMarker[] markers = p1.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
		assertMarkers("Unexpected markers",
				"",  markers);

		this.workingCopy.discardWorkingCopy();
		this.workingCopy = getCompilationUnit("p2/src-test/p2/Test.java").getWorkingCopy(this.wcOwner, null);
		this.problemRequestor.initialize(testSource.toCharArray());
		this.workingCopy.reconcile(JLS_LATEST, true, this.wcOwner, null);
		assertProblems("Expecting no problems", "----------\n" + "----------\n", this.problemRequestor);
		this.workingCopy.discardWorkingCopy();
	} finally {
		deleteProject(p1);
		deleteProject(p2);
	}
}
public void testBug547113() throws CoreException {
	if (!isJRE9)
		return;
	IJavaProject unnamed = createJava9Project("unnamed");
	IJavaProject a = createJava9Project("a");
	IJavaProject b = createJava9Project("b");
	IJavaProject c = createJava9Project("c");
	try {
		createFolder("a/src/com/example/a");
		createFile("a/src/com/example/Base.java",
				"package com.example;\n" +
				"\n" +
				"public class Base {}\n");
		createFile("a/src/com/example/a/A.java",
				"package com.example.a;\n" +
				"\n" +
				"public class A {}\n");
		createFile("a/src/module-info.java",
				"open module com.example.a {\n" +
				"	exports com.example;\n" +
				"	exports com.example.a;\n" +
				"	\n" +
				"	requires unnamed;\n" +
				"}\n");
		addModularProjectEntry(a, unnamed);

		createFolder("b/src/com/example/b");
		createFile("b/src/com/example/b/B.java",
				"package com.example.b;\n" +
				"\n" +
				"import com.example.Base;\n" +
				"import com.example.a.A;\n" +
				"\n" +
				"public class B {\n" +
				"\n" +
				"	public void a(A a) {\n" +
				"		System.out.println(a);\n" +
				"	}\n" +
				"\n" +
				"	public void base(Base base) {\n" +
				"		System.out.println(base);\n" +
				"	}\n" +
				"\n" +
				"}\n");
		createFile("b/src/module-info.java",
				"open module com.example.b {\n" +
				"	exports com.example.b;\n" +
				"	requires transitive com.example.a;\n" +
				"}\n");
		addModularProjectEntry(b, a);
		addModularProjectEntry(b, unnamed);

		createFolder("c/src/com/example/c");
		String cSource = "package com.example.c;\n" +
				"\n" +
				"import com.example.Base;\n" +
				"import com.example.a.A;\n" +
				"import com.example.b.B;\n" +
				"\n" +
				"public class C {\n" +
				"\n" +
				"	public static void main(String[] args) {\n" +
				"		new B().a(new A());\n" +
				"		new B().base(new Base());\n" +
				"		System.out.println(\"done.\");\n" +
				"	}\n" +
				"}\n";
		createFile("c/src/com/example/c/C.java", cSource);
		createFile("c/src/module-info.java",
				"open module com.example.c {\n" +
				"	exports com.example.c;\n" +
				"	requires com.example.b;\n" +
				"}\n");
		addModularProjectEntry(c, a);
		addModularProjectEntry(c, b);
		addModularProjectEntry(c, unnamed);

		waitForAutoBuild();

		this.workingCopy.discardWorkingCopy();
		this.problemRequestor.initialize(cSource.toCharArray());
		this.workingCopy = getCompilationUnit("c/src/com/example/c/C.java").getWorkingCopy(this.wcOwner, null);
		this.problemRequestor.initialize(this.workingCopy.getSource().toCharArray());
		this.workingCopy.reconcile(AST_INTERNAL_LATEST, true, this.wcOwner, null);
		assertProblems("Expecting no problems",
				"----------\n" +
				"----------\n",
				this.problemRequestor);
	} finally {
		deleteProject(unnamed);
		deleteProject(a);
		deleteProject(b);
		deleteProject(c);
	}
}
public void testBug564289_001() throws Exception {
	if (!isJRE9)
		return;
	IJavaProject p = null;
	String outputDirectory = Util.getOutputDirectory();
	try {
		String lib1path = "externalLib/foo.jar";
		Util.createJar(
				new String[] {
					"net/openhft/chronicle/core/io/IORuntimeException.java",
					"package net.openhft.chronicle.core.io;\n" +
					"public class IORuntimeException extends java.lang.RuntimeException {\n" +
					"	private static final long serialVersionUID = 1L;\n" +
					"	public IORuntimeException(java.lang.String message) {}\n" +
					"}",
					"net/openhft/chronicle/bytes/ReadBytesMarshallable.java",
					"package net.openhft.chronicle.bytes;\n" +
					"public abstract interface ReadBytesMarshallable {\n" +
					"	public abstract void readMarshallable() throws net.openhft.chronicle.core.io.IORuntimeException;\n" +
					"}",
					"net/openhft/chronicle/bytes/BytesMarshallable.java",
					"package net.openhft.chronicle.bytes;\n" +
					"public abstract interface BytesMarshallable extends net.openhft.chronicle.bytes.ReadBytesMarshallable {\n" +
					"	  public default void readMarshallable() throws net.openhft.chronicle.core.io.IORuntimeException {\n" +
					"	  }\n" +
					"}",
					"java/lang/Sri420.java",
					"package java.lang;\n" +
					"public class Sri420 {\n" +
					"}",
				},
				null,
				getExternalResourcePath(lib1path),
				JavaCore.VERSION_1_8);

		p = createJava9Project("p", "11");
		addLibraryEntry(p, new Path(getExternalResourcePath(lib1path)), null, null, null, null, null, false);

		createFolder("p/src/X");
		createFile("p/src/X.java",
				"import net.openhft.chronicle.bytes.BytesMarshallable; \n" +
				"/** */\n" +
				"public class X {\n" +
				"    public static void main(String[] args) {\n" +
				"        System.out.println(\"BytesMarshallable: \" + new BytesMarshallable() {});\n" +
				"    } \n" +
				"}\n");
		p.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
		waitForAutoBuild();
		IMarker[] markers = p.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
		String expected = "Type java.lang.String is indirectly referenced from required .class files but cannot be resolved"
				+ " since the declaring package java.lang exported from module java.base conflicts with a package accessible"
				+ " from module <unnamed>";
		assertMarkers("Unexpected markers",	expected,  markers);

		this.workingCopy.discardWorkingCopy();
		this.workingCopy = getCompilationUnit("p/src/X.java").getWorkingCopy(this.wcOwner, null);
		this.problemRequestor.initialize(this.workingCopy.getSource().toCharArray());
		this.workingCopy.reconcile(JLS_LATEST, true, this.wcOwner, null);
		String expectedErrorMessage = 		"----------\n" +
				"1. ERROR in /p/src/X.java (at line 1)\n" +
				"	import net.openhft.chronicle.bytes.BytesMarshallable; \n" +
				"	^\n" +
				expected + "\n" +
				"----------\n";
		assertProblems("Expecting  problems",expectedErrorMessage, this.problemRequestor);

		markers = p.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
		assertMarkers("Unexpected markers", expected,  markers);
	} finally {
		deleteExternalResource("externalLib");
		deleteProject(p);
		File outputDir = new File(outputDirectory);
		if (outputDir.exists())
			Util.flushDirectoryContent(outputDir);
	}
}
}
