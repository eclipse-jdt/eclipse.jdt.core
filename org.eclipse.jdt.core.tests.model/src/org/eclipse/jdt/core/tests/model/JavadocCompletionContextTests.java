/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
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

import org.eclipse.jdt.core.*;

import junit.framework.*;

public class JavadocCompletionContextTests extends AbstractJavaModelCompletionTests {

public JavadocCompletionContextTests(String name) {
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
	return buildModelTestSuite(JavadocCompletionContextTests.class);
}
public void test0001() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0001/X.java",
		"""
			package test0001;
			/**
			 * @see ZZZZ
			 */
			public class X {
			}""");

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
		"""
			package test0002;
			/**
			 * @see ZZZZ
			 */
			public class X {
			}""");

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
		"""
			package test0003;
			/**
			 * @see ZZZZ
			 */
			public class X {
			}""");

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
		"""
			package test0004;
			/**
			 * @see\s
			 */
			public class X {
			}""");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("@see ") + "@see ".length();
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("@see ") + "@see ".length();

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
		"""
			package test0005;
			/**
			 * @see X.ZZZZ
			 */
			public class X {
			}""");

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
		"""
			package test0006;
			/**
			 * @see X.ZZZZ
			 */
			public class X {
			}""");

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
		"""
			package test0007;
			/**
			 * @see X.ZZZZ
			 */
			public class X {
			}""");

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
		"""
			package test0008;
			/**
			 * @see X.
			 */
			public class X {
			}""");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("X.") + "X.".length();
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("X.") + "X.".length();

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
		"""
			package test0009;
			/**
			 * @see test0009.ZZZZ
			 */
			public class X {
			}""");

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
		"""
			package test0010;
			/**
			 * @see test0010.ZZZZ
			 */
			public class X {
			}""");

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
		"""
			package test0011;
			/**
			 * @see test0011.ZZZZ
			 */
			public class X {
			}""");

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
		"""
			package test0012;
			/**
			 * @see test0012.
			 */
			public class X {
			}""");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("test0012.") + "test0012.".length();
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("test0012.") + "test0012.".length();

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
		"""
			package test0013;
			/**
			 * @see #ZZZZ
			 */
			public class X {
			}""");

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
		"""
			package test0014;
			/**
			 * @see #ZZZZ
			 */
			public class X {
			}""");

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
		"""
			package test0015;
			/**
			 * @see #ZZZZ
			 */
			public class X {
			}""");

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
		"/Completion/src3/test0012/X.java",
		"""
			package test0016;
			/**
			 * @see #
			 */
			public class X {
			}""");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("#") + "#".length();
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("#") + "#".length();

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
		"""
			package test0017;
			/**
			 * @see X#ZZZZ
			 */
			public class X {
			}""");

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
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0018/X.java",
		"""
			package test0018;
			/**
			 * @see X#ZZZZ
			 */
			public class X {
			}""");

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
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0019/X.java",
		"""
			package test0019;
			/**
			 * @see X#ZZZZ
			 */
			public class X {
			}""");

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
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0020/X.java",
		"""
			package test0020;
			/**
			 * @see X#
			 */
			public class X {
			}""");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("#") + "#".length();
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("#") + "#".length();

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
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0021/X.java",
		"""
			package test0021;
			/**
			 * @see X#foo(ZZZZ
			 */
			public class X {
			  public void foo(Object a, Object b){}
			}""");

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
		"""
			package test0022;
			/**
			 * @see X#foo(ZZZZ
			 */
			public class X {
			  public void foo(Object a, Object b){}
			}""");

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
		"""
			package test0023;
			/**
			 * @see X#foo(ZZZZ
			 */
			public class X {
			  public void foo(Object a, Object b){}
			}""");

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
		"""
			package test0024;
			/**
			 * @see X#foo(
			 */
			public class X {
			  public void foo(Object a, Object b){}
			}""");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("#foo(") + "#foo(".length();
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("#foo(") + "#foo(".length();

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
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0025/X.java",
		"""
			package test0025;
			/**
			 * @see X#foo(Object ZZZZ
			 */
			public class X {
			  public void foo(Object a, Object b){}
			}""");

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
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0026/X.java",
		"""
			package test0026;
			/**
			 * @see X#foo(Object ZZZZ
			 */
			public class X {
			  public void foo(Object a, Object b){}
			}""");

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
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0027/X.java",
		"""
			package test0027;
			/**
			 * @see X#foo(Object ZZZZ
			 */
			public class X {
			  public void foo(Object a, Object b){}
			}""");

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
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0028/X.java",
		"""
			package test0028;
			/**
			 * @see X#foo(Object\s
			 */
			public class X {
			  public void foo(Object a, Object b){}
			}""");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("#foo(Object ") + "#foo(Object ".length();
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("#foo(Object ") + "#foo(Object ".length();

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
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0029/X.java",
		"""
			package test0029;
			/**
			 * @see X#foo(Object a,ZZZZ
			 */
			public class X {
			  public void foo(Object a, Object b){}
			}""");

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
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0030/X.java",
		"""
			package test0030;
			/**
			 * @see X#foo(Object a,ZZZZ
			 */
			public class X {
			  public void foo(Object a, Object b){}
			}""");

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
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0031/X.java",
		"""
			package test0031;
			/**
			 * @see X#foo(Object a,ZZZZ
			 */
			public class X {
			  public void foo(Object a, Object b){}
			}""");

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
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0032/X.java",
		"""
			package test0032;
			/**
			 * @see X#foo(Object a,
			 */
			public class X {
			  public void foo(Object a, Object b){}
			}""");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("#foo(Object a,") + "#foo(Object a,".length();
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("#foo(Object a,") + "#foo(Object a,".length();

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
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0033/X.java",
		"""
			package test0033;
			/**
			 * @see X#X(ZZZZ
			 */
			public class X {
			  public X(Object a, Object b){}
			}""");

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
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0034/X.java",
		"""
			package test0034;
			/**
			 * @see X#X(ZZZZ
			 */
			public class X {
			  public X(Object a, Object b){}
			}""");

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
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0035/X.java",
		"""
			package test0035;
			/**
			 * @see X#X(ZZZZ
			 */
			public class X {
			  public X(Object a, Object b){}
			}""");

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
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0036/X.java",
		"""
			package test0036;
			/**
			 * @see X#X(
			 */
			public class X {
			  public X(Object a, Object b){}
			}""");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("#X(") + "#X(".length();
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("#X(") + "#X(".length();

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
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0037/X.java",
		"""
			package test0037;
			/**
			 * @see X#X(Object ZZZZ
			 */
			public class X {
			  public X(Object a, Object b){}
			}""");

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
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0038/X.java",
		"""
			package test0038;
			/**
			 * @see X#X(Object ZZZZ
			 */
			public class X {
			  public X(Object a, Object b){}
			}""");

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
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0039/X.java",
		"""
			package test0039;
			/**
			 * @see X#X(Object ZZZZ
			 */
			public class X {
			  public X(Object a, Object b){}
			}""");

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
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0040/X.java",
		"""
			package test0040;
			/**
			 * @see X#X(Object\s
			 */
			public class X {
			  public X(Object a, Object b){}
			}""");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("#X(Object ") + "#X(Object ".length();
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("#X(Object ") + "#X(Object ".length();

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
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0041/X.java",
		"""
			package test0041;
			/**
			 * @see X#X(Object a,ZZZZ
			 */
			public class X {
			  public X(Object a, Object b){}
			}""");

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
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0042/X.java",
		"""
			package test0042;
			/**
			 * @see X#X(Object a,ZZZZ
			 */
			public class X {
			  public X(Object a, Object b){}
			}""");

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
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0043/X.java",
		"""
			package test0043;
			/**
			 * @see X#X(Object a,ZZZZ
			 */
			public class X {
			  public X(Object a, Object b){}
			}""");

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
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0044/X.java",
		"""
			package test0044;
			/**
			 * @see X#X(Object a,
			 */
			public class X {
			  public X(Object a, Object b){}
			}""");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("#X(Object a,") + "#X(Object a,".length();
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("#X(Object a,") + "#X(Object a,".length();

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
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0045/X.java",
		"""
			package test0045;
			public class X {
			  /**
			   * @param ZZZZ
			   */
			  public void foo(Object a, Object b){}
			}""");

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
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0046/X.java",
		"""
			package test0046;
			public class X {
			  /**
			   * @param ZZZZ
			   */
			  public void foo(Object a, Object b){}
			}""");

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
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0047/X.java",
		"""
			package test0047;
			public class X {
			  /**
			   * @param ZZZZ
			   */
			  public void foo(Object a, Object b){}
			}""");

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
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0048/X.java",
		"""
			package test0048;
			public class X {
			  /**
			   * @param\s
			   */
			  public void foo(Object a, Object b){}
			}""");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("@param ") + "@param ".length();
	int tokenEnd = tokenStart + "".length() - 1;
	int cursorLocation = str.lastIndexOf("@param ") + "@param ".length();

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
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0049/X.java",
		"""
			package test0049;
			public class X {
			  /**
			   * @ZZZZ
			   */
			  public void foo(){}
			}""");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("@ZZZZ");
	int tokenEnd = tokenStart + "@ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"@ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0050() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0050/X.java",
		"""
			package test0050;
			public class X {
			  /**
			   * @ZZZZ
			   */
			  public void foo(){}
			}""");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("@ZZZZ");
	int tokenEnd = tokenStart + "@ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ");

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"@\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0051() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0051/X.java",
		"""
			package test0051;
			public class X {
			  /**
			   * @ZZZZ
			   */
			  public void foo(){}
			}""");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("@ZZZZ");
	int tokenEnd = tokenStart + "@ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"@ZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0052() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0052/X.java",
		"""
			package test0052;
			public class X {
			  /**
			   * @
			   */
			  public void foo(){}
			}""");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("@");
	int tokenEnd = tokenStart + "@".length() - 1;
	int cursorLocation = str.lastIndexOf("@") + "@".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"@\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0053() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0053/X.java",
		"""
			package test0053;
			public class X {
			  /**
			   * blabla @ZZZZ
			   */
			  public void foo(){}
			}""");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("@ZZZZ");
	int tokenEnd = tokenStart + "@ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"@ZZZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0054() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0054/X.java",
		"""
			package test0054;
			public class X {
			  /**
			   * blabla @ZZZZ
			   */
			  public void foo(){}
			}""");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("@ZZZZ");
	int tokenEnd = tokenStart + "@ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ");

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"@\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0055() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0055/X.java",
		"""
			package test0055;
			public class X {
			  /**
			   * blabla @ZZZZ
			   */
			  public void foo(){}
			}""");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("@ZZZZ");
	int tokenEnd = tokenStart + "@ZZZZ".length() - 1;
	int cursorLocation = str.lastIndexOf("ZZZZ") + "ZZ".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"@ZZ\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
public void test0056() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0056/X.java",
		"""
			package test0056;
			public class X {
			  /**
			   * blabla @
			   */
			  public void foo(){}
			}""");

	String str = this.workingCopies[0].getSource();
	int tokenStart = str.lastIndexOf("@");
	int tokenEnd = tokenStart + "@".length() - 1;
	int cursorLocation = str.lastIndexOf("@") + "@".length();

	CompletionResult result = contextComplete(this.workingCopies[0], cursorLocation);

	assertResults(
		"completion offset="+(cursorLocation)+"\n" +
		"completion range=["+(tokenStart)+", "+(tokenEnd)+"]\n" +
		"completion token=\"@\"\n" +
		"completion token kind=TOKEN_KIND_NAME\n" +
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null\n"+
		"completion token location=UNKNOWN",
		result.context);
}
}
