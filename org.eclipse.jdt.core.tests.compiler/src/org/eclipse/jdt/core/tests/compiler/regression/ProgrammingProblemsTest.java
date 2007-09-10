/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import junit.framework.Test;

/* Collects potential programming problems tests that are not segregated in a
 * dedicated test class (aka NullReferenceTest). */
public class ProgrammingProblemsTest extends AbstractRegressionTest {

public ProgrammingProblemsTest(String name) { 
    super(name);
}

	// Static initializer to specify tests subset using TESTS_* static variables
  	// All specified tests which does not belong to the class are skipped...
  	// Only the highest compliance level is run; add the VM argument
  	// -Dcompliance=1.4 (for example) to lower it if needed
  	static {
//    	TESTS_NAMES = new String[] { "test001" };
//    	TESTS_NUMBERS = new int[] { 1 };   
//  	TESTS_RANGE = new int[] { 1, -1 }; 
  	}

public static Test suite() {
    return buildAllCompliancesTestSuite(testClass());
}
  
public static Class testClass() {
    return ProgrammingProblemsTest.class;
}

void runTest(
		String[] testFiles,
		String[] errorOptions,
		String[] warningOptions,
		String[] ignoreOptions,
		boolean expectingCompilerErrors,
		String expectedCompilerLog,
		String expectedOutputString,
		boolean forceExecution,
		String[] classLib,
		boolean shouldFlushOutputDirectory, 
		String[] vmArguments, 
		Map customOptions,
		ICompilerRequestor clientRequestor,
		boolean skipJavac) {
	Map compilerOptions = customOptions;
	if (errorOptions != null || warningOptions != null || 
			ignoreOptions != null) {
		if (compilerOptions == null) {
			compilerOptions = new HashMap();
		}
		if (errorOptions != null) {
		    for (int i = 0; i < errorOptions.length; i++) {
		    	compilerOptions.put(errorOptions[i], CompilerOptions.ERROR);
		    }
		}
		if (warningOptions != null) {
		    for (int i = 0; i < warningOptions.length; i++) {
		    	compilerOptions.put(warningOptions[i], CompilerOptions.WARNING);
		    }
		}
		if (ignoreOptions != null) {
		    for (int i = 0; i < ignoreOptions.length; i++) {
		    	compilerOptions.put(ignoreOptions[i], CompilerOptions.IGNORE);
		    }
		}
	}
	runTest(testFiles,
		expectingCompilerErrors,
		expectedCompilerLog,
		expectedOutputString,
		forceExecution,
		classLib,
		shouldFlushOutputDirectory, 
		vmArguments, 
		compilerOptions,
		clientRequestor,
		skipJavac);
}

// default behavior upon unread parameters
public void test0001_unread_parameters() {
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"  public void foo(boolean b) {\n" + 
			"  }\n" + 
			"}\n"
			},
		null /* errorOptions */,
		null /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */, 
		null /* vmArguments */, 
		null /* customOptions */,
		null /* clientRequestor */,
		false /* skipJavac */);
}

// reporting unread paramaters as warning
public void test0002_unread_parameters() {
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"  public void foo(boolean b) {\n" + 
			"  }\n" + 
			"}\n"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedParameter
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"----------\n" + 
		"1. WARNING in X.java (at line 2)\r\n" + 
		"	public void foo(boolean b) {\r\n" + 
		"	                        ^\n" + 
		"The parameter b is never read\n" + 
		"----------\n" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */, 
		null /* vmArguments */, 
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}

// disabling the reporting of unread parameters using the Javadoc
// @param disables by default
public void test0003_unread_parameters() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_DocCommentSupport, 
			CompilerOptions.ENABLED);
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"/** @param b mute warning **/\n" + 
			"  public void foo(boolean b) {\n" + 
			"  }\n" + 
			"}\n"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedParameter
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */, 
		null /* vmArguments */, 
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}

