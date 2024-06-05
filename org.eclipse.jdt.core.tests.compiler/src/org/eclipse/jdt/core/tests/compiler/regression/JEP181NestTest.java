/*******************************************************************************
 * Copyright (c) 2018, 2021 IBM Corporation and others.
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
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.core.util.ClassFormatException;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "rawtypes" })
public class JEP181NestTest extends AbstractComparableTest {

	String versionString = null;

public JEP181NestTest(String name) {
	super(name);
}
// No need for a tearDown()
@Override
protected void setUp() throws Exception {
	super.setUp();
	this.versionString = AbstractCompilerTest.getVersionString(this.complianceLevel);
}

/*
 * Toggle compiler in mode -11
 */
// Static initializer to specify tests subset using TESTS_* static variables
// All specified tests which do not belong to the class are skipped...
static {
//	TESTS_NAMES = new String[] { "testBug572190" };
//	TESTS_NUMBERS = new int[] { 50, 51, 52, 53 };
//	TESTS_RANGE = new int[] { 34, 38 };
}
private static final String[] source_classic = JEP181NestTest.getTestSeriesClassic();

public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_11);
}
private static String[] getTestSeriesClassic() {
	return new String[] {
			"pack1/X.java",
			"""
				package pack1;
				public class X {
					public class Y {
						class Z {}
					}
					public static class A {
						public static class B {}
						public class C {}
					}
					public static void main(String[] args) {
						System.out.println("SUCCESS");
					}
					public void foo() {
						System.out.println("foo");
					}
				}
				""",
	};
}
private void verifyClassFile(String expectedOutput, String classFileName, int mode, boolean positive) throws IOException, ClassFormatException {
	String result = getClassFileContents(classFileName, mode);
	verifyOutput(result, expectedOutput, positive);
}
private String getClassFileContents( String classFileName, int mode) throws IOException,
ClassFormatException {
	File f = new File(OUTPUT_DIR + File.separator + classFileName);
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String result = disassembler.disassemble(classFileBytes, "\n", mode);
	return result;
}
private void verifyOutputPositive(String result, String expectedOutput) {
	verifyOutput(result, expectedOutput, true);
}
private void verifyOutputNegative(String result, String expectedOutput) {
	verifyOutput(result, expectedOutput, false);
}
private void verifyOutput(String result, String expectedOutput, boolean positive) {
	int index = result.indexOf(expectedOutput);
	if (positive) {
		if (index == -1 || expectedOutput.length() == 0) {
			System.out.println(Util.displayString(result, 3));
			System.out.println("...");
		}
		if (index == -1) {
			assertEquals("Wrong contents", expectedOutput, result);
		}
	} else {
		if (index != -1) {
			assertEquals("Unexpected contents", "", result);
		}
	}
}
private void verifyClassFile(String expectedOutput, String classFileName, int mode) throws IOException,
	ClassFormatException {
	verifyClassFile(expectedOutput, classFileName, mode, true);
}
private void verifyNegativeClassFile(String unExpectedOutput, String classFileName, int mode) throws IOException,
ClassFormatException {
	verifyClassFile(unExpectedOutput, classFileName, mode, false);
}
public void testBug535851_001() throws Exception {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_11);

	this.runConformTest(
			JEP181NestTest.source_classic,
			"SUCCESS",
			options
	);

	String expectedPartialOutput =
			"""
		Nest Members:
		   #37 pack1/X$A,
		   #44 pack1/X$A$B,
		   #46 pack1/X$A$C,
		   #40 pack1/X$Y,
		   #48 pack1/X$Y$Z
		""";
	verifyClassFile(expectedPartialOutput, "pack1/X.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug535851_002() throws Exception {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_11);

	this.runConformTest(
		JEP181NestTest.source_classic,
		"SUCCESS",
		options
	);

	String expectedPartialOutput =
		"Nest Host: #17 pack1/X\n";
	verifyClassFile(expectedPartialOutput, "pack1/X$A.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug535851_003() throws Exception {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_11);

	this.runConformTest(
		JEP181NestTest.source_classic,
		"SUCCESS",
		options
	);

	String unExpectedPartialOutput =
		"NestMembers:";
	verifyNegativeClassFile(unExpectedPartialOutput, "pack1/X$A.class", ClassFileBytesDisassembler.SYSTEM);
}
public void testBug535851_004() throws Exception {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_11);

	this.runConformTest(
			JEP181NestTest.source_classic,
			"SUCCESS",
			options
	);

	String expectedPartialOutput =
		"Nest Host: #24 pack1/X\n";
	verifyClassFile(expectedPartialOutput, "pack1/X$Y$Z.class", ClassFileBytesDisassembler.SYSTEM);
}
// vanilla anonymous declaration
public void testBug535851_005() throws Exception {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_11);
	String[] files = new String[] {
			"pack1/X.java",
			"""
				package pack1;
				public class X {
					public static void main(String[] args) {
						System.out.println("SUCCESS");
					}
					public void foo() {
						Y y = new Y() {
						    void bar() {}
						};
				       System.out.println(y.toString());
					}
				}
				abstract class Y {
					abstract void bar();
				}
				""",
	};
	this.runConformTest(
			files,
			"SUCCESS",
			options
	);

	String expectedPartialOutput =
		"Nest Members:\n" +
		"   #33 pack1/X$1\n";
	verifyClassFile(expectedPartialOutput, "pack1/X.class", ClassFileBytesDisassembler.SYSTEM);

	expectedPartialOutput = "Nest Host: #23 pack1/X\n";
	verifyClassFile(expectedPartialOutput, "pack1/X$1.class", ClassFileBytesDisassembler.SYSTEM);
	verifyNegativeClassFile(expectedPartialOutput, "pack1/Y.class", ClassFileBytesDisassembler.SYSTEM);
}
// anonymous declaration inside another anonymous declaration
public void testBug535851_006() throws Exception {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_11);
	String[] files = new String[] {
			"pack1/X.java",
			"""
				package pack1;
				public class X {
					public static void main(String[] args) {
						System.out.println("SUCCESS");
					}
					public void foo() {
						Y y = new Y() {
						    void bar() {
						        Y y1 = new Y() {
						           void bar() {}
						        };
				               System.out.println(y1.toString());
					        }
						};
				       System.out.println(y.toString());
					}
				}
				abstract class Y {
					abstract void bar();
				}
				""",
	};
	this.runConformTest(
			files,
			"SUCCESS",
			options
	);

	String expectedPartialOutput =
			"""
		Nest Members:
		   #33 pack1/X$1,
		   #48 pack1/X$1$1
		""";
	verifyClassFile(expectedPartialOutput, "pack1/X.class", ClassFileBytesDisassembler.SYSTEM);

	expectedPartialOutput = "Nest Host: #48 pack1/X\n";
	verifyClassFile(expectedPartialOutput, "pack1/X$1.class", ClassFileBytesDisassembler.SYSTEM);
	expectedPartialOutput = "Nest Host: #28 pack1/X\n";
	verifyClassFile(expectedPartialOutput, "pack1/X$1$1.class", ClassFileBytesDisassembler.SYSTEM);
	verifyNegativeClassFile(expectedPartialOutput, "pack1/Y.class", ClassFileBytesDisassembler.SYSTEM);
}

