/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
import java.util.Hashtable;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.codeassist.RelevanceConstants;

import junit.framework.*;

public class CompletionTests_1_5 extends AbstractJavaModelCompletionTests implements RelevanceConstants {

public CompletionTests_1_5(String name) {
	super(name);
}
public void setUpSuite() throws Exception {
	setUpJavaProject("Completion", "1.5");
	super.setUpSuite();
}

public void tearDownSuite() throws Exception {
	super.tearDownSuite();
	deleteProject("Completion");
}
public static Test suite() {
	TestSuite suite = new Suite(CompletionTests_1_5.class.getName());		

	if (true) {
		Class c = CompletionTests_1_5.class;
		Method[] methods = c.getMethods();
		for (int i = 0, max = methods.length; i < max; i++) {
			if (methods[i].getName().startsWith("test")) { //$NON-NLS-1$
				suite.addTest(new CompletionTests_1_5(methods[i].getName()));
			}
		}
		return suite;
	}
	suite.addTest(new CompletionTests_1_5("test0136"));			
	return suite;
}

public void test0001() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0001", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = "X<St";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("should have one class",
		"element:String    completion:String    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED),
		requestor.getResults());
}
public void test0002() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0002", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = "X<Ob";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("should have one class",
		"element:Object    completion:Object    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED),
		requestor.getResults());
}
public void test0003() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0003", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = "X<St";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("should have one class",
		"element:String    completion:String    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_EXACT_EXPECTED_TYPE + R_NON_RESTRICTED),
		requestor.getResults());
}
public void test0004() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0004", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = "X<XZ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("should have one class",
		"element:XZX    completion:XZX    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"\n"+
		"element:XZXSuper    completion:XZXSuper    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_EXACT_EXPECTED_TYPE + R_NON_RESTRICTED),
		requestor.getResults());
}
public void test0005() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0005", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = "Y<St";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("should have one class",
		"element:String    completion:String    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED),
		requestor.getResults());
}
public void test0006() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0006", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = "Y<Ob";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("should have one class",
		"element:Object    completion:Object    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED),
		requestor.getResults());
}
public void test0007() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0007", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = "Y<St";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("should have one class",
		"element:String    completion:String    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_EXACT_EXPECTED_TYPE + R_NON_RESTRICTED),
		requestor.getResults());
}
public void test0008() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0008", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = "Y<XY";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("should have one class",
		"element:XYX    completion:XYX    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"\n"+
		"element:XYXSuper    completion:XYXSuper    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_EXACT_EXPECTED_TYPE + R_NON_RESTRICTED),
		requestor.getResults());
}
public void test0009() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0009", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = "/**/T_";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("should have one class",
		"element:T_1    completion:T_1    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"\n"+
		"element:T_2    completion:T_2    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED),
		requestor.getResults());
}
public void test0010() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0010", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = "/**/T_";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("should have one class",
		"element:T_1    completion:T_1    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"\n"+
		"element:T_2    completion:T_2    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"\n"+
		"element:T_3    completion:T_3    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"\n"+
		"element:T_4    completion:T_4    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED),
		requestor.getResults());
}
public void test0011() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0011", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = ".Y001";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("should have one class",
		"element:Z0011<java.lang.Object>.Y0011    completion:Y0011    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED),
		requestor.getResults());
}
public void test0012() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0012", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = ".Y001";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("should have one class",
		"element:Z0012<java.lang.Object>.Y0012    completion:Y0012    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED),
		requestor.getResults());
}
public void test0013() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0013", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = ".Y001";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("should have one class",
		"element:Z0013<java.lang.Object>.Y0013    completion:Y0013    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED),
		requestor.getResults());
}
public void test0014() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0014", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = ".Y001";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("should have one class",
		"element:Z0014<java.lang.Object>.Y0014    completion:Y0014    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_CLASS + R_NON_RESTRICTED),
		requestor.getResults());
}
public void test0015() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0015", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = ".Y001";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("should have one class",
		"element:Z0015<java.lang.Object>.Y0015    completion:Y0015    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "\n" +
		"element:Z0015<java.lang.Object>.Y0015I    completion:Y0015I    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_CLASS + R_NON_RESTRICTED),
		requestor.getResults());
}
public void test0016() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0016", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = ".Y001";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("should have one class",
		"element:Z0016<java.lang.Object>.Y0016    completion:Y0016    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED),
		requestor.getResults());
}
public void test0017() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0017", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = ".Y001";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("should have one class",
		"element:Z0017<java.lang.Object>.Y0017    completion:Y0017    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED),
		requestor.getResults());
}
public void test0018() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0018", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = ".Y001";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("should have one class",
		"element:Z0018<java.lang.Object>.Y0018    completion:Y0018    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED),
		requestor.getResults());
}
public void test0019() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0019", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = ".Y001";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("should have one class",
		"element:Z0019<java.lang.Object>.Y0019    completion:Y0019    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED),
		requestor.getResults());
}
public void test0020() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0020", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = ".Y002";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("should have one class",
		"element:Z0020<java.lang.Object>.Y0020    completion:Y0020    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED),
		requestor.getResults());
}
public void test0021() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0021", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = "<Z0021";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("should have one class",
		"element:Z0021Z    completion:Z0021Z    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"\n"+
		"element:Z0021ZZ    completion:Z0021ZZ    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED),
		requestor.getResults());
}
public void test0022() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0022", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = "<Z0022Z";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("should have one class",
		"element:Z0022ZZ    completion:Z0022ZZ    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_EXACT_EXPECTED_TYPE + R_NON_RESTRICTED)+"\n"+
		"element:Z0022ZZZ    completion:Z0022ZZZ    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED),
		requestor.getResults());
}
public void test0023() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0023", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = "<St";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("should have one class",
		"",
		requestor.getResults());
}
public void test0024() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0024", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = "<St";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("should have one class",
		"",
		requestor.getResults());
}
public void test0025() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0025", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = "<St";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("should have one class",
		"element:String    completion:String    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED),
		requestor.getResults());
}
public void test0026() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0026", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = "Z<St";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("should have one class",
		"element:String    completion:String    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED),
		requestor.getResults());
}
public void test0027() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0027", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = "7<St";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("should have one class",
		"element:String    completion:String    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED),
		requestor.getResults());
}
public void test0028() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0028", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = "<St";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("should have one class",
		"element:String    completion:String    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_EXACT_EXPECTED_TYPE + R_NON_RESTRICTED),
		requestor.getResults());
}
public void test0029() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0029", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = "Inner2";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("should have one class",
		"element:Test.Inner2<T>    completion:Inner2    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_NAME + R_NON_RESTRICTED),
		requestor.getResults());
}
public void test0030() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0030", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = "ZZ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("unexpected result",
		"element:ZZX    completion:ZZX    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+ "\n" +
		"element:ZZY    completion:ZZY    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED),
		requestor.getResults());
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=72501
 */
