/*******************************************************************************
 * Copyright (c)  2021, 2023 IBM Corporation and others.
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
			"""
				public class X2 {
				public static void main(String[] args) {
				 enum Y2 {
					BLEU,
					BLANC,
					ROUGE;
					public static void main(String[] args) {
						for(Y2 y: Y2.values()) {
							System.out.print(y);
						}
					}
				  }
				  Y2.main(args);
					}
				}
				"""

				);
		search("Y2", ENUM, ALL_OCCURRENCES);
		assertSearchResults("""
			src/X.java void X2.main(String[]):Y2#1 [Y2] EXACT_MATCH
			src/X.java void void X2.main(String[]):Y2#1.main(String[]) [Y2] EXACT_MATCH
			src/X.java void void X2.main(String[]):Y2#1.main(String[]) [Y2] EXACT_MATCH
			src/X.java void X2.main(String[]) [Y2] EXACT_MATCH""");

	}

	// declaration occurrence of local enum
		public void testBug570246_002() throws CoreException {
			this.workingCopies = new ICompilationUnit[1];
			this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"""
					public class X2 {
					public static void main(String[] args) {
					 enum Y2 {
						BLEU,
						BLANC,
						ROUGE;
						public static void main(String[] args) {
							for(Y2 y: Y2.values()) {
								System.out.print(y);
							}
						}
					  }
					  Y2.main(args);
						}
					}
					"""

					);
			search("Y2", ENUM, DECLARATIONS);
			assertSearchResults("src/X.java void X2.main(String[]):Y2#1 [Y2] EXACT_MATCH");

		}

		// declaration occurrence of local enum
		public void testBug570246_003a() throws CoreException {
			this.workingCopies = new ICompilationUnit[1];
			this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",

					"""
						public class X {
						    public static void main(String[] args) {
						          enum Role { M, D }
						 enum T {
						       PHILIPPE(37) {
						               public boolean isManager() {
						                       return true;
						               }
						       },
						       DAVID(27),
						       JEROME(33),
						       OLIVIER(35),
						       KENT(40),
						       YODA(41),
						       FREDERIC;
						       final static int OLD = 41;
						
						
						   int age;
						       Role role;
						
						       T() { this(OLD); }
						       T(int age) {
						               this.age = age;
						       }
						       public int age() { return this.age; }
						       public boolean isManager() { return false; }
						       void setRole(boolean mgr) {
						               this.role = mgr ? Role.M : Role.D;
						       }
						}
						       System.out.print("JDTCore team:");
						       T oldest = null;
						       int maxAge = Integer.MIN_VALUE;
						       for (T t : T.values()) {
						            if (t == T.YODA) continue;// skip YODA
						            t.setRole(t.isManager());
						                        if (t.age() > maxAge) {
						               oldest = t;
						               maxAge = t.age();
						            }
						                      Location l = switch(t) {
						                         case PHILIPPE, DAVID, JEROME, FREDERIC-> Location.SNZ;
						                         case OLIVIER, KENT -> Location.OTT;
						                         default-> throw new AssertionError("Unknown team member: " + t);
						                       };
						
						            System.out.print(" "+ t + ':'+t.age()+':'+l+':'+t.role);
						        }
						        System.out.println(" WINNER is:" + T.valueOf(oldest.name()));
						    }
						
						   private enum Location { SNZ, OTT }
						}"""
					);
			search("T", ENUM, ALL_OCCURRENCES);
			assertSearchResults("""
				src/X.java void X.main(String[]):T#1 [T] EXACT_MATCH
				src/X.java void X.main(String[]) [T] EXACT_MATCH
				src/X.java void X.main(String[]) [T] EXACT_MATCH
				src/X.java void X.main(String[]) [T] EXACT_MATCH
				src/X.java void X.main(String[]) [T] EXACT_MATCH
				src/X.java void X.main(String[]) [T] EXACT_MATCH""");


		}
		public void testBug570246_003b() throws CoreException {
			this.workingCopies = new ICompilationUnit[1];
			this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",

					"""
						public class X {
						    public static void main(String[] args) {
						          enum Role { M, D }
						 enum T {
						       PHILIPPE(37) {
						               public boolean isManager() {
						                       return true;
						               }
						       },
						       DAVID(27),
						       JEROME(33),
						       OLIVIER(35),
						       KENT(40),
						       YODA(41),
						       FREDERIC;
						       final static int OLD = 41;
						
						
						   int age;
						       Role role;
						
						       T() { this(OLD); }
						       T(int age) {
						               this.age = age;
						       }
						       public int age() { return this.age; }
						       public boolean isManager() { return false; }
						       void setRole(boolean mgr) {
						               this.role = mgr ? Role.M : Role.D;
						       }
						}
						       System.out.print("JDTCore team:");
						       T oldest = null;
						       int maxAge = Integer.MIN_VALUE;
						       for (T t : T.values()) {
						            if (t == T.YODA) continue;// skip YODA
						            t.setRole(t.isManager());
						                        if (t.age() > maxAge) {
						               oldest = t;
						               maxAge = t.age();
						            }
						                      Location l = switch(t) {
						                         case PHILIPPE, DAVID, JEROME, FREDERIC-> Location.SNZ;
						                         case OLIVIER, KENT -> Location.OTT;
						                         default-> throw new AssertionError("Unknown team member: " + t);
						                       };
						
						            System.out.print(" "+ t + ':'+t.age()+':'+l+':'+t.role);
						        }
						        System.out.println(" WINNER is:" + T.valueOf(oldest.name()));
						    }
						
						   private enum Location { SNZ, OTT }
						}"""
					);
			search("Role", ENUM, ALL_OCCURRENCES);
			assertSearchResults("""
				src/X.java void X.main(String[]):Role#1 [Role] EXACT_MATCH
				src/X.java void X.main(String[]):T#1.role [Role] EXACT_MATCH
				src/X.java void void X.main(String[]):T#1.setRole(boolean) [Role] EXACT_MATCH
				src/X.java void void X.main(String[]):T#1.setRole(boolean) [Role] EXACT_MATCH""");

		}

		// declaration occurrence of local interface
		public void testBug570246_004() throws CoreException {
			this.workingCopies = new ICompilationUnit[1];
			this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
					"""
						public class X {
						 static void foo() {
						   interface F {
						     static int create(int lo) {
						       I myI = s -> lo;
						       return myI.bar(0);
						     }
						   }
						   System.out.println(F.create(0));
						     }
						 public static void main(String[] args) {
						   X.foo();
						 }
						}
						
						interface I {
						 int bar(int l);
						}"""

					);
			search("F", INTERFACE, DECLARATIONS);
			assertSearchResults("src/X.java void X.foo():F#1 [F] EXACT_MATCH");

		}

		// all occurrence of local interface
		public void testBug570246_005() throws CoreException {
			this.workingCopies = new ICompilationUnit[1];
			this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
					"""
						public class X {
						 static void foo() {
						   interface F {
						     static int create(int lo) {
						       I myI = s -> lo;
						       return myI.bar(0);
						     }
						   }
						   System.out.println(F.create(0));
						     }
						 public static void main(String[] args) {
						   X.foo();
						 }
						}
						
						interface I {
						 int bar(int l);
						}"""

					);
			search("F", INTERFACE, ALL_OCCURRENCES);
			assertSearchResults("src/X.java void X.foo():F#1 [F] EXACT_MATCH\n"
					+ "src/X.java void X.foo() [F] EXACT_MATCH");

		}

		public void testBug570246_006() throws CoreException {
				this.workingCopies = new ICompilationUnit[1];
				this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
						"""
							public class X {
							 static void foo() {
							   int f = switch (5) {
										case 5: {
											interface Inter{
											\t
											}
											class C implements Inter{
												public int j = 5;
											}
										\t
											yield new C().j;
										}
										default:
											throw new IllegalArgumentException("Unexpected value: " );
										};
								System.out.println(f);
							 }
							 public static void main(String[] args) {
							   X.foo();
							 }
							}"""

						);
				search("Inter", INTERFACE, ALL_OCCURRENCES);
				assertSearchResults("src/X.java void X.foo():Inter#1 [Inter] EXACT_MATCH\n"
						+ "src/X.java void X.foo():C#1 [Inter] EXACT_MATCH");

			}
		public void testBug572100 () throws CoreException {
			this.workingCopies = new ICompilationUnit[1];
			this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/Bug572100/X.java",
					"""
						public interface X {
						 interface inter1  {
						 record record1(Class<?> type) implements inter1 {
						    public record1 {
						     if (!type.isPrimitive()) {
						    }
						  }
						 }
						 }
						}
						"""
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
				"""
					package b573388;
					
					public class X {
					public static void main() {
							R r= new R(7);
							C c= new C();
					}
					}
					"""
				);
			this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b573388/C.java",
					"""
						package b573388;
						
						public class C {
						}
						"""
					);
			this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b573388/R.java",
				"""
					package b573388;
					
					public record R(int a) {
					}
					"""
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
					"""
						public class X {
						private void method(Object o) {
						if ((o instanceof String xvar ))\s
						{
						 System.out.println(/*here*/xvar);
						}
						}
						}""");

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
					"""
						public class X {
						private void method(Object o) {
						if ((o instanceof String /*here*/xvar ))\s
						{
						 System.out.println(xvar+xvar);
						}
						}
						}""");

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
					"""
						public class X {
						private void method(Object o) {
						if ((o instanceof String /*here*/xvar ))\s
						{
						 System.out.println(xvar+xvar);
						}
						}
						}""");

			// working copies
			try {

				String str = this.workingCopies[0].getSource();
				String selection = "/*here*/xvar";
				int start = str.indexOf(selection);
				int length = selection.length();

				IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
				ILocalVariable local = (ILocalVariable) elements[0];
				search(local, ALL_OCCURRENCES, EXACT_RULE);
				assertSearchResults("""
					src/X.java void X.method(Object).xvar [xvar] EXACT_MATCH
					src/X.java void X.method(Object) [xvar] EXACT_MATCH
					src/X.java void X.method(Object) [xvar] EXACT_MATCH""");

			} finally {

			}
		}
		public void testBug574870_4() throws CoreException {
			this.workingCopies = new ICompilationUnit[1];
			this.workingCopies[0] = getWorkingCopy(
					"/JavaSearchBugs/src/X.java",
					"""
						public class X {
						private void method(Object o) {
						if (o instanceof String xvar )\s
						{
						 System.out.println(/*here*/xvar);
						}
						}
						}""");

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
				"""
				package test;
				public class Test {
				protected final java.util.HashMap<?, ?> internal;
				protected final java.util.HashMap<?, ?> map;
				    public Test() {
					    internal = new java.util.HashMap<>();
					    map = internal;
				    }
				}
				""";
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
	public void testGH1519_ImplicitRecordConstructors() throws CoreException {
		String testProjectName = "testGH1519_ImplicitRecordConstructors";
		try {
			IJavaProject project = createJava16Project(testProjectName, new String[] {"src"});
			String packageFolder = "/" + testProjectName + "/src";
			createFolder(packageFolder);
			String source = "public record Person (String name, int age) {}";
			createFile(packageFolder + "/Person.java", source);
			buildAndExpectNoProblems(project);

			ConstructorDeclarationsCollector requestor = new ConstructorDeclarationsCollector();
			searchAllConstructorDeclarations("Perso", SearchPattern.R_PREFIX_MATCH, requestor);
			assertSearchResults(
					".Person#Person(String name,int age)", requestor);
		} finally {
			deleteProject(testProjectName);
		}
	}

	/*
	 * unit test for https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1297
	 */
	public void testBug1297_01() throws Exception {
		IJavaProject project = null;
		try
		{
			project = createJavaProject("P", new String[] {""}, new String[] { "/P/lib1297.jar", "JCL15_LIB" }, "", "1.5");
			org.eclipse.jdt.core.tests.util.Util.createJar(new String[] {
					"p1/AnnotationTypes.java",
					"""
						package p1;
						import java.lang.annotation.*;
						@Retention(RetentionPolicy.RUNTIME)
						@Target(ElementType.METHOD)
						@interface MyCustomAnnotation {
							String value();
						}
						public class AnnotationTypes {
							@MyCustomAnnotation(value = "Eclipse")
							public void myMethod() {
								System.out.println("Annotated method called");
							}
							@MyCustomAnnotation(value = "Eclipse1")
							public void newAnnotation() { }
						}
						""" },
					project.getProject().getLocation().append("lib1297.jar").toOSString(), "1.5");


			refresh(project);


			createFolder("/P/p1");
			String testSource =
				"""
				package p1;
				import java.lang.annotation.*;
				@Retention(RetentionPolicy.RUNTIME)
				@Target(ElementType.METHOD)
				public class Test {
					@MyCustomAnnotation(value = "Custom Annotation Example")
					public void annotatedMethod() {\
						System.out.println("Annotated method called");
					}
					@MyCustomAnnotation(value = "Custom Annotation Example1")
					public void test123() {\
						System.out.println("Annotated method called");
					}
					public static void main(String[] args) throws NoSuchMethodException {
					}
				}
				""";
			createFile("/P/p1/Test.java", testSource);

			SearchPattern pattern = SearchPattern.createPattern(
					"p1.MyCustomAnnotation",
					ANNOTATION_TYPE,
					ANNOTATION_TYPE_REFERENCE,
					SearchPattern.R_EXACT_MATCH);

			IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project}, true);

			search(pattern, scope, this.resultCollector);
			assertSearchResults(
					"""
						p1/Test.java void p1.Test.annotatedMethod() [MyCustomAnnotation] EXACT_MATCH
						p1/Test.java void p1.Test.test123() [MyCustomAnnotation] EXACT_MATCH
						lib1297.jar void p1.AnnotationTypes.myMethod() [No source] EXACT_MATCH
						lib1297.jar void p1.AnnotationTypes.newAnnotation() [No source] EXACT_MATCH"""
					);
		} finally {
			deleteProject(project);
		}
	}

}


