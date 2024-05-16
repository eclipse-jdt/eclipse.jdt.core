/*******************************************************************************
 * Copyright (c) 2019, 2022 IBM Corporation and others.
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

package org.eclipse.jdt.core.tests.model;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;

import junit.framework.Test;

public class ResolveTests12To15 extends AbstractJavaModelTests {
	ICompilationUnit wc = null;

static {
//	 TESTS_NAMES = new String[] { "testBug577508_4" };
	// TESTS_NUMBERS = new int[] { 124 };
	// TESTS_RANGE = new int[] { 16, -1 };
}
public static Test suite() {
	return buildModelTestSuite(ResolveTests12To15.class);
}
public ResolveTests12To15(String name) {
	super(name);
}
@Override
public ICompilationUnit getWorkingCopy(String path, String source) throws JavaModelException {
	return super.getWorkingCopy(path, source, this.wcOwner);
}
@Override
public void setUpSuite() throws Exception {
	super.setUpSuite();
	setUpJavaProject("Resolve", "12", false);
	setUpJavaProject("Resolve15", "15", false);
	waitUntilIndexesReady();
}
@Override
protected void setUp() throws Exception {
	super.setUp();
	this.wcOwner = new WorkingCopyOwner(){};
}
@Override
public void tearDownSuite() throws Exception {
	deleteProject("Resolve");
	deleteProject("Resolve15");
	super.tearDownSuite();
}

@Override
protected void tearDown() throws Exception {
	if (this.wc != null) {
		this.wc.discardWorkingCopy();
	}
	super.tearDown();
}
/*
 * Multi constant case statement with ':', selection node is the string constant
 */
