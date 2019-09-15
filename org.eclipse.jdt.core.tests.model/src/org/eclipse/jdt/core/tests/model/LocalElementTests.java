/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;

import junit.framework.Test;

public class LocalElementTests extends ModifyingResourceTests {

	public LocalElementTests(String name) {
		super(name);
	}

	// Use this static initializer to specify subset for tests
	// All specified tests which do not belong to the class are skipped...
	static {
		// Names of tests to run: can be "testBugXXXX" or "BugXXXX")
//		TESTS_NAMES = new String[] { "testLocalType8" };
		// Numbers of tests to run: "test<number>" will be run for each number of this array
//		TESTS_NUMBERS = new int[] { 13 };
		// Range numbers of tests to run: all tests between "test<first>" and "test<last>" will be run for { first, last }
//		TESTS_RANGE = new int[] { 16, -1 };
	}
	public static Test suite() {
		return buildModelTestSuite(LocalElementTests.class);
	}

	@Override
	public void setUpSuite() throws Exception {
		createJavaProject("P");
	}

	@Override
	public void tearDownSuite() throws Exception {
		deleteProject("P");
	}

	/*
	 * Anonymous type test.
	 */
	public void testAnonymousType1() throws CoreException {
		try {
			createFile(
				"/P/X.java",
				"public class X {\n" +
				"  void foo() {\n" +
				"    run(new X() {\n" +
				"    });\n" +
				"  }\n" +
				"  void run(X x) {\n" +
				"  }\n" +
				"}"
			);
			ICompilationUnit cu = getCompilationUnit("/P/X.java");
			assertElementDescendants(
				"Unexpected compilation unit contents",
				"X.java\n" +
				"  class X\n" +
				"    void foo()\n" +
				"      class <anonymous #1>\n" +
				"    void run(X)",
				cu);
		} finally {
			deleteFile("/P/X.java");
		}
	}

	/*
	 * Anonymous type test.
	 */
	public void testAnonymousType2() throws CoreException {
		try {
			createFile(
				"/P/X.java",
				"public class X {\n" +
				"  public class Y {\n" +
				"  }\n" +
				"  void foo() {\n" +
				"    run(new X() {\n" +
				"    });\n" +
				"    run(new Y() {\n" +
				"    });\n" +
				"  }\n" +
				"  void run(X x) {\n" +
				"  }\n" +
				"}"
			);
			ICompilationUnit cu = getCompilationUnit("/P/X.java");
			assertElementDescendants(
				"Unexpected compilation unit contents",
				"X.java\n" +
				"  class X\n" +
				"    class Y\n" +
				"    void foo()\n" +
				"      class <anonymous #1>\n" +
				"      class <anonymous #2>\n" +
				"    void run(X)",
				cu);
		} finally {
			deleteFile("/P/X.java");
		}
	}

	/*
	 * Anonymous type test.
	 */
	public void testAnonymousType3() throws CoreException {
		try {
			createFile(
				"/P/X.java",
				"public class X {\n" +
				"  void foo() {\n" +
				"    run(new X() {\n" +
				"      void bar() {\n" +
				"        run(new X() {\n" +
				"        });\n" +
				"      }\n" +
				"    });\n" +
				"  }\n" +
				"  void run(X x) {\n" +
				"  }\n" +
				"}"
			);
			ICompilationUnit cu = getCompilationUnit("/P/X.java");
			assertElementDescendants(
				"Unexpected compilation unit contents",
				"X.java\n" +
				"  class X\n" +
				"    void foo()\n" +
				"      class <anonymous #1>\n" +
				"        void bar()\n" +
				"          class <anonymous #1>\n" +
				"    void run(X)",
				cu);
		} finally {
			deleteFile("/P/X.java");
		}
	}

