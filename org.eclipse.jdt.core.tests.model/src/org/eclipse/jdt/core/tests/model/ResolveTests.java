/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
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

import java.io.IOException;

import junit.framework.Test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;

public class ResolveTests extends AbstractJavaModelTests {
	ICompilationUnit wc = null;

static {
//	TESTS_NAMES = new String[] { "testSecondaryTypes" };
}
public static Test suite() {
	return buildModelTestSuite(ResolveTests.class);
}

public ResolveTests(String name) {
	super(name);
}
@Override
public ICompilationUnit getWorkingCopy(String path, String source) throws JavaModelException {
	return super.getWorkingCopy(path, source, this.wcOwner);
}
private IJavaElement[] select(String path, String source, String selection) throws JavaModelException {
	this.wc = getWorkingCopy(path, source);
	String str = this.wc.getSource();
	int start = str.lastIndexOf(selection);
	int length = selection.length();
	return this.wc.codeSelect(start, length, this.wcOwner);
}
@Override
public void setUpSuite() throws Exception {
	super.setUpSuite();

	setUpJavaProject("Resolve");
}
@Override
protected void setUp() throws Exception {
	super.setUp();
	this.wcOwner = new WorkingCopyOwner(){};
}

@Override
public void tearDownSuite() throws Exception {
	deleteProject("Resolve");

	super.tearDownSuite();
}
@Override
protected void tearDown() throws Exception {
	if(this.wc != null) {
		this.wc.discardWorkingCopy();
	}
	super.tearDown();
}
/**
 * Resolve default abstract method
 * bugs http://dev.eclipse.org/bugs/show_bug.cgi?id=23594
 */
public void testAbstractMethod() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveAbstractMethod.java");
	IJavaElement[] elements = codeSelect(cu, "foo", "foo");
	assertElementsEqual(
		"Unexpected elements",
		"foo() [in SuperInterface [in SuperInterface.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=165900
public void testAmbiguousMethod1() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Resolve/src/test/Test.java",
		"package test;\n"+
		"public class Test {\n" +
		"  void foo(Test1 t) {}\n" +
		"  void foo(Test2 t) {}\n" +
		"  void bar(Object o) {\n" +
		"    foo(o);\n" +
		"  }\n" +
		"}\n" +
		"class Test1 {\n" +
		"}\n" +
		"class Test2 {\n" +
		"}"
	);

	String str = this.workingCopies[0].getSource();
	int start = str.lastIndexOf("foo(o)");
	int length = "foo".length();
	IJavaElement[] elements =  this.workingCopies[0].codeSelect(start, length, this.wcOwner);

	assertElementsEqual(
			"Unexpected elements",
			"foo(Test1) [in Test [in [Working copy] Test.java [in test [in src [in Resolve]]]]]",
			elements
		);
}
/**
 * Resolve an argument name
 */
public void testArgumentName1() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveArgumentName.java");
	IJavaElement[] elements = codeSelect(cu, "var1", "var1");
	assertElementsEqual(
		"Unexpected elements",
		"var1 [in foo(Object, int) [in ResolveArgumentName [in ResolveArgumentName.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/**
 * Resolve an argument name with base type
 */
public void testArgumentName2() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveArgumentName.java");
	IJavaElement[] elements = codeSelect(cu, "var2", "var2");
	assertElementsEqual(
		"Unexpected elements",
		"var2 [in foo(Object, int) [in ResolveArgumentName [in ResolveArgumentName.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/**
 * Resolve the field "length" of an array
 */
public void testArrayLength() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveArrayLength.java");
	IJavaElement[] elements = codeSelect(cu, "length", "length");
	assertElementsEqual(
		"Unexpected elements",
		"",
		elements
	);
}

/**
 * Resolve an argument name inside catch statement
 */
public void testCatchArgumentName1() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveCatchArgumentName.java");
	IJavaElement[] elements = codeSelect(cu, "var1", "var1");
	assertElementsEqual(
		"Unexpected elements",
		"var1 [in foo() [in ResolveCatchArgumentName [in ResolveCatchArgumentName.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/**
 * Resolve an argument name inside catch statement with base type
 */
public void testCatchArgumentName2() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveCatchArgumentName.java");
	IJavaElement[] elements = codeSelect(cu, "var2", "var2");
	assertElementsEqual(
		"Unexpected elements",
		"var2 [in foo() [in ResolveCatchArgumentName [in ResolveCatchArgumentName.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/**
 * bugs http://dev.eclipse.org/bugs/show_bug.cgi?id=24626
 */
public void testCatchArgumentType1() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveCatchArgumentType1.java");
	IJavaElement[] elements = codeSelect(cu, "Y1", "Y1");
	assertElementsEqual(
		"Unexpected elements",
		"Y1 [in X1 [in X1.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/**
 * bugs http://dev.eclipse.org/bugs/show_bug.cgi?id=24626
 */
public void testCatchArgumentType2() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveCatchArgumentType2.java");
	IJavaElement[] elements = codeSelect(cu, "Y1", "Y1");
	assertElementsEqual(
		"Unexpected elements",
		"Y1 [in X1 [in X1.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/**
 * bugs http://dev.eclipse.org/bugs/show_bug.cgi?id=342054
 */
public void testCatchArgumentType3() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveCatchArgumentType1.java");
	IJavaElement[] elements = codeSelect(cu, "exception", "exception");
	assertElementsEqual(
		"Unexpected elements",
		"exception [in foo() [in ResolveCatchArgumentType1 [in ResolveCatchArgumentType1.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
	assertFalse("Not a parameter", ((ILocalVariable) elements[0]).isParameter());
	assertEquals("Wrong flags", Flags.AccFinal, ((ILocalVariable) elements[0]).getFlags());
}
/**
 * Resolve the class 'X' (field type).
 */
public void testClass1() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveClass1.java");
	IJavaElement[] elements = codeSelect(cu, "X", "X");
	assertElementsEqual(
		"Unexpected elements",
		"X [in X.java [in p1 [in src [in Resolve]]]]",
		elements
	);
}
/**
 * Resolve the class 'X' (local variable type).
 */
public void testClass2() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveClass2.java");
	IJavaElement[] elements = codeSelect(cu, "X", "X");
	assertElementsEqual(
		"Unexpected elements",
		"X [in X.java [in p1 [in src [in Resolve]]]]",
		elements
	);
}
/**
 * Resolve the class 'X'(array initializer type).
 */
public void testClass3() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveClass3.java");
	IJavaElement[] elements = codeSelect(cu, "X[]{", "X");
	assertElementsEqual(
		"Unexpected elements",
		"X [in X.java [in p1 [in src [in Resolve]]]]",
		elements
	);
}
/**
 * Resolve the class 'X' (return type).
 */
public void testClass4() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveClass4.java");
	IJavaElement[] elements = codeSelect(cu, "X", "X");
	assertElementsEqual(
		"Unexpected elements",
		"X [in X.java [in p1 [in src [in Resolve]]]]",
		elements
	);
}
/**
 * Resolve the class 'X' (method argument).
 */
public void testClass5() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveClass5.java");
	IJavaElement[] elements = codeSelect(cu, "X", "X");
	assertElementsEqual(
		"Unexpected elements",
		"X [in X.java [in p1 [in src [in Resolve]]]]",
		elements
	);
}
/**
 * Resolve the class 'SuperClass' (super class).
 */
public void testClass6() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveClass6.java");
	IJavaElement[] elements = codeSelect(cu, "X", "X");
	assertElementsEqual(
		"Unexpected elements",
		"X [in X.java [in p1 [in src [in Resolve]]]]",
		elements
	);
}
/**
 * Resolve a constructor
 */
public void testConstructor() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveConstructor.java");
	IJavaElement[] elements = codeSelect(cu, "ResolveConstructor(\"", "ResolveConstructor");
	assertElementsEqual(
		"Unexpected elements",
		"ResolveConstructor(String) [in ResolveConstructor [in ResolveConstructor.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/**
 * Resolve a constructor call
 */
public void testConstructor2() throws JavaModelException {
	IClassFile cf = getClassFile("Resolve", "class-folder", "", "ResolveConstructorCall.class");
	IJavaElement[] elements = codeSelect(cf, "ResolveConstructorCall();", "ResolveConstructorCall");
	assertElementsEqual(
		"Unexpected elements",
		"ResolveConstructorCall() [in ResolveConstructorCall [in ResolveConstructorCall.class [in <default> [in class-folder [in Resolve]]]]]",
		elements
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=204417
 */
public void testConstructor3() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Resolve/src/test/p/Type.java",
		"package test.p;\n" +
		"public class Type {\n" +
		"  void foo() {\n" +
		"    new AClass(unknown) {};\n" +
		"  }\n" +
		"}\n" +
		"class AClass {\n" +
		"}\n"
	);
	IJavaElement[] elements = codeSelect(this.workingCopies[0], "AClass(unknown)", "AClass");
	assertElementsEqual(
		"Unexpected elements",
		"AClass [in [Working copy] Type.java [in test.p [in src [in Resolve]]]]",
		elements
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=204417
 */
public void testConstructor4() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Resolve/src/test/p/Type.java",
		"package test.p;\n" +
		"public class Type {\n" +
		"  void foo() {\n" +
		"    new AClass(unknown) {};\n" +
		"  }\n" +
		"}\n" +
		"class AClass {\n" +
		"  public AClass(Object o) {}\n" +
		"}\n"
	);
	IJavaElement[] elements = codeSelect(this.workingCopies[0], "AClass(unknown)", "AClass");
	assertElementsEqual(
		"Unexpected elements",
		"AClass(Object) [in AClass [in [Working copy] Type.java [in test.p [in src [in Resolve]]]]]",
		elements
	);
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=194743
 * ResolveConstructorCall2.class has no attached source
 */
public void testConstructor5() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Resolve/src/Test.java",
		"public class Test {\n" +
		"  void foo() {\n" +
		"    new ResolveConstructorCall2();\n" +
		"  }\n" +
		"}\n" +
		"\n"
	);
	IJavaElement[] elements = codeSelect(this.workingCopies[0], "ResolveConstructorCall2", "ResolveConstructorCall2");
	assertElementsEqual(
		"Unexpected elements",
		"ResolveConstructorCall2() [in ResolveConstructorCall2 [in ResolveConstructorCall2.class [in <default> [in class-folder [in Resolve]]]]]",
		elements
	);
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=194743
 * ResolveConstructorCall3.class has attached source and a default constructor
 */
public void testConstructor6() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Resolve/src/Test.java",
		"public class Test {\n" +
		"  void foo() {\n" +
		"    new ResolveConstructorCall3();\n" +
		"  }\n" +
		"}\n" +
		"\n"
	);
	IJavaElement[] elements = codeSelect(this.workingCopies[0], "ResolveConstructorCall3", "ResolveConstructorCall3");
	assertElementsEqual(
		"Unexpected elements",
		"ResolveConstructorCall3 [in ResolveConstructorCall3.class [in <default> [in class-folder [in Resolve]]]]",
		elements
	);
}
/**
 * Resolve constructor call
 */
