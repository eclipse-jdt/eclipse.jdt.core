/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
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

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;

public class CompletionContextTests_1_5 extends AbstractJavaModelCompletionTests {

public CompletionContextTests_1_5(String name) {
	super(name);
}
@Override
public void setUpSuite() throws Exception {
	if (COMPLETION_PROJECT == null)  {
		COMPLETION_PROJECT = setUpJavaProject("Completion", "1.5");
	} else {
		setUpProjectCompliance(COMPLETION_PROJECT, "1.5");
	}
	super.setUpSuite();
}
public static Test suite() {
	return buildModelTestSuite(CompletionContextTests_1_5.class);
}
public void test0001() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0001/X.java",
		"package test0001;\n" +
		"public class X<T> {\n" +
		"  X<Object>.ZZZZ\n" +
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
public void test0002() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0002/X.java",
		"package test0002;\n" +
		"public class X<T> {\n" +
		"  X<Object>.ZZZZ\n" +
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
public void test0003() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0003/X.java",
		"package test0003;\n" +
		"public class X<T> {\n" +
		"  X<Object>.ZZZZ\n" +
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
public void test0004() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0004/X.java",
		"package test0004;\n" +
		"public class X<T> {\n" +
		"  X<Object>.\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf(">.") + ">.".length();
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf(">.") + ">.".length();

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
public void test0005() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0005/X.java",
		"package test0005;\n" +
		"@ZZZZ\n" +
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
public void test0006() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0006/X.java",
		"package test0006;\n" +
		"@ZZZZ\n" +
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
public void test0007() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0007/X.java",
		"package test0007;\n" +
		"@ZZZZ\n" +
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
public void test0008() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0008/X.java",
		"package test0008;\n" +
		"@\n" +
		"public class X {\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("@") + "@".length();
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("@") + "@".length();

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
public void test0009() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0009/X.java",
		"package test0009;\n" +
		"class Y {\n" +
		"}\n" +
		"@Y.ZZZZ\n" +
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
public void test0010() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0010/X.java",
		"package test0010;\n" +
		"class Y {\n" +
		"}\n" +
		"@Y.ZZZZ\n" +
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
public void test0011() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0011/X.java",
		"package test0011;\n" +
		"class Y {\n" +
		"}\n" +
		"@Y.ZZZZ\n" +
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
public void test0012() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0012/X.java",
		"package test0012;\n" +
		"class Y {\n" +
		"}\n" +
		"@Y.\n" +
		"public class X {\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("@Y.") + "@Y.".length();
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("@Y.") + "@Y.".length();

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
public void test0013() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0013/X.java",
		"package test0013;\n" +
		"@test0013.ZZZZ\n" +
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
public void test0014() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0014/X.java",
		"package test0014;\n" +
		"@test0014.ZZZZ\n" +
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
public void test0015() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0015/X.java",
		"package test0015;\n" +
		"@test0015.ZZZZ\n" +
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
public void test0016() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0016/X.java",
		"package test0016;\n" +
		"@test0016.\n" +
		"public class X {\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("@test0016.") + "@test0016.".length();
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("@test0016.") + "@test0016.".length();

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
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0017/X.java",
		"package test0017;\n" +
		"@interface Y {\n" +
		"  int value();\n" +
		"}\n" +
		"@Y(ZZZZ)\n" +
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
		"expectedTypesSignatures={I}\n" +
		"expectedTypesKeys={I}\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0018() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0018/X.java",
		"package test0018;\n" +
		"@interface Y {\n" +
		"  int value();\n" +
		"}\n" +
		"@Y(ZZZZ)\n" +
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
		"expectedTypesSignatures={I}\n" +
		"expectedTypesKeys={I}\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0019() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0019/X.java",
		"package test0019;\n" +
		"@interface Y {\n" +
		"  int value();\n" +
		"}\n" +
		"@Y(ZZZZ)\n" +
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
		"expectedTypesSignatures={I}\n" +
		"expectedTypesKeys={I}\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0020() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0020/X.java",
		"package test0020;\n" +
		"@interface Y {\n" +
		"  int value();\n" +
		"}\n" +
		"@Y()\n" +
		"public class X {\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("@Y(") + "@Y(".length();
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("@Y(") + "@Y(".length();

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
public void test0021() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0021/X.java",
		"package test0021;\n" +
		"@interface Y {\n" +
		"  int value1();\n" +
		"  int value2();\n" +
		"}\n" +
		"@Y(value1=1,ZZZZ)\n" +
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
public void test0022() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0022/X.java",
		"package test0022;\n" +
		"@interface Y {\n" +
		"  int value1();\n" +
		"  int value2();\n" +
		"}\n" +
		"@Y(value1=1,ZZZZ)\n" +
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
public void test0023() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0023/X.java",
		"package test0023;\n" +
		"@interface Y {\n" +
		"  int value1();\n" +
		"  int value2();\n" +
		"}\n" +
		"@Y(value1=1,ZZZZ)\n" +
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
public void test0024() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0024/X.java",
		"package test0024;\n" +
		"@interface Y {\n" +
		"  int value1();\n" +
		"  int value2();\n" +
		"}\n" +
		"@Y(value1=1,)\n" +
		"public class X {\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("value1=1,") + "value1=1,".length();
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("value1=1,") + "value1=1,".length();

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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=191125
public void test0025() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0006/X.java",
		"package test0006;\n" +
		"@ZZZZ\n" +
		"public class X {\n" +
		"}");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src3/pkgannotation/QQAnnotation.java",
		"package pkgannotations;\n" +
		"public @interface QQAnnotation {\n" +
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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=202467
public void test0026() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"  @MyAnnot ZZZZ\n" +
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
// enable this test when this case will be supported
public void test0027() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"  public void foo() {\n" +
		"    @MyAnnot ZZZZ\n" +
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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=202470
public void test0028() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X<TX> extends A<String> {\n" +
		"  public void methodX(TX x) {}\n" +
		"  public void foo() {\n" +
		"    zzzz\n" +
		"  }\n" +
		"}");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src3/test/A.java",
		"package test;\n" +
		"public class A<TA> {\n" +
		"  public void methodA(TA a) {}\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("zzzz");
	int tokenEnd = tokenStart + "zzzz".length() - 1;
	int cursorLocation = str.lastIndexOf("zzzz") + "zzzz".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, false, true);

