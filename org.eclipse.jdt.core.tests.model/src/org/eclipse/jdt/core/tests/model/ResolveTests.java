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

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.core.JavaModelStatus;

import junit.framework.*;

public class ResolveTests extends AbstractJavaModelTests {

public ResolveTests(String name) {
	super(name);
}
public void setUpSuite() throws Exception {
	super.setUpSuite();
	
	setUpJavaProject("Resolve");
}
public void tearDownSuite() throws Exception {
	deleteProject("Resolve");
	
	super.tearDownSuite();
}


public static Test suite() {
	TestSuite suite = new Suite(ResolveTests.class.getName());
	
	// empty selection tests
	suite.addTest(new ResolveTests("testResolveEmptySelection"));
	suite.addTest(new ResolveTests("testResolveEmptySelection2"));
	suite.addTest(new ResolveTests("testResolveEmptySelectionOnMethod"));
	
	// selection tests
	suite.addTest(new ResolveTests("testResolveInvalidResolve"));
	suite.addTest(new ResolveTests("testResolveConstructor"));
	suite.addTest(new ResolveTests("testResolveMethod"));
	suite.addTest(new ResolveTests("testResolveField"));
	suite.addTest(new ResolveTests("testResolvePackage"));
	suite.addTest(new ResolveTests("testResolveClass1"));
	suite.addTest(new ResolveTests("testResolveClass2"));
	suite.addTest(new ResolveTests("testResolveClass3"));
	suite.addTest(new ResolveTests("testResolveClass4"));
	suite.addTest(new ResolveTests("testResolveClass5"));
	suite.addTest(new ResolveTests("testResolveClass6"));
	suite.addTest(new ResolveTests("testResolveInterface"));
	suite.addTest(new ResolveTests("testResolveInClassFileWithSource"));
	suite.addTest(new ResolveTests("testResolveInClassFileWithoutSource"));
	suite.addTest(new ResolveTests("testResolveQualifiedType"));
	suite.addTest(new ResolveTests("testResolvePartiallyQualifiedType"));
	suite.addTest(new ResolveTests("testResolveUnicode"));
	suite.addTest(new ResolveTests("testNegativeResolveUnicode"));
	suite.addTest(new ResolveTests("testResolveArrayLength"));
	suite.addTest(new ResolveTests("testResolveConstructorDeclaration"));
	suite.addTest(new ResolveTests("testResolveMethodDeclaration"));
	suite.addTest(new ResolveTests("testResolveFieldDeclaration"));
	suite.addTest(new ResolveTests("testResolveTypeDeclaration"));
	suite.addTest(new ResolveTests("testResolveMemberTypeDeclaration"));
	suite.addTest(new ResolveTests("testResolveMemberTypeDeclaration2"));
	suite.addTest(new ResolveTests("testResolveMethodWithIncorrectParameter"));
	suite.addTest(new ResolveTests("testResolveExplicitSuperConstructorCall"));
	suite.addTest(new ResolveTests("testResolveExplicitThisConstructorCall"));
	suite.addTest(new ResolveTests("testResolveMessageSendOnBaseType"));
	suite.addTest(new ResolveTests("testResolveMethodWithInnerTypeInClassFile"));
	suite.addTest(new ResolveTests("testResolveTypeInComment"));
	suite.addTest(new ResolveTests("testResolveImport"));
	suite.addTest(new ResolveTests("testResolveConstructorCallOfMemberType"));
	suite.addTest(new ResolveTests("testResolveLocalName1"));
	suite.addTest(new ResolveTests("testResolveLocalName2"));
	suite.addTest(new ResolveTests("testResolveArgumentName1"));
	suite.addTest(new ResolveTests("testResolveArgumentName2"));
	suite.addTest(new ResolveTests("testResolveCatchArgumentName1"));
	suite.addTest(new ResolveTests("testResolveCatchArgumentName2"));
	suite.addTest(new ResolveTests("testResolveAbstractMethod"));
	suite.addTest(new ResolveTests("testResolveInnerClassAsParamater"));
	return suite;
}

/**
 * Resolve empty selection
 */
public void testResolveEmptySelection() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveTypeEmptySelection.java");

