/*******************************************************************************
 * Copyright (c) 2005, 2023 IBM Corporation and others.
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

import java.io.IOException;

import junit.framework.Test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;

public class CompletionContextTests extends AbstractJavaModelCompletionTests {

public CompletionContextTests(String name) {
	super(name);
}
@Override
public void setUpSuite() throws Exception {
	if (COMPLETION_PROJECT == null)  {
		COMPLETION_PROJECT = setUpJavaProject("Completion", "1.4");
	} else {
		setUpProjectCompliance(COMPLETION_PROJECT, "1.4");
	}
	super.setUpSuite();
}

public static Test suite() {
	return buildModelTestSuite(CompletionContextTests.class);
}
static {
			//TESTS_NAMES = new String[]{"testBug553097"};
}
public void test0001() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0001/X.java",
		"package test0001;\n" +
		"public class X {\n" +
		"  ZZZZ\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location={MEMBER_START}",
		result.context);
}
public void test0002() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0002/X.java",
		"package test0002;\n" +
		"public class X {\n" +
		"  ZZZZ\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ");

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location={MEMBER_START}",
		result.context);
}
public void test0003() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0003/X.java",
		"package test0003;\n" +
		"public class X {\n" +
		"  ZZZZ\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location={MEMBER_START}",
		result.context);
}
public void test0004() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0004/X.java",
		"package test0004;\n" +
		"public class X {\n" +
		"  /**/\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("/**/") + "/**/".length();
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("/**/") + "/**/".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location={MEMBER_START}",
		result.context);
}
public void test0005() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0005/X.java",
		"package test0005;\n" +
		"public class X {\n" +
		"  ZZZZ foo()\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location={MEMBER_START}",
		result.context);
}
public void test0006() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0006/X.java",
		"package test0006;\n" +
		"public class X {\n" +
		"  ZZZZ foo()\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ");

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location={MEMBER_START}",
		result.context);
}
public void test0007() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0007/X.java",
		"package test0007;\n" +
		"public class X {\n" +
		"  ZZZZ foo()\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location={MEMBER_START}",
		result.context);
}
public void test0008() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0008/X.java",
		"package test0008;\n" +
		"public class X {\n" +
		"  /**/ foo()\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("/**/") + "/**/".length();
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("/**/") + "/**/".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location={MEMBER_START}",
		result.context);
}
public void test0009() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0009/X.java",
		"package test0009;\n" +
		"public class X {\n" +
		"  void foo() {\n" +
		"    ZZZZ\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location={STATEMENT_START}",
		result.context);
}
public void test0010() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0010/X.java",
		"package test0010;\n" +
		"public class X {\n" +
		"  void foo() {\n" +
		"    ZZZZ\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ");

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location={STATEMENT_START}",
		result.context);
}
public void test0011() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0011/X.java",
		"package test0011;\n" +
		"public class X {\n" +
		"  void foo() {\n" +
		"    ZZZZ\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location={STATEMENT_START}",
		result.context);
}
public void test0012() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0012/X.java",
		"package test0012;\n" +
		"public class X {\n" +
		"  void foo() {\n" +
		"    /**/\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("/**/") + "/**/".length();
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("/**/") + "/**/".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location={STATEMENT_START}",
		result.context);
}
public void test0013() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0013/X.java",
		"package test0013;\n" +
		"public class X extends ZZZZ {\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0014() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0014/X.java",
		"package test0014;\n" +
		"public class X extends ZZZZ {\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ");

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0015() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0015/X.java",
		"package test0015;\n" +
		"public class X extends ZZZZ {\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0016() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0016/X.java",
		"package test0016;\n" +
		"public class X extends /**/ {\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("/**/") + "/**/".length();
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("/**/") + "/**/".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0017() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src3/test0017/YYYY.java",
		"package test0017;\n" +
		"public class YYYY {\n" +
		"  public class ZZZZ {\n" +
		"  }\n" +
		"}");
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0017/X.java",
		"package test0017;\n" +
		"public class X {\n" +
		"  void foo() {\n" +
		"    YYYY.ZZZZ\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0018() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src3/test0018/YYYY.java",
		"package test0018;\n" +
		"public class YYYY {\n" +
		"  public class ZZZZ {\n" +
		"  }\n" +
		"}");
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0018/X.java",
		"package test0018;\n" +
		"public class X {\n" +
		"  void foo() {\n" +
		"    YYYY.ZZZZ\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ");

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0019() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src3/test0019/YYYY.java",
		"package test0019;\n" +
		"public class YYYY {\n" +
		"  public class ZZZZ {\n" +
		"  }\n" +
		"}");
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0019/X.java",
		"package test0019;\n" +
		"public class X {\n" +
		"  void foo() {\n" +
		"    YYYY.ZZZZ\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0020() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src3/test0020/YYYY.java",
		"package test0020;\n" +
		"public class YYYY {\n" +
		"  public class ZZZZ {\n" +
		"  }\n" +
		"}");
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0020/X.java",
		"package test0020;\n" +
		"public class X {\n" +
		"  void foo() {\n" +
		"    YYYY.\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("YYYY.") + "YYYY.".length();
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("YYYY.") + "YYYY.".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0021() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0021/X.java",
		"package test0021;\n" +
		"public class X {\n" +
		"  void foo() {\n" +
		"    test0021.ZZZZ\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0022() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0022/X.java",
		"package test0022;\n" +
		"public class X {\n" +
		"  void foo() {\n" +
		"    test0022.ZZZZ\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ");

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0023() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0023/X.java",
		"package test0023;\n" +
		"public class X {\n" +
		"  void foo() {\n" +
		"    test0023.ZZZZ\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0024() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0024/X.java",
		"package test0024;\n" +
		"public class X {\n" +
		"  void foo() {\n" +
		"    test0024.\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("test0024.") + "test0024.".length();
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("test0024.") + "test0024.".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0025() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0025/X.java",
		"package test0025;\n" +
		"public class X {\n" +
		"  Object var;\n" +
		"  void foo() {\n" +
		"    var.ZZZZ\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0026() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0026/X.java",
		"package test0026;\n" +
		"public class X {\n" +
		"  Object var;\n" +
		"  void foo() {\n" +
		"    var.ZZZZ\n" +
		"  }\n" +
		"}");



	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ");

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0027() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0027/X.java",
		"package test0027;\n" +
		"public class X {\n" +
		"  Object var;\n" +
		"  void foo() {\n" +
		"    var.ZZZZ\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0028() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0028/X.java",
		"package test0028;\n" +
		"public class X {\n" +
		"  Object var;\n" +
		"  void foo() {\n" +
		"    var.\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("var.") + "var.".length();
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("var.") + "var.".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0029() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src3/test0029/YYYY.java",
		"package test0029;\n" +
		"public class YYYY {\n" +
		"  public class ZZZZ {\n" +
		"  }\n" +
		"}");
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0029/X.java",
		"package test0029;\n" +
		"public class X extends YYYY.ZZZZ {\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0030() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src3/test0030/YYYY.java",
		"package test0030;\n" +
		"public class YYYY {\n" +
		"  public class ZZZZ {\n" +
		"  }\n" +
		"}");
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0030/X.java",
		"package test0030;\n" +
		"public class X extends YYYY.ZZZZ {\n" +
		"}");



	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ");

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0031() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src3/test0031/YYYY.java",
		"package test0031;\n" +
		"public class YYYY {\n" +
		"  public class ZZZZ {\n" +
		"  }\n" +
		"}");
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0031/X.java",
		"package test0031;\n" +
		"public class X extends YYYY.ZZZZ {\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0032() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src3/test0032/YYYY.java",
		"package test0032;\n" +
		"public class YYYY {\n" +
		"  public class ZZZZ {\n" +
		"  }\n" +
		"}");
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0032/X.java",
		"package test0032;\n" +
		"public class X extends YYYY. {\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("YYYY.") + "YYYY.".length();
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("YYYY.") + "YYYY.".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0033() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0033/X.java",
		"package test0033;\n" +
		"public class X extends test0033.ZZZZ {\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0034() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0034/X.java",
		"package test0034;\n" +
		"public class X extends test0034.ZZZZ {\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ");

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0035() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0035/X.java",
		"package test0035;\n" +
		"public class X extends test0035.ZZZZ {\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0036() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0036/X.java",
		"package test0036;\n" +
		"public class X extends test0036. {\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("test0036.") + "test0036.".length();
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("test0036.") + "test0036.".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0037() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0037/X.java",
		"package test0037;\n" +
		"public class X {\n" +
		"  X ZZZZ;\n" +
		"  X foo(){\n" +
		"    foo().ZZZZ\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0038() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0038/X.java",
		"package test0038;\n" +
		"public class X {\n" +
		"  X ZZZZ;\n" +
		"  X foo(){\n" +
		"    foo().ZZZZ\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ");

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0039() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0039/X.java",
		"package test0039;\n" +
		"public class X {\n" +
		"  X ZZZZ;\n" +
		"  X foo(){\n" +
		"    foo().ZZZZ\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0040() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0040/X.java",
		"package test0040;\n" +
		"public class X {\n" +
		"  X ZZZZ;\n" +
		"  X foo(){\n" +
		"    foo().\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("foo().") + "foo().".length();
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("foo().") + "foo().".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0041() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0041/X.java",
		"package test0041;\n" +
		"public class X {\n" +
		"  X ZZZZ;\n" +
		"  X foo(){\n" +
		"    int.ZZZZ\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0042() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0042/X.java",
		"package test0042;\n" +
		"public class X {\n" +
		"  X ZZZZ;\n" +
		"  X foo(){\n" +
		"    int.ZZZZ\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ");

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0043() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0043/X.java",
		"package test0043;\n" +
		"public class X {\n" +
		"  X ZZZZ;\n" +
		"  X foo(){\n" +
		"    int.ZZZZ\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0044() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0044/X.java",
		"package test0044;\n" +
		"public class X {\n" +
		"  X ZZZZ;\n" +
		"  X foo(){\n" +
		"    int.\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("int.") + "int.".length();
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("int.") + "int.".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0045() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0045/X.java",
		"package test0045;\n" +
		"public class X {\n" +
		"  void ZZZZ(){\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0046() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0046/X.java",
		"package test0046;\n" +
		"public class X {\n" +
		"  void ZZZZ(){\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ");

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0047() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0047/X.java",
		"package test0047;\n" +
		"public class X {\n" +
		"  void ZZZZ(){\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0048() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0048/X.java",
		"package test0048;\n" +
		"public class X {\n" +
		"  void (){\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("void ") + "void ".length();
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("void ") + "void ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0049() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0049/X.java",
		"package test0049;\n" +
		"public class X {\n" +
		"  int ZZZZ;\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0050() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0050/X.java",
		"package test0050;\n" +
		"public class X {\n" +
		"  int ZZZZ;\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ");

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0051() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0051/X.java",
		"package test0051;\n" +
		"public class X {\n" +
		"  int ZZZZ;\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0052() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0052/X.java",
		"package test0052;\n" +
		"public class X {\n" +
		"  int ;\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("int ") + "int ".length();
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("int ") + "int ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0053() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0053/X.java",
		"package test0053;\n" +
		"public class X {\n" +
		"  {int ZZZZ;}\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0054() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0054/X.java",
		"package test0054;\n" +
		"public class X {\n" +
		"  {int ZZZZ;}\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ");

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0055() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0055/X.java",
		"package test0055;\n" +
		"public class X {\n" +
		"  {int ZZZZ;}\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0056() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0056/X.java",
		"package test0056;\n" +
		"public class X {\n" +
		"  {int ;}\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("int ") + "int ".length();
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("int ") + "int ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0057() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0057/X.java",
		"package test0057;\n" +
		"public class X {\n" +
		"  void foo(int ZZZZ){}\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0058() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0058/X.java",
		"package test0058;\n" +
		"public class X {\n" +
		"  void foo(int ZZZZ){}\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ");

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0059() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0059/X.java",
		"package test0059;\n" +
		"public class X {\n" +
		"  void foo(int ZZZZ){}\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0060() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0060/X.java",
		"package test0060;\n" +
		"public class X {\n" +
		"  void foo(int ){}\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("int ") + "int ".length();
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("int ") + "int ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0061() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0061/X.java",
		"package test0061;\n" +
		"public class X ZZZZ {\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0062() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0062/X.java",
		"package test0062;\n" +
		"public class X ZZZZ {\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ");

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0063() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0063/X.java",
		"package test0063;\n" +
		"public class X ZZZZ {\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0064() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0064/X.java",
		"package test0064;\n" +
		"public class X  {\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("X ") + "X ".length();
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("X ") + "X ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0065() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0065/X.java",
		"package test0065;\n" +
		"ZZZZ\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0066() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0066/X.java",
		"package test0066;\n" +
		"ZZZZ\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ");

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0067() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0067/X.java",
		"package test0067;\n" +
		"ZZZZ\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0068() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0068/X.java",
		"package test0068;\n" +
		"/**/\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("/**/") + "/**/".length();
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("/**/") + "/**/".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0069() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0069/X.java",
		"package test0069;\n" +
		"public class X {\n" +
		"  {\n" +
		"    do{\n" +
		"    } ZZZZ\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0070() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0070/X.java",
		"package test0070;\n" +
		"public class X {\n" +
		"  {\n" +
		"    do{\n" +
		"    } ZZZZ\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ");

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0071() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0071/X.java",
		"package test0071;\n" +
		"public class X {\n" +
		"  {\n" +
		"    do{\n" +
		"    } ZZZZ\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0072() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0072/X.java",
		"package test0072;\n" +
		"public class X {\n" +
		"  {\n" +
		"    do{\n" +
		"    }/**/ \n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("/**/ ") + "/**/ ".length();
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("/**/ ") + "/**/ ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0073() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0073/X.java",
		"package ZZZZ;\n" +
		"public class X {\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0074() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0074/X.java",
		"package ZZZZ;\n" +
		"public class X {\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ");

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0075() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0075/X.java",
		"package ZZZZ;\n" +
		"public class X {\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0076() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0076/X.java",
		"package \n" +
		"public class X {\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("package ") + "package ".length();
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("package ") + "package ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0077() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0077/test/X.java",
		"package test0077.ZZZZ;\n" +
		"public class X {\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0078() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0078/test/X.java",
		"package test0078.ZZZZ;\n" +
		"public class X {\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ");

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0079() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0079/test/X.java",
		"package test0079.ZZZZ;\n" +
		"public class X {\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0080() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0080/test/X.java",
		"package test0080.\n" +
		"public class X {\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("test0080.") + "test0080.".length();
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("test0080.") + "test0080.".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0081() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0081/X.java",
		"package test0081;\n" +
		"import ZZZZ;\n" +
		"public class X {\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location={IN_IMPORT}",
		result.context);
}
public void test0082() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0082/X.java",
		"package test0082;\n" +
		"import ZZZZ;\n" +
		"public class X {\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ");

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location={IN_IMPORT}",
		result.context);
}
public void test0083() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0083/X.java",
		"package test0083;\n" +
		"import ZZZZ;\n" +
		"public class X {\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location={IN_IMPORT}",
		result.context);
}
public void test0084() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0084/X.java",
		"package test0084;\n" +
		"import \n" +
		"public class X {\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("import ") + "import ".length();
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("import ") + "import ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location={IN_IMPORT}",
		result.context);
}
public void test0085() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0085/test/X.java",
		"package test0085;\n" +
		"import test0085.ZZZZ;\n" +
		"public class X {\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location={IN_IMPORT}",
		result.context);
}
public void test0086() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0086/test/X.java",
		"package test0086;\n" +
		"import test0086.ZZZZ;\n" +
		"public class X {\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ");

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location={IN_IMPORT}",
		result.context);
}
public void test0087() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0087/test/X.java",
		"package test0087;\n" +
		"import test0087.ZZZZ;\n" +
		"public class X {\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location={IN_IMPORT}",
		result.context);
}
public void test0088() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0084/test/X.java",
		"package test0088;\n" +
		"import test0085.\n" +
		"public class X {\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("test0085.") + "test0085.".length();
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("test0085.") + "test0085.".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location={IN_IMPORT}",
		result.context);
}
public void test0089() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0089/X.java",
		"package test0089;\n" +
		"public class X {\n" +
		"  void foo(int a, int b) {\n" +
		"    this.foo(ZZZZ\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures={I}\n" +
		"expectedTypesKeys={I}\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0090() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0090/X.java",
		"package test0090;\n" +
		"public class X {\n" +
		"  void foo(int a, int b) {\n" +
		"    this.foo(ZZZZ\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ");

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures={I}\n" +
		"expectedTypesKeys={I}\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0091() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0091/X.java",
		"package test0091;\n" +
		"public class X {\n" +
		"  void foo(int a, int b) {\n" +
		"    this.foo(ZZZZ\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures={I}\n" +
		"expectedTypesKeys={I}\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0092() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0092/X.java",
		"package test0092;\n" +
		"public class X {\n" +
		"  void foo(int a, int b) {\n" +
		"    this.foo(\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("this.foo(") + "this.foo(".length();
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("this.foo(") + "this.foo(".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures={I}\n" +
		"expectedTypesKeys={I}\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0093() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0093/X.java",
		"package test0093;\n" +
		"public class X {\n" +
		"  void foo(int a, int b) {\n" +
		"    this.foo(0,ZZZZ\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures={I}\n" +
		"expectedTypesKeys={I}\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0094() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0094/X.java",
		"package test0094;\n" +
		"public class X {\n" +
		"  void foo(int a, int b) {\n" +
		"    this.foo(0,ZZZZ\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ");

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures={I}\n" +
		"expectedTypesKeys={I}\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0095() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0095/X.java",
		"package test0095;\n" +
		"public class X {\n" +
		"  void foo(int a, int b) {\n" +
		"    this.foo(0,ZZZZ\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures={I}\n" +
		"expectedTypesKeys={I}\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0096() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0096/X.java",
		"package test0096;\n" +
		"public class X {\n" +
		"  void foo(int a, int b) {\n" +
		"    this.foo(0,\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("this.foo(0,") + "this.foo(0,".length();
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("this.foo(0,") + "this.foo(0,".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures={I}\n" +
		"expectedTypesKeys={I}\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0097() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0097/X.java",
		"package test0097;\n" +
		"public class X {\n" +
		"  X(int a, int b) {}\n" +
		"  void foo(int a, int b) {\n" +
		"    new X(ZZZZ\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures={I}\n" +
		"expectedTypesKeys={I}\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0098() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0098/X.java",
		"package test0098;\n" +
		"public class X {\n" +
		"  X(int a, int b) {}\n" +
		"  void foo(int a, int b) {\n" +
		"    new X(ZZZZ\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ");

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures={I}\n" +
		"expectedTypesKeys={I}\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0099() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0099/X.java",
		"package test0099;\n" +
		"public class X {\n" +
		"  X(int a, int b) {}\n" +
		"  void foo(int a, int b) {\n" +
		"    new X(ZZZZ\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures={I}\n" +
		"expectedTypesKeys={I}\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0100() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0100/X.java",
		"package test0100;\n" +
		"public class X {\n" +
		"  X(int a, int b) {}\n" +
		"  void foo(int a, int b) {\n" +
		"    new X(\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("new X(") + "new X(".length();
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("new X(") + "new X(".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures={I}\n" +
		"expectedTypesKeys={I}\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0101() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0101/X.java",
		"package test0101;\n" +
		"public class X {\n" +
		"  X(int a, int b) {}\n" +
		"  void foo(int a, int b) {\n" +
		"    new X(0,ZZZZ\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures={I}\n" +
		"expectedTypesKeys={I}\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0102() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0102/X.java",
		"package test0102;\n" +
		"public class X {\n" +
		"  X(int a, int b) {}\n" +
		"  void foo(int a, int b) {\n" +
		"    new X(0,ZZZZ\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ");

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures={I}\n" +
		"expectedTypesKeys={I}\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0103() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0103/X.java",
		"package test0103;\n" +
		"public class X {\n" +
		"  X(int a, int b) {}\n" +
		"  void foo(int a, int b) {\n" +
		"    new X(0,ZZZZ\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures={I}\n" +
		"expectedTypesKeys={I}\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0104() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0104/X.java",
		"package test0104;\n" +
		"public class X {\n" +
		"  X(int a, int b) {}\n" +
		"  void foo(int a, int b) {\n" +
		"    new X(0,\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("new X(0,") + "new X(0,".length();
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("new X(0,") + "new X(0,".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures={I}\n" +
		"expectedTypesKeys={I}\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0105() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0105/X.java",
		"package test0105;\n" +
		"public class X {\n" +
		"  Object o = ZZZZ\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures={Ljava.lang.Object;}\n" +
		"expectedTypesKeys={Ljava/lang/Object;}\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0106() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0106/X.java",
		"package test0106;\n" +
		"public class X {\n" +
		"  Object o = ZZZZ\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ");

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures={Ljava.lang.Object;}\n" +
		"expectedTypesKeys={Ljava/lang/Object;}\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0107() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0107/X.java",
		"package test0107;\n" +
		"public class X {\n" +
		"  Object o = ZZZZ\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures={Ljava.lang.Object;}\n" +
		"expectedTypesKeys={Ljava/lang/Object;}\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0108() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0108/X.java",
		"package test0108;\n" +
		"public class X {\n" +
		"  Object o = \n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("Object o = ") + "Object o = ".length();
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("Object o = ") + "Object o = ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures={Ljava.lang.Object;}\n" +
		"expectedTypesKeys={Ljava/lang/Object;}\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0109() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0109/X.java",
		"package test0109;\n" +
		"public class X {\n" +
		"  Object o = new ZZZZ\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures={Ljava.lang.Object;}\n" +
		"expectedTypesKeys={Ljava/lang/Object;}\n"+
		"completion token location={CONSTRUCTOR_START}",
		result.context);
}
public void test0110() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0110/X.java",
		"package test0110;\n" +
		"public class X {\n" +
		"  Object o = new ZZZZ\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ");

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures={Ljava.lang.Object;}\n" +
		"expectedTypesKeys={Ljava/lang/Object;}\n"+
		"completion token location={CONSTRUCTOR_START}",
		result.context);
}
public void test0111() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0111/X.java",
		"package test0111;\n" +
		"public class X {\n" +
		"  Object o = new ZZZZ\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures={Ljava.lang.Object;}\n" +
		"expectedTypesKeys={Ljava/lang/Object;}\n"+
		"completion token location={CONSTRUCTOR_START}",
		result.context);
}
public void test0112() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0112/X.java",
		"package test0112;\n" +
		"public class X {\n" +
		"  Object o = new \n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("Object o = new ") + "Object o = new ".length();
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("Object o = new ") + "Object o = new ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures={Ljava.lang.Object;}\n" +
		"expectedTypesKeys={Ljava/lang/Object;}\n"+
		"completion token location={CONSTRUCTOR_START}",
		result.context);
}
public void test0113() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0113/X.java",
		"package test0113;\n" +
		"public class X {\n" +
		"  Object o = new Object() {\n" +
		"    ZZZZ\n" +
		"  };\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location={MEMBER_START}",
		result.context);
}
public void test0114() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0114/X.java",
		"package test0114;\n" +
		"public class X {\n" +
		"  Object o = new Object() {\n" +
		"    ZZZZ\n" +
		"  };\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ");

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location={MEMBER_START}",
		result.context);
}
public void test0115() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0115/X.java",
		"package test0115;\n" +
		"public class X {\n" +
		"  Object o = new Object() {\n" +
		"    ZZZZ\n" +
		"  };\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location={MEMBER_START}",
		result.context);
}
public void test0116() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0116/X.java",
		"package test0116;\n" +
		"public class X {\n" +
		"  Object o = new Object() {\n" +
		"    /**/\n" +
		"  };\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("/**/") + "/**/".length();
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("/**/") + "/**/".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location={MEMBER_START}",
		result.context);
}
public void test0117() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0117/X.java",
		"package test0117;\n" +
		"public class X {\n" +
		"  String s = \"ZZZZ\";\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("\"ZZZZ\"");
	int tokenEnd = tokenStart + "\"ZZZZ\"".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_STRING_LITERAL\n" +
		"expectedTypesSignatures={Ljava.lang.String;}\n" +
		"expectedTypesKeys={Ljava/lang/String;}\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0118() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0118/X.java",
		"package test0118;\n" +
		"public class X {\n" +
		"  String s = \"ZZZZ\";\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("\"ZZZZ\"");
	int tokenEnd = tokenStart + "\"ZZZZ\"".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZ\"\n" +
		"completion token kind=TOKEN_KIND_STRING_LITERAL\n" +
		"expectedTypesSignatures={Ljava.lang.String;}\n" +
		"expectedTypesKeys={Ljava/lang/String;}\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0119() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0119/X.java",
		"package test0119;\n" +
		"public class X {\n" +
		"  String s = \"ZZZZ\";\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("\"ZZZZ\"");
	int tokenEnd = tokenStart + "\"ZZZZ\"".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_STRING_LITERAL\n" +
		"expectedTypesSignatures={Ljava.lang.String;}\n" +
		"expectedTypesKeys={Ljava/lang/String;}\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0120() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0120/X.java",
		"package test0120;\n" +
		"public class X {\n" +
		"  String s = \"ZZZZ\";\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("\"ZZZZ\"");
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("\"ZZZZ") + "".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures={Ljava.lang.String;}\n" +
		"expectedTypesKeys={Ljava/lang/String;}\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0121() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0121/X.java",
		"package test0121;\n" +
		"public class X {\n" +
		"  String s = \"ZZZZ\";\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int cursorLocation = str.lastIndexOf("\"ZZZZ\"") + "\"ZZZZ\"".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=[-1, -1]\n" +
		"completion token=null\n" +
		"completion token kind=TOKEN_KIND_UNKNOWN\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0122() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0122/X.java",
		"package test0122;\n" +
		"public class X {\n" +
		"  String s = \"\";\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("\"\"");
	int tokenEnd = tokenStart + "\"\"".length() - 1;
	int cursorLocation = str.lastIndexOf("\"\"") + "\"".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_STRING_LITERAL\n" +
		"expectedTypesSignatures={Ljava.lang.String;}\n" +
		"expectedTypesKeys={Ljava/lang/String;}\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0123() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0123/X.java",
		"package test0123;\n" +
		"public class X {\n" +
		"  String s = \"ZZZZ\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("\"ZZZZ");
	int tokenEnd = tokenStart + "\"ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_STRING_LITERAL\n" +
		"expectedTypesSignatures={Ljava.lang.String;}\n" +
		"expectedTypesKeys={Ljava/lang/String;}\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0124() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0124/X.java",
		"package test0124;\n" +
		"public class X {\n" +
		"  String s = \"ZZZZ\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("\"ZZZZ");
	int tokenEnd = tokenStart + "\"ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZ\"\n" +
		"completion token kind=TOKEN_KIND_STRING_LITERAL\n" +
		"expectedTypesSignatures={Ljava.lang.String;}\n" +
		"expectedTypesKeys={Ljava/lang/String;}\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0125() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0125/X.java",
		"package test0125;\n" +
		"public class X {\n" +
		"  String s = \"ZZZZ\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("\"ZZZZ");
	int tokenEnd = tokenStart + "\"ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_STRING_LITERAL\n" +
		"expectedTypesSignatures={Ljava.lang.String;}\n" +
		"expectedTypesKeys={Ljava/lang/String;}\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0126() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0126/X.java",
		"package test0126;\n" +
		"public class X {\n" +
		"  String s = \"ZZZZ\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("\"ZZZZ");
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("\"ZZZZ") + "".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures={Ljava.lang.String;}\n" +
		"expectedTypesKeys={Ljava/lang/String;}\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0127() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0127/X.java",
		"package test0127;\n" +
		"public class X {\n" +
		"  String s = \"\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("\"");
	int tokenEnd = tokenStart + "\"".length() - 1;
	int cursorLocation = str.lastIndexOf("\"") + "\"".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_STRING_LITERAL\n" +
		"expectedTypesSignatures={Ljava.lang.String;}\n" +
		"expectedTypesKeys={Ljava/lang/String;}\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0128() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0127/X.java",
		"package test0127;\n" +
		"public class X {\n" +
		"  String s0 = \"\n" +
		"  String s = \"ZZZZ\"\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("\"ZZZZ\"");
	int tokenEnd = tokenStart + "\"ZZZZ\"".length() - 1;
	int cursorLocation = str.lastIndexOf("\"ZZZZ\"") + "\"ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_STRING_LITERAL\n" +
		"expectedTypesSignatures={Ljava.lang.String;}\n" +
		"expectedTypesKeys={Ljava/lang/String;}\n"+
		"completion token location=UNKNOWN",
		result.context);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=202467
