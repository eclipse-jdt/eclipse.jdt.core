/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
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
/*
 * Here we focus on various aspects of the runtime behavior of the generated
 * code.
 */
package org.eclipse.jdt.core.tests.compiler.regression;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

import junit.framework.Test;

@SuppressWarnings({ "rawtypes" })
public class RuntimeTests extends AbstractRegressionTest {

public RuntimeTests(String name) {
	super(name);
}

// Static initializer to specify tests subset using TESTS_* static variables
// All specified tests which does not belong to the class are skipped...
// Only the highest compliance level is run; add the VM argument
// -Dcompliance=1.4 (for example) to lower it if needed
static {
//		TESTS_NAMES = new String[] { "test0001" };
//	 	TESTS_NUMBERS = new int[] { 1 };
//		TESTS_RANGE = new int[] { 1, -1 };
}

public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}

public static Class testClass() {
	return RuntimeTests.class;
}

// decided not to keep this active because of negative effects on the test
// series (the OOME potentially causing grief to others)
// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=217078
// memory exhaustion - try to allocate too big an instance
public void _test0001_memory_exhaustion() {
	runTest(
		new String[] { /* testFiles */
			"X.java",
			"""
				public class X {
				  public static void main(String args[]) {
				    try {\
				      Y y = new Y(Integer.MAX_VALUE);
				    }\
				    catch (OutOfMemoryError e) {
				      System.out.println("SUCCESS");
				      return;
				    }
				    System.out.println("FAILURE");
				  }
				}
				class Y {
				  long storage[];
				  Y(int itemsNb) {
				    storage = new long[itemsNb];
				  }
				}
				"""},
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"SUCCESS" /* expectedOutputString */,
		null /* expectedErrorString - skip this because some JREs emit additional info to stderr in case of exception */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */,
		null /* vmArguments */,
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}

// synchronization - concurrent access to a resource with explicit and
// implicit locks
public void test0500_synchronization() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				static public void main (String args[]) {
				  new Lock().implicitLock();
				}
				}
				class Lock extends Thread {
				  byte step = 0;
				  void logStep(String start) {
				    System.out.println(start + " " + this.step); //$NON-NLS-1$
				  }
				  public void run() {
				    for (int i = 1; i < 3; i++) {
				      logStep("explicit lock"); //$NON-NLS-1$
				      synchronized (this) {
				        this.step++;
				        notify();
				        while(this.step < 2 * i) {
				          try {
				            wait();
				          } catch (InterruptedException e) {
				            System.out.println("EXCEPTION"); //$NON-NLS-1$
				          }
				        }
				      }
				    }
				  }
				  synchronized void implicitLock() {
				      this.start();
				      for (int i = 0; i < 2; i++) {
				        while (this.step < 1 + i * 2) {
				          try {
				            wait();
				          } catch (InterruptedException e) {
				            System.out.println("EXCEPTION"); //$NON-NLS-1$
				          }
				        }
				        logStep("implicit lock"); //$NON-NLS-1$
				        this.step++;
				        notify();
				      }
				      return;
				  }
				}
				"""},
		"""
			explicit lock 0
			implicit lock 1
			explicit lock 2
			implicit lock 3"""
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=126712
// reflection - access to a public method of a package visible
// class through a public extending class
public void test0600_reflection() {
	runConformTest(
		true,
		new String[] {
			"X.java",
			"""
				import java.lang.reflect.*;
				import p.*;
				public class X {
				static public void main (String args[]) {
				  Y y = new Y();
				  try {
				    Method foo = Y.class.getMethod("foo", (Class []) null);
				    y.foo();
				    foo.invoke(y, (Object []) null);
				  } catch (NoSuchMethodException e) {
				      //ignore
				  } catch (InvocationTargetException e) {
				      //ignore
				  } catch (IllegalAccessException e) {
				    System.out.print("FAILURE: IllegalAccessException");
				  }
				}
				}""",
			"p/Y.java",
			"""
				package p;
				public class Y extends Z {
				  /* empty */
				}
				""",
			"p/Z.java",
			"""
				package p;
				class Z {
				  public void foo() {
				  System.out.println("SUCCESS"); //$NON-NLS-1$
				  }
				}
				"""},
		"",
		this.complianceLevel <= ClassFileConstants.JDK1_5 ? "SUCCESS\n" + "FAILURE: IllegalAccessException" : "SUCCESS\n" + "SUCCESS",
		"",
		JavacTestOptions.EclipseJustification.EclipseBug126712
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=126712
// reflection - access to a public field of a package visible
// class through a public extending class
public void test0601_reflection() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.lang.reflect.*;
				import p.*;
				public class X {
				static public void main (String args[]) {
				  Y y = new Y();
				  try {
				    Field f = Y.class.getField("m");
				    System.out.println(y.m);
				    System.out.println(f.get(y));
				  } catch (NoSuchFieldException e) {
				      //ignore
				  } catch (IllegalAccessException e) {
				    System.out.print("FAILURE: IllegalAccessException");
				  }
				}
				}""",
			"p/Y.java",
			"""
				package p;
				public class Y extends Z {
				  /* empty */
				}
				""",
			"p/Z.java",
			"""
				package p;
				class Z {
				  public String m = "SUCCESS";
				}
				"""},
		"SUCCESS\n" +
		"FAILURE: IllegalAccessException"
	);
}

