/*******************************************************************************
 * Copyright (c) 2017, 2018 GK Software SE, and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class Deprecated9Test extends AbstractRegressionTest9 {
	public Deprecated9Test(String name) {
		super(name);
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_9);
	}

	static {
//		TESTS_NAMES = new String[] { "test007" };
	}

	@Override
	protected INameEnvironment[] getClassLibs(boolean useDefaultClasspaths) {
		if (this.javaClassLib != null) {
			String encoding = getCompilerOptions().get(CompilerOptions.OPTION_Encoding);
			if ("".equals(encoding))
				encoding = null;
			return new INameEnvironment[] {
					this.javaClassLib,
					new FileSystem(this.classpaths, new String[]{}, // ignore initial file names
							encoding // default encoding
					)};
		}
		return super.getClassLibs(useDefaultClasspaths);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159709
	// guard variant for DeprecatedTest#test015 using an annotation
	public void test002() {
		Map<String, String> customOptions = new HashMap<>();
		customOptions.put(JavaCore.COMPILER_PB_DEPRECATION, CompilerOptions.WARNING);
		customOptions.put(JavaCore.COMPILER_PB_TERMINAL_DEPRECATION, CompilerOptions.ERROR);
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
					  @Deprecated(since="1.2",forRemoval=true)
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
				The type N1.N2 has been deprecated since version 1.2 and marked for removal
				----------
				2. ERROR in p\\M1.java (at line 4)
					a.N1.N2.N3 m = null;
					        ^^
				The type N1.N2.N3 has been deprecated and marked for removal
				----------
				3. ERROR in p\\M1.java (at line 5)
					m.foo();
					  ^^^^^
				The method foo() from the type N1.N2.N3 has been deprecated and marked for removal
				----------
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void test002binary() {
		Runner runner = new Runner();
		runner.customOptions = new HashMap<>();
		runner.customOptions.put(JavaCore.COMPILER_PB_DEPRECATION, CompilerOptions.WARNING);
		runner.customOptions.put(JavaCore.COMPILER_PB_TERMINAL_DEPRECATION, CompilerOptions.ERROR);
		runner.customOptions.put(CompilerOptions.OPTION_ReportNullReference, CompilerOptions.IGNORE);
		runner.testFiles =
			new String[] {
				"a/N1.java",
				"""
					package a;
					public class N1 {
					  @Deprecated(since="1.2",forRemoval=true)
					  public class N2 {\
					    public void foo() {}\
					    public class N3 {\
					      public void foo() {}\
					    }\
					  }\
					}
					"""
			};
		runner.runConformTest();

		runner.shouldFlushOutputDirectory = false;
		runner.testFiles =
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
					"""
			};
		runner.expectedCompilerLog =
			"""
				----------
				1. ERROR in p\\M1.java (at line 4)
					a.N1.N2.N3 m = null;
					     ^^
				The type N1.N2 has been deprecated since version 1.2 and marked for removal
				----------
				2. ERROR in p\\M1.java (at line 4)
					a.N1.N2.N3 m = null;
					        ^^
				The type N1.N2.N3 has been deprecated and marked for removal
				----------
				3. ERROR in p\\M1.java (at line 5)
					m.foo();
					  ^^^^^
				The method foo() from the type N1.N2.N3 has been deprecated and marked for removal
				----------
				""";
		runner.javacTestOptions =
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
		runner.runNegativeTest();
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=191909
	public void test004() {
		Map<String, String> customOptions = new HashMap<>();
		customOptions.put(JavaCore.COMPILER_PB_DEPRECATION, CompilerOptions.WARNING);
		customOptions.put(JavaCore.COMPILER_PB_TERMINAL_DEPRECATION, CompilerOptions.ERROR);
		this.runNegativeTest(
			true,
			new String[] {
				"test1/E01.java",
				"""
					package test1;
					public class E01 {
						@Deprecated(forRemoval=true,since="3")
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
				The field E01.x has been deprecated since version 3 and marked for removal
				----------
				2. ERROR in test1\\E02.java (at line 5)
					System.out.println(E01.y);
					                       ^
				The field E01.y has been deprecated since version 3 and marked for removal
				----------
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	// Bug 354536 - compiling package-info.java still depends on the order of compilation units
	public void test005a() {
		Map<String, String> customOptions = new HashMap<>();
		customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.WARNING);
		customOptions.put(CompilerOptions.OPTION_ReportTerminalDeprecation, CompilerOptions.ERROR);
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
				"@java.lang.Deprecated(forRemoval=true)\n" +
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
				The type X has been deprecated and marked for removal
				----------
				2. ERROR in p2\\C.java (at line 3)
					void bar(p1.X.Inner a) {
					              ^^^^^
				The type X.Inner has been deprecated and marked for removal
				----------
				3. ERROR in p2\\C.java (at line 4)
					a.foo();
					  ^^^^^
				The method foo() from the type X.Inner has been deprecated and marked for removal
				----------
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void test005b() {
		Map<String, String> customOptions = new HashMap<>();
		customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.WARNING);
		customOptions.put(CompilerOptions.OPTION_ReportTerminalDeprecation, CompilerOptions.IGNORE);
		this.runConformTest(
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
				"@java.lang.Deprecated(forRemoval=true)\n" +
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
			customOptions);
	}
	public void test005c() {
		Runner runner = new Runner();
		runner.customOptions = new HashMap<>();
		runner.customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.WARNING);
		runner.customOptions.put(CompilerOptions.OPTION_ReportTerminalDeprecation, CompilerOptions.WARNING);
		runner.testFiles =
			new String[] {
				"p1/X.java",
				"""
					package p1;
					public class X {
					    public static class Inner {
							 @java.lang.Deprecated(forRemoval=true)
					        public void foo() {}
					    }
					}
					""",
				"p1/package-info.java",
				"@java.lang.Deprecated(forRemoval=false)\n" +
				"package p1;\n",
				"p2/C.java",
				"""
					package p2;
					public class C {
						 @SuppressWarnings("deprecation")
					    void bar(p1.X.Inner a) {
					        a.foo();
					    }
					}
					""",
			};
		runner.expectedCompilerLog =
			"""
				----------
				1. WARNING in p2\\C.java (at line 5)
					a.foo();
					  ^^^^^
				The method foo() from the type X.Inner has been deprecated and marked for removal
				----------
				""";
		runner.runWarningTest();
	}
	public void test006() {
		Runner runner = new Runner();
		runner.customOptions = new HashMap<>();
		runner.customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.WARNING);
		runner.customOptions.put(CompilerOptions.OPTION_ReportTerminalDeprecation, CompilerOptions.ERROR);
		runner.testFiles =
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
					@SuppressWarnings("all") @Deprecated(since="4")
					public class E01 {
						public static int x = 5;
					}"""
			};
		runner.expectedCompilerLog =
			"""
				----------
				1. WARNING in test1\\E02.java (at line 3)
					public void foo(E01 arg) {
					                ^^^
				The type E01 is deprecated since version 4
				----------
				""";
		runner.runWarningTest();
	}
	// method overriding
	public void test007() {
		Map<String, String> customOptions = new HashMap<>();
		customOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.WARNING);
		customOptions.put(CompilerOptions.OPTION_ReportTerminalDeprecation, CompilerOptions.ERROR);
		customOptions.put(CompilerOptions.OPTION_ReportDeprecationWhenOverridingDeprecatedMethod, CompilerOptions.ENABLED);
		this.runNegativeTest(
			true,
			new String[] {
				"p1/X.java",
				"""
					package p1;
					public class X {
						 @java.lang.Deprecated(forRemoval=false)
					    public void foo() {}
						 @java.lang.Deprecated(forRemoval=true)
						 public void bar() {}
					}
					""",
				"p2/C.java",
				"""
					package p2;
					import p1.X;
					public class C extends X {
					    @Override public void foo() {}
					    @Override public void bar() {}
					}
					""",
			},
			null, customOptions,
			"""
				----------
				1. WARNING in p2\\C.java (at line 4)
					@Override public void foo() {}
					                      ^^^^^
				The method C.foo() overrides a deprecated method from X
				----------
				2. ERROR in p2\\C.java (at line 5)
					@Override public void bar() {}
					                      ^^^^^
				The method C.bar() overrides a method from X that has been deprecated and marked for removal
				----------
				""",
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError);
	}
	public void testSinceSource() {
		Runner runner = new Runner();
		runner.customOptions = new HashMap<>();
		runner.customOptions.put(JavaCore.COMPILER_PB_DEPRECATION, CompilerOptions.WARNING);
		runner.customOptions.put(JavaCore.COMPILER_PB_DEPRECATION_WHEN_OVERRIDING_DEPRECATED_METHOD, CompilerOptions.ENABLED);
		runner.customOptions.put(JavaCore.COMPILER_PB_TERMINAL_DEPRECATION, CompilerOptions.ERROR);
		runner.testFiles =
			new String[] {
				"test1/E01.java",
				"""
					package test1;
					public class E01 {
						@Deprecated(since="1.0") protected static class Old {}
						@Deprecated(since="2") public static int x = 5, y= 10;
						@Deprecated(since="3.0.0") public E01() {}
						@Deprecated(since="4-SNAPSHOT") protected void old() {}
					}""",
				"test1/E02.java",
				"""
					package test1;
					public class E02 {
						public void foo() {
							System.out.println(new E01.Old());
							E01 e = new E01();
							e.old();
							System.out.println(E01.x);
							System.out.println(E01.y);
						}
						class E03 extends E01 {
							protected void old() {}
						}
					}"""
			};
		runner.expectedCompilerLog =
			"""
				----------
				1. WARNING in test1\\E02.java (at line 4)
					System.out.println(new E01.Old());
					                       ^^^^^^^^^
				The constructor E01.Old() is deprecated since version 1.0
				----------
				2. WARNING in test1\\E02.java (at line 4)
					System.out.println(new E01.Old());
					                           ^^^
				The type E01.Old is deprecated since version 1.0
				----------
				3. WARNING in test1\\E02.java (at line 5)
					E01 e = new E01();
					            ^^^^^
				The constructor E01() is deprecated since version 3.0.0
				----------
				4. WARNING in test1\\E02.java (at line 6)
					e.old();
					  ^^^^^
				The method old() from the type E01 is deprecated since version 4-SNAPSHOT
				----------
				5. WARNING in test1\\E02.java (at line 7)
					System.out.println(E01.x);
					                       ^
				The field E01.x is deprecated since version 2
				----------
				6. WARNING in test1\\E02.java (at line 8)
					System.out.println(E01.y);
					                       ^
				The field E01.y is deprecated since version 2
				----------
				7. WARNING in test1\\E02.java (at line 10)
					class E03 extends E01 {
					      ^^^
				The constructor E01() is deprecated since version 3.0.0
				----------
				8. WARNING in test1\\E02.java (at line 11)
					protected void old() {}
					               ^^^^^
				The method E02.E03.old() overrides a method from E01 that is deprecated since version 4-SNAPSHOT
				----------
				""";
		runner.runWarningTest();
	}
	public void testSinceBinary() {
		Runner runner = new Runner();
		runner.customOptions = new HashMap<>();
		runner.customOptions.put(JavaCore.COMPILER_PB_DEPRECATION, CompilerOptions.WARNING);
		runner.customOptions.put(JavaCore.COMPILER_PB_DEPRECATION_WHEN_OVERRIDING_DEPRECATED_METHOD, CompilerOptions.ENABLED);
		runner.customOptions.put(JavaCore.COMPILER_PB_TERMINAL_DEPRECATION, CompilerOptions.ERROR);
		runner.testFiles =
			new String[] {
				"test1/E01.java",
				"""
					package test1;
					public class E01 {
						@Deprecated(since="1.0") protected static class Old {}
						@Deprecated(since="2") public static int x = 5, y= 10;
						@Deprecated(since="3.0.0") public E01() {}
						@Deprecated(since="4-SNAPSHOT") protected void old() {}
					}"""
			};
		runner.runConformTest();

		runner.shouldFlushOutputDirectory = false;
		runner.testFiles =
			new String[] {
				"test1/E02.java",
				"""
					package test1;
					public class E02 {
						public void foo() {
							System.out.println(new E01.Old());
							E01 e = new E01();
							e.old();
							System.out.println(E01.x);
							System.out.println(E01.y);
						}
						class E03 extends E01 {
							protected void old() {}
						}
					}"""
			};
		runner.expectedCompilerLog =
			"""
				----------
				1. WARNING in test1\\E02.java (at line 4)
					System.out.println(new E01.Old());
					                       ^^^^^^^^^
				The constructor E01.Old() is deprecated since version 1.0
				----------
				2. WARNING in test1\\E02.java (at line 4)
					System.out.println(new E01.Old());
					                           ^^^
				The type E01.Old is deprecated since version 1.0
				----------
				3. WARNING in test1\\E02.java (at line 5)
					E01 e = new E01();
					            ^^^^^
				The constructor E01() is deprecated since version 3.0.0
				----------
				4. WARNING in test1\\E02.java (at line 6)
					e.old();
					  ^^^^^
				The method old() from the type E01 is deprecated since version 4-SNAPSHOT
				----------
				5. WARNING in test1\\E02.java (at line 7)
					System.out.println(E01.x);
					                       ^
				The field E01.x is deprecated since version 2
				----------
				6. WARNING in test1\\E02.java (at line 8)
					System.out.println(E01.y);
					                       ^
				The field E01.y is deprecated since version 2
				----------
				7. WARNING in test1\\E02.java (at line 10)
					class E03 extends E01 {
					      ^^^
				The constructor E01() is deprecated since version 3.0.0
				----------
				8. WARNING in test1\\E02.java (at line 11)
					protected void old() {}
					               ^^^^^
				The method E02.E03.old() overrides a method from E01 that is deprecated since version 4-SNAPSHOT
				----------
				""";
		runner.runWarningTest();
	}
	public void testSinceTerminally() {
		Runner runner = new Runner();
		runner.customOptions = new HashMap<>();
		runner.customOptions.put(JavaCore.COMPILER_PB_DEPRECATION, CompilerOptions.WARNING);
		runner.customOptions.put(JavaCore.COMPILER_PB_DEPRECATION_WHEN_OVERRIDING_DEPRECATED_METHOD, CompilerOptions.ENABLED);
		runner.customOptions.put(JavaCore.COMPILER_PB_TERMINAL_DEPRECATION, CompilerOptions.ERROR);
		runner.testFiles =
			new String[] {
				"test1/E01.java",
				"""
					package test1;
					public class E01 {
						@Deprecated(since="1.0", forRemoval=true) protected static class Old {}
						@Deprecated(since="2", forRemoval=true) public static int x = 5, y= 10;
						@Deprecated(since="3.0.0", forRemoval=true) public E01() {}
						@Deprecated(since="4-SNAPSHOT", forRemoval=true) protected void old() {}
					}""",
				"test1/E02.java",
				"""
					package test1;
					public class E02 {
						public void foo() {
							System.out.println(new E01.Old());
							E01 e = new E01();
							e.old();
							System.out.println(E01.x);
							System.out.println(E01.y);
						}
						class E03 extends E01 {
							protected void old() {}
						}
					}"""
			};
		runner.expectedCompilerLog =
			"""
				----------
				1. ERROR in test1\\E02.java (at line 4)
					System.out.println(new E01.Old());
					                       ^^^^^^^^^
				The constructor E01.Old() has been deprecated since version 1.0 and marked for removal
				----------
				2. ERROR in test1\\E02.java (at line 4)
					System.out.println(new E01.Old());
					                           ^^^
				The type E01.Old has been deprecated since version 1.0 and marked for removal
				----------
				3. ERROR in test1\\E02.java (at line 5)
					E01 e = new E01();
					            ^^^^^
				The constructor E01() has been deprecated since version 3.0.0 and marked for removal
				----------
				4. ERROR in test1\\E02.java (at line 6)
					e.old();
					  ^^^^^
				The method old() from the type E01 has been deprecated since version 4-SNAPSHOT and marked for removal
				----------
				5. ERROR in test1\\E02.java (at line 7)
					System.out.println(E01.x);
					                       ^
				The field E01.x has been deprecated since version 2 and marked for removal
				----------
				6. ERROR in test1\\E02.java (at line 8)
					System.out.println(E01.y);
					                       ^
				The field E01.y has been deprecated since version 2 and marked for removal
				----------
				7. ERROR in test1\\E02.java (at line 10)
					class E03 extends E01 {
					      ^^^
				The constructor E01() has been deprecated since version 3.0.0 and marked for removal
				----------
				8. ERROR in test1\\E02.java (at line 11)
					protected void old() {}
					               ^^^^^
				The method E02.E03.old() overrides a method from E01 that has been deprecated since version 4-SNAPSHOT and marked for removal
				----------
				""";
		runner.javacTestOptions =
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError;
		runner.runNegativeTest();
	}
	public void testDeprecatedPackageExport() {
		associateToModule("mod1",
				"p1/package-info.java", "p1/C1.java",
				"p2/package-info.java", "p2/C2.java",
				"p3/package-info.java", "p3/C3.java",
				"p4/package-info.java", "p4/C4.java");
		Runner runner = new Runner();
		runner.customOptions = new HashMap<>();
		runner.customOptions.put(JavaCore.COMPILER_PB_DEPRECATION, CompilerOptions.ERROR);
		runner.customOptions.put(JavaCore.COMPILER_PB_TERMINAL_DEPRECATION, CompilerOptions.ERROR);
		runner.testFiles =
			new String[] {
				"p1/package-info.java",
				"@Deprecated package p1;\n",
				"p1/C1.java",
				"package p1; public class C1 {}\n",
				"p2/package-info.java",
				"@Deprecated(since=\"13\") package p2;\n",
				"p2/C2.java",
				"package p2; public class C2 {}\n",
				"p3/package-info.java",
				"@Deprecated(since=\"13\",forRemoval=true) package p3;\n",
				"p3/C3.java",
				"package p3; public class C3 {}\n",
				"p4/package-info.java",
				"@Deprecated(since=\"14\",forRemoval=true) package p4;\n",
				"p4/C4.java",
				"package p4; public class C4 {}\n",
				"module-info.java",
				"""
					module mod1 {
						exports p1;
						exports p2;
						exports p3;
						opens p4;
					}
					"""
			};
		runner.runConformTest();
	}
	public void testDeprecatedModule() {
		Runner runner = new Runner();
		runner.customOptions = new HashMap<>();
		runner.customOptions.put(JavaCore.COMPILER_PB_DEPRECATION, CompilerOptions.WARNING);
		runner.customOptions.put(JavaCore.COMPILER_PB_TERMINAL_DEPRECATION, CompilerOptions.ERROR);
		runner.testFiles =
			new String[] {
				"folder0/module-info.java",
				"@Deprecated module mod.dep {}\n",
				"folder1/module-info.java",
				"@Deprecated(since=\"42\") module mod.dep.since {}\n",
				"folder2/module-info.java",
				"@Deprecated(forRemoval=true) module mod.dep.terminally {}\n",
				"folder3/module-info.java",
				"@Deprecated(since=\"42\",forRemoval=true) module mod.dep.since.terminally {}\n",
				"module-info.java",
				"""
					module mod1 {
						requires mod.dep;
						requires mod.dep.since;
						requires mod.dep.terminally;
						requires mod.dep.since.terminally;
					}
					"""
			};
		runner.expectedCompilerLog =
			"""
				----------
				1. WARNING in module-info.java (at line 2)
					requires mod.dep;
					         ^^^^^^^
				The module mod.dep is deprecated
				----------
				2. WARNING in module-info.java (at line 3)
					requires mod.dep.since;
					         ^^^^^^^^^^^^^
				The module mod.dep.since is deprecated since version 42
				----------
				3. ERROR in module-info.java (at line 4)
					requires mod.dep.terminally;
					         ^^^^^^^^^^^^^^^^^^
				The module mod.dep.terminally has been deprecated and marked for removal
				----------
				4. ERROR in module-info.java (at line 5)
					requires mod.dep.since.terminally;
					         ^^^^^^^^^^^^^^^^^^^^^^^^
				The module mod.dep.since.terminally has been deprecated since version 42 and marked for removal
				----------
				""";
		runner.runNegativeTest();
	}
	public void testDeprecatedProvidedServices() {
		javacUsePathOption(" --module-source-path ");
		associateToModule("mod0", "module-info.java", "p1/IServiceDep.java", "p1/IServiceDepSince.java", "p1/IServiceTermDep.java", "p1/IServiceTermDepSince.java");
		associateToModule("mod1", "p1impl/ServiceDep.java", "p1impl/ServiceDepSince.java", "p1impl/ServiceTermDep.java", "p1impl/ServiceTermDepSince.java");
		Runner runner = new Runner();
		runner.customOptions = new HashMap<>();
		runner.customOptions.put(JavaCore.COMPILER_PB_DEPRECATION, CompilerOptions.INFO);
		runner.customOptions.put(JavaCore.COMPILER_PB_TERMINAL_DEPRECATION, CompilerOptions.WARNING);
		runner.testFiles =
			new String[] {
				"p1/IServiceDep.java",
				"""
					package p1;
					@Deprecated
					public interface IServiceDep {}
					""",
				"p1/IServiceDepSince.java",
				"""
					package p1;
					@Deprecated(since="2")
					public interface IServiceDepSince {}
					""",
				"p1/IServiceTermDep.java",
				"""
					package p1;
					@Deprecated(forRemoval=true)
					public interface IServiceTermDep {}
					""",
				"p1/IServiceTermDepSince.java",
				"""
					package p1;
					@Deprecated(since="3",forRemoval=true)
					public interface IServiceTermDepSince {}
					""",
				"module-info.java",
				"""
					module mod0 {
						exports p1;
					}
					""",
				"p1impl/ServiceDep.java",
				"""
					package p1impl;
					@Deprecated
					public class ServiceDep implements p1.IServiceDep {}
					""",
				"p1impl/ServiceDepSince.java",
				"""
					package p1impl;
					@Deprecated(since="2")
					public class ServiceDepSince implements p1.IServiceDepSince {}
					""",
				"p1impl/ServiceTermDep.java",
				"""
					package p1impl;
					@Deprecated(forRemoval=true)
					public class ServiceTermDep implements p1.IServiceTermDep {}
					""",
				"p1impl/ServiceTermDepSince.java",
				"""
					package p1impl;
					@Deprecated(since="3",forRemoval=true)
					public class ServiceTermDepSince implements p1.IServiceTermDepSince {}
					""",
				"mod1/module-info.java",
				"""
					module mod1 {
						requires mod0;
						provides p1.IServiceDep with p1impl.ServiceDep;
						provides p1.IServiceDepSince with p1impl.ServiceDepSince;
						provides p1.IServiceTermDep with p1impl.ServiceTermDep;
						provides p1.IServiceTermDepSince with p1impl.ServiceTermDepSince;
					}
					"""
			};
		runner.expectedCompilerLog =
			"""
				----------
				1. INFO in mod1\\module-info.java (at line 3)
					provides p1.IServiceDep with p1impl.ServiceDep;
					            ^^^^^^^^^^^
				The type IServiceDep is deprecated
				----------
				2. INFO in mod1\\module-info.java (at line 3)
					provides p1.IServiceDep with p1impl.ServiceDep;
					                                    ^^^^^^^^^^
				The type ServiceDep is deprecated
				----------
				3. INFO in mod1\\module-info.java (at line 4)
					provides p1.IServiceDepSince with p1impl.ServiceDepSince;
					            ^^^^^^^^^^^^^^^^
				The type IServiceDepSince is deprecated since version 2
				----------
				4. INFO in mod1\\module-info.java (at line 4)
					provides p1.IServiceDepSince with p1impl.ServiceDepSince;
					                                         ^^^^^^^^^^^^^^^
				The type ServiceDepSince is deprecated since version 2
				----------
				5. WARNING in mod1\\module-info.java (at line 5)
					provides p1.IServiceTermDep with p1impl.ServiceTermDep;
					            ^^^^^^^^^^^^^^^
				The type IServiceTermDep has been deprecated and marked for removal
				----------
				6. WARNING in mod1\\module-info.java (at line 5)
					provides p1.IServiceTermDep with p1impl.ServiceTermDep;
					                                        ^^^^^^^^^^^^^^
				The type ServiceTermDep has been deprecated and marked for removal
				----------
				7. WARNING in mod1\\module-info.java (at line 6)
					provides p1.IServiceTermDepSince with p1impl.ServiceTermDepSince;
					            ^^^^^^^^^^^^^^^^^^^^
				The type IServiceTermDepSince has been deprecated since version 3 and marked for removal
				----------
				8. WARNING in mod1\\module-info.java (at line 6)
					provides p1.IServiceTermDepSince with p1impl.ServiceTermDepSince;
					                                             ^^^^^^^^^^^^^^^^^^^
				The type ServiceTermDepSince has been deprecated since version 3 and marked for removal
				----------
				""";
		runner.runWarningTest();
	}
	public void testDeprecatedUsedServices() {
		javacUsePathOption(" --module-path ");

		associateToModule("mod0", "p1/IServiceDep.java", "p1/IServiceDepSince.java", "p1/IServiceTermDep.java", "p1/IServiceTermDepSince.java");
		Runner runner = new Runner();
		runner.customOptions = new HashMap<>();
		runner.customOptions.put(JavaCore.COMPILER_PB_DEPRECATION, CompilerOptions.INFO);
		runner.customOptions.put(JavaCore.COMPILER_PB_TERMINAL_DEPRECATION, CompilerOptions.WARNING);
		runner.testFiles =
			new String[] {
				"p1/IServiceDep.java",
				"""
					package p1;
					@Deprecated
					public interface IServiceDep {}
					""",
				"p1/IServiceDepSince.java",
				"""
					package p1;
					@Deprecated(since="2")
					public interface IServiceDepSince {}
					""",
				"p1/IServiceTermDep.java",
				"""
					package p1;
					@Deprecated(forRemoval=true)
					public interface IServiceTermDep {}
					""",
				"p1/IServiceTermDepSince.java",
				"""
					package p1;
					@Deprecated(since="3",forRemoval=true)
					public interface IServiceTermDepSince {}
					""",
				"module-info.java",
				"""
					module mod0 {
						exports p1;
					}
					""",
			};
		runner.runConformTest();

		runner.shouldFlushOutputDirectory = false;
		runner.testFiles =
			new String[] {
				"module-info.java",
				"""
					module mod2 {
						requires mod0;
						uses p1.IServiceDep;
						uses p1.IServiceDepSince;
						uses p1.IServiceTermDep;
						uses p1.IServiceTermDepSince;
					}
					"""
			};
		runner.expectedCompilerLog =
			"""
				----------
				1. INFO in module-info.java (at line 3)
					uses p1.IServiceDep;
					        ^^^^^^^^^^^
				The type IServiceDep is deprecated
				----------
				2. INFO in module-info.java (at line 4)
					uses p1.IServiceDepSince;
					        ^^^^^^^^^^^^^^^^
				The type IServiceDepSince is deprecated since version 2
				----------
				3. WARNING in module-info.java (at line 5)
					uses p1.IServiceTermDep;
					        ^^^^^^^^^^^^^^^
				The type IServiceTermDep has been deprecated and marked for removal
				----------
				4. WARNING in module-info.java (at line 6)
					uses p1.IServiceTermDepSince;
					        ^^^^^^^^^^^^^^^^^^^^
				The type IServiceTermDepSince has been deprecated since version 3 and marked for removal
				----------
				""";
		runner.runWarningTest();
	}
	public void testBug533063_1() throws Exception {
		INameEnvironment save = this.javaClassLib;
		try {
			List<String> limitModules = Arrays.asList("java.se", "jdk.xml.bind");
			this.javaClassLib = new CustomFileSystem(limitModules);
			Runner runner = new Runner();
			runner.testFiles = new String[] {
				"module-info.java",
				"""
					module my.mod {
						requires jdk.xml.bind;
					}
					"""
			};
			if (isJRE11Plus) {
				runner.expectedCompilerLog =
					"""
						----------
						1. ERROR in module-info.java (at line 2)
							requires jdk.xml.bind;
							         ^^^^^^^^^^^^
						jdk.xml.bind cannot be resolved to a module
						----------
						""";
				runner.runNegativeTest();
			} else {
				runner.expectedCompilerLog =
					"""
						----------
						1. WARNING in module-info.java (at line 2)
							requires jdk.xml.bind;
							         ^^^^^^^^^^^^
						The module jdk.xml.bind has been deprecated since version 9 and marked for removal
						----------
						""";
				runner.runWarningTest();
			}
		} finally {
			this.javaClassLib = save;
		}
	}
	public void testBug533063_2() throws Exception {
		javacUsePathOption(" --module-path ");

		runConformTest(new String[] {
			"dont.use/module-info.java",
			"@Deprecated(forRemoval=true,since=\"9\") module dont.use {}\n"
		});
		this.moduleMap.clear(); // don't use the source module beyond this point
		Runner runner = new Runner();
		runner.shouldFlushOutputDirectory = false;
		runner.testFiles = new String[] {
			"my.mod/module-info.java",
			"""
				module my.mod {
					requires dont.use;
				}
				"""
		};
		runner.expectedCompilerLog =
			"""
				----------
				1. WARNING in my.mod\\module-info.java (at line 2)
					requires dont.use;
					         ^^^^^^^^
				The module dont.use has been deprecated since version 9 and marked for removal
				----------
				""";
		runner.runWarningTest();
	}
	public void testBug534304() throws Exception {
		runNegativeTest(
			new String[] {
				"p1/C1.java",
				"""
					package p1;
					
					import pdep.Dep1;
					
					public class C1 {
						Dep1 f;
					}
					""",
				"pdep/Dep1.java",
				"""
					package pdep;
					
					import pmissing.CMissing;
					
					@Deprecated(since="13")
					@CMissing
					public class Dep1 {
					
					}
					"""
			},
			"""
				----------
				1. WARNING in p1\\C1.java (at line 3)
					import pdep.Dep1;
					       ^^^^^^^^^
				The type Dep1 is deprecated since version 13
				----------
				2. WARNING in p1\\C1.java (at line 6)
					Dep1 f;
					^^^^
				The type Dep1 is deprecated since version 13
				----------
				----------
				1. ERROR in pdep\\Dep1.java (at line 3)
					import pmissing.CMissing;
					       ^^^^^^^^
				The import pmissing cannot be resolved
				----------
				2. ERROR in pdep\\Dep1.java (at line 6)
					@CMissing
					 ^^^^^^^^
				CMissing cannot be resolved to a type
				----------
				""");
	}
	public void testBug542795() throws Exception {
		Runner runner = new Runner();
		runner.customOptions = new HashMap<>();
		runner.customOptions.put(JavaCore.COMPILER_PB_DEPRECATION, CompilerOptions.ERROR);
		runner.testFiles = new String[] {
			"test/ReaderWarningView.java",
			"""
				package test;
				@java.lang.Deprecated
				public class ReaderWarningView {}
				""",
			"Test.java",
			"public class Test implements test.Screen.Component {}\n",
			"test/Screen.java",
			"""
				package test;
				@interface Annot{ Class<?> value(); }
				@Annot(test.Screen.Component.class)
				@java.lang.Deprecated
				public final class Screen {
					@java.lang.Deprecated
					public interface Component extends test.ReaderWarningView.Component {
					}
				}
				""",
		};
		runner.expectedCompilerLog =
				"""
					----------
					1. ERROR in Test.java (at line 1)
						public class Test implements test.Screen.Component {}
						             ^^^^
					The hierarchy of the type Test is inconsistent
					----------
					2. ERROR in Test.java (at line 1)
						public class Test implements test.Screen.Component {}
						                                  ^^^^^^
					The type Screen is deprecated
					----------
					3. ERROR in Test.java (at line 1)
						public class Test implements test.Screen.Component {}
						                                         ^^^^^^^^^
					The type Screen.Component is deprecated
					----------
					----------
					1. ERROR in test\\Screen.java (at line 7)
						public interface Component extends test.ReaderWarningView.Component {
						                                   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					test.ReaderWarningView.Component cannot be resolved to a type
					----------
					""";
		runner.runNegativeTest();
	}
	public void testGH1431() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"Parent.java",
				"""
				@Deprecated(since = AbstractChild.TEST_CONSTANT) // this now fails
				public class Parent extends AbstractChild {
				    private static final String REF_OK = AbstractChild.TEST_CONSTANT; // this compiles OK
				}
				""",
				"AbstractChild.java",
				"""
				public abstract class AbstractChild implements Constants {
				    // redacted for brevity
				}
				""",
				"Constants.java",
				"""
				public interface Constants {
				    public static final String TEST_CONSTANT = "this is a test";
				}
				"""
			};
		runner.runConformTest();
	}
	public void testGH1412() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
			"AbstractClass.java",
			"""
			public abstract class AbstractClass<T> {}
			""",
			"AnnotationWithClassValue.java",
			"""
			public @interface AnnotationWithClassValue {
				Class<? extends AbstractClass<?>> value();
			}
			""",
			"ConcreteClass.java",
			"""
			//Adding @Deprecated here fixes the bug
			//@Deprecated
			public class ConcreteClass extends AbstractClass<AnnotatedClass> {}
			""",
			"AnnotatedClass.java",
			"""
			@Deprecated
			@AnnotationWithClassValue(ConcreteClass.class) //Type mismatch: cannot convert from Class<ConcreteClass> to Class<? extends AbstractClass<?>>
			public class AnnotatedClass {}
			"""
		};
		runner.runConformTest();
	}
	public static Class<?> testClass() {
		return Deprecated9Test.class;
	}
}
