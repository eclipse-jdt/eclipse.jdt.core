/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;


import java.io.IOException;

import junit.framework.Test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.JavaModelManager;

public class ReconcilerTests extends ModifyingResourceTests {
	
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
			if (isCanceling) this.progressMonitor.setCanceled(true); // auto-cancel on first problem
			super.acceptProblem(problem);
		}		
	}
/**
 */
public ReconcilerTests(String name) {
	super(name);
}
// Use this static initializer to specify subset for tests
// All specified tests which do not belong to the class are skipped...
static {
	// Names of tests to run: can be "testBugXXXX" or "BugXXXX")
	//TESTS_NAMES = new String[] { "testAccessRestriction4" };
	// Numbers of tests to run: "test<number>" will be run for each number of this array
	//TESTS_NUMBERS = new int[] { 13 };
	// Range numbers of tests to run: all tests between "test<first>" and "test<last>" will be run for { first, last }
	//TESTS_RANGE = new int[] { 16, -1 };
}
public static Test suite() {
	return buildTestSuite(ReconcilerTests.class);
}
protected void assertProblems(String message, String expected) {
	assertProblems(message, expected, this.problemRequestor);
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
	this.workingCopy = getCompilationUnit("Reconciler/src/p1/X.java").getWorkingCopy(new WorkingCopyOwner() {}, this.problemRequestor, null);
	this.problemRequestor.initialize(this.workingCopy.getSource().toCharArray());
	startDeltas();
}
public void setUpSuite() throws Exception {
	super.setUpSuite();
	createJavaProject("Reconciler", new String[] {"src"}, new String[] {"JCL_LIB"}, "bin");
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
	IJavaProject javaProject = createJavaProject("Reconciler15", new String[] {"src"}, new String[] {"JCL_LIB"}, "bin", "1.5");
	addLibrary(
		javaProject, 
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
			"}"
		}, 
		JavaCore.VERSION_1_5
	);
}
private void setUp15WorkingCopy() throws JavaModelException {
	String contents = this.workingCopy.getSource();
	setUpWorkingCopy("Reconciler15/src/p1/X.java", contents);
}
private void setUpWorkingCopy(String path, String contents) throws JavaModelException {
	this.workingCopy.discardWorkingCopy();
	this.workingCopy = getCompilationUnit(path).getWorkingCopy(new WorkingCopyOwner() {}, this.problemRequestor, null);
	setWorkingCopyContents(contents);
	this.workingCopy.makeConsistent(null);
}
private void setWorkingCopyContents(String contents) throws JavaModelException {
	this.workingCopy.getBuffer().setContents(contents);
	this.problemRequestor.initialize(contents.toCharArray());
}
/**
 * Cleanup after the previous test.
 */
public void tearDown() throws Exception {
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
 * Ensures that no problem is created for a reference to a type that is included in a prereq project.
 */
public void testAccessRestriction() throws CoreException {
	try {
		createJavaProject("P1", new String[] {"src"}, new String[] {"JCL_LIB"}, null, null, new String[0], null, null, new boolean[0], "bin", null, new String[][] {{"**/X.java"}}, null, "1.4");
		createFolder("/P1/src/p");
		createFile("/P1/src/p/X.java", "package p; public class X {}");
		
		createJavaProject("P2", new String[] {"src"}, new String[] {"JCL_LIB"}, new String[] {"/P1"}, "bin");
		setUpWorkingCopy("/P2/src/Y.java", "public class Y extends p.X {}");
		assertProblems(
			"Unexpected problems", 
			"----------\n" + 
			"----------\n"
		);
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}
/*
 * Ensures that no problem is created for a reference to a binary type that is included in a prereq project.
 * (regression test for bug 82542 Internal error during AST creation)
 */
public void testAccessRestriction2() throws CoreException, IOException {
	try {
		IJavaProject project = createJavaProject("P1");
		addLibrary(
			project,
			"lib.jar",
			"libsrc.zip",
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"}",
			},
			new String[] {
				"**/*.class"
			},
			null,
			"1.4"
		);
		createJavaProject("P2", new String[] {"src"}, new String[] {"JCL_LIB"}, new String[] {"/P1"}, "bin");
		setUpWorkingCopy("/P2/src/Y.java", "public class Y extends p.X {}");
		assertProblems(
			"Unexpected problems", 
			"----------\n" + 
			"----------\n"
		);
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}
/*
 * Ensures that no problem is created for a reference to a type that is included and not exported in a prereq project
 * but with combineAccessRestriction flag set to false.
 */
public void testAccessRestriction3() throws CoreException {
	try {
		createJavaProject("P1");
		createFolder("/P1/p");
		createFile("/P1/p/X.java", "package p; public class X {}");
		
		createJavaProject("P2", new String[] {}, new String[] {}, null, null, new String[] {"/P1"}, null, null, new boolean[] {true}, "", null, null, null, "1.4");
		
		createJavaProject("P3", new String[] {"src"}, new String[] {"JCL_LIB"}, null, null, new String[] {"/P2"}, null, new String[][] {new String[] {"**/X.java"}}, false/*don't combine access restrictions*/, new boolean[] {true}, "bin", null, null, null, "1.4");
		setUpWorkingCopy("/P3/src/Y.java", "public class Y extends p.X {}");
		assertProblems(
			"Unexpected problems", 
			"----------\n" + 
			"----------\n"
		);
	} finally {
		deleteProjects(new String[] {"P1", "P2", "P3" });
	}
}
/*
 * Ensures that a problem is created for a reference to a type that is included and not exported in a prereq project
 * but with combineAccessRestriction flag set to true.
 */
public void testAccessRestriction4() throws CoreException {
	try {
		createJavaProject("P1");
		createFolder("/P1/p");
		createFile("/P1/p/X.java", "package p; public class X {}");
		
		createJavaProject("P2", new String[] {}, new String[] {}, null, null, new String[] {"/P1"}, null, null, new boolean[] {true}, "", null, null, null, "1.4");
		
		createJavaProject("P3", new String[] {"src"}, new String[] {"JCL_LIB"}, null, null, new String[] {"/P2"}, null, new String[][] {new String[] {"**/X.java"}}, true/*combine access restrictions*/, new boolean[] {true}, "bin", null, null, null, "1.4");
		setUpWorkingCopy("/P3/src/Y.java", "public class Y extends p.X {}");
		assertProblems(
			"Unexpected problems", 
			"----------\n" + 
			"1. WARNING in /P3/src/Y.java (at line 1)\n" + 
			"	public class Y extends p.X {}\n" + 
			"	                       ^^^\n" + 
			"Access restriction: The type X is not accessible due to restriction on required project P1\n" + 
			"----------\n"
		);
	} finally {
		deleteProjects(new String[] {"P1", "P2", "P3" });
	}
}
/**
 * Ensures that the reconciler handles duplicate members correctly.
 */
public void testAddDuplicateMember() throws JavaModelException {
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected delta", 
		"X[*]: {CHILDREN | FINE GRAINED}\n" + 
		"	foo()#2[+]: {}"
	);
}
/**
 * Ensures that the reconciler reconciles the new contents with the current
 * contents, updating the structure of this reconciler's compilation
 * unit, and fires the Java element delta for the structural changes
 * of the addition of a field and a constructor.
 */
public void testAddFieldAndConstructor() throws JavaModelException {
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  int i;\n" +
		"  X(int i) {\n" +
		"    this.i = i;\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected delta", 
		"X[*]: {CHILDREN | FINE GRAINED}\n" + 
		"	i[+]: {}\n" + 
		"	X(int)[+]: {}"
	);
}
/**
 * Ensures that the reconciler reconciles the new contents with the current
 * contents, updating the structure of this reconciler's compilation
 * unit, and fires the Java element delta for the structural changes
 * of the addition of a field and a constructor.
 */
