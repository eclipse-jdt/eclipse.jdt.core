/*******************************************************************************
 * Copyright (c) 2002 IBM Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.Hashtable;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.codeassist.RelevanceConstants;
import org.eclipse.jdt.internal.core.JavaModelStatus;

import junit.framework.*;

public class CompletionTests extends AbstractJavaModelTests implements RelevanceConstants {

public CompletionTests(String name) {
	super(name);
}
public void setUpSuite() throws Exception {
	super.setUpSuite();
	
	setUpJavaProject("Completion");
}
public void tearDownSuite() throws Exception {
	deleteProject("Completion");
	
	super.tearDownSuite();
}


public static Test suite() {
	TestSuite suite = new Suite(CompletionTests.class.getName());
	
	suite.addTest(new CompletionTests("testCompletionCaseInsensitive"));
	suite.addTest(new CompletionTests("testCompletionNullRequestor"));
	suite.addTest(new CompletionTests("testCompletionFindExceptions1"));
	suite.addTest(new CompletionTests("testCompletionFindExceptions2"));
	suite.addTest(new CompletionTests("testCompletionFindClass"));
	suite.addTest(new CompletionTests("testCompletionFindClass2"));
	suite.addTest(new CompletionTests("testCompletionFindClassDefaultPackage"));
	suite.addTest(new CompletionTests("testCompletionFindConstructor"));
	suite.addTest(new CompletionTests("testCompletionFindField1"));
	suite.addTest(new CompletionTests("testCompletionFindField2"));
	suite.addTest(new CompletionTests("testCompletionFindField3"));
	suite.addTest(new CompletionTests("testCompletionFindImport1"));
	suite.addTest(new CompletionTests("testCompletionFindImport2"));
	suite.addTest(new CompletionTests("testCompletionFindLocalVariable"));
	suite.addTest(new CompletionTests("testCompletionFindMemberType1"));
	suite.addTest(new CompletionTests("testCompletionFindMemberType2"));
	suite.addTest(new CompletionTests("testCompletionFindMethod1"));
	suite.addTest(new CompletionTests("testCompletionFindMethod2"));
	suite.addTest(new CompletionTests("testCompletionFindMethodInThis"));
	suite.addTest(new CompletionTests("testCompletionFindMethodWhenInProcess"));
	suite.addTest(new CompletionTests("testCompletionFindThisDotField"));
	suite.addTest(new CompletionTests("testCompletionEndOfCompilationUnit"));
	suite.addTest(new CompletionTests("testCompletionOutOfBounds"));
	suite.addTest(new CompletionTests("testCompletionRepeatedType"));
	suite.addTest(new CompletionTests("testCompletionOnClassFile"));
	suite.addTest(new CompletionTests("testCompletionCaseInsensitivePackage"));
	suite.addTest(new CompletionTests("testCompletionFindSuperInterface"));
	suite.addTest(new CompletionTests("testCompletionVisibilityCheckEnabled"));
	suite.addTest(new CompletionTests("testCompletionVisibilityCheckDisabled"));
	suite.addTest(new CompletionTests("testCompletionAmbiguousFieldName"));
	suite.addTest(new CompletionTests("testCompletionAmbiguousFieldName2"));
	suite.addTest(new CompletionTests("testCompletionAmbiguousFieldName3"));
	suite.addTest(new CompletionTests("testCompletionAmbiguousFieldName4"));
	suite.addTest(new CompletionTests("testCompletionPrefixFieldName1"));
	suite.addTest(new CompletionTests("testCompletionPrefixFieldName2"));
	suite.addTest(new CompletionTests("testCompletionPrefixMethodName1"));
	suite.addTest(new CompletionTests("testCompletionPrefixMethodName2"));
	suite.addTest(new CompletionTests("testCompletionPrefixMethodName3"));
	suite.addTest(new CompletionTests("testCompletionMethodDeclaration"));
	suite.addTest(new CompletionTests("testCompletionMethodDeclaration2"));
	suite.addTest(new CompletionTests("testCompletionMethodDeclaration3"));
	suite.addTest(new CompletionTests("testCompletionMethodDeclaration4"));
	suite.addTest(new CompletionTests("testCompletionMethodDeclaration5"));
	suite.addTest(new CompletionTests("testCompletionMethodDeclaration6"));
	suite.addTest(new CompletionTests("testCompletionMethodDeclaration7"));
	suite.addTest(new CompletionTests("testCompletionMethodDeclaration8"));
	suite.addTest(new CompletionTests("testCompletionMethodDeclaration9"));
	suite.addTest(new CompletionTests("testCompletionMethodDeclaration10"));
	suite.addTest(new CompletionTests("testCompletionFieldName"));
	suite.addTest(new CompletionTests("testCompletionLocalName"));
	suite.addTest(new CompletionTests("testCompletionArgumentName"));
	suite.addTest(new CompletionTests("testCompletionCatchArgumentName"));
	suite.addTest(new CompletionTests("testCompletionAmbiguousType"));
	suite.addTest(new CompletionTests("testCompletionAmbiguousType2"));
	suite.addTest(new CompletionTests("testCompletionWithBinaryFolder"));
	suite.addTest(new CompletionTests("testCompletionVariableNameOfArray1"));
	suite.addTest(new CompletionTests("testCompletionVariableNameOfArray2"));
	suite.addTest(new CompletionTests("testCompletionVariableNameOfArray3"));
	suite.addTest(new CompletionTests("testCompletionVariableNameOfArray4"));
	suite.addTest(new CompletionTests("testCompletionVariableNameUnresolvedType"));
	suite.addTest(new CompletionTests("testCompletionSameSuperClass"));
	suite.addTest(new CompletionTests("testCompletionSuperType"));
	suite.addTest(new CompletionTests("testCompletionSuperType2"));
	suite.addTest(new CompletionTests("testCompletionSuperType3"));
	suite.addTest(new CompletionTests("testCompletionSuperType4"));
	suite.addTest(new CompletionTests("testCompletionSuperType5"));
	suite.addTest(new CompletionTests("testCompletionSuperType6"));
	suite.addTest(new CompletionTests("testCompletionSuperType7"));
	suite.addTest(new CompletionTests("testCompletionSuperType8"));
	suite.addTest(new CompletionTests("testCompletionMethodThrowsClause"));
	suite.addTest(new CompletionTests("testCompletionMethodThrowsClause2"));
	suite.addTest(new CompletionTests("testCompletionThrowStatement"));
	suite.addTest(new CompletionTests("testCompletionUnresolvedReturnType"));
	suite.addTest(new CompletionTests("testCompletionUnresolvedParameterType"));
	suite.addTest(new CompletionTests("testCompletionUnresolvedFieldType"));
	
	return suite;
}
/**
 * Ensures that completion is not case sensitive
 */
