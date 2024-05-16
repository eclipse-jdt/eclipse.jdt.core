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
package org.eclipse.jdt.core.tests.compiler.parser;

import java.util.Locale;

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblem;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

public class SyntaxErrorTest extends AbstractCompilerTest {
	public static boolean optimizeStringLiterals = false;
	public static long sourceLevel = ClassFileConstants.JDK1_3; //$NON-NLS-1$

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
				new CompilerOptions(getCompilerOptions()),
				new DefaultProblemFactory(Locale.getDefault())),
			optimizeStringLiterals);
	ICompilationUnit sourceUnit = new CompilationUnit(source, testName, null);
	CompilationResult compilationResult = new CompilationResult(sourceUnit, 0, 0, 0);

	parser.parse(sourceUnit, compilationResult);

	StringBuilder buffer = new StringBuilder(100);
	if (compilationResult.hasProblems() || compilationResult.hasTasks()) {
		CategorizedProblem[] problems = compilationResult.getAllProblems();
		int count = problems.length;
		int problemCount = 0;
		char[] unitSource = compilationResult.compilationUnit.getContents();
		for (int i = 0; i < count; i++) {
			if (problems[i] != null) {
				if (problemCount == 0)
					buffer.append("----------\n");
				problemCount++;
				buffer.append(problemCount + (problems[i].isError() ? ". ERROR" : ". WARNING"));
				buffer.append(" in " + new String(problems[i].getOriginatingFileName()).replace('/', '\\'));
				try {
					buffer.append(((DefaultProblem)problems[i]).errorReportSource(unitSource));
					buffer.append("\n");
					buffer.append(problems[i].getMessage());
					buffer.append("\n");
				} catch (Exception e) {
				}
				buffer.append("----------\n");
			}
		}
	}
	String computedSyntaxErrorDiagnosis = buffer.toString();
 	//System.out.println(Util.displayString(computedSyntaxErrorDiagnosis));
	assertEquals(
		"Invalid syntax error diagnosis" + testName,
		Util.convertToIndependantLineDelimiter(expectedSyntaxErrorDiagnosis),
		Util.convertToIndependantLineDelimiter(computedSyntaxErrorDiagnosis));
}
/*
 * Should diagnose parenthesis mismatch
 */