public void testAddImports() throws JavaModelException {
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"import java.lang.reflect.*;\n" +
		"import java.util.Vector;\n" +
		"public class X {\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected delta", 
		"<import container>[*]: {CHILDREN | FINE GRAINED}\n" +
		"	import java.lang.reflect.*[+]: {}\n" +
		"	import java.util.Vector[+]: {}"
	);
}
/**
 * Ensures that the reconciler reconciles the new contents with the current
 * contents, updating the structure of this reconciler's compilation
 * unit, and fires the Java element delta for the structural changes
 * of the addition of a method.
 */
public void testAddMethod1() throws JavaModelException {
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"  public void bar() {\n" +
		"  }\n" +
		"}");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected delta", 
		"X[*]: {CHILDREN | FINE GRAINED}\n" +
		"	bar()[+]: {}"
	);
}
/**
 * Ensures that the reconciler reconciles the new contents with the current
 * contents,updating the structure of this reconciler's compilation
 * unit, and fires the Java element delta for the structural changes
 * of the addition of a portion of a new method.
 */
public void testAddPartialMethod1() throws JavaModelException {
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void some()\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected delta", 
		"X[*]: {CHILDREN | FINE GRAINED}\n" + 
		"	some()[+]: {}"
	);
}
/**
 * Ensures that the reconciler reconciles the new contents with the current
 * contents,updating the structure of this reconciler's compilation
 * unit, and fires the Java element delta for the structural changes
 * of the addition of a portion of a new method.  Ensures that when a
 * second part is added to the new method no structural changes are recognized.
 */
public void testAddPartialMethod1and2() throws JavaModelException {
	// Add partial method before foo
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void some()\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	
	// Add { on partial method
	clearDeltas();
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void some() {\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected delta", 
		""
	);
}
/*
 * Ensures that reconciling a subclass doesn't close the buffer while resolving its superclass.
 * (regression test for bug 62854 refactoring does not trigger reconcile)
 */
public void testBufferOpenAfterReconcile() throws CoreException {
 	try {
		createFile(
			"/Reconciler/src/p1/Super.java",
			"package p1;\n" +
			"public class Super {\n" +
			"}"
		);
		setWorkingCopyContents(
			"package p1;\n" +
			"import p2.*;\n" +
			"public class X extends Super {\n" +
			"  public void foo() {\n" +
			"  }\n" +
			"}");
		IBuffer buffer = this.workingCopy.getBuffer();
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, true, null, null);
		assertTrue("Buffer should still be open", !buffer.isClosed());
	} finally {
		deleteFile("/Reconciler/src/p1/Super.java");
	}
}
/**
 * Ensure an OperationCanceledException is correcly thrown when progress monitor is canceled
 * @deprecated using deprecated code
 */
public void testCancel() throws JavaModelException {
	setWorkingCopyContents(
		"package p1;\n" +
		"public class X {\n" +
		"  void foo(String s) {\n" +
		"  }\n" +
		"}"
	);
	this.workingCopy.makeConsistent(null);
	
	// count the number of time isCanceled() is called when converting this source unit
	CancelCounter counter = new CancelCounter();
	this.workingCopy.reconcile(AST.JLS2, true, null, counter);
	
	// throw an OperatonCanceledException at each point isCanceled() is called
	for (int i = 0; i < counter.count; i++) {
		boolean gotException = false;
		try {
			this.workingCopy.reconcile(AST.JLS2, true, null, new Canceler(i));
		} catch (OperationCanceledException e) {
			gotException = true;
		}
		assertTrue("Should get an OperationCanceledException (" + i + ")", gotException);
	}
	
	// last should not throw an OperationCanceledException
	this.workingCopy.reconcile(AST.JLS2, true, null, new Canceler(counter.count));
}
/**
 * Ensures that the reconciler reconciles the new contents with the current
 * contents,updating the structure of this reconciler's compilation
 * unit, and fires the Java element deltas for the structural changes
 * of a method's type parameter change.
 */
public void testChangeMethodTypeParameters() throws JavaModelException {
	setUp15WorkingCopy();
	clearDeltas();
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public <T> void foo() {\n" +
		"  }\n" +
		"}");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected delta", 
		"X[*]: {CHILDREN | FINE GRAINED}\n" +
		"	foo()[*]: {CONTENT}"
	);
}
/**
 * Ensures that the reconciler reconciles the new contents with the current
 * contents,updating the structure of this reconciler's compilation
 * unit, and fires the Java element deltas for the structural changes
 * of a type's type parameter change.
 */