public void testCompletionCaseInsensitive() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src", "", "CompletionCaseInsensitive.java");
	
	String str = cu.getSource();
	String completeBehind = "Field";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals("should have one class",
		"element:field    completion:field    relevance:"+(R_DEFAULT),
		requestor.getResults());
}
/**
 * Complete a package in a case insensitive way
 */
public void testCompletionCaseInsensitivePackage() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionCaseInsensitivePackage.java");

	String str = cu.getSource();
	String completeBehind = "Ja";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	
	cu.codeComplete(cursorLocation, requestor);
	assertEquals(
		"should have package completions",
		"element:jarpack1    completion:jarpack1    relevance:"+(R_DEFAULT)+"\n" +
		"element:jarpack2    completion:jarpack2    relevance:"+(R_DEFAULT)+"\n" +
		"element:java    completion:java    relevance:"+(R_DEFAULT)+"\n" +
		"element:java.lang    completion:java.lang    relevance:"+(R_DEFAULT),
		requestor.getResults());
}

/**
 * Complete at end of file.
 */
public void testCompletionEndOfCompilationUnit() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src", "", "CompletionEndOfCompilationUnit.java");
	cu.codeComplete(cu.getSourceRange().getOffset() + cu.getSourceRange().getLength(), requestor);
	assertEquals(
		"should have two methods of 'foo'", 
		"element:foo    completion:foo()    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:foo    completion:foo()    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());	
}

/**
 * Complete the type "A" from "new A".
 */
public void testCompletionFindClass() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionFindClass.java");

	String str = cu.getSource();
	String completeBehind = "A";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	
	cu.codeComplete(cursorLocation, requestor);
	assertEquals(
		"should have one class",
		"element:A    completion:A    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:ABC    completion:p1.ABC    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:ABC    completion:p2.ABC    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());	
}

/**
 * The same type must be find only once
 */
public void testCompletionFindClass2() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionFindClass2.java");

	String str = cu.getSource();
	String completeBehind = "PX";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have one classe", 
		"element:PX    completion:pack1.PX    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());
}


/**
 * Complete the type "Default" in the default package example.
 */
public void testCompletionFindClassDefaultPackage() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionDefaultPackage.java");

	String str = cu.getSource();
	String completeBehind = "Def";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have one class", 
		"element:Default    completion:Default    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());	
}

/**
 * Complete the constructor "CompletionFindConstructor" from "new CompletionFindConstructor(".
 */
