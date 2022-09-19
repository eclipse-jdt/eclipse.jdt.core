/*******************************************************************************
 * Copyright (c) 2021 IBM Corporation and others.
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
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.ReferenceMatch;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeReferenceMatch;
import org.eclipse.jdt.internal.core.LocalVariable;
import org.eclipse.jdt.internal.core.SourceField;

import junit.framework.Test;

public class JavaSearchBugs17Tests extends AbstractJavaSearchTests {

	static {
		// org.eclipse.jdt.internal.core.search.BasicSearchEngine.VERBOSE = true;
		// TESTS_NUMBERS = new int[] { 19 };
		// TESTS_RANGE = new int[] { 1, -1 };
		// TESTS_NAMES = new String[] {"testBug542559_001"};
	}

	public JavaSearchBugs17Tests(String name) {
		super(name);
		this.endChar = "";
	}

	public static Test suite() {
		return buildModelTestSuite(JavaSearchBugs17Tests.class, BYTECODE_DECLARATION_ORDER);
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

	// switch pattern search - test reference of an object in case statement
	public void testBug573943_001() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public class X {\n" +
						"public static void main(String[] args) {\n" +
						"foo(Integer.valueOf(5));\n" +
						"foo(new Object());\n" +
						"}\n" +
						"private static void foo(Object o) {\n" +
						" switch (o) {\n" +
						"	case Integer i     -> System.out.println(\"Integer:\" + i);\n" +
						"	case String /*here*/s     -> System.out.println(\"String:\" + s + s);\n" +
						"	default       -> System.out.println(\"Object\" + o);\n" +
						" 	}\n" +
						"}\n" +
						"}\n"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		// working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			String str = this.workingCopies[0].getSource();
			String selection = "/*here*/s";
			int start = str.indexOf(selection);
			int length = selection.length();

			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			ILocalVariable local = (ILocalVariable) elements[0];
			search(local, REFERENCES, EXACT_RULE);
			assertSearchResults("src/X.java void X.foo(Object) [s] EXACT_MATCH\n"
					+ "src/X.java void X.foo(Object) [s] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	// find reference on a field in switch pattern
	public void testBug573943_002() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public class X {\n" +
						"public static  int /*here*/field \n" +
						"public static void main(String[] args) {\n" +
						"foo(Integer.valueOf(5));\n" +
						"foo(new Object());\n" +
						"}\n" +
						"private static void foo(Object o) {\n" +
						" switch (o) {\n" +
						"	case Integer i   -> System.out.println(\"Integer:\" + i);\n" +
						"	case String s     -> System.out.println(\"String:\" + s + field);\n" +
						"	default       -> System.out.println(\"Object\" + o);\n" +
						" 	}\n" +
						"}\n" +
						"}\n"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		// working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			String str = this.workingCopies[0].getSource();
			String selection = "/*here*/field";
			int start = str.indexOf(selection);
			int length = selection.length();

			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			SourceField field = (SourceField) elements[0];
			search(field, REFERENCES, EXACT_RULE);
			assertSearchResults("src/X.java void X.foo(Object) [field] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	// find all occurrence of a field in switch pattern
	public void testBug573943_003() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public class X {\n" +
						"public static  int /*here*/field \n" +
						"public static void main(String[] args) {\n" +
						"foo(Integer.valueOf(5));\n" +
						"foo(new Object());\n" +
						"}\n" +
						"private static void foo(Object o) {\n" +
						" switch (o) {\n" +
						"	case Integer i   -> System.out.println(\"Integer:\" + i);\n" +
						"	case String s     -> System.out.println(\"String:\" + s + field);\n" +
						"	default       -> System.out.println(\"Object\" + o);\n" +
						" 	}\n" +
						"}\n" +
						"}\n"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		// working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			String str = this.workingCopies[0].getSource();
			String selection = "/*here*/field";
			int start = str.indexOf(selection);
			int length = selection.length();

			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			SourceField field = (SourceField) elements[0];
			search(field, ALL_OCCURRENCES, EXACT_RULE);
			assertSearchResults("src/X.java X.field [field] EXACT_MATCH\n"
					+ "src/X.java void X.foo(Object) [field] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}

	// find all reference of a local variable in switch pattern
	public void testBug573943_004() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public class X {\n" +
						"public static  int /*here*/field \n" +
						"public static void main(String[] args) {\n" +
						"foo(Integer.valueOf(5));\n" +
						"foo(new Object());\n" +
						"}\n" +
						"private static void foo(Object o) {\n" +
						" int /*here*/local=0" +
						" switch (o) {\n" +
						"	case Integer i   -> System.out.println(\"Integer:\" + i);\n" +
						"	case String s     -> System.out.println(\"String:\" + s + local);\n" +
						"	default       -> System.out.println(\"Object\" + o);\n" +
						" 	}\n" +
						"}\n" +
						"}\n"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		// working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			String str = this.workingCopies[0].getSource();
			String selection = "/*here*/local";
			int start = str.indexOf(selection);
			int length = selection.length();

			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			ILocalVariable field = (ILocalVariable) elements[0];
			search(field, REFERENCES, EXACT_RULE);
			assertSearchResults("src/X.java void X.foo(Object) [local] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	// find all reference of a local variable in switch pattern
	public void testBug573943_005() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public class X {\n" +
						"public static  int /*here*/field \n" +
						"public static void main(String[] args) {\n" +
						"foo(Integer.valueOf(5));\n" +
						"foo(new Object());\n" +
						"}\n" +
						"private static void foo(Object o) {\n" +
						" int /*here*/local=0" +
						" switch (o) {\n" +
						"	case Integer i   -> System.out.println(\"Integer:\" + i +local);\n" +
						"	case String s     -> System.out.println(\"String:\" + s + local);\n" +
						"	default       -> System.out.println(\"Object\" + o);\n" +
						" 	}\n" +
						"}\n" +
						"}\n"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		// working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			String str = this.workingCopies[0].getSource();
			String selection = "/*here*/local";
			int start = str.indexOf(selection);
			int length = selection.length();

			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			ILocalVariable field = (ILocalVariable) elements[0];
			search(field, ALL_OCCURRENCES, EXACT_RULE);
			assertSearchResults("src/X.java void X.foo(Object).local [local] EXACT_MATCH\n"
					+ "src/X.java void X.foo(Object) [local] EXACT_MATCH\n"
					+ "src/X.java void X.foo(Object) [local] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	// find all occurrence of a field in switch case statement and switch expression
	public void testBug573943_006() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public class X {\n" +
						"public static  int /*here*/field \n" +
						"public static void main(String[] args) {\n" +
						"foo(Integer.valueOf(5));\n" +
						"foo(new Object());\n" +
						"}\n" +
						"private static void foo(Object o) {\n" +
						" switch (o) {\n" +
						"	case Integer i && field > 0  -> System.out.println(\"Integer:\" + i);\n" +
						"	case String s     -> System.out.println(\"String:\" + s + field);\n" +
						"	default       -> System.out.println(\"Object\" + o);\n" +
						" 	}\n" +
						"}\n" +
						"}\n"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		// working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			String str = this.workingCopies[0].getSource();
			String selection = "/*here*/field";
			int start = str.indexOf(selection);
			int length = selection.length();

			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			SourceField field = (SourceField) elements[0];
			search(field, ALL_OCCURRENCES, EXACT_RULE);
			assertSearchResults("src/X.java X.field [field] EXACT_MATCH\n"
					+ "src/X.java void X.foo(Object) [field] EXACT_MATCH\n"
					+ "src/X.java void X.foo(Object) [field] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	// find all reference of a local variable in switch pattern amd case statements
	public void testBug573943_007() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public class X {\n" +
						"public static  int /*here*/field \n" +
						"public static void main(String[] args) {\n" +
						"foo(Integer.valueOf(5));\n" +
						"foo(new Object());\n" +
						"}\n" +
						"private static void foo(Object o) {\n" +
						" int /*here*/local=0" +
						" switch (o) {\n" +
						"	case Integer i when local >9  -> System.out.println(\"Integer:\" + i +local);\n" +
						"	case String s     -> System.out.println(\"String:\" + s + local);\n" +
						"	default       -> System.out.println(\"Object\" + o);\n" +
						" 	}\n" +
						"}\n" +
						"}\n"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		// working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			String str = this.workingCopies[0].getSource();
			String selection = "/*here*/local";
			int start = str.indexOf(selection);
			int length = selection.length();

			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			ILocalVariable field = (ILocalVariable) elements[0];
			search(field, ALL_OCCURRENCES, EXACT_RULE);
			assertSearchResults("src/X.java void X.foo(Object).local [local] EXACT_MATCH\n"
					+ "src/X.java void X.foo(Object) [local] EXACT_MATCH\n"
					+ "src/X.java void X.foo(Object) [local] EXACT_MATCH\n"
					+ "src/X.java void X.foo(Object) [local] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	// switch pattern search - test reference of an object in case statement as well as switch expression
	public void testBug573943_008() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public class X {\n" +
						"public static void main(String[] args) {\n" +
						"foo(Integer.valueOf(5));\n" +
						"foo(new Object());\n" +
						"}\n" +
						"private static void foo(Object o) {\n" +
						" switch (o) {\n" +
						"	case Integer i     -> System.out.println(\"Integer:\" + i);\n" +
						"	case String /*here*/s when s.hashCode()>0    -> System.out.println(\"String:\" );\n" +
						"	default       -> System.out.println(\"Object\" + o);\n" +
						" 	}\n" +
						"}\n" +
						"}\n"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		// working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			String str = this.workingCopies[0].getSource();
			String selection = "/*here*/s";
			int start = str.indexOf(selection);
			int length = selection.length();

			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			ILocalVariable local = (ILocalVariable) elements[0];
			search(local, REFERENCES, EXACT_RULE);
			assertSearchResults("src/X.java void X.foo(Object) [s] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}

	// switch pattern search - test reference of an custom class object in case statement
	public void testBug573943_009() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"sealed interface I permits A, B {}\n" +
						"final class A implements S {}\n" +
						"final class B implements S {}\n" +
						"public class X {\n" +
						"public static void main(String[] args) {\n" +
						"foo(new A());\n" +
						"}\n" +
						"private static void foo(S o) {\n" +
						" switch (o) {\n" +
						"	case A /*here*/a :     System.out.println(\"A:\" + a +a); break;\n" +
						"	case B b :     System.out.println(\"B:\" + b);\n" +
						"	default  : System.out.println(\"Object\" + o);\n" +
						" 	}\n" +
						"}\n" +
						"}\n"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		// working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			String str = this.workingCopies[0].getSource();
			String selection = "/*here*/a";
			int start = str.indexOf(selection);
			int length = selection.length();

			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			ILocalVariable local = (ILocalVariable) elements[0];
			search(local, REFERENCES, EXACT_RULE);
			assertSearchResults("src/X.java void X.foo(S) [a] EXACT_MATCH\n"
					+ "src/X.java void X.foo(S) [a] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	// switch pattern search - test reference of an custom class object in case statement amd switch statement
	public void testBug573943_010() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"sealed interface I permits A, B {}\n" +
						"final class A implements S {}\n" +
						"final class B implements S {}\n" +
						"public class X {\n" +
						"public static void main(String[] args) {\n" +
						"foo(new A());\n" +
						"}\n" +
						"private static void foo(S o) {\n" +
						" switch (o) {\n" +
						"	case A /*here*/a when a.hashCode()> 0 :     System.out.println(\"A:\" + a +a); break;\n" +
						"	case B b :     System.out.println(\"B:\" + b);\n" +
						"	default  :     System.out.println(\"Object\" + o);\n" +
						" 	}\n" +
						"}\n" +
						"}\n"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		// working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			String str = this.workingCopies[0].getSource();
			String selection = "/*here*/a";
			int start = str.indexOf(selection);
			int length = selection.length();

			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			ILocalVariable local = (ILocalVariable) elements[0];
			search(local, REFERENCES, EXACT_RULE);
			assertSearchResults("src/X.java void X.foo(S) [a] EXACT_MATCH\n"
					+ "src/X.java void X.foo(S) [a] EXACT_MATCH\n"
					+ "src/X.java void X.foo(S) [a] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	// switch pattern search - test reference of an custom record object in case statement
	public void testBug573943_011() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"sealed interface I permits A, B {}\n" +
						"record A (int i) implements S {}\n" +
						"record B (int i) implements S {}\n" +
						"public class X {\n" +
						"public static void main(String[] args) {\n" +
						"foo(new A());\n" +
						"}\n" +
						"private static void foo(S o) {\n" +
						" switch (o) {\n" +
						"	case A /*here*/a :     System.out.println(\"A:\" + a +a); break;\n" +
						"	case B b :     System.out.println(\"B:\" + b);break;\n" +
						"	default   :    System.out.println(\"Object\" + o);\n" +
						" 	}\n" +
						"}\n" +
						"}\n"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		// working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			String str = this.workingCopies[0].getSource();
			String selection = "/*here*/a";
			int start = str.indexOf(selection);
			int length = selection.length();

			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			ILocalVariable local = (ILocalVariable) elements[0];
			search(local, REFERENCES, EXACT_RULE);
			assertSearchResults("src/X.java void X.foo(S) [a] EXACT_MATCH\n"
					+ "src/X.java void X.foo(S) [a] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	// switch pattern search - test reference of an custom record object in case statement and switch statement
	public void testBug573943_012() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"sealed interface I permits A, B {}\n" +
						"record A (int i) implements S {}\n" +
						"record B (int i) implements S {}\n" +
						"public class X {\n" +
						"public static void main(String[] args) {\n" +
						"foo(new A());\n" +
						"}\n" +
						"private static void foo(S o) {\n" +
						" switch (o) {\n" +
						"	case A /*here*/a when a.hashCode() :     System.out.println(\"A:\" + a +a); break;\n" +
						"	case B b :     System.out.println(\"B:\" + b);\n" +
						"	default  :     System.out.println(\"Object\" + o);\n" +
						" 	}\n" +
						"}\n" +
						"}\n"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		// working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			String str = this.workingCopies[0].getSource();
			String selection = "/*here*/a";
			int start = str.indexOf(selection);
			int length = selection.length();

			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			ILocalVariable local = (ILocalVariable) elements[0];
			search(local, REFERENCES, EXACT_RULE);
			assertSearchResults("src/X.java void X.foo(S) [a] EXACT_MATCH\n"
					+ "src/X.java void X.foo(S) [a] EXACT_MATCH\n"
					+ "src/X.java void X.foo(S) [a] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	// switch pattern search - test reference of an custom class object in case statement and switch expression
	public void testBug573943_013() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"sealed interface I permits A, B {}\n" +
						"final class A implements S {}\n" +
						"final class B implements S {}\n" +
						"public class X {\n" +
						"public static void main(String[] args) {\n" +
						"foo(new A());\n" +
						"}\n" +
						"private static int foo(S o) {\n" +
						" switch (o) {\n" +
						"	case A /*here*/a when a.hashCode()> 0 -> 1;\n" +
						"	case B b ->2;\n" +
						"	default  -> 3;\n" +
						" 	}\n" +
						"}\n" +
						"}\n"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		// working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			String str = this.workingCopies[0].getSource();
			String selection = "/*here*/a";
			int start = str.indexOf(selection);
			int length = selection.length();

			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			ILocalVariable local = (ILocalVariable) elements[0];
			search(local, REFERENCES, EXACT_RULE);
			assertSearchResults("src/X.java int X.foo(S) [a] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	// switch pattern search - test reference of an object in case statement as well as switch pattern
	public void testBug573943_014() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public class X {\n" +
						"public static void main(String[] args) {\n" +
						"foo(Integer.valueOf(5));\n" +
						"foo(new Object());\n" +
						"}\n" +
						"private static void foo(Object o) {\n" +
						" switch (o) {\n" +
						"	case Integer i     : System.out.println(\"Integer:\" + i);break;\n" +
						"	case String /*here*/s     : System.out.println(\"String:\" + s + s);break;\n" +
						"	default       : System.out.println(\"Object\" + o);\n" +
						" 	}\n" +
						"}\n" +
						"}\n"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		// working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			String str = this.workingCopies[0].getSource();
			System.out.println(str);
			String selection = "/*here*/s";
			int start = str.indexOf(selection);
			int length = selection.length();

			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			ILocalVariable local = (ILocalVariable) elements[0];
			search(local, REFERENCES, EXACT_RULE);
			assertSearchResults("src/X.java void X.foo(Object) [s] EXACT_MATCH\n"
					+ "src/X.java void X.foo(Object) [s] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	// switch pattern search - test reference of an object in case statement as well as switch pattern
	public void testBug573943_015() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public class X {\n" +
						"public static void main(String[] args) {\n" +
						"foo(Integer.valueOf(5));\n" +
						"foo(new Object());\n" +
						"}\n" +
						"private static void foo(Object o) {\n" +
						" switch (o) {\n" +
						"	case Integer i     : System.out.println(\"Integer:\" + i);break;\n" +
						"	case String /*here*/s when s.hashCode()>0    : System.out.println(\"String:\" );break;\n" +
						"	default       : System.out.println(\"Object\" + o);\n" +
						" 	}\n" +
						"}\n" +
						"}\n"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		// working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			String str = this.workingCopies[0].getSource();
			String selection = "/*here*/s";
			int start = str.indexOf(selection);
			int length = selection.length();

			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			ILocalVariable local = (ILocalVariable) elements[0];
			search(local, REFERENCES, EXACT_RULE);
			assertSearchResults("src/X.java void X.foo(Object) [s] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	// type reference with a switch expression pattern
	// see testBug573943_012 for switch pattern and type reference
	public void testBug573943_016() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"sealed interface I permits A, B {}\n" +
						"final class A implements S {}\n" +
						"final class B implements S {}\n" +
						"public class X {\n" +
						"public static void main(String[] args) {\n" +
						"foo(new A());\n" +
						"}\n" +
						"private static void foo(S o) {\n" +
						" switch (o) {\n" +
						"	case A /*here*/a ->     System.out.println(\"A:\" + a +a); \n" +
						"	case B b ->    System.out.println(\"B:\" + b);\n" +
						"	default  -> System.out.println(\"Object\" + o);\n" +
						" 	}\n" +
						"}\n" +
						"}\n"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		// working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			String str = this.workingCopies[0].getSource();
			String selection = "/*here*/a";
			int start = str.indexOf(selection);
			int length = selection.length();

			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			ILocalVariable local = (ILocalVariable) elements[0];
			search(local, REFERENCES, EXACT_RULE);
			assertSearchResults("src/X.java void X.foo(S) [a] EXACT_MATCH\n"
					+ "src/X.java void X.foo(S) [a] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}

	// type reference with a switch expression pattern
	public void testBug573943_017() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"sealed interface I permits A, B {}\n" +
						"final class A implements S {}\n" +
						"final class B implements S {}\n" +
						"public class X {\n" +
						"public static void main(String[] args) {\n" +
						"foo(new A());\n" +
						"}\n" +
						"private static void foo(S o) {\n" +
						" switch (o) {\n" +
						"	case A /*here*/a when a.toString().length()>2 ->     System.out.println(\"A:\" + a +a); \n" +
						"	case B b ->    System.out.println(\"B:\" + b);\n" +
						"	default  -> System.out.println(\"Object\" + o);\n" +
						" 	}\n" +
						"}\n" +
						"}\n"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		// working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			String str = this.workingCopies[0].getSource();
			String selection = "/*here*/a";
			int start = str.indexOf(selection);
			int length = selection.length();

			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			ILocalVariable local = (ILocalVariable) elements[0];
			search(local, REFERENCES, EXACT_RULE);
			assertSearchResults("src/X.java void X.foo(S) [a] EXACT_MATCH\n"
					+"src/X.java void X.foo(S) [a] EXACT_MATCH\n"
					+ "src/X.java void X.foo(S) [a] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	// selection of pattern variable in case statement and verify that it is local variable
	public void testBug573943_018() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public class X {\n" +
						"public static void main(String[] args) {\n" +
						"foo(Integer.valueOf(5));\n" +
						"foo(new Object());\n" +
						"}\n" +
						"private static void foo(Object o) {\n" +
						" switch (o) {\n" +
						"	case Integer i     -> System.out.println(\"Integer:\" + i);\n" +
						"	case String s     -> System.out.println(\"String:\" + /*here*/s + s);\n" +
						"	default       -> System.out.println(\"Object\" + o);\n" +
						" 	}\n" +
						"}\n" +
						"}\n"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		// working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			String str = this.workingCopies[0].getSource();
			String selection = "/*here*/s";
			int start = str.indexOf(selection);
			int length = selection.length();

			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			assertTrue(elements[0] instanceof LocalVariable);

		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	// selection of pattern variable in case statement and search for declaration
	public void testBug573943_019() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public class X {\n" +
						"public static void main(String[] args) {\n" +
						"foo(Integer.valueOf(5));\n" +
						"foo(new Object());\n" +
						"}\n" +
						"private static void foo(Object o) {\n" +
						" switch (o) {\n" +
						"	case Integer i     -> System.out.println(\"Integer:\" + i);\n" +
						"	case String s     -> System.out.println(\"String:\" + /*here*/s + s);\n" +
						"	default       -> System.out.println(\"Object\" + o);\n" +
						" 	}\n" +
						"}\n" +
						"}\n"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		// working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			String str = this.workingCopies[0].getSource();
			String selection = "/*here*/s";
			int start = str.indexOf(selection);
			int length = selection.length();

			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			ILocalVariable local = (ILocalVariable) elements[0];
			search(local, DECLARATIONS, EXACT_RULE);
			assertSearchResults("src/X.java void X.foo(Object).s [s] EXACT_MATCH");

		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	// selection of guarded pattern variable in case statement and verify that it is local variable
	public void testBug573943_020() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public class X {\n" +
						"public static void main(String[] args) {\n" +
						"foo(Integer.valueOf(5));\n" +
						"foo(new Object());\n" +
						"}\n" +
						"private static void foo(Object o) {\n" +
						" final int a=0; \n" +
						" switch (o) {\n" +
						"	case Integer i  when a > 5  -> System.out.println(\"Integer:\" + /*here*/i);\n" +
						"	case String s     -> System.out.println(\"String:\" + s + s);\n" +
						"	default       -> System.out.println(\"Object\" + o);\n" +
						" 	}\n" +
						"}\n" +
						"}\n"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		// working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			String str = this.workingCopies[0].getSource();
			String selection = "/*here*/i";
			int start = str.indexOf(selection);
			int length = selection.length();

			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			assertTrue(elements[0] instanceof LocalVariable);


		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	// selection of pattern variable in case statement and search for declaration
	public void testBug573943_021() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public class X {\n" +
						"public static void main(String[] args) {\n" +
						"foo(Integer.valueOf(5));\n" +
						"foo(new Object());\n" +
						"}\n" +
						"private static void foo(Object o) {\n" +
						" final int a=0; \n" +
						" switch (o) {\n" +
						"	case Integer i  when a > 5  -> System.out.println(\"Integer:\" + /*here*/i);\n" +
						"	case String s     -> System.out.println(\"String:\" + s + s);\n" +
						"	default       -> System.out.println(\"Object\" + o);\n" +
						" 	}\n" +
						"}\n" +
						"}\n"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		// working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			String str = this.workingCopies[0].getSource();
			String selection = "/*here*/i";
			int start = str.indexOf(selection);
			int length = selection.length();

			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			ILocalVariable local = (ILocalVariable) elements[0];
			search(local, DECLARATIONS, EXACT_RULE);
			assertSearchResults("src/X.java void X.foo(Object).i [i] EXACT_MATCH");

		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	//not a working copy test
	public void testBug573943_022() throws Exception {
		try {
			IJavaProject project = createJavaProject("first", new String[] {"src"}, new String[] {"JCL17_LIB"}, "bin", "17");
			project.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			project.open(null);
			createFolder("/first/src/p1");
			createFile("/first/src/p1/BClass.java",
					"package p1;\n" +
							"public class BClass {\n" +
							"}\n"
					);
			createFile("/first/src/p1/X.java",
					"package p1;\n" +
							"public class X {\n" +
							"public static void main(String[] args) {\n" +
							"foo(Integer.valueOf(5));\n" +
							"foo(new Object());\n" +
							"}\n" +
							"private static void foo(Object o) {\n" +
							" switch (o) {\n" +
							"	case BClass i   -> System.out.println(\"Integer:\" + i);\n" +
							"	default       -> System.out.println(\"Object\" + o);\n" +
							" 	}\n" +
							"}\n" +
							"}\n"
					);
			project.close();
			project.open(null);
			waitUntilIndexesReady();

			SearchPattern pattern = SearchPattern.createPattern("BClass", IJavaSearchConstants.TYPE, REFERENCES, SearchPattern.R_EXACT_MATCH);
			IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
					{project});
			search(pattern, scope, this.resultCollector);
			assertSearchResults(
					"src/p1/X.java void p1.X.foo(Object) [BClass] EXACT_MATCH",
					this.resultCollector);
		}
		finally {

			deleteProject("first");
		}
	}
	// find reference on a field in switch pattern - without select
	public void testBug573943_023() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public class X {\n" +
						"public static  int /*here*/fieldj17 \n" +
						"public static void main(String[] args) {\n" +
						"foo(Integer.valueOf(5));\n" +
						"foo(new Object());\n" +
						"}\n" +
						"private static void foo(Object o) {\n" +
						" switch (o) {\n" +
						"	case Integer i   -> System.out.println(\"Integer:\" + i);\n" +
						"	case String s     -> System.out.println(\"String:\" + s + fieldj17 +fieldj17);\n" +
						"	default       -> System.out.println(\"Object\" + o);\n" +
						" 	}\n" +
						"}\n" +
						"}\n"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		// working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);

			search("fieldj17", FIELD, REFERENCES);
			assertSearchResults("src/X.java void X.foo(Object) [fieldj17] EXACT_MATCH\n"
					+ "src/X.java void X.foo(Object) [fieldj17] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	// find all occurrence on a field in switch pattern - without select
	public void testBug573943_024() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public class X {\n" +
						"public static  int /*here*/fieldj17 \n" +
						"public static void main(String[] args) {\n" +
						"foo(Integer.valueOf(5));\n" +
						"foo(new Object());\n" +
						"}\n" +
						"private static void foo(Object o) {\n" +
						" switch (o) {\n" +
						"	case Integer i   -> System.out.println(\"Integer:\" + i);\n" +
						"	case String s     -> System.out.println(\"String:\" + s + fieldj17 +fieldj17);\n" +
						"	default       -> System.out.println(\"Object\" + o);\n" +
						" 	}\n" +
						"}\n" +
						"}\n"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		// working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);

			search("fieldj17", FIELD, ALL_OCCURRENCES);
			assertSearchResults("src/X.java X.fieldj17 [fieldj17] EXACT_MATCH\n"+
					"src/X.java void X.foo(Object) [fieldj17] EXACT_MATCH\n"
					+ "src/X.java void X.foo(Object) [fieldj17] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	// find all occurrence on a class in switch pattern - without select ( switch statement)
	public void testBug573943_026() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"sealed interface I permits A17, B {}\n" +
						"final class AJ17 implements S {}\n" +
						"final class B implements S {}\n" +
						"public class X {\n" +
						"public static void main(String[] args) {\n" +
						"foo(new A());\n" +
						"}\n" +
						"private static void foo(S o) {\n" +
						" switch (o) {\n" +
						"	case AJ17 /*here*/a :     System.out.println(); break;\n" +
						"	case B b :     System.out.println(\"B:\" + b);\n" +
						"	default  : System.out.println(\"Object\" + o);\n" +
						" 	}\n" +
						"}\n" +
						"}\n"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		// working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			search("AJ17", CLASS, ALL_OCCURRENCES);
			assertSearchResults("src/X.java AJ17 [AJ17] EXACT_MATCH\n"
					+ "src/X.java void X.foo(S) [AJ17] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	// find all reference on a class in switch pattern - without select ( Switch Statement)
	public void testBug573943_027() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"sealed interface I permits A17, B {}\n" +
						"final class AJ17 implements S {}\n" +
						"final class B implements S {}\n" +
						"public class X {\n" +
						"public static void main(String[] args) {\n" +
						"foo(new A());\n" +
						"}\n" +
						"private static void foo(S o) {\n" +
						" switch (o) {\n" +
						"	case AJ17 /*here*/a :     System.out.println(); break;\n" +
						"	case B b :     System.out.println(\"B:\" + b);\n" +
						"	default  : System.out.println(\"Object\" + o);\n" +
						" 	}\n" +
						"}\n" +
						"}\n"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		// working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			search("AJ17", CLASS, REFERENCES);
			assertSearchResults("src/X.java void X.foo(S) [AJ17] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	// find all reference on a class in switch pattern - without select
	public void testBug573943_028() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"sealed interface I permits A17, B {}\n" +
						"final class AJ17 implements S {}\n" +
						"final class B implements S {}\n" +
						"public class X {\n" +
						"public static void main(String[] args) {\n" +
						"foo(new A());\n" +
						"}\n" +
						"private static void foo(S o) {\n" +
						" switch (o) {\n" +
						"	case AJ17 /*here*/a ->     System.out.println(); \n" +
						"	case B b ->     System.out.println(\"B:\" + b);\n" +
						"	default  -> System.out.println(\"Object\" + o);\n" +
						" 	}\n" +
						"}\n" +
						"}\n"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		// working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			search("AJ17", CLASS, REFERENCES);
			assertSearchResults("src/X.java void X.foo(S) [AJ17] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	// find all occurrence on a class in switch pattern - without select
	public void testBug573943_029() throws Exception {
		try {
			IJavaProject project = createJavaProject("first", new String[] {"src"}, new String[] {"JCL17_LIB"}, "bin", "17");
			project.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			project.open(null);
			createFolder("/first/src/p1");
			createFile("/first/src/p1/BClass.java",
					"package p1;\n" +
							"public class BClass {\n" +
							"}\n"
					);
			createFile("/first/src/p1/X.java",
					"package p1;\n" +
							"public class X {\n" +
							" public int field_j17; "+
							"public static void main(String[] args) {\n" +
							"foo(Integer.valueOf(5));\n" +
							"foo(new Object());\n" +
							"}\n" +
							"private static void foo(Object o) {\n" +
							" switch (o) {\n" +
							"	case BClass i && field_j17>0  -> System.out.println(\"Integer:\" + i);\n" +
							"	default       -> System.out.println(\"Object\" + o);\n" +
							" 	}\n" +
							"}\n" +
							"}\n"
					);
			project.close();
			project.open(null);
			waitUntilIndexesReady();

			SearchPattern pattern = SearchPattern.createPattern("field_j17", IJavaSearchConstants.FIELD, ALL_OCCURRENCES, SearchPattern.R_EXACT_MATCH);
			IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
					{project});
			search(pattern, scope, this.resultCollector);
			assertSearchResults(
					"src/p1/X.java p1.X.field_j17 [field_j17] EXACT_MATCH\n"
							+ "src/p1/X.java void p1.X.foo(Object) [field_j17] EXACT_MATCH",
							this.resultCollector);
		}
		finally {

			deleteProject("first");
		}
	}
	// find all references on a class in switch pattern - without select
	public void testBug573943_030() throws Exception {
		try {
			IJavaProject project = createJavaProject("first", new String[] {"src"}, new String[] {"JCL17_LIB"}, "bin", "17");
			project.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			project.open(null);
			createFolder("/first/src/p1");
			createFile("/first/src/p1/BClass.java",
					"package p1;\n" +
							"public class BClass {\n" +
							"}\n"
					);
			createFile("/first/src/p1/X.java",
					"package p1;\n" +
							"public class X {\n" +
							" public int field_j17; "+
							"public static void main(String[] args) {\n" +
							"foo(Integer.valueOf(5));\n" +
							"foo(new Object());\n" +
							"}\n" +
							"private static void foo(Object o) {\n" +
							" switch (o) {\n" +
							"	case BClass i && field_j17>0  -> System.out.println(\"Integer:\" + i);\n" +
							"	default       -> System.out.println(\"Object\" + o);\n" +
							" 	}\n" +
							"}\n" +
							"}\n"
					);
			project.close();
			project.open(null);
			waitUntilIndexesReady();

			SearchPattern pattern = SearchPattern.createPattern("field_j17", IJavaSearchConstants.FIELD, REFERENCES, SearchPattern.R_EXACT_MATCH);
			IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
					{project});
			search(pattern, scope, this.resultCollector);
			assertSearchResults(
					"src/p1/X.java void p1.X.foo(Object) [field_j17] EXACT_MATCH",
					this.resultCollector);
		}
		finally {

			deleteProject("first");
		}
	}
	public void testBug573943_031() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public class X {\n" +
						"public static void main(String[] args) {\n" +
						"foo(Integer.valueOf(5));\n" +
						"foo(new Object());\n" +
						"}\n" +
						"private static void foo(Object o) {\n" +
						" switch (o) {\n" +
						"	case Integer i     -> System.out.println(\"Integer:\" + i);\n" +
						"	case String s when /*here*/s.hashCode()>0    -> System.out.println(\"String:\" );\n" +
						"	default       -> System.out.println(\"Object\" + o);\n" +
						" 	}}\n" +
						"}\n" +
						"}\n"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		// working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			String str = this.workingCopies[0].getSource();
			String selection = "/*here*/s";
			int start = str.indexOf(selection);
			int length = selection.length();

			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			assertEquals("incorrect no of elements", 1, elements.length);
			assertTrue(elements[0] instanceof ILocalVariable);

		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	public void testBug573943_032() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public class X {\n" +
						"public static void main(String[] args) {\n" +
						"foo(Integer.valueOf(5));\n" +
						"foo(new Object());\n" +
						"}\n" +
						"private static void foo(Object o) {\n" +
						" String myVar= new String();" +
						" switch (o) {\n" +
						"	case Integer i     -> System.out.println(\"Integer:\" + i);\n" +
						"	case String s1 when s1 != myVar && 0 < /*here*/s1.length()   -> System.out.println(s1 );\n" +
						"	default       -> {\n" +
						"	String s1 =  new String();\n" +
						"	System.out.println(s1);\n" +
						" 	}}\n" +
						"}\n" +
						"}\n"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		// working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			String str = this.workingCopies[0].getSource();
			String selection = "/*here*/s1";
			int start = str.indexOf(selection);
			int length = selection.length();
			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			assertEquals("incorrect no of elements", 1, elements.length);
			assertTrue(elements[0] instanceof ILocalVariable);
			search(elements[0], REFERENCES, EXACT_RULE);
			assertSearchResults("src/X.java void X.foo(Object) [s1] EXACT_MATCH\n"
					+ "src/X.java void X.foo(Object) [s1] EXACT_MATCH\n"
					+ "src/X.java void X.foo(Object) [s1] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}

	public void testBug573943_033() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public class X {\n" +
						"public static void main(String[] args) {\n" +
						"foo(Integer.valueOf(5));\n" +
						"foo(new Object());\n" +
						"}\n" +
						"private static void foo(Object o) {\n" +
						" String myVar= new String();" +
						" switch (o) {\n" +
						"	case Integer i     -> System.out.println(\"Integer:\" + i);\n" +
						"	case String s1 when /*here*/s1 != myVar && 0 < s1.length()   -> System.out.println(s1 );\n" +
						"	default       -> {\n" +
						"	String s1 =  new String();\n" +
						"	System.out.println(s1);\n" +
						" 	}}\n" +
						"}\n" +
						"}\n"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		// working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			String str = this.workingCopies[0].getSource();
			String selection = "/*here*/s1";
			int start = str.indexOf(selection);
			int length = selection.length();
			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			assertEquals("incorrect no of elements", 1, elements.length);
			assertTrue(elements[0] instanceof ILocalVariable);
			search(elements[0], REFERENCES, EXACT_RULE);
			assertSearchResults("src/X.java void X.foo(Object) [s1] EXACT_MATCH\n"
					+ "src/X.java void X.foo(Object) [s1] EXACT_MATCH\n"
					+ "src/X.java void X.foo(Object) [s1] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}

	public void testBug573943_034() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public class X {\n" +
						"public static void main(String[] args) {\n" +
						"foo(Integer.valueOf(5));\n" +
						"foo(new Object());\n" +
						"}\n" +
						"private static void foo(Object o) {\n" +
						" String myVar= new String();" +
						" switch (o) {\n" +
						"	case Integer i     -> System.out.println(\"Integer:\" + i);\n" +
						"	case String s1 when s1 != myVar && 0 < s1.length()   -> System.out.println(s1 );\n" +
						"	default       -> {\n" +
						"	String s1 =  new String();\n" +
						"	System.out.println(/*here*/s1);\n" +
						" 	}}\n" +
						"}\n" +
						"}\n"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		// working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			String str = this.workingCopies[0].getSource();
			String selection = "/*here*/s1";
			int start = str.indexOf(selection);
			int length = selection.length();
			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			assertEquals("incorrect no of elements", 1, elements.length);
			assertTrue(elements[0] instanceof ILocalVariable);
			search(elements[0], REFERENCES, EXACT_RULE);
			assertSearchResults("src/X.java void X.foo(Object) [s1] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}

	// selection of guarded pattern variable along with instance of expression in case statement and verify that it is local variable
	public void testBug575718_035() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public class X {\n" +
						"public static void main(String[] args) {\n" +
						"foo(Integer.valueOf(5));\n" +
						"foo(new Object());\n" +
						"}\n" +
						"private static int  foo(Object o) {\n" +
						" return switch (o) {\n" +
						"	case String strGP && (o instanceof String c3 && c3.length() > 0) -> 0;\n" +
						"	default       -> 0;\n" +
						" 	};\n" +
						" 	}\n" +
						"}\n"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		// working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			String str = this.workingCopies[0].getSource();
			String selection = "strGP";
			int start = str.indexOf(selection);
			int length = selection.length();

			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			assertTrue(elements[0] instanceof LocalVariable);


		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}

	// selection of guarded pattern variable in case statement along with instanceof expression and verify it gets all references
	public void testBug575718_036() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public class X {\n" +
						"public static void main(String[] args) {\n" +
						"foo(Integer.valueOf(5));\n" +
						"foo(new Object());\n" +
						"}\n" +
						"private static int  foo(Object o) {\n" +
						" return switch (o) {\n" +
						"	case String strGP when (o instanceof String c3 && c3.length() > 0) && strGP.length() > 0 -> 0;\n" +
						"	default       -> 0;\n" +
						" 	};\n" +
						" 	}\n" +
						"}\n"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		// working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			String str = this.workingCopies[0].getSource();
			String selection = "strGP";
			int start = str.indexOf(selection);
			int length = selection.length();

			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			assertTrue(elements[0] instanceof LocalVariable);
			search(elements[0], REFERENCES, EXACT_RULE);
			assertSearchResults(
					"src/X.java int X.foo(Object) [strGP] EXACT_MATCH");


		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	// selection of guarded pattern variable in case statement along with instanceof expression and verify that it gets all occurences
		public void testBug575718_037() throws CoreException {
			this.workingCopies = new ICompilationUnit[1];
			this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
					"public class X {\n" +
							"public static void main(String[] args) {\n" +
							"foo(Integer.valueOf(5));\n" +
							"foo(new Object());\n" +
							"}\n" +
							"private static int  foo(Object o) {\n" +
							" return switch (o) {\n" +
							"	case String strGP when (o instanceof String c3 && c3.length() > 0) && strGP.length() > 0 -> 0;\n" +
							"	default       -> 0;\n" +
							" 	};\n" +
							" 	}\n" +
							"}\n"
					);
			IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
			// working copies
			String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
			try {
				javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
				String str = this.workingCopies[0].getSource();
				String selection = "strGP";
				int start = str.indexOf(selection);
				int length = selection.length();

				IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
				assertTrue(elements[0] instanceof LocalVariable);
				search(elements[0], ALL_OCCURRENCES, EXACT_RULE);
				assertSearchResults(
						"src/X.java int X.foo(Object).strGP [strGP] EXACT_MATCH\n"
						+ "src/X.java int X.foo(Object) [strGP] EXACT_MATCH");


			} finally {
				javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
			}
		}


}