public void testChangeTypeTypeParameters() throws JavaModelException {
	setUp15WorkingCopy();
	clearDeltas();
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X <T> {\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected delta", 
		"X[*]: {CONTENT}"
	);
}
/**
 * Ensures that the reconciler reconciles the new contents with the current
 * contents,updating the structure of this reconciler's compilation
 * unit, and fires the Java element deltas for the structural changes
 * of a method visibility change.
 */
public void testChangeMethodVisibility() throws JavaModelException {
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  private void foo() {\n" +
		"  }\n" +
		"}");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected delta", 
		"X[*]: {CHILDREN | FINE GRAINED}\n" +
		"	foo()[*]: {MODIFIERS CHANGED}"
	);
}
/**
 * Ensures that the correct delta is reported when closing the working copy and modifying its buffer.
 */
public void testCloseWorkingCopy() throws JavaModelException {
	IBuffer buffer = this.workingCopy.getBuffer();
	this.workingCopy.close();
	buffer.setContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"  public void bar() {\n" +
		"  }\n" +
		"}");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected delta", 
		"X[*]: {CHILDREN | FINE GRAINED}\n" +
		"	bar()[+]: {}"
	);
}

/**
 * Ensures that a reference to a constant with type mismatch doesn't show an error.
 * (regression test for bug 17104 Compiler does not complain but "Quick Fix" ??? complains)
 */
public void testConstantReference() throws CoreException {
	try {
		createFile(
			"/Reconciler/src/p1/OS.java",
			"package p1;\n" +
			"public class OS {\n" +
			"	public static final int CONST = 23 * 1024;\n" +
			"}");
		setWorkingCopyContents(
			"package p1;\n" +
			"public class X {\n" +
			"	public short c;\n" +
			"	public static void main(String[] arguments) {\n" +
			"		short c = 1;\n" +
			"		switch (c) {\n" +
			"			case OS.CONST: return;\n" +
			"		}\n" +
			"	}\n" +
			"}");
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
		assertProblems(
			"Unexpected problems",
			"----------\n" + 
			"----------\n"
		);
	} finally {
		deleteFile("/Reconciler/src/p1/OS.java");
	}
}
/**
 * Ensures that the reconciler reconciles the new contents with the current
 * contents, updating the structure of this reconciler's compilation
 * unit, and fires the Java element deltas for the structural changes
 * of a method being deleted.
 */
public void testDeleteMethod1() throws JavaModelException {
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"}");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected delta", 
		"X[*]: {CHILDREN | FINE GRAINED}\n" +
		"	foo()[-]: {}"
	);
}
/**
 * Ensures that the reconciler reconciles the new contents with the current
 * contents, updating the structure of this reconciler's compilation
 * unit, and fires the Java element deltas for the structural changes
 * of two methods being deleted.
 */
public void testDeleteTwoMethods() throws JavaModelException {
	// create 2 methods
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"  public void bar() {\n" +
		"  }\n" +
		"}");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	
	// delete the 2 methods
	clearDeltas();
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"}");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected delta", 
		"X[*]: {CHILDREN | FINE GRAINED}\n" +
		"	bar()[-]: {}\n" +
		"	foo()[-]: {}"
	);
}
/*
 * Ensures that excluded part of prereq project are not visible
 */
