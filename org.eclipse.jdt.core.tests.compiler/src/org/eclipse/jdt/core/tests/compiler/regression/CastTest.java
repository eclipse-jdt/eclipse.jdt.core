/*******************************************************************************
 * Copyright (c) 2003, 2020 IBM Corporation and others.
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
 *     Stephan Herrmann - Contributions for
 *								Bug 428274 - [1.8] [compiler] Cannot cast from Number to double
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest.JavacTestOptions.Excuse;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class CastTest extends AbstractRegressionTest {

public CastTest(String name) {
	super(name);
}
@Override
protected Map getCompilerOptions() {
	Map defaultOptions = super.getCompilerOptions();
	defaultOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.WARNING);
	defaultOptions.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	return defaultOptions;
}
public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}

static {
//	TESTS_NAMES = new String[] { "test428388d" };
}
/*
 * check extra checkcast (interface->same interface)
 */
public void test001() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {\t
				    public static void main(String[] args) {\t
				       Cloneable c1 = new int[0];\s
						Cloneable c2 = (Cloneable)c1;\s
						System.out.print("SUCCESS");\t
				    }\t
				}\t
				""",
		},
		"SUCCESS");

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);

	String expectedOutput =
		"""
		  // Method descriptor #15 ([Ljava/lang/String;)V
		  // Stack: 2, Locals: 3
		  public static void main(java.lang.String[] args);
		     0  iconst_0
		     1  newarray int [10]
		     3  astore_1 [c1]
		     4  aload_1 [c1]
		     5  astore_2 [c2]
		     6  getstatic java.lang.System.out : java.io.PrintStream [16]
		     9  ldc <String "SUCCESS"> [22]
		    11  invokevirtual java.io.PrintStream.print(java.lang.String) : void [24]
		    14  return
		      Line numbers:
		        [pc: 0, line: 3]
		        [pc: 4, line: 4]
		        [pc: 6, line: 5]
		        [pc: 14, line: 6]
		      Local variable table:
		        [pc: 0, pc: 15] local: args index: 0 type: java.lang.String[]
		        [pc: 4, pc: 15] local: c1 index: 1 type: java.lang.Cloneable
		        [pc: 6, pc: 15] local: c2 index: 2 type: java.lang.Cloneable
		""";
	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 2));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, actualOutput);
	}
}

public void test002() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						// standard expressions
						String s = (String) null;	// UNnecessary
						String t = (String) "hello";	// UNnecessary
						float f = (float) 12;			// UNnecessary
						int i = (int)12.0;				//   necessary
					}
				}
				"""
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		"""
			----------
			1. ERROR in X.java (at line 4)
				String s = (String) null;	// UNnecessary
				           ^^^^^^^^^^^^^
			Unnecessary cast from null to String
			----------
			2. ERROR in X.java (at line 5)
				String t = (String) "hello";	// UNnecessary
				           ^^^^^^^^^^^^^^^^
			Unnecessary cast from String to String
			----------
			3. ERROR in X.java (at line 6)
				float f = (float) 12;			// UNnecessary
				          ^^^^^^^^^^
			Unnecessary cast from int to float
			----------
			""",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
public void test003() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						// message sends	\t
						foo((Object) "hello");		//   necessary
						foo((String) "hello");			// UNnecessary
						foo((Object) null);			//   necessary
						foo((String) null);				// UNnecessary but keep as useful documentation\s
					}
					static void foo(String s) {
						System.out.println("foo(String):"+s);
					}
					static void foo(Object o) {
						System.out.println("foo(Object):"+o);
					}
				}
				"""
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		"""
			----------
			1. ERROR in X.java (at line 5)
				foo((String) "hello");			// UNnecessary
				    ^^^^^^^^^^^^^^^^
			Unnecessary cast from String to String
			----------
			""",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
public void test004() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						// constructors
						new X((Object) "hello");	//   necessary
						new X((String) "hello");	// UNnecessary
						new X((Object) null);		//   necessary
						new X((String) null);		// UNnecessary but keep as useful documentation
					}
					X(){}
					X(String s){
						System.out.println("new X(String):"+s);
					}
					X(Object o){
						System.out.println("new X(Object):"+o);
					}
				}
				"""
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		"""
			----------
			1. ERROR in X.java (at line 5)
				new X((String) "hello");	// UNnecessary
				      ^^^^^^^^^^^^^^^^
			Unnecessary cast from String to String
			----------
			""",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
public void test005() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						// qualified allocations
						new X().new XM3((Object) "hello");	//   necessary
						new X().new XM3((String) "hello");	// UNnecessary
						new X().new XM3((Object) null);		//   necessary
						new X().new XM3((String) null);		// UNnecessary but keep as useful documentation
						new X().new XM3((Object) "hello"){};	//   necessary
						new X().new XM3((String) "hello"){};	// UNnecessary
						new X().new XM3((Object) null){};		//   necessary
						new X().new XM3((String) null){};		// UNnecessary but keep as useful documentation
					}
					X(){}
					static class XM1 extends X {}
					static class XM2 extends X {}
					class XM3 {
						XM3(String s){
							System.out.println("new XM3(String):"+s);
						}
						XM3(Object o){
							System.out.println("new XM3(Object):"+o);
						}
					}\t
				}
				"""
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		"""
				----------
				1. ERROR in X.java (at line 5)
					new X().new XM3((String) "hello");	// UNnecessary
					                ^^^^^^^^^^^^^^^^
				Unnecessary cast from String to String
				----------
				2. ERROR in X.java (at line 9)
					new X().new XM3((String) "hello"){};	// UNnecessary
					                ^^^^^^^^^^^^^^^^
				Unnecessary cast from String to String
				----------
				""",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
public void _test006() { // TODO (philippe) add support to conditional expression for unnecessary cast
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						// ternary operator
						String s = null, t = null;\t
						X x0 = s == t
							? (X)new XM1()			// UNnecessary
							: new X();
						X x1 = s == t\s
							? (X)new XM1()			//   necessary
							: new XM2();
						X x2 = s == t\s
							? new XM1()
							: (X)new XM2();			//   necessary
						X x3 = s == t\s
							? (X)new XM1()			//   necessary
							: (X)new XM2();			//   necessary
					}
					X(){}
					static class XM1 extends X {}
					static class XM2 extends X {}
				}
				"""
		},
		"x",
		null,
		true,
		customOptions);
}

public void test007() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
					X(){}
					class XM3 {
						XM3(String s){
							System.out.println("new XM3(String):"+s);
						}
						XM3(Object o){
							System.out.println("new XM3(Object):"+o);
						}
					}\t
				\t
					class XM4 extends XM3 {
						XM4(String s){
							super((Object) s); // necessary
							System.out.println("new XM4(String):"+s);
						}
						XM4(Object o){
							super((String) o); // necessary
							System.out.println("new XM4(Object):"+o);
						}
						XM4(Thread t){
							super((Object) t); // UNnecessary
							System.out.println("new XM4(Thread):"+t);
						}
						XM4(){
							super((String)null); // UNnecessary but keep as useful documentation
							System.out.println("new XM4():");
						}
						XM4(int i){
							super((Object)null); // necessary
							System.out.println("new XM4():");
						}
					}
				}
				"""
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		"""
			----------
			1. ERROR in X.java (at line 22)
				super((Object) t); // UNnecessary
				      ^^^^^^^^^^
			Unnecessary cast from Thread to Object
			----------
			""",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
public void test008() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						boolean b1 = new XM1() instanceof X; // UNnecessary
						boolean b2 = new X() instanceof XM1; // necessary
						boolean b3 = null instanceof X;
					}
					static class XM1 extends X {}
				}
				"""
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		"""
			----------
			1. ERROR in X.java (at line 3)
				boolean b1 = new XM1() instanceof X; // UNnecessary
				             ^^^^^^^^^^^^^^^^^^^^^^
			The expression of type X.XM1 is already an instance of type X
			----------
			""",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
public void test009() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						boolean b1 = ((X) new XM1()) == new X(); // UNnecessary
						boolean b2 = ((X) new XM1()) == new XM2(); // necessary
						boolean b3 = ((X) null) == new X(); // UNnecessary
					}
					static class XM1 extends X {}
					static class XM2 extends X {}
				}
				"""
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		"""
			----------
			1. ERROR in X.java (at line 3)
				boolean b1 = ((X) new XM1()) == new X(); // UNnecessary
				             ^^^^^^^^^^^^^^^
			Unnecessary cast from X.XM1 to X
			----------
			2. ERROR in X.java (at line 5)
				boolean b3 = ((X) null) == new X(); // UNnecessary
				             ^^^^^^^^^^
			Unnecessary cast from null to X
			----------
			""",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
public void test010() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						long l1 = ((long) 1) + 2L; // UNnecessary
						long l2 = ((long)1) + 2; // necessary
						long l3 = 0;\
						l3 += (long)12; // UNnecessary
					}
				}
				"""
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		"""
			----------
			1. ERROR in X.java (at line 3)
				long l1 = ((long) 1) + 2L; // UNnecessary
				          ^^^^^^^^^^
			Unnecessary cast from int to long
			----------
			2. ERROR in X.java (at line 5)
				long l3 = 0;		l3 += (long)12; // UNnecessary
				            		      ^^^^^^^^
			Unnecessary cast from int to long
			----------
			""",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