	String str = cu.getSource();
	String selectAt = "ect";
	String selection = "";
	int start = str.lastIndexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = cu.codeSelect(start, length);
	
	assertTrue("should have one type", elements.length == 1 && 
		elements[0].getElementName().equals("Object") &&
		elements[0] instanceof IType);	
}
/**
 * Resolve empty selection
 */
public void testResolveEmptySelection2() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveTypeEmptySelection2.java");

	String str = cu.getSource();
	String selectAt = "Obj";
	String selection = "";
	int start = str.lastIndexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = cu.codeSelect(start, length);
	
	assertTrue("should have one type", elements.length == 1 && 
		elements[0].getElementName().equals("Object") &&
		elements[0] instanceof IType);	
}
/**
 * Resolve empty selection
 */
public void testResolveEmptySelectionOnMethod() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveEmptySelectionOnMethod.java");

	String str = cu.getSource();
	String selectAt = "oo";
	String selection = "";
	int start = str.lastIndexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = cu.codeSelect(start, length);
	
	assertTrue("should have one method", elements.length == 1 && 
		elements[0].getElementName().equals("foo") &&
		elements[0] instanceof IMethod);	
}
/**
 * Resolve the method
 */
public void testResolveMethodWithIncorrectParameter() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveMethodWithIncorrectParameter.java");
	
	String source = cu.getSource();
	
	int start = source.indexOf("foo(\"String");
	int length = "foo".length();
	
	IJavaElement[] elements = cu.codeSelect(start, length);
	assertTrue("should have one method of 'foo'",
		elements.length == 1 &&
		elements[0].getElementName().equals("foo") &&
		elements[0] instanceof IMethod);		
}
/**
 * Resolse explicit super constructor call
 */
public void testResolveExplicitSuperConstructorCall() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveExplicitSuperConstructorCall.java");

	String str = cu.getSource();
	String selectAt = "super(";
	String selection = "super";
	int start = str.indexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = cu.codeSelect(start, length);

	assertTrue("should have one type", elements.length == 1 && 
		elements[0].getElementName().equals("SuperClass") &&
		elements[0] instanceof IMethod);	
}
/**
 * Resolse explicit this constructor call
 */
public void testResolveExplicitThisConstructorCall() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveExplicitThisConstructorCall.java");

	String str = cu.getSource();
	String selectAt = "this(";
	String selection = "this";
	int start = str.indexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = cu.codeSelect(start, length);

	assertTrue("should have one type", elements.length == 1 && 
		elements[0].getElementName().equals("ResolveExplicitThisConstructorCall") &&
		elements[0] instanceof IMethod);	
}
/**
 * Try to resolve message send on base type.
 */
public void testResolveMessageSendOnBaseType() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveMessageSendOnBaseType.java");

	String str = cu.getSource();
	String selectAt = "hello";
	String selection = "hello";
	IJavaElement[] elements = cu.codeSelect(
		str.indexOf(selectAt), 
		selection.length());

	assertTrue("should have nothing", elements.length == 0);	
}
/**
 * Resolve method in inner type.
 */
public void testResolveMethodWithInnerTypeInClassFile() throws JavaModelException {
	IClassFile cu = getClassFile("Resolve", "zzz.jar", "", "MyClass$Inner.class");

	String str = cu.getSource();
	String selectAt = "test";
	String selection = "test";
	int start = str.indexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = cu.codeSelect(start, length);

	assertTrue("should have one method", elements.length == 1 && 
		elements[0].getElementName().equals("test") &&
		elements[0] instanceof IMethod);	
}
/**
 * Resolve type in comment.
 */
public void testResolveTypeInComment() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveTypeInComment.java");

	String str = cu.getSource();
	String selectAt = "X */";
	String selection = "X";
	int start = str.indexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = cu.codeSelect(start, length);
	
	assertTrue("should have one type", elements.length == 1 &&
		elements[0] instanceof IType &&
		((IType)elements[0]).getFullyQualifiedName().equals("p2.X"));	
}
/**
 * Resolve in import
 */