public void testExcludePartOfAnotherProject1() throws CoreException {
	IClasspathEntry[] newEntries = createClasspath("Reconciler", new String[] {"/P", "**/internal/"}, false/*no inclusion*/, true/*has exclusion*/);
	try {
		addClasspathEntries(newEntries, true);
		createJavaProject("P");
		createFolder("/P/p/internal");
		createFile(
			"/P/p/internal/Y.java",
			"package p.internal;\n" +
			"public class Y {\n" +
			"}"
		);
		setWorkingCopyContents(
			"package p1;\n" +
			"public class X extends p.internal.Y {\n" +
			"}"
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
		assertProblems(
			"Unexpected problems",
			"----------\n" + 
			"1. ERROR in /Reconciler/src/p1/X.java (at line 2)\n" + 
			"	public class X extends p.internal.Y {\n" + 
			"	                       ^^^^^^^^^^^^\n" + 
			"Access restriction: The type Y is not accessible due to restriction on required project P\n" + 
			"----------\n"
		);
	} finally {
		removeClasspathEntries(newEntries);
		deleteProject("P");
	}
}
/*
 * Ensures that packages that are not in excluded part of prereq project are visible
 */
public void testExcludePartOfAnotherProject2() throws CoreException {
	IClasspathEntry[] newEntries = createClasspath("Reconciler", new String[] {"/P", "**/internal/"}, false/*no inclusion*/, true/*has exclusion*/);
	try {
		addClasspathEntries(newEntries, true);
		createJavaProject("P");
		createFolder("/P/p/api");
		createFile(
			"/P/p/api/Y.java",
			"package p.api;\n" +
			"public class Y {\n" +
			"}"
		);
		setWorkingCopyContents(
			"package p1;\n" +
			"public class X extends p.api.Y {\n" +
			"}"
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
		assertProblems(
			"Unexpected problems",
			"----------\n" + 
			"----------\n"
		);
	} finally {
		removeClasspathEntries(newEntries);
		deleteProject("P");
	}
}
/*
 * Ensures that included part of prereq project are visible
 */
public void testIncludePartOfAnotherProject1() throws CoreException {
	IClasspathEntry[] newEntries = createClasspath("Reconciler", new String[] {"/P", "**/api/"}, true/*has inclusion*/, false/*no exclusion*/);
	try {
		addClasspathEntries(newEntries, true);
		createJavaProject("P");
		createFolder("/P/p/api");
		createFile(
			"/P/p/api/Y.java",
			"package p.api;\n" +
			"public class Y {\n" +
			"}"
		);
		setWorkingCopyContents(
			"package p1;\n" +
			"public class X extends p.api.Y {\n" +
			"}"
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
		assertProblems(
			"Unexpected problems",
			"----------\n" + 
			"----------\n"
		);
	} finally {
		removeClasspathEntries(newEntries);
		deleteProject("P");
	}
}
/*
 * Ensures that packages that are not in included part of prereq project are not visible
 */
public void testIncludePartOfAnotherProject2() throws CoreException {
	IClasspathEntry[] newEntries = createClasspath("Reconciler", new String[] {"/P", "**/api/"}, true/*has inclusion*/, false/*no exclusion*/);
	try {
		addClasspathEntries(newEntries, true);
		createJavaProject("P");
		createFolder("/P/p/internal");
		createFile(
			"/P/p/internal/Y.java",
			"package p.internal;\n" +
			"public class Y {\n" +
			"}"
		);
		setWorkingCopyContents(
			"package p1;\n" +
			"public class X extends p.internal.Y {\n" +
			"}"
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
		assertProblems(
			"Unexpected problems",
			"----------\n" + 
			"1. ERROR in /Reconciler/src/p1/X.java (at line 2)\n" + 
			"	public class X extends p.internal.Y {\n" + 
			"	                       ^^^^^^^^^^^^\n" + 
			"Access restriction: The type Y is not accessible due to restriction on required project P\n" + 
			"----------\n"
		);
	} finally {
		removeClasspathEntries(newEntries);
		deleteProject("P");
	}
}
/**
 * Start with no imports, add an import, and then append to the import name.
 */
public void testGrowImports() throws JavaModelException {
	// no imports
	setWorkingCopyContents(
		"package p1;\n" +
		"public class X {\n" +
		"}");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	
	// add an import
	clearDeltas();
	setWorkingCopyContents(
		"package p1;\n" +
		"import p\n" +
		"public class X {\n" +
		"}");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected delta", 
		"<import container>[+]: {}"
	);
		
	// append to import name
	clearDeltas();
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2\n" +
		"public class X {\n" +
		"}");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected delta", 
		"<import container>[*]: {CHILDREN | FINE GRAINED}\n" +
		"	import p2[+]: {}\n" +
		"	import p[-]: {}"
	);
}
/**
 * Introduces a syntax error in the modifiers of a method.
 */
public void testMethodWithError01() throws CoreException {
	// Introduce syntax error
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public.void foo() {\n" +
		"  }\n" +
		"}");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected delta after syntax error", 
		"X[*]: {CHILDREN | FINE GRAINED}\n" +
		"	foo()[*]: {MODIFIERS CHANGED}"
	);
	assertProblems(
		"Unexpected problems",
		"----------\n" + 
		"1. ERROR in /Reconciler/src/p1/X.java (at line 4)\n" + 
		"	public.void foo() {\n" + 
		"	      ^\n" + 
		"Syntax error on token \".\", delete this token\n" + 
		"----------\n"
	);

	// Fix the syntax error
	clearDeltas();
	String contents =
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}";
	setWorkingCopyContents(contents);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected delta after fixing syntax error", 
		"X[*]: {CHILDREN | FINE GRAINED}\n" +
		"	foo()[*]: {MODIFIERS CHANGED}"
	);
	assertProblems(
		"Unexpected problems",
		"----------\n" + 
		"1. WARNING in /Reconciler/src/p1/X.java (at line 2)\n" + 
		"	import p2.*;\n" + 
		"	       ^^\n" + 
		"The import p2 is never used\n" + 
		"----------\n"
	);
}
/**
 * Test reconcile force flag
 */
public void testMethodWithError02() throws CoreException {
	String contents =
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public.void foo() {\n" +
		"  }\n" +
		"}";		
	setWorkingCopyContents(contents);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);

	// use force flag to refresh problems			
	this.problemRequestor.initialize(contents.toCharArray());
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, true, null, null);
	assertProblems(
		"Unexpected problems",
		"----------\n" + 
		"1. ERROR in /Reconciler/src/p1/X.java (at line 4)\n" + 
		"	public.void foo() {\n" + 
		"	      ^\n" + 
		"Syntax error on token \".\", delete this token\n" + 
		"----------\n"
	);
}

/**
 * Test reconcile force flag off
 */
public void testMethodWithError03() throws CoreException {
	String contents =
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public.void foo() {\n" +
		"  }\n" +
		"}";
	setWorkingCopyContents(contents);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);

	// reconcile with force flag turned off
	this.problemRequestor.initialize(contents.toCharArray());
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertProblems(
		"Unexpected problems",
		""
	);
}
/**
 * Test reconcile force flag + cancel
 */
public void testMethodWithError04() throws CoreException {

	CancelingProblemRequestor myPbRequestor = new CancelingProblemRequestor();
	
	this.workingCopy.discardWorkingCopy();
	ICompilationUnit x = getCompilationUnit("Reconciler", "src", "p1", "X.java");
	this.problemRequestor = myPbRequestor;
	this.workingCopy = x.getWorkingCopy(new WorkingCopyOwner() {}, this.problemRequestor, null);

	String contents =
		"package p1;\n" +
		"public class X {\n" +
		"	Zork f;	\n"+
		"	void foo(Zork z){\n"+
		"	}\n"+
		"}	\n";
	setWorkingCopyContents(contents);

	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);

	// use force flag to refresh problems			
	myPbRequestor.isCanceling = true;
	myPbRequestor.initialize(contents.toCharArray());
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, true, null, myPbRequestor.progressMonitor);
	assertProblems(
		"Unexpected problems",
		"----------\n" + 
		"1. ERROR in /Reconciler/src/p1/X.java (at line 3)\n" + 
		"	Zork f;	\n" + 
		"	^^^^\n" + 
		"Zork cannot be resolved to a type\n" + 
		"----------\n"
	);
}

/**
 * Test reconcile force flag off
 */
