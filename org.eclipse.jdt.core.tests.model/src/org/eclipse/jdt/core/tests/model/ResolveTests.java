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

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.ICompilationUnit;

import junit.framework.*;

public class ResolveTests extends AbstractJavaModelTests {


public static Test suite() {
	return new Suite(ResolveTests.class);
}

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
	
	assertElementsEqual(
		"Unexpected elements",
		"",
		elements
	);
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
	
	assertElementsEqual(
		"Unexpected elements",
		"foo [in SuperInterface [in SuperInterface.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
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
	
	assertElementsEqual(
		"Unexpected elements",
		"Object [in Object.class [in java.lang [in " + getExternalJCLPath(). toString() + " [in Resolve]]]]",
		elements
	);
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
	
	assertElementsEqual(
		"Unexpected elements",
		"",
		elements
	);
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

	assertElementsEqual(
		"Unexpected elements",
		"",
		elements
	);
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
	
	assertElementsEqual(
		"Unexpected elements",
		"Object [in Object.class [in java.lang [in " + getExternalJCLPath(). toString() + " [in Resolve]]]]",
		elements
	);
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
	
	assertElementsEqual(
		"Unexpected elements",
		"",
		elements
	);
}
/**
 * bugs http://dev.eclipse.org/bugs/show_bug.cgi?id=24626
 */
public void testResolveCatchArgumentType1() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveCatchArgumentType1.java");
	
	String str = cu.getSource();
	String selectAt = "Y1";
	String selection = "Y1";
	int start = str.lastIndexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = cu.codeSelect(start, length);
	