	String jclPath = getExternalJCLPathString("1.5");
	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"zzzz\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n" +
		"completion token location={STATEMENT_START}\n" +
		"visibleElements={\n" +
		"	methodX(TX) {key=Ltest/X;.methodX(TTX;)V} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]],\n" +
		"	foo() {key=Ltest/X;.foo()V} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]],\n" +
		"	methodA(TA) {key=Ltest/A<Ljava/lang/String;>;.methodA(Ljava/lang/String;)V} [in A [in [Working copy] A.java [in test [in src3 [in Completion]]]]],\n" +
		"	wait(long, int) {key=Ljava/lang/Object;.wait(JI)V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	wait(long) {key=Ljava/lang/Object;.wait(J)V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	wait() {key=Ljava/lang/Object;.wait()V|Ljava/lang/IllegalMonitorStateException;|Ljava/lang/InterruptedException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	toString() {key=Ljava/lang/Object;.toString()Ljava/lang/String;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	notifyAll() {key=Ljava/lang/Object;.notifyAll()V|Ljava/lang/IllegalMonitorStateException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	notify() {key=Ljava/lang/Object;.notify()V|Ljava/lang/IllegalMonitorStateException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	hashCode() {key=Ljava/lang/Object;.hashCode()I} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	getClass() {key=Ljava/lang/Object;.getClass()Ljava/lang/Class<+Ljava/lang/Object;>;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	finalize() {key=Ljava/lang/Object;.finalize()V|Ljava/lang/Throwable;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	equals(java.lang.Object) {key=Ljava/lang/Object;.equals(Ljava/lang/Object;)Z} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"	clone() {key=Ljava/lang/Object;.clone()Ljava/lang/Object;|Ljava/lang/CloneNotSupportedException;} [in Object [in Object.class [in java.lang [in "+jclPath+"]]]],\n" +
		"}",
		result.context);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=202470