public void test011() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						String s1 = ((long) 1) + "hello"; // necessary
						String s2 = ((String)"hello") + 2; // UNnecessary
						String s3 = ((String)null) + null; // necessary
						String s4 = ((int) (byte)1) + "hello"; // necessary
					}
				}
				"""
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		"""
			----------
			1. ERROR in X.java (at line 4)
				String s2 = ((String)"hello") + 2; // UNnecessary
				            ^^^^^^^^^^^^^^^^^
			Unnecessary cast from String to String
			----------
			""",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
public void test012() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						// message sends	\t
						X x = new YM1();\t
						foo((X) x);			// UNnecessary
						foo((XM1) x);	// UNnecessary
						foo((YM1) x);	// necessary\s
					}
					static void foo(X x) {}
					static void foo(YM1 ym1) {}
				  static class XM1 extends X {}
				  static class YM1 extends XM1 {}
				}
				"""
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		"""
			----------
			1. ERROR in X.java (at line 5)
				foo((X) x);			// UNnecessary
				    ^^^^^
			Unnecessary cast from X to X
			----------
			2. ERROR in X.java (at line 6)
				foo((XM1) x);	// UNnecessary
				    ^^^^^^^
			Unnecessary cast from X to X.XM1
			----------
			""",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=42289
public void test013() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
				\t
					public static void main(String[] args) {
						int a = 0, b = 1;
						long d;
						d = (long)a; 				// unnecessary
						d = (long)a + b; 		// necessary\s
						d = d + a + (long)b; 	// unnecessary
					}
				}
				
				""",
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		"""
			----------
			1. ERROR in X.java (at line 6)
				d = (long)a; 				// unnecessary
				    ^^^^^^^
			Unnecessary cast from int to long
			----------
			2. ERROR in X.java (at line 8)
				d = d + a + (long)b; 	// unnecessary
				            ^^^^^^^
			Unnecessary cast from int to long
			----------
			""",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
// 39925 - Unnecessary instanceof checking leads to a NullPointerException
public void test014() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
					boolean b = new Cloneable() {} instanceof Cloneable;
				}"""
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		"""
			----------
			1. ERROR in X.java (at line 2)
				boolean b = new Cloneable() {} instanceof Cloneable;
				            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			The expression of type new Cloneable(){} is already an instance of type Cloneable
			----------
			""",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
// narrowing cast on base types may change value, thus necessary
public void test015() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
					void foo() {\t
				    int lineCount = 10;\s
				    long time = 1000;\s
				    double linePerSeconds1 = ((int) (lineCount * 10000.0 / time)) / 10.0; // necessary\s
				    double linePerSeconds2 = ((double) (lineCount * 10000.0 / time)) / 10.0; // UNnecessary\s
				  }\s
				}"""
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		"""
			----------
			1. ERROR in X.java (at line 6)
				double linePerSeconds2 = ((double) (lineCount * 10000.0 / time)) / 10.0; // UNnecessary\s
				                         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Unnecessary cast from double to double
			----------
			""",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
// narrowing cast on base types may change value, thus necessary
public void test016() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
					void foo() {\t
				    int lineCount = 10;\s
				    long time = 1000;\s
				    print((int) (lineCount * 10000.0 / time)); // necessary\s
				    print((double) (lineCount * 10000.0 / time)); // UNnecessary\s
				  }\s
				  void print(double d) {} \s
				}"""
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		"""
			----------
			1. ERROR in X.java (at line 6)
				print((double) (lineCount * 10000.0 / time)); // UNnecessary\s
				      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Unnecessary cast from double to double
			----------
			""",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
//fault tolerance (40288)
public void test017() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void bar() {
						foo((X) this);
						foo((X) zork());
					}
					void foo(X x) {
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				foo((X) this);
				    ^^^^^^^^
			Unnecessary cast from X to X
			----------
			2. ERROR in X.java (at line 4)
				foo((X) zork());
				        ^^^^
			The method zork() is undefined for the type X
			----------
			""",
		null,
		true,
		customOptions);
}
//fault tolerance (40423)
public void test018() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class Y {
					static Y[] foo(int[] tab) {
						return null;
					}
				}
				public class X extends Y {
					Y[] bar() {
						return (Y[]) Y.foo(new double[] {});
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 8)
				return (Y[]) Y.foo(new double[] {});
				               ^^^
			The method foo(int[]) in the type Y is not applicable for the arguments (double[])
			----------
			""",
		null,
		true,
		customOptions);
}
//fault tolerance (40288)
public void tes019() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void bar() {
						X x1 =(X) this;
						X x2 = (X) zork();
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				X x1 =(X) this;
				      ^^^^^^^^
			Unnecessary cast to type X for expression of type X
			----------
			2. ERROR in X.java (at line 4)
				X x2 = (X) zork();
				           ^^^^
			The method zork() is undefined for the type X
			----------
			""",
		null,
		true,
		customOptions);
}
//fault tolerance
public void test020() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void bar() {
						long l = (long)zork() + 2;
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				long l = (long)zork() + 2;
				               ^^^^
			The method zork() is undefined for the type X
			----------
			""",
		null,
		true,
		customOptions);
}

// unnecessary cast diagnosis should also consider receiver type (40572)
public void test021() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	this.runNegativeTest(
		new String[] {
			"p1/A.java",
			"""
				package p1;
				public class A {
					public class Member1 {}
					public class Member2 {}
					class Member3 {}
				   public static class Member4 {
					   public static class M4Member {}
				   }
				}
				""",
			"p2/B.java",
			"""
				package p2;
				import p1.A;
				public class B extends A {
					public class Member1 {}
				}
				""",
			"p1/C.java",
			"""
				package p1;
				import p2.B;
				public class C extends B {
					void baz(B b) {
						((A)b).new Member1(); // necessary since would bind to B.Member instead
						((A)b).new Member2(); // UNnecessary
						((A)b).new Member3(); // necessary since visibility issue
						((A)b).new Member4().new M4Member(); // fault tolerance
						((A)zork()).new Member1(); // fault-tolerance
						// anonymous
						((A)b).new Member1(){}; // necessary since would bind to B.Member instead
						((A)b).new Member2(){}; // UNnecessary
						((A)b).new Member3(){}; // necessary since visibility issue
						((A)b).new Member4().new M4Member(){}; // fault tolerance
						((A)zork()).new Member1(){}; // fault-tolerance
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in p1\\C.java (at line 6)
				((A)b).new Member2(); // UNnecessary
				^^^^^^
			Unnecessary cast from B to A
			----------
			2. ERROR in p1\\C.java (at line 8)
				((A)b).new Member4().new M4Member(); // fault tolerance
				^^^^^^
			Unnecessary cast from B to A
			----------
			3. ERROR in p1\\C.java (at line 9)
				((A)zork()).new Member1(); // fault-tolerance
				    ^^^^
			The method zork() is undefined for the type C
			----------
			4. ERROR in p1\\C.java (at line 12)
				((A)b).new Member2(){}; // UNnecessary
				^^^^^^
			Unnecessary cast from B to A
			----------
			5. ERROR in p1\\C.java (at line 14)
				((A)b).new Member4().new M4Member(){}; // fault tolerance
				^^^^^^
			Unnecessary cast from B to A
			----------
			6. ERROR in p1\\C.java (at line 15)
				((A)zork()).new Member1(){}; // fault-tolerance
				    ^^^^
			The method zork() is undefined for the type C
			----------
			""",
		null,
		true,
		customOptions);
}
// unnecessary cast diagnosis should tolerate array receiver type (40752)
public void test022() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	customOptions.put(CompilerOptions.OPTION_ReportUncheckedTypeOperation, CompilerOptions.IGNORE);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X {\t
				  void foo(java.util.Map map){\s
				    int[] fillPattern = new int[0];\s
				    if (fillPattern.equals((int[])map.get("x"))) {\s
				    } \s
				  }\s
				}\s
				""",
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		"""
			----------
			1. ERROR in X.java (at line 4)
				if (fillPattern.equals((int[])map.get("x"))) {\s
				                       ^^^^^^^^^^^^^^^^^^^
			Unnecessary cast from Object to int[]
			----------
			""",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}

// unnecessary cast diagnosis should tolerate array receiver type (40752)
public void test023() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
				
					public static void main(String[] args) {
						final long lgLow32BitMask1 = ~(~((long) 0) << 32);		// necessary
						final long lgLow32BitMask2 = ~(~0 << 32);					// necessary
						final long lgLow32BitMask3 = ~(~((long) 0L) << 32);	// unnecessary
						final long lgLow32BitMask4 = ~(~((int) 0L) << 32);		// necessary
						System.out.println("lgLow32BitMask1: "+lgLow32BitMask1);
						System.out.println("lgLow32BitMask2: "+lgLow32BitMask2);
					}
				}""",
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		"""
			----------
			1. ERROR in X.java (at line 6)
				final long lgLow32BitMask3 = ~(~((long) 0L) << 32);	// unnecessary
				                                ^^^^^^^^^^^
			Unnecessary cast from long to long
			----------
			""",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}

// unnecessary cast diagnosis for message receiver (44400)
public void test024() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
					public void foo(Object bar) {
						System.out.println(((Object) bar).toString());
					}
				}""",
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		"""
			----------
			1. ERROR in X.java (at line 3)
				System.out.println(((Object) bar).toString());
				                   ^^^^^^^^^^^^^^
			Unnecessary cast from Object to Object
			----------
			""",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}

// unnecessary cast diagnosis for message receiver (44400)
// variation with field access
public void test025() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
					int i;
					public void foo(X bar) {
						System.out.println(((X) bar).i);
					}
				}""",
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		"""
			----------
			1. ERROR in X.java (at line 4)
				System.out.println(((X) bar).i);
				                   ^^^^^^^^^
			Unnecessary cast from X to X
			----------
			""",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=47074
 */
public void test026() {

	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	if (options.sourceLevel < ClassFileConstants.JDK1_5) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					  public static void main(String[] args) {
					    A a = null;
					    B b = (B) a;
					  }
					}
					interface A {
					  void doSomething();
					}
					interface B {
					  int doSomething();
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					B b = (B) a;
					      ^^^^^
				Cannot cast from A to B
				----------
				""");
		return;
	}
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args) {
				    A a = null;
				    B b = (B) a;
				  }
				}
				interface A {
				  void doSomething();
				}
				interface B {
				  int doSomething();
				}""",
		},
		"");

}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=47074
 */
