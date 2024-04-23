/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import junit.framework.Test;

public class AccessRestrictionsTests extends ModifyingResourceTests {
	static class ProblemRequestor extends AbstractJavaModelTests.ProblemRequestor {
		ProblemRequestor (String source) {
			if (source != null)
				this.unitSource = source.toCharArray();
		}
		ProblemRequestor() {
		}
		@Override
		public void acceptProblem(IProblem problem) {
			super.acceptProblem(problem);
		}
	}

	protected ProblemRequestor problemRequestor;

	public AccessRestrictionsTests(String name) {
		super(name);
	}

	// Use this static initializer to specify subset for tests
	// All specified tests which do not belong to the class are skipped...
	static {
		// Names of tests to run, like "testXXX"
  		//TESTS_NAMES = new String[] { "test004" };
		// Numbers of tests to run: "test<number>" will be run for each number of this array
		//TESTS_NUMBERS = new int[] { 1 };
		// Range numbers of tests to run: all tests between "test<first>" and "test<last>" will be run for { first, last }
		//TESTS_RANGE = new int[] { 16, -1 };
	}

	public static Test suite() {
		return buildModelTestSuite(AccessRestrictionsTests.class);
	}

@Override
public void setUpSuite() throws Exception {
	super.setUpSuite();
	setUpJavaProject("AccessRestrictions");
}

@Override
public void tearDownSuite() throws Exception {
	deleteProject("AccessRestrictions");
	super.tearDownSuite();
}

	protected void assertProblems(String message, String expected) {
		assertProblems(message, expected, this.problemRequestor);
	}

	@Override
	public ICompilationUnit getWorkingCopy(String path, String source) throws JavaModelException {
		return getWorkingCopy(path, source, this.wcOwner);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.wcOwner = new WorkingCopyOwner() {
			public IProblemRequestor getProblemRequestor(ICompilationUnit unit) {
				return AccessRestrictionsTests.this.problemRequestor;
			}
		};
	}

/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=76266
 * Ensures that a problem is created for a reference to a method of a type that is not
 * accessible in a prereq project, even though it is accessed through an intermediate
 * accessible class.
 */
public void test001() throws CoreException {
	ICompilationUnit x1 = null, x2 = null, y =  null, z = null;
	try {
		createJavaProject(
			"P1",
			new String[] {"src"},
			new String[] {"JCL_LIB"},
			"bin");
		this.problemRequestor = new ProblemRequestor();
		x1 = getWorkingCopy(
			"/P1/src/p/X1.java",
			"package p;\n" +
			"public class X1 {\n" +
			"	void foo() {\n" +
			"	}\n" +
			"}"
		);
		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n"
		);
		x2 = getWorkingCopy(
			"/P1/src/p/X2.java",
			"package p;\n" +
			"public class X2 extends X1 {\n" +
			"	void bar() {\n" +
			"	}\n" +
			"}"
		);
		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n"
		);
		IJavaProject p2 = createJavaProject("P2", new String[] {"src"},
				new String[] {"JCL_LIB"}, "bin");
		IClasspathEntry[] classpath = p2.getRawClasspath();
		int length = classpath.length;
		System.arraycopy(classpath, 0, classpath = new IClasspathEntry[length+1], 0, length);
		classpath[length] = createSourceEntry("P2", "/P1", "-p/X1");
		p2.setRawClasspath(classpath, null);
		// check the most basic case
		String src =
			"package p;\n" +
			"public class Z extends X1 {\n" +
			"}";
		this.problemRequestor = new ProblemRequestor(src);
		z = getWorkingCopy(
			"/P2/src/p/Z.java",
			src
		);
		assertProblems(
			"Unexpected problems value",
			"----------\n" +
			"1. ERROR in /P2/src/p/Z.java (at line 2)\n" +
			"	public class Z extends X1 {\n" +
			"	                       ^^\n" +
			"Access restriction: The type \'X1\' is not API (restriction on required project \'P1\')\n" +
			"----------\n"
		);
		// check the specifics of this test case
		src =
			"package p;\n" +
			"public class Y extends X2 {\n" +
			"	void foobar() {\n" +
			"		foo(); // accesses X1.foo, should trigger an error\n" +
			"		bar(); // accesses X2.bar, OK\n" +
			"	}\n" +
			"}";
		this.problemRequestor = new ProblemRequestor(src);
		y = getWorkingCopy(
			"/P2/src/p/Y.java",
			src
		);
		assertProblems(
			"Unexpected problems value",
			"----------\n" +
			"1. ERROR in /P2/src/p/Y.java (at line 4)\n" +
			"	foo(); // accesses X1.foo, should trigger an error\n" +
			"	^^^\n" +
			"Access restriction: The method \'X1.foo()\' is not API (restriction on required project \'P1\')\n" +
			"----------\n"
		);
	} finally {
		if (x1 != null)
			x1.discardWorkingCopy();
		if (x2 != null)
			x2.discardWorkingCopy();
		if (y != null)
			y.discardWorkingCopy();
		if (z != null)
			z.discardWorkingCopy();
		deleteProjects(new String[] {"P1", "P2"});
	}
}

/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=76266
 * Ensures that a problem is created for a reference to a field of a type that is not
 * accessible in a prereq project, even though it is accessed through an intermediate
 * accessible class.
 */
