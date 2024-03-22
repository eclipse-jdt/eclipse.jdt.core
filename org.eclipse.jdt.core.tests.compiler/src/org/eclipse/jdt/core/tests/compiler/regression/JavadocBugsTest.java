/*******************************************************************************
 * Copyright (c) 2000, 2023 IBM Corporation and others.
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
 *     Stephan Herrmann <stephan@cs.tu-berlin.de> - Contribution for bug 185682 - Increment/decrement operators mark local variables as read
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.JavadocTagConstants;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class JavadocBugsTest extends JavadocTest {

	String docCommentSupport = CompilerOptions.ENABLED;
	String reportInvalidJavadoc = CompilerOptions.ERROR;
	String reportMissingJavadocDescription = CompilerOptions.RETURN_TAG;
	String reportInvalidJavadocVisibility = CompilerOptions.PRIVATE;
	String reportMissingJavadocTags = CompilerOptions.ERROR;
	String reportMissingJavadocComments = null;
	String reportMissingJavadocCommentsVisibility = null;
	String reportDeprecation = CompilerOptions.ERROR;
	String reportJavadocDeprecation = null;
	String processAnnotations = null;

public JavadocBugsTest(String name) {
	super(name);
}

public static Class javadocTestClass() {
	return JavadocBugsTest.class;
}

// Use this static initializer to specify subset for tests
// All specified tests which does not belong to the class are skipped...
static {
//		TESTS_PREFIX = "testBug96237";
//		TESTS_NAMES = new String[] { "testBug382606" };
//		TESTS_NUMBERS = new int[] { 129241 };
//		TESTS_RANGE = new int[] { 21, 50 };
}

public static Test suite() {
	return buildAllCompliancesTestSuite(javadocTestClass());
}

@Override
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_DocCommentSupport, this.docCommentSupport);
	options.put(CompilerOptions.OPTION_ReportInvalidJavadoc, this.reportInvalidJavadoc);
	if (!CompilerOptions.IGNORE.equals(this.reportInvalidJavadoc)) {
		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsVisibility, this.reportInvalidJavadocVisibility);
	}
	if (this.reportJavadocDeprecation != null) {
		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsDeprecatedRef, this.reportJavadocDeprecation);
	}
	if (this.reportMissingJavadocComments != null) {
		options.put(CompilerOptions.OPTION_ReportMissingJavadocComments, this.reportMissingJavadocComments);
		options.put(CompilerOptions.OPTION_ReportMissingJavadocCommentsOverriding, CompilerOptions.ENABLED);
		if (this.reportMissingJavadocCommentsVisibility != null) {
			options.put(CompilerOptions.OPTION_ReportMissingJavadocCommentsVisibility, this.reportMissingJavadocCommentsVisibility);
		}
	} else {
		options.put(CompilerOptions.OPTION_ReportMissingJavadocComments, this.reportInvalidJavadoc);
	}
	if (this.reportMissingJavadocTags != null) {
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, this.reportMissingJavadocTags);
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTagsOverriding, CompilerOptions.ENABLED);
	} else {
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, this.reportInvalidJavadoc);
	}
	if (this.reportMissingJavadocDescription != null) {
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTagDescription, this.reportMissingJavadocDescription);
	}
	if (this.processAnnotations != null) {
		options.put(CompilerOptions.OPTION_Process_Annotations, this.processAnnotations);
	}
	options.put(CompilerOptions.OPTION_ReportFieldHiding, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportDeprecation, this.reportDeprecation);
	options.put(CompilerOptions.OPTION_ReportUnusedImport, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	return options;
}
/* (non-Javadoc)
 * @see junit.framework.TestCase#setUp()
 */
@Override
protected void setUp() throws Exception {
	super.setUp();
	this.docCommentSupport = CompilerOptions.ENABLED;
	this.reportInvalidJavadoc = CompilerOptions.ERROR;
	this.reportInvalidJavadocVisibility = CompilerOptions.PRIVATE;
	this.reportMissingJavadocTags = CompilerOptions.IGNORE;
	this.reportMissingJavadocComments = CompilerOptions.IGNORE;
	this.reportMissingJavadocCommentsVisibility = CompilerOptions.PUBLIC;
	this.reportDeprecation = CompilerOptions.ERROR;
}

/**
 * Bug 45596.
 * When this bug happened, compiler wrongly complained on missing parameter javadoc
 * entries for method declaration in anonymous class.
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=45596">45596</a>
 */
public void testBug45596() {
	runConformTest(
		new String[] {
			"test/X.java",
			"""
				package test;
				class X {
					void foo(int x, String str) {}
				}
				""",
			"test/Y.java",
			"""
				package test;
				class Y {
				  /** */
				  protected X field = new X() {
				    void foo(int x, String str) {}
				  };
				}
				"""});
}

/**
 * Additional test for bug 45596.
 * Verify correct complain about missing parameter javadoc entries in anonymous class.
 * Since bug 47132, @param, @return and @throws tags are not resolved in javadoc of anonymous
 * class...
 */
public void testBug45596a() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void foo(int x, String str) {}
				}
				""",
			"Y1.java",
			"""
				public class Y1 {
					/** */
					protected X field = new X() {
						/** Invalid javadoc comment in anonymous class */
						void foo(String str) {}
					};
				}
				""",
			"Y2.java",
			"""
				public class Y2 {
					/** */
					void foo() {
						X x = new X() {
							/** Invalid javadoc comment in anonymous class */
							void foo(String str) {}
						};
						x.foo(0, "");
					}
				}
				""",
			"Y3.java",
			"""
				public class Y3 {
					static X x;
					static {
						x = new X() {
							/** Invalid javadoc comment in anonymous class */
							void foo(String str) {}
						};
					}
				}
				""" }
		);
}

/**
 * Additional test for bug 45596.
 * Verify no complain about missing parameter javadoc entries.
 */
public void testBug45596b() {
	runConformTest(
		new String[] {
	"X.java",
	"""
		public class X {
			void foo(int x, String str) {}
		}
		""",
	"Y1.java",
	"""
		public class Y1 {
			/** */
			protected X field = new X() {
				/**
				 * Valid javadoc comment in anonymous class.
				 * @param str String
				 * @return int
				 */
				int bar(String str) {
					return 10;
				}
			};
		}
		""",
	"Y2.java",
	"""
		public class Y2 {
			/** */
			void foo() {
				X x = new X() {
					/**
					 * Valid javadoc comment in anonymous class.
					 * @param str String
					 * @return int
					 */
					int bar(String str) {
						return 10;
					}
				};
				x.foo(0, "");
			}
		}
		""",
	"Y3.java",
	"""
		public class Y3 {
			static X x;
			static {
				x = new X() {
					/**
					 * Valid javadoc comment in anonymous class.
					 * @param str String
					 * @return int
					 */
					int bar(String str) {
						return 10;
					}
				};
			}
		}
		"""}
		);
}

/**
 * Bug 45592.
 * When this bug happened, a NullPointerException occured during the compilation.
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=45592">45592</a>
 */
public void testBug45592() {
	runConformTest(
		new String[] {
			"a/Y.java",
			"""
				package a;
				
				/** */
				public class Y {
					protected boolean bar(Object obj) {
						return obj == null;
					}
				}
				""",
			"test/X.java",
			"""
				package test;
				public class X {
					public static Boolean valueOf(boolean bool) {
						if (bool) {
							return Boolean.TRUE;
						} else {
							return Boolean.FALSE;
						}
					}
				}
				""",
			"test/YY.java",
			"""
				package test;
				
				import a.Y;
				
				/** */
				public class YY extends Y {
					/**
					 * Returns a Boolean.
					 * @param key
					 * @return A Boolean telling whether the key is null or not.
					 * @see #bar(Object)
					 */
					protected Boolean foo(Object key) {
						return X.valueOf(bar(key));
					}
				}
				"""
		}
	);
}

/**
 * Bug 45737.
 * When this bug happened, compiler complains on return type and argument of method bar.
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=45737">45737</a>
 */
public void testBug45737() {
	runConformTest(
		new String[] {
			"Y.java",
			"""
				class Y {
					void foo() {
						X x = new X() {
							/**
							 * Valid javadoc comment in anonymous class.
							 * @param str String
							 * @return int
							 */
							int bar(String str) {
								return 10;
							}
						};
						x.foo();
					}
				}
				""",
			"X.java",
			"""
				class X {
					void foo() {}
				}
				"""
		}
	);
}

/**
 * Bug 45669.
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=45669">45669</a>
 */
public void testBug45669() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					/**
					 * Valid javadoc comment with tags mixed order
					 * @param str first param
					 * 		@see String
					 * @param dbl second param
					 * 		@see Double
					 * 		also
					 * 		@see "String ref"
					 * @return int
					 * @throws InterruptedException
					 *\s
					 */
					int foo(String str, Double dbl) throws InterruptedException {
						return 0;
					}
				}
				"""
		}
	);
}
/*
 * Additional test for bug 45669.
 * Verify that compiler complains when @throws tag is between @param tags.
 */
public void testBug45669a() {
	this.reportMissingJavadocTags = CompilerOptions.ERROR;
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					/**
					 * Javadoc comment with tags invalid mixed order
					 * @param str first param
					 * 		@see String
					 * @throws InterruptedException
					 * @param dbl second param
					 * 		@see Double
					 * 		also
					 * 		@see "String ref"
					 * @return int
					 *\s
					 */
					public int foo(String str, Double dbl) throws InterruptedException {
						return 0;
					}
				}
				"""
		},
	"""
		----------
		1. ERROR in X.java (at line 7)
			* @param dbl second param
			   ^^^^^
		Javadoc: Unexpected tag
		----------
		2. ERROR in X.java (at line 14)
			public int foo(String str, Double dbl) throws InterruptedException {
			                                  ^^^
		Javadoc: Missing tag for parameter dbl
		----------
		""",
	JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 45958.
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=45958">45958</a>
 */
public void testBug45958() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					int x;
					public X(int i) {
						x = i;
					}
					/**
					 * @see #X(int)
					 */
					void foo() {
					}
				}
				"""
		}
	);
}
public void testBug45958a() {
	runNegativeTest(
		new String[] {
		   "X.java",
	   		"""
				public class X {
					int x;
					public X(int i) {
						x = i;
					}
					/**
					 * @see #X(String)
					 */
					public void foo() {
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				* @see #X(String)
				        ^^^^^^^^^
			Javadoc: The constructor X(String) is undefined
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug45958b() {
	runNegativeTest(
		new String[] {
		   "X.java",
	   		"""
				public class X {
					int x;
					public X(int i) {
						x = i;
					}
					/**
					 * @see #X(int)
					 */
					public void foo() {
					}
				}
				""",
	   		"XX.java",
	   		"""
				public class XX extends X {
					/**
					 * @param i
					 * @see #X(int)
					 */
					public XX(int i) {
						super(i);
						x++;
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in XX.java (at line 4)
				* @see #X(int)
				        ^
			Javadoc: The method X(int) is undefined for the type XX
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug45958c() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					int x;
					public X(int i) {
						x = i;
					}
					/**
					 * @see #X(String)
					 */
					void foo() {
					}
					void X(String str) {}
				}
				"""
		}
	);
}

/**
 * Bug 46901.
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=46901">46901</a>
 */
public void testBug46901() {
	runConformTest(
		new String[] {
			"A.java",
			"""
				public abstract class A {
					public A() { super(); }
				}
				""",
			"X.java",
			"""
				/**
				 * @see A#A()
				 */
				public class X extends A {
					public X() { super(); }
				}
				"""
		}
	);
}

/**
 * Bug 47215.
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=47215">47215</a>
 */
public void testBug47215() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
					/**
					 * @see X
					 * @see X#X(int)
					 * @see X(double)
					 * @see X   (double)
					 * @see X[double]
					 * @see X!=}}
					 * @see foo()
					 * @see foo  ()
					 */
					public class X {
						public X(int i){}
						public void foo() {}
					}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				* @see X(double)
				       ^^^^^^^^^
			Javadoc: Missing #: "X(double)"
			----------
			2. ERROR in X.java (at line 6)
				* @see X[double]
				       ^^^^^^^^^
			Javadoc: Malformed reference (missing end space separator)
			----------
			3. ERROR in X.java (at line 7)
				* @see X!=}}
				       ^^^^^
			Javadoc: Malformed reference (missing end space separator)
			----------
			4. ERROR in X.java (at line 8)
				* @see foo()
				       ^^^^^
			Javadoc: Missing #: "foo()"
			----------
			5. ERROR in X.java (at line 9)
				* @see foo  ()
				       ^^^
			Javadoc: foo cannot be resolved to a type
			----------
			""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 47341.
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=47341">47341</a>
 */
public void testBug47341() {
	runConformTest(
		new String[] {
			"p1/X.java",
			"""
				package p1;
				public class X {
					void foo_package() {}
					protected void foo_protected() {}
				}
				""",
			"p1/Y.java",
			"""
				package p1;
				public class Y extends X {
					/**
					 * @see #foo_package()
					 */
					protected void bar() {
						foo_package();
					}
				}
				""",
			"p2/Y.java",
			"""
				package p2;
				import p1.X;
				
				public class Y extends X {
					/**
					 * @see X#foo_protected()
					 */
					protected void bar() {
						foo_protected();
					}
				}
				"""
		}
	);
}

/**
 * Bug 47132.
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=47132">47132</a>
 */
public void testBug47132() {
	this.reportMissingJavadocComments = CompilerOptions.ERROR;
	runConformTest(
		new String[] {
			"X.java",
			"""
				/** */
				public class X {
				  /** */
				  public void foo(){
				    new Object(){
						public int x;
				       public void bar(){}
				    };
				  }
				}
				"""
		}
	);
}

/**
 * Bug 47339.
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=47339">47339</a>
 */
public void testBug47339() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				/** */
				public class X implements Comparable {
					/**
					 * @see java.lang.Comparable#compareTo(java.lang.Object)
					 */
					public int compareTo(Object o) {
						return 0;
					}
					/** @see Object#toString() */
					public String toString(){
						return "";
					}
				}
				"""
		}
	);
}
public void testBug47339a() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				/** */
				public class X extends RuntimeException {
				\t
					/**
					 * @see RuntimeException#RuntimeException(java.lang.String)
					 */
					public X(String message) {
						super(message);
					}
				}
				"""
		}
	);
}
public void testBug47339b() {
	this.reportMissingJavadocTags = CompilerOptions.ERROR;
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				/** */
				public class X implements Comparable {
					/** */
					public int compareTo(Object o) {
						return 0;
					}
					/** */
					public String toString(){
						return "";
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				public int compareTo(Object o) {
				       ^^^
			Javadoc: Missing tag for return type
			----------
			2. ERROR in X.java (at line 4)
				public int compareTo(Object o) {
				                            ^
			Javadoc: Missing tag for parameter o
			----------
			3. ERROR in X.java (at line 8)
				public String toString(){
				       ^^^^^^
			Javadoc: Missing tag for return type
			----------
			""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug47339c() {
	this.reportMissingJavadocTags = CompilerOptions.ERROR;
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				/** */
				public class X extends RuntimeException {
				\t
					/** */
					public X(String message) {
						super(message);
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				public X(String message) {
				                ^^^^^^^
			Javadoc: Missing tag for parameter message
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 48064.
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=48064">48064</a>
 */
public void testBug48064() {
	this.reportMissingJavadocTags = CompilerOptions.ERROR;
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public X(String str) {}
				}
				""",
			"Y.java",
			"""
				public class Y extends X {
					/**
					 * @see X#X(STRING)
					 */
					public Y(String str) {super(str);}
				}
				"""
		},
		"""
			----------
			1. ERROR in Y.java (at line 3)
				* @see X#X(STRING)
				           ^^^^^^
			Javadoc: STRING cannot be resolved to a type
			----------
			2. ERROR in Y.java (at line 5)
				public Y(String str) {super(str);}
				                ^^^
			Javadoc: Missing tag for parameter str
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug48064a() {
	this.reportMissingJavadocTags = CompilerOptions.ERROR;
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public void foo(String str) {}
				}
				""",
			"Y.java",
			"""
				public class Y extends X {
					/**
					 * @see X#foo(STRING)
					 */
					public void foo(String str) {super.foo(str);}
				}
				"""
		},
		"""
			----------
			1. ERROR in Y.java (at line 3)
				* @see X#foo(STRING)
				             ^^^^^^
			Javadoc: STRING cannot be resolved to a type
			----------
			2. ERROR in Y.java (at line 5)
				public void foo(String str) {super.foo(str);}
				                       ^^^
			Javadoc: Missing tag for parameter str
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 48523.
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=48523">48523</a>
 */
public void testBug48523() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				public class X {
					public void foo() throws IOException {}
				}
				""",
			"Y.java",
			"""
				import java.io.IOException;
				public class Y extends X {
					/**
					 * @throws IOException
					 * @see X#foo()
					 */
					public void foo() throws IOException {}
				}
				"""
		}
	);
}

/**
 * Bug 48711.
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=48711">48711</a>
 */
public void testBug48711() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.*;
				
				public class X {
					/**
					 * @throws IOException
					 * @throws EOFException
					 * @throws FileNotFoundException
					 */
					public void foo() throws IOException {}
				}
				"""
		}
	);
}

/**
 * Bug 45782.
 * When this bug happened, compiler wrongly complained on missing parameters declaration
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=45782">45782</a>
 */
public void testBug45782() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X implements Comparable {
				
					/**
					 * Overridden method with return value and parameters.
					 * {@inheritDoc}
					 */
					public boolean equals(Object obj) {
						return super.equals(obj);
					}
				
					/**
					 * Overridden method with return value and thrown exception.
					 * {@inheritDoc}
					 */
					public Object clone() throws CloneNotSupportedException {
						return super.clone();
					}
				
					/**
					 * Implemented method (Comparable)  with return value and parameters.
					 * {@inheritDoc}
					 */
					public int compareTo(Object o) { return 0; }
				}
				"""
		});
}
public void testBug45782a() {
	this.reportMissingJavadocTags = CompilerOptions.ERROR;
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					/**
					 * Unefficient inheritDoc tag on a method which is neither overridden nor implemented...
					 * {@inheritDoc}
					 */
					public int foo(String str) throws IllegalArgumentException { return 0; }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				* {@inheritDoc}
				    ^^^^^^^^^^
			Javadoc: Unexpected tag
			----------
			2. ERROR in X.java (at line 6)
				public int foo(String str) throws IllegalArgumentException { return 0; }
				       ^^^
			Javadoc: Missing tag for return type
			----------
			3. ERROR in X.java (at line 6)
				public int foo(String str) throws IllegalArgumentException { return 0; }
				                      ^^^
			Javadoc: Missing tag for parameter str
			----------
			4. ERROR in X.java (at line 6)
				public int foo(String str) throws IllegalArgumentException { return 0; }
				                                  ^^^^^^^^^^^^^^^^^^^^^^^^
			Javadoc: Missing tag for declared exception IllegalArgumentException
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 49260.
 * When this bug happened, compiler wrongly complained on Invalid parameters declaration
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=49260">49260</a>
 */
public void testBug49260() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.Vector;
				public final class X {
					int bar(String str, int var, Vector list, char[] array) throws IllegalAccessException { return 0; }
					/**
					 * Valid method reference on several lines
					 * @see #bar(String str,
					 * 		int var,
					 * 		Vector list,
					 * 		char[] array)
					 */
					void foo() {}
				}
				""" });
}

/**
 * Bug 48385.
 * When this bug happened, compiler does not complain on CharOperation references in @link tags
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=48385">48385</a>
 */
public void testBug48385() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.Vector;
				public class X {
					/**
					 * Method outside javaDoc Comment
					 *  1) {@link String} tag description not empty
					 *  2) {@link CharOperation Label not empty} tag description not empty
					 * @param str
					 * @param var tag description not empty
					 * @param list third param with embedded tag: {@link Vector}
					 * @param array fourth param with several embedded tags on several lines:
					 *  1) {@link String} tag description not empty
					 *  2) {@linkplain CharOperation Label not empty} tag description not empty
					 * @throws IllegalAccessException
					 * @throws NullPointerException tag description not empty
					 * @return an integer
					 * @see String
					 * @see Vector tag description not empty
					 * @see Object tag description includes embedded tags and several lines:
					 *  1) {@link String} tag description not empty
					 *  2) {@link CharOperation Label not empty} tag description not empty
					 */
					int foo(String str, int var, Vector list, char[] array) throws IllegalAccessException { return 0; }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 6)
				*  2) {@link CharOperation Label not empty} tag description not empty
				             ^^^^^^^^^^^^^
			Javadoc: CharOperation cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 12)
				*  2) {@linkplain CharOperation Label not empty} tag description not empty
				                  ^^^^^^^^^^^^^
			Javadoc: CharOperation cannot be resolved to a type
			----------
			3. ERROR in X.java (at line 20)
				*  2) {@link CharOperation Label not empty} tag description not empty
				             ^^^^^^^^^^^^^
			Javadoc: CharOperation cannot be resolved to a type
			----------
			""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

public void testBug48385And49620() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.Vector;
				public class X {
					/**
					 * Method outside javaDoc Comment
					 *  1) {@link
					 * 				String} tag description not empty
					 *  2) {@link
					 * 				CharOperation Label not empty} tag description not empty
					 * @param
					 * 				str
					 * @param
					 * 				var tag description not empty
					 * @param list third param with embedded tag: {@link
					 * 				Vector} but also on several lines: {@link
					 * 				CharOperation}
					 * @param array fourth param with several embedded tags on several lines:
					 *  1) {@link String} tag description not empty
					 *  2) {@link CharOperation Label not empty} tag description not empty
					 * @throws
					 * 					IllegalAccessException
					 * @throws
					 * 					NullPointerException tag description not empty
					 * @return
					 * 					an integer
					 * @see
					 * 			String
					 * @see
					 * 		Vector
					 * 		tag description not empty
					 * @see Object tag description includes embedded tags and several lines:
					 *  1) {@link String} tag description not empty
					 *  2) {@link CharOperation Label not empty} tag description not empty
					 */
					int foo(String str, int var, Vector list, char[] array) throws IllegalAccessException { return 0; }
				}
				"""},
		"""
			----------
			1. ERROR in X.java (at line 8)
				* 				CharOperation Label not empty} tag description not empty
				  				^^^^^^^^^^^^^
			Javadoc: CharOperation cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 15)
				* 				CharOperation}
				  				^^^^^^^^^^^^^
			Javadoc: CharOperation cannot be resolved to a type
			----------
			3. ERROR in X.java (at line 18)
				*  2) {@link CharOperation Label not empty} tag description not empty
				             ^^^^^^^^^^^^^
			Javadoc: CharOperation cannot be resolved to a type
			----------
			4. ERROR in X.java (at line 32)
				*  2) {@link CharOperation Label not empty} tag description not empty
				             ^^^^^^^^^^^^^
			Javadoc: CharOperation cannot be resolved to a type
			----------
			""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug48385a() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					/**
					 * Method outside javaDoc Comment
					 *  1) {@link } Missing reference
					 *  2) {@link Unknown} Cannot be resolved
					 *  3) {@link *} Missing reference
					 *  4) {@link #} Invalid reference
					 *  5) {@link String } } Valid reference
					 *  6) {@link String {} Invalid tag
					 * @return int
					 */
					int foo() {return 0;}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				*  1) {@link } Missing reference
				        ^^^^
			Javadoc: Missing reference
			----------
			2. ERROR in X.java (at line 5)
				*  2) {@link Unknown} Cannot be resolved
				             ^^^^^^^
			Javadoc: Unknown cannot be resolved to a type
			----------
			3. ERROR in X.java (at line 6)
				*  3) {@link *} Missing reference
				        ^^^^
			Javadoc: Missing reference
			----------
			4. ERROR in X.java (at line 7)
				*  4) {@link #} Invalid reference
				             ^
			Javadoc: Invalid reference
			----------
			5. ERROR in X.java (at line 9)
				*  6) {@link String {} Invalid tag
				      ^^^^^^^^^^^^^^^
			Javadoc: Missing closing brace for inline tag
			----------
			""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 49491.
 * When this bug happened, compiler complained on duplicated throws tag
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=49491">49491</a>
 */
public void testBug49491() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public final class X {
					/**
					 * Now valid duplicated throws tag
					 * @throws IllegalArgumentException First comment
					 * @throws IllegalArgumentException Second comment
					 * @throws IllegalArgumentException Last comment
					 */
					void foo() throws IllegalArgumentException {}
				}
				""" });
}
public void testBug49491a() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public final class X {
					/**
					 * Duplicated param tags should be still flagged
					 * @param str First comment
					 * @param str Second comment
					 * @param str Last comment
					 */
					void foo(String str) {}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				* @param str Second comment
				         ^^^
			Javadoc: Duplicate tag for parameter
			----------
			2. ERROR in X.java (at line 6)
				* @param str Last comment
				         ^^^
			Javadoc: Duplicate tag for parameter
			----------
			""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 48376.
 * When this bug happened, compiler complained on duplicated throws tag
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=48376">48376</a>
 */
public void testBug48376() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				/**
					* @see <a href="http:/www.ibm.com">IBM Home Page</a>
					* @see <a href="http:/www.ibm.com">
					*          IBM Home Page</a>
					* @see <a href="http:/www.ibm.com">
					*          IBM Home Page
					* 			</a>
					* @see <a href="http:/www.ibm.com">
					*
					*          IBM
					*
					*          Home Page
					*
					*
					* 			</a>
					* @see Object
					*/
				public class X {
				}
				"""
	 });
}
public void testBug48376a() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				/**
					* @see <a href="http:/www.ibm.com">IBM Home Page
					* @see <a href="http:/www.ibm.com">
					*          IBM Home Page
					* @see <a href="http:/www.ibm.com">
					*          IBM Home Page<
					* 			/a>
					* @see <a href="http:/www.ibm.com">
					*
					*          IBM
					*
					*          Home Page
					*
					*
					* 		\t
					* @see Unknown
					*/
				public class X {
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				* @see <a href="http:/www.ibm.com">IBM Home Page
				       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Javadoc: Malformed link reference
			----------
			2. ERROR in X.java (at line 3)
				* @see <a href="http:/www.ibm.com">
				*          IBM Home Page
				       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Javadoc: Malformed link reference
			----------
			3. ERROR in X.java (at line 6)
				*          IBM Home Page<
				                        ^
			Javadoc: Malformed link reference
			----------
			4. ERROR in X.java (at line 8)
				* @see <a href="http:/www.ibm.com">
				*
				*          IBM
				*
				*          Home Page
				       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Javadoc: Malformed link reference
			----------
			5. ERROR in X.java (at line 16)
				* @see Unknown
				       ^^^^^^^
			Javadoc: Unknown cannot be resolved to a type
			----------
			""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 50644.
 * When this bug happened, compiler complained on duplicated throws tag
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=50644">50644</a>
 */
public void testBug50644() {
	this.reportInvalidJavadoc = CompilerOptions.IGNORE;
	runConformTest(
		new String[] {
			"p1/X.java",
			"""
				package p1;
				public class X {
					/**
					 * Should not be @deprecated
					 */
					public void foo() {}
				}
				""",
			"p2/Y.java",
			"""
				package p2;
				import p1.X;
				public class Y {
					public void foo() {
						X x = new X();
						x.foo();
					}
				}
				"""
	 });
}

/**
 * Bug 50695.
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=50695">50695</a>
 */
public void testBug50695() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					/**
					 * @see java
					 * @see java.util
					 */
					void foo() {}
				}
				"""
		 });
}
public void testBug50695b() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					/**
					 * @see java.unknown
					 */
					void foo() {}
				}
				"""
		 },
		"""
			----------
			1. ERROR in X.java (at line 3)
				* @see java.unknown
				       ^^^^^^^^^^^^
			Javadoc: java.unknown cannot be resolved to a type
			----------
			""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 51626.
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=51626">51626</a>
 */