public void test0031() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0031", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = "Stri";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("unexpected result",
		"",
		requestor.getResults());
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=59082
 */
public void test0032() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0032", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = "Stri";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("unexpected result",
		"element:String    completion:String    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED),
		requestor.getResults());
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=59082
 */
public void test0033() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0033", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = "Stri";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("unexpected result",
		"element:String    completion:String    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED),
		requestor.getResults());
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=59082
 */
public void test0034() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0034", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = "Stri";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("unexpected result",
		"",
		requestor.getResults());
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=59082
 */
public void test0035() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0035", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = "Stri";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("unexpected result",
		"",
		requestor.getResults());
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=59082
 */
public void test0036() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0036", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = "Stri";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("unexpected result",
		"",
		requestor.getResults());
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=59082
 */
public void test0037() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0037", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = "Stri";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("unexpected result",
		"",
		requestor.getResults());
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=59082
 */
public void test0038() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0038", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = "Stri";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("unexpected result",
		"",
		requestor.getResults());
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=59082
 */
public void test0039() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0039", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = "Stri";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("unexpected result",
		"",
		requestor.getResults());
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=59082
 */
public void test0040() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0040", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = "Stri";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("unexpected result",
		"element:String    completion:String    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED),
		requestor.getResults());
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=59082
 */
public void test0041() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0041", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = "Stri";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("unexpected result",
		"element:String    completion:String    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED),
		requestor.getResults());
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=59082
 */
public void test0042() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0042", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = "Stri";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("unexpected result",
		"",
		requestor.getResults());
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=59082
 */
public void test0043() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0043", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = "Stri";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("unexpected result",
		"",
		requestor.getResults());
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=59082
 */
public void test0044() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0044", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = "Stri";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("unexpected result",
		"",
		requestor.getResults());
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=59082
 */
public void test0045() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0045", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = "Stri";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("unexpected result",
		"",
		requestor.getResults());
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=59082
 */
public void test0046() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0046", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = "Stri";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("unexpected result",
		"",
		requestor.getResults());
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=59082
 */
public void test0047() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0047", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = "Stri";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("unexpected result",
		"",
		requestor.getResults());
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=75455
 */
public void test0048() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0048", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = "l.ba";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("unexpected result",
		"element:bar    completion:bar()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED),
		requestor.getResults());
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=75455
 */
public void test0049() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0049", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = "l.ba";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("unexpected result",
		"element:bar    completion:bar()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED),
		requestor.getResults());
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=74753
 */