	/*
	 * Anonymous type test.
	 */
	public void testAnonymousType4() throws CoreException {
		try {
			createFile(
				"/P/X.java",
				"public class X {\n" +
				"  {\n" +
				"      field = new Vector() {\n" +
				"      };\n" +
				"  }\n" +
				"  Object field = new Object() {\n" +
				"  };\n" +
				"  void foo() {\n" +
				"    run(new X() {\n" +
				"    });\n" +
				"  }\n" +
				"  void run(X x) {\n" +
				"  }\n" +
				"}"
			);
			ICompilationUnit cu = getCompilationUnit("/P/X.java");
			assertElementDescendants(
				"Unexpected compilation unit contents",
				"X.java\n" +
				"  class X\n" +
				"    <initializer #1>\n" +
				"      class <anonymous #1>\n" +
				"    Object field\n" +
				"      class <anonymous #1>\n" +
				"    void foo()\n" +
				"      class <anonymous #1>\n" +
				"    void run(X)",
				cu);
		} finally {
			deleteFile("/P/X.java");
		}
	}

	/*
	 * Anonymous type test.
	 * (regression test for bug 69028 Anonymous type in argument of super() is not in type hierarchy)
	 */
	public void testAnonymousType5() throws CoreException {
		try {
			createFile(
				"/P/X.java",
				"public class X {\n" +
				"  X(Object o) {\n" +
				"  }\n" +
				"}\n" +
				"class Y extends X {\n" +
				"  Y() {\n" +
				"    super(new Object() {});\n" +
				"  }\n" +
				"}"
			);
			ICompilationUnit cu = getCompilationUnit("/P/X.java");
			assertElementDescendants(
				"Unexpected compilation unit contents",
				"X.java\n" +
				"  class X\n" +
				"    X(Object)\n" +
				"  class Y\n" +
				"    Y()\n" +
				"      class <anonymous #1>",
				cu);
		} finally {
			deleteFile("/P/X.java");
		}
	}

	/*
	 * Ensures that an anonymous in an enum constant is said to be local.
	 * (regression test for bug 85298 [1.5][enum] IType of anonymous enum declaration says isLocal() == false)
	 */
	public void testAnonymousType6() throws CoreException {
		try {
			createJavaProject("P15", new String[] {""}, new String[] {"JCL15_LIB"}, "", "1.5");
			createFile(
				"/P15/En.java",
				"public enum En {\n" +
				"  CONST() {};\n" +
				"}"
			);
			IType type = getCompilationUnit("/P15/En.java").getType("En").getField("CONST").getType("", 1);
			assertTrue("Should be a local type", type.isLocal());
		} finally {
			deleteProject("P15");
		}
	}

	/*
	 * Anonymous type test.
	 * (regression test for bug 147485 Anonymous type missing from java model)
	 */
	public void testAnonymousType7() throws CoreException {
		try {
			createFile(
				"/P/X.java",
				"public class X {\n" +
				"	class Y {\n" +
				"	}\n" +
				"	{\n" +
				"		new Y() {\n" +
				"			class Z {\n" +
				"			}\n" +
				"			{\n" +
				"				new Y() {\n" +
				"				};\n" +
				"			}\n" +
				"		};\n" +
				"	}\n" +
				"}"
			);
			ICompilationUnit cu = getCompilationUnit("/P/X.java");
			assertElementDescendants(
				"Unexpected compilation unit contents",
				"X.java\n" +
				"  class X\n" +
				"    class Y\n" +
				"    <initializer #1>\n" +
				"      class <anonymous #1>\n" +
				"        class Z\n" +
				"        <initializer #1>\n" +
				"          class <anonymous #1>",
				cu);
		} finally {
			deleteFile("/P/X.java");
		}
	}

	/*
	 * IType.getSuperclassName() test
	 */
	public void testGetSuperclassName() throws CoreException {
		try {
			createFile(
				"/P/X.java",
				"public class X {\n" +
				"  void foo() {\n" +
				"    run(new X() {\n" +
				"    });\n" +
				"  }\n" +
				"  void run(X x) {\n" +
				"  }\n" +
				"}"
			);
			ICompilationUnit cu = getCompilationUnit("/P/X.java");
			IType type = cu.getType("X").getMethod("foo", new String[0]).getType("", 1);
			assertEquals(
				"Unexpected superclass name",
				"X",
				type.getSuperclassName());
		} finally {
			deleteFile("/P/X.java");
		}
	}