public void test002() throws CoreException {
	ICompilationUnit x1 = null, x2 = null, y =  null;
	try {
		createJavaProject(
			"P1",
			new String[] {"src"},
			new String[] {"JCL_LIB"},
			"bin");
		this.problemRequestor = new ProblemRequestor();
		x1 = getWorkingCopy(
			"/P1/src/p/X1.java",
			"package p;\n" +
			"public class X1 {\n" +
			"	int m1;\n" +
			"}"
		);
		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n"
		);
		x2 = getWorkingCopy(
			"/P1/src/p/X2.java",
			"package p;\n" +
			"public class X2 extends X1 {\n" +
			"	char m2;\n" +
			"}"
		);
		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n"
		);
		IJavaProject p2 = createJavaProject("P2", new String[] {"src"},
				new String[] {"JCL_LIB"}, "bin");
		IClasspathEntry[] classpath = p2.getRawClasspath();
		int length = classpath.length;
		System.arraycopy(classpath, 0, classpath = new IClasspathEntry[length+1], 0, length);
		classpath[length] = createSourceEntry("P2", "/P1", "-p/X1");
		p2.setRawClasspath(classpath, null);
		String src =
			"package p;\n" +
			"public class Y extends X2 {\n" +
			"	int foobar() {\n" +
			"		int l1 = m1; // accesses X1.m1, should trigger an error\n" +
			"		char l2 = m2; // accesses X2.m2, OK\n" +
			"		return l1 + l2;\n" +
			"	}\n" +
			"}";
		this.problemRequestor = new ProblemRequestor(src);
		y = getWorkingCopy(
			"/P2/src/p/Y.java",
			src
		);
		assertProblems(
			"Unexpected problems value",
			"----------\n" +
			"1. ERROR in /P2/src/p/Y.java (at line 4)\n" +
			"	int l1 = m1; // accesses X1.m1, should trigger an error\n" +
			"	         ^^\n" +
			"Access restriction: The field \'X1.m1\' is not API (restriction on required project \'P1\')\n" +
			"----------\n"
		);
	} finally {
		if (x1 != null)
			x1.discardWorkingCopy();
		if (x2 != null)
			x2.discardWorkingCopy();
		if (y != null)
			y.discardWorkingCopy();
		deleteProjects(new String[] {"P1", "P2"});
	}
}

/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=76266
 * Ensures that a problem is created for a reference to a member type of a type that is not
 * accessible in a prereq project, even though it is accessed through an intermediate
 * accessible class.
 */
public void test003() throws CoreException {
	ICompilationUnit x1 = null, x2 = null, y =  null;
	try {
		createJavaProject(
			"P1",
			new String[] {"src"},
			new String[] {"JCL_LIB"},
			"bin");
		this.problemRequestor = new ProblemRequestor();
		x1 = getWorkingCopy(
			"/P1/src/p/X1.java",
			"package p;\n" +
			"public class X1 {\n" +
			"	class C1 {\n" +
			"	   protected C1 (int dummy) {}\n" +
			"	   protected void foo() {}\n" +
			"	}\n" +
			"	interface I1 {}\n" +
			"}"
		);
		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n"
		);
		x2 = getWorkingCopy(
			"/P1/src/p/X2.java",
			"package p;\n" +
			"public class X2 extends X1 {\n" +
			"	class C2 {}\n" +
			"	interface I2 {}\n" +
			"}"
		);
		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n"
		);
		IJavaProject p2 = createJavaProject("P2", new String[] {"src"},
				new String[] {"JCL_LIB"}, "bin");
		IClasspathEntry[] classpath = p2.getRawClasspath();
		int length = classpath.length;
		System.arraycopy(classpath, 0, classpath = new IClasspathEntry[length+1], 0, length);
		classpath[length] = createSourceEntry("P2", "/P1", "-p/X1");
		p2.setRawClasspath(classpath, null);
		String src =
			"package p;\n" +
			"public class Y extends X2 {\n" +
			"	class C3a extends C1 {      // error\n" +
			"	   C3a() {\n" +
			"	      super(0);\n" +
			"	      foo();                // error\n" +
			"	   }\n" +
			"	}\n" +
			"	class C3c extends C2 implements I2 {}\n" +
			"	String foobar() {\n" +
			"		C1 m1 =                 // error\n" +
			"		        new C1(0);      // error\n" +
			"		C2 m2 = new C2();\n" +
			"		return m1 == null || m2 == null ? \"!OK\" : \"OK\";\n" +
			"	}\n" +
			"}";
		this.problemRequestor = new ProblemRequestor(src);
		y = getWorkingCopy(
			"/P2/src/p/Y.java",
			src
		);
		assertProblems(
			"Unexpected problems value",
			"----------\n" +
			"1. ERROR in /P2/src/p/Y.java (at line 3)\n" +
			"	class C3a extends C1 {      // error\n" +
			"	                  ^^\n" +
			"Access restriction: The type \'X1.C1\' is not API (restriction on required project \'P1\')\n" +
			"----------\n" +
			"2. ERROR in /P2/src/p/Y.java (at line 5)\n" +
			"	super(0);\n" +
			"	^^^^^^^^^\n" +
			"Access restriction: The constructor \'X1.C1(int)\' is not API (restriction on required project \'P1\')\n" +
			"----------\n" +
			"3. ERROR in /P2/src/p/Y.java (at line 6)\n" +
			"	foo();                // error\n" +
			"	^^^\n" +
			"Access restriction: The method \'X1.C1.foo()\' is not API (restriction on required project \'P1\')\n" +
			"----------\n" +
			"4. ERROR in /P2/src/p/Y.java (at line 11)\n" +
			"	C1 m1 =                 // error\n" +
			"	^^\n" +
			"Access restriction: The type \'X1.C1\' is not API (restriction on required project \'P1\')\n" +
			"----------\n" +
			"5. ERROR in /P2/src/p/Y.java (at line 12)\n" +
			"	new C1(0);      // error\n" +
			"	    ^^\n" +
			"Access restriction: The type \'X1.C1\' is not API (restriction on required project \'P1\')\n" +
			"----------\n" +
			"6. ERROR in /P2/src/p/Y.java (at line 12)\n" +
			"	new C1(0);      // error\n" +
			"	    ^^\n" +
			"Access restriction: The constructor \'X1.C1(int)\' is not API (restriction on required project \'P1\')\n" +
			"----------\n"
		);
	} finally {
		if (x1 != null)
			x1.discardWorkingCopy();
		if (x2 != null)
			x2.discardWorkingCopy();
		if (y != null)
			y.discardWorkingCopy();
		deleteProjects(new String[] {"P1", "P2"});
	}
}

