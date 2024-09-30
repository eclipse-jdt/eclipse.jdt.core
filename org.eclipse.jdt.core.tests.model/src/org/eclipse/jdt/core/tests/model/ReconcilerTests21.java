/*******************************************************************************
* Copyright (c) 2024 Advantest Europe GmbH and others.
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Srikanth Sankaran - initial implementation
*******************************************************************************/

package org.eclipse.jdt.core.tests.model;


import junit.framework.Test;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.dom.AST;

public class ReconcilerTests21 extends ModifyingResourceTests {

	protected ICompilationUnit workingCopy;
	protected ProblemRequestor problemRequestor;
	static final int JLS_LATEST = AST.JLS21;

public ReconcilerTests21(String name) {
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
	return buildModelTestSuite(ReconcilerTests21.class);
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
			return ReconcilerTests21.this.problemRequestor;
		}
	};
	this.workingCopy = getCompilationUnit("Reconciler21/src/module-info.java").getWorkingCopy(this.wcOwner, null);
	this.problemRequestor.initialize(this.workingCopy.getSource().toCharArray());
	startDeltas();
}
@Override
public void setUpSuite() throws Exception {
	super.setUpSuite();

	// Create project with 21 compliance
	IJavaProject project21 = createJava21Project("Reconciler21");
	project21.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_21);
	project21.setOption(JavaCore.COMPILER_PB_UNUSED_LOCAL, JavaCore.IGNORE);
	project21.setOption(JavaCore.COMPILER_PB_INVALID_JAVADOC, JavaCore.WARNING);


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
	deleteProject("Reconciler21");
	super.tearDownSuite();
}

// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2108
// [Sealed classes] Inconsistent type hierarchy on sealed interface when used with other from different level in class hierarchy
public void testIssue2108() throws Exception {
	if (!isJRE21)
		return;
	IJavaProject p = createJava21Project("p");
	try {
		createFile("p/src/BaseInterface.java",
				   "public sealed interface BaseInterface permits SubInterfaceA, SubInterfaceB {}\n");
		createFile("p/src/SubInterfaceA.java",
				   "public sealed interface SubInterfaceA extends BaseInterface permits TestClassA, SubSubInterfaceA {}\n");
		createFile("p/src/SubInterfaceB.java",
				   "public sealed interface SubInterfaceB extends BaseInterface permits TestClassB {}\n");
		createFile("p/src/SubSubInterfaceA.java",
				   "public sealed interface SubSubInterfaceA extends SubInterfaceA permits TestClassB {}\n");
		createFile("p/src/TestClassA.java",
				   "public final class TestClassA implements SubInterfaceA {}\n");
		createFile("p/src/TestClassB.java",
				   "public final class TestClassB implements SubSubInterfaceA, SubInterfaceB {}\n");

		p.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
		IMarker[] markers = p.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
		assertMarkers("markers in p",
				"",
				markers);

		this.workingCopy = getCompilationUnit("p/src/SubSubInterfaceA.java").getWorkingCopy(this.wcOwner, null);
		this.problemRequestor.initialize(this.workingCopy.getSource().toCharArray());
		this.workingCopy.reconcile(JLS_LATEST, true, this.wcOwner, null);
		assertProblems("Expecting no problems",
				"----------\n" +
				"----------\n",
				this.problemRequestor);
		this.workingCopy.discardWorkingCopy();
	} finally {
		deleteProject(p);
	}
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/62
// [Reconciler][Sealed types] Inconsistent type hierarchy with sealed classes
public void testIssue62() throws Exception {
	if (!isJRE21)
		return;
	IJavaProject p = createJava21Project("p");
	try {
		createFile("p/src/Interface1.java",
				   "public sealed interface Interface1 permits Class1, Interface2 {}\n");
		createFile("p/src/Interface2.java",
				   "public sealed interface Interface2 extends Interface1 permits Class2, Interface3 {}\n");
		createFile("p/src/Interface3.java",
				   "public sealed interface Interface3 extends Interface2 permits Class3 {}\n");
		createFile("p/src/Class1.java",
				   "public non-sealed class Class1 implements Interface1 {}\n");
		createFile("p/src/Class2.java",
				   "public non-sealed class Class2 extends Class1 implements Interface2 {}\n");
		createFile("p/src/Class3.java",
				   "public final class Class3 extends Class2 implements Interface3 {}\n");

		p.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
		IMarker[] markers = p.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
		assertMarkers("markers in p",
				"",
				markers);

		this.workingCopy = getCompilationUnit("p/src/Class2.java").getWorkingCopy(this.wcOwner, null);
		this.problemRequestor.initialize(this.workingCopy.getSource().toCharArray());
		this.workingCopy.reconcile(JLS_LATEST, true, this.wcOwner, null);
		assertProblems("Expecting no problems",
				"----------\n" +
				"----------\n",
				this.problemRequestor);
		this.workingCopy.discardWorkingCopy();
	} finally {
		deleteProject(p);
	}
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1998
// [Sealed classes] Incorrect Reconciler error: The type A that implements a sealed interface I should be a permitted subtype of I
public void testIssue1998() throws Exception {
	if (!isJRE21)
		return;
	IJavaProject p = createJava21Project("p");
	try {
		createFile("p/src/X.java",
				"""
				sealed interface I {}
				final class A implements I {}

				record R<T extends I>(T x, T  y) {}

				public class X {
				    public static int foo(R r) {
				       return  switch (r) {
				            case R(A a1, A a2) -> 0;
				        };
				    }

				    @SuppressWarnings("unchecked")
				       public static void main(String argv[]) {
				       System.out.println(X.foo(new R(new A(), new A())));
				    }
				}
				""");
		p.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
		IMarker[] markers = p.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
		assertMarkers("markers in p",
				"R is a raw type. References to generic type R<T> should be parameterized\n" +
				"R is a raw type. References to generic type R<T> should be parameterized",
				markers);

		ICompilationUnit unit = getCompilationUnit("p/src/X.java");
		this.problemRequestor.initialize(unit.getSource().toCharArray());
		this.workingCopy = getCompilationUnit("p/src/X.java").getWorkingCopy(this.wcOwner, null);
		assertProblems("Expecting no problems",
				"----------\n" +
				"1. WARNING in /p/src/X.java (at line 7)\n" +
				"	public static int foo(R r) {\n" +
				"	                      ^\n" +
				"R is a raw type. References to generic type R<T> should be parameterized\n" +
				"----------\n" +
				"2. WARNING in /p/src/X.java (at line 15)\n" +
				"	System.out.println(X.foo(new R(new A(), new A())));\n" +
				"	                             ^\n" +
				"R is a raw type. References to generic type R<T> should be parameterized\n" +
				"----------\n",
				this.problemRequestor);
		this.workingCopy.discardWorkingCopy();
	} finally {
		deleteProject(p);
	}
}
public void testGH2782() throws CoreException {
	IJavaProject p = createJava21Project("p");
	try {
		createFolder("p/src/test");
		createFile("p/src/test/StringVar2.java",
				"""
				package test;
                public class StringVar2 {
                  public void test () {
                    "foo".
                    if (true);
                  }
                }
				""");
		p.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
		IMarker[] markers = p.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
		sortMarkers(markers);
		assertMarkers("markers in p",
				"""
				Constructor call must be the first statement in a constructor
				Syntax error on token "if", super expected""",
				markers);

		ICompilationUnit unit = getCompilationUnit("p/src/test/StringVar2.java");
		this.problemRequestor.initialize(unit.getSource().toCharArray());
		this.workingCopy = getCompilationUnit("p/src/test/StringVar2.java").getWorkingCopy(this.wcOwner, null);
		assertProblems("Expecting no problems",
				"""
				----------
				1. ERROR in /p/src/test/StringVar2.java (at line 5)
					if (true);
					^^
				Syntax error on token "if", super expected
				----------
				""",
				this.problemRequestor);
		this.workingCopy.discardWorkingCopy();
	} finally {
		deleteProject(p);
	}
}

}