public void testCompletionFindConstructor() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionFindConstructor.java");

	String str = cu.getSource();
	String completeBehind = "CompletionFindConstructor(";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	assertEquals(
		"should have two constructor (a constructor and an anonymous type)", 
		"element:CompletionFindConstructor    completion:)    relevance:"+(R_DEFAULT)+"\n" +
		"element:CompletionFindConstructor    completion:)    relevance:"+(R_DEFAULT),
		requestor.getResults());
}

/**
 * Complete the exception "Exception" in a catch clause.
 */
public void testCompletionFindExceptions1() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionFindException1.java");

	String str = cu.getSource();
	String completeBehind = "Ex";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals(
		"should have one class", 
		"element:Exception    completion:Exception    relevance:"+(R_DEFAULT + R_CASE + R_EXCEPTION),
		requestor.getResults());
}

/**
 * Complete the exception "Exception" in a throws clause.
 */
public void testCompletionFindExceptions2() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionFindException2.java");

	String str = cu.getSource();
	String completeBehind = "Ex";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have one class",
		"element:Exception    completion:Exception    relevance:"+(R_DEFAULT + R_CASE + R_EXCEPTION),
		requestor.getResults());
}

/**
 * Complete the field "var" from "va";
 */
public void testCompletionFindField1() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionFindField1.java");

	String str = cu.getSource();
	String completeBehind = "va";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals(
		"should have one field: 'var' and one variable: 'var'", 
		"element:var    completion:this.var    relevance:"+(R_DEFAULT + R_CASE)+"\n"+
		"element:var    completion:var    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());	
}

/**
 * Complete the field "var" from "this.va";
 */
public void testCompletionFindField2() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionFindField2.java");

	String str = cu.getSource();
	String completeBehind = "va";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	assertEquals(
		"should have 1 field of starting with 'va'",
		"element:var    completion:var    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());
}

public void testCompletionFindField3() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionFindField3.java");

	String str = cu.getSource();
	String completeBehind = "b.ba";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:bar    completion:bar    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());
}

/**
 * Complete the import, "import pac"
 */
public void testCompletionFindImport1() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionFindImport1.java");

	String str = cu.getSource();
	String completeBehind = "pac";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have three imports \"pack1\" & \"pack1\" & \"pack1.pack3\" & \"pack2\"", 
		"element:pack    completion:pack.*;    relevance:"+(R_DEFAULT + R_CASE)+"\n"+
		"element:pack1    completion:pack1.*;    relevance:"+(R_DEFAULT + R_CASE)+"\n"+
		"element:pack1.pack3    completion:pack1.pack3.*;    relevance:"+(R_DEFAULT + R_CASE)+"\n"+
		"element:pack2    completion:pack2.*;    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());
}

public void testCompletionFindImport2() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionFindImport2.java");

	String str = cu.getSource();
	String completeBehind = "pack1.P";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have six completions",
		"element:PX    completion:pack1.PX;    relevance:"+(R_DEFAULT + R_CASE)+"\n"+
		"element:pack1.pack3    completion:pack1.pack3.*;    relevance:"+(R_DEFAULT),
		requestor.getResults());
}

/**
 * Complete the local variable "var";
 */
public void testCompletionFindLocalVariable() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionFindLocalVariable.java");

	String str = cu.getSource();
	String completeBehind = "va";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	assertEquals(
		"should have one local variable of 'var'", 
		"element:var    completion:var    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());	
}

/**
 * Complete the method call "a.foobar" from "a.fooba";
 */
public void testCompletionFindMethod1() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionFindMethod1.java");

	String str = cu.getSource();
	String completeBehind = "fooba";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	assertEquals(
		"should have two methods of 'foobar'", 
		"element:foobar    completion:foobar()    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:foobar    completion:foobar()    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());		
}


/**
 * Too much Completion match on interface
 */
public void testCompletionFindMethod2() throws JavaModelException {
	
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionFindMethod2.java");

	String str = cu.getSource();
	String completeBehind = "fooba";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have two completions", 
		"element:foobar    completion:foobar()    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:foobar    completion:foobar()    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());	
}


/**
 * Complete the method call "foobar" from "fooba";
 */
public void testCompletionFindMethodInThis() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionFindMethodInThis.java");

	String str = cu.getSource();
	String completeBehind = "fooba";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	assertEquals(
		"should have one method of 'foobar'", 
		"element:foobar    completion:foobar    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());		
}

/**
 * Complete the method call "foobar" from "fooba".  The compilation
 * unit simulates typing in process; ie it has incomplete structure/syntax errors.
 */
public void testCompletionFindMethodWhenInProcess() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionFindMethodInProcess.java");

	String str = cu.getSource();
	String completeBehind = "fooba";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	assertEquals(
		"should have a method of 'foobar'", 
		"element:foobar    completion:foobar()    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());
	cu.close();
}

