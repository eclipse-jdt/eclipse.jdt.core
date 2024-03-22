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
			"""
				interface I {
					void foo();
				}
				public class X {
					static void goo() {}
					public static void main(String[] args) {
						I i = X::goo;
					}
				}
				""");

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
			"""
				interface I {
					void foo();
				}
				class Y {
					static void goo() {}
				}
				public class X {
					public static void main(String[] args) {
						I i = X::goo;
					}
				}
				""");

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
			"""
				interface I {
					void foo();
				}
				class Y {
					private static void goo() {}
				}
				public class X extends Y {
					public static void main(String[] args) {
						I i = X::goo;
					}
				}
				""");

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
			"""
				interface I {
					void foo();
				}
				class Y {
					static void goo() {}
				}
				public class X extends Y {
					public static void main(String[] args) {
						I i = X::goo;
					}
				}
				""");

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
			"""
				interface I {
					void foo();
				}
				public class X {
					void goo() {}
					public static void main(String[] args) {
						I i = new X()::goo;
					}
				}
				""");

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
			"""
				interface I {
					void foo();
				}
				class Y {
					void goo() {}
				}
				public class X extends Y {
					public static void main(String[] args) {
						I i = new X()::goo;
					}
				}
				""");

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
			"""
				interface I {
					void foo();
				}
				public class X {
					static void goo() {}
					public static void main(String[] args) {
						I i = new X()::goo;
					}
				}
				""");

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
			"""
				interface I {
					void foo(X x);
				}
				class Y {
					void goo() {}
				}
				public class X extends Y {
					public static void main(String[] args) {
						I i = new X()::goo;
					}
				}
				""");

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
			"""
				interface I {
					void foo(X x);
				}
				class Y {
					static void goo() {}
				}
				public class X extends Y {
					public static void main(String[] args) {
						I i = X::goo;
					}
				}
				""");

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
			"""
				interface I {
					void foo(X x);
				}
				class Y {
					void goo() {}
				}
				public class X extends Y {
					public static void main(String[] args) {
						I i = X::goo;
					}
				}
				""");

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
			"""
				interface I {
					void foo(X x);
				}
				class Y {
				}
				public class X extends Y {
					static void goo(X x) {}
					void goo() {}
					public static void main(String[] args) {
						I i = X::goo;
					}
				}
				""");

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
			"""
				interface I {
					void foo(X x);
				}
				class Y {
				}
				public class X extends Y {
					static void goo(X x) {}
					void goo() {}
					public static void main(String[] args) {
						I i = X::goo;
					}
				}
				""");

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
			"""
				interface I {
					void foo(X x);
				}
				class Y {
				}
				public class X extends Y {
					static void goo(X x) {}
					void goo() {}
					public static void main(String[] args) {
						I i = X::<Y>goo;
					}
				}
				""");

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
			"""
				interface I {
					X foo(int x);
				}
				class Y {}
				public class X {
					public static void main(String[] args) {
						I i = X::<Y>new;
					}
				}
				""");

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
			"""
				interface I {
					X foo(int x);
				}
				class Y {}
				public class X {
				   X(long i) {}
					public static void main(String[] args) {
						I i = X::<Y>new;
					}
				}
				""");

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
			"""
				interface I {
					Y foo(int x);
				}
				class Y {}
				public class X {
				   X(long i) {}
					public static void main(String[] args) {
						I i = X::<Y>new;
					}
				}
				""");

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
			"""
				interface I {
					Y foo(int x);
				}
				class Y {}
				public class X extends Y {
				   X(long i) {}
					public static void main(String[] args) {
						I i = X::<Y>new;
					}
				}
				""");

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
			"""
				interface I {
					X foo(int x);
				}
				class Y {}
				public class X {
				   X(Integer i) {}
					public static void main(String[] args) {
						I i = X::<Y>new;
					}
				}
				""");

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
			"""
				interface I {
					int foo();
				}
				class Y {}
				public class X {
					public static void main(String[] args) {
						I i = new X()::<Y>hashCode;
					}
				}
				""");

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
			"""
				interface I {
					int foo();
				}
				class Y {}
				public class X {
					public static void main(String[] args) {
						I i = ((I)()->0)::<Y>hashCode;
					}
				}
				""");

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
			"""
				interface I {
					int foo();
				}
				class Y {
				    int foo() { return 10;}
				}
				public class X {
					public void main(String[] args) {
						I i = super::foo;
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
		"",
		elements
	);
}
// Test (positive) super:: style method references - such constructor references are not grammatical.
public void test0022() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src/X.java",
			"""
				interface I {
					int foo();
				}
				class Y {
				    int foo() { return 10;}
				}
				public class X extends Y {
					public void main(String[] args) {
						I i = super::foo;
					}
				}
				""");

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
			"""
				interface I {
					int [] foo(int x);
				}
				class Y {
				    int foo() { return 10;}
				}
				public class X extends Y {
					public void main(String[] args) {
						I i = int []::new;
					}
				}
				""");

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
			"""
				interface I {
					int foo(int a);
				}
				public class X {\t
					void foo() {
						I i = (xyz) -> {
							return xyz;
						};
					}
				}
				""");

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
			"""
				interface I {
					int foo(int a);
				}
				public class X {\t
					void foo() {
						I i = (abc) -> abc++;\s
					}
				}
				""");

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
			"""
				interface I {
					int foo(int a);
				}
				public class X {\t
					I i = (abc) -> abc++;\s
				}
				""");

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
			"""
				interface I {
				    I doit(I xyz);
				}
				public class X {\s
					public static void main(String[] args) {
						I i = (pqr) -> {
							return (xyz) -> {
								return (abc) -> abc;\s
							};
						};
					}
				}
				""");
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
			"""
				interface I {
				    I doit(I xyz);
				}
				public class X {\s
					public static void main(String[] args) {
						I i = (pqr) -> {
							return (xyz) -> {
								return (abc) -> xyz;\s
							};
						};
					}
				}
				""");
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
			"""
				interface I {
				    I doit(I xyz);
				}
				public class X {\s
					public static void main(String[] args) {
						I i = (pqr) -> {
							return (xyz) -> {
								return (abc) -> args;\s
							};
						};
					}
				}
				""");
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
			"""
				interface I {
				    I doit(I xyz);
				}
				public class X {\s
					X fx = new X((pqr) -> {
						return (zyx) -> {
							return (abc) -> zyx;\s
						};
					});
					X(I i) {
					}
					void foo(X x) {}
					public static void main(String[] args) {
						X x = null;
						x = new X((pqr) -> {
							return (xyz) -> {
								return (abc) -> xyz;\s
							};
						});
						System.out.println(x);
					}
				}
				""");
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
			"""
				interface I {
				    I doit(I xyz);
				}
				public class X {\s
					X(I i) {
					}
					void foo(X x) {}
					public static void main(String[] args) {
						X x = null;
						x = new X((pqr) -> {
							return (xyz) -> {
								return (abc) -> xyz;\s
							};
						});
						System.out.println(x);
					}
				}
				""");
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
			"""
				interface I {
				    I doit(I xyz);
				}
				public class X {\s
					X fx = new X((pqr) -> {
						return (xyz) -> {
							return (abc) -> xyz;\s
						};
					});
					X(I i) {
					}
					void foo(X x) {}
					public static void main(String[] args) {
						X x = null;
						I i = args != null ? (mno) -> mno : (def) -> (hij) -> {
							return hij;
						};
					}
				}
				""");
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
			"""
				interface I {
				    I doit(I xyz);
				}
				public class X {\s
					X fx = new X((pqr) -> {
						return (xyz) -> {
							return (abc) -> xyz;\s
						};
					});
					X(I i) {
					}
					void foo(X x) {}
					public static void main(String[] args) {
						X x = null;
						I i;
				       i = args != null ? (mno) -> mno : (def) -> (hij) -> {
							return hij;
						};
					}
				}
				""");
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
		String source = """
			package p;
			public class X {
			  FI i1 = (a, barg) -> a+barg;
			}
			interface FI { int f1(int a, int b); }
			""";
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
		String source = """
			package p;
			public class X {
			  void foo() {
				FI i2 = (a, barg) -> { return a+barg; };
			  }
			}
			interface FI { int f1(int a, int b); }
			""";
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
		String source = """
			package p;
			public class X {
			  void foo() {
				FI i2 = (a, barg) -> { int x = 2; while (x < 2) { x++; } return a+barg; };
			  }
			}
			interface FI { int f1(int a, int b); }
			""";
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
		String source = """
			package p;
			public class X {
			  FI i1 = (barg) -> ++barg;
			}
			interface FI { int f1(int b); }
			""";
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
		String source = """
			package p;
			public class X {
			  FI i1 = (aarg) -> { return aarg++;};
			}
			interface FI { int f1(int a); }
			""";
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
		String source = """
			package p;
			public class X {
			  FI i1 = (aarg) -> {  int x = aarg; return aarg++;};
			}
			interface FI { int f1(int a); }
			""";
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
		String source = """
			package p;
			public class X {
			 public void boo(FI fi) {}
			  void foo() {
				boo((aarg) -> aarg++);
			  }
			}
			interface FI { int f1(int a); }
			""";
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
		String source = """
			package p;
			public class X {
			 public void boo(FI fi) {}
			  void foo() {
				boo((aarg) -> {int b = 10; return aarg++;});
			  }
			}
			interface FI { int f1(int a); }
			""";
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
		String source = """
			package p;
			public class X {
			 public void boo(FI fi) {}
			  void foo() {
				boo((aarg, x) -> x + aarg++);
			  }
			}
			interface FI { int f1(int a, int b); }
			""";
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
		String source = """
			package p;
			public class X {
			 public void boo(FI fi) {}
			  void foo() {
				boo((aarg, x) -> {int b = 10; return x + aarg++;});
			  }
			}
			interface FI { int f1(int a, int b); }
			""";
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
		String source = """
			package p;
			public class X {
			 public void boo(int x, int y, FI fi) {}
			  void foo() {
				boo(2, 4, (aarg) -> aarg++);
			  }
			}
			interface FI { int f1(int a); }
			""";
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
		String source = """
			package p;
			public class X {
			 public void boo(int x, FI fi) {}
			  void foo() {
				boo(2, (aarg) -> {int b = 10; return aarg++;});
			  }
			}
			interface FI { int f1(int a); }
			""";
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
		String source = """
			package p;
			public class X {
			 public void boo(int x, int y, FI fi) {}
			  void foo() {
				boo(2, 5+6, (aarg, x) -> x + aarg++);
			  }
			}
			interface FI { int f1(int a, int b); }
			""";
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
		String source = """
			package p;
			public class X {
			 public void boo(int x, FI fi) {}
			  void foo() {
				boo(2, (aarg, x) -> {int b = 10; return x + aarg++;});
			  }
			}
			interface FI { int f1(int a, int b); }
			""";
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
			"""
				class Collections {
					public static void sort(ArrayList list, Comparator c) {
					}
				}
				interface Comparator {
					int compareTo(X t, X s);
				}
				class ArrayList {
				}
				public class X {
					int compareTo(X x) { return 0; }
					void foo() {
						Collections.sort(new ArrayList(), (X o1, X o2) -> o1.compareTo(o2));
					}
				}
				""");

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
			"""
				import java.util.ArrayList;
				import java.util.Arrays;
				import java.util.Collections;
				import java.util.Comparator;
				public class X {
				   int compareTo(X x) { return 0; }
					void foo() {
						Collections.sort(new ArrayList<X>(Arrays.asList(new X(), new X(), new X()),
								new Comparator<X>() {
									@Override
									public int compare(X o1, X o2) {
										return o1.compareTo(o2);
									}
								});
					}
				}
				""");

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
			"""
				interface I {
					void foo(int x, int y);
				}
				public class X {
					I i = (first, second) -> { System.out.println(first); };
				}
				""");

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
			"""
				interface I {
					void foo(X x, Object y);
				}
				public class X {
					I i = (first, second) -> { System.out.println(); };
				}
				""");

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
			"""
				interface I {
					void foo(X x, Object y);
				}
				public class X {
					I i = (first, second) -> { System.out.println(); };
				}
				""");

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
			"""
				interface I {
					I foo (I x);
				}
				public class X {
					static void goo(I i) {}
					public static void main(String[] args) {
						goo((x) -> (y) -> (z) -> z.foo((p) -> p));
					}
				}\s
				""");

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
			"""
				interface I {
					I foo (I x);
				}
				public class X {
					static void goo(I i) {}
					public static void main(String[] args) {
						goo( x -> y -> z -> z.foo(p -> p));
					}
				}\s
				""");

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
			"""
				interface I {
					J foo(String x, String y);
				}
				interface J {
					K foo(String x, String y);
				}
				interface K {
					int foo(String x, int y);
				}
				public class X {
					static void goo(K i) {}
					public static void main(String[] args) {
						I i = (x, y) -> { return (a, b) -> (p, q) -> a.length(); };
					}
				}
				""");

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
			"""
				interface I {
					J foo(String x, String y);
				}
				interface J {
					K foo(String x, String y);
				}
				interface K {
					int foo(String x, int y);
				}
				public class X {
					static void goo(K i) {}
					public static void main(String[] args) {
						I i = (x, y) -> { return (a, b) -> (p, q) -> a.length(); };
					}
				}
				""");

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
			"""
				interface I {
					int foo(String x, Integer y);
				}
				public class X {
					public static void main(String[] args) {
						I i = (x, y) -> {
							x = "Hello"
							y = 10;	\t
							if (x.length() > y) {
								System.out.println("if");
							} else {
								System.out.println("else");
							}
							return x.length();
						};
						// System.out.println((I) (p, q) -> { return q.
					}
				}
				""");

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
			"""
				public class X {
					static F f = X::m; // [1] Works
					int i = fun(X::m); // [2] Does not work
					public static int m(int x) {
						return x;
					}
					private int fun(F f) {
						return f.foo(0);
					}
				}
				interface F {
					int foo(int x);
				}
				""");

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
			"""
				public class X {
					int i = fun(X::m); // [2] Does not work
					public static int m(int x) {
						return x;
					}
					private int fun(F f) {
						return f.foo(0);
					}
				}
				interface F {
					int foo(int x);
				}
				""");

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
			"""
				import java.util.List;
				import java.util.Map;
				import java.util.stream.Collectors;
				class Person {
					String getLast() { return ""; };
				}
				public class X {
					void test1(List<Person> roster) {
				        Map<String, Person> map =\s
				                roster
				                    .stream()
				                    .collect(
				                        Collectors.toMap(
				                            p -> p.getLast(), //[1]
				                            p -> p            //[2]
				                        ));
					}
				}
				"""
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
			"""
				import java.io.IOException;
				import java.nio.file.Path;
				import java.util.ArrayList;
				import java.util.List;
				import java.util.function.Function;
				import java.util.jar.JarEntry;
				import java.util.jar.JarFile;
				import java.util.stream.Collectors;
				import java.util.stream.Stream;
				class InsistentCapture {
				  static void processJar(Path plugin) throws IOException {
				    try(JarFile jar = new JarFile(plugin.toFile())) {
				      try(Stream<JarEntry> entries = jar.stream()) {
				        Function<? super JarEntry, ? extends String> toName =
				          entry -> entry.getName();
				        Stream<? extends String> stream = entries.map(toName).distinct(); // Ok
				        withWildcard(entries.map(toName).distinct()); // Ok
				        withWildcard(stream); // Ok
				        Stream<String> stream2 = entries.map(toName).distinct(); // ERROR
				        withoutWildcard(entries.map(toName).distinct()); // ERROR
				        withoutWildcard(stream); // ERROR
				        withoutWildcard(stream2); // Ok
				        withoutWildcard(coerce(stream)); // Ok
				        withoutWildcard(stream.map((String v1) -> { // ERROR
				          String r = "" + v1; // Hover on v: Ok
				          return r;
				        }));
				        withoutWildcard(stream.map((v2) -> { // Ok
				          String r = "" + v2; // Hover on v: NOT OK
				          return r;
				        }));
				      }
				    }
				  }
				  private static Stream<String> coerce(Stream<? extends String> stream) {
				    if("1" == "") { return stream.collect(Collectors.toList()).stream(); // ERROR
				    }
				    return stream.collect(Collectors.toList()); // NO ERROR
				  }
				  private static void withWildcard(Stream<? extends String> distinct) {
				    distinct.forEach(s1 -> System.out.println(s1)); // hover on s: NOT OK
				  }
				  private static void withoutWildcard(Stream<String> distinct) {
				    distinct.forEach(s2 -> System.out.println(s2)); // hover on s: Ok
				  }
				}
				"""
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
			"""
				import java.io.IOException;
				import java.nio.file.Path;
				import java.util.ArrayList;
				import java.util.List;
				import java.util.function.Function;
				import java.util.jar.JarEntry;
				import java.util.jar.JarFile;
				import java.util.stream.Collectors;
				import java.util.stream.Stream;
				class InsistentCapture {
				  static void processJar(Path plugin) throws IOException {
				    try(JarFile jar = new JarFile(plugin.toFile())) {
				      try(Stream<JarEntry> entries = jar.stream()) {
				        Function<? super JarEntry, ? extends String> toName =
				          entry -> entry.getName();
				        Stream<? extends String> stream = entries.map(toName).distinct(); // Ok
				        withWildcard(entries.map(toName).distinct()); // Ok
				        withWildcard(stream); // Ok
				        Stream<String> stream2 = entries.map(toName).distinct(); // ERROR
				        withoutWildcard(entries.map(toName).distinct()); // ERROR
				        withoutWildcard(stream); // ERROR
				        withoutWildcard(stream2); // Ok
				        withoutWildcard(coerce(stream)); // Ok
				        withoutWildcard(stream.map((String v1) -> { // ERROR
				          String r = "" + v1; // Hover on v: Ok
				          return r;
				        }));
				        withoutWildcard(stream.map((v2) -> { // Ok
				          String r = "" + v2; // Hover on v: NOT OK
				          return r;
				        }));
				      }
				    }
				  }
				  private static Stream<String> coerce(Stream<? extends String> stream) {
				    if("1" == "") { return stream.collect(Collectors.toList()).stream(); // ERROR
				    }
				    return stream.collect(Collectors.toList()); // NO ERROR
				  }
				  private static void withWildcard(Stream<? extends String> distinct) {
				    distinct.forEach(s1 -> System.out.println(s1)); // hover on s: NOT OK
				  }
				  private static void withoutWildcard(Stream<String> distinct) {
				    distinct.forEach(s2 -> System.out.println(s2)); // hover on s: Ok
				  }
				}
				"""
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
			"""
				import java.io.IOException;
				import java.nio.file.Path;
				import java.util.ArrayList;
				import java.util.List;
				import java.util.function.Function;
				import java.util.jar.JarEntry;
				import java.util.jar.JarFile;
				import java.util.stream.Collectors;
				import java.util.stream.Stream;
				class InsistentCapture {
				  static void processJar(Path plugin) throws IOException {
				    try(JarFile jar = new JarFile(plugin.toFile())) {
				      try(Stream<JarEntry> entries = jar.stream()) {
				        Function<? super JarEntry, ? extends String> toName =
				          entry -> entry.getName();
				        Stream<? extends String> stream = entries.map(toName).distinct(); // Ok
				        withWildcard(entries.map(toName).distinct()); // Ok
				        withWildcard(stream); // Ok
				        Stream<String> stream2 = entries.map(toName).distinct(); // ERROR
				        withoutWildcard(entries.map(toName).distinct()); // ERROR
				        withoutWildcard(stream); // ERROR
				        withoutWildcard(stream2); // Ok
				        withoutWildcard(coerce(stream)); // Ok
				        withoutWildcard(stream.map((String v1) -> { // ERROR
				          String r = "" + v1; // Hover on v: Ok
				          return r;
				        }));
				        withoutWildcard(stream.map((v2) -> { // Ok
				          String r = "" + v2; // Hover on v: NOT OK
				          return r;
				        }));
				      }
				    }
				  }
				  private static Stream<String> coerce(Stream<? extends String> stream) {
				    if("1" == "") { return stream.collect(Collectors.toList()).stream(); // ERROR
				    }
				    return stream.collect(Collectors.toList()); // NO ERROR
				  }
				  private static void withWildcard(Stream<? extends String> distinct) {
				    distinct.forEach(s1 -> System.out.println(s1)); // hover on s: NOT OK
				  }
				  private static void withoutWildcard(Stream<String> distinct) {
				    distinct.forEach(s2 -> System.out.println(s2)); // hover on s: Ok
				  }
				}
				"""
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
			"""
				import java.io.IOException;
				import java.nio.file.Path;
				import java.util.ArrayList;
				import java.util.List;
				import java.util.function.Function;
				import java.util.jar.JarEntry;
				import java.util.jar.JarFile;
				import java.util.stream.Collectors;
				import java.util.stream.Stream;
				class InsistentCapture {
				  static void processJar(Path plugin) throws IOException {
				    try(JarFile jar = new JarFile(plugin.toFile())) {
				      try(Stream<JarEntry> entries = jar.stream()) {
				        Function<? super JarEntry, ? extends String> toName =
				          entry -> entry.getName();
				        Stream<? extends String> stream = entries.map(toName).distinct(); // Ok
				        withWildcard(entries.map(toName).distinct()); // Ok
				        withWildcard(stream); // Ok
				        Stream<String> stream2 = entries.map(toName).distinct(); // ERROR
				        withoutWildcard(entries.map(toName).distinct()); // ERROR
				        withoutWildcard(stream); // ERROR
				        withoutWildcard(stream2); // Ok
				        withoutWildcard(coerce(stream)); // Ok
				        withoutWildcard(stream.map((String v1) -> { // ERROR
				          String r = "" + v1; // Hover on v: Ok
				          return r;
				        }));
				        withoutWildcard(stream.map((v2) -> { // Ok
				          String r = "" + v2; // Hover on v: NOT OK
				          return r;
				        }));
				      }
				    }
				  }
				  private static Stream<String> coerce(Stream<? extends String> stream) {
				    if("1" == "") { return stream.collect(Collectors.toList()).stream(); // ERROR
				    }
				    return stream.collect(Collectors.toList()); // NO ERROR
				  }
				  private static void withWildcard(Stream<? extends String> distinct) {
				    distinct.forEach(s1 -> System.out.println(s1)); // hover on s: NOT OK
				  }
				  private static void withoutWildcard(Stream<String> distinct) {
				    distinct.forEach(s2 -> System.out.println(s2)); // hover on s: Ok
				  }
				}
				"""
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
			"""
				class Y {
				    static void goo(I i) {}
				}
				public class X {
				    public static void main(String [] args) {
				        Y.goo(x -> x * x);
				    }
				}
				interface I {
					int foo(int x);
				}
				""");

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
			"""
				class Y {
				    static void goo(I i) {}
				}
				public class X {
				    public static void main(String [] args) {
				        Y.goo(x -> x * x);
				    }
				}
				interface I {
					int foo(int x);
				}
				""");

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
			"""
				class Y {
				    static void goo(I i) {}
				}
				public class X {
				    public static void main(String [] args) {
				        Y.goo(x -> x * x);
				    }
				}
				interface I {
					int foo(int x);
				}
				""");

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
			"""
				class Y {
				    static void goo(I i) {}
				}
				public class X {
				    static int zoo(int x) { return x; }
				    public static void main(String [] args) {
				        Y.goo(X::zoo);
				    }
				}
				interface I {
					int foo(int x);
				}
				""");

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
			"""
				class Y {
				    static void goo(I i) {}
				}
				public class X {
				    static int zoo(int x) { return x; }
				    public static void main(String [] args) {
				        Y.goo(X::zoo);
				    }
				}
				interface I {
					int foo(int x);
				}
				""");

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
			"""
				class Y {
				    static void goo(I i) {}
				}
				public class X {
				    static int zoo(int x) { return x; }
				    public static void main(String [] args) {
				        Y.goo(X::zoo);
				    }
				}
				interface I {
					int foo(int x);
				}
				""");

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
			"""
				import java.util.ArrayList;
				import java.util.Collections;
				import java.util.Comparator;
				import java.util.List;
				public class X {
					private void foo() {
						List<Person> people= new ArrayList<>();
						Collections.sort(people, Comparator.comparing(p -> p.getLastName()));
					}
				}
				class Person{
					String getLastName() {
						return null;
					}
				}
				""");

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
			"""
				import java.util.ArrayList;
				import java.util.Collections;
				import java.util.Comparator;
				import java.util.List;
				public class X {
					private void foo() {
						List<Person> people= new ArrayList<>();
						Collections.sort(people, Comparator.comparing((Person p) -> p.getLastName()));
					}
				}
				class Person{
					String getLastName() {
						return null;
					}
				}
				""");

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
			"""
				import java.util.ArrayList;
				import java.util.Collections;
				import java.util.Comparator;
				import java.util.List;
				public class X {
					private void foo() {
						Comparator.reverseOrder();
					}
				}
				""");

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
			"""
				import java.util.Comparator;
				public class ComparatorUse {
					Comparator<String> c =
							Comparator.comparing((String s)->s.toString())
							.thenComparing(s -> s.length());
				}
				""");

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
			"""
				import java.util.Comparator;
				public class ComparatorUse {
					Comparator<String> c =
							Comparator.comparing((String s)->s.toString())
							.thenComparing(s -> s.length());
				}
				""");

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
			"""
				@FunctionalInterface
				interface FI {
					int foo();
				}
				class C1 {
					void fun1(int x) {
						FI test= () -> {
							for (int k=0;k<1;) ;
							for (int k=0;k<1;) ;
							try {
							} catch (Exception ex) {
							}
							return 0;
						};
					}
				}
				""");

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
			"""
				interface Supplier<T> {
				    T get();
				}
				interface Runnable {
				    public abstract void run();
				}
				public class X {
					public static void main(String[] args) {
						execute(() -> {
							executeInner(() -> {
				                int xxx = 10;
							});
							return null;
						});
						System.out.println("done");
					}
					static <R> R execute(Supplier<R> supplier) {
						return null;
					}
					static void executeInner(Runnable callback) {
					}
				}
				""");

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
			"""
				interface Function<T, R> {
				    R apply(T t);
				}
				public class X {
					public static void main(String[] args) {
						Function<String, String> f1= (String s, Function this) -> s;
						Function<String, String> f2= (Function this, String s) -> s;
					}\s
				}
				"""
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
			"""
				import java.util.List;
				interface Getter<E> {
				    E get(List<E> list, int i);
				}
				public class X<U> {
					public void foo(List<U> l) {
						Getter<U> g= (x, i) -> x.get(i);
					}\s
				}
				"""
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
			"""
				import java.util.List;
				interface Getter<E> {
				    E get(List<E> list, int i);
				}
				public class X<U> {
					public void foo(List<U> l) {
						Getter<U> g= (x, i) -> x.get(i);
						Getter<U> g1= (x, i) -> x.get(i);
					}\s
				}
				"""
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
			"""
				import java.util.List;
				interface Getter<E> {
				    E get(List<E> list, int i);
				}
				public class X<U> {
					public void foo(List<U> l) {
						Getter<U> g= (x, i) -> x.get(i);
					}\s
				}
				"""
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
			"""
				interface I {
				    I doit(I xyz);
				}
				public class X {\s
					public static void main(String[] args) {
						I i = (pqr) -> {
							return (xyz) -> {
								return (abc) -> abc;\s
							};
						};
					}
				}
				""");
	String str = this.workingCopies[0].getSource();

	String selection = "abc)";
	int start = str.indexOf(selection);
	IJavaElement[] elements = this.workingCopies[0].codeSelect(start, 3);
	ILocalVariable local = (ILocalVariable) elements[0];
	String memento = local.getHandleIdentifier();
		assertEquals(
				"Incorrect memento string",
				"""
					=Resolve/src<{X.java[X~main~\\[QString;=)="LI;!103!169!110=&doit!1="LI;="pqr="LI;="LX\\~I;.doit\\(LI;)\
					LI;@pqr!104!106!104!106!LI;!0!true=&=)="LI;!124!164!131=&doit!1="LI;="xyz="LI;="LX\\~I;.doit\\(LI;)\
					LI;@xyz!125!127!125!127!LI;!0!true=&=)="LI;!146!157!153=&doit!1="LI;="abc="LI;="LX\\~I;.doit\\(LI;)\
					LI;@abc!147!149!147!149!LI;!0!true=&@abc!147!149!147!149!LI;!0!true""",
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
			"""
				=Resolve/src<{X.java[X~main~\\[QString;=)="LI;!103!169!110=&doit!1="LI;="pqr="LI;="LX\\~I;.doit\\(LI;)\
				LI;@pqr!104!106!104!106!LI;!0!true=&=)="LI;!124!164!131=&doit!1="LI;="xyz="LI;="LX\\~I;.doit\\(LI;)\
				LI;@xyz!125!127!125!127!LI;!0!true=&@xyz!125!127!125!127!LI;!0!true""",
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
			"""
				interface I {\
				  int foo(int x);\
				}\
				public class X {\
				  int bar(int x) {
				      return i;
				  }
				  public static void main(String[] args) {\
				    I i = (x) -> {\
				      return x;\
				    };\
				   i.foo(10);\
				   X x = new X();
				   I i2 = x::bar;
				   i2.foo(10);
				  }\
				}""");


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
			"""
				class Y {
					public void fooY() {return;}
					public void bar(I2 i) {return;}
					public void bar(I i) {return;}  \s
				}
				class fooY() {}
				interface I { void fooI(Y y); }
				interface I2 { void fooI2(int n);}
				public class X {
					void foo() {
						I i = Y::fooY; // works
					}
				}""");

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
			"""
				class Y {
					public void fooY() {return;}
					public void bar(I2 i) {return;}
					public void bar(I i) {return;}  \s
				}
				class fooY{}
				interface I { void fooI(Y y); }
				interface I2 { void fooI2(int n);}
				public class X {
					void foo() {
						Y y = new Y();
						y.bar(Y::fooY);
					}
				}""");
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
			"""
				@FunctionalInterface
				interface FI {
					default int getID() {
						return 11;
					}
					void print();
				}
				class T {
					FI f2 = () -> System.out.println(super.toString());
				}
				""");

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
			"""
				class Y {
					public void fooY() {return;}
					public void bar(I2 i) {return;}
					public void bar(I i) {return;}  \s
				}
				class fooY{}
				interface I { void fooI(Y y); }
				interface I2 { void fooI2(int n);}
				public class X {
					void foo() {
						Y y = new Y();
						y.bar(Y::fooY);
					}
				}""");
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
			"""
				import java.util.Comparator;
				import java.util.stream.Collectors;
				
				interface Something {
				      public int getSize();
				      public Instant getTime();
				}
				interface Instant extends Comparable<Instant> {
				}
				public class Example {
				   public void test2() {
				      java.util.stream.Collector<Something,?,java.util.Map<Integer,Something>> c =\s
				      Collectors.collectingAndThen(
				            Collectors.<Something>toList(),
				            list -> list.stream().collect(Collectors.groupingBy(Something::getSize,
				                     // Returns Collector<Something,?,Object> - INCORRECT!
				                     Collectors.collectingAndThen(
				                        Collectors.<Something>toList(),
				                        list2 -> list2.stream().sorted(Comparator.comparing(Something::getTime)).limit(1).findAny().orElse(null)
				                     )
				                  )));
				   }
				}
				""");
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
			"""
				import java.util.function.Function;
				
				public class Example {
				   static <T> T id(T t) { return t; }
				   static <T,X> T f1 (X x) { return null; }
				  \s
				   String test() {
					   return f3(y -> y.f2(Example::f1, id(y)));
				   }
				   <U,V> V f2(Function<U, V> f, U u) {return f.apply(null);}
				   <R> R f3(Function<Example,R> f) { return null; }
				}
				""");
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
			"""
				import java.util.function.Function;
				
				public class Snippet {
				    void m1() {
				    	MyObservable.range(1, 2).groupBy(integer -> {
					 		return "even";
						});
				    }
				
				}
				class MyObservable<T> {
					static MyObservable<Integer> range(int i1, int i2) {
						return new MyObservable<>();
					}
					<K> void groupBy(Function<T, K> func) {
					}
				}""");
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
			"""
				import java.io.File;
				import java.util.ArrayList;
				import java.util.Collection;
				import java.util.stream.Stream;
				
				public class Snippet {
					public static void main(String[] args) {
						Collection<File> rootDirectories = new ArrayList<>();
						Stream<Object> directories = rootDirectories.stream()
								.map(dir -> dir.listFiles(File::isDirectory));
						System.out.println(directories);
					}
				}""");
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
			"""
				import java.util.function.Predicate;
				
				public class Reproducer {
				
				    private final Predicate<Object> predicate =
				            input -> (input instanceof String withoutThisVariableNameThereIsNoError);
				}
				""");
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
			"""
				public class LambdaTest {
					public static void method(String value) {
						System.out.print("para");
					}
				
					public static void method(java.util.function.Supplier<String> supplier) {
						System.out.print(supplier.get());
					}
				
					public static void main(String[] args) {
						LambdaTest.method(LambdaTest.class::toString);
						System.out.print("extra");
					}
				}
				""");
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
			"""
				import java.lang.reflect.Constructor;
				import java.util.HashSet;
				import java.util.Optional;
				import java.util.Set;
				
				public class EclipseOpenDeclarationBug {
				  public EclipseOpenDeclarationBug(Class<?> cls) {
				    Set<Constructor<?>> constructors = new HashSet<>();
				
				    getPublicEmptyConstructor(cls).ifPresent(c -> {
				      if(constructors.isEmpty()) {
				        constructors.add(c);
				      }
				    });
				
				    if(constructors.size() < 1) {
				      throw new IllegalArgumentException("No suitable constructor found; provide an empty constructor or annotate one with @Inject: " + cls);
				    }
				  }
				
				  private static <T> Optional<Constructor<T>> getPublicEmptyConstructor(Class<T> cls) {
				    try {
				      return Optional.of(cls.getConstructor());
				    }
				    catch(NoSuchMethodException e) {
				      return Optional.empty();
				    }
				  }
				}
				""");
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
			"""
				import java.util.Optional;
				
				public class Test {
				
				  public void xyz() {
				    getOptionalValue().ifPresent(val -> {
				      int i = 1;
				      System.out.print(val);
				    });
				    try {
				    } catch (Exception e) {
				    }
				  }
				
				  public Optional<String> getOptionalValue() {
				    return Optional.empty();
				  }
				}
				""");
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
}
