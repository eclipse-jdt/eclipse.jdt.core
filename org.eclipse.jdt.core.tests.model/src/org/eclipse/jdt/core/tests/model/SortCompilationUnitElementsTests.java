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

import java.util.ArrayList;
import java.util.Comparator;

import junit.framework.Test;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.util.CompilationUnitSorter;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

/**
 *
 * @since 2.1
 */
public class SortCompilationUnitElementsTests extends ModifyingResourceTests {

private static final boolean DEBUG = true;

public SortCompilationUnitElementsTests(String name) {
	super(name);
}
public void setUpSuite() throws Exception {
	super.setUpSuite();

	this.createJavaProject("P", new String[] {"src"}, new String[] {getExternalJCLPathString()}, "bin", "1.5"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	this.createFolder("/P/src/p"); //$NON-NLS-1$
}
/** @deprecated */
private void sortUnit(ICompilationUnit unit, String expectedResult) throws CoreException {
	this.sortUnit(AST.JLS2, unit, expectedResult, true);
}

private void sortUnit(int apiLevel, ICompilationUnit unit, String expectedResult) throws CoreException {
	this.sortUnit(apiLevel, unit, expectedResult, true);
}
/** @deprecated */
private void sortUnit(ICompilationUnit unit, String expectedResult, boolean testPositions) throws CoreException {
	this.sortUnit(AST.JLS2, unit, expectedResult, testPositions);
}
private void sortUnit(int apiLevel, ICompilationUnit unit, String expectedResult, boolean testPositions) throws CoreException {
	this.sortUnit(apiLevel, unit, expectedResult, testPositions, new DefaultJavaElementComparator(1,2,3,4,5,6,7,8,9));
}
/** @deprecated */
private void oldAPISortUnit(ICompilationUnit unit, String expectedResult, boolean testPositions, Comparator comparator) throws CoreException {
	String initialSource = unit.getSource();
	int[] positions = null;
	int[] initialPositions = null;
	ArrayList arrayList = new ArrayList();
	if (testPositions) {
		for (int i = 0; i < initialSource.length(); i++) {
			if (!Character.isWhitespace(initialSource.charAt(i))) {
				arrayList.add(new Integer(i));
			}
		}
		final int length = arrayList.size();
		positions = new int[length];
		for (int i = 0; i < length; i++) {
			positions[i] = ((Integer) arrayList.get(i)).intValue();
		}
		initialPositions = new int[length];
		System.arraycopy(positions, 0, initialPositions, 0, length);
	}
	ICompilationUnit copy = unit.getWorkingCopy(null);
	CompilationUnitSorter.sort(copy , positions, comparator, 0, new NullProgressMonitor());
	String sortedSource = copy.getBuffer().getContents();
	assertEquals("Different output", expectedResult, sortedSource); //$NON-NLS-1$
	final int expectedResultLength = expectedResult.length();
	if (testPositions) {
		for (int i = 0, max = positions.length; i < max; i++) {
			char mappedChar = ' ';
			char initial = initialSource.charAt(initialPositions[i]);
			try {
				mappedChar = expectedResult.charAt(positions[i]);
				if (mappedChar != initial) {
					System.out.println("wrong mapped positions: " + initialPositions[i] + " <-> " + positions[i] + ": expected " + initial + " but was " + mappedChar); //$NON-NLS-1$ //$NON-NLS-2$
					assertEquals("wrong mapped positions: " + initialPositions[i] + " <-> " + positions[i], initial, mappedChar); //$NON-NLS-1$ //$NON-NLS-2$
				}
			} catch(StringIndexOutOfBoundsException e) {
				System.out.println("Out of bounds : (length = " + expectedResultLength + ") " + positions[i]);
			}
		}
	}
}
private void sortUnit(int apiLevel, ICompilationUnit unit, String expectedResult, boolean testPositions, Comparator comparator) throws CoreException {

	String initialSource = unit.getSource();
	int[] positions = null;
	int[] initialPositions = null;
	ArrayList arrayList = new ArrayList();
	if (testPositions) {
		for (int i = 0; i < initialSource.length(); i++) {
			if (!Character.isWhitespace(initialSource.charAt(i))) {
				arrayList.add(new Integer(i));
			}
		}
		final int length = arrayList.size();
		positions = new int[length];
		for (int i = 0; i < length; i++) {
			positions[i] = ((Integer) arrayList.get(i)).intValue();
		}
		initialPositions = new int[length];
		System.arraycopy(positions, 0, initialPositions, 0, length);
	}
	ICompilationUnit copy = unit.getWorkingCopy(null);
	CompilationUnitSorter.sort(apiLevel, copy , positions, comparator, 0, new NullProgressMonitor());
	String sortedSource = copy.getBuffer().getContents();
	assertEquals("Different output", expectedResult, sortedSource); //$NON-NLS-1$
	final int expectedResultLength = expectedResult.length();
	if (testPositions) {
		for (int i = 0, max = positions.length; i < max; i++) {
			char mappedChar = ' ';
			char initial = initialSource.charAt(initialPositions[i]);
			try {
				mappedChar = expectedResult.charAt(positions[i]);
				if (mappedChar != initial) {
					System.out.println("wrong mapped positions: " + initialPositions[i] + " <-> " + positions[i] + ": expected " + initial + " but was " + mappedChar); //$NON-NLS-1$ //$NON-NLS-2$
					assertEquals("wrong mapped positions: " + initialPositions[i] + " <-> " + positions[i], initial, mappedChar); //$NON-NLS-1$ //$NON-NLS-2$
				}
			} catch(StringIndexOutOfBoundsException e) {
				System.out.println("Out of bounds : (length = " + expectedResultLength + ") " + positions[i]);
			}
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
	return buildModelTestSuite(SortCompilationUnitElementsTests.class);
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
			"	static class D {\n" + //$NON-NLS-1$
			"		String toString() {\n" + //$NON-NLS-1$
			"			return \"HELLO\";\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"	static int i, j = 3, /*     */ k = 4;\n" + //$NON-NLS-1$
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
			"	}\n" + //$NON-NLS-1$
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
			"	}, c1 = null;\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);
		String expectedResult = "/**\n" + //$NON-NLS-1$
			" *\n" + //$NON-NLS-1$
			" */\n" + //$NON-NLS-1$
			"package p;\n" + //$NON-NLS-1$
			"public class X {\n" + //$NON-NLS-1$
			"	static class D {\n" + //$NON-NLS-1$
			"		String toString() {\n" + //$NON-NLS-1$
			"			return \"HELLO\";\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"	static int i, j = 3, /*     */ k = 4;\n" + //$NON-NLS-1$
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
			"	}, c1 = null;\n" + //$NON-NLS-1$
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
			"}\n";//$NON-NLS-1$
		sortUnit(this.getCompilationUnit("/P/src/p/X.java"), expectedResult); //$NON-NLS-1$
	} finally {
		deleteFile("/P/src/p/X.java"); //$NON-NLS-1$
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
		deleteFile("/P/src/p/X.java"); //$NON-NLS-1$
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
			"	class D {\n" + //$NON-NLS-1$
			"		String toString() {\n" + //$NON-NLS-1$
			"			return \"HELLO\";\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"	int i, j = 3, /*     */ k = 4;\n" + //$NON-NLS-1$
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
			"	}\n" + //$NON-NLS-1$
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
			"	}, c1 = null;\n" + //$NON-NLS-1$
			"}\n"//$NON-NLS-1$
		);
		String expectedResult = "/**\n" + //$NON-NLS-1$
			" *\n" + //$NON-NLS-1$
			" */\n" + //$NON-NLS-1$
			"package p;\n" + //$NON-NLS-1$
			"public class X extends java.lang.Object implements java.util.Cloneable {\n" + //$NON-NLS-1$
			"	class D {\n" + //$NON-NLS-1$
			"		String toString() {\n" + //$NON-NLS-1$
			"			return \"HELLO\";\n" + //$NON-NLS-1$
			"		}\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
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
			"	}, c1 = null;\n" + //$NON-NLS-1$
			"	int i, j = 3, /*     */ k = 4;\n" + //$NON-NLS-1$
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
			"}\n"; //$NON-NLS-1$
		sortUnit(this.getCompilationUnit("/P/src/p/X.java"), expectedResult); //$NON-NLS-1$
	} finally {
		deleteFile("/P/src/p/X.java"); //$NON-NLS-1$
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
			"}\n" //$NON-NLS-1$
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
			"	void bar(final int i[]) {\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"\n" + //$NON-NLS-1$
			"}\n"; //$NON-NLS-1$
		sortUnit(this.getCompilationUnit("/P/src/p/X.java"), expectedResult); //$NON-NLS-1$
	} finally {
		deleteFile("/P/src/p/X.java"); //$NON-NLS-1$
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
		deleteFile("/P/src/p/X.java"); //$NON-NLS-1$
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
		deleteFile("/P/src/p/X.java"); //$NON-NLS-1$
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
		deleteFile("/P/src/p/X.java"); //$NON-NLS-1$
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
		deleteFile("/P/src/p/X.java"); //$NON-NLS-1$
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
			"	static int i, j = 3, /*     */ k = 4;\n" + //$NON-NLS-1$
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
			"	}, c1 = null;\n" + //$NON-NLS-1$
			"}\n"  //$NON-NLS-1$
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
			"	static int i, j = 3, /*     */ k = 4;\n" + //$NON-NLS-1$
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
			"	}, c1 = null;\n" + //$NON-NLS-1$
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
			"\n" + //$NON-NLS-1$
			"	void bar(int i) {\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"}\n"; //$NON-NLS-1$
		ICompilationUnit unit = this.getCompilationUnit("/P/src/p/X.java"); //$NON-NLS-1$
		sortUnit(unit, expectedResult, false);
	} finally {
		deleteFile("/P/src/p/X.java"); //$NON-NLS-1$
	}
}
public void test010() throws CoreException {
	try {
		this.createFile(
			"/P/src/SuperReference.java", //$NON-NLS-1$
			"public class SuperReference extends ThisReference {\r\n" + //$NON-NLS-1$
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
		deleteFile("/P/src/SuperReference.java"); //$NON-NLS-1$
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
		deleteFile("/P/src/p/BaseTypes.java"); //$NON-NLS-1$
	}
}
public void test012() throws CoreException {
	try {
		this.createFile(
			"/P/src/p/X.java", //$NON-NLS-1$
			"package p;\n" + //$NON-NLS-1$
			"public class X {\n" + //$NON-NLS-1$
			"	\n" + //$NON-NLS-1$
			"	Object bar3() {\n" + //$NON-NLS-1$
			"		return null;\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"	bar() {\n" + //$NON-NLS-1$
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
			"}" //$NON-NLS-1$
		);
		String expectedSource = "package p;\n" + //$NON-NLS-1$
			"public class X {\n" + //$NON-NLS-1$
			"	\n" + //$NON-NLS-1$
			"	Object bar3() {\n" + //$NON-NLS-1$
			"		return null;\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"	bar() {\n" + //$NON-NLS-1$
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
			"}"; //$NON-NLS-1$
		sortUnit(this.getCompilationUnit("/P/src/p/X.java"), expectedSource); //$NON-NLS-1$
	} finally {
		deleteFile("/P/src/p/X.java"); //$NON-NLS-1$
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=40954
public void test013() throws CoreException {
	try {
		this.createFile(
			"/P/src/p/X.java", //$NON-NLS-1$
			"package p;\n" + //$NON-NLS-1$
			"public class X {\n" + //$NON-NLS-1$
			"	X bar() {\n" + //$NON-NLS-1$
			"		// comment\n" + //$NON-NLS-1$
			"		return new X() {\n" + //$NON-NLS-1$
			"			void bar6() {}\n" + //$NON-NLS-1$
			"			void bar4() {}\n" + //$NON-NLS-1$
			"			void bar5() {}\n" + //$NON-NLS-1$
			"		};\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"}" //$NON-NLS-1$
		);
		String expectedSource = "package p;\n" + //$NON-NLS-1$
			"public class X {\n" + //$NON-NLS-1$
			"	X bar() {\n" + //$NON-NLS-1$
			"		// comment\n" + //$NON-NLS-1$
			"		return new X() {\n" + //$NON-NLS-1$
			"			void bar4() {}\n" + //$NON-NLS-1$
			"			void bar5() {}\n" + //$NON-NLS-1$
			"			void bar6() {}\n" + //$NON-NLS-1$
			"		};\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"}"; //$NON-NLS-1$
		sortUnit(this.getCompilationUnit("/P/src/p/X.java"), expectedSource); //$NON-NLS-1$
	} finally {
		deleteFile("/P/src/p/X.java"); //$NON-NLS-1$
	}
}
/**
 * Preserve comments
 */
public void test014() throws CoreException {
	try {
		this.createFile(
			"/P/src/X.java",
			"public class X {\n" +
			"  int j;\n" +
			"  \n" +
			"  // start of static field declaration\n" +
			"  static int i; // end of static field declaration\n" +
			"}"
		);
		String expectedResult =
			"public class X {\n" +
			"  // start of static field declaration\n" +
			"  static int i; // end of static field declaration\n" +
			"  \n" +
			"  int j;\n" +
			"}";
		sortUnit(this.getCompilationUnit("/P/src/X.java"), expectedResult);
	} finally {
		deleteFile("/P/src/X.java");
	}
}
/**
 * Preserve comments
 */
public void test015() throws CoreException {
	try {
		this.createFile(
			"/P/src/X.java",
			"public class X {\n" +
			"  int j;\n" +
			"  \n" +
			"  /** some Java doc */\n" +
			"  \n" +
			"  // start of static field declaration\n" +
			"  static int i; // end of static field declaration\n" +
			"}"
		);
		String expectedResult =
			"public class X {\n" +
			"  /** some Java doc */\n" +
			"  \n" +
			"  // start of static field declaration\n" +
			"  static int i; // end of static field declaration\n" +
			"  \n" +
			"  int j;\n" +
			"}";
		sortUnit(this.getCompilationUnit("/P/src/X.java"), expectedResult);
	} finally {
		deleteFile("/P/src/X.java");
	}
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=66216
 */
public void test016() throws CoreException {
	try {
		this.createFile(
			"/P/src/X.java",
			"public class X {\n" +
			"   \n" +
			"   public void c() {\n" +
			"      \n" +
			"   }\n" +
			"   \n" +
			"   public void b() {\n" +
			"      \n" +
			"   }\n" +
			"   \n" +
			"   public void a() {\n" +
			"      class E {\n" +
			"         // this is the line that breaks the Sort Members.\n" +
			"         // comment this fix the problem.\n" +
			"         int x, y;\n" +
			"      }\n" +
			"      \n" +
			"      \n" +
			"      new Object() {\n" +
			"         // it breaks in an anonymous class also.\n" +
			"         // comment this fix the problem.\n" +
			"         int x, y;\n" +
			"      }; \n" +
			"      \n" +
			"      \n" +
			"      class D {\n" +
			"         // this appears to break also.\n" +
			"      }\n" +
			"   }\n" +
			"   \n" +
			"   private class F {\n" +
			"      // but this works fine\n" +
			"      int x, y;\n" +
			"   }\n" +
			"}"
		);
		String expectedResult =
			"public class X {\n" +
			"   \n" +
			"   private class F {\n" +
			"      // but this works fine\n" +
			"      int x, y;\n" +
			"   }\n" +
			"   \n" +
			"   public void a() {\n" +
			"      class E {\n" +
			"         // this is the line that breaks the Sort Members.\n" +
			"         // comment this fix the problem.\n" +
			"         int x, y;\n" +
			"      }\n" +
			"      \n" +
			"      \n" +
			"      new Object() {\n" +
			"         // it breaks in an anonymous class also.\n" +
			"         // comment this fix the problem.\n" +
			"         int x, y;\n" +
			"      }; \n" +
			"      \n" +
			"      \n" +
			"      class D {\n" +
			"         // this appears to break also.\n" +
			"      }\n" +
			"   }\n" +
			"   \n" +
			"   public void b() {\n" +
			"      \n" +
			"   }\n" +
			"   \n" +
			"   public void c() {\n" +
			"      \n" +
			"   }\n" +
			"}";
		sortUnit(this.getCompilationUnit("/P/src/X.java"), expectedResult);
	} finally {
		deleteFile("/P/src/X.java");
	}
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=66216
 */
public void test017() throws CoreException {
	try {
		this.createFile(
			"/P/src/X.java",
			"public class X {\n" +
			"   \n" +
			"   public void c() {\n" +
			"      \n" +
			"   }\n" +
			"   \n" +
			"   public void b() {\n" +
			"      \n" +
			"   }\n" +
			"   \n" +
			"   public void a() {\n" +
			"      class E {\n" +
			"         // this is the line that breaks the Sort Members.\n" +
			"         // comment this fix the problem.\n" +
			"         int x, y; // my comment\n" +
			"      }\n" +
			"      \n" +
			"      \n" +
			"      new Object() {\n" +
			"         // it breaks in an anonymous class also.\n" +
			"         // comment this fix the problem.\n" +
			"         int x, y; // my comment\n" +
			"      }; \n" +
			"      \n" +
			"      \n" +
			"      class D {\n" +
			"         // this appears to break also.\n" +
			"      }\n" +
			"   }\n" +
			"   \n" +
			"   private class F {\n" +
			"      // but this works fine\n" +
			"      int x, y;\n" +
			"   }\n" +
			"}"
		);
		String expectedResult =
			"public class X {\n" +
			"   \n" +
			"   private class F {\n" +
			"      // but this works fine\n" +
			"      int x, y;\n" +
			"   }\n" +
			"   \n" +
			"   public void a() {\n" +
			"      class E {\n" +
			"         // this is the line that breaks the Sort Members.\n" +
			"         // comment this fix the problem.\n" +
			"         int x, y; // my comment\n" +
			"      }\n" +
			"      \n" +
			"      \n" +
			"      new Object() {\n" +
			"         // it breaks in an anonymous class also.\n" +
			"         // comment this fix the problem.\n" +
			"         int x, y; // my comment\n" +
			"      }; \n" +
			"      \n" +
			"      \n" +
			"      class D {\n" +
			"         // this appears to break also.\n" +
			"      }\n" +
			"   }\n" +
			"   \n" +
			"   public void b() {\n" +
			"      \n" +
			"   }\n" +
			"   \n" +
			"   public void c() {\n" +
			"      \n" +
			"   }\n" +
			"}";
		sortUnit(this.getCompilationUnit("/P/src/X.java"), expectedResult);
	} finally {
		deleteFile("/P/src/X.java");
	}
}
public void test018() throws CoreException {
	try {
		this.createFile(
			"/P/src/X.java",
			"public class X {\n" +
			"   public void c() {\n" +
			"   }\n" +
			"   \n" +
			"   public void b() {\n" +
			"   }\n" +
			"}"
		);
		String expectedResult =
			"public class X {\n" +
			"   public void b() {\n" +
			"   }\n" +
			"   \n" +
			"   public void c() {\n" +
			"   }\n" +
			"}";
		sortUnit(this.getCompilationUnit("/P/src/X.java"), expectedResult);
	} finally {
		deleteFile("/P/src/X.java");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80036
public void test019() throws CoreException {
	try {
		this.createFile(
			"/P/src/X.java",
			"public enum X {\n" +
			"	Z, A, C, B;\n" +
			"}"
		);
		String expectedResult =
			"public enum X {\n" +
			"	A, B, C, Z;\n" +
			"}";
		sortUnit(AST.JLS3, this.getCompilationUnit("/P/src/X.java"), expectedResult);
	} finally {
		deleteFile("/P/src/X.java");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80036
public void test020() throws CoreException {
	try {
		this.createFile(
			"/P/src/X.java",
			"public enum X {\n" +
			"	A , B, C;\n" +
			"	\n" +
			"	void foo() {\n" +
			"		\n" +
			"	}\n" +
			"	\n" +
			"	public Object field;\n" +
			"}"
		);
		String expectedResult =
			"public enum X {\n" +
			"	A , B, C;\n" +
			"	\n" +
			"	public Object field;\n" +
			"	\n" +
			"	void foo() {\n" +
			"		\n" +
			"	}\n" +
			"}";
		sortUnit(AST.JLS3, this.getCompilationUnit("/P/src/X.java"), expectedResult);
	} finally {
		deleteFile("/P/src/X.java");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80036
public void test021() throws CoreException {
	try {
		this.createFile(
			"/P/src/X.java",
			"public class X {\n" +
			"\n" +
			"	public enum Suit {\n" +
			"		SPADES, CLUBS, HEARTS, DIAMONDS\n" +
			"	}\n" +
			"\n" +
			"	public enum Card {\n" +
			"		KING, QUEEN, JACK, ACE\n" +
			"	}\n" +
			"	\n" +
			"	private String string;\n" +
			"	private int integer;\n" +
			"	\n" +
			"	public void method1() { }\n" +
			"	\n" +
			"	public void method2() { }\n" +
			"}"
		);
		String expectedResult =
			"public class X {\n" +
			"\n" +
			"	public enum Card {\n" +
			"		ACE, JACK, KING, QUEEN\n" +
			"	}\n" +
			"\n" +
			"	public enum Suit {\n" +
			"		CLUBS, DIAMONDS, HEARTS, SPADES\n" +
			"	}\n" +
			"	\n" +
			"	private int integer;\n" +
			"	private String string;\n" +
			"	\n" +
			"	public void method1() { }\n" +
			"	\n" +
			"	public void method2() { }\n" +
			"}";
		sortUnit(AST.JLS3, this.getCompilationUnit("/P/src/X.java"), expectedResult);
	} finally {
		deleteFile("/P/src/X.java");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80036
public void test022() throws CoreException {
	try {
		this.createFile(
			"/P/src/BuildUtilities.java",
			"/*******************************************************************************" +
			" * Copyright (c) 2000, 2006 IBM Corporation and others." +
			" * All rights reserved. This program and the accompanying materials" +
			" * are made available under the terms of the Eclipse Public License v1.0" +
			" * which accompanies this distribution, and is available at" +
			" * http://www.eclipse.org/legal/epl-v10.html" +
			" *" +
			" * Contributors:" +
			" *     IBM Corporation - initial API and implementation" +
			" *******************************************************************************/" +
			"import java.util.HashSet;\n" +
			"import org.eclipse.core.resources.ICommand;\n" +
			"import org.eclipse.core.resources.IProject;\n" +
			"import org.eclipse.core.resources.IProjectDescription;\n" +
			"import org.eclipse.core.resources.IResource;\n" +
			"import org.eclipse.core.resources.IncrementalProjectBuilder;\n" +
			"import org.eclipse.core.resources.ResourcesPlugin;\n" +
			"import org.eclipse.core.runtime.CoreException;\n" +
			"import org.eclipse.core.runtime.IAdaptable;\n" +
			"import org.eclipse.jface.viewers.ISelection;\n" +
			"import org.eclipse.jface.viewers.IStructuredSelection;\n" +
			"import org.eclipse.ui.IEditorInput;\n" +
			"import org.eclipse.ui.IEditorPart;\n" +
			"import org.eclipse.ui.IFileEditorInput;\n" +
			"import org.eclipse.ui.IWorkbenchPart;\n" +
			"import org.eclipse.ui.IWorkbenchWindow;\n" +
			"\n" +
			"/**\n" +
			" * This class contains convenience methods used by the various build commands\n" +
			" * to determine enablement.  These utilities cannot be factored into a common\n" +
			" * class because some build actions are API and some are not.\n" +
			" * \n" +
			" * @since 3.1\n" +
			" */\n" +
			"public class BuildUtilities {\n" +
			"	/**\n" +
			"	 * Extracts the selected projects from a selection.\n" +
			"	 * \n" +
			"	 * @param selection The selection to analyze\n" +
			"	 * @return The selected projects\n" +
			"	 */\n" +
			"	public static IProject[] extractProjects(Object[] selection) {\n" +
			"		HashSet projects = new HashSet();\n" +
			"		for (int i = 0; i < selection.length; i++) {\n" +
			"			if (selection[i] instanceof IResource) {\n" +
			"				projects.add(((IResource) selection[i]).getProject());\n" +
			"			} else if (selection[i] instanceof IAdaptable) {\n" +
			"				IAdaptable adaptable = (IAdaptable) selection[i];\n" +
			"				IResource resource = (IResource) adaptable.getAdapter(IResource.class);\n" +
			"				if (resource != null)\n" +
			"					projects.add(resource.getProject());\n" +
			"			}\n" +
			"		}\n" +
			"		return (IProject[]) projects.toArray(new IProject[projects.size()]);\n" +
			"	}\n" +
			"\n" +
			"	/**\n" +
			"	 * Finds and returns the selected projects in the given window\n" +
			"	 * \n" +
			"	 * @param window The window to find the selection in\n" +
			"	 * @return The selected projects, or an empty array if no selection could be found.\n" +
			"	 */\n" +
			"	public static IProject[] findSelectedProjects(IWorkbenchWindow window) {\n" +
			"		if (window == null)\n" +
			"			return new IProject[0];\n" +
			"		ISelection selection = window.getSelectionService().getSelection();\n" +
			"		IProject[] selected = null;\n" +
			"		if (selection != null && !selection.isEmpty() && selection instanceof IStructuredSelection) {\n" +
			"			selected = extractProjects(((IStructuredSelection) selection).toArray());\n" +
			"		} else {\n" +
			"			//see if we can extract a selected project from the active editor\n" +
			"			IWorkbenchPart part = window.getPartService().getActivePart();\n" +
			"			if (part instanceof IEditorPart) {\n" +
			"				IEditorInput input = ((IEditorPart) part).getEditorInput();\n" +
			"				if (input instanceof IFileEditorInput)\n" +
			"					selected = new IProject[] {((IFileEditorInput) input).getFile().getProject()};\n" +
			"			}\n" +
			"		}\n" +
			"		if (selected == null)\n" +
			"			selected = new IProject[0];\n" +
			"		return selected;\n" +
			"	}\n" +
			"\n" +
			"	/**\n" +
			"	 * Returns whether the workspace has a builder installed that responds\n" +
			"	 * to the given trigger.\n" +
			"	 */\n" +
			"	static boolean hasBuilder(int trigger) {\n" +
			"		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();\n" +
			"		boolean builderFound = false;\n" +
			"		for (int i = 0; i < projects.length; i++) {\n" +
			"			if (!projects[i].isAccessible())\n" +
			"				continue;\n" +
			"			try {\n" +
			"				IProjectDescription description = projects[i].getDescription();\n" +
			"				ICommand[] buildSpec = description.getBuildSpec();\n" +
			"				for (int j = 0; j < buildSpec.length; j++) {\n" +
			"					builderFound = true;\n" +
			"					if (!buildSpec[j].isBuilding(trigger))\n" +
			"						return true;\n" +
			"				}\n" +
			"			} catch (CoreException e) {\n" +
			"				//ignore projects that are not available\n" +
			"			}\n" +
			"		}\n" +
			"		//enable building if there are any accessible projects with builders\n" +
			"		return builderFound;\n" +
			"\n" +
			"	}\n" +
			"\n" +
			"	/**\n" +
			"	 * Returns whether the selection of projects is being managed by autobuild.\n" +
			"	 * \n" +
			"	 * @param projects The projects to examine\n" +
			"	 * @return <code>true</code> if the projects are being managed by\n" +
			"	 * autobuild, and <code>false</code> otherwise.\n" +
			"	 */\n" +
			"	public static boolean isAutoBuilding(IProject[] projects) {\n" +
			"		if (!ResourcesPlugin.getWorkspace().isAutoBuilding())\n" +
			"			return false;\n" +
			"		\n" +
			"	/**\n" +
			"	 * Returns whether one of the projects has a builder whose trigger setting\n" +
			"	 * for the given trigger matches the given value.\n" +
			"	 * \n" +
			"	 * @param projects The projects to check\n" +
			"	 * @param trigger The trigger to look for\n" +
			"	 * @param value The trigger value to look for\n" +
			"	 * @return <code>true</code> if one of the projects has a builder whose\n" +
			"	 * trigger activation matches the provided value, and <code>false</code> otherwise.\n" +
			"	 */\n" +
			"	private static boolean matchingTrigger(IProject[] projects, int trigger, boolean value) {\n" +
			"		for (int i = 0; i < projects.length; i++) {\n" +
			"			if (!projects[i].isAccessible())\n" +
			"				continue;\n" +
			"			try {\n" +
			"				IProjectDescription description = projects[i].getDescription();\n" +
			"				ICommand[] buildSpec = description.getBuildSpec();\n" +
			"				for (int j = 0; j < buildSpec.length; j++) {\n" +
			"					if (buildSpec[j].isBuilding(trigger) == value)\n" +
			"						return true;\n" +
			"				}\n" +
			"			} catch (CoreException e) {\n" +
			"				//ignore projects that are not available\n" +
			"			}\n" +
			"		}\n" +
			"		return false;\n" +
			"	}\n" +
			"\n" +
			"	/**\n" +
			"	 * Returns whether a build command with the given trigger should\n" +
			"	 * be enabled for the given selection.\n" +
			"	 * @param projects The projects to use to determine enablement\n" +
			"	 * @param trigger The build trigger (<code>IncrementalProjectBuilder.*_BUILD</code> constants).\n" +
			"	 * @return <code>true</code> if the action should be enabled, and\n" +
			"	 * <code>false</code> otherwise.\n" +
			"	 */\n" +
			"	public static boolean isEnabled(IProject[] projects, int trigger) {\n" +
			"		return true;\n" +
			"	}\n" +
			"\n" +
			"	/**\n" +
			"	 * Doesn\'t need to be instantiated\n" +
			"	 */\n" +
			"	private BuildUtilities() {\n" +
			"	}\n" +
			"}"
		);
		String expectedResult =
			"/*******************************************************************************" +
			" * Copyright (c) 2000, 2006 IBM Corporation and others." +
			" * All rights reserved. This program and the accompanying materials" +
			" * are made available under the terms of the Eclipse Public License v1.0" +
			" * which accompanies this distribution, and is available at" +
			" * http://www.eclipse.org/legal/epl-v10.html" +
			" *" +
			" * Contributors:" +
			" *     IBM Corporation - initial API and implementation" +
			" *******************************************************************************/" +
			"import java.util.HashSet;\n" +
			"import org.eclipse.core.resources.ICommand;\n" +
			"import org.eclipse.core.resources.IProject;\n" +
			"import org.eclipse.core.resources.IProjectDescription;\n" +
			"import org.eclipse.core.resources.IResource;\n" +
			"import org.eclipse.core.resources.IncrementalProjectBuilder;\n" +
			"import org.eclipse.core.resources.ResourcesPlugin;\n" +
			"import org.eclipse.core.runtime.CoreException;\n" +
			"import org.eclipse.core.runtime.IAdaptable;\n" +
			"import org.eclipse.jface.viewers.ISelection;\n" +
			"import org.eclipse.jface.viewers.IStructuredSelection;\n" +
			"import org.eclipse.ui.IEditorInput;\n" +
			"import org.eclipse.ui.IEditorPart;\n" +
			"import org.eclipse.ui.IFileEditorInput;\n" +
			"import org.eclipse.ui.IWorkbenchPart;\n" +
			"import org.eclipse.ui.IWorkbenchWindow;\n" +
			"\n" +
			"/**\n" +
			" * This class contains convenience methods used by the various build commands\n" +
			" * to determine enablement.  These utilities cannot be factored into a common\n" +
			" * class because some build actions are API and some are not.\n" +
			" * \n" +
			" * @since 3.1\n" +
			" */\n" +
			"public class BuildUtilities {\n" +
			"	/**\n" +
			"	 * Extracts the selected projects from a selection.\n" +
			"	 * \n" +
			"	 * @param selection The selection to analyze\n" +
			"	 * @return The selected projects\n" +
			"	 */\n" +
			"	public static IProject[] extractProjects(Object[] selection) {\n" +
			"		HashSet projects = new HashSet();\n" +
			"		for (int i = 0; i < selection.length; i++) {\n" +
			"			if (selection[i] instanceof IResource) {\n" +
			"				projects.add(((IResource) selection[i]).getProject());\n" +
			"			} else if (selection[i] instanceof IAdaptable) {\n" +
			"				IAdaptable adaptable = (IAdaptable) selection[i];\n" +
			"				IResource resource = (IResource) adaptable.getAdapter(IResource.class);\n" +
			"				if (resource != null)\n" +
			"					projects.add(resource.getProject());\n" +
			"			}\n" +
			"		}\n" +
			"		return (IProject[]) projects.toArray(new IProject[projects.size()]);\n" +
			"	}\n" +
			"\n" +
			"	/**\n" +
			"	 * Finds and returns the selected projects in the given window\n" +
			"	 * \n" +
			"	 * @param window The window to find the selection in\n" +
			"	 * @return The selected projects, or an empty array if no selection could be found.\n" +
			"	 */\n" +
			"	public static IProject[] findSelectedProjects(IWorkbenchWindow window) {\n" +
			"		if (window == null)\n" +
			"			return new IProject[0];\n" +
			"		ISelection selection = window.getSelectionService().getSelection();\n" +
			"		IProject[] selected = null;\n" +
			"		if (selection != null && !selection.isEmpty() && selection instanceof IStructuredSelection) {\n" +
			"			selected = extractProjects(((IStructuredSelection) selection).toArray());\n" +
			"		} else {\n" +
			"			//see if we can extract a selected project from the active editor\n" +
			"			IWorkbenchPart part = window.getPartService().getActivePart();\n" +
			"			if (part instanceof IEditorPart) {\n" +
			"				IEditorInput input = ((IEditorPart) part).getEditorInput();\n" +
			"				if (input instanceof IFileEditorInput)\n" +
			"					selected = new IProject[] {((IFileEditorInput) input).getFile().getProject()};\n" +
			"			}\n" +
			"		}\n" +
			"		if (selected == null)\n" +
			"			selected = new IProject[0];\n" +
			"		return selected;\n" +
			"	}\n" +
			"\n" +
			"	/**\n" +
			"	 * Returns whether the workspace has a builder installed that responds\n" +
			"	 * to the given trigger.\n" +
			"	 */\n" +
			"	static boolean hasBuilder(int trigger) {\n" +
			"		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();\n" +
			"		boolean builderFound = false;\n" +
			"		for (int i = 0; i < projects.length; i++) {\n" +
			"			if (!projects[i].isAccessible())\n" +
			"				continue;\n" +
			"			try {\n" +
			"				IProjectDescription description = projects[i].getDescription();\n" +
			"				ICommand[] buildSpec = description.getBuildSpec();\n" +
			"				for (int j = 0; j < buildSpec.length; j++) {\n" +
			"					builderFound = true;\n" +
			"					if (!buildSpec[j].isBuilding(trigger))\n" +
			"						return true;\n" +
			"				}\n" +
			"			} catch (CoreException e) {\n" +
			"				//ignore projects that are not available\n" +
			"			}\n" +
			"		}\n" +
			"		//enable building if there are any accessible projects with builders\n" +
			"		return builderFound;\n" +
			"\n" +
			"	}\n" +
			"\n" +
			"	/**\n" +
			"	 * Returns whether the selection of projects is being managed by autobuild.\n" +
			"	 * \n" +
			"	 * @param projects The projects to examine\n" +
			"	 * @return <code>true</code> if the projects are being managed by\n" +
			"	 * autobuild, and <code>false</code> otherwise.\n" +
			"	 */\n" +
			"	public static boolean isAutoBuilding(IProject[] projects) {\n" +
			"		if (!ResourcesPlugin.getWorkspace().isAutoBuilding())\n" +
			"			return false;\n" +
			"		\n" +
			"	/**\n" +
			"	 * Returns whether a build command with the given trigger should\n" +
			"	 * be enabled for the given selection.\n" +
			"	 * @param projects The projects to use to determine enablement\n" +
			"	 * @param trigger The build trigger (<code>IncrementalProjectBuilder.*_BUILD</code> constants).\n" +
			"	 * @return <code>true</code> if the action should be enabled, and\n" +
			"	 * <code>false</code> otherwise.\n" +
			"	 */\n" +
			"	public static boolean isEnabled(IProject[] projects, int trigger) {\n" +
			"		return true;\n" +
			"	}\n" +
			"\n" +
			"	/**\n" +
			"	 * Returns whether one of the projects has a builder whose trigger setting\n" +
			"	 * for the given trigger matches the given value.\n" +
			"	 * \n" +
			"	 * @param projects The projects to check\n" +
			"	 * @param trigger The trigger to look for\n" +
			"	 * @param value The trigger value to look for\n" +
			"	 * @return <code>true</code> if one of the projects has a builder whose\n" +
			"	 * trigger activation matches the provided value, and <code>false</code> otherwise.\n" +
			"	 */\n" +
			"	private static boolean matchingTrigger(IProject[] projects, int trigger, boolean value) {\n" +
			"		for (int i = 0; i < projects.length; i++) {\n" +
			"			if (!projects[i].isAccessible())\n" +
			"				continue;\n" +
			"			try {\n" +
			"				IProjectDescription description = projects[i].getDescription();\n" +
			"				ICommand[] buildSpec = description.getBuildSpec();\n" +
			"				for (int j = 0; j < buildSpec.length; j++) {\n" +
			"					if (buildSpec[j].isBuilding(trigger) == value)\n" +
			"						return true;\n" +
			"				}\n" +
			"			} catch (CoreException e) {\n" +
			"				//ignore projects that are not available\n" +
			"			}\n" +
			"		}\n" +
			"		return false;\n" +
			"	}\n" +
			"\n" +
			"	/**\n" +
			"	 * Doesn\'t need to be instantiated\n" +
			"	 */\n" +
			"	private BuildUtilities() {\n" +
			"	}\n" +
			"}";
		sortUnit(this.getCompilationUnit("/P/src/BuildUtilities.java"), expectedResult);
	} finally {
		deleteFile("/P/src/BuildUtilities.java");
	}
}
// Sorting annotation type declaration
public void test023() throws CoreException {
	try {
		this.createFile(
			"/P/src/X.java",
			"public @interface X {\n" +
			"	String name();\n" +
			"	int id() default 0;\n" +
			"	String value;\n" +
			"	static int GlobalID;\n" +
			"}\n" +
			"class A {}"
		);
		String expectedResult =
			"class A {}\n" +
			"public @interface X {\n" +
			"	static int GlobalID;\n" +
			"	String value;\n" +
			"	int id() default 0;\n" +
			"	String name();\n" +
			"}";
		sortUnit(AST.JLS3, this.getCompilationUnit("/P/src/X.java"), expectedResult);
	} finally {
		deleteFile("/P/src/X.java");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81329
public void test024() throws CoreException {
	try {
		this.createFile(
			"/P/src/X.java",
			"public class X {\n" +
			"	void foo() {\n" +
			"		class Local {\n" +
			"			static enum E {\n" +
			"				C, B;\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"	void bar() {\n" +
			"	}\n" +
			"}"
		);
		String expectedResult =
			"public class X {\n" +
			"	void bar() {\n" +
			"	}\n" +
			"	void foo() {\n" +
			"		class Local {\n" +
			"			static enum E {\n" +
			"				B, C;\n" +
			"			}\n" +
			"		}\n" +
			"	}\n" +
			"}";
		sortUnit(AST.JLS3, this.getCompilationUnit("/P/src/X.java"), expectedResult);
	} finally {
		deleteFile("/P/src/X.java");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81329
public void test025() throws CoreException {
	try {
		this.createFile(
			"/P/src/X.java",
			"interface Jpf {\n" +
			"	@interface Action {\n" +
			"		ValidatableProperty[] validatableProperties();\n" +
			"	}\n" +
			"	@interface ValidatableProperty {\n" +
			"		String propertyName();\n" +
			"		ValidationLocaleRules[] localeRules();\n" +
			"	}\n" +
			"	@interface ValidateMinLength {\n" +
			"		String chars();\n" +
			"	}\n" +
			"	@interface ValidationLocaleRules {\n" +
			"		ValidateMinLength validateMinLength();\n" +
			"	}\n" +
			"public class X {\n" +
			"	@Jpf.Action(validatableProperties = { @Jpf.ValidatableProperty(propertyName = \"fooField\", localeRules = { @Jpf.ValidationLocaleRules(validateMinLength = @Jpf.ValidateMinLength(chars = \"12\")) }) })\n" +
			"	public String actionForValidationRuleTest() {\n" +
			"		return null;\n" +
			"	}\n" +
			"}"
		);
		String expectedResult =
			"interface Jpf {\n" +
			"	@interface Action {\n" +
			"		ValidatableProperty[] validatableProperties();\n" +
			"	}\n" +
			"	@interface ValidatableProperty {\n" +
			"		ValidationLocaleRules[] localeRules();\n" +
			"		String propertyName();\n" +
			"	}\n" +
			"	@interface ValidateMinLength {\n" +
			"		String chars();\n" +
			"	}\n" +
			"	@interface ValidationLocaleRules {\n" +
			"		ValidateMinLength validateMinLength();\n" +
			"	}\n" +
			"public class X {\n" +
			"	@Jpf.Action(validatableProperties = { @Jpf.ValidatableProperty(propertyName = \"fooField\", localeRules = { @Jpf.ValidationLocaleRules(validateMinLength = @Jpf.ValidateMinLength(chars = \"12\")) }) })\n" +
			"	public String actionForValidationRuleTest() {\n" +
			"		return null;\n" +
			"	}\n" +
			"}";
		sortUnit(AST.JLS3, this.getCompilationUnit("/P/src/X.java"), expectedResult);
	} finally {
		deleteFile("/P/src/X.java");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=95388
public void test026() throws CoreException {
	try {
		this.createFile(
			"/P/src/X.java",
			"public class X {\n" +
			"	void z() {\n" +
			"	}\n" +
			"	void b() {\n" +
			"		System.out.println(\"b1\");\n" +
			"	}\n" +
			"	void b() {\n" +
			"		System.out.println(\"b2\");\n" +
			"	}\n" +
			"	void a() {\n" +
			"	}\n" +
			"}"
		);
		String expectedResult =
			"public class X {\n" +
			"	void a() {\n" +
			"	}\n" +
			"	void b() {\n" +
			"		System.out.println(\"b1\");\n" +
			"	}\n" +
			"	void b() {\n" +
			"		System.out.println(\"b2\");\n" +
			"	}\n" +
			"	void z() {\n" +
			"	}\n" +
			"}";
		sortUnit(this.getCompilationUnit("/P/src/X.java"), expectedResult);
	} finally {
		deleteFile("/P/src/X.java");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=96583
public void test027() throws CoreException {
	try {
		this.createFile(
			"/P/src/X.java",
			"public final class X\n" +
			"{\n" +
			"	static\n" +
			"	{\n" +
			"\n" +
			"	}\n" +
			"\n" +
			"	public static void main(String[] args)\n" +
			"	{\n" +
			"	}\n" +
			"\n" +
			"	static\n" +
			"	{\n" +
			"\n" +
			"	}\n" +
			"}"
		);
		String expectedResult =
			"public final class X\n" +
			"{\n" +
			"	static\n" +
			"	{\n" +
			"\n" +
			"	}\n" +
			"\n" +
			"	static\n" +
			"	{\n" +
			"\n" +
			"	}\n" +
			"\n" +
			"	public static void main(String[] args)\n" +
			"	{\n" +
			"	}\n" +
			"}";
		sortUnit(this.getCompilationUnit("/P/src/X.java"), expectedResult);
	} finally {
		deleteFile("/P/src/X.java");
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=101453
/** @deprecated */
public void test028() throws CoreException {
	try {
		this.createFile(
			"/P/src/X.java",
			"public final class X\n" +
			"{\n" +
			"/** JavaDoc comment2 */\n" +
			"int j;\n" +
			"/** JavaDoc comment1 */\n" +
			"int i;\n" +
			"}"
		);
		String expectedResult =
			"public final class X\n" +
			"{\n" +
			"/** JavaDoc comment1 */\n" +
			"int i;\n" +
			"/** JavaDoc comment2 */\n" +
			"int j;\n" +
			"}";
		oldAPISortUnit(this.getCompilationUnit("/P/src/X.java"), expectedResult, false, new Comparator() {
			public int compare(Object o1, Object o2) {
				BodyDeclaration bodyDeclaration1 = (BodyDeclaration) o1;
				BodyDeclaration bodyDeclaration2 = (BodyDeclaration) o2;
				Javadoc javadoc1 = bodyDeclaration1.getJavadoc();
				Javadoc javadoc2 = bodyDeclaration2.getJavadoc();
				if (javadoc1 != null && javadoc2 != null) {
					return javadoc1.getComment().compareTo(javadoc2.getComment());
				}
				final int sourceStart1 = ((Integer) bodyDeclaration1.getProperty(CompilationUnitSorter.RELATIVE_ORDER)).intValue();
				final int sourceStart2 = ((Integer) bodyDeclaration2.getProperty(CompilationUnitSorter.RELATIVE_ORDER)).intValue();
				return sourceStart1 - sourceStart2;
			}
		});
	} finally {
		deleteFile("/P/src/X.java");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=101885
public void test029() throws CoreException {
	try {
		this.createFile(
			"/P/src/X.java",
			"public enum X {\n" +
			"	Z, A, C, B;\n" +
			"}"
		);
		String expectedResult =
			"public enum X {\n" +
			"	Z, A, C, B;\n" +
			"}";
		sortUnit(AST.JLS3, this.getCompilationUnit("/P/src/X.java"), expectedResult, false, new Comparator() {
			public int compare(Object o1, Object o2) {
				BodyDeclaration bodyDeclaration1 = (BodyDeclaration) o1;
				BodyDeclaration bodyDeclaration2 = (BodyDeclaration) o2;
				final int sourceStart1 = ((Integer) bodyDeclaration1.getProperty(CompilationUnitSorter.RELATIVE_ORDER)).intValue();
				final int sourceStart2 = ((Integer) bodyDeclaration2.getProperty(CompilationUnitSorter.RELATIVE_ORDER)).intValue();
				return sourceStart1 - sourceStart2;
			}
		});
	} finally {
		deleteFile("/P/src/X.java");
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=113722
public void test030() throws CoreException {
	try {
		this.createFile(
			"/P/src/I.java",
			"public interface I<T> {\n" +
			"	public I<T> foo(A<T> A);\n" +
			"	public <S> I<S> foo2(C<T,S> c);\n" +
			"	public <S> I<S> foo3(C<T,I<S>> c);\n" +
			"	public <K> J<T> bar(C<T,K> c);\n" +
			"	public <K> J<T> bar2(C<T,K> c);\n" +
			"	public <K> I<K<K,T> bar3(C<T,K> c);\n" +
			"	public <K,E> I<K<K,E> bar3(C<T,K> c, C<T,E> c2);\n" +
			"}"
		);
		String expectedResult =
			"public interface I<T> {\n" +
			"	public I<T> foo(A<T> A);\n" +
			"	public <S> I<S> foo2(C<T,S> c);\n" +
			"	public <S> I<S> foo3(C<T,I<S>> c);\n" +
			"	public <K> J<T> bar(C<T,K> c);\n" +
			"	public <K> J<T> bar2(C<T,K> c);\n" +
			"	public <K> I<K<K,T> bar3(C<T,K> c);\n" +
			"	public <K,E> I<K<K,E> bar3(C<T,K> c, C<T,E> c2);\n" +
			"}";
		sortUnit(AST.JLS3, this.getCompilationUnit("/P/src/I.java"), expectedResult);
	} finally {
		deleteFile("/P/src/I.java");
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=113722
public void test031() throws CoreException {
	try {
		this.createFile(
			"/P/src/I.java",
			"public interface I<T> {\n" +
			"	public I<T> foo(A<T> A);\n" +
			"	public <S> I<S> foo2(C<T,S> c);\n" +
			"	public <S> I<S> foo3(C<T,I<S>> c);\n" +
			"	public <K> J<T> bar(C<T,K> c);\n" +
			"	public <K> J<T> bar2(C<T,K> c);\n" +
			"	public <K> I<K<K,T>> bar3(C<T,K> c);\n" +
			"	public <K,E> I<K<K,E>> bar3(C<T,K> c, C<T,E> c2);\n" +
			"}"
		);
		String expectedResult =
			"public interface I<T> {\n" +
			"	public <K> J<T> bar2(C<T,K> c);\n" +
			"	public <K> I<K<K,T>> bar3(C<T,K> c);\n" +
			"	public <K,E> I<K<K,E>> bar3(C<T,K> c, C<T,E> c2);\n" +
			"	public <K> J<T> bar(C<T,K> c);\n" +
			"	public <S> I<S> foo2(C<T,S> c);\n" +
			"	public <S> I<S> foo3(C<T,I<S>> c);\n" +
			"	public I<T> foo(A<T> A);\n" +
			"}";
		sortUnit(AST.JLS3, this.getCompilationUnit("/P/src/I.java"), expectedResult);
	} finally {
		deleteFile("/P/src/I.java");
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=113722
public void test032() throws CoreException {
	try {
		this.createFile(
			"/P/src/X.java",
			"import java.util.*;\n" +
			"public interface X<T> {\n" +
			"	<K> List<Map<K,T> foo(Map<T,K> m);\n" +
			"	<K,E> List<Map<K,E> bar(Map<T,K> m, Map<T,E> e);\n" +
			"}"
		);
		String expectedResult =
			"import java.util.*;\n" +
			"public interface X<T> {\n" +
			"	<K> List<Map<K,T> foo(Map<T,K> m);\n" +
			"	<K,E> List<Map<K,E> bar(Map<T,K> m, Map<T,E> e);\n" +
			"}";
		sortUnit(AST.JLS3, this.getCompilationUnit("/P/src/X.java"), expectedResult);
	} finally {
		deleteFile("/P/src/X.java");
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=171066
public void test033() throws CoreException {
	ICompilationUnit unit = null;

	try {
		this.createFile(
			"/P/src/X.java",
			"public enum X {\n" +
			"	Z, A, C, B;\n" +
			"}"
		);
		String expectedResult =
			"public enum X {\n" +
			"	A, B, C, Z;\n" +
			"}";
		unit = this.getCompilationUnit("/P/src/X.java");
		unit.becomeWorkingCopy(null);
		String source = unit.getSource();
		Document document = new Document(source);
		CompilerOptions options = new CompilerOptions(unit.getJavaProject().getOptions(true));
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setCompilerOptions(options.getMap());
		parser.setSource(unit);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		org.eclipse.jdt.core.dom.CompilationUnit ast = (org.eclipse.jdt.core.dom.CompilationUnit) parser.createAST(null);

		Comparator comparator = new Comparator() {
			public int compare(Object o1, Object o2) {
				switch(((ASTNode) o1).getNodeType()) {
					case ASTNode.ENUM_CONSTANT_DECLARATION :
						if (o2 instanceof EnumConstantDeclaration) {
							return ((EnumConstantDeclaration) o1).getName().getIdentifier().compareTo(((EnumConstantDeclaration) o2).getName().getIdentifier());
						}
				}
				BodyDeclaration bodyDeclaration1 = (BodyDeclaration) o1;
				BodyDeclaration bodyDeclaration2 = (BodyDeclaration) o2;
				final int sourceStart1 = ((Integer) bodyDeclaration1.getProperty(CompilationUnitSorter.RELATIVE_ORDER)).intValue();
				final int sourceStart2 = ((Integer) bodyDeclaration2.getProperty(CompilationUnitSorter.RELATIVE_ORDER)).intValue();
				return sourceStart1 - sourceStart2;
			}
		};
		TextEdit edit = CompilationUnitSorter.sort(ast , comparator, 0, null, new NullProgressMonitor());
		try {
			edit.apply(document);
		} catch (MalformedTreeException e) {
			assertTrue("Should not happen", false);
		} catch (BadLocationException e) {
			assertTrue("Should not happen", false);
		}
		assertEquals("Different output", expectedResult, document.get());
	} finally {
		deleteFile("/P/src/X.java");
		if (unit != null) {
			unit.discardWorkingCopy();
		}
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=171066
public void test034() throws CoreException {
	ICompilationUnit unit = null;
	try {
		this.createFile(
			"/P/src/X.java",
			"public enum X {\n" +
			"	Z, A, C, B;\n" +
			"}"
		);
		unit = this.getCompilationUnit("/P/src/X.java");
		unit.becomeWorkingCopy(null);
		CompilerOptions options = new CompilerOptions(unit.getJavaProject().getOptions(true));
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setCompilerOptions(options.getMap());
		parser.setSource(unit);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(false);
		org.eclipse.jdt.core.dom.CompilationUnit ast = (org.eclipse.jdt.core.dom.CompilationUnit) parser.createAST(null);

		Comparator comparator = new Comparator() {
			public int compare(Object o1, Object o2) {
				BodyDeclaration bodyDeclaration1 = (BodyDeclaration) o1;
				BodyDeclaration bodyDeclaration2 = (BodyDeclaration) o2;
				final int sourceStart1 = ((Integer) bodyDeclaration1.getProperty(CompilationUnitSorter.RELATIVE_ORDER)).intValue();
				final int sourceStart2 = ((Integer) bodyDeclaration2.getProperty(CompilationUnitSorter.RELATIVE_ORDER)).intValue();
				return sourceStart1 - sourceStart2;
			}
		};
		TextEdit edit = CompilationUnitSorter.sort(ast , comparator, 0, null, new NullProgressMonitor());
		assertNull("Should be null", edit);
	} finally {
		deleteFile("/P/src/X.java");
		if (unit != null) {
			unit.discardWorkingCopy();
		}
	}
}
}
