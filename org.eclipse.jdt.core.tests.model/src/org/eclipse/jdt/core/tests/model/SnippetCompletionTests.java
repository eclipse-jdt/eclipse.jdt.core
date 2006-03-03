/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.codeassist.RelevanceConstants;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

import junit.framework.*;

public class SnippetCompletionTests extends AbstractJavaModelTests implements RelevanceConstants {
	
public SnippetCompletionTests(String name) {
	super(name);
}
public void setUpSuite() throws Exception {
	super.setUpSuite();
	
	setUpJavaProject("SnippetCompletion");
}
public void tearDownSuite() throws Exception {
	deleteProject("SnippetCompletion");
	
	super.tearDownSuite();
}

public static Test suite() {
	return buildModelTestSuite(SnippetCompletionTests.class);
	/*
	TestSuite suite = new Suite(SnippetCompletionTests.class.getName());
	
	suite.addTest(new SnippetCompletionTests("testCodeSnippetAssistForClassFile"));
	suite.addTest(new SnippetCompletionTests("testCodeSnippetAssistForCompilationUnit"));
	suite.addTest(new SnippetCompletionTests("testCodeSnippetAssistForClassFileWithSource"));
	suite.addTest(new SnippetCompletionTests("testCodeSnippetAssistForCompilationUnitWithoutSource"));
	suite.addTest(new SnippetCompletionTests("testCodeSnippetAssistForClassFileInInnerClass"));
	suite.addTest(new SnippetCompletionTests("testCodeSnippetAssistForClassFileInInterface"));
	suite.addTest(new SnippetCompletionTests("testCodeSnippetAssistForClassFileInInterface2"));
	suite.addTest(new SnippetCompletionTests("testCodeSnippetAssistForClassFileWithDollar"));
	suite.addTest(new SnippetCompletionTests("testCodeSnippetAssistInsideNumber"));
	
	return suite;
	*/
}

public void testCodeSnippetAssistForClassFile() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	IClassFile cf = getClassFile("SnippetCompletion", "class-folder", "aa.bb.cc", "AClass.class");
	IType type = cf.getType();
	
	String snippet = 
		"int varX;\n" +
		"int varY;\n" +
		"var";
		
	char[][] typeNames = {"SuperClass".toCharArray(), "int".toCharArray()};
	char[][] names = {"varsc".toCharArray(), "var".toCharArray()};
	int[] modifiers = {ClassFileConstants.AccDefault, ClassFileConstants.AccFinal};
	
	type.codeComplete(snippet.toCharArray(), -1, snippet.length()-2, typeNames, names, modifiers, false, requestor);
	
	assertEquals(
		"should have 5 completions",
		"element:var    completion:var    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"\n"+
		"element:varX    completion:varX    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"\n"+
		"element:varY    completion:varY    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"\n"+
		"element:varsc    completion:varsc    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"\n"+
		"element:void    completion:void    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED),
		requestor.getResults());
}


public void testCodeSnippetAssistForCompilationUnit() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("SnippetCompletion", "src", "aa.bb.cc", "BClass.java");
	IType type = cu.getTypes()[0];
	String snippet = 
		"int varX;\n" +
		"int varY;\n" +
		"var";
		
	char[][] typeNames = {"SuperClass".toCharArray(), "int".toCharArray()};
	char[][] names = {"varsc".toCharArray(), "var".toCharArray()};
	int[] modifiers = {ClassFileConstants.AccDefault, ClassFileConstants.AccFinal};

	String insertAftrer = "Victory{}";
	String s = cu.getSource();
	int insertion = -1;
	if(s != null)
		insertion = s.lastIndexOf(insertAftrer) + insertAftrer.length();

	type.codeComplete(snippet.toCharArray(), insertion, snippet.length()-2, typeNames, names, modifiers, false, requestor);
	
	assertEquals(
		"should have 6 completions",
		"element:Victory    completion:Victory    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED)+"\n"+
		"element:var    completion:var    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"\n"+
		"element:varX    completion:varX    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"\n"+
		"element:varY    completion:varY    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"\n"+
		"element:varsc    completion:varsc    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"\n"+
		"element:void    completion:void    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED),
		requestor.getResults());
}

public void testCodeSnippetAssistForClassFileWithSource() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	IClassFile cf = getClassFile("SnippetCompletion", "class-folder", "aa.bb.cc", "CClass.class");
	IType type = cf.getType();
	
	String snippet = 
		"int varX;\n" +
		"int varY;\n" +
		"var";
		
	char[][] typeNames = {"SuperClass".toCharArray(), "int".toCharArray()};
	char[][] names = {"varsc".toCharArray(), "var".toCharArray()};
	int[] modifiers = {ClassFileConstants.AccDefault, ClassFileConstants.AccFinal};

	String insertAftrer = "Victory{}";
	String s = cf.getSource();
	int insertion = -1;
	if(s != null)
		insertion = s.lastIndexOf(insertAftrer) + insertAftrer.length();
	
	type.codeComplete(snippet.toCharArray(), insertion, snippet.length()-2, typeNames, names, modifiers, false, requestor);
	
	assertEquals(
		"should have 6 completions",
		"element:Victory    completion:Victory    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED)+"\n"+
		"element:var    completion:var    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"\n"+
		"element:varX    completion:varX    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"\n"+
		"element:varY    completion:varY    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"\n"+
		"element:varsc    completion:varsc    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"\n"+
		"element:void    completion:void    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) ,
		requestor.getResults());
}


