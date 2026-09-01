/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;
import java.util.Map;
import junit.framework.Test;
import org.eclipse.jdt.core.tests.util.PreviewTest;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@PreviewTest
public class ValueClassesAndObjectsTest extends AbstractRegressionTestCommon {
	static {
//		TESTS_NUMBERS = new int [] { 1 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] { "testIssue3536" };
	}
	public static Class<?> testClass() {
		return ValueClassesAndObjectsTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_28);
	}
	public ValueClassesAndObjectsTest(String testName) {
		super(testName);
	}

	private static final JavacTestOptions JAVAC_OPTIONS = new JavacTestOptions("--enable-preview -source 28");
	private static final String[] VMARGS = new String[] {"--enable-preview"};

	// Enables the tests to run individually
	protected Map<String, String> getCompilerOptions(boolean preview) {
		Map<String, String> defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_28);
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_28);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_28);
		defaultOptions.put(CompilerOptions.OPTION_EnablePreviews, preview ? CompilerOptions.ENABLED : CompilerOptions.DISABLED);
		defaultOptions.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.WARNING);
		return defaultOptions;
	}

	protected void runNegativeTest(String[] testFiles, String expectedCompilerLog) {
		Map<String, String> customOptions = getCompilerOptions(true);
		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedCompilerLog = expectedCompilerLog;
		runner.javacTestOptions = JAVAC_OPTIONS;
		runner.customOptions = customOptions;
		runner.expectedJavacOutputString = null;
		runner.runNegativeTest();
	}

	@Override
	protected void runConformTest(String[] testFiles, String expectedOutput) {
		runConformTest(testFiles, expectedOutput, getCompilerOptions(true), VMARGS, JAVAC_OPTIONS);
	}

	protected void runWarningTest(String[] testFiles, String expectedCompilerLog, Map<String, String> customOptions) {
		if (!isJRE16Plus)
			return;
		Runner runner = new Runner();
		runner.testFiles = testFiles;
		runner.expectedCompilerLog = expectedCompilerLog;
		runner.customOptions = customOptions;
		runner.javacTestOptions = JavacTestOptions.forReleaseWithPreview("28");
		runner.vmArguments = VMARGS;
		runner.runWarningTest();
	}

	// ========= OPT-IN to run.javac mode: ===========
	@Override
	protected void setUp() throws Exception {
		this.runJavacOptIn = true;
		super.setUp();
	}
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		this.runJavacOptIn = false; // do it last, so super can still clean up
	}

	@Override
	protected JavacTestOptions getJavacTestOptions() {
		return JAVAC_OPTIONS;
	}

	// =================================================
	// https://cr.openjdk.org/~dlsmith/jep401/latest/
    public void testValueTypes_001() {
		runNegativeTest(new String[] {
			"X.java",
				"""
				 public class X {
				    public static void main(String[] args) {
						System.out.println("");
					}
				}
				class value {}
			"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 6)\n" +
			"	class value {}\n" +
			"	      ^^^^^\n" +
			"\'value\' is not a valid type name; it is a restricted identifier and not allowed as a type identifier in Java 28\n" +
			"----------\n");
	}

    public void testValueTypes_002() {
		runConformTest(new String[] {
			"X.java",
				"""
				 public value class X {
				    public static void main(String[] args) {
				    	System.out.println("Ok!");
					}
				}
			"""
			},
			"Ok!");
	}

    // Snippet from https://openjdk.org/jeps/401 illustrating or lack/presence of identity - with preview turned on for compiler and runtime
    public void testValueness() {
 		runConformTest(new String[] {
 			"X.java",
 			"""
			import java.time.LocalDate;
			import java.util.Objects;

			public class X {
			  public static void main(String[] args){
				  Integer x = 1996, y = 1996;
				  System.out.println(x == y);
				  System.out.println(Objects.hasIdentity(x));

				  LocalDate d1 = LocalDate.of(1996, 1, 23);
				  System.out.println(d1);
				  LocalDate d2 = d1.plusYears(30);
				  System.out.println(d2);
				  LocalDate d3 = d2.minusYears(30);
				  System.out.println(d3);
				  System.out.println(d1 == d3);
				  System.out.println(Objects.hasIdentity(d1));


				  String s = "abcd";
				  System.out.println(Objects.hasIdentity(s));
				  String t = "aabcd".substring(1);
				  System.out.println(s == t);
				  System.out.println(s.equals(t));
			  }
			}
 			"""
 			},
			"true\n" +
			"false\n" +
			"1996-01-23\n" +
			"2026-01-23\n" +
			"1996-01-23\n" +
			"true\n" +
			"false\n" +
			"true\n" +
			"false\n" +
			"true");
 	}

    // Same snippet as above but with preview turned off for both compiler and runtime
    public void testIdentityfulness() {
 		runConformTest(new String[] {
 			"X.java",
 			"""
			import java.time.LocalDate;
			import java.util.Objects;

			public class X {
			  public static void main(String[] args){
				  Integer x = 1996, y = 1996;
				  System.out.println(x == y);
				  System.out.println(Objects.hasIdentity(x));

				  LocalDate d1 = LocalDate.of(1996, 1, 23);
				  System.out.println(d1);
				  LocalDate d2 = d1.plusYears(30);
				  System.out.println(d2);
				  LocalDate d3 = d2.minusYears(30);
				  System.out.println(d3);
				  System.out.println(d1 == d3);
				  System.out.println(Objects.hasIdentity(d1));


				  String s = "abcd";
				  System.out.println(Objects.hasIdentity(s));
				  String t = "aabcd".substring(1);
				  System.out.println(s == t);
				  System.out.println(s.equals(t));
			  }
			}
 			"""
 			},
			"false\n" +
			"true\n" +
			"1996-01-23\n" +
			"2026-01-23\n" +
			"1996-01-23\n" +
			"false\n" +
			"true\n" +
			"true\n" +
			"false\n" +
			"true",
 			getCompilerOptions(false), new String[] {}, new JavacTestOptions("-source 28"));
 	}

    // Same snippet as above but with preview turned off for compiler and turned on for runtime
    public void testValueness_without_compiler_preview_with_runtime_preview() {
 		runConformTest(new String[] {
 			"X.java",
 			"""
			import java.time.LocalDate;
			import java.util.Objects;

			public class X {
			  public static void main(String[] args){
				  Integer x = 1996, y = 1996;
				  System.out.println(x == y);
				  System.out.println(Objects.hasIdentity(x));

				  LocalDate d1 = LocalDate.of(1996, 1, 23);
				  System.out.println(d1);
				  LocalDate d2 = d1.plusYears(30);
				  System.out.println(d2);
				  LocalDate d3 = d2.minusYears(30);
				  System.out.println(d3);
				  System.out.println(d1 == d3);
				  System.out.println(Objects.hasIdentity(d1));


				  String s = "abcd";
				  System.out.println(Objects.hasIdentity(s));
				  String t = "aabcd".substring(1);
				  System.out.println(s == t);
				  System.out.println(s.equals(t));
			  }
			}
 			"""
 			},
			"true\n" +
			"false\n" +
			"1996-01-23\n" +
			"2026-01-23\n" +
			"1996-01-23\n" +
			"true\n" +
			"false\n" +
			"true\n" +
			"false\n" +
			"true",
			getCompilerOptions(false), VMARGS, new JavacTestOptions("-source 28"));
 	}
    // Same snippet as above but with preview turned on for compiler and turned off for runtime
    public void testValueness_with_compiler_preview_without_runtime_preview() {
		Runner runner = new Runner();
		runner.customOptions = getCompilerOptions(true); // preview enabled
		runner.testFiles = new String[] {
				"X.java",
				"""
				import java.time.LocalDate;
				import java.util.Objects;

				public class X {
				  public static void main(String[] args){
					  Integer x = 1996, y = 1996;
					  System.out.println(x == y);
					  System.out.println(Objects.hasIdentity(x));

					  LocalDate d1 = LocalDate.of(1996, 1, 23);
					  System.out.println(d1);
					  LocalDate d2 = d1.plusYears(30);
					  System.out.println(d2);
					  LocalDate d3 = d2.minusYears(30);
					  System.out.println(d3);
					  System.out.println(d1 == d3);
					  System.out.println(Objects.hasIdentity(d1));


					  String s = "abcd";
					  System.out.println(Objects.hasIdentity(s));
					  String t = "aabcd".substring(1);
					  System.out.println(s == t);
					  System.out.println(s.equals(t));
				  }
				}
				"""
			};
		runner.javacTestOptions = JAVAC_OPTIONS;
//		runner.vmArguments = VMARGS; not passing --enable-preview to java
		runner.expectedErrorString =
				"""
				java.lang.UnsupportedClassVersionError: Preview features are not enabled for X (class file version 72.65535). Try running with '--enable-preview'
				""";
		runner.runConformTest();
	}

    // Snippet from https://openjdk.org/jeps/401 - test synchronization - compile time
    public void testSynchronizationCompileTime() {
    	runNegativeTest(new String [] {
 				"X.java",
 				"""
 				import java.time.LocalDate;

 				public class X {
 				  public static void main(String[] args){
 					  LocalDate d1 = LocalDate.of(1996, 1, 23);
 					  synchronized (d1) { d1.notify(); }
 				  }
 				}
 				"""},
    			"----------\n" +
				"1. ERROR in X.java (at line 6)\n" +
				"	synchronized (d1) { d1.notify(); }\n" +
				"	              ^^\n" +
				"Illegal attempt to synchronize on an instance of a value class\n" +
				"----------\n");

 	}
    // Snippet from https://openjdk.org/jeps/401 - test synchronization - run time
    public void testSynchronizationRunTime() {
    	runConformTest(new String [] {
 				"X.java",
 				"""
 				import java.time.LocalDate;

 				public class X {
 				  public static void main(String[] args){
 					  LocalDate d1 = LocalDate.of(1996, 1, 23);
 					  Object o = d1;
 					  try {
 					      synchronized (o) { d1.notify(); }
				      } catch (IdentityException e) {
				          System.out.println("All well!");
				      }
 				  }
 				}
 				"""},
    			"All well!");
 	}
    // Test subclassing - legal and illegal scenarios
    // Snippet from https://openjdk.org/jeps/401 - test synchronization - compile time
    public void testSubclassing() {
    	runNegativeTest(new String [] {
 				"X.java",
 				"""
				import java.io.IOException;

				// Legal subclassing scenarios
				value class V1 {} // implicit extension of jlO
				abstract value class V2 extends Object {} // explicit extension of jlO
				value class V3 extends java.lang.Object {} // explicit extension of explicitly spelled out jlO
				value class V4 extends java.lang.Number { // concrete extension of abstract value class

					private static final long serialVersionUID = 1L;

					@Override
					public int intValue() {
						return 0;
					}

					@Override
					public long longValue() {
						return 0;
					}

					@Override
					public float floatValue() {
						return 0;
					}

					@Override
					public double doubleValue() {
						return 0;
					}
				}
				abstract value class V5 extends Number { // abstract extension of abstract value class
					private static final long serialVersionUID = 1L;
				}

				value class V6 implements java.io.Serializable { // A value class may implement interfaces
					private static final long serialVersionUID = 1L;
				}
				class I1 extends V2 {} // identity class may subclass an abstract value class
				value record VPoint(int x, int y) {} // Legal value record

				// negative tests below
				value class V7 extends String {} // cannot subclass concrete identity class
				value class V8 extends java.util.Map<String, String> {} // a super class must be a class
				value class V9 extends java.io.InputStream { // cannot subclass abstract identity class

				    @Override
				    public int read() throws IOException {
				        return 0;
				    }}
				value class V10 extends V3 {} // cannot subclass a concrete value class
				class I2 extends V1 {} // identity class cannot subclass a concrete value class.
				abstract value class V11 extends java.util.ArrayList<String> { // abstract value class may not subclass concrete identity class
					private static final long serialVersionUID = 1L;
				}
 				"""},
    			"----------\n" +
				"1. WARNING in X.java (at line 4)\n" +
				"	value class V1 {} // implicit extension of jlO\n" +
				"	^^^^^\n" +
				"You are using a preview language feature that may or may not be supported in a future release\n" +
				"----------\n" +
				"2. WARNING in X.java (at line 5)\n" +
				"	abstract value class V2 extends Object {} // explicit extension of jlO\n" +
				"	         ^^^^^\n" +
				"You are using a preview language feature that may or may not be supported in a future release\n" +
				"----------\n" +
				"3. WARNING in X.java (at line 6)\n" +
				"	value class V3 extends java.lang.Object {} // explicit extension of explicitly spelled out jlO\n" +
				"	^^^^^\n" +
				"You are using a preview language feature that may or may not be supported in a future release\n" +
				"----------\n" +
				"4. WARNING in X.java (at line 7)\n" +
				"	value class V4 extends java.lang.Number { // concrete extension of abstract value class\n" +
				"	^^^^^\n" +
				"You are using a preview language feature that may or may not be supported in a future release\n" +
				"----------\n" +
				"5. WARNING in X.java (at line 31)\n" +
				"	abstract value class V5 extends Number { // abstract extension of abstract value class\n" +
				"	         ^^^^^\n" +
				"You are using a preview language feature that may or may not be supported in a future release\n" +
				"----------\n" +
				"6. WARNING in X.java (at line 35)\n" +
				"	value class V6 implements java.io.Serializable { // A value class may implement interfaces\n" +
				"	^^^^^\n" +
				"You are using a preview language feature that may or may not be supported in a future release\n" +
				"----------\n" +
				"7. WARNING in X.java (at line 39)\n" +
				"	value record VPoint(int x, int y) {} // Legal value record\n" +
				"	^^^^^\n" +
				"You are using a preview language feature that may or may not be supported in a future release\n" +
				"----------\n" +
				"8. WARNING in X.java (at line 42)\n" +
				"	value class V7 extends String {} // cannot subclass concrete identity class\n" +
				"	^^^^^\n" +
				"You are using a preview language feature that may or may not be supported in a future release\n" +
				"----------\n" +
				"9. ERROR in X.java (at line 42)\n" +
				"	value class V7 extends String {} // cannot subclass concrete identity class\n" +
				"	                       ^^^^^^\n" +
				"The type V7 cannot subclass the final class String\n" +
				"----------\n" +
				"10. WARNING in X.java (at line 43)\n" +
				"	value class V8 extends java.util.Map<String, String> {} // a super class must be a class\n" +
				"	^^^^^\n" +
				"You are using a preview language feature that may or may not be supported in a future release\n" +
				"----------\n" +
				"11. ERROR in X.java (at line 43)\n" +
				"	value class V8 extends java.util.Map<String, String> {} // a super class must be a class\n" +
				"	                       ^^^^^^^^^^^^^\n" +
				"The type Map<String,String> cannot be the superclass of V8; a superclass must be a class\n" +
				"----------\n" +
				"12. WARNING in X.java (at line 44)\n" +
				"	value class V9 extends java.io.InputStream { // cannot subclass abstract identity class\n" +
				"	^^^^^\n" +
				"You are using a preview language feature that may or may not be supported in a future release\n" +
				"----------\n" +
				"13. ERROR in X.java (at line 44)\n" +
				"	value class V9 extends java.io.InputStream { // cannot subclass abstract identity class\n" +
				"	                       ^^^^^^^^^^^^^^^^^^^\n" +
				"A value class may extend either java.lang.Object or an abstract value class, but not an identity class\n" +
				"----------\n" +
				"14. WARNING in X.java (at line 50)\n" +
				"	value class V10 extends V3 {} // cannot subclass a concrete value class\n" +
				"	^^^^^\n" +
				"You are using a preview language feature that may or may not be supported in a future release\n" +
				"----------\n" +
				"15. ERROR in X.java (at line 50)\n" +
				"	value class V10 extends V3 {} // cannot subclass a concrete value class\n" +
				"	                        ^^\n" +
				"The type V10 cannot subclass the final class V3\n" +
				"----------\n" +
				"16. ERROR in X.java (at line 51)\n" +
				"	class I2 extends V1 {} // identity class cannot subclass a concrete value class.\n" +
				"	                 ^^\n" +
				"The type I2 cannot subclass the final class V1\n" +
				"----------\n" +
				"17. WARNING in X.java (at line 52)\n" +
				"	abstract value class V11 extends java.util.ArrayList<String> { // abstract value class may not subclass concrete identity class\n" +
				"	         ^^^^^\n" +
				"You are using a preview language feature that may or may not be supported in a future release\n" +
				"----------\n" +
				"18. ERROR in X.java (at line 52)\n" +
				"	abstract value class V11 extends java.util.ArrayList<String> { // abstract value class may not subclass concrete identity class\n" +
				"	                                 ^^^^^^^^^^^^^^^^^^^\n" +
				"A value class may extend either java.lang.Object or an abstract value class, but not an identity class\n" +
				"----------\n");
 	}


    public void testSynchronizedMethods() {
           runNegativeTest(new String [] {
               "X.java",
               """
               public value class X {
                   synchronized void foo() {} // error - no lock
                   static synchronized void goo() {} // ok.
               }
               """},
    		   "----------\n" +
			   "1. WARNING in X.java (at line 1)\n" +
			   "	public value class X {\n" +
			   "	       ^^^^^\n" +
			   "You are using a preview language feature that may or may not be supported in a future release\n" +
			   "----------\n" +
               "2. ERROR in X.java (at line 2)\n" +
               "	synchronized void foo() {} // error - no lock\n" +
               "	                  ^^^^^\n" +
               "A value class may not declare a synchronized instance method\n" +
               "----------\n");
    }

    public void testFieldFinality () {
       runNegativeTest(new String [] {
               "X.java",
               """
               public value class X {
                   int x = 99;
                   static int xx = 99;
                   int y;
                   X() {
                      // y = 123;
                   }
                   void foo() {
                       x++; // error
                       xx++; // ok
                       y = 123; // error
                   }
               }
               """},
    		   "----------\n" +
			   "1. WARNING in X.java (at line 1)\n" +
			   "	public value class X {\n" +
			   "	       ^^^^^\n" +
			   "You are using a preview language feature that may or may not be supported in a future release\n" +
			   "----------\n" +
               "----------\n" +
               "2. ERROR in X.java (at line 5)\n" +
               "	X() {\n" +
               "	^^^\n" +
               "The blank final field y may not have been initialized\n" +
               "----------\n" +
               "3. ERROR in X.java (at line 9)\n" +
               "	x++; // error\n" +
               "	^\n" +
               "The final field X.x cannot be assigned\n" +
               "----------\n" +
               "4. ERROR in X.java (at line 11)\n" +
               "	y = 123; // error\n" +
               "	^\n" +
               "The final field X.y cannot be assigned\n" +
               "----------\n");
    }

    public void testFieldModifiers() throws Exception {
       runConformTest(
           new String[] {
               "X.java",
               """
               public value class X {
                   int x = 99;
                   static int xx = 99;
                   int y;
                   X() {
                      y = 123;
                   }
                   public static void main(String [] args) {
                       System.out.println("Ok!");
                   }
               }
               """
           },
           "Ok!");
       String expectedOutput =
               "// Compiled from X.java (version 28 : 72.65535, no super bit)\n" + // why is preview flag missing ??
               "public final class X {\n" +
               "  Constant pool:\n";
       verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
       expectedOutput =
               "  // Field descriptor #6 I\n" +
               "  final strict_init int x = 99;\n" +
               "  \n" +
               "  // Field descriptor #6 I\n" +
               "  static int xx;\n" +
               "  \n" +
               "  // Field descriptor #6 I\n" +
               "  final strict_init int y;\n";
       verifyClassFile(expectedOutput, "X.class", ClassFileBytesDisassembler.SYSTEM);
    }

    public void testPreviewAPI() throws Exception {
		Runner runner = new Runner();
		runner.customOptions = getCompilerOptions(true); // preview enabled
		runner.testFiles = new String[] {
				"X.java",
				"""
				import java.util.Objects;

				public class X {
				  public static void main(String[] args){
					  Integer x = 1996, y = 1996;
					  System.out.println(x == y);
					  System.out.println(Objects.hasIdentity(x));
				  }
				}
				"""
			};
		runner.javacTestOptions = JAVAC_OPTIONS;
		runner.vmArguments = VMARGS;
		runner.expectedCompilerLog =
				"----------\n" +
				"1. WARNING in X.java (at line 7)\n" +
				"	System.out.println(Objects.hasIdentity(x));\n" +
				"	                   ^^^^^^^^^^^^^^^^^^^^^^\n" +
				"You are using an API that is part of the preview feature 'Value Classes and Objects' and may be removed in future\n" +
				"----------\n";
		runner.runWarningTest();
    }

    // Warn on finalize method, the garbage collector never invokes it
    public void testFinalizeMethod() {
    	runWarningTest(new String [] {
 				"X.java",
 				"""
 				public value class X {
 				    public void finalize() {}
 				    public static void main(String [] args) {
 				        System.out.println("Ok!");
			        }
 				}
 				"""},
    			"----------\n" +
				"1. WARNING in X.java (at line 1)\n" +
				"	public value class X {\n" +
				"	       ^^^^^\n" +
				"You are using a preview language feature that may or may not be supported in a future release\n" +
				"----------\n" +
				"2. WARNING in X.java (at line 2)\n" +
				"	public void finalize() {}\n" +
				"	            ^^^^^^^^^^\n" +
				"The finalize method is useless in a value class\n" +
				"----------\n",
				getCompilerOptions(true));
 	}

    public void testEmissionOfLoadableDescriptors() throws Exception {
    	runConformTest(
    	           new String[] {
    	               "X.java",
    	               """
						import java.lang.classfile.ClassFile;
						import java.lang.classfile.ClassModel;
						import java.lang.classfile.attribute.LoadableDescriptorsAttribute;
						import java.net.URI;
						import java.nio.file.Path;
						import java.nio.file.Paths;

						value class V1 {}
						value class V2 {}
						value class V3 {}
						value class V4 {}
						value class V5 {}
						value class V6 {}
						value class V7 {}
    	                value class V8 {}

						public class X {

						    V1 v1 = new V1();
						    V2[] v2a = null;
						    java.util.ArrayList<V3> alv3 = null;

						    <T extends V8> void foo(V4 v4, V5[] v5a, java.util.ArrayList<V6> alv6, T t) {
						        V7 v7 = new V7();
						    }


						    public static void main(String[] args) throws Exception {
						        Class<?> self = X.class;
						        URI uri = self.getResource(self.getSimpleName() + ".class").toURI();
						        Path classPath = Paths.get(uri);
						        ClassModel classModel = ClassFile.of().parse(classPath);
						        for (var attribute : classModel.attributes()) {
						            if (attribute instanceof LoadableDescriptorsAttribute loadableAttr) {
						                loadableAttr.loadableDescriptors().forEach(desc -> {
						                    System.out.println(desc.stringValue());
						                });
						            }
						        }
						    }
						}
    	               """
    	           },
    	           "LV1;\n" +
    	           "LV4;\n" +
    	           "LV8;");
    }

    // test value class that cannot be a record - snippet from JEP401
    public void testValueClassThatCannotBeRecord() {
        runConformTest(new String [] {
                "EURCurrency.java",
                """
                public value class EURCurrency {

                    private long cs;  // implicitly final

                    private EURCurrency(long cs) { this.cs = cs; }

                    public EURCurrency(long e, int c, boolean neg) {
                        this(neg ? -e * 100 - c : e * 100 + c);
                    }

                    public EURCurrency(long e, int c) { this(e, c, false); }

                    public long euros() { return Math.abs(cs) / 100; }
                    public int cents() { return (int) Math.abs(cs) % 100; }
                    public boolean negative() { return cs < 0; }

                    public String toString() {
                        var prefix = negative() ? "-" : "";
                        return "%s%d,%d".formatted(prefix, euros(), cents());
                    }

                    public static void main(String [] args) {
                        EURCurrency e1 = new EURCurrency(237);
                        System.out.println(e1);
                        EURCurrency e2 = new EURCurrency(2, 37);
                        System.out.println(e2);
                        System.out.println(e1 == e2);
                    }
                }
                """},
        		"2,37\n" +
				"2,37\n" +
        		"true");
    }

    // test value records - snippet from JEP401
    public void testValueRecordWithSynthesizedCanonicalConstructor() {
    	runConformTest(new String [] {
 				"X.java",
 				"""
 				import java.util.Objects;

 				public class X {
 				    value record Point(int x, int y) {}

 				    public static void main(String [] args) {
 				    	Point p = new Point(17, 3);
 				    	System.out.println(p);
 				    	System.out.println(Objects.hasIdentity(p));
 				    	System.out.println(new Point(17, 3) == p);
 				    	System.out.println(new Point(17, 4) == p);
 				    }

 				}
 				"""},
    			"Point[x=17, y=3]\n" +
				"false\n" +
				"true\n" +
				"false");
 	}

    // test value record with compact constructor
    public void testValueRecordWithCompactConstructor() {
        runConformTest(new String [] {
                "Point.java",
                """
				public value record Point(int x, int y) {

					public Point {
						System.out.println(x);
						System.out.println(y);
					}
					public static void main(String[] args) {
						Point p1 = new Point (1024, 1024);
						System.out.println(p1);
						Point p2 = new Point(512, 512);
						System.out.println(p2);
						System.out.println(p1 == p2);
					}
				}
                """},
        		"1024\n" +
				"1024\n" +
				"Point[x=1024, y=1024]\n" +
				"512\n" +
				"512\n" +
				"Point[x=512, y=512]\n" +
				"false");
    }

    // test value record with broken constructor
    public void testValueRecordWithExpressBrokenConstructor() {
        runNegativeTest(new String [] {
                "Point.java",
                """
				public value record Point(int x, int y) {

					public Point(int x, int y) {
						System.out.println(x);
						System.out.println(y);
					}
					public static void main(String[] args) {
						Point p1 = new Point (1024, 1024);
						System.out.println(p1);
						Point p2 = new Point(512, 512);
						System.out.println(p2);
						System.out.println(p1 == p2);
					}
				}
                """},
        		"----------\n" +
				"1. WARNING in Point.java (at line 1)\r\n" +
				"	public value record Point(int x, int y) {\r\n" +
				"	       ^^^^^\n" +
				"You are using a preview language feature that may or may not be supported in a future release\n" +
				"----------\n" +
				"2. ERROR in Point.java (at line 3)\r\n" +
				"	public Point(int x, int y) {\r\n" +
				"	       ^^^^^^^^^^^^^^^^^^^\n" +
				"The blank final field x may not have been initialized\n" +
				"----------\n" +
				"3. ERROR in Point.java (at line 3)\r\n" +
				"	public Point(int x, int y) {\r\n" +
				"	       ^^^^^^^^^^^^^^^^^^^\n" +
				"The blank final field y may not have been initialized\n" +
				"----------\n");
    }

    // test value class constructor calling super before all fields are initialized
    public void _testValueClassTooEagerSuper() {
        runNegativeTest(new String [] {
                "Point.java",
                """
				public value class Point {

					int x;
					int y;

					public Point(int x, int y) {
						this.x = x;
						super();
						//this.y = y;
					}
					public static void main(String[] args) {
						Point p1 = new Point (1024, 1024);
						Point p2 = new Point(512, 512);
						System.out.println(p1 == p2);
					}
				}
                """},
        		"----------\n" +
				"1. WARNING in Point.java (at line 1)\r\n" +
				"	public value record Point(int x, int y) {\r\n" +
				"	       ^^^^^\n" +
				"You are using a preview language feature that may or may not be supported in a future release\n" +
				"----------\n" +
				"2. ERROR in Point.java (at line 3)\r\n" +
				"	public Point(int x, int y) {\r\n" +
				"	       ^^^^^^^^^^^^^^^^^^^\n" +
				"The blank final field x may not have been initialized\n" +
				"----------\n" +
				"3. ERROR in Point.java (at line 3)\r\n" +
				"	public Point(int x, int y) {\r\n" +
				"	       ^^^^^^^^^^^^^^^^^^^\n" +
				"The blank final field y may not have been initialized\n" +
				"----------\n");
    }

    // test value record with explicit constructor
    public void testValueRecordWithExpressConstructor() {
        runConformTest(new String [] {
                "Point.java",
                """
				public value record Point(int x, int y) {
					public Point(int x, int y) {
						System.out.println(x);
						System.out.println(y);
						this.x = x;
						this.y = y;
					}
					public static void main(String[] args) {
						Point p1 = new Point (1024, 1024);
						System.out.println(p1);
						Point p2 = new Point(512, 512);
						System.out.println(p2);
						System.out.println(p1 == p2);
					}
				}
                """},
        		"1024\n" +
				"1024\n" +
				"Point[x=1024, y=1024]\n" +
				"512\n" +
				"512\n" +
				"Point[x=512, y=512]\n" +
				"false");
    }

    // test constructor chaining
    public void testConstructorChaining() {
        runConformTest(new String [] {
                "X.java",
                """
				abstract value class Base {
					Base() {
						System.out.println("In Super Ctor");
					}
				}

				public value class X extends Base {

					final int x = 5;
					final int y = 10;
					final int z = 15;

					X() {
						super();
						System.out.println("In X Ctor Epilogue");
					}

					public static void main(String[] args) {
						new X();
					}
				}
                """},
        		"In Super Ctor\n" +
				"In X Ctor Epilogue");
    }

    // test constructor chaining
    public void testConstructorChaining_2() {
        runConformTest(new String [] {
                "X.java",
                """
				abstract value class Base {
					Base() {
						System.out.println("In Super Ctor");
					}
				}

				public value class X extends Base {

					final int x = 5;
					final int y = 10;
					final int z = 15;

					X() {
					    System.out.println("In X Ctor Prologue");
						super();
						System.out.println("In X Ctor Epilogue");
					}

					public static void main(String[] args) {
						new X();
					}
				}
                """},
        		"In X Ctor Prologue\n" +
        		"In Super Ctor\n" +
				"In X Ctor Epilogue");
    }

    // test constructor chaining
    public void testConstructorChaining_3() {
        runConformTest(new String [] {
                "X.java",
                """
				abstract value class Base {
				    Base() {
				        System.out.println("In Super Constructor");
				    }
				}

				public value class X extends Base {

				    final int x = 5;
				    final int y = 10;
				    final int z = 15;

				    {
				        System.out.println("In Initializer block");
				    }

				    X() {
				        System.out.println("In X Ctor Prologue");
				        super();
				        System.out.println("In X Ctor Epilogue");
				    }

				    public static void main(String[] args) {
				        new X();
				    }
				}
                """},
        		"In X Ctor Prologue\n" +
				"In Super Constructor\n" +
				"In Initializer block\n" +
				"In X Ctor Epilogue");
    }

    // test constructor chaining
    public void testConstructorChaining_4() {
        runConformTest(new String [] {
                "X.java",
                """
				abstract value class Base {
				    Base() {
				        System.out.println("In Super Constructor");
				    }
				}

				public value class X extends Base {

				    final int x = 5;
				    final int y = 10;
				    final int z = 15;

				    {
				        System.out.println("In Initializer block");
				    }

				    X() {
				        System.out.println("In X Ctor Prologue");
				    }

				    public static void main(String[] args) {
				        new X();
				    }
				}
                """},
        		"In X Ctor Prologue\n" +
				"In Super Constructor\n" +
				"In Initializer block");
    }

    // test constructor chaining
    public void testConstructorChaining_5() {
        runConformTest(new String [] {
                "X.java",
                """
				abstract value class Base {
				    Base() {
				        System.out.println("In Super Constructor");
				    }
				}

				public value class X extends Base {

				    final int x = 5;
				    final int y = 10;
				    final int z = 15;

				    {
				        System.out.println("In Initializer block");
				    }

				    X() {
				        super();
				        System.out.println("In X Epilogue block");

				    }

				    public static void main(String[] args) {
				        new X();
				    }
				}
                """},
        		"In Super Constructor\n" +
				"In Initializer block\n" +
				"In X Epilogue block");
    }

    // Disallow return in constructor prologue
    public void _testReturnFromPrologue() {
        runConformTest(new String [] {
                "X.java",
                """
				public value class X  {

					final int x = 5;
					final int y = 10;
					final int z = 15;

					X() {
						System.out.println("In X Ctor");
						return;
					}

					public static void main(String[] args) {
						new X();
					}
				}
                """},
        		"In X Ctor Prologue\n" +
        		"In Super Ctor\n" +
				"In X Ctor Epilogue");
    }

    public void _testInnerValueClass() {
        runConformTest(new String [] {
                "X.java",
                """
                public class X {
					public value class Point {
					    int x;
					    int y;
						public Point(int x, int y) {
							this.x = x;
							this.y = y;
						}
					public static void main(String[] args) {
						X x = new X();
						Point p1 = x.new Point (1024, 1024);
						System.out.println(p1);
						Point p2 = new X().new Point(1024, 1024);
						System.out.println(p1 == p2);
						Point p3 = x.new Point(1024, 1024);
						System.out.println(p1 == p3);
					}
				}
                """},
        		"1024\n" +
				"1024\n" +
				"Point[x=1024, y=1024]\n" +
				"512\n" +
				"512\n" +
				"Point[x=512, y=512]\n" +
				"false");
    }

 }