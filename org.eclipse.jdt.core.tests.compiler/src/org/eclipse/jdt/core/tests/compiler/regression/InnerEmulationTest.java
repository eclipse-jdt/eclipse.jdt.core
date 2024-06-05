/*******************************************************************************
 * Copyright (c) 2006, 2018 IBM Corporation and others.
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
 *     Stephan Herrmann - Contribution for
 *								Bug 388800 - [1.8] adjust tests to 1.8 JRE
 *     Keigo Imai - Contribution for  bug 388903 - Cannot extend inner class as an anonymous class when it extends the outer class
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.core.util.IClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class InnerEmulationTest extends AbstractRegressionTest {
static {
//		TESTS_NAMES = new String[] { "Bug58069" };
//		TESTS_NUMBERS = new int[] { 173, 174 };
//		TESTS_RANGE = new int[] { 144, -1 };
}
public InnerEmulationTest(String name) {
	super(name);
}
@Override
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportUnusedParameter, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	return options;
}
public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}
/**
 * Protected access emulation : should be performed onto implicit field and method accesses
 */
public void test001() {
	this.runConformTest(
		new String[] {
			/* p1.A */
			"p1/A.java",
			"""
				package p1;							\t
				import p2.*;							\t
				public class A {						\t
					protected int value = 0;			\t
					protected A delegatee;				\t
					public A(A del, int val) {			\t
						this.delegatee = del;			\t
						this.value = val;				\t
					}									\t
					protected void foo() {				\t
						value += 3;						\t
					}									\t
					public static void main(String[] argv){\t
						int result = new B(				\t
									  new B(null, 10), 20) \s
									 .value; 			\t
						int expected = 30; 				\t
						System.out.println( 			\t
							result == expected 			\t
								? "SUCCESS"  			\t
								: "FAILED : got "+result+" instead of "+ expected);\s
					}									\t
				}										\t
				""",
			/* p2.B */
			"p2/B.java",
			"""
				package p2;							\t
				import p1.*;							\t
				public class B extends A {				\t
					public B(B del, int val){			\t
						super(del, val);				\t
						Runnable r = new Runnable () {	\t
							public void run() {			\t
								foo(); 					\t
								if (delegatee != null) 	\t
									value += 7;			\t
							}							\t
						};								\t
						r.run();						\t
					}									\t
				}										\t
				"""
		},
		"SUCCESS"
	);
}
/**
 * 1FN4S4Z: The compiler doesn't detect a illegal constructor invocation which leads to a VerifyError
 */
public void test002() {
	this.runNegativeTest(
		new String[] {
			/* A.java */
			"A.java",
			"""
				class B {\s
				}\s
				public class A {\s
					B b;\s
					class C extends B {\s
						public C() {\s
						}\s
					}\s
					public A() {\s
						this(new C());\s
					}\s
					public A(C c) {\s
						this.b = c;\s
					}\s
					public static void main(String[] args) {\s
						A a = new A();\s
						System.out.println(a);\s
					}\s
				}\s
				"""
		},
		"""
			----------
			1. ERROR in A.java (at line 10)
				this(new C());\s
				     ^^^^^^^
			No enclosing instance of type A is available due to some intermediate constructor invocation
			----------
			"""

	);
}
/**
 * 1FZ2G7R: use of non static inner class in constuctor
 */
public void test003() {
	String errMessage = isMinimumCompliant(ClassFileConstants.JDK11) ?
			"""
				----------
				1. ERROR in A.java (at line 8)
					super(getRunnable(), new B().toString());\s
					                     ^^^^^^^
				No enclosing instance of type A is available due to some intermediate constructor invocation
				----------
				"""
			:
			"""
				----------
				1. WARNING in A.java (at line 8)
					super(getRunnable(), new B().toString());\s
					                     ^^^^^^^
				Access to enclosing constructor A.B() is emulated by a synthetic accessor method
				----------
				2. ERROR in A.java (at line 8)
					super(getRunnable(), new B().toString());\s
					                     ^^^^^^^
				No enclosing instance of type A is available due to some intermediate constructor invocation
				----------
				""";
	this.runNegativeTest(
		new String[] {
			/* A.java */
			"A.java",
			"""
				public class A extends Thread {\s
					private class B {\s
					}\s
					private static Runnable getRunnable() {\s
						return null;\s
					}\s
					public A() {\s
						super(getRunnable(), new B().toString());\s
					}\s
				}\s
				"""
		},
		errMessage);
}
/**
 * 1F995V9: Walkback in innerclass emulation when mixing source and binaries
 */
public void test004() {


	/* first compile A3.java */

	this.runConformTest(
		new String[] {
			/* A3.java */
			"A3.java",
			"""
				class A3 {\s
					class B {}\s
				}\s
				"""
		}); // no specific success output string

	/* then compile with previous input */

	this.runConformTest(
		new String[] {
			/* A4.java */
			"A4.java",
			"""
				class A4 {\s
					void foo(){\s
						new A3().new B(){};\s
					}\s
				}\s
				"""
		},
		null, // no specific success output string
		null, // use default class-path
		false,
		null); // do not flush previous output dir content

}
/**
 * 1FK9ALJ: Cannot invoke private super constructor ...
 */
public void test005() {
	this.runConformTest(
		new String[] {
			/* X.java */
			"X.java",
			"""
				public class X {\s
					private X(){}\s
					class Y extends X {\s
					}\s
					public static void main(String[] argv){\t
						new X().new Y();	 				\s
						System.out.println("SUCCESS");	\s
					}									\t
				}\s
				"""
		},
		"SUCCESS"
	);
}
/**
 * 1FKLXDL: Verification error due to incorrect private access emulation
 */
public void test006() {
	this.runConformTest(
		new String[] {
			/* X.java */
			"X.java",
			"""
				public class X {\s
					public static void main(String[] argv){\s
						new B();\s
						System.out.println("SUCCESS");\s
					}\s
					private static void foo(int i, int j) {\s
						System.out.println("private foo");\s
					}\s
					static class B {\s
						{\s
							foo(1, 2);\s
						}\s
					}\s
				}		\s
				"""
		},
		"private foo\n" +
		"SUCCESS"
	);
}
/**
 * 1PQCT5T: Missing emulation for access to sibling local types
 */
public void test007() {
	this.runConformTest(
		new String[] {
			/* Y.java */
			"Y.java",
			"""
				public class Y {\s
					public static void main(String[] argv){\s
						if (new Y().bar() == 3)\s
							System.out.println("SUCCESS");\s
						else\s
							System.out.println("FAILED");\s
					}\s
					int bar() {\s
						final int i = "xxx".length();\s
						class X {\s
							class AX {\s
								int foo() {\s
									return new BX().foo();\s
								}\s
							}\s
							class BX {\s
								int foo() {\s
									return new CX().foo();\s
								}\s
							}\s
							class CX {\s
								int foo() {\s
									return i;\s
								}\s
							}\s
						}\s
						return new X().new AX().foo();\s
					}\s
				}\s
				"""
		},
		"SUCCESS"
	);
}
/**
 * 1PQCT5T: Missing emulation for access to sibling local types
 */
public void test008() {
	this.runConformTest(
		new String[] {
			/* Y2.java */
			"Y2.java",
			"""
				public class Y2 {\s
					public static void main(String[] argv){\s
						if (new Y2().foo(45) == 45)\s
							System.out.println("SUCCESS");\s
						else\s
							System.out.println("FAILED");\s
					}\s
					int foo(final int i){\s
						class B {\s
							int foo(){\s
								return new C().foo();\s
							}\s
							class C {\s
								int foo(){ return i; }\s
							}\s
						};\s
						return new B().foo();\s
					}\s
				}\s
				"""
		},
		"SUCCESS"
	);
}
/**
 * 1PQCT5T: Missing emulation for access to sibling local types
 */
public void test009() {
	this.runConformTest(
		new String[] {
			/* Y3.java */
			"Y3.java",
			"""
				public class Y3 {\s
					public static void main(String[] argv){\s
						if (new Y3().bar() == 8)\s
							System.out.println("SUCCESS");\s
						else\s
							System.out.println("FAILED");\s
					}\s
					int bar() {\s
						final int i = "xxx".length();\s
						final String s = this.toString();\s
						class X {\s
							class AX {\s
								int foo() {\s
									return i + new CX().foo();\s
								}\s
							}\s
							class BX {\s
								int foo() {\s
									return new AX().foo();\s
								}\s
							}\s
							class CX {\s
								int foo() {\s
									return 5;\s
								}\s
							}\s
						}\s
						return new X().new AX().foo();\s
					}\s
				}\s
				"""
		},
		"SUCCESS"
	);
}
/**
 * 1PQCT5T: Missing emulation for access to sibling local types
 */
public void test010() {
	this.runConformTest(
		new String[] {
			/* Y4.java */
			"Y4.java",
			"""
				public class Y4 {\s
					public static void main(String[] argv){\s
						if (new Y4().bar() == 3)\s
							System.out.println("SUCCESS");\s
						else\s
							System.out.println("FAILED");\s
					}\s
					int bar() {\s
						final int i = "xxx".length();\s
						final String s = this.toString();\s
						class X {\s
							class AX {\s
								int bar() {\s
									class BX {\s
										int foo() {\s
											return new AX().foo();\s
										}\s
									}\s
									return new BX().foo();\s
								}\s
								int foo() {\s
									return i;\s
								}\s
							}\s
						}\s
						return new X().new AX().bar();\s
					}\s
				}\s
				"""
		},
		"SUCCESS"
	);
}
/**
 * 1PQCT5T: Missing emulation for access to sibling local types
 */
public void test011() {
	this.runConformTest(
		new String[] {
			/* Y5.java */
			"Y5.java",
			"""
				public class Y5 {\s
					public static void main(String[] argv){\s
						if (new Y5().bar(5) == 5)\s
							System.out.println("SUCCESS");\s
						else\s
							System.out.println("FAILED");\s
					}\s
					int bar(final int i) {\s
						class X {\s
							int bar() {\s
								return new Object(){ \s
										int foo(){\s
											return i;\s
										}\s
									}.foo();\s
							}\s
						}\s
						return new X().bar();\s
					}\s
				}\s
				"""
		},
		"SUCCESS"
	);
}
/**
 * 1F3AH7N: GPF on innerclass emulation for double anonymous type
 */
public void test012() {
	this.runConformTest(
		new String[] {
			/* A.java */
			"A.java",
			"""
				public class A {\s
					public static void main(String[] argv){\s
						if (new A().foo() == 5)\s
							System.out.println("SUCCESS");\s
						else\s
							System.out.println("FAILED");\s
					}\s
					int foo() {\s
						return new A() {\s
							int foo() {\s
								final int i = "hello".length();\s
								return new A() {\s
									int foo() {\s
										return i;\s
									}\s
								}\s
								.foo();\s
							}\s
						}\s
						.foo();\s
					}\s
				}\s
				"""
		},
		"SUCCESS"
	);
}
/**
 * 1F26XE2: Bug in inner class emulation
 */
public void test013() {
	this.runConformTest(
		new String[] {
			/* Inner.java */
			"Inner.java",
			"""
				public class Inner {\s
					public static void main(String[] argv){\s
						new B().new C("hello");\s
						System.out.println("SUCCESS");\s
					}\s
				  class A { \t
				    public A(String s){ this.s=s; }\t
				    String s;\t
				  }\t
				}\t
				class B {\t
				  class C extends Inner.A {\t
				    public C(String s){  B.this.inner.super(s); }   \t
				  }\t
				  Inner inner=new Inner();\t
				}\t
				"""
		},
		"SUCCESS"
	);
}
/**
 * 1EX5I8Z: Inner class emulation bug
 */
public void test014() {
	this.runConformTest(
		new String[] {
			/* Z1.java */
			"Z1.java",
			"""
				public class Z1 {\s
					public static void main(String[] argv){\s
						new Z1().new W();\s
						System.out.println("SUCCESS");\s
					}\s
					class W extends Y {\s
						W() {\s
							super(new Object(), foo());\s
						}\s
					}\s
					String foo() {\s
						return "";\s
					}\s
				}\s
				class Y {\s
					Y(Object o, String s) {\s
					}\s
				}\t
				"""
		},
		"SUCCESS"
	);
}
/**
 * 1EUC39Y: Incorrect Synthetic Emulation
 */
public void test015() {
	this.runConformTest(
		new String[] {
			/* AAA.java */
			"AAA.java",
			"""
				public class AAA { \s
					public static void main(String argv[]){ \s
						if (new AAA().foo(5) == 15); \s
						System.out.println("SUCCESS");\s
					} \s
					int foo(final int loc){ \s
						class I { \s
							int i = loc; \s
							{ \s
								System.out.println("loc="+ loc );	 \s
							} \s
							int foo(){  \s
								System.out.println("I:i="+ i );	 \s
								return i;}  \s
						}   \s
						class J extends I { \s
							I obj = new I(){ \s
								int foo() { \s
									System.out.println("J"); \s
									return super.foo() + 10; }}; \s
						} \s
						return new J().obj.foo(); \s
					} \s
				 } \s
				"""
		},
		"""
			loc=5
			loc=5
			J
			I:i=5
			SUCCESS"""
	);
}
/**
 * 1EUC39Y: Incorrect Synthetic Emulation
 */
public void test016() {
	this.runConformTest(
		new String[] {
			/* AAA.java */
			"AAA.java",
			"""
				public class AAA { \s
					public static void main(String argv[]){ \s
						if (new AAA().foo(5) == 15); \s
						System.out.println("SUCCESS");\s
					} \s
					class B{}\s
					int foo(final int loc){\s
						class I extends B {\s
							int i = loc;\s
							{\s
								System.out.println("loc="+ loc );	\s
							}\s
							int foo(){ \s
								System.out.println("I:i="+ i );	\s
								return i;} \s
						}  \s
						class J extends I {\s
							I obj = new I(){\s
								int foo() {\s
									System.out.println("J");\s
									return super.foo() + 10; }};\s
						}\s
						return new J().obj.foo();\s
					}\s
				 } \s
				"""
		},
		"""
			loc=5
			loc=5
			J
			I:i=5
			SUCCESS"""
	);
}
/**
 * Complex multi-threaded test involving inner classes
 */
public void test017() {
	this.runConformTest(
		new String[] {
			/* MultiComplex.java */
			"MultiComplex.java",
			"""
				public class MultiComplex {
					// should just be an interface, once supported...
					abstract class AbstractTask implements Runnable {
						public void run() {
							MultiComplex.this.notifyCompletion(this,0);\s
						}
						abstract public String taskName();	\t
					}
					public static void main(String argv[]){
						try {
							new MultiComplex().performTasks(3);
						}
						catch(InterruptedException e){};
					} \s
					void notifyCompleted(AbstractTask task) {
					}
					void notifyCompletion(AbstractTask task, int percentage) {
					}
					void notifyExecutionEnd() {
						System.out.println("EXECUTION FINISHED");
					}
					void notifyExecutionStart() {
						System.out.println("EXECUTION STARTING");
					}
					void performTasks(final int maxTasks) throws InterruptedException {
						Thread workers[] = new Thread[maxTasks];
						AbstractTask tasks[] = new AbstractTask[maxTasks];
						final int maxIteration = 5;
				\s
						// Local Task\s
						class Task extends AbstractTask {\s
								String taskName;\s
								Task(String aName) {
									taskName = aName;
								}
								public String taskName() {\s
									return taskName;\s
								}
				\t
								public void run() {
									super.run();
									for(int j = 0; j < maxIteration; j++)
										MultiComplex.this.notifyCompletion(this,  (int)((float) (j + 1) / maxIteration * 100));
								}
						};
						notifyExecutionStart();
					\t
						// Creating and launching the tasks
						for (int ii = 0; ii < maxTasks; ii++) {
							final int i = ii;
							tasks[i] = new Task(String.valueOf(i + 1)) {		\t
								public String taskName() {\s
									return super.taskName() +  " of " + maxTasks; }
								public void run() {
									super.run();
									MultiComplex.this.notifyCompleted(this);
								}	\t
							};
							workers[i] = new Thread(tasks[i],tasks[i].taskName());
							workers[i].start();
						}
						// Waiting for *all* tasks to be ended
						for (int i = 0; i < tasks.length; i++)
							workers[i].join();
						notifyExecutionEnd();
					}
				}
				"""
		},
		"EXECUTION STARTING\n" +
		"EXECUTION FINISHED"
	);
}
/**
 * Complex multi-threaded test involving inner classes
 */
public void test018() {
	this.runConformTest(
		new String[] {
			/* MultiAnonymous.java */
			"MultiAnonymous.java",
			"""
				public class MultiAnonymous {
					public static void main(String argv[]){
						try {
							new MultiAnonymous().performTasks(3);
						}
						catch(InterruptedException e){};
					}
					void notifyExecutionEnd() {
						System.out.println("EXECUTION FINISHED");
					}
					void notifyExecutionStart() {
						System.out.println("EXECUTION STARTING");
					}
					void performTasks(final int maxTasks) throws java.lang.InterruptedException {
						Thread workers[] = new Thread[maxTasks];
						Runnable tasks[] = new Runnable[maxTasks];
						final int maxIteration = 5;
						notifyExecutionStart();
					\t
						// Creating and launching the tasks
						for (int ii = 0; ii < maxTasks; ii++) {
							final int i = ii;
							tasks[i] = new Runnable() {		\t
								public String toString() { return ((i + 1) + " of " + maxTasks); }
								public void run() {
									for(int j = 0; j < maxIteration; j++)
										notifyCompletion( (int)((float) (j + 1) / maxIteration * 100));
								}	\t
						\t
								void notifyCompletion(int percentage) {
								}
							};
							workers[i] = new Thread(tasks[i],"Running task("+(tasks[i].toString())+")");
							workers[i].start();
						}
						// Waiting for *all* tasks to be ended
						for (int i = 0; i < tasks.length; i++)
							workers[i].join();
						notifyExecutionEnd();
					}
				}
				"""
		},
		"EXECUTION STARTING\n" +
		"EXECUTION FINISHED"
	);
}
/**
 * Complex multi-threaded test involving inner classes
 */
public void test019() {
	this.runConformTest(
		new String[] {
			/* MultiComplex2.java */
			"MultiComplex2.java",
			"""
				public class MultiComplex2 {
					public interface AbstractTask extends Runnable {
						public void run();
						public String taskName();	\t
					}
				\t
					public static void main(String argv[]){
						try {
							new MultiComplex2().performTasks(3);
						}
						catch(InterruptedException e){};
					}
					void notifyCompleted(AbstractTask task) {
					}
					void notifyCompletion(AbstractTask task, int percentage) {
					}
					void notifyExecutionEnd() {
						System.out.println("EXECUTION FINISHED");
					}
					void notifyExecutionStart() {
						System.out.println("EXECUTION STARTING");
					}
						void performTasks(final int maxTasks) throws java.lang.InterruptedException {
						Thread workers[] = new Thread[maxTasks];
						AbstractTask tasks[] = new AbstractTask[maxTasks];
						final int maxIteration = 5;
						// Local Task
						class Task implements AbstractTask {
								String taskName;
								Task(String aName) {
									taskName = aName;
								}
								public String taskName() {\s
									return taskName;\s
								}
				\t
								public void run() {
									MultiComplex2.this.notifyCompletion(this,0);\s
									for(int j = 0; j < maxIteration; j++)
										MultiComplex2.this.notifyCompletion(this,  (int)((float) (j + 1) / maxIteration * 100));
								}
						};
						notifyExecutionStart();
					\t
						// Creating and launching the tasks
						for (int ii = 0; ii < maxTasks; ii++) {
							final int i = ii;
							tasks[i] = new Task(String.valueOf(i + 1)) {		\t
								public String taskName() {\s
									return super.taskName() +  " of " + maxTasks; }
								public void run() {
									super.run();
									MultiComplex2.this.notifyCompleted(this);
								}	\t
							};
							workers[i] = new Thread(tasks[i],tasks[i].taskName());
							workers[i].start();
						}
						// Waiting for *all* tasks to be ended
						for (int i = 0; i < tasks.length; i++)
							workers[i].join();
						notifyExecutionEnd();
					}
				}
				"""
		},
		"EXECUTION STARTING\n" +
		"EXECUTION FINISHED"
	);
}
/**
 * Complex multi-threaded test involving inner classes
 */
public void test020() {
	this.runConformTest(
		new String[] {
			/* MultiLocal.java */
			"MultiLocal.java",
			"""
				public class MultiLocal {
					public static void main(String argv[]){
						class Task implements Runnable {
							private String taskName;
							private int maxIteration;\s
							public Task(String name, int value) {
								taskName = name;\s
								maxIteration = value;
							}
				\t
							public String toString() { return taskName; }
							public void run() {
								for(int i = 0; i < maxIteration; i++)
									notifyCompletion( (int)((float) (i + 1) / maxIteration * 100));
							}	\t
						\t
							void notifyCompletion(int percentage) {
							}
						};
						MultiLocal multi = new MultiLocal();
						int maxTasks = 3;
						Task tasks[] = new Task[maxTasks];
						for (int i = 0; i < maxTasks; i++)\s
							tasks[i] = new Task(String.valueOf(i),5);
						try {
							multi.performTasks(tasks);
						}
						catch(InterruptedException e){};
					}
					void notifyExecutionEnd() {
						System.out.println("EXECUTION FINISHED");
					}
					void notifyExecutionStart() {
						System.out.println("EXECUTION STARTING");
					}
					void performTasks(Runnable tasks[]) throws java.lang.InterruptedException {
						Thread workers[] = new Thread[tasks.length];
						notifyExecutionStart();
					\t
						// Launching the tasks
						for (int i = 0; i < tasks.length; i++) {
							workers[i] = new Thread(tasks[i],"Running task("+(tasks[i].toString())+")");
							workers[i].start();
						}
						// Waiting for *all* tasks to be ended
						for (int i = 0; i < tasks.length; i++)
							workers[i].join();
						notifyExecutionEnd();
					}
				}
				"""
		},
		"EXECUTION STARTING\n" +
		"EXECUTION FINISHED"
	);
}
/**
 * Complex multi-threaded test involving inner classes
 */
