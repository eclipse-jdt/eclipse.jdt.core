/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.tests.util.Util;

import junit.framework.Test;

public class TypeResolveTests extends ModifyingResourceTests {
	ICompilationUnit cu;
public TypeResolveTests(String name) {
	super(name);
}
private IType getType(IType[] types, String sourceTypeName) throws JavaModelException {
	for (int i = 0; i < types.length; i++) {
		IType sourceType = types[i];
		if (sourceType.getTypeQualifiedName().equals(sourceTypeName)) {
			return sourceType;
		} else if ((sourceType = getType(sourceType.getTypes(), sourceTypeName)) != null) {
			return sourceType;
		}
	}
	return null;
}
private IType getType(String sourceTypeName) throws JavaModelException {
	return getType(this.cu.getTypes(), sourceTypeName);
}
private String[][] resolveType(String typeName, String sourceTypeName) throws JavaModelException {
	IType sourceType = this.getType(sourceTypeName);
	assertTrue("Type " + sourceTypeName + " was not found", sourceType != null);
	return sourceType.resolveType(typeName);
}
protected void assertTypesEqual(String expected, String[][] types) {
	StringBuffer buffer = new StringBuffer();
	if(types != null) {
		for (int i = 0, length = types.length; i < length; i++) {
			String[] qualifiedName = types[i];
			String packageName = qualifiedName[0];
			if (packageName.length() > 0) {
				buffer.append(packageName);
				buffer.append(".");
			}
			buffer.append(qualifiedName[1]);
			if (i < length-1) {
				buffer.append("\n");
			}
		}
	} else {
		buffer.append("<null>");
	}
	String actual = buffer.toString();
	if (!expected.equals(actual)) {
	 	System.out.print(Util.displayString(actual, 2));
	 	System.out.println(",");
	}
	assertEquals(
		"Unexpected types",
		expected,
		actual);
}
/* (non-Javadoc)
 * @see org.eclipse.jdt.core.tests.model.AbstractJavaModelTests#setUpSuite()
 */
public void setUpSuite() throws Exception {
	super.setUpSuite();
	setUpJavaProject("TypeResolve");
	this.cu = this.getCompilationUnit("TypeResolve", "src", "p", "TypeResolve.java");
	addLibrary("myLib.jar", "myLibsrc.zip", new String[] {
			"p1/X.java",
			"package p1;\n" +
			"public class X {\n" +
			"}",
			"p2/Y.java",
			"package p2;\n" +
			"import p1.X;\n" +
			"public class Y {\n" +
			"  class Member {\n" +
			"    X field;\n" +
			"  }\n" +
			"  X foo() {\n" +
			"   return new X() {};" +
			"  }\n" +
			"}",
		}, JavaCore.VERSION_1_4);
}
	static {
//		TESTS_NUMBERS = new int[] { 182, 183 };
//		TESTS_NAMES = new String[] {"test0177"};
	}
	public static Test suite() {
		return buildModelTestSuite(TypeResolveTests.class);
	}
/* (non-Javadoc)
 * @see org.eclipse.jdt.core.tests.model.SuiteOfTestCases#tearDownSuite()
 */
public void tearDownSuite() throws Exception {
	deleteProject("TypeResolve");
	super.tearDownSuite();
}
/**
 * Resolve the type "B" within one of the secondary types.
 * (regression test for bug 23829 IType::resolveType incorrectly returns null)
 */
public void testResolveInSecondaryType() throws JavaModelException {
	IType type = getCompilationUnit("/TypeResolve/src/p3/B.java").getType("Test");
	String[][] types = type.resolveType("B");
	assertTypesEqual(
		"p3.B",
		types);
}
/**
 * Resolve the type "B" within one of its inner classes.
 */
public void testResolveMemberTypeInInner() throws JavaModelException {
	String[][] types = resolveType("B", "TypeResolve$A$B$D");
	assertTypesEqual(
		"p.TypeResolve.A.B",
		types);
}
/*
 * Resolve a parameterized type
 * (regression test for bug 94903 Error setting method breakpoint in 1.5 project)
 */
public void testResolveParameterizedType() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[] {"JCL15_LIB"}, "bin", "1.5");
		createFile(
			"/P/src/X.java",
			"public class X<T> {\n" +
			"  X<String> field;\n" +
			"}"
		);
		IType type = getCompilationUnit("/P/src/X.java").getType("X");
		String[][] types = type.resolveType("X<String>");
		assertTypesEqual(
			"X",
			types);
	} finally {
		deleteProject("P");
	}
}
/**
 * Resolve the type "C" within one of its sibling classes.
 */
