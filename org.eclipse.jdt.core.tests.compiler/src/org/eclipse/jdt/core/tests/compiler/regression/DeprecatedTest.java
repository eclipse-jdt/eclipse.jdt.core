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
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;

import junit.framework.Test;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class DeprecatedTest extends AbstractRegressionTest {

static {
//	TESTS_NAMES = new String[] { "test008a" };
}

protected char[][] invisibleType;

public DeprecatedTest(String name) {
	super(name);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}

@Override
protected void tearDown() throws Exception {
	this.invisibleType = null;
	super.tearDown();
}

@Override
protected INameEnvironment getNameEnvironment(final String[] testFiles, String[] classPaths, Map<String, String> options) {
	// constructs a name environment that is able to hide a type of name 'this.invisibleType':
	this.classpaths = classPaths == null ? getDefaultClassPaths() : classPaths;
	return new InMemoryNameEnvironment(testFiles, getClassLibs(classPaths == null, options)) {
		@Override
		public NameEnvironmentAnswer findType(char[][] compoundTypeName) {
			if (DeprecatedTest.this.invisibleType != null && CharOperation.equals(DeprecatedTest.this.invisibleType, compoundTypeName))
				return null;
			return super.findType(compoundTypeName);
		}
		@Override
		public NameEnvironmentAnswer findType(char[] typeName, char[][] packageName) {
			if (DeprecatedTest.this.invisibleType != null && DeprecatedTest.this.invisibleType.length == packageName.length+1) {
				char[][] packName = CharOperation.subarray(DeprecatedTest.this.invisibleType, 0, DeprecatedTest.this.invisibleType.length-1);
				if (CharOperation.equals(packageName, packName)) {
					char[] simpleName = DeprecatedTest.this.invisibleType[DeprecatedTest.this.invisibleType.length-1];
					if (CharOperation.equals(simpleName, typeName))
						return null;
				}
			}
			return super.findType(typeName, packageName);
		}
	};
}

public void test001() {
	this.runNegativeTest(new String[] {
		"p/B.java",
		"""
			package p;
			class B extends A {
			    float x = super.x;
			}
			""",

		"p/A.java",
		"""
			package p;
			class A {
			    /** @deprecated */
			    int x = 1;
			}
			""",
	},
	"""
		----------
		1. WARNING in p\\B.java (at line 3)
			float x = super.x;
			      ^
		The field B.x is hiding a field from type A
		----------
		2. WARNING in p\\B.java (at line 3)
			float x = super.x;
			                ^
		The field A.x is deprecated
		----------
		"""
	);
}
public void test002() {
	this.runNegativeTest(new String[] {
		"p/C.java",
		"""
			package p;
			class C {
			    static int x = new A().x;
			}
			""",

		"p/A.java",
		"""
			package p;
			class A {
			    /** @deprecated */
			    int x = 1;
			}
			""",

	},
		"""
			----------
			1. WARNING in p\\C.java (at line 3)
				static int x = new A().x;
				                       ^
			The field A.x is deprecated
			----------
			"""
	);
}
public void test003() {
	this.runNegativeTest(new String[] {
		"p/Top.java",
		"""
			package p;
			public class Top {
			 \s
			  class M1 {
			    class M2 {}
			  };
			 \s
			  static class StaticM1 {
			    static class StaticM2 {
			      class NonStaticM3{}};
			  };
			 \s
			public static void main(String argv[]){
			  Top tip = new Top();
			  System.out.println("Still alive 0");
			  tip.testStaticMember();
			  System.out.println("Still alive 1");
			  tip.testStaticMember1();
			  System.out.println("Still alive 2");
			  tip.testStaticMember2();
			  System.out.println("Still alive 3");
			  tip.testStaticMember3();
			  System.out.println("Still alive 4");
			  tip.testStaticMember4();
			  System.out.println("Completed");
			}
			  void testMember(){
			    new M1().new M2();}
			  void testStaticMember(){
			    new StaticM1().new StaticM2();}
			  void testStaticMember1(){
			    new StaticM1.StaticM2();}
			  void testStaticMember2(){
			    new StaticM1.StaticM2().new NonStaticM3();}
			  void testStaticMember3(){
			    // define an anonymous subclass of the non-static M3
			    new StaticM1.StaticM2().new NonStaticM3(){};
			  }  \s
			  void testStaticMember4(){
			    // define an anonymous subclass of the non-static M3
			    new StaticM1.StaticM2().new NonStaticM3(){
			      Object hello(){
			        return new StaticM1.StaticM2().new NonStaticM3();
			      }};
			     \s
			  }   \s
			}
			""",
		},
		"""
			----------
			1. ERROR in p\\Top.java (at line 30)
				new StaticM1().new StaticM2();}
				^^^^^^^^^^^^^^
			Illegal enclosing instance specification for type Top.StaticM1.StaticM2
			----------
			2. WARNING in p\\Top.java (at line 42)
				Object hello(){
				       ^^^^^^^
			The method hello() from the type new Top.StaticM1.StaticM2.NonStaticM3(){} is never used locally
			----------
			""");
}
/**
 * Regression test for PR #1G9ES9B
 */
public void test004() {
	this.runNegativeTest(new String[] {
		"p/Warning.java",
		"""
			package p;
			import java.util.Date;
			public class Warning {
			public Warning() {
			     super();
			     Date dateObj = new Date();
			     dateObj.UTC(1,2,3,4,5,6);
			}
			}
			""",
		},
		"""
			----------
			1. WARNING in p\\Warning.java (at line 7)
				dateObj.UTC(1,2,3,4,5,6);
				^^^^^^^^^^^^^^^^^^^^^^^^
			The static method UTC(int, int, int, int, int, int) from the type Date should be accessed in a static way
			----------
			2. WARNING in p\\Warning.java (at line 7)
				dateObj.UTC(1,2,3,4,5,6);
				        ^^^^^^^^^^^^^^^^
			The method UTC(int, int, int, int, int, int) from the type Date is deprecated
			----------
			""");
}
public void test005() {
	this.runConformTest(
		new String[] {
			"X.java",
		  """
			public class X {
			/**
			 * @deprecated
			 */
			 	public static class Y {
				}
			   public static void main(String[] args) {\t
			        System.out.print("SUCCESS");\t
				}\t
			}"""
		},
		"SUCCESS", // expected output
		null,
		true, // flush previous output dir content
		null, // special vm args
		null,  // custom options
		null); // custom requestor
	this.runNegativeTest(
		new String[] {
			"A.java",
			"public class A extends X.Y {}"
		},
		"""
			----------
			1. WARNING in A.java (at line 1)
				public class A extends X.Y {}
				                         ^
			The type X.Y is deprecated
			----------
			""",// expected output
		null,
		false, // flush previous output dir content
		null);  // custom options
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=40839
public void test006() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					/**
					  @deprecated
					 */
					; // line comment
					static int i;
				   public static void main(String[] args) {\t
				        System.out.print("SUCCESS");\t
					}\t
				}"""
		},
		"SUCCESS", // expected output
		null,
		true, // flush previous output dir content
		null, // special vm args
		null,  // custom options
		null); // custom requestor
		runConformTest(
			// test directory preparation
	 		false /* do not flush output directory */,
			new String[] { /* test files */
				"A.java",
				"""
					public class A {
					   public static void main(String[] args) {\t
					        System.out.print(X.i);\t
						}\t
					}"""
			},
			// compiler results
			"" /* expected compiler log */,
			// runtime results
			"0" /* expected output string */,
			"" /* expected error string */,
			// javac options
			JavacTestOptions.DEFAULT /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=88124
public void test007() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				/**
				 * @deprecated
				 */
				public class X {
				}
				""",

			"Y.java",
			"""
				/**
				 * @deprecated
				 */
				public class Y {
				  Zork z;
				  X x;
				  X foo() {
				    X x; // unexpected deprecated warning here
				  }
				}
				""",
		},
		"""
			----------
			1. ERROR in Y.java (at line 5)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			2. WARNING in Y.java (at line 8)
				X x; // unexpected deprecated warning here
				  ^
			The local variable x is hiding a field from type Y
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=88124 - variation
public void test008() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				/**
				 * @deprecated
				 */
				public class X {
				}
				""",
		},
		"");
	this.runNegativeTest(
		new String[] {
			"Y.java",
			"""
				/**
				 * @deprecated
				 */
				public class Y {
				  Zork z;
				  void foo() {
				    X x; // unexpected deprecated warning here
				  }
				}
				""",
		},
		"""
			----------
			1. ERROR in Y.java (at line 5)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			""",// expected output
		null,
		false, // flush previous output dir content
		null);  // custom options
}
// variation of test008 on behalf of Bug 526335 - [9][hovering] Deprecation warning should show the new 'since' deprecation value
// verify that we don't attempt to access java.lang.Deprecated in a 1.4 based compilation.
public void test008a() throws IOException {
	String jarPath = LIB_DIR+File.separator+"p008a"+File.separator+"x.jar";
	Util.createJar(new String[] {
			"X.java",
			"""
				package p008a;
				@Deprecated
				public class X {
				}
				""",
		},
		jarPath,
		"1.5");

	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"Y.java",
			"""
				public class Y {
				  void foo() {
				    p008a.X x;
				  }
				}
				""",
		};
	String[] libs = getDefaultClassPaths();
	libs = Arrays.copyOf(libs, libs.length+1);
	libs[libs.length-1] = jarPath;
	runner.classLibraries = libs;
	runner.expectedCompilerLog =
		"""
			----------
			1. WARNING in Y.java (at line 3)
				p008a.X x;
				      ^
			The type X is deprecated
			----------
			""";
	if (this.complianceLevel < ClassFileConstants.JDK1_5) {
		// simulate we were running on a JRE without java.lang.Deprecated
		this.invisibleType = TypeConstants.JAVA_LANG_DEPRECATED;
	}
	runner.runWarningTest();
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=88124 - variation
public void test009() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				/**
				 * @deprecated
				 */
				public class X {
				}
				""",

			"Y.java",
			"""
				/**
				 * @deprecated
				 */
				public class Y {
				  Zork z;
				  void foo() {
				    X x; // unexpected deprecated warning here
				  }
				}
				""",
		},
		"""
			----------
			1. ERROR in Y.java (at line 5)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=88187