public void test021() {
	this.runConformTest(
		new String[] {
			/* MultiLocal2.java */
			"MultiLocal2.java",
			"""
				public class MultiLocal2 {
					public static void main(String argv[]){
						final int maxTasks = 3;
						class Task implements Runnable {
							private String taskName;
							private int maxIteration;
							public Task(String name, int value) {
								taskName = name;\s
								maxIteration = value;
							}
				\t
							public String toString() { return taskName + " of " + String.valueOf(maxTasks); }
							public void run() {
								for(int i = 0; i < maxIteration; i++)
									notifyCompletion( (int)((float) (i + 1) / maxIteration * 100));
							}	\t
						\t
							void notifyCompletion(int percentage) {
							}
						};
						MultiLocal2 multi = new MultiLocal2();
						Task tasks[] = new Task[maxTasks];
						for (int i = 0; i < maxTasks; i++)\s
							tasks[i] = new Task(String.valueOf(i+1),5);
						try {
							multi.performTasks(tasks);
						}
						catch(InterruptedException e){};
					}
					void notifyExecutionEnd() {
						System.out.println("EXECUTION FINISHED");
					}
					void notifyExecutionStart() {
						System.out.println("EXECUTION STARTING");
					}
					void performTasks(Runnable tasks[]) throws java.lang.InterruptedException {
						Thread workers[] = new Thread[tasks.length];
						notifyExecutionStart();
					\t
						// Launching the tasks
						for (int i = 0; i < tasks.length; i++) {
							workers[i] = new Thread(tasks[i],"Running task("+(tasks[i].toString())+")");
							workers[i].start();
						}
						// Waiting for *all* tasks to be ended
						for (int i = 0; i < tasks.length; i++)
							workers[i].join();
						notifyExecutionEnd();
					}
				}
				"""
		},
		"EXECUTION STARTING\n" +
		"EXECUTION FINISHED"
	);
}
/**
 * Complex multi-threaded test involving inner classes
 */
public void test022() {
	this.runConformTest(
		new String[] {
			/* MultiMember.java */
			"MultiMember.java",
			"""
				public class MultiMember {
					class Task implements Runnable {
						private String taskName;\s
						private int maxIteration;
						public Task(String name, int value) {
							taskName = name;\s
							maxIteration = value;
						}
						public String toString() { return taskName; }
						public void run() {
							for(int i = 0; i < maxIteration; i++)
								notifyCompletion( (int)((float) (i + 1) / maxIteration * 100));
						}	\t
					\t
						void notifyCompletion(int percentage) {
						}
					}
					public static void main(String argv[]){
						MultiMember multi = new MultiMember();
						int maxTasks = 3;
						Task tasks[] = new Task[maxTasks];
						for (int i = 0; i < maxTasks; i++)\s
							tasks[i] = multi.new Task(String.valueOf(i),5);
						try {
							multi.performTasks(tasks);
						}
						catch(InterruptedException e){};
					}
					void notifyExecutionEnd() {
						System.out.println("EXECUTION FINISHED");
					}
					void notifyExecutionStart() {
						System.out.println("EXECUTION STARTING");
					}
					void performTasks(Task tasks[]) throws java.lang.InterruptedException {
						Thread workers[] = new Thread[tasks.length];
						notifyExecutionStart();
					\t
						// Launching the tasks
						for (int i = 0; i < tasks.length; i++) {
							workers[i] = new Thread(tasks[i],"Running task("+(tasks[i].toString())+")");
							workers[i].start();
						}
						// Waiting for *all* tasks to be ended
						for (int i = 0; i < tasks.length; i++)
							workers[i].join();
						notifyExecutionEnd();
					}
				}
				"""
		},
		"EXECUTION STARTING\n" +
		"EXECUTION FINISHED"
	);
}
/**
 * No need for protected access emulation
 */
public void test023() {
	this.runConformTest(
		new String[] {
			/* X.java */
			"p/X.java",
			"""
				package p;\s
				public class X extends q.Y {\s
					void bar(){ Object o = someObject; }\s
					public static void main(String[] argv){\s
						new X().bar();
						System.out.println("SUCCESS");
					}
				}
				""",
			/* Y.java */
			"q/Y.java",
			"""
				package q;\s
				public class Y {\s
					protected Object someObject;\s
				}
				"""
		},
		"SUCCESS"
	);
}
/**
 * No need for protected access emulation
 */
public void test024() {
	this.runConformTest(
		new String[] {
			/* X.java */
			"p/X.java",
			"""
				package p;\s
				public class X extends q.Y {\s
					void bar(){ foo(); }\s
					public static void main(String[] argv){\s
						new X().bar();
						System.out.println("SUCCESS");
					}
				}
				""",
			/* Y.java */
			"q/Y.java",
			"""
				package q;\s
				public class Y {\s
					protected Object foo(){ return null;}\s
				}
				"""
		},
		"SUCCESS"
	);
}

public void test025() {
	this.runConformTest(
		new String[] {
			/* PortReport.java */
			"PortReport.java",
			"""
				import java.util.*;\s
				public class PortReport {\s
					public static void main(String[] args) {\s
						Portfolio port = new Portfolio("foobar");\s
						System.out.println("SUCCESS");\s
					}\s
				}\s
				""",
			/* Portfolio.java */
			"Portfolio.java",
			"""
				import java.util.*;\t
				public class Portfolio {\t
					String name;\t
					public Portfolio(String buf) {\t
						TokenBuffer tbuf = new TokenBuffer();\t
						switch (1) {\t
							case TokenBuffer.T_NAME :\t
								name = "figi";\t
						}\t
					}\t
					String getName() {\t
						return name;\t
					}\t
					class TokenBuffer {\t
						static final int T_NAME = 3;\t
					}\t
				}\t
				"""
		},
		"SUCCESS"
	);
}
/**
 * Compatability - Compiler does not comply with 1.1 standard.
 */
public void test026() {
	this.runConformTest(
		new String[] {
			/* Test.java */
			"p2/Test.java",
			"""
				package p2;\t
				public class Test {\t
					public static void main (String args[]){\t
						new c2();\t
					}\t
				}\t
				""",
			/* c1.java */
			"p1/c1.java",
			"""
				package p1;\t
				public class c1 {\t
					protected class c1a{\t
						public c1a(){}\t
						public void foo(){ System.out.println("Foo called");\t
						}\t
					};\t
				}\t
				""",
			/* c2.java */
			"p2/c2.java",
			"""
				package p2;\t
				import p1.*;\t
				public class c2 extends c1 {\t
					public c1a myC1a;\t
					{\t
						myC1a = new c1a();\t
						myC1a.foo();\t
					}\t
				}\t
				"""
		},
		"Foo called"
	);
}
/**
 * Compatability - Compiler does not comply with 1.1 standard.
 */
public void test027() {
	this.runNegativeTest(
		new String[] {
			/* Test.java */
			"p2/Test.java",
			"""
				package p2;\t
				public class Test {\t
					public static void main (String args[]){\t
						new c2();\t
					}\t
				}\t
				""",
			/* c1.java */
			"p1/c1.java",
			"""
				package p1;\t
				public class c1 {\t
					public class c1m {\t
						protected class c1a{\t
							public c1a(){}\t
							public void foo(){ System.out.println("Foo called");\t
							}\t
						};\t
					};\t
				}\t
				""",
			/* c2.java */
			"p2/c2.java",
			"""
				package p2;\t
				import p1.*;\t
				public class c2 extends c1 {\t
					public c1m.c1a myC1a;\t
					{\t
						myC1a = new c1m().new c1a();\t
						myC1a.foo();\t
					}\t
				}\t
				"""
		},
		"""
			----------
			1. ERROR in p2\\c2.java (at line 4)
				public c1m.c1a myC1a;\t
				       ^^^^^^^
			The type c1m.c1a is not visible
			----------
			2. ERROR in p2\\c2.java (at line 6)
				myC1a = new c1m().new c1a();\t
				                      ^^^
			The type c1.c1m.c1a is not visible
			----------
			3. ERROR in p2\\c2.java (at line 7)
				myC1a.foo();\t
				^^^^^
			The type c1.c1m.c1a is not visible
			----------
			""");
}
/**
 * Compatability - Compiler does not comply with 1.1 standard.
 */
public void test028() {
	this.runNegativeTest(
		new String[] {
			/* Test.java */
			"p2/Test.java",
			"""
				package p2;\t
				public class Test {\t
					public static void main (String args[]){\t
						new c2();\t
					}\t
				}\t
				""",
			/* c1.java */
			"p1/c1.java",
			"""
				package p1;\t
				public class c1 {\t
					protected class c1m {\t
						protected class c1a{\t
							public c1a(){}\t
							public void foo(){ System.out.println("Foo called");\t
							}\t
						};\t
					};\t
				}\t
				""",
			/* c2.java */
			"p2/c2.java",
			"""
				package p2;\t
				import p1.*;\t
				public class c2 extends c1 {\t
					public c1m.c1a myC1a;\t
					{\t
						myC1a = new c1m().new c1a();\t
						myC1a.foo();\t
					}\t
				}\t
				"""
		},
		"""
			----------
			1. ERROR in p2\\c2.java (at line 4)
				public c1m.c1a myC1a;\t
				       ^^^^^^^
			The type c1m.c1a is not visible
			----------
			2. ERROR in p2\\c2.java (at line 6)
				myC1a = new c1m().new c1a();\t
				        ^^^^^^^^^
			The constructor c1.c1m() is not visible
			----------
			3. ERROR in p2\\c2.java (at line 6)
				myC1a = new c1m().new c1a();\t
				                      ^^^
			The type c1.c1m.c1a is not visible
			----------
			4. ERROR in p2\\c2.java (at line 7)
				myC1a.foo();\t
				^^^^^
			The type c1.c1m.c1a is not visible
			----------
			""");
}
/**
 * Compatability - Compiler does not comply with 1.1 standard.
 */
public void test029() {
	this.runConformTest(
		new String[] {
			/* Test.java */
			"p2/Test.java",
			"""
				package p2;\t
				public class Test {\t
					public static void main (String args[]){\t
						new c2();\t
					}\t
				}\t
				""",
			/* c1.java */
			"p1/c1.java",
			"""
				package p1;\t
				public class c1 {\t
					protected class c1a{\t
						public c1a(){}\t
						public void foo(){ System.out.println("Foo called");\t
						}\t
					};\t
				}\t
				""",
			/* c2.java */
			"p2/c2.java",
			"""
				package p2;\t
				import p1.*;\t
				public class c2 extends c1 {\t
					public c1.c1a myC1a;
					{\t
						myC1a = new c1a();\t
						myC1a.foo();\t
					}\t
				}\t
				"""
		},
		"Foo called");
}
/**
 * Compatability - Compiler does not comply with 1.1 standard.
 */
public void test030() {
	this.runNegativeTest(
		new String[] {
			/* Test.java */
			"p2/Test.java",
			"""
				package p2;\t
				public class Test {\t
					public static void main (String args[]){\t
						new c2();\t
					}\t
				}\t
				""",
			/* c1.java */
			"p1/c1.java",
			"""
				package p1;\t
				public class c1 {\t
					protected class c1a{\t
						public c1a(){}\t
						public void foo(){ System.out.println("Foo called");\t
						}\t
					};\t
				}\t
				""",
			/* c2.java */
			"p2/c2.java",
			"""
				package p2;\t
				import p1.*;\t
				public class c2 extends c1.c1a {// qualified acces does not work\t
					public c1a myC1a;\s
					{\t
						myC1a = new c1a();\t
						myC1a.foo();\t
					}\t
				}\t
				"""
		},
		"""
			----------
			1. ERROR in p2\\c2.java (at line 3)
				public class c2 extends c1.c1a {// qualified acces does not work\t
				                        ^^^^^^
			The type c1.c1a is not visible
			----------
			2. ERROR in p2\\c2.java (at line 4)
				public c1a myC1a;\s
				       ^^^
			c1a cannot be resolved to a type
			----------
			3. ERROR in p2\\c2.java (at line 6)
				myC1a = new c1a();\t
				^^^^^
			c1a cannot be resolved to a type
			----------
			4. ERROR in p2\\c2.java (at line 6)
				myC1a = new c1a();\t
				            ^^^
			c1a cannot be resolved to a type
			----------
			5. ERROR in p2\\c2.java (at line 7)
				myC1a.foo();\t
				^^^^^
			c1a cannot be resolved to a type
			----------
			""");
}
/**
 * Compatibility - Compiler does not comply with 1.1 standard.
 */
public void test031() {
	this.runNegativeTest(
		new String[] {
			/* Test.java */
			"p2/Test.java",
			"""
				package p2;\t
				public class Test {\t
					public static void main (String args[]){\t
						new c2();\t
					}\t
				}\t
				""",
			/* c1.java */
			"p1/c1.java",
			"""
				package p1;\t
				public class c1 {\t
					protected class c1a{\t
						public c1a(){}\t
						public void foo(){ System.out.println("Foo called");\t
						}\t
					};\t
				}\t
				""",
			/* c2.java */
			"p2/c2.java",
			"""
				package p2;\t
				import p1.c1.*;\t
				public class c2 extends c1a {\t
					public c1a myC1a;\s
					{\t
						myC1a = new c1a();\t
						myC1a.foo();\t
					}\t
				}\t
				"""
		},
		"""
			----------
			1. ERROR in p2\\c2.java (at line 3)
				public class c2 extends c1a {\t
				                        ^^^
			The type c1a is not visible
			----------
			2. ERROR in p2\\c2.java (at line 4)
				public c1a myC1a;\s
				       ^^^
			The type c1a is not visible
			----------
			3. ERROR in p2\\c2.java (at line 6)
				myC1a = new c1a();\t
				            ^^^
			The type c1a is not visible
			----------
			4. ERROR in p2\\c2.java (at line 7)
				myC1a.foo();\t
				^^^^^
			The type c1.c1a is not visible
			----------
			""");
}
/**
 * VerifyError using .class literal inside inner classes
 */
public void test032() {
	this.runConformTest(
		new String[] {
			"p/A.java",
			"""
				package p;\t
				public class A {
						public class B {
								public B() {
								}
								public Class getCls() {
										return A.class;
								}
						}
						public A() {
								super();
								B b = new B();
								System.out.println("Class: " + b.getCls());
						}
						public static void main(String[] args) {
								A a = new A();
						}
				}
				"""
		},
		"Class: class p.A");
}
/**
 * Missing implementation in the compiler compiling invalid code
 */
public void test033() {
	String errMessage = isMinimumCompliant(ClassFileConstants.JDK11) ?
			"""
				----------
				1. ERROR in p1\\A2.java (at line 20)
					(new D.E(null, null, null, new F(get()) {}) {}).execute();\t
					^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				No enclosing instance of type D is accessible. Must qualify the allocation with an enclosing instance of type D (e.g. x.new A() where x is an instance of D).
				----------
				"""
			:
			"""
				----------
				1. WARNING in p1\\A2.java (at line 18)
					private class C extends B {\t
					              ^
				Access to enclosing constructor A2.B() is emulated by a synthetic accessor method
				----------
				2. ERROR in p1\\A2.java (at line 20)
					(new D.E(null, null, null, new F(get()) {}) {}).execute();\t
					^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				No enclosing instance of type D is accessible. Must qualify the allocation with an enclosing instance of type D (e.g. x.new A() where x is an instance of D).
				----------
				""";
	this.runNegativeTest(
		new String[] {
			/* A2.java */
			"p1/A2.java",
			"""
				package p1;\t
				class D {\t
					class E {\t
						E(Object o, Object o1, Object o2, F f) {}\t
						void execute() {}\t
					}\t
				}\t
				class F {\t
					F(Object o) {\t
					}\t
				}\t
				public class A2 {\t
					private abstract class B {\t
						public Object get() {\t
							return null;\t
						}\t
					}\t
					private class C extends B {\t
						public void foo() {\t
							(new D.E(null, null, null, new F(get()) {}) {}).execute();\t
						}\t
					}\t
				}
				"""
		},
		errMessage);
}
/**
 * Missing implementation in the compiler compiling invalid code
 */
public void test034() {
	this.runConformTest(
		new String[] {
			/* A2.java */
			"p1/A2.java",
			"""
				package p1;\t
				class D {\t
					class E {\t
						E(Object o, Object o1, Object o2, F f) {}\t
						void execute() {}\t
					}\t
				}\t
				class F {\t
					F(Object o) {\t
					}\t
				}\t
				public class A2 {\t
					private abstract class B {\t
						public Object get() {\t
							return null;\t
						}\t
					}\t
					private class C extends B {\t
						public void foo() {\t
							(new D().new E(null, null, null, new F(get()) {}) {}).execute();\t
						}\t
					}\t
					public static void main(String[] argv){\t
						new A2().new C().foo();\t
						System.out.println("SUCCESS");\t
					}\t
				}\t
				"""
		},
		"SUCCESS"
	);
}
/**
 * Missing implementation in the compiler compiling invalid code
 */
public void test035() {
	String errMessage = isMinimumCompliant(ClassFileConstants.JDK11) ?
			"""
				----------
				1. ERROR in p1\\A2.java (at line 20)
					(new D.E(null, null, null, new F(get()) {})).execute();\t
					^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				No enclosing instance of type D is accessible. Must qualify the allocation with an enclosing instance of type D (e.g. x.new A() where x is an instance of D).
				----------
				"""
			:
			"""
				----------
				1. WARNING in p1\\A2.java (at line 18)
					private class C extends B {\t
					              ^
				Access to enclosing constructor A2.B() is emulated by a synthetic accessor method
				----------
				2. ERROR in p1\\A2.java (at line 20)
					(new D.E(null, null, null, new F(get()) {})).execute();\t
					^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				No enclosing instance of type D is accessible. Must qualify the allocation with an enclosing instance of type D (e.g. x.new A() where x is an instance of D).
				----------
				""";
	this.runNegativeTest(new String[] {
			/* A2.java */
			"p1/A2.java",
			"""
				package p1;\t
				class D {\t
					class E {\t
						E(Object o, Object o1, Object o2, F f) {}\t
						void execute() {}\t
					}\t
				}\t
				class F {\t
					F(Object o) {\t
					}\t
				}\t
				public class A2 {\t
					private abstract class B {\t
						public Object get() {\t
							return null;\t
						}\t
					}\t
					private class C extends B {\t
						public void foo() {\t
							(new D.E(null, null, null, new F(get()) {})).execute();\t
						}\t
					}\t
				}
				"""},
		errMessage);
}
/**
 * ClassCastException during inner class emulation
 */
public void test036() {
	this.runConformTest(
		new String[] {
			/* A.java */
			"p1/A.java",
			"""
				package p1;\t
				public class A {\t
					public static void main(String[] argv){\t
						new A().foo();\t
						System.out.println("SUCCESS");\t
					}\t
					public Object foo() {\t
						B b = new B() {\t
							protected Object bar() {\t
								return new B.C() {};\t
							}\t
						};\t
						return b;\t
					}\t
				}\t
				class B {\t
					class C {\t
					}\t
				}\t
				"""
		},
		"SUCCESS"
	);
}
/**
 * ClassCastException during inner class emulation
 */
public void test037() {
	this.runConformTest(
		new String[] {
			/* A.java */
			"p1/A.java",
			"""
				package p1;\t
				public class A {\t
					public static void main(String[] argv){\t
						new A().foo();\t
						System.out.println("SUCCESS");\t
					}\t
					public Object foo() {\t
						I i = new I() {\t
							protected Object bar() {\t
								return new I.C() {};\t
							}\t
						};\t
						return i;\t
					}\t
				}\t
				interface I {\t
					class C {\t
					}\t
				}\t
				"""
		},
		"SUCCESS"
	);
}

/**
 * Enclosing instance comparison
 */
