/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class FieldAccessTest extends AbstractRegressionTest {
	static {
//		TESTS_NAMES = new String[] { "test000" };
//		TESTS_NUMBERS = new int[] { 22 };
//		TESTS_RANGE = new int[] { 21, 50 };
	}

public FieldAccessTest(String name) {
	super(name);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}
@Override
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportIndirectStaticAccess, CompilerOptions.ERROR);
	return options;
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=149004
public void test001() {
	this.runConformTest(
		new String[] {
			"foo/BaseFoo.java",
			"""
				package foo;
				class BaseFoo {
				 public static final int VAL = 0;
				}""",
			"foo/NextFoo.java",
			"""
				package foo;
				public class NextFoo extends BaseFoo {
				}""",
			"bar/Bar.java",
			"""
				package bar;
				public class Bar {
				 int v = foo.NextFoo.VAL;
				}"""
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=149004
public void test002() {
	this.runNegativeTest(
		new String[] {
			"foo/BaseFoo.java",
			"""
				package foo;
				public class BaseFoo {
				 public static final int VAL = 0;
				}""",
			"foo/NextFoo.java",
			"""
				package foo;
				public class NextFoo extends BaseFoo {
				}""",
			"bar/Bar.java",
			"""
				package bar;
				public class Bar {
				 int v = foo.NextFoo.VAL;
				}"""
		},
		"""
			----------
			1. ERROR in bar\\Bar.java (at line 3)
				int v = foo.NextFoo.VAL;
				                    ^^^
			The static field BaseFoo.VAL should be accessed directly
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=149004
public void test003() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportNonStaticAccessToStatic, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
			"foo/BaseFoo.java",
			"""
				package foo;
				class BaseFoo {
				 public static final int VAL = 0;
				}""",
			"foo/NextFoo.java",
			"""
				package foo;
				public class NextFoo extends BaseFoo {
				}""",
			"bar/Bar.java",
			"""
				package bar;
				import foo.NextFoo;
				public class Bar {
					NextFoo[] tab = new NextFoo[] { new NextFoo() };
					int v = tab[0].VAL;
				}"""
		},
		"",
		null,
		true,
		null,
		options,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=149004
public void test004() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportNonStaticAccessToStatic, CompilerOptions.IGNORE);
	this.runNegativeTest(
		true,
		new String[] {
			"foo/BaseFoo.java",
			"""
				package foo;
				public class BaseFoo {
				 public static final int VAL = 0;
				}""",
			"foo/NextFoo.java",
			"""
				package foo;
				public class NextFoo extends BaseFoo {
				}""",
			"bar/Bar.java",
			"""
				package bar;
				import foo.NextFoo;
				public class Bar {
					NextFoo[] tab = new NextFoo[] { new NextFoo() };
					int v = tab[0].VAL;
				}"""
		},
		null,
		options,
		"""
			----------
			1. ERROR in bar\\Bar.java (at line 5)
				int v = tab[0].VAL;
				               ^^^
			The static field BaseFoo.VAL should be accessed directly
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=142234
public void test005() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnqualifiedFieldAccess, CompilerOptions.ERROR);
	this.runNegativeTest(
		true,
		new String[] {
			"X.java",
			"""
				public class X {
					private String memberVariable;
					public String getMemberVariable() {
						return (memberVariable);
					}
				}"""
		},
		null,
		options,
		"""
			----------
			1. ERROR in X.java (at line 4)
				return (memberVariable);
				        ^^^^^^^^^^^^^^
			Unqualified access to the field X.memberVariable\s
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=142234
public void test006() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnqualifiedFieldAccess, CompilerOptions.ERROR);
	this.runNegativeTest(
		true,
		new String[] {
			"X.java",
			"""
				public class X {
					private String memberVariable;
					public String getMemberVariable() {
						return \\u0028memberVariable\\u0029;
					}
				}"""
		},
		null,
		options,
		"""
			----------
			1. ERROR in X.java (at line 4)
				return \\u0028memberVariable\\u0029;
				             ^^^^^^^^^^^^^^
			Unqualified access to the field X.memberVariable\s
			----------
			""",
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=179056
public void test007() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					private void foo() {
						new A().a2.a.test = 8;
					}
				}""",
			"A.java",
			"""
				class A {
					private int test;
					A a2;
					A a = new A();
				}
				
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				new A().a2.a.test = 8;
				             ^^^^
			The field A.test is not visible
			----------
			""",
		null,
		true,
		options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=179056
public void test008() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					private void foo() {
						new A().a2.a.test = 8;
					}
				}""",
			"A.java",
			"""
				class A {
					int test;
					private A a2;
					A a = new A();
				}
				
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				new A().a2.a.test = 8;
				        ^^
			The field A.a2 is not visible
			----------
			""",
		null,
		true,
		options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=179056
public void test009() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					private void foo() {
						new A().a2.a.test = 8;
					}
				}""",
			"A.java",
			"""
				class A {
					int test;
					A a2;
					private A a = new A();
				}
				
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				new A().a2.a.test = 8;
				           ^
			The field A.a is not visible
			----------
			""",
		null,
		true,
		options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=179056
public void test010() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					private void foo() {
						A.a2.a.test = 8;
					}
				}""",
			"A.java",
			"""
				class A {
					static int test;
					static A a2;
					static private A a = new A();
				}
				
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				A.a2.a.test = 8;
				     ^
			The field A.a is not visible
			----------
			""",
		null,
		true,
		options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=179056
public void test011() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					private void foo() {
						A.a2.a.test = 8;
					}
				}""",
			"A.java",
			"""
				class A {
					static int test;
					static private A a2;
					static A a = new A();
				}
				
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				A.a2.a.test = 8;
				  ^^
			The field A.a2 is not visible
			----------
			""",
		null,
		true,
		options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=179056
public void test012() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					private void foo() {
						A.a2.a.test = 8;
					}
				}""",
			"A.java",
			"""
				class A {
					private static int test;
					static A a2;
					A a = new A();
				}
				
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				A.a2.a.test = 8;
				       ^^^^
			The field A.test is not visible
			----------
			""",
		null,
		true,
		options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=179056
public void test013() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X extends A {
					private void foo() {
						test = 8;
					}
				}""",
			"A.java",
			"""
				class A {
					private int test;
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				test = 8;
				^^^^
			The field A.test is not visible
			----------
			""",
		null,
		true,
		options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=179056
public void test014() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X extends A {
					private void foo() {
						this.test = 8;
					}
				}""",
			"A.java",
			"""
				class A {
					private int test;
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				this.test = 8;
				     ^^^^
			The field A.test is not visible
			----------
			""",
		null,
		true,
		options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=179056
public void test015() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X extends A {
					private void foo() {
						MyA.test = 8;
					}
				}""",
			"A.java",
			"""
				class A {
					private static A MyA;
					static int test;
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				MyA.test = 8;
				^^^
			The field A.MyA is not visible
			----------
			""",
		null,
		true,
		options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=179056
public void test016() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X extends A {
					private void foo() {
						MyA2.MyA.test = 8;
					}
				}""",
			"A.java",
			"""
				class A {
					private static A MyA;
					static A MyA2;
					static int test;
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				MyA2.MyA.test = 8;
				     ^^^
			The field A.MyA is not visible
			----------
			""",
		null,
		true,
		options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=222534
public void test017() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFieldHiding, CompilerOptions.WARNING);

	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
						Zork z;
				       private static class Inner1 {
				                private int field;
				       }
				       private static class Inner2 extends Inner1 {
				                private int field;
				                public void bar() {System.out.println(field);}
				       }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			""",
		null,
		true,
		options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=222534 - variation
public void test018() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportFieldHiding, CompilerOptions.WARNING);

	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
						Zork z;
						public static int field;
				       private static class Inner1 {
				                private int field;
				       }
				       private static class Inner2 extends Inner1 {
				                private int field;
				                public void bar() {System.out.println(field);}
				       }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			2. WARNING in X.java (at line 5)
				private int field;
				            ^^^^^
			The field X.Inner1.field is hiding a field from type X
			----------
			3. WARNING in X.java (at line 8)
				private int field;
				            ^^^^^
			The field X.Inner2.field is hiding a field from type X
			----------
			""",
		null,
		true,
		options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=222534 - variation
public void test019() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportLocalVariableHiding, CompilerOptions.WARNING);

	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
						Zork z;
				       private static class Inner1 {
				                private int field;
				       }
				       private static class Inner2 extends Inner1 {
				                public void bar(int field) {System.out.println(field);}
				       }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			""",
		null,
		true,
		options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=222534 - variation
public void test020() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportLocalVariableHiding, CompilerOptions.WARNING);

	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
						Zork z;
						public static int field;
				       private static class Inner1 {
				                private int field;
				       }
				       private static class Inner2 extends Inner1 {
				                public void bar(int field) {System.out.println(field);}
				       }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			2. WARNING in X.java (at line 5)
				private int field;
				            ^^^^^
			The field X.Inner1.field is hiding a field from type X
			----------
			3. WARNING in X.java (at line 8)
				public void bar(int field) {System.out.println(field);}
				                    ^^^^^
			The parameter field is hiding a field from type X
			----------
			""",
		null,
		true,
		options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=303830
public void test021() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				
				public class X {
					public void bar() {
						ArrayList myList = new ArrayList();
						int len = myList.length;
					}
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				int len = myList.length;
				                 ^^^^^^
			length cannot be resolved or is not a field
			----------
			""",
		null,
		true,
		options);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=303830
public void test022() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					static int NEW_FIELD;
				}""",
			"Y.java",
			"""
				public class Y {
					void foo() {
						int i = X.OLD_FIELD;
					}
					void bar() {
						int j = X.OLD_FIELD;
					}
				}"""
		},
		"""
			----------
			1. ERROR in Y.java (at line 3)
				int i = X.OLD_FIELD;
				          ^^^^^^^^^
			OLD_FIELD cannot be resolved or is not a field
			----------
			2. ERROR in Y.java (at line 6)
				int j = X.OLD_FIELD;
				          ^^^^^^^^^
			OLD_FIELD cannot be resolved or is not a field
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318171
public void test023() {
	this.runNegativeTest(
		new String[] {
			"p1/A.java",
			"""
				package p1;
				public abstract class A {
				    protected int field;
				}
				""",
			"p2/B.java",
			"""
				package p2;
				import p1.A;
				public abstract class B extends A {
				    protected int field;
				}
				"""
		},
		"""
			----------
			1. WARNING in p2\\B.java (at line 4)
				protected int field;
				              ^^^^^
			The field B.field is hiding a field from type A
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318171
public void test024() {
	this.runNegativeTest(
		new String[] {
			"p1/A.java",
			"""
				package p1;
				public abstract class A extends Super {
				}
				""",
			"p1/Super.java",
			"""
				package p1;
				public abstract class Super extends SuperSuper {
				}
				""",
			"p1/SuperSuper.java",
			"""
				package p1;
				public abstract class SuperSuper {
				    protected int field;
				}
				""",
			"p2/B.java",
			"""
				package p2;
				import p1.A;
				public abstract class B extends A {
				    protected int field;
				}
				"""
		},
		"""
			----------
			1. WARNING in p2\\B.java (at line 4)
				protected int field;
				              ^^^^^
			The field B.field is hiding a field from type SuperSuper
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318171
public void test025() {
	this.runNegativeTest(
		new String[] {
			"p1/A.java",
			"""
				package p1;
				public abstract class A extends Super {
				}
				""",
			"p1/Super.java",
			"""
				package p1;
				public abstract class Super extends SuperSuper {
				}
				""",
			"p1/SuperSuper.java",
			"""
				package p1;
				public abstract class SuperSuper implements Interface{
				}
				""",
			"p1/Interface.java",
			"""
				package p1;
				public interface Interface{
				    int field = 123;
				}
				""",
			"p2/B.java",
			"""
				package p2;
				import p1.A;
				public abstract class B extends A {
				    protected int field;
				}
				"""
		},
		"""
			----------
			1. WARNING in p2\\B.java (at line 4)
				protected int field;
				              ^^^^^
			The field B.field is hiding a field from type Interface
			----------
			""");
}
public void testBug361039() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // to leverage autounboxing
	runNegativeTest(
		new String[] {
			"Bug361039.java",
			"""
				public class Bug361039 {
					public Bug361039(boolean b) {
					}
					private Object foo() {
						return new Bug361039(!((Boolean)this.f));
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in Bug361039.java (at line 5)
				return new Bug361039(!((Boolean)this.f));
				                                     ^
			f cannot be resolved or is not a field
			----------
			""");
}
public void testBug568959_001() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8) return; // lambda
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				 public void foo(Object o) {
				   I i = () -> {
				     while (o.eq) {
				       // nothing
				     }
				   };
				 }
				}
				interface I {\s
				 public abstract void run();
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				while (o.eq) {
				         ^^
			eq cannot be resolved or is not a field
			----------
			""");
}
public static Class testClass() {
	return FieldAccessTest.class;
}
}