/*
 * Discouraged access message - type via discouraged rule.
 */
public void test004() throws CoreException {
	ICompilationUnit x1 = null, z = null;
	try {
		createJavaProject(
			"P1",
			new String[] {"src"},
			new String[] {"JCL_LIB"},
			"bin");
		this.problemRequestor = new ProblemRequestor();
		x1 = getWorkingCopy(
			"/P1/src/p/X1.java",
			"package p;\n" +
			"public class X1 {\n" +
			"	class C1 {}\n" +
			"	interface I1 {}\n" +
			"}"
		);
		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n"
		);
		IJavaProject p2 = createJavaProject("P2", new String[] {"src"},
				new String[] {"JCL_LIB"}, "bin");
		IClasspathEntry[] classpath = p2.getRawClasspath();
		int length = classpath.length;
		System.arraycopy(classpath, 0, classpath = new IClasspathEntry[length+1], 0, length);
		classpath[length] = createSourceEntry("P2", "/P1", "~p/X1");
		p2.setRawClasspath(classpath, null);
		String src =
			"package p;\n" +
			"public class Z extends X1 {\n" +
			"}";
		this.problemRequestor = new ProblemRequestor(src);
		z = getWorkingCopy(
			"/P2/src/p/Z.java",
			src
		);
		assertProblems(
			"Unexpected problems value",
			"----------\n" +
			"1. WARNING in /P2/src/p/Z.java (at line 2)\n" +
			"	public class Z extends X1 {\n" +
			"	                       ^^\n" +
			"Discouraged access: The type \'X1\' is not API (restriction on required project \'P1\')\n" +
			"----------\n"
		);
	} finally {
		if (x1 != null)
			x1.discardWorkingCopy();
		if (z != null)
			z.discardWorkingCopy();
		deleteProjects(new String[] {"P1", "P2"});
	}
}

/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=76266
 * Ensures that a problem is created for a reference to a method of a type that is not
 * accessible in a prereq project, even though it is accessed through an intermediate
 * class that implements an interface that defines the same method, both the second
 * class and the interface being accessible.
 * The point here is that the existence of the accessible interface may imply that the
 * foo method be accessible through X2. By design, the lookup returns X1#foo though,
 * like it does for a press upon F3 in the interface, and hence the access restriction
 * gets triggered. Rule of thumb: if pressing F3 on a method or field directs the
 * interface to a definition within a restricted type, then the use of the said method
 * or field is restricted.
 */
public void test005() throws CoreException {
	ICompilationUnit x1 = null, i1 = null, x2 = null, y =  null;
	try {
		createJavaProject(
			"P1",
			new String[] {"src"},
			new String[] {"JCL_LIB"},
			"bin");
		this.problemRequestor = new ProblemRequestor();
		x1 = getWorkingCopy(
			"/P1/src/p/X1.java",
			"package p;\n" +
			"public class X1 {\n" +
			"	public void foo() {\n" +
			"	}\n" +
			"}"
		);
		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n"
		);
		i1 = getWorkingCopy(
			"/P1/src/q/I1.java",
			"package q;\n" +
			"interface I1 {\n" +
			"	void foo();\n" +
			"}"
		);
		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n"
		);
		x2 = getWorkingCopy(
			"/P1/src/q/X2.java",
			"package q;\n" +
			"public class X2 extends p.X1 {\n" +
			"	public void bar() {\n" +
			"	  foo();\n" +
			"	}\n" +
			"}"
		);
		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n"
		);
		IJavaProject p2 = createJavaProject(
			"P2",
			new String[] {"src"},
			new String[] {"JCL_LIB"},
			"bin");
		IClasspathEntry[] classpath = p2.getRawClasspath();
		int length = classpath.length;
		System.arraycopy(classpath, 0,
				classpath = new IClasspathEntry[length + 1], 0, length);
		classpath[length] = createSourceEntry("P2", "/P1", "-p/X1");
		p2.setRawClasspath(classpath, null);
		String src =
			"package r;\n" +
			"public class Y {\n" +
			"	void foobar() {\n" +
			"		(new q.X2()).foo(); // accesses p.X1#foo, should trigger an error\n" +
			"		(new q.X2()).bar(); // accesses q.X2#bar, OK\n" +
			"	}\n" +
			"}";
		this.problemRequestor = new ProblemRequestor(src);
		y = getWorkingCopy(
			"/P2/src/r/Y.java",
			src
		);
		assertProblems(
			"Unexpected problems value",
			"----------\n" +
			"1. ERROR in /P2/src/r/Y.java (at line 4)\n" +
			"	(new q.X2()).foo(); // accesses p.X1#foo, should trigger an error\n" +
			"	             ^^^\n" +
			"Access restriction: The method \'X1.foo()\' is not API (restriction on required project \'P1\')\n" +
			"----------\n"
		);
	} finally {
		if (x1 != null)
			x1.discardWorkingCopy();
		if (i1 != null)
			i1.discardWorkingCopy();
		if (x2 != null)
			x2.discardWorkingCopy();
		if (y != null)
			y.discardWorkingCopy();
		deleteProjects(new String[] {"P1", "P2"});
	}
}