public void testMethodWithError05() throws CoreException {
	try {
		createFolder("/Reconciler/src/tests");
		String contents =
			"package tests;	\n"+
			"abstract class AbstractSearchableSource extends AbstractSource implements SearchableSource {	\n"+
			"	abstract int indexOfImpl(long value);	\n"+
			"	public final int indexOf(long value) {	\n"+
			"		return indexOfImpl(value);	\n"+
			"	}	\n"+
			"}	\n";
		createFile(
			"/Reconciler/src/tests/AbstractSearchableSource.java", 
			contents);
	
		createFile(
			"/Reconciler/src/tests/Source.java", 
			"package tests;	\n"+
			"interface Source {	\n"+
			"	long getValue(int index);	\n"+
			"	int size();	\n"+
			"}	\n");
	
		createFile(
			"/Reconciler/src/tests/AbstractSource.java", 
			"package tests;	\n"+
			"abstract class AbstractSource implements Source {	\n"+
			"	AbstractSource() {	\n"+
			"	}	\n"+
			"	void invalidate() {	\n"+
			"	}	\n"+
			"	abstract long getValueImpl(int index);	\n"+
			"	abstract int sizeImpl();	\n"+
			"	public final long getValue(int index) {	\n"+
			"		return 0;	\n"+
			"	}	\n"+
			"	public final int size() {	\n"+
			"		return 0;	\n"+
			"	}	\n"+
			"}	\n");
	
		createFile(
			"/Reconciler/src/tests/SearchableSource.java", 
			"package tests;	\n"+
			"interface SearchableSource extends Source {	\n"+
			"	int indexOf(long value);	\n"+
			"}	\n");
	
		ICompilationUnit compilationUnit = getCompilationUnit("Reconciler", "src", "tests", "AbstractSearchableSource.java");
		ProblemRequestor pbReq =  new ProblemRequestor();
		ICompilationUnit wc = compilationUnit.getWorkingCopy(new WorkingCopyOwner() {}, pbReq, null);
		pbReq.initialize(contents.toCharArray());
		startDeltas();
		wc.reconcile(ICompilationUnit.NO_AST, true, null, null);
		String actual = pbReq.problems.toString();
		String expected = 
			"----------\n" + 
			"----------\n";
		if (!expected.equals(actual)){
		 	System.out.println(Util.displayString(actual, 2));
		}
		assertEquals(
			"unexpected errors",
			expected,
			actual);
	} finally {
		deleteFile("/Reconciler/src/tests/AbstractSearchableSource.java");
		deleteFile("/Reconciler/src/tests/SearchableSource.java");
		deleteFile("/Reconciler/src/tests/Source.java");
		deleteFile("/Reconciler/src/tests/AbstractSource.java");
		deleteFolder("/Reconciler/src/tests");
	}
}
/*
 * Test that the creation of a working copy detects errors
 * (regression test for bug 33757 Problem not detected when opening a working copy)
 */
public void testMethodWithError06() throws CoreException {
	this.workingCopy.discardWorkingCopy(); // don't use the one created in setUp()
	this.workingCopy = null;
	try {
		String contents =
			"package p1;\n" +
			"public class Y {\n" +
			"  public.void foo() {\n" +
			"  }\n" +
			"}";
		createFile(
			"/Reconciler/src/p1/Y.java", 
			contents
		);
		this.problemRequestor =  new ProblemRequestor();
		this.problemRequestor.initialize(contents.toCharArray());
		this.workingCopy = getCompilationUnit("Reconciler/src/p1/Y.java").getWorkingCopy(new WorkingCopyOwner() {}, this.problemRequestor, null);
		assertProblems(
			"Unexpected problems",
			"----------\n" + 
			"1. ERROR in /Reconciler/src/p1/Y.java (at line 3)\n" + 
			"	public.void foo() {\n" + 
			"	      ^\n" + 
			"Syntax error on token \".\", delete this token\n" + 
			"----------\n"
		);
	} finally {
		deleteFile("/Reconciler/src/p1/Y.java");
	}
}
/*
 * Test that the opening of a working copy detects errors
 * (regression test for bug 33757 Problem not detected when opening a working copy)
 */
public void testMethodWithError07() throws CoreException {
	this.workingCopy.discardWorkingCopy(); // don't use the one created in setUp()
	this.workingCopy = null;
	try {
		String contents =
			"package p1;\n" +
			"public class Y {\n" +
			"  public.void foo() {\n" +
			"  }\n" +
			"}";
		createFile(
			"/Reconciler/src/p1/Y.java", 
			contents
		);
		this.problemRequestor =  new ProblemRequestor();
		this.problemRequestor.initialize(contents.toCharArray());
		this.workingCopy = getCompilationUnit("Reconciler/src/p1/Y.java").getWorkingCopy(new WorkingCopyOwner() {}, this.problemRequestor, null);

		// Close working copy
		JavaModelManager.getJavaModelManager().removeInfoAndChildren((CompilationUnit)workingCopy); // use a back door as working copies cannot be closed
		
		// Reopen should detect syntax error
		this.problemRequestor.initialize(contents.toCharArray());
		this.workingCopy.open(null);
		assertProblems(
			"Unexpected problems",
			"----------\n" + 
			"1. ERROR in /Reconciler/src/p1/Y.java (at line 3)\n" + 
		"	public.void foo() {\n" + 
		"	      ^\n" + 
		"Syntax error on token \".\", delete this token\n" + 
		"----------\n"
		);
	} finally {
		deleteFile("/Reconciler/src/p1/Y.java");
	}
}
/*
 * Test that the units with similar names aren't presenting each other errors
 * (regression test for bug 39475)
 */
public void testMethodWithError08() throws CoreException {
	this.workingCopy.discardWorkingCopy(); // don't use the one created in setUp()
	this.workingCopy = null;
	try {
		createFile(
			"/Reconciler/src/p1/X01.java", 
			"package p1;\n" +
			"public abstract class X01 {\n" +
			"	public abstract void bar();	\n"+
			"  public abstract void foo(Zork z); \n"+
			"}"
		);
		String contents = 
			"package p2;\n" +
			"public class X01 extends p1.X01 {\n" +
			"	public void bar(){}	\n"+
			"}";
		createFile(
			"/Reconciler/src/p2/X01.java", 
			contents
		);
		this.problemRequestor =  new ProblemRequestor();
		this.problemRequestor.initialize(contents.toCharArray());
		this.workingCopy = getCompilationUnit("Reconciler/src/p2/X01.java").getWorkingCopy(new WorkingCopyOwner() {}, this.problemRequestor, null);

		// Close working copy
		JavaModelManager.getJavaModelManager().removeInfoAndChildren((CompilationUnit)workingCopy); // use a back door as working copies cannot be closed
		
		// Reopen should detect syntax error
		this.problemRequestor.initialize(contents.toCharArray());
		this.workingCopy.open(null);
		assertProblems(
			"Unexpected problems",
			"----------\n" + 
			"----------\n" // shouldn't report problem against p2.X01
		);
	} finally {
		deleteFile("/Reconciler/src/p1/X01.java");
		deleteFile("/Reconciler/src/p2/X01.java");
	}
}
/*
 * Scenario of reconciling using a working copy owner
 */
