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


import junit.framework.Test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;

public class ReconcilerStatementsRecoveryTests extends ModifyingResourceTests {

	protected ICompilationUnit workingCopy;
	protected ProblemRequestor problemRequestor;

	/* A problem requestor that auto-cancels on first problem */
	class CancelingProblemRequestor extends ProblemRequestor {
		IProgressMonitor progressMonitor = new IProgressMonitor() {
			boolean isCanceled = false;
			public void beginTask(String name, int totalWork) {}
			public void done() {}
			public void internalWorked(double work) {}
			public boolean isCanceled() {
				return this.isCanceled;
			}
			public void setCanceled(boolean value) {
				this.isCanceled = value;
			}
			public void setTaskName(String name) {}
			public void subTask(String name) {}
			public void worked(int work) {}
		};

		boolean isCanceling = false;
		public void acceptProblem(IProblem problem) {
			if (this.isCanceling) this.progressMonitor.setCanceled(true); // auto-cancel on first problem
			super.acceptProblem(problem);
		}
	}

/**
 */
public ReconcilerStatementsRecoveryTests(String name) {
	super(name);
}
// Use this static initializer to specify subset for tests
// All specified tests which do not belong to the class are skipped...
static {
//	JavaModelManager.VERBOSE = true;
//	org.eclipse.jdt.internal.core.search.BasicSearchEngine.VERBOSE = true;
//	TESTS_PREFIX = "testIgnoreIfBetterNonAccessibleRule";
//	TESTS_NAMES = new String[] { "testExternal" };
//	TESTS_NUMBERS = new int[] { 118823 };
//	TESTS_RANGE = new int[] { 16, -1 };
}
public static Test suite() {
	return buildModelTestSuite(ReconcilerStatementsRecoveryTests.class);
}
protected void assertProblems(String message, String expected) {
	assertProblems(message, expected, this.problemRequestor);
}
// Expect no error as soon as indexing is finished
protected void assertNoProblem(char[] source, ICompilationUnit unit) throws InterruptedException, JavaModelException {
	IndexManager indexManager = JavaModelManager.getIndexManager();
	if (this.problemRequestor.problemCount > 0) {
		// If errors then wait for indexes to finish
		while (indexManager.awaitingJobsCount() > 0) {
			Thread.sleep(100);
		}
		// Reconcile again to see if error goes away
		this.problemRequestor.initialize(source);
		unit.getBuffer().setContents(source); // need to set contents again to be sure that following reconcile will be really done
		unit.reconcile(AST.JLS3,
			true, // force problem detection to see errors if any
			null,	// do not use working copy owner to not use working copies in name lookup
			null);
		if (this.problemRequestor.problemCount > 0) {
			assertEquals("Working copy should NOT have any problem!", "", this.problemRequestor.problems.toString());
		}
	}
}
protected void addClasspathEntries(IClasspathEntry[] entries, boolean enableForbiddenReferences) throws JavaModelException {
	IJavaProject project = getJavaProject("Reconciler");
	IClasspathEntry[] oldClasspath = project.getRawClasspath();
	int oldLength = oldClasspath.length;
	int length = entries.length;
	IClasspathEntry[] newClasspath = new IClasspathEntry[oldLength+length];
	System.arraycopy(oldClasspath, 0, newClasspath, 0, oldLength);
	System.arraycopy(entries, 0, newClasspath, oldLength, length);
	project.setRawClasspath(newClasspath, null);

	if (enableForbiddenReferences) {
		project.setOption(JavaCore.COMPILER_PB_FORBIDDEN_REFERENCE, JavaCore.ERROR);
	}
}
protected void removeClasspathEntries(IClasspathEntry[] entries) throws JavaModelException {
	IJavaProject project = getJavaProject("Reconciler");
	IClasspathEntry[] oldClasspath = project.getRawClasspath();
	int oldLength = oldClasspath.length;
	int length = entries.length;
	IClasspathEntry[] newClasspath = new IClasspathEntry[oldLength-length];
	System.arraycopy(oldClasspath, 0, newClasspath, 0, oldLength-length);
	project.setRawClasspath(newClasspath, null);
}
/**
 * Setup for the next test.
 */
public void setUp() throws Exception {
	super.setUp();
	this.problemRequestor =  new ProblemRequestor();
	this.workingCopy = getCompilationUnit("Reconciler/src/p1/X.java").getWorkingCopy(newWorkingCopyOwner(this.problemRequestor), null);
	this.problemRequestor.initialize(this.workingCopy.getSource().toCharArray());
	startDeltas();
}
public void setUpSuite() throws Exception {
	super.setUpSuite();

	// Create project with 1.4 compliance
	IJavaProject project14 = createJavaProject("Reconciler", new String[] {"src"}, new String[] {"JCL_LIB"}, "bin");
	createFolder("/Reconciler/src/p1");
	createFolder("/Reconciler/src/p2");
	createFile(
		"/Reconciler/src/p1/X.java",
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}"
	);
	project14.setOption(JavaCore.COMPILER_PB_UNUSED_LOCAL, JavaCore.IGNORE);
	project14.setOption(JavaCore.COMPILER_PB_INVALID_JAVADOC, JavaCore.WARNING);

