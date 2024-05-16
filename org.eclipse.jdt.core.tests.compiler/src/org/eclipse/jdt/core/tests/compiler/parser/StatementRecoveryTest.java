/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
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

import junit.framework.Test;

import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

public class StatementRecoveryTest extends AbstractCompilerTest {
	public static final boolean ONLY_DIET_PLUS_BODY_WITH_STATEMENT_RECOVERY = false;

	public static boolean optimizeStringLiterals = false;
	public static long sourceLevel = ClassFileConstants.JDK1_3; //$NON-NLS-1$

static {
//	TESTS_NAMES = new String[] { "test0037"};
//	TESTS_RANGE = new int[] {10, 20};
}
public static Test suite() {
	return buildAllCompliancesTestSuite(StatementRecoveryTest.class);
}
public StatementRecoveryTest(String testName){
	super(testName);
}
public void checkParse(
	char[] source,
	String expectedDietUnitToString,
	String expectedDietWithStatementRecoveryUnitToString,
	String expectedDietPlusBodyUnitToString,
	String expectedDietPlusBodyWithStatementRecoveryUnitToString,
	String expectedFullUnitToString,
	String expectedFullWithStatementRecoveryUnitToString,
	String testName) {

	/* using regular parser in DIET mode */
	if(!ONLY_DIET_PLUS_BODY_WITH_STATEMENT_RECOVERY){
		Parser parser =
			new Parser(
				new ProblemReporter(
					DefaultErrorHandlingPolicies.proceedWithAllProblems(),
					new CompilerOptions(getCompilerOptions()),
					new DefaultProblemFactory(Locale.getDefault())),
				optimizeStringLiterals);
		parser.setMethodsFullRecovery(false);
		parser.setStatementsRecovery(false);

		ICompilationUnit sourceUnit = new CompilationUnit(source, testName, null);
		CompilationResult compilationResult = new CompilationResult(sourceUnit, 0, 0, 0);

		CompilationUnitDeclaration computedUnit = parser.dietParse(sourceUnit, compilationResult);
		String computedUnitToString = computedUnit.toString();
		if (!expectedDietUnitToString.equals(computedUnitToString)){
			System.out.println(Util.displayString(computedUnitToString));
		}
		assertEquals(
			"Invalid unit diet structure" + testName,
			expectedDietUnitToString,
			computedUnitToString);
	}
	/* using regular parser in DIET mode and statementRecoveryEnabled */
	if(!ONLY_DIET_PLUS_BODY_WITH_STATEMENT_RECOVERY){
		Parser parser =
			new Parser(
				new ProblemReporter(
					DefaultErrorHandlingPolicies.proceedWithAllProblems(),
					new CompilerOptions(getCompilerOptions()),
					new DefaultProblemFactory(Locale.getDefault())),
				optimizeStringLiterals);

		ICompilationUnit sourceUnit = new CompilationUnit(source, testName, null);
		CompilationResult compilationResult = new CompilationResult(sourceUnit, 0, 0, 0);

		CompilationUnitDeclaration computedUnit = parser.dietParse(sourceUnit, compilationResult);
		String computedUnitToString = computedUnit.toString();
		if (!expectedDietWithStatementRecoveryUnitToString.equals(computedUnitToString)){
			System.out.println(Util.displayString(computedUnitToString));
		}
		assertEquals(
			"Invalid unit diet structure with statement recovery enabled" + testName,
			expectedDietWithStatementRecoveryUnitToString,
			computedUnitToString);
	}
	/* using regular parser in DIET mode + getMethodBodies */
	if(!ONLY_DIET_PLUS_BODY_WITH_STATEMENT_RECOVERY){
		Parser parser =
			new Parser(
				new ProblemReporter(
					DefaultErrorHandlingPolicies.proceedWithAllProblems(),
					new CompilerOptions(getCompilerOptions()),
					new DefaultProblemFactory(Locale.getDefault())),
				optimizeStringLiterals);
		parser.setMethodsFullRecovery(false);
		parser.setStatementsRecovery(false);

		ICompilationUnit sourceUnit = new CompilationUnit(source, testName, null);
		CompilationResult compilationResult = new CompilationResult(sourceUnit, 0, 0, 0);

		CompilationUnitDeclaration computedUnit = parser.dietParse(sourceUnit, compilationResult);
		String computedUnitToString = computedUnit.toString();
		if (!expectedDietUnitToString.equals(computedUnitToString)){
			System.out.println(Util.displayString(computedUnitToString));
		}
		assertEquals(
			"Invalid unit diet structure" + testName,
			expectedDietUnitToString,
			computedUnitToString);
		if (computedUnit.types != null) {
			for (int i = 0, length = computedUnit.types.length; i < length; i++){
				computedUnit.types[i].parseMethods(parser, computedUnit);
			}
		}
		computedUnitToString = computedUnit.toString();
		if (!expectedDietPlusBodyUnitToString.equals(computedUnitToString)){
			System.out.println(Util.displayString(computedUnitToString));
		}

		assertEquals(
			"Invalid unit diet+body structure" + testName,
			expectedDietPlusBodyUnitToString,
			computedUnitToString);
	}
	/* using regular parser in DIET mode + getMethodBodies and statementRecoveryEnabled */
	{
		Parser parser =
			new Parser(
				new ProblemReporter(
					DefaultErrorHandlingPolicies.proceedWithAllProblems(),
					new CompilerOptions(getCompilerOptions()),
					new DefaultProblemFactory(Locale.getDefault())),
				optimizeStringLiterals);

		ICompilationUnit sourceUnit = new CompilationUnit(source, testName, null);
		CompilationResult compilationResult = new CompilationResult(sourceUnit, 0, 0, 0);

		CompilationUnitDeclaration computedUnit = parser.dietParse(sourceUnit, compilationResult);
		String computedUnitToString = computedUnit.toString();
		if (!expectedDietWithStatementRecoveryUnitToString.equals(computedUnitToString)){
			System.out.println(Util.displayString(computedUnitToString));
		}
		assertEquals(
			"Invalid unit diet structure" + testName,
			expectedDietWithStatementRecoveryUnitToString,
			computedUnitToString);
		if (computedUnit.types != null) {
			for (int i = 0, length = computedUnit.types.length; i < length; i++){
				computedUnit.types[i].parseMethods(parser, computedUnit);
			}
		}
		computedUnitToString = computedUnit.toString();
		if (!expectedDietPlusBodyWithStatementRecoveryUnitToString.equals(computedUnitToString)){
			System.out.println(Util.displayString(computedUnitToString));
		}

		assertEquals(
			"Invalid unit diet+body structure with statement recovery enabled" + testName,
			expectedDietPlusBodyWithStatementRecoveryUnitToString,
			computedUnitToString);
	}
	/* using regular parser in FULL mode */
	if(!ONLY_DIET_PLUS_BODY_WITH_STATEMENT_RECOVERY){
		Parser parser =
			new Parser(
				new ProblemReporter(
					DefaultErrorHandlingPolicies.proceedWithAllProblems(),
					new CompilerOptions(getCompilerOptions()),
					new DefaultProblemFactory(Locale.getDefault())),
				optimizeStringLiterals);
		parser.setMethodsFullRecovery(false);
		parser.setStatementsRecovery(false);

		ICompilationUnit sourceUnit = new CompilationUnit(source, testName, null);
		CompilationResult compilationResult = new CompilationResult(sourceUnit, 0, 0, 0);

		CompilationUnitDeclaration computedUnit = parser.parse(sourceUnit, compilationResult);
		String computedUnitToString = computedUnit.toString();
		if (!expectedFullUnitToString.equals(computedUnitToString)){
			System.out.println(Util.displayString(computedUnitToString));
		}
		assertEquals(
			"Invalid unit full structure" + testName,
			expectedFullUnitToString,
			computedUnitToString);

	}
	/* using regular parser in FULL mode and statementRecoveryEnabled */
	if(!ONLY_DIET_PLUS_BODY_WITH_STATEMENT_RECOVERY){
		Parser parser =
			new Parser(
				new ProblemReporter(
					DefaultErrorHandlingPolicies.proceedWithAllProblems(),
					new CompilerOptions(getCompilerOptions()),
					new DefaultProblemFactory(Locale.getDefault())),
				optimizeStringLiterals);

		ICompilationUnit sourceUnit = new CompilationUnit(source, testName, null);
		CompilationResult compilationResult = new CompilationResult(sourceUnit, 0, 0, 0);

		CompilationUnitDeclaration computedUnit = parser.parse(sourceUnit, compilationResult);
		String computedUnitToString = computedUnit.toString();
		if (!expectedFullWithStatementRecoveryUnitToString.equals(computedUnitToString)){
			System.out.println(Util.displayString(computedUnitToString));
		}
		assertEquals(
			"Invalid unit full structure with statement recovery enabled" + testName,
			expectedFullWithStatementRecoveryUnitToString,
			computedUnitToString);

	}
}

public void test0001() {

	String s =
		"""
		package a;										\t
		public class X {							\t
		  void foo() {							\t
		    System.out.println();				\t
		  }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		expectedDietPlusBodyUnitToString;

	String expectedFullUnitToString =
		expectedDietPlusBodyUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedFullUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0002() {

	String s =
		"""
		package a;										\t
		public class X {							\t
		  void foo() {							\t
		    #                    				\t
		    System.out.println();				\t
		  }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0003() {

	String s =
		"""
		package a;										\t
		public class X {							\t
		  void foo() {							\t
		    System.out.println();				\t
		    #                    				\t
		  }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0004() {

	String s =
		"""
		package a;										\t
		public class X {							\t
		  void foo() {							\t
		    #                    				\t
		    System.out.println();				\t
		    System.out.println();				\t
		  }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		    System.out.println();
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0005() {

	String s =
		"""
		package a;										\t
		public class X {							\t
		  void foo() {							\t
		    System.out.println();				\t
		    System.out.println();				\t
		    #                    				\t
		  }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		    System.out.println();
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0006() {

	String s =
		"""
		package a;										\t
		public class X {							\t
		  void foo() {							\t
		    System.out.println();				\t
		    System.out.println();				\t
		    #                    				\t
		    System.out.println();				\t
		    System.out.println();				\t
		  }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		    System.out.println();
		    System.out.println();
		    System.out.println();
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0007() {

	String s =
		"""
		package a;										\t
		public class X {							\t
		  void foo() {							\t
		    #                    				\t
		    System.out.println();				\t
		    if(true) {							\t
		      System.out.println();				\t
		    }									\t
		    System.out.println();				\t
		  }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		    if (true)
		        {
		          System.out.println();
		        }
		    System.out.println();
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0008() {

	String s =
		"""
		package a;										\t
		public class X {							\t
		  void foo() {							\t
		    System.out.println();				\t
		    if(true) {							\t
		      System.out.println();				\t
		    }									\t
		    System.out.println();				\t
		    #                    				\t
		  }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		    if (true)
		        {
		          System.out.println();
		        }
		    System.out.println();
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0009() {

	String s =
		"""
		package a;										\t
		public class X {							\t
		  void foo() {							\t
		    System.out.println();				\t
		    if(true) {							\t
		      System.out.println();				\t
		    }									\t
		    System.out.println();				\t
		    #                    				\t
		    System.out.println();				\t
		    if(true) {							\t
		      System.out.println();				\t
		    }									\t
		    System.out.println();				\t
		  }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		    if (true)
		        {
		          System.out.println();
		        }
		    System.out.println();
		    System.out.println();
		    if (true)
		        {
		          System.out.println();
		        }
		    System.out.println();
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0010() {

	String s =
		"""
		package a;										\t
		public class X {							\t
		  void foo() {							\t
		    System.out.println();				\t
		    class Z {							\t
		      void foo() {}						\t
		    }									\t
		    System.out.println();				\t
		  }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		    class Z {
		      Z() {
		        super();
		      }
		      void foo() {
		      }
		    }
		    System.out.println();
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		    class Z {
		      Z() {
		        super();
		      }
		      void foo() {
		      }
		    }
		    System.out.println();
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietPlusBodyUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietPlusBodyUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0011() {

	String s =
		"""
		package a;										\t
		public class X {							\t
		  void foo() {							\t
		    System.out.println();				\t
		    class Z {							\t
		      void foo() {}						\t
		    }									\t
		    System.out.println();				\t
		    #									\t
		  }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		    class Z {
		      Z() {
		        super();
		      }
		      void foo() {
		      }
		    }
		    System.out.println();
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0012() {

	String s =
		"""
		package a;										\t
		public class X {							\t
		  void foo() {							\t
		    #									\t
		    System.out.println();				\t
		    class Z {							\t
		      void foo() {}						\t
		    }									\t
		    System.out.println();				\t
		  }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		    class Z {
		      Z() {
		        super();
		      }
		      void foo() {
		      }
		    }
		    System.out.println();
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0013() {

	String s =
		"""
		package a;										\t
		public class X {							\t
		  void foo() {							\t
		    System.out.println();				\t
		    class Z {							\t
		      void foo() {}						\t
		    }									\t
		    System.out.println();				\t
		    #									\t
		    System.out.println();				\t
		    class Y {							\t
		      void foo() {}						\t
		    }									\t
		    System.out.println();				\t
		  }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		    class Z {
		      Z() {
		        super();
		      }
		      void foo() {
		      }
		    }
		    System.out.println();
		    System.out.println();
		    class Y {
		      Y() {
		        super();
		      }
		      void foo() {
		      }
		    }
		    System.out.println();
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0014() {

	String s =
		"""
		package a;										\t
		public class X {							\t
		  void foo() {							\t
		    System.out.println();				\t
		    class Z {							\t
		      void foo() {						\t
		        System.out.println();			\t
		      }									\t
		    }									\t
		    System.out.println();				\t
		    #									\t
		  }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		    class Z {
		      Z() {
		        super();
		      }
		      void foo() {
		        System.out.println();
		      }
		    }
		    System.out.println();
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0015() {

	String s =
		"""
		package a;										\t
		public class X {							\t
		  void foo() {							\t
		    #									\t
		    System.out.println();				\t
		    class Z {							\t
		      void foo() {						\t
		        System.out.println();			\t
		      }									\t
		    }									\t
		    System.out.println();				\t
		  }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		    class Z {
		      Z() {
		        super();
		      }
		      void foo() {
		        System.out.println();
		      }
		    }
		    System.out.println();
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0016() {

	String s =
		"""
		package a;										\t
		public class X {							\t
		  void foo() {							\t
		    System.out.println();				\t
		    class Z {							\t
		      void foo() {						\t
		        System.out.println();			\t
		      }									\t
		    }									\t
		    System.out.println();				\t
		    #									\t
		    System.out.println();				\t
		    class Z {							\t
		      void foo() {						\t
		        System.out.println();			\t
		      }									\t
		    }									\t
		    System.out.println();				\t
		  }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		    class Z {
		      Z() {
		        super();
		      }
		      void foo() {
		        System.out.println();
		      }
		    }
		    System.out.println();
		    System.out.println();
		    class Z {
		      Z() {
		        super();
		      }
		      void foo() {
		        System.out.println();
		      }
		    }
		    System.out.println();
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0017() {

	String s =
		"""
		package a;										\t
		public class X {							\t
		  void foo() {							\t
		    System.out.println();				\t
		    class Z {							\t
		      void foo() {						\t
		        System.out.println();			\t
		        if(true) {						\t
		          System.out.println();			\t
		        }								\t
		        System.out.println();			\t
		        #								\t
		      }									\t
		    }									\t
		    System.out.println();				\t
		  }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		    class Z {
		      Z() {
		        super();
		      }
		      void foo() {
		        System.out.println();
		        if (true)
		            {
		              System.out.println();
		            }
		        System.out.println();
		      }
		    }
		    System.out.println();
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0018() {

	String s =
		"""
		package a;										\t
		public class X {							\t
		  void foo() {							\t
		    System.out.println();				\t
		    class Z {							\t
		      void foo() {						\t
		        #								\t
		        System.out.println();			\t
		        if(true) {						\t
		          System.out.println();			\t
		        }								\t
		        System.out.println();			\t
		      }									\t
		    }									\t
		    System.out.println();				\t
		  }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		    class Z {
		      Z() {
		        super();
		      }
		      void foo() {
		        System.out.println();
		        if (true)
		            {
		              System.out.println();
		            }
		        System.out.println();
		      }
		    }
		    System.out.println();
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0019() {

	String s =
		"""
		package a;										\t
		public class X {							\t
		  void foo() {							\t
		    System.out.println();				\t
		    class Z {							\t
		      void foo() {						\t
		        System.out.println();			\t
		        if(true) {						\t
		          System.out.println();			\t
		        }								\t
		        System.out.println();			\t
		        #								\t
		        System.out.println();			\t
		        if(true) {						\t
		          System.out.println();			\t
		        }								\t
		        System.out.println();			\t
		      }									\t
		    }									\t
		    System.out.println();				\t
		  }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		    class Z {
		      Z() {
		        super();
		      }
		      void foo() {
		        System.out.println();
		        if (true)
		            {
		              System.out.println();
		            }
		        System.out.println();
		        System.out.println();
		        if (true)
		            {
		              System.out.println();
		            }
		        System.out.println();
		      }
		    }
		    System.out.println();
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0020() {

	String s =
		"""
		package a;										\t
		public class X {							\t
		  void foo() {							\t
		    #									\t
		    System.out.println();				\t
		    class Z {							\t
		      void foo() {						\t
		        System.out.println();			\t
		        if(true) {						\t
		          System.out.println();			\t
		        }								\t
		        System.out.println();			\t
		        #								\t
		        System.out.println();			\t
		        if(true) {						\t
		          System.out.println();			\t
		        }								\t
		        System.out.println();			\t
		      }									\t
		    }									\t
		    System.out.println();				\t
		  }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		    class Z {
		      Z() {
		        super();
		      }
		      void foo() {
		        System.out.println();
		        if (true)
		            {
		              System.out.println();
		            }
		        System.out.println();
		        System.out.println();
		        if (true)
		            {
		              System.out.println();
		            }
		        System.out.println();
		      }
		    }
		    System.out.println();
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0021() {

	String s =
		"""
		package a;										\t
		public class X {							\t
		  void foo() {							\t
		    System.out.println();				\t
		    new Object() {						\t
		      void foo() {}						\t
		    };									\t
		    System.out.println();				\t
		  }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		    new Object() {
		      void foo() {
		      }
		    };
		    System.out.println();
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		    new Object() {
		      void foo() {
		      }
		    };
		    System.out.println();
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietPlusBodyUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietPlusBodyUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0022() {

	String s =
		"""
		package a;										\t
		public class X {							\t
		  void foo() {							\t
		    System.out.println();				\t
		    new Object() {						\t
		      void foo() {}						\t
		    };									\t
		    System.out.println();				\t
		    #									\t
		  }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		    new Object() {
		      void foo() {
		      }
		    };
		    System.out.println();
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0023() {

	String s =
		"""
		package a;										\t
		public class X {							\t
		  void foo() {							\t
		    #									\t
		    System.out.println();				\t
		    new Object() {						\t
		      void bar() {}						\t
		    };									\t
		    System.out.println();				\t
		  }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		    new Object() {
		      void bar() {
		      }
		    };
		    System.out.println();
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0024() {

	String s =
		"""
		package a;										\t
		public class X {							\t
		  void foo() {							\t
		    System.out.println();				\t
		    new Object() {						\t
		      void bar() {}						\t
		    };									\t
		    System.out.println();				\t
		    #									\t
		    System.out.println();				\t
		    new Object() {						\t
		      void bar() {}						\t
		    };									\t
		    System.out.println();				\t
		  }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		    new Object() {
		      void bar() {
		      }
		    };
		    System.out.println();
		    System.out.println();
		    new Object() {
		      void bar() {
		      }
		    };
		    System.out.println();
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0025() {

	String s =
		"""
		package a;										\t
		public class X {							\t
		  void foo() {							\t
		    System.out.println();				\t
		    new Object() {						\t
		      void foo() {						\t
		        System.out.println();			\t
		      }									\t
		    };									\t
		    System.out.println();				\t
		    #									\t
		  }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		    new Object() {
		      void foo() {
		        System.out.println();
		      }
		    };
		    System.out.println();
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0026() {

	String s =
		"""
		package a;										\t
		public class X {							\t
		  void foo() {							\t
		    #									\t
		    System.out.println();				\t
		    new Object() {						\t
		      void foo() {						\t
		        System.out.println();			\t
		      }									\t
		    };									\t
		    System.out.println();				\t
		  }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		    new Object() {
		      void foo() {
		        System.out.println();
		      }
		    };
		    System.out.println();
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0027() {

	String s =
		"""
		package a;										\t
		public class X {							\t
		  void foo() {							\t
		    System.out.println();				\t
		    new Object() {						\t
		      void foo() {						\t
		        System.out.println();			\t
		      }									\t
		    };									\t
		    System.out.println();				\t
		    #									\t
		    System.out.println();				\t
		    new Object() {						\t
		      void foo() {						\t
		        System.out.println();			\t
		      }									\t
		    };									\t
		    System.out.println();				\t
		  }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		    new Object() {
		      void foo() {
		        System.out.println();
		      }
		    };
		    System.out.println();
		    System.out.println();
		    new Object() {
		      void foo() {
		        System.out.println();
		      }
		    };
		    System.out.println();
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0028() {

	String s =
		"""
		package a;										\t
		public class X {							\t
		  void foo() {							\t
		    System.out.println();				\t
		    new Object() {						\t
		      void foo() {						\t
		        System.out.println();			\t
		        if(true) {						\t
		          System.out.println();			\t
		        }								\t
		        System.out.println();			\t
		        #								\t
		      }									\t
		    };									\t
		    System.out.println();				\t
		  }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		    new Object() {
		      void foo() {
		        System.out.println();
		        if (true)
		            {
		              System.out.println();
		            }
		        System.out.println();
		      }
		    };
		    System.out.println();
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0029() {

	String s =
		"""
		package a;										\t
		public class X {							\t
		  void foo() {							\t
		    System.out.println();				\t
		    new Object() {						\t
		      void foo() {						\t
		        #								\t
		        System.out.println();			\t
		        if(true) {						\t
		          System.out.println();			\t
		        }								\t
		        System.out.println();			\t
		      }									\t
		    };									\t
		    System.out.println();				\t
		  }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		    new Object() {
		      void foo() {
		        System.out.println();
		        if (true)
		            {
		              System.out.println();
		            }
		        System.out.println();
		      }
		    };
		    System.out.println();
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0030() {

	String s =
		"""
		package a;										\t
		public class X {							\t
		  void foo() {							\t
		    System.out.println();				\t
		    new Object() {						\t
		      void foo() {						\t
		        System.out.println();			\t
		        if(true) {						\t
		          System.out.println();			\t
		        }								\t
		        System.out.println();			\t
		        #								\t
		        System.out.println();			\t
		        if(true) {						\t
		          System.out.println();			\t
		        }								\t
		        System.out.println();			\t
		      }									\t
		    };									\t
		    System.out.println();				\t
		  }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		    new Object() {
		      void foo() {
		        System.out.println();
		        if (true)
		            {
		              System.out.println();
		            }
		        System.out.println();
		        System.out.println();
		        if (true)
		            {
		              System.out.println();
		            }
		        System.out.println();
		      }
		    };
		    System.out.println();
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0031() {

	String s =
		"""
		package a;										\t
		public class X {							\t
		  void foo() {							\t
		    #									\t
		    System.out.println();				\t
		    new Object() {						\t
		      void foo() {						\t
		        System.out.println();			\t
		        if(true) {						\t
		          System.out.println();			\t
		        }								\t
		        System.out.println();			\t
		        #								\t
		        System.out.println();			\t
		        if(true) {						\t
		          System.out.println();			\t
		        }								\t
		        System.out.println();			\t
		      }									\t
		    };									\t
		    System.out.println();				\t
		  }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		    new Object() {
		      void foo() {
		        System.out.println();
		        if (true)
		            {
		              System.out.println();
		            }
		        System.out.println();
		        System.out.println();
		        if (true)
		            {
		              System.out.println();
		            }
		        System.out.println();
		      }
		    };
		    System.out.println();
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0032() {

	String s =
		"""
		package a;										\t
		public class X {							\t
		  void foo() {							\t
		    #									\t
		    System.out.println();				\t
		    bar(new Object() {					\t
		      void foo() {						\t
		        System.out.println();			\t
		        if(true) {						\t
		          System.out.println();			\t
		        }								\t
		        System.out.println();			\t
		        #								\t
		        System.out.println();			\t
		        if(true) {						\t
		          System.out.println();			\t
		        }								\t
		        System.out.println();			\t
		      }									\t
		    });									\t
		    System.out.println();				\t
		  }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		    System.out.println();
		    bar(new Object() {
		  void foo() {
		    System.out.println();
		    if (true)
		        {
		          System.out.println();
		        }
		    System.out.println();
		    System.out.println();
		    if (true)
		        {
		          System.out.println();
		        }
		    System.out.println();
		  }
		});
		    System.out.println();
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0033() {

	String s =
		"""
		package a;										\t
		public class X {							\t
		  void foo() {							\t
		    #									\t
		    class Z {							\t
		      void foo() {						\t
		        System.out.println();			\t
		      }									\t
		    }									\t
		  }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		    class Z {
		      Z() {
		        super();
		      }
		      void foo() {
		        System.out.println();
		      }
		    }
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0034() {

	String s =
		"""
		package a;										\t
		public class X {							\t
		  void foo() {							\t
		    #									\t
		    new Object() {						\t
		      void foo() {						\t
		        System.out.println();			\t
		      }									\t
		    };									\t
		  }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		    new Object() {
		      void foo() {
		        System.out.println();
		      }
		    };
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0035() {

	String s =
		"""
		package a;										\t
		public class X {							\t
		  void foo() {							\t
		    bar(\\u0029							\t
		  }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		    bar();
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0036() {

	String s =
		"""
		package a;										\t
		public class X {							\t
		  void foo() {							\t
		    if(true) {							\t
		      foo();								\t
		    }									\t
		    for(;								\t
		    if(true) {							\t
		      foo();								\t
		    }									\t
		  }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		    if (true)
		        {
		          foo();
		        }
		    for (; ; )\s
		      ;
		    if (true)
		        {
		          foo();
		        }
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0037() {

	String s =
		"""
		package a;										\t
		public class X {							\t
		  void foo() {							\t
		    if() {								\t
		      foo();								\t
		    }									\t
		  }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		    if ($missing$)
		        {
		          foo();
		        }
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0038() {
	String s =
		"""
		package p1;									\t
		public class A {								\t
			public interface B {						\t
				public abstract void aMethod (int A);	\t
				public interface C {					\t
					public abstract void anotherMethod(int A);
				}										\t
			}											\t
			public class aClass implements B, B.C {		\t
				public void aMethod (int A) {			\t
					public void anotherMethod(int A) {};\t
				}										\t
			}											\t
		   	public static void main (String argv[]) {\t
				System.out.println("SUCCESS");		\t
			}											\t
		}""";

	String expectedDietUnitToString =
		"""
		package p1;
		public class A {
		  public interface B {
		    public interface C {
		      public abstract void anotherMethod(int A);
		    }
		    public abstract void aMethod(int A);
		  }
		  public class aClass implements B, B.C {
		    public aClass() {
		    }
		    public void aMethod(int A) {
		    }
		  }
		  public A() {
		  }
		  public static void main(String[] argv) {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		package p1;
		public class A {
		  public interface B {
		    public interface C {
		      public abstract void anotherMethod(int A);
		    }
		    public abstract void aMethod(int A);
		  }
		  public class aClass implements B, B.C {
		    public aClass() {
		      super();
		    }
		    public void aMethod(int A) {
		    }
		  }
		  public A() {
		    super();
		  }
		  public static void main(String[] argv) {
		    System.out.println("SUCCESS");
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"package p1;\n" +
		"public class A {\n" +
		"  public interface B {\n" +
		"    public interface C {\n" +
		"      public abstract void anotherMethod(int A);\n" +
		"    }\n" +
		"    public abstract void aMethod(int A);\n" +
		"  }\n" +
		"  public class aClass implements B, B.C {\n" +
		"    public aClass() {\n" +
		"      super();\n" +
		"    }\n" +
		"    public void aMethod(int A) {\n" +
		"      public void anotherMethod;\n" +
		(this.complianceLevel < ClassFileConstants.JDK14
		?
		"      int A;\n" +
		"      ;\n"
		:
		""
		) +
		"    }\n" +
		"  }\n" +
		"  public A() {\n" +
		"    super();\n" +
		"  }\n" +
		"  public static void main(String[] argv) {\n" +
		"    System.out.println(\"SUCCESS\");\n" +
		"  }\n" +
		"}\n";

	String expectedFullUnitToString =
		"""
		package p1;
		public class A {
		  public interface B {
		    public interface C {
		      public abstract void anotherMethod(int A);
		    }
		    public abstract void aMethod(int A);
		  }
		  public class aClass implements B, B.C {
		    public aClass() {
		    }
		    public void aMethod(int A) {
		    }
		    public void anotherMethod(int A) {
		    }
		  }
		  {
		  }
		  public A() {
		  }
		  public static void main(String[] argv) {
		  }
		}
		""";

	String expectedFullWithStatementRecoveryUnitToString =
		expectedFullUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0039() {

	String s =
		"""
		package a;										\t
		public class X {							\t
		  {										\t
		    System.out.println();				\t
		    foo()								\t
		    System.out.println();				\t
		  }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  {
		  }
		  public X() {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X {
		  {
		  }
		  public X() {
		    super();
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		package a;
		public class X {
		  {
		    System.out.println();
		    foo();
		    System.out.println();
		  }
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0040() {

	String s =
		"""
		package a;										\t
		public class X {							\t
		  {										\t
		    System.out.println();				\t
		    class Y {							\t
		      {									\t
		        System.out.println();			\t
		        foo()							\t
		        System.out.println();			\t
		      }									\t
		    }									\t
		    System.out.println();				\t
		  }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  {
		  }
		  public X() {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X {
		  {
		  }
		  public X() {
		    super();
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		package a;
		public class X {
		  {
		    System.out.println();
		    class Y {
		      {
		        System.out.println();
		        foo();
		        System.out.println();
		      }
		      Y() {
		        super();
		      }
		    }
		    System.out.println();
		  }
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0041() {

	String s =
		"""
		package a;										\t
		public class X {							\t
		  {										\t
		    System.out.println();				\t
		    class Y {							\t
		      {									\t
		        System.out.println();			\t
		        foo()							\t
		        System.out.println();			\t
		      }									\t
		    }									\t
		    System.out.println();				\t
		    class Z {							\t
		      {									\t
		        System.out.println();			\t
		        foo()							\t
		        System.out.println();			\t
		      }									\t
		    }									\t
		    System.out.println();				\t
		    foo()								\t
		    System.out.println();				\t
		  }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  {
		  }
		  public X() {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X {
		  {
		  }
		  public X() {
		    super();
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		package a;
		public class X {
		  {
		    System.out.println();
		    class Y {
		      {
		        System.out.println();
		        foo();
		        System.out.println();
		      }
		      Y() {
		        super();
		      }
		    }
		    System.out.println();
		    class Z {
		      {
		        System.out.println();
		        foo();
		        System.out.println();
		      }
		      Z() {
		        super();
		      }
		    }
		    System.out.println();
		    foo();
		    System.out.println();
		  }
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void test0042() {

	String s =
		"""
		package a;										\t
		public class X {							\t
		  void foo() {							\t
		    for(int i							\t
		  }										\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		    for (int i;; ; )\s
		      ;
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=80339
public void test0043() {

	String s =
		"""
		package a;										\t
		public interface Test {					\t
		  public void myMethod()					\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public interface Test {
		  public void myMethod() {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public interface Test {
		  public void myMethod() {
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		package a;
		public interface Test {
		  public void myMethod() {
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=173992
public void test0044() {

	String s =
		"""
		import java.io.EOFException;
		import java.io.FileNotFoundException;
		import java.io.IOException;
		import org.xml.sax.SAXException;
		public class X {
		public void doSomething() throws FileNotFoundException, EOFException, SAXException{
		
		}
		public void doSomethingElse() {
		try {
			doSomething();
		}
		 catch ( SAXException exception) {
		
		} \s
		catch ( FileNotFoundException exception ) {
		
		}   \s
		catch (
			// working before the slashes
		) {
		
		}\s
		}\s
		}
		""";

	String expectedDietUnitToString =
		"""
		import java.io.EOFException;
		import java.io.FileNotFoundException;
		import java.io.IOException;
		import org.xml.sax.SAXException;
		public class X {
		  public X() {
		  }
		  public void doSomething() throws FileNotFoundException, EOFException, SAXException {
		  }
		  public void doSomethingElse() {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		import java.io.EOFException;
		import java.io.FileNotFoundException;
		import java.io.IOException;
		import org.xml.sax.SAXException;
		public class X {
		  public X() {
		    super();
		  }
		  public void doSomething() throws FileNotFoundException, EOFException, SAXException {
		  }
		  public void doSomethingElse() {
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		import java.io.EOFException;
		import java.io.FileNotFoundException;
		import java.io.IOException;
		import org.xml.sax.SAXException;
		public class X {
		  public X() {
		    super();
		  }
		  public void doSomething() throws FileNotFoundException, EOFException, SAXException {
		  }
		  public void doSomethingElse() {
		    try
		      {
		        doSomething();
		      }
		    catch (SAXException exception)
		      {
		      }
		    catch (FileNotFoundException exception)
		      {
		      }
		    catch ($missing$ $missing$)
		      {
		      }
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=204662
public void test0045() {

	String s =
		"""
		public class BadClass {
		
		   public void method(Object obj) {
		
			  /*//this version compiles
			  People oPeople;
			  {oPeople= (People) obj;}//*/
		
			  /*//this version fails, but the compiler errors are fine
		      class People oPeople;
			  oPeople= (class People) obj;//*/
		
			  //this version fails with internal compiler error
			  class People oPeople;
			  {oPeople= (class People) obj;}
		   }
		
		}
		""";

	String expectedDietUnitToString =
		"""
		public class BadClass {
		  public BadClass() {
		  }
		  public void method(Object obj) {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		public class BadClass {
		  public BadClass() {
		    super();
		  }
		  public void method(Object obj) {
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		public class BadClass {
		  public BadClass() {
		    super();
		  }
		  public void method(Object obj) {
		    class People {
		      {
		        class People {
		          People() {
		            super();
		          }
		        }
		      }
		      People() {
		        super();
		      }
		    }
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=204662
public void test0046() {

	String s =
		"""
		public class X {
			public void foo() {\s
				class Y ;
			\t
					{
						class Z ;
						}
			}
		}
		""";

	String expectedDietUnitToString =
		"""
		public class X {
		  public X() {
		  }
		  public void foo() {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  public X() {
		    super();
		  }
		  public void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		public class X {
		  public X() {
		    super();
		  }
		  public void foo() {
		    class Y {
		      {
		        class Z {
		          Z() {
		            super();
		          }
		        }
		      }
		      Y() {
		        super();
		      }
		    }
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=204662
public void test0047() {

	String s =
		"""
		public class X {
			public void foo() {\s
				class Y ;
			\t
					void bar() {
						class Z ;
						}
			}
		}
		""";

	String expectedDietUnitToString =
		"""
		public class X {
		  public X() {
		  }
		  public void foo() {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		public class X {
		  public X() {
		    super();
		  }
		  public void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		public class X {
		  public X() {
		    super();
		  }
		  public void foo() {
		    class Y {
		      Y() {
		        super();
		      }
		      void bar() {
		        class Z {
		          Z() {
		            super();
		          }
		        }
		      }
		    }
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
public void testBug430336() {

	String s =
		"""
		package test1;
		import java.util.Collection;
		public class E {
		    void foo(Collection collection) {
		        collection
		    }
		}
		""";

	String expectedDietUnitToString =
		"""
		package test1;
		import java.util.Collection;
		public class E {
		  public E() {
		  }
		  void foo(Collection collection) {
		  }
		}
		""";

	String expectedDietWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String expectedDietPlusBodyUnitToString =
		"""
		package test1;
		import java.util.Collection;
		public class E {
		  public E() {
		    super();
		  }
		  void foo(Collection collection) {
		  }
		}
		""";

	String expectedDietPlusBodyWithStatementRecoveryUnitToString =
		"""
		package test1;
		import java.util.Collection;
		public class E {
		  public E() {
		    super();
		  }
		  void foo(Collection collection) {
		    collection = $missing$;
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedFullWithStatementRecoveryUnitToString =
		expectedDietUnitToString;

	String testName = "<test>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietWithStatementRecoveryUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyWithStatementRecoveryUnitToString,
		expectedFullUnitToString,
		expectedFullWithStatementRecoveryUnitToString,
		testName);
}
}