public void testBug51626() {
	runConformTest(
		new String[] {
			"p1/X.java",
			"""
				package p1;
				public class X {
					/**
					 * @see String
					 * toto @deprecated
					 */
					public void foo() {}
				}
				""",
			"p2/Y.java",
			"""
				package p2;
				import p1.*;
				public class Y {
					void foo() {
						X x = new X();\s
						x.foo();
					}
				}
				"""
	 });
}

/**
 * Bug 52216.
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=52216">52216</a>
 */
public void testBug52216() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				/**
				 * Valid ref with white spaces at the end
				* @see <a href="http://www.ietf.org/rfc/rfc2045.txt">RFC 2045 - Section 6.8</a>		  \s
				*/
				public class X {
				}
				"""
	 });
}
public void testBug52216a() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				/**
				* @see "Valid ref with white spaces at the end"	  \s
				*/
				public class X {
				}
				"""
	 });
}
public void testBug52216b() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				/**
				* @see <a href="http://www.ietf.org/rfc/rfc2045.txt">RFC 2045 - Section 6.8</a>		  \s
				* @see <a href="http://www.ietf.org/rfc/rfc2045.txt">RFC 2045 - Section 6.8</a>
				* @see <a href="http://www.ietf.org/rfc/rfc2045.txt">RFC 2045 - Section 6.8</a>			,
				* @see "Valid ref with white spaces at the end"
				* @see "Valid ref with white spaces at the end"	  \s
				* @see "Invalid ref"	   .
				*/
				public class X {
				}
				"""
		 },
		"""
			----------
			1. ERROR in X.java (at line 4)
				* @see <a href="http://www.ietf.org/rfc/rfc2045.txt">RFC 2045 - Section 6.8</a>			,
				                                                                            ^^^^^^^
			Javadoc: Unexpected text
			----------
			2. ERROR in X.java (at line 7)
				* @see "Invalid ref"	   .
				                    ^^^^^
			Javadoc: Unexpected text
			----------
			""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 51529.
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=51529">51529</a>
 */
public void testBug51529() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.Vector;
				public class X {
					/**
					 * @see Vector
					 */
					void foo() {}
				}
				"""
	 });
}
public void testBug51529a() {
	this.reportInvalidJavadoc = CompilerOptions.IGNORE;
	this.reportMissingJavadocComments = CompilerOptions.IGNORE;
	runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.Vector;
				public class X {
					/**
					 * @see Vector
					 */
					void foo() {}
				}
				"""
		}
	);
}
public void testBug51529b() {
	this.docCommentSupport = CompilerOptions.DISABLED;
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.Vector;
				public class X {
					/**
					 * @see Vector
					 */
					void foo() {}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				import java.util.Vector;
				       ^^^^^^^^^^^^^^^^
			The import java.util.Vector is never used
			----------
			""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * bug 51911: [Javadoc] @see method w/out ()
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=51911"
 */
// Conform since bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=191322 has been fixed
public void testBug51911() {
	// Warn an ambiguous method reference
	runConformTest(
		new String[] {
			"X.java",
			"""
				/**
				 * @see #foo
				 */
				public class X {
					public void foo(int i, float f) {}
					public void foo(String str) {}
				}
				"""
	 	}
	);
}
public void testBug51911a() {
	// Accept unambiguous method reference
	runConformTest(
		new String[] {
			"X.java",
			"""
				/**
				 * @see #foo
				 */
				public class X {
					public void foo(String str) {}
				}
				"""
	 	}
	);
}
public void testBug51911b() {
	// Accept field reference with method name
	runConformTest(
		new String[] {
			"X.java",
			"""
				/**
				 * @see #foo
				 */
				public class X {
					public int foo;
					public void foo(String str) {}
				}
				"""
	 	}
	);
}
public void testBug51911c() {
	// Accept field reference with ambiguous method name
	runConformTest(
		new String[] {
			"X.java",
			"""
				/**
				 * @see #foo
				 */
				public class X {
					public int foo;
					public void foo() {}
					public void foo(String str) {}
				}
				"""
	 	}
	);
}

/**
 * Bug 53279: [Javadoc] Compiler should complain when inline tag is not terminated
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=53279">53279</a>
 */
public void testBug53279() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					/**
					 * Unterminated inline tags
					 *  {@link Object
					 */
					void foo() {}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				*  {@link Object
				   ^^^^^^^^^^^^^
			Javadoc: Missing closing brace for inline tag
			----------
			""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug53279a() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					/**
					 * Unterminated inline tags
					 *  {@link Object
					 * @return int
					 */
					int foo() {return 0;}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				*  {@link Object
				   ^^^^^^^^^^^^^
			Javadoc: Missing closing brace for inline tag
			----------
			""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug53279b() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					/**
					 * Unterminated inline tags
					 *  {@link       \s
					 */
					void foo() {}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				*  {@link       \s
				   ^^^^^^^^^^^^^^
			Javadoc: Missing closing brace for inline tag
			----------
			2. ERROR in X.java (at line 4)
				*  {@link       \s
				     ^^^^
			Javadoc: Missing reference
			----------
			""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug53279c() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					/**
					 * Unterminated inline tags
					 *  {@link
					 * @return int
					 */
					int foo() {return 0;}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				*  {@link
				   ^^^^^^
			Javadoc: Missing closing brace for inline tag
			----------
			2. ERROR in X.java (at line 4)
				*  {@link
				     ^^^^
			Javadoc: Missing reference
			----------
			""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 53290: [Javadoc] Compiler should complain when tag name is not correct
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=53290">53290</a>
 */
public void testBug53290() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					/**
					 * See as inline tag
					 *  {@see Object}
					 *  @see Object
					 *  @link Object
					 *  {@link Object}
					 */
					void foo() {}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				*  {@see Object}
				     ^^^
			Javadoc: Unexpected tag
			----------
			2. ERROR in X.java (at line 6)
				*  @link Object
				    ^^^^
			Javadoc: Unexpected tag
			----------
			""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 62812: Some malformed javadoc tags are not reported as malformed
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=62812">62812</a>
 */
public void testBug62812() {
	runNegativeTest(
		new String[] {
			"Test.java",
			"""
				/**
				 * @see Object#clone())
				 * @see Object#equals(Object)}
				 * @see Object#equals(Object))
				 * @see Object#equals(Object)xx
				 */
				public class Test {
				}
				"""
		},
		"""
			----------
			1. ERROR in Test.java (at line 2)
				* @see Object#clone())
				                   ^^^
			Javadoc: Malformed reference (missing end space separator)
			----------
			2. ERROR in Test.java (at line 3)
				* @see Object#equals(Object)}
				                    ^^^^^^^^^
			Javadoc: Malformed reference (missing end space separator)
			----------
			3. ERROR in Test.java (at line 4)
				* @see Object#equals(Object))
				                    ^^^^^^^^^
			Javadoc: Malformed reference (missing end space separator)
			----------
			4. ERROR in Test.java (at line 5)
				* @see Object#equals(Object)xx
				                    ^^^^^^^^^^
			Javadoc: Malformed reference (missing end space separator)
			----------
			""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug62812a() {
	runNegativeTest(
		new String[] {
			"Test.java",
			"""
				/**
				 * {@link Object#clone())}
				 * {@link Object#equals(Object)}
				 * {@link Object#equals(Object))}
				 * {@link Object#equals(Object)xx}
				 */
				public class Test {
				}
				"""
		},
		"""
			----------
			1. ERROR in Test.java (at line 2)
				* {@link Object#clone())}
				                     ^^^^
			Javadoc: Malformed reference (missing end space separator)
			----------
			2. ERROR in Test.java (at line 4)
				* {@link Object#equals(Object))}
				                      ^^^^^^^^^^
			Javadoc: Malformed reference (missing end space separator)
			----------
			3. ERROR in Test.java (at line 5)
				* {@link Object#equals(Object)xx}
				                      ^^^^^^^^^^^
			Javadoc: Malformed reference (missing end space separator)
			----------
			""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 51606: [Javadoc] Compiler should complain when tag name is not correct
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=51606">51606</a>
 */
// Cleaned up this test as part of fix for https://bugs.eclipse.org/bugs/show_bug.cgi?id=247037
// We should not complain about the missing @param tag for Y.foo at all, since the comments are
// automatically inherited.
public void testBug51606() {
	this.reportMissingJavadocTags = CompilerOptions.ERROR;
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  /**
				   * @param a aaa
				   * @param b bbb
				   */
				  public void foo(int a, int b) {
				  }
				}
				""",
			"Y.java",
			"""
				public class Y extends X {
				  /**
				  *  @param a {@inheritDoc}
				   */
				  public void foo(int a, int b) {
				  }
				}
				"""
		},
		""
	);
}
public void testBug51606a() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  /**
				   * @param a aaa
				   * @param b bbb
				   */
				  public void foo(int a, int b) {
				  }
				}
				""",
			"Y.java",
			"""
				public class Y extends X {
				  /**
				   * {@inheritDoc}
				  *  @param a aaaaa
				   */
				  public void foo(int a, int b) {
				  }
				}
				"""
		},
		""
	);
}
public void testBug51606b() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  /**
				   * @param a aaa
				   * @param b bbb
				   */
				  public void foo(int a, int b) {
				  }
				}
				""",
			"Y.java",
			"""
				public class Y extends X {
				  /**
				   * Text before inherit tag
				   * {@inheritDoc}
				  *  @param a aaaaa
				   */
				  public void foo(int a, int b) {
				  }
				}
				"""
		}
	);
}
public void testBug51606c() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  /**
				   * @param a aaa
				   * @param b bbb
				   */
				  public void foo(int a, int b) {
				  }
				}
				""",
			"Y.java",
			"""
				public class Y extends X {
				  /**
				   * Text before inherit tag {@inheritDoc}
				  *  @param a aaaaa
				   */
				  public void foo(int a, int b) {
				  }
				}
				"""
		}
	);
}

/**
 * Bug 65174: Spurious "Javadoc: Missing reference" error
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=65174">65174</a>
 */
public void testBug65174() {
	runConformTest(
		new String[] {
			"Test.java",
			"""
				/**
				 * Comment with no error: {@link
				 * Object valid} because it\'s not on first line
				 */
				public class Test {
					/** Comment previously with error: {@link
					 * Object valid} because tag is on comment very first line
					 */
					void foo() {}
				}
				"""
		}
	);
}
public void testBug65174a() {
	runConformTest(
		new String[] {
			"Test.java",
			"""
				/**
				 * Comment with no error: {@link    	\t
				 * Object valid} because it\'s not on first line
				 */
				public class Test {
					/** Comment previously with error: {@link   	\t
					 * Object valid} because tag is on comment very first line
					 */
					void foo() {}
				}
				"""
		}
	);
}
public void testBug65174b() {
	runNegativeTest(
		new String[] {
			"Test.java",
			"""
				/**
				 * Comment with no error: {@link java.lang.
				 * Object valid} because it\'s not on first line
				 */
				public class Test {
					/** Comment previously with error: {@link java.lang.
					 * Object valid} because tag is on comment very first line
					 */
					void foo() {}
				}
				"""
		},
		"""
			----------
			1. ERROR in Test.java (at line 2)\r
				* Comment with no error: {@link java.lang.\r
				                               ^^^^^^^^^^^
			Javadoc: Invalid reference
			----------
			2. ERROR in Test.java (at line 6)\r
				/** Comment previously with error: {@link java.lang.\r
				                                         ^^^^^^^^^^^
			Javadoc: Invalid reference
			----------
			"""

	);
}
public void testBug65174c() {
	runConformTest(
		new String[] {
			"Test.java",
			"""
				/**
				 * Comment with no error: {@link Object
				 * valid} because it\'s not on first line
				 */
				public class Test {
					/** Comment previously with no error: {@link Object
					 * valid} because tag is on comment very first line
					 */
					void foo() {}
				}
				"""
		}
	);
}
public void testBug65174d() {
	runConformTest(
		new String[] {
			"Test.java",
			"""
				public class Test {
					/** Comment previously with no error: {@link Object valid} comment on one line */
					void foo1() {}
					/** Comment previously with no error: {@link Object valid}       */
					void foo2() {}
					/** Comment previously with no error: {@link Object valid}*/
					void foo3() {}
					/**                    {@link Object valid} comment on one line */
					void foo4() {}
					/**{@link Object valid} comment on one line */
					void foo5() {}
					/**       {@link Object valid} 				*/
					void foo6() {}
					/**{@link Object valid} 				*/
					void foo7() {}
					/**				{@link Object valid}*/
					void foo8() {}
					/**{@link Object valid}*/
					void foo9() {}
				}
				"""
		}
	);
}

/**
 * bug 65180: Spurious "Javadoc: xxx cannot be resolved or is not a field" error with inner classes
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=65180"
 */
 // Conform since bug "http://bugs.eclipse.org/bugs/show_bug.cgi?id=191322" has been fixed
public void testBug65180() {
	runConformTest(
		new String[] {
			"Test.java",
			"""
				public class Test {
					public class Inner {
						/**
						 * Does something.
						 *\s
						 * @see #testFunc
						 */
						public void innerFunc() {
							testFunc();
						}
					}
				\t
					public void testFunc() {}
				}
				
				"""
		}
	);
}
public void testBug65180a() {
	runConformTest(
		new String[] {
			"Test.java",
			"""
				public class Test {
					public class Inner {
						/**
						 * Does something.
						 *\s
						 * @see #testFunc()
						 */
						public void innerFunc() {
							testFunc();
						}
					}
				\t
					public void testFunc() {}
				}
				"""
		}
	);
}
public void testBug65180b() {
	runConformTest(
		new String[] {
			"Test.java",
			"""
				public class Test {
					public class Inner {
						/**
						 * Does something.
						 *\s
						 * @see Test#testFunc
						 * @see Test#testFunc()
						 */
						public void innerFunc() {
							testFunc();
						}
					}
				\t
					public void testFunc() {}
				}
				"""
		}
	);
}
 // Conform since bug "http://bugs.eclipse.org/bugs/show_bug.cgi?id=191322" has been fixed
public void testBug65180c() {
	runConformTest(
		new String[] {
			"Test.java",
			"""
				public class Test {
					public class Inner {
						/**
						 * Does something.
						 *\s
						 * @see #testFunc
						 */
						public void innerFunc() {
							testFunc();
						}
					}
				\t
					public void testFunc() {}
					public void testFunc(String str) {}
				}
				"""
		}
	);
}
public void testBug65180d() {
	runConformTest(
		new String[] {
			"Test.java",
			"""
				public class Test {
					int testField;
					public class Inner {
						/**
						 * Does something.
						 *\s
						 * @see #testField
						 * @see #testFunc(int)
						 */
						public void innerFunc() {
							testFunc(testField);
						}
					}
				\t
					public void testFunc(int test) {
						testField = test;\s
					}
				}
				"""
		}
	);
}
public void testBug65180e() {
	runConformTest(
		new String[] {
			"ITest.java",
			"""
				public interface ITest {
					/**
					 * @see #foo()\s
					 */
					public static int field = 0;
					/**
					 * @see #field
					 */
					public void foo();
				}
				"""
		}
	);
}
public void testBug65180f() {
	runConformTest(
		new String[] {
			"Test.java",
			"""
				public class Test {
				    static class SuperInner {
				    	public int field;
				        public void foo() {}
				     }
				   \s
					public static class Inner extends SuperInner {
						/**
						 * @see #field
						 */
						public static int f;
						/**
						 * @see #foo()
						 */
						public static void bar() {}
					}
				\t
					public void foo() {}
				}"""
		}
	);
}

/**
 * bug 65253: [Javadoc] @@tag is wrongly parsed as @tag
 * test Verify that @@return is not interpreted as a return tag<br>
 * 	Note that since fix for bug 237742, the '@' in a tag name does no longer
 * 	flag it as invalid...
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=65253"
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=237742"
 */
public void testBug65253() {
	this.reportMissingJavadocTags = CompilerOptions.ERROR;
	runNegativeTest(
		new String[] {
			"Test.java",
			"""
				/**
				 * Comment\s
				 * @@@@see Unknown Should not complain on ref
				 */
				public class Test {
					/**
					 * Comment
					 * @@@param xxx Should not complain on param
					 * @@return int
					 */
					int foo() { // should warn on missing tag for return type
						return 0;
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in Test.java (at line 11)
				int foo() { // should warn on missing tag for return type
				^^^
			Javadoc: Missing tag for return type
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 66551: Error in org.eclipse.swt project on class PrinterData
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=66551">66551</a>
 */
public void testBug66551() {
	runConformTest(
		new String[] {
			"Test.java",
			"""
				public class Test {
				    int field;
				    /**
				     *  @see #field
				     */
				    void foo(int field) {
				    }
				
				}
				"""
		}
	);
}
public void testBug66551a() {
	runConformTest(
		new String[] {
			"Test.java",
			"""
				public class Test {
				    static int field;
				    /**
				     *  @see #field
				     */
				    static void foo(int field) {
				    }
				
				}
				"""
		}
	);
}
public void testBug66551b() {
	runConformTest(
		new String[] {
			"Test.java",
			"""
				public class Test {
					int field;
					/**
					 * {@link #field}
					 */
					void foo(int field) {
					}
				
				}
				"""
		}
	);
}
public void testBug66551c() {
	runConformTest(
		new String[] {
			"Test.java",
			"""
				public class Test {
					static int field;
					/**
					 * {@link #field}
					 */
					static void foo(int field) {
					}
				
				}
				"""
		}
	);
}

/**
 * Bug 66573: Shouldn't bind to local constructs
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=66573">66573</a>
 */
public void testBug66573() {
	runNegativeTest(
		new String[] {
			"Test.java",
			"""
				public class Test {
				    /**
				     * @see Local
				     */
				    void foo() {
				        class Local {\s
				            // shouldn\'t be seen from javadoc
				         }
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in Test.java (at line 3)
				* @see Local
				       ^^^^^
			Javadoc: Local cannot be resolved to a type
			----------
			""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 68017: Javadoc processing does not detect missing argument to @return
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=68017">68017</a>
 */
public void testBug68017conform() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					/**@return valid integer*/
					public int foo1() {return 0; }
					/**
					 *	@return #
					 */
					public int foo2() {return 0; }
				}
				""",
		}
	);
}
public void testBug68017negative() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					/**@return*/
					public int foo1() {return 0; }
					/**@return        */
					public int foo2() {return 0; }
					/**@return****/
					public int foo3() {return 0; }
					/**
					 *	@return
					 */
					public int foo4() {return 0; }
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				/**@return*/
				    ^^^^^^
			Javadoc: Description expected after @return
			----------
			2. ERROR in X.java (at line 4)
				/**@return        */
				    ^^^^^^
			Javadoc: Description expected after @return
			----------
			3. ERROR in X.java (at line 6)
				/**@return****/
				    ^^^^^^
			Javadoc: Description expected after @return
			----------
			4. ERROR in X.java (at line 9)
				*	@return
				 	 ^^^^^^
			Javadoc: Description expected after @return
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
// Javadoc issue a warning on following tests
public void testBug68017javadocWarning1() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					/**
					 *	@return* */
					public int foo1() {return 0; }
					/**@return** **/
					public int foo2() {return 0; }
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				*	@return* */
				 	 ^^^^^^
			Javadoc: Description expected after @return
			----------
			2. ERROR in X.java (at line 5)
				/**@return** **/
				    ^^^^^^
			Javadoc: Description expected after @return
			----------
			""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug68017javadocWarning2() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					/**
					 *	@return #
					 */
					public int foo1() {return 0; }
					/**
					 *	@return @
					 */
					public int foo2() {return 0; }
				}
				"""
		}
	);
}
public void testBug68017javadocWarning3() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					/**
					 *	@return#
					 *	@return#text
					 */
					public int foo() {return 0; }
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				*	@return#
				 	 ^^^^^^^
			Javadoc: Invalid tag
			----------
			2. ERROR in X.java (at line 4)
				*	@return#text
				 	 ^^^^^^^^^^^
			Javadoc: Invalid tag
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 68025: Javadoc processing does not detect some wrong links
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=68025">68025</a>
 */
public void testBug68025conform() {
	runConformTest(
		new String[] {
			"Y.java",
			"""
				public class Y {
					public int field;
					public void foo() {}
				}
				""",
			"Z.java",
			"""
				public class Z {
					/**
					 *	@see Y#field #valid
					 *	@see Y#foo #valid
					 */
					public void foo1() {}
					/**@see Y#field     # valid*/
					public void foo2() {}
					/**@see Y#foo		# valid*/
					public void foo3() {}
					/**@see Y#foo()
					 *# valid*/
					public void foo4() {}
				}
				"""
		}
	);
}
public void testBug68025negative() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public int field;
					public void foo() {}
					/**
					 *	@see #field#invalid
					 *	@see #foo#invalid
					 */
					public void foo1() {}
					/**@see Y#field# invalid*/
					public void foo2() {}
					/**@see Y#foo#	invalid*/
					public void foo3() {}
					/**@see Y#foo()#
					 *valid*/
					public void foo4() {}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				*	@see #field#invalid
				 	     ^^^^^^^^^^^^^^
			Javadoc: Malformed reference (missing end space separator)
			----------
			2. ERROR in X.java (at line 6)
				*	@see #foo#invalid
				 	     ^^^^^^^^^^^^
			Javadoc: Malformed reference (missing end space separator)
			----------
			3. ERROR in X.java (at line 9)
				/**@see Y#field# invalid*/
				         ^^^^^^^^^^^^^^^^^
			Javadoc: Malformed reference (missing end space separator)
			----------
			4. ERROR in X.java (at line 11)
				/**@see Y#foo#	invalid*/
				         ^^^^^^^^^^^^^^^
			Javadoc: Malformed reference (missing end space separator)
			----------
			5. ERROR in X.java (at line 13)
				/**@see Y#foo()#
				             ^^^
			Javadoc: Malformed reference (missing end space separator)
			----------
			""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 68726: [Javadoc] Target attribute in @see link triggers warning
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=68726">68726</a>
 */
public void testBug68726conform1() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					/**
					 *	@see Object <a href="http://www.eclipse.org" target="_top">Eclipse</a>
					 */
					void foo1() {}
					/**@see Object <a href="http://www.eclipse.org" target="_top" target1="_top1" target2="_top2">Eclipse</a>*/
					void foo2() {}
				}
				"""
		}
	);
}
public void testBug68726conform2() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				/**
					* @see <a href="http:/www.ibm.com" target="_top">IBM Home Page</a>
					* @see <a href="http:/www.ibm.com" target="_top">
					*          IBM Home Page</a>
					* @see <a href="http:/www.ibm.com" target="_top">
					*          IBM Home Page
					* 			</a>
					* @see <a href="http:/www.ibm.com" target="_top">
					*
					*          IBM
					*
					*          Home Page
					*
					*
					* 			</a>
					* @see Object
					*/
				public class X {
				}
				"""
		}
	);
}
public void testBug68726negative1() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					/**
					 * Invalid URL link references
					 *
					 * @see <a href="invalid" target
					 * @see <a href="invalid" target=
					 * @see <a href="invalid" target="
					 * @see <a href="invalid" target="_top
					 * @see <a href="invalid" target="_top"
					 * @see <a href="invalid" target="_top">
					 * @see <a href="invalid" target="_top">
					 * @see <a href="invalid" target="_top">invalid
					 * @see <a href="invalid" target="_top">invalid<
					 * @see <a href="invalid" target="_top">invalid</
					 * @see <a href="invalid" target="_top">invalid</a
					 * @see <a href="invalid" target="_top">invalid</a> no text allowed after the href
					 */
					void foo() {}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				* @see <a href="invalid" target
				       ^^^^^^^^^^^^^^^^^^^^^^^^
			Javadoc: Malformed link reference
			----------
			2. ERROR in X.java (at line 6)
				* @see <a href="invalid" target=
				       ^^^^^^^^^^^^^^^^^^^^^^^^^
			Javadoc: Malformed link reference
			----------
			3. ERROR in X.java (at line 7)
				* @see <a href="invalid" target="
				       ^^^^^^^^^^^^^^^^^^^^^^^^^^
			Javadoc: Malformed link reference
			----------
			4. ERROR in X.java (at line 8)
				* @see <a href="invalid" target="_top
				       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Javadoc: Malformed link reference
			----------
			5. ERROR in X.java (at line 9)
				* @see <a href="invalid" target="_top"
				       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Javadoc: Malformed link reference
			----------
			6. ERROR in X.java (at line 10)
				* @see <a href="invalid" target="_top">
				       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Javadoc: Malformed link reference
			----------
			7. ERROR in X.java (at line 11)
				* @see <a href="invalid" target="_top">
				       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Javadoc: Malformed link reference
			----------
			8. ERROR in X.java (at line 12)
				* @see <a href="invalid" target="_top">invalid
				       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Javadoc: Malformed link reference
			----------
			9. ERROR in X.java (at line 13)
				* @see <a href="invalid" target="_top">invalid<
				                                              ^
			Javadoc: Malformed link reference
			----------
			10. ERROR in X.java (at line 14)
				* @see <a href="invalid" target="_top">invalid</
				                                              ^^
			Javadoc: Malformed link reference
			----------
			11. ERROR in X.java (at line 15)
				* @see <a href="invalid" target="_top">invalid</a
				                                              ^^^
			Javadoc: Malformed link reference
			----------
			12. ERROR in X.java (at line 16)
				* @see <a href="invalid" target="_top">invalid</a> no text allowed after the href
				                                               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Javadoc: Unexpected text
			----------
			""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug68726negative2() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				/**
					* @see <a href="http:/www.ibm.com" target="_top">IBM Home Page
					* @see <a href="http:/www.ibm.com" target="_top">
					*          IBM Home Page
					* @see <a href="http:/www.ibm.com" target="_top">
					*          IBM Home Page<
					* 			/a>
					* @see <a href="http:/www.ibm.com" target="_top">
					*
					*          IBM
					*
					*          Home Page
					*
					*
					* 		\t
					* @see Unknown
					*/
				public class X {
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				* @see <a href="http:/www.ibm.com" target="_top">IBM Home Page
				       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Javadoc: Malformed link reference
			----------
			2. ERROR in X.java (at line 3)
				* @see <a href="http:/www.ibm.com" target="_top">
				*          IBM Home Page
				       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Javadoc: Malformed link reference
			----------
			3. ERROR in X.java (at line 6)
				*          IBM Home Page<
				                        ^
			Javadoc: Malformed link reference
			----------
			4. ERROR in X.java (at line 8)
				* @see <a href="http:/www.ibm.com" target="_top">
				*
				*          IBM
				*
				*          Home Page
				       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Javadoc: Malformed link reference
			----------
			5. ERROR in X.java (at line 16)
				* @see Unknown
				       ^^^^^^^
			Javadoc: Unknown cannot be resolved to a type
			----------
			""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 69272: [Javadoc] Invalid malformed reference (missing separator)
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=69272">69272</a>
 */
public void testBug69272classValid() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					/**@see Object*/
					public void foo1() {}
					/**@see Object
					*/
					public void foo2() {}
					/**@see Object    */
					public void foo3() {}
					/**@see Object****/
					public void foo4() {}
					/**@see Object		****/
					public void foo5() {}
				}
				"""
		}
	);
}
public void testBug69272classInvalid() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					/**@see Object* */
					public void foo1() {}
					/**@see Object*** ***/
					public void foo2() {}
					/**@see Object***
					 */
					public void foo3() {}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				/**@see Object* */
				        ^^^^^^^
			Javadoc: Malformed reference (missing end space separator)
			----------
			2. ERROR in X.java (at line 4)
				/**@see Object*** ***/
				        ^^^^^^^^^
			Javadoc: Malformed reference (missing end space separator)
			----------
			3. ERROR in X.java (at line 6)
				/**@see Object***
				        ^^^^^^^^^
			Javadoc: Malformed reference (missing end space separator)
			----------
			""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug69272fieldValid() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					int field;
					/**@see #field*/
					public void foo1() {}
					/**@see #field
					*/
					public void foo2() {}
					/**@see #field    */
					public void foo3() {}
					/**@see #field****/
					public void foo4() {}
					/**@see #field		********/
					public void foo5() {}
				}
				"""
		}
	);
}
public void testBug69272fieldInvalid() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					int field;
					/**@see #field* */
					public void foo1() {}
					/**@see #field*** ***/
					public void foo2() {}
					/**@see #field***
					 */
					public void foo3() {}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				/**@see #field* */
				        ^^^^^^^
			Javadoc: Malformed reference (missing end space separator)
			----------
			2. ERROR in X.java (at line 5)
				/**@see #field*** ***/
				        ^^^^^^^^^
			Javadoc: Malformed reference (missing end space separator)
			----------
			3. ERROR in X.java (at line 7)
				/**@see #field***
				        ^^^^^^^^^
			Javadoc: Malformed reference (missing end space separator)
			----------
			""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug69272methodValid() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					/**@see Object#wait()*/
					public void foo1() {}
					/**@see Object#wait()
					*/
					public void foo2() {}
					/**@see Object#wait()    */
					public void foo3() {}
					/**@see Object#wait()****/
					public void foo4() {}
					/**@see Object#wait()		****/
					public void foo5() {}
				}
				"""
		}
	);
}
public void testBug69272methodInvalid() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					/**@see Object#wait()* */
					public void foo1() {}
					/**@see Object#wait()*** ***/
					public void foo2() {}
					/**@see Object#wait()***
					 */
					public void foo3() {}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				/**@see Object#wait()* */
				                   ^^^
			Javadoc: Malformed reference (missing end space separator)
			----------
			2. ERROR in X.java (at line 4)
				/**@see Object#wait()*** ***/
				                   ^^^^^
			Javadoc: Malformed reference (missing end space separator)
			----------
			3. ERROR in X.java (at line 6)
				/**@see Object#wait()***
				                   ^^^^^
			Javadoc: Malformed reference (missing end space separator)
			----------
			""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 69275: [Javadoc] Invalid warning on @see link
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=69275">69275</a>
 */
public void testBug69275conform() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					/**@see <a href="http://www.eclipse.org">text</a>*/
					void foo1() {}
					/**@see <a href="http://www.eclipse.org">text</a>
					*/
					void foo2() {}
					/**@see <a href="http://www.eclipse.org">text</a>		*/
					void foo3() {}
					/**@see <a href="http://www.eclipse.org">text</a>**/
					void foo4() {}
					/**@see <a href="http://www.eclipse.org">text</a>     *****/
					void foo5() {}
				}
				"""
		}
	);
}
public void testBug69275negative() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					/**@see <a href="http://www.eclipse.org">text</a>* */
					void foo1() {}
					/**@see <a href="http://www.eclipse.org">text</a>	** **/
					void foo2() {}
					/**@see <a href="http://www.eclipse.org">text</a>**
					*/
					void foo3() {}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				/**@see <a href="http://www.eclipse.org">text</a>* */
				                                              ^^^^^^^
			Javadoc: Unexpected text
			----------
			2. ERROR in X.java (at line 4)
				/**@see <a href="http://www.eclipse.org">text</a>	** **/
				                                              ^^^^^^^^^^
			Javadoc: Unexpected text
			----------
			""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 69302: [Javadoc] Invalid reference warning inconsistent with javadoc tool
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=69302"
 */