public void test027() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	if (options.sourceLevel < ClassFileConstants.JDK1_5) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					  public static void main(String[] args) {
					    A a = null;
					    boolean b = a instanceof B;
					  }
					}
					interface A {
					  void doSomething();
					}
					interface B {
					  int doSomething();
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 4)
					boolean b = a instanceof B;
					            ^^^^^^^^^^^^^^
				Incompatible conditional operand types A and B
				----------
				""");
		return;
	}
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args) {
				    A a = null;
				    boolean b = a instanceof B;
				  }
				}
				interface A {
				  void doSomething();
				}
				interface B {
				  int doSomething();
				}""",
		},
		"");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=47074
 */
public void test028() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	if (options.sourceLevel < ClassFileConstants.JDK1_5) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
					  public static void main(String[] args) {
					    A a = null;
					    B b = null;
					    boolean c = a == b;
					  }
					}
					interface A {
					  void doSomething();
					}
					interface B {
					  int doSomething();
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					boolean c = a == b;
					            ^^^^^^
				Incompatible operand types A and B
				----------
				""");
		return;
	}
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  public static void main(String[] args) {
				    A a = null;
				    B b = null;
				    boolean c = a == b;
				  }
				}
				interface A {
				  void doSomething();
				}
				interface B {
				  int doSomething();
				}""",
		},
		"");

}

/*
 * verify error when assigning null to array
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26903
 */
public void test029() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {\t
				    public static void main(String[] args) {\t
						try {\t
						    char[][] qName;\t
							qName = null;\t
							qName[0] = new char[1];\t
						} catch(Exception e){\t
						}\t
						try {\t
						    char[][] qName;\t
							qName = (char[][])null;\t
							qName[0] = new char[1];\t
						} catch(Exception e){\t
						}\t
						try {\t
						    char[][] qName;\t
							qName = (char[][])(char[][])(char[][])null;\t
							qName[0] = new char[2];\t
						} catch(Exception e){\t
						}\t
						try {\t
						    char[][] qName;\t
							qName = args.length > 1 ? new char[1][2] : null;\t
							qName[0] = new char[3];\t
						} catch(Exception e){\t
						}\t
						System.out.println("SUCCESS");\t
					}\t
				}\t
				""",
		},
	"SUCCESS");
}

/*
 * verify error when assigning null to array
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26903
 */
public void test030() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {\t
				    public static void main(String[] args) {\t
						try {\t
							char[][] qName = null;\t
							qName[0] = new char[1];\t
						} catch(Exception e){\t
						}\t
						try {\t
							char[][] qName = (char[][])null;\t
							qName[0] = new char[1];\t
						} catch(Exception e){\t
						}\t
						try {\t
							char[][] qName = (char[][])(char[][])(char[][])null;\t
							qName[0] = new char[2];\t
						} catch(Exception e){\t
						}\t
						try {\t
							char[][] qName = args.length > 1 ? new char[1][2] : null;\t
							qName[0] = new char[3];\t
						} catch(Exception e){\t
						}\t
						System.out.println("SUCCESS");\t
					}\t
				}\t
				""",
		},
	"SUCCESS");
}

/*
 * verify error when assigning null to array
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26903
 */
public void test031() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {\t
				    public static void main(String[] args) {\t
						try {\t
							char[][] qName = null;\t
							setName(qName[0]);\t
						} catch(Exception e){\t
						}\t
						try {\t
							char[][] qName = (char[][])null;\t
							setName(qName[0]);\t
						} catch(Exception e){\t
						}\t
						try {\t
							char[][] qName = (char[][])(char[][])(char[][])null;\t
							setName(qName[0]);\t
						} catch(Exception e){\t
						}\t
						try {\t
							char[][] qName = args.length > 1 ? new char[1][2] : null;\t
							setName(qName[0]);\t
						} catch(Exception e){\t
						}\t
						System.out.println("SUCCESS");\t
					}\t
					static void setName(char[] name) {\t
					}\t
				}\t
				""",
		},
	"SUCCESS");
}
/*
 * verify error when assigning null to array
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=26903
 */
public void test032() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public static void main(String[] args) {
							try {
								((int[]) null)[0] = 0;
								((int[]) null)[0] += 1;
								((int[]) null)[0] ++;
							} catch (NullPointerException e) {
								System.out.print("SUCCESS");
							}
					}
				}
				""",
		},
	"SUCCESS");
}

/*
 * unused cast diagnosis
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=54763
 */
public void test033() {
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				import java.util.ArrayList;
				import java.util.List;
				
				public class X {
				    public static void main(String [] args) {
				        List list = (List) new ArrayList();
				        list = (List) new ArrayList();
				       \s
				        String s = (String) "hello";
				        s += (List) new ArrayList();
				       \s
				        ArrayList alist = new ArrayList();
				        List list2 = (List) alist;
				        list2 = (List) alist;
				       \s
				        String s2 = (String) "hello";
				        s2 += (List) alist;
				    }
				}
				"""
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		"""
			----------
			1. ERROR in X.java (at line 6)
				List list = (List) new ArrayList();
				            ^^^^^^^^^^^^^^^^^^^^^^
			Unnecessary cast from ArrayList to List
			----------
			2. ERROR in X.java (at line 7)
				list = (List) new ArrayList();
				       ^^^^^^^^^^^^^^^^^^^^^^
			Unnecessary cast from ArrayList to List
			----------
			3. ERROR in X.java (at line 9)
				String s = (String) "hello";
				           ^^^^^^^^^^^^^^^^
			Unnecessary cast from String to String
			----------
			4. ERROR in X.java (at line 10)
				s += (List) new ArrayList();
				     ^^^^^^^^^^^^^^^^^^^^^^
			Unnecessary cast from ArrayList to List
			----------
			5. ERROR in X.java (at line 13)
				List list2 = (List) alist;
				             ^^^^^^^^^^^^
			Unnecessary cast from ArrayList to List
			----------
			6. ERROR in X.java (at line 14)
				list2 = (List) alist;
				        ^^^^^^^^^^^^
			Unnecessary cast from ArrayList to List
			----------
			7. ERROR in X.java (at line 16)
				String s2 = (String) "hello";
				            ^^^^^^^^^^^^^^^^
			Unnecessary cast from String to String
			----------
			8. ERROR in X.java (at line 17)
				s2 += (List) alist;
				      ^^^^^^^^^^^^
			Unnecessary cast from ArrayList to List
			----------
			""",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
/*
 * check non insertion of checkcast for unnecessary cast to interfaces
 * (same test case as test033)
 */
public void test034() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				import java.util.List;
				
				public class X {
				    public static void main(String [] args) {
				        List list = (List) new ArrayList();
				        list = (List) new ArrayList();
				       \s
				        ArrayList alist = new ArrayList();
				        List list2 = (List) alist;
				        list2 = (List) alist;
				       \s
				       System.out.println("SUCCESS");
				    }
				}
				""",
		},
		"SUCCESS");

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);

	String expectedOutput =
		"""
		  // Method descriptor #15 ([Ljava/lang/String;)V
		  // Stack: 2, Locals: 4
		  public static void main(java.lang.String[] args);
		     0  new java.util.ArrayList [16]
		     3  dup
		     4  invokespecial java.util.ArrayList() [18]
		     7  astore_1 [list]
		     8  new java.util.ArrayList [16]
		    11  dup
		    12  invokespecial java.util.ArrayList() [18]
		    15  astore_1 [list]
		    16  new java.util.ArrayList [16]
		    19  dup
		    20  invokespecial java.util.ArrayList() [18]
		    23  astore_2 [alist]
		    24  aload_2 [alist]
		    25  astore_3 [list2]
		    26  aload_2 [alist]
		    27  astore_3 [list2]
		    28  getstatic java.lang.System.out : java.io.PrintStream [19]
		    31  ldc <String "SUCCESS"> [25]
		    33  invokevirtual java.io.PrintStream.println(java.lang.String) : void [27]
		    36  return
		      Line numbers:
		        [pc: 0, line: 6]
		        [pc: 8, line: 7]
		        [pc: 16, line: 9]
		        [pc: 24, line: 10]
		        [pc: 26, line: 11]
		        [pc: 28, line: 13]
		        [pc: 36, line: 14]
		      Local variable table:
		        [pc: 0, pc: 37] local: args index: 0 type: java.lang.String[]
		        [pc: 8, pc: 37] local: list index: 1 type: java.util.List
		        [pc: 24, pc: 37] local: alist index: 2 type: java.util.ArrayList
		        [pc: 26, pc: 37] local: list2 index: 3 type: java.util.List
		""";
	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 2));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, actualOutput);
	}
}
// javac incorrectly accepts it
public void test035() {
	String[] sources = {
			"Test231.java",
			"""
				public class Test231 implements Test231i
				{
					void	foo()
					{
						new Object()
						{
							Test231i	bar()
							{
								return	(Test231i)this;
							}
						};
					}
				}
				
				
				interface Test231i
				{
				}
				"""
		};
	if (this.complianceLevel < ClassFileConstants.JDK9) {
		runNegativeTest(sources,
			"""
				----------
				1. ERROR in Test231.java (at line 9)
					return	(Test231i)this;
					      	^^^^^^^^^^^^^^
				Cannot cast from new Object(){} to Test231i
				----------
				""",
		// javac options
		JavacTestOptions.JavacHasABug.JavacBugFixed_6_10 /* javac test options */);
	} else {
		runConformTest(sources, "");
	}
}
public void test036() {
	runConformTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public final class X {
					private static final boolean DO_BUG = true;
				
					// Workaround: cast null to Base
					private static Base base = DO_BUG ?
					// (Base)null
							null : new Base() {
								public final String test() {
									return ("anonymous");
								}
							};
				
					private X() {
					}
				
					public static void main(String[] argv) {
						if (base == null)
							System.out.println("no base");
						else
							System.out.println(base.test());
					}
				
					private static abstract class Base {
						public Base() {
						}
				
						public abstract String test();
					}
				}
				"""
		},
		// compiler results
		"", /* expected compiler log */
		// runtime results
		"no base" /* expected output string */,
		"" /* expected error string */,
		// javac options
		JavacTestOptions.JavacHasABug.JavacBugFixed_7 /* javac test options */);
}
public void test037() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						Integer[] integers = {};
						int[] ints = (int[]) integers;
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				int[] ints = (int[]) integers;
				             ^^^^^^^^^^^^^^^^
			Cannot cast from Integer[] to int[]
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=101208
public void test038() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void foo() {
						System.out.println(null instanceof Object);
				      Zork z;
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 4)\r
				Zork z;\r
				^^^^
			Zork cannot be resolved to a type
			----------
			""");
}
//unnecessary cast warnings in assignment (Object o = (String) something).
public void test039() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.*;
				public class X {
					Object fo = (String) new Object();
					void foo(ArrayList al) {
						List l = (List) al;
						Object o;
						o = (ArrayList) al;
						Object o2 = (ArrayList) al;
						o = (ArrayList) l;
						Object o3 = (ArrayList) l;
						Zork z;
					}
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 3)
				Object fo = (String) new Object();
				            ^^^^^^^^^^^^^^^^^^^^^
			Unnecessary cast from Object to String
			----------
			2. WARNING in X.java (at line 5)
				List l = (List) al;
				         ^^^^^^^^^
			Unnecessary cast from ArrayList to List
			----------
			3. WARNING in X.java (at line 7)
				o = (ArrayList) al;
				    ^^^^^^^^^^^^^^
			Unnecessary cast from ArrayList to ArrayList
			----------
			4. WARNING in X.java (at line 8)
				Object o2 = (ArrayList) al;
				            ^^^^^^^^^^^^^^
			Unnecessary cast from ArrayList to ArrayList
			----------
			5. WARNING in X.java (at line 9)
				o = (ArrayList) l;
				    ^^^^^^^^^^^^^
			Unnecessary cast from List to ArrayList
			----------
			6. WARNING in X.java (at line 10)
				Object o3 = (ArrayList) l;
				            ^^^^^^^^^^^^^
			Unnecessary cast from List to ArrayList
			----------
			7. ERROR in X.java (at line 11)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			""");
}
//http://bugs.eclipse.org/bugs/show_bug.cgi?id=116647
public void test040() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					{
						int i = 12;
						int j = (byte) i;
						float f = (float) i;
						Zork z;
					}
				}
				"""
		},
		"""
			----------
			1. WARNING in X.java (at line 5)
				float f = (float) i;
				          ^^^^^^^^^
			Unnecessary cast from int to float
			----------
			2. ERROR in X.java (at line 6)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=158855
public void test041() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public abstract class X {
				    class A extends X {
				        public void callMe() {
				        }
				    }
				    public abstract void callMe();
				    class B {
				        public void callSite() {
				            // expect warning not there:
				            ((A) this.getAA()).callMe();
				            Integer max = Integer.valueOf(1);
				            // execpted warning there:
				            Integer other = (Integer) max;
				        }
				        public X getAA() {
				            Zork z;
				            return null;
				        }
				    }
				}""", // =================
		},
		"""
			----------
			1. WARNING in X.java (at line 13)
				Integer other = (Integer) max;
				                ^^^^^^^^^^^^^
			Unnecessary cast from Integer to Integer
			----------
			2. ERROR in X.java (at line 16)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=159654
public void test042() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.List;
				public class X {
					public static void main(String[] args) {
						System.out.print("SUCCESS");\t
					}
				\t
					public static void foo(boolean b, List l) {
						if (b) {
							String s = (String) l.get(0);
						}
					}
				}""",
		},
		"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=159654
