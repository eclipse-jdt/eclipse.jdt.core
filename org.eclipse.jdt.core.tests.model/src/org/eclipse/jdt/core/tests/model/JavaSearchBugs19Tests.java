/*******************************************************************************
 * Copyright (c) 2022, 2023 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.ReferenceMatch;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.TypeReferenceMatch;
import org.eclipse.jdt.internal.core.SourceType;

import junit.framework.Test;

public class JavaSearchBugs19Tests extends AbstractJavaSearchTests {

	static {
		// org.eclipse.jdt.internal.core.search.BasicSearchEngine.VERBOSE = true;
		// TESTS_NUMBERS = new int[] { 19 };
		// TESTS_RANGE = new int[] { 1, -1 };
		// TESTS_NAMES = new String[] {"testBug542559_001"};
	}

	public JavaSearchBugs19Tests(String name) {
		super(name);
		this.endChar = "";
	}

	public static Test suite() {
		return buildModelTestSuite(JavaSearchBugs19Tests.class, BYTECODE_DECLARATION_ORDER);
	}

	class TestCollector extends JavaSearchResultCollector {
		public void acceptSearchMatch(SearchMatch searchMatch) throws CoreException {
			super.acceptSearchMatch(searchMatch);
		}
	}

	class ReferenceCollector extends JavaSearchResultCollector {
		protected void writeLine() throws CoreException {
			super.writeLine();
			ReferenceMatch refMatch = (ReferenceMatch) this.match;
			IJavaElement localElement = refMatch.getLocalElement();
			if (localElement != null) {
				this.line.append("+[");
				if (localElement.getElementType() == IJavaElement.ANNOTATION) {
					this.line.append('@');
					this.line.append(localElement.getElementName());
					this.line.append(" on ");
					this.line.append(localElement.getParent().getElementName());
				} else {
					this.line.append(localElement.getElementName());
				}
				this.line.append(']');
			}
		}
	}

	class TypeReferenceCollector extends ReferenceCollector {
		protected void writeLine() throws CoreException {
			super.writeLine();
			TypeReferenceMatch typeRefMatch = (TypeReferenceMatch) this.match;
			IJavaElement[] others = typeRefMatch.getOtherElements();
			int length = others == null ? 0 : others.length;
			if (length > 0) {
				this.line.append("+[");
				for (int i = 0; i < length; i++) {
					IJavaElement other = others[i];
					if (i > 0)
						this.line.append(',');
					if (other.getElementType() == IJavaElement.ANNOTATION) {
						this.line.append('@');
						this.line.append(other.getElementName());
						this.line.append(" on ");
						this.line.append(other.getParent().getElementName());
					} else {
						this.line.append(other.getElementName());
					}
				}
				this.line.append(']');
			}
		}
	}

	protected IJavaProject setUpJavaProject(final String projectName, String compliance, boolean useFullJCL)
			throws CoreException, IOException {
		// copy files in project from source workspace to target workspace
		IJavaProject setUpJavaProject = super.setUpJavaProject(projectName, compliance, useFullJCL);
		return setUpJavaProject;
	}

	IJavaSearchScope getJavaSearchScope() {
		return SearchEngine.createJavaSearchScope(new IJavaProject[] { getJavaProject("JavaSearchBugs") });
	}

	IJavaSearchScope getJavaSearchScopeBugs(String packageName, boolean addSubpackages) throws JavaModelException {
		if (packageName == null)
			return getJavaSearchScope();
		return getJavaSearchPackageScope("JavaSearchBugs", packageName, addSubpackages);
	}

	public ICompilationUnit getWorkingCopy(String path, String source) throws JavaModelException {
		if (this.wcOwner == null) {
			this.wcOwner = new WorkingCopyOwner() {
			};
		}
		return getWorkingCopy(path, source, this.wcOwner);
	}

	@Override
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		JAVA_PROJECT = setUpJavaProject("JavaSearchBugs", "19");
	}

	public void tearDownSuite() throws Exception {
		deleteProject("JavaSearchBugs");
		super.tearDownSuite();
	}

	protected void setUp() throws Exception {
		super.setUp();
		this.resultCollector = new TestCollector();
		this.resultCollector.showAccuracy(true);
	}

	// record pattern - just check if search for record pattern local declaration works as expected
	// Enable with fix of https://github.com/eclipse-jdt/eclipse.jdt.core/issues/785
	public void _testIssue215_001() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public class X {\n"
						+ "  static void print(Rectangle r) {\n"
						+ "    if (r instanceof (Rectangle(ColoredPoint(Point(int x, int y), Color c),\n"
						+ "                               ColoredPoint lr) /*here*/r11)) {\n"
						+ "        System.out.println(\"Upper-left corner: \" + r11);\n"
						+ "    }\n"
						+ "  }\n"
						+ "  public static void main(String[] obj) {\n"
						+ "    print(new Rectangle(new ColoredPoint(new Point(0, 0), Color.BLUE), \n"
						+ "                               new ColoredPoint(new Point(10, 15), Color.RED)));\n"
						+ "  }\n"
						+ "}\n"
						+ "record Point(int x, int y) {}\n"
						+ "enum Color { RED, GREEN, BLUE }\n"
						+ "record ColoredPoint(Point p, Color c) {}\n"
						+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		// working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			String str = this.workingCopies[0].getSource();
			String selection = "/*here*/r11";
			int start = str.indexOf(selection);
			int length = selection.length();

			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			ILocalVariable local = (ILocalVariable) elements[0];
			search(local, REFERENCES, EXACT_RULE);
			assertSearchResults("src/X.java void X.print(Rectangle) [r11] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}


	// record pattern - just check if search for component in record pattern local declaration works as expected
	public void testIssue215_002() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public class X {\n"
						+ "  static void print(Rectangle r) {\n"
						+ "    if (r instanceof (Rectangle(ColoredPoint(Point(int /*here*/xyz, int y), Color c),\n"
						+ "                               ColoredPoint lr))) {\n"
						+ "        System.out.println(\"Upper-left corner: \" + xyz);\n"
						+ "    }\n"
						+ "  }\n"
						+ "  public static void main(String[] obj) {\n"
						+ "    print(new Rectangle(new ColoredPoint(new Point(0, 0), Color.BLUE), \n"
						+ "                               new ColoredPoint(new Point(10, 15), Color.RED)));\n"
						+ "  }\n"
						+ "}\n"
						+ "record Point(int x, int y) {}\n"
						+ "enum Color { RED, GREEN, BLUE }\n"
						+ "record ColoredPoint(Point p, Color c) {}\n"
						+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		// working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			String str = this.workingCopies[0].getSource();
			String selection = "/*here*/xyz";
			int start = str.indexOf(selection);
			int length = selection.length();

			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			ILocalVariable local = (ILocalVariable) elements[0];
			search(local, REFERENCES, EXACT_RULE);
			assertSearchResults("src/X.java void X.print(Rectangle) [xyz] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	// record pattern - just check if all occurence search for record pattern local declaration works
	// Enable with fix of https://github.com/eclipse-jdt/eclipse.jdt.core/issues/785
	public void _testIssue215_003() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public class X {\n"
						+ "  static void print(Rectangle r) {\n"
						+ "    if (r instanceof (Rectangle(ColoredPoint(Point(int x, int y), Color c),\n"
						+ "                               ColoredPoint lr) /*here*/r11)) {\n"
						+ "        System.out.println(\"Upper-left corner: \" + r11);\n"
						+ "    }\n"
						+ "  }\n"
						+ "  public static void main(String[] obj) {\n"
						+ "    print(new Rectangle(new ColoredPoint(new Point(0, 0), Color.BLUE), \n"
						+ "                               new ColoredPoint(new Point(10, 15), Color.RED)));\n"
						+ "  }\n"
						+ "}\n"
						+ "record Point(int x, int y) {}\n"
						+ "enum Color { RED, GREEN, BLUE }\n"
						+ "record ColoredPoint(Point p, Color c) {}\n"
						+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		// working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			String str = this.workingCopies[0].getSource();
			String selection = "/*here*/r11";
			int start = str.indexOf(selection);
			int length = selection.length();

			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			ILocalVariable local = (ILocalVariable) elements[0];
			search(local, ALL_OCCURRENCES, EXACT_RULE);
			assertSearchResults(
					"src/X.java void X.print(Rectangle).r11 [r11] EXACT_MATCH\n"
							+ "src/X.java void X.print(Rectangle) [r11] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}


	// record pattern - just check if all occurence search for component in record pattern local declaration works
	// Enable with fix of https://github.com/eclipse-jdt/eclipse.jdt.core/issues/785
	public void _testIssue215_004() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public class X {\n"
						+ "  static void print(Rectangle r) {\n"
						+ "    if (r instanceof (Rectangle(ColoredPoint(Point(int /*here*/xyz, int y), Color c),\n"
						+ "                               ColoredPoint lr) r11)) {\n"
						+ "        System.out.println(\"Upper-left corner: \" + xyz);\n"
						+ "    }\n"
						+ "  }\n"
						+ "  public static void main(String[] obj) {\n"
						+ "    print(new Rectangle(new ColoredPoint(new Point(0, 0), Color.BLUE), \n"
						+ "                               new ColoredPoint(new Point(10, 15), Color.RED)));\n"
						+ "  }\n"
						+ "}\n"
						+ "record Point(int x, int y) {}\n"
						+ "enum Color { RED, GREEN, BLUE }\n"
						+ "record ColoredPoint(Point p, Color c) {}\n"
						+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		// working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			String str = this.workingCopies[0].getSource();
			String selection = "/*here*/xyz";
			int start = str.indexOf(selection);
			int length = selection.length();

			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			ILocalVariable local = (ILocalVariable) elements[0];
			search(local, ALL_OCCURRENCES, EXACT_RULE);
			assertSearchResults(
					"src/X.java void X.print(Rectangle).xyz [xyz] EXACT_MATCH\n"
							+ "src/X.java void X.print(Rectangle) [xyz] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	// record pattern - if search for "valid record pattern and make the pattern variable available in switch expression" works
	// Enable with fix of https://github.com/eclipse-jdt/eclipse.jdt.core/issues/785
	public void _testIssue215_005() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"@SuppressWarnings(\"preview\")"
						+ "public class X {\n"
						+ "  public static void printLowerRight(Rectangle r) {\n"
						+ "    int res = switch(r) {\n"
						+ "       case Rectangle(ColoredPoint(Point(int x, int y), Color c),\n"
						+ "                               ColoredPoint lr) /*here*/r11  -> {\n"
						+ "             System.out.println(r11);\n"
						+ "        		yield 1;  \n"
						+ "        } \n"
						+ "        default -> 0;\n"
						+ "    }; \n"
						+ "    System.out.println(res);\n"
						+ "  }\n"
						+ "  public static void main(String[] args) {\n"
						+ "    printLowerRight(new Rectangle(new ColoredPoint(new Point(15, 5), Color.BLUE), \n"
						+ "        new ColoredPoint(new Point(30, 10), Color.RED)));\n"
						+ "  }\n"
						+ "}\n"
						+ "record Point(int x, int y) {}\n"
						+ "enum Color { RED, GREEN, BLUE }\n"
						+ "record ColoredPoint(Point p, Color c) {}\n"
						+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		// working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			String str = this.workingCopies[0].getSource();
			String selection = "/*here*/r11";
			int start = str.indexOf(selection);
			int length = selection.length();

			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			ILocalVariable local = (ILocalVariable) elements[0];
			search(local, REFERENCES, EXACT_RULE);
			assertSearchResults("src/X.java void X.printLowerRight(Rectangle) [r11] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	// record pattern - if search for component in "valid record pattern and make the pattern variable available in switch expression" works
	// Enable with fix of https://github.com/eclipse-jdt/eclipse.jdt.core/issues/785
	public void _testIssue215_006() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"@SuppressWarnings(\"preview\")"
						+ "public class X {\n"
						+ "  public static void printLowerRight(Rectangle r) {\n"
						+ "    int res = switch(r) {\n"
						+ "       case Rectangle(ColoredPoint(Point(int /*here*/xyz, int y), Color c),\n"
						+ "                               ColoredPoint lr) r11  -> {\n"
						+ "             System.out.println(xyz);\n"
						+ "        		yield 1;  \n"
						+ "        } \n"
						+ "        default -> 0;\n"
						+ "    }; \n"
						+ "    System.out.println(res);\n"
						+ "  }\n"
						+ "  public static void main(String[] args) {\n"
						+ "    printLowerRight(new Rectangle(new ColoredPoint(new Point(15, 5), Color.BLUE), \n"
						+ "        new ColoredPoint(new Point(30, 10), Color.RED)));\n"
						+ "  }\n"
						+ "}\n"
						+ "record Point(int x, int y) {}\n"
						+ "enum Color { RED, GREEN, BLUE }\n"
						+ "record ColoredPoint(Point p, Color c) {}\n"
						+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		// working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			String str = this.workingCopies[0].getSource();
			String selection = "/*here*/xyz";
			int start = str.indexOf(selection);
			int length = selection.length();

			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			ILocalVariable local = (ILocalVariable) elements[0];
			search(local, REFERENCES, EXACT_RULE);
			assertSearchResults("src/X.java void X.printLowerRight(Rectangle) [xyz] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	// record pattern - if search for enum in "valid record pattern and make the pattern variable available in switch expression" works
	// Enable with fix of https://github.com/eclipse-jdt/eclipse.jdt.core/issues/785
	public void _testIssue215_007() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"@SuppressWarnings(\"preview\")"
						+ "public class X {\n"
						+ "  public static void printLowerRight(Rectangle r) {\n"
						+ "    int res = switch(r) {\n"
						+ "       case Rectangle(ColoredPoint(Point(int xyz, int y), Color /*here*/c1),\n"
						+ "                               ColoredPoint lr) r11  -> {\n"
						+ "             System.out.println(c1);\n"
						+ "        		yield 1;  \n"
						+ "        } \n"
						+ "        default -> 0;\n"
						+ "    }; \n"
						+ "    System.out.println(res);\n"
						+ "  }\n"
						+ "  public static void main(String[] args) {\n"
						+ "    printLowerRight(new Rectangle(new ColoredPoint(new Point(15, 5), Color.BLUE), \n"
						+ "        new ColoredPoint(new Point(30, 10), Color.RED)));\n"
						+ "  }\n"
						+ "}\n"
						+ "record Point(int x, int y) {}\n"
						+ "enum Color { RED, GREEN, BLUE }\n"
						+ "record ColoredPoint(Point p, Color c) {}\n"
						+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		// working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			String str = this.workingCopies[0].getSource();
			String selection = "/*here*/c1";
			int start = str.indexOf(selection);
			int length = selection.length();

			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			ILocalVariable local = (ILocalVariable) elements[0];
			search(local, REFERENCES, EXACT_RULE);
			assertSearchResults("src/X.java void X.printLowerRight(Rectangle) [c1] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	// record pattern - if search for record  in "valid record pattern and make the pattern variable available in switch expression" works
	public void testIssue215_008() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"@SuppressWarnings(\"preview\")"
						+ "public class X {\n"
						+ "  public static void printLowerRight(Rectangle r) {\n"
						+ "    int res = switch(r) {\n"
						+ "       case Rectangle(ColoredPoint(Point(int xyz, int y), Color c1),\n"
						+ "                               ColoredPoint lr) r11  -> {\n"
						+ "             System.out.println(c1);\n"
						+ "        		yield 1;  \n"
						+ "        } \n"
						+ "        default -> 0;\n"
						+ "    }; \n"
						+ "    System.out.println(res);\n"
						+ "  }\n"
						+ "  public static void main(String[] args) {\n"
						+ "    printLowerRight(new Rectangle(new ColoredPoint(new Point(15, 5), Color.BLUE), \n"
						+ "        new ColoredPoint(new Point(30, 10), Color.RED)));\n"
						+ "  }\n"
						+ "}\n"
						+ "record /*here*/Point(int x, int y) {}\n"
						+ "enum Color { RED, GREEN, BLUE }\n"
						+ "record ColoredPoint(Point p, Color c) {}\n"
						+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		// working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			String str = this.workingCopies[0].getSource();
			String selection = "/*here*/Point";
			int start = str.indexOf(selection);
			int length = selection.length();

			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			SourceType local = (SourceType) elements[0];
			search(local, REFERENCES, EXACT_RULE);
			assertSearchResults("src/X.java void X.printLowerRight(Rectangle) [Point] EXACT_MATCH\n"
					+ "src/X.java void X.main(String[]) [Point] EXACT_MATCH\n"
					+ "src/X.java void X.main(String[]) [Point] EXACT_MATCH\n"
					+ "src/X.java ColoredPoint.p [Point] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	// record pattern - if search for another record in nested "valid record pattern and make the pattern variable available in switch expression" works
	public void testIssue215_009() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"@SuppressWarnings(\"preview\")"
						+ "public class X {\n"
						+ "  public static void printLowerRight(Rectangle r) {\n"
						+ "    int res = switch(r) {\n"
						+ "       case Rectangle(ColoredPoint(Point(int xyz, int y), Color c1),\n"
						+ "                               ColoredPoint lr) r11  -> {\n"
						+ "             System.out.println(c1);\n"
						+ "        		yield 1;  \n"
						+ "        } \n"
						+ "        default -> 0;\n"
						+ "    }; \n"
						+ "    System.out.println(res);\n"
						+ "  }\n"
						+ "  public static void main(String[] args) {\n"
						+ "    printLowerRight(new Rectangle(new ColoredPoint(new Point(15, 5), Color.BLUE), \n"
						+ "        new ColoredPoint(new Point(30, 10), Color.RED)));\n"
						+ "  }\n"
						+ "}\n"
						+ "record /*here*/Point(int x, int y) {}\n"
						+ "enum Color { RED, GREEN, BLUE }\n"
						+ "record /*here*/ColoredPoint(Point p, Color c) {}\n"
						+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		// working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			String str = this.workingCopies[0].getSource();
			String selection = "/*here*/ColoredPoint";
			int start = str.indexOf(selection);
			int length = selection.length();

			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			SourceType local = (SourceType) elements[0];
			search(local, REFERENCES, EXACT_RULE);
			assertSearchResults("src/X.java void X.printLowerRight(Rectangle) [ColoredPoint] EXACT_MATCH\n"
					+ "src/X.java void X.printLowerRight(Rectangle) [ColoredPoint] EXACT_MATCH\n"
					+ "src/X.java void X.main(String[]) [ColoredPoint] EXACT_MATCH\n"
					+ "src/X.java void X.main(String[]) [ColoredPoint] EXACT_MATCH\n"
					+ "src/X.java Rectangle.upperLeft [ColoredPoint] EXACT_MATCH\n"
					+ "src/X.java Rectangle.lowerRight [ColoredPoint] EXACT_MATCH"
					+ "");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	// record pattern - if search for another record in nested "valid record pattern and make the pattern variable available in switch expression" works
	public void testIssue215_010() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"@SuppressWarnings(\"preview\")"
						+ "public class X {\n"
						+ "  public static void printLowerRight(Rectangle r) {\n"
						+ "    int res = switch(r) {\n"
						+ "       case Rectangle(ColoredPoint(Point(int xyz, int y), Color c1),\n"
						+ "                               ColoredPoint lr) r11  -> {\n"
						+ "             System.out.println(c1);\n"
						+ "        		yield 1;  \n"
						+ "        } \n"
						+ "        default -> 0;\n"
						+ "    }; \n"
						+ "    System.out.println(res);\n"
						+ "  }\n"
						+ "  public static void main(String[] args) {\n"
						+ "    printLowerRight(new Rectangle(new ColoredPoint(new Point(15, 5), Color.BLUE), \n"
						+ "        new ColoredPoint(new Point(30, 10), Color.RED)));\n"
						+ "  }\n"
						+ "}\n"
						+ "record Point(int x, int y) {}\n"
						+ "enum /*here*/Color { RED, GREEN, BLUE }\n"
						+ "record ColoredPoint(Point p, Color c) {}\n"
						+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		// working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			String str = this.workingCopies[0].getSource();
			String selection = "/*here*/Color";
			int start = str.indexOf(selection);
			int length = selection.length();

			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			SourceType local = (SourceType) elements[0];
			search(local, REFERENCES, EXACT_RULE);
			assertSearchResults("src/X.java void X.printLowerRight(Rectangle) [Color] EXACT_MATCH\n"
					+ "src/X.java void X.main(String[]) [Color] EXACT_MATCH\n"
					+ "src/X.java void X.main(String[]) [Color] EXACT_MATCH\n"
					+ "src/X.java ColoredPoint.c [Color] EXACT_MATCH"
					+ ""
					+ "");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	// record pattern - just check if all occurence search for record pattern local declaration works
	// Enable with fix of https://github.com/eclipse-jdt/eclipse.jdt.core/issues/785
	public void _testIssue215_011() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"@SuppressWarnings(\"preview\")"
						+ "public class X {\n"
						+ "  public static void printLowerRight(Rectangle r) {\n"
						+ "    int res = switch(r) {\n"
						+ "       case Rectangle(ColoredPoint(Point(int x, int y), Color c),\n"
						+ "                               ColoredPoint /*here*/lr) r1  -> {\n"
						+ "    				System.out.println(\"x= \" + x);\n"
						+ "    				System.out.println(\"y= \" + y);\n"
						+ "    				System.out.println(\"lr= \" + lr);\n"
						+ "    				System.out.println(\"lr.c()= \" + lr.c());\n"
						+ "    				System.out.println(\"lr.p()= \" + lr.p());\n"
						+ "    				System.out.println(\"lr.p().x()= \" + lr.p().x());\n"
						+ "    				System.out.println(\"lr.p().y()= \" + lr.p().y());\n"
						+ "    				System.out.println(\"c= \" + c);\n"
						+ "    				System.out.println(\"r1= \" + r1);\n"
						+ "        		yield x;  \n"
						+ "        } \n"
						+ "        default -> 0;\n"
						+ "    }; \n"
						+ "    System.out.println(\"Returns: \" + res);\n"
						+ "  }\n"
						+ "  public static void main(String[] args) {\n"
						+ "    printLowerRight(new Rectangle(new ColoredPoint(new Point(15, 5), Color.BLUE), \n"
						+ "        new ColoredPoint(new Point(30, 10), Color.RED)));\n"
						+ "  }\n"
						+ "}\n"
						+ "record Point(int x, int y) {}\n"
						+ "enum Color { RED, GREEN, BLUE }\n"
						+ "record ColoredPoint(Point p, Color c) {}\n"
						+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		// working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			String str = this.workingCopies[0].getSource();
			String selection = "/*here*/lr";
			int start = str.indexOf(selection);
			int length = selection.length();

			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			ILocalVariable local = (ILocalVariable) elements[0];
			search(local, REFERENCES, EXACT_RULE);
			assertSearchResults(
					"src/X.java void X.printLowerRight(Rectangle) [lr] EXACT_MATCH\n"
							+ "src/X.java void X.printLowerRight(Rectangle) [lr] EXACT_MATCH\n"
							+ "src/X.java void X.printLowerRight(Rectangle) [lr] EXACT_MATCH\n"
							+ "src/X.java void X.printLowerRight(Rectangle) [lr] EXACT_MATCH\n"
							+ "src/X.java void X.printLowerRight(Rectangle) [lr] EXACT_MATCH"
							+ "");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	// record pattern - just check if search for record pattern local declaration works - another example
	// Enable with fix of https://github.com/eclipse-jdt/eclipse.jdt.core/issues/785
	public void _testIssue215_0012() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"@SuppressWarnings(\"preview\")"
						+ "public class X {\n"
						+ "	 @SuppressWarnings(\"preview\")\n"
						+ "	public static void print(Pair p) {\n"
						+ "    if (p instanceof Pair(Teacher(Object n), Student(Object n1, Integer i)) /*here*/r1) {                \n"
						+ "			 System.out.println(n1.getClass().getTypeName() + \":\" + n1 + \",\" + r1); \n"
						+ "			 System.out.println(\"MORE\" + r1);\n"
						+ "		 } else {                         \n"
						+ "			 System.out.println(\"ELSE\");\n"
						+ "		 } \n"
						+ "  }\n"
						+ "  public static void main(String[] args) {\n"
						+ "    print(new Pair(new Teacher(\"123\"), new Student(\"abc\", 10)));\n"
						+ "  }\n"
						+ "}\n"
						+ "sealed interface Person permits Student, Teacher {\n"
						+ "    String name();\n"
						+ "}\n"
						+ " record Student(String name, Integer id) implements Person {}\n"
						+ " record Teacher(String name) implements Person {}\n"
						+ " record Pair(Person s, Person s1) {}\n"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		// working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			String str = this.workingCopies[0].getSource();
			String selection = "/*here*/r1";
			int start = str.indexOf(selection);
			int length = selection.length();

			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			ILocalVariable local = (ILocalVariable) elements[0];
			search(local, REFERENCES, EXACT_RULE);
			assertSearchResults("src/X.java void X.print(Pair) [r1] EXACT_MATCH\n"
					+ "src/X.java void X.print(Pair) [r1] EXACT_MATCH"
					+ "");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	public void testIssue344_001() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public class X {\n"
						+ "  static void print(Rectangle r) {\n"
						+ "    if (r instanceof (Rectangle(ColoredPoint(Point(int x, int y), Color c),\n"
						+ "                               ColoredPoint lr) r11)) {\n"
						+ "        System.out.println(\"Upper-left corner: \" + /*here*/lr);\n"
						+ "    }\n"
						+ "  }\n"
						+ "  public static void main(String[] obj) {\n"
						+ "    print(new Rectangle(new ColoredPoint(new Point(0, 0), Color.BLUE), \n"
						+ "                               new ColoredPoint(new Point(10, 15), Color.RED)));\n"
						+ "  }\n"
						+ "}\n"
						+ "record Point(int x, int y) {}\n"
						+ "enum Color { RED, GREEN, BLUE }\n"
						+ "record ColoredPoint(Point p, Color c) {}\n"
						+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		// working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			String str = this.workingCopies[0].getSource();
			String selection = "/*here*/lr";
			int start = str.indexOf(selection);
			int length = selection.length();

			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			ILocalVariable local = (ILocalVariable) elements[0];
			search(local, DECLARATIONS, EXACT_RULE);
			assertSearchResults("src/X.java void X.print(Rectangle).lr [lr] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	public void testIssue344_002() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public class X {\n"
						+ "  static void print(Rectangle r) {\n"
						+ "switch (r) {\n"
						+ "case Rectangle r1 when (r instanceof (Rectangle(ColoredPoint upperLeft2, ColoredPoint lowerRight))):\n"
						+ "	System.out.println( /*here*/upperLeft2);\n"
						+ "	break;\n"
						+ "	default :\n"
						+ "		break;\n"
						+ "	} \n"
						+ "  }\n"
						+ "  public static void main(String[] obj) {\n"
						+ "    print(new Rectangle(new ColoredPoint(new Point(0, 0), Color.BLUE), \n"
						+ "                               new ColoredPoint(new Point(10, 15), Color.RED)));\n"
						+ "  }\n"
						+ "}\n"
						+ "record Point(int x, int y) {}\n"
						+ "enum Color { RED, GREEN, BLUE }\n"
						+ "record ColoredPoint(Point p, Color c) {}\n"
						+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		// working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			String str = this.workingCopies[0].getSource();
			String selection = "/*here*/upperLeft2";
			int start = str.indexOf(selection);
			int length = selection.length();

			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			ILocalVariable local = (ILocalVariable) elements[0];
			search(local, DECLARATIONS, EXACT_RULE);
			assertSearchResults("src/X.java void X.print(Rectangle).upperLeft2 [upperLeft2] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	public void testIssue708_1() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public class Test1 {\n"
				+ "	public void test(Type type, String string) {\n"
				+ "		switch (type) {\n"
				+ "		case openDeclarationFails -> {\n"
				+ "			switch (string) {\n"
				+ "			case \"Test\" -> method(Type.openDeclarationFails);\n"
				+ "			}\n"
				+ "			method(Type.openDeclarationFails);\n"
				+ "		}\n"
				+ "		case anotherValue -> {\n"
				+ "			switch (string) {\n"
				+ "			case \"Test\" -> method(Type.anotherValue);\n"
				+ "			}\n"
				+ "		}\n"
				+ "		}\n"
				+ "	}\n"
				+ "	private void method(Type relay) {}\n"
				+ "	static public enum Type {\n"
				+ "		openDeclarationFails, anotherValue;\n"
				+ "	}\n"
				+ "}"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			String str = this.workingCopies[0].getSource();
			String selection = "Type.openDeclarationFails";
			int start = str.indexOf(selection);
			int length = selection.length();

			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			IField local = (IField) elements[0];
			search(local, DECLARATIONS, EXACT_RULE);
			assertSearchResults("src/X.java Test1$Type.openDeclarationFails [openDeclarationFails] EXACT_MATCH");

			str = this.workingCopies[0].getSource();
			selection = "Type.anotherValue";
			start = str.indexOf(selection);
			length = selection.length();

			elements = this.workingCopies[0].codeSelect(start, length);
			local = (IField) elements[0];
			this.resultCollector.clear();
			search(local, DECLARATIONS, EXACT_RULE);
			assertSearchResults("src/X.java Test1$Type.anotherValue [anotherValue] EXACT_MATCH");

			str = this.workingCopies[0].getSource();
			selection = "openDeclarationFails";
			start = str.lastIndexOf(selection);
			length = selection.length();

			elements = this.workingCopies[0].codeSelect(start, length);
			local = (IField) elements[0];
			this.resultCollector.clear();
			search(local, ALL_OCCURRENCES, EXACT_RULE);
			assertSearchResults("src/X.java void Test1.test(Type, String) [openDeclarationFails] EXACT_MATCH\n" +
					"src/X.java void Test1.test(Type, String) [openDeclarationFails] EXACT_MATCH\n" +
					"src/X.java void Test1.test(Type, String) [openDeclarationFails] EXACT_MATCH\n" +
					"src/X.java Test1$Type.openDeclarationFails [openDeclarationFails] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
}