public void testResolveImport() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveImport.java");

	String str = cu.getSource();
	String selectAt = "ImportedClass";
	String selection = "ImportedClass";
	int start = str.lastIndexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = cu.codeSelect(start, length);
	
	assertTrue("should have one type", elements.length == 1 && 
		elements[0].getElementName().equals("ImportedClass") &&
		elements[0] instanceof IType);	
}
/**
 * Resolve constructor call */
public void testResolveConstructorCallOfMemberType() throws JavaModelException {
	IClassFile cf = getClassFile("Resolve", "class-folder", "", "ResolveConstructorCallOfMemberType.class");
	
	String str = cf.getSource();
	String selectAt = "Inner";
	String selection = "Inner";
	int start = str.lastIndexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = cf.codeSelect(start, length);
	
	assertTrue("should have one method", elements.length == 1 && 
		elements[0].getElementName().equals("Inner") &&
		elements[0] instanceof IMethod);	
}
/**
 * Resolve a local declaration name
 */
public void testResolveLocalName1() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalName.java");

	String str = cu.getSource();
	String selectAt = "var1";
	String selection = "var1";
	int start = str.lastIndexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = cu.codeSelect(start, length);
	
	assertTrue("should have one type", elements.length == 1 && 
		elements[0].getElementName().equals("Object") &&
		elements[0] instanceof IType);	
}
/**
 * Resolve a local declaration name with base type
 */
public void testResolveLocalName2() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalName.java");

	String str = cu.getSource();
	String selectAt = "var2";
	String selection = "var2";
	int start = str.lastIndexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = cu.codeSelect(start, length);
	
	assertTrue("should have nothing", elements.length == 0);	
}
/**
 * Resolve an argument name
 */
public void testResolveArgumentName1() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveArgumentName.java");

	String str = cu.getSource();
	String selectAt = "var1";
	String selection = "var1";
	int start = str.lastIndexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = cu.codeSelect(start, length);
	
	assertTrue("should have one type", elements.length == 1 && 
		elements[0].getElementName().equals("Object") &&
		elements[0] instanceof IType);	
}
/**
 * Resolve an argument name with base type
 */
public void testResolveArgumentName2() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveArgumentName.java");

	String str = cu.getSource();
	String selectAt = "var2";
	String selection = "var2";
	int start = str.lastIndexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = cu.codeSelect(start, length);
	
	assertTrue("should have nothing", elements.length == 0);	
}

/**
 * Resolve an argument name inside catch statement
 */
public void testResolveCatchArgumentName1() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveCatchArgumentName.java");

	String str = cu.getSource();
	String selectAt = "var1";
	String selection = "var1";
	int start = str.lastIndexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = cu.codeSelect(start, length);
	
	assertTrue("should have one type", elements.length == 1 && 
		elements[0].getElementName().equals("Object") &&
		elements[0] instanceof IType);	
}
/**
 * Resolve an argument name inside catch statement with base type
 */
public void testResolveCatchArgumentName2() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveCatchArgumentName.java");

	String str = cu.getSource();
	String selectAt = "var2";
	String selection = "var2";
	int start = str.lastIndexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = cu.codeSelect(start, length);
	
	assertTrue("should have nothing", elements.length == 0);	
}
/**
 * Attempt to resolve outside of the range of the compilation unit.
 */
public void testResolveInvalidResolve() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "p1", "X.java");
	try {
		cu.codeSelect(-1, 10); 
	} catch (JavaModelException jme) {
		assertTrue("Incorrect status on the JavaModelException", jme.getStatus().getCode() == JavaModelStatus.INDEX_OUT_OF_BOUNDS);
		return;
	}
	assertTrue("Exception should have been thrown for out of bounds resolution", false);
}
/**
 * Resolve a constructor
 */
