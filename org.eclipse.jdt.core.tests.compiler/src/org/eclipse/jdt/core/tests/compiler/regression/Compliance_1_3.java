/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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

import java.io.File;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class Compliance_1_3 extends AbstractRegressionTest {
boolean docSupport = false;

public Compliance_1_3(String name) {
	super(name);
}

/*
 * Toggle compiler in mode -1.3
 */
@Override
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	if (this.docSupport) {
		options.put(CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadoc, CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTags, CompilerOptions.ENABLED);
	}
	options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportUnusedParameter, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	return options;
}
public static Test suite() {
		return buildUniqueComplianceTestSuite(testClass(), ClassFileConstants.JDK1_3);
}
public static Class testClass() {
	return Compliance_1_3.class;
}
// Use this static initializer to specify subset for tests
// All specified tests which does not belong to the class are skipped...
static {
//		TESTS_NAMES = new String[] { "Bug58069" };
//		TESTS_NUMBERS = new int[] { 104 };
//		TESTS_RANGE = new int[] { 76, -1 };
}
/* (non-Javadoc)
 * @see junit.framework.TestCase#setUp()
 */
@Override
protected void setUp() throws Exception {
	super.setUp();
	// Javadoc disabled by default
	this.docSupport = false;
}

// test001 - moved to SuperTypeTest#test002
// test002 - moved to SuperTypeTest#test003
// test003 - moved to SuperTypeTest#test004
// test004 - moved to SuperTypeTest#test005
// test005 - moved to SuperTypeTest#test006
// test006 - moved to SuperTypeTest#test007
// test007 - moved to TryStatementTest#test057
// test008 - moved to LookupTest#test074
// test009 - moved to RuntimeTests#test1004

// check actualReceiverType when array type
public void test010() {
	this.runConformTest(
		new String[] {
			"p1/Z.java",
			"""
				package p1;\s
				public class Z {\t
					public static void main(String[] arguments) {\s
						String[] s = new String[]{"SUCCESS" };\t
						System.out.print(s.length);\t
						System.out.print(((String[])s.clone())[0]);\t
					}\s
				}\s
				"""
		},
		"1SUCCESS");
}
// test unreachable code complaints
public void test011() {
	this.runNegativeTest(
		new String[] {
			"p1/X.java",
			"""
				package p1;\s
				public class X {\s
					void foo() {\s
						while (false);\t
						while (false) System.out.println("unreachable");\t
						do ; while (false);\t
						do System.out.println("unreachable"); while (false);\t
						for (;false;);\t
						for (;false;) System.out.println("unreachable");\t
						if (false);\t
						if (false)System.out.println("unreachable");	\t
					}\t
				}\s
				"""
		},
		"""
			----------
			1. ERROR in p1\\X.java (at line 5)
				while (false) System.out.println("unreachable");\t
				              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Unreachable code
			----------
			2. ERROR in p1\\X.java (at line 9)
				for (;false;) System.out.println("unreachable");\t
				              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Unreachable code
			----------
			3. WARNING in p1\\X.java (at line 11)
				if (false)System.out.println("unreachable");	\t
				          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Dead code
			----------
			""");
}
// binary compatibility
public void test012() {
	this.runConformTest(
		new String[] {
			"p1/Y.java",
			"""
				package p1;\t
				class Store {\t
					String value;\t
					Store(String value){\t
						this.value = value;\t
					}\t
				}\t
				class Top {\t
					static String bar = "Top.bar";\t
					String foo = "Top.foo";\t
					Store store = new Store("Top.store");\t
					static Store sstore = new Store("Top.sstore");\t
					static Top ss = new Top();\t
				}\t
				public class Y extends Updated {	\t
					public static void main(String[] arguments) {\t
						new Y().test();\t
					}\t
					void test() {	\t
						System.out.print("*** FIELD ACCESS ***");\t
						System.out.print("*1* new Updated().bar: " + new Updated().bar);\t
						System.out.print("*2* new Updated().foo: " + new Updated().foo);\t
						System.out.print("*3* new Y().foo: " + new Y().foo);\t
						System.out.print("*4* new Y().bar: " + new Y().bar);\t
						System.out.print("*5* bar: " + bar);\t
						System.out.print("*6* foo: " + foo);\t
						System.out.print("*7* Y.bar: " + Y.bar);\t
						System.out.print("*8* this.bar: " + this.bar);\t
						System.out.print("*9* this.foo: " + this.foo);\t
						System.out.print("*10* store.value: " + store.value);\t
						System.out.print("*11* sstore.value: " + sstore.value);\t
						System.out.print("*12* ss.sstore.value: " + ss.sstore.value);\t
					}	\t
				}	\t
				""",
			"p1/Updated.java",
			"""
				package p1;\t
				public class Updated extends Top {\t
				}\t
				"""
		},
		"""
			*** FIELD ACCESS ***\
			*1* new Updated().bar: Top.bar\
			*2* new Updated().foo: Top.foo\
			*3* new Y().foo: Top.foo\
			*4* new Y().bar: Top.bar\
			*5* bar: Top.bar\
			*6* foo: Top.foo\
			*7* Y.bar: Top.bar\
			*8* this.bar: Top.bar\
			*9* this.foo: Top.foo\
			*10* store.value: Top.store\
			*11* sstore.value: Top.sstore\
			*12* ss.sstore.value: Top.sstore""");

	this.runConformTest(
		new String[] {
			"p1/Updated.java",
			"""
				package p1;\s
				public class Updated extends Top {\s
					public static void main(String[] arguments) {\s
						Y.main(arguments);\t
					}\t
					static String bar = "Updated.bar";\t
					String foo = "Updated.foo";\t
					Store store = new Store("Updated.store");\t
					static Store sstore = new Store("Updated.sstore");\t
					static Updated ss = new Updated();\t
				}\s
				"""
		},
		"""
			*** FIELD ACCESS ***\
			*1* new Updated().bar: Top.bar\
			*2* new Updated().foo: Top.foo\
			*3* new Y().foo: Top.foo\
			*4* new Y().bar: Top.bar\
			*5* bar: Top.bar\
			*6* foo: Top.foo\
			*7* Y.bar: Top.bar\
			*8* this.bar: Top.bar\
			*9* this.foo: Top.foo\
			*10* store.value: Top.store\
			*11* sstore.value: Top.sstore\
			*12* ss.sstore.value: Top.sstore""",
		null, // use default class-path
		false, // do not flush previous output dir content
		null); // no special vm args
}
// binary compatibility
public void test013() {
	this.runConformTest(
		new String[] {
			"p1/Y.java",
			"""
				package p1;\t
				class Store {\t
					String value;\t
					Store(String value){\t
						this.value = value;\t
					}\t
				}\t
				class Top {\t
					static String bar() { return "Top.bar()"; }\t
					String foo() { return "Top.foo()"; }\t
				}\t
				public class Y extends Updated {	\t
					public static void main(String[] arguments) {\t
						new Y().test();\t
					}\t
					void test() {	\t
						System.out.print("*** METHOD ACCESS ***");\t
						System.out.print("*1* new Updated().bar(): " + new Updated().bar());\t
						System.out.print("*2* new Updated().foo(): " + new Updated().foo());\t
						System.out.print("*3* new Y().foo(): " + new Y().foo());\t
						System.out.print("*4* new Y().bar(): " + new Y().bar());\t
						System.out.print("*5* bar(): " + bar());\t
						System.out.print("*6* foo(): " + foo());\t
						System.out.print("*7* Y.bar(): " + Y.bar());\t
						System.out.print("*8* this.bar(): " + this.bar());\t
						System.out.print("*9* this.foo(): " + this.foo());\t
					}	\t
				}	\t
				""",
			"p1/Updated.java",
			"""
				package p1;\t
				public class Updated extends Top {\t
				}\t
				"""
		},
		"""
			*** METHOD ACCESS ***\
			*1* new Updated().bar(): Top.bar()\
			*2* new Updated().foo(): Top.foo()\
			*3* new Y().foo(): Top.foo()\
			*4* new Y().bar(): Top.bar()\
			*5* bar(): Top.bar()\
			*6* foo(): Top.foo()\
			*7* Y.bar(): Top.bar()\
			*8* this.bar(): Top.bar()\
			*9* this.foo(): Top.foo()""");

	this.runConformTest(
		new String[] {
			"p1/Updated.java",
			"""
				package p1;\s
				public class Updated extends Top {\s
					public static void main(String[] arguments) {\s
						Y.main(arguments);\t
					}\t
					static String bar() { return "Updated.bar()"; }\t
					String foo() { return "Updated.foo()"; }\t
				}\s
				"""
		},
		"""
			*** METHOD ACCESS ***\
			*1* new Updated().bar(): Top.bar()\
			*2* new Updated().foo(): Updated.foo()\
			*3* new Y().foo(): Updated.foo()\
			*4* new Y().bar(): Top.bar()\
			*5* bar(): Top.bar()\
			*6* foo(): Updated.foo()\
			*7* Y.bar(): Top.bar()\
			*8* this.bar(): Top.bar()\
			*9* this.foo(): Updated.foo()""",
		null, // use default class-path
		false, // do not flush previous output dir content
		null); // no special vm args
}

public void test014() {
	this.runNegativeTest(
		new String[] {
			"p1/X.java",
			"""
				package p1;\t
				class T {\t
					void foo(boolean b) {\t
						 System.out.print("T.foo(boolean)#"); \t
					}\t
					boolean bar = false;\t
					class Member {\t
						void display(){ System.out.print("T.Member#"); }\t
					}\t
				}\t
				public class X {\t
					void foo(int i) {\t
						 System.out.println("X.foo(int)#"); 		\t
					}\t
					int bar;\t
					class Member {\t
						void display(){ System.out.print("X.Member#"); }\t
					}\t
					public static void main(String[] arguments) {\t
						new X().bar();\t
					}			\t
					void bar() { \t
						new T() {\t
							{\t
								foo(true);\t
								System.out.print((boolean)bar + "#");\t
								Member m = new Member();\t
								m.display();\t
							} \t
						};\t
					}\t
				}\t
				"""
		},
		"""
			----------
			1. ERROR in p1\\X.java (at line 25)
				foo(true);\t
				^^^
			The method foo is defined in an inherited type and an enclosing scope
			----------
			2. ERROR in p1\\X.java (at line 26)
				System.out.print((boolean)bar + "#");\t
				                          ^^^
			The field bar is defined in an inherited type and an enclosing scope\s
			----------
			3. ERROR in p1\\X.java (at line 27)
				Member m = new Member();\t
				^^^^^^
			The type Member is defined in an inherited type and an enclosing scope
			----------
			4. ERROR in p1\\X.java (at line 27)
				Member m = new Member();\t
				               ^^^^^^
			The type Member is defined in an inherited type and an enclosing scope
			----------
			"""
	);
}