public void testCompletionFindSuperInterface() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionFindSuperInterface.java");

	String str = cu.getSource();
	String completeBehind = "Super";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
		
	assertEquals(
		"element:SuperClass    completion:SuperClass    relevance:"+(R_DEFAULT + R_CASE)+"\n"+
		"element:SuperInterface    completion:SuperInterface    relevance:"+(R_DEFAULT + R_CASE + R_INTERFACE),
		requestor.getResults());
}

/**
 * Complete the field "bar" from "this.ba"
 */
public void testCompletionFindThisDotField() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionFindThisDotField.java");

	String str = cu.getSource();
	String completeBehind = "this.ba";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	assertEquals(
		"should have one result of 'bar'", 
		"element:bar    completion:bar    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());
}

/**
 * Attempt to do completion with a null requestor
 */
public void testCompletionNullRequestor() throws JavaModelException {
	try {
		ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionFindThisDotField.java");
		cu.codeComplete(5, (ICompletionRequestor)null);
	} catch (IllegalArgumentException iae) {
		return;
	}
	assertTrue("Should not be able to do completion with a null requestor", false);
}

/**
 * Ensures that the code assist features works on class files with associated source.
 */
public void testCompletionOnClassFile() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	IClassFile cu = getClassFile("Completion", "zzz.jar", "jarpack1", "X.class");
	
	String str = cu.getSource();
	String completeBehind = "Obj";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	assertEquals(
		"should have one class", 
		"element:Object    completion:Object    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());
}

/**
 * Test that an out of bounds index causes an exception.
 */
public void testCompletionOutOfBounds() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionOutOfBounds.java");
	try {
		cu.codeComplete(cu.getSource().length() + 1, requestor);
	} catch (JavaModelException e) {
		assertTrue("Should be out of bounds", e.getStatus().getCode() == IJavaModelStatusConstants.INDEX_OUT_OF_BOUNDS);
		return;
	}
	assertTrue("should have failed", false);
}

/**
 * Complete the type "Repeated", "RepeatedOtherType from "Repeated".
 */
public void testCompletionRepeatedType() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionRepeatedType.java");

	String str = cu.getSource();
	String completeBehind = "/**/CompletionRepeated";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	assertEquals(
		"should have two types",
		"element:CompletionRepeatedOtherType    completion:CompletionRepeatedOtherType    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:CompletionRepeatedType    completion:CompletionRepeatedType    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());	
}


public void testCompletionVisibilityCheckEnabled() throws JavaModelException {
	String visibilityCheckID = "org.eclipse.jdt.core.codeComplete.visibilityCheck";
	Hashtable options = JavaCore.getOptions();
	Object visibilityCheckPreviousValue = options.get(visibilityCheckID);
	options.put(visibilityCheckID,"enabled");
	JavaCore.setOptions(options);
	
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionVisibilityCheck.java");

	String str = cu.getSource();
	String completeBehind = "x.p";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	options.put(visibilityCheckID,visibilityCheckPreviousValue);
	JavaCore.setOptions(options);
	assertEquals(
		"should have two methods", 
		"element:protectedFoo    completion:protectedFoo()    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:publicFoo    completion:publicFoo()    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());
}


public void testCompletionVisibilityCheckDisabled() throws JavaModelException {
	String visibilityCheckID = "org.eclipse.jdt.core.codeComplete.visibilityCheck";
	Hashtable options = JavaCore.getOptions();
	Object visibilityCheckPreviousValue = options.get(visibilityCheckID);
	options.put(visibilityCheckID,"disabled");
	JavaCore.setOptions(options);
	
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionVisibilityCheck.java");

	String str = cu.getSource();
	String completeBehind = "x.p";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);
	
	options.put(visibilityCheckID,visibilityCheckPreviousValue);
	JavaCore.setOptions(options);
	assertEquals(
		"should have three methods", 
		"element:privateFoo    completion:privateFoo()    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:protectedFoo    completion:protectedFoo()    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:publicFoo    completion:publicFoo()    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());
}

public void testCompletionAmbiguousFieldName() throws JavaModelException {

	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionAmbiguousFieldName.java");

	String str = cu.getSource();
	String completeBehind = "xBa";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have two completions", 
		"element:xBar    completion:this.xBar    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:xBar    completion:xBar    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());
}

public void testCompletionAmbiguousFieldName2() throws JavaModelException {

	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionAmbiguousFieldName2.java");

	String str = cu.getSource();
	String completeBehind = "xBa";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have two completions", 
		"element:xBar    completion:CompletionAmbiguousFieldName2.this.xBar    relevance:"+(R_DEFAULT + R_CASE)+"\n"+
		"element:xBar    completion:xBar    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());
}

public void testCompletionAmbiguousFieldName3() throws JavaModelException {

	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionAmbiguousFieldName3.java");

	String str = cu.getSource();
	String completeBehind = "xBa";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have two completions", 
		"element:xBar    completion:ClassFoo.this.xBar    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:xBar    completion:xBar    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());
}


public void testCompletionAmbiguousFieldName4() throws JavaModelException {

	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionAmbiguousFieldName4.java");

	String str = cu.getSource();
	String completeBehind = "xBa";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have one completion", 
		"element:xBar    completion:xBar    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());
}


public void testCompletionPrefixFieldName1() throws JavaModelException {

	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionPrefixFieldName1.java");

	String str = cu.getSource();
	String completeBehind = "xBa";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have two completions", 
		"element:xBar    completion:CompletionPrefixFieldName1.this.xBar    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:xBar    completion:xBar    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());
}


public void testCompletionPrefixFieldName2() throws JavaModelException {

	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionPrefixFieldName2.java");

	String str = cu.getSource();
	String completeBehind = "xBa";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have one completion", 
		"element:xBar    completion:xBar    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());
}


public void testCompletionPrefixMethodName1() throws JavaModelException {

	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionPrefixMethodName1.java");

	String str = cu.getSource();
	String completeBehind = "xBa";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have two completions", 
		"element:xBar    completion:CompletionPrefixMethodName1.this.xBar()    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:xBar    completion:xBar()    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());
}


public void testCompletionPrefixMethodName2() throws JavaModelException {

	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionPrefixMethodName2.java");

	String str = cu.getSource();
	String completeBehind = "xBa";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have one completion", 
		"element:xBar    completion:xBar()    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());
}

public void testCompletionPrefixMethodName3() throws JavaModelException {

	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionPrefixMethodName3.java");

	String str = cu.getSource();
	String completeBehind = "xBar(1,";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have one completion", 
		"element:xBar    completion:    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:xBar    completion:CompletionPrefixMethodName3.this.xBar(1,    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());
}

public void testCompletionFindMemberType1() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionFindMemberType1.java");

	String str = cu.getSource();
	String completeBehind = "Inner";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:B1.Inner1    completion:Inner1    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());
}