public void test0129() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("public");
	int tokenEnd = tokenStart + "public".length() - 1;
	int cursorLocation = str.lastIndexOf("public") + "public".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"public\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=202467
public void test0130() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"  ZZZZ\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location={MEMBER_START}",
		result.context);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=202467
public void test0131() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"  public ZZZZ\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=202467
public void test0132() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"  yyyy.ZZZZ\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=202467
public void test0133() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"  java.ZZZZ\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=202467
public void test0134() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"  ZZZZ foo(\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location={MEMBER_START}",
		result.context);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=202467
public void test0135() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"  public void foo() {\n" +
		"    ZZZZ\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location={STATEMENT_START}",
		result.context);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=202467
// enable this test when this case will be supported
public void test0136() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"  public void foo() {\n" +
		"    final ZZZZ\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=202467
public void test0137() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"  public void foo() {\n" +
		"    yyyy.ZZZZ\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=202467
public void test0138() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"  public void foo() {\n" +
		"    java.ZZZZ\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=202467
public void test0139() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"  public void foo() {\n" +
		"    bar(ZZZZ\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=202467
public void test0140() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"  public void foo() {\n" +
		"    class Y {\n" +
		"      ZZZZ\n" +
		"    }\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location={MEMBER_START}",
		result.context);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=202467
