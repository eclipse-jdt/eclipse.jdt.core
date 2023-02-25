/*******************************************************************************
 * Copyright (c)  2021, 2022 IBM Corporation and others.
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
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.ReferenceMatch;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeReferenceMatch;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jdt.internal.core.search.matching.DeclarationOfAccessedFieldsPattern;

import junit.framework.Test;

public class JavaSearchBugs16Tests extends AbstractJavaSearchTests {

	static {
		//	 org.eclipse.jdt.internal.core.search.BasicSearchEngine.VERBOSE = true;
		//		TESTS_NUMBERS = new int[] { 19 };
		//		TESTS_RANGE = new int[] { 1, -1 };
		//		TESTS_NAMES = new String[] {"testBug542559_001"};
	}

	public JavaSearchBugs16Tests(String name) {
		super(name);
		this.endChar = "";
	}

	public static Test suite() {
		return buildModelTestSuite(JavaSearchBugs16Tests.class, BYTECODE_DECLARATION_ORDER);
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
			int length = others==null ? 0 : others.length;
			if (length > 0) {
				this.line.append("+[");
				for (int i=0; i<length; i++) {
					IJavaElement other = others[i];
					if (i>0) this.line.append(',');
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

	protected IJavaProject setUpJavaProject(final String projectName, String compliance, boolean useFullJCL) throws CoreException, IOException {
		// copy files in project from source workspace to target workspace
		IJavaProject setUpJavaProject = super.setUpJavaProject(projectName, compliance, useFullJCL);
		return setUpJavaProject;
	}

	IJavaSearchScope getJavaSearchScope() {
		return SearchEngine.createJavaSearchScope(new IJavaProject[] {getJavaProject("JavaSearchBugs")});
	}

	IJavaSearchScope getJavaSearchScopeBugs(String packageName, boolean addSubpackages) throws JavaModelException {
		if (packageName == null) return getJavaSearchScope();
		return getJavaSearchPackageScope("JavaSearchBugs", packageName, addSubpackages);
	}

	public ICompilationUnit getWorkingCopy(String path, String source) throws JavaModelException {
		if (this.wcOwner == null) {
			this.wcOwner = new WorkingCopyOwner() {};
		}
		return getWorkingCopy(path, source, this.wcOwner);
	}

	@Override
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		JAVA_PROJECT = setUpJavaProject("JavaSearchBugs", "16");
	}

	public void tearDownSuite() throws Exception {
		deleteProject("JavaSearchBugs");
		super.tearDownSuite();
	}

	protected void setUp () throws Exception {
		super.setUp();
		this.resultCollector = new TestCollector();
		this.resultCollector.showAccuracy(true);
	}




	// all occurrences of local enum
	public void testBug570246_001() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
			"public class X2 {\n" +
			"public static void main(String[] args) {\n" +
		    " enum Y2 {\n" +
			"	BLEU,\n" +
			"	BLANC,\n" +
			"	ROUGE;\n" +
			"	public static void main(String[] args) {\n" +
			"		for(Y2 y: Y2.values()) {\n" +
			"			System.out.print(y);\n" +
			"		}\n" +
			"	}\n" +
			"  }\n" +
			"  Y2.main(args);\n" +
		"	}\n" +
		"}\n"

				);
		search("Y2", ENUM, ALL_OCCURRENCES);
		assertSearchResults("src/X.java void X2.main(String[]):Y2#1 [Y2] EXACT_MATCH\n" +
				"src/X.java void void X2.main(String[]):Y2#1.main(String[]) [Y2] EXACT_MATCH\n" +
				"src/X.java void void X2.main(String[]):Y2#1.main(String[]) [Y2] EXACT_MATCH\n" +
				"src/X.java void X2.main(String[]) [Y2] EXACT_MATCH");

	}

	// declaration occurrence of local enum
		public void testBug570246_002() throws CoreException {
			this.workingCopies = new ICompilationUnit[1];
			this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public class X2 {\n" +
				"public static void main(String[] args) {\n" +
			    " enum Y2 {\n" +
				"	BLEU,\n" +
				"	BLANC,\n" +
				"	ROUGE;\n" +
				"	public static void main(String[] args) {\n" +
				"		for(Y2 y: Y2.values()) {\n" +
				"			System.out.print(y);\n" +
				"		}\n" +
				"	}\n" +
				"  }\n" +
				"  Y2.main(args);\n" +
			"	}\n" +
			"}\n"

					);
			search("Y2", ENUM, DECLARATIONS);
			assertSearchResults("src/X.java void X2.main(String[]):Y2#1 [Y2] EXACT_MATCH");

		}

		// declaration occurrence of local enum
		public void testBug570246_003a() throws CoreException {
			this.workingCopies = new ICompilationUnit[1];
			this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",

					"public class X {\n"+
							"    public static void main(String[] args) {\n"+
							"          enum Role { M, D }\n"+
							" enum T {\n"+
							"       PHILIPPE(37) {\n"+
							"               public boolean isManager() {\n"+
							"                       return true;\n"+
							"               }\n"+
							"       },\n"+
							"       DAVID(27),\n"+
							"       JEROME(33),\n"+
							"       OLIVIER(35),\n"+
							"       KENT(40),\n"+
							"       YODA(41),\n"+
							"       FREDERIC;\n"+
							"       final static int OLD = 41;\n"+
							"\n"+
							"\n"+
							"   int age;\n"+
							"       Role role;\n"+
							"\n"+
							"       T() { this(OLD); }\n"+
							"       T(int age) {\n"+
							"               this.age = age;\n"+
							"       }\n"+
							"       public int age() { return this.age; }\n"+
							"       public boolean isManager() { return false; }\n"+
							"       void setRole(boolean mgr) {\n"+
							"               this.role = mgr ? Role.M : Role.D;\n"+
							"       }\n"+
							"}\n"+
							"       System.out.print(\"JDTCore team:\");\n"+
							"       T oldest = null;\n"+
							"       int maxAge = Integer.MIN_VALUE;\n"+
							"       for (T t : T.values()) {\n"+
							"            if (t == T.YODA) continue;// skip YODA\n"+
							"            t.setRole(t.isManager());\n"+
							"                        if (t.age() > maxAge) {\n"+
							"               oldest = t;\n"+
							"               maxAge = t.age();\n"+
							"            }\n"+
							"                      Location l = switch(t) {\n"+
							"                         case PHILIPPE, DAVID, JEROME, FREDERIC-> Location.SNZ;\n"+
							"                         case OLIVIER, KENT -> Location.OTT;\n"+
							"                         default-> throw new AssertionError(\"Unknown team member: \" + t);\n"+
							"                       };\n"+
							"\n"+
							"            System.out.print(\" \"+ t + ':'+t.age()+':'+l+':'+t.role);\n"+
							"        }\n"+
							"        System.out.println(\" WINNER is:\" + T.valueOf(oldest.name()));\n"+
							"    }\n"+
							"\n"+
							"   private enum Location { SNZ, OTT }\n"+
							"}"
					);
			search("T", ENUM, ALL_OCCURRENCES);
			assertSearchResults("src/X.java void X.main(String[]):T#1 [T] EXACT_MATCH\n"
					+ "src/X.java void X.main(String[]) [T] EXACT_MATCH\n"
					+ "src/X.java void X.main(String[]) [T] EXACT_MATCH\n"
					+ "src/X.java void X.main(String[]) [T] EXACT_MATCH\n"
					+ "src/X.java void X.main(String[]) [T] EXACT_MATCH\n"
					+ "src/X.java void X.main(String[]) [T] EXACT_MATCH");


		}
		public void testBug570246_003b() throws CoreException {
			this.workingCopies = new ICompilationUnit[1];
			this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",

					"public class X {\n"+
							"    public static void main(String[] args) {\n"+
							"          enum Role { M, D }\n"+
							" enum T {\n"+
							"       PHILIPPE(37) {\n"+
							"               public boolean isManager() {\n"+
							"                       return true;\n"+
							"               }\n"+
							"       },\n"+
							"       DAVID(27),\n"+
							"       JEROME(33),\n"+
							"       OLIVIER(35),\n"+
							"       KENT(40),\n"+
							"       YODA(41),\n"+
							"       FREDERIC;\n"+
							"       final static int OLD = 41;\n"+
							"\n"+
							"\n"+
							"   int age;\n"+
							"       Role role;\n"+
							"\n"+
							"       T() { this(OLD); }\n"+
							"       T(int age) {\n"+
							"               this.age = age;\n"+
							"       }\n"+
							"       public int age() { return this.age; }\n"+
							"       public boolean isManager() { return false; }\n"+
							"       void setRole(boolean mgr) {\n"+
							"               this.role = mgr ? Role.M : Role.D;\n"+
							"       }\n"+
							"}\n"+
							"       System.out.print(\"JDTCore team:\");\n"+
							"       T oldest = null;\n"+
							"       int maxAge = Integer.MIN_VALUE;\n"+
							"       for (T t : T.values()) {\n"+
							"            if (t == T.YODA) continue;// skip YODA\n"+
							"            t.setRole(t.isManager());\n"+
							"                        if (t.age() > maxAge) {\n"+
							"               oldest = t;\n"+
							"               maxAge = t.age();\n"+
							"            }\n"+
							"                      Location l = switch(t) {\n"+
							"                         case PHILIPPE, DAVID, JEROME, FREDERIC-> Location.SNZ;\n"+
							"                         case OLIVIER, KENT -> Location.OTT;\n"+
							"                         default-> throw new AssertionError(\"Unknown team member: \" + t);\n"+
							"                       };\n"+
							"\n"+
							"            System.out.print(\" \"+ t + ':'+t.age()+':'+l+':'+t.role);\n"+
							"        }\n"+
							"        System.out.println(\" WINNER is:\" + T.valueOf(oldest.name()));\n"+
							"    }\n"+
							"\n"+
							"   private enum Location { SNZ, OTT }\n"+
							"}"
					);
			search("Role", ENUM, ALL_OCCURRENCES);
			assertSearchResults("src/X.java void X.main(String[]):Role#1 [Role] EXACT_MATCH\n"
					+ "src/X.java void X.main(String[]):T#1.role [Role] EXACT_MATCH\n"
					+ "src/X.java void void X.main(String[]):T#1.setRole(boolean) [Role] EXACT_MATCH\n"
					+ "src/X.java void void X.main(String[]):T#1.setRole(boolean) [Role] EXACT_MATCH");

		}

		// declaration occurrence of local interface
		public void testBug570246_004() throws CoreException {
			this.workingCopies = new ICompilationUnit[1];
			this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
					"public class X {\n"+
					" static void foo() {\n"+
					"   interface F {\n"+
					"     static int create(int lo) {\n"+
					"       I myI = s -> lo;\n"+
					"       return myI.bar(0);\n"+
					"     }\n"+
					"   }\n"+
					"   System.out.println(F.create(0));\n"+
					"     }\n"+
					" public static void main(String[] args) {\n"+
					"   X.foo();\n"+
					" }\n"+
					"}\n"+
					"\n"+
					"interface I {\n"+
					" int bar(int l);\n"+
					"}"

					);
			search("F", INTERFACE, DECLARATIONS);
			assertSearchResults("src/X.java void X.foo():F#1 [F] EXACT_MATCH");

		}

		// all occurrence of local interface
		public void testBug570246_005() throws CoreException {
			this.workingCopies = new ICompilationUnit[1];
			this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
					"public class X {\n"+
					" static void foo() {\n"+
					"   interface F {\n"+
					"     static int create(int lo) {\n"+
					"       I myI = s -> lo;\n"+
					"       return myI.bar(0);\n"+
					"     }\n"+
					"   }\n"+
					"   System.out.println(F.create(0));\n"+
					"     }\n"+
					" public static void main(String[] args) {\n"+
					"   X.foo();\n"+
					" }\n"+
					"}\n"+
					"\n"+
					"interface I {\n"+
					" int bar(int l);\n"+
					"}"

					);
			search("F", INTERFACE, ALL_OCCURRENCES);
			assertSearchResults("src/X.java void X.foo():F#1 [F] EXACT_MATCH\n"
					+ "src/X.java void X.foo() [F] EXACT_MATCH");

		}

		public void testBug570246_006() throws CoreException {
				this.workingCopies = new ICompilationUnit[1];
				this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
						"public class X {\n"+
								" static void foo() {\n"+
								"   int f = switch (5) {\n"+
								"			case 5: {\n"+
								"				interface Inter{\n"+
								"					\n"+
								"				}\n"+
								"				class C implements Inter{\n"+
								"					public int j = 5;\n"+
								"				}\n"+
								"				\n"+
								"				yield new C().j;\n"+
								"			}\n"+
								"			default:\n"+
								"				throw new IllegalArgumentException(\"Unexpected value: \" );\n"+
								"			};\n"+
								"	System.out.println(f);\n"+
								" }\n"+
								" public static void main(String[] args) {\n"+
								"   X.foo();\n"+
								" }\n"+
								"}"

						);
				search("Inter", INTERFACE, ALL_OCCURRENCES);
				assertSearchResults("src/X.java void X.foo():Inter#1 [Inter] EXACT_MATCH\n"
						+ "src/X.java void X.foo():C#1 [Inter] EXACT_MATCH");

			}
		public void testBug572100 () throws CoreException {
			this.workingCopies = new ICompilationUnit[1];
			this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/Bug572100/X.java",
					"public interface X {\n"+
				 " interface inter1  {\n"+
				   " record record1(Class<?> type) implements inter1 {\n"+
				  "    public record1 {\n"+
				   "     if (!type.isPrimitive()) {\n"+
				   "    }\n"+
				    "  }\n"+
				   " }\n"+
				 " }\n"+
				"}\n"
				);
			String str = this.workingCopies[0].getSource();
			String selection = "inter1";
			int start = str.indexOf(selection);
			int length = selection.length();
			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			SourceType st = (SourceType)elements[0];
			SearchPattern pattern = new DeclarationOfAccessedFieldsPattern(st);
			new SearchEngine(this.workingCopies).search(pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			getJavaSearchWorkingCopiesScope(),
			this.resultCollector,
			null);
			assertSearchResults(
					""
			);
		}
		public void testBug573388 () throws CoreException {
			this.workingCopies = new ICompilationUnit[3];
			this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b573388/X.java",
				"package b573388;\n\n" +
				"public class X {\n"+
					"public static void main() {\n" +
					"		R r= new R(7);\n"+
					"		C c= new C();\n"+
					"}\n"+
				"}\n"
				);
			this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b573388/C.java",
					"package b573388;\n\n" +
					"public class C {\n"+
					"}\n"
					);
			this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b573388/R.java",
				"package b573388;\n\n" +
				"public record R(int a) {\n"+
				"}\n"
				);
			String str = this.workingCopies[2].getSource();
			String selection = "X";
			int start = str.indexOf(selection);
			int length = selection.length();
			IJavaElement[] elements = this.workingCopies[2].codeSelect(start, length);
			SourceType st = (SourceType)elements[0];
			new SearchEngine(this.workingCopies).searchDeclarationsOfReferencedTypes(st, this.resultCollector, null);
			assertSearchResults(
					"src/b573388/R.java b573388.R [R] EXACT_MATCH\n" +
					"src/b573388/C.java b573388.C [C] EXACT_MATCH"
			);
		}
		public void testBug574870_1() throws CoreException {
			this.workingCopies = new ICompilationUnit[1];
			this.workingCopies[0] = getWorkingCopy(
					"/JavaSearchBugs/src/X.java",
					"public class X {\n" +
							"private void method(Object o) {\n" +
							"if ((o instanceof String xvar )) \n" +
							"{\n" +
							" System.out.println(/*here*/xvar);\n" +
							"}\n" +

							"}\n" +

					"}");

			// working copies
			try {

				String str = this.workingCopies[0].getSource();
				String selection = "/*here*/xvar";
				int start = str.indexOf(selection);
				int length = selection.length();

				IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
				ILocalVariable local = (ILocalVariable) elements[0];
				search(local, DECLARATIONS, EXACT_RULE);
				assertSearchResults("src/X.java void X.method(Object).xvar [xvar] EXACT_MATCH");

			} finally {

			}
		}
		public void testBug574870_2() throws CoreException {
			this.workingCopies = new ICompilationUnit[1];
			this.workingCopies[0] = getWorkingCopy(
					"/JavaSearchBugs/src/X.java",
					"public class X {\n" +
							"private void method(Object o) {\n" +
							"if ((o instanceof String /*here*/xvar )) \n" +
							"{\n" +
							" System.out.println(xvar+xvar);\n" +
							"}\n" +

							"}\n" +

					"}");

			// working copies
			try {

				String str = this.workingCopies[0].getSource();
				String selection = "/*here*/xvar";
				int start = str.indexOf(selection);
				int length = selection.length();

				IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
				ILocalVariable local = (ILocalVariable) elements[0];
				search(local, REFERENCES, EXACT_RULE);
				assertSearchResults("src/X.java void X.method(Object) [xvar] EXACT_MATCH\n"
						+ "src/X.java void X.method(Object) [xvar] EXACT_MATCH");

			} finally {

			}
		}
		public void testBug574870_3() throws CoreException {
			this.workingCopies = new ICompilationUnit[1];
			this.workingCopies[0] = getWorkingCopy(
					"/JavaSearchBugs/src/X.java",
					"public class X {\n" +
							"private void method(Object o) {\n" +
							"if ((o instanceof String /*here*/xvar )) \n" +
							"{\n" +
							" System.out.println(xvar+xvar);\n" +
							"}\n" +

							"}\n" +

					"}");

			// working copies
			try {

				String str = this.workingCopies[0].getSource();
				String selection = "/*here*/xvar";
				int start = str.indexOf(selection);
				int length = selection.length();

				IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
				ILocalVariable local = (ILocalVariable) elements[0];
				search(local, ALL_OCCURRENCES, EXACT_RULE);
				assertSearchResults("src/X.java void X.method(Object).xvar [xvar] EXACT_MATCH\n"
						+ "src/X.java void X.method(Object) [xvar] EXACT_MATCH\n"
						+ "src/X.java void X.method(Object) [xvar] EXACT_MATCH");

			} finally {

			}
		}
		public void testBug574870_4() throws CoreException {
			this.workingCopies = new ICompilationUnit[1];
			this.workingCopies[0] = getWorkingCopy(
					"/JavaSearchBugs/src/X.java",
					"public class X {\n" +
							"private void method(Object o) {\n" +
							"if (o instanceof String xvar ) \n" +
							"{\n" +
							" System.out.println(/*here*/xvar);\n" +
							"}\n" +

							"}\n" +

					"}");

			// working copies
			try {

				String str = this.workingCopies[0].getSource();
				String selection = "/*here*/xvar";
				int start = str.indexOf(selection);
				int length = selection.length();
				IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
				assertEquals("incorrect number of elements", 1, elements.length);
				ILocalVariable local = (ILocalVariable) elements[0];
				search(local, DECLARATIONS, EXACT_RULE);
				assertSearchResults("src/X.java void X.method(Object).xvar [xvar] EXACT_MATCH");

			} finally {

			}
		}

	/*
	 * A record defined in a JRE class causes an AIOOBE during a call hierarchy computation.
	 * The record is encountered while indexing a Java project with a snippet,
	 * when computing the call hierarchy of a field in the snippet.
	 * The same AIOOBE can be reproduced with a reference search in the same snippet.
	 * https://github.com/eclipse-jdt/eclipse.jdt.core/issues/790
	 */
	public void testAIOOBEForRecordClassGh790() throws Exception {
		String testProjectName = "gh790AIOOBEForRecordClass";
		try {
			IJavaProject project = createJava16Project(testProjectName, new String[] {"src"});
			String packageFolder = "/" + testProjectName + "/src/test";
			createFolder(packageFolder);
			String testSource =
				"package test;\n" +
				"public class Test {\n" +
				"protected final java.util.HashMap<?, ?> internal;\n" +
				"protected final java.util.HashMap<?, ?> map;\n" +
				"    public Test() {\n" +
				"	    internal = new java.util.HashMap<>();\n" +
				"	    map = internal;\n" +
				"    }\n" +
			    "}\n";
			createFile(packageFolder + "/Test.java", testSource);
			buildAndExpectNoProblems(project);

			IType type = project.findType("test.Test");
			IField field = type.getField("internal");
			search(field, REFERENCES, EXACT_RULE, SearchEngine.createWorkspaceScope(), this.resultCollector);
			assertSearchResults(
					"src/test/Test.java test.Test() [internal] EXACT_MATCH\n" +
					"src/test/Test.java test.Test() [internal] EXACT_MATCH");
		} finally {
			deleteProject(testProjectName);
		}
	}

}


