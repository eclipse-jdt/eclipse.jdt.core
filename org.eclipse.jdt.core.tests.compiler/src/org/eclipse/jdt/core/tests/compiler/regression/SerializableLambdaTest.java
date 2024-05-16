/*******************************************************************************
 * Copyright (c) 2014, 2017 GoPivotal, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *        Andy Clement (GoPivotal, Inc) aclement@gopivotal.com - Contributions for
 *                          Bug 405104 - [1.8][compiler][codegen] Implement support for serializeable lambdas
 *                          Bug 439889 - [1.8][compiler] [lambda] Deserializing lambda fails with IllegalArgumentException: "Invalid lambda deserialization"
 *                          Bug 442416 - $deserializeLambda$ missing cases for nested lambdas
 *                          Bug 442418 - $deserializeLambda$ off-by-one error when deserializing the captured arguments of a lambda that also capture this
 *                          Bug 449467 - [1.8][compiler] Invalid lambda deserialization with anonymous class
 *        Olivier Tardieu tardieu@us.ibm.com - Contributions for
 *                          Bug 442416 - $deserializeLambda$ missing cases for nested lambdas
 *                          Bug 442418 - $deserializeLambda$ off-by-one error when deserializing the captured arguments of a lambda that also capture this
 *        IBM Corporation - Additional tests
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.Map;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.core.util.IBootstrapMethodsEntry;
import org.eclipse.jdt.core.util.IClassFileAttribute;
import org.eclipse.jdt.core.util.IClassFileReader;
import org.eclipse.jdt.core.util.IConstantPool;
import org.eclipse.jdt.core.util.IConstantPoolConstant;
import org.eclipse.jdt.core.util.IConstantPoolEntry;
import org.eclipse.jdt.core.util.IConstantPoolEntry2;
import org.eclipse.jdt.core.util.IMethodInfo;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.util.BootstrapMethodsAttribute;

import junit.framework.Test;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class SerializableLambdaTest extends AbstractRegressionTest {

	static {
//		TESTS_NUMBERS = new int [] { 40 };
//		TESTS_NAMES = new String[] { "testTypeVariable" };
	}

	public static Class testClass() {
		return SerializableLambdaTest.class;
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_8);
	}
	public SerializableLambdaTest(String testName){
		super(testName);
	}

	// Enables the tests to run individually
	@Override
	protected Map getCompilerOptions() {
		Map defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_8);
		defaultOptions.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_8);
		defaultOptions.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_8);
		return defaultOptions;
	}

	public static final String RUNNER_CLASS =
		"""
		public class Y {
		  public static void main(String[]args) {
		    com.foo.X.main(args);
		  }
		}""";

	private static final String HELPER_CLASS =
		"""
		package util;
		import java.io.*;
		public class Helper {
		public static void print(Object o ) {System.err.println(o);}
		static byte[][] data;
		
		public static void write(Object o) { write(0,o); }
		public static void write(int i, Object o) {
		    if (data==null) data=new byte[10][];
		    try {
		        ByteArrayOutputStream baos = new ByteArrayOutputStream();
		        ObjectOutputStream oos = new ObjectOutputStream(baos);
		        oos.writeObject(o);
		        oos.flush();
		        oos.close();
		        data[i] = baos.toByteArray();
		    } catch (Exception e) {
		    }
		}
		
		public static Object read() { return read(0); }
		public static Object read(int i) {
		    try {
		        ByteArrayInputStream bais = new ByteArrayInputStream(data[i]);
		        ObjectInputStream ois = new ObjectInputStream(bais);
		        Object o = ois.readObject();
		        ois.close();
		        return o;
		    } catch (Exception e) {
		    }
		    return null;
		}
		}
		""";

	/**
	 * Verifies that after deserializing it is usable, also that the bootstrap methods attribute indicates use of altMetafactory
	 */
	public void test001_simple() throws Exception {
		this.runConformTest(
				new String[]{
					"X.java",
					"""
						import java.io.*;
						public class X {
						    interface Foo extends Serializable { int m(); }
						
						    public static void main(String[] args) {
						        Foo f1 = null;
						        f1 = () -> 3;
						        util.Helper.write(f1);
						        f1 = (Foo)util.Helper.read();
						        System.out.println(f1.m());
						    }
						}
						""",
					"Helper.java",HELPER_CLASS,
					},
					"3",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"""
			0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;
			  Method arguments:
			    ()I
			    invokestatic X.lambda$0:()I
			    ()I
			    1
			""";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "X.class");
		checkExpected(expectedOutput,data);
	}

	/**
	 * Sanity test, non serializable should have bootstrap methods attribute reference to metafactory.
	 */
	public void test002_simpleNonSerializable() throws Exception {
		this.runConformTest(
				new String[]{
					"X.java",
					"""
						import java.io.*;
						public class X {
						    interface Foo { int m(); }
						
						    public static void main(String[] args) {
						        Foo f1 = null;
						        f1 = () -> 3;
						        System.out.println(f1.m());
						    }
						}
						""",
					"Helper.java",HELPER_CLASS,
					},
					"3");
		String expectedOutput =
				"""
			0: invokestatic java/lang/invoke/LambdaMetafactory.metafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;
			  Method arguments:
			    ()I
			    invokestatic X.lambda$0:()I
			    ()I
			""";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "X.class");
		checkExpected(expectedOutput,data);
	}

	/**
	 * Basic test that deserializeLambda can cope with two lambda expressions.
	 */
	public void test003_twoSerializedLambdas() throws Exception {
		this.runConformTest(
				new String[]{
					"X.java",
					"""
						import java.io.*;
						public class X {
						    interface Foo extends Serializable { int m(); }
						
						    public static void main(String[] args) {
						        Foo f1 = null, f2 = null;
						        f1 = () -> 33;
						        f2 = () -> 99;
						        util.Helper.write(0,f1);
						        util.Helper.write(1,f2);
						        f2 = (Foo)util.Helper.read(1);
						        f1 = (Foo)util.Helper.read(0);
						        System.out.println(f1.m());
						        System.out.println(f2.m());
						    }
						}
						""",
					"Helper.java",HELPER_CLASS,
					},
					"33\n99",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"""
			0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;
			  Method arguments:
			    ()I
			    invokestatic X.lambda$0:()I
			    ()I
			    1
			1: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;
			  Method arguments:
			    ()I
			    invokestatic X.lambda$1:()I
			    ()I
			    1
			""";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "X.class");
		checkExpected(expectedOutput,data);
	}

	public void test004_lambdaWithParameterInPackage() throws Exception {
		this.runConformTest(
				new String[]{
					"Y.java",
					"""
						public class Y {
						  public static void main(String[]args) {
						    com.foo.X.main(args);
						  }
						}""",
					"X.java",
					"""
						package com.foo;
						import java.io.*;
						public class X {
						    interface Foo extends Serializable { int m(int i); }
						
						    public static void main(String[] args) {
						        Foo f1 = null, f2 = null;
						        f1 = (i) -> i*2;
						        util.Helper.write(f1);
						        f1 = (Foo)util.Helper.read();
						        System.out.println(f1.m(4));
						    }
						}
						""",
					"Helper.java",HELPER_CLASS,
					},
					"8",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"""
			0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;
			  Method arguments:
			    (I)I
			    invokestatic com/foo/X.lambda$0:(I)I
			    (I)I
			    1
			""";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "com"+File.separator+"foo"+File.separator+"X.class");
		checkExpected(expectedOutput,data);
	}

	public void test005_capturingVariableLambdaWithParameterInPackage() throws Exception {
		this.runConformTest(
				new String[]{
					"Y.java",
					"""
						public class Y {
						  public static void main(String[]args) {
						    com.foo.X.main(args);
						  }
						}""",
					"X.java",
					"""
						package com.foo;
						import java.io.*;
						public class X {
						    interface Foo extends Serializable { int m(int i); }
						
						    public static void main(String[] args) {
						        Foo f1 = null;
						        int multiplier = 3;
						        f1 = (i) -> i * multiplier;
						        util.Helper.write(f1);
						        f1 = (Foo)util.Helper.read();
						        System.out.println(f1.m(4));
						    }
						}
						""",
					"Helper.java",HELPER_CLASS,
					},
					"12",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"""
			0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;
			  Method arguments:
			    (I)I
			    invokestatic com/foo/X.lambda$0:(II)I
			    (I)I
			    1
			""";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "com"+File.separator+"foo"+File.separator+"X.class");
		checkExpected(expectedOutput,data);
	}

	// differing types, not just int
	public void test006_capturingVariableLambdaWithParameterInPackage() throws Exception {
		this.runConformTest(
				new String[]{
					"Y.java",
					"""
						public class Y {
						  public static void main(String[]args) {
						    com.foo.X.main(args);
						  }
						}""",
					"X.java",
					"""
						package com.foo;
						import java.io.*;
						public class X {
						    interface Foo extends Serializable { int m(String n); }
						
						    public static void main(String[] args) {
						        Foo f1 = null;
						        int multiplier = 3;
						        f1 = (n) -> Integer.valueOf(n) * multiplier;
						        util.Helper.write(f1);
						        f1 = (Foo)util.Helper.read();
						        System.out.println(f1.m("33"));
						    }
						}
						""",
					"Helper.java",HELPER_CLASS,
					},
					"99",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"""
			0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;
			  Method arguments:
			    (Ljava/lang/String;)I
			    invokestatic com/foo/X.lambda$0:(ILjava/lang/String;)I
			    (Ljava/lang/String;)I
			    1
			""";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "com"+File.separator+"foo"+File.separator+"X.class");
		checkExpected(expectedOutput,data);
	}

	// Fails the same way as javac right now... with NPE (b120)
	public void xtest007_capturingFieldLambdaWithParameterInPackage() throws Exception {
		this.runConformTest(
				new String[]{
					"Y.java",
					"""
						public class Y {
						  public static void main(String[]args) {
						    com.foo.X.main(args);
						  }
						}""",
					"X.java",
					"""
						package com.foo;
						import java.io.*;
						public class X {
						    int multiplier = 3;
						    interface Foo extends Serializable { int m(int i); }
						
						    public static void main(String[] args) {
						      new X().run();
						    }
						    public void run() {
						        Foo f1 = null;
						        f1 = (i) -> i * this.multiplier;
						        util.Helper.write(f1);
						        f1 = (Foo)util.Helper.read();
						        System.out.println(f1.m(4));
						    }
						}
						""",
					"Helper.java",HELPER_CLASS,
					},
					"12",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"""
			0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;
			  Method arguments:
			    (I)I
			    invokestatic com/foo/X.lambda$0:(II)I
			    (I)I
			    1
			""";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "com"+File.separator+"foo"+File.separator+"X.class");
		checkExpected(expectedOutput,data);
	}

	public void test008_capturingTwoVariableLambdaWithParameterInPackage() throws Exception {
		this.runConformTest(
				new String[]{
					"Y.java",
					"""
						public class Y {
						  public static void main(String[]args) {
						    com.foo.X.main(args);
						  }
						}""",
					"X.java",
					"""
						package com.foo;
						import java.io.*;
						public class X {
						    interface Foo extends Serializable { float m(int i, float f); }
						
						    public static void main(String[] args) {
						      new X().run();
						    }
						    public void run() {
						        Foo f1 = null;
						        f1 = (i,f) -> ((float)i) * f;
						        util.Helper.write(f1);
						        f1 = (Foo)util.Helper.read();
						        System.out.println(f1.m(3,4.0f));
						    }
						}
						""",
					"Helper.java",HELPER_CLASS,
					},
					"12.0",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"""
			0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;
			  Method arguments:
			    (IF)F
			    invokestatic com/foo/X.lambda$0:(IF)F
			    (IF)F
			    1
			""";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "com"+File.separator+"foo"+File.separator+"X.class");
		checkExpected(expectedOutput,data);
	}

	public void test009_capturingTwoSlotVariablesLambdaWithParameterInPackage() throws Exception {
		this.runConformTest(
				new String[]{
					"Y.java",RUNNER_CLASS,
					"X.java",
					"""
						package com.foo;
						import java.io.*;
						public class X {
						    interface Foo extends Serializable { double m(int i, long l); }
						
						    public static void main(String[] args) {
						      new X().run();
						    }
						    public void run() {
						        Foo f1 = null;
						        f1 = (i,l) -> (double)(i*l);
						        util.Helper.write(f1);
						        f1 = (Foo)util.Helper.read();
						        System.out.println(f1.m(3,40L));
						    }
						}
						""",
					"Helper.java",HELPER_CLASS,
					},
					"120.0",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"""
			0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;
			  Method arguments:
			    (IJ)D
			    invokestatic com/foo/X.lambda$0:(IJ)D
			    (IJ)D
			    1
			""";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "com"+File.separator+"foo"+File.separator+"X.class");
		checkExpected(expectedOutput,data);
	}

	public void test010_VarargsLambdaExpression() throws Exception {
		this.runConformTest(
				new String[]{
					"Y.java",RUNNER_CLASS,
					"X.java",
					"""
						package com.foo;
						import java.io.*;
						public class X {
						    interface Foo extends Serializable { String m(String... ss); }
						
						    public static void main(String[] args) {
						      new X().run();
						    }
						    public void run() {
						        Foo f1 = null;
						        f1 = (strings) -> strings[0]+strings[1];
						        util.Helper.write(f1);
						        f1 = (Foo)util.Helper.read();
						        System.out.println(f1.m("abc","def"));
						    }
						}
						""",
					"Helper.java",HELPER_CLASS,
					},
					"abcdef",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"""
			0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;
			  Method arguments:
			    ([Ljava/lang/String;)Ljava/lang/String;
			    invokestatic com/foo/X.lambda$0:([Ljava/lang/String;)Ljava/lang/String;
			    ([Ljava/lang/String;)Ljava/lang/String;
			    1
			""";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "com"+File.separator+"foo"+File.separator+"X.class");
		checkExpected(expectedOutput,data);
	}

	// Fails same way as javac right now... with an NPE (b120)
	public void xtest011_CapturingInstance() throws Exception {
		this.runConformTest(
				new String[]{
					"Y.java",RUNNER_CLASS,
					"X.java",
					"""
						package com.foo;
						import java.io.*;
						public class X {
						    interface Foo extends Serializable { String m(); }
						
						    String fieldValue = "hello";
						    public static void main(String[] args) {
						      new X().run();
						    }
						    public void run() {
						        Foo f1 = null;
						        f1 = () -> this.fieldValue;
						        util.Helper.write(f1);
						        f1 = (Foo)util.Helper.read();
						        System.out.println(f1.m());
						    }
						}
						""",
					"Helper.java",HELPER_CLASS,
					},
					"abcdef",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"""
			0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;
			  Method arguments:
			    ([Ljava/lang/String;)Ljava/lang/String;
			    invokestatic com/foo/X.lambda$0:([Ljava/lang/String;)Ljava/lang/String;
			    ([Ljava/lang/String;)Ljava/lang/String;
			    1
			""";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "com"+File.separator+"foo"+File.separator+"X.class");
		checkExpected(expectedOutput,data);
	}

	public void test012_intersectionCast() throws Exception {
		this.runConformTest(
				new String[]{
					"X.java",
					"""
						import java.io.*;
						public class X {
						    interface Foo extends Serializable { int m(); }
						    interface Marker {}
						
						    public static void main(String[] args) {
						        Foo f1 = null;
						        f1 = (Foo & Marker) () -> 3;
						        System.out.println("isMarker?"+(f1 instanceof Marker));
						        util.Helper.write(f1);
						        f1 = (Foo)util.Helper.read();
						        System.out.println(f1.m());
						        System.out.println("isMarker?"+(f1 instanceof Marker));
						    }
						}
						""",
					"Helper.java",HELPER_CLASS,
					},
					"isMarker?true\n3\nisMarker?true",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n"+
				"  Method arguments:\n"+
				"    ()I\n"+
				"    invokestatic X.lambda$0:()I\n"+
				"    ()I\n"+
				"    3\n"+ // BitFlags: 0x01 = FLAG_SERIALIZABLE 0x02 = FLAG_MARKER
				"    1\n"+ // Marker interface count
				"    X$Marker\n";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "X.class");
		checkExpected(expectedOutput,data);
	}

	public void test013_intersectionCast() throws Exception {
		this.runConformTest(
				new String[]{
					"X.java",
					"""
						import java.io.*;
						interface Goo {}
						public class X {
						    interface Foo extends Serializable { int m(); }
						    interface Marker {}
						
						    public static void main(String[] args) {
						        Foo f1 = null;
						        f1 = (Foo & Goo & Serializable & Marker) () -> 3;
						        System.out.println("isMarker?"+(f1 instanceof Marker));
						        System.out.println("isGoo?"+(f1 instanceof Goo));
						        System.out.println("isSerializable?"+(f1 instanceof Serializable));
						        util.Helper.write(f1);
						        f1 = (Foo)util.Helper.read();
						        System.out.println(f1.m());
						        System.out.println("isMarker?"+(f1 instanceof Marker));
						        System.out.println("isGoo?"+(f1 instanceof Goo));
						        System.out.println("isSerializable?"+(f1 instanceof Serializable));
						    }
						}
						""",
					"Helper.java",HELPER_CLASS,
					},
					"isMarker?true\nisGoo?true\nisSerializable?true\n3\nisMarker?true\nisGoo?true\nisSerializable?true",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n"+
				"  Method arguments:\n"+
				"    ()I\n"+
				"    invokestatic X.lambda$0:()I\n"+
				"    ()I\n"+
				"    3\n"+ // BitFlags: 0x01 = FLAG_SERIALIZABLE 0x02 = FLAG_MARKER
				"    2\n"+ // Marker interface count
				"    Goo\n"+
				"    X$Marker\n";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "X.class");
		checkExpected(expectedOutput,data);
	}

	public void test014_intersectionCastAndNotSerializable() throws Exception {
		this.runConformTest(
				new String[]{
					"X.java",
					"""
						import java.io.*;
						interface Goo {}
						public class X {
						    interface Foo { int m(); }
						    interface Marker {}
						
						    public static void main(String[] args) {
						        Foo f1 = null;
						        f1 = (Foo & Goo & Marker) () -> 3;
						        System.out.println("isMarker?"+(f1 instanceof Marker));
						        System.out.println("isGoo?"+(f1 instanceof Goo));
						        System.out.println("isSerializable?"+(f1 instanceof Serializable));
						        System.out.println(f1.m());
						    }
						}
						""",
					"Helper.java",HELPER_CLASS,
					},
					"isMarker?true\nisGoo?true\nisSerializable?false\n3",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n"+
				"  Method arguments:\n"+
				"    ()I\n"+
				"    invokestatic X.lambda$0:()I\n"+
				"    ()I\n"+
				"    2\n"+ // BitFlags: 0x02 = FLAG_MARKER
				"    2\n"+ // Marker interface count
				"    Goo\n"+
				"    X$Marker\n";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "X.class");
		checkExpected(expectedOutput,data);
	}

	public void test015_serializableViaIntersectionCast() throws Exception {
		this.runConformTest(
				new String[]{
					"X.java",
					"""
						import java.io.*;
						interface Goo {}
						public class X {
						    interface Foo { int m(); }
						    interface Marker {}
						
						    public static void main(String[] args) {
						        Foo f1 = null;
						        f1 = (Foo & Goo & Serializable & Marker) () -> 3;
						        System.out.println("isMarker?"+(f1 instanceof Marker));
						        System.out.println("isGoo?"+(f1 instanceof Goo));
						        System.out.println("isSerializable?"+(f1 instanceof Serializable));
						        util.Helper.write(f1);
						        f1 = (Foo)util.Helper.read();
						        System.out.println(f1.m());
						        System.out.println("isMarker?"+(f1 instanceof Marker));
						        System.out.println("isGoo?"+(f1 instanceof Goo));
						        System.out.println("isSerializable?"+(f1 instanceof Serializable));
						    }
						}
						""",
					"Helper.java",HELPER_CLASS,
					},
					"isMarker?true\nisGoo?true\nisSerializable?true\n3\nisMarker?true\nisGoo?true\nisSerializable?true",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;\n"+
				"  Method arguments:\n"+
				"    ()I\n"+
				"    invokestatic X.lambda$0:()I\n"+
				"    ()I\n"+
				"    3\n"+ // BitFlags: 0x01 = FLAG_SERIALIZABLE 0x02 = FLAG_MARKER
				"    2\n"+ // Marker interface count
				"    Goo\n"+
				"    X$Marker\n";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "X.class");
		checkExpected(expectedOutput,data);
	}

	// SAM type not first in intersection cast
	public void test016_bug424211() throws Exception {
		this.runConformTest(
				new String[]{
					"X.java",
					"""
						import java.io.Serializable;
						public class X {
							public static void main(String argv[]) throws Exception {
								AutoCloseable one = ((Serializable & AutoCloseable) (() -> {}));
								one.close();
							}
						}"""
					},
					"",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"""
			0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;
			  Method arguments:
			    ()V
			    invokestatic X.lambda$0:()V
			    ()V
			    1
			""";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "X.class");
		checkExpected(expectedOutput,data);
	}

	// Now SAM type first
	public void test017_bug424211() throws Exception {
		this.runConformTest(
				new String[]{
					"X.java",
					"""
						import java.io.Serializable;
						public class X {
							public static void main(String argv[]) throws Exception {
								AutoCloseable one = ((AutoCloseable & Serializable) (() -> {}));
								one.close();
							}
						}"""
					},
					"",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"""
			0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;
			  Method arguments:
			    ()V
			    invokestatic X.lambda$0:()V
			    ()V
			    1
			""";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "X.class");
		checkExpected(expectedOutput,data);
	}

	// Not Serializable but a regular marker interface
	public void test018_bug424211() throws Exception {
		this.runConformTest(
				new String[]{
					"X.java",
					"""
						import java.io.Serializable;
						interface Marker {}
						public class X {
							public static void main(String argv[]) throws Exception {
								AutoCloseable one = ((Marker & AutoCloseable) (() -> {}));
								one.close();
							}
						}"""
					},
					"",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"""
			0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;
			  Method arguments:
			    ()V
			    invokestatic X.lambda$0:()V
			    ()V
			    2
			    1
			    Marker
			""";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "X.class");
		checkExpected(expectedOutput,data);
	}

	// Now SAM type not first and serialization occurring
	public void test019_bug424211() throws Exception {
		this.runConformTest(
				new String[]{
					"X.java",
					"""
						import java.io.Serializable;
						interface SAM {int m();}
						public class X {
							public static void main(String argv[]) throws Exception {
								SAM one = ((Serializable & SAM) (() -> 3));
						        System.out.println(one.m());
						        util.Helper.write(one);
						        one = (SAM)util.Helper.read();
						        System.out.println(one.m());
							}
						}""",
					"Helper.java",HELPER_CLASS,
					},
					"3\n3",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"""
			0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;
			  Method arguments:
			    ()I
			    invokestatic X.lambda$0:()I
			    ()I
			    1
			""";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "X.class");
		checkExpected(expectedOutput,data);
	}

	public void test020_lambdaNames() throws Exception {
		this.runConformTest(
				new String[]{
					"X.java",
					"""
						import java.io.Serializable;
						interface Foo {int m();}
						interface FooN extends Serializable {int m();}
						public class X {
							public static void main(String argv[]) throws Exception {
								AutoCloseable one = () -> {};
						       new X().m();
						       one.close();
							}
						   public void m() { Foo f = () -> 3; System.out.println(f.m());}
						   public void n() { FooN f = () -> 3; System.out.println(f.m());}
						}"""
					},
					"3",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
			"  private static synthetic void lambda$0() throws java.lang.Exception;\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
		expectedOutput =
			"  private static synthetic int lambda$1();\n";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	public void test021_lambdaNamesVariants() throws Exception {
		this.runConformTest(
				new String[]{
					"X.java",
					"""
						import java.io.Serializable;
						interface Foo {int m();}
						interface FooSer extends Serializable {int m();}
						interface FooI {int m(int i);}
						interface FooSerI extends Serializable {int m(int i);}
						public class X {
						
						   Foo instanceField = () -> 1;
						   FooSer instanceFieldSer = () -> 2;
						   static Foo staticField = () -> 3;
						   static FooSer staticFieldSer = () -> 4;
						   FooI instanceFieldI = (i) -> 5;
						   FooSerI instanceFieldSerI = (i) -> 6;
						
							public static void main(String argv[]) throws Exception {
						     int x = 4;
						     Foo a = () -> 1;
						     FooSer b = () -> 2;
						     FooI c = (i) -> 3;
						     FooSerI d = (i) -> 4;
						     Foo e = () -> x;
						     FooSer f = () -> x+1;
						     FooI g = (i) -> x+2;
						     FooSerI h = (i) -> x+3;
							}
						}"""
					},
					"",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"""
			static lambda$2()I
			static lambda$3()I
			static lambda$0()I
			static lambda$1()I
			static lambda$4(I)I
			static lambda$5(I)I
			static lambda$6()I
			static lambda$7()I
			static lambda$8(I)I
			static lambda$9(I)I
			static lambda$10(I)I
			static lambda$11(I)I
			static lambda$12(II)I
			static lambda$13(II)I
			""";
		String actualOutput = printLambdaMethods(OUTPUT_DIR + File.separator + "X.class");
		if (!actualOutput.equals(expectedOutput)) {
			printIt(actualOutput);
			assertEquals(expectedOutput,actualOutput);
		}
	}

	public void test022_nestedLambdas() throws Exception {
		this.runConformTest(
				new String[]{
					"X.java",
					"""
						import java.io.Serializable;
						interface Foo extends Serializable {int m();}
						public class X {
							public static void main(String argv[]) throws Exception {
								Foo f = () -> { return ((Foo)()->33).m();};
						       System.out.println(f.m());
						       util.Helper.write(f);
						       f = (Foo)util.Helper.read();
						       System.out.println(f.m());
							}
						}""",
					"Helper.java",HELPER_CLASS,
					},
					"33\n33",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"""
			0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;
			  Method arguments:
			    ()I
			    invokestatic X.lambda$0:()I
			    ()I
			    1
			1: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;
			  Method arguments:
			    ()I
			    invokestatic X.lambda$1:()I
			    ()I
			    1
			""";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "X.class");
		checkExpected(expectedOutput,data);
	}

	public void test023_lambdasInOtherPlaces_Field() throws Exception {
		this.runConformTest(
				new String[]{
					"X.java",
					"""
						import java.io.Serializable;
						interface Foo extends Serializable {int m();}
						public class X {
						   Foo f = () -> 99;
							public static void main(String argv[]) throws Exception {
						     new X().run();
						   }
						   public void run() {
						       System.out.println(f.m());
						       util.Helper.write(f);
						       f = (Foo)util.Helper.read();
						       System.out.println(f.m());
							}
						}""",
					"Helper.java",HELPER_CLASS,
					},
					"99\n99",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"""
			0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;
			  Method arguments:
			    ()I
			    invokestatic X.lambda$0:()I
			    ()I
			    1
			""";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "X.class");
		checkExpected(expectedOutput,data);
	}

	public void test024_lambdasInOtherPlaces_MethodParameter() throws Exception {
		this.runConformTest(
				new String[]{
					"X.java",
					"""
						import java.io.Serializable;
						interface Foo extends Serializable {int m();}
						public class X {
							public static void main(String argv[]) throws Exception {
						       new X().run(()->33);
						   }
						   public void run(Foo f) {
						       System.out.println(f.m());
						       util.Helper.write(f);
						       f = (Foo)util.Helper.read();
						       System.out.println(f.m());
							}
						}""",
					"Helper.java",HELPER_CLASS,
					},
					"33\n33",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"""
			0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;
			  Method arguments:
			    ()I
			    invokestatic X.lambda$0:()I
			    ()I
			    1
			""";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "X.class");
		checkExpected(expectedOutput,data);
	}

	public void test025_lambdasWithGenericInferencing() throws Exception {
		this.runConformTest(
				new String[]{
					"X.java",
					"""
						import java.io.Serializable;
						import java.util.function.*;
						public class X {
							public static void main(String argv[]) throws Exception {
						       new X().run();
						   }
						   public void run() {
						       IntFunction<Integer> times3 = (IntFunction<Integer> & Serializable) (triple) -> 3 * triple;
						       System.out.println(times3.apply(4));
						       util.Helper.write(times3);
						       times3 = (IntFunction<Integer>)util.Helper.read();
						       System.out.println(times3.apply(4));
							}
						}""",
					"Helper.java",HELPER_CLASS,
					},
					"12\n12",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"""
			0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;
			  Method arguments:
			    (I)Ljava/lang/Object;
			    invokestatic X.lambda$0:(I)Ljava/lang/Integer;
			    (I)Ljava/lang/Integer;
			    1
			""";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "X.class");
		checkExpected(expectedOutput,data);
	}
	public void test026_lambdasInOtherPlaces_Clinit() throws Exception {
		this.runConformTest(
				new String[]{
					"X.java",
					"""
						import java.io.Serializable;
						interface Foo extends Serializable {int m();}
						public class X {
						   static {
						     Foo f = () -> 99;
						   }
							public static void main(String argv[]) throws Exception {
						     new X().run();
						   }
						   public void run() {
						       Foo f = ()->99;
						       System.out.println(f.m());
						       util.Helper.write(f);
						       f = (Foo)util.Helper.read();
						       System.out.println(f.m());
							}
						}""",
					"Helper.java",HELPER_CLASS,
					},
					"99\n99",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
		String expectedOutput =
				"""
			0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;
			  Method arguments:
			    ()I
			    invokestatic X.lambda$0:()I
			    ()I
			    1
			1: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;
			  Method arguments:
			    ()I
			    invokestatic X.lambda$1:()I
			    ()I
			    1
			""";
		String data = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "X.class");
		checkExpected(expectedOutput,data);
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=449467 - [1.8][compiler] Invalid lambda deserialization with anonymous class
	public void test449467() throws Exception {
		this.runConformTest(
				new String[]{
					"TestClass.java",
					"""
						import java.io.ByteArrayInputStream;
						import java.io.ByteArrayOutputStream;
						import java.io.ObjectInputStream;
						import java.io.ObjectOutputStream;
						import java.io.Serializable;
						
						public class TestClass implements Serializable {
						  String msg = "HEY!";
						  OtherClass other;
						
						  public TestClass(StringBuilder sb) {
						    other = new OtherClass() {
						      {
						        other2 = new OtherClass2((Runnable & Serializable) () -> {
						          sb.length();
						          say();
						        });
						      }
						    };
						  }
						
						  public void say() {
						    System.out.println(msg);
						  }
						
						  public static void main(String[] args) throws Exception {
						    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
						    try (ObjectOutputStream out = new ObjectOutputStream(buffer)) {
						      out.writeObject(new TestClass(new StringBuilder()));
						    }
						    try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()))) {
						      TestClass s = (TestClass) in.readObject();
						      s.say();
						    }
						  }
						}
						
						class OtherClass implements Serializable {
						  OtherClass2 other2;
						}
						
						class OtherClass2 implements Serializable {
						  Runnable runnable;
						
						  public OtherClass2(Runnable runnable) {
						    this.runnable = runnable;
						  }
						}
						"""
					},
					"HEY!",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
	}

	public void test449467_2() throws Exception {
		this.runConformTest(
				new String[]{
					"com/foo/TestClass.java",
					"""
						package com.foo;
						import java.io.ByteArrayInputStream;
						import java.io.ByteArrayOutputStream;
						import java.io.ObjectInputStream;
						import java.io.ObjectOutputStream;
						import java.io.Serializable;
						public class TestClass implements Serializable {
						  String msg = "HEY!";
						  OtherClass other;
						
						  public TestClass(StringBuilder sb) {
						    other = new OtherClass() {
						      {
						        other2 = new OtherClass2((Runnable & Serializable) () -> {
						          sb.length();
						          say();
						        });
						      }
						    };
						  }
						
						  public void say() {
						    System.out.println(msg);
						  }
						
						  public static void main(String[] args) throws Exception {
						    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
						    try (ObjectOutputStream out = new ObjectOutputStream(buffer)) {
						      out.writeObject(new TestClass(new StringBuilder()));
						    }
						    try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()))) {
						      TestClass s = (TestClass) in.readObject();
						      s.say();
						    }
						  }
						}
						
						class OtherClass implements Serializable {
						  OtherClass2 other2;
						}
						
						class OtherClass2 implements Serializable {
						  Runnable runnable;
						
						  public OtherClass2(Runnable runnable) {
						    this.runnable = runnable;
						  }
						}
						"""
					},
					"HEY!",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428552,  [1.8][compiler][codegen] Serialization does not work for method references
	public void test428552() throws Exception {
		this.runConformTest(
				new String[]{
					"X.java",
					"""
						import java.io.*;
						public class X {
							interface Example extends Serializable {
								String convert(X o);
							}
							public static void main(String[] args) throws IOException {
								Example e=X::toString;
						       util.Helper.write(e);
						       e = (Example)util.Helper.read();
						       System.out.println(e.convert(new X()));
							}
						   public String toString() {
						       return "XItIs";
						   }
						}
						""",
					"Helper.java",HELPER_CLASS,
					},
					"XItIs",
					null,
					true,
					new String [] { "-Ddummy" }); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428642
	public void test428642() throws Exception {
		this.runConformTest(
				new String[]{
					"QuickSerializedLambdaTest.java",
					"""
						import java.io.*;
						import java.util.function.IntConsumer;
						
						public class QuickSerializedLambdaTest {
							interface X extends IntConsumer,Serializable{}
							public static void main(String[] args) throws IOException, ClassNotFoundException {
								X x2 = System::exit; // method reference
								ByteArrayOutputStream debug=new ByteArrayOutputStream();
								try(ObjectOutputStream oo=new ObjectOutputStream(debug))
								{
									oo.writeObject(x2);
								}
								try(ObjectInputStream oi=new ObjectInputStream(new ByteArrayInputStream(debug.toByteArray())))
								{
									X x=(X)oi.readObject();
									x.accept(0);// shall exit
								}
								throw new AssertionError("should not reach this point");
							}
						}
						""",
					"Helper.java",
					"""
						public class Helper {
						  public static String tostring(java.lang.invoke.SerializedLambda sl) {
						    return sl.toString();
						  }
						}"""
				},
				"",
				null,true,
				new String[]{"-Ddummy"}); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
	}

	public void test428642_2() throws Exception {
		this.runConformTest(
				new String[]{
					"Helper.java",
					"""
						public class Helper {
						  public static String tostring(java.lang.invoke.SerializedLambda sl) {
						    return sl.toString();
						  }
						  public static void main(String[]argv) throws Exception {
						    foo.QuickSerializedLambdaTest.main(argv);
						  }
						}""",
					"QuickSerializedLambdaTest.java",
					"""
						package foo;
						import java.io.*;
						import java.util.function.IntConsumer;
						
						public class QuickSerializedLambdaTest {
							interface X extends IntConsumer,Serializable{}
							public static void main(String[] args) throws IOException, ClassNotFoundException {
								X x1 = i -> System.out.println(i);// lambda expression
								X x2 = System::exit; // method reference
								ByteArrayOutputStream debug=new ByteArrayOutputStream();
								try(ObjectOutputStream oo=new ObjectOutputStream(debug))
								{
									oo.writeObject(x1);
									oo.writeObject(x2);
								}
								try(ObjectInputStream oi=new ObjectInputStream(new ByteArrayInputStream(debug.toByteArray())))
								{
									X x=(X)oi.readObject();
									x.accept(42);// shall print "42"
									x=(X)oi.readObject();
									x.accept(0);// shall exit
								}
								throw new AssertionError("should not reach this point");
							}
						}
						"""
				},
				"42",
				null,true,
				new String[]{"-Ddummy"}); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429112, [1.8][compiler] Exception when compiling Serializable array constructor reference
	public void test429112() throws Exception {
		this.runConformTest(
				new String[]{
					"X.java",
					"""
						import java.io.*;
						import java.util.function.IntFunction;
						public class X {
						  interface IF extends IntFunction<Object>, Serializable {}
						  public static void main(String[] args) throws IOException, ClassNotFoundException {
						    IF factory=String[]::new;
						    Object o = factory.apply(1234);
								ByteArrayOutputStream debug=new ByteArrayOutputStream();
								try(ObjectOutputStream oo=new ObjectOutputStream(debug))
								{
									oo.writeObject(factory);
								}
								try(ObjectInputStream oi=new ObjectInputStream(new ByteArrayInputStream(debug.toByteArray())))
								{
									IF x = (IF)oi.readObject();
									Object p = x.apply(1234);
						           System.out.println(p.getClass());
						           String [] sa = (String []) p;
						           System.out.println(sa.length);
								}
							}
						}
						""",
				},
				"class [Ljava.lang.String;\n" +
				"1234",
				null,true,
				new String[]{"-Ddummy"}); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=439889 - [1.8][compiler] [lambda] Deserializing lambda fails with IllegalArgumentException: "Invalid lambda deserialization"
	public void test439889() throws Exception {
		this.runConformTest(
				new String[]{
					"SerializationTest.java",
					"import java.io.*;\n"+
					"\n"+
					"public class SerializationTest implements Serializable {\n"+
					"	interface SerializableRunnable extends Runnable, Serializable {\n"+
					"	}\n"+
					"\n"+
					"	SerializableRunnable runnable;\n"+
					"\n"+
					"	public SerializationTest() {\n"+
					"		final SerializationTest self = this;\n"+
					"		// runnable = () -> self.doSomething();\n"+
					"		runnable = () -> this.doSomething();\n"+ // results in this method handle: #166 invokespecial SerializationTest.lambda$0:()V
					"       }\n"+
					"\n"+
					"	public void doSomething() {\n"+
					"		System.out.println(\"Hello,world!\");\n"+
					"	}\n"+
					"\n"+
					"	public static void main(String[] args) throws Exception {\n"+
					"		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();\n"+
					"		try (ObjectOutputStream out = new ObjectOutputStream(buffer) ) {\n"+
					"			out.writeObject(new SerializationTest());\n"+
					"		}\n"+
					"		try (ObjectInputStream in = new ObjectInputStream( new ByteArrayInputStream(buffer.toByteArray()))) {\n"+
					"			final SerializationTest s = (SerializationTest) in.readObject();\n"+
					"			s.doSomething();\n"+
					"		}\n"+
					"	}\n"+
					"}\n"
					},
					"Hello,world!",
					null,true,
					new String[]{"-Ddummy"}); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
	}

	public void test439889_2() throws Exception {
		this.runConformTest(
				new String[]{
					"SerializationTest.java",
					"import java.io.*;\n"+
					"\n"+
					"public class SerializationTest implements Serializable {\n"+
					"	interface SerializableRunnable extends Runnable, Serializable {\n"+
					"	}\n"+
					"\n"+
					"	SerializableRunnable runnable;\n"+
					"\n"+
					"	public SerializationTest() {\n"+
					"		final SerializationTest self = this;\n"+
					"		runnable = () -> self.doSomething();\n"+ // results in this method handle: #168 invokestatic SerializationTest.lambda$0:(LSerializationTest;)V
					"		// runnable = () -> this.doSomething();\n"+
					"       }\n"+
					"\n"+
					"	public void doSomething() {\n"+
					"		System.out.println(\"Hello,world!\");\n"+
					"	}\n"+
					"\n"+
					"	public static void main(String[] args) throws Exception {\n"+
					"		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();\n"+
					"		try (ObjectOutputStream out = new ObjectOutputStream(buffer) ) {\n"+
					"			out.writeObject(new SerializationTest());\n"+
					"		}\n"+
					"		try (ObjectInputStream in = new ObjectInputStream( new ByteArrayInputStream(buffer.toByteArray()))) {\n"+
					"			final SerializationTest s = (SerializationTest) in.readObject();\n"+
					"			s.doSomething();\n"+
					"		}\n"+
					"	}\n"+
					"}\n"
					},
					"Hello,world!",
					null,true,
					new String[]{"-Ddummy"}); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
	}

	public void testNestedLambdas_442416() throws Exception {
		this.runConformTest(
				new String[]{
					"Foo.java",
					"""
						import java.io.*;
						public class Foo {
						   static byte[] toSer(Object o) {
						       try {
									final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
									try (ObjectOutputStream out = new ObjectOutputStream(buffer) ) {
										out.writeObject(o);
									}
									return buffer.toByteArray();
						       } catch (Exception e) {e.printStackTrace();return null;}
						   }
						   static Object fromSer(byte[] bs) {
						       try {
									try (ObjectInputStream in = new ObjectInputStream( new ByteArrayInputStream(bs))) {
										final Object s = in.readObject();
										return s;
									}
						       } catch (Exception e) {e.printStackTrace();return null;}
						   }
							public static void main(String[] args) throws Exception {
						       Runnable nested1,nested2;
								Runnable lambda0 = (java.io.Serializable & Runnable) () -> {
									Runnable lambda1 = (java.io.Serializable & Runnable) () -> {
										Runnable lambda2 = (java.io.Serializable & Runnable) () -> {
											System.out.println("Hello,world!");
										};
						       		byte[] bs = toSer(lambda2);
										Runnable r = (Runnable)fromSer(bs);
						       		r.run();
									};
						       	byte[] bs = toSer(lambda1);
									Runnable r = (Runnable)fromSer(bs);
						       	r.run();
								};
						       byte[] bs = toSer(lambda0);
								Runnable r = (Runnable)fromSer(bs);
						       r.run();
							}
						}
						""",
				},
				"Hello,world!",
				null,true,
				new String[]{"-Ddummy"}); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
	}

	public void testBindingThis_442418() throws Exception {
		this.runConformTest(
				new String[]{
					"Foo.java",
					"""
						import java.io.*;
						public class Foo implements Serializable {
						   static byte[] toSer(Object o) {
						       try {
									final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
									try (ObjectOutputStream out = new ObjectOutputStream(buffer) ) {
										out.writeObject(o);
									}
									return buffer.toByteArray();
								} catch (Exception e) {e.printStackTrace();return null;}
							}
							static Object fromSer(byte[] bs) {
							try {
									try (ObjectInputStream in = new ObjectInputStream( new ByteArrayInputStream(bs))) {
										final Object s = in.readObject();
										return s;
									}
						       } catch (Exception e) {e.printStackTrace();return null;}
						   }
								void m(int i) {
									System.out.println(i);
								}
								void n(int i) {
									Runnable lambda = (java.io.Serializable & Runnable) () -> { this.m(i); };
									byte[] bs = toSer(lambda);
									Runnable r = (Runnable)fromSer(bs);
									r.run();
								}
							public static void main(String[] args) throws Exception {
								new Foo().n(42);
							}
						}
						""",
				},
				"42",
				null,true,
				new String[]{"-Ddummy"}); // Not sure, unless we force the VM to not be reused by passing dummy vm argument, the generated program aborts midway through its execution.
	}

	public void testbug479119() {
		this.runConformTest(
			new String[]{
				"Testbed.java",
				"""
					import java.io.ObjectStreamClass;
					import java.io.Serializable;
					import java.lang.invoke.SerializedLambda;
					import java.lang.reflect.Method;
					import java.util.function.IntFunction;
					import java.util.stream.Stream;
					public class Testbed {
						public static void main(String[] args) {
							System.out.println(getMethod(Testbed::foo).equals(getMethod(Testbed::foo)));
						}
						private static void foo() { }
						static interface MethodRef extends Runnable, Serializable { }
						private static Method getMethod(MethodRef methodRef) {
							try {
								final Method invokeWriteReplaceMethod = ObjectStreamClass.class.getDeclaredMethod("invokeWriteReplace", Object.class);
								invokeWriteReplaceMethod.setAccessible(true);
								final SerializedLambda l = (SerializedLambda)invokeWriteReplaceMethod.invoke(
										ObjectStreamClass.lookupAny(methodRef.getClass()),
										methodRef
									);
								System.out.println("Looking for " + l.getImplClass() + "." + l.getImplMethodName());
								final Method[] methods = Stream.of(Class.forName(l.getImplClass()).getDeclaredMethods()).
									filter(m -> m.getName().equals(l.getImplMethodName())).
									toArray(Method[]::new);
								if(methods.length != 1) throw new AssertionError("TODO: check signature");
								return methods[0];
							} catch(Exception e) {
								throw new RuntimeException(e);
							}
						}
					}
					"""
		},
		"""
			Looking for Testbed.foo
			Looking for Testbed.foo
			true""",
		null,true,
		(isJRE9Plus
		? new String[] { "--add-opens", "java.base/java.io=ALL-UNNAMED" }
		: new String [] { "-Ddummy" })
		);


		String bootstrapEntries = printBootstrapMethodsAttribute(OUTPUT_DIR + File.separator + "Testbed.class");
		String expectedOutput =
				"""
			0: invokestatic java/lang/invoke/LambdaMetafactory.altMetafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;
			  Method arguments:
			    ()V
			    invokestatic Testbed.foo:()V
			    ()V
			    1
			1: invokestatic java/lang/invoke/LambdaMetafactory.metafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;
			  Method arguments:
			    (Ljava/lang/Object;)Z
			    invokestatic Testbed.lambda$2:(Ljava/lang/invoke/SerializedLambda;Ljava/lang/reflect/Method;)Z
			    (Ljava/lang/reflect/Method;)Z
			2: invokestatic java/lang/invoke/LambdaMetafactory.metafactory:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;
			  Method arguments:
			    (I)Ljava/lang/Object;
			    invokestatic Testbed.lambda$3:(I)[Ljava/lang/reflect/Method;
			    (I)[Ljava/lang/reflect/Method;
			""";

		checkExpected(expectedOutput, bootstrapEntries);
	}

	public void testbug479119a() {
		this.runConformTest(
			new String[]{
				"Testbed.java",
				"""
					import java.io.ObjectStreamClass;
					import java.io.Serializable;
					import java.lang.invoke.SerializedLambda;
					import java.lang.reflect.Constructor;
					import java.lang.reflect.Executable;
					import java.lang.reflect.Method;
					import java.util.function.IntFunction;
					import java.util.stream.Stream;
					public class Testbed {
						public static void main(String[] args) {
							System.out.println(getMethod(Testbed::foo).equals(getMethod(Testbed::foo)));
							System.out.println(getMethod(new Foo()::method).equals(getMethod(new Bar()::method)));
							System.out.println(getMethod(MethodRefImpl::new).equals(getMethod(MethodRefImpl::new)));
						}
						static class MethodRefImpl implements MethodRef {
							@Override
							public void run() {}
						}
						public static class Base {
					        public void method () {}
					    }
					    public static class Foo extends Base {}
					    public static class Bar extends Base {}
						private static void foo() { }
						static interface MethodRef extends Runnable, Serializable { }
						private static Executable getMethod(MethodRef methodRef) {
							try {
								final Method invokeWriteReplaceMethod = ObjectStreamClass.class.getDeclaredMethod("invokeWriteReplace", Object.class);
								invokeWriteReplaceMethod.setAccessible(true);
								final SerializedLambda l = (SerializedLambda)invokeWriteReplaceMethod.invoke(
										ObjectStreamClass.lookupAny(methodRef.getClass()),
										methodRef
									);
								System.out.println("Looking for " + l.getImplClass() + "." + l.getImplMethodName());
								boolean isConstructor = l.getImplMethodName().indexOf("<init>") >= 0;
								final Executable[] methods = Stream.of(isConstructor ? Class.forName(l.getImplClass()).getDeclaredConstructors() : Class.forName(l.getImplClass()).getDeclaredMethods()).
									filter(m -> m.getName().equals(isConstructor ? l.getImplClass() : l.getImplMethodName())).
									toArray(isConstructor ? Constructor[]::new : Method[]::new);
								if(methods.length != 1) throw new AssertionError("TODO: check signature");
								return methods[0];
							} catch(Exception e) {
								throw new RuntimeException(e);
							}
						}
					}
					"""
		},
		"""
			Looking for Testbed.foo
			Looking for Testbed.foo
			true
			Looking for Testbed$Base.method
			Looking for Testbed$Base.method
			true
			Looking for Testbed$MethodRefImpl.<init>
			Looking for Testbed$MethodRefImpl.<init>
			true""",
		null,true,
		(isJRE9Plus
		? new String[] { "--add-opens", "java.base/java.io=ALL-UNNAMED" }
		: new String [] { "-Ddummy" })
		);
	}

	// Serializable reference expressions that share the same name
	public void testbug479119b() {
		this.runConformTest(
			new String[]{
				"X.java",
				"""
					import java.io.ByteArrayInputStream;
					import java.io.ByteArrayOutputStream;
					import java.io.IOException;
					import java.io.ObjectInputStream;
					import java.io.ObjectOutputStream;
					import java.io.Serializable;
					public class X {
					    public static interface Consumer<T> extends Serializable {
					        void accept(T t);
					    }
					    public static class Foo {
					    	public void method () {
					        	System.out.println("Foo");
					        }
					    }
					    public static class Bar {
					    	public void method () {
					        	System.out.println("Bar");
					        }
					    }
					    public static void main (String[] args) throws IOException, ClassNotFoundException {
					        Consumer<Foo> foo = Foo::method;
					        Consumer<Bar> bar = Bar::method;
					        Consumer<Foo> baz = (b) -> {b.method();};
					        ByteArrayOutputStream debug=new ByteArrayOutputStream();
							try(ObjectOutputStream oo=new ObjectOutputStream(debug)) {
								oo.writeObject(bar);
							}
							try(ObjectInputStream oi=new ObjectInputStream(new ByteArrayInputStream(debug.toByteArray()))) {
								Consumer<Bar> x = (Consumer)oi.readObject();
								x.accept(new Bar());
							}
							debug.reset();
							try(ObjectOutputStream oo=new ObjectOutputStream(debug)) {
								oo.writeObject(foo);
							}
							try(ObjectInputStream oi=new ObjectInputStream(new ByteArrayInputStream(debug.toByteArray()))) {
								Consumer<Foo> x = (Consumer)oi.readObject();
								x.accept(new Foo());
							}
					    }
					}
					"""
		},
		"Bar\n" +
		"Foo",
		null,true,
		new String[]{"-Ddummy"});
	}
	public void testbug479119_comment20() {
		this.runConformTest(
			new String[]{
				"Testbed.java",
				"""
					import java.io.ByteArrayInputStream;
					import java.io.ByteArrayOutputStream;
					import java.io.IOException;
					import java.io.ObjectInputStream;
					import java.io.ObjectOutputStream;
					import java.io.Serializable;
					interface FI extends Serializable{
						void run(Testbed args);
					}
					interface IF extends Serializable{
						void run();
					}
					public class Testbed implements Serializable{
						String f;
						Testbed(String str) {
							f = str;
						}
						void test() throws IOException, ClassNotFoundException {
							accept(Testbed::foo);
							accept(this::foo);	\t
						}
						void foo() {
							System.out.println(this.f);
						}
						void accept(FI fi) {
							fi.run(this);
						}
						void accept(IF i) {
							i.run();
						}
						public static void main(String[] args) throws ClassNotFoundException, IOException {
							Testbed t = new Testbed("IF");
							Testbed t2 = new Testbed("FI");
							IF i = t::foo;
							FI f = Testbed::foo;
							ByteArrayOutputStream debug=new ByteArrayOutputStream();
							try(ObjectOutputStream oo=new ObjectOutputStream(debug))
							{
								oo.writeObject(i);
							}
							try(ObjectInputStream oi=new ObjectInputStream(new ByteArrayInputStream(debug.toByteArray())))
							{
								IF x = (IF)oi.readObject();
								t.accept(x);
							}
							debug=new ByteArrayOutputStream();
							try(ObjectOutputStream oo=new ObjectOutputStream(debug))
							{
								oo.writeObject(f);
							}
							try(ObjectInputStream oi=new ObjectInputStream(new ByteArrayInputStream(debug.toByteArray())))
							{
								FI x = (FI)oi.readObject();
								t2.accept(x);
							}
						}
					}"""
		},
		"IF\n" +
		"FI",
		null,true,
		new String[]{"-Ddummy"});
	}
	public void testbug485333() {
		this.runConformTest(
			new String[]{
				"Test.java",
				"""
					import java.io.ByteArrayInputStream;
					import java.io.ByteArrayOutputStream;
					import java.io.ObjectInputStream;
					import java.io.ObjectOutputStream;
					import java.io.Serializable;
					interface Func<T> extends Serializable {
					  T get();
					}
					class Impl implements Serializable {
					  int val = 0;
					  public int next() {
					    val += 1;
					    return val;
					  }
					}
					public class Test {
					  final Impl impl = new Impl();
					  final Func<Integer> func = (Func<Integer> & Cloneable)impl::next;
					  public void test() throws Throwable {
					    byte[] bytes = write(func);//25
					    Func<Integer> func = read(bytes);
					    System.out.println(func.get());
					  }
					  public static void main(String[] args) throws Throwable {
						new Test().test();
					}
					  @SuppressWarnings("unchecked")
					  private static Func<Integer> read(byte[] bytes) throws Exception {
					    ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
					    try (ObjectInputStream ois = new ObjectInputStream(bis)) {
					      return (Func<Integer>) ois.readObject();
					    }
					  }
					  private static byte[] write(Func<Integer> func) throws Exception {
					    ByteArrayOutputStream bos = new ByteArrayOutputStream();
					    System.out.println(func.get());
					    try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
					      oos.writeObject(func);//42
					    }
					    return bos.toByteArray();
					  }
					}"""
		},
		"1\n" +
		"2",
		null,true,
		new String[]{"-Ddummy"});
	}
	public void testbug494487() {
		Map options = getCompilerOptions();
		options.put(CompilerOptions.OPTION_LocalVariableAttribute, CompilerOptions.DO_NOT_GENERATE);
		this.runConformTest(
			new String[]{
				"Test.java",
				"""
					import java.io.IOException;
					import java.io.Serializable;
					public class Test {
					  class AnException extends Exception {
					  }
					  class Asd {
					    public Asd(String asd) { data = asd; }
					    private final String data;
					    @Override
					    public String toString() {
					      return data;
					    }
					  }
					  public interface Test1 extends Serializable {
					    void test() throws IOException;
					  }
					  public interface Test2 {
					    void test() throws AnException;
					  }
					  public void test1( Test1 test ) {
					    try {
					      test.test();
					    } catch( IOException e ) {
					      e.printStackTrace();
					    }
					  }
					  public void test2( Test2 test ) {
					    try {
					      test.test();
					    } catch( AnException e ) {
					      System.out.println( e );
					    }
					  }
					  public void lambdas() {
					    test1( () -> System.out.println( "test a" ) );
					    test1( () -> System.out.println( "test b" ) );
					    test2( () -> System.out.println( "test c" ) );
					    test2( () -> System.out.println( "test d" ) );
					  }
					  public void print( CharSequence a, String b, long c ) {
					    System.out.println( a );
					    System.out.println( b );
					    System.out.println( c );
					  }
					  public void filler() {
					    System.out.println( "Now we need to get this class file closer to 3000 bytes boundary" );
					    filler1();
					    filler2();
					    filler3();
					    filler4();
					    filler5();
					    filler6();
					    filler7();
					    filler8();
					    filler9();
					    filler10();
					    filler11();
					    filler12();
					    filler13();
					    filler14();
					    filler15();
					    filler16();
					    filler17();
					    filler18();
					    filler19();
					    filler20();
					    filler21();
					    filler22();
					    filler23();
					    filler24();
					    filler25();
					    filler26();
					    filler27();
					    filler28();
					    print( "a", "b", System.currentTimeMillis() );
					    print( "a", "b", System.currentTimeMillis() );
					    print( "a", "b", System.currentTimeMillis() );
					    print( "a", "b", System.currentTimeMillis() );
					    print( "a", "b", System.currentTimeMillis() );
					    print( "a", "b", System.currentTimeMillis() );
					  }
					  private void filler28() {
					    print( "a", "b", System.currentTimeMillis() );
					  }
					  private void filler27() {
					    print( "a", "b", System.currentTimeMillis() );
					  }
					  private void filler26() {
					    print( "a", "b", System.currentTimeMillis() );
					  }
					  private void filler25() {
					    print( "a", "b", System.currentTimeMillis() );
					  }
					  private void filler24() {
					    print( "a", "b", System.currentTimeMillis() );
					  }
					  private void filler23() {
					    print( "a", "b", System.currentTimeMillis() );
					  }
					  private void filler22() {
					    print( "a", "b", System.currentTimeMillis() );
					  }
					  private void filler21() {
					    print( "a", "b", System.currentTimeMillis() );
					  }
					  private void filler20() {
					    print( "a", "b", System.currentTimeMillis() );
					  }
					  private void filler19() {
					    print( "a", "b", System.currentTimeMillis() );
					  }
					  private void filler18() {
					    print( "a", "b", System.currentTimeMillis() );
					  }
					  private void filler17() {
					    print( "a", "b", System.currentTimeMillis() );
					  }
					  private void filler16() {
					    print( "a", "b", System.currentTimeMillis() );
					  }
					  private void filler15() {
					    print( "a", "b", System.currentTimeMillis() );
					  }
					  private void filler14() {
					    print( "a", "b", System.currentTimeMillis() );
					  }
					  private void filler13() {
					    print( "a", "b", System.currentTimeMillis() );
					  }
					  private void filler12() {
					    print( "a", "b", System.currentTimeMillis() );
					  }
					  private void filler11() {
					    print( "a", "b", System.currentTimeMillis() );
					  }
					  private void filler10() {
					    print( "a", "b", System.currentTimeMillis() );
					  }
					  private void filler9() {
					    print( "a", "b", System.currentTimeMillis() );
					  }
					  private void filler8() {
					    print( "a", "b", System.currentTimeMillis() );
					  }
					  private void filler7() {
					    print( "a", "b", System.currentTimeMillis() );
					  }
					  private void filler6() {
					    print( "a", "b", System.currentTimeMillis() );
					  }
					  private void filler5() {
					    print( c.toString(), d.toString(), System.currentTimeMillis() );
					  }
					  private void filler4() {
					    print( a.toString(), b.toString(), System.currentTimeMillis() );
					  }
					  private void filler3() {
					    print( "a", System.getenv( "asd" ), System.currentTimeMillis() );
					  }
					  private void filler2() {
					    print( "a", System.lineSeparator(), System.currentTimeMillis() );
					  }
					  private void filler1() {
					    print( "a", "b", System.currentTimeMillis() );
					  }
					  private final Asd a = new Asd("a");
					  private final Asd b = new Asd("b");
					  private final Asd c = new Asd("c");
					  private final Asd d = new Asd("d");
					}
					"""
		},
		options);
	}
	public void testbug497879() {
		this.runConformTest(
			new String[]{
				"LambdaSerializationTest.java",
				"""
					import java.io.ByteArrayInputStream;
					import java.io.ByteArrayOutputStream;
					import java.io.IOException;
					import java.io.ObjectInputStream;
					import java.io.ObjectOutputStream;
					import java.io.Serializable;
					import java.util.ArrayList;
					import java.util.List;
					import java.util.function.Supplier;
					public class LambdaSerializationTest {
					    interface SerializableSupplier<T> extends Supplier<T>, Serializable {}
					    public static void constructorReferenceSerialization() throws IOException, ClassNotFoundException {
					        SerializableSupplier<List<?>> function = ArrayList::new; //Collections::emptyList;
					        Object result = serializeDeserialize(function);
					        Class<?>[] infs = result.getClass().getInterfaces();
					        for(int i = 0; i < infs.length; i++) {
					            System.out.println(infs[i]);
					        }
					    }
					    private static Object serializeDeserialize(Object obj) throws IOException, ClassNotFoundException {
					        try (
					            ByteArrayOutputStream buffer = new ByteArrayOutputStream(); //
					            ObjectOutputStream output = new ObjectOutputStream(buffer)) {
					            output.writeObject(obj);
					            try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()))) {
					                return input.readObject();
					            }
					        }
					    }
					    public static void main(String[] args) {
							try {
								LambdaSerializationTest.constructorReferenceSerialization();
							} catch (ClassNotFoundException | IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}"""
		},
		"interface LambdaSerializationTest$SerializableSupplier",
		null,true,
		new String[]{"-Ddummy"});
	}
	public void testbug497879a() {
		this.runConformTest(
			new String[]{
				"LambdaSerializationTest.java",
				"""
					import java.io.ByteArrayInputStream;
					import java.io.ByteArrayOutputStream;
					import java.io.IOException;
					import java.io.ObjectInputStream;
					import java.io.ObjectOutputStream;
					import java.io.Serializable;
					import java.util.ArrayList;
					import java.util.List;
					import java.util.function.Supplier;
					public class LambdaSerializationTest {
					    interface SerializableSupplier<T> extends Supplier<T>, Serializable {}
					    static class Junk {
					    	private Junk() {}
					    }
					    public static void constructorReferenceSerialization() throws IOException, ClassNotFoundException {
					        SerializableSupplier<Junk> function = Junk::new;
					        Object result = serializeDeserialize(function);
					        Class<?>[] infs = result.getClass().getInterfaces();
					        for(int i = 0; i < infs.length; i++) {
					            System.out.println(infs[i]);
					        }
					    }
					    private static Object serializeDeserialize(Object obj) throws IOException, ClassNotFoundException {
					        try (
					            ByteArrayOutputStream buffer = new ByteArrayOutputStream(); //
					            ObjectOutputStream output = new ObjectOutputStream(buffer)) {
					            output.writeObject(obj);
					            try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()))) {
					                return input.readObject();
					            }
					        }
					    }
					    public static void main(String[] args) {
							try {
								LambdaSerializationTest.constructorReferenceSerialization();
							} catch (ClassNotFoundException | IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}"""
		},
		"interface LambdaSerializationTest$SerializableSupplier",
		null,true,
		new String[]{"-Ddummy"});
	}
	public void testbug497879b() {
		this.runConformTest(
			new String[]{
				"LambdaSerializationTest.java",
				"""
					import java.io.ByteArrayInputStream;
					import java.io.ByteArrayOutputStream;
					import java.io.IOException;
					import java.io.ObjectInputStream;
					import java.io.ObjectOutputStream;
					import java.io.Serializable;
					import java.util.ArrayList;
					import java.util.List;
					import java.util.function.Supplier;
					public class LambdaSerializationTest {
					    interface SerializableSupplier<T> extends Serializable {
					        T get(int count);
					    }
					    public static void constructorReferenceSerialization() throws IOException, ClassNotFoundException {
					        SerializableSupplier<List[]> function = ArrayList[]::new;
					        Object result = serializeDeserialize(function);
					        Class<?>[] infs = result.getClass().getInterfaces();
					        for(int i = 0; i < infs.length; i++) {
					            System.out.println(infs[i]);
					        }
					    }
					    private static Object serializeDeserialize(Object obj) throws IOException, ClassNotFoundException {
					        try (
					            ByteArrayOutputStream buffer = new ByteArrayOutputStream(); //
					            ObjectOutputStream output = new ObjectOutputStream(buffer)) {
					            output.writeObject(obj);
					            try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()))) {
					                return input.readObject();
					            }
					        }
					    }
					    public static void main(String[] args) {
							try {
								LambdaSerializationTest.constructorReferenceSerialization();
							} catch (ClassNotFoundException | IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}"""
		},
		"interface LambdaSerializationTest$SerializableSupplier",
		null,true,
		new String[]{"-Ddummy"});
	}
	public void testbug503118() {
		this.runConformTest(
			new String[]{
				"lambdabug/App.java",
				"""
					package lambdabug;
					import java.io.ByteArrayInputStream;
					import java.io.ByteArrayOutputStream;
					import java.io.ObjectInputStream;
					import java.io.ObjectOutputStream;
					import java.io.Serializable;
					import java.util.function.Function;
					public class App {
						public static interface SerialFunction<T, R> extends Function<T, R>, Serializable {
						}
						public static interface TestInterface extends Serializable {
							public Integer method(Integer i);
						}
						public static class TestClass implements TestInterface {
							private static final long serialVersionUID = 1L;
							@Override
							public Integer method(Integer i) {
								return i;
							}
						}
						public static void main(String[] args) throws Exception {
							TestInterface testService = getService();
							SerialFunction<Integer, Integer> sf = testService::method;
							ByteArrayOutputStream bos = new ByteArrayOutputStream();
							new ObjectOutputStream(bos).writeObject(sf);
							Object o = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
							System.out.println(o.getClass().getInterfaces()[0]);
						}
						private static TestInterface getService() {
							return new TestClass();
						}
					}
					"""
		},
		"interface lambdabug.App$SerialFunction",
		null,true,
		new String[]{"-Ddummy"});
	}
	public void testbug507011() {
		this.runConformTest(
			new String[]{
				"VerifyErrorDerived.java",
				"""
					import java.io.Serializable;
					import java.util.function.Function;
					public class VerifyErrorDerived extends VerifyErrorBase {
						public static void main(String [] args) {
							System.out.println("hello world");
						}
						public int derivedMethod(String param) {
							SerializableFunction<String, Integer> f = super::baseMethod;
							return f.apply(param);
						}
					}
					interface SerializableFunction<T, R> extends Function<T, R>, Serializable {}""",
				"VerifyErrorBase.java",
				"""
					public class VerifyErrorBase {
						public int baseMethod(String param) {
							return 7;
						}
					}
					"""
		},
		"hello world",
		null,true,
		new String[]{"-Ddummy"});
	}
	public void testbug509782() {
		this.runConformTest(
			new String[]{
				"compilertest/BaseType.java",
				"""
					package compilertest;
					import java.io.ByteArrayInputStream;
					import java.io.ByteArrayOutputStream;
					import java.io.ObjectInputStream;
					import java.io.ObjectOutputStream;
					import java.io.Serializable;
					import compilertest.sub.SubType;
					public class BaseType implements Serializable {
					    protected void doSomething() {
					    }
					    public static void main(String[] args) throws Exception {
					        SubType instance = new SubType();
					        ByteArrayOutputStream bs = new ByteArrayOutputStream();
					        ObjectOutputStream out = new ObjectOutputStream(bs);
					        out.writeObject(instance);
					        byte[] data = bs.toByteArray();
					        ObjectInputStream in = new ObjectInputStream(
					                new ByteArrayInputStream(data));
					        in.readObject();
					        System.out.println("Done");
					    }
					}""",
				"compilertest/sub/SubType.java",
				"""
					package compilertest.sub;
					import java.io.Serializable;
					import compilertest.BaseType;
					public class SubType extends BaseType {
					    Runnable task = (Runnable & Serializable) this::doSomething;
					}
					"""
		},
		"Done",
		null,true,
		new String[]{"-Ddummy"});
	}
	public void testbug566155() {
		// method reference must be compiled as an implicit lambda expression
		// else it cannot be serialized correctly
		this.runConformTest(new String[] {
				"OuterClass.java",
				"""
					import java.io.*;
					import java.util.function.Supplier;
					
					public class OuterClass implements Serializable {
					
					    private static final long serialVersionUID = 5390565572939096897L;
					    public Supplier<OuterClass.InnerClass> supplier;
					
					    @SuppressWarnings("unchecked")
					    public OuterClass() {
					        this.supplier = (Supplier<OuterClass.InnerClass> & Serializable) InnerClass::new;
					    }
					
					    public class InnerClass implements Serializable {
					        private static final long serialVersionUID = 2478179807896338433L;
					 	   public InnerClass() {
					        }
					    }
					
					}
					"""
		}, "");
		String expectedOutput =
			"lambda$1()LOuterClass$InnerClass;\n";
		String data = printLambdaMethods(OUTPUT_DIR + File.separator + "OuterClass.class");
		checkExpected(expectedOutput,data);
	}
	public void testbugGH155() {
		// before resolution, $deserializeLambda$ expects java.lang.Object return type
		// while SerializedLambda advertises java.lang.Comparable return type
		// deserialization fails as $deserializeLambda$ cannot find appropriate deserializer.
		this.runConformTest(new String[] {
				"TestSerializableLambda.java",
				"""
					import java.io.*;
					import java.util.function.Function;
					
					public class TestSerializableLambda {
						private static Object serializeDeserialize(Object obj) throws IOException, ClassNotFoundException {
							try (
								ByteArrayOutputStream buffer = new ByteArrayOutputStream(); //
								ObjectOutputStream output = new ObjectOutputStream(buffer)) {
								output.writeObject(obj);
								try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()))) {
									return input.readObject();
								}
							}
						}
						public static void main(String[] args) {
							try {
								SerializableHolder<LambdaProvider<Long>> r = new SerializableHolder<>();
								serializeDeserialize(r);
								System.out.println("OK");
							} catch (ClassNotFoundException | IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					\t
						public static class SerializableHolder<E extends LambdaProvider<?>> implements Serializable {
							private static final long serialVersionUID = -2775595600924717218L;
							private Function<E, ?> idExpression;
					
							public SerializableHolder() {
								this.idExpression = (Serializable & Function<E, ?>) LambdaProvider::getId;
							}
						}
					
						public static class LambdaProvider<I extends Comparable<I>> {
							public I getId() {
								return null;
							}
						}
					}"""
				},
				"OK");
	}
	// ---

	private void checkExpected(String expected, String actual) {
		if (!expected.equals(actual)) {
			printIt(actual);
		}
		assertEquals(expected,actual);
	}

	/**
	 * Print a piece of text with the necessary extra quotes and newlines so that it can be cut/pasted into
	 * the test source file.
	 */
	private void printIt(String text) {
		String quotedText = text;
		if (!quotedText.startsWith("\"")) {
			quotedText = "\""+quotedText.replaceAll("\n", "\\\\n\"+\n\"");
			quotedText = quotedText.substring(0,quotedText.length()-3);
		}
		System.out.println(quotedText);
	}

	/**
	 * Print the bootstrap methods attribute in a very similar fashion to javap for checking.
	 * Unlike javap the constant pool indexes are not included, to make the test a little less
	 * fragile.
	 */
	private String printBootstrapMethodsAttribute(String filepath) {
		IClassFileReader cfr = ToolFactory.createDefaultClassFileReader(filepath, IClassFileReader.CLASSFILE_ATTRIBUTES);
		BootstrapMethodsAttribute bootstrapMethodsAttribute = null;
		IClassFileAttribute[] attrs = cfr.getAttributes();
		for (int i=0,max=attrs.length;i<max;i++) {
			if (new String(attrs[i].getAttributeName()).equals("BootstrapMethods")) {
				bootstrapMethodsAttribute = (BootstrapMethodsAttribute)attrs[i];
			}
		}
		if (bootstrapMethodsAttribute==null) {
			return "";
		}
		IConstantPool cp = cfr.getConstantPool();
		StringBuilder sb = new StringBuilder();
		int bmaLength = bootstrapMethodsAttribute.getBootstrapMethodsLength();
		for (int i=0;i<bmaLength;i++) {
			IBootstrapMethodsEntry entry = bootstrapMethodsAttribute.getBootstrapMethods()[i];
			int mr = entry.getBootstrapMethodReference();
			IConstantPoolEntry2 icpe = (IConstantPoolEntry2)cfr.getConstantPool().decodeEntry(mr);

			sb.append(i).append(": ").append(formatReferenceKind(icpe.getReferenceKind()));
			sb.append(" ").append(format(cp,icpe.getReferenceIndex()));
			sb.append("\n");
			int[] args = entry.getBootstrapArguments();
			sb.append("  Method arguments:\n");
			for (int a=0;a<args.length;a++) {
				sb.append("    ").append(format(cp,args[a])).append("\n");
			}
		}
		return sb.toString();
	}

	private String printLambdaMethods(String filepath) {
		IClassFileReader cfr = ToolFactory.createDefaultClassFileReader(filepath, IClassFileReader.METHOD_INFOS);
		IMethodInfo[] methodInfos = cfr.getMethodInfos();
		StringBuilder buf = new StringBuilder();
		for (int i = 0, max = methodInfos.length; i < max; i++) {
			IMethodInfo methodInfo = methodInfos[i];
			if (!new String(methodInfo.getName()).startsWith("lambda"))
				continue;
			int accessFlags = methodInfo.getAccessFlags();
			if (Modifier.isStatic(accessFlags)) {
				buf.append("static ");
			}
			buf.append(methodInfo.getName());
			buf.append(methodInfo.getDescriptor());
			buf.append("\n");
		}
		return buf.toString();
	}

	String formatReferenceKind(int kind) {
		switch (kind) {
			case IConstantPoolConstant.METHOD_TYPE_REF_InvokeStatic:
				return "invokestatic";
			default:
				throw new IllegalStateException("nyi for "+kind);
		}
	}

	String format(IConstantPool cp, int entryNumber) {
		IConstantPoolEntry entry = cp.decodeEntry(entryNumber);
		if (entry == null) {
			return "null";
		}
		switch (entry.getKind()) {
			case IConstantPoolConstant.CONSTANT_Integer:
				return Integer.toString(entry.getIntegerValue());
			case IConstantPoolConstant.CONSTANT_Utf8:
				return new String(entry.getUtf8Value());
			case IConstantPoolConstant.CONSTANT_Methodref:
				return new String(entry.getClassName())+"."+new String(entry.getMethodName())+":"+new String(entry.getMethodDescriptor());
			case IConstantPoolConstant.CONSTANT_MethodHandle:
				IConstantPoolEntry2 entry2 = (IConstantPoolEntry2)entry;
				return formatReferenceKind(entry2.getReferenceKind())+" "+format(cp,entry2.getReferenceIndex());
			case IConstantPoolConstant.CONSTANT_MethodType:
				return format(cp,((IConstantPoolEntry2)entry).getDescriptorIndex());
			case IConstantPoolConstant.CONSTANT_Class:
				return new String(entry.getClassInfoName());
			default:
					throw new IllegalStateException("nyi for "+entry.getKind());
		}
	}

}