public void test010() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.ENABLED);
	this.runNegativeTest(
		new String[] {
            "X.java",
            """
				/**
				 * @deprecated
				 */
				public class X {
				        /**
				         * @see I2#foo()
				         */
				        I1 foo() {
				                return null;
				        }
				       Zork z;
				}
				""",
			"I1.java",
			"""
				/**
				 * @deprecated
				 */
				public interface I1 {
						 // empty block
				}
				""",
			"I2.java",
			"""
				/**
				 * @deprecated
				 */
				public interface I2 {
						 I1 foo(); // unexpected warning here
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 11)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			""",
		null,
		true,
		customOptions);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=123522
public void test011() {
	this.runNegativeTest(
		new String[] {
				"p1/X.java", // =================
				"""
					package p1;
					import p2.I;
					/** @deprecated */
					public class X {
						Zork z;
					}
					""", // =================
				"p2/I.java", // =================
				"""
					package p2;
					/** @deprecated */
					public interface I {
					}
					""", // =================
		},
		"""
			----------
			1. ERROR in p1\\X.java (at line 5)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			""");
}

// @deprecated upon locals do not influence the deprecation diagnostic
// JLS3 9.6
public void test012() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportDeprecation,
		CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportDeprecationInDeprecatedCode,
		CompilerOptions.IGNORE);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
            "X.java",
			"""
				public class X {
				    void foo() {
				        /** @deprecated */
				        int i1 = Y.m;
				    }
				    /** @deprecated */
				    void bar() {
				        int i1 = Y.m;
				    }
				}
				""",
            "Y.java",
			"""
				public class Y {
				    /** @deprecated */
				    static int m;
				}
				""",	},
			// compiler options
			null /* no class libraries */,
			customOptions /* custom options */,
			"""
				----------
				1. ERROR in X.java (at line 4)
					int i1 = Y.m;
					           ^
				The field Y.m is deprecated
				----------
				""",
			// javac options
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}

