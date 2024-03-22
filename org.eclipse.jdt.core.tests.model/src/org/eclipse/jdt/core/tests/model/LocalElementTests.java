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
				"""
					public class X {
					  void foo() {
					    run(new X() {
					    });
					  }
					  void run(X x) {
					  }
					}"""
			);
			ICompilationUnit cu = getCompilationUnit("/P/X.java");
			assertElementDescendants(
				"Unexpected compilation unit contents",
				"""
					X.java
					  class X
					    void foo()
					      class <anonymous #1>
					    void run(X)""",
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
				"""
					public class X {
					  public class Y {
					  }
					  void foo() {
					    run(new X() {
					    });
					    run(new Y() {
					    });
					  }
					  void run(X x) {
					  }
					}"""
			);
			ICompilationUnit cu = getCompilationUnit("/P/X.java");
			assertElementDescendants(
				"Unexpected compilation unit contents",
				"""
					X.java
					  class X
					    class Y
					    void foo()
					      class <anonymous #1>
					      class <anonymous #2>
					    void run(X)""",
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
				"""
					public class X {
					  void foo() {
					    run(new X() {
					      void bar() {
					        run(new X() {
					        });
					      }
					    });
					  }
					  void run(X x) {
					  }
					}"""
			);
			ICompilationUnit cu = getCompilationUnit("/P/X.java");
			assertElementDescendants(
				"Unexpected compilation unit contents",
				"""
					X.java
					  class X
					    void foo()
					      class <anonymous #1>
					        void bar()
					          class <anonymous #1>
					    void run(X)""",
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
				"""
					public class X {
					  {
					      field = new Vector() {
					      };
					  }
					  Object field = new Object() {
					  };
					  void foo() {
					    run(new X() {
					    });
					  }
					  void run(X x) {
					  }
					}"""
			);
			ICompilationUnit cu = getCompilationUnit("/P/X.java");
			assertElementDescendants(
				"Unexpected compilation unit contents",
				"""
					X.java
					  class X
					    <initializer #1>
					      class <anonymous #1>
					    Object field
					      class <anonymous #1>
					    void foo()
					      class <anonymous #1>
					    void run(X)""",
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
				"""
					public class X {
					  X(Object o) {
					  }
					}
					class Y extends X {
					  Y() {
					    super(new Object() {});
					  }
					}"""
			);
			ICompilationUnit cu = getCompilationUnit("/P/X.java");
			assertElementDescendants(
				"Unexpected compilation unit contents",
				"""
					X.java
					  class X
					    X(Object)
					  class Y
					    Y()
					      class <anonymous #1>""",
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
				"""
					public enum En {
					  CONST() {};
					}"""
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
				"""
					public class X {
						class Y {
						}
						{
							new Y() {
								class Z {
								}
								{
									new Y() {
									};
								}
							};
						}
					}"""
			);
			ICompilationUnit cu = getCompilationUnit("/P/X.java");
			assertElementDescendants(
				"Unexpected compilation unit contents",
				"""
					X.java
					  class X
					    class Y
					    <initializer #1>
					      class <anonymous #1>
					        class Z
					        <initializer #1>
					          class <anonymous #1>""",
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
				"""
					public class X {
					  void foo() {
					    run(new X() {
					    });
					  }
					  void run(X x) {
					  }
					}"""
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
			"""
				<anonymous #1> [in <initializer #1> [in X [in X.java [in <default> [in <project root> [in P]]]]]]
				Y [in <initializer #1> [in X [in X.java [in <default> [in <project root> [in P]]]]]]
				<anonymous #1> [in f [in X [in X.java [in <default> [in <project root> [in P]]]]]]
				<anonymous #1> [in foo(int, String) [in X [in X.java [in <default> [in <project root> [in P]]]]]]
				Z [in foo(int, String) [in X [in X.java [in <default> [in <project root> [in P]]]]]]""",
			types);
	}

	/*
	 * Local type test.
	 */
	public void testLocalType1() throws CoreException {
		try {
			createFile(
				"/P/X.java",
				"""
					public class X {
					  void foo() {
					    class Y {
					    }
					  }
					}"""
			);
			ICompilationUnit cu = getCompilationUnit("/P/X.java");
			assertElementDescendants(
				"Unexpected compilation unit contents",
				"""
					X.java
					  class X
					    void foo()
					      class Y""",
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
				"""
					public class X {
					  void foo() {
					    class Y {
					    }
					    class Z {
					    }
					  }
					}"""
			);
			ICompilationUnit cu = getCompilationUnit("/P/X.java");
			assertElementDescendants(
				"Unexpected compilation unit contents",
				"""
					X.java
					  class X
					    void foo()
					      class Y
					      class Z""",
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
				"""
					public class X {
					  void foo() {
					    class Y {
					      void bar() {
					        class Z {
					        }
					      }
					    }
					  }
					}"""
			);
			ICompilationUnit cu = getCompilationUnit("/P/X.java");
			assertElementDescendants(
				"Unexpected compilation unit contents",
				"""
					X.java
					  class X
					    void foo()
					      class Y
					        void bar()
					          class Z""",
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
				"""
					public class X {
					  {
					      class Y {
					      }
					  }
					  void foo() {
					    class Z {
					    }
					  }
					}"""
			);
			ICompilationUnit cu = getCompilationUnit("/P/X.java");
			assertElementDescendants(
				"Unexpected compilation unit contents",
				"""
					X.java
					  class X
					    <initializer #1>
					      class Y
					    void foo()
					      class Z""",
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
				"""
					public class X {
					  void foo() {
					    class Z {
					    }
					    Z
					  }
					}"""
			);
			ICompilationUnit cu = getCompilationUnit("/P/X.java");
			assertElementDescendants(
				"Unexpected compilation unit contents",
				"""
					X.java
					  class X
					    void foo()
					      class Z""",
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
					"""
						public class X {
						  void foo() {
						    class Y {
						      {
						        class Z {
						        }
						      }
						    }
						  }
						}"""
			);
			ICompilationUnit cu = getCompilationUnit("/P/X.java");
			assertElementDescendants(
					"Unexpected compilation unit contents",
					"""
						X.java
						  class X
						    void foo()
						      class Y
						        <initializer #1>
						          class Z""",
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
					"""
						public class X {
						  void foo() {
						    class Y {
						      {
						        class Z {
						        }
						      }
						      String s = null;
						    }
						  }
						}"""
			);
			ICompilationUnit cu = getCompilationUnit("/P/X.java");
			assertElementDescendants(
					"Unexpected compilation unit contents",
					"""
						X.java
						  class X
						    void foo()
						      class Y
						        <initializer #1>
						          class Z
						        String s""",
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
					"""
						public class X {
						  void foo() {
						    class Y {
						      String s = null;
						      {
						        class Z {
						        }
						      }
						    }
						  }
						}"""
			);
			ICompilationUnit cu = getCompilationUnit("/P/X.java");
			assertElementDescendants(
					"Unexpected compilation unit contents",
					"""
						X.java
						  class X
						    void foo()
						      class Y
						        String s
						        <initializer #1>
						          class Z""",
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
					"""
						public class X {
						  {
						    class Y {
						      String s = null;
						      {
						        class Z {
						        }
						      }
						    }
						  }
						}"""
			);
			ICompilationUnit cu = getCompilationUnit("/P/X.java");
			assertElementDescendants(
					"Unexpected compilation unit contents",
					"""
						X.java
						  class X
						    <initializer #1>
						      class Y
						        String s
						        <initializer #1>
						          class Z""",
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
					"""
						public class X {
						  void foo() {
						    class Y {
						      String s = null;
						      {
						        {\
						          class Z {
						          }\
						        }
						      }
						    }
						  }
						}"""
			);
			ICompilationUnit cu = getCompilationUnit("/P/X.java");
			assertElementDescendants(
					"Unexpected compilation unit contents",
					"""
						X.java
						  class X
						    void foo()
						      class Y
						        String s
						        <initializer #1>
						          class Z""",
					cu);
		} finally {
			deleteFile("/P/X.java");
		}
	}
}