public void testCompletionFindMemberType2() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionFindMemberType2.java");

	String str = cu.getSource();
	String completeBehind = "Inner";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:B2.Inner2    completion:Inner2    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());
}

public void testCompletionMethodDeclaration() throws JavaModelException {

	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionMethodDeclaration.java");

	String str = cu.getSource();
	String completeBehind = "eq";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have two completions", 
		"element:eqFoo    completion:public int eqFoo(int a,Object b)    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:equals    completion:public boolean equals(Object arg0)    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());
}

public void testCompletionMethodDeclaration2() throws JavaModelException {

	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionMethodDeclaration2.java");

	String str = cu.getSource();
	String completeBehind = "eq";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have two completions", 
		"element:eqFoo    completion:public int eqFoo(int a,Object b)    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:equals    completion:public boolean equals(Object arg0)    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());
}

/**
 * Completion should not propose declarations of method already locally implemented
 */
public void testCompletionMethodDeclaration3() throws JavaModelException {

	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionMethodDeclaration3.java");

	String str = cu.getSource();
	String completeBehind = "eq";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have one completion", 
		"element:equals    completion:public boolean equals(Object arg0)    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());
}


public void testCompletionMethodDeclaration4() throws JavaModelException {

	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionMethodDeclaration4.java");

	String str = cu.getSource();
	String completeBehind = "eq";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have one completion", 
		"element:eqFoo    completion:public int eqFoo(int a,Object b)    relevance:"+(R_DEFAULT + R_CASE)+"\n"+
		"element:equals    completion:public boolean equals(Object arg0)    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());
}

public void testCompletionMethodDeclaration5() throws JavaModelException {

	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionMethodDeclaration5.java");

	String str = cu.getSource();
	String completeBehind = "new CompletionSuperClass() {";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have one completion", 
		"element:CompletionMethodDeclaration5    completion:CompletionMethodDeclaration5    relevance:"+(R_DEFAULT + R_CASE)+"\n"+
		"element:clone    completion:protected Object clone() throws CloneNotSupportedException    relevance:"+(R_DEFAULT + R_CASE)+"\n"+
		"element:eqFoo    completion:public int eqFoo(int a,Object b)    relevance:"+(R_DEFAULT + R_CASE)+"\n"+
		"element:equals    completion:public boolean equals(Object arg0)    relevance:"+(R_DEFAULT + R_CASE)+"\n"+
		"element:finalize    completion:protected void finalize() throws Throwable    relevance:"+(R_DEFAULT + R_CASE)+"\n"+
		"element:hashCode    completion:public int hashCode()    relevance:"+(R_DEFAULT + R_CASE)+"\n"+
		"element:toString    completion:public String toString()    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());
}

