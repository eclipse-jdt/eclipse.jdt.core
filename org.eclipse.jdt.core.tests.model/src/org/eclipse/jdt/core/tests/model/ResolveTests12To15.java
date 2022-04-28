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
public void _testBug553149_4() throws JavaModelException {
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
	String selection = "x_ instanceof";
	int start = str.lastIndexOf(selection);
	int length = "x_".length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"x_ [in X [in [Working copy] X.java [in <default> [in src [in Resolve15]]]]]",
		elements
	);
}
public void _testBug553149_5() throws JavaModelException {
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
}