public void testMethodWithError09() throws CoreException {
	this.workingCopy.discardWorkingCopy(); // don't use the one created in setUp()
	this.workingCopy = null;
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	ICompilationUnit workingCopy1 = null;
	try {
		workingCopy1 = getCompilationUnit("/Reconciler/src/p1/X1.java").getWorkingCopy(owner, null, null);
		workingCopy1.getBuffer().setContents(
			"package p1;\n" +
			"public abstract class X1 {\n" +
			"	public abstract void bar();	\n"+
			"}"
		);
		workingCopy1.makeConsistent(null);
		
		this.problemRequestor =  new ProblemRequestor();
		this.workingCopy = getCompilationUnit("Reconciler/src/p/X.java").getWorkingCopy(owner, this.problemRequestor, null);
		setWorkingCopyContents(
			"package p;\n" +
			"public class X extends p1.X1 {\n" +
			"	public void bar(){}	\n"+
			"}"
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, owner, null);

		assertProblems(
			"Unexpected problems",
			"----------\n" + 
			"----------\n" // shouldn't report problem against p.X
		);
	} finally {
		if (workingCopy1 != null) {
			workingCopy1.discardWorkingCopy();
		}
	}
}
/*
 * Scenario of reconciling using a working copy owner  (68557)
 */
public void testMethodWithError10() throws CoreException {
	this.workingCopy.discardWorkingCopy(); // don't use the one created in setUp()
	this.workingCopy = null;
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	ICompilationUnit workingCopy1 = null;
	try {
		createFolder("/Reconciler15/src/test/cheetah");
		workingCopy1 = getCompilationUnit("/Reconciler15/src/test/cheetah/NestedGenerics.java").getWorkingCopy(owner, null, null);
		workingCopy1.getBuffer().setContents(
			"package test.cheetah;\n"+
			"import java.util.List;\n"+
			"import java.util.Stack;\n"+
			"public class NestedGenerics {\n"+
			"    Stack< List<Object>> stack = new Stack< List<Object> >();\n"+
			"}\n"
		);
		workingCopy1.makeConsistent(null);
		
		this.problemRequestor =  new ProblemRequestor();
		this.workingCopy = getCompilationUnit("Reconciler15/src/test/cheetah/NestedGenericsTest.java").getWorkingCopy(owner, this.problemRequestor, null);
		setWorkingCopyContents(
			"package test.cheetah;\n"+
			"import java.util.Stack;\n"+
			"public class NestedGenericsTest {\n"+
			"    void test() {  \n"+
			"        Stack s = new NestedGenerics().stack;  \n"+
			"    }\n"+
			"}\n"
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, owner, null);

		assertProblems(
			"Unexpected problems",
			"----------\n" + 
			"----------\n"
		);
	} finally {
		if (workingCopy1 != null) {
			workingCopy1.discardWorkingCopy();
		}
		deleteFolder("/Reconciler15/src/test");
	}
}
/*
 * Scenario of reconciling using a working copy owner (68557)
 */
public void testMethodWithError11() throws CoreException {
	this.workingCopy.discardWorkingCopy(); // don't use the one created in setUp()
	this.workingCopy = null;
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	ICompilationUnit workingCopy1 = null;
	try {
		createFolder("/Reconciler15/src/test/cheetah");
		workingCopy1 = getCompilationUnit("/Reconciler15/src/test/cheetah/NestedGenerics.java").getWorkingCopy(owner, null, null);
		workingCopy1.getBuffer().setContents(
			"package test.cheetah;\n"+
			"import java.util.*;\n"+
			"public class NestedGenerics {\n"+
			"    Map<List<Object>,String> map = null;\n"+
			"    Stack<List<Object>> stack2 = null;\n"+
			"    Map<List<Object>,List<Object>> map3 = null;\n"+
			"}\n"
		);
		workingCopy1.makeConsistent(null);
		
		this.problemRequestor =  new ProblemRequestor();
		this.workingCopy = getCompilationUnit("Reconciler15/src/test/cheetah/NestedGenericsTest.java").getWorkingCopy(owner, this.problemRequestor, null);
		setWorkingCopyContents(
			"package test.cheetah;\n"+
			"import java.util.*;\n"+
			"public class NestedGenericsTest {\n"+
			"    void test() {  \n"+
			"        Map m = new NestedGenerics().map;  \n"+
			"		 Stack s2 = new NestedGenerics().stack2;    \n"+
			"        Map m3 = new NestedGenerics().map3;    \n"+
			"    }\n"+
			"}\n"
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, owner, null);

		assertProblems(
			"Unexpected problems",
			"----------\n" + 
			"----------\n"
		);
	} finally {
		if (workingCopy1 != null) {
			workingCopy1.discardWorkingCopy();
		}
		deleteFolder("/Reconciler15/src/test");
	}
}
/*
 * Scenario of reconciling using a working copy owner (68557 variation with wildcards)
 */
