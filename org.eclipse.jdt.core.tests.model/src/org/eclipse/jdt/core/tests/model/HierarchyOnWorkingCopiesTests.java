/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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

import java.io.IOException;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IOrdinaryClassFile;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;

public class HierarchyOnWorkingCopiesTests extends WorkingCopyTests {

static {
//	TESTS_NAMES = new String [] { "test450442" };
}

public HierarchyOnWorkingCopiesTests(String name) {
	super(name);
}

public static Test suite() {
	return buildModelTestSuite(HierarchyOnWorkingCopiesTests.class);
	/* NOTE: cannot use 'new Suite(HierarchyOnWorkingCopiesTests.class)' as this would include tests from super class
	TestSuite suite = new Suite(HierarchyOnWorkingCopiesTests.class.getName());

	suite.addTest(new HierarchyOnWorkingCopiesTests("testSimpleSuperTypeHierarchy"));
	suite.addTest(new HierarchyOnWorkingCopiesTests("testSimpleSubTypeHierarchy"));

	return suite;
	*/
}

@Override
protected void setUp() throws Exception {
	this.indexDisabledForTest = false;
	super.setUp();
}

public void testSimpleSubTypeHierarchy() throws CoreException {
	String newContents =
		"""
		package x.y;
		public class A extends B {
		}""";
	this.copy.getBuffer().setContents(newContents);
	this.copy.reconcile(ICompilationUnit.NO_AST, false, null, null);

	IFile file = null;
	try {
		file = this.createFile(
			"P/src/x/y/B.java",
			"""
				package x.y;
				public class B {
				}""");

		IType type = this.getCompilationUnit("P/src/x/y/B.java").getType("B");
		ITypeHierarchy h = type.newTypeHierarchy(new ICompilationUnit[] {this.copy}, null);

		assertHierarchyEquals(
			"Focus: B [in B.java [in x.y [in src [in P]]]]\n" +
			"Super types:\n" +
			"  Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
			"Sub types:\n" +
			"  A [in [Working copy] A.java [in x.y [in src [in P]]]]\n",
			h);
	} finally {
		if (file != null) {
			this.deleteResource(file);
		}
	}
}
public void testSimpleSuperTypeHierarchy() throws CoreException {
	String newContents =
		"""
		package x.y;
		public class A {
		}
		class B {
		}""";
	this.copy.getBuffer().setContents(newContents);
	this.copy.reconcile(ICompilationUnit.NO_AST, false, null, null);

	IFile file = null;
	try {
		file = this.createFile(
			"P/src/x/y/C.java",
			"""
				package x.y;
				public class C extends B {
				}""");

		IType type = this.getCompilationUnit("P/src/x/y/C.java").getType("C");
		ITypeHierarchy h = type.newSupertypeHierarchy(new ICompilationUnit[] {this.copy}, null);

		assertHierarchyEquals(
			"Focus: C [in C.java [in x.y [in src [in P]]]]\n" +
			"Super types:\n" +
			"  B [in [Working copy] A.java [in x.y [in src [in P]]]]\n" +
			"    Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
			"Sub types:\n",
			h);
	} finally {
		if (file != null) {
			this.deleteResource(file);
		}
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=228845
//make sure uncommitted changes to primary working copy shows up in hierarchy
public void test228845() throws CoreException {
	String newContents =
		"""
		package x.y;
		public class A extends B {
		}""";

	ICompilationUnit primaryCu = this.copy.getPrimary();
	primaryCu.becomeWorkingCopy(null);

	primaryCu.getBuffer().setContents(newContents);
	primaryCu.reconcile(ICompilationUnit.NO_AST, false, null, null);

	IFile file = null;
	try {
		file = this.createFile(
			"P/src/x/y/B.java",
			"""
				package x.y;
				public class B {
				}""");

		IType type = this.getCompilationUnit("P/src/x/y/B.java").getType("B");
		ITypeHierarchy h = type.newTypeHierarchy(null);  // no working copies explicitly passed, should still honor primary working copies.

		assertHierarchyEquals(
			"Focus: B [in B.java [in x.y [in src [in P]]]]\n" +
			"Super types:\n" +
			"  Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
			"Sub types:\n" +
			"  A [in [Working copy] A.java [in x.y [in src [in P]]]]\n",
			h);
	} finally {
		primaryCu.discardWorkingCopy();
		if (file != null) {
			this.deleteResource(file);
		}
	}
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=228845
//make sure uncommitted changes to primary working copy shows up in hierarchy
//created out of a BinaryType.
public void test228845b() throws CoreException, IOException {

	addLibrary(getJavaProject("P"), "myLib.jar", "myLibsrc.zip", new String[] {
			"my/pkg/X.java",
			"""
				package my.pkg;
				public class X {
				}""",
			"my/pkg/Y.java",
			"""
				package my.pkg;
				public class Y {
				  }
				""",
		}, JavaCore.VERSION_1_4);


	IFile file = null;
	ICompilationUnit primaryCu = null;

	try {
		file = this.createFile(
			"P/src/Q.java",
			"public class Q {} \n");

		primaryCu = this.getCompilationUnit("P/src/Q.java").getWorkingCopy(null).getPrimary();
		primaryCu.becomeWorkingCopy(null);

		String newContents =
		"public class Q extends my.pkg.X {\n" +
		"}";

		primaryCu.getBuffer().setContents(newContents);
		primaryCu.reconcile(ICompilationUnit.NO_AST, false, null, null);

		IOrdinaryClassFile cf = getClassFile("P", "myLib.jar", "my.pkg", "X.class");
		IType typ = cf.getType();

		ITypeHierarchy h = typ.newTypeHierarchy(null);

		assertHierarchyEquals(
			"Focus: X [in X.class [in my.pkg [in myLib.jar [in P]]]]\n" +
			"Super types:\n" +
			"  Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
			"Sub types:\n" +
			"  Q [in [Working copy] Q.java [in <default> [in src [in P]]]]\n",
			h);
	} finally {
		if (primaryCu != null) {
			primaryCu.discardWorkingCopy();
		}
		if (file!= null) {
			this.deleteResource(file);
		}
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=228845
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400905
// Fix for 228845 does not seem to work for anonymous/local/functional types.
public void test400905() throws CoreException {
	String newContents =
		"""
		package x.y;
		public class A {
		    void foo() {
		        class X extends B {}
		    }
		}""";

	ICompilationUnit primaryCu = this.copy.getPrimary();
	primaryCu.becomeWorkingCopy(null);

	primaryCu.getBuffer().setContents(newContents);
	primaryCu.reconcile(ICompilationUnit.NO_AST, false, null, null);

	IFile file = null;
	try {
		file = this.createFile(
			"P/src/x/y/B.java",
			"""
				package x.y;
				public class B {
				}""");

		IType type = this.getCompilationUnit("P/src/x/y/B.java").getType("B");
		ITypeHierarchy h = type.newTypeHierarchy(null);  // no working copies explicitly passed, should still honor primary working copies.

		assertHierarchyEquals(
				"Focus: B [in B.java [in x.y [in src [in P]]]]\n" +
				"Super types:\n" +
				"  Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
				"Sub types:\n" +
				"  X [in foo() [in A [in [Working copy] A.java [in x.y [in src [in P]]]]]]\n",
			h);
	} finally {
		primaryCu.discardWorkingCopy();
		if (file != null) {
			this.deleteResource(file);
		}
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=228845
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400905
// Fix for 228845 does not seem to work for anonymous/local/functional types.
public void test400905a() throws CoreException {
	String newContents =
		"""
		package x.y;
		public class A {
		    void foo() {
		        X x  = new B() {}
		    }
		}""";

	ICompilationUnit primaryCu = this.copy.getPrimary();
	primaryCu.becomeWorkingCopy(null);

	primaryCu.getBuffer().setContents(newContents);
	primaryCu.reconcile(ICompilationUnit.NO_AST, false, null, null);

	IFile file = null;
	try {
		file = this.createFile(
			"P/src/x/y/B.java",
			"""
				package x.y;
				public class B {
				}""");

		IType type = this.getCompilationUnit("P/src/x/y/B.java").getType("B");
		ITypeHierarchy h = type.newTypeHierarchy(null);  // no working copies explicitly passed, should still honor primary working copies.

		assertHierarchyEquals(
				"Focus: B [in B.java [in x.y [in src [in P]]]]\n" +
				"Super types:\n" +
				"  Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
				"Sub types:\n" +
				"  <anonymous #1> [in foo() [in A [in [Working copy] A.java [in x.y [in src [in P]]]]]]\n",
			h);
	} finally {
		primaryCu.discardWorkingCopy();
		if (file != null) {
			this.deleteResource(file);
		}
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=228845
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=400905
// Fix for 228845 does not seem to work for anonymous/local/functional types.
public void test400905b() throws CoreException, IOException {
	IJavaProject javaProject = getJavaProject("P");
	String oldCompliance = javaProject.getOption(JavaCore.COMPILER_COMPLIANCE, true);
	String oldSource = javaProject.getOption(JavaCore.COMPILER_SOURCE, true);
	try {
		javaProject.setOption(JavaCore.COMPILER_COMPLIANCE, "1.8");
		javaProject.setOption(JavaCore.COMPILER_SOURCE, "1.8");
		String newContents =
						"""
			package x.y;
			interface I {\s
				int thrice(int x);
			}
			interface J {
				int twice(int x);
			}
			public class X {
				I i = (x) -> {return x * 3;};\s
				X x = null;
				static void goo(I i) {}\s
				public static void main(String[] args) {\s
					goo((x)-> {\s
						int y = 3;
						return x * y;\s
					});
					I i2 = (x) -> {
						int y = 3;\s
						return x * y;
					};
					J j1 = (x) -> {\s
						int y = 2; \s
						return x * y;
					}; \s
				}
			}
			""";

		ICompilationUnit primaryCu = this.copy.getPrimary();
		primaryCu.becomeWorkingCopy(null);

		primaryCu.getBuffer().setContents(newContents);
		primaryCu.reconcile(ICompilationUnit.NO_AST, false, null, null);

		try {
			IType type = primaryCu.getType("I");
			ITypeHierarchy h = type.newTypeHierarchy(null);  // no working copies explicitly passed, should still honor primary working copies.

			assertHierarchyEquals(
							"""
								Focus: I [in [Working copy] A.java [in x.y [in src [in P]]]]
								Super types:
								Sub types:
								  <lambda #1> [in i [in X [in [Working copy] A.java [in x.y [in src [in P]]]]]]
								  <lambda #1> [in main(String[]) [in X [in [Working copy] A.java [in x.y [in src [in P]]]]]]
								  <lambda #1> [in main(String[]) [in X [in [Working copy] A.java [in x.y [in src [in P]]]]]]
								""",
				h);
		} finally {
			primaryCu.discardWorkingCopy();
		}
	} finally {
		if (oldCompliance != null)
			javaProject.setOption(JavaCore.COMPILER_COMPLIANCE, oldCompliance);
		if (oldSource != null)
			javaProject.setOption(JavaCore.COMPILER_SOURCE, oldSource);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429435, [1.8][search]Hierarchy search for lambda expressions do not show all the lambda expressions
public void test429435() throws CoreException, IOException {
	IJavaProject javaProject = getJavaProject("P");
	String oldCompliance = javaProject.getOption(JavaCore.COMPILER_COMPLIANCE, true);
	String oldSource = javaProject.getOption(JavaCore.COMPILER_SOURCE, true);
	try {
		javaProject.setOption(JavaCore.COMPILER_COMPLIANCE, "1.8");
		javaProject.setOption(JavaCore.COMPILER_SOURCE, "1.8");
		String newContents =
						"""
			package x.y;
			interface I {
			    public int doit();
			}
			public class X {
			void zoo() {
				    I i = () /*1*/-> {
			                 I i2 = () /*2*/-> Y.foo(() -> Y.foo(()->Y.foo(()->10)));
			                 final Y y = new Y() {
			                		@Override
			                		public int doit() {
			                			I i = () -> 10;
			                			return i.doit();
			                		}
			                 };
			                 return 0;
			       };
			   }
			}
			 class Y implements I{
			
				static int foo(I i) { return 0;}
				@Override
				public int doit() {
					// TODO Auto-generated method stub
					return 0;
				}	\s
			}
			""";

		ICompilationUnit primaryCu = this.copy.getPrimary();
		primaryCu.becomeWorkingCopy(null);

		primaryCu.getBuffer().setContents(newContents);
		primaryCu.reconcile(ICompilationUnit.NO_AST, false, null, null);

		try {
			IType type = primaryCu.getType("I");
			ITypeHierarchy h = type.newTypeHierarchy(null);  // no working copies explicitly passed, should still honor primary working copies.

			assertHierarchyEquals(
					"""
						Focus: I [in [Working copy] A.java [in x.y [in src [in P]]]]
						Super types:
						Sub types:
						  <lambda #1> [in doit() [in <anonymous #1> [in doit() [in <lambda #1> [in zoo() [in X [in [Working copy] A.java [in x.y [in src [in P]]]]]]]]]]
						  <lambda #1> [in doit() [in <lambda #1> [in doit() [in <lambda #1> [in doit() [in <lambda #1> [in doit() [in <lambda #1> [in zoo() [in X [in [Working copy] A.java [in x.y [in src [in P]]]]]]]]]]]]]]
						  <lambda #1> [in doit() [in <lambda #1> [in doit() [in <lambda #1> [in doit() [in <lambda #1> [in zoo() [in X [in [Working copy] A.java [in x.y [in src [in P]]]]]]]]]]]]
						  <lambda #1> [in doit() [in <lambda #1> [in doit() [in <lambda #1> [in zoo() [in X [in [Working copy] A.java [in x.y [in src [in P]]]]]]]]]]
						  <lambda #1> [in doit() [in <lambda #1> [in zoo() [in X [in [Working copy] A.java [in x.y [in src [in P]]]]]]]]
						  <lambda #1> [in zoo() [in X [in [Working copy] A.java [in x.y [in src [in P]]]]]]
						  Y [in [Working copy] A.java [in x.y [in src [in P]]]]
						""",
				h);
		} finally {
			primaryCu.discardWorkingCopy();
		}
	} finally {
		if (oldCompliance != null)
			javaProject.setOption(JavaCore.COMPILER_COMPLIANCE, oldCompliance);
		if (oldSource != null)
			javaProject.setOption(JavaCore.COMPILER_SOURCE, oldSource);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429537, [1.8][hierarchy]NPE in hierarchy resolution
public void test429537() throws CoreException, IOException {
	IJavaProject javaProject = getJavaProject("P");
	String oldCompliance = javaProject.getOption(JavaCore.COMPILER_COMPLIANCE, true);
	String oldSource = javaProject.getOption(JavaCore.COMPILER_SOURCE, true);
	try {
		javaProject.setOption(JavaCore.COMPILER_COMPLIANCE, "1.8");
		javaProject.setOption(JavaCore.COMPILER_SOURCE, "1.8");
		String newContents =
						"""
			package x.y;
			public class X extends Y {
			public static void main(String [] args) {
				I<Y> c = () /* foo */ -> () /* bar */ -> {};
				I<Y> y = args.length < 1 ? (() /* true */-> 42) : (() /* false */ -> 23);
				Object o = (I) () /* cast */ -> 42;
				}
			}
			interface I<T> {
				public T foo();
			}
			class Y {
				public void bar() {}
			}
			""";

		ICompilationUnit primaryCu = this.copy.getPrimary();
		primaryCu.becomeWorkingCopy(null);

		primaryCu.getBuffer().setContents(newContents);
		primaryCu.reconcile(ICompilationUnit.NO_AST, false, null, null);

		try {
			IType type = primaryCu.getType("I");
			ITypeHierarchy h = type.newTypeHierarchy(null);  // no working copies explicitly passed, should still honor primary working copies.

			assertHierarchyEquals(
							"""
								Focus: I [in [Working copy] A.java [in x.y [in src [in P]]]]
								Super types:
								Sub types:
								  <lambda #1> [in main(String[]) [in X [in [Working copy] A.java [in x.y [in src [in P]]]]]]
								  <lambda #1> [in main(String[]) [in X [in [Working copy] A.java [in x.y [in src [in P]]]]]]
								  <lambda #1> [in main(String[]) [in X [in [Working copy] A.java [in x.y [in src [in P]]]]]]
								  <lambda #1> [in main(String[]) [in X [in [Working copy] A.java [in x.y [in src [in P]]]]]]
								""",
				h);
		} finally {
			primaryCu.discardWorkingCopy();
		}
	} finally {
		if (oldCompliance != null)
			javaProject.setOption(JavaCore.COMPILER_COMPLIANCE, oldCompliance);
		if (oldSource != null)
			javaProject.setOption(JavaCore.COMPILER_SOURCE, oldSource);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=442534, Eclipse's Run button does not work.
public void test442534() throws CoreException, IOException {

	IJavaProject project = null;
	try {
		project = this.createJavaProject(
				"Bug442534",
				new String[] {"src"},
				new String[] {this.getExternalJCLPathString(), "lib"},
				"bin");
		project.setOption(JavaCore.COMPILER_COMPLIANCE, "1.8");
		project.setOption(JavaCore.COMPILER_SOURCE, "1.8");

		this.createFolder("Bug442534/src/q");
		this.createFile("Bug442534/src/X.java",
				"""
					import q.*;
					public final class X {
						public static void main(String[] args) {
							foo(e -> { if (new Object() instanceof Y);});
						};
						static void foo(I i) {
							return;
						}
					}
					interface I {
						void foo(int x);
					}
					""");

		this.createFile("Bug442534/src/q/Y.java",
				"""
					package q;
					public class Y {
					}
					""");

		this.createFile("Bug442534/src/q/package-info.java",
				"package q;\n"
				);

		project.findType("X").newSupertypeHierarchy(null);
	} finally {
		if (project != null)
			this.deleteProject(project);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=450442,  [1.8] NPE at HandleFactory.createElement on hover
public void test450442() throws CoreException, IOException {

	IJavaProject project = null;
	try {
		project = this.createJavaProject(
				"Bug450442",
				new String[] {"src"},
				new String[] {this.getExternalJCLPathString(), "lib"},
				"bin");
		project.setOption(JavaCore.COMPILER_COMPLIANCE, "1.8");
		project.setOption(JavaCore.COMPILER_SOURCE, "1.8");

		this.createFile("Bug450442/src/Claxx.java",
				"""
					import java.util.function.Consumer;
					public class Claxx {
						void post(Runnable r) {
							r.run();
						}
						void absorb(Consumer<Claxx> c) throws Exception {
							c.accept(this);
						}
					\t
						static void execute() {
							System.out.println("exec!");
						}
						static void executeGiven(Object o) {
							System.out.println("exec " + o);
						}
						void executeObject() {
							System.out.println("exec " + this);
						}\t
					}
					class ClaxxTest {
						Claxx claxx = new Claxx();
					\t
						void doInBackground() throws Exception {
							claxx.post(Claxx::execute);\s
							absorb(Claxx::executeGiven);
							post(this::executeObject);
						\t
							absorb(Claxx::executeObject);
							post(() -> execute());
							post(() -> executeGiven(this)); // not convertible
							post(() -> executeObject());
						}
					}
					""");

		ITypeHierarchy h = project.findType("Claxx").newSupertypeHierarchy(null);
		assertHierarchyEquals(
				"Focus: Claxx [in Claxx.java [in <default> [in src [in Bug450442]]]]\n" +
				"Super types:\n" +
				"  Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
				"Sub types:\n",
				h);
	} finally {
		if (project != null)
			this.deleteProject(project);
	}
}
}

