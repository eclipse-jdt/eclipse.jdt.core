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
	
	this.createJavaProject("P", new String[] {"src"}, new String[] {getExternalJCLPathString()}, "bin"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	this.createFolder("/P/src/p"); //$NON-NLS-1$
}
private void sortUnit(ICompilationUnit unit, String expectedResult) throws CoreException {
	this.sortUnit(unit, expectedResult, true);
}

private void sortUnit(ICompilationUnit unit, String expectedResult, boolean testPositions) throws CoreException {
	char[] initialSource = unit.getSource().toCharArray();
	int[] positions = null;
	if (testPositions) {
		positions = new int[initialSource.length];
		for (int i = 0; i < initialSource.length; i++) {
			positions[i] = i;
		}
	}
	ICompilationUnit copy = (ICompilationUnit) unit.getWorkingCopy();
	CompilationUnitSorter.sort(copy , positions, new DefaultJavaElementComparator(1,2,3,4,5,6,7,8,9), 0, new NullProgressMonitor());
	String sortedSource = copy.getBuffer().getContents();
	assertEquals("Different output", sortedSource, expectedResult); //$NON-NLS-1$
	if (testPositions) {
		for (int i = 0, max = positions.length; i < max; i++) {
			assertEquals("wrong mapped positions at " + i + " <-> " + positions[i], initialSource[i], expectedResult.charAt(positions[i])); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
}

void debug(ICompilationUnit unit, String id) throws JavaModelException {
	String source = unit.getBuffer().getContents();
	if (DEBUG) {
		System.out.println("========================== " + id + " =============================="); //$NON-NLS-1$ //$NON-NLS-2$
		System.out.println(source);
		System.out.println("========================== " + id + " =============================="); //$NON-NLS-1$ //$NON-NLS-2$
	}
}

public static Test suite() {
	TestSuite suite = new Suite(SortCompilationUnitElementsTests.class.getName());

	if (true) {
		Class c = SortCompilationUnitElementsTests.class;
		Method[] methods = c.getMethods();
		for (int i = 0, max = methods.length; i < max; i++) {
			if (methods[i].getName().startsWith("test")) { //$NON-NLS-1$
				suite.addTest(new SortCompilationUnitElementsTests(methods[i].getName()));
			}
		}
	} else {
		suite.addTest(new SortCompilationUnitElementsTests("test011")); //$NON-NLS-1$
	}	
	return suite;
}
public void tearDownSuite() throws Exception {
	this.deleteProject("P"); //$NON-NLS-1$
	super.tearDownSuite();
}
/**
 * Calls methods that do nothing to ensure code coverage
 */
public void test001() throws CoreException {
	try {
		this.createFile(
			"/P/src/p/X.java", //$NON-NLS-1$
			"/**\n" + //$NON-NLS-1$
			" *\n" + //$NON-NLS-1$
			" */\n" + //$NON-NLS-1$
			"package p;\n" + //$NON-NLS-1$
			"public class X {\n" + //$NON-NLS-1$
			"	\n" + //$NON-NLS-1$
			"	static class D {\n" + //$NON-NLS-1$
			"		String toString() {\n" + //$NON-NLS-1$
			"			return \"HELLO\";\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"	// start of static field declaration\n" + //$NON-NLS-1$
			"\n" + //$NON-NLS-1$
			"	static int i, j = 3, /*     */ k = 4;// end of static field declaration\n" + //$NON-NLS-1$
			"	void bar(int i) {\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"	void bar() {\n" + //$NON-NLS-1$
			"		\n\n" + //$NON-NLS-1$
			"		class E {\n" + //$NON-NLS-1$
			"			void bar7() {\n" + //$NON-NLS-1$
			"				System.out.println();\n" + //$NON-NLS-1$
			"			}\n" + //$NON-NLS-1$
			"			void bar9() {}\n" + //$NON-NLS-1$
			"			void bar2() {}\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"		Object o = new E();\n" + //$NON-NLS-1$
			"		System.out.println(o);\n" + //$NON-NLS-1$
			"		class C {\n" + //$NON-NLS-1$
			"			void bar6() {}\n" + //$NON-NLS-1$
			"			void bar4() {}\n" + //$NON-NLS-1$
			"			void bar5() {}\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"	}\n\n" + //$NON-NLS-1$
			"	Object b1 = null, a1 = new Object() {\n" + //$NON-NLS-1$
			"		void bar2() {\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"		void bar() {\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"		void bar4() {\n" + //$NON-NLS-1$
			"			System.out.println();\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"		void bar3() {\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"	}, c1 = null; // end of multiple field declaration\n" + //$NON-NLS-1$
			"	// end of class X\n" + //$NON-NLS-1$
			"}\n" +  //$NON-NLS-1$
			"// end of compilation unit\n" //$NON-NLS-1$
		);
		String expectedResult = "/**\n" + //$NON-NLS-1$
			" *\n" + //$NON-NLS-1$
			" */\n" + //$NON-NLS-1$
			"package p;\n" + //$NON-NLS-1$
			"public class X {\n" + //$NON-NLS-1$
			"	\n" + //$NON-NLS-1$
			"	static class D {\n" + //$NON-NLS-1$
			"		String toString() {\n" + //$NON-NLS-1$
			"			return \"HELLO\";\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"	// start of static field declaration\n" + //$NON-NLS-1$
			"\n" + //$NON-NLS-1$
			"	static int i, j = 3, /*     */ k = 4;// end of static field declaration\n" + //$NON-NLS-1$
			"\n" + //$NON-NLS-1$
			"	Object b1 = null, a1 = new Object() {\n" + //$NON-NLS-1$
			"		void bar() {\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"		void bar2() {\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"		void bar3() {\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"		void bar4() {\n" + //$NON-NLS-1$
			"			System.out.println();\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"	}, c1 = null; // end of multiple field declaration\n" + //$NON-NLS-1$
			"	void bar() {\n" + //$NON-NLS-1$
			"		\n" + //$NON-NLS-1$
			"\n" + //$NON-NLS-1$
			"		class E {\n" + //$NON-NLS-1$
			"			void bar2() {}\n" + //$NON-NLS-1$
			"			void bar7() {\n" + //$NON-NLS-1$
			"				System.out.println();\n" + //$NON-NLS-1$
			"			}\n" + //$NON-NLS-1$
			"			void bar9() {}\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"		Object o = new E();\n" + //$NON-NLS-1$
			"		System.out.println(o);\n" + //$NON-NLS-1$
			"		class C {\n" + //$NON-NLS-1$
			"			void bar4() {}\n" + //$NON-NLS-1$
			"			void bar5() {}\n" + //$NON-NLS-1$
			"			void bar6() {}\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"	void bar(int i) {\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"	// end of class X\n" + //$NON-NLS-1$
			"}\n" + //$NON-NLS-1$
			"// end of compilation unit\n"; //$NON-NLS-1$
		sortUnit(this.getCompilationUnit("/P/src/p/X.java"), expectedResult); //$NON-NLS-1$
	} finally {
		this.deleteFile("/P/src/p/X.java"); //$NON-NLS-1$
	}
}
public void test002() throws CoreException {
	try {
		this.createFile(
			"/P/src/p/X.java", //$NON-NLS-1$
			"package p;\n" + //$NON-NLS-1$
			"public class X {\n" + //$NON-NLS-1$
			"	\n" + //$NON-NLS-1$
			"	class D {\n" + //$NON-NLS-1$
			"		String toString() {\n" + //$NON-NLS-1$
			"			return \"HELLO\";\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"	int i, j, k;\n" + //$NON-NLS-1$
			"	Object bar() {\n" + //$NON-NLS-1$
			"		System.out.println();\n" + //$NON-NLS-1$
			"		Object o = new Object() {    };\n" + //$NON-NLS-1$
			"		System.out.println(o);\n" + //$NON-NLS-1$
			"		class C {\n" + //$NON-NLS-1$
			"			void bar6() {}\n" + //$NON-NLS-1$
			"			void bar4() {}\n" + //$NON-NLS-1$
			"			void bar5() {}\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"		return new C();\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"	Object bar3() {\n" + //$NON-NLS-1$
			"		System.out.println();\n" + //$NON-NLS-1$
			"		Object o = new Object() {        };\n" + //$NON-NLS-1$
			"		System.out.println(o);\n" + //$NON-NLS-1$
			"		return o;\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"	Object a1 = new Object() { }, o1 = null;\n" + //$NON-NLS-1$
			"}" //$NON-NLS-1$
		);
		String expectedSource = "package p;\n" + //$NON-NLS-1$
			"public class X {\n" + //$NON-NLS-1$
			"	\n" + //$NON-NLS-1$
			"	class D {\n" + //$NON-NLS-1$
			"		String toString() {\n" + //$NON-NLS-1$
			"			return \"HELLO\";\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"	Object a1 = new Object() { }, o1 = null;\n" + //$NON-NLS-1$
			"	int i, j, k;\n" + //$NON-NLS-1$
			"	Object bar() {\n" + //$NON-NLS-1$
			"		System.out.println();\n" + //$NON-NLS-1$
			"		Object o = new Object() {    };\n" + //$NON-NLS-1$
			"		System.out.println(o);\n" + //$NON-NLS-1$
			"		class C {\n" + //$NON-NLS-1$
			"			void bar4() {}\n" + //$NON-NLS-1$
			"			void bar5() {}\n" + //$NON-NLS-1$
			"			void bar6() {}\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"		return new C();\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"	Object bar3() {\n" + //$NON-NLS-1$
			"		System.out.println();\n" + //$NON-NLS-1$
			"		Object o = new Object() {        };\n" + //$NON-NLS-1$
			"		System.out.println(o);\n" + //$NON-NLS-1$
			"		return o;\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"}"; //$NON-NLS-1$
		sortUnit(this.getCompilationUnit("/P/src/p/X.java"), expectedSource); //$NON-NLS-1$
	} finally {
		this.deleteFile("/P/src/p/X.java"); //$NON-NLS-1$
	}
}
/**
 * Calls methods that do nothing to ensure code coverage
 */
public void test003() throws CoreException {
	try {
		this.createFile(
			"/P/src/p/X.java", //$NON-NLS-1$
			"/**\n" + //$NON-NLS-1$
			" *\n" + //$NON-NLS-1$
			" */\n" + //$NON-NLS-1$
			"package p;\n" + //$NON-NLS-1$
			"public class X extends java.lang.Object implements java.util.Cloneable {\n" + //$NON-NLS-1$
			"	\n" + //$NON-NLS-1$
			"	class D {\n" + //$NON-NLS-1$
			"		String toString() {\n" + //$NON-NLS-1$
			"			return \"HELLO\";\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"\n" + //$NON-NLS-1$
			"	// start of field declaration\n" + //$NON-NLS-1$
			"\n" + //$NON-NLS-1$
			"	int i, j = 3, /*     */ k = 4;// end of field declaration\n" + //$NON-NLS-1$
			"	void bar(final int i[]) {\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"	void bar() {\n" + //$NON-NLS-1$
			"		\n\n" + //$NON-NLS-1$
			"		class E {\n" + //$NON-NLS-1$
			"			void bar7() {\n" + //$NON-NLS-1$
			"				System.out.println();\n" + //$NON-NLS-1$
			"			}\n" + //$NON-NLS-1$
			"			void bar9() {}\n" + //$NON-NLS-1$
			"			void bar2() {}\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"		Object o = new E();\n" + //$NON-NLS-1$
			"		System.out.println(o);\n" + //$NON-NLS-1$
			"		class C {\n" + //$NON-NLS-1$
			"			void bar6() {}\n" + //$NON-NLS-1$
			"			void bar4() {}\n" + //$NON-NLS-1$
			"			void bar5() {}\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"	}\n\n" + //$NON-NLS-1$
			"	Object b1[] = null, a1 = new Object() {\n" + //$NON-NLS-1$
			"		void bar2(int[] j) {\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"		void bar() {\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"		void bar4() {\n" + //$NON-NLS-1$
			"			System.out.println();\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"		void bar3() {\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"	}, c1 = null; // end of multiple field declaration\n" + //$NON-NLS-1$
			"	// end of class X\n" + //$NON-NLS-1$
			"}\n" +  //$NON-NLS-1$
			"// end of compilation unit\n" //$NON-NLS-1$
		);
		String expectedResult = "/**\n" + //$NON-NLS-1$
			" *\n" + //$NON-NLS-1$
			" */\n" + //$NON-NLS-1$
			"package p;\n" + //$NON-NLS-1$
			"public class X extends java.lang.Object implements java.util.Cloneable {\n" + //$NON-NLS-1$
			"	\n" + //$NON-NLS-1$
			"	class D {\n" + //$NON-NLS-1$
			"		String toString() {\n" + //$NON-NLS-1$
			"			return \"HELLO\";\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"\n" + //$NON-NLS-1$
			"	Object b1[] = null, a1 = new Object() {\n" + //$NON-NLS-1$
			"		void bar() {\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"		void bar2(int[] j) {\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"		void bar3() {\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"		void bar4() {\n" + //$NON-NLS-1$
			"			System.out.println();\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"	}, c1 = null; // end of multiple field declaration\n" + //$NON-NLS-1$
			"\n" + //$NON-NLS-1$
			"	// start of field declaration\n" + //$NON-NLS-1$
			"\n" + //$NON-NLS-1$
			"	int i, j = 3, /*     */ k = 4;// end of field declaration\n" + //$NON-NLS-1$
			"	void bar() {\n" + //$NON-NLS-1$
			"		\n" + //$NON-NLS-1$
			"\n" + //$NON-NLS-1$
			"		class E {\n" + //$NON-NLS-1$
			"			void bar2() {}\n" + //$NON-NLS-1$
			"			void bar7() {\n" + //$NON-NLS-1$
			"				System.out.println();\n" + //$NON-NLS-1$
			"			}\n" + //$NON-NLS-1$
			"			void bar9() {}\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"		Object o = new E();\n" + //$NON-NLS-1$
			"		System.out.println(o);\n" + //$NON-NLS-1$
			"		class C {\n" + //$NON-NLS-1$
			"			void bar4() {}\n" + //$NON-NLS-1$
			"			void bar5() {}\n" + //$NON-NLS-1$
			"			void bar6() {}\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"	void bar(final int i[]) {\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"	// end of class X\n" + //$NON-NLS-1$
			"}\n" + //$NON-NLS-1$
			"// end of compilation unit\n"; //$NON-NLS-1$
		sortUnit(this.getCompilationUnit("/P/src/p/X.java"), expectedResult); //$NON-NLS-1$
	} finally {
		this.deleteFile("/P/src/p/X.java"); //$NON-NLS-1$
	}
}
/**
 * Calls methods that do nothing to ensure code coverage
 */
public void test004() throws CoreException {
	try {
		this.createFile(
			"/P/src/p/X.java", //$NON-NLS-1$
			"/**\n" + //$NON-NLS-1$
			" *\n" + //$NON-NLS-1$
			" */\n" + //$NON-NLS-1$
			"package p;\n" + //$NON-NLS-1$
			"public class X extends java.lang.Object implements java.util.Cloneable {\n" + //$NON-NLS-1$
			"	\n" + //$NON-NLS-1$
			"	class D {\n" + //$NON-NLS-1$
			"		String toString() {\n" + //$NON-NLS-1$
			"			return \"HELLO\";\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"\n" + //$NON-NLS-1$
			"	// start of method declaration\n" + //$NON-NLS-1$
			"\n" + //$NON-NLS-1$
			"	void bar(final int i[]) {\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"	void bar() {\n" + //$NON-NLS-1$
			"		\n\n" + //$NON-NLS-1$
			"		class E {\n" + //$NON-NLS-1$
			"			Object bar7() {\n" + //$NON-NLS-1$
			"				return new Object() {\n" + //$NON-NLS-1$
			"					void bar9() {}\n" + //$NON-NLS-1$
			"					void bar2() {}\n" + //$NON-NLS-1$
			"				};\n" + //$NON-NLS-1$
			"			}\n" + //$NON-NLS-1$
			"			void bar9() {}\n" + //$NON-NLS-1$
			"			void bar2() {}\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"		Object o = new E();\n" + //$NON-NLS-1$
			"		System.out.println(o);\n" + //$NON-NLS-1$
			"		class C {\n" + //$NON-NLS-1$
			"			void bar6() {}\n" + //$NON-NLS-1$
			"			void bar4() {}\n" + //$NON-NLS-1$
			"			void bar5() {}\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"	}\n\n" + //$NON-NLS-1$
			"	// end of class X\n" + //$NON-NLS-1$
			"}\n" +  //$NON-NLS-1$
			"// end of compilation unit\n" //$NON-NLS-1$
		);
		String expectedResult = "/**\n" + //$NON-NLS-1$
			" *\n" + //$NON-NLS-1$
			" */\n" + //$NON-NLS-1$
			"package p;\n" + //$NON-NLS-1$
			"public class X extends java.lang.Object implements java.util.Cloneable {\n" + //$NON-NLS-1$
			"	\n" + //$NON-NLS-1$
			"	class D {\n" + //$NON-NLS-1$
			"		String toString() {\n" + //$NON-NLS-1$
			"			return \"HELLO\";\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"	void bar() {\n" + //$NON-NLS-1$
			"		\n" + //$NON-NLS-1$
			"\n" + //$NON-NLS-1$
			"		class E {\n" + //$NON-NLS-1$
			"			void bar2() {}\n" + //$NON-NLS-1$
			"			Object bar7() {\n" + //$NON-NLS-1$
			"				return new Object() {\n" + //$NON-NLS-1$
			"					void bar2() {}\n" + //$NON-NLS-1$
			"					void bar9() {}\n" + //$NON-NLS-1$
			"				};\n" + //$NON-NLS-1$
			"			}\n" + //$NON-NLS-1$
			"			void bar9() {}\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"		Object o = new E();\n" + //$NON-NLS-1$
			"		System.out.println(o);\n" + //$NON-NLS-1$
			"		class C {\n" + //$NON-NLS-1$
			"			void bar4() {}\n" + //$NON-NLS-1$
			"			void bar5() {}\n" + //$NON-NLS-1$
			"			void bar6() {}\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"\n" + //$NON-NLS-1$
			"	// start of method declaration\n" + //$NON-NLS-1$
			"\n" + //$NON-NLS-1$
			"	void bar(final int i[]) {\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"\n" + //$NON-NLS-1$
			"	// end of class X\n" + //$NON-NLS-1$
			"}\n" + //$NON-NLS-1$
			"// end of compilation unit\n"; //$NON-NLS-1$
		sortUnit(this.getCompilationUnit("/P/src/p/X.java"), expectedResult); //$NON-NLS-1$
	} finally {
		this.deleteFile("/P/src/p/X.java"); //$NON-NLS-1$
	}
}
public void test005() throws CoreException {
	try {
		this.createFile(
			"/P/src/p/X.java", //$NON-NLS-1$
			"package p;\n" + //$NON-NLS-1$
			"public class X {\n" + //$NON-NLS-1$
			"	Object bar3() {\n" + //$NON-NLS-1$
			"		System.out.println();\n" + //$NON-NLS-1$
			"		Object o = new Object() {        };\n" + //$NON-NLS-1$
			"		System.out.println(o);\n" + //$NON-NLS-1$
			"		return o;\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"}" //$NON-NLS-1$
		);
		String expectedResult = "package p;\n" + //$NON-NLS-1$
			"public class X {\n" + //$NON-NLS-1$
			"	Object bar3() {\n" + //$NON-NLS-1$
			"		System.out.println();\n" + //$NON-NLS-1$
			"		Object o = new Object() {        };\n" + //$NON-NLS-1$
			"		System.out.println(o);\n" + //$NON-NLS-1$
			"		return o;\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"}"; //$NON-NLS-1$
		sortUnit(this.getCompilationUnit("/P/src/p/X.java"), expectedResult); //$NON-NLS-1$
	} finally {
		this.deleteFile("/P/src/p/X.java"); //$NON-NLS-1$
	}
}
public void test006() throws CoreException {
	try {
		this.createFile(
			"/P/src/p/X.java", //$NON-NLS-1$
			"package p;\n" + //$NON-NLS-1$
			"public class X {\n" + //$NON-NLS-1$
			"	Object bar3() {\n" + //$NON-NLS-1$
			"		System.out.println();\n" + //$NON-NLS-1$
			"		return new Object() {\n" + //$NON-NLS-1$
			"			public void bar6() {}\n" + //$NON-NLS-1$
			"			void bar4() throws IOException, Exception, NullPointerException {}\n" + //$NON-NLS-1$
			"			void bar5() {}\n" + //$NON-NLS-1$
			"       };\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"}" //$NON-NLS-1$
		);
		String expectedResult = "package p;\n" + //$NON-NLS-1$
			"public class X {\n" + //$NON-NLS-1$
			"	Object bar3() {\n" + //$NON-NLS-1$
			"		System.out.println();\n" + //$NON-NLS-1$
			"		return new Object() {\n" + //$NON-NLS-1$
			"			void bar4() throws IOException, Exception, NullPointerException {}\n" + //$NON-NLS-1$
			"			void bar5() {}\n" + //$NON-NLS-1$
			"			public void bar6() {}\n" + //$NON-NLS-1$
			"       };\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"}"; //$NON-NLS-1$
		sortUnit(this.getCompilationUnit("/P/src/p/X.java"), expectedResult); //$NON-NLS-1$
	} finally {
		this.deleteFile("/P/src/p/X.java"); //$NON-NLS-1$
	}
}
public void test007() throws CoreException {
	try {
		this.createFile(
			"/P/src/p/X.java", //$NON-NLS-1$
			"package p;\n" + //$NON-NLS-1$
			"public class X {\n" + //$NON-NLS-1$
			"	Object bar3() {\n" + //$NON-NLS-1$
			"		System.out.println();\n" + //$NON-NLS-1$
			"		return new Object() {\n" + //$NON-NLS-1$
			"			public static void bar6() {}\n" + //$NON-NLS-1$
			"			void bar5() {}\n" + //$NON-NLS-1$
			"			void bar4() throws IOException, Exception, NullPointerException {}\n" + //$NON-NLS-1$
			"       };\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"}" //$NON-NLS-1$
		);
		String expectedResult = "package p;\n" + //$NON-NLS-1$
			"public class X {\n" + //$NON-NLS-1$
			"	Object bar3() {\n" + //$NON-NLS-1$
			"		System.out.println();\n" + //$NON-NLS-1$
			"		return new Object() {\n" + //$NON-NLS-1$
			"			public static void bar6() {}\n" + //$NON-NLS-1$
			"			void bar4() throws IOException, Exception, NullPointerException {}\n" + //$NON-NLS-1$
			"			void bar5() {}\n" + //$NON-NLS-1$
			"       };\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"}"; //$NON-NLS-1$
		sortUnit(this.getCompilationUnit("/P/src/p/X.java"), expectedResult); //$NON-NLS-1$
	} finally {
		this.deleteFile("/P/src/p/X.java"); //$NON-NLS-1$
	}
}

public void test008() throws CoreException {
	try {
		this.createFile(
			"/P/src/p/X.java", //$NON-NLS-1$
			"package p;\n" + //$NON-NLS-1$
			"public class X {\n" + //$NON-NLS-1$
			"	Object bar3() {\n" + //$NON-NLS-1$
			"		System.out.println();\n" + //$NON-NLS-1$
			"		return new Object() {\n" + //$NON-NLS-1$
			"			public static void bar6() {}\n" + //$NON-NLS-1$
			"			void bar5() {}\n" + //$NON-NLS-1$
			"			void bar4() throws IOException, Exception, NullPointerException {}\n" + //$NON-NLS-1$
			"       };\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"}" //$NON-NLS-1$
		);
		String expectedResult = "package p;\n" + //$NON-NLS-1$
			"public class X {\n" + //$NON-NLS-1$
			"	Object bar3() {\n" + //$NON-NLS-1$
			"		System.out.println();\n" + //$NON-NLS-1$
			"		return new Object() {\n" + //$NON-NLS-1$
			"			public static void bar6() {}\n" + //$NON-NLS-1$
			"			void bar4() throws IOException, Exception, NullPointerException {}\n" + //$NON-NLS-1$
			"			void bar5() {}\n" + //$NON-NLS-1$
			"       };\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"}"; //$NON-NLS-1$
		sortUnit(this.getCompilationUnit("/P/src/p/X.java"), expectedResult); //$NON-NLS-1$
	} finally {
		this.deleteFile("/P/src/p/X.java"); //$NON-NLS-1$
	}
}
/**
 * Calls methods that do nothing to ensure code coverage
 */
public void test009() throws CoreException {
	try {
		this.createFile(
			"/P/src/p/X.java", //$NON-NLS-1$
			"/**\n" + //$NON-NLS-1$
			" *\n" + //$NON-NLS-1$
			" */\n" + //$NON-NLS-1$
			"package p;\n" + //$NON-NLS-1$
			"public class X {\n" + //$NON-NLS-1$
			"	\n" + //$NON-NLS-1$
			"	static class D {\n" + //$NON-NLS-1$
			"		String toString() {\n" + //$NON-NLS-1$
			"			return \"HELLO\";\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"	// start of static field declaration\n" + //$NON-NLS-1$
			"\n" + //$NON-NLS-1$
			"	static int i, j = 3, /*     */ k = 4;// end of static field declaration\n" + //$NON-NLS-1$
			"	void bar(int i) {\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"	void bar() {\n" + //$NON-NLS-1$
			"		\n\n" + //$NON-NLS-1$
			"		class E {\n" + //$NON-NLS-1$
			"			void bar7() {\n" + //$NON-NLS-1$
			"				System.out.println();\n" + //$NON-NLS-1$
			"			}\n" + //$NON-NLS-1$
			"			void bar9() {}\n" + //$NON-NLS-1$
			"			void bar2() {}\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"		Object o = new E();\n" + //$NON-NLS-1$
			"		System.out.println(o);\n" + //$NON-NLS-1$
			"		class C {\n" + //$NON-NLS-1$
			"			void bar6() {}\n" + //$NON-NLS-1$
			"			void bar4() {}\n" + //$NON-NLS-1$
			"			void bar5() {}\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"	}\n\n" + //$NON-NLS-1$
			"	Object b1 = null, a1 = new Object() {\n" + //$NON-NLS-1$
			"		void bar2() {\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"		void bar() {\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"		void bar4() {\n" + //$NON-NLS-1$
			"			System.out.println();\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"		void bar3() {\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"	}, c1 = null; // end of multiple field declaration\n" + //$NON-NLS-1$
			"	// end of class X\n" + //$NON-NLS-1$
			"}\n" +  //$NON-NLS-1$
			"// end of compilation unit\n" //$NON-NLS-1$
		);
		String expectedResult = "/**\n" + //$NON-NLS-1$
			" *\n" + //$NON-NLS-1$
			" */\n" + //$NON-NLS-1$
			"package p;\n" + //$NON-NLS-1$
			"public class X {\n" + //$NON-NLS-1$
			"	\n" + //$NON-NLS-1$
			"	static class D {\n" + //$NON-NLS-1$
			"		String toString() {\n" + //$NON-NLS-1$
			"			return \"HELLO\";\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"	// start of static field declaration\n" + //$NON-NLS-1$
			"\n" + //$NON-NLS-1$
			"	static int i, j = 3, /*     */ k = 4;// end of static field declaration\n" + //$NON-NLS-1$
			"\n" + //$NON-NLS-1$
			"	Object b1 = null, a1 = new Object() {\n" + //$NON-NLS-1$
			"		void bar() {\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"		void bar2() {\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"		void bar3() {\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"		void bar4() {\n" + //$NON-NLS-1$
			"			System.out.println();\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"	}, c1 = null; // end of multiple field declaration\n" + //$NON-NLS-1$
			"	void bar() {\n" + //$NON-NLS-1$
			"		\n" + //$NON-NLS-1$
			"\n" + //$NON-NLS-1$
			"		class E {\n" + //$NON-NLS-1$
			"			void bar2() {}\n" + //$NON-NLS-1$
			"			void bar7() {\n" + //$NON-NLS-1$
			"				System.out.println();\n" + //$NON-NLS-1$
			"			}\n" + //$NON-NLS-1$
			"			void bar9() {}\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"		Object o = new E();\n" + //$NON-NLS-1$
			"		System.out.println(o);\n" + //$NON-NLS-1$
			"		class C {\n" + //$NON-NLS-1$
			"			void bar4() {}\n" + //$NON-NLS-1$
			"			void bar5() {}\n" + //$NON-NLS-1$
			"			void bar6() {}\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"	void bar(int i) {\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"	// end of class X\n" + //$NON-NLS-1$
			"}\n" + //$NON-NLS-1$
			"// end of compilation unit\n"; //$NON-NLS-1$
		ICompilationUnit unit = this.getCompilationUnit("/P/src/p/X.java"); //$NON-NLS-1$
		sortUnit(unit, expectedResult, false);		
	} finally {
		this.deleteFile("/P/src/p/X.java"); //$NON-NLS-1$
	}
}
public void test010() throws CoreException {
	try {
		this.createFile(
			"/P/src/SuperReference.java", //$NON-NLS-1$
			"public class SuperReference extends ThisReference {\r\n" + //$NON-NLS-1$
			"	\r\n" + //$NON-NLS-1$
			"public SuperReference(int sourceStart, int sourceEnd) {\r\n" + //$NON-NLS-1$
			"	super(sourceStart, sourceEnd);\r\n" + //$NON-NLS-1$
			"}\r\n" + //$NON-NLS-1$
			"public static ExplicitConstructorCall implicitSuperConstructorCall() {\r\n" + //$NON-NLS-1$
			"	return new ExplicitConstructorCall(ExplicitConstructorCall.ImplicitSuper);\r\n" + //$NON-NLS-1$
			"}\r\n" + //$NON-NLS-1$
			"public boolean isImplicitThis() {\r\n" + //$NON-NLS-1$
			"	\r\n" + //$NON-NLS-1$
			"	return false;\r\n" + //$NON-NLS-1$
			"}\r\n" + //$NON-NLS-1$
			"public boolean isSuper() {\r\n" + //$NON-NLS-1$
			"	\r\n" + //$NON-NLS-1$
			"	return true;\r\n" + //$NON-NLS-1$
			"}\r\n" + //$NON-NLS-1$
			"public boolean isThis() {\r\n" + //$NON-NLS-1$
			"	\r\n" + //$NON-NLS-1$
			"	return false ;\r\n" + //$NON-NLS-1$
			"}\r\n" + //$NON-NLS-1$
			"public TypeBinding resolveType(BlockScope scope) {\r\n" + //$NON-NLS-1$
			"	constant = NotAConstant;\r\n" + //$NON-NLS-1$
			"	if (!checkAccess(scope.methodScope()))\r\n" + //$NON-NLS-1$
			"		return null;\r\n" + //$NON-NLS-1$
			"	SourceTypeBinding enclosingTb = scope.enclosingSourceType();\r\n" + //$NON-NLS-1$
			"	if (scope.isJavaLangObject(enclosingTb)) {\r\n" + //$NON-NLS-1$
			"		scope.problemReporter().cannotUseSuperInJavaLangObject(this);\r\n" + //$NON-NLS-1$
			"		return null;\r\n" + //$NON-NLS-1$
			"	}\r\n" + //$NON-NLS-1$
			"	return this.resolvedType = enclosingTb.superclass;\r\n" + //$NON-NLS-1$
			"}\r\n" + //$NON-NLS-1$
			"public String toStringExpression(){\r\n" + //$NON-NLS-1$
			"\r\n" + //$NON-NLS-1$
			"	return \"super\"; //$NON-NLS-1$\r\n" + //$NON-NLS-1$
			"	\r\n" + //$NON-NLS-1$
			"}\r\n" + //$NON-NLS-1$
			"public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope blockScope) {\r\n" + //$NON-NLS-1$
			"	visitor.visit(this, blockScope);\r\n" + //$NON-NLS-1$
			"	visitor.endVisit(this, blockScope);\r\n" + //$NON-NLS-1$
			"}\r\n" + //$NON-NLS-1$
			"}" //$NON-NLS-1$
		);
		String expectedResult = "public class SuperReference extends ThisReference {\r\n" + //$NON-NLS-1$
			"public static ExplicitConstructorCall implicitSuperConstructorCall() {\r\n" + //$NON-NLS-1$
			"	return new ExplicitConstructorCall(ExplicitConstructorCall.ImplicitSuper);\r\n" + //$NON-NLS-1$
			"}\r\n" + //$NON-NLS-1$
			"	\r\n" + //$NON-NLS-1$
			"public SuperReference(int sourceStart, int sourceEnd) {\r\n" + //$NON-NLS-1$
			"	super(sourceStart, sourceEnd);\r\n" + //$NON-NLS-1$
			"}\r\n" + //$NON-NLS-1$
			"public boolean isImplicitThis() {\r\n" + //$NON-NLS-1$
			"	\r\n" + //$NON-NLS-1$
			"	return false;\r\n" + //$NON-NLS-1$
			"}\r\n" + //$NON-NLS-1$
			"public boolean isSuper() {\r\n" + //$NON-NLS-1$
			"	\r\n" + //$NON-NLS-1$
			"	return true;\r\n" + //$NON-NLS-1$
			"}\r\n" + //$NON-NLS-1$
			"public boolean isThis() {\r\n" + //$NON-NLS-1$
			"	\r\n" + //$NON-NLS-1$
			"	return false ;\r\n" + //$NON-NLS-1$
			"}\r\n" + //$NON-NLS-1$
			"public TypeBinding resolveType(BlockScope scope) {\r\n" + //$NON-NLS-1$
			"	constant = NotAConstant;\r\n" + //$NON-NLS-1$
			"	if (!checkAccess(scope.methodScope()))\r\n" + //$NON-NLS-1$
			"		return null;\r\n" + //$NON-NLS-1$
			"	SourceTypeBinding enclosingTb = scope.enclosingSourceType();\r\n" + //$NON-NLS-1$
			"	if (scope.isJavaLangObject(enclosingTb)) {\r\n" + //$NON-NLS-1$
			"		scope.problemReporter().cannotUseSuperInJavaLangObject(this);\r\n" + //$NON-NLS-1$
			"		return null;\r\n" + //$NON-NLS-1$
			"	}\r\n" + //$NON-NLS-1$
			"	return this.resolvedType = enclosingTb.superclass;\r\n" + //$NON-NLS-1$
			"}\r\n" + //$NON-NLS-1$
			"public String toStringExpression(){\r\n" + //$NON-NLS-1$
			"\r\n" + //$NON-NLS-1$
			"	return \"super\"; //$NON-NLS-1$\r\n" + //$NON-NLS-1$
			"	\r\n" + //$NON-NLS-1$
			"}\r\n" + //$NON-NLS-1$
			"public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope blockScope) {\r\n" + //$NON-NLS-1$
			"	visitor.visit(this, blockScope);\r\n" + //$NON-NLS-1$
			"	visitor.endVisit(this, blockScope);\r\n" + //$NON-NLS-1$
			"}\r\n" + //$NON-NLS-1$
			"}"; //$NON-NLS-1$
		sortUnit(this.getCompilationUnit("/P/src/SuperReference.java"), expectedResult); //$NON-NLS-1$
	} finally {
		this.deleteFile("/P/src/SuperReference.java"); //$NON-NLS-1$
	}
}
public void test011() throws CoreException {
	try {
		this.createFile(
			"/P/src/p/BaseTypes.java", //$NON-NLS-1$
			"/*******************************************************************************\r\n" + //$NON-NLS-1$
			" * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.\r\n" + //$NON-NLS-1$
			" * All rights reserved. This program and the accompanying materials \r\n" + //$NON-NLS-1$
			" * are made available under the terms of the Common Public License v0.5 \r\n" + //$NON-NLS-1$
			" * which accompanies this distribution, and is available at\r\n" + //$NON-NLS-1$
			" * http://www.eclipse.org/legal/cpl-v05.html\r\n" + //$NON-NLS-1$
			" * \r\n" + //$NON-NLS-1$
			" * Contributors:\r\n" + //$NON-NLS-1$
			" *     IBM Corporation - initial API and implementation\r\n" + //$NON-NLS-1$
			" ******************************************************************************/\r\n" + //$NON-NLS-1$
			"package p;\r\n" + //$NON-NLS-1$
			"\r\n" + //$NON-NLS-1$
			"public interface BaseTypes {\r\n" + //$NON-NLS-1$
			"	final BaseTypeBinding IntBinding = new BaseTypeBinding(TypeIds.T_int, \"int\".toCharArray(), new char[] {'I'}); //$NON-NLS-1$\r\n" + //$NON-NLS-1$
			"	final BaseTypeBinding ByteBinding = new BaseTypeBinding(TypeIds.T_byte, \"byte\".toCharArray(), new char[] {'B'}); //$NON-NLS-1$\r\n" + //$NON-NLS-1$
			"	final BaseTypeBinding ShortBinding = new BaseTypeBinding(TypeIds.T_short, \"short\".toCharArray(), new char[] {'S'}); //$NON-NLS-1$\r\n" + //$NON-NLS-1$
			"	final BaseTypeBinding CharBinding = new BaseTypeBinding(TypeIds.T_char, \"char\".toCharArray(), new char[] {'C'}); //$NON-NLS-1$\r\n" + //$NON-NLS-1$
			"	final BaseTypeBinding LongBinding = new BaseTypeBinding(TypeIds.T_long, \"long\".toCharArray(), new char[] {'J'}); //$NON-NLS-1$\r\n" + //$NON-NLS-1$
			"	final BaseTypeBinding FloatBinding = new BaseTypeBinding(TypeIds.T_float, \"float\".toCharArray(), new char[] {'F'}); //$NON-NLS-1$\r\n" + //$NON-NLS-1$
			"	final BaseTypeBinding DoubleBinding = new BaseTypeBinding(TypeIds.T_double, \"double\".toCharArray(), new char[] {'D'}); //$NON-NLS-1$\r\n" + //$NON-NLS-1$
			"	final BaseTypeBinding BooleanBinding = new BaseTypeBinding(TypeIds.T_boolean, \"boolean\".toCharArray(), new char[] {'Z'}); //$NON-NLS-1$\r\n" + //$NON-NLS-1$
			"	final BaseTypeBinding NullBinding = new BaseTypeBinding(TypeIds.T_null, \"null\".toCharArray(), new char[] {'N'}); //N stands for null even if it is never internally used //$NON-NLS-1$\r\n" + //$NON-NLS-1$
			"	final BaseTypeBinding VoidBinding = new BaseTypeBinding(TypeIds.T_void, \"void\".toCharArray(), new char[] {'V'}); //$NON-NLS-1$\r\n" + //$NON-NLS-1$
			"}\r\n" //$NON-NLS-1$
		);
		String expectedResult = "/*******************************************************************************\r\n" + //$NON-NLS-1$
			" * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.\r\n" + //$NON-NLS-1$
			" * All rights reserved. This program and the accompanying materials \r\n" + //$NON-NLS-1$
			" * are made available under the terms of the Common Public License v0.5 \r\n" + //$NON-NLS-1$
			" * which accompanies this distribution, and is available at\r\n" + //$NON-NLS-1$
			" * http://www.eclipse.org/legal/cpl-v05.html\r\n" + //$NON-NLS-1$
			" * \r\n" + //$NON-NLS-1$
			" * Contributors:\r\n" + //$NON-NLS-1$
			" *     IBM Corporation - initial API and implementation\r\n" + //$NON-NLS-1$
			" ******************************************************************************/\r\n" + //$NON-NLS-1$
			"package p;\r\n" + //$NON-NLS-1$
			"\r\n" + //$NON-NLS-1$
			"public interface BaseTypes {\r\n" + //$NON-NLS-1$
			"	final BaseTypeBinding BooleanBinding = new BaseTypeBinding(TypeIds.T_boolean, \"boolean\".toCharArray(), new char[] {'Z'}); //$NON-NLS-1$\r\n" + //$NON-NLS-1$
			"	final BaseTypeBinding ByteBinding = new BaseTypeBinding(TypeIds.T_byte, \"byte\".toCharArray(), new char[] {'B'}); //$NON-NLS-1$\r\n" + //$NON-NLS-1$
			"	final BaseTypeBinding CharBinding = new BaseTypeBinding(TypeIds.T_char, \"char\".toCharArray(), new char[] {'C'}); //$NON-NLS-1$\r\n" + //$NON-NLS-1$
			"	final BaseTypeBinding DoubleBinding = new BaseTypeBinding(TypeIds.T_double, \"double\".toCharArray(), new char[] {'D'}); //$NON-NLS-1$\r\n" + //$NON-NLS-1$
			"	final BaseTypeBinding FloatBinding = new BaseTypeBinding(TypeIds.T_float, \"float\".toCharArray(), new char[] {'F'}); //$NON-NLS-1$\r\n" + //$NON-NLS-1$
			"	final BaseTypeBinding IntBinding = new BaseTypeBinding(TypeIds.T_int, \"int\".toCharArray(), new char[] {'I'}); //$NON-NLS-1$\r\n" + //$NON-NLS-1$
			"	final BaseTypeBinding LongBinding = new BaseTypeBinding(TypeIds.T_long, \"long\".toCharArray(), new char[] {'J'}); //$NON-NLS-1$\r\n" + //$NON-NLS-1$
			"	final BaseTypeBinding NullBinding = new BaseTypeBinding(TypeIds.T_null, \"null\".toCharArray(), new char[] {'N'}); //N stands for null even if it is never internally used //$NON-NLS-1$\r\n" + //$NON-NLS-1$
			"	final BaseTypeBinding ShortBinding = new BaseTypeBinding(TypeIds.T_short, \"short\".toCharArray(), new char[] {'S'}); //$NON-NLS-1$\r\n" + //$NON-NLS-1$
			"	final BaseTypeBinding VoidBinding = new BaseTypeBinding(TypeIds.T_void, \"void\".toCharArray(), new char[] {'V'}); //$NON-NLS-1$\r\n" + //$NON-NLS-1$
			"}\r\n"; //$NON-NLS-1$
		sortUnit(this.getCompilationUnit("/P/src/p/BaseTypes.java"), expectedResult); //$NON-NLS-1$
	} finally {
		this.deleteFile("/P/src/p/BaseTypes.java"); //$NON-NLS-1$
	}
}

}