/*
 * check handling of default abstract methods
 */
public void test015() {
	this.runConformTest(
		new String[] {
			"p1/X.java",
			"""
				package p1;\t
				public class X {\t
					public static void main(String[] arguments) {\t
						C c = new C() {\t
							public void doSomething(){\t
								System.out.println("SUCCESS");\t
							}\t
						};\t
						c.doSomething();\t
					}\t
				}\t
				interface I {\t
					void doSomething();\t
				}\t
				abstract class C implements I {\t
				}\t
				"""
		},
		"SUCCESS");
}

public void test016() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class T {\t
				      void foo(boolean b) {}\t
				}\t
				public class X {\t
				      void foo(int i) {}\t
				      void bar() {\t
				            new T() {\t
				                  {\t
				                        foo(0); \t
				                  }\t
				            };\t
				      }\t
				} \t
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				foo(0); \t
				^^^
			The method foo(boolean) in the type T is not applicable for the arguments (int)
			----------
			""");
}

public void test017() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class T {\t
				      void foo(boolean b) {}\t
				}\t
				public class X {\t
				      void foo(int i) {}\t
				      void bar() {\t
				            new T() {\t
				                  {\t
				                        foo(false); \t
				                  }\t
				            };\t
				      }\t
				} \t
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				foo(false); \t
				^^^
			The method foo is defined in an inherited type and an enclosing scope
			----------
			""");
}

public void test018() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class T {\t
				      void foo(int j) {}\t
				}\t
				public class X {\t
				      void foo(int i) {}\t
				      void bar() {\t
				            new T() {\t
				                  {\t
				                        foo(0); \t
				                  }\t
				            };\t
				      }\t
				} \t
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				foo(0); \t
				^^^
			The method foo is defined in an inherited type and an enclosing scope
			----------
			""");
}
public void test019() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class T {\t
				      void foo(int j) { System.out.println("SUCCESS"); }\t
				}\t
				class U {\t
				      void foo(int j) { System.out.println("FAILED"); }\t
				}\t
				public class X extends U {\t
				      void bar() {\t
				            new T() {\t
				                  {\t
				                        foo(0); \t
				                  }\t
				            };\t
				      }\t
				      public static void main(String[] arguments) {\t
							new X().bar();\t
				      }\t
				} \t
				"""
		},
		"SUCCESS");
}
public void test020() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class T {\t
				      void foo(int j) { System.out.println("SUCCESS"); }\t
				}\t
				class U {\t
				      void foo(boolean j) { System.out.println("FAILED"); }\t
				}\t
				public class X extends U {\t
				      void bar() {\t
				            new T() {\t
				                  {\t
				                        foo(0); \t
				                  }\t
				            };\t
				      }\t
				      public static void main(String[] arguments) {\t
							new X().bar();\t
				      }\t
				} \t
				"""
		},
		"SUCCESS");
}
public void test020a() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class T {\t
				      void foo(U j) { System.out.println("SUCCESS"); }\t
				}\t
				class U {\t
				}\t
				public class X extends U {\t
				      void foo(X j) { System.out.println("FAILED"); }\t
				      void bar() {\t
				            new T() {\t
				                  {\t
				                        foo(new X()); \t
				                  }\t
				            };\t
				      }\t
				      public static void main(String[] arguments) {\t
							new X().bar();\t
				      }\t
				} \t
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 11)\r
				foo(new X()); 	\r
				^^^
			The method foo is defined in an inherited type and an enclosing scope
			----------
			""");
}
// binary check for 11511
public void test021() {
	this.runConformTest(
		new String[] {
			"p1/Z.java",
			"""
				package p1;\t
				public class Z extends AbstractA {\t
					public static void main(String[] arguments) {\t
						new Z().init(); \t
					}\t
				}\t
				abstract class AbstractB implements K {\t
					public void init() {\t
						System.out.println("AbstractB.init()");\t
					}\t
				}\t
				interface K {\t
					void init();\t
					void init(int i);\t
				}\t
				""",
			"p1/AbstractA.java",
			"""
				package p1;\t
				public abstract class AbstractA extends AbstractB implements K {\t
					public void init(int i) {\t
					}\t
				}\t
				"""
		},
		"AbstractB.init()"); // no special vm args

		String computedReferences = findReferences(OUTPUT_DIR + "/p1/Z.class");
		boolean check =
			computedReferences.indexOf("ref/p1") >= 0
			&& computedReferences.indexOf("ref/AbstractB") >= 0
			&& computedReferences.indexOf("methodRef/init/0") >= 0;
		if (!check){
			System.out.println(computedReferences);
		}
		assertTrue("did not bind 'new Z().init()' to AbstractB.init()'", check);
}

/*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=11511
 * variant - applicable error diagnosis
 */
public void test022() {

	this.runNegativeTest(
		new String[] {
			"p1/T.java",
			"""
				package p1;\t
				interface II {}\t
				class TT {\t
					void foo(boolean b) {}\t
					void foo(int i, boolean b) {}\t
					void foo(String s) {}\t
				}\t
				public abstract class T implements II {\t
					void foo(int i) {}\t
					void bar() {\t
						new TT() {\t
							{\t
								foo(0); // should say that foo(int, boolean) isn't applicable\t
							}\t
						};\t
					}\t
					void boo() {\t
						new TT() {\t
							{\t
								foo(true); // should complain ambiguity\t
							}\t
						};\t
					}\t
				} \t
				"""
		},
		"""
			----------
			1. ERROR in p1\\T.java (at line 13)
				foo(0); // should say that foo(int, boolean) isn\'t applicable\t
				^^^
			The method foo(int, boolean) in the type TT is not applicable for the arguments (int)
			----------
			2. ERROR in p1\\T.java (at line 20)
				foo(true); // should complain ambiguity\t
				^^^
			The method foo is defined in an inherited type and an enclosing scope
			----------
			""");
}

 /*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=11511
 * variant - applicable error diagnosis
 */
public void test023() {

	this.runNegativeTest(
		new String[] {
			"p1/T.java",
			"""
				package p1;\t
				interface II {}\t
				abstract class TT {\t
					void foo(boolean b) {}\t
					void foo(int i, boolean b) {}\t
					void foo(String s) {}\t
				}\t
				public abstract class T implements II {\t
					void foo(int i) {}\t
					void bar() {\t
						new TT() {\t
							{\t
								foo(0); // should say that foo(int, boolean) isn't applicable\t
							}\t
						};\t
					}\t
					void boo() {\t
						new TT() {\t
							{\t
								foo(true); // should complain ambiguity\t
							}\t
						};\t
					}\t
				} \t
				"""
		},
		"""
			----------
			1. ERROR in p1\\T.java (at line 13)
				foo(0); // should say that foo(int, boolean) isn\'t applicable\t
				^^^
			The method foo(int, boolean) in the type TT is not applicable for the arguments (int)
			----------
			2. ERROR in p1\\T.java (at line 20)
				foo(true); // should complain ambiguity\t
				^^^
			The method foo is defined in an inherited type and an enclosing scope
			----------
			""");
}
/*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=11511
 * variant - applicable error diagnosis
 */
public void test024() {

	this.runNegativeTest(
		new String[] {
			"p1/X.java",
			"""
				package p1;\t
				interface II {}\t
				abstract class T implements II {\t
					void foo(boolean b) {}\t
					void foo(int i, boolean b) {}\t
				}\t
				abstract class TT implements II {\t
					void foo(boolean b) {}\t
				}\t
				public class X {\t
					void foo(int i) {}\t
					void bar() {\t
						new T() {\t
							{\t
								foo(0); // javac says foo cannot be resolved because of multiple matches\t
							}\t
						};\t
					}\t
					void bar2() {\t
						new TT() {\t
							{\t
								foo(0); // should say that foo(boolean) isn't applicable\t
							}\t
						};\t
					}\t
					void boo() {\t
						new T() {\t
							{\t
								foo(true); // should complain ambiguity\t
							}\t
						};\t
					}\t
				}\t
				"""
		},
		"""
			----------
			1. ERROR in p1\\X.java (at line 15)
				foo(0); // javac says foo cannot be resolved because of multiple matches\t
				^^^
			The method foo(int, boolean) in the type T is not applicable for the arguments (int)
			----------
			2. ERROR in p1\\X.java (at line 22)
				foo(0); // should say that foo(boolean) isn\'t applicable\t
				^^^
			The method foo(boolean) in the type TT is not applicable for the arguments (int)
			----------
			3. ERROR in p1\\X.java (at line 29)
				foo(true); // should complain ambiguity\t
				^^^
			The method foo is defined in an inherited type and an enclosing scope
			----------
			""");
}
/*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=11511
 * variant - applicable error diagnosis (no matter if super is abstract or not)
 */
public void test025() {

	this.runNegativeTest(
		new String[] {
			"p1/X.java",
			"""
				package p1;\t
				public class X extends AbstractY {\t
					void bar(){\t
						init("hello");\t
					}	\t
				}\t
				abstract class AbstractY implements I {\t
				}\t
				interface I {\t
					void init(String s, int i);\t
				}\t
				"""
		},
		"""
			----------
			1. ERROR in p1\\X.java (at line 2)
				public class X extends AbstractY {\t
				             ^
			The type X must implement the inherited abstract method I.init(String, int)
			----------
			2. ERROR in p1\\X.java (at line 4)
				init("hello");\t
				^^^^
			The method init(String, int) in the type AbstractY is not applicable for the arguments (String)
			----------
			""");
}