/*
 * Missing access restriction violation error on generic type.
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=122995
 */
public void test006() throws CoreException {
	ICompilationUnit x = null, y =  null;
	try {
		IJavaProject p1 = createJavaProject(
			"P1",
			new String[] {"src"},
			new String[] {"JCL_LIB"},
			"bin");
		p1.setOption("org.eclipse.jdt.core.compiler.compliance", "1.5");
		p1.setOption("org.eclipse.jdt.core.compiler.source", "1.5");
		p1.setOption("org.eclipse.jdt.core.compiler.targetPlatform", "1.5");
		p1.setOption(JavaCore.COMPILER_PB_RAW_TYPE_REFERENCE, JavaCore.IGNORE);
		this.problemRequestor = new ProblemRequestor();
		x = getWorkingCopy(
			"/P1/src/p/X.java",
			"package p;\n" +
			"public class X<T> {\n" +
			"	T m;\n" +
			"}"
		);
		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n"
		);
		IJavaProject p2 = createJavaProject(
				"P2",
				new String[] {"src"},
				new String[] {"JCL_LIB"},
				"bin");
		p2.setOption("org.eclipse.jdt.core.compiler.compliance", "1.5");
		p2.setOption("org.eclipse.jdt.core.compiler.source", "1.5");
		p2.setOption("org.eclipse.jdt.core.compiler.targetPlatform", "1.5");
		p2.setOption(JavaCore.COMPILER_PB_RAW_TYPE_REFERENCE, JavaCore.IGNORE);
		IClasspathEntry[] classpath = p2.getRawClasspath();
		int length = classpath.length;
		System.arraycopy(classpath, 0, classpath = new IClasspathEntry[length+1], 0, length);
		classpath[length] = createSourceEntry("P2", "/P1", "-p/X");
		p2.setRawClasspath(classpath, null);
		String src =
			"package p;\n" +
			"public class Y {\n" +
			"	X x1;\n" +
			"	X<String> x2 = new X<String>();\n" +
			"}";
		this.problemRequestor = new ProblemRequestor(src);
		y = getWorkingCopy(
			"/P2/src/p/Y.java",
			src
		);
		assertProblems(
			"Unexpected problems value",
			"----------\n" +
			"1. ERROR in /P2/src/p/Y.java (at line 3)\n" +
			"	X x1;\n" +
			"	^\n" +
			"Access restriction: The type \'X<T>\' is not API (restriction on required project \'P1\')\n" +
			"----------\n" +
			"2. ERROR in /P2/src/p/Y.java (at line 4)\n" +
			"	X<String> x2 = new X<String>();\n" +
			"	^\n" +
			"Access restriction: The type \'X<String>\' is not API (restriction on required project \'P1\')\n" +
			"----------\n" +
			"3. ERROR in /P2/src/p/Y.java (at line 4)\n" +
			"	X<String> x2 = new X<String>();\n" +
			"	                   ^\n" +
			"Access restriction: The type \'X<String>\' is not API (restriction on required project \'P1\')\n" +
			"----------\n" +
			"4. ERROR in /P2/src/p/Y.java (at line 4)\n" +
			"	X<String> x2 = new X<String>();\n" +
			"	                   ^\n" +
			"Access restriction: The constructor \'X<String>()\' is not API (restriction on required project \'P1\')\n" +
			"----------\n");
	} finally {
		if (x != null) {
			x.discardWorkingCopy();
		}
		if (y != null) {
			y.discardWorkingCopy();
		}
		deleteProjects(new String[] {"P1", "P2"});
	}
}

/*
 * Missing access restriction violation error on generic type.
 * More complex type parameter - stretch the erasure.
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=122995
 */
