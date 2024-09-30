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
 *     Stephan Herrmann - contribution for Bug 300576 - NPE Computing type hierarchy when compliance doesn't match libraries
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.io.IOException;
import junit.framework.Test;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class ResolveTests_1_5 extends AbstractJavaModelTests {
	ICompilationUnit wc = null;

static {
	// Names of tests to run: can be "testBugXXXX" or "BugXXXX")
	//TESTS_NAMES = new String[] { "test0095" };
	// Numbers of tests to run: "test<number>" will be run for each number of this array
	//TESTS_NUMBERS = new int[] { 124 };
	// Range numbers of tests to run: all tests between "test<first>" and "test<last>" will be run for { first, last }
	//TESTS_RANGE = new int[] { 16, -1 };
}
public static Test suite() {
	return buildModelTestSuite(ResolveTests_1_5.class, BYTECODE_DECLARATION_ORDER);
}
public ResolveTests_1_5(String name) {
	super(name);
}
@Override
public ICompilationUnit getWorkingCopy(String path, String source) throws JavaModelException {
	return super.getWorkingCopy(path, source, this.wcOwner);
}
private IJavaElement[] select(String path, String source, String selection) throws JavaModelException {
	this.wc = getWorkingCopy(path, source);
	String str = this.wc.getSource();
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	return this.wc.codeSelect(start, length, this.wcOwner);
}
private IJavaElement[] selectAfter(String path, String source, String selection) throws JavaModelException {
	this.wc = getWorkingCopy(path, source);
	String str = this.wc.getSource();
	int start = str.lastIndexOf(selection) + selection.length();
	return this.wc.codeSelect(start, 0, this.wcOwner);
}
@Override
public void setUpSuite() throws Exception {
	super.setUpSuite();

	setUpJavaProject("Resolve", CompilerOptions.getFirstSupportedJavaVersion());

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
public void test0001() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src2", "test0001", "Test.java");

	String str = cu.getSource();
	String selection = "iii";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = cu.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"iii [in foo(Iterable) [in Test [in Test.java [in test0001 [in src2 [in Resolve]]]]]]",
		elements
	);
}
public void test0002() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src2", "test0002", "Test.java");

	String str = cu.getSource();
	String selection = "Y";
	int start = str.indexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = cu.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Y [in X [in Test [in Test.java [in test0002 [in src2 [in Resolve]]]]]]",
		elements
	);
}
public void test0003() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src2", "test0003", "Test.java");

	String str = cu.getSource();
	String selection = "X";
	int start = str.indexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = cu.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"X [in Test [in Test.java [in test0003 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0004() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0004/Test.java",
			"package test0004;\n" +
			"public class Test <T> {\n" +
			"	test0004.Test.X<Object>.Y<Object> var;\n" +
			"	public class X <TX> {\n" +
			"		public class Y <TY> {\n" +
			"		}\n" +
			"	}\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "Test";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Test [in [Working copy] Test.java [in test0004 [in src2 [in Resolve]]]]",
		elements
	);
}
public void test0005() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src2", "test0005", "Test.java");

	String str = cu.getSource();
	String selection = "test0005";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = cu.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"test0005 [in src2 [in Resolve]]",
		elements
	);
}
public void test0006() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src2", "test0006", "Test.java");

	String str = cu.getSource();
	String selection = "Test0006";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = cu.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"<Test0006> [in Test [in Test.java [in test0006 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0007() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src2", "test0007", "Test.java");

	String str = cu.getSource();
	String selection = "Test0007";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = cu.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"<Test0007> [in Test [in Test.java [in test0007 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0008() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src2", "test0008", "Test.java");

	String str = cu.getSource();
	String selection = "Test0008";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = cu.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"<Test0008> [in Inner [in Test [in Test.java [in test0008 [in src2 [in Resolve]]]]]]",
		elements
	);
}
public void test0009() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src2", "test0009", "Test.java");

	String str = cu.getSource();
	String selection = "Test0009";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = cu.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"<Test0009> [in Inner [in Test [in Test.java [in test0009 [in src2 [in Resolve]]]]]]",
		elements
	);
}
public void test0010() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src2", "test0010", "Test.java");

	String str = cu.getSource();
	String selection = "Test0010";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = cu.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"<Test0010> [in Test [in Test.java [in test0010 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0011() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src2", "test0011", "Test.java");

	String str = cu.getSource();
	String selection = "Test0011";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = cu.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"<Test0011> [in foo() [in Test [in Test.java [in test0011 [in src2 [in Resolve]]]]]]",
		elements
	);
}
public void test0012() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src2", "test0012", "Test.java");

	String str = cu.getSource();
	String selection = "Test0012";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = cu.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"<Test0012> [in foo() [in Test [in Test.java [in test0012 [in src2 [in Resolve]]]]]]",
		elements
	);
}
public void test0013() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src2", "test0013", "Test.java");

	String str = cu.getSource();
	String selection = "Test0013";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = cu.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"<Test0013> [in foo() [in Inner [in Test [in Test.java [in test0013 [in src2 [in Resolve]]]]]]]",
		elements
	);
}
public void test0014() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src2", "test0014", "Test.java");

	String str = cu.getSource();
	String selection = "Test0014";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = cu.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"<Test0014> [in foo() [in Inner [in Test [in Test.java [in test0014 [in src2 [in Resolve]]]]]]]",
		elements
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=71852
 */
public void test0015() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src2", "test0015", "Test.java");

	String str = cu.getSource();
	String selection = "var";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = cu.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"var [in foo() [in Test [in Test.java [in test0015 [in src2 [in Resolve]]]]]]",
		elements
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=72105
 */
public void test0016() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src2", "test0016", "Test.java");

	String str = cu.getSource();
	String selection = "T";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = cu.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"<T> [in foo() [in Test [in Test.java [in test0016 [in src2 [in Resolve]]]]]]",
		elements
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=72105
 */
public void test0017() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src2", "test0017", "Test.java");

	String str = cu.getSource();
	String selection = "T";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = cu.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"<T> [in foo() [in Test [in Test.java [in test0017 [in src2 [in Resolve]]]]]]",
		elements
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=72105
 */
public void test0018() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src2", "test0018", "Test.java");

	String str = cu.getSource();
	String selection = "T";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = cu.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"<T> [in foo(T) [in Test [in Test.java [in test0018 [in src2 [in Resolve]]]]]]",
		elements
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=72105
 */
public void test0019() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src2", "test0019", "Test.java");

	String str = cu.getSource();
	String selection = "T";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = cu.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"<T> [in foo(Object, T, Object) [in Test [in Test.java [in test0019 [in src2 [in Resolve]]]]]]",
		elements
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=72105
 */
public void test0020() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src2", "test0020", "Test.java");

	String str = cu.getSource();
	String selection = "T";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = cu.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"<T> [in foo(X<T>) [in Test [in Test.java [in test0020 [in src2 [in Resolve]]]]]]",
		elements
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=72105
 */
public void test0021() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src2", "test0021", "Test.java");

	String str = cu.getSource();
	String selection = "T";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = cu.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"<T> [in foo() [in Test [in Test.java [in test0021 [in src2 [in Resolve]]]]]]",
		elements
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=74286
 */
public void test0022() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src2", "test0022", "Test.java");

	String str = cu.getSource();
	String selection = "add";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = cu.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"add(T, A<T>, A<T>.B, A<T>.C<T>, A<T>.B.D<T>) [in X [in X.java [in test0022 [in src2 [in Resolve]]]]]",
		elements
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=74286
 */
public void test0023() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src2", "test0023", "Test.java");

	String str = cu.getSource();
	String selection = "add";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = cu.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"add(T, test0023.A<T>, test0023.A<T>.B, test0023.A<T>.C<T>, test0023.A<T>.B.D<T>, test0023.E, test0023.E.F<T>) [in X [in X.class [in test0023 [in test0023.jar [in Resolve]]]]]",
		elements
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=77184
 */
public void test0024() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src2", "test0024", "Test.java");

	String str = cu.getSource();
	String selection = "Test";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = cu.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Test [in Test.java [in test0024 [in src2 [in Resolve]]]]",
		elements
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=77184
 */
