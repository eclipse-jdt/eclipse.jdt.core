/*******************************************************************************
 * Copyright (c) 2006, 2014 IBM Corporation and others.
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

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import java.util.Map;

import junit.framework.Test;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class SuperTypeTest extends AbstractRegressionTest {

	public SuperTypeTest(String name) {
		super(name);
	}
	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "test000" };
//		TESTS_NUMBERS = new int[] { 42, 43, 44 };
//		TESTS_RANGE = new int[] { 11, -1 };
	}

	public static Test suite() {
		return buildAllCompliancesTestSuite(testClass());
	}

	public static Class testClass() {
		return SuperTypeTest.class;
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=136106
	 */
	public void test001() {
		this.runConformTest(
			new String[] {
				/* org.eclipse.curiosity.A */
				"org/eclipse/curiosity/A.java",
				"""
					package org.eclipse.curiosity;
					public abstract class A implements InterfaceA {
						private void e() {
						}
						public void f() {
							this.e();
						}
					}""",
				/* org.eclipse.curiosity.InterfaceA */
				"org/eclipse/curiosity/InterfaceA.java",
				"package org.eclipse.curiosity;\n" +
				"public interface InterfaceA extends InterfaceBase {}\n",
				"org/eclipse/curiosity/InterfaceBase.java",
				"""
					package org.eclipse.curiosity;
					public interface InterfaceBase {
					    public void a();
					    public void b();
					    public void c();
					    public void d();
					}"""
			}
		);
	}
// was Compliance_1_x#test001
public void test002() {
	String[] sources = new String[] {
		"p1/Test.java",
		"""
			package p1;\s
			public class Test {\s
				public static void main(String[] arguments) {\s
					new Test().foo();\s
				}\s
				class M {\s
				}\s
				void foo(){\s
					class Y extends Secondary {\s
						M m;\s
					};\s
					System.out.println("SUCCESS");\t
				}\s
			}\s
			class Secondary {\s
				class M {}\s
			}\s
			"""
	};
	if (this.complianceLevel == ClassFileConstants.JDK1_3) {
		runNegativeTest(
			sources,
			"""
				----------
				1. ERROR in p1\\Test.java (at line 10)
					M m;\s
					^
				The type M is defined in an inherited type and an enclosing scope
				----------
				""");
	} else {
		runConformTest(
			sources,
			"SUCCESS");
	}
}

// was Compliance_1_x#test002
public void test003() {
	String[] sources = new String[] {
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
					class Y extends Secondary {\s
						String z = bar();\t
					};\s
					System.out.println(new Y().z);\t
				}\s
			}\s
			class Secondary {\s
				String bar(){ return "SUCCESS"; }\s
			}\s
			"""
	};
	if (this.complianceLevel == ClassFileConstants.JDK1_3) {
		runNegativeTest(
			sources,
			"""
				----------
				1. ERROR in p1\\Test.java (at line 11)
					String z = bar();\t
					           ^^^
				The method bar is defined in an inherited type and an enclosing scope
				----------
				""");
	} else {
		runConformTest(
			sources,
			"SUCCESS");
	}
}

// was Compliance_1_x#test003
public void test004() {
	String[] sources = new String[] {
		"p1/Test.java",
		"""
			package p1;\s
			public class Test {\s
				public static void main(String[] arguments) {\s
					new Test().foo();\s
				}\s
				String bar = "FAILED";\
				void foo(){\s
					class Y extends Secondary {\s
						String z = bar;\s
					};\s
					System.out.println(new Y().z);\t
				}\s
			}\s
			class Secondary {\s
				String bar = "SUCCESS";\s
			}\s
			"""
	};
	if (this.complianceLevel == ClassFileConstants.JDK1_3) {
		runNegativeTest(
			sources,
			"""
				----------
				1. ERROR in p1\\Test.java (at line 8)
					String z = bar;\s
					           ^^^
				The field bar is defined in an inherited type and an enclosing scope\s
				----------
				""");
	} else {
		runConformTest(
			sources,
			"SUCCESS");
	}
}

// was Compliance_1_x#test004
public void test005() {
	this.runConformTest(
		new String[] {
			"p1/Test.java",
			"""
				package p1;\s
				public class Test {\s
					public static void main(String[] arguments) {\s
						new Test().foo();\s
					}\s
					String bar() {\s
						return "SUCCESS";\t
					}\s
					void foo(){\s
						class Y extends Secondary {\s
							String z = bar();\t
						};\s
						System.out.println(new Y().z);\t
					}\s
				}\s
				class Secondary {\s
					private String bar(){ return "FAILED"; }\s
				}\s
				"""
		},
		"SUCCESS");
}

// was Compliance_1_x#test005
public void test006() {
	this.runConformTest(
		new String[] {
			"p1/Test.java",
			"""
				package p1;\s
				public class Test {\s
					public static void main(String[] arguments) {\s
						new Test().foo();\s
					}\s
					String bar = "SUCCESS";\
					void foo(){\s
						class Y extends Secondary {\s
							String z = bar;\s
						};\s
						System.out.println(new Y().z);\t
					}\s
				}\s
				class Secondary {\s
					private String bar = "FAILED";\s
				}\s
				"""
		},
		"SUCCESS");
}

// was Compliance_1_x#test006
public void test007() {
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
						class Y extends Secondary {\s
							String z = bar();\t
						};\s
						System.out.println(new Y().z);\t
					}\s
				}\s
				class Secondary {\s
					String bar(int i){ return "SUCCESS"; }\s
				}\s
				"""
		},
		"""
			----------
			1. ERROR in p1\\Test.java (at line 11)
				String z = bar();\t
				           ^^^
			The method bar(int) in the type Secondary is not applicable for the arguments ()
			----------
			"""
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=77918
// default is silent
public void test008() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X implements I {}
				class Y extends X implements I, J {}\
				interface I {}
				interface J {}
				"""
		},
		""
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=77918
// raising an error
public void test009() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRedundantSuperinterface,  CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X implements I {}
				class Y extends X implements I, J {}
				interface I {}
				interface J {}
				"""
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 2)\n" +
		"	class Y extends X implements I, J {}\n" +
		"	                             ^\n" +
		"Redundant superinterface I for the type Y, already defined by X\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=77918
