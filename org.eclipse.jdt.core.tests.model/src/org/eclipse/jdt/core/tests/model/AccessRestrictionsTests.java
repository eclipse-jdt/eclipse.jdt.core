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


import junit.framework.Test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.IProblem;

public class AccessRestrictionsTests extends ModifyingResourceTests {
	static class ProblemRequestor extends AbstractJavaModelTests.ProblemRequestor {
		ProblemRequestor (String source) {
			if (source != null) 
				unitSource = source.toCharArray();
		}
		ProblemRequestor() {
		}
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
		return buildTestSuite(AccessRestrictionsTests.class);
	}

	protected void assertProblems(String message, String expected) {
		assertProblems(message, expected, this.problemRequestor);
	}

/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=76266
 * Ensures that a problem is created for a reference to a member of a type that is not
 * accessible in a prereq project, even though it is accessed through an intermediate 
 * accessible mother class.
 * Checking methods.
 */
public void test001() throws CoreException {
	ICompilationUnit x1 = null, x2 = null, y =  null, z = null;
	try {
		WorkingCopyOwner owner = new WorkingCopyOwner(){};
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
			"}",
			owner,
			this.problemRequestor);	
		assertProblems(
			"Unexpected problems", 
			"----------\n" + 
			"----------\n"
		);
		this.problemRequestor = new ProblemRequestor();
		x2 = getWorkingCopy(			
			"/P1/src/p/X2.java",
			"package p;\n" +
			"public class X2 extends X1 {\n" +
			"	void bar() {\n" +
			"	}\n" +
			"}",			owner,
			this.problemRequestor);
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
			src,
			owner,
			this.problemRequestor);
		assertProblems(
			"Unexpected problems value", 
			"----------\n" + 
			"1. ERROR in /P2/src/p/Z.java (at line 2)\n" + 
			"	public class Z extends X1 {\n" + 
			"	                       ^^\n" + 
			"Access restriction: The type X1 is not accessible due to restriction on required project P1\n" + 
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
			src,
			owner,
			this.problemRequestor);
		assertProblems(
			"Unexpected problems value", 
			"----------\n" + 
			"1. ERROR in /P2/src/p/Y.java (at line 4)\n" + 
			"	foo(); // accesses X1.foo, should trigger an error\n" + 
			"	^^^^^\n" + 
			"Access restriction: The method foo() from the type X1 is not accessible due to restriction on required project P1\n" + 
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
 * Ensures that a problem is created for a reference to a member of a type that is not
 * accessible in a prereq project, even though it is accessed through an intermediate 
 * accessible mother class.
 * Checking members.
 */
public void test002() throws CoreException {
	ICompilationUnit x1 = null, x2 = null, y =  null, z = null;
	try {
		WorkingCopyOwner owner = new WorkingCopyOwner(){};
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
			"}",
			owner,
			this.problemRequestor);	
		assertProblems(
			"Unexpected problems", 
			"----------\n" + 
			"----------\n"
		);
		this.problemRequestor = new ProblemRequestor();
		x2 = getWorkingCopy(			
			"/P1/src/p/X2.java",
			"package p;\n" +
			"public class X2 extends X1 {\n" +
			"	char m2;\n" +
			"}",
			owner,
			this.problemRequestor);
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
			src,
			owner,
			this.problemRequestor);
		assertProblems(
			"Unexpected problems value", 
			"----------\n" + 
			"1. ERROR in /P2/src/p/Z.java (at line 2)\n" + 
			"	public class Z extends X1 {\n" + 
			"	                       ^^\n" + 
			"Access restriction: The type X1 is not accessible due to restriction on required project P1\n" + 
			"----------\n"
		);
		// check the specifics of this test case
		src =
			"package p;\n" +
			"public class Y extends X2 {\n" +
			"	void foobar() {\n" +
			"		int l1 = m1; // accesses X1.m1, should trigger an error\n" +
			"		char l2 = m2; // accesses X2.m2, OK\n" +
			"	}\n" +
			"}";
		this.problemRequestor = new ProblemRequestor(src);
		y = getWorkingCopy(			
			"/P2/src/p/Y.java", 
			src,
			owner,
			this.problemRequestor);
		assertProblems(
			"Unexpected problems value", 
			"----------\n" + 
			"1. ERROR in /P2/src/p/Y.java (at line 4)\n" + 
			"	int l1 = m1; // accesses X1.m1, should trigger an error\n" + 
			"	         ^^\n" + 
			"Access restriction: The field m1 from the type X1 is not accessible due to restriction on required project P1\n" + 
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
 * Ensures that a problem is created for a reference to a member of a type that is not
 * accessible in a prereq project, even though it is accessed through an intermediate 
 * accessible mother class.
 * Checking member types.
 */
public void test003() throws CoreException {
	ICompilationUnit x1 = null, x2 = null, y =  null, z = null;
	try {
		WorkingCopyOwner owner = new WorkingCopyOwner(){};
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
			"}",
			owner,
			this.problemRequestor);	
		assertProblems(
			"Unexpected problems", 
			"----------\n" + 
			"----------\n"
		);
		this.problemRequestor = new ProblemRequestor();
		x2 = getWorkingCopy(			
			"/P1/src/p/X2.java",
			"package p;\n" +
			"public class X2 extends X1 {\n" +
			"	class C2 {}\n" +
			"	interface I2 {}\n" +
			"}",
			owner,
			this.problemRequestor);
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
			src,
			owner,
			this.problemRequestor);
		assertProblems(
			"Unexpected problems value", 
			"----------\n" + 
			"1. ERROR in /P2/src/p/Z.java (at line 2)\n" + 
			"	public class Z extends X1 {\n" + 
			"	                       ^^\n" + 
			"Access restriction: The type X1 is not accessible due to restriction on required project P1\n" + 
			"----------\n"
		);
		// check the specifics of this test case
		src =
			"package p;\n" +
			"public class Y extends X2 {\n" +
			"	class C3a extends C1 {      // error\n" +
			"	   C3a() {\n" +
			"	      super(0);\n" +
			"	      foo();                // error\n" +
			"	   }\n" +
			"	}\n" +
			"	class C3c extends C2 implements I2 {}\n" +
			"	void foobar() {\n" +
			"		C1 m1 =                 // error\n" +
			"		        new C1(0);      // error\n" +
			"		C2 m2 = new C2();\n" +
			"	}\n" +
			"}";
		this.problemRequestor = new ProblemRequestor(src);
		y = getWorkingCopy(			
			"/P2/src/p/Y.java", 
			src,
			owner,
			this.problemRequestor);
		assertProblems(
			"Unexpected problems value", 
			"----------\n" + 
			"1. ERROR in /P2/src/p/Y.java (at line 3)\n" + 
			"	class C3a extends C1 {      // error\n" + 
			"	                  ^^\n" + 
			"Access restriction: The type X1.C1 is not accessible due to restriction on required project P1\n" + 
			"----------\n" + 
			"2. ERROR in /P2/src/p/Y.java (at line 5)\n" + 
			"	super(0);\n" + 
			"	^^^^^^^^\n" + 
			"Access restriction: The constructor X1.C1(int) is not accessible due to restriction on required project P1\n" + 
			"----------\n" + 
			"3. ERROR in /P2/src/p/Y.java (at line 6)\n" + 
			"	foo();                // error\n" + 
			"	^^^^^\n" + 
			"Access restriction: The method foo() from the type X1.C1 is not accessible due to restriction on required project P1\n" + 
			"----------\n" + 
			"4. ERROR in /P2/src/p/Y.java (at line 11)\n" + 
			"	C1 m1 =                 // error\n" + 
			"	^^\n" + 
			"Access restriction: The type X1.C1 is not accessible due to restriction on required project P1\n" + 
			"----------\n" + 
			"5. ERROR in /P2/src/p/Y.java (at line 12)\n" + 
			"	new C1(0);      // error\n" + 
			"	^^^^^^^^^\n" + 
			"Access restriction: The constructor X1.C1(int) is not accessible due to restriction on required project P1\n" + 
			"----------\n" + 
			"6. ERROR in /P2/src/p/Y.java (at line 12)\n" + 
			"	new C1(0);      // error\n" + 
			"	    ^^\n" + 
			"Access restriction: The type X1.C1 is not accessible due to restriction on required project P1\n" + 
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
 * Discouraged access message - type via discouraged rule.
 */
public void test004() throws CoreException {
	ICompilationUnit x1 = null, z = null;
	try {
		WorkingCopyOwner owner = new WorkingCopyOwner(){};
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
			"}",
			owner,
			this.problemRequestor);	
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
			src,
			owner,
			this.problemRequestor);
		assertProblems(
			"Unexpected problems value", 
			"----------\n" + 
			"1. WARNING in /P2/src/p/Z.java (at line 2)\n" + 
			"	public class Z extends X1 {\n" + 
			"	                       ^^\n" + 
			"Discouraged access: The type X1 is not accessible due to restriction on required project P1\n" + 
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
}