	/*
	 * IMember.getType(...) test
	 */
	public void testGetType() {
		ICompilationUnit cu = getCompilationUnit("P/X.java");
		IType topLevelType = cu.getType("X");
		IJavaElement[] types = new IJavaElement[5];
		types[0] = topLevelType.getInitializer(1).getType("", 1);
		types[1] = topLevelType.getInitializer(1).getType("Y", 1);
		types[2] = topLevelType.getField("f").getType("", 1);
		types[3] = topLevelType.getMethod("foo", new String[] {"I", "QString;"}).getType("", 1);
		types[4] = topLevelType.getMethod("foo", new String[] {"I", "QString;"}).getType("Z", 1);
		assertElementsEqual(
			"Unexpected types",
			"<anonymous #1> [in <initializer #1> [in X [in X.java [in <default> [in <project root> [in P]]]]]]\n" +
			"Y [in <initializer #1> [in X [in X.java [in <default> [in <project root> [in P]]]]]]\n" +
			"<anonymous #1> [in f [in X [in X.java [in <default> [in <project root> [in P]]]]]]\n" +
			"<anonymous #1> [in foo(int, String) [in X [in X.java [in <default> [in <project root> [in P]]]]]]\n" +
			"Z [in foo(int, String) [in X [in X.java [in <default> [in <project root> [in P]]]]]]",
			types);
	}

	/*
	 * Local type test.
	 */
	public void testLocalType1() throws CoreException {
		try {
			createFile(
				"/P/X.java",
				"public class X {\n" +
				"  void foo() {\n" +
				"    class Y {\n" +
				"    }\n" +
				"  }\n" +
				"}"
			);
			ICompilationUnit cu = getCompilationUnit("/P/X.java");
			assertElementDescendants(
				"Unexpected compilation unit contents",
				"X.java\n" +
				"  class X\n" +
				"    void foo()\n" +
				"      class Y",
				cu);
		} finally {
			deleteFile("/P/X.java");
		}
	}

	/*
	 * Local type test.
	 */
	public void testLocalType2() throws CoreException {
		try {
			createFile(
				"/P/X.java",
				"public class X {\n" +
				"  void foo() {\n" +
				"    class Y {\n" +
				"    }\n" +
				"    class Z {\n" +
				"    }\n" +
				"  }\n" +
				"}"
			);
			ICompilationUnit cu = getCompilationUnit("/P/X.java");
			assertElementDescendants(
				"Unexpected compilation unit contents",
				"X.java\n" +
				"  class X\n" +
				"    void foo()\n" +
				"      class Y\n" +
				"      class Z",
				cu);
		} finally {
			deleteFile("/P/X.java");
		}
	}

	/*
	 * Local type test.
	 */
	public void testLocalType3() throws CoreException {
		try {
			createFile(
				"/P/X.java",
				"public class X {\n" +
				"  void foo() {\n" +
				"    class Y {\n" +
				"      void bar() {\n" +
				"        class Z {\n" +
				"        }\n" +
				"      }\n" +
				"    }\n" +
				"  }\n" +
				"}"
			);
			ICompilationUnit cu = getCompilationUnit("/P/X.java");
			assertElementDescendants(
				"Unexpected compilation unit contents",
				"X.java\n" +
				"  class X\n" +
				"    void foo()\n" +
				"      class Y\n" +
				"        void bar()\n" +
				"          class Z",
				cu);
		} finally {
			deleteFile("/P/X.java");
		}
	}

	/*
	 * Local type test.
	 */
	public void testLocalType4() throws CoreException {
		try {
			createFile(
				"/P/X.java",
				"public class X {\n" +
				"  {\n" +
				"      class Y {\n" +
				"      }\n" +
				"  }\n" +
				"  void foo() {\n" +
				"    class Z {\n" +
				"    }\n" +
				"  }\n" +
				"}"
			);
			ICompilationUnit cu = getCompilationUnit("/P/X.java");
			assertElementDescendants(
				"Unexpected compilation unit contents",
				"X.java\n" +
				"  class X\n" +
				"    <initializer #1>\n" +
				"      class Y\n" +
				"    void foo()\n" +
				"      class Z",
				cu);
		} finally {
			deleteFile("/P/X.java");
		}
	}

