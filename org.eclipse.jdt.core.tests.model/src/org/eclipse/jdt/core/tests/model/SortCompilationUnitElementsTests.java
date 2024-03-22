/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
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
@SuppressWarnings({"rawtypes", "unchecked"})
public class SortCompilationUnitElementsTests extends ModifyingResourceTests {

private static final boolean DEBUG = true;

public SortCompilationUnitElementsTests(String name) {
	super(name);
}
@Override
public void setUpSuite() throws Exception {
	super.setUpSuite();

	final String compliance = "1.5"; //$NON-NLS-1$
	this.createJavaProject("P", new String[] {"src"}, new String[] {getExternalJCLPathString(compliance)}, "bin", compliance); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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

/**
 * Internal synonym for deprecated constant AST.JSL3
 * to alleviate deprecation warnings.
 * @deprecated
 */
/*package*/ static final int JLS3_INTERNAL = AST.JLS3;

/** @deprecated */
private void oldAPISortUnit(ICompilationUnit unit, String expectedResult, boolean testPositions, Comparator comparator) throws CoreException {
	String initialSource = unit.getSource();
	int[] positions = null;
	int[] initialPositions = null;
	ArrayList arrayList = new ArrayList();
	if (testPositions) {
		for (int i = 0; i < initialSource.length(); i++) {
			if (!Character.isWhitespace(initialSource.charAt(i))) {
				arrayList.add(Integer.valueOf(i));
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
				arrayList.add(Integer.valueOf(i));
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
@Override
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
					"""
			/**
			 *
			 */
			package p;
			public class X {
				static class D {
					String toString() {
						return "HELLO";
					}
				}
				static int i, j = 3, /*     */ k = 4;
				void bar(int i) {
				}
				void bar() {
				\t
			
					class E {
						void bar7() {
							System.out.println();
						}
						void bar9() {}
						void bar2() {}
					}
					Object o = new E();
					System.out.println(o);
					class C {
						void bar6() {}
						void bar4() {}
						void bar5() {}
					}
				}
				Object b1 = null, a1 = new Object() {
					void bar2() {
					}
					void bar() {
					}
					void bar4() {
						System.out.println();
					}
					void bar3() {
					}
				}, c1 = null;
			}
			""" //$NON-NLS-1$
				);
		String expectedResult = """
			/**
			 *
			 */
			package p;
			public class X {
				static class D {
					String toString() {
						return "HELLO";
					}
				}
				static int i, j = 3, /*     */ k = 4;
				Object b1 = null, a1 = new Object() {
					void bar() {
					}
					void bar2() {
					}
					void bar3() {
					}
					void bar4() {
						System.out.println();
					}
				}, c1 = null;
				void bar() {
				\t
			
					class E {
						void bar2() {}
						void bar7() {
							System.out.println();
						}
						void bar9() {}
					}
					Object o = new E();
					System.out.println(o);
					class C {
						void bar4() {}
						void bar5() {}
						void bar6() {}
					}
				}
				void bar(int i) {
				}
			}
			""";//$NON-NLS-1$
		sortUnit(this.getCompilationUnit("/P/src/p/X.java"), expectedResult); //$NON-NLS-1$
	} finally {
		deleteFile("/P/src/p/X.java"); //$NON-NLS-1$
	}
}
public void test002() throws CoreException {
	try {
		this.createFile(
					"/P/src/p/X.java", //$NON-NLS-1$
					"""
			package p;
			public class X {
			\t
				class D {
					String toString() {
						return "HELLO";
					}
				}
				int i, j, k;
				Object bar() {
					System.out.println();
					Object o = new Object() {    };
					System.out.println(o);
					class C {
						void bar6() {}
						void bar4() {}
						void bar5() {}
					}
					return new C();
				}
				Object bar3() {
					System.out.println();
					Object o = new Object() {        };
					System.out.println(o);
					return o;
				}
				Object a1 = new Object() { }, o1 = null;
			}""" //$NON-NLS-1$
				);
		String expectedSource = """
			package p;
			public class X {
			\t
				class D {
					String toString() {
						return "HELLO";
					}
				}
				Object a1 = new Object() { }, o1 = null;
				int i, j, k;
				Object bar() {
					System.out.println();
					Object o = new Object() {    };
					System.out.println(o);
					class C {
						void bar4() {}
						void bar5() {}
						void bar6() {}
					}
					return new C();
				}
				Object bar3() {
					System.out.println();
					Object o = new Object() {        };
					System.out.println(o);
					return o;
				}
			}"""; //$NON-NLS-1$
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
					"""
			/**
			 *
			 */
			package p;
			public class X extends java.lang.Object implements java.util.Cloneable {
				class D {
					String toString() {
						return "HELLO";
					}
				}
				int i, j = 3, /*     */ k = 4;
				void bar(final int i[]) {
				}
				void bar() {
				\t
			
					class E {
						void bar7() {
							System.out.println();
						}
						void bar9() {}
						void bar2() {}
					}
					Object o = new E();
					System.out.println(o);
					class C {
						void bar6() {}
						void bar4() {}
						void bar5() {}
					}
				}
				Object b1[] = null, a1 = new Object() {
					void bar2(int[] j) {
					}
					void bar() {
					}
					void bar4() {
						System.out.println();
					}
					void bar3() {
					}
				}, c1 = null;
			}
			"""//$NON-NLS-1$
				);
		String expectedResult = """
			/**
			 *
			 */
			package p;
			public class X extends java.lang.Object implements java.util.Cloneable {
				class D {
					String toString() {
						return "HELLO";
					}
				}
				Object b1[] = null, a1 = new Object() {
					void bar() {
					}
					void bar2(int[] j) {
					}
					void bar3() {
					}
					void bar4() {
						System.out.println();
					}
				}, c1 = null;
				int i, j = 3, /*     */ k = 4;
				void bar() {
				\t
			
					class E {
						void bar2() {}
						void bar7() {
							System.out.println();
						}
						void bar9() {}
					}
					Object o = new E();
					System.out.println(o);
					class C {
						void bar4() {}
						void bar5() {}
						void bar6() {}
					}
				}
				void bar(final int i[]) {
				}
			}
			"""; //$NON-NLS-1$
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
					"""
			/**
			 *
			 */
			package p;
			public class X extends java.lang.Object implements java.util.Cloneable {
			\t
				class D {
					String toString() {
						return "HELLO";
					}
				}
				void bar(final int i[]) {
				}
				void bar() {
				\t
			
					class E {
						Object bar7() {
							return new Object() {
								void bar9() {}
								void bar2() {}
							};
						}
						void bar9() {}
						void bar2() {}
					}
					Object o = new E();
					System.out.println(o);
					class C {
						void bar6() {}
						void bar4() {}
						void bar5() {}
					}
				}
			
			}
			""" //$NON-NLS-1$
				);
		String expectedResult = """
			/**
			 *
			 */
			package p;
			public class X extends java.lang.Object implements java.util.Cloneable {
			\t
				class D {
					String toString() {
						return "HELLO";
					}
				}
				void bar() {
				\t
			
					class E {
						void bar2() {}
						Object bar7() {
							return new Object() {
								void bar2() {}
								void bar9() {}
							};
						}
						void bar9() {}
					}
					Object o = new E();
					System.out.println(o);
					class C {
						void bar4() {}
						void bar5() {}
						void bar6() {}
					}
				}
				void bar(final int i[]) {
				}
			
			}
			"""; //$NON-NLS-1$
		sortUnit(this.getCompilationUnit("/P/src/p/X.java"), expectedResult); //$NON-NLS-1$
	} finally {
		deleteFile("/P/src/p/X.java"); //$NON-NLS-1$
	}
}
public void test005() throws CoreException {
	try {
		this.createFile(
					"/P/src/p/X.java", //$NON-NLS-1$
					"""
			package p;
			public class X {
				Object bar3() {
					System.out.println();
					Object o = new Object() {        };
					System.out.println(o);
					return o;
				}
			}""" //$NON-NLS-1$
				);
		String expectedResult = """
			package p;
			public class X {
				Object bar3() {
					System.out.println();
					Object o = new Object() {        };
					System.out.println(o);
					return o;
				}
			}"""; //$NON-NLS-1$
		sortUnit(this.getCompilationUnit("/P/src/p/X.java"), expectedResult); //$NON-NLS-1$
	} finally {
		deleteFile("/P/src/p/X.java"); //$NON-NLS-1$
	}
}
public void test006() throws CoreException {
	try {
		this.createFile(
					"/P/src/p/X.java", //$NON-NLS-1$
					"""
			package p;
			public class X {
				Object bar3() {
					System.out.println();
					return new Object() {
						public void bar6() {}
						void bar4() throws IOException, Exception, NullPointerException {}
						void bar5() {}
			       };
				}
			}""" //$NON-NLS-1$
				);
		String expectedResult = """
			package p;
			public class X {
				Object bar3() {
					System.out.println();
					return new Object() {
						void bar4() throws IOException, Exception, NullPointerException {}
						void bar5() {}
						public void bar6() {}
			       };
				}
			}"""; //$NON-NLS-1$
		sortUnit(this.getCompilationUnit("/P/src/p/X.java"), expectedResult); //$NON-NLS-1$
	} finally {
		deleteFile("/P/src/p/X.java"); //$NON-NLS-1$
	}
}
public void test007() throws CoreException {
	try {
		this.createFile(
					"/P/src/p/X.java", //$NON-NLS-1$
					"""
			package p;
			public class X {
				Object bar3() {
					System.out.println();
					return new Object() {
						public static void bar6() {}
						void bar5() {}
						void bar4() throws IOException, Exception, NullPointerException {}
			       };
				}
			}""" //$NON-NLS-1$
				);
		String expectedResult = """
			package p;
			public class X {
				Object bar3() {
					System.out.println();
					return new Object() {
						public static void bar6() {}
						void bar4() throws IOException, Exception, NullPointerException {}
						void bar5() {}
			       };
				}
			}"""; //$NON-NLS-1$
		sortUnit(this.getCompilationUnit("/P/src/p/X.java"), expectedResult); //$NON-NLS-1$
	} finally {
		deleteFile("/P/src/p/X.java"); //$NON-NLS-1$
	}
}

public void test008() throws CoreException {
	try {
		this.createFile(
					"/P/src/p/X.java", //$NON-NLS-1$
					"""
			package p;
			public class X {
				Object bar3() {
					System.out.println();
					return new Object() {
						public static void bar6() {}
						void bar5() {}
						void bar4() throws IOException, Exception, NullPointerException {}
			       };
				}
			}""" //$NON-NLS-1$
				);
		String expectedResult = """
			package p;
			public class X {
				Object bar3() {
					System.out.println();
					return new Object() {
						public static void bar6() {}
						void bar4() throws IOException, Exception, NullPointerException {}
						void bar5() {}
			       };
				}
			}"""; //$NON-NLS-1$
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
					"""
			/**
			 *
			 */
			package p;
			public class X {
			\t
				static class D {
					String toString() {
						return "HELLO";
					}
				}
				static int i, j = 3, /*     */ k = 4;
				void bar(int i) {
				}
				void bar() {
				\t
			
					class E {
						void bar7() {
							System.out.println();
						}
						void bar9() {}
						void bar2() {}
					}
					Object o = new E();
					System.out.println(o);
					class C {
						void bar6() {}
						void bar4() {}
						void bar5() {}
					}
				}
			
				Object b1 = null, a1 = new Object() {
					void bar2() {
					}
					void bar() {
					}
					void bar4() {
						System.out.println();
					}
					void bar3() {
					}
				}, c1 = null;
			}
			"""  //$NON-NLS-1$
				);
		String expectedResult = """
			/**
			 *
			 */
			package p;
			public class X {
			\t
				static class D {
					String toString() {
						return "HELLO";
					}
				}
				static int i, j = 3, /*     */ k = 4;
				Object b1 = null, a1 = new Object() {
					void bar() {
					}
					void bar2() {
					}
					void bar3() {
					}
					void bar4() {
						System.out.println();
					}
				}, c1 = null;
				void bar() {
				\t
			
					class E {
						void bar2() {}
						void bar7() {
							System.out.println();
						}
						void bar9() {}
					}
					Object o = new E();
					System.out.println(o);
					class C {
						void bar4() {}
						void bar5() {}
						void bar6() {}
					}
				}
			
				void bar(int i) {
				}
			}
			"""; //$NON-NLS-1$
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
					"""
			public class SuperReference extends ThisReference {\r
			public SuperReference(int sourceStart, int sourceEnd) {\r
				super(sourceStart, sourceEnd);\r
			}\r
			public static ExplicitConstructorCall implicitSuperConstructorCall() {\r
				return new ExplicitConstructorCall(ExplicitConstructorCall.ImplicitSuper);\r
			}\r
			public boolean isImplicitThis() {\r
				\r
				return false;\r
			}\r
			public boolean isSuper() {\r
				\r
				return true;\r
			}\r
			public boolean isThis() {\r
				\r
				return false ;\r
			}\r
			public TypeBinding resolveType(BlockScope scope) {\r
				constant = NotAConstant;\r
				if (!checkAccess(scope.methodScope()))\r
					return null;\r
				SourceTypeBinding enclosingTb = scope.enclosingSourceType();\r
				if (scope.isJavaLangObject(enclosingTb)) {\r
					scope.problemReporter().cannotUseSuperInJavaLangObject(this);\r
					return null;\r
				}\r
				return this.resolvedType = enclosingTb.superclass;\r
			}\r
			public String toStringExpression(){\r
			\r
				return "super"; //$NON-NLS-1$\r
				\r
			}\r
			public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope blockScope) {\r
				visitor.visit(this, blockScope);\r
				visitor.endVisit(this, blockScope);\r
			}\r
			}""" //$NON-NLS-1$
				);
		String expectedResult = """
			public class SuperReference extends ThisReference {\r
			public static ExplicitConstructorCall implicitSuperConstructorCall() {\r
				return new ExplicitConstructorCall(ExplicitConstructorCall.ImplicitSuper);\r
			}\r
			public SuperReference(int sourceStart, int sourceEnd) {\r
				super(sourceStart, sourceEnd);\r
			}\r
			public boolean isImplicitThis() {\r
				\r
				return false;\r
			}\r
			public boolean isSuper() {\r
				\r
				return true;\r
			}\r
			public boolean isThis() {\r
				\r
				return false ;\r
			}\r
			public TypeBinding resolveType(BlockScope scope) {\r
				constant = NotAConstant;\r
				if (!checkAccess(scope.methodScope()))\r
					return null;\r
				SourceTypeBinding enclosingTb = scope.enclosingSourceType();\r
				if (scope.isJavaLangObject(enclosingTb)) {\r
					scope.problemReporter().cannotUseSuperInJavaLangObject(this);\r
					return null;\r
				}\r
				return this.resolvedType = enclosingTb.superclass;\r
			}\r
			public String toStringExpression(){\r
			\r
				return "super"; //$NON-NLS-1$\r
				\r
			}\r
			public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope blockScope) {\r
				visitor.visit(this, blockScope);\r
				visitor.endVisit(this, blockScope);\r
			}\r
			}"""; //$NON-NLS-1$
		sortUnit(this.getCompilationUnit("/P/src/SuperReference.java"), expectedResult); //$NON-NLS-1$
	} finally {
		deleteFile("/P/src/SuperReference.java"); //$NON-NLS-1$
	}
}
public void test011() throws CoreException {
	try {
		this.createFile(
					"/P/src/p/BaseTypes.java", //$NON-NLS-1$
					"""
			/*******************************************************************************\r
			 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.\r
			 * All rights reserved. This program and the accompanying materials \r
			 * are made available under the terms of the Common Public License v0.5 \r
			 * which accompanies this distribution, and is available at\r
			 * http://www.eclipse.org/legal/cpl-v05.html\r
			 * \r
			 * Contributors:\r
			 *     IBM Corporation - initial API and implementation\r
			 ******************************************************************************/\r
			package p;\r
			\r
			public interface BaseTypes {\r
				final BaseTypeBinding IntBinding = new BaseTypeBinding(TypeIds.T_int, "int".toCharArray(), new char[] {'I'}); //$NON-NLS-1$\r
				final BaseTypeBinding ByteBinding = new BaseTypeBinding(TypeIds.T_byte, "byte".toCharArray(), new char[] {'B'}); //$NON-NLS-1$\r
				final BaseTypeBinding ShortBinding = new BaseTypeBinding(TypeIds.T_short, "short".toCharArray(), new char[] {'S'}); //$NON-NLS-1$\r
				final BaseTypeBinding CharBinding = new BaseTypeBinding(TypeIds.T_char, "char".toCharArray(), new char[] {'C'}); //$NON-NLS-1$\r
				final BaseTypeBinding LongBinding = new BaseTypeBinding(TypeIds.T_long, "long".toCharArray(), new char[] {'J'}); //$NON-NLS-1$\r
				final BaseTypeBinding FloatBinding = new BaseTypeBinding(TypeIds.T_float, "float".toCharArray(), new char[] {'F'}); //$NON-NLS-1$\r
				final BaseTypeBinding DoubleBinding = new BaseTypeBinding(TypeIds.T_double, "double".toCharArray(), new char[] {'D'}); //$NON-NLS-1$\r
				final BaseTypeBinding BooleanBinding = new BaseTypeBinding(TypeIds.T_boolean, "boolean".toCharArray(), new char[] {'Z'}); //$NON-NLS-1$\r
				final BaseTypeBinding NullBinding = new BaseTypeBinding(TypeIds.T_null, "null".toCharArray(), new char[] {'N'}); //N stands for null even if it is never internally used //$NON-NLS-1$\r
				final BaseTypeBinding VoidBinding = new BaseTypeBinding(TypeIds.T_void, "void".toCharArray(), new char[] {'V'}); //$NON-NLS-1$\r
			}\r
			""" //$NON-NLS-1$
				);
		String expectedResult = """
			/*******************************************************************************\r
			 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.\r
			 * All rights reserved. This program and the accompanying materials \r
			 * are made available under the terms of the Common Public License v0.5 \r
			 * which accompanies this distribution, and is available at\r
			 * http://www.eclipse.org/legal/cpl-v05.html\r
			 * \r
			 * Contributors:\r
			 *     IBM Corporation - initial API and implementation\r
			 ******************************************************************************/\r
			package p;\r
			\r
			public interface BaseTypes {\r
				final BaseTypeBinding BooleanBinding = new BaseTypeBinding(TypeIds.T_boolean, "boolean".toCharArray(), new char[] {'Z'}); //$NON-NLS-1$\r
				final BaseTypeBinding ByteBinding = new BaseTypeBinding(TypeIds.T_byte, "byte".toCharArray(), new char[] {'B'}); //$NON-NLS-1$\r
				final BaseTypeBinding CharBinding = new BaseTypeBinding(TypeIds.T_char, "char".toCharArray(), new char[] {'C'}); //$NON-NLS-1$\r
				final BaseTypeBinding DoubleBinding = new BaseTypeBinding(TypeIds.T_double, "double".toCharArray(), new char[] {'D'}); //$NON-NLS-1$\r
				final BaseTypeBinding FloatBinding = new BaseTypeBinding(TypeIds.T_float, "float".toCharArray(), new char[] {'F'}); //$NON-NLS-1$\r
				final BaseTypeBinding IntBinding = new BaseTypeBinding(TypeIds.T_int, "int".toCharArray(), new char[] {'I'}); //$NON-NLS-1$\r
				final BaseTypeBinding LongBinding = new BaseTypeBinding(TypeIds.T_long, "long".toCharArray(), new char[] {'J'}); //$NON-NLS-1$\r
				final BaseTypeBinding NullBinding = new BaseTypeBinding(TypeIds.T_null, "null".toCharArray(), new char[] {'N'}); //N stands for null even if it is never internally used //$NON-NLS-1$\r
				final BaseTypeBinding ShortBinding = new BaseTypeBinding(TypeIds.T_short, "short".toCharArray(), new char[] {'S'}); //$NON-NLS-1$\r
				final BaseTypeBinding VoidBinding = new BaseTypeBinding(TypeIds.T_void, "void".toCharArray(), new char[] {'V'}); //$NON-NLS-1$\r
			}\r
			"""; //$NON-NLS-1$
		sortUnit(this.getCompilationUnit("/P/src/p/BaseTypes.java"), expectedResult); //$NON-NLS-1$
	} finally {
		deleteFile("/P/src/p/BaseTypes.java"); //$NON-NLS-1$
	}
}
public void test012() throws CoreException {
	try {
		this.createFile(
					"/P/src/p/X.java", //$NON-NLS-1$
					"""
			package p;
			public class X {
			\t
				Object bar3() {
					return null;
				}
				bar() {
					System.out.println();
					Object o = new Object() {    };
					System.out.println(o);
					class C {
						void bar6() {}
						void bar4() {}
						void bar5() {}
					}
					return new C();
				}
			}""" //$NON-NLS-1$
				);
		String expectedSource = """
			package p;
			public class X {
			\t
				Object bar3() {
					return null;
				}
				bar() {
					System.out.println();
					Object o = new Object() {    };
					System.out.println(o);
					class C {
						void bar4() {}
						void bar5() {}
						void bar6() {}
					}
					return new C();
				}
			}"""; //$NON-NLS-1$
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
					"""
			package p;
			public class X {
				X bar() {
					// comment
					return new X() {
						void bar6() {}
						void bar4() {}
						void bar5() {}
					};
				}
			}""" //$NON-NLS-1$
				);
		String expectedSource = """
			package p;
			public class X {
				X bar() {
					// comment
					return new X() {
						void bar4() {}
						void bar5() {}
						void bar6() {}
					};
				}
			}"""; //$NON-NLS-1$
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
			"""
				public class X {
				  int j;
				 \s
				  // start of static field declaration
				  static int i; // end of static field declaration
				}"""
		);
		String expectedResult =
			"""
			public class X {
			  // start of static field declaration
			  static int i; // end of static field declaration
			 \s
			  int j;
			}""";
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
			"""
				public class X {
				  int j;
				 \s
				  /** some Java doc */
				 \s
				  // start of static field declaration
				  static int i; // end of static field declaration
				}"""
		);
		String expectedResult =
			"""
			public class X {
			  /** some Java doc */
			 \s
			  // start of static field declaration
			  static int i; // end of static field declaration
			 \s
			  int j;
			}""";
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
			"""
				public class X {
				  \s
				   public void c() {
				     \s
				   }
				  \s
				   public void b() {
				     \s
				   }
				  \s
				   public void a() {
				      class E {
				         // this is the line that breaks the Sort Members.
				         // comment this fix the problem.
				         int x, y;
				      }
				     \s
				     \s
				      new Object() {
				         // it breaks in an anonymous class also.
				         // comment this fix the problem.
				         int x, y;
				      };\s
				     \s
				     \s
				      class D {
				         // this appears to break also.
				      }
				   }
				  \s
				   private class F {
				      // but this works fine
				      int x, y;
				   }
				}"""
		);
		String expectedResult =
			"""
			public class X {
			  \s
			   private class F {
			      // but this works fine
			      int x, y;
			   }
			  \s
			   public void a() {
			      class E {
			         // this is the line that breaks the Sort Members.
			         // comment this fix the problem.
			         int x, y;
			      }
			     \s
			     \s
			      new Object() {
			         // it breaks in an anonymous class also.
			         // comment this fix the problem.
			         int x, y;
			      };\s
			     \s
			     \s
			      class D {
			         // this appears to break also.
			      }
			   }
			  \s
			   public void b() {
			     \s
			   }
			  \s
			   public void c() {
			     \s
			   }
			}""";
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
			"""
				public class X {
				  \s
				   public void c() {
				     \s
				   }
				  \s
				   public void b() {
				     \s
				   }
				  \s
				   public void a() {
				      class E {
				         // this is the line that breaks the Sort Members.
				         // comment this fix the problem.
				         int x, y; // my comment
				      }
				     \s
				     \s
				      new Object() {
				         // it breaks in an anonymous class also.
				         // comment this fix the problem.
				         int x, y; // my comment
				      };\s
				     \s
				     \s
				      class D {
				         // this appears to break also.
				      }
				   }
				  \s
				   private class F {
				      // but this works fine
				      int x, y;
				   }
				}"""
		);
		String expectedResult =
			"""
			public class X {
			  \s
			   private class F {
			      // but this works fine
			      int x, y;
			   }
			  \s
			   public void a() {
			      class E {
			         // this is the line that breaks the Sort Members.
			         // comment this fix the problem.
			         int x, y; // my comment
			      }
			     \s
			     \s
			      new Object() {
			         // it breaks in an anonymous class also.
			         // comment this fix the problem.
			         int x, y; // my comment
			      };\s
			     \s
			     \s
			      class D {
			         // this appears to break also.
			      }
			   }
			  \s
			   public void b() {
			     \s
			   }
			  \s
			   public void c() {
			     \s
			   }
			}""";
		sortUnit(this.getCompilationUnit("/P/src/X.java"), expectedResult);
	} finally {
		deleteFile("/P/src/X.java");
	}
}
public void test018() throws CoreException {
	try {
		this.createFile(
			"/P/src/X.java",
			"""
				public class X {
				   public void c() {
				   }
				  \s
				   public void b() {
				   }
				}"""
		);
		String expectedResult =
			"""
			public class X {
			   public void b() {
			   }
			  \s
			   public void c() {
			   }
			}""";
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
			"""
				public enum X {
					Z, A, C, B;
				}"""
		);
		String expectedResult =
			"""
			public enum X {
				A, B, C, Z;
			}""";
		sortUnit(JLS3_INTERNAL, this.getCompilationUnit("/P/src/X.java"), expectedResult);
	} finally {
		deleteFile("/P/src/X.java");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80036
public void test020() throws CoreException {
	try {
		this.createFile(
			"/P/src/X.java",
			"""
				public enum X {
					A , B, C;
				\t
					void foo() {
					\t
					}
				\t
					public Object field;
				}"""
		);
		String expectedResult =
			"""
			public enum X {
				A , B, C;
			\t
				public Object field;
			\t
				void foo() {
				\t
				}
			}""";
		sortUnit(JLS3_INTERNAL, this.getCompilationUnit("/P/src/X.java"), expectedResult);
	} finally {
		deleteFile("/P/src/X.java");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80036
public void test021() throws CoreException {
	try {
		this.createFile(
			"/P/src/X.java",
			"""
				public class X {
				
					public enum Suit {
						SPADES, CLUBS, HEARTS, DIAMONDS
					}
				
					public enum Card {
						KING, QUEEN, JACK, ACE
					}
				\t
					private String string;
					private int integer;
				\t
					public void method1() { }
				\t
					public void method2() { }
				}"""
		);
		String expectedResult =
			"""
			public class X {
			
				public enum Card {
					ACE, JACK, KING, QUEEN
				}
			
				public enum Suit {
					CLUBS, DIAMONDS, HEARTS, SPADES
				}
			\t
				private int integer;
				private String string;
			\t
				public void method1() { }
			\t
				public void method2() { }
			}""";
		sortUnit(JLS3_INTERNAL, this.getCompilationUnit("/P/src/X.java"), expectedResult);
	} finally {
		deleteFile("/P/src/X.java");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80036
public void test022() throws CoreException {
	try {
		this.createFile(
			"/P/src/BuildUtilities.java",
			"""
				/*******************************************************************************\
				 * Copyright (c) 2000, 2006 IBM Corporation and others.\
				 * All rights reserved. This program and the accompanying materials\
				 * are made available under the terms of the Eclipse Public License v1.0\
				 * which accompanies this distribution, and is available at\
				 * http://www.eclipse.org/legal/epl-v10.html\
				 *\
				 * Contributors:\
				 *     IBM Corporation - initial API and implementation\
				 *******************************************************************************/\
				import java.util.HashSet;
				import org.eclipse.core.resources.ICommand;
				import org.eclipse.core.resources.IProject;
				import org.eclipse.core.resources.IProjectDescription;
				import org.eclipse.core.resources.IResource;
				import org.eclipse.core.resources.IncrementalProjectBuilder;
				import org.eclipse.core.resources.ResourcesPlugin;
				import org.eclipse.core.runtime.CoreException;
				import org.eclipse.core.runtime.IAdaptable;
				import org.eclipse.jface.viewers.ISelection;
				import org.eclipse.jface.viewers.IStructuredSelection;
				import org.eclipse.ui.IEditorInput;
				import org.eclipse.ui.IEditorPart;
				import org.eclipse.ui.IFileEditorInput;
				import org.eclipse.ui.IWorkbenchPart;
				import org.eclipse.ui.IWorkbenchWindow;
				
				/**
				 * This class contains convenience methods used by the various build commands
				 * to determine enablement.  These utilities cannot be factored into a common
				 * class because some build actions are API and some are not.
				 *\s
				 * @since 3.1
				 */
				public class BuildUtilities {
					/**
					 * Extracts the selected projects from a selection.
					 *\s
					 * @param selection The selection to analyze
					 * @return The selected projects
					 */
					public static IProject[] extractProjects(Object[] selection) {
						HashSet projects = new HashSet();
						for (int i = 0; i < selection.length; i++) {
							if (selection[i] instanceof IResource) {
								projects.add(((IResource) selection[i]).getProject());
							} else if (selection[i] instanceof IAdaptable) {
								IAdaptable adaptable = (IAdaptable) selection[i];
								IResource resource = (IResource) adaptable.getAdapter(IResource.class);
								if (resource != null)
									projects.add(resource.getProject());
							}
						}
						return (IProject[]) projects.toArray(new IProject[projects.size()]);
					}
				
					/**
					 * Finds and returns the selected projects in the given window
					 *\s
					 * @param window The window to find the selection in
					 * @return The selected projects, or an empty array if no selection could be found.
					 */
					public static IProject[] findSelectedProjects(IWorkbenchWindow window) {
						if (window == null)
							return new IProject[0];
						ISelection selection = window.getSelectionService().getSelection();
						IProject[] selected = null;
						if (selection != null && !selection.isEmpty() && selection instanceof IStructuredSelection) {
							selected = extractProjects(((IStructuredSelection) selection).toArray());
						} else {
							//see if we can extract a selected project from the active editor
							IWorkbenchPart part = window.getPartService().getActivePart();
							if (part instanceof IEditorPart) {
								IEditorInput input = ((IEditorPart) part).getEditorInput();
								if (input instanceof IFileEditorInput)
									selected = new IProject[] {((IFileEditorInput) input).getFile().getProject()};
							}
						}
						if (selected == null)
							selected = new IProject[0];
						return selected;
					}
				
					/**
					 * Returns whether the workspace has a builder installed that responds
					 * to the given trigger.
					 */
					static boolean hasBuilder(int trigger) {
						IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
						boolean builderFound = false;
						for (int i = 0; i < projects.length; i++) {
							if (!projects[i].isAccessible())
								continue;
							try {
								IProjectDescription description = projects[i].getDescription();
								ICommand[] buildSpec = description.getBuildSpec();
								for (int j = 0; j < buildSpec.length; j++) {
									builderFound = true;
									if (!buildSpec[j].isBuilding(trigger))
										return true;
								}
							} catch (CoreException e) {
								//ignore projects that are not available
							}
						}
						//enable building if there are any accessible projects with builders
						return builderFound;
				
					}
				
					/**
					 * Returns whether the selection of projects is being managed by autobuild.
					 *\s
					 * @param projects The projects to examine
					 * @return <code>true</code> if the projects are being managed by
					 * autobuild, and <code>false</code> otherwise.
					 */
					public static boolean isAutoBuilding(IProject[] projects) {
						if (!ResourcesPlugin.getWorkspace().isAutoBuilding())
							return false;
					\t
					/**
					 * Returns whether one of the projects has a builder whose trigger setting
					 * for the given trigger matches the given value.
					 *\s
					 * @param projects The projects to check
					 * @param trigger The trigger to look for
					 * @param value The trigger value to look for
					 * @return <code>true</code> if one of the projects has a builder whose
					 * trigger activation matches the provided value, and <code>false</code> otherwise.
					 */
					private static boolean matchingTrigger(IProject[] projects, int trigger, boolean value) {
						for (int i = 0; i < projects.length; i++) {
							if (!projects[i].isAccessible())
								continue;
							try {
								IProjectDescription description = projects[i].getDescription();
								ICommand[] buildSpec = description.getBuildSpec();
								for (int j = 0; j < buildSpec.length; j++) {
									if (buildSpec[j].isBuilding(trigger) == value)
										return true;
								}
							} catch (CoreException e) {
								//ignore projects that are not available
							}
						}
						return false;
					}
				
					/**
					 * Returns whether a build command with the given trigger should
					 * be enabled for the given selection.
					 * @param projects The projects to use to determine enablement
					 * @param trigger The build trigger (<code>IncrementalProjectBuilder.*_BUILD</code> constants).
					 * @return <code>true</code> if the action should be enabled, and
					 * <code>false</code> otherwise.
					 */
					public static boolean isEnabled(IProject[] projects, int trigger) {
						return true;
					}
				
					/**
					 * Doesn\'t need to be instantiated
					 */
					private BuildUtilities() {
					}
				}"""
		);
		String expectedResult =
			"""
			/*******************************************************************************\
			 * Copyright (c) 2000, 2006 IBM Corporation and others.\
			 * All rights reserved. This program and the accompanying materials\
			 * are made available under the terms of the Eclipse Public License v1.0\
			 * which accompanies this distribution, and is available at\
			 * http://www.eclipse.org/legal/epl-v10.html\
			 *\
			 * Contributors:\
			 *     IBM Corporation - initial API and implementation\
			 *******************************************************************************/\
			import java.util.HashSet;
			import org.eclipse.core.resources.ICommand;
			import org.eclipse.core.resources.IProject;
			import org.eclipse.core.resources.IProjectDescription;
			import org.eclipse.core.resources.IResource;
			import org.eclipse.core.resources.IncrementalProjectBuilder;
			import org.eclipse.core.resources.ResourcesPlugin;
			import org.eclipse.core.runtime.CoreException;
			import org.eclipse.core.runtime.IAdaptable;
			import org.eclipse.jface.viewers.ISelection;
			import org.eclipse.jface.viewers.IStructuredSelection;
			import org.eclipse.ui.IEditorInput;
			import org.eclipse.ui.IEditorPart;
			import org.eclipse.ui.IFileEditorInput;
			import org.eclipse.ui.IWorkbenchPart;
			import org.eclipse.ui.IWorkbenchWindow;
			
			/**
			 * This class contains convenience methods used by the various build commands
			 * to determine enablement.  These utilities cannot be factored into a common
			 * class because some build actions are API and some are not.
			 *\s
			 * @since 3.1
			 */
			public class BuildUtilities {
				/**
				 * Extracts the selected projects from a selection.
				 *\s
				 * @param selection The selection to analyze
				 * @return The selected projects
				 */
				public static IProject[] extractProjects(Object[] selection) {
					HashSet projects = new HashSet();
					for (int i = 0; i < selection.length; i++) {
						if (selection[i] instanceof IResource) {
							projects.add(((IResource) selection[i]).getProject());
						} else if (selection[i] instanceof IAdaptable) {
							IAdaptable adaptable = (IAdaptable) selection[i];
							IResource resource = (IResource) adaptable.getAdapter(IResource.class);
							if (resource != null)
								projects.add(resource.getProject());
						}
					}
					return (IProject[]) projects.toArray(new IProject[projects.size()]);
				}
			
				/**
				 * Finds and returns the selected projects in the given window
				 *\s
				 * @param window The window to find the selection in
				 * @return The selected projects, or an empty array if no selection could be found.
				 */
				public static IProject[] findSelectedProjects(IWorkbenchWindow window) {
					if (window == null)
						return new IProject[0];
					ISelection selection = window.getSelectionService().getSelection();
					IProject[] selected = null;
					if (selection != null && !selection.isEmpty() && selection instanceof IStructuredSelection) {
						selected = extractProjects(((IStructuredSelection) selection).toArray());
					} else {
						//see if we can extract a selected project from the active editor
						IWorkbenchPart part = window.getPartService().getActivePart();
						if (part instanceof IEditorPart) {
							IEditorInput input = ((IEditorPart) part).getEditorInput();
							if (input instanceof IFileEditorInput)
								selected = new IProject[] {((IFileEditorInput) input).getFile().getProject()};
						}
					}
					if (selected == null)
						selected = new IProject[0];
					return selected;
				}
			
				/**
				 * Returns whether the workspace has a builder installed that responds
				 * to the given trigger.
				 */
				static boolean hasBuilder(int trigger) {
					IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
					boolean builderFound = false;
					for (int i = 0; i < projects.length; i++) {
						if (!projects[i].isAccessible())
							continue;
						try {
							IProjectDescription description = projects[i].getDescription();
							ICommand[] buildSpec = description.getBuildSpec();
							for (int j = 0; j < buildSpec.length; j++) {
								builderFound = true;
								if (!buildSpec[j].isBuilding(trigger))
									return true;
							}
						} catch (CoreException e) {
							//ignore projects that are not available
						}
					}
					//enable building if there are any accessible projects with builders
					return builderFound;
			
				}
			
				/**
				 * Returns whether the selection of projects is being managed by autobuild.
				 *\s
				 * @param projects The projects to examine
				 * @return <code>true</code> if the projects are being managed by
				 * autobuild, and <code>false</code> otherwise.
				 */
				public static boolean isAutoBuilding(IProject[] projects) {
					if (!ResourcesPlugin.getWorkspace().isAutoBuilding())
						return false;
				\t
				/**
				 * Returns whether one of the projects has a builder whose trigger setting
				 * for the given trigger matches the given value.
				 *\s
				 * @param projects The projects to check
				 * @param trigger The trigger to look for
				 * @param value The trigger value to look for
				 * @return <code>true</code> if one of the projects has a builder whose
				 * trigger activation matches the provided value, and <code>false</code> otherwise.
				 */
				private static boolean matchingTrigger(IProject[] projects, int trigger, boolean value) {
					for (int i = 0; i < projects.length; i++) {
						if (!projects[i].isAccessible())
							continue;
						try {
							IProjectDescription description = projects[i].getDescription();
							ICommand[] buildSpec = description.getBuildSpec();
							for (int j = 0; j < buildSpec.length; j++) {
								if (buildSpec[j].isBuilding(trigger) == value)
									return true;
							}
						} catch (CoreException e) {
							//ignore projects that are not available
						}
					}
					return false;
				}
			
				/**
				 * Returns whether a build command with the given trigger should
				 * be enabled for the given selection.
				 * @param projects The projects to use to determine enablement
				 * @param trigger The build trigger (<code>IncrementalProjectBuilder.*_BUILD</code> constants).
				 * @return <code>true</code> if the action should be enabled, and
				 * <code>false</code> otherwise.
				 */
				public static boolean isEnabled(IProject[] projects, int trigger) {
					return true;
				}
			
				/**
				 * Doesn\'t need to be instantiated
				 */
				private BuildUtilities() {
				}
			}""";
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
			"""
				public @interface X {
					String name();
					int id() default 0;
					String value;
					static int GlobalID;
				}
				class A {}"""
		);
		String expectedResult =
			"""
			class A {}
			public @interface X {
				static int GlobalID;
				String value;
				int id() default 0;
				String name();
			}""";
		sortUnit(JLS3_INTERNAL, this.getCompilationUnit("/P/src/X.java"), expectedResult);
	} finally {
		deleteFile("/P/src/X.java");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81329
public void test024() throws CoreException {
	try {
		this.createFile(
			"/P/src/X.java",
			"""
				public class X {
					void foo() {
						class Local {
							static enum E {
								C, B;
							}
						}
					}
					void bar() {
					}
				}"""
		);
		String expectedResult =
			"""
			public class X {
				void bar() {
				}
				void foo() {
					class Local {
						static enum E {
							B, C;
						}
					}
				}
			}""";
		sortUnit(JLS3_INTERNAL, this.getCompilationUnit("/P/src/X.java"), expectedResult);
	} finally {
		deleteFile("/P/src/X.java");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=81329
public void test025() throws CoreException {
	try {
		this.createFile(
			"/P/src/X.java",
			"""
				interface Jpf {
					@interface Action {
						ValidatableProperty[] validatableProperties();
					}
					@interface ValidatableProperty {
						String propertyName();
						ValidationLocaleRules[] localeRules();
					}
					@interface ValidateMinLength {
						String chars();
					}
					@interface ValidationLocaleRules {
						ValidateMinLength validateMinLength();
					}
				public class X {
					@Jpf.Action(validatableProperties = { @Jpf.ValidatableProperty(propertyName = "fooField", localeRules = { @Jpf.ValidationLocaleRules(validateMinLength = @Jpf.ValidateMinLength(chars = "12")) }) })
					public String actionForValidationRuleTest() {
						return null;
					}
				}"""
		);
		String expectedResult =
			"""
			interface Jpf {
				@interface Action {
					ValidatableProperty[] validatableProperties();
				}
				@interface ValidatableProperty {
					ValidationLocaleRules[] localeRules();
					String propertyName();
				}
				@interface ValidateMinLength {
					String chars();
				}
				@interface ValidationLocaleRules {
					ValidateMinLength validateMinLength();
				}
			public class X {
				@Jpf.Action(validatableProperties = { @Jpf.ValidatableProperty(propertyName = "fooField", localeRules = { @Jpf.ValidationLocaleRules(validateMinLength = @Jpf.ValidateMinLength(chars = "12")) }) })
				public String actionForValidationRuleTest() {
					return null;
				}
			}""";
		sortUnit(JLS3_INTERNAL, this.getCompilationUnit("/P/src/X.java"), expectedResult);
	} finally {
		deleteFile("/P/src/X.java");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=95388
public void test026() throws CoreException {
	try {
		this.createFile(
			"/P/src/X.java",
			"""
				public class X {
					void z() {
					}
					void b() {
						System.out.println("b1");
					}
					void b() {
						System.out.println("b2");
					}
					void a() {
					}
				}"""
		);
		String expectedResult =
			"""
			public class X {
				void a() {
				}
				void b() {
					System.out.println("b1");
				}
				void b() {
					System.out.println("b2");
				}
				void z() {
				}
			}""";
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
			"""
				public final class X
				{
					static
					{
				
					}
				
					public static void main(String[] args)
					{
					}
				
					static
					{
				
					}
				}"""
		);
		String expectedResult =
			"""
			public final class X
			{
				static
				{
			
				}
			
				static
				{
			
				}
			
				public static void main(String[] args)
				{
				}
			}""";
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
			"""
				public final class X
				{
				/** JavaDoc comment2 */
				int j;
				/** JavaDoc comment1 */
				int i;
				}"""
		);
		String expectedResult =
			"""
			public final class X
			{
			/** JavaDoc comment1 */
			int i;
			/** JavaDoc comment2 */
			int j;
			}""";
		oldAPISortUnit(this.getCompilationUnit("/P/src/X.java"), expectedResult, false, new Comparator() {
			@Override
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
			"""
				public enum X {
					Z, A, C, B;
				}"""
		);
		String expectedResult =
			"""
			public enum X {
				Z, A, C, B;
			}""";
		sortUnit(JLS3_INTERNAL, this.getCompilationUnit("/P/src/X.java"), expectedResult, false, new Comparator() {
			@Override
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
@SuppressWarnings("deprecation")
public void testBug543073_001() throws CoreException {
	try {
		this.createFile(
			"/P/src/X.java",
			"""
				public enum X {
					Z, A, C, B;
				}"""
		);
		String expectedResult =
			"""
			public enum X {
				Z, A, C, B;
			}""";
		sortUnit(AST_INTERNAL_JLS10, this.getCompilationUnit("/P/src/X.java"), expectedResult, false, new Comparator() {
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
			"""
				public interface I<T> {
					public I<T> foo(A<T> A);
					public <S> I<S> foo2(C<T,S> c);
					public <S> I<S> foo3(C<T,I<S>> c);
					public <K> J<T> bar(C<T,K> c);
					public <K> J<T> bar2(C<T,K> c);
					public <K> I<K<K,T> bar3(C<T,K> c);
					public <K,E> I<K<K,E> bar3(C<T,K> c, C<T,E> c2);
				}"""
		);
		String expectedResult =
			"""
			public interface I<T> {
				public I<T> foo(A<T> A);
				public <S> I<S> foo2(C<T,S> c);
				public <S> I<S> foo3(C<T,I<S>> c);
				public <K> J<T> bar(C<T,K> c);
				public <K> J<T> bar2(C<T,K> c);
				public <K> I<K<K,T> bar3(C<T,K> c);
				public <K,E> I<K<K,E> bar3(C<T,K> c, C<T,E> c2);
			}""";
		sortUnit(JLS3_INTERNAL, this.getCompilationUnit("/P/src/I.java"), expectedResult);
	} finally {
		deleteFile("/P/src/I.java");
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=113722
public void test031() throws CoreException {
	try {
		this.createFile(
			"/P/src/I.java",
			"""
				public interface I<T> {
					public I<T> foo(A<T> A);
					public <S> I<S> foo2(C<T,S> c);
					public <S> I<S> foo3(C<T,I<S>> c);
					public <K> J<T> bar(C<T,K> c);
					public <K> J<T> bar2(C<T,K> c);
					public <K> I<K<K,T>> bar3(C<T,K> c);
					public <K,E> I<K<K,E>> bar3(C<T,K> c, C<T,E> c2);
				}"""
		);
		String expectedResult =
			"""
			public interface I<T> {
				public <K> J<T> bar2(C<T,K> c);
				public <K> I<K<K,T>> bar3(C<T,K> c);
				public <K,E> I<K<K,E>> bar3(C<T,K> c, C<T,E> c2);
				public <K> J<T> bar(C<T,K> c);
				public <S> I<S> foo2(C<T,S> c);
				public <S> I<S> foo3(C<T,I<S>> c);
				public I<T> foo(A<T> A);
			}""";
		sortUnit(JLS3_INTERNAL, this.getCompilationUnit("/P/src/I.java"), expectedResult);
	} finally {
		deleteFile("/P/src/I.java");
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=113722
public void test032() throws CoreException {
	try {
		this.createFile(
			"/P/src/X.java",
			"""
				import java.util.*;
				public interface X<T> {
					<K> List<Map<K,T> foo(Map<T,K> m);
					<K,E> List<Map<K,E> bar(Map<T,K> m, Map<T,E> e);
				}"""
		);
		String expectedResult =
			"""
			import java.util.*;
			public interface X<T> {
				<K> List<Map<K,T> foo(Map<T,K> m);
				<K,E> List<Map<K,E> bar(Map<T,K> m, Map<T,E> e);
			}""";
		sortUnit(JLS3_INTERNAL, this.getCompilationUnit("/P/src/X.java"), expectedResult);
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
			"""
				public enum X {
					Z, A, C, B;
				}"""
		);
		String expectedResult =
			"""
			public enum X {
				A, B, C, Z;
			}""";
		unit = this.getCompilationUnit("/P/src/X.java");
		unit.becomeWorkingCopy(null);
		String source = unit.getSource();
		Document document = new Document(source);
		CompilerOptions options = new CompilerOptions(unit.getJavaProject().getOptions(true));
		ASTParser parser = ASTParser.newParser(JLS3_INTERNAL);
		parser.setCompilerOptions(options.getMap());
		parser.setSource(unit);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		org.eclipse.jdt.core.dom.CompilationUnit ast = (org.eclipse.jdt.core.dom.CompilationUnit) parser.createAST(null);

		Comparator comparator = new Comparator() {
			@Override
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
			"""
				public enum X {
					Z, A, C, B;
				}"""
		);
		unit = this.getCompilationUnit("/P/src/X.java");
		unit.becomeWorkingCopy(null);
		CompilerOptions options = new CompilerOptions(unit.getJavaProject().getOptions(true));
		ASTParser parser = ASTParser.newParser(JLS3_INTERNAL);
		parser.setCompilerOptions(options.getMap());
		parser.setSource(unit);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(false);
		org.eclipse.jdt.core.dom.CompilationUnit ast = (org.eclipse.jdt.core.dom.CompilationUnit) parser.createAST(null);

		Comparator comparator = new Comparator() {
			@Override
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=446255
public void testBug446255() throws CoreException {
	String fileName = "/P/src/BuildUtilities.java";
	try {
		this.createFile(
			fileName,
			"""
				public class MainScreen {
					public MainScreen() {}
					/**
					 * m1
					 */
					void m1() {}
				\t
						this.m2();
					}
				\t
					void m2() {}
				}"""
		);
		String expectedResult =
			"""
			public class MainScreen {
				public MainScreen() {}
				/**
				 * m1
				 */
				void m1() {}
			\t
					this.m2();
				}
			\t
				void m2() {}
			}""";
		sortUnit(this.getCompilationUnit(fileName), expectedResult);
	} finally {
		deleteFile(fileName);
	}
}

}
