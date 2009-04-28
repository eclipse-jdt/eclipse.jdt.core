/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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
protected void assertResults(String expected, String actual) {
	try {
		assertEquals(expected, actual);
	} catch(ComparisonFailure c) {
		System.out.println(actual);
		System.out.println();
		throw c;
	}
}
public void testCodeSnippetAssistForClassFile() throws JavaModelException {
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(false,false,true);
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

	int tokenStart = snippet.lastIndexOf("var");
	int tokenEnd = tokenStart + "var".length();
	assertResults(
		"var[LOCAL_VARIABLE_REF]{var, null, I, var, ["+tokenStart+", "+tokenEnd+"], "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n"+
		"varX[LOCAL_VARIABLE_REF]{varX, null, I, varX, ["+tokenStart+", "+tokenEnd+"], "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n"+
		"varY[LOCAL_VARIABLE_REF]{varY, null, I, varY, ["+tokenStart+", "+tokenEnd+"], "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n"+
		"varsc[LOCAL_VARIABLE_REF]{varsc, null, Laa.bb.cc.SuperClass;, varsc, ["+tokenStart+", "+tokenEnd+"], "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}",
		requestor.getResults());
}


public void testCodeSnippetAssistForCompilationUnit() throws JavaModelException {
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(false,false,true);
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

	int tokenStart = snippet.lastIndexOf("var");
	int tokenEnd = tokenStart + "var".length();
	assertResults(
		"Victory[TYPE_REF]{Victory, aa.bb.cc, LVictory;, null, ["+tokenStart+", "+tokenEnd+"], "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n"+
		"var[LOCAL_VARIABLE_REF]{var, null, I, var, ["+tokenStart+", "+tokenEnd+"], "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n"+
		"varX[LOCAL_VARIABLE_REF]{varX, null, I, varX, ["+tokenStart+", "+tokenEnd+"], "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n"+
		"varY[LOCAL_VARIABLE_REF]{varY, null, I, varY, ["+tokenStart+", "+tokenEnd+"], "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n"+
		"varsc[LOCAL_VARIABLE_REF]{varsc, null, Laa.bb.cc.SuperClass;, varsc, ["+tokenStart+", "+tokenEnd+"], "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}",
		requestor.getResults());
}

public void testCodeSnippetAssistForClassFileWithSource() throws JavaModelException {
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(false,false,true);
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

	int tokenStart = snippet.lastIndexOf("var");
	int tokenEnd = tokenStart + "var".length();
	assertResults(
		"Victory[TYPE_REF]{Victory, aa.bb.cc, LVictory;, null, ["+tokenStart+", "+tokenEnd+"], "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n"+
		"var[LOCAL_VARIABLE_REF]{var, null, I, var, ["+tokenStart+", "+tokenEnd+"], "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n"+
		"varX[LOCAL_VARIABLE_REF]{varX, null, I, varX, ["+tokenStart+", "+tokenEnd+"], "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n"+
		"varY[LOCAL_VARIABLE_REF]{varY, null, I, varY, ["+tokenStart+", "+tokenEnd+"], "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n"+
		"varsc[LOCAL_VARIABLE_REF]{varsc, null, Laa.bb.cc.SuperClass;, varsc, ["+tokenStart+", "+tokenEnd+"], "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}",
		requestor.getResults());
}


public void testCodeSnippetAssistForCompilationUnitWithoutSource() throws JavaModelException {
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(false,false,true);
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

	int tokenStart = snippet.lastIndexOf("var");
	int tokenEnd = tokenStart + "var".length();
	assertResults(
		"var[LOCAL_VARIABLE_REF]{var, null, I, var, ["+tokenStart+", "+tokenEnd+"], "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n"+
		"varX[LOCAL_VARIABLE_REF]{varX, null, I, varX, ["+tokenStart+", "+tokenEnd+"], "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n"+
		"varY[LOCAL_VARIABLE_REF]{varY, null, I, varY, ["+tokenStart+", "+tokenEnd+"], "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n"+
		"varsc[LOCAL_VARIABLE_REF]{varsc, null, Laa.bb.cc.SuperClass;, varsc, ["+tokenStart+", "+tokenEnd+"], "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}",
		requestor.getResults());
}


