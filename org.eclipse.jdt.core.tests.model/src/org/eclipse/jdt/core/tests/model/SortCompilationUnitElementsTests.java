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
import org.eclipse.jdt.core.util.JavaCompilationUnitSorter;

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
	debug(unit, "BEFORE");
	JavaCompilationUnitSorter.sort(new ICompilationUnit[] { unit }, new DefaultJavaElementComparator(), new NullProgressMonitor());
	String sortedSource = unit.getBuffer().getContents();
	assertEquals("Different output", sortedSource, expectedResult);
	JavaCompilationUnitSorter.sort(new ICompilationUnit[] { unit }, new DefaultJavaElementComparator(), new NullProgressMonitor());
	String sortedSource2 = unit.getBuffer().getContents();
	debug(unit, "AFTER");
	assertEquals("Different output", sortedSource, sortedSource2);
}
private void sortUnit(ICompilationUnit unit) throws CoreException {
	debug(unit, "BEFORE");
	JavaCompilationUnitSorter.sort(new ICompilationUnit[] { unit }, new DefaultJavaElementComparator(), new NullProgressMonitor());
	String sortedSource = unit.getBuffer().getContents();
	JavaCompilationUnitSorter.sort(new ICompilationUnit[] { unit }, new DefaultJavaElementComparator(), new NullProgressMonitor());
	String sortedSource2 = unit.getBuffer().getContents();
	debug(unit, "AFTER");
	assertEquals("Different output", sortedSource, sortedSource2);
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
		suite.addTest(new SortCompilationUnitElementsTests("test006"));
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
			"	class D {\n" +
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
		String expectedResult = "/**\n" +			" *\n" +			" */\n" +			"package p;\n" +			"public class X {\n" +			"	\n" +			"	class D {\n" +			"		String toString() {\n" +			"			return \"HELLO\";\n" +			"		}\n" +			"	}\n" +			"	// start of static field declaration\n" +			"\n" +			"	static int i, j = 3, /*     */ k = 4;// end of static field declaration\n" +			"\n" +			"	Object b1 = null, a1 = new Object() {\n" +			"		void bar() {\n" +			"		}\n" +			"		void bar2() {\n" +			"		}\n" +			"		void bar3() {\n" +			"		}\n" +			"		void bar4() {\n" +			"			System.out.println();\n" +			"		}\n" +			"	}, c1 = null; // end of multiple field declaration\n" +			"	void bar() {\n" +			"		\n" +			"\n" +			"		class E {\n" +			"			void bar2() {}\n" +			"			void bar7() {\n" +			"				System.out.println();\n" +			"			}\n" +			"			void bar9() {}\n" +			"		}\n" +			"		Object o = new E();\n" +			"		System.out.println(o);\n" +			"		class C {\n" +			"			void bar4() {}\n" +			"			void bar5() {}\n" +			"			void bar6() {}\n" +			"		}\n" +			"	}\n" +			"	void bar(int i) {\n" +			"	}\n" +			"	// end of class X\n" +			"}\n" +			"// end of compilation unit\n";
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

}