// lambda with anonymous inside anonymous
public void testBug535851_007() throws Exception {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_11);
	String[] files = new String[] {
			"pack1/X.java",
			"""
				package pack1;
				
				public class X {
					public static void main(String[] args) {
						System.out.println("SUCCESS");
					}
					public void foo() {
						I i = ()->{
							Y y = new Y() {\t
								@Override
								void bar() {
									Y y1 = new Y() {
										@Override
										void bar() {}
									};
									System.out.println(y1);
								}
							};
							System.out.println(y.toString());
						};
						i.apply();
					}
				}
				interface I {
					void apply();
				}
				abstract class Y {
					abstract void bar();
				}
				""",
	};
	this.runConformTest(
			files,
			"SUCCESS",
			options
	);

	String expectedPartialOutput =
			"""
		Nest Members:
		   #44 pack1/X$1,
		   #77 pack1/X$1$1
		""";
	verifyClassFile(expectedPartialOutput, "pack1/X.class", ClassFileBytesDisassembler.SYSTEM);

	expectedPartialOutput = "Nest Host: #42 pack1/X\n";
	verifyClassFile(expectedPartialOutput, "pack1/X$1.class", ClassFileBytesDisassembler.SYSTEM);
	expectedPartialOutput = "Nest Host: #28 pack1/X\n";
	verifyClassFile(expectedPartialOutput, "pack1/X$1$1.class", ClassFileBytesDisassembler.SYSTEM);
	verifyNegativeClassFile(expectedPartialOutput, "pack1/Y.class", ClassFileBytesDisassembler.SYSTEM);
}
// type declaration in method
public void testBug535851_008() throws Exception {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_11);
	String[] files = new String[] {
			"pack1/X.java",
			"""
				package pack1;
				
				public class X {
				   public void foo() {
				       class Y {
				           // nothing
				       }
				       Y y = new Y();
				       System.out.println("SUCCESS");
				   }
				   public static void main(String[] args) {
				       new X().foo();
				   }
				}
				""",
	};
	this.runConformTest(
			files,
			"SUCCESS",
			options
	);

	String expectedPartialOutput =
			"Nest Members:\n" +
			"   #15 pack1/X$1Y\n";
	verifyClassFile(expectedPartialOutput, "pack1/X.class", ClassFileBytesDisassembler.SYSTEM);

	expectedPartialOutput = "Nest Host: #22 pack1/X\n";
	verifyClassFile(expectedPartialOutput, "pack1/X$1Y.class", ClassFileBytesDisassembler.SYSTEM);
}
// testing the inner private instance field access from enclosing type
public void testBug535918_001a() throws Exception {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_11);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_11);

	this.runConformTest(
			new String[] {
					"pack1/X.java",
					"""
						package pack1;
						public class X {
							public static class Y {
								private int priv_int = 100;
								public int pub_int = 200;
							\t
							}
							public static void main(String[] args) {
								int sum = foo();
								System.out.println("SUCCESS:" + sum);
							}
							public static int foo() {
								Y y = new Y();
								int i = y.priv_int;
								int j = y.pub_int;
								return i + j;
							}
							public void bar() {
								System.out.println("bar");
							}
						}
						""",
			},
			"SUCCESS:300",
			options
	);

	String expectedPartialOutput =
		"getfield pack1.X$Y.priv_in";
	verifyClassFile(expectedPartialOutput, "pack1/X.class", ClassFileBytesDisassembler.SYSTEM);
	String unExpectedPartialOutput =
			"access$";
		verifyNegativeClassFile(unExpectedPartialOutput, "pack1/X$Y.class", ClassFileBytesDisassembler.SYSTEM);
}
//testing the inner private static field access from enclosing type
public void testBug535918_001b() throws Exception {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_11);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_11);

	this.runConformTest(
			new String[] {
					"pack1/X.java",
					"""
						package pack1;
						public class X {
							public static class Y {
								private static int priv_int = 100;
								public int pub_int = 200;
							\t
							}
							public static void main(String[] args) {
								int sum = foo();
								System.out.println("SUCCESS:" + sum);
							}
							public static int foo() {
								Y y = new Y();
								int i = y.priv_int;
								int j = y.pub_int;
								return i + j;
							}
							public void bar() {
								System.out.println("bar");
							}
						}
						""",
			},
			"SUCCESS:300",
			options
	);

	String expectedPartialOutput = "getstatic pack1.X$Y.priv_int";
	verifyClassFile(expectedPartialOutput, "pack1/X.class", ClassFileBytesDisassembler.SYSTEM);
	expectedPartialOutput =
			(this.complianceLevel < ClassFileConstants.JDK9 ?
					"Nest Members:\n" +
					"   #50 pack1/X$Y\n"
				:
					"Nest Members:\n" +
					"   #40 pack1/X$Y\n");
	verifyClassFile(expectedPartialOutput, "pack1/X.class", ClassFileBytesDisassembler.SYSTEM);
	expectedPartialOutput = "Nest Host: #25 pack1/X\n";
	verifyClassFile(expectedPartialOutput, "pack1/X$Y.class", ClassFileBytesDisassembler.SYSTEM);
	String unExpectedPartialOutput = "invokestatic pack1.X$Y.access$";
	verifyNegativeClassFile(unExpectedPartialOutput, "pack1/X.class", ClassFileBytesDisassembler.SYSTEM);
	unExpectedPartialOutput = "access$";
	verifyNegativeClassFile(unExpectedPartialOutput, "pack1/X$Y.class", ClassFileBytesDisassembler.SYSTEM);
}
//testing the nested private field access from enclosing type
public void testBug535918_001c() throws Exception {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_11);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_11);

	this.runConformTest(
			new String[] {
					"pack1/X.java",
					"""
						package pack1;
						public class X {
							public static class Y {
								private int priv_int = 100;
								public int pub_int = 200;
							\t
							}
							public static void main(String[] args) {
								int sum = foo();
								System.out.println("SUCCESS:" + sum);
							}
							public static int foo() {
								Y y = new Y();
								int i = y.priv_int;
								int j = y.pub_int;
								return i + j;
							}
							public void bar() {
								System.out.println("bar");
							}
						}
						""",
			},
			"SUCCESS:300",
			options
	);

	String unExpectedPartialOutput =
			"access$";
		verifyNegativeClassFile(unExpectedPartialOutput, "pack1/X$Y.class", ClassFileBytesDisassembler.SYSTEM);
}
//testing the nested private method access from same type (implicit nesting/nest host)
public void testBug535918_002() throws Exception {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_11);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_11);
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_11);

	this.runConformTest(
			new String[] {
					"pack1/X.java",
					"""
						package pack1;
						public class X {
							private int priv_non_static_method() {
								return 100;
							}
							public int pub_non_static_method() {
								return priv_non_static_method();
							}
							public static void main(String[] args) {
								X x = new X();
								int result =x.pub_non_static_method();
								System.out.println(result);
							}
						}
						""",
			},
			"100",
			options
	);

	String expectedPartialOutput =
		"invokevirtual pack1.X.priv_non_static_method()";
	verifyClassFile(expectedPartialOutput, "pack1/X.class", ClassFileBytesDisassembler.SYSTEM);
}