public void test0141() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"  public void foo() {\n" +
		"    new Object() {\n" +
		"      ZZZZ\n" +
		"    };\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location={MEMBER_START}",
		result.context);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=202467
public void test0142() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"  {\n" +
		"    ZZZZ\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("ZZZZ");
	int tokenEnd = tokenStart + "ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location={STATEMENT_START}",
		result.context);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=202470
public void test0143() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"zzzz\n" +
		"public class X {\n" +
		"  public int field0;\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("zzzz");
	int tokenEnd = tokenStart + "zzzz".length() - 1;
	int cursorLocation = str.lastIndexOf("zzzz") + "zzzz".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, false, true);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"zzzz\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n" +
		"completion token location=UNKNOWN\n" +
		"visibleElements={}",
		result.context);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=202470
public void test0144() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"  zzzz\n" +
		"  public int field0;\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("zzzz");
	int tokenEnd = tokenStart + "zzzz".length() - 1;
	int cursorLocation = str.lastIndexOf("zzzz") + "zzzz".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, false, true);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"zzzz\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n" +
		"completion token location={MEMBER_START}\n" +
		"visibleElements={}",
		result.context);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=202470
public void test0145() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"  public int field0;\n" +
		"  public int field1 = zzzz;\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("zzzz");
	int tokenEnd = tokenStart + "zzzz".length() - 1;
	int cursorLocation = str.lastIndexOf("zzzz") + "zzzz".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, false, true);

	String jclPath = getExternalJCLPathString();
	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"zzzz\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures={I}\n" +
		"expectedTypesKeys={I}\n" +
		"completion token location=UNKNOWN\n" +
		"visibleElements={\n" +
		"	field0 {key=Ltest/X;.field0)I} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]],\n" +
		"	wait(long, int) {key=Ljava/lang/Object;.wait(JI)V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	wait(long) {key=Ljava/lang/Object;.wait(J)V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	wait() {key=Ljava/lang/Object;.wait()V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	toString() {key=Ljava/lang/Object;.toString()Ljava/lang/String;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	notifyAll() {key=Ljava/lang/Object;.notifyAll()V|Ljava/lang/IllegalMonitorStateException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	notify() {key=Ljava/lang/Object;.notify()V|Ljava/lang/IllegalMonitorStateException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	hashCode() {key=Ljava/lang/Object;.hashCode()I} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	getClass() {key=Ljava/lang/Object;.getClass()Ljava/lang/Class;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	finalize() {key=Ljava/lang/Object;.finalize()V|Ljava/lang/Throwable;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	equals(java.lang.Object) {key=Ljava/lang/Object;.equals(Ljava/lang/Object;)Z} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	clone() {key=Ljava/lang/Object;.clone()Ljava/lang/Object;|Ljava/lang/CloneNotSupportedException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"}",
		result.context);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=202470
