/*******************************************************************************
* Copyright (c) 2023 Advantest Europe GmbH and others.
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
*
https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Srikanth Sankaran - initial implementation
*******************************************************************************/

package org.eclipse.jdt.core.tests.model;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;

import junit.framework.Test;

public class ResolveTests21 extends AbstractJavaModelTests {
	ICompilationUnit wc = null;

static {
//	 TESTS_NAMES = new String[] { "testBug577508_4" };
	// TESTS_NUMBERS = new int[] { 124 };
	// TESTS_RANGE = new int[] { 16, -1 };
}
public static Test suite() {
	return buildModelTestSuite(ResolveTests21.class);
}
public ResolveTests21(String name) {
	super(name);
}
@Override
public ICompilationUnit getWorkingCopy(String path, String source) throws JavaModelException {
	return super.getWorkingCopy(path, source, this.wcOwner);
}
@Override
public void setUpSuite() throws Exception {
	super.setUpSuite();
	setUpJavaProject("Resolve", "21", false);
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

public void test001() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve/src/X.java",
			"public class X {\n"
			+ "  private String abc = \"abc\"; // unused\n"
			+ "  public void main(String[] args) {\n"
			+ "    String s = STR.\"A simple String \\{clone(abc)}\";\n"
			+ "    System.out.println(s);\n"
			+ "  }\n"
			+ "  public String clone(String s) {\n"
			+ "    return \"clone\";\n"
			+ "  }\n"
			+ "}");
	String str = this.wc.getSource();
	String selection = "clone";
	int start = str.indexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"clone(String) [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
public void test002() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve/src/X.java",
			"public class X {\n"
			+ "  private String abc = \"abc\"; // unused\n"
			+ "  public void main(String[] args) {\n"
			+ "    String s = STR.\"A simple String \\{clone(abc)}\";\n"
			+ "    System.out.println(s);\n"
			+ "  }\n"
			+ "  public String clone(String s) {\n"
			+ "    return \"clone\";\n"
			+ "  }\n"
			+ "}");
	String str = this.wc.getSource();
	String selection = "abc";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"abc [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
public void test003() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve/src/X.java",
			"public class X {\n"
			+ "  static int CONST = 0;\n"
			+ "    private static int foo() {\n"
			+ "    return CONST;\n"
			+ "  }\n"
			+ "  public static void main(String argv[]) {\n"
			+ "    String str = STR.\"{\\{new Object() { class Test { int i; Test() { i = foo();}}}.new Test().i\\u007d}\";\n"
			+ "    System.out.println(str.equals(\"{0}\"));\n"
			+ "  }\n"
			+ "}");
	String str = this.wc.getSource();
	String selection = "foo";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"foo() [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
public void test004() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve/src/X.java",
			"public class X {\n"
			+ "    private final static int LF  = (char) 0x000A;\n"
			+ "    private static boolean compare(String s) {\n"
			+ "        char[] chars = new char[] {LF,'a','b','c','d'};\n"
			+ "        if (chars.length != s.length())\n"
			+ "            return false;\n"
			+ "        for (int i = 0; i < s.length(); i++) {\n"
			+ "            if(chars[i] != s.charAt(i)) {\n"
			+ "                return false;\n"
			+ "            }\n"
			+ "        }\n"
			+ "        return true;\n"
			+ "    }\n"
			+ "    public static void main(String argv[]) {\n"
			+ "        String abcd = \"abcd\"; //$NON-NLS-1$\n"
			+ "        String textBlock = STR.\"\"\"\n"
			+ "   \n"
			+ "\\{abcd}\"\"\";//$NON-NLS-1$\n"
			+ "        System.out.println(compare(textBlock));\n"
			+ "    }\n"
			+ "}");
	String str = this.wc.getSource();
	String selection = "abcd";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"abcd [in main(String[]) [in X [in [Working copy] X.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2572
// [code select] ClassCastException when hovering in switch case yield
public void testIssue2572() throws JavaModelException {
	this.wc = getWorkingCopy("/Resolve/src/Test.java",
			"""
			public class Test {
				public static void main(String[] args) {
					test(new Bar(0));
					test(new FooBar<>("0"));
				}

				public static void test(final Foo foo) {
					final String str = switch (foo) {
					case Bar(Number number) -> {
						yield number.toString();
					}
					case BarFoo(String data) -> {
						yield data;
					}
					case final FooBar<?> fooBar -> {
						yield fooBar.object.toString();
					}
					};
					System.out.println(str);
				}

				private static sealed interface Foo {
				}

				private record Bar(Number number) implements Foo {
				}

				private record BarFoo(String data) implements Foo {
				}

				private record FooBar<T>(T object) implements Foo {
				}
			}
			"""
			);
	String str = this.wc.getSource();
	String selection = "toString";
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	IJavaElement[] elements = this.wc.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"toString() [in Object [in Object.class [in java.lang [in " + getExternalPath() + "jclMin21.jar]]]]",
		elements
	);
}
}
