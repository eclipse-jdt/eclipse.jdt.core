/*******************************************************************************
 * Copyright (c) 2020, 2021 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
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
import java.util.Map;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.core.util.ClassFormatException;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class LocalStaticsTest extends AbstractRegressionTest {

	static {
//		TESTS_NUMBERS = new int [] { 40 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] { "testBug569444_001"};
	}

	public static Class<?> testClass() {
		return LocalStaticsTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_16);
	}
	public LocalStaticsTest(String testName){
		super(testName);
	}

	// Enables the tests to run individually
	protected Map<String, String> getCompilerOptions() {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_16); // FIXME
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_16);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_16);
		defaultOptions.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		defaultOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
		return defaultOptions;
	}

	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput) {
		runConformTest(testFiles, expectedOutput, getCompilerOptions());
	}

	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput, Map<String, String> customOptions) {
		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedOutputString = expectedOutput;
		runner.vmArguments = new String[] {"--enable-preview"};
		runner.customOptions = customOptions;
		runner.runConformTest();
	}
	@Override
	protected void runNegativeTest(String[] testFiles, String expectedCompilerLog) {
		runNegativeTest(testFiles, expectedCompilerLog, null);
	}
	protected void runWarningTest(String[] testFiles, String expectedCompilerLog) {
		runWarningTest(testFiles, expectedCompilerLog, null);
	}
	protected void runWarningTest(String[] testFiles, String expectedCompilerLog, Map<String, String> customOptions) {
		runWarningTest(testFiles, expectedCompilerLog, customOptions, null);
	}
	protected void runWarningTest(String[] testFiles, String expectedCompilerLog,
			Map<String, String> customOptions, String javacAdditionalTestOptions) {

		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedCompilerLog = expectedCompilerLog;
		runner.customOptions = customOptions;
		runner.runWarningTest();
	}

	private static void verifyClassFile(String expectedOutput, String classFileName)
			throws IOException, ClassFormatException {
		String result = getClassfileContent(classFileName);
		int index = result.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(result, 3));
			System.out.println("...");
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, result);
		}
	}
	private static String getClassfileContent(String classFileName) throws IOException, ClassFormatException {
		File f = new File(OUTPUT_DIR + File.separator + classFileName);
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.SYSTEM);
		return result;
	}

	public void testBug566284_001() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 static void foo() {
					   interface F {
					     static int create(int lo) {
					       I myI = s -> lo;
					       return myI.bar(0);
					     }
					   }
					   System.out.println(F.create(0));
					     }
					 public static void main(String[] args) {
					   X.foo();
					 }
					}
					
					interface I {
					 int bar(int l);
					}"""
			},
			"0");
	}


	public void testBug566284_002() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 static void foo() {
					   record R() {
					     static int create(int lo) {
					       I myI = s -> lo;
					       return myI.bar(0);
					     }
					   }
					   System.out.println(R.create(0));
					     }
					 public static void main(String[] args) {
					   X.foo();
					 }
					}
					
					interface I {
					 int bar(int l);
					}"""
			},
			"0");
	}
	public void testBug566284_003() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					 static int si;
					 int nsi;
					
					 void m() {
					   int li;
					
					   interface F {
					     static int fi = 0;
					
					     default void foo(int i) {
					       System.out.println(li); // error, local variable of method of outer enclosing class
					       System.out.println(nsi); // error, non-static member
					       System.out.println(fi); // ok, static member of current class
					       System.out.println(si); // ok, static member of enclosing class
					       System.out.println(i); // ok, local variable of current method
					     }
					
					     static void bar(int lo) {
					       int k = lo; // ok
					       int j = fi; // ok
					       I myI = s -> lo; // ok, local var of method
					     }
					
					     static void bar2(int lo) {
					       I myI = s -> li; // error - local var of outer class
					     }
					   }
					 }
					}
					
					interface I {
					 int bar(int l);
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 12)
					System.out.println(li); // error, local variable of method of outer enclosing class
					                   ^^
				Cannot make a static reference to the non-static variable li
				----------
				2. ERROR in X.java (at line 13)
					System.out.println(nsi); // error, non-static member
					                   ^^^
				Cannot make a static reference to the non-static field nsi
				----------
				3. ERROR in X.java (at line 26)
					I myI = s -> li; // error - local var of outer class
					             ^^
				Cannot make a static reference to the non-static variable li
				----------
				"""
			);
	}

	public void testBug566518_001() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 static void foo() {
					   int f = switch (5) {
								case 5: {
									interface I{
									\t
									}
									class C implements I{
										public int j = 5;
									}
								\t
									yield new C().j;
								}
								default:
									throw new IllegalArgumentException("Unexpected value: " );
								};
						System.out.println(f);
					 }
					 public static void main(String[] args) {
					   X.foo();
					 }
					}"""
			},
			"5");
	}

	public void testBug566518_002() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 static void foo() {
					   class F {
					     int create(int lo) {
					       I myI = s -> lo;
					       return myI.bar(0);
					     }
					   }
					   System.out.println(new F().create(0));
					     }
					 public static void main(String[] args) {
					   X.foo();
					 }
					}
					
					interface I {
					 int bar(int l);
					}"""
			},
			"0");
	}

	public void testBug566518_003() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public interface X {
					 static void foo() {
					   class F {
					     int create(int lo) {
					       I myI = s -> lo;
					       return myI.bar(0);
					     }
					   }
					   System.out.println(new F().create(0));
					     }
					 public static void main(String[] args) {
					   X.foo();
					 }
					}
					
					interface I {
					 int bar(int l);
					}"""
			},
			"0");
	}

	public void testBug566518_004() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public interface X {
					 static void foo() {
					   interface F {
					     static int create(int lo) {
					       I myI = s -> lo;
					       return myI.bar(0);
					     }
					   }
					   System.out.println(F.create(0));
					     }
					 public static void main(String[] args) {
					   X.foo();
					 }
					}
					
					interface I {
					 int bar(int l);
					}"""
			},
			"0");
	}

	public void testBug566518_005() {
		runNegativeTest(
			new String[] {
					"X.java",
					"""
						public class X {
						 static void foo() {
						   int f = switch (5) {
									case 5: {
										interface I{
										\t
										}
										class C implements I{
											public int j = 5;
										}
									\t
										yield new C().j;
									}
									default:
										throw new IllegalArgumentException("Unexpected value: " );
									};
							System.out.println(f);
							class C1 implements I{
								public int j = 5;
							}
						 }
						 public static void main(String[] args) {
						   X.foo();
						 }
						}
						"""
			},
			"""
				----------
				1. ERROR in X.java (at line 18)
					class C1 implements I{
					                    ^
				I cannot be resolved to a type
				----------
				"""
			);
	}

	public void testBug566518_006() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public enum X {
					 A, B, C;
					 public void foo() {
					   int f = switch (5) {
								case 5: {
									interface I{
									\t
									}
									class C implements I{
										public int j = 5;
									}
								\t
									yield new C().j;
								}
								default:
									throw new IllegalArgumentException("Unexpected value: " );
								};
					 }
					 public static void main(String[] args) {
					   X x = X.A;
						System.out.println();
					 }
					}"""
			},
			"");
	}
	// 6.5.5.1
	public void testBug566715_001() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X<T> {
					 static void foo() {
						interface I {
							X<T> supply();
						}
					 }
					}
					"""
			},
			"""
				----------
				1. WARNING in X.java (at line 3)
					interface I {
					          ^
				The type I is never used locally
				----------
				2. ERROR in X.java (at line 4)
					X<T> supply();
					  ^
				Cannot make a static reference to the non-static type T
				----------
				"""
			);
	}
	// 6.5.5.1
	public void testBug566715_002() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X<T> {
					  void foo() {
						interface I {
							X<T> supply();
						}
					 }
					}
					"""
			},
			"""
				----------
				1. WARNING in X.java (at line 3)
					interface I {
					          ^
				The type I is never used locally
				----------
				2. ERROR in X.java (at line 4)
					X<T> supply();
					  ^
				Cannot make a static reference to the non-static type T
				----------
				"""
			);
	}
	// 6.5.5.1
	public void testBug566715_003() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X<T> {
					  void foo() {
						record R(X<T> x) {}
					 }
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					record R(X<T> x) {}
					           ^
				Cannot make a static reference to the non-static type T
				----------
				"""
			);
	}
	// 9.1.1/14.3
	public void testBug566720_001() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X<T> {
					  void foo() {
						public interface I {}
					 }
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					public interface I {}
					                 ^
				Illegal modifier for the local interface I; abstract and strictfp are the only modifiers allowed explicitly\s
				----------
				"""
			);
	}
	// 9.1.1/14.3
	public void testBug566720_002() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X<T> {
					  void foo() {
						private interface I {}
					 }
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					private interface I {}
					                  ^
				Illegal modifier for the local interface I; abstract and strictfp are the only modifiers allowed explicitly\s
				----------
				"""
			);
	}
	// 9.1.1
	public void testBug566720_003() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X<T> {
					  void foo() {
						protected interface I {}
					 }
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					protected interface I {}
					                    ^
				Illegal modifier for the local interface I; abstract and strictfp are the only modifiers allowed explicitly\s
				----------
				"""
			);
	}
	// 9.1.1
	public void testBug566720_004() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X<T> {
					  void foo() {
						final interface I {}
					 }
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					final interface I {}
					                ^
				Illegal modifier for the local interface I; abstract and strictfp are the only modifiers allowed explicitly\s
				----------
				"""
			);
	}
	// 9.1.1
	public void testBug566720_005() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X<T> {
					  public static void main(String[] args) {
						static interface I {}
					 }
					}
					"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					static interface I {}
					                 ^
				Illegal modifier for the local interface I; abstract and strictfp are the only modifiers allowed explicitly\s
				----------
				""");
	}
	public void testBug566748_001() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X<T> {
					        int count;
					        void doNothing() {}
					     void foo1(String s) {
					        int i;
					       interface I {
					               default X<T> bar() {
					                       if (count > 0 || i > 0 || s == null)
					                               return null;
					                       doNothing();
					                               return null;
					               }
					       }\s
					    }
					     void foo2(String s) {
					       try {
					               throw new Exception();
					       } catch (Exception e) {
					               interface I {
					                       default int bar() {
					                         return e != null ? 0 : 1;
					                       }
					               }\s
					              \s
					       }
					    }
					}"""
			},
			"""
				----------
				1. WARNING in X.java (at line 6)
					interface I {
					          ^
				The type I is never used locally
				----------
				2. ERROR in X.java (at line 7)
					default X<T> bar() {
					          ^
				Cannot make a static reference to the non-static type T
				----------
				3. ERROR in X.java (at line 8)
					if (count > 0 || i > 0 || s == null)
					    ^^^^^
				Cannot make a static reference to the non-static field count
				----------
				4. ERROR in X.java (at line 8)
					if (count > 0 || i > 0 || s == null)
					                 ^
				Cannot make a static reference to the non-static variable i
				----------
				5. ERROR in X.java (at line 8)
					if (count > 0 || i > 0 || s == null)
					                          ^
				Cannot make a static reference to the non-static variable s
				----------
				6. ERROR in X.java (at line 10)
					doNothing();
					^^^^^^^^^
				Cannot make a static reference to the non-static method doNothing() from the type X<T>
				----------
				7. WARNING in X.java (at line 19)
					interface I {
					          ^
				The type I is never used locally
				----------
				8. ERROR in X.java (at line 21)
					return e != null ? 0 : 1;
					       ^
				Cannot make a static reference to the non-static variable e
				----------
				"""
			);
	}
	public void testBug566748_002() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					interface X<T> {
					 int count = 0;
					
					 default void doNothing() {}
					
					 default void foo1(String s) {
					   int i;
					   interface I {
					     default X<T> bar() {
					       if (count > 0 || i > 0 || s == null)
					         return null;
					       doNothing();
					       return null;
					     }
					   }
					 }
					
					 default void foo2(String s) {
					       try {
					               throw new Exception();
					       } catch (Exception e) {
					               interface I {\s
					                       default int bar() {
					                         return e != null ? 0 : 1;
					                       }  \s
					               }  \s
					                  \s
					       }  \s
					    }
					}"""
			},
			"""
				----------
				1. WARNING in X.java (at line 8)
					interface I {
					          ^
				The type I is never used locally
				----------
				2. ERROR in X.java (at line 9)
					default X<T> bar() {
					          ^
				Cannot make a static reference to the non-static type T
				----------
				3. ERROR in X.java (at line 10)
					if (count > 0 || i > 0 || s == null)
					                 ^
				Cannot make a static reference to the non-static variable i
				----------
				4. ERROR in X.java (at line 10)
					if (count > 0 || i > 0 || s == null)
					                          ^
				Cannot make a static reference to the non-static variable s
				----------
				5. ERROR in X.java (at line 12)
					doNothing();
					^^^^^^^^^
				Cannot make a static reference to the non-static method doNothing() from the type X<T>
				----------
				6. WARNING in X.java (at line 22)
					interface I {\s
					          ^
				The type I is never used locally
				----------
				7. ERROR in X.java (at line 24)
					return e != null ? 0 : 1;
					       ^
				Cannot make a static reference to the non-static variable e
				----------
				"""
			);
	}
	// 9.6
	public void testBug564557AnnotInterface_001() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					 void foo() {
					   class I {
					     @interface Annot {
					     }
					   }
					 }
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					@interface Annot {
					           ^^^^^
				The member annotation Annot can only be defined inside a top-level class or interface or in a static context
				----------
				"""
			);
	}
	// 9.6
	public void testBug564557AnnotInterface_002() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					 void foo() {
					   interface I {
					     @interface Annot {
					     }
					   }
					 }
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					@interface Annot {
					           ^^^^^
				The member annotation Annot can only be defined inside a top-level class or interface or in a static context
				----------
				"""
			);
	}
	// 9.4 && 15.12.3
	public void testBug564557MethodInvocation_003() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					 void foo() {
					   Zork();
					   interface I {
					     default void bar() {}
					     default void b1() {
					       class J {
					          void jb2() {
					           bar();
					         }
					       }
					     }
					   }
					 }
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				"""
			);
	}
	// 9.4 && 15.12.3
	public void testBug564557MethodInvocation_004() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					 void foo() {
					   interface I {
					     default void bar() {}
					     default void b1() {
					       interface J {
					          default void jb2() {
					           bar();
					         }
					       }
					     }
					   }
					 }
					}"""
			},
			"""
				----------
				1. WARNING in X.java (at line 3)
					interface I {
					          ^
				The type I is never used locally
				----------
				2. WARNING in X.java (at line 5)
					default void b1() {
					             ^^^^
				The method b1() from the type I is never used locally
				----------
				3. WARNING in X.java (at line 6)
					interface J {
					          ^
				The type J is never used locally
				----------
				4. ERROR in X.java (at line 8)
					bar();
					^^^
				Cannot make a static reference to the non-static method bar() from the type I
				----------
				"""
			);
	}
	// 13.1
	public void testBug564557BinaryForm_005() throws Exception {
		runConformTest(
			new String[] {
					"X.java",
					"""
						class X {
						 public static void main(String[] args) {
						   System.out.println("");
						 }
						 void foo() {
						   interface I {
						   }
						 }
						}"""
			},
			"");
		String expectedOutput = "abstract static interface X$1I {\n";
		LocalStaticsTest.verifyClassFile(expectedOutput, "X$1I.class");
	}
	// 14.3 for enum
	public void testBug564557BinaryForm_006() throws Exception {
		runConformTest(
			new String[] {
					"X.java",
					"""
						class X {
						 public static void main(String[] args) {
						   System.out.println("");
						 }
						 void foo() {
						   enum I {
						   }
						 }
						}"""
			},
			"");
		String expectedOutput = "static final enum X$1I {\n";
		LocalStaticsTest.verifyClassFile(expectedOutput, "X$1I.class");
	}
	// 15.8.3
	public void testBug564557thisInStatic_007() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					 void foo() {
					   interface I {
					     int count = 0;
					     static void bar() {
					       int i = this.count;
					     }
					   }
					 }
					}"""
			},
			"""
				----------
				1. WARNING in X.java (at line 3)
					interface I {
					          ^
				The type I is never used locally
				----------
				2. WARNING in X.java (at line 4)
					int count = 0;
					    ^^^^^
				The value of the field I.count is not used
				----------
				3. ERROR in X.java (at line 6)
					int i = this.count;
					        ^^^^
				Cannot use this in a static context
				----------
				"""
			);
	}
	// 15.8.3
	public void testBug564557thisInStatic_008() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					 int count = 0;
					 void foo() {
					   interface I {
					     static void bar() {
					       int i = X.this.count;
					     }
					   }
					 }
					}"""
			},
			"""
				----------
				1. WARNING in X.java (at line 4)
					interface I {
					          ^
				The type I is never used locally
				----------
				2. ERROR in X.java (at line 6)
					int i = X.this.count;
					        ^^^^^^
				No enclosing instance of the type X is accessible in scope
				----------
				"""
			);
	}
	public void testBug568514LocalEnums_001() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					    public void foo() {
					        public enum I {}
					    }
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					public enum I {}
					            ^
				Illegal modifier for local enum I; no explicit modifier is permitted
				----------
				"""
		);
	}
	public void testBug568514LocalEnums_002() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					    public void foo() {
					        public enum I {}
					    }
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					public enum I {}
					            ^
				Illegal modifier for local enum I; no explicit modifier is permitted
				----------
				""",
			null,
			true,
			options
		);
	}
	public void testBug568514LocalEnums_003() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					    public void foo() {
					        public enum I {}
					    Zork;
					    }
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					Zork;
					^^^^
				Syntax error, insert "VariableDeclarators" to complete LocalVariableDeclaration
				----------
				"""
		);
	}

	public void testBug568514LocalEnums_004() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					    public void foo() {
					        public strictfp enum I {}
					    Zork;
					    }
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					Zork;
					^^^^
				Syntax error, insert "VariableDeclarators" to complete LocalVariableDeclaration
				----------
				"""
		);
	}

	public void testBug566579_001() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 private String I=null;
					 public void foo() {
					   int f = switch (5) {
								case 5: {
									interface I{
										public int getVal();
									}
									class C implements I{
										private int j=5;
										@Override
										public int getVal() {
											return j;
										}
									}
								\t
									I abc= new C();\
									yield abc.getVal();
								}
								default:
									yield (I==null ? 0 : I.length());
								};
					 }
					 public static void main(String[] args) {
					   X x = new X();
					   x.I = "abc";
						System.out.println();
					 }
					}"""
			},
			"");
	}
	public void testBug566579_002() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {\t
						public void main5(int i) {
							interface i{
								public static int i=0;
							}
						}
						public static void main(String[] args) {
							System.out.println();
						}
					}"""
			},
			"");
	}
	public void testBug566579_003() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {\t
						public void main5() {
							int i=10;
							interface i{
								public static int i=0;
							}
						}
						public static void main(String[] args) {
							System.out.println();
						}
					}"""
			},
			"");
	}
	public void testBug566579_004() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {\t
						public void main5() {
							try {
								int i=10;
							} catch(NullPointerException npe) {
								interface i{
									public static int npe=0;
								}
							}\
						}
						public static void main(String[] args) {
							System.out.println();
						}
					}"""
			},
			"");
	}
	public void testBug569444_001() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 public static void main(String[] args) {
					   System.out.println("hello");
					   class Y{
					     static int field;
					     public static void foo() {}
					   }
					   record R() {}
					 }
					 class Z {
					   static int f2;
					   static {};
					 }
					}"""
			},
			"hello");
	}
	public void testBug569444_002() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 private void foo() {
					   class Y {
					     static record R() {}
					     static class Z{}
					     interface I{}
					     static interface II{}
					   }
					 }
					 public static void main(String[] args) {
					   System.out.println("hello");
					 }
					}"""
			},
			"hello");
	}
	public void testBug569444_003() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 public void foo() {
					   @SuppressWarnings("unused")
					   class Y {
					     static record R() {}
					      class Z{
					       static class zz{}
					     }
					     interface I{
					       abstract int bar();
					     }
					   }
					    new Y.I() {
					     @Override
					     public int bar() {
					       return 0;
					     }
					    \s
					   };
					 }
					 public static void main(String[] args) {
					   System.out.println("hello");
					 }
					}"""
			},
			"hello");
	}
	public void testBug569444_004() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 public void foo() {
					    @SuppressWarnings("unused")
					    static class zzz{}
					 }
					 public static void main(String[] args) {
					   System.out.println("hello");
					 }
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					static class zzz{}
					             ^^^
				Illegal modifier for the local class zzz; only abstract or final is permitted
				----------
				""");
	}
	public void testBug569444_005() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 public void foo() {
					     static class Z{} //  static not allowed
					     class Y{
					       static class ZZ{} // static allowed
					     }
					   static record R() {} // explicit static not allowed
					   static interface I {} // explicit static not allowed
					    }
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					static class Z{} //  static not allowed
					             ^
				Illegal modifier for the local class Z; only abstract or final is permitted
				----------
				2. ERROR in X.java (at line 7)
					static record R() {} // explicit static not allowed
					              ^
				A local class or interface R is implicitly static; cannot have explicit static declaration
				----------
				3. ERROR in X.java (at line 8)
					static interface I {} // explicit static not allowed
					                 ^
				Illegal modifier for the local interface I; abstract and strictfp are the only modifiers allowed explicitly\s
				----------
				""");
	}
	public void testBug569444_006() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 public void foo() {
					   for (;;) {
					     static class Y  {}
					     static record R() {}
					     static interface I{}
					     break;
					   }
					    }
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					static class Y  {}
					             ^
				Illegal modifier for the local class Y; only abstract or final is permitted
				----------
				2. ERROR in X.java (at line 5)
					static record R() {}
					              ^
				A local class or interface R is implicitly static; cannot have explicit static declaration
				----------
				3. ERROR in X.java (at line 6)
					static interface I{}
					                 ^
				Illegal modifier for the local interface I; abstract and strictfp are the only modifiers allowed explicitly\s
				----------
				""");
	}
	public void testBug571163_001() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					 class X {
					    public void foo() {
					        class Y {
					            static Y y;
					             static {
					                y = Y.this;
					            }
					            class Z {
					                static Y yy;
					                static {
					                       yy = Y.this; //error not flagged here
					                }
					            }
					        }\s
					     }
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					y = Y.this;
					    ^^^^^^
				Cannot use this in a static context
				----------
				2. ERROR in X.java (at line 11)
					yy = Y.this; //error not flagged here
					     ^^^^^^
				Cannot use this in a static context
				----------
				""");
	}
	public void testBug571300_001() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					class X {
					  public static void main(String[] args) {
					   new X().foo(); \s
					 }
					 public void foo() {
					   interface I {
					     class Z {}
					   }
					    I.Z z = new I.Z() { // error flagged incorrectly
					     public String toString() {
					       return "I.Z";
					     }
					    };
					    System.out.println(z.toString());
					  }
					}"""
			},
			"I.Z");
	}
	public void testBug571274_001() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					class X {
					 void m() {
					   interface Y<T> {
					     class Z {
					        T foo() {// T should not be allowed
					         return null;
					       }
					     }
					   }
					 }
					 }"""
			},
			"""
				----------
				1. WARNING in X.java (at line 3)
					interface Y<T> {
					          ^
				The type Y<T> is never used locally
				----------
				2. WARNING in X.java (at line 4)
					class Z {
					      ^
				The type Y<T>.Z is never used locally
				----------
				3. ERROR in X.java (at line 5)
					T foo() {// T should not be allowed
					^
				Cannot make a static reference to the non-static type T
				----------
				""");
	}
	public void testBug566774_001() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						class X {
						 static String a;
						 String b;
						 static String concat() {
						        return a + b;
						 }
						 }"""
				},
				"""
					----------
					1. ERROR in X.java (at line 5)
						return a + b;
						           ^
					Cannot make a static reference to the non-static field b
					----------
					""");
	}
	public void testBug566774_002() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						class X {
						 static String a;
						 String b;
						 int index() {
						     interface I {
								class Matcher {
									void check() {
										if (a == null || b == null) {
											throw new IllegalArgumentException();
										}
									}
								}
							   }
							I.Matcher matcher = new I.Matcher();
							matcher.check();
							return 0;
						 }
						 }"""
				},
				"""
					----------
					1. ERROR in X.java (at line 8)
						if (a == null || b == null) {
						                 ^
					Cannot make a static reference to the non-static field b
					----------
					""");
	}

	public void testBug566774_003() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						class X {
						 public static void main(String[] args) {
						 	class Checker1 {
						 		void checkWhitespace(int x) {
						     		String arg = args[x];
									if (!arg.trim().equals(arg)) {
										throw new IllegalArgumentException();
									}
								}
							}
							final Checker1 c1 = new Checker1();
							for (int i = 1; i < args.length; i++) {
								Runnable r = () -> {
									c1.checkWhitespace(i);
								};
							}
						 }
						 }"""
				},
				"""
					----------
					1. ERROR in X.java (at line 14)
						c1.checkWhitespace(i);
						                   ^
					Local variable i defined in an enclosing scope must be final or effectively final
					----------
					""");
	}
	public void testBug566774_004() {
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							class X {
							 public static void main(String[] args) {
							 	interface I {
							 		class Checker2 {
							 			void checkFlag(int x) {
							     			String arg = args[x];
											if (!arg.startsWith("-")) {
												throw new IllegalArgumentException();
											}
										}
									}
								}
								I.Checker2 c2 = new I.Checker2();
								for (int i = 1; i < args.length; i++) {
									Runnable r = () -> {
										c2.checkFlag(i);
									};
								}
							 }
							 }"""
					},
					"""
						----------
						1. ERROR in X.java (at line 6)
							String arg = args[x];
							             ^^^^
						Cannot make a static reference to the non-static variable args
						----------
						2. ERROR in X.java (at line 16)
							c2.checkFlag(i);
							             ^
						Local variable i defined in an enclosing scope must be final or effectively final
						----------
						""");
	}
	public void testBug572994_001() {
		runNegativeTest(
			new String[] {
					"X.java",
					"""
						public class X {
						
						 public class Singleton {
						   private static Singleton pinstance = new Singleton();
						   public static Singleton instance() {
						     return pinstance;
						   }
						   public String message() {
						     return "Hello world!";
						   }
						 }
						\s
						 public static void main(String[] args) {
						   System.out.println(Singleton.instance().message());
						 }
						}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						private static Singleton pinstance = new Singleton();
						                                     ^^^^^^^^^^^^^^^
					No enclosing instance of type X is accessible. Must qualify the allocation with an enclosing instance of type X (e.g. x.new A() where x is an instance of X).
					----------
					""");
	}
	public void testBug572994_002() {
		runNegativeTest(
			new String[] {
					"X.java",
					"""
						public class X {
						
						 public class Singleton {
						   private static Singleton pinstance = this;
						   public static Singleton instance() {
						     return pinstance;
						   }
						   public String message() {
						     return "Hello world!";
						   }
						 }
						\s
						 public static void main(String[] args) {
						   System.out.println(Singleton.instance().message());
						 }
						}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						private static Singleton pinstance = this;
						                                     ^^^^
					Cannot use this in a static context
					----------
					""");
	}
	public void testBug572994_003() {
		runNegativeTest(
			new String[] {
					"X.java",
					"""
						public class X {
						
						 public class Singleton {
						   private static Y pinstance = new Y();
						   public static Y instance() {
						     return pinstance;
						   }
						   public String message() {
						     return "Hello world!";
						   }
						 }
						\s
						 public static void main(String[] args) {
						 }
						 class Y {}
						}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						private static Y pinstance = new Y();
						                             ^^^^^^^
					No enclosing instance of type X is accessible. Must qualify the allocation with an enclosing instance of type X (e.g. x.new A() where x is an instance of X).
					----------
					""");
	}
	// Test that static field inside inner types are properly initialized
	public void testBug574791_1() {
		runConformTest(
			new String[] {
					"EnumTester.java",
					"""
						public class EnumTester {
							public static void main(String[] args) {
								Test e = Test.ONE;
								System.out.println(e.value());
								System.out.println(MyTest.TWO.value());
								I TWO = new I() {
									private static final String value = getString();
									@Override
									public String value() {
										return value;
									}
								};
								System.out.println(TWO.value());
							}
							private static String getString() {
								return "Hi from EnumTester";
							}
							class MyTest {
								public static final String value = getString();
								private static String getString() {
									return "Hi from MyTest";
								}
								public static I TWO = new I() {
									private static final String value = getString();
									@Override
									public String value() {
										return value;
									}
								};
							}
							interface I {
								public String value();
							}
						}
						enum Test {
							ONE {
								private static final String value = getString();
								@Override
								String value() {
									return value;
								}
							};
							abstract String value();
							private static String getString() {
								return "Hi from Test";
							}
						}"""
				},
			"""
				Hi from Test
				Hi from MyTest
				Hi from EnumTester""");
	}
	// Test that the static initializer is generated only when required
	// i.e., when the (anonymous) inner class contains a static field
	public void testBug574791_2() throws Exception {
		runConformTest(
			new String[] {
					"X.java",
					"""
						public class X {
							public static void main(String[] args) {
						 }
						}
						enum Test {
							ONE {
								private static final String value = getString();
								@Override
								String value() {
									return value;
								}
							},
							TWO {
								String value() {
									return "TWO";
								}
							},
							;
							abstract String value();
							private static String getString() {
								return "default";
							}
						}"""
				},
			"");
			String expectedOutput =
					"""
				  // Method descriptor #17 ()V
				  // Stack: 1, Locals: 0
				  static {};
				    0  invokestatic Test.getString() : java.lang.String [18]
				    3  putstatic Test$1.value : java.lang.String [22]
				    6  return\
				""";
			String content = getClassfileContent("Test$1.class");
			assertTrue("Expected code not found", content.indexOf(expectedOutput) != -1);
			expectedOutput = "  static {};";
			content = getClassfileContent("Test$2.class");
			assertTrue("Unexpected code found", content.indexOf(expectedOutput) == -1);
	}
	public void testBug574791_3() {
		runConformTest(
			new String[] {
					"EnumTester.java",
					"""
						public class EnumTester {
							public static void main(String[] args) {
								Test e = Test.ONE;
								System.out.println(e.value());
								System.out.println(MyTest.TWO.value());
								I TWO = new I() {
									private static final String value = getString();
									@Override
									public String value() {
										return value;
									}
								};
								System.out.println(TWO.value());
							}
							private static String getString() {
								return "Hi from EnumTester";
							}
							class MyTest {
								public static String value;
						     static {
						       value = getString();
						     }
								private static String getString() {
									return "Hi from MyTest";
								}
								public static I TWO = new I() {
									private static final String value = getString();
									@Override
									public String value() {
										return value;
									}
								};
							}
							interface I {
								public String value();
							}
						}
						enum Test {
							ONE {
								public static String value;
						     static {
						       value = getString();
						     }
								@Override
								String value() {
									return value;
								}
							};
							abstract String value();
							private static String getString() {
								return "Hi from Test";
							}
						}"""
				},
			"""
				Hi from Test
				Hi from MyTest
				Hi from EnumTester""");
	}
}