public void test0146() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"  public int field0;\n" +
		"  { zzzz }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("zzzz");
	int tokenEnd = tokenStart + "zzzz".length() - 1;
	int cursorLocation = str.lastIndexOf("zzzz") + "zzzz".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, false, true);

	String jclPath = getExternalJCLPathString();
	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"zzzz\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n" +
		"completion token location={STATEMENT_START}\n" +
		"visibleElements={\n" +
		"	field0 {key=Ltest/X;.field0)I} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]],\n" +
		"	wait(long, int) {key=Ljava/lang/Object;.wait(JI)V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	wait(long) {key=Ljava/lang/Object;.wait(J)V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	wait() {key=Ljava/lang/Object;.wait()V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	toString() {key=Ljava/lang/Object;.toString()Ljava/lang/String;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	notifyAll() {key=Ljava/lang/Object;.notifyAll()V|Ljava/lang/IllegalMonitorStateException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	notify() {key=Ljava/lang/Object;.notify()V|Ljava/lang/IllegalMonitorStateException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	hashCode() {key=Ljava/lang/Object;.hashCode()I} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	getClass() {key=Ljava/lang/Object;.getClass()Ljava/lang/Class;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	finalize() {key=Ljava/lang/Object;.finalize()V|Ljava/lang/Throwable;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	equals(java.lang.Object) {key=Ljava/lang/Object;.equals(Ljava/lang/Object;)Z} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	clone() {key=Ljava/lang/Object;.clone()Ljava/lang/Object;|Ljava/lang/CloneNotSupportedException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"}",
		result.context);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=202470
public void test0147() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"  public int field0;\n" +
		"  public void foo() {\n" +
		"    zzzz\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("zzzz");
	int tokenEnd = tokenStart + "zzzz".length() - 1;
	int cursorLocation = str.lastIndexOf("zzzz") + "zzzz".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, false, true);

	String jclPath = getExternalJCLPathString();
	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"zzzz\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n" +
		"completion token location={STATEMENT_START}\n" +
		"visibleElements={\n" +
		"	field0 {key=Ltest/X;.field0)I} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]],\n" +
		"	foo() {key=Ltest/X;.foo()V} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]],\n" +
		"	wait(long, int) {key=Ljava/lang/Object;.wait(JI)V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	wait(long) {key=Ljava/lang/Object;.wait(J)V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	wait() {key=Ljava/lang/Object;.wait()V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	toString() {key=Ljava/lang/Object;.toString()Ljava/lang/String;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	notifyAll() {key=Ljava/lang/Object;.notifyAll()V|Ljava/lang/IllegalMonitorStateException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	notify() {key=Ljava/lang/Object;.notify()V|Ljava/lang/IllegalMonitorStateException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	hashCode() {key=Ljava/lang/Object;.hashCode()I} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	getClass() {key=Ljava/lang/Object;.getClass()Ljava/lang/Class;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	finalize() {key=Ljava/lang/Object;.finalize()V|Ljava/lang/Throwable;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	equals(java.lang.Object) {key=Ljava/lang/Object;.equals(Ljava/lang/Object;)Z} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	clone() {key=Ljava/lang/Object;.clone()Ljava/lang/Object;|Ljava/lang/CloneNotSupportedException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"}",
		result.context);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=202470
public void test0148() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"  public int field0;\n" +
		"  public int fieldX0;\n" +
		"  public class Y {\n" +
		"    public int field0;\n" +
		"    public int fieldY0;\n" +
		"    public void fooY() {\n" +
		"      int local0;\n" +
		"      int localfooY0;\n" +
		"      if (true) {\n" +
		"        int local0;\n" +
		"        int localfooY1;\n" +
		"        zzzz\n" +
		"      }\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("zzzz");
	int tokenEnd = tokenStart + "zzzz".length() - 1;
	int cursorLocation = str.lastIndexOf("zzzz") + "zzzz".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, false, true);

	String jclPath = getExternalJCLPathString();
	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"zzzz\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n" +
		"completion token location={STATEMENT_START}\n" +
		"visibleElements={\n" +
		"	local0 [in fooY() [in Y [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]]]],\n" +
		"	localfooY0 [in fooY() [in Y [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]]]],\n" +
		"	localfooY1 [in fooY() [in Y [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]]]],\n" +
		"	fieldY0 {key=Ltest/X$Y;.fieldY0)I} [in Y [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]]],\n" +
		"	field0 {key=Ltest/X$Y;.field0)I} [in Y [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]]],\n" +
		"	fieldX0 {key=Ltest/X;.fieldX0)I} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]],\n" +
		"	fooY() {key=Ltest/X$Y;.fooY()V} [in Y [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]]],\n" +
		"	wait(long, int) {key=Ljava/lang/Object;.wait(JI)V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	wait(long) {key=Ljava/lang/Object;.wait(J)V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	wait() {key=Ljava/lang/Object;.wait()V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	toString() {key=Ljava/lang/Object;.toString()Ljava/lang/String;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	notifyAll() {key=Ljava/lang/Object;.notifyAll()V|Ljava/lang/IllegalMonitorStateException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	notify() {key=Ljava/lang/Object;.notify()V|Ljava/lang/IllegalMonitorStateException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	hashCode() {key=Ljava/lang/Object;.hashCode()I} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	getClass() {key=Ljava/lang/Object;.getClass()Ljava/lang/Class;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	finalize() {key=Ljava/lang/Object;.finalize()V|Ljava/lang/Throwable;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	equals(java.lang.Object) {key=Ljava/lang/Object;.equals(Ljava/lang/Object;)Z} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	clone() {key=Ljava/lang/Object;.clone()Ljava/lang/Object;|Ljava/lang/CloneNotSupportedException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"}",
		result.context);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=202470
public void test0149() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"  public void method0() {}\n" +
		"  public void method0(int i) {}\n" +
		"  public class Y {\n" +
		"    public void method1() {}\n" +
		"    public void method0(int i) {}\n" +
		"    public void foo() {\n" +
		"      zzzz\n" +
		"    }\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("zzzz");
	int tokenEnd = tokenStart + "zzzz".length() - 1;
	int cursorLocation = str.lastIndexOf("zzzz") + "zzzz".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, false, true);

	String jclPath = getExternalJCLPathString();
	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"zzzz\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n" +
		"completion token location={STATEMENT_START}\n" +
		"visibleElements={\n" +
		"	method1() {key=Ltest/X$Y;.method1()V} [in Y [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]]],\n" +
		"	method0(int) {key=Ltest/X$Y;.method0(I)V} [in Y [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]]],\n" +
		"	foo() {key=Ltest/X$Y;.foo()V} [in Y [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]]],\n" +
		"	wait(long, int) {key=Ljava/lang/Object;.wait(JI)V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	wait(long) {key=Ljava/lang/Object;.wait(J)V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	wait() {key=Ljava/lang/Object;.wait()V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	toString() {key=Ljava/lang/Object;.toString()Ljava/lang/String;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	notifyAll() {key=Ljava/lang/Object;.notifyAll()V|Ljava/lang/IllegalMonitorStateException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	notify() {key=Ljava/lang/Object;.notify()V|Ljava/lang/IllegalMonitorStateException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	hashCode() {key=Ljava/lang/Object;.hashCode()I} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	getClass() {key=Ljava/lang/Object;.getClass()Ljava/lang/Class;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	finalize() {key=Ljava/lang/Object;.finalize()V|Ljava/lang/Throwable;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	equals(java.lang.Object) {key=Ljava/lang/Object;.equals(Ljava/lang/Object;)Z} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	clone() {key=Ljava/lang/Object;.clone()Ljava/lang/Object;|Ljava/lang/CloneNotSupportedException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	method0() {key=Ltest/X;.method0()V} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]],\n" +
		"}",
		result.context);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=202470