public void test038() {
	this.runConformTest(
		new String[] {
			/* X.java */
			"X.java",
			"""
				public class X {\t
					public static void main(String argv[]) {\t
						if (new X().foo())\t
							System.out.println("FAILED");\t
						System.out.println("SUCCESS");\t
					}\t
					boolean bar() {\t
						return false;\t
					}\t
					boolean foo() {\t
						X x = new X() {\t
							boolean test() {\t
								return (X.this == this);\t
							}\t
						};\t
						return x.bar();\t
					}\t
				}\t
				"""
		},
		"SUCCESS"
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=6456
 * Invalid error when compiling access to protected member inside innerclass
 */
public void test039() {
	this.runConformTest(
		new String[] {
			/* p1/X.java */
			"p1/X.java",
			"""
				package p1;\t
				public class X {\t
					protected void foo() {\t
						System.out.println("SUCCESS");\t
					}\t
					public static void main(String argv[]) {\t
						new p2.Y().bar();\t
					}\t
				}\t
				""",
			/* p2/Y.java */
			"p2/Y.java",
			"""
				package p2;\t
				public class Y extends p1.X {\t
					public void bar() {\t
						new Object(){\t
					      void doSomething(){\t
					        Y.this.foo();\s
					      }\t
					    }.doSomething();\t
					}\t
				}\t
				""",
		},
		"SUCCESS"
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=6456
 * Invalid error when compiling access to protected member inside innerclass
 */
public void test040() {
	this.runConformTest(
		new String[] {
			/* p1/X.java */
			"p1/X.java",
			"""
				package p1;\t
				public class X {\t
					protected void foo() {\t
						System.out.println("SUCCESS");\t
					}\t
					public static void main(String argv[]) {\t
						new p2.Y().bar();\t
					}\t
				}\t
				""",
			/* p2/Y.java */
			"p2/Y.java",
			"""
				package p2;\t
				public class Y extends p1.X {\t
					public void bar() {\t
						new Object(){\t
					      void doSomething(){\t
					        new Y().foo();\s
					      }\t
					    }.doSomething();\t
					}\t
				}\t
				""",
		},
		"SUCCESS"
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=6456
 * Invalid error when compiling access to protected member inside innerclass
 */
public void test041() {
	this.runConformTest(
		new String[] {
			/* p1/X.java */
			"p1/X.java",
			"""
				package p1;\t
				public class X {\t
					protected void foo() {\t
						System.out.println("SUCCESS");\t
					}\t
					public static void main(String argv[]) {\t
						new p2.Y().bar();\t
					}\t
				}\t
				""",
			/* p2/Y.java */
			"p2/Y.java",
			"""
				package p2;\t
				public class Y extends p1.X {\t
					public void bar() {\t
						new Object(){\t
					      void doSomething(){\t
					        Y.super.foo();\s
					      }\t
					    }.doSomething();\t
					}\t
				}\t
				""",
		},
		"SUCCESS"
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=6456
 * Invalid error when compiling access to protected member inside innerclass
 */
public void test042() {
	this.runConformTest(
		new String[] {
			/* p1/X.java */
			"p1/X.java",
			"""
				package p1;\t
				public class X {\t
					protected String foo = "SUCCESS";\t
					public static void main(String argv[]) {\t
						new p2.Y().bar();\t
					}\t
				}\t
				""",
			/* p2/Y.java */
			"p2/Y.java",
			"""
				package p2;\t
				public class Y extends p1.X {\t
					public void bar() {\t
						new Object(){\t
					      void doSomething(){\t
					        System.out.println(Y.super.foo);\s
					      }\t
					    }.doSomething();\t
					}\t
				}\t
				""",
		},
		"SUCCESS"
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=6456
 * Invalid error when compiling access to protected member inside innerclass
 */
public void test043() {
	this.runConformTest(
		new String[] {
			/* p1/X.java */
			"p1/X.java",
			"""
				package p1;\t
				public class X {\t
					protected String foo = "SUCCESS";\t
					public static void main(String argv[]) {\t
						new p2.Y().bar();\t
					}\t
				}\t
				""",
			/* p2/Y.java */
			"p2/Y.java",
			"""
				package p2;\t
				public class Y extends p1.X {\t
					public void bar() {\t
						new Object(){\t
					      void doSomething(){\t
					        System.out.println(Y.this.foo);\s
					      }\t
					    }.doSomething();\t
					}\t
				}\t
				""",
		},
		"SUCCESS"
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=6456
 * Invalid error when compiling access to protected member inside innerclass
 */
public void test044() {
	this.runConformTest(
		new String[] {
			/* p1/X.java */
			"p1/X.java",
			"""
				package p1;\t
				public class X {\t
					protected String foo = "SUCCESS";\t
					public static void main(String argv[]) {\t
						new p2.Y().bar();\t
					}\t
				}\t
				""",
			/* p2/Y.java */
			"p2/Y.java",
			"""
				package p2;\t
				public class Y extends p1.X {\t
					public void bar() {\t
						new Object(){\t
					      void doSomething(){\t
					        System.out.println(new Y().foo);\s
					      }\t
					    }.doSomething();\t
					}\t
				}\t
				""",
		},
		"SUCCESS"
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=6456
 * Invalid error when compiling access to protected member inside innerclass
 */
public void test045() {
	this.runConformTest(
		new String[] {
			/* p1/X.java */
			"p1/X.java",
			"""
				package p1;\t
				public class X {\t
					protected String foo = "SUCCESS";\t
					public static void main(String argv[]) {\t
						new p2.Y().bar();\t
					}\t
				}\t
				""",
			/* p2/Y.java */
			"p2/Y.java",
			"""
				package p2;\t
				public class Y extends p1.X {\t
					public void bar() {\t
						new Object(){\t
					      void doSomething(){\t
					        System.out.println(foo);\s
					      }\t
					    }.doSomething();\t
					}\t
				}\t
				""",
		},
		"SUCCESS"
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=6456
 * Invalid error when compiling access to protected member inside innerclass
 */
public void test046() {
	this.runConformTest(
		new String[] {
			/* p1/X.java */
			"p1/X.java",
			"""
				package p1;\t
				public class X {\t
					protected String foo = "SUCCESS";\t
					public static void main(String argv[]) {\t
						new p2.Y().bar();\t
					}\t
				}\t
				""",
			/* p2/Y.java */
			"p2/Y.java",
			"""
				package p2;\t
				public class Y extends p1.X {\t
				   Y someY;\t
					public void bar() {\t
						someY = this;\t
						new Object(){\t
					      void doSomething(){\t
					        System.out.println(someY.foo);\s
					      }\t
					    }.doSomething();\t
					}\t
				}\t
				""",
		},
		"SUCCESS"
	);
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=6456
 * Invalid error when compiling access to protected member inside innerclass
 */
public void test047() {
	this.runConformTest(
		new String[] {
			/* p1/X.java */
			"p1/X.java",
			"""
				package p1;\t
				public class X {\t
					protected String foo = "FAILED";\t
					public static void main(String argv[]) {\t
						new p2.Y().bar();\t
					}\t
				}\t
				""",
			/* p2/Y.java */
			"p2/Y.java",
			"""
				package p2;\t
				public class Y extends p1.X {\t
					public void bar() {\t
						new Object(){\t
					      void doSomething(){\t
					        System.out.println(Y.super.foo = "SUCCESS");\s
					      }\t
					    }.doSomething();\t
					}\t
				}\t
				""",
		},
		"SUCCESS"
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=6456
 * Invalid error when compiling access to protected member inside innerclass
 */
public void test048() {
	this.runConformTest(
		new String[] {
			/* p1/X.java */
			"p1/X.java",
			"""
				package p1;\t
				public class X {\t
					protected String foo = "FAILED";\t
					public static void main(String argv[]) {\t
						new p2.Y().bar();\t
					}\t
				}\t
				""",
			/* p2/Y.java */
			"p2/Y.java",
			"""
				package p2;\t
				public class Y extends p1.X {\t
					public void bar() {\t
						new Object(){\t
					      void doSomething(){\t
					        System.out.println(Y.this.foo = "SUCCESS");\s
					      }\t
					    }.doSomething();\t
					}\t
				}\t
				""",
		},
		"SUCCESS"
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=6456
 * Invalid error when compiling access to protected member inside innerclass
 */
public void test049() {
	this.runConformTest(
		new String[] {
			/* p1/X.java */
			"p1/X.java",
			"""
				package p1;\t
				public class X {\t
					protected String foo = "FAILED";\t
					public static void main(String argv[]) {\t
						new p2.Y().bar();\t
					}\t
				}\t
				""",
			/* p2/Y.java */
			"p2/Y.java",
			"""
				package p2;\t
				public class Y extends p1.X {\t
					public void bar() {\t
						new Object(){\t
					      void doSomething(){\t
					        System.out.println(new Y().foo = "SUCCESS");\s
					      }\t
					    }.doSomething();\t
					}\t
				}\t
				""",
		},
		"SUCCESS"
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=6456
 * Invalid error when compiling access to protected member inside innerclass
 */
public void test050() {
	this.runConformTest(
		new String[] {
			/* p1/X.java */
			"p1/X.java",
			"""
				package p1;\t
				public class X {\t
					protected String foo = "FAILED";\t
					public static void main(String argv[]) {\t
						new p2.Y().bar();\t
					}\t
				}\t
				""",
			/* p2/Y.java */
			"p2/Y.java",
			"""
				package p2;\t
				public class Y extends p1.X {\t
					public void bar() {\t
						new Object(){\t
					      void doSomething(){\t
					        System.out.println(foo = "SUCCESS");\s
					      }\t
					    }.doSomething();\t
					}\t
				}\t
				""",
		},
		"SUCCESS"
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=6456
 * Invalid error when compiling access to protected member inside innerclass
 */
public void test051() {
	this.runConformTest(
		new String[] {
			/* p1/X.java */
			"p1/X.java",
			"""
				package p1;\t
				public class X {\t
					protected String foo = "FAILED";\t
					public static void main(String argv[]) {\t
						new p2.Y().bar();\t
					}\t
				}\t
				""",
			/* p2/Y.java */
			"p2/Y.java",
			"""
				package p2;\t
				public class Y extends p1.X {\t
				   Y someY;\t
					public void bar() {\t
						someY = this;\t
						new Object(){\t
					      void doSomething(){\t
					        System.out.println(someY.foo = "SUCCESS");\s
					      }\t
					    }.doSomething();\t
					}\t
				}\t
				""",
		},
		"SUCCESS"
	);
}


public void test052() {
	this.runConformTest(
		new String[] {
			/* p2/Y.java */
			"p2/Y.java",
			"""
				package p2;\t
				public class Y {\t
					void bar(final int someVal){\t
						class Local {\t
							void localDo(final int localVal){\t
								new Object(){\t
									void doSomething(){\t
										System.out.print(someVal + localVal);\t
									}\t
								}.doSomething();\t
							}			\t
						};\t
						Local loc = new Local();\t
						loc.localDo(8);\t
						class SubLocal extends Local {\t
							void localDo(final int localVal){\t
								super.localDo(localVal + 1);\t
								new Object(){\t
									void doSomething(){\t
										SubLocal.super.localDo(localVal + 2);\t
										System.out.print(someVal + localVal + 3);\t
									}\t
								}.doSomething();\t
							}\t
						};\t
						SubLocal subloc = new SubLocal();\t
						subloc.localDo(8);\t
					}\t
					public static void main(String[] arguments) {\t
						new Y().bar(4);\t
						System.out.println();\t
					}\t
				}\t
				""",
		},
		"12131415"
	);
}

public void test053() {
	this.runConformTest(
		new String[] {
			/* p2/Z.java */
			"p2/Z.java",
			"""
				package p2;\t
				import p1.X;\t
				public class Z {\t
					class ZMember extends X {\t
						ZMember(Object o){}\t
						Z bar(){\t
							System.out.println(foo = "FAILED");\t
							return Z.this;\t
						}\t
					}\t
					Z(final Object foo){\t
						Object obj2 = new Object(){\t
							Object doSomething(){\t
								ZMember obj3 = new ZMember(foo){\t
									Z bar(){\t
										System.out.println(this.foo);\t
										return Z.this;\t
									}\s
								};\t
								obj3.bar();\t
								return this;\t
							}\t
						}.doSomething();\t
					}	\t
					public static void main(String[] arguments) {\t
						new Z(new Object());\t
					}\t
				}\t
				""",
			/* p1/X.java */
			"p1/X.java",
			"""
				package p1;\t
				public class X {\t
					protected String foo = "SUCCESS";\t
				}\t
				"""
		},
		"SUCCESS"
	);
}
public void test055() {
	this.runNegativeTest(
		new String[] {
			/* p1/X.java */
			"p1/X.java",
			"""
				package p1;\t
				public class X {\t
					void foo() {\t
						class L1 {\t
							class LM1 {\t
							}\t
						}\t
						class L2 extends L1.LM1 {\t
						}\t
						new L2();\t
					}\t
					public static void main(String[] arguments) {\t
						new X().foo();\t
					}\t
				}\t
				""",
		},
		"""
			----------
			1. ERROR in p1\\X.java (at line 8)
				class L2 extends L1.LM1 {\t
				      ^^
			No enclosing instance of type L1 is accessible to invoke the super constructor. Must define a constructor and explicitly qualify its super constructor invocation with an instance of L1 (e.g. x.super() where x is an instance of L1).
			----------
			"""

	);
}

public void test056() {
	this.runNegativeTest(
		new String[] {
			/* p1/X.java */
			"p1/X.java",
			"""
				package p1;\t
				public class X {\t
					void foo() {\t
						class L1 {\t
							class LM1 {\t
							}\t
						}\t
						new L1().new LM1(){};	//ok
						new L1.LM1(){};	//ko
					}\t
					public static void main(String[] arguments) {\t
						new X().foo();\t
					}\t
				}\t
				""",
		},
		"""
			----------
			1. ERROR in p1\\X.java (at line 9)
				new L1.LM1(){};	//ko
				^^^^^^^^^^^^^^
			No enclosing instance of type L1 is accessible. Must qualify the allocation with an enclosing instance of type L1 (e.g. x.new A() where x is an instance of L1).
			----------
			"""
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=9813
 * VerifyError with Inner Class having private constructor
 */
public void test057() {
	this.runConformTest(
		new String[] {
			/* VE_Test.java */
			"VE_Test.java",
			"""
				public class VE_Test {\t
				    class VE_Inner {\t
				        private VE_Inner() {}\t
				    }\t
				    private static void test(){\t
				        VE_Test ve_test = new VE_Test();\t
				        VE_Inner pi = ve_test.new VE_Inner();\t
				    }\t
				    public static void main(String[] args){\t
				        new VE_Test();\t
				        System.out.println("SUCCESS");\t
				    }\t
				}\t
				""",
		},
		"SUCCESS"
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=11845
 * NPE during emulation
 */
public void test058() {
	this.runConformTest(
		new String[] {
			"p1/Test.java",
			"""
				package p1;\t
				import p2.A;\t
				public class Test {\t
				    public static void main(String[] args){\t
				        new Test().a.bar();\t
				    }\t
					private A a = new A() {\t
						public void bar() {\t
							new Object() {\t
								protected void foo() {\t
									init();\t
								}\t
							}.foo(); \t
						}\t
					};\t
					private void init() {\t
						System.out.println("SUCCESS");\t
					}\t
				}\t
				""",
			"p2/A.java",
			"""
				package p2;\t
				public class A {\t
				  public void bar() {\t
				  }\t
				  private void init() {\t
						System.out.println("FAILED");\t
				  }\t
				} \t
				"""
		},
		"SUCCESS"
	);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=11845
 * variant for single name ref
 */
public void test059() {
	this.runConformTest(
		new String[] {
			"p1/Test.java",
			"""
				package p1;\t
				import p2.A;\t
				public class Test {\t
				    public static void main(String[] args){\t
				        new Test().a.bar();\t
				    }\t
					private A a = new A() {\t
						public void bar() {\t
							new Object() {\t
								protected void foo() {\t
									System.out.println(init);\t
								}\t
							}.foo(); \t
						}\t
					};\t
					private String init = "SUCCESS";\t
				}\t
				""",
			"p2/A.java",
			"""
				package p2;\t
				public class A {\t
				  public void bar() {\t
				  }\t
					private String init = "FAILED";\t
				} \t
				"""
		},
		"SUCCESS"
	);
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=11845
 * variant for qualified name ref
 */
public void test060() {
	this.runConformTest(
		new String[] {
			"p1/Test.java",
			"""
				package p1;\t
				import p2.A;\t
				public class Test {\t
				    public static void main(String[] args){\t
				        new Test().a.bar();\t
				    }\t
					private A a = new A() {\t
						public void bar() {\t
							class L {\t
								Test next = Test.this;\t
								protected void foo() {\t
									System.out.println(next.init);\t
								}\t
							};\t
							new L().foo(); \t
						}\t
					};\t
					private String init = "SUCCESS";\t
				}\t
				""",
			"p2/A.java",
			"""
				package p2;\t
				public class A {\t
				  public void bar() {\t
				  }\t
				} \t
				"""
		},
		"SUCCESS"
	);
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=11845
 * variant for field name ref
 */
public void test061() {
	this.runConformTest(
		new String[] {
			"p1/Test.java",
			"""
				package p1;\t
				import p2.A;\t
				public class Test {\t
				    public static void main(String[] args){\t
				        new Test().a.bar();\t
				    }\t
					private A a = new A() {\t
						public void bar() {\t
							class L {\t
								protected void foo() {\t
									System.out.println(Test.this.init);\t
								}\t
							};\t
							new L().foo(); \t
						}\t
					};\t
					private String init = "SUCCESS";\t
				}\t
				""",
			"p2/A.java",
			"""
				package p2;\t
				public class A {\t
				  public void bar() {\t
				  }\t
				} \t
				"""
		},
		"SUCCESS"
	);
}

public void test062() {
	this.runConformTest(
		new String[] {
			"p1/X.java",
			"""
				package p1;\t
				public class X {\t
				    public static void main(String args[]) {\t
						final String s = args.length == 0 ? "SUCCESS" : null;\t
						final class Local {\t
							private Local() {\t
								new LocalMember();\t
							}\t
							class LocalMember {\t
								{\t
									new LocalMemberMember();\t
								}\t
								class LocalMemberMember {\t
									{\t
										System.out.println(s);\t
									}\t
								}\t
							}\t
						}\t
						new Local();\t
				    }\t
				}\t
				"""
		},
		"SUCCESS"
	);
}

public void test062a() {
	this.runConformTest(
		new String[] {
			"p1/X.java",
			"""
				package p1;\t
				public class X {\t
				    public static void main(String args[]) {\t
						final String s = "SUCCESS";
						final class Local {\t
							Local() {\t
								new LocalMember();\t
							}\t
							class LocalMember {\t
								{\t
									new LocalMemberMember();\t
								}\t
								class LocalMemberMember {\t
									{\t
										System.out.println(s);\t
									}\t
								}\t
							}\t
						}\t
						new Local();\t
				    }\t
				}\t
				"""
		},
		"SUCCESS"
	);
}

public void test063() {
	this.runConformTest(
		new String[] {
			"p1/X.java",
			"""
				package p1;\t
				public class X {\t
					String a = new Object(){\t
							class LocalMember {\t
								String s = "SUCCESS";\t
							};\t
							String get_a(){\t
								return new LocalMember().s;\t
							}\t
					}.get_a();\t
					public static void main(String argv[]) {\t
						System.out.println(new X().a);\t
					}\t
				}\t
				"""
		},
		"SUCCESS"
		);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=21447
 * should invoke Y.foo() at runtime
 */
public void test064(){
	this.runConformTest(
		new String[] {
			"p2/Y.java",
			"""
				package p2;\t
				public class Y extends T {\t
					public static void main(String argv[]) {\t
						new Y().bar();\t
					}\t
					protected void foo() {\t
						System.out.println("Y.foo()");\t
					}\t
				}\t
				""",
			"p1/X.java",
			"""
				package p1;\t
				public class X {\t
					protected void foo() {\t
						System.out.println("X.foo()");\t
					}\t
				}\t
				""",
			"p2/T.java",
			"""
				package p2;\t
				public class T extends p1.X {\t
					public void bar() {\t
						new Object(){\t
							void doSomething(){\t
								T.this.foo();\t
							}\t
						}.doSomething();\t
					}\t
				}\t
				""",
		},
		"Y.foo()"
		);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=21447
 * variation - if T.foo() is defined
 */
public void test065(){
	this.runConformTest(
		new String[] {
			"p2/Y.java",
			"""
				package p2;\t
				public class Y extends T {\t
					public static void main(String argv[]) {\t
						new Y().bar();\t
					}\t
					protected void foo() {\t
						System.out.println("Y.foo()");\t
					}\t
				}\t
				""",
			"p1/X.java",
			"""
				package p1;\t
				public class X {\t
					protected void foo() {\t
						System.out.println("X.foo()");\t
					}\t
				}\t
				""",
			"p2/T.java",
			"""
				package p2;\t
				public class T extends p1.X {\t
					public void bar() {\t
						new Object(){\t
							void doSomething(){\t
								T.this.foo();\t
							}\t
						}.doSomething();\t
					}\t
					protected void foo() {\t
						System.out.println("T.foo()");\t
					}\t
				}\t
				""",
		},
		"Y.foo()"
		);
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=21447
 * should invoke Y.foo() and X.foo() at runtime (through 2 separate access methods)
 */
public void test066(){
	this.runConformTest(
		new String[] {
			"p2/Y.java",
			"""
				package p2;\t
				public class Y extends T {\t
					public static void main(String argv[]) {\t
						new Y().bar();\t
					}\t
					protected void foo() {\t
						System.out.print("Y.foo()-");\t
					}\t
				}\t
				""",
			"p1/X.java",
			"""
				package p1;\t
				public class X {\t
					protected void foo() {\t
						System.out.println("X.foo()");\t
					}\t
				}\t
				""",
			"p2/T.java",
			"""
				package p2;\t
				public class T extends p1.X {\t
					public void bar() {\t
						new Object(){\t
							void doSomething(){\t
								T.this.foo();\t
								T.super.foo();	//need extra access method\s
							}\t
						}.doSomething();\t
					}\t
				}\t
				""",
		},
		"Y.foo()-X.foo()"
		);
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=22093
 * test collision check for synthetic accessors to constructors
 */
public void test067(){
	this.runConformTest(
		new String[] {
			"p/Test.java",
			"""
				package p;\t
				public class Test {\t
					class Member {\t
						private Member(){\t
						}\t
						private Member(Member other){\t
						}\t
					}\t
					public static void main(String[] arguments) {\t
						Test t = new Test();\t
						Member m1 = t.new Member();\t
						t.new Member(m1);\t
						System.out.println("SUCCESS");\t
					}\t
				}\t
				"""
		},
		"SUCCESS");
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=25229
public void test068(){
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {\t
					public static void main(String[] arguments) {\t
						new X().new X2().foo();\t
					}\t
					class X1 {\t
						void baz1() {\t
							System.out.print("-X1.baz1()");\t
						}\t
					}\t
					class X2 {\t
						void foo(){\t
							X.this.new X1(){\t
								void bar(){\t
									baz();\t
									baz1();\t
									baz2();\t
								}\t
							}.bar();\t
						}	\t
						void baz2() {\t
							System.out.println("-X2.baz2()");\t
						}\t
					}\t
					void baz() {\t
						System.out.print("X.baz()");\t
					}\t
				}\t
				"""
		},
		"X.baz()-X1.baz1()-X2.baz2()");
}

// http://bugs.eclipse.org/bugs/show_bug.cgi?id=26122
// synthetic outer local variables must be appended after user arguments
public void test069() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n"+
			"    X(String s, int j) {}	\n"+
			"    public static void main(String[] args) {	\n"+
			"        final int i;	\n"+
			"        new X(\"hello\", i = 1) {	\n"+ // val$i must be appended after i got assigned
			"            { 	\n"+
			"            	System.out.print(\"SUCCESS:\"+i); 	\n"+
			"            }	\n"+
			"        };	\n"+
			"    }	\n"+
			"}	\n"
		},
		"SUCCESS:1");
}
// variation on test069
public void test070() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n"+
			"    X() {}	\n"+
			"    public static void main(String[] args) {	\n"+
			"        final int i;	\n"+
			"        new X().new Y(\"hello\", i = 1) {	\n"+ // val$i must be appended after i got assigned
			"            { 	\n"+
			"            	System.out.print(\"SUCCESS:\"+i); 	\n"+
			"            }	\n"+
			"        };	\n"+
			"    }	\n"+
			"	class Y {	\n" +
			"		Y(String s, int j) {}	\n" +
			"	}	\n" +
			"}	\n"
		},
		"SUCCESS:1");
}

// test too many synthetic arguments
public void test071() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {\t
					void foo(int i) {\t
						final long v0 = i, v1 = i, v2 = i, v3 = i, v4 = i, v5 = i, v6 = i;\t
						final long v7 = i, v8 = i, v9 = i, v10 = i, v11 = i, v12 = i, v13 = i;\t
						final long v14 = i, v15 = i, v16 = i, v17 = i, v18 = i, v19 = i, v20 = i;\t
						final long v21 = i, v22 = i, v23 = i, v24 = i, v25 = i, v26 = i, v27 = i;\t
						final long v28 = i, v29 = i, v30 = i, v31 = i, v32 = i, v33 = i, v34 = i;\t
						final long v35 = i, v36 = i, v37 = i, v38 = i, v39 = i, v40 = i, v41 = i;\t
						final long v42 = i, v43 = i, v44 = i, v45 = i, v46 = i, v47 = i, v48 = i;\t
						final long v49 = i, v50 = i, v51 = i, v52 = i, v53 = i, v54 = i, v55 = i;\t
						final long v56 = i, v57 = i, v58 = i, v59 = i, v60 = i, v61 = i, v62 = i;\t
						final long v63 = i, v64 = i, v65 = i, v66 = i, v67 = i, v68 = i, v69 = i;\t
						final long v70 = i, v71 = i, v72 = i, v73 = i, v74 = i, v75 = i, v76 = i;\t
						final long v77 = i, v78 = i, v79 = i, v80 = i, v81 = i, v82 = i, v83 = i;\t
						final long v84 = i, v85 = i, v86 = i, v87 = i, v88 = i, v89 = i, v90 = i;\t
						final long v91 = i, v92 = i, v93 = i, v94 = i, v95 = i, v96 = i, v97 = i;\t
						final long v98 = i, v99 = i, v100 = i, v101 = i, v102 = i, v103 = i, v104 = i;\t
						final long v105 = i, v106 = i, v107 = i, v108 = i, v109 = i, v110 = i, v111 = i;\t
						final long v112 = i, v113 = i, v114 = i, v115 = i, v116 = i, v117 = i, v118 = i;\t
						final long v119 = i, v120 = i, v121 = i, v122 = i, v123 = i, v124 = i, v125 = i;\t
						final long v126 = i;											\t
						final int v127 = i;	// int is already too many arg				\t
						new X() {														\t
							{															\t
								System.out.println(										\t
									v0 + v1 + v2 + v3 + v4 + v5 + v6					\t
									+ v7 + v8 + v9 + v10 + v11 + v12 + v13				\t
									+ v14 + v15 + v16 + v17 + v18 + v19 + v20			\t
									+ v21 + v22 + v23 + v24 + v25 + v26 + v27			\t
									+ v28 + v29 + v30 + v31 + v32 + v33 + v34			\t
									+ v35 + v36 + v37 + v38 + v39 + v40 + v41			\t
									+ v42 + v43 + v44 + v45 + v46 + v47 + v48			\t
									+ v49 + v50 + v51 + v52 + v53 + v54 + v55			\t
									+ v56 + v57 + v58 + v59 + v60 + v61 + v62			\t
									+ v63 + v64 + v65 + v66 + v67 + v68 + v69			\t
									+ v70 + v71 + v72 + v73 + v74 + v75 + v76			\t
									+ v77 + v78 + v79 + v80 + v81 + v82 + v83			\t
									+ v84 + v85 + v86 + v87 + v88 + v89 + v90			\t
									+ v91 + v92 + v93 + v94 + v95 + v96 + v97			\t
									+ v98 + v99 + v100 + v101 + v102 + v103 + v104		\t
									+ v105 + v106 + v107 + v108 + v109 + v110 + v111	\t
									+ v112 + v113 + v114 + v115 + v116 + v117 + v118	\t
									+ v119 + v120 + v121 + v122 + v123 + v124 + v125	\t
									+ v126);											\t
							}\t
						};\t
					}\t
				}\t
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 23)
				new X() {														\t
				    ^^^
			Too many synthetic parameters, emulated parameter val$v126 is exceeding the limit of 255 words eligible for method parameters
			----------
			""",
		JavacTestOptions.SKIP /* javac simply does not catch this case */);
}

// test too many synthetic arguments
public void test072() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {\t
					void foo(int i) {\t
						final long v0 = i, v1 = i, v2 = i, v3 = i, v4 = i, v5 = i, v6 = i;\t
						final long v7 = i, v8 = i, v9 = i, v10 = i, v11 = i, v12 = i, v13 = i;\t
						final long v14 = i, v15 = i, v16 = i, v17 = i, v18 = i, v19 = i, v20 = i;\t
						final long v21 = i, v22 = i, v23 = i, v24 = i, v25 = i, v26 = i, v27 = i;\t
						final long v28 = i, v29 = i, v30 = i, v31 = i, v32 = i, v33 = i, v34 = i;\t
						final long v35 = i, v36 = i, v37 = i, v38 = i, v39 = i, v40 = i, v41 = i;\t
						final long v42 = i, v43 = i, v44 = i, v45 = i, v46 = i, v47 = i, v48 = i;\t
						final long v49 = i, v50 = i, v51 = i, v52 = i, v53 = i, v54 = i, v55 = i;\t
						final long v56 = i, v57 = i, v58 = i, v59 = i, v60 = i, v61 = i, v62 = i;\t
						final long v63 = i, v64 = i, v65 = i, v66 = i, v67 = i, v68 = i, v69 = i;\t
						final long v70 = i, v71 = i, v72 = i, v73 = i, v74 = i, v75 = i, v76 = i;\t
						final long v77 = i, v78 = i, v79 = i, v80 = i, v81 = i, v82 = i, v83 = i;\t
						final long v84 = i, v85 = i, v86 = i, v87 = i, v88 = i, v89 = i, v90 = i;\t
						final long v91 = i, v92 = i, v93 = i, v94 = i, v95 = i, v96 = i, v97 = i;\t
						final long v98 = i, v99 = i, v100 = i, v101 = i, v102 = i, v103 = i, v104 = i;\t
						final long v105 = i, v106 = i, v107 = i, v108 = i, v109 = i, v110 = i, v111 = i;\t
						final long v112 = i, v113 = i, v114 = i, v115 = i, v116 = i, v117 = i, v118 = i;\t
						final long v119 = i, v120 = i, v121 = i, v122 = i, v123 = i, v124 = i, v125 = i;\t
						new X() {														\t
							{															\t
								System.out.println(										\t
									v0 + v1 + v2 + v3 + v4 + v5 + v6					\t
									+ v7 + v8 + v9 + v10 + v11 + v12 + v13				\t
									+ v14 + v15 + v16 + v17 + v18 + v19 + v20			\t
									+ v21 + v22 + v23 + v24 + v25 + v26 + v27			\t
									+ v28 + v29 + v30 + v31 + v32 + v33 + v34			\t
									+ v35 + v36 + v37 + v38 + v39 + v40 + v41			\t
									+ v42 + v43 + v44 + v45 + v46 + v47 + v48			\t
									+ v49 + v50 + v51 + v52 + v53 + v54 + v55			\t
									+ v56 + v57 + v58 + v59 + v60 + v61 + v62			\t
									+ v63 + v64 + v65 + v66 + v67 + v68 + v69			\t
									+ v70 + v71 + v72 + v73 + v74 + v75 + v76			\t
									+ v77 + v78 + v79 + v80 + v81 + v82 + v83			\t
									+ v84 + v85 + v86 + v87 + v88 + v89 + v90			\t
									+ v91 + v92 + v93 + v94 + v95 + v96 + v97			\t
									+ v98 + v99 + v100 + v101 + v102 + v103 + v104		\t
									+ v105 + v106 + v107 + v108 + v109 + v110 + v111	\t
									+ v112 + v113 + v114 + v115 + v116 + v117 + v118	\t
									+ v119 + v120 + v121 + v122 + v123 + v124 + v125);	\t
							}\t
						};\t
					}\t
				    public static void main(String[] args) {\t
				       System.out.print("SUCCESS"); \t
				    }\t
				}\t
				"""
		},
		"SUCCESS");
}

/**
 * verify error in synthetic access to constructor
 * (ordering of parameters after moving outerlocals after user args)
 */
public void test073() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {\t
				    public static void main(final String[] args) {\t
						class Local {\t
							private Local(String str){\t
								Object o = args;\t
								System.out.println(str);\t
							}\t
						};\t
						new Local("SUCCESS");\t
					}\t
				}\t
				"""
	},
	"SUCCESS");
}

/**
 * verify error in synthetic access to constructor - test collisions
 * (ordering of parameters after moving outerlocals after user args)
 */
public void test074() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n" +
			"    public static void main(final String[] args) {	\n" +
			"		class Local {	\n" +
			"			public Local(String str, Local loc, String str2){	\n" + // collision
			"			}	\n" +
			"			public Local(String str, Local loc){	\n" + // collision
			"			}	\n" +
			"			private Local(String str){	\n" +
			"				Object o = args;	\n" +
			"				System.out.println(str);	\n" +
			"			}	\n" +
			"		};	\n" +
			"		new Local(\"SUCCESS\");	\n" +
			"	}	\n" +
			"}	\n"
	},
	"SUCCESS");
}

/**
 * should not consider synthetic methods on binary types
 */
public void test075() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {\t
				   public static void main(final String[] args) {\t
						System.out.println("SUCCESS");\t
					}\t
					private static void foo() {\t
					}\t
				    A a = new A();\t
				    class A {\t
						private A() {}\t
						A(String s) {\t
							foo();\t
						}\t
						A(int s) {\t
							foo();\t
						}\t
				    }\t
				    class B extends A {\t
				    	B(){\t
				    		super();\t
				    	}\t
				    }\t
				}\t
				"""
		},
		"SUCCESS");

	this.runNegativeTest(
		new String[] {
			"Y.java",
			"""
				public class Y {\t
					void foo(){\t
						new X().new A(null);\t
						new X().access$0();\t
					}\t
				}\t
				"""
		},
		"""
			----------
			1. ERROR in Y.java (at line 4)
				new X().access$0();\t
				        ^^^^^^^^
			The method access$0() is undefined for the type X
			----------
			""",
		null, // use default class-path
		false); // do not flush previous output dir content

}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=27413
 * implicit enclosing instances
 */
public void test076() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	if (options.sourceLevel <= ClassFileConstants.JDK1_3) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {	\n"+
				"	X(Object o){	\n"+
				"		class A { 	\n"+
				"			private A() {	\n"+ // implicit enclosing instance in non-static context
				"			}	\n"+
				"		}	\n"+
				"		class B extends X {	\n"+
				"			B() {	\n"+
				"				super(new A(){	\n"+
				"				});	\n"+
				"			}	\n"+
				"		}	\n"+
				"	}	\n"+
				"} 	\n"
			},
			"""
				----------
				1. ERROR in X.java (at line 9)
					super(new A(){\t
								});\t
					      ^^^^^^^^^^^^^^^
				No enclosing instance of type X is available due to some intermediate constructor invocation
				----------
				2. WARNING in X.java (at line 9)
					super(new A(){\t
					          ^^^
				Access to enclosing constructor A() is emulated by a synthetic accessor method
				----------
				""");
		return;
	}
	if (options.sourceLevel <= ClassFileConstants.JDK1_4) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {	\n"+
				"	X(Object o){	\n"+
				"		class A { 	\n"+
				"			private A() {	\n"+ // implicit enclosing instance in non-static context
				"			}	\n"+
				"		}	\n"+
				"		class B extends X {	\n"+
				"			B() {	\n"+
				"				super(new A(){	\n"+
				"				});	\n"+
				"			}	\n"+
				"		}	\n"+
				"	}	\n"+
				"} 	\n"
			},
			"""
				----------
				1. ERROR in X.java (at line 9)
					super(new A(){\t
								});\t
					      ^^^^^^^^^^^^^^^
				No enclosing instance of type X is available due to some intermediate constructor invocation
				----------
				""");
		return;
	}
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n"+
			"	X(Object o){	\n"+
			"		class A { 	\n"+
			"			private A() {	\n"+ // implicit enclosing instance in non-static context
			"			}	\n"+
			"		}	\n"+
			"		class B extends X {	\n"+
			"			B() {	\n"+
			"				super(new A(){	\n"+
			"				});	\n"+
			"			}	\n"+
			"		}	\n"+
			"	}	\n"+
			"} 	\n"
		},
		"");
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=27413
 * implicit enclosing instances
 */
public void test077() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n"+
			"	public static void main(String[] args){	\n"+
			"		System.out.println(\"SUCCESS\");	\n"+
			"	}	\n" +
			"	X(Object o){	\n"+
			"	}	\n" +
			"	static void foo() {	\n"+
			"		class A { 	\n"+ // no implicit enclosing in STATIC context
			"			private A() {	\n"+
			"			}	\n"+
			"		}	\n"+
			"		class B extends X {	\n"+
			"			B() {	\n"+
			"				super(new A(){	\n"+
			"				});	\n"+
			"			}	\n"+
			"		}	\n"+
			"	}	\n"+
			"} 	\n"
		},
		"SUCCESS");
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=27413
 * implicit enclosing instances
 */
public void test078() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	if (options.sourceLevel <= ClassFileConstants.JDK1_3) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {	\n"+
				"	X(Object o){	\n"+
				"		class A { 	\n"+
				"			private A() {	\n"+ // implicit enclosing instance in non-static context
				"			}	\n"+
				"		}	\n"+
				"		class B extends X {	\n"+
				"			B() {	\n"+
				"				super(new A(){	\n"+
				"					void foo() { System.out.println(X.this);	} \n"+
				"				});	\n"+
				"			}	\n"+
				"		}	\n"+
				"	}	\n"+
				"} 	\n"
			},
			"""
				----------
				1. ERROR in X.java (at line 9)
					super(new A(){\t
									void foo() { System.out.println(X.this);	}\s
								});\t
					      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				No enclosing instance of type X is available due to some intermediate constructor invocation
				----------
				2. WARNING in X.java (at line 9)
					super(new A(){\t
					          ^^^
				Access to enclosing constructor A() is emulated by a synthetic accessor method
				----------
				3. ERROR in X.java (at line 10)
					void foo() { System.out.println(X.this);	}\s
					                                ^^^^^^
				No enclosing instance of the type X is accessible in scope
				----------
				""");
		return;
	}
	if (options.sourceLevel <= ClassFileConstants.JDK1_4) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {	\n"+
				"	X(Object o){	\n"+
				"		class A { 	\n"+
				"			private A() {	\n"+ // implicit enclosing instance in non-static context
				"			}	\n"+
				"		}	\n"+
				"		class B extends X {	\n"+
				"			B() {	\n"+
				"				super(new A(){	\n"+
				"					void foo() { System.out.println(X.this);	} \n"+
				"				});	\n"+
				"			}	\n"+
				"		}	\n"+
				"	}	\n"+
				"} 	\n"
			},
			"""
				----------
				1. ERROR in X.java (at line 9)
					super(new A(){\t
									void foo() { System.out.println(X.this);	}\s
								});\t
					      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				No enclosing instance of type X is available due to some intermediate constructor invocation
				----------
				2. ERROR in X.java (at line 10)
					void foo() { System.out.println(X.this);	}\s
					                                ^^^^^^
				No enclosing instance of the type X is accessible in scope
				----------
				""");
		return;
	}
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {	\n"+
			"	X(Object o){	\n"+
			"		class A { 	\n"+
			"			private A() {	\n"+ // implicit enclosing instance in non-static context
			"			}	\n"+
			"		}	\n"+
			"		class B extends X {	\n"+
			"			B() {	\n"+
			"				super(new A(){	\n"+
			"					void foo() { System.out.println(X.this);	} \n"+
			"				});	\n"+
			"			}	\n"+
			"		}	\n"+
			"	}	\n"+
			"} 	\n"
		},
		"");
}

/*
 * Check that X.this is actually bound to an X, and not innermost compatible type (Z)
 */
public void test079() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {\t
					public static void main(String[] arguments) {\t
						new X().new Y().new Z().bar();\t
					}\t
					String foo() { return "X-foo"; }\t
					class Y extends X {\t
						String foo() { return "Y-foo"; }\t
						class Z extends Y {\t
							Z(){\t
								X.this.super();\t
							}\t
							String foo() { return "Z-foo"; }\t
							void bar () {\t
								System.out.println(X.this.foo());\t
							}\t
						}\t
					}\t
				}\t
				"""
		},
		"X-foo");
}

public void test080() { // verified as conform
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {\t
					public static void main(String[] arguments) {\s
						new X().new C();\t
					}\t
					int m() {\t
						System.out.println("SUCCESS");\t
						return 1;\t
					}\t
					class C extends B {\t
						C(int j){}\t
						C(){\t
							this(X.this.m());\t
						}\t
					}\t
				}\t
				class B extends X {\t
				}\t
				"""
		},
		"SUCCESS");
}

public void test081() {
	this.runConformTest(
		new String[] {
			"p/X.java",
			"package p;	\n"+
			"public class X {	\n"+
			"	public static void main(String[] arguments) { \n"+
			"		new X().new C();	\n"+
			"	}	\n"+
			"	int m() {	\n"+
			"		System.out.println(\"SUCCESS\");	\n"+
			"		return 1;	\n"+
			"	}	\n"+
			"	class C extends q.B {	\n"+
			"		C(int j){}	\n"+
			"		C(){	\n"+
			"			this(m());	\n"+ // only X.this can see m()
			"		}	\n"+
			"	}	\n"+
			"}	\n",
			"q/B.java",
			"""
				package q;\t
				public class B extends p.X {\t
				}\t
				"""
		},
		"SUCCESS");
}

/*
 * Default constructor for Z, will use enclosing 'this' as default-value for enclosing instance for super().
 */
public void test083() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {\t
					public static void main(String[] arguments) {\t
						new X().new Y().new Z().bar();\t
					}\t
					String foo() { return "X-foo"; }\t
					class Y extends X {\t
						String foo() { return "Y-foo"; }\t
						class Z {\t
							Z(){\t
								//X.this.super();\t
							}\t
							String foo() { return "Z-foo"; }\t
							void bar () {\t
								System.out.println(X.this.foo());\t
							}\t
						}\t
					}\t
				}\t
				"""
		},
		"X-foo");
}

public void test084() {
	this.runConformTest(
		new String[] {
			"Foo.java",
			"""
				public class Foo {\t
					public static void main(String[] arguments) {\t
						new Foo(null);\t
					}\t
					Foo(int i){}\t
					Foo(Object o){	\t
						class A { 	\t
							private A() {	 \t
							}	\t
						}	\t
						class B extends Foo {	\t
							B() {	\t
								super(0);\t
								new A(){	\t
									void foo() { \t
										System.out.println(Foo.this.getClass().getName());	\t
									} \t
								}.foo();	\t
							}	\t
						}	\t
						new B();\t
					}	\t
				} \t
				""",
		},
		"Foo");
}

public void test085() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	if (options.sourceLevel < ClassFileConstants.JDK1_5) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {\t
						int m() {	\t
							return 1;	\t
						}	\t
						class C extends B {	\t
							C(int j){}	\t
							C(){	\t
								this(\t
									new B(){ \t
										X x = X.this; \t
										int m(){\t
											return 1;\t
										}\t
									}.m());\t
							}	\t
						}	\t
					}	\t
					class B extends X {	\t
					}\t
					"""
			},
		"""
			----------
			1. ERROR in X.java (at line 10)
				X x = X.this; \t
				      ^^^^^^
			No enclosing instance of the type X is accessible in scope
			----------
			""");
		return;
	}
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {\t
					int m() {	\t
						return 1;	\t
					}	\t
					class C extends B {	\t
						C(int j){}	\t
						C(){	\t
							this(\t
								new B(){ \t
									X x = X.this; \t
									int m(){\t
										return 1;\t
									}\t
								}.m());\t
						}	\t
					}	\t
				}	\t
				class B extends X {	\t
				}\t
				"""
		},
		"");
}

public void test086() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {\t
					public static void main(String[] arguments) { \t
						new X().new C();	\t
					}	\t
					int m() {	\t
						return 1;	\t
					}	\t
					class C extends B {	\t
						C(int j){}	\t
						C(){	\t
							this(\t
								new B(){ \t
									int m(){\t
										System.out.println("SUCCESS");	\t
										return 1;\t
									}\t
								}.m());\t
						}	\t
					}	\t
				}	\t
				class B extends X {	\t
				}\t
				"""
		},
		"SUCCESS");
}

public void test087() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	if (options.complianceLevel <= ClassFileConstants.JDK1_4) {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					public class X {\t
						public static void main(String[] arguments) {\t
							new X().f();\t
						}\t
					    void f () {\t
					        class C {\t
					        	C() {\t
					        		System.out.println("["+X.this.getClass().getName()+"]");\t
					        	}\t
					        }\t
					        class N extends X {\t
					            { new C(); } // selects N.this, not O.this\t
					        }\t
					        new N();\t
					    }\t
					}\t
					"""
			},
			"[X$1$N]");
		return;
	}
	this.runConformTest(
		true,
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] arguments) {
						new X().f();
					}
				    void f () {
				        class C {
				        	C() {
				        		System.out.println("["+X.this.getClass().getName()+"]");\t
				        	}
				        }
				        class N extends X {
				            { new C(); } // selects N.this, not O.this\t
				        }
				        new N();
				    }\t
				}
				"""
		},
		"",
		"[X$1N]", // should be [X] indeed
		"",
		JavacTestOptions.EclipseHasABug.EclipseBug235809);
}

public void test088() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {\t
					class Middle {\t
						class Inner {\t
						}\t
					} \t
					class M extends Middle.Inner {\t
					}\t
				}\t
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 6)
				class M extends Middle.Inner {\t
				      ^
			No enclosing instance of type X.Middle is accessible to invoke the super constructor. Must define a constructor and explicitly qualify its super constructor invocation with an instance of X.Middle (e.g. x.super() where x is an instance of X.Middle).
			----------
			""");
}

public void test089() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {\t
					static class Middle {\t
						static class Inner {\t
						}\t
					} \t
					class M extends Middle.Inner {\t
					}\t
					public static void main(String[] arguments) {\t
						new X().new M();\t
						System.out.println("SUCCESS");\t
					}\t
				}\t
				"""
		},
		"SUCCESS");
}

public void test090() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {\t
					void foo() {\t
						class Middle {\t
							class Inner {\t
							}\t
						} \t
						class M extends Middle.Inner {\t
							M() {\t
								new Middle().super();\t
							}\t
						}\t
					}\t
					public static void main(String[] arguments) {\t
						new X().foo();\t
						System.out.println("SUCCESS");\t
					}\t
				}\t
				"""
		},
		"SUCCESS");
}

// ensure that local member empty constructor gets implicit constructor call
public void test091() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {\t
					Object o = new Object() {\t
						class Inner {\t
							private Inner() {}\t
						}\t
					}.new Inner(){};\t
					public static void main(String[] arguments) {\t
						new X();\t
						System.out.println("SUCCESS");\t
					}\t
				}\t
				""",
		},
		"SUCCESS");
}

/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=29636
 * ensure first anonymous is X$1(extends X), last is X$2(extends A)
 */
public void test092() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {\t
					class A {\t
					}\t
					public static void main(String[] arguments) {\t
						System.out.println("["+new X(){}.new A(){}.getClass().getName()+"]");\t
					}\t
				}\t
				""",
		},
		"[X$2]");
}

public void test093() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {\t
					public static void main(String[] arguments) {\t
						System.out.println(X.this);\t
					}\t
				}\t
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 3)
				System.out.println(X.this);\t
				                   ^^^^^^
			Cannot use this in a static context
			----------
			""");
}

public void test094() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {\t
					class Y {}\t
					public static void main(String[] arguments) {\t
						int i = 0;\t
						i.new Y();\t
					}\t
				}\t
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				i.new Y();\t
				^
			Cannot use an expression of the type int as a valid enclosing instance
			----------
			""");
}
//http://bugs.eclipse.org/bugs/show_bug.cgi?id=30280
public void test095() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {\t
					class Y {}\t
					public static void main(String[] arguments) {\t
						int i = 0;\t
						i.new Y(){};\t
					}\t
				}\t
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 5)
				i.new Y(){};\t
				^
			Cannot use an expression of the type int as a valid enclosing instance
			----------
			""");
}
public void test096() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {\t
					static class Y {}\t
					void foo() {\t
						new X().new Y(){};\t
					}\t
				}\t
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 4)
				new X().new Y(){};\t
				^^^^^^^
			Illegal enclosing instance specification for type X.Y
			----------
			""");
}
/**
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=35456
 */
public void test097() {
	this.runConformTest(
		new String[] {
			"apples/Base.java",
			"""
				package apples;\t
				public class Base {\t
					protected String foo = "SUCCESS";\t
					public static void main(String[] args){\t
						new oranges.Derived().new Inner(new oranges.Derived()).bug();\t
					}\t
				}\t
				""",
			"oranges/Derived.java",
			"""
				package oranges;\t
				import apples.*;\t
				public class Derived extends Base {\t
				    public class Inner {\t
				        Derived c = null;\t
				        public Inner(Derived c) {\t
				            this.c = c;\t
				        } \t
				        public void bug() {\t
				            // The following reference to Base.foo causes the \t
				            // VerifyError\t
				            System.out.println(c.foo);\t
				        }\t
				    }\t
				}\t
				""",
		},
		"SUCCESS");
}

/**
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=33751
 */
public void test098() {
	this.runConformTest(
		true,
		new String[] {
			"X.java",
			"""
				public class X {\t
					public static void main(String[] args) {\t
						System.out.println("first inner class = " + new Object() {}.getClass());\t
						if (true) {\t
							System.out.println("Always true");\t
						} else {\t
						System.out.println("unreachable inner class = " + new Object() {}.getClass());\t
						}\t
						System.out.println("last inner class = " + new Object() {}.getClass());\t
					}\t
				}\t
				""",
		},
		"""
			----------
			1. WARNING in X.java (at line 6)
				} else {\t
					System.out.println("unreachable inner class = " + new Object() {}.getClass());\t
					}\t
				       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			Dead code
			----------
			""",
		"""
			first inner class = class X$1
			Always true
			last inner class = class X$2""",
		"",
		JavacTestOptions.SKIP /* optimization that we chose deliberately */);
}

/**
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=40240
 */
public void test099() {
	this.runConformTest(
		new String[] {
			"Y.java",
			"""
				public class Y {\s
					public static void main(String[] args) {\t
						class Local {}\s
						System.out.println("SUCCESS");\t
					}\s
				}\s
				""",
		},
		"SUCCESS");

	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	if (options.complianceLevel <= ClassFileConstants.JDK1_4) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {\s
					    public static void main(String argv[]) {\s
							Object a = new Y$1$Local();        // compile-time error\s
					    }\s
					}\s
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 3)
					Object a = new Y$1$Local();        // compile-time error\s
					               ^^^^^^^^^
				The nested type Y$1$Local cannot be referenced using its binary name
				----------
				""",
			null,
			false);
		return;
	}
	this.runNegativeTest(
		false,
		new String[] {
			"X.java",
			"""
				public class X {\s
				    public static void main(String argv[]) {\s
						Object a = new Y$1Local();        // compile-time error\s
				    }\s
				}\s
				""",
		},
		null,
		null,
		"""
			----------
			1. ERROR in X.java (at line 3)
				Object a = new Y$1Local();        // compile-time error\s
				               ^^^^^^^^
			The nested type Y$1Local cannot be referenced using its binary name
			----------
			""",
		JavacTestOptions.JavacHasABug.JavacBug4094180);
}

/*
 * Check that member type allocation is granted access to compatible enclosing instance available as constructor argument
 */
public void test101() {
	this.runConformTest(
		new String[] {
			"X.java",
		"public class X {\n" +
		"	X(Object o) {\n" +
		"	}\n" +
		"	public static void main(String[] args) {\n" +
		"		new X(null).new M(null);\n" +
		"		System.out.println(\"SUCCESS\");\n" +
		"	}\n" +
		"	class M extends Top {\n" + // no issue if M is unrelated to X
		"		M() {\n" +
		"			super(null);\n" +
		"		}\n" +
		"		M(Object o) {\n" +
		"			super(new M(){});\n" +
		"		}\n" +
		"	}\n" +
		"	class Top {\n" +
		"		Top(Object o) {\n" +
		"		}\n" +
		"	}\n" +
		"}\n",
		},
		"SUCCESS");
}

/*
 * Check that direct member type allocation is denied access to compatible enclosing instance available as constructor argument
 */
public void test102() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					X(Object o) {
					}
					class M extends X {
						M() {
							super(null); //1
						}
						M(Object o) {
							super(new M());//2
						}
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				super(new M());//2
				      ^^^^^^^
			No enclosing instance of type X is available due to some intermediate constructor invocation
			----------
			""");
}


/*
 * Check that indirect member type allocation is denied access to compatible enclosing instance available as constructor argument
 */
public void test104() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					X(Object o) {
					}
					public static void main(String[] args) {
						new X(null).new M(null);
						System.out.println("SUCCESS");
					}
					class N extends X {
						N() {
							super(null); //1
						}
						N(Object o) {
							super(new M());//2
						}
					}
				 	class M extends X {
						M() {
							super(null); //3
						}
						M(Object o) {
							super(new M());//4
						}
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 13)
				super(new M());//2
				      ^^^^^^^
			No enclosing instance of type X is available due to some intermediate constructor invocation
			----------
			2. ERROR in X.java (at line 21)
				super(new M());//4
				      ^^^^^^^
			No enclosing instance of type X is available due to some intermediate constructor invocation
			----------
			""");
}

public void test107() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {\s
					public static class Y {\s
						public Y(Z z) {}\s
					}\s
					public interface Z {}\s
				}\s
				
				class A {\s
					private static class B extends X.Y implements X.Z {\s
						B(A a) {\s
							super(B.this);\s
						}\s
					}\s
				}\s""",
		},
		"""
			----------
			1. ERROR in X.java (at line 11)
				super(B.this);\s
				      ^^^^^^
			Cannot refer to 'this' nor 'super' while explicitly invoking a constructor
			----------
			""");
}

 // javac 1.4.2 incorrectly accepts it, jikes rejects it as we do
public void test108() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	if (options.sourceLevel == ClassFileConstants.JDK1_4) {	 // 1.3 and 1.5 both accept it
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						{
							class Local1 extends X {
							}
							class Local2 extends Local1 {
							}
						}
					}""",
			},
			"""
				----------
				1. ERROR in X.java (at line 5)
					class Local2 extends Local1 {
					      ^^^^^^
				No enclosing instance of type X is available due to some intermediate constructor invocation
				----------
				""");
		return;
	}
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					{
						class Local1 extends X {
						}
						class Local2 extends Local1 {
						}
					}
				}""",
		},
		"");
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44538
public void test109() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	if (options.sourceLevel == ClassFileConstants.JDK1_4) {		 // 1.3 and 1.5 both accept it
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public void bar() {
							class C extends X {
								public void foo() {
								\t
								}
							}
							X a= new X() {
								public void foo() {
								\t
								}
							};
							class D extends C {
							\t
							};
						}
					}"""
			},
			"""
				----------
				1. ERROR in X.java (at line 13)
					class D extends C {
					      ^
				No enclosing instance of type X is available due to some intermediate constructor invocation
				----------
				""");
		return;
	}
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public void bar() {
						class C extends X {
							public void foo() {
							\t
							}
						}
						X a= new X() {
							public void foo() {
							\t
							}
						};
						class D extends C {
						\t
						};
					}
				}"""
		},
		"");
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44715 - NPE when generating fake reachable local type
public void test110() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						if (true) {
							System.out.println("SUCCESS");
							return;
						}
						class ShouldNotBeGenerated {
						}
					}
				}"""
		},
		"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44715 - variation with anonymous type
public void test111() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						if (true) {
							System.out.println("SUCCESS");
							return;
						}
						new Object() {};\s
					}
				}"""
		},
		"SUCCESS");
}
public void test112() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    private Object t;
				    X(Object t) {
				        this.t = t;
				    }
				    public static void main(String[] args) {
				        new X("OUTER").bar();
				    }
				    void bar() {
				        new X(this) {
				            void run() {
				                new Object() {
				                    void run() {
								        System.out.println(t);
				                    }
				                }.run();
				            }
				        }.run();
				    }
				}
				"""
		},
		"OUTER");
}
public void test113() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    private Object t;
				    X(Object t) {
				        this.t = t;
				    }
				    public static void main(String[] args) {
				        new X("OUTER").bar();
				    }
				    void bar() {
				        new X(this) {
				            void run() {
				                new Object() {
				                    void run() {
										try {\t
											X x = (X) t;\t
								        } catch(ClassCastException e){\s
											System.out.println("SUCCESS");
										}\s
				                    }
				                }.run();
				            }
				        }.run();
				    }
				}
				"""
		},
		"SUCCESS");
}
public void test114() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					String s;
					X(String s) {
						this.s = s;
					}
					void foo() {
						class L extends X {
							L() {
								super(s);
								System.out.println(s);	\t
							}
						}
						new L();
					}
					public static void main(String[] args) {
						new X("SUCCESS").foo();	\t
					}
				}
				"""
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				super(s);
				      ^
			Cannot refer to an instance field s while explicitly invoking a constructor
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=58606
public void test115() {
	this.runConformTest(
		new String[] {
			"p2/X2.java",
			"""
				package p2;
				public class X2 extends p1.X1 {
				    private void foo() {
				        new p1.X1.M1() {
				            public void bar() {
				                System.out.print(X2.this.field);
				                X2.this.doit();
				            }
				        }.bar();
				    }
				    public static void main(String[] args) {
				        X2 t2 = new X2();
				        t2.foo();
				    }
				}""",
			"p1/X1.java",
			"""
				package p1;
				public class X1 {
				    public abstract class M1 {
				        public abstract void bar();
				    }
				    protected static String field = "SUCC";
				    protected static void doit() {
				        System.out.println("ESS");
				    }
				}""",
			},
		"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=68698
public void test116() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				class Display {
				  public interface Bla {
				    void a();
				  }
				}
				public class X {
				  void aMethod() {
				    Display display = null;
				    display.new Bla() {
				    };
				  }
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				display.new Bla() {
				^^^^^^^
			Illegal enclosing instance specification for type Display.Bla
			----------
			2. ERROR in X.java (at line 9)
				display.new Bla() {
				            ^^^^^
			The type new Display.Bla(){} must implement the inherited abstract method Display.Bla.a()
			----------
			""");
}

