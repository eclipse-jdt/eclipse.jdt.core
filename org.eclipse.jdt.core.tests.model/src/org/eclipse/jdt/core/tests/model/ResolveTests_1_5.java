/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.lang.reflect.Method;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.ICompilationUnit;

import junit.framework.*;

public class ResolveTests_1_5 extends AbstractJavaModelTests {
	ICompilationUnit wc = null;
	
public static Test suite() {
	TestSuite suite = new Suite(ResolveTests_1_5.class.getName());		

	if (true) {
		Class c = ResolveTests_1_5.class;
		Method[] methods = c.getMethods();
		for (int i = 0, max = methods.length; i < max; i++) {
			if (methods[i].getName().startsWith("test")) { //$NON-NLS-1$
				suite.addTest(new ResolveTests_1_5(methods[i].getName()));
			}
		}
		return suite;
	}
	suite.addTest(new ResolveTests_1_5("test0028"));			
	return suite;
}

public ResolveTests_1_5(String name) {
	super(name);
}
public void setUpSuite() throws Exception {
	super.setUpSuite();
	
	setUpJavaProject("Resolve", "1.5");
	
	waitUntilIndexesReady();
}

public void tearDownSuite() throws Exception {
	deleteProject("Resolve");
	
	super.tearDownSuite();
}

protected void tearDown() throws Exception {
	if(this.wc != null) {
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
		"Y key=Ltest0002/Test$X<Ljava/lang/Object;>.Y<Ljava/lang/Object;>; [in X [in Test [in Test.java [in test0002 [in src2 [in Resolve]]]]]]",
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
		"X key=Ltest0003/Test$X<Ljava/lang/Object;>; [in Test [in Test.java [in test0003 [in src2 [in Resolve]]]]]",
		elements
	);
}
public void test0004() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src2", "test0004", "Test.java");
	
	String str = cu.getSource();
	String selection = "Test";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	
	IJavaElement[] elements = cu.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Test [in Test.java [in test0004 [in src2 [in Resolve]]]]",
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
	
	String str = wc.getSource();
	String selection = "Inn";
	int start = str.lastIndexOf(selection);
	
	IJavaElement[] elements = wc.codeSelect(start, 0);
	assertElementsEqual(
		"Unexpected elements",
		"Inner key=Ltest0027/Test$Inner<Ljava/lang/Object;>; [in Test [in [Working copy] Test.java [in test0027 [in src2 [in Resolve]]]]]",
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
	
	String str = wc.getSource();
	String selection = "Inn";
	int start = str.lastIndexOf(selection);
	
	IJavaElement[] elements = wc.codeSelect(start, 0);
	assertElementsEqual(
		"Unexpected elements",
		"Inner key=Ltest0028/Test<Ljava/lang/Object;>.Inner; [in Test [in [Working copy] Test.java [in test0028 [in src2 [in Resolve]]]]]",
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
	
	String str = wc.getSource();
	String selection = "Inn";
	int start = str.lastIndexOf(selection);
	
	IJavaElement[] elements = wc.codeSelect(start, 0);
	assertElementsEqual(
		"Unexpected elements",
		"Inner key=Ltest0029/Test<Ljava/lang/Object;>.Inner<Ljava/lang/Object;>; [in Test [in [Working copy] Test.java [in test0029 [in src2 [in Resolve]]]]]",
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
	
	String str = wc.getSource();
	String selection = "Inner";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	
	IJavaElement[] elements = wc.codeSelect(start, length);
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
	
	String str = wc.getSource();
	String selection = "Inner";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	
	IJavaElement[] elements = wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Inner key=Ltest0031/Test$Inner<Ljava/lang/Object;>; [in Test [in [Working copy] Test.java [in test0031 [in src2 [in Resolve]]]]]",
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
	
	String str = wc.getSource();
	String selection = "Inner";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	
	IJavaElement[] elements = wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Inner key=Ltest0032/Test<Ljava/lang/Object;>.Inner; [in Test [in [Working copy] Test.java [in test0032 [in src2 [in Resolve]]]]]",
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
	
	String str = wc.getSource();
	String selection = "Inner";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	
	IJavaElement[] elements = wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Inner key=Ltest0033/Test<Ljava/lang/Object;>.Inner<Ljava/lang/Object;>; [in Test [in [Working copy] Test.java [in test0033 [in src2 [in Resolve]]]]]",
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
	
	String str = wc.getSource();
	String selection = "Test.Inner";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	
	IJavaElement[] elements = wc.codeSelect(start, length);
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
	
	String str = wc.getSource();
	String selection = "Test.Inner<Object>";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	
	IJavaElement[] elements = wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Inner key=Ltest0035/Test$Inner<Ljava/lang/Object;>; [in Test [in [Working copy] Test.java [in test0035 [in src2 [in Resolve]]]]]",
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
	
	String str = wc.getSource();
	String selection = "Test<Object>.Inner";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	
	IJavaElement[] elements = wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Inner key=Ltest0036/Test<Ljava/lang/Object;>.Inner; [in Test [in [Working copy] Test.java [in test0036 [in src2 [in Resolve]]]]]",
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
	
	String str = wc.getSource();
	String selection = "Test<Object>.Inner<Object>";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	
	IJavaElement[] elements = wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Inner key=Ltest0037/Test<Ljava/lang/Object;>.Inner<Ljava/lang/Object;>; [in Test [in [Working copy] Test.java [in test0037 [in src2 [in Resolve]]]]]",
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
	
	String str = wc.getSource();
	String selection = "Test.Inner";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	
	IJavaElement[] elements = wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Inner key=Ltest0038/Test$Inner<Ljava/lang/Object;>; [in Test [in [Working copy] Test.java [in test0038 [in src2 [in Resolve]]]]]",
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
	
	String str = wc.getSource();
	String selection = "Test<Object>.Inner";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	
	IJavaElement[] elements = wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Inner key=Ltest0039/Test<Ljava/lang/Object;>.Inner<Ljava/lang/Object;>; [in Test [in [Working copy] Test.java [in test0039 [in src2 [in Resolve]]]]]",
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
	
	String str = wc.getSource();
	String selection = "Inner<Object>";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	
	IJavaElement[] elements = wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Inner key=Ltest0040/Test<Ljava/lang/Object;>.Inner<Ljava/lang/Object;>; [in Test [in [Working copy] Test.java [in test0040 [in src2 [in Resolve]]]]]",
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
	
	String str = wc.getSource();
	String selection = "Local1<Local3<Object>>.Local2<Local3<Object>>";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	
	IJavaElement[] elements = wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Local2 key=Ltest0041/Test$1$Local1<Ltest0041/Test$1$Local3<Ljava/lang/Object;>;>.Local2<Ltest0041/Test$1$Local3<Ljava/lang/Object;>;>; [in Local1 [in foo() [in Test [in [Working copy] Test.java [in test0041 [in src2 [in Resolve]]]]]]]",
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
	
	String str = wc.getSource();
	String selection = "Test<? super String>.Inner<? extends String>";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	
	IJavaElement[] elements = wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Inner key=Ltest0042/Test<-Ljava/lang/String;>.Inner<+Ljava/lang/String;>; [in Test [in [Working copy] Test.java [in test0042 [in src2 [in Resolve]]]]]",
		elements
	);
}
}