public void test007() throws CoreException {
	ICompilationUnit x = null, y =  null;
	try {
		IJavaProject p1 = createJavaProject(
			"P1",
			new String[] {"src"},
			new String[] {"JCL_LIB"},
			"bin");
		p1.setOption("org.eclipse.jdt.core.compiler.compliance", "1.5");
		p1.setOption("org.eclipse.jdt.core.compiler.source", "1.5");
		p1.setOption("org.eclipse.jdt.core.compiler.targetPlatform", "1.5");
		p1.setOption(JavaCore.COMPILER_PB_RAW_TYPE_REFERENCE, JavaCore.IGNORE);
		this.problemRequestor = new ProblemRequestor();
		x = getWorkingCopy(
			"/P1/src/p/X.java",
			"package p;\n" +
			"public class X<T extends String> {\n" +
			"  T m;\n" +
			"  public X (T t) {\n" +
			"    this.m = t;\n" +
			"  }\n" +
			"}"
		);
		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n"
		);
		IJavaProject p2 = createJavaProject(
				"P2",
				new String[] {"src"},
				new String[] {"JCL_LIB"},
				"bin");
		p2.setOption("org.eclipse.jdt.core.compiler.compliance", "1.5");
		p2.setOption("org.eclipse.jdt.core.compiler.source", "1.5");
		p2.setOption("org.eclipse.jdt.core.compiler.targetPlatform", "1.5");
		p2.setOption(JavaCore.COMPILER_PB_RAW_TYPE_REFERENCE, JavaCore.IGNORE);
		IClasspathEntry[] classpath = p2.getRawClasspath();
		int length = classpath.length;
		System.arraycopy(classpath, 0, classpath = new IClasspathEntry[length+1], 0, length);
		classpath[length] = createSourceEntry("P2", "/P1", "-p/X");
		p2.setRawClasspath(classpath, null);
		String src =
			"package p;\n" +
			"public class Y {\n" +
			"	X x1;\n" +
			"	X<String> x2 = new X<String>(\"\");\n" +
			"}";
		this.problemRequestor = new ProblemRequestor(src);
		y = getWorkingCopy(
			"/P2/src/p/Y.java",
			src
		);
		assertProblems(
			"Unexpected problems value",
			"----------\n" +
			"1. ERROR in /P2/src/p/Y.java (at line 3)\n" +
			"	X x1;\n" +
			"	^\n" +
			"Access restriction: The type \'X<T>\' is not API (restriction on required project \'P1\')\n" +
			"----------\n" +
			"2. ERROR in /P2/src/p/Y.java (at line 4)\n" +
			"	X<String> x2 = new X<String>(\"\");\n" +
			"	^\n" +
			"Access restriction: The type \'X<String>\' is not API (restriction on required project \'P1\')\n" +
			"----------\n" +
			"3. ERROR in /P2/src/p/Y.java (at line 4)\n" +
			"	X<String> x2 = new X<String>(\"\");\n" +
			"	                   ^\n" +
			"Access restriction: The type \'X<String>\' is not API (restriction on required project \'P1\')\n" +
			"----------\n" +
			"4. ERROR in /P2/src/p/Y.java (at line 4)\n" +
			"	X<String> x2 = new X<String>(\"\");\n" +
			"	                   ^\n" +
			"Access restriction: The constructor \'X<String>(String)\' is not API (restriction on required project \'P1\')\n" +
			"----------\n");
	} finally {
		if (x != null) {
			x.discardWorkingCopy();
		}
		if (y != null) {
			y.discardWorkingCopy();
		}
		deleteProjects(new String[] {"P1", "P2"});
	}
}

/*
 * Missing access restriction violation error on generic type.
 * Method case.
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=122995
 */
public void test008() throws CoreException {
	ICompilationUnit x1 = null, x2 = null, y =  null;
	try {
		IJavaProject p1 = createJavaProject(
			"P1",
			new String[] {"src"},
			new String[] {"JCL_LIB"},
			"bin");
		p1.setOption("org.eclipse.jdt.core.compiler.compliance", "1.5");
		p1.setOption("org.eclipse.jdt.core.compiler.source", "1.5");
		p1.setOption("org.eclipse.jdt.core.compiler.targetPlatform", "1.5");
		p1.setOption(JavaCore.COMPILER_PB_RAW_TYPE_REFERENCE, JavaCore.IGNORE);
		this.problemRequestor = new ProblemRequestor();
		x1 = getWorkingCopy(
			"/P1/src/p/X1.java",
			"package p;\n" +
			"public class X1<T> {\n" +
			"	void foo() {\n" +
			"	}\n" +
			"}"
		);
		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n"
		);
		x2 = getWorkingCopy(
			"/P1/src/p/X2.java",
			"package p;\n" +
			"public class X2 extends X1 {\n" +
			"	void bar() {\n" +
			"	}\n" +
			"}"
		);
		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n"
		);
		IJavaProject p2 = createJavaProject("P2", new String[] {"src"},
				new String[] {"JCL_LIB"}, "bin");
		p2.setOption("org.eclipse.jdt.core.compiler.compliance", "1.5");
		p2.setOption("org.eclipse.jdt.core.compiler.source", "1.5");
		p2.setOption("org.eclipse.jdt.core.compiler.targetPlatform", "1.5");
		IClasspathEntry[] classpath = p2.getRawClasspath();
		int length = classpath.length;
		System.arraycopy(classpath, 0, classpath = new IClasspathEntry[length+1], 0, length);
		classpath[length] = createSourceEntry("P2", "/P1", "-p/X1");
		p2.setRawClasspath(classpath, null);
		String src =
			"package p;\n" +
			"public class Y extends X2 {\n" +
			"	void foobar() {\n" +
			"		foo(); // accesses X1.foo, should trigger an error\n" +
			"		bar(); // accesses X2.bar, OK\n" +
			"	}\n" +
			"}";
		this.problemRequestor = new ProblemRequestor(src);
		y = getWorkingCopy(
			"/P2/src/p/Y.java",
			src
		);
		assertProblems(
			"Unexpected problems value",
			"----------\n" +
			"1. ERROR in /P2/src/p/Y.java (at line 4)\n" +
			"	foo(); // accesses X1.foo, should trigger an error\n" +
			"	^^^\n" +
			"Access restriction: The method \'X1.foo()\' is not API (restriction on required project \'P1\')\n" +
			"----------\n"
		);
	} finally {
		if (x1 != null)
			x1.discardWorkingCopy();
		if (x2 != null)
			x2.discardWorkingCopy();
		if (y != null)
			y.discardWorkingCopy();
		deleteProjects(new String[] {"P1", "P2"});
	}
}

