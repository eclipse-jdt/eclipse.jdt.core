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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICodeAssist;
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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=408230, [1.8][hovering] NPE on hovering over a type inferred parameter in lambda expression
public void test0024() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"interface I {\n" +
			"	int foo(int a);\n" +
			"}\n" +
			"public class X {	\n" +
			"	void foo() {\n" +
			"		I i = (xyz) -> {\n" +
			"			return xyz;\n" +
			"		};\n" +
			"	}\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = "xyz";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"xyz [in foo() [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=408230, [1.8][hovering] NPE on hovering over a type inferred parameter in lambda expression
public void test0025() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"interface I {\n" +
			"	int foo(int a);\n" +
			"}\n" +
			"public class X {	\n" +
			"	void foo() {\n" +
			"		I i = (abc) -> abc++; \n" +
			"	}\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = "abc";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"abc [in foo() [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=408230, [1.8][hovering] NPE on hovering over a type inferred parameter in lambda expression
public void test0026() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"interface I {\n" +
			"	int foo(int a);\n" +
			"}\n" +
			"public class X {	\n" +
			"	I i = (abc) -> abc++; \n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = "abc";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"abc [in i [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=408230, [1.8][hovering] NPE on hovering over a type inferred parameter in lambda expression
public void test0027() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"interface I {\n" +
			"    I doit(I xyz);\n" +
			"}\n" +
			"public class X { \n" +
			"	public static void main(String[] args) {\n" +
			"		I i = (pqr) -> {\n" +
			"			return (xyz) -> {\n" +
			"				return (abc) -> abc; \n" +
			"			};\n" +
			"		};\n" +
			"	}\n" +
			"}\n");
	String str = this.wc.getSource();
	String selection = "abc";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"abc [in main(String[]) [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=408230, [1.8][hovering] NPE on hovering over a type inferred parameter in lambda expression
public void test0028() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"interface I {\n" +
			"    I doit(I xyz);\n" +
			"}\n" +
			"public class X { \n" +
			"	public static void main(String[] args) {\n" +
			"		I i = (pqr) -> {\n" +
			"			return (xyz) -> {\n" +
			"				return (abc) -> xyz; \n" +
			"			};\n" +
			"		};\n" +
			"	}\n" +
			"}\n");
	String str = this.wc.getSource();
	String selection = "xyz";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"xyz [in main(String[]) [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=408230, [1.8][hovering] NPE on hovering over a type inferred parameter in lambda expression
public void test0029() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"interface I {\n" +
			"    I doit(I xyz);\n" +
			"}\n" +
			"public class X { \n" +
			"	public static void main(String[] args) {\n" +
			"		I i = (pqr) -> {\n" +
			"			return (xyz) -> {\n" +
			"				return (abc) -> args; \n" +
			"			};\n" +
			"		};\n" +
			"	}\n" +
			"}\n");
	String str = this.wc.getSource();
	String selection = "args";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"args [in main(String[]) [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=408230, [1.8][hovering] NPE on hovering over a type inferred parameter in lambda expression
public void test0030() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"interface I {\n" +
			"    I doit(I xyz);\n" +
			"}\n" +
			"public class X { \n" +
			"	X fx = new X((pqr) -> {\n" +
			"		return (zyx) -> {\n" +
			"			return (abc) -> zyx; \n" +
			"		};\n" +
			"	});\n" +
			"	X(I i) {\n" +
			"	}\n" +
			"	void foo(X x) {}\n" +
			"	public static void main(String[] args) {\n" +
			"		X x = null;\n" +
			"		x = new X((pqr) -> {\n" +
			"			return (xyz) -> {\n" +
			"				return (abc) -> xyz; \n" +
			"			};\n" +
			"		});\n" +
			"		System.out.println(x);\n" +
			"	}\n" +
			"}\n");
	String str = this.wc.getSource();
	String selection = "zyx";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"zyx [in fx [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=408230, [1.8][hovering] NPE on hovering over a type inferred parameter in lambda expression
public void test0031() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"interface I {\n" +
			"    I doit(I xyz);\n" +
			"}\n" +
			"public class X { \n" +
			"	X(I i) {\n" +
			"	}\n" +
			"	void foo(X x) {}\n" +
			"	public static void main(String[] args) {\n" +
			"		X x = null;\n" +
			"		x = new X((pqr) -> {\n" +
			"			return (xyz) -> {\n" +
			"				return (abc) -> xyz; \n" +
			"			};\n" +
			"		});\n" +
			"		System.out.println(x);\n" +
			"	}\n" +
			"}\n");
	String str = this.wc.getSource();
	String selection = "xyz";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"xyz [in main(String[]) [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=408230, [1.8][hovering] NPE on hovering over a type inferred parameter in lambda expression
public void test0032() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"interface I {\n" +
			"    I doit(I xyz);\n" +
			"}\n" +
			"public class X { \n" +
			"	X fx = new X((pqr) -> {\n" +
			"		return (xyz) -> {\n" +
			"			return (abc) -> xyz; \n" +
			"		};\n" +
			"	});\n" +
			"	X(I i) {\n" +
			"	}\n" +
			"	void foo(X x) {}\n" +
			"	public static void main(String[] args) {\n" +
			"		X x = null;\n" +
			"		I i = args != null ? (mno) -> mno : (def) -> (hij) -> {\n" +
			"			return hij;\n" +
			"		};\n" +
			"	}\n" +
			"}\n");
	String str = this.wc.getSource();
	String selection = "hij";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"hij [in main(String[]) [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=408230, [1.8][hovering] NPE on hovering over a type inferred parameter in lambda expression
