/*******************************************************************************
 * Copyright (c) 2009, 2014 IBM Corporation and others.
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
 *     Stephan Herrmann - Contribution for Bug 343713 - [compiler] bogus line number in constructor of inner class in 1.5 compliance
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;

import junit.framework.Test;

@SuppressWarnings({ "rawtypes" })
public class InnerEmulationTest_1_5 extends AbstractRegressionTest {
static {
//		TESTS_NAMES = new String[] { "Bug58069" };
//		TESTS_NUMBERS = new int[] { 13 };
//		TESTS_RANGE = new int[] { 144, -1 };
}
public InnerEmulationTest_1_5(String name) {
	super(name);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_5);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=275381
public void test1() throws Exception {
	this.runConformTest(new String[] {
		"X.java",
		"""
			import java.util.Collection;
			import java.util.Map;
			public class X {
				public void foo(Collection<? extends Map.Entry> args) { /* dummy */ }
			}"""
	});
	String expectedOutput =
		"""
		  Inner classes:
		    [inner class info: #25 java/util/Map$Entry, outer class info: #27 java/util/Map
		     inner name: #29 Entry, accessflags: 1545 public abstract static]
		""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=275381
public void test2() throws Exception {
	this.runConformTest(new String[] {
		"p/X.java",
		"""
			package p;
			import java.util.Collection;
			import java.util.Map;
			public class X {
				public void foo(Map.Entry args) { /* dummy */ }
			}"""
	});
	String expectedOutput =
		"""
		  Inner classes:
		    [inner class info: #21 java/util/Map$Entry, outer class info: #23 java/util/Map
		     inner name: #25 Entry, accessflags: 1545 public abstract static]
		""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "p" + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=275381
public void test3() throws Exception {
	this.runConformTest(new String[] {
		"X.java",
		"""
			import java.util.Map;
			import java.util.List;
			public class X {
				<U extends List<?>, T extends Map.Entry> X(List<U> lu, T t) {
				}
			}"""
	});
	String expectedOutput =
		"""
		  Inner classes:
		    [inner class info: #27 java/util/Map$Entry, outer class info: #29 java/util/Map
		     inner name: #31 Entry, accessflags: 1545 public abstract static]
		""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=275381
public void test4() throws Exception {
	this.runConformTest(new String[] {
		"X.java",
		"""
			import java.util.Map;
			import java.util.List;
			public class X<T extends Object & Comparable<? super Map.Entry>> {}"""
	});
	String expectedOutput =
		"""
		  Inner classes:
		    [inner class info: #21 java/util/Map$Entry, outer class info: #23 java/util/Map
		     inner name: #25 Entry, accessflags: 1545 public abstract static]
		""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=275381
public void test5() throws Exception {
	this.runConformTest(new String[] {
		"p/X.java",
		"""
			package p;
			import java.util.Collection;
			import java.util.Map;
			public class X {
				public void foo(Map.Entry<String, String> args) { /* dummy */ }
			}"""
	});
	String expectedOutput =
		"""
		  Inner classes:
		    [inner class info: #25 java/util/Map$Entry, outer class info: #27 java/util/Map
		     inner name: #29 Entry, accessflags: 1545 public abstract static]
		""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "p" + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=275381
public void test6() throws Exception {
	this.runConformTest(new String[] {
		"p/X.java",
		"""
			package p;
			import java.util.Collection;
			import java.util.Map;
			public class X {
				Map.Entry<String, String> f;
			}"""
	});
	String expectedOutput =
		"""
		  Inner classes:
		    [inner class info: #21 java/util/Map$Entry, outer class info: #23 java/util/Map
		     inner name: #25 Entry, accessflags: 1545 public abstract static]
		""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "p" + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=275381
public void test7() throws Exception {
	this.runConformTest(new String[] {
		"p/X.java",
		"""
			package p;
			import java.util.Collection;
			import java.util.Map;
			public class X<E extends Object & Map.Entry<String, E>> {
			}"""
	});
	String expectedOutput =
		"""
		  Inner classes:
		    [inner class info: #21 java/util/Map$Entry, outer class info: #23 java/util/Map
		     inner name: #25 Entry, accessflags: 1545 public abstract static]
		""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "p" + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=275381
public void test8() throws Exception {
	this.runConformTest(new String[] {
		"p/X.java",
		"""
			package p;
			import java.util.Collection;
			import java.util.Map;
			class A {
				static class B{}
			}
			public class X<E extends Object & Map.Entry<E, A.B>> {
			}"""
	});
	String expectedOutput =
		"""
		  Inner classes:
		    [inner class info: #21 java/util/Map$Entry, outer class info: #23 java/util/Map
		     inner name: #25 Entry, accessflags: 1545 public abstract static],
		    [inner class info: #26 p/A$B, outer class info: #28 p/A
		     inner name: #30 B, accessflags: 8 static]
		""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "p" + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=275381
public void test9() throws Exception {
	this.runConformTest(new String[] {
		"p/X.java",
		"""
			package p;
			import java.util.Collection;
			import java.util.Map;
			class A {
				static class B{}
			}
			public class X {
				<E extends Object & Map.Entry<E, A.B>> void foo(E e) {}
			}"""
	});
	String expectedOutput =
		"""
		  Inner classes:
		    [inner class info: #25 java/util/Map$Entry, outer class info: #27 java/util/Map
		     inner name: #29 Entry, accessflags: 1545 public abstract static],
		    [inner class info: #30 p/A$B, outer class info: #32 p/A
		     inner name: #34 B, accessflags: 8 static]
		""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "p" + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=275381
public void test10() throws Exception {
	this.runConformTest(new String[] {
		"p/X.java",
		"""
			package p;
			import java.util.Collection;
			import java.util.Map;
			class A {
				static interface B<U, T>{}
			}
			class C {
				static class D<U, T>{}
			}
			public class X {
				<E extends Object & A.B<E, Map.Entry<E, C.D>>> void foo(E e) {}
			}"""
	});
	String expectedOutput =
		"""
		  Inner classes:
		    [inner class info: #25 java/util/Map$Entry, outer class info: #27 java/util/Map
		     inner name: #29 Entry, accessflags: 1545 public abstract static],
		    [inner class info: #30 p/A$B, outer class info: #32 p/A
		     inner name: #34 B, accessflags: 1544 abstract static],
		    [inner class info: #35 p/C$D, outer class info: #37 p/C
		     inner name: #39 D, accessflags: 8 static]
		""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "p" + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=275381
public void test11() throws Exception {
	this.runConformTest(new String[] {
		"X.java",
		"""
			import java.util.*;
			
			 public class X {
			\s
				static abstract class SelfType<T extends SelfType<T>>{
				}
			\s
				static class SuperType extends SelfType<SuperType>{
				}
			\s
				static class SubType extends SuperType{}
			\s
				static <T extends SelfType<T>> List<T> makeSingletonList(T t){
					return Collections.singletonList(t);
				}
			\s
				static <T extends SelfType<T>,S extends T> List<T> makeSingletonList2(S s){
					return Collections.singletonList((T)s); // #0
				}
			}"""
	});
	String expectedOutput =
		"""
		  Inner classes:
		    [inner class info: #35 X$SelfType, outer class info: #1 X
		     inner name: #37 SelfType, accessflags: 1032 abstract static],
		    [inner class info: #38 X$SubType, outer class info: #1 X
		     inner name: #40 SubType, accessflags: 8 static],
		    [inner class info: #41 X$SuperType, outer class info: #1 X
		     inner name: #43 SuperType, accessflags: 8 static]
		""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=275381
public void test12() throws Exception {
	this.runConformTest(new String[] {
		"p/X.java",
		"""
			package p;
			import java.util.Collection;
			import java.util.Map;
			public class X {
				Collection<Map.Entry> field;
			}"""
	});
	String expectedOutput =
		"""
		  Inner classes:
		    [inner class info: #21 java/util/Map$Entry, outer class info: #23 java/util/Map
		     inner name: #25 Entry, accessflags: 1545 public abstract static]
		""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "p" + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=275381
public void test13() throws Exception {
	this.runConformTest(new String[] {
		"p/X.java",
		"""
			package p;
			import java.util.Collection;
			import java.util.Map;
			class A {
				static interface B<U, T>{}
			}
			class C {
				static class D<U, T>{}
			}
			public class X extends C.D implements A.B {
			}"""
	});
	String expectedOutput =
		"""
		  Inner classes:
		    [inner class info: #5 p/A$B, outer class info: #19 p/A
		     inner name: #21 B, accessflags: 1544 abstract static],
		    [inner class info: #3 p/C$D, outer class info: #22 p/C
		     inner name: #24 D, accessflags: 8 static]
		""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "p" + File.separator + "X.class", "X", expectedOutput);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=343713
// [compiler] bogus line number in constructor of inner class in 1.5 compliance
public void test14() throws Exception {
	runConformTest(new String[] {
		"LineNumberBug.java",
		"""
			public class LineNumberBug {
			    class Inner {
					public Inner() {
						System.out.println("Inner()");
					}
			    }
				public static void main(String[] args) {
					new LineNumberBug().new Inner();
				}
			}
			"""
	});
	String expectedOutput =
		"""
		  // Method descriptor #8 (LLineNumberBug;)V
		  // Stack: 2, Locals: 2
		  public LineNumberBug$Inner(LineNumberBug arg0);
		     0  aload_0 [this]
		     1  aload_1 [arg0]
		     2  putfield LineNumberBug$Inner.this$0 : LineNumberBug [10]
		     5  aload_0 [this]
		     6  invokespecial java.lang.Object() [12]
		     9  getstatic java.lang.System.out : java.io.PrintStream [15]
		    12  ldc <String "Inner()"> [21]
		    14  invokevirtual java.io.PrintStream.println(java.lang.String) : void [23]
		    17  return
		      Line numbers:
		        [pc: 0, line: 3]
		        [pc: 9, line: 4]
		        [pc: 17, line: 5]
		""";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "LineNumberBug$Inner.class", "LineNumberBug$Inner", expectedOutput);
}
public void testBug546362() throws Exception {
	runConformTest(new String[] {
		"Schema.java",
		"import java.util.HashMap;\n" +
		"\n" +
		"public class Schema {\n" +
		"    public Integer[] getElements(HashMap<String, Integer> map) {\n" +
		"        return map.entrySet().toArray(new Integer[0]);\n" +
		"    }\n" +
		"}\n" +
		""
	});
	String expectedOutput =
			"  Inner classes:\n" +
			"    [inner class info: #41 java/util/Map$Entry, outer class info: #43 java/util/Map\n" +
			"     inner name: #45 Entry, accessflags: 1545 public abstract static]\n" +
			"";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "Schema.class", "X", expectedOutput);
}
public static Class testClass() {
	return InnerEmulationTest_1_5.class;
}
}