public void test0150() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X extends A {\n" +
		"  public void method0() {}\n" +
		"  public void method0(int i) {}\n" +
		"  public class Y extends B {\n" +
		"    public void method1() {}\n" +
		"    public void method0(int i) {}\n" +
		"    public void foo() {\n" +
		"      zzzz\n" +
		"    }\n" +
		"  }\n" +
		"}");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src3/test/A.java",
		"package test;\n" +
		"public class A {\n" +
		"  public void methodA() {}\n" +
		"  public void method0(int i) {}\n" +
		"}");

	this.workingCopies[2] = getWorkingCopy(
		"/Completion/src3/test/B.java",
		"package test;\n" +
		"public class B {\n" +
		"  public void methodB() {}\n" +
		"  public void method0(int i) {}\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("zzzz");
	int tokenEnd = tokenStart + "zzzz".length() - 1;
	int cursorLocation = str.lastIndexOf("zzzz") + "zzzz".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, false, true);

	String jclPath = getExternalJCLPathString();
	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"zzzz\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n" +
		"completion token location={STATEMENT_START}\n" +
		"visibleElements={\n" +
		"	method1() {key=Ltest/X$Y;.method1()V} [in Y [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]]],\n" +
		"	method0(int) {key=Ltest/X$Y;.method0(I)V} [in Y [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]]],\n" +
		"	foo() {key=Ltest/X$Y;.foo()V} [in Y [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]]],\n" +
		"	methodB() {key=Ltest/B;.methodB()V} [in B [in [Working copy] B.java [in test [in src3 [in Completion]]]]],\n" +
		"	wait(long, int) {key=Ljava/lang/Object;.wait(JI)V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	wait(long) {key=Ljava/lang/Object;.wait(J)V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	wait() {key=Ljava/lang/Object;.wait()V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	toString() {key=Ljava/lang/Object;.toString()Ljava/lang/String;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	notifyAll() {key=Ljava/lang/Object;.notifyAll()V|Ljava/lang/IllegalMonitorStateException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	notify() {key=Ljava/lang/Object;.notify()V|Ljava/lang/IllegalMonitorStateException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	hashCode() {key=Ljava/lang/Object;.hashCode()I} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	getClass() {key=Ljava/lang/Object;.getClass()Ljava/lang/Class;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	finalize() {key=Ljava/lang/Object;.finalize()V|Ljava/lang/Throwable;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	equals(java.lang.Object) {key=Ljava/lang/Object;.equals(Ljava/lang/Object;)Z} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	clone() {key=Ljava/lang/Object;.clone()Ljava/lang/Object;|Ljava/lang/CloneNotSupportedException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	method0() {key=Ltest/X;.method0()V} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]],\n" +
		"	methodA() {key=Ltest/A;.methodA()V} [in A [in [Working copy] A.java [in test [in src3 [in Completion]]]]],\n" +
		"}",
		result.context);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=202470
public void test0151() throws CoreException, IOException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X extends A {\n" +
		"  public void method0() {}\n" +
		"  public void method0(int i) {}\n" +
		"  public class Y extends B {\n" +
		"    public void method1() {}\n" +
		"    public void method0(int i) {}\n" +
		"    public void foo() {\n" +
		"      zzzz\n" +
		"    }\n" +
		"  }\n" +
		"}");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src3/test/B.java",
		"package test;\n" +
		"public class B extends D {\n" +
		"  public void methodB() {}\n" +
		"  public void method0(int i) {}\n" +
		"}");

	this.workingCopies[2] = getWorkingCopy(
		"/Completion/src3/test/D.java",
		"package test;\n" +
		"public class D {\n" +
		"  public void methodD() {}\n" +
		"  public void method0(int i) {}\n" +
		"}");

	this.addLibrary(
		getJavaProject("Completion"),
		"test.jar",
		"testsrc.zip",
		new String[] {
			"/Completion/test/A.java",
			"package test;\n" +
			"public class A extends C {\n" +
			"  public void methodA() {}\n" +
			"  public void method0(int i) {}\n" +
			"}",
			"/Completion/test/C.java",
			"package test;\n" +
			"public class C {\n" +
			"  public void methodC() {}\n" +
			"  public void method0(int i) {}\n" +
			"}"
		},
		"1.4");


	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("zzzz");
	int tokenEnd = tokenStart + "zzzz".length() - 1;
	int cursorLocation = str.lastIndexOf("zzzz") + "zzzz".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, false, true);

	String jclPath = getExternalJCLPathString();
	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"zzzz\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n" +
		"completion token location={STATEMENT_START}\n" +
		"visibleElements={\n" +
		"	method1() {key=Ltest/X$Y;.method1()V} [in Y [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]]],\n" +
		"	method0(int) {key=Ltest/X$Y;.method0(I)V} [in Y [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]]],\n" +
		"	foo() {key=Ltest/X$Y;.foo()V} [in Y [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]]],\n" +
		"	methodB() {key=Ltest/B;.methodB()V} [in B [in [Working copy] B.java [in test [in src3 [in Completion]]]]],\n" +
		"	methodD() {key=Ltest/D;.methodD()V} [in D [in [Working copy] D.java [in test [in src3 [in Completion]]]]],\n" +
		"	wait(long, int) {key=Ljava/lang/Object;.wait(JI)V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	wait(long) {key=Ljava/lang/Object;.wait(J)V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	wait() {key=Ljava/lang/Object;.wait()V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	toString() {key=Ljava/lang/Object;.toString()Ljava/lang/String;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	notifyAll() {key=Ljava/lang/Object;.notifyAll()V|Ljava/lang/IllegalMonitorStateException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	notify() {key=Ljava/lang/Object;.notify()V|Ljava/lang/IllegalMonitorStateException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	hashCode() {key=Ljava/lang/Object;.hashCode()I} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	getClass() {key=Ljava/lang/Object;.getClass()Ljava/lang/Class;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	finalize() {key=Ljava/lang/Object;.finalize()V|Ljava/lang/Throwable;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	equals(java.lang.Object) {key=Ljava/lang/Object;.equals(Ljava/lang/Object;)Z} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	clone() {key=Ljava/lang/Object;.clone()Ljava/lang/Object;|Ljava/lang/CloneNotSupportedException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	method0() {key=Ltest/X;.method0()V} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]],\n" +
		"	methodA() {key=Ltest/A;.methodA()V} [in A [in A.class [in test [in test.jar [in Completion]]]]],\n" +
		"	methodC() {key=Ltest/C;.methodC()V} [in C [in C.class [in test [in test.jar [in Completion]]]]],\n" +
		"}",
		result.context);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=202470
public void test0152() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"  public void method0() {}\n" +
		"  public void method0(int i) {}\n" +
		"  public class Y {\n" +
		"    public void method1() {}\n" +
		"    public void method0(int i) {}\n" +
		"    public void foo() {\n" +
		"      zzzz\n" +
		"    }\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int cursorLocation = str.lastIndexOf("zzzz") + "zzzz".length();

	boolean unsupported = false;
	try {
		contextComplete0(this.workingCopies[0], cursorLocation, false /*do not use extended context*/,true /*ask enclosing element*/, true /*ask visible elements*/, null);
	} catch (UnsupportedOperationException e) {
		// this is expected because visible elements computation require heavy context
		unsupported = true;
	}
	assertTrue("getVisibleElements() shouldn't be supported", unsupported);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=202470
public void test0153() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"  public A methodX() {return null;}\n" +
		"  public void foo() {\n" +
		"    zzzz\n" +
		"  }\n" +
		"}");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src3/test/A.java",
		"package test;\n" +
		"public class A {\n" +
		"}");


	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("zzzz");
	int tokenEnd = tokenStart + "zzzz".length() - 1;
	int cursorLocation = str.lastIndexOf("zzzz") + "zzzz".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, false, true, "Ltest/A;");

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"zzzz\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n" +
		"completion token location={STATEMENT_START}\n" +
		"visibleElements={\n" +
		"	methodX() {key=Ltest/X;.methodX()Ltest/A;} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]],\n" +
		"}",
		result.context);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=202470
public void test0154() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"  public A methodX() {return null;}\n" +
		"  public void foo() {\n" +
		"    zzzz\n" +
		"  }\n" +
		"}");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src3/test/A.java",
		"package test;\n" +
		"public class A {\n" +
		"}");

	this.workingCopies[2] = getWorkingCopy(
		"/Completion/src3/test/B.java",
		"package test;\n" +
		"public class B {\n" +
		"}");


	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("zzzz");
	int tokenEnd = tokenStart + "zzzz".length() - 1;
	int cursorLocation = str.lastIndexOf("zzzz") + "zzzz".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, false, true, "Ltest/B;");

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"zzzz\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n" +
		"completion token location={STATEMENT_START}\n" +
		"visibleElements={}",
		result.context);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=202470
public void test0155() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"  public A methodX() {return null;}\n" +
		"  public void foo() {\n" +
		"    zzzz\n" +
		"  }\n" +
		"}");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src3/test/A.java",
		"package test;\n" +
		"public class A extends B {\n" +
		"}");

	this.workingCopies[2] = getWorkingCopy(
		"/Completion/src3/test/B.java",
		"package test;\n" +
		"public class B {\n" +
		"}");


	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("zzzz");
	int tokenEnd = tokenStart + "zzzz".length() - 1;
	int cursorLocation = str.lastIndexOf("zzzz") + "zzzz".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, false, true, "Ltest/B;");

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"zzzz\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n" +
		"completion token location={STATEMENT_START}\n" +
		"visibleElements={\n" +
		"	methodX() {key=Ltest/X;.methodX()Ltest/A;} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]],\n" +
		"}",
		result.context);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=202470
public void test0156() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"  public A methodX() {return null;}\n" +
		"  public void foo() {\n" +
		"    zzzz\n" +
		"  }\n" +
		"}");


	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("zzzz");
	int tokenEnd = tokenStart + "zzzz".length() - 1;
	int cursorLocation = str.lastIndexOf("zzzz") + "zzzz".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, false, true, "Ltest/Zork;");

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"zzzz\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n" +
		"completion token location={STATEMENT_START}\n" +
		"visibleElements={}",
		result.context);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=202470
public void test0157() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"  public test.Zork methodX() {return null;}\n" +
		"  public void foo() {\n" +
		"    zzzz\n" +
		"  }\n" +
		"}");


	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("zzzz");
	int tokenEnd = tokenStart + "zzzz".length() - 1;
	int cursorLocation = str.lastIndexOf("zzzz") + "zzzz".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, false, true, "Ltest/Zork;");

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"zzzz\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n" +
		"completion token location={STATEMENT_START}\n" +
		"visibleElements={\n" +
		"	methodX() {key=Ltest/X;.methodX()Ltest/Zork;} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]],\n" +
		"}",
		result.context);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=202470
public void test0158() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"  public void foo() {\n" +
		"    zzzz\n" +
		"  }\n" +
		"}");


	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("zzzz");
	int tokenEnd = tokenStart + "zzzz".length() - 1;
	int cursorLocation = str.lastIndexOf("zzzz") + "zzzz".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, true, false);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"zzzz\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n" +
		"completion token location={STATEMENT_START}\n" +
		"enclosingElement=foo() {key=Ltest/X;.foo()V} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]]",
		result.context);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=202470
public void test0159() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"  public void foo() {}\n" +
		"  zzzz\n" +
		"  public void bar() {}\n" +
		"}");


	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("zzzz");
	int tokenEnd = tokenStart + "zzzz".length() - 1;
	int cursorLocation = str.lastIndexOf("zzzz") + "zzzz".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, true, false);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"zzzz\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n" +
		"completion token location={MEMBER_START}\n" +
		"enclosingElement=X {key=Ltest/X;} [in [Working copy] X.java [in test [in src3 [in Completion]]]]",
		result.context);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=202470
