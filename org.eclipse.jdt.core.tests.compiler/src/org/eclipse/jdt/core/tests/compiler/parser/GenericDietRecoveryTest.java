/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *		IBM Corporation - initial API and implementation
 *		Stephan Herrmann - Contribution for
 *								bug 401035 - [1.8] A few tests have started failing recently
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.parser;

import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.codeassist.complete.CompletionParser;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.SourceElementParser;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class GenericDietRecoveryTest extends AbstractCompilerTest {
	public static boolean optimizeStringLiterals = false;
	public static long sourceLevel = ClassFileConstants.JDK1_3; //$NON-NLS-1$

public GenericDietRecoveryTest(String testName){
	super(testName);
}
static {
//	TESTS_NAMES = new String[] { "test0025" };
}
/*
 * Toggle compiler in mode -1.5
 */
@Override
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);
	return options;
}

public void checkParse(
	char[] source,
	String expectedDietUnitToString,
	String expectedDietPlusBodyUnitToString,
	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
	String expectedFullUnitToString,
	String expectedCompletionDietUnitToString, String testName) {

	/* using regular parser in DIET mode */
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
		if (!expectedDietUnitToString.equals(computedUnitToString)){
			System.out.println(Util.displayString(computedUnitToString));
		}
		assertEquals(
			"Invalid unit diet structure" + testName,
			expectedDietUnitToString,
			computedUnitToString);
	}
	/* using regular parser in DIET mode + getMethodBodies */
	{
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

	/* using regular parser in DIET mode + getMethodBodies + statements recovery*/
	{
		Parser parser =
			new Parser(
				new ProblemReporter(
					DefaultErrorHandlingPolicies.proceedWithAllProblems(),
					new CompilerOptions(getCompilerOptions()),
					new DefaultProblemFactory(Locale.getDefault())),
				optimizeStringLiterals);
		parser.setMethodsFullRecovery(true);
		parser.setStatementsRecovery(true);

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
		if (!expectedDietPlusBodyPlusStatementsRecoveryUnitToString.equals(computedUnitToString)){
			System.out.println(Util.displayString(computedUnitToString));
		}

		assertEquals(
			"Invalid unit diet+body structure" + testName,
			expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
			computedUnitToString);
	}

	/* using regular parser in FULL mode */
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
	/* using source element parser in DIET mode */
	{
		SourceElementParser parser =
			new SourceElementParser(
				new TestSourceElementRequestor(),
				new DefaultProblemFactory(Locale.getDefault()),
				new CompilerOptions(getCompilerOptions()),
				false/*don't record local declarations*/,
				true/*optimize string literals*/);

		ICompilationUnit sourceUnit = new CompilationUnit(source, testName, null);
		CompilationResult compilationResult = new CompilationResult(sourceUnit, 0, 0, 0);

		CompilationUnitDeclaration computedUnit = parser.dietParse(sourceUnit, compilationResult);
		String computedUnitToString = computedUnit.toString();
		if (!expectedDietUnitToString.equals(computedUnitToString)){
			System.out.println(Util.displayString(computedUnitToString));
		}
		assertEquals(
			"Invalid source element diet structure" + testName,
			expectedDietUnitToString,
			computedUnitToString);
	}
	/* using source element parser in FULL mode */
	{
		SourceElementParser parser =
			new SourceElementParser(
				new TestSourceElementRequestor(),
				new DefaultProblemFactory(Locale.getDefault()),
				new CompilerOptions(getCompilerOptions()),
				false/*don't record local declarations*/,
				true/*optimize string literals*/);

		ICompilationUnit sourceUnit = new CompilationUnit(source, testName, null);
		CompilationResult compilationResult = new CompilationResult(sourceUnit, 0, 0, 0);

		CompilationUnitDeclaration computedUnit = parser.parse(sourceUnit, compilationResult);
		String computedUnitToString = computedUnit.toString();
		if (!expectedFullUnitToString.equals(computedUnitToString)){
			System.out.println(Util.displayString(computedUnitToString));
		}
		assertEquals(
			"Invalid source element full structure" + testName,
			expectedFullUnitToString,
			computedUnitToString);
	}
	/* using completion parser in DIET mode */
	{
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		CompletionParser parser =
			new CompletionParser(
				new ProblemReporter(
					DefaultErrorHandlingPolicies.proceedWithAllProblems(),
					options,
					new DefaultProblemFactory(Locale.getDefault())),
				false);

		ICompilationUnit sourceUnit = new CompilationUnit(source, testName, null);
		CompilationResult compilationResult = new CompilationResult(sourceUnit, 0, 0, 0);

		CompilationUnitDeclaration computedUnit = parser.dietParse(sourceUnit, compilationResult, Integer.MAX_VALUE);
		String computedUnitToString = computedUnit.toString();
		if (!expectedCompletionDietUnitToString.equals(computedUnitToString)){
			System.out.println(Util.displayString(computedUnitToString));
		}
		assertEquals(
			"Invalid completion diet structure" + testName,
			expectedCompletionDietUnitToString,
			computedUnitToString);
	}
}

