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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.*;

import junit.framework.Test;

public class GetSourceTests extends ModifyingResourceTests {
	
	ICompilationUnit cu;

	public GetSourceTests(String name) {
		super(name);
	}
	
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		createJavaProject("P");
		createFolder("/P/p");
		createFile(
			"/P/p/X.java",
			"package p;\n" +
			"import java.lang.*;\n" +
			"public class X {\n" +
			"  public Object field;\n" +
			"  private int s\\u0069ze;\n" +
			"  void foo(String s) {\n" +
			"    final int var1 = 2;\n" +
			"    Object var2;\n" +
			"    for (int i = 0;  i < 10; i++) {}\n" +
			"  }\n" +
			"  private int bar() {\n" +
			"    return 1;\n" +
			"  }\n" +
			"  /**\n" +
			"   * Returns the size.\n" +
			"   * @return\n" + 
			"   *     the size\n" +
			"   */\n" +
			"  int getSiz\\u0065 () {\n" +
			"    return this.size;\n" +
			"  }\n" +
			"}"
		);
		this.cu = getCompilationUnit("/P/p/X.java");
	}
	
	public static Test suite() {
		if (false) {
			Suite suite = new Suite(GetSourceTests.class.getName());
			suite.addTest(new GetSourceTests("testNameRangeAnonymous"));
			return suite;
		}
		
		return new Suite(GetSourceTests.class);
	}
	
	public void tearDownSuite() throws Exception {
		deleteProject("P");
		super.tearDownSuite();
	}
	
	/**
	 * Ensure the source for a field contains the modifiers, field
	 * type, name, and terminator.
	 */
	public void testField() throws JavaModelException {
		IType type = this.cu.getType("X");
		IField field = type.getField("field");
	
		String actualSource = field.getSource();
		String expectedSource = "public Object field;";
		assertSourceEquals("Unexpected source'", expectedSource, actualSource);
	}
	
	/**
	 * Ensure the source for an import contains the 'import' keyword,
	 * name, and terminator.
	 */
	public void testImport() throws JavaModelException {
		IImportDeclaration i = this.cu.getImport("java.lang.*");
	
		String actualSource = i.getSource();
		String expectedSource = "import java.lang.*;";
		assertSourceEquals("Unexpected source'", expectedSource, actualSource);
	}
	
	/*
	 * Ensures the source for a local variable contains the modifiers, type and name.
	 */
	public void testLocalVariable1() throws JavaModelException {
		ILocalVariable var = getLocalVariable("/P/p/X.java", "var1 = 2;", "var1");
		
		String actualSource = ((ISourceReference)var).getSource();
		String expectedSource = "final int var1 = 2;";
		assertSourceEquals("Unexpected source'", expectedSource, actualSource);
	}
	
	/*
	 * Ensures the source for a local variable contains the modifiers, type and name.
	 */
	public void testLocalVariable2() throws JavaModelException {
		ILocalVariable var = getLocalVariable("/P/p/X.java", "var2;", "var2");
		
		String actualSource = ((ISourceReference)var).getSource();
		String expectedSource = "Object var2;";
		assertSourceEquals("Unexpected source'", expectedSource, actualSource);
	}
	
	/*
	 * Ensures the source for a local variable contains the modifiers, type and name.
	 */
	public void testLocalVariable3() throws JavaModelException {
		ILocalVariable var = getLocalVariable("/P/p/X.java", "i = 0;", "i");
		
		String actualSource = ((ISourceReference)var).getSource();
		String expectedSource = "int i = 0"; // semi-colon is not part of the local declaration in a for statement
		assertSourceEquals("Unexpected source'", expectedSource, actualSource);
	}
	
	/*
	 * Ensures the source for a local variable contains the modifiers, type and name.
	 */
	public void testLocalVariable4() throws JavaModelException {
		ILocalVariable var = getLocalVariable("/P/p/X.java", "s) {", "s");
		
		String actualSource = ((ISourceReference)var).getSource();
		String expectedSource = "String s";
		assertSourceEquals("Unexpected source'", expectedSource, actualSource);
	}
	
	/**
	 * Ensure the source for a method contains the modifiers, return
	 * type, selector, and terminator.
	 */
	public void testMethod() throws JavaModelException {
		IType type = this.cu.getType("X");
		IMethod method= type.getMethod("bar", new String[0]);
	
		String actualSource = method.getSource();
		String expectedSource =
			"private int bar() {\n" + 
			"    return 1;\n" + 
			"  }";
		assertSourceEquals("Unexpected source'", expectedSource, actualSource);
	}
	
	/*
	 * Ensures the name range for a method with syntax errors in its header is correct.
	 * (regression test for bug 43139 Delete member in Outliner not working)
	 */
	public void testNameRangeMethodWithSyntaxError() throws CoreException {
		try {
			String cuSource = 
				"package p;\n" +
				"public class Y {\n" +
				"  void foo() {\n" +
				"  }\n" +
				"  void static bar() {}\n" +
				"}";
			createFile("/P/p/Y.java", cuSource);
			IMethod method= getCompilationUnit("/P/p/Y.java").getType("Y").getMethod("bar", new String[0]);
		
			String actualSource = getNameSource(cuSource, method);
			String expectedSource = "bar";
			assertSourceEquals("Unexpected source'", expectedSource, actualSource);
		} finally {
			deleteFile("/P/p/Y.java");
		}
	}
	
	/*
	 * Ensures the name range for an anonymous class is correct.
	 * (regression test for bug 44450 Strange name range for anonymous classes)
	 */
	public void testNameRangeAnonymous() throws CoreException {
		try {
			String cuSource = 
				"package p;\n" +
				"public class Y {\n" +
				"  void foo() {\n" +
				"    Y y = new Y() {};\n" +
				"    class C {\n" +
				"    }\n"+
				"  }\n" +
				"}";
			createFile("/P/p/Y.java", cuSource);
			IType anoymous = getCompilationUnit("/P/p/Y.java").getType("Y").getMethod("foo", new String[0]).getType("", 1);
		
			String actualSource = getNameSource(cuSource, anoymous);
			String expectedSource = "Y";
			assertSourceEquals("Unexpected source'", expectedSource, actualSource);
		} finally {
			deleteFile("/P/p/Y.java");
		}
	}
	
	private String getNameSource(String cuSource, IMember member) throws JavaModelException {
		ISourceRange nameRange = member.getNameRange();
		int start = nameRange.getOffset();
		int end = start+nameRange.getLength();
		String actualSource = start >= 0 && end >= start ? cuSource.substring(start, end) : "";
		return actualSource;
	}

	/**
	 * Ensure the source for a field contains the modifiers, field
	 * type, name, and terminator, and unicode characters.
	 */
	public void testUnicodeField() throws JavaModelException {
		IType type = this.cu.getType("X");
		IField field = type.getField("size");
	
		String actualSource = field.getSource();
		String expectedSource = "private int s\\u0069ze;";
		assertSourceEquals("Unexpected source'", expectedSource, actualSource);
	}
	
	/**
	 * Ensure the source for a field contains the modifiers, field
	 * type, name, and terminator, and unicode characters.
	 */
	public void testUnicodeMethod() throws JavaModelException {
		IType type = this.cu.getType("X");
		IMethod method= type.getMethod("getSize", null);
	
		String actualSource = method.getSource();
		String expectedSource = 
			"/**\n" + 
			"   * Returns the size.\n" + 
			"   * @return\n" + 
			"   *     the size\n" + 
			"   */\n" + 
			"  int getSiz\\u0065 () {\n" + 
			"    return this.size;\n" + 
			"  }";
		assertSourceEquals("Unexpected source", expectedSource, actualSource);
	}
}