public void test0160() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"zzzz\n" +
		"public class X {\n" +
		"}");


	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("zzzz");
	int tokenEnd = tokenStart + "zzzz".length() - 1;
	int cursorLocation = str.lastIndexOf("zzzz") + "zzzz".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, true, false);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"zzzz\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n" +
		"completion token location=UNKNOWN\n" +
		"enclosingElement=[Working copy] X.java [in test [in src3 [in Completion]]]",
		result.context);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=202470
public void test0161() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"  public void foo() {}\n" +
		"  public class Y {\n" +
		"    public void fooY() {}\n" +
		"      zzzz\n" +
		"    public void barY() {}\n" +
		"  }\n" +
		"  public void bar() {}\n" +
		"}");


	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("zzzz");
	int tokenEnd = tokenStart + "zzzz".length() - 1;
	int cursorLocation = str.lastIndexOf("zzzz") + "zzzz".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, true, false);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"zzzz\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n" +
		"completion token location={MEMBER_START}\n" +
		"enclosingElement=Y {key=Ltest/X$Y;} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]]",
		result.context);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=226673
public void test0162() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"  public A methodX() {return null;}\n" +
		"  public void foo() {\n" +
		"    zzzz\n" +
		"  }\n" +
		"}");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src3/test/A.java",
		"package test;\n" +
		"public class A {\n" +
		"}");


	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("zzzz");
	int tokenEnd = tokenStart + "zzzz".length() - 1;
	int cursorLocation = str.lastIndexOf("zzzz") + "zzzz".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, false, true, "Ltest.A;");

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"zzzz\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n" +
		"completion token location={STATEMENT_START}\n" +
		"visibleElements={\n" +
		"	methodX() {key=Ltest/X;.methodX()Ltest/A;} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]],\n" +
		"}",
		result.context);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=230885
public void test0163() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"public class Foo  {\n"+
		"\n"+
		"        private void addDepencency(int source, int target, int depth) {\n"+
		"        }\n"+
		"\n"+
		"        private void addDataDependencies(int source) {\n"+
		"                addD/**/\n"+
		"        }\n"+
		"\n"+
		"        private void addDataDependencies(int source) {\n"+
		"        }\n"+
		"}");


	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("addD/**/");
	int tokenEnd = tokenStart + "addD".length() - 1;
	int cursorLocation = str.lastIndexOf("addD/**/") + "addD".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, false, true, "I");

	String jclPath = getExternalJCLPathString();
	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"addD\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n" +
		"completion token location={STATEMENT_START}\n" +
		"visibleElements={\n" +
		"	source [in addDataDependencies(int) [in Foo [in [Working copy] X.java [in test [in src3 [in Completion]]]]]],\n" +
		"	hashCode() {key=Ljava/lang/Object;.hashCode()I} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"}",
		result.context);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=230885
public void test0164() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"public class Foo  {\n"+
		"\n"+
		"        private void addDepencency(int source, int target, int depth) {\n"+
		"        }\n"+
		"\n"+
		"        private int addDataDependencies(int source) {\n"+
		"        }\n"+
		"\n"+
		"        private int addDataDependencies(int source) {\n"+
		"                addD/**/\n"+
		"        }\n"+
		"\n"+
		"        private int addDataDependencies(int source) {\n"+
		"        }\n"+
		"}");


	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("addD/**/");
	int tokenEnd = tokenStart + "addD".length() - 1;
	int cursorLocation = str.lastIndexOf("addD/**/") + "addD".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, false, true, "I");

	String jclPath = getExternalJCLPathString();
	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"addD\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n" +
		"completion token location={STATEMENT_START}\n" +
		"visibleElements={\n" +
		"	source [in addDataDependencies(int)#2 [in Foo [in [Working copy] X.java [in test [in src3 [in Completion]]]]]],\n" +
		"	addDataDependencies(int) {key=Ltest/X~Foo;.addDataDependencies(I)I} [in Foo [in [Working copy] X.java [in test [in src3 [in Completion]]]]],\n" +
		"	hashCode() {key=Ljava/lang/Object;.hashCode()I} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"}",
		result.context);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=231747
public void test0165() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"  class Y {\n" +
		"  }\n" +
		"  class Y {\n" +
		"    int var;\n" +
		"    void foo() {\n" +
		"      var\n" +
		"    }\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = -1;
	int tokenEnd = -1;
	int cursorLocation = str.lastIndexOf("var") + "var".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, false, true, "I");

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=null\n" +
		"completion token kind=TOKEN_KIND_UNKNOWN\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n" +
		"completion token location=UNKNOWN\n" +
		"visibleElements={}",
		result.context);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=243023
public void test0166() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0001/X.java",
		"package test0001;\n" +
		"public class X {\n" +
		"  public void foo() {\n" +
		"    if ( null == /**/\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("/**/");
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("/**/") + "".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=236306
public void test0167() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"import java.text.MessageFormat;\n" +
		"public class X {\n" +
		"  public void foo() {\n" +
		"    String fooBar = \"Hi\";" +
		"	 String furchtbar= MessageFormat.format\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("format");
	int tokenEnd = tokenStart + "format".length() - 1;
	int cursorLocation = str.lastIndexOf("format") + "format".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, true, true);
	String jclPath = getExternalJCLPathString();
	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"format\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures={Ljava.lang.String;}\n" +
		"expectedTypesKeys={Ljava/lang/String;}\n" +
		"completion token location=UNKNOWN\n" +
		"enclosingElement=foo() {key=Ltest/X;.foo()V} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]]\n" +
		"visibleElements={\n" +
		"	fooBar [in foo() [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]]],\n" +
		"	foo() {key=Ltest/X;.foo()V} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]],\n" +
		"	wait(long, int) {key=Ljava/lang/Object;.wait(JI)V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	wait(long) {key=Ljava/lang/Object;.wait(J)V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	wait() {key=Ljava/lang/Object;.wait()V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	toString() {key=Ljava/lang/Object;.toString()Ljava/lang/String;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	notifyAll() {key=Ljava/lang/Object;.notifyAll()V|Ljava/lang/IllegalMonitorStateException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	notify() {key=Ljava/lang/Object;.notify()V|Ljava/lang/IllegalMonitorStateException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	hashCode() {key=Ljava/lang/Object;.hashCode()I} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	getClass() {key=Ljava/lang/Object;.getClass()Ljava/lang/Class;} [in Object [in Object.class [in java.lang [in " + jclPath +"]]]],\n" +
		"	finalize() {key=Ljava/lang/Object;.finalize()V|Ljava/lang/Throwable;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	equals(java.lang.Object) {key=Ljava/lang/Object;.equals(Ljava/lang/Object;)Z} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	clone() {key=Ljava/lang/Object;.clone()Ljava/lang/Object;|Ljava/lang/CloneNotSupportedException;} [in Object [in Object.class [in java.lang [in " + jclPath +"]]]],\n" +
		"}",
		result.context);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=236306
public void test0168() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"import java.text.MessageFormat;\n" +
		"public class X {\n" +
		"  public String format(String str, Object... args) {\n" +
		"	 return \"hello\";\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"    String fooBar = \"Hi\";" +
		"	 String furchtbar= this.format\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("format");
	int tokenEnd = tokenStart + "format".length() - 1;
	int cursorLocation = str.lastIndexOf("format") + "format".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, true, true);
	String jclPath = getExternalJCLPathString();
	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"format\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures={Ljava.lang.String;}\n" +
		"expectedTypesKeys={Ljava/lang/String;}\n" +
		"completion token location=UNKNOWN\n" +
		"enclosingElement=foo() {key=Ltest/X;.foo()V} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]]\n" +
		"visibleElements={\n" +
		"	fooBar [in foo() [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]]],\n" +
		"	format(String, Object[]) {key=Ltest/X;.format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]],\n" +
		"	foo() {key=Ltest/X;.foo()V} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]],\n" +
		"	wait(long, int) {key=Ljava/lang/Object;.wait(JI)V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	wait(long) {key=Ljava/lang/Object;.wait(J)V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	wait() {key=Ljava/lang/Object;.wait()V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	toString() {key=Ljava/lang/Object;.toString()Ljava/lang/String;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	notifyAll() {key=Ljava/lang/Object;.notifyAll()V|Ljava/lang/IllegalMonitorStateException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	notify() {key=Ljava/lang/Object;.notify()V|Ljava/lang/IllegalMonitorStateException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	hashCode() {key=Ljava/lang/Object;.hashCode()I} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	getClass() {key=Ljava/lang/Object;.getClass()Ljava/lang/Class;} [in Object [in Object.class [in java.lang [in " + jclPath +"]]]],\n" +
		"	finalize() {key=Ljava/lang/Object;.finalize()V|Ljava/lang/Throwable;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	equals(java.lang.Object) {key=Ljava/lang/Object;.equals(Ljava/lang/Object;)Z} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	clone() {key=Ljava/lang/Object;.clone()Ljava/lang/Object;|Ljava/lang/CloneNotSupportedException;} [in Object [in Object.class [in java.lang [in " + jclPath +"]]]],\n" +
		"}",
		result.context);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=236306
public void test0169() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"import java.text.MessageFormat;\n" +
		"public class X {\n" +
		"  public String format(String str, Object... args) {\n" +
		"	 return \"hello\";\n" +
		"  }\n" +
		"  public String methods(String str, String s) {\n" +
		"	 return \"hello\";\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"	 X x = new X();\n" +
		"    String fooBar = \"Hi\";\n" +
		"	 String furchtbar= x.methods(fooBar, format\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("format");
	int tokenEnd = tokenStart + "format".length() - 1;
	int cursorLocation = str.lastIndexOf("format") + "format".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, true, true);
	String jclPath = getExternalJCLPathString();
	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"format\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures={Ljava.lang.String;}\n" +
		"expectedTypesKeys={Ljava/lang/String;}\n" +
		"completion token location=UNKNOWN\n" +
		"enclosingElement=foo() {key=Ltest/X;.foo()V} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]]\n" +
		"visibleElements={\n" +
		"	x [in foo() [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]]],\n" +
		"	fooBar [in foo() [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]]],\n" +
		"	methods(String, String) {key=Ltest/X;.methods(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]],\n" +
		"	format(String, Object[]) {key=Ltest/X;.format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]],\n" +
		"	foo() {key=Ltest/X;.foo()V} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]],\n" +
		"	wait(long, int) {key=Ljava/lang/Object;.wait(JI)V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	wait(long) {key=Ljava/lang/Object;.wait(J)V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	wait() {key=Ljava/lang/Object;.wait()V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	toString() {key=Ljava/lang/Object;.toString()Ljava/lang/String;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	notifyAll() {key=Ljava/lang/Object;.notifyAll()V|Ljava/lang/IllegalMonitorStateException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	notify() {key=Ljava/lang/Object;.notify()V|Ljava/lang/IllegalMonitorStateException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	hashCode() {key=Ljava/lang/Object;.hashCode()I} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	getClass() {key=Ljava/lang/Object;.getClass()Ljava/lang/Class;} [in Object [in Object.class [in java.lang [in " + jclPath +"]]]],\n" +
		"	finalize() {key=Ljava/lang/Object;.finalize()V|Ljava/lang/Throwable;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	equals(java.lang.Object) {key=Ljava/lang/Object;.equals(Ljava/lang/Object;)Z} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	clone() {key=Ljava/lang/Object;.clone()Ljava/lang/Object;|Ljava/lang/CloneNotSupportedException;} [in Object [in Object.class [in java.lang [in " + jclPath +"]]]],\n" +
		"}",
		result.context);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=236306