// @deprecated upon locals do not influence the deprecation diagnostic
// JLS3 9.6
// @Deprecated variant
public void test013() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_ReportDeprecation,
			CompilerOptions.ERROR);
		customOptions.put(CompilerOptions.OPTION_ReportDeprecationInDeprecatedCode,
			CompilerOptions.IGNORE);
		runNegativeTest(
			// test directory preparation
			true /* flush output directory */,
			new String[] { /* test files */
	            "X.java",
				"""
					public class X {
					    void foo() {
					        @Deprecated
					        int i1 = Y.m;
					    }
					    @Deprecated
					    void bar() {
					        int i1 = Y.m;
					    }
					}
					""",
	            "Y.java",
				"""
					public class Y {
					    @Deprecated
					    static int m;
					}
					""",
			},
			// compiler options
			null /* no class libraries */,
			customOptions /* custom options */,
			"""
				----------
				1. ERROR in X.java (at line 4)
					int i1 = Y.m;
					           ^
				The field Y.m is deprecated
				----------
				""",
			// javac options
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159243
public void test014() {
	this.runNegativeTest(
		new String[] {
			"p/X.java",
			"""
				package p;
				/**
				 * @deprecated
				 */
				public class X {
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
			The type X is deprecated
			----------
			2. ERROR in Y.java (at line 3)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			3. WARNING in Y.java (at line 5)
				X x;
				^
			The type X is deprecated
			----------
			4. WARNING in Y.java (at line 6)
				X[] xs = { x };
				^
			The type X is deprecated
			----------
			5. WARNING in Y.java (at line 9)
				p.X x;
				  ^
			The type X is deprecated
			----------
			6. WARNING in Y.java (at line 10)
				p.X[] xs = { x };
				  ^
			The type X is deprecated
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159709
// the order of the CUs must not modify the behavior, see also test016
public void test015() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.IGNORE);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
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
				  /** @deprecated */
				  public class N2 {\
				    public class N3 {\
				      public void foo() {}\
				    }\
				  }\
				}
				""",
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
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
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);

}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159709
public void test016() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.IGNORE);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"a/N1.java",
			"""
				package a;
				public class N1 {
				  /** @deprecated */
				  public class N2 {\
				    public class N3 {\
				      public void foo() {}\
				    }\
				  }\
				}
				""",
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
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
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
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);

}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159709
// variant: self-contained case, hence no report
public void test017() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportDeprecationInDeprecatedCode, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"a/N1.java",
			"""
				package a;
				public class N1 {
				  /** @deprecated */
				  public class N2 {\
				    public class N3 {\
				      public void foo() {}\
				    }\
				  }\
				  void bar() {
				    a.N1.N2.N3 m = null;
				    m.foo();
				  }
				}
				"""
		},
		"",
		null,
		true,
		null,
		customOptions,
		null,
		false);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159709