public void testResolveConstructor() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveConstructor.java");
	
	String str = cu.getSource();
	String selectAt = "ResolveConstructor(\"";
	String selection = "ResolveConstructor";
	int start = str.lastIndexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = cu.codeSelect(start, length);
	
	assertTrue("should have one method", elements.length == 1 && 
		elements[0].getElementName().equals("ResolveConstructor") &&
		elements[0] instanceof IMethod &&
		((IMethod)elements[0]).getParameterTypes()[0].equals("QString;"));	
}
/**
 * Resolve the method "foo"
 */
public void testResolveMethod() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveMethod.java");
	
	String str = cu.getSource();
	String selectAt = "foo(\"";
	String selection = "foo";
	int start = str.lastIndexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = cu.codeSelect(start, length);

	assertTrue("should have one method of 'foo'",
		elements.length == 1 &&
		elements[0].getElementName().equals("foo") &&
		elements[0] instanceof IMethod &&
		((IMethod)elements[0]).getParameterTypes()[0].equals("QString;"));		
}
/**
 * Resolve the field "foo"
 */
public void testResolveField() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveField.java");
	
	String str = cu.getSource();
	String selectAt = "foo =";
	String selection = "foo";
	int start = str.lastIndexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = cu.codeSelect(start, length);
	
	assertTrue("should have one field of 'foo'",
			elements.length == 1 &&
			elements[0].getElementName().equals("foo") &&
			elements[0] instanceof IField);	
}
/**
 * Resolve the package "java.lang"
 */
public void testResolvePackage() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolvePackage.java");
	
	String str = cu.getSource();
	String selectAt = "lang";
	String selection = "lang";
	int start = str.lastIndexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = cu.codeSelect(start, length);
	
	assertTrue("should have one package of 'java.lang'",
		elements.length == 1 &&
		elements[0].getElementName().equals("java.lang") &&
		elements[0] instanceof IPackageFragment);	
}
/**
 * Resolve the class 'X' (field type).
 */
public void testResolveClass1() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveClass1.java");
	
	String str = cu.getSource();
	String selectAt = "X";
	String selection = "X";
	int start = str.lastIndexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = cu.codeSelect(start, length);
	
	assertTrue("should have one class", elements.length == 1 && 
		elements[0].getElementName().equals("X") &&
		elements[0] instanceof IType);
}
/**
 * Resolve the class 'X' (local variable type).
 */
public void testResolveClass2() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveClass2.java");
	
	String str = cu.getSource();
	String selectAt = "X";
	String selection = "X";
	int start = str.lastIndexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = cu.codeSelect(start, length);
	
	assertTrue("should have one class", elements.length == 1 && 
		elements[0].getElementName().equals("X") &&
		elements[0] instanceof IType);
}
/**
 * Resolve the class 'X'(array initializer type).
 */
public void testResolveClass3() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveClass3.java");
	
	String str = cu.getSource();
	String selectAt = "X[]{";
	String selection = "X";
	int start = str.lastIndexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = cu.codeSelect(start, length);
	
	assertTrue("should have one class", elements.length == 1 && 
		elements[0].getElementName().equals("X") &&
		elements[0] instanceof IType);
}
/**
 * Resolve the class 'X' (return type).
 */
public void testResolveClass4() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveClass4.java");
	
	String str = cu.getSource();
	String selectAt = "X";
	String selection = "X";
	int start = str.lastIndexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = cu.codeSelect(start, length);
	
	assertTrue("should have one class", elements.length == 1 && 
		elements[0].getElementName().equals("X") &&
		elements[0] instanceof IType);
}
/**
 * Resolve the class 'X' (method argument).
 */
public void testResolveClass5() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveClass5.java");
	
	String str = cu.getSource();
	String selectAt = "X";
	String selection = "X";
	int start = str.lastIndexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = cu.codeSelect(start, length);
	
	assertTrue("should have one class", elements.length == 1 && 
		elements[0].getElementName().equals("X") &&
		elements[0] instanceof IType);
}
/**
 * Resolve the class 'SuperClass' (super class).
 */
public void testResolveClass6() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveClass6.java");
	
	String str = cu.getSource();
	String selectAt = "X";
	String selection = "X";
	int start = str.lastIndexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = cu.codeSelect(start, length);
	
	assertTrue("should have one class", elements.length == 1 && 
		elements[0].getElementName().equals("X") &&
		elements[0] instanceof IType);
}
/**
 * Resolve the interface "Y"
 */