/*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=11511
 * variant - applicable error diagnosis (no matter if super is abstract or not)
 */
public void test026() {

	this.runNegativeTest(
		new String[] {
			"p1/X.java",
			"""
				package p1;\t
				public class X extends AbstractY {\t
					void bar(){\t
						init("hello");\t
					}	\t
				}\t
				class AbstractY implements I {\t
				}\t
				interface I {\t
					void init(String s, int i);\t
				}\t
				"""
		},
		"""
			----------
			1. ERROR in p1\\X.java (at line 4)
				init("hello");\t
				^^^^
			The method init(String, int) in the type I is not applicable for the arguments (String)
			----------
			2. ERROR in p1\\X.java (at line 7)
				class AbstractY implements I {\t
				      ^^^^^^^^^
			The type AbstractY must implement the inherited abstract method I.init(String, int)
			----------
			"""
);
}

/*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=11922
 * code gen for for(;false;...)
 */
public void test027() {

	this.runConformTest(
		new String[] {
			"p1/X.java",
			"""
				package p1;\t
				public class X {\t
					public static void main(String[] arguments) {\t
						for (;false;p());\t
						System.out.println("SUCCESS");\t
					}\t
					static void p(){\t
						System.out.println("FAILED");\t
					}\t
				}\t
				"""
		},
		"SUCCESS");
}
/*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=12445
 * should report unreachable empty statement
 */
public void test028() {

	this.runConformTest(
		new String[] {
			"p1/X.java",
			"""
				package p1;\t
				interface FooInterface {\t
					public boolean foo(int a);\t
					public boolean bar(int a);\t
				}\t
				public class X extends Z {\t
					public boolean foo(int a){ return true; }\t
					public boolean bar(int a){ return false; }\t
					public static void main(String[] arguments) {\t
						System.out.println(new X().test(0));\t
					}\t
				}
				abstract class Z implements FooInterface {\t
					public boolean foo(int a, int b) {\t
						return true;\t
					}\t
					public String test(int a) {\t
						boolean result = foo(a);\s
						if (result)\t
							return "SUCCESS";\t
						else\t
							return "FAILED";\t
					}\t
				}\t
				"""
		},
		"SUCCESS");
}
/*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=21580
 * verify error on qualified name ref in 1.4
 */
public void test029() {

	this.runConformTest(
		new String[] {
			"p/X.java",
			"""
				package p;\t
				public class X {\t
					public static void main(String[] args) {\t
						new X();\t
						System.out.println("SUCCESS");\t
					}  \t
					Woof woof_1;\t
					public class Honk {\t
						Integer honks;\t
					}\t
					public class Meow {\t
						Honk honk_1;\t
					}\t
					public class Woof {\t
						Meow meow_1;\t
					}\t
					public void setHonks(int num) {\t
						// This is the line that causes the VerifyError\t
						woof_1.meow_1.honk_1.honks = new Integer(num);\t
						// Here is equivalent code that does not cause the error.\t
						//  Honk h = woof_1.moo_1.meow_1.honk_1;\t
						//  h.honks = new Integer(num);\t
					}\t
				}\t
				"""
		},
		"SUCCESS");
}
/*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=21580
 * 1.4 signals invocations of non-visible abstract protected method implementations.
 */
public void test030() {

	this.runNegativeTest(
		new String[] {
			"p/X.java",
			"""
				package p;\t
				public class X {\t
					public static void main(String[] args){\t
						new q.X2().foo("String");\t
						new q.X2().bar("String");\t
						new q.X2().barbar("String");\t
						new q.X2().baz("String");\t
					}\t
				}\t
				""",

			"p/X1.java",
			"""
				package p;\t
				public abstract class X1 {\t
					protected void foo(Object o){	System.out.println("X1.foo(Object)"); }\t
					protected void bar(Object o){	System.out.println("X1.bar(Object)"); }\t
					void barbar(Object o){	System.out.println("X1.barbar(Object)"); }\t
					protected void baz(Object o) { System.out.println("X1.baz(Object)"); }\t
				}\t
				""",

			"q/X2.java",
			"""
				package q;\t
				public class X2 extends p.X1 {\t
					protected void foo(int i) { System.out.println("X2.foo(int)"); }\t
					protected void bar(Object o) { System.out.println("X2.bar(Object)"); }\t
					void barbar(Object o){	System.out.println("X2.barbar(Object)"); }\t
					protected void baz(String s) {	System.out.println("X2.baz(String)"); }\t
				}\t
				""",
		},
		"""
			----------
			1. ERROR in p\\X.java (at line 6)
				new q.X2().barbar("String");\t
				           ^^^^^^
			The method barbar(Object) from the type X2 is not visible
			----------
			----------
			1. WARNING in q\\X2.java (at line 5)
				void barbar(Object o){	System.out.println("X2.barbar(Object)"); }\t
				     ^^^^^^^^^^^^^^^^
			The method X2.barbar(Object) does not override the inherited method from X1 since it is private to a different package
			----------
			""");
}

/*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=21580
 * 1.4 signals invocations of non-visible abstract protected method implementations.
 */
public void test031() {

	this.runNegativeTest(
		new String[] {
			"p/X.java",
			"""
				package p;\t
				public class X extends q.X2 {\t
					public static void main(String[] args){\t
							new X().doSomething();\t
					}\t
					void doSomething(){\t
						foo("String");\t
						bar("String");\t
						barbar("String");\t
						baz("String");\t
					}\t
				}\t
				""",

			"p/X1.java",
			"""
				package p;\t
				public abstract class X1 {\t
					protected void foo(Object o){	System.out.println("X1.foo(Object)"); }\t
					protected void bar(Object o){	System.out.println("X1.bar(Object)"); }\t
					void barbar(Object o){	System.out.println("X1.barbar(Object)"); }\t
					protected void baz(Object o) { System.out.println("X1.baz(Object)"); }\t
				}\t
				""",

			"q/X2.java",
			"""
				package q;\t
				public class X2 extends p.X1 {\t
					protected void foo(int i) { System.out.println("X2.foo(int)"); }\t
					protected void bar(Object o) { System.out.println("X2.bar(Object)"); }\t
					void barbar(Object o){	System.out.println("X2.barbar(Object)"); }\t
					protected void baz(String s) {	System.out.println("X2.baz(String)"); }\t
				}\t
				""",
		},
		"""
			----------
			1. ERROR in p\\X.java (at line 9)
				barbar("String");\t
				^^^^^^
			The method barbar(Object) from the type X2 is not visible
			----------
			----------
			1. WARNING in q\\X2.java (at line 5)
				void barbar(Object o){	System.out.println("X2.barbar(Object)"); }\t
				     ^^^^^^^^^^^^^^^^
			The method X2.barbar(Object) does not override the inherited method from X1 since it is private to a different package
			----------
			"""
);
}

/*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=21580
 * 1.4 signals invocations of non-visible abstract protected field implementations.
 */
public void test032() {

	this.runNegativeTest(
		new String[] {
			"p/X.java",
			"""
				package p;\t
				public class X {\t
					public static void main(String[] args){\t
						System.out.println(new q.X2().foo);\t
						System.out.println(new q.X2().bar);\t
					}\t
				}\t
				""",

			"p/X1.java",
			"""
				package p;\t
				public abstract class X1 {\t
					protected String foo = "X1.foo"; \t
					String bar = "X1.bar";\t
				}\t
				""",

			"q/X2.java",
			"""
				package q;\t
				public class X2 extends p.X1 {\t
					protected String foo = "X2.foo";\t
					String bar = "X2.bar";\t
				}\t
				""",
		},
		"""
			----------
			1. ERROR in p\\X.java (at line 4)
				System.out.println(new q.X2().foo);\t
				                              ^^^
			The field X2.foo is not visible
			----------
			2. ERROR in p\\X.java (at line 5)
				System.out.println(new q.X2().bar);\t
				                              ^^^
			The field X2.bar is not visible
			----------
			----------
			1. WARNING in q\\X2.java (at line 3)
				protected String foo = "X2.foo";\t
				                 ^^^
			The field X2.foo is hiding a field from type X1
			----------
			""");
}
/*
 * Initialization of synthetic fields prior to super constructor call
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=23075
 */
public void test033() {

	this.runConformTest(
		new String[] {
			"A.java",
			"""
				public class A {\t
				  public int m;\t
				  public void pp() {\t
				     C c = new C(4);\t
				     System.out.println(c.get());\t
				  }\t
				  public static void main(String[] args) {\t
				     A a = new A();\t
					  try {\t
				       a.pp(); \t
						System.out.println("SyntheticInit BEFORE SuperConstructorCall");\t
					  } catch(NullPointerException e) {\t
						System.out.println("SyntheticInit AFTER SuperConstructorCall"); // should no longer occur with target 1.4\s
					  }\t
				  }\t
				  class C extends B {\t
				    public C(int x1) {\t
				      super(x1);    \t
				    }\t
				    protected void init(int x1) {\t
				       x = m * x1; // <- NULL POINTER EXCEPTION because of m\t
				    }  \t
				  }\t
				}\t
				class B {\t
				  int x;\t
				  public B(int x1) {\t
				    init(x1);\t
				  }\t
				  protected void init(int x1) {\t
				    x  = x1;\t
				  }\t
				  public int get() {\t
				    return x;\t
				  }\t
				}\t
				"""
		},
		"SyntheticInit AFTER SuperConstructorCall");
}

/*
 * Initialization of synthetic fields prior to super constructor call - NPE check
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=25174
 */