public void testCompletionMethodDeclaration6() throws JavaModelException {

	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionMethodDeclaration6.java");

	String str = cu.getSource();
	String completeBehind = "clon";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have one completion", 
		"element:CloneNotSupportedException    completion:CloneNotSupportedException    relevance:"+(R_DEFAULT),
		requestor.getResults());
}

public void testCompletionMethodDeclaration7() throws JavaModelException {

	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionMethodDeclaration7.java");

	String str = cu.getSource();
	String completeBehind = "clon";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have one completion", 
		"element:CloneNotSupportedException    completion:CloneNotSupportedException    relevance:"+(R_DEFAULT)+"\n"+
		"element:clone    completion:protected Object clone() throws CloneNotSupportedException    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());
}

public void testCompletionMethodDeclaration8() throws JavaModelException {

	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionMethodDeclaration8.java");

	String str = cu.getSource();
	String completeBehind = "clon";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have one completion", 
		"element:CloneNotSupportedException    completion:CloneNotSupportedException    relevance:"+(R_DEFAULT)+"\n"+
		"element:clone    completion:protected Object clone() throws CloneNotSupportedException    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());
}

public void testCompletionMethodDeclaration9() throws JavaModelException {

	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionMethodDeclaration9.java");

	String str = cu.getSource();
	String completeBehind = "clon";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have one completion", 
		"element:CloneNotSupportedException    completion:CloneNotSupportedException    relevance:"+(R_DEFAULT)+"\n"+
		"element:clone    completion:protected Object clone() throws CloneNotSupportedException    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());
}

public void testCompletionMethodDeclaration10() throws JavaModelException {

	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionMethodDeclaration10.java");

	String str = cu.getSource();
	String completeBehind = "clon";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have one completion", 
		"element:CloneNotSupportedException    completion:CloneNotSupportedException    relevance:"+(R_DEFAULT)+"\n"+
		"element:clone    completion:protected Object clone() throws CloneNotSupportedException    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());
}

public void testCompletionFieldName() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionFieldName.java");

	String str = cu.getSource();
	String completeBehind = "ClassWithComplexName ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have two completions",
		"element:classWithComplexName    completion:classWithComplexName    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:complexName2    completion:complexName2    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:name    completion:name    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:withComplexName    completion:withComplexName    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());
}


public void testCompletionLocalName() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionLocalName.java");

	String str = cu.getSource();
	String completeBehind = "ClassWithComplexName ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have two completions", 
		"element:classWithComplexName    completion:classWithComplexName    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:complexName2    completion:complexName2    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:name    completion:name    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:withComplexName    completion:withComplexName    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());
}


public void testCompletionArgumentName() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionArgumentName.java");

	String str = cu.getSource();
	String completeBehind = "ClassWithComplexName ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have two completions", 
		"element:classWithComplexName    completion:classWithComplexName    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:complexName2    completion:complexName2    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:name    completion:name    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:withComplexName    completion:withComplexName    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());
}


public void testCompletionCatchArgumentName() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionCatchArgumentName.java");

	String str = cu.getSource();
	String completeBehind = "ex";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have one completion", 
		"element:exception    completion:exception    relevance:10",
		requestor.getResults());
}


public void testCompletionAmbiguousType() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionAmbiguousType.java");

	String str = cu.getSource();
	String completeBehind = "ABC";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have two completions", 
		"element:ABC    completion:p1.ABC    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:ABC    completion:p2.ABC    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());
}


public void testCompletionAmbiguousType2() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionAmbiguousType2.java");

	String str = cu.getSource();
	String completeBehind = "ABC";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have two completions", 
		"element:ABC    completion:ABC    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:ABC    completion:p2.ABC    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());
}


public void testCompletionWithBinaryFolder() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionWithBinaryFolder.java");

	String str = cu.getSource();
	String completeBehind = "My";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have two completions",
		"element:MyClass    completion:MyClass    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:mypackage    completion:mypackage    relevance:"+(R_DEFAULT),
		requestor.getResults());
}


public void testCompletionVariableNameOfArray1() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionVariableNameOfArray1.java");

	String str = cu.getSource();
	String completeBehind = "ob";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have one completion",
		"element:objects    completion:objects    relevance:10",
		requestor.getResults());
}


public void testCompletionVariableNameOfArray2() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionVariableNameOfArray2.java");

	String str = cu.getSource();
	String completeBehind = "cl";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have one completion",
		"element:classes    completion:classes    relevance:10",
		requestor.getResults());
}