	assertElementsEqual(
		"Unexpected elements",
		"Y1 [in X1 [in X1.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/**
 * bugs http://dev.eclipse.org/bugs/show_bug.cgi?id=24626
 */
public void testResolveCatchArgumentType2() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveCatchArgumentType2.java");
	
	String str = cu.getSource();
	String selectAt = "Y1";
	String selection = "Y1";
	int start = str.lastIndexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = cu.codeSelect(start, length);
	
	assertElementsEqual(
		"Unexpected elements",
		"Y1 [in X1 [in X1.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
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
	
	assertElementsEqual(
		"Unexpected elements",
		"X [in X.java [in p1 [in src [in Resolve]]]]",
		elements
	);
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
	
	assertElementsEqual(
		"Unexpected elements",
		"X [in X.java [in p1 [in src [in Resolve]]]]",
		elements
	);
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
	
	assertElementsEqual(
		"Unexpected elements",
		"X [in X.java [in p1 [in src [in Resolve]]]]",
		elements
	);
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
	
	assertElementsEqual(
		"Unexpected elements",
		"X [in X.java [in p1 [in src [in Resolve]]]]",
		elements
	);
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
	
	assertElementsEqual(
		"Unexpected elements",
		"X [in X.java [in p1 [in src [in Resolve]]]]",
		elements
	);
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
	
	assertElementsEqual(
		"Unexpected elements",
		"X [in X.java [in p1 [in src [in Resolve]]]]",
		elements
	);
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
	
	assertElementsEqual(
		"Unexpected elements",
		"ResolveConstructor [in ResolveConstructor [in ResolveConstructor.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/**
 * Resolve constructor call
 */
public void testResolveConstructorCallOfMemberType() throws JavaModelException {
	IClassFile cf = getClassFile("Resolve", "class-folder", "", "ResolveConstructorCallOfMemberType.class");
	
	String str = cf.getSource();
	String selectAt = "Inner";
	String selection = "Inner";
	int start = str.lastIndexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = cf.codeSelect(start, length);
	
	assertElementsEqual(
		"Unexpected elements",
		"Inner [in Inner [in ResolveConstructorCallOfMemberType$Inner.class [in <default> [in class-folder [in Resolve]]]]]",
		elements
	);
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
	
	assertElementsEqual(
		"Unexpected elements",
		"ResolveConstructorDeclaration [in ResolveConstructorDeclaration [in ResolveConstructorDeclaration.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
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
	
	assertElementsEqual(
		"Unexpected elements",
		"Object [in Object.class [in java.lang [in " + getExternalJCLPath(). toString() + " [in Resolve]]]]",
		elements
	);
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
	
	assertElementsEqual(
		"Unexpected elements",
		"Object [in Object.class [in java.lang [in " + getExternalJCLPath(). toString() + " [in Resolve]]]]",
		elements
	);
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
	
	assertElementsEqual(
		"Unexpected elements",
		"foo [in ResolveEmptySelectionOnMethod [in ResolveEmptySelectionOnMethod.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
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

	assertElementsEqual(
		"Unexpected elements",
		"SuperClass [in SuperClass [in SuperClass.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
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

	assertElementsEqual(
		"Unexpected elements",
		"ResolveExplicitThisConstructorCall [in ResolveExplicitThisConstructorCall [in ResolveExplicitThisConstructorCall.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
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
	
	assertElementsEqual(
		"Unexpected elements",
		"foo [in ResolveField [in ResolveField.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
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
	
	assertElementsEqual(
		"Unexpected elements",
		"foo [in ResolveFieldDeclaration [in ResolveFieldDeclaration.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
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
	
	assertElementsEqual(
		"Unexpected elements",
		"ImportedClass [in ImportedClass.java [in a.b [in src [in Resolve]]]]",
		elements
	);
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
	
	assertElementsEqual(
		"Unexpected elements",
		"",
		elements
	);
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
	
	assertElementsEqual(
		"Unexpected elements",
		"Object [in Object.class [in java.lang [in " + getExternalJCLPath(). toString() + " [in Resolve]]]]",
		elements
	);
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
	
	assertElementsEqual(
		"Unexpected elements",
		"foo [in ResolveInnerClassAsParamater [in ResolveInnerClassAsParamater.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
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
	
	assertElementsEqual(
		"Unexpected elements",
		"Y [in Y.java [in p1 [in src [in Resolve]]]]",
		elements
	);
}
/**
 * Attempt to resolve outside of the range of the compilation unit.
 */
public void testResolveInvalidResolve() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "p1", "X.java");
	try {
		cu.codeSelect(-1, 10); 
	} catch (IllegalArgumentException e) {
		assertEquals("Incorrect message on assertion failure", "Selected range (-1, 9) is not located in supplied source range (0, 36)", e.getMessage());
		return;
	}
	assertTrue("Exception should have been thrown for out of bounds resolution", false);
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
	
	assertElementsEqual(
		"Unexpected elements",
		"Object [in Object.class [in java.lang [in " + getExternalJCLPath(). toString() + " [in Resolve]]]]",
		elements
	);
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
	
	assertElementsEqual(
		"Unexpected elements",
		"",
		elements
	);
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
	
	assertElementsEqual(
		"Unexpected elements",
		"MemberInterface [in ResolveMemberTypeDeclaration1 [in ResolveMemberTypeDeclaration1.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
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
	
	assertElementsEqual(
		"Unexpected elements",
		"MemberOfMember [in Member [in ResolveMemberTypeDeclaration2 [in ResolveMemberTypeDeclaration2.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
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

	assertElementsEqual(
		"Unexpected elements",
		"",
		elements
	);
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

	assertElementsEqual(
		"Unexpected elements",
		"foo [in ResolveMethod [in ResolveMethod.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
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
	
	assertElementsEqual(
		"Unexpected elements",
		"foo [in ResolveMethodDeclaration [in ResolveMethodDeclaration.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
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
	assertElementsEqual(
		"Unexpected elements",
		"foo [in ResolveMethodWithIncorrectParameter [in ResolveMethodWithIncorrectParameter.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
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

	assertElementsEqual(
		"Unexpected elements",
		"test [in MyClass [in MyClass.class [in <default> [in zzz.jar [in Resolve]]]]]",
		elements
	);
}
/**
 * bug 33785
 */
public void testResolveMethodWithInnerTypeInClassFile2() throws JavaModelException {
	IClassFile cu = getClassFile("Resolve", "zzz.jar", "", "MyClass2$Inner.class");

	String str = cu.getSource();
	String selectAt = "method";
	String selection = "method";
	int start = str.lastIndexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = cu.codeSelect(start, length);

	assertElementsEqual(
		"Unexpected elements",
		"method [in MyClass2 [in MyClass2.class [in <default> [in zzz.jar [in Resolve]]]]]",
		elements
	);
		
	IMethod method = (IMethod) elements[0];
	ISourceRange sourceRange = method.getSourceRange();
	String methodString = "void method(MyClass2.Inner[] arg){}";
	int o = str.indexOf(methodString);
	int l = methodString.length();
	assertEquals("Unexpected offset", o, sourceRange.getOffset());
	assertEquals("Unexpected length", l, sourceRange.getLength());
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
	
	assertElementsEqual(
		"Unexpected elements",
		"java.lang [in " + getExternalJCLPath().toString() + " [in Resolve]]",
		elements
	);
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
	
	assertElementsEqual(
		"Unexpected elements",
		"",
		elements
	);
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
	
	assertElementsEqual(
		"Unexpected elements",
		"Object [in Object.class [in java.lang [in " + getExternalJCLPath(). toString() + " [in Resolve]]]]",
		elements
	);
}
/**
 * bugs http://dev.eclipse.org/bugs/show_bug.cgi?id=25888
 */
public void testResolveStaticClassConstructor() throws JavaModelException {
	IClassFile cu = getClassFile("Resolve", "test25888.jar", "", "ResolveStaticClassConstructor.class");

	String str = cu.getSource();
	String selectAt = "StaticInnerClass";
	String selection = "StaticInnerClass";
	int start = str.lastIndexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = cu.codeSelect(start, length);

	assertElementsEqual(
		"Unexpected elements",
		"StaticInnerClass [in StaticInnerClass [in ResolveStaticClassConstructor$StaticInnerClass.class [in <default> [in test25888.jar [in Resolve]]]]]",
		elements
	);
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
	
	assertElementsEqual(
		"Unexpected elements",
		"OtherType [in ResolveTypeDeclaration.java [in <default> [in src [in Resolve]]]]",
		elements
	);
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
	
	assertElementsEqual(
		"Unexpected elements",
		"X [in X.java [in p2 [in src [in Resolve]]]]",
		elements
	);
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
	
	assertElementsEqual(
		"Unexpected elements",
		"String [in String.class [in java.lang [in " + getExternalJCLPath().toString() + " [in Resolve]]]]",
		elements
	);
}
}