public void test0033() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"interface I {\n" +
			"    I doit(I xyz);\n" +
			"}\n" +
			"public class X { \n" +
			"	X fx = new X((pqr) -> {\n" +
			"		return (xyz) -> {\n" +
			"			return (abc) -> xyz; \n" +
			"		};\n" +
			"	});\n" +
			"	X(I i) {\n" +
			"	}\n" +
			"	void foo(X x) {}\n" +
			"	public static void main(String[] args) {\n" +
			"		X x = null;\n" +
			"		I i;\n" +
			"       i = args != null ? (mno) -> mno : (def) -> (hij) -> {\n" +
			"			return hij;\n" +
			"		};\n" +
			"	}\n" +
			"}\n");
	String str = this.wc.getSource();
	String selection = "hij";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"hij [in main(String[]) [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
//Bug 408230 - [1.8][hovering] NPE on hovering over a type inferred parameter in lambda expression
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=408230
public void testBug408230a() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8");
		String source = "package p;\n" +
				"public class X {\n" +
				"  FI i1 = (a, barg) -> a+barg;\n" +
				"}\n" +
				"interface FI { int f1(int a, int b); }\n";
		createFolder("/P/src/p");
		createFile(
			"/P/src/p/X.java",
			source
		);
		waitForAutoBuild();
		
		ICompilationUnit unit = getCompilationUnit("/P/src/p/X.java"); 
		String selectString = "barg";
		IJavaElement [] variable = ((ICodeAssist) unit).codeSelect(source.lastIndexOf(selectString), selectString.length());
		assertEquals(1, variable.length);
	} finally {
		deleteProject("P");
	}
}
public void testBug408230b() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8");
		String source = "package p;\n" +
				"public class X {\n" +
				"  void foo() {\n" +
				"	FI i2 = (a, barg) -> { return a+barg; };\n" +
				"  }\n" +
				"}\n" +
				"interface FI { int f1(int a, int b); }\n";
		createFolder("/P/src/p");
		createFile(
			"/P/src/p/X.java",
			source
		);
		waitForAutoBuild();
		
		ICompilationUnit unit = getCompilationUnit("/P/src/p/X.java"); 
		String selectString = "barg";
		IJavaElement [] variable = ((ICodeAssist) unit).codeSelect(source.lastIndexOf(selectString), selectString.length());
		assertEquals(1, variable.length);
	} finally {
		deleteProject("P");
	}
}
public void testBug408230c() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8");
		String source = "package p;\n" +
				"public class X {\n" +
				"  void foo() {\n" +
				"	FI i2 = (a, barg) -> { int x = 2; while (x < 2) { x++; } return a+barg; };\n" +
				"  }\n" +
				"}\n" +
				"interface FI { int f1(int a, int b); }\n";
		createFolder("/P/src/p");
		createFile(
			"/P/src/p/X.java",
			source
		);
		waitForAutoBuild();
		
		ICompilationUnit unit = getCompilationUnit("/P/src/p/X.java"); 
		String selectString = "barg";
		IJavaElement [] variable = ((ICodeAssist) unit).codeSelect(source.lastIndexOf(selectString), selectString.length());
		assertEquals(1, variable.length);
	} finally {
		deleteProject("P");
	}
}
public void testBug408230d() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8");
		String source = "package p;\n" +
				"public class X {\n" +
				"  FI i1 = (barg) -> ++barg;\n" +
				"}\n" +
				"interface FI { int f1(int b); }\n";
		createFolder("/P/src/p");
		createFile(
			"/P/src/p/X.java",
			source
		);
		waitForAutoBuild();
		
		ICompilationUnit unit = getCompilationUnit("/P/src/p/X.java"); 
		String selectString = "barg";
		IJavaElement [] variable = ((ICodeAssist) unit).codeSelect(source.lastIndexOf(selectString), selectString.length());
		assertEquals(1, variable.length);
	} finally {
		deleteProject("P");
	}
}
public void testBug408230e() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8");
		String source = "package p;\n" +
				"public class X {\n" +
				"  FI i1 = (aarg) -> { return aarg++;};\n" +
				"}\n" +
				"interface FI { int f1(int a); }\n";
		createFolder("/P/src/p");
		createFile(
			"/P/src/p/X.java",
			source
		);
		waitForAutoBuild();
		
		ICompilationUnit unit = getCompilationUnit("/P/src/p/X.java"); 
		String selectString = "aarg";
		IJavaElement [] variable = ((ICodeAssist) unit).codeSelect(source.lastIndexOf(selectString), selectString.length());
		assertEquals(1, variable.length);
	} finally {
		deleteProject("P");
	}
}
public void testBug408230f() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8");
		String source = "package p;\n" +
				"public class X {\n" +
				"  FI i1 = (aarg) -> {  int x = aarg; return aarg++;};\n" +
				"}\n" +
				"interface FI { int f1(int a); }\n";
		createFolder("/P/src/p");
		createFile(
			"/P/src/p/X.java",
			source
		);
		waitForAutoBuild();
		
		ICompilationUnit unit = getCompilationUnit("/P/src/p/X.java"); 
		String selectString = "aarg";
		IJavaElement [] variable = ((ICodeAssist) unit).codeSelect(source.lastIndexOf(selectString), selectString.length());
		assertEquals(1, variable.length);
	} finally {
		deleteProject("P");
	}
}
public void testBug408230g() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8");
		String source = "package p;\n" +
				"public class X {\n" +
				" public void boo(FI fi) {}\n" +
				"  void foo() {\n" +
				"	boo((aarg) -> aarg++);\n" +
				"  }\n" +
				"}\n" +
				"interface FI { int f1(int a); }\n";
		createFolder("/P/src/p");
		createFile(
			"/P/src/p/X.java",
			source
		);
		waitForAutoBuild();
		
		ICompilationUnit unit = getCompilationUnit("/P/src/p/X.java"); 
		String selectString = "aarg";
		IJavaElement [] variable = ((ICodeAssist) unit).codeSelect(source.lastIndexOf(selectString), selectString.length());
		assertEquals(1, variable.length);
	} finally {
		deleteProject("P");
	}
}
public void testBug408230h() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8");
		String source = "package p;\n" +
				"public class X {\n" +
				" public void boo(FI fi) {}\n" +
				"  void foo() {\n" +
				"	boo((aarg) -> {int b = 10; return aarg++;});\n" +
				"  }\n" +
				"}\n" +
				"interface FI { int f1(int a); }\n";
		createFolder("/P/src/p");
		createFile(
			"/P/src/p/X.java",
			source
		);
		waitForAutoBuild();
		
		ICompilationUnit unit = getCompilationUnit("/P/src/p/X.java"); 
		String selectString = "aarg";
		IJavaElement [] variable = ((ICodeAssist) unit).codeSelect(source.lastIndexOf(selectString), selectString.length());
		assertEquals(1, variable.length);
	} finally {
		deleteProject("P");
	}
}
public void testBug408230i() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8");
		String source = "package p;\n" +
				"public class X {\n" +
				" public void boo(FI fi) {}\n" +
				"  void foo() {\n" +
				"	boo((aarg, x) -> x + aarg++);\n" +
				"  }\n" +
				"}\n" +
				"interface FI { int f1(int a, int b); }\n";
		createFolder("/P/src/p");
		createFile(
			"/P/src/p/X.java",
			source
		);
		waitForAutoBuild();
		
		ICompilationUnit unit = getCompilationUnit("/P/src/p/X.java"); 
		String selectString = "aarg";
		IJavaElement [] variable = ((ICodeAssist) unit).codeSelect(source.lastIndexOf(selectString), selectString.length());
		assertEquals(1, variable.length);
	} finally {
		deleteProject("P");
	}
}
public void testBug408230j() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8");
		String source = "package p;\n" +
				"public class X {\n" +
				" public void boo(FI fi) {}\n" +
				"  void foo() {\n" +
				"	boo((aarg, x) -> {int b = 10; return x + aarg++;});\n" +
				"  }\n" +
				"}\n" +
				"interface FI { int f1(int a, int b); }\n";
		createFolder("/P/src/p");
		createFile(
			"/P/src/p/X.java",
			source
		);
		waitForAutoBuild();
		
		ICompilationUnit unit = getCompilationUnit("/P/src/p/X.java"); 
		String selectString = "aarg";
		IJavaElement [] variable = ((ICodeAssist) unit).codeSelect(source.lastIndexOf(selectString), selectString.length());
		assertEquals(1, variable.length);
	} finally {
		deleteProject("P");
	}
}
public void testBug408230k() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8");
		String source = "package p;\n" +
				"public class X {\n" +
				" public void boo(int x, int y, FI fi) {}\n" +
				"  void foo() {\n" +
				"	boo(2, 4, (aarg) -> aarg++);\n" +
				"  }\n" +
				"}\n" +
				"interface FI { int f1(int a); }\n";
		createFolder("/P/src/p");
		createFile(
			"/P/src/p/X.java",
			source
		);
		waitForAutoBuild();
		
		ICompilationUnit unit = getCompilationUnit("/P/src/p/X.java"); 
		String selectString = "aarg";
		IJavaElement [] variable = ((ICodeAssist) unit).codeSelect(source.lastIndexOf(selectString), selectString.length());
		assertEquals(1, variable.length);
	} finally {
		deleteProject("P");
	}
}
public void testBug408230l() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8");
		String source = "package p;\n" +
				"public class X {\n" +
				" public void boo(int x, FI fi) {}\n" +
				"  void foo() {\n" +
				"	boo(2, (aarg) -> {int b = 10; return aarg++;});\n" +
				"  }\n" +
				"}\n" +
				"interface FI { int f1(int a); }\n";
		createFolder("/P/src/p");
		createFile(
			"/P/src/p/X.java",
			source
		);
		waitForAutoBuild();
		
		ICompilationUnit unit = getCompilationUnit("/P/src/p/X.java"); 
		String selectString = "aarg";
		IJavaElement [] variable = ((ICodeAssist) unit).codeSelect(source.lastIndexOf(selectString), selectString.length());
		assertEquals(1, variable.length);
	} finally {
		deleteProject("P");
	}
}
public void testBug408230m() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8");
		String source = "package p;\n" +
				"public class X {\n" +
				" public void boo(int x, int y, FI fi) {}\n" +
				"  void foo() {\n" +
				"	boo(2, 5+6, (aarg, x) -> x + aarg++);\n" +
				"  }\n" +
				"}\n" +
				"interface FI { int f1(int a, int b); }\n";
		createFolder("/P/src/p");
		createFile(
			"/P/src/p/X.java",
			source
		);
		waitForAutoBuild();
		
		ICompilationUnit unit = getCompilationUnit("/P/src/p/X.java"); 
		String selectString = "aarg";
		IJavaElement [] variable = ((ICodeAssist) unit).codeSelect(source.lastIndexOf(selectString), selectString.length());
		assertEquals(1, variable.length);
	} finally {
		deleteProject("P");
	}
}
public void testBug408230n() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8");
		String source = "package p;\n" +
				"public class X {\n" +
				" public void boo(int x, FI fi) {}\n" +
				"  void foo() {\n" +
				"	boo(2, (aarg, x) -> {int b = 10; return x + aarg++;});\n" +
				"  }\n" +
				"}\n" +
				"interface FI { int f1(int a, int b); }\n";
		createFolder("/P/src/p");
		createFile(
			"/P/src/p/X.java",
			source
		);
		waitForAutoBuild();
		
		ICompilationUnit unit = getCompilationUnit("/P/src/p/X.java"); 
		String selectString = "aarg";
		IJavaElement [] variable = ((ICodeAssist) unit).codeSelect(source.lastIndexOf(selectString), selectString.length());
		assertEquals(1, variable.length);
	} finally {
		deleteProject("P");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=417935, [1.8][code select] ICU#codeSelect doesn't work on reference to lambda parameter
public void test417935() throws JavaModelException {  // JCL_MIN does not have the relevant classes - these are needed to handle lambda. Use local versions.
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"class Collections {\n" +
			"	public static void sort(ArrayList list, Comparator c) {\n" +
			"	}\n" +
			"}\n" +
			"interface Comparator {\n" +
			"	int compareTo(X t, X s);\n" +
			"}\n" +
			"class ArrayList {\n" +
			"}\n" +
			"public class X {\n" +
			"	int compareTo(X x) { return 0; }\n" +
			"	void foo() {\n" +
			"		Collections.sort(new ArrayList(), (X o1, X o2) -> o1.compareTo(o2));\n" +
			"	}\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = "compareTo";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"compareTo(X) [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=417935, [1.8][code select] ICU#codeSelect doesn't work on reference to lambda parameter
public void test417935a() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"import java.util.ArrayList;\n" +
			"import java.util.Arrays;\n" +
			"import java.util.Collections;\n" +
			"import java.util.Comparator;\n" +
			"public class X {\n" +
			"   int compareTo(X x) { return 0; }\n" +
			"	void foo() {\n" +
			"		Collections.sort(new ArrayList<X>(Arrays.asList(new X(), new X(), new X()),\n" +
			"				new Comparator<X>() {\n" +
			"					@Override\n" +
			"					public int compare(X o1, X o2) {\n" +
			"						return o1.compareTo(o2);\n" +
			"					}\n" +
			"				});\n" +
			"	}\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = "compareTo";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"compareTo(X) [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
public void testFieldInit() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"interface I {\n" +
			"	void foo(int x, int y);\n" +
			"}\n" +
			"public class X {\n" +
			"	I i = (first, second) -> { System.out.println(first); };\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = "first";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"first [in i [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422468, [1.8][assist] Code assist issues with type elided lambda parameters
public void test422468() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"interface I {\n" +
			"	void foo(X x, Object y);\n" +
			"}\n" +
			"public class X {\n" +
			"	I i = (first, second) -> { System.out.println(); };\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = "first";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"first [in i [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422468, [1.8][assist] Code assist issues with type elided lambda parameters
public void test422468a() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"interface I {\n" +
			"	void foo(X x, Object y);\n" +
			"}\n" +
			"public class X {\n" +
			"	I i = (first, second) -> { System.out.println(); };\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = "second";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"second [in i [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422468, [1.8][assist] Code assist issues with type elided lambda parameters
public void test422468b() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"interface I {\n" +
			"	I foo (I x);\n" +
			"}\n" +
			"public class X {\n" +
			"	static void goo(I i) {}\n" +
			"	public static void main(String[] args) {\n" +
			"		goo((x) -> (y) -> (z) -> z.foo((p) -> p));\n" +
			"	}\n" +
			"} \n");

	String str = this.wc.getSource();
	String selection = "y";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"y [in main(String[]) [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422468, [1.8][assist] Code assist issues with type elided lambda parameters
public void test422468c() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"interface I {\n" +
			"	I foo (I x);\n" +
			"}\n" +
			"public class X {\n" +
			"	static void goo(I i) {}\n" +
			"	public static void main(String[] args) {\n" +
			"		goo( x -> y -> z -> z.foo(p -> p));\n" +
			"	}\n" +
			"} \n");

	String str = this.wc.getSource();
	String selection = "y";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"y [in main(String[]) [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422468, [1.8][assist] Code assist issues with type elided lambda parameters
public void test422468d() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"interface I {\n" +
			"	J foo(String x, String y);\n" +
			"}\n" +
			"interface J {\n" +
			"	K foo(String x, String y);\n" +
			"}\n" +
			"interface K {\n" +
			"	int foo(String x, int y);\n" +
			"}\n" +
			"public class X {\n" +
			"	static void goo(K i) {}\n" +
			"	public static void main(String[] args) {\n" +
			"		I i = (x, y) -> { return (a, b) -> (p, q) -> a.length(); };\n" +
			"	}\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = "a.length";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"length() [in String [in String.class [in java.lang [in "+ getExternalPath() + "jclMin1.8.jar]]]]",
		elements
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422468, [1.8][assist] Code assist issues with type elided lambda parameters
public void test422468e() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"interface I {\n" +
			"	J foo(String x, String y);\n" +
			"}\n" +
			"interface J {\n" +
			"	K foo(String x, String y);\n" +
			"}\n" +
			"interface K {\n" +
			"	int foo(String x, int y);\n" +
			"}\n" +
			"public class X {\n" +
			"	static void goo(K i) {}\n" +
			"	public static void main(String[] args) {\n" +
			"		I i = (x, y) -> { return (a, b) -> (p, q) -> a.length(); };\n" +
			"	}\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = "q";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"q [in main(String[]) [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=422468, [1.8][assist] Code assist issues with type elided lambda parameters
public void testParser() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"interface I {\n" +
			"	int foo(String x, Integer y);\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		I i = (x, y) -> {\n" +
			"			x = \"Hello\"\n" +
			"			y = 10;		\n" +
			"			if (x.length() > y) {\n" +
			"				System.out.println(\"if\");\n" +
			"			} else {\n" +
			"				System.out.println(\"else\");\n" +
			"			}\n" +
			"			return x.length();\n" +
			"		};\n" +
			"		// System.out.println((I) (p, q) -> { return q.\n" +
			"	}\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = "x";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"x [in main(String[]) [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
}