public void test117() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				\t
					public static void main(String[] args) {
						new X().bar();
					}
					void bar() {
						new X(){
							void baz() {
								new M();
							}
						}.baz();
					}
					class M {
						M() {
							System.out.println("SUCCESS");
						}
					}
				}
				""",
		},
		"SUCCESS");
}

public void test118() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void foo() {}
					class M {
						M(Object o) {}
						M() {
							this(new Object() {
								void baz() {
									foo();
									bar();
								}
							});
							new Object() {
								void baz() {
									foo();
									bar();
								}
							};
						}
						void bar() {}
						void baz() {
							new Object() {
								void baz() {
									foo();
									bar();
								}
							};
						}
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 9)
				bar();
				^^^
			Cannot refer to an instance method while explicitly invoking a constructor
			----------
			""");
}
public void test119() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	if (options.sourceLevel < ClassFileConstants.JDK1_5) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							new X().new M();
						}
						void foo(String s) { System.out.print("<foo:"+s+">"); }
						class M {
							M(Runnable r) { r.run(); }
							M() {
								this(new Runnable() {
									public void run() {
										foo("0");
										new Object() {
											void baz() {
					//							foo("1");
											}
										};
										class Local {
											void baz() {
					//							foo("2");
											}
										}			\t
										new Local();
									}
								});
								new Object() {
									void baz() {
										foo("3");
										bar("3");
									}
								}.baz();
							}
							void bar(String s) { System.out.print("<bar:"+s+">"); }
							void baz() {
								new Object() {
									void baz() {
										foo("4");
										bar("4");
									}
								};
							}
						}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 11)
					foo("0");
					^^^^^^^^
				No enclosing instance of the type X is accessible in scope
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
						new X().new M();
					}
					void foo(String s) { System.out.print("<foo:"+s+">"); }
					class M {
						M(Runnable r) { r.run(); }
						M() {
							this(new Runnable() {
								public void run() {
									foo("0");
									new Object() {
										void baz() {
				//							foo("1");
										}
									};
									class Local {
										void baz() {
				//							foo("2");
										}
									}			\t
									new Local();
								}
							});
							new Object() {
								void baz() {
									foo("3");
									bar("3");
								}
							}.baz();
						}
						void bar(String s) { System.out.print("<bar:"+s+">"); }
						void baz() {
							new Object() {
								void baz() {
									foo("4");
									bar("4");
								}
							};
						}
					}
				}
				""",
		},
		"<foo:0><foo:3><bar:3>");
}
public void test120() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	if (options.sourceLevel < ClassFileConstants.JDK1_5) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						void foo() {}
						class M {
							M(Object o) {}
							M() {
								this(new Object() {
									void baz() {
										new Object() {
											void baz() {
												foo(); //0
											}
										};
										class Local {
											void baz() {
												foo(); //1
											}
										}
										new Local();
										foo();//2
									}
								});
								new Object() {
									void baz() {
										foo();//3
										bar();
									}
								};
							}
							void bar() {}
							void baz() {
								new Object() {
									void baz() {
										foo();//4
										bar();
									}
								};
							}
						}
					}
					""",
			},
		"""
			----------
			1. ERROR in X.java (at line 10)
				foo(); //0
				^^^^^
			No enclosing instance of the type X is accessible in scope
			----------
			2. ERROR in X.java (at line 15)
				foo(); //1
				^^^^^
			No enclosing instance of the type X is accessible in scope
			----------
			3. ERROR in X.java (at line 19)
				foo();//2
				^^^^^
			No enclosing instance of the type X is accessible in scope
			----------
			""");
		return;
	}
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void foo() {}
					class M {
						M(Object o) {}
						M() {
							this(new Object() {
								void baz() {
									class Local {
										void baz() {
											foo(); //1
										}
									}
									new Local();
									foo();//2
								}
							});
							new Object() {
								void baz() {
									foo();//3
									bar();
								}
							};
						}
						void bar() {}
						void baz() {
							new Object() {
								void baz() {
									foo();//4
									bar();
								}
							};
						}
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 10)
				foo(); //1
				^^^^^
			No enclosing instance of the type X is accessible in scope
			----------
			""");
}
public void test121() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					X(Object o) {
					}
					class M extends X {
						M() {
							super(null); //1
						}
						M(Object o) {
							super(new X(null){});//2
						}
					}
				}
				""",
		},
		"");
}
public void _test122() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	if (options.sourceLevel < ClassFileConstants.JDK1_5) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public class X {
						public static void main(String[] args) {
							new X().new M();
						}
						void foo(String s) { System.out.print("<foo:"+s+">"); }
						class M {
							M(Runnable r) { r.run(); }
							M() {
								this(new Runnable() {
									{
										foo("0");
										bar("0");
									}
									public void run() {
									}
								});
							}
							void bar(String s) { System.out.print("<bar:"+s+">"); }
						}
					}
					""",
			},
			"""
				----------
				cannot access foo(0)
				----------
				2. ERROR in X.java (at line 12)
					bar("0");
					^^^
				Cannot refer to an instance method while explicitly invoking a constructor
				----------
				""");
		return;
	}
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String[] args) {
						new X().new M();
					}
					void foo(String s) { System.out.print("<foo:"+s+">"); }
					class M {
						M(Runnable r) { r.run(); }
						M() {
							this(new Runnable() {
								{
									foo("0");
									bar("0");
								}
								public void run() {
								}
							});
						}
						void bar(String s) { System.out.print("<bar:"+s+">"); }
					}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 12)
				bar("0");
				^^^
			Cannot refer to an instance method while explicitly invoking a constructor
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=110182 - variation
public void test123() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				class Y {
					public static final boolean b = false;
				}
				public class X {
				    private static Y y = new Y();\s
				    private static Object o = new Object();\s
				
					static class Z {
						Z() {
					    	if (y.b) {
					    		System.out.println("dead code");
					    	}
						}
						public int bar() {
					    	if (y.b) {
					    		System.out.println("dead code");
					    	}
				    		System.out.println("bar");
							return 0;
						}
					}
				    static int foo() {
				    	synchronized(o) {\s
					    	Z z = new Z();
				    		return z.bar();
				    	}
				    }
				   \s
				    public static void main(String[] args) {
				    	foo();
				    }
				}
				""",
		},
		"bar");
	String nestHost = "";
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	if (options.complianceLevel >= ClassFileConstants.JDK11) {
		nestHost = "\n" +
						  "Nest Host: #32 X\n";
	}
	// ensure synthetic access method got generated for enclosing field
	String expectedOutput =
		"  // Method descriptor #6 ()V\n" +
			"  // Stack: 1, Locals: 1\n" +
			"  X$Z();\n" +
			"    0  aload_0 [this]\n" +
			"    1  invokespecial java.lang.Object() [8]\n" +
			"    4  return\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 9]\n" +
			"        [pc: 4, line: 13]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 5] local: this index: 0 type: X.Z\n" +
			"  \n" +
			"  // Method descriptor #15 ()I\n" +
			"  // Stack: 2, Locals: 1\n" +
			"  public int bar();\n" +
			"     0  getstatic java.lang.System.out : java.io.PrintStream [16]\n" +
			"     3  ldc <String \"bar\"> [22]\n" +
			"     5  invokevirtual java.io.PrintStream.println(java.lang.String) : void [23]\n" +
			"     8  iconst_0\n" +
			"     9  ireturn\n" +
			"      Line numbers:\n" +
			"        [pc: 0, line: 18]\n" +
			"        [pc: 8, line: 19]\n" +
			"      Local variable table:\n" +
			"        [pc: 0, pc: 10] local: this index: 0 type: X.Z\n" +
			"\n" +
			"  Inner classes:\n" +
			"    [inner class info: #1 X$Z, outer class info: #32 X\n" +
			"     inner name: #34 Z, accessflags: 8 static]\n" +
			nestHost +
			"}";

	File f = new File(OUTPUT_DIR + File.separator + "X$Z.class");
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=77473
public void test124() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				    public static void main(String[] args) throws Exception {
				        Foo foo = new Foo();
				        try {
					        foo.frob(Baz.class);
				        	System.out.println("FAILED");
				        } catch(IllegalAccessException e){
				        	System.out.println("SUCCESS");
				        }
				    }
				    private static class Baz {
				    }
				}
				class Foo {
				    public void frob(Class cls) throws Exception {
				        Object o = cls.newInstance();
				    }
				}
				""",
		},
		"SUCCESS");
	// ensure synthetic access method got generated for enclosing field
	String expectedOutput =
		"""
		  // Method descriptor #6 ()V
		  // Stack: 1, Locals: 1
		  private X$Baz();
		    0  aload_0 [this]
		    1  invokespecial java.lang.Object() [8]
		    4  return
		      Line numbers:
		        [pc: 0, line: 11]
		      Local variable table:
		        [pc: 0, pc: 5] local: this index: 0 type: X.Baz
		""";

	File f = new File(OUTPUT_DIR + File.separator + "X$Baz.class");
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=77473 - variation
public void test125() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				\t
					void foo(final String s) {
						class Local {
							private Local() {}
								void bar() {
									System.out.println(s);
								}
						}
						new Local().bar();
					}
					public static void main(String[] args) {
						new X().foo("SUCCESS");
					}
				}
				""",
		},
		"SUCCESS");
	// check private constructor outcome (if >= 1.4 modifier change, if 1.3 synthetic emulation)
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	String expectedOutput = options.complianceLevel <= ClassFileConstants.JDK1_3
		? 	"""
			class X$1$Local {
			 \s
			  // Field descriptor #6 LX;
			  final synthetic X this$0;
			 \s
			  // Field descriptor #9 Ljava/lang/String;
			  private final synthetic java.lang.String val$s;
			 \s
			  // Method descriptor #11 (LX;Ljava/lang/String;)V
			  // Stack: 2, Locals: 3
			  private X$1$Local(X arg0, java.lang.String arg1);
			     0  aload_0 [this]
			     1  invokespecial java.lang.Object() [13]
			     4  aload_0 [this]
			     5  aload_1 [arg0]
			     6  putfield X$1$Local.this$0 : X [16]
			     9  aload_0 [this]
			    10  aload_2 [arg1]
			    11  putfield X$1$Local.val$s : java.lang.String [18]
			    14  return
			      Line numbers:
			        [pc: 0, line: 5]
			      Local variable table:
			        [pc: 0, pc: 15] local: this index: 0 type: new X(){}.Local
			 \s
			  // Method descriptor #15 ()V
			  // Stack: 2, Locals: 1
			  void bar();
			     0  getstatic java.lang.System.out : java.io.PrintStream [25]
			     3  aload_0 [this]
			     4  getfield X$1$Local.val$s : java.lang.String [18]
			     7  invokevirtual java.io.PrintStream.println(java.lang.String) : void [31]
			    10  return
			      Line numbers:
			        [pc: 0, line: 7]
			        [pc: 10, line: 8]
			      Local variable table:
			        [pc: 0, pc: 11] local: this index: 0 type: new X(){}.Local
			 \s
			  // Method descriptor #37 (LX;Ljava/lang/String;LX$1$Local;)V
			  // Stack: 3, Locals: 4
			  synthetic X$1$Local(X arg0, java.lang.String arg1, new X(){}.Local arg2);
			    0  aload_0 [this]
			    1  aload_1 [arg0]
			    2  aload_2 [arg1]
			    3  invokespecial X$1$Local(X, java.lang.String) [38]
			    6  return
			      Line numbers:
			        [pc: 0, line: 5]
			
			  Inner classes:
			    [inner class info: #1 X$1$Local, outer class info: #0
			     inner name: #43 Local, accessflags: 0 default]
			}"""
		: options.complianceLevel == ClassFileConstants.JDK1_4
			?  	"""
				class X$1$Local {
				 \s
				  // Field descriptor #6 LX;
				  final synthetic X this$0;
				 \s
				  // Field descriptor #9 Ljava/lang/String;
				  private final synthetic java.lang.String val$s;
				 \s
				  // Method descriptor #11 (LX;Ljava/lang/String;)V
				  // Stack: 2, Locals: 3
				  X$1$Local(X arg0, java.lang.String arg1);
				     0  aload_0 [this]
				     1  aload_1 [arg0]
				     2  putfield X$1$Local.this$0 : X [13]
				     5  aload_0 [this]
				     6  aload_2 [arg1]
				     7  putfield X$1$Local.val$s : java.lang.String [15]
				    10  aload_0 [this]
				    11  invokespecial java.lang.Object() [17]
				    14  return
				      Line numbers:
				        [pc: 0, line: 5]
				      Local variable table:
				        [pc: 0, pc: 15] local: this index: 0 type: new X(){}.Local
				 \s
				  // Method descriptor #19 ()V
				  // Stack: 2, Locals: 1
				  void bar();
				     0  getstatic java.lang.System.out : java.io.PrintStream [25]
				     3  aload_0 [this]
				     4  getfield X$1$Local.val$s : java.lang.String [15]
				     7  invokevirtual java.io.PrintStream.println(java.lang.String) : void [31]
				    10  return
				      Line numbers:
				        [pc: 0, line: 7]
				        [pc: 10, line: 8]
				      Local variable table:
				        [pc: 0, pc: 11] local: this index: 0 type: new X(){}.Local
				
				  Inner classes:
				    [inner class info: #1 X$1$Local, outer class info: #0
				     inner name: #40 Local, accessflags: 0 default]
				}"""
			:	"class X$1Local {\n" +
				"  \n" +
				"  // Field descriptor #6 LX;\n" +
				"  final synthetic X this$0;\n" +
				"  \n" +
				"  // Field descriptor #8 Ljava/lang/String;\n" +
				"  private final synthetic java.lang.String val$s;\n" +
				"  \n" +
				"  // Method descriptor #10 (LX;Ljava/lang/String;)V\n" +
				"  // Stack: 2, Locals: 3\n" +
				(isMinimumCompliant(ClassFileConstants.JDK11) ? "  private " :"  ") +
				"X$1Local(X arg0, java.lang.String arg1);\n" +
				"     0  aload_0 [this]\n" +
				"     1  aload_1 [arg0]\n" +
				"     2  putfield X$1Local.this$0 : X [12]\n" +
				"     5  aload_0 [this]\n" +
				"     6  aload_2 [arg1]\n" +
				"     7  putfield X$1Local.val$s : java.lang.String [14]\n" +
				"    10  aload_0 [this]\n" +
				"    11  invokespecial java.lang.Object() [16]\n" +
				"    14  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 5]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 15] local: this index: 0 type: new X(){}\n" +
				"  \n" +
				"  // Method descriptor #18 ()V\n" +
				"  // Stack: 2, Locals: 1\n" +
				"  void bar();\n" +
				"     0  getstatic java.lang.System.out : java.io.PrintStream [24]\n" +
				"     3  aload_0 [this]\n" +
				"     4  getfield X$1Local.val$s : java.lang.String [14]\n" +
				"     7  invokevirtual java.io.PrintStream.println(java.lang.String) : void [30]\n" +
				"    10  return\n" +
				"      Line numbers:\n" +
				"        [pc: 0, line: 7]\n" +
				"        [pc: 10, line: 8]\n" +
				"      Local variable table:\n" +
				"        [pc: 0, pc: 11] local: this index: 0 type: new X(){}\n" +
				"\n" +
				"  Inner classes:\n" +
				"    [inner class info: #1 X$1Local, outer class info: #0\n" +
				"     inner name: #44 Local, accessflags: 0 default]\n" +
				(isMinimumCompliant(ClassFileConstants.JDK11) ?
				"  Enclosing Method: #39  #41 X.foo(Ljava/lang/String;)V\n" +
				"\n" +
				"Nest Host: #39 X\n" : "");

	File f = new File(OUTPUT_DIR + File.separator + (options.complianceLevel >= ClassFileConstants.JDK1_5 ? "X$1Local.class" : "X$1$Local.class"));
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130117
public void test126() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	if (options.sourceLevel < ClassFileConstants.JDK1_5) {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					public interface X {
					        private class Inner {}
					        private interface IInner {}
					}
					""",
			},
			"""
				----------
				1. ERROR in X.java (at line 2)
					private class Inner {}
					              ^^^^^
				The interface member type Inner can only be public
				----------
				2. ERROR in X.java (at line 3)
					private interface IInner {}
					                  ^^^^^^
				The interface member type IInner can only be public
				----------
				""");
		return;
	}
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public interface X {
				        private class Inner {}
				        private interface IInner {}
				        private enum EInner {}
				        private @interface AInner {}
				}
				""",
		},
		"""
			----------
			1. ERROR in X.java (at line 2)
				private class Inner {}
				              ^^^^^
			The interface member type Inner can only be public
			----------
			2. ERROR in X.java (at line 3)
				private interface IInner {}
				                  ^^^^^^
			The interface member type IInner can only be public
			----------
			3. ERROR in X.java (at line 4)
				private enum EInner {}
				             ^^^^^^
			The interface member type EInner can only be public
			----------
			4. ERROR in X.java (at line 5)
				private @interface AInner {}
				                   ^^^^^^
			The interface member type AInner can only be public
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=89347
public void test127() {
	this.runConformTest(
		new String[] {
			"p/BugContainer.java",
			"""
				package p;
				
				public abstract class BugContainer {
				        protected static class InternalInfo$ {
				                public InternalInfo$() {}
				        }
				        abstract protected InternalInfo$ getInfo();
				}
				""", // =================
		},
		"");
	this.runConformTest(
		new String[] {
				"q/BugUser.java", // =================
				"""
					package q;
					
					import p.BugContainer;
					
					public class BugUser extends BugContainer{
					        protected InternalInfo$ getInfo() {
					                return new InternalInfo$();
					        }
					}""", // =================
			},
		"",
		null,
		false,
		null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=89347 - variation
public void test128() {
	this.runConformTest(
		new String[] {
			"p/BugContainer.java",
			"""
				package p;
				
				public abstract class BugContainer {
				        protected static class InternalInfo$ {
				                public InternalInfo$() {}
				        }
				        abstract protected InternalInfo$ getInfo();
				}
				""", // =================
			"q/BugUser.java", // =================
			"""
				package q;
				
				import p.BugContainer;
				
				public class BugUser extends BugContainer{
				        protected InternalInfo$ getInfo() {
				                return new InternalInfo$();
				        }
				}""", // =================
		},
		"");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=160132 - variation
public void test129() {
	this.runConformTest(
		new String[] {
			"X.java", //========================
			"""
				public interface X {
				  interface Entry {
				    interface Internal extends Entry {
				      Internal createEntry();
				    }
				  }
				}
				""", //========================
			"Y.java",
			"""
				public class Y implements X.Entry.Internal {
				  public Internal createEntry() {
				    return null;
				  }
				}
				""" , //========================
		},
		"");
	// compile Y against X binary
	this.runConformTest(
			new String[] {
				"Y.java", //========================
				"""
					public class Y implements X.Entry.Internal {
					  public Internal createEntry() {
					    return null;
					  }
					}
					""" , //========================
			},
			"",
			null,
			false,
			null);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=164497
public void test130() {
	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	if (options.sourceLevel <= ClassFileConstants.JDK1_3) {
    	runConformTest(
   			true /* flush output directory */,
    		new String[] { /* test files */
    			"X.java", //========================
    			"""
					public class X {
					    public static void main(String[] args) {
					    	new M().foo2();
					    }
					}
					class M  {
						String name;
					\t
						M() {
							this.name = "SUCCESS";
						}
					
						private class Y extends N {
							private Y() {
								super();
							}
							protected void foo(Z z) {
								z.bar(new A());
							}
						}
					\t
					    public class A implements I {
					    	public void configure() {
					    		new B().foo();
					    	}
					    	public class B {
					            public void foo() {
									try {
					                System.out.println(M.this.name);
									} catch(NullPointerException e) {
										System.err.println("NPE THROWN");
									}
					            }
					        }
					    }
					   \s
					    public void foo2() {
					    	new Y();
					    }
					}
					class Z {
						void bar(I i) {
							i.configure();
						}
					}
					
					interface I {
						void configure();
					}
					
					class N {
						protected void foo(Z z) {
						}
						N() {
							this.foo(new Z());
						}
					}
					"""
    		},
			null /* do not check compiler log */,
			"" /* expected output string */,
			"NPE THROWN" /* expected error string */,
			JavacTestOptions.DEFAULT /* default javac test options */);
    	return;
	}
	this.runConformTest(
    		new String[] {
    			"X.java", //========================
    			"""
					public class X {
					    public static void main(String[] args) {
					    	new M().foo2();
					    }
					}
					class M  {
						String name;
					\t
						M() {
							this.name = "SUCCESS";
						}
					
						private class Y extends N {
							private Y() {
								super();
							}
							protected void foo(Z z) {
								z.bar(new A());
							}
						}
					\t
					    public class A implements I {
					    	public void configure() {
					    		new B().foo();
					    	}
					    	public class B {
					            public void foo() {
					                System.out.println(M.this.name);
					            }
					        }
					    }
					   \s
					    public void foo2() {
					    	new Y();
					    }
					}
					class Z {
						void bar(I i) {
							i.configure();
						}
					}
					
					interface I {
						void configure();
					}
					
					class N {
						protected void foo(Z z) {
						}
						N() {
							this.foo(new Z());
						}
					}
					"""
    		},
    		"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=165662
public void test131() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void foo() {
						class Local {
							void foo() {
							}
						}
						{
							class Local {
								Local(int i) {
									this.init(i);
									this.bar(); // should detect error
								}
								void init(int i) {
								}
							}
							Local l = new Local(0); // should be fine
						}
						Local l = new Local();
						l.foo();
					}
				}""", // =================,
		},
		"""
			----------
			1. ERROR in X.java (at line 8)
				class Local {
				      ^^^^^
			Duplicate nested type Local
			----------
			2. ERROR in X.java (at line 11)
				this.bar(); // should detect error
				     ^^^
			The method bar() is undefined for the type Local
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=165662
public void test132() {
	this.runConformTest(
		true,
		new String[] {
			"X.java",
			"""
				public class X {
					public static void main(String argv[]) {
						class Local {}
						class Foo {
							void foo() {
								class Local {}
							}
						}
					}
				}""", // =================
		},
		"""
			----------
			1. WARNING in X.java (at line 6)
				class Local {}
				      ^^^^^
			The type Local is hiding the type Local
			----------
			""",
		"",
		"",
		JavacTestOptions.Excuse.EclipseHasSomeMoreWarnings);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=168331
public void test133() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		this.runConformTest(
			new String[] {
				"X.java",	//===================
				"""
					public class X {
					  public static interface I {
					  }
					  public static interface IE extends I {
					  }
					  public static interface J {
					    I getI(int i);
					  }
					  public static interface JE extends J {
					    IE getI(int i);
					  }
					  public static class Y implements JE {
					    public IE getI(int i) {
					      return null;
					    }
					  }
					  private J j = new Y();
					  public void foo() {
					    j.getI(0);
					    System.out.println("SUCCESS");
					  }
					  public static void main(String[] args) {
					    new X().foo();
					  }
					}""", 		// =================
			},
			"SUCCESS");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=168331
public void test134() {
	if (this.complianceLevel >= ClassFileConstants.JDK1_5) {
		this.runConformTest(
			new String[] {
				"X.java",	//===================
				"""
					public class X {
					  public interface I {
					    public String foo();
					  }
					  public interface J {
					    public I getI();
					  }
					  public static class XI implements I {
					    public String foo() {
					      return "XI";
					    }
					  }
					  public interface K extends J {
					    public XI getI();
					  }
					  public static abstract class XK implements K {
					    public XI getI() {
					      return new XI();
					    }
					  }
					  public static class Y extends XK {
					  }
					  public static void main(String[] args) {
					    K k = new Y();
					    System.out.println(k.getI().foo());
					    J j = k;
					    System.out.println(j.getI().foo());
					  }
					}""", 		// =================
			},
			"XI\nXI");
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=152961
public void test135() {
	this.runNegativeTest(
		new String[] {
			"p/X.java",
			"""
				package p;
				import p.A;
				public class X {
				\t
					void foo(Object o, Object [] os) {
						A.M2.MM1 mm1 = (A.M2.MM1) o;
						A.M2.MM1[] mm1s = (A.M2.MM1[]) os;
					}
					private interface Outer {
						interface Inner {
							String variable = "my testing";
						}
					}
					public static void main(String[] args) {
						System.out.println(Outer.Inner.variable);
						Zork z;
					}
				}""", // =================,
			"p/A.java",
			"""
				package p;
				/** @deprecated */
				public class A {
					public class M1 {
						public class MM1 {
						}
					}
					public class M2 extends M1 {
					}
				}""", // =================,
		},
		"""
			----------
			1. WARNING in p\\X.java (at line 2)
				import p.A;
				       ^^^
			The type A is deprecated
			----------
			2. WARNING in p\\X.java (at line 6)
				A.M2.MM1 mm1 = (A.M2.MM1) o;
				^
			The type A is deprecated
			----------
			3. WARNING in p\\X.java (at line 6)
				A.M2.MM1 mm1 = (A.M2.MM1) o;
				  ^^
			The type A.M2 is deprecated
			----------
			4. WARNING in p\\X.java (at line 6)
				A.M2.MM1 mm1 = (A.M2.MM1) o;
				     ^^^
			The type A.M1.MM1 is deprecated
			----------
			5. WARNING in p\\X.java (at line 6)
				A.M2.MM1 mm1 = (A.M2.MM1) o;
				                ^
			The type A is deprecated
			----------
			6. WARNING in p\\X.java (at line 6)
				A.M2.MM1 mm1 = (A.M2.MM1) o;
				                  ^^
			The type A.M2 is deprecated
			----------
			7. WARNING in p\\X.java (at line 6)
				A.M2.MM1 mm1 = (A.M2.MM1) o;
				                     ^^^
			The type A.M1.MM1 is deprecated
			----------
			8. WARNING in p\\X.java (at line 7)
				A.M2.MM1[] mm1s = (A.M2.MM1[]) os;
				^
			The type A is deprecated
			----------
			9. WARNING in p\\X.java (at line 7)
				A.M2.MM1[] mm1s = (A.M2.MM1[]) os;
				  ^^
			The type A.M2 is deprecated
			----------
			10. WARNING in p\\X.java (at line 7)
				A.M2.MM1[] mm1s = (A.M2.MM1[]) os;
				     ^^^
			The type A.M1.MM1 is deprecated
			----------
			11. WARNING in p\\X.java (at line 7)
				A.M2.MM1[] mm1s = (A.M2.MM1[]) os;
				                   ^
			The type A is deprecated
			----------
			12. WARNING in p\\X.java (at line 7)
				A.M2.MM1[] mm1s = (A.M2.MM1[]) os;
				                     ^^
			The type A.M2 is deprecated
			----------
			13. WARNING in p\\X.java (at line 7)
				A.M2.MM1[] mm1s = (A.M2.MM1[]) os;
				                        ^^^
			The type A.M1.MM1 is deprecated
			----------
			14. ERROR in p\\X.java (at line 16)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=152961 - variation
