/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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
 *								bug 328281 - visibility leaks not detected when analyzing unused field in private class
 *								bug 379784 - [compiler] "Method can be static" is not getting reported
 *								bug 379834 - Wrong "method can be static" in presence of qualified super and different staticness of nested super class.
 *     Jesper S Moller <jesper@selskabet.org> - Contributions for
 *								bug 378674 - "The method can be declared as static" is wrong
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ProblemTypeAndMethodTest extends AbstractRegressionTest {
public ProblemTypeAndMethodTest(String name) {
	super(name);
}
// Static initializer to specify tests subset using TESTS_* static variables
// All specified tests which does not belong to the class are skipped...
static {
//		TESTS_NAMES = new String[] { "test376550" };
//		TESTS_NUMBERS = new int[] { 113 };
//		TESTS_RANGE = new int[] { 108, -1 };
}

public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}

public static Class testClass() {
	return ProblemTypeAndMethodTest.class;
}
public void test001() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				        interface Moosh { void foo(); }
				
				        static abstract class A implements Moosh {}
				
				        static class W extends A {}
				        static class Y extends A {}
				        static class Z extends A {}
				        public static void main(String[] args) {
				                new W();  // throws ClassFormatError
				        }
				}"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				static class W extends A {}
				             ^
			The type X.W must implement the inherited abstract method X.Moosh.foo()
			----------
			2. ERROR in X.java (at line 7)
				static class Y extends A {}
				             ^
			The type X.Y must implement the inherited abstract method X.Moosh.foo()
			----------
			3. ERROR in X.java (at line 8)
				static class Z extends A {}
				             ^
			The type X.Z must implement the inherited abstract method X.Moosh.foo()
			----------
			""",
		null /* no extra class libraries */,
		true /* flush output directory */,
		null /* no custom options */,
		true /* do not generate output */,
		false /* do not show category */,
		false /* do not show warning token */,
		false  /* do not skip javac for this peculiar test */,
		false  /* do not perform statements recovery */);
	ClassFileReader reader = getClassFileReader(OUTPUT_DIR + File.separator  +"X$W.class", "X$W");
	IBinaryMethod[] methods = reader.getMethods();
	assertEquals("Wrong size", 2, methods.length);
	int counter = 0;
	for (int i = 0; i < 2; i++) {
		IBinaryMethod method = methods[i];
		if (new String(method.getSelector()).equals("foo")) {
			counter++;
		}
	}
	assertEquals("Wrong number of foo method", 1, counter);

	reader = getClassFileReader(OUTPUT_DIR + File.separator  +"X$Y.class", "X$Y");
	methods = reader.getMethods();
	assertEquals("Wrong size", 2, methods.length);
	counter = 0;
	for (int i = 0; i < 2; i++) {
		IBinaryMethod method = methods[i];
		if (new String(method.getSelector()).equals("foo")) {
			counter++;
		}
	}
	assertEquals("Wrong number of foo method", 1, counter);

	reader = getClassFileReader(OUTPUT_DIR + File.separator  +"X$Z.class", "X$Z");
	methods = reader.getMethods();
	assertEquals("Wrong size", 2, methods.length);
	counter = 0;
	for (int i = 0; i < 2; i++) {
		IBinaryMethod method = methods[i];
		if (new String(method.getSelector()).equals("foo")) {
			counter++;
		}
	}
	assertEquals("Wrong number of foo method", 1, counter);
}

public void test002() {
	this.runNegativeTest(new String[] {
			"X.java",
			"""
				public class X extends Zork {
					void foo() {
						Zork z = this;
						String s = this;
						Zork2 z2 = this;
					}
					Zork fz = this;
					String fs = this;
					Zork2 fz2 = this;
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 1)
				public class X extends Zork {
				                       ^^^^
			Zork cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 3)
				Zork z = this;
				^^^^
			Zork cannot be resolved to a type
			----------
			3. ERROR in X.java (at line 4)
				String s = this;
				           ^^^^
			Type mismatch: cannot convert from X to String
			----------
			4. ERROR in X.java (at line 5)
				Zork2 z2 = this;
				^^^^^
			Zork2 cannot be resolved to a type
			----------
			5. ERROR in X.java (at line 7)
				Zork fz = this;
				^^^^
			Zork cannot be resolved to a type
			----------
			6. ERROR in X.java (at line 8)
				String fs = this;
				            ^^^^
			Type mismatch: cannot convert from X to String
			----------
			7. ERROR in X.java (at line 9)
				Zork2 fz2 = this;
				^^^^^
			Zork2 cannot be resolved to a type
			----------
			""");
}

public void test003() {
	this.runNegativeTest(new String[] {
			"X.java",
			"""
				public class X {
					Zork field;
				\t
					void foo(Y y) {
						Object o = y.foo();
						Object s = y.slot;
						y.bar(null);
						Object s2 = new Y().slot;
						Object f = field;
					}
				}
				class Y {
					Zork foo() {	return null; }
					void bar(Zork z) {}
					Zork slot;
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				Zork field;
				^^^^
			Zork cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 5)
				Object o = y.foo();
				             ^^^
			The method foo() from the type Y refers to the missing type Zork
			----------
			3. ERROR in X.java (at line 6)
				Object s = y.slot;
				           ^^^^^^
			Zork cannot be resolved to a type
			----------
			4. ERROR in X.java (at line 7)
				y.bar(null);
				  ^^^
			The method bar(Zork) from the type Y refers to the missing type Zork
			----------
			5. ERROR in X.java (at line 8)
				Object s2 = new Y().slot;
				            ^^^^^^^^^^^^
			Zork cannot be resolved to a type
			----------
			6. ERROR in X.java (at line 9)
				Object f = field;
				           ^^^^^
			Zork cannot be resolved to a type
			----------
			7. ERROR in X.java (at line 13)
				Zork foo() {	return null; }
				^^^^
			Zork cannot be resolved to a type
			----------
			8. ERROR in X.java (at line 14)
				void bar(Zork z) {}
				         ^^^^
			Zork cannot be resolved to a type
			----------
			9. ERROR in X.java (at line 15)
				Zork slot;
				^^^^
			Zork cannot be resolved to a type
			----------
			""");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200
public void test004() {
	this.runConformTest(
			new String[] {
					"p/OtherFoo.java", //-----------------------------------------------------------------------
					"""
						package p;
						
						import q.Zork;
						
						public class OtherFoo extends Zork{
							public class OtherMember extends Zork {}
							public Zork foo;
							public Zork bar() {	return null; }
							public void baz(Zork z) {}
						}
						""",
					"q/Zork.java", //-----------------------------------------------------------------------
					"""
						package q;
						public class Zork {
						}
						""",
			},
			"");
	this.runNegativeTest(
		new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					import p.OtherFoo;
					import q.Zork;
					
					public class X {
						void foo() {
							OtherFoo ofoo;
							String s1 = ofoo.foo;
							String s2 = ofoo.bar();
							String s3 = ofoo.new OtherMember();
							ofoo.baz(this);
						}
						void bar() {
							OtherX ox;
							String s1 = ox.foo;
							String s2 = ox.bar();
							String s3 = ox.new OtherMember();
							ox.baz(this);
						}
					}\t
					
					class OtherX {
						public class OtherMember extends Zork {}
						public Zork foo;
						public Zork bar() {	return null; }
						public void baz(Zork z) {}
					}
					""",
		},
		"""
			----------
			1. ERROR in X.java (at line 7)
				String s1 = ofoo.foo;
				            ^^^^^^^^
			Type mismatch: cannot convert from Zork to String
			----------
			2. ERROR in X.java (at line 8)
				String s2 = ofoo.bar();
				            ^^^^^^^^^^
			Type mismatch: cannot convert from Zork to String
			----------
			3. ERROR in X.java (at line 9)
				String s3 = ofoo.new OtherMember();
				            ^^^^^^^^^^^^^^^^^^^^^^
			Type mismatch: cannot convert from OtherFoo.OtherMember to String
			----------
			4. ERROR in X.java (at line 10)
				ofoo.baz(this);
				     ^^^
			The method baz(Zork) in the type OtherFoo is not applicable for the arguments (X)
			----------
			5. ERROR in X.java (at line 14)
				String s1 = ox.foo;
				            ^^^^^^
			Type mismatch: cannot convert from Zork to String
			----------
			6. ERROR in X.java (at line 15)
				String s2 = ox.bar();
				            ^^^^^^^^
			Type mismatch: cannot convert from Zork to String
			----------
			7. ERROR in X.java (at line 16)
				String s3 = ox.new OtherMember();
				            ^^^^^^^^^^^^^^^^^^^^
			Type mismatch: cannot convert from OtherX.OtherMember to String
			----------
			8. ERROR in X.java (at line 17)
				ox.baz(this);
				   ^^^
			The method baz(Zork) in the type OtherX is not applicable for the arguments (X)
			----------
			""",
		null,
		false);

	// delete binary file Zork (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "q" + File.separator + "Zork.class"));

	this.runNegativeTest(
			new String[] {
					"X.java", //-----------------------------------------------------------------------
					"""
						import p.OtherFoo;
						import q.Zork;
						
						public class X {
							void foo() {
								OtherFoo ofoo;
								String s1 = ofoo.foo;
								String s2 = ofoo.bar();
								String s3 = ofoo.new OtherMember();
								ofoo.baz(this);
							}
							void bar() {
								OtherX ox;
								String s1 = ox.foo;
								String s2 = ox.bar();
								String s3 = ox.new OtherMember();
								ox.baz(this);
							}
						}\t
						
						class OtherX {
							public class OtherMember extends Zork {}
							public Zork foo;
							public Zork bar() {	return null; }
							public void baz(Zork z) {}
						}
						""",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					import p.OtherFoo;
					^
				The type q.Zork cannot be resolved. It is indirectly referenced from required type p.OtherFoo
				----------
				2. ERROR in X.java (at line 2)
					import q.Zork;
					       ^^^^^^
				The import q.Zork cannot be resolved
				----------
				3. ERROR in X.java (at line 7)
					String s1 = ofoo.foo;
					            ^^^^^^^^
				Zork cannot be resolved to a type
				----------
				4. ERROR in X.java (at line 8)
					String s2 = ofoo.bar();
					                 ^^^
				The method bar() from the type OtherFoo refers to the missing type Zork
				----------
				5. ERROR in X.java (at line 9)
					String s3 = ofoo.new OtherMember();
					            ^^^^^^^^^^^^^^^^^^^^^^
				Type mismatch: cannot convert from OtherFoo.OtherMember to String
				----------
				6. ERROR in X.java (at line 10)
					ofoo.baz(this);
					     ^^^
				The method baz(Zork) from the type OtherFoo refers to the missing type Zork
				----------
				7. ERROR in X.java (at line 14)
					String s1 = ox.foo;
					            ^^^^^^
				Zork cannot be resolved to a type
				----------
				8. ERROR in X.java (at line 15)
					String s2 = ox.bar();
					               ^^^
				The method bar() from the type OtherX refers to the missing type Zork
				----------
				9. ERROR in X.java (at line 16)
					String s3 = ox.new OtherMember();
					            ^^^^^^^^^^^^^^^^^^^^
				Type mismatch: cannot convert from OtherX.OtherMember to String
				----------
				10. ERROR in X.java (at line 17)
					ox.baz(this);
					   ^^^
				The method baz(Zork) from the type OtherX refers to the missing type Zork
				----------
				11. ERROR in X.java (at line 22)
					public class OtherMember extends Zork {}
					                                 ^^^^
				Zork cannot be resolved to a type
				----------
				12. ERROR in X.java (at line 23)
					public Zork foo;
					       ^^^^
				Zork cannot be resolved to a type
				----------
				13. ERROR in X.java (at line 24)
					public Zork bar() {	return null; }
					       ^^^^
				Zork cannot be resolved to a type
				----------
				14. ERROR in X.java (at line 25)
					public void baz(Zork z) {}
					                ^^^^
				Zork cannot be resolved to a type
				----------
				""",
			null,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test005() {
	this.runConformTest(
			new String[] {
					"p/OtherFoo.java", //-----------------------------------------------------------------------
					"""
						package p;
						
						import q1.q2.Zork;
						
						public class OtherFoo extends Zork{
							public class OtherMember extends Zork {}
							public Zork foo;
							public Zork bar() {	return null; }
							public void baz(Zork z) {}
						}
						""",
					"q1/q2/Zork.java", //-----------------------------------------------------------------------
					"""
						package q1.q2;
						public class Zork {
						}
						""",
			},
			"");

	// delete binary folder q1 (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "q1"));

	runNegativeTest(
		// test directory preparation
		false /* do not flush output directory */,
		new String[] { /* test files */
			"X.java", //-----------------------------------------------------------------------
			"""
				public class X {
					void foo() {
						p.OtherFoo ofoo = new p.OtherFoo();
						ofoo.bar();
						q1.q2.Zork z;
					}
				}\t
				""",
		},
		// compiler options
		null /* no class libraries */,
		null /* no custom options */,
		"""
			----------
			1. ERROR in X.java (at line 4)
				ofoo.bar();
				^^^^^^^^^^
			The type q1.q2.Zork cannot be resolved. It is indirectly referenced from required type p.OtherFoo
			----------
			2. ERROR in X.java (at line 4)
				ofoo.bar();
				     ^^^
			The method bar() from the type OtherFoo refers to the missing type Zork
			----------
			3. ERROR in X.java (at line 5)
				q1.q2.Zork z;
				^^^^^^^^^^
			q1.q2.Zork cannot be resolved to a type
			----------
			""",
		// javac options
		JavacTestOptions.SKIP_UNTIL_FRAMEWORK_FIX /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test006() {
	this.runConformTest(
			new String[] {
					"p/OtherFoo.java", //-----------------------------------------------------------------------
					"""
						package p;
						
						import q1.q2.Zork;
						
						public class OtherFoo extends Zork{
							public class OtherMember extends Zork {}
							public Zork foo;
							public Zork bar() {	return null; }
							public void baz(Zork z) {}
						}
						""",
					"q1/q2/Zork.java", //-----------------------------------------------------------------------
					"""
						package q1.q2;
						public class Zork {
						}
						""",
			},
			"");

	// delete binary folder q1 (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "q1"));

	runNegativeTest(
		// test directory preparation
		false /* do not flush output directory */,
		new String[] { /* test files */
			"X.java", //-----------------------------------------------------------------------
			"""
				import q1.q2.*;
				public class X {
					void foo() {
						p.OtherFoo ofoo = new p.OtherFoo();
						ofoo.bar();
						Zork z;
					}
				}\t
				""",
		},
		// compiler options
		null /* no class libraries */,
		null /* no custom options */,
		"""
			----------
			1. ERROR in X.java (at line 1)
				import q1.q2.*;
				       ^^
			The import q1 cannot be resolved
			----------
			2. ERROR in X.java (at line 5)
				ofoo.bar();
				^^^^^^^^^^
			The type q1.q2.Zork cannot be resolved. It is indirectly referenced from required type p.OtherFoo
			----------
			3. ERROR in X.java (at line 5)
				ofoo.bar();
				     ^^^
			The method bar() from the type OtherFoo refers to the missing type Zork
			----------
			4. ERROR in X.java (at line 6)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			""",
		// javac options
		JavacTestOptions.SKIP_UNTIL_FRAMEWORK_FIX /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test007() {
	this.runConformTest(
			new String[] {
					"p/OtherFoo.java", //-----------------------------------------------------------------------
					"""
						package p;
						
						import q1.q2.Zork;
						
						public class OtherFoo extends Zork{
							public class OtherMember extends Zork {}
							public Zork foo;
							public Zork bar() {	return null; }
							public void baz(Zork z) {}
						}
						""",
					"q1/q2/Zork.java", //-----------------------------------------------------------------------
					"""
						package q1.q2;
						public class Zork {
						}
						""",
			},
			"");

	// delete binary folder q1 (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "q1"));

	runNegativeTest(
		// test directory preparation
		false /* do not flush output directory */,
		new String[] { /* test files */
			"X.java", //-----------------------------------------------------------------------
			"""
				import q1.q2.Zork;
				public class X {
					void foo() {
						p.OtherFoo ofoo = new p.OtherFoo();
						ofoo.bar();
						Zork z;
					}
				}\t
				""",
		},
		// compiler options
		null /* no class libraries */,
		null /* no custom options */,
		"""
			----------
			1. ERROR in X.java (at line 1)
				import q1.q2.Zork;
				       ^^
			The import q1 cannot be resolved
			----------
			2. ERROR in X.java (at line 5)
				ofoo.bar();
				^^^^^^^^^^
			The type q1.q2.Zork cannot be resolved. It is indirectly referenced from required type p.OtherFoo
			----------
			3. ERROR in X.java (at line 5)
				ofoo.bar();
				     ^^^
			The method bar() from the type OtherFoo refers to the missing type Zork
			----------
			4. ERROR in X.java (at line 6)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			""",
		// javac options
		JavacTestOptions.SKIP_UNTIL_FRAMEWORK_FIX /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test008() {
	this.runConformTest(
			new String[] {
					"p/OtherFoo.java", //-----------------------------------------------------------------------
					"""
						package p;
						
						import q1.q2.Zork;
						
						public class OtherFoo extends Zork{
							public class OtherMember extends Zork {}
							public Zork foo;
							public Zork bar() {	return null; }
							public void baz(Zork z) {}
						}
						""",
					"q1/q2/Zork.java", //-----------------------------------------------------------------------
					"""
						package q1.q2;
						public class Zork {
						}
						""",
			},
			"");

	// delete binary folder q1 (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "q1"));

	runNegativeTest(
		// test directory preparation
		false /* do not flush output directory */,
		new String[] { /* test files */
			"X.java", //-----------------------------------------------------------------------
			"""
				public class X {
					void foo() {
						q1.q2.Zork z;
					}
				}\t
				""",
		},
		// compiler options
		null /* no class libraries */,
		null /* no custom options */,
		"""
			----------
			1. ERROR in X.java (at line 3)
				q1.q2.Zork z;
				^^
			q1 cannot be resolved to a type
			----------
			""",
		// javac options
		JavacTestOptions.SKIP_UNTIL_FRAMEWORK_FIX /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test009() {
	this.runConformTest(
			new String[] {
					"p/OtherFoo.java", //-----------------------------------------------------------------------
					"""
						package p;
						
						import q1.q2.Zork;
						
						public class OtherFoo extends Zork{
							public class OtherMember extends Zork {}
							public Zork foo;
							public Zork bar() {	return null; }
							public void baz(Zork z) {}
						}
						""",
					"q1/q2/Zork.java", //-----------------------------------------------------------------------
					"""
						package q1.q2;
						public class Zork {
						}
						""",
			},
			"");

	// delete binary folder q1 (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "q1"));

	runNegativeTest(
		// test directory preparation
		false /* do not flush output directory */,
		new String[] { /* test files */
			"X.java", //-----------------------------------------------------------------------
			"""
				import q1.q2.*;
				public class X {
					void foo() {
						Zork z;
					}
				}\t
				""",
		},
		// compiler options
		null /* no class libraries */,
		null /* no custom options */,
		"""
			----------
			1. ERROR in X.java (at line 1)
				import q1.q2.*;
				       ^^
			The import q1 cannot be resolved
			----------
			2. ERROR in X.java (at line 4)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			""",			// javac options
		JavacTestOptions.SKIP_UNTIL_FRAMEWORK_FIX /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test010() {
	this.runConformTest(
			new String[] {
					"p/OtherFoo.java", //-----------------------------------------------------------------------
					"""
						package p;
						
						import q1.q2.Zork;
						
						public class OtherFoo extends Zork{
							public class OtherMember extends Zork {}
							public Zork foo;
							public Zork bar() {	return null; }
							public void baz(Zork z) {}
						}
						""",
					"q1/q2/Zork.java", //-----------------------------------------------------------------------
					"""
						package q1.q2;
						public class Zork {
						}
						""",
			},
			"");

	// delete binary folder q1 (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "q1"));

	runNegativeTest(
		// test directory preparation
		false /* do not flush output directory */,
		new String[] { /* test files */
			"X.java", //-----------------------------------------------------------------------
			"""
				import q1.q2.Zork;
				public class X {
					void foo() {
						Zork z;
					}
				}\t
				""",
		},
		// compiler options
		null /* no class libraries */,
		null /* no custom options */,
		"""
			----------
			1. ERROR in X.java (at line 1)
				import q1.q2.Zork;
				       ^^
			The import q1 cannot be resolved
			----------
			2. ERROR in X.java (at line 4)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			""",
		// javac options
		JavacTestOptions.SKIP_UNTIL_FRAMEWORK_FIX /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test011() {
	this.runNegativeTest(new String[] {
			"X.java",
			"""
				public class  X {
					java[] field1;
					java.lang[] field2;
					void field3;
					void[] field4;
				\t
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				java[] field1;
				^^^^
			java cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 3)
				java.lang[] field2;
				^^^^^^^^^
			java.lang cannot be resolved to a type
			----------
			3. ERROR in X.java (at line 4)
				void field3;
				     ^^^^^^
			void is an invalid type for the variable field3
			----------
			4. ERROR in X.java (at line 5)
				void[] field4;
				^^^^^^
			void[] is an invalid type
			----------
			""");
}
public void test012() {
	String expectedResult;
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		expectedResult =
			"""
				----------
				1. ERROR in X.java (at line 2)
					Class c1 = java[].class;
					           ^^^^
				java cannot be resolved to a type
				----------
				2. ERROR in X.java (at line 3)
					Class c2 = java.lang[].class;
					           ^^^^^^^^^
				java.lang cannot be resolved to a type
				----------
				3. ERROR in X.java (at line 5)
					Class c4 = void[].class;
					           ^^^^^^
				void[] is an invalid type
				----------
				""";
	} else {
		expectedResult =
			"""
				----------
				1. WARNING in X.java (at line 2)
					Class c1 = java[].class;
					^^^^^
				Class is a raw type. References to generic type Class<T> should be parameterized
				----------
				2. ERROR in X.java (at line 2)
					Class c1 = java[].class;
					           ^^^^
				java cannot be resolved to a type
				----------
				3. WARNING in X.java (at line 3)
					Class c2 = java.lang[].class;
					^^^^^
				Class is a raw type. References to generic type Class<T> should be parameterized
				----------
				4. ERROR in X.java (at line 3)
					Class c2 = java.lang[].class;
					           ^^^^^^^^^
				java.lang cannot be resolved to a type
				----------
				5. WARNING in X.java (at line 4)
					Class c3 = void.class;
					^^^^^
				Class is a raw type. References to generic type Class<T> should be parameterized
				----------
				6. WARNING in X.java (at line 5)
					Class c4 = void[].class;
					^^^^^
				Class is a raw type. References to generic type Class<T> should be parameterized
				----------
				7. ERROR in X.java (at line 5)
					Class c4 = void[].class;
					           ^^^^^^
				void[] is an invalid type
				----------
				""";
	}
	this.runNegativeTest(new String[] {
			"X.java",
			"""
				public class  X {
					Class c1 = java[].class;
					Class c2 = java.lang[].class;
					Class c3 = void.class;
					Class c4 = void[].class;
				}
				""",
		},
		expectedResult);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test013() {
	String expectedResult;
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		expectedResult =
			"""
				----------
				1. ERROR in X.java (at line 3)
					Class c2 = java.lang[].class;
					           ^^^^^^^^^
				java.lang cannot be resolved to a type
				----------
				""";
	} else {
		expectedResult =
			"""
				----------
				1. WARNING in X.java (at line 3)
					Class c2 = java.lang[].class;
					^^^^^
				Class is a raw type. References to generic type Class<T> should be parameterized
				----------
				2. ERROR in X.java (at line 3)
					Class c2 = java.lang[].class;
					           ^^^^^^^^^
				java.lang cannot be resolved to a type
				----------
				""";
	}
	this.runNegativeTest(new String[] {
			"X.java",
			"""
				public class  X {
					// check if no prior reference to missing 'java'
					Class c2 = java.lang[].class;
				}
				""",
		},
		expectedResult);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test014() {
	String expectedResult;
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		expectedResult =
			"""
				----------
				1. ERROR in X.java (at line 3)
					Class c2 = java.lang[].class;
					           ^^^^^^^^^
				java.lang cannot be resolved to a type
				----------
				""";
	} else {
		expectedResult =
			"""
				----------
				1. WARNING in X.java (at line 3)
					Class c2 = java.lang[].class;
					^^^^^
				Class is a raw type. References to generic type Class<T> should be parameterized
				----------
				2. ERROR in X.java (at line 3)
					Class c2 = java.lang[].class;
					           ^^^^^^^^^
				java.lang cannot be resolved to a type
				----------
				""";
	}
	this.runNegativeTest(new String[] {
			"X.java",
			"""
				public class  X {
					// check if no prior reference to missing 'java'
					Class c2 = java.lang[].class;
				}
				""",
		},
		expectedResult);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test015() {
	String expectedResult;
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		expectedResult =
			"""
				----------
				1. ERROR in X.java (at line 2)
					Class a = zork1[].class;
					          ^^^^^
				zork1 cannot be resolved to a type
				----------
				2. ERROR in X.java (at line 3)
					Class x = zork1.zork2[].class;	// compile time error
					          ^^^^^
				zork1 cannot be resolved to a type
				----------
				3. ERROR in X.java (at line 5)
					Class a2 = zork1.class;
					           ^^^^^
				zork1 cannot be resolved to a type
				----------
				4. ERROR in X.java (at line 6)
					Class x2 = zork1.zork2.class;	// compile time error\t
					           ^^^^^
				zork1 cannot be resolved to a type
				----------
				""";
	} else {
		expectedResult =
			"""
				----------
				1. WARNING in X.java (at line 2)
					Class a = zork1[].class;
					^^^^^
				Class is a raw type. References to generic type Class<T> should be parameterized
				----------
				2. ERROR in X.java (at line 2)
					Class a = zork1[].class;
					          ^^^^^
				zork1 cannot be resolved to a type
				----------
				3. WARNING in X.java (at line 3)
					Class x = zork1.zork2[].class;	// compile time error
					^^^^^
				Class is a raw type. References to generic type Class<T> should be parameterized
				----------
				4. ERROR in X.java (at line 3)
					Class x = zork1.zork2[].class;	// compile time error
					          ^^^^^
				zork1 cannot be resolved to a type
				----------
				5. WARNING in X.java (at line 5)
					Class a2 = zork1.class;
					^^^^^
				Class is a raw type. References to generic type Class<T> should be parameterized
				----------
				6. ERROR in X.java (at line 5)
					Class a2 = zork1.class;
					           ^^^^^
				zork1 cannot be resolved to a type
				----------
				7. WARNING in X.java (at line 6)
					Class x2 = zork1.zork2.class;	// compile time error\t
					^^^^^
				Class is a raw type. References to generic type Class<T> should be parameterized
				----------
				8. ERROR in X.java (at line 6)
					Class x2 = zork1.zork2.class;	// compile time error\t
					           ^^^^^
				zork1 cannot be resolved to a type
				----------
				""";
	}
	this.runNegativeTest(new String[] {
			"X.java",
			"""
				public class  X {
					Class a = zork1[].class;
					Class x = zork1.zork2[].class;	// compile time error
				\t
					Class a2 = zork1.class;
					Class x2 = zork1.zork2.class;	// compile time error\t
				}
				""",
		},
		expectedResult);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test016() {
	this.runNegativeTest(new String[] {
			"X.java",
			"""
				public class X {
					java.langz.AClass1 field1;
					java.langz.AClass2 field2;
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				java.langz.AClass1 field1;
				^^^^^^^^^^
			java.langz cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 3)
				java.langz.AClass2 field2;
				^^^^^^^^^^
			java.langz cannot be resolved to a type
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test017() {
	this.runNegativeTest(new String[] {
			"X.java",
			"""
				public class X {
					java.langz field1;
					java.langz.AClass2 field2;
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				java.langz field1;
				^^^^^^^^^^
			java.langz cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 3)
				java.langz.AClass2 field2;
				^^^^^^^^^^
			java.langz cannot be resolved to a type
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test018() {
	this.runNegativeTest(new String[] {
			"X.java",
			"""
				public class X {
					java.langz.AClass1 field1;
					java.langz field2;
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				java.langz.AClass1 field1;
				^^^^^^^^^^
			java.langz cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 3)
				java.langz field2;
				^^^^^^^^^^
			java.langz cannot be resolved to a type
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test019() {
	this.runConformTest(
			new String[] {
					"p/OtherFoo.java", //-----------------------------------------------------------------------
					"""
						package p;
						
						import q1.q2.Zork;
						
						public class OtherFoo extends Zork{
							public class OtherMember extends Zork {}
							public Zork foo;
							public Zork bar() {	return null; }
							public void baz(Zork z) {}
						}
						""",
					"q1/q2/Zork.java", //-----------------------------------------------------------------------
					"""
						package q1.q2;
						public class Zork {
						}
						""",
			},
			"");

	// delete binary folder q1 (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "q1"));

	this.runNegativeTest(
			new String[] {
					"X.java", //-----------------------------------------------------------------------
					"""
						public class X {
							void foo(p.OtherFoo ofoo) {
								a.b.Missing1 m1;
								q1.q2.Missing2 m2;
							}
						}\t
						""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					a.b.Missing1 m1;
					^
				a cannot be resolved to a type
				----------
				2. ERROR in X.java (at line 4)
					q1.q2.Missing2 m2;
					^^^^^^^^^^^^^^
				q1.q2.Missing2 cannot be resolved to a type
				----------
				""",
			null,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test020() {
	this.runConformTest(
			new String[] {
					"p/OtherFoo.java", //-----------------------------------------------------------------------
					"""
						package p;
						
						import q1.q2.Zork;
						
						public class OtherFoo extends Zork{
							public class OtherMember extends Zork {}
							public Zork foo;
							public Zork bar() {	return null; }
							public void baz(Zork z) {}
						}
						""",
					"q1/q2/Zork.java", //-----------------------------------------------------------------------
					"""
						package q1.q2;
						public class Zork {
						}
						""",
			},
			"");

	// no need to delete Zork actually - any lazy reference would cause q1.q2 to be created as a package

	this.runNegativeTest(
			new String[] {
					"X.java", //-----------------------------------------------------------------------
					"""
						public class X {
							void foo(p.OtherFoo ofoo) {
								a.b.Missing1 m1;
								q1.q2.Missing2 m2;
							}
						}\t
						""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					a.b.Missing1 m1;
					^
				a cannot be resolved to a type
				----------
				2. ERROR in X.java (at line 4)
					q1.q2.Missing2 m2;
					^^^^^^^^^^^^^^
				q1.q2.Missing2 cannot be resolved to a type
				----------
				""",
			null,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test021() {
	this.runNegativeTest(
			new String[] {
					"X.java", //-----------------------------------------------------------------------
					"""
						public class X {
							void foo(p.OtherFoo ofoo) {
								a.b.Missing1 m1;
								q1.q2.Missing2 m2;
							}
						}\t
						""",
					"p/OtherFoo.java", //-----------------------------------------------------------------------
					"""
						package p;
						public class OtherFoo extends q1.q2.Zork{
						}
						""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					a.b.Missing1 m1;
					^
				a cannot be resolved to a type
				----------
				2. ERROR in X.java (at line 4)
					q1.q2.Missing2 m2;
					^^
				q1 cannot be resolved to a type
				----------
				----------
				1. ERROR in p\\OtherFoo.java (at line 2)
					public class OtherFoo extends q1.q2.Zork{
					                              ^^
				q1 cannot be resolved to a type
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test022() {
	this.runConformTest(
			new String[] {
					"p/OtherFoo.java", //-----------------------------------------------------------------------
					"""
						package p;
						
						import q1.q2.Zork;
						
						public class OtherFoo {
							public Zork foo;
						}
						""",
					"q1/q2/Zork.java", //-----------------------------------------------------------------------
					"""
						package q1.q2;
						public class Zork {
						}
						""",
			},
			"");

	// delete binary folder q1 (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "q1"));

	this.runNegativeTest(
			new String[] {
					"X.java", //-----------------------------------------------------------------------
					"""
						public class X {
							void foo(q1.q2.Missing1 m1) {
								a.b.Missing1 m1a;
								p.OtherFoo ofoo;
								q1.q2.Missing1 m11;
							}
						}\t
						""",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					void foo(q1.q2.Missing1 m1) {
					         ^^
				q1 cannot be resolved to a type
				----------
				2. ERROR in X.java (at line 3)
					a.b.Missing1 m1a;
					^
				a cannot be resolved to a type
				----------
				3. ERROR in X.java (at line 5)
					q1.q2.Missing1 m11;
					^^
				q1 cannot be resolved to a type
				----------
				""",
			null,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test023() {
	this.runConformTest(
			new String[] {
					"p/OtherFoo.java", //-----------------------------------------------------------------------
					"""
						package p;
						
						import q1.q2.Zork;
						
						public class OtherFoo {
							public Zork foo;
						}
						""",
					"q1/q2/Zork.java", //-----------------------------------------------------------------------
					"""
						package q1.q2;
						public class Zork {
						}
						""",
			},
			"");

	// leave package behind

	this.runNegativeTest(
			new String[] {
					"X.java", //-----------------------------------------------------------------------
					"""
						public class X {
							void foo(q1.q2.Missing1 m1) {
								a.b.Missing1 m1a;
								p.OtherFoo ofoo;
								q1.q2.Missing1 m11;
							}
						}\t
						""",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					void foo(q1.q2.Missing1 m1) {
					         ^^^^^^^^^^^^^^
				q1.q2.Missing1 cannot be resolved to a type
				----------
				2. ERROR in X.java (at line 3)
					a.b.Missing1 m1a;
					^
				a cannot be resolved to a type
				----------
				3. ERROR in X.java (at line 5)
					q1.q2.Missing1 m11;
					^^^^^^^^^^^^^^
				q1.q2.Missing1 cannot be resolved to a type
				----------
				""",
			null,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test024() {
	this.runConformTest(
			new String[] {
					"p/OtherFoo.java", //-----------------------------------------------------------------------
					"""
						package p;
						
						import q1.q2.Zork;
						
						public class OtherFoo {
							public Zork foo;
						}
						""",
					"q1/q2/Zork.java", //-----------------------------------------------------------------------
					"""
						package q1.q2;
						public class Zork {
						}
						""",
			},
			"");

	// delete binary folder q1/q2 (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "q1/q2"));

	this.runNegativeTest(
			new String[] {
					"X.java", //-----------------------------------------------------------------------
					"""
						public class X {
							void foo(q1.q2.Missing1 m1) {
								a.b.Missing1 m1a;
								p.OtherFoo ofoo;
								q1.q2.Missing1 m11;
							}
						}\t
						""",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					void foo(q1.q2.Missing1 m1) {
					         ^^^^^
				q1.q2 cannot be resolved to a type
				----------
				2. ERROR in X.java (at line 3)
					a.b.Missing1 m1a;
					^
				a cannot be resolved to a type
				----------
				3. ERROR in X.java (at line 5)
					q1.q2.Missing1 m11;
					^^^^^^^^^^^^^^
				q1.q2.Missing1 cannot be resolved to a type
				----------
				""",
			null,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test025() {
	this.runConformTest(
			new String[] {
					"p/OtherFoo.java", //-----------------------------------------------------------------------
					"""
						package p;
						
						import q1.q2.Zork;
						
						public class OtherFoo {
							public Zork foo;
						}
						""",
					"q1/q2/Zork.java", //-----------------------------------------------------------------------
					"""
						package q1.q2;
						public class Zork {
						}
						""",
			},
			"");

	// delete binary folder q1/q2 (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "q1/q2"));

	this.runNegativeTest(
			new String[] {
					"X.java", //-----------------------------------------------------------------------
					"""
						public class X {
							void foo(q1.q2.Missing1 m1) {
								a.b.Missing1 m1a;
								p.OtherFoo ofoo;
							}
						}\t
						""",
					"Y.java", //-----------------------------------------------------------------------
					"""
						public class Y {
							void foo() {
								q1.q2.Missing1 m11;
							}
						}\t
						""",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					void foo(q1.q2.Missing1 m1) {
					         ^^^^^
				q1.q2 cannot be resolved to a type
				----------
				2. ERROR in X.java (at line 3)
					a.b.Missing1 m1a;
					^
				a cannot be resolved to a type
				----------
				----------
				1. ERROR in Y.java (at line 3)
					q1.q2.Missing1 m11;
					^^^^^^^^^^^^^^
				q1.q2.Missing1 cannot be resolved to a type
				----------
				""",
			null,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test026() {
	this.runNegativeTest(
			new String[] {
					"X.java", //-----------------------------------------------------------------------
					"""
						public class X {
							void foo(Missing1 m1) {
								Missing2 m2 = m1;
							}
						}
						""",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					void foo(Missing1 m1) {
					         ^^^^^^^^
				Missing1 cannot be resolved to a type
				----------
				2. ERROR in X.java (at line 3)
					Missing2 m2 = m1;
					^^^^^^^^
				Missing2 cannot be resolved to a type
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test027() {
	this.runNegativeTest(
			new String[] {
					"X.java", //-----------------------------------------------------------------------
					"""
						public class X {
							void foo(X x) {
								new Other().m2 = x;
								Other other = new Other();
								other.m2 = x;
								other.m2.m3 = x;
							}
						}
						
						class Other {
							Missing2 m2;
						}
						""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					new Other().m2 = x;
					^^^^^^^^^^^^^^
				Missing2 cannot be resolved to a type
				----------
				2. ERROR in X.java (at line 5)
					other.m2 = x;
					^^^^^^^^
				Missing2 cannot be resolved to a type
				----------
				3. ERROR in X.java (at line 6)
					other.m2.m3 = x;
					^^^^^^^^
				Missing2 cannot be resolved to a type
				----------
				4. ERROR in X.java (at line 11)
					Missing2 m2;
					^^^^^^^^
				Missing2 cannot be resolved to a type
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test028() {
	this.runNegativeTest(
			new String[] {
					"X.java", //-----------------------------------------------------------------------
					"""
						public class X {
							void foo(X x) {
								System.out.println(new Other().m2.m3);
								System.out.println(new Other().m2.m3());
								Missing2.foo();
							}
						}
						
						class Other {
							Missing2 m2;
						}
						""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					System.out.println(new Other().m2.m3);
					                   ^^^^^^^^^^^^^^
				Missing2 cannot be resolved to a type
				----------
				2. ERROR in X.java (at line 4)
					System.out.println(new Other().m2.m3());
					                   ^^^^^^^^^^^^^^
				Missing2 cannot be resolved to a type
				----------
				3. ERROR in X.java (at line 5)
					Missing2.foo();
					^^^^^^^^
				Missing2 cannot be resolved
				----------
				4. ERROR in X.java (at line 10)
					Missing2 m2;
					^^^^^^^^
				Missing2 cannot be resolved to a type
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test029() throws Exception {
	this.runNegativeTest(
			new String[] {
				"Y.java", //-----------------------------------------------------------------------
				"public class Y extends Z {\n" +
				"}\n",
			},
			"""
				----------
				1. ERROR in Y.java (at line 1)
					public class Y extends Z {
					                       ^
				Z cannot be resolved to a type
				----------
				""",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);

	// check Y superclass in problem classfile: shoud not be Z otherwise the class cannot load
	String expectedOutput =
		"public class Y {\n";

	File f = new File(OUTPUT_DIR + File.separator + "Y.class");
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
	int index = result.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(result, 3));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, result);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test030() {
	this.runNegativeTest(
			new String[] {
				"Y.java", //-----------------------------------------------------------------------
				"public class Y extends Z {\n" +
				"}\n",
			},
			"""
				----------
				1. ERROR in Y.java (at line 1)
					public class Y extends Z {
					                       ^
				Z cannot be resolved to a type
				----------
				""",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);

	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X extends Y {\n" +
				"}\n",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					public class X extends Y {
					             ^
				The hierarchy of the type X is inconsistent
				----------
				""",
			null,
			false, // do not flush output
			null,
			true, // generate output
			false,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test031() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"public class X extends Y {\n" +
				"}\n",
				"Y.java", //-----------------------------------------------------------------------
				"public class Y extends Z {\n" +
				"}\n",
			},
			"""
				----------
				1. ERROR in X.java (at line 1)
					public class X extends Y {
					             ^
				The hierarchy of the type X is inconsistent
				----------
				----------
				1. ERROR in Y.java (at line 1)
					public class Y extends Z {
					                       ^
				Z cannot be resolved to a type
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test032() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					import existing.*;
					public class X {
					  void foo(p.Zork z) {}
					  void bar(Zork z) {} // should bind to existing.Zork
					}
					""",
				"p/Clyde.java", //-----------------------------------------------------------------------
				"""
					package p;
					public class Clyde {
					}
					""",
				"existing/Zork.java", //-----------------------------------------------------------------------
				"""
					package existing;
					public class Zork {
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					void foo(p.Zork z) {}
					         ^^^^^^
				p.Zork cannot be resolved to a type
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test033() {
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) {
		return;
	}
	this.runNegativeTest(
			new String[] {
				"Y.java", //-----------------------------------------------------------------------
				"@Z public class Y {\n" +
				"}\n",
			},
			"""
				----------
				1. ERROR in Y.java (at line 1)
					@Z public class Y {
					 ^
				Z cannot be resolved to a type
				----------
				""",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);

	runConformTest(
		// test directory preparation
		false /* do not flush output directory */,
		new String[] { /* test files */
			"X.java", //-----------------------------------------------------------------------
			"public class X extends Y {\n" +
			"}\n",
		},
		// compiler results
		"" /* expected compiler log */,
		// runtime results
		"" /* expected output string */,
		"" /* expected error string */,
		// javac options
		JavacTestOptions.SKIP_UNTIL_FRAMEWORK_FIX /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test034() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					public class X {
						void foo(X1 x1) {
							Object o = x1.bar();
						}
					}
					""",
				"X1.java", //-----------------------------------------------------------------------
				"""
					public class X1 {
						Zork bar() { return null; }\t
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					Object o = x1.bar();
					              ^^^
				The method bar() from the type X1 refers to the missing type Zork
				----------
				----------
				1. ERROR in X1.java (at line 2)
					Zork bar() { return null; }\t
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test035() {
	this.runNegativeTest(
			new String[] {
				"X1.java", //-----------------------------------------------------------------------
				"""
					public class X1 {
						Zork bar() { return null; }\t
					}
					"""
			},
			"""
				----------
				1. ERROR in X1.java (at line 2)
					Zork bar() { return null; }\t
					^^^^
				Zork cannot be resolved to a type
				----------
				""",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);

	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					public class X {
						void foo(X1 x1) {
							Object o = x1.bar();
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					Object o = x1.bar();
					              ^^^
				The method bar() from the type X1 refers to the missing type Zork
				----------
				""",
			null,
			false, // do not flush output
			null,
			true, // generate output
			false,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test036() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					public class X {
						void foo(X1 x1) {
							Object o = x1.bar(x1);
						}
					}
					""",
				"X1.java", //-----------------------------------------------------------------------
				"""
					public class X1 {
						Object bar(Zork z) { return null; }\t
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					Object o = x1.bar(x1);
					              ^^^
				The method bar(Zork) from the type X1 refers to the missing type Zork
				----------
				----------
				1. ERROR in X1.java (at line 2)
					Object bar(Zork z) { return null; }\t
					           ^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test037() {
	this.runNegativeTest(
			new String[] {
				"X1.java", //-----------------------------------------------------------------------
				"""
					public class X1 {
						Object bar(Zork z) { return null; }\t
					}
					"""
			},
			"""
				----------
				1. ERROR in X1.java (at line 2)
					Object bar(Zork z) { return null; }\t
					           ^^^^
				Zork cannot be resolved to a type
				----------
				""",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);

	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					public class X {
						void foo(X1 x1) {
							Object o = x1.bar(x1);
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					Object o = x1.bar(x1);
					              ^^^
				The method bar(Zork) from the type X1 refers to the missing type Zork
				----------
				""",
			null,
			false, // do not flush output
			null,
			true, // generate output
			false,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test038() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					public class X {
						void foo(X1 x1) {
							Object o = x1.bar(x1);
						}
					}
					""",
				"X1.java", //-----------------------------------------------------------------------
				"""
					public class X1 {
						Object bar(Object o) throws Zork { return null; }\t
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					Object o = x1.bar(x1);
					              ^^^
				The method bar(Object) from the type X1 refers to the missing type Zork
				----------
				----------
				1. ERROR in X1.java (at line 2)
					Object bar(Object o) throws Zork { return null; }\t
					                            ^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test039() {
	this.runNegativeTest(
			new String[] {
				"X1.java", //-----------------------------------------------------------------------
				"""
					public class X1 {
						Object bar(Object o) throws Zork { return null; }\t
					}
					"""
			},
			"""
				----------
				1. ERROR in X1.java (at line 2)
					Object bar(Object o) throws Zork { return null; }\t
					                            ^^^^
				Zork cannot be resolved to a type
				----------
				""",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);

	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					public class X {
						void foo(X1 x1) {
							Object o = x1.bar(x1);
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					Object o = x1.bar(x1);
					              ^^^
				The method bar(Object) from the type X1 refers to the missing type Zork
				----------
				""",
			null,
			false, // do not flush output
			null,
			true, // generate output
			false,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test040() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					public class X {
						void foo(X1 x1) {
							Object o = new X1(x1);
						}
					}
					""",
				"X1.java", //-----------------------------------------------------------------------
				"""
					public class X1 {
						public X1(Zork z) {}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					Object o = new X1(x1);
					           ^^^^^^^^^^
				The constructor X1(Zork) refers to the missing type Zork
				----------
				----------
				1. ERROR in X1.java (at line 2)
					public X1(Zork z) {}
					          ^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test041() {
	this.runNegativeTest(
			new String[] {
				"X1.java", //-----------------------------------------------------------------------
				"""
					public class X1 {
						public X1(Zork z) {}
					}
					"""
			},
			"""
				----------
				1. ERROR in X1.java (at line 2)
					public X1(Zork z) {}
					          ^^^^
				Zork cannot be resolved to a type
				----------
				""",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);

	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					public class X {
						void foo(X1 x1) {
							Object o = new X1(x1);
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					Object o = new X1(x1);
					           ^^^^^^^^^^
				The constructor X1(Zork) refers to the missing type Zork
				----------
				""",
			null,
			false, // do not flush output
			null,
			true, // generate output
			false,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test042() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					public class X {
						void foo(X1 x1) {
							Object o = new X1();
						}
					}
					""",
				"X1.java", //-----------------------------------------------------------------------
				"""
					public class X1 {
						public X1() throws Zork {}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					Object o = new X1();
					           ^^^^^^^^
				The constructor X1() refers to the missing type Zork
				----------
				----------
				1. ERROR in X1.java (at line 2)
					public X1() throws Zork {}
					                   ^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test043() {
	this.runNegativeTest(
			new String[] {
				"X1.java", //-----------------------------------------------------------------------
				"""
					public class X1 {
						public X1() throws Zork {}
					}
					"""
			},
			"""
				----------
				1. ERROR in X1.java (at line 2)
					public X1() throws Zork {}
					                   ^^^^
				Zork cannot be resolved to a type
				----------
				""",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);

	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					public class X {
						void foo(X1 x1) {
							Object o = new X1();
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					Object o = new X1();
					           ^^^^^^^^
				The constructor X1() refers to the missing type Zork
				----------
				""",
			null,
			false, // do not flush output
			null,
			true, // generate output
			false,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test044() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					public class X {
						void foo(X1 x1) {
							Object o = new X1(x1){};
						}
					}
					""",
				"X1.java", //-----------------------------------------------------------------------
				"""
					public class X1 {
						public X1(Zork z) {}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					Object o = new X1(x1){};
					               ^^^^^^
				The constructor X1(Zork) refers to the missing type Zork
				----------
				----------
				1. ERROR in X1.java (at line 2)
					public X1(Zork z) {}
					          ^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test045() {
	this.runNegativeTest(
			new String[] {
				"X1.java", //-----------------------------------------------------------------------
				"""
					public class X1 {
						public X1(Zork z) {}
					}
					"""
			},
			"""
				----------
				1. ERROR in X1.java (at line 2)
					public X1(Zork z) {}
					          ^^^^
				Zork cannot be resolved to a type
				----------
				""",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);

	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					public class X {
						void foo(X1 x1) {
							Object o = new X1(x1){};
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					Object o = new X1(x1){};
					               ^^^^^^
				The constructor X1(Zork) refers to the missing type Zork
				----------
				""",
			null,
			false, // do not flush output
			null,
			true, // generate output
			false,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test046() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					public class X {
						void foo(X1 x1) {
							Object o = new X1(){};
						}
					}
					""",
				"X1.java", //-----------------------------------------------------------------------
				"""
					public class X1 {
						public X1() throws Zork {}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					Object o = new X1(){};
					               ^^^^
				The constructor X1() refers to the missing type Zork
				----------
				----------
				1. ERROR in X1.java (at line 2)
					public X1() throws Zork {}
					                   ^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test047() {
	this.runNegativeTest(
			new String[] {
				"X1.java", //-----------------------------------------------------------------------
				"""
					public class X1 {
						public X1() throws Zork {}
					}
					"""
			},
			"""
				----------
				1. ERROR in X1.java (at line 2)
					public X1() throws Zork {}
					                   ^^^^
				Zork cannot be resolved to a type
				----------
				""",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);

	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					public class X {
						void foo(X1 x1) {
							Object o = new X1(){};
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					Object o = new X1(){};
					               ^^^^
				The constructor X1() refers to the missing type Zork
				----------
				""",
			null,
			false, // do not flush output
			null,
			true, // generate output
			false,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test048() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					public class X extends X1 {
						X(X1 x1) {
							super(x1);
						}
					}
					""",
				"X1.java", //-----------------------------------------------------------------------
				"""
					public class X1 {
						public X1(Zork z) {}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					super(x1);
					^^^^^^^^^^
				The constructor X1(Zork) refers to the missing type Zork
				----------
				----------
				1. ERROR in X1.java (at line 2)
					public X1(Zork z) {}
					          ^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test049() {
	this.runNegativeTest(
			new String[] {
				"X1.java", //-----------------------------------------------------------------------
				"""
					public class X1 {
						public X1(Zork z) {}
					}
					"""
			},
			"""
				----------
				1. ERROR in X1.java (at line 2)
					public X1(Zork z) {}
					          ^^^^
				Zork cannot be resolved to a type
				----------
				""",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);

	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					public class X extends X1 {
						X(X1 x1) {
							super(x1);
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					super(x1);
					^^^^^^^^^^
				The constructor X1(Zork) refers to the missing type Zork
				----------
				""",
			null,
			false, // do not flush output
			null,
			true, // generate output
			false,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test050() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					public class X extends X1 {
						X(X1 x1) {
							super();
						}
					}
					""",
				"X1.java", //-----------------------------------------------------------------------
				"""
					public class X1 {
						public X1() throws Zork {}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					super();
					^^^^^^^^
				The constructor X1() refers to the missing type Zork
				----------
				----------
				1. ERROR in X1.java (at line 2)
					public X1() throws Zork {}
					                   ^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test051() {
	this.runNegativeTest(
			new String[] {
				"X1.java", //-----------------------------------------------------------------------
				"""
					public class X1 {
						public X1() throws Zork {}
					}
					"""
			},
			"""
				----------
				1. ERROR in X1.java (at line 2)
					public X1() throws Zork {}
					                   ^^^^
				Zork cannot be resolved to a type
				----------
				""",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);

	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					public class X extends X1 {
						X(X1 x1) {
							super();
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					super();
					^^^^^^^^
				The constructor X1() refers to the missing type Zork
				----------
				""",
			null,
			false, // do not flush output
			null,
			true, // generate output
			false,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test052() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					public class X {
						void foo(X1 x1) {
							Object o;
							o = x1.next.zork;
							o = this.zork;
							o = zork;
							o = x1.next.zork.foo();
							o = this.zork.foo();
							o = zork.foo();
						}
						Zork zork;
					}
					class X1 {
						X1 next;
						Zork zork;
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					o = x1.next.zork;
					    ^^^^^^^^^^^^
				Zork cannot be resolved to a type
				----------
				2. ERROR in X.java (at line 5)
					o = this.zork;
					    ^^^^^^^^^
				Zork cannot be resolved to a type
				----------
				3. ERROR in X.java (at line 6)
					o = zork;
					    ^^^^
				Zork cannot be resolved to a type
				----------
				4. ERROR in X.java (at line 7)
					o = x1.next.zork.foo();
					    ^^^^^^^^^^^^
				Zork cannot be resolved to a type
				----------
				5. ERROR in X.java (at line 8)
					o = this.zork.foo();
					    ^^^^^^^^^
				Zork cannot be resolved to a type
				----------
				6. ERROR in X.java (at line 9)
					o = zork.foo();
					    ^^^^
				Zork cannot be resolved to a type
				----------
				7. ERROR in X.java (at line 11)
					Zork zork;
					^^^^
				Zork cannot be resolved to a type
				----------
				8. ERROR in X.java (at line 15)
					Zork zork;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test053() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					public class X {
						void bar(X1 x1) {
							String s;
							s = x1.next.zork;
							s = this.zork;
							s = zork;
							s = x1.next.zork.foo();
							s = this.zork.foo();
							s = zork.foo();
						}\t
						Zork zork;
					}
					class X1 {
						X1 next;
						Zork zork;
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					s = x1.next.zork;
					    ^^^^^^^^^^^^
				Zork cannot be resolved to a type
				----------
				2. ERROR in X.java (at line 5)
					s = this.zork;
					    ^^^^^^^^^
				Zork cannot be resolved to a type
				----------
				3. ERROR in X.java (at line 6)
					s = zork;
					    ^^^^
				Zork cannot be resolved to a type
				----------
				4. ERROR in X.java (at line 7)
					s = x1.next.zork.foo();
					    ^^^^^^^^^^^^
				Zork cannot be resolved to a type
				----------
				5. ERROR in X.java (at line 8)
					s = this.zork.foo();
					    ^^^^^^^^^
				Zork cannot be resolved to a type
				----------
				6. ERROR in X.java (at line 9)
					s = zork.foo();
					    ^^^^
				Zork cannot be resolved to a type
				----------
				7. ERROR in X.java (at line 11)
					Zork zork;
					^^^^
				Zork cannot be resolved to a type
				----------
				8. ERROR in X.java (at line 15)
					Zork zork;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test054() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					public class X {
						void baz(X1 x1) {
							Zork z;
							z = x1.next.zork;
							z = this.zork;
							z = zork;
							z = x1.next.zork.foo();
							z = this.zork.foo();
							z = zork.foo();
						}\t
						Zork zork;
					}
					class X1 {
						X1 next;
						Zork zork;
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				2. ERROR in X.java (at line 4)
					z = x1.next.zork;
					    ^^^^^^^^^^^^
				Zork cannot be resolved to a type
				----------
				3. ERROR in X.java (at line 5)
					z = this.zork;
					    ^^^^^^^^^
				Zork cannot be resolved to a type
				----------
				4. ERROR in X.java (at line 6)
					z = zork;
					    ^^^^
				Zork cannot be resolved to a type
				----------
				5. ERROR in X.java (at line 7)
					z = x1.next.zork.foo();
					    ^^^^^^^^^^^^
				Zork cannot be resolved to a type
				----------
				6. ERROR in X.java (at line 8)
					z = this.zork.foo();
					    ^^^^^^^^^
				Zork cannot be resolved to a type
				----------
				7. ERROR in X.java (at line 9)
					z = zork.foo();
					    ^^^^
				Zork cannot be resolved to a type
				----------
				8. ERROR in X.java (at line 11)
					Zork zork;
					^^^^
				Zork cannot be resolved to a type
				----------
				9. ERROR in X.java (at line 15)
					Zork zork;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test055() {
	this.runNegativeTest(
			new String[] {
				"X1.java", //-----------------------------------------------------------------------
				"""
					public class X1 {
						public X1 next;
						public Zork zork;
					}
					"""
			},
			"""
				----------
				1. ERROR in X1.java (at line 3)
					public Zork zork;
					       ^^^^
				Zork cannot be resolved to a type
				----------
				""",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);

	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					public class X {
						void foo(X1 x1) {
							Object o;
							o = x1.next.zork;
							o = this.zork;
							o = zork;
							o = x1.next.zork.foo();
							o = this.zork.foo();
							o = zork.foo();
						}
						Zork zork;
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					o = x1.next.zork;
					    ^^^^^^^^^^^^
				Zork cannot be resolved to a type
				----------
				2. ERROR in X.java (at line 5)
					o = this.zork;
					    ^^^^^^^^^
				Zork cannot be resolved to a type
				----------
				3. ERROR in X.java (at line 6)
					o = zork;
					    ^^^^
				Zork cannot be resolved to a type
				----------
				4. ERROR in X.java (at line 7)
					o = x1.next.zork.foo();
					    ^^^^^^^^^^^^
				Zork cannot be resolved to a type
				----------
				5. ERROR in X.java (at line 8)
					o = this.zork.foo();
					    ^^^^^^^^^
				Zork cannot be resolved to a type
				----------
				6. ERROR in X.java (at line 9)
					o = zork.foo();
					    ^^^^
				Zork cannot be resolved to a type
				----------
				7. ERROR in X.java (at line 11)
					Zork zork;
					^^^^
				Zork cannot be resolved to a type
				----------
				""",
			null,
			false, // do not flush output
			null,
			true, // generate output
			false,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test056() {
	this.runNegativeTest(
			new String[] {
				"X1.java", //-----------------------------------------------------------------------
				"""
					public class X1 {
						public X1 next;
						public Zork zork;
					}
					"""
			},
			"""
				----------
				1. ERROR in X1.java (at line 3)
					public Zork zork;
					       ^^^^
				Zork cannot be resolved to a type
				----------
				""",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);

	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					public class X {
						void bar(X1 x1) {
							String s;
							s = x1.next.zork;
							s = this.zork;
							s = zork;
							s = x1.next.zork.foo();
							s = this.zork.foo();
							s = zork.foo();
						}\t
						Zork zork;
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					s = x1.next.zork;
					    ^^^^^^^^^^^^
				Zork cannot be resolved to a type
				----------
				2. ERROR in X.java (at line 5)
					s = this.zork;
					    ^^^^^^^^^
				Zork cannot be resolved to a type
				----------
				3. ERROR in X.java (at line 6)
					s = zork;
					    ^^^^
				Zork cannot be resolved to a type
				----------
				4. ERROR in X.java (at line 7)
					s = x1.next.zork.foo();
					    ^^^^^^^^^^^^
				Zork cannot be resolved to a type
				----------
				5. ERROR in X.java (at line 8)
					s = this.zork.foo();
					    ^^^^^^^^^
				Zork cannot be resolved to a type
				----------
				6. ERROR in X.java (at line 9)
					s = zork.foo();
					    ^^^^
				Zork cannot be resolved to a type
				----------
				7. ERROR in X.java (at line 11)
					Zork zork;
					^^^^
				Zork cannot be resolved to a type
				----------
				""",
			null,
			false, // do not flush output
			null,
			true, // generate output
			false,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test057() {
	this.runNegativeTest(
			new String[] {
				"X1.java", //-----------------------------------------------------------------------
				"""
					public class X1 {
						public X1 next;
						public Zork zork;
					}
					"""
			},
			"""
				----------
				1. ERROR in X1.java (at line 3)
					public Zork zork;
					       ^^^^
				Zork cannot be resolved to a type
				----------
				""",
			null,
			true, // flush output
			null,
			true, // generate output
			false,
			false);

	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					public class X {
						void baz(X1 x1) {
							Zork z;
							z = x1.next.zork;
							z = this.zork;
							z = zork;
							z = x1.next.zork.foo();
							z = this.zork.foo();
							z = zork.foo();
						}\t
						Zork zork;
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				2. ERROR in X.java (at line 4)
					z = x1.next.zork;
					    ^^^^^^^^^^^^
				Zork cannot be resolved to a type
				----------
				3. ERROR in X.java (at line 5)
					z = this.zork;
					    ^^^^^^^^^
				Zork cannot be resolved to a type
				----------
				4. ERROR in X.java (at line 6)
					z = zork;
					    ^^^^
				Zork cannot be resolved to a type
				----------
				5. ERROR in X.java (at line 7)
					z = x1.next.zork.foo();
					    ^^^^^^^^^^^^
				Zork cannot be resolved to a type
				----------
				6. ERROR in X.java (at line 8)
					z = this.zork.foo();
					    ^^^^^^^^^
				Zork cannot be resolved to a type
				----------
				7. ERROR in X.java (at line 9)
					z = zork.foo();
					    ^^^^
				Zork cannot be resolved to a type
				----------
				8. ERROR in X.java (at line 11)
					Zork zork;
					^^^^
				Zork cannot be resolved to a type
				----------
				""",
			null,
			false, // do not flush output
			null,
			true, // generate output
			false,
			false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test058() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					public class X {
						void foo(X1 x1) {
							x1.bar().baz();
						}
					}
					
					class X1 {
						Zork bar(){}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					x1.bar().baz();
					   ^^^
				The method bar() from the type X1 refers to the missing type Zork
				----------
				2. ERROR in X.java (at line 8)
					Zork bar(){}
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test059() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					public class X {
						void foo(X1 x1) {
							new X1(x1).baz();
							new X1(null).baz();
							new Zork().baz();
							new X1(x1){}.baz();
							new X1(null){}.baz();
							new Zork(){}.baz();
						}
					}
					
					class X1 {
						X1(Zork z) {}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					new X1(x1).baz();
					^^^^^^^^^^
				The constructor X1(Zork) refers to the missing type Zork
				----------
				2. ERROR in X.java (at line 3)
					new X1(x1).baz();
					           ^^^
				The method baz() is undefined for the type X1
				----------
				3. ERROR in X.java (at line 4)
					new X1(null).baz();
					^^^^^^^^^^^^
				The constructor X1(Zork) refers to the missing type Zork
				----------
				4. ERROR in X.java (at line 4)
					new X1(null).baz();
					             ^^^
				The method baz() is undefined for the type X1
				----------
				5. ERROR in X.java (at line 5)
					new Zork().baz();
					    ^^^^
				Zork cannot be resolved to a type
				----------
				6. ERROR in X.java (at line 6)
					new X1(x1){}.baz();
					    ^^^^^^
				The constructor X1(Zork) refers to the missing type Zork
				----------
				7. ERROR in X.java (at line 6)
					new X1(x1){}.baz();
					             ^^^
				The method baz() is undefined for the type new X1(){}
				----------
				8. ERROR in X.java (at line 7)
					new X1(null){}.baz();
					    ^^^^^^^^
				The constructor X1(Zork) refers to the missing type Zork
				----------
				9. ERROR in X.java (at line 7)
					new X1(null){}.baz();
					               ^^^
				The method baz() is undefined for the type new X1(){}
				----------
				10. ERROR in X.java (at line 8)
					new Zork(){}.baz();
					    ^^^^
				Zork cannot be resolved to a type
				----------
				11. ERROR in X.java (at line 13)
					X1(Zork z) {}
					   ^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test060() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					public class X {
						void foo(Zork z) {
							z.bar();
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					void foo(Zork z) {
					         ^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test061() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					public class X {
						void foo(X1 x1) {
							int i = x1.next.z;
							int j = x1.next.zArray;
						}
					}
					
					class X1 {
						X1 next;
						Zork z;
						Zork[] zArray;
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					int i = x1.next.z;
					        ^^^^^^^^^
				Zork cannot be resolved to a type
				----------
				2. ERROR in X.java (at line 4)
					int j = x1.next.zArray;
					        ^^^^^^^^^^^^^^
				Zork cannot be resolved to a type
				----------
				3. ERROR in X.java (at line 10)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				4. ERROR in X.java (at line 11)
					Zork[] zArray;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test062() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					public class X {
						X1 x1;
						void foo() {
							int i = x1.next.z;
							int j = x1.next.zArray;
						}
					}
					
					class X1 {
						X1 next;
						Zork z;
						Zork[] zArray;
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					int i = x1.next.z;
					        ^^^^^^^^^
				Zork cannot be resolved to a type
				----------
				2. ERROR in X.java (at line 5)
					int j = x1.next.zArray;
					        ^^^^^^^^^^^^^^
				Zork cannot be resolved to a type
				----------
				3. ERROR in X.java (at line 11)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				4. ERROR in X.java (at line 12)
					Zork[] zArray;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test063() {
	this.runNegativeTest(
			new String[] {
				"p/X.java", //-----------------------------------------------------------------------
				"""
					package p;
					public class X {
						void foo() {
							int i = p.X1.z;
							int j = p.X1.zArray;
						}
					}
					
					class X1 {
						static Zork z;
						static Zork[] zArray;
					}
					"""
			},
			"""
				----------
				1. ERROR in p\\X.java (at line 4)
					int i = p.X1.z;
					        ^^^^^^
				Zork cannot be resolved to a type
				----------
				2. ERROR in p\\X.java (at line 5)
					int j = p.X1.zArray;
					        ^^^^^^^^^^^
				Zork cannot be resolved to a type
				----------
				3. ERROR in p\\X.java (at line 10)
					static Zork z;
					       ^^^^
				Zork cannot be resolved to a type
				----------
				4. ERROR in p\\X.java (at line 11)
					static Zork[] zArray;
					       ^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test064() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					import java.io.*;	\t
					public class X {
					    void foo() {
					        Serializable[] v= new ArrayListExtra[10];
					    }
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					Serializable[] v= new ArrayListExtra[10];
					                      ^^^^^^^^^^^^^^
				ArrayListExtra cannot be resolved to a type
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test065() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					import java.io.*;	\t
					public class X {
					    void foo() {
					    	int l = array.length;
					    	Object o = array[1];
					
					    }
					    Zork[] array;
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					int l = array.length;
					        ^^^^^
				Zork cannot be resolved to a type
				----------
				2. ERROR in X.java (at line 5)
					Object o = array[1];
					           ^^^^^
				Zork cannot be resolved to a type
				----------
				3. ERROR in X.java (at line 8)
					Zork[] array;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test066() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					public class X {
					        void foo() {
					                Zork z1 = null;
					                Object o = z1;
					                Object o1 = z1.z2;
					                Object o2 = bar();
					                Zork[] array = null;
					                int length = array.length;
					        }
					        Zork bar() {
					        }
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					Zork z1 = null;
					^^^^
				Zork cannot be resolved to a type
				----------
				2. ERROR in X.java (at line 6)
					Object o2 = bar();
					            ^^^
				The method bar() from the type X refers to the missing type Zork
				----------
				3. ERROR in X.java (at line 7)
					Zork[] array = null;
					^^^^
				Zork cannot be resolved to a type
				----------
				4. ERROR in X.java (at line 10)
					Zork bar() {
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test067() {
	if (this.complianceLevel <= ClassFileConstants.JDK1_4)
		return;
	this.runNegativeTest(
			new String[] {
				"E.java", //-----------------------------------------------------------------------
				"""
					public class E<T> {
					    class SomeType { }
					    void foo() {
					        E<XYX> list= new E<SomeType>();
					        list = new E<SomeType>();
					    }
					    E<XYX> fList= new E<SomeType>();
					}
					"""
			},
			"""
				----------
				1. ERROR in E.java (at line 4)
					E<XYX> list= new E<SomeType>();
					  ^^^
				XYX cannot be resolved to a type
				----------
				2. ERROR in E.java (at line 7)
					E<XYX> fList= new E<SomeType>();
					  ^^^
				XYX cannot be resolved to a type
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test068() {
	if (this.complianceLevel <= ClassFileConstants.JDK1_4)
		return;
	this.runNegativeTest(
			new String[] {
				"E.java", //-----------------------------------------------------------------------
				"""
					import java.util.Map;
					public class E<T> {
					    static class SomeType { }
					    void foo() {
					        E<Map<String, ? extends XYX>> list= new E<Map<String, ? extends SomeType>>() {
					        };
					    }
					}
					"""
			},
			"""
				----------
				1. ERROR in E.java (at line 5)
					E<Map<String, ? extends XYX>> list= new E<Map<String, ? extends SomeType>>() {
					                        ^^^
				XYX cannot be resolved to a type
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test069() {
	this.runNegativeTest(
			new String[] {
				"p/X.java", //-----------------------------------------------------------------------
				"""
					package p;
					public class X {
						IOException foo() {}
					}
					""",
				"p/Y.java", //-----------------------------------------------------------------------
				"""
					package p;
					import java.io.*;
					public class Y {
					   void foo(IOException e) {}
					   void bar(Zork z) {}
					}
					"""
			},
			"""
				----------
				1. ERROR in p\\X.java (at line 3)
					IOException foo() {}
					^^^^^^^^^^^
				IOException cannot be resolved to a type
				----------
				----------
				1. ERROR in p\\Y.java (at line 5)
					void bar(Zork z) {}
					         ^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test070() {
	this.runNegativeTest(
			new String[] {
				"p/X.java", //-----------------------------------------------------------------------
				"""
					package p;
					public class X {
						IOException foo() {}
					}
					""",
				"q/Y.java", //-----------------------------------------------------------------------
				"""
					package q;
					import p.*;
					import java.io.*;
					public class Y {
					   void foo(IOException e) {}
					   void bar(Zork z) {}
					}
					"""
			},
			"""
				----------
				1. ERROR in p\\X.java (at line 3)
					IOException foo() {}
					^^^^^^^^^^^
				IOException cannot be resolved to a type
				----------
				----------
				1. ERROR in q\\Y.java (at line 6)
					void bar(Zork z) {}
					         ^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test071() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					public class X {
						IOException foo() {}
					}
					""",
				"Y.java", //-----------------------------------------------------------------------
				"""
					import java.io.*;
					public class Y {
					   void foo(IOException e) {}
					   void bar(Zork z) {}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					IOException foo() {}
					^^^^^^^^^^^
				IOException cannot be resolved to a type
				----------
				----------
				1. ERROR in Y.java (at line 4)
					void bar(Zork z) {}
					         ^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test072() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					public class X {
						public void foo() throws Foo {
						}
						public void bar() throws Zork {
						}\t
					}
					
					class Foo extends Zork {
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					public void foo() throws Foo {
					                         ^^^
				No exception of type Foo can be thrown; an exception type must be a subclass of Throwable
				----------
				2. ERROR in X.java (at line 4)
					public void bar() throws Zork {
					                         ^^^^
				Zork cannot be resolved to a type
				----------
				3. ERROR in X.java (at line 8)
					class Foo extends Zork {
					                  ^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test073() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					public class X {
						/**
						 * @see Foo.Private#foo()
						 * @param p
						 */
						void foo(Foo.Private p) {
							p.foo();
						}
					}
					
					class Foo {
						private class Private {
							private void foo(){}
						}
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					void foo(Foo.Private p) {
					         ^^^^^^^^^^^
				The type Foo.Private is not visible
				----------
				2. ERROR in X.java (at line 7)
					p.foo();
					^
				The type Foo.Private is not visible
				----------
				3. WARNING in X.java (at line 12)
					private class Private {
					              ^^^^^^^
				The type Foo.Private is never used locally
				----------
				4. WARNING in X.java (at line 13)
					private void foo(){}
					             ^^^^^
				The method foo() from the type Foo.Private is never used locally
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test074() {
	String expected = this.complianceLevel <= ClassFileConstants.JDK1_4
		? 		"""
			----------
			1. ERROR in X.java (at line 4)
				bar1().foo();
				^^^^
			The method bar1() from the type X refers to the missing type Zork
			----------
			2. ERROR in X.java (at line 5)
				bar2();
				^^^^
			The method bar2() from the type X refers to the missing type Zork
			----------
			3. ERROR in X.java (at line 6)
				bar3(null);
				^^^^
			The method bar3(Zork) from the type X refers to the missing type Zork
			----------
			4. ERROR in X.java (at line 7)
				bar4(null,null);
				^^^^
			The method bar4(Zork) from the type X refers to the missing type Zork
			----------
			5. ERROR in X.java (at line 9)
				Zork<String> bar1() {}
				^^^^
			Zork cannot be resolved to a type
			----------
			6. ERROR in X.java (at line 9)
				Zork<String> bar1() {}
				     ^^^^^^
			Syntax error, parameterized types are only available if source level is 1.5 or greater
			----------
			7. ERROR in X.java (at line 10)
				List<Zork> bar2() {}
				     ^^^^
			Syntax error, parameterized types are only available if source level is 1.5 or greater
			----------
			8. ERROR in X.java (at line 10)
				List<Zork> bar2() {}
				     ^^^^
			Zork cannot be resolved to a type
			----------
			9. ERROR in X.java (at line 11)
				void bar3(Zork<String> z) {}
				          ^^^^
			Zork cannot be resolved to a type
			----------
			10. ERROR in X.java (at line 11)
				void bar3(Zork<String> z) {}
				               ^^^^^^
			Syntax error, parameterized types are only available if source level is 1.5 or greater
			----------
			11. ERROR in X.java (at line 12)
				void bar4(Zork<String,String> z) {}
				          ^^^^
			Zork cannot be resolved to a type
			----------
			12. ERROR in X.java (at line 12)
				void bar4(Zork<String,String> z) {}
				               ^^^^^^^^^^^^^
			Syntax error, parameterized types are only available if source level is 1.5 or greater
			----------
			"""
		: 		"""
			----------
			1. ERROR in X.java (at line 4)
				bar1().foo();
				^^^^
			The method bar1() from the type X refers to the missing type Zork
			----------
			2. ERROR in X.java (at line 5)
				bar2();
				^^^^
			The method bar2() from the type X refers to the missing type Zork
			----------
			3. ERROR in X.java (at line 6)
				bar3(null);
				^^^^
			The method bar3(Zork<String>) from the type X refers to the missing type Zork
			----------
			4. ERROR in X.java (at line 7)
				bar4(null,null);
				^^^^
			The method bar4(Zork<String,String>) from the type X refers to the missing type Zork
			----------
			5. ERROR in X.java (at line 9)
				Zork<String> bar1() {}
				^^^^
			Zork cannot be resolved to a type
			----------
			6. ERROR in X.java (at line 10)
				List<Zork> bar2() {}
				     ^^^^
			Zork cannot be resolved to a type
			----------
			7. ERROR in X.java (at line 11)
				void bar3(Zork<String> z) {}
				          ^^^^
			Zork cannot be resolved to a type
			----------
			8. ERROR in X.java (at line 12)
				void bar4(Zork<String,String> z) {}
				          ^^^^
			Zork cannot be resolved to a type
			----------
			""";

	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					import java.util.List;
					public class X {
						void foo() {
							bar1().foo();
							bar2();
							bar3(null);
							bar4(null,null);
						}
						Zork<String> bar1() {}
						List<Zork> bar2() {}
						void bar3(Zork<String> z) {}
						void bar4(Zork<String,String> z) {}
					}
					""",//-----------------------------------------------------------------------
			},
			expected);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test075() {
	String expected = this.complianceLevel <= ClassFileConstants.JDK1_4
		? 		"""
			----------
			1. ERROR in X.java (at line 3)
				Zork<?,?> z = (Zork<?, ? extends Number>) o;
				^^^^
			Zork cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 3)
				Zork<?,?> z = (Zork<?, ? extends Number>) o;
				     ^^^
			Syntax error, parameterized types are only available if source level is 1.5 or greater
			----------
			3. ERROR in X.java (at line 3)
				Zork<?,?> z = (Zork<?, ? extends Number>) o;
				               ^^^^
			Zork cannot be resolved to a type
			----------
			4. ERROR in X.java (at line 3)
				Zork<?,?> z = (Zork<?, ? extends Number>) o;
				                    ^^^^^^^^^^^^^^^^^^^
			Syntax error, parameterized types are only available if source level is 1.5 or greater
			----------
			5. ERROR in X.java (at line 4)
				String s = (Zork<?, ? extends Number>) o;
				           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Zork cannot be resolved to a type
			----------
			6. ERROR in X.java (at line 4)
				String s = (Zork<?, ? extends Number>) o;
				            ^^^^
			Zork cannot be resolved to a type
			----------
			7. ERROR in X.java (at line 4)
				String s = (Zork<?, ? extends Number>) o;
				                 ^^^^^^^^^^^^^^^^^^^
			Syntax error, parameterized types are only available if source level is 1.5 or greater
			----------
			"""
		: 		"""
			----------
			1. ERROR in X.java (at line 3)
				Zork<?,?> z = (Zork<?, ? extends Number>) o;
				^^^^
			Zork cannot be resolved to a type
			----------
			2. WARNING in X.java (at line 3)
				Zork<?,?> z = (Zork<?, ? extends Number>) o;
				              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Type safety: Unchecked cast from Object to Zork<?,? extends Number>
			----------
			3. ERROR in X.java (at line 3)
				Zork<?,?> z = (Zork<?, ? extends Number>) o;
				               ^^^^
			Zork cannot be resolved to a type
			----------
			4. WARNING in X.java (at line 4)
				String s = (Zork<?, ? extends Number>) o;
				           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Type safety: Unchecked cast from Object to Zork<?,? extends Number>
			----------
			5. ERROR in X.java (at line 4)
				String s = (Zork<?, ? extends Number>) o;
				           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Zork<capture#3-of ?,capture#4-of ? extends Number> cannot be resolved to a type
			----------
			6. ERROR in X.java (at line 4)
				String s = (Zork<?, ? extends Number>) o;
				            ^^^^
			Zork cannot be resolved to a type
			----------
			""";

	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					public class X {
						void foo(Object o) {
							Zork<?,?> z = (Zork<?, ? extends Number>) o;
							String s = (Zork<?, ? extends Number>) o;
						}
					}
					""",//-----------------------------------------------------------------------
			},
			expected);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test076() {
	String expected = this.complianceLevel <= ClassFileConstants.JDK1_4
		? 		"""
			----------
			1. ERROR in X.java (at line 3)
				Zork<?,?> z = (Zork<?, ? super Number>) o;
				^^^^
			Zork cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 3)
				Zork<?,?> z = (Zork<?, ? super Number>) o;
				     ^^^
			Syntax error, parameterized types are only available if source level is 1.5 or greater
			----------
			3. ERROR in X.java (at line 3)
				Zork<?,?> z = (Zork<?, ? super Number>) o;
				               ^^^^
			Zork cannot be resolved to a type
			----------
			4. ERROR in X.java (at line 3)
				Zork<?,?> z = (Zork<?, ? super Number>) o;
				                    ^^^^^^^^^^^^^^^^^
			Syntax error, parameterized types are only available if source level is 1.5 or greater
			----------
			5. ERROR in X.java (at line 4)
				String s = (Zork<?, ? super Number>) o;
				           ^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Zork cannot be resolved to a type
			----------
			6. ERROR in X.java (at line 4)
				String s = (Zork<?, ? super Number>) o;
				            ^^^^
			Zork cannot be resolved to a type
			----------
			7. ERROR in X.java (at line 4)
				String s = (Zork<?, ? super Number>) o;
				                 ^^^^^^^^^^^^^^^^^
			Syntax error, parameterized types are only available if source level is 1.5 or greater
			----------
			"""
		: 		"""
			----------
			1. ERROR in X.java (at line 3)
				Zork<?,?> z = (Zork<?, ? super Number>) o;
				^^^^
			Zork cannot be resolved to a type
			----------
			2. WARNING in X.java (at line 3)
				Zork<?,?> z = (Zork<?, ? super Number>) o;
				              ^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Type safety: Unchecked cast from Object to Zork<?,? super Number>
			----------
			3. ERROR in X.java (at line 3)
				Zork<?,?> z = (Zork<?, ? super Number>) o;
				               ^^^^
			Zork cannot be resolved to a type
			----------
			4. WARNING in X.java (at line 4)
				String s = (Zork<?, ? super Number>) o;
				           ^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Type safety: Unchecked cast from Object to Zork<?,? super Number>
			----------
			5. ERROR in X.java (at line 4)
				String s = (Zork<?, ? super Number>) o;
				           ^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Zork<capture#3-of ?,capture#4-of ? super Number> cannot be resolved to a type
			----------
			6. ERROR in X.java (at line 4)
				String s = (Zork<?, ? super Number>) o;
				            ^^^^
			Zork cannot be resolved to a type
			----------
			""";

	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					public class X {
						void foo(Object o) {
							Zork<?,?> z = (Zork<?, ? super Number>) o;
							String s = (Zork<?, ? super Number>) o;
						}
					}
					""",//-----------------------------------------------------------------------
			},
			expected);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=196200 - variation
public void test077() {
	String expected = this.complianceLevel <= ClassFileConstants.JDK1_4
		? 		"""
			----------
			1. ERROR in X.java (at line 3)
				Zork<?,?> z = (Zork<?, ? super Number[]>) o;
				^^^^
			Zork cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 3)
				Zork<?,?> z = (Zork<?, ? super Number[]>) o;
				     ^^^
			Syntax error, parameterized types are only available if source level is 1.5 or greater
			----------
			3. ERROR in X.java (at line 3)
				Zork<?,?> z = (Zork<?, ? super Number[]>) o;
				               ^^^^
			Zork cannot be resolved to a type
			----------
			4. ERROR in X.java (at line 3)
				Zork<?,?> z = (Zork<?, ? super Number[]>) o;
				                    ^^^^^^^^^^^^^^^^^^^
			Syntax error, parameterized types are only available if source level is 1.5 or greater
			----------
			5. ERROR in X.java (at line 4)
				String s = (Zork<?, ? extends Number[]>) o;
				           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Zork cannot be resolved to a type
			----------
			6. ERROR in X.java (at line 4)
				String s = (Zork<?, ? extends Number[]>) o;
				            ^^^^
			Zork cannot be resolved to a type
			----------
			7. ERROR in X.java (at line 4)
				String s = (Zork<?, ? extends Number[]>) o;
				                 ^^^^^^^^^^^^^^^^^^^^^
			Syntax error, parameterized types are only available if source level is 1.5 or greater
			----------
			"""
		: 		"""
			----------
			1. ERROR in X.java (at line 3)
				Zork<?,?> z = (Zork<?, ? super Number[]>) o;
				^^^^
			Zork cannot be resolved to a type
			----------
			2. WARNING in X.java (at line 3)
				Zork<?,?> z = (Zork<?, ? super Number[]>) o;
				              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Type safety: Unchecked cast from Object to Zork<?,? super Number[]>
			----------
			3. ERROR in X.java (at line 3)
				Zork<?,?> z = (Zork<?, ? super Number[]>) o;
				               ^^^^
			Zork cannot be resolved to a type
			----------
			4. WARNING in X.java (at line 4)
				String s = (Zork<?, ? extends Number[]>) o;
				           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Type safety: Unchecked cast from Object to Zork<?,? extends Number[]>
			----------
			5. ERROR in X.java (at line 4)
				String s = (Zork<?, ? extends Number[]>) o;
				           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Zork<capture#3-of ?,capture#4-of ? extends Number[]> cannot be resolved to a type
			----------
			6. ERROR in X.java (at line 4)
				String s = (Zork<?, ? extends Number[]>) o;
				            ^^^^
			Zork cannot be resolved to a type
			----------
			""";

	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					public class X {
						void foo(Object o) {
							Zork<?,?> z = (Zork<?, ? super Number[]>) o;
							String s = (Zork<?, ? extends Number[]>) o;
						}
					}
					""",//-----------------------------------------------------------------------
			},
			expected);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=220967
public void test078() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					import java.util.List;
					interface B {
					  B m(String seq);
					}
					public class X implements B {
						public Zork m(String arg0) {
							return null;
						}
					}
					""",//-----------------------------------------------------------------------
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					public Zork m(String arg0) {
					       ^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=220967 - variation
public void test079() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					public class X {
						public Zork m(X x) {
							return x;
						}
					}
					""",//-----------------------------------------------------------------------
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					public Zork m(X x) {
					       ^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=220967 - variation
public void test080() {
	this.runNegativeTest(
			new String[] {
				"X.java", //-----------------------------------------------------------------------
				"""
					import java.util.List;
					interface B {
					  void m() throws Exception;
					}
					public class X implements B {
						public void m() throws IOException {
						}
					}
					""",//-----------------------------------------------------------------------
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					public void m() throws IOException {
					                       ^^^^^^^^^^^
				IOException cannot be resolved to a type
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=239758
public void test081() {
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) return;
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(	CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.ENABLED);
	runner.customOptions.put(CompilerOptions.OPTION_ReportInvalidJavadoc, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportInvalidJavadocTags, CompilerOptions.ENABLED);
	runner.testFiles =
			new String[] {
				"com/ost/util/report/Matrix.java", // =================
				"""
					package com.ost.util.report;
					import java.io.Serializable;
					import com.ost.util.report.exceptions.InvalidRowSizeException;
					public class Matrix<T> implements Serializable {
						/**
						 * @see exceptions.InvalidRowSizeException2
						 */
						public synchronized final void addRow(Object[] row){
								throw new InvalidRowSizeException();
						}
					}
					""",
				"com/ost/util/report/FilterConstraintSpecification.java", // =================
				"""
					package com.ost.util.report;
					import java.io.Serializable;
					import com.ost.util.report.exceptions.MalformedFilterConstraintSpecification;
					public final class FilterConstraintSpecification implements Serializable, Cloneable {
						private final void makeConstraint(){
							throw new MalformedFilterConstraintSpecification();
						}
					}
					""",
				"com/ost/util/report/exceptions/MalformedFilterConstraintSpecification.java", // =================
				"""
					package com.ost.util.report.exceptions;
					public class MalformedFilterConstraintSpecification extends RuntimeException {
						/** Creates a new instance of MalformedFilterConstraintSpecification */
						public MalformedFilterConstraintSpecification() {
							super();
						}
						/* Creates a new instance of MalformedFilterConstraintSpecification */
						public MalformedFilterConstraintSpecification(String message) {
							super(message);
						}
					}
					""",
				"com/ost/util/report/exceptions/InvalidRowSizeException.java", // =================
				"""
					package com.ost.util.report.exceptions;
					public class InvalidRowSizeException extends RuntimeException {
						/** Creates a new instance of InvalidRowSizeException */
						public InvalidRowSizeException() {
							super();
						}
						/* Creates a new instance of InvalidRowSizeException */
						public InvalidRowSizeException(String message) {
							super(message);
						}
					}
					"""
			};
	runner.expectedCompilerLog =
			"""
				----------
				1. WARNING in com\\ost\\util\\report\\Matrix.java (at line 4)
					public class Matrix<T> implements Serializable {
					             ^^^^^^
				The serializable class Matrix does not declare a static final serialVersionUID field of type long
				----------
				2. ERROR in com\\ost\\util\\report\\Matrix.java (at line 6)
					* @see exceptions.InvalidRowSizeException2
					       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Javadoc: exceptions cannot be resolved to a type
				----------
				----------
				1. WARNING in com\\ost\\util\\report\\FilterConstraintSpecification.java (at line 4)
					public final class FilterConstraintSpecification implements Serializable, Cloneable {
					                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				The serializable class FilterConstraintSpecification does not declare a static final serialVersionUID field of type long
				----------
				2. WARNING in com\\ost\\util\\report\\FilterConstraintSpecification.java (at line 5)
					private final void makeConstraint(){
					                   ^^^^^^^^^^^^^^^^
				The method makeConstraint() from the type FilterConstraintSpecification is never used locally
				----------
				----------
				1. WARNING in com\\ost\\util\\report\\exceptions\\MalformedFilterConstraintSpecification.java (at line 2)
					public class MalformedFilterConstraintSpecification extends RuntimeException {
					             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				The serializable class MalformedFilterConstraintSpecification does not declare a static final serialVersionUID field of type long
				----------
				----------
				1. WARNING in com\\ost\\util\\report\\exceptions\\InvalidRowSizeException.java (at line 2)
					public class InvalidRowSizeException extends RuntimeException {
					             ^^^^^^^^^^^^^^^^^^^^^^^
				The serializable class InvalidRowSizeException does not declare a static final serialVersionUID field of type long
				----------
				""";
	runner.javacTestOptions =
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=239758 - variation
public void test082() {
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) return;
	this.runConformTest(
			new String[] {
				"com/ost/util/report/Matrix.java", // =================
				"""
					package com.ost.util.report;
					import java.io.Serializable;
					import com.ost.util.report.exceptions.InvalidRowSizeException;
					public class Matrix<T> implements Serializable {
						/**
						 * @see exceptions.InvalidRowSizeException2
						 */
						public synchronized final void addRow(Object[] row){
								throw new InvalidRowSizeException();
						}
					}
					""",
				"com/ost/util/report/FilterConstraintSpecification.java", // =================
				"""
					package com.ost.util.report;
					import java.io.Serializable;
					import com.ost.util.report.exceptions.MalformedFilterConstraintSpecification;
					public final class FilterConstraintSpecification implements Serializable, Cloneable {
						private final void makeConstraint(){
							throw new MalformedFilterConstraintSpecification();
						}
					}
					""",
				"com/ost/util/report/exceptions/MalformedFilterConstraintSpecification.java", // =================
				"""
					package com.ost.util.report.exceptions;
					public class MalformedFilterConstraintSpecification extends RuntimeException {
						/** Creates a new instance of MalformedFilterConstraintSpecification */
						public MalformedFilterConstraintSpecification() {
							super();
						}
						/* Creates a new instance of MalformedFilterConstraintSpecification */
						public MalformedFilterConstraintSpecification(String message) {
							super(message);
						}
					}
					""",
				"com/ost/util/report/exceptions/InvalidRowSizeException.java", // =================
				"""
					package com.ost.util.report.exceptions;
					public class InvalidRowSizeException extends RuntimeException {
						/** Creates a new instance of InvalidRowSizeException */
						public InvalidRowSizeException() {
							super();
						}
						/* Creates a new instance of InvalidRowSizeException */
						public InvalidRowSizeException(String message) {
							super(message);
						}
					}
					"""
			},
			"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=239758 - variation
public void test083() {
	this.runConformTest(
			new String[] {
				"foo/X.java", // =================
				"""
					package foo;
					import foo.exceptions.*;
					public class X {
					  class exceptions {}
					  exceptions E;
					}
					""",
				"foo/exceptions/Z.java", // =================
				"""
					package foo.exceptions;
					public class Z {
					}
					"""
			},
			"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=239758 - variation
public void test084() {
	this.runNegativeTest(
			new String[] {
				"foo/X.java", // =================
				"""
					package foo;
					import foo.exceptions.*;
					public class X {
					  exceptions E;
					}
					class exceptions {}
					""",
				"foo/exceptions/Z.java", // =================
				"""
					package foo.exceptions;
					public class Z {
					}
					"""
			},
			"""
				----------
				1. WARNING in foo\\X.java (at line 2)
					import foo.exceptions.*;
					       ^^^^^^^^^^^^^^
				The import foo.exceptions is never used
				----------
				----------
				1. ERROR in foo\\exceptions\\Z.java (at line 1)
					package foo.exceptions;
					        ^^^^^^^^^^^^^^
				The package foo.exceptions collides with a type
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=239758 - variation
public void test085() {
	this.runNegativeTest(
			new String[] {
				"p/X.java", // =================
				"""
					package p;
					public class X extends zork.Z {
					}
					""",
				"p/Y.java", // =================
				"""
					package p;
					import p.zork.Z;
					public class Y {
					}
					""",
				"p/zork/Z.java", // =================
				"""
					package p.zork;
					public class Z {
					}
					"""
			},
			"""
				----------
				1. ERROR in p\\X.java (at line 2)
					public class X extends zork.Z {
					                       ^^^^
				zork cannot be resolved to a type
				----------
				----------
				1. WARNING in p\\Y.java (at line 2)
					import p.zork.Z;
					       ^^^^^^^^
				The import p.zork.Z is never used
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=239758 - variation
public void test086() {
	this.runNegativeTest(
			new String[] {
				"p/X.java", // =================
				"""
					package p;
					public class X extends zork.Z {
					}
					""",
				"p/Y.java", // =================
				"""
					package p;
					import p.zork.*;
					public class Y {
					}
					""",
				"p/zork/Z.java", // =================
				"""
					package p.zork;
					public class Z {
					}
					"""
			},
			"""
				----------
				1. ERROR in p\\X.java (at line 2)
					public class X extends zork.Z {
					                       ^^^^
				zork cannot be resolved to a type
				----------
				----------
				1. WARNING in p\\Y.java (at line 2)
					import p.zork.*;
					       ^^^^^^
				The import p.zork is never used
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=239758 - variation
public void test087() {
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) return;
	this.runNegativeTest(
			new String[] {
				"p/X.java", // =================
				"""
					package p;
					public class X extends zork.Z {
					}
					""",
				"p/Y.java", // =================
				"""
					package p;
					import static p.zork.Z.M;
					public class Y {
					}
					""",
				"p/zork/Z.java", // =================
				"""
					package p.zork;
					public class Z {
						public static class M {}
					}
					"""
			},
			"""
				----------
				1. ERROR in p\\X.java (at line 2)
					public class X extends zork.Z {
					                       ^^^^
				zork cannot be resolved to a type
				----------
				----------
				1. WARNING in p\\Y.java (at line 2)
					import static p.zork.Z.M;
					              ^^^^^^^^^^
				The import p.zork.Z.M is never used
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=239758 - variation
public void test088() {
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) return;
	this.runNegativeTest(
			new String[] {
				"p/X.java", // =================
				"""
					package p;
					public class X extends zork.Z {
					}
					""",
				"p/Y.java", // =================
				"""
					package p;
					import static p.zork.Z.*;
					public class Y {
					}
					""",
				"p/zork/Z.java", // =================
				"""
					package p.zork;
					public class Z {
						static class M {}
					}
					"""
			},
			"""
				----------
				1. ERROR in p\\X.java (at line 2)
					public class X extends zork.Z {
					                       ^^^^
				zork cannot be resolved to a type
				----------
				----------
				1. WARNING in p\\Y.java (at line 2)
					import static p.zork.Z.*;
					              ^^^^^^^^
				The import p.zork.Z is never used
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=245304
public void test089() {
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) return;
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.ENABLED);
	this.runConformTest(
			new String[] {
					"com/foo/bar/baz/reporting/dom/ReportExceptionBase.java", // ================
					"""
						package com.foo.bar.baz.reporting.dom;
						public class ReportExceptionBase extends Exception  {
						}
						""",
					"com/foo/bar/baz/reporting/Report.java", // ================
					"""
						package com.foo.bar.baz.reporting;
						import com.foo.bar.baz.reporting.dom.ReportExceptionBase;
						/**
						 * {@link dom.ReportDefs.ReportType.foo foo}
						 */
						public abstract class Report {
						}
						""",
					"com/foo/bar/baz/reporting/Derived.java", // ================
					"""
						package com.foo.bar.baz.reporting;
						import com.foo.bar.baz.reporting.dom.ReportExceptionBase;
						public class Derived {
						  public Derived() throws ReportExceptionBase {
						    throw new ReportExceptionBase();
						  }
						}
						""",
			},
			"",
			null,
			true,
			null,
			options,
			null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=247666
public void test090() {
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) return;
	this.runNegativeTest(
			new String[] {
				"X.java", // =================
				"""
					public class X {
					<U,V extends Runnable> void foo(Zork z) {}\t
						void bar() {
							foo(null);
						}\s
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					<U,V extends Runnable> void foo(Zork z) {}\t
					                                ^^^^
				Zork cannot be resolved to a type
				----------
				2. ERROR in X.java (at line 4)
					foo(null);
					^^^
				The method foo(Zork) from the type X refers to the missing type Zork
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=252288
public void test091()  throws Exception {
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) return;
	this.runNegativeTest(
		new String[] {
			"TypeUtils.java",
			"""
				import java.util.Collection;
				import java.util.Iterator;
				
				public final class TypeUtils {
				
					// personal
				
					private TypeUtils() {
					}
				
					// class
				
					/**
					 * Returns true if a target type is exactly any one in a group of types.
					 * @param target Target type. Never null.
					 * @param types Group of types. If empty, returns false. Never null.
					 * @return True if the target is a valid type.
					 */
					public static boolean isIdenticalToAny(Class<?> target, Collection<Class<?>> types) {
						if (target == null) throw new IllegalArgumentException(
						"Target is null.");
				
						if (types.contains(target)) return true;
						return false;
					}
				
					/**
					 * Returns true if a target type is the same or a subtype of (assignable to)
					 * a reference type. Convenience method for completeness. Forwards to
					 * Class.isAssignableFrom().
					 * @param target Target type. Never null.
					 * @param type Reference type. Never null.
					 * @return True if condition is met.
					 */
					public static boolean isAssignableTo(Class<?> target, Class<?> type) {
						return type.isAssignableFrom(target);
					}
				
					/**
					 * Returns true if a target type is the same or a subtype of (assignable to)
					 * any one reference type.
					 * @param target Target type. Never null.
					 * @param types Reference types (Class). Never null. If empty returns false.
					 * @return True if condition is met.
					 */
					public static boolean isAssignableToAny(Class<?> target,
					Collection<Class<?>> types) {
						if (types.isEmpty()) return false;
				
						for(Class<?> type : types) {
							if (type.isAssignableFrom(target)) return true;
						}
						return false;
					}
				
					/**
					 * Returns true if any one target type is the same or a subtype of
					 * (assignable to) a reference type.
					 * @param targets Target types (Class). Never null. If empty returns false.
					 * @param type Reference type. Never null.
					 * @return True if condition is met.
					 */
					public static boolean areAnyAssignableTo(Collection<Class<?>> targets,
					Class<?> type) {
						if (targets.isEmpty()) return false;
				
						for(Class<?> target : targets) {
							if (type.isAssignableFrom(target)) return true;
						}
						return false;
					}
				
					/**
					 * Returns true if any one target type is the same or a subtype of
					 * (assignable to) any one reference type.
					 * @param targets Target types (Class). Never null. If empty returns false.
					 * @param types Reference types (Class). Never null. If empty returns false.
					 * @return True if condition is met.
					 */
					public static boolean areAnyAssignableToAny(Collection<Class<?>> targets,
					Collection<Class<?>> types) {
						if (targets.isEmpty()) return false;
						if (types.isEmpty()) return false;
				
						for(Class<?> target : targets) {
							if (isAssignableToAny(target, types)) return true;
						}
						return false;
					}
				
					/**
					 * Returns true if a target object\'s type is the same or a subtype of
					 * (assignable to) a reference type. Convenience method for completeness.
					 * Forwards to Class.isInstance().
					 * @param target Target object. Never null.
					 * @param type Reference type. Never null.
					 * @return True if condition is met.
					 */
					public static boolean isInstanceOf(Object target, Class<?> type) {
						return type.isInstance(target);
					}
				
					/**
					 * Returns true if a target object\'s type is the same or a subtype of
					 * (assignable to) any one type.
					 * @param target Target object. Never null.
					 * @param types Reference types. Never null. If empty returns false.
					 * @return True if condition is met.
					 */
					public static boolean isInstanceOfAny(Object target,
					Collection<Class<?>> types) {
						if (types.isEmpty()) return false;
				
						for (Class<?> type : types) {
							if (type.isInstance(target)) return true;
						}
						return false;
					}
				
					/**
					 * Returns true if any one target object\'s type is the same or a subtype of
					 * (assignable to) a reference type.
					 * @param targets Target objects. Never null. If empty returns false.
					 * @param type Reference type. Never null.
					 * @return True if condition is met.
					 */
					public static boolean areAnyInstanceOf(Collection<Object> targets,
					Class<?> type) {
						if (targets.isEmpty()) return false;
				
						for(Object target : targets) {
							if (type.isInstance(target)) return true;
						}
						return false;
					}
				
					/**
					 * Returns true if all target object types are the same or a subtype of
					 * (assignable to) a reference type.
					 * @param targets Target objects. Never null. If empty returns
					 * false.
					 * @param type Reference type. Never null.
					 * @return True if condition is met.
					 */
					public static boolean areAllInstanceOf(Collection<Object> targets,
					Class<?> type) {
						if (targets.isEmpty()) return false;
				
						for(Object target : targets) {
							if (!type.isInstance(target)) return false;
						}
						return true;
					}
				
					/**
					 * Returns true if no target object types are the same or a subtype of
					 * (assignable to) a reference type.
					 * @param targets Target objects. Never null. If empty returns
					 * false.
					 * @param type Reference type. Never null.
					 * @return True if condition is met.
					 */
					public static boolean areNoneInstanceOf(Collection<Object> targets,
					Class<?> type) {
						if (targets.isEmpty()) return false;
				
						for(Object target : targets) {
							if (type.isInstance(target)) return false;
						}
						return true;
					}
				
					/**
					 * Returns true if any one target object\'s type is the same or a subtype of
					 * (assignable to) any one reference type.
					 * @param targets Target objects. Never null. If empty returns
					 * false.
					 * @param types Reference types. Never null. If empty returns false.
					 * @return True if condition is met.
					 */
					public static boolean areAnyInstanceOfAny(Collection<Object> targets,
					Collection<Class<?>> types) {
						if (targets.isEmpty()) return false;
						if (types.isEmpty()) return false;
				
						for(Object target : targets) {
							if (isInstanceOfAny(target, types)) return true;
						}
						return false;
					}
				
					/**
					 * Returns only those target objects whose type is identical to the included
					 * reference type.
					 * @param targets Group of target objects. If empty, returns empty. Never
					 * null.
					 * @param type Included reference type. Never null.
					 * @param retVal Return value object. The collection of valid target
					 * objects. Can be {@code targets}. Never null.
					 * @return Reference to retVal. Never null.
					 */
					public static Collection<Object> includeIdenticalTo(Collection<Object> targets,
					Class<?> type, Collection<Object> retVal) {
						// save targets in retVal
						if (targets != retVal) {
							retVal.clear();
							retVal.addAll(targets);
						}
				
						// remove unwanted targets, by target
						Iterator<?> objectI = retVal.iterator();
						while (objectI.hasNext()) {
							Object object = objectI.next();
							if (!type.equals(object.getClass())) objectI.remove();
						}
				
						return retVal;
					}
				
					/**
					 * Returns only those target objects whose type is exactly one of the
					 * included reference types.
					 * @param targets Group of target objects. If empty, returns empty. Never
					 * null.
					 * @param types Group of included reference types. If empty, returns empty.
					 * If null, all types are included (all targets are returned).
					 * @param retVal Return value object. The collection of valid target
					 * objects. Can be {@code targets}. Never null.
					 * @return Reference to retVal. Never null.
					 */
					public static Collection<Object> includeIdenticalToAny(
					Collection<Object> targets,
					Collection<Class<?>> types, Collection<Object> retVal) {
						// save targets in retVal
						if (targets != retVal) {
							retVal.clear();
							retVal.addAll(targets);
						}
				
						if (types == null) return retVal;
				
						// remove unwanted targets, by target
						Iterator<Object> objectI = retVal.iterator();
						while (objectI.hasNext()) {
							Object object = objectI.next();
							if (!isIdenticalToAny(object.getClass(), types)) objectI.remove();
						}
				
						return retVal;
					}
				
					/**
					 * Returns only those target objects whose type is NOT identical to the
					 * excluded reference type.
					 * @param targets Group of target objects. If empty, returns empty. Never
					 * null.
					 * @param type The excluded reference type. Never null.
					 * @param retVal Return value object. The collection of valid target
					 * objects. Can be {@code targets}. Never null.
					 * @return Reference to retVal. Never null.
					 */
					public static Collection<Object> excludeIdenticalTo(
					Collection<Object> targets, Class<?> type,
					Collection<Object> retVal) {
						// save targets in retVal
						if (targets != retVal) {
							retVal.clear();
							retVal.addAll(targets);
						}
				
						// remove unwanted targets, by target
						Iterator<Object> objectI = retVal.iterator();
						while (objectI.hasNext()) {
							Object object = objectI.next();
							if (type.equals(object.getClass())) objectI.remove();
						}
				
						return retVal;
					}
				
					/**
					 * Returns only those target objects whose type is NOT exactly one of the
					 * excluded reference types.
					 * @param targets Group of target objects. If empty, returns empty. Never
					 * null.
					 * @param types Group of excluded reference types. If empty, returns empty.
					 * If null, no types are excluded (all targets are returned).
					 * @param retVal Return value object. The collection of valid target
					 * objects. Can be targets. Never null.
					 * @return Reference to retVal. Never null.
					 */
					public static Collection<Object> excludeIdenticalToAny(
					Collection<Object> targets, Collection<Class<?>> types,
					Collection<Object> retVal) {
						// save targets in retVal
						if (targets != retVal) {
							retVal.clear();
							retVal.addAll(targets);
						}
				
						if (types == null) return retVal;
				
						// remove unwanted targets, by target
						Iterator<Object> objectI = retVal.iterator();
						while (objectI.hasNext()) {
							Object object = objectI.next();
							if (isIdenticalToAny(object.getClass(), types)) objectI.remove();
						}
				
						return retVal;
					}
				
					/**
					 * Returns only those target objects whose type is assignable to (an
					 * instance of) the included reference type.
					 * @param targets Group of target objects. If empty, returns empty. Never
					 * null.
					 * @param type Included reference type. Never null.
					 * @param retVal Return value object. The collection of valid target objects
					 * (Object). Can be targets. Never null.
					 * @return Reference to retVal. Never null.
					 */
					public static Collection<Object> includeAssignableTo(
					Collection<Object> targets, Class<?> type, Collection<Object> retVal) {
						// save targets in retVal
						if (targets != retVal) {
							retVal.clear();
							retVal.addAll(targets);
						}
				
						// remove unwanted targets, by target
						Iterator<Object> objectI = retVal.iterator();
						while (objectI.hasNext()) {
							Object object = objectI.next();
							if (!type.isInstance(object)) objectI.remove();
						}
				
						return retVal;
					}
				
					/**
					 * Returns only those target objects whose type is assignable to (an
					 * instance of) any one of the included reference types.
					 * @param targets Group of target objects. If empty, returns empty. Never
					 * null.
					 * @param types Group of included reference types. If empty, returns empty.
					 * If null, all types are included (all targets are returned).
					 * @param retVal Return value object. The collection of valid target
					 * objects. Can be targets. Never null.
					 * @return Reference to retVal. Never null.
					 */
					public static Collection<Object> includeAssignableToAny(
					Collection<Object> targets, Collection<Class<?>> types,
					Collection<Object> retVal) {
						// save targets in retVal
						if (targets != retVal) {
							retVal.clear();
							retVal.addAll(targets);
						}
				
						if (types == null) return retVal;
				
						// remove unwanted targets, by target
						Iterator<Object> objectI = retVal.iterator();
						while (objectI.hasNext()) {
							Object object = objectI.next();
							if (!isInstanceOfAny(object, types)) objectI.remove();
						}
				
						return retVal;
					}
				
					/**
					 * Returns only those target objects whose type is NOT assignable to (an
					 * instance of) the excluded reference type.
					 * @param targets Group of target objects. If empty, returns empty. Never
					 * null.
					 * @param type The excluded reference type. Never null.
					 * @param retVal Return value object. The collection of valid target
					 * objects. Never null.
					 * @return Reference to retVal. Never null.
					 */
					public static Collection<Object> excludeAssignableTo(
					Collection<Object> targets, Class<?> type, Collection<Object> retVal) {
						// save targets in retVal
						if (targets != retVal) {
							retVal.clear();
							retVal.addAll(targets);
						}
				
						// remove unwanted targets, by target
						Iterator<Object> objectI = retVal.iterator();
						while (objectI.hasNext()) {
							Object object = objectI.next();
							if (type.isInstance(object)) objectI.remove();
						}
				
						return retVal;
					}
				
					/**
					 * Returns only those target objects whose type is NOT assignable to (an
					 * instance of) any one of the excluded reference types.
					 * @param targets Group of target objects. If empty, returns empty. Never
					 * null.
					 * @param types Group of excluded reference types. If empty, returns empty.
					 * If null, no types are excluded (all targets are returned).
					 * @param retVal Return value object. The collection of valid target
					 * objects. Never null.
					 * @return Reference to retVal. Never null.
					 */
					public static Collection<Object> excludeAssignableToAny(
					Collection<Object> targets, Collection<Class<?>> types,
					Collection<Object> retVal) {
						// save targets in retVal
						if (targets != retVal) {
							retVal.clear();
							retVal.addAll(targets);
						}
				
						if (types == null) return retVal;
				
						// remove unwanted targets, by target
						Iterator<Object> objectI = retVal.iterator();
						while (objectI.hasNext()) {
							Object object = objectI.next();
							if (isInstanceOfAny(object, types)) objectI.remove();
						}
				
						return retVal;
					}
				
					/**
					 * Returns the first target object whose type is assignable to (an instance
					 * of) the reference type.
					 * @param targets Group of target objects. If empty, returns null.
					 * Never null.
					 * @param type Reference type. Never null.
					 * @return The result (Object, assignable instance of type). Null if none.
					 */
					public static <T extends Class<?>> T getFirstAssignableTo(
					Collection<Object> targets, T type) {
						for(Object target : targets) {
							if (type.isInstance(target)) return target;
						}
				
						return null;
					}
				
					/**
					 * Returns the first target object whose type is exactly the specified type.
					 * @param targets Group of target objects (Object). If empty, returns null.
					 * Never null.
					 * @param type The type. Never null. objects (Object). Can be targets. Never
					 * null.
					 * @return The result (Object, exact instance of type). Null if none.
					 */
					public static Object getFirstIdenticalTo(Collection targets, Class type) {
						Iterator targetI = targets.iterator();
						while (targetI.hasNext()) {
							Object target = targetI.next();
							if (type.equals(target.getClass())) return target;
						}
				
						return null;
					}
				
					/**
					 * Gets a target object T from a source object S in a group of objects, and
					 * returns the target objects in result group R. A group object is ignored
					 * if it is not a source type or, if it is a source type, its target object
					 * is not a target type.
					 * @param group Temp input group of shared exposed objects. If null, returns
					 * empty.
					 * @param sourceType Desired source object type. Never null.
					 * @param getter Gets a target object from a source object. Never null.
					 * @param targetType Desired target object type. Never null.
					 * @param retVal Temp output group of shared exposed target objects. Never
					 * null.
					 * @return Reference to retVal. Never null.
					 */
					public static <S,T,TT extends T,R extends Collection<? super TT>> R getAll(
					Collection<?> group, Class<? extends S> sourceType, Getter<S,T> getter,
					Class<TT> targetType, R retVal) {
						if (sourceType == null) throw new IllegalArgumentException(
						"Source type is null.");
						if (getter == null) throw new IllegalArgumentException(
						"Getter is null.");
						if (targetType == null) throw new IllegalArgumentException(
						"Target type is null.");
						if (retVal == null) throw new IllegalArgumentException(
						"Return value is null.");
						retVal.clear();
				
						if (group == null) return retVal;
				
						for (Object obj : group) {
							if (!sourceType.isInstance(obj)) continue; // ignore
							S source = (S) obj;
							T target = getter.getFrom(source);
							if (!targetType.isInstance(target)) continue; // ignore
							retVal.add((TT) target);
						}
				
						return retVal;
					}
				
					/**
					 * Similar to getAll(Collection, Class, Getter, Class, Collection), but all
					 * target objects are returned, regardless of type, including nulls.
					 * @param group Temp input group of shared exposed objects. If null, returns
					 * empty.
					 * @param sourceType Desired source object type. Never null.
					 * @param getter Gets a target object from a source object. Never null.
					 * @param retVal Temp output group of shared exposed target objects. Never
					 * null.
					 * @return Reference to retVal. Never null.
					 */
					public static <S,T,R extends Collection<? super T>> R getAll(
					Collection<?> group, Class<? extends S> sourceType, Getter<S,T> getter,
					R retVal) {
						if (sourceType == null) throw new IllegalArgumentException(
						"Source type is null.");
						if (getter == null) throw new IllegalArgumentException(
						"Getter is null.");
						if (retVal == null) throw new IllegalArgumentException(
						"Return value is null.");
						retVal.clear();
				
						if (group == null) return retVal;
				
						for (Object obj : group) {
							if (!sourceType.isInstance(obj)) continue; // ignore
							S source = (S) obj;
							T target = getter.getFrom(source);
							retVal.add(target);
						}
				
						return retVal;
					}
				}
				""",
		},
		"""
			----------
			1. WARNING in TypeUtils.java (at line 441)
				public static <T extends Class<?>> T getFirstAssignableTo(
				                         ^^^^^
			The type parameter T should not be bounded by the final type Class<?>. Final types cannot be further extended
			----------
			2. ERROR in TypeUtils.java (at line 444)
				if (type.isInstance(target)) return target;
				                                    ^^^^^^
			Type mismatch: cannot convert from Object to T
			----------
			3. WARNING in TypeUtils.java (at line 458)
				public static Object getFirstIdenticalTo(Collection targets, Class type) {
				                                         ^^^^^^^^^^
			Collection is a raw type. References to generic type Collection<E> should be parameterized
			----------
			4. WARNING in TypeUtils.java (at line 458)
				public static Object getFirstIdenticalTo(Collection targets, Class type) {
				                                                             ^^^^^
			Class is a raw type. References to generic type Class<T> should be parameterized
			----------
			5. WARNING in TypeUtils.java (at line 459)
				Iterator targetI = targets.iterator();
				^^^^^^^^
			Iterator is a raw type. References to generic type Iterator<E> should be parameterized
			----------
			6. ERROR in TypeUtils.java (at line 483)
				Collection<?> group, Class<? extends S> sourceType, Getter<S,T> getter,
				                                                    ^^^^^^
			Getter cannot be resolved to a type
			----------
			7. WARNING in TypeUtils.java (at line 499)
				S source = (S) obj;
				           ^^^^^^^
			Type safety: Unchecked cast from Object to S
			----------
			8. WARNING in TypeUtils.java (at line 502)
				retVal.add((TT) target);
				           ^^^^^^^^^^^
			Type safety: Unchecked cast from T to TT
			----------
			9. ERROR in TypeUtils.java (at line 520)
				Collection<?> group, Class<? extends S> sourceType, Getter<S,T> getter,
				                                                    ^^^^^^
			Getter cannot be resolved to a type
			----------
			10. WARNING in TypeUtils.java (at line 534)
				S source = (S) obj;
				           ^^^^^^^
			Type safety: Unchecked cast from Object to S
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=250297
public void test092() {
	this.runNegativeTest(
		new String[] {
			"p1/p2/X.java", // =================
			"""
				package p1.p2;
				public class X {
					public p2.p3.Z z() {return null;}
				}
				"""
		},
		"""
			----------
			1. ERROR in p1\\p2\\X.java (at line 3)
				public p2.p3.Z z() {return null;}
				       ^^
			p2 cannot be resolved to a type
			----------
			""",
		null,
		false,
		null,
		true,
		false,
		false
	);
	Runner runner = new Runner();
	runner.javacTestOptions = JavacTestOptions.SKIP; // javac did not produce p1/p2/X.class which is needed below
	runner.testFiles =
		new String[] {
			"a/b/A.java", // =================
			"""
				package a.b;
				public class A {
					p1.p2.X x;
					void test() { x.z(); }
					void foo(p2.p3.Z z) {}
				}
				""",
			"p2/p3/Z.java", // =================
			"package p2.p3;\n" +
			"public class Z {}\n"
		};
	runner.shouldFlushOutputDirectory = false;
	runner.runConformTest();
	runner.testFiles =
		new String[] {
			"a/b/A.java", // =================
			"""
				package a.b;
				public class A {
					p1.p2.X x;
					void test() { x.z(); }
					void foo(p2.p3.Z z) {}
				}
				"""
		};
	runner.runConformTest();
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=250297 - variation
public void test093() {
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) return;
	this.runNegativeTest(
			new String[] {
				"X.java", // =================
				"""
					import java.util.List;
					
					public class X {
						void foo() {
							List<? extends Zork> zlist = null;
							bar(zlist.get(0));
						}
						<T> T bar(T t) { return t; }
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					List<? extends Zork> zlist = null;
					               ^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=250297 - variation
public void test094() {
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) return;
	this.runNegativeTest(
			new String[] {
				"X.java", // =================
				"""
					import java.util.List;
					
					public class X {
						void foo(boolean b, Runnable r) {
							bar(r);
						}
						<T> T bar(Zork z) { return z; }
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					bar(r);
					^^^
				The method bar(Zork) from the type X refers to the missing type Zork
				----------
				2. ERROR in X.java (at line 7)
					<T> T bar(Zork z) { return z; }
					          ^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=250297 - variation
public void test095() {
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) return;
	this.runNegativeTest(
			new String[] {
				"X.java", // =================
				"""
					import java.util.List;
					
					public class X {
						void foo(boolean b, Runnable r) {
							bar(r);
						}
						<T> bar(Zork z) { return z; }
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					bar(r);
					^^^
				The method bar(Runnable) is undefined for the type X
				----------
				2. ERROR in X.java (at line 7)
					<T> bar(Zork z) { return z; }
					    ^^^^^^^^^^^
				Return type for the method is missing
				----------
				3. ERROR in X.java (at line 7)
					<T> bar(Zork z) { return z; }
					        ^^^^
				Zork cannot be resolved to a type
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=257384
public void test096() {
	this.runNegativeTest(
		new String[] {
			"p2/B.java", // =================
			"""
				package p2;
				import p1.A;
				public abstract class B {
					public static A foo() {}
				}
				""",
			"p3/C.java", // =================
			"""
				package p3;
				import p1.A;
				public abstract class C extends p2.B {
					public static A foo() {}
				}
				""",
			"p/D.java", // =================
			"package p;\n" +
			"public class D extends p3.C {}"
		},
		"""
			----------
			1. ERROR in p2\\B.java (at line 2)
				import p1.A;
				       ^^
			The import p1 cannot be resolved
			----------
			2. ERROR in p2\\B.java (at line 4)
				public static A foo() {}
				              ^
			A cannot be resolved to a type
			----------
			----------
			1. ERROR in p3\\C.java (at line 2)
				import p1.A;
				       ^^
			The import p1 cannot be resolved
			----------
			2. ERROR in p3\\C.java (at line 4)
				public static A foo() {}
				              ^
			A cannot be resolved to a type
			----------
			"""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=258248
public void test097() {
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) return;
	this.runNegativeTest(
		new String[] {
			"X.java", // =================
			"""
				public class X {
				
					public static interface InnerInterface<TheTypeMirror, TheDeclaredType extends TheTypeMirror, TheClassType extends TheDeclaredType, TheInterfaceType extends TheDeclaredType, ThePrimitiveType extends TheTypeMirror, TheArrayType extends TheTypeMirror, TheTypeVariable extends TheTypeMirror, TheWildcardType extends TheTypeMirror, TheFieldDeclaration, TheTypeParameterDeclaration, TheTypeDeclaration, TheClassDeclaration extends TheTypeDeclaration> {
					}
					protected <TheTypeMirror, TheDeclaredType extends TheTypeMirror, TheClassType extends TheDeclaredType, TheInterfaceType extends TheDeclaredType, ThePrimitiveType extends TheTypeMirror, TheArrayType extends TheTypeMirror, TheTypeVariable extends TheTypeMirror, TheWildcardType extends TheTypeMirror, TheFieldDeclaration, TheTypeParameterDeclaration, TheTypeDeclaration, TheClassDeclaration extends TheTypeDeclaration, Env extends InnerInterface<TheTypeMirror, TheDeclaredType, TheClassType, TheInterfaceType, ThePrimitiveType, TheArrayType, TheTypeVariable, TheWildcardType, TheFieldDeclaration, TheTypeParameterDeclaration, TheTypeDeclaration, TheClassDeclaration>, ParamType extends TheTypeMirror> void testMethod(
							TheFieldDeclaratation fieldDeclaratation, Env
							environment) {
				
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				TheFieldDeclaratation fieldDeclaratation, Env
				^^^^^^^^^^^^^^^^^^^^^
			TheFieldDeclaratation cannot be resolved to a type
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=296660
public void test098() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"X.java",
			"""
				public class X {
				    private class A {
				    	public void foo(int a) {
				   		System.out.println("Hello");
				    	}
					    public void foo(float a) {
				   		System.out.println("Hello");
				   	}
				   	public void foo(boolean a) {
				   		System.out.println("Hello");
				   	}
				      	public void foo(Integer a) {
				   		System.out.println("Hello");
				   	}
				   }
				   private class B extends A {
						public void foo(int a) {
				   		System.out.println("Hello");
				   	}
						public void foo(float a) {
				   		System.out.println("Hello");
				   	}
				   	public void foo(double a) {
				   		System.out.println("Hello");
				   	}
				   	public void foo(char a) {
				   		System.out.println("Hello");
				   	}
				   }
				}
				"""
		};
	runner.expectedCompilerLog = isMinimumCompliant(ClassFileConstants.JDK11) ?
		"""
			----------
			1. WARNING in X.java (at line 3)
				public void foo(int a) {
				            ^^^^^^^^^^
			The method foo(int) from the type X.A is never used locally
			----------
			2. WARNING in X.java (at line 6)
				public void foo(float a) {
				            ^^^^^^^^^^^^
			The method foo(float) from the type X.A is never used locally
			----------
			3. WARNING in X.java (at line 9)
				public void foo(boolean a) {
				            ^^^^^^^^^^^^^^
			The method foo(boolean) from the type X.A is never used locally
			----------
			4. WARNING in X.java (at line 12)
				public void foo(Integer a) {
				            ^^^^^^^^^^^^^^
			The method foo(Integer) from the type X.A is never used locally
			----------
			5. WARNING in X.java (at line 16)
				private class B extends A {
				              ^
			The type X.B is never used locally
			----------
			6. WARNING in X.java (at line 23)
				public void foo(double a) {
				            ^^^^^^^^^^^^^
			The method foo(double) from the type X.B is never used locally
			----------
			7. WARNING in X.java (at line 26)
				public void foo(char a) {
				            ^^^^^^^^^^^
			The method foo(char) from the type X.B is never used locally
			----------
			"""
		:
		"""
			----------
			1. WARNING in X.java (at line 3)
				public void foo(int a) {
				            ^^^^^^^^^^
			The method foo(int) from the type X.A is never used locally
			----------
			2. WARNING in X.java (at line 6)
				public void foo(float a) {
				            ^^^^^^^^^^^^
			The method foo(float) from the type X.A is never used locally
			----------
			3. WARNING in X.java (at line 9)
				public void foo(boolean a) {
				            ^^^^^^^^^^^^^^
			The method foo(boolean) from the type X.A is never used locally
			----------
			4. WARNING in X.java (at line 12)
				public void foo(Integer a) {
				            ^^^^^^^^^^^^^^
			The method foo(Integer) from the type X.A is never used locally
			----------
			5. WARNING in X.java (at line 16)
				private class B extends A {
				              ^
			The type X.B is never used locally
			----------
			6. WARNING in X.java (at line 16)
				private class B extends A {
				              ^
			Access to enclosing constructor X.A() is emulated by a synthetic accessor method
			----------
			7. WARNING in X.java (at line 23)
				public void foo(double a) {
				            ^^^^^^^^^^^^^
			The method foo(double) from the type X.B is never used locally
			----------
			8. WARNING in X.java (at line 26)
				public void foo(char a) {
				            ^^^^^^^^^^^
			The method foo(char) from the type X.B is never used locally
			----------
			""";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings;
	runner.runWarningTest();
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=296660
public void test099() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"X.java",
			"""
				public class X {
				    private class A {
				    	public void foo(int a) {
				   		System.out.println("Hello");
				    	}
					    public void foo(float a) {
				   		System.out.println("Hello");
				   	}
				   	public void foo(boolean a) {
				   		System.out.println("Hello");
				   	}
				      	public void foo(Integer a) {
				   		System.out.println("Hello");
				   	}
				   }
				   private class B extends A {
						public void foo(int a) {
				   		System.out.println("Hello");
				   	}
						public void foo(float a) {
				   		System.out.println("Hello");
				   	}
				   	public void foo(double a) {
				   		System.out.println("Hello");
				   	}
				   	public void foo(char a) {
				   		System.out.println("Hello");
				   	}
				   }
				   public class C extends B {
						public void foo(int a) {
							System.out.println("Hello");
						}
						public void foo(double a) {
							System.out.println("Hello");
						}
						public void foo(boolean a) {
							System.out.println("Hello");
						}
						public void foo(byte a) {
							System.out.println("Hello");
						}
				   }
				}
				"""
		};
	runner.expectedCompilerLog = isMinimumCompliant(ClassFileConstants.JDK11) ?
		"""
			----------
			1. WARNING in X.java (at line 3)
				public void foo(int a) {
				            ^^^^^^^^^^
			The method foo(int) from the type X.A is never used locally
			----------
			2. WARNING in X.java (at line 6)
				public void foo(float a) {
				            ^^^^^^^^^^^^
			The method foo(float) from the type X.A is never used locally
			----------
			3. WARNING in X.java (at line 9)
				public void foo(boolean a) {
				            ^^^^^^^^^^^^^^
			The method foo(boolean) from the type X.A is never used locally
			----------
			4. WARNING in X.java (at line 23)
				public void foo(double a) {
				            ^^^^^^^^^^^^^
			The method foo(double) from the type X.B is never used locally
			----------
			"""
		:
		"""
			----------
			1. WARNING in X.java (at line 3)
				public void foo(int a) {
				            ^^^^^^^^^^
			The method foo(int) from the type X.A is never used locally
			----------
			2. WARNING in X.java (at line 6)
				public void foo(float a) {
				            ^^^^^^^^^^^^
			The method foo(float) from the type X.A is never used locally
			----------
			3. WARNING in X.java (at line 9)
				public void foo(boolean a) {
				            ^^^^^^^^^^^^^^
			The method foo(boolean) from the type X.A is never used locally
			----------
			4. WARNING in X.java (at line 16)
				private class B extends A {
				              ^
			Access to enclosing constructor X.A() is emulated by a synthetic accessor method
			----------
			5. WARNING in X.java (at line 23)
				public void foo(double a) {
				            ^^^^^^^^^^^^^
			The method foo(double) from the type X.B is never used locally
			----------
			6. WARNING in X.java (at line 30)
				public class C extends B {
				             ^
			Access to enclosing constructor X.B() is emulated by a synthetic accessor method
			----------
			""";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings;
	runner.runWarningTest();
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=296660
// check independence of textual order
public void test099a() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"X.java",
			"""
				public class X {
				   public class C extends B {
						public void foo(int a) {
							System.out.println("Hello");
						}
						public void foo(double a) {
							System.out.println("Hello");
						}
						public void foo(boolean a) {
							System.out.println("Hello");
						}
						public void foo(byte a) {
							System.out.println("Hello");
						}
				   }
				   private class B extends A {
						public void foo(int a) {
				   		System.out.println("Hello");
				   	}
						public void foo(float a) {
				   		System.out.println("Hello");
				   	}
				   	public void foo(double a) {
				   		System.out.println("Hello");
				   	}
				   	public void foo(char a) {
				   		System.out.println("Hello");
				   	}
				   }
				   private class A {
				    	public void foo(int a) {
				   		System.out.println("Hello");
				    	}
					    public void foo(float a) {
				   		System.out.println("Hello");
				   	}
				   	public void foo(boolean a) {
				   		System.out.println("Hello");
				   	}
				      	public void foo(Integer a) {
				   		System.out.println("Hello");
				   	}
				   }
				}
				"""
		};
	runner.expectedCompilerLog = isMinimumCompliant(ClassFileConstants.JDK11) ?
		"""
			----------
			1. WARNING in X.java (at line 23)
				public void foo(double a) {
				            ^^^^^^^^^^^^^
			The method foo(double) from the type X.B is never used locally
			----------
			2. WARNING in X.java (at line 31)
				public void foo(int a) {
				            ^^^^^^^^^^
			The method foo(int) from the type X.A is never used locally
			----------
			3. WARNING in X.java (at line 34)
				public void foo(float a) {
				            ^^^^^^^^^^^^
			The method foo(float) from the type X.A is never used locally
			----------
			4. WARNING in X.java (at line 37)
				public void foo(boolean a) {
				            ^^^^^^^^^^^^^^
			The method foo(boolean) from the type X.A is never used locally
			----------
			"""
		:
		"""
			----------
			1. WARNING in X.java (at line 2)
				public class C extends B {
				             ^
			Access to enclosing constructor X.B() is emulated by a synthetic accessor method
			----------
			2. WARNING in X.java (at line 16)
				private class B extends A {
				              ^
			Access to enclosing constructor X.A() is emulated by a synthetic accessor method
			----------
			3. WARNING in X.java (at line 23)
				public void foo(double a) {
				            ^^^^^^^^^^^^^
			The method foo(double) from the type X.B is never used locally
			----------
			4. WARNING in X.java (at line 31)
				public void foo(int a) {
				            ^^^^^^^^^^
			The method foo(int) from the type X.A is never used locally
			----------
			5. WARNING in X.java (at line 34)
				public void foo(float a) {
				            ^^^^^^^^^^^^
			The method foo(float) from the type X.A is never used locally
			----------
			6. WARNING in X.java (at line 37)
				public void foo(boolean a) {
				            ^^^^^^^^^^^^^^
			The method foo(boolean) from the type X.A is never used locally
			----------
			""";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings;
	runner.runWarningTest();
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=296660
// check usage via super-call
public void test099b() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"X.java",
			"""
				public class X {
				    private class A {
				    	public void foo(int a) {
				   		System.out.println("Hello");
				    	}
					    public void foo(float a) {
				   		System.out.println("Hello");
				   	}
				   	public void foo(boolean a) {
				   		System.out.println("Hello");
				   	}
				      	public void foo(Integer a) {
				   		System.out.println("Hello");
				   	}
				   }
				   private class B extends A {
						public void foo(int a) {
				   		super.foo(a);
				   	}
						public void foo(float a) {
				   		super.foo(a);
				   	}
				   	public void foo(double a) {
				   		System.out.println("Hello");
				   	}
				   	public void foo(char a) {
				   		System.out.println("Hello");
				   	}
				   }
				   public class C extends B {
						public void foo(int a) {
							System.out.println("Hello");
						}
						public void foo(double a) {
							super.foo(a);
						}
						public void foo(boolean a) {
							super.foo(a);
						}
						public void foo(byte a) {
							System.out.println("Hello");
						}
				   }
				}
				"""
		};
	if (!isMinimumCompliant(ClassFileConstants.JDK11)) {
		runner.javacTestOptions = JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings;
		runner.expectedCompilerLog =
			"""
				----------
				1. WARNING in X.java (at line 16)
					private class B extends A {
					              ^
				Access to enclosing constructor X.A() is emulated by a synthetic accessor method
				----------
				2. WARNING in X.java (at line 30)
					public class C extends B {
					             ^
				Access to enclosing constructor X.B() is emulated by a synthetic accessor method
				----------
				""";
		runner.runWarningTest();
	} else {
		runner.runConformTest();
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=296660
public void test100() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"X.java",
			"""
				public class X {
				    private class A {
				        public void foo() {}
				    }
				    public class B extends A {}
				}"""
		};
	if (!isMinimumCompliant(ClassFileConstants.JDK11)) {

		runner.expectedCompilerLog =
				"""
					----------
					1. WARNING in X.java (at line 5)
						public class B extends A {}
						             ^
					Access to enclosing constructor X.A() is emulated by a synthetic accessor method
					----------
					""";
		runner.javacTestOptions = JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings;
		runner.runWarningTest();
	} else {
		runner.runConformTest();
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=296660
public void test101() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"X.java",
			"""
				public class X {
				    private class A {
				        public void foo() {}
				        public void foo(int a) {}
				    }
				    public class B extends A {}
				}"""
		};
	if (!isMinimumCompliant(ClassFileConstants.JDK11)) {
		runner.expectedCompilerLog =
				"""
					----------
					1. WARNING in X.java (at line 6)
						public class B extends A {}
						             ^
					Access to enclosing constructor X.A() is emulated by a synthetic accessor method
					----------
					""";
		runner.javacTestOptions = JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings;
		runner.runWarningTest();
	} else {
		runner.runConformTest();
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=296660
public void test102() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"X.java",
			"""
				public class X {
				    private class A {
				        private void foo() {}
				        private void foo(int a) {}
				    }
				    public class B extends A {}
				}"""
		};
	runner.expectedCompilerLog = isMinimumCompliant(ClassFileConstants.JDK11) ?
		"""
			----------
			1. WARNING in X.java (at line 3)
				private void foo() {}
				             ^^^^^
			The method foo() from the type X.A is never used locally
			----------
			2. WARNING in X.java (at line 4)
				private void foo(int a) {}
				             ^^^^^^^^^^
			The method foo(int) from the type X.A is never used locally
			----------
			"""
		:
		"""
			----------
			1. WARNING in X.java (at line 3)
				private void foo() {}
				             ^^^^^
			The method foo() from the type X.A is never used locally
			----------
			2. WARNING in X.java (at line 4)
				private void foo(int a) {}
				             ^^^^^^^^^^
			The method foo(int) from the type X.A is never used locally
			----------
			3. WARNING in X.java (at line 6)
				public class B extends A {}
				             ^
			Access to enclosing constructor X.A() is emulated by a synthetic accessor method
			----------
			""";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings;
	runner.runWarningTest();
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=296660
public void test103() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"X.java",
			"""
				public class X {
				    private class A {
				        public void foo() {}
				        public void foo(int a) {}
				    }
				    private class B extends A {}
				}"""
		};
	runner.expectedCompilerLog = isMinimumCompliant(ClassFileConstants.JDK11) ?
		"""
			----------
			1. WARNING in X.java (at line 3)
				public void foo() {}
				            ^^^^^
			The method foo() from the type X.A is never used locally
			----------
			2. WARNING in X.java (at line 4)
				public void foo(int a) {}
				            ^^^^^^^^^^
			The method foo(int) from the type X.A is never used locally
			----------
			3. WARNING in X.java (at line 6)
				private class B extends A {}
				              ^
			The type X.B is never used locally
			----------
			"""
		:
		"""
			----------
			1. WARNING in X.java (at line 3)
				public void foo() {}
				            ^^^^^
			The method foo() from the type X.A is never used locally
			----------
			2. WARNING in X.java (at line 4)
				public void foo(int a) {}
				            ^^^^^^^^^^
			The method foo(int) from the type X.A is never used locally
			----------
			3. WARNING in X.java (at line 6)
				private class B extends A {}
				              ^
			The type X.B is never used locally
			----------
			4. WARNING in X.java (at line 6)
				private class B extends A {}
				              ^
			Access to enclosing constructor X.A() is emulated by a synthetic accessor method
			----------
			""";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings;
	runner.runWarningTest();
}
public void test104() {
	this.runNegativeTest(
		new String[] {
			"p/Bar.java", //-----------------------------------------------------------------------
			"""
				package p;
				import q.Zork;
				public abstract class Bar {
					protected abstract boolean isBaz();
				}
				""",
		},
		"""
			----------
			1. ERROR in p\\Bar.java (at line 2)
				import q.Zork;
				       ^
			The import q cannot be resolved
			----------
			""",
		null /* no extra class libraries */,
		true /* flush output directory */,
		null /* no custom options */,
		true /* do not generate output */,
		false /* do not show category */,
		false /* do not show warning token */,
		false  /* do not skip javac for this peculiar test */,
		false  /* do not perform statements recovery */);
	Runner runner = new Runner();
	runner.shouldFlushOutputDirectory =
		false;
	runner.testFiles =
		new String[] {
			"X.java", //-----------------------------------------------------------------------
			"""
				import p.Bar;
				public class X extends Bar {
					protected boolean isBaz() {
						return false;
					}
				}""",
		};
	runner.expectedOutputString =
		"";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.JavacHasErrorsEclipseHasNone; // ecj can create .class from erroneous .java
	runner.runConformTest();
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=243917
public void test105() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    static {
				        System.loadLibrary("tpbrooktrout");
				    }
				    private final int time;
				    private int foo() { return 0;}
				    private class Inner {}
				    public X(int delay) {
				        time = delay;
				    }
				    public native void run(Inner i);
				}
				"""
		},
		"");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=245007
public void test106() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						new Listener() {
							  void foo(int a) { }
						}.bar();
				       new Listener() {
							  void foo(int a) { }
						}.field = 10;
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				new Listener() {
				    ^^^^^^^^
			Listener cannot be resolved to a type
			----------
			2. ERROR in X.java (at line 6)
				new Listener() {
				    ^^^^^^^^
			Listener cannot be resolved to a type
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=319425
public void test107() {
	this.runNegativeTest(
		new String[] {
			"p/OuterBogus.java", //-----------------------------------------------------------------------
			"""
				package p;
				abstract final class OuterBogus {
					public static void call() {
						System.out.println("Hi. I'm outer bogus.");
					}
				}""",
		},
		"""
			----------
			1. ERROR in p\\OuterBogus.java (at line 2)
				abstract final class OuterBogus {
				                     ^^^^^^^^^^
			The class OuterBogus can be either abstract or final, not both
			----------
			""",
		null /* no extra class libraries */,
		true /* flush output directory */,
		null /* no custom options */,
		true /* do not generate output */,
		false /* do not show category */,
		false /* do not show warning token */,
		false  /* do not skip javac for this peculiar test */,
		false  /* do not perform statements recovery */);
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"p/Bogus.java", //-----------------------------------------------------------------------
			"""
				package p;
				
				public class Bogus {
					public static void main(String[] args) {
						try {
							OuterBogus.call();
						} catch(ClassFormatError e) {
							System.out.println("Wrong error found");
						} catch(Error e) {
							System.out.println("Compilation error found");
						}
					}
				}""",
		};
	runner.expectedOutputString =
		"Compilation error found";
	runner.shouldFlushOutputDirectory =
		false;
	runner.javacTestOptions =
		JavacTestOptions.Excuse.JavacHasErrorsEclipseHasNone; // ecj can create .class from erroneous .java
	runner.runConformTest();
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=321414
public void test108() {
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) return;
	String errMessage = isMinimumCompliant(ClassFileConstants.JDK11) ?
			"""
				----------
				1. WARNING in SyntheticConstructorTooManyArgs.java (at line 23)
					@SuppressWarnings("synthetic-access")
					                  ^^^^^^^^^^^^^^^^^^
				Unnecessary @SuppressWarnings("synthetic-access")
				----------
				"""
			:
			"""
				----------
				1. ERROR in SyntheticConstructorTooManyArgs.java (at line 4)
					private A(
							/*this,*/int p01, int p02, int p03, int p04, int p05, int p06, int p07, int p08, int p09, int p0a, int p0b, int p0c, int p0d, int p0e, int p0f,\s
							int p10, int p11, int p12, int p13, int p14, int p15, int p16, int p17, int p18, int p19, int p1a, int p1b, int p1c, int p1d, int p1e, int p1f,\s
							int p20, int p21, int p22, int p23, int p24, int p25, int p26, int p27, int p28, int p29, int p2a, int p2b, int p2c, int p2d, int p2e, int p2f,\s
							int p30, int p31, int p32, int p33, int p34, int p35, int p36, int p37, int p38, int p39, int p3a, int p3b, int p3c, int p3d, int p3e, int p3f,\s
							int p40, int p41, int p42, int p43, int p44, int p45, int p46, int p47, int p48, int p49, int p4a, int p4b, int p4c, int p4d, int p4e, int p4f,\s
							int p50, int p51, int p52, int p53, int p54, int p55, int p56, int p57, int p58, int p59, int p5a, int p5b, int p5c, int p5d, int p5e, int p5f,\s
							int p60, int p61, int p62, int p63, int p64, int p65, int p66, int p67, int p68, int p69, int p6a, int p6b, int p6c, int p6d, int p6e, int p6f,\s
							int p70, int p71, int p72, int p73, int p74, int p75, int p76, int p77, int p78, int p79, int p7a, int p7b, int p7c, int p7d, int p7e, int p7f,\s
							int p80, int p81, int p82, int p83, int p84, int p85, int p86, int p87, int p88, int p89, int p8a, int p8b, int p8c, int p8d, int p8e, int p8f,\s
							int p90, int p91, int p92, int p93, int p94, int p95, int p96, int p97, int p98, int p99, int p9a, int p9b, int p9c, int p9d, int p9e, int p9f,\s
							int pa0, int pa1, int pa2, int pa3, int pa4, int pa5, int pa6, int pa7, int pa8, int pa9, int paa, int pab, int pac, int pad, int pae, int paf,\s
							int pb0, int pb1, int pb2, int pb3, int pb4, int pb5, int pb6, int pb7, int pb8, int pb9, int pba, int pbb, int pbc, int pbd, int pbe, int pbf,\s
							int pc0, int pc1, int pc2, int pc3, int pc4, int pc5, int pc6, int pc7, int pc8, int pc9, int pca, int pcb, int pcc, int pcd, int pce, int pcf,\s
							int pd0, int pd1, int pd2, int pd3, int pd4, int pd5, int pd6, int pd7, int pd8, int pd9, int pda, int pdb, int pdc, int pdd, int pde, int pdf,\s
							int pe0, int pe1, int pe2, int pe3, int pe4, int pe5, int pe6, int pe7, int pe8, int pe9, int pea, int peb, int pec, int ped, int pee, int pef,\s
							int pf0, int pf1, int pf2, int pf3, int pf4, int pf5, int pf6, int pf7, int pf8, int pf9, int pfa, int pfb, int pfc, int pfd, int pfe
							) {}
					        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				The synthetic method created to access A(int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int) of type SyntheticConstructorTooManyArgs.A has too many parameters
				----------
				""";
	this.runNegativeTest(
		new String[] {
			"SyntheticConstructorTooManyArgs.java", //-----------------------------------------------------------------------
			"""
				public class SyntheticConstructorTooManyArgs {
				
					static class A {
						private A(
							/*this,*/int p01, int p02, int p03, int p04, int p05, int p06, int p07, int p08, int p09, int p0a, int p0b, int p0c, int p0d, int p0e, int p0f,\s
							int p10, int p11, int p12, int p13, int p14, int p15, int p16, int p17, int p18, int p19, int p1a, int p1b, int p1c, int p1d, int p1e, int p1f,\s
							int p20, int p21, int p22, int p23, int p24, int p25, int p26, int p27, int p28, int p29, int p2a, int p2b, int p2c, int p2d, int p2e, int p2f,\s
							int p30, int p31, int p32, int p33, int p34, int p35, int p36, int p37, int p38, int p39, int p3a, int p3b, int p3c, int p3d, int p3e, int p3f,\s
							int p40, int p41, int p42, int p43, int p44, int p45, int p46, int p47, int p48, int p49, int p4a, int p4b, int p4c, int p4d, int p4e, int p4f,\s
							int p50, int p51, int p52, int p53, int p54, int p55, int p56, int p57, int p58, int p59, int p5a, int p5b, int p5c, int p5d, int p5e, int p5f,\s
							int p60, int p61, int p62, int p63, int p64, int p65, int p66, int p67, int p68, int p69, int p6a, int p6b, int p6c, int p6d, int p6e, int p6f,\s
							int p70, int p71, int p72, int p73, int p74, int p75, int p76, int p77, int p78, int p79, int p7a, int p7b, int p7c, int p7d, int p7e, int p7f,\s
							int p80, int p81, int p82, int p83, int p84, int p85, int p86, int p87, int p88, int p89, int p8a, int p8b, int p8c, int p8d, int p8e, int p8f,\s
							int p90, int p91, int p92, int p93, int p94, int p95, int p96, int p97, int p98, int p99, int p9a, int p9b, int p9c, int p9d, int p9e, int p9f,\s
							int pa0, int pa1, int pa2, int pa3, int pa4, int pa5, int pa6, int pa7, int pa8, int pa9, int paa, int pab, int pac, int pad, int pae, int paf,\s
							int pb0, int pb1, int pb2, int pb3, int pb4, int pb5, int pb6, int pb7, int pb8, int pb9, int pba, int pbb, int pbc, int pbd, int pbe, int pbf,\s
							int pc0, int pc1, int pc2, int pc3, int pc4, int pc5, int pc6, int pc7, int pc8, int pc9, int pca, int pcb, int pcc, int pcd, int pce, int pcf,\s
							int pd0, int pd1, int pd2, int pd3, int pd4, int pd5, int pd6, int pd7, int pd8, int pd9, int pda, int pdb, int pdc, int pdd, int pde, int pdf,\s
							int pe0, int pe1, int pe2, int pe3, int pe4, int pe5, int pe6, int pe7, int pe8, int pe9, int pea, int peb, int pec, int ped, int pee, int pef,\s
							int pf0, int pf1, int pf2, int pf3, int pf4, int pf5, int pf6, int pf7, int pf8, int pf9, int pfa, int pfb, int pfc, int pfd, int pfe
							) {}
					}
					@SuppressWarnings("synthetic-access")
					A a = new A(
						  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
						);
					public static void main(String[] args) {
						StringBuilder params = new StringBuilder();
						params.append("/*this,*/");
						for (int p = 1; p < 255; p++) {
							if (p > 1) {
								params.append(", ");
								if (p % 16 == 0)
									params.append('\\n');
							}
							params.append("int p"
									+ Character.forDigit(p / 16, 16)
									+ Character.forDigit(p % 16, 16)
									);
						}
						System.out.println(params);
						A.class.getName(); // ClassFormatError
					}
				}""",
		},
		errMessage);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=321414
public void test109() {
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) return;
	this.runConformTest(
		new String[] {
			"SyntheticConstructorTooManyArgs.java", //-----------------------------------------------------------------------
			"""
				public class SyntheticConstructorTooManyArgs {
				
					static class A {
						private A foo(
							/*this,*/int p01, int p02, int p03, int p04, int p05, int p06, int p07, int p08, int p09, int p0a, int p0b, int p0c, int p0d, int p0e, int p0f,\s
							int p10, int p11, int p12, int p13, int p14, int p15, int p16, int p17, int p18, int p19, int p1a, int p1b, int p1c, int p1d, int p1e, int p1f,\s
							int p20, int p21, int p22, int p23, int p24, int p25, int p26, int p27, int p28, int p29, int p2a, int p2b, int p2c, int p2d, int p2e, int p2f,\s
							int p30, int p31, int p32, int p33, int p34, int p35, int p36, int p37, int p38, int p39, int p3a, int p3b, int p3c, int p3d, int p3e, int p3f,\s
							int p40, int p41, int p42, int p43, int p44, int p45, int p46, int p47, int p48, int p49, int p4a, int p4b, int p4c, int p4d, int p4e, int p4f,\s
							int p50, int p51, int p52, int p53, int p54, int p55, int p56, int p57, int p58, int p59, int p5a, int p5b, int p5c, int p5d, int p5e, int p5f,\s
							int p60, int p61, int p62, int p63, int p64, int p65, int p66, int p67, int p68, int p69, int p6a, int p6b, int p6c, int p6d, int p6e, int p6f,\s
							int p70, int p71, int p72, int p73, int p74, int p75, int p76, int p77, int p78, int p79, int p7a, int p7b, int p7c, int p7d, int p7e, int p7f,\s
							int p80, int p81, int p82, int p83, int p84, int p85, int p86, int p87, int p88, int p89, int p8a, int p8b, int p8c, int p8d, int p8e, int p8f,\s
							int p90, int p91, int p92, int p93, int p94, int p95, int p96, int p97, int p98, int p99, int p9a, int p9b, int p9c, int p9d, int p9e, int p9f,\s
							int pa0, int pa1, int pa2, int pa3, int pa4, int pa5, int pa6, int pa7, int pa8, int pa9, int paa, int pab, int pac, int pad, int pae, int paf,\s
							int pb0, int pb1, int pb2, int pb3, int pb4, int pb5, int pb6, int pb7, int pb8, int pb9, int pba, int pbb, int pbc, int pbd, int pbe, int pbf,\s
							int pc0, int pc1, int pc2, int pc3, int pc4, int pc5, int pc6, int pc7, int pc8, int pc9, int pca, int pcb, int pcc, int pcd, int pce, int pcf,\s
							int pd0, int pd1, int pd2, int pd3, int pd4, int pd5, int pd6, int pd7, int pd8, int pd9, int pda, int pdb, int pdc, int pdd, int pde, int pdf,\s
							int pe0, int pe1, int pe2, int pe3, int pe4, int pe5, int pe6, int pe7, int pe8, int pe9, int pea, int peb, int pec, int ped, int pee, int pef,\s
							int pf0, int pf1, int pf2, int pf3, int pf4, int pf5, int pf6, int pf7, int pf8, int pf9, int pfa, int pfb, int pfc, int pfd, int pfe
							) { return new A();}
					}
					@SuppressWarnings("synthetic-access")
					A a = new A().foo(
						  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
						);
					public static void main(String[] args) {
						StringBuilder params = new StringBuilder();
						params.append("/*this,*/");
						for (int p = 1; p < 255; p++) {
							if (p > 1) {
								params.append(", ");
								if (p % 16 == 0)
									params.append('\\n');
							}
							params.append("int p"
									+ Character.forDigit(p / 16, 16)
									+ Character.forDigit(p % 16, 16)
									);
						}
						System.out.println(params);
						A.class.getName(); // ClassFormatError
					}
				}""",
		},
		"""
			/*this,*/int p01, int p02, int p03, int p04, int p05, int p06, int p07, int p08, int p09, int p0a, int p0b, int p0c, int p0d, int p0e, int p0f,\s
			int p10, int p11, int p12, int p13, int p14, int p15, int p16, int p17, int p18, int p19, int p1a, int p1b, int p1c, int p1d, int p1e, int p1f,\s
			int p20, int p21, int p22, int p23, int p24, int p25, int p26, int p27, int p28, int p29, int p2a, int p2b, int p2c, int p2d, int p2e, int p2f,\s
			int p30, int p31, int p32, int p33, int p34, int p35, int p36, int p37, int p38, int p39, int p3a, int p3b, int p3c, int p3d, int p3e, int p3f,\s
			int p40, int p41, int p42, int p43, int p44, int p45, int p46, int p47, int p48, int p49, int p4a, int p4b, int p4c, int p4d, int p4e, int p4f,\s
			int p50, int p51, int p52, int p53, int p54, int p55, int p56, int p57, int p58, int p59, int p5a, int p5b, int p5c, int p5d, int p5e, int p5f,\s
			int p60, int p61, int p62, int p63, int p64, int p65, int p66, int p67, int p68, int p69, int p6a, int p6b, int p6c, int p6d, int p6e, int p6f,\s
			int p70, int p71, int p72, int p73, int p74, int p75, int p76, int p77, int p78, int p79, int p7a, int p7b, int p7c, int p7d, int p7e, int p7f,\s
			int p80, int p81, int p82, int p83, int p84, int p85, int p86, int p87, int p88, int p89, int p8a, int p8b, int p8c, int p8d, int p8e, int p8f,\s
			int p90, int p91, int p92, int p93, int p94, int p95, int p96, int p97, int p98, int p99, int p9a, int p9b, int p9c, int p9d, int p9e, int p9f,\s
			int pa0, int pa1, int pa2, int pa3, int pa4, int pa5, int pa6, int pa7, int pa8, int pa9, int paa, int pab, int pac, int pad, int pae, int paf,\s
			int pb0, int pb1, int pb2, int pb3, int pb4, int pb5, int pb6, int pb7, int pb8, int pb9, int pba, int pbb, int pbc, int pbd, int pbe, int pbf,\s
			int pc0, int pc1, int pc2, int pc3, int pc4, int pc5, int pc6, int pc7, int pc8, int pc9, int pca, int pcb, int pcc, int pcd, int pce, int pcf,\s
			int pd0, int pd1, int pd2, int pd3, int pd4, int pd5, int pd6, int pd7, int pd8, int pd9, int pda, int pdb, int pdc, int pdd, int pde, int pdf,\s
			int pe0, int pe1, int pe2, int pe3, int pe4, int pe5, int pe6, int pe7, int pe8, int pe9, int pea, int peb, int pec, int ped, int pee, int pef,\s
			int pf0, int pf1, int pf2, int pf3, int pf4, int pf5, int pf6, int pf7, int pf8, int pf9, int pfa, int pfb, int pfc, int pfd, int pfe""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=321414
public void test110() {
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) return;
	this.runConformTest(
		new String[] {
			"SyntheticConstructorTooManyArgs.java", //-----------------------------------------------------------------------
			"""
				public class SyntheticConstructorTooManyArgs {
				
					static class A {
						private static A foo(
							/*this,*/int p01, int p02, int p03, int p04, int p05, int p06, int p07, int p08, int p09, int p0a, int p0b, int p0c, int p0d, int p0e, int p0f,\s
							int p10, int p11, int p12, int p13, int p14, int p15, int p16, int p17, int p18, int p19, int p1a, int p1b, int p1c, int p1d, int p1e, int p1f,\s
							int p20, int p21, int p22, int p23, int p24, int p25, int p26, int p27, int p28, int p29, int p2a, int p2b, int p2c, int p2d, int p2e, int p2f,\s
							int p30, int p31, int p32, int p33, int p34, int p35, int p36, int p37, int p38, int p39, int p3a, int p3b, int p3c, int p3d, int p3e, int p3f,\s
							int p40, int p41, int p42, int p43, int p44, int p45, int p46, int p47, int p48, int p49, int p4a, int p4b, int p4c, int p4d, int p4e, int p4f,\s
							int p50, int p51, int p52, int p53, int p54, int p55, int p56, int p57, int p58, int p59, int p5a, int p5b, int p5c, int p5d, int p5e, int p5f,\s
							int p60, int p61, int p62, int p63, int p64, int p65, int p66, int p67, int p68, int p69, int p6a, int p6b, int p6c, int p6d, int p6e, int p6f,\s
							int p70, int p71, int p72, int p73, int p74, int p75, int p76, int p77, int p78, int p79, int p7a, int p7b, int p7c, int p7d, int p7e, int p7f,\s
							int p80, int p81, int p82, int p83, int p84, int p85, int p86, int p87, int p88, int p89, int p8a, int p8b, int p8c, int p8d, int p8e, int p8f,\s
							int p90, int p91, int p92, int p93, int p94, int p95, int p96, int p97, int p98, int p99, int p9a, int p9b, int p9c, int p9d, int p9e, int p9f,\s
							int pa0, int pa1, int pa2, int pa3, int pa4, int pa5, int pa6, int pa7, int pa8, int pa9, int paa, int pab, int pac, int pad, int pae, int paf,\s
							int pb0, int pb1, int pb2, int pb3, int pb4, int pb5, int pb6, int pb7, int pb8, int pb9, int pba, int pbb, int pbc, int pbd, int pbe, int pbf,\s
							int pc0, int pc1, int pc2, int pc3, int pc4, int pc5, int pc6, int pc7, int pc8, int pc9, int pca, int pcb, int pcc, int pcd, int pce, int pcf,\s
							int pd0, int pd1, int pd2, int pd3, int pd4, int pd5, int pd6, int pd7, int pd8, int pd9, int pda, int pdb, int pdc, int pdd, int pde, int pdf,\s
							int pe0, int pe1, int pe2, int pe3, int pe4, int pe5, int pe6, int pe7, int pe8, int pe9, int pea, int peb, int pec, int ped, int pee, int pef,\s
							int pf0, int pf1, int pf2, int pf3, int pf4, int pf5, int pf6, int pf7, int pf8, int pf9, int pfa, int pfb, int pfc, int pfd, int pfe
							) { return new A();}
					}
					@SuppressWarnings("synthetic-access")
					A a = A.foo(
						  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
						);
					public static void main(String[] args) {
						StringBuilder params = new StringBuilder();
						params.append("/*this,*/");
						for (int p = 1; p < 255; p++) {
							if (p > 1) {
								params.append(", ");
								if (p % 16 == 0)
									params.append('\\n');
							}
							params.append("int p"
									+ Character.forDigit(p / 16, 16)
									+ Character.forDigit(p % 16, 16)
									);
						}
						System.out.println(params);
						A.class.getName(); // ClassFormatError
					}
				}""",
		},
		"""
			/*this,*/int p01, int p02, int p03, int p04, int p05, int p06, int p07, int p08, int p09, int p0a, int p0b, int p0c, int p0d, int p0e, int p0f,\s
			int p10, int p11, int p12, int p13, int p14, int p15, int p16, int p17, int p18, int p19, int p1a, int p1b, int p1c, int p1d, int p1e, int p1f,\s
			int p20, int p21, int p22, int p23, int p24, int p25, int p26, int p27, int p28, int p29, int p2a, int p2b, int p2c, int p2d, int p2e, int p2f,\s
			int p30, int p31, int p32, int p33, int p34, int p35, int p36, int p37, int p38, int p39, int p3a, int p3b, int p3c, int p3d, int p3e, int p3f,\s
			int p40, int p41, int p42, int p43, int p44, int p45, int p46, int p47, int p48, int p49, int p4a, int p4b, int p4c, int p4d, int p4e, int p4f,\s
			int p50, int p51, int p52, int p53, int p54, int p55, int p56, int p57, int p58, int p59, int p5a, int p5b, int p5c, int p5d, int p5e, int p5f,\s
			int p60, int p61, int p62, int p63, int p64, int p65, int p66, int p67, int p68, int p69, int p6a, int p6b, int p6c, int p6d, int p6e, int p6f,\s
			int p70, int p71, int p72, int p73, int p74, int p75, int p76, int p77, int p78, int p79, int p7a, int p7b, int p7c, int p7d, int p7e, int p7f,\s
			int p80, int p81, int p82, int p83, int p84, int p85, int p86, int p87, int p88, int p89, int p8a, int p8b, int p8c, int p8d, int p8e, int p8f,\s
			int p90, int p91, int p92, int p93, int p94, int p95, int p96, int p97, int p98, int p99, int p9a, int p9b, int p9c, int p9d, int p9e, int p9f,\s
			int pa0, int pa1, int pa2, int pa3, int pa4, int pa5, int pa6, int pa7, int pa8, int pa9, int paa, int pab, int pac, int pad, int pae, int paf,\s
			int pb0, int pb1, int pb2, int pb3, int pb4, int pb5, int pb6, int pb7, int pb8, int pb9, int pba, int pbb, int pbc, int pbd, int pbe, int pbf,\s
			int pc0, int pc1, int pc2, int pc3, int pc4, int pc5, int pc6, int pc7, int pc8, int pc9, int pca, int pcb, int pcc, int pcd, int pce, int pcf,\s
			int pd0, int pd1, int pd2, int pd3, int pd4, int pd5, int pd6, int pd7, int pd8, int pd9, int pda, int pdb, int pdc, int pdd, int pde, int pdf,\s
			int pe0, int pe1, int pe2, int pe3, int pe4, int pe5, int pe6, int pe7, int pe8, int pe9, int pea, int peb, int pec, int ped, int pee, int pef,\s
			int pf0, int pf1, int pf2, int pf3, int pf4, int pf5, int pf6, int pf7, int pf8, int pf9, int pfa, int pfb, int pfc, int pfd, int pfe""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=321414
public void test111() {
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) return;
	String errMessage = isMinimumCompliant(ClassFileConstants.JDK11) ?
			"""
				----------
				1. WARNING in SyntheticConstructorTooManyArgs.java (at line 23)
					@SuppressWarnings("synthetic-access")
					                  ^^^^^^^^^^^^^^^^^^
				Unnecessary @SuppressWarnings("synthetic-access")
				----------
				"""
			:
			"""
				----------
				1. ERROR in SyntheticConstructorTooManyArgs.java (at line 4)
					private A(
							/*this,*/int p01, int p02, int p03, int p04, int p05, int p06, int p07, int p08, int p09, int p0a, int p0b, int p0c, int p0d, int p0e, int p0f,\s
							int p10, int p11, int p12, int p13, int p14, int p15, int p16, int p17, int p18, int p19, int p1a, int p1b, int p1c, int p1d, int p1e, int p1f,\s
							int p20, int p21, int p22, int p23, int p24, int p25, int p26, int p27, int p28, int p29, int p2a, int p2b, int p2c, int p2d, int p2e, int p2f,\s
							int p30, int p31, int p32, int p33, int p34, int p35, int p36, int p37, int p38, int p39, int p3a, int p3b, int p3c, int p3d, int p3e, int p3f,\s
							int p40, int p41, int p42, int p43, int p44, int p45, int p46, int p47, int p48, int p49, int p4a, int p4b, int p4c, int p4d, int p4e, int p4f,\s
							int p50, int p51, int p52, int p53, int p54, int p55, int p56, int p57, int p58, int p59, int p5a, int p5b, int p5c, int p5d, int p5e, int p5f,\s
							int p60, int p61, int p62, int p63, int p64, int p65, int p66, int p67, int p68, int p69, int p6a, int p6b, int p6c, int p6d, int p6e, int p6f,\s
							int p70, int p71, int p72, int p73, int p74, int p75, int p76, int p77, int p78, int p79, int p7a, int p7b, int p7c, int p7d, int p7e, int p7f,\s
							int p80, int p81, int p82, int p83, int p84, int p85, int p86, int p87, int p88, int p89, int p8a, int p8b, int p8c, int p8d, int p8e, int p8f,\s
							int p90, int p91, int p92, int p93, int p94, int p95, int p96, int p97, int p98, int p99, int p9a, int p9b, int p9c, int p9d, int p9e, int p9f,\s
							int pa0, int pa1, int pa2, int pa3, int pa4, int pa5, int pa6, int pa7, int pa8, int pa9, int paa, int pab, int pac, int pad, int pae, int paf,\s
							int pb0, int pb1, int pb2, int pb3, int pb4, int pb5, int pb6, int pb7, int pb8, int pb9, int pba, int pbb, int pbc, int pbd, int pbe, int pbf,\s
							int pc0, int pc1, int pc2, int pc3, int pc4, int pc5, int pc6, int pc7, int pc8, int pc9, int pca, int pcb, int pcc, int pcd, int pce, int pcf,\s
							int pd0, int pd1, int pd2, int pd3, int pd4, int pd5, int pd6, int pd7, int pd8, int pd9, int pda, int pdb, int pdc, int pdd, int pde, int pdf,\s
							int pe0, int pe1, int pe2, int pe3, int pe4, int pe5, int pe6, int pe7, int pe8, int pe9, int pea, int peb, int pec, int ped, int pee, int pef,\s
							int pf0, int pf1, int pf2, int pf3, int pf4, int pf5, int pf6, int pf7, int pf8, int pf9, int pfa, int pfb, int pfc, int pfd
							) {}
					        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				The synthetic method created to access A(int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int, int) of type SyntheticConstructorTooManyArgs.A has too many parameters
				----------
				""";
	this.runNegativeTest(
		new String[] {
			"SyntheticConstructorTooManyArgs.java", //-----------------------------------------------------------------------
			"""
				public class SyntheticConstructorTooManyArgs {
				
					class A {
						private A(
							/*this,*/int p01, int p02, int p03, int p04, int p05, int p06, int p07, int p08, int p09, int p0a, int p0b, int p0c, int p0d, int p0e, int p0f,\s
							int p10, int p11, int p12, int p13, int p14, int p15, int p16, int p17, int p18, int p19, int p1a, int p1b, int p1c, int p1d, int p1e, int p1f,\s
							int p20, int p21, int p22, int p23, int p24, int p25, int p26, int p27, int p28, int p29, int p2a, int p2b, int p2c, int p2d, int p2e, int p2f,\s
							int p30, int p31, int p32, int p33, int p34, int p35, int p36, int p37, int p38, int p39, int p3a, int p3b, int p3c, int p3d, int p3e, int p3f,\s
							int p40, int p41, int p42, int p43, int p44, int p45, int p46, int p47, int p48, int p49, int p4a, int p4b, int p4c, int p4d, int p4e, int p4f,\s
							int p50, int p51, int p52, int p53, int p54, int p55, int p56, int p57, int p58, int p59, int p5a, int p5b, int p5c, int p5d, int p5e, int p5f,\s
							int p60, int p61, int p62, int p63, int p64, int p65, int p66, int p67, int p68, int p69, int p6a, int p6b, int p6c, int p6d, int p6e, int p6f,\s
							int p70, int p71, int p72, int p73, int p74, int p75, int p76, int p77, int p78, int p79, int p7a, int p7b, int p7c, int p7d, int p7e, int p7f,\s
							int p80, int p81, int p82, int p83, int p84, int p85, int p86, int p87, int p88, int p89, int p8a, int p8b, int p8c, int p8d, int p8e, int p8f,\s
							int p90, int p91, int p92, int p93, int p94, int p95, int p96, int p97, int p98, int p99, int p9a, int p9b, int p9c, int p9d, int p9e, int p9f,\s
							int pa0, int pa1, int pa2, int pa3, int pa4, int pa5, int pa6, int pa7, int pa8, int pa9, int paa, int pab, int pac, int pad, int pae, int paf,\s
							int pb0, int pb1, int pb2, int pb3, int pb4, int pb5, int pb6, int pb7, int pb8, int pb9, int pba, int pbb, int pbc, int pbd, int pbe, int pbf,\s
							int pc0, int pc1, int pc2, int pc3, int pc4, int pc5, int pc6, int pc7, int pc8, int pc9, int pca, int pcb, int pcc, int pcd, int pce, int pcf,\s
							int pd0, int pd1, int pd2, int pd3, int pd4, int pd5, int pd6, int pd7, int pd8, int pd9, int pda, int pdb, int pdc, int pdd, int pde, int pdf,\s
							int pe0, int pe1, int pe2, int pe3, int pe4, int pe5, int pe6, int pe7, int pe8, int pe9, int pea, int peb, int pec, int ped, int pee, int pef,\s
							int pf0, int pf1, int pf2, int pf3, int pf4, int pf5, int pf6, int pf7, int pf8, int pf9, int pfa, int pfb, int pfc, int pfd
							) {}
					}
					@SuppressWarnings("synthetic-access")
					A a = new A(
						  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0
						);
					public static void main(String[] args) {
						StringBuilder params = new StringBuilder();
						params.append("/*this,*/");
						for (int p = 1; p < 255; p++) {
							if (p > 1) {
								params.append(", ");
								if (p % 16 == 0)
									params.append('\\n');
							}
							params.append("int p"
									+ Character.forDigit(p / 16, 16)
									+ Character.forDigit(p % 16, 16)
									);
						}
						System.out.println(params);
						A.class.getName(); // ClassFormatError
					}
				}""",
		},
		errMessage);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=321414
public void test112() {
	if (this.complianceLevel <= ClassFileConstants.JDK1_4) return;
	this.runConformTest(
		new String[] {
			"SyntheticConstructorTooManyArgs.java", //-----------------------------------------------------------------------
			"""
				public class SyntheticConstructorTooManyArgs {
				
					class A {
						private A(
							/*this,*/int p01, int p02, int p03, int p04, int p05, int p06, int p07, int p08, int p09, int p0a, int p0b, int p0c, int p0d, int p0e, int p0f,\s
							int p10, int p11, int p12, int p13, int p14, int p15, int p16, int p17, int p18, int p19, int p1a, int p1b, int p1c, int p1d, int p1e, int p1f,\s
							int p20, int p21, int p22, int p23, int p24, int p25, int p26, int p27, int p28, int p29, int p2a, int p2b, int p2c, int p2d, int p2e, int p2f,\s
							int p30, int p31, int p32, int p33, int p34, int p35, int p36, int p37, int p38, int p39, int p3a, int p3b, int p3c, int p3d, int p3e, int p3f,\s
							int p40, int p41, int p42, int p43, int p44, int p45, int p46, int p47, int p48, int p49, int p4a, int p4b, int p4c, int p4d, int p4e, int p4f,\s
							int p50, int p51, int p52, int p53, int p54, int p55, int p56, int p57, int p58, int p59, int p5a, int p5b, int p5c, int p5d, int p5e, int p5f,\s
							int p60, int p61, int p62, int p63, int p64, int p65, int p66, int p67, int p68, int p69, int p6a, int p6b, int p6c, int p6d, int p6e, int p6f,\s
							int p70, int p71, int p72, int p73, int p74, int p75, int p76, int p77, int p78, int p79, int p7a, int p7b, int p7c, int p7d, int p7e, int p7f,\s
							int p80, int p81, int p82, int p83, int p84, int p85, int p86, int p87, int p88, int p89, int p8a, int p8b, int p8c, int p8d, int p8e, int p8f,\s
							int p90, int p91, int p92, int p93, int p94, int p95, int p96, int p97, int p98, int p99, int p9a, int p9b, int p9c, int p9d, int p9e, int p9f,\s
							int pa0, int pa1, int pa2, int pa3, int pa4, int pa5, int pa6, int pa7, int pa8, int pa9, int paa, int pab, int pac, int pad, int pae, int paf,\s
							int pb0, int pb1, int pb2, int pb3, int pb4, int pb5, int pb6, int pb7, int pb8, int pb9, int pba, int pbb, int pbc, int pbd, int pbe, int pbf,\s
							int pc0, int pc1, int pc2, int pc3, int pc4, int pc5, int pc6, int pc7, int pc8, int pc9, int pca, int pcb, int pcc, int pcd, int pce, int pcf,\s
							int pd0, int pd1, int pd2, int pd3, int pd4, int pd5, int pd6, int pd7, int pd8, int pd9, int pda, int pdb, int pdc, int pdd, int pde, int pdf,\s
							int pe0, int pe1, int pe2, int pe3, int pe4, int pe5, int pe6, int pe7, int pe8, int pe9, int pea, int peb, int pec, int ped, int pee, int pef,\s
							int pf0, int pf1, int pf2, int pf3, int pf4, int pf5, int pf6, int pf7, int pf8, int pf9, int pfa, int pfb, int pfc
							) {}
					}
					@SuppressWarnings("synthetic-access")
					A a = new A(
						  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
						0,0,0,0,0,0,0,0,0,0,0,0,0
						);
					public static void main(String[] args) {
						StringBuilder params = new StringBuilder();
						params.append("/*this,*/");
						for (int p = 1; p < 253; p++) {
							if (p > 1) {
								params.append(", ");
								if (p % 16 == 0)
									params.append('\\n');
							}
							params.append("int p"
									+ Character.forDigit(p / 16, 16)
									+ Character.forDigit(p % 16, 16)
									);
						}
						System.out.println(params);
						A.class.getName(); // ClassFormatError
					}
				}""",
		},
		"""
			/*this,*/int p01, int p02, int p03, int p04, int p05, int p06, int p07, int p08, int p09, int p0a, int p0b, int p0c, int p0d, int p0e, int p0f,\s
			int p10, int p11, int p12, int p13, int p14, int p15, int p16, int p17, int p18, int p19, int p1a, int p1b, int p1c, int p1d, int p1e, int p1f,\s
			int p20, int p21, int p22, int p23, int p24, int p25, int p26, int p27, int p28, int p29, int p2a, int p2b, int p2c, int p2d, int p2e, int p2f,\s
			int p30, int p31, int p32, int p33, int p34, int p35, int p36, int p37, int p38, int p39, int p3a, int p3b, int p3c, int p3d, int p3e, int p3f,\s
			int p40, int p41, int p42, int p43, int p44, int p45, int p46, int p47, int p48, int p49, int p4a, int p4b, int p4c, int p4d, int p4e, int p4f,\s
			int p50, int p51, int p52, int p53, int p54, int p55, int p56, int p57, int p58, int p59, int p5a, int p5b, int p5c, int p5d, int p5e, int p5f,\s
			int p60, int p61, int p62, int p63, int p64, int p65, int p66, int p67, int p68, int p69, int p6a, int p6b, int p6c, int p6d, int p6e, int p6f,\s
			int p70, int p71, int p72, int p73, int p74, int p75, int p76, int p77, int p78, int p79, int p7a, int p7b, int p7c, int p7d, int p7e, int p7f,\s
			int p80, int p81, int p82, int p83, int p84, int p85, int p86, int p87, int p88, int p89, int p8a, int p8b, int p8c, int p8d, int p8e, int p8f,\s
			int p90, int p91, int p92, int p93, int p94, int p95, int p96, int p97, int p98, int p99, int p9a, int p9b, int p9c, int p9d, int p9e, int p9f,\s
			int pa0, int pa1, int pa2, int pa3, int pa4, int pa5, int pa6, int pa7, int pa8, int pa9, int paa, int pab, int pac, int pad, int pae, int paf,\s
			int pb0, int pb1, int pb2, int pb3, int pb4, int pb5, int pb6, int pb7, int pb8, int pb9, int pba, int pbb, int pbc, int pbd, int pbe, int pbf,\s
			int pc0, int pc1, int pc2, int pc3, int pc4, int pc5, int pc6, int pc7, int pc8, int pc9, int pca, int pcb, int pcc, int pcd, int pce, int pcf,\s
			int pd0, int pd1, int pd2, int pd3, int pd4, int pd5, int pd6, int pd7, int pd8, int pd9, int pda, int pdb, int pdc, int pdd, int pde, int pdf,\s
			int pe0, int pe1, int pe2, int pe3, int pe4, int pe5, int pe6, int pe7, int pe8, int pe9, int pea, int peb, int pec, int ped, int pee, int pef,\s
			int pf0, int pf1, int pf2, int pf3, int pf4, int pf5, int pf6, int pf7, int pf8, int pf9, int pfa, int pfb, int pfc""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=325567
public void test113() {
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				public class X {
					public static void bar(int i) {
						final String before;
						try {
							before = foo();
						} catch (IOException e) {
							// ignore
						}
						B b = new B(new I() {
							public String bar() {
								return new String(before);
							}
						});
						try {
							b.toString();
						} catch(Exception e) {
							// ignore
						}
					}
					private static String foo() throws IOException {
						return null;
					}
					static class B {
						B(I i) {
							//ignore
						}
					}
					static interface I {
						String bar();
					}
				}"""
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in X.java (at line 12)
				return new String(before);
				                  ^^^^^^
			The local variable before may not have been initialized
			----------
			""";
	runner.generateOutput =
		true;
	runner.runNegativeTest();

	runner = new Runner();
	runner.testFiles =
		new String[] {
			"Y.java", //-----------------------------------------------------------------------
			"""
				public class Y {
					public static void main(String[] args) {
						try {
							X.bar(3);
						} catch(VerifyError e) {
							System.out.println("FAILED");
						}
					}
				}""",
		};
	runner.expectedOutputString =
		"";
	runner.shouldFlushOutputDirectory =
		false;
	runner.javacTestOptions =
		JavacTestOptions.Excuse.JavacHasErrorsEclipseHasNone; // ecj can create .class from erroneous .java
	runner.runConformTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318682
// QualifiedNameReference, SingleNameReference and MessageSend
// Can be static warning shown
public void test114() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.testFiles =
		new String[] {
			"X.java",
			"""
				public class X {
					public static int field1;
					public static int field2;
					public void bar(int i) {
						System.out.println(foo());
						foo();\
						System.out.println(X.field1);
						System.out.println(field2);
						field2 = 1;
					}
					public final void bar2(int i) {
						System.out.println(foo());
						foo();\
						System.out.println(X.field1);
						System.out.println(field2);
						field2 = 1;
					}
					private void bar3(int i) {
						System.out.println(foo());
						foo();\
						System.out.println(X.field1);
						System.out.println(field2);
						field2 = 1;
					}
					private static String foo() {
						return null;
					}
				}"""
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in X.java (at line 4)
				public void bar(int i) {
				            ^^^^^^^^^^
			The method bar(int) from the type X can potentially be declared as static
			----------
			2. ERROR in X.java (at line 10)
				public final void bar2(int i) {
				                  ^^^^^^^^^^^
			The method bar2(int) from the type X can be declared as static
			----------
			3. WARNING in X.java (at line 16)
				private void bar3(int i) {
				             ^^^^^^^^^^^
			The method bar3(int) from the type X is never used locally
			----------
			4. ERROR in X.java (at line 16)
				private void bar3(int i) {
				             ^^^^^^^^^^^
			The method bar3(int) from the type X can be declared as static
			----------
			""";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318682
// FieldReference and MessageSend
// Can be static warning shown
public void test115() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.testFiles =
		new String[] {
			"X.java",
			"""
				public class X extends B{
					public static int field1;
					public static int field2;
					public void bar(int i) {
						System.out.println(foo());
						X.field2 = 2;
						System.out.println(field1);
						A a = new A();
						a.a1();
					}
					private static String foo() {
						return null;
					}
				}
				class A{
					public void a1() {
					}
				}
				class B{
					public void b1(){
					}
				}"""
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in X.java (at line 4)
				public void bar(int i) {
				            ^^^^^^^^^^
			The method bar(int) from the type X can potentially be declared as static
			----------
			""";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318682
// MessageSend in different ways
public void test116a() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportNonStaticAccessToStatic, CompilerOptions.IGNORE);
	runner.testFiles =
		new String[] {
			"X.java",
			"""
				public class X extends B{
					public static int field1;
					public X xfield;
					public void bar1(int i) {
						baz();
					}
					public void bar2(int i) {
						this.baz();
					}
					public void bar3(int i) {
						this.xfield.baz();
					}
					public void bar4(int i) {
						xfield.baz();
					}
					public void bar5(int i) {
						X x = new X();
						x.baz();
					}
					public void bar6(int i) {
						A.xA.baz();
					}
					public void bar7(int i) {
						b1();
					}
					public void bar8(int i) {
						this.b1();
					}
					public void bar9(int i) {
						new X().b1();
					}
					public void baz() {
					}
				}
				class A{
					public static X xA;
				}
				class B{
					public void b1(){
					}
				}""",
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in X.java (at line 16)
				public void bar5(int i) {
				            ^^^^^^^^^^^
			The method bar5(int) from the type X can potentially be declared as static
			----------
			2. ERROR in X.java (at line 20)
				public void bar6(int i) {
				            ^^^^^^^^^^^
			The method bar6(int) from the type X can potentially be declared as static
			----------
			3. ERROR in X.java (at line 29)
				public void bar9(int i) {
				            ^^^^^^^^^^^
			The method bar9(int) from the type X can potentially be declared as static
			----------
			""";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318682
// MessageSend in different ways, referencing a static method.
public void test116b() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportNonStaticAccessToStatic, CompilerOptions.IGNORE);
	runner.testFiles =
		new String[] {
			"X.java",
			"""
				public class X extends B{
					public static int field1;
					public static X xfield;
					public void bar1(int i) {
						baz();
					}
					public void bar2(int i) {
						this.baz();
					}
					public void bar3(int i) {
						this.xfield.baz();
					}
					public void bar4(int i) {
						xfield.baz();
					}
					public void bar5(int i) {
						X x = new X();
						x.baz();
					}
					public void bar6(int i) {
						A.xA.baz();
					}
					public void bar7(int i) {
						b1();
					}
					public void bar8(int i) {
						this.b1();
					}
					public void bar9(int i) {
						new X().b1();
					}
					public static void baz() {
					}
				}
				class A{
					public static X xA;
				}
				class B{
					public static void b1(){
					}
				}""",
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in X.java (at line 4)
				public void bar1(int i) {
				            ^^^^^^^^^^^
			The method bar1(int) from the type X can potentially be declared as static
			----------
			2. ERROR in X.java (at line 13)
				public void bar4(int i) {
				            ^^^^^^^^^^^
			The method bar4(int) from the type X can potentially be declared as static
			----------
			3. ERROR in X.java (at line 16)
				public void bar5(int i) {
				            ^^^^^^^^^^^
			The method bar5(int) from the type X can potentially be declared as static
			----------
			4. ERROR in X.java (at line 20)
				public void bar6(int i) {
				            ^^^^^^^^^^^
			The method bar6(int) from the type X can potentially be declared as static
			----------
			5. ERROR in X.java (at line 23)
				public void bar7(int i) {
				            ^^^^^^^^^^^
			The method bar7(int) from the type X can potentially be declared as static
			----------
			6. ERROR in X.java (at line 29)
				public void bar9(int i) {
				            ^^^^^^^^^^^
			The method bar9(int) from the type X can potentially be declared as static
			----------
			""";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318682
// Referring a field in different ways, accessing non-static field.
public void test117a() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportNonStaticAccessToStatic, CompilerOptions.IGNORE);
	runner.testFiles =
		new String[] {
			"X.java",
			"""
				public class X extends B{
					public int field1;
					public X xfield;
					public void bar1(int i) {
						field1 = 1;
					}
					public void bar2(int i) {
						this.field1 = 1;
					}
					public void bar3(int i) {
						System.out.println(field1);
					}
					public void bar4(int i) {
						System.out.println(this.field1);
					}
					public void bar5(int i) {
						X x = new X();
						x.field1 = 1;
					}
					public void bar6(int i) {
						A.xA.field1 = 1;
					}
					public void bar7(int i) {
						b1 = 1;
					}
					public void bar8(int i) {
						this.b1 = 1;
					}
					public void bar9(int i) {
						new X().b1 = 1;
					}
					public void bar10(int i) {
						this.xfield.field1 = 1;
					}
					public void bar11(int i) {
						System.out.println(this.xfield.field1);
					}
					public void bar12(int i) {
						System.out.println(new X().b1);
					}
					public void bar13(int i) {
						System.out.println(b1);
					}
					public void bar14(int i) {
						System.out.println(this.b1);
					}
					public void bar15(int i) {
						xfield.field1 = 1;
					}
					public void bar16(int i) {
						System.out.println(xfield.field1);
					}
					public void baz() {
					}
				}
				class A{
					public static X xA;
				}
				class B{
					public int b1;
				}""",
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in X.java (at line 16)
				public void bar5(int i) {
				            ^^^^^^^^^^^
			The method bar5(int) from the type X can potentially be declared as static
			----------
			2. ERROR in X.java (at line 20)
				public void bar6(int i) {
				            ^^^^^^^^^^^
			The method bar6(int) from the type X can potentially be declared as static
			----------
			3. ERROR in X.java (at line 29)
				public void bar9(int i) {
				            ^^^^^^^^^^^
			The method bar9(int) from the type X can potentially be declared as static
			----------
			4. ERROR in X.java (at line 38)
				public void bar12(int i) {
				            ^^^^^^^^^^^^
			The method bar12(int) from the type X can potentially be declared as static
			----------
			""";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318682
// Referring a field in different ways, accessing non-static field.
public void test117b() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportNonStaticAccessToStatic, CompilerOptions.IGNORE);
	runner.testFiles =
		new String[] {
			"X.java",
			"""
				public class X extends B{
					public static int field1;
					public static X xfield;
					public void bar1(int i) {
						field1 = 1;
					}
					public void bar2(int i) {
						this.field1 = 1;
					}
					public void bar3(int i) {
						System.out.println(field1);
					}
					public void bar4(int i) {
						System.out.println(this.field1);
					}
					public void bar5(int i) {
						X x = new X();
						x.field1 = 1;
					}
					public void bar6(int i) {
						A.xA.field1 = 1;
					}
					public void bar7(int i) {
						b1 = 1;
					}
					public void bar8(int i) {
						this.b1 = 1;
					}
					public void bar9(int i) {
						new X().b1 = 1;
					}
					public void bar10(int i) {
						this.xfield.field1 = 1;
					}
					public void bar11(int i) {
						System.out.println(this.xfield.field1);
					}
					public void bar12(int i) {
						System.out.println(new X().b1);
					}
					public void bar13(int i) {
						System.out.println(b1);
					}
					public void bar14(int i) {
						System.out.println(this.b1);
					}
					public void bar15(int i) {
						xfield.field1 = 1;
					}
					public void bar16(int i) {
						System.out.println(xfield.field1);
					}
					public void baz() {
					}
				}
				class A{
					public static X xA;
				}
				class B{
					public static int b1;
				}""",
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in X.java (at line 4)
				public void bar1(int i) {
				            ^^^^^^^^^^^
			The method bar1(int) from the type X can potentially be declared as static
			----------
			2. ERROR in X.java (at line 10)
				public void bar3(int i) {
				            ^^^^^^^^^^^
			The method bar3(int) from the type X can potentially be declared as static
			----------
			3. ERROR in X.java (at line 16)
				public void bar5(int i) {
				            ^^^^^^^^^^^
			The method bar5(int) from the type X can potentially be declared as static
			----------
			4. ERROR in X.java (at line 20)
				public void bar6(int i) {
				            ^^^^^^^^^^^
			The method bar6(int) from the type X can potentially be declared as static
			----------
			5. ERROR in X.java (at line 23)
				public void bar7(int i) {
				            ^^^^^^^^^^^
			The method bar7(int) from the type X can potentially be declared as static
			----------
			6. ERROR in X.java (at line 29)
				public void bar9(int i) {
				            ^^^^^^^^^^^
			The method bar9(int) from the type X can potentially be declared as static
			----------
			7. ERROR in X.java (at line 38)
				public void bar12(int i) {
				            ^^^^^^^^^^^^
			The method bar12(int) from the type X can potentially be declared as static
			----------
			8. ERROR in X.java (at line 41)
				public void bar13(int i) {
				            ^^^^^^^^^^^^
			The method bar13(int) from the type X can potentially be declared as static
			----------
			9. ERROR in X.java (at line 47)
				public void bar15(int i) {
				            ^^^^^^^^^^^^
			The method bar15(int) from the type X can potentially be declared as static
			----------
			10. ERROR in X.java (at line 50)
				public void bar16(int i) {
				            ^^^^^^^^^^^^
			The method bar16(int) from the type X can potentially be declared as static
			----------
			""";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318682
// Final class -> can be static (and not potentially be static) warning shown
public void test118() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.testFiles =
		new String[] {
				"X.java",
				"""
					final public class X {
						public static int field1;
						public static int field2;
						public void bar(int i) {
							System.out.println(foo());
							foo();\
							System.out.println(X.field1);
							System.out.println(field2);
							field2 = 1;
						}
						public static int foo(){ return 1;}
					}"""
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in X.java (at line 4)
				public void bar(int i) {
				            ^^^^^^^^^^
			The method bar(int) from the type X can be declared as static
			----------
			""";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318682
// Method of a local class -> can't be static, so no warning
// Also method with such a local class accessing a member of the outer class can't be static
public void test119() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.testFiles =
		new String[] {
				"X.java",
				"""
					public class X {
						public static int field1;
						public int field2;
						public void bar(int i) {
							(new Object() {
								public boolean foo1() {
									return X.this.field2 == 1;
								}
							}).foo1();
						System.out.println(X.field1);
						}
						public void bar2(int i) {
							(new Object() {
								public boolean foo1() {
									System.out.println(X.field1);
									return true;\
								}
							}).foo1();
						System.out.println(X.field1);
						}
					}"""
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in X.java (at line 12)
				public void bar2(int i) {
				            ^^^^^^^^^^^
			The method bar2(int) from the type X can potentially be declared as static
			----------
			""";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318682
// Method using type parameters declared by enclosing class can't be static, so don't warn
public void test120() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.testFiles =
		new String[] {
				"X.java",
				"""
					public class X<T> {
						public static int field1;
						public int field2;
						public void bar(T t) {
							X.field1 = 1;
							System.out.println(t);
						}
						public <E> void bar2(E e) {
							X.field1 = 1;
							System.out.println(e);
						}
						public <E> void bar3() {
							T a;
							System.out.println();
						}
						public <E,Y> void bar4() {
							Y a;
							System.out.println();
						}
					}"""
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in X.java (at line 8)
				public <E> void bar2(E e) {
				                ^^^^^^^^^
			The method bar2(E) from the type X<T> can potentially be declared as static
			----------
			2. ERROR in X.java (at line 16)
				public <E,Y> void bar4() {
				                  ^^^^^^
			The method bar4() from the type X<T> can potentially be declared as static
			----------
			""";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318682
// Access to super in a method disqualifies it from being static
public void test121() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportNonStaticAccessToStatic, CompilerOptions.IGNORE);
	runner.testFiles =
		new String[] {
				"X.java",
				"""
					public class X extends A{
						public static int field1;
						public int field2;
						public void methodA() {
							super.methodA();
						}
						public void bar() {
							super.fieldA = 1;
						}
						public void bar2() {
							System.out.println(super.fieldA);
						}
						public void bar3() {
							System.out.println(X.fieldA);
						}
					}
					class A{
						public static int fieldA;
					   public void methodA(){
					   }
					}"""
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in X.java (at line 13)
				public void bar3() {
				            ^^^^^^
			The method bar3() from the type X can potentially be declared as static
			----------
			""";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318682
// Methods of non-static member types can't be static
public void test122() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportNonStaticAccessToStatic, CompilerOptions.IGNORE);
	runner.testFiles =
		new String[] {
				"X.java",
				"""
					public class X {
						class A{
					   	void methodA() {
								System.out.println();
							}
					   }
						static class B{
					   	void methodB() {
								System.out.println();
							}
					   }
					}"""
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in X.java (at line 8)
				void methodB() {
				     ^^^^^^^^^
			The method methodB() from the type X.B can potentially be declared as static
			----------
			""";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318682
// If method returns type parameter not declared by it, it cannot be static
public void test123() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportNonStaticAccessToStatic, CompilerOptions.IGNORE);
	runner.testFiles =
		new String[] {
				"X.java",
				"""
					public class X<T> {
						<E,Y> T method1() {
							return null;
						}
						<E,Y> E method2() {
							return null;
						}
					}"""
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in X.java (at line 5)
				<E,Y> E method2() {
				        ^^^^^^^^^
			The method method2() from the type X<T> can potentially be declared as static
			----------
			""";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=335845
// If method allocates an inner non-static type without an enclosing object, method can't be static
public void testBug335845a() {
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportNonStaticAccessToStatic, CompilerOptions.IGNORE);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.IGNORE);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedObjectAllocation, CompilerOptions.IGNORE);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	compilerOptions.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					public class X {
						private class Bar {
							int a = 1;
						}
						private void foo() {
							new Bar();
						}
					}"""
		},
		"",
		null /* no extra class libraries */,
		true /* flush output directory */,
		compilerOptions /* custom options */
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=335845
// If method allocates an inner non-static type without an enclosing object, method can't be static
public void testBug335845b() {
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportNonStaticAccessToStatic, CompilerOptions.IGNORE);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.IGNORE);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedObjectAllocation, CompilerOptions.IGNORE);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	compilerOptions.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					public class X {
						private class Bar {
							int a = 1;
						}
						private void foo() {
							int x = new Bar().a;
						}
					}"""
		},
		"",
		null /* no extra class libraries */,
		true /* flush output directory */,
		compilerOptions /* custom options */
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=335845
// If method allocates an inner static type without an enclosing object, method can be static
public void testBug335845c() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportNonStaticAccessToStatic, CompilerOptions.IGNORE);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.IGNORE);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedObjectAllocation, CompilerOptions.IGNORE);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	runner.customOptions.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	runner.testFiles =
		new String[] {
				"X.java",
				"""
					public class X {
						private static class Bar {
							int a = 1;
						}
						private void foo() {
							new Bar();
							int x = new Bar().a;\
						}
					}"""
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in X.java (at line 5)
				private void foo() {
				             ^^^^^
			The method foo() from the type X can be declared as static
			----------
			""";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=335845
// If method allocates an inner non-static type without an enclosing object, method can't be static
public void testBug335845d() {
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportNonStaticAccessToStatic, CompilerOptions.IGNORE);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.IGNORE);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedObjectAllocation, CompilerOptions.IGNORE);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	compilerOptions.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					public class X {
						private class Bar {
							class Bar2{}
						}
						private void foo() {
							new Bar().new Bar2();
						}
					}"""
		},
		"",
		null /* no extra class libraries */,
		true /* flush output directory */,
		compilerOptions /* custom options */
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=335845
// If method allocates an inner static type without an enclosing object, method can be static
public void testBug335845e() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportNonStaticAccessToStatic, CompilerOptions.IGNORE);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.IGNORE);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedObjectAllocation, CompilerOptions.IGNORE);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	runner.customOptions.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	runner.testFiles =
		new String[] {
				"X.java",
				"""
					public class X {
						private class Bar {
							int a = 1;
						}
						private void foo() {
							new X().new Bar();
						}
					}"""
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in X.java (at line 5)
				private void foo() {
				             ^^^^^
			The method foo() from the type X can be declared as static
			----------
			""";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=335845
// If method allocates an inner static type without an enclosing object, method can be static
public void testBug335845f() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportNonStaticAccessToStatic, CompilerOptions.IGNORE);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.IGNORE);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedObjectAllocation, CompilerOptions.IGNORE);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	runner.customOptions.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	runner.testFiles =
		new String[] {
				"X.java",
				"""
					public class X {
						private class Bar {
							int a = 1;
						}
						private void foo() {
							X x = new X();\
							x.new Bar().a = 2;
						}
					}"""
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in X.java (at line 5)
				private void foo() {
				             ^^^^^
			The method foo() from the type X can be declared as static
			----------
			""";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=335845
// If method allocates an inner static type without an enclosing object, method can be static
public void testBug335845g() {
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportNonStaticAccessToStatic, CompilerOptions.IGNORE);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.IGNORE);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedObjectAllocation, CompilerOptions.IGNORE);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	compilerOptions.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
				"p/X.java",
				"""
					package p;
					public class X {
						class Bar {
						}
					}"""
		}
	);
	this.runNegativeTest(
		new String[] {
				"p/Y.java",
				"""
					package p;
					public class Y extends X {
						private void foo() {
							new Bar();
						}
					}"""
		},
		"",
		null /* no extra class libraries */,
		false /* flush output directory */,
		compilerOptions /* custom options */
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=335780
// For this reference as an argument of a message send, method can't be static
public void test124a() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportNonStaticAccessToStatic, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					public class X {
						public void method1() {
							Foo.m(this);
						}
					static class Foo{
						static void m(X bug) {
						\t
						}
					}
					}"""
		},
		"",
		null /* no extra class libraries */,
		true /* flush output directory */,
		compilerOptions /* custom options */
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=335780
// For this reference as an argument of a message send, method can't be static
public void test124b() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					public class X {
					   public static X xField;\
						public void method1() {
							Foo.m(this.xField);
						}
					static class Foo{
						static void m(X bug) {
						\t
						}
					}
					}"""
		},
		"""
			----------
			1. WARNING in X.java (at line 3)
				Foo.m(this.xField);
				           ^^^^^^
			The static field X.xField should be accessed in a static way
			----------
			""",
		null /* no extra class libraries */,
		true /* flush output directory */,
		compilerOptions /* custom options */
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=354502
// Anonymous class instantiation of a non-static member type, method can't be static
public void test354502() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	runner.testFiles =

		new String[] {
				"X.java",
				"""
					public class X {
					   public abstract class Abstract{}
					   public static abstract class Abstract2{}
						private void method1() {
							new Abstract() {};
						}
						private void method2() {
							new Abstract2() {};
						}
					}"""
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in X.java (at line 7)
				private void method2() {
				             ^^^^^^^^^
			The method method2() from the type X can be declared as static
			----------
			""";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=360164
public void test360164() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
	this.runConformTest(
			new String[] {
					"p/B.java",
					"""
						package p;
						
						public abstract class B<K,V> {
							 protected abstract V foo(K element);
						}
						""",
					"p/C.java",
					"""
						package p;
						public class C {
						}
						""",
					"p/D.java",
					"""
						package p;
						public class D extends E {
						}
						""",
					"p/E.java",
					"""
						package p;
						public abstract class E implements I {
						}
						""",
					"p/I.java",
					"""
						package p;
						public interface I {
						}
						""",
					"p/X.java",
					"""
						package p;
						public class X {
							private final class A extends B<C,D>{
								@Override
								protected D foo(C c) {
									return null;
								}
						   }
						}
						""",
			},
			"");

	// delete binary file I (i.e. simulate removing it from classpath for subsequent compile)
	Util.delete(new File(OUTPUT_DIR, "p" + File.separator + "I.class"));

	runNegativeTest(
		// test directory preparation
		false /* do not flush output directory */,
		new String[] { /* test files */
				"p/X.java",
				"""
					package p;
					public class X {
						private final class A extends B<C,D>{
							@Override
							protected D foo(C c) {
					            Zork z;
								return null;
							}
					   }
					}
					""",
		},
		// compiler options
		null /* no class libraries */,
		null /* no custom options */,
		"""
			----------
			1. WARNING in p\\X.java (at line 3)
				private final class A extends B<C,D>{
				                    ^
			The type X.A is never used locally
			----------
			2. ERROR in p\\X.java (at line 6)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			""",
		// javac options
		JavacTestOptions.SKIP_UNTIL_FRAMEWORK_FIX /* javac test options */);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
// SingleNameReference, assignment of instance field inside a local class method
public void test376550_1a() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					public class X {
						int i = 1;
					   public void upper1(){}
					   public void foo(){
					   	class Local{
								int i2 = 1;
								void method1() {
									i = 1;
								}
							}
						}
					}"""
		},
		"",
		null /* no extra class libraries */,
		true /* flush output directory */,
		compilerOptions /* custom options */
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
// SingleNameReference, assignment of instance field of local class inside a local class method
public void test376550_1b() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	new Runner() {{
	  this.customOptions = getCompilerOptions();
	  this.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	  this.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	  this.customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	  this.testFiles =
		new String[] {
				"X.java",
				"""
					public class X {
						int i = 1;
					   public void upper1(){}
					   public void foo(){
					   	class Local{
								int i2 = 1;
								void method2() {
									i2 = 1;
								}
							}
						}
					}"""
		};
	  this.expectedCompilerLog =
		"""
			----------
			1. ERROR in X.java (at line 4)
				public void foo(){
				            ^^^^^
			The method foo() from the type X can potentially be declared as static
			----------
			""";
	  this.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	}}.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
// LocalDeclaration with type as a type variable binding
public void test376550_2a() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	runner.testFiles =
		new String[] {
				"X.java",
				"""
					public class X<T> {
					   public void upper1(){}
					   public void foo(){
					   	class Local<K>{
								void method2() {
									K k;
								}
							}
						}
					}"""
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in X.java (at line 3)
				public void foo(){
				            ^^^^^
			The method foo() from the type X<T> can potentially be declared as static
			----------
			""";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
// LocalDeclaration with type as a type variable binding
public void test376550_2b() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					public class X<T> {
					   public void upper1(){}
					   public void foo(){
					   	class Local<K>{
								void method2() {
									T t;
								}
							}
						}
					}"""
		},
		"",
		null /* no extra class libraries */,
		true /* flush output directory */,
		compilerOptions /* custom options */
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
// MessageSend, calling outer class method inside a local class method
public void test376550_3a() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					public class X<T> {
					   public void upper1(){}
					   public void foo(){
					   	class Local<K>{
								void lower() {}
								void method2() {
									upper1();
								}
							}
						}
					}"""
		},
		"",
		null /* no extra class libraries */,
		true /* flush output directory */,
		compilerOptions /* custom options */
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
// MessageSend, calling local class method inside a local class method
public void test376550_3b() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	runner.testFiles =
		new String[] {
				"X.java",
				"""
					public class X<T> {
					   public void upper1(){}
					   public void foo(){
					   	class Local<K>{
								void lower() {}
								void method2() {
									lower();
								}
							}
						}
					}"""
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in X.java (at line 3)
				public void foo(){
				            ^^^^^
			The method foo() from the type X<T> can potentially be declared as static
			----------
			""";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
// Local class instance field is an argument in messageSend in local class method
public void test376550_4a() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	runner.testFiles =
		new String[] {
				"X.java",
				"""
					public class X<T> {
					   int i1 = 1;
					   public void foo(){
					   	class Local<K>{
								int i2 = 1;
								void lower(int i) {}
								void method2() {
									lower(i2);
								}
							}
						}
					}"""
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in X.java (at line 3)
				public void foo(){
				            ^^^^^
			The method foo() from the type X<T> can potentially be declared as static
			----------
			""";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
// Outerclass instance field is an argument in messageSend in local class method
public void test376550_4b() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					public class X<T> {
					   int i1 = 1;
					   public void foo(){
					   	class Local<K>{
								int i2 = 1;
								void lower(int i) {}
								void method2() {
									lower(i1);
								}
							}
						}
					}"""
		},
		"",
		null /* no extra class libraries */,
		true /* flush output directory */,
		compilerOptions /* custom options */
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
// QualifiedNameReference, accessing local class instance field
public void test376550_5a() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	runner.testFiles =
		new String[] {
				"X.java",
				"""
					public class X {
					   int i1 = 1;
					   public void foo(){
					   	class Local{
								int i2 = 1;
								void method2() {
									Local.this.i2 = 1;
								}
							}
						}
					}"""
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in X.java (at line 3)
				public void foo(){
				            ^^^^^
			The method foo() from the type X can potentially be declared as static
			----------
			""";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
// https://bugs.eclispe.org/379784 - [compiler] "Method can be static" is not getting reported
// Variation of the above
public void test376550_5aa() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	runner.testFiles =
		new String[] {
				"X.java",
				"""
					public class X {
						int i1 = 1;
						public void foo(){
							class Local{
								int i2 = 1;
					       }
					       class Local2 extends Local {
								void method2() {
									Local2.this.i2 = 1;
								}
							}
						}
					}
					"""
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in X.java (at line 3)
				public void foo(){
				            ^^^^^
			The method foo() from the type X can potentially be declared as static
			----------
			""";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
// QualifiedNameReference, accessing outer class instance field
public void test376550_5b() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					public class X {
					   int i1 = 1;
					   public void foo(){
					   	class Local{
								int i2 = 1;
								void method2() {
									X.this.i1 = 1;
								}
							}
						}
					}"""
		},
		"",
		null /* no extra class libraries */,
		true /* flush output directory */,
		compilerOptions /* custom options */
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
// QualifiedNameRef.analyseCode()
public void test376550_6a() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	runner.testFiles =
		new String[] {
				"X.java",
				"""
					public class X {
					   int i1 = 1;
					   public void foo(){
					   	class Local{
								int i2 = 1;
								boolean method2() {
									return Local.this.i2 == 1;
								}
							}
						}
					}"""
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in X.java (at line 3)
				public void foo(){
				            ^^^^^
			The method foo() from the type X can potentially be declared as static
			----------
			""";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
// QualifiedNameRef.analyseCode()
public void test376550_6b() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					public class X {
					   int i1 = 1;
					   public void foo(){
					   	class Local{
								int i2 = 1;
								boolean method2() {
									return X.this.i1 == 1;
								}
							}
						}
					}"""
		},
		"",
		null /* no extra class libraries */,
		true /* flush output directory */,
		compilerOptions /* custom options */
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
// QualifiedAllocationExpression, allocating an anonymous type without an enclosing instance of parent type
// anon. type is declared in local class
public void test376550_7a() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	runner.testFiles =
		new String[] {
				"X.java",
				"""
					public class X {
					   abstract class AbsUp{}
					   public void foo(){
					   	class Local{
								abstract class AbsLow{}
								void method2() {
									new AbsLow(){};
								}
							}
						}
					}"""
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in X.java (at line 3)
				public void foo(){
				            ^^^^^
			The method foo() from the type X can potentially be declared as static
			----------
			""";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
// QualifiedAllocationExpression, allocating an anonymous type without an enclosing instance of parent type
// anon. type is declared in outer class
public void test376550_7b() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					public class X {
					   abstract class AbsUp{}
					   public void foo(){
					   	class Local{
								abstract  class AbsLow{}
								void method2() {
									new AbsUp(){};
								}
							}
						}
					}"""
		},
		"",
		null /* no extra class libraries */,
		true /* flush output directory */,
		compilerOptions /* custom options */
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
// FieldRef, from object of a class in outer class
public void test376550_8a() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	runner.testFiles =
		new String[] {
				"X.java",
				"""
					public class X {
					   class AbsUp{ int a;}
					   public void foo(){
					   	class Local{
								class AbsLow{  int a;}
								void method2() {
									int abc = new AbsLow().a;
								}
							}
						}
					}"""
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in X.java (at line 3)
				public void foo(){
				            ^^^^^
			The method foo() from the type X can potentially be declared as static
			----------
			""";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
//FieldRef, from object of a class in local class
public void test376550_8b() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					public class X {
					   class AbsUp{ int a;}
					   public void foo(){
					   	class Local{
								class AbsLow{  int a;}
								void method2() {
									int abc = new AbsUp().a;
								}
							}
						}
					}"""
		},
		"",
		null /* no extra class libraries */,
		true /* flush output directory */,
		compilerOptions /* custom options */
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
// QualifiedNameRef, accessing a field from local class field
public void test376550_9a() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	runner.testFiles =
		new String[] {
				"X.java",
				"""
					public class X {
					   X xup;
						int i = 1;
					   public void foo(){
					   	class Local{
								X xdown;
								class AbsLow{  int a;}
								void method2() {
									int abc = xdown.i;
								}
							}
						}
					}"""
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in X.java (at line 4)
				public void foo(){
				            ^^^^^
			The method foo() from the type X can potentially be declared as static
			----------
			""";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
// QualifiedNameRef, accessing a field from local class field
public void test376550_9b() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					public class X {
					   X xup;
						int i = 1;
					   public void foo(){
					   	class Local{
								X xdown;
								class AbsLow{  int a;}
								void method2() {
									int abc = xup.i;
								}
							}
						}
					}"""
		},
		"",
		null /* no extra class libraries */,
		true /* flush output directory */,
		compilerOptions /* custom options */
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
// QualifiedNameRef, accessing a field from local class field
public void test376550_10a() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	runner.testFiles =
		new String[] {
				"X.java",
				"""
					public class X {
					   X xup;
						int i = 1;
					   public void foo(){
					   	class Local{
								X xdown;
								void calc(int i1){}
								void method2() {
									calc(xdown.i);
								}
							}
						}
					}"""
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in X.java (at line 4)
				public void foo(){
				            ^^^^^
			The method foo() from the type X can potentially be declared as static
			----------
			""";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
// QualifiedNameRef, accessing a field from local class field
public void test376550_10b() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					public class X {
					   X xup;
						int i = 1;
					   public void foo(){
					   	class Local{
								X xdown;
								void calc(int i1){}
								void method2() {
									calc(xup.i);
								}
							}
						}
					}"""
		},
		"",
		null /* no extra class libraries */,
		true /* flush output directory */,
		compilerOptions /* custom options */
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
// bug test case
public void test376550_11() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	runner.testFiles =
		new String[] {
				"X.java",
				"""
					import java.util.ArrayList;
					import java.util.Collection;
					public class X {
					   private Object o = new Object();
					   public final Collection<Object> go() {
					   	return new ArrayList<Object>() {
								{ add(o);}
							};
						}
					}"""
		};
	runner.expectedCompilerLog = isMinimumCompliant(ClassFileConstants.JDK11) ?
		"""
			----------
			1. WARNING in X.java (at line 6)
				return new ArrayList<Object>() {
				           ^^^^^^^^^^^^^^^^^^^
			The serializable class  does not declare a static final serialVersionUID field of type long
			----------
			"""
		:
		"""
			----------
			1. WARNING in X.java (at line 6)
				return new ArrayList<Object>() {
				           ^^^^^^^^^^^^^^^^^^^
			The serializable class  does not declare a static final serialVersionUID field of type long
			----------
			2. WARNING in X.java (at line 7)
				{ add(o);}
				      ^
			Read access to enclosing field X.o is emulated by a synthetic accessor method
			----------
			""";
	runner.runWarningTest();
}

// https://bugs.eclipse.org/376550
// https://bugs.eclipse.org/379784 - [compiler] "Method can be static" is not getting reported
// bug test case
public void test376550_11a() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	runner.testFiles =
		new String[] {
				"X.java",
				"""
					import java.util.ArrayList;
					import java.util.Collection;
					public class X {
					   private Object o = new Object();
					   public final Collection<Object> go() {
					   	return new ArrayList<Object>() {
								{ add(null);}
							};
						}
					}"""
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in X.java (at line 5)
				public final Collection<Object> go() {
				                                ^^^^
			The method go() from the type X can be declared as static
			----------
			2. WARNING in X.java (at line 6)
				return new ArrayList<Object>() {
				           ^^^^^^^^^^^^^^^^^^^
			The serializable class  does not declare a static final serialVersionUID field of type long
			----------
			""";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
public void test376550_12() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	runner.testFiles =
		new String[] {
				"X.java",
				"""
					import java.util.ArrayList;
					import java.util.Collection;
					public class X<E> {
					   private Object o = new Object();
					   public final <E1> Collection<E1> go() {
					   	return new ArrayList<E1>() {
								{ E1 e;}
							};
						}
					}"""
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in X.java (at line 5)
				public final <E1> Collection<E1> go() {
				                                 ^^^^
			The method go() from the type X<E> can be declared as static
			----------
			2. WARNING in X.java (at line 6)
				return new ArrayList<E1>() {
				           ^^^^^^^^^^^^^^^
			The serializable class  does not declare a static final serialVersionUID field of type long
			----------
			""";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376550
// https://bugs.eclipse.org/379834 - Wrong "method can be static" in presence of qualified super and different staticness of nested super class.
public void test376550_13() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
				"QualifiedSuper.java",
				"""
					public class QualifiedSuper {
						class InnerS {
							void flub() {}
						}
						static class InnerT extends InnerS {
							InnerT(QualifiedSuper qs) {
								qs.super();
							}
							final void schlumpf() {
								InnerT.super.flub();
							}
						}\t
					}
					"""
		},
		"",
		null /* no extra class libraries */,
		true /* flush output directory */,
		null,
		compilerOptions /* custom options */,
		null
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=379530
public void test379530() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
				"X.java",
				"""
					public class X<S> {
					   S s;
						{
							 S /*[*/s/*]*/;
							 s= X.this.s;\
						}
					}"""
		},
		"",
		null /* no extra class libraries */,
		true /* flush output directory */,
		null,
		compilerOptions /* custom options */,
		null
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=393781
public void test393781() {
	Map compilerOptions = getCompilerOptions(); // OPTION_ReportRawTypeReference
	Object oldOption = compilerOptions.get(CompilerOptions.OPTION_ReportRawTypeReference);
	compilerOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	try {
		this.runNegativeTest(
				new String[] {
						"p/X.java",
						"""
							public class X {
							   public void foo(Map map, String str) {}
								public void foo1() {}
								public void bar(java.util.Map map) {
									foo(map, "");
									foo(map);
									foo();
									foo1(map, "");
								}
							}
							class Map {}
							"""
				},
				"""
					----------
					1. ERROR in p\\X.java (at line 5)
						foo(map, "");
						^^^
					The method foo(Map, java.lang.String) in the type X is not applicable for the arguments (java.util.Map, java.lang.String)
					----------
					2. ERROR in p\\X.java (at line 6)
						foo(map);
						^^^
					The method foo(Map, String) in the type X is not applicable for the arguments (Map)
					----------
					3. ERROR in p\\X.java (at line 7)
						foo();
						^^^
					The method foo(Map, String) in the type X is not applicable for the arguments ()
					----------
					4. ERROR in p\\X.java (at line 8)
						foo1(map, "");
						^^^^
					The method foo1() in the type X is not applicable for the arguments (Map, String)
					----------
					""",
				null,
				true,
				compilerOptions /* default options */
			);
	} finally {
		compilerOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, oldOption);
	}
}
private void runStaticWarningConformTest(String fileName, String body) {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map compilerOptions = getCompilerOptions();
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	compilerOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			fileName,
			body
		},
		compilerOptions /* custom options */
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=378674
//Can be static warning shown in the wrong places, i.e. if the type parameter is used in the signature
public void test378674_comment0() {
	runStaticWarningConformTest(
		"Test.java",
		"public class Test<T> {\n" +
		"\n" +
		"    @SuppressWarnings({ \"unchecked\", \"rawtypes\" })\n" +
		"    public static void main(String[] args) {\n" +
		"        new Test().method(null);\n" +
		"    }\n" +
		"\n" +
		"    private static class SubClass<A> {\n" +
		"\n" +
		"    }\n" +
		"\n" +
		"    private void method(SubClass<T> s) {\n" +
		"        System.out.println(s);\n" +
		"    }\n" +
		"\n" +
		"}\n" +
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=378674
public void test378674_comment1b() {
	runStaticWarningConformTest(
		"X.java",
		"import java.util.Collection;\n" +
		"class X<E>{\n" +
		"   public final <E1> Collection<E> go() {  // cannot be static\n" +
		"		return null; \n" +
		"   }\n" +
		"}\n" +
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=378674
//Can be static warning shown in the wrong places
public void test378674_comment1c() {
	runStaticWarningConformTest(
		"X.java",
		"import java.util.Collection;\n" +
		"import java.util.ArrayList;\n" +
		"	class X<E>{\n" +
		"   public final <E1> Collection<?> go() {  // cannot be static\n" +
		"		return new ArrayList<E>(); \n" +
		"   }\n" +
		"}\n" +
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=378674
//Can be static warning shown in the wrong places
public void test378674_comment2() {
	runStaticWarningConformTest(
		"X.java",
		"public class X<T> {\n" +
		"	public final void foo() {\n" +
		"		java.util.List<T> k;\n" +
		"	}\n" +
		"}\n" +
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=378674
public void test378674_comment3() {
	runStaticWarningConformTest(
		"Test.java",
		"public class Test {\n" +
		"	//false positive of method can be declared static\n" +
		"	void bar() {\n" +
		"		foo(Test.this);\n" +
		"	}\n" +
		"\n" +
		"	private static void foo(Test test) {\n" +
		"		System.out.println(test.getClass().getName());\n" +
		"	}\n" +
		"}\n" +
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=378674
//Can be static warning shown in the wrong places
public void test378674_comment5a() {
	runStaticWarningConformTest(
		"Test.java",
		"public class Test<T> {\n" +
		"\n" +
		"    @SuppressWarnings({ \"unchecked\", \"rawtypes\" })\n" +
		"    public static void main(String[] args) {\n" +
		"        new Test().method2(null);\n" +
		"    }\n" +
		"\n" +
		"    private static class SubClass<A> {\n" +
		"\n" +
		"    }\n" +
		"\n" +
		"    private void method2(SubClass<java.util.List<T>> s) {\n" +
		"        System.out.println(s);\n" +
		"    }\n" +
		"\n" +
		"}\n" +
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=378674
public void test378674_comment5b() {
	runStaticWarningConformTest(
		"Test.java",
		"public class Test<T> {\n" +
		"\n" +
		"    @SuppressWarnings({ \"unchecked\", \"rawtypes\" })\n" +
		"    public static void main(String[] args) {\n" +
		"        new Test().method();\n" +
		"    }\n" +
		"\n" +
		"    private java.util.Collection<T> method() {\n" +
		"        return null;\n" +
		"    }\n" +
		"\n" +
		"}\n" +
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=378674
public void test378674_comment9() {
	runStaticWarningConformTest(
		"Test.java",
		"public class Test<T> {\n" +
		"\n" +
		"    @SuppressWarnings({ \"rawtypes\" })\n" +
		"    public static void main(String[] args) {\n" +
		"        new Test().method();\n" +
		"    }\n" +
		"\n" +
		"    private java.util.Collection<? extends T> method() {\n" +
		"        return null;\n" +
		"    }\n" +
		"}\n" +
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=378674
public void test378674_comment11() {
	runStaticWarningConformTest(
		"Test.java",
		"public class Test<T> {\n" +
		"\n" +
		"    @SuppressWarnings({ \"rawtypes\" })\n" +
		"    public static void main(String[] args) {\n" +
		"        new Test().method1();\n" +
		"        new Test().method2();\n" +
		"    }\n" +
		"\n" +
		"   private <TT extends T> TT method1() { \n" +
		"		return null;\n" +
		"	}\n" +
		"\n" +
		"   private <TT extends Object & Comparable<? super T>> TT method2() { \n" +
		"		return null;\n" +
		"	}\n" +
		"}\n" +
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=378674
public void test378674_comment21a() {
	runStaticWarningConformTest(
		"X.java",
		"public class X<P extends Exception> {\n" +
		"	final <T> void foo(T x) throws P {\n" +
		"	}\n" +
		"}\n" +
		""
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=378674
public void test378674_comment21b() {
	runStaticWarningConformTest(
		"X.java",
		"""
			public class X<P extends Exception> {
				final <T> void foo(T x) {
					Object o = (P) null;
				}
			}
			"""
	);
}//https://bugs.eclipse.org/bugs/show_bug.cgi?id=378674
public void test378674_comment21c() {
	runStaticWarningConformTest(
		"X.java",
		"""
			public class X<P extends Exception> {
				final <T> void foo(T x) {
					new Outer().new Inner<P>();
				}
			}
			class Outer {
				class Inner<Q> {}
			}
			"""
	);
}//https://bugs.eclipse.org/bugs/show_bug.cgi?id=378674
public void test378674_comment21d() {
	runStaticWarningConformTest(
		"X.java",
		"""
			public class X<P extends Exception> {
				final <T> void foo(T x) {
					class Local {
						P p;
					}
				}
			}
			"""
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406396, Method can be static analysis misses a bunch of cases...
public void test406396() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.testFiles =
		new String[] {
			"X.java",
			"""
				public class X  {
					int f;
					void foo() {
						class Y {
							int p;
							{
								class Z {
									int f = p;
								}
							}
						};
					}
				}
				"""
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in X.java (at line 3)
				void foo() {
				     ^^^^^
			The method foo() from the type X can potentially be declared as static
			----------
			2. WARNING in X.java (at line 4)
				class Y {
				      ^
			The type Y is never used locally
			----------
			3. WARNING in X.java (at line 7)
				class Z {
				      ^
			The type Z is never used locally
			----------
			4. WARNING in X.java (at line 8)
				int f = p;
				    ^
			The field Z.f is hiding a field from type X
			----------
			5. WARNING in X.java (at line 8)
				int f = p;
				    ^
			The value of the field Z.f is not used
			----------
			""";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=406396, Method can be static analysis misses a bunch of cases...
public void test406396a() {
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBeStatic, CompilerOptions.ERROR);
	runner.customOptions.put(CompilerOptions.OPTION_ReportMethodCanBePotentiallyStatic, CompilerOptions.ERROR);
	runner.testFiles =
		new String[] {
			"X.java",
			"""
				public class X  {
					int f;
					int foo() {
						int f = 0;
						return f;
					}
					int goo() {
						return 0;
					}
				}
				"""
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in X.java (at line 3)
				int foo() {
				    ^^^^^
			The method foo() from the type X can potentially be declared as static
			----------
			2. WARNING in X.java (at line 4)
				int f = 0;
				    ^
			The local variable f is hiding a field from type X
			----------
			3. ERROR in X.java (at line 7)
				int goo() {
				    ^^^^^
			The method goo() from the type X can potentially be declared as static
			----------
			""";
	runner.javacTestOptions =
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
	runner.runNegativeTest();
}
public void testBug542829() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7) return;

	// m.Issing is a type that comes and goes:
	String nameMissing = "m/Issing.java";
	String contentMissing =
		"""
		package m;
		public class Issing {
			g.Ood getGood() { return new g.Ood(); }
		}
		""";

	Runner runner = new Runner();
	runner.generateOutput = true;
	runner.testFiles = new String[] {
		"g/Ood.java",
		"""
			package g;
			public class Ood {
				@Override public String toString() {
					return "good";
				}
			}
			""",
		"g/Ontainer.java",
		"""
			package g;
			import java.util.*;
			import m.Issing;
			public class Ontainer {
				public List<Issing> getElements() { return new ArrayList<>(); }
			}
			""",
		nameMissing,
		contentMissing
	};
	runner.expectedCompilerLog = null;
	runner.runConformTest();

	// now we break it:
	Util.delete(new File(OUTPUT_DIR + File.separator + "m" + File.separator + "Issing.class"));
	runner.shouldFlushOutputDirectory = false;

	// in this class file a MissingTypes attribute ("m/Issing") is generated:
	runner.testFiles = new String[] {
		"b/Roken.java",
		"""
			package b;
			import g.Ood;\
			public class Roken {
				Ood getGood(m.Issing provider) {
					return provider.getGood();
				}
			
			}
			"""
	};
	runner.expectedCompilerLog =
		"""
			----------
			1. ERROR in b\\Roken.java (at line 3)
				Ood getGood(m.Issing provider) {
				            ^^^^^^^^
			m.Issing cannot be resolved to a type
			----------
			""";
	runner.runNegativeTest();

	runner.javacTestOptions = JavacTestOptions.SKIP; // javac did not produce b/Roken.class which is needed below

	// restore the class as binary:
	runner.testFiles = new String[] {
		nameMissing,
		contentMissing
	};
	runner.expectedCompilerLog = null;
	runner.runConformTest();

	// next compilation has two references to m.Issing:
	runner.testFiles = new String[] {
		"t/Rigger.java",
		"""
			package t;
			import b.Roken;
			public class Rigger {}
			""",
		"t/Est.java",
		"""
			package t;
			public class Est {
				void foo(g.Ontainer container) {
					for (m.Issing miss: container.getElements()) {
						System.out.print(miss);
					}
				}
			}
			"""
	};
	runner.runConformTest();
}
public void testBug576735() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return;

	String path = getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "lib576735.jar";
	String[] libs = getDefaultClassPaths();
	int len = libs.length;
	System.arraycopy(libs, 0, libs = new String[len+1], 0, len);
	libs[len] = path;
	Runner runner = new Runner();
	runner.classLibraries = libs;
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_Process_Annotations, CompilerOptions.ENABLED);
	runner.testFiles = new String[] {
			"does/not/Work.java",
			"package does.not;\n" + // force this package to exist
			"public interface Work {}",
			"p/X.java",
			"""
				package p;
				import does.not.Work;
				import good.WithAnnotatedMethod;
				import does.not.ExistAnnotation;
				public class X {
					WithAnnotatedMethod field;
					void meth0(@ExistAnnotation Work d) {
					}
				}
				"""};
	runner.expectedCompilerLog =
			"""
				----------
				1. ERROR in p\\X.java (at line 1)
					package p;
					^
				The type does.not.ExistAnnotation cannot be resolved. It is indirectly referenced from required type good.WithAnnotatedMethod
				----------
				2. ERROR in p\\X.java (at line 4)
					import does.not.ExistAnnotation;
					       ^^^^^^^^^^^^^^^^^^^^^^^^
				The import does.not.ExistAnnotation cannot be resolved
				----------
				3. ERROR in p\\X.java (at line 7)
					void meth0(@ExistAnnotation Work d) {
					            ^^^^^^^^^^^^^^^
				ExistAnnotation cannot be resolved to a type
				----------
				""";
	runner.runNegativeTest();
}
}