public void test043() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.List;
				public class X {
					public static void main(String[] args) {
						System.out.print("SUCCESS");\t
					}
				\t
					public static void foo(boolean b, List l) {
						if (b) {
							Object o = (Object) l.get(0);
						}
					}
				}""",
		},
		"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=159822
public void test044() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public static String s;
				    public static void main(String[] args) throws Throwable {
				      if (args.length == 0) {
				        Class c = Class.forName("X");
				        String s = ((X) c.newInstance()).s;
				        System.out.println(s);
				      }
				      System.out.println();
				    }
				}""",
		},
		"null");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=239305
public void test045() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						System.out.println(castLongToInt(3));
					}
					private static int castLongToInt(long longVal) {
						return (int)((long)longVal);
					}
				}
				""",
		},
		"3");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=282869
public void test046() {
	this.runConformTest(
		true,
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						char a = 65;
						String b = "" + a; // -> "A"
						String c = "" + (int) a;
						System.out.print(b);
						System.out.print(c);
					\t
						String logText = " second case ";
						char firstChar = 65;
						logText += (int) firstChar;
						System.out.println(logText);
					}
				}""",
		},
		"",
		"A65 second case 65",
		"",
		JavacTestOptions.SKIP);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=287676
// Test to make sure that an unnecessary cast warning is produced in case of
// wrapper types like Integer, Character, Short, Byte, etc.
public void test047() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	if (options.sourceLevel < ClassFileConstants.JDK1_5) return;
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				public class X{
					void test() {\
						Integer a = 1;
						ArrayList<Character> aList = new ArrayList<Character>(1);
						a = (Integer)a + (Integer)2;
						if ((Character)aList.get(0) == 'c')
							System.out.println();
					}
				}"""
		},
		"""
			----------
			1. WARNING in X.java (at line 5)
				a = (Integer)a + (Integer)2;
				    ^^^^^^^^^^
			Unnecessary cast from Integer to Integer
			----------
			2. WARNING in X.java (at line 5)
				a = (Integer)a + (Integer)2;
				                 ^^^^^^^^^^
			Unnecessary cast from int to Integer
			----------
			3. WARNING in X.java (at line 6)
				if ((Character)aList.get(0) == 'c')
				    ^^^^^^^^^^^^^^^^^^^^^^^
			Unnecessary cast from Character to Character
			----------
			"""
	);
}
public void testBug418795() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // uses autoboxing
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"	void foo() {\n" +
			"		 Integer smallNumber = 42;\n" +
			"        Integer largeNumber = 500;\n" +
			"\n" +
			"        // this prints:\n" +
			"        if (smallNumber == 42)\n" +
			"            System.out.println(\"42\");\n" +
			"\n" +
			"        // this prints:\n" +
			"        if (largeNumber == 500)\n" +
			"            System.out.println(\"500\");\n" +
			"\n" +
			"        // this prints:\n" +
			"        if (smallNumber == (Object) 42)\n" +
			"            System.out.println(\"42\");\n" +
			"\n" +
			"        // this doesn't print:\n" +
			"        if (largeNumber == (Object) 500)\n" +
			"            System.out.println(\"500\");\n" +
			"" +
			"	}\n" +
			"}\n"
		},
		options);
}
public void testBug329437() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5) return; // uses autoboxing
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String... args) {
						Integer a = Integer.valueOf(10);
						Integer b = Integer.valueOf(10);
						boolean abEqual = (int)a == (int)b;
						System.out.println(abEqual);
					}
				}
				"""
		},
		options);
}
public void testBug521778() {
	Map options = getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					static int intThruFloat(int x) { return (int)(float)x; }
					static long longThruFloat(long x) { return (long)(float)x; }
					static long longThruDouble(long x) { return (long)(double)x; }
					public static void main(String[] args) {
						System.out.println(intThruFloat(2147483646));
						System.out.println(longThruFloat(-9223372036854775806L));
						System.out.print(longThruDouble(-9223372036854775807L));
					}
				}
				"""
		},
		"""
			2147483647
			-9223372036854775808
			-9223372036854775808""",		// the input
		options);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=302919
public void test048() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	if (options.sourceLevel < ClassFileConstants.JDK1_5) return;
	this.runNegativeTest(
		new String[] {
			"A.java",
			"""
				public class A<T> extends D<T> {
				    public class A1 extends D1 {
				    }
				    void m1(A<T> tree) {
				        A.A1 v = ((A.A1) tree.root);
				    }
				    Zork z;
				}
				class D<T> {
				    protected D1 root;
				    protected class D1 {
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in A.java (at line 7)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			"""
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=302919
public void test049() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	if (options.sourceLevel < ClassFileConstants.JDK1_5) return;
	Runner runner = new Runner();
	runner.testFiles =
		new String[] {
			"A.java",
			"""
				public class A {
					void foo(Other2<?>.Member2<?> om2) {
						Other<?>.Member m = (Other<?>.Member) om2;
						m = om2;
					}
				}
				class Other<T> {
					class Member {}
				}
				class Other2<T> extends Other<T> {
					class Member2<U> extends Other<U>.Member {
					}
				}
				"""
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. WARNING in A.java (at line 3)
				Other<?>.Member m = (Other<?>.Member) om2;
				                    ^^^^^^^^^^^^^^^^^^^^^
			Unnecessary cast from Other2<?>.Member2<capture#1-of ?> to Other<?>.Member
			----------
			""";
	runner.javacTestOptions =
		Excuse.EclipseHasSomeMoreWarnings; // javac is inconsistent: accepting both assignments, not issuing a warning though in simpler cases it does
	// note that javac 1.6 doesn't even accept the syntax of this cast
	runner.runWarningTest();
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=302919
public void test050() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	if (options.sourceLevel < ClassFileConstants.JDK1_5) return;
	this.runNegativeTest(
		new String[] {
			"A.java",
			"""
				public class A<T> extends D<T> {
				    public class A1 extends D.D1 {
				    }
				    void m1(A<T> tree) {
				        A.A1 v = ((A.A1) tree.root);
				    }
				    Zork z;
				}
				class D<T> {
				    protected D1 root;
				    protected class D1 {
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in A.java (at line 7)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			"""
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=353085
public void test051() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	String source =
			"""
		public class X {
			public static void main(String[] args) {
				Object x = foo();
				boolean y = (boolean) x;
				System.out.println(y);
			}
			public static Object foo() {
				return Boolean.TRUE;
			}
		}""";
	if (options.sourceLevel < ClassFileConstants.JDK1_7) {
		this.runNegativeTest(
				new String[] {
					"X.java",
					source
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						boolean y = (boolean) x;
						            ^^^^^^^^^^^
					Cannot cast from Object to boolean
					----------
					"""
			);
	} else {
		this.runConformTest(
				new String[] {
					"X.java",
					source
				},
				"true"
			);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=353085
public void test052() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	String source =
			"""
		public class X {
			public static void main(String[] args) {
				Object x = foo();
				byte y = (byte) x;
				System.out.println(y);
			}
			public static Object foo() {
				return Byte.valueOf((byte)1);
			}
		}""";
	if (options.sourceLevel < ClassFileConstants.JDK1_7) {
		this.runNegativeTest(
				new String[] {
					"X.java",
					source
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						byte y = (byte) x;
						         ^^^^^^^^
					Cannot cast from Object to byte
					----------
					"""
			);
	} else {
		this.runConformTest(
				new String[] {
					"X.java",
					source
				},
				"1"
			);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=353085
public void test053() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	String source =
			"""
		public class X {
			public static void main(String[] args) {
				Object x = foo();
				char y = (char) x;
				System.out.println(y);
			}
			public static Object foo() {
				return Character.valueOf('d');
			}
		}""";
	if (options.sourceLevel < ClassFileConstants.JDK1_7) {
		this.runNegativeTest(
				new String[] {
					"X.java",
					source
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						char y = (char) x;
						         ^^^^^^^^
					Cannot cast from Object to char
					----------
					"""
			);
	} else {
		this.runConformTest(
				new String[] {
					"X.java",
					source
				},
				"d"
			);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=353085
// Also confirm that a check cast and unboxing conversion are generated.
public void test054() throws Exception {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	String source =
			"""
		public class X {
			public static void main(String[] args) {
				Object x = foo();
				int y = (int) x;
				System.out.println(y);
			}
			public static Object foo() {
				return Integer.valueOf(1);
			}
		}""";
	if (options.sourceLevel < ClassFileConstants.JDK1_7) {
		this.runNegativeTest(
				new String[] {
					"X.java",
					source
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						int y = (int) x;
						        ^^^^^^^
					Cannot cast from Object to int
					----------
					"""
			);
	} else {
		this.runConformTest(
				new String[] {
					"X.java",
					source
				},
				"1"
			);
		String expectedOutput =
				"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 2, Locals: 3
			  public static void main(java.lang.String[] args);
			     0  invokestatic X.foo() : java.lang.Object [16]
			     3  astore_1 [x]
			     4  aload_1 [x]
			     5  checkcast java.lang.Integer [20]
			     8  invokevirtual java.lang.Integer.intValue() : int [22]
			    11  istore_2 [y]
			    12  getstatic java.lang.System.out : java.io.PrintStream [26]
			    15  iload_2 [y]
			    16  invokevirtual java.io.PrintStream.println(int) : void [32]
			    19  return
			      Line numbers:
			        [pc: 0, line: 3]
			        [pc: 4, line: 4]
			        [pc: 12, line: 5]
			        [pc: 19, line: 6]
			      Local variable table:
			        [pc: 0, pc: 20] local: args index: 0 type: java.lang.String[]
			        [pc: 4, pc: 20] local: x index: 1 type: java.lang.Object
			        [pc: 12, pc: 20] local: y index: 2 type: int
			 \s
			""";
		File f = new File(OUTPUT_DIR + File.separator + "X.class");
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
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=353085
public void test055() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	String source =
			"""
		public class X {
			public static void main(String[] args) {
				Object x = foo();
				long y = (long) x;
				System.out.println(y);
			}
			public static Object foo() {
				return Long.valueOf(Long.MAX_VALUE);
			}
		}""";
	if (options.sourceLevel < ClassFileConstants.JDK1_7) {
		this.runNegativeTest(
				new String[] {
					"X.java",
					source
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						long y = (long) x;
						         ^^^^^^^^
					Cannot cast from Object to long
					----------
					"""
			);
	} else {
		this.runConformTest(
				new String[] {
					"X.java",
					source
				},
				"9223372036854775807"
			);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=353085
public void test056() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	String source =
			"""
		public class X {
			public static void main(String[] args) {
				Object x = foo();
				short y = (short) x;
				System.out.println(y);
			}
			public static Object foo() {
				return Short.valueOf((short) 1);
			}
		}""";
	if (options.sourceLevel < ClassFileConstants.JDK1_7) {
		this.runNegativeTest(
				new String[] {
					"X.java",
					source
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						short y = (short) x;
						          ^^^^^^^^^
					Cannot cast from Object to short
					----------
					"""
			);
	} else {
		this.runConformTest(
				new String[] {
					"X.java",
					source
				},
				"1"
			);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=353085
public void test057() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	String source =
			"""
		public class X {
			public static void main(String[] args) {
				Object x = foo();
				double y = (double) x;
				System.out.println(y);
			}
			public static Object foo() {
				return Double.valueOf(1.0);
			}
		}""";
	if (options.sourceLevel < ClassFileConstants.JDK1_7) {
		this.runNegativeTest(
				new String[] {
					"X.java",
					source
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						double y = (double) x;
						           ^^^^^^^^^^
					Cannot cast from Object to double
					----------
					"""
			);
	} else {
		this.runConformTest(
				new String[] {
					"X.java",
					source
				},
				"1.0"
			);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=353085
public void test058() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	String source =
			"""
		public class X {
			public static void main(String[] args) {
				Object x = foo();
				float y = (float) x;
				System.out.println(y);
			}
			public static Object foo() {
				return Float.valueOf(1.0f);
			}
		}""";
	if (options.sourceLevel < ClassFileConstants.JDK1_7) {
		this.runNegativeTest(
				new String[] {
					"X.java",
					source
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						float y = (float) x;
						          ^^^^^^^^^
					Cannot cast from Object to float
					----------
					"""
			);
	} else {
		this.runConformTest(
				new String[] {
					"X.java",
					source
				},
				"1.0"
			);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=353085
public void test059() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	String source =
			"""
		public class X {
			public static void main(String[] args) {
				Object x = foo();
				try {
					int y = (int) x;
				} catch (ClassCastException e) {
					System.out.println("SUCCESS");
					return;
				}
				System.out.println("FAIL");
			}
			public static Object foo() {
				return Float.valueOf(1.0f);
			}
		}""";
	if (options.sourceLevel < ClassFileConstants.JDK1_7) {
		this.runNegativeTest(
				new String[] {
					"X.java",
					source
				},
				"""
					----------
					1. ERROR in X.java (at line 5)
						int y = (int) x;
						        ^^^^^^^
					Cannot cast from Object to int
					----------
					"""
			);
	} else {
		this.runConformTest(
				new String[] {
					"X.java",
					source
				},
				"SUCCESS"
			);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=353085
public void test059b() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	String source =
			"""
		public class X {
			public static void main(String[] args) {
				Object x = foo();
				try {
					int y = (int) x;
				} catch (ClassCastException e) {
					System.out.println("SUCCESS");
					return;
				}
				System.out.println("FAIL");
			}
			public static Object foo() {
				return Boolean.TRUE;
			}
		}""";
	if (options.sourceLevel < ClassFileConstants.JDK1_7) {
		this.runNegativeTest(
				new String[] {
					"X.java",
					source
				},
				"""
					----------
					1. ERROR in X.java (at line 5)
						int y = (int) x;
						        ^^^^^^^
					Cannot cast from Object to int
					----------
					"""
			);
	} else {
		this.runConformTest(
				new String[] {
					"X.java",
					source
				},
				"SUCCESS"
			);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=353085
public void test059c() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	String source =
			"""
		public class X {
			public static void main(String[] args) {
				Object x = foo();
				try {
					char y = (char) x;
				} catch (ClassCastException e) {
					System.out.println("SUCCESS");
					return;
				}
				System.out.println("FAIL");
			}
			public static Object foo() {
				return Boolean.TRUE;
			}
		}""";
	if (options.sourceLevel < ClassFileConstants.JDK1_7) {
		this.runNegativeTest(
				new String[] {
					"X.java",
					source
				},
				"""
					----------
					1. ERROR in X.java (at line 5)
						char y = (char) x;
						         ^^^^^^^^
					Cannot cast from Object to char
					----------
					"""
			);
	} else {
		this.runConformTest(
				new String[] {
					"X.java",
					source
				},
				"SUCCESS"
			);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=353085
public void test060() {
	String source =
			"""
		public class X {
			public static void main(String[] args) {
				Object x = foo();
				Boolean y = (Boolean) x;
				System.out.println(y);
			}
			public static Object foo() {
				return Boolean.TRUE;
			}
		}""";
	this.runConformTest(
			new String[] {
				"X.java",
				source
			},
			"true"
		);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=353085
public void test061() {
	String source =
			"""
		public class X {
			public static void main(String[] args) {
				Object x = foo();
				try {
					Float y = (Float) x;
				} catch (ClassCastException e) {
					System.out.println("SUCCESS");
					return;
				}
				System.out.println("FAIL");
			}
			public static Object foo() {
				return Boolean.TRUE;
			}
		}""";
	this.runConformTest(
			new String[] {
				"X.java",
				source
			},
			"SUCCESS"
		);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=359284
// Verify that checkcast is emitted for a cast expression.
public void test061b() throws Exception {
	String source =
		"""
		public class X {
		public X() {
		    Object[] x = (Object[])null;
		}
		}
		""";
	this.runConformTest(
			new String[] {
				"X.java",
				source
			},
			""
		);
	String expectedOutput =
			"""
		public class X {
		 \s
		  // Method descriptor #6 ()V
		  // Stack: 1, Locals: 2
		  public X();
		     0  aload_0 [this]
		     1  invokespecial java.lang.Object() [8]
		     4  aconst_null
		     5  checkcast java.lang.Object[] [10]
		     8  astore_1 [x]
		     9  return
		      Line numbers:
		        [pc: 0, line: 2]
		        [pc: 4, line: 3]
		        [pc: 9, line: 4]
		      Local variable table:
		        [pc: 0, pc: 10] local: this index: 0 type: X
		        [pc: 9, pc: 10] local: x index: 1 type: java.lang.Object[]
		}""";
	File f = new File(OUTPUT_DIR + File.separator + "X.class");
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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=420283, [1.8] Wrong error "Type is not visible" for cast to intersection type
public void test420283() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	if (this.complianceLevel < ClassFileConstants.JDK1_8) {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						import java.io.Serializable;
						import java.util.List;
						public class X {
						    void foo(List<Integer> l) {
						        Integer i = (Integer & Serializable) l.get(0);
						    }
						    public static void main(String [] args) {
						        System.out.println("SUCCESS");
						    }
						}
						"""
				},
				"""
					----------
					1. WARNING in X.java (at line 5)
						Integer i = (Integer & Serializable) l.get(0);
						            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					Unnecessary cast from Integer to Integer & Serializable
					----------
					2. ERROR in X.java (at line 5)
						Integer i = (Integer & Serializable) l.get(0);
						             ^^^^^^^^^^^^^^^^^^^^^^
					Additional bounds are not allowed in cast operator at source levels below 1.8
					----------
					""");
		return;
	}
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.io.Serializable;
					import java.util.List;
					public class X {
					    void foo(List<Integer> l) {
					        Integer i = (Integer & Serializable) l.get(0);
					    }
					    public static void main(String [] args) {
					        System.out.println("SUCCESS");
					    }
					}
					"""
			},
			"SUCCESS"
		);
}

public void testBug428274() {
	String source =
			"""
		public class Junk4 {
		    static void setValue(Number n) {
		        int rounded = (int) Math.round((double) n);
				System.out.println(rounded);
		    }
			public static void main(String[] args) {
				setValue(Double.valueOf(3.3));
				setValue(Double.valueOf(3.7));
			}
		}
		""";
	if (this.complianceLevel < ClassFileConstants.JDK1_7) {
		runNegativeTest(
			new String[] {
				"Junk4.java",
				source
			},
			"""
				----------
				1. ERROR in Junk4.java (at line 3)
					int rounded = (int) Math.round((double) n);
					                               ^^^^^^^^^^
				Cannot cast from Number to double
				----------
				""");
	} else {
		runConformTest(
			new String[] {
				"Junk4.java",
				source
			},
			"3\n4");
	}
}
public void testBug428274b() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return; // uses generics
	Runner runner = new Runner();
	runner.testFiles = new String[] {
			"Junk4.java",
			"""
				public class Junk4<T> {
				    void setValue(T n) {
				        int rounded = (int) Math.round((double) n);
						System.out.println(rounded);
				    }
					public static void main(String[] args) {
						Junk4<Number> j = new Junk4<Number>();
						j.setValue(Double.valueOf(3.3));
						j.setValue(Double.valueOf(3.7));
					}
				}
				"""
	};
	if (this.complianceLevel < ClassFileConstants.JDK1_7) {
		runner.expectedCompilerLog =
			"""
				----------
				1. ERROR in Junk4.java (at line 3)
					int rounded = (int) Math.round((double) n);
					                               ^^^^^^^^^^
				Cannot cast from T to double
				----------
				""";
		runner.runNegativeTest();
	} else {
		runner.expectedOutputString =
			"3\n4";
		runner.javacTestOptions = JavacTestOptions.JavacHasABug.JavacBug8144832;
		runner.runConformTest();
	}
}
// note: spec allows all reference types, but neither javac nor common sense accept arrays :)
public void testBug428274c() {
	String source =
			"""
		public class Junk4 {
		    static void setValue(Object[] n) {
		        int rounded = (int) Math.round((double) n);
				System.out.println(rounded);
		    }
			public static void main(String[] args) {
				setValue(new Double[] { Double.valueOf(3.3) });
			}
		}
		""";
	runNegativeTest(
		new String[] {
			"Junk4.java",
			source
		},
		"""
			----------
			1. ERROR in Junk4.java (at line 3)
				int rounded = (int) Math.round((double) n);
				                               ^^^^^^^^^^
			Cannot cast from Object[] to double
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428388, [1.8][compiler] Casting to primitives is over tolerant - probable regression since bug 428274
public void test428388() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public static void main(String[] args) {
					int x = (int) "Hello";
				    }
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				int x = (int) "Hello";
				        ^^^^^^^^^^^^^
			Cannot cast from String to int
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428388, [1.8][compiler] Casting to primitives is over tolerant - probable regression since bug 428274
public void test428388a() throws Exception {
	if (this.complianceLevel < ClassFileConstants.JDK1_7)
		return;

	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    static void setValue(Number n) {
				       int rounded = (int) Math.round((double) n);
						System.out.println(rounded);
				    }
					public static void main(String[] args) {
						setValue(Double.valueOf(3.3));
						setValue(Double.valueOf(3.7));
					}
				}
				""",
		},
		"3\n4");

	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);

	String expectedOutput =
			"""
		  // Method descriptor #15 (Ljava/lang/Number;)V
		  // Stack: 2, Locals: 2
		  static void setValue(java.lang.Number n);
		     0  aload_0 [n]
		     1  checkcast java.lang.Double [16]
		     4  invokevirtual java.lang.Double.doubleValue() : double [18]
		     7  invokestatic java.lang.Math.round(double) : long [22]
		    10  l2i
		    11  istore_1 [rounded]
		    12  getstatic java.lang.System.out : java.io.PrintStream [28]
		    15  iload_1 [rounded]
		    16  invokevirtual java.io.PrintStream.println(int) : void [34]
		    19  return
		      Line numbers:
		        [pc: 0, line: 3]
		        [pc: 12, line: 4]
		        [pc: 19, line: 5]
		      Local variable table:
		        [pc: 0, pc: 20] local: n index: 0 type: java.lang.Number
		        [pc: 12, pc: 20] local: rounded index: 1 type: int
		 \s
		""";
	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 2));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, actualOutput);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428388, [1.8][compiler] Casting to primitives is over tolerant - probable regression since bug 428274
public void test428388b() throws Exception {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    static void setValue(Number n) {
				       char rounded = (char) n;
						System.out.println(rounded);
				    }
					public static void main(String[] args) {
						setValue(Double.valueOf(3.3));
						setValue(Double.valueOf(3.7));
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				char rounded = (char) n;
				               ^^^^^^^^
			Cannot cast from Number to char
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428388, [1.8][compiler] Casting to primitives is over tolerant - probable regression since bug 428274
public void test428388c() throws Exception {
	if (this.complianceLevel < ClassFileConstants.JDK1_7)
		return;
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    static void setValue(Number n) {
				       try {
				           byte rounded = (byte) n;
						    System.out.println(rounded);
				       } catch (ClassCastException c) {
				           System.out.println("CCE");
				       }
				    }
					public static void main(String[] args) {
						setValue(Double.valueOf(3.3));
						setValue(Double.valueOf(3.7));
					}
				}
				""",
		},
		"CCE\nCCE");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428388, [1.8][compiler] Casting to primitives is over tolerant - probable regression since bug 428274
public void test428388d() throws Exception {
	if (this.complianceLevel < ClassFileConstants.JDK1_7)
		return;
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.Serializable;
				public class X implements Serializable {
					static int test(Serializable v) {
				       try {
						    return (int)v;
				       } catch (ClassCastException c) {
				           System.out.println("CCE");
				       }
				       return -1;
					}
					public static void main(String[] args) {
						int i = test(new X());
						System.out.println(i);
					}
				}
				""",
		},
		"CCE\n-1");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428388, [1.8][compiler] Casting to primitives is over tolerant - probable regression since bug 428274
public void test428388e() throws Exception {
	if (this.complianceLevel < ClassFileConstants.JDK1_7)
		return;
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.Serializable;
				public class X implements Serializable {
					static int test(Serializable v) {
				       try {
						    return (int)v;
				       } catch (ClassCastException c) {
				           System.out.println("CCE");
				       }
				       return -1;
					}
					public static void main(String[] args) {
						int i = test(new Long(1234));
						System.out.println(i);
					}
				}
				""",
		},
		"CCE\n-1");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428388, [1.8][compiler] Casting to primitives is over tolerant - probable regression since bug 428274
public void test428388f() throws Exception {
	if (this.complianceLevel < ClassFileConstants.JDK1_7)
		return;
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.Serializable;
				public class X implements Serializable {
					static int test(Serializable v) {
				       try {
						    return (int)v;
				       } catch (ClassCastException c) {
				           System.out.println("CCE");
				       }
				       return -1;
					}
					public static void main(String[] args) {
						int i = test(new Integer(1234));
						System.out.println(i);
					}
				}
				""",
		},
		"1234");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428388, [1.8][compiler] Casting to primitives is over tolerant - probable regression since bug 428274
public void test428388g() throws Exception {
	if (this.complianceLevel < ClassFileConstants.JDK1_7)
		return;
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.io.Serializable;
				public class X implements Serializable {
				  static <S extends Boolean & Serializable>int test(S b) {
				    return (int) b;
				  }
				
				  public static void main(String[] args) {
				    int i = test(Boolean.TRUE);
				    System.out.println(i);
				  }
				}
				""",
		},
		"""
			----------
			1. WARNING in X.java (at line 2)
				public class X implements Serializable {
				             ^
			The serializable class X does not declare a static final serialVersionUID field of type long
			----------
			2. WARNING in X.java (at line 3)
				static <S extends Boolean & Serializable>int test(S b) {
				                  ^^^^^^^
			The type parameter S should not be bounded by the final type Boolean. Final types cannot be further extended
			----------
			3. ERROR in X.java (at line 4)
				return (int) b;
				       ^^^^^^^
			Cannot cast from S to int
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428388, [1.8][compiler] Casting to primitives is over tolerant - probable regression since bug 428274
public void test428388h() throws Exception {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return; // uses intersection cast
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.io.Serializable;
				public class X implements Serializable {
				  static int test(Serializable b) {
				    return (int) (Boolean & Serializable) b;
				  }
				  public static void main(String[] args) {
				    int i = test(Boolean.TRUE);
				    System.out.println(i);
				  }
				}
				""",
		},
		"""
			----------
			1. WARNING in X.java (at line 2)
				public class X implements Serializable {
				             ^
			The serializable class X does not declare a static final serialVersionUID field of type long
			----------
			2. ERROR in X.java (at line 4)
				return (int) (Boolean & Serializable) b;
				       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Cannot cast from Boolean & Serializable to int
			----------
			""");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428388, [1.8][compiler] Casting to primitives is over tolerant - probable regression since bug 428274
public void test428388i() throws Exception {
	if (this.complianceLevel < ClassFileConstants.JDK1_7)
		return;
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.Serializable;
				public class X implements Serializable {
					static int test(Serializable v) {
				       try {
						    return (int)v;
				       } catch (ClassCastException c) {
				           System.out.println("CCE");
				       }
				       return -1;
					}
					public static void main(String[] args) {
						int i = test(new Integer(1234));
						System.out.println(i);
					}
				}
				""",
		},
		"1234");
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);

	String expectedOutput =
			"""
		  // Method descriptor #17 (Ljava/io/Serializable;)I
		  // Stack: 2, Locals: 2
		  static int test(java.io.Serializable v);
		     0  aload_0 [v]
		     1  checkcast java.lang.Integer [18]
		     4  invokevirtual java.lang.Integer.intValue() : int [20]
		     7  ireturn
		     8  astore_1 [c]
		     9  getstatic java.lang.System.out : java.io.PrintStream [24]
		    12  ldc <String "CCE"> [30]
		    14  invokevirtual java.io.PrintStream.println(java.lang.String) : void [32]
		    17  iconst_m1
		    18  ireturn
		""";

	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 2));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, actualOutput);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428388, [1.8][compiler] Casting to primitives is over tolerant - probable regression since bug 428274
public void test428388j() throws Exception {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return; // uses intersection cast
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.Serializable;
				public class X implements Serializable {
				  static int test(Serializable b) {
				    return (int) (Integer & Serializable) b;
				  }
				  public static void main(String[] args) {
				    int i = test(10101010);
				    System.out.println(i);
				  }
				}
				""",
		},
		"10101010");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428522,  [1.8] VerifyError when a non primitive type cast to primitive type
public void test428522() throws Exception {
	if (this.complianceLevel < ClassFileConstants.JDK1_7)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public static void main(String args[]) {
				    	long l = (long) ((Object) 100L);
				    	System.out.println("OK");
				    }
				}
				""",
		},
		"OK", customOptions);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);

	String expectedOutput =
			"""
		  // Method descriptor #15 ([Ljava/lang/String;)V
		  // Stack: 2, Locals: 1
		  public static void main(java.lang.String[] args);
		     0  ldc2_w <Long 100> [16]
		     3  invokestatic java.lang.Long.valueOf(long) : java.lang.Long [18]
		     6  checkcast java.lang.Long [19]
		     9  invokevirtual java.lang.Long.longValue() : long [24]
		    12  pop2
		    13  getstatic java.lang.System.out : java.io.PrintStream [28]
		    16  ldc <String "OK"> [34]
		    18  invokevirtual java.io.PrintStream.println(java.lang.String) : void [36]
		    21  return
		      Line numbers:
		        [pc: 0, line: 3]
		        [pc: 13, line: 4]
		        [pc: 21, line: 5]
		      Local variable table:
		        [pc: 0, pc: 22] local: args index: 0 type: java.lang.String[]
		}""";
	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 2));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, actualOutput);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428522,  [1.8] VerifyError when a non primitive type cast to primitive type
public void test428522a() throws Exception {
	if (this.complianceLevel < ClassFileConstants.JDK1_7)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public static void main(String args[]) {
				    	long l = (long) ((Object) 100L);
				    	System.out.println("OK");
				    }
				}
				""",
		},
		"OK", customOptions);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);

	String expectedOutput =
			"""
		  // Method descriptor #15 ([Ljava/lang/String;)V
		  // Stack: 2, Locals: 3
		  public static void main(java.lang.String[] args);
		     0  ldc2_w <Long 100> [16]
		     3  invokestatic java.lang.Long.valueOf(long) : java.lang.Long [18]
		     6  checkcast java.lang.Long [19]
		     9  invokevirtual java.lang.Long.longValue() : long [24]
		    12  lstore_1 [l]
		    13  getstatic java.lang.System.out : java.io.PrintStream [28]
		    16  ldc <String "OK"> [34]
		    18  invokevirtual java.io.PrintStream.println(java.lang.String) : void [36]
		    21  return
		      Line numbers:
		        [pc: 0, line: 3]
		        [pc: 13, line: 4]
		        [pc: 21, line: 5]
		      Local variable table:
		        [pc: 0, pc: 22] local: args index: 0 type: java.lang.String[]
		        [pc: 13, pc: 22] local: l index: 1 type: long
		}""";
	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 2));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, actualOutput);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428522,  [1.8] VerifyError when a non primitive type cast to primitive type