/*
 * Missing access restriction violation error on generic type.
 * Field case.
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=122995
 */
public void test009() throws CoreException {
	ICompilationUnit x1 = null, x2 = null, y =  null;
	try {
		IJavaProject p1 = createJavaProject(
			"P1",
			new String[] {"src"},
			new String[] {"JCL_LIB"},
			"bin");
		p1.setOption("org.eclipse.jdt.core.compiler.compliance", "1.5");
		p1.setOption("org.eclipse.jdt.core.compiler.source", "1.5");
		p1.setOption("org.eclipse.jdt.core.compiler.targetPlatform", "1.5");
		p1.setOption(JavaCore.COMPILER_PB_RAW_TYPE_REFERENCE, JavaCore.IGNORE);
		this.problemRequestor = new ProblemRequestor();
		x1 = getWorkingCopy(
			"/P1/src/p/X1.java",
			"package p;\n" +
			"public class X1<T> {\n" +
			"	int m1;\n" +
			"}"
		);
		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n"
		);
		x2 = getWorkingCopy(
			"/P1/src/p/X2.java",
			"package p;\n" +
			"public class X2 extends X1 {\n" +
			"	char m2;\n" +
			"}"
		);
		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n"
		);
		IJavaProject p2 = createJavaProject("P2", new String[] {"src"},
				new String[] {"JCL_LIB"}, "bin");
		p2.setOption("org.eclipse.jdt.core.compiler.compliance", "1.5");
		p2.setOption("org.eclipse.jdt.core.compiler.source", "1.5");
		p2.setOption("org.eclipse.jdt.core.compiler.targetPlatform", "1.5");
		IClasspathEntry[] classpath = p2.getRawClasspath();
		int length = classpath.length;
		System.arraycopy(classpath, 0, classpath = new IClasspathEntry[length+1], 0, length);
		classpath[length] = createSourceEntry("P2", "/P1", "-p/X1");
		p2.setRawClasspath(classpath, null);
		String src =
			"package p;\n" +
			"public class Y extends X2 {\n" +
			"	int foobar() {\n" +
			"		int l1 = m1; // accesses X1.m1, should trigger an error\n" +
			"		char l2 = m2; // accesses X2.m2, OK\n" +
			"		return l1 + l2;\n" +
			"	}\n" +
			"}";
		this.problemRequestor = new ProblemRequestor(src);
		y = getWorkingCopy(
			"/P2/src/p/Y.java",
			src
		);
		assertProblems(
			"Unexpected problems value",
			"----------\n" +
			"1. ERROR in /P2/src/p/Y.java (at line 4)\n" +
			"	int l1 = m1; // accesses X1.m1, should trigger an error\n" +
			"	         ^^\n" +
			"Access restriction: The field \'X1.m1\' is not API (restriction on required project \'P1\')\n" +
			"----------\n"
		);
	} finally {
		if (x1 != null)
			x1.discardWorkingCopy();
		if (x2 != null)
			x2.discardWorkingCopy();
		if (y != null)
			y.discardWorkingCopy();
		deleteProjects(new String[] {"P1", "P2"});
	}
}

/*
 * Missing access restriction violation error on generic type.
 * Inner type case.
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=122995
 */