public void test001() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve/src/X.java","""
		public class X {
		static final String ONE="One", TWO = "Two", THREE="Three";
		  public static void foo(String num) {
		 	 switch (num) {
			   case ONE, TWO, THREE:
				 System.out.println(num);
				 break;
		    }\
		  }
		}
		""");

	String str = this.wc.getSource();
	String selection = "ONE";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"ONE [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/*
 * Multi constant case statement with ':', selection node is the first enum constant
 */
public void test002() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve/src/X.java","""
		public class X {
		  public static void foo(Num num) {
		 	 switch (num) {
			   case ONE, TWO, THREE:
				 System.out.println(num);
				 break;
		    }\
		  }
			enum Num { ONE, TWO, THREE;}
		}
		""");
	String str = this.wc.getSource();
	String selection = "ONE";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"ONE [in Num [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/*
 * Multi constant case statement with ':', selection node is the second string constant
 */
public void test003() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve/src/X.java","""
		public class X {
		static final String ONE="One", TWO = "Two", THREE="Three";
		  public static void foo(String num) {
		 	 switch (num) {
			   case ONE, TWO, THREE:
				 System.out.println(num);
				 break;
		    }\
		  }
		}
		""");
	String str = this.wc.getSource();
	String selection = "TWO";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"TWO [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/*
 * Multi constant case statement with ':', selection node is the second enum constant
 */
public void test004() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve/src/X.java","""
		public class X {
		  public static void foo(Num num) {
		 	 switch (num) {
			   case ONE, TWO, THREE:
				 System.out.println(num);
				 break;
		    }\
		  }
			enum Num { ONE, TWO, THREE;}
		}
		""");
	String str = this.wc.getSource();
	String selection = "TWO";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"TWO [in Num [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/*
 * Multi constant case statement with '->', selection node is the string constant
 */
public void test005() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve/src/X.java","""
		public class X {
		static final String ONE="One", TWO = "Two", THREE="Three";
		  public static void foo(String num) {
		 	 switch (num) {
			   case ONE, TWO, THREE ->
				 System.out.println(num);
		    }\
		  }
		}
		""");
	String str = this.wc.getSource();
	String selection = "ONE";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"ONE [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/*
 * Multi constant case statement with '->', selection node is the first enum constant
 */
public void test006() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve/src/X.java","""
		public class X {
		  public static void foo(Num num) {
		 	 switch (num) {
			   case ONE, TWO, THREE ->
				 System.out.println(num);
				 break; // illegal, but should be ignored and shouldn't matter
		    }\
		  }
			enum Num { ONE, TWO, THREE;}
		}
		""");
	String str = this.wc.getSource();
	String selection = "ONE";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"ONE [in Num [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/*
 * Multi constant case statement with '->', selection node is the second string constant
 */
public void test007() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve/src/X.java","""
		public class X {
		static final String ONE="One", TWO = "Two", THREE="Three";
		  public static void foo(String num) {
		 	 switch (num) {
			   case ONE, TWO, THREE ->
				 System.out.println(num);
				 break;
		    }\
		  }
		}
		""");
	String str = this.wc.getSource();
	String selection = "TWO";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"TWO [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/*
 * Multi constant case statement with '->', selection node is the second enum constant
 */
public void test008() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve/src/X.java","""
		public class X {
		  public static void foo(Num num) {
		 	 switch (num) {
			   case ONE, TWO, THREE ->
				 System.out.println(num);
				 break;
		    }\
		  }
			enum Num { ONE, TWO, THREE;}
		}
		""");
	String str = this.wc.getSource();
	String selection = "TWO";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"TWO [in Num [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/*
 * Multi constant case statement with '->', selection is a reference in the case block
 * which same as the switch's expression
 */
public void test009() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve/src/X.java","""
		public class X {
		  public static void foo(Num num_) {
		 	 switch (num_) {
			   case ONE, TWO, THREE ->
				 System.out.println(num_);
				 break;
		    }\
		  }
			enum Num { ONE, TWO, THREE;}
		}
		""");
	String str = this.wc.getSource();
	String selection = "num_";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"num_ [in foo(Num) [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/*
 * Multi constant case statement with '->', selection is a reference in the case block
 * which is referencing a local variable defined in the case block
 */
public void test010() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve/src/X.java","""
		public class X {
		  public static void foo(Num num_) {
		 	 switch (num_) {
			   case ONE, TWO, THREE -> {
				 int i_j = 0;\
				 System.out.println(i_j);
				 break;\
				 }
		    }\
		  }
			enum Num { ONE, TWO, THREE;}
		}
		""");
	String str = this.wc.getSource();
	String selection = "i_j";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"i_j [in foo(Num) [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/*
 * Multi constant case statement with '->', selection is a referenced name of type enum in switch expression
 */
public void test011() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve/src/X.java","""
		public class X {
		  public static void foo(Num num_) {
		 	 switch (num_) {
			   case ONE, TWO, THREE -> {
				 break;\
				 }
		    }\
		  }
			enum Num { ONE, TWO, THREE;}
		}
		""");
	String str = this.wc.getSource();
	String selection = "num_";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"num_ [in foo(Num) [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/*
 * Multi constant case statement with '->', selection is a referenced name of type int in switch expression
 */
public void test012() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve/src/X.java","""
		public class X {
		  public static void foo(int num_) {
		 	 switch (num_ + 1) {
			   case 1, 2, 3 -> {
				 break;\
				 }
		    }\
		  }
		}
		""");
	String str = this.wc.getSource();
	String selection = "num_";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"num_ [in foo(int) [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/*
 * Multi constant case statement with '->', selection is a referenced name of type int in switch expression
 */
public void test013() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve/src/X.java","""
		public class X {
		  public static void foo(int num_) {
		 	 int i = switch (num_) {
			   case 1, 2, 3 -> (num_ + 1);
		      default -> 0;
		    }\
		  }
		}
		""");
	String str = this.wc.getSource();
	String selection = "num_";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"num_ [in foo(int) [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/*
 * Multi constant case statement with '->', selection is a referenced name of type int in switch expression
 */
public void test014() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve/src/X.java","""
		public class X {
		  public static void foo(int num_) {
		 	 int i = switch (num_) {
			   case 1, 2, 3 -> 0;
		      default -> (num_ + 1);
		    }\
		  }
		}
		""");
	String str = this.wc.getSource();
	String selection = "num_";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"num_ [in foo(int) [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/*
 * Multi constant case statement with '->', selection is a referenced name of type int in switch expression
 */
public void test015() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve/src/X.java","""
		public class X {
		  public static void foo(int num_) {
		 	 int i = switch (num_) {
			   case 1, 2, 3 -> 0;
		      default -> (num_ + 1);
		    }\
		  }
		}
		""");
	String str = this.wc.getSource();
	String selection = "num_";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"num_ [in foo(int) [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/*
 * Multi constant case statement with '->', selection is a referenced name of type int in switch expression
 */
public void test016() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve/src/X.java","""
		public class X {
			public void bar(int s) {
				int i_j = switch (s) {
					case 1, 2, 3 -> (s+1);
					default -> i_j;
				};
			}
		}
		""");
	String str = this.wc.getSource();
	String selection = "i_j";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"i_j [in bar(int) [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
public void test017() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve/src/X.java","""
		public class X {
			public void bar(int s) {
				int i_j = switch (s) {
					case 1, 2, 3 -> (s+1);
					default -> (1+i_j);
				};
			}
		}
		""");
	String str = this.wc.getSource();
	String selection = "i_j";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"i_j [in bar(int) [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
public void test018() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve/src/X.java",
			"""
				import java.util.function.*;
				interface IN0 {}\s
				interface IN1 extends IN0 {}\s
				interface IN2 extends IN0 {}
				public class X {
					 IN1 n_1() { return new IN1() {}; }\s
					IN2 n_2() { return null; }\s
					<M> void m( Supplier< M> m2) { }\s
					void testSw(int i) {\s
						m(switch(i) {\s
							case 1 -> this::n_1;\s
							default -> this::n_2; });\s
					}
				}
				""");
	String str = this.wc.getSource();
	String selection = "n_1";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"n_1() [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
public void test019() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve/src/X.java",
			"""
				import java.util.function.*;
				interface IN0 {}\s
				interface IN1 extends IN0 {}\s
				interface IN2 extends IN0 {}
				public class X {
					 IN1 n_1() { return new IN1() {}; }\s
					IN2 n_2() { return null; }\s
					<M> void m( Supplier< M> m2) { }\s
					void testSw(int i) {\s
						m(switch(i) {\s
							case 2 -> () -> n_1();\s
							default -> this::n_2; });\s
					}
				}
				""");
	String str = this.wc.getSource();
	String selection = "n_1";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"n_1() [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
public void test020() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve/src/X.java",
			"""
				import java.util.function.*;
				interface IN0 {}\s
				interface IN1 extends IN0 {}\s
				interface IN2 extends IN0 {}
				public class X {
					 IN1 n_1() { return new IN1() {}; }\s
					IN2 n_2() { return null; }\s
					<M> void m( Supplier< M> m2) { }\s
					void testSw(int i) {\s
						m(switch(i) {\s
							default -> this::n_2; });\s
					}
				}
				""");
	String str = this.wc.getSource();
	String selection = "n_2";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"n_2() [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
public void test021() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve/src/X.java",
			"""
				import java.util.function.*;
				interface IN0 {}\s
				interface IN1 extends IN0 {}\s
				interface IN2 extends IN0 {}
				public class X {
					 IN1 n_1(int ijk) { return new IN1() {}; }\s
					IN2 n_2() { return null; }\s
					<M> void m( Supplier< M> m2) { }\s
					void testSw(int ijk) {\s
						m(switch(ijk) {\s
							default -> () -> n_1(ijk); });\s
					}
				}
				""");
	String str = this.wc.getSource();
	String selection = "n_1";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"n_1(int) [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
public void test022() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve/src/X.java",
			"""
				import java.util.function.*;
				interface IN0 {}\s
				interface IN1 extends IN0 {}\s
				interface IN2 extends IN0 {}
				public class X {
					 IN1 n_1(int ijk) { return new IN1() {}; }\s
					IN2 n_2() { return null; }\s
					<M> void m( Supplier< M> m2) { }\s
					void testSw(int ijk) {\s
						m(switch(ijk) {\s
							default -> () -> n_1(ijk); });\s
					}
				}
				""");
	String str = this.wc.getSource();
	String selection = "ijk";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"ijk [in testSw(int) [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
public void testBug553149_1() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve15/src/X.java",
			"""
				public class X {
				    protected Object x_ = "FIELD X";
				    @SuppressWarnings("preview")
					   public void f(Object obj, boolean b) {
				        if ((x_ instanceof String y) && y.length() > 0) {
				            System.out.println(y.toLowerCase());
				        }
				    }
				}""");
	String str = this.wc.getSource();
	String selection = "x_";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"x_ [in X [in [Working copy] X.java [in <default> [in src [in Resolve15]]]]]",
		elements
	);
}
public void testBug553149_2() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve15/src/X.java",
			"""
				public class X {
				    protected Object x_ = "FIELD X";
				    @SuppressWarnings("preview")
					   public void f(Object obj, boolean b) {
				        if ((x_ instanceof String y_) && y_.length() > 0) {
				            System.out.println(y.toLowerCase());
				        }
				    }
				}""");
	String str = this.wc.getSource();
	String selection = "y_";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"y_ [in f(Object, boolean) [in X [in [Working copy] X.java [in <default> [in src [in Resolve15]]]]]]",
		elements
	);
}
public void testBug553149_3() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve15/src/X.java",
			"""
				public class X {
				    protected Object x_ = "FIELD X";
				    @SuppressWarnings("preview")
					   public void f(Object obj, boolean b) {
				        if ((x_ instanceof String x_) && x_.length() > 0) {
				            System.out.println(y.toLowerCase());
				        }
				    }
				}""");
	String str = this.wc.getSource();
	String selection = "x_";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"x_ [in f(Object, boolean) [in X [in [Working copy] X.java [in <default> [in src [in Resolve15]]]]]]",
		elements
	);
}
public void testBug553149_4() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve15/src/X.java",
			"""
				public class X {
				    protected Object x_ = "FIELD X";
				    @SuppressWarnings("preview")
					   public void f(Object obj, boolean b) {
				        if ((x_ instanceof String x_) && x_.length() > 0) {
				            System.out.println(x_.toLowerCase());
				        }
				    }
				}""");
	String str = this.wc.getSource();
	String selection = "x_";
	int start = str.lastIndexOf(selection);
	int length = "x_".length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"x_ [in f(Object, boolean) [in X [in [Working copy] X.java [in <default> [in src [in Resolve15]]]]]]",
		elements
	);
}
public void testBug553149_5() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve15/src/X.java",
			"""
				public class X {
				    protected Object x_ = "FIELD X";
				    @SuppressWarnings("preview")
					   public void f(Object obj, boolean b) {
				        if ((x_ instanceof String x_) && x_.length() > 0) {
				            System.out.println(x_.toLowerCase());
				        }
				        System.out.println(x_.toLowerCase());
				    }
				}""");
	String str = this.wc.getSource();
	String selection = "x_";
	int start = str.lastIndexOf(selection);
	int length = "x_".length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"x_ [in X [in [Working copy] X.java [in <default> [in src [in Resolve15]]]]]",
		elements
	);
}
public void testBug553149_6() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve15/src/X.java",
			"""
				public class X {
				    @SuppressWarnings("preview")
					   public void f(Object obj, boolean b) {
				        if ((y instanceof String /*not selecting */x_) && /* selecting*/x_.length() > 0) {
				            System.out.println(x_.toLowerCase());
				        }
				    }
				}""");
	String str = this.wc.getSource();
	String selection = "x_";
	int start = str.indexOf(selection);
	int length = "x_".length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"x_ [in f(Object, boolean) [in X [in [Working copy] X.java [in <default> [in src [in Resolve15]]]]]]",
		elements
	);
}
public void testBug574697() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Resolve/src/Test2.java",
			"""
				import java.util.List;
				import java.util.ArrayList;
				public class Test2 {
				    public String getGreeting() {
				        return "foo";
				    }
				    public static void main(String[] args) {
				        List<Integer> foo = new List<>() {
				            private void test() {
				                new Test2().getGreeting();
				            }
				        };
				    }
				}
				""");

	String str = this.workingCopies[0].getSource();
	String selectAt = "getGreeting()";
	String selection = "getGreeting";
	int start = str.lastIndexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length, this.wcOwner);

	assertElementsEqual(
			"Unexpected elements",
			"getGreeting() [in Test2 [in [Working copy] Test2.java [in <default> [in src [in Resolve]]]]]",
			elements);
}
public void testBugDiamond() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Resolve/src/Test2.java",
			"""
				import java.util.List;
				public class Test2 {
				    public static void test() {
				        List<Integer> foo = new List<>() {\
				          String s2;
				            private void test() {
				                System.out.println(s2);
				            }
				        };
				    }
				}
				""");

	String str = this.workingCopies[0].getSource();
	String selectAt = "s2";
	String selection = "s2";
	int start = str.lastIndexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length, this.wcOwner);

	assertElementsEqual(
			"Unexpected elements",
			"s2 [in <anonymous #1> [in test() [in Test2 [in [Working copy] Test2.java [in <default> [in src [in Resolve]]]]]]]",
			elements);
}
public void testBug577508_1() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve15/src/X.java",
			"""
				public class X {
				    public X () {
						new Runnable() {
							public void run () {
								Object object = null;
								if (object instanceof Thread thread) thread.start();
								tryToOpenDeclarationOnThisMethod();
							}
						};
					}
					public void tryToOpenDeclarationOnThisMethod () {
					}
				}""");
	String str = this.wc.getSource();
	String selection = "tryToOpenDeclarationOnThisMethod";
	int start = str.indexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"tryToOpenDeclarationOnThisMethod() [in X [in [Working copy] X.java [in <default> [in src [in Resolve15]]]]]",
		elements
	);
}
public void testBug577508_2() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve15/src/X.java",
			"""
				public class X {
				    public X () {
						for (Object object : new Object[] {"test"}) {
							if (object instanceof String string) {
								System.out.println(string);
								tryToOpenDeclarationOnThisMethod();
							}
						}
					}
					static public void tryToOpenDeclarationOnThisMethod () {
					}
				}""");
	String str = this.wc.getSource();
	String selection = "tryToOpenDeclarationOnThisMethod";
	int start = str.indexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"tryToOpenDeclarationOnThisMethod() [in X [in [Working copy] X.java [in <default> [in src [in Resolve15]]]]]",
		elements
	);
}
public void testBug577508_3() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve15/src/X.java",
			"""
				public class X {
				  public static void main(String[] args) {
				    Set<Foo> foos = Set.of(new Foo(), new Bar());
				    for (Foo foo : foos) {
				      String string;
				      if (foo instanceof Bar bar) {
				        string = "__";
				      }
				    }
				    String[] names = new String[] {};
				    for (String name : names) {
				      int size = name.length();
				    }
				  }
				  static class Foo {}
				  static class Bar extends Foo {}
				}""");
	String str = this.wc.getSource();
	String selection = "length";
	int start = str.indexOf(selection);
	int length = "length".length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"length() [in String [in String.class [in java.lang [in "+ getExternalPath() + "jclMin14.jar]]]]",
		elements
	);
}
public void testBug577508_4() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve15/src/X.java",
			"""
				public class X {
				  static public void main (String[] args) {
					Object[] objects = new Object[3];
					for (Object object : objects)\s
						if (object instanceof String string && !(object instanceof Runnable))\s
							System.out.println(); // Open Declaration fails here if you remove the braces from the for loop.
					System.out.println(); // Open Declaration always fails here.
				}
				}""");
	String str = this.wc.getSource();
	String selection = "println";
	int start = str.indexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"println(java.lang.String) [in PrintStream [in PrintStream.class [in java.io [in "+ getExternalPath() + "jclMin14.jar]]]]",
		elements
	);

	str = this.wc.getSource();
	start = str.lastIndexOf(selection);
	length = selection.length();
	elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"println(java.lang.String) [in PrintStream [in PrintStream.class [in java.io [in "+ getExternalPath() + "jclMin14.jar]]]]",
		elements
	);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1278
// Wrong method redirect
public void testGH1278() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve15/src/TestSelect.java",
					"""
						public class TestSelect {
						    class Integer {}
						    class Double {}
						
							public void foo(String s) {
						
							}
						
							public void foo(Integer i) {
						
							}
						
							public void foo(Double d) {
						
							}
						
							public void foo2(Integer i) {
								Object test = 1d;
								if (test instanceof Double test2) {
									foo(test2);
								}
							}
						
						}
						""");
	String str = this.wc.getSource();
	String selection = "foo";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"foo(Double) [in TestSelect [in [Working copy] TestSelect.java [in <default> [in src [in Resolve15]]]]]",
		elements
	);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1288
// Open Declaration (F3) sometimes not working for "Pattern Matching for instanceof
public void testGH1288() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve15/src/DetectVMInstallationsJob.java",
					"""
						public class X {
							public void test(Object s) {
								if(s instanceof String x) {
									x./*fails1*/length();
								}
							}
							public void test2(Object s) {
								if(s instanceof String x)
									x./*works1*/length();
							}
						\t
							public void foo(Object s) {
							\t
								if (s instanceof String x) {
									x./*fails2*/length(); x./*works2*/length();
								}
						
								if (s instanceof String x) {
									x./*fails3*/length(); x./*works3*/length(); x./*works4*/length();
								}
						
								if (s instanceof String x) {
									int i; x./*works5*/length(); // works
								}
						
								if (s instanceof String x) {
									int i; x./*fails4*/length(); int j; // fails
								}
						
								if (s instanceof String x) {
									x./*fails5*/length(); int j; // fails
								}
							}
						}
						""");
	String str = this.wc.getSource();
	String selection = "/*fails1*/length";
	int start = str.indexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"length() [in String [in String.class [in java.lang [in "+ getExternalPath() + "jclMin14.jar]]]]",
		elements
	);
	selection = "/*fails2*/length";
	start = str.indexOf(selection);
	elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"length() [in String [in String.class [in java.lang [in "+ getExternalPath() + "jclMin14.jar]]]]",
		elements
	);
	selection = "/*fails3*/length";
	start = str.indexOf(selection);
	elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"length() [in String [in String.class [in java.lang [in "+ getExternalPath() + "jclMin14.jar]]]]",
		elements
	);
	selection = "/*fails4*/length";
	start = str.indexOf(selection);
	elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"length() [in String [in String.class [in java.lang [in "+ getExternalPath() + "jclMin14.jar]]]]",
		elements
	);
	selection = "/*fails5*/length";
	start = str.indexOf(selection);
	elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"length() [in String [in String.class [in java.lang [in "+ getExternalPath() + "jclMin14.jar]]]]",
		elements
	);
	selection = "/*works1*/length";
	start = str.indexOf(selection);
	elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"length() [in String [in String.class [in java.lang [in "+ getExternalPath() + "jclMin14.jar]]]]",
		elements
	);
	selection = "/*works2*/length";
	start = str.indexOf(selection);
	elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"length() [in String [in String.class [in java.lang [in "+ getExternalPath() + "jclMin14.jar]]]]",
		elements
	);
	selection = "/*works3*/length";
	start = str.indexOf(selection);
	elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"length() [in String [in String.class [in java.lang [in "+ getExternalPath() + "jclMin14.jar]]]]",
		elements
	);
	selection = "/*works4*/length";
	start = str.indexOf(selection);
	elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"length() [in String [in String.class [in java.lang [in "+ getExternalPath() + "jclMin14.jar]]]]",
		elements
	);
	selection = "/*works5*/length";
	start = str.indexOf(selection);
	elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"length() [in String [in String.class [in java.lang [in "+ getExternalPath() + "jclMin14.jar]]]]",
		elements
	);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1288
// Open Declaration (F3) sometimes not working for "Pattern Matching for instanceof
public void testGH1288_while() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve15/src/X.java",
					"""
						public class X {
							public void test(Object s) {
								while(s instanceof String x) {
									x.hashCode();
								}
								while(s.hashCode()) {
									System.out.println();
								}
								while(s instanceof String x && x.length() > 0) {
									System.out.println();
									x.length();
								}
								while(s instanceof String xyz && xyz == "abc") {
									System.out.println();
								}
							}
						}
						""" );
	String str = this.wc.getSource();
	String selection = "length";
	int start = str.indexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"length() [in String [in String.class [in java.lang [in "+ getExternalPath() + "jclMin14.jar]]]]",
		elements
	);
	start = str.lastIndexOf(selection);
	length = selection.length();
	elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"length() [in String [in String.class [in java.lang [in "+ getExternalPath() + "jclMin14.jar]]]]",
		elements
	);

	selection = "xyz";
	start = str.lastIndexOf(selection);
	length = selection.length();
	elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"xyz [in test(Object) [in X [in [Working copy] X.java [in <default> [in src [in Resolve15]]]]]]",
		elements
	);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1288
// Open Declaration (F3) sometimes not working for "Pattern Matching for instanceof
public void testGH1288_do_while() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve15/src/X.java",
			"""
				import java.util.ArrayList;
				
				public class X {
					public void foo(ArrayList<Object> alo) {
						int i = 0;
						do {
						}	while (!(alo.get(i) instanceof String patVar) || /*here*/patVar.length() > 0);
						patVar.hashCode();
					}
				}
				""" );
	String str = this.wc.getSource();
	String selection = "/*here*/patVar";
	int start = str.indexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"patVar [in foo(ArrayList<Object>) [in X [in [Working copy] X.java [in <default> [in src [in Resolve15]]]]]]",
		elements
	);
	selection = "patVar";
	start = str.lastIndexOf(selection);
	length = selection.length();
	elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"patVar [in foo(ArrayList<Object>) [in X [in [Working copy] X.java [in <default> [in src [in Resolve15]]]]]]",
		elements
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=573257
// Errors when using instanceof pattern inside enum
public void testBug573257() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve15/src/X.java",
					"""
						public enum ASD {
						
							A1 {
								void f(Object o) {
									if (o instanceof String s) {
										System.out.println(s);
									}
								}
							}
						}
						""");
	String str = this.wc.getSource();
	String selection = "System";
	int start = str.indexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"System [in System.class [in java.lang [in " + getExternalPath() + "jclMin14.jar]]]",
		elements
	);
	selection = "out";
	start = str.indexOf(selection);
	length = selection.length();
	elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"out [in System [in System.class [in java.lang [in " + getExternalPath() + "jclMin14.jar]]]]",
		elements
	);
	selection = "println";
	start = str.indexOf(selection);
	length = selection.length();
	elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"println(java.lang.String) [in PrintStream [in PrintStream.class [in java.io [in " + getExternalPath() + "jclMin14.jar]]]]",
		elements
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=576794
// Class rename fails with ClassCastException
public void testBug576794() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve15/src/RenameFails.java",
					"""
						import java.lang.annotation.Annotation;
						
						public class RenameFails {
						
						    private final static ClassValue<RenameFails> STUFF = new ClassValue<>() {
						
						        @Override
						        protected RenameFails computeValue(Class<?> type) {
						            for (Annotation a : type.getAnnotations()) {
						                if (a instanceof Deprecated h) {
						                \t
						                }
						            }
						            return null;
						        }
						    };
						}
						""");
	String str = this.wc.getSource();
	String selection = "Deprecated";
	int start = str.indexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Deprecated [in Deprecated.class [in java.lang [in " + getExternalPath() + "jclMin14.jar]]]",
		elements
	);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1263
// CCE: LocalDeclaration cannot be cast to class ForeachStatement
public void testGH1263() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve15/src/DetectVMInstallationsJob.java",
					"""
						import java.util.Set;
						import java.util.function.Predicate;
						
						public class DetectVMInstallationsJob  {
						\t
							interface Collection<E> extends Iterable<E> {
								default boolean removeIf(Predicate<? super E> filter) {
									return true;
							    }
							}
							public interface Predicate<T> {
							    boolean test(T t);
							}
						
							protected void run() {
								Collection<String> candidates = null;
								Set<Object> knownVMs = null;
								Collection<Object> systemVMs = null;
								if ("".equals("")) {
										systemVMs = null;
										systemVMs.removeIf(t -> knownVMs.contains(null));
										for (int systemVM : new int[] { 10 }) {
											candidates.removeIf(t -> t.equals(null));
										}
								}
								for (int f : new int [] {}) {
									String install = null;
									if (!(install instanceof String vm && vm.hashCode() != 0)) {
									}
								}
							}
						}
						""");
	String str = this.wc.getSource();
	String selection = "->";
	int start = str.indexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"test(T) [in Predicate [in DetectVMInstallationsJob [in [Working copy] DetectVMInstallationsJob.java [in <default> [in src [in Resolve15]]]]]]",
		elements
	);
	start = str.lastIndexOf(selection);
	elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"test(T) [in Predicate [in DetectVMInstallationsJob [in [Working copy] DetectVMInstallationsJob.java [in <default> [in src [in Resolve15]]]]]]",
		elements
	);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1364
// SelectionParser behavior erratic wrt to live pattern variables upon loop exit
public void testGH1364() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve15/src/X.java",
					"""
						public class X {
							String xx = "Hello";
							void foo(Object o) {
								do {
								\t
								} while (!(o instanceof X xxx));
								xxx.foo(o); // F3 on xxx fails
								while (!(o instanceof X yyy)) {
								\t
								}
								yyy.foo(o); // F3 on yyy works ok
							}
						}
						""");
	String str = this.wc.getSource();
	String selection = "xxx.foo";
	int start = str.indexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"foo(Object) [in X [in [Working copy] X.java [in <default> [in src [in Resolve15]]]]]",
		elements
	);
	selection = "yyy.foo";
	start = str.lastIndexOf(selection);
	elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"foo(Object) [in X [in [Working copy] X.java [in <default> [in src [in Resolve15]]]]]",
		elements
	);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1360
// SelectionParser miscomputes type of variable
public void testGH1360() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve15/src/X.java",
					"""
						public class X {
							String xx = "Hello";
							void foo(Object o) {
								if (o instanceof X xx) {
									/*pattern*/xx.foo(o);
								} else {
									System.out.println(/*field*/xx);  // F3 on xx jumps wrongly to to o instanceof X xx
								}
							}
						}
						""");
	String str = this.wc.getSource();
	String selection = "/*pattern*/xx";
	int start = str.indexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"xx [in foo(Object) [in X [in [Working copy] X.java [in <default> [in src [in Resolve15]]]]]]",
		elements
	);
	selection = "/*field*/xx";
	start = str.indexOf(selection);
	length = selection.length();
	elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"xx [in X [in [Working copy] X.java [in <default> [in src [in Resolve15]]]]]",
		elements
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=567497
//  [15] Search for declaration of pattern variable not working
public void testBug567497() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve15/src/X.java",
			"""
				public class X {
				    protected Object y = "FIELD X";
				    @SuppressWarnings("preview")
					public void f(Object obj, boolean b) {
				        if ((y instanceof String /*not selecting */x) && /* selecting*/x.length() > 0) {
				            System.out.println(x.toLowerCase());
				        }
				    }
				}
				""");
	String str = this.wc.getSource();
	String selection = "/* selecting*/x";
	int start = str.indexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"x [in f(Object, boolean) [in X [in [Working copy] X.java [in <default> [in src [in Resolve15]]]]]]",
		elements
	);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1420
// Sporadic errors reported for Java hover or Ctrl+Click
public void testGH1420() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve15/src/X.java",
					"""
                    public class X {\n" +

					    class Job {}

						public void foo(boolean b) {
							if (!b) {
							} else {
								Job j = new Job() {
									protected void run() {
										/*here*/getTarget();
									}
									void getTarget() {
									}
								};
							}
						}
					}
					""");
	String str = this.wc.getSource();
	String selection = "/*here*/getTarget";
	int start = str.indexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"getTarget() [in <anonymous #1> [in foo(boolean) [in X [in [Working copy] X.java [in <default> [in src [in Resolve15]]]]]]]",
		elements
	);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1420
// Sporadic errors reported for Java hover or Ctrl+Click
public void testGH1420_2() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve15/src/X.java",
					"""
                 public class X {\n" +

					    class Job {}

						public void foo(Job j) {}

						public void foo(boolean b) {
							if (!b) {
							} else {
								foo(new Job() {
									protected void run() {
										/*here*/getTarget();
									}
									void getTarget() {
									}
								});
							}
						}
					}
					""");
	String str = this.wc.getSource();
	String selection = "/*here*/getTarget";
	int start = str.indexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"getTarget() [in <anonymous #1> [in foo(boolean) [in X [in [Working copy] X.java [in <default> [in src [in Resolve15]]]]]]]",
		elements
	);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1420
// Sporadic errors reported for Java hover or Ctrl+Click
public void testGH1420_3() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve15/src/X.java",
					"""
              public class X {\n" +

					    class Job {}

						public void foo(Job j) {}

						public void foo(boolean b) {
							if (!b) {
							} else {
								class LocalClass {
									protected void run() {
										/*here*/getTarget();
									}
									void getTarget() {
									}
								};
							}
						}
					}
					""");
	String str = this.wc.getSource();
	String selection = "/*here*/getTarget";
	int start = str.indexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"getTarget() [in LocalClass [in foo(boolean) [in X [in [Working copy] X.java [in <default> [in src [in Resolve15]]]]]]]",
		elements
	);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/860
// Field assignment to anonymous type breaks selection
public void testGH860() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve15/src/eclipse_bug/CurrentTextSelectionCannotBeOpened.java",
					"""
           			package eclipse_bug;

					public class CurrentTextSelectionCannotBeOpened {
						public static class Super {
							public boolean boolMethod(){return true;}
						}

						Object obj;
						public boolean somecode(){
							obj=new Object(){
								public boolean somecode(){
									Super sup=new Super();
									return sup.boolMethod();
								}
							};
							return false;
						}
					}
					""");
	String str = this.wc.getSource();
	String selection = "boolMethod";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"boolMethod() [in Super [in CurrentTextSelectionCannotBeOpened [in [Working copy] CurrentTextSelectionCannotBeOpened.java [in eclipse_bug [in src [in Resolve15]]]]]]",
		elements
	);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1568
// Current text selection cannot be opened in an editor
public void testGH1568() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve15/src/RowRenderData.java",
			"""
			import java.util.ArrayList;
			import java.util.List;

			public class RowRenderData {

			    private List<Object> cells = new ArrayList<>();

			    public List<Object> getCells() {
			        return cells;
			    }

			    public void setCells(List<Object> cells) {
			        this.cells = cells;
			    }

			    public static void main(String[] args) {
			        List<RowRenderData> rows = new ArrayList<>();
			        if (true) {
			            for (RowRenderData row : rows) {
			                row.getCells();
			            }
			        }
			    }
			}
			""");
	String str = this.wc.getSource();
	String selection = "row";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"row [in main(String[]) [in RowRenderData [in [Working copy] RowRenderData.java [in <default> [in src [in Resolve15]]]]]]",
		elements
	);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1568
// Current text selection cannot be opened in an editor
public void testGH1568_2() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve15/src/X.java",
			"""
			public class X {
				Integer abcdef = 10;
			    public void main(String argv[]) {
			        Object o = argv;
			        if (!(o instanceof String [] abcdef)) {
			        	if (abcdef == null) {

			        	}
			        } else {
			        	if (abcdef.length > 0) {

			        	}
			        }
			    }
			}
			""");
	String str = this.wc.getSource();
	String selection = "abcdef";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"abcdef [in main(String[]) [in X [in [Working copy] X.java [in <default> [in src [in Resolve15]]]]]]",
		elements
	);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1568
// Current text selection cannot be opened in an editor
public void testGH1568_3() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve15/src/X.java",
			"""
			public class X {

				public void main(String argv[]) {
					Object o = argv;
					if (o instanceof String[] abcdef) {
						if (o != null) {
							if (argv[0] instanceof String str) {
							}
						} else {
							if (abcdef.length > 0) {


							}
						}
					}
				}
			}
			""");
	String str = this.wc.getSource();
	String selection = "abcdef";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"abcdef [in main(String[]) [in X [in [Working copy] X.java [in <default> [in src [in Resolve15]]]]]]",
		elements
	);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1568
// Current text selection cannot be opened in an editor
public void testGH1568_4() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve15/src/X.java",
			"""
			public class X {
				Integer abcdef = 10;
			    public void main(String argv[]) {
			        Object o = argv;
			        if (!(o instanceof String [] abcdef)) {
			        	if (abcdef == null) {

			        	}
			        } else {
			        }
			    }
			}
			""");
	String str = this.wc.getSource();
	String selection = "abcdef";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"abcdef [in X [in [Working copy] X.java [in <default> [in src [in Resolve15]]]]]",
		elements
	);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1568
// Current text selection cannot be opened in an editor
public void testGH1568_5() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve15/src/X.java",
			"""
			import java.util.ArrayList;
			import java.util.List;

			public class RowRenderData {

			    private List<Object> cells = new ArrayList<>();

			    public List<Object> getCells() {
			        return cells;
			    }

			    public void setCells(List<Object> cells) {
			        this.cells = cells;
			    }

			    public static void main(String[] args) {
			        List<RowRenderData> rows = new ArrayList<>();
			        if (true) {
			            for (RowRenderData row = new RowRenderData(); row != null;) {
			                row.getCells();
			            }
			        }
			    }
			}
			""");
	String str = this.wc.getSource();
	String selection = "row";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"row [in main(String[]) [in RowRenderData [in [Working copy] X.java [in <default> [in src [in Resolve15]]]]]]",
		elements
	);
}
}