// sibling access: private static field
public void testBug535918_003a() throws Exception {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_11);
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_11);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_11);

	this.runConformTest(
			new String[] {
					"pack1/X.java",
					"""
						package pack1;
						public class X {
							public static class Y {
								private static int priv_int = 100;
							}
							public static class Z {
								public static int foo() {
									int i = Y.priv_int;
									return i;
								}
							}
							public static void main(String[] args) {
								int sum = Z.foo();
								System.out.println("SUCCESS:"+sum);
							}
						}
						""",
			},
			"SUCCESS:100",
			options
	);

	String XFile = getClassFileContents("pack1/X.class", ClassFileBytesDisassembler.SYSTEM);
	String XYFile = getClassFileContents("pack1/X$Y.class", ClassFileBytesDisassembler.SYSTEM);
	String XZFile = getClassFileContents("pack1/X$Z.class", ClassFileBytesDisassembler.SYSTEM);
	String partialOutput = (this.complianceLevel < ClassFileConstants.JDK9 ?
			"""
				Nest Members:
				   #55 pack1/X$Y,
				   #17 pack1/X$Z"""
		:
			"""
				Nest Members:
				   #59 pack1/X$Y,
				   #17 pack1/X$Z""");
	verifyOutputPositive(XFile, partialOutput);
	verifyOutputPositive(XYFile, "Nest Host: #22 pack1/X");
	verifyOutputPositive(XZFile, "Nest Host: #26 pack1/X");
	verifyOutputPositive(XZFile, "getstatic pack1.X$Y.priv_int");

	verifyOutputNegative(XYFile, "access$");
	verifyOutputNegative(XZFile, "invokestatic pack1.X$Y.access$0");
}
//sibling access: private instance field
public void testBug535918_003b() throws Exception {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_11);
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_11);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_11);

	this.runConformTest(
			new String[] {
					"pack1/X.java",
					"""
						package pack1;
						public class X {
							public static class Y {
								private int priv_int = 100;
							}
							public static class Z {
								public static int foo() {
									Y y = new Y();
									int i = y.priv_int;
									return i;
								}
							}
							public static void main(String[] args) {
								int sum = Z.foo();
								System.out.println("SUCCESS:"+sum);
							}
						}
						""",
			},
			"SUCCESS:100",
			options
	);

	String XFile = getClassFileContents("pack1/X.class", ClassFileBytesDisassembler.SYSTEM);
	String XYFile = getClassFileContents("pack1/X$Y.class", ClassFileBytesDisassembler.SYSTEM);
	String XZFile = getClassFileContents("pack1/X$Z.class", ClassFileBytesDisassembler.SYSTEM);
	String partialOutput = (this.complianceLevel < ClassFileConstants.JDK9 ?
			"""
				Nest Members:
				   #55 pack1/X$Y,
				   #17 pack1/X$Z"""
		:
			"""
				Nest Members:
				   #59 pack1/X$Y,
				   #17 pack1/X$Z""");
	verifyOutputPositive(XFile, partialOutput);
	verifyOutputPositive(XYFile, "Nest Host: #21 pack1/X");
	verifyOutputPositive(XZFile, "Nest Host: #29 pack1/X");
	verifyOutputPositive(XZFile, "getfield pack1.X$Y.priv_int");

	verifyOutputNegative(XYFile, "access$");
	verifyOutputNegative(XZFile, "invokestatic pack1.X$Y.access$0");
}
//sibling access: private instance field via Allocation Expression Field reference
// note: internally this follows a different code path
public void testBug535918_003c() throws Exception {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_11);
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_11);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_11);

	this.runConformTest(
			new String[] {
					"pack1/X.java",
					"""
						package pack1;
						public class X {
							public static class Y {
								private int priv_int = 100;
							}
							public static class Z {
								public static int foo() {
									int i = new Y().priv_int;
									return i;
								}
							}
							public static void main(String[] args) {
								int sum = Z.foo();
								System.out.println("SUCCESS:"+sum);
							}
						}
						""",
			},
			"SUCCESS:100",
			options
	);

	String XFile = getClassFileContents("pack1/X.class", ClassFileBytesDisassembler.SYSTEM);
	String XYFile = getClassFileContents("pack1/X$Y.class", ClassFileBytesDisassembler.SYSTEM);
	String XZFile = getClassFileContents("pack1/X$Z.class", ClassFileBytesDisassembler.SYSTEM);
	String partialOutput = (this.complianceLevel < ClassFileConstants.JDK9 ?
			"""
				Nest Members:
				   #55 pack1/X$Y,
				   #17 pack1/X$Z"""
		:
			"""
				Nest Members:
				   #59 pack1/X$Y,
				   #17 pack1/X$Z""");
	verifyOutputPositive(XFile, partialOutput);
	verifyOutputPositive(XYFile, "Nest Host: #21 pack1/X");
	verifyOutputPositive(XZFile, "Nest Host: #27 pack1/X");
	verifyOutputPositive(XZFile, "getfield pack1.X$Y.priv_int");

	verifyOutputNegative(XYFile, "access$");
	verifyOutputNegative(XZFile, "invokestatic pack1.X$Y.access$0");
}
//sibling and super: private static field access of a super-type is accessed from a sub-type with
//both super-type and sub-type being nestmates.
public void testBug535918_003d() throws Exception {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_11);
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_11);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_11);

	this.runConformTest(
			new String[] {
					"pack1/X.java",
					"""
						package pack1;
						public class X {
							public static class Y {
								private static int priv_int = 100;
							}
							public static class Z extends Y {
								public static int foo() {
									int i = Y.priv_int;
									return i;
								}
							}
							public static void main(String[] args) {
								int sum = Z.foo();
								System.out.println("SUCCESS:"+sum);
							}
						}
						""",
			},
			"SUCCESS:100",
			options
	);

	String XFile = getClassFileContents("pack1/X.class", ClassFileBytesDisassembler.SYSTEM);
	String XYFile = getClassFileContents("pack1/X$Y.class", ClassFileBytesDisassembler.SYSTEM);
	String XZFile = getClassFileContents("pack1/X$Z.class", ClassFileBytesDisassembler.SYSTEM);
	String partialOutput = (this.complianceLevel < ClassFileConstants.JDK9 ?
			"""
				Nest Members:
				   #55 pack1/X$Y,
				   #17 pack1/X$Z"""
		:
			"""
				Nest Members:
				   #59 pack1/X$Y,
				   #17 pack1/X$Z""");
	verifyOutputPositive(XFile, partialOutput);
	verifyOutputPositive(XYFile, "Nest Host: #22 pack1/X");
	verifyOutputPositive(XZFile, "Nest Host: #24 pack1/X");
	verifyOutputPositive(XZFile, "getstatic pack1.X$Y.priv_int");

	verifyOutputNegative(XYFile, "access$");
	verifyOutputNegative(XZFile, "invokestatic pack1.X$Y.access$0");
}
//sibling and super: private instance field of a super-type is accessed from a sub-type with
//both super-type and sub-type being nestmates.
public void testBug535918_003e() throws Exception {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_11);
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_11);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_11);

	this.runConformTest(
			new String[] {
					"pack1/X.java",
					"""
						package pack1;
						public class X {
							public static class Y{
								private int priv_int = 100;
							}
							public static class Z extends Y {
								public static int foo() {
									Y y = new Y();
									int i = y.priv_int;
									return i;
								}
							}
							public static void main(String[] args) {
								int sum = Z.foo();
								System.out.println("SUCCESS:"+sum);
							}
						}
						""",
			},
			"SUCCESS:100",
			options
	);

	String XFile = getClassFileContents("pack1/X.class", ClassFileBytesDisassembler.SYSTEM);
	String XYFile = getClassFileContents("pack1/X$Y.class", ClassFileBytesDisassembler.SYSTEM);
	String XZFile = getClassFileContents("pack1/X$Z.class", ClassFileBytesDisassembler.SYSTEM);
	String partialOutput = (this.complianceLevel < ClassFileConstants.JDK9 ?
			"""
				Nest Members:
				   #55 pack1/X$Y,
				   #17 pack1/X$Z"""
		:
			"""
				Nest Members:
				   #59 pack1/X$Y,
				   #17 pack1/X$Z""");
	verifyOutputPositive(XFile, partialOutput);
	verifyOutputPositive(XYFile, "Nest Host: #21 pack1/X");
	verifyOutputPositive(XZFile, "Nest Host: #26 pack1/X");
	verifyOutputPositive(XZFile, "getfield pack1.X$Y.priv_int");

	verifyOutputNegative(XYFile, "access$");
	verifyOutputNegative(XZFile, "invokestatic pack1.X$Y.access$0");
}
//sibling and super with super keyword: private instance field of a super-type is accessed from a sub-type
// user keyword super with both super-type and sub-type being nestmates.
public void testBug535918_003f() throws Exception {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_11);
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_11);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_11);

	this.runConformTest(
			new String[] {
					"pack1/X.java",
					"""
						package pack1;
						public class X {
							public static class Y{
								private int priv_int = 100;
							}
							public static class Z extends Y {
								public int foo() {
									int i = super.priv_int;
									return i;
								}
							}
							public static void main(String[] args) {
								int sum = new Z().foo();
								System.out.println("SUCCESS:"+sum);
							}
						}
						""",
			},
			"SUCCESS:100",
			options
	);

	String XFile = getClassFileContents("pack1/X.class", ClassFileBytesDisassembler.SYSTEM);
	String XYFile = getClassFileContents("pack1/X$Y.class", ClassFileBytesDisassembler.SYSTEM);
	String XZFile = getClassFileContents("pack1/X$Z.class", ClassFileBytesDisassembler.SYSTEM);
	String partialOutput = (this.complianceLevel < ClassFileConstants.JDK9 ?
			"""
				Nest Members:
				   #55 pack1/X$Y,
				   #17 pack1/X$Z"""
		:
			"""
				Nest Members:
				   #60 pack1/X$Y,
				   #16 pack1/X$Z""");
	verifyOutputPositive(XFile, partialOutput);
	verifyOutputPositive(XYFile, "Nest Host: #21 pack1/X");
	verifyOutputPositive(XZFile, "Nest Host: #24 pack1/X");
	verifyOutputPositive(XZFile, "getfield pack1.X$Y.priv_int");

	verifyOutputNegative(XYFile, "access$");
	verifyOutputNegative(XZFile, "invokestatic pack1.X$Y.access$0");
}
//vanilla field access of enclosing type: private static field of enclosing type accessed in inner type
public void testBug535918_004a() throws Exception {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_11);
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_11);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_11);

	this.runConformTest(
			new String[] {
					"pack1/X.java",
					"""
						package pack1;
						public class X {
							private static int priv_int = 100;
							public static class Y {
								public static int foo() {
									return priv_int;
								}
							}
							public static void main(String[] args) {
								int sum = Y.foo();
								System.out.println("SUCCESS:"+sum);
							}
						}
						""",
			},
			"SUCCESS:100",
			options
	);

	String XFile = getClassFileContents("pack1/X.class", ClassFileBytesDisassembler.SYSTEM);
	String XYFile = getClassFileContents("pack1/X$Y.class", ClassFileBytesDisassembler.SYSTEM);
	String partialOutput = "Nest Members:\n" +
			"   #22 pack1/X$Y\n";
	verifyOutputPositive(XFile, partialOutput);
	verifyOutputPositive(XYFile, "Nest Host: #17 pack1/X");
	verifyOutputPositive(XYFile, "getstatic pack1.X.priv_int");

	verifyOutputNegative(XFile, "access$");
	verifyOutputNegative(XFile, "invokestatic pack1.X.access$0()");

}
//vanilla field access of enclosing type: private instance field of enclosing type accessed in inner type
public void testBug535918_004b() throws Exception {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_11);
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_11);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_11);

	this.runConformTest(
			new String[] {
					"pack1/X.java",
					"""
						package pack1;
						public class X {
							private int priv_int = 100;
							public class Y {
								public int foo() {
									return priv_int;
								}
							}
							public static void main(String[] args) {
								int sum = new X().new Y().foo();
								System.out.println("SUCCESS:"+sum);
							}
						}
						""",
			},
			"SUCCESS:100",
			options
	);

	String XFile = getClassFileContents("pack1/X.class", ClassFileBytesDisassembler.SYSTEM);
	String XYFile = getClassFileContents("pack1/X$Y.class", ClassFileBytesDisassembler.SYSTEM);
	String partialOutput = "Nest Members:\n" +
			"   #20 pack1/X$Y\n";
	verifyOutputPositive(XFile, partialOutput);
	verifyOutputPositive(XYFile, "Nest Host: #22 pack1/X");
	verifyOutputPositive(XYFile, "getfield pack1.X.priv_int");

	verifyOutputNegative(XFile, "access$");
	verifyOutputNegative(XYFile, "invokestatic pack1.X.access$0()");

}
//nestmate inner constructor call from outer - no synthetic and appropriate call site params
public void testBug535918_005a() throws Exception {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_11);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_11);

	this.runConformTest(
			new String[] {
					"pack1/X.java",
					"""
						package pack1;
						public class X {
							class Y {
								class Z {
									private Z() {
									}
								}
								Z d;
								private Y() {
									this.d = new Z();
								}
							}
							@Override
							public String toString() {
								return "SUCCESS";
							}
						  public static void main(String[] argv) {
						    System.out.println(new X());
						  }
						}
						""",
			},
			"SUCCESS",
			options
	);

	String XFile = getClassFileContents("pack1/X.class", ClassFileBytesDisassembler.SYSTEM);
	String XYFile = getClassFileContents("pack1/X$Y.class", ClassFileBytesDisassembler.SYSTEM);
	String XYZFile = getClassFileContents("pack1/X$Y$Z.class", ClassFileBytesDisassembler.SYSTEM);
	String partialOutput = """
		Nest Members:
		   #38 pack1/X$Y,
		   #42 pack1/X$Y$Z""";
	verifyOutputPositive(XFile, partialOutput);
	verifyOutputPositive(XYFile, "Nest Host: #31 pack1/X");
	verifyOutputPositive(XYZFile, "Nest Host: #24 pack1/X");
	verifyOutputPositive(XYFile, "invokespecial pack1.X$Y$Z(pack1.X$Y)"); //only one param

	verifyOutputNegative(XYZFile, "synthetic X$Y$Z");
	verifyOutputNegative(XYFile, "invokespecial pack1.X$Y$Z(pack1.X$Y, pack1.X$Y$Z)");
}
//nestmate sibling constructor call - no synthetic and appropriate call site params
public void testBug535918_005b() throws Exception {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_11);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_11);

	this.runConformTest(
			new String[] {
					"pack1/X.java",
					"""
						package pack1;
						public class X {
							class Z {
								private Z() {
								}
							}
							class Y {
								Z d;
								private Y() {
									this.d = new Z();
								}
							}
							@Override
							public String toString() {
								return "SUCCESS";
							}
						  public static void main(String[] argv) {
						    System.out.println(new X());
						  }
						}
						""",
			},
			"SUCCESS",
			options
	);

	String XFile = getClassFileContents("pack1/X.class", ClassFileBytesDisassembler.SYSTEM);
	String XYFile = getClassFileContents("pack1/X$Y.class", ClassFileBytesDisassembler.SYSTEM);
	String XZFile = getClassFileContents("pack1/X$Z.class", ClassFileBytesDisassembler.SYSTEM);
	String partialOutput = """
		Nest Members:
		   #38 pack1/X$Y,
		   #41 pack1/X$Z""";
	verifyOutputPositive(XFile, partialOutput);
	verifyOutputPositive(XYFile, "Nest Host: #30 pack1/X");
	verifyOutputPositive(XZFile, "Nest Host: #22 pack1/X");
	verifyOutputPositive(XYFile, "invokespecial pack1.X$Z(pack1.X)"); //only one param

	verifyOutputNegative(XZFile, "synthetic X$Z");
	verifyOutputNegative(XYFile, "invokespecial pack1.X$Z(pack1.X$Y, pack1.X$Z)");
}
//nestmate outer constructor call from inner - no synthetic and appropriate call site params
public void testBug535918_005c() throws Exception {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_11);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_11);

	this.runConformTest(
			new String[] {
					"pack1/X.java",
					"""
						package pack1;
						public class X {
							class Y {
								private Y() {
								}
								class Z {
									Y y;
									private Z() {
										this.y = new Y();
									}
								}
							}
							@Override
							public String toString() {
								return "SUCCESS";
							}
						  public static void main(String[] argv) {
						    System.out.println(new X());
						  }
						}
						""",
			},
			"SUCCESS",
			options
	);

	String XFile = getClassFileContents("pack1/X.class", ClassFileBytesDisassembler.SYSTEM);
	String XYFile = getClassFileContents("pack1/X$Y.class", ClassFileBytesDisassembler.SYSTEM);
	String XYZFile = getClassFileContents("pack1/X$Y$Z.class", ClassFileBytesDisassembler.SYSTEM);
	String partialOutput = """
		Nest Members:
		   #38 pack1/X$Y,
		   #42 pack1/X$Y$Z""";
	verifyOutputPositive(XFile, partialOutput);
	verifyOutputPositive(XYFile, "Nest Host: #24 pack1/X");
	verifyOutputPositive(XYZFile, "Nest Host: #34 pack1/X");
	verifyOutputPositive(XYZFile, "invokespecial pack1.X$Y(pack1.X)"); //only one param

	verifyOutputNegative(XYFile, "synthetic X$Y");
	verifyOutputNegative(XYZFile, "invokespecial pack1.X$Y(pack1.X, pack1.X$Y)");
}
//nestmate super call to private constructor from sibling nestmate which is a subtype - no synthetic and appropriate call site params
public void testBug535918_005d() throws Exception {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_11);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_11);

	this.runConformTest(
			new String[] {
					"pack1/X.java",
					"""
						package pack1;
						public class X {
							  private class Y {
							    private Y() {
							      super();
							    }
							  }
							  private class Z extends Y {
							    private Z() {
							      super();
							    }
							  }
						  public static void main(String[] argv) {
								  System.out.println("SUCCESS");
							  }
						}
						""",
			},
			"SUCCESS",
			options
	);

	String XFile = getClassFileContents("pack1/X.class", ClassFileBytesDisassembler.SYSTEM);
	String XYFile = getClassFileContents("pack1/X$Y.class", ClassFileBytesDisassembler.SYSTEM);
	String XZFile = getClassFileContents("pack1/X$Z.class", ClassFileBytesDisassembler.SYSTEM);
	String partialOutput = """
		Nest Members:
		   #35 pack1/X$Y,
		   #38 pack1/X$Z""";
	verifyOutputPositive(XFile, partialOutput);
	verifyOutputPositive(XYFile, "Nest Host: #22 pack1/X");
	verifyOutputPositive(XZFile, "Nest Host: #21 pack1/X");
	verifyOutputPositive(XZFile, "invokespecial pack1.X$Y(pack1.X)"); //only one param

	verifyOutputNegative(XYFile, "synthetic X$Y");
	verifyOutputNegative(XZFile, "invokespecial pack1.X$Y(pack1.X, pack1.X$Y)");
}
// nestmate super call to private constructor from sibling nestmate which is a subtype
// super is a parameterized type
public void testBug535918_005e() throws Exception {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_11);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_11);

	this.runConformTest(
			new String[] {
					"pack1/X.java",
					"""
						package pack1;
						public class X {
						  private static class Y<T> implements AutoCloseable {
						    private Y() {
						      super();
						    }
						    public void close() {
						    }
						  }
						  @SuppressWarnings("unused")
						private static class Z extends Y<Object> {
						    private Z() {
						      super();
						    }
						  }
						  public static void main(String[] args) {
							  System.out.println("SUCCESS");
						  }
						}
						""",
			},
			"SUCCESS",
			options
	);

	String XFile = getClassFileContents("pack1/X.class", ClassFileBytesDisassembler.SYSTEM);
	String XYFile = getClassFileContents("pack1/X$Y.class", ClassFileBytesDisassembler.SYSTEM);
	String XZFile = getClassFileContents("pack1/X$Z.class", ClassFileBytesDisassembler.SYSTEM);
	String partialOutput = """
		Nest Members:
		   #35 pack1/X$Y,
		   #38 pack1/X$Z""";
	verifyOutputPositive(XFile, partialOutput);
	verifyOutputPositive(XYFile, "Nest Host: #24 pack1/X");
	verifyOutputPositive(XZFile, "Nest Host: #19 pack1/X");
	verifyOutputPositive(XZFile, "invokespecial pack1.X$Y()"); //only one param

	verifyOutputNegative(XYFile, "synthetic pack1.X$Y(pack1.X.Y arg0)");
	verifyOutputNegative(XZFile, "2  invokespecial pack1.X$Y(pack1.X$Y)");
}
//nestmate constructor reference
public void testBug535918_005f() throws Exception {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_11);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_11);

	this.runConformTest(
			new String[] {
					"X.java",
					"""
						interface I {
						  X makeX(int x);
						}
						public class X {
						  void foo() {
						    class Y {
						    	void f() {
						    		I i = X::new;
						    		i.makeX(123456);
						    	}
						    }
						    new Y().f();
						  }
						  private X(int x) {
						    super();
						    System.out.println("SUCCESS");
						  }
						  X() {
						    super();
						  }
						  public static void main(String[] args) {
						    new X().foo();
						  }
						}
						""",
			},
			"SUCCESS",
			options
	);

	String XFile = getClassFileContents("X.class", ClassFileBytesDisassembler.SYSTEM);
	String X1YFile = getClassFileContents("X$1Y.class", ClassFileBytesDisassembler.SYSTEM);
	String partialOutput = "Nest Members:\n" +
			"   #8 X$1Y\n";
	verifyOutputPositive(XFile, partialOutput);
	verifyOutputPositive(X1YFile, "Nest Host: #33 X");

	verifyOutputNegative(X1YFile, "synthetic X$Y(pack1.X.Y arg0)");
}
//testing the nested private method access from enclosing type
public void testBug535918_005g() throws Exception {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_11);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_11);

	this.runConformTest(
			new String[] {
					"pack1/X.java",
					"""
						package pack1;
						public class X {
							public static class Y {
								private int priv_instance_method() {
									return 100;
								}
								public int pub_instance_method() {
									int pri = priv_instance_method();
									return 200 + pri;
								}
							}
							public static void main(String[] args) {
								int sum = foo();
								System.out.println("SUCCESS:"+sum);
							}
							public static int foo() {
								Y y = new Y();
								int i = y.priv_instance_method();
								int j = y.pub_instance_method();
								return i + j;
							}
							public void bar() {
								System.out.println("bar");
							}
						}
						""",
			},
			"SUCCESS:400",
			options
	);

	String expectedPartialOutput =
		"invokevirtual pack1.X$Y.priv_instance_method()";
	verifyClassFile(expectedPartialOutput, "pack1/X.class", ClassFileBytesDisassembler.SYSTEM);
}
//negative testing the nested private method access from enclosing type is not via invokespecial
public void testBug535918_005h() throws Exception {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_11);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_11);

	this.runConformTest(
			new String[] {
					"pack1/X.java",
					"""
						package pack1;
						public class X {
							public static class Y {
								private int priv_instance_method() {
									return 100;
								}
								public int pub_instance_method() {
									int pri = priv_instance_method();
									return 200 + pri;
								}
							}
							public static void main(String[] args) {
								int sum = foo();
								System.out.println("SUCCESS:"+sum);
							}
							public static int foo() {
								Y y = new Y();
								int i = y.priv_instance_method();
								int j = y.pub_instance_method();
								return i + j;
							}
							public void bar() {
								System.out.println("bar");
							}
						}
						""",
			},
			"SUCCESS:400",
			options
	);

	String unExpectedPartialOutput =
		"invokespecial pack1.X$Y.priv_instance_method()";
	verifyNegativeClassFile(unExpectedPartialOutput, "pack1/X.class", ClassFileBytesDisassembler.SYSTEM);
}
//negative testing the synthetic method - access - not present in nested class
public void testBug535918_005i() throws Exception {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_11);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_11);

	this.runConformTest(
			new String[] {
					"pack1/X.java",
					"""
						package pack1;
						public class X {
							public static class Y {
								private int priv_instance_method() {
									return 100;
								}
								public int pub_instance_method() {
									int pri = priv_instance_method();
									return 200 + pri;
								}
							}
							public static void main(String[] args) {
								int sum = foo();
								System.out.println("SUCCESS:"+sum);
							}
							public static int foo() {
								Y y = new Y();
								int i = y.priv_instance_method();
								int j = y.pub_instance_method();
								return i + j;
							}
							public void bar() {
								System.out.println("bar");
							}
						}
						""",
			},
			"SUCCESS:400",
			options
	);

	String unExpectedPartialOutput =
			"access$";
		verifyNegativeClassFile(unExpectedPartialOutput, "pack1/X$Y.class", ClassFileBytesDisassembler.SYSTEM);
}
//private interface method invoked from a method inside the same interface should be invokeinterface
public void testBug535918_005j() throws Exception {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_11);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_11);

	this.runConformTest(
			new String[] {
					"pack1/X.java",
					"""
						package pack1;
						interface I {
							private void foo() {}
							public default void apply() {
								foo();
							}
						}
						public class X {
							public static void main(String[] args) {
							}
						}
						""",
			},
			"",
			options
	);

	String IFile = getClassFileContents("pack1/I.class", ClassFileBytesDisassembler.SYSTEM);
	String partialOutput = "invokeinterface pack1.I.foo";
	verifyOutputPositive(IFile, partialOutput);
}
//private interface method invoked from a nestmate interface should be invokeinterface
public void testBug535918_005k() throws Exception {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_11);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_11);

	this.runConformTest(
			new String[] {
					"pack1/X.java",
					"""
						package pack1;
						interface I {\
							private void foo() {}\
							\
							interface J {\
								public default void apply() {\
									I i = new X();\
									i.foo();\
								}	\
							}\
						}\
						public class X implements I{
						}
						""",
			},
			"",
			options
	);

	String IFile = getClassFileContents("pack1/I$J.class", ClassFileBytesDisassembler.SYSTEM);
	String partialOutput = "invokeinterface pack1.I.foo";
	verifyOutputPositive(IFile, partialOutput);
}
//test for SyntheticMethodBinding.SuperField*Access
public void testBug535918_0056a() throws Exception {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_11);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_11);

	this.runConformTest(
			new String[] {
					"pack1/X.java",
					"""
						package pack1;
						public class X {
							private int priv_int;
						
							class Y extends X {
								class Z extends Y {
									public void foo() {
										X.Y.super.priv_int = 0;
									}
								}
							}
						
							public static void main(String[] args) {
								new X().new Y().new Z().foo();
							}
						}
						""",
			},
			"",
			options
	);

	String XFile = getClassFileContents("pack1/X.class", ClassFileBytesDisassembler.SYSTEM);
	String XYFile = getClassFileContents("pack1/X$Y.class", ClassFileBytesDisassembler.SYSTEM);
	String XYZFile = getClassFileContents("pack1/X$Y$Z.class", ClassFileBytesDisassembler.SYSTEM);
	String partialOutput = """
		Nest Members:
		   #20 pack1/X$Y,
		   #18 pack1/X$Y$Z""";
	verifyOutputPositive(XFile, partialOutput);
	verifyOutputPositive(XYFile, "Nest Host: #3 pack1/X");
	verifyOutputPositive(XYZFile, "Nest Host: #22 pack1/X");
	verifyOutputPositive(XYZFile, "putfield pack1.X.priv_int"); //direct access

	verifyOutputNegative(XYFile, "synthetic X$Y");
	verifyOutputNegative(XYZFile, "invokestatic X.access$0(X, int)");
}

