/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;

import junit.framework.Test;

public class LocalElementTests extends ModifyingResourceTests {
	
	public LocalElementTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		return new Suite(LocalElementTests.class);
	}

	public void setUpSuite() throws Exception {
		createJavaProject("P");
	}

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
				"      class <anonymous>\n" + 
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
				"      class <anonymous>\n" + 
				"      class <anonymous>\n" + 
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
				"      class <anonymous>\n" + 
				"        void bar()\n" +
				"          class <anonymous>\n" + 
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
				"    initializer\n" + 
				"      class <anonymous>\n" + 
				"    Object field\n" + 
				"      class <anonymous>\n" + 
				"    void foo()\n" + 
				"      class <anonymous>\n" + 
				"    void run(X)",
				cu);
		} finally {
			deleteFile("/P/X.java");
		}
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
				"    initializer\n" + 
				"      class Y\n" + 
				"    void foo()\n" + 
				"      class Z",
				cu);
		} finally {
			deleteFile("/P/X.java");
		}
	}

}