public void test010() throws CoreException {
	ICompilationUnit x1 = null, x2 = null, y =  null;
	try {
		IJavaProject p1 = createJavaProject(
			"P1",
			new String[] {"src"},
			new String[] {"JCL_LIB"},
			"bin");
		p1.setOption("org.eclipse.jdt.core.compiler.compliance", "1.5");
		p1.setOption("org.eclipse.jdt.core.compiler.source", "1.5");
		p1.setOption("org.eclipse.jdt.core.compiler.targetPlatform", "1.5");
		p1.setOption(JavaCore.COMPILER_PB_RAW_TYPE_REFERENCE, JavaCore.IGNORE);
		this.problemRequestor = new ProblemRequestor();
		x1 = getWorkingCopy(
			"/P1/src/p/X1.java",
			"package p;\n" +
			"public class X1<T> {\n" +
			"	class C1 {\n" +
			"	   protected C1 (int dummy) {}\n" +
			"	   protected void foo() {}\n" +
			"	}\n" +
			"	interface I1 {}\n" +
			"}"
		);
		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n"
		);
		x2 = getWorkingCopy(
			"/P1/src/p/X2.java",
			"package p;\n" +
			"public class X2 extends X1 {\n" +
			"	class C2 {}\n" +
			"	interface I2 {}\n" +
			"}"
		);
		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n"
		);
		IJavaProject p2 = createJavaProject("P2", new String[] {"src"},
				new String[] {"JCL_LIB"}, "bin");
		p2.setOption("org.eclipse.jdt.core.compiler.compliance", "1.5");
		p2.setOption("org.eclipse.jdt.core.compiler.source", "1.5");
		p2.setOption("org.eclipse.jdt.core.compiler.targetPlatform", "1.5");
		p2.setOption(JavaCore.COMPILER_PB_RAW_TYPE_REFERENCE, JavaCore.IGNORE);
		IClasspathEntry[] classpath = p2.getRawClasspath();
		int length = classpath.length;
		System.arraycopy(classpath, 0, classpath = new IClasspathEntry[length+1], 0, length);
		classpath[length] = createSourceEntry("P2", "/P1", "-p/X1");
		p2.setRawClasspath(classpath, null);
		String src =
			"package p;\n" +
			"public class Y extends X2 {\n" +
			"	class C3a extends C1 {      // error\n" +
			"	   C3a() {\n" +
			"	      super(0);\n" +
			"	      foo();                // error\n" +
			"	   }\n" +
			"	}\n" +
			"	class C3c extends C2 implements I2 {}\n" +
			"	String foobar() {\n" +
			"		C1 m1 =                 // error\n" +
			"		        new C1(0);      // error\n" +
			"		C2 m2 = new C2();\n" +
			"		return m1 == null || m2 == null ? \"!OK\" : \"OK\";\n" +
			"	}\n" +
			"}";
		this.problemRequestor = new ProblemRequestor(src);
		y = getWorkingCopy(
			"/P2/src/p/Y.java",
			src
		);
		assertProblems(
			"Unexpected problems value",
			"----------\n" +
			"1. ERROR in /P2/src/p/Y.java (at line 3)\n" +
			"	class C3a extends C1 {      // error\n" +
			"	                  ^^\n" +
			"Access restriction: The type \'X1.C1\' is not API (restriction on required project \'P1\')\n" +
			"----------\n" +
			"2. ERROR in /P2/src/p/Y.java (at line 5)\n" +
			"	super(0);\n" +
			"	^^^^^^^^^\n" +
			"Access restriction: The constructor \'X1.C1(int)\' is not API (restriction on required project \'P1\')\n" +
			"----------\n" +
			"3. ERROR in /P2/src/p/Y.java (at line 6)\n" +
			"	foo();                // error\n" +
			"	^^^\n" +
			"Access restriction: The method \'X1.C1.foo()\' is not API (restriction on required project \'P1\')\n" +
			"----------\n" +
			"4. ERROR in /P2/src/p/Y.java (at line 11)\n" +
			"	C1 m1 =                 // error\n" +
			"	^^\n" +
			"Access restriction: The type \'X1.C1\' is not API (restriction on required project \'P1\')\n" +
			"----------\n" +
			"5. ERROR in /P2/src/p/Y.java (at line 12)\n" +
			"	new C1(0);      // error\n" +
			"	    ^^\n" +
			"Access restriction: The type \'X1.C1\' is not API (restriction on required project \'P1\')\n" +
			"----------\n" +
			"6. ERROR in /P2/src/p/Y.java (at line 12)\n" +
			"	new C1(0);      // error\n" +
			"	    ^^\n" +
			"Access restriction: The constructor \'X1.C1(int)\' is not API (restriction on required project \'P1\')\n" +
			"----------\n"
		);
	} finally {
		if (x1 != null)
			x1.discardWorkingCopy();
		if (x2 != null)
			x2.discardWorkingCopy();
		if (y != null)
			y.discardWorkingCopy();
		deleteProjects(new String[] {"P1", "P2"});
	}
}

/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=122885
 * Checking library messages.
 */
