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
import java.util.Hashtable;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.codeassist.RelevanceConstants;

import junit.framework.*;

public class CompletionTests_1_5 extends AbstractJavaModelTests implements RelevanceConstants {
	Hashtable oldOptions;
public CompletionTests_1_5(String name) {
	super(name);
}
public void setUpSuite() throws Exception {
	super.setUpSuite();
	
	setUpJavaProject("Completion");
	
	this.oldOptions = JavaCore.getOptions();
	Hashtable options = new Hashtable(this.oldOptions);
	options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
	JavaCore.setOptions(options);
	
	waitUntilIndexesReady();
}
public void tearDownSuite() throws Exception {
	deleteProject("Completion");
	
	JavaCore.setOptions(oldOptions);
	
	super.tearDownSuite();
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
	suite.addTest(new CompletionTests_1_5("test0028"));			
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
		"element:String    completion:String    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
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
		"element:Object    completion:Object    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
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
		"element:String    completion:String    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_EXACT_EXPECTED_TYPE),
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
		"element:XZX    completion:XZX    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n"+
		"element:XZXSuper    completion:XZXSuper    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_EXACT_EXPECTED_TYPE),
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
		"element:String    completion:String    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
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
		"element:Object    completion:Object    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
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
		"element:String    completion:String    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_EXACT_EXPECTED_TYPE),
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
		"element:XYX    completion:XYX    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n"+
		"element:XYXSuper    completion:XYXSuper    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_EXACT_EXPECTED_TYPE),
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
		"element:T_1    completion:T_1    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n"+
		"element:T_2    completion:T_2    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
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
		"element:T_1    completion:T_1    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n"+
		"element:T_2    completion:T_2    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n"+
		"element:T_3    completion:T_3    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n"+
		"element:T_4    completion:T_4    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
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
		"element:Z0011.Y0011    completion:Y0011    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
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
		"element:Z0012.Y0012    completion:Y0012    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
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
		"element:Z0013.Y0013    completion:Y0013    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
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
		"element:Z0014.Y0014    completion:Y0014    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_CLASS),
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
		"element:Z0015.Y0015    completion:Y0015    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE) + "\n" +
		"element:Z0015.Y0015I    completion:Y0015I    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_CLASS),
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
		"element:Z0016.Y0016    completion:Y0016    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
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
		"element:Z0017.Y0017    completion:Y0017    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
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
		"element:Z0018.Y0018    completion:Y0018    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
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
		"element:Z0019.Y0019    completion:Y0019    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
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
		"element:Z0020.Y0020    completion:Y0020    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
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
		"element:Z0021Z    completion:Z0021Z    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n"+
		"element:Z0021ZZ    completion:Z0021ZZ    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
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
		"element:Z0022ZZ    completion:Z0022ZZ    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_EXACT_EXPECTED_TYPE)+"\n"+
		"element:Z0022ZZZ    completion:Z0022ZZZ    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
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
		"element:String    completion:String    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
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
		"element:String    completion:String    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
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
		"element:String    completion:String    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
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
		"element:String    completion:String    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
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
		"element:String    completion:String    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
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
		"element:String    completion:String    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_EXACT_EXPECTED_TYPE),
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
		"element:Test.Inner2    completion:Inner2    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_NAME),
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
		"element:ZZX    completion:ZZX    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+ "\n" +
		"element:ZZY    completion:ZZY    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED),
		requestor.getResults());
}
}
