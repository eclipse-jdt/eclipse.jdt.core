/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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
import org.eclipse.jdt.internal.compiler.lookup.CompilerModifiers;

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
	TestSuite suite = new Suite(SnippetCompletionTests.class.getName());
	
	suite.addTest(new SnippetCompletionTests("testCodeSnippetAssistForClassFile"));
	suite.addTest(new SnippetCompletionTests("testCodeSnippetAssistForCompilationUnit"));
	suite.addTest(new SnippetCompletionTests("testCodeSnippetAssistForClassFileWithSource"));
	suite.addTest(new SnippetCompletionTests("testCodeSnippetAssistForCompilationUnitWithoutSource"));
	suite.addTest(new SnippetCompletionTests("testCodeSnippetAssistForClassFileInInnerClass"));

	return suite;
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
	int[] modifiers = {CompilerModifiers.AccDefault, CompilerModifiers.AccFinal};
	
	type.codeComplete(snippet.toCharArray(), -1, snippet.length()-2, typeNames, names, modifiers, false, requestor);
	
	assertEquals(
		"should have 5 completions",
		"element:var    completion:var    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n"+
		"element:varX    completion:varX    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n"+
		"element:varY    completion:varY    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n"+
		"element:varsc    completion:varsc    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n"+
		"element:void    completion:void    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
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
	int[] modifiers = {CompilerModifiers.AccDefault, CompilerModifiers.AccFinal};

	String insertAftrer = "Victory{}";
	String s = cu.getSource();
	int insertion = -1;
	if(s != null)
		insertion = s.lastIndexOf(insertAftrer) + insertAftrer.length();

	type.codeComplete(snippet.toCharArray(), insertion, snippet.length()-2, typeNames, names, modifiers, false, requestor);
	
	assertEquals(
		"should have 6 completions",
		"element:Victory    completion:Victory    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n"+
		"element:var    completion:var    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n"+
		"element:varX    completion:varX    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n"+
		"element:varY    completion:varY    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n"+
		"element:varsc    completion:varsc    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n"+
		"element:void    completion:void    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
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
	int[] modifiers = {CompilerModifiers.AccDefault, CompilerModifiers.AccFinal};

	String insertAftrer = "Victory{}";
	String s = cf.getSource();
	int insertion = -1;
	if(s != null)
		insertion = s.lastIndexOf(insertAftrer) + insertAftrer.length();
	
	type.codeComplete(snippet.toCharArray(), insertion, snippet.length()-2, typeNames, names, modifiers, false, requestor);
	
	assertEquals(
		"should have 6 completions",
		"element:Victory    completion:Victory    relevance:"+(R_DEFAULT + R_INTERESTING + R_UNQUALIFIED)+"\n"+
		"element:var    completion:var    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n"+
		"element:varX    completion:varX    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n"+
		"element:varY    completion:varY    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n"+
		"element:varsc    completion:varsc    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n"+
		"element:void    completion:void    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE) ,
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
	int[] modifiers = {CompilerModifiers.AccDefault, CompilerModifiers.AccFinal};

	int insertion = -1;

	type.codeComplete(snippet.toCharArray(), insertion, snippet.length()-2, typeNames, names, modifiers, false, requestor);
	
	assertEquals(
		"should have 5 completions",
		"element:var    completion:var    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n"+
		"element:varX    completion:varX    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n"+
		"element:varY    completion:varY    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n"+
		"element:varsc    completion:varsc    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n"+
		"element:void    completion:void    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
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
	int[] modifiers = {CompilerModifiers.AccDefault, CompilerModifiers.AccFinal};

	String insertAftrer = "Victory{}";
	String s = cf.getSource();
	int insertion = -1;
	if(s != null)
		insertion = s.lastIndexOf(insertAftrer) + insertAftrer.length();
	
	type.codeComplete(snippet.toCharArray(), insertion, snippet.length()-2, typeNames, names, modifiers, false, requestor);
	
	assertEquals(
		"should have 5 completions",
		"element:var    completion:var    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n"+
		"element:varX    completion:varX    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n"+
		"element:varY    completion:varY    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n"+
		"element:varsc    completion:varsc    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED)+"\n"+
		"element:void    completion:void    relevance:"+(R_DEFAULT + R_INTERESTING + R_CASE),
		requestor.getResults());
}

}