public void test428522b() throws Exception {
	if (this.complianceLevel < ClassFileConstants.JDK1_7)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public static void main(String args[]) {
				       try {
				    	    int l = (int) ((Object) 100L);
				       } catch (ClassCastException c) {
				    	    System.out.println("CCE:OK");
				       }
				    }
				}
				""",
		},
		"CCE:OK", customOptions);

}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428522,  [1.8] VerifyError when a non primitive type cast to primitive type
public void test428522c() throws Exception {
	if (this.complianceLevel < ClassFileConstants.JDK1_7)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public static void main(String args[]) {
				    	int l = (int) ((Object) 100);
				    	System.out.println("OK");
				    }
				}
				""",
		},
		"OK", customOptions);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);

	String expectedOutput =
			"""
		  // Method descriptor #15 ([Ljava/lang/String;)V
		  // Stack: 2, Locals: 1
		  public static void main(java.lang.String[] args);
		     0  bipush 100
		     2  invokestatic java.lang.Integer.valueOf(int) : java.lang.Integer [16]
		     5  checkcast java.lang.Integer [17]
		     8  invokevirtual java.lang.Integer.intValue() : int [22]
		    11  pop
		    12  getstatic java.lang.System.out : java.io.PrintStream [26]
		    15  ldc <String "OK"> [32]
		    17  invokevirtual java.io.PrintStream.println(java.lang.String) : void [34]
		    20  return
		      Line numbers:
		        [pc: 0, line: 3]
		        [pc: 12, line: 4]
		        [pc: 20, line: 5]
		      Local variable table:
		        [pc: 0, pc: 21] local: args index: 0 type: java.lang.String[]
		}""";
	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 2));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, actualOutput);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=441731 JDT reports unnecessary cast, using the Quickfix to remove it creates syntax error