public void testResolveInterface() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveInterface.java");
	
	String str = cu.getSource();
	String selectAt = "Y";
	String selection = "Y";
	int start = str.lastIndexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = cu.codeSelect(start, length);
	
	assertTrue("should have one interface", elements.length == 1 && 
		elements[0].getElementName().equals("Y") &&
		elements[0] instanceof IType);	
}
/**
 * Tests code resolve on a class file with attached source.
 */
public void testResolveInClassFileWithSource() throws JavaModelException {
	IClassFile cu = getClassFile("Resolve", "p3.jar", "p3", "X.class");

	String str = cu.getSource();
	String selectAt = "Object";
	String selection = "Object";
	int start = str.indexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = cu.codeSelect(start, length);
	
	assertTrue(elements != null && elements.length == 1 &&
		elements[0].getElementName().equals("Object") &&
		elements[0].getElementType() == IJavaElement.TYPE);
}
/**
 * Tests code resolve on a class file without attached source.
 */
public void testResolveInClassFileWithoutSource() throws JavaModelException {
	IClassFile cu = getClassFile("Resolve", "p4.jar", "p4", "X.class");

	String selection = "Object";
	int start = 34;
	int length = selection.length();
	IJavaElement[] elements = cu.codeSelect(start, length);
	
	assertTrue(elements != null && elements.length == 0);
}
/**
 * Resolve the qualified type "java.lang.Object"
 */
public void testResolveQualifiedType() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveQualifiedType.java");
	
	String str = cu.getSource();
	String selectAt = "java.lang.Object";
	String selection = "java.lang.Object";
	int start = str.indexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = cu.codeSelect(start, length);
	
	assertTrue("should have one class", elements.length == 1);
	IJavaElement element = elements[0];
	assertTrue("Should be an IType", element instanceof IType);
	IType type = (IType)element;
	assertTrue("should be java.lang.Object",
		type.getElementName().equals("Object") &&
		type.getPackageFragment().getElementName().equals("java.lang"));	
}
/**
 * Try to resolve the qualified type "lang.Object"
 */
public void testResolvePartiallyQualifiedType() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolvePartiallyQualifiedType.java");
	
	String str = cu.getSource();
	String selectAt = "lang.Object";
	String selection = "lang.Object";
	int start = str.indexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = cu.codeSelect(start, length);
	
	assertTrue("should have no result class", elements != null && elements.length == 0);
}
/**
 * Resolve the type "java.lang. \u0053ring"
 */
public void testResolveUnicode() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveUnicode.java");
	
	String str = cu.getSource();
	String selectAt = "java.lang.\\u0053tring";
	String selection = "java.lang.\\u0053tring";
	int start = str.indexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = cu.codeSelect(start, length);
	
	assertTrue("should have one class 'java.lang.\u0053tring'", elements.length == 1);
	IJavaElement element = elements[0];
	assertTrue("should be an IType", element instanceof IType);
	IType type = (IType)element;
	assertEquals("unexpected element name", "\u0053tring", type.getElementName());
	assertEquals("unexpected parent name", "java.lang", type.getPackageFragment().getElementName());
}
/**
 * Tries to resolve the type "lang. \u0053tring" which doesn't exist.
 */
public void testNegativeResolveUnicode() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveUnicode.java");
	
	String str = cu.getSource();
	String selectAt = "lang.\\u0053tring";
	String selection = "lang.\\u0053tring";
	int start = str.indexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = cu.codeSelect(start, length);
	
	assertTrue("should have no class", elements != null && elements.length == 0);
}
/**
 * Resolve the field "length" of an array
 */
public void testResolveArrayLength() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveArrayLength.java");
	
	String str = cu.getSource();
	String selectAt = "length";
	String selection = "length";
	int start = str.indexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = cu.codeSelect(start, length);

	assertTrue("should have no field 'length'", elements != null &&  elements.length == 0);	
}
/**
 * Resolve constructor declaration
 */