public void test034() {

	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {\t
					public static void main(String[] arguments) {\t
						new X().new X2();\t
					}\t
					class X1 {\t
						X1(){\t
							this.baz();\t
						}\t
						void baz() {\t
							System.out.println("-X1.baz()");\t
						}\t
					}\t
					class X2 extends X1 {\t
						void baz() {\t
							System.out.print(X.this==null ? "X.this == null" : "X.this != null");\t
							X1 x1 = X.this.new X1(){\t
								void baz(){\t
									System.out.println("-X$1.baz()");\t
								}\t
							};\t
						}\t
					}\t
				}
				""",
		},
		"X.this == null-X$1.baz()");
}

public void test035() {
	this.runConformTest(
		new String[] {
			/* p1/X.java */
			"p1/X.java",
			"""
				package p1;\t
				public class X {\t
					class Y { Y(int i){} }\t
					public static void main(String[] arguments) {\t
						int i = 1;\t
						try {\t
							X x =null;\t
							x.new Y(++i);\t
							System.out.println("SUCCESS:"+i);\t
						} catch(NullPointerException e){\t
							System.out.println("FAILED");\t
						}\t
					}\t
				}\t
				""",
		},
		"SUCCESS:2"
	);
}

public void test036() {
	this.runConformTest(
		new String[] {
			/* p1/X.java */
			"p1/X.java",
			"""
				package p1;\t
				public class X {\t
					class Y {}\t
					static class Z extends Y {\t
						Z (X x){\t
							x.super();\t
						}	\t
					}\t
					public static void main(String[] arguments) {\t
						try {\t
							new Z(null);\t
							System.out.println("SUCCESS");\t
						} catch(NullPointerException e){\t
							System.out.println("FAILED");\t
						}\t
					}\t
				}\t
				""",
		},
		"SUCCESS"
	);
}

/*
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=24744
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=23096
 */
public void test037() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_TaskTags, "TODO:");
	this.runNegativeTest(
		new String[] {
			"p/X.java",
			"""
				package p;\t
				public class X {
				}
				// TODO: something"""
		},
		"""
			----------
			1. WARNING in p\\X.java (at line 4)
				// TODO: something
				   ^^^^^^^^^^^^^^^
			TODO: something
			----------
			""",
		null,
		true,
		customOptions);
}

/*
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=24833
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=23096
 */
public void test038() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_TaskTags, "TODO:");
	this.runNegativeTest(
		new String[] {
			"X.java",
			"// TODO: something"
		},
		"""
			----------
			1. WARNING in X.java (at line 1)
				// TODO: something
				   ^^^^^^^^^^^^^^^
			TODO: something
			----------
			""",
		null,
		true,
		customOptions,
		"java.lang.ClassNotFoundException");
}

/*
 * unreachable empty statement/block not diagnosed in 1.3
 */
public void test039() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {\t
					public static void main(String[] args){\t
						for (;null != null;);\t
						for (;null != null;){}\t
						for (;false;);\t
						for (;false;){}\t
						while (false);\t
						while (false){}\t
						if (false) {} else {}\t
						if (false) ; else ;		\t
						System.out.println("SUCCESS");\t
					}\t
				}\t
				""",
		},
		"SUCCESS");
}

// jls6.5.5.1 - simple type names favor member type over toplevel one.
//http://bugs.eclipse.org/bugs/show_bug.cgi?id=30705
public void test040() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {\t
					interface Homonym {}\t
					void foo() {\t
						class Homonym extends X {\t
							{\t
								class Y extends Homonym {};\t
							}\t
						}\t
					}\t
				}\t
				class Homonym extends X {\t
					{\t
						class Y extends Homonym {};\t
					}\t
				}\t
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 4)
				class Homonym extends X {\t
				      ^^^^^^^
			The type Homonym is hiding the type X.Homonym
			----------
			2. ERROR in X.java (at line 6)
				class Y extends Homonym {};\t
				                ^^^^^^^
			The type Homonym is defined in an inherited type and an enclosing scope
			----------
			3. ERROR in X.java (at line 6)
				class Y extends Homonym {};\t
				                ^^^^^^^
			The type X.Homonym cannot be the superclass of Y; a superclass must be a class
			----------
			4. ERROR in X.java (at line 13)
				class Y extends Homonym {};\t
				                ^^^^^^^
			The type Homonym is defined in an inherited type and an enclosing scope
			----------
			5. ERROR in X.java (at line 13)
				class Y extends Homonym {};\t
				                ^^^^^^^
			The type X.Homonym cannot be the superclass of Y; a superclass must be a class
			----------
			""");
}
/*
 * 30856 - 1.4 compliant mode should consider abstract method matches
 */
public void test041() {
	this.runConformTest(
		new String[] {
			"p/X.java", //================================
			"""
				package p;\t
				public class X {\t
					void foo(int i, float f){}\t
					public static void main(String[] args) {\t
						q.Y y = new q.Y.Z();\t
						y.bar();\t
					}\t
				}\t
				""",
			"q/Y.java", //================================
			"""
				package q;\t
				public abstract class Y extends p.X implements I {\t
					public void bar(){   foo(1, 2); }\t
					public static class Z extends Y {\t
						public void foo(float f, int i) {\t
							System.out.println("SUCCESS");\t
						}\t
					}\t
				}\t
				interface I {\t
					void foo(float f, int i);\t
				}\t
				""",
		},
		"SUCCESS");
}
/*
 * variation - 30856 - 1.4 compliant mode should consider abstract method matches
 */
public void test042() {
	this.runConformTest(
		new String[] {
			"p/X.java", //================================
			"""
				package p;\t
				public class X extends X0 {\t
					void foo(int i, float f){}\t
					public static void main(String[] args) {\t
						q.Y y = new q.Y.Z();\t
						y.bar();\t
					}\t
				}\t
				class X0 {\t
					void foo(int i, double d){}\t
				}\t
				""",
			"q/Y.java", //================================
			"""
				package q;\t
				public abstract class Y extends p.X implements I {\t
					public void bar(){   foo(1, 2); }\t
					public static class Z extends Y {\t
						public void foo(float f, int i) {\t
							System.out.println("SUCCESS");\t
						}\t
					}\t
				}\t
				interface I {\t
					void foo(float f, int i);\t
				}\t
				""",
		},
		"SUCCESS");
}

// binary compatibility
public void _test043() {
	this.runConformTest(
		new String[] {
			"p1/Y.java",
			"""
				package p1;\t
				public class Y extends A implements I {\s
					public static void main(String[] args) {\t
						Y.printValues();\t
					}\t
					public static void printValues() {\t
						System.out.println("i="+i+",j="+j+",Y.i="+Y.i+",Y.j="+Y.j);\t
					}\t
				}\t
				""",
			"p1/A.java",
			"""
				package p1;\t
				public class A {\t
					static int i = 1;\t
				}\t
				""",
			"p1/I.java",
			"""
				package p1;\t
				interface I {\t
					int j = "aa".length();\t
				}\t
				""",
		},
		"i=1,j=2,Y.i=1,Y.j=2");

	this.runConformTest(
		new String[] {
			"p1/A.java",
			"""
				package p1;\t
				public class A {\t
					static int j = 3;\t
					public static void main(String[] args) {\t
						Y.printValues();\t
					}\t
				}\t
				""",
			"p1/I.java",
			"""
				package p1;\t
				interface I {\t
					int j = "aaaa".length();\t
				}\t
				""",
		},
		"i=4,j=3,Y.i=4,Y.j=3",
		null, // use default class-path
		false, // do not flush previous output dir content
		null); // no special vm args
}
/*
 * array.clone() should use array type in methodRef
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=36307
 */
public void test044() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public static void main(String[] args) {
						args.clone();\t
						System.out.println("SUCCESS");
				    }
				}
				""",
		},
		"SUCCESS");

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);

	String expectedOutput =
		"     1  invokevirtual java.lang.Object.clone() : java.lang.Object [16]\n";

	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 2));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, actualOutput);
	}
}

// 39172
public void test045() {
	this.runConformTest(
		new String[] {
			"p/X.java",
			"""
				package p;\t
				public class X {\s
					public static void main(String[] args) {\t
						System.out.println("SUCCESS");\t
						return;;\t
					}\t
				}\t
				"""
		},
		"SUCCESS"
	);
}
/**
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=39467
 * should diagnose missing abstract method implementation
 */
public void test046() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X extends Y {
				}
				abstract class Y extends Z {
				  public abstract void foo();
				}
				abstract class Z extends T {
				}
				class T implements I {
				  public void foo(){}
				}
				interface I {
				    public void foo ();
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				public class X extends Y {
				             ^
			The type X must implement the inherited abstract method Y.foo()
			----------
			"""
	);
}
/**
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=40442
 * Abstract class fails to invoke interface-defined method in 1.4 compliance mode.
 */
public void test047() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X extends AbstractDoubleAlgorithm {
				\t
					public static void main(String[] args) {
						((ObjectAlgorithm)(new X())).operate(new Double(0));
					}
				    public void operate(Double pDouble)
				    {
				        System.out.println("SUCCESS");
				    }
				}
				abstract class AbstractDoubleAlgorithm implements DoubleAlgorithm {
				    public void operate(Object pObject)
				    {
				        operate((Double)pObject);
				    }
				}
				interface DoubleAlgorithm extends ObjectAlgorithm {
				    void operate(Double pDouble);
				}
				interface ObjectAlgorithm {
				    void operate(Object pObject);
				}"""
		},
		"SUCCESS"
	);
}
/**
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=40442
 * Abstract class fails to invoke interface-defined method in 1.4 compliance mode.
 * variation with 2 found methods
 */
public void test048() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X extends AbstractDoubleAlgorithm {
				\t
					public static void main(String[] args) {
						((ObjectAlgorithm)(new X())).operate(new Double(0));
					}
				    public void operate(Double pDouble)
				    {
				        System.out.println("SUCCESS");
				    }
				}
				abstract class AbstractDoubleAlgorithm implements DoubleAlgorithm {
				    public void operate(Object pObject)
				    {
				        operate((Double)pObject);
				    }
				    public void operate(X x) {}
				}
				interface DoubleAlgorithm extends ObjectAlgorithm {
				    void operate(Double pDouble);
				}
				interface ObjectAlgorithm {
				    void operate(Object pObject);
				}"""
		},
		"SUCCESS"
	);
}
/**
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=41278
 */