// raising an error - deeper hierarchy
public void test010() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRedundantSuperinterface, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X implements I {}
				class Y extends X {}
				class Z extends Y implements J, I {}
				interface I {}
				interface J {}
				"""
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 3)\n" +
		"	class Z extends Y implements J, I {}\n" +
		"	                                ^\n" +
		"Redundant superinterface I for the type Z, already defined by X\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=77918
// no error - deeper hierarchy
public void test011() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRedundantSuperinterface,  CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X implements I {}
				class Y extends X {}
				class Z extends Y implements J {}\
				interface I {}
				interface J {}
				"""
		},
		"",
		null /* no extra class libraries */,
		true /* flush output directory */,
		null /* no vm arguments */,
		customOptions,
		null /* no custom requestor*/);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=77918
// error - extending interfaces
public void test012() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRedundantSuperinterface,  CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X implements J {}
				class Y extends X implements I {}
				interface I {}
				interface J extends I {}
				"""
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		// compiler results
		"----------\n" + /* expected compiler log */
		"1. ERROR in X.java (at line 2)\n" +
		"	class Y extends X implements I {}\n" +
		"	                             ^\n" +
		"Redundant superinterface I for the type Y, already defined by J\n" +
		"----------\n",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=288749
public void test013() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRedundantSuperinterface,  CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				import java.util.*;
				interface X<E> extends List<E>, Collection<E>, Iterable<E> {}
				interface Y<E> extends Collection<E>, List<E> {}
				interface XXX<E> extends Iterable<E>, List<E>, Collection<E> {}
				abstract class Z implements List<Object>, Collection<Object> {}
				abstract class ZZ implements Collection<Object>, List<Object> {}"""
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		"""
			----------
			1. ERROR in X.java (at line 2)
				interface X<E> extends List<E>, Collection<E>, Iterable<E> {}
				                                ^^^^^^^^^^
			Redundant superinterface Collection<E> for the type X<E>, already defined by List<E>
			----------
			2. ERROR in X.java (at line 2)
				interface X<E> extends List<E>, Collection<E>, Iterable<E> {}
				                                               ^^^^^^^^
			Redundant superinterface Iterable<E> for the type X<E>, already defined by List<E>
			----------
			3. ERROR in X.java (at line 3)
				interface Y<E> extends Collection<E>, List<E> {}
				                       ^^^^^^^^^^
			Redundant superinterface Collection<E> for the type Y<E>, already defined by List<E>
			----------
			4. ERROR in X.java (at line 4)
				interface XXX<E> extends Iterable<E>, List<E>, Collection<E> {}
				                         ^^^^^^^^
			Redundant superinterface Iterable<E> for the type XXX<E>, already defined by List<E>
			----------
			5. ERROR in X.java (at line 4)
				interface XXX<E> extends Iterable<E>, List<E>, Collection<E> {}
				                                               ^^^^^^^^^^
			Redundant superinterface Collection<E> for the type XXX<E>, already defined by List<E>
			----------
			6. ERROR in X.java (at line 5)
				abstract class Z implements List<Object>, Collection<Object> {}
				                                          ^^^^^^^^^^
			Redundant superinterface Collection<Object> for the type Z, already defined by List<Object>
			----------
			7. ERROR in X.java (at line 6)
				abstract class ZZ implements Collection<Object>, List<Object> {}
				                             ^^^^^^^^^^
			Redundant superinterface Collection<Object> for the type ZZ, already defined by List<Object>
			----------
			""",
		JavacTestOptions.SKIP);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=288749