	// Create project with 1.5 compliance
	IJavaProject project15 = createJavaProject("Reconciler15", new String[] {"src"}, new String[] {"JCL15_LIB"}, "bin", "1.5");
	addLibrary(
		project15,
		"lib15.jar",
		"lib15src.zip",
		new String[] {
			"java/util/List.java",
			"package java.util;\n" +
			"public class List<T> {\n" +
			"}",
			"java/util/Stack.java",
			"package java.util;\n" +
			"public class Stack<T> {\n" +
			"}",
			"java/util/Map.java",
			"package java.util;\n" +
			"public interface Map<K,V> {\n" +
			"}",
			"java/lang/annotation/Annotation.java",
			"package java.lang.annotation;\n" +
			"public interface Annotation {\n" +
			"}",
			"java/lang/Deprecated.java",
			"package java.lang;\n" +
			"public @interface Deprecated {\n" +
			"}",
			"java/lang/SuppressWarnings.java",
			"package java.lang;\n" +
			"public @interface SuppressWarnings {\n" +
			"   String[] value();\n" +
			"}"
		},
		JavaCore.VERSION_1_5
	);
	project15.setOption(JavaCore.COMPILER_PB_UNUSED_LOCAL, JavaCore.IGNORE);
}
//private void setUp15WorkingCopy() throws JavaModelException {
//	setUp15WorkingCopy("Reconciler15/src/p1/X.java", new WorkingCopyOwner() {});
//}
//private void setUp15WorkingCopy(String path, WorkingCopyOwner owner) throws JavaModelException {
//	String contents = this.workingCopy.getSource();
//	setUpWorkingCopy(path, contents, owner);
//}
//private void setUpWorkingCopy(String path, String contents) throws JavaModelException {
//	setUpWorkingCopy(path, contents, new WorkingCopyOwner() {});
//}
//private void setUpWorkingCopy(String path, String contents, WorkingCopyOwner owner) throws JavaModelException {
//	this.workingCopy.discardWorkingCopy();
//	this.workingCopy = getCompilationUnit(path).getWorkingCopy(owner, this.problemRequestor, null);
//	setWorkingCopyContents(contents);
//	this.workingCopy.makeConsistent(null);
//}
void setWorkingCopyContents(String contents) throws JavaModelException {
	this.workingCopy.getBuffer().setContents(contents);
	this.problemRequestor.initialize(contents.toCharArray());
}
/**
 * Cleanup after the previous test.
 */
public void tearDown() throws Exception {
	TestCompilationParticipant.PARTICIPANT = null;
	if (this.workingCopy != null) {
		this.workingCopy.discardWorkingCopy();
	}
	stopDeltas();
	super.tearDown();
}
public void tearDownSuite() throws Exception {
	deleteProject("Reconciler");
	deleteProject("Reconciler15");
	super.tearDownSuite();
}
/*
 * No ast and no statements recovery
 */
public void testStatementsRecovery01() throws CoreException {
	// Introduce syntax error
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void foo() {\n" +
		"     UnknownType name\n" +
		"  }\n" +
		"}");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, false, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta after syntax error",
		"[Working copy] X.java[*]: {CONTENT | FINE GRAINED}"
	);
	assertProblems(
		"Unexpected problems",
		"----------\n" +
		"1. ERROR in /Reconciler/src/p1/X.java (at line 5)\n" +
		"	UnknownType name\n" +
		"	            ^^^^\n" +
		"Syntax error, insert \";\" to complete BlockStatements\n" +
		"----------\n"
	);

	clearDeltas();
}
/*
 * Ast and no statements recovery
 */
public void testStatementsRecovery02() throws CoreException {
	// Introduce syntax error
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void foo() {\n" +
		"     UnknownType name\n" +
		"  }\n" +
		"}");
	this.workingCopy.reconcile(AST.JLS3, false, false, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta after syntax error",
		"[Working copy] X.java[*]: {CONTENT | FINE GRAINED | AST AFFECTED}"
	);
	assertProblems(
		"Unexpected problems",
		"----------\n" +
		"1. ERROR in /Reconciler/src/p1/X.java (at line 5)\n" +
		"	UnknownType name\n" +
		"	            ^^^^\n" +
		"Syntax error, insert \";\" to complete BlockStatements\n" +
		"----------\n"
	);

	clearDeltas();
}
/*
 * No ast, statements recovery
 */
public void testStatementsRecovery03() throws CoreException {
	// Introduce syntax error
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void foo() {\n" +
		"     UnknownType name\n" +
		"  }\n" +
		"}");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, true, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta after syntax error",
		"[Working copy] X.java[*]: {CONTENT | FINE GRAINED}"
	);
	assertProblems(
		"Unexpected problems",
		"----------\n" +
		"1. ERROR in /Reconciler/src/p1/X.java (at line 5)\n" +
		"	UnknownType name\n" +
		"	^^^^^^^^^^^\n" +
		"UnknownType cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in /Reconciler/src/p1/X.java (at line 5)\n" +
		"	UnknownType name\n" +
		"	            ^^^^\n" +
		"Syntax error, insert \";\" to complete BlockStatements\n" +
		"----------\n"
	);

	clearDeltas();
}
/*
 * Ast, statements recovery
 */
public void testStatementsRecovery04() throws CoreException {
	// Introduce syntax error
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void foo() {\n" +
		"     UnknownType name\n" +
		"  }\n" +
		"}");
	this.workingCopy.reconcile(AST.JLS3, false, true, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta after syntax error",
		"[Working copy] X.java[*]: {CONTENT | FINE GRAINED | AST AFFECTED}"
	);
	assertProblems(
		"Unexpected problems",
		"----------\n" +
		"1. ERROR in /Reconciler/src/p1/X.java (at line 5)\n" +
		"	UnknownType name\n" +
		"	^^^^^^^^^^^\n" +
		"UnknownType cannot be resolved to a type\n" +
		"----------\n" +
		"2. ERROR in /Reconciler/src/p1/X.java (at line 5)\n" +
		"	UnknownType name\n" +
		"	            ^^^^\n" +
		"Syntax error, insert \";\" to complete BlockStatements\n" +
		"----------\n"
	);

	clearDeltas();
}
}