public void test049() {
	this.runConformTest(
		new String[] {
			"pa/Caller.java",
			"""
				package pa;
				import pb.Concrete;
				public class Caller {
				
					public static void main(String[] args) {
						Concrete aConcrete = new Concrete();\s
						aConcrete.callme();
					}
				}
				""",
			"pa/Abstract.java",
			"""
				package pa;
				public abstract class Abstract {
				
					protected void callme(){}
				}
				""",
			"pb/Concrete.java",
			"""
				package pb;
				public class Concrete extends pa.Abstract {
				
					protected void callme(){	System.out.println("SUCCESS"); }
				}
				""",
		},
		"SUCCESS");
}

public void test050() {
	this.runConformTest(new String[] {
		"p/X.java",
		"""
			package p;
			public class X {
			  public static void main(String args[]) {
			     foo();
			  }
			  public static void foo() {
			     int a1 = 1;
			     int a2 = 1;
			     a1 = 2;
			     while (false) {};
			     a2 = 2;
			  }
			}
			""",
	});
}

public void test051() {
	this.runConformTest(new String[] {
		"p/X.java",
		"""
			package p;
			public class X {
			  public static void main(String args[]) {
			     foo();
			  }
			  public static void foo() {
			     int a1 = 1;
			     int a2 = 1;
			     a1 = 2;
			     while (false);
			     a2 = 2;
			  }
			}
			""",
	});
}

public void test052() {
	this.runNegativeTest(
		new String[] {
			"p/A.java",
			"""
				package p;
				public class A {
				  public static void main(String[] argv) {
				    foo();
				  }
				  private int i;
				  static class Y extends X {
				    int x = i;
				  }
				  public static void foo() {
				    return;
				  }
				}""",

			"p/X.java",
			"""
				package p;
				public class X {
				  public static void main(String argv[]) {
				     foo();
				  }
				  public static void foo() {
				     int a1 = 1;
				     int a2 = 1;
				     a1 = 2;
				     while (false);
				     a2 = 2;
				  }
				}"""
		},
		"""
			----------
			1. ERROR in p\\A.java (at line 8)
				int x = i;
				        ^
			Cannot make a static reference to the non-static field i
			----------
			""");
}

public void test053() {
	this.runNegativeTest(
		new String[] {
			"p/X.java",
			"""
				package p;
				class X {
				  static class A {
				    interface I {
				      int a = 3;
				    }
				  }\s
				  interface I {\s
				    int b = 4;
				  }
				  class Y extends A implements I {
				    Object F() {
				      return new I() {
				        int c = a; // WE SHOULD NOT BE ABLE TO SEE BOTH a and b
				        int d = b; // WE SHOULD NOT BE ABLE TO SEE BOTH a and b
				      };
				    }
				  }
				}""",
		},
		"""
			----------
			1. ERROR in p\\X.java (at line 13)
				return new I() {
				           ^
			The type I is defined in an inherited type and an enclosing scope
			----------
			"""
	);
}

public void test054() {
	this.runNegativeTest(
		new String[] {
			"p/X.java",
			"""
				package p;
				public class X {
				  static class A {
				    interface I {
				      int a = 3;
				      void foo();
				    }
				  }
				  interface I {
				    int a = 4;
				    void foo();
				  }
				  class Y extends A implements I {
				    public void foo() {
				      new I() {
				        public void foo() {
				          System.out.println("X$1::foo-" + a);
				        }
				      }
				      .foo();
				    }
				  }
				public static void main(String argv[]) {
				  new X().new Y().foo();
				}
				}""",
		},
		"""
			----------
			1. ERROR in p\\X.java (at line 15)
				new I() {
				    ^
			The type I is defined in an inherited type and an enclosing scope
			----------
			"""
	);
}
public void test055() {
	this.runNegativeTest(
		new String[] {
			"p/X.java",
			"""
				package p;
				public class X {
				  static class A {
				    interface I2 {
				      int a = 3;
				      void foo();
				    }
				  }
				  interface I1 {
				    int a = 4;
				    void foo(int a);
				  }
				  class Y extends A implements I1 {
				    public void foo(int a) {
				      new I2() {
				        public void foo() {
				          System.out.println("X$1::foo-" + a);
				        }
				      }
				      .foo();
				    }
				  }
				public static void main(String argv[]) {
				  new X().new Y().foo(8);
				}
				}""",
		},
		"""
			----------
			1. WARNING in p\\X.java (at line 11)
				void foo(int a);
				             ^
			The parameter a is hiding a field from type X.I1
			----------
			2. WARNING in p\\X.java (at line 14)
				public void foo(int a) {
				                    ^
			The parameter a is hiding a field from type X.I1
			----------
			3. ERROR in p\\X.java (at line 17)
				System.out.println("X$1::foo-" + a);
				                                 ^
			The field a is defined in an inherited type and an enclosing scope\s
			----------
			"""
	);
}

public void test056() {
	this.runNegativeTest(
		new String[] {
			"p/MethodQualification.java",
			"""
				package p;
				public class MethodQualification {
				  void foo() {
				  System.out.println("Inherited foo() for anonymous type");
				  class Local {
				    void foo(){
				    System.out.println("Enclosing foo() for anonymous type");
				    new MethodQualification () { {foo();} };
				    }
				  };
				  } \s
				}""",
		},
		"""
			----------
			1. ERROR in p\\MethodQualification.java (at line 8)
				new MethodQualification () { {foo();} };
				                              ^^^
			The method foo is defined in an inherited type and an enclosing scope
			----------
			"""
	);
}

public void test057() {
	this.runNegativeTest(
		new String[] {
			"p/AG.java",
			"""
				package p;
				/**
				 * 1F9RITI
				 */
				public class AG {
				  public class X {
				    class B {
				      int intValueOfB = -9;
				    }
				    class SomeInner extends A {
				      void someMethod() {
				        int i = new B().intValueOfB; //HERE ERROR SHOULD BE DETECTED
				      }
				    }
				  }
				  class A {
				    class B {
				      int intValueOfB = -9;
				    }
				  }
				}""",
		},
		"""
			----------
			1. ERROR in p\\AG.java (at line 12)
				int i = new B().intValueOfB; //HERE ERROR SHOULD BE DETECTED
				            ^
			The type B is defined in an inherited type and an enclosing scope
			----------
			"""
	);
}

public void test058() {
	this.runNegativeTest(
		new String[] {
			"p/AE.java",
			"""
				package p;
				/**
				 * 1F9RITI
				 */
				public class AE {
				  public class X {
				    int intValue = 153;
				    class SomeInner extends A {
				      void someMethod() {
				        int i = intValue; //HERE THE ERROR - SHOULD HAVE A QUALIFICATION
				      }
				    }
				  }
				  class A {
				    int intValue = 153;
				  }
				}""",
		},
		"""
			----------
			1. ERROR in p\\AE.java (at line 10)
				int i = intValue; //HERE THE ERROR - SHOULD HAVE A QUALIFICATION
				        ^^^^^^^^
			The field intValue is defined in an inherited type and an enclosing scope\s
			----------
			"""
	);
}

public void test059() {
	this.runNegativeTest(
		new String[] {
			"p/FieldQualification.java",
			"""
				package p;
				public class FieldQualification {
				  String field = "Inherited field for anonymous type";
				void foo() {
				  class Local {
				    String field = "Enclosing field for anonymous type";
				    void foo() {
				      System.out.println("Enclosing foo() for anonymous type");
				      new FieldQualification() {
				        {
				          System.out.println(field);
				        }
				      };
				    }
				  };
				}
				}""",
		},
		"""
			----------
			1. WARNING in p\\FieldQualification.java (at line 6)
				String field = "Enclosing field for anonymous type";
				       ^^^^^
			The field Local.field is hiding a field from type FieldQualification
			----------
			2. ERROR in p\\FieldQualification.java (at line 11)
				System.out.println(field);
				                   ^^^^^
			The field field is defined in an inherited type and an enclosing scope\s
			----------
			"""
	);
}

public void test060() {
	this.runNegativeTest(
		new String[] {
			"p/AF.java",
			"""
				package p;
				/**
				 * 1F9RITI
				 */
				public class AF {
				  public class X {
				    int intMethod() {
				      return 3333;
				    }
				    class SomeInner extends A {
				      void someMethod() {
				        int i = intMethod(); //ERROR HERE SHOULD BE DETECTED
				      }
				    }
				  }
				  class A {
				    int intMethod() {
				      return 3333;
				    }
				  }
				}""",
		},
		"""
			----------
			1. ERROR in p\\AF.java (at line 12)
				int i = intMethod(); //ERROR HERE SHOULD BE DETECTED
				        ^^^^^^^^^
			The method intMethod is defined in an inherited type and an enclosing scope
			----------
			"""
	);
}

/*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=32342
 */
public void test061() {
	this.runNegativeTest(
		new String[] {
			"p/X.java", //======================
			"""
				package p;\t
				public class X extends q.Y {\t
					X someField;\t
				}\t
				class Z extends q.Y {\t
					Z someField;\t
				}\t
				""",
			"q/Y.java", //======================
			"""
				package q;\t
				public class Y {\t
					private static class X {}\t
					public static class Z {}\t
				}\t
				"""
		},
		"""
			----------
			1. ERROR in p\\X.java (at line 6)
				Z someField;\t
				^
			The type Z is defined in an inherited type and an enclosing scope
			----------
			""");
}

/*
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=11435
 * variant - must still complain when targeting super abstract method
 */
public void test062() {

	this.runNegativeTest(
		new String[] {
			"p1/Y.java",
			"""
				package p1;\t
				public class Y extends AbstractT {\t
					public void init(){\t
						super.init();\t
					}\t
				}\t
				abstract class AbstractT implements J {\t
				}\t
				interface J {\t
					void init();\t
				}\t
				"""
		},
		"""
			----------
			1. ERROR in p1\\Y.java (at line 4)
				super.init();\t
				^^^^^^^^^^^^
			Cannot directly invoke the abstract method init() for the type AbstractT
			----------
			"""); // expected log
}

