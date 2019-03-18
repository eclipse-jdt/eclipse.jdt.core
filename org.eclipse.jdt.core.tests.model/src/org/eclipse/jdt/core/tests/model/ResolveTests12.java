/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
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

public class ResolveTests12 extends AbstractJavaModelTests {
	ICompilationUnit wc = null;

static {
//	 TESTS_NAMES = new String[] { "test018" };
	// TESTS_NUMBERS = new int[] { 124 };
	// TESTS_RANGE = new int[] { 16, -1 };
}
public static Test suite() {
	return buildModelTestSuite(ResolveTests12.class);
}
public ResolveTests12(String name) {
	super(name);
}
public ICompilationUnit getWorkingCopy(String path, String source) throws JavaModelException {
	return super.getWorkingCopy(path, source, this.wcOwner);
}
public void setUpSuite() throws Exception {
	super.setUpSuite();
	setUpJavaProject("Resolve", "12", false);
	waitUntilIndexesReady();
}
protected void setUp() throws Exception {
	super.setUp();
	this.wcOwner = new WorkingCopyOwner(){};
}
public void tearDownSuite() throws Exception {
	deleteProject("Resolve");
	super.tearDownSuite();
}

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
}