public void test014() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRedundantSuperinterface,  CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"public class X implements I, J {}",
			"I.java",
			"public interface I {}",
			"J.java",
			"public interface J extends I {}"
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		"""
			----------
			1. ERROR in X.java (at line 1)
				public class X implements I, J {}
				                          ^
			Redundant superinterface I for the type X, already defined by J
			----------
			""",
		JavacTestOptions.SKIP);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=320911 (as is)
public void test015() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRedundantSuperinterface,  CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				interface IVerticalRulerColumn {}
				interface IVerticalRulerInfo {}
				interface IVerticalRulerInfoExtension {}
				interface IChangeRulerColumn extends IVerticalRulerColumn, IVerticalRulerInfoExtension {}
				interface IRevisionRulerColumn extends IVerticalRulerColumn, IVerticalRulerInfo, IVerticalRulerInfoExtension {}
				public final class X implements IVerticalRulerColumn, IVerticalRulerInfo, IVerticalRulerInfoExtension, IChangeRulerColumn, IRevisionRulerColumn {}
				"""
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		"""
			----------
			1. ERROR in X.java (at line 6)
				public final class X implements IVerticalRulerColumn, IVerticalRulerInfo, IVerticalRulerInfoExtension, IChangeRulerColumn, IRevisionRulerColumn {}
				                                ^^^^^^^^^^^^^^^^^^^^
			Redundant superinterface IVerticalRulerColumn for the type X, already defined by IChangeRulerColumn
			----------
			2. ERROR in X.java (at line 6)
				public final class X implements IVerticalRulerColumn, IVerticalRulerInfo, IVerticalRulerInfoExtension, IChangeRulerColumn, IRevisionRulerColumn {}
				                                                      ^^^^^^^^^^^^^^^^^^
			Redundant superinterface IVerticalRulerInfo for the type X, already defined by IRevisionRulerColumn
			----------
			3. ERROR in X.java (at line 6)
				public final class X implements IVerticalRulerColumn, IVerticalRulerInfo, IVerticalRulerInfoExtension, IChangeRulerColumn, IRevisionRulerColumn {}
				                                                                          ^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Redundant superinterface IVerticalRulerInfoExtension for the type X, already defined by IChangeRulerColumn
			----------
			""",
		JavacTestOptions.SKIP);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=320911 (variation)
public void test016() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRedundantSuperinterface,  CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				interface IVerticalRulerColumn {}
				interface IVerticalRulerInfo {}
				interface IVerticalRulerInfoExtension {}
				interface IChangeRulerColumn extends IVerticalRulerColumn, IVerticalRulerInfoExtension {}
				interface IRevisionRulerColumn extends IVerticalRulerColumn, IVerticalRulerInfo, IVerticalRulerInfoExtension {}
				class Z implements IChangeRulerColumn {}
				class Y extends Z implements IRevisionRulerColumn {}
				public final class X extends Y implements IVerticalRulerColumn, IVerticalRulerInfo, IVerticalRulerInfoExtension {}
				"""
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		"""
			----------
			1. ERROR in X.java (at line 8)
				public final class X extends Y implements IVerticalRulerColumn, IVerticalRulerInfo, IVerticalRulerInfoExtension {}
				                                          ^^^^^^^^^^^^^^^^^^^^
			Redundant superinterface IVerticalRulerColumn for the type X, already defined by IRevisionRulerColumn
			----------
			2. ERROR in X.java (at line 8)
				public final class X extends Y implements IVerticalRulerColumn, IVerticalRulerInfo, IVerticalRulerInfoExtension {}
				                                                                ^^^^^^^^^^^^^^^^^^
			Redundant superinterface IVerticalRulerInfo for the type X, already defined by IRevisionRulerColumn
			----------
			3. ERROR in X.java (at line 8)
				public final class X extends Y implements IVerticalRulerColumn, IVerticalRulerInfo, IVerticalRulerInfoExtension {}
				                                                                                    ^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Redundant superinterface IVerticalRulerInfoExtension for the type X, already defined by IRevisionRulerColumn
			----------
			""",
		JavacTestOptions.SKIP);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=320911 (variation)