public void test063() {
	this.runConformTest(
		new String[] {
			/* p1/X.java */
			"p1/X.java",
			"""
				package p1;\t
				public class X {\t
					class Y extends X {}\t
					class Z extends Y {\t
						Z(){\t
							System.out.println("SUCCESS");\t
						}\t
					}\t
					public static void main(String[] arguments) {\t
						new X().new Z();\t
					}\t
				}\t
				""",
		},
		"SUCCESS"
	);
}
/**
 * Allow selection of own enclosing instance arg for super constructor call in 1.3 compliant mode
 */
public void test064() {
	this.runConformTest(
		new String[] {
			"Foo.java",
			"""
				public class Foo {
					public static void main(String[] args) {
						System.out.println("SUCCESS");
					}
					public class Bar extends Foo {
						public Bar() {
						}
					}
					public class Baz extends Bar {
						public Baz() {
						}
					}
				}
				"""
		},
		"SUCCESS");
}

public void test065() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {\t
					public static void main(String[] arguments) {\t
						new X().new Y().new Z().bar();\t
					}\t
					String foo() { return "X-foo"; }\t
					class Y extends X {\t
						String foo() { return "Y-foo"; }\t
						class Z extends Y {\t
							Z(){\t
								//X.this.super();\t
							}\t
							String foo() { return "Z-foo"; }\t
							void bar () {\t
								System.out.println(X.this.foo());\t
							}\t
						}\t
					}\t
				}\t
				"""
		},
		"X-foo");
}

/*
 * Check that anonymous type allocation is denied access to compatible enclosing instance available as constructor argument
 */
public void test066() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  X(Object o) {}
				  class M extends X {
				    M(){
				      super(null);
				    }
				    M(Object o) {
				      super(new M(){});
				    }
				  }
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 8)
				super(new M(){});
				      ^^^^^^^^^
			No enclosing instance of type X is available due to some intermediate constructor invocation
			----------
			""");
}

/*
 * Check that indirect member type allocation is denied access to compatible enclosing instance available as constructor argument
 */
public void test067() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					X(Object o) {
					}
					class N extends X {
						N(Object o) {
							super(o);
						}
					}
					class M extends N {
						M() {
							super(null); //1
						}
						M(Object o) {
							super(new M());//2
						}
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 14)
				super(new M());//2
				      ^^^^^^^
			No enclosing instance of type X is available due to some intermediate constructor invocation
			----------
			""");
}

/*
 * Check that indirect member type allocation is denied access to compatible enclosing instance available as constructor argument
 */
public void test068() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						System.out.println("SUCCESS");
					}
					class MX1 extends X {
						MX1() {
						}
					}
					class MX2 extends MX1 {
						MX2() {
							super();	// ko
						}
						MX2(X x) {
							this();		// ok
						}
					}
				}
				""",
		},
		"SUCCESS");
}

/*
 * Check that indirect member type allocation is denied access to compatible enclosing instance available as constructor argument
 */
public void test069() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					class MX3 extends X {
						MX3(X x) {
						}
					}
					class MX4 extends MX3 {
						MX4() {
							super(new MX4());	// ko
						}
						MX4(X x) {
							this();		// ok
						}
						MX4(int i) {
							this(new MX4());		// ko
						}
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 8)
				super(new MX4());	// ko
				      ^^^^^^^^^
			No enclosing instance of type X is available due to some intermediate constructor invocation
			----------
			2. ERROR in X.java (at line 14)
				this(new MX4());		// ko
				     ^^^^^^^^^
			No enclosing instance of type X is available due to some intermediate constructor invocation
			----------
			""");
}

// binary compatibility
public void test070() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X extends Middle {
					public static void main(String argv[]) {
						System.out.println(new X().field);
					}
				}
				class Middle extends Top {
				}
				class Top {
					String field = "Top.field";
				}
				"""
		},
		"Top.field");

	this.runConformTest(
		new String[] {
			"Middle.java",
			"""
				public class Middle extends Top {
					public static void main(String[] arguments) {\s
						X.main(arguments);\t
					}\t
					String field = "Middle.field";
				}
				"""
		},
		"Top.field",
		null, // use default class-path
		false, // do not flush previous output dir content
		null); // no special vm args
}

/*
 * 43429 - AbstractMethodError calling clone() at runtime when using Eclipse compiler
 */
public void test071() {
	this.runConformTest(
		new String[] {
			"X.java", //================================
			"""
				public class X {
					public interface Copyable extends Cloneable {
						public Object clone() throws CloneNotSupportedException;
					}
					public interface TestIf extends Copyable {
					}
					public static class ClassA implements Copyable {
						public Object clone() throws CloneNotSupportedException {
							return super.clone();
						}
					}
					public static class ClassB implements TestIf {
						public Object clone() throws CloneNotSupportedException {
							return super.clone();
						}
					}
					public static void main(String[] args) throws Exception {
						Copyable o1 = new ClassA();
						ClassB o2 = new ClassB();
						TestIf o3 = o2;
						Object clonedObject;
						clonedObject = o1.clone();
						clonedObject = o2.clone();
						// The following line fails at runtime with AbstractMethodError when
						// compiled with Eclipse
						clonedObject = o3.clone();
						System.out.println("SUCCESS");
					}
				}""",
		},
		"SUCCESS");
}

public void test072() {

	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public static void main(String[] args) {
				        try {
				            f();
				        } catch(NullPointerException e) {
				            System.out.println("SUCCESS");
				        }
				    }
				    static void f() {
				        Object x = new Object() {
				            {
				                    if (true) throw null;
				            }
				        };
				    }
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 12)
				if (true) throw null;
				                ^^^^
			Cannot throw null as an exception
			----------
			""");
}

// 52221
public void test073() {

	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class X {
				    public static void main(String[] args) {
				       \s
				        switch(args.length) {
				           \s
				            case 1:
				                int i = 0;
				                class Local {
					            }
				                break;
				               \s
							case 0 :
							    System.out.println(i); // local var can be referred to, only an initialization pb
							    System.out.println(new Local());
				        		break;
				
							case 2 :
				                class Local { // not a duplicate
					            }
				        		break;
				        }
				    }
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 18)
				class Local { // not a duplicate
				      ^^^^^
			Duplicate nested type Local
			----------
			""");
}

// checking for captured outer local initialization status
// NOTE: only complain against non-inlinable outer locals
// http://bugs.eclipse.org/bugs/show_bug.cgi?id=26134
public void test074() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {\t
				    public static void main(String[] args) {\t
				    	String nonInlinedString = "[Local]";\t
				    	int i = 2;\t
						switch(i){\t
							case 1:\t
								final String displayString = nonInlinedString;
								final String inlinedString = "a";\t
								class Local {\t
									public String toString() {\t
										return inlinedString + displayString;\t
									}\t
								}\t
							case 2:\t
								System.out.print(new Local());\t
								System.out.print("-");\t
								System.out.println(new Local(){\t
									public String toString() {\t
										return super.toString()+": anonymous";\t
									}\t
								});\t
						}\t
				    }\t
				}\t
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 15)
				System.out.print(new Local());\t
				                 ^^^^^^^^^^^
			The local variable displayString may not have been initialized
			----------
			2. ERROR in X.java (at line 17)
				System.out.println(new Local(){\t
								public String toString() {\t
									return super.toString()+": anonymous";\t
								}\t
							});\t
				                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			The local variable displayString may not have been initialized
			----------
			""");
}
public void test075() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {\t
				    public void foo(int p1) {}\s
				    public void foo(short p1) {}\s
				}\t
				""",
			"Y.java",
			"""
				public class Y extends X {\t
				    public void foo(long p1) {}\s
				    public void test() { foo((short) 1); }\s
				}\t
				""",
		},
		"""
			----------
			1. ERROR in Y.java (at line 3)\r
				public void test() { foo((short) 1); } \r
				                     ^^^
			The method foo(long) is ambiguous for the type Y
			----------
			""");
}
/**
 * Test fix bug 58069 for type.
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=58069">58069</a>
 */
public void test076() {
	this.docSupport = true;
	runNegativeTest(
		new String[] {
			"IX.java",
			"""
				interface IX {
					public static class Problem extends Exception {}
				}
				""",
			"X.java",
			"""
				public abstract class X {
					public static class Problem extends Exception {}
					public abstract static class InnerClass implements IX {
						/**
						 * @throws Problem\s
						 */
						public void foo() throws IllegalArgumentException {
						}
					}
				}
				
				"""
		},
		"""
			----------
			1. WARNING in IX.java (at line 2)
				public static class Problem extends Exception {}
				                    ^^^^^^^
			The serializable class Problem does not declare a static final serialVersionUID field of type long
			----------
			----------
			1. WARNING in X.java (at line 2)
				public static class Problem extends Exception {}
				                    ^^^^^^^
			The serializable class Problem does not declare a static final serialVersionUID field of type long
			----------
			2. ERROR in X.java (at line 5)
				* @throws Problem\s
				          ^^^^^^^
			Javadoc: The type Problem is defined in an inherited type and an enclosing scope
			----------
			"""
	);
}
/**
 * Test fix bug 58069 for method.
 * Note that problem is not flagged in doc comments as it is only raised while verifying
 * implicit method and javadoc resolution does not use it.
 */
public void test077() {
	this.docSupport = true;
	this.runNegativeTest(
		new String[] {
			"p1/Test.java",
			"""
				package p1;\s
				public class Test {\s
					public static void main(String[] arguments) {\s
						new Test().foo();\s
					}\s
					String bar() {\s
						return "FAILED";\t
					}\s
					void foo(){\s
						/** @see #bar() */
						class Y extends Secondary {\s
							/** @see #bar() */
							String z = bar();\t
						};\s
						System.out.println(new Y().z);\t
					}\s
				}\s
				class Secondary {\s
					String bar(){ return "FAILED"; }\s
				}\s
				"""
		},
		"""
			----------
			1. ERROR in p1\\Test.java (at line 13)
				String z = bar();\t
				           ^^^
			The method bar is defined in an inherited type and an enclosing scope
			----------
			"""
	);
}
/**
 * Test fix bug 58069 for field.
 * Note that problem is not flagged in doc comments as it is only raised while verifying
 * Name or Qualified name references and javadoc reference is a field reference.
 */