// variant: using a binary class
// **
public void test018() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportDeprecationInDeprecatedCode, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.IGNORE);
	this.runConformTest(
		new String[] {
			"a/N1.java",
			"""
				package a;
				public class N1 {
				  /** @deprecated */
				  public class N2 {\
				    public class N3 {\
				      public void foo() {}\
				    }\
				  }\
				}
				"""
		},
		"",
		null,
		true,
		null,
		customOptions,
		null,
		false);
	runNegativeTest(
		// test directory preparation
		false /* do not flush output directory */,
		new String[] { /* test files */
			"p/M1.java",
			"""
				package p;
				public class M1 {
				  void bar() {
				    a.N1.N2.N3 m = null;
				    m.foo();
				  }
				}
				"""
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
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
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=191909 (1.4 variant)
public void test019() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"test1/E01.java",
			"""
				package test1;
				public class E01 {
					/** @deprecated */
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
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
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
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=300031
public void test020() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"a.b.c.d/Deprecated.java",
			"""
				package a.b.c.d;
				public class Deprecated {
					/** @deprecated */
					public class Inner {
						/** @deprecated */
						public class Inn {
						}
					}
					/** @deprecated */
					public Deprecated foo(){ return null;}
					/** @deprecated */
					public Deprecated goo(){ return null;}
					/** @deprecated */
					public static Deprecated bar(){ return null;}
				}
				""",
			"a.b.c.d.e/T.java",
			"""
				package a.b.c.d.e;
				import a.b.c.d.Deprecated;
				public class T {
					a.b.c.d.Deprecated f;
					a.b.c.d.Deprecated.Inner.Inn g;
					Deprecated.Inner i;
					public void m() {
						f.foo().goo();
						a.b.c.d.Deprecated.bar();
					}
				}"""
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		"""
			----------
			1. ERROR in a.b.c.d.e\\T.java (at line 5)
				a.b.c.d.Deprecated.Inner.Inn g;
				                   ^^^^^
			The type Deprecated.Inner is deprecated
			----------
			2. ERROR in a.b.c.d.e\\T.java (at line 5)
				a.b.c.d.Deprecated.Inner.Inn g;
				                         ^^^
			The type Deprecated.Inner.Inn is deprecated
			----------
			3. ERROR in a.b.c.d.e\\T.java (at line 6)
				Deprecated.Inner i;
				           ^^^^^
			The type Deprecated.Inner is deprecated
			----------
			4. ERROR in a.b.c.d.e\\T.java (at line 8)
				f.foo().goo();
				  ^^^^^
			The method foo() from the type Deprecated is deprecated
			----------
			5. ERROR in a.b.c.d.e\\T.java (at line 8)
				f.foo().goo();
				        ^^^^^
			The method goo() from the type Deprecated is deprecated
			----------
			6. ERROR in a.b.c.d.e\\T.java (at line 9)
				a.b.c.d.Deprecated.bar();
				                   ^^^^^
			The method bar() from the type Deprecated is deprecated
			----------
			""",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
public static Class testClass() {
	return DeprecatedTest.class;
}
}