public void test0170() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"  public String format(String str, Object... args) {\n" +
		"	 return \"hello\";\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"    String fooBar = \"Hi\";\n" +
		"	 String furchtbar= format\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("format");
	int tokenEnd = tokenStart + "format".length() - 1;
	int cursorLocation = str.lastIndexOf("format") + "format".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, true, true);
	String jclPath = getExternalJCLPathString();
	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"format\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures={Ljava.lang.String;}\n" +
		"expectedTypesKeys={Ljava/lang/String;}\n" +
		"completion token location=UNKNOWN\n" +
		"enclosingElement=foo() {key=Ltest/X;.foo()V} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]]\n" +
		"visibleElements={\n" +
		"	fooBar [in foo() [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]]],\n" +
		"	format(String, Object[]) {key=Ltest/X;.format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]],\n" +
		"	foo() {key=Ltest/X;.foo()V} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]],\n" +
		"	wait(long, int) {key=Ljava/lang/Object;.wait(JI)V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	wait(long) {key=Ljava/lang/Object;.wait(J)V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	wait() {key=Ljava/lang/Object;.wait()V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	toString() {key=Ljava/lang/Object;.toString()Ljava/lang/String;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	notifyAll() {key=Ljava/lang/Object;.notifyAll()V|Ljava/lang/IllegalMonitorStateException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	notify() {key=Ljava/lang/Object;.notify()V|Ljava/lang/IllegalMonitorStateException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	hashCode() {key=Ljava/lang/Object;.hashCode()I} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	getClass() {key=Ljava/lang/Object;.getClass()Ljava/lang/Class;} [in Object [in Object.class [in java.lang [in " + jclPath +"]]]],\n" +
		"	finalize() {key=Ljava/lang/Object;.finalize()V|Ljava/lang/Throwable;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	equals(java.lang.Object) {key=Ljava/lang/Object;.equals(Ljava/lang/Object;)Z} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	clone() {key=Ljava/lang/Object;.clone()Ljava/lang/Object;|Ljava/lang/CloneNotSupportedException;} [in Object [in Object.class [in java.lang [in " + jclPath +"]]]],\n" +
		"}",
		result.context);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=236306
public void test0171() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"import java.text.MessageFormat;\n" +
		"public class X {\n" +
		"  public String format(String str, Object... args) {\n" +
		"	 return \"hello\";\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"    String fooBar = \"Hi\";\n" +
		"	 String furchtbar= new String(MessageFormat.format\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("format");
	int tokenEnd = tokenStart + "format".length() - 1;
	int cursorLocation = str.lastIndexOf("format") + "format".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, true, true);
	String jclPath = getExternalJCLPathString();
	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"format\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n" +
		"completion token location=UNKNOWN\n" +
		"enclosingElement=foo() {key=Ltest/X;.foo()V} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]]\n" +
		"visibleElements={\n" +
		"	fooBar [in foo() [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]]],\n" +
		"	format(String, Object[]) {key=Ltest/X;.format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]],\n" +
		"	foo() {key=Ltest/X;.foo()V} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]],\n" +
		"	wait(long, int) {key=Ljava/lang/Object;.wait(JI)V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	wait(long) {key=Ljava/lang/Object;.wait(J)V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	wait() {key=Ljava/lang/Object;.wait()V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	toString() {key=Ljava/lang/Object;.toString()Ljava/lang/String;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	notifyAll() {key=Ljava/lang/Object;.notifyAll()V|Ljava/lang/IllegalMonitorStateException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	notify() {key=Ljava/lang/Object;.notify()V|Ljava/lang/IllegalMonitorStateException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	hashCode() {key=Ljava/lang/Object;.hashCode()I} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	getClass() {key=Ljava/lang/Object;.getClass()Ljava/lang/Class;} [in Object [in Object.class [in java.lang [in " + jclPath +"]]]],\n" +
		"	finalize() {key=Ljava/lang/Object;.finalize()V|Ljava/lang/Throwable;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	equals(java.lang.Object) {key=Ljava/lang/Object;.equals(Ljava/lang/Object;)Z} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	clone() {key=Ljava/lang/Object;.clone()Ljava/lang/Object;|Ljava/lang/CloneNotSupportedException;} [in Object [in Object.class [in java.lang [in " + jclPath +"]]]],\n" +
		"}",
		result.context);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=236306
public void test0172() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"import java.text.MessageFormat;\n" +
		"public class X {\n" +
		"  public String format(String str, Object... args) {\n" +
		"	 return \"hello\";\n" +
		"  }\n" +
		"  public void foo() {\n" +
		"    String fooBar = \"Hi\";\n" +
		"	 int i = 1;\n" +
		"	 String furchtbar= (i < 1) ? fooBar : format\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("format");
	int tokenEnd = tokenStart + "format".length() - 1;
	int cursorLocation = str.lastIndexOf("format") + "format".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, true, true);
	String jclPath = getExternalJCLPathString();
	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"format\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures={Ljava.lang.String;}\n" +
		"expectedTypesKeys={Ljava/lang/String;}\n" +
		"completion token location=UNKNOWN\n" +
		"enclosingElement=foo() {key=Ltest/X;.foo()V} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]]\n" +
		"visibleElements={\n" +
		"	fooBar [in foo() [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]]],\n" +
		"	i [in foo() [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]]],\n" +
		"	format(String, Object[]) {key=Ltest/X;.format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]],\n" +
		"	foo() {key=Ltest/X;.foo()V} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]],\n" +
		"	wait(long, int) {key=Ljava/lang/Object;.wait(JI)V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	wait(long) {key=Ljava/lang/Object;.wait(J)V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	wait() {key=Ljava/lang/Object;.wait()V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	toString() {key=Ljava/lang/Object;.toString()Ljava/lang/String;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	notifyAll() {key=Ljava/lang/Object;.notifyAll()V|Ljava/lang/IllegalMonitorStateException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	notify() {key=Ljava/lang/Object;.notify()V|Ljava/lang/IllegalMonitorStateException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	hashCode() {key=Ljava/lang/Object;.hashCode()I} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	getClass() {key=Ljava/lang/Object;.getClass()Ljava/lang/Class;} [in Object [in Object.class [in java.lang [in " + jclPath +"]]]],\n" +
		"	finalize() {key=Ljava/lang/Object;.finalize()V|Ljava/lang/Throwable;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	equals(java.lang.Object) {key=Ljava/lang/Object;.equals(Ljava/lang/Object;)Z} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	clone() {key=Ljava/lang/Object;.clone()Ljava/lang/Object;|Ljava/lang/CloneNotSupportedException;} [in Object [in Object.class [in java.lang [in " + jclPath +"]]]],\n" +
		"}",
		result.context);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=236306
public void test0173() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"import java.text.MessageFormat;\n" +
		"public class X {\n" +
		"  public void foo() {\n" +
		"    String fooBar = \"Hi\";\n" +
		"	 String furchtbar= (fooBar = String.format\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("format");
	int tokenEnd = tokenStart + "format".length() - 1;
	int cursorLocation = str.lastIndexOf("format") + "format".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, true, true);
	String jclPath = getExternalJCLPathString();
	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"format\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures={Ljava.lang.String;}\n" +
		"expectedTypesKeys={Ljava/lang/String;}\n" +
		"completion token location=UNKNOWN\n" +
		"enclosingElement=foo() {key=Ltest/X;.foo()V} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]]\n" +
		"visibleElements={\n" +
		"	fooBar [in foo() [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]]],\n" +
		"	foo() {key=Ltest/X;.foo()V} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]],\n" +
		"	wait(long, int) {key=Ljava/lang/Object;.wait(JI)V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	wait(long) {key=Ljava/lang/Object;.wait(J)V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	wait() {key=Ljava/lang/Object;.wait()V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	toString() {key=Ljava/lang/Object;.toString()Ljava/lang/String;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	notifyAll() {key=Ljava/lang/Object;.notifyAll()V|Ljava/lang/IllegalMonitorStateException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	notify() {key=Ljava/lang/Object;.notify()V|Ljava/lang/IllegalMonitorStateException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	hashCode() {key=Ljava/lang/Object;.hashCode()I} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	getClass() {key=Ljava/lang/Object;.getClass()Ljava/lang/Class;} [in Object [in Object.class [in java.lang [in " + jclPath +"]]]],\n" +
		"	finalize() {key=Ljava/lang/Object;.finalize()V|Ljava/lang/Throwable;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	equals(java.lang.Object) {key=Ljava/lang/Object;.equals(Ljava/lang/Object;)Z} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	clone() {key=Ljava/lang/Object;.clone()Ljava/lang/Object;|Ljava/lang/CloneNotSupportedException;} [in Object [in Object.class [in java.lang [in " + jclPath +"]]]],\n" +
		"}",
		result.context);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=312603