// disabling the reporting of unread parameters using the Javadoc
// @param disabling can be disabled
public void test0004_unread_parameters() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_DocCommentSupport, 
			CompilerOptions.ENABLED);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedParameterIncludeDocCommentReference, 
			CompilerOptions.DISABLED);
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"/** @param b mute warning **/\n" + 
			"  public void foo(boolean b) {\n" + 
			"  }\n" + 
			"}\n"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedParameter
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"----------\n" + 
		"1. WARNING in X.java (at line 3)\n" + 
		"	public void foo(boolean b) {\n" + 
		"	                        ^\n" + 
		"The parameter b is never read\n" + 
		"----------\n" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */, 
		null /* vmArguments */, 
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}

// disabling the reporting of unread parameters using SuppressWarnings
public void test0005_unread_parameters() {
	if (COMPLIANCE_1_5.compareTo(complianceLevel) <= 0) {
		runTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"@SuppressWarnings(\"unused\")\n" + // most specific token
				"  public void foo(boolean b) {\n" + 
				"  }\n" + 
				"@SuppressWarnings(\"all\")\n" + // least specific token
				"  public void foo(int i) {\n" + 
				"  }\n" + 
				"}\n"
				},
			null /* errorOptions */,
			new String[] {
				CompilerOptions.OPTION_ReportUnusedParameter
				} /* warningOptions */,
			null /* ignoreOptions */,
			false /* expectingCompilerErrors */,
			"" /* expectedCompilerLog */,
			"" /* expectedOutputString */,
			false /* forceExecution */,
			null /* classLib */,
			true /* shouldFlushOutputDirectory */, 
			null /* vmArguments */, 
			null /* customOptions */,
			null /* clientRequestor */,
			true /* skipJavac */);
	}
}

// reporting unread paramaters as error
public void test0006_unread_parameters() {
	runTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"  public void foo(boolean b) {\n" + 
			"  }\n" + 
			"}\n"
			},
		new String[] {
			CompilerOptions.OPTION_ReportUnusedParameter
			} /* errorOptions */,
		null /* warningOptions */,
		null /* ignoreOptions */,
		true /* expectingCompilerErrors */,
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\r\n" + 
		"	public void foo(boolean b) {\r\n" + 
		"	                        ^\n" + 
		"The parameter b is never read\n" + 
		"----------\n" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */, 
		null /* vmArguments */, 
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}

// default behavior upon unnecessary declaration of thrown checked exceptions
public void test0007_declared_thrown_checked_exceptions() {
	runTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" + 
			"public class X {\n" + 
			"  public void foo() throws IOException {\n" + 
			"  }\n" + 
			"}\n"
			},
		null /* errorOptions */,
		null /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */, 
		null /* vmArguments */, 
		null /* customOptions */,
		null /* clientRequestor */,
		false /* skipJavac */);
}

// reporting unnecessary declaration of thrown checked exceptions as warning
public void test0008_declared_thrown_checked_exceptions() {
	runTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" + 
			"public class X {\n" + 
			"  public void foo() throws IOException {\n" + 
			"  }\n" + 
			"}\n"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"----------\n" + 
		"1. WARNING in X.java (at line 3)\n" + 
		"	public void foo() throws IOException {\n" + 
		"	                         ^^^^^^^^^^^\n" + 
		"The declared exception IOException is not actually thrown by the method foo() from type X\n" + 
		"----------\n" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */, 
		null /* vmArguments */, 
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}

// disabling the reporting of unnecessary declaration of thrown checked 
// exceptions using the Javadoc
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=73244
// @throws disables by default
public void test0009_declared_thrown_checked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_DocCommentSupport, 
			CompilerOptions.ENABLED);
	runTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" + 
			"public class X {\n" + 
			"/** @throws IOException mute warning **/\n" + 
			"  public void foo() throws IOException {\n" + 
			"  }\n" + 
			"}\n"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */, 
		null /* vmArguments */, 
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}

