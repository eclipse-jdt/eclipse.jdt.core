/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.core.tests.model;

import junit.framework.Test;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;

public class ResolveTests18 extends AbstractJavaModelTests {
	ICompilationUnit wc = null;

static {
	// TESTS_NAMES = new String[] { "test0023" };
	// TESTS_NUMBERS = new int[] { 124 };
	// TESTS_RANGE = new int[] { 16, -1 };
}
public static Test suite() {
	return buildModelTestSuite(ResolveTests18.class);
}
public ResolveTests18(String name) {
	super(name);
}
public ICompilationUnit getWorkingCopy(String path, String source) throws JavaModelException {
	return super.getWorkingCopy(path, source, this.wcOwner);
}
public void setUpSuite() throws Exception {
	super.setUpSuite();

	setUpJavaProject("Resolve", "1.8");

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
// Test (positive): self static methods can be targetted by references.
public void test0001() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"interface I {\n" +
			"	void foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	static void goo() {}\n" +
			"	public static void main(String[] args) {\n" +
			"		I i = X::goo;\n" +
			"	}\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = "goo";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"goo() [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
// Test (negative): no valid target
public void test0002() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"interface I {\n" +
			"	void foo();\n" +
			"}\n" +
			"class Y {\n" +
			"	static void goo() {}\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		I i = X::goo;\n" +
			"	}\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = "goo";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"",
		elements
	);
}
// test negative, super method targetted is not visible
public void test0003() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"interface I {\n" +
			"	void foo();\n" +
			"}\n" +
			"class Y {\n" +
			"	private static void goo() {}\n" +
			"}\n" +
			"public class X extends Y {\n" +
			"	public static void main(String[] args) {\n" +
			"		I i = X::goo;\n" +
			"	}\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = "goo";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"",
		elements
	);
}
// test (positive) that method references can target super static methods.
public void test0004() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"interface I {\n" +
			"	void foo();\n" +
			"}\n" +
			"class Y {\n" +
			"	static void goo() {}\n" +
			"}\n" +
			"public class X extends Y {\n" +
			"	public static void main(String[] args) {\n" +
			"		I i = X::goo;\n" +
			"	}\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = "goo";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"goo() [in Y [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
// Test (positive) method reference with primaries can target self methods.
public void test0005() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"interface I {\n" +
			"	void foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	void goo() {}\n" +
			"	public static void main(String[] args) {\n" +
			"		I i = new X()::goo;\n" +
			"	}\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = "goo";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"goo() [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
// Test (positive) method reference with primaries can target super methods.
public void test0006() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"interface I {\n" +
			"	void foo();\n" +
			"}\n" +
			"class Y {\n" +
			"	void goo() {}\n" +
			"}\n" +
			"public class X extends Y {\n" +
			"	public static void main(String[] args) {\n" +
			"		I i = new X()::goo;\n" +
			"	}\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = "goo";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"goo() [in Y [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
// Test that static method cannot be invoked with a receiver.
public void test0007() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"interface I {\n" +
			"	void foo();\n" +
			"}\n" +
			"public class X {\n" +
			"	static void goo() {}\n" +
			"	public static void main(String[] args) {\n" +
			"		I i = new X()::goo;\n" +
			"	}\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = "goo";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"",
		elements
	);
}
// Negative test
public void test0008() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"interface I {\n" +
			"	void foo(X x);\n" +
			"}\n" +
			"class Y {\n" +
			"	void goo() {}\n" +
			"}\n" +
			"public class X extends Y {\n" +
			"	public static void main(String[] args) {\n" +
			"		I i = new X()::goo;\n" +
			"	}\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = "goo";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"",
		elements
	);
}
// Test (negative) method references: +ve test below
public void test0009() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"interface I {\n" +
			"	void foo(X x);\n" +
			"}\n" +
			"class Y {\n" +
			"	static void goo() {}\n" +
			"}\n" +
			"public class X extends Y {\n" +
			"	public static void main(String[] args) {\n" +
			"		I i = X::goo;\n" +
			"	}\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = "goo";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"",
		elements
	);
}
// Test positive that metod references can target super methods
public void test0010() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"interface I {\n" +
			"	void foo(X x);\n" +
			"}\n" +
			"class Y {\n" +
			"	void goo() {}\n" +
			"}\n" +
			"public class X extends Y {\n" +
			"	public static void main(String[] args) {\n" +
			"		I i = X::goo;\n" +
			"	}\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = "goo";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"goo() [in Y [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
// Test (negative) method references
public void test0011() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"interface I {\n" +
			"	void foo(X x);\n" +
			"}\n" +
			"class Y {\n" +
			"}\n" +
			"public class X extends Y {\n" +
			"	static void goo(X x) {}\n" +
			"	void goo() {}\n" +
			"	public static void main(String[] args) {\n" +
			"		I i = X::goo;\n" +
			"	}\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = "goo";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"",
		elements
	);
}
// Test positive that lhs of method reference is selectable.
public void test0012() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"interface I {\n" +
			"	void foo(X x);\n" +
			"}\n" +
			"class Y {\n" +
			"}\n" +
			"public class X extends Y {\n" +
			"	static void goo(X x) {}\n" +
			"	void goo() {}\n" +
			"	public static void main(String[] args) {\n" +
			"		I i = X::goo;\n" +
			"	}\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = "X";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]",
		elements
	);
}
// Test positive that type arguments of method reference are selectable
public void test0013() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"interface I {\n" +
			"	void foo(X x);\n" +
			"}\n" +
			"class Y {\n" +
			"}\n" +
			"public class X extends Y {\n" +
			"	static void goo(X x) {}\n" +
			"	void goo() {}\n" +
			"	public static void main(String[] args) {\n" +
			"		I i = X::<Y>goo;\n" +
			"	}\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = "Y";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Y [in [Working copy] X.java [in <default> [in src [in Resolve]]]]",
		elements
	);
}
// Test (negative) constructor references: +ve test below
public void test0014() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"interface I {\n" +
			"	X foo(int x);\n" +
			"}\n" +
			"class Y {}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		I i = X::<Y>new;\n" +
			"	}\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = "new";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"",
		elements
	);
}
// Test (positive) constructor references.
public void test0015() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"interface I {\n" +
			"	X foo(int x);\n" +
			"}\n" +
			"class Y {}\n" +
			"public class X {\n" +
			"   X(long i) {}\n" +
			"	public static void main(String[] args) {\n" +
			"		I i = X::<Y>new;\n" +
			"	}\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = "new";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"X(long) [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
// Test (negative) type mismatch. Positive test with correction below.
public void test0016() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"interface I {\n" +
			"	Y foo(int x);\n" +
			"}\n" +
			"class Y {}\n" +
			"public class X {\n" +
			"   X(long i) {}\n" +
			"	public static void main(String[] args) {\n" +
			"		I i = X::<Y>new;\n" +
			"	}\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = "new";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"",
		elements
	);
}
// Test (positive) that the type arguments are selectable.
public void test0017() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"interface I {\n" +
			"	Y foo(int x);\n" +
			"}\n" +
			"class Y {}\n" +
			"public class X extends Y {\n" +
			"   X(long i) {}\n" +
			"	public static void main(String[] args) {\n" +
			"		I i = X::<Y>new;\n" +
			"	}\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = "Y";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Y [in [Working copy] X.java [in <default> [in src [in Resolve]]]]",
		elements
	);
}
// Test (positive) that the LHS type is selectable.
public void test0018() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"interface I {\n" +
			"	X foo(int x);\n" +
			"}\n" +
			"class Y {}\n" +
			"public class X {\n" +
			"   X(Integer i) {}\n" +
			"	public static void main(String[] args) {\n" +
			"		I i = X::<Y>new;\n" +
			"	}\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = "X";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]",
		elements
	);
}
// Test (positive) that super type methods can be targetted via a primary.
public void test0019() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"interface I {\n" +
			"	int foo();\n" +
			"}\n" +
			"class Y {}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		I i = new X()::<Y>hashCode;\n" +
			"	}\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = "hashCode";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"hashCode() [in Object [in Object.class [in java.lang [in "+ getExternalPath() + "jclMin1.8.jar]]]]",
		elements
	);
}
// Test (positive) that interface types on the LHS can refer to object methods.
public void test0020() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"interface I {\n" +
			"	int foo();\n" +
			"}\n" +
			"class Y {}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		I i = ((I)()->0)::<Y>hashCode;\n" +
			"	}\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = "hashCode";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"hashCode() [in Object [in Object.class [in java.lang [in "+ getExternalPath() + "jclMin1.8.jar]]]]",
		elements
	);
}
// Test (negative) super:: style method references - such constructor references are not grammatical.
public void test0021() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"interface I {\n" +
			"	int foo();\n" +
			"}\n" +
			"class Y {\n" +
			"    int foo() { return 10;}\n" +
			"}\n" +
			"public class X {\n" +
			"	public void main(String[] args) {\n" +
			"		I i = super::foo;\n" +
			"	}\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = "foo";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"",
		elements
	);
}
// Test (positive) super:: style method references - such constructor references are not grammatical.
public void test0022() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"interface I {\n" +
			"	int foo();\n" +
			"}\n" +
			"class Y {\n" +
			"    int foo() { return 10;}\n" +
			"}\n" +
			"public class X extends Y {\n" +
			"	public void main(String[] args) {\n" +
			"		I i = super::foo;\n" +
			"	}\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = "super::foo";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"foo() [in Y [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
// Test (negative) that the synthesized array constructor is NOT selectable - it is fabricated out of nowhere, no where to navigate to !
public void test0023() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"interface I {\n" +
			"	int [] foo(int x);\n" +
			"}\n" +
			"class Y {\n" +
			"    int foo() { return 10;}\n" +
			"}\n" +
			"public class X extends Y {\n" +
			"	public void main(String[] args) {\n" +
			"		I i = int []::new;\n" +
			"	}\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = "new";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"",
		elements
	);
}
}
