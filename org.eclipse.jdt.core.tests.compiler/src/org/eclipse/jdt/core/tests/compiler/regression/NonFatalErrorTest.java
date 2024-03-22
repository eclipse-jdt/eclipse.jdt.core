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

import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class NonFatalErrorTest extends AbstractRegressionTest {
	public NonFatalErrorTest(String name) {
		super(name);
	}
	// Static initializer to specify tests subset using TESTS_* static variables
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_NAMES = new String[] { "test127" };
//		TESTS_NUMBERS = new int[] { 7 };
//		TESTS_RANGE = new int[] { 169, 180 };
	}

	public static Test suite() {
		return buildAllCompliancesTestSuite(testClass());
	}

	public static Class testClass() {
		return NonFatalErrorTest.class;
	}

	public void test001() {
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_FatalOptionalError, CompilerOptions.DISABLED);
		customOptions.put(CompilerOptions.OPTION_ReportUnusedImport, CompilerOptions.ERROR);
		runNegativeTest(
			// test directory preparation
			true /* flush output directory */,
			new String[] { /* test files */
				"X.java",
				"""
					import java.util.*;
					
					public class X {
							 public static void main(String argv[]) {
									 System.out.println("SUCCESS");
							 }
					}"""
			},
			// compiler options
			null /* no class libraries */,
			customOptions /* custom options */,
			"""
				----------
				1. ERROR in X.java (at line 1)
					import java.util.*;
					       ^^^^^^^^^
				The import java.util is never used
				----------
				""",
			// runtime results
			"SUCCESS" /* expected output string */,
			null /* do not check error string */,
			// javac options
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
	}

	public void test002() {
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_FatalOptionalError, CompilerOptions.ENABLED);
		customOptions.put(CompilerOptions.OPTION_ReportUnusedImport, CompilerOptions.ERROR);
		runNegativeTest(
			// test directory preparation
			true /* flush output directory */,
			new String[] { /* test files */
				"X.java",
				"""
					import java.util.*;
					
					public class X {
							 public static void main(String argv[]) {
									 System.out.println("SUCCESS");
							 }
					}"""
			},
			// compiler options
			null /* no class libraries */,
			customOptions /* custom options */,
			"""
				----------
				1. ERROR in X.java (at line 1)
					import java.util.*;
					       ^^^^^^^^^
				The import java.util is never used
				----------
				""",
			// runtime results
			"" /* expected output string */,
			"java.lang.Error: Unresolved compilation problem: \n" + /* expectedErrorString */
			"\n",
			// javac options
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
	}

	public void test003() {
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_FatalOptionalError, CompilerOptions.DISABLED);
		customOptions.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.ERROR);
		runNegativeTest(
			// test directory preparation
			true /* flush output directory */,
			new String[] { /* test files */
				"X.java",
				"""
					public class X {
							 public static void main(String argv[]) {
									 System.out.println("SUCCESS");
							 }
					}"""
			},
			// compiler options
			null /* no class libraries */,
			customOptions /* custom options */,
			"""
				----------
				1. ERROR in X.java (at line 3)
					System.out.println("SUCCESS");
					                   ^^^^^^^^^
				Non-externalized string literal; it should be followed by //$NON-NLS-<n>$
				----------
				""",
			// runtime results
			"SUCCESS" /* expected output string */,
			null /* do not check error string */,
			// javac options
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
	}

	public void test004() {
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_FatalOptionalError, CompilerOptions.DISABLED);
		customOptions.put(CompilerOptions.OPTION_ReportUndocumentedEmptyBlock, CompilerOptions.ERROR);
		runNegativeTest(
			// test directory preparation
			true /* flush output directory */,
			new String[] { /* test files */
				"X.java",
				"""
					public class X {
							 public static void foo() {}
							 public static void main(String argv[]) {
									foo();
									System.out.println("SUCCESS");
							 }
					}"""
			},
			// compiler options
			null /* no class libraries */,
			customOptions /* custom options */,
			"""
				----------
				1. ERROR in X.java (at line 2)
					public static void foo() {}
					                         ^^
				Empty block should be documented
				----------
				""",
			// runtime results
			"SUCCESS" /* expected output string */,
			null /* do not check error string */,
			// javac options
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
	}

	public void test005() {
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_FatalOptionalError, CompilerOptions.ENABLED);
		customOptions.put(CompilerOptions.OPTION_ReportUndocumentedEmptyBlock, CompilerOptions.ERROR);
		runNegativeTest(
			// test directory preparation
			true /* flush output directory */,
			new String[] { /* test files */
				"X.java",
				"""
					public class X {
							 public static void foo() {}
							 public static void main(String argv[]) {
									foo();
									System.out.println("SUCCESS");
							 }
					}"""
			},
			// compiler options
			null /* no class libraries */,
			customOptions /* custom options */,
			"""
				----------
				1. ERROR in X.java (at line 2)
					public static void foo() {}
					                         ^^
				Empty block should be documented
				----------
				""",
			// runtime results
			"" /* expected output string */,
			"""
				java.lang.Error: Unresolved compilation problem:\s
					Empty block should be documented
				
				""",
			// javac options
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=319626
	public void test006() {
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_FatalOptionalError, CompilerOptions.DISABLED);
		customOptions.put(CompilerOptions.OPTION_ReportUndocumentedEmptyBlock, CompilerOptions.ERROR);
		runNegativeTest(
			// test directory preparation
			true /* flush output directory */,
			new String[] { /* test files */
				"X.java",
				"""
					public class X {
						{     }
						static {  }
					 	X() { }
					 	X(int a) {}
					 	public void foo() {}
						public static void main(String argv[]) {
							System.out.println("SUCCESS");
						}
					}
					"""
			},
			// compiler options
			null /* no class libraries */,
			customOptions /* custom options */,
			"""
				----------
				1. ERROR in X.java (at line 2)
					{     }
					^^^^^^^
				Empty block should be documented
				----------
				2. ERROR in X.java (at line 3)
					static {  }
					       ^^^^
				Empty block should be documented
				----------
				3. ERROR in X.java (at line 5)
					X(int a) {}
					         ^^
				Empty block should be documented
				----------
				4. ERROR in X.java (at line 6)
					public void foo() {}
					                  ^^
				Empty block should be documented
				----------
				""",
			// runtime results
			"SUCCESS" /* expected output string */,
			null /* do not check error string */,
			// javac options
			JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
	}
	public void test007() {
		if (this.complianceLevel < ClassFileConstants.JDK1_5) {
			return;
		}
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_FatalOptionalError,
				CompilerOptions.ENABLED);
		customOptions.put(CompilerOptions.OPTION_ReportUnusedLocal,
				CompilerOptions.ERROR);
		customOptions.put(CompilerOptions.OPTION_SuppressWarnings,
				CompilerOptions.ENABLED);
		customOptions.put(CompilerOptions.OPTION_SuppressOptionalErrors,
				CompilerOptions.ENABLED);
		customOptions.put(CompilerOptions.OPTION_ReportUnusedWarningToken,
				CompilerOptions.ERROR);
		runConformTest(
				new String[] { /* test files */
						"X.java",
						"""
							public class X {
							        @SuppressWarnings("unused")
							        static void foo() {
							            String s = null;
							            System.out.println("SUCCESS");
							        }
							        public static void main(String argv[]) {
							            foo();
							        }
							}"""
				},
				"SUCCESS" /* expected output string */,
				null /* no class libraries */,
				true,
				null,
				customOptions /* custom options */,
				// compiler results
				null /* do not check error string */);
	}
	public void testImportUnresolved() {
		Map<String,String> options = getCompilerOptions();
		options.put(JavaCore.COMPILER_PB_UNUSED_IMPORT, JavaCore.ERROR);
		runNegativeTest(
			true, // flush dir
			new String[] {
				"X.java",
				"""
					import com.bogus.Missing;
					public class X {
						public static void main(String[] args) {
							new X().test();
						}
						void test() {
							System.out.println("OK");
						}
					}
					"""
			},
			null, // libs
			options,
			"""
				----------
				1. ERROR in X.java (at line 1)
					import com.bogus.Missing;
					       ^^^^^^^^^
				The import com.bogus cannot be resolved
				----------
				""",
			"OK",
			"",
			JavacTestOptions.SKIP);
	}
	public void testImportUnresolved_fatal() {
		Map<String,String> options = getCompilerOptions();
		try {
			options.put(JavaCore.COMPILER_PB_UNUSED_IMPORT, JavaCore.ERROR);
			options.put(JavaCore.COMPILER_PB_FATAL_OPTIONAL_ERROR, JavaCore.ENABLED);
			options.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);
			runNegativeTest(
					true, // flush dir
					new String[] {
							"p/Z.java",
							"""
								package p;
								public class Z {
									public static void main(String[] args) throws Exception {
										try {
											Class.forName("X").newInstance();
										} catch (java.lang.Error e) {
											System.err.println(e.getMessage());
										}
									}
								}
								""",
									"X.java",
									"""
										import com.bogus.Missing;
										public class X {
											public static void main(String[] args) {
												new X().test();
											}
											void test() {
												System.out.println("OK");
											}
										}
										"""
					},
					null, // libs
					options,
					"""
						----------
						1. ERROR in X.java (at line 1)
							import com.bogus.Missing;
							       ^^^^^^^^^
						The import com.bogus cannot be resolved
						----------
						""",
							"",
							"Unresolved compilation problem: \n" +
									"	The import com.bogus cannot be resolved",
									JavacTestOptions.SKIP);
		} finally {
			options.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.WARNING);
		}
	}
	public void testPackageConflict() {
		Map<String,String> options = getCompilerOptions();
		try {
			options.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);

			runNegativeTest(
					true, // flush dir
					new String[] {
							"p/z.java",
							"""
								package p;
								public class z {
									public static void main(String[] args) throws Exception {
										try {
											Class.forName("p.z.X").newInstance();
										} catch (ClassNotFoundException e) {
											System.out.println(e.getClass().getName());
										}
									}
								}
								""",
									"p/z/X.java",
									"""
										package p.z;
										public class X {
											public X() {
												System.out.println("OK");
											}
										}
										""",
					},
					null, // libs
					options,
					"""
						----------
						1. ERROR in p\\z\\X.java (at line 1)
							package p.z;
							        ^^^
						The package p.z collides with a type
						----------
						""",
							"java.lang.ClassNotFoundException", // cannot generate code in presence of the above error
							"",
							JavacTestOptions.SKIP);
		} finally {
			options.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.WARNING);
		}
	}
	public void testImportVariousProblems() {
		Map<String,String> options = getCompilerOptions();
		try {
			options.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);

			runNegativeTest(
					true, // flush dir
					new String[] {
							"p/Z.java",
							"""
								package p;
								public class Z {
									public static void main(String[] args) throws Exception {
										try {
											Class.forName("X").newInstance();
										} catch (ClassNotFoundException e) {
											System.out.println(e.getClass().getName());
										}
									}
								}
								""",
									"p1/Y.java",
									"package p1;\n" +
											"public class Y {}\n",
											"p2/Y.java",
											"package p2;\n" +
													"public class Y {}\n",
													"X.java",
													"""
														import java.util;
														import p.Z;
														import p1.Y;
														import p2.Y;
														public class X {
															public X() {
																System.out.println("OK");
															}
														}
														class Z {}
														"""
					},
					null, // libs
					options,
					"""
						----------
						1. ERROR in X.java (at line 1)
							import java.util;
							       ^^^^^^^^^
						Only a type can be imported. java.util resolves to a package
						----------
						2. ERROR in X.java (at line 2)
							import p.Z;
							       ^^^
						The import p.Z conflicts with a type defined in the same file
						----------
						3. ERROR in X.java (at line 4)
							import p2.Y;
							       ^^^^
						The import p2.Y collides with another import statement
						----------
						""",
							"OK",
							"",
							JavacTestOptions.SKIP);
		} finally {
			options.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.WARNING);
		}
	}
	public void testImportStaticProblems() {
		if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // uses static imports
		Map<String,String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);
		runNegativeTest(
			true, // flush dir
			new String[] {
				"p/Z.java",
				"""
					package p;
					public class Z {
						public static void main(String[] args) throws Exception {
							try {
								Class.forName("X").newInstance();
							} catch (ClassNotFoundException e) {
								System.out.println(e.getClass().getName());
							}
						}
					}
					""",
				"p1/Y.java",
				"""
					package p1;
					public class Y {
						static int f;
					}
					""",
				"X.java",
				"""
					import static p1.Y;
					import static p1.Y.f;
					public class X {
						public X() {
							System.out.println("OK");
						}
					}
					class Z {}
					"""
			},
			null, // libs
			options,
			"""
				----------
				1. ERROR in X.java (at line 1)
					import static p1.Y;
					              ^^^^
				The static import p1.Y must be a field or member type
				----------
				2. ERROR in X.java (at line 2)
					import static p1.Y.f;
					              ^^^^^^
				The field Y.p1.Y.f is not visible
				----------
				""",
			"OK",
			"",
			JavacTestOptions.SKIP);
	}
	public void testDuplicateImports1() {
		if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // uses static imports
		runConformTest(
			new String[] {
				"Test.java",
				"""
					import java.lang.Character.Subset;
					import static java.lang.Character.Subset;
					
					public class Test {
						Subset s = null;
					}
					"""
			});
	}
	public void testDuplicateImports2() {
		if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // uses static imports
		runConformTest(
			new String[] {
				"Test.java",
				"""
					import static java.awt.geom.Line2D.Double;
					import static java.awt.geom.Line2D.Double;
					
					public class Test {
						Double d = null;
					}
					"""
			});
	}
	public void testDuplicateImports3() {
		if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // uses static imports
		runNegativeTest(
			new String[] {
				"Test.java",
				"""
					import static java.awt.geom.Line2D.Double;
					import static java.awt.geom.Point2D.Double;
					
					public class Test {
						Double d = null;
					}
					"""
			},
			(this.complianceLevel < ClassFileConstants.JDK1_8
			?
				"""
					----------
					1. ERROR in Test.java (at line 2)
						import static java.awt.geom.Point2D.Double;
						              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					The import java.awt.geom.Point2D.Double collides with another import statement
					----------
					"""
			:
				"""
					----------
					1. ERROR in Test.java (at line 5)
						Double d = null;
						^^^^^^
					The type Double is ambiguous
					----------
					"""));
	}
}