public void test011() throws CoreException {
	ICompilationUnit y = null;
	try {
		createJavaProject(
			"P1",
			new String[] {"src"},
			new String[] {"JCL_LIB", "/AccessRestrictions/lib.jar"},
			new String[][]{{}, {}},
			new String[][]{{}, {"**/*"}},
			null/*no project*/,
			null/*no inclusion pattern*/,
			null/*no exclusion pattern*/,
			null/*no exported project*/,
			"bin",
			null/*no source outputs*/,
			null/*no inclusion pattern*/,
			null/*no exclusion pattern*/,
			"1.4");
		this.problemRequestor = new ProblemRequestor();
		y = getWorkingCopy(
			"/P1/src/q/Y.java",
			"package q;\n" +
			"public class Y {\n" +
			"	void foo() {\n" +
			"     p.X x = new p.X();\n" +
			"     x.foo();\n" +
			"     if (x.m > 0) {}\n" +
			"	}\n" +
			"}"
		);
		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"1. ERROR in /P1/src/q/Y.java (at line 4)\n" +
			"	p.X x = new p.X();\n" +
			"	^^^\n" +
			"Access restriction: The type \'X\' is not API (restriction on required library \'AccessRestrictions/lib.jar\')\n" +
			"----------\n" +
			"2. ERROR in /P1/src/q/Y.java (at line 4)\n" +
			"	p.X x = new p.X();\n" +
			"	            ^^^\n" +
			"Access restriction: The type \'X\' is not API (restriction on required library \'AccessRestrictions/lib.jar\')\n" +
			"----------\n" +
			"3. ERROR in /P1/src/q/Y.java (at line 4)\n" +
			"	p.X x = new p.X();\n" +
			"	            ^^^\n" +
			"Access restriction: The constructor \'X()\' is not API (restriction on required library \'AccessRestrictions/lib.jar\')\n" +
			"----------\n" +
			"4. ERROR in /P1/src/q/Y.java (at line 5)\n" +
			"	x.foo();\n" +
			"	  ^^^\n" +
			"Access restriction: The method \'X.foo()\' is not API (restriction on required library \'AccessRestrictions/lib.jar\')\n" +
			"----------\n" +
			"5. ERROR in /P1/src/q/Y.java (at line 6)\n" +
			"	if (x.m > 0) {}\n" +
			"	      ^\n" +
			"Access restriction: The field \'X.m\' is not API (restriction on required library \'AccessRestrictions/lib.jar\')\n" +
			"----------\n"
		);
	} finally {
		if (y != null)
			y.discardWorkingCopy();
		deleteProject("P1");
	}
}
public void testBug545766() throws CoreException {
	ICompilationUnit x1 = null, z = null;
	try {
		createJavaProject(
			"P1",
			new String[] {"src"},
			new String[] {"JCL15_LIB"},
			"bin",
			"1.5");
		createFolder("/P1/src/p");
		createFile("/P1/src/p/X1.java",
			"package p;\n" +
			"public class X1 {\n" +
			"	public enum E {" +
			"		E1(), E2();" +
			"	}\n" +
			"}"
		);
		IJavaProject p2 = createJavaProject("P2", new String[] {"src"},
				new String[] {"JCL15_LIB"}, "bin", "1.5");
		IClasspathEntry[] classpath = p2.getRawClasspath();
		int length = classpath.length;
		System.arraycopy(classpath, 0, classpath = new IClasspathEntry[length+1], 0, length);
		classpath[length] = createSourceEntry("P2", "/P1", "-p/X1");
		p2.setRawClasspath(classpath, null);

		String src =
			"package p2;\n" +
			"import p.X1;\n" +
			"public class Z {\n" +
			"	X1.E e = X1.E.E1;" +
			"}";
		String expectedProblems =
				"1. ERROR in /P2/src/p2/Z.java (at line 2)\n" +
				"	import p.X1;\n" +
				"	       ^^^^\n" +
				"Access restriction: The type \'X1\' is not API (restriction on required project \'P1\')\n" +
				"----------\n" +
				"2. ERROR in /P2/src/p2/Z.java (at line 4)\n" +
				"	X1.E e = X1.E.E1;}\n" +
				"	^^^^\n" +
				"Access restriction: The type \'X1\' is not API (restriction on required project \'P1\')\n" +
				"----------\n" +
				"3. ERROR in /P2/src/p2/Z.java (at line 4)\n" +
				"	X1.E e = X1.E.E1;}\n" +
				"	^^^^\n" +
				"Access restriction: The type \'X1.E\' is not API (restriction on required project \'P1\')\n" +
				"----------\n" +
				"4. ERROR in /P2/src/p2/Z.java (at line 4)\n" +
				"	X1.E e = X1.E.E1;}\n" +
				"	         ^^^^^^^\n" +
				"Access restriction: The type \'X1\' is not API (restriction on required project \'P1\')\n" +
				"----------\n" +
				"5. ERROR in /P2/src/p2/Z.java (at line 4)\n" +
				"	X1.E e = X1.E.E1;}\n" +
				"	         ^^^^^^^\n" +
				"Access restriction: The type \'X1.E\' is not API (restriction on required project \'P1\')\n" +
				"----------\n" +
				"6. ERROR in /P2/src/p2/Z.java (at line 4)\n" +
				"	X1.E e = X1.E.E1;}\n" +
				"	              ^^\n" +
				"Access restriction: The field \'X1.E.E1\' is not API (restriction on required project \'P1\')\n" +
				"----------\n";
		this.problemRequestor = new ProblemRequestor(src);
		z = getWorkingCopy("/P2/src/p2/Z.java", src);
		assertProblems("Unexpected problems value", "----------\n" + expectedProblems);

		int start = src.indexOf("E1");
		IJavaElement[] elements = z.codeSelect(start, 2);
		assertElementsEqual("Unexpected elements", "E1 [in E [in X1 [in X1.java [in p [in src [in P1]]]]]]", elements);

		createFolder("/P2/src/p2");
		createFile("/P2/src/p2/Z.java", src);
		ASTParser parser = ASTParser.newParser(AST_INTERNAL_LATEST);
		parser.setProject(p2);
		parser.setSource((ITypeRoot)p2.findElement(new Path("p2/Z.java")));
		parser.setResolveBindings(true);
		ASTNode ast = parser.createAST(null); // <== NPE was thrown here
		assertProblems("unexpected problems",
				expectedProblems,
				((CompilationUnit) ast).getProblems(),
				src.toCharArray());
	} finally {
		if (x1 != null)
			x1.discardWorkingCopy();
		if (z != null)
			z.discardWorkingCopy();
		deleteProjects(new String[] {"P1", "P2"});
	}
}
}