public void test0050() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0050", "Test.java");
	
	String str = cu.getSource();
	String completeBehind = "Test<T_0050";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("unexpected result",
		"element:T_0050    completion:T_0050    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_NAME + R_UNQUALIFIED + R_NON_RESTRICTED),
		requestor.getResults());
}
public void test0051() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();
	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
		ICompilationUnit cu= getCompilationUnit("Completion", "src3", "test0051", "Test.java");
	
		String str = cu.getSource();
		String completeBehind = "Inner";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);
	
		assertResults(
				"QQType1.Inner2[TYPE_REF]{Inner2, pkgstaticimport, Lpkgstaticimport.QQType1$Inner2;, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	} finally {
		JavaCore.setOptions(oldOptions);
	}
}
public void test0052() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();
	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
		ICompilationUnit cu= getCompilationUnit("Completion", "src3", "test0052", "Test.java");
	
		String str = cu.getSource();
		String completeBehind = "Inner";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);
	
		assertResults(
				"QQType2.Inner2[TYPE_REF]{Inner2, test0052, Ltest0052.QQType2$Inner2;, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
				"QQType2.Inner4[TYPE_REF]{Inner4, test0052, Ltest0052.QQType2$Inner4;, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
				"QQType2.Inner8[TYPE_REF]{Inner8, test0052, Ltest0052.QQType2$Inner8;, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	} finally {
		JavaCore.setOptions(oldOptions);
	}
}
public void test0053() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();
	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
		ICompilationUnit cu= getCompilationUnit("Completion", "src3", "test0053", "Test.java");
	
		String str = cu.getSource();
		String completeBehind = "Inner";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);
	
		assertResults(
				"QQType1.Inner1[TYPE_REF]{Inner1, pkgstaticimport, Lpkgstaticimport.QQType1$Inner1;, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
				"QQType1.Inner2[TYPE_REF]{Inner2, pkgstaticimport, Lpkgstaticimport.QQType1$Inner2;, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
				"QQType1.Inner3[TYPE_REF]{Inner3, pkgstaticimport, Lpkgstaticimport.QQType1$Inner3;, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
				"QQType1.Inner4[TYPE_REF]{Inner4, pkgstaticimport, Lpkgstaticimport.QQType1$Inner4;, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	} finally {
		JavaCore.setOptions(oldOptions);
	}
}
public void test0054() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();
	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
		ICompilationUnit cu= getCompilationUnit("Completion", "src3", "test0054", "Test.java");
	
		String str = cu.getSource();
		String completeBehind = "Inner";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);
	
		assertResults(
				"QQType1.Inner2[TYPE_REF]{Inner2, pkgstaticimport, Lpkgstaticimport.QQType1$Inner2;, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	} finally {
		JavaCore.setOptions(oldOptions);
	}
}
public void test0055() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();
	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
		ICompilationUnit cu= getCompilationUnit("Completion", "src3", "test0055", "Test.java");
	
		String str = cu.getSource();
		String completeBehind = "Inner";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);
	
		assertResults(
				"QQType1.Inner2[TYPE_REF]{Inner2, pkgstaticimport, Lpkgstaticimport.QQType1$Inner2;, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	} finally {
		JavaCore.setOptions(oldOptions);
	}
}
public void test0056() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();
	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
		ICompilationUnit cu= getCompilationUnit("Completion", "src3", "test0056", "Test.java");
	
		String str = cu.getSource();
		String completeBehind = "Inner";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);
	
		assertResults(
				"QQType1.Inner2[TYPE_REF]{Inner2, pkgstaticimport, Lpkgstaticimport.QQType1$Inner2;, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	} finally {
		JavaCore.setOptions(oldOptions);
	}
}
public void test0057() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();
	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
		ICompilationUnit cu= getCompilationUnit("Completion", "src3", "test0057", "Test.java");
	
		String str = cu.getSource();
		String completeBehind = "Inner";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);
	
		assertResults(
				"QQType1.Inner2[TYPE_REF]{Inner2, pkgstaticimport, Lpkgstaticimport.QQType1$Inner2;, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	} finally {
		JavaCore.setOptions(oldOptions);
	}
}
public void test0058() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();
	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
		ICompilationUnit cu= getCompilationUnit("Completion", "src3", "test0058", "Test.java");
	
		String str = cu.getSource();
		String completeBehind = "zzvarzz";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);
	
		assertResults(
				"zzvarzz2[FIELD_REF]{zzvarzz2, Lpkgstaticimport.QQType4;, I, zzvarzz2, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	} finally {
		JavaCore.setOptions(oldOptions);
	}
}
public void test0059() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();
	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
		ICompilationUnit cu= getCompilationUnit("Completion", "src3", "test0059", "Test.java");
	
		String str = cu.getSource();
		String completeBehind = "zzvarzz";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);
	
		assertResults(
				"zzvarzz2[FIELD_REF]{zzvarzz2, Ltest0059.QQType5;, I, zzvarzz2, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzvarzz4[FIELD_REF]{zzvarzz4, Ltest0059.QQType5;, I, zzvarzz4, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzvarzz8[FIELD_REF]{zzvarzz8, Ltest0059.QQType5;, I, zzvarzz8, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	} finally {
		JavaCore.setOptions(oldOptions);
	}
}
public void test0060() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();
	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
		ICompilationUnit cu= getCompilationUnit("Completion", "src3", "test0060", "Test.java");
	
		String str = cu.getSource();
		String completeBehind = "zzvarzz";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);
	
		assertResults(
				"zzvarzz1[FIELD_REF]{zzvarzz1, Lpkgstaticimport.QQType4;, I, zzvarzz1, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzvarzz2[FIELD_REF]{zzvarzz2, Lpkgstaticimport.QQType4;, I, zzvarzz2, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzvarzz3[FIELD_REF]{zzvarzz3, Lpkgstaticimport.QQType4;, I, zzvarzz3, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzvarzz4[FIELD_REF]{zzvarzz4, Lpkgstaticimport.QQType4;, I, zzvarzz4, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	} finally {
		JavaCore.setOptions(oldOptions);
	}
}
public void test0061() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();
	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
		ICompilationUnit cu= getCompilationUnit("Completion", "src3", "test0061", "Test.java");
	
		String str = cu.getSource();
		String completeBehind = "zzvarzz";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);
	
		assertResults(
				"zzvarzz2[FIELD_REF]{zzvarzz2, Lpkgstaticimport.QQType4;, I, zzvarzz2, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	} finally {
		JavaCore.setOptions(oldOptions);
	}
}
public void test0062() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();
	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
		ICompilationUnit cu= getCompilationUnit("Completion", "src3", "test0062", "Test.java");
	
		String str = cu.getSource();
		String completeBehind = "zzvarzz";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);
	
		assertResults(
				"zzvarzz2[FIELD_REF]{zzvarzz2, Lpkgstaticimport.QQType4;, I, zzvarzz2, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	} finally {
		JavaCore.setOptions(oldOptions);
	}
}
public void test0063() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();
	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
		ICompilationUnit cu= getCompilationUnit("Completion", "src3", "test0063", "Test.java");
	
		String str = cu.getSource();
		String completeBehind = "zzvarzz";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);
	
		assertResults(
				"zzvarzz2[FIELD_REF]{zzvarzz2, Lpkgstaticimport.QQType4;, I, zzvarzz2, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	} finally {
		JavaCore.setOptions(oldOptions);
	}
}
public void test0064() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();
	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
		ICompilationUnit cu= getCompilationUnit("Completion", "src3", "test0064", "Test.java");
	
		String str = cu.getSource();
		String completeBehind = "zzvarzz";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);
	
		assertResults(
				"zzvarzz2[FIELD_REF]{zzvarzz2, Lpkgstaticimport.QQType4;, I, zzvarzz2, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	} finally {
		JavaCore.setOptions(oldOptions);
	}
}
public void test0065() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();
	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
		ICompilationUnit cu= getCompilationUnit("Completion", "src3", "test0065", "Test.java");
	
		String str = cu.getSource();
		String completeBehind = "zzfoozz";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);
	
		assertResults(
				"zzfoozz2[METHOD_REF]{zzfoozz2(), Lpkgstaticimport.QQType7;, ()V, zzfoozz2, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	} finally {
		JavaCore.setOptions(oldOptions);
	}
}
public void test0066() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();
	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
		ICompilationUnit cu= getCompilationUnit("Completion", "src3", "test0066", "Test.java");
	
		String str = cu.getSource();
		String completeBehind = "zzfoozz";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);
	
		assertResults(
				"zzfoozz2[METHOD_REF]{zzfoozz2(), Ltest0066.QQType8;, ()V, zzfoozz2, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzfoozz4[METHOD_REF]{zzfoozz4(), Ltest0066.QQType8;, ()V, zzfoozz4, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzfoozz8[METHOD_REF]{zzfoozz8(), Ltest0066.QQType8;, ()V, zzfoozz8, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	} finally {
		JavaCore.setOptions(oldOptions);
	}
}
public void test0067() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();
	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
		ICompilationUnit cu= getCompilationUnit("Completion", "src3", "test0067", "Test.java");
	
		String str = cu.getSource();
		String completeBehind = "zzfoozz";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);
	
		assertResults(
				"zzfoozz1[METHOD_REF]{zzfoozz1(), Lpkgstaticimport.QQType7;, ()V, zzfoozz1, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzfoozz2[METHOD_REF]{zzfoozz2(), Lpkgstaticimport.QQType7;, ()V, zzfoozz2, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzfoozz3[METHOD_REF]{zzfoozz3(), Lpkgstaticimport.QQType7;, ()V, zzfoozz3, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzfoozz4[METHOD_REF]{zzfoozz4(), Lpkgstaticimport.QQType7;, ()V, zzfoozz4, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	} finally {
		JavaCore.setOptions(oldOptions);
	}
}
public void test0068() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();
	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);
		
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
		ICompilationUnit cu= getCompilationUnit("Completion", "src3", "test0068", "Test.java");
	
		String str = cu.getSource();
		String completeBehind = "zzfoozz";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor);
	
		assertResults(
				"zzfoozz2[METHOD_REF]{zzfoozz2(), Lpkgstaticimport.QQType7;, ()V, zzfoozz2, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	} finally {
		JavaCore.setOptions(oldOptions);
	}
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=74295
 */
