/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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
 *     Stephan Herrmann <stephan@cs.tu-berlin.de> - Contributions for
 *								bug 354536 - compiling package-info.java still depends on the order of compilation units
 *								bug 384870 - [compiler] @Deprecated annotation not detected if preceded by other annotation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class Deprecated15Test extends AbstractRegressionTest {
public Deprecated15Test(String name) {
	super(name);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_5);
}
public void test001() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.WARNING);
	this.runNegativeTest(
		new String[] {
			"p/X.java",
			"""
				package p;
				/**
				 * @deprecated
				 */
				public class X<T> {
				}
				""",
			"Y.java",
			"""
				import p.X;
				public class Y {
				  Zork z;
				  void foo() {
				    X x;
				    X[] xs = { x };
				  }
				  void bar() {
				    p.X x;
				    p.X[] xs = { x };
				  }
				}
				""",
		},
		"""
			----------
			1. WARNING in Y.java (at line 1)
				import p.X;
				       ^^^
			The type X<T> is deprecated
			----------
			2. ERROR in Y.java (at line 3)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			3. WARNING in Y.java (at line 5)
				X x;
				^
			The type X<T> is deprecated
			----------
			4. WARNING in Y.java (at line 5)
				X x;
				^
			X is a raw type. References to generic type X<T> should be parameterized
			----------
			5. WARNING in Y.java (at line 6)
				X[] xs = { x };
				^
			The type X<T> is deprecated
			----------
			6. WARNING in Y.java (at line 6)
				X[] xs = { x };
				^
			X is a raw type. References to generic type X<T> should be parameterized
			----------
			7. WARNING in Y.java (at line 9)
				p.X x;
				^^^
			X is a raw type. References to generic type X<T> should be parameterized
			----------
			8. WARNING in Y.java (at line 9)
				p.X x;
				  ^
			The type X<T> is deprecated
			----------
			9. WARNING in Y.java (at line 10)
				p.X[] xs = { x };
				^^^
			X is a raw type. References to generic type X<T> should be parameterized
			----------
			10. WARNING in Y.java (at line 10)
				p.X[] xs = { x };
				  ^
			The type X<T> is deprecated
			----------
			""",
		null,
		true,
		options);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159709
// guard variant for DeprecatedTest#test015 using an annotation
public void test002() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.IGNORE);
	this.runNegativeTest(
		true,
		new String[] {
			"p/M1.java",
			"""
				package p;
				public class M1 {
				  void bar() {
				    a.N1.N2.N3 m = null;
				    m.foo();
				  }
				}
				""",
			"a/N1.java",
			"""
				package a;
				public class N1 {
				  @Deprecated
				  public class N2 {\
				    public void foo() {}\
				    public class N3 {\
				      public void foo() {}\
				    }\
				  }\
				}
				""",
		},
		null, customOptions,
		"""
			----------
			1. ERROR in p\\M1.java (at line 4)
				a.N1.N2.N3 m = null;
				     ^^
			The type N1.N2 is deprecated
			----------
			2. ERROR in p\\M1.java (at line 4)
				a.N1.N2.N3 m = null;
				        ^^
			The type N1.N2.N3 is deprecated
			----------
			3. ERROR in p\\M1.java (at line 5)
				m.foo();
				  ^^^^^
			The method foo() from the type N1.N2.N3 is deprecated
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=161214
// shows that Member2 is properly tagged as deprecated (use the debugger, since
// we do not report deprecation in the unit where the deprecated type is
// declared anyway)
public void test003() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class X {
				  void foo() {
				    class Local {
				      class Member1 {
				        void bar() {
				          Member2 m2; // Member2 is deprecated
				        }
				      }
				      @Deprecated
				      class Member2 {
				      }
				    }
				  }
				}
				"""
		},
		"",
		null,
		true,
		null,
		customOptions,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=191909
public void test004() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.ERROR);
	this.runNegativeTest(
		true,
		new String[] {
			"test1/E01.java",
			"""
				package test1;
				public class E01 {
					@Deprecated
					public static int x = 5, y= 10;
				}""",
			"test1/E02.java",
			"""
				package test1;
				public class E02 {
					public void foo() {
						System.out.println(E01.x);
						System.out.println(E01.y);
					}
				}"""
		},
		null, customOptions,
		"""
			----------
			1. ERROR in test1\\E02.java (at line 4)
				System.out.println(E01.x);
				                       ^
			The field E01.x is deprecated
			----------
			2. ERROR in test1\\E02.java (at line 5)
				System.out.println(E01.y);
				                       ^
			The field E01.y is deprecated
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// Bug 354536 - compiling package-info.java still depends on the order of compilation units
public void test005() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.ERROR);
	this.runNegativeTest(
		true,
		new String[] {
			"p1/X.java",
			"""
				package p1;
				public class X {
				    public static class Inner {\
				        public void foo() {}
				    }
				}
				""",
			"p1/package-info.java",
			"@java.lang.Deprecated\n" +
			"package p1;\n",
			"p2/C.java",
			"""
				package p2;
				public class C {
				    void bar(p1.X.Inner a) {
				        a.foo();
				    }
				}
				""",
		},
		null, customOptions,
		"""
			----------
			1. ERROR in p2\\C.java (at line 3)
				void bar(p1.X.Inner a) {
				            ^
			The type X is deprecated
			----------
			2. ERROR in p2\\C.java (at line 3)
				void bar(p1.X.Inner a) {
				              ^^^^^
			The type X.Inner is deprecated
			----------
			3. ERROR in p2\\C.java (at line 4)
				a.foo();
				  ^^^^^
			The method foo() from the type X.Inner is deprecated
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
// https://bugs.eclipse.org/384870 - [compiler] @Deprecated annotation not detected if preceded by other annotation
public void test006() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.ERROR);
	this.runNegativeTest(
		true,
		new String[] {
			"test1/E02.java",
			"""
				package test1;
				public class E02 {
					public void foo(E01 arg) {
						// nop
					}
				}""",
			"test1/E01.java",
			"""
				package test1;
				@SuppressWarnings("all") @Deprecated
				public class E01 {
					public static int x = 5;
				}"""
		},
		null, customOptions,
		"""
			----------
			1. ERROR in test1\\E02.java (at line 3)
				public void foo(E01 arg) {
				                ^^^
			The type E01 is deprecated
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
public static Class testClass() {
	return Deprecated15Test.class;
}
}