public void test441731() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface MUIElement {}
				interface MUIElementContainer<T extends MUIElement> extends MUIElement{}
				interface MWindowElement extends MUIElement {}
				interface MWindow extends MUIElementContainer<MWindowElement> {}
				public class X {
					void test(MUIElementContainer<MUIElement> me) {
						if(((MUIElement) me) instanceof MWindow) return;
						MWindow mw = (MWindow)((MUIElement)me);
					}
				}
				"""
		},
		customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=448112, [compiler] Compiler crash (ArrayIndexOutOfBoundsException at StackMapFrame.addStackItem()) with unused variable
public void test448112() throws Exception {
	if (this.complianceLevel < ClassFileConstants.JDK1_7)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				
				  static class Y {
					  public Object getAttribute(String name) {
					  	return new Long(100L);
					  }
					}
					public static void foo2(Y y) {
				
						try {
							long v1 = (Long) y.getAttribute("v1");
							long v2 = (Long) y.getAttribute("v2");
				
							System.out.println(String.valueOf(v1));
				
						} catch (java.lang.Throwable t) {}
					}
				\t
					public static void main(String args[]) {
						foo2(new Y());
				  }
				}""",
		},
		"100", customOptions);
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
	String actualOutput =
		disassembler.disassemble(
			classFileBytes,
			"\n",
			ClassFileBytesDisassembler.DETAILED);

	String expectedOutput =
			"""
		  public static void foo2(X.Y y);
		     0  aload_0 [y]
		     1  ldc <String "v1"> [16]
		     3  invokevirtual X$Y.getAttribute(java.lang.String) : java.lang.Object [18]
		     6  checkcast java.lang.Long [24]
		     9  invokevirtual java.lang.Long.longValue() : long [26]
		    12  lstore_1 [v1]
		    13  aload_0 [y]
		    14  ldc <String "v2"> [30]
		    16  invokevirtual X$Y.getAttribute(java.lang.String) : java.lang.Object [18]
		    19  checkcast java.lang.Long [24]
		    22  pop
		    23  getstatic java.lang.System.out : java.io.PrintStream [32]
		    26  lload_1 [v1]
		    27  invokestatic java.lang.String.valueOf(long) : java.lang.String [38]
		    30  invokevirtual java.io.PrintStream.println(java.lang.String) : void [44]
		    33  goto 37
		    36  pop
		    37  return
		""";
	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 2));
	}
	if (index == -1) {
		assertEquals("Wrong contents", expectedOutput, actualOutput);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=461706 [1.8][compiler] "Unnecessary cast" problems for necessary cast in lambda expression
public void test461706() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"Bug.java",
			"""
				import java.util.ArrayList;
				import java.util.List;
				public class Bug {
					private static class AndCondition implements ICondition {
						public AndCondition(ICondition cond1, ICondition cond2) {
							// todo
						}
					}
					private static class SimpleCondition implements ICondition {
					}
					private static interface ICondition {
						ICondition TRUE = new SimpleCondition();
						default ICondition and(final ICondition cond) {
							return new AndCondition(this, cond);
						}
					}
					public static void main(final String[] args) {
						final List<SimpleCondition> conditions = new ArrayList<>();
						conditions.stream()
								.map(x -> (ICondition)x)
								.reduce((x, y) -> x.and(y))
								.orElse(ICondition.TRUE);
					}
				}"""
		},
		customOptions);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=461706 [1.8][compiler] "Unnecessary cast" problems for necessary cast in lambda expression