public void testCodeSnippetAssistForCompilationUnitWithoutSource() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("SnippetCompletion", "src", "aa.bb.cc", "BClass.java");
	IType type = cu.getTypes()[0];
	
	String snippet = 
		"int varX;\n" +
		"int varY;\n" +
		"var";
		
	char[][] typeNames = {"SuperClass".toCharArray(), "int".toCharArray()};
	char[][] names = {"varsc".toCharArray(), "var".toCharArray()};
	int[] modifiers = {ClassFileConstants.AccDefault, ClassFileConstants.AccFinal};

	int insertion = -1;

	type.codeComplete(snippet.toCharArray(), insertion, snippet.length()-2, typeNames, names, modifiers, false, requestor);
	
	assertEquals(
		"should have 5 completions",
		"element:var    completion:var    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"\n"+
		"element:varX    completion:varX    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"\n"+
		"element:varY    completion:varY    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"\n"+
		"element:varsc    completion:varsc    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"\n"+
		"element:void    completion:void    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED),
		requestor.getResults());
}


public void testCodeSnippetAssistForClassFileInInnerClass() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	IClassFile cf = getClassFile("SnippetCompletion", "class-folder", "aa.bb.cc", "AClass$Inner.class");
	IType type = cf.getType();
	
	String snippet = 
		"int varX;\n" +
		"int varY;\n" +
		"var";
		
	char[][] typeNames = {"SuperClass".toCharArray(), "int".toCharArray()};
	char[][] names = {"varsc".toCharArray(), "var".toCharArray()};
	int[] modifiers = {ClassFileConstants.AccDefault, ClassFileConstants.AccFinal};

	String insertAftrer = "Victory{}";
	String s = cf.getSource();
	int insertion = -1;
	if(s != null)
		insertion = s.lastIndexOf(insertAftrer) + insertAftrer.length();
	
	type.codeComplete(snippet.toCharArray(), insertion, snippet.length()-2, typeNames, names, modifiers, false, requestor);
	
	assertEquals(
		"should have 5 completions",
		"element:var    completion:var    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"\n"+
		"element:varX    completion:varX    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"\n"+
		"element:varY    completion:varY    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"\n"+
		"element:varsc    completion:varsc    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"\n"+
		"element:void    completion:void    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED),
		requestor.getResults());
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=50686
 */
public void testCodeSnippetAssistForClassFileInInterface() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	IClassFile cf = getClassFile("SnippetCompletion", "class-folder", "xx.yy", "MyInterface.class");
	IType type = cf.getType();
	
	String snippet = 
		"int varX;\n" +
		"int varY;\n" +
		"var";
		
	char[][] typeNames = {"SuperClass".toCharArray(), "int".toCharArray()};
	char[][] names = {"varsc".toCharArray(), "var".toCharArray()};
	int[] modifiers = {ClassFileConstants.AccDefault, ClassFileConstants.AccFinal};
	
	type.codeComplete(snippet.toCharArray(), -1, snippet.length()-2, typeNames, names, modifiers, false, requestor);
	
	assertEquals(
		"should have 5 completions",
		"element:var    completion:var    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"\n"+
		"element:varX    completion:varX    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"\n"+
		"element:varY    completion:varY    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"\n"+
		"element:varsc    completion:varsc    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"\n"+
		"element:void    completion:void    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED),
		requestor.getResults());
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=62201
 */
public void testCodeSnippetAssistForClassFileInInterface2() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	IClassFile cf = getClassFile("SnippetCompletion", "class-folder", "xx.yy", "MyInterface2.class");
	IType type = cf.getType();
	
	String snippet = 
		"int varX;\n" +
		"int varY;\n" +
		"var";
		
	char[][] typeNames = {"SuperClass".toCharArray(), "int".toCharArray()};
	char[][] names = {"varsc".toCharArray(), "var".toCharArray()};
	int[] modifiers = {ClassFileConstants.AccDefault, ClassFileConstants.AccFinal};
	
	type.codeComplete(snippet.toCharArray(), -1, snippet.length()-2, typeNames, names, modifiers, false, requestor);
	
	assertEquals(
		"should have 5 completions",
		"element:var    completion:var    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"\n"+
		"element:varFoo    completion:varFoo()    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"\n"+
		"element:varX    completion:varX    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"\n"+
		"element:varY    completion:varY    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"\n"+
		"element:varsc    completion:varsc    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"\n"+
		"element:void    completion:void    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED),
		requestor.getResults());
}
public void testCodeSnippetAssistForClassFileWithDollar() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	IClassFile cf = getClassFile("SnippetCompletion", "class-folder", "test00XX", "Test.class");
	IType type = cf.getType();
	
	String snippet = 
		"int varX;\n" +
		"int varY;\n" +
		"var";
		
	char[][] typeNames = {};
	char[][] names = {};
	int[] modifiers = {};

	type.codeComplete(snippet.toCharArray(), -1, snippet.length()-2, typeNames, names, modifiers, false, requestor);
	
	assertEquals(
		"should have 3 completions",
		"element:varX    completion:varX    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"\n"+
		"element:varY    completion:varY    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"\n"+
		"element:void    completion:void    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) ,
		requestor.getResults());
}

public void testCodeSnippetAssistInsideNumber() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	IClassFile cf = getClassFile("SnippetCompletion", "class-folder", "aa.bb.cc", "AClass.class");
	IType type = cf.getType();
	
	String snippet = 
		"new double[] {1.2, 3.\n";
		
	char[][] typeNames = {"SuperClass".toCharArray(), "int".toCharArray()};
	char[][] names = {"varsc".toCharArray(), "var".toCharArray()};
	int[] modifiers = {ClassFileConstants.AccDefault, ClassFileConstants.AccFinal};
	
	type.codeComplete(snippet.toCharArray(), -1, snippet.length()-2, typeNames, names, modifiers, false, requestor);
	
	assertEquals(
		"should have 0 completions",
		"",
		requestor.getResults());
}

}
