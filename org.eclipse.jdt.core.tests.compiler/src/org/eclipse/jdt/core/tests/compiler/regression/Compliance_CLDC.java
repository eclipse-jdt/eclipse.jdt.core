/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class Compliance_CLDC extends AbstractRegressionTest {

public Compliance_CLDC(String name) {
	super(name);
}

/*
 * Toggle compiler in mode -1.3
 */
@Override
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_3);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_CLDC1_1);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_3);
	return options;
}
public static Test suite() {
		return buildUniqueComplianceTestSuite(testClass(), ClassFileConstants.JDK1_3);
}
public static Class testClass() {
	return Compliance_CLDC.class;
}
// Use this static initializer to specify subset for tests
// All specified tests which does not belong to the class are skipped...
static {
//		TESTS_NAMES = new String[] { "Bug58069" };
//		TESTS_NUMBERS = new int[] { 104 };
//		TESTS_RANGE = new int[] { 76, -1 };
}
public void test001() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.awt.Image;
				import java.awt.Toolkit;
				import java.awt.image.ImageProducer;
				import java.net.URL;
				
				public class X {
				
					public Image loadImage(String name) {
						Toolkit toolkit= Toolkit.getDefaultToolkit();
						try {
							URL url= X.class.getResource(name);
							return toolkit.createImage((ImageProducer) url.getContent());
						} catch (Exception ex) {
						}
						return null;
					}
				\t
					public static void main(String[] args) {
							System.out.println("OK");
					}
				}""",
		},
		"OK");
}
public void test002() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				
					public static void main(String[] args) {
							System.out.print(X.class != null);
							System.out.print(String.class != null);
							System.out.print(Object.class != null);
							System.out.print(X.class != null);
					}
				}""",
		},
		"truetruetruetrue");

	String expectedOutput =
		"""
		  // Method descriptor #20 ([Ljava/lang/String;)V
		  // Stack: 3, Locals: 1
		  public static void main(java.lang.String[] args);
		      0  getstatic java.lang.System.out : java.io.PrintStream [21]
		      3  getstatic X.class$0 : java.lang.Class [27]
		      6  dup
		      7  ifnonnull 35
		     10  pop
		     11  ldc <String "X"> [29]
		     13  invokestatic java.lang.Class.forName(java.lang.String) : java.lang.Class [30]
		     16  dup
		     17  putstatic X.class$0 : java.lang.Class [27]
		     20  goto 35
		     23  new java.lang.NoClassDefFoundError [36]
		     26  dup_x1
		     27  swap
		     28  invokevirtual java.lang.Throwable.getMessage() : java.lang.String [38]
		     31  invokespecial java.lang.NoClassDefFoundError(java.lang.String) [44]
		     34  athrow
		     35  ifnull 42
		     38  iconst_1
		     39  goto 43
		     42  iconst_0
		     43  invokevirtual java.io.PrintStream.print(boolean) : void [47]
		     46  getstatic java.lang.System.out : java.io.PrintStream [21]
		     49  getstatic X.class$1 : java.lang.Class [53]
		     52  dup
		     53  ifnonnull 81
		     56  pop
		     57  ldc <String "java.lang.String"> [55]
		     59  invokestatic java.lang.Class.forName(java.lang.String) : java.lang.Class [30]
		     62  dup
		     63  putstatic X.class$1 : java.lang.Class [53]
		     66  goto 81
		     69  new java.lang.NoClassDefFoundError [36]
		     72  dup_x1
		     73  swap
		     74  invokevirtual java.lang.Throwable.getMessage() : java.lang.String [38]
		     77  invokespecial java.lang.NoClassDefFoundError(java.lang.String) [44]
		     80  athrow
		     81  ifnull 88
		     84  iconst_1
		     85  goto 89
		     88  iconst_0
		     89  invokevirtual java.io.PrintStream.print(boolean) : void [47]
		     92  getstatic java.lang.System.out : java.io.PrintStream [21]
		     95  getstatic X.class$2 : java.lang.Class [57]
		     98  dup
		     99  ifnonnull 127
		    102  pop
		    103  ldc <String "java.lang.Object"> [59]
		    105  invokestatic java.lang.Class.forName(java.lang.String) : java.lang.Class [30]
		    108  dup
		    109  putstatic X.class$2 : java.lang.Class [57]
		    112  goto 127
		    115  new java.lang.NoClassDefFoundError [36]
		    118  dup_x1
		    119  swap
		    120  invokevirtual java.lang.Throwable.getMessage() : java.lang.String [38]
		    123  invokespecial java.lang.NoClassDefFoundError(java.lang.String) [44]
		    126  athrow
		    127  ifnull 134
		    130  iconst_1
		    131  goto 135
		    134  iconst_0
		    135  invokevirtual java.io.PrintStream.print(boolean) : void [47]
		    138  getstatic java.lang.System.out : java.io.PrintStream [21]
		    141  getstatic X.class$0 : java.lang.Class [27]
		    144  dup
		    145  ifnonnull 173
		    148  pop
		    149  ldc <String "X"> [29]
		    151  invokestatic java.lang.Class.forName(java.lang.String) : java.lang.Class [30]
		    154  dup
		    155  putstatic X.class$0 : java.lang.Class [27]
		    158  goto 173
		    161  new java.lang.NoClassDefFoundError [36]
		    164  dup_x1
		    165  swap
		    166  invokevirtual java.lang.Throwable.getMessage() : java.lang.String [38]
		    169  invokespecial java.lang.NoClassDefFoundError(java.lang.String) [44]
		    172  athrow
		    173  ifnull 180
		    176  iconst_1
		    177  goto 181
		    180  iconst_0
		    181  invokevirtual java.io.PrintStream.print(boolean) : void [47]
		    184  return
		      Exception Table:
		        [pc: 11, pc: 16] -> 23 when : java.lang.ClassNotFoundException
		        [pc: 57, pc: 62] -> 69 when : java.lang.ClassNotFoundException
		        [pc: 103, pc: 108] -> 115 when : java.lang.ClassNotFoundException
		        [pc: 149, pc: 154] -> 161 when : java.lang.ClassNotFoundException
		      Line numbers:
		        [pc: 0, line: 4]
		        [pc: 46, line: 5]
		        [pc: 92, line: 6]
		        [pc: 138, line: 7]
		        [pc: 184, line: 8]
		      Local variable table:
		        [pc: 0, pc: 185] local: args index: 0 type: java.lang.String[]
		      Stack map : number of frames 16
		        [pc: 23, full, stack: {java.lang.ClassNotFoundException}, locals: {java.lang.String[]}]
		        [pc: 35, full, stack: {java.io.PrintStream, java.lang.Class}, locals: {java.lang.String[]}]
		        [pc: 42, full, stack: {java.io.PrintStream}, locals: {java.lang.String[]}]
		        [pc: 43, full, stack: {java.io.PrintStream, int}, locals: {java.lang.String[]}]
		        [pc: 69, full, stack: {java.lang.ClassNotFoundException}, locals: {java.lang.String[]}]
		        [pc: 81, full, stack: {java.io.PrintStream, java.lang.Class}, locals: {java.lang.String[]}]
		        [pc: 88, full, stack: {java.io.PrintStream}, locals: {java.lang.String[]}]
		        [pc: 89, full, stack: {java.io.PrintStream, int}, locals: {java.lang.String[]}]
		        [pc: 115, full, stack: {java.lang.ClassNotFoundException}, locals: {java.lang.String[]}]
		        [pc: 127, full, stack: {java.io.PrintStream, java.lang.Class}, locals: {java.lang.String[]}]
		        [pc: 134, full, stack: {java.io.PrintStream}, locals: {java.lang.String[]}]
		        [pc: 135, full, stack: {java.io.PrintStream, int}, locals: {java.lang.String[]}]
		        [pc: 161, full, stack: {java.lang.ClassNotFoundException}, locals: {java.lang.String[]}]
		        [pc: 173, full, stack: {java.io.PrintStream, java.lang.Class}, locals: {java.lang.String[]}]
		        [pc: 180, full, stack: {java.io.PrintStream}, locals: {java.lang.String[]}]
		        [pc: 181, full, stack: {java.io.PrintStream, int}, locals: {java.lang.String[]}]
		""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
public void test003() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				
					public static void main(String[] args) {
							System.out.print(int.class != null);
					}
				}""",
		},
		"true");

	String expectedOutput =
		"""
		// Compiled from X.java (version 1.1 : 45.3, super bit)
		public class X {
		 \s
		  // Method descriptor #6 ()V
		  // Stack: 1, Locals: 1
		  public X();
		    0  aload_0 [this]
		    1  invokespecial java.lang.Object() [8]
		    4  return
		      Line numbers:
		        [pc: 0, line: 1]
		      Local variable table:
		        [pc: 0, pc: 5] local: this index: 0 type: X
		 \s
		  // Method descriptor #15 ([Ljava/lang/String;)V
		  // Stack: 2, Locals: 1
		  public static void main(java.lang.String[] args);
		     0  getstatic java.lang.System.out : java.io.PrintStream [16]
		     3  getstatic java.lang.Integer.TYPE : java.lang.Class [22]
		     6  ifnull 13
		     9  iconst_1
		    10  goto 14
		    13  iconst_0
		    14  invokevirtual java.io.PrintStream.print(boolean) : void [28]
		    17  return
		      Line numbers:
		        [pc: 0, line: 4]
		        [pc: 17, line: 5]
		      Local variable table:
		        [pc: 0, pc: 18] local: args index: 0 type: java.lang.String[]
		      Stack map : number of frames 2
		        [pc: 13, full, stack: {java.io.PrintStream}, locals: {java.lang.String[]}]
		        [pc: 14, full, stack: {java.io.PrintStream, int}, locals: {java.lang.String[]}]
		}""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
public void test004() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.InputStream;
				
				public class X {
					private static final Y[] A = new Y[1];
				
					public static void x() {
						for (int i = 0; i < 0; i++) {
							try {
								A[i] = foo(X.class.getResourceAsStream(""), null);
							} catch (Throwable e) {
							}
						}
					}
				
					public static boolean a = false;
				
					private static int b = -1;
				
					private static int C = 0;
				
					public static void z(int c) {
						if (!a || (b == c && A[c].foo() == C)) {
							return;
						}
						y();
						b = c;
						try {
							A[c].bar();
						} catch (Throwable e) {
						}
					}
				
					public static void y() {
					}
				
					static Y foo(InputStream stream, String s) {
						return null;
					}
				}""",
			"Y.java",
			"""
				interface Y {
					int foo();
					void bar();
				}"""
		},
		"");
	}
}