// partial rebuild - method signature changed (return type)
public void test1000_partial_rebuild() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				static public void main(String args[]) {
				  Z.go();
				}
				}
				""",
			"Z.java",
			"""
				public class Z {
				static public void go() {
				  int flag = 0;
				  try {
				    new Y().random();
				    flag = 1;
				  }
				  catch (NoSuchMethodError e) {
				    flag = 2;
				  }
				  catch (Throwable t) {
				    flag = 3;
				  }
				  System.out.println(flag);
				}
				}
				""",
			"Y.java",
			"""
				public class Y {
				java.util.Random generator = new java.util.Random();\
				public byte random() {
				  return (byte) (generator.nextInt() % Byte.MAX_VALUE);
				}
				}
				""",
			},
		"1");
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				static public void main(String args[]) {
				  Z.go();
				}
				}
				""",
			"Y.java",
			"""
				public class Y {
				java.util.Random generator = new java.util.Random();\
				public int random() {
				  return generator.nextInt();
				}
				}
				""",
			},
		"2",
		null,
		false, // do not purge output directory - pick old version of Z.class
		null);
}

// partial rebuild - method signature changed (parameter type)
public void test1001_partial_rebuild() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				static public void main(String args[]) {
				  Z.go();
				}
				}
				""",
			"Z.java",
			"""
				public class Z {
				static public void go() {
				  byte flag = 0;
				  try {
				    new Y().random(flag);
				    flag = 1;
				  }
				  catch (NoSuchMethodError e) {
				    flag = 2;
				  }
				  catch (Throwable t) {
				    flag = 3;
				  }
				  System.out.println(flag);
				}
				}
				""",
			"Y.java",
			"""
				public class Y {
				public int random(byte seed) {
				  return seed++;
				}
				}
				""",
			},
		"1");
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				static public void main(String args[]) {
				  Z.go();
				}
				}
				""",
			"Y.java",
			"""
				public class Y {
				public int random(int seed) {
				  return seed++;
				}
				}
				""",
			},
		"2",
		null,
		false, // do not purge output directory - pick old version of Z.class
		null);
}

