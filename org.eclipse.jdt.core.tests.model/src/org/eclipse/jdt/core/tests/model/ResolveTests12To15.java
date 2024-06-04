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
	this.wc = getWorkingCopy("/Resolve/src/X.java","public class X {\n" +
	"static final String ONE=\"One\", TWO = \"Two\", THREE=\"Three\";\n" +
	"  public static void foo(String num) {\n" +
	" 	 switch (num) {\n" +
	"	   case ONE, TWO, THREE:\n" +
	"		 System.out.println(num);\n" +
	"		 break;\n" +
	"    }" +
	"  }\n" +
	"}\n");

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
	this.wc = getWorkingCopy("/Resolve/src/X.java","public class X {\n" +
	"  public static void foo(Num num) {\n" +
	" 	 switch (num) {\n" +
	"	   case ONE, TWO, THREE:\n" +
	"		 System.out.println(num);\n" +
	"		 break;\n" +
	"    }" +
	"  }\n" +
	"	enum Num { ONE, TWO, THREE;}\n" +
	"}\n");
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
	this.wc = getWorkingCopy("/Resolve/src/X.java","public class X {\n" +
	"static final String ONE=\"One\", TWO = \"Two\", THREE=\"Three\";\n" +
	"  public static void foo(String num) {\n" +
	" 	 switch (num) {\n" +
	"	   case ONE, TWO, THREE:\n" +
	"		 System.out.println(num);\n" +
	"		 break;\n" +
	"    }" +
	"  }\n" +
	"}\n");
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
	this.wc = getWorkingCopy("/Resolve/src/X.java","public class X {\n" +
	"  public static void foo(Num num) {\n" +
	" 	 switch (num) {\n" +
	"	   case ONE, TWO, THREE:\n" +
	"		 System.out.println(num);\n" +
	"		 break;\n" +
	"    }" +
	"  }\n" +
	"	enum Num { ONE, TWO, THREE;}\n" +
	"}\n");
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
	this.wc = getWorkingCopy("/Resolve/src/X.java","public class X {\n" +
	"static final String ONE=\"One\", TWO = \"Two\", THREE=\"Three\";\n" +
	"  public static void foo(String num) {\n" +
	" 	 switch (num) {\n" +
	"	   case ONE, TWO, THREE ->\n" +
	"		 System.out.println(num);\n" +
	"    }" +
	"  }\n" +
	"}\n");
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
	this.wc = getWorkingCopy("/Resolve/src/X.java","public class X {\n" +
	"  public static void foo(Num num) {\n" +
	" 	 switch (num) {\n" +
	"	   case ONE, TWO, THREE ->\n" +
	"		 System.out.println(num);\n" +
	"		 break; // illegal, but should be ignored and shouldn't matter\n" +
	"    }" +
	"  }\n" +
	"	enum Num { ONE, TWO, THREE;}\n" +
	"}\n");
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
	this.wc = getWorkingCopy("/Resolve/src/X.java","public class X {\n" +
	"static final String ONE=\"One\", TWO = \"Two\", THREE=\"Three\";\n" +
	"  public static void foo(String num) {\n" +
	" 	 switch (num) {\n" +
	"	   case ONE, TWO, THREE ->\n" +
	"		 System.out.println(num);\n" +
	"		 break;\n" +
	"    }" +
	"  }\n" +
	"}\n");
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
	this.wc = getWorkingCopy("/Resolve/src/X.java","public class X {\n" +
	"  public static void foo(Num num) {\n" +
	" 	 switch (num) {\n" +
	"	   case ONE, TWO, THREE ->\n" +
	"		 System.out.println(num);\n" +
	"		 break;\n" +
	"    }" +
	"  }\n" +
	"	enum Num { ONE, TWO, THREE;}\n" +
	"}\n");
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
	this.wc = getWorkingCopy("/Resolve/src/X.java","public class X {\n" +
	"  public static void foo(Num num_) {\n" +
	" 	 switch (num_) {\n" +
	"	   case ONE, TWO, THREE ->\n" +
	"		 System.out.println(num_);\n" +
	"		 break;\n" +
	"    }" +
	"  }\n" +
	"	enum Num { ONE, TWO, THREE;}\n" +
	"}\n");
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
	this.wc = getWorkingCopy("/Resolve/src/X.java","public class X {\n" +
	"  public static void foo(Num num_) {\n" +
	" 	 switch (num_) {\n" +
	"	   case ONE, TWO, THREE -> {\n" +
	"		 int i_j = 0;" +
	"		 System.out.println(i_j);\n" +
	"		 break;" +
	"		 }\n" +
	"    }" +
	"  }\n" +
	"	enum Num { ONE, TWO, THREE;}\n" +
	"}\n");
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
	this.wc = getWorkingCopy("/Resolve/src/X.java","public class X {\n" +
	"  public static void foo(Num num_) {\n" +
	" 	 switch (num_) {\n" +
	"	   case ONE, TWO, THREE -> {\n" +
	"		 break;" +
	"		 }\n" +
	"    }" +
	"  }\n" +
	"	enum Num { ONE, TWO, THREE;}\n" +
	"}\n");
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
	this.wc = getWorkingCopy("/Resolve/src/X.java","public class X {\n" +
	"  public static void foo(int num_) {\n" +
	" 	 switch (num_ + 1) {\n" +
	"	   case 1, 2, 3 -> {\n" +
	"		 break;" +
	"		 }\n" +
	"    }" +
	"  }\n" +
	"}\n");
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
	this.wc = getWorkingCopy("/Resolve/src/X.java","public class X {\n" +
	"  public static void foo(int num_) {\n" +
	" 	 int i = switch (num_) {\n" +
	"	   case 1, 2, 3 -> (num_ + 1);\n" +
	"      default -> 0;\n" +
	"    }" +
	"  }\n" +
	"}\n");
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
	this.wc = getWorkingCopy("/Resolve/src/X.java","public class X {\n" +
	"  public static void foo(int num_) {\n" +
	" 	 int i = switch (num_) {\n" +
	"	   case 1, 2, 3 -> 0;\n" +
	"      default -> (num_ + 1);\n" +
	"    }" +
	"  }\n" +
	"}\n");
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
	this.wc = getWorkingCopy("/Resolve/src/X.java","public class X {\n" +
	"  public static void foo(int num_) {\n" +
	" 	 int i = switch (num_) {\n" +
	"	   case 1, 2, 3 -> 0;\n" +
	"      default -> (num_ + 1);\n" +
	"    }" +
	"  }\n" +
	"}\n");
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
	this.wc = getWorkingCopy("/Resolve/src/X.java","public class X {\n" +
			"	public void bar(int s) {\n" +
			"		int i_j = switch (s) {\n" +
			"			case 1, 2, 3 -> (s+1);\n" +
			"			default -> i_j;\n" +
			"		};\n" +
			"	}\n" +
			"}\n");
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
	this.wc = getWorkingCopy("/Resolve/src/X.java","public class X {\n" +
			"	public void bar(int s) {\n" +
			"		int i_j = switch (s) {\n" +
			"			case 1, 2, 3 -> (s+1);\n" +
			"			default -> (1+i_j);\n" +
			"		};\n" +
			"	}\n" +
			"}\n");
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
			"import java.util.function.*;\n" +
			"interface IN0 {} \n" +
			"interface IN1 extends IN0 {} \n" +
			"interface IN2 extends IN0 {}\n" +
			"public class X {\n" +
			"	 IN1 n_1() { return new IN1() {}; } \n" +
			"	IN2 n_2() { return null; } \n" +
			"	<M> void m( Supplier< M> m2) { } \n" +
			"	void testSw(int i) { \n" +
			"		m(switch(i) { \n" +
			"			case 1 -> this::n_1; \n" +
			"			default -> this::n_2; }); \n" +
			"	}\n" +
			"}\n");
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
			"import java.util.function.*;\n" +
			"interface IN0 {} \n" +
			"interface IN1 extends IN0 {} \n" +
			"interface IN2 extends IN0 {}\n" +
			"public class X {\n" +
			"	 IN1 n_1() { return new IN1() {}; } \n" +
			"	IN2 n_2() { return null; } \n" +
			"	<M> void m( Supplier< M> m2) { } \n" +
			"	void testSw(int i) { \n" +
			"		m(switch(i) { \n" +
			"			case 2 -> () -> n_1(); \n" +
			"			default -> this::n_2; }); \n" +
			"	}\n" +
			"}\n");
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
			"import java.util.function.*;\n" +
			"interface IN0 {} \n" +
			"interface IN1 extends IN0 {} \n" +
			"interface IN2 extends IN0 {}\n" +
			"public class X {\n" +
			"	 IN1 n_1() { return new IN1() {}; } \n" +
			"	IN2 n_2() { return null; } \n" +
			"	<M> void m( Supplier< M> m2) { } \n" +
			"	void testSw(int i) { \n" +
			"		m(switch(i) { \n" +
			"			default -> this::n_2; }); \n" +
			"	}\n" +
			"}\n");
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
			"import java.util.function.*;\n" +
			"interface IN0 {} \n" +
			"interface IN1 extends IN0 {} \n" +
			"interface IN2 extends IN0 {}\n" +
			"public class X {\n" +
			"	 IN1 n_1(int ijk) { return new IN1() {}; } \n" +
			"	IN2 n_2() { return null; } \n" +
			"	<M> void m( Supplier< M> m2) { } \n" +
			"	void testSw(int ijk) { \n" +
			"		m(switch(ijk) { \n" +
			"			default -> () -> n_1(ijk); }); \n" +
			"	}\n" +
			"}\n");
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
			"import java.util.function.*;\n" +
			"interface IN0 {} \n" +
			"interface IN1 extends IN0 {} \n" +
			"interface IN2 extends IN0 {}\n" +
			"public class X {\n" +
			"	 IN1 n_1(int ijk) { return new IN1() {}; } \n" +
			"	IN2 n_2() { return null; } \n" +
			"	<M> void m( Supplier< M> m2) { } \n" +
			"	void testSw(int ijk) { \n" +
			"		m(switch(ijk) { \n" +
			"			default -> () -> n_1(ijk); }); \n" +
			"	}\n" +
			"}\n");
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
			"public class X {\n"
					+ "    protected Object x_ = \"FIELD X\";\n"
					+ "    @SuppressWarnings(\"preview\")\n"
					+ "	   public void f(Object obj, boolean b) {\n"
					+ "        if ((x_ instanceof String y) && y.length() > 0) {\n"
					+ "            System.out.println(y.toLowerCase());\n"
					+ "        }\n"
					+ "    }\n"
					+ "}");
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
			"public class X {\n"
					+ "    protected Object x_ = \"FIELD X\";\n"
					+ "    @SuppressWarnings(\"preview\")\n"
					+ "	   public void f(Object obj, boolean b) {\n"
					+ "        if ((x_ instanceof String y_) && y_.length() > 0) {\n"
					+ "            System.out.println(y.toLowerCase());\n"
					+ "        }\n"
					+ "    }\n"
					+ "}");
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
			"public class X {\n"
					+ "    protected Object x_ = \"FIELD X\";\n"
					+ "    @SuppressWarnings(\"preview\")\n"
					+ "	   public void f(Object obj, boolean b) {\n"
					+ "        if ((x_ instanceof String x_) && x_.length() > 0) {\n"
					+ "            System.out.println(y.toLowerCase());\n"
					+ "        }\n"
					+ "    }\n"
					+ "}");
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
			"public class X {\n"
					+ "    protected Object x_ = \"FIELD X\";\n"
					+ "    @SuppressWarnings(\"preview\")\n"
					+ "	   public void f(Object obj, boolean b) {\n"
					+ "        if ((x_ instanceof String x_) && x_.length() > 0) {\n"
					+ "            System.out.println(x_.toLowerCase());\n"
					+ "        }\n"
					+ "    }\n"
					+ "}");
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
			"public class X {\n"
					+ "    protected Object x_ = \"FIELD X\";\n"
					+ "    @SuppressWarnings(\"preview\")\n"
					+ "	   public void f(Object obj, boolean b) {\n"
					+ "        if ((x_ instanceof String x_) && x_.length() > 0) {\n"
					+ "            System.out.println(x_.toLowerCase());\n"
					+ "        }\n"
					+ "        System.out.println(x_.toLowerCase());\n"
					+ "    }\n"
					+ "}");
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
			"public class X {\n"
					+ "    @SuppressWarnings(\"preview\")\n"
					+ "	   public void f(Object obj, boolean b) {\n"
					+ "        if ((y instanceof String /*not selecting */x_) && /* selecting*/x_.length() > 0) {\n"
					+ "            System.out.println(x_.toLowerCase());\n"
					+ "        }\n"
					+ "    }\n"
					+ "}");
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
			"import java.util.List;\n" +
			"import java.util.ArrayList;\n" +
			"public class Test2 {\n" +
			"    public String getGreeting() {\n" +
			"        return \"foo\";\n" +
			"    }\n" +
			"    public static void main(String[] args) {\n" +
			"        List<Integer> foo = new List<>() {\n" +
			"            private void test() {\n" +
			"                new Test2().getGreeting();\n" +
			"            }\n" +
			"        };\n" +
			"    }\n" +
			"}\n");

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
			"import java.util.List;\n" +
			"public class Test2 {\n" +
			"    public static void test() {\n" +
			"        List<Integer> foo = new List<>() {"
			+ "          String s2;\n" +
			"            private void test() {\n" +
			"                System.out.println(s2);\n" +
			"            }\n" +
			"        };\n" +
			"    }\n" +
			"}\n");

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
			"public class X {\n"
					+ "    public X () {\n"
					+ "		new Runnable() {\n"
					+ "			public void run () {\n"
					+ "				Object object = null;\n"
					+ "				if (object instanceof Thread thread) thread.start();\n"
					+ "				tryToOpenDeclarationOnThisMethod();\n"
					+ "			}\n"
					+ "		};\n"
					+ "	}\n"
					+ "	public void tryToOpenDeclarationOnThisMethod () {\n"
					+ "	}\n"
					+ "}");
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
			"public class X {\n"
					+ "    public X () {\n"
					+ "		for (Object object : new Object[] {\"test\"}) {\n"
					+ "			if (object instanceof String string) {\n"
					+ "				System.out.println(string);\n"
					+ "				tryToOpenDeclarationOnThisMethod();\n"
					+ "			}\n"
					+ "		}\n"
					+ "	}\n"
					+ "	static public void tryToOpenDeclarationOnThisMethod () {\n"
					+ "	}\n"
					+ "}");
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
			"public class X {\n"
			+ "  public static void main(String[] args) {\n"
			+ "    Set<Foo> foos = Set.of(new Foo(), new Bar());\n"
			+ "    for (Foo foo : foos) {\n"
			+ "      String string;\n"
			+ "      if (foo instanceof Bar bar) {\n"
			+ "        string = \"__\";\n"
			+ "      }\n"
			+ "    }\n"
			+ "    String[] names = new String[] {};\n"
			+ "    for (String name : names) {\n"
			+ "      int size = name.length();\n"
			+ "    }\n"
			+ "  }\n"
			+ "  static class Foo {}\n"
			+ "  static class Bar extends Foo {}\n"
			+ "}");
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
			"public class X {\n"
			+ "  static public void main (String[] args) {\n"
			+ "	Object[] objects = new Object[3];\n"
			+ "	for (Object object : objects) \n"
			+ "		if (object instanceof String string && !(object instanceof Runnable)) \n"
			+ "			System.out.println(); // Open Declaration fails here if you remove the braces from the for loop.\n"
			+ "	System.out.println(); // Open Declaration always fails here.\n"
			+ "}\n"
			+ "}");
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
					"public class TestSelect {\n" +
	                "    class Integer {}\n" +
					"    class Double {}\n" +
					"\n" +
					"	public void foo(String s) {\n" +
					"\n" +
					"	}\n" +
					"\n" +
					"	public void foo(Integer i) {\n" +
					"\n" +
					"	}\n" +
					"\n" +
					"	public void foo(Double d) {\n" +
					"\n" +
					"	}\n" +
					"\n" +
					"	public void foo2(Integer i) {\n" +
					"		Object test = 1d;\n" +
					"		if (test instanceof Double test2) {\n" +
					"			foo(test2);\n" +
					"		}\n" +
					"	}\n" +
					"\n" +
					"}\n");
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
					"public class X {\n" +
					"	public void test(Object s) {\n" +
					"		if(s instanceof String x) {\n" +
					"			x./*fails1*/length();\n" +
					"		}\n" +
					"	}\n" +
					"	public void test2(Object s) {\n" +
					"		if(s instanceof String x)\n" +
					"			x./*works1*/length();\n" +
					"	}\n" +
					"	\n" +
					"	public void foo(Object s) {\n" +
					"		\n" +
					"		if (s instanceof String x) {\n" +
					"			x./*fails2*/length(); x./*works2*/length();\n" +
					"		}\n" +
					"\n" +
					"		if (s instanceof String x) {\n" +
					"			x./*fails3*/length(); x./*works3*/length(); x./*works4*/length();\n" +
					"		}\n" +
					"\n" +
					"		if (s instanceof String x) {\n" +
					"			int i; x./*works5*/length(); // works\n" +
					"		}\n" +
					"\n" +
					"		if (s instanceof String x) {\n" +
					"			int i; x./*fails4*/length(); int j; // fails\n" +
					"		}\n" +
					"\n" +
					"		if (s instanceof String x) {\n" +
					"			x./*fails5*/length(); int j; // fails\n" +
					"		}\n" +
					"	}\n" +
					"}\n");
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
					"public class X {\n" +
					"	public void test(Object s) {\n" +
					"		while(s instanceof String x) {\n" +
					"			x.hashCode();\n" +
					"		}\n" +
					"		while(s.hashCode()) {\n" +
					"			System.out.println();\n" +
					"		}\n" +
					"		while(s instanceof String x && x.length() > 0) {\n" +
					"			System.out.println();\n" +
					"			x.length();\n" +
					"		}\n" +
					"		while(s instanceof String xyz && xyz == \"abc\") {\n" +
					"			System.out.println();\n" +
					"		}\n" +
					"	}\n" +
					"}\n" );
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
			"import java.util.ArrayList;\n" +
					"\n" +
					"public class X {\n" +
					"	public void foo(ArrayList<Object> alo) {\n" +
					"		int i = 0;\n" +
					"		do {\n" +
					"		}	while (!(alo.get(i) instanceof String patVar) || /*here*/patVar.length() > 0);\n" +
					"		patVar.hashCode();\n" +
					"	}\n" +
					"}\n" );
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
					"public enum ASD {\n" +
					"\n" +
					"	A1 {\n" +
					"		void f(Object o) {\n" +
					"			if (o instanceof String s) {\n" +
					"				System.out.println(s);\n" +
					"			}\n" +
					"		}\n" +
					"	}\n" +
					"}\n");
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
					"import java.lang.annotation.Annotation;\n" +
					"\n" +
					"public class RenameFails {\n" +
					"\n" +
					"    private final static ClassValue<RenameFails> STUFF = new ClassValue<>() {\n" +
					"\n" +
					"        @Override\n" +
					"        protected RenameFails computeValue(Class<?> type) {\n" +
					"            for (Annotation a : type.getAnnotations()) {\n" +
					"                if (a instanceof Deprecated h) {\n" +
					"                	\n" +
					"                }\n" +
					"            }\n" +
					"            return null;\n" +
					"        }\n" +
					"    };\n" +
					"}\n");
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
					"import java.util.Set;\n" +
					"import java.util.function.Predicate;\n" +
					"\n" +
					"public class DetectVMInstallationsJob  {\n" +
					"	\n" +
					"	interface Collection<E> extends Iterable<E> {\n" +
					"		default boolean removeIf(Predicate<? super E> filter) {\n" +
					"			return true;\n" +
					"	    }\n" +
					"	}\n" +
					"	public interface Predicate<T> {\n" +
					"	    boolean test(T t);\n" +
					"	}\n" +
					"\n" +
					"	protected void run() {\n" +
					"		Collection<String> candidates = null;\n" +
					"		Set<Object> knownVMs = null;\n" +
					"		Collection<Object> systemVMs = null;\n" +
					"		if (\"\".equals(\"\")) {\n" +
					"				systemVMs = null;\n" +
					"				systemVMs.removeIf(t -> knownVMs.contains(null));\n" +
					"				for (int systemVM : new int[] { 10 }) {\n" +
					"					candidates.removeIf(t -> t.equals(null));\n" +
					"				}\n" +
					"		}\n" +
					"		for (int f : new int [] {}) {\n" +
					"			String install = null;\n" +
					"			if (!(install instanceof String vm && vm.hashCode() != 0)) {\n" +
					"			}\n" +
					"		}\n" +
					"	}\n" +
					"}\n");
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
					"public class X {\n" +
					"	String xx = \"Hello\";\n" +
					"	void foo(Object o) {\n" +
					"		do {\n" +
					"			\n" +
					"		} while (!(o instanceof X xxx));\n" +
					"		xxx.foo(o); // F3 on xxx fails\n" +
					"		while (!(o instanceof X yyy)) {\n" +
					"			\n" +
					"		}\n" +
					"		yyy.foo(o); // F3 on yyy works ok\n" +
					"	}\n" +
					"}\n");
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
					"public class X {\n" +
					"	String xx = \"Hello\";\n" +
					"	void foo(Object o) {\n" +
					"		if (o instanceof X xx) {\n" +
					"			/*pattern*/xx.foo(o);\n" +
					"		} else {\n" +
					"			System.out.println(/*field*/xx);  // F3 on xx jumps wrongly to to o instanceof X xx\n" +
					"		}\n" +
					"	}\n" +
					"}\n");
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
			"public class X {\n" +
					"    protected Object y = \"FIELD X\";\n" +
					"    @SuppressWarnings(\"preview\")\n" +
					"	public void f(Object obj, boolean b) {\n" +
					"        if ((y instanceof String /*not selecting */x) && /* selecting*/x.length() > 0) {\n" +
					"            System.out.println(x.toLowerCase());\n" +
					"        }\n" +
					"    }\n" +
					"}\n");
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
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2443
// ArrayStoreException in SelectionParser.buildMoreCompletionContext
public void testGH2443() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve15/src/X.java",
					"""
					 public class X {

						public static void main(String[] args) {

							for (String sourcePath : args) {
								new X() {
									public void foo(String file) {
										file.hashCode();
									}

								};
							}
						}
					}
					""");
	String str = this.wc.getSource();
	String selection = "file";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"file [in foo(String) [in <anonymous #1> [in main(String[]) [in X [in [Working copy] X.java [in <default> [in src [in Resolve15]]]]]]]]",
		elements
	);
}
}