public void test0029() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"  public A<String> methodX() {return null;}\n" +
		"  public void foo() {\n" +
		"    zzzz\n" +
		"  }\n" +
		"}");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src3/test/A.java",
		"package test;\n" +
		"public class A<TA> {\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("zzzz");
	int tokenEnd = tokenStart + "zzzz".length() - 1;
	int cursorLocation = str.lastIndexOf("zzzz") + "zzzz".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, false, true, "Ltest/A<Ljava/lang/String;>;");

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"zzzz\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n" +
		"completion token location={STATEMENT_START}\n" +
		"visibleElements={\n" +
		"	methodX() {key=Ltest/X;.methodX()Ltest/A<Ljava/lang/String;>;} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]],\n" +
		"}",
		result.context);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=202470
public void test0030() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"  public A<String> methodX() {return null;}\n" +
		"  public void foo() {\n" +
		"    zzzz\n" +
		"  }\n" +
		"}");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src3/test/A.java",
		"package test;\n" +
		"public class A<TA> {\n" +
		"}");

	this.workingCopies[2] = getWorkingCopy(
		"/Completion/src3/test/B.java",
		"package test;\n" +
		"public class B<TB> {\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("zzzz");
	int tokenEnd = tokenStart + "zzzz".length() - 1;
	int cursorLocation = str.lastIndexOf("zzzz") + "zzzz".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, false, true, "Ltest/B<Ljava/lang/String;>;");

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
public void test0031() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"  public A<String> methodX() {return null;}\n" +
		"  public void foo() {\n" +
		"    zzzz\n" +
		"  }\n" +
		"}");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src3/test/A.java",
		"package test;\n" +
		"public class A<TA> extends B<TA>{\n" +
		"}");

	this.workingCopies[2] = getWorkingCopy(
		"/Completion/src3/test/B.java",
		"package test;\n" +
		"public class B<TB> {\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("zzzz");
	int tokenEnd = tokenStart + "zzzz".length() - 1;
	int cursorLocation = str.lastIndexOf("zzzz") + "zzzz".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, false, true, "Ltest/B<Ljava/lang/String;>;");

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"zzzz\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n" +
		"completion token location={STATEMENT_START}\n" +
		"visibleElements={\n" +
		"	methodX() {key=Ltest/X;.methodX()Ltest/A<Ljava/lang/String;>;} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]],\n" +
		"}",
		result.context);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=202470
public void test0032() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"  public A<String> methodX() {return null;}\n" +
		"  public void foo() {\n" +
		"    zzzz\n" +
		"  }\n" +
		"}");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src3/test/A.java",
		"package test;\n" +
		"public class A<TA> {\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("zzzz");
	int tokenEnd = tokenStart + "zzzz".length() - 1;
	int cursorLocation = str.lastIndexOf("zzzz") + "zzzz".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, false, true, "Ltest/Zork<Ljava/lang/String;>;");

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
public void test0033() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"  public A<String> methodX() {return null;}\n" +
		"  public void foo() {\n" +
		"    zzzz\n" +
		"  }\n" +
		"}");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src3/test/A.java",
		"package test;\n" +
		"public class A<TA> {\n" +
		"}");


	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("zzzz");
	int tokenEnd = tokenStart + "zzzz".length() - 1;
	int cursorLocation = str.lastIndexOf("zzzz") + "zzzz".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, false, true, "Ltest/A<LZork;>;");

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
public void test0034() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X<T> {\n" +
		"  public T methodX() {return null;}\n" +
		"  public void foo() {\n" +
		"    zzzz\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("zzzz");
	int tokenEnd = tokenStart + "zzzz".length() - 1;
	int cursorLocation = str.lastIndexOf("zzzz") + "zzzz".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, false, true, "TT;");

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"zzzz\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n" +
		"completion token location={STATEMENT_START}\n" +
		"visibleElements={\n" +
		"	methodX() {key=Ltest/X;.methodX()TT;} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]],\n" +
		"}",
		result.context);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=202470