public void test078() {
	this.docSupport = true;
	this.runNegativeTest(
		new String[] {
			"p1/Test.java",
			"""
				package p1;\s
				public class Test {\s
					public static void main(String[] arguments) {\s
						new Test().foo();\s
					}\s
					String bar = "FAILED";\
					void foo(){\s
						/** @see #bar */
						class Y extends Secondary {\s
							/** @see #bar */
							String z = bar;\s
						};\s
						System.out.println(new Y().z);\t
					}\s
				}\s
				class Secondary {\s
					String bar = "FAILED";\s
				}\s
				"""
		},
		"""
			----------
			1. ERROR in p1\\Test.java (at line 10)
				String z = bar;\s
				           ^^^
			The field bar is defined in an inherited type and an enclosing scope\s
			----------
			"""
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=47227
 */
public void test079() {
	this.runNegativeTest(
		new String[] {
			"Hello.java",
			"""
				void ___eval() {
					new Runnable() {
						int ___run() throws Throwable {
							return blah;
						}
						private String blarg;
						public void run() {
						}
					};
				}
				public class Hello {
					private static int x;
					private String blah;
					public static void main(String[] args) {
					}
					public void hello() {
					}
					public boolean blah() {
						return false;
					}
					public void foo() {
					}
				}
				"""
		},
		"""
		----------
		1. ERROR in Hello.java (at line 1)
			void ___eval() {
			^
		The preview feature Unnamed Classes and Instance Main Methods is only available with source level 21 and above
		----------
		2. ERROR in Hello.java (at line 4)
			return blah;
			       ^^^^
		blah cannot be resolved to a variable
		----------
		3. ERROR in Hello.java (at line 14)
			public static void main(String[] args) {
			                   ^^^^^^^^^^^^^^^^^^^
		The method main cannot be declared static; static methods can only be declared in a static or top level type
		----------
		"""
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=67643
 * from 1.5 source level on most specific common super type is allowed
 */
public void test080() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				public class X {
				    private static class C1 extends ArrayList {
				    }
				    private static class C2 extends ArrayList {
				    }
				    public static void main(String[] args) {
						ArrayList list = args == null ? new C1(): new C2();
						System.out.println("SUCCESS");
					}
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 3)
				private static class C1 extends ArrayList {
				                     ^^
			The serializable class C1 does not declare a static final serialVersionUID field of type long
			----------
			2. WARNING in X.java (at line 5)
				private static class C2 extends ArrayList {
				                     ^^
			The serializable class C2 does not declare a static final serialVersionUID field of type long
			----------
			3. ERROR in X.java (at line 8)
				ArrayList list = args == null ? new C1(): new C2();
				                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Incompatible conditional operand types X.C1 and X.C2
			----------
			""");
}
public void test081() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public X foo() { return this; }\s
				}
				class Y extends X {
				    public Y foo() { return this; }\s
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				public Y foo() { return this; }\s
				       ^
			The return type is incompatible with X.foo()
			----------
			""");
}
// covariance
public void test082() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				\t
					public static void main(String[] args) {
						X x = new X1();
						System.out.println(x.foo());
					}
					Object foo() {
						return null;
					}
				}
				
				class X1 extends X {
					String foo() {
						return "SUCCESS";
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 13)
				String foo() {
				^^^^^^
			The return type is incompatible with X.foo()
			----------
			""");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=66533
 */
public void test084() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void foo() {
						Object enum = null;
					}
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 3)
				Object enum = null;
				       ^^^^
			\'enum\' should not be used as an identifier, since it is a reserved keyword from source level 1.5 on
			----------
			""");
}
/**
 * Test unused import with static
 */
public void test085() {
	this.runNegativeTest(
		new String[] {
			"A.java",
			"""
				import static j.l.S.*;
				import static j.l.S.in;
				
				public class A {
				
				}
				""",
			"j/l/S.java",
			"""
				package j.l;
				public class S {
					public static int in;
				}
				"""
		},
		"""
			----------
			1. ERROR in A.java (at line 1)
				import static j.l.S.*;
				^^^^^^^^^^^^^^^^^^^^^^
			Syntax error, static imports are only available if source level is 1.5 or greater
			----------
			2. ERROR in A.java (at line 2)
				import static j.l.S.in;
				^^^^^^^^^^^^^^^^^^^^^^^
			Syntax error, static imports are only available if source level is 1.5 or greater
			----------
			3. ERROR in A.java (at line 2)
				import static j.l.S.in;
				              ^^^^^^^^
			The import j.l.S.in cannot be resolved
			----------
			"""
		);
}
/**
 * Test invalid static import syntax
 */
public void test086() {
	this.runNegativeTest(
		new String[] {
			"p/S.java",
			"""
				package p;
				public class S {
				    public final static String full = "FULL";
				    public final static String success = "SUCCESS";
				}
				""",
			"X.java",
			"""
				import static p.S;
				public class X {
					public static void main ( String[] args) {
					\t
				      System.out.print(full+" "+p.S.success);
				   }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				import static p.S;
				^^^^^^^^^^^^^^^^^^
			Syntax error, static imports are only available if source level is 1.5 or greater
			----------
			2. ERROR in X.java (at line 5)
				System.out.print(full+" "+p.S.success);
				                 ^^^^
			full cannot be resolved to a variable
			----------
			"""
		);
}
public void test087() {
	this.runNegativeTest(
		new String[] {
			"p/S.java",
			"""
				public class S {
				    public final static String full = "FULL";
				    public final static String success = "SUCCESS";
				}
				""",
			"X.java",
			"""
				import static S;
				public class X {
					public static void main ( String[] args) {
					\t
				      System.out.print(full+" "+S.success);
				   }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				import static S;
				^^^^^^^^^^^^^^^^
			Syntax error, static imports are only available if source level is 1.5 or greater
			----------
			2. ERROR in X.java (at line 5)
				System.out.print(full+" "+S.success);
				                 ^^^^
			full cannot be resolved to a variable
			----------
			"""
		);
}
public void test088() {
	this.runNegativeTest(
		new String[] {
			"p/X.java",
			"""
				package p;
				import java.util.Date;
				import java.lang.reflect.*;
				public class X extends Date implements Runnable{
				\s
				 Integer w = Integer.valueOf(90);
				 protected double x = 91.1;
				 public long y = 92;
				 static public Boolean z = Boolean.valueOf(true);\s
				 public class X_inner {
				  public X_inner() {
				   this.super();
				   System.out.println("....");
				  }
				 }
				 X_inner a = new X_inner();
				 public interface X_interface {
				   public void f();\s
				 }
				 static {
				  System.out.println("Static initializer");
				 }
				 public X() { }\s
				 public X(int a1,int b1) { }\s
				 private void a() { System.out.println("A");}\s
				 protected void b() { System.out.println("B");}\s
				 public void c() { System.out.println("C");}\s
				 static public int d() {System.out.println("Static D");return -1;}\s
				 public static void main(String args[]) {
				  X  b = new X();
				  Class c = b.getClass();
				  Class _getClasses [] = X.class.getClasses();\s
				//  System.out.println(_getClasses[0].toString());
				//  System.out.println(_getClasses[1].toString());
				  if (_getClasses.length == 0) {System.out.println("FAILED");};
				  Constructor _getConstructors[] = c.getConstructors();\s
				  try {
				   Field _getField = c.getField("y");
				   Method _getMethod = c.getMethod("d",null);
				\s
				   Boolean b_z = X.z;\s
				  }
				  catch (NoSuchFieldException e) { System.out.println("NoSuchFieldException");}
				  catch (NoSuchMethodException e) { System.out.println("NoSuchMethodException");};
				 }\s
				 public void run() {System.out.println("RUN");}\s
				}""",
		},
		"""
			----------
			1. WARNING in p\\X.java (at line 4)
				public class X extends Date implements Runnable{
				             ^
			The serializable class X does not declare a static final serialVersionUID field of type long
			----------
			2. ERROR in p\\X.java (at line 12)
				this.super();
				^^^^
			Illegal enclosing instance specification for type Object
			----------
			""");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78089
 */
public void test089() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
				    @interface I1 {}
				}
				
				public class X {
				    public static void main(String argv[])   {
				    	System.out.print("SUCCESS");
				    }
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				@interface I1 {}
				           ^^
			Syntax error, annotation declarations are only available if source level is 1.5 or greater
			----------
			""");
}
//78104
public void test090() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				\t
					void foo(int[] ints, Object o) {
						ints = ints.clone();
						ints = (int[])ints.clone();
						X x = this.clone();
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				ints = ints.clone();
				       ^^^^^^^^^^^^
			Type mismatch: cannot convert from Object to int[]
			----------
			2. ERROR in X.java (at line 6)
				X x = this.clone();
				      ^^^^^^^^^^^^
			Type mismatch: cannot convert from Object to X
			----------
			"""
	);
}
//78104 - variation
public void test091() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				\t
					public static void main(String[] args) {
						args = args.clone();
					}
				}""",
		},
		"""
			----------
			1. ERROR in X.java (at line 4)\r
				args = args.clone();\r
				       ^^^^^^^^^^^^
			Type mismatch: cannot convert from Object to String[]
			----------
			"""
	);
}
// check autoboxing only enabled in 5.0 source mode
public void test092() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void foo(Boolean b) {
						if (b) {\s
							int i = 0;
						}
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				if (b) {\s
				    ^
			Type mismatch: cannot convert from Boolean to boolean
			----------
			"""
	);
}
public void test093() {
	this.runNegativeTest(
		new String[] {
			"p/X_1.java",
			"""
				package p;
				/*   dena JTest Suite, Version 2.2, September 1997
				 *   Copyright (c) 1995-1997 Modena Software (I) Pvt. Ltd., All Rights Reserved
				 */
				/*  Section    :  Inner classes\s
				 *  FileName   :  ciner026.java
				 *  Purpose    :  Positive test for Inner classes
				 * \s
				 *  An anonymous class can have initializers but cannot have a constructor.
				 *  The argument list of the associated new expression is implicitely\s
				 *  passed to the constructor of the super class.\s
				 *
				 */
				\s
				 class X_1 {
				  static int xx = 100;
				  //inner class Y \s
				  static class Y { \s
				   public int j = 0;
				   Y(int x){ j = x; }
				   } \s
				 public void call_inner()
				 {
				   int i = test_anonymous().j;
				 }    \s
				 public static void main(String argv[])
				 {
				   X_1 ox = new X_1();
				   ox.call_inner();\s
				 } \s
				public void newMethod ( ) {
				  Float f1 = null;
				  f1=(f1==0.0)?1.0:f1;
				}
				   static Y test_anonymous()
				   {\s
				    //anonymous implementation of class Y
				    return new Y(xx) //xx should be implicitely passed to Y()
				    {
				    };   \s
				  \s
				   } //end test_anonymous     \s
				} """,
		},
		"""
			----------
			1. ERROR in p\\X_1.java (at line 33)
				f1=(f1==0.0)?1.0:f1;
				   ^^^^^^^^^
			Incompatible operand types Float and double
			----------
			"""
	);
}
/*
 * Test unused import warning in presence of syntax errors
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=21022
 */
public void test094(){

	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.io.*;\t
				public class X {\t
					void foo(){
						()
						IOException e;
					}\s
				}	\t
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				()
				^^
			Syntax error on tokens, delete these tokens
			----------
			""");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=84743
public void test095(){

	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface I {
				   int foo();
				}
				interface J {
				   String foo();
				}
				\s
				public class X implements I {
				   public int foo() {
				 	return 0;
				   }
				   public static void main(String[] args) {
				         I i = new X();
				         try {
					        J j = (J) i;
				         } catch(ClassCastException e) {
					        System.out.println("SUCCESS");
				         }
				  }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 15)
				J j = (J) i;
				      ^^^^^
			Cannot cast from I to J
			----------
			""");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=47074
 */
public void test096() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				
				    interface A {
				       void doSomething();
				    }
				
				    interface B {
				       int doSomething();
				    }
				
				    interface C extends B {
				    }
				
				    public static void main(String[] args) {
				       \s
				        A a = null;
				        C c = (C)a; //COMPILER ERROR
				    }
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 17)
				C c = (C)a; //COMPILER ERROR
				      ^^^^
			Cannot cast from X.A to X.C
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=79396
public void test097() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				
				    public static void main(String argv[]) {
				    	int cst = X1.CST;
				        X2.Root.foo();
				    }
				    static void foo() {}
				}
				
				class X1 {
				    static {
						System.out.print("[X1]");
				    }
				    public static final int CST = 12;
				    static X Root = null;
				}
				class X2 {
				    static {
						System.out.print("[X2]");
				    }
				    public final int CST = 12;
				    static X Root = null;
				}
				"""
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=78906
public void test098() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void foo() {
						System.out.print("foo");
					}
					class Y {
						String this$0;
						String this$0$;
						void print() {\s
							foo();
							System.out.println(this$0+this$0$);
						}
					}
					public static void main(String[] args) {
						X.Y y = new X().new Y();
						y.this$0 = "hello";
						y.this$0$ = "world";
						y.print();
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				String this$0;
				       ^^^^^^
			Duplicate field X.Y.this$0
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=77349
public void test099() {
	this.runNegativeTest(
		new String[] {
			"I.java",
			"""
				public interface I extends Cloneable {
					class Inner {
						Object bar(I i) throws CloneNotSupportedException { return i.clone(); }
					}
				}
				"""
		},
		"""
			----------
			1. WARNING in I.java (at line 3)
				Object bar(I i) throws CloneNotSupportedException { return i.clone(); }
				                                                           ^^^^^^^^^
			Access to enclosing method clone() from the type Object is emulated by a synthetic accessor method
			----------
			""",
		null,
		true,
		null,
		"java.lang.ClassFormatError"
		// no compile errors but generates ClassFormatError if run
	);
}

public void test100() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    int \\ud800\\udc05\\ud800\\udc04\\ud800\\udc03\\ud800\\udc02\\ud800\\udc01\\ud800\\udc00;
				    void foo() {
				        int \\ud800\\udc05\\ud800\\udc04\\ud800\\udc03\\ud800\\udc02\\ud800\\udc01\\ud800\\udc00;
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				int \\ud800\\udc05\\ud800\\udc04\\ud800\\udc03\\ud800\\udc02\\ud800\\udc01\\ud800\\udc00;
				    ^^^^^^
			Invalid unicode
			----------
			2. ERROR in X.java (at line 4)
				int \\ud800\\udc05\\ud800\\udc04\\ud800\\udc03\\ud800\\udc02\\ud800\\udc01\\ud800\\udc00;
				    ^^^^^^
			Invalid unicode
			----------
			"""
	);
}
public void test101() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					Character c0 = \'a\';
					public static void main(String argv[]) {
						Character c1;
						c1 = \'b\';
				
						Character c2 = \'c\';
						Character[] c3 = { \'d\' };
				\t
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				Character c0 = \'a\';
				               ^^^
			Type mismatch: cannot convert from char to Character
			----------
			2. ERROR in X.java (at line 5)
				c1 = \'b\';
				     ^^^
			Type mismatch: cannot convert from char to Character
			----------
			3. ERROR in X.java (at line 7)
				Character c2 = \'c\';
				               ^^^
			Type mismatch: cannot convert from char to Character
			----------
			4. ERROR in X.java (at line 8)
				Character[] c3 = { \'d\' };
				                   ^^^
			Type mismatch: cannot convert from char to Character
			----------
			"""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=108856
public void test102() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] s) {
						new Object() {
							{
								new Object() {
									{
										System.out.println(this.getClass().getName());
									}
								};
							}
						};
					}
				}
				"""
		},
		"X$2");
}
public void test103() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public static void main(String[] args) {
						System.out.print(X.class);
				    }
				}
				""",
		},
		"class X");

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();

	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);

	String expectedOutput =
		"""
		// Compiled from X.java (version 1.1 : 45.3, super bit)
		public class X {
		 \s
		  // Field descriptor #6 Ljava/lang/Class;
		  static synthetic java.lang.Class class$0;
		 \s
		  // Method descriptor #9 ()V
		  // Stack: 1, Locals: 1
		  public X();
		    0  aload_0 [this]
		    1  invokespecial java.lang.Object() [11]
		    4  return
		      Line numbers:
		        [pc: 0, line: 1]
		      Local variable table:
		        [pc: 0, pc: 5] local: this index: 0 type: X
		 \s
		  // Method descriptor #18 ([Ljava/lang/String;)V
		  // Stack: 3, Locals: 1
		  public static void main(java.lang.String[] args);
		     0  getstatic java.lang.System.out : java.io.PrintStream [19]
		     3  getstatic X.class$0 : java.lang.Class [25]
		     6  dup
		     7  ifnonnull 35
		    10  pop
		    11  ldc <String "X"> [27]
		    13  invokestatic java.lang.Class.forName(java.lang.String) : java.lang.Class [28]
		    16  dup
		    17  putstatic X.class$0 : java.lang.Class [25]
		    20  goto 35
		    23  new java.lang.NoClassDefFoundError [34]
		    26  dup_x1
		    27  swap
		    28  invokevirtual java.lang.Throwable.getMessage() : java.lang.String [36]
		    31  invokespecial java.lang.NoClassDefFoundError(java.lang.String) [42]
		    34  athrow
		    35  invokevirtual java.io.PrintStream.print(java.lang.Object) : void [45]
		    38  return
		      Exception Table:
		        [pc: 11, pc: 16] -> 23 when : java.lang.ClassNotFoundException
		      Line numbers:
		        [pc: 0, line: 3]
		        [pc: 38, line: 4]
		      Local variable table:
		        [pc: 0, pc: 39] local: args index: 0 type: java.lang.String[]
		}""";

	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 2));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, actualOutput);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=125570
public void test104() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] s) {
						new Object() {
							{
								new Object() {
									{
										class Y {
											{
												System.out.print(this.getClass());
												System.out.print(\' \');
												System.out.print(this.getClass().getName());
											}
										}
										;
										new Y();
									}
								};
							}
						};
					}
				}"""
		},
		"class X$1$Y X$1$Y");
}