public void test461706a() {
	if (this.complianceLevel < ClassFileConstants.JDK1_8)
		return;
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.WARNING);
	runner.testFiles =
		new String[] {
			"Bug.java",
			"""
				import java.util.ArrayList;
				import java.util.List;
				public class Bug {
					private static class AndCondition implements ICondition {
						public AndCondition(ICondition cond1, ICondition cond2) {
							// todo
						}
					}
					static class SimpleCondition implements ICondition {
					}
					private static interface ICondition {
						ICondition TRUE = new SimpleCondition();
						default ICondition and(final ICondition cond) {
							return new AndCondition(this, cond);
						}
					}
					public static void main(final String[] args) {
						final List<ICondition> conditions = new ArrayList<>();
						conditions.stream()
								.map(x -> (ICondition)x)
								.reduce((x, y) -> x.and(y))
								.orElse(ICondition.TRUE);
					}
				}"""
		};
	runner.expectedCompilerLog =
		"""
			----------
			1. WARNING in Bug.java (at line 20)
				.map(x -> (ICondition)x)
				          ^^^^^^^^^^^^^
			Unnecessary cast from Bug.ICondition to Bug.ICondition
			----------
			""";
	runner.runWarningTest();
}
public void testAnonymous_bug520727() {
	String[] source = {
		"O.java",
		"""
			import java.io.Serializable;
			public class O {
				Object in = new Object() {
			        public Object foo() {
			                return (Serializable) this;
			        }
				};
			}
			"""
	};
	if (this.complianceLevel < ClassFileConstants.JDK9) {
		runNegativeTest(source,
				"""
					----------
					1. ERROR in O.java (at line 5)
						return (Serializable) this;
						       ^^^^^^^^^^^^^^^^^^^
					Cannot cast from new Object(){} to Serializable
					----------
					""");
	} else {
		// starting from JLS 9, anonymous classes are *not* final, hence casting is legal:
		runConformTest(source,"");
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=543727 False positive "Unnecessary cast"
public void test543727() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"Bug.java",
			"""
				import java.util.ArrayList;
				import java.util.List;
				public class Bug {
				   public static void main(String[] args) {
				       List<Comparable<?>> vector = new ArrayList<>();
				       vector.add(0);
				       if (vector.get(0) == (Integer)0) {
				           System.out.print("SUCCESS");
				       }
				   }\
				}
				""",
		},
		"SUCCESS");
}
public void test543727_notequals() {
	if (this.complianceLevel < ClassFileConstants.JDK1_7)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"Bug.java",
			"""
				import java.util.ArrayList;
				import java.util.List;
				public class Bug {
				   public static void main(String[] args) {
				       List<Comparable<?>> vector = new ArrayList<>();
				       vector.add(0);
				       if (vector.get(0) != (Integer)1) {
				           System.out.print("SUCCESS");
				       }
				   }\
				}
				""",
		},
		"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=548647 JDT reports unnecessary cast, using the Quickfix to remove it creates syntax error