public void test0035() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"  public <T> T methodX() {return null;}\n" +
		"  public void foo() {\n" +
		"    zzzz\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("zzzz");
	int tokenEnd = tokenStart + "zzzz".length() - 1;
	int cursorLocation = str.lastIndexOf("zzzz") + "zzzz".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, false, true, "TT;");

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
public void test0036() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"  public <T> T methodX() {\n" +
		"    zzzz\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("zzzz");
	int tokenEnd = tokenStart + "zzzz".length() - 1;
	int cursorLocation = str.lastIndexOf("zzzz") + "zzzz".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, false, true, "TT;");

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"zzzz\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n" +
		"completion token location={STATEMENT_START}\n" +
		"visibleElements={\n" +
		"	methodX() {key=Ltest/X;.methodX<T:Ljava/lang/Object;>()TT;} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]],\n" +
		"}",
		result.context);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=226673
public void test0037() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"  public A<String> methodX() {return null;}\n" +
		"  public void foo() {\n" +
		"    zzzz\n" +
		"  }\n" +
		"}");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src3/test/A.java",
		"package test;\n" +
		"public class A<TA> {\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("zzzz");
	int tokenEnd = tokenStart + "zzzz".length() - 1;
	int cursorLocation = str.lastIndexOf("zzzz") + "zzzz".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, false, true, "Ltest.A<Ljava.lang.String;>;");

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"zzzz\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n" +
		"completion token location={STATEMENT_START}\n" +
		"visibleElements={\n" +
		"	methodX() {key=Ltest/X;.methodX()Ltest/A<Ljava/lang/String;>;} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]],\n" +
		"}",
		result.context);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=226673
public void test0038() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"  public A<Z<String>>.B<Z<String>>.C<Z<String>> methodX() {return null;}\n" +
		"  public void foo() {\n" +
		"    zzzz\n" +
		"  }\n" +
		"}");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src3/test/Z.java",
		"package test;\n" +
		"public class Z<TZ> {\n" +
		"}");

	this.workingCopies[2] = getWorkingCopy(
		"/Completion/src3/test/A.java",
		"package test;\n" +
		"public class A<TA> {\n" +
		"  public class B<TB> {\n" +
		"    public class C<TC> {\n" +
		"    }\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("zzzz");
	int tokenEnd = tokenStart + "zzzz".length() - 1;
	int cursorLocation = str.lastIndexOf("zzzz") + "zzzz".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, false, true, "Ltest.A<Ltest.Z<Ljava.lang.String;>;>.B<Ltest.Z<Ljava.lang.String;>;>.C<Ltest.Z<Ljava.lang.String;>;>;");

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"zzzz\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n" +
		"completion token location={STATEMENT_START}\n" +
		"visibleElements={\n" +
		"	methodX() {key=Ltest/X;.methodX()Ltest/A<Ltest/Z<Ljava/lang/String;>;>.B<Ltest/Z<Ljava/lang/String;>;>.C<Ltest/Z<Ljava/lang/String;>;>;} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]],\n" +
		"}",
		result.context);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=226673
public void test0039() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"  public A<A<Z<String>>.B<Z<String>>> methodX() {return null;}\n" +
		"  public void foo() {\n" +
		"    zzzz\n" +
		"  }\n" +
		"}");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src3/test/Z.java",
		"package test;\n" +
		"public class Z<TZ> {\n" +
		"}");

	this.workingCopies[2] = getWorkingCopy(
		"/Completion/src3/test/A.java",
		"package test;\n" +
		"public class A<TA> {\n" +
		"  public class B<TB> {\n" +
		"    public class C<TC> {\n" +
		"    }\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("zzzz");
	int tokenEnd = tokenStart + "zzzz".length() - 1;
	int cursorLocation = str.lastIndexOf("zzzz") + "zzzz".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, false, true, "Ltest.A<Ltest.A<Ltest.Z<Ljava.lang.String;>;>.B<Ltest.Z<Ljava.lang.String;>;>;>;");

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"zzzz\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n" +
		"completion token location={STATEMENT_START}\n" +
		"visibleElements={\n" +
		"	methodX() {key=Ltest/X;.methodX()Ltest/A<Ltest/A<Ltest/Z<Ljava/lang/String;>;>.B<Ltest/Z<Ljava/lang/String;>;>;>;} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]],\n" +
		"}",
		result.context);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=227761