	/*
	 * Local type test.
	 */
	public void testLocalType5() throws CoreException {
		try {
			createFile(
				"/P/X.java",
				"public class X {\n" +
				"  void foo() {\n" +
				"    class Z {\n" +
				"    }\n" +
				"    Z\n" +
				"  }\n" +
				"}"
			);
			ICompilationUnit cu = getCompilationUnit("/P/X.java");
			assertElementDescendants(
				"Unexpected compilation unit contents",
				"X.java\n" +
				"  class X\n" +
				"    void foo()\n" +
				"      class Z",
				cu);
		} finally {
			deleteFile("/P/X.java");
		}
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=167357
	public void testLocalType6() throws CoreException {
		try {
			createFile(
					"/P/X.java",
					"public class X {\n" +
					"  void foo() {\n" +
					"    class Y {\n" +
					"      {\n" +
					"        class Z {\n" +
					"        }\n" +
					"      }\n" +
					"    }\n" +
					"  }\n" +
					"}"
			);
			ICompilationUnit cu = getCompilationUnit("/P/X.java");
			assertElementDescendants(
					"Unexpected compilation unit contents",
					"X.java\n" +
					"  class X\n" +
					"    void foo()\n" +
					"      class Y\n" +
					"        <initializer #1>\n" +
					"          class Z",
					cu);
		} finally {
			deleteFile("/P/X.java");
		}
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=167357
	public void testLocalType7() throws CoreException {
		try {
			createFile(
					"/P/X.java",
					"public class X {\n" +
					"  void foo() {\n" +
					"    class Y {\n" +
					"      {\n" +
					"        class Z {\n" +
					"        }\n" +
					"      }\n" +
					"      String s = null;\n" +
					"    }\n" +
					"  }\n" +
					"}"
			);
			ICompilationUnit cu = getCompilationUnit("/P/X.java");
			assertElementDescendants(
					"Unexpected compilation unit contents",
					"X.java\n" +
					"  class X\n" +
					"    void foo()\n" +
					"      class Y\n" +
					"        <initializer #1>\n" +
					"          class Z\n" +
					"        String s",
					cu);
		} finally {
			deleteFile("/P/X.java");
		}
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=167357
	public void testLocalType8() throws CoreException {
		try {
			createFile(
					"/P/X.java",
					"public class X {\n" +
					"  void foo() {\n" +
					"    class Y {\n" +
					"      String s = null;\n" +
					"      {\n" +
					"        class Z {\n" +
					"        }\n" +
					"      }\n" +
					"    }\n" +
					"  }\n" +
					"}"
			);
			ICompilationUnit cu = getCompilationUnit("/P/X.java");
			assertElementDescendants(
					"Unexpected compilation unit contents",
					"X.java\n" +
					"  class X\n" +
					"    void foo()\n" +
					"      class Y\n" +
					"        String s\n"+
					"        <initializer #1>\n" +
					"          class Z",
					cu);
		} finally {
			deleteFile("/P/X.java");
		}
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=167357
	public void testLocalType9() throws CoreException {
		try {
			createFile(
					"/P/X.java",
					"public class X {\n" +
					"  {\n" +
					"    class Y {\n" +
					"      String s = null;\n" +
					"      {\n" +
					"        class Z {\n" +
					"        }\n" +
					"      }\n" +
					"    }\n" +
					"  }\n" +
					"}"
			);
			ICompilationUnit cu = getCompilationUnit("/P/X.java");
			assertElementDescendants(
					"Unexpected compilation unit contents",
					"X.java\n" +
					"  class X\n" +
					"    <initializer #1>\n" +
					"      class Y\n" +
					"        String s\n"+
					"        <initializer #1>\n" +
					"          class Z",
					cu);
		} finally {
			deleteFile("/P/X.java");
		}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=167357
	public void testLocalType10() throws CoreException {
		try {
			createFile(
					"/P/X.java",
					"public class X {\n" +
					"  void foo() {\n" +
					"    class Y {\n" +
					"      String s = null;\n" +
					"      {\n" +
					"        {" +
					"          class Z {\n" +
					"          }" +
					"        }\n" +
					"      }\n" +
					"    }\n" +
					"  }\n" +
					"}"
			);
			ICompilationUnit cu = getCompilationUnit("/P/X.java");
			assertElementDescendants(
					"Unexpected compilation unit contents",
					"X.java\n" +
					"  class X\n" +
					"    void foo()\n" +
					"      class Y\n" +
					"        String s\n"+
					"        <initializer #1>\n" +
					"          class Z",
					cu);
		} finally {
			deleteFile("/P/X.java");
		}
	}
}