public void test0069() throws JavaModelException {
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	ICompilationUnit cu= getCompilationUnit("Completion", "src3", "test0069", "Test.java");

	String str = cu.getSource();
	String completeBehind = "icell.p";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertResults(
			"putValue[METHOD_REF]{putValue(), Ltest0069.Test<Ljava.lang.String;>;, (Ljava.lang.String;)V, putValue, (value), " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=77573
 */
public void test0070() throws JavaModelException {
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	ICompilationUnit cu= getCompilationUnit("Completion", "src3", "test0070", "Test.java");

	String str = cu.getSource();
	String completeBehind = "test0070";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertResults(
			"test0070.p[PACKAGE_REF]{test0070.p.*;, test0070.p, null, null, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"test0070[PACKAGE_REF]{test0070.*;, test0070, null, null, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_NAME + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=77573
 */
public void test0071() throws JavaModelException {
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	ICompilationUnit cu= getCompilationUnit("Completion", "src3", "test0071", "Test.java");

	String str = cu.getSource();
	String completeBehind = "test0071.p.Im";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertResults(
			"ImportedClass[TYPE_REF]{test0071.p.ImportedClass;, test0071.p, Ltest0071.p.ImportedClass;, null, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=77573
 */
public void test0072() throws JavaModelException {
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	ICompilationUnit cu= getCompilationUnit("Completion", "src3", "test0072", "Test.java");

	String str = cu.getSource();
	String completeBehind = "test0072.p.ImportedClass.ZZ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertResults(
			"ZZZ1[FIELD_REF]{test0072.p.ImportedClass.ZZZ1;, Ltest0072.p.ImportedClass;, I, ZZZ1, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"ZZZ2[METHOD_IMPORT]{test0072.p.ImportedClass.ZZZ2;, Ltest0072.p.ImportedClass;, ()V, ZZZ2, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"ZZZ2[METHOD_IMPORT]{test0072.p.ImportedClass.ZZZ2;, Ltest0072.p.ImportedClass;, (I)V, ZZZ2, (i), " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=77573
 */
public void test0073() throws JavaModelException {
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	ICompilationUnit cu= getCompilationUnit("Completion", "src3", "test0073", "Test.java");

	String str = cu.getSource();
	String completeBehind = "test0073.p.ImportedClass.Inner.ZZ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertResults(
			"ZZZ1[FIELD_REF]{test0073.p.ImportedClass.Inner.ZZZ1;, Ltest0073.p.ImportedClass$Inner;, I, ZZZ1, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"ZZZ2[METHOD_IMPORT]{test0073.p.ImportedClass.Inner.ZZZ2;, Ltest0073.p.ImportedClass$Inner;, ()V, ZZZ2, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"ZZZ2[METHOD_IMPORT]{test0073.p.ImportedClass.Inner.ZZZ2;, Ltest0073.p.ImportedClass$Inner;, (I)V, ZZZ2, (i), " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=77573
 */
public void test0074() throws JavaModelException {
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	ICompilationUnit cu= getCompilationUnit("Completion", "src3", "test0074", "Test.java");

	String str = cu.getSource();
	String completeBehind = "test0074.p.ImportedClass.Inner.ZZ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertResults(
			"ZZZ1[FIELD_REF]{test0074.p.ImportedClass.Inner.ZZZ1;, Ltest0074.p.ImportedClass$Inner;, I, ZZZ1, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"ZZZ2[METHOD_IMPORT]{test0074.p.ImportedClass.Inner.ZZZ2;, Ltest0074.p.ImportedClass$Inner;, ()V, ZZZ2, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"ZZZ2[METHOD_IMPORT]{test0074.p.ImportedClass.Inner.ZZZ2;, Ltest0074.p.ImportedClass$Inner;, (I)V, ZZZ2, (i), " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
public void test0075() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0075/Test.java",
			"package test0075;\n" +
			"public @QQAnnot class Test {\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "@QQAnnot";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"QQAnnotation[TYPE_REF]{pkgannotations.QQAnnotation, pkgannotations, Lpkgannotations.QQAnnotation;, null, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
public void test0076() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0076/Test.java",
			"package test0076;\n" +
			"public @QQAnnot class Test\n" +
			"");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "@QQAnnot";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"QQAnnotation[TYPE_REF]{pkgannotations.QQAnnotation, pkgannotations, Lpkgannotations.QQAnnotation;, null, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
public void test0077() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0077/Test.java",
			"package test0077;\n" +
			"public @QQAnnot\n" +
			"");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "@QQAnnot";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"QQAnnotation[TYPE_REF]{pkgannotations.QQAnnotation, pkgannotations, Lpkgannotations.QQAnnotation;, null, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
public void test0078() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0078/Test.java",
			"package test0078;\n" +
			"public class Test {\n" +
			"  public @QQAnnot void foo() {\n" +
			"  }\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "@QQAnnot";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"QQAnnotation[TYPE_REF]{pkgannotations.QQAnnotation, pkgannotations, Lpkgannotations.QQAnnotation;, null, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
public void test0079() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0078/Test.java",
			"package test0078;\n" +
			"public class Test {\n" +
			"  public @QQAnnot void foo(\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "@QQAnnot";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"QQAnnotation[TYPE_REF]{pkgannotations.QQAnnotation, pkgannotations, Lpkgannotations.QQAnnotation;, null, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
public void test0080() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0078/Test.java",
			"package test0078;\n" +
			"public class Test {\n" +
			"  public @QQAnnot int var;\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "@QQAnnot";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"QQAnnotation[TYPE_REF]{pkgannotations.QQAnnotation, pkgannotations, Lpkgannotations.QQAnnotation;, null, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
public void test0081() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0078/Test.java",
			"package test0078;\n" +
			"public class Test {\n" +
			"  public @QQAnnot int var\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "@QQAnnot";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"QQAnnotation[TYPE_REF]{pkgannotations.QQAnnotation, pkgannotations, Lpkgannotations.QQAnnotation;, null, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
public void test0082() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0078/Test.java",
			"package test0078;\n" +
			"public class Test {\n" +
			"  void foo(@QQAnnot int i) {}\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "@QQAnnot";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"QQAnnotation[TYPE_REF]{pkgannotations.QQAnnotation, pkgannotations, Lpkgannotations.QQAnnotation;, null, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
public void test0083() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0078/Test.java",
			"package test0078;\n" +
			"public class Test {\n" +
			"  void foo() {@QQAnnot int i}\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "@QQAnnot";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"QQAnnotation[TYPE_REF]{pkgannotations.QQAnnotation, pkgannotations, Lpkgannotations.QQAnnotation;, null, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
public void test0084() throws JavaModelException {
	ICompilationUnit imported = null;
	try {
		imported = getWorkingCopy(
				"/Completion/src3/pkgstaticimport/MyClass0084.java",
				"package pkgstaticimport;\n" +
				"public class MyClass0084 {\n" +
				"   public static int foo() {return 0;}\n" +
				"   public static int foo(int i) {return 0;}\n" +
				"}");
		
		CompletionResult result = complete(
				"/Completion/src3/test0084/Test.java",
				"package test0084;\n" +
				"import static pkgstaticimport.MyClass0084.foo;\n" +
				"public class Test {\n" +
				"  void bar() {\n" +
				"    int i = foo\n" +
				"  }\n" +
				"}",
				"foo");
		
		assertResults(
				"foo[METHOD_REF]{foo(), Lpkgstaticimport.MyClass0084;, ()I, foo, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_NAME + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"foo[METHOD_REF]{foo(), Lpkgstaticimport.MyClass0084;, (I)I, foo, (i), " + (R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_NAME + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(imported != null) {
			imported.discardWorkingCopy();
		}
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=85290
public void test0085() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0085/TestAnnotation.java",
			"package test0085;\n" +
			"public @interface TestAnnotation {\n" +
			"}\n" +
			"@TestAnnotati\n" +
			"class Test2 {\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "@TestAnnotati";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"TestAnnotation[TYPE_REF]{TestAnnotation, test0085, Ltest0085.TestAnnotation;, null, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=85290
public void test0086() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/TestAnnotation.java",
			"public @interface TestAnnotation {\n" +
			"}\n" +
			"@TestAnnotati\n" +
			"class Test2 {\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "@TestAnnotati";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"TestAnnotation[TYPE_REF]{TestAnnotation, , LTestAnnotation;, null, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=85402
public void test0087() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0087/TestAnnotation.java",
			"package test0087;\n" +
			"public @interface TestAnnotation {\n" +
			"}\n" +
			"@\n" +
			"class Test2 {\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "@";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"Test2[TYPE_REF]{Test2, test0087, Ltest0087.Test2;, null, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}\n" +
			"TestAnnotation[TYPE_REF]{TestAnnotation, test0087, Ltest0087.TestAnnotation;, null, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0088() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0088/TestAnnotation.java",
			"package test0088;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"@TestAnnotation(foo)\n" +
			"class Test2 {\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1, Ltest0088.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0089() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0089/TestAnnotation.java",
			"package test0089;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo)\n" +
			"  void bar(){}\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1, Ltest0089.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0090() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0090/TestAnnotation.java",
			"package test0090;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo)\n" +
			"  int var;\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1, Ltest0090.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0091() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0091/TestAnnotation.java",
			"package test0091;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  void bar(){\n" +
			"    @TestAnnotation(foo)\n" +
			"    int var;\n" +
			"  }\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1, Ltest0091.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0092() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0092/TestAnnotation.java",
			"package test0092;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  void bar(int var1, @TestAnnotation(foo) int var2){\n" +
			"  }\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1, Ltest0092.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0093() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0093/TestAnnotation.java",
			"package test0093;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo)\n" +
			"  Test2(){\n" +
			"  }\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1, Ltest0093.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0094() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0094/TestAnnotation.java",
			"package test0094;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"@TestAnnotation(foo\n" +
			"class Test2 {\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1, Ltest0094.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0095() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0095/TestAnnotation.java",
			"package test0095;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo\n" +
			"  void bar(){}\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1, Ltest0095.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0096() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0096/TestAnnotation.java",
			"package test0096;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo\n" +
			"  int var;\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1, Ltest0096.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0097() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0097/TestAnnotation.java",
			"package test0097;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  void bar(){\n" +
			"    @TestAnnotation(foo\n" +
			"    int var;\n" +
			"  }\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1, Ltest0097.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0098() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0098/TestAnnotation.java",
			"package test0098;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  void bar(int var1, @TestAnnotation(foo int var2){\n" +
			"  }\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1, Ltest0098.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0099() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0099/TestAnnotation.java",
			"package test0099;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo\n" +
			"  Test2(){\n" +
			"  }\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1, Ltest0099.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0100() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0100/TestAnnotation.java",
			"package test0100;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"@TestAnnotation(foo=\"\")\n" +
			"class Test2 {\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1, Ltest0100.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0101() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0101/TestAnnotation.java",
			"package test00101;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo=\"\")\n" +
			"  void bar(){}\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1, Ltest0101.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0102() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0102/TestAnnotation.java",
			"package test0102;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo=\"\")\n" +
			"  int var;\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1, Ltest0102.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0103() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0103/TestAnnotation.java",
			"package test00103;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  void bar(){\n" +
			"    @TestAnnotation(foo=\"\")\n" +
			"    int var;\n" +
			"  }\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1, Ltest0103.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0104() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0104/TestAnnotation.java",
			"package test0104;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  void bar(int var1, @TestAnnotation(foo=\"\") int var2){\n" +
			"  }\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1, Ltest0104.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0105() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0105/TestAnnotation.java",
			"package test0105;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo=\"\")\n" +
			"  Test2(){\n" +
			"  }\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1, Ltest0105.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0106() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0106/TestAnnotation.java",
			"package test0106;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"@TestAnnotation(foo=\"\"\n" +
			"class Test2 {\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1, Ltest0106.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0107() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0107/TestAnnotation.java",
			"package test0107;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo=\"\"\n" +
			"  void bar(){}\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1, Ltest0107.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0108() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0108/TestAnnotation.java",
			"package test0108;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo=\"\"\n" +
			"  int var;\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1, Ltest0108.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0109() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0109/TestAnnotation.java",
			"package test0109;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  void bar(){\n" +
			"    @TestAnnotation(foo=\"\"\n" +
			"    int var;\n" +
			"  }\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1, Ltest0109.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0110() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0110/TestAnnotation.java",
			"package test0110;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  void bar(int var1, @TestAnnotation(foo=\"\" int var2){\n" +
			"  }\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1, Ltest0110.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0111() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0111/TestAnnotation.java",
			"package test0111;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo=\"\"\n" +
			"  Test2(){\n" +
			"  }\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1, Ltest0111.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0112() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0112/TestAnnotation.java",
			"package test0112;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"@TestAnnotation(foo1=\"\", foo)\n" +
			"class Test2 {\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2, Ltest0112.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0113() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0113/TestAnnotation.java",
			"package test0113;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo1=\"\", foo)\n" +
			"  void bar(){}\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2, Ltest0113.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0114() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0114/TestAnnotation.java",
			"package test0114;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo1=\"\", foo)\n" +
			"  int var;\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2, Ltest0114.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0115() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0115/TestAnnotation.java",
			"package test0115;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  void bar(){\n" +
			"    @TestAnnotation(foo1=\"\", foo)\n" +
			"    int var;\n" +
			"  }\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2, Ltest0115.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0116() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0116/TestAnnotation.java",
			"package test0116;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  void bar(int var1, @TestAnnotation(foo1=\"\", foo) int var2){\n" +
			"  }\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2, Ltest0116.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0117() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0117/TestAnnotation.java",
			"package test0117;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo1=\"\", foo)\n" +
			"  Test2(){\n" +
			"  }\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2, Ltest0117.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0118() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0118/TestAnnotation.java",
			"package test0118;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"@TestAnnotation(foo1=\"\", foo\n" +
			"class Test2 {\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2, Ltest0118.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0119() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0119/TestAnnotation.java",
			"package test0119;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo1=\"\", foo\n" +
			"  void bar(){}\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2, Ltest0119.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0120() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0120/TestAnnotation.java",
			"package test0120;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo1=\"\", foo\n" +
			"  int var;\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2, Ltest0120.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0121() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0121/TestAnnotation.java",
			"package test0121;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  void bar(){\n" +
			"    @TestAnnotation(foo1=\"\", foo\n" +
			"    int var;\n" +
			"  }\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2, Ltest0121.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0122() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0122/TestAnnotation.java",
			"package test0122;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  void bar(int var1, @TestAnnotation(foo1=\"\", foo int var2){\n" +
			"  }\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2, Ltest0122.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0123() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0123/TestAnnotation.java",
			"package test0123;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo1=\"\", foo\n" +
			"  Test2(){\n" +
			"  }\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2, Ltest0123.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0124() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0124/TestAnnotation.java",
			"package test0124;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"@TestAnnotation(foo1=\"\", foo=\"\")\n" +
			"class Test2 {\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2, Ltest0124.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0125() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0125/TestAnnotation.java",
			"package test0125;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo1=\"\", foo=\"\")\n" +
			"  void bar(){}\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2, Ltest0125.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0126() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0126/TestAnnotation.java",
			"package test0126;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo1=\"\", foo=\"\")\n" +
			"  int var;\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2, Ltest0126.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0127() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0127/TestAnnotation.java",
			"package test0127;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  void bar(){\n" +
			"    @TestAnnotation(foo1=\"\", foo=\"\")\n" +
			"    int var;\n" +
			"  }\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2, Ltest0127.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0128() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0128/TestAnnotation.java",
			"package test0128;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  void bar(int var1, @TestAnnotation(foo1=\"\", foo=\"\") int var2){\n" +
			"  }\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2, Ltest0128.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0129() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0129/TestAnnotation.java",
			"package test0129;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo1=\"\", foo=\"\")\n" +
			"  Test2(){\n" +
			"  }\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2, Ltest0129.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0130() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0130/TestAnnotation.java",
			"package test0130;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"@TestAnnotation(foo1=\"\", foo=\"\"\n" +
			"class Test2 {\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2, Ltest0130.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0131() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0131/TestAnnotation.java",
			"package test0131;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo1=\"\", foo=\"\"\n" +
			"  void bar(){}\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2, Ltest0131.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0132() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0132/TestAnnotation.java",
			"package test0132;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo1=\"\", foo=\"\"\n" +
			"  int var;\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2, Ltest0132.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0133() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0133/TestAnnotation.java",
			"package test0133;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  void bar(){\n" +
			"    @TestAnnotation(foo1=\"\", foo=\"\"\n" +
			"    int var;\n" +
			"  }\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2, Ltest0133.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0134() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0134/TestAnnotation.java",
			"package test0134;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  void bar(int var1, @TestAnnotation(foo1=\"\", foo=\"\" int var2){\n" +
			"  }\n" +
			"}");
	
	
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2, Ltest0134.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0135() throws JavaModelException {
	CompletionResult result = complete(
			"/Completion/src3/test0135/TestAnnotation.java",
			"package test0135;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo1=\"\", foo=\"\"\n" +
			"  Test2(){\n" +
			"  }\n" +
			"}",
			"foo");
	
	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2, Ltest0135.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			result.proposals);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=84554
public void test0136() throws JavaModelException {
	ICompilationUnit enumeration = null;
	try {
		enumeration = getWorkingCopy(
				"/Completion/src3/test0136/Colors.java",
				"package test0136;\n" +
				"public enum Colors {\n" +
				"  RED, BLUE, WHITE;\n" +
				"}");
		
		CompletionResult result = complete(
				"/Completion/src3/test0136/Test.java",
				"package test0136;\n" +
				"public class Test {\n" +
				"  void bar(Colors c) {\n" +
				"    switch(c) {\n" + 
				"      case RED :\n" + 
				"        break;\n" + 
				"    }\n" + 
				"  }\n" +
				"}",
				"RED");
		
		assertResults(
				"expectedTypesSignatures=null\n" +
				"expectedTypesKeys=null",
				result.context);
		
		assertResults(
				"RED[FIELD_REF]{RED, Ltest0136.Colors;, Ltest0136.Colors;, RED, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_NAME + R_UNQUALIFIED + R_NON_RESTRICTED + R_ENUM_CONSTANT) + "}",
				result.proposals);
	} finally {
		if(enumeration != null) {
			enumeration.discardWorkingCopy();
		}
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84554
public void test0137() throws JavaModelException {
	ICompilationUnit enumeration = null;
	try {
		enumeration = getWorkingCopy(
				"/Completion/src3/test0137/Colors.java",
				"package test0137;\n" +
				"public enum Colors {\n" +
				"  RED, BLUE, WHITE;\n" +
				"}");
		
		CompletionResult result = complete(
				"/Completion/src3/test0137/Test.java",
				"package test0137;\n" +
				"public class Test {\n" +
				"  void bar(Colors c) {\n" +
				"    switch(c) {\n" + 
				"      case BLUE :\n" + 
				"      case RED :\n" + 
				"        break;\n" + 
				"    }\n" + 
				"  }\n" +
				"}",
				"RED");
		
		assertResults(
				"expectedTypesSignatures=null\n" +
				"expectedTypesKeys=null",
				result.context);
		
		assertResults(
				"RED[FIELD_REF]{RED, Ltest0137.Colors;, Ltest0137.Colors;, RED, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_NAME + R_UNQUALIFIED + R_NON_RESTRICTED + R_ENUM_CONSTANT) + "}",
				result.proposals);
	} finally {
		if(enumeration != null) {
			enumeration.discardWorkingCopy();
		}
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84554
public void test0138() throws JavaModelException {
	ICompilationUnit enumeration = null;
	try {
		enumeration = getWorkingCopy(
				"/Completion/src3/test0138/Colors.java",
				"package test0138;\n" +
				"public enum Colors {\n" +
				"  RED, BLUE, WHITE;\n" +
				"}");
		
		CompletionResult result = complete(
				"/Completion/src3/test0138/Test.java",
				"package test0138;\n" +
				"public class Test {\n" +
				"  void bar(Colors c) {\n" +
				"    switch(c) {\n" + 
				"      case BLUE :\n" + 
				"        break;\n" + 
				"      case RED :\n" + 
				"        break;\n" + 
				"    }\n" + 
				"  }\n" +
				"}",
				"RED");
		
		assertResults(
				"expectedTypesSignatures=null\n" +
				"expectedTypesKeys=null",
				result.context);
		
		assertResults(
				"RED[FIELD_REF]{RED, Ltest0138.Colors;, Ltest0138.Colors;, RED, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_NAME + R_UNQUALIFIED + R_NON_RESTRICTED + R_ENUM_CONSTANT) + "}",
				result.proposals);
	} finally {
		if(enumeration != null) {
			enumeration.discardWorkingCopy();
		}
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84554
public void test0139() throws JavaModelException {
	ICompilationUnit enumeration = null;
	try {
		enumeration = getWorkingCopy(
				"/Completion/src3/test0139/Colors.java",
				"package test0139;\n" +
				"public enum Colors {\n" +
				"  RED, BLUE, WHITE;\n" +
				"}");
		
		CompletionResult result = complete(
				"/Completion/src3/test0139/Test.java",
				"package test0139;\n" +
				"public class Test {\n" +
				"  void bar(Colors c) {\n" +
				"    switch(c) {\n" + 
				"      case BLUE :\n" + 
				"        break;\n" + 
				"      case RED :\n" + 
				"    }\n" + 
				"  }\n" +
				"}",
				"RED");
		
		assertResults(
				"expectedTypesSignatures=null\n" +
				"expectedTypesKeys=null",
				result.context);
		
		assertResults(
				"RED[FIELD_REF]{RED, Ltest0139.Colors;, Ltest0139.Colors;, RED, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_NAME + R_UNQUALIFIED + R_NON_RESTRICTED + R_ENUM_CONSTANT) + "}",
				result.proposals);
	} finally {
		if(enumeration != null) {
			enumeration.discardWorkingCopy();
		}
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84554
public void test0140() throws JavaModelException {
	ICompilationUnit enumeration = null;
	try {
		enumeration = getWorkingCopy(
				"/Completion/src3/test0140/Colors.java",
				"package test0140;\n" +
				"public enum Colors {\n" +
				"  RED, BLUE, WHITE;\n" +
				"}");
		
		CompletionResult result = complete(
				"/Completion/src3/test0140/Test.java",
				"package test0140;\n" +
				"public class Test {\n" +
				"  void bar(Colors c) {\n" +
				"    switch(c) {\n" + 
				"      case BLUE :\n" + 
				"        break;\n" + 
				"      case RED\n" + 
				"    }\n" + 
				"  }\n" +
				"}",
				"RED");
		
		assertResults(
				"expectedTypesSignatures=null\n" +
				"expectedTypesKeys=null",
				result.context);
		
		assertResults(
				"RED[FIELD_REF]{RED, Ltest0140.Colors;, Ltest0140.Colors;, RED, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_NAME + R_UNQUALIFIED + R_NON_RESTRICTED + R_ENUM_CONSTANT) + "}",
				result.proposals);
	} finally {
		if(enumeration != null) {
			enumeration.discardWorkingCopy();
		}
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84554
public void test0141() throws JavaModelException {
	ICompilationUnit enumeration = null;
	try {
		enumeration = getWorkingCopy(
				"/Completion/src3/test0141/Colors.java",
				"package test0141;\n" +
				"public class Colors {\n" +
				"  public final static int RED = 0, BLUE = 1, WHITE = 3;\n" +
				"}");
		
		CompletionResult result = complete(
				"/Completion/src3/test0141/Test.java",
				"package test0141;\n" +
				"public class Test {\n" +
				"  void bar(Colors c) {\n" +
				"    switch(c) {\n" + 
				"      case BLUE :\n" + 
				"        break;\n" + 
				"      case RED :\n" + 
				"        break;\n" + 
				"    }\n" + 
				"  }\n" +
				"}",
				"RED");
		
		assertResults(
				"expectedTypesSignatures=null\n" +
				"expectedTypesKeys=null",
				result.context);
		
		assertResults(
				"",
				result.proposals);
	} finally {
		if(enumeration != null) {
			enumeration.discardWorkingCopy();
		}
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84554
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=88295
public void test0142() throws JavaModelException {
	ICompilationUnit enumeration = null;
	try {
		enumeration = getWorkingCopy(
				"/Completion/src3/test0142/Colors.java",
				"package test0142;\n" +
				"public enum Colors {\n" +
				"  RED, BLUE, WHITE;\n" +
				"  public static final int RED2 = 0;\n" +
				"}");
		
		CompletionResult result = complete(
				"/Completion/src3/test0142/Test.java",
				"package test0142;\n" +
				"public class Test {\n" +
				"  void bar(Colors REDc) {\n" +
				"    switch(REDc) {\n" + 
				"      case BLUE :\n" + 
				"        break;\n" + 
				"      case RED:\n" + 
				"        break;\n" +
				"    }\n" + 
				"  }\n" +
				"}",
				"RED");
		
		assertResults(
				"expectedTypesSignatures=null\n" +
				"expectedTypesKeys=null",
				result.context);
		
		assertResults(
				"RED[FIELD_REF]{RED, Ltest0142.Colors;, Ltest0142.Colors;, RED, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_NAME + R_UNQUALIFIED + R_NON_RESTRICTED + R_ENUM_CONSTANT) + "}",
				result.proposals);
	} finally {
		if(enumeration != null) {
			enumeration.discardWorkingCopy();
		}
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=88756
public void test0143() throws JavaModelException {
	Hashtable oldCurrentOptions = JavaCore.getOptions();
	ICompilationUnit enumeration = null;
	try {
		Hashtable options = new Hashtable(oldCurrentOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.DISABLED);
		JavaCore.setOptions(options);
		
		enumeration = getWorkingCopy(
				"/Completion/src3/test0143/Colors.java",
				"package test0143;\n" +
				"public enum Colors {\n" +
				"  RED, BLUE, WHITE;\n" +
				"  private Colors(){};\n" +
				"}");
		
		CompletionResult result = complete(
				"/Completion/src3/test0143/Test.java",
				"package test0143;\n" +
				"public class Test {\n" +
				"  void bar() {\n" +
				"    new Colors(\n" + 
				"  }\n" +
				"}",
				"Colors(");
		
		assertResults(
				"expectedTypesSignatures=null\n" +
				"expectedTypesKeys=null",
				result.context);
		
		assertResults(
				"",
				result.proposals);
	} finally {
		if(enumeration != null) {
			enumeration.discardWorkingCopy();
		}
		JavaCore.setOptions(oldCurrentOptions);
	}
}
}
