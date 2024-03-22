/*******************************************************************************
 * Copyright (c) 2021, 2024 IBM Corporation and others.
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
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class SwitchPatternTest extends AbstractRegressionTest9 {

	static {
//		TESTS_NUMBERS = new int [] { 40 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] { "testBug575053_002"};
//		TESTS_NAMES = new String[] { "testBug575571_1"};
	}

	private static String previewLevel = "21";

	public static Class<?> testClass() {
		return SwitchPatternTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_21);
	}
	public SwitchPatternTest(String testName){
		super(testName);
	}

	// Enables the tests to run individually
	protected Map<String, String> getCompilerOptions() {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_21);
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_21);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_21);
		defaultOptions.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);
		defaultOptions.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		defaultOptions.put(CompilerOptions.OPTION_Store_Annotations, CompilerOptions.ENABLED);
		return defaultOptions;
	}

	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput) {
		runConformTest(testFiles, expectedOutput, "", getCompilerOptions());
	}
	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput, Map<String, String> customOptions) {
		runConformTest(testFiles, expectedOutput, "", customOptions);
	}
	protected void runConformTest(String[] testFiles, String expectedOutput, String errorOutput) {
		runConformTest(testFiles, expectedOutput, errorOutput, getCompilerOptions());
	}
	protected void runConformTest(String[] testFiles, String expectedOutput, String expectedErrorOutput, Map<String, String> customOptions) {
		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedOutputString = expectedOutput;
		runner.expectedErrorString = expectedErrorOutput;
		runner.vmArguments = new String[] {"--enable-preview"};
		runner.customOptions = customOptions;
		runner.runConformTest();
	}
	@Override
	protected void runNegativeTest(String[] testFiles, String expectedCompilerLog) {
		runNegativeTest(testFiles, expectedCompilerLog, "");
	}
	protected void runNegativeTest(String[] testFiles, String expectedCompilerLog, String javacLog) {
		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedCompilerLog = expectedCompilerLog;
		runner.expectedJavacOutputString = expectedCompilerLog;
		runner.vmArguments = null;
		runner.customOptions = getCompilerOptions();
		runner.runNegativeTest();
		runNegativeTest(testFiles, expectedCompilerLog, JavacTestOptions.forReleaseWithPreview(SwitchPatternTest.previewLevel));
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
		runner.vmArguments = new String[] {};
		runner.runWarningTest();
	}

	private static void verifyClassFile(String expectedOutput, String classFileName, int mode)
			throws IOException, ClassFormatException {
		File f = new File(OUTPUT_DIR + File.separator + classFileName);
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
		ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
		String result = disassembler.disassemble(classFileBytes, "\n", mode);
		int index = result.indexOf(expectedOutput);
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(result, 3));
			System.out.println("...");
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, result);
		}
	}
	public void testIssue57_001() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 private static void foo(Object o) {
					   switch (o) {
					     case Integer i     -> System.out.println("String:");
					     case String s     -> System.out.println("String: Hello World!");
					     default       -> System.out.println("Object");
					   }
					 }
					 public static void main(String[] args) {
					   foo("Hello World");
					 }
					}""",
			},
			"String: Hello World!");
	}
	public void testIssue57_002() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 private static void foo(Object o) {
					   switch (o) {
					     case Integer i when i > 10    -> System.out.println("Integer: greater than 10");
					     case String  s   -> System.out.println("String: Hello World!");
					     default       -> System.out.println("Object");
					   }
					 }
					 public static void main(String[] args) {
					   foo(12);
					 }
					}""",
			},
			"Integer: greater than 10");
	}
	public void testBug573516_002() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 private static void foo(Object o) {
					   switch (o) {
					     case String s     -> System.out.println("String:");
					     default       -> System.out.println("Object");
					   }
					 }
					 public static void main(String[] args) {
					   foo("Hello World");
					   Zork();
					 }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 10)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""");
	}
	public void testBug573516_003() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 private static void foo(Object o) {
					   switch (o) {
					     case String s  : System.out.println("String:"); break;
					     case Integer i  : System.out.println("Integer:");break;
					     default       : System.out.println("Object");break;
					   }
					 }
					 public static void main(String[] args) {
					   foo("Hello World");
					   Zork();
					 }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 11)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""");
	}
	public void testBug573516_004() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 private static void foo(Object o) {
					   switch (o) {
					     case Integer t when t > 0 -> System.out.println("Integer && t > 0");
					     default       -> System.out.println("Object");
					   }
					 }
					 public static void main(String[] args) {
					   foo("Hello World");
					   Zork();
					 }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 10)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""");
	}
	public void testBug573516_005() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 private static void foo(Object o) {
					   switch (o) {
					     case Integer t, String s, X x : System.out.println("Integer, String or X");
					     default : System.out.println("Object");
					   }
					 }
					 public static void main(String[] args) {
					   foo("Hello World");
					   Zork();
					 }
					}
					class Y {}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					case Integer t, String s, X x : System.out.println("Integer, String or X");
					             ^
				Named pattern variables are not allowed here
				----------
				2. ERROR in X.java (at line 4)
					case Integer t, String s, X x : System.out.println("Integer, String or X");
					                ^^^^^^^^
				Cannot mix pattern with other case labels
				----------
				3. ERROR in X.java (at line 4)
					case Integer t, String s, X x : System.out.println("Integer, String or X");
					                       ^
				Named pattern variables are not allowed here
				----------
				4. ERROR in X.java (at line 4)
					case Integer t, String s, X x : System.out.println("Integer, String or X");
					                          ^^^
				Cannot mix pattern with other case labels
				----------
				5. ERROR in X.java (at line 4)
					case Integer t, String s, X x : System.out.println("Integer, String or X");
					                            ^
				Named pattern variables are not allowed here
				----------
				6. ERROR in X.java (at line 10)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""");
	}
	public void testBug573516_006() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 private static void foo(Object o) {
					   switch (o) {
					     case Integer t, String s when s.length > 0, X x when x.hashCode() > 10 : System.out.println("Integer, String or X");
					     default : System.out.println("Object");
					   }
					 }
					 public static void main(String[] args) {
					   foo("Hello World");
					   Zork();
					 }
					}
					class Y {}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					case Integer t, String s when s.length > 0, X x when x.hashCode() > 10 : System.out.println("Integer, String or X");
					             ^
				Named pattern variables are not allowed here
				----------
				2. ERROR in X.java (at line 4)
					case Integer t, String s when s.length > 0, X x when x.hashCode() > 10 : System.out.println("Integer, String or X");
					                ^^^^^^^^^^^^^^^^^^^^^^^^^^
				Cannot mix pattern with other case labels
				----------
				3. ERROR in X.java (at line 4)
					case Integer t, String s when s.length > 0, X x when x.hashCode() > 10 : System.out.println("Integer, String or X");
					                       ^
				Named pattern variables are not allowed here
				----------
				4. ERROR in X.java (at line 4)
					case Integer t, String s when s.length > 0, X x when x.hashCode() > 10 : System.out.println("Integer, String or X");
					                         ^^^^^^^^^^^^^^^^^
				Syntax error on token(s), misplaced construct(s)
				----------
				5. ERROR in X.java (at line 4)
					case Integer t, String s when s.length > 0, X x when x.hashCode() > 10 : System.out.println("Integer, String or X");
					                                            ^^^^^^^^^^^^^^^^^^^^^^^^^^
				Cannot mix pattern with other case labels
				----------
				6. ERROR in X.java (at line 4)
					case Integer t, String s when s.length > 0, X x when x.hashCode() > 10 : System.out.println("Integer, String or X");
					                                              ^
				Named pattern variables are not allowed here
				----------
				7. ERROR in X.java (at line 4)
					case Integer t, String s when s.length > 0, X x when x.hashCode() > 10 : System.out.println("Integer, String or X");
					                                                     ^
				x cannot be resolved
				----------
				8. ERROR in X.java (at line 10)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""");
	}
	public void testBug573516_007() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 private static void foo(Object o) {
					   switch (o) {
					     case Integer t, String : System.out.println("Error should be flagged for String");
					     default : System.out.println("Object");
					   }
					 }
					 public static void main(String[] args) {
					   foo("Hello World");
					   Zork();
					 }
					}
					class Y {}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					case Integer t, String : System.out.println("Error should be flagged for String");
					                ^^^^^^
				Cannot mix pattern with other case labels
				----------
				2. ERROR in X.java (at line 4)
					case Integer t, String : System.out.println("Error should be flagged for String");
					                ^^^^^^
				String cannot be resolved to a variable
				----------
				3. ERROR in X.java (at line 10)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""");
	}
	public void testBug573516_008() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 private static void foo(Object o) {
					   switch (o.hashCode()) {
					     case Integer t, String : System.out.println("Error should be flagged for Integer and String");
					     default : System.out.println("Object");
					   }
					 }
					 public static void main(String[] args) {
					   foo("Hello World");
					   Zork();
					 }
					}
					class Y {}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					case Integer t, String : System.out.println("Error should be flagged for Integer and String");
					     ^^^^^^^^^
				Type mismatch: cannot convert from int to Integer
				----------
				2. ERROR in X.java (at line 4)
					case Integer t, String : System.out.println("Error should be flagged for Integer and String");
					                ^^^^^^
				Cannot mix pattern with other case labels
				----------
				3. ERROR in X.java (at line 4)
					case Integer t, String : System.out.println("Error should be flagged for Integer and String");
					                ^^^^^^
				String cannot be resolved to a variable
				----------
				4. ERROR in X.java (at line 10)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""");
	}
	public void testBug573516_009() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 private static void foo(Object o) {
					   switch (o.hashCode()) {
					     case null, default : System.out.println("Default");
					     default : System.out.println("Object");
					   }
					 }
					 public static void main(String[] args) {
					   foo("Hello World");
					   Zork();
					 }
					}
					class Y {}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					case null, default : System.out.println("Default");
					     ^^^^
				Type mismatch: cannot convert from null to int
				----------
				2. ERROR in X.java (at line 5)
					default : System.out.println("Object");
					^^^^^^^
				The default case is already defined
				----------
				3. ERROR in X.java (at line 10)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""");
	}
	public void testBug573516_010() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 private static void foo(Object o) {
					   switch (o.hashCode()) {
					     case String s, default : System.out.println("Error should be flagged for String and default");
					     default : System.out.println("Object");
					   }
					 }
					 public static void main(String[] args) {
					   foo("Hello World");
					   Zork();
					 }
					}
					class Y {}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					case String s, default : System.out.println("Error should be flagged for String and default");
					     ^^^^^^^^
				Type mismatch: cannot convert from int to String
				----------
				2. ERROR in X.java (at line 4)
					case String s, default : System.out.println("Error should be flagged for String and default");
					               ^^^^^^^
				Cannot mix pattern with other case labels
				----------
				3. ERROR in X.java (at line 4)
					case String s, default : System.out.println("Error should be flagged for String and default");
					               ^^^^^^^
				A 'default' can occur after 'case' only as a second case label expression and that too only if 'null' precedes  in 'case null, default'\s
				----------
				4. ERROR in X.java (at line 5)
					default : System.out.println("Object");
					^^^^^^^
				The default case is already defined
				----------
				5. ERROR in X.java (at line 10)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""");
	}
	public void testBug573516_011() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 private static void foo(Object o) {
					   switch (o.hashCode()) {
					     case var s : System.out.println("Error should be ANY_PATTERN");
					     default : System.out.println("Object");
					   }
					 }
					 public static void main(String[] args) {
					   foo("Hello World");
					   Zork();
					 }
					}
					class Y {}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					case var s : System.out.println("Error should be ANY_PATTERN");
					     ^^^
				'var' is not allowed here
				----------
				2. ERROR in X.java (at line 10)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""");
	}
	public void testBug574228_001() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 private static void foo(Object o) {
					   switch (o) {
					     case 1: System.out.println("Integer"); break;
					     default : System.out.println("Object");
					   }
					 }
					   public static void main(String[] args) {
					   foo("Hello World");
					     Zork();
					 }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					case 1: System.out.println("Integer"); break;
					     ^
				Type mismatch: cannot convert from int to Object
				----------
				2. ERROR in X.java (at line 10)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""");
	}

	public void testBug573936_01() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						 private static void foo(Object o) {
						   switch (o) {
						     case Integer I:\s
						       System.out.println("Integer");\s
						       System.out.println(I);\s
						     case String s when s.length()>1:\s
						       System.out.println("String s && s.length()>1");\s
						       System.out.println(s);\s
						       break;// error no fallthrough allowed in pattern
						     case X x:
						       System.out.println("X");\s
						       System.out.println(x);
						       break;
						     default : System.out.println("Object");\s
						   }
						 }  \s
						   public static void main(String[] args) {
						   foo("Hello World!");
						     foo("H");
						   foo(bar());
						 }
						   public static Object bar() { return new Object();}
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 7)
						case String s when s.length()>1:\s
						^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					Illegal fall-through to a pattern
					----------
					""");
	}
	public void testBug573939_01() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						 private static void foo(Object o) {
						   switch (o) {
						     case Integer s : System.out.println("Integer");
						     case String s1: System.out.println("String ");
						     default : System.out.println("Object");
						   }
						 }
						 public static void main(String[] args) {
						   foo("Hello World");
						   Zork();
						 }
						}
						class Y {}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 5)
						case String s1: System.out.println("String ");
						^^^^^^^^^^^^^^
					Illegal fall-through to a pattern
					----------
					2. ERROR in X.java (at line 11)
						Zork();
						^^^^
					The method Zork() is undefined for the type X
					----------
					""");
	}
	public void testBug573939_02() {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						 private static void foo(Object o) {
						   switch (o) {
						     case Integer I: System.out.println("Integer"); break;
						     case String s when s.length()>1: System.out.println("String > 1"); break;
						     case String s1: System.out.println("String"); break;
						     case X x: System.out.println("X"); break;
						     default : System.out.println("Object");
						   }
						 }
						   public static void main(String[] args) {
						   foo("Hello World!");
						   foo("H");
						   foo(bar());
						 }
						   public static Object bar() { return new Object();}
						}""",
				},
				"""
					String > 1
					String
					Object""");
	}
	public void testBug573939_03() {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						 private static void foo(Object o) {
						   switch (o) {
						     case Integer I:\s
						       System.out.println("Integer");\s
						       System.out.println(I);\s
						       break;\s
						     case String s when s.length()>1:\s
						       System.out.println("String s when s.length()>1");\s
						       System.out.println(s);\s
						       break;
						     case String s1:\s
						       System.out.println("String");\s
						       System.out.println(s1);
						       break;\s
						     case X x:
						       System.out.println("X");\s
						       System.out.println(x);
						       break;
						     default : System.out.println("Object");\s
						   }
						 }  \s
						   public static void main(String[] args) {
						   foo("Hello World!");
						     foo("H");
						   foo(bar());
						 }
						   public static Object bar() { return new Object();}
						}""",
				},
				"""
					String s when s.length()>1
					Hello World!
					String
					H
					Object""");
	}
	public void testBug573939_03b() {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						 private static void foo(Object o) {
						   switch (o) {
						     case Integer I:\s
						       System.out.println("Integer");\s
						       System.out.println(I);\s
						       break;\s
						     case String s when s.length()>1:\s
						       System.out.println("String s when s.length()>1");\s
						       System.out.println(s);\s
						       break;
						     case String s:\s
						       System.out.println("String");\s
						       System.out.println(s);
						       break;\s
						     case X x:
						       System.out.println("X");\s
						       System.out.println(x);
						       break;
						     default : System.out.println("Object");\s
						   }
						 }  \s
						   public static void main(String[] args) {
						   foo("Hello World!");
						     foo("H");
						   foo(bar());
						 }
						   public static Object bar() { return new Object();}
						}""",
				},
				"""
					String s when s.length()>1
					Hello World!
					String
					H
					Object""");
	}
	public void test045() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public abstract class X {
					  public static void main(String[] args) {
					    switch (args.length) {
					      case 1:
					        final int j = 1;
					      case 2:
					        switch (5) {
					          case j:
					        }
					    }
					  }
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 8)
					case j:
					     ^
				The local variable j may not have been initialized
				----------
				""");
	}
	public void testBug574525_01() {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						 private static void foo(Object o) {
						   switch (o) {
						     case Integer I:\s
						       System.out.println("Integer");\s
						       System.out.println(I);\s
						       break;\s
						     case null:
						       System.out.println("NULL");\s
						       break;
						     default : System.out.println("Object");\s
						   }
						 }  \s
						   public static void main(String[] args) {
						     foo(null);
						 }
						}""",
				},
				"NULL");
	}
	public void testBug574525_02() {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						 private static void foo(Object o) {
						   switch (o) {
						     case Integer I:\s
						       System.out.println("Integer");\s
						       System.out.println(I);\s
						       break;\s
						     case String s when s.length()>1:\s
						       System.out.println("String s when s.length()>1");\s
						       System.out.println(s);\s
						       break;
						     case String s1:\s
						       System.out.println("String");\s
						       System.out.println(s1);
						       break;\s
						     case X x:
						       System.out.println("X");\s
						       System.out.println(x);
						       break;
						     case null:
						       System.out.println("NULL");\s
						       break;
						     default : System.out.println("Object");\s
						   }
						 }  \s
						   public static void main(String[] args) {
						   foo("Hello World!");
						   foo(null);
						   foo(bar());
						 }
						   public static Object bar() { return new Object();}
						}""",
				},
				"""
					String s when s.length()>1
					Hello World!
					NULL
					Object""");
	}
	public void testBug574525_03() {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						 private static void foo(Integer o) {
						   switch (o) {
						     case 10:\s
						       System.out.println("Integer");\s
						       System.out.println(o);\s
						       break;\s
						     case null:
						       System.out.println("NULL");\s
						       break;
						     default : System.out.println(o);\s
						   }
						 }  \s
						   public static void main(String[] args) {
						     foo(0);
						 }
						}""",
				},
				"0");
	}
	public void testBug574525_04() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						 private static void foo(int o) {
						   switch (o) {
						     case 10:\s
						       System.out.println("Integer");\s
						       System.out.println(o);\s
						       break;\s
						     case null:
						       System.out.println("NULL");\s
						       break;
						     default : System.out.println(o);\s
						   }
						 }  \s
						   public static void main(String[] args) {
						     foo(0);
						 }
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 8)
						case null:
						     ^^^^
					Type mismatch: cannot convert from null to int
					----------
					""");
	}
	public void testBug574538_01() {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						 public static void main(String[] args) {
						   foo(Integer.valueOf(11));
						   foo(Integer.valueOf(9));
						 }
						
						 private static void foo(Object o) {
						   switch (o) {
						   case Integer i when i>10:
						     System.out.println("Greater than 10:" + o);
						     break;
						   case Integer j when j>0:
						     System.out.println("Greater than 0:" + o);
						     break;
						   default:
						     System.out.println("Object" + o);
						   }
						 }
						}""",
				},
				"Greater than 10:11\n" +
				"Greater than 0:9");
	}
	public void testBug574538_02() {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						 public static void main(String[] args) {
						   foo1(Integer.valueOf(10));
						   foo1(Integer.valueOf(11));
						   foo1("Hello World!");
						 }
						
						 private static void foo1(Object o) {
						   switch (o) {
						   case Integer i when i>10 -> System.out.println("Greater than 10:");
						   case String s when s.equals("ff") -> System.out.println("String:" + s);
						   default -> System.out.println("Object:" + o);
						   }
						 }
						}""",
				},
				"""
					Object:10
					Greater than 10:
					Object:Hello World!""");
	}

	public void testBug574549_01() {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						 public static void main(String[] args) {
						   foo("Hello World!");
						 }
						
						 private static void foo(Object o) {
						   switch (o) {
						    case null, default:
						     System.out.println("Object: " + o);
						   }
						 }
						}""",
				},
				"Object: Hello World!");
	}
	public void testBug574549_02() {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						 public static void main(String[] args) {
						   foo(Integer.valueOf(11));
						   foo(Integer.valueOf(9));
						   foo("Hello World!");
						 }
						
						 private static void foo(Object o) {
						   switch (o) {
						   case Integer i when i>10:
						     System.out.println("Greater than 10:" + o);
						     break;
						   case Integer j when j>0:
						     System.out.println("Greater than 0:" + o);
						     break;
						   case null,default:
						     System.out.println("Give Me Some SunShine:" + o);
						   }
						 }
						}""",
				},
				"""
					Greater than 10:11
					Greater than 0:9
					Give Me Some SunShine:Hello World!""");
	}
	public void testBug574549_03() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						 public static void main(String[] args) {
						   foo("Hello World!");
						 }
						
						 private static void foo(Object o) {
						   switch (o) {
						   case Integer i :
						     System.out.println("Integer:" + o);
						     break;
						   case null, default:
						     System.out.println("Object" + o);
						   case null, default:
						     System.out.println("Give me Some Sunshine" + o);
						   }
						 }
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 11)
						case null, default:
						     ^^^^
					Duplicate case
					----------
					2. ERROR in X.java (at line 13)
						case null, default:
						     ^^^^
					Duplicate case
					----------
					3. ERROR in X.java (at line 13)
						case null, default:
						           ^^^^^^^
					The default case is already defined
					----------
					""");
	}
	public void testBug574549_04() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						 public static void main(String[] args) {
						   foo("Hello World!");
						 }
						
						 private static void foo(Object o) {
						   switch (o) {
						   case Integer i :
						     System.out.println("Integer:" + o);
						     break;
						   case default:
						     System.out.println("Object" + o);
						   default:
						     System.out.println("Give me Some Sunshine" + o);
						   }
						 }
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 11)
						case default:
						     ^^^^^^^
					A \'default\' can occur after \'case\' only as a second case label expression and that too only if \'null\' precedes  in \'case null, default\'\s
					----------
					2. ERROR in X.java (at line 13)
						default:
						^^^^^^^
					The default case is already defined
					----------
					""");
	}
	// Test that when a pattern variable is unused and when the OPTION_PreserveUnusedLocal
	// option is used, no issue is reported at runtime.
	public void testBug573937_1() {
		Map<String,String> options = getCompilerOptions();
		String opt = options.get(CompilerOptions.OPTION_PreserveUnusedLocal);
		try {
			options.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.DISABLED);
			this.runConformTest(
				new String[] {
				"X.java",
					"""
						public class X {
							public static void main(String[] args) {
								System.out.println("Hello");
							}
							public static void foo(Object o) {
								switch (o) {
									case String s:
										break;
									default:
										break;
								}
							}
						}""",
					},
					"Hello",
					options);
		} finally {
			options.put(CompilerOptions.OPTION_PreserveUnusedLocal, opt);
		}
	}
	// A simple pattern variable in a case is not visible in the
	// following case statement
	public void testBug573937_2() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public static void foo(Object o) {
									switch (o) {
										case String s:
											System.out.println(s);
											break;
										case Integer i:
											System.out.println(s);
										default:
											break;
									}
								}
							}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 8)
						System.out.println(s);
						                   ^
					s cannot be resolved to a variable
					----------
					""");
	}
	// Same as above, but without break statement
	public void testBug573937_3() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public static void foo(Object o) {
									switch (o) {
										case String s:
											System.out.println(s);
										case Integer i:
											System.out.println(s);
										default:
											System.out.println(s);
											break;
									}
								}
							}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 7)
						System.out.println(s);
						                   ^
					s cannot be resolved to a variable
					----------
					2. ERROR in X.java (at line 9)
						System.out.println(s);
						                   ^
					s cannot be resolved to a variable
					----------
					""");
	}
	// Test that compiler rejects attempts to redeclare local variable
	// with same name as a pattern variable
	public void testBug573937_4() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public static void foo(Object o) {
									switch (o) {
										case String s:
											String s = null;
											System.out.println(s);
											break;
										default:
											break;
									}
								}
							}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 5)
						String s = null;
						       ^
					A pattern variable with the same name is already defined in the statement
					----------
					""");
	}
	// Test that compiler allows local variable with same name as a
	// pattern variable in a different case statement
	public void testBug573937_5() {
		this.runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public static void foo(Object o) {
									switch (o) {
										case String s:
											System.out.println(s);
											break;
										default:
											String s = null;
											break;
									}
								}
								public static void main(String[] args) {
									foo("hello");
								}
							}""",
				},
				"hello");
	}
	// Test that a pattern variable can't use name of an already existing local
	// variable
	public void testBug573937_6() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public static void foo(Object o) {
									switch (o) {
										case String o:
											System.out.println(o);
											break;
										default:
											break;
									}
								}
							}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						case String o:
						            ^
					Duplicate local variable o
					----------
					""");
	}
	// Test that compiler rejects attempts to redeclare another pattern
	// variable (instanceof) with same name as that a pattern variable in
	// that case statement
	public void testBug573937_7() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public static void foo(Object o) {
									switch (o) {
										case String s1:
											if (o instanceof String s1) {
												System.out.println(s1);
											}
											break;
										default:
											break;
									}
								}
							} """,
				},
				"""
					----------
					1. ERROR in X.java (at line 5)
						if (o instanceof String s1) {
						                        ^^
					A pattern variable with the same name is already defined in the statement
					----------
					""");
	}
	// Test that when multiple case statements declare pattern variables
	// with same name, correct ones are used in their respective scopes.
	public void testBug573937_8() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public static void foo(Object o) {
									switch (o) {
										case String s1:
											System.out.println(s1.length());
											break;
										case Integer s1:
											System.out.println(s1.length());
											break;
										default:
											break;
									}
								}
							} """,
				},
				"""
					----------
					1. ERROR in X.java (at line 8)
						System.out.println(s1.length());
						                      ^^^^^^
					The method length() is undefined for the type Integer
					----------
					""");
	}
	// Test that a pattern variable declared in the preceding case statement
	// can't be used in the case statement itself
	public void testBug573937_9() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public static void foo(Object o) {
									switch (o) {
										case Integer i1:
											break;
										case String s1 when s1.length() > i1:
												System.out.println(s1.length());
											break;
										default:
											break;
									}
								}
							} """,
				},
				"""
					----------
					1. ERROR in X.java (at line 6)
						case String s1 when s1.length() > i1:
						                                  ^^
					i1 cannot be resolved to a variable
					----------
					""");
	}
	// Test that redefining pattern variables with null is allowed
	// and produce expected result (NPE) when run.
	public void testBug573937_10() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"X.java",
				"""
					public class X {
					@SuppressWarnings("null")\
						public static void foo(Object o) {
						  try {
							switch (o) {
								case String s1 when s1.length() == 0:
										break;\
								case String s1:
										s1 = null;
										System.out.println(s1.length());
									break;
								default:
									break;
							}
						  } catch(Exception e) {
					    System.out.println(e.getMessage());
						  };
						}
						public static void main(String[] args) {
							foo("hello");
						}
					}""",
		};
		runner.expectedOutputString = "Cannot invoke \"String.length()\" because \"s1\" is null";
		runner.expectedJavacOutputString = "Cannot invoke \"String.length()\" because \"<local4>\" is null";
		runner.vmArguments = new String[] {"--enable-preview"};
		runner.customOptions = getCompilerOptions();
		runner.javacTestOptions = JavacTestOptions.forReleaseWithPreview(SwitchPatternTest.previewLevel);
		runner.runConformTest();
	}
	// Test that a pattern variable is allowed in a switch label throw
	// statement and when run, produces expected result
	public void testBug573937_11() {
		Runner runner = new Runner();
		runner.testFiles = new String[] {
				"X.java",
				"""
					public class X {
						public static void foo(Object o) throws Exception {
							switch (o) {
								case String s1:
									throw new Exception(s1);
								default:
									break;
							}
						}
						public static void main(String[] args) throws Exception {
							try {
							  foo("hello");
							} catch(Exception e) {
							  e.printStackTrace(System.out);
							};
						}
					} """,
		};
		runner.expectedOutputString = """
			java.lang.Exception: hello
				at X.foo(X.java:5)
				at X.main(X.java:12)""";
		runner.expectedJavacOutputString = """
			java.lang.Exception: hello
				at X.foo(X.java:5)
				at X.main(X.java:12)""";
		runner.vmArguments = new String[] {"--enable-preview"};
		runner.customOptions = getCompilerOptions();
		runner.javacTestOptions = JavacTestOptions.forReleaseWithPreview(SwitchPatternTest.previewLevel);
		runner.runConformTest();
	}
	// A non effectively final referenced from the RHS of the guarding expression
	// is reported by the compiler.
	public void testBug574612_1() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public static void foo(Object o) {
									int len = 2;
									switch (o) {
									case String o1 when o1.length() > len:
										len = 0;
									break;
									default:
										break;
									}
								}
							} """,
				},
				"""
					----------
					1. ERROR in X.java (at line 5)
						case String o1 when o1.length() > len:
						                                  ^^^
					Local variable len referenced from a guard must be final or effectively final
					----------
					""");
	}
	// A non effectively final referenced from the LHS of the guarding expression
	// is reported by the compiler.
	public void testBug574612_2() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public static void foo(Object o) {
									int len = 2;
									switch (o) {
									case String o1 when len < o1.length():
										len = 0;
									break;
									default:
										break;
									}
								}
							} """,
				},
				"""
					----------
					1. ERROR in X.java (at line 5)
						case String o1 when len < o1.length():
						                    ^^^
					Local variable len referenced from a guard must be final or effectively final
					----------
					""");
	}
	// An explicitly final local variable, also referenced in a guarding expression of a pattern
	// and later on re-assigned is only reported for the explicit final being modified
	public void testBug574612_3() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public static void foo(Object o) {
									final int len = 2;
									switch (o) {
									case String o1 when len < o1.length():
										len = 0;
									break;
									default:
										break;
									}
								}
							} """,
				},
				"""
					----------
					1. ERROR in X.java (at line 6)
						len = 0;
						^^^
					The final local variable len cannot be assigned. It must be blank and not using a compound assignment
					----------
					""");
	}
	public void testBug574612_4() {
		this.runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public static void foo(Object o) {
									int len = 2;
									switch (o) {
									case String o1 when len < o1.length():
										System.out.println(o1);
									break;
									default:
										break;
									}
								}
								public static void main(String[] args) throws Exception {
									foo("hello");
								}
							} """,
				},
				"hello");
	}
	public void testBug574719_001() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 public static int foo(Integer o) {
					   int k = 0;
					   switch (o) {
					     case 0, default   : k = 1;
					   }
					   return k;
					 }\s
					 public static void main(String[] args) {
					   System.out.println(foo(100 ));
					 }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					case 0, default   : k = 1;
					        ^^^^^^^
				A \'default\' can occur after \'case\' only as a second case label expression and that too only if \'null\' precedes  in \'case null, default\'\s
				----------
				""");
	}
	public void testBug574719_006() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 public static int foo(Integer o) {
					   int k = 0;
					   switch (o) {
					     case 0  : k = 2; break;
					     case 1, default, default   : k = 1;
					   }
					   return k;
					 }\s
					 public static void main(String[] args) {
					   System.out.println(foo(100));
					   System.out.println(foo(0));
					 }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 6)
					case 1, default, default   : k = 1;
					        ^^^^^^^
				A \'default\' can occur after \'case\' only as a second case label expression and that too only if \'null\' precedes  in \'case null, default\'\s
				----------
				2. ERROR in X.java (at line 6)
					case 1, default, default   : k = 1;
					                 ^^^^^^^
				The default case is already defined
				----------
				3. ERROR in X.java (at line 6)
					case 1, default, default   : k = 1;
					                 ^^^^^^^
				A \'default\' can occur after \'case\' only as a second case label expression and that too only if \'null\' precedes  in \'case null, default\'\s
				----------
				""");
	}
	public void testBug574561_001() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 public static int foo(Integer o) {
					   int k = 0;
					   switch (o) {
					     default, default  : k = 2; break;
					   }
					   return k;
					 }\s
					 public static void main(String[] args) {
					   System.out.println(foo(100));
					   System.out.println(foo(0));
					 }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					default, default  : k = 2; break;
					       ^
				Syntax error on token ",", : expected
				----------
				""");
	}
	public void testBug574561_002() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 public static int foo(Integer o) {
					   int k = 0;
					   switch (o) {
					     case default, 1, default   : k = 1;
					   }
					   return k;
					 }\s
					 public static void main(String[] args) {
					   System.out.println(foo(100));
					   System.out.println(foo(0));
					 }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					case default, 1, default   : k = 1;
					     ^^^^^^^
				A \'default\' can occur after \'case\' only as a second case label expression and that too only if \'null\' precedes  in \'case null, default\'\s
				----------
				2. ERROR in X.java (at line 5)
					case default, 1, default   : k = 1;
					                 ^^^^^^^
				The default case is already defined
				----------
				3. ERROR in X.java (at line 5)
					case default, 1, default   : k = 1;
					                 ^^^^^^^
				A \'default\' can occur after \'case\' only as a second case label expression and that too only if \'null\' precedes  in \'case null, default\'\s
				----------
				""");
	}
	public void testBug574561_003() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 public static int foo(Integer o) {
					   int k = 0;
					   switch (o) {
					     case default, 1, default   : k = 1;
					   }
					   return k;
					 }\s
					 public static void main(String[] args) {
					   System.out.println(foo(100));
					   System.out.println(foo(0));
					 }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					case default, 1, default   : k = 1;
					     ^^^^^^^
				A \'default\' can occur after \'case\' only as a second case label expression and that too only if \'null\' precedes  in \'case null, default\'\s
				----------
				2. ERROR in X.java (at line 5)
					case default, 1, default   : k = 1;
					                 ^^^^^^^
				The default case is already defined
				----------
				3. ERROR in X.java (at line 5)
					case default, 1, default   : k = 1;
					                 ^^^^^^^
				A \'default\' can occur after \'case\' only as a second case label expression and that too only if \'null\' precedes  in \'case null, default\'\s
				----------
				""");
	}
	public void testBug574793_001() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 public static void main(String[] args) {}
					 private static void foo1(int o) {
					   switch (o) {
					     case null  -> System.out.println("null");
					     case 20  -> System.out.println("20");
					   }
					 }
					 private static void foo(Object o) {
					   switch (o) {
					   case "F"  :
					     break;
					   case 2 :
					     break;
					   default:
					     break;
					   }
					 }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					switch (o) {
					        ^
				An enhanced switch statement should be exhaustive; a default label expected
				----------
				2. ERROR in X.java (at line 5)
					case null  -> System.out.println("null");
					     ^^^^
				Type mismatch: cannot convert from null to int
				----------
				3. ERROR in X.java (at line 11)
					case "F"  :
					     ^^^
				Type mismatch: cannot convert from String to Object
				----------
				4. ERROR in X.java (at line 13)
					case 2 :
					     ^
				Type mismatch: cannot convert from int to Object
				----------
				""");
	}
	public void testBug574559_001() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 public static void main(String[] args) {}
					 public static void foo1(Integer o) {
					   switch (o) {
					     case 1, Integer i  -> System.out.println(o);
					   }
					 }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					case 1, Integer i  -> System.out.println(o);
					        ^^^^^^^^^
				Cannot mix pattern with other case labels
				----------
				""");
	}
	public void testBug574559_002() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 public static void main(String[] args) {}
					 private static void foo1(Integer o) {
					   switch (o) {
					     case  Integer i, 30  -> System.out.println(o);
					   }
					 }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					case  Integer i, 30  -> System.out.println(o);
					                 ^^
				Cannot mix pattern with other case labels
				----------
				2. ERROR in X.java (at line 5)
					case  Integer i, 30  -> System.out.println(o);
					                 ^^
				This case label is dominated by one of the preceding case labels
				----------
				""");
	}
	// Test that fall-through to a pattern is not allowed (label statement group has one statement)
	public void testBug573940_1() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						public void foo(Number n) {
							switch (n) {
							case Integer i :
								System.out.println(i);
							case Float f :
								System.out.println(f);
							case Object o : break;
							}
						}
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 6)
						case Float f :
						^^^^^^^^^^^^
					Illegal fall-through to a pattern
					----------
					2. ERROR in X.java (at line 8)
						case Object o : break;
						^^^^^^^^^^^^^
					Illegal fall-through to a pattern
					----------
					""");
	}
	// Test that fall-through to a pattern is not allowed (label statement group has zero statement)
	public void testBug573940_2() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						public void foo(Number n) {
							switch (n) {
							case Integer i :
							case Float f :
								System.out.println(f);
						     break;
							default : break;
							}
						}
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						case Integer i :
						^^^^^^^^^^^^^^
					Illegal fall-through from a case label pattern
					----------
					2. ERROR in X.java (at line 5)
						case Float f :
						^^^^^^^^^^^^
					Illegal fall-through to a pattern
					----------
					""");
	}
	// Test that fall-through to a pattern is not allowed (label statement group has zero statement)
	public void testBug573940_2a() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						public void foo(Number n) {
							switch (n) {
							default :
							case Float f :
								System.out.println(f);
						     break;
							}
						}
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 5)
						case Float f :
						     ^^^^^^^
					This case label is dominated by one of the preceding case labels
					----------
					""");
	}
	// Test that falling through from a pattern to a default is allowed
	public void testBug573940_3() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						public static void foo(Number n) {
								switch (n) {
								case Integer i :
									System.out.println(i);
								default:
									System.out.println("null");
								}
							}
						public static void main(String[] args) {
								foo(Integer.valueOf(5));
							}
						}""",
				},
				"5\n" +
				"null");
	}
	// Test that a case statement with pattern is allowed when statement group ends
	// with an Throw statement instead of a break statement
	public void testBug573940_4() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						public static void foo(Number n) {
								switch (n) {
								case Integer i :
									throw new IllegalArgumentException();
								default:
									System.out.println("null");
								}
							}
						public static void main(String[] args) {
								try{
									foo(Integer.valueOf(5));
								} catch(Exception e) {
								 	e.printStackTrace(System.out);
								}
							}
						}""",
				},
				"""
					java.lang.IllegalArgumentException
						at X.foo(X.java:5)
						at X.main(X.java:12)""");
	}
	// Test that switch expression with pattern variables is reported when a case statement
	// doesn't return any value.
	public void testBug573940_5() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
							public static void foo(Number n) {
								int j =\s
									switch (n) {
									case Integer i -> {
									}
									default -> {
										yield 1;
									}
								};
							}
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 6)
						}
						^^
					A switch labeled block in a switch expression should not complete normally
					----------
					""");
	}
	public void testBug574564_001() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 public static void main(String[] args) {
					   foo(new String("Hello"));
					 }
					 private static void foo(Object o) {
					   switch (o) {
					     case var i  -> System.out.println(0);
					     default -> System.out.println(o);
					   }
					 }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 7)
					case var i  -> System.out.println(0);
					     ^^^
				'var' is not allowed here
				----------
				""");
	}
	public void testBug574564_002() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 public static void main(String[] args) {
					   foo(new String("Hello"));
					 }
					 private static void foo(Object o) {
					   switch (o) {
					     case var i, var j, var k  -> System.out.println(0);
					     default -> System.out.println(o);
					   }
					 }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 7)
					case var i, var j, var k  -> System.out.println(0);
					     ^^^
				'var' is not allowed here
				----------
				2. ERROR in X.java (at line 7)
					case var i, var j, var k  -> System.out.println(0);
					         ^
				Named pattern variables are not allowed here
				----------
				3. ERROR in X.java (at line 7)
					case var i, var j, var k  -> System.out.println(0);
					            ^^^^^
				Cannot mix pattern with other case labels
				----------
				4. ERROR in X.java (at line 7)
					case var i, var j, var k  -> System.out.println(0);
					            ^^^
				'var' is not allowed here
				----------
				5. ERROR in X.java (at line 7)
					case var i, var j, var k  -> System.out.println(0);
					                ^
				Named pattern variables are not allowed here
				----------
				6. ERROR in X.java (at line 7)
					case var i, var j, var k  -> System.out.println(0);
					                   ^^^^^
				Cannot mix pattern with other case labels
				----------
				7. ERROR in X.java (at line 7)
					case var i, var j, var k  -> System.out.println(0);
					                   ^^^
				'var' is not allowed here
				----------
				8. ERROR in X.java (at line 7)
					case var i, var j, var k  -> System.out.println(0);
					                       ^
				Named pattern variables are not allowed here
				----------
				""");
	}
	public void testBug574564_003() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 public static void main(String[] args) {
					   foo(10);
					 }
					 private static void foo(Integer o) {
					   switch (o) {
					     case var i, 10  -> System.out.println(0);
					     default -> System.out.println(o);
					   }
					 }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 7)
					case var i, 10  -> System.out.println(0);
					     ^^^
				\'var\' is not allowed here
				----------
				2. ERROR in X.java (at line 7)
					case var i, 10  -> System.out.println(0);
					            ^^
				Cannot mix pattern with other case labels
				----------
				""");
	}
	public void testBug574564_004() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 public static void main(String[] args) {
					   foo(10);
					 }
					 private static void foo(Integer o) {
					   switch (o) {
					     case var i, 10, var k  -> System.out.println(0);
					     default -> System.out.println(o);
					   }
					 }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 7)
					case var i, 10, var k  -> System.out.println(0);
					     ^^^
				'var' is not allowed here
				----------
				2. ERROR in X.java (at line 7)
					case var i, 10, var k  -> System.out.println(0);
					            ^^
				Cannot mix pattern with other case labels
				----------
				3. ERROR in X.java (at line 7)
					case var i, 10, var k  -> System.out.println(0);
					                ^^^^^
				Cannot mix pattern with other case labels
				----------
				4. ERROR in X.java (at line 7)
					case var i, 10, var k  -> System.out.println(0);
					                ^^^
				'var' is not allowed here
				----------
				""");
	}
	public void testBug574564_005() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 public static void main(String[] args) {
					   foo(10);
					 }
					 private static void foo(Integer o) {
					   switch (o) {
					     case  10, null, var k  -> System.out.println(0);
					     default -> System.out.println(o);
					   }
					 }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 7)
					case  10, null, var k  -> System.out.println(0);
					          ^^^^
				A null case label has to be either the only expression in a case label or the first expression followed only by a default
				----------
				2. ERROR in X.java (at line 7)
					case  10, null, var k  -> System.out.println(0);
					                ^^^^^
				Cannot mix pattern with other case labels
				----------
				3. ERROR in X.java (at line 7)
					case  10, null, var k  -> System.out.println(0);
					                ^^^
				'var' is not allowed here
				----------
				""");
	}
	public void testBug574564_006() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 public static void main(String[] args) {
					   foo(10);
					 }
					 private static void foo(Integer o) {
					   switch (o) {
					     case  default, var k  -> System.out.println(0);
					     default -> System.out.println(o);
					   }
					 }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 7)
					case  default, var k  -> System.out.println(0);
					      ^^^^^^^
				A \'default\' can occur after \'case\' only as a second case label expression and that too only if \'null\' precedes  in \'case null, default\'\s
				----------
				2. ERROR in X.java (at line 7)
					case  default, var k  -> System.out.println(0);
					               ^^^
				\'var\' is not allowed here
				----------
				3. ERROR in X.java (at line 7)
					case  default, var k  -> System.out.println(0);
					               ^^^^^
				Cannot mix pattern with other case labels
				----------
				4. ERROR in X.java (at line 8)
					default -> System.out.println(o);
					^^^^^^^
				The default case is already defined
				----------
				""");
	}
	public void testBug574564_007() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 public static void main(String[] args) {
					   foo(10);
					 }
					 private static void foo(Integer o) {
					   switch (o) {
					     case  default, default, var k  -> System.out.println(0);
					     default -> System.out.println(o);
					   }
					 }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 7)
					case  default, default, var k  -> System.out.println(0);
					      ^^^^^^^
				A \'default\' can occur after \'case\' only as a second case label expression and that too only if \'null\' precedes  in \'case null, default\'\s
				----------
				2. ERROR in X.java (at line 7)
					case  default, default, var k  -> System.out.println(0);
					               ^^^^^^^
				The default case is already defined
				----------
				3. ERROR in X.java (at line 7)
					case  default, default, var k  -> System.out.println(0);
					               ^^^^^^^
				A \'default\' can occur after \'case\' only as a second case label expression and that too only if \'null\' precedes  in \'case null, default\'\s
				----------
				4. ERROR in X.java (at line 7)
					case  default, default, var k  -> System.out.println(0);
					                        ^^^
				\'var\' is not allowed here
				----------
				5. ERROR in X.java (at line 7)
					case  default, default, var k  -> System.out.println(0);
					                        ^^^^^
				Cannot mix pattern with other case labels
				----------
				6. ERROR in X.java (at line 8)
					default -> System.out.println(o);
					^^^^^^^
				The default case is already defined
				----------
				""");
	}
	public void testBug574564_008() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 public static void main(String[] args) {
					   foo(10);
					 }
					 private static void foo(Integer o) {
					   switch (o) {
					     case  default, 1, var k  -> System.out.println(0);
					     default -> System.out.println(o);
					   }
					 }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 7)
					case  default, 1, var k  -> System.out.println(0);
					      ^^^^^^^
				A \'default\' can occur after \'case\' only as a second case label expression and that too only if \'null\' precedes  in \'case null, default\'\s
				----------
				2. ERROR in X.java (at line 7)
					case  default, 1, var k  -> System.out.println(0);
					                  ^^^
				\'var\' is not allowed here
				----------
				3. ERROR in X.java (at line 7)
					case  default, 1, var k  -> System.out.println(0);
					                  ^^^^^
				Cannot mix pattern with other case labels
				----------
				4. ERROR in X.java (at line 8)
					default -> System.out.println(o);
					^^^^^^^
				The default case is already defined
				----------
				""");
	}
	public void testBug574564_009() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 private static void foo(Object o) {
					   switch (o) {
					     case String s, default, Integer i  -> System.out.println(0);
					   }
					 }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					case String s, default, Integer i  -> System.out.println(0);
					               ^^^^^^^
				Cannot mix pattern with other case labels
				----------
				2. ERROR in X.java (at line 4)
					case String s, default, Integer i  -> System.out.println(0);
					               ^^^^^^^
				A 'default' can occur after 'case' only as a second case label expression and that too only if 'null' precedes  in 'case null, default'\s
				----------
				3. ERROR in X.java (at line 4)
					case String s, default, Integer i  -> System.out.println(0);
					                        ^^^^^^^^^
				Cannot mix pattern with other case labels
				----------
				"""
);
	}
	public void testBug574564_010() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_20);
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 private static void foo(Object o) {
					   switch (o) {
					     case String s, default, Integer i  -> System.out.println(0);
					   }
					 }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					case String s, default, Integer i  -> System.out.println(0);
					               ^^^^^^^
				Cannot mix pattern with other case labels
				----------
				2. ERROR in X.java (at line 4)
					case String s, default, Integer i  -> System.out.println(0);
					               ^^^^^^^
				A 'default' can occur after 'case' only as a second case label expression and that too only if 'null' precedes  in 'case null, default'\s
				----------
				3. ERROR in X.java (at line 4)
					case String s, default, Integer i  -> System.out.println(0);
					                        ^^^^^^^^^
				Cannot mix pattern with other case labels
				----------
				""",
			null,
			true,
			options);
	}
	public void testBug574564_013() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 private static void foo(Object o) {
					   switch (o) {
					     case default, default -> System.out.println(0);
					   }
					 }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					case default, default -> System.out.println(0);
					     ^^^^^^^
				A \'default\' can occur after \'case\' only as a second case label expression and that too only if \'null\' precedes  in \'case null, default\'\s
				----------
				2. ERROR in X.java (at line 4)
					case default, default -> System.out.println(0);
					              ^^^^^^^
				The default case is already defined
				----------
				3. ERROR in X.java (at line 4)
					case default, default -> System.out.println(0);
					              ^^^^^^^
				A \'default\' can occur after \'case\' only as a second case label expression and that too only if \'null\' precedes  in \'case null, default\'\s
				----------
				""");
	}
	public void testBug574563_001() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 public static void main(String[] args) {}
					 private static void foo1(Integer o) {
					   switch (o) {
					     case null, null  -> System.out.println(o);
					     default  -> System.out.println(o);
					   }
					 }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					case null, null  -> System.out.println(o);
					     ^^^^
				Duplicate case
				----------
				2. ERROR in X.java (at line 5)
					case null, null  -> System.out.println(o);
					           ^^^^
				Duplicate case
				----------
				""");
	}
	public void testBug574563_002() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 public static void foo(Object o) {
					   switch (o) {
					     case null, Integer i  -> System.out.println(0);
					     default -> System.out.println(o);
					   }
					 }
					 public static void bar(Object o) {
					   Zork();
					 }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					case null, Integer i  -> System.out.println(0);
					           ^^^^^^^^^
				Cannot mix pattern with other case labels
				----------
				2. ERROR in X.java (at line 9)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""");
	}
	public void testBug574563_003() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 private static void foo(Object o) {
					   switch (o) {
					     case Integer i, null  -> System.out.println(0);
					     default -> System.out.println(o);
					   }
					   Zork();
					 }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					case Integer i, null  -> System.out.println(0);
					                ^^^^
				Cannot mix pattern with other case labels
				----------
				2. ERROR in X.java (at line 4)
					case Integer i, null  -> System.out.println(0);
					                ^^^^
				A null case label has to be either the only expression in a case label or the first expression followed only by a default
				----------
				3. ERROR in X.java (at line 7)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""");
	}
	public void testBug574563_004() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 private static void foo(Object o) {
					   switch (o) {
					     case null, Integer i when i > 10 -> System.out.println(0);
					     default -> System.out.println(o);
					   }
					   Zork();
					 }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					case null, Integer i when i > 10 -> System.out.println(0);
					           ^^^^^^^^^^^^^^^^^^^^^
				Cannot mix pattern with other case labels
				----------
				2. ERROR in X.java (at line 7)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""");
	}
	public void testBug574563_005() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 private static void foo(Object o) {
					   switch (o) {
					     case Integer i when i > 10, null  -> System.out.println(0);
					     default -> System.out.println(o);
					   }
					   Zork();
					 }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					case Integer i when i > 10, null  -> System.out.println(0);
					                            ^^^^
				Cannot mix pattern with other case labels
				----------
				2. ERROR in X.java (at line 4)
					case Integer i when i > 10, null  -> System.out.println(0);
					                            ^^^^
				A null case label has to be either the only expression in a case label or the first expression followed only by a default
				----------
				3. ERROR in X.java (at line 7)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""");
	}
	public void testBug575030_01() {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						 public static void main(String[] args) {
						   foo("Hello World!");
						 }
						
						 private static void foo(String o) {
						   switch (o) {
						     case String s -> System.out.println(s);
						   }
						 }
						}""",
				},
				"Hello World!");
	}
	public void testBug574614_001() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 public static void main(String[] args) {
					   foo(Long.valueOf(10));
					 }
					 private static void foo(Object o) {
					   String s1 = " Hello ";
					   String s2 = "World!";
					   switch (o) {
					     case Integer I when I > 10: break;
					      case X J: break;
					      case String s : break;
					      default:
					       s1 = new StringBuilder(String.valueOf(s1)).append(String.valueOf(s2)).toString();
					       System.out.println(s1);
					       break;\s
					   }
					 }
					}""",
			},
			"Hello World!");
	}
	public void testBug574614_002() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 public static void main(String[] args) {
					   foo(Long.valueOf(0));
					 }
					 private static void foo(Object o) {
					   switch (o) {
					     case Integer I:
					       break;
					      case String s :
					       break;
					      case X J:
					       break;
					      default:
					       String s1 = "Hello ";
					       String s2 = "World!";
					       s1 = s1 +s2;\s
					       System.out.println(s1);
					       break;
					   }
					 }\s
					}""",
			},
			"Hello World!");
	}
	public void testBug573921_1() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						 private static void foo(Object o) {
								switch(o) {
									case CharSequence cs ->
									System.out.println("A sequence of length " + cs.length());
									case String s when s.length() > 0 ->\s
									System.out.println("A string: " + s);
									default -> {
										break;
									}\s
								}
						 }
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 6)
						case String s when s.length() > 0 ->\s
						     ^^^^^^^^
					This case label is dominated by one of the preceding case labels
					----------
					""");
	}
	public void testBug573921_2() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						 private static void foo(Object o) {
								switch(o) {
									case CharSequence cs:
										System.out.println("A sequence of length " + cs.length());
										break;
									case String s:
										System.out.println("A string: " + s);
										break;
									default:\s
										break;
								}
						 }
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 7)
						case String s:
						     ^^^^^^^^
					This case label is dominated by one of the preceding case labels
					----------
					""");
	}
	public void testBug573921_3() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						 public static void main(String[] args) {
						   foo("Hello!");
						 }
						 private static void foo(Object o) {
								switch(o) {
									case String s:
										System.out.println("String:" + s);
										break;
									case CharSequence cs:
										System.out.println("A CS:" + cs);
										break;
									default:\s
										break;
								}
						 }
						}""",
				},
				"String:Hello!");
	}
	public void testBug573921_4() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						 public static void main(String[] args) {
						   foo(new StringBuffer("Hello!"));
						 }
						 private static void foo(Object o) {
								switch(o) {
									case String s:
										System.out.println("String:" + s);
										break;
									case CharSequence cs:
										System.out.println("A CS:" + cs.toString());
										break;
									default:\s
										break;
								}
						 }
						}""",
				},
				"A CS:Hello!");
	}
	public void testBug573921_5() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						 public static void main(String[] args) {
						   foo("Hello");
						 }
						 private static void foo(Object o) {
								switch(o) {
								case String s when s.length() < 5 :
									System.out.println("1:" + s);
									break;
								case String s when s.length() == 5:
									System.out.println("2:" + s);
									break;
								default : System.out.println("Object");
							}
						 }
						}""",
				},
				"2:Hello");
	}
	public void testBug573921_6() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						 public static void main(String[] args) {
						   foo("");
						 }
						 private static void foo(Object o) {
								switch(o) {
								case String s when s.length() < 5 :
									System.out.println("1:" + s);
									break;
								case String s when s.length() == 5:
									System.out.println("2:" + s);
									break;
								default : System.out.println("Object");
							}
						 }
						}""",
				},
				"1:");
	}
	public void testBug573921_7() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						import java.util.*;
						public class X {
						 @SuppressWarnings("rawtypes")
						 private static void foo(Object o) {
								switch(o) {
								case List cs:
									System.out.println("A sequence of length " + cs.size());
									break;
								case List<String> s:\s
									System.out.println("A string: " + s);
									break;
								} \
						 }
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 5)
						switch(o) {
						       ^
					An enhanced switch statement should be exhaustive; a default label expected
					----------
					2. ERROR in X.java (at line 9)
						case List<String> s:\s
						     ^^^^^^^^^^^^^^
					Type Object cannot be safely cast to List<String>
					----------
					3. ERROR in X.java (at line 9)
						case List<String> s:\s
						     ^^^^^^^^^^^^^^
					This case label is dominated by one of the preceding case labels
					----------
					""");
	}
	public void testBug573921_8() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						import java.util.*;
						public class X {
						 @SuppressWarnings("rawtypes")
						 private static void foo(Object o) {
								switch(o.hashCode()) {
								case String s:
									break;
								default:\s
									break;
								} \
						 }
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 6)
						case String s:
						     ^^^^^^^^
					Type mismatch: cannot convert from int to String
					----------
					""");
	}
	public void testBug573921_9() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						import java.util.*;
						public class X {
						 @SuppressWarnings("rawtypes")
						 private static void foo(Object o) {
								switch(o) {
								case Object o1:
									break;
								default:\s
									break;
								} \
						 }
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 8)
						default:\s
						^^^^^^^
					Switch case cannot have both unconditional pattern and default label
					----------
					""");
	}
	public void testBug573921_10() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						import java.util.*;
						public class X {
						 @SuppressWarnings("rawtypes")
						 private static void foo(List<String> o) {
								switch(o) {
								case List o1:
									break;
								default:\s
									break;
								} \
						 }
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 8)
						default:\s
						^^^^^^^
					Switch case cannot have both unconditional pattern and default label
					----------
					""");
	}
	public void testBug573921_11() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						import java.util.*;
						public class X {
						 @SuppressWarnings("rawtypes")
						 private static void foo(String s) {
								switch(s) {
								case CharSequence cs:
									break;
								default:\s
									break;
								} \
						 }
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 8)
						default:\s
						^^^^^^^
					Switch case cannot have both unconditional pattern and default label
					----------
					""");
	}
	public void testBug575049_001() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					sealed interface I permits A,B,C {}
					final class A implements I {}
					final class B implements I {}
					record C(int j) implements I {} // Implicitly final
					public class X {
					 static int testSealedCoverage(I i) {
					   return switch (i) {
					   case A a -> 0;
					   case B b -> 1;
					   case C c -> 2; // No default required!
					   default -> 3;
					   };
					 }
					 public static void main(String[] args) {
					   A a = new A();
					   System.out.println(testSealedCoverage(a));
					 }
					}""",
			},
			"0");
	}
	public void testBug575049_002() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					sealed interface I permits A,B,C {}
					final class A implements I {}
					final class B implements I {}
					record C(int j) implements I {} // Implicitly final
					public class X {
					 static int testSealedCoverage(I i) {
					   return switch (i) {
					   case A a -> 0;
					   case B b -> 1;
					   case C c -> 2; // No default required!
					   };
					 }
					 public static void main(String[] args) {
					   A a = new A();
					   System.out.println(testSealedCoverage(a));
					 }
					}""",
			},
			"0");
	}
	public void testBug575049_003() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					sealed interface I permits A,B,C {}
					final class A implements I {}
					final class B implements I {}
					record C(int j) implements I {} // Implicitly final
					public class X {
					 static int testSealedCoverage(I i) {
					   return switch (i) {
					   case A a -> 0;
					   case B b -> 1;
					   default -> 2; // No default required!
					   };
					 }
					 public static void main(String[] args) {
					   A a = new A();
					   System.out.println(testSealedCoverage(a));
					 }
					}""",
			},
			"0");
	}
	public void testBug575049_004() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					sealed interface I permits A,B,C {}
					final class A implements I {}
					final class B implements I {}
					record C(int j) implements I {} // Implicitly final
					public class X {
					 static int testSealedCoverage(I i) {
					   return switch (i) {
					   case A a -> 0;
					   case B b -> 1;
					   };
					 }
					 public static void main(String[] args) {
					   A a = new A();
					   System.out.println(testSealedCoverage(a));
					 }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 7)
					return switch (i) {
					               ^
				A switch expression should have a default case
				----------
				""");
	}
	public void testBug575048_01() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 static int foo(Integer i) {
					   return switch (i) {
					     default -> 2;
					     case Integer i1 -> 0;
					   };
					 }
					 public static void main(String[] args) {
					   System.out.println(foo(1));
					 }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					case Integer i1 -> 0;
					^^^^^^^^^^^^^^^
				Switch case cannot have both unconditional pattern and default label
				----------
				2. ERROR in X.java (at line 5)
					case Integer i1 -> 0;
					     ^^^^^^^^^^
				This case label is dominated by one of the preceding case labels
				----------
				""");
	}
	public void testBug575053_001() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public void foo(String o) {
							switch (o) {
							  case String s when s.length() > 0  -> {}
							  default -> {}
							}\s
						}
						public static void main(String[] args) {
							try{
							  (new X()).foo(null);
							} catch(Exception e) {
							 	System.out.println("Null Pointer Exception Thrown");
							}
						}
					}""",
			},
			"Null Pointer Exception Thrown");
	}
	public void testBug575053_002() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public void foo(Object o) {
							switch (o) {
							  case String s -> {}
							  default -> {}
							}\s
						}
						public static void main(String[] args) {
							try{
							  (new X()).foo(null);
							} catch(Exception t) {
							 	System.err.println("Null Pointer Exception Thrown");
							}
						}
					}""",
			},
			"",
			"Null Pointer Exception Thrown");
	}
	public void testBug575249_01() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static int foo(Object o) {
							return switch (o) {
							  case (String s) : yield 0;
							  default : yield 1;
							};
						}
						public static void main(String[] args) {
							System.out.println(foo("Hello"));
						}
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					case (String s) : yield 0;
					             ^
				Syntax error on token "s", delete this token
				----------
				2. ERROR in X.java (at line 4)
					case (String s) : yield 0;
					                  ^^^^^
				Syntax error on token "yield", AssignmentOperator expected after this token
				----------
				""");
	}
	public void testBug575249_02() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static int foo(Object o) {
							return switch (o) {
							  case String s when s.length() < 10 : yield 0;
							  default : yield 1;
							};
						}
						public static void main(String[] args) {
							System.out.println(foo("Hello"));
						}
					}""",
			},
			"0");
	}
	public void testBug575249_03() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static int foo(Object o) {
							return switch (o) {
							  case (String s) -> 0;
							  default -> 1;
							};
						}
						public static void main(String[] args) {
							System.out.println(foo("Hello"));
						}
					}""",
			},
			"""
			----------
			1. ERROR in X.java (at line 4)
				case (String s) -> 0;
				^^^^
			Syntax error on token "case", ( expected after this token
			----------
			2. ERROR in X.java (at line 4)
				case (String s) -> 0;
				                   ^
			Syntax error, insert ":" to complete SwitchLabel
			----------
			"""
				);
	}
	public void testBug575249_04() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static int foo(Object o) {
							return switch (o) {
							  case String s when s.length() < 10 -> 0;
							  default -> 1;
							};
						}
						public static void main(String[] args) {
							System.out.println(foo("Hello"));
						}
					}""",
			},
			"0");
	}
	public void testBug575241_01() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 static int foo(Integer i) {
					   return switch (i) {
					     case Integer i1 -> 0;
					   };
					 }
					 public static void main(String[] args) {
					   System.out.println(foo(1));
					 }
					}""",
			},
			"0");
	}
	public void testBug575241_02() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 static int foo(Integer i) {
					   return switch (i) {
					     case Object o -> 0;
					   };
					 }
					 public static void main(String[] args) {
					   System.out.println(foo(1));
					 }
					}""",
			},
			"0");
	}
	public void testBug575241_03() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 static int foo(Object myVar) {
					   return switch (myVar) {
					     case null  -> 0;
					     case Integer o -> 1;
					     case Object obj ->2;
					   };
					 }
					 public static void main(String[] args) {
					   System.out.println(foo(Integer.valueOf(0)));
					   System.out.println(foo(null));
					 }
					}""",
			},
			"1\n" +
			"0");
	}
	public void testBug575241_04() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 static int foo(Object myVar) {
					   return switch (myVar) {
					     case Integer o -> 1;
					     case Object obj ->2;
					   };
					 }
					 public static void main(String[] args) {
					   System.out.println(foo(Integer.valueOf(0)));
					   try {
					   foo(null);
					   } catch (NullPointerException e) {
					     System.out.println("NPE");
					   }
					 }
					}""",
			},
			"1\n" +
			"NPE");
	}
	public void testBug575241_05() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 static void foo(Integer myVar) {
					    switch (myVar) {
					     case  null  -> System.out.println(100);
					     case Integer o -> System.out.println(o);
					   };
					 }
					 public static void main(String[] args) {
					   foo(Integer.valueOf(0));
					   try {
					   foo(null);
					   } catch (NullPointerException e) {
					     System.out.println("NPE");
					   }
					 }
					}""",
			},
			"0\n" +
			"100");
	}
	public void testBug575241_06() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 static void foo(Integer myVar) {
					    switch (myVar) {
					     case Integer o -> System.out.println(o);
					   };
					 }
					 public static void main(String[] args) {
					   foo(Integer.valueOf(0));
					   try {
					   foo(null);
					   } catch (NullPointerException e) {
					     System.out.println("NPE");
					   }
					 }
					}""",
			},
			"0\n" +
			"NPE");
	}
	public void testBug575241_07() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 static void foo(String myVar) {
					    switch (myVar) {
					     case  null  -> System.out.println(100);
					     case String o -> System.out.println(o);
					   };
					 }
					 public static void main(String[] args) {
					   foo("Hello");
					   foo(null);
					 }
					}""",
			},
			"Hello\n" +
			"100");
	}
	public void testBug575241_08() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 static void foo(String myVar) {
					    switch (myVar) {
					     case String o -> System.out.println(o);
					   };
					 }
					 public static void main(String[] args) {
					   foo("Hello");
					   try {
					   foo(null);
					   } catch (NullPointerException e) {
					     System.out.println("NPE");
					   }
					 }
					}""",
			},
			"Hello\n" +
			"NPE");
	}
	public void testBug575356_01() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						 static void foo(Integer myVar) {
						    switch (myVar) {
						     case default -> System.out.println("hello");
						   };  \s
						 }  \s
						
						 public static  void main(String[] args) {
						   foo(Integer.valueOf(10));\s
						 }\s
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						case default -> System.out.println("hello");
						     ^^^^^^^
					A \'default\' can occur after \'case\' only as a second case label expression and that too only if \'null\' precedes  in \'case null, default\'\s
					----------
					""");
	}
	public void testBug575356_02() {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						 static void foo(Integer myVar) {
						    switch (myVar) {
						     case null, default -> System.out.println("hello");
						   };  \s
						 }  \s
						
						 public static  void main(String[] args) {
						   foo(Integer.valueOf(10));\s
						 }\s
						}""",
				},
				"hello");
	}
	public void testBug575356_03() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						 static void foo(Integer myVar) {
						    switch (myVar) {
						     case default, null -> System.out.println("hello");
						   };  \s
						 }  \s
						
						 public static  void main(String[] args) {
						   foo(Integer.valueOf(10));\s
						 }\s
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						case default, null -> System.out.println("hello");
						     ^^^^^^^
					A \'default\' can occur after \'case\' only as a second case label expression and that too only if \'null\' precedes  in \'case null, default\'\s
					----------
					2. ERROR in X.java (at line 4)
						case default, null -> System.out.println("hello");
						              ^^^^
					A null case label has to be either the only expression in a case label or the first expression followed only by a default
					----------
					""");
	}
	public void testBug575356_04() {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						private static void foo(Object o) {
						   switch (o) {
						    case Integer i ->
						      System.out.println("Integer:"+ i );
						    case null, default -> System.out.println(o.toString() );
						   }
						}
						
						 public static  void main(String[] args) {
						   foo(Integer.valueOf(10));\s
						   foo(new String("Hello"));\s
						 }\s
						}""",
				},
				"Integer:10\n" +
				"Hello");
	}
	public void testBug575052_001() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 static void foo(Object o) {
					   switch (o) {
					   case String s -> System.out.println(s);
					   default -> System.out.println(0);
					   };
					 }
					 public static void main(String[] args) {
					   foo("Hello");
					 }
					}""",
			},
			"Hello");
	}
	public void testBug575052_002() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 static void foo(Object o) {
					   switch (o) {
					   	case String s -> System.out.println(s);
					   };
					 }
					 public static void main(String[] args) {
					   foo("Hello");
					 }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					switch (o) {
					        ^
				An enhanced switch statement should be exhaustive; a default label expected
				----------
				""");
	}
	public void testBug575052_003() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 static int foo(Object o) {
					   switch (o) {
					   	case null -> System.out.println(0);
					   };
					 }
					 public static void main(String[] args) {
					   foo("Hello");
					 }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					switch (o) {
					        ^
				An enhanced switch statement should be exhaustive; a default label expected
				----------
				""");
	}
	public void testBug575052_004() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 private static int foo(int i) {
					   switch (i) {
					   case 1:
					     break;
					   }
					   return 0;
					 }
					 public static void main(String[] args) {
					   System.out.println(X.foo(0));
					 }
					}""",
			},
			"0");
	}
	public void testBug575050_001() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 static int foo(Object o) {
					   return switch (o) {
					   	case String s -> 0;
					   };
					 }
					 public static void main(String[] args) {
					   foo("Hello");
					 }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					return switch (o) {
					               ^
				A switch expression should have a default case
				----------
				""");
	}
	public void testBug575050_002() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 static int  foo(Object o) {
					   return switch (o) {
					   	case null -> 0;
					   };
					 }
					 public static void main(String[] args) {
					   foo("Hello");
					 }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					return switch (o) {
					               ^
				A switch expression should have a default case
				----------
				""");
	}
	// From 14.11.1.2 - null to be handled separately - no dominance here
	public void testBug575047_01() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						 static int foo(Integer i) {
						   return switch (i) {
						     case Integer i1 -> 0;
						     case null -> 2;
						   };
						   Zork();
						 }
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 7)
						Zork();
						^^^^
					The method Zork() is undefined for the type X
					----------
					""");
	}
	// A switch label that has a pattern case label element p dominates another
	// switch label that has a constant case label element c if either of the
	// following is true:
	//   * the type of c is a primitive type and its wrapper class (5.1.7) is a subtype of the erasure of the type of p.
    //   * the type of c is a reference type and is a subtype of the erasure of the type of p.
	public void testBug575047_02() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						 static int foo(Integer i) {
						   return switch (i) {
						     case Integer i1 -> i1;
						     case 0 -> 0;
						   };
						 }
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 5)
						case 0 -> 0;
						     ^
					This case label is dominated by one of the preceding case labels
					----------
					""");
	}
	public void testBug575047_03() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						 static void foo(Color c) {
						   switch (c) {
									case Color c1 :\s
										break;
									case Blue :
										break;
								}
						 }
						enum Color { Blue, Red; }
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 6)
						case Blue :
						     ^^^^
					This case label is dominated by one of the preceding case labels
					----------
					""");
	}
	public void testBug575047_04() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						 static int foo(Integer i) {
						   return switch (i) {
						     case null -> 2;
						     case Integer i1 -> 0;
						   };
						 }
						 public static void main(String[] args) {
						   System.out.println(foo(null));
						   System.out.println(foo(Integer.valueOf(0)));
						 }
						}""",
				},
				"2\n" +
				"0");
	}
	public void testBug575047_05() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						 static void foo(float c) {
						   switch (c) {
									case 0 :\s
										break;
									default :
								}
						 }
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						case 0 :\s
						     ^
					Type mismatch: cannot convert from int to float
					----------
					""");
	}
	public void testBug575047_06() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						 static int foo(String o) {
						    return switch (o) {
								     case String s when s.length() > 0 -> 3;
								     case String s1 -> 1;
								     case String s -> -1;
								   };
						 }
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 6)
						case String s -> -1;
						     ^^^^^^^^
					The switch statement cannot have more than one unconditional pattern
					----------
					2. ERROR in X.java (at line 6)
						case String s -> -1;
						     ^^^^^^^^
					This case label is dominated by one of the preceding case labels
					----------
					""");
	}
	// Test that when a literal is used as case constant
	// we report type mismatch error against the literal's type and
	// not on other types the case statement may have resolved too
	public void testBug575047_07() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						 static void foo(Number i) {
							    switch (i) {
								 case Integer j, "":
									 System.out.println(0);
								 default:
							   }
							}
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						case Integer j, "":
						                ^^
					Cannot mix pattern with other case labels
					----------
					2. ERROR in X.java (at line 4)
						case Integer j, "":
						                ^^
					Type mismatch: cannot convert from String to Number
					----------
					""");
	}
	public void testBug575047_08() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						 static int foo(Integer i) {
						   return switch (i) {
						     case 0 -> 0;
						     case Integer i1 -> i1;
						   };
						 }
						 public static void main(String[] args) {
						   System.out.println(foo(3));
						   System.out.println(foo(0));
						 }
						}""",
				},
				"3\n"+
				"0");
	}
	public void testBug575047_09() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						 	static int foo(String i) {
								return switch (i) {
							     case "" -> 0;
							     case String s -> -1;
							   };
							}
						 public static void main(String[] args) {
						   System.out.println(foo(""));
						   System.out.println(foo("abc"));
						 }
						}""",
				},
				"0\n" +
				"-1");
	}
	public void testBug575047_10() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
							static String foo(Object o) {
							   return switch (o) {
								 case String i when i.length() == 0 -> "empty";
							     case String i when i.length() > 0 -> "zero+";
							     case Color s -> s.toString();
								 default -> "unknown";
							   };
							}
							public static void main(String[] args) {
								System.out.println(foo("abc"));
								System.out.println(foo(""));
								System.out.println(Color.Blue);
								System.out.println(foo(args));
							}
						}\s
						enum Color {
							Blue, Red;\s
						}""",
				},
				"""
					zero+
					empty
					Blue
					unknown""");
	}
	// Positive - Mix enum constants as well as suitable pattern var
	public void testBug575047_11() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
							static String foo(Color o) {
								return switch (o) {
							     case Red -> "Const:Red";
							     case Color s -> s.toString();
							   };
							}
							public static void main(String[] args) {
								System.out.println(foo(Color.Red));
								System.out.println(foo(Color.Blue));
							}
						}\s
						enum Color {
							Blue, Red;\s
						}""",
				},
				"Const:Red\n" +
				"Blue");
	}
	public void testBug575047_12() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
							static String foo(Color o) {
								return switch (o) {
							     case Red -> "Red";
							     case Color s when s == Color.Blue  -> s.toString();
							     case Color s -> s.toString();
							   };
							}
							public static void main(String[] args) {
								System.out.println(Color.Red);
								System.out.println(Color.Blue);
							}
						}\s
						enum Color {
							Blue, Red;\s
						}""",
				},
				"Red\n" +
				"Blue");
	}
	public void testBug575047_13() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
							static String foo(Color o) {
								return switch (o) {
							     case Color s when s == Color.Blue  -> s.toString();
							     case Red -> "Red";
							     case null -> "";
							   };
							}
						}\s
						enum Color {
							Blue, Red;\s
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 3)
						return switch (o) {
						               ^
					A Switch expression should cover all possible values
					----------
					""");
	}
	public void testBug575047_14() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
							static String foo(Color o) {
								return switch (o) {
							     case Color s when s == Color.Blue  -> s.toString();
							     case Red -> "Red";
							   };
							}
						}\s
						enum Color {
							Blue, Red;\s
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 3)
						return switch (o) {
						               ^
					A Switch expression should cover all possible values
					----------
					""");
	}
	public void testBug575047_15() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
							static void foo(Integer o) {
								switch (o) {
								  case 1: break;
								  case Integer s when s == 2:
									  System.out.println(s);break;
								  case null, default:
									  System.out.println("null/default");
								}
							}
							public static  void main(String[] args) {
								foo(null);
							}
						}""",
				},
				"null/default");
	}
	public void testBug575360_001() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						 static void foo(String myVar) { // String
						    switch (myVar) {
						     case null, default : System.out.println("hello");
						   };  \s
						 }
						 public static  void main(String[] args) {\s
						   foo(new String("Hello"));\s
						 }
						}""",
				},
				"hello");
	}
	public void testBug575055_001() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
							public int foo(CharSequence c) {
								return switch (c) {
								   case CharSequence c1 when (c instanceof String c1 && c1.length() > 0) -> 0;
								   default -> 0;
								};
							}\
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						case CharSequence c1 when (c instanceof String c1 && c1.length() > 0) -> 0;
						                                               ^^
					A pattern variable with the same name is already defined in the statement
					----------
					""");
	}
	// Fails with Javac as it prints Javac instead of throwing NPE
	// https://bugs.openjdk.java.net/browse/JDK-8272776
	public void testBug575051_1() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
							public void foo(Object o) {
							  try{
								switch (o) {
								  default:
									  break;
								  case String s :
									  System.out.println(s);
								}\s
							  } catch(Exception t) {
								 t.printStackTrace(System.out);
							  }
							}
							public static void main(String[] args) {
								  (new X()).foo(null);
							}
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 7)
						case String s :
						     ^^^^^^^^
					This case label is dominated by one of the preceding case labels
					----------
					""");
	}
	// Test we don't report any illegal fall-through to null case
	public void testBug575051_2() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
							public void foo(Object o) {
								switch (o) {
								  case String s :
									  System.out.println(s);
										//$FALL-THROUGH$
								  case null:
									  break;
								  default :\s
										  break;
								}
							}
							public static void main(String[] args) {
								(new X()).foo(null);
							}
						}""",
				},
				"");
	}
	public void testBug575571_1() {
		Map<String, String> options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportMissingDefaultCase, CompilerOptions.WARNING);
		runWarningTest(
		new String[] {
		"X.java",
		"""
			public class X {
			       public void foo(Color o) {
			               switch (o) {
			                 case Blue:
			                       break;
			               }
			       }
			       public static void main(String[] args) {}
			}
			enum Color {   Blue;  }
			""",
		},
		"""
			----------
			1. WARNING in X.java (at line 3)
				switch (o) {
				        ^
			The switch over the enum type Color should have a default case
			----------
			""",
		options);
	}
	public void testBug575571_2() {
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public void foo(Color o) {
									switch (o) {
									  case Blue:
									  case Color c:
										break;
									}
								}
								public static void main(String[] args) {}
							}
							enum Color {	Blue, Red;  }
							""",
				},
				"""
					----------
					1. ERROR in X.java (at line 5)
						case Color c:
						^^^^^^^^^^^^
					Illegal fall-through to a pattern
					----------
					""");
	}
	public void testBug575714_01() {
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							class X {
							 static Object foo(Object o) {
							   switch (o) {
							       case Object __ -> throw new AssertionError();\s
							   }
							 }
							 public static void main(String[] args) {
							   Zork();
							 }
							}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 8)
						Zork();
						^^^^
					The method Zork() is undefined for the type X
					----------
					""");
	}
	public void testBug575714_02() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						class X {
						 static Object foo(Object o) {
						   switch (o) {
						       case Object __ -> System.out.println("Hello");\s
						   }
						 }
						 public static void main(String[] args) {
						   X.foo(new X());
						 }
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 2)
						static Object foo(Object o) {
						              ^^^^^^^^^^^^^
					This method must return a result of type Object
					----------
					""");
	}
	public void testBug575714_03() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						class X {
						 static Object foo(Object o) {
						   switch (o) {
						       case Object __ -> System.out.println("Hello");\s
						   }
						   return null;
						 }
						 public static void main(String[] args) {
						   X.foo(new X());
						 }
						}""",
				},
				"Hello");
	}
	public void testBug575714_04() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						class X {
						 static Object foo(Object o) throws Exception {
						   switch (o) {
						       case Object __ -> throw new Exception();\s
						   }
						 }
						 public static void main(String[] args) {
						   try {
						     X.foo(new X());
						   } catch (Exception e) {
						     System.out.println("Hello");
						   }
						 }
						}""",
				},
				"Hello");
	}
	public void testBug575687_1() {
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
								static void number(Number i) {
									switch (i) {
										case Integer i2, 4.5:
										case 4.3: System.out.println();
										default: System.out.println("nothing");
									}
								}
								public static void main(String[] args) {}
							}
							enum Color {	Blue, Red;  }
							""",
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						case Integer i2, 4.5:
						                 ^^^
					Cannot mix pattern with other case labels
					----------
					2. ERROR in X.java (at line 4)
						case Integer i2, 4.5:
						                 ^^^
					Type mismatch: cannot convert from double to Number
					----------
					3. ERROR in X.java (at line 5)
						case 4.3: System.out.println();
						     ^^^
					Type mismatch: cannot convert from double to Number
					----------
					""");
	}
	public void testBug575686_1() {
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
								void m(Object o) {
									switch (o) {
										case Integer i1, String s1 ->
											System.out.print(s1);
										default -> System.out.print("default");
										case Number n, null ->
											System.out.print(o);
										case null, Class c ->
											System.out.print(o);
									}
								}
								public static void main(String[] args) {}
							}
							enum Color {	Blue, Red;  }
							""",
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						case Integer i1, String s1 ->
						             ^^
					Named pattern variables are not allowed here
					----------
					2. ERROR in X.java (at line 4)
						case Integer i1, String s1 ->
						                 ^^^^^^^^^
					Cannot mix pattern with other case labels
					----------
					3. ERROR in X.java (at line 4)
						case Integer i1, String s1 ->
						                        ^^
					Named pattern variables are not allowed here
					----------
					4. ERROR in X.java (at line 5)
						System.out.print(s1);
						                 ^^
					s1 cannot be resolved to a variable
					----------
					5. ERROR in X.java (at line 7)
						case Number n, null ->
						     ^^^^^^^^
					This case label is dominated by one of the preceding case labels
					----------
					6. ERROR in X.java (at line 7)
						case Number n, null ->
						               ^^^^
					Cannot mix pattern with other case labels
					----------
					7. ERROR in X.java (at line 7)
						case Number n, null ->
						               ^^^^
					A null case label has to be either the only expression in a case label or the first expression followed only by a default
					----------
					8. ERROR in X.java (at line 7)
						case Number n, null ->
						               ^^^^
					Duplicate case
					----------
					9. ERROR in X.java (at line 9)
						case null, Class c ->
						     ^^^^
					Duplicate case
					----------
					10. ERROR in X.java (at line 9)
						case null, Class c ->
						           ^^^^^^^
					Cannot mix pattern with other case labels
					----------
					11. WARNING in X.java (at line 9)
						case null, Class c ->
						           ^^^^^
					Class is a raw type. References to generic type Class<T> should be parameterized
					----------
					12. ERROR in X.java (at line 9)
						case null, Class c ->
						           ^^^^^^^
					This case label is dominated by one of the preceding case labels
					----------
					""");
	}
	public void testBug575738_001() {
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
								private static void foo(Object o) {
								   switch (o.hashCode()) {
								     case int t: System.out.println("Integer");\s
								     default : System.out.println("Object");\s
								   }
								}
								public static void main(String[] args) {\s
									foo("Hello World");
								}
							}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						case int t: System.out.println("Integer");\s
						     ^^^^^
					Unexpected type int, expected class or array type
					----------
					""");
	}
	public void testBug575738_002() {
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
								private static void foo(Object o) {
								   switch (o.hashCode()) {
								     case Integer t: System.out.println("Integer");\s
								     default : System.out.println("Object");\s
								   }
								}
								public static void main(String[] args) {\s
									foo("Hello World");
								}
							}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						case Integer t: System.out.println("Integer");\s
						     ^^^^^^^^^
					Type mismatch: cannot convert from int to Integer
					----------
					""");
	}

	public void testBug576075_001() throws Exception {
		runConformTest(
			new String[] {
				"p/Rec.java",
				"""
					package p;
					import p.Rec.MyInterface.MyClass1;
					import p.Rec.MyInterface.MyClass2;
					public record Rec(MyInterface c) {
						public static sealed interface MyInterface permits MyClass1, MyClass2 {
							public static final class MyClass1 implements MyInterface { }
					        public static final class MyClass2 implements MyInterface { }
					    }
					    public boolean bla() {
					        return switch (c) {
					            case MyClass1 mc1 -> true;
					            case MyClass2 mc2 -> false;
					        };
					    }
					    public static void main(String[] args) {
					        new Rec(new MyClass1()).hashCode();
					        System.out.println("works");
					    }
					}
					"""
			},
		 "works");
		String expectedOutput =
				"""
			Bootstrap methods:
			  0 : # 95 invokestatic java/lang/runtime/SwitchBootstraps.typeSwitch:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;
				Method arguments:
					#32 p/Rec$MyInterface$MyClass1
					#34 p/Rec$MyInterface$MyClass2,
			  1 : # 102 invokestatic java/lang/runtime/ObjectMethods.bootstrap:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/TypeDescriptor;Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/invoke/MethodHandle;)Ljava/lang/Object;
				Method arguments:
					#1 p/Rec
					#103 c
					#104 REF_getField c:Lp/Rec$MyInterface;""";
		SwitchPatternTest.verifyClassFile(expectedOutput, "p/Rec.class", ClassFileBytesDisassembler.SYSTEM);
	}
	public void testBug576785_001() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					sealed interface J<X> permits D, E {}
					final class D implements J<String> {}
					final class E<X> implements J<X> {}
					
					public class X {
					       static int testExhaustive2(J<Integer> ji) {
					               return switch (ji) { // Exhaustive!
					               case E<Integer> e -> 42;
					               };
					       }
					       public static void main(String[] args) {
					               J<Integer> ji = new E<>();
					               System.out.println(X.testExhaustive2(ji));
					       }
					}""",
			},
			"42");
	}
	public void testBug576785_002() {
		runNegativeTest(
				new String[] {
				"X.java",
				"""
					@SuppressWarnings("rawtypes")
					sealed interface J<T> permits D, E, F {}
					final class D implements J<String> {}
					final class E<T> implements J<T> {}
					final class F<T> implements J<T> {}
					
					public class X {
					 static int testExhaustive2(J<Integer> ji) {
					   return switch (ji) { // Exhaustive!
					   case E<Integer> e -> 42;
					   };
					 }
					 public static void main(String[] args) {
					   J<Integer> ji = new E<>();
					   System.out.println(X.testExhaustive2(ji));
					   Zork();
					 }
					}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 9)
						return switch (ji) { // Exhaustive!
						               ^^
					A switch expression should have a default case
					----------
					2. ERROR in X.java (at line 16)
						Zork();
						^^^^
					The method Zork() is undefined for the type X
					----------
					""");
	}
	public void testBug576830_001() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 static void foo(Object o) {
					   switch (o) {
					   };
					 }
					 public static void main(String[] args) {
					   foo("Hello");
					 }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					switch (o) {
					        ^
				An enhanced switch statement should be exhaustive; a default label expected
				----------
				""");
	}
	public void testBug578107_001() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					sealed class C permits D {}
					final class D extends C {}
					public class X {
					       static  void foo(C ji) {
					                switch (ji) { // non-exhaustive
					                  case D d : System.out.println("D"); break;
					               };\s
					       }\s
					       public static void main(String[] args) {
					               X.foo(new D());
					               Zork();
					       }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					switch (ji) { // non-exhaustive
					        ^^
				An enhanced switch statement should be exhaustive; a default label expected
				----------
				2. ERROR in X.java (at line 11)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""");
	}
	public void testBug578107_002() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					abstract sealed class C permits D {}
					final class D extends C {}
					public class X {
					       static  void foo(C ji) {
					                switch (ji) { // non-exhaustive
					                  case D d : System.out.println("D"); break;
					               };\s
					       }\s
					       public static void main(String[] args) {
					               X.foo(new D());
					               Zork();
					       }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 11)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""");
	}
	public void testBug578107_003() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					sealed interface C permits D {}
					final class D implements C {}
					public class X {
					       static  void foo(C ji) {
					                switch (ji) { // non-exhaustive
					                  case D d : System.out.println("D"); break;
					               };\s
					       }\s
					       public static void main(String[] args) {
					               X.foo(new D());
					               Zork();
					       }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 11)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""");
	}
	public void testBug578108_001() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					sealed abstract class C permits D {}
					final class D extends C {}
					public class X {
					 static <T extends C> void foo(T  ji) {
					    switch (ji) { // exhaustive because C is sealed and abstract
					      case D d : System.out.println("D"); break;
					   };\s
					 }\s
					 public static void main(String[] args) {
					   X.foo(new D());
					   Zork();
					 }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 11)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""");
	}
	public void testBug578108_002() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					sealed interface C permits D {}
					final class D implements C {}
					public class X {
					 static <T extends C> void foo(T  ji) {
					    switch (ji) { // exhaustive because C is sealed and abstract
					      case D d : System.out.println("D"); break;
					   };\s
					 }\s
					 public static void main(String[] args) {
					   X.foo(new D());
					   Zork();
					 }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 11)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""");
	}
	public void testBug578143_001() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 static  int foo(Object o) {
					   return switch (o) {\s
					      case X x when true -> 0;
					      default -> 1;
					   };\s
					 }\s
					 public static void main(String[] args) {
					   System.out.println(X.foo(new X()));
					   System.out.println(X.foo(new Object()));
					 }
					}""",
			},
			"0\n" +
			"1");
	}
	public void testBug578143_002() {
		runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 public static void main(String[] args) {
					     Boolean input = false;
					     int result = switch(input) {
					       case Boolean p when true -> 1;
					     };
					     System.out.println(result);
					 }
					}""",
			},
			"1");
	}
	public void testBug578402() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X  {
						enum Color{BLUE, RED, YELLOW;}
						    public static void run(Color c) {
						        switch(c) {
						                case BLUE -> {
						                    System.out.println("BLUE");
						                }
						                case RED -> {
						                    System.out.println("RED");
						                }
						                case Object o -> {
						                    System.out.println(o.toString());
						                }
						            }
						    }\
							public static void main(String[] args) {
								run(Color.RED);
								run(Color.BLUE);
							}
						}"""
				},
				"RED\n" +
				"BLUE");
	}
	public void testBug578402_2() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X  {
						static final String CONST = "abc";
						    public static void main(String args[]) {
						        System.out.println(run());
						    }
						    public static int run() {
						        String s = "abc";
						        int a = -1;
						        switch (s) {
						            case CONST -> {
						                a = 2;
						                break;
						            }
						            case null -> {
						                a = 0;
						                break;\s
						            }
						            default -> {
						            	a = 1;
						            }
						        }
						        return a;
						    }
						}""",
				},
				"2");
	}
	// to be enabled after bug 578417 is fixed.
	public void testBug578402_3() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X  {
						static final String CONST = "abc";
						    public static void main(String args[]) {
						        System.out.println(run());
						    }
						    public static int run() {
						        String s = "abc";
						        int a = -1;
						        switch (s) {
						            case CONST -> {
						                a = 2;
						                break;
						            }
						            case String s1 -> {
						                a = 0;
						                break;\s
						            }
						        }
						        return a;
						    }
						}""",
				},
				"2");
	}
	public void testBug578241_1() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    public static void foo(Object obj, int x) {
						    	switch (obj) {
						    		case String s when (switch (x) {
											case 1 -> { yield true; }
											default -> { yield false; }
						   	 									})\t
						   	 		 			-> {
						   	 		 				System.out.println("true");
						   	 		 			}
										\t
						   	 		 default -> {
						   	 			System.out.println("false");
						   	 		 }
						    	}\t
						    }
						    public static void main(String[] args) {
								foo("abc", 1);
							}
						}""",
				},
				"true");
	}
	private String getTestCaseForBug578504 (String caseConstant) {
		return "public class X {\n"
				+ "    public Object literal = \"a\";\n"
				+ "	public boolean foo() {\n"
				+ "        String s = switch(literal) {\n"
				+ "            " + caseConstant
				+ "                yield \"a\";\n"
				+ "            }\n"
				+ "            default -> { \n"
				+ "                yield \"b\";\n"
				+ "            }\n"
				+ "        }; \n"
				+ "        return s.equals(\"a\");\n"
				+ "    }\n"
				+ "    public static void main(String[] argv) {\n"
				+ "    	X c = new X();\n"
				+ "    	System.out.println(c.foo());\n"
				+ "    }\n"
				+ "}";
	}
	public void testBug578504_1() {
		runConformTest(
				new String[] {
					"X.java",
					getTestCaseForBug578504("case String a when (a.equals(\"a\") && a != null)  -> { \n")
					,
				},
				"true");
	}
	public void testBug578504_2() {
		runConformTest(
				new String[] {
					"X.java",
					getTestCaseForBug578504("case CharSequence a when (a instanceof String ss && (ss == null && ss != null))  -> {\n"),
				},
				"false");
	}
	public void testBug578504_3() {
		runConformTest(
				new String[] {
					"X.java",
					getTestCaseForBug578504("case CharSequence a when (a instanceof String ss && ss != null) && ss != null  -> {\n"),
				},
				"true");
	}
	public void testBug578504_6() {
		runConformTest(
				new String[] {
					"X.java",
					getTestCaseForBug578504("case CharSequence a when (a instanceof String ss && a instanceof String sss) && ss == sss  -> {\n"),
				},
				"true");
	}
	public void testBug578504_7() {
		runConformTest(
				new String[] {
					"X.java",
					getTestCaseForBug578504("case CharSequence a when (a instanceof String ss && a instanceof String sss) && ss != sss  -> {\n"),
				},
				"false");
	}
	public void testBug578553_1() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
							static Long foo(Number n) {
								return switch (n) {
							     case (Long l) when l.toString().equals("0") -> {
							    	 yield ++l;
							     }
								default -> throw new IllegalArgumentException();
							   };
							}
							public static void main(String[] args) {
								System.out.println(foo(0L));
							}
						}""",
				},
				"""
				----------
				1. ERROR in X.java (at line 4)
					case (Long l) when l.toString().equals("0") -> {
					           ^
				Syntax error on token "l", delete this token
				----------
				2. ERROR in X.java (at line 4)
					case (Long l) when l.toString().equals("0") -> {
					              ^^^^
				Syntax error, insert ":: IdentifierOrNew" to complete ReferenceExpression
				----------
				3. ERROR in X.java (at line 4)
					case (Long l) when l.toString().equals("0") -> {
					              ^^^^
				Syntax error, insert ":" to complete SwitchLabel
				----------
				4. ERROR in X.java (at line 4)
					case (Long l) when l.toString().equals("0") -> {
					                                            ^^
				Syntax error on token "->", ; expected
				----------
				""");
	}
	public void testBug578553_2() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
							static Long foo(Number n) { \s
								return switch (n) {\s
							     case (Long l) when l.toString().equals("0") -> {
							    	 yield switch(l) {
							    	 case Long l1 when l1.toString().equals(l1.toString()) -> {
							    	 	yield ++l + ++l1;
							    	 }
									default -> throw new IllegalArgumentException("Unexpected value: " + l);
							    	 };
							     }
								default -> throw new IllegalArgumentException();
							   };
							}
							public static void main(String[] args) {
								System.out.println(foo(0L));
							}
						}""",
				},
				"""
				----------
				1. ERROR in X.java (at line 4)
					case (Long l) when l.toString().equals("0") -> {
					           ^
				Syntax error on token "l", delete this token
				----------
				2. ERROR in X.java (at line 4)
					case (Long l) when l.toString().equals("0") -> {
					              ^^^^
				Syntax error, insert ":: IdentifierOrNew" to complete ReferenceExpression
				----------
				3. ERROR in X.java (at line 4)
					case (Long l) when l.toString().equals("0") -> {
					              ^^^^
				Syntax error, insert ":" to complete SwitchLabel
				----------
				4. ERROR in X.java (at line 4)
					case (Long l) when l.toString().equals("0") -> {
					                                            ^^
				Syntax error on token "->", ; expected
				----------
				5. ERROR in X.java (at line 6)
					case Long l1 when l1.toString().equals(l1.toString()) -> {
					             ^^^^
				Syntax error on token "when", , expected
				----------
				""");
	}
	public void testBug578553_3() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
							static Long foo(Number n) { \s
								return switch (n) {\s
							     case (Long l) when l.toString().equals("0") -> {
							    	 yield switch(l) {
							    	 case Long l1 when l.toString().equals(l1.toString()) -> {
							    	 	yield ++l + ++l1;
							    	 }
									default -> throw new IllegalArgumentException("Unexpected value: " + l);
							    	 };
							     }
								default -> throw new IllegalArgumentException();
							   };
							}
						}""",
				},
				"""
				----------
				1. ERROR in X.java (at line 4)
					case (Long l) when l.toString().equals("0") -> {
					           ^
				Syntax error on token "l", delete this token
				----------
				2. ERROR in X.java (at line 4)
					case (Long l) when l.toString().equals("0") -> {
					              ^^^^
				Syntax error, insert ":: IdentifierOrNew" to complete ReferenceExpression
				----------
				3. ERROR in X.java (at line 4)
					case (Long l) when l.toString().equals("0") -> {
					              ^^^^
				Syntax error, insert ":" to complete SwitchLabel
				----------
				4. ERROR in X.java (at line 4)
					case (Long l) when l.toString().equals("0") -> {
					                                            ^^
				Syntax error on token "->", ; expected
				----------
				5. ERROR in X.java (at line 6)
					case Long l1 when l.toString().equals(l1.toString()) -> {
					             ^^^^
				Syntax error on token "when", , expected
				----------
				""");
	}
	public void testBug578553_4() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
							static Long foo(Number n) { \s
							int i = 0;
							return switch(n) {
							  case Long l when (1 == switch(l) {
								//case\s
									default -> { \s
										yield (i++);
									}\s
								}) -> 1L;\s
							  default -> throw new IllegalArgumentException("Unexpected value: " + n);
							  };
							}
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 8)
						yield (i++);
						       ^
					Local variable i referenced from a guard must be final or effectively final
					----------
					""");
	}
	public void testBug578553_5() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
							static Long foo(Number n) { \s
							int i = 0;
							return switch(n) {
							  case Long l when (1 == switch(l) {
								//case\s
									default -> { \s
										yield ++i;
									}\s
								}) -> 1L;\s
							  default -> throw new IllegalArgumentException("Unexpected value: " + n);
							  };
							}
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 8)
						yield ++i;
						        ^
					Local variable i referenced from a guard must be final or effectively final
					----------
					""");
	}
	public void testBug578553_6() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
							static Long foo(Number n) { \s
							int i = 0;
							return switch(n) {
							  case Long l when (1 == switch(l) {
								//case\s
									default -> { \s
										yield (i=i+1);
									}\s
								}) -> 1L;\s
							  default -> throw new IllegalArgumentException("Unexpected value: " + n);
							  };
							}
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 8)
						yield (i=i+1);
						       ^
					Local variable i referenced from a guard must be final or effectively final
					----------
					""");
	}
	public void testBug578553_7() {
		runNegativeTest(
				false /*skipJavac */,
				JavacTestOptions.JavacHasABug.JavacBug8299416,
				new String[] {
					"X.java",
					"""
						public class X {
						 static int bar() { return 1; }
							static Long foo(Number n) { \s
							int i = 0;
							return switch(n) {
							  case Long l when (1 == switch(l) {
								//case\s
									default -> { \s
										yield (i = bar());
									}\s
								}) -> 1L;\s
							  default -> throw new IllegalArgumentException("Unexpected value: " + n);
							  };
							}
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 9)
						yield (i = bar());
						       ^
					Local variable i referenced from a guard must be final or effectively final
					----------
					""");
	}
	public void testBug578568_1() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public static int foo(Number arg0) {
							        int result = 0;
							        result =\s
							         switch (arg0) {
							            case Object p -> {
							                switch (arg0) {
							                     case Number p1 -> {
							                        yield 1;
							                    }
							                }
							            }
							        };\s
							        return result;
							    }
							 public static void main(String[] args) {
							    	System.out.println(foo(0L));
								}\
							}""",
				},
				"1");
	}
	public void testBug578568_2() {
		runConformTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public static int foo(Number arg0) {
							        return switch (arg0) {
							            case Object p : {
							                switch (arg0) {
							                     case Number p1 : {
							                        yield 1;
							                    }
							                }
							            }
							        };\s
							    }
							 public static void main(String[] args) {
							    	System.out.println(foo(0L));
								}\
							}""",
				},
				"1");
	}
	public void testBug578568_3() {
		runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public static int foo(Object arg0) {
							        return switch (arg0) {
							            case Object p : {
							                switch (arg0) {
							                    case Number p1 : {
							                        yield 1;
							                    }
							                    default: {
							                    }\
							                }
							            }
							        };\s
							 }
							}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 11)
						}
						^^
					A switch labeled block in a switch expression should not complete normally
					----------
					""");
	}
	public void testBug578416() {
		runConformTest(new String[] {
				"X.java",
				"""
					public class X {
					    public static int testMethod(I i) {
					       return switch (i) {
					            case I p1 when (p1 instanceof C p2) : {
					                yield p2.value(); // Error here
					            }
					            case I p3 : {
					                yield p3.value(); // No error here
					            }
					        };
					    }
					    interface I {
					        public int value();
					    }
					    class C implements I {
					    	@Override
					    	public int value() {
					    		return 0;
					    	}
					    }
					    public static void main(String[] args) {
					    	I i = new I() {
					    		public int value() {
					    			return 10;
					    		}\s
					    	};\s
					    	System.out.println(testMethod(i));
					    	System.out.println(testMethod(new X().new C()));
						}\
					}
					"""},
				"10\n" +
				"0");
	}
	public void testBug578416_1() {
		runConformTest(new String[] {
				"X.java",
				"""
					public class X {
					    public static int testMethod(I i) {
					       return switch (i) {
					            case I p1 when (p1 instanceof C p2) : {
					                yield p2.value();
					            }
					            case I p3 : {
					                yield p3.value();
					            }
					        };
					    }
					    interface I {
					        public int value();
					    }
					    class C implements I {
					    	@Override
					    	public int value() {
					    		return 0;
					    	}
					    }
					    public static void main(String[] args) {
					    	I i = new I() {
					    		public int value() {
					    			return 10;
					    		}\s
					    	};\s
					    	System.out.println(testMethod(i));
					    	System.out.println(testMethod(new X().new C()));
						}\
					}
					"""},
				"10\n" +
				"0");
	}
	public void testBug578416_2() {
		runConformTest(new String[] {
				"X.java",
				"""
					public class X {
					    public static int foo(Object o) {
					       return switch (o) {
					            case Number n when (n instanceof Integer i) : {
					                yield n.intValue() + i; // Error here
					            }
					            case Number n2 : {
					                yield n2.intValue();
					            }
					            default : {
					                yield -1;
					            }
					        };
					    }
					    public static void main(String[] args) {
					    	System.out.println(foo(new Integer(10)));
					    	System.out.println(foo(new Integer(5)));
					    	System.out.println(foo(new Long(5L)));
					    	System.out.println(foo(new Float(0)));
						}\
					}
					"""},
				"""
					20
					10
					5
					0""");
	}
	public void testBug578416_3() {
		runConformTest(new String[] {
				"X.java",
				"""
					public class X {
					    public static int foo(Object o) {
					       return switch (o) {
					            case Number n when (n instanceof Integer i && i.equals(10)) : {
					                yield n.intValue() + i; // Error here
					            }
					            case Number n2 : {
					                yield n2.intValue();
					            }
					            default : {
					                yield -1;
					            }
					        };
					    }
					    public static void main(String[] args) {
					    	System.out.println(foo(new Integer(10)));
					    	System.out.println(foo(new Integer(5)));
					    	System.out.println(foo(new Long(5L)));
					    	System.out.println(foo(new Float(0)));
						}\
					}
					"""},
				"""
					20
					5
					5
					0""");
	}
	public void testBug578635_1() {
		runConformTest(new String[] {
				"X.java",
				"""
					public class X {
						public static boolean foo(Integer n) {
					    	return switch (n) {
						    	case Integer x when x.equals(10) -> {
						    		yield true;
						    	}
						    	case Comparable y -> {
						    		yield false;
						    	}
					    	};
					    }
					    public static void main(String[] argv) {
					    	System.out.println(foo(Integer.valueOf(0)));
					    	System.out.println(foo(Integer.valueOf(10)));
					    }
					}"""},
				"false\n" +
				"true");
	}
	public void testBug578635_2() {
		runNegativeTest(new String[] {
				"X.java",
				"""
					public class X {
					   @SuppressWarnings({ "rawtypes" })
						public static boolean foo(Integer n) {
					    	return switch (n) {
						    	case Integer x when x.equals(10) -> {
						    		yield true;
						    	}
						    	case Comparable y -> {
						    		yield false;
						    	}
						    	default -> {
						    		yield false;
						    	}
					    	};
					    }
					    public static void main(String[] argv) {
					    	System.out.println(foo(Integer.valueOf(0)));
					    	System.out.println(foo(Integer.valueOf(10)));
					    }
					}"""},
				"""
					----------
					1. ERROR in X.java (at line 11)
						default -> {
						^^^^^^^
					Switch case cannot have both unconditional pattern and default label
					----------
					""");
	}
	public void testBug578635_3() {
		runNegativeTest(new String[] {
				"X.java",
				"""
					public class X {
					   @SuppressWarnings({ "rawtypes" })
						public static boolean foo(Integer n) {
					    	return switch (n) {
						    	case Integer x when x.equals(10) -> {
						    		yield true;
						    	}
						    	case Comparable y -> {
						    		yield false;
						    	}
						    	default -> {
						    		yield false;
						    	}
					    	};
					    }
					    public static void main(String[] argv) {
					    	System.out.println(foo(Integer.valueOf(0)));
					    	System.out.println(foo(Integer.valueOf(10)));
					    }
					}"""},
				"""
					----------
					1. ERROR in X.java (at line 11)
						default -> {
						^^^^^^^
					Switch case cannot have both unconditional pattern and default label
					----------
					""");
	}
	public void testBug578417_1() {
		runConformTest(new String[] {
				"X.java",
				"""
					public class X {
					    static final String CONSTANT = "abc";
					    static String CON2 = "abc";
					    public static int foo() {
					        int res = 0;
					        switch (CON2) {
					            case CONSTANT -> {
					                res = 1;
					                break;
					            }
					            case String s -> {
					                res = 2;
					                break;
					            }
					        }
					        return res;
					    }
					    public static void main(String argv[]) {
					    	System.out.println(foo());\s
					    }
					}"""},
				"1" );
	}
	public void testBug578132_001() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						 static  int foo(Object o, boolean b) {
						   return switch (o) {\s
						      case X x when b -> 0; // compilation error
						      default -> 1;
						   };\s
						 }\s
						 public static void main(String[] args) {
						   System.out.println(X.foo(new X(), true));
						   System.out.println(X.foo(new Object(), true));
						 }
						}"""
				},
				"0\n"+
				"1");
	}
	public void test576788_1() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						     public static void foo1(Object o) {
						    	boolean b = switch (o) {
						    		case String s -> {
						    			yield s == null;
						    		}
						    		case null -> {
						    			yield true;
						    		}
						    		default -> true;
						    	};
						    	System.out.println(b);
						    }\s
						    public static void main(String[] argv) {
						    	foo1(null);
						    	foo1("abc");
						    }
						}"""
				},
				"true\n"+
				"false");
	}
	public void testBug577374_001() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    sealed interface A {}
						    sealed interface B1 extends A {}
						    sealed interface B2 extends A {}
						    sealed interface C extends A {}
						    final class D1 implements B1, C {}
						    final class D2 implements B2, C {}
						   \s
						    public static int test(A arg) {
						        return switch (arg) {
						            case B1 b1 -> 1;
						            case B2 b2 -> 2;
						        };
						    }
						    public static void main(String[] args) {
						   X.D1 d1 = new X().new D1();
						   System.out.println(X.test(d1));
						 }
						}"""
				},
				"1");
	}
	public void testBug579355_001() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						       static void constantLabelMustAppearBeforePattern(Integer o) {
						               switch (o) {
						               case -1, 1 -> System.out.println("special case:" + o);
						               case Integer i when i > 0 -> System.out.println("positive integer: " + o);
						               case Integer i -> System.out.println("other integer: " + o);
						               }
						       }
						
						       public static void main(String[] args) {
						               X.constantLabelMustAppearBeforePattern(-10);
						               X.constantLabelMustAppearBeforePattern(-1);
						               X.constantLabelMustAppearBeforePattern(0);
						               X.constantLabelMustAppearBeforePattern(1);
						               X.constantLabelMustAppearBeforePattern(10);
						       }\s
						}"""
				},
				"""
					other integer: -10
					special case:-1
					other integer: 0
					special case:1
					positive integer: 10""");
	}
	public void testBug579355_002() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						       static void constantLabelMustAppearBeforePattern(Integer o) {
						               switch (o) {
						               case -1, 1 -> System.out.println("special case:" + o);
						               case null -> System.out.println("null");
						               case Integer i when i > 0 -> System.out.println("positive integer: " + o);
						               case Integer i -> System.out.println("other integer: " + o);
						               }
						       }
						
						       public static void main(String[] args) {
						               X.constantLabelMustAppearBeforePattern(-10);
						               X.constantLabelMustAppearBeforePattern(-1);
						               X.constantLabelMustAppearBeforePattern(0);
						               X.constantLabelMustAppearBeforePattern(1);
						               X.constantLabelMustAppearBeforePattern(10);
						               X.constantLabelMustAppearBeforePattern(null);
						       }\s
						}"""
				},
				"""
					other integer: -10
					special case:-1
					other integer: 0
					special case:1
					positive integer: 10
					null""");
	}
	public void testBug579355_004() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    public static Color color = Color.BLUE;
						    public static void main(String args[]) {
						        Color c;\s
						        var result = switch(color){
						                case BLUE ->  (c = color) == Color.BLUE;
						                case RED, GREEN ->  (c = color) + "text";
						                case YELLOW ->  new String((c = color) + "text");
						                default ->  (c = color);
						                };
						        if (result != null && c == Color.BLUE) {
						        	System.out.println("Pass");
						        } else {
						        	System.out.println("Fail");
						        }
						    }\s
						}
						enum Color{BLUE, RED, GREEN, YELLOW;}"""
				},
				"Pass");
	}
	public void testBug579355_005() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						    public int foo(Character c) {
						        int result = 0;
						        result = switch (c) {
						            case Character c1 -> 1;
						            case (short)1 -> 5;
						        };
						        return result;
						    }
						    public static void main(String args[]) {
						    	X x = new X();
						    	if (x.foo('\\u0001') == 1) {
						            System.out.println("Pass");
						        } else {
						        	System.out.println("Fail");
						        }
						    }
						}"""
				},
				"Pass");
	}
	public void testIssue449_001() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						  public static void main(String[] args) {
						    Object obj = null;
						    var a = switch (obj) {
						        case null -> 1;
						        default   -> 2;
						    };
						    System.out.println(a);
						  }
						}"""
				},
				"1");
	}
	public void testIssue554_001() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						  public static void main(String[] args) {
						    String obj = null;
						    var a = switch (obj) {
						        case null -> 1;
						        default   -> 2;
						    };
						    System.out.println(a);
						  }
						}"""
				},
				"1");
	}
	public void testIssue_556_001() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						     public static void foo1(String o) {
						    	boolean b = switch (o) {
						    		case "abc", null -> {
						    			yield false;
						    		}
						    		default -> true;
						    	};
						    	System.out.println(b);
						    }\s
						    public static void main(String[] argv) {
						    	foo1(null);
						    	foo1("abc");
						    }
						}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						case "abc", null -> {
						            ^^^^
					A null case label has to be either the only expression in a case label or the first expression followed only by a default
					----------
					""");
	}
	public void testIssue_556_002() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						 public static void main(String[] args) {
						   foo("Hello World!");
						 }
						
						 private static void foo(Object o) {
						   switch (o) {
						    case default:
						     System.out.println("Object: " + o);
						   }
						 }
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 8)
						case default:
						     ^^^^^^^
					A \'default\' can occur after \'case\' only as a second case label expression and that too only if \'null\' precedes  in \'case null, default\'\s
					----------
					""");
	}
	public void testIssue_556_003() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						 public static void main(String[] args) {
						   foo(Integer.valueOf(11));
						   foo(Integer.valueOf(9));
						   foo("Hello World!");
						 }
						
						 private static void foo(Object o) {
						   switch (o) {
						   case Integer i when i>10:
						     System.out.println("Greater than 10:" + o);
						     break;
						   case Integer j when j>0:
						     System.out.println("Greater than 0:" + o);
						     break;
						   case default:
						     System.out.println("Give Me Some SunShine:" + o);
						   }
						 }
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 16)
						case default:
						     ^^^^^^^
					A \'default\' can occur after \'case\' only as a second case label expression and that too only if \'null\' precedes  in \'case null, default\'\s
					----------
					""");
	}
	public void testIssue_556_004() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						 public static void main(String[] args) {
						   foo("Hello World!");
						 }
						
						 private static void foo(Object o) {
						   switch (o) {
						   case Integer i :
						     System.out.println("Integer:" + o);
						     break;
						   case default:
						     System.out.println("Object" + o);
						   case default:
						     System.out.println("Give me Some Sunshine" + o);
						   }
						 }
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 11)
						case default:
						     ^^^^^^^
					A \'default\' can occur after \'case\' only as a second case label expression and that too only if \'null\' precedes  in \'case null, default\'\s
					----------
					2. ERROR in X.java (at line 13)
						case default:
						^^^^^^^^^^^^
					The default case is already defined
					----------
					3. ERROR in X.java (at line 13)
						case default:
						     ^^^^^^^
					A \'default\' can occur after \'case\' only as a second case label expression and that too only if \'null\' precedes  in \'case null, default\'\s
					----------
					""");
	}
	public void testIssue_556_005() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 private static void foo(Object o) {
					   switch (o.hashCode()) {
					     case default : System.out.println("Default");
					     default : System.out.println("Object");
					   }
					 }
					 public static void main(String[] args) {
					   foo("Hello World");
					   Zork();
					 }
					}
					class Y {}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					case default : System.out.println("Default");
					     ^^^^^^^
				A \'default\' can occur after \'case\' only as a second case label expression and that too only if \'null\' precedes  in \'case null, default\'\s
				----------
				2. ERROR in X.java (at line 5)
					default : System.out.println("Object");
					^^^^^^^
				The default case is already defined
				----------
				3. ERROR in X.java (at line 10)
					Zork();
					^^^^
				The method Zork() is undefined for the type X
				----------
				""");
	}
	public void testIssue_556_006() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 public static int foo(Integer o) {
					   int k = 0;
					   switch (o) {
					     case 0, default   : k = 1;
					   }
					   return k;
					 }\s
					 public static void main(String[] args) {
					   System.out.println(foo(100 ));
					 }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					case 0, default   : k = 1;
					        ^^^^^^^
				A \'default\' can occur after \'case\' only as a second case label expression and that too only if \'null\' precedes  in \'case null, default\'\s
				----------
				""");
	}
	public void testIssue_556_007() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					 public static int foo(Integer o) {
					   int k = 0;
					   switch (o) {
					     case 0, default, 1   : k = 1;
					   }
					   return k;
					 }\s
					 public static void main(String[] args) {
					   System.out.println(foo(100 ));
					 }
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					case 0, default, 1   : k = 1;
					        ^^^^^^^
				A \'default\' can occur after \'case\' only as a second case label expression and that too only if \'null\' precedes  in \'case null, default\'\s
				----------
				""");
	}
	public void testIssue_556_008() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
							public void foo(Object o) {
							  try{
								switch (o) {
								  default:
									  break;
								  case String s :
									  System.out.println(10);
									  break;
								  case String s when (s.length() == 10):
									  System.out.println(s);
								}\s
							  } catch(Exception t) {
								 t.printStackTrace(System.out);
							  }
							}
							public static void main(String[] args) {
								  (new X()).foo(null);
							}
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 7)
						case String s :
						     ^^^^^^^^
					This case label is dominated by one of the preceding case labels
					----------
					2. ERROR in X.java (at line 10)
						case String s when (s.length() == 10):
						     ^^^^^^^^
					This case label is dominated by one of the preceding case labels
					----------
					""");
	}
	public void testIssue_556_009() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
							public void foo(Object o) {
							  try{
								switch (o) {
								  case null, default:
									  break;
								  case String s :
									  System.out.println(s);
								}\s
							  } catch(Exception t) {
								 t.printStackTrace(System.out);
							  }
							}
							public static void main(String[] args) {
								  (new X()).foo(null);
							}
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 7)
						case String s :
						     ^^^^^^^^
					This case label is dominated by one of the preceding case labels
					----------
					""");
	}
	public void testIssue658() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
							public static void main(String argv[]) {
								(new X()).foo("abc");
							}
							public void foo(String s) {
								int v = 0;
								Boolean b1 = Boolean.valueOf(true);
								switch (s) {
									case String obj when b1 -> v = 1;
									default -> v = 0;
								}
								System.out.println(v);
								Boolean b2 = Boolean.valueOf(false);
								switch (s) {
									case String obj when b2 -> v = 1;
									default -> v = 0;
								}
								System.out.println(v);
							}
						}"""
				},
				"1\n0");
	}
	public void testIssue711_1() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						import java.util.*;
						public class X {
						public static void foo(List<Number> l) {
							switch (l) {
							    case ArrayList<Number> al ->\s
							        System.out.println("An ArrayList of Number");
							    case ArrayList<? extends Number> aln -> // Error - dominated case label
							        System.out.println("An ArrayList of Number");
							    default ->\s
							        System.out.println("A List");
							}
						}
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 7)
						case ArrayList<? extends Number> aln -> // Error - dominated case label
						     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					This case label is dominated by one of the preceding case labels
					----------
					""");
	}
	public void testIssue711_2() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						import java.util.*;
						public class X {
						public static void foo(List<Number> l) {
							switch (l) {
							    case ArrayList<? extends Number> aln ->
							        System.out.println("An ArrayList of Number");
							    case ArrayList<Number> al ->  // Error - dominated case label
							        System.out.println("An ArrayList of Number");
							    default ->\s
							        System.out.println("A List");
							}
						}
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 7)
						case ArrayList<Number> al ->  // Error - dominated case label
						     ^^^^^^^^^^^^^^^^^^^^
					This case label is dominated by one of the preceding case labels
					----------
					""");
	}
	public void testIssue742_1() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						import java.util.*;
						public class X {
						public static void foo(Integer n) {
						  switch (n) {
						    case Integer i when true -> // Allowed but why write this?
						        System.out.println("An integer");\s
						    case Integer i ->                     // Error - dominated case label
						        System.out.println("An integer");\s
						  }
						}
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 7)
						case Integer i ->                     // Error - dominated case label
						     ^^^^^^^^^
					The switch statement cannot have more than one unconditional pattern
					----------
					2. ERROR in X.java (at line 7)
						case Integer i ->                     // Error - dominated case label
						     ^^^^^^^^^
					This case label is dominated by one of the preceding case labels
					----------
					""");
	}
	public void testIssue742_2() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						import java.util.*;
						public class X {
						public static void foo(Integer n) {
						  switch (n) {
						    case Integer i -> // Allowed but why write this?
						        System.out.println("An integer");\s
						    case Integer i when true ->                     // Error - dominated case label
						        System.out.println("An integer");\s
						  }
						}
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 7)
						case Integer i when true ->                     // Error - dominated case label
						     ^^^^^^^^^
					The switch statement cannot have more than one unconditional pattern
					----------
					2. ERROR in X.java (at line 7)
						case Integer i when true ->                     // Error - dominated case label
						     ^^^^^^^^^
					This case label is dominated by one of the preceding case labels
					----------
					""");
	}
	public void testIssue712_001() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						      \s
						       public static void main(String[] args) {
						               Object o = "Hello World";
						               foo(o);
						       }
						       public static void foo(Object o) {
						         switch (o) {
						           case String s:
						               System.out.println(s);        // No break!
						           case R():
						               System.out.println("It's either an R or a string"); // Allowed
						               break;
						           default:
						         }
						       }
						
						}
						
						record R() {}\s
						record S() {}
						"""
				},
				"Hello World\n" +
				"It\'s either an R or a string");
	}
	public void testIssue712_002() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						\s
						 public static void main(String[] args) {
						   Object o = new R();
						   foo(o);
						 }
						 public static void foo(Object o) {
						   switch (o) {
						     case R():
						     case S():                         // Multiple case labels!
						         System.out.println("Either R or an S");
						         break;
						     default:
						 }
						 }
						
						}
						
						record R() {}
						record S() {}
						"""
				},
				"Either R or an S");
	}
	public void testIssue712_003() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						\s
						 public static void main(String[] args) {
						   Object o = null;
						   foo(o);
						 }
						 public static void foo(Object o) {
						   switch (o) {
						     case null:
						     case R():                         // Multiple case labels!
						         System.out.println("Either null or an R");
						         break;
						     default:
						 }
						 }
						}
						
						record R() {}
						record S() {}"""
				},
				"Either null or an R");
	}
	public void testIssue712_004() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						\s
						 public static void foo(Object o) {
						   switch (o) {
						     case Integer i :
						     case R():                         // Multiple case labels!
						         System.out.println("R Only");
						     default:
						   }
						 }
						}
						\s
						record R() {}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 5)
						case Integer i :
						^^^^^^^^^^^^^^
					Illegal fall-through from a case label pattern
					----------
					""");
	}
	public void testIssueDefaultDominance_001() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						 public class X {
						\s
						 public static void foo(Object o) {
						   switch (o) {
						   case Float f: System.out.println("integer"); break;
						   default: System.out.println("default"); break;
						   case Integer i: System.out.println("integer"); break;
						   }     \s
						 }
						}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 7)
						case Integer i: System.out.println("integer"); break;
						     ^^^^^^^^^
					This case label is dominated by one of the preceding case labels
					----------
					""");
	}
	public void testIssueDefaultDominance_002() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						 public class X {
						\s
						 public static void foo(Object o) {
						   switch (o) {
						   default: System.out.println("default"); break;
						   case Integer i: System.out.println("integer"); break;
						   }     \s
						 }
						}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 6)
						case Integer i: System.out.println("integer"); break;
						     ^^^^^^^^^
					This case label is dominated by one of the preceding case labels
					----------
					""");
	}
	public void testIssueDefaultDominance_003() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						 public class X {
						\s
						 public static void foo(Object o) {
						   switch (o) {
						   default: System.out.println("default"); break;
						   case null: System.out.println("null"); break;
						   }     \s
						 }
						}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 6)
						case null: System.out.println("null"); break;
						     ^^^^
					This case label is dominated by one of the preceding case labels
					----------
					""");
	}
	public void testIssueDefaultDominance_004() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						 public class X {
						\s
						 public static void foo(Object o) {
						   switch (o) {
						   case Float f: System.out.println("integer"); break;
						   default: System.out.println("default"); break;
						   case null: System.out.println("null"); break;
						   }     \s
						 }
						}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 7)
						case null: System.out.println("null"); break;
						     ^^^^
					This case label is dominated by one of the preceding case labels
					----------
					""");
	}
	public void testIssue919() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					 public class X {
					   static void defaultCanAppearBeforePattern(Integer i) {
						  switch (i) {
						  case null -> System.out.println("value unavailable: " + i);
						  case -1, 1 -> System.out.println("absolute value 1: " + i);
						  default -> System.out.println("other integer: " + i);
						  case Integer value when value > 0 -> System.out.println("positive integer: " + i);
						  }
					  }
					  static void defaultCanAppearBeforeNull(Integer i) {
						  switch (i) {
						  case -1, 1 -> System.out.println("absolute value 1: " + i);
						  default -> System.out.println("other integer: " + i);
						  case null -> System.out.println("value unavailable: " + i);
						  case Integer value when value > 0 -> System.out.println("positive integer: " + i);
						  }
					  }
					  static void defaultCanAppearBeforeConstantLabel(Integer i) {
						  switch (i) {
						  case null -> System.out.println("value unavailable: " + i);
						  default -> System.out.println("other integer: " + i);
						  case -1, 1 -> System.out.println("absolute value 1: " + i);
						  case Integer value when value > 0 -> System.out.println("positive integer: " + i);
						  }
					  }
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 7)
					case Integer value when value > 0 -> System.out.println("positive integer: " + i);
					     ^^^^^^^^^^^^^
				This case label is dominated by one of the preceding case labels
				----------
				2. ERROR in X.java (at line 14)
					case null -> System.out.println("value unavailable: " + i);
					     ^^^^
				This case label is dominated by one of the preceding case labels
				----------
				3. ERROR in X.java (at line 15)
					case Integer value when value > 0 -> System.out.println("positive integer: " + i);
					     ^^^^^^^^^^^^^
				This case label is dominated by one of the preceding case labels
				----------
				4. ERROR in X.java (at line 23)
					case Integer value when value > 0 -> System.out.println("positive integer: " + i);
					     ^^^^^^^^^^^^^
				This case label is dominated by one of the preceding case labels
				----------
				""");
	}
	public void testIssue1126a() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						 	static int foo(String i) {
								return switch (i) {
							     case "abc" -> 0;
							     case "abcd" -> 1;
							     case String s -> -1;
							   };
							}
						 public static void main(String[] args) {
						   System.out.println(foo("abcd"));
						   System.out.println(foo("abc"));
						 }
						}""",
				},
				"1\n" +
				"0");
	}
	public void testIssue1126b() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						 	static int foo(String i) {
								return switch (i) {
							     case "FB" -> 0;
							     case "Ea" -> 1;
							     case String s -> -1;
							   };
							}
						 public static void main(String[] args) {
						   System.out.println(foo("Ea"));
						   System.out.println(foo("FB"));
						 }
						}""",
				},
				"1\n" +
				"0");
	}
	public void testIssue587_001() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						  sealed interface I<T> permits A, B {}
						  final static class A<T> implements I<String> {}
						  final static class B<Y> implements I<Y> {}
						
						  static int testGenericSealedExhaustive(I<Integer> i) {
						    return switch (i) {
						      // Exhaustive as no A case possible!
						      case B<Integer> bi -> 42;
						    };
						  }
						  public static void main(String[] args) {
						       System.out.println(testGenericSealedExhaustive(new B<Integer>()));
						  }
						}""",
				},
				"42");
	}
	public void testIssueExhaustiveness_001() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						 public static void main(String argv[]) {
						   System.out.println(foo());
						 }
						
						 public static int foo() {
						   return switch (I.getIC()) {
						     case IC c -> 42;
						   };
						 }
						}
						
						sealed interface I<T> permits IC {
						 public static I getIC() {
						   return new IC(){};
						 }
						}
						
						non-sealed interface IC<T> extends I {}""",
				},
				"42");
	}
	public void testIssueExhaustiveness_002() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						record R(int i) {}
						public class X {
						
						    public static int foo(R exp) {
						        return switch (exp) {
						            case R r -> 42;
						        };
						    }
						    public static void main(String argv[]) {
						       System.out.println(foo(new R(10)));
						    }
						}"""
				},
				"42");
	}
	public void testIssueExhaustiveness_003() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						record R(X x) {}
						public class X {
						
						    public static int foo(R exp) {
						        return switch (exp) {
						            case R(Object o) -> 42;
						        };
						    }
						    public static void main(String argv[]) {
						       System.out.println(foo(new R(new X())));
						    }
						}"""
				},
				"42");
	}
	public void testIssueExhaustiveness_004() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						sealed interface I permits A, J {}
						sealed interface J extends I {}
						
						final class A implements I {}
						final record R() implements J {}
						
						public class X {
						
						    public static int foo(I i) {
						        return switch (i) {
						            case A a -> 0;
						        };
						    }
						
						    public static void main(String argv[]) {
						       Zork();
						    }
						}"""
				},
				"""
					----------
					1. ERROR in X.java (at line 10)
						return switch (i) {
						               ^
					A switch expression should have a default case
					----------
					2. ERROR in X.java (at line 16)
						Zork();
						^^^^
					The method Zork() is undefined for the type X
					----------
					"""
);
	}
	public void testIssueExhaustiveness_005() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						sealed interface I {}
						final class A implements I {}
						
						record R<T extends I>(T x, T  y) {}
						
						public class X {
						    public static int foo(R r) {
						       return  switch (r) {
						            case R(A a1, A a2) -> 0;
						        };
						    }
						
						    @SuppressWarnings("unchecked")
						       public static void main(String argv[]) {
						       System.out.println(X.foo(new R(new A(), new A())));
						    }
						}"""
				},
				"0");
	}
	public void testIssue1250_1() {
		if (this.complianceLevel < ClassFileConstants.JDK21) {
			return;
		}
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						enum E {
							A1, A2;
						}
						public class X {
							public static void foo(E e) {
								switch (e) {
									case E.A1 -> {
										System.out.println("A1");
									}
									case E.A2 -> {
										System.out.println("A2");
									}
								}
							}
							public static void main(String[] args) {
								foo(E.A1);
							}
						}""",
				},
				"A1");
	}
	public void testIssue1250_2() {
		if (this.complianceLevel < ClassFileConstants.JDK21) {
			return;
		}
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						enum E {
							A1, A2;
							enum InnerE {
								B1, B2;
							}
						}
						public class X {
							public static void foo(E.InnerE e) {
								switch (e) {
									case E.InnerE.B1 -> {
										System.out.println("B1"); //$NON-NLS-1$
									}\s
									default -> {}
								}
							}
							public static void main(String[] args) {\s
								foo(E.InnerE.B1);
							}
						}""",
				},
				"B1");
	}
	public void testIssue1250_3() {
		if (this.complianceLevel < ClassFileConstants.JDK21) {
			return;
		}
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						enum E {
							A1, A2;
							enum InnerE {
								B1, B2;
							}
						}
						public class X {
							public static void foo(E.InnerE e) {
								switch (e) {
									case E.A1 -> {
										System.out.println("B1"); //$NON-NLS-1$
									}\s
									default -> {}
								}
							}
							public static void main(String[] args) {\s
								foo(E.InnerE.B1);
							}
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 10)
						case E.A1 -> {
						     ^^^^
					Type mismatch: cannot convert from E to E.InnerE
					----------
					""");
	}
	public void testIssue1250_4() {
		if (this.complianceLevel < ClassFileConstants.JDK21) {
			return;
		}
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						interface I {}
						enum E implements I {
							A0, A1, A2, A3, A4;
						}
						public class X {
							public String testMethod(I exp) {
								String res = "";
								switch (exp) {
									case E.A0 -> {
										res = "const A0";
										break;
									}
									case E.A1 -> {
										res = "const A1";
										break;
									}
									case E.A2 -> {
										res = "const A2";
										break;
									}
									case E.A3 -> {
										res = "const A3";
										break;
									}
									case E.A4 -> {
										res = "const A4";
										break;
									}
									default -> {
										res = "default";
									}
								}
								return res;
							}
							public static void main(String[] args) {
								System.out.println((new X()).testMethod(E.A2));
								System.out.println((new X()).testMethod(E.A3));
								System.out.println((new X()).testMethod(E.A4));
								System.out.println((new X()).testMethod(E.A0));
								System.out.println((new X()).testMethod(E.A1));
								System.out.println((new X()).testMethod(new I() {
								}));
							}
						}""",
				},
				"""
					const A2
					const A3
					const A4
					const A0
					const A1
					default""");
	}
	public void testIssue1250_5() {
		if (this.complianceLevel < ClassFileConstants.JDK21) {
			return;
		}
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						interface I {
						}
						enum E implements I {
							A0, A1, A2, A3, A4;
						}
						enum E1 implements I {
							B0, B1;
						}
						public class X {
							public String foo(I exp) {
								String res = "";
								switch (exp) {
									case E.A0 -> {
										res = "const A0";
										break;
									}
									case E.A1 -> {
										res = "const A1";
										break;
									}
									case E1.B0 -> {
										res = "const B0";
										break;
									}
									case E e -> {
										res = e.toString();
									}
									case E1 e1 -> {
										res = e1.toString();
									}
									default -> {
										res = "default";
									}
								}
								return res;
							}
							public static void main(String[] args) {
								System.out.println((new X()).foo(E.A0));
								System.out.println((new X()).foo(E.A1));
								System.out.println((new X()).foo(E.A2));
								System.out.println((new X()).foo(E1.B0));
								System.out.println((new X()).foo(E1.B1));
								System.out.println((new X()).foo(new I() {
								}));
							}
						}""",
				},
				"""
					const A0
					const A1
					A2
					const B0
					B1
					default""");
	}
	public void testIssue1250_6() {
		if (this.complianceLevel < ClassFileConstants.JDK21) {
			return;
		}
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						interface I {
						}
						enum XEnum {
						    A, B;
						    interface I {}
						    enum E implements I {
						        A0, A1;
						    }
						}
						public class X {
						    public String foo(XEnum.I exp) {
						        String res = "";
						        switch (exp) {
						            case XEnum.E.A0 -> {
						                res = "A0";
						                break;
						            }
						            case XEnum.E.A1 -> {
						                res = "A1";
						                break;
						            }
						            default -> {
						                res = "Ad";
						            }
						        }
						        return res;
						    }
							public static void main(String[] args) {
								System.out.println((new X()).foo(XEnum.E.A1));
							}
						}""",
				},
				"A1");
	}
	public void testIssue1250_7() {
		if (this.complianceLevel < ClassFileConstants.JDK21) {
			return;
		}
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						interface I {
						    interface InnerI {}
						    enum E implements InnerI {
						        A0, A1;
						    }
						}
						public class X {
						    public String foo(I.InnerI exp) {
						        String res = "";
						        res = switch (exp) {
						            case I.E.A0 -> {
						                yield "A0";
						            }
						            case I.E.A1 -> {
						                yield "A1";
						            }
						            default -> {
						                yield "Ad";
						            }
						        };
						        return res;
						    }
							public static void main(String[] args) {
								System.out.println((new X()).foo(I.E.A1));
							}
						}""",
				},
				"A1");
	}
	public void testIssue1250_8() {
		if (this.complianceLevel < ClassFileConstants.JDK21) {
			return;
		}
		this.runConformTest(
				new String[] {
					"p/q/X.java",
					"""
						package p.q;
						interface I {
						}
						enum E implements I {
							A0, A1, A2, A3, A4;
						}
						enum E1 implements I {
							B0, B1;
						}
						public class X {
							public String foo(I exp) {
								String res = "";
								switch (exp) {
									case E.A0 -> {
										res = "const A0";
										break;
									}
									case E.A1 -> {
										res = "const A1";
										break;
									}
									case E1.B0 -> {
										res = "const B0";
										break;
									}
									case E e -> {
										res = e.toString();
									}
									case E1 e1 -> {
										res = e1.toString();
									}
									default -> {
										res = "default";
									}
								}
								return res;
							}
							public static void main(String[] args) {
								System.out.println((new X()).foo(E.A0));
								System.out.println((new X()).foo(E.A1));
								System.out.println((new X()).foo(E.A2));
								System.out.println((new X()).foo(E1.B0));
								System.out.println((new X()).foo(E1.B1));
								System.out.println((new X()).foo(new I() {
								}));
							}
						}""",
				},
				"""
					const A0
					const A1
					A2
					const B0
					B1
					default""");
	}
	public void testIssue1351_1() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public static void foo() {
									Object o = new String("");
									int len = 2;
									switch (o) {
									case String o1 when ((String) o).length() == o1.length() :
										o = null;
										o1 = null;
										break;
									default:
										break;
									}
								}
							} """,
				},
				"""
					----------
					1. ERROR in X.java (at line 6)
						case String o1 when ((String) o).length() == o1.length() :
						                              ^
					Local variable o referenced from a guard must be final or effectively final
					----------
					""");
	}
	public void testIssue1351_2() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							public class X {
								public static void foo() {
									Object o = new String("");
									int len = 2;
									switch (o) {
									case String o1 when o1.length() == ((String) o).length():
										o = null;
										o1 = null;
										break;
									default:
										break;
									}
								}
							} """,
				},
				"""
					----------
					1. ERROR in X.java (at line 6)
						case String o1 when o1.length() == ((String) o).length():
						                    ^^
					Local variable o1 referenced from a guard must be final or effectively final
					----------
					""");
	}
	public void testIssue1351_3() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							class C {
								int v;
								public int value() { return this.v;
							}
							}
							public class X {
								public void foo(C c) {
									switch (c) {
									case C c1 when c1.v == c1.value():
										c1 = null;
										break;
									default:
										break;
									}
								}
							}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 9)
						case C c1 when c1.v == c1.value():
						               ^^
					Local variable c1 referenced from a guard must be final or effectively final
					----------
					""");
	}
	public void testIssue1351_3a() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							class C {
								int v;
								public int value() { return this.v;
							}
							}
							public class X {
								public void foo(C c) {
									switch (c) {
									case C c1 when c1.value() == c1.v:
										c1 = null;
										break;
									default:
										break;
									}
								}
							} """,
				},
				"""
					----------
					1. ERROR in X.java (at line 9)
						case C c1 when c1.value() == c1.v:
						               ^^
					Local variable c1 referenced from a guard must be final or effectively final
					----------
					""");
	}
	public void testIssue1351_3b() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							interface I {
								int v = 0;
								public default int val() {
									return v;
								}
							}
							public class X {
								public void foo(I intf) {
									switch (intf) {
									case I i1 when i1.v > i1.val():
										i1 = null;
										break;
									default:
										break;
									}
								}
							} """,
				},
				"""
					----------
					1. ERROR in X.java (at line 10)
						case I i1 when i1.v > i1.val():
						               ^^
					Local variable i1 referenced from a guard must be final or effectively final
					----------
					2. WARNING in X.java (at line 10)
						case I i1 when i1.v > i1.val():
						                  ^
					The static field I.v should be accessed in a static way
					----------
					""");
	}
	public void testIssue1351_3c() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							interface I {
								int v = 1;
								public default int val() {
									return 0;
								}
							}
							public class X {
								public void foo(I intf) {
									switch (intf) {
									case I i1 when I.v > i1.val():
										i1 = null;
										break;
									default:
										break;
									}
								}
							} """,
				},
				"""
					----------
					1. ERROR in X.java (at line 10)
						case I i1 when I.v > i1.val():
						                     ^^
					Local variable i1 referenced from a guard must be final or effectively final
					----------
					""");
	}
	public void testIssue1351_4() {
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							class C {
								int v;
								public int value() { return this.v;
							}
							}
							public class X {
								C c0;
								public void foo(C c) {
									switch (c) {
									case C c1 when c0.v == c0.value():
										c0 = null;
										break;
									}
								}
							} """,
				},
				"""
					----------
					1. ERROR in X.java (at line 9)
						switch (c) {
						        ^
					An enhanced switch statement should be exhaustive; a default label expected
					----------
					""");
	}
	public void testIssue1351_5() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
							public static void foo() {
								Integer in = 0;
								switch (in) {
								    case Integer i ->
								        System.out.println("Boxed");
								    case 95 ->
								        System.out.println("Literal!");
								}
							}
						} """,
				},
				"""
					----------
					1. ERROR in X.java (at line 7)
						case 95 ->
						     ^^
					This case label is dominated by one of the preceding case labels
					----------
					""");
	}
	public void testIssue1351_6() {
		runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
							static String foo(Color o) {
								return switch (o) {
							     case Color s when true  -> s.toString();
							     case Red -> "Red";
							     case null -> "";
							   };
							}
						}\s
						enum Color {
							Red;\s
						}""",
				},
				"""
					----------
					1. ERROR in X.java (at line 5)
						case Red -> "Red";
						     ^^^
					This case label is dominated by one of the preceding case labels
					----------
					""");
	}
	public void testIssue1351_7() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public int foo(Byte exp) {
							int res = 0;
							switch (exp) {
								case Byte p when p.equals(exp), (byte) 0 -> {
									res = 6;
									break;
								}
								default -> {}
							}
							return res;
						}
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					case Byte p when p.equals(exp), (byte) 0 -> {
					                                ^^^^^^^^
				Cannot mix pattern with other case labels
				----------
				""");
	}
	public void testIssue1351_8() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public int foo(Byte exp) {
							int res = 0;
							switch (exp) {
								case (byte) 0, Byte p when p.equals(exp) -> {
									res = 6;
									break;
								}
								default -> {}
							}
							return res;
						}
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					case (byte) 0, Byte p when p.equals(exp) -> {
					               ^^^^^^^^^^^^^^^^^^^^^^^^^
				Cannot mix pattern with other case labels
				----------
				""");
	}
	public void testIssue1351_9() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public int foo(Byte exp) {
							int res = 0;
							switch (exp) {
								case (byte) 0, (byte) 10, Byte p when p.equals(exp) -> {
									res = 6;
									break;
								}
								default -> {}
							}
							return res;
						}
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					case (byte) 0, (byte) 10, Byte p when p.equals(exp) -> {
					                          ^^^^^^^^^^^^^^^^^^^^^^^^^
				Cannot mix pattern with other case labels
				----------
				""");
	}
	public void testIssue1351_10() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public int foo(Byte exp) {
							int res = 0;
							switch (exp) {
								case Byte p when p.equals(exp), null -> {
									res = 6;
									break;
								}
								default -> {}
							}
							return res;
						}
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					case Byte p when p.equals(exp), null -> {
					                                ^^^^
				Cannot mix pattern with other case labels
				----------
				2. ERROR in X.java (at line 5)
					case Byte p when p.equals(exp), null -> {
					                                ^^^^
				A null case label has to be either the only expression in a case label or the first expression followed only by a default
				----------
				""");
	}
	public void testIssue1351_11() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public int foo(Byte exp) {
							int res = 0;
							switch (exp) {
								case Byte p when p.equals(exp), default -> {
									res = 6;
									break;
								}
							}
							return res;
						}
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					case Byte p when p.equals(exp), default -> {
					                                ^^^^^^^
				Cannot mix pattern with other case labels
				----------
				2. ERROR in X.java (at line 5)
					case Byte p when p.equals(exp), default -> {
					                                ^^^^^^^
				A 'default' can occur after 'case' only as a second case label expression and that too only if 'null' precedes  in 'case null, default'\s
				----------
				""");
	}

	public void testDisambiguatedRestrictedIdentifierWhenAsFirstMethodInvokation() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
							public static void main(String argv[]) {
								when("Pass");
							}
							static void when(String arg) {
								System.out.println(arg);
							}
						}"""
				},
				"Pass");
	}

	public void testDisambiguatedRestrictedIdentifierWhenAsFirstVariableDeclaration() {
		runConformTest(
				new String[] {
					"when.java",
					"""
						public class when {
							public static void main(String argv[]) {
								when x = new when();
								System.out.println(x);
							}
							public String toString() {
								return "Pass";
							}
						}"""
				},
				"Pass");
	}

	public void testDisambiguatedRestrictedIdentifierWhenAsTypeInACase() {
		runConformTest(
				new String[] {
					"when.java",
					"""
						public class when {
							public String toString() {
								return switch((Object) this) {
									case when x -> "Pass";
									default -> "Fail";
								};
							}
							public static void main(String argv[]) {
								System.out.println(new when());
							}
						}"""
				},
				"Pass");
	}

	public void testDisambiguatedRestrictedIdentifierWhenAfterAParenthesis() {
		runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
							public static void main(String argv[]) {
								System.out.println( (Boolean) when(true) );
							}
							static Object when(Object arg) {
								return arg;
							}
						}"""
				},
				"true");
	}

	public void testValidCodeWithVeryAmbiguousUsageOfWhen() {
		runConformTest(
				new String[] {
					"when.java",
					"""
						class when {
						  boolean when = true;
						  static boolean when(when arg) {
						    return switch(arg) {
						      case when when when when.when && when.when(null) -> when.when;
						      case null -> true;
						      default -> false;
						    };
						  }
						  public static void main(String[] args) {
						    System.out.println(when(new when()));
						  }
						}"""
				},
				"true");
	}
	public void testIssue1466_01() {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
					public class X {

						  private static String foo(Integer i) {
						    return switch (i) {
						      case null -> "null";
						      case Integer value when value > 0 -> value.toString();
						      default -> i.toString();
						    };
						  }

						  public static void main(String[] args) {
						    System.out.println(foo(0));
						  }
						}

					""",
				},
				"0");
	}
	public void testIssue1466_02() {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
					public class X {
					  public static void main(String[] args) {
					    constantLabelMustAppearBeforePatternInteger(-1);
					    constantLabelMustAppearBeforePatternInteger(0);
					    constantLabelMustAppearBeforePatternInteger(42);
					    constantLabelMustAppearBeforePatternInteger(-99);
					    constantLabelMustAppearBeforePatternInteger(Integer.valueOf(123));
					    constantLabelMustAppearBeforePatternInteger(null);
					  }
					  static String constantLabelMustAppearBeforePatternInteger(Integer i) {
					    switch (i) {
					      case null -> System.out.println("value unavailable: " + i);
					      case -1, 1 -> System.out.println("absolute value 1: " + i);
					      case Integer value when value > 0 -> System.out.println("positive integer: " + i);
					      default -> System.out.println("other integer: " + i);
					    }
					    return i == null ? "null" : i.toString();
					  }
					}

					""",
				},
				"""
					absolute value 1: -1
					other integer: 0
					positive integer: 42
					other integer: -99
					positive integer: 123
					value unavailable: null"""
);
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1767
	// NPE in switch with case null
	public void testIssue1767() {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
					public class X {
					   public static void main(String[] args) {
						   Integer o = null;
						   switch (o) {
						     case null:
						       System.out.println("NULL");
						       break;
						     default : System.out.println(o);
						   }
					   }
					}
					""",
				},
				"NULL");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/277
	// [19] statement switch with a case null does not compile
	public void testIssue277() {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
					public class X {
					  enum Color { RED, BLACK }

					  public static void main(String[] args) {
					    Color color = null;
					    switch (color) {
					      case null -> System.out.println("NULL");
					      case RED -> System.out.println("RED");
					      case BLACK -> System.out.println("BLACK");
					    }
					  }
					}
					""",
				},
				"NULL");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/277
	// [19] statement switch with a case null does not compile
	public void testIssue277_original() {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
					public class X {
					  enum Color { RED, BLACK }

					  public static void main(String[] args) {
					    Color color = Color.RED;
					    switch (color) {
					      case null -> throw null;
					      case RED -> System.out.println("RED");
					      case BLACK -> System.out.println("BLACK");
					    }
					  }
					}
					""",
				},
				"RED");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/554
	// [19] statement switch with a case null does not compile
	public void testIssue554() {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
					public class X {
					    public static void main(String[] args) {
					        MyEnum val = null;
					        switch (val) {
					        case null:
					            System.out.println("val is null");
					            break;
					        }
					    }
					}
					enum MyEnum {
					    a
					}
					""",
				},
				"val is null");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/113
	// [switch] The Class file generated by ECJ for guarded patterns behaves incorrectly
	public void testGHI113() {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
					public class X {

						interface Shape {
							public double calculateArea();
						}

						record Triangle(int base, int height) implements Shape {

							@Override
							public double calculateArea() {
								return (0.5 * base * height);
							}

						}

						record Square(int side) implements Shape {

							@Override
							public double calculateArea() {
								return (side * side);
							}

						}

						static String evaluate(Shape s) {
							return switch(s) {
								case null ->
									"NULL";
								case Triangle T when (T.calculateArea() > 100) ->
								    "Large Triangle : " + T.calculateArea();
								case Triangle T ->
								    "Small Triangle : " + T.calculateArea();
								default ->
								    "shape : " + s.calculateArea();
							};
						}

						public static void main(String[] args) {
							System.out.println(evaluate(new Triangle(10, 10)));
							System.out.println(evaluate(new Triangle(20, 20)));
							System.out.println(evaluate(new Square(10)));
							System.out.println(evaluate(null));
						}
					}
					""",
				},
				"""
					Small Triangle : 50.0
					Large Triangle : 200.0
					shape : 100.0
					NULL""");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1853
	// [switch][pattern] Scope of pattern binding extends illegally resulting in wrong diagnostic
	public void testGH1853() {
		runConformTest(
			new String[] {
				"X.java",
				"""
				public class X {
				    public static void main(String[] args) {
						Object o = new Object();
						switch (o) {
						case String s :
							if (!(o instanceof String str))
								throw new RuntimeException();
						case null :
							if (!(o instanceof String str))
								throw new RuntimeException();
						default:
				            System.out.println("Default");
						}
					}
				}
				"""
			},
			"Default");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1856
	// [switch][record patterns] NPE: Cannot invoke "org.eclipse.jdt.internal.compiler.lookup.MethodBinding.isStatic()"
	public void testGHI1856() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
					public class X {

						public class Data {
						    String name;
						}

						record WrapperRec(ExhaustiveSwitch.Data data) {}


						public static void main(String[] args) {
						    switch (new Object()) {
						        case WrapperRec(var data) when data.name.isEmpty() -> { }
						        default -> {}
						    }
						}
					}
					""",
				},
				"""
					----------
					1. ERROR in X.java (at line 1)
						public class X {
						^
					Data cannot be resolved to a type
					----------
					2. ERROR in X.java (at line 7)
						record WrapperRec(ExhaustiveSwitch.Data data) {}
						                  ^^^^^^^^^^^^^^^^
					ExhaustiveSwitch cannot be resolved to a type
					----------
					3. ERROR in X.java (at line 12)
						case WrapperRec(var data) when data.name.isEmpty() -> { }
						                ^^^^^^^^
					Data cannot be resolved to a type
					----------
					""");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1856
	// [switch][record patterns] NPE: Cannot invoke "org.eclipse.jdt.internal.compiler.lookup.MethodBinding.isStatic()"
	public void testGHI1856_2() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
					public class X {

						public class Data {
						    String name;
						}

						record WrapperRec(ExhaustiveSwitch.Data data) {}


						public static void main(String[] args) {
						    switch (new Object()) {
						        case WrapperRec(ExhaustiveSwitch.Data data) when data.name.isEmpty() -> { }
						        default -> {}
						    }
						}
					}
					""",
				},
				"""
					----------
					1. ERROR in X.java (at line 1)
						public class X {
						^
					Data cannot be resolved to a type
					----------
					2. ERROR in X.java (at line 7)
						record WrapperRec(ExhaustiveSwitch.Data data) {}
						                  ^^^^^^^^^^^^^^^^
					ExhaustiveSwitch cannot be resolved to a type
					----------
					3. ERROR in X.java (at line 12)
						case WrapperRec(ExhaustiveSwitch.Data data) when data.name.isEmpty() -> { }
						                ^^^^^^^^^^^^^^^^
					ExhaustiveSwitch cannot be resolved to a type
					----------
					4. ERROR in X.java (at line 12)
						case WrapperRec(ExhaustiveSwitch.Data data) when data.name.isEmpty() -> { }
						                ^^^^^^^^^^^^^^^^^^^^^^^^^^
					Record component with type Data is not compatible with type ExhaustiveSwitch.Data
					----------
					""");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1955
	// [Patterns] Redesign resolution of patterns to follow natural visitation
	public void testGH1955() {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
					sealed interface I<T> {}
					record R<T extends A<B>>(T t) implements I<T> {}
					public class X {
					    @SuppressWarnings("rawtypes")
						public static <T extends I> int foo(T t) {
					        return switch(t) {
					            case R(A<? extends B> p) -> 0;
					            case R(var varp) -> 1;
					        };
					    }
					}
					class A<T> {}
					abstract class B {}
					class C extends B {}
					""",
				},
				"""
					----------
					1. ERROR in X.java (at line 8)
						case R(var varp) -> 1;
						     ^^^^^^^^^^^
					This case label is dominated by one of the preceding case labels
					----------
					""");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/773
	// [20][pattern switch] unnecessary code generated
	public void testIssue773() throws Exception {
		runConformTest(
			new String[] {
				"X.java",
				"""
				public class X {
					public static void main(String[] args) {
						Object o = null;
						foo(new R());
						foo(new S());
					}
					@SuppressWarnings("preview")
					public static void foo(Object o) {
						switch (o) {
						    case R():                         // Multiple case labels!
						        System.out.println("R Only");
						    case S():                         // Multiple case labels!
						        System.out.println("Either S or an R");
						        break;
						    default:
						}
					}
				}

				record R() {}
				record S() {}
				"""
			},
		 """
			R Only
			Either S or an R
			Either S or an R""");

		String expectedOutput =
				"""
			  // Method descriptor #22 (Ljava/lang/Object;)V
			  // Stack: 2, Locals: 3
			  public static void foo(java.lang.Object o);
			     0  aload_0 [o]
			     1  dup
			     2  invokestatic java.util.Objects.requireNonNull(java.lang.Object) : java.lang.Object [30]
			     5  pop
			     6  astore_1
			     7  iconst_0
			     8  istore_2
			     9  aload_1
			    10  iload_2
			    11  invokedynamic 0 typeSwitch(java.lang.Object, int) : int [36]
			    16  tableswitch default: 56
			          case 0: 40
			          case 1: 48
			    40  getstatic java.lang.System.out : java.io.PrintStream [40]
			    43  ldc <String "R Only"> [46]
			    45  invokevirtual java.io.PrintStream.println(java.lang.String) : void [48]
			    48  getstatic java.lang.System.out : java.io.PrintStream [40]
			    51  ldc <String "Either S or an R"> [54]
			    53  invokevirtual java.io.PrintStream.println(java.lang.String) : void [48]
			    56  return
			""";

		SwitchPatternTest.verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/773
	// [20][pattern switch] unnecessary code generated
	public void testIssue773_2() throws Exception {
		runConformTest(
			new String[] {
				"X.java",
				"""
				interface I {}

				record R (I i, I  j) {}

				class A implements I {}
				class B implements I {}

				public class X {

					static int swtch(Object o) {
						return switch (o) {
							case R(A a1, A a2) -> 1;
							case R(B b1, B b2) -> 2;
							case Object obj -> 3;
						};
					}
					public static void main(String argv[]) {
						Object o = new R(new A(), new A());
						System.out.print(swtch(o));
						o = new R(new B(), new B());
						System.out.print(swtch(o));
						o = new R(new I() {}, new I() {});
						System.out.println(swtch(o));
					}
				}
				"""
			},
		 "123");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/773
	// [20][pattern switch] unnecessary code generated
	public void testIssue773_3() throws Exception {
		runConformTest(
			new String[] {
				"X.java",
				"""
				interface I {}

				record R (I i, I  j) {}

				class A implements I {}
				class B implements I {}

				public class X {

					static int swtch(Object o) {
						return switch (o) {
							case R(A a1, A a2) when o == null -> 1;
							case R(B b1, B b2) when o == null -> 2;
							case Object obj -> 3;
						};
					}
					public static void main(String argv[]) {
						Object o = new R(new A(), new A());
						System.out.print(swtch(o));
						o = new R(new B(), new B());
						System.out.print(swtch(o));
						o = new R(new I() {}, new I() {});
						System.out.println(swtch(o));
					}
				}
				"""
			},
		 "333");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/773
	// [20][pattern switch] unnecessary code generated
	public void testIssue773_4() throws Exception {
		runConformTest(
			new String[] {
				"X.java",
				"""
				interface I {}

				record R (I i, I  j) {}

				class A implements I {}
				class B implements I {}

				public class X {

					static int swtch(Object o) {
						return switch (o) {
							case R(A a1, A a2) when o != null -> 1;
							case R(B b1, B b2) when o != null -> 2;
							case Object obj -> 3;
						};
					}
					public static void main(String argv[]) {
						Object o = new R(new A(), new A());
						System.out.print(swtch(o));
						o = new R(new B(), new B());
						System.out.print(swtch(o));
						o = new R(new I() {}, new I() {});
						System.out.println(swtch(o));
					}
				}
				"""
			},
		 "123");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/773
	// [20][pattern switch] unnecessary code generated
	public void testIssue773_5() throws Exception {
		runConformTest(
			new String[] {
				"X.java",
				"""
				interface I {}

				record R (I i, I  j) {}

				class A implements I {}
				class B implements I {}

				public class X {

					static int swtch(Object o) {
						return switch (o) {
							case R(A a1, A a2) when o != null -> 1;
							case R(B b1, B b2) when o == null -> 2;
							case Object obj -> 3;
						};
					}
					public static void main(String argv[]) {
						Object o = new R(new A(), new A());
						System.out.print(swtch(o));
						o = new R(new B(), new B());
						System.out.print(swtch(o));
						o = new R(new I() {}, new I() {});
						System.out.println(swtch(o));
					}
				}
				"""
			},
		 "133");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/773
	// [20][pattern switch] unnecessary code generated
	public void testIssue773_6() throws Exception {
		runConformTest(
			new String[] {
				"X.java",
				"""
				public class X {
				   record CoffeeBreak() {}
				       public int recharge(CoffeeBreak c) {
				           int energyLevel = 0;
				           switch (c) {
				               case CoffeeBreak( ) -> {
				                   energyLevel = 3;
				               }
				               default->{
				                   energyLevel = -3;
				               }
				           }
				           return energyLevel;
				       }
				       public static void main(String argv[]) {
				           X t = new X();
				           CoffeeBreak c = new CoffeeBreak();
				           if (t.recharge(c) == 3) {
				        	   System.out.println("OK!");
				           } else {
				        	   System.out.println("!OK!");
				           }
				       }
				}
				"""
			},
		 "OK!");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2053
	// ECJ rejects guarded pattern in switch as being dominated by prior cases
	public void testIssue2053() throws Exception {
		runConformTest(
			new String[] {
				"X.java",
				"""
				interface I {}

				record R (I i, I  j) {}

				class A implements I {}
				class B implements I {}

				public class X {

					static int swtch(Object o) {
						return switch (o) {
							case R(A a1, A a2) when true -> 1;
							case R(B b1, B b2) when o != null -> 2;
							case Object obj -> 3;
						};
					}
					public static void main(String argv[]) {
						Object o = new R(new A(), new A());
						System.out.print(swtch(o));
						o = new R(new B(), new B());
						System.out.print(swtch(o));
						o = new R(new I() {}, new I() {});
						System.out.println(swtch(o));
					}
				}
				"""
			},
		 "123");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2053
	// ECJ rejects guarded pattern in switch as being dominated by prior cases
	public void testIssue2053_2() throws Exception {
		runConformTest(
			new String[] {
				"X.java",
				"""
				interface I {}

				record R (I i, I  j) {}

				class A implements I {}
				class B implements I {}

				public class X {

					static int swtch(Object o) {
						return switch (o) {
							case R(A a1, A a2) when true -> 1;
							case R(B b1, B b2) when o == null -> 2;
							case Object obj -> 3;
						};
					}
					public static void main(String argv[]) {
						Object o = new R(new A(), new A());
						System.out.print(swtch(o));
						o = new R(new B(), new B());
						System.out.print(swtch(o));
						o = new R(new I() {}, new I() {});
						System.out.println(swtch(o));
					}
				}
				"""
			},
		 "133");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2077
	// [Patterns] Incorrect complaint about non-final variable reference from guard expression
	public void testIssue2077() throws Exception {
		runConformTest(
			new String[] {
				"X.java",
				"""
				public class X {
				    public static void main(String [] args) {
				        new X().foo("Hello ", 1);
				    }
				    public void foo(Object obj, int x) {
				        int y = 10;
				        y = 20;
				        y = 30;
				        switch (obj) {
				         case String s when switch (x) {
				                    case 1 -> { int y1 = 10; y1 = 30; yield y1!=20; }
				                    default -> { yield false; }
				                }
				                 -> {
				                    System.out.println(s + "OK");
				                    if (y == 0)
				                    	System.out.println(s + "OK");
				                 }

				         default -> {}
				        }
				    }
				}
				"""
			},
		 "Hello OK");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2077
	// [Patterns] Incorrect complaint about non-final variable reference from guard expression
	public void testIssue2077_2() throws Exception {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				public class X {
				    public static void main(String [] args) {
				        new X().foo("Hello ", 1);
				    }
				    public void foo(Object obj, int x) {
				        int y = 10;
				        y = 20;
				        y = 30;
				        switch (obj) {
				         case String s when switch (x) {
				                    case 1 -> { int y1 = 10; y1 = 30; yield 30 != y1; }
				                    default -> { yield false; }
				                } && y != 0
				                 -> {
				                    System.out.println(s + "OK");
				                    if (y == 0)
				                    	System.out.println(s + "OK");
				                 }

				         default -> {}
				        }
				    }
				}
				"""
			},
			"""
				----------
				1. ERROR in X.java (at line 13)\r
					} && y != 0\r
					     ^
				Local variable y referenced from a guard must be final or effectively final
				----------
				""");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2077
	// [Patterns] Incorrect complaint about non-final variable reference from guard expression
	public void testIssue2077_3() throws Exception {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				public class X {
				    public static void main(String [] args) {
				        new X().foo("Hello ", 1);
				    }
				    public void foo(Object obj, int x) {
				        int y = 10;
				        y = 20;
				        y = 30;
				        switch (obj) {
				         case String s when switch (x) {
				                    case 1 -> { int y1 = 10; y1 = 30; yield y != y1; }
				                    default -> { yield false; }
				                } && y != 0
				                 -> {
				                    System.out.println(s + "OK");
				                    if (y == 0)
				                    	System.out.println(s + "OK");
				                 }

				         default -> {}
				        }
				    }
				}
				"""
			},
			"""
				----------
				1. ERROR in X.java (at line 11)\r
					case 1 -> { int y1 = 10; y1 = 30; yield y != y1; }\r
					                                        ^
				Local variable y referenced from a guard must be final or effectively final
				----------
				""");
		    // We throw AbortMethod after first error, so second error doesn't surface
	}
}