public void test0025() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src2", "test0025", "Test.java");

	String str = cu.getSource();
	String selection = "Test";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = cu.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Test [in Test.java [in test0025 [in src2 [in Resolve]]]]",
		elements
	);
}
public void test0026() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0026/Test.java",
			"package test0026;\n" +
			"public class Test<T> {\n" +
			"	public class Inner<U> {\n" +
			"	}\n" +
			"	Test.Inner x;\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "Inn";
	int start = str.lastIndexOf(selection);

	IJavaElement[] elements = this.wc.codeSelect(start, 0);
	assertElementsEqual(
		"Unexpected elements",
		"Inner [in Test [in [Working copy] Test.java [in test0026 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0027() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0027/Test.java",
			"package test0027;\n" +
			"public class Test<T> {\n" +
			"	public class Inner<U> {\n" +
			"	}\n" +
			"	Test.Inner<Object> x;\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "Inn";
	int start = str.lastIndexOf(selection);

	IJavaElement[] elements = this.wc.codeSelect(start, 0);
	assertElementsEqual(
		"Unexpected elements",
		"Inner [in Test [in [Working copy] Test.java [in test0027 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0028() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0028/Test.java",
			"package test0028;\n" +
			"public class Test<T> {\n" +
			"	public class Inner<U> {\n" +
			"	}\n" +
			"	Test<Object>.Inner x;\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "Inn";
	int start = str.lastIndexOf(selection);

	IJavaElement[] elements = this.wc.codeSelect(start, 0);
	assertElementsEqual(
		"Unexpected elements",
		"Inner [in Test [in [Working copy] Test.java [in test0028 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0029() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0029/Test.java",
			"package test0029;\n" +
			"public class Test<T> {\n" +
			"	public class Inner<U> {\n" +
			"	}\n" +
			"	Test<Object>.Inner<Object> x;\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "Inn";
	int start = str.lastIndexOf(selection);

	IJavaElement[] elements = this.wc.codeSelect(start, 0);
	assertElementsEqual(
		"Unexpected elements",
		"Inner [in Test [in [Working copy] Test.java [in test0029 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0030() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0030/Test.java",
			"package test0030;\n" +
			"public class Test<T> {\n" +
			"	public class Inner<U> {\n" +
			"	}\n" +
			"	Test.Inner x;\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "Inner";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Inner [in Test [in [Working copy] Test.java [in test0030 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0031() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0031/Test.java",
			"package test0031;\n" +
			"public class Test<T> {\n" +
			"	public class Inner<U> {\n" +
			"	}\n" +
			"	Test.Inner<Object> x;\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "Inner";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Inner [in Test [in [Working copy] Test.java [in test0031 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0032() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0032/Test.java",
			"package test0032;\n" +
			"public class Test<T> {\n" +
			"	public class Inner<U> {	\n" +
			"	}\n" +
			"	Test<Object>.Inner x;\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "Inner";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Inner [in Test [in [Working copy] Test.java [in test0032 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0033() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0033/Test.java",
			"package test0033;\n" +
			"public class Test<T> {\n" +
			"	public class Inner<U> {\n" +
			"	}\n" +
			"	Test<Object>.Inner<Object> x;\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "Inner";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Inner [in Test [in [Working copy] Test.java [in test0033 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0034() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0034/Test.java",
			"package test0034;\n" +
			"public class Test<T> {\n" +
			"	public class Inner<U> {\n" +
			"	}\n" +
			"	Test.Inner x;\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "Test.Inner";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Inner [in Test [in [Working copy] Test.java [in test0034 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0035() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0035/Test.java",
			"package test0035;\n" +
			"public class Test<T> {\n" +
			"	public class Inner<U> {	\n" +
			"	}\n" +
			"	Test.Inner<Object> x;\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "Test.Inner<Object>";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Inner [in Test [in [Working copy] Test.java [in test0035 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0036() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0036/Test.java",
			"package test0036;\n" +
			"public class Test<T> {\n" +
			"	public class Inner<U> {	\n" +
			"	}\n" +
			"	Test<Object>.Inner x;\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "Test<Object>.Inner";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Inner [in Test [in [Working copy] Test.java [in test0036 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0037() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0037/Test.java",
			"package test0037;\n" +
			"public class Test<T> {\n" +
			"	public class Inner<U> {	\n" +
			"	}\n" +
			"	Test<Object>.Inner<Object> x;\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "Test<Object>.Inner<Object>";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Inner [in Test [in [Working copy] Test.java [in test0037 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0038() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0038/Test.java",
			"package test0038;\n" +
			"public class Test<T> {\n" +
			"	public class Inner<U> {\n" +
			"	}\n" +
			"	Test.Inner<Object> x;\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "Test.Inner";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Inner [in Test [in [Working copy] Test.java [in test0038 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0039() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0039/Test.java",
			"package test0039;\n" +
			"public class Test<T> {\n" +
			"	public class Inner<U> {\n" +
			"	}\n" +
			"	Test<Object>.Inner<Object> x;\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "Test<Object>.Inner";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Inner [in Test [in [Working copy] Test.java [in test0039 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0040() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0040/Test.java",
			"package test0040;\n" +
			"public class Test<T> {\n" +
			"	public class Inner<U> {\n" +
			"	}\n" +
			"	Test<Object>.Inner<Object> x;\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "Inner<Object>";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Inner [in Test [in [Working copy] Test.java [in test0040 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0041() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0041/Test.java",
			"package test0041;\n" +
			"public class Test<T> {\n" +
			"	void foo() {\n" +
			"		class Local1<T1> {\n" +
			"			class Local2<T2> {\n" +
			"			}\n" +
			"		}\n" +
			"		class Local3<T3> {\n" +
			"		} \n" +
			"		Local1<Local3<Object>>.Local2<Local3<Object>> l;\n" +
			"	}\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "Local1<Local3<Object>>.Local2<Local3<Object>>";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Local2 [in Local1 [in foo() [in Test [in [Working copy] Test.java [in test0041 [in src2 [in Resolve]]]]]]]",
		elements
	);
}
public void test0042() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0042/Test.java",
			"package test0042;\n" +
			"public class Test<T> {\n" +
			"	public class Inner<U> {	\n" +
			"	}\n" +
			"	Test<? super String>.Inner<? extends String> v;\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "Test<? super String>.Inner<? extends String>";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Inner [in Test [in [Working copy] Test.java [in test0042 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0043() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0043/Test.java",
			"package test0043;\n" +
			"public class Test<T> {\n" +
			"	Test<T> var;\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "Test";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Test [in [Working copy] Test.java [in test0043 [in src2 [in Resolve]]]]",
		elements
	);
}
public void test0044() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0044/Test.java",
			"package test0044;\n" +
			"public class Test<T1> {\n" +
			"}\n" +
			"class Test2<T2> {\n" +
			"	Test<T2> var;\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "Test";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Test [in [Working copy] Test.java [in test0044 [in src2 [in Resolve]]]]",
		elements
	);
}
public void test0045() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0045/Test.java",
			"package test0045;\n" +
			"public class Test<T1> {\n" +
			"	String var;\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "var";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"var [in Test [in [Working copy] Test.java [in test0045 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0046() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0046/Test.java",
			"package test0046;\n" +
			"public class Test<T1> {\n" +
			"	String var;\n" +
			"	void foo() {\n" +
			"	  var = null;\n" +
			"	}\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "var";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"var [in Test [in [Working copy] Test.java [in test0046 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0047() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0047/Test.java",
			"package test0047;\n" +
			"public class Test<T1> {\n" +
			"	public String var;\n" +
			"	void foo() {\n" +
			"	  Test<String> t = null;\n" +
			"	  t.var = null;\n" +
			"	}\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "var";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"var [in Test [in [Working copy] Test.java [in test0047 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0048() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0048/Test.java",
			"package test0048;\n" +
			"public class Test<T1> {\n" +
			"	public String var;\n" +
			"	void foo() {\n" +
			"	  Test<?> t = new Test<String>;\n" +
			"	  t.var = null;\n" +
			"	}\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "var";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"var [in Test [in [Working copy] Test.java [in test0048 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0049() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0049/Test.java",
			"package test0049;\n" +
			"public class Test<T1> {\n" +
			"	public String var;\n" +
			"	void foo() {\n" +
			"	  Test<T1> t = null;\n" +
			"	  t.var = null;\n" +
			"	}\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "var";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"var [in Test [in [Working copy] Test.java [in test0049 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0050() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0050/Test.java",
			"package test0050;\n" +
			"public class Test<T1> {\n" +
			"	public String var;\n" +
			"	void foo() {\n" +
			"	  Test t = null;\n" +
			"	  t.var = null;\n" +
			"	}\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "var";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"var [in Test [in [Working copy] Test.java [in test0050 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0051() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0051/Test.java",
			"package test0051;\n" +
			"public class Test {\n" +
			"	void foo() {\n" +
			"	  class Inner<T> {\n" +
			"	    public String var;\n" +
			"	  }" +
			"	  Inner<Object> i = null;\n" +
			"	  i.var = null;\n" +
			"	}\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "var";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"var [in Inner [in foo() [in Test [in [Working copy] Test.java [in test0051 [in src2 [in Resolve]]]]]]]",
		elements
	);
}
public void test0052() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0052/Test.java",
			"package test0052;\n" +
			"public class Test {\n" +
			"	void foo() {\n" +
			"	  class Inner<T> {\n" +
			"	    public T var;\n" +
			"	  }" +
			"	  Inner<Object> i = null;\n" +
			"	  i.var = null;\n" +
			"	}\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "var";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"var [in Inner [in foo() [in Test [in [Working copy] Test.java [in test0052 [in src2 [in Resolve]]]]]]]",
		elements
	);
}
public void test0053() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0053/Test.java",
			"package test0053;\n" +
			"public class Test<T> {\n" +
			"	public void foo() {\n" +
			"   }\n" +
			"}\n" +
			"class Test2<T> {\n" +
			"  void bar() {\n" +
			"    Test<String> var = null;\n" +
			"    var.foo();\n" +
			"  }\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "foo";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"foo() [in Test [in [Working copy] Test.java [in test0053 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0054() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0054/Test.java",
			"package test0054;\n" +
			"public class Test<T> {\n" +
			"	public void foo() {\n" +
			"   }\n" +
			"}\n" +
			"class Test2<T> {\n" +
			"  void bar() {\n" +
			"    Test var = null;\n" +
			"    var.foo();\n" +
			"  }\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "foo";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"foo() [in Test [in [Working copy] Test.java [in test0054 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0055() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0055/Test.java",
			"package test0055;\n" +
			"public class Test<T> {\n" +
			"	public void foo() {\n" +
			"   }\n" +
			"}\n" +
			"class Test2<T> {\n" +
			"  void bar() {\n" +
			"    Test<T> var = null;\n" +
			"    var.foo();\n" +
			"  }\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "foo";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"foo() [in Test [in [Working copy] Test.java [in test0055 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0056() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0056/Test.java",
			"package test0056;\n" +
			"public class Test<T> {\n" +
			"  public void foo() {\n" +
			"  }\n" +
			"  void bar() {\n" +
			"    Test<T> var = null;\n" +
			"    var.foo();\n" +
			"  }\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "foo";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"foo() [in Test [in [Working copy] Test.java [in test0056 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0057() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0057/Test.java",
			"package test0057;\n" +
			"public class Test<T1> {\n" +
			"  public <T2> void foo() {\n" +
			"  }\n" +
			"}\n" +
			"class Test2 {\n" +
			"  void bar() {\n" +
			"    Test<String> var = null;\n" +
			"    var.<Object>foo();\n" +
			"  }\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "foo";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"foo() [in Test [in [Working copy] Test.java [in test0057 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0058() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0058/Test.java",
			"package test0058;\n" +
			"public class Test<T1> {\n" +
			"  public <T2> void foo() {\n" +
			"  }\n" +
			"}\n" +
			"class Test2 {\n" +
			"  void bar() {\n" +
			"    Test<String> var = null;\n" +
			"    var.foo();\n" +
			"  }\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "foo";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"foo() [in Test [in [Working copy] Test.java [in test0058 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0059() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0059/Test.java",
			"package test0059;\n" +
			"public class Test {\n" +
			"  public <T2> void foo() {\n" +
			"  }\n" +
			"}\n" +
			"class Test2 {\n" +
			"  void bar() {\n" +
			"    Test var = null;\n" +
			"    var.<String>foo();\n" +
			"  }\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "foo";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"foo() [in Test [in [Working copy] Test.java [in test0059 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0060() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0060/Test.java",
			"package test0060;\n" +
			"public class Test {\n" +
			"  public <T2> void foo() {\n" +
			"  }\n" +
			"}\n" +
			"class Test2 {\n" +
			"  void bar() {\n" +
			"    Test var = null;\n" +
			"    var.foo();\n" +
			"  }\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "foo";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"foo() [in Test [in [Working copy] Test.java [in test0060 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0061() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0061/Test.java",
			"package test0061;\n" +
			"public class Test {\n" +
			"  public <T2> void foo() {\n" +
			"    Test var;\n" +
			"    var.<T2>foo();\n" +
			"  }\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "foo";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"foo() [in Test [in [Working copy] Test.java [in test0061 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0062() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0062/Test.java",
			"package test0062;\n" +
			"public class Test<T1> {\n" +
			"  public <T2> void foo() {\n" +
			"    Test var;\n" +
			"    var.<T1>foo();\n" +
			"  }\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "foo";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"foo() [in Test [in [Working copy] Test.java [in test0062 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0063() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0063/Test.java",
			"package test0063;\n" +
			"public class Test<T1> {\n" +
			"  public void foo() {\n" +
			"  }\n" +
			"}\n" +
			"class Test2 {\n" +
			"  void bar() {\n" +
			"    Test<String> var;\n" +
			"    var.foo();\n" +
			"  }\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "foo";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"foo() [in Test [in [Working copy] Test.java [in test0063 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0064() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0064/Test.java",
			"package test0064;\n" +
			"public class Test {\n" +
			"  public <U> Test(U u) {\n" +
			"  }\n" +
			"  void bar() {\n" +
			"    new <String>Test(null);\n" +
			"  }\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "Test";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Test(U) [in Test [in [Working copy] Test.java [in test0064 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0065() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0065/Test.java",
			"package test0065;\n" +
			"public class Test {\n" +
			"  public <U> Test(U u) {\n" +
			"  }\n" +
			"  void bar() {\n" +
			"    new Test(null);\n" +
			"  }\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "Test";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Test(U) [in Test [in [Working copy] Test.java [in test0065 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0066() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0066/Test.java",
			"package test0066;\n" +
			"public class Test<T> {\n" +
			"  public <U> Test(U u) {\n" +
			"  }\n" +
			"  void bar() {\n" +
			"    new <String>Test<String>(null);\n" +
			"  }\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "Test";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Test(U) [in Test [in [Working copy] Test.java [in test0066 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0067() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0067/Test.java",
			"package test0067;\n" +
			"public class Test<T> {\n" +
			"  public <U> Test(U u) {\n" +
			"  }\n" +
			"  void bar() {\n" +
			"    new Test<String>(null);\n" +
			"  }\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "Test";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Test(U) [in Test [in [Working copy] Test.java [in test0067 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0068() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0068/Test.java",
			"package test0068;\n" +
			"public class Test<T> {\n" +
			"  public <U> Test(U u) {\n" +
			"  }\n" +
			"  void bar() {\n" +
			"    new Test(null);\n" +
			"  }\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "Test";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Test(U) [in Test [in [Working copy] Test.java [in test0068 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0069() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0069/Test.java",
			"package test0069;\n" +
			"public class Test<T> {\n" +
			"  public <U> Test(U u) {\n" +
			"  }\n" +
			"  public class Inner<V> {\n" +
			"    public <W> Inner(W w) {\n" +
			"    }\n" +
			"  }\n" +
			"  void bar() {\n" +
			"    new <String>Test<String>(null).new <String>Inner<String>(null);\n" +
			"  }\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "Inner";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Inner(W) [in Inner [in Test [in [Working copy] Test.java [in test0069 [in src2 [in Resolve]]]]]]",
		elements
	);
}
public void test0070() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0070/Test.java",
			"package test0070;\n" +
			"public class Test {\n" +
			"  public <U> Test(U u) {\n" +
			"  }\n" +
			"  void bar() {\n" +
			"    new <String>Test(null){};\n" +
			"  }\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "Test";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Test(U) [in Test [in [Working copy] Test.java [in test0070 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0071() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0071/Test.java",
			"package test0071;\n" +
			"public class Test {\n" +
			"  public <U> Test(U u) {\n" +
			"  }\n" +
			"  void bar() {\n" +
			"    new Test(null){};\n" +
			"  }\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "Test";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Test(U) [in Test [in [Working copy] Test.java [in test0071 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0072() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0072/Test.java",
			"package test0072;\n" +
			"public class Test<T> {\n" +
			"  public <U> Test(U u) {\n" +
			"  }\n" +
			"  void bar() {\n" +
			"    new <String>Test<String>(null){};\n" +
			"  }\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "Test";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Test(U) [in Test [in [Working copy] Test.java [in test0072 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0073() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0073/Test.java",
			"package test0073;\n" +
			"public class Test<T> {\n" +
			"  public <U> Test(U u) {\n" +
			"  }\n" +
			"  void bar() {\n" +
			"    new Test<String>(null){};\n" +
			"  }\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "Test";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Test(U) [in Test [in [Working copy] Test.java [in test0073 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0074() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0074/Test.java",
			"package test0074;\n" +
			"public class Test<T> {\n" +
			"  public <U> Test(U u) {\n" +
			"  }\n" +
			"  void bar() {\n" +
			"    new Test(null){};\n" +
			"  }\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "Test";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Test(U) [in Test [in [Working copy] Test.java [in test0074 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0075() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0075/Test.java",
			"package test0075;\n" +
			"public class Test<T> {\n" +
			"  public <U> Test(U u) {\n" +
			"  }\n" +
			"  public class Inner<V> {\n" +
			"    public <W> Inner(W w) {\n" +
			"    }\n" +
			"  }\n" +
			"  void bar() {\n" +
			"    new <String>Test<String>(null).new <String>Inner<String>(null){};\n" +
			"  }\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "Inner";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Inner(W) [in Inner [in Test [in [Working copy] Test.java [in test0075 [in src2 [in Resolve]]]]]]",
		elements
	);
}
public void test0076() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0076/Test.java",
			"package test0076;\n" +
			"public class Test<T> {\n" +
			"  public class Inner<U, V> {\n" +
			"  }\n" +
			"  Test<? super String>.Inner<int[][], Test<String[]>> var;\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "Inner";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Inner [in Test [in [Working copy] Test.java [in test0076 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0077() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0077/Test.java",
			"package test0077;\n" +
			"@interface MyAnn {\n" +
			"}\n" +
			"public @MyAnn class Test {\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "MyAnn";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"MyAnn [in [Working copy] Test.java [in test0077 [in src2 [in Resolve]]]]",
		elements
	);
}
public void test0078() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0078/Test.java",
			"package test0078;\n" +
			"@interface MyAnn {\n" +
			"  String value();\n" +
			"}\n" +
			"public @MyAnn(\"\") class Test {\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "MyAnn";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"MyAnn [in [Working copy] Test.java [in test0078 [in src2 [in Resolve]]]]",
		elements
	);
}
public void test0079() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0079/Test.java",
			"package test0079;\n" +
			"@interface MyAnn {\n" +
			"  String value();\n" +
			"}\n" +
			"public @MyAnn class Test {\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "MyAnn";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"MyAnn [in [Working copy] Test.java [in test0079 [in src2 [in Resolve]]]]",
		elements
	);
}
public void test0080() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0080/Test.java",
			"package test0080;\n" +
			"@interface MyAnn {\n" +
			"  String value1();\n" +
			"  String value2();\n" +
			"}\n" +
			"public @MyAnn(value1 = \"\", value2 = \"\") class Test {\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "MyAnn";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"MyAnn [in [Working copy] Test.java [in test0080 [in src2 [in Resolve]]]]",
		elements
	);
}
public void test0081() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0081/Test.java",
			"package test0080;\n" +
			"@interface MyAnn {\n" +
			"  String value1();\n" +
			"  String value2();\n" +
			"}\n" +
			"public @MyAnn(value1 = \"\", value2 = \"\") class Test {\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "value1";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"value1() [in MyAnn [in [Working copy] Test.java [in test0081 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0082() throws JavaModelException {
	IJavaElement[] elements = select(
			"/Resolve/src2/test0082/Test.java",
			"package test0082;\n" +
			"public class Test<T> {\n" +
			"}",
			"Test");
	assertEquals("test0082.Test<T>", ((IType)elements[0]).getFullyQualifiedParameterizedName());
}
public void test0083() throws JavaModelException {
	IJavaElement[] elements = select(
			"/Resolve/src2/test0083/Test.java",
			"package test0083;\n" +
			"public class Test<T> {\n" +
			"  Test<String> field;\n" +
			"}",
			"Test");
	assertEquals("test0083.Test<java.lang.String>", ((IType)elements[0]).getFullyQualifiedParameterizedName());
}
public void test0084() throws JavaModelException {
	IJavaElement[] elements = select(
			"/Resolve/src2/test0084/Test.java",
			"package test0084;\n" +
			"public class Test<T> {\n" +
			"  Test field;\n" +
			"}",
			"Test");
	assertEquals("test0084.Test", ((IType)elements[0]).getFullyQualifiedParameterizedName());
}
public void test0085() throws JavaModelException {
	IJavaElement[] elements = select(
			"/Resolve/src2/test0085/Test.java",
			"package test0085;\n" +
			"public class Test<T> {\n" +
			"  class Member {\n" +
			"  }\n" +
			"}",
			"Member");
	assertEquals("test0085.Test<T>.Member", ((IType)elements[0]).getFullyQualifiedParameterizedName());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=80957
public void test0086() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Resolve/src2/test0086/Test.java",
			"package test0080;\n" +
			"public class Test {\n" +
			"   List<Integer> list;\n" +
			"}");

	String str = this.wc.getSource();
	String selection = "List";
	int start = str.lastIndexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"",
		elements
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82137
public void test0087() throws JavaModelException {
	ICompilationUnit imported = null;
	try {
		imported = getWorkingCopy(
				"/Resolve/src2/p/MyClass0087.java",
				"package p;\n" +
				"public class MyClass0087 {\n" +
				"   public static int bar = 0;\n" +
				"}");

		IJavaElement[] elements = select(
				"/Resolve/src2/test0087/Test.java",
				"import static p.MyClass0087.bar;\n" +
				"package test0087;\n" +
				"public class Test {\n" +
				"}",
				"bar");

		assertElementsEqual(
			"Unexpected elements",
			"bar [in MyClass0087 [in [Working copy] MyClass0087.java [in p [in src2 [in Resolve]]]]]",
			elements
		);
	} finally {
		if(imported != null) {
			imported.discardWorkingCopy();
		}
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82137
public void test0088() throws JavaModelException {
	ICompilationUnit imported = null;
	try {
		imported = getWorkingCopy(
				"/Resolve/src2/p/MyClass0088.java",
				"package p;\n" +
				"public class MyClass0088 {\n" +
				"   public static void foo() {}\n" +
				"   public static void foo(int i) {}\n" +
				"}");

		IJavaElement[] elements = select(
				"/Resolve/src2/test0088/Test.java",
				"import static p.MyClass0088.foo;\n" +
				"package test0088;\n" +
				"public class Test {\n" +
				"}",
				"foo");

		assertElementsEqual(
			"Unexpected elements",
			"foo(int) [in MyClass0088 [in [Working copy] MyClass0088.java [in p [in src2 [in Resolve]]]]]\n" +
			"foo() [in MyClass0088 [in [Working copy] MyClass0088.java [in p [in src2 [in Resolve]]]]]",
			elements
		);
	} finally {
		if(imported != null) {
			imported.discardWorkingCopy();
		}
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82558
public void test0089() throws JavaModelException {
	IJavaElement[] elements = selectAfter(
			"/Resolve/src2/test0089/Test.java",
			"package test0089;\n" +
			"public class Test<T> {\n" +
			"  Test(String t) {}\n" +
			"  Test(Test<String> ts) {}\n" +
			"  void bar() {\n" +
			"    new Test<String>(new Test<String>(\"\"));\n" +
			"  }\n" +
			"}",
			"  new Te");

	assertElementsEqual(
		"Unexpected elements",
		"Test(Test<String>) [in Test [in [Working copy] Test.java [in test0089 [in src2 [in Resolve]]]]]",
		elements
	);

}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=83489
public void test0090() throws JavaModelException {
	IJavaElement[] elements = selectAfter(
			"/Resolve/src2/test0090/Test.java",
			"package test0090;\n" +
			"public class Test {\n" +
			"  <T>Test(T t) {}\n" +
			"}",
			"T");

	assertElementsEqual(
		"Unexpected elements",
		"<T> [in Test(T) [in Test [in [Working copy] Test.java [in test0090 [in src2 [in Resolve]]]]]]",
		elements
	);

}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=86971
public void test0091() throws JavaModelException {
	ICompilationUnit imported = null;
	try {
		imported = getWorkingCopy(
				"/Resolve/src2/test0091/MyAnnot.java",
				"package test0091;\n" +
				"public @interface MyAnnot {\n" +
				"}");

		IJavaElement[] elements = select(
				"/Resolve/src2/test0091/Test.java",
				"package test0091;\n" +
				"@MyAnnot\n" +
				"public class Test {\n" +
				"}",
				"@MyAnnot");

		assertElementsEqual(
			"Unexpected elements",
			"MyAnnot [in [Working copy] MyAnnot.java [in test0091 [in src2 [in Resolve]]]]",
			elements
		);
	} finally {
		if(imported != null) {
			imported.discardWorkingCopy();
		}
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=86971
public void test0092() throws JavaModelException {
	ICompilationUnit imported = null;
	try {
		imported = getWorkingCopy(
				"/Resolve/src2/test0092/MyAnnot.java",
				"package test0092;\n" +
				"public @interface MyAnnot {\n" +
				"}");

		IJavaElement[] elements = select(
				"/Resolve/src2/test0092/Test.java",
				"package test0092;\n" +
				"@MyAnnot @MyAnnot\n" +
				"public class Test {\n" +
				"}",
				"MyAnnot @MyAnnot");

		assertElementsEqual(
			"Unexpected elements",
			"",
			elements
		);
	} finally {
		if(imported != null) {
			imported.discardWorkingCopy();
		}
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=85379
public void test0093() throws JavaModelException {
	ICompilationUnit imported = null;
	try {
		imported = getWorkingCopy(
				"/Resolve/src2/test0093/MyEnum.java",
				"package test0093;\n" +
				"public enum MyEnum {\n" +
				"  MyEnumConstant;\n" +
				"}");

		IJavaElement[] elements = select(
				"/Resolve/src2/test0093/Test.java",
				"package test0093;\n" +
				"public class Test {\n" +
				"  void foo(MyEnum e) {\n" +
				"    switch(e) {\n" +
				"      case MyEnumConstant:\n" +
				"        break;\n" +
				"    }\n" +
				"  }\n" +
				"}\n",
				"MyEnumConstant");

		assertElementsEqual(
			"Unexpected elements",
			"MyEnumConstant [in MyEnum [in [Working copy] MyEnum.java [in test0093 [in src2 [in Resolve]]]]]",
			elements
		);
	} finally {
		if(imported != null) {
			imported.discardWorkingCopy();
		}
	}
}
public void test0094() throws JavaModelException {
	IJavaElement[] elements = select(
			"/Resolve/src2/test0094/Test.java",
			"package test0094;\n" +
			"package import;\n" +
			"public class Test {\n" +
			"  public void goo(ZZArrayList<String> a) {\n" +
			"    a.get(0);\n" +
			"  }\n" +
			"}\n",
			"get");

	assertElementsEqual(
		"Unexpected elements",
		"get(int) {key=Ltest0094/ZZArrayList<Ljava/lang/String;>;.get(I)Ljava/lang/String;} [in ZZArrayList [in ZZArrayList.class [in test0094 [in class-folder [in Resolve]]]]]",
		elements,
		true/*show key*/
	);
}
/*
 * Regression test for bug 87929 Wrong decoding of type signature with wildcards
 */
public void test0095() throws JavaModelException {
	IJavaElement[] elements = select(
			"/Resolve/src2/test0095/X.java",
			"package test0095;\n" +
			"public class X {\n" +
			"	Y<?, ? extends Z<? super Exception>> y;\n" +
			"}\n" +
			"class Y<K, V> {\n" +
			"}\n" +
			"class Z<T> {\n" +
			"}",
			"Y<?, ? extends Z<? super Exception>>");

	assertElementsEqual(
		"Unexpected elements",
		"Y {key=Ltest0095/X~Y<Ltest0095/X~Y;{0}*Ltest0095/X~Y;{1}+Ltest0095/X~Z<Ltest0095/X~Z;{0}-Ljava/lang/Exception;>;>;} [in [Working copy] X.java [in test0095 [in src2 [in Resolve]]]]",
		elements,
		true/*show key*/
	);

	String key = ((IType) elements[0]).getKey();
	String signature = new BindingKey(key).toSignature();
	String[] typeArguments = Signature.getTypeArguments(signature);
	assertStringsEqual(
		"Unexpected type arguments",
		"*\n" +
		"+Ltest0095.Z<-Ljava.lang.Exception;>;\n",
		typeArguments);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=94653
public void test0096() throws JavaModelException {
	IJavaElement[] elements = select(
			"/Resolve/src2/test0096/X.java",
			"package test0095;\n" +
			"public class X<T> {\n" +
			"  class Inner<U> {\n" +
			"  }\n" +
			"  X<String>.Inner var;\n" +
			"}",
			"Inner");

	assertElementsEqual(
		"Unexpected elements",
		"Inner {key=Ltest0096/X<Ljava/lang/String;>.Inner<>;} [in X [in [Working copy] X.java [in test0096 [in src2 [in Resolve]]]]]",
		elements,
		true/*show key*/
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=95481
public void test0097() throws JavaModelException {
	ICompilationUnit imported = null;
	try {
		imported = getWorkingCopy(
				"/Resolve/src2/test0097/Key.java",
				"public class Key<\n" +
				"	TT extends Test<KK, TT>,\n" +
				"	KK extends Key<TT, KK>> {\n" +
				"}\n");

		IJavaElement[] elements = select(
				"/Resolve/src2/test0097/Test.java",
				"public class Test<\n" +
				"	K extends Key<T, K>,\n" +
				"	T extends Test<K, T>> {\n" +
				"}\n",
				"Key");

		assertElementsEqual(
			"Unexpected elements",
			"Key [in [Working copy] Key.java [in test0097 [in src2 [in Resolve]]]]",
			elements
		);
	} finally {
		if(imported != null) {
			imported.discardWorkingCopy();
		}
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=83206
public void test0098() throws JavaModelException {
	ICompilationUnit imported = null;
	try {
		imported = getWorkingCopy(
				"/Resolve/src2/test0098/Color.java",
				"public enum Color<\n" +
				"	BLUE,\n" +
				"	RED;\n" +
				"}\n");

		IJavaElement[] elements = select(
				"/Resolve/src2/test0098/Test.java",
				"public class Test<\n" +
				"	void foo() {\n" +
				"		Color.valueOf(\"RED\");\n" +
				"	}\n" +
				"}\n",
				"valueOf");

		assertElementsEqual(
			"Unexpected elements",
			"",
			elements
		);
	} finally {
		if(imported != null) {
			imported.discardWorkingCopy();
		}
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=99645
public void test0099() throws JavaModelException {
	IJavaElement[] elements = select(
			"/Resolve/src2/test0099/Test.java",
			"public class Test<\n" +
			"	void foo() {\n" +
			"		class Local<TLocal>{}\n" +
			"	}\n" +
			"}\n",
			"TLocal");

	assertElementsEqual(
		"Unexpected elements",
		"<TLocal> [in Local [in foo() [in Test [in [Working copy] Test.java [in test0099 [in src2 [in Resolve]]]]]]]",
		elements
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=99645
public void test0100() throws JavaModelException {
	IJavaElement[] elements = select(
			"/Resolve/src2/test0100/Test.java",
			"public class Test<\n" +
			"	void foo() {\n" +
			"		class Local<TLocal>{\n" +
			"			TLocal var;\n" +
			"		}\n" +
			"	}\n" +
			"}\n",
			"TLocal");

	assertElementsEqual(
		"Unexpected elements",
		"<TLocal> [in Local [in foo() [in Test [in [Working copy] Test.java [in test0100 [in src2 [in Resolve]]]]]]]",
		elements
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=99645
public void test0101() throws JavaModelException {
	IClassFile cu = getClassFile("Resolve", "class-folder", "test0101", "Test.class");

	String str = cu.getSource();
	int start = str.indexOf("T field;");
	int length = "T".length();
	IJavaElement[] elements = cu.codeSelect(start, length);
	assertElementsEqual(
			"Unexpected elements",
			"<T> [in Test [in Test.class [in test0101 [in class-folder [in Resolve]]]]]",
			elements
	);

	ISourceRange nameRange = ((ITypeParameter)elements[0]).getNameRange();
	assertEquals("Offset is not correct" , str.indexOf("T>"), nameRange.getOffset());
	assertEquals("Length is not correct" , "T".length(), nameRange.getLength());
}

/*
 * Ensure that resolving a binary synthetic contructor call in a generic class returns the member type
 * (regression test for bug 131519 JDK with attached source unnavigable.)
 */
public void test0102() throws CoreException, IOException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {}, new String[] {"JCL18_LIB"}, "", CompilerOptions.getFirstSupportedJavaVersion());
		String source =
			"public class X<E> {\n" +
			"  private class Y {\n" +
			"  }\n" +
			"  Object foo() {\n" +
			"    return new Y();\n" +
			"  }\n" +
			"}";
		addLibrary(project, "lib15.jar", "lib15.zip", new String[] {"X.java", source}, CompilerOptions.getFirstSupportedJavaVersion());
		IClassFile classFile = getClassFile("P", "/P/lib15.jar", "", "X.class");
		int start = source.indexOf("Y()");
		int end = source.indexOf("();");
		IJavaElement[] elements = classFile.codeSelect(start, end-start);
		assertElementsEqual(
			"Unexpected selection",
			"Y [in X$Y.class [in <default> [in lib15.jar [in P]]]]",
			elements);
	} finally {
		deleteProject("P");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=165900
public void test0103() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Resolve/src/test/Test.java",
		"package test;\n"+
		"public class Test {\n" +
		"  <T extends Test1> void foo(T t) {}\n" +
		"  <T extends Test2> void foo(T t) {}\n" +
		"  void bar(Object o) {\n" +
		"    foo(o);\n" +
		"  }\n" +
		"}\n" +
		"class Test1 {\n" +
		"}\n" +
		"class Test2 {\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int start = str.lastIndexOf("foo(o)");
	int length = "foo".length();
	IJavaElement[] elements =  this.workingCopies[0].codeSelect(start, length, this.wcOwner);

	assertElementsEqual(
			"Unexpected elements",
			"foo(T) [in Test [in [Working copy] Test.java [in test [in src [in Resolve]]]]]",
			elements
		);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=165900
public void test0104() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Resolve/src/test/Test.java",
		"package test;\n"+
		"public class Test {\n" +
		"  <T extends Test1> T foo(Test3<T> t) {return null;}\n" +
		"  <T extends Test2> T foo(Test3<T> t) {return null;}\n" +
		"  void bar(Object o) {\n" +
		"    foo(o);\n" +
		"  }\n" +
		"}\n" +
		"class Test1 {\n" +
		"}\n" +
		"class Test2 {\n" +
		"}\n" +
		"class Test3 <U> {\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int start = str.lastIndexOf("foo(o)");
	int length = "foo".length();
	IJavaElement[] elements =  this.workingCopies[0].codeSelect(start, length, this.wcOwner);

	assertElementsEqual(
			"Unexpected elements",
			"foo(Test3<T>) [in Test [in [Working copy] Test.java [in test [in src [in Resolve]]]]]",
			elements
		);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=165900
public void test0105() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Resolve/src/test/Test.java",
		"package test;\n"+
		"public class Test {\n" +
		"  <T extends Test1> T foo(Test3<T> t) {return null;}\n" +
		"  <T extends Test2> T foo(Test3<T> t) {return null;}\n" +
		"  void bar(Test3 o) {\n" +
		"    foo(o);\n" +
		"  }\n" +
		"}\n" +
		"class Test1 {\n" +
		"}\n" +
		"class Test2 {\n" +
		"}\n" +
		"class Test3 <U> {\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int start = str.lastIndexOf("foo(o)");
	int length = "foo".length();
	IJavaElement[] elements =  this.workingCopies[0].codeSelect(start, length, this.wcOwner);

	assertElementsEqual(
			"Unexpected elements",
			"foo(Test3<T>) [in Test [in [Working copy] Test.java [in test [in src [in Resolve]]]]]",
			elements
		);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=165900
public void test0106() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Resolve/src/test/Test.java",
		"package test;\n"+
		"public class Test {\n" +
		"  <T extends Test1> T foo(Test3<T> t) {return null;}\n" +
		"  <T extends Test2> T foo(Test3<T> t) {return null;}\n" +
		"  void bar(Test3<Object> o) {\n" +
		"    foo(o);\n" +
		"  }\n" +
		"}\n" +
		"class Test1 {\n" +
		"}\n" +
		"class Test2 {\n" +
		"}\n" +
		"class Test3 <U> {\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int start = str.lastIndexOf("foo(o)");
	int length = "foo".length();
	IJavaElement[] elements =  this.workingCopies[0].codeSelect(start, length, this.wcOwner);

	assertElementsEqual(
			"Unexpected elements",
			"foo(Test3<T>) [in Test [in [Working copy] Test.java [in test [in src [in Resolve]]]]]",
			elements
		);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=185318
public void test0107() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Resolve/src/test/Test.java",
			"package test;\n" +
			"import static test0107.q.Y.foo2;\n" +
			"public class Test {\n" +
			"}");

	String str = this.workingCopies[0].getSource();
	int start = str.lastIndexOf("foo2");
	int length = "foo2".length();
	IJavaElement[] elements =  this.workingCopies[0].codeSelect(start, length, this.wcOwner);

	assertElementsEqual(
			"Unexpected elements",
			"foo2() [in Y [in Y.class [in test0107.q [in bug185318r.jar [in Resolve]]]]]",
			elements
		);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=185318
public void test0108() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Resolve/src/test/Test.java",
			"package test;\n" +
			"import static test0108.q.Y.foo2;\n" +
			"public class Test {\n" +
			"}");

	String str = this.workingCopies[0].getSource();
	int start = str.lastIndexOf("foo2");
	int length = "foo2".length();
	IJavaElement[] elements =  this.workingCopies[0].codeSelect(start, length, this.wcOwner);

	assertElementsEqual(
			"Unexpected elements",
			"foo2() [in Y [in Y.class [in test0108.q [in bug185318r.jar [in Resolve]]]]]",
			elements
		);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=185318
public void test0109() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Resolve/src/test/Test.java",
			"package test;\n" +
			"import static test0109.q.Y.foo2;\n" +
			"public class Test {\n" +
			"}");

	String str = this.workingCopies[0].getSource();
	int start = str.lastIndexOf("foo2");
	int length = "foo2".length();
	IJavaElement[] elements =  this.workingCopies[0].codeSelect(start, length, this.wcOwner);

	assertElementsEqual(
			"Unexpected elements",
			"foo2 [in Y [in Y.class [in test0109.q [in bug185318r.jar [in Resolve]]]]]",
			elements
		);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=185318
public void test0110() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Resolve/src/test/Test.java",
			"package test;\n" +
			"import static test0110.q.Y.foo2;\n" +
			"public class Test {\n" +
			"}");

	String str = this.workingCopies[0].getSource();
	int start = str.lastIndexOf("foo2");
	int length = "foo2".length();
	IJavaElement[] elements =  this.workingCopies[0].codeSelect(start, length, this.wcOwner);

	assertElementsEqual(
			"Unexpected elements",
			"foo2 [in Y [in Y.class [in test0110.q [in bug185318r.jar [in Resolve]]]]]",
			elements
		);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=209661
public void test0111() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Resolve/src/test/Test.java",
			"package test;\n" +
			"\n" +
			"public class T {" +
			"	void m(@MyAnnot(\"unuse\") int arg) {\n" +
			"		@MyAnnot(\"unused\") int local; \n" +
			"	}\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Resolve/src/test/MyAnnot.java",
			"package test;\n" +
			"\n" +
			"public @inteface MyAnnot {\n" +
			"	String value();\n" +
			"}");

	String str = this.workingCopies[0].getSource();
	int start = str.lastIndexOf("local");
	int length = "local".length();
	IJavaElement[] elements =  this.workingCopies[0].codeSelect(start, length, this.wcOwner);

	assertElementsEqual(
			"Unexpected elements",
			"local [in m(int) [in T [in [Working copy] Test.java [in test [in src [in Resolve]]]]]]",
			elements
		);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=209661
public void test0112() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Resolve/src/test/Test.java",
			"package test;\n" +
			"\n" +
			"public class T {" +
			"	void m(@MyAnnot(\"unuse\") int arg) {\n" +
			"		@MyAnnot(\"unused\") int local; \n" +
			"	}\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Resolve/src/test/MyAnnot.java",
			"package test;\n" +
			"\n" +
			"public @inteface MyAnnot {\n" +
			"	String value();\n" +
			"}");

	String str = this.workingCopies[0].getSource();
	int start = str.lastIndexOf("arg");
	int length = "arg".length();
	IJavaElement[] elements =  this.workingCopies[0].codeSelect(start, length, this.wcOwner);

	assertElementsEqual(
			"Unexpected elements",
			"arg [in m(int) [in T [in [Working copy] Test.java [in test [in src [in Resolve]]]]]]",
			elements
		);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=222458
public void test0113() throws JavaModelException {
	ICompilationUnit definition = null;
	try {
		definition = getWorkingCopy(
			"/Resolve/src2/test0113/Test.java",
			"package test0113;\n" +
			"public class Test {\n" +
			"  class Member<T> {\n" +
			"  }\n" +
			"}",
			this.wcOwner
		);
		IJavaElement[] elements = select(
				"/Resolve/src2/test0113/Test2.java",
				"package test0113;\n" +
				"public class Test2 {\n" +
				"  Test.Member<String> field;\n" +
				"}",
				"Member");
		assertEquals("test0113.Test.Member<java.lang.String>", ((IType)elements[0]).getFullyQualifiedParameterizedName());
	} finally {
		if (definition != null)
			definition.discardWorkingCopy();
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=222458
public void test0114() throws Exception {
	IResource rootResource = getPackageFragmentRoot("Resolve", "/Resolve/class-folder").getResource();
	IPath rootLocation = rootResource.getLocation();
	try {
		Util.createClassFolder(new String[] {
			"test0114/Test.java",
			"package test0114;\n" +
			"public class Test {\n" +
			"  class Member<T> {\n" +
			"  }\n" +
			"}"
			},
			rootLocation.toOSString(),
			CompilerOptions.getFirstSupportedJavaVersion());
		rootResource.refreshLocal(IResource.DEPTH_INFINITE, null);
		IJavaElement[] elements = select(
				"/Resolve/src2/test0114/Test2.java",
				"package test0114;\n" +
				"public class Test2 {\n" +
				"  Test.Member<String> field;\n" +
				"}",
				"Member");
		assertEquals("test0114.Test.Member<java.lang.String>", ((IType)elements[0]).getFullyQualifiedParameterizedName());
	} finally {
		deleteResource(rootLocation.append("test0114").toFile());
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=238534
public void test0115() throws Exception {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Resolve/src/test/X.java",
			"package test;\n" +
			"\n" +
			"import java.io.IOException;\n" +
			"\n" +
			"interface TreeVisitor<T, U> {\n" +
			"        public T visit(U location);\n" +
			"}\n" +
			"\n" +
			"interface TreeVisitable<U> {\n" +
			"        public <T> T visit(TreeVisitor<T, U> visitor) throws IOException;\n" +
			"}\n" +
			"\n" +
			"abstract class Param implements TreeVisitable<Param> {\n" +
			"        public final Param lookforParam(final String name) {\n" +
			"                TreeVisitor<Param, Param> visitor = new TreeVisitor<Param,Param>() {\n" +
			"                        public Param visit(Param location) {\n" +
			"                                return null;\n" +
			"                        }\n" +
			"                };\n" +
			"                return visit(visitor); // SELECT #visit(...)\n" +
			"        }\n" +
			"\n" +
			"        public abstract <T> T visit(TreeVisitor<T, Param> visitor);\n" +
			"}\n" +
			"\n" +
			"class StructParam extends Param {\n" +
			"        public <T> T visit(TreeVisitor<T, Param> visitor) {\n" +
			"                return null;\n" +
			"        }\n" +
			"}\n" +
			"\n" +
			"public class X {\n" +
			"        public static void main(String[] args) {\n" +
			"                StructParam p = new StructParam();\n" +
			"                p.lookforParam(\"abc\");\n" +
			"                System.out.println(\"done\");\n" +
			"        }\n" +
			"\n" +
			"}");

	String str = this.workingCopies[0].getSource();
	int start = str.lastIndexOf("visit(visitor); // SELECT #visit(...)");
	int length = "visit".length();
	IJavaElement[] elements =  this.workingCopies[0].codeSelect(start, length, this.wcOwner);

	assertElementsEqual(
			"Unexpected elements",
			"visit(TreeVisitor<T,Param>) [in Param [in [Working copy] X.java [in test [in src [in Resolve]]]]]",
			elements
		);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=238534
public void test0116() throws Exception {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Resolve/src/test/Test.java",
			"package test;\n" +
			"\n" +
			"import java.io.IOException;\n" +
			"\n" +
			"public abstract class Test implements A, B {\n" +
			"	public void bar(String i) {\n" +
			"		foo(i);\n" +
			"	}\n" +
			"}\n" +
			"interface A {\n" +
			"	public <T> void foo(T a) throws EA{\n" +
			"	}\n" +
			"}\n" +
			"interface B {\n" +
			"	public <T> void foo(T b) throws EB {\n" +
			"	}\n" +
			"}\n" +
			"class EA extends Exception {\n" +
			"}\n" +
			"class EB extends Exception {\n" +
			"}");

	String str = this.workingCopies[0].getSource();
	int start = str.lastIndexOf("foo(i)");
	int length = "foo".length();
	IJavaElement[] elements =  this.workingCopies[0].codeSelect(start, length, this.wcOwner);

	assertElementsEqual(
			"Unexpected elements",
			"foo(T) [in A [in [Working copy] Test.java [in test [in src [in Resolve]]]]]",
			elements
		);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=230830
public void test0117() throws Exception {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Resolve/src/test/Test.java",
			"package test;\n" +
			"\n" +
			"public class Test {\n" +
			"	public void foo() {\n" +
			"		@A(n=) String str;\n" +
			"	}\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Resolve/src/test/A.java",
			"package test;\n" +
			"\n" +
			"public @interface A {\n" +
			"	public String n();\n" +
			"}");


	String str = this.workingCopies[0].getSource();
	int start = str.lastIndexOf("str");
	int length = "str".length();
	IJavaElement[] elements =  this.workingCopies[0].codeSelect(start, length, this.wcOwner);

	assertElementsEqual(
			"Unexpected elements",
			"str [in foo() [in Test [in [Working copy] Test.java [in test [in src [in Resolve]]]]]]",
			elements
		);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=230830
public void test0118() throws Exception {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Resolve/src/test/Test.java",
			"package test;\n" +
			"\n" +
			"public class Test {\n" +
			"	public void foo() {\n" +
			"		@A(n=) String str;\n" +
			"	}\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Resolve/src/test/A.java",
			"package test;\n" +
			"\n" +
			"public @interface A {\n" +
			"	public String n();\n" +
			"}");


	String str = this.workingCopies[0].getSource();
	int start = str.lastIndexOf("str");
	int length = "str".length();
	IJavaElement[] elements =  this.workingCopies[0].codeSelect(start, length, this.wcOwner);

	if (elements != null &&
			elements.length > 0 &&
			elements[0] instanceof IAnnotatable) {
		IAnnotation[] annotations = ((IAnnotatable)elements[0]).getAnnotations();
		assertAnnotationsEqual(
			"@A(n=<null>)\n",
			annotations);
	} else {
		assertTrue("Unexpected elements", false);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=209639
public void test0119() throws Exception {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
			"/Resolve/src/test/Test.java",
			"package test;\n" +
			"public class Test  {\n" +
			"	public List<String> foo() {\n" +
			"		return Collections.emptyList();\n" +
			"	}\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Resolve/src/test/Collections.java",
			"package test;\n" +
			"public class Collections  {\n" +
			"	public static final <T> List<T> emptyList() {return null;}\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Resolve/src/test/List.java",
			"package test;\n" +
			"public class List<E>  {\n" +
			"}");

	String str = this.workingCopies[0].getSource();
	int start = str.lastIndexOf("emptyList");
	int length = "emptyList".length();
	IJavaElement[] elements =  this.workingCopies[0].codeSelect(start, length, this.wcOwner);

	assertElementsEqual(
			"Unexpected elements",
			"emptyList() {key=Ltest/Collections;.emptyList<T:Ljava/lang/Object;>()Ltest/List<TT;>;%<Ljava/lang/String;>} [in Collections [in [Working copy] Collections.java [in test [in src [in Resolve]]]]]",
			elements,
			true
		);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=209639
public void test0120() throws Exception {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
			"/Resolve/src/test/Test.java",
			"package test;\n" +
			"public class Test  {\n" +
			"	public void foo() {\n" +
			"		List<String> local = Collections.emptyList();\n" +
			"	}\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Resolve/src/test/Collections.java",
			"package test;\n" +
			"public class Collections  {\n" +
			"	public static final <T> List<T> emptyList() {return null;}\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Resolve/src/test/List.java",
			"package test;\n" +
			"public class List<E>  {\n" +
			"}");

	String str = this.workingCopies[0].getSource();
	int start = str.lastIndexOf("emptyList");
	int length = "emptyList".length();
	IJavaElement[] elements =  this.workingCopies[0].codeSelect(start, length, this.wcOwner);

	assertElementsEqual(
			"Unexpected elements",
			"emptyList() {key=Ltest/Collections;.emptyList<T:Ljava/lang/Object;>()Ltest/List<TT;>;%<Ljava/lang/String;>} [in Collections [in [Working copy] Collections.java [in test [in src [in Resolve]]]]]",
			elements,
			true
		);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=209639
public void test0121() throws Exception {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
			"/Resolve/src/test/Test.java",
			"package test;\n" +
			"public class Test  {\n" +
			"	List<String> field = Collections.emptyList();\n" +
			"	public void foo() {\n" +
			"	}\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Resolve/src/test/Collections.java",
			"package test;\n" +
			"public class Collections  {\n" +
			"	public static final <T> List<T> emptyList() {return null;}\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Resolve/src/test/List.java",
			"package test;\n" +
			"public class List<E>  {\n" +
			"}");

	String str = this.workingCopies[0].getSource();
	int start = str.lastIndexOf("emptyList");
	int length = "emptyList".length();
	IJavaElement[] elements =  this.workingCopies[0].codeSelect(start, length, this.wcOwner);

	assertElementsEqual(
			"Unexpected elements",
			"emptyList() {key=Ltest/Collections;.emptyList<T:Ljava/lang/Object;>()Ltest/List<TT;>;%<Ljava/lang/String;>} [in Collections [in [Working copy] Collections.java [in test [in src [in Resolve]]]]]",
			elements,
			true
		);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=209639
public void test0122() throws Exception {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Resolve/src/test/Test.java",
			"package test;\n" +
			"public class Test  {\n" +
			"	static <T> T foo() { return null; }\n" +
			"	String[] strings = { foo() };\n" +
			"}");

	String str = this.workingCopies[0].getSource();
	int start = str.lastIndexOf("foo");
	int length = "foo".length();
	IJavaElement[] elements =  this.workingCopies[0].codeSelect(start, length, this.wcOwner);

	assertElementsEqual(
			"Unexpected elements",
			"foo() {key=Ltest/Test;.foo<T:Ljava/lang/Object;>()TT;%<Ljava/lang/String;>} [in Test [in [Working copy] Test.java [in test [in src [in Resolve]]]]]",
			elements,
			true
		);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=209639
public void test0123() throws Exception {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Resolve/src/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"        <T> T bar(T t) { return t; }\n" +
			"        void foo(boolean b, Runnable r) {\n" +
			"                Zork z = null;\n" +
			"                String s = (String) bar(z); // 5\n" +
			"        }\n" +
			"}\n" +
			"\n");

	String str = this.workingCopies[0].getSource();
	int start = str.lastIndexOf("bar");
	int length = "bar".length();
	IJavaElement[] elements =  this.workingCopies[0].codeSelect(start, length, this.wcOwner);

	assertElementsEqual(
			"Unexpected elements",
			"bar(T) {key=Ltest/Test;.bar<T:Ljava/lang/Object;>(TT;)TT;%<>} [in Test [in [Working copy] Test.java [in test [in src [in Resolve]]]]]",
			elements,
			true
		);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=308356
public void test0124() throws Exception {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Resolve/src/Test.java",
			"public class Test {\n" +
			"	public static void main(String[] args) {\n" +
			"		String str\ud842\udf9f = null;\n" +
			"	}\n" +
			"}");

	String str = this.workingCopies[0].getSource();
	String CONST1 = "String ";
	int start = str.indexOf("String ");
	start += CONST1.length();
	int start2 = str.indexOf(" =");
	int length = start2 - start + 1;
	IJavaElement[] elements =  this.workingCopies[0].codeSelect(start, length, this.wcOwner);

	assertElementsEqual(
			"Unexpected elements",
			"str\ud842\udf9f [in main(String[]) [in Test [in [Working copy] Test.java [in <default> [in src [in Resolve]]]]]]",
			elements,
			true
		);
}
// Bug 300576 - NPE Computing type hierarchy when compliance doesn't match libraries
// test that missing java.lang.Enum due to bogus project setup doesn't cause NPE
public void test0125() throws CoreException {
	// using wrong JCL (should be CompilerOptions.getFirstSupportedJavaVersion()):
	try {
		this.createJavaProject("P0125", new String[] {"src"}, new String[] {getExternalJCLPathString()}, "bin", CompilerOptions.getFirstSupportedJavaVersion()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		WorkingCopyOwner owner = newWorkingCopyOwner(new BasicProblemRequestor());
		ICompilationUnit cu = getWorkingCopy(
				"/P0125/src/Test.java",
				"public enum Test {\n" +
				"}",
				owner);
		IType type = cu.getType("Test");
		assertEquals("", "Test", type.getElementName());
		assertEquals("", null, type.getSuperclassName());
	} finally {
		deleteProject("P0125");
	}
}

public void testBrokenSwitch0() throws JavaModelException {
	ICompilationUnit cu = getWorkingCopy("/Resolve/src/Test.java",
			"interface ILog {\n" +
			"	void log(String status);\n" +
			"}\n" +
			"public class Test {\n" +
			"    private static ILog test() {\n" +
			"        return new ILog() {\n" +
			"            @Override\n" +
			"            public void log(String status) {\n" +
			"                switch (status.length()) { // here\n" +
			"                case\n" +
			"                }\n" +
			"            }\n" +
			"        };\n" +
			"    }\n" +
			"}\n");
	IJavaElement[] elements = codeSelect(cu, "length()", "length");
	assertElementsEqual(
				"Unexpected elements",
				"length() [in String [in String.class [in java.lang [in "+ getExternalJCLPathString(CompilerOptions.getFirstSupportedJavaVersion()) + "]]]]",
				elements);
}

public void testBrokenSwitch1() throws JavaModelException {
	ICompilationUnit cu = getWorkingCopy("/Resolve/src/Test.java",
			"interface ILog {\n" +
			"	void log(String status);\n" +
			"}\n" +
			"public class Test {\n" +
			"    private static ILog test() {\n" +
			"        return new ILog() {\n" +
			"            @Override\n" +
			"            public void log(String status) {\n" +
			"                Boolean severity = Boolean.FALSE;" +
			"                switch (status.length()) { // here\n" +
			"                case\n" +
			"                }\n" +
			"            }\n" +
			"        };\n" +
			"    }\n" +
			"}\n");
	IJavaElement[] elements = codeSelect(cu, "length()", "length");
	assertElementsEqual(
				"Unexpected elements",
				"length() [in String [in String.class [in java.lang [in "+ getExternalJCLPathString(CompilerOptions.getFirstSupportedJavaVersion()) + "]]]]",
				elements);
}
}