public void testCodeSnippetAssistForClassFileInInnerClass() throws JavaModelException {
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(false,false,true);
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

	int tokenStart = snippet.lastIndexOf("var");
	int tokenEnd = tokenStart + "var".length();
	assertResults(
		"var[LOCAL_VARIABLE_REF]{var, null, I, var, ["+tokenStart+", "+tokenEnd+"], "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n"+
		"varX[LOCAL_VARIABLE_REF]{varX, null, I, varX, ["+tokenStart+", "+tokenEnd+"], "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n"+
		"varY[LOCAL_VARIABLE_REF]{varY, null, I, varY, ["+tokenStart+", "+tokenEnd+"], "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n"+
		"varsc[LOCAL_VARIABLE_REF]{varsc, null, Laa.bb.cc.SuperClass;, varsc, ["+tokenStart+", "+tokenEnd+"], "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}",
		requestor.getResults());
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=50686
 */
public void testCodeSnippetAssistForClassFileInInterface() throws JavaModelException {
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(false,false,true);
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

	int tokenStart = snippet.lastIndexOf("var");
	int tokenEnd = tokenStart + "var".length();
	assertResults(
		"var[LOCAL_VARIABLE_REF]{var, null, I, var, ["+tokenStart+", "+tokenEnd+"], "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n"+
		"varX[LOCAL_VARIABLE_REF]{varX, null, I, varX, ["+tokenStart+", "+tokenEnd+"], "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n"+
		"varY[LOCAL_VARIABLE_REF]{varY, null, I, varY, ["+tokenStart+", "+tokenEnd+"], "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n"+
		"varsc[LOCAL_VARIABLE_REF]{varsc, null, LSuperClass;, varsc, ["+tokenStart+", "+tokenEnd+"], "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}",
		requestor.getResults());
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=62201
 */
public void testCodeSnippetAssistForClassFileInInterface2() throws JavaModelException {
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(false,false,true);
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

	int tokenStart = snippet.lastIndexOf("var");
	int tokenEnd = tokenStart + "var".length();
	assertResults(
		"var[LOCAL_VARIABLE_REF]{var, null, I, var, ["+tokenStart+", "+tokenEnd+"], "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n"+
		"varFoo[METHOD_REF]{varFoo(), Lxx.yy.MyInterface2;, ()V, varFoo, ["+tokenStart+", "+tokenEnd+"], "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n"+
		"varX[LOCAL_VARIABLE_REF]{varX, null, I, varX, ["+tokenStart+", "+tokenEnd+"], "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n"+
		"varY[LOCAL_VARIABLE_REF]{varY, null, I, varY, ["+tokenStart+", "+tokenEnd+"], "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n"+
		"varsc[LOCAL_VARIABLE_REF]{varsc, null, LSuperClass;, varsc, ["+tokenStart+", "+tokenEnd+"], "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}",
		requestor.getResults());
}
public void testCodeSnippetAssistForClassFileWithDollar() throws JavaModelException {
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(false,false,true);
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

	int tokenStart = snippet.lastIndexOf("var");
	int tokenEnd = tokenStart + "var".length();
	assertResults(
		"varX[LOCAL_VARIABLE_REF]{varX, null, I, varX, ["+tokenStart+", "+tokenEnd+"], "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n"+
		"varY[LOCAL_VARIABLE_REF]{varY, null, I, varY, ["+tokenStart+", "+tokenEnd+"], "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}",
		requestor.getResults());
}

public void testCodeSnippetAssistInsideNumber() throws JavaModelException {
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(false,false,true);
	IClassFile cf = getClassFile("SnippetCompletion", "class-folder", "aa.bb.cc", "AClass.class");
	IType type = cf.getType();

	String snippet =
		"new double[] {1.2, 3.\n";

	char[][] typeNames = {"SuperClass".toCharArray(), "int".toCharArray()};
	char[][] names = {"varsc".toCharArray(), "var".toCharArray()};
	int[] modifiers = {ClassFileConstants.AccDefault, ClassFileConstants.AccFinal};

	type.codeComplete(snippet.toCharArray(), -1, snippet.length()-2, typeNames, names, modifiers, false, requestor);

	assertResults(
		"",
		requestor.getResults());
}

}