public void testResolveSiblingTypeInInner() throws JavaModelException {
	String[][] types = resolveType("C", "TypeResolve$A$B");
	assertTypesEqual(
		"p.TypeResolve.A.C",
		types);
}
/*
 * Resolve the type "X" within a top level binary type.
 */
public void testResolveTypeInBinary1() throws JavaModelException {
	IType type = getPackageFragmentRoot("/TypeResolve/myLib.jar").getPackageFragment("p2").getClassFile("Y.class").getType();
	String[][] types = type.resolveType("X");
	assertTypesEqual(
		"p1.X",
		types);
}
/*
 * Resolve the type "X" within a member binary type.
 */
public void testResolveTypeInBinary2() throws JavaModelException {
	IType type = getPackageFragmentRoot("/TypeResolve/myLib.jar").getPackageFragment("p2").getClassFile("Y$Member.class").getType();
	String[][] types = type.resolveType("X");
	assertTypesEqual(
		"p1.X",
		types);
}
/*
 * Resolve the type "X" within an anonymous binary type.
 */
public void testResolveTypeInBinary3() throws JavaModelException {
	IType type = getPackageFragmentRoot("/TypeResolve/myLib.jar").getPackageFragment("p2").getClassFile("Y$1.class").getType();
	String[][] types = type.resolveType("X");
	assertTypesEqual(
		"p1.X",
		types);
}
/*
 * Resolve the type "int" within a member binary type with a constructor.
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=212224 )
 */
public void testResolveTypeInBinary4() throws Exception {
	try {
		addLibrary("lib212224.jar", "lib212224src.zip", new String[] {
			"X212224.java",
			"public class X212224 {\n" +
			"  public class Member {\n" +
			"    Member(int i) {\n" +
			"    }\n" +
			"  }\n" +
			"}"
		}, "1.4");
		IType type = getPackageFragmentRoot("/TypeResolve/lib212224.jar").getPackageFragment("").getClassFile("X212224$Member.class").getType();
		String[][] types = type.resolveType("int");
		assertTypesEqual(
			"<null>",
			types);
	} finally {
		removeLibrary(this.currentProject, "lib212224.jar", "lib212224src.zip");
	}
}
/**
 * Resolve the type "X" with a type import for it
 * within an inner class
 */
public void testResolveTypeInInner() throws JavaModelException {
	String[][] types = resolveType("X", "TypeResolve$A");
	assertTypesEqual(
		"p1.X",
		types);
}
/**
 * Resolve the type "Object" within a local class.
 * (regression test for bug 48350 IType#resolveType(String) fails on local types)
 */
public void testResolveTypeInInner2() throws JavaModelException {
	IType type = getCompilationUnit("/TypeResolve/src/p5/A.java").getType("A").getMethod("foo", new String[] {}).getType("Local", 1);

	String[][] types = type.resolveType("Object");
	assertTypesEqual(
		"java.lang.Object",
		types);
}
/**
 * Resolve the type "String".
 */
public void testResolveTypeInJavaLang() throws JavaModelException {
	String[][] types = resolveType("String", "TypeResolve");
	assertTypesEqual(
		"java.lang.String",
		types);
}
/**
 * Resolve the type "Vector" with no imports.
 */
public void testResolveTypeWithNoImports() throws JavaModelException {
	String[][] types = resolveType("Vector", "TypeResolve");
	assertTypesEqual(
		"<null>",
		types);
}
/**
 * Resolve the type "Y" with an on-demand import.
 */
public void testResolveTypeWithOnDemandImport() throws JavaModelException {
	String[][] types = resolveType("Y", "TypeResolve");
	assertTypesEqual(
		"p2.Y",
		types);
}
/**
 * Resolve the type "X" with a type import for it.
 */
public void testResolveTypeWithTypeImport() throws JavaModelException {
	String[][] types = resolveType("X", "TypeResolve");
	assertTypesEqual(
		"p1.X",
		types);
}
/**
 * Resolve the type "String".
 */
public void testResolveString() throws JavaModelException {
	String[][] types = resolveType("String", "TypeResolve");
	assertTypesEqual(
		"java.lang.String",
		types);
}
/**
 * Resolve the type "A.Inner".
 */
public void testResolveInnerType1() throws JavaModelException {
	IType type = getCompilationUnit("/TypeResolve/src/p4/B.java").getType("B");
	String[][] types = type.resolveType("A.Inner");
	assertTypesEqual(
		"p4.A.Inner",
		types);
}
/**
 * Resolve the type "p4.A.Inner".
 */
public void testResolveInnerType2() throws JavaModelException {
	IType type = getCompilationUnit("/TypeResolve/src/p4/B.java").getType("B");
	String[][] types = type.resolveType("p4.A.Inner");
	assertTypesEqual(
		"p4.A.Inner",
		types);
}
}
