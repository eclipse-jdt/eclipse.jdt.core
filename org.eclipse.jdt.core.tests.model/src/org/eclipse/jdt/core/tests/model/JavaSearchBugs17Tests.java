/*******************************************************************************
 * Copyright (c) 2021, 2023 IBM Corporation and others.
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
import java.util.Map;
import junit.framework.Test;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IType;
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
import org.eclipse.jdt.internal.core.SourceType;

public class JavaSearchBugs17Tests extends AbstractJavaSearchTests {

	static {
		// org.eclipse.jdt.internal.core.search.BasicSearchEngine.VERBOSE = true;
		// TESTS_NUMBERS = new int[] { 19 };
		// TESTS_RANGE = new int[] { 1, -1 };
//		 TESTS_NAMES = new String[] {"testBug573943_022"};
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
		JAVA_PROJECT = setUpJavaProject("JavaSearchBugs", "21");
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
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
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
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
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
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
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
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
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
						"	case Integer i when field > 0  -> System.out.println(\"Integer:\" + i);\n" +
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
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
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
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
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
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
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
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
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
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
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
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
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
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
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
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
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
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
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
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
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
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
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
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
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
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
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
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
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
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
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
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
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
			IJavaProject project = createJavaProject("first", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "21");
			project.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
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
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);

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
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);

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
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
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
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
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
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
			search("AJ17", CLASS, REFERENCES);
			assertSearchResults("src/X.java void X.foo(S) [AJ17] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	// find all occurrence on a class in switch pattern - without select
	public void testBug573943_029() throws Exception {
		try {
			IJavaProject project = createJavaProject("first", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "21");
			project.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
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
							"	case BClass i when field_j17>0  -> System.out.println(\"Integer:\" + i);\n" +
							"	default -> System.out.println(\"Object\" + o);\n" +
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
			IJavaProject project = createJavaProject("first", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "21");
			project.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
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
							" public int field_j17;\n"+
							"public static void main(String[] args) {\n" +
							"foo(Integer.valueOf(5));\n" +
							"foo(new Object());\n" +
							"}\n" +
							"private static void foo(Object o) {\n" +
							" switch (o) {\n" +
							"	case BClass i when field_j17 > 0  -> System.out.println(\"Integer:\" + i);\n" +
							"	default -> System.out.println(\"Object\" + o);\n" +
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
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
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
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
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
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
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
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
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
						"	case String strGP when (o instanceof String c3 when c3.length() > 0) -> 0;\n" +
						"	default       -> 0;\n" +
						" 	};\n" +
						" 	}\n" +
						"}\n"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); // assuming single project for all
		// working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
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
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
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
				javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
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

		// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/968
		// JavaElementHyperlinkDetector: Parser runs into NegativeArraySizeException in some cases
		public void testGH968() throws CoreException {
			try {
				IJavaProject project = createJavaProject("p", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "17");
				project.open(null);
				createFile("/p/src/TestEcl.java",
						"import java.lang.StackWalker.Option;\n" +
						"\n" +
						"public class TestEcl {\n" +
						"\n" +
						"	@FunctionalInterface\n" +
						"	public interface Callable<V> {\n" +
						"	    /**\n" +
						"	     * Computes a result, or throws an exception if unable to do so.\n" +
						"	     *\n" +
						"	     * @return computed result\n" +
						"	     * @throws Exception if unable to compute a result\n" +
						"	     */\n" +
						"	    V call() throws Exception;\n" +
						"	}\n" +
						"	\n" +
						"	@FunctionalInterface\n" +
						"	public interface Function<T, R> {\n" +
						"\n" +
						"	    /**\n" +
						"	     * Applies this function to the given argument.\n" +
						"	     *\n" +
						"	     * @param t the function argument\n" +
						"	     * @return the function result\n" +
						"	     */\n" +
						"	    R apply(T t);\n" +
						"	}\n" +
						"	\n" +
						"	public static final Callable<Void> test = new Callable<>() {\n" +
						"		@Override\n" +
						"		public Void call() throws Exception {\n" +
						"			Option opt = Option.RETAIN_CLASS_REFERENCE;\n" +
						"			\n" +
						"			boolean a = switch (/*here*/opt) {\n" +
						"				case RETAIN_CLASS_REFERENCE -> true;\n" +
						"				// Enabling this line breaks eclipse\n" +
						"				// CTRL+SHIFT+G Will throw exception Code resolve error 'java.lang.NegativeArraySizeException: -1'\n" +
						"				case SHOW_HIDDEN_FRAMES -> true; \n" +
						"				default -> throw new IllegalArgumentException(\"Unexpected value\");\n" +
						"			};\n" +
						"	\n" +
						"			boolean b = switch (opt) {\n" +
						"				case RETAIN_CLASS_REFERENCE -> true;\n" +
						"				default -> throw new IllegalArgumentException(\"Unexpected value\");\n" +
						"			};\n" +
						"			return null;\n" +
						"		}\n" +
						"	};\n" +
						"	\n" +
						"	public static final Function<Object, Object> test2 = new Function<>() {\n" +
						"		@Override\n" +
						"		public Object apply(Object t) {\n" +
						"			return null;\n" +
						"		}\n" +
						"	};\n" +
						"}\n");
				project.close();
				project.open(null);
				waitUntilIndexesReady();
				SearchPattern pattern = SearchPattern.createPattern("Callable", IJavaSearchConstants.TYPE, REFERENCES, SearchPattern.R_EXACT_MATCH | SearchPattern.R_ERASURE_MATCH);
				IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[]
						{project});
				search(pattern, scope, this.resultCollector);
				assertSearchResults(
						"src/TestEcl.java TestEcl.test:<anonymous>#1 [Callable] EXACT_MATCH\n" +
						"src/TestEcl.java TestEcl.test [Callable] EXACT_MATCH",
						this.resultCollector);
			}
			finally {
				deleteProject("p");
			}
		}

		public void testGH968_2() throws CoreException {
			try {
				this.workingCopies = new ICompilationUnit[1];
				this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/OptionActions.java",
						"public class OptionActions {\n" +
						"\n" +
						"	public enum TestEnum {\n" +
						"		X\n" +
						"	}\n" +
						"	\n" +
						"	@FunctionalInterface\n" +
						"	public interface Callable<V> {\n" +
						"	    /**\n" +
						"	     * Computes a result, or throws an exception if unable to do so.\n" +
						"	     *\n" +
						"	     * @return computed result\n" +
						"	     * @throws Exception if unable to compute a result\n" +
						"	     */\n" +
						"	    V call() throws Exception;\n" +
						"	}\n" +
						"	public final record TestDraft<T >() {}\n" +
						"	public final record TestDraft2<T, U >() {}\n" +
						"	\n" +
						"	public static final /*here*/Callable<Void> test = new Callable<>() {\n" +
						"		@Override\n" +
						"		public Void call() throws Exception {\n" +
						"			TestEnum p = TestEnum.X;\n" +
						"			\n" +
						"			Object v = switch (p) {\n" +
						"				case X -> null;\n" +
						"			};\n" +
						"	\n" +
						"			/*here*/TestDraft<Integer> draft = new TestDraft(); // This line is fine\n" +
						"			TestDraft2<Integer, Integer> draft2 = new TestDraft2(); \n" +
						"			return null;\n" +
						"		}\n" +
						"	};\n" +
						"}\n");

				String str = this.workingCopies[0].getSource();
				String selection = "/*here*/Callable";
				int start = str.indexOf(selection);
				int length = selection.length();
				IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
				assertElementsEqual(
					"Unexpected elements",
					"Callable [in OptionActions [in [Working copy] OptionActions.java [in <default> [in src [in JavaSearchBugs]]]]]",
					elements
				);
			}
			finally {
				deleteProject("p");
			}
		}
		// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/968
		// JavaElementHyperlinkDetector: Parser runs into NegativeArraySizeException in some cases
		public void testGH968_3() throws CoreException {
			this.workingCopies = new ICompilationUnit[1];
			this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/TestEcl.java",
					"import java.lang.StackWalker.Option;\n" +
					"\n" +
					"public class TestEcl {\n" +
					"\n" +
					"	@FunctionalInterface\n" +
					"	public interface Callable {\n" +
					"	    /**\n" +
					"	     * Computes a result, or throws an exception if unable to do so.\n" +
					"	     *\n" +
					"	     * @return computed result\n" +
					"	     * @throws Exception if unable to compute a result\n" +
					"	     */\n" +
					"	    void call() throws Exception;\n" +
					"	}\n" +
					"	\n" +
					"	@FunctionalInterface\n" +
					"	public interface Function<T, R> {\n" +
					"\n" +
					"	    /**\n" +
					"	     * Applies this function to the given argument.\n" +
					"	     *\n" +
					"	     * @param t the function argument\n" +
					"	     * @return the function result\n" +
					"	     */\n" +
					"	    R apply(T t);\n" +
					"	}\n" +
					"	\n" +
					"	public static final Callable test = new Callable() {\n" +
					"		@Override\n" +
					"		public Void call() throws Exception {\n" +
					"			Option opt = Option.RETAIN_CLASS_REFERENCE;\n" +
					"			\n" +
					"			boolean a = switch (/*here*/opt) {\n" +
					"				case RETAIN_CLASS_REFERENCE -> true;\n" +
					"				// Enabling this line breaks eclipse\n" +
					"				// CTRL+SHIFT+G Will throw exception Code resolve error 'java.lang.NegativeArraySizeException: -1'\n" +
					"				case SHOW_HIDDEN_FRAMES -> true; \n" +
					"				default -> throw new IllegalArgumentException(\"Unexpected value\");\n" +
					"			};\n" +
					"	\n" +
					"			boolean b = switch (opt) {\n" +
					"				case RETAIN_CLASS_REFERENCE -> true;\n" +
					"				default -> throw new IllegalArgumentException(\"Unexpected value\");\n" +
					"			};\n" +
					"			return;\n" +
					"		}\n" +
					"	};\n" +
					"	\n" +
					"	public static final Function<Object, Object> test2 = new Function<>() {\n" +
					"		@Override\n" +
					"		public Object apply(Object t) {\n" +
					"			return null;\n" +
					"		}\n" +
					"	};\n" +
					"}\n");

			String str = this.workingCopies[0].getSource();
			String selection =  "/*here*/opt";
			int start = str.indexOf(selection);
			int length = selection.length();

			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			assertTrue(elements.length ==1);
			assertTrue(elements[0] instanceof LocalVariable);
			search(elements[0], REFERENCES, EXACT_RULE);
			assertSearchResults(
					"src/TestEcl.java Void TestEcl.test:<anonymous>#1.call() [opt] EXACT_MATCH\n" +
					"src/TestEcl.java Void TestEcl.test:<anonymous>#1.call() [opt] EXACT_MATCH");
		}

		// check for permit reference
		public void test564049_001() throws CoreException {
			this.workingCopies = new ICompilationUnit[1];
			this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
					"public sealed class X permits Y{ \n" +
							"	}\n" +
							"	final class Y extends X {}\n"

					);
			IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
			String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
			try {
				javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
				search("Y", CLASS, REFERENCES);
				assertSearchResults("src/X.java X [Y] EXACT_MATCH");
			} finally {
				javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
			}
		}
		// select a class and check its permit reference
		public void test564049_002() throws CoreException {
			this.workingCopies = new ICompilationUnit[1];
			this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
					"public sealed class X permits Y{ \n" +
							"	}\n" +
							"	final class /*here*/Y extends X {}\n"

					);
			String str = this.workingCopies[0].getSource();
			String selection = "/*here*/Y";
			int start = str.indexOf(selection);
			int length = selection.length();

			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			assertTrue(elements.length ==1);
			assertTrue((elements[0] instanceof SourceType));
			search(elements[0], REFERENCES, EXACT_RULE);
			assertSearchResults(
					"src/X.java X [Y] EXACT_MATCH");
		}

		// select a class ( at permit location) and check its reference
		public void test564049_003() throws CoreException {
			this.workingCopies = new ICompilationUnit[1];
			this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
					"public sealed class X permits /*here*/Y{ \n" +
							"	}\n" +
							"	final class Y extends X {}\n"

					);
			String str = this.workingCopies[0].getSource();
			String selection = "/*here*/Y";
			int start = str.indexOf(selection);
			int length = selection.length();

			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			assertTrue(elements.length ==1);
			assertTrue((elements[0] instanceof SourceType));
			search(elements[0], REFERENCES, EXACT_RULE);
			assertSearchResults(
					"src/X.java X [Y] EXACT_MATCH");
		}
		// check for permit reference if it is the nth permitted item
		public void test564049_004() throws CoreException {
			this.workingCopies = new ICompilationUnit[1];
			this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
					"public sealed class X permits Y,Q{ \n" +
							"	}\n" +
							"	final class Q extends X {}\n" +
							"	final class Y extends X {}\n"

					);
			IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
			String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
			try {
				javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
				search("Q", CLASS, REFERENCES);
				assertSearchResults("src/X.java X [Q] EXACT_MATCH");
			} finally {
				javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
			}
		}

		// select a class and check its nth permit reference
		public void test564049_005() throws CoreException {
			this.workingCopies = new ICompilationUnit[1];
			this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
					"public sealed class X permits Y,Q{ \n" +
							"	}\n" +
							"	final class /*here*/Q extends X {}\n" +
							"	final class Y extends X {}\n"

					);
			String str = this.workingCopies[0].getSource();
			String selection = "/*here*/Q";
			int start = str.indexOf(selection);
			int length = selection.length();

			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			assertTrue(elements.length ==1);
			assertTrue((elements[0] instanceof SourceType));
			search(elements[0], REFERENCES, EXACT_RULE);
			assertSearchResults(
					"src/X.java X [Q] EXACT_MATCH");
		}

		// check for permit reference with supertype finegrain
		public void test564049_006() throws CoreException {
			this.workingCopies = new ICompilationUnit[1];
			this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
					"public sealed class X permits Y{ \n" +
							"	}\n" +
							"	final class Y extends X {}\n"

					);
			IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
			String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
			try {
				javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
				search("Y", CLASS, SUPERTYPE_TYPE_REFERENCE);
				assertSearchResults("");
			} finally {
				javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
			}
		}

		// check for permit reference with permittype finegrain
		public void test564049_007() throws CoreException {
			this.workingCopies = new ICompilationUnit[1];
			this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
					"public sealed class X permits Y{ \n" +
							"	}\n" +
							"	final class Y extends X {}\n"

					);
			IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
			String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
			try {
				javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
				search("Y", CLASS, PERMITTYPE_TYPE_REFERENCE);
				assertSearchResults("src/X.java X [Y] EXACT_MATCH");
			} finally {
				javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
			}
		}
		// check for permit reference with permittype or supertype finegrain
		public void test564049_008() throws CoreException {
			this.workingCopies = new ICompilationUnit[1];
			this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
					"public sealed class X permits Y{ \n" +
							"	}\n" +
							"	final class Y extends X {}\n"

					);
			IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
			String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
			try {
				javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
				search("Y", CLASS, PERMITTYPE_TYPE_REFERENCE | SUPERTYPE_TYPE_REFERENCE);
				assertSearchResults("src/X.java X [Y] EXACT_MATCH");
			} finally {
				javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
			}
		}

		// check for permit reference for qualified type
		public void test564049_009() throws CoreException {
			this.workingCopies = new ICompilationUnit[1];
			this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/p1/X.java",
					"package p1;\n"+
							"public sealed class X permits A.Y {\n" +
							"	public static void main(String[] args) {}\n" +
							"}\n" +
							"class A {\n" +
							"	sealed class Y extends X {\n" +
							"		final class SubInnerY extends Y {}\n" +
							"	} \n" +
							"	final class Z extends Y {}\n" +
							"   final class SubY extends Y {}" +
							"}"

					);
			IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
			String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
			try {
				javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
				search("A.Y", CLASS, REFERENCES);
				assertSearchResults(
						"src/p1/X.java p1.X [A.Y] EXACT_MATCH\n" +
						"src/p1/X.java p1.A$Y$SubInnerY [Y] EXACT_MATCH\n" +
						"src/p1/X.java p1.A$Z [Y] EXACT_MATCH\n" +
						"src/p1/X.java p1.A$SubY [Y] EXACT_MATCH");
			} finally {
				javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
			}
		}


		// check for permit reference with permittype finegrain - negative test case
		public void test564049_010() throws CoreException {
			this.workingCopies = new ICompilationUnit[1];
			this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
					"public sealed class X extends Y{ \n" +
							"	}\n" +
							"	 class Y {}\n"

					);
			IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
			String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
			try {
				javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
				search("Y", CLASS, PERMITTYPE_TYPE_REFERENCE);
				assertSearchResults("");
			} finally {
				javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
			}
		}
		// permit reference in another file
		public void test564049_011() throws CoreException {
			IJavaProject project1 = createJavaProject("JavaSearchBugs15", new String[] {"src"}, new String[] {"JCL15_LIB"}, "bin", "17");
			try {
				Map<String, String> options = project1.getOptions(false);
				options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_17);
				project1.setOptions(options);
				project1.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
				project1.open(null);
				createFolder("/JavaSearchBugs15/src/pack11");
				String fileContent = "package pack11;\n" +
						"public sealed class X11_ permits X12_{\n" +
						"}\n";
				String fileContent2 = "package pack11;\n" +
						"final public class /*here*/X12_ extends X11_ {\n" +
						"}\n";

				createFile("/JavaSearchBugs15/src/pack11/X11_.java", fileContent);
				createFile("/JavaSearchBugs15/src/pack11/X12_.java",fileContent2);
				ICompilationUnit unit = getCompilationUnit("/JavaSearchBugs15/src/pack11/X12_.java");
				String x11 = "/*here*/X12_";
				int start = fileContent2.indexOf(x11);
				IJavaElement[] elements = unit.codeSelect(start, x11.length());
				assertTrue(elements.length ==1);
				IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
				search(elements[0].getElementName(), TYPE, PERMITTYPE_TYPE_REFERENCE, EXACT_RULE, scope);
				assertSearchResults("src/pack11/X11_.java pack11.X11_ [X12_] EXACT_MATCH");
			} finally {
				deleteProject(project1);
			}
		}

	 	public void testPermitReferenceInSourceJar() throws CoreException {

	 		IType myClass = getClassFile("JavaSearchBugs", "lib/permit_reference_in_source_jar.jar", "pack", "PermitClass2.class").getType();
	 		search(
	 			myClass,
	 			ALL_OCCURRENCES,
	 			getJavaSearchScope(),
	 			this.resultCollector);
	 		assertSearchResults(
	 				"lib/permit_reference_in_source_jar.jar pack.MyClass2 EXACT_MATCH\n" +
	 				"lib/permit_reference_in_source_jar.jar pack.PermitClass2 EXACT_MATCH",
	 			this.resultCollector);

	 	}
}