public void test017() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRedundantSuperinterface,  CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				interface IVerticalRulerColumn {}
				interface IVerticalRulerInfo {}
				interface IVerticalRulerInfoExtension {}
				interface IChangeRulerColumn extends IVerticalRulerColumn, IVerticalRulerInfoExtension {}
				interface IRevisionRulerColumn extends IVerticalRulerColumn, IVerticalRulerInfo, IVerticalRulerInfoExtension {}
				class Z implements IRevisionRulerColumn{}
				class C extends Z {}
				class B extends C implements IChangeRulerColumn {}
				class H extends B {}
				class Y extends H  {}
				public final class X extends Y implements IVerticalRulerColumn, IVerticalRulerInfo, IVerticalRulerInfoExtension {}
				"""
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		"""
			----------
			1. ERROR in X.java (at line 11)
				public final class X extends Y implements IVerticalRulerColumn, IVerticalRulerInfo, IVerticalRulerInfoExtension {}
				                                          ^^^^^^^^^^^^^^^^^^^^
			Redundant superinterface IVerticalRulerColumn for the type X, already defined by IRevisionRulerColumn
			----------
			2. ERROR in X.java (at line 11)
				public final class X extends Y implements IVerticalRulerColumn, IVerticalRulerInfo, IVerticalRulerInfoExtension {}
				                                                                ^^^^^^^^^^^^^^^^^^
			Redundant superinterface IVerticalRulerInfo for the type X, already defined by IRevisionRulerColumn
			----------
			3. ERROR in X.java (at line 11)
				public final class X extends Y implements IVerticalRulerColumn, IVerticalRulerInfo, IVerticalRulerInfoExtension {}
				                                                                                    ^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Redundant superinterface IVerticalRulerInfoExtension for the type X, already defined by IRevisionRulerColumn
			----------
			""",
		JavacTestOptions.SKIP);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=320911 (variation)
public void test018() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportRedundantSuperinterface,  CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				interface IVerticalRulerColumn {}
				interface IVerticalRulerInfo {}
				interface IVerticalRulerInfoExtension {}
				interface IChangeRulerColumn extends IVerticalRulerColumn, IVerticalRulerInfoExtension {}
				interface IRevisionRulerColumn extends IVerticalRulerColumn, IVerticalRulerInfo, IVerticalRulerInfoExtension {}
				class Z implements IVerticalRulerInfoExtension {}
				class C extends Z {}
				class B extends C implements IChangeRulerColumn {}
				class H extends B implements IVerticalRulerInfo {}
				class Y extends H  implements IVerticalRulerColumn {}
				public final class X extends Y implements IVerticalRulerColumn, IVerticalRulerInfo, IVerticalRulerInfoExtension {}
				"""
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		"""
			----------
			1. ERROR in X.java (at line 10)
				class Y extends H  implements IVerticalRulerColumn {}
				                              ^^^^^^^^^^^^^^^^^^^^
			Redundant superinterface IVerticalRulerColumn for the type Y, already defined by IChangeRulerColumn
			----------
			2. ERROR in X.java (at line 11)
				public final class X extends Y implements IVerticalRulerColumn, IVerticalRulerInfo, IVerticalRulerInfoExtension {}
				                                          ^^^^^^^^^^^^^^^^^^^^
			Redundant superinterface IVerticalRulerColumn for the type X, already defined by Y
			----------
			3. ERROR in X.java (at line 11)
				public final class X extends Y implements IVerticalRulerColumn, IVerticalRulerInfo, IVerticalRulerInfoExtension {}
				                                                                ^^^^^^^^^^^^^^^^^^
			Redundant superinterface IVerticalRulerInfo for the type X, already defined by H
			----------
			4. ERROR in X.java (at line 11)
				public final class X extends Y implements IVerticalRulerColumn, IVerticalRulerInfo, IVerticalRulerInfoExtension {}
				                                                                                    ^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Redundant superinterface IVerticalRulerInfoExtension for the type X, already defined by Z
			----------
			""",
		JavacTestOptions.SKIP);
}
}