public void testBug545387_01() throws Exception {
	Map<String, String> options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_11);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_11);

	this.runConformTest(
			new String[] {
					"pack1/X.java",
					"""
						package pack1;
						public class X {
							public class Inner1 {
								private void foo() {
									System.out.println("hello");;
								}
							}
							public class Sub1 extends Inner1 {
								public class Sub2 {
									void testFoo() {
										Sub1.super.foo();
									}
								}
								void testFoo() {
									(new Sub2()).testFoo();
								}
							}
							public static void main(String[] args) {
								Sub1 s1 = getS1();
								s1.testFoo();
							}
							public static Sub1 getS1() {
								return new X().new Sub1();
							}
						}
						""",
			},
			"hello",
			options
	);

	String XSub1Sub2 = getClassFileContents("pack1/X$Sub1$Sub2.class", ClassFileBytesDisassembler.SYSTEM);
	verifyOutputPositive(XSub1Sub2, "Nest Host: #29 pack1/X");
}

public void testBug572190_01() throws Exception {

	this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {
					       public void foo() {
					               new Thread(() -> {
					                       new Object() {
					                       };
					               });
					       }
					       public static void main(String[] args) {
					               System.out.println(0);
					       }
					}""",
			},
			"0"
	);

	String XFile = getClassFileContents("X.class", ClassFileBytesDisassembler.SYSTEM);
	String expected = """
		Nest Members:
		   #41 X$1
		Bootstrap methods:
		""" ;
	String unexpectedOutput = """
		Nest Members:
		   #41 X$1,
		   #68 X$2
		Bootstrap methods:
		""";
	verifyOutputPositive(XFile, expected);

	verifyOutputNegative(XFile, unexpectedOutput);
}

public void testBug572190_02() throws Exception {

	this.runConformTest(
			new String[] {
				"pack1/X.java",
				"""
					package pack1;
					
					import pack1.XB.EF;
					
					public class X {
					       private static int foo() {
					               return  EF.values().length;
					       }
					    public static void main(String argv[])   {
					       System.out.println(X.foo());
					    }
					    public enum ch { }
					}
					""",
				"pack1/XA.java",
				"""
					package pack1;
					public class XA {
					    public enum EC {}
					}""",
				"pack1/XB.java",
				"""
					package pack1;
					public class XB {
					    protected enum EF {}
					}""",
			},
			"0"
	);

	String XFile = getClassFileContents("pack1/XB.class", ClassFileBytesDisassembler.SYSTEM);
	String expected = """
		Nest Members:
		   #17 pack1/XB$EF
		}""";
	verifyOutputPositive(XFile, expected);

}

public static Class testClass() {
	return JEP181NestTest.class;
}
}