public void testResolveConstructorDeclaration() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveConstructorDeclaration.java");
	
	String str = cu.getSource();
	String selectAt = "ResolveConstructorDeclaration(i";
	String selection = "ResolveConstructorDeclaration";
	int start = str.indexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = cu.codeSelect(start, length);
	
	assertTrue("should have one method", elements.length == 1 && 
		elements[0].getElementName().equals("ResolveConstructorDeclaration") &&
		elements[0] instanceof IMethod &&
		((IMethod)elements[0]).getParameterTypes()[0].equals("I"));	
}
/**
 * Resolve method declaration
 */
public void testResolveMethodDeclaration() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveMethodDeclaration.java");
	
	String str = cu.getSource();
	String selectAt = "foo(i";
	String selection = "foo";
	int start = str.indexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = cu.codeSelect(start, length);
	
	assertTrue("should have one method", elements.length == 1 && 
		elements[0].getElementName().equals("foo") &&
		elements[0] instanceof IMethod &&
		((IMethod)elements[0]).getParameterTypes()[0].equals("I"));	
}
/**
 * Resolve field declaration
 */
public void testResolveFieldDeclaration() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveFieldDeclaration.java");
	
	String str = cu.getSource();
	String selectAt = "foo";
	String selection = "foo";
	int start = str.indexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = cu.codeSelect(start, length);
	
	assertTrue("should have one field", elements.length == 1 && 
		elements[0].getElementName().equals("foo") &&
		elements[0] instanceof IField);	
}
/**
 * Resolve type declaration
 */
public void testResolveTypeDeclaration() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveTypeDeclaration.java");
	
	String str = cu.getSource();
	String selectAt = "OtherType";
	String selection = "OtherType";
	int start = str.indexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = cu.codeSelect(start, length);
	
	assertTrue("should have one type", elements.length == 1 && 
		elements[0].getElementName().equals("OtherType") &&
		elements[0] instanceof IType);	
}
/**
 * Resolve member type declaration
 */
public void testResolveMemberTypeDeclaration() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveMemberTypeDeclaration1.java");
	
	String str = cu.getSource();
	String selectAt = "MemberInterface";
	String selection = "MemberInterface";
	int start = str.indexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = cu.codeSelect(start, length);
	
	assertTrue("should have one type", elements.length == 1 && 
		elements[0].getElementName().equals("MemberInterface") &&
		elements[0] instanceof IType);	
}
/**
 * Resolve member type declaration located in default package
 */
public void testResolveMemberTypeDeclaration2() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveMemberTypeDeclaration2.java");
	
	String str = cu.getSource();
	String selectAt = "MemberOfMember";
	String selection = "MemberOfMember";
	int start = str.indexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = cu.codeSelect(start, length);
	
	assertTrue("should have one type", elements.length == 1 && 
		elements[0].getElementName().equals("MemberOfMember") &&
		elements[0] instanceof IType);	
}
/**
 * Resolve default abstrart method
 * bugs http://dev.eclipse.org/bugs/show_bug.cgi?id=23594
 */
public void testResolveAbstractMethod() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveAbstractMethod.java");
	
	String str = cu.getSource();
	String selectAt = "foo";
	String selection = "foo";
	int start = str.indexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = cu.codeSelect(start, length);
	
	assertTrue("should have one method", elements.length == 1 && 
		elements[0].getElementName().equals("foo") &&
		elements[0] instanceof IMethod &&
		((IMethod)elements[0]).getDeclaringType().getElementName().equals("SuperInterface"));
}
/**
 * bugs http://dev.eclipse.org/bugs/show_bug.cgi?id=25687
 */
public void testResolveInnerClassAsParamater() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveInnerClassAsParamater.java");
	
	String str = cu.getSource();
	String selectAt = "foo";
	String selection = "foo";
	int start = str.lastIndexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = cu.codeSelect(start, length);
	
	assertTrue("should have one method", elements.length == 1 && 
		elements[0].getElementName().equals("foo") &&
		elements[0] instanceof IMethod);
}
}