public void test01() {

	String s =
		"""
		public class X {							\t
		 public void solve(){						\t
													\t
		  X[] results = new X[10];					\t
		  for(int i = 0; i < 10; i++){				\t
		   X result = results[i];					\t
		   boolean found = false;					\t
		   for(int j = 0; j < 10; j++){			\t
		    if (this.equals(result.documentName){	\t
		     found = true;							\t
		     break;								\t
		    }										\t
		   }										\t
		  }										\t
		  return andResult;						\t
		 }											\t
		}											\t
		""";

	String expectedSyntaxErrorDiagnosis =
		"""
		----------
		1. ERROR in <parenthesis mismatch> (at line 9)
			if (this.equals(result.documentName){	\t
			                                   ^
		Syntax error, insert ") Statement" to complete BlockStatements
		----------
		""";

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
		"""
		class Bar {		\t
			Bar() {			\t
				this(fred().x{);
			}				\t
		}					\t
		""";

	String expectedSyntaxErrorDiagnosis =
		"""
		----------
		1. ERROR in <brace mismatch> (at line 3)
			this(fred().x{);
			             ^
		Syntax error on token "{", delete this token
		----------
		""";

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
		"""
		public class X { // should complain\t
			int foo(						\t
				[ arg1, 					\t
				{ arg2, ]					\t
				  arg3, 					\t
			){								\t
			}								\t
		}									\t
		""";

	String expectedSyntaxErrorDiagnosis =
			"""
		----------
		1. ERROR in <parenthesis mismatch> (at line 3)
			[ arg1, 					\t
			^
		Syntax error on token "[", byte expected
		----------
		2. ERROR in <parenthesis mismatch> (at line 4)
			{ arg2, ]					\t
			^
		Syntax error on token "{", byte expected
		----------
		3. ERROR in <parenthesis mismatch> (at line 4)
			{ arg2, ]					\t
			        ^
		Syntax error on token "]", byte expected
		----------
		4. ERROR in <parenthesis mismatch> (at line 5)
			arg3, 					\t
			    ^
		Syntax error on token ",", FormalParameter expected after this token
		----------
		""";

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
		"""
		public class X { // should not complain\t
			int foo(							\t
				{ arg1, 						\t
				{ arg2, }						\t
				  arg3, }						\t
			){									\t
			}									\t
		}										\t
		""";

	String expectedSyntaxErrorDiagnosis =
		"""
		----------
		1. ERROR in <no parenthesis mismatch> (at line 2)
			int foo(							\t
			       ^
		Syntax error on token "(", = expected
		----------
		2. ERROR in <no parenthesis mismatch> (at line 5)
			arg3, }						\t
			^^^^
		Syntax error on token "arg3", delete this token
		----------
		3. ERROR in <no parenthesis mismatch> (at line 6)
			){									\t
			^
		Syntax error on token ")", ; expected
		----------
		""";

	String testName = "<no parenthesis mismatch>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=61189
public void test05() {

	String s =
		"""
		public class X {						\t
			public void foo() {					\t
				(X) foo(); 						\t
			}									\t
		}										\t
		""";

	String expectedSyntaxErrorDiagnosis =
		"""
		----------
		1. ERROR in <test> (at line 3)
			(X) foo(); 						\t
			  ^
		Syntax error, insert "AssignmentOperator Expression" to complete Assignment
		----------
		2. ERROR in <test> (at line 3)
			(X) foo(); 						\t
			  ^
		Syntax error, insert ";" to complete BlockStatements
		----------
		""";

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=61189
public void test06() {

	String s =
		"""
		public class X { 						\t
			public void foo(int i) {			\t
				i; 								\t
			}									\t
		}										\t
		""";

	String expectedSyntaxErrorDiagnosis =
			"""
		----------
		1. ERROR in <test> (at line 3)
			i; 								\t
			^
		Syntax error, insert "VariableDeclarators" to complete LocalVariableDeclaration
		----------
		""";

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=133292
public void test07() {

	String s =
		"""
		public class X { 										\t
			java.lang.Object o[] = { new String("SUCCESS") ; };\t
		}														\t
		""";

	String expectedSyntaxErrorDiagnosis =
		"""
		----------
		1. ERROR in <test> (at line 2)
			java.lang.Object o[] = { new String("SUCCESS") ; };\t
			                                               ^
		Syntax error on token ";", , expected
		----------
		""";

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=133292
public void test08() {

	String s =
		"""
		public class X { 										\t
			Object o[] = { new String("SUCCESS") ; };			\t
		}														\t
		""";

	String expectedSyntaxErrorDiagnosis =
		"""
		----------
		1. ERROR in <test> (at line 2)
			Object o[] = { new String("SUCCESS") ; };			\t
			                                     ^
		Syntax error on token ";", , expected
		----------
		""";

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=133292
public void test09() {

	String s =
		"""
		public class X { 											\t
			void foo() {											\t
				java.lang.Object o[] = { new String("SUCCESS") ; };\t
			}														\t
		}															\t
		""";

	String expectedSyntaxErrorDiagnosis =
		"""
		----------
		1. ERROR in <test> (at line 3)
			java.lang.Object o[] = { new String("SUCCESS") ; };\t
			                                               ^
		Syntax error on token ";", , expected
		----------
		""";

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=133292
public void test10() {

	String s =
		"""
		public class X { 											\t
			void foo() {											\t
				Object o[] = { new String("SUCCESS") ; };			\t
			}														\t
		}															\t
		""";

	String expectedSyntaxErrorDiagnosis =
		"""
		----------
		1. ERROR in <test> (at line 3)
			Object o[] = { new String("SUCCESS") ; };			\t
			                                     ^
		Syntax error on token ";", , expected
		----------
		""";

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=80339
public void test11() {

	String s =
		"""
		package a;									\t
		public interface Test {					\t
		  public void myMethod()					\t
		}											\t
		""";

	String expectedSyntaxErrorDiagnosis =
		"""
		----------
		1. ERROR in <test> (at line 3)
			public void myMethod()					\t
			                     ^
		Syntax error, insert ";" to complete MethodDeclaration
		----------
		""";

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=80339
public void test12() {

	String s =
		"""
		package a;									\t
		public interface Test {					\t
		  public void myMethod()					\t
		    System.out.println();					\t
		}											\t
		""";

	String expectedSyntaxErrorDiagnosis =
		"""
		----------
		1. ERROR in <test> (at line 3)
			public void myMethod()					\t
			                     ^
		Syntax error on token ")", { expected after this token
		----------
		2. ERROR in <test> (at line 5)
			}											\t
			^
		Syntax error, insert "}" to complete InterfaceBody
		----------
		""";

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=221266
public void test13() {

	String s =
		"""
		package a;									\t
		public class Test {						\t
		  public void foo() {						\t
		    foo(a  "\\"");						\t
		  }										\t
		}											\t
		""";

	String expectedSyntaxErrorDiagnosis =
		"""
		----------
		1. ERROR in <test> (at line 4)
			foo(a  "\\"");						\t
			       ^^^^
		Syntax error on token ""\\\""", delete this token
		----------
		""";

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=212713
public void test14() {

	String s =
		"""
		public interface Test {
		  static {  }
		  {         }
		}
		""";

	String expectedSyntaxErrorDiagnosis =
		"""
		----------
		1. ERROR in <test> (at line 2)
			static {  }
			       ^^^^
		The interface Test cannot define an initializer
		----------
		2. ERROR in <test> (at line 3)
			{         }
			^^^^^^^^^^^
		The interface Test cannot define an initializer
		----------
		""";

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=210419
public void test15() {

	String s =
		"""
		package bug;
		public class Test {
		  static int X;
		  String field = { String str;
		      switch (X) {
		        case 0:
		          str = "zero";
		          break;
		        default:
		          str = "other";
		          break;
		      }
		      this.field = str;
		  };
		  public static void main(String[] args) {
		    System.out.println(new Test().field);
		  }
		}
		""";

	String expectedSyntaxErrorDiagnosis =
		"""
		----------
		1. ERROR in <test> (at line 4)
			String field = { String str;
			               ^^^^^^^^
		Syntax error on token(s), misplaced construct(s)
		----------
		2. ERROR in <test> (at line 4)
			String field = { String str;
			                           ^
		Syntax error on token ";", { expected after this token
		----------
		""";

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedSyntaxErrorDiagnosis,
		testName);
}
}