public void testCompletionVariableNameOfArray3() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionVariableNameOfArray3.java");

	String str = cu.getSource();
	String completeBehind = "ob";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have one completion",
		"element:objects    completion:objects    relevance:10",
		requestor.getResults());
}


public void testCompletionVariableNameOfArray4() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionVariableNameOfArray4.java");

	String str = cu.getSource();
	String completeBehind = "ob";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have no completion",
		"",
		requestor.getResults());
}


public void testCompletionVariableNameUnresolvedType() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionVariableNameUnresolvedType.java");

	String str = cu.getSource();
	String completeBehind = "ob";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have no completion",
		"",
		requestor.getResults());
}


public void testCompletionSameSuperClass() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionSameSuperClass.java");

	String str = cu.getSource();
	String completeBehind = "bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"should have five completions",
		"element:bar    completion:CompletionSameSuperClass.this.bar    relevance:"+(R_DEFAULT + R_CASE)+"\n"+
		"element:bar    completion:CompletionSameSuperClass.this.bar()    relevance:"+(R_DEFAULT + R_CASE)+"\n"+
		"element:bar    completion:bar    relevance:"+(R_DEFAULT + R_CASE)+"\n"+
		"element:bar    completion:bar()    relevance:"+(R_DEFAULT + R_CASE)+"\n"+
		"element:bar    completion:this.bar    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());
}

public void testCompletionSuperType() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionSuperType.java");

	String str = cu.getSource();
	String completeBehind = "CompletionSuperClass.";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:CompletionSuperClass.Inner    completion:Inner    relevance:"+(R_DEFAULT + R_CASE + R_CLASS),
		requestor.getResults());
}

public void testCompletionSuperType2() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionSuperType2.java");

	String str = cu.getSource();
	String completeBehind = "CompletionSuper";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:CompletionSuperClass    completion:CompletionSuperClass    relevance:"+(R_DEFAULT + R_CASE + R_CLASS)+"\n" +
		"element:CompletionSuperClass2    completion:CompletionSuperClass2    relevance:"+(R_DEFAULT + R_CASE + R_CLASS)+"\n" +
		"element:CompletionSuperInterface    completion:CompletionSuperInterface    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:CompletionSuperInterface2    completion:CompletionSuperInterface2    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:CompletionSuperType    completion:CompletionSuperType    relevance:"+(R_DEFAULT + R_CASE + R_CLASS)+"\n" +
		"element:CompletionSuperType2    completion:CompletionSuperType2    relevance:"+(R_DEFAULT + R_CASE + R_CLASS)+"\n" +
		"element:CompletionSuperType3    completion:CompletionSuperType3    relevance:"+(R_DEFAULT + R_CASE + R_CLASS)+"\n" +
		"element:CompletionSuperType4    completion:CompletionSuperType4    relevance:"+(R_DEFAULT + R_CASE + R_CLASS)+"\n" +
		"element:CompletionSuperType5    completion:CompletionSuperType5    relevance:"+(R_DEFAULT + R_CASE + R_CLASS)+"\n" +
		"element:CompletionSuperType6    completion:CompletionSuperType6    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:CompletionSuperType7    completion:CompletionSuperType7    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:CompletionSuperType8    completion:CompletionSuperType8    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());
}

public void testCompletionSuperType3() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionSuperType3.java");

	String str = cu.getSource();
	String completeBehind = "CompletionSuper";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:CompletionSuperClass    completion:CompletionSuperClass    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:CompletionSuperClass2    completion:CompletionSuperClass2    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:CompletionSuperInterface    completion:CompletionSuperInterface    relevance:"+(R_DEFAULT + R_CASE + R_INTERFACE)+"\n" +
		"element:CompletionSuperInterface2    completion:CompletionSuperInterface2    relevance:"+(R_DEFAULT + R_CASE + R_INTERFACE)+"\n" +
		"element:CompletionSuperType    completion:CompletionSuperType    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:CompletionSuperType2    completion:CompletionSuperType2    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:CompletionSuperType3    completion:CompletionSuperType3    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:CompletionSuperType4    completion:CompletionSuperType4    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:CompletionSuperType5    completion:CompletionSuperType5    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:CompletionSuperType6    completion:CompletionSuperType6    relevance:"+(R_DEFAULT + R_CASE + R_INTERFACE)+"\n" +
		"element:CompletionSuperType7    completion:CompletionSuperType7    relevance:"+(R_DEFAULT + R_CASE + R_INTERFACE)+"\n" +
		"element:CompletionSuperType8    completion:CompletionSuperType8    relevance:"+(R_DEFAULT + R_CASE + R_INTERFACE),
		requestor.getResults());
}