public void test548647() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface MUIElement {}
				interface MUIElementContainer<T extends MUIElement> extends MUIElement{}
				interface MWindowElement extends MUIElement {}
				interface MWindow extends MUIElementContainer<MWindowElement> {}
				public class X {
					MUIElementContainer<MUIElement> field;
					MUIElementContainer<MUIElement> getField() {
						return field;
					}
					void test(MUIElementContainer<MUIElement> me) {
						MUIElementContainer<MUIElement> localVar = me;
						if ((Object) localVar instanceof MWindow) return;
						if(((Object) me) instanceof MWindow) return;
						if ((MUIElement)field instanceof MWindow) return;
						if ((MUIElement)getField() instanceof MWindow) return;
						MWindow mw = (MWindow)((MUIElement)me);
					}
				}
				"""
		},
		customOptions);
}
public void test548647a() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.WARNING);
	runner.testFiles =
		new String[] {
			"Bug.java",
			"""
				public class Bug {
					Integer k;
					private Number getK() { return k; }
					public void fn(Number n) {
						Number j = n;
						if ((Number) n instanceof Long) return;
						if ((Number) k instanceof Integer) return;
						if ((Number) j instanceof Integer) return;
						if ((Number) getK() instanceof Integer) return;
					}
				}"""
		};
	runner.expectedCompilerLog =
			"""
				----------
				1. WARNING in Bug.java (at line 6)
					if ((Number) n instanceof Long) return;
					    ^^^^^^^^^^
				Unnecessary cast from Number to Number
				----------
				2. WARNING in Bug.java (at line 7)
					if ((Number) k instanceof Integer) return;
					    ^^^^^^^^^^
				Unnecessary cast from Integer to Number
				----------
				3. WARNING in Bug.java (at line 8)
					if ((Number) j instanceof Integer) return;
					    ^^^^^^^^^^
				Unnecessary cast from Number to Number
				----------
				4. WARNING in Bug.java (at line 9)
					if ((Number) getK() instanceof Integer) return;
					    ^^^^^^^^^^^^^^^
				Unnecessary cast from Number to Number
				----------
				""";
	runner.runWarningTest();
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=472466 [compiler] bogus warning "unnecessary cast"
public void test472466() {
	if (this.complianceLevel < ClassFileConstants.JDK1_5)
		return;
	Runner runner = new Runner();
	runner.customOptions = getCompilerOptions();
	runner.customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.WARNING);
	runner.testFiles =
		new String[] {
			"X.java",
			"""
				public class X {
					public int foo() {
						Object x = 4;
						Integer y = 5;
						if (x == (Object)50) return -1;
						if (x == (Integer)50) return -2;
						if ((Integer)x == (Integer)50) return -3;
						if (y == 7) return -4;
						if ((Integer)y == 9) return -5;
						if ((Object)50 == x) return -6;
						if ((Integer)50 == x) return -7;
						if ((Integer)50 == (Integer)x) return -8;
						if (7 == y) return -9;
						return 0;
					}
				}
				"""
		};
	runner.expectedCompilerLog =
			"""
				----------
				1. WARNING in X.java (at line 9)
					if ((Integer)y == 9) return -5;
					    ^^^^^^^^^^
				Unnecessary cast from Integer to Integer
				----------
				""";
	runner.runWarningTest();
}

public void testBug561167() {
	if (this.complianceLevel < ClassFileConstants.JDK10)
		return;
	Map customOptions = getCompilerOptions();
	customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
	runNegativeTest(
		// test directory preparation
		true /* flush output directory */,
		new String[] { /* test files */
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						var s = (String) null;	// Necessary
						var t = (String) "hello";	// UNnecessary
						var f = (float) 12;			// Necessary
						var g = (float)f;			// UNnecessary
					}
				}
				"""
		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		"""
			----------
			1. ERROR in X.java (at line 4)
				var t = (String) "hello";	// UNnecessary
				        ^^^^^^^^^^^^^^^^
			Unnecessary cast from String to String
			----------
			2. ERROR in X.java (at line 6)
				var g = (float)f;			// UNnecessary
				        ^^^^^^^^
			Unnecessary cast from float to float
			----------
			""",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=572534
// ClassCastException LocalTypeBinding cannot be cast to ParameterizedTypeBinding in inferDiamondConstructor
public void testBug572534() {
	if (this.complianceLevel > ClassFileConstants.JDK1_8) {
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.ERROR);
		customOptions.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
		runNegativeTest(
			// test directory preparation
			true /* flush output directory */,
			new String[] { /* test files */
				"X.java",
				"""
					import java.util.List;
					
					public class X {
					\t
						public static void main(String[] args) throws Exception {
							List<String> list = null;
							Object foo = null;
							list = new ObjectMapper2().readValue((String)foo, new TypeReference2<>() { /*  */ });
						\t
							// Commenting out the previous line and explicitly typing the TypeReference works around it
							list = new ObjectMapper2().readValue((String)foo, new TypeReference2<List<String>>() { /*  */ });
							System.out.println(list);
						}
					\t
						private static class TypeReference2<T> implements Comparable<TypeReference2<T>> {
							@Override
							public int compareTo(TypeReference2<T> o) {
								return 0;
							}
						}
					   private void unused() {}
					
						private static class ObjectMapper2 {
							private <T> T readValue(String content, TypeReference2<T> valueTypeRef) {
								return readValue(content, "");
							}
					
							private <T> T readValue(String content, String foo) {
								return null;
							}
						}
					}
					"""

		},
		// compiler options
		null /* no class libraries */,
		customOptions /* custom options */,
		"""
			----------
			1. WARNING in X.java (at line 21)
				private void unused() {}
				             ^^^^^^^^
			The method unused() from the type X is never used locally
			----------
			""",
		// javac options
		JavacTestOptions.Excuse.EclipseWarningConfiguredAsError /* javac test options */);
	}
}

public static Class testClass() {
	return CastTest.class;
}
}