// disabling the reporting of unnecessary declaration of thrown checked 
// exceptions using the Javadoc
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=73244
// @throws disabling can be disabled
public void test0010_declared_thrown_checked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_DocCommentSupport, 
			CompilerOptions.ENABLED);
	customOptions.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionIncludeDocCommentReference, 
			CompilerOptions.DISABLED);
	runTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" + 
			"public class X {\n" + 
			"/** @throws IOException mute warning **/\n" + 
			"  public void foo() throws IOException {\n" + 
			"  }\n" + 
			"}\n"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"----------\n" + 
		"1. WARNING in X.java (at line 4)\n" + 
		"	public void foo() throws IOException {\n" + 
		"	                         ^^^^^^^^^^^\n" + 
		"The declared exception IOException is not actually thrown by the method foo() from type X\n" + 
		"----------\n" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */, 
		null /* vmArguments */, 
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}

// disabling the reporting of unnecessary declaration of thrown checked 
// exceptions using SuppressWarnings
public void test0011_declared_thrown_checked_exceptions() {
	if (COMPLIANCE_1_5.compareTo(complianceLevel) <= 0) {
		runTest(
			new String[] {
				"X.java",
				"import java.io.IOException;\n" + 
				"public class X {\n" + 
				"@SuppressWarnings(\"all\")\n" + // no specific token
				"  public void foo() throws IOException {\n" + 
				"  }\n" + 
				"}\n"
				},
			null /* errorOptions */,
			new String[] {
				CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
				} /* warningOptions */,
			null /* ignoreOptions */,
			false /* expectingCompilerErrors */,
			"" /* expectedCompilerLog */,
			"" /* expectedOutputString */,
			false /* forceExecution */,
			null /* classLib */,
			true /* shouldFlushOutputDirectory */, 
			null /* vmArguments */, 
			null /* customOptions */,
			null /* clientRequestor */,
			true /* skipJavac */);
	}
}

// reporting unnecessary declaration of thrown checked exceptions as error
public void test0012_declared_thrown_checked_exceptions() {
	runTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" + 
			"public class X {\n" + 
			"  public void foo() throws IOException {\n" + 
			"  }\n" + 
			"}\n"
			},
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* errorOptions */,
		null /* warningOptions */,
		null /* ignoreOptions */,
		true /* expectingCompilerErrors */,
		"----------\n" + 
		"1. ERROR in X.java (at line 3)\n" + 
		"	public void foo() throws IOException {\n" + 
		"	                         ^^^^^^^^^^^\n" + 
		"The declared exception IOException is not actually thrown by the method foo() from type X\n" + 
		"----------\n" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */, 
		null /* vmArguments */, 
		null /* customOptions */,
		null /* clientRequestor */,
		true /* skipJavac */);
}

// disabling the reporting of unnecessary declaration of thrown checked 
// exceptions using the Javadoc
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=73244
// @throws disables by default, but only exact matches work
public void test0013_declared_thrown_checked_exceptions() {
	Map customOptions = new HashMap();
	customOptions.put(CompilerOptions.OPTION_DocCommentSupport, 
			CompilerOptions.ENABLED);
	runTest(
		new String[] {
			"X.java",
			"import java.io.IOException;\n" + 
			"import java.io.EOFException;\n" + 
			"public class X {\n" + 
			"/** @throws EOFException does not mute warning for IOException **/\n" + 
			"  public void foo() throws IOException {\n" + 
			"  }\n" + 
			"}\n"
			},
		null /* errorOptions */,
		new String[] {
			CompilerOptions.OPTION_ReportUnusedDeclaredThrownException
			} /* warningOptions */,
		null /* ignoreOptions */,
		false /* expectingCompilerErrors */,
		"----------\n" + 
		"1. WARNING in X.java (at line 5)\n" + 
		"	public void foo() throws IOException {\n" + 
		"	                         ^^^^^^^^^^^\n" + 
		"The declared exception IOException is not actually thrown by the method foo() from type X\n" + 
		"----------\n" /* expectedCompilerLog */,
		"" /* expectedOutputString */,
		false /* forceExecution */,
		null /* classLib */,
		true /* shouldFlushOutputDirectory */, 
		null /* vmArguments */, 
		customOptions,
		null /* clientRequestor */,
		true /* skipJavac */);
}
}