public void testConstructorCallOfMemberType() throws JavaModelException {
	IClassFile cf = getClassFile("Resolve", "class-folder", "", "ResolveConstructorCallOfMemberType.class");
	IJavaElement[] elements = codeSelect(cf, "Inner()", "Inner");
	assertElementsEqual(
		"Unexpected elements",
		"Inner [in ResolveConstructorCallOfMemberType$Inner.class [in <default> [in class-folder [in Resolve]]]]",
		elements
	);
}
/**
 * Resolve constructor call
 */
//TODO(david) enable this test when https://bugs.eclipse.org/bugs/show_bug.cgi?id=108784 will be fixed.
public void _testConstructorCallOfMemberType2() throws JavaModelException {
	IClassFile cf = getClassFile("Resolve", "class-folder", "", "ResolveConstructorCallOfMemberType2.class");
	IJavaElement[] elements = codeSelect(cf, "Inner();", "Inner");
	assertElementsEqual(
		"Unexpected elements",
		"Inner(ResolveConstructorCallOfMemberType2) [in Inner [in ResolveConstructorCallOfMemberType2$Inner.class [in <default> [in class-folder [in Resolve]]]]]",
		elements
	);
}
/**
 * Resolve constructor declaration
 */
public void testConstructorDeclaration() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveConstructorDeclaration.java");
	IJavaElement[] elements = codeSelect(cu, "ResolveConstructorDeclaration(i", "ResolveConstructorDeclaration");
	assertElementsEqual(
		"Unexpected elements",
		"ResolveConstructorDeclaration(int) [in ResolveConstructorDeclaration [in ResolveConstructorDeclaration.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=252481
 */
public void testEmptyCU1() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Resolve/src/Test.java",
		"//this CU must contain only an unknown type name" +
		"Unknown\n" +
		"\n"
	);

	IJavaElement[] elements = codeSelect(this.workingCopies[0], "Unknown", "Unknown");
	assertElementsEqual(
		"Unexpected elements",
		"",
		elements
	);
}
/**
 * Resolve empty selection
 */
public void testEmptySelection() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveTypeEmptySelection.java");
	IJavaElement[] elements = codeSelect(cu, "ject", "");
	assertElementsEqual(
		"Unexpected elements",
		"Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]",
		elements
	);
}
/**
 * Resolve empty selection
 */
public void testEmptySelection2() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveTypeEmptySelection2.java");
	IJavaElement[] elements = codeSelect(cu, "Obj", "");
	assertElementsEqual(
		"Unexpected elements",
		"Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]",
		elements
	);
}
/**
 * Resolve empty selection
 */
