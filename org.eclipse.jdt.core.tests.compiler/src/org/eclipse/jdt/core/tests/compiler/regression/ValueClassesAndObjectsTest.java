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
				"2. ERROR in X.java (at line 5)\n" +
				"	X() {\n" +
				"	^^^\n" +
				"The field \'y\' must be initialized before chaining to the super class constructor\n" +
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
				"1. WARNING in Point.java (at line 1)\n" +
				"	public value record Point(int x, int y) {\n" +
				"	       ^^^^^\n" +
				"You are using a preview language feature that may or may not be supported in a future release\n" +
				"----------\n" +
				"2. ERROR in Point.java (at line 3)\n" +
				"	public Point(int x, int y) {\n" +
				"	       ^^^^^^^^^^^^^^^^^^^\n" +
				"The field \'x\' must be initialized before chaining to the super class constructor\n" +
				"----------\n" +
				"3. ERROR in Point.java (at line 3)\n" +
				"	public Point(int x, int y) {\n" +
				"	       ^^^^^^^^^^^^^^^^^^^\n" +
				"The field \'y\' must be initialized before chaining to the super class constructor\n" +
				"----------\n");
    }

    // test value class constructor calling super before all fields are initialized
    public void testValueClassTooEagerSuper() {
        runNegativeTest(new String [] {
                "Point.java",
                """
				public value class Point {

					int x;
					int y;

					public Point(int x, int y) {
						this.x = x;
						super();
						this.y = y;
					}
					public static void main(String[] args) {
						Point p1 = new Point (1024, 1024);
						Point p2 = new Point(512, 512);
						System.out.println(p1 == p2);
					}
				}
                """},
        		"----------\n" +
				"1. WARNING in Point.java (at line 1)\n" +
				"	public value class Point {\n" +
				"	       ^^^^^\n" +
				"You are using a preview language feature that may or may not be supported in a future release\n" +
				"----------\n" +
				"2. ERROR in Point.java (at line 8)\n" +
				"	super();\n" +
				"	^^^^^^^^\n" +
				"The field \'y\' must be initialized before chaining to the super class constructor\n" +
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
    public void testReturnFromPrologue() {
        runNegativeTest(new String [] {
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
        		"----------\n" +
				"1. WARNING in X.java (at line 1)\n" +
				"	public value class X  {\n" +
				"	       ^^^^^\n" +
				"You are using a preview language feature that may or may not be supported in a future release\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 9)\n" +
				"	return;\n" +
				"	^^^^^^^\n" +
				"return; statement not allowed in an early construction context\n" +
				"----------\n");
    }

    public void testInnerValueClass() {
        runConformTest(new String [] {
                "X.java",
                """
				public class X {

				    record Y(int x, int y) {
				    }

				    public value class Point {
				        int x;
				        int y;

				        public Point(int x, int y) {
				            this.x = x;
				            this.y = y;
				        }

				        // Forces retention of the outer instance reference by javac, if return X.this; is used as opposed to return null;
				        X outer() {
				            return null;
				           //  return X.this;
				        }

				        public String toString() {
				            return "Point(" + x + ", " + y + ")";
				        }
				    }

				    public static void main(String[] args) {
				        X x = new X();
				        Point p1 = x.new Point(1024, 1024);
				        System.out.println(p1);
				        Point p2 = new X().new Point(1024, 1024);
				        System.out.println(p1 == p2);
				        Point p3 = x.new Point(1024, 1024);
				        System.out.println(p1 == p3);
				    }
				}
                """},
        		"Point(1024, 1024)\n" +
				"false\n" +
				"true");
    }

    public void testStrictlyInitializedRecordClass() {
        runConformTest(new String [] {
                "Point.java",
                """
				import java.lang.reflect.Field;

				public record Point(int x, int y) {

				    public Point(int x, int y) {
				        System.out.println(x);
				        System.out.println(y);
				        this.x = x;
				        this.y = y;
				    }

				    public static void main(String[] args) {
				        Point p1 = new Point(1024, 1024);
				        System.out.println(p1);
				        Point p2 = new Point(512, 512);
				        System.out.println(p2);
				        System.out.println(p1 == p2);

				        for (Field field : Point.class.getDeclaredFields()) {

				            String fieldName = field.getName();
				            String fieldType = field.getType().getSimpleName();

				            int modifiersBitmask = field.getModifiers();

				            System.out.print("Name: " + fieldName);
				            System.out.print(" (" + fieldType + ")");
				            if ((modifiersBitmask & 0x800) != 0)
				                System.out.println(" is strictly initialized");
				        }
				    }
				}
                """},
        		"1024\n" +
				"1024\n" +
				"Point[x=1024, y=1024]\n" +
				"512\n" +
				"512\n" +
				"Point[x=512, y=512]\n" +
				"false\n" +
				"Name: x (int) is strictly initialized\n" +
				"Name: y (int) is strictly initialized");
    }

    // Same snippet as above but with preview turned off for both compiler and runtime
    public void testLooselyInitializedRecordClass() {
 		runConformTest(new String[] {
 			"X.java",
 			"""
            import java.lang.reflect.Field;

            public class X {

                public record Point(int x, int y) {

                    public Point(int x, int y) {
                        System.out.println(x);
                        System.out.println(y);
                        this.x = x;
                        this.y = y;
                    }
                 }

                public static void main(String[] args) {
                    Point p1 = new Point(1024, 1024);
                    System.out.println(p1);
                    Point p2 = new Point(512, 512);
                    System.out.println(p2);
                    System.out.println(p1 == p2);

                    for (Field field : Point.class.getDeclaredFields()) {

                        String fieldName = field.getName();
                        String fieldType = field.getType().getSimpleName();

                        int modifiersBitmask = field.getModifiers();

                        System.out.print("Name: " + fieldName);
                        System.out.print(" (" + fieldType + ")");
                        if ((modifiersBitmask & 0x800) != 0)
                            System.out.println(" is strictly initialized");
                        else
                            System.out.println(" is loosely initialized");
                    }
                }
            }
 			"""
 			},
			"1024\n" +
			"1024\n" +
			"Point[x=1024, y=1024]\n" +
			"512\n" +
			"512\n" +
			"Point[x=512, y=512]\n" +
			"false\n" +
			"Name: x (int) is loosely initialized\n" +
			"Name: y (int) is loosely initialized",
 			getCompilerOptions(false), new String[] {}, new JavacTestOptions("-source 28"));
 	}

    // https://github.com/eclipse-jdt/eclipse.jdt.core/issues/5324
    // Implement strict/safe construction rules for record classes
    public void testIssue5324() {
    	Map<String, String> options = getCompilerOptions(true);
    	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_28);
    	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_28);
    	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_28);
    	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);

    	runNegativeTest(
    		new String[] {
    			"X.java",
    			"""
    			import java.util.List;

    			record Node(String label, List<Node> edges) {

    				static void nullCheck(Object arg, Object owner) {
    					if (arg == null) {
    						String msg = "null arg for " + owner.toString();
    						throw new IllegalArgumentException(msg);
    					}
    				}

    				public Node {
    					nullCheck(label, this);
    					nullCheck(edges, this);
    				}
    			}
    			"""
    		},
    		"----------\n" +
			"1. ERROR in X.java (at line 13)\n" +
			"	nullCheck(label, this);\n" +
			"	                 ^^^^\n" +
			"Cannot use \'this\' in an early construction context\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 14)\n" +
			"	nullCheck(edges, this);\n" +
			"	                 ^^^^\n" +
			"Cannot use \'this\' in an early construction context\n" +
			"----------\n",
    		null,
    		false,
    		options);
    }

    // https://github.com/eclipse-jdt/eclipse.jdt.core/issues/5324
    // Implement strict/safe construction rules for record classes
    public void testIssue5324_2() {
    	Map<String, String> options = getCompilerOptions(false);
    	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_28);
    	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_28);
    	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_28);
    	options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.DISABLED);

    	runConformTest(
    		new String[] {
    			"Node.java",
    			"""
    			import java.util.List;

    			public record Node(String label, List<Node> edges) {

    				static void nullCheck(Object arg, Object owner) {
    					if (arg == null) {
    						String msg = "null arg for " + owner.toString();
    						throw new IllegalArgumentException(msg);
    					}
    				}

    				public Node {
    					nullCheck(label, this);
    					nullCheck(edges, this);
    				}

    				public static void main(String[] args) {
    					System.out.println("Ok!");
    				}
    			}
    			"""
    		},
    		"Ok!",
    		options);
    }

    // Instance initializer blocks are run in the late construction phase;
    public void testInstanceInitializerBlocks() {
        runNegativeTest(new String [] {
                "X.java",
                """
				public value class X  {

					final int x;
					final int y;
					final int z = 15;

					{
						this.y = 10;
						this.x++;
					}

					X() {
						this.x = 5;
						super();
						return;
					}

					public static void main(String[] args) {
						new X();
					}

					void foo() {
						this.x++; // error
					}
				}
                """},
        		"----------\n" +
				"1. WARNING in X.java (at line 1)\n" +
				"	public value class X  {\n" +
				"	       ^^^^^\n" +
				"You are using a preview language feature that may or may not be supported in a future release\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 8)\n" +
				"	this.y = 10;\n" +
				"	     ^\n" +
				"The final field X.y cannot be assigned\n" +
				"----------\n" +
				"3. ERROR in X.java (at line 9)\n" +
				"	this.x++;\n" +
				"	     ^\n" +
				"The final field X.x cannot be assigned\n" +
				"----------\n" +
				"4. ERROR in X.java (at line 14)\n" +
				"	super();\n" +
				"	^^^^^^^^\n" +
				"The field \'y\' must be initialized before chaining to the super class constructor\n" +
				"----------\n" +
				"5. ERROR in X.java (at line 23)\n" +
				"	this.x++; // error\n" +
				"	     ^\n" +
				"The final field X.x cannot be assigned\n" +
				"----------\n");
    }

    // Instance initializer blocks are run in the late construction phase;
    public void testInstanceInitializerBlocks_2() { // different order of visitation of the instance initializer block and the field initializers
        runNegativeTest(new String [] {
                "X.java",
                """
				public value class X  {

                    {
						this.x = 10;
					}

					final int x = 20;

					X() {

					}

					public static void main(String[] args) {
						new X();
					}
				}
                """},
        		"----------\n" +
				"1. WARNING in X.java (at line 1)\n" +
				"	public value class X  {\n" +
				"	       ^^^^^\n" +
				"You are using a preview language feature that may or may not be supported in a future release\n" +
				"----------\n" +
				"2. ERROR in X.java (at line 4)\n" +
				"	this.x = 10;\n" +
				"	     ^\n" +
				"The final field X.x cannot be assigned\n" +
				"----------\n");
    }

    // https://github.com/eclipse-jdt/eclipse.jdt.core/issues/5329
    // With preview enabled, allow fields to be read in early construction contexts
    // Fields that are initialized at declaration site cannot be read in early construction context! (since the initialization happens at super() boundary)
    public void testFieldReadsInEarlyConstruction() { // check illegal access via FieldReference, SingleNameReference & QualifiedNameReference
        runNegativeTest(
            new String[] {
                "X.java",
                """
                public class X {
                    X xo = new X();
                    int x;
                    int y;
                    int z = 10;
                    int k = 0;
                    X() {
                        x = 42;
                        y = x + xo.x + z + this.k;
                        super();
                        System.out.println("x = " + x + ", y = " + y);
                    }

                    public static void main() {
                        new X();
                    }
                }
                """
            },
            "----------\n" +
    		"1. ERROR in X.java (at line 9)\n" +
    		"	y = x + xo.x + z + this.k;\n" +
    		"	        ^\n" +
    		"Cannot refer to field xo in an early construction context\n" +
    		"----------\n" +
    		"2. ERROR in X.java (at line 9)\n" +
    		"	y = x + xo.x + z + this.k;\n" +
    		"	               ^\n" +
    		"Cannot refer to field z in an early construction context\n" +
    		"----------\n" +
    		"3. ERROR in X.java (at line 9)\n" +
    		"	y = x + xo.x + z + this.k;\n" +
    		"	                   ^^^^^^\n" +
    		"Cannot refer to field k in an early construction context\n" +
    		"----------\n");
    }

    // https://github.com/eclipse-jdt/eclipse.jdt.core/issues/5329
    // With preview enabled, allow fields to be read in early construction contexts
    public void testFieldReadsInEarlyConstruction_2() {
        runNegativeTest(
            new String[] {
                "X.java",
                """
                public class X {
                    final X xo;
                    int x;
                    int y;
                    X() {
                        x = 42;
                        y = x + xo.x;
                        super();
                        System.out.println("x = " + x + ", y = " + y);
                    }

                    public static void main() {
                        new X();
                    }
                }
                """
            },
            "----------\n" +
    		"1. ERROR in X.java (at line 7)\n" +
    		"	y = x + xo.x;\n" +
    		"	        ^^\n" +
    		"The blank final field xo may not have been initialized\n" +
    		"----------\n");
    }

    // https://github.com/eclipse-jdt/eclipse.jdt.core/issues/5329
    // With preview enabled, allow fields to be read in early construction contexts
    public void testFieldReadsInEarlyConstruction_3() {
        runConformTest(
            new String[] {
                "X.java",
                """
                public class X {
                    final String xo;
                    int x;
                    int y;
                    X() {
                        x = 42;
                        xo = "Hello";
                        y = x + xo.length();
                        super();
                        System.out.println("x = " + x + ", y = " + y);
                    }

                    public static void main() {
                        new X();
                    }
                }
                """
            },
            "x = 42, y = 47");
    }

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/5329
	// Exercise reads found in the prologue, including receivers and operands
	// of assignments, plus the argument of the explicit constructor call.
	public void testFieldReadsInEarlyConstructor() {
		runConformTest(new String[] {
			"X.java",
			"""
			public class X extends SuperType {
			    static int staticField = 17;

			    int directRead;
			    int assigned;
			    int rhsRead;
			    int[] array;
			    int index;
			    Box object;
			    int compound;
			    int increment;
			    int constructorArgument;
			    Box qualifiedRoot;
			    int afterConstructorCall;
			    final int blankFinal;

			    static class Box {
			        int field;
			    }

			    X(int rhsRead) {
			        directRead = 1;
			        assigned = rhsRead;             // parameter shadows the field rhsRead
			        this.rhsRead = 3;
			        array = new int[2];
			        index = 1;
			        array[index] = directRead + assigned;
			        object = new Box();
			        object.field = array[index];   // object is read on the LHS
			        compound = 4;
			        compound += object.field;       // reads old compound and object
			        increment = 2;
			        this.increment++;               // reads old increment
			        constructorArgument = 5;
			        qualifiedRoot = new Box();
			        qualifiedRoot.field = 7;
			        blankFinal = 11;
			        int localOnly = 6;

			        System.out.println("pre=" + directRead + "," + this.assigned
			            + "," + this.rhsRead + "," + array[index] + ","
			            + object.field + "," + compound + "," + increment
			            + "," + blankFinal + "," + staticField + "," + localOnly);
			        super(constructorArgument + qualifiedRoot.field + blankFinal);
			        afterConstructorCall = directRead;
			        System.out.println("after=" + afterConstructorCall + ","
			            + compound + "," + blankFinal);
			    }

			    public static void main(String[] args) {
			        new X(9);
			    }
			}

			class SuperType {
			    SuperType(int value) {
			        System.out.println("super=" + value);
			    }
			}
			"""
		},
		"pre=1,9,3,10,10,14,3,11,17,6\n" +
		"super=23\n" +
		"after=1,14,11");
	}

	public void testTooEagerReadOfFinalField() {
		runNegativeTest(
				new String[] {
						"X.java",
						"""
						public class X {
						    final int i = 42;
						    final double d;
						    X() {
						        System.out.println("i = " + i);
						        System.out.println("d = " + d); // flow analysis not done due to resolve error in previous line
						        super();
						    }
						    void main() {
						    }
						}
			    		""" },
						"----------\n" +
						"1. ERROR in X.java (at line 5)\n" +
						"	System.out.println(\"i = \" + i);\n" +
						"	                            ^\n" +
						"Cannot refer to field i in an early construction context\n" +
						"----------\n");
	}

	public void testTooEagerReadOfBlankFinalField() {
		runNegativeTest(
				new String[] {
						"X.java",
						"""
						public class X {
						    final double d;
						    X() {
						        System.out.println("d = " + d);
						        super();
						    }
						    void main() {
						    }
						}
			    		""" },
						"----------\n" +
						"1. ERROR in X.java (at line 4)\n" +
						"	System.out.println(\"d = \" + d);\n" +
						"	                            ^\n" +
						"The blank final field d may not have been initialized\n" +
						"----------\n");
	}
	public void testDefaultInitializationsInEarlyReads() {
		runConformTest(
				new String[] {
						"X.java",
						"""
						public class X {
						    boolean b;
						    char c;
						    byte by;
						    short s;
						    int i;
						    long l;
						    float f;
						    double d;
						    String string;
						    X() {
						        System.out.println("b = " + b);
						        System.out.println("c = " + c);
						        System.out.println("by = " + by);
						        System.out.println("s = " + s);
						        System.out.println("i = " + i);
						        System.out.println("l = " + l);
						        System.out.println("f = " + f);
						        System.out.println("d = " + d);
						        System.out.println("string = " + string);
						        super();
						    }
						    void main() {
						    }
						}
			    		""" },
						"b = false\n" +
						"c = \u0000\n" +
						"by = 0\n" +
						"s = 0\n" +
						"i = 0\n" +
						"l = 0\n" +
						"f = 0.0\n" +
						"d = 0.0\n" +
						"string = null");
	}

	public void testDefaultInitializationsInEarlyReadsThroughThis() {
		runConformTest(
				new String[] {
						"X.java",
						"""
						public class X {
						    boolean b;
						    char c;
						    byte by;
						    short s;
						    int i;
						    long l;
						    float f;
						    double d;
						    String string;
						    X() {
						        c = 'A';
						        this.i = 42;
						        s = 99;
						        this.f = 134.456f;
						        d = 456.789;
						        this.l = 9876543;
						        string = "Hello world";
						        System.out.println("b = " + b);
						        System.out.println("c = " + this.c);
						        System.out.println("by = " + by);
						        System.out.println("s = " + s);
						        System.out.println("i = " + i);
						        System.out.println("l = " + this.l);
						        System.out.println("f = " + this.f);
						        System.out.println("d = " + d);
						        System.out.println("string = " + string);
						        super();
						        System.out.println("b = " + b);
						        System.out.println("c = " + this.c);
						        System.out.println("by = " + by);
						        System.out.println("s = " + s);
						        System.out.println("i = " + i);
						        System.out.println("l = " + this.l);
						        System.out.println("f = " + this.f);
						        System.out.println("d = " + d);
						        System.out.println("string = " + string);
						    }
						    void main() {
						    }
						}
			    		""" },
						"b = false\n" +
						"c = A\n" +
						"by = 0\n" +
						"s = 99\n" +
						"i = 42\n" +
						"l = 9876543\n" +
						"f = 134.456\n" +
						"d = 456.789\n" +
						"string = Hello world\n" +
						"b = false\n" +
						"c = A\n" +
						"by = 0\n" +
						"s = 99\n" +
						"i = 42\n" +
						"l = 9876543\n" +
						"f = 134.456\n" +
						"d = 456.789\n" +
						"string = Hello world");
	}

	public void testFieldAssignmentInSuperCallIsPreserved() {
	    runConformTest(
	        new String[] {
	            "X.java",
	            """
	            class S {
	                S(int i, int j) {}
	            }
	            public class X extends S {
	                int x;
	                int y;
	                X() {
	                    super(x = 42, y += 99);
	                    System.out.println(x + " " + y);
	                }
	                public static void main(String[] args) {
	                    new X();
	                }
	            }
	            """
	        },
	        "42 99");
	}

	public void testTooEagerReadOfSuperField() {
		runNegativeTest(
				new String[] {
						"X.java",
						"""
						class Super {
						    int i;
						    Super(int k) {}
						}

						 class Test extends Super {
						    Test() {
						        super(i);
						    }
						}
			    		""" },
						"----------\n" +
						"1. ERROR in X.java (at line 8)\n" +
						"	super(i);\n" +
						"	      ^\n" +
						"Cannot refer to field i in an early construction context\n" +
						"----------\n");
	}

	public void testProxyFlush() {
	    runConformTest(
	        new String[] {
	            "X.java",
	            """
				class S {
				    S() {
				    }
				}

				public class X extends S {
				    int x;

				    {
				        x = 2;
				    }

				    X() {
				        x = 1;
				        int ignored = x; // causes x to receive a proxy
				        super();
				    }

				    public static void main(String[] args) {
				        System.out.print(new X().x);
				    }
				}
	            """
	        },
	        "2");
	}

	public void testFieldAccessBeforeAlternateConstructorCall() {
	    runNegativeTest(
	        new String[] {
	            "X.java",
	            """
				class S {
				    S() {
				    }
				}

				public class X extends S {
				    int x;

				    {
				        x = 2;
				    }

				    X(int x) {}
				    X() {
				        x = 1;
				        int ignored = x;
				        this(x);
				        System.out.println(x);
				    }

				    public static void main(String[] args) {
				        System.out.print(new X().x);
				    }
				}
	            """
	        },
	        "----------\n" +
    		"1. ERROR in X.java (at line 15)\n" +
    		"	x = 1;\n" +
    		"	^\n" +
    		"Cannot refer to field x in an early construction context\n" +
    		"----------\n" +
    		"2. ERROR in X.java (at line 16)\n" +
    		"	int ignored = x;\n" +
    		"	              ^\n" +
    		"Cannot refer to field x in an early construction context\n" +
    		"----------\n" +
    		"3. ERROR in X.java (at line 17)\n" +
    		"	this(x);\n" +
    		"	     ^\n" +
    		"Cannot refer to field x in an early construction context\n" +
    		"----------\n");
	}

	// ===== Value classes: instance fields in early construction context =====

	// Initializer value is readable (simple name) in a super-chaining prologue
	public void testValueClassECC_Conform_01() {
		runConformTest(new String[] {
			"X.java",
			"""
			public value class X {
				int f = 42;
				{
					System.out.println("Init block");
				}
				X() {
					System.out.println(f);
					super();
					System.out.println("epilogue");
				}
				public static void main(String[] args) {
					new X();
				}
			}
			"""
			},
			"42\n" +
			"Init block\n" +
			"epilogue");
	}

	// Initializer value is readable (this-qualified) in a super-chaining prologue
	public void testValueClassECC_Conform_02() {
		runConformTest(new String[] {
			"X.java",
			"""
			public value class X {
				int f = 42;
				X() {
					System.out.println(this.f);
					super();
				}
				public static void main(String[] args) {
					new X();
				}
			}
			"""
			},
			"42");
	}

	// Field initializers execute before any prologue statement
	public void testValueClassECC_Conform_03() {
		runConformTest(new String[] {
			"X.java",
			"""
			public value class X {
				int f = init();
				static int init() {
					System.out.println("initializer");
					return 1;
				}
				X() {
					System.out.println("prologue");
					super();
					System.out.println("epilogue");
				}
				public static void main(String[] args) {
					new X();
				}
			}
			"""
			},
			"initializer\n" +
			"prologue\n" +
			"epilogue");
	}

	// Initializers run in declaration order; later ones see earlier ones
	public void testValueClassECC_Conform_04() {
		runConformTest(new String[] {
			"X.java",
			"""
			public value class X {
				int a = 1;
				int b = a + 1;
				X() {
					System.out.println(a + " " + b);
					super();
				}
				public static void main(String[] args) {
					new X();
				}
			}
			"""
			},
			"1 2");
	}

	// Blank field written then read in prologue
	public void testValueClassECC_Conform_05() {
		runConformTest(new String[] {
			"X.java",
			"""
			public value class X {
				int f;
				X(int v) {
					f = v;
					System.out.println(f);
					super();
				}
				public static void main(String[] args) {
					new X(7);
				}
			}
			"""
			},
			"7");
	}

	// Blank field computed from an initialized field in prologue; value survives construction
	public void testValueClassECC_Conform_06() {
		runConformTest(new String[] {
			"X.java",
			"""
			public value class X {
				int x = 10;
				int y;
				X() {
					y = x * 2;
					System.out.println(this.y);
					super();
				}
				public static void main(String[] args) {
					System.out.println(new X().y);
				}
			}
			"""
			},
			"20\n" +
			"20");
	}

	// Blank field definitely assigned on all paths, then read
	public void testValueClassECC_Conform_07() {
		runConformTest(new String[] {
			"X.java",
			"""
			public value class X {
				int f;
				X(boolean b) {
					if (b)
						f = 1;
					else
						f = 2;
					System.out.println(f);
					super();
				}
				public static void main(String[] args) {
					new X(false);
				}
			}
			"""
			},
			"2");
	}

	// Fields (initialized and blank) read in the argument of super(..)
	public void testValueClassECC_Conform_08() {
		runConformTest(new String[] {
			"X.java",
			"""
			abstract value class Base {
				Base(int v) {
					System.out.println("base " + v);
				}
			}
			public value class X extends Base {
				int f = 3;
				int g;
				X() {
					g = f + 1;
					super(f + g);
				}
				public static void main(String[] args) {
					new X();
				}
			}
			"""
			},
			"base 7");
	}

	// Initializers run exactly once when construction goes through this(..)
	public void testValueClassECC_Conform_09() {
		runConformTest(new String[] {
			"X.java",
			"""
			public value class X {
				static int count;
				int f = count();
				static int count() {
					return ++count;
				}
				X() {
					this(0);
				}
				X(int i) {
					super();
				}
				public static void main(String[] args) {
					X x = new X();
					System.out.println(count + " " + x.f);
				}
			}
			"""
			},
			"1 1");
	}

	// A this-chaining constructor may read fields in its epilogue
	public void testValueClassECC_Conform_10() {
		runConformTest(new String[] {
			"X.java",
			"""
			public value class X {
				int f = 5;
				X() {
					this(1);
					System.out.println(f);
				}
				X(int i) {
					super();
				}
				public static void main(String[] args) {
					new X();
				}
			}
			"""
			},
			"5");
	}

	// Initialized fields of every type read in prologue (code generation)
	public void testValueClassECC_Conform_11() {
		runConformTest(new String[] {
			"X.java",
			"""
			public value class X {
				boolean b = true;
				char c = 'c';
				byte by = 1;
				short s = 2;
				int i = 3;
				long l = 4L;
				float fl = 5.5f;
				double d = 6.5;
				String str = "s";
				X() {
					System.out.println(b + " " + c + " " + by + " " + s + " " + i + " " + l + " " + fl + " " + d + " " + str);
					super();
				}
				public static void main(String[] args) {
					new X();
				}
			}
			"""
			},
			"true c 1 2 3 4 5.5 6.5 s");
	}

	// Blank fields of every type written then read in prologue (code generation)
	public void testValueClassECC_Conform_12() {
		runConformTest(new String[] {
			"X.java",
			"""
			public value class X {
				boolean b;
				char c;
				byte by;
				short s;
				int i;
				long l;
				float fl;
				double d;
				String str;
				X() {
					b = true;
					c = 'c';
					by = 1;
					s = 2;
					i = 3;
					l = 4L;
					fl = 5.5f;
					d = 6.5;
					str = "s";
					System.out.println(b + " " + c + " " + by + " " + s + " " + i + " " + l + " " + fl + " " + d + " " + str);
					super();
				}
				public static void main(String[] args) {
					X x = new X();
					System.out.println(x.l + " " + x.d + " " + x.str);
				}
			}
			"""
			},
			"true c 1 2 3 4 5.5 6.5 s\n" +
			"4 6.5 s");
	}

	// Initializer block runs in late construction: after super(), before epilogue
	public void testValueClassECC_Conform_13() {
		runConformTest(new String[] {
			"X.java",
			"""
			public value class X {
				int f = init();
				static int init() {
					System.out.println("initializer");
					return 1;
				}
				{
					System.out.println("block " + f);
				}
				X() {
					System.out.println("prologue");
					super();
					System.out.println("epilogue");
				}
				public static void main(String[] args) {
					new X();
				}
			}
			"""
			},
			"initializer\n" +
			"prologue\n" +
			"block 1\n" +
			"epilogue");
	}

	// Initializer block may read a field assigned in the prologue
	public void testValueClassECC_Conform_14() {
		runConformTest(new String[] {
			"X.java",
			"""
			public value class X {
				int f;
				{
					System.out.println(f);
				}
				X() {
					f = 7;
					super();
				}
				public static void main(String[] args) {
					new X();
				}
			}
			"""
			},
			"7");
	}

	// Each super-chaining constructor sees the initializers
	public void testValueClassECC_Conform_15() {
		runConformTest(new String[] {
			"X.java",
			"""
			public value class X {
				int f = 1;
				int g;
				X() {
					g = f + 1;
					super();
				}
				X(int i) {
					g = f + i;
					super();
				}
				public static void main(String[] args) {
					System.out.println(new X().g + " " + new X(10).g);
				}
			}
			"""
			},
			"2 11");
	}

	// Parameter shadows field: simple name binds to parameter, this.f to the initialized field
	public void testValueClassECC_Conform_16() {
		runConformTest(new String[] {
			"X.java",
			"""
			public value class X {
				int f = 1;
				X(int f) {
					System.out.println(f + " " + this.f);
					super();
				}
				public static void main(String[] args) {
					new X(2);
				}
			}
			"""
			},
			"2 1");
	}

	// Initialized field read repeatedly (loop) in prologue
	public void testValueClassECC_Conform_17() {
		runConformTest(new String[] {
			"X.java",
			"""
			public value class X {
				int f = 3;
				int g;
				X() {
					int sum = 0;
					for (int i = 0; i < f; i++)
						sum += f;
					g = sum;
					super();
				}
				public static void main(String[] args) {
					System.out.println(new X().g);
				}
			}
			"""
			},
			"9");
	}

	// Read of blank field before assignment
	public void testValueClassECC_Negative_01() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public value class X {
				int f;
				X() {
					System.out.println(f);
					f = 1;
					super();
				}
			}
			"""
			},
			"----------\n" +
			"1. WARNING in X.java (at line 1)\n" +
			"	public value class X {\n" +
			"	       ^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	System.out.println(f);\n" +
			"	                   ^\n" +
			"The blank final field f may not have been initialized\n" +
			"----------\n");
	}

	// Read of blank field after assignment on only one path
	public void testValueClassECC_Negative_02() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public value class X {
				int f;
				X(boolean b) {
					if (b)
						f = 1;
					System.out.println(f);
					super();
				}
			}
			"""
			},
			"----------\n" +
			"1. WARNING in X.java (at line 1)\n" +
			"	public value class X {\n" +
			"	       ^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 6)\n" +
			"	System.out.println(f);\n" +
			"	                   ^\n" +
			"The blank final field f may not have been initialized\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 7)\n" +
			"	super();\n" +
			"	^^^^^^^^\n" +
			"The field \'f\' must be initialized before chaining to the super class constructor\n" +
			"----------\n");
	}

	// Blank field assigned twice in prologue
	public void testValueClassECC_Negative_03() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public value class X {
				int f;
				X() {
					f = 1;
					f = 2;
					super();
				}
			}
			"""
			},
			"----------\n" +
			"1. WARNING in X.java (at line 1)\n" +
			"	public value class X {\n" +
			"	       ^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	f = 2;\n" +
			"	^\n" +
			"The final field f may already have been assigned\n" +
			"----------\n");
	}

	// Initialized field assigned again in prologue
	public void testValueClassECC_Negative_04() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public value class X {
				int f = 1;
				X() {
					f = 2;
					super();
				}
			}
			"""
			},
			"----------\n" +
			"1. WARNING in X.java (at line 1)\n" +
			"	public value class X {\n" +
			"	       ^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	f = 2;\n" +
			"	^\n" +
			"Cannot assign field 'f' in an early construction context, because it has an initializer\n" +
			"----------\n");
	}

	// Initialized field compound-assigned in prologue
	public void testValueClassECC_Negative_05() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public value class X {
				int f = 1;
				X() {
					this.f += 2;
					super();
				}
			}
			"""
			},
			"----------\n" +
			"1. WARNING in X.java (at line 1)\n" +
			"	public value class X {\n" +
			"	       ^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	this.f += 2;\n" +
			"	     ^\n" +
			"The final field X.f cannot be assigned\n" +
			"----------\n");
	}

	// Blank field compound-assigned before any assignment
	public void testValueClassECC_Negative_06() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public value class X {
				int f;
				X() {
					f += 1;
					super();
				}
			}
			"""
			},
			"----------\n" +
			"1. WARNING in X.java (at line 1)\n" +
			"	public value class X {\n" +
			"	       ^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	f += 1;\n" +
			"	^\n" +
			"The blank final field f may not have been initialized\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 4)\n" +
			"	f += 1;\n" +
			"	^\n" +
			"The final field X.f cannot be assigned\n" +
			"----------\n");
	}

	// Initializer reads a blank field: initializers run before the prologue assigns it
	public void testValueClassECC_Negative_07() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public value class X {
				int y;
				int x = y + 1;
				X() {
					y = 1;
					super();
				}
			}
			"""
			},
			"----------\n" +
			"1. WARNING in X.java (at line 1)\n" +
			"	public value class X {\n" +
			"	       ^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	int x = y + 1;\n" +
			"	        ^\n" +
			"The blank final field y may not have been initialized\n" +
			"----------\n");
	}

	// this(..)-chaining prologue may not read an initialized field (simple name)
	public void testValueClassECC_Negative_08() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public value class X {
				int f = 1;
				X() {
					int i = f;
					this(i);
				}
				X(int i) {
					super();
				}
			}
			"""
			},
			"----------\n" +
			"1. WARNING in X.java (at line 1)\n" +
			"	public value class X {\n" +
			"	       ^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	int i = f;\n" +
			"	        ^\n" +
			"Cannot refer to field f in an early construction context\n" +
			"----------\n");
	}

	// this(..)-chaining prologue may not read an initialized field (this-qualified)
	public void testValueClassECC_Negative_09() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public value class X {
				int f = 1;
				X() {
					int i = this.f;
					this(i);
				}
				X(int i) {
					super();
				}
			}
			"""
			},
			"----------\n" +
			"1. WARNING in X.java (at line 1)\n" +
			"	public value class X {\n" +
			"	       ^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	int i = this.f;\n" +
			"	        ^^^^^^\n" +
			"Cannot refer to field f in an early construction context\n" +
			"----------\n");
	}

	// this(..)-chaining prologue may not write a blank field
	public void testValueClassECC_Negative_10() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public value class X {
				int f;
				X() {
					f = 1;
					this(2);
				}
				X(int i) {
					f = i;
					super();
				}
			}
			"""
			},
			"----------\n" +
			"1. WARNING in X.java (at line 1)\n" +
			"	public value class X {\n" +
			"	       ^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	f = 1;\n" +
			"	^\n" +
			"Cannot refer to field f in an early construction context\n" +
			"----------\n");
	}

	// Field may not be referenced in the arguments of this(..)
	public void testValueClassECC_Negative_11() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public value class X {
				int f = 1;
				X() {
					this(f);
				}
				X(int i) {
					super();
				}
			}
			"""
			},
			"----------\n" +
			"1. WARNING in X.java (at line 1)\n" +
			"	public value class X {\n" +
			"	       ^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	this(f);\n" +
			"	     ^\n" +
			"Cannot refer to field f in an early construction context\n" +
			"----------\n");
	}

	// Blank field never assigned before super()
	public void testValueClassECC_Negative_12() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public value class X {
				int f;
				X() {
					super();
				}
			}
			"""
			},
			"----------\n" +
			"1. WARNING in X.java (at line 1)\n" +
			"	public value class X {\n" +
			"	       ^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	super();\n" +
			"	^^^^^^^^\n" +
			"The field 'f' must be initialized before chaining to the super class constructor\n" +
			"----------\n");
	}

	// Blank field assigned only after super() is too late
	public void testValueClassECC_Negative_13() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public value class X {
				int f;
				X() {
					super();
					f = 1;
				}
			}
			"""
			},
			"----------\n" +
			"1. WARNING in X.java (at line 1)\n" +
			"	public value class X {\n" +
			"	       ^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	super();\n" +
			"	^^^^^^^^\n" +
			"The field 'f' must be initialized before chaining to the super class constructor\n" +
			"----------\n");
	}

	// Initializer-side assignment + prologue write in this(..) target still counts once:
	// initialized field is not re-assignable in the super-chaining target
	public void testValueClassECC_Negative_14() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public value class X {
				int f = 1;
				X() {
					this(2);
				}
				X(int i) {
					f = i;
					super();
				}
			}
			"""
			},
			"----------\n" +
			"1. WARNING in X.java (at line 1)\n" +
			"	public value class X {\n" +
			"	       ^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 7)\n" +
			"	f = i;\n" +
			"	^\n" +
			"Cannot assign field 'f' in an early construction context, because it has an initializer\n" +
			"----------\n");
	}

	// Lambda in prologue may not read a field
	public void testValueClassECC_Negative_15() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public value class X {
				int f;
				X() {
					f = 1;
					Runnable r = () -> System.out.println(f);
					super();
				}
			}
			"""
			},
			"----------\n" +
			"1. WARNING in X.java (at line 1)\n" +
			"	public value class X {\n" +
			"	       ^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 5)\n" +
			"	Runnable r = () -> System.out.println(f);\n" +
			"	                                      ^\n" +
			"Cannot refer to field f in an early construction context\n" +
			"----------\n");
	}

	// Initializer block may not assign a blank field
	public void testValueClassECC_Negative_16() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public value class X {
				int f;
				{
					f = 1;
				}
				X() {
					f = 2;
					super();
				}
			}
			"""
			},
			"----------\n" +
			"1. WARNING in X.java (at line 1)\n" +
			"	public value class X {\n" +
			"	       ^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	f = 1;\n" +
			"	^\n" +
			"The final field X.f cannot be assigned\n" +
			"----------\n");
	}

	// Initializer block may not assign an initialized field
	public void testValueClassECC_Negative_17() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public value class X {
				int f = 1;
				{
					this.f = 2;
				}
				X() {
					super();
				}
			}
			"""
			},
			"----------\n" +
			"1. WARNING in X.java (at line 1)\n" +
			"	public value class X {\n" +
			"	       ^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	this.f = 2;\n" +
			"	     ^\n" +
			"The final field X.f cannot be assigned\n" +
			"----------\n");
	}

	// Initializer block may not assign a blank field only there (not a substitute for the prologue)
	public void testValueClassECC_Negative_18() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public value class X {
				int f;
				{
					f = 1;
				}
				X() {
					super();
				}
			}
			"""
			},
			"----------\n" +
			"1. WARNING in X.java (at line 1)\n" +
			"	public value class X {\n" +
			"	       ^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	f = 1;\n" +
			"	^\n" +
			"The final field X.f cannot be assigned\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 7)\n" +
			"	super();\n" +
			"	^^^^^^^^\n" +
			"The field 'f' must be initialized before chaining to the super class constructor\n" +
			"----------\n");
	}

	public void testStrictFieldInitializationWithSyntheticDefaultConstructor() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
				value class X {
					int f;
				}
				"""
			},
			"----------\n" +
			"1. WARNING in X.java (at line 1)\n" +
			"	value class X {\n" +
			"	^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 1)\n" +
			"	value class X {\n" +
			"	            ^\n" +
			"The field \'f\' must be initialized before chaining to the super class constructor\n" +
			"----------\n");
	}

	public void testIdentityRecordWithUninitializedComponent() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				record X(int value) {
					public X(int value) {
					}
				}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 2)\n" +
			"	public X(int value) {\n" +
			"	       ^^^^^^^^^^^^\n" +
			"The field \'value\' must be initialized before chaining to the super class constructor\n" +
			"----------\n");
	}

	public void testAnnotatedSuperConstructorTypeArgument() throws Exception {
		runConformTest(new String[] {
			"X.java",
			"""
			import java.lang.annotation.ElementType;
			import java.lang.annotation.Retention;
			import java.lang.annotation.RetentionPolicy;
			import java.lang.annotation.Target;

			@Target(ElementType.TYPE_USE)
			@Retention(RetentionPolicy.RUNTIME)
			@interface A {}

			class Base {
				<T> Base(T value) {}
			}

			public class X extends Base {
				X() {
					<@A String>super("ok");
				}
				void main() {}
			}
			"""
		}, "");

		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(
			new java.io.File(OUTPUT_DIR, "X.class"));
		String actualOutput = org.eclipse.jdt.core.ToolFactory.createDefaultClassFileBytesDisassembler()
			.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.SYSTEM);

		assertTrue("Missing annotated constructor type argument:\n" + actualOutput,
			java.util.regex.Pattern.compile(
				"@A\\(\\R\\s*target type = 0x48 CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT"
				+ "\\R\\s*offset = \\d+\\R\\s*type argument index = 0")
				.matcher(actualOutput).find());
	}

	public void testFalseProxy() throws Exception {
		runNegativeTest(new String[] {
			"X.java",
			"""
			value class X {
			    int f;

			    X() {
			        int f = 1;
			        System.out.println(f);
			        super();
			    }
			}
			"""
		},
		"----------\n" +
		"1. WARNING in X.java (at line 1)\n" +
		"	value class X {\n" +
		"	^^^^^\n" +
		"You are using a preview language feature that may or may not be supported in a future release\n" +
		"----------\n" +
		"2. WARNING in X.java (at line 5)\n" +
		"	int f = 1;\n" +
		"	    ^\n" +
		"The local variable f is hiding a field from type X\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 7)\n" +
		"	super();\n" +
		"	^^^^^^^^\n" +
		"The field \'f\' must be initialized before chaining to the super class constructor\n" +
		"----------\n");
	}

	public void testPrematureProxy() throws Exception {
		runConformTest(new String[] {
			"X.java",
			"""
			class S {
			    S() {
			        init();
			    }
			    void init() {}
			}

			public class X extends S {
			    int x;

			    X() {
			        int x = 1;      // local shadows the field
			        int y = x;      // reads the LOCAL, not the field
			        if (y != 1)
			            throw new AssertionError(y);
			        super();
			    }

			    @Override
			    void init() {
			        this.x = 42;    // field written during super()
			    }

			    public static void main(String[] args) {
			        System.out.println(new X().x);
			    }
			}
			"""
		},
		"42");
	}

	public void testProxyDoesntLeakIntoLambdaOrLocalClass() throws Exception {
		runNegativeTest(new String[] {
			"X.java",
			"""
			import java.util.function.Supplier;

			public class X {
				int f;

				X() {
					int early = f; // force proxy

					Supplier<Integer> s = () -> f;
					class L {
						L() {
							int e = f;
						}

						int get() {
							return f;
						}
					}

					int a = s.get();
					int b = new L().get();
					if (a != 0 || b != 0) {
						throw new AssertionError(a + ":" + b);
					}
					super();
				}

				public static void main(String[] args) {
					new X();
				}
			}
			"""
		},
		"----------\n" +
		"1. ERROR in X.java (at line 9)\n" +
		"	Supplier<Integer> s = () -> f;\n" +
		"	                            ^\n" +
		"Cannot refer to field f in an early construction context\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 12)\n" +
		"	int e = f;\n" +
		"	        ^\n" +
		"Cannot refer to field f in an early construction context\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 16)\n" +
		"	return f;\n" +
		"	       ^\n" +
		"Cannot refer to field f in an early construction context\n" +
		"----------\n");
	}

	public void testInheritedFieldReadInEarlyConstructionContext() {
		runNegativeTest(
			new String[] {
				"X.java",
				"""
				class S {
				    int inherited;
				}

				public class X extends S {
				    int own;

				    X() {
				        int a = inherited; // inherited field: should be rejected
				        int b = own;       // current-class field
				        super();
				    }
				}
				"""
			},
			"----------\n" +
			"1. ERROR in X.java (at line 9)\n" +
			"	int a = inherited; // inherited field: should be rejected\n" +
			"	        ^^^^^^^^^\n" +
			"Cannot refer to field inherited in an early construction context\n" +
			"----------\n"
		);
	}

	public void testUnreachableConstructorCallDoesNotLeakProxies() {
		runConformTest(new String[] {
			"X.java",
			"""
			public value class X {
			    int f = 42;

			    X() {
			        int early = f;
			        if (true) {
			            throw new RuntimeException();
			        }
			        super();
			    }

			    X(int value) {
			        super();
			        System.out.println(f);
			    }

			    public static void main(String[] args) {
			        new X(1);
			    }
			}
			"""
		}, "42");
	}

	public void testProxyFlushDoesNotClobberWritesDuringSuper() {
		runConformTest(new String[] {
			"X.java",
			"""
			class S {
			    S() {
			        init();
			    }
			    void init() {}
			}

			public class X extends S {
			    int x;
			    int y = 99;
			    X() {
			        x = 1;
			        int y = x;      // reads the LOCAL, not the field
			        if (y != 1)
			            throw new AssertionError(y);
			        super();
			    }

			    @Override
			    void init() {
			        this.x = 42;    // field written during super()
			        this.y = 24;    // field written during super()
			    }

			    public static void main(String[] args) {
			        X x = new X();
			        System.out.println(x.x);
			        System.out.println(x.y);
			    }
			}
			"""
		},
		"42\n99");
	}

	public void testValueFieldWithInstanceMethodInvocationInitializer() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public value class X {
				int a = foo();
				int b = a + c;
				int c = sfoo();
				X() {
					System.out.println(a + " " + b);
					super();
				}
				public static void main(String[] args) {
					new X();
				}
				int foo() {
					return 99;
				}
				static int sfoo() {
				    return 44;
				}
			}
			"""
		},
		"----------\n" +
		"1. WARNING in X.java (at line 1)\n" +
		"	public value class X {\n" +
		"	       ^^^^^\n" +
		"You are using a preview language feature that may or may not be supported in a future release\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 2)\n" +
		"	int a = foo();\n" +
		"	        ^^^^^\n" +
		"Cannot invoke method foo() in an early construction context\n" +
		"----------\n" +
		"3. ERROR in X.java (at line 3)\n" +
		"	int b = a + c;\n" +
		"	            ^\n" +
		"Cannot reference a field before it is defined\n" +
		"----------\n");
	}

	public void testValueFieldWithStaticMethodInvocationInitializer() {
		runConformTest(new String[] {
			"X.java",
			"""
			public value class X {
				int a = sfoo();
				int b = a + 10;
				int c = a + b;
				X() {
					System.out.println(a + " " + b + " " + c);
					super();
				}
				public static void main(String[] args) {
					new X();
				}
				int foo() {
					return 99;
				}
				static int sfoo() {
				    return 44;
				}
			}
			"""
		},
				"44 54 98");
	}

	public void testNoVerifyErrorOnQualifiedThis() {
		runConformTest(new String[] {
			"X.java",
			"""
			class ProblemReporter {
				public void record() {
				}
			}

			interface ISourceElementRequestor {
				void acceptProblem(String problem);
			}

			public class X {
				ISourceElementRequestor requestor;
				ProblemReporter problemReporter;

				public X() {
					this.requestor = p -> System.out.println(p);
					this.problemReporter = new ProblemReporter() {
						@Override
						public void record() {
							X.this.requestor.acceptProblem("No Problem : 1");
						}
					};
					X.this.requestor.acceptProblem("ReferenceOfFieldOfThis"); // only for this we should build a ReferenceOfFieldOfThis
				}

				public String toString() {
					return "Some X";
				}

				void foo() {
					X.this.requestor.acceptProblem("No Problem : 3");
				}

				void X() {
					X.this.requestor.acceptProblem("No Problem : 2");
				}

				public static void main(String[] args) {
					new X().problemReporter.record();
					new X().X();
					new X().foo();
				}
			}
			"""
		},
		"ReferenceOfFieldOfThis\n" +
		"No Problem : 1\n" +
		"ReferenceOfFieldOfThis\n" +
		"No Problem : 2\n" +
		"ReferenceOfFieldOfThis\n" +
		"No Problem : 3");
	}

	public void testThrowInValueInitBlock() {
		runConformTest(new String[] {
			"X.java",
			"""
			public value class X {
			    int f;
			    int g = 1;

			    X() {
			        f = 10;
			        System.out.println("init block marker");
			        super();
			        System.out.println(f + g);
			    }

			    {
			        System.out.println("initializer");
			        if (true) {
			            throw new RuntimeException("boom");
			        }
			    }

			    public static void main(String[] args) {
			        try {
			            new X();
			        } catch (RuntimeException e) {
			            System.out.println(e.getMessage());
			        }
			    }
			}
			"""
		},
		"init block marker\n" +
		"initializer\n" +
		"boom");
	}

	// Value class initializer blocks run immediately before the constructor epilogue;
	// an initializer that completes abruptly must render the epilogue unreachable.
	public void testInitializerFlowInfoReachesEpilogue() {
		runNegativeTest(new String[] {
			"X.java",
			"""
			public value class X {
				int f = 1;
				{
					throw new RuntimeException("boom");
				}
				X() {
					super();
					System.out.println(f);
				}
			}
			"""
			},
			"----------\n" +
			"1. WARNING in X.java (at line 1)\n" +
			"	public value class X {\n" +
			"	       ^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 3)\n" +
			"	{\n" +
			"		throw new RuntimeException(\"boom\");\n" +
			"	}\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Initializer does not complete normally\n" +
			"----------\n" +
			"3. WARNING in X.java (at line 8)\n" +
			"	System.out.println(f);\n" +
			"	^^^^^^^^^^^^^^^^^^^^^\n" +
			"Dead code\n" +
			"----------\n");
	}

	public void testDeadCodeAfterThrowingInitializerBlock() {
		Map<String, String> options = getCompilerOptions(true);
		options.put(CompilerOptions.OPTION_ReportDeadCode, CompilerOptions.ERROR);

		runNegativeTest(
			new String[] {
				"X.java",
				"""
				public value class X {
					int f = 1;

					{
						if (true)
							throw new RuntimeException("boom");
					}

					X() {
						super();
						System.out.println(f);
					}

					public static void main(String[] args) {
						new X();
					}
				}
				"""
			},
			"----------\n" +
			"1. WARNING in X.java (at line 1)\n" +
			"\tpublic value class X {\n" +
			"\t       ^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 11)\n" +
			"\tSystem.out.println(f);\n" +
			"\t^^^^^^^^^^^^^^^^^^^^^\n" +
			"Dead code\n" +
			"----------\n",
			null,
			false,
			options);
	}

	public void testECC_proxy_slot_isolation_01() {
		runConformTest(new String[] {
			"X.java",
			"""
			public value class X {
				int a = 1;
				int b = a + 1;

				X(int p) {
					int local = p;
					super();
				}

				public static void main(String[] args) {
					X x = new X(7);
					System.out.println(x.a);
					System.out.println(x.b);
				}
			}
			""",
		}, "1\n2");
	}

	public void testECC_proxy_slot_isolation_02() {
		runConformTest(new String[] {
			"X.java",
			"""
			public value class X {
				int a = 1;
				int b = a + 1;

				X(int p) {
					int local = p + 40;
					super();
				}

				public static void main(String[] args) {
					System.out.println(new X(7).a);
				}
			}
			""",
		}, "1");
	}

	public void testECC_legal_read_of_declared_field() {
		runConformTest(new String[] {
			"X.java",
			"""
			public class X {
				int x;

				X() {
					x = 10;
					int y = x;
					super();
					System.out.println(y);
				}

				public static void main(String[] args) {
					new X();
				}
			}
			""",
		}, "10");
	}

	public void testECC_legal_read_of_this_field() {
		runConformTest(new String[] {
			"X.java",
			"""
			public class X {
				int x;

				X() {
					x = 42;
					int y = this.x;
					super();
					System.out.println(y);
				}

				public static void main(String[] args) {
					new X();
				}
			}
			""",
		}, "42");
	}

	public void testECC_legal_read_in_expression_and_assignment() {
		runConformTest(new String[] {
			"X.java",
			"""
			public class X {
				int x;
				int y;

				X() {
					x = 5;
					y = x + 2;
					super();
					System.out.println(y);
				}

				public static void main(String[] args) {
					new X();
				}
			}
			""",
		}, "7");
	}

	public void testImportedInitializerProxyAliasingReproducer() {
		runConformTest(new String[] {
			"X.java",
			"""
			public value class X {
				int a = 1;
				int b = a + 1; // initializer reads a, so constructor imports proxy for a

				X(int p) {
					int observed = a;   // keep imported proxy for a live in constructor
					int local = p + 40; // candidate competing slot
					super();
					System.out.println(a);
					System.out.println(b);
					System.out.println(observed);
					System.out.println(local);
				}

				public static void main(String[] args) {
					X x = new X(7);
					System.out.println(x.a);
					System.out.println(x.b);
				}
			}
			"""
		}, "1\n2\n1\n47\n1\n2");
	}

	public void testImportedInitializerProxyDoesNotAliasNarrowConstructorLocal() {
		runConformTest(new String[] {
			"X.java",
			"""
			public value class X {
				long a = 1L;
				long b = a + 1L; // forces an imported proxy for long field a

				X(int parameter) {
					int local = parameter + 40; // one-slot local; must not overlap long proxy
					super();

					// Keep the constructor local live across the proxy flush.
					System.out.println("local=" + local);
				}

				public static void main(String[] args) {
					X x = new X(7);
					System.out.println("a=" + x.a);
					System.out.println("b=" + x.b);
				}
			}
			"""
		}, "local=47\na=1\nb=2");
	}

	public void testImportedInitializerProxyDoesNotAliasWideConstructorLocal() {
		runConformTest(new String[] {
			"X.java",
			"""
			public value class X {
				int a = 1;
				int b = a + 1; // forces an imported proxy for int field a

				X(int parameter) {
					long local = parameter + 40L; // two-slot local
					super();

					System.out.println("local=" + local);
				}

				public static void main(String[] args) {
					X x = new X(7);
					System.out.println("local-check=" + 47L);
					System.out.println("a=" + x.a);
					System.out.println("b=" + x.b);
				}
			}
			"""
		},
		"local=47\n" +
		"local-check=47\n" +
		"a=1\n" +
		"b=2");
	}

	public void testProxyCreationResumesAfterLambdaDetour() {
		runConformTest(new String[] {
			"X.java",
			"""
			interface I { int get(); }

			public class X {
			    int a;
			    int b;

			    X() {
			        a = 1;
			        I i = () -> 0;   // detour out and back
			        int x = b;       // first read of b after return: must still get proxy
			        super();
			        System.out.println(x);
			    }

			    public static void main(String[] args) {
			        new X();
			    }
			}
			"""
		}, "0");
	}

	public void testProxyCreationResumesAfterLocalClassDetour() {
		runConformTest(new String[] {
			"X.java",
			"""
			public class X {
			    int a;
			    int b;

			    X() {
			        a = 1;
			        class L {
			            int get() { return 0; }
			        }
			        int x = b;   // first read of b after detour: must still get proxy
			        super();
			        System.out.println(x);
			    }

			    public static void main(String[] args) {
			        new X();
			    }
			}
			"""
		}, "0");
	}

	public void testValueFieldInitializerProxyCreationResumesAfterLambdaDetour() {
		runConformTest(new String[] {
			"X.java",
			"""
			interface I { int get(); }

			public value class X {
			    int a = 1;
			    int b = ((java.util.function.IntSupplier) (() -> 0)).getAsInt() + a;

			    X() {
			        System.out.println(b);
			        super();
			    }

			    public static void main(String[] args) {
			        new X();
			    }
			}
			"""
		}, "1");
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/5327
	// javac 28b16 seems to have started to set this bit.
	public void testValueFieldInitializerProxyCreationResumesAfterAnonymousClassDetour() {
		runConformTest(new String[] {
			"X.java",
			"""
			interface I { int get(); }

			public value class X {
			    int a = 1;
			    int b = (new I() {
			        @Override
			        public int get() { return 0; }
			    }).get() + a;

			    X() {
			        System.out.println(b);
			        super();
			    }

			    public static void main(String[] args) {
			        new X();
			    }
			}
			"""
		}, "1");
	}

	public void testDeadCodeAfterThrowingInitializerBlock_AllConstructors_NoRepeatedInitializerAnalysis() {
		Map<String, String> options = getCompilerOptions(true);
		options.put(CompilerOptions.OPTION_ReportDeadCode, CompilerOptions.ERROR);

		runNegativeTest(
			new String[] {
				"X.java",
				"""
				public value class X {
					int f = 1;

					{
						throw new RuntimeException("boom");
					}

					X() {
						super();
						System.out.println("ctor0");
					}

					X(int i) {
						super();
						System.out.println("ctor1");
					}
				}
				"""
			},
			"----------\n" +
			"1. WARNING in X.java (at line 1)\n" +
			"	public value class X {\n" +
			"	       ^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 4)\n" +
			"	{\n" +
			"		throw new RuntimeException(\"boom\");\n" +
			"	}\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Initializer does not complete normally\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 10)\n" +
			"	System.out.println(\"ctor0\");\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Dead code\n" +
			"----------\n" +
			"4. ERROR in X.java (at line 15)\n" +
			"	System.out.println(\"ctor1\");\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Dead code\n" +
			"----------\n",
			null,
			false,
			options);
	}

	public void _testAliasingWithInnerScopes() {
		runConformTest(new String[] {
			"X.java",
			"""
			public value class X {
			    int a = 1;
			    int b = a + 1;        // initializer reads a → proxy for a is created and imported

			    X(int p) {
			        if (p > 0) {
			            int t = p + 40;   // nested-block local: may get a's proxy slot
			            System.out.println(t);
			        }
			        super();              // flushProxies: putfield a ← slot (47?)
			    }

			    public static void main(String[] args) {
			        X x = new X(7);
			        System.out.println(x.a + " " + x.b);
			    }
			}
			"""
		}, "1");
	}

	public void testDeadCodeAfterSecondThrowingInitializerBlock_AllConstructors() {
		Map<String, String> options = getCompilerOptions(true);
		options.put(CompilerOptions.OPTION_ReportDeadCode, CompilerOptions.ERROR);

		runNegativeTest(
			new String[] {
				"X.java",
				"""
				public value class X {
					int f = 1;

					{
						System.out.println("first");
					}

					{
						if (true)
							throw new RuntimeException("boom");
					}

					X() {
						super();
						System.out.println("ctor0");
					}

					X(int i) {
						super();
						System.out.println("ctor1");
					}
				}
				"""
			},
			"----------\n" +
			"1. WARNING in X.java (at line 1)\n" +
			"	public value class X {\n" +
			"	       ^^^^^\n" +
			"You are using a preview language feature that may or may not be supported in a future release\n" +
			"----------\n" +
			"2. ERROR in X.java (at line 15)\n" +
			"	System.out.println(\"ctor0\");\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Dead code\n" +
			"----------\n" +
			"3. ERROR in X.java (at line 20)\n" +
			"	System.out.println(\"ctor1\");\n" +
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
			"Dead code\n" +
			"----------\n",
			null,
			false,
			options);
	}

	public void testValueClassECC_Negative_duplicateBlankFinalAssignment() {
	    runNegativeTest(new String[] {
	        "X.java",
	        """
	        public value class X {
	            final int f;

	            X() {
	                f = 1;
	                f = 2;
	                System.out.println(f);
	                super();
	            }

	            public static void main(String[] args) {
	                new X();
	            }
	        }
	        """
	    },
		"----------\n" +
		"1. WARNING in X.java (at line 1)\n" +
		"	public value class X {\n" +
		"	       ^^^^^\n" +
		"You are using a preview language feature that may or may not be supported in a future release\n" +
		"----------\n" +
		"2. ERROR in X.java (at line 6)\n" +
		"	f = 2;\n" +
		"	^\n" +
		"The final field f may already have been assigned\n" +
		"----------\n");
	}

	public void testValueClassECC_Conform_importedInitializerProxyRead() {
	    runConformTest(new String[] {
	        "X.java",
	        """
	        public value class X {
	            int a = 1;
	            int b = a + 1; // initializer reads a, so the constructor imports a proxy for a

	            X() {
	                System.out.println(this.a);
	                super();
	            }

	            public static void main(String[] args) {
	                new X();
	            }
	        }
	        """
	    }, "1");
	}

	public void testValueRecord() {
	    runConformTest(new String[] {
	        "Point.java",
	        """
			public value record Point(int x, int y) {
				public Point(int x, int y) {
					this.y = y;
					this.x = this.y;
				}

				public static void main(String[] args) {
					System.out.println(new Point(512, 1024));
				}
			}
	        """
	    }, "Point[x=1024, y=1024]");
	}

	public void testSharedProxyIdCollisionAcrossConstructors() {
	    runNegativeTest(new String[] {
	        "X.java",
	        """
	        public value class X {
	        	int a = 1;
	        	int b = a + 1;
	        	int c;
	        	X() {
	        		System.out.println(c);
	        		super();
	        	}
	        	X(int p) {
	        		c = p;
	        		super();
	        	}
	        }
	        """
	    },
	    "----------\n" +
	    "1. WARNING in X.java (at line 1)\n" +
	    "	public value class X {\n" +
	    "	       ^^^^^\n" +
	    "You are using a preview language feature that may or may not be supported in a future release\n" +
	    "----------\n" +
	    "2. ERROR in X.java (at line 6)\n" +
	    "	System.out.println(c);\n" +
	    "	                   ^\n" +
	    "The blank final field c may not have been initialized\n" +
	    "----------\n" +
	    "3. ERROR in X.java (at line 7)\n" +
	    "	super();\n" +
	    "	^^^^^^^^\n" +
	    "The field 'c' must be initialized before chaining to the super class constructor\n" +
	    "----------\n");
	}

public void testValueCompactConstructorAssignmentWithProxyBypass() {
    runNegativeTest(new String[] {
        "Point.java",
        """
        public value record Point(int x, int y) {
            public Point {
                this.x = 10;           // explicit assignment in compact constructor
                int read = this.x;     // read forces proxy creation
                this.y = 20;           // second assignment
            }
        }
        """
    },
	"----------\n" +
	"1. WARNING in Point.java (at line 1)\n" +
	"	public value record Point(int x, int y) {\n" +
	"	       ^^^^^\n" +
	"You are using a preview language feature that may or may not be supported in a future release\n" +
	"----------\n" +
	"2. ERROR in Point.java (at line 3)\n" +
	"	this.x = 10;           // explicit assignment in compact constructor\n" +
	"	^^^^^^\n" +
	"Illegal explicit assignment of a final field x in compact constructor\n" +
	"----------\n" +
	"3. ERROR in Point.java (at line 4)\n" +
	"	int read = this.x;     // read forces proxy creation\n" +
	"	                ^\n" +
	"The blank final field x may not have been initialized\n" +
	"----------\n" +
	"4. ERROR in Point.java (at line 5)\n" +
	"	this.y = 20;           // second assignment\n" +
	"	^^^^^^\n" +
	"Illegal explicit assignment of a final field y in compact constructor\n" +
	"----------\n");
}
 }