public void testCompletionSuperType4() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionSuperType4.java");

	String str = cu.getSource();
	String completeBehind = "CompletionSuperClass2.Inner";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:CompletionSuperClass2.InnerClass    completion:InnerClass    relevance:"+(R_DEFAULT + R_CASE + R_CLASS)+"\n" +
		"element:CompletionSuperClass2.InnerInterface    completion:InnerInterface    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());
}

public void testCompletionSuperType5() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionSuperType5.java");

	String str = cu.getSource();
	String completeBehind = "CompletionSuperInterface2.Inner";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:CompletionSuperInterface2.InnerClass    completion:InnerClass    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:CompletionSuperInterface2.InnerInterface    completion:InnerInterface    relevance:"+(R_DEFAULT + R_CASE + R_INTERFACE),
		requestor.getResults());
}

public void testCompletionSuperType6() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionSuperType6.java");

	String str = cu.getSource();
	String completeBehind = "CompletionSuper";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:CompletionSuperClass    completion:CompletionSuperClass    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:CompletionSuperClass2    completion:CompletionSuperClass2    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:CompletionSuperInterface    completion:CompletionSuperInterface    relevance:"+(R_DEFAULT + R_CASE + R_INTERFACE)+"\n" +
		"element:CompletionSuperInterface2    completion:CompletionSuperInterface2    relevance:"+(R_DEFAULT + R_CASE + R_INTERFACE)+"\n" +
		"element:CompletionSuperType    completion:CompletionSuperType    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:CompletionSuperType2    completion:CompletionSuperType2    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:CompletionSuperType3    completion:CompletionSuperType3    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:CompletionSuperType4    completion:CompletionSuperType4    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:CompletionSuperType5    completion:CompletionSuperType5    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:CompletionSuperType6    completion:CompletionSuperType6    relevance:"+(R_DEFAULT + R_CASE + R_INTERFACE)+"\n" +
		"element:CompletionSuperType7    completion:CompletionSuperType7    relevance:"+(R_DEFAULT + R_CASE + R_INTERFACE)+"\n" +
		"element:CompletionSuperType8    completion:CompletionSuperType8    relevance:"+(R_DEFAULT + R_CASE + R_INTERFACE),
		requestor.getResults());
}

public void testCompletionSuperType7() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionSuperType7.java");

	String str = cu.getSource();
	String completeBehind = "CompletionSuperClass2.Inner";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:CompletionSuperClass2.InnerClass    completion:InnerClass    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:CompletionSuperClass2.InnerInterface    completion:InnerInterface    relevance:"+(R_DEFAULT + R_CASE + R_INTERFACE),
		requestor.getResults());
}

public void testCompletionSuperType8() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionSuperType8.java");

	String str = cu.getSource();
	String completeBehind = "CompletionSuperInterface2.Inner";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:CompletionSuperInterface2.InnerClass    completion:InnerClass    relevance:"+(R_DEFAULT + R_CASE)+"\n" +
		"element:CompletionSuperInterface2.InnerInterface    completion:InnerInterface    relevance:"+(R_DEFAULT + R_CASE + R_INTERFACE),
		requestor.getResults());
}

public void testCompletionMethodThrowsClause() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionMethodThrowsClause.java");

	String str = cu.getSource();
	String completeBehind = "Ex";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:Exception    completion:Exception    relevance:"+(R_DEFAULT + R_CASE + R_EXCEPTION),
		requestor.getResults());
}

public void testCompletionMethodThrowsClause2() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionMethodThrowsClause2.java");

	String str = cu.getSource();
	String completeBehind = "Ex";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:Exception    completion:java.lang.Exception    relevance:"+(R_DEFAULT + R_CASE + R_EXCEPTION),
		requestor.getResults());
}

public void testCompletionThrowStatement() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionThrowStatement.java");

	String str = cu.getSource();
	String completeBehind = "Ex";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:Exception    completion:Exception    relevance:"+(R_DEFAULT + R_CASE + R_EXCEPTION),
		requestor.getResults());
}

public void testCompletionUnresolvedReturnType() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionUnresolvedReturnType.java");

	String str = cu.getSource();
	String completeBehind = "bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:barPlus    completion:barPlus()    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());
}

public void testCompletionUnresolvedParameterType() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionUnresolvedParameterType.java");

	String str = cu.getSource();
	String completeBehind = "bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:barPlus    completion:barPlus()    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());
}

public void testCompletionUnresolvedFieldType() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu= getCompilationUnit("Completion", "src", "", "CompletionUnresolvedFieldType.java");

	String str = cu.getSource();
	String completeBehind = "bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals(
		"element:barPlus    completion:barPlus()    relevance:"+(R_DEFAULT + R_CASE),
		requestor.getResults());
}


}
