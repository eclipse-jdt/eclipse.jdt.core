/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.lang.reflect.Method;

import org.eclipse.jdt.core.*;

import org.eclipse.jdt.internal.codeassist.RelevanceConstants;

import junit.framework.*;

public class CompletionContextTests extends AbstractJavaModelCompletionTests implements RelevanceConstants {

public CompletionContextTests(String name) {
	super(name);
}
public void setUpSuite() throws Exception {
	if (COMPLETION_PROJECT == null)  {
		COMPLETION_PROJECT = setUpJavaProject("Completion", "1.4");
	} else {
		setUpProjectCompliance(COMPLETION_PROJECT, "1.4");
	}
	super.setUpSuite();
}

public static Test suite() {
	TestSuite suite = new Suite(CompletionContextTests.class.getName());		

	if (true) {
		Class c = CompletionContextTests.class;
		Method[] methods = c.getMethods();
		for (int i = 0, max = methods.length; i < max; i++) {
			if (methods[i].getName().startsWith("test")) { //$NON-NLS-1$
				suite.addTest(new CompletionContextTests(methods[i].getName()));
			}
		}
		return suite;
	}
	suite.addTest(new CompletionContextTests("test0065"));			
	return suite;
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
		result.context);
}
public void _test0016() throws JavaModelException {
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures={I}\n" +
		"expectedTypesKeys={I}",
		result.context);
}
public void _test0090() throws JavaModelException {
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
		"expectedTypesSignatures={I}\n" +
		"expectedTypesKeys={I}",
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
		"expectedTypesSignatures={I}\n" +
		"expectedTypesKeys={I}",
		result.context);
}
public void _test0092() throws JavaModelException {
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
		"expectedTypesSignatures={I}\n" +
		"expectedTypesKeys={I}",
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
		"expectedTypesSignatures={I}\n" +
		"expectedTypesKeys={I}",
		result.context);
}
public void _test0094() throws JavaModelException {
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
		"expectedTypesSignatures={I}\n" +
		"expectedTypesKeys={I}",
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
		"expectedTypesSignatures={I}\n" +
		"expectedTypesKeys={I}",
		result.context);
}
public void _test0096() throws JavaModelException {
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
		"expectedTypesSignatures={I}\n" +
		"expectedTypesKeys={I}",
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
		"expectedTypesSignatures={I}\n" +
		"expectedTypesKeys={I}",
		result.context);
}
public void _test0098() throws JavaModelException {
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
		"expectedTypesSignatures={I}\n" +
		"expectedTypesKeys={I}",
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
		"expectedTypesSignatures={I}\n" +
		"expectedTypesKeys={I}",
		result.context);
}
public void _test0100() throws JavaModelException {
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
		"expectedTypesSignatures={I}\n" +
		"expectedTypesKeys={I}",
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
		"expectedTypesSignatures={I}\n" +
		"expectedTypesKeys={I}",
		result.context);
}
public void _test0102() throws JavaModelException {
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
		"expectedTypesSignatures={I}\n" +
		"expectedTypesKeys={I}",
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
		"expectedTypesSignatures={I}\n" +
		"expectedTypesKeys={I}",
		result.context);
}
public void _test0104() throws JavaModelException {
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
		"expectedTypesSignatures={I}\n" +
		"expectedTypesKeys={I}",
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
		"expectedTypesSignatures={Ljava.lang.Object;}\n" +
		"expectedTypesKeys={Ljava/lang/Object;}",
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
		"expectedTypesSignatures={Ljava.lang.Object;}\n" +
		"expectedTypesKeys={Ljava/lang/Object;}",
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
		"expectedTypesSignatures={Ljava.lang.Object;}\n" +
		"expectedTypesKeys={Ljava/lang/Object;}",
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
		"expectedTypesSignatures={Ljava.lang.Object;}\n" +
		"expectedTypesKeys={Ljava/lang/Object;}",
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
		"expectedTypesSignatures={Ljava.lang.Object;}\n" +
		"expectedTypesKeys={Ljava/lang/Object;}",
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
		"expectedTypesSignatures={Ljava.lang.Object;}\n" +
		"expectedTypesKeys={Ljava/lang/Object;}",
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
		"expectedTypesSignatures={Ljava.lang.Object;}\n" +
		"expectedTypesKeys={Ljava/lang/Object;}",
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
		"expectedTypesSignatures={Ljava.lang.Object;}\n" +
		"expectedTypesKeys={Ljava/lang/Object;}",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
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
		"expectedTypesSignatures=null\n" +
		"expectedTypesKeys=null",
		result.context);
}
}