public void test136() {
	this.runNegativeTest(
		new String[] {
			"p/X.java",
			"""
				package p;
				public class X {
					private interface Outer {
						interface Inner {
							String variable = "my testing";
						}
					}
					public static void main(String[] args) {
						Outer.Inner variable = null;
						System.out.println(variable);
						Zork z;
					}
				}""", // =================,
		},
		"""
			----------
			1. ERROR in p\\X.java (at line 11)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=152961 - variation
public void test137() {
	this.runNegativeTest(
		new String[] {
			"p/X.java",
			"""
				package p;
				public class X {
					private interface Outer {
						interface Inner {
							String variable = "my testing";
						}
					}
					private interface Outer2 extends Outer {
					}
					public static void main(String[] args) {
						System.out.println(Outer2.Inner.variable);
						Zork z;
					}
				}""", // =================,
		},
		"""
			----------
			1. ERROR in p\\X.java (at line 12)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=152961 - variation
public void test138() {
	this.runNegativeTest(
		new String[] {
			"p/X.java",
			"""
				public class X {
					private interface Outer {
						interface Inner {
							String variable = "my testing";
						}
					}
					private interface Outer2 extends Outer {
					}
					public static void main(String[] args) {
						Outer2.Inner variable = null;
						System.out.println(variable);
						Zork z;
					}
				}""", // =================,
		},
		"""
			----------
			1. ERROR in p\\X.java (at line 12)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=152961 - variation
public void test139() {
	String errMessage = isMinimumCompliant(ClassFileConstants.JDK11) ?
			"""
				----------
				1. ERROR in X.java (at line 9)
					class Y extends Zork {}
					                ^^^^
				Zork cannot be resolved to a type
				----------
				"""
			:
			"""
				----------
				1. WARNING in X.java (at line 5)
					private class Y extends A {
					              ^
				Access to enclosing constructor X.A() is emulated by a synthetic accessor method
				----------
				2. ERROR in X.java (at line 9)
					class Y extends Zork {}
					                ^^^^
				Zork cannot be resolved to a type
				----------
				""";
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {\s
				   private class A {
				    class B {}
				  }
				  private class Y extends A {
				  }
				  Y.B d = null;
				}
				class Y extends Zork {}
				""", // =================
		},
		errMessage);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=171184
public void test140() throws Exception {
	this.runConformTest(new String[] {
		"p/A.java",
		"""
			package p;
			public class A {
				public static interface I {
					void foo();
				}
			}""",
		"p1/X.java",
		"""
			package p1;
			import p.A;
			public class X implements A.I {
			        public void foo() { /* dummy */ }
			}"""
	});
	String expectedOutput =
		"""
		  Inner classes:
		    [inner class info: #5 p/A$I, outer class info: #20 p/A
		     inner name: #22 I, accessflags: 1545 public abstract static]
		""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "p1" + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=171184
public void test141() throws Exception {
	this.runConformTest(new String[] {
		"p/A.java",
		"""
			package p;
			public class A {
				public static class B {
					void foo() { /* dummy */ }
				}
			}""",
		"p1/X.java",
		"""
			package p1;
			import p.A;
			public class X extends A.B {
			}"""
	});
	String expectedOutput =
		"""
		  Inner classes:
		    [inner class info: #3 p/A$B, outer class info: #17 p/A
		     inner name: #19 B, accessflags: 9 public static]
		""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "p1" + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=171184
public void test142() throws Exception {
	this.runConformTest(new String[] {
		"p/A.java",
		"""
			package p;
			public class A {
				public class B {
					void foo() { /* dummy */ }
				}
			}""",
		"p1/X.java",
		"""
			package p1;
			import p.A;
			public class X {
				Object foo() {
					return new A().new B();
				}
			}"""
	});
	String expectedOutput =
		"""
		  Inner classes:
		    [inner class info: #16 p/A$B, outer class info: #18 p/A
		     inner name: #31 B, accessflags: 1 public]
		""";
	if (new CompilerOptions(getCompilerOptions()).targetJDK == ClassFileConstants.JDK1_1) {
		expectedOutput =
			"""
				  Inner classes:
				    [inner class info: #16 p/A$B, outer class info: #18 p/A
				     inner name: #27 B, accessflags: 1 public]
				""";
	}
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "p1" + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=171184
public void test143() throws Exception {
	this.runConformTest(new String[] {
		"X.java",
		"""
			class A {
				public class B {
					void foo() { /* dummy */ }
				}
			}
			public class X {
				Object foo() {
					return A.B.class;
				}
			}"""
	});
	if (new CompilerOptions(getCompilerOptions()).targetJDK >= ClassFileConstants.JDK1_5) {
		String expectedOutput =
			"""
			  Inner classes:
			    [inner class info: #16 A$B, outer class info: #21 A
			     inner name: #23 B, accessflags: 1 public]
			""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=171184
public void test144() throws Exception {
	this.runConformTest(new String[] {
		"X.java",
		"""
			class A {
				public static class B {
					public static int CONST = 0;
				}
			}
			public class X {
				int foo() {
					return A.B.CONST;
				}
			}"""
	});
	String expectedOutput =
		"""
		  Inner classes:
		    [inner class info: #17 A$B, outer class info: #25 A
		     inner name: #27 B, accessflags: 9 public static]
		""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=171184
public void test145() throws Exception {
	this.runConformTest(new String[] {
		"X.java",
		"""
			class A {
				public static class B {
				}
			}
			public class X {
				A.B field;
			}"""
	});
	String expectedOutput =
		"""
		  Inner classes:
		    [inner class info: #19 A$B, outer class info: #21 A
		     inner name: #23 B, accessflags: 9 public static]
		""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=171184
public void test146() throws Exception {
	this.runConformTest(new String[] {
		"X.java",
		"""
			class A {
				public static class B {
				}
			}
			public class X {
				int foo(A.B o) {
					return 0;
				}
			}"""
	});
	String expectedOutput =
		"""
		  Inner classes:
		    [inner class info: #21 A$B, outer class info: #23 A
		     inner name: #25 B, accessflags: 9 public static]
		""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=171184
public void test147() throws Exception {
	this.runConformTest(new String[] {
		"X.java",
		"""
			class A {
				public static class B {
				}
			}
			public class X {
				A.B foo() {
					return null;
				}
			}"""
	});
	String expectedOutput =
		"""
		  Inner classes:
		    [inner class info: #19 A$B, outer class info: #21 A
		     inner name: #23 B, accessflags: 9 public static]
		""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=171184
public void test148() throws Exception {
	this.runConformTest(new String[] {
		"X.java",
		"""
			class A {
				public static class B extends Exception {
				}
			}
			public class X {
				void foo() throws A.B{
				}
			}"""
	});
	String expectedOutput =
		"""
		  Inner classes:
		    [inner class info: #16 A$B, outer class info: #21 A
		     inner name: #23 B, accessflags: 9 public static]
		""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=171749
public void test149() throws Exception {
	this.runConformTest(new String[] {
		"X.java",
		"""
			public final class X implements A.Foo1 {
			        public void foo() {}
			        public A.Foo2 foo2() {   return null; }
			        public void foo3( A.Foo3 foo ) {}
			        public void foo4() { A.Foo4 foo = null; }
			        public void foo5() {
			                new A.Foo5() {
			                        public void foo() {}
			                }.foo();
			        }
			        public static class Foo6 implements A.Foo6 {
			                public void foo() {}
			        }
			        public void foo7() { Bar2.foo7().foo(); }
			}""",
		"A.java",
		"""
			class A {
			        public static interface Foo1 { void foo(); }
			        public static interface Foo2 { void foo(); }
			        public static interface Foo3 { void foo(); }
			        public static interface Foo4 { void foo(); }
			        public static interface Foo5 { void foo(); }
			        public static interface Foo6 { void foo(); }
			        public static interface Foo7 { void foo(); }
			}""",
		"Bar2.java",
		"""
			class Bar2 {
			        public static A.Foo7 foo7() { return null; }
			}"""
	});
	String expectedOutput =
		"""
		  Inner classes:
		    [inner class info: #5 A$Foo1, outer class info: #44 A
		     inner name: #46 Foo1, accessflags: 1545 public abstract static],
		    [inner class info: #47 A$Foo2, outer class info: #44 A
		     inner name: #49 Foo2, accessflags: 1545 public abstract static],
		    [inner class info: #50 A$Foo3, outer class info: #44 A
		     inner name: #52 Foo3, accessflags: 1545 public abstract static],
		    [inner class info: #39 A$Foo7, outer class info: #44 A
		     inner name: #53 Foo7, accessflags: 1545 public abstract static],
		    [inner class info: #25 X$1, outer class info: #0
		     inner name: #0, accessflags: 0 default],
		    [inner class info: #54 X$Foo6, outer class info: #1 X
		     inner name: #56 Foo6, accessflags: 9 public static]
		""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=210422
public void test150() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.io.Serializable;
					
					public final class X implements Serializable {
					
					        void bar() {}
					
					        interface IM {}
					        class SMember extends String {}
					
					        class Member extends X { \s
					                ZorkMember z;
					                void foo() {
					                        this.bar();
					                        Zork1 z;
					                }\s
					        }
					
					        void foo() {
					                new X().new IM();
					                class Local extends X {\s
					                        ZorkLocal z;
					                        void foo() {
					                                this.bar();
					                                Zork3 z;
					                        }
					                }
					                new X() {
					                        ZorkAnonymous2 z;                      \s
					                        void foo() {
					                                this.bar();
					                                Zork4 z;
					                        }
					                };
					        }
					}
					"""
			},
			"""
				----------
				1. WARNING in X.java (at line 3)
					public final class X implements Serializable {
					                   ^
				The serializable class X does not declare a static final serialVersionUID field of type long
				----------
				2. ERROR in X.java (at line 8)
					class SMember extends String {}
					                      ^^^^^^
				The type SMember cannot subclass the final class String
				----------
				3. ERROR in X.java (at line 10)
					class Member extends X { \s
					                     ^
				The type Member cannot subclass the final class X
				----------
				4. ERROR in X.java (at line 11)
					ZorkMember z;
					^^^^^^^^^^
				ZorkMember cannot be resolved to a type
				----------
				5. ERROR in X.java (at line 13)
					this.bar();
					     ^^^
				The method bar() is undefined for the type X.Member
				----------
				6. ERROR in X.java (at line 14)
					Zork1 z;
					^^^^^
				Zork1 cannot be resolved to a type
				----------
				7. WARNING in X.java (at line 14)
					Zork1 z;
					      ^
				The local variable z is hiding a field from type X.Member
				----------
				8. ERROR in X.java (at line 19)
					new X().new IM();
					            ^^
				Cannot instantiate the type X.IM
				----------
				9. ERROR in X.java (at line 20)
					class Local extends X {\s
					                    ^
				The type Local cannot subclass the final class X
				----------
				10. ERROR in X.java (at line 21)
					ZorkLocal z;
					^^^^^^^^^
				ZorkLocal cannot be resolved to a type
				----------
				11. ERROR in X.java (at line 23)
					this.bar();
					     ^^^
				The method bar() is undefined for the type Local
				----------
				12. ERROR in X.java (at line 24)
					Zork3 z;
					^^^^^
				Zork3 cannot be resolved to a type
				----------
				13. WARNING in X.java (at line 24)
					Zork3 z;
					      ^
				The local variable z is hiding a field from type Local
				----------
				14. ERROR in X.java (at line 27)
					new X() {
					    ^
				An anonymous class cannot subclass the final class X
				----------
				15. ERROR in X.java (at line 28)
					ZorkAnonymous2 z;                      \s
					^^^^^^^^^^^^^^
				ZorkAnonymous2 cannot be resolved to a type
				----------
				16. ERROR in X.java (at line 30)
					this.bar();
					     ^^^
				The method bar() is undefined for the type new X(){}
				----------
				17. ERROR in X.java (at line 31)
					Zork4 z;
					^^^^^
				Zork4 cannot be resolved to a type
				----------
				18. WARNING in X.java (at line 31)
					Zork4 z;
					      ^
				The local variable z is hiding a field from type new X(){}
				----------
				""");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=216683
public void test151() {
	long compliance = new CompilerOptions(getCompilerOptions()).complianceLevel;
	if (compliance <= ClassFileConstants.JDK1_3) {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						
						    public static interface Foo { }
						    public static interface Bar { }
						
						    private static class B2F extends X { }
						    private static class F2B extends X { }
						
						    public static abstract class Key {
						
						        public abstract Key flip();
						
						        private static class B2F extends Key {
						            private static B2F create() { return new B2F(); }
						            public Key flip() { return F2B.create(); }
						        }
						
						        private static class F2B extends Key {
						            private static F2B create() { return new F2B(); }
						            public Key flip() { return B2F.create(); }
						        }
						    }
						}""", // =================
				},
				"""
					----------
					1. ERROR in X.java (at line 15)
						public Key flip() { return F2B.create(); }
						                           ^^^
					The type F2B is defined in an inherited type and an enclosing scope
					----------
					2. ERROR in X.java (at line 20)
						public Key flip() { return B2F.create(); }
						                           ^^^
					The type B2F is defined in an inherited type and an enclosing scope
					----------
					""");
	} else if (compliance == ClassFileConstants.JDK1_4) {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						
						    public static interface Foo { }
						    public static interface Bar { }
						
						    private static class B2F extends X { }
						    private static class F2B extends X { }
						
						    public static abstract class Key {
						
						        public abstract Key flip();
						
						        private static class B2F extends Key {
						            private static B2F create() { return new B2F(); }
						            public Key flip() { return F2B.create(); }
						        }
						
						        private static class F2B extends Key {
						            private static F2B create() { return new F2B(); }
						            public Key flip() { return B2F.create(); }
						        }
						    }
						}""", // =================

				},
				"");
	} else {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X<U, V> {
						
						    public static interface Foo { }
						    public static interface Bar { }
						
						    private static class B2F extends X<Bar, Foo> { }
						    private static class F2B extends X<Foo, Bar> { }
						
						    public static abstract class Key<S, T> {
						
						        public abstract Key<T, S> flip();
						
						        private static class B2F extends Key<Bar, Foo> {
						            private static B2F create() { return new B2F(); }
						            public Key<Foo, Bar> flip() { return F2B.create(); }
						        }
						
						        private static class F2B extends Key<Foo, Bar> {
						            private static F2B create() { return new F2B(); }
						            public Key<Bar, Foo> flip() { return B2F.create(); }
						        }
						    }
						}""", // =================
				},
				"");
	}
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=216683 - variation
public void test152() {
	long compliance = new CompilerOptions(getCompilerOptions()).complianceLevel;
	if (compliance <= ClassFileConstants.JDK1_3) {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
						
						    public static interface Foo { }
						    public static interface Bar { }
						
						    public static class B2F extends X { }
						    public static class F2B extends X { }
						
						    public static abstract class Key {
						
						        public abstract Key flip();
						
						        public static class B2F extends Key {
						            private static B2F create() { return new B2F(); }
						            public Key flip() { return F2B.create(); }
						        }
						
						        public static class F2B extends Key {
						            private static F2B create() { return new F2B(); }
						            public Key flip() { return B2F.create(); }
						        }
						    }
						}""", // =================
				},
				"""
					----------
					1. ERROR in X.java (at line 15)
						public Key flip() { return F2B.create(); }
						                           ^^^
					The type F2B is defined in an inherited type and an enclosing scope
					----------
					2. ERROR in X.java (at line 20)
						public Key flip() { return B2F.create(); }
						                           ^^^
					The type B2F is defined in an inherited type and an enclosing scope
					----------
					""");
	} else if (compliance == ClassFileConstants.JDK1_4) {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
						
						    public static interface Foo { }
						    public static interface Bar { }
						
						    private static class B2F extends X { }
						    private static class F2B extends X { }
						
						    public static abstract class Key {
						
						        public abstract Key flip();
						
						        private static class B2F extends Key {
						            private static B2F create() { return new B2F(); }
						            public Key flip() { return F2B.create(); }
						        }
						
						        private static class F2B extends Key {
						            private static F2B create() { return new F2B(); }
						            public Key flip() { return B2F.create(); }
						        }
						    }
						}""", // =================

				},
				"");
	} else {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X<U, V> {
						
						    public static interface Foo { }
						    public static interface Bar { }
						
						    private static class B2F extends X<Bar, Foo> { }
						    private static class F2B extends X<Foo, Bar> { }
						
						    public static abstract class Key<S, T> {
						
						        public abstract Key<T, S> flip();
						
						        private static class B2F extends Key<Bar, Foo> {
						            private static B2F create() { return new B2F(); }
						            public Key<Foo, Bar> flip() { return F2B.create(); }
						        }
						
						        private static class F2B extends Key<Foo, Bar> {
						            private static F2B create() { return new F2B(); }
						            public Key<Bar, Foo> flip() { return B2F.create(); }
						        }
						    }
						}""", // =================
				},
				"");
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=201487
public void _test153() {
	long compliance = new CompilerOptions(getCompilerOptions()).complianceLevel;
	if (compliance <= ClassFileConstants.JDK1_4) {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
							public class Test3 {
								protected void load() {
								}
							}
							public class Test2 {
								public Test2(String string, Test3 test3) {
								}
							}
							private String var1;
						private class Test5 {
								private class Test4 extends Test2 {
									public Test4() {
										super("available", new Test3() {
											protected void load() {
												System.out.println(X.this.var1.trim());
												System.out.println(var1.trim());
											}
										});
									}
								}
							}
						}""", // =================
				},
				"""
					----------
					1. ERROR in X.java (at line 16)
						System.out.println(X.this.var1.trim());
						                   ^^^^^^
					No enclosing instance of the type X is accessible in scope
					----------
					2. WARNING in X.java (at line 16)
						System.out.println(X.this.var1.trim());
						                          ^^^^
					Read access to enclosing field X.var1 is emulated by a synthetic accessor method
					----------
					3. WARNING in X.java (at line 17)
						System.out.println(var1.trim());
						                   ^^^^
					Read access to enclosing field X.var1 is emulated by a synthetic accessor method
					----------
					4. ERROR in X.java (at line 17)
						System.out.println(var1.trim());
						                   ^^^^
					No enclosing instance of the type X is accessible in scope
					----------
					""");
	} else {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
							public class Test3 {
								protected void load() {
								}
							}
							public class Test2 {
								public Test2(String string, Test3 test3) {
								}
							}
							private String var1;
						private class Test5 {
								private class Test4 extends Test2 {
									public Test4() {
										super("available", new Test3() {
											protected void load() {
												System.out.println(X.this.var1.trim());
												System.out.println(var1.trim());
											}
										});
									}
								}
							}
						}""", // =================
				},
				"");
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=201487 - variation
public void test154() {
	long compliance = new CompilerOptions(getCompilerOptions()).complianceLevel;
	if (compliance <= ClassFileConstants.JDK1_4) {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
							public class Test3 {
								protected void load() {
								}
							}
							public class Test2 {
								public Test2(String string, Test3 test3) {
								}
							}
							private String var1;
						//	private class Test5 {
								private class Test4 extends Test2 {
									public Test4() {
										super("available", new Test3() {
											protected void load() {
												System.out.println(X.this.var1.trim());
												System.out.println(var1.trim());
											}
										});
									}
								}
						//	}
						}""", // =================
				},
				"""
					----------
					1. ERROR in X.java (at line 16)
						System.out.println(X.this.var1.trim());
						                   ^^^^^^
					No enclosing instance of the type X is accessible in scope
					----------
					2. WARNING in X.java (at line 16)
						System.out.println(X.this.var1.trim());
						                          ^^^^
					Read access to enclosing field X.var1 is emulated by a synthetic accessor method
					----------
					3. WARNING in X.java (at line 17)
						System.out.println(var1.trim());
						                   ^^^^
					Read access to enclosing field X.var1 is emulated by a synthetic accessor method
					----------
					4. ERROR in X.java (at line 17)
						System.out.println(var1.trim());
						                   ^^^^
					No enclosing instance of the type X is accessible in scope
					----------
					""");
	} else {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
							public class Test3 {
								protected void load() {
								}
							}
							public class Test2 {
								public Test2(String string, Test3 test3) {
								}
							}
							private String var1;
						//	private class Test5 {
								private class Test4 extends Test2 {
									public Test4() {
										super("available", new Test3() {
											protected void load() {
												System.out.println(X.this.var1.trim());
												System.out.println(var1.trim());
											}
										});
									}
								}
						//	}
						}""", // =================
				},
				"");
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=
public void test155() throws Exception {
	this.runConformTest(new String[] {
		"X.java",
		"""
			public class X {
				Object foo() {
					return new X() {};
				}
			}"""
	});
	File fileX = new File(OUTPUT_DIR + File.separator  +"X$1.class");
	IClassFileReader reader = ToolFactory.createDefaultClassFileReader(fileX.getAbsolutePath(), IClassFileReader.CONSTANT_POOL);
	assertFalse("Should not be final", Flags.isFinal(reader.getAccessFlags()));
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=128563
public void test156() throws Exception {
	this.runConformTest(new String[] {
		"package1/A.java",//=======================
		"""
			package package1;
			abstract class A {
			    protected final void outerMethod() {
			    }
			}
			""",
		"package1/B.java",//=======================
		"""
			package package1;
			public class B extends A {
			}
			""",
		"package2/C.java",//=======================
		"""
			package package2;
			import package1.B;
			public class C extends B {
			    private final MyInner myInner = new MyInner();
			    private class MyInner {
			        public void innerMethod() {
			            C.this.outerMethod();
			        }
			    }
			    public static void main(String[] args) {
			        final C c = new C();
			        c.myInner.innerMethod();
			    }
			}
			""",
	},
	"");
	String expectedOutput =
		"""
		  static synthetic void access$0(package2.C arg0);
		    0  aload_0 [arg0]
		    1  invokevirtual package2.C.outerMethod() : void\
		""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "package2" + File.separator + "C.class", "C", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=249107
public void test157() throws Exception {
	this.runConformTest(new String[] {
		"package1/A.java",//=======================
		"""
			package package1;
			abstract class A {
			    protected int outerField; {
			    }
			}
			""",
		"package1/B.java",//=======================
		"""
			package package1;
			public class B extends A {
			}
			""",
		"package2/C.java",//=======================
		"""
			package package2;
			import package1.B;
			public class C extends B {
			    private final MyInner myInner = new MyInner();
			    private class MyInner {
			        public void innerMethod() {
			            int j = C.this.outerField;
			        }
			    }
			    public static void main(String[] args) {
			        final C c = new C();
			        c.myInner.innerMethod();
			    }
			}
			""",
	},
	"");
	String expectedOutput =
		"""
		  // Method descriptor #33 (Lpackage2/C;)I
		  // Stack: 1, Locals: 1
		  static synthetic int access$0(package2.C arg0);
		    0  aload_0 [arg0]
		    1  getfield package2.C.outerField : int\
		""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "package2" + File.separator + "C.class", "C", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=249107 - variation
public void test158() throws Exception {
	this.runConformTest(new String[] {
		"package1/A.java",//=======================
		"""
			package package1;
			abstract class A {
			    protected int outerField; {
			    }
			}
			""",
		"package1/B.java",//=======================
		"""
			package package1;
			public class B extends A {
			}
			""",
		"package2/C.java",//=======================
		"""
			package package2;
			import package1.B;
			public class C extends B {
			    private final MyInner myInner = new MyInner();
			    private class MyInner {
			        public void innerMethod() {
			            C.this.outerField = 12;
			        }
			    }
			    public static void main(String[] args) {
			        final C c = new C();
			        c.myInner.innerMethod();
			    }
			}
			""",
	},
	"");
	String expectedOutput =
		"""
		  // Method descriptor #33 (Lpackage2/C;I)V
		  // Stack: 2, Locals: 2
		  static synthetic void access$0(package2.C arg0, int arg1);
		    0  aload_0 [arg0]
		    1  iload_1 [arg1]
		    2  putfield package2.C.outerField : int\
		""";

	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "package2" + File.separator + "C.class", "C", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=249107 - variation
public void test159() throws Exception {
	this.runConformTest(new String[] {
		"package1/A.java",//=======================
		"""
			package package1;
			abstract class A {
			    protected int outerField; {
			    }
			}
			""",
		"package1/B.java",//=======================
		"""
			package package1;
			public class B extends A {
			}
			""",
		"package2/C.java",//=======================
		"""
			package package2;
			import package1.B;
			public class C extends B {
			    private final MyInner myInner = new MyInner();
			    private class MyInner {
			        public void innerMethod() {
			            int j = outerField;
			        }
			    }
			    public static void main(String[] args) {
			        final C c = new C();
			        c.myInner.innerMethod();
			    }
			}
			""",
	},
	"");
	String expectedOutput =
		"""
		  // Method descriptor #33 (Lpackage2/C;)I
		  // Stack: 1, Locals: 1
		  static synthetic int access$0(package2.C arg0);
		    0  aload_0 [arg0]
		    1  getfield package2.C.outerField : int\
		""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "package2" + File.separator + "C.class", "C", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=249107 - variation
public void test160() throws Exception {
	this.runConformTest(new String[] {
		"package1/A.java",//=======================
		"""
			package package1;
			abstract class A {
			    protected int outerField; {
			    }
			}
			""",
		"package1/B.java",//=======================
		"""
			package package1;
			public class B extends A {
			}
			""",
		"package2/C.java",//=======================
		"""
			package package2;
			import package1.B;
			public class C extends B {
			    private final MyInner myInner = new MyInner();
			    private class MyInner {
			        public void innerMethod() {
			            outerField = 12;
			        }
			    }
			    public static void main(String[] args) {
			        final C c = new C();
			        c.myInner.innerMethod();
			    }
			}
			""",
	},
	"");
	String expectedOutput =
		"""
		  // Method descriptor #33 (Lpackage2/C;I)V
		  // Stack: 2, Locals: 2
		  static synthetic void access$0(package2.C arg0, int arg1);
		    0  aload_0 [arg0]
		    1  iload_1 [arg1]
		    2  putfield package2.C.outerField : int\
		""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "package2" + File.separator + "C.class", "C", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=249107 - variation
public void test161() throws Exception {
	this.runConformTest(new String[] {
		"package1/A.java",//=======================
		"""
			package package1;
			abstract class A {
			    static protected int outerField; {
			    }
			}
			""",
		"package1/B.java",//=======================
		"""
			package package1;
			public class B extends A {
			}
			""",
		"package2/C.java",//=======================
		"""
			package package2;
			import package1.B;
			public class C extends B {
			    private final MyInner myInner = new MyInner();
			    private class MyInner {
			        public void innerMethod() {
			            int j = C.this.outerField;
			        }
			    }
			    public static void main(String[] args) {
			        final C c = new C();
			        c.myInner.innerMethod();
			    }
			}
			""",
	},
	"");
	String expectedOutput =
		"""
		  // Method descriptor #33 ()I
		  // Stack: 1, Locals: 0
		  static synthetic int access$0();
		    0  getstatic package2.C.outerField : int\
		""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "package2" + File.separator + "C.class", "C", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=249107 - variation
public void test162() throws Exception {
	this.runConformTest(new String[] {
		"package1/A.java",//=======================
		"""
			package package1;
			abstract class A {
			    static protected int outerField; {
			    }
			}
			""",
		"package1/B.java",//=======================
		"""
			package package1;
			public class B extends A {
			}
			""",
		"package2/C.java",//=======================
		"""
			package package2;
			import package1.B;
			public class C extends B {
			    private final MyInner myInner = new MyInner();
			    private class MyInner {
			        public void innerMethod() {
			            C.this.outerField = 12;
			        }
			    }
			    public static void main(String[] args) {
			        final C c = new C();
			        c.myInner.innerMethod();
			    }
			}
			""",
	},
	"");
	String expectedOutput =
		"""
		  // Method descriptor #33 (I)V
		  // Stack: 1, Locals: 1
		  static synthetic void access$0(int arg0);
		    0  iload_0 [arg0]
		    1  putstatic package2.C.outerField : int\
		""";

	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "package2" + File.separator + "C.class", "C", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=249107 - variation
public void test163() throws Exception {
	this.runConformTest(new String[] {
		"package1/A.java",//=======================
		"""
			package package1;
			abstract class A {
			    static protected int outerField; {
			    }
			}
			""",
		"package1/B.java",//=======================
		"""
			package package1;
			public class B extends A {
			}
			""",
		"package2/C.java",//=======================
		"""
			package package2;
			import package1.B;
			public class C extends B {
			    private final MyInner myInner = new MyInner();
			    private class MyInner {
			        public void innerMethod() {
			            int j = outerField;
			        }
			    }
			    public static void main(String[] args) {
			        final C c = new C();
			        c.myInner.innerMethod();
			    }
			}
			""",
	},
	"");
	String expectedOutput =
		"""
		  // Method descriptor #33 ()I
		  // Stack: 1, Locals: 0
		  static synthetic int access$0();
		    0  getstatic package2.C.outerField : int\
		""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "package2" + File.separator + "C.class", "C", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=249107 - variation
public void test164() throws Exception {
	this.runConformTest(new String[] {
		"package1/A.java",//=======================
		"""
			package package1;
			abstract class A {
			    static protected int outerField; {
			    }
			}
			""",
		"package1/B.java",//=======================
		"""
			package package1;
			public class B extends A {
			}
			""",
		"package2/C.java",//=======================
		"""
			package package2;
			import package1.B;
			public class C extends B {
			    private final MyInner myInner = new MyInner();
			    private class MyInner {
			        public void innerMethod() {
			            outerField = 12;
			        }
			    }
			    public static void main(String[] args) {
			        final C c = new C();
			        c.myInner.innerMethod();
			    }
			}
			""",
	},
	"");
	String expectedOutput =
		"""
		  // Method descriptor #33 (I)V
		  // Stack: 1, Locals: 1
		  static synthetic void access$0(int arg0);
		    0  iload_0 [arg0]
		    1  putstatic package2.C.outerField : int\
		""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "package2" + File.separator + "C.class", "C", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=128563 - variation
public void test165() throws Exception {
	this.runConformTest(new String[] {
		"package1/A.java",//=======================
		"""
			package package1;
			abstract class A {
			    static protected final void outerMethod() {
			    }
			}
			""",
		"package1/B.java",//=======================
		"""
			package package1;
			public class B extends A {
			}
			""",
		"package2/C.java",//=======================
		"""
			package package2;
			import package1.B;
			public class C extends B {
			    private final MyInner myInner = new MyInner();
			    private class MyInner {
			        public void innerMethod() {
			            C.this.outerMethod();
			        }
			    }
			    public static void main(String[] args) {
			        final C c = new C();
			        c.myInner.innerMethod();
			    }
			}
			""",
	},
	"");
	String expectedOutput =
		"""
		  // Method descriptor #8 ()V
		  // Stack: 0, Locals: 0
		  static synthetic void access$0();
		    0  invokestatic package2.C.outerMethod() : void\
		""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "package2" + File.separator + "C.class", "C", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=249107 - variation
public void test166() throws Exception {
	this.runConformTest(new String[] {
		"X.java",//=======================
		"""
			class XSuper {
				protected String field = "[XSuper#field]";//$NON-NLS-1$
			}
			public class X extends XSuper {
				protected String field = "[X#field]";//$NON-NLS-1$
				public static void main(String[] args) {
					new X().foo();
				}
				void foo() {
					new Object() {
						void bar() {
							System.out.print("X.this.field=" + X.this.field);
							System.out.print("X.super.field=" + X.super.field);
						}
					}.bar();
				}
			}
			""",
	},
	"X.this.field=[X#field]X.super.field=[XSuper#field]");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=249107 - variation
public void test167() throws Exception {
	this.runConformTest(new String[] {
		"X.java",//=======================
		"""
			class XSuper {
				protected String method() { return "[XSuper#method()]"; }//$NON-NLS-1$
			}
			public class X extends XSuper {
				protected String method() { return "[X#method()]"; }//$NON-NLS-1$
				public static void main(String[] args) {
					new X().foo();
				}
				void foo() {
					new Object() {
						void bar() {
							System.out.print("X.this.method()=" + X.this.method());
							System.out.print("X.super.method()=" + X.super.method());
						}
					}.bar();
				}
			}
			""",
	},
	"X.this.method()=[X#method()]X.super.method()=[XSuper#method()]");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=249107 - variation
public void test168() throws Exception {
	this.runConformTest(new String[] {
		"X.java",//=======================
		"""
			class XSuper {
				protected String field;
			}
			public class X extends XSuper {
				protected String field;
				public static void main(String[] args) {
					new X().foo();
				}
				void foo() {
					new Object() {
						void bar() {
							X.this.field = "[X#field]";
							X.super.field = "[XSuper#field]";
							System.out.print("X.this.field=" + X.this.field);
							System.out.print("X.super.field=" + X.super.field);
						}
					}.bar();
				}
			}
			""",
	},
	"X.this.field=[X#field]X.super.field=[XSuper#field]");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=249107 - variation
public void test169() throws Exception {
	this.runConformTest(new String[] {
		"X.java",//=======================
		"""
			import p.XSuper;
			public class X extends XSuper {
				protected String method() { return "[X#method()]"; }//$NON-NLS-1$
				public static void main(String[] args) {
					new X().foo();
				}
				void foo() {
					new Object () {
						void bar() {
							System.out.print("X.this.method()=" + X.this.method());
							System.out.print("X.super.method()=" + X.super.method());
						}
					}.bar();
				}
			}
			""",
		"p/XSuper.java",//=======================
		"""
			package p;
			class XInternal {
				protected String method() { return "[XInternal#method()]"; }//$NON-NLS-1$
			}
			public class XSuper extends XInternal {
			}
			""",
	},
	"X.this.method()=[X#method()]X.super.method()=[XInternal#method()]");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=249107 - variation
public void test170() throws Exception {
	this.runConformTest(new String[] {
		"X.java",//=======================
		"""
			public class X {
			    class Member {
			        private String field = "SUCCESS";
			    }
			    class SubMember extends Member {
			    	void foo() {
			    		System.out.println(super.field);
			    	}
			    }\t
			    public static void main(String argv[]) {
					new X().new SubMember().foo();    \t
			    }
			}
			""",
	},
	"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=249107 - variation
public void test171() throws Exception {
	this.runConformTest(new String[] {
		"X.java",//=======================
		"""
			public class X {
			    class Member {
			        private String method() { return "SUCCESS"; }
			    }
			    class SubMember extends Member {
			    	void foo() {
			    		System.out.println(super.method());
			    	}
			    }\t
			    public static void main(String argv[]) {
					new X().new SubMember().foo();    \t
			    }
			}
			""",
	},
	"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=197271
public void test172() throws Exception {
	String[] files = new String[] {
			"X.java",
			"""
				public class X {
					void a() {}
					private static void a(String s) {}
					private void c() {}
					private static void c(String s) {}
					static class M1 extends X {
						public void x() {
							a(null);
							c(null);
						}
					}
					static class M2 {
						public void x() {
							a(null);
							c(null);
						}
					}
					public static void main(String[] args) {
					}
				}
				""",
		};
	if (this.complianceLevel < ClassFileConstants.JDK11) {
		this.runNegativeTest(
				files,
				"""
					----------
					1. WARNING in X.java (at line 8)
						a(null);
						^^^^^^^
					Access to enclosing method a(String) from the type X is emulated by a synthetic accessor method
					----------
					2. WARNING in X.java (at line 9)
						c(null);
						^^^^^^^
					Access to enclosing method c(String) from the type X is emulated by a synthetic accessor method
					----------
					3. WARNING in X.java (at line 14)
						a(null);
						^^^^^^^
					Access to enclosing method a(String) from the type X is emulated by a synthetic accessor method
					----------
					4. WARNING in X.java (at line 15)
						c(null);
						^^^^^^^
					Access to enclosing method c(String) from the type X is emulated by a synthetic accessor method
					----------
					"""
				);
	} else {
		this.runConformTest(files, "");
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=308245
public void test173() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",//=======================
			"""
				import java.util.ArrayList;
				import java.util.Comparator;
				import java.util.List;
				public class X {
					public static void main(String[] args) {
						final List yourList = new ArrayList();
						final List myList = new ArrayList();
						new Comparator() {
							public int compare(Object o1, Object o2) {
								compare(yourList != null ? yourList : myList, yourList);
								return 0;
							}
						};
						System.out.println("SUCCESS");
					}
				}""",
		},
		"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=308245
public void test174() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",//=======================
			"""
				import java.util.Comparator;
				public class X {
					public static class MyList {
						int size;
					}
					public static void main(String[] args) {
						final MyList yourList = new MyList();
						final MyList myList = new MyList();
						new Comparator() {
							public int compare(Object o1, Object o2) {
								return compare((MyList) o1, (MyList) o2);
							}
							public int compare(MyList o1, MyList o2) {
								return foo(yourList != null ? yourList.size : myList.size, yourList.size);
							}
							private int foo(int i, int j) {
								return i - j;
							}
						};
						System.out.println("SUCCESS");
					}
				}""",
		},
		"SUCCESS");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=388903
public void test175() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",//=======================
			"""
				public class X {
					class Inner extends X {
					}
					public static void main(String[] args) {
						new X().new Inner(){};
						System.out.println("SUCCESS");
					}
				}""",
		},
		"SUCCESS");
	this.runConformTest(
			new String[] {
				"X.java",//=======================
				"""
					public class X {
						String which;
						X(String s) {
							this.which = s;
						}
						class Inner extends X {
							Inner() {
								super("Inner");
								System.out.print( X.this.which + "," ); // will output 'Enclosing,'
							}
						}
						void check() {
							new X("Enclosing").new Inner() {
								{
									System.out.print( X.this.which + "," ); // will output 'Context,'
								}
								void f() {
									System.out.println( X.this.which ); // will output 'Context'
								}
							}.f();
						}
						public static void main(String[] args) {
							new X("Context").check();
						}
					}""",
			},
			"Enclosing,Context,Context");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=435419 Instantiating needs outer constructor
public void test176() {
	this.runConformTest(
		new String[] {
			"Demo.java",
			"""
				import java.util.ArrayList;
				public class Demo {
				        static class ExprFactoryList extends ArrayList {
				                class Expr {}
				                class Expr2 extends Expr {}
				        }
				        final static ExprFactoryList arith =  new ExprFactoryList() {
				                {
				                        add(new Object() {public Expr generate() {return new Expr() {};} }); // OK
				                        add(new Object() {public Expr generate() {return new Expr2() {};} }); // Ok
				                }
				        };
				        final static ExprFactoryList statementFactory =  new ExprFactoryList() {
				                class Statement extends Expr {}
				                void m() {
				                        add(new Object() {
				                                public void generate() {
				                                        new Statement(){}; // OK
				                                }
				                        });
				                }
				                {
				                        add (new Statement()); // OK
				                        add(new Object() {
				                                public void generate() {
				                                        new Statement(); // OK
				                                        new Statement(){}; // cannot compile
				                                }
				                        });
				                }
				        };
				        public static void main(String[] args) {
				        	Demo demo = new Demo();
				        	System.out.println("SUCCESS");
				        }
				      \s
				}"""
		},
		"SUCCESS");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=484546 "IncompatibleClassChangeError: Expected static method[...]" with inner classes
public void testbug484546() {
	this.runConformTest(
		new String[] {
			"inner/test/InnerTest.java",
			"""
				package inner.test;
				class Inner029SuperSuper {
				  public int getValue() {
				    return 10;
				  }
				}
				class Inner029Super extends Inner029SuperSuper {
				}
				class InnerSuper extends Inner029Super {
				  public int getValue() {
				    return 20;
				  }
				}
				public class InnerTest extends Inner029Super {
				  public int result = new Inner().getInner2().test();
				  class Inner extends InnerSuper {
				    Inner2 getInner2() {
				      return new Inner2();
				    }
				    class Inner2 {
				      public int test() {
				        return InnerTest.super.getValue();
				      }
				    }
				  }
				  public static void main(String[] args) {
					System.out.println(new InnerTest().result);
				}
				}
				"""
		},
		"10");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=373371 [compiler] JDT Compiler reports an error whereas javac compiles without problem
public void testbug373371() {
	String[] sources = new String[] {
		"Outer.java",
		"""
			class Outer {
			    class Inner extends Outer {    }
			    class SubInner extends Inner {
			        public SubInner() {
			          // Outer.this.super(); // (1)
			        }
			    }
			}"""
	};
	if (this.complianceLevel < ClassFileConstants.JDK1_4 || this.complianceLevel > ClassFileConstants.JDK1_6) {
		this.runConformTest(sources);
	} else {
		this.runNegativeTest(
			sources,
			"""
				----------
				1. ERROR in Outer.java (at line 4)
					public SubInner() {
					       ^^^^^^^^^^
				No enclosing instance of type Outer is available due to some intermediate constructor invocation
				----------
				""");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=522061 EJC 4.8M1 does not compile a class that javac 1.8.0_112 compiles
public void testbug522061() {
	String[] sources = new String[] {
		"ztest/Foo.java",
		"""
			package ztest;
			import java.io.File;
			import javax.swing.Action;
			public abstract class Foo {
				public FilteredFileTree matching(final Action filterConfigAction) {
					return new FilteredFileTree() {
						//@Override
						protected File filter(File set) {
							return null;
						}
					};
				}
				public String getDisplayName() {
					return null;
				}
			   private abstract class FilteredFileTree extends Foo {
					protected abstract File filter(File set);
					public String getDisplayName() {
						return Foo.this.toString();
					}
				}
			}"""
	};
	if (this.complianceLevel < ClassFileConstants.JDK1_4 || this.complianceLevel > ClassFileConstants.JDK1_6) {
		this.runConformTest(sources);
	} else {
		this.runNegativeTest(
			sources,
			"""
				----------
				1. WARNING in ztest\\Foo.java (at line 6)
					return new FilteredFileTree() {
					           ^^^^^^^^^^^^^^^^^^
				Access to enclosing constructor Foo.FilteredFileTree() is emulated by a synthetic accessor method
				----------
				2. ERROR in ztest\\Foo.java (at line 6)
					return new FilteredFileTree() {
					           ^^^^^^^^^^^^^^^^^^
				No enclosing instance of type Foo is available due to some intermediate constructor invocation
				----------
				""");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=481793 Compilation error when trying to compile nested inner classes
public void testbug481793() {
	String[] sources = new String[] {
		"A.java",
		"""
			public class A {
				public class B extends A {
					public class C extends B {}
				}
			}"""
	};
	if (this.complianceLevel < ClassFileConstants.JDK1_4 || this.complianceLevel > ClassFileConstants.JDK1_6) {
		this.runConformTest(sources);
	} else {
		this.runNegativeTest(
			sources,
			"""
				----------
				1. ERROR in A.java (at line 3)
					public class C extends B {}
					             ^
				No enclosing instance of type A is available due to some intermediate constructor invocation
				----------
				""");
	}
}
public static Class testClass() {
	return InnerEmulationTest.class;
}
}