public void testEmptySelectionOnMethod() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveEmptySelectionOnMethod.java");
	IJavaElement[] elements = codeSelect(cu, "oo();", "");
	assertElementsEqual(
		"Unexpected elements",
		"foo() [in ResolveEmptySelectionOnMethod [in ResolveEmptySelectionOnMethod.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/**
 * Resolse explicit super constructor call
 */
public void testExplicitSuperConstructorCall() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveExplicitSuperConstructorCall.java");
	IJavaElement[] elements = codeSelect(cu, "super(", "super");
	assertElementsEqual(
		"Unexpected elements",
		"SuperClass(int) [in SuperClass [in SuperClass.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/**
 * Resolse explicit this constructor call
 */
public void testExplicitThisConstructorCall() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveExplicitThisConstructorCall.java");
	IJavaElement[] elements = codeSelect(cu, "this(", "this");
	assertElementsEqual(
		"Unexpected elements",
		"ResolveExplicitThisConstructorCall() [in ResolveExplicitThisConstructorCall [in ResolveExplicitThisConstructorCall.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/**
 * Resolve the field "foo"
 */
public void testField() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveField.java");
	IJavaElement[] elements = codeSelect(cu, "foo =", "foo");
	assertElementsEqual(
		"Unexpected elements",
		"foo [in ResolveField [in ResolveField.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/**
 * Resolve field declaration
 */
public void testFieldDeclaration() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveFieldDeclaration.java");
	IJavaElement[] elements = codeSelect(cu, "foo", "foo");
	assertElementsEqual(
		"Unexpected elements",
		"foo [in ResolveFieldDeclaration [in ResolveFieldDeclaration.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/**
 * Resolve in import
 */
public void testImport() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveImport.java");
	IJavaElement[] elements = codeSelect(cu, "ImportedClass", "ImportedClass");
	assertElementsEqual(
		"Unexpected elements",
		"ImportedClass [in ImportedClass.java [in a.b [in src [in Resolve]]]]",
		elements
	);
}
/**
 * Tests code resolve on a class file without attached source.
 */
public void testInClassFileWithoutSource() throws JavaModelException {
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
public void testInClassFileWithSource() throws JavaModelException {
	IClassFile cf = getClassFile("Resolve", "p3.jar", "p3", "X.class");
	IJavaElement[] elements = codeSelect(cf, "Object", "Object");
	assertElementsEqual(
		"Unexpected elements",
		"Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]",
		elements
	);
}
/**
 * bugs http://dev.eclipse.org/bugs/show_bug.cgi?id=25687
 */
public void testInnerClassAsParamater() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveInnerClassAsParamater.java");
	IJavaElement[] elements = codeSelect(cu, "foo(i)", "foo");
	assertElementsEqual(
		"Unexpected elements",
		"foo(Inner) [in ResolveInnerClassAsParamater [in ResolveInnerClassAsParamater.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/**
 * Resolve the interface "Y"
 */
public void testInterface() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveInterface.java");
	IJavaElement[] elements = codeSelect(cu, "Y", "Y");
	assertElementsEqual(
		"Unexpected elements",
		"Y [in Y.java [in p1 [in src [in Resolve]]]]",
		elements
	);
}
/**
 * Attempt to resolve outside of the range of the compilation unit.
 */
public void testInvalidResolve() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "p1", "X.java");
	try {
		cu.codeSelect(-1, 10);
	} catch (JavaModelException e) {
		return;
	}
	assertTrue("Exception should have been thrown for out of bounds resolution", false);
}
/**
 * Resolve the local class 'Y' (field type).
 */
public void testLocalClass1() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalClass1.java");
	IJavaElement[] elements = codeSelect(cu, "Y[]", "Y");
	assertElementsEqual(
		"Unexpected elements",
		"Y [in foo() [in ResolveLocalClass1 [in ResolveLocalClass1.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/**
 * Resolve the local class 'Y' (local variable type).
 */
public void testLocalClass2() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalClass2.java");
	IJavaElement[] elements = codeSelect(cu, "Y y", "Y");
	assertElementsEqual(
		"Unexpected elements",
		"Y [in foo() [in ResolveLocalClass2 [in ResolveLocalClass2.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/**
 * Resolve the local class 'Y'(array initializer type).
 */
public void testLocalClass3() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalClass3.java");
	IJavaElement[] elements = codeSelect(cu, "Y[]{", "Y");
	assertElementsEqual(
		"Unexpected elements",
		"Y [in foo() [in ResolveLocalClass3 [in ResolveLocalClass3.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/**
 * Resolve the local class 'Y' (return type).
 */
public void testLocalClass4() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalClass4.java");
	IJavaElement[] elements = codeSelect(cu, "Y bar()", "Y");
	assertElementsEqual(
		"Unexpected elements",
		"Y [in foo() [in ResolveLocalClass4 [in ResolveLocalClass4.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/**
 * Resolve the local class 'Y' (method argument).
 */
public void testLocalClass5() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalClass5.java");
	IJavaElement[] elements = codeSelect(cu, "Y y", "Y");
	assertElementsEqual(
		"Unexpected elements",
		"Y [in foo() [in ResolveLocalClass5 [in ResolveLocalClass5.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/**
 * Resolve the local class 'SuperClass' (super class).
 */
public void testLocalClass6() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalClass6.java");
	IJavaElement[] elements = codeSelect(cu, "Y { // superclass", "Y");
	assertElementsEqual(
		"Unexpected elements",
		"Y [in foo() [in ResolveLocalClass6 [in ResolveLocalClass6.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=68710
 */
public void testLocalClass7() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalClass7.java");
	IJavaElement[] elements = codeSelect(cu, "X var", "X");
	assertElementsEqual(
		"Unexpected elements",
		"X [in <anonymous #2> [in foo2() [in ResolveLocalClass7 [in ResolveLocalClass7.java [in <default> [in src [in Resolve]]]]]]]",
		elements
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=126227
 */
public void testLocalClass8() throws JavaModelException {
	// select the default constructor of a local class
	IJavaElement[] elements = select(
			"/Resolve/src/test/Test.java",
			"package test;\n" +
			"public class Test\n" +
			"  void foo() {\n" +
			"    class LocalClass {}\n" +
			"    Object o = new LocalClass();\n" +
			"  }\n" +
			"}\n",
			"LocalClass");

	assertElementsEqual(
		"Unexpected elements",
		"LocalClass [in foo() [in Test [in [Working copy] Test.java [in test [in src [in Resolve]]]]]]",
		elements
	);
}
/*
 * Resolve the local class in a class file and ensure it is a binary type.
 * (regression test for bug 131459 Java model returns stale resolved source type for binary type
 */
public void testLocalClass9() throws CoreException, IOException {
	try {
		IJavaProject project = createJavaProject("P");
		addLibrary(
			project,
			"lib.jar",
			"libsrc.zip",
			new String[] {
				"X.java",
				"public class X {\n" +
				"  void foo() {\n" +
				"    class Y {\n" +
				"    }\n" +
				"  }\n" +
				"}"
			},
			"1.4");
		IClassFile classFile = getClassFile("P", "/P/lib.jar", "", "X.class");
		IJavaElement[] elements = codeSelect(classFile, "Y", "Y");
		assertTrue("Should be a binary type", ((IType) elements[0]).isBinary());
	} finally {
		deleteProject("P");
	}
}
/**
 * Resolve a local constructor
 */
public void testLocalConstructor() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalConstructor.java");
	IJavaElement[] elements = codeSelect(cu, "Y(\"", "Y");
	assertElementsEqual(
		"Unexpected elements",
		"Y(String) [in Y [in foo() [in ResolveLocalConstructor [in ResolveLocalConstructor.java [in <default> [in src [in Resolve]]]]]]]",
		elements
	);
}
/**
 * Resolve local constructor declaration
 */
public void testLocalConstructorDeclaration() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalConstructorDeclaration.java");
	IJavaElement[] elements = codeSelect(cu, "Y(i", "Y");
	assertElementsEqual(
		"Unexpected elements",
		"Y(int) [in Y [in foo() [in ResolveLocalConstructorDeclaration [in ResolveLocalConstructorDeclaration.java [in <default> [in src [in Resolve]]]]]]]",
		elements
	);
}
/**
 * Resolve the local field "fred"
 */
public void testLocalField() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalField.java");
	IJavaElement[] elements = codeSelect(cu, "fred =", "fred");
	assertElementsEqual(
		"Unexpected elements",
		"fred [in Y [in foo() [in ResolveLocalField [in ResolveLocalField.java [in <default> [in src [in Resolve]]]]]]]",
		elements
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=68710
 */
public void testLocalField2() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalField2.java");
	IJavaElement[] elements = codeSelect(cu, "var =", "var");
	assertElementsEqual(
		"Unexpected elements",
		"var [in <anonymous #2> [in foo2() [in ResolveLocalField2 [in ResolveLocalField2.java [in <default> [in src [in Resolve]]]]]]]",
		elements
	);
}
/**
 * Resolve local field declaration
 */
public void testLocalFieldDeclaration() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalFieldDeclaration.java");
	IJavaElement[] elements = codeSelect(cu, "fred", "fred");
	assertElementsEqual(
		"Unexpected elements",
		"fred [in Y [in foo() [in ResolveLocalFieldDeclaration [in ResolveLocalFieldDeclaration.java [in <default> [in src [in Resolve]]]]]]]",
		elements
	);
}
/**
 * Resolve local member type declaration
 */
public void testLocalMemberTypeDeclaration() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalMemberTypeDeclaration1.java");
	IJavaElement[] elements = codeSelect(cu, "Member {", "Member");
	assertElementsEqual(
		"Unexpected elements",
		"Member [in Y [in foo() [in ResolveLocalMemberTypeDeclaration1 [in ResolveLocalMemberTypeDeclaration1.java [in <default> [in src [in Resolve]]]]]]]",
		elements
	);
}
/**
 * Resolve member type declaration
 */
public void testLocalMemberTypeDeclaration2() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalMemberTypeDeclaration2.java");
	IJavaElement[] elements = codeSelect(cu, "MemberOfMember", "MemberOfMember");
	assertElementsEqual(
		"Unexpected elements",
		"MemberOfMember [in Member [in Y [in foo() [in ResolveLocalMemberTypeDeclaration2 [in ResolveLocalMemberTypeDeclaration2.java [in <default> [in src [in Resolve]]]]]]]]",
		elements
	);
}
/**
 * Resolve the method "foo"
 */
public void testLocalMethod() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalMethod.java");
	IJavaElement[] elements = codeSelect(cu, "foo(\"", "foo");
	assertElementsEqual(
		"Unexpected elements",
		"foo(String) [in Y [in bar() [in ResolveLocalMethod [in ResolveLocalMethod.java [in <default> [in src [in Resolve]]]]]]]",
		elements
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=68710
 */
public void testLocalMethod2() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalMethod2.java");
	IJavaElement[] elements = codeSelect(cu, "bar();", "bar");
	assertElementsEqual(
		"Unexpected elements",
		"bar() [in <anonymous #2> [in foo2() [in ResolveLocalMethod2 [in ResolveLocalMethod2.java [in <default> [in src [in Resolve]]]]]]]",
		elements
	);
}
/**
 * Resolve method declaration
 */
public void testLocalMethodDeclaration() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalMethodDeclaration.java");
	IJavaElement[] elements = codeSelect(cu, "foo(i", "foo");
	assertElementsEqual(
		"Unexpected elements",
		"foo(int) [in Y [in bar() [in ResolveLocalMethodDeclaration [in ResolveLocalMethodDeclaration.java [in <default> [in src [in Resolve]]]]]]]",
		elements
	);
}
/**
 * Resolve a local declaration name
 */
public void testLocalName1() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalName.java");
	IJavaElement[] elements = codeSelect(cu, "var1 = new Object();", "var1");
	assertElementsEqual(
		"Unexpected elements",
		"var1 [in foo() [in ResolveLocalName [in ResolveLocalName.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/**
 * Resolve a local declaration name with base type
 */
public void testLocalName2() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalName.java");
	IJavaElement[] elements = codeSelect(cu, "var2 = 1;", "var2");
	assertElementsEqual(
		"Unexpected elements",
		"var2 [in foo() [in ResolveLocalName [in ResolveLocalName.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/**
 * Resolve a local variable reference
 */
public void testLocalName3() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalName.java");
	IJavaElement[] elements = codeSelect(cu, "var1.toString();", "var1");
	assertElementsEqual(
		"Unexpected elements",
		"var1 [in foo() [in ResolveLocalName [in ResolveLocalName.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/**
 * Resolve a local variable reference
 */
public void testLocalName4() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalName.java");
	IJavaElement[] elements = codeSelect(cu, "var2++;", "var2");
	assertElementsEqual(
		"Unexpected elements",
		"var2 [in foo() [in ResolveLocalName [in ResolveLocalName.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/**
 * Resolve a local variable reference
 */
public void testLocalName5() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalName.java");
	IJavaElement[] elements = codeSelect(cu, "var3.hashCode();", "var3");
	assertElementsEqual(
		"Unexpected elements",
		"var3 [in foo() [in ResolveLocalName [in ResolveLocalName.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/**
 * Resolve a local variable reference
 */
public void testLocalName6() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalName.java");
	IJavaElement[] elements = codeSelect(cu, "var3.toString();", "var3");
	assertElementsEqual(
		"Unexpected elements",
		"var3 [in foo() [in ResolveLocalName [in ResolveLocalName.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/**
 * Resolve a local variable reference
 */
public void testLocalName7() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalName.java");
	IJavaElement[] elements = codeSelect(cu, "var4;", "var4");
	assertElementsEqual(
		"Unexpected elements",
		"var4 [in foo() [in ResolveLocalName [in ResolveLocalName.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/*
 * Resolve a local reference and ensure it returns true when asked isStructureKnown().
 * (regression test for bug 48422 Calling isStructureKnown() on ILocalVaraible throws JavaModelExceptions)
 */
public void testLocalVarIsStructureKnown() throws JavaModelException {
	ILocalVariable localVar = getLocalVariable("/Resolve/src/ResolveLocalName.java", "var1 = new Object();", "var1");
	assertTrue(localVar.isStructureKnown());
}
/*
 * Resolve a local reference and ensure its type signature is correct.
 */
public void testLocalVarTypeSignature1() throws JavaModelException {
	ILocalVariable localVar = getLocalVariable("/Resolve/src/ResolveLocalName.java", "var1 = new Object();", "var1");
	assertEquals(
		"Unexpected type signature",
		"QObject;",
		localVar.getTypeSignature());
}
/*
 * Resolve a local reference and ensure its type signature is correct.
 */
public void testLocalVarTypeSignature2() throws JavaModelException {
	ILocalVariable localVar = getLocalVariable("/Resolve/src/ResolveLocalName.java", "var2 = 1;", "var2");
	assertEquals(
		"Unexpected type signature",
		"I",
		localVar.getTypeSignature());
}
/**
 * Resolve member type declaration
 */
public void testMemberTypeDeclaration() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveMemberTypeDeclaration1.java");
	IJavaElement[] elements = codeSelect(cu, "MemberInterface", "MemberInterface");
	assertElementsEqual(
		"Unexpected elements",
		"MemberInterface [in ResolveMemberTypeDeclaration1 [in ResolveMemberTypeDeclaration1.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/**
 * Resolve member type declaration located in default package
 */
public void testMemberTypeDeclaration2() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveMemberTypeDeclaration2.java");
	IJavaElement[] elements = codeSelect(cu, "MemberOfMember", "MemberOfMember");
	assertElementsEqual(
		"Unexpected elements",
		"MemberOfMember [in Member [in ResolveMemberTypeDeclaration2 [in ResolveMemberTypeDeclaration2.java [in <default> [in src [in Resolve]]]]]]",
		elements
	);
}
/**
 * Try to resolve message send on base type.
 */
public void testMessageSendOnBaseType() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveMessageSendOnBaseType.java");
	IJavaElement[] elements = codeSelect(cu, "hello", "hello");
	assertElementsEqual(
		"Unexpected elements",
		"",
		elements
	);
}
/**
 * Resolve the method "foo"
 */
public void testMethod() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveMethod.java");
	IJavaElement[] elements = codeSelect(cu, "foo(\"", "foo");
	assertElementsEqual(
		"Unexpected elements",
		"foo(String) [in ResolveMethod [in ResolveMethod.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/**
 * Resolve method declaration
 */
public void testMethodDeclaration() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveMethodDeclaration.java");
	IJavaElement[] elements = codeSelect(cu, "foo(i", "foo");
	assertElementsEqual(
		"Unexpected elements",
		"foo(int) [in ResolveMethodDeclaration [in ResolveMethodDeclaration.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
/**
 * Resolve method declaration in anonymous
 * (regression test for bug 45655 exception while editing java file)
 */
public void testMethodDeclarationInAnonymous() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveMethodDeclarationInAnonymous.java");
	IJavaElement[] elements = codeSelect(cu, "bar()", "bar");
	assertElementsEqual(
		"Unexpected elements",
		"bar() [in <anonymous #1> [in foo() [in ResolveMethodDeclarationInAnonymous [in ResolveMethodDeclarationInAnonymous.java [in <default> [in src [in Resolve]]]]]]]",
		elements
	);
}
/**
 * Resolve method declaration in anonymous
 * (regression test for bug 45786 No selection on method declaration in field initializer)
 */
public void testMethodDeclarationInAnonymous2() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveMethodDeclarationInAnonymous2.java");
	IJavaElement[] elements = codeSelect(cu, "foo()", "foo");
	assertElementsEqual(
		"Unexpected elements",
		"foo() [in <anonymous #1> [in field [in ResolveMethodDeclarationInAnonymous2 [in ResolveMethodDeclarationInAnonymous2.java [in <default> [in src [in Resolve]]]]]]]",
		elements
	);
}
/**
 * Resolve method declaration in anonymous
 * (regression test for bug 47795 NPE selecting method in anonymous 2 level deep)
 */
public void testMethodDeclarationInAnonymous3() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveMethodDeclarationInAnonymous3.java");
	IJavaElement[] elements = codeSelect(cu, "selectMe(", "selectMe");
	assertElementsEqual(
		"Unexpected elements",
		"selectMe() [in <anonymous #1> [in bar() [in <anonymous #1> [in foo() [in ResolveMethodDeclarationInAnonymous3 [in ResolveMethodDeclarationInAnonymous3.java [in <default> [in src [in Resolve]]]]]]]]]",
		elements
	);
}
/**
 * Resolve method declaration in anonymous
 * (regression test for bug 47214 Cannot open declaration on a selected method of an anonymous class)
 */
public void testMethodDeclarationInAnonymous4() throws JavaModelException {
	IClassFile classFile = getClassFile("Resolve", "test47214.jar", "p", "X.class");
	IJavaElement[] elements = codeSelect(classFile, "bar(", "bar");
	assertElementsEqual(
		"Unexpected elements",
		"bar() [in <anonymous> [in X$1.class [in p [in test47214.jar [in Resolve]]]]]",
		elements
	);
}
/**
 * Resolve the method
 */
public void testMethodWithIncorrectParameter() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveMethodWithIncorrectParameter.java");
	IJavaElement[] elements = codeSelect(cu, "foo(\"String", "foo");
	assertElementsEqual(
		"Unexpected elements",
			"foo(int) [in ResolveMethodWithIncorrectParameter [in ResolveMethodWithIncorrectParameter.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=126160
public void testMethodWithIncorrectParameter2() throws JavaModelException {
	IJavaElement[] elements = select(
			"/Resolve/src/test/Test.java",
			"package test;\n" +
			"public class Test\n" +
			"  void called(String arg) {\n" +
			"  }\n" +
			"  void foo() {\n" +
			"    new Object() {\n" +
			"      void bar() {\n" +
			"        called(zzz);\n" +
			"      }\n" +
			"    };\n" +
			"  }\n" +
			"}\n",
			"called");

	assertElementsEqual(
		"Unexpected elements",
		"called(String) [in Test [in [Working copy] Test.java [in test [in src [in Resolve]]]]]",
		elements
	);

}
/**
 * Resolve method in inner type.
 */
public void testMethodWithInnerTypeInClassFile() throws JavaModelException {
	IClassFile cf = getClassFile("Resolve", "zzz.jar", "", "MyClass$Inner.class");
	IJavaElement[] elements = codeSelect(cf, "test", "test");
	assertElementsEqual(
		"Unexpected elements",
		"test() [in MyClass [in MyClass.class [in <default> [in zzz.jar [in Resolve]]]]]",
		elements
	);
}
/**
 * bug 33785
 */
public void testMethodWithInnerTypeInClassFile2() throws JavaModelException {
	IClassFile cf = getClassFile("Resolve", "zzz.jar", "", "MyClass2$Inner.class");
	IJavaElement[] elements = codeSelect(cf, "method", "method");
	assertElementsEqual(
		"Unexpected elements",
		"method(MyClass2.Inner[]) [in MyClass2 [in MyClass2.class [in <default> [in zzz.jar [in Resolve]]]]]",
		elements
	);

	IMethod method = (IMethod) elements[0];
	ISourceRange sourceRange = method.getSourceRange();
	String methodString = "void method(MyClass2.Inner[] arg){}";
	int o = cf.getSource().indexOf(methodString);
	int l = methodString.length();
	assertEquals("Unexpected offset", o, sourceRange.getOffset());
	assertEquals("Unexpected length", l, sourceRange.getLength());
}
/**
 * Tries to resolve the type "lang. \u0053tring" which doesn't exist.
 */
public void testNegativeResolveUnicode() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveUnicode.java");
	IJavaElement[] elements = codeSelect(cu, "lang.\\u0053tring", "lang.\\u0053tring");
	assertElementsEqual(
		"Unexpected elements",
		"String [in String.class [in java.lang [in "+ getExternalJCLPathString() + "]]]",
		elements
	);
}
/**
 * Resolve the package "java.lang"
 */
public void testPackage() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolvePackage.java");
	IJavaElement[] elements = codeSelect(cu, "lang", "lang");
	assertElementsEqual(
		"Unexpected elements",
		"java.lang [in " + getExternalJCLPathString() + "]",
		elements
	);
}
/**
 * Try to resolve the qualified type "lang.Object"
 */
public void testPartiallyQualifiedType() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolvePartiallyQualifiedType.java");
	IJavaElement[] elements = codeSelect(cu, "lang.Object", "lang.Object");
	assertElementsEqual(
		"Unexpected elements",
		"Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]",
		elements
	);
}
/**
 * Resolve the qualified type "java.lang.Object"
 */
public void testQualifiedType() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveQualifiedType.java");
	IJavaElement[] elements = codeSelect(cu, "java.lang.Object", "java.lang.Object");
	assertElementsEqual(
		"Unexpected elements",
		"Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]",
		elements
	);
}
/**
 * bugs http://dev.eclipse.org/bugs/show_bug.cgi?id=25888
 */
public void testStaticClassConstructor() throws JavaModelException {
	IClassFile cu = getClassFile("Resolve", "test25888.jar", "", "ResolveStaticClassConstructor.class");
	IJavaElement[] elements = codeSelect(cu, "StaticInnerClass();", "StaticInnerClass");
	assertElementsEqual(
		"Unexpected elements",
		"StaticInnerClass() [in StaticInnerClass [in ResolveStaticClassConstructor$StaticInnerClass.class [in <default> [in test25888.jar [in Resolve]]]]]",
		elements
	);
}
/**
 * Resolve type declaration
 */
public void testTypeDeclaration() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveTypeDeclaration.java");
	IJavaElement[] elements = codeSelect(cu, "OtherType", "OtherType");
	assertElementsEqual(
		"Unexpected elements",
		"OtherType [in ResolveTypeDeclaration.java [in <default> [in src [in Resolve]]]]",
		elements
	);
}
/**
 * Resolve type in comment.
 */
public void testTypeInComment() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveTypeInComment.java");
	IJavaElement[] elements = codeSelect(cu, "X */", "X");
	assertElementsEqual(
		"Unexpected elements",
		"X [in X.java [in p2 [in src [in Resolve]]]]",
		elements
	);
}
/**
 * Resolve the type "java.lang. \u0053ring"
 */
public void testUnicode() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveUnicode.java");
	IJavaElement[] elements = codeSelect(cu, "java.lang.\\u0053tring", "java.lang.\\u0053tring");
	assertElementsEqual(
		"Unexpected elements",
		"String [in String.class [in java.lang [in "+ getExternalJCLPathString() + "]]]",
		elements
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=180683
public void testUnicode2() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Resolve/src/test/B.java",
		"package test;\n"+
		"public class \\u0042 {\n" +
		"  void foo() {\n" +
		"    \\u0042 var = null;\n" +
		"  }\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int start = str.lastIndexOf("42");
	int length = "".length();
	IJavaElement[] elements =  this.workingCopies[0].codeSelect(start, length, this.wcOwner);

	assertElementsEqual(
			"Unexpected elements",
			"B [in [Working copy] B.java [in test [in src [in Resolve]]]]",
			elements
		);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=180683
public void testUnicode3() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Resolve/src/test/B.java",
		"package test;\n"+
		"public class \\u0042 {\n" +
		"  void foo() {\n" +
		"    \\u004");

	String str = this.workingCopies[0].getSource();
	int start = str.lastIndexOf("4");
	int length = "".length();
	IJavaElement[] elements =  this.workingCopies[0].codeSelect(start, length, this.wcOwner);

	assertElementsEqual(
			"Unexpected elements",
			"",
			elements
		);
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=47177
 */
public void testLocalNameForClassFile() throws JavaModelException {
	IClassFile cu = getClassFile("Resolve", "test47177.jar", "", "ResolveLocalName.class");

	//Resolve a local declaration name
	IJavaElement[] elements = codeSelect(cu, "var1 = new Object();", "var1");
	assertElementsEqual(
			"Unexpected elements",
			"var1 [in foo() [in ResolveLocalName [in ResolveLocalName.class [in <default> [in test47177.jar [in Resolve]]]]]]",
			elements
	);

	// Resolve a local declaration name with base type
	elements = codeSelect(cu, "var2 = 1;", "var2");
	assertElementsEqual(
			"Unexpected elements",
			"var2 [in foo() [in ResolveLocalName [in ResolveLocalName.class [in <default> [in test47177.jar [in Resolve]]]]]]",
			elements
	);

	// Resolve a local variable reference
	elements = codeSelect(cu, "var1.toString();", "var1");
	assertElementsEqual(
			"Unexpected elements",
			"var1 [in foo() [in ResolveLocalName [in ResolveLocalName.class [in <default> [in test47177.jar [in Resolve]]]]]]",
			elements
	);

	// Resolve a local variable reference
	elements = codeSelect(cu, "var2++;", "var2");
	assertElementsEqual(
			"Unexpected elements",
			"var2 [in foo() [in ResolveLocalName [in ResolveLocalName.class [in <default> [in test47177.jar [in Resolve]]]]]]",
			elements
	);

	// Resolve a local variable reference
	elements = codeSelect(cu, "var3.hashCode();", "var3");
	assertElementsEqual(
			"Unexpected elements",
			"var3 [in foo() [in ResolveLocalName [in ResolveLocalName.class [in <default> [in test47177.jar [in Resolve]]]]]]",
			elements
	);

	// Resolve a local variable reference
	elements = codeSelect(cu, "var3.toString();", "var3");
	assertElementsEqual(
			"Unexpected elements",
			"var3 [in foo() [in ResolveLocalName [in ResolveLocalName.class [in <default> [in test47177.jar [in Resolve]]]]]]",
			elements
	);

	// Resolve a local variable reference
	elements = codeSelect(cu, "var4;", "var4");
	assertElementsEqual(
			"Unexpected elements",
			"var4 [in foo() [in ResolveLocalName [in ResolveLocalName.class [in <default> [in test47177.jar [in Resolve]]]]]]",
			elements
	);
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=42365
 */
public void testMethodDeclarationInInterface() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveMethodDeclarationInInterface.java");
	IJavaElement[] elements = codeSelect(cu, "foo", "foo");
	assertElementsEqual(
			"Unexpected elements",
			"foo() [in QI [in QI.class [in <default> [in jj.jar [in Resolve]]]]]",
			elements
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=142303
public void testMethodInAnonymous1() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Resolve/src/Test2.java",
			"public class Test2 {\n" +
			"\n" +
			"    private void foo(boolean v) {\n" +
			"    }\n" +
			"\n" +
			"    void function(boolean v) {\n" +
			"        new Object() {\n" +
			"            public void run() {\n" +
			"                if (false) {\n" +
			"                } else {\n" +
			"                    foo(false); // <-- right-click, open declaration fails\n" +
			"                }\n" +
			"            }\n" +
			"        };\n" +
			"        new Object() {  public void run() {   } };\n" +
			"        if (v) {}\n" +
			"    }\n" +
			"}");

	String str = this.workingCopies[0].getSource();
	String selectAt = "foo(false)";
	String selection = "foo";
	int start = str.indexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length, this.wcOwner);

	assertElementsEqual(
			"Unexpected elements",
			"foo(boolean) [in Test2 [in [Working copy] Test2.java [in <default> [in src [in Resolve]]]]]",
			elements);
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=57414
 */
public void testEndOfFile() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveEndOfFile.java");
	IJavaElement[] elements = codeSelectAt(cu, "zzz");
	assertElementsEqual(
			"Unexpected elements",
			"",
			elements
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=144858
public void testDuplicateLocals1() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Resolve/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"	void foo() {\n" +
		"		int x = 0;\n" +
		"		TestString x = null;\n" +
		"		x.bar;\n" +
		"	}\n" +
		"}");

	this.workingCopies[1] = getWorkingCopy(
		"/Resolve/src/test/TestString.java",
		"package test;"+
		"public class TestString {\n" +
		"	public void bar() {\n" +
		"	}\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int start = str.lastIndexOf("x");
	int length = "x".length();
	IJavaElement[] elements =  this.workingCopies[0].codeSelect(start, length, this.wcOwner);

	assertElementsEqual(
			"Unexpected elements",
			"x [in foo() [in Test [in [Working copy] Test.java [in test [in src [in Resolve]]]]]]",
			elements
		);

	assertEquals(
			"Unexpected type",
			"QTestString;",
			((ILocalVariable)elements[0]).getTypeSignature());
	assertFalse(((ILocalVariable)elements[0]).isParameter());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=144858
public void testDuplicateLocals2() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Resolve/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"        public static void main(String[] args) {\n" +
		"                int x = 2;\n" +
		"                try {\n" +
		"                \n" +
		"                } catch(TestException x) {\n" +
		"                        x.bar();\n" +
		"                } catch(Exception e) {\n" +
		"                }\n" +
		"        }\n" +
		"}");

	this.workingCopies[1] = getWorkingCopy(
		"/Resolve/src/test/TestException.java",
		"package test;"+
		"public class TestException extends Exception {\n" +
		"	public void bar() {\n" +
		"	}\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int start = str.lastIndexOf("x.");
	int length = "x".length();
	IJavaElement[] elements =  this.workingCopies[0].codeSelect(start, length, this.wcOwner);

	assertElementsEqual(
			"Unexpected elements",
			"x [in main(String[]) [in Test [in [Working copy] Test.java [in test [in src [in Resolve]]]]]]",
			elements
		);

	assertEquals(
			"Unexpected type",
			"QTestException;",
			((ILocalVariable)elements[0]).getTypeSignature());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=144858
public void testDuplicateLocals3() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Resolve/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"        public static void main(String[] args) {\n" +
		"                int x = x = 0;\n" +
		"                if (true) {\n" +
		"                        TestString x = x = null;\n" +
		"                }\n" +
		"        }\n" +
		"}");

	this.workingCopies[1] = getWorkingCopy(
		"/Resolve/src/test/TestString.java",
		"package test;"+
		"public class TestString {\n" +
		"	public void bar() {\n" +
		"	}\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int start = str.lastIndexOf("x");
	int length = "x".length();
	IJavaElement[] elements =  this.workingCopies[0].codeSelect(start, length, this.wcOwner);

	assertElementsEqual(
			"Unexpected elements",
			"x [in main(String[]) [in Test [in [Working copy] Test.java [in test [in src [in Resolve]]]]]]",
			elements
		);

	assertEquals(
			"Unexpected type",
			"QTestString;",
			((ILocalVariable)elements[0]).getTypeSignature());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=144858
public void testDuplicateLocals4() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Resolve/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"        public static void main(String[] args) {\n" +
		"                for (int x = 0; x < 10; x++) {\n" +
		"                        for (TestString x = null; x.bar() < 5;)  {\n" +
		"                                // do something\n" +
		"                        }\n" +
		"                }\n" +
		"        }\n" +
		"}");

	this.workingCopies[1] = getWorkingCopy(
		"/Resolve/src/test/TestString.java",
		"package test;"+
		"public class TestString {\n" +
		"	public int bar() {\n" +
		"		return 0;\n" +
		"	}\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int start = str.lastIndexOf("x");
	int length = "x".length();
	IJavaElement[] elements =  this.workingCopies[0].codeSelect(start, length, this.wcOwner);

	assertElementsEqual(
			"Unexpected elements",
			"x [in main(String[]) [in Test [in [Working copy] Test.java [in test [in src [in Resolve]]]]]]",
			elements
		);

	assertEquals(
			"Unexpected type",
			"QTestString;",
			((ILocalVariable)elements[0]).getTypeSignature());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=144858
public void testDuplicateLocals5() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Resolve/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"        public static void main(String[] args) {\n" +
		"                for (int x = 0; x < 10; x++) {\n" +
		"                        for (TestString x = null; x.bar() < 5;)  {\n" +
		"                                x.bar(); // do something\n" +
		"                        }\n" +
		"                }\n" +
		"        }\n" +
		"}");

	this.workingCopies[1] = getWorkingCopy(
		"/Resolve/src/test/TestString.java",
		"package test;"+
		"public class TestString {\n" +
		"	public int bar() {\n" +
		"		return 0;\n" +
		"	}\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int start = str.lastIndexOf("x");
	int length = "x".length();
	IJavaElement[] elements =  this.workingCopies[0].codeSelect(start, length, this.wcOwner);

	assertElementsEqual(
			"Unexpected elements",
			"x [in main(String[]) [in Test [in [Working copy] Test.java [in test [in src [in Resolve]]]]]]",
			elements
		);

	assertEquals(
			"Unexpected type",
			"QTestString;",
			((ILocalVariable)elements[0]).getTypeSignature());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=165662
public void testDuplicateLocalsType1() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Resolve/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"  void foo() {\n" +
		"     class Local {\n" +
		"        public void foo() {}\n" +
		"     }\n" +
		"     {\n" +
		"        class Local {\n" +
		"                Local(int i) {\n" +
		"                        this.init(i);\n" +
		"                }\n" +
		"				 void init(int i) {}\n" +
		"                public void bar() {}\n" +
		"        }\n" +
		"        Local l = new Local(0);\n" +
		"        l.bar();\n" +
		"     }\n" +
		"  }\n" +
		"}");

	this.workingCopies[1] = getWorkingCopy(
		"/Resolve/src/test/TestString.java",
		"package test;"+
		"public class TestString {\n" +
		"	public int bar() {\n" +
		"		return 0;\n" +
		"	}\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int start = str.lastIndexOf("bar");
	int length = "bar".length();
	IJavaElement[] elements =  this.workingCopies[0].codeSelect(start, length, this.wcOwner);

	assertElementsEqual(
			"Unexpected elements",
			"bar() [in Local#2 [in foo() [in Test [in [Working copy] Test.java [in test [in src [in Resolve]]]]]]]",
			elements
		);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=165662
public void testDuplicateLocalsType2() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Resolve/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"        void foo() {\n" +
		"                class Local {\n" +
		"                        void foo() {\n" +
		"                        }\n" +
		"                }\n" +
		"                {\n" +
		"                        class Local {\n" +
		"                               Local(int i) {\n" +
		"                                       this.init(i);\n" +
		"                                       this.bar();\n" +
		"                               }\n" +
		"				 				void init(int i) {}\n" +
		"                        		void bar() {\n" +
		"                        		}\n" +
		"                        }\n" +
		"                        Local l = new Local(0);\n" +
		"                }\n" +
		"                Local l = new Local();\n" +
		"                l.foo();\n" +
		"        }\n" +
		"}");

	this.workingCopies[1] = getWorkingCopy(
		"/Resolve/src/test/TestString.java",
		"package test;"+
		"public class TestString {\n" +
		"	public int bar() {\n" +
		"		return 0;\n" +
		"	}\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int start = str.lastIndexOf("foo");
	int length = "foo".length();
	IJavaElement[] elements =  this.workingCopies[0].codeSelect(start, length, this.wcOwner);

	assertElementsEqual(
			"Unexpected elements",
			"foo() [in Local [in foo() [in Test [in [Working copy] Test.java [in test [in src [in Resolve]]]]]]]",
			elements
		);
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=65259
 */
public void testDuplicateMethodDeclaration() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveDuplicateMethodDeclaration.java");

	String str = cu.getSource();
	int start = str.indexOf("foo");
	int length = "foo".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);

	assertElementsEqual(
			"Unexpected elements",
			"foo() [in ResolveDuplicateMethodDeclaration [in ResolveDuplicateMethodDeclaration.java [in <default> [in src [in Resolve]]]]]",
			elements
	);
}
/**
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=65259
 */
public void testDuplicateMethodDeclaration2() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveDuplicateMethodDeclaration.java");

	String str = cu.getSource();
	int start = str.lastIndexOf("foo");
	int length = "foo".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);

	assertElementsEqual(
			"Unexpected elements",
			"foo()#2 [in ResolveDuplicateMethodDeclaration [in ResolveDuplicateMethodDeclaration.java [in <default> [in src [in Resolve]]]]]",
			elements
	);
}
public void testDuplicateMethodDeclaration3() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveDuplicateMethodDeclaration3.java");

	String str = cu.getSource();
	int start = str.indexOf("foo");
	int length = "foo".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);

	assertElementsEqual(
			"Unexpected elements",
			"foo(Object) [in ResolveDuplicateMethodDeclaration3 [in ResolveDuplicateMethodDeclaration3.java [in <default> [in src [in Resolve]]]]]",
			elements
	);
}
public void testDuplicateMethodDeclaration4() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveDuplicateMethodDeclaration3.java");

	String str = cu.getSource();
	int start = str.lastIndexOf("foo");
	int length = "foo".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);

	assertElementsEqual(
			"Unexpected elements",
			"foo(Object)#2 [in ResolveDuplicateMethodDeclaration3 [in ResolveDuplicateMethodDeclaration3.java [in <default> [in src [in Resolve]]]]]",
			elements
	);
}
public void testDuplicateMethodDeclaration5() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveDuplicateMethodDeclaration5.java");

	String str = cu.getSource();
	int start = str.indexOf("foo");
	int length = "foo".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);

	assertElementsEqual(
			"Unexpected elements",
			"foo(Zork) [in ResolveDuplicateMethodDeclaration5 [in ResolveDuplicateMethodDeclaration5.java [in <default> [in src [in Resolve]]]]]",
			elements
	);
}
public void testDuplicateMethodDeclaration6() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveDuplicateMethodDeclaration5.java");

	String str = cu.getSource();
	int start = str.lastIndexOf("foo");
	int length = "foo".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);

	assertElementsEqual(
			"Unexpected elements",
			"foo(Zork)#2 [in ResolveDuplicateMethodDeclaration5 [in ResolveDuplicateMethodDeclaration5.java [in <default> [in src [in Resolve]]]]]",
			elements
	);
}
public void testDuplicateMethodDeclaration7() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveDuplicateMethodDeclaration7.java");

	String str = cu.getSource();
	int start = str.indexOf("foo");
	int length = "foo".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);

	assertElementsEqual(
			"Unexpected elements",
			"foo(Zork) [in Inner [in ResolveDuplicateMethodDeclaration7 [in ResolveDuplicateMethodDeclaration7.java [in <default> [in src [in Resolve]]]]]]",
			elements
	);
}
public void testDuplicateMethodDeclaration8() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveDuplicateMethodDeclaration7.java");

	String str = cu.getSource();
	int start = str.lastIndexOf("foo");
	int length = "foo".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);

	assertElementsEqual(
			"Unexpected elements",
			"foo(Zork)#2 [in Inner [in ResolveDuplicateMethodDeclaration7 [in ResolveDuplicateMethodDeclaration7.java [in <default> [in src [in Resolve]]]]]]",
			elements
	);
}
public void testDuplicateMethodDeclaration9() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveDuplicateMethodDeclaration9.java");

	String str = cu.getSource();
	int start = str.indexOf("foo(/*1*/");
	int length = "foo".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);

	assertElementsEqual(
			"Unexpected elements",
			"foo(Zork) [in Inner [in ResolveDuplicateMethodDeclaration9 [in ResolveDuplicateMethodDeclaration9.java [in <default> [in src [in Resolve]]]]]]",
			elements
	);
}
public void testDuplicateMethodDeclaration10() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveDuplicateMethodDeclaration9.java");

	String str = cu.getSource();
	int start = str.lastIndexOf("foo(/*1*/");
	int length = "foo".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);

	assertElementsEqual(
			"Unexpected elements",
			"foo(Zork)#2 [in Inner [in ResolveDuplicateMethodDeclaration9 [in ResolveDuplicateMethodDeclaration9.java [in <default> [in src [in Resolve]]]]]]",
			elements
	);
}
public void testDuplicateMethodDeclaration11() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveDuplicateMethodDeclaration11.java");

	String str = cu.getSource();
	int start = str.indexOf("foo(/*2*/");
	int length = "foo".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);

	assertElementsEqual(
			"Unexpected elements",
			"foo(Zork) [in Inner#2 [in ResolveDuplicateMethodDeclaration11 [in ResolveDuplicateMethodDeclaration11.java [in <default> [in src [in Resolve]]]]]]",
			elements
	);
}
public void testDuplicateMethodDeclaration12() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveDuplicateMethodDeclaration11.java");

	String str = cu.getSource();
	int start = str.lastIndexOf("foo(/*2*/");
	int length = "foo".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);

	assertElementsEqual(
			"Unexpected elements",
			"foo(Zork)#2 [in Inner#2 [in ResolveDuplicateMethodDeclaration11 [in ResolveDuplicateMethodDeclaration11.java [in <default> [in src [in Resolve]]]]]]",
			elements
	);
}
public void testDuplicateFieldDeclaration() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveDuplicateFieldDeclaration.java");

	String str = cu.getSource();
	int start = str.indexOf("var;/*1*/");
	int length = "var".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);

	assertElementsEqual(
			"Unexpected elements",
			"var [in Inner [in ResolveDuplicateFieldDeclaration [in ResolveDuplicateFieldDeclaration.java [in <default> [in src [in Resolve]]]]]]",
			elements
	);
}
public void testDuplicateFieldDeclaration2() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveDuplicateFieldDeclaration.java");

	String str = cu.getSource();
	int start = str.lastIndexOf("var;/*1*/");
	int length = "var".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);

	assertElementsEqual(
			"Unexpected elements",
			"var#2 [in Inner [in ResolveDuplicateFieldDeclaration [in ResolveDuplicateFieldDeclaration.java [in <default> [in src [in Resolve]]]]]]",
			elements
	);
}
public void testDuplicateFieldDeclaration3() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveDuplicateFieldDeclaration3.java");

	String str = cu.getSource();
	int start = str.indexOf("var;/*2*/");
	int length = "var".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);

	assertElementsEqual(
			"Unexpected elements",
			"var [in Inner#2 [in ResolveDuplicateFieldDeclaration3 [in ResolveDuplicateFieldDeclaration3.java [in <default> [in src [in Resolve]]]]]]",
			elements
	);
}
public void testDuplicateFieldDeclaration4() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveDuplicateFieldDeclaration3.java");

	String str = cu.getSource();
	int start = str.lastIndexOf("var;/*2*/");
	int length = "var".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);

	assertElementsEqual(
			"Unexpected elements",
			"var#2 [in Inner#2 [in ResolveDuplicateFieldDeclaration3 [in ResolveDuplicateFieldDeclaration3.java [in <default> [in src [in Resolve]]]]]]",
			elements
	);
}
public void testDuplicateTypeDeclaration() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveDuplicateTypeDeclaration.java");

	String str = cu.getSource();
	int start = str.indexOf("Inner");
	int length = "Inner".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);

	assertElementsEqual(
			"Unexpected elements",
			"Inner [in ResolveDuplicateTypeDeclaration [in ResolveDuplicateTypeDeclaration.java [in <default> [in src [in Resolve]]]]]",
			elements
	);
}
public void testDuplicateTypeDeclaration2() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveDuplicateTypeDeclaration.java");

	String str = cu.getSource();
	int start = str.lastIndexOf("Inner");
	int length = "Inner".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);

	assertElementsEqual(
			"Unexpected elements",
			"Inner#2 [in ResolveDuplicateTypeDeclaration [in ResolveDuplicateTypeDeclaration.java [in <default> [in src [in Resolve]]]]]",
			elements
	);
}
public void testDuplicateTypeDeclaration3() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveDuplicateTypeDeclaration3.java");

	String str = cu.getSource();
	int start = str.indexOf("Inner2/*1*/");
	int length = "Inner2".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);

	assertElementsEqual(
			"Unexpected elements",
			"Inner2 [in Inner [in ResolveDuplicateTypeDeclaration3 [in ResolveDuplicateTypeDeclaration3.java [in <default> [in src [in Resolve]]]]]]",
			elements
	);
}
public void testDuplicateTypeDeclaration4() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveDuplicateTypeDeclaration3.java");

	String str = cu.getSource();
	int start = str.lastIndexOf("Inner2/*1*/");
	int length = "Inner2".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);

	assertElementsEqual(
			"Unexpected elements",
			"Inner2#2 [in Inner [in ResolveDuplicateTypeDeclaration3 [in ResolveDuplicateTypeDeclaration3.java [in <default> [in src [in Resolve]]]]]]",
			elements
	);
}
public void testDuplicateTypeDeclaration5() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveDuplicateTypeDeclaration5.java");

	String str = cu.getSource();
	int start = str.indexOf("Inner2/*2*/");
	int length = "Inner2".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);

	assertElementsEqual(
			"Unexpected elements",
			"Inner2 [in Inner#2 [in ResolveDuplicateTypeDeclaration5 [in ResolveDuplicateTypeDeclaration5.java [in <default> [in src [in Resolve]]]]]]",
			elements
	);
}
public void testDuplicateTypeDeclaration6() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveDuplicateTypeDeclaration5.java");

	String str = cu.getSource();
	int start = str.lastIndexOf("Inner2/*2*/");
	int length = "Inner2".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);

	assertElementsEqual(
			"Unexpected elements",
			"Inner2#2 [in Inner#2 [in ResolveDuplicateTypeDeclaration5 [in ResolveDuplicateTypeDeclaration5.java [in <default> [in src [in Resolve]]]]]]",
			elements
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=119434
public void testDuplicateTypeDeclaration7() throws CoreException, IOException {
	String jarName = "bug119434.jar";
	String srcName = "bug119434_src.zip";
	try {
		String[] pathAndContents = new String[] {
			"test/p/Type.java",
			"package test.p;"+
			"public class Type {\n" +
			"}\n"
		};

		addLibrary(jarName, srcName, pathAndContents, JavaCore.VERSION_1_4);

		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy(
			"/Resolve/src/test/Test.java",
			"package test;"+
			"import test.p.Type;"+
			"public class Test {\n" +
			"}\n");

		this.workingCopies[1] = getWorkingCopy(
			"/Resolve/src/test/p/Type.java",
			"package test.p;"+
			"public class Type {\n" +
			"}\n");

		String str = this.workingCopies[0].getSource();
		int start = str.lastIndexOf("Type");
		int length = "Type".length();
		IJavaElement[] elements =  this.workingCopies[0].codeSelect(start, length, this.wcOwner);

		assertElementsEqual(
			"Unexpected elements",
			"Type [in [Working copy] Type.java [in test.p [in src [in Resolve]]]]",
			elements
		);
	} finally {
		removeLibrary(this.currentProject, jarName, srcName);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=120766
public void testDuplicateTypeDeclaration8() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Resolve/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"  void foo() {\n" +
		"    test.p1.Type t = new test.p1.Type();\n" +
		"  }\n" +
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Resolve/src/test/p1/Type.java",
		"package test.p1;"+
		"public class Type {\n" +
		"  public Type(int i) {}\n" +
		"}\n");

	this.workingCopies[2] = getWorkingCopy(
		"/Resolve/src/test/p2/Type.java",
		"package test.p2;"+
		"public class Type {\n" +
		"}\n");

	String str = this.workingCopies[0].getSource();
	int start = str.lastIndexOf("Type");
	int length = "Type".length();
	IJavaElement[] elements =  this.workingCopies[0].codeSelect(start, length, this.wcOwner);

	assertElementsEqual(
			"Unexpected elements",
			"Type [in [Working copy] Type.java [in test.p1 [in src [in Resolve]]]]",
			elements
	);
}
public void testArrayParameterInsideParent1() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveArrayParameterInsideParent1.java");

	String str = cu.getSource();
	int start = str.lastIndexOf("var");
	int length = "var".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);

	assertElementsEqual(
			"Unexpected elements",
			"var [in test(int[]) [in ResolveArrayParameterInsideParent1 [in ResolveArrayParameterInsideParent1.java [in <default> [in src [in Resolve]]]]]]",
			elements
	);
}
public void testDeepLocalVariable() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveDeepLocalVariable.java");

	String str = cu.getSource();
	int start = str.lastIndexOf("foo");
	int length = "foo".length();
	IJavaElement[] elements =  cu.codeSelect(start, length);

	assertElementsEqual(
			"Unexpected elements",
			"foo [in D9() [in D9 [in D8 [in D7 [in D6 [in D5 [in D4 [in D3 [in D2 [in D1 [in ResolveDeepLocalVariable [in ResolveDeepLocalVariable.java [in <default> [in src [in Resolve]]]]]]]]]]]]]]]",
			elements
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=68710
 */
public void testLocalVariable() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveLocalVariable.java");
	IJavaElement[] elements = codeSelect(cu, "var =", "var");
	assertElementsEqual(
		"Unexpected elements",
		"var [in toto() [in <anonymous #2> [in foo2() [in ResolveLocalVariable [in ResolveLocalVariable.java [in <default> [in src [in Resolve]]]]]]]]",
		elements
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78931
 */
public void testQualifiedName1() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveQualifiedName1.java");

	IJavaElement[] elements = codeSelect(cu, "pp.qq.XX.YY.ZZ", "pp.qq.XX.YY.ZZ");

	assertElementsEqual(
		"Unexpected elements",
		"ZZ [in YY [in XX [in XX.java [in pp.qq [in src [in Resolve]]]]]]",
		elements
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78931
 */
public void testQualifiedName2() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveQualifiedName2.java");

	IJavaElement[] elements = codeSelect(cu, "qq.XX.YY.ZZ", "qq.XX.YY.ZZ");

	assertElementsEqual(
		"Unexpected elements",
		"ZZ [in YY [in XX [in XX.java [in pp.qq [in src [in Resolve]]]]]]",
		elements
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78931
 */
public void testQualifiedName3() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveQualifiedName3.java");

	IJavaElement[] elements = codeSelect(cu, "XX.YY.ZZ", "XX.YY.ZZ");

	assertElementsEqual(
		"Unexpected elements",
		"ZZ [in YY [in XX [in XX.java [in pp.qq [in src [in Resolve]]]]]]",
		elements
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78931
 */
public void testQualifiedName4() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveQualifiedName4.java");

	IJavaElement[] elements = codeSelect(cu, "YY.ZZ", "YY.ZZ");

	assertElementsEqual(
		"Unexpected elements",
		"ZZ [in YY [in XX [in XX.java [in pp.qq [in src [in Resolve]]]]]]",
		elements
	);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=78931
 */
public void testQualifiedName5() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "", "ResolveQualifiedName5.java");

	IJavaElement[] elements = codeSelect(cu, "YY.ZZ", "YY.ZZ");

	assertElementsEqual(
		"Unexpected elements",
		"ZZ [in YY [in XX [in ResolveQualifiedName5 [in ResolveQualifiedName5.java [in <default> [in src [in Resolve]]]]]]]",
		elements
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84001
public void testTypeInsideConstructor() throws JavaModelException {
	ICompilationUnit imported = null;
	try {
		imported = getWorkingCopy(
				"/Resolve/src/test/AType.java",
				"public class AType {\n" +
				"	public class Sub {\n" +
				"	}\n" +
				"}\n");

		IJavaElement[] elements = select(
				"/Resolve/src/test/Test.java",
				"public class Test<\n" +
				"	void foo() {\n" +
				"	  new Test.Sub();\n" +
				"	}\n" +
				"}\n",
				"Test");

		assertElementsEqual(
			"Unexpected elements",
			"Test [in [Working copy] Test.java [in test [in src [in Resolve]]]]",
			elements
		);
	} finally {
		if(imported != null) {
			imported.discardWorkingCopy();
		}
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=79309
public void testMemberTypeInImport() throws JavaModelException {
	ICompilationUnit imported = null;
	try {
		imported = getWorkingCopy(
				"/Resolve/src/test/AType.java",
				"public class AType {\n" +
				"	public class Sub {\n" +
				"	}\n" +
				"}\n");

		IJavaElement[] elements = select(
				"/Resolve/src/test/Test.java",
				"import test.AType.Sub;\n" +
				"public class Test\n" +
				"}\n",
				"Sub");

		assertElementsEqual(
			"Unexpected elements",
			"Sub [in AType [in [Working copy] AType.java [in test [in src [in Resolve]]]]]",
			elements
		);
	} finally {
		if(imported != null) {
			imported.discardWorkingCopy();
		}
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=99901
public void testSingleNameInImport() throws JavaModelException {
	ICompilationUnit aType = null;
	try {
		aType = getWorkingCopy(
				"/Resolve/src/zzz/AType.java",
				"package zzz;\n" +
				"public class AType {\n" +
				"}\n");

		IJavaElement[] elements = select(
				"/Resolve/src/test/Test.java",
				"package test;\n" +
				"import zzz.*;\n" +
				"public class Test\n" +
				"}\n",
				"zzz");

		assertElementsEqual(
			"Unexpected elements",
			"zzz [in src [in Resolve]]",
			elements
		);
	} finally {
		if(aType != null) {
			aType.discardWorkingCopy();
		}
	}
}
/**
 * Bug 120350: [model] Secondary type not found by code resolve
 */
public void testSecondaryTypes() throws JavaModelException {
	waitUntilIndexesReady();
	ICompilationUnit cu = getCompilationUnit("Resolve", "src", "b120350", "X.java");
	String str = cu.getSource();
	int start = str.indexOf("Secondary");
	int length = "Secondary".length();
	IJavaElement[] elements = cu.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"Secondary [in Test.java [in b120350 [in src [in Resolve]]]]",
		elements
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=192497
public void testSelectOnCursor1() throws JavaModelException {
	ICompilationUnit cu = getWorkingCopy(
			"/Resolve/src/AType.java",
			"public class AType {\n" +
			"  public void doLoad(){}\n" +
			"  public void foo() {\n" +
			"    doLoad();\n" +
			"  }\n" +
			"}\n");

	String str = cu.getSource();
	// perform code select between 'd' and 'o'
	int start = str.indexOf("oLoad();");
	int length = 0;
	IJavaElement[] elements = cu.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"doLoad() [in AType [in [Working copy] AType.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=207572
public void testSelectOnCursor2() throws JavaModelException {
	ICompilationUnit cu = getWorkingCopy(
			"/Resolve/src/AType.java",
			"public class X {\n" +
			"        Object o;\n" +
			"\n" +
			"        String foo() {\n" +
			"                return \"aaa\n" +
			"        }\n" +
			"}n");

	String str = cu.getSource();

	int start = str.indexOf("foo") + "fo".length();
	int length = 0;
	IJavaElement[] elements = cu.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"foo() [in X [in [Working copy] AType.java [in <default> [in src [in Resolve]]]]]",
		elements
	);
}


/*
 * Ensures that the first type is found when defined in 2 different roots by working copies.
 * (regression test for 194399 IJavaProject.findType(String, String, WorkingCopyOwner) doesn't return the same element with different VMs.)
 */
public void testWorkingCopyOrder1() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Resolve/src/test/p/Type.java",
		"package test.p;\n" +
		"public class Type {\n" +
		"}\n"
	);
	this.workingCopies[1] = getWorkingCopy(
		"/Resolve/src2/test/p/Type.java",
		"package test.p;\n" +
		"public class Type {\n" +
		"}\n"
	);
	IJavaProject javaProject = getJavaProject("Resolve");
	IType foundType = javaProject.findType("test.p", "Type", this.wcOwner);
	assertElementEquals(
		"Unexpected type",
		"Type [in [Working copy] Type.java [in test.p [in src [in Resolve]]]]",
		foundType);
}

/*
 * Ensures that a working copy is put in the right order on the classpath (thus it is found before a type on another classpath entry)
 * (regression test for bug 194432 IJavaProject.findType(String, String, WorkingCopyOwner) return the wrong duplicate element)
 */
public void testWorkingCopyOrder2() throws Exception {
	String jarName = "bug194432.jar";
	String srcName = "bug194432_src.zip";
	try {
		String[] pathAndContents = new String[] {
			"test/p/Type.java",
			"package test.p;\n" +
			"public class Type {\n" +
			"}\n"
		};
		addLibrary(jarName, srcName, pathAndContents, JavaCore.VERSION_1_4);
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
			"/Resolve/src/test/p/Type.java",
			"package test.p;\n" +
			"public class Type {\n" +
			"}\n"
		);
		IJavaProject javaProject = getJavaProject("Resolve");
		IType foundType = javaProject.findType("test.p", "Type", this.wcOwner);
		assertElementEquals(
			"Unexpected type",
			"Type [in [Working copy] Type.java [in test.p [in src [in Resolve]]]]",
			foundType);
	} finally {
		removeLibrary(this.currentProject, jarName, srcName);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=221215
public void testInvalidField1() throws JavaModelException {
	ICompilationUnit cu = getWorkingCopy(
		"/Resolve/src/test/Test.java",
		"package test;"+
		"public class Event {\n" +
		"        public int x;\n" +
		"\n" +
		"        public void handle(Event e) {\n" +
		"                e.x.eee.foo();\n" +
		"        }\n" +
		"}");
	String str = cu.getSource();

	int start = str.indexOf("eee") + "e".length();
	int length = 0;
	IJavaElement[] elements = cu.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"",
		elements
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=221215 - variation
public void testInvalidField2() throws JavaModelException {
	ICompilationUnit cu = getWorkingCopy(
		"/Resolve/src/test/Test.java",
		"package test;"+
		"public class Event {\n" +
		"        public int x;\n" +
		"\n" +
		"        public void handle(Event e) {\n" +
		"                this.x.eee.foo();\n" +
		"        }\n" +
		"}");
	String str = cu.getSource();

	int start = str.indexOf("eee") + "e".length();
	int length = 0;
	IJavaElement[] elements = cu.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"",
		elements
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=221215 - variation
public void testInvalidField3() throws JavaModelException {
	ICompilationUnit cu = getWorkingCopy(
		"/Resolve/src/test/Test.java",
		"package test;"+
		"public class Event {\n" +
		"        public int x;\n" +
		"\n" +
		"        public void handle(Event e) {\n" +
		"                e.x.e.foo();\n" +
		"        }\n" +
		"}");
	String str = cu.getSource();

	int start = str.indexOf("foo") + "fo".length();
	int length = 0;
	IJavaElement[] elements = cu.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"",
		elements
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=221215 - variation
public void testInvalidField4() throws JavaModelException {
	ICompilationUnit cu = getWorkingCopy(
		"/Resolve/src/test/Test.java",
		"package test;"+
		"public class Event {\n" +
		"        public int x;\n" +
		"\n" +
		"        public void handle(Event e) {\n" +
		"                this.x.e.foo();\n" +
		"        }\n" +
		"}");
	String str = cu.getSource();

	int start = str.indexOf("foo") + "fo".length();
	int length = 0;
	IJavaElement[] elements = cu.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"",
		elements
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=221215 - variation
public void testInvalidMethod1() throws JavaModelException {
	ICompilationUnit cu = getWorkingCopy(
		"/Resolve/src/test/Test.java",
		"package test;"+
		"public class Event {\n" +
		"        public int x;\n" +
		"\n" +
		"        public void handle(Event e) {\n" +
		"                e.x.e().foo();\n" +
		"        }\n" +
		"}");
	String str = cu.getSource();

	int start = str.indexOf("foo") + "fo".length();
	int length = 0;
	IJavaElement[] elements = cu.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"",
		elements
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=221215 - variation
public void testInvalidMethod2() throws JavaModelException {
	ICompilationUnit cu = getWorkingCopy(
		"/Resolve/src/test/Test.java",
		"package test;"+
		"public class Event {\n" +
		"        public int x;\n" +
		"\n" +
		"        public void handle(Event e) {\n" +
		"                this.x.e().foo();\n" +
		"        }\n" +
		"}");
	String str = cu.getSource();

	int start = str.indexOf("foo") + "fo".length();
	int length = 0;
	IJavaElement[] elements = cu.codeSelect(start, length);
	assertElementsEqual(
		"Unexpected elements",
		"",
		elements
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=235658
// To verify that "open declaration" works for a protected interface, which is
// an inner type of an extending class's superclass.
public void testInterfaceX() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Resolve/src/test/Bug.java",
		"package test;\n"+
		"public class Bug {\n" +
		"  void foo() {}\n" +
		"  protected interface Proto {}\n" +
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Resolve/src/Type.java",
		"import test.Bug;\n"+
		"import test.Bug.*;\n"+
		"class Type extends Bug implements Proto {\n" +
		"}\n");

	String str = this.workingCopies[1].getSource();
	int start = str.lastIndexOf("Proto");
	int length = "Proto".length();
	IJavaElement[] elements =  this.workingCopies[1].codeSelect(start, length, this.wcOwner);

	assertElementsEqual(
			"Unexpected elements",
			"Proto [in Bug [in [Working copy] Bug.java [in test [in src [in Resolve]]]]]",
			elements
	);
}
public void test306078() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Resolve/src/A.java",
		"public class A { private static final int X = 0; }");

	this.workingCopies[1] = getWorkingCopy(
		"/Resolve/src/B.java",
		"public class B { int x = A.X; }");

	String str = this.workingCopies[1].getSource();
	int start = str.lastIndexOf("X");
	int length = "X".length();
	IJavaElement[] elements =  this.workingCopies[1].codeSelect(start, length, this.wcOwner);

	assertElementsEqual(
			"Unexpected elements",
			"X [in A [in [Working copy] A.java [in <default> [in src [in Resolve]]]]]",
			elements
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=299384
public void testCodeSelectInHybrid1415Projects() throws CoreException, IOException {
	String jarName = "bug299384.jar";
	String srcName = "bug299384_src.zip";
	try {
		String[] pathAndContents = new String[] {
			"TestSuite.java",
			"public class TestSuite {\n" +
			"    public TestSuite(final Class<? extends TestCase> p) {}\n" +
			"}\n" +
			"class TestCase {}\n"
		};

		addLibrary(jarName, srcName, pathAndContents, JavaCore.VERSION_1_5);

		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
			"/Resolve/src/Test.java",
			"public class TextEditTests extends TestCase {\n" +
			"	{\n" +
			"		new TestSuite(TextEditTests.class);\n" +
			"	}\n" +
			"}\n");


		String str = this.workingCopies[0].getSource();
		int start = str.lastIndexOf("TestSuite");
		int length = "TestSuite".length();
		IJavaElement[] elements =  this.workingCopies[0].codeSelect(start, length, this.wcOwner);

		assertElementsEqual(
			"Unexpected elements",
			"TestSuite(java.lang.Class) [in TestSuite [in TestSuite.class [in <default> [in bug299384.jar [in Resolve]]]]]",
			elements
		);
	} finally {
		removeLibrary(this.currentProject, jarName, srcName);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=342054
public void testMethodParameter() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Resolve/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"	void foo(int x) {\n" +
		"	}\n" +
		"}");

	String str = this.workingCopies[0].getSource();
	int start = str.lastIndexOf("x");
	int length = "x".length();
	IJavaElement[] elements =  this.workingCopies[0].codeSelect(start, length, this.wcOwner);

	assertElementsEqual(
			"Unexpected elements",
			"x [in foo(int) [in Test [in [Working copy] Test.java [in test [in src [in Resolve]]]]]]",
			elements
		);
	assertTrue("Not a parameter", ((ILocalVariable)elements[0]).isParameter());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=244544
public void testConstantInLocal() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Resolve/src/Test2.java",
			"class X {\n" +
			"    public static void main(String[] args) {\n" +
			"        class Local {\n" +
			"            private static final long CONSTANT = 1L; // code select fails\n" +
			"        }\n" +
			"        new X() {\n" +
			"            private static final long FINAL = 1L; // code select fails\n" +
			"        };\n" +
			"    }\n" +
			"}\n");

	String str = this.workingCopies[0].getSource();
	String selectAt = "FINAL";
	String selection = "FINAL";
	int start = str.indexOf(selectAt);
	int length = selection.length();
	IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length, this.wcOwner);

	assertElementsEqual(
			"Unexpected elements",
			"FINAL [in <anonymous #1> [in main(String[]) [in X [in [Working copy] Test2.java [in <default> [in src [in Resolve]]]]]]]",
			elements);

	selectAt = "CONSTANT";
	selection = "CONSTANT";
	start = str.indexOf(selectAt);
	length = selection.length();
	elements = this.workingCopies[0].codeSelect(start, length, this.wcOwner);

	assertElementsEqual(
			"Unexpected elements",
			"CONSTANT [in Local [in main(String[]) [in X [in [Working copy] Test2.java [in <default> [in src [in Resolve]]]]]]]",
			elements);
}
}
