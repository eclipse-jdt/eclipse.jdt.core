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
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class JavadocTest_1_5 extends JavadocTest {

	String docCommentSupport = CompilerOptions.ENABLED;
	String reportInvalidJavadoc = CompilerOptions.ERROR;
	String reportMissingJavadocTags = CompilerOptions.ERROR;
	String reportMissingJavadocComments = null;

	public JavadocTest_1_5(String name) {
		super(name);
	}

	public static Class javadocTestClass() {
		return JavadocTest_1_5.class;
	}

	// Use this static initializer to specify subset for tests
	// All specified tests which does not belong to the class are skipped...
	static {
		// 	Names of tests to run: can be "testBugXXXX" or "BugXXXX")
//		testsNames = new String[] {
//			"testBug70892negative1", "testBug70892negative2"
//		};
		// Numbers of tests to run: "test<number>" will be run for each number of this array
//		testsNumbers = new int[] { 3, 7, 10, 21 };
		// Range numbers of tests to run: all tests between "test<first>" and "test<last>" will be run for { first, last }
//		testsRange = new int[] { 21, 50 };
	}
	public static Test suite() {
		return buildTestSuite(javadocTestClass());
	}

	protected Map getCompilerOptions() {
		Map options = super.getCompilerOptions();
		options.put(CompilerOptions.OPTION_DocCommentSupport, this.docCommentSupport);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadoc, reportInvalidJavadoc);
		if (reportMissingJavadocComments != null) 
			options.put(CompilerOptions.OPTION_ReportMissingJavadocComments, reportMissingJavadocComments);
		else
			options.put(CompilerOptions.OPTION_ReportMissingJavadocComments, reportInvalidJavadoc);
		if (reportMissingJavadocTags != null) 
			options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, reportMissingJavadocTags);
		else
			options.put(CompilerOptions.OPTION_ReportMissingJavadocTags, reportInvalidJavadoc);
		options.put(CompilerOptions.OPTION_ReportFieldHiding, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.ERROR);
		options.put(CompilerOptions.OPTION_ReportUnusedImport, CompilerOptions.ERROR);
		return options;
	}
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		this.docCommentSupport = CompilerOptions.ENABLED;
		reportInvalidJavadoc = CompilerOptions.ERROR;
		reportMissingJavadocTags = CompilerOptions.ERROR;
		reportMissingJavadocComments = null;
	}

	/**
	 * Test fix for bug 70892: [1.5][Javadoc] Compiler should parse reference for inline tag @value
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=70892">70892</a>
	 * These two tests fail for 1.5 source level but should pass for 1.3 or 1.4
	 * @see JavadocTest_1_4
	 */
	public void testBug70892negative1() {
		reportMissingJavadocTags = CompilerOptions.IGNORE;
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"X.java",
				"/**\n" + 
					" * {@value \"invalid\"}\n" + 
					" * {@value <a href=\"invalid\">invalid</a>} invalid\n" + 
					" * {@value #field}\n" + 
					" * {@value #foo}\n" + 
					" * {@value #foo()}\n" + 
					" */\n" + 
					"public class X {\n" + 
					"	int field;\n" + 
					"	void foo() {}\n" + 
					"}\n"
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 2)\n" + 
				"	* {@value \"invalid\"}\n" + 
				"	          ^^^^^^^^^\n" + 
				"Javadoc: Only static field reference is allowed for @value tag\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 3)\n" + 
				"	* {@value <a href=\"invalid\">invalid</a>} invalid\n" + 
				"	          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Only static field reference is allowed for @value tag\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 4)\n" + 
				"	* {@value #field}\n" + 
				"	           ^^^^^\n" + 
				"Javadoc: Only static field reference is allowed for @value tag\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 5)\n" + 
				"	* {@value #foo}\n" + 
				"	           ^^^\n" + 
				"Javadoc: Only static field reference is allowed for @value tag\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 6)\n" + 
				"	* {@value #foo()}\n" + 
				"	           ^^^\n" + 
				"Javadoc: Only static field reference is allowed for @value tag\n" + 
				"----------\n"
		);
	}
	public void testBug70892negative2() {
		reportMissingJavadocTags = CompilerOptions.IGNORE;
		reportMissingJavadocComments = CompilerOptions.IGNORE;
		runNegativeTest(
			new String[] {
				"X.java",
				"/**\n" + 
					" * {@value \"invalid}\n" + 
					" * {@value <a href}\n" + 
					" * {@value <a href=\"invalid\">invalid</a} invalid\n" + 
					" * {@value #fild}\n" + 
					" * {@value #fo}\n" + 
					" * {@value #f()}\n" + 
					" */\n" + 
					"public class X {\n" + 
					"	int field;\n" + 
					"	void foo() {}\n" + 
					"}\n"	
			},
			"----------\n" + 
				"1. ERROR in X.java (at line 2)\n" + 
				"	* {@value \"invalid}\n" + 
				"	         ^^^^^^^^^^\n" + 
				"Javadoc: Invalid reference\n" + 
				"----------\n" + 
				"2. ERROR in X.java (at line 3)\n" + 
				"	* {@value <a href}\n" + 
				"	          ^^^^^^^\n" + 
				"Javadoc: Only static field reference is allowed for @value tag\n" + 
				"----------\n" + 
				"3. ERROR in X.java (at line 4)\n" + 
				"	* {@value <a href=\"invalid\">invalid</a} invalid\n" + 
				"	          ^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"Javadoc: Only static field reference is allowed for @value tag\n" + 
				"----------\n" + 
				"4. ERROR in X.java (at line 5)\n" + 
				"	* {@value #fild}\n" + 
				"	           ^^^^\n" + 
				"Javadoc: fild cannot be resolved or is not a field\n" + 
				"----------\n" + 
				"5. ERROR in X.java (at line 6)\n" + 
				"	* {@value #fo}\n" + 
				"	           ^^\n" + 
				"Javadoc: fo cannot be resolved or is not a field\n" + 
				"----------\n" + 
				"6. ERROR in X.java (at line 7)\n" + 
				"	* {@value #f()}\n" + 
				"	           ^\n" + 
				"Javadoc: The method f() is undefined for the type X\n" + 
				"----------\n" + 
				"7. ERROR in X.java (at line 7)\n" + 
				"	* {@value #f()}\n" + 
				"	           ^\n" + 
				"Javadoc: Only static field reference is allowed for @value tag\n" + 
				"----------\n"
		);
	}
}