public void testBug69302conform1() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					/**
					 *	@see Object <a href="http://www.eclipse.org">Eclipse</a>
					 */
					void foo1() {}
					/**
					 *	@see Object "Valid string reference"
					 */
					void foo2() {}
				}
				"""
		}
	);
}
public void testBug69302negative1() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					/**
					 *	@see Unknown <a href="http://www.eclipse.org">Eclipse</a>
					 */
					void foo1() {}
					/**
					 *	@see Unknown "Valid string reference"
					 */
					void foo2() {}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				*	@see Unknown <a href="http://www.eclipse.org">Eclipse</a>
				 	     ^^^^^^^
			Javadoc: Unknown cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 7)
				*	@see Unknown "Valid string reference"
				 	     ^^^^^^^
			Javadoc: Unknown cannot be resolved to a type
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug69302negative2() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					/**@see Unknown blabla <a href="http://www.eclipse.org">text</a>*/
					void foo1() {}
					/**@see Unknown blabla "Valid string reference"*/
					void foo2() {}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				/**@see Unknown blabla <a href="http://www.eclipse.org">text</a>*/
				        ^^^^^^^
			Javadoc: Unknown cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 4)
				/**@see Unknown blabla "Valid string reference"*/
				        ^^^^^^^
			Javadoc: Unknown cannot be resolved to a type
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * bug 70892: [1.5][Javadoc] Compiler should parse reference for inline tag @value
 * test Ensure that reference in tag 'value' is only verified when source level >= 1.5
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=70892"
 */
public void testBug70892a() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					/**
					 * {@value}
					 */
					static int field1;
					/**
					 * {@value }
					 */
					static int field2;
					/**
					 * {@value #field}
					 */
					static int field;
				}
				"""
		}
	);
}
public void testBug70892b() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			public class X {
				/**
				 * {@value "invalid"}
				 */
				final static int field1 = 1;
				/**
				 * {@value <a href="invalid">invalid</a>} invalid
				 */
				final static int field2 = 2;
				/**
				 * {@value #field}
				 */
				final static int field3 = 3;
				/**
				 * {@value #foo}
				 */
				final static int field4 = 4;
				/**
				 * {@value #foo()}
				 */
				final static int field5 = 5;
				void foo() {}
			}
			"""
	};
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		runConformTest(testFiles);
	} else {
		runNegativeTest(testFiles,
			"""
				----------
				1. ERROR in X.java (at line 3)\r
					* {@value "invalid"}\r
					          ^^^^^^^^^
				Javadoc: Only static field reference is allowed for @value tag
				----------
				2. ERROR in X.java (at line 7)\r
					* {@value <a href="invalid">invalid</a>} invalid\r
					          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Only static field reference is allowed for @value tag
				----------
				3. ERROR in X.java (at line 11)\r
					* {@value #field}\r
					           ^^^^^
				Javadoc: field cannot be resolved or is not a field
				----------
				4. ERROR in X.java (at line 15)\r
					* {@value #foo}\r
					           ^^^
				Javadoc: Only static field reference is allowed for @value tag
				----------
				5. ERROR in X.java (at line 19)\r
					* {@value #foo()}\r
					           ^^^^^
				Javadoc: Only static field reference is allowed for @value tag
				----------
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
}

/**
 * Bug 73348: [Javadoc] Missing description for return tag is not always warned
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=73348">73348</a>
 */
public void testBug73348conform() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					/**
					 *	@return     \s
					 *	int
					 */
					public int foo1() {return 0; }
					/**
					 *	@return     \s
					 *	int
					 *	@see Object
					 */
					public int foo2() {return 0; }
				}
				""",
		}
	);
}
public void testBug73348negative() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					/**
					 *	@return
					 *	@see Object
					 */
					public int foo1() {return 0; }
					/**
					 *	@return     \s
					 *	@see Object
					 */
					public int foo2() {return 0; }
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				*	@return
				 	 ^^^^^^
			Javadoc: Description expected after @return
			----------
			2. ERROR in X.java (at line 8)
				*	@return     \s
				 	 ^^^^^^
			Javadoc: Description expected after @return
			----------
			""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * bug 73352: [Javadoc] Missing description should be warned for all tags
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=73352"
 */
public void testBug73352a() {
	String[] units = new String[] {
		"X.java",
		"""
			/**
			* @since
			* @author
			* @version
			*/
			public class X {
				/**
				 * @param  aParam
				 * @return
				 * @see
				 * @since
				 * @throws NullPointerException
				 * @exception NullPointerException
				 * @serial
				 * @serialData
				 * @serialField
				 * @deprecated
				 */
				public String foo(String aParam) {
					return new String();
				}
			}
			"""
	};
	this.reportInvalidJavadoc = CompilerOptions.WARNING;
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runConformTest(
			true,
			units,
			"""
				----------
				1. WARNING in X.java (at line 2)
					* @since
					   ^^^^^
				Javadoc: Description expected after @since
				----------
				2. WARNING in X.java (at line 3)
					* @author
					   ^^^^^^
				Javadoc: Description expected after @author
				----------
				3. WARNING in X.java (at line 4)
					* @version
					   ^^^^^^^
				Javadoc: Description expected after @version
				----------
				4. WARNING in X.java (at line 8)
					* @param  aParam
					          ^^^^^^
				Javadoc: Description expected after this reference
				----------
				5. WARNING in X.java (at line 9)
					* @return
					   ^^^^^^
				Javadoc: Description expected after @return
				----------
				6. WARNING in X.java (at line 10)
					* @see
					   ^^^
				Javadoc: Missing reference
				----------
				7. WARNING in X.java (at line 11)
					* @since
					   ^^^^^
				Javadoc: Description expected after @since
				----------
				8. WARNING in X.java (at line 12)
					* @throws NullPointerException
					          ^^^^^^^^^^^^^^^^^^^^
				Javadoc: Description expected after this reference
				----------
				9. WARNING in X.java (at line 13)
					* @exception NullPointerException
					             ^^^^^^^^^^^^^^^^^^^^
				Javadoc: Description expected after this reference
				----------
				10. WARNING in X.java (at line 14)
					* @serial
					   ^^^^^^
				Javadoc: Description expected after @serial
				----------
				11. WARNING in X.java (at line 15)
					* @serialData
					   ^^^^^^^^^^
				Javadoc: Description expected after @serialData
				----------
				12. WARNING in X.java (at line 16)
					* @serialField
					   ^^^^^^^^^^^
				Javadoc: Description expected after @serialField
				----------
				13. WARNING in X.java (at line 17)
					* @deprecated
					   ^^^^^^^^^^
				Javadoc: Description expected after @deprecated
				----------
				""",
			null,
			null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings
		);
}
public void testBug73352b() {
	String[] units = new String[] {
		"X.java",
		"""
			/**
			* @since 1.0
			* @author John Doe
			* @version 1.1
			*/
			public class X {
				/**
				 * @param  aParam comment
				 * @return String
				 * @see String
				 * @since 1.1
				 * @throws NullPointerException an exception
				 * @exception NullPointerException an exception
				 * @serial aSerial
				 * @serialData aSerialData
				 * @serialField aSerialField
				 * @deprecated use another method
				 */
				public String foo(String aParam) {
					return new String();
				}
			}
			"""
	};
	this.reportInvalidJavadoc = CompilerOptions.WARNING;
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runConformTest(units);
}
public void testBug73352c() {
	String[] units = new String[] {
		"X.java",
		"""
			/**
			* @since
			* @author
			* @version
			*/
			public class X {
				/**
				 * @param  aParam
				 * @return
				 * @see
				 * @since
				 * @throws NullPointerException
				 * @exception NullPointerException
				 * @serial
				 * @serialData
				 * @serialField
				 * @deprecated
				 */
				public String foo(String aParam) {
					return new String();
				}
			}
			"""
	};
	this.reportInvalidJavadoc = CompilerOptions.WARNING;
	this.reportMissingJavadocDescription = CompilerOptions.RETURN_TAG;
	runConformTest(
		true,
		units,
		"""
			----------
			1. WARNING in X.java (at line 9)
				* @return
				   ^^^^^^
			Javadoc: Description expected after @return
			----------
			2. WARNING in X.java (at line 10)
				* @see
				   ^^^
			Javadoc: Missing reference
			----------
			""",
		null,
		null,
		JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings
		);
}

public void testBug73352d() {
	String[] units = new String[] {
		"X.java",
		"""
			/**
			* @since
			* @author
			* @version
			*/
			public class X {
				/**
				 * @param  aParam
				 * @return
				 * @see
				 * @since
				 * @throws NullPointerException
				 * @exception NullPointerException
				 * @serial
				 * @serialData
				 * @serialField
				 * @deprecated
				 */
				public String foo(String aParam) {
					return new String();
				}
			}
			"""
	};
	this.reportInvalidJavadoc = CompilerOptions.WARNING;
	this.reportMissingJavadocDescription = CompilerOptions.NO_TAG;
	runConformTest(units);
}

/**
 * Bug 73479: [Javadoc] Improve error message for invalid link in @see tags
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=73479">73479</a>
 */
public void testBug73479() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					/**
					 *	@see <a href="spec.html#section">Java Spec<a>
					 */
					public void foo() {}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				*	@see <a href="spec.html#section">Java Spec<a>
				 	                                          ^^^
			Javadoc: Malformed link reference
			----------
			""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 73995: [Javadoc] Wrong warning for missing return type description for &#064;return {&#064;inheritdoc}
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=73995">73995</a>
 */
public void testBug73995() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X extends Base {
					/**
					 *	@return {@link Object}    \s
					 */
					public int foo1() {return 0; }
					/** @return {@inheritDoc} */
					public int foo2() {return 0; }
					/**
					 *	@return
					 *		{@unknown_tag}
					 */
					public int foo3() {return 0; }
				}
				class Base {
				/** return "The foo2 value" */\
				public int foo2(){return 0;}
				}"""
		});
}

/**
 * Bug 74369: [Javadoc] incorrect javadoc in local class
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=74369">74369</a>
 */
