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
package org.eclipse.jdt.core.tests.compiler.parser;

import java.util.Locale;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblem;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

public class SyntaxErrorTest extends org.eclipse.jdt.core.tests.junit.extension.TestCase {
	public static boolean optimizeStringLiterals = false;
	public static boolean assertMode = false;
	
	public SyntaxErrorTest(String testName){
	super(testName);
}
public void checkParse(
	char[] source, 
	String expectedSyntaxErrorDiagnosis,
	String testName) {

	/* using regular parser in DIET mode */
	Parser parser = 
		new Parser(
			new ProblemReporter(
				DefaultErrorHandlingPolicies.proceedWithAllProblems(), 
				new CompilerOptions(), 
				new DefaultProblemFactory(Locale.getDefault())),
			optimizeStringLiterals,
			assertMode);
	ICompilationUnit sourceUnit = new CompilationUnit(source, testName, null);
	CompilationResult compilationResult = new CompilationResult(sourceUnit, 0, 0, 0);	
	
	parser.parse(sourceUnit, compilationResult);

	StringBuffer buffer = new StringBuffer(100);
	if (compilationResult.hasProblems() || compilationResult.hasTasks()) {
		IProblem[] problems = compilationResult.getAllProblems();
		int count = problems.length;
		int problemCount = 0;
		for (int i = 0; i < count; i++) { 
			if (problems[i] != null) {
				if (problemCount == 0)
					buffer.append("----------\n");
				problemCount++;
				buffer.append(problemCount + (problems[i].isError() ? ". ERROR" : ". WARNING"));
				buffer.append(" in " + new String(problems[i].getOriginatingFileName()));
				try {
					buffer.append(((DefaultProblem)problems[i]).errorReportSource(compilationResult.compilationUnit));
					buffer.append("\n");
					buffer.append(problems[i].getMessage());
					buffer.append("\n");
				} catch (Exception e) {
				}
				buffer.append("----------\n");
			}
		};
	}
	String computedSyntaxErrorDiagnosis = buffer.toString();
 	//System.out.println(Util.displayString(computedSyntaxErrorDiagnosis));
	assertEquals(
		"Invalid syntax error diagnosis" + testName,
		expectedSyntaxErrorDiagnosis,
		computedSyntaxErrorDiagnosis);
}
/*
 * Should diagnose parenthesis mismatch
 */
public void test01() {

	String s = 
		"public class X {								\n"+
		" public void solve(){							\n"+
		"												\n"+
		"  X[] results = new X[10];						\n"+
		"  for(int i = 0; i < 10; i++){					\n"+
		"   X result = results[i];						\n"+
		"   boolean found = false;						\n"+
		"   for(int j = 0; j < 10; j++){				\n"+
		"    if (this.equals(result.documentName){		\n"+
		"     found = true;								\n"+
		"     break;									\n"+
		"    }											\n"+
		"   }											\n"+
		"  }											\n"+
		"  return andResult;							\n"+
		" }												\n"+
		"}												\n"; 	

	String expectedSyntaxErrorDiagnosis =
		"----------\n" +
		"1. ERROR in <parenthesis mismatch> (at line 9)\n" + 
		"	if (this.equals(result.documentName){		\n" + 
		"	   ^\n" + 
		"Unmatched bracket\n" + 
		"----------\n" + 
		"2. ERROR in <parenthesis mismatch> (at line 9)\n" + 
		"	if (this.equals(result.documentName){		\n" + 
		"	                                    ^\n" + 
		"Syntax error on token \"{\", \")\" expected\n" + 
		"----------\n";

	String testName = "<parenthesis mismatch>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
/*
 * Should diagnose brace mismatch
 */
public void test02() {

	String s = 
		"class Bar {			\n"+				
		"	Bar() {				\n"+
		"		this(fred().x{);\n"+
		"	}					\n"+						
		"}						\n"; 	

	String expectedSyntaxErrorDiagnosis =
		"----------\n" +
		"1. ERROR in <brace mismatch> (at line 3)\n" + 
		"	this(fred().x{);\n" + 
		"	    ^\n" + 
		"Unmatched bracket\n" + 
		"----------\n" + 
		"2. ERROR in <brace mismatch> (at line 3)\n" + 
		"	this(fred().x{);\n" + 
		"	             ^\n" + 
		"Syntax error on token \"{\", \")\" expected\n" + 
		"----------\n";

	String testName = "<brace mismatch>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
/*
 * Should diagnose parenthesis mismatch
 */
public void test03() {

	String s = 
		"public class X { // should complain	\n"+
		"	int foo(							\n"+
		"		[ arg1, 						\n"+
		"		{ arg2, ]						\n"+
		"		  arg3, 						\n"+
		"	){									\n"+
		"	}									\n"+
		"}										\n"; 	

	String expectedSyntaxErrorDiagnosis =
		"----------\n" + 
		"1. ERROR in <parenthesis mismatch> (at line 2)\n" + 
		"	int foo(							\n" + 
		"	       ^\n" + 
		"Unmatched bracket\n" + 
		"----------\n" + 
		"2. ERROR in <parenthesis mismatch> (at line 3)\n" + 
		"	[ arg1, 						\n" + 
		"	^\n" + 
		"Syntax error on token \"[\", \"float\", \"double\", \"byte\", \"short\", \"int\", \"long\", \"char\", \"boolean\", \"void\", \"Identifier\" expected\n" + 
		"----------\n";

	String testName = "<parenthesis mismatch>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
/*
 * Should not diagnose parenthesis mismatch
 */
public void test04() {

	String s = 
		"public class X { // should not complain	\n"+
		"	int foo(								\n"+
		"		{ arg1, 							\n"+
		"		{ arg2, }							\n"+
		"		  arg3, }							\n"+
		"	){										\n"+
		"	}										\n"+
		"}											\n"; 	

	String expectedSyntaxErrorDiagnosis =
		"----------\n" + 
		"1. ERROR in <no parenthesis mismatch> (at line 3)\n" + 
		"	{ arg1, 							\n" + 
		"	^\n" + 
		"Syntax error on token \"{\", \"float\", \"double\", \"byte\", \"short\", \"int\", \"long\", \"char\", \"boolean\", \"void\", \"Identifier\" expected\n" + 
		"----------\n";

	String testName = "<no parenthesis mismatch>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
}