public void testMethodWithError12() throws CoreException {
	this.workingCopy.discardWorkingCopy(); // don't use the one created in setUp()
	this.workingCopy = null;
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	ICompilationUnit workingCopy1 = null;
	try {
		createFolder("/Reconciler15/src/test/cheetah");
		workingCopy1 = getCompilationUnit("/Reconciler15/src/test/cheetah/NestedGenerics.java").getWorkingCopy(owner, null, null);
		workingCopy1.getBuffer().setContents(
			"package test.cheetah;\n"+
			"import java.util.*;\n"+
			"public class NestedGenerics {\n"+
			"    Map<List<?>,? super String> map = null;\n"+
			"    Stack<List<? extends Object>> stack2 = null;\n"+
			"    Map<List<Object[]>,List<Object>[]> map3 = null;\n"+
			"}\n"
		);
		workingCopy1.makeConsistent(null);
		
		this.problemRequestor =  new ProblemRequestor();
		this.workingCopy = getCompilationUnit("Reconciler15/src/test/cheetah/NestedGenericsTest.java").getWorkingCopy(owner, this.problemRequestor, null);
		setWorkingCopyContents(
			"package test.cheetah;\n"+
			"import java.util.*;\n"+
			"public class NestedGenericsTest {\n"+
			"    void test() {  \n"+
			"        Map m = new NestedGenerics().map;  \n"+
			"		 Stack s2 = new NestedGenerics().stack2;    \n"+
			"        Map m3 = new NestedGenerics().map3;    \n"+
			"    }\n"+
			"}\n"
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, owner, null);

		assertProblems(
			"Unexpected problems",
			"----------\n" + 
			"----------\n"
		);
	} finally {
		if (workingCopy1 != null) {
			workingCopy1.discardWorkingCopy();
		}
		deleteFolder("/Reconciler15/src/test");
	}
}
/*
 * Scenario of reconciling using a working copy owner (68730)
 */
public void testMethodWithError13() throws CoreException {
	this.workingCopy.discardWorkingCopy(); // don't use the one created in setUp()
	this.workingCopy = null;
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	ICompilationUnit workingCopy1 = null;
	try {
		workingCopy1 = getCompilationUnit("/Reconciler15/src/test/X.java").getWorkingCopy(owner, null, null);
		createFolder("/Reconciler15/src/test");
		workingCopy1.getBuffer().setContents(
			"package test;\n"+
			"public class X <T extends String, U> {\n"+
			"	<Y1> void bar(Y1[] y) {}\n"+
			"	void bar2(Y<E3[]>[] ye[]) {}\n"+
			"    void foo(java.util.Map<Object[],String>.MapEntry<p.K<T>[],? super q.r.V8> m){}\n"+
			"    Class<? extends Object> getClass0() {}\n"+
			"    <E extends String> void pair (X<? extends E, U> e, T t){}\n"+
			"}\n"
		);
		workingCopy1.makeConsistent(null);
		
		this.problemRequestor =  new ProblemRequestor();
		this.workingCopy = getCompilationUnit("Reconciler15/src/test/Y.java").getWorkingCopy(owner, this.problemRequestor, null);
		setWorkingCopyContents(
			"package test;\n"+
			"public class Y {\n"+
			"	void foo(){\n"+
			"		X someX = new X();\n"+
			"		someX.bar(null);\n"+
			"	}\n"+
			"}\n"
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, owner, null);

		assertProblems(
			"Unexpected problems",
		"----------\n" + 
		"1. WARNING in /Reconciler15/src/test/Y.java (at line 5)\n" + 
		"	someX.bar(null);\n" + 
		"	^^^^^^^^^^^^^^^\n" + 
		"Type safety: The method bar(Object[]) belongs to the raw type X. References to generic type X<T,U> should be parameterized\n" + 
		"----------\n"
		);
	} finally {
		if (workingCopy1 != null) {
			workingCopy1.discardWorkingCopy();
		}
		deleteFolder("/Reconciler15/src/test");
	}
}
/*
 * Scenario of reconciling using a working copy owner (66424)
 */
public void testMethodWithError14() throws CoreException {
	this.workingCopy.discardWorkingCopy(); // don't use the one created in setUp()
	this.workingCopy = null;
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	ICompilationUnit workingCopy1 = null;
	try {
		workingCopy1 = getCompilationUnit("/Reconciler15/src/test/X.java").getWorkingCopy(owner, null, null);
		createFolder("/Reconciler15/src/test");
		workingCopy1.getBuffer().setContents(
			"package test;\n"+
			"public class X <T> {\n"+
			"	<U> void bar(U u) {}\n"+
			"}\n"
		);
		workingCopy1.makeConsistent(null);
		
		this.problemRequestor =  new ProblemRequestor();
		this.workingCopy = getCompilationUnit("Reconciler15/src/test/Y.java").getWorkingCopy(owner, this.problemRequestor, null);
		setWorkingCopyContents(
			"package test;\n"+
			"public class Y {\n"+
			"	void foo(){\n"+
			"		X someX = new X();\n"+
			"		someX.bar();\n"+
			"	}\n"+
			"}\n"
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, owner, null);

		assertProblems(
			"Unexpected problems",
			"----------\n" + 
			"1. ERROR in /Reconciler15/src/test/Y.java (at line 5)\n" + 
			"	someX.bar();\n" + 
			"	      ^^^\n" + 
			"The method bar(Object) in the type X is not applicable for the arguments ()\n" + 
			"----------\n"
		);
	} finally {
		if (workingCopy1 != null) {
			workingCopy1.discardWorkingCopy();
		}
		deleteFolder("/Reconciler15/src/test");
	}
}
/**
 * Ensures that the reconciler handles member move correctly.
 */
public void testMoveMember() throws JavaModelException {
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"  public void bar() {\n" +
		"  }\n" +
		"}");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	clearDeltas();
	
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void bar() {\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"  }\n" +
		"}");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected delta", 
		"X[*]: {CHILDREN | FINE GRAINED}\n" + 
		"	bar()[*]: {REORDERED}\n" + 
		"	foo()[*]: {REORDERED}"
	);
}
/**
 * Ensures that the reconciler does nothing when the source
 * to reconcile with is the same as the current contents.
 */
public void testNoChanges1() throws JavaModelException {
	setWorkingCopyContents(this.workingCopy.getSource());
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected delta",
		""
	);
}
/**
 * Ensures that the reconciler does nothing when the source
 * to reconcile with has the same structure as the current contents.
 */
public void testNoChanges2() throws JavaModelException {
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void foo() {\n" +
		"    System.out.println()\n" +
		"  }\n" +
		"}"
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected delta",
		""
	);
}
/**
 * Ensures that the reconciler reconciles the new contents with the current
 * contents, updating the structure of this reconciler's compilation
 * unit, and fires the Java element deltas for the structural changes
 * of a renaming a method; the original method deleted and the new method added structurally.
 */
