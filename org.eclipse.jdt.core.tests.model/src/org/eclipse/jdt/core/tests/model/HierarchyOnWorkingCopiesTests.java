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

/**
 */
public void testSimpleSubTypeHierarchy() throws CoreException {
	String newContents =
		"package x.y;\n" +
		"public class A extends B {\n" +
		"}";
	this.copy.getBuffer().setContents(newContents);
	this.copy.reconcile(ICompilationUnit.NO_AST, false, null, null);

	IFile file = null;
	try {
		file = this.createFile(
			"P/src/x/y/B.java",
			"package x.y;\n" +
			"public class B {\n" +
			"}");

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
/**
 */
public void testSimpleSuperTypeHierarchy() throws CoreException {
	String newContents =
		"package x.y;\n" +
		"public class A {\n" +
		"}\n"  +
		"class B {\n" +
		"}";
	this.copy.getBuffer().setContents(newContents);
	this.copy.reconcile(ICompilationUnit.NO_AST, false, null, null);

	IFile file = null;
	try {
		file = this.createFile(
			"P/src/x/y/C.java",
			"package x.y;\n" +
			"public class C extends B {\n" +
			"}");

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
		"package x.y;\n" +
		"public class A extends B {\n" +
		"}";

	ICompilationUnit primaryCu = this.copy.getPrimary();
	primaryCu.becomeWorkingCopy(null);

	primaryCu.getBuffer().setContents(newContents);
	primaryCu.reconcile(ICompilationUnit.NO_AST, false, null, null);

	IFile file = null;
	try {
		file = this.createFile(
			"P/src/x/y/B.java",
			"package x.y;\n" +
			"public class B {\n" +
			"}");

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
			"package my.pkg;\n" +
			"public class X {\n" +
			"}",
			"my/pkg/Y.java",
			"package my.pkg;\n" +
			"public class Y {\n" +
			"  }\n",
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
		"package x.y;\n" +
		"public class A {\n" +
		"    void foo() {\n" +
        "        class X extends B {}\n" +
		"    }\n" +
		"}";

	ICompilationUnit primaryCu = this.copy.getPrimary();
	primaryCu.becomeWorkingCopy(null);

	primaryCu.getBuffer().setContents(newContents);
	primaryCu.reconcile(ICompilationUnit.NO_AST, false, null, null);

	IFile file = null;
	try {
		file = this.createFile(
			"P/src/x/y/B.java",
			"package x.y;\n" +
			"public class B {\n" +
			"}");

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
		"package x.y;\n" +
		"public class A {\n" +
		"    void foo() {\n" +
        "        X x  = new B() {}\n" +
		"    }\n" +
		"}";

	ICompilationUnit primaryCu = this.copy.getPrimary();
	primaryCu.becomeWorkingCopy(null);

	primaryCu.getBuffer().setContents(newContents);
	primaryCu.reconcile(ICompilationUnit.NO_AST, false, null, null);

	IFile file = null;
	try {
		file = this.createFile(
			"P/src/x/y/B.java",
			"package x.y;\n" +
			"public class B {\n" +
			"}");

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
						"package x.y;\n" +
						"interface I { \n" +
						"	int thrice(int x);\n" +
						"}\n" +
						"interface J {\n" +
						"	int twice(int x);\n" +
						"}\n" +
						"public class X {\n" +
						"	I i = (x) -> {return x * 3;}; \n" +
						"	X x = null;\n" +
						"	static void goo(I i) {} \n" +
						"	public static void main(String[] args) { \n" +
						"		goo((x)-> { \n" +
						"			int y = 3;\n" +
						"			return x * y; \n" +
						"		});\n" +
						"		I i2 = (x) -> {\n" +
						"			int y = 3; \n" +
						"			return x * y;\n" +
						"		};\n" +
						"		J j1 = (x) -> { \n" +
						"			int y = 2;  \n" +
						"			return x * y;\n" +
						"		};  \n" +
						"	}\n" +
						"}\n";

		ICompilationUnit primaryCu = this.copy.getPrimary();
		primaryCu.becomeWorkingCopy(null);

		primaryCu.getBuffer().setContents(newContents);
		primaryCu.reconcile(ICompilationUnit.NO_AST, false, null, null);

		try {
			IType type = primaryCu.getType("I");
			ITypeHierarchy h = type.newTypeHierarchy(null);  // no working copies explicitly passed, should still honor primary working copies.

			assertHierarchyEquals(
							"Focus: I [in [Working copy] A.java [in x.y [in src [in P]]]]\n" +
							"Super types:\n" +
							"Sub types:\n" +
							"  <lambda #1> [in i [in X [in [Working copy] A.java [in x.y [in src [in P]]]]]]\n" +
							"  <lambda #1> [in main(String[]) [in X [in [Working copy] A.java [in x.y [in src [in P]]]]]]\n" +
							"  <lambda #1> [in main(String[]) [in X [in [Working copy] A.java [in x.y [in src [in P]]]]]]\n",
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
						"package x.y;\n" +
						"interface I {\n" +
						"    public int doit();\n" +
						"}\n" +
						"public class X {\n" +
						"void zoo() {\n" +
						"	    I i = () /*1*/-> {\n" +
						"                 I i2 = () /*2*/-> Y.foo(() -> Y.foo(()->Y.foo(()->10)));\n" +
						"                 final Y y = new Y() {\n" +
						"                		@Override\n" +
						"                		public int doit() {\n" +
						"                			I i = () -> 10;\n" +
						"                			return i.doit();\n" +
						"                		}\n" +
						"                 };\n" +
						"                 return 0;\n" +
						"       };\n" +
						"   }\n" +
						"}\n" +
						" class Y implements I{\n" +
						"\n" +
						"	static int foo(I i) { return 0;}\n" +
						"	@Override\n" +
						"	public int doit() {\n" +
						"		// TODO Auto-generated method stub\n" +
						"		return 0;\n" +
						"	}	 \n" +
						"}\n";

		ICompilationUnit primaryCu = this.copy.getPrimary();
		primaryCu.becomeWorkingCopy(null);

		primaryCu.getBuffer().setContents(newContents);
		primaryCu.reconcile(ICompilationUnit.NO_AST, false, null, null);

		try {
			IType type = primaryCu.getType("I");
			ITypeHierarchy h = type.newTypeHierarchy(null);  // no working copies explicitly passed, should still honor primary working copies.

			assertHierarchyEquals(
					"Focus: I [in [Working copy] A.java [in x.y [in src [in P]]]]\n" +
							"Super types:\n" +
							"Sub types:\n" +
							"  <lambda #1> [in doit() [in <anonymous #1> [in doit() [in <lambda #1> [in zoo() [in X [in [Working copy] A.java [in x.y [in src [in P]]]]]]]]]]\n" +
							"  <lambda #1> [in doit() [in <lambda #1> [in doit() [in <lambda #1> [in doit() [in <lambda #1> [in doit() [in <lambda #1> [in zoo() [in X [in [Working copy] A.java [in x.y [in src [in P]]]]]]]]]]]]]]\n" +
							"  <lambda #1> [in doit() [in <lambda #1> [in doit() [in <lambda #1> [in doit() [in <lambda #1> [in zoo() [in X [in [Working copy] A.java [in x.y [in src [in P]]]]]]]]]]]]\n" +
							"  <lambda #1> [in doit() [in <lambda #1> [in doit() [in <lambda #1> [in zoo() [in X [in [Working copy] A.java [in x.y [in src [in P]]]]]]]]]]\n" +
							"  <lambda #1> [in doit() [in <lambda #1> [in zoo() [in X [in [Working copy] A.java [in x.y [in src [in P]]]]]]]]\n" +
							"  <lambda #1> [in zoo() [in X [in [Working copy] A.java [in x.y [in src [in P]]]]]]\n" +
							"  Y [in [Working copy] A.java [in x.y [in src [in P]]]]\n",
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
						"package x.y;\n" +
						"public class X extends Y {\n" +
						"public static void main(String [] args) {\n" +
						"	I<Y> c = () /* foo */ -> () /* bar */ -> {};\n" +
						"	I<Y> y = args.length < 1 ? (() /* true */-> 42) : (() /* false */ -> 23);\n" +
						"	Object o = (I) () /* cast */ -> 42;\n" +
						"	}\n" +
						"}\n" +
						"interface I<T> {\n" +
						"	public T foo();\n" +
						"}\n" +
						"class Y {\n" +
						"	public void bar() {}\n" +
						"}\n";

		ICompilationUnit primaryCu = this.copy.getPrimary();
		primaryCu.becomeWorkingCopy(null);

		primaryCu.getBuffer().setContents(newContents);
		primaryCu.reconcile(ICompilationUnit.NO_AST, false, null, null);

		try {
			IType type = primaryCu.getType("I");
			ITypeHierarchy h = type.newTypeHierarchy(null);  // no working copies explicitly passed, should still honor primary working copies.

			assertHierarchyEquals(
							"Focus: I [in [Working copy] A.java [in x.y [in src [in P]]]]\n" +
							"Super types:\n" +
							"Sub types:\n" +
							"  <lambda #1> [in main(String[]) [in X [in [Working copy] A.java [in x.y [in src [in P]]]]]]\n" +
							"  <lambda #1> [in main(String[]) [in X [in [Working copy] A.java [in x.y [in src [in P]]]]]]\n" +
							"  <lambda #1> [in main(String[]) [in X [in [Working copy] A.java [in x.y [in src [in P]]]]]]\n" +
							"  <lambda #1> [in main(String[]) [in X [in [Working copy] A.java [in x.y [in src [in P]]]]]]\n",
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
				"import q.*;\n" +
				"public final class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		foo(e -> { if (new Object() instanceof Y);});\n" +
				"	};\n" +
				"	static void foo(I i) {\n" +
				"		return;\n" +
				"	}\n" +
				"}\n" +
				"interface I {\n" +
				"	void foo(int x);\n" +
				"}\n");

		this.createFile("Bug442534/src/q/Y.java",
				"package q;\n" +
				"public class Y {\n" +
				"}\n");

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
				"import java.util.function.Consumer;\n" +
				"public class Claxx {\n" +
				"	void post(Runnable r) {\n" +
				"		r.run();\n" +
				"	}\n" +
				"	void absorb(Consumer<Claxx> c) throws Exception {\n" +
				"		c.accept(this);\n" +
				"	}\n" +
				"	\n" +
				"	static void execute() {\n" +
				"		System.out.println(\"exec!\");\n" +
				"	}\n" +
				"	static void executeGiven(Object o) {\n" +
				"		System.out.println(\"exec \" + o);\n" +
				"	}\n" +
				"	void executeObject() {\n" +
				"		System.out.println(\"exec \" + this);\n" +
				"	}	\n" +
				"}\n" +
				"class ClaxxTest {\n" +
				"	Claxx claxx = new Claxx();\n" +
				"	\n" +
				"	void doInBackground() throws Exception {\n" +
				"		claxx.post(Claxx::execute); \n" +
				"		absorb(Claxx::executeGiven);\n" +
				"		post(this::executeObject);\n" +
				"		\n" +
				"		absorb(Claxx::executeObject);\n" +
				"		post(() -> execute());\n" +
				"		post(() -> executeGiven(this)); // not convertible\n" +
				"		post(() -> executeObject());\n" +
				"	}\n" +
				"}\n");

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