public void test0040() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"  public X foo() {return null;}\n" +
		"  public A() {\n" +
		"    zzzz\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("zzzz");
	int tokenEnd = tokenStart + "zzzz".length() - 1;
	int cursorLocation = str.lastIndexOf("zzzz") + "zzzz".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, false, true, "Ltest.X;");

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"zzzz\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n" +
		"completion token location={STATEMENT_START}\n" +
		"visibleElements={\n" +
		"	foo() {key=Ltest/X;.foo()Ltest/X;} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]],\n" +
		"}",
		result.context);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=274557
public void test0041() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"  @TestAnnot(value=\"\")\n" +
		"  public int field = 0;\n" +
		"}");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src3/test/TestAnnot.java",
		"package test;\n" +
		"public @interface X {\n" +
		"  String value();\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("\"\"");
	int tokenEnd = tokenStart + "\"\"".length() - 1;
	int cursorLocation = str.lastIndexOf("value=\"") + "value=\"".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_STRING_LITERAL\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n" +
		"completion token location=UNKNOWN",
		result.context);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=274557
public void test0042() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"  @TestAnnot(\"\")\n" +
		"  public int field = 0;\n" +
		"}");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src3/test/TestAnnot.java",
		"package test;\n" +
		"public @interface X {\n" +
		"  String value();\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("\"\"");
	int tokenEnd = tokenStart + "\"\"".length() - 1;
	int cursorLocation = str.lastIndexOf("@TestAnnot(\"") + "@TestAnnot(\"".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"\"\n" +
		"completion token kind=TOKEN_KIND_STRING_LITERAL\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n" +
		"completion token location=UNKNOWN",
		result.context);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=311022
public void testBug311022a() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X {\n" +
		"    void foo(Object o) {}\n" +
		"	 <T> void bar() {\n" +
		"    	T<T> loc = 12;\n" +
		"    	foo\n" +
		"    }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("foo");
	int tokenEnd = tokenStart + "foo".length() - 1;
	int cursorLocation = str.lastIndexOf("foo") + "foo".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, true, true, "Ljava.lang.Object;");
	String jclPath = getExternalJCLPathString("1.5");
	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"foo\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n" +
		"completion token location={STATEMENT_START}\n" +
		"enclosingElement=bar() {key=Ltest/X;.bar<T:Ljava/lang/Object;>()V} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]]\n" +
		"visibleElements={\n" +
		"	toString() {key=Ljava/lang/Object;.toString()Ljava/lang/String;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	getClass() {key=Ljava/lang/Object;.getClass()Ljava/lang/Class<+Ljava/lang/Object;>;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"	clone() {key=Ljava/lang/Object;.clone()Ljava/lang/Object;|Ljava/lang/CloneNotSupportedException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
		"}",
		result.context);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=311022
public void testBug311022b() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test/X.java",
		"package test;\n" +
		"public class X<A1,A2> {\n" +
		"    void foo(Object 0) {}\n" +
		"	 <T> void bar() {\n" +
		"    	X<String, String, String> x;\n" +
		"    	foo\n" +
		"    }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("foo");
	int tokenEnd = tokenStart + "foo".length() - 1;
	int cursorLocation = str.lastIndexOf("foo") + "foo".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation, true, true, "Ljava.lang.Object;");
	String jclPath = getExternalJCLPathString("1.5");
	assertResults(
			"completion offset="+(cursorLocation)+"\n" +
			"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
			"completion token=\"foo\"\n" +
			"completion token kind=TOKEN_KIND_NAME\n" +
			"expectedTypesSignatures=null\n" +
			"expectedTypesKeys=null\n" +
			"completion token location={STATEMENT_START}\n" +
			"enclosingElement=bar() {key=Ltest/X;.bar<T:Ljava/lang/Object;>()V} [in X [in [Working copy] X.java [in test [in src3 [in Completion]]]]]\n" +
			"visibleElements={\n" +
			"	toString() {key=Ljava/lang/Object;.toString()Ljava/lang/String;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
			"	getClass() {key=Ljava/lang/Object;.getClass()Ljava/lang/Class<+Ljava/lang/Object;>;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
			"	clone() {key=Ljava/lang/Object;.clone()Ljava/lang/Object;|Ljava/lang/CloneNotSupportedException;} [in Object [in Object.class [in java.lang [in " + jclPath + "]]]],\n" +
			"}",
			result.context);
}
}