public void test0174() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"import java.text.MessageFormat;\n" +
		"public class X {\n" +
		"    String fooBar = \"Hi\";" +
		"	 String furchtbar= MessageFormat.format\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("format");
	int tokenEnd = tokenStart + "format".length() - 1;
	int cursorLocation = str.lastIndexOf("format") + "format".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, true, true);
	String jclPath = getExternalJCLPathString();
	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"format\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures={Ljava.lang.String;}\n" +
		"expectedTypesKeys={Ljava/lang/String;}\n" +
		"completion token location=UNKNOWN\n" +
		"enclosingElement=furchtbar {key=Ltest/X;.furchtbar)Ljava/lang/String;} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]]\n" +
		"visibleElements={\n" +
		"	fooBar {key=Ltest/X;.fooBar)Ljava/lang/String;} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]],\n" +
		"	wait(long, int) {key=Ljava/lang/Object;.wait(JI)V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	wait(long) {key=Ljava/lang/Object;.wait(J)V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	wait() {key=Ljava/lang/Object;.wait()V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	toString() {key=Ljava/lang/Object;.toString()Ljava/lang/String;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	notifyAll() {key=Ljava/lang/Object;.notifyAll()V|Ljava/lang/IllegalMonitorStateException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	notify() {key=Ljava/lang/Object;.notify()V|Ljava/lang/IllegalMonitorStateException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	hashCode() {key=Ljava/lang/Object;.hashCode()I} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	getClass() {key=Ljava/lang/Object;.getClass()Ljava/lang/Class;} [in Object [in Object.class [in java.lang [in " + jclPath +"]]]],\n" +
		"	finalize() {key=Ljava/lang/Object;.finalize()V|Ljava/lang/Throwable;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	equals(java.lang.Object) {key=Ljava/lang/Object;.equals(Ljava/lang/Object;)Z} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	clone() {key=Ljava/lang/Object;.clone()Ljava/lang/Object;|Ljava/lang/CloneNotSupportedException;} [in Object [in Object.class [in java.lang [in " + jclPath +"]]]],\n" +
		"}",
		result.context);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=312603
public void test0175() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"import java.text.MessageFormat;\n" +
		"public class X {\n" +
		"    String fooBar = \"Hi\";" +
		"	 String furchtbar= new String(String.format;\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("format");
	int tokenEnd = tokenStart + "format".length() - 1;
	int cursorLocation = str.lastIndexOf("format") + "format".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, true, true);
	String jclPath = getExternalJCLPathString();
	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"format\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n" +
		"completion token location=UNKNOWN\n" +
		"enclosingElement=X {key=Ltest/X;} [in [Working copy] X.java [in test [in src3 [in Completion]]]]\n" +
		"visibleElements={\n" +
		"	fooBar {key=Ltest/X;.fooBar)Ljava/lang/String;} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]],\n" +
		"	wait(long, int) {key=Ljava/lang/Object;.wait(JI)V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	wait(long) {key=Ljava/lang/Object;.wait(J)V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	wait() {key=Ljava/lang/Object;.wait()V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	toString() {key=Ljava/lang/Object;.toString()Ljava/lang/String;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	notifyAll() {key=Ljava/lang/Object;.notifyAll()V|Ljava/lang/IllegalMonitorStateException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	notify() {key=Ljava/lang/Object;.notify()V|Ljava/lang/IllegalMonitorStateException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	hashCode() {key=Ljava/lang/Object;.hashCode()I} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	getClass() {key=Ljava/lang/Object;.getClass()Ljava/lang/Class;} [in Object [in Object.class [in java.lang [in " + jclPath +"]]]],\n" +
		"	finalize() {key=Ljava/lang/Object;.finalize()V|Ljava/lang/Throwable;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	equals(java.lang.Object) {key=Ljava/lang/Object;.equals(Ljava/lang/Object;)Z} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	clone() {key=Ljava/lang/Object;.clone()Ljava/lang/Object;|Ljava/lang/CloneNotSupportedException;} [in Object [in Object.class [in java.lang [in " + jclPath +"]]]],\n" +
		"}",
		result.context);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=312603
public void test0176() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"import java.text.MessageFormat;\n" +
		"public class X {\n" +
		"    String fooBar = \"Hi\";" +
		"	 String furchtbar= (fooBar = new String(String.format;\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("format");
	int tokenEnd = tokenStart + "format".length() - 1;
	int cursorLocation = str.lastIndexOf("format") + "format".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, true, true);
	String jclPath = getExternalJCLPathString();
	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"format\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n" +
		"completion token location=UNKNOWN\n" +
		"enclosingElement=X {key=Ltest/X;} [in [Working copy] X.java [in test [in src3 [in Completion]]]]\n" +
		"visibleElements={\n" +
		"	fooBar {key=Ltest/X;.fooBar)Ljava/lang/String;} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]],\n" +
		"	wait(long, int) {key=Ljava/lang/Object;.wait(JI)V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	wait(long) {key=Ljava/lang/Object;.wait(J)V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	wait() {key=Ljava/lang/Object;.wait()V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	toString() {key=Ljava/lang/Object;.toString()Ljava/lang/String;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	notifyAll() {key=Ljava/lang/Object;.notifyAll()V|Ljava/lang/IllegalMonitorStateException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	notify() {key=Ljava/lang/Object;.notify()V|Ljava/lang/IllegalMonitorStateException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	hashCode() {key=Ljava/lang/Object;.hashCode()I} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	getClass() {key=Ljava/lang/Object;.getClass()Ljava/lang/Class;} [in Object [in Object.class [in java.lang [in " + jclPath +"]]]],\n" +
		"	finalize() {key=Ljava/lang/Object;.finalize()V|Ljava/lang/Throwable;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	equals(java.lang.Object) {key=Ljava/lang/Object;.equals(Ljava/lang/Object;)Z} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	clone() {key=Ljava/lang/Object;.clone()Ljava/lang/Object;|Ljava/lang/CloneNotSupportedException;} [in Object [in Object.class [in java.lang [in " + jclPath +"]]]],\n" +
		"}",
		result.context);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=312603
public void test0177() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"import java.text.MessageFormat;\n" +
		"public class X {\n" +
		"    String fooBar = \"Hi\";" +
		"	 String furchtbar= MessageFormat.format\n" +
		"    String abc1 = \"Hi\";" +
		"    String abc2 = \"Hi\";" +
		"    String abc3 = \"Hi\";" +
		"    String abc4 = \"Hi\";" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("format");
	int tokenEnd = tokenStart + "format".length() - 1;
	int cursorLocation = str.lastIndexOf("format") + "format".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, true, true);
	String jclPath = getExternalJCLPathString();
	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"format\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures={Ljava.lang.String;}\n" +
		"expectedTypesKeys={Ljava/lang/String;}\n" +
		"completion token location=UNKNOWN\n" +
		"enclosingElement=furchtbar {key=Ltest/X;.furchtbar)Ljava/lang/String;} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]]\n" +
		"visibleElements={\n" +
		"	fooBar {key=Ltest/X;.fooBar)Ljava/lang/String;} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]],\n" +
		"	wait(long, int) {key=Ljava/lang/Object;.wait(JI)V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	wait(long) {key=Ljava/lang/Object;.wait(J)V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	wait() {key=Ljava/lang/Object;.wait()V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	toString() {key=Ljava/lang/Object;.toString()Ljava/lang/String;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	notifyAll() {key=Ljava/lang/Object;.notifyAll()V|Ljava/lang/IllegalMonitorStateException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	notify() {key=Ljava/lang/Object;.notify()V|Ljava/lang/IllegalMonitorStateException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	hashCode() {key=Ljava/lang/Object;.hashCode()I} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	getClass() {key=Ljava/lang/Object;.getClass()Ljava/lang/Class;} [in Object [in Object.class [in java.lang [in " + jclPath +"]]]],\n" +
		"	finalize() {key=Ljava/lang/Object;.finalize()V|Ljava/lang/Throwable;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	equals(java.lang.Object) {key=Ljava/lang/Object;.equals(Ljava/lang/Object;)Z} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	clone() {key=Ljava/lang/Object;.clone()Ljava/lang/Object;|Ljava/lang/CloneNotSupportedException;} [in Object [in Object.class [in java.lang [in " + jclPath +"]]]],\n" +
		"}",
		result.context);
}
public void testRegressionBug574267() throws Exception {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/Sample.java",
			"class Sample {\n" +
			"	void sample(String foo) {\n" +
			"		if (foo != null) {\n" +
			"			sys // content assist here\n" +
			"			System.out.println();\n" +
			"		}\n" +
			"	}\n" +
			"}");

	String str = this.workingCopies[0].getSource();
	String completeAfter = "sys";
	int tokenStart = str.indexOf(completeAfter);
	int tokenEnd = tokenStart + completeAfter.length() - 1;
	int cursorLocation = tokenStart + completeAfter.length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, true, true);
	String jclPath = getExternalJCLPathString();
	assertResults("completion offset="+(cursorLocation)+"\n" +
			"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
			"completion token=\"sys\"\n" +
			"completion token kind=TOKEN_KIND_NAME\n" +
			"expectedTypesSignatures=null\n" +
			"expectedTypesKeys=null\n" +
			"completion token location={STATEMENT_START}\n" +
			"enclosingElement=sample(String) {key=LSample;.sample(Ljava/lang/String;)V} [in Sample [in [Working copy] Sample.java [in <default> [in src [in Completion]]]]]\n" +
			"visibleElements={\n" +
			"	foo [in sample(String) [in Sample [in [Working copy] Sample.java [in <default> [in src [in Completion]]]]]],\n" +
			"	sample(String) {key=LSample;.sample(Ljava/lang/String;)V} [in Sample [in [Working copy] Sample.java [in <default> [in src [in Completion]]]]],\n" +
			"	wait(long, int) {key=Ljava/lang/Object;.wait(JI)V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
			"	wait(long) {key=Ljava/lang/Object;.wait(J)V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
			"	wait() {key=Ljava/lang/Object;.wait()V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
			"	toString() {key=Ljava/lang/Object;.toString()Ljava/lang/String;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
			"	notifyAll() {key=Ljava/lang/Object;.notifyAll()V|Ljava/lang/IllegalMonitorStateException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
			"	notify() {key=Ljava/lang/Object;.notify()V|Ljava/lang/IllegalMonitorStateException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
			"	hashCode() {key=Ljava/lang/Object;.hashCode()I} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
			"	getClass() {key=Ljava/lang/Object;.getClass()Ljava/lang/Class;} [in Object [in Object.class [in java.lang [in " + jclPath +"]]]],\n" +
			"	finalize() {key=Ljava/lang/Object;.finalize()V|Ljava/lang/Throwable;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
			"	equals(java.lang.Object) {key=Ljava/lang/Object;.equals(Ljava/lang/Object;)Z} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
			"	clone() {key=Ljava/lang/Object;.clone()Ljava/lang/Object;|Ljava/lang/CloneNotSupportedException;} [in Object [in Object.class [in java.lang [in " + jclPath +"]]]],\n" +
			"}"
			, result.context);
}
public void testBug553097_1() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0123/X.java",
		"package test0123;\n" +
		"public class X {\n" +
		"  String s = \"\"\"\n" +
		"ZZZZ\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	String token = "ZZZZ";
	int tokenStart = str.lastIndexOf(token);
	int tokenEnd = tokenStart + token.length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);
	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location={MEMBER_START}",
		result.context);
}
}
