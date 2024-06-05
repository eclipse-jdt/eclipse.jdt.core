/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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
import java.util.Map;

import junit.framework.Test;

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

// This test suite test the first implementation of the annotation recovery.
// Tests must be updated with annotation recovery improvment
// TODO(david) update test suite
@SuppressWarnings({ "unchecked", "rawtypes" })
public class AnnotationDietRecoveryTest extends AbstractCompilerTest {
	private static final boolean CHECK_ALL_PARSE = true;
	public static boolean optimizeStringLiterals = false;
	public static long sourceLevel = ClassFileConstants.JDK1_3; //$NON-NLS-1$

public AnnotationDietRecoveryTest(String testName){
	super(testName);
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(testClass(), F_1_5);
}
public static Class testClass() {
	return AnnotationDietRecoveryTest.class;
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
	String expectedFullUnitToString,
	String expectedCompletionDietUnitToString,
	String testName) {

	/* using regular parser in DIET mode */
	if (CHECK_ALL_PARSE){
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
	/* using regular parser in FULL mode */
	if (CHECK_ALL_PARSE){
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
	if (CHECK_ALL_PARSE){
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
	if (CHECK_ALL_PARSE){
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
	if (CHECK_ALL_PARSE){
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
		public @interface X						\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public @interface X {
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
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
public void test0002() {

	String s =
		"""
		package a;										\t
		public @interface X <T> {						\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public @interface X<T> {
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
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
public void test0003() {

	String s =
		"""
		package a;										\t
		public @interface X {						\t
		  String foo()						\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public @interface X {
		  String foo() {
		  }
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
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
public void test0004() {

	String s =
		"""
		package a;										\t
		public @interface X {					\t
		  String foo() default "blabla"		\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public @interface X {
		  String foo() default "blabla" {
		  }
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
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=79770
 */
public void test0005() {

	String s =
		"""
		@Documented		\t
		@Rentention(RententionPolicy.RUNTIME)			\t
		@Target(ElementType.TYPE)						\t
		@interface MyAnn { 								\t
		  String value() default "Default Message"		\t
		}												\t
		public class X {									\t
			public @MyAnn void something() { }				\t
		}												\t
		""";

	String expectedDietUnitToString =
		"""
		@Documented @Rentention(RententionPolicy.RUNTIME) @Target(ElementType.TYPE) @interface MyAnn {
		  String value() default "Default Message" {
		  }
		}
		public class X {
		  public X() {
		  }
		  public @MyAnn void something() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		@Documented @Rentention(RententionPolicy.RUNTIME) @Target(ElementType.TYPE) @interface MyAnn {
		  String value() default "Default Message" {
		  }
		}
		public class X {
		  public X() {
		    super();
		  }
		  public @MyAnn void something() {
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
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
public void test0006() {

	String s =
		"""
		package a;										\t
		public @interface X {					\t
		  String foo() {}		\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public @interface X {
		  String foo() {
		  }
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
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
public void test0007() {

	String s =
		"""
		package a;										\t
		public @interface X {					\t
		  String foo(							\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public @interface X {
		  String foo() {
		  }
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
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
public void test0008() {

	String s =
		"""
		package a;										\t
		public class X {				        	\t
		  void foo(int var1, @Annot(at1=zzz, at2) int var2 {\t
		  }							        	\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  void foo(int var1) {
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
		  void foo(int var1) {
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
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
public void test0009() {

	String s =
		"""
		package a;										\t
		public class X {				        	\t
		  @SuppressWarnings("unchecked");
		  List<Test> l;	\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  @SuppressWarnings("unchecked") List<Test> l;
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X {
		  @SuppressWarnings("unchecked") List<Test> l;
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"""
		package a;
		public class X {
		  List<Test> l;
		  public X() {
		  }
		}
		""";

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
public void test0010() {

	String s =
		"""
		package a;										\t
		public class X {						\t
		  String foo() {						\t
		       @interface Y {					\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  @interface Y {
		  }
		  public X() {
		  }
		  String foo() {
		  }
		}
		""";


	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X {
		  @interface Y {
		  }
		  public X() {
		    super();
		  }
		  String foo() {
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
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=176725
public void test0011() {

	String s =
		"""
		package a;									\t
		public class X {						\t
		  #									\t
		  @AnAnnotation({var})				\t
		  public void foo() {				\t
		  }									\t
		}									\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  public @AnAnnotation({var}) void foo() {
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
		  public @AnAnnotation({var}) void foo() {
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
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=210404
public void test0012() {

	String s =
		"""
		package a;										\t
		public class X {				        	\t
		  void foo(int var1, @Annot(at1=zzz, at2=@Annot(at3=zzz, at4)) int var2 {\t
		  }							        	\t
		}										\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  void foo(int var1) {
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
		  void foo(int var1) {
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
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0013() {

	String s =
		"""
		package a;									\t
		@AnAnnotation(name)					\t
		@AnAnnotation2(name2)				\t
		public class X {						\t
		}									\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public @AnAnnotation(name) @AnAnnotation2(name2) class X {
		  public X() {
		  }
		}
		""";


	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public @AnAnnotation(name) @AnAnnotation2(name2) class X {
		  public X() {
		    super();
		  }
		}
		""";


	String expectedFullUnitToString =
		expectedDietPlusBodyUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0014() {

	String s =
		"""
		package a;									\t
		#									\t
		@AnAnnotation(name)					\t
		@AnAnnotation2(name2)				\t
		public class X {						\t
		}									\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public @AnAnnotation(name) @AnAnnotation2(name2) class X {
		  public X() {
		  }
		}
		""";


	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public @AnAnnotation(name) @AnAnnotation2(name2) class X {
		  public X() {
		    super();
		  }
		}
		""";


	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0015() {

	String s =
		"""
		package a;									\t
		@AnAnnotation(name)					\t
		@AnAnnotation2(name2)				\t
		public class X {						\t
		#									\t
		}									\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public @AnAnnotation(name) @AnAnnotation2(name2) class X {
		  public X() {
		  }
		}
		""";


	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public @AnAnnotation(name) @AnAnnotation2(name2) class X {
		  public X() {
		    super();
		  }
		}
		""";


	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0016() {

	String s =
		"""
		package a;									\t
		@AnAnnotation(name)					\t
		@AnAnnotation2(name2)				\t
		#									\t
		public class X {						\t
		}									\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public @AnAnnotation(name) @AnAnnotation2(name2) class X {
		  public X() {
		  }
		}
		""";


	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public @AnAnnotation(name) @AnAnnotation2(name2) class X {
		  public X() {
		    super();
		  }
		}
		""";


	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		}
		""";

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0017() {

	String s =
		"""
		package a;									\t
		@AnAnnotation(name)					\t
		#									\t
		@AnAnnotation2(name2)				\t
		public class X {						\t
		}									\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public @AnAnnotation(name) @AnAnnotation2(name2) class X {
		  public X() {
		  }
		}
		""";


	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public @AnAnnotation(name) @AnAnnotation2(name2) class X {
		  public X() {
		    super();
		  }
		}
		""";


	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"""
		package a;
		public @AnAnnotation2(name2) class X {
		  public X() {
		  }
		}
		""";

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0018() {

	String s =
		"""
		package a;									\t
		@AnAnnotation(name=)					\t
		@AnAnnotation2(name2)				\t
		public class X {						\t
		}									\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public @AnAnnotation(name = $missing$) @AnAnnotation2(name2) class X {
		  public X() {
		  }
		}
		""";


	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public @AnAnnotation(name = $missing$) @AnAnnotation2(name2) class X {
		  public X() {
		    super();
		  }
		}
		""";


	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"""
		package a;
		public @AnAnnotation2(name2) class X {
		  public X() {
		  }
		}
		""";

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0019() {

	String s =
		"""
		package a;									\t
		@AnAnnotation(name)					\t
		@AnAnnotation2(name2=)				\t
		public class X {						\t
		}									\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public @AnAnnotation(name) @AnAnnotation2(name2 = $missing$) class X {
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public @AnAnnotation(name) @AnAnnotation2(name2 = $missing$) class X {
		  public X() {
		    super();
		  }
		}
		""";


	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		}
		""";

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0020() {

	String s =
		"""
		package a;									\t
		public class X {						\t
		  @AnAnnotation(name) #				\t
		  int field;							\t
		}									\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  @AnAnnotation(name) int field;
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X {
		  @AnAnnotation(name) int field;
		  public X() {
		    super();
		  }
		}
		""";


	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"""
		package a;
		public class X {
		  int field;
		  public X() {
		  }
		}
		""";

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0021() {

	String s =
		"""
		package a;									\t
		public class X {						\t
		  @AnAnnotation(name=)				\t
		  int field;							\t
		}									\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  @AnAnnotation(name = $missing$) int field;
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class X {
		  @AnAnnotation(name = $missing$) int field;
		  public X() {
		    super();
		  }
		}
		""";


	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"""
		package a;
		public class X {
		  int field;
		  public X() {
		  }
		}
		""";

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0022() {

	String s =
		"""
		package a;									\t
		public class X {						\t
		  @AnAnnotation(name) #				\t
		  void foo() {}						\t
		}									\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  @AnAnnotation(name) void foo() {
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
		  @AnAnnotation(name) void foo() {
		  }
		}
		""";


	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0023() {

	String s =
		"""
		package a;									\t
		public class X {						\t
		  @AnAnnotation(name=)				\t
		  void foo() {}						\t
		}									\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  @AnAnnotation(name = $missing$) void foo() {
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
		  @AnAnnotation(name = $missing$) void foo() {
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0024() {

	String s =
		"""
		package a;														\t
		public class X {											\t
		  void foo(int param1, @AnAnnotation(name) # int param2) {}\t
		}														\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  void foo(int param1) {
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
		  void foo(int param1) {
		  }
		}
		""";


	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0025() {

	String s =
		"""
		package a;														\t
		public class X {											\t
		  void foo(int param1, @AnAnnotation(name=) int param2) {}\t
		}														\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  void foo(int param1) {
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
		  void foo(int param1) {
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0026() {

	String s =
		"""
		package a;														\t
		public class X {											\t
		  void foo(int param1, @AnAnnotation(@AnAnnotation1(name1="a", name2=) int param2) {}\t
		}														\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		  void foo(int param1) {
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
		  void foo(int param1) {
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		expectedDietUnitToString;

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0027() {

	String s =
		"""
		package a;																				\t
		@AnAnnotation1(name1="a", #)													\t
		public class X {																	\t
		}																				\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public @AnAnnotation1(name1 = "a") class X {
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public @AnAnnotation1(name1 = "a") class X {
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		}
		""";

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0028() {

	String s =
		"""
		package a;																				\t
		@AnAnnotation1(name1="a", name2=@AnAnnotation2(name3="b"), #)				\t
		public class X {																	\t
		}																				\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public @AnAnnotation1(name1 = "a",name2 = @AnAnnotation2(name3 = "b")) class X {
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public @AnAnnotation1(name1 = "a",name2 = @AnAnnotation2(name3 = "b")) class X {
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		}
		""";

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0030() {

	String s =
		"""
		package a;																				\t
		@AnAnnotation1("a"#)															\t
		public class X {																	\t
		}																				\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public @AnAnnotation1("a") class X {
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public @AnAnnotation1("a") class X {
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		}
		""";

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0031() {

	String s =
		"""
		package a;																				\t
		@AnAnnotation1("a", name2=@AnAnnotation2(name3="b"), #)						\t
		public class X {																	\t
		}																				\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public @AnAnnotation1("a") @AnAnnotation2(name3 = "b") class X {
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public @AnAnnotation1("a") @AnAnnotation2(name3 = "b") class X {
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		}
		""";

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0032() {

	String s =
		"""
		package a;																				\t
		@AnAnnotation1("a", name2=@AnAnnotation2(name3="b"))							\t
		public class X {																	\t
		}																				\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public @AnAnnotation1("a") @AnAnnotation2(name3 = "b") class X {
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public @AnAnnotation1("a") @AnAnnotation2(name3 = "b") class X {
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		}
		""";

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0033() {

	String s =
		"""
		package a;																				\t
		@AnAnnotation1(name=new Object() {})	#											\t
		public class X {																	\t
		}																				\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public @AnAnnotation1(name = new Object() {
		}) class X {
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public @AnAnnotation1(name = new Object() {
		}) class X {
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		}
		""";

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0034() {

	String s =
		"""
		package a;																				\t
		@AnAnnotation1(name=new Object() {},#)											\t
		public class X {																	\t
		}																				\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public @AnAnnotation1(name = new Object() {
		}) class X {
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public @AnAnnotation1(name = new Object() {
		}) class X {
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		}
		""";

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778
public void test0035() {

	String s =
		"""
		package a;																				\t
		@AnAnnotation1(name=new Object() {#})											\t
		public class X {																	\t
		}																				\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public @AnAnnotation1(name = $missing$) class X {
		  public X() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public @AnAnnotation1(name = $missing$) class X {
		  public X() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		}
		""";

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227855
public void test0036() {

	String s =
		"""
		package a;																				\t
		#																				\t
		public class Test {																\t
		  public Test() {}																\t
		  @SuppressWarnings(value="")													\t
		  private int id;																\t
		}																				\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class Test {
		  private @SuppressWarnings(value = "") int id;
		  public Test() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class Test {
		  private @SuppressWarnings(value = "") int id;
		  public Test() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"""
		package a;
		public class Test {
		  private @SuppressWarnings(value = "") int id;
		  public Test() {
		  }
		}
		""";

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227855
public void test0037() {

	String s =
		"""
		package a;																				\t
		#																				\t
		public class Test {																\t
		  public int id0;																\t
		  @SuppressWarnings(value="")													\t
		  private int id;																\t
		}																				\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public class Test {
		  public int id0;
		  private @SuppressWarnings(value = "") int id;
		  public Test() {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public class Test {
		  public int id0;
		  private @SuppressWarnings(value = "") int id;
		  public Test() {
		    super();
		  }
		}
		""";

	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"""
		package a;
		public class Test {
		  public int id0;
		  private @SuppressWarnings(value = "") int id;
		  public Test() {
		  }
		}
		""";

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=228464
public void test0038() {

	String s =
		"""
		package a;									\t
		@AnAnnotation(name=)					\t
		public class X {						\t
		}									\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public @AnAnnotation(name = $missing$) class X {
		  public X() {
		  }
		}
		""";


	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public @AnAnnotation(name = $missing$) class X {
		  public X() {
		    super();
		  }
		}
		""";


	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		}
		""";

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=228464
public void test0039() {

	String s =
		"""
		package a;									\t
		@AnAnnotation(name1=a,name2=)		\t
		public class X {						\t
		}									\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public @AnAnnotation(name1 = a,name2 = $missing$) class X {
		  public X() {
		  }
		}
		""";


	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public @AnAnnotation(name1 = a,name2 = $missing$) class X {
		  public X() {
		    super();
		  }
		}
		""";


	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		}
		""";

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=228464
public void test0040() {

	String s =
		"""
		package a;									\t
		@AnAnnotation(name1=a,name2=,name3=c)\t
		public class X {						\t
		}									\t
		""";

	String expectedDietUnitToString =
		"""
		package a;
		public @AnAnnotation(name1 = a,name2 = $missing$) class X {
		  public X() {
		  }
		}
		""";


	String expectedDietPlusBodyUnitToString =
		"""
		package a;
		public @AnAnnotation(name1 = a,name2 = $missing$) class X {
		  public X() {
		    super();
		  }
		}
		""";


	String expectedFullUnitToString =
		expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
		"""
		package a;
		public class X {
		  public X() {
		  }
		}
		""";

	String testName = "<generic type recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=366003
public void test0041() {

	String s =
			"""
		package snippet;
		public class Bug366003 {
		        void foo(Object o1){}
		        @Blah org.User(@Bla String str){}
		}
		""";

	String expectedDietUnitToString =
			"""
		package snippet;
		public class Bug366003 {
		  public Bug366003() {
		  }
		  void foo(Object o1) {
		  }
		  @Blah User(@Bla String str) {
		  }
		}
		""";

	String expectedDietPlusBodyUnitToString =
			"""
		package snippet;
		public class Bug366003 {
		  public Bug366003() {
		    super();
		  }
		  void foo(Object o1) {
		  }
		  @Blah User(@Bla String str) {
		  }
		}
		""";

	String expectedFullUnitToString = expectedDietUnitToString;

	String expectedCompletionDietUnitToString =
			"""
		package snippet;
		public class Bug366003 {
		  public Bug366003() {
		  }
		  void foo(Object o1) {
		  }
		  User(@Bla String str) {
		  }
		}
		""";

	String testName = "<annotation recovery>";
	checkParse(
		s.toCharArray(),
		expectedDietUnitToString,
		expectedDietPlusBodyUnitToString,
		expectedFullUnitToString,
		expectedCompletionDietUnitToString,
		testName);
}
}