public void testBug74369() {
	runConformTest(
		new String[] {
			"Test.java",
			"""
				public class Test {
				   public void method() {
				       /**
				        * @see #hsgdfdj
				        */
				        System.out.println("println");
				        class Local {}
				    }
				}"""
		}
	);
}
public void testBug74369deprecated() {
	runNegativeTest(
		new String[] {
			"p/Y.java",
			"""
				package p;
				
				
				public class Y {
				   /**
				    * @deprecated
				    */
				   public void bar() {}
				}
				""",
			"X.java",
			"""
				import p.Y;
				
				public class X {
					Object obj = new Object() {
						public boolean equals(Object o) {
							/**
							 * @deprecated
							 */
					        System.out.println("println");
					        class Local {
					        	void bar() {
									new Y().bar();
					        	}
					        }
							return super.equals(o);
						}
					};
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 12)
				new Y().bar();
				        ^^^^^
			The method bar() from the type Y is deprecated
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 76324: [Javadoc] Wrongly reports invalid link format in @see and @link
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=76324">76324</a>
 */
public void testBug76324() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				
				/**
				 * Subclasses perform GUI-related work in a dedicated thread. For instructions
				 * on using this class, see
				 * {@link <a  href="http://download.oracle.com/javase/tutorial/uiswing/misc/index.html"> Swing tutorial </a>}
				 *\s
				 * @see <a
				 *      href="http://gee.cs.oswego.edu/dl/classes/EDU/oswego/cs/dl/util/concurrent/intro.html">
				 *      EDU.oswego.cs.dl.util.concurrent </a>
				 * @see <a
				 *      href="http://download.oracle.com/javase/6/docs/api/java/util/concurrent/package-summary.html">
				 *      JDK 6.0 </a>
				 * @author {@link <a href="http://gee.cs.oswego.edu/dl">Doug Lea</a>}
				 * @author {@link <a href="http://home.pacbell.net/jfai">J?rgen Failenschmid</a>}
				  *
				  * It is assumed that you have read the introductory document
				  * {@link <a HREF="../../../../../internat/overview.htm">
				  * Internationalization</a>}
				  * and are familiar with\s
				 */
				public class X {
				
				}
				"""
		}
	);
}
// URL Link references
public void testBug76324url() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					/**
					 * Invalid inline URL link references\s
					 *
					 * {@link <}
					 * {@link <a}
					 * {@link <a hre}
					 * {@link <a href}
					 * {@link <a href=}
					 * {@link <a href="}
					 * {@link <a href="invalid}
					 * {@link <a href="invalid"}
					 * {@link <a href="invalid">}
					 * {@link <a href="invalid">invalid}
					 * {@link <a href="invalid">invalid<}
					 * {@link <a href="invalid">invalid</}
					 * {@link <a href="invalid">invalid</a}
					 * {@link <a href="invalid">invalid</a> no text allowed after}
					 */
					public void s_foo() {
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				* {@link <}
				         ^^
			Javadoc: Malformed link reference
			----------
			2. ERROR in X.java (at line 6)
				* {@link <a}
				         ^^^
			Javadoc: Malformed link reference
			----------
			3. ERROR in X.java (at line 7)
				* {@link <a hre}
				         ^^^^^^^
			Javadoc: Malformed link reference
			----------
			4. ERROR in X.java (at line 8)
				* {@link <a href}
				         ^^^^^^^^
			Javadoc: Malformed link reference
			----------
			5. ERROR in X.java (at line 9)
				* {@link <a href=}
				         ^^^^^^^^^
			Javadoc: Malformed link reference
			----------
			6. ERROR in X.java (at line 10)
				* {@link <a href="}
				         ^^^^^^^^^^
			Javadoc: Malformed link reference
			----------
			7. ERROR in X.java (at line 11)
				* {@link <a href="invalid}
				         ^^^^^^^^^^^^^^^^^
			Javadoc: Malformed link reference
			----------
			8. ERROR in X.java (at line 12)
				* {@link <a href="invalid"}
				         ^^^^^^^^^^^^^^^^^^
			Javadoc: Malformed link reference
			----------
			9. ERROR in X.java (at line 13)
				* {@link <a href="invalid">}
				         ^^^^^^^^^^^^^^^^^^^
			Javadoc: Malformed link reference
			----------
			10. ERROR in X.java (at line 14)
				* {@link <a href="invalid">invalid}
				         ^^^^^^^^^^^^^^^^^^^^^^^^^^
			Javadoc: Malformed link reference
			----------
			11. ERROR in X.java (at line 15)
				* {@link <a href="invalid">invalid<}
				                                  ^^
			Javadoc: Malformed link reference
			----------
			12. ERROR in X.java (at line 16)
				* {@link <a href="invalid">invalid</}
				                                  ^^^
			Javadoc: Malformed link reference
			----------
			13. ERROR in X.java (at line 17)
				* {@link <a href="invalid">invalid</a}
				                                  ^^^^
			Javadoc: Malformed link reference
			----------
			14. ERROR in X.java (at line 18)
				* {@link <a href="invalid">invalid</a> no text allowed after}
				                                   ^^^^^^^^^^^^^^^^^^^^^^^^^^
			Javadoc: Unexpected text
			----------
			""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
// String references
public void testBug76324string() {
	runNegativeTest(
		new String[] {
		"X.java",
		"""
			public class X {
				/**
				 * Inline string references\s
				 *
				 * {@link "}
				 * {@link "unterminated string}
				 * {@link "invalid string""}
				 * {@link "valid string"}
				 * {@link "invalid" no text allowed after the string}
				 */
				public void s_foo() {
				}
			}
			""" },
		"""
			----------
			1. ERROR in X.java (at line 5)
				* {@link "}
				         ^^
			Javadoc: Invalid reference
			----------
			2. ERROR in X.java (at line 6)
				* {@link "unterminated string}
				         ^^^^^^^^^^^^^^^^^^^^^
			Javadoc: Invalid reference
			----------
			3. ERROR in X.java (at line 7)
				* {@link "invalid string""}
				                         ^^
			Javadoc: Unexpected text
			----------
			4. ERROR in X.java (at line 9)
				* {@link "invalid" no text allowed after the string}
				                  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Javadoc: Unexpected text
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 77510: [javadoc] compiler wrongly report deprecation when option "process javadoc comments" is not set
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=77510">77510</a>
 */
public void testBug77510enabled() {
	runNegativeTest(
		new String[] {
			"A.java",
			"""
				public class A {
					/** \\u0009 @deprecated */
					static int i0009;
					/** \\u000a @deprecated */
					static int i000a;
					/** \\u000b @deprecated */
					static int i000b;
					/** \\u000c @deprecated */
					static int i000c;
					/** \\u001c @deprecated */
					static int i001c;
					/** \\u001d @deprecated */
					static int i001d;
					/** \\u001e @deprecated */
					static int i001e;
					/** \\u001f @deprecated */
					static int i001f;
					/** \\u2007 @deprecated */
					static int i2007;
					/** \\u202f @deprecated */
					static int i202f;
				}
				""",
			"X.java",
			"""
				public class X {
					int i0 = A.i0009;
					int i1 = A.i000a;
					int i2 = A.i000b;
					int i3 = A.i000c;
					int i4 = A.i001c;
					int i5 = A.i001d;
					int i6 = A.i001e;
					int i7 = A.i001f;
					int i8 = A.i2007;
					int i9 = A.i202f;
				}
				""" },
		"""
			----------
			1. ERROR in X.java (at line 2)
				int i0 = A.i0009;
				           ^^^^^
			The field A.i0009 is deprecated
			----------
			2. ERROR in X.java (at line 3)
				int i1 = A.i000a;
				           ^^^^^
			The field A.i000a is deprecated
			----------
			3. ERROR in X.java (at line 5)
				int i3 = A.i000c;
				           ^^^^^
			The field A.i000c is deprecated
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug77510disabled() {
	this.docCommentSupport = CompilerOptions.IGNORE;
	runNegativeTest(
		new String[] {
			"A.java",
			"""
				public class A {
					/** \\u0009 @deprecated */
					static int i0009;
					/** \\u000a @deprecated */
					static int i000a;
					/** \\u000b @deprecated */
					static int i000b;
					/** \\u000c @deprecated */
					static int i000c;
					/** \\u001c @deprecated */
					static int i001c;
					/** \\u001d @deprecated */
					static int i001d;
					/** \\u001e @deprecated */
					static int i001e;
					/** \\u001f @deprecated */
					static int i001f;
					/** \\u2007 @deprecated */
					static int i2007;
					/** \\u202f @deprecated */
					static int i202f;
				}
				""",
			"X.java",
			"""
				public class X {
					int i0 = A.i0009;
					int i1 = A.i000a;
					int i2 = A.i000b;
					int i3 = A.i000c;
					int i4 = A.i001c;
					int i5 = A.i001d;
					int i6 = A.i001e;
					int i7 = A.i001f;
					int i8 = A.i2007;
					int i9 = A.i202f;
				}
				""" },
		"""
			----------
			1. ERROR in X.java (at line 2)
				int i0 = A.i0009;
				           ^^^^^
			The field A.i0009 is deprecated
			----------
			2. ERROR in X.java (at line 3)
				int i1 = A.i000a;
				           ^^^^^
			The field A.i000a is deprecated
			----------
			3. ERROR in X.java (at line 5)
				int i3 = A.i000c;
				           ^^^^^
			The field A.i000c is deprecated
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Test bug 77260: [Javadoc] deprecation warning should not be reported when @deprecated tag is set
 */
public void testBug77260() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				/** @deprecated */
				public class X {
					public int x;
					public void foo() {}
				}
				""",
			"Y.java",
			"""
				/**
				 * @see X
				 * @deprecated
				 */
				public class Y {
					/** @see X#x */
					public int x;
					/** @see X#foo() */
					public void foo() {}
				}
				""",
			"Z.java",
			"""
				public class Z {
					/**\s
					 * @see X#x
					 * @deprecated
					 */
					public int x;
					/**
					 * @see X#foo()
					 * @deprecated
					 */
					public void foo() {}
				}
				""" }
	);
}
public void testBug77260nested() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportDeprecationInDeprecatedCode, CompilerOptions.ENABLED);
	runNegativeTest(
		true,
		new String[] {
			"X.java",
			"""
				/** @deprecated */
				public class X {
					public int x;
					public void foo() {}
				}
				""",
			"Y.java",
			"""
				/**
				 * @see X
				 * @deprecated
				 */
				public class Y {
					/** @see X#x */
					public int x;
					/** @see X#foo() */
					public void foo() {}
				}
				""",
			"Z.java",
			"""
				public class Z {
					/**\s
					 * @see X#x
					 * @deprecated
					 */
					public int x;
					/**
					 * @see X#foo()
					 * @deprecated
					 */
					public void foo() {}
				}
				""" },
		null,
		options,
		"""
			----------
			1. ERROR in Y.java (at line 2)
				* @see X
				       ^
			Javadoc: The type X is deprecated
			----------
			2. ERROR in Y.java (at line 6)
				/** @see X#x */
				         ^
			Javadoc: The type X is deprecated
			----------
			3. ERROR in Y.java (at line 6)
				/** @see X#x */
				           ^
			Javadoc: The field X.x is deprecated
			----------
			4. ERROR in Y.java (at line 8)
				/** @see X#foo() */
				         ^
			Javadoc: The type X is deprecated
			----------
			5. ERROR in Y.java (at line 8)
				/** @see X#foo() */
				           ^^^^^
			Javadoc: The method foo() from the type X is deprecated
			----------
			----------
			1. ERROR in Z.java (at line 3)
				* @see X#x
				       ^
			Javadoc: The type X is deprecated
			----------
			2. ERROR in Z.java (at line 3)
				* @see X#x
				         ^
			Javadoc: The field X.x is deprecated
			----------
			3. ERROR in Z.java (at line 8)
				* @see X#foo()
				       ^
			Javadoc: The type X is deprecated
			----------
			4. ERROR in Z.java (at line 8)
				* @see X#foo()
				         ^^^^^
			Javadoc: The method foo() from the type X is deprecated
			----------
			""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug77260nested_disabled() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportDeprecationInDeprecatedCode, CompilerOptions.ENABLED);
	options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsDeprecatedRef, CompilerOptions.DISABLED);
	runConformTest(
		new String[] {
			"X.java",
			"""
				/** @deprecated */
				public class X {
					public int x;
					public void foo() {}
				}
				""",
			"Y.java",
			"""
				/**
				 * @see X
				 * @deprecated
				 */
				public class Y {
					/** @see X#x */
					public int x;
					/** @see X#foo() */
					public void foo() {}
				}
				""",
			"Z.java",
			"""
				public class Z {
					/**\s
					 * @see X#x
					 * @deprecated
					 */
					public int x;
					/**
					 * @see X#foo()
					 * @deprecated
					 */
					public void foo() {}
				}
				"""
		},
		"",
		null,
		true,
		null,
		options,
		null
	);
}

/**
 * Bug 77602: [javadoc] "Only consider members as visible as" is does not work for syntax error
 */
public void testBug77602() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  /**
				   * @see
				   * @see UnknownClass
				   */
				  protected void foo() {
				  }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				* @see
				   ^^^
			Javadoc: Missing reference
			----------
			2. ERROR in X.java (at line 4)
				* @see UnknownClass
				       ^^^^^^^^^^^^
			Javadoc: UnknownClass cannot be resolved to a type
			----------
			""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug77602_Public() {
	this.reportInvalidJavadocVisibility = CompilerOptions.PUBLIC;
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  /**
				   * @see
				   * @see UnknownClass
				   */
				  protected void foo() {
				  }
				}
				"""
		}
	);
}

/**
 * Bug 78091: [1.5][javadoc] Compiler should accept new 1.5 syntax for @param
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=78091">78091</a>
 */
public void testBug78091() {
	this.reportMissingJavadocTags = CompilerOptions.ERROR;
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					/**
					 * Valid type parameter reference
					 * @param xxx.yyy invalid
					 * @param obj(x) invalid
					 */
					public void foo(int xxx, Object obj) {}
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				* @param xxx.yyy invalid
				         ^^^^^^^
			Javadoc: Invalid param tag name
			----------
			2. ERROR in X.java (at line 5)
				* @param obj(x) invalid
				         ^^^^^^
			Javadoc: Invalid param tag name
			----------
			3. ERROR in X.java (at line 7)
				public void foo(int xxx, Object obj) {}
				                    ^^^
			Javadoc: Missing tag for parameter xxx
			----------
			4. ERROR in X.java (at line 7)
				public void foo(int xxx, Object obj) {}
				                                ^^^
			Javadoc: Missing tag for parameter obj
			----------
			""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 80910: [javadoc] Invalid missing reference warning on @see or @link tags
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=80910"
 */
public void testBug80910() {
	runNegativeTest(
		new String[] {
			"Test.java",
			"""
				public class Test {
					int field;
				
					/**
					 * @param key\'s toto
					 * @see #field
					 */
					public void foo(int x) {
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in Test.java (at line 5)
				* @param key\'s toto
				         ^^^^^
			Javadoc: Invalid param tag name
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 82088: [search][javadoc] Method parameter types references not found in @see/@link tags
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=82088"
 */
public void testBug82088() {
	runNegativeTest(
		new String[] {
			"Test.java",
			"""
				public class Test {
					int field;
				
					/**
					 * @param key\'s toto
					 * @see #field
					 */
					public void foo(int x) {
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in Test.java (at line 5)
				* @param key\'s toto
				         ^^^^^
			Javadoc: Invalid param tag name
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 83285: [javadoc] Javadoc reference to constructor of secondary type has no binding / not found by search
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=83285"
 */
public void testBug83285a() {
	runConformTest(
		new String[] {
			"p/A.java",
			"""
				package p;
				class A { }
				class C {
				    /**
				     * Link {@link #C(String)} was also wrongly warned...
				     */
				    private String fGerman;
				    public C(String german) {
				        fGerman = german;
				    }
				}"""
		}
	);
}
public void testBug83285b() {
	runConformTest(
		new String[] {
			"p/A.java",
			"""
				package p;
				class A {
					A(char c) {}
				}
				class B {
					B(Exception ex) {}
					void foo() {}\s
					class C {\s
					    /**
					     * Link {@link #B(Exception)} OK
					     * Link {@link #B.C(String)} OK
					     * Link {@link #foo()} OK
					     * Link {@link #bar()} OK
					     */
					    public C(String str) {}
						void bar() {}
					}
				}"""
		}
	);
}
public void testBug83285c() {
	runNegativeTest(
		new String[] {
			"p/A.java",
			"""
				package p;
				class A {
					A(char c) {}
				}
				class B {
					B(Exception ex) {}
					void foo() {}
					class C {\s
					    /**
					     * Link {@link #A(char)} KO
					     * Link {@link #B(char)}  KO
					     * Link {@link #C(char)} KO
					     * Link {@link #foo(int)} KO
					     * Link {@link #bar(int)} KO
					     */
					    public C(String str) {}
						void bar() {}
					}
				}"""
		},
		"""
			----------
			1. ERROR in p\\A.java (at line 10)
				* Link {@link #A(char)} KO
				               ^
			Javadoc: The method A(char) is undefined for the type B.C
			----------
			2. ERROR in p\\A.java (at line 11)
				* Link {@link #B(char)}  KO
				               ^
			Javadoc: The method B(char) is undefined for the type B.C
			----------
			3. ERROR in p\\A.java (at line 12)
				* Link {@link #C(char)} KO
				               ^^^^^^^
			Javadoc: The constructor B.C(char) is undefined
			----------
			4. ERROR in p\\A.java (at line 13)
				* Link {@link #foo(int)} KO
				               ^^^
			Javadoc: The method foo(int) is undefined for the type B.C
			----------
			5. ERROR in p\\A.java (at line 14)
				* Link {@link #bar(int)} KO
				               ^^^
			Javadoc: The method bar() in the type B.C is not applicable for the arguments (int)
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 86769: [javadoc] Warn/Error for 'Missing javadoc comments' doesn't recognize private inner classes
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=86769"
 */
public void testBug86769_Classes1() {
	this.reportMissingJavadocComments = CompilerOptions.ERROR;
	this.reportMissingJavadocCommentsVisibility = CompilerOptions.PROTECTED;
	runNegativeTest(
		new String[] {
			"A.java",
			"""
				/**
				 * Test bug 86769\s
				 */
				public class A {
					private class Level1Private {
						private class Level2_PrivPriv {}
						class Level2_PrivDef {}
						protected class Level2_PrivPro {}
						public class Level2_PrivPub {}
					}
					class Level1Default{
						private class Level2_DefPriv {}
						class Level2_DefDef {}
						protected class Level2_DefPro {}
						public class Level2_DefPub {}
					}
					protected class Level1Protected {
						private class Level2_ProtPriv {}
						class Level2_ProDef {}
						protected class Level2_ProPro {}
						public class Level2_ProPub {}\s
					}
					public class Level1Public {
						private class Level2_PubPriv {}
						class Level2_PubDef {}
						protected class Level2_PubPro {}
						public class Level2_PubPub {}
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in A.java (at line 17)
				protected class Level1Protected {
				                ^^^^^^^^^^^^^^^
			Javadoc: Missing comment for protected declaration
			----------
			2. ERROR in A.java (at line 20)
				protected class Level2_ProPro {}
				                ^^^^^^^^^^^^^
			Javadoc: Missing comment for protected declaration
			----------
			3. ERROR in A.java (at line 21)
				public class Level2_ProPub {}\s
				             ^^^^^^^^^^^^^
			Javadoc: Missing comment for protected declaration
			----------
			4. ERROR in A.java (at line 23)
				public class Level1Public {
				             ^^^^^^^^^^^^
			Javadoc: Missing comment for public declaration
			----------
			5. ERROR in A.java (at line 26)
				protected class Level2_PubPro {}
				                ^^^^^^^^^^^^^
			Javadoc: Missing comment for protected declaration
			----------
			6. ERROR in A.java (at line 27)
				public class Level2_PubPub {}
				             ^^^^^^^^^^^^^
			Javadoc: Missing comment for public declaration
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug86769_Classes2() {
	this.reportMissingJavadocComments = CompilerOptions.ERROR;
	this.reportMissingJavadocCommentsVisibility = CompilerOptions.DEFAULT;
	runNegativeTest(
		new String[] {
			"B.java",
			"""
				/**
				 * Test bug 86769
				 */
				public class B {
					class Level0_Default {
						private class Level1Private {
							private class Level2_PrivPriv {}
							class Level2_PrivDef {}
							protected class Level2_PrivPro {}
							public class Level2_PrivPub {}
						}
					}
					public class Level0_Public {
						class Level1Default{
							private class Level2_DefPriv {}
							class Level2_DefDef {}
							protected class Level2_DefPro {}
							public class Level2_DefPub {}
						}
					}
					protected class Level0_Protected {
						protected class Level1Protected {
							private class Level2_ProtPriv {}
							class Level2_ProDef {}
							protected class Level2_ProPro {}
							public class Level2_ProPub {}\s
						}
					}
					private class Level0_Private {
						public class Level1Public {
							private class Level2_PubPriv {}
							class Level2_PubDef {}
							protected class Level2_PubPro {}
							public class Level2_PubPub {}
						}
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in B.java (at line 5)
				class Level0_Default {
				      ^^^^^^^^^^^^^^
			Javadoc: Missing comment for default declaration
			----------
			2. ERROR in B.java (at line 13)
				public class Level0_Public {
				             ^^^^^^^^^^^^^
			Javadoc: Missing comment for public declaration
			----------
			3. ERROR in B.java (at line 14)
				class Level1Default{
				      ^^^^^^^^^^^^^
			Javadoc: Missing comment for default declaration
			----------
			4. ERROR in B.java (at line 16)
				class Level2_DefDef {}
				      ^^^^^^^^^^^^^
			Javadoc: Missing comment for default declaration
			----------
			5. ERROR in B.java (at line 17)
				protected class Level2_DefPro {}
				                ^^^^^^^^^^^^^
			Javadoc: Missing comment for default declaration
			----------
			6. ERROR in B.java (at line 18)
				public class Level2_DefPub {}
				             ^^^^^^^^^^^^^
			Javadoc: Missing comment for default declaration
			----------
			7. ERROR in B.java (at line 21)
				protected class Level0_Protected {
				                ^^^^^^^^^^^^^^^^
			Javadoc: Missing comment for protected declaration
			----------
			8. ERROR in B.java (at line 22)
				protected class Level1Protected {
				                ^^^^^^^^^^^^^^^
			Javadoc: Missing comment for protected declaration
			----------
			9. ERROR in B.java (at line 24)
				class Level2_ProDef {}
				      ^^^^^^^^^^^^^
			Javadoc: Missing comment for default declaration
			----------
			10. ERROR in B.java (at line 25)
				protected class Level2_ProPro {}
				                ^^^^^^^^^^^^^
			Javadoc: Missing comment for protected declaration
			----------
			11. ERROR in B.java (at line 26)
				public class Level2_ProPub {}\s
				             ^^^^^^^^^^^^^
			Javadoc: Missing comment for protected declaration
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug86769_Field1() {
	this.reportMissingJavadocComments = CompilerOptions.ERROR;
	this.reportMissingJavadocCommentsVisibility = CompilerOptions.PUBLIC;
	runNegativeTest(
		new String[] {
			"A.java",
			"""
				/**
				 * Test bug 86769
				 */
				public class A {
					private class InnerPrivate {
						private int pri_pri;
						int pri_def;
						protected int pri_pro;
						public int pri_pub;
					}
					class InnerDefault{
						private int def_pri;
						int def_def;
						protected int def_pro;
						public int def_pub;
					}
					protected class InnerProtected {
						private int pro_pri;
						int pro_def;
						protected int pro_pro;
						public int pro_pub;\s
					}
					public class InnerPublic {
						private int pub_pri;
						int pub_def;
						protected int pub_pro;
						public int pub_pub;
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in A.java (at line 23)
				public class InnerPublic {
				             ^^^^^^^^^^^
			Javadoc: Missing comment for public declaration
			----------
			2. ERROR in A.java (at line 27)
				public int pub_pub;
				           ^^^^^^^
			Javadoc: Missing comment for public declaration
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug86769_Fields2() {
	this.reportMissingJavadocComments = CompilerOptions.ERROR;
	this.reportMissingJavadocCommentsVisibility = CompilerOptions.PRIVATE;
	runNegativeTest(
		new String[] {
			"B.java",
			"""
				/**
				 * Test bug 86769
				 */
				public class B {
					private class Level1 {
						private class InnerPrivate {
							private int pri_pri;
							int pri_def;
							protected int pri_pro;
							public int pri_pub;
						}
						class InnerDefault{
							private int def_pri;
							int def_def;
							protected int def_pro;
							public int def_pub;
						}
						protected class InnerProtected {
							private int pro_pri;
							int pro_def;
							protected int pro_pro;
							public int pro_pub;\s
						}
						public class InnerPublic {
							private int pub_pri;
							int pub_def;
							protected int pub_pro;
							public int pub_pub;
						}
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in B.java (at line 5)
				private class Level1 {
				              ^^^^^^
			Javadoc: Missing comment for private declaration
			----------
			2. ERROR in B.java (at line 6)
				private class InnerPrivate {
				              ^^^^^^^^^^^^
			Javadoc: Missing comment for private declaration
			----------
			3. ERROR in B.java (at line 7)
				private int pri_pri;
				            ^^^^^^^
			Javadoc: Missing comment for private declaration
			----------
			4. ERROR in B.java (at line 8)
				int pri_def;
				    ^^^^^^^
			Javadoc: Missing comment for private declaration
			----------
			5. ERROR in B.java (at line 9)
				protected int pri_pro;
				              ^^^^^^^
			Javadoc: Missing comment for private declaration
			----------
			6. ERROR in B.java (at line 10)
				public int pri_pub;
				           ^^^^^^^
			Javadoc: Missing comment for private declaration
			----------
			7. ERROR in B.java (at line 12)
				class InnerDefault{
				      ^^^^^^^^^^^^
			Javadoc: Missing comment for private declaration
			----------
			8. ERROR in B.java (at line 13)
				private int def_pri;
				            ^^^^^^^
			Javadoc: Missing comment for private declaration
			----------
			9. ERROR in B.java (at line 14)
				int def_def;
				    ^^^^^^^
			Javadoc: Missing comment for private declaration
			----------
			10. ERROR in B.java (at line 15)
				protected int def_pro;
				              ^^^^^^^
			Javadoc: Missing comment for private declaration
			----------
			11. ERROR in B.java (at line 16)
				public int def_pub;
				           ^^^^^^^
			Javadoc: Missing comment for private declaration
			----------
			12. ERROR in B.java (at line 18)
				protected class InnerProtected {
				                ^^^^^^^^^^^^^^
			Javadoc: Missing comment for private declaration
			----------
			13. ERROR in B.java (at line 19)
				private int pro_pri;
				            ^^^^^^^
			Javadoc: Missing comment for private declaration
			----------
			14. ERROR in B.java (at line 20)
				int pro_def;
				    ^^^^^^^
			Javadoc: Missing comment for private declaration
			----------
			15. ERROR in B.java (at line 21)
				protected int pro_pro;
				              ^^^^^^^
			Javadoc: Missing comment for private declaration
			----------
			16. ERROR in B.java (at line 22)
				public int pro_pub;\s
				           ^^^^^^^
			Javadoc: Missing comment for private declaration
			----------
			17. ERROR in B.java (at line 24)
				public class InnerPublic {
				             ^^^^^^^^^^^
			Javadoc: Missing comment for private declaration
			----------
			18. ERROR in B.java (at line 25)
				private int pub_pri;
				            ^^^^^^^
			Javadoc: Missing comment for private declaration
			----------
			19. ERROR in B.java (at line 26)
				int pub_def;
				    ^^^^^^^
			Javadoc: Missing comment for private declaration
			----------
			20. ERROR in B.java (at line 27)
				protected int pub_pro;
				              ^^^^^^^
			Javadoc: Missing comment for private declaration
			----------
			21. ERROR in B.java (at line 28)
				public int pub_pub;
				           ^^^^^^^
			Javadoc: Missing comment for private declaration
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug86769_Metthods1() {
	this.reportMissingJavadocComments = CompilerOptions.ERROR;
	this.reportMissingJavadocCommentsVisibility = CompilerOptions.PUBLIC;
	runNegativeTest(
		new String[] {
			"A.java",
			"""
				/**
				 * Test bug 86769
				 */
				public class A {
					private class InnerPrivate {
						private void pri_pri() {}
						void pri_def() {}
						protected void pri_pro() {}
						public void pri_pub() {}
					}
					class InnerDefault{
						private void def_pri() {}
						void def_def() {}
						protected void def_pro() {}
						public void def_pub() {}
					}
					protected class InnerProtected {
						private void pro_pri() {}
						void pro_def() {}
						protected void pro_pro() {}
						public void pro_pub() {}\s
					}
					public class InnerPublic {
						private void pub_pri() {}
						void pub_def() {}
						protected void pub_pro() {}
						public void pub_pub() {}
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in A.java (at line 23)
				public class InnerPublic {
				             ^^^^^^^^^^^
			Javadoc: Missing comment for public declaration
			----------
			2. ERROR in A.java (at line 27)
				public void pub_pub() {}
				            ^^^^^^^^^
			Javadoc: Missing comment for public declaration
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug86769_Methods2() {
	this.reportMissingJavadocComments = CompilerOptions.ERROR;
	this.reportMissingJavadocCommentsVisibility = CompilerOptions.PROTECTED;
	runConformTest(
		new String[] {
			"B.java",
			"""
				/**
				 * Test bug 86769
				 */
				public class B {
					private class Level1 {
						private class InnerPrivate {
							private void pri_pri() {}
							void pri_def() {}
							protected void pri_pro() {}
							public void pri_pub() {}
						}
						class InnerDefault{
							private void def_pri() {}
							void def_def() {}
							protected void def_pro() {}
							public void def_pub() {}
						}
						protected class InnerProtected {
							private void pro_pri() {}
							void pro_def() {}
							protected void pro_pro() {}
							public void pro_pub() {}\s
						}
						public class InnerPublic {
							private void pub_pri() {}
							void pub_def() {}
							protected void pub_pro() {}
							public void pub_pub() {}
						}
					}
				}
				"""
		}
	);
}

/**
 * Bug 87404: [javadoc] Unexpected not defined warning on constructor
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=87404"
 */
public void testBug87404() {
	runConformTest(
		new String[] {
			"p/A.java",
			"""
				package p;
				class A {
					A(char c) {}
					class B {
						B(Exception ex) {}
					}
					void foo() {}
				    /**
				     * Link {@link #A(char)} OK\s
				     * Link {@link #A(String)} OK
				     * Link {@link #foo()} OK
				     * Link {@link #bar()} OK
				     */
				    public A(String str) {}
					void bar() {}
				}"""
		}
	);
}

/**
 * Bug 90302: [javadoc] {&#064;inheritDoc} should be inactive for non-overridden method
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=90302"
 */
public void testBug90302() {
	this.reportMissingJavadocTags = CompilerOptions.ERROR;
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				/**
				 * @see #foo(String)
				 */
				public class X {
					/**
					 * Static method
					 * @param str
					 * @return int
					 * @throws NumberFormatException
					 */
					static int foo(String str) throws NumberFormatException{
						return Integer.parseInt(str);
					}
				}
				""",
			"Y.java",
			"""
				/**
				 * @see #foo(String)
				 */
				public class Y extends X {\s
					/**
					 * Static method: does not override super
					 * {@inheritDoc}
					 */
					static int foo(String str) throws NumberFormatException{
						return Integer.parseInt(str);
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in Y.java (at line 7)
				* {@inheritDoc}
				    ^^^^^^^^^^
			Javadoc: Unexpected tag
			----------
			2. ERROR in Y.java (at line 9)
				static int foo(String str) throws NumberFormatException{
				       ^^^
			Javadoc: Missing tag for return type
			----------
			3. ERROR in Y.java (at line 9)
				static int foo(String str) throws NumberFormatException{
				                      ^^^
			Javadoc: Missing tag for parameter str
			----------
			4. ERROR in Y.java (at line 9)
				static int foo(String str) throws NumberFormatException{
				                                  ^^^^^^^^^^^^^^^^^^^^^
			Javadoc: Missing tag for declared exception NumberFormatException
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug90302b() {
	this.reportMissingJavadocTags = CompilerOptions.ERROR;
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				/** */
				public class X {
				}
				""",
			"Y.java",
			"""
				/**
				 * @see #foo(String)
				 */
				public class Y extends X {\s
					/**
					 * Simple method: does not override super
					 * {@inheritDoc}
					 */
					static int foo(String str) throws NumberFormatException{
						return Integer.parseInt(str);
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in Y.java (at line 7)
				* {@inheritDoc}
				    ^^^^^^^^^^
			Javadoc: Unexpected tag
			----------
			2. ERROR in Y.java (at line 9)
				static int foo(String str) throws NumberFormatException{
				       ^^^
			Javadoc: Missing tag for return type
			----------
			3. ERROR in Y.java (at line 9)
				static int foo(String str) throws NumberFormatException{
				                      ^^^
			Javadoc: Missing tag for parameter str
			----------
			4. ERROR in Y.java (at line 9)
				static int foo(String str) throws NumberFormatException{
				                                  ^^^^^^^^^^^^^^^^^^^^^
			Javadoc: Missing tag for declared exception NumberFormatException
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 103304: [Javadoc] Wrong reference proposal for inner classes.
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=103304"
 */
public void testBug103304a_public() {
	String[] units = new String[] {
			"boden/IAFAState.java",
			"""
				package boden;
				public interface IAFAState {
				    public class ValidationException extends Exception {
				        public ValidationException(String variableName, IAFAState subformula) {
				            super("Variable \'"+variableName+"\' may be unbound in \'"+subformula+"\'");
				        }
				        public void method() {}
				    }
				    /**
				     * Validates a formula for consistent bindings. Bindings are consistent, when at each point in time,
				     * the set of povided variables can be guaranteed to be a superset of the set of required variables.
				     * @throws ValidationException Thrown if a variable is unbound.\s
				     * @see ValidationException#IAFAState.ValidationException(String, IAFAState)
				     * @see IAFAState.ValidationException#method()
				     * @see ValidationException
				     * {@link ValidationException}
				     */
				    public void validate() throws ValidationException;
				}
				""",
			"boden/TestValid.java",
			"""
				package boden;
				import boden.IAFAState.ValidationException;
				/**
				 * @see ValidationException
				 * @see IAFAState.ValidationException
				 */
				public class TestValid {
					/** \s
					 * @see ValidationException#IAFAState.ValidationException(String, IAFAState)
					 */
					IAFAState.ValidationException valid1;
					/**
					 * @see IAFAState.ValidationException#IAFAState.ValidationException(String, IAFAState)
					 */
					IAFAState.ValidationException valid2;
				}
				"""
		};
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		runNegativeTest(units,
			"""
				----------
				1. ERROR in boden\\TestValid.java (at line 4)
					* @see ValidationException
					       ^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid member type qualification
				----------
				2. ERROR in boden\\TestValid.java (at line 9)
					* @see ValidationException#IAFAState.ValidationException(String, IAFAState)
					       ^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid member type qualification
				----------
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	} else {
		runConformTest(units);
	}
}

public void testBug103304a_private() {
	String[] units = new String[] {
			"boden/IAFAState.java",
			"""
				package boden;
				public interface IAFAState {
				    public class ValidationException extends Exception {
				        public ValidationException(String variableName, IAFAState subformula) {
				            super("Variable \'"+variableName+"\' may be unbound in \'"+subformula+"\'");
				        }
				        public void method() {}
				    }
				    /**
				     * Validates a formula for consistent bindings. Bindings are consistent, when at each point in time,
				     * the set of povided variables can be guaranteed to be a superset of the set of required variables.
				     * @throws ValidationException Thrown if a variable is unbound.\s
				     * @see ValidationException#IAFAState.ValidationException(String, IAFAState)
				     * @see IAFAState.ValidationException#method()
				     * @see ValidationException
				     * {@link ValidationException}
				     */
				    public void validate() throws ValidationException;
				}
				""",
			"boden/TestValid.java",
			"""
				package boden;
				import boden.IAFAState.ValidationException;
				/**
				 * @see ValidationException
				 * @see IAFAState.ValidationException
				 */
				public class TestValid {
					/** \s
					 * @see ValidationException#IAFAState.ValidationException(String, IAFAState)
					 */
					IAFAState.ValidationException valid1;
					/**
					 * @see IAFAState.ValidationException#IAFAState.ValidationException(String, IAFAState)
					 */
					IAFAState.ValidationException valid2;
				}
				"""
		};
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		runNegativeTest(units,
			"""
				----------
				1. ERROR in boden\\TestValid.java (at line 4)
					* @see ValidationException
					       ^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid member type qualification
				----------
				2. ERROR in boden\\TestValid.java (at line 9)
					* @see ValidationException#IAFAState.ValidationException(String, IAFAState)
					       ^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid member type qualification
				----------
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	} else {
		runConformTest(units);
	}
}

public void testBug103304b() {
	this.reportInvalidJavadocVisibility = CompilerOptions.PRIVATE;
	String[] units = new String[] {
			"boden/IAFAState.java",
			"""
				package boden;
				public interface IAFAState {
				    public class ValidationException extends Exception {
				        public ValidationException(String variableName, IAFAState subformula) {
				            super("Variable \'"+variableName+"\' may be unbound in \'"+subformula+"\'");
				        }
				        public void method() {}
				    }
				}
				""",
			"boden/TestInvalid1.java",
			"""
				package boden;
				import boden.IAFAState.ValidationException;
				public class TestInvalid1 {
					/**\s
					 * @see ValidationException#ValidationException(String, IAFAState)
					 * @see ValidationException#IAFAState.ValidationException(String, IAFAState)
					 */\s
					IAFAState.ValidationException invalid;
				}
				""",
			"boden/TestInvalid2.java",
			"""
				package boden;
				public class TestInvalid2 {
					/**
					 * @see IAFAState.ValidationException#ValidationException(String, IAFAState)
					 */
					IAFAState.ValidationException invalid;
				}
				""",
			"boden/TestInvalid3.java",
			"""
				package boden;
				import boden.IAFAState.ValidationException;
				public class TestInvalid3 {
					/**
					 * @see IAFAState.ValidationException#IAFA.State.ValidationException(String, IAFAState)
					 */
					IAFAState.ValidationException invalid;
				}
				""",
			"boden/TestInvalid4.java",
			"""
				package boden;
				import boden.IAFAState.ValidationException;
				public class TestInvalid4 {
					/**
					 * @see IAFAState.ValidationException#IAFAState .ValidationException(String, IAFAState)
					 */
					IAFAState.ValidationException invalid;
				}
				"""
		};
	String errors_14 = new String (
			"""
				----------
				1. ERROR in boden\\TestInvalid1.java (at line 5)
					* @see ValidationException#ValidationException(String, IAFAState)
					       ^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid member type qualification
				----------
				2. ERROR in boden\\TestInvalid1.java (at line 6)
					* @see ValidationException#IAFAState.ValidationException(String, IAFAState)
					       ^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid member type qualification
				----------
				----------
				1. ERROR in boden\\TestInvalid3.java (at line 2)
					import boden.IAFAState.ValidationException;
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				The import boden.IAFAState.ValidationException is never used
				----------
				2. ERROR in boden\\TestInvalid3.java (at line 5)
					* @see IAFAState.ValidationException#IAFA.State.ValidationException(String, IAFAState)
					                                     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid member type qualification
				----------
				----------
				1. ERROR in boden\\TestInvalid4.java (at line 2)
					import boden.IAFAState.ValidationException;
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				The import boden.IAFAState.ValidationException is never used
				----------
				2. ERROR in boden\\TestInvalid4.java (at line 5)
					* @see IAFAState.ValidationException#IAFAState .ValidationException(String, IAFAState)
					                                     ^^^^^^^^^
				Javadoc: IAFAState cannot be resolved or is not a field
				----------
				"""
	);
	String errors_50 = new String (
			"""
				----------
				1. ERROR in boden\\TestInvalid3.java (at line 2)
					import boden.IAFAState.ValidationException;
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				The import boden.IAFAState.ValidationException is never used
				----------
				2. ERROR in boden\\TestInvalid3.java (at line 5)
					* @see IAFAState.ValidationException#IAFA.State.ValidationException(String, IAFAState)
					                                     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid member type qualification
				----------
				----------
				1. ERROR in boden\\TestInvalid4.java (at line 2)
					import boden.IAFAState.ValidationException;
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				The import boden.IAFAState.ValidationException is never used
				----------
				2. ERROR in boden\\TestInvalid4.java (at line 5)
					* @see IAFAState.ValidationException#IAFAState .ValidationException(String, IAFAState)
					                                     ^^^^^^^^^
				Javadoc: IAFAState cannot be resolved or is not a field
				----------
				"""
	);
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		runNegativeTest(units, errors_14);
	} else {
		runNegativeTest(units, errors_50, JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}

}
public void testBug103304c() {
	runConformTest(
		new String[] {
			"test/Test.java",
			"""
				package test;
				public interface Test {
					public class Level0 {
						public Level0() {}
					}
					public interface Member {
						public class Level1 {
							public Level1() {}
						}
					}
				}
				""",
			"test/C.java",
			"""
				package test;
				public class C {
					/**
					 * @see Test.Level0#Test.Level0()
					 */
					Test.Level0 valid = new Test.Level0();
					/**
					 * @see Test.Level0#Level0()
					 */
					Test.Level0 invalid = new Test.Level0();
				}
				"""
		}
		//test\C.java:10: warning - Tag @see: can't find Level0() in test.Test.Level0 => bug ID: 4288720
	);
}
public void testBug103304d() {
	runNegativeTest(
		new String[] {
			"test/Test.java",
			"""
				package test;
				public interface Test {
					public class Level0 {
						public Level0() {}
					}
					public interface Member {
						public class Level1 {
							public Level1() {}
						}
					}
				}
				""",
			"test/C2.java",
			"""
				package test;
				public class C2 {
					/**
					 * @see Test.Member.Level1#Test.Member.Level1()
					 */
					Test.Member.Level1 valid = new Test.Member.Level1();
					/**
					 * @see Test.Member.Level1#Level1()
					 */
					Test.Member.Level1 invalid = new Test.Member.Level1();
					/**
					 * @see Test.Member.Level1#Test.Level1()
					 */
					Test.Member.Level1 wrong = new Test.Member.Level1();
				}
				"""
		},
		"""
			----------
			1. ERROR in test\\C2.java (at line 12)
				* @see Test.Member.Level1#Test.Level1()
				                          ^^^^^^^^^^^^^
			Javadoc: Invalid member type qualification
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug103304e() {
	runConformTest(
		new String[] {
			"implicit/Valid.java",
			"""
				package implicit;
				public interface Valid {
					public class Level0 {
						/**
						 * @see #Valid.Level0() Valid
						 */
						public Level0() {}
						/**
						 * @see #Valid.Level0(String) Valid
						 */
						public Level0(String str) {}
					}
					public interface Member {
						public class Level1 {
							/**
							 * @see #Valid.Member.Level1() Valid
							 */
							public Level1() {}
							/**
							 * @see #Valid.Member.Level1(int) Valid
							 */
							public Level1(int x) {}
						}
					}
				}
				"""
		}
	);
}
public void testBug103304f() {
	runNegativeTest(
		new String[] {
			"implicit/Invalid.java",
			"""
				package implicit;
				public interface Invalid {
					public class Level0 {
						/**
						 * @see #Level0() Invalid
						 */
						public Level0() {}
						/**
						 * @see #Level0(String) Invalid
						 */
						public Level0(String str) {}
					}
					public interface Member {
						public class Level1 {
							/**
							 * @see #Level1() Invalid
							 * @see #Member.Level1() Invalid
							 * @see #Invalid.Level1() Invalid
							 */
							public Level1() {}
							/**
							 * @see #Level1(int) Invalid
							 * @see #Invalid.Level1(int) Invalid
							 * @see #Member.Level1(int) Invalid
							 */
							public Level1(int x) {}
						}
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in implicit\\Invalid.java (at line 17)
				* @see #Member.Level1() Invalid
				        ^^^^^^^^^^^^^^^
			Javadoc: Invalid member type qualification
			----------
			2. ERROR in implicit\\Invalid.java (at line 18)
				* @see #Invalid.Level1() Invalid
				        ^^^^^^^^^^^^^^^^
			Javadoc: Invalid member type qualification
			----------
			3. ERROR in implicit\\Invalid.java (at line 23)
				* @see #Invalid.Level1(int) Invalid
				        ^^^^^^^^^^^^^^^^^^^
			Javadoc: Invalid member type qualification
			----------
			4. ERROR in implicit\\Invalid.java (at line 24)
				* @see #Member.Level1(int) Invalid
				        ^^^^^^^^^^^^^^^^^^
			Javadoc: Invalid member type qualification
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 116464: [javadoc] Unicode tag name are not correctly parsed
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=116464"
 */
public void testBug116464() {
	this.reportMissingJavadocTags = CompilerOptions.ERROR;
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					/**
					 * @\\u0070\\u0061\\u0072\\u0061\\u006d str xxx
					 */
					void foo(String str) {}
				}
				"""
		}
	);
}

/**
 * bug 125518: [javadoc] Embedding html in a link placed in a @see JavaDoc tag causes a warning
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=125518"
 */
public void testBug125518a() {
	String[] units = new String[] {
		"pkg/X.java",
		"""
			package pkg;
			
			public class X {
				/**
				 * @see <a href="ccwww.xyzzy.com/rfc123.html">invalid></
				 */
				public void foo() {\s
				\s
				}
			}
			"""
	};
	this.reportInvalidJavadoc = CompilerOptions.WARNING;
	runConformTest(true, units,
			"""
				----------
				1. WARNING in pkg\\X.java (at line 5)
					* @see <a href="ccwww.xyzzy.com/rfc123.html">invalid></
					                                                     ^^
				Javadoc: Malformed link reference
				----------
				""",
			null, null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
}

public void testBug125518b() {
	String[] units = new String[] {
		"pkg/X.java",
		"""
			package pkg;
			
			public class X {
				/**
				 * @see <a href="ccwww.xyzzy.com/rfc123.html">invalid></a
				 */
				public void foo() {\s
				\s
				}
			}
			"""
	};
	this.reportInvalidJavadoc = CompilerOptions.WARNING;
	runConformTest(true, units,
			"""
				----------
				1. WARNING in pkg\\X.java (at line 5)
					* @see <a href="ccwww.xyzzy.com/rfc123.html">invalid></a
					                                                     ^^^
				Javadoc: Malformed link reference
				----------
				""",
			null, null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
}

public void testBug125518c() {
	String[] units = new String[] {
		"pkg/X.java",
		"""
			package pkg;
			
			public class X {
				/**
				 * @see <a href="ccwww.xyzzy.com/rfc123.html">invalid></>
				 */
				public void foo() {\s
				\s
				}
			}
			"""
	};
	this.reportInvalidJavadoc = CompilerOptions.WARNING;
	runConformTest(true, units,
			"""
				----------
				1. WARNING in pkg\\X.java (at line 5)
					* @see <a href="ccwww.xyzzy.com/rfc123.html">invalid></>
					                                                     ^^^
				Javadoc: Malformed link reference
				----------
				""",
			null, null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
}

public void testBug125518d() {
	String[] units = new String[] {
		"pkg/X.java",
		"""
			package pkg;
			
			public class X {
				/**
				 * @see <a href="ccwww.xyzzy.com/rfc123.html">invalid></aa>
				 */
				public void foo() {\s
				\s
				}
			}
			"""
	};
	this.reportInvalidJavadoc = CompilerOptions.WARNING;
	runConformTest(true, units,
			"""
				----------
				1. WARNING in pkg\\X.java (at line 5)
					* @see <a href="ccwww.xyzzy.com/rfc123.html">invalid></aa>
					                                                     ^^^^^
				Javadoc: Malformed link reference
				----------
				""",
			null, null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
}

public void testBug125518e() {
	String[] units = new String[] {
		"pkg/X.java",
		"""
			package pkg;
			
			public class X {
				/**
				 * @see <a href="http\u003A\u002F\u002Fwww.eclipse.org"><valid>value</valid></a>
				 */
				public void foo() {\s
				\s
				}
			}
			"""
		};
	this.reportInvalidJavadoc = CompilerOptions.WARNING;
	runConformTest(units);
}

/**
 * Bug 125903: [javadoc] Treat whitespace in javadoc tags as invalid tags
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=125903"
 */
public void testBug125903() {
	this.reportMissingJavadocTags = CompilerOptions.ERROR;
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				/**
				 * {@ link java.lang.String}
				 * @ since 2.1
				 */
				public class X {
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				* {@ link java.lang.String}
				   ^^
			Javadoc: Invalid tag
			----------
			2. ERROR in X.java (at line 3)
				* @ since 2.1
				  ^^
			Javadoc: Invalid tag
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * Bug 128954: Javadoc problems with category CAT_INTERNAL
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=128954"
 */
public void testBug128954() {
	this.reportInvalidJavadoc = CompilerOptions.WARNING;
	this.reportDeprecation = CompilerOptions.WARNING;
	runNegativeTest(
		new String[] {
			"X.java", //========================
			"""
				public class X {
					/**
					 * @see p.A#bar()
					 */
					void foo() {
						Zork z;
					}
				}
				""",
			"p/A.java",  //========================
			"""
				package p;
				public class A {
					/** @deprecated */
					public void bar() {
					}
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 3)
				* @see p.A#bar()
				           ^^^^^
			[@cat:javadoc] [@sup:javadoc] Javadoc: The method bar() from the type A is deprecated
			----------
			2. ERROR in X.java (at line 6)
				Zork z;
				^^^^
			[@cat:type] Zork cannot be resolved to a type
			----------
			""",
		null,
		true,
		null,
		false,
		true,
		true);
}

/**
 * Bug 128954: Javadoc problems with category CAT_INTERNAL - variation
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=128954"
 */
public void testBug128954a() {
	this.reportInvalidJavadoc = CompilerOptions.WARNING;
	this.reportDeprecation = CompilerOptions.WARNING;
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				\t
					/**
					 * @see p.A#bar()
					 */
					void foo() {
						Zork z;
					}
				}
				""",
		},
		"""
			----------
			1. WARNING in X.java (at line 4)
				* @see p.A#bar()
				       ^^^
			[@cat:javadoc] [@sup:javadoc] Javadoc: p cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 7)
				Zork z;
				^^^^
			[@cat:type] Zork cannot be resolved to a type
			----------
			""",
		null,
		true,
		null,
		false,
		true,
		true);
}

/**
 * Bug 129241: [Javadoc] deprecation warning wrongly reported when ignoring Malformed Javadoc comments
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=129241"
 */
public void testBug129241a() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					/**
					 * @see p.A#bar
					 */
					void foo() {}
				}
				""",
			"p/A.java",
			"""
				package p;
				/** @deprecated */
				public class A {
					void bar() {}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				* @see p.A#bar
				       ^^^
			Javadoc: The type A is deprecated
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug129241b() {
	this.reportDeprecation = CompilerOptions.IGNORE;
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					/**
					 * @see p.A#bar
					 */
					void foo() {}
				}
				""",
			"p/A.java",
			"""
				package p;
				/** @deprecated */
				public class A {
					void bar() {}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				* @see p.A#bar
				       ^^^
			Javadoc: The type A is deprecated
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug129241c() {
	this.reportJavadocDeprecation = CompilerOptions.DISABLED;
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					/**
					 * @see p.A#bar
					 */
					void foo() {}
				}
				""",
			"p/A.java",
			"""
				package p;
				/** @deprecated */
				public class A {
					void bar() {}
				}
				"""
		}
	);
}
public void testBug129241d() {
	this.reportInvalidJavadoc = CompilerOptions.IGNORE;
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					/**
					 * @see p.A#bar
					 */
					void foo() {}
				}
				""",
			"p/A.java",
			"""
				package p;
				/** @deprecated */
				public class A {
					void bar() {}
				}
				"""
		}
	);
}

/**
 * Bug 132813: NPE in Javadoc.resolve(Javadoc.java:196) + log swamped
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=132813"
 */
public void testBug132813() {
	runNegativeTest(
		new String[] {
			"Test.java",
			"""
				public class X {\s
					/**	 */\s
					public Test() {}
					/**	 */
					public test() {}
				}
				"""			},
		"""
			----------
			1. ERROR in Test.java (at line 1)
				public class X {\s
				             ^
			The public type X must be defined in its own file
			----------
			2. ERROR in Test.java (at line 3)
				public Test() {}
				       ^^^^^^
			Return type for the method is missing
			----------
			3. ERROR in Test.java (at line 5)
				public test() {}
				       ^^^^^^
			Return type for the method is missing
			----------
			"""
	);
}

/**
 * Bug 149013: [javadoc] In latest 3.3 build, there is a javadoc error in org.eclipse.core.resources
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=149013"
 */
public void testBug149013_Private01() {
	this.reportMissingJavadocTags = CompilerOptions.IGNORE;
	runConformTest(
		new String[] {
			"test1/X.java",
			"""
				package test1;
				public class X {
					class Inner {
						class Level2 {
							class Level3 {}
						}
					}
				}
				""",
			"test1/Test.java",
			"""
				package test1;
				/**
				 * @see X.Inner
				 * @see X.Inner.Level2
				 * @see X.Inner.Level2.Level3
				 */
				public class Test {}
				""",
		}
	);
}
public void testBug149013_Public01() {
	this.reportMissingJavadocTags = CompilerOptions.DISABLED;
	this.reportInvalidJavadocVisibility = CompilerOptions.PUBLIC;
	runNegativeTest(
		new String[] {
			"test1/X.java",
			"""
				package test1;
				public class X {
					class Inner {
						class Level2 {
							class Level3 {}
						}
					}
				}
				""",
			"test1/Test.java",
			"""
				package test1;
				/**
				 * @see X.Inner
				 * @see X.Inner.Level2
				 * @see X.Inner.Level2.Level3
				 */
				public class Test {
				}
				"""
		},
		"""
			----------
			1. ERROR in test1\\Test.java (at line 3)
				* @see X.Inner
				       ^^^^^^^
			Javadoc: \'public\' visibility for malformed doc comments hides this \'default\' reference
			----------
			2. ERROR in test1\\Test.java (at line 4)
				* @see X.Inner.Level2
				       ^^^^^^^^^^^^^^
			Javadoc: \'public\' visibility for malformed doc comments hides this \'default\' reference
			----------
			3. ERROR in test1\\Test.java (at line 5)
				* @see X.Inner.Level2.Level3
				       ^^^^^^^^^^^^^^^^^^^^^
			Javadoc: \'public\' visibility for malformed doc comments hides this \'default\' reference
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug149013_Private02() {
	this.reportMissingJavadocTags = CompilerOptions.IGNORE;
	runNegativeTest(
		new String[] {
			"test1/X.java",
			"""
				package test1;
				public class X {
					class Inner {
						class Level2 {
							class Level3 {}
						}
					}
				}
				""",
			"test2/Test.java",
			"""
				package test2;
				import test1.X;
				/**
				 * @see X.Inner
				 * @see X.Inner.Level2
				 * @see X.Inner.Level2.Level3
				 */
				public class Test {}
				""",
		},
		"""
			----------
			1. ERROR in test2\\Test.java (at line 4)\r
				* @see X.Inner\r
				       ^^^^^^^
			Javadoc: The type X.Inner is not visible
			----------
			2. ERROR in test2\\Test.java (at line 5)\r
				* @see X.Inner.Level2\r
				       ^^^^^^^^^^^^^^
			Javadoc: The type X.Inner is not visible
			----------
			3. ERROR in test2\\Test.java (at line 6)\r
				* @see X.Inner.Level2.Level3\r
				       ^^^^^^^^^^^^^^^^^^^^^
			Javadoc: The type X.Inner is not visible
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug149013_Public02() {
	this.reportMissingJavadocTags = CompilerOptions.DISABLED;
	this.reportInvalidJavadocVisibility = CompilerOptions.PUBLIC;
	runNegativeTest(
		new String[] {
			"test1/X.java",
			"""
				package test1;
				public class X {
					class Inner {
						class Level2 {
							class Level3 {}
						}
					}
				}
				""",
			"test2/Test.java",
			"""
				package test2;
				import test1.X;
				/**
				 * @see X.Inner
				 * @see X.Inner.Level2
				 * @see X.Inner.Level2.Level3
				 */
				public class Test {}
				""",
		},
		"""
			----------
			1. ERROR in test2\\Test.java (at line 4)\r
				* @see X.Inner\r
				       ^^^^^^^
			Javadoc: The type X.Inner is not visible
			----------
			2. ERROR in test2\\Test.java (at line 5)\r
				* @see X.Inner.Level2\r
				       ^^^^^^^^^^^^^^
			Javadoc: The type X.Inner is not visible
			----------
			3. ERROR in test2\\Test.java (at line 6)\r
				* @see X.Inner.Level2.Level3\r
				       ^^^^^^^^^^^^^^^^^^^^^
			Javadoc: The type X.Inner is not visible
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug149013_Private03() {
	this.reportMissingJavadocTags = CompilerOptions.IGNORE;
	runNegativeTest(
		new String[] {
			"test1/X.java",
			"""
				package test1;
				public class X {
					class Inner {
						class Level2 {
							class Level3 {}
						}
					}
				}
				""",
			"test3/Test.java",
			"""
				package test3;
				/**
				 * @see test1.X.Inner
				 * @see test1.X.Inner.Level2
				 * @see test1.X.Inner.Level2.Level3
				 */
				public class Test {}
				"""
		},
		"""
			----------
			1. ERROR in test3\\Test.java (at line 3)\r
				* @see test1.X.Inner\r
				       ^^^^^^^^^^^^^
			Javadoc: The type test1.X.Inner is not visible
			----------
			2. ERROR in test3\\Test.java (at line 4)\r
				* @see test1.X.Inner.Level2\r
				       ^^^^^^^^^^^^^^^^^^^^
			Javadoc: The type test1.X.Inner is not visible
			----------
			3. ERROR in test3\\Test.java (at line 5)\r
				* @see test1.X.Inner.Level2.Level3\r
				       ^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Javadoc: The type test1.X.Inner is not visible
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug149013_Public03() {
	this.reportMissingJavadocTags = CompilerOptions.DISABLED;
	this.reportInvalidJavadocVisibility = CompilerOptions.PUBLIC;
	runNegativeTest(
		new String[] {
			"test1/X.java",
			"""
				package test1;
				public class X {
					class Inner {
						class Level2 {
							class Level3 {}
						}
					}
				}
				""",
			"test3/Test.java",
			"""
				package test3;
				/**
				 * @see test1.X.Inner
				 * @see test1.X.Inner.Level2
				 * @see test1.X.Inner.Level2.Level3
				 */
				public class Test {}
				"""
		},
		"""
			----------
			1. ERROR in test3\\Test.java (at line 3)\r
				* @see test1.X.Inner\r
				       ^^^^^^^^^^^^^
			Javadoc: The type test1.X.Inner is not visible
			----------
			2. ERROR in test3\\Test.java (at line 4)\r
				* @see test1.X.Inner.Level2\r
				       ^^^^^^^^^^^^^^^^^^^^
			Javadoc: The type test1.X.Inner is not visible
			----------
			3. ERROR in test3\\Test.java (at line 5)\r
				* @see test1.X.Inner.Level2.Level3\r
				       ^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Javadoc: The type test1.X.Inner is not visible
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * bug 153399: [javadoc] JDT Core should warn if the @value tag is not used correctly
 * test Ensure that 'value' tag is well warned when not used correctly
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=153399"
 */
public void testBug153399a() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			public class X {\s
				/**
				 * {@value #MY_VALUE}
				 */
				public final static int MY_VALUE = 0;\s
				/**
				 * {@value #MY_VALUE}
				 */
				public void foo() {}
				/**
				 * {@value #MY_VALUE}
				 */
				class Sub {}\s
			}
			"""
	};
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		runNegativeTest(testFiles,
			"""
				----------
				1. ERROR in X.java (at line 7)
					* {@value #MY_VALUE}
					    ^^^^^
				Javadoc: Unexpected tag
				----------
				2. ERROR in X.java (at line 11)
					* {@value #MY_VALUE}
					    ^^^^^
				Javadoc: Unexpected tag
				----------
				"""
		);
	} else {
		runConformTest(testFiles);
	}
}
public void testBug153399b() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			public class X {\s
				/**
				 * {@value}
				 */
				public final static int MY_VALUE = 0;\s
				/**
				 * {@value}
				 */
				public void foo() {}
				/**
				 * {@value}
				 */
				class Sub {}\s
			}
			"""
	};
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		runNegativeTest(testFiles,
			"""
				----------
				1. ERROR in X.java (at line 7)
					* {@value}
					    ^^^^^
				Javadoc: Unexpected tag
				----------
				2. ERROR in X.java (at line 11)
					* {@value}
					    ^^^^^
				Javadoc: Unexpected tag
				----------
				"""
		);
	} else {
		runConformTest(testFiles);
	}
}
public void testBug153399c() {
	String[] testFiles = new String[] {
		"p1/X.java",
		"""
			package p1;
			public class X {
				/**
				 * @return a
				 */
				boolean get() {
					return false;
				}
			}
			"""
	};
	runConformTest(testFiles);
}
public void testBug153399d() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			public class X {\s
				/**
				 * {@value #MY_VALUE}
				 * {@value}
				 * {@value Invalid}
				 */
				public final static int MY_VALUE = 0;\s
			}
			"""
	};
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		runNegativeTest(testFiles,
			"""
				----------
				1. ERROR in X.java (at line 3)
					* {@value #MY_VALUE}
					    ^^^^^
				Javadoc: Unexpected tag
				----------
				2. ERROR in X.java (at line 4)
					* {@value}
					    ^^^^^
				Javadoc: Unexpected tag
				----------
				"""
		);
	} else {
		runNegativeTest(testFiles,
			"""
				----------
				1. ERROR in X.java (at line 5)
					* {@value Invalid}
					          ^^^^^^^^
				Javadoc: Invalid reference
				----------
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
}
public void testBug153399e() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			public class X {\s
				/**
				 * {@value Invalid}
				 * {@value #MY_VALUE}
				 */
				public final static int MY_VALUE = 0;\s
			}
			"""
	};
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		runNegativeTest(testFiles,
			"""
				----------
				1. ERROR in X.java (at line 3)
					* {@value Invalid}
					    ^^^^^
				Javadoc: Unexpected tag
				----------
				"""
		);
	} else {
		runNegativeTest(testFiles,
			"""
				----------
				1. ERROR in X.java (at line 3)
					* {@value Invalid}
					          ^^^^^^^^
				Javadoc: Invalid reference
				----------
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
}

/**
 * bug 160015: [1.5][javadoc] Missing warning on autoboxing compatible methods
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=160015"
 */
public void testBug160015() {
	runNegativeTest(new String[] {
			"Test.java",
			"""
				/**
				 * @see #method(Long) Warning!
				 */
				public class Test {
					public void method(long l) {}
					/**
					 * @see #method(Long) Warning!
					 */
					void bar() {}
				}
				"""
		},
		"""
			----------
			1. ERROR in Test.java (at line 2)
				* @see #method(Long) Warning!
				        ^^^^^^
			Javadoc: The method method(long) in the type Test is not applicable for the arguments (Long)
			----------
			2. ERROR in Test.java (at line 7)
				* @see #method(Long) Warning!
				        ^^^^^^
			Javadoc: The method method(long) in the type Test is not applicable for the arguments (Long)
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * bug 163659: [javadoc] Compiler should warn when method parameters are not identical
 * test Ensure that a warning is raised when method parameter types are not identical
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=163659"
 */
public void testBug163659() {
	runNegativeTest(
		new String[] {
			"Test.java",
			"""
				/**
				 * @see #foo(MyInterface)
				 * @see #foo(MySubInterface)
				 */
				public class Test {
					public void foo(MyInterface mi) {
					}
				}
				interface MyInterface {}
				interface MySubInterface extends MyInterface {}\s
				"""
		},
		"""
			----------
			1. ERROR in Test.java (at line 3)
				* @see #foo(MySubInterface)
				        ^^^
			Javadoc: The method foo(MyInterface) in the type Test is not applicable for the arguments (MySubInterface)
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * bug 165794: [javadoc] Should not report ambiguous on method with parameterized types as parameters
 * test Ensure that no warning are raised when ambiguous parameterized methods are present in javadoc comments
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=165794"
 */
public void _testBug165794() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			/**
			 * No reasonable hint for resolving the {@link #getMax(A)}.
			 */
			public class X {
			    /**
			     * Extends Number method.
			     * @see #getMax(A ipZ)
			     */
			    public <T extends Y> T getMax(final A<T> ipY) {
			        return ipY.t();
			    }
			   \s
			    /**
			     * Extends Exception method.
			     * @see #getMax(A ipY)
			     */
			    public <T extends Z> T getMax(final A<T> ipZ) {
			        return ipZ.t();
			    }
			}
			class A<T> {
				T t() { return null; }
			}
			class Y {}
			class Z {}"""
	};
	if (this.complianceLevel <= ClassFileConstants.JDK1_4
			|| this.complianceLevel >= ClassFileConstants.JDK1_7) {
		return;
	}
	runConformTest(testFiles);
}
/**
 * bug 166365: [javadoc] severity level of malformed javadoc comments did not work properly
 * test Ensure that no warning is raised when visibility is lower than the javadoc option one
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=166365"
 */
public void testBug166365() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			public class X {
			    /**
			     * @return
			     */
			    private String getSomePrivate() {
			        return "SomePrivate";
			    }
			    /**
			     * @return
			     */
			    protected String getSomeProtected() {
			        return "SomeProtected";
			    }
			    /**
			     * @return
			     */
			    String getSomeDefault() {
			        return "SomeDefault";
			    }
			    /**
			     * @return
			     */
			    public String getSomePublic() {
			        return "SomePublic";
			    }
			}
			"""
	};
	this.reportInvalidJavadocVisibility = CompilerOptions.PUBLIC;
	runNegativeTest(testFiles,
		"""
			----------
			1. ERROR in X.java (at line 21)
				* @return
				   ^^^^^^
			Javadoc: Description expected after @return
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * bug 166436: [javadoc] Potentially wrong javadoc warning for unexpected duplicate tag value
 * test Ensure that no duplicate warning is raised for value tag
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=166436"
 */
public void testBug166436() {
	String[] testFiles = new String[] {
		"X.java",
		"""
			public class X {
				public static final String PUBLIC_CONST = "public";
				protected static final String PROTECTED_CONST = "protected";
				static final String DEFAULT_CONST = "default";\s
				private static final String PRIVATE_CONST = "private";\s
				/**
				 * Values:
				 * <ul>
				 * 	<li>{@value #PUBLIC_CONST}</li>
				 * 	<li>{@value #PROTECTED_CONST}</li>
				 * 	<li>{@value #DEFAULT_CONST}</li>
				 * 	<li>{@value #PRIVATE_CONST}</li>
				 * </ul>
				 */
				public X() {
				}
			}
			"""
	};
	this.reportInvalidJavadocVisibility = CompilerOptions.PUBLIC;
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		runNegativeTest(testFiles,
			"""
				----------
				1. ERROR in X.java (at line 9)
					* 	<li>{@value #PUBLIC_CONST}</li>
					  	      ^^^^^
				Javadoc: Unexpected tag
				----------
				2. ERROR in X.java (at line 10)
					* 	<li>{@value #PROTECTED_CONST}</li>
					  	      ^^^^^
				Javadoc: Unexpected tag
				----------
				3. ERROR in X.java (at line 11)
					* 	<li>{@value #DEFAULT_CONST}</li>
					  	      ^^^^^
				Javadoc: Unexpected tag
				----------
				4. ERROR in X.java (at line 12)
					* 	<li>{@value #PRIVATE_CONST}</li>
					  	      ^^^^^
				Javadoc: Unexpected tag
				----------
				"""
		);
	} else {
		runNegativeTest(testFiles,
			"""
				----------
				1. ERROR in X.java (at line 10)
					* 	<li>{@value #PROTECTED_CONST}</li>
					  	            ^^^^^^^^^^^^^^^^
				Javadoc: \'public\' visibility for malformed doc comments hides this \'protected\' reference
				----------
				2. ERROR in X.java (at line 11)
					* 	<li>{@value #DEFAULT_CONST}</li>
					  	            ^^^^^^^^^^^^^^
				Javadoc: \'public\' visibility for malformed doc comments hides this \'default\' reference
				----------
				3. ERROR in X.java (at line 12)
					* 	<li>{@value #PRIVATE_CONST}</li>
					  	            ^^^^^^^^^^^^^^
				Javadoc: \'public\' visibility for malformed doc comments hides this \'private\' reference
				----------
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
		);
	}
}

/**
 * bug 168849: [javadoc] Javadoc warning on @see reference in class level docs.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=168849"
 */
public void testBug168849a() {
	String[] units = new String[] {
		"pkg/X.java",
		"""
			package pkg;
			
			public class X {
				/**
				 * @see http://www.eclipse.org/
				 */
				public void foo() {\s
				\s
				}
			}
			"""
	};
	this.reportInvalidJavadoc = CompilerOptions.WARNING;
	runConformTest(true, units,
			"""
				----------
				1. WARNING in pkg\\X.java (at line 5)
					* @see http://www.eclipse.org/
					       ^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid URL reference. Double quote the reference or use the href syntax
				----------
				""",
			null, null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
}

public void testBug168849b() {
	String[] units = new String[] {
		"pkg/X.java",
		"""
			package pkg;
			
			public class X {
				/**
				 * @see http://ftp.eclipse.org/
				 */
				public void foo() {\s
				\s
				}
			}
			"""
	};
	this.reportInvalidJavadoc = CompilerOptions.WARNING;
	runConformTest(true, units,
			"""
				----------
				1. WARNING in pkg\\X.java (at line 5)
					* @see http://ftp.eclipse.org/
					       ^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid URL reference. Double quote the reference or use the href syntax
				----------
				""",
			null, null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
}

public void testBug168849c() {
	String[] units = new String[] {
		"pkg/X.java",
		"""
			package pkg;
			
			public class X {
				/**
				 * @see ://
				 */
				public void foo() {\s
				\s
				}
			}
			"""
	};
	this.reportInvalidJavadoc = CompilerOptions.WARNING;
	runConformTest(
			true, units,
			"""
				----------
				1. WARNING in pkg\\X.java (at line 5)
					* @see ://
					   ^^^
				Javadoc: Missing reference
				----------
				""",
			null, null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
}

public void testBug168849d() {
	String[] units = new String[] {
		"pkg/X.java",
		"""
			package pkg;
			
			public class X {
				/**
				 * @see http\u003A\u002F\u002Fwww.eclipse.org
				 */
				public void foo() {\s
				\s
				}
			}
			"""
	};
	this.reportInvalidJavadoc = CompilerOptions.WARNING;
	runConformTest(true, units,
			"""
				----------
				1. WARNING in pkg\\X.java (at line 5)
					* @see http://www.eclipse.org
					       ^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Invalid URL reference. Double quote the reference or use the href syntax
				----------
				""",
			null, null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
}

public void testBug168849e() {
	String[] units = new String[] {
		"pkg/X.java",
		"""
			package pkg;
			
			public class X {
				/**
				 * @see "http\u003A\u002F\u002Fwww.eclipse.org"
				 */
				public void foo() {\s
				\s
				}
			}
			"""
	};
	this.reportInvalidJavadoc = CompilerOptions.WARNING;
	runConformTest(units);
}

public void testBug168849f() {
	String[] units = new String[] {
		"pkg/X.java",
		"""
			package pkg;
			
			public class X {
				/**
				 * @see "http://www.eclipse.org/"
				 */
				public void foo() {\s
				\s
				}
			}
			"""
	};
	this.reportInvalidJavadoc = CompilerOptions.WARNING;
	runConformTest(units);
}

public void testBug168849g() {
	String[] units = new String[] {
		"pkg/X.java",
		"""
			package pkg;
			
			public class X {
				/**
				 * @see http:/ invalid reference
				 */
				public void foo() {\s
				\s
				}
			}
			"""
	};
	this.reportInvalidJavadoc = CompilerOptions.WARNING;
	runConformTest(true, units,
			"""
				----------
				1. WARNING in pkg\\X.java (at line 5)
					* @see http:/ invalid reference
					       ^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Malformed reference (missing end space separator)
				----------
				""",
			null, null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
}

public void testBug168849h() {
	String[] units = new String[] {
		"pkg/X.java",
		"""
			package pkg;
			
			public class X {
				/**
				 * @see Object:/ invalid reference
				 */
				public void foo() {\s
				\s
				}
			}
			"""
	};
	this.reportInvalidJavadoc = CompilerOptions.WARNING;
	runConformTest(true, units,
			"""
				----------
				1. WARNING in pkg\\X.java (at line 5)
					* @see Object:/ invalid reference
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Malformed reference (missing end space separator)
				----------
				""",
			null, null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
}

public void testBug168849i() {
	String[] units = new String[] {
		"pkg/X.java",
		"""
			package pkg;
			
			public class X {
				/**
				 * @see http\u003A\u002F invalid reference
				 */
				public void foo() {\s
				\s
				}
			}
			"""
	};
	this.reportInvalidJavadoc = CompilerOptions.WARNING;
	runConformTest(true, units,
			"""
				----------
				1. WARNING in pkg\\X.java (at line 5)
					* @see http:/ invalid reference
					       ^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Malformed reference (missing end space separator)
				----------
				""",
			null, null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
}

public void testBug168849j() {
	String[] units = new String[] {
		"pkg/X.java",
		"""
			package pkg;
			
			public class X {
				/**
				 * @see Object\u003A\u002F invalid reference
				 */
				public void foo() {\s
				\s
				}
			}
			"""
	};
	this.reportInvalidJavadoc = CompilerOptions.WARNING;
	runConformTest(true, units,
			"""
				----------
				1. WARNING in pkg\\X.java (at line 5)
					* @see Object:/ invalid reference
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: Malformed reference (missing end space separator)
				----------
				""",
			null, null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
}

/**
 * bug 170637: [javadoc] incorrect warning about missing parameter javadoc when using many links
 * test Verify that javadoc parser is not blown-up when there's a lot of inline tags
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=170637"
 */
public void testBug170637() {
	this.reportMissingJavadocTags = CompilerOptions.ERROR;
	runConformTest(
		new String[] {
			"JavaDocTest.java",
			"""
				public interface JavaDocTest
				{
				  /**
				   * This is some stupid test...
				   *\s
				   * {@link JavaDocTest}
				   *\s
				   * @param bar1 {@link JavaDocTest}
				   * @param bar2 {@link JavaDocTest}
				   * @param bar3 {@link JavaDocTest}
				   * @param bar4 {@link JavaDocTest}
				   * @param bar5 {@link JavaDocTest}
				   * @param bar6 {@link JavaDocTest}
				   * @param bar7 {@link JavaDocTest}
				   * @param bar8 {@link JavaDocTest}
				   * @param bar9 {@link JavaDocTest}
				   * @param bar10 {@link JavaDocTest}
				   * @param bar11 {@link JavaDocTest}
				   * @param bar12 {@link JavaDocTest}
				   * @param bar13 {@link JavaDocTest}
				   *\s
				   * @return A string!
				   */
				  public String foo(String bar1,
				      String bar2,
				      String bar3,
				      String bar4,
				      String bar5,
				      String bar6,
				      String bar7,
				      String bar8,
				      String bar9,
				      String bar10,
				      String bar11,
				      String bar12,
				      String bar13
				      );
				
				  /**
				   * This is some more stupid test...
				   *\s
				   * {@link JavaDocTest}
				   * {@link JavaDocTest}
				   * {@link JavaDocTest}
				   * {@link JavaDocTest}
				   * {@link JavaDocTest}
				   * {@link JavaDocTest}
				   * {@link JavaDocTest}
				   * {@link JavaDocTest}
				   * {@link JavaDocTest}
				   * {@link JavaDocTest}
				   * {@link JavaDocTest}
				   * {@link JavaDocTest}
				   * {@link JavaDocTest}
				   * {@link JavaDocTest}
				   * {@link JavaDocTest}
				   * {@link JavaDocTest}
				   * {@link JavaDocTest}
				   * {@link JavaDocTest}
				   * {@link JavaDocTest}
				   * {@link JavaDocTest}
				   * {@link JavaDocTest}
				   *\s
				   * @param bar1\s
				   * @param bar2\s
				   * @param bar3\s
				   * @param bar4\s
				   * @param bar5\s
				   * @param bar6\s
				   * @param bar7\s
				   * @param bar8\s
				   * @param bar9\s
				   * @param bar10\s
				   * @param bar11\s
				   * @param bar12\s
				   * @param bar13\s
				   *\s
				   * @return A string!
				   */
				  public String foo2(String bar1,
				      String bar2,
				      String bar3,
				      String bar4,
				      String bar5,
				      String bar6,
				      String bar7,
				      String bar8,
				      String bar9,
				      String bar10,
				      String bar11,
				      String bar12,
				      String bar13
				      );
				}
				"""
		}
	);
}
public void testBug170637a() {
	// conform test: verify we can handle a large number of tags
	String[] units = new String[] {
		"pkg/X.java",
		"""
			package pkg;
			public interface X
			{
			  /**
			   * Test for bug {@link "https://bugs.eclipse.org/bugs/show_bug.cgi?id=170637"}
			   *\s
			   *\s
			   * @param bar1 {@link X}
			   * @param bar2 {@link X}
			   * @param bar3 {@link X}
			   * @param bar4 {@link X}
			   * @param bar5 {@link X}
			   * @param bar6 {@link X}
			   * @param bar7 {@link X}
			   * @param bar8 {@link X}
			   * @param bar9 {@link X}
			   * @param bar10 {@link X}
			   * @param bar11 {@link X}
			   * @param bar12 {@link X}
			   * @param bar13 {@link X}
			   * @param bar14 {@link X}
			   * @param bar15 {@link X}
			   * @param bar16 {@link X}
			   * @param bar17 {@link X}
			   * @param bar18 {@link X}
			   * @param bar19 {@link X}
			   * @param bar20 {@link X}
			   * @param bar21 {@link X}
			   * @param bar22 {@link X}
			   * @param bar23 {@link X}
			   * @param bar24 {@link X}
			   * @param bar25 {@link X}
			   * @param bar26 {@link X}
			   * @param bar27 {@link X}
			   * @param bar28 {@link X}
			   * @param bar29 {@link X}
			   * @param bar30 {@link X}
			   *\s
			   * @return A string
			   */
			  public String foo(String bar1,
			      String bar2,
			      String bar3,
			      String bar4,
			      String bar5,
			      String bar6,
			      String bar7,
			      String bar8,
			      String bar9,
			      String bar10,
			      String bar11,
			      String bar12,
			      String bar13,
			      String bar14,
			      String bar15,
			      String bar16,
			      String bar17,
			      String bar18,
			      String bar19,
			      String bar20,
			      String bar21,
			      String bar22,
			      String bar23,
			      String bar24,
			      String bar25,
			      String bar26,
			      String bar27,
			      String bar28,
			      String bar29,
			      String bar30
			      );
			}
			"""
	};
	this.reportMissingJavadocTags = CompilerOptions.ERROR;
	runConformTest(units);
}
public void testBug170637b() {
	// conform test: verify we are able to raise warnings when dealing with a large number of tags
	String[] units = new String[] {
		"X.java",
		"""
			public interface X
			{
			  /**
			   * Test for bug {@link "https://bugs.eclipse.org/bugs/show_bug.cgi?id=170637"}
			   *\s
			   *\s
			   * @param bar1 {@link X}
			   * @param bar2 {@link X}
			   * @param bar3 {@link X}
			   * @param bar4 {@link X}
			   * @param bar5 {@link X}
			   * @param bar6 {@link X}
			   * @param bar7 {@link X}
			   * @param bar8 {@link X}
			   * @param bar9 {@link X}
			   * @param bar10 {@link X}
			   * @param bar11 {@link X}
			   * @param bar12 {@link X}
			   * @param bar13 {@link X}
			   * @param bar14 {@link X}
			   * @param bar15 {@link X}
			   * @param bar16 {@link X}
			   * @param bar17 {@link X}
			   * @param bar18 {@link X}
			   * @param bar19 {@link X}
			   * @param bar20 {@link X}
			   * @param bar21 {@link X}
			   * @param bar22 {@link X}
			   * @param bar23 {@link X}
			   * @param bar24 {@link X}
			   * @param bar25 {@link X}
			   * @param bar26 {@link X}
			   * @param bar27 {@link X}
			   * @param bar28 {@link X}
			   * @param bar29 {@link X}
			   * @param bar30 {@link X}
			   *\s
			   * @return A string
			   */
			  public String foo(String bar1,
			      String bar2,
			      String bar3,
			      String bar4,
			      String bar5,
			      String bar6,
			      String bar7,
			      String bar8,
			      String bar9,
			      String bar10,
			      String bar11,
			      String bar12,
			      String bar13,
			      String bar14,
			      String bar15,
			      String bar16,
			      String bar17,
			      String bar18,
			      String bar19,
			      String bar20,
			      String bar21,
			      String bar22,
			      String bar23,
			      String bar24,
			      String bar25,
			      String bar26,
			      String bar27,
			      String bar28,
			      String bar29,
			      String bar30,
			      String bar31
			      );
			}
			"""
	};
	this.reportMissingJavadocTags = CompilerOptions.ERROR;
	runNegativeTest(units,
		"""
			----------
			1. ERROR in X.java (at line 70)
				String bar31
				       ^^^^^
			Javadoc: Missing tag for parameter bar31
			----------
			""");
}

/**
 * Bug 176027: [javadoc] @link to member type handled incorrectly
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=176027"
 */
public void testBug176027a() {
	// case1 class X static class Inner
	String[] units = new String[] {
		"otherpkg/C.java",
		"""
			package otherpkg;
			public class C {
			        public static class Inner { }
			}
			"""
		,
		"somepkg/MemberTypeDocTest.java",
		"""
			package somepkg;
			import otherpkg.C.Inner;
			/**
			 * {@link Inner} -- error/warning\s
			 */
			public class MemberTypeDocTest {
			      void m() { }
			}
			"""
	};
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		runNegativeTest(units,
			"""
				----------
				1. ERROR in somepkg\\MemberTypeDocTest.java (at line 4)
					* {@link Inner} -- error/warning\s
					         ^^^^^
				Javadoc: Invalid member type qualification
				----------
				"""
		);
	}
	else {
		runConformTest(units);
	}
}

public void testBug176027b() {
	// case3 class X class Inner
	String[] units = new String[] {
		"otherpkg/C.java",
		"""
			package otherpkg;
			public class C {
			        public class Inner { }
			}
			"""
		,
		"somepkg/MemberTypeDocTest.java",
		"""
			package somepkg;
			import otherpkg.C.Inner;
			/**
			 * {@link Inner} -- error/warning\s
			 */
			public class MemberTypeDocTest {
			      void m() { }
			}
			"""
	};
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		runNegativeTest(units,
			"""
				----------
				1. ERROR in somepkg\\MemberTypeDocTest.java (at line 4)
					* {@link Inner} -- error/warning\s
					         ^^^^^
				Javadoc: Invalid member type qualification
				----------
				"""
		);
	}
	else {
		runConformTest(units);
	}
}

public void testBug176027c() {
	// case3 class X interface Inner
	String[] units = new String[] {
		"otherpkg/C.java",
		"""
			package otherpkg;
			public class C {
			        public interface Inner { }
			}
			"""
		,
		"somepkg/MemberTypeDocTest.java",
		"""
			package somepkg;
			import otherpkg.C.Inner;
			/**
			 * {@link Inner} -- error/warning\s
			 */
			public class MemberTypeDocTest {
			      void m() { }
			}
			"""
	};
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		runNegativeTest(units,
			"""
				----------
				1. ERROR in somepkg\\MemberTypeDocTest.java (at line 4)
					* {@link Inner} -- error/warning\s
					         ^^^^^
				Javadoc: Invalid member type qualification
				----------
				"""
		);
	}
	else {
		runConformTest(units);
	}
}

public void testBug176027d() {
	// case4 interface X static class Inner
	String[] units = new String[] {
		"otherpkg/C.java",
		"""
			package otherpkg;
			public interface C {
			        public static class Inner { }
			}
			"""
		,
		"somepkg/MemberTypeDocTest.java",
		"""
			package somepkg;
			import otherpkg.C.Inner;
			/**
			 * {@link Inner} -- error/warning\s
			 */
			public class MemberTypeDocTest {
			      void m() { }
			}
			"""
	};
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		runNegativeTest(units,
			"""
				----------
				1. ERROR in somepkg\\MemberTypeDocTest.java (at line 4)
					* {@link Inner} -- error/warning\s
					         ^^^^^
				Javadoc: Invalid member type qualification
				----------
				"""
		);
	}
	else {
		runConformTest(units);
	}
}

public void testBug176027f() {
	// case5 interface X class Inner
	String[] units = new String[] {
		"otherpkg/C.java",
		"""
			package otherpkg;
			public interface C {
			        public class Inner { }
			}
			"""
		,
		"somepkg/MemberTypeDocTest.java",
		"""
			package somepkg;
			import otherpkg.C.Inner;
			/**
			 * {@link Inner} -- error/warning\s
			 */
			public class MemberTypeDocTest {
			      void m() { }
			}
			"""
	};
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		runNegativeTest(units,
			"""
				----------
				1. ERROR in somepkg\\MemberTypeDocTest.java (at line 4)
					* {@link Inner} -- error/warning\s
					         ^^^^^
				Javadoc: Invalid member type qualification
				----------
				"""
		);
	}
	else {
		runConformTest(units);
	}
}

public void testBug176027g() {
	// case6 interface X interface Inner
	String[] units = new String[] {
		"otherpkg/C.java",
		"""
			package otherpkg;
			public interface C {
			        public interface Inner { }
			}
			"""
		,
		"somepkg/MemberTypeDocTest.java",
		"""
			package somepkg;
			import otherpkg.C.Inner;
			/**
			 * {@link Inner} -- error/warning\s
			 */
			public class MemberTypeDocTest {
			      void m() { }
			}
			"""
	};
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		runNegativeTest(units,
			"""
				----------
				1. ERROR in somepkg\\MemberTypeDocTest.java (at line 4)
					* {@link Inner} -- error/warning\s
					         ^^^^^
				Javadoc: Invalid member type qualification
				----------
				"""
		);
	}
	else {
		runConformTest(units);
	}
}

public void testBug176027h_public() {
	// test embedded inner classes
	String[] units = new String[] {
			"mainpkg/Outer.java",
			"""
				package mainpkg;
				public class Outer {
				        public class Inner {
				        	public class MostInner{
				   \s
				        	}
				        }\s
				}
				"""
			,
			"pkg1/Valid1.java",
			"""
				package pkg1;\s
				import mainpkg.Outer.Inner.MostInner;
				// valid import - no error in 5.0
				
				/**\s
				 * {@link MostInner}
				 *\s
				 */\s
				public class Valid1 {\s
					/**\s
					 * {@link MostInner}\s
					 *\s
					 */\s
				      void m() { }\s
				}
				"""
			,
			"pkg2/Valid2.java",
			"""
				package pkg2;\s
				import mainpkg.Outer.Inner.*;
				//valid import - no error in 5.0
				
				/**\s
				 * {@link MostInner}
				 *\s
				 */\s
				public class Valid2 {\s
				      void m() { }\s
				}
				"""
			,
			"pkg3/Invalid3.java",
			"""
				package pkg3;\s
				import mainpkg.Outer.*;
				//invalid import: expecting warning / error
				
				/**\s
				 * {@link MostInner} -- error/warning \s
				 *\s
				 */\s
				public class Invalid3 {\s
				      void m() { }\s
				}
				"""
	};

	String error14 = new String (
		"""
			----------
			1. ERROR in pkg1\\Valid1.java (at line 6)
				* {@link MostInner}
				         ^^^^^^^^^
			Javadoc: Invalid member type qualification
			----------
			----------
			1. ERROR in pkg2\\Valid2.java (at line 6)
				* {@link MostInner}
				         ^^^^^^^^^
			Javadoc: Invalid member type qualification
			----------
			----------
			1. ERROR in pkg3\\Invalid3.java (at line 2)
				import mainpkg.Outer.*;
				       ^^^^^^^^^^^^^
			The import mainpkg.Outer is never used
			----------
			2. ERROR in pkg3\\Invalid3.java (at line 6)
				* {@link MostInner} -- error/warning \s
				         ^^^^^^^^^
			Javadoc: MostInner cannot be resolved to a type
			----------
			""");

	String error50 = new String (
			"""
				----------
				1. ERROR in pkg3\\Invalid3.java (at line 2)
					import mainpkg.Outer.*;
					       ^^^^^^^^^^^^^
				The import mainpkg.Outer is never used
				----------
				2. ERROR in pkg3\\Invalid3.java (at line 6)
					* {@link MostInner} -- error/warning \s
					         ^^^^^^^^^
				Javadoc: MostInner cannot be resolved to a type
				----------
				""");

	this.reportInvalidJavadocVisibility = CompilerOptions.PUBLIC;
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		runNegativeTest(units,error14);
	}
	else {
		runNegativeTest(units,error50, JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

public void testBug176027h_private() {
	// test embedded inner classes
	String[] units = new String[] {
			"mainpkg/Outer.java",
			"""
				package mainpkg;
				public class Outer {
				        public class Inner {
				        	public class MostInner{
				   \s
				        	}
				        }\s
				}
				"""
			,
			"pkg1/Valid1.java",
			"""
				package pkg1;\s
				import mainpkg.Outer.Inner.MostInner;
				// valid import - no error in 5.0
				
				/**\s
				 * {@link MostInner}
				 *\s
				 */\s
				public class Valid1 {\s
					/**\s
					 * {@link MostInner}\s
					 *\s
					 */\s
				      void m() { }\s
				}
				"""
			,
			"pkg2/Valid2.java",
			"""
				package pkg2;\s
				import mainpkg.Outer.Inner.*;
				//valid import - no error in 5.0
				
				/**\s
				 * {@link MostInner}
				 *\s
				 */\s
				public class Valid2 {\s
				      void m() { }\s
				}
				"""
			,
			"pkg3/Invalid3.java",
			"""
				package pkg3;\s
				import mainpkg.Outer.*;
				//invalid import: expecting warning / error
				
				/**\s
				 * {@link MostInner} -- error/warning \s
				 *\s
				 */\s
				public class Invalid3 {\s
				      void m() { }\s
				}
				"""
	};

	String error14 = new String(
			"""
				----------
				1. ERROR in pkg1\\Valid1.java (at line 6)
					* {@link MostInner}
					         ^^^^^^^^^
				Javadoc: Invalid member type qualification
				----------
				2. ERROR in pkg1\\Valid1.java (at line 11)
					* {@link MostInner}\s
					         ^^^^^^^^^
				Javadoc: Invalid member type qualification
				----------
				----------
				1. ERROR in pkg2\\Valid2.java (at line 6)
					* {@link MostInner}
					         ^^^^^^^^^
				Javadoc: Invalid member type qualification
				----------
				----------
				1. ERROR in pkg3\\Invalid3.java (at line 2)
					import mainpkg.Outer.*;
					       ^^^^^^^^^^^^^
				The import mainpkg.Outer is never used
				----------
				2. ERROR in pkg3\\Invalid3.java (at line 6)
					* {@link MostInner} -- error/warning \s
					         ^^^^^^^^^
				Javadoc: MostInner cannot be resolved to a type
				----------
				""");

	String error50 = new String(
			"""
				----------
				1. ERROR in pkg3\\Invalid3.java (at line 2)
					import mainpkg.Outer.*;
					       ^^^^^^^^^^^^^
				The import mainpkg.Outer is never used
				----------
				2. ERROR in pkg3\\Invalid3.java (at line 6)
					* {@link MostInner} -- error/warning \s
					         ^^^^^^^^^
				Javadoc: MostInner cannot be resolved to a type
				----------
				""");

	this.reportInvalidJavadocVisibility = CompilerOptions.PRIVATE;
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		runNegativeTest(units,error14);
	}
	else {
		runNegativeTest(units,error50, JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
}

/**
 * bug 177009: [javadoc] Missing Javadoc tag not reported
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=177009"
 */
public void testBug177009a() {
	String[] units = new String[] {
		"pkg/X.java",
		"""
			package pkg;
			
			public class X {
				public X(String str, int anInt) {
				}
			}
			""",
		"pkg/Y.java",
		"""
			package pkg;
			
			public class Y extends X {
				private static int myInt = 0;
				/**
				 * @see X#X(String, int)
				 */
				public Y(String str) {
					super(str, myInt);
				}
			}
			"""
	};
	this.reportMissingJavadocTags = CompilerOptions.WARNING;
	runConformTest(
			true,
			units,
			"""
				----------
				1. WARNING in pkg\\Y.java (at line 8)
					public Y(String str) {
					                ^^^
				Javadoc: Missing tag for parameter str
				----------
				""",
			null, null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
}

public void testBug177009b() {
	String[] units = new String[] {
		"pkg/X.java",
		"""
			package pkg;
			
			public class X {
				public X(String str, int anInt) {
				}
			}
			""",
		"pkg/Y.java",
		"""
			package pkg;
			
			public class Y extends X {
				/**
				 * @param str
				 * @param anInt
				 * @see X#X(String, int)
				 */
				public Y(String str, int anInt, int anotherInt) {
					super(str, anInt);
				}
			}
			"""
	};
	this.reportMissingJavadocTags = CompilerOptions.WARNING;
	runConformTest(true, units,
			"""
				----------
				1. WARNING in pkg\\Y.java (at line 9)
					public Y(String str, int anInt, int anotherInt) {
					                                    ^^^^^^^^^^
				Javadoc: Missing tag for parameter anotherInt
				----------
				""",
			null, null,
			JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
}

/**
 * bug 190970: [javadoc] "field never read locally" analysis should not consider javadoc
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=190970"
 */
public void testBug190970a() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.WARNING);
	this.runConformTest(
		true,
		new String[] {
			"X.java",
			"""
				public class X {
				private int unused1;
				
				/**
				 * Same value as {@link #unused1}
				 */
				private int unused2;
				}
				""",
		},
		null,
		customOptions,
		"""
			----------
			1. WARNING in X.java (at line 2)
				private int unused1;
				            ^^^^^^^
			The value of the field X.unused1 is not used
			----------
			2. WARNING in X.java (at line 7)
				private int unused2;
				            ^^^^^^^
			The value of the field X.unused2 is not used
			----------
			""",
		null, null,
		JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings
	);
}
// test unused methods
public void testBug190970b() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.WARNING);
	this.runConformTest(
		true,
		new String[] {
			"pkg/X.java",
			"""
				package pkg;
				
				public class X {
				private void unused1() {}
				/**
				 * Same value as {@link #unused1()}
				 */
				private void unused2() {}
				}
				""",
		},
		null,
		customOptions,
		"""
			----------
			1. WARNING in pkg\\X.java (at line 4)
				private void unused1() {}
				             ^^^^^^^^^
			The method unused1() from the type X is never used locally
			----------
			2. WARNING in pkg\\X.java (at line 8)
				private void unused2() {}
				             ^^^^^^^^^
			The method unused2() from the type X is never used locally
			----------
			""",
		null, null,
		JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings
	);
}
// test unused types
public void testBug190970c() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.WARNING);
	runConformTest(
		true,
	new String[] {
		"pkg/X.java",
		"""
			package pkg;
			
			public class X {
			private class unused1 {}
			/**
			 * {@link X.unused1}
			 */
			private class unused2 {}
			}
			""",
	},
	null,
	customOptions,
	"""
		----------
		1. WARNING in pkg\\X.java (at line 4)
			private class unused1 {}
			              ^^^^^^^
		The type X.unused1 is never used locally
		----------
		2. WARNING in pkg\\X.java (at line 8)
			private class unused2 {}
			              ^^^^^^^
		The type X.unused2 is never used locally
		----------
		""",
	null, null,
	JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings
	);
}

//	static { TESTS_PREFIX = "testBug191322"; }
/**
 * bug 191322: [javadoc] @see or @link reference to method without signature fails to resolve to base class method
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=191322"
 */
public void testBug191322() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void foo() {}
					/**
					 * {@link #foo}.
					 * @see #foo
					 */
					void goo() {}
				}
				""",
			"Y.java",
			"""
				class Y extends X {
					/**
					 * {@link #foo}
					 * @see #foo
					 */
					void hoo() {}
				}"""
		}
	);
}
public void testBug191322b() {
	runConformTest(
		new String[] {
			"b/X.java",
			"""
				package b;
				public class X {
					void foo() {}
				}
				class Y extends X {}
				class W extends Y {}
				class Z extends W {
					/**
					 * {@link #foo}
					 * @see #foo
					 */
					void hoo() {}
				}
				"""
		}
	);
}
public void testBug191322c() {
	runConformTest(
		new String[] {
			"c/X.java",
			"""
				package c;
				public interface X {
					void foo();
				}
				interface Y extends X {
					/**
					 * {@link #foo}
					 * @see #foo
					 */
					void hoo();
				}
				"""
		}
	);
}
public void testBug191322d() {
	runConformTest(
		new String[] {
			"d/X.java",
			"""
				package d;
				public interface X {
					void foo();
				}
				interface Y extends X {}
				abstract class W implements Y {}
				abstract class Z extends W {
					/**
					 * {@link #foo}
					 * @see #foo
					 */
					void hoo() {}
				}
				"""
		}
	);
}
public void testBug191322e() {
	runConformTest(
		new String[] {
			"e/X.java",
			"""
				package e;
				public class X {
					void foo() {}
					class Y {
						/**
						 * {@link #foo}
						 * @see #foo
						 */
						void hoo() {}
					}
				}
				"""
		}
	);
}
public void testBug191322f() {
	runConformTest(
		new String[] {
			"f/X.java",
			"""
				package f;
				public class X {
					void foo() {}
					void foo(String str) {}
				}
				class Y extends X {
					/**
					 * {@link #foo}
					 * @see #foo
					 */
					void hoo() {}
				}
				"""
		}
	);
}
public void testBug191322g() {
	runConformTest(
		new String[] {
			"g/X.java",
			"""
				package g;
				public class X {
					void foo(String str) {}
					void foo(int x) {}
				}
				class Y extends X {
					/**
					 * {@link #foo}
					 * @see #foo
					 */
					void hoo() {}
				}
				"""
		}
	);
}
public void testBug191322h() {
	runConformTest(
		new String[] {
			"h/X.java",
			"""
				package h;
				public class X {
					void foo(String str) {}
				}
				class Y extends X {
					/**
					 * {@link #foo}
					 * @see #foo
					 */
					void hoo() {}
				}
				"""
		}
	);
}
public void testBug191322i() {
	runConformTest(
		new String[] {
			"i/X.java",
			"""
				package i;
				interface X {
					void foo();
				}
				interface Y {
					void foo(int i);
				}
				abstract class Z implements X, Y {
					/**
					 * @see #foo
					 */
					void bar() {
					}
				}"""
		}
	);
}

/**
 * bug 195374: [javadoc] Missing Javadoc warning for required qualification for inner types at 1.4 level
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=195374"
 */
public void testBug195374() {
	String[] units = new String[] {
		"X.java",
		"""
			public class X {
				public static class Param {
			       /**
			         * warning expected when compliance < 1.5 {@link X#setParams(Param[])}
			         * no warning expected {@link X#setParams(X.Param[])}
			         */
			        public int getIndex() {
			                    return 0;
			        }
			    }
			    public void setParams(Param[] params) {
				}
			}
			"""
	};

	String error14 = new String(
		"""
			----------
			1. ERROR in X.java (at line 4)
				* warning expected when compliance < 1.5 {@link X#setParams(Param[])}
				                                                            ^^^^^^^
			Javadoc: Invalid member type qualification
			----------
			""");
	this.reportInvalidJavadocVisibility = CompilerOptions.PUBLIC;
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		runNegativeTest(units,error14);
	}
	else {
		runConformTest(units);
	}
}

/**
 * bug 207765: [javadoc] Javadoc warning on @see reference could be improved
 * test Ensure we have different message depending on tag value
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=207765"
 */
public void testBug207765() {
	runNegativeTest(
		new String[] {
			"pkg/X.java",
			"""
				package pkg;
				
				public class X {
					/**
					 * {@link "http://www.eclipse.org/}
					 * @see "http://www.eclipse.org/
					 */
					public void foo() {\s
					\s
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in pkg\\X.java (at line 5)
				* {@link "http://www.eclipse.org/}
				         ^^^^^^^^^^^^^^^^^^^^^^^^^
			Javadoc: Invalid reference
			----------
			2. ERROR in pkg\\X.java (at line 6)
				* @see "http://www.eclipse.org/
				       ^^^^^^^^^^^^^^^^^^^^^^^^
			Javadoc: Invalid URL reference. Double quote the reference or use the href syntax
			----------
			"""
	);
}

/**
 * bug 222900: [Javadoc] Missing description is warned if valid description is on a new line
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=222900"
 */
public void testBug222900a() {
	String[] units = new String[] {
		"X.java",
		"""
			/**
			* @since
			* 	description
			* @author
			* 	description
			* @version
			* 	description
			*/
			public class X {
				/**
				 * @param  aParam
				 *         description
				 * @return
				 *         description
				 * @since
				 *         description
				 * @throws NullPointerException
				 *         description
				 * @exception NullPointerException
				 *            description
				 * @serial
				 *         description
				 * @serialData
				 *         description
				 * @serialField
				 *         description
				 * @deprecated
				 *         description
				 */
				public String foo(String aParam) {
					return new String();
				}
			}
			"""
	};
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runConformTest(units);
}
public void testBug222900b() {
	String[] units = new String[] {
		"X.java",
		"""
			/**
			 * {@code
			 *        description}
			 * {@literal
			 *        description}
			*/
			public class X {
			}
			"""
	};
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runConformTest(units);
}
public void testBug222900c() {
	String[] units = new String[] {
		"X.java",
		"""
			/**
			 * Test the {@code} missing description
			 * Test the {@code
			 * } missing description
			 * Test the {@code X} with description
			 * Test the {@code
			 * public class X} with description
			*/
			public class X {
			}
			"""
	};
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runNegativeTest(units,
		"""
			----------
			1. ERROR in X.java (at line 2)
				* Test the {@code} missing description
				             ^^^^
			Javadoc: Description expected after @code
			----------
			2. ERROR in X.java (at line 3)
				* Test the {@code
				             ^^^^
			Javadoc: Description expected after @code
			----------
			"""
	);
}

/**
 * bug 222902: [Javadoc] Missing description should not be warned in some cases
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=222902"
 */
public void testBug222902() {
	String[] units = new String[] {
		"X.java",
		"""
			/**
			 * {@code}
			 * {@literal}
			 * @author
			 * @deprecated
			 * @since
			 * @version
			 * @generated
			 * @code
			 * @literal
			*/
			public class X {
				/**
				 * @param  aParam
				 * @return
				 * @throws NullPointerException
				 * @exception NullPointerException
				 */
				public String foo(String aParam) {
					return new String();
				}
				/**
				 * @serial
				 * @serialData
				 * @serialField
				 */
				Object field;
			}
			"""
	};
	this.reportInvalidJavadoc = CompilerOptions.WARNING;
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runConformTest(true, units,
		"""
			----------
			1. WARNING in X.java (at line 2)
				* {@code}
				    ^^^^
			Javadoc: Description expected after @code
			----------
			2. WARNING in X.java (at line 3)
				* {@literal}
				    ^^^^^^^
			Javadoc: Description expected after @literal
			----------
			3. WARNING in X.java (at line 4)
				* @author
				   ^^^^^^
			Javadoc: Description expected after @author
			----------
			4. WARNING in X.java (at line 5)
				* @deprecated
				   ^^^^^^^^^^
			Javadoc: Description expected after @deprecated
			----------
			5. WARNING in X.java (at line 6)
				* @since
				   ^^^^^
			Javadoc: Description expected after @since
			----------
			6. WARNING in X.java (at line 7)
				* @version
				   ^^^^^^^
			Javadoc: Description expected after @version
			----------
			7. WARNING in X.java (at line 14)
				* @param  aParam
				          ^^^^^^
			Javadoc: Description expected after this reference
			----------
			8. WARNING in X.java (at line 15)
				* @return
				   ^^^^^^
			Javadoc: Description expected after @return
			----------
			9. WARNING in X.java (at line 16)
				* @throws NullPointerException
				          ^^^^^^^^^^^^^^^^^^^^
			Javadoc: Description expected after this reference
			----------
			10. WARNING in X.java (at line 17)
				* @exception NullPointerException
				             ^^^^^^^^^^^^^^^^^^^^
			Javadoc: Description expected after this reference
			----------
			11. WARNING in X.java (at line 23)
				* @serial
				   ^^^^^^
			Javadoc: Description expected after @serial
			----------
			12. WARNING in X.java (at line 24)
				* @serialData
				   ^^^^^^^^^^
			Javadoc: Description expected after @serialData
			----------
			13. WARNING in X.java (at line 25)
				* @serialField
				   ^^^^^^^^^^^
			Javadoc: Description expected after @serialField
			----------
			""",
		null, null,
		JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings
	);
}

/**
 * bug 227730: [Javadoc] Missing description should not be warned for @inheritDoc
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=227730"
 */
public void testBug227730a() {
	String[] units = new String[] {
		"pkg/X.java",
		"""
			package pkg;
			
			public class X extends Object {
				/**
				 * {@inheritDoc}
				 */
				public String toString() {\s
					return "foo";
				}
			}
			"""
	};
	this.reportInvalidJavadoc = CompilerOptions.ERROR;
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runConformTest(units);
}

public void testBug227730b() {
	String[] units = new String[] {
		"pkg/X.java",
		"""
			package pkg;
			
			public class X extends Object {
				/**
				 * {@docRoot}
				 */
				public String toString() {\s
					return "foo";
				}
			}
			"""
	};
	this.reportInvalidJavadoc = CompilerOptions.ERROR;
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runConformTest(units);
}

/**
 * bug 233187: [javadoc] partially qualified inner types  should be warned
 * test verify that partial inner class qualification are warned as javadoc tools does
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=233187"
 */
public void testBug233187a() {
	String[] units = new String[] {
		"test/a/X.java",
		"""
			package test.a;
			
			public class X {
			   public static class Y {
			        public static class Z {\s
			            /**
			             * The position in the new method signature depends on
			             * the position in the array passed to
			             * {@link X.Y#foo(test.a.X.Y.Z[])} OK for javadoc tool
			             * {@link X.Y#foo(test.a.X.Y.Z)} KO for javadoc tool
			             * {@link X.Y#foo(no_test.a.X.Y.Z[])} KO for javadoc tool
			             * {@link X.Y#foo(Y.Z[])} KO for javadoc tool
			             * {@link test.a.X.Y#foo(Y.Z[])} KO for javadoc tool
			             */
			            public int bar() {
			                return 0;
			            }
			        }
			
			        public void foo(Z[] params) {
			        }
			    }
			}
			"""
	};
	runNegativeTest(units,
		"""
			----------
			1. ERROR in test\\a\\X.java (at line 10)
				* {@link X.Y#foo(test.a.X.Y.Z)} KO for javadoc tool
				             ^^^
			Javadoc: The method foo(X.Y.Z[]) in the type X.Y is not applicable for the arguments (X.Y.Z)
			----------
			2. ERROR in test\\a\\X.java (at line 11)
				* {@link X.Y#foo(no_test.a.X.Y.Z[])} KO for javadoc tool
				                 ^^^^^^^^^^^^^^^
			Javadoc: no_test[] cannot be resolved to a type
			----------
			3. ERROR in test\\a\\X.java (at line 12)
				* {@link X.Y#foo(Y.Z[])} KO for javadoc tool
				                 ^^^
			Javadoc: Invalid member type qualification
			----------
			4. ERROR in test\\a\\X.java (at line 13)
				* {@link test.a.X.Y#foo(Y.Z[])} KO for javadoc tool
				                        ^^^
			Javadoc: Invalid member type qualification
			----------
			"""
	);
}
public void testBug233187b() {
	runNegativeTest(
		new String[] {
			"test/b/X.java",
			"""
				package test.b;
				
				public class X {
				   public static class Y {
				        public static class Z {\s
				            /**
				             * The position in the new method signature depends on
				             * the position in the array passed to
				             * {@link X.Y#foo(test.b.X.Y.Z)} OK for javadoc tool
				            * {@link X.Y#foo(test.b.X.Y.Z[])} KO for javadoc tool
				             * {@link X.Y#foo(no_test.b.X.Y.Z)} KO for javadoc tool
				             * {@link X.Y#foo(Y.Z)} KO for javadoc tool
				             * {@link test.b.X.Y#foo(Y.Z)} KO for javadoc tool
				             */
				            public int bar() {
				                return 0;
				            }
				        }
				
				        public void foo(Z params) {
				        }
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in test\\b\\X.java (at line 10)
				* {@link X.Y#foo(test.b.X.Y.Z[])} KO for javadoc tool
				             ^^^
			Javadoc: The method foo(X.Y.Z) in the type X.Y is not applicable for the arguments (X.Y.Z[])
			----------
			2. ERROR in test\\b\\X.java (at line 11)
				* {@link X.Y#foo(no_test.b.X.Y.Z)} KO for javadoc tool
				                 ^^^^^^^^^^^^^^^
			Javadoc: no_test cannot be resolved to a type
			----------
			3. ERROR in test\\b\\X.java (at line 12)
				* {@link X.Y#foo(Y.Z)} KO for javadoc tool
				                 ^^^
			Javadoc: Invalid member type qualification
			----------
			4. ERROR in test\\b\\X.java (at line 13)
				* {@link test.b.X.Y#foo(Y.Z)} KO for javadoc tool
				                        ^^^
			Javadoc: Invalid member type qualification
			----------
			"""
	);
}
public void testBug233187c() {
	runConformTest(
		new String[] {
			"test/c/X.java",
			"""
				package test.c;
				
				public class X {
					static class Y {\s
					}
					void foo(Y y) {}
					/**
					 * @see #foo(X.Y)
					 */
					void bar() {}
				}
				"""
		}
	);
}

/**
 * bug 233887: Build of Eclipse project stop by NullPointerException and will not continue on Eclipse version later than 3.4M7
 * test Ensure that no NPE is raised when a 1.5 param tag syntax is incorrectly used on a fiel with an initializer
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=233887"
 */
public void testBug233887() {
	String expectedError = this.complianceLevel <= ClassFileConstants.JDK1_4 ?
		"""
			----------
			1. ERROR in NPETest.java (at line 5)
				* @param <name> <description>
				         ^^^^^^
			Javadoc: Invalid param tag name
			----------
			"""
	:
		"""
			----------
			1. ERROR in NPETest.java (at line 5)
				* @param <name> <description>
				   ^^^^^
			Javadoc: Unexpected tag
			----------
			""";
	runNegativeTest(
		new String[] {
			"NPETest.java",
			"""
				public class NPETest {
					public NPETest() {
					}
					/**
					 * @param <name> <description>
					 */
					private static final int MAX = 50;
				
				}
				"""
		},
		expectedError,
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * bug 237937: [javadoc] Wrong "Javadoc: Malformed link reference" if href label contains //
 * test Ensure that no warning is raised when href label contains '//'
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=237937"
 */
public void testBug237937() {
	runConformTest(
		new String[] {
			"Link.java",
			"""
				/**
				 * @see <a href="http://www.eclipse.org/">http://www.eclipse.org</a>
				 * @see <a href="http://www.eclipse.org/">//</a>
				 */
				public class Link {}
				"""
		}
	);
}

/**
 * bug 246712: [javadoc] Unexpected warning about missing parameter doc in case of @inheritDoc
 * test Ensure inline tag are considered as description
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=246712"
 */
public void testBug246712() {
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				
					/**
					 * Do something more.
					 *\s
					 * @param monitor The monitor
					 * @return {@link String X}
					 */
					String foo(Object monitor) {
						return "X";
					}
				}
				""",
			"Y.java",
			"""
				public class Y extends X {
				
					/**
					 * Do something more.
					 *\s
					 * {@inheritDoc}
					 *\s
					 * @param monitor {@inheritDoc}
					 * @return {@link String Y}
					 */
					String foo(Object monitor) {
						return "Y";
					}
				}
				"""
		}
	);
}
public void testBug246712b() {
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runConformTest(
		new String[] {
			"X.java",
			"""
				/**
				 * @author {@link String}
				 * @since {@link String}
				 * @version {@link String}
				 * @deprecated {@link String}
				*/
				public class X {
					/**
					 * @return {@link String}
					 * @since {@link String}
					 * @throws  Exception {@link String}
					 * @exception Exception {@link String}
					 * @serial {@link String}
					 * @serialData {@link String}
					 * @serialField {@link String}
					 * @deprecated {@link String}
					 */
					public String foo(String aParam) throws Exception {
						return new String();
					}
				}"""
		}
	);
}
// duplicate
public void testBug246715() {
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				
					final static int WAIT_YES = 0;
					final static int WAIT_NO = 1;
				\t
					/**
					 * Do something more.
					 *\s
					 * @param waitFlag {@link #WAIT_YES} or {@link #WAIT_NO}
					 */
					String foo(int waitFlag) {
						return "X";
					}
				}
				"""
		}
	);
}

/**
 * bug 254825: [javadoc] compile error when referencing outer param from inner class javadoc
 * test Ensure that local variable reference does not imply missing compiler implementation error
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=254825"
 */
public void testBug254825() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {
				  public Object foo(Object o) {\s
				    return new Object() {
				      /** @see #o */
				      public void x() {}
				    };
				  }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				/** @see #o */
				          ^
			Javadoc: o cannot be resolved or is not a field
			----------
			"""
	);
}
public void testBug254825b() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {
				  /** @see #o */
				  public Object foo(Object o) { return null; }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				/** @see #o */
				          ^
			Javadoc: o cannot be resolved or is not a field
			----------
			"""
	);
}


/**
 * bug 258798: [1.5][compiler] Return type should be erased after unchecked conversion during inference
 * test Fix for this bug had side effects while reporting missing tags in javadoc comments.<br>
 * Following tests have been written to verify that noticed issues have been solved:
 * <ol>
 * <li>missing tags should be reported even when the method/constructor has
 * 	a &#064;see reference on itself</li>
 * <li>missing tag should be reported when superclass constructor has different
 * 	arguments (even if they are compatible)</li>
 * <li>missing tag should not be reported when method arguments are the same
 * 	even when the type argument is not the same</li>
 * </ol>
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=258798"
 */
public void testBug258798_1() {
	this.reportMissingJavadocTags = CompilerOptions.WARNING;
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				/**
				* @see #X(int)
				*/
				X(int i) {
				}
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 5)
				X(int i) {
				      ^
			Javadoc: Missing tag for parameter i
			----------
			"""
	);
}
public void testBug258798_2a() {
	this.reportMissingJavadocTags = CompilerOptions.WARNING;
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				X(int i) {}
				}
				class Y extends X {
				/** @see X#X(int) */
				Y(double d) { super(0); }
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 6)
				Y(double d) { super(0); }
				         ^
			Javadoc: Missing tag for parameter d
			----------
			"""
	);
}
public void testBug258798_2b() {
	this.reportMissingJavadocTags = CompilerOptions.WARNING;
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.*;
					public class X<T> {
					X(ArrayList<T> alt) {}
					}
					class Y<U> extends X<U> {
					/** @see X#X(ArrayList) */
					Y(List<U> lu) { super(null); }
					}
					"""
			},
			"""
				----------
				1. WARNING in X.java (at line 7)
					Y(List<U> lu) { super(null); }
					          ^^
				Javadoc: Missing tag for parameter lu
				----------
				"""
		);
	}
}
public void testBug258798_2c() {
	this.reportMissingJavadocTags = CompilerOptions.WARNING;
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.*;
					public class X<T> {
					X(Object o) {}
					}
					class Y<U> extends X<U> {
					/** @see X#X(Object) */
					Y(List<U> lu) { super(lu); }
					}
					"""
			},
			"""
				----------
				1. WARNING in X.java (at line 7)
					Y(List<U> lu) { super(lu); }
					          ^^
				Javadoc: Missing tag for parameter lu
				----------
				"""
		);
	}
}
public void testBug258798_3() {
	this.reportMissingJavadocTags = CompilerOptions.WARNING;
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.*;
					public class X<T> {
					X(List<T> lt) {}
					}
					class Y<U> extends X<U> {
					/** @see X#X(List) */
					Y(List<U> lu) { super(null); }
					}
					"""
			}
		);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=247037, make sure that we complain when @inheritdoc
// is used where it is outlawed by the specs. This test verifies that we complain when @inheritDoc
// is used with classes and interfaces.
public void testBug247037() {
	this.reportMissingJavadocTags = CompilerOptions.ERROR;
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				/**
				 * {@inheritDoc}
				 */
				public class X {
				}
				/**
				 * {@inheritDoc}
				 */\
				interface Blah {
				    void BlahBlah();
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				* {@inheritDoc}
				    ^^^^^^^^^^
			Javadoc: Unexpected tag
			----------
			2. ERROR in X.java (at line 7)
				* {@inheritDoc}
				    ^^^^^^^^^^
			Javadoc: Unexpected tag
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=247037, make sure that we complain when @inheritdoc
//is used where it is outlawed by the specs. Here we test that when @inheritDoc is applied to a
// field or constructor, we complain.
public void testBug247037b() {
	this.reportMissingJavadocTags = CompilerOptions.ERROR;
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				}
				class Y extends X {
				    /**
				     * {@inheritDoc}
				    */
				    public int field = 10;
				    /**
				     * @param x {@inheritDoc}
				    */
				    Y(int x) {}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				* {@inheritDoc}
				    ^^^^^^^^^^
			Javadoc: Unexpected tag
			----------
			2. ERROR in X.java (at line 9)
				* @param x {@inheritDoc}
				             ^^^^^^^^^^
			Javadoc: Unexpected tag
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=247037, make sure that we complain when @inheritdoc
//is used where it is outlawed by the specs. In this test we test the use of @inheritedDoc in some
// block tags.
public void testBug247037c() {
	this.reportMissingJavadocTags = CompilerOptions.ERROR;
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    /**
				     * @since 1.0
				     * @return Blah
				     * @param blah Blah Blah
				     * @throws Exception When something is wrong
				     */
				    public int m(int blah) throws Exception {
				        return 0;
				    }
				}
				class Y extends X {
				    /**
				     * @param blah {@inheritDoc}
				     * @return {@inheritDoc}
				     * @since {@inheritDoc}
				     * @author {@inheritDoc}
				     * @see {@inheritDoc}
				     * @throws Exception {@inheritDoc}
				     * @exception Exception {@inheritDoc}
				     */
				    public int m(int blah) throws Exception {
						return 1;
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 16)
				* @since {@inheritDoc}
				           ^^^^^^^^^^
			Javadoc: Unexpected tag
			----------
			2. ERROR in X.java (at line 17)
				* @author {@inheritDoc}
				            ^^^^^^^^^^
			Javadoc: Unexpected tag
			----------
			3. ERROR in X.java (at line 18)
				* @see {@inheritDoc}
				   ^^^
			Javadoc: Missing reference
			----------
			4. ERROR in X.java (at line 18)
				* @see {@inheritDoc}
				         ^^^^^^^^^^
			Javadoc: Unexpected tag
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=247037, make sure that we complain when @inheritdoc
// is used where it is outlawed by the specs. Test to verify that every bad use of @inheritDoc triggers
// a message from the compiler
public void testBug247037d() {
	this.reportMissingJavadocTags = CompilerOptions.ERROR;
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				}
				class Y extends X {
				    /**
				     * @param blah {@inheritDoc}
				     * @return {@inheritDoc}
				     * @author {@inheritDoc}
				     */
				    public int n(int blah) {
						return 1;
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				* @param blah {@inheritDoc}
				                ^^^^^^^^^^
			Javadoc: Unexpected tag
			----------
			2. ERROR in X.java (at line 6)
				* @return {@inheritDoc}
				            ^^^^^^^^^^
			Javadoc: Unexpected tag
			----------
			3. ERROR in X.java (at line 7)
				* @author {@inheritDoc}
				            ^^^^^^^^^^
			Javadoc: Unexpected tag
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
/**
 * bug 267833:[javadoc] Custom tags should not be allowed for inline tags
 * test Ensure that a warning is raised when customs tags are used as inline tags
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=267833"
 */
public void testBug267833() {
	runConformTest(
		new String[] {
			"X.java",
			"""
				/**
				* Invalid custom tag {@custom "Invalid"}  \s
				* @custom "Valid"
				*/
				public class X {
				}"""
		});
}
/**
 * Additional test for bug 267833
 * test Ensure that the JavadocTagConstants.JAVADOC_TAG_TYPE array is up to date with the other arrays, such as
 *  JavadocTagConstants.TAG_NAMES, JavadocTagConstants.INLINE_TAGS and JavadocTagConstants.BLOCK_TAGS
 */
public void testBug267833_2() {

	assertEquals(JavadocTagConstants.TAG_NAMES.length,JavadocTagConstants.JAVADOC_TAG_TYPE.length);

	int tagsLength = JavadocTagConstants.TAG_NAMES.length;
	nextTag:for (int index=0; index < tagsLength; index++) {
		char[] tagName = JavadocTagConstants.TAG_NAMES[index];
		if (tagName.length > 0) {
			for (int i=0; i < JavadocTagConstants.BLOCK_TAGS_LENGTH; i++) {
				int length = JavadocTagConstants.BLOCK_TAGS[i].length;
				for (int j=0; j < length; j++) {
					if (tagName == JavadocTagConstants.BLOCK_TAGS[i][j]) {
						int tagType = JavadocTagConstants.JAVADOC_TAG_TYPE[index];
						assertTrue((tagType & JavadocTagConstants.TAG_TYPE_BLOCK) != 0);
						continue nextTag;
					}
				}
			}
			for (int i=0; i < JavadocTagConstants.INLINE_TAGS_LENGTH; i++) {
				int length = JavadocTagConstants.INLINE_TAGS[i].length;
				for (int j=0; j < length; j++) {
					if (tagName == JavadocTagConstants.INLINE_TAGS[i][j]) {
						int tagType = JavadocTagConstants.JAVADOC_TAG_TYPE[index];
						assertTrue((tagType & JavadocTagConstants.TAG_TYPE_INLINE) != 0);
						continue nextTag;
					}
				}
			}
			for (int i=0; i < JavadocTagConstants.IN_SNIPPET_TAGS_LENGTH; i++) {
				int length = JavadocTagConstants.IN_SNIPPET_TAGS[i].length;
				for (int j=0; j < length; j++) {
					if (tagName == JavadocTagConstants.IN_SNIPPET_TAGS[i][j]) {
						assertEquals(JavadocTagConstants.JAVADOC_TAG_TYPE[index], JavadocTagConstants.TAG_TYPE_IN_SNIPPET);
						continue nextTag;
					}
				}
			}
		}
		assertEquals(JavadocTagConstants.JAVADOC_TAG_TYPE[index], JavadocTagConstants.TAG_TYPE_NONE);
	}
}
/**
 * Additional test for bug 267833
 * test Ensure that a warning is raised when block tags are used as inline tags.
 */
public void testBug267833_3() {
	if(this.complianceLevel >= ClassFileConstants.JDK16) {
		return;
	}
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				/**\s
				* Description {@see String} , {@return int}, {@since 1.0}, {@param i}, {@throws NullPointerException}
				* and more {@author jay}, {@category cat}, {@deprecated}, {@exception NullPointerException}, {@version 1.1}
				* and more {@since 1.0}, {@serial 0L}, {@serialData data}, {@serialField field}
				* @param i
				* @return value
				* @throws NullPointerException\s
				*/
				public int foo(int i) {
					return 0;
				}
				}
				""" },
			"""
				----------
				1. ERROR in X.java (at line 3)
					* Description {@see String} , {@return int}, {@since 1.0}, {@param i}, {@throws NullPointerException}
					                ^^^
				Javadoc: Unexpected tag
				----------
				2. ERROR in X.java (at line 3)
					* Description {@see String} , {@return int}, {@since 1.0}, {@param i}, {@throws NullPointerException}
					                                ^^^^^^
				Javadoc: Unexpected tag
				----------
				3. ERROR in X.java (at line 3)
					* Description {@see String} , {@return int}, {@since 1.0}, {@param i}, {@throws NullPointerException}
					                                               ^^^^^
				Javadoc: Unexpected tag
				----------
				4. ERROR in X.java (at line 3)
					* Description {@see String} , {@return int}, {@since 1.0}, {@param i}, {@throws NullPointerException}
					                                                             ^^^^^
				Javadoc: Unexpected tag
				----------
				5. ERROR in X.java (at line 3)
					* Description {@see String} , {@return int}, {@since 1.0}, {@param i}, {@throws NullPointerException}
					                                                                         ^^^^^^
				Javadoc: Unexpected tag
				----------
				6. ERROR in X.java (at line 4)
					* and more {@author jay}, {@category cat}, {@deprecated}, {@exception NullPointerException}, {@version 1.1}
					             ^^^^^^
				Javadoc: Unexpected tag
				----------
				7. ERROR in X.java (at line 4)
					* and more {@author jay}, {@category cat}, {@deprecated}, {@exception NullPointerException}, {@version 1.1}
					                            ^^^^^^^^
				Javadoc: Unexpected tag
				----------
				8. ERROR in X.java (at line 4)
					* and more {@author jay}, {@category cat}, {@deprecated}, {@exception NullPointerException}, {@version 1.1}
					                                             ^^^^^^^^^^
				Javadoc: Unexpected tag
				----------
				9. ERROR in X.java (at line 4)
					* and more {@author jay}, {@category cat}, {@deprecated}, {@exception NullPointerException}, {@version 1.1}
					                                                            ^^^^^^^^^
				Javadoc: Unexpected tag
				----------
				10. ERROR in X.java (at line 4)
					* and more {@author jay}, {@category cat}, {@deprecated}, {@exception NullPointerException}, {@version 1.1}
					                                                                                               ^^^^^^^
				Javadoc: Unexpected tag
				----------
				11. ERROR in X.java (at line 5)
					* and more {@since 1.0}, {@serial 0L}, {@serialData data}, {@serialField field}
					             ^^^^^
				Javadoc: Unexpected tag
				----------
				12. ERROR in X.java (at line 5)
					* and more {@since 1.0}, {@serial 0L}, {@serialData data}, {@serialField field}
					                           ^^^^^^
				Javadoc: Unexpected tag
				----------
				13. ERROR in X.java (at line 5)
					* and more {@since 1.0}, {@serial 0L}, {@serialData data}, {@serialField field}
					                                         ^^^^^^^^^^
				Javadoc: Unexpected tag
				----------
				14. ERROR in X.java (at line 5)
					* and more {@since 1.0}, {@serial 0L}, {@serialData data}, {@serialField field}
					                                                             ^^^^^^^^^^^
				Javadoc: Unexpected tag
				----------
				""");
}
/**
 * Additional test for bug 267833 and https://github.com/eclipse-jdt/eclipse.jdt.core/issues/795
 * For java 16+:
 * 1) Ensure that a warning is raised when block tags are used as inline tags.
 * 2) Ensure there is no error reported for return tag used inline
 * 3) TODO: ensure  there is no error reported for duplicated return tag if it is used inline and as block
 */
public void testBug267833_3a() {
	if(this.complianceLevel < ClassFileConstants.JDK16) {
		return;
	}
	runNegativeTest(
			new String[] {
					"X.java",
					"""
						public class X {
						/**\s
						* Description {@see String} , {@return int}, {@since 1.0}, {@param i}, {@throws NullPointerException}
						* and more {@author jay}, {@category cat}, {@deprecated}, {@exception NullPointerException}, {@version 1.1}
						* and more {@since 1.0}, {@serial 0L}, {@serialData data}, {@serialField field}
						* @param i
						* @return value
						* @throws NullPointerException\s
						*/
						public int foo(int i) {
							return 0;
						}
						}
						""" },
			"""
				----------
				1. ERROR in X.java (at line 3)
					* Description {@see String} , {@return int}, {@since 1.0}, {@param i}, {@throws NullPointerException}
					                ^^^
				Javadoc: Unexpected tag
				----------
				2. ERROR in X.java (at line 3)
					* Description {@see String} , {@return int}, {@since 1.0}, {@param i}, {@throws NullPointerException}
					                                               ^^^^^
				Javadoc: Unexpected tag
				----------
				3. ERROR in X.java (at line 3)
					* Description {@see String} , {@return int}, {@since 1.0}, {@param i}, {@throws NullPointerException}
					                                                             ^^^^^
				Javadoc: Unexpected tag
				----------
				4. ERROR in X.java (at line 3)
					* Description {@see String} , {@return int}, {@since 1.0}, {@param i}, {@throws NullPointerException}
					                                                                         ^^^^^^
				Javadoc: Unexpected tag
				----------
				5. ERROR in X.java (at line 4)
					* and more {@author jay}, {@category cat}, {@deprecated}, {@exception NullPointerException}, {@version 1.1}
					             ^^^^^^
				Javadoc: Unexpected tag
				----------
				6. ERROR in X.java (at line 4)
					* and more {@author jay}, {@category cat}, {@deprecated}, {@exception NullPointerException}, {@version 1.1}
					                            ^^^^^^^^
				Javadoc: Unexpected tag
				----------
				7. ERROR in X.java (at line 4)
					* and more {@author jay}, {@category cat}, {@deprecated}, {@exception NullPointerException}, {@version 1.1}
					                                             ^^^^^^^^^^
				Javadoc: Unexpected tag
				----------
				8. ERROR in X.java (at line 4)
					* and more {@author jay}, {@category cat}, {@deprecated}, {@exception NullPointerException}, {@version 1.1}
					                                                            ^^^^^^^^^
				Javadoc: Unexpected tag
				----------
				9. ERROR in X.java (at line 4)
					* and more {@author jay}, {@category cat}, {@deprecated}, {@exception NullPointerException}, {@version 1.1}
					                                                                                               ^^^^^^^
				Javadoc: Unexpected tag
				----------
				10. ERROR in X.java (at line 5)
					* and more {@since 1.0}, {@serial 0L}, {@serialData data}, {@serialField field}
					             ^^^^^
				Javadoc: Unexpected tag
				----------
				11. ERROR in X.java (at line 5)
					* and more {@since 1.0}, {@serial 0L}, {@serialData data}, {@serialField field}
					                           ^^^^^^
				Javadoc: Unexpected tag
				----------
				12. ERROR in X.java (at line 5)
					* and more {@since 1.0}, {@serial 0L}, {@serialData data}, {@serialField field}
					                                         ^^^^^^^^^^
				Javadoc: Unexpected tag
				----------
				13. ERROR in X.java (at line 5)
					* and more {@since 1.0}, {@serial 0L}, {@serialData data}, {@serialField field}
					                                                             ^^^^^^^^^^^
				Javadoc: Unexpected tag
				----------
				14. ERROR in X.java (at line 7)
					* @return value
					   ^^^^^^
				Javadoc: Duplicate tag for return type
				----------
				""");
}

/**
 * bug 281609: [javadoc] "Javadoc: Invalid reference" warning for @link to Java package
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=281609"
 */
public void testBug281609a() {
	runNegativeTest(
		new String[] {
			"pkg/X.java",
			"""
				package pkg;
				
				public class X {
					/**
					 * @see java
					 * @see java.lang
					 * @see PKG
					 * @see pkg
					 */
					public void foo() {\s
					\s
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in pkg\\X.java (at line 7)
				* @see PKG
				       ^^^
			Javadoc: PKG cannot be resolved to a type
			----------
			"""
	);
}
public void testBug281609b() {
	runConformTest(
		new String[] {
			"x/y/z/X.java",
			"""
				package x.y.z;
				
				public class X {
					/**
					 * @see java
					 * @see java.lang
					 * @see x
					 * @see x.y
					 * @see x.y.z
					 */
					public void foo() {\s
					\s
					}
				}
				"""
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=292510
// Test to verify that partial types are demarcated correctly while
// annotating a deprecated type error in javadoc.
public void testBug292510() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportDeprecationInDeprecatedCode, CompilerOptions.ENABLED);
	runNegativeTest(
		true,
		new String[] {
			"X.java",
			"""
				/**  @deprecated */
				public class X {
				    public class XX {
				        public class XXX {
				        }
				    }
				}
				""",
			"Y.java",
			"""
				/**
				 * @see X.XX.XXX
				 */
				public class Y {
				}
				"""},
		null,
		options,
		"""
			----------
			1. ERROR in Y.java (at line 2)
				* @see X.XX.XXX
				       ^
			Javadoc: The type X is deprecated
			----------
			2. ERROR in Y.java (at line 2)
				* @see X.XX.XXX
				       ^^^^
			Javadoc: The type X.XX is deprecated
			----------
			3. ERROR in Y.java (at line 2)
				* @see X.XX.XXX
				       ^^^^^^^^
			Javadoc: The type X.XX.XXX is deprecated
			----------
			""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=316782
// Test to verify that turning on process annotations doesn't turn on javadoc check
public void testBug316782() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) {
		return;
	}
	this.processAnnotations = CompilerOptions.ENABLED;
	this.docCommentSupport = CompilerOptions.DISABLED;
	runConformTest(
		new String[] {
			"X.java",
			"""
				/**  @see X.XX.XXX */
				public class X {
				/**  @see X.XX.XXX */
				    public void foo() { }
				}
				"""
		});
}
/**
 * bug 222188: [javadoc] Incorrect usage of inner type not reported
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=222188"
 */
public void testBug222188a() {
	// case 1: partially qualified reference in another package
	String[] units = new String[] {
		"pack/Test.java",
		"""
			package pack;
			public class Test {
			        public interface Inner { }
			}
			"""
		,
		"pack2/X.java",
		"""
			package pack2;
			import pack.Test;
			public class X {
			/**
			 * See also {@link Test.Inner} -- error/warning\s
			 */
			     public void m() { }
			}
			"""
	};
	runNegativeTest(units,
		"""
			----------
			1. ERROR in pack2\\X.java (at line 5)
				* See also {@link Test.Inner} -- error/warning\s
				                  ^^^^^^^^^^
			Javadoc: Invalid member type qualification
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug222188b() {
	// case 2: fully but invalid qualified reference in another package
	String[] units = new String[] {
		"pack/Test.java",
		"""
			package pack;
			public class Test {
			        public interface Inner { }
			}
			"""
		,
		"pack2/X.java",
		"""
			package pack2;
			public class X {
			/**
			 * See also {@link pack.Test.Inners} -- error/warning\s
			 */
			     public void m() { }
			}
			"""
	};
	runNegativeTest(units,
		"""
			----------
			1. ERROR in pack2\\X.java (at line 4)
				* See also {@link pack.Test.Inners} -- error/warning\s
				                  ^^^^^^^^^^^^^^^^
			Javadoc: pack.Test.Inners cannot be resolved to a type
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

/**
 * bug 221539: [javadoc] doesn't detect non visible inner class
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=221539"
 */
public void testBug221539a() {
	// partially qualified reference in the same package
	String[] units = new String[] {
		"p/Test.java",
		"""
			package p;
			/**
			 * {@link Test.Inner} not ok for Javadoc
			 * {@link Foo.Inner} ok for Javadoc
			 */
			public class Test extends Foo {
			}
			"""
		,
		"p/Foo.java",
		"""
			package p;
			public class Foo {
				static class Inner {}
			}
			"""
	};
	runNegativeTest(units,
		"""
			----------
			1. ERROR in p\\Test.java (at line 3)
				* {@link Test.Inner} not ok for Javadoc
				         ^^^^^^^^^^
			Javadoc: Invalid member type qualification
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}
public void testBug221539b() {
	// partially qualified reference in different package
	String[] units = new String[] {
		"p1/Test.java",
		"""
			package p1;
			import p2.Foo;
			/**
			 * {@link Test.Inner} not ok for Javadoc
			 * {@link Foo.Inner} not ok Javadoc
			 * {@link p2.Foo.Inner} ok for Javadoc as fully qualified
			 */
			public class Test extends Foo {
			}
			"""
		,
		"p2/Foo.java",
		"""
			package p2;
			public class Foo {
				public static class Inner {}
			}
			"""
	};
	runNegativeTest(units,
		"""
			----------
			1. ERROR in p1\\Test.java (at line 4)
				* {@link Test.Inner} not ok for Javadoc
				         ^^^^^^^^^^
			Javadoc: Invalid member type qualification
			----------
			2. ERROR in p1\\Test.java (at line 5)
				* {@link Foo.Inner} not ok Javadoc
				         ^^^^^^^^^
			Javadoc: Invalid member type qualification
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError
	);
}

public void testBug221539c() {
	// case 3: partially qualified references are valid within the same CU
	this.reportInvalidJavadocVisibility = CompilerOptions.PRIVATE;
	runConformTest(
		new String[] {
			"pack/Test.java",
			"""
				package pack;
				/**
				 * @see Inner.Level2.Level3
				 * @see Test.Inner.Level2.Level3
				 */
				public class Test {
					public class Inner {
						/**
						 * @see Level3
						 * @see Level2.Level3
						 * @see Inner.Level2.Level3
						 * @see Test.Inner.Level2.Level3
						 */
						public class Level2 {
							class Level3 {
							}
						}
					}
				}
				"""
		}
	);
}

public void testBug382606() {
	runConformTest(
			new String[] {
				"pack/A.java",
				"""
					package pack;
					/**
					* @see A
					*/
					public interface A {
					}
					/**
					* @see #B()
					*/
					class B {
					 B() {}
					
					 public void foo(){
					     new B();
					 }
					}
					"""
			}
		);
}

/**
 * bug 206345: [javadoc] compiler should not interpret contents of {@literal}
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=206345"
 */
public void testBug206345a() {
	// @litteral tags display text without interpreting the text as HTML markup or nested javadoc tags
	String[] units = new String[] {
		"pkg/X.java",
		"""
			package pkg;
			
			public class X extends Object {
				/**
				 * This is {@literal raw text:
				 * 			{@link BadLink} is just text}
				 * 			{@link expected_error}
				 * }
				 */
				public String toString() {\s
					return "foo";
				}
			}
			"""
	};
	this.reportInvalidJavadoc = CompilerOptions.ERROR;
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runNegativeTest(units,
		"""
			----------
			1. ERROR in pkg\\X.java (at line 7)\r
				* 			{@link expected_error}\r
				  			       ^^^^^^^^^^^^^^
			Javadoc: expected_error cannot be resolved to a type
			----------
			""");
}
public void testBug206345b() {
	// same for @code tags
	String[] units = new String[] {
		"pkg/X.java",
		"""
			package pkg;
			
			public class X extends Object {
				/**
				 * This is {@code raw text:
				 * 			{@link BadLink} is just text}
				 * 			{@link expected_error}
				 * }
				 */
				public String toString() {\s
					return "foo";
				}
			}
			"""
	};
	this.reportInvalidJavadoc = CompilerOptions.ERROR;
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runNegativeTest(units,
		"""
			----------
			1. ERROR in pkg\\X.java (at line 7)\r
				* 			{@link expected_error}\r
				  			       ^^^^^^^^^^^^^^
			Javadoc: expected_error cannot be resolved to a type
			----------
			""");
}
public void testBug206345c() {
	// verify we still validate other syntax
	String[] units = new String[] {
		"pkg/X.java",
		"""
			package pkg;
			
			public class X extends Object {
				/**
				 * This is {@link raw text:
				 * 			{@link BadLink} is just text}
				 * 			{@link expected_error}
				 * }
				 */
				public String toString() {\s
					return "foo";
				}
			}
			"""
	};
	this.reportInvalidJavadoc = CompilerOptions.ERROR;
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runNegativeTest(units,
		"""
			----------
			1. ERROR in pkg\\X.java (at line 5)
				* This is {@link raw text:
				          ^^^^^^^^^^^^^^^^
			Javadoc: Missing closing brace for inline tag
			----------
			2. ERROR in pkg\\X.java (at line 5)
				* This is {@link raw text:
				                 ^^^
			Javadoc: raw cannot be resolved to a type
			----------
			3. ERROR in pkg\\X.java (at line 6)
				* 			{@link BadLink} is just text}
				  			       ^^^^^^^
			Javadoc: BadLink cannot be resolved to a type
			----------
			4. ERROR in pkg\\X.java (at line 7)
				* 			{@link expected_error}
				  			       ^^^^^^^^^^^^^^
			Javadoc: expected_error cannot be resolved to a type
			----------
			""");
}
public void testBug206345d() {
	String[] units = new String[] {
		"pkg/X.java",
		"""
			package pkg;
			
			public class X extends Object {
				/**
				 * This is {@literal raw text:
				 * 			{@link BadLink}}}} is just text}
				 * 			{@link expected_error}
				 * }
				 */
				public String toString() {\s
					return "foo";
				}
			}
			"""
	};
	this.reportInvalidJavadoc = CompilerOptions.ERROR;
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runNegativeTest(units,
		"""
			----------
			1. ERROR in pkg\\X.java (at line 7)
				* 			{@link expected_error}
				  			       ^^^^^^^^^^^^^^
			Javadoc: expected_error cannot be resolved to a type
			----------
			""");
}
public void testBug206345e() {
	String[] units = new String[] {
		"pkg/X.java",
		"""
			package pkg;
			
			public class X extends Object {
				/**
				 * This is {@code raw text:
				 * 			{{{{{{@link BadLink}}} is just text}
				 * @since 4.2
				 */
				public String toString() {\s
					return "foo";
				}
			}
			"""
	};
	this.reportInvalidJavadoc = CompilerOptions.ERROR;
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runNegativeTest(units,
		"""
			----------
			1. ERROR in pkg\\X.java (at line 5)\r
				* This is {@code raw text:
				 * 			{{{{{{@link BadLink}}} is just text}\r
				          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Javadoc: Missing closing brace for inline tag
			----------
			""");
}
public void testBug206345f() {
	String[] units = new String[] {
		"pkg/X.java",
		"""
			package pkg;
			
			public class X extends Object {
				/**
				 * This is {@code raw text:
				 * 			{@link BadLink}
				 * @since 4.2
				 */
				public String toString() {\s
					return "foo";
				}
			}
			"""
	};
	this.reportInvalidJavadoc = CompilerOptions.ERROR;
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runNegativeTest(units,
		"""
			----------
			1. ERROR in pkg\\X.java (at line 5)\r
				* This is {@code raw text:
				 * 			{@link BadLink}\r
				          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Javadoc: Missing closing brace for inline tag
			----------
			""");
	}
public void testBug206345g() {
	String[] units = new String[] {
		"pkg/X.java",
		"""
			package pkg;
			
			public class X extends Object {
				/**
				 * This is {@code raw text:
				 * 			{@link BadLink
				 * @since 4.2
				 */
				public String toString() {\s
					return "foo";
				}
			}
			"""
	};
	this.reportInvalidJavadoc = CompilerOptions.ERROR;
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runNegativeTest(units,
		"""
			----------
			1. ERROR in pkg\\X.java (at line 5)
				* This is {@code raw text:
				          ^^^^^^^^^^^^^^^^
			Javadoc: Missing closing brace for inline tag
			----------
			""");
}
public void testBug206345h() {
	String[] units = new String[] {
		"pkg/X.java",
		"""
			package pkg;
			
			public class X extends Object {
				/**
				 * This is {@code raw text:
				 * @since 4.2
				 */
				public String toString() {\s
					return "foo";
				}
			}
			"""
	};
	this.reportInvalidJavadoc = CompilerOptions.ERROR;
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runNegativeTest(units,
		"""
			----------
			1. ERROR in pkg\\X.java (at line 5)\r
				* This is {@code raw text:\r
				          ^^^^^^^^^^^^^^^^
			Javadoc: Missing closing brace for inline tag
			----------
			""");
}
public void testBug206345i() {
	String[] units = new String[] {
		"pkg/X.java",
		"""
			package pkg;
			
			public class X extends Object {
				/**
				 * This is {@code raw text:
				 */
				public String toString() {\s
					return "foo";
				}
			}
			"""
	};
	this.reportInvalidJavadoc = CompilerOptions.ERROR;
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runNegativeTest(units,
		"""
			----------
			1. ERROR in pkg\\X.java (at line 5)\r
				* This is {@code raw text:\r
				          ^^^^^^^^^^^^^^^^
			Javadoc: Missing closing brace for inline tag
			----------
			""");
}
public void testBug206345j() {
	String[] units = new String[] {
		"pkg/X.java",
		"""
			package pkg;
			
			public class X extends Object {
				/**
				 * This is {@literal raw text:
				 * 			{@link BadLink} is just text}
				 */
				public String toString() {\s
					return "foo";
				}
			}
			"""
	};
	this.reportInvalidJavadoc = CompilerOptions.ERROR;
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runConformReferenceTest(units);
}
public void testBug206345k() {
	String[] units = new String[] {
		"pkg/X.java",
		"""
			package pkg;
			
			public class X extends Object {
				/**
				 * This is {@code raw text:
				 * 			{@link BadLink} is just text}
				 * }
				 */
				public String toString() {\s
					return "foo";
				}
			}
			"""
	};
	this.reportInvalidJavadoc = CompilerOptions.ERROR;
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runConformReferenceTest(units);
}
public void testBug206345l() {
	String[] units = new String[] {
		"pkg/X.java",
		"""
			package pkg;
			
			public class X extends Object {
				/**
				 * This is {@literal raw text:
				 * 			{@link BadLink}
				 */
				public String toString() {\s
					return "foo";
				}
			}
			"""
	};
	this.reportInvalidJavadoc = CompilerOptions.ERROR;
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runNegativeTest(units,
		"""
			----------
			1. ERROR in pkg\\X.java (at line 5)
				* This is {@literal raw text:
				 * 			{@link BadLink}
				          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Javadoc: Missing closing brace for inline tag
			----------
			""");
}
public void testBug206345m() {
	String[] units = new String[] {
		"pkg/X.java",
		"""
			package pkg;
			
			public class X extends Object {
				/**
				 * This is {@code raw text:
				 * 			{@link BadLink}
				 */
				public String toString() {\s
					return "foo";
				}
			}
			"""
	};
	this.reportInvalidJavadoc = CompilerOptions.ERROR;
	this.reportMissingJavadocDescription = CompilerOptions.ALL_STANDARD_TAGS;
	runNegativeTest(units,
		"""
			----------
			1. ERROR in pkg\\X.java (at line 5)
				* This is {@code raw text:
				 * 			{@link BadLink}
				          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Javadoc: Missing closing brace for inline tag
			----------
			""");
}
}

