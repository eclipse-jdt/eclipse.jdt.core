/*******************************************************************************
 * Copyright (c) 2002 IBM Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.lang.reflect.Method;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.util.CompilationUnitSorter;
import org.eclipse.jdt.core.util.CompilationUnitSorter.DefaultJavaElementComparator;

/**
 * 
 * @since 2.1
 */
public class SortCompilationUnitElementsTests extends ModifyingResourceTests {

private static final boolean DEBUG = false;

public SortCompilationUnitElementsTests(String name) {
	super(name);
}
public void setUpSuite() throws Exception {
	super.setUpSuite();
	
	this.createJavaProject("P", new String[] {"src"}, new String[] {getExternalJCLPathString()}, "bin");
	this.createFolder("/P/src/p");
}
private void sortUnit(ICompilationUnit unit, String expectedResult) throws CoreException {
	this.sortUnit(unit, expectedResult, true);
}

private void sortUnit(ICompilationUnit unit, String expectedResult, boolean testPositions) throws CoreException {
	debug(unit, "BEFORE");
	if (testPositions) {
		char[] initialSource = unit.getSource().toCharArray();
		int[] positions = new int[initialSource.length];
		for (int i = 0; i < initialSource.length; i++) {
			positions[i] = i;
		}
		CompilationUnitSorter.sort(new ICompilationUnit[] { unit }, new int[][] { positions }, new DefaultJavaElementComparator(1,2,3,4,5,6,7,8,9), new NullProgressMonitor());
		String sortedSource = unit.getBuffer().getContents();
		assertEquals("Different output", sortedSource, expectedResult);
		for (int i = 0, max = positions.length; i < max; i++) {
			assertEquals("wrong mapped positions at " + i + " <-> " + positions[i], initialSource[i], expectedResult.charAt(positions[i]));
		}
	} else {
		CompilationUnitSorter.sort(new ICompilationUnit[] { unit }, null, new DefaultJavaElementComparator(1,2,3,4,5,6,7,8,9), new NullProgressMonitor());
		String sortedSource = unit.getBuffer().getContents();
		if (expectedResult == null || expectedResult.length() == 0) {
			System.out.println(sortedSource);
		} else {
			assertEquals("Different output", sortedSource, expectedResult);
		}
	}
	debug(unit, "AFTER");
}

private void debug(ICompilationUnit unit, String id) throws JavaModelException {
	String source = unit.getBuffer().getContents();
	if (DEBUG) {
		System.out.println("========================== " + id + " =============================="); //$NON-NLS-1$
		System.out.println(source);
		System.out.println("========================== " + id + " =============================="); //$NON-NLS-1$
	}
}

public static Test suite() {
	TestSuite suite = new Suite(SortCompilationUnitElementsTests.class.getName());

	if (true) {
		Class c = SortCompilationUnitElementsTests.class;
		Method[] methods = c.getMethods();
		for (int i = 0, max = methods.length; i < max; i++) {
			if (methods[i].getName().startsWith("test")) {
				suite.addTest(new SortCompilationUnitElementsTests(methods[i].getName()));
			}
		}
	} else {
		suite.addTest(new SortCompilationUnitElementsTests("test011"));
	}	
	return suite;
}
public void tearDownSuite() throws Exception {
	this.deleteProject("P");
	super.tearDownSuite();
}
/**
 * Calls methods that do nothing to ensure code coverage
 */
public void test001() throws CoreException {
	try {
		this.createFile(
			"/P/src/p/X.java",
			"/**\n" +
			" *\n" +
			" */\n" +
			"package p;\n" +
			"public class X {\n" +
			"	\n" +
			"	static class D {\n" +
			"		String toString() {\n" +
			"			return \"HELLO\";\n" +
			"		}\n" +
			"	}\n" +
			"	// start of static field declaration\n" +
			"\n" +
			"	static int i, j = 3, /*     */ k = 4;// end of static field declaration\n" +
			"	void bar(int i) {\n" +
			"	}\n" +
			"	void bar() {\n" +
			"		\n\n" +
			"		class E {\n" +
			"			void bar7() {\n" +
			"				System.out.println();\n" +
			"			}\n" +
			"			void bar9() {}\n" +
			"			void bar2() {}\n" +
			"		}\n" +
			"		Object o = new E();\n" +
			"		System.out.println(o);\n" +
			"		class C {\n" +
			"			void bar6() {}\n" +
			"			void bar4() {}\n" +
			"			void bar5() {}\n" +
			"		}\n" +
			"	}\n\n" +
			"	Object b1 = null, a1 = new Object() {\n" +
			"		void bar2() {\n" +
			"		}\n" +
			"		void bar() {\n" +
			"		}\n" +
			"		void bar4() {\n" +
			"			System.out.println();\n" +
			"		}\n" +
			"		void bar3() {\n" +
			"		}\n" +
			"	}, c1 = null; // end of multiple field declaration\n" +
			"	// end of class X\n" +
			"}\n" + 
			"// end of compilation unit\n"
		);
		String expectedResult = "/**\n" +			" *\n" +			" */\n" +			"package p;\n" +			"public class X {\n" +			"	\n" +			"	static class D {\n" +			"		String toString() {\n" +			"			return \"HELLO\";\n" +			"		}\n" +			"	}\n" +			"	// start of static field declaration\n" +			"\n" +			"	static int i, j = 3, /*     */ k = 4;// end of static field declaration\n" +			"\n" +			"	Object b1 = null, a1 = new Object() {\n" +			"		void bar() {\n" +			"		}\n" +			"		void bar2() {\n" +			"		}\n" +			"		void bar3() {\n" +			"		}\n" +			"		void bar4() {\n" +			"			System.out.println();\n" +			"		}\n" +			"	}, c1 = null; // end of multiple field declaration\n" +			"	void bar() {\n" +			"		\n" +			"\n" +			"		class E {\n" +			"			void bar2() {}\n" +			"			void bar7() {\n" +			"				System.out.println();\n" +			"			}\n" +			"			void bar9() {}\n" +			"		}\n" +			"		Object o = new E();\n" +			"		System.out.println(o);\n" +			"		class C {\n" +			"			void bar4() {}\n" +			"			void bar5() {}\n" +			"			void bar6() {}\n" +			"		}\n" +			"	}\n" +			"	void bar(int i) {\n" +			"	}\n" +			"	// end of class X\n" +			"}\n" +			"// end of compilation unit\n";
		sortUnit(this.getCompilationUnit("/P/src/p/X.java"), expectedResult);
	} finally {
		this.deleteFile("/P/src/p/X.java");
	}
}
public void test002() throws CoreException {
	try {
		this.createFile(
			"/P/src/p/X.java",
			"package p;\n" +
			"public class X {\n" +
			"	\n" +
			"	class D {\n" +
			"		String toString() {\n" +
			"			return \"HELLO\";\n" +
			"		}\n" +
			"	}\n" +
			"	int i, j, k;\n" +
			"	Object bar() {\n" +
			"		System.out.println();\n" +
			"		Object o = new Object() {    };\n" +
			"		System.out.println(o);\n" +
			"		class C {\n" +
			"			void bar6() {}\n" +
			"			void bar4() {}\n" +
			"			void bar5() {}\n" +
			"		}\n" +
			"		return new C();\n" +
			"	}\n" +
			"	Object bar3() {\n" +
			"		System.out.println();\n" +
			"		Object o = new Object() {        };\n" +
			"		System.out.println(o);\n" +
			"		return o;\n" +
			"	}\n" +
			"	Object a1 = new Object() { }, o1 = null;\n" +
			"}"
		);
		String expectedSource = "package p;\n" +			"public class X {\n" +			"	\n" +			"	class D {\n" +			"		String toString() {\n" +			"			return \"HELLO\";\n" +			"		}\n" +			"	}\n" +			"	Object a1 = new Object() { }, o1 = null;\n" +			"	int i, j, k;\n" +			"	Object bar() {\n" +			"		System.out.println();\n" +			"		Object o = new Object() {    };\n" +			"		System.out.println(o);\n" +			"		class C {\n" +			"			void bar4() {}\n" +			"			void bar5() {}\n" +			"			void bar6() {}\n" +			"		}\n" +			"		return new C();\n" +			"	}\n" +			"	Object bar3() {\n" +			"		System.out.println();\n" +			"		Object o = new Object() {        };\n" +			"		System.out.println(o);\n" +			"		return o;\n" +			"	}\n" +			"}";
		sortUnit(this.getCompilationUnit("/P/src/p/X.java"), expectedSource);
	} finally {
		this.deleteFile("/P/src/p/X.java");
	}
}
/**
 * Calls methods that do nothing to ensure code coverage
 */
public void test003() throws CoreException {
	try {
		this.createFile(
			"/P/src/p/X.java",
			"/**\n" +
			" *\n" +
			" */\n" +
			"package p;\n" +
			"public class X extends java.lang.Object implements java.util.Cloneable {\n" +
			"	\n" +
			"	class D {\n" +
			"		String toString() {\n" +
			"			return \"HELLO\";\n" +
			"		}\n" +
			"	}\n" +			"\n" +
			"	// start of field declaration\n" +
			"\n" +
			"	int i, j = 3, /*     */ k = 4;// end of field declaration\n" +
			"	void bar(final int i[]) {\n" +
			"	}\n" +
			"	void bar() {\n" +
			"		\n\n" +
			"		class E {\n" +
			"			void bar7() {\n" +
			"				System.out.println();\n" +
			"			}\n" +
			"			void bar9() {}\n" +
			"			void bar2() {}\n" +
			"		}\n" +
			"		Object o = new E();\n" +
			"		System.out.println(o);\n" +
			"		class C {\n" +
			"			void bar6() {}\n" +
			"			void bar4() {}\n" +
			"			void bar5() {}\n" +
			"		}\n" +
			"	}\n\n" +
			"	Object b1[] = null, a1 = new Object() {\n" +
			"		void bar2(int[] j) {\n" +
			"		}\n" +
			"		void bar() {\n" +
			"		}\n" +
			"		void bar4() {\n" +
			"			System.out.println();\n" +
			"		}\n" +
			"		void bar3() {\n" +
			"		}\n" +
			"	}, c1 = null; // end of multiple field declaration\n" +
			"	// end of class X\n" +
			"}\n" + 
			"// end of compilation unit\n"
		);
		String expectedResult = "/**\n" +			" *\n" +			" */\n" +			"package p;\n" +			"public class X extends java.lang.Object implements java.util.Cloneable {\n" +			"	\n" +			"	class D {\n" +			"		String toString() {\n" +			"			return \"HELLO\";\n" +			"		}\n" +			"	}\n" +			"\n" +			"	Object b1[] = null, a1 = new Object() {\n" +			"		void bar() {\n" +			"		}\n" +			"		void bar2(int[] j) {\n" +			"		}\n" +			"		void bar3() {\n" +			"		}\n" +			"		void bar4() {\n" +			"			System.out.println();\n" +			"		}\n" +			"	}, c1 = null; // end of multiple field declaration\n" +			"\n" +			"	// start of field declaration\n" +			"\n" +			"	int i, j = 3, /*     */ k = 4;// end of field declaration\n" +			"	void bar() {\n" +			"		\n" +			"\n" +			"		class E {\n" +			"			void bar2() {}\n" +			"			void bar7() {\n" +			"				System.out.println();\n" +			"			}\n" +			"			void bar9() {}\n" +			"		}\n" +			"		Object o = new E();\n" +			"		System.out.println(o);\n" +			"		class C {\n" +			"			void bar4() {}\n" +			"			void bar5() {}\n" +			"			void bar6() {}\n" +			"		}\n" +			"	}\n" +			"	void bar(final int i[]) {\n" +			"	}\n" +			"	// end of class X\n" +			"}\n" +			"// end of compilation unit\n";
		sortUnit(this.getCompilationUnit("/P/src/p/X.java"), expectedResult);
	} finally {
		this.deleteFile("/P/src/p/X.java");
	}
}
/**
 * Calls methods that do nothing to ensure code coverage
 */
public void test004() throws CoreException {
	try {
		this.createFile(
			"/P/src/p/X.java",
			"/**\n" +
			" *\n" +
			" */\n" +
			"package p;\n" +
			"public class X extends java.lang.Object implements java.util.Cloneable {\n" +
			"	\n" +
			"	class D {\n" +
			"		String toString() {\n" +
			"			return \"HELLO\";\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"	// start of method declaration\n" +
			"\n" +
			"	void bar(final int i[]) {\n" +
			"	}\n" +
			"	void bar() {\n" +
			"		\n\n" +
			"		class E {\n" +
			"			Object bar7() {\n" +
			"				return new Object() {\n" +
			"					void bar9() {}\n" +
			"					void bar2() {}\n" +
			"				};\n" +
			"			}\n" +
			"			void bar9() {}\n" +
			"			void bar2() {}\n" +
			"		}\n" +
			"		Object o = new E();\n" +
			"		System.out.println(o);\n" +
			"		class C {\n" +
			"			void bar6() {}\n" +
			"			void bar4() {}\n" +
			"			void bar5() {}\n" +
			"		}\n" +
			"	}\n\n" +
			"	// end of class X\n" +
			"}\n" + 
			"// end of compilation unit\n"
		);
		String expectedResult = "/**\n" +			" *\n" +			" */\n" +			"package p;\n" +			"public class X extends java.lang.Object implements java.util.Cloneable {\n" +			"	\n" +			"	class D {\n" +			"		String toString() {\n" +			"			return \"HELLO\";\n" +			"		}\n" +			"	}\n" +			"	void bar() {\n" +			"		\n" +			"\n" +			"		class E {\n" +			"			void bar2() {}\n" +			"			Object bar7() {\n" +			"				return new Object() {\n" +			"					void bar2() {}\n" +			"					void bar9() {}\n" +			"				};\n" +			"			}\n" +			"			void bar9() {}\n" +			"		}\n" +			"		Object o = new E();\n" +			"		System.out.println(o);\n" +			"		class C {\n" +			"			void bar4() {}\n" +			"			void bar5() {}\n" +			"			void bar6() {}\n" +			"		}\n" +			"	}\n" +			"\n" +			"	// start of method declaration\n" +			"\n" +			"	void bar(final int i[]) {\n" +			"	}\n" +			"\n" +			"	// end of class X\n" +			"}\n" +			"// end of compilation unit\n";
		sortUnit(this.getCompilationUnit("/P/src/p/X.java"), expectedResult);
	} finally {
		this.deleteFile("/P/src/p/X.java");
	}
}
public void test005() throws CoreException {
	try {
		this.createFile(
			"/P/src/p/X.java",
			"package p;\n" +
			"public class X {\n" +
			"	Object bar3() {\n" +
			"		System.out.println();\n" +
			"		Object o = new Object() {        };\n" +
			"		System.out.println(o);\n" +
			"		return o;\n" +
			"	}\n" +
			"}"
		);
		String expectedResult = "package p;\n" +			"public class X {\n" +			"	Object bar3() {\n" +			"		System.out.println();\n" +			"		Object o = new Object() {        };\n" +			"		System.out.println(o);\n" +			"		return o;\n" +			"	}\n" +			"}";
		sortUnit(this.getCompilationUnit("/P/src/p/X.java"), expectedResult);
	} finally {
		this.deleteFile("/P/src/p/X.java");
	}
}
public void test006() throws CoreException {
	try {
		this.createFile(
			"/P/src/p/X.java",
			"package p;\n" +
			"public class X {\n" +
			"	Object bar3() {\n" +
			"		System.out.println();\n" +
			"		return new Object() {\n" +
			"			public void bar6() {}\n" +
			"			void bar4() throws IOException, Exception, NullPointerException {}\n" +
			"			void bar5() {}\n" +
			"       };\n" +
			"	}\n" +
			"}"
		);
		String expectedResult = "package p;\n" +
			"public class X {\n" +
			"	Object bar3() {\n" +
			"		System.out.println();\n" +
			"		return new Object() {\n" +
			"			void bar4() throws IOException, Exception, NullPointerException {}\n" +
			"			void bar5() {}\n" +
			"			public void bar6() {}\n" +
			"       };\n" +
			"	}\n" +
			"}";
		sortUnit(this.getCompilationUnit("/P/src/p/X.java"), expectedResult);
	} finally {
		this.deleteFile("/P/src/p/X.java");
	}
}
public void test007() throws CoreException {
	try {
		this.createFile(
			"/P/src/p/X.java",
			"package p;\n" +
			"public class X {\n" +
			"	Object bar3() {\n" +
			"		System.out.println();\n" +
			"		return new Object() {\n" +
			"			public static void bar6() {}\n" +
			"			void bar5() {}\n" +
			"			void bar4() throws IOException, Exception, NullPointerException {}\n" +
			"       };\n" +
			"	}\n" +
			"}"
		);
		String expectedResult = "package p;\n" +
			"public class X {\n" +
			"	Object bar3() {\n" +
			"		System.out.println();\n" +
			"		return new Object() {\n" +
			"			public static void bar6() {}\n" +
			"			void bar4() throws IOException, Exception, NullPointerException {}\n" +
			"			void bar5() {}\n" +
			"       };\n" +
			"	}\n" +
			"}";
		sortUnit(this.getCompilationUnit("/P/src/p/X.java"), expectedResult);
	} finally {
		this.deleteFile("/P/src/p/X.java");
	}
}

public void test008() throws CoreException {
	try {
		this.createFile(
			"/P/src/p/X.java",
			"package p;\n" +
			"public class X {\n" +
			"	Object bar3() {\n" +
			"		System.out.println();\n" +
			"		return new Object() {\n" +
			"			public static void bar6() {}\n" +
			"			void bar5() {}\n" +
			"			void bar4() throws IOException, Exception, NullPointerException {}\n" +
			"       };\n" +
			"	}\n" +
			"}"
		);
		String expectedResult = "package p;\n" +
			"public class X {\n" +
			"	Object bar3() {\n" +
			"		System.out.println();\n" +
			"		return new Object() {\n" +
			"			public static void bar6() {}\n" +
			"			void bar4() throws IOException, Exception, NullPointerException {}\n" +
			"			void bar5() {}\n" +
			"       };\n" +
			"	}\n" +
			"}";
		sortUnit(this.getCompilationUnit("/P/src/p/X.java"), expectedResult);
	} finally {
		this.deleteFile("/P/src/p/X.java");
	}
}
/**
 * Calls methods that do nothing to ensure code coverage
 */
public void test009() throws CoreException {
	try {
		this.createFile(
			"/P/src/p/X.java",
			"/**\n" +
			" *\n" +
			" */\n" +
			"package p;\n" +
			"public class X {\n" +
			"	\n" +
			"	static class D {\n" +
			"		String toString() {\n" +
			"			return \"HELLO\";\n" +
			"		}\n" +
			"	}\n" +
			"	// start of static field declaration\n" +
			"\n" +
			"	static int i, j = 3, /*     */ k = 4;// end of static field declaration\n" +
			"	void bar(int i) {\n" +
			"	}\n" +
			"	void bar() {\n" +
			"		\n\n" +
			"		class E {\n" +
			"			void bar7() {\n" +
			"				System.out.println();\n" +
			"			}\n" +
			"			void bar9() {}\n" +
			"			void bar2() {}\n" +
			"		}\n" +
			"		Object o = new E();\n" +
			"		System.out.println(o);\n" +
			"		class C {\n" +
			"			void bar6() {}\n" +
			"			void bar4() {}\n" +
			"			void bar5() {}\n" +
			"		}\n" +
			"	}\n\n" +
			"	Object b1 = null, a1 = new Object() {\n" +
			"		void bar2() {\n" +
			"		}\n" +
			"		void bar() {\n" +
			"		}\n" +
			"		void bar4() {\n" +
			"			System.out.println();\n" +
			"		}\n" +
			"		void bar3() {\n" +
			"		}\n" +
			"	}, c1 = null; // end of multiple field declaration\n" +
			"	// end of class X\n" +
			"}\n" + 
			"// end of compilation unit\n"
		);
		String expectedResult = "/**\n" +
			" *\n" +
			" */\n" +
			"package p;\n" +
			"public class X {\n" +
			"	\n" +
			"	static class D {\n" +
			"		String toString() {\n" +
			"			return \"HELLO\";\n" +
			"		}\n" +
			"	}\n" +
			"	// start of static field declaration\n" +
			"\n" +
			"	static int i, j = 3, /*     */ k = 4;// end of static field declaration\n" +
			"\n" +
			"	Object b1 = null, a1 = new Object() {\n" +
			"		void bar() {\n" +
			"		}\n" +
			"		void bar2() {\n" +
			"		}\n" +
			"		void bar3() {\n" +
			"		}\n" +
			"		void bar4() {\n" +
			"			System.out.println();\n" +
			"		}\n" +
			"	}, c1 = null; // end of multiple field declaration\n" +
			"	void bar() {\n" +
			"		\n" +
			"\n" +
			"		class E {\n" +
			"			void bar2() {}\n" +
			"			void bar7() {\n" +
			"				System.out.println();\n" +
			"			}\n" +
			"			void bar9() {}\n" +
			"		}\n" +
			"		Object o = new E();\n" +
			"		System.out.println(o);\n" +
			"		class C {\n" +
			"			void bar4() {}\n" +
			"			void bar5() {}\n" +
			"			void bar6() {}\n" +
			"		}\n" +
			"	}\n" +
			"	void bar(int i) {\n" +
			"	}\n" +
			"	// end of class X\n" +
			"}\n" +
			"// end of compilation unit\n";
		ICompilationUnit unit = this.getCompilationUnit("/P/src/p/X.java");
/*		int[] positions = new int[] { 529 };
		int[] expectedPositions = new int[] { 288 };
		sortUnit(unit, positions, expectedResult, expectedPositions);*/
		sortUnit(unit, expectedResult, false);		
	} finally {
		this.deleteFile("/P/src/p/X.java");
	}
}
public void test010() throws CoreException {
	try {
		this.createFile(
			"/P/src/SuperReference.java",
			"public class SuperReference extends ThisReference {\r\n" +			"	\r\n" +			"public SuperReference(int sourceStart, int sourceEnd) {\r\n" +			"	super(sourceStart, sourceEnd);\r\n" +			"}\r\n" +			"public static ExplicitConstructorCall implicitSuperConstructorCall() {\r\n" +			"	return new ExplicitConstructorCall(ExplicitConstructorCall.ImplicitSuper);\r\n" +			"}\r\n" +			"public boolean isImplicitThis() {\r\n" +			"	\r\n" +			"	return false;\r\n" +			"}\r\n" +			"public boolean isSuper() {\r\n" +			"	\r\n" +			"	return true;\r\n" +			"}\r\n" +			"public boolean isThis() {\r\n" +			"	\r\n" +			"	return false ;\r\n" +			"}\r\n" +			"public TypeBinding resolveType(BlockScope scope) {\r\n" +			"	constant = NotAConstant;\r\n" +			"	if (!checkAccess(scope.methodScope()))\r\n" +			"		return null;\r\n" +			"	SourceTypeBinding enclosingTb = scope.enclosingSourceType();\r\n" +			"	if (scope.isJavaLangObject(enclosingTb)) {\r\n" +			"		scope.problemReporter().cannotUseSuperInJavaLangObject(this);\r\n" +			"		return null;\r\n" +			"	}\r\n" +			"	return this.resolvedType = enclosingTb.superclass;\r\n" +			"}\r\n" +			"public String toStringExpression(){\r\n" +			"\r\n" +			"	return \"super\"; //$NON-NLS-1$\r\n" +			"	\r\n" +			"}\r\n" +			"public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope blockScope) {\r\n" +			"	visitor.visit(this, blockScope);\r\n" +			"	visitor.endVisit(this, blockScope);\r\n" +			"}\r\n" +			"}"
		);
		String expectedResult = "public class SuperReference extends ThisReference {\r\n" +			"public static ExplicitConstructorCall implicitSuperConstructorCall() {\r\n" +			"	return new ExplicitConstructorCall(ExplicitConstructorCall.ImplicitSuper);\r\n" +			"}\r\n" +			"	\r\n" +			"public SuperReference(int sourceStart, int sourceEnd) {\r\n" +			"	super(sourceStart, sourceEnd);\r\n" +			"}\r\n" +			"public boolean isImplicitThis() {\r\n" +			"	\r\n" +			"	return false;\r\n" +			"}\r\n" +			"public boolean isSuper() {\r\n" +			"	\r\n" +			"	return true;\r\n" +			"}\r\n" +			"public boolean isThis() {\r\n" +			"	\r\n" +			"	return false ;\r\n" +			"}\r\n" +			"public TypeBinding resolveType(BlockScope scope) {\r\n" +			"	constant = NotAConstant;\r\n" +			"	if (!checkAccess(scope.methodScope()))\r\n" +			"		return null;\r\n" +			"	SourceTypeBinding enclosingTb = scope.enclosingSourceType();\r\n" +			"	if (scope.isJavaLangObject(enclosingTb)) {\r\n" +			"		scope.problemReporter().cannotUseSuperInJavaLangObject(this);\r\n" +			"		return null;\r\n" +			"	}\r\n" +			"	return this.resolvedType = enclosingTb.superclass;\r\n" +			"}\r\n" +			"public String toStringExpression(){\r\n" +			"\r\n" +			"	return \"super\"; //$NON-NLS-1$\r\n" +			"	\r\n" +			"}\r\n" +			"public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope blockScope) {\r\n" +			"	visitor.visit(this, blockScope);\r\n" +			"	visitor.endVisit(this, blockScope);\r\n" +			"}\r\n" +			"}";
		sortUnit(this.getCompilationUnit("/P/src/SuperReference.java"), expectedResult);
	} finally {
		this.deleteFile("/P/src/SuperReference.java");
	}
}
public void test011() throws CoreException {
	try {
		this.createFile(
			"/P/src/p/BaseTypes.java",
			"/*******************************************************************************\r\n" +			" * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.\r\n" +			" * All rights reserved. This program and the accompanying materials \r\n" +			" * are made available under the terms of the Common Public License v0.5 \r\n" +			" * which accompanies this distribution, and is available at\r\n" +			" * http://www.eclipse.org/legal/cpl-v05.html\r\n" +			" * \r\n" +			" * Contributors:\r\n" +			" *     IBM Corporation - initial API and implementation\r\n" +			" ******************************************************************************/\r\n" +			"package p;\r\n" +			"\r\n" +			"public interface BaseTypes {\r\n" +			"	final BaseTypeBinding IntBinding = new BaseTypeBinding(TypeIds.T_int, \"int\".toCharArray(), new char[] {'I'}); //$NON-NLS-1$\r\n" +			"	final BaseTypeBinding ByteBinding = new BaseTypeBinding(TypeIds.T_byte, \"byte\".toCharArray(), new char[] {'B'}); //$NON-NLS-1$\r\n" +			"	final BaseTypeBinding ShortBinding = new BaseTypeBinding(TypeIds.T_short, \"short\".toCharArray(), new char[] {'S'}); //$NON-NLS-1$\r\n" +			"	final BaseTypeBinding CharBinding = new BaseTypeBinding(TypeIds.T_char, \"char\".toCharArray(), new char[] {'C'}); //$NON-NLS-1$\r\n" +			"	final BaseTypeBinding LongBinding = new BaseTypeBinding(TypeIds.T_long, \"long\".toCharArray(), new char[] {'J'}); //$NON-NLS-1$\r\n" +			"	final BaseTypeBinding FloatBinding = new BaseTypeBinding(TypeIds.T_float, \"float\".toCharArray(), new char[] {'F'}); //$NON-NLS-1$\r\n" +			"	final BaseTypeBinding DoubleBinding = new BaseTypeBinding(TypeIds.T_double, \"double\".toCharArray(), new char[] {'D'}); //$NON-NLS-1$\r\n" +			"	final BaseTypeBinding BooleanBinding = new BaseTypeBinding(TypeIds.T_boolean, \"boolean\".toCharArray(), new char[] {'Z'}); //$NON-NLS-1$\r\n" +			"	final BaseTypeBinding NullBinding = new BaseTypeBinding(TypeIds.T_null, \"null\".toCharArray(), new char[] {'N'}); //N stands for null even if it is never internally used //$NON-NLS-1$\r\n" +			"	final BaseTypeBinding VoidBinding = new BaseTypeBinding(TypeIds.T_void, \"void\".toCharArray(), new char[] {'V'}); //$NON-NLS-1$\r\n" +			"}\r\n"
		);
		String expectedResult = "/*******************************************************************************\r\n" +			" * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.\r\n" +			" * All rights reserved. This program and the accompanying materials \r\n" +			" * are made available under the terms of the Common Public License v0.5 \r\n" +			" * which accompanies this distribution, and is available at\r\n" +			" * http://www.eclipse.org/legal/cpl-v05.html\r\n" +			" * \r\n" +			" * Contributors:\r\n" +			" *     IBM Corporation - initial API and implementation\r\n" +			" ******************************************************************************/\r\n" +			"package p;\r\n" +			"\r\n" +			"public interface BaseTypes {\r\n" +			"	final BaseTypeBinding BooleanBinding = new BaseTypeBinding(TypeIds.T_boolean, \"boolean\".toCharArray(), new char[] {'Z'}); //$NON-NLS-1$\r\n" +			"	final BaseTypeBinding ByteBinding = new BaseTypeBinding(TypeIds.T_byte, \"byte\".toCharArray(), new char[] {'B'}); //$NON-NLS-1$\r\n" +			"	final BaseTypeBinding CharBinding = new BaseTypeBinding(TypeIds.T_char, \"char\".toCharArray(), new char[] {'C'}); //$NON-NLS-1$\r\n" +			"	final BaseTypeBinding DoubleBinding = new BaseTypeBinding(TypeIds.T_double, \"double\".toCharArray(), new char[] {'D'}); //$NON-NLS-1$\r\n" +			"	final BaseTypeBinding FloatBinding = new BaseTypeBinding(TypeIds.T_float, \"float\".toCharArray(), new char[] {'F'}); //$NON-NLS-1$\r\n" +			"	final BaseTypeBinding IntBinding = new BaseTypeBinding(TypeIds.T_int, \"int\".toCharArray(), new char[] {'I'}); //$NON-NLS-1$\r\n" +			"	final BaseTypeBinding LongBinding = new BaseTypeBinding(TypeIds.T_long, \"long\".toCharArray(), new char[] {'J'}); //$NON-NLS-1$\r\n" +			"	final BaseTypeBinding NullBinding = new BaseTypeBinding(TypeIds.T_null, \"null\".toCharArray(), new char[] {'N'}); //N stands for null even if it is never internally used //$NON-NLS-1$\r\n" +			"	final BaseTypeBinding ShortBinding = new BaseTypeBinding(TypeIds.T_short, \"short\".toCharArray(), new char[] {'S'}); //$NON-NLS-1$\r\n" +			"	final BaseTypeBinding VoidBinding = new BaseTypeBinding(TypeIds.T_void, \"void\".toCharArray(), new char[] {'V'}); //$NON-NLS-1$\r\n" +			"}\r\n";
		sortUnit(this.getCompilationUnit("/P/src/p/BaseTypes.java"), expectedResult);
	} finally {
		this.deleteFile("/P/src/p/BaseTypes.java");
	}
}

}