public void testRenameMethod1() throws JavaModelException {
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void bar() {\n" +
		"  }\n" +
		"}"
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected delta",
		"X[*]: {CHILDREN | FINE GRAINED}\n" + 
		"	bar()[+]: {}\n" + 
		"	foo()[-]: {}"
	);
}
/**
 * Ensures that the reconciler reconciles the new contents with the current
 * contents,updating the structure of this reconciler's compilation
 * unit, and fires the Java element delta for the structural changes
 * of the addition of a portion of a new method.
 */
public void testRenameWithSyntaxError() throws JavaModelException {
	setWorkingCopyContents(
		"package p1;\n" +
		"import p2.*;\n" +
		"public class X {\n" +
		"  public void bar( {\n" +
		"  }\n" +
		"}"
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected delta",
		"X[*]: {CHILDREN | FINE GRAINED}\n" + 
		"	bar()[+]: {}\n" + 
		"	foo()[-]: {}"
	);
	assertProblems(
		"Unexpected problems",
		"----------\n" + 
		"1. ERROR in /Reconciler/src/p1/X.java (at line 4)\n" + 
		"	public void bar( {\n" + 
		"	               ^\n" + 
		"Syntax error, insert \")\" to complete MethodDeclaration\n" + 
		"----------\n"
	);
}
/**
 * Ensure that an unhandled exception is detected.
 */
public void testUnhandledException() throws JavaModelException {
	setWorkingCopyContents(
		"package p1;\n" +
		"public class X {\n" +
		"  public void foo() {\n" +
		"    throw new Exception();\n" +
		"  }\n" +
		"}"
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertProblems(
		"Unexpected problems",
		"----------\n" + 
		"1. ERROR in /Reconciler/src/p1/X.java (at line 4)\n" + 
		"	throw new Exception();\n" + 
		"	^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Unhandled exception type Exception\n" + 
		"----------\n"
	);
}
/**
 * Check that forcing a make consistent action is leading the next reconcile to not notice changes.
 */
public void testMakeConsistentFoolingReconciler() throws JavaModelException {
	setWorkingCopyContents("");
	this.workingCopy.makeConsistent(null);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Should have got NO delta", 
		""
	);
}
/**
 * Test bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=60689
 * AST on reconcile: AST without Javadoc comments created
 * @deprecated using deprecated code
 */
public void testBug60689() throws JavaModelException {
	setWorkingCopyContents("public class X {\n" +
		"	/**\n" +
		"	 * Returns the length of the string representing the number of \n" +
		"	 * indents in the given string <code>line</code>. Returns \n" +
		"	 * <code>-1<code> if the line isn't prefixed with an indent of\n" +
		"	 * the given number of indents. \n" +
		"	 */\n" +
		"	public static int computeIndentLength(String line, int numberOfIndents, int tabWidth) {\n" +
		"		return 0;\n" +
		"}"
	);
	org.eclipse.jdt.core.dom.CompilationUnit testCU = this.workingCopy.reconcile(AST.JLS2, true, null, null);
	assertNotNull("We should have a comment!", testCU.getCommentList());
	assertEquals("We should have 1 comment!", 1, testCU.getCommentList().size());
	testCU = this.workingCopy.reconcile(AST.JLS2, true, null, null);
	assertNotNull("We should have a comment!", testCU.getCommentList());
	assertEquals("We should have one comment!", 1, testCU.getCommentList().size());
}
/*
 * Ensures that a method that has a type parameter with bound can be overriden in another working copy.
 * (regression test for bug 76780 [model] return type not recognized correctly on some generic methods)
 */
public void testTypeParameterWithBound() throws CoreException {
	this.workingCopy.discardWorkingCopy(); // don't use the one created in setUp()
	this.workingCopy = null;
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	ICompilationUnit workingCopy1 = null;
	try {
		workingCopy1 = getWorkingCopy(
			"/Reconciler15/src/test/I.java",
			"package test;\n"+
			"public interface I {\n"+
			"	<T extends I> void foo(T t);\n"+
			"}\n",
			owner,
			null /*no problem requestor*/
		);
		
		this.problemRequestor =  new ProblemRequestor();
		this.workingCopy = getWorkingCopy("Reconciler15/src/test/X.java", "", owner, this.problemRequestor);
		setWorkingCopyContents(
			"package test;\n"+
			"public class X implements I {\n"+
			"	public <T extends I> void foo(T t) {\n"+
			"	}\n"+
			"}\n"
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, owner, null);

		assertProblems(
			"Unexpected problems",
			"----------\n" + 
			"----------\n"
		);
	} finally {
		if (workingCopy1 != null) {
			workingCopy1.discardWorkingCopy();
		}
	}
}
/*
 * Ensures that a varargs method can be referenced from another working copy.
 */
public void testVarargs() throws CoreException {
	this.workingCopy.discardWorkingCopy(); // don't use the one created in setUp()
	this.workingCopy = null;
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	ICompilationUnit workingCopy1 = null;
	try {
		workingCopy1 = getWorkingCopy(
			"/Reconciler15/src/test/X.java",
			"package test;\n"+
			"public class X {\n"+
			"	void bar(String ... args) {}\n"+
			"}\n",
			owner,
			null /*no problem requestor*/
		);
		
		this.problemRequestor =  new ProblemRequestor();
		this.workingCopy = getWorkingCopy("Reconciler15/src/test/Y.java", "", owner, this.problemRequestor);
		setWorkingCopyContents(
			"package test;\n"+
			"public class Y {\n"+
			"	void foo(){\n"+
			"		X someX = new X();\n"+
			"		someX.bar(\"a\", \"b\");\n"+
			"	}\n"+
			"}\n"
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, owner, null);

		assertProblems(
			"Unexpected problems",
			"----------\n" + 
			"----------\n"
		);
	} finally {
		if (workingCopy1 != null) {
			workingCopy1.discardWorkingCopy();
		}
	}
}

}