// enclosing instance - note that the behavior is different in 1.5
public void test105() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    static class Y { }
				    static class Z1 {
				        Runnable m;
				        Z1(Runnable p) {
				            this.m = p;
				        }
				    }
				    class Z2 extends Z1 {
				        Z2(final Y p) {
				            super(new Runnable() {
				                public void run() {
				                    foo(p);
				                }
				            });
				        }
				    }
				    void foo(Y p) { }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 13)
				foo(p);
				^^^^^^
			No enclosing instance of the type X is accessible in scope
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=79798
public void test106() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.io.*;
				import java.util.zip.*;
				public class X {
					void x() throws ZipException {
						IandJ ij= new K();
						ij.m();
					}
					void y() throws ZipException {
						K k= new K();
						k.m();
					}
				}
				interface I { void m() throws IOException; }
				interface J { void m() throws ZipException; }
				interface IandJ extends I, J {}
				class K implements IandJ { public void m() throws ZipException { } }"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				ij.m();
				^^^^^^
			Unhandled exception type IOException
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=79798
public void test107() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						C c = new D();
						c.xyz();
					}
				}
				class AException extends Exception { }
				class BException extends Exception { }
				interface A { void xyz() throws AException; }
				interface B { void xyz() throws BException; }
				interface C extends A, B { }
				class D implements C {
					public void xyz() { System.out.println(1); }
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				c.xyz();
				^^^^^^^
			Unhandled exception type AException
			----------
			2. WARNING in X.java (at line 7)
				class AException extends Exception { }
				      ^^^^^^^^^^
			The serializable class AException does not declare a static final serialVersionUID field of type long
			----------
			3. WARNING in X.java (at line 8)
				class BException extends Exception { }
				      ^^^^^^^^^^
			The serializable class BException does not declare a static final serialVersionUID field of type long
			----------
			""");
}
}