// partial rebuild - method signature changed (visibility)
public void test1002_partial_rebuild() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				static public void main(String args[]) {
				  new Z().go();
				}
				}
				""",
			"Z.java",
			"""
				public class Z extends p.Y {
				  class ZInner extends YInner {
				    // empty
				  }
				public void go() {
				  byte flag = 0;
				  try {
				    new ZInner().foo();
				    flag = 1;
				  }
				  catch (IllegalAccessError e) {
				    flag = 2;
				  }
				  catch (Throwable t) {
				    flag = 3;
				  }
				  System.out.println(flag);
				}
				}
				""",
			"p/Y.java",
			"""
				package p;
				public class Y {
				  public class YInner {
				    public void foo() {
				      return;
				    }
				  }
				}
				""",
			},
		"1");
	this.runConformTest(
		false,
		new String[] {
			"X.java",
			"""
				public class X {
				static public void main(String args[]) {
				  new Z().go();
				}
				}
				""",
			"p/Y.java",
			"""
				package p;
				public class Y {
				  public class YInner {
				    void foo() {
				      return;
				    }
				  }
				}
				""",
			},
		"",
		"2",
		"",
		JavacTestOptions.JavacHasABug.JavacBugFixed_6_10);
}

// partial rebuild - method signature changed (visibility)
public void test1003_partial_rebuild() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				static public void main(String args[]) {
				  new Z().go();
				}
				}
				""",
			"Z.java",
			"""
				public class Z extends p.Y {
				  class ZInner extends YInner {
				    // empty
				  }
				public void go() {
				  byte flag = 0;
				  try {
				    new ZInner().foo();
				    flag = 1;
				  }
				  catch (IllegalAccessError e) {
				    flag = 2;
				  }
				  catch (Throwable t) {
				    flag = 3;
				  }
				  System.out.println(flag);
				}
				}
				""",
			"p/Y.java",
			"""
				package p;
				public class Y {
				  public class YInner {
				    public void foo() {
				      return;
				    }
				  }
				}
				""",
			},
		"1");
	this.runConformTest(
		false, // do not purge output directory - pick old version of Z.class
		new String[] {
			"X.java",
			"""
				public class X {
				static public void main(String args[]) {
				  new Z().go();
				}
				}
				""",
			"p/Y.java",
			"""
				package p;
				public class Y {
				  public class YInner {
				    protected void foo() {
				      return;
				    }
				  }
				}
				""",
			},
		"",
		"2",
		"",
		JavacTestOptions.JavacHasABug.JavacBugFixed_6_10);
}

// partial rebuild - extending class now redefines extended class fields and
//                   methods
// was Compliance_1_x#test009
public void test1004_partial_rebuild() {
	this.runConformTest(
		new String[] {
			"p1/Z.java",
			"""
				package p1;\s
				public class Z {\t
					public static void main(String[] arguments) {\s
						Y y = new Y();\t
						System.out.print(y.field);\t
						System.out.print(y.staticField);\t
						System.out.print(y.method());\t
						System.out.println(y.staticMethod());\t
					}\s
				}\s
				""",
			"p1/X.java",
			"""
				package p1;\s
				public class X {\s
					public String field = "X.field-";\t
					public static String staticField = "X.staticField-";\t
					public String method(){ return "X.method()-";	}\t
					public static String staticMethod(){ return "X.staticMethod()-";	}\t
				}\s
				""",
			"p1/Y.java",
			"""
				package p1;\s
				public class Y extends X {\s
				}\s
				"""
		},
		"X.field-X.staticField-X.method()-X.staticMethod()-");
	String expectedOutput =
		this.complianceLevel == ClassFileConstants.JDK1_3 ?
			"X.field-X.staticField-Y.method()-X.staticMethod()-" :
			"Y.field-Y.staticField-Y.method()-Y.staticMethod()-";
	this.runConformTest(
		new String[] {
			"p1/Y.java",
			"""
				package p1;\s
				public class Y extends X {\s
					public static void main(String[] arguments) {\s
						Z.main(arguments);\t
					}\t
					public String field = "Y.field-";\t
					public static String staticField = "Y.staticField-";\t
					public String method(){ return "Y.method()-";	}\t
					public static String staticMethod(){ return "Y.staticMethod()-";	}\t
				}\s
				"""
		},
		expectedOutput, // expected output
		null, // use default class-path
		false, // do not flush previous output dir content
		null); // no special vm args
}

}
