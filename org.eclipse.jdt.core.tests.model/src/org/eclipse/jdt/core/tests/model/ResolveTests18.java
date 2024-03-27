/*******************************************************************************
 * Copyright (c) 2014, 2018 IBM Corporation and others.
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

import junit.framework.Test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.BindingKey;
import org.eclipse.jdt.core.ICodeAssist;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.internal.core.LambdaExpression;
import org.eclipse.jdt.internal.core.LambdaMethod;

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
@Override
public ICompilationUnit getWorkingCopy(String path, String source) throws JavaModelException {
	return super.getWorkingCopy(path, source, this.wcOwner);
}
@Override
public void setUpSuite() throws Exception {
	super.setUpSuite();

	setUpJavaProject("Resolve", "1.8", true);

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

	super.tearDownSuite();
}

@Override
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
		"hashCode() [in Object [in Object.class [in java.lang [in "+ getExternalPath() + "jclFull1.8.jar]]]]",
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
		"hashCode() [in Object [in Object.class [in java.lang [in "+ getExternalPath() + "jclFull1.8.jar]]]]",
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
		"xyz [in foo(int) [in <lambda #1> [in foo() [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]]]",
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
		"abc [in foo(int) [in <lambda #1> [in foo() [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]]]",
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
		"abc [in foo(int) [in <lambda #1> [in i [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]]]",
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
		"abc [in doit(I) [in <lambda #1> [in doit(I) [in <lambda #1> [in doit(I) [in <lambda #1> [in main(String[]) [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]]]]]]]",
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
		"xyz [in doit(I) [in <lambda #1> [in doit(I) [in <lambda #1> [in main(String[]) [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]]]]]",
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
		"zyx [in doit(I) [in <lambda #1> [in doit(I) [in <lambda #1> [in fx [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]]]]]",
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
		"xyz [in doit(I) [in <lambda #1> [in doit(I) [in <lambda #1> [in main(String[]) [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]]]]]",
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
		"hij [in doit(I) [in <lambda #1> [in doit(I) [in <lambda #1> [in main(String[]) [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]]]]]",
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
		"hij [in doit(I) [in <lambda #1> [in doit(I) [in <lambda #1> [in main(String[]) [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]]]]]",
		elements
	);
}
//Bug 408230 - [1.8][hovering] NPE on hovering over a type inferred parameter in lambda expression
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=408230
public void testBug408230a() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8", true);
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
		createJavaProject("P", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8", true);
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
		createJavaProject("P", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8", true);
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
		createJavaProject("P", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8", true);
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
		createJavaProject("P", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8", true);
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
		createJavaProject("P", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8", true);
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
		createJavaProject("P", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8", true);
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
		createJavaProject("P", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8", true);
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
		createJavaProject("P", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8", true);
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
		createJavaProject("P", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8", true);
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
		createJavaProject("P", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8", true);
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
		createJavaProject("P", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8", true);
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
		createJavaProject("P", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8", true);
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
		createJavaProject("P", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8", true);
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
		"first [in foo(int, int) [in <lambda #1> [in i [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]]]",
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
		"first [in foo(X, java.lang.Object) [in <lambda #1> [in i [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]]]",
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
		"second [in foo(X, java.lang.Object) [in <lambda #1> [in i [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]]]",
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
		"y [in foo(I) [in <lambda #1> [in foo(I) [in <lambda #1> [in main(String[]) [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]]]]]",
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
		"y [in foo(I) [in <lambda #1> [in foo(I) [in <lambda #1> [in main(String[]) [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]]]]]",
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
		"length() [in String [in String.class [in java.lang [in "+ getExternalPath() + "jclFull1.8.jar]]]]",
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
		"q [in foo(java.lang.String, int) [in <lambda #1> [in foo(java.lang.String, java.lang.String) [in <lambda #1> [in foo(java.lang.String, java.lang.String) [in <lambda #1> [in main(String[]) [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]]]]]]]",
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
		"x [in foo(java.lang.String, java.lang.Integer) [in <lambda #1> [in main(String[]) [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]]]",
		elements
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=424110, [1.8][hovering] Hover, F3 does not work for method reference in method invocation
public void test424110() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"public class X {\n" +
			"	static F f = X::m; // [1] Works\n" +
			"	int i = fun(X::m); // [2] Does not work\n" +
			"	public static int m(int x) {\n" +
			"		return x;\n" +
			"	}\n" +
			"	private int fun(F f) {\n" +
			"		return f.foo(0);\n" +
			"	}\n" +
			"}\n" +
			"interface F {\n" +
			"	int foo(int x);\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = "m";
	int start = str.indexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"m(int) [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=424110, [1.8][hovering] Hover, F3 does not work for method reference in method invocation
public void test424110a() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"public class X {\n" +
			"	int i = fun(X::m); // [2] Does not work\n" +
			"	public static int m(int x) {\n" +
			"		return x;\n" +
			"	}\n" +
			"	private int fun(F f) {\n" +
			"		return f.foo(0);\n" +
			"	}\n" +
			"}\n" +
			"interface F {\n" +
			"	int foo(int x);\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = "m";
	int start = str.indexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"m(int) [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=424071, [1.8][select] cannot select method invoked on a lambda parameter with inferred type
public void test424071() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"import java.util.List;\n" +
			"import java.util.Map;\n" +
			"import java.util.stream.Collectors;\n" +
			"class Person {\n" +
			"	String getLast() { return \"\"; };\n" +
			"}\n" +
			"public class X {\n" +
			"	void test1(List<Person> roster) {\n" +
			"        Map<String, Person> map = \n" +
			"                roster\n" +
			"                    .stream()\n" +
			"                    .collect(\n" +
			"                        Collectors.toMap(\n" +
			"                            p -> p.getLast(), //[1]\n" +
			"                            p -> p            //[2]\n" +
			"                        ));\n" +
			"	}\n" +
			"}\n"
			);

	String str = this.wc.getSource();
	String selection = "getLast";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"getLast() [in Person [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=424198, [1.8][hover] IAE in Signature.createCharArrayTypeSignature when hovering on variable of wildcard type, plus compile errors
public void test424198() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"import java.io.IOException;\n" +
			"import java.nio.file.Path;\n" +
			"import java.util.ArrayList;\n" +
			"import java.util.List;\n" +
			"import java.util.function.Function;\n" +
			"import java.util.jar.JarEntry;\n" +
			"import java.util.jar.JarFile;\n" +
			"import java.util.stream.Collectors;\n" +
			"import java.util.stream.Stream;\n" +
			"class InsistentCapture {\n" +
			"  static void processJar(Path plugin) throws IOException {\n" +
			"    try(JarFile jar = new JarFile(plugin.toFile())) {\n" +
			"      try(Stream<JarEntry> entries = jar.stream()) {\n" +
			"        Function<? super JarEntry, ? extends String> toName =\n" +
			"          entry -> entry.getName();\n" +
			"        Stream<? extends String> stream = entries.map(toName).distinct(); // Ok\n" +
			"        withWildcard(entries.map(toName).distinct()); // Ok\n" +
			"        withWildcard(stream); // Ok\n" +
			"        Stream<String> stream2 = entries.map(toName).distinct(); // ERROR\n" +
			"        withoutWildcard(entries.map(toName).distinct()); // ERROR\n" +
			"        withoutWildcard(stream); // ERROR\n" +
			"        withoutWildcard(stream2); // Ok\n" +
			"        withoutWildcard(coerce(stream)); // Ok\n" +
			"        withoutWildcard(stream.map((String v1) -> { // ERROR\n" +
			"          String r = \"\" + v1; // Hover on v: Ok\n" +
			"          return r;\n" +
			"        }));\n" +
			"        withoutWildcard(stream.map((v2) -> { // Ok\n" +
			"          String r = \"\" + v2; // Hover on v: NOT OK\n" +
			"          return r;\n" +
			"        }));\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"  private static Stream<String> coerce(Stream<? extends String> stream) {\n" +
			"    if(\"1\" == \"\") { return stream.collect(Collectors.toList()).stream(); // ERROR\n" +
			"    }\n" +
			"    return stream.collect(Collectors.toList()); // NO ERROR\n" +
			"  }\n" +
			"  private static void withWildcard(Stream<? extends String> distinct) {\n" +
			"    distinct.forEach(s1 -> System.out.println(s1)); // hover on s: NOT OK\n" +
			"  }\n" +
			"  private static void withoutWildcard(Stream<String> distinct) {\n" +
			"    distinct.forEach(s2 -> System.out.println(s2)); // hover on s: Ok\n" +
			"  }\n" +
			"}\n"
			);

	String str = this.wc.getSource();
	String selection = "v1";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"v1 [in apply(java.lang.String) [in <lambda #1> [in processJar(Path) [in InsistentCapture [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]]]",
		elements,
		true
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=424198, [1.8][hover] IAE in Signature.createCharArrayTypeSignature when hovering on variable of wildcard type, plus compile errors
public void test424198a() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"import java.io.IOException;\n" +
			"import java.nio.file.Path;\n" +
			"import java.util.ArrayList;\n" +
			"import java.util.List;\n" +
			"import java.util.function.Function;\n" +
			"import java.util.jar.JarEntry;\n" +
			"import java.util.jar.JarFile;\n" +
			"import java.util.stream.Collectors;\n" +
			"import java.util.stream.Stream;\n" +
			"class InsistentCapture {\n" +
			"  static void processJar(Path plugin) throws IOException {\n" +
			"    try(JarFile jar = new JarFile(plugin.toFile())) {\n" +
			"      try(Stream<JarEntry> entries = jar.stream()) {\n" +
			"        Function<? super JarEntry, ? extends String> toName =\n" +
			"          entry -> entry.getName();\n" +
			"        Stream<? extends String> stream = entries.map(toName).distinct(); // Ok\n" +
			"        withWildcard(entries.map(toName).distinct()); // Ok\n" +
			"        withWildcard(stream); // Ok\n" +
			"        Stream<String> stream2 = entries.map(toName).distinct(); // ERROR\n" +
			"        withoutWildcard(entries.map(toName).distinct()); // ERROR\n" +
			"        withoutWildcard(stream); // ERROR\n" +
			"        withoutWildcard(stream2); // Ok\n" +
			"        withoutWildcard(coerce(stream)); // Ok\n" +
			"        withoutWildcard(stream.map((String v1) -> { // ERROR\n" +
			"          String r = \"\" + v1; // Hover on v: Ok\n" +
			"          return r;\n" +
			"        }));\n" +
			"        withoutWildcard(stream.map((v2) -> { // Ok\n" +
			"          String r = \"\" + v2; // Hover on v: NOT OK\n" +
			"          return r;\n" +
			"        }));\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"  private static Stream<String> coerce(Stream<? extends String> stream) {\n" +
			"    if(\"1\" == \"\") { return stream.collect(Collectors.toList()).stream(); // ERROR\n" +
			"    }\n" +
			"    return stream.collect(Collectors.toList()); // NO ERROR\n" +
			"  }\n" +
			"  private static void withWildcard(Stream<? extends String> distinct) {\n" +
			"    distinct.forEach(s1 -> System.out.println(s1)); // hover on s: NOT OK\n" +
			"  }\n" +
			"  private static void withoutWildcard(Stream<String> distinct) {\n" +
			"    distinct.forEach(s2 -> System.out.println(s2)); // hover on s: Ok\n" +
			"  }\n" +
			"}\n"
			);

	String str = this.wc.getSource();
	String selection = "v2";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"v2 [in apply(java.lang.String) [in <lambda #1> [in processJar(Path) [in InsistentCapture [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]]]",
		elements,
		true
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=424198, [1.8][hover] IAE in Signature.createCharArrayTypeSignature when hovering on variable of wildcard type, plus compile errors
public void test424198b() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"import java.io.IOException;\n" +
			"import java.nio.file.Path;\n" +
			"import java.util.ArrayList;\n" +
			"import java.util.List;\n" +
			"import java.util.function.Function;\n" +
			"import java.util.jar.JarEntry;\n" +
			"import java.util.jar.JarFile;\n" +
			"import java.util.stream.Collectors;\n" +
			"import java.util.stream.Stream;\n" +
			"class InsistentCapture {\n" +
			"  static void processJar(Path plugin) throws IOException {\n" +
			"    try(JarFile jar = new JarFile(plugin.toFile())) {\n" +
			"      try(Stream<JarEntry> entries = jar.stream()) {\n" +
			"        Function<? super JarEntry, ? extends String> toName =\n" +
			"          entry -> entry.getName();\n" +
			"        Stream<? extends String> stream = entries.map(toName).distinct(); // Ok\n" +
			"        withWildcard(entries.map(toName).distinct()); // Ok\n" +
			"        withWildcard(stream); // Ok\n" +
			"        Stream<String> stream2 = entries.map(toName).distinct(); // ERROR\n" +
			"        withoutWildcard(entries.map(toName).distinct()); // ERROR\n" +
			"        withoutWildcard(stream); // ERROR\n" +
			"        withoutWildcard(stream2); // Ok\n" +
			"        withoutWildcard(coerce(stream)); // Ok\n" +
			"        withoutWildcard(stream.map((String v1) -> { // ERROR\n" +
			"          String r = \"\" + v1; // Hover on v: Ok\n" +
			"          return r;\n" +
			"        }));\n" +
			"        withoutWildcard(stream.map((v2) -> { // Ok\n" +
			"          String r = \"\" + v2; // Hover on v: NOT OK\n" +
			"          return r;\n" +
			"        }));\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"  private static Stream<String> coerce(Stream<? extends String> stream) {\n" +
			"    if(\"1\" == \"\") { return stream.collect(Collectors.toList()).stream(); // ERROR\n" +
			"    }\n" +
			"    return stream.collect(Collectors.toList()); // NO ERROR\n" +
			"  }\n" +
			"  private static void withWildcard(Stream<? extends String> distinct) {\n" +
			"    distinct.forEach(s1 -> System.out.println(s1)); // hover on s: NOT OK\n" +
			"  }\n" +
			"  private static void withoutWildcard(Stream<String> distinct) {\n" +
			"    distinct.forEach(s2 -> System.out.println(s2)); // hover on s: Ok\n" +
			"  }\n" +
			"}\n"
			);

	String str = this.wc.getSource();
	String selection = "s1";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"s1 [in accept(java.lang.String) [in <lambda #1> [in withWildcard(Stream<? extends String>) [in InsistentCapture [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]]]",
		elements,
		true
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=424198, [1.8][hover] IAE in Signature.createCharArrayTypeSignature when hovering on variable of wildcard type
public void test424198c() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"import java.io.IOException;\n" +
			"import java.nio.file.Path;\n" +
			"import java.util.ArrayList;\n" +
			"import java.util.List;\n" +
			"import java.util.function.Function;\n" +
			"import java.util.jar.JarEntry;\n" +
			"import java.util.jar.JarFile;\n" +
			"import java.util.stream.Collectors;\n" +
			"import java.util.stream.Stream;\n" +
			"class InsistentCapture {\n" +
			"  static void processJar(Path plugin) throws IOException {\n" +
			"    try(JarFile jar = new JarFile(plugin.toFile())) {\n" +
			"      try(Stream<JarEntry> entries = jar.stream()) {\n" +
			"        Function<? super JarEntry, ? extends String> toName =\n" +
			"          entry -> entry.getName();\n" +
			"        Stream<? extends String> stream = entries.map(toName).distinct(); // Ok\n" +
			"        withWildcard(entries.map(toName).distinct()); // Ok\n" +
			"        withWildcard(stream); // Ok\n" +
			"        Stream<String> stream2 = entries.map(toName).distinct(); // ERROR\n" +
			"        withoutWildcard(entries.map(toName).distinct()); // ERROR\n" +
			"        withoutWildcard(stream); // ERROR\n" +
			"        withoutWildcard(stream2); // Ok\n" +
			"        withoutWildcard(coerce(stream)); // Ok\n" +
			"        withoutWildcard(stream.map((String v1) -> { // ERROR\n" +
			"          String r = \"\" + v1; // Hover on v: Ok\n" +
			"          return r;\n" +
			"        }));\n" +
			"        withoutWildcard(stream.map((v2) -> { // Ok\n" +
			"          String r = \"\" + v2; // Hover on v: NOT OK\n" +
			"          return r;\n" +
			"        }));\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"  private static Stream<String> coerce(Stream<? extends String> stream) {\n" +
			"    if(\"1\" == \"\") { return stream.collect(Collectors.toList()).stream(); // ERROR\n" +
			"    }\n" +
			"    return stream.collect(Collectors.toList()); // NO ERROR\n" +
			"  }\n" +
			"  private static void withWildcard(Stream<? extends String> distinct) {\n" +
			"    distinct.forEach(s1 -> System.out.println(s1)); // hover on s: NOT OK\n" +
			"  }\n" +
			"  private static void withoutWildcard(Stream<String> distinct) {\n" +
			"    distinct.forEach(s2 -> System.out.println(s2)); // hover on s: Ok\n" +
			"  }\n" +
			"}\n"
			);

	String str = this.wc.getSource();
	String selection = "s2";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"s2 [in accept(java.lang.String) [in <lambda #1> [in withoutWildcard(Stream<String>) [in InsistentCapture [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]]]",
		elements,
		true
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429262,  [1.8][code select] Hover/navigation support at -> and ::
public void test429262() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"class Y {\n" +
			"    static void goo(I i) {}\n" +
			"}\n" +
			"public class X {\n" +
			"    public static void main(String [] args) {\n" +
			"        Y.goo(x -> x * x);\n" +
			"    }\n" +
			"}\n" +
			"interface I {\n" +
			"	int foo(int x);\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = "->";
	int start = str.indexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"foo(int) [in I [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429262,  [1.8][code select] Hover/navigation support at -> and ::
public void test429262a() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"class Y {\n" +
			"    static void goo(I i) {}\n" +
			"}\n" +
			"public class X {\n" +
			"    public static void main(String [] args) {\n" +
			"        Y.goo(x -> x * x);\n" +
			"    }\n" +
			"}\n" +
			"interface I {\n" +
			"	int foo(int x);\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = ">";
	int start = str.indexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"foo(int) [in I [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429262,  [1.8][code select] Hover/navigation support at -> and ::
public void test429262b() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"class Y {\n" +
			"    static void goo(I i) {}\n" +
			"}\n" +
			"public class X {\n" +
			"    public static void main(String [] args) {\n" +
			"        Y.goo(x -> x * x);\n" +
			"    }\n" +
			"}\n" +
			"interface I {\n" +
			"	int foo(int x);\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = "-";
	int start = str.indexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"foo(int) [in I [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429262,  [1.8][code select] Hover/navigation support at -> and ::
public void test429262c() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"class Y {\n" +
			"    static void goo(I i) {}\n" +
			"}\n" +
			"public class X {\n" +
			"    static int zoo(int x) { return x; }\n" +
			"    public static void main(String [] args) {\n" +
			"        Y.goo(X::zoo);\n" +
			"    }\n" +
			"}\n" +
			"interface I {\n" +
			"	int foo(int x);\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = "::";
	int start = str.indexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"foo(int) [in I [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429262,  [1.8][code select] Hover/navigation support at -> and ::
public void test429262d() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"class Y {\n" +
			"    static void goo(I i) {}\n" +
			"}\n" +
			"public class X {\n" +
			"    static int zoo(int x) { return x; }\n" +
			"    public static void main(String [] args) {\n" +
			"        Y.goo(X::zoo);\n" +
			"    }\n" +
			"}\n" +
			"interface I {\n" +
			"	int foo(int x);\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = ":";
	int start = str.indexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"foo(int) [in I [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429262,  [1.8][code select] Hover/navigation support at -> and ::
public void test429262e() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"class Y {\n" +
			"    static void goo(I i) {}\n" +
			"}\n" +
			"public class X {\n" +
			"    static int zoo(int x) { return x; }\n" +
			"    public static void main(String [] args) {\n" +
			"        Y.goo(X::zoo);\n" +
			"    }\n" +
			"}\n" +
			"interface I {\n" +
			"	int foo(int x);\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = ":";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"foo(int) [in I [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428968, [1.8] NPE while computing a text hover
public void test428968() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"import java.util.ArrayList;\n" +
			"import java.util.Collections;\n" +
			"import java.util.Comparator;\n" +
			"import java.util.List;\n" +
			"public class X {\n" +
			"	private void foo() {\n" +
			"		List<Person> people= new ArrayList<>();\n" +
			"		Collections.sort(people, Comparator.comparing(p -> p.getLastName()));\n" +
			"	}\n" +
			"}\n" +
			"class Person{\n" +
			"	String getLastName() {\n" +
			"		return null;\n" +
			"	}\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = "comparing";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"comparing(java.util.function.Function<? super T,? extends U>) {key=Ljava/util/Comparator<>;.comparing<T:Ljava/lang/Object;U::Ljava/lang/Comparable<-TU;>;>(Ljava/util/function/Function<-TT;+TU;>;)Ljava/util/Comparator<TT;>;%<LX~Person;Ljava/lang/String;>} [in Comparator [in Comparator.class [in java.util [in "+ getExternalPath() + "jclFull1.8.jar]]]]",
		elements, true
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428968, [1.8] NPE while computing a text hover
public void test428968a() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"import java.util.ArrayList;\n" +
			"import java.util.Collections;\n" +
			"import java.util.Comparator;\n" +
			"import java.util.List;\n" +
			"public class X {\n" +
			"	private void foo() {\n" +
			"		List<Person> people= new ArrayList<>();\n" +
			"		Collections.sort(people, Comparator.comparing((Person p) -> p.getLastName()));\n" +
			"	}\n" +
			"}\n" +
			"class Person{\n" +
			"	String getLastName() {\n" +
			"		return null;\n" +
			"	}\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = "comparing";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"comparing(java.util.function.Function<? super T,? extends U>) {key=Ljava/util/Comparator<>;.comparing<T:Ljava/lang/Object;U::Ljava/lang/Comparable<-TU;>;>(Ljava/util/function/Function<-TT;+TU;>;)Ljava/util/Comparator<TT;>;%<LX~Person;Ljava/lang/String;>} [in Comparator [in Comparator.class [in java.util [in "+ getExternalPath() + "jclFull1.8.jar]]]]",
		elements, true
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=428968, [1.8] NPE while computing a text hover
public void test428968b() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"import java.util.ArrayList;\n" +
			"import java.util.Collections;\n" +
			"import java.util.Comparator;\n" +
			"import java.util.List;\n" +
			"public class X {\n" +
			"	private void foo() {\n" +
			"		Comparator.reverseOrder();\n" +
			"	}\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = "reverseOrder";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"reverseOrder() {key=Ljava/util/Comparator<>;.reverseOrder<T::Ljava/lang/Comparable<-TT;>;>()Ljava/util/Comparator<TT;>;%<^{175#0};>} [in Comparator [in Comparator.class [in java.util [in "+ getExternalPath() + "jclFull1.8.jar]]]]",
		elements, true
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=425064,  [1.8][compiler] NPE in CaptureBinding.computeUniqueKey
public void test425064() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"import java.util.Comparator;\n" +
			"public class ComparatorUse {\n" +
			"	Comparator<String> c =\n" +
			"			Comparator.comparing((String s)->s.toString())\n" +
			"			.thenComparing(s -> s.length());\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = "comparing";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"comparing(java.util.function.Function<? super T,? extends U>) {key=Ljava/util/Comparator<>;.comparing<T:Ljava/lang/Object;U::Ljava/lang/Comparable<-TU;>;>(Ljava/util/function/Function<-TT;+TU;>;)Ljava/util/Comparator<TT;>;%<Ljava/lang/String;Ljava/lang/String;>} [in Comparator [in Comparator.class [in java.util [in "+ getExternalPath() + "jclFull1.8.jar]]]]",
		elements, true
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=425064,  [1.8][compiler] NPE in CaptureBinding.computeUniqueKey
public void test425064a() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"import java.util.Comparator;\n" +
			"public class ComparatorUse {\n" +
			"	Comparator<String> c =\n" +
			"			Comparator.comparing((String s)->s.toString())\n" +
			"			.thenComparing(s -> s.length());\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = "thenComparing";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"thenComparing(java.util.function.Function<? super T,? extends U>) {key=Ljava/util/Comparator<Ljava/lang/String;>;.thenComparing<U::Ljava/lang/Comparable<-TU;>;>(Ljava/util/function/Function<-Ljava/lang/String;+TU;>;)Ljava/util/Comparator<Ljava/lang/String;>;%<Ljava/lang/Integer;>} [in Comparator [in Comparator.class [in java.util [in "+ getExternalPath() + "jclFull1.8.jar]]]]",
		elements, true
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429845, [1.8] CCE on hover
public void test429845() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"@FunctionalInterface\n" +
			"interface FI {\n" +
			"	int foo();\n" +
			"}\n" +
			"class C1 {\n" +
			"	void fun1(int x) {\n" +
			"		FI test= () -> {\n" +
			"			for (int k=0;k<1;) ;\n" +
			"			for (int k=0;k<1;) ;\n" +
			"			try {\n" +
			"			} catch (Exception ex) {\n" +
			"			}\n" +
			"			return 0;\n" +
			"		};\n" +
			"	}\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = "ex";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"ex [in foo() [in <lambda #1> [in fun1(int) [in C1 [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]]]",
		elements, true
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429948, Unhandled event loop exception is thrown when a lambda expression is nested
public void test429948() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"interface Supplier<T> {\n" +
			"    T get();\n" +
			"}\n" +
			"interface Runnable {\n" +
			"    public abstract void run();\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		execute(() -> {\n" +
			"			executeInner(() -> {\n" +
			"                int xxx = 10;\n" +
			"			});\n" +
			"			return null;\n" +
			"		});\n" +
			"		System.out.println(\"done\");\n" +
			"	}\n" +
			"	static <R> R execute(Supplier<R> supplier) {\n" +
			"		return null;\n" +
			"	}\n" +
			"	static void executeInner(Runnable callback) {\n" +
			"	}\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = "xxx";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"xxx [in run() [in <lambda #1> [in get() [in <lambda #1> [in main(String[]) [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]]]]]",
		elements, true
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429934, [1.8][search] for references to type of lambda with 'this' parameter throws AIIOBE
public void test429934() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/Resolve/src/X.java",
			"interface Function<T, R> {\n" +
			"    R apply(T t);\n" +
			"}\n" +
			"public class X {\n" +
			"	public static void main(String[] args) {\n" +
			"		Function<String, String> f1= (String s, Function this) -> s;\n" +
			"		Function<String, String> f2= (Function this, String s) -> s;\n" +
			"	} \n" +
			"}\n"
	);

	String str = this.workingCopies[0].getSource();
	String selection = "s";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
	assertElementsEqual(
			"Unexpected elements",
			"s [in main(String[]) [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]",
			elements, true
		);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=429812, [1.8][model] Signatures returned by lambda IMethod APIs should be dot-based

public void test429812() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/Resolve/src/X.java",
			"import java.util.List;\n" +
			"interface Getter<E> {\n" +
			"    E get(List<E> list, int i);\n" +
			"}\n" +
			"public class X<U> {\n" +
			"	public void foo(List<U> l) {\n" +
			"		Getter<U> g= (x, i) -> x.get(i);\n" +
			"	} \n" +
			"}\n"
			);

	String str = this.workingCopies[0].getSource();
	String selection = "x";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
	assertElementsEqual(
			"Unexpected elements",
			"x [in get(java.util.List<U>, int) [in <lambda #1> [in foo(List<U>) [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]]]",
			elements, true
			);
	IMethod lambda = (IMethod) elements[0].getParent();
	assertEquals("(Ljava.util.List<TU;>;I)TU;", lambda.getSignature());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430136
public void test430136() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/Resolve/src/X.java",
			"import java.util.List;\n" +
			"interface Getter<E> {\n" +
			"    E get(List<E> list, int i);\n" +
			"}\n" +
			"public class X<U> {\n" +
			"	public void foo(List<U> l) {\n" +
			"		Getter<U> g= (x, i) -> x.get(i);\n" +
			"		Getter<U> g1= (x, i) -> x.get(i);\n" +
			"	} \n" +
			"}\n"
			);

	String str = this.workingCopies[0].getSource();

	String selection = "x,";
	int start = str.indexOf(selection);
	IJavaElement[] elements = this.workingCopies[0].codeSelect(start, 1);
	IMethod lambda = (IMethod) elements[0].getParent();
	String memento = lambda.getHandleIdentifier();
	assertEquals("Incorrect memento string",
			"=Resolve/src<{X.java[X~foo~QList\\<QU;>;=)=\"LGetter\\<TU;>;!144!161!152=&get!2=\"Ljava.util.List\\<TU;>;=\"x=\"I=\"i=\"TU;=\"LX\\~Getter\\<LX;:TU;>;.get\\(Ljava\\/util\\/List\\<TU;>;I)TU;@x!145!145!145!145!Ljava\\/util\\/List;!0!true@i!148!148!148!148!I!0!true=&",
			memento);
	IJavaElement result = JavaCore.create(memento);
	assertEquals("Java elements should be equal", lambda, result);
	LambdaExpression expression = (LambdaExpression) lambda.getParent();
	memento = expression.getHandleIdentifier();
	assertEquals("Incorrect memento string",
			"=Resolve/src<{X.java[X~foo~QList\\<QU;>;=)=\"LGetter\\<TU;>;!144!161!152=&get!2=\"Ljava.util.List\\<TU;>;=\"x=\"I=\"i=\"TU;=\"LX\\~Getter\\<LX;:TU;>;.get\\(Ljava\\/util\\/List\\<TU;>;I)TU;@x!145!145!145!145!Ljava\\/util\\/List;!0!true@i!148!148!148!148!I!0!true=)",
			memento);
	result = JavaCore.create(memento);
	assertEquals("Java elements should be equal", expression, result);

	start = str.lastIndexOf(selection);
	elements = this.workingCopies[0].codeSelect(start, 1);
	lambda = (IMethod) elements[0].getParent();
	memento = lambda.getHandleIdentifier();
	assertEquals("Incorrect memento string",
			"=Resolve/src<{X.java[X~foo~QList\\<QU;>;=)=\"LGetter\\<TU;>;!180!197!188=&get!2=\"Ljava.util.List\\<TU;>;=\"x=\"I=\"i=\"TU;=\"LX\\~Getter\\<LX;:TU;>;.get\\(Ljava\\/util\\/List\\<TU;>;I)TU;@x!181!181!181!181!Ljava\\/util\\/List;!0!true@i!184!184!184!184!I!0!true=&",
			memento);
	result = JavaCore.create(memento);
	assertEquals("Java elements should be equal", lambda, result);
	expression = (LambdaExpression) lambda.getParent();
	memento = expression.getHandleIdentifier();
	assertEquals("Incorrect memento string",
			"=Resolve/src<{X.java[X~foo~QList\\<QU;>;=)=\"LGetter\\<TU;>;!180!197!188=&get!2=\"Ljava.util.List\\<TU;>;=\"x=\"I=\"i=\"TU;=\"LX\\~Getter\\<LX;:TU;>;.get\\(Ljava\\/util\\/List\\<TU;>;I)TU;@x!181!181!181!181!Ljava\\/util\\/List;!0!true@i!184!184!184!184!I!0!true=)",
			memento);
	result = JavaCore.create(memento);
	assertEquals("Java elements should be equal", expression, result);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430307,  [1.8][model] NPE trying to get children of a LambdaExpression restored from handleIdentifier
public void test430307() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/Resolve/src/X.java",
			"import java.util.List;\n" +
			"interface Getter<E> {\n" +
			"    E get(List<E> list, int i);\n" +
			"}\n" +
			"public class X<U> {\n" +
			"	public void foo(List<U> l) {\n" +
			"		Getter<U> g= (x, i) -> x.get(i);\n" +
			"	} \n" +
			"}\n"
			);

	String str = this.workingCopies[0].getSource();

	String selection = "x,";
	int start = str.indexOf(selection);
	IJavaElement[] elements = this.workingCopies[0].codeSelect(start, 1);
	ILocalVariable local = (ILocalVariable) elements[0];
	String memento = local.getHandleIdentifier();
	assertEquals("Incorrect memento string",
			"=Resolve/src<{X.java[X~foo~QList\\<QU;>;=)=\"LGetter\\<TU;>;!144!161!152=&get!2=\"Ljava.util.List\\<TU;>;=\"x=\"I=\"i=\"TU;=\"LX\\~Getter\\<LX;:TU;>;.get\\(Ljava\\/util\\/List\\<TU;>;I)TU;@x!145!145!145!145!Ljava\\/util\\/List;!0!true@i!148!148!148!148!I!0!true=&@x!145!145!145!145!Ljava.util.List\\<LU;>;!0!true",
			memento);
	IJavaElement result = JavaCore.create(memento);
	assertEquals("Java elements should be equal", local, result);

	IJavaElement parentMethod = result.getParent();
	IJavaElement parentExpr = parentMethod.getParent();
	IMethod lambda = (IMethod) elements[0].getParent();
	memento = lambda.getHandleIdentifier();
	assertEquals("Incorrect memento string",
			"=Resolve/src<{X.java[X~foo~QList\\<QU;>;=)=\"LGetter\\<TU;>;!144!161!152=&get!2=\"Ljava.util.List\\<TU;>;=\"x=\"I=\"i=\"TU;=\"LX\\~Getter\\<LX;:TU;>;.get\\(Ljava\\/util\\/List\\<TU;>;I)TU;@x!145!145!145!145!Ljava\\/util\\/List;!0!true@i!148!148!148!148!I!0!true=&",
			memento);
	result = JavaCore.create(memento);
	assertEquals("Java elements should be equal", lambda, result);
	assertEquals("Java elements should be equal", result, parentMethod);
	LambdaExpression expression = (LambdaExpression) lambda.getParent();
	memento = expression.getHandleIdentifier();
	assertEquals("Incorrect memento string",
			"=Resolve/src<{X.java[X~foo~QList\\<QU;>;=)=\"LGetter\\<TU;>;!144!161!152=&get!2=\"Ljava.util.List\\<TU;>;=\"x=\"I=\"i=\"TU;=\"LX\\~Getter\\<LX;:TU;>;.get\\(Ljava\\/util\\/List\\<TU;>;I)TU;@x!145!145!145!145!Ljava\\/util\\/List;!0!true@i!148!148!148!148!I!0!true=)",
			memento);
	LambdaExpression recreatedType = (LambdaExpression) JavaCore.create(memento);
	assertEquals("Java elements should be equal", expression, recreatedType);
	assertEquals("Java elements should be equal", recreatedType, parentExpr);
	LambdaMethod child = (LambdaMethod) recreatedType.getChildren()[0];
	assertEquals("Java elements should be equal", lambda, child);
}
public void test430307a() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/Resolve/src/X.java",
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
	String str = this.workingCopies[0].getSource();

	String selection = "abc)";
	int start = str.indexOf(selection);
	IJavaElement[] elements = this.workingCopies[0].codeSelect(start, 3);
	ILocalVariable local = (ILocalVariable) elements[0];
	String memento = local.getHandleIdentifier();
		assertEquals(
				"Incorrect memento string",
				"=Resolve/src<{X.java[X~main~\\[QString;=)=\"LI;!103!169!110=&doit!1=\"LI;=\"pqr=\"LI;=\"LX\\~I;.doit\\(LI;)"
				+ "LI;@pqr!104!106!104!106!LI;!0!true=&=)=\"LI;!124!164!131=&doit!1=\"LI;=\"xyz=\"LI;=\"LX\\~I;.doit\\(LI;)"
				+ "LI;@xyz!125!127!125!127!LI;!0!true=&=)=\"LI;!146!157!153=&doit!1=\"LI;=\"abc=\"LI;=\"LX\\~I;.doit\\(LI;)"
				+ "LI;@abc!147!149!147!149!LI;!0!true=&@abc!147!149!147!149!LI;!0!true",
				memento);
	IJavaElement result = JavaCore.create(memento);
	assertEquals("Java elements should be equal", local, result);

	IJavaElement parentMethod = result.getParent();
	IJavaElement parentExpr = parentMethod.getParent();
	assertEquals("Java elements should be equal", parentMethod, local.getParent());
	assertEquals("Java elements should be equal", parentExpr, local.getParent().getParent());

	selection = "xyz)";
	start = str.lastIndexOf(selection);
	elements = this.workingCopies[0].codeSelect(start, 3);
	local = (ILocalVariable) elements[0];
	memento = local.getHandleIdentifier();
	assertEquals("Incorrect memento string",
			"=Resolve/src<{X.java[X~main~\\[QString;=)=\"LI;!103!169!110=&doit!1=\"LI;=\"pqr=\"LI;=\"LX\\~I;.doit\\(LI;)"
			+ "LI;@pqr!104!106!104!106!LI;!0!true=&=)=\"LI;!124!164!131=&doit!1=\"LI;=\"xyz=\"LI;=\"LX\\~I;.doit\\(LI;)"
			+ "LI;@xyz!125!127!125!127!LI;!0!true=&@xyz!125!127!125!127!LI;!0!true",
			memento);
	result = JavaCore.create(memento);
	assertEquals("Java elements should be equal", local, result);

	parentMethod = result.getParent();
	parentExpr = parentMethod.getParent();
	assertEquals("Java elements should be equal", parentMethod, local.getParent());
	assertEquals("Java elements should be equal", parentExpr, local.getParent().getParent());

	selection = "pqr)";
	start = str.indexOf(selection);
	elements = this.workingCopies[0].codeSelect(start, 3);
	local = (ILocalVariable) elements[0];
	memento = local.getHandleIdentifier();
	assertEquals("Incorrect memento string",
			"=Resolve/src<{X.java[X~main~\\[QString;=)=\"LI;!103!169!110=&doit!1=\"LI;=\"pqr=\"LI;=\"LX\\~I;.doit\\(LI;)"
			+ "LI;@pqr!104!106!104!106!LI;!0!true=&@pqr!104!106!104!106!LI;!0!true",
			memento);
	result = JavaCore.create(memento);
	assertEquals("Java elements should be equal", local, result);

	parentMethod = result.getParent();
	parentExpr = parentMethod.getParent();
	assertEquals("Java elements should be equal", parentMethod, local.getParent());
	assertEquals("Java elements should be equal", parentExpr, local.getParent().getParent());
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=439234, [1.8][navigation] Clicking F3 on a lambda arrow doesn't work
public void test439234() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"interface I {" +
			"  int foo(int x);" +
			"}" +
			"public class X {" +
			"  int bar(int x) {\n" +
			"      return i;\n" +
			"  }\n" +
			"  public static void main(String[] args) {" +
			"    I i = (x) -> {" +
			"      return x;" +
			"    };" +
			"   i.foo(10);" +
			"   X x = new X();\n" +
			"   I i2 = x::bar;\n" +
			"   i2.foo(10);\n" +
			"  }" +
			"}");


	// check if selection of -> works
	// ----------------------------------
	String str = this.wc.getSource();
	String selection = "->";
	int start = str.indexOf(selection);
	int length = selection.length();
	IJavaElement[] elements;

	elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"foo(int) [in I [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]",
		elements
	);

	// Length of the selection is added to the offset.
	// The target function is not identified without the fix.
	elements = this.wc.codeSelect(start + length, 0);
	assertElementsEqual(
		"Unexpected elements",
		"foo(int) [in I [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]",
		elements
	);

	selection = "-> "; // Extra space
	start = str.indexOf(selection);
	length = selection.length();
	elements = this.wc.codeSelect(start, length);
	// Can't figure out the target
	assertElementsEqual("Expected no message",	"",	elements);
	elements = this.wc.codeSelect(start + length, 0);
	// Can't figure out the target
	assertElementsEqual("Expected no message",	"",	elements);

	selection = "-> {"; //illegal selection -> {
	start = str.indexOf(selection);
	length = selection.length();
	elements = this.wc.codeSelect(start, length);
	// Can't figure out the target
	assertElementsEqual("Expected no message",	"",	elements);
	elements = this.wc.codeSelect(start + length, 0);
	// Can't figure out the target
	assertElementsEqual("Expected no message",	"",	elements);

	// ----------------------------------
	// Check if selection of :: works
	selection = "::";
	start = str.indexOf(selection);
	length = selection.length();
	elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"foo(int) [in I [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
	elements = this.wc.codeSelect(start + length, 0);
	assertElementsEqual(
		"Unexpected elements",
		"foo(int) [in I [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=440731, [1.8] Hover, F3 doesn't work for method reference in method invocation
public void test440731() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"class Y {\n" +
			"	public void fooY() {return;}\n" +
			"	public void bar(I2 i) {return;}\n" +
			"	public void bar(I i) {return;}   \n" +
			"}\n" +
			"class fooY() {}\n" +
			"interface I { void fooI(Y y); }\n" +
			"interface I2 { void fooI2(int n);}\n" +
			"public class X {\n" +
			"	void foo() {\n" +
			"		I i = Y::fooY; // works\n" +
			"	}\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "fooY";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	IJavaElement[] elements;

	elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"fooY() [in Y [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]",
		elements
	);

	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"class Y {\n" +
			"	public void fooY() {return;}\n" +
			"	public void bar(I2 i) {return;}\n" +
			"	public void bar(I i) {return;}   \n" +
			"}\n" +
			// [1]: Why class fooY {} ?
			"class fooY{}\n" +
			"interface I { void fooI(Y y); }\n" +
			"interface I2 { void fooI2(int n);}\n" +
			"public class X {\n" +
			"	void foo() {\n" +
			"		Y y = new Y();\n" +
			"		y.bar(Y::fooY);\n" +
			"	}\n" +
			"}");
	str = this.wc.getSource();
	selection = "::";
	//y.bar(Y::fooY)
	//       ^^
	start = str.lastIndexOf(selection);
	length = selection.length();

	elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"fooI(Y) [in I [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]",
		elements);

	// [1] The reason for having the class fooY {} as part of the test case:
	// Without the fix we resolve to the type fooY (class) and not the method fooY()
	// declared in class Y. Please see Comment 4.

	selection = "fooY";
	//y.bar(Y::fooY)
	//         ^^^^

	start = str.lastIndexOf(selection);
	length = selection.length();

	// Unable to find element without fix.
	elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"fooY() [in Y [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]",
		elements);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430572,  [1.8] CCE on hovering over 'super' in lambda expression
public void test430572() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"@FunctionalInterface\n" +
			"interface FI {\n" +
			"	default int getID() {\n" +
			"		return 11;\n" +
			"	}\n" +
			"	void print();\n" +
			"}\n" +
			"class T {\n" +
			"	FI f2 = () -> System.out.println(super.toString());\n" +
			"}\n");

	String str = this.wc.getSource();
	String selection = "super";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	IJavaElement[] elements;

	elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Object [in Object.class [in java.lang [in "+ getExternalPath() + "jclFull1.8.jar]]]",
		elements
	);

	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"class Y {\n" +
			"	public void fooY() {return;}\n" +
			"	public void bar(I2 i) {return;}\n" +
			"	public void bar(I i) {return;}   \n" +
			"}\n" +
			// [1]: Why class fooY {} ?
			"class fooY{}\n" +
			"interface I { void fooI(Y y); }\n" +
			"interface I2 { void fooI2(int n);}\n" +
			"public class X {\n" +
			"	void foo() {\n" +
			"		Y y = new Y();\n" +
			"		y.bar(Y::fooY);\n" +
			"	}\n" +
			"}");
	str = this.wc.getSource();
	selection = "::";
	//y.bar(Y::fooY)
	//       ^^
	start = str.lastIndexOf(selection);
	length = selection.length();

	elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"fooI(Y) [in I [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]",
		elements);

	// [1] The reason for having the class fooY {} as part of the test case:
	// Without the fix we resolve to the type fooY (class) and not the method fooY()
	// declared in class Y. Please see Comment 4.

	selection = "fooY";
	//y.bar(Y::fooY)
	//         ^^^^

	start = str.lastIndexOf(selection);
	length = selection.length();

	// Unable to find element without fix.
	elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"fooY() [in Y [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]",
		elements);
}
// nested poly invocation:
public void testBug487791() throws JavaModelException {
	this.wc = getWorkingCopy(
			"Resolve/src/Example.java",
			"import java.util.Comparator;\n" +
			"import java.util.stream.Collectors;\n" +
			"\n" +
			"interface Something {\n" +
			"      public int getSize();\n" +
			"      public Instant getTime();\n" +
			"}\n" +
			"interface Instant extends Comparable<Instant> {\n" +
			"}\n" +
			"public class Example {\n" +
			"   public void test2() {\n" +
			"      java.util.stream.Collector<Something,?,java.util.Map<Integer,Something>> c = \n" +
			"      Collectors.collectingAndThen(\n" +
			"            Collectors.<Something>toList(),\n" +
			"            list -> list.stream().collect(Collectors.groupingBy(Something::getSize,\n" +
			"                     // Returns Collector<Something,?,Object> - INCORRECT!\n" +
			"                     Collectors.collectingAndThen(\n" + // <-- select here
			"                        Collectors.<Something>toList(),\n" +
			"                        list2 -> list2.stream().sorted(Comparator.comparing(Something::getTime)).limit(1).findAny().orElse(null)\n" +
			"                     )\n" +
			"                  )));\n" +
			"   }\n" +
			"}\n");
	this.wc.becomeWorkingCopy(null);

	String str = this.wc.getSource();
	String selection = "collectingAndThen";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"collectingAndThen(java.util.stream.Collector<T,A,R>, java.util.function.Function<R,RR>) [in Collectors [in Collectors.class [in java.util.stream [in "+ getExternalPath() + "jclFull1.8.jar]]]]",
		elements
	);
	String signature = new BindingKey(((IMethod) elements[0]).getKey()).toSignature();
	String[] typeArguments = Signature.getTypeArguments(signature);
	assertEquals("number of type arguments", 3, typeArguments.length);
	assertEquals("4th type argument", "LSomething;", typeArguments[2]);
	String returnType = Signature.getReturnType(signature);
	assertEquals("return type", "Ljava.util.stream.Collector<LSomething;!*LSomething;>;", returnType);
}
// ReferenceExpression:
public void testBug487791b() throws JavaModelException {
	this.wc = getWorkingCopy(
			"Resolve/src/Example.java",
			"import java.util.function.Function;\n" +
			"\n" +
			"public class Example {\n" +
			"   static <T> T id(T t) { return t; }\n" +
			"   static <T,X> T f1 (X x) { return null; }\n" +
			"   \n" +
			"   String test() {\n" +
			"	   return f3(y -> y.f2(Example::f1, id(y)));\n" +  // <- select f1 here
			"   }\n" +
			"   <U,V> V f2(Function<U, V> f, U u) {return f.apply(null);}\n" +
			"   <R> R f3(Function<Example,R> f) { return null; }\n" +
			"}\n");
	this.wc.becomeWorkingCopy(null);

	String str = this.wc.getSource();
	String selection = "f1";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"f1(X) [in Example [in [Working copy] Example.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
	BindingKey bindingKey = new BindingKey(((IMethod) elements[0]).getKey());
	String signature = bindingKey.toSignature();
	assertEquals("signature", "<T:Ljava.lang.Object;X:Ljava.lang.Object;>(LExample;)Ljava.lang.String;", signature);
	String[] typeArguments = bindingKey.getTypeArguments();
	assertEquals("number of type arguments", 2, typeArguments.length);
	assertEquals("1st type argument", "Ljava.lang.String;", typeArguments[0]);
	assertEquals("2nd type argument", "LExample;", typeArguments[1]);
}
public void testBug515758() throws JavaModelException {
	this.wc = getWorkingCopy(
			"Resolve/src/Snippet.java",
			"import java.util.function.Function;\n" +
			"\n" +
			"public class Snippet {\n" +
			"    void m1() {\n" +
			"    	MyObservable.range(1, 2).groupBy(integer -> {\n" +
			"	 		return \"even\";\n" +
			"		});\n" +
			"    }\n" +
			"\n" +
			"}\n" +
			"class MyObservable<T> {\n" +
			"	static MyObservable<Integer> range(int i1, int i2) {\n" +
			"		return new MyObservable<>();\n" +
			"	}\n" +
			"	<K> void groupBy(Function<T, K> func) {\n" +
			"	}\n" +
			"}");
	String str = this.wc.getSource();
	String selection = "range";
	int start = str.indexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"range(int, int) [in MyObservable [in [Working copy] Snippet.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1264
// NPE below LambdaExpression.copy()
public void testGH1264() throws Exception {
	this.wc = getWorkingCopy(
			"Resolve/src/Snippet.java",
			"import java.io.File;\n"
			 + "import java.util.ArrayList;\n"
			 + "import java.util.Collection;\n"
			 + "import java.util.stream.Stream;\n"
			 + "\n"
			 + "public class Snippet {\n"
			 + "	public static void main(String[] args) {\n"
			 + "		Collection<File> rootDirectories = new ArrayList<>();\n"
			 + "		Stream<Object> directories = rootDirectories.stream()\n"
			 + "				.map(dir -> dir.listFiles(File::isDirectory));\n"
			 + "		System.out.println(directories);\n"
			 + "	}\n"
			 + "}");
	String str = this.wc.getSource();
	String selection = "->";
	int start = str.indexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"apply(T) [in Function [in Function.class [in java.util.function [in " + getExternalPath() + "jclFull1.8.jar]]]]",
		elements
	);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1195
// Open declaration results in ClassCastException: LocalDeclaration cannot be cast to LambdaExpression
public void testGH1195() throws Exception {
	this.wc = getWorkingCopy(
			"Resolve/src/Reproducer.java",
			"import java.util.function.Predicate;\n" +
			"\n" +
			"public class Reproducer {\n" +
			"\n" +
			"    private final Predicate<Object> predicate =\n" +
			"            input -> (input instanceof String withoutThisVariableNameThereIsNoError);\n" +
			"}\n");
	String str = this.wc.getSource();
	String selection = "predicate";
	int start = str.indexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"predicate [in Reproducer [in [Working copy] Reproducer.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=576252
// Open declaration / Javadoc popup is confused by overloaded method with method reference
public void testBug576252() throws Exception {
	this.wc = getWorkingCopy(
			"Resolve/src/LambdaTest.java",
			"public class LambdaTest {\n" +
			"	public static void method(String value) {\n" +
			"		System.out.print(\"para\");\n" +
			"	}\n" +
			"\n" +
			"	public static void method(java.util.function.Supplier<String> supplier) {\n" +
			"		System.out.print(supplier.get());\n" +
			"	}\n" +
			"\n" +
			"	public static void main(String[] args) {\n" +
			"		LambdaTest.method(LambdaTest.class::toString);\n" +
			"		System.out.print(\"extra\");\n" +
			"	}\n" +
			"}\n");
	String str = this.wc.getSource();
	String selection = "method";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"method(java.util.function.Supplier<String>) [in LambdaTest [in [Working copy] LambdaTest.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=578011
// Open Declaration (F3) not navigating to static method in same class
public void testBug578011() throws Exception {
	this.wc = getWorkingCopy(
			"Resolve/src/EclipseOpenDeclarationBug.java",
			"import java.lang.reflect.Constructor;\n" +
			"import java.util.HashSet;\n" +
			"import java.util.Optional;\n" +
			"import java.util.Set;\n" +
			"\n" +
			"public class EclipseOpenDeclarationBug {\n" +
			"  public EclipseOpenDeclarationBug(Class<?> cls) {\n" +
			"    Set<Constructor<?>> constructors = new HashSet<>();\n" +
			"\n" +
			"    getPublicEmptyConstructor(cls).ifPresent(c -> {\n" +
			"      if(constructors.isEmpty()) {\n" +
			"        constructors.add(c);\n" +
			"      }\n" +
			"    });\n" +
			"\n" +
			"    if(constructors.size() < 1) {\n" +
			"      throw new IllegalArgumentException(\"No suitable constructor found; provide an empty constructor or annotate one with @Inject: \" + cls);\n" +
			"    }\n" +
			"  }\n" +
			"\n" +
			"  private static <T> Optional<Constructor<T>> getPublicEmptyConstructor(Class<T> cls) {\n" +
			"    try {\n" +
			"      return Optional.of(cls.getConstructor());\n" +
			"    }\n" +
			"    catch(NoSuchMethodException e) {\n" +
			"      return Optional.empty();\n" +
			"    }\n" +
			"  }\n" +
			"}\n");
	String str = this.wc.getSource();
	String selection = "getPublicEmptyConstructor";
	int start = str.indexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"getPublicEmptyConstructor(Class<T>) [in EclipseOpenDeclarationBug [in [Working copy] EclipseOpenDeclarationBug.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=546563
// [navigation] Open Declaration not working
public void testBug546563() throws Exception {
	this.wc = getWorkingCopy(
			"Resolve/src/Test.java",
			"import java.util.Optional;\n" +
			"\n" +
			"public class Test {\n" +
			"\n" +
			"  public void xyz() {\n" +
			"    getOptionalValue().ifPresent(val -> {\n" +
			"      int i = 1;\n" +
			"      System.out.print(val);\n" +
			"    });\n" +
			"    try {\n" +
			"    } catch (Exception e) {\n" +
			"    }\n" +
			"  }\n" +
			"\n" +
			"  public Optional<String> getOptionalValue() {\n" +
			"    return Optional.empty();\n" +
			"  }\n" +
			"}\n");
	String str = this.wc.getSource();
	String selection = "getOptionalValue";
	int start = str.indexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"getOptionalValue() [in Test [in [Working copy] Test.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}

// Inspired from ReslveTests18.test0027
public void test0027_BindingForLambdaMethod() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"""
			interface I {
			    I doit(I xyz);
			}
			public class X {
				public static void main(String[] args) {
					I i = (pqr) -> {
						return (xyz) -> {
							return (abc) -> abc;
						};
					};
				}
			}
			""");

	String str = this.wc.getSource();
	String selection = "abc";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
	parser.setSource(this.wc);
	parser.setProject(this.wc.getJavaProject());
	parser.setWorkingCopyOwner(this.wc.getOwner());
	parser.setCompilerOptions(this.wc.getOptions(true));
	parser.setResolveBindings(true);
	parser.setBindingsRecovery(true);
	parser.setStatementsRecovery(true);
	CompilationUnit dom = (CompilationUnit)parser.createAST(null);
	Name variable = (Name)new NodeFinder(dom, start, length).getCoveredNode();
	IJavaElement javaElement = variable.resolveBinding().getJavaElement();	

	assertElementsEqual(
		"Unexpected elements",
		"abc [in doit(I) [in <lambda #1> [in doit(I) [in <lambda #1> [in doit(I) [in <lambda #1> [in main(String[]) [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]]]]]]]",
		new IJavaElement[] { javaElement }
	);
}
}