public void test0001() {

	String s =
		"""
		package a;										\t
		public class X <A {						\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X<A> {
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X<A> {
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
public void test0002() {

	String s =
		"""
		package a;										\t
		public interface X <A {					\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public interface X<A> {
		}
		""";

	String expectedDietPlusBodyUnitToString =
		expectedDietUnitToString;

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
public void test0003() {

	String s =
		"""
		package a;										\t
		public class X <A>						\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X<A> {
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X<A> {
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
public void test0004() {

	String s =
		"""
		package a;										\t
		public class X <A, B						\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X<A, B> {
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X<A, B> {
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
public void test0005() {

	String s =
		"""
		package a;										\t
		public class X <A, B						\t
		   A a;									\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X<A, B> {
		  A a;
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X<A, B> {
		  A a;
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
public void test0006() {

	String s =
		"""
		package a;										\t
		public class X <A extends String, B		\t
		   A a;									\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X<A extends String, B> {
		  A a;
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X<A extends String, B> {
		  A a;
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
public void test0007() {

	String s =
		"""
		package a;										\t
		public class X <A extends				\t
		   A a;									\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X<A extends A> {
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X<A extends A> {
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}

public void test0008() {

	String s =
		"""
		package a;										\t
		public class X <A exteds	B>				\t
		   A a;									\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X<A> {
		  exteds B;
		  A a;
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X<A> {
		  exteds B;
		  A a;
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
public void test0009() {

	String s =
		"""
		package a;										\t
		public class X <A extends>				\t
		   A a;									\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X<A> {
		  A a;
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X<A> {
		  A a;
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}

public void test0010() {

	String s =
		"""
		package a;										\t
		public class X <A extends				\t
		   void foo(){}							\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X<A> {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X<A> {
		  public X() {
		    super();
		  }
		  void foo() {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}

public void test0011() {

	String s =
		"""
		package a;										\t
		public class X <A, B extends				\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X<A, B> {
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X<A, B> {
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
public void test0012() {

	String s =
		"""
		package a;										\t
		public class X <A, B extends	Z			\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X<A, B extends Z> {
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X<A, B extends Z> {
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
public void test0013() {

	String s =
		"""
		package a;										\t
		public class X <A, B extends	Z<			\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X<A, B> {
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X<A, B> {
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
public void test0014() {

	String s =
		"""
		package a;										\t
		public class X <A, B extends	Z<Y			\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X<A, B> {
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X<A, B> {
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
public void test0015() {

	String s =
		"""
		package a;										\t
		public class X <A, B extends	Z<Y>		\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X<A, B extends Z<Y>> {
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X<A, B extends Z<Y>> {
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
public void test0016() {

	String s =
		"""
		package a;										\t
		public class X <A super int> {			\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X<A> {
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X<A> {
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
public void test0017() {

	String s =
		"""
		package a;										\t
		public class X <A<B super int>> {		\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X<A> {
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X<A> {
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
public void test0018() {

	String s =
		"""
		package a;										\t
		public class X <A<B<C super int>>> {		\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X<A> {
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X<A> {
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
public void test0019() {

	String s =
		"""
		package a;										\t
		public class X {		\t
				void foo()[
				  Object o = (Y<Z>.W<U>)e
			\t
				}
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

	String expectedDietPlusBodyPlusStatementsRecoveryUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		    super();
		  }
		  void foo() {
		    Object o = (Y<Z>.W<U>) e;
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyPlusStatementsRecoveryUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
public void test0020() {
	String s =
		"""
		public class X<T> {
		  public class B<U> {}
		  public static void main (String[] args) {
		    X<?>.B[] b = new X<?>.B[1];
		    X<?>.B<?>[] b = new X<?>.B<?>[1];
		    p.p1.X<?>.q.B<?>[] b = new p.p1.X<?>.q.B<?>[1];
		    p.p1.X<?>.q.B[] b = new p.p1.X<?>.q.B[1];
		    p.p1.X<?>[] b = new p.p1.X<?>[1];
		    p.p1.X<String, Integer>.q.B<?>[] b = new p.p1.X<String, Integer>.q.B<?>[1];
		    X<?>.B<?> b = null;
		    p.p1.X<?>.q.B<?> b = null;
		    p.p1.X<String, Integer>.q.B<?> b = null;
		  }
		}""";

	String expectedDietUnitToString =
		"""
		public class X<T> {
		  public class B<U> {
		    public B() {
		    }
		  }
		  public X() {
		  }
		  public static void main(String[] args) {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		public class X<T> {
		  public class B<U> {
		    public B() {
		      super();
		    }
		  }
		  public X() {
		    super();
		  }
		  public static void main(String[] args) {
		    X<?>.B[] b = new X<?>.B[1];
		    X<?>.B<?>[] b = new X<?>.B<?>[1];
		    p.p1.X<?>.q.B<?>[] b = new p.p1.X<?>.q.B<?>[1];
		    p.p1.X<?>.q.B[] b = new p.p1.X<?>.q.B[1];
		    p.p1.X<?>[] b = new p.p1.X<?>[1];
		    p.p1.X<String, Integer>.q.B<?>[] b = new p.p1.X<String, Integer>.q.B<?>[1];
		    X<?>.B<?> b = null;
		    p.p1.X<?>.q.B<?> b = null;
		    p.p1.X<String, Integer>.q.B<?> b = null;
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietPlusBodyUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=113765
public void test0021() {
	String s =
		"""
		import java.util.*;
		public interface X<T> {
			<K> List<Map<K,T> foo(Map<T,K> m);
			<K,E> List<Map<K,E> bar(Map<T,K> m, Map<T,E> e);
		}""";

	String expectedDietUnitToString =
		"""
		import java.util.*;
		public interface X<T> {
		  <K>Map<K, T> foo(Map<T, K> m);
		  <K, E>Map<K, E> bar(Map<T, K> m, Map<T, E> e);
		}
		""";

	String expectedDietPlusBodyUnitToString =
		expectedDietUnitToString;

	String expectedFullUnitToString = expectedDietPlusBodyUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=113765
public void test0022() {
	String s =
		"""
		import java.util.*;
		public interface X<T> {
			<K> List<Map<K,T> foo();
		}""";

	String expectedDietUnitToString =
		"""
		import java.util.*;
		public interface X<T> {
		  <K>Map<K, T> foo();
		}
		""";

	String expectedDietPlusBodyUnitToString =
		expectedDietUnitToString;

	String expectedFullUnitToString = expectedDietPlusBodyUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=113765
public void test0023() {
	String s =
		"""
		import java.util.*;
		public interface X<T> {
			<K>
			List<Map<K,T> foo();
		}""";

	String expectedDietUnitToString =
		"""
		import java.util.*;
		public interface X<T> {
		  Map<K, T> foo();
		}
		""";

	String expectedDietPlusBodyUnitToString =
		expectedDietUnitToString;

	String expectedFullUnitToString = expectedDietPlusBodyUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=113765
public void test0024() {
	String s =
		"""
		import java.util.*;
		public interface X<T> {
			<K> public void foo();
		}""";

	String expectedDietUnitToString =
		"""
		import java.util.*;
		public interface X<T> {
		  public void foo();
		}
		""";

	String expectedDietPlusBodyUnitToString =
		expectedDietUnitToString;

	String expectedFullUnitToString = expectedDietPlusBodyUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=113765
public void test0025() {
	String s =
		"""
		import java.util.*;
		public interface X<T> {
			<K> public List<Map<K,T> foo();
		}""";

	String expectedDietUnitToString =
		"""
		import java.util.*;
		public interface X<T> {
		  public <K>Map<K, T> foo();
		}
		""";

	String expectedDietPlusBodyUnitToString =
		expectedDietUnitToString;

	String expectedFullUnitToString = expectedDietPlusBodyUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=113765
public void test0026() {
	String s =
		"""
		import java.util.*;
		public interface X<T> {
			<K> Map<List<T>,List<K> foo();
		}""";

	String expectedDietUnitToString =
		"""
		import java.util.*;
		public interface X<T> {
		  <T>List<K> foo();
		}
		""";

	String expectedDietPlusBodyUnitToString =
		expectedDietUnitToString;

	String expectedFullUnitToString = expectedDietPlusBodyUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString, testName);
